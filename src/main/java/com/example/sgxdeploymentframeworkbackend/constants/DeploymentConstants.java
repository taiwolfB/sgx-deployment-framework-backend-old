package com.example.sgxdeploymentframeworkbackend.constants;

import lombok.Data;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "deployment")
@Data
public class DeploymentConstants {

    private String loggedInUser;
    private String susbscriptionId;
    private String resourceGroupName;
    private String location;
    private String vnetName;
    private String subnetName;
    private String ipName;
    private String ipConfigName;
    private String publicKeyName;
    private String privateKeyName;
    private String nsgName;
    private String nicName;
    private String vmName;
    private String username;
    private String password;
    private String securityRuleInboundName;
    private String securityRuleOutboundName;
    private String securityRuleSshName;

}
