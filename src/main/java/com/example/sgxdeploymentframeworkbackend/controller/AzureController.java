package com.example.sgxdeploymentframeworkbackend.controller;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.fluent.models.SshPublicKeyGenerateKeyPairResultInner;
import com.azure.resourcemanager.compute.fluent.models.SshPublicKeyResourceInner;
import com.azure.resourcemanager.compute.models.*;
import com.azure.resourcemanager.network.models.*;
import com.azure.resourcemanager.network.models.IpVersion;
//import com.azure.resourcemanager.subscription.SubscriptionManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.example.sgxdeploymentframeworkbackend.config.WebSocketListener;
//import com.microsoft.aad.msal4j.*;
//import com.microsoft.azure.management.resources.ResourceGroup;
//import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.example.sgxdeploymentframeworkbackend.constants.DeploymentProperties;
import com.example.sgxdeploymentframeworkbackend.dto.AuthDto;
import com.example.sgxdeploymentframeworkbackend.dto.AuthResponseDto;
import com.example.sgxdeploymentframeworkbackend.dto.DeploymentDto;
//import com.microsoft.azure.management.resources.ResourceGroup;
import com.example.sgxdeploymentframeworkbackend.service.DeploymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/azure")
@Slf4j
public class AzureController {

    @Autowired
    private DeploymentProperties deploymentProperties;

    @Autowired
    private Environment environment;

    @Autowired
    private WebSocketListener webSocketListener;

    private AzureResourceManager azureResourceManager;

    @Autowired
    private DeploymentService deploymentService;


    @PostMapping("/authorize")
    public AuthResponseDto authorizeAzureAccount(@RequestBody AuthDto authDto){
        AuthResponseDto authResponseDto = new AuthResponseDto();
        try {
           authResponseDto = deploymentService.authorize(authDto);
           return authResponseDto;
        } catch (Exception ex) {
            authResponseDto.setMessage("Failed to authenticate user due to wrong subscription id or wrong tenant id.");
            authResponseDto.setHttpCode(400);
            return authResponseDto;
        }
    }

    @PostMapping("/deploy")
    public DeploymentDto deploy() throws IOException {
        deploymentService.deploy();
        return new DeploymentDto();
    }

    @GetMapping("/unauthorize")
    public String unauthorizeAccount() throws IOException, InterruptedException {
        return "YES";
    }
}
