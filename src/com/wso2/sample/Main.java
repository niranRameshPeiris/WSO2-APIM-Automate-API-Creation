package com.wso2.sample;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import static com.wso2.sample.Configuration.SP_NAME;
import static com.wso2.sample.RESTCalls.*;

public class Main {
    public static void main(String[] args) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        String AccessToken = null;

        System.out.println("==============================================");
        System.out.println("============ Starting Java Client ============");
        System.out.println("==============================================");

        int numberOfTenants = 2;
        int numberOfAPIs = 10;

        for (int i=0;i<numberOfTenants;i++){
            // tenant variables
            String tenantUsername = "niran";
            String tenantPassword = "admin";
            String tenantDomain = "test"+i+".wso2.com";
            String firstname = "niran";
            String lastname = "peiris";

            // Create Tenant
            CreateTenant(tenantDomain, tenantUsername,  tenantPassword,  firstname,  lastname);
            // Register Client in tenant
            String ClientCredentials = ClientRegistration(tenantUsername +"@"+tenantDomain,tenantPassword,SP_NAME);

            try {
                Thread.sleep(100);
            } catch(InterruptedException e) {
                System.out.println("An Exception Occurred: " + e);
            }

            // Get the Access Token for tenant
            if( ClientCredentials != null){
                AccessToken = GettingAccessToken(tenantUsername +"@"+tenantDomain,tenantPassword,ClientCredentials,"apim:api_view apim:api_publish apim:api_create");
            }

            try {
                Thread.sleep(100);
            } catch(InterruptedException e) {
                System.out.println("An Exception Occurred: " + e);
            }

            for (int k=0;k<numberOfAPIs;k++){

                // CreateAPIs in tenant
                String name = "WSO2-APIM-32-Test-API-"+k;
                String context = "wso2apim32testapi"+k;

                API api = CreateAPI(AccessToken,name,"/t/"+tenantDomain+context,tenantUsername +"@"+tenantDomain);

                try {
                    Thread.sleep(100);
                } catch(InterruptedException e) {
                    System.out.println("An Exception Occurred: " + e);
                }

                // Publish API
                APIStateChange(AccessToken, api.getId(), api.getName(),"Publish");
            }
        }

        System.out.println("==============================================");
        System.out.println("============ Java Client Completed ===========");
        System.out.println("==============================================");

    }


}