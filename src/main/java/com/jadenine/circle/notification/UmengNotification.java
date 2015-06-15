package com.jadenine.circle.notification;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class UmengNotification {
    // This Map is used for constructing the whole request string.
    protected final Map<String, Object> notification = new HashMap<String, Object>();

    // The host
    protected static final String host = "http://msg.umeng.com";

    // The upload path
    protected static final String uploadPath = "/upload";

    // The post path
    protected static final String postPath = "/api/send";

    // The app master secret
    protected String appMasterSecret;

    // The user agent
    protected final String USER_AGENT = "Mozilla/5.0";

    // Keys can be set in the root level
    protected static final HashSet<String> ROOT_KEYS = new HashSet<String>(Arrays.asList(new
            String[]{
            "appkey", "timestamp", "type", "device_tokens", "alias", "alias_type", "file_id",
            "filter", "production_mode", "feedback", "description", "thirdparty_id"}));

    // Keys can be set in the policy level
    protected static final HashSet<String> POLICY_KEYS = new HashSet<String>(Arrays.asList(new
            String[]{
            "start_time", "expire_time", "max_send_num"
    }));

    // Set predefined keys in the rootJson, for extra keys(Android) or customized keys(IOS) please
    // refer to corresponding methods in the subclass.
    public abstract boolean setPredefinedKeyValue(String key, Object value) throws Exception;

    public void setAppMasterSecret(String secret) {
        appMasterSecret = secret;
    }

    public boolean send() throws Exception{
        String url = host + postPath;
        ObjectMapper mapper = new ObjectMapper();
        String postBody = mapper.writeValueAsString(notification);
        System.out.println("Notification " + postBody);

        byte[] bodyBytes = postBody.getBytes("utf8");

        String sign = DigestUtils.md5Hex(("POST" + url + postBody + appMasterSecret).getBytes
                ("utf8"));
        url = url + "?sign=" + sign;

        HttpURLConnection connection = (HttpURLConnection) new java.net.URL(url)
                .openConnection();
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setDoOutput(true);
        connection.getOutputStream().write(bodyBytes);
        connection.getOutputStream().close();

        int status = connection.getResponseCode();
        System.out.println("Response Code : " + status);

        if (status != 200) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(connection
                    .getErrorStream()));

            StringBuffer result = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            throw new Exception("status:" + status + "\n" + result);
        }
        return true;
    }


}
