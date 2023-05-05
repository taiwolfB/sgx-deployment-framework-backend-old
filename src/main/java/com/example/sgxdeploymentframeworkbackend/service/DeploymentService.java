package com.example.sgxdeploymentframeworkbackend.service;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DeviceCodeCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.fluent.models.SshPublicKeyGenerateKeyPairResultInner;
import com.azure.resourcemanager.compute.fluent.models.SshPublicKeyResourceInner;
import com.azure.resourcemanager.compute.implementation.VirtualMachinesImpl;
import com.azure.resourcemanager.compute.models.*;
import com.azure.resourcemanager.network.models.*;
import com.azure.resourcemanager.network.models.IpVersion;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.example.sgxdeploymentframeworkbackend.config.WebSocketListener;
import com.example.sgxdeploymentframeworkbackend.constants.DeploymentProperties;
import com.example.sgxdeploymentframeworkbackend.dto.AuthDto;
import com.example.sgxdeploymentframeworkbackend.dto.AuthResponseDto;
import com.example.sgxdeploymentframeworkbackend.dto.DeploymentDto;
import com.example.sgxdeploymentframeworkbackend.dto.WebSocketDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

@Service
@Slf4j
public class DeploymentService {

    @Autowired
    private WebSocketListener webSocketListener;

    @Autowired
    private DeploymentProperties deploymentProperties;


    private AzureResourceManager azureResourceManager;

    public AuthResponseDto authorize(AuthDto authDto) {
        AuthResponseDto authResponseDto = new AuthResponseDto();
        DeviceCodeCredential deviceCodeCredential = new DeviceCodeCredentialBuilder()
                .tenantId(authDto.getTenantId())
                .challengeConsumer(challenge -> {
                    System.out.println(challenge.getMessage());
                    WebSocketDto webSocketDto = new WebSocketDto();
                    webSocketDto.setUrl("https://microsoft.com/devicelogin");
                    webSocketDto.setDeviceCode(challenge.getMessage().substring(100, 109));
                    webSocketListener.pushSystemStatusToWebSocket(webSocketDto);
                })
                .build();

        AzureProfile azureProfile = new AzureProfile(AzureEnvironment.AZURE);
        azureResourceManager  = AzureResourceManager
                .authenticate(deviceCodeCredential, azureProfile)
                .withSubscription(authDto.getSubscriptionId());

        azureResourceManager.accessManagement().activeDirectoryUsers().list().forEach(r -> {
            authResponseDto.setLoggedInUser(r.name());
        });


        authResponseDto.setMessage("Authentication successful.");
        authResponseDto.setHttpCode(200);
        deploymentProperties.setSubscriptionId(authDto.getSubscriptionId());
        deploymentProperties.setTenantId(authDto.getTenantId());
        return authResponseDto;
    }

    private List<String> createScript() {
        List<String> script = new ArrayList<>();
        script.add("echo set debconf to Noninteractive");
        script.add("echo \"debconf debconf/frontend select Noninteractive\" | sudo debconf-set-selections");
        script.add("yes yes | sudo apt-get install build-essential git gcc -y -q");
        script.add("yes yes | sudo apt-get install build-essential");
        script.add("yes | sudo apt -y install g++");
        script.add("cd /opt");
        script.add("mkdir intel");
        script.add("cd intel");
        script.add("echo \"deb [arch=amd64] https://download.01.org/intel-sgx/sgx_repo/ubuntu focal main\" | sudo tee /etc/apt/sources.list.d/intel-sgx.list");
        script.add("sudo wget -qO - https://download.01.org/intel-sgx/sgx_repo/ubuntu/intel-sgx-deb.key | sudo apt-key add");
        script.add("sudo apt-get update");
        script.add("yes | sudo apt-get install libsgx-epid libsgx-quote-ex libsgx-dcap-ql libsgx-uae-service");
        script.add("yes | sudo apt-get install libsgx-urts-dbgsym libsgx-enclave-common-dbgsym libsgx-dcap-ql-dbgsym libsgx-dcap-default-qpl-dbgsym");
        script.add("yes | sudo apt-get install libsgx-dcap-default-qpl libsgx-launch libsgx-urts");
        script.add("sudo wget - https://download.01.org/intel-sgx/latest/linux-latest/distro/ubuntu20.04-server/sgx_linux_x64_sdk_2.19.100.3.bin");
        script.add("sudo chmod +x sgx_linux_x64_sdk_2.19.100.3.bin");
        script.add("yes yes | sudo ./sgx_linux_x64_sdk_2.19.100.3.bin");
        script.add(". /opt/intel/sgxsdk/environment");
        script.add("yes | sudo apt-get install libsgx-enclave-common-dev libsgx-dcap-ql-dev libsgx-dcap-default-qpl-dev");
        script.add("export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/opt/intel/sgxsdk/SampleCode/RemoteAttestation/sample_libcrypto");
        script.add("set LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/opt/intel/sgxsdk/SampleCode/RemoteAttestation/sample_libcrypto");
//        # SETUP NODEJS for PCCS install
        script.add("sudo curl -sL https://deb.nodesource.com/setup_16.x | sudo -E bash -");
        script.add("yes | sudo apt-get install -y nodejs");
        script.add("yes | sudo apt-get install python3 cracklib-runtime");
//        #instal DCAP PCCS & CONFIGURE
        script.add("yes | sudo apt-get install expect autotools-dev automake libssl-dev");
        script.add("cd /home/azureuser/");
        script.add("wget https://www.openssl.org/source/openssl-1.1.1i.tar.gz");
        script.add("tar xf openssl-1.1.1i.tar.gz");
        script.add("cd openssl-1.1.1i");
        script.add("./config --prefix=/opt/openssl/1.1.1i --openssldir=/opt/openssl/1.1.1i");
        script.add("sudo make");
        script.add("sudo make install");
        script.add("cd ../");
        script.add("sudo git clone https://github.com/taiwolfB/sgx-deployment-framework-remote-attestation");
        script.add("cd sgx-deployment-framework-remote-attestation");
        script.add("sudo chmod 777 install_dcap_pccs.exp");
        script.add("sudo expect install_dcap_pccs.exp");
        script.add("sudo ./bootstrap");
        script.add("sudo ./configure --with-openssldir=/opt/openssl/1.1.1i");
        script.add("sudo make");
        //script.add("sudo ./run-server 8085");
//        script.add("sudo git clone https://github.com/taiwolfB/IntelSGX_LINUX_SAMPLES.git");
//        script.add("cd IntelSGX_LINUX_SAMPLES");
//        script.add("sudo chmod 777 install_dcap_pccs.exp");
//        script.add("sudo expect install_dcap_pccs.exp");
//        script.add("cd ../");
//        script.add("sudo git clone https://github.com/taiwolfB/INTEL_SGX_RA_SAMPLE_UPDATED_.git");
        return script;
    }

