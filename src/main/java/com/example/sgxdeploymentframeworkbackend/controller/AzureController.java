package com.example.sgxdeploymentframeworkbackend.controller;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.DeviceCodeCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.subscription.SubscriptionManager;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.example.sgxdeploymentframeworkbackend.TokenCacheAspect;
import com.example.sgxdeploymentframeworkbackend.constants.DeploymentConstants;
import com.microsoft.aad.msal4j.*;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.reader.StreamReader;

import java.io.*;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@RestController
@RequestMapping("/azure")
@Slf4j
public class AzureController {

    @Autowired
    private DeploymentConstants deploymentConstants;

    private String resolveFilePath(String path){
        File file = new File(path);
        System.out.println(file.getAbsolutePath());
        return file.getAbsolutePath();
    }

    @PostMapping("/authorize")
    public String authorizeAzureAccount() throws Exception {
//        String scriptPath = resolveFilePath("authorize_script.py");
////        File error = new File(resolveFilePath("login-error.txt"));
////        boolean test = error.createNewFile();
////        System.out.println(test);
//        ProcessBuilder processBuilder = new
//                ProcessBuilder("python3", scriptPath)
//                .inheritIO();
//
//        Process workerProcess = processBuilder.start();
//
//        Thread.sleep(8000);
//        workerProcess.destroyForcibly();
//        Thread.sleep(8000);
//
//        BufferedReader bufferedReader = new BufferedReader(new FileReader(resolveFilePath("login-error1.txt")));
//
//
////        BufferedReader bufferedReader = new BufferedReader(
////                new InputStreamReader(
////                        workerProcess.getErrorStream()
////                ));
//        String Output_line = "";
////
//        while ((Output_line = bufferedReader.readLine()) != null) {
//            log.info(Output_line);
//            System.out.println(Output_line);
//        }
//        bufferedReader.close();
//        return Output_line;

        DeviceCodeCredential deviceCodeCredential = new DeviceCodeCredentialBuilder()
                .challengeConsumer(challenge -> {
                    // lets user know of the challenge
                    System.out.println(challenge.getMessage());
                }).build();


//        SubscriptionManager subscriptionManager = SubscriptionManager.authenticate(deviceCodeCredential, azureProfile);
//        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
//        TokenCredential credential = new DefaultAzureCredentialBuilder()
//                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
//                .build();

        SecretClient client = new SecretClientBuilder()
                .vaultUrl("https://testbodoo.vault.azure.net/")
                .credential(deviceCodeCredential)
                .buildClient();
//        AzureResourceManager azureResourceManager = AzureResourceManager.authenticate();
        System.out.println(client.getSecret("test1").getValue());

        AzureProfile azureProfile = new AzureProfile(AzureEnvironment.AZURE);
        System.out.println(azureProfile.getSubscriptionId());
        return "YES";
    }

    @GetMapping("/unauthorize")
    public String unauthorizeAccount() throws IOException, InterruptedException {
        String scriptPath = resolveFilePath("unauthorize_script.py");
        String loggedInUser = deploymentConstants.getLoggedInUser();
        ProcessBuilder processBuilder = new
                ProcessBuilder("python3",scriptPath, loggedInUser)
                .inheritIO();

        Process workerProcess = processBuilder.start();
        workerProcess.waitFor();
//
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(
                        workerProcess.getInputStream()
                ));
        String Output_line = "";


        while ((Output_line = bufferedReader.readLine()) != null) {
            System.out.println(Output_line);
        }

        return "YES";
    }

    @GetMapping("/deploy")
    public String beginDeployment() throws IOException, InterruptedException {
        String scriptPath = resolveFilePath("deploy_script.py");
        ProcessBuilder processBuilder = new
                ProcessBuilder("python3", scriptPath,
                deploymentConstants.getSusbscriptionId(),
                deploymentConstants.getResourceGroupName(),
                deploymentConstants.getLocation(),
                deploymentConstants.getVnetName(),
                deploymentConstants.getIpName())
//                deploymentConstants.getIpConfigName(),
//                deploymentConstants.getPublicKeyName(),
//                deploymentConstants.getPrivateKeyName(),
//                deploymentConstants.getNsgName(),
//                deploymentConstants.getNicName(),
//                deploymentConstants.getVmName(),
//                deploymentConstants.getUsername(),
//                deploymentConstants.getPassword(),
//                deploymentConstants.getSecurityRuleInboundName(),
//                deploymentConstants.getSecurityRuleOutboundName(),
//                deploymentConstants.getSecurityRuleSshName())
                .inheritIO();

        Process workerProcess = processBuilder.start();
        workerProcess.waitFor();

        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(
                        workerProcess.getInputStream()
                ));
        String Output_line = "";

        while ((Output_line = bufferedReader.readLine()) != null) {
            System.out.println(Output_line);
        }
        return Output_line;
    }


}
