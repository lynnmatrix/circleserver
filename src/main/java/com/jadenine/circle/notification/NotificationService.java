package com.jadenine.circle.notification;

import com.jadenine.circle.entity.Topic;

/**
 * Created by linym on 6/13/15.
 */
public class NotificationService {
    private static final String PRODUCTION_MODE = "true";

    private static final String appMasterSecret = "pjssf88meufwow99spolo3fhym8idral";
    private static final String appkey = "557a4f3f67e58e45f2000660";
    public static final String ALIAS_TYPE_AP = "AP";

    private static final String TEST_AP_JADENINE = "c4:04:15:15:94:61";

    public static boolean testNotifyDevice() throws Exception{
        Topic fakeTopic = new Topic();
        fakeTopic.setAp(TEST_AP_JADENINE);
        fakeTopic.setTopic("假 topic");
        return notifyNewTopic(fakeTopic);
    }

    public static boolean notifyNewTopic(Topic topic) throws Exception {
        return notifyAlias(ALIAS_TYPE_AP, topic.getAp(), topic);
    }

    public static boolean notifyAlias(String aliasType, String alias, Topic topic) throws Exception {
        AndroidCustomizedcast customizedcast = new AndroidCustomizedcast();
        customizedcast.setAppMasterSecret(appMasterSecret);

        customizedcast.setPredefinedKeyValue("appkey", appkey);
        customizedcast.setPredefinedKeyValue("timestamp", Integer.toString((int) (System
                .currentTimeMillis() / 1000)));

        customizedcast.setPredefinedKeyValue("ticker", topic.getTopic());
        customizedcast.setPredefinedKeyValue("title", "New Topic");
        customizedcast.setPredefinedKeyValue("text", topic.getTopic());
        customizedcast.setPredefinedKeyValue("after_open", "go_app");
        customizedcast.setPredefinedKeyValue("display_type", "notification");

        customizedcast.setPredefinedKeyValue("production_mode", PRODUCTION_MODE);

        customizedcast.setPredefinedKeyValue("alias", alias);
        customizedcast.setPredefinedKeyValue("alias_type", aliasType);

        return customizedcast.send();
    }
}
