package com.jadenine.circle.notification;

import com.jadenine.circle.entity.Topic;
import com.sun.javafx.binding.StringFormatter;

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

    public static boolean testNotifyDevice() throws Exception{
        Topic fakeTopic = new Topic();
        fakeTopic.setAp(TEST_AP_JADENINE);
        fakeTopic.setTopic("假 topic");
        return notifyNewTopic(fakeTopic);
    }

    public static boolean notifyNewTopic(Topic topic) throws Exception {
        return groupBroadcast(topic.getAp(), topic);
    }

    public static boolean groupBroadcast(String tag, Topic topic) throws Exception {
        AndroidGroupcast groupcast = new AndroidGroupcast();
        groupcast.setAppMasterSecret(appMasterSecret);

        groupcast.setPredefinedKeyValue("appkey", appkey);
        groupcast.setPredefinedKeyValue("timestamp", Integer.toString((int) (System
                .currentTimeMillis() / 1000)));

        groupcast.setPredefinedKeyValue("ticker", topic.getTopic());
        groupcast.setPredefinedKeyValue("title", "New Topic");
        groupcast.setPredefinedKeyValue("text", topic.getTopic());
        groupcast.setPredefinedKeyValue("after_open", "go_app");
        groupcast.setPredefinedKeyValue("display_type", "notification");

        groupcast.setPredefinedKeyValue("production_mode", PRODUCTION_MODE);

        Map<String, Object> filterJson = new HashMap<>();
        Map<String, Object> whereJson = new HashMap<>();
        List<Object> tagArray = new ArrayList<>(1);
        Map<String, Object> testTag = new HashMap<>();
        testTag.put("tag", tag);
        tagArray.add(testTag);

        whereJson.put("and", tagArray);
        filterJson.put("where", whereJson);
        System.out.println(filterJson.toString());

//        String filterFormat = "{\"where\": { \"and\":[\"tag\":\"%s\"]}";
//        String filterJson = StringFormatter.format(filterFormat, tag).getValue();

        groupcast.setPredefinedKeyValue("filter", filterJson);

        return groupcast.send();
    }
}
