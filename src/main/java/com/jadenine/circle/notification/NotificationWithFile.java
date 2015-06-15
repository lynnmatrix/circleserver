package com.jadenine.circle.notification;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by linym on 6/13/15.
 */
public abstract class NotificationWithFile extends AndroidNotification {

    private static String join(String[] contentArray) {
        return String.join("\n", contentArray);
    }

    // Upload file with device_tokens or alias to Umeng
    protected String uploadContents(String[] contents) throws Exception {
        if (!notification.containsKey("appkey") || !notification.containsKey("timestamp") || !notification
                .containsKey("validation_token")) {
            throw new Exception("appkey, timestamp and validation_token needs to be set.");
        }

        // Construct the json string
        HashMap<String, Object> uploadJson = new HashMap<>();
        uploadJson.put("appkey", notification.get("appkey"));
        uploadJson.put("timestamp", notification.get("timestamp"));
        uploadJson.put("validation_token", notification.get("validation_token"));
        uploadJson.put("content", join(contents));

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(uploadJson);
        byte[] jsonBytes = jsonString.getBytes();

        String url = host + uploadPath;
        HttpURLConnection connection = (HttpURLConnection) new java.net.URL(url).openConnection();
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode(jsonBytes.length);
        connection.getOutputStream().write(jsonBytes);
        connection.getOutputStream().close();

        Map<String, String> resultMap = mapper.readValue(connection.getInputStream(), HashMap
                .class);

        // Decode response string and get file_id from it
        if(!resultMap.containsKey("ret") || !"SUCCESS".equalsIgnoreCase(resultMap.get("ret"))) {
            throw new Exception("Failed to upload file");
        }

        String dataString = resultMap.get("data");
        Map<String, String> dataMap = mapper.readValue(dataString, HashMap.class);

        String fileId = dataMap.get("file_id");
        // Set file_id into notification using setPredefinedKeyValue
        setPredefinedKeyValue("file_id", fileId);
        return fileId;
    }
}