    private void updateDeploymentProperties() {
        Integer previousResourceGroupNumber = 0;
        for (ResourceGroup resourceGroup: azureResourceManager.resourceGroups().list()) {
            if (resourceGroup.name().startsWith(deploymentProperties.getResourceGroupName())) {
                StringTokenizer st = new StringTokenizer(resourceGroup.name(),deploymentProperties.getResourceGroupName());
                if (st.hasMoreTokens()) {
                    previousResourceGroupNumber = Integer.parseInt(st.nextElement().toString());
                }
            }
        }
        deploymentProperties.setNextResourceNumber(previousResourceGroupNumber + 1);
    }

    public DeploymentDto deploy() throws IOException {
        updateDeploymentProperties();
        ResourceGroup resourceGroup = azureResourceManager.resourceGroups()
                .define(deploymentProperties.getResourceGroupName() + deploymentProperties.getNextResourceNumber())
                .withRegion(deploymentProperties.getLocation())
                .create();
        log.info("Provisioned Resource Group " + resourceGroup.name());

        Network network = azureResourceManager.networks()
                .define(deploymentProperties.getVnetName() + deploymentProperties.getNextResourceNumber())
                .withRegion(deploymentProperties.getLocation())
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace(deploymentProperties.getVnetAddressSpace())
                .withSubnet(deploymentProperties.getSubnetName() + deploymentProperties.getNextResourceNumber(),
                        deploymentProperties.getSubnetAddressSpace())
                .create();
        log.info("Provisioned virtual network "
                + network.name()
                + " with address prefix "
                + network.addressSpaces()
                + " and Subnet Address space "
                + deploymentProperties.getSubnetAddressSpace());

        PublicIpAddress ipAddress = azureResourceManager.publicIpAddresses()
                .define(deploymentProperties.getIpName() + deploymentProperties.getNextResourceNumber())
                .withRegion(deploymentProperties.getLocation())
                .withExistingResourceGroup(resourceGroup)
                .withStaticIP()
                .withSku(PublicIPSkuType.STANDARD)
                .withIpAddressVersion(IpVersion.IPV4)
                .create();
        log.info("Provisioned ip address "
                + ipAddress.name()
                + " with address "
                + ipAddress.ipAddress());

        NetworkSecurityGroup networkSecurityGroup = azureResourceManager.networkSecurityGroups()
                .define(deploymentProperties.getNsgName() + deploymentProperties.getNextResourceNumber())
                .withRegion(deploymentProperties.getLocation())
                .withExistingResourceGroup(resourceGroup)
                .defineRule(deploymentProperties.getSecurityRuleInboundName())
                .allowInbound()
                .fromAnyAddress()
                .fromAnyPort()
                .toAnyAddress()
                .toAnyPort()
                .withProtocol(SecurityRuleProtocol.TCP)
                .withPriority(deploymentProperties.getSecurityRulePriority())
                .withDescription(deploymentProperties.getSecurityRuleInboundDescription())
                .attach()
                .defineRule(deploymentProperties.getSecurityRuleOutboundName())
                .allowOutbound()
                .fromAnyAddress()
                .fromAnyPort()
                .toAnyAddress()
                .toAnyPort()
                .withProtocol(SecurityRuleProtocol.TCP)
                .withPriority(deploymentProperties.getSecurityRulePriority())
                .withDescription(deploymentProperties.getSecurityRuleOutboundDescription())
                .attach()
                .create();
        log.info("Provisioned Network Security group " + networkSecurityGroup.name());

        NetworkInterface networkInterface= azureResourceManager.networkInterfaces()
                .define(deploymentProperties.getNicName() + deploymentProperties.getNextResourceNumber())
                .withRegion(deploymentProperties.getLocation())
                .withExistingResourceGroup(resourceGroup)
                .withExistingPrimaryNetwork(network)
                .withSubnet(deploymentProperties.getSubnetName() + deploymentProperties.getNextResourceNumber())
                .withPrimaryPrivateIPAddressDynamic()
                .withExistingPrimaryPublicIPAddress(ipAddress)
                .withExistingNetworkSecurityGroup(networkSecurityGroup)
                .create();
        log.info("Provisioned network interface " + networkInterface.name());

        azureResourceManager
                .virtualMachines()
                .manager()
                .serviceClient()
                .getSshPublicKeys()
                .create(resourceGroup.name(),
                        deploymentProperties.getPublicKeyName() + deploymentProperties.getNextResourceNumber(),
                        new SshPublicKeyResourceInner()
                                .withLocation(deploymentProperties.getLocation()));
        SshPublicKeyGenerateKeyPairResultInner sshPublicKeyGenerateKeyPairResultInner = azureResourceManager
                .virtualMachines()
                .manager()
                .serviceClient()
                .getSshPublicKeys()
                .generateKeyPair(resourceGroup.name(),
                        deploymentProperties.getPublicKeyName() + deploymentProperties.getNextResourceNumber());
        log.info("Provisioned ssh keys");

        FileWriter fileWriter = new FileWriter(deploymentProperties.getPrivateKeyName()
                + deploymentProperties.getNextResourceNumber()
                + ".pem");
        fileWriter.write(sshPublicKeyGenerateKeyPairResultInner.privateKey());
        fileWriter.close();

        System.out.println(sshPublicKeyGenerateKeyPairResultInner.privateKey());

        log.info("Provisioning virtual machine, this might take a few minutes.");
        SshPublicKey sshPublicKey = new SshPublicKey()
                .withPath("/home/" + deploymentProperties.getUsername() + "/.ssh/authorized_keys")
                .withKeyData(sshPublicKeyGenerateKeyPairResultInner.publicKey());
        SshConfiguration sshConfiguration = new SshConfiguration()
                .withPublicKeys(Collections.singletonList(sshPublicKey));
        LinuxConfiguration linuxConfiguration = new LinuxConfiguration()
                .withDisablePasswordAuthentication(true)
                .withSsh(sshConfiguration);


        VirtualMachine virtualMachine = azureResourceManager.virtualMachines()
                .define(deploymentProperties.getVmName() + deploymentProperties.getNextResourceNumber())
                .withRegion(deploymentProperties.getLocation())
                .withExistingResourceGroup(resourceGroup)
                .withExistingPrimaryNetworkInterface(networkInterface)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_20_04_LTS_GEN2)
                .withRootUsername(deploymentProperties.getUsername())
                .withRootPassword(deploymentProperties.getPassword())
                .withSsh(sshPublicKey.keyData())
                .withComputerName(deploymentProperties.getComputerName() + deploymentProperties.getResourceGroupName())
                .withSize(deploymentProperties.getVmSize())
                .create();

        log.info("Provisioned virtual machine " + virtualMachine.name());

//        virtualMachine.powerOff();
//        azureResourceManager
//                .virtualMachines()
//                .manager()
//                .serviceClient()
//                .getVirtualMachines()
//                .update(
//                        resourceGroup.name(),
//                        virtualMachine.name(),
//                        new VirtualMachineUpdateInner()
//                                .withOsProfile(
//                                        new OSProfile()
//                                                .withComputerName(deploymentProperties.getComputerName() + deploymentProperties.getNextResourceNumber())
//                                                .withAdminPassword(deploymentProperties.getPassword())
//                                                .withAdminUsername(deploymentProperties.getUsername())
//                                                .withLinuxConfiguration(linuxConfiguration)));
//
//        virtualMachine.start();
        log.info("Running startup script.");
        RunCommandInput runCommandInput = new RunCommandInput()
                .withCommandId("RunShellScript")
                .withScript(createScript());

        RunCommandResult runCommandResult = azureResourceManager
                .virtualMachines()
                .runCommand(resourceGroup.name(), virtualMachine.name(), runCommandInput);

        log.info("Initialize script result: ");
        runCommandResult.value().forEach(l -> log.info(l.message()));
        return new DeploymentDto();
    }


    private String resolveFilePath(String path){
        File file = new File(path);
        System.out.println(file.getAbsolutePath());
        return file.getAbsolutePath();
    }
}
