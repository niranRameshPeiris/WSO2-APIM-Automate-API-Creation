package com.wso2.sample;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Base64;

import static com.wso2.sample.Configuration.*;

public class RESTCalls {

    public static void APIStateChange(String AccessToken, String Id, String Name , String State) throws IOException {

        URL obj = new URL(PROTOCOL +MGT_HOSTNAME+":"+MGT_PORT+ REPUBLISH_REST_API +"?apiId="+Id+"&action="+State );
        HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Authorization", "Bearer "+AccessToken);
        httpURLConnection.setDoOutput(true);

        int responseCode = httpURLConnection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("============ API : "+Name+"("+Id+") : Successfully Changed the State to "+State+" State ============");

        } else {
            System.out.println(responseCode + "Error Changing API :"+Name+" : State ....");
        }
    }
    public static API CreateAPI(String AccessToken,String Name, String Context ,String Owner) throws IOException {

        API api = new API();
        String jsonInputString = "{\n" +
                "    \"name\": \""+Name+"\",\n" +
                "    \"description\": \"This is a simple API.\",\n" +
                "    \"context\": \""+Context+"\",\n" +
                "    \"version\": \"1.0.0\",\n" +
                "    \"provider\": \""+Owner+"\",\n" +
                "    \"lifeCycleStatus\": \"PUBLISHED\",\n" +
                "    \"policies\": [\"Unlimited\"],\n" +
                "    \"apiThrottlingPolicy\": \"Unlimited\",\n" +
                "    \"gatewayEnvironments\": [\"Production and Sandbox\"],\n" +
                "    \"endpointConfig\": {\n" +
                "        \"endpoint_type\": \"http\",\n" +
                "        \"sandbox_endpoints\": {\n" +
                "            \"url\": \"https://localhost:9443/am/sample/pizzashack/v1/api/\"\n" +
                "        },\n" +
                "        \"production_endpoints\": {\n" +
                "            \"url\": \"https://localhost:9443/am/sample/pizzashack/v1/api/\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"endpointImplementationType\": \"ENDPOINT\",\n" +
                "    \"scopes\": [],\n" +
                "    \"keyManagers\": [\n" +
                "        \"all\"\n" +
                "    ]\n" +
                "}";

        URL obj = new URL(PROTOCOL +MGT_HOSTNAME+":"+MGT_PORT+CREATE_REST_API);
        HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        httpURLConnection.setRequestProperty("Authorization", "Bearer "+AccessToken);
        httpURLConnection.setDoOutput(true);

        try(OutputStream os = httpURLConnection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = httpURLConnection.getResponseCode();
        System.out.println("Create API Call Response Code :" + responseCode);

        if (responseCode == HttpURLConnection.HTTP_CREATED) {
            BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in .readLine()) != null) {
                response.append(inputLine);
            } in .close();

            System.out.println("Create API Call  Response: " + response.toString());
            JsonObject jsonObject = new JsonParser().parse(response.toString()).getAsJsonObject();

            api.setId(jsonObject.get("id").getAsString());
            api.setName(jsonObject.get("name").getAsString());

        } else {
            System.out.println(responseCode + "Error Creating API :"+Name);
        }

        return api;
    }

    // pass the scopes as below
    // "apim:api_view apim:api_publish apim:api_create"
    public static String GettingAccessToken(String Username, String Password, String ClientCredentials, String scopes) throws IOException {

        System.out.println("============ Generating Access Token ============");

        String access_token =null;

        URL obj = new URL(PROTOCOL +MGT_HOSTNAME+":"+MGT_PORT+TOKEN_ENDPOINT );
        HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Authorization", "Basic "+ClientCredentials);
        httpURLConnection.setDoOutput(true);
        OutputStream os = httpURLConnection.getOutputStream();
        os.write(("grant_type=password&username="+Username+"&password="+Password+"&scope="+scopes).getBytes());
        os.flush();
        os.close();

        int responseCode = httpURLConnection.getResponseCode();
        System.out.println("Token Call Response Code :" + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in .readLine()) != null) {
                response.append(inputLine);
            } in .close();

            System.out.println("Token Endpoint Response: " + response.toString());
            JsonObject jsonObject = new JsonParser().parse(response.toString()).getAsJsonObject();
            access_token = jsonObject.get("access_token").getAsString();
            System.out.println("Access Token : " + access_token);

        } else {
            System.out.println("Error connecting to Token Endpoint ....");
        }

        System.out.println("============ Access Token Generated ============");
        return access_token;
    }

    public static String ClientRegistration(String Username, String Password, String AppName) throws IOException {

        System.out.println("============ Starting Client Registration ============");

        String CLIENT_ID = null;
        String CLIENT_SECRET = null;
        String ENCODED_VALUE = null;

        String encodedCredentials = Base64.getEncoder().encodeToString((Username +":"+ Password).getBytes());
        System.out.println( "Base64 Encoded Credentials: " + encodedCredentials);
        String jsonInputString = "{\"callbackUrl\": \"www.niran.lk\", \"clientName\": \""+ AppName +"\", \"owner\": \""+ Username + "\", \"grantType\": \"password refresh_token\", \"saasApp\": true}";

        URL obj = new URL(PROTOCOL +MGT_HOSTNAME+":"+MGT_PORT+DCR_ENDPOINT);
        HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        httpURLConnection.setRequestProperty("Authorization", "Basic "+encodedCredentials);
        httpURLConnection.setDoOutput(true);

        try(OutputStream os = httpURLConnection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = httpURLConnection.getResponseCode();
        System.out.println("Client Registration Call Response Code :" + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in .readLine()) != null) {
                response.append(inputLine);
            } in .close();

            System.out.println("DCR Endpoint Response: " + response.toString());
            JsonObject jsonObject = new JsonParser().parse(response.toString()).getAsJsonObject();
            CLIENT_ID = jsonObject.get("clientId").getAsString();
            CLIENT_SECRET = jsonObject.get("clientSecret").getAsString();
            ENCODED_VALUE= Base64.getEncoder().encodeToString((CLIENT_ID +":"+ CLIENT_SECRET).getBytes());
            System.out.println("Client ID : " +CLIENT_ID );
            System.out.println("Client Secret : " +CLIENT_SECRET );
            System.out.println("Client Credentials : " +ENCODED_VALUE );
        } else {
            System.out.println("Error connecting to DCR Endpoint ....");
        }

        System.out.println("============ Client Registration Completed ============");
        return ENCODED_VALUE;
    }

    public static void CreateTenant(String domain,String username, String password, String firstname, String lastname) throws IOException {

        System.out.println("============ Creating Tenant ============");

        String encodedCredentials = Base64.getEncoder().encodeToString((ADMIN_USERNAME +":"+ ADMIN_PASSWORD).getBytes());

        String xmlInputString = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.mgt.tenant.carbon.wso2.org\" xmlns:xsd=\"http://beans.common.stratos.carbon.wso2.org/xsd\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <ser:addTenant>\n" +
                "         <ser:tenantInfoBean>\n" +
                "            <xsd:active>true</xsd:active>\n" +
                "            <xsd:admin>"+username+"</xsd:admin>\n" +
                "            <xsd:adminPassword>"+password+"</xsd:adminPassword>\n" +
                "            <xsd:email>"+username+"@"+domain+"</xsd:email>\n" +
                "            <xsd:firstname>"+firstname+"</xsd:firstname>\n" +
                "            <xsd:lastname>"+lastname+"</xsd:lastname>\n" +
                "            <xsd:tenantDomain>"+domain+"</xsd:tenantDomain>\n" +
                "         </ser:tenantInfoBean>\n" +
                "      </ser:addTenant>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";


        URL obj = new URL(PROTOCOL +MGT_HOSTNAME+":"+MGT_PORT+CREATE_TENANT_SOAP_ENDPOINT);
        HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
        httpURLConnection.setRequestProperty("SOAPAction", "urn:addTenant");
        httpURLConnection.setRequestProperty("Authorization", "Basic "+encodedCredentials);
        httpURLConnection.setDoOutput(true);

        try(OutputStream os = httpURLConnection.getOutputStream()) {
            byte[] input = xmlInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = httpURLConnection.getResponseCode();
        System.out.println("Admin Service Call Response Code :" + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in .readLine()) != null) {
                response.append(inputLine);
            } in .close();

            System.out.println("Admin Service Call Response: " + response.toString());

        } else {
            System.out.println("Error connecting to SOAP Endpoint ....");
        }

        System.out.println("============ Tenant Created ============");
    }

    static TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
    } };
}
