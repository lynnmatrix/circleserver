package com.jadenine.circle.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadenine.circle.entity.Bomb;
import com.jadenine.circle.entity.DirectMessage;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linym on 6/13/15.
 */
public class NotificationService {
    private static final String PRODUCTION_MODE = "true";

    private static final String appMasterSecret = "pjssf88meufwow99spolo3fhym8idral";
    private static final String appkey = "557a4f3f67e58e45f2000660";

    private static final String TEST_AP_JADENINE = "c4:04:15:15:94:61";
    public static final String DISPLAY_TYEPE_MESSAGE = "message";
    public static final String DISPLAY_TYPE_NOTIFICATION = "notification";

    private static ObjectMapper objectMapper;
    public static void setObjectMapper(ObjectMapper objectMapper) {
        NotificationService.objectMapper = objectMapper;
    }

    public static boolean notifyNewTopic(Bomb bomb) throws Exception {
        AndroidGroupCast notification = buildCommonGroupCast(DISPLAY_TYPE_NOTIFICATION, bomb
                        .getCircle(),
                bomb.getContent(), bomb.getContent(), bomb.getImages());
        StringWriter writer = new StringWriter();
        HashMap<String, Object> customMap = new HashMap<>(2);
        customMap.put("type", "topic");
        customMap.put("data", bomb);
        objectMapper.writeValue(writer, customMap);

        String custom = writer.toString();
        System.out.println("custom:" + custom);
        notification.setPredefinedKeyValue("custom", custom);
        return notification.send();
    }

    public static boolean notifyNewChat(DirectMessage message) throws Exception {
        AndroidGroupCast groupCast = buildCommonGroupCast(message.getTo(), message.getContent(),
                message.getContent(), null);

        return groupCast.send();
    }

    private static AndroidGroupCast buildCommonGroupCast(String tag, String
            ticker, String text, String img) throws Exception {
        return buildCommonGroupCast(DISPLAY_TYPE_NOTIFICATION, tag, ticker, text, img);
    }

    private static AndroidGroupCast buildCommonGroupCast(String displayType, String tag, String
            ticker, String text, String img) throws Exception {
        AndroidGroupCast groupCast = new AndroidGroupCast();
        groupCast.setAppMasterSecret(appMasterSecret);

        groupCast.setPredefinedKeyValue("appkey", appkey);
        groupCast.setPredefinedKeyValue("timestamp", Integer.toString((int) (System
                .currentTimeMillis() / 1000)));

        groupCast.setPredefinedKeyValue("ticker", ticker);
        groupCast.setPredefinedKeyValue("title", tag);
        groupCast.setPredefinedKeyValue("text", text);
        if(null != img && img.length() > 0) {
            groupCast.setPredefinedKeyValue("img", img);
        }
        groupCast.setPredefinedKeyValue("after_open", "go_app");
        groupCast.setPredefinedKeyValue("display_type", displayType);

        groupCast.setPredefinedKeyValue("production_mode", PRODUCTION_MODE);

        Map<String, Object> filterJson = new HashMap<>();
        Map<String, Object> whereJson = new HashMap<>();
        List<Object> tagArray = new ArrayList<>(1);
        Map<String, Object> testTag = new HashMap<>();
        testTag.put("tag", tag);
        tagArray.add(testTag);

        whereJson.put("and", tagArray);
        filterJson.put("where", whereJson);

        groupCast.setPredefinedKeyValue("filter", filterJson);
        return groupCast;
    }

}
