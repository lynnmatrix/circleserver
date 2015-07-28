package com.jadenine.circle.notification;

import com.jadenine.circle.entity.Bomb;
import com.jadenine.circle.entity.DirectMessage;
import com.jadenine.circle.entity.Topic;

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

    public static boolean notifyNewTopic(Bomb bomb) throws Exception {
        return groupBroadcast(bomb.getAp(), bomb.getContent(), bomb.getContent());
    }

    public static boolean groupBroadcast(String tag, Topic topic) throws Exception {
        return groupBroadcast(tag, topic.getTopic(), topic.getTopic());
    }

    public static boolean notifyNewChat(DirectMessage message) throws Exception {
        return groupBroadcast(message.getTo(), message.getContent(), message.getContent());
    }

    private static boolean groupBroadcast(String tag, String ticker, String text) throws Exception {
        AndroidGroupCast groupCast = new AndroidGroupCast();
        groupCast.setAppMasterSecret(appMasterSecret);

        groupCast.setPredefinedKeyValue("appkey", appkey);
        groupCast.setPredefinedKeyValue("timestamp", Integer.toString((int) (System
                .currentTimeMillis() / 1000)));

        groupCast.setPredefinedKeyValue("ticker", ticker);
        groupCast.setPredefinedKeyValue("title", "New Topic");
        groupCast.setPredefinedKeyValue("text", text);
        groupCast.setPredefinedKeyValue("after_open", "go_app");
        groupCast.setPredefinedKeyValue("display_type", "notification");

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

        return groupCast.send();
    }
}
