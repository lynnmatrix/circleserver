package com.jadenine.circle.notification;

/**
 * Created by linym on 6/13/15.
 */
public class NotificationService {
    private static final String PRODUCTION_MODE = "false";

    private static final String appMasterSecret = "pjssf88meufwow99spolo3fhym8idral";
    private static final String appkey = "557a4f3f67e58e45f2000660";

    public static boolean testNotifyDevice(){
        AndroidUnicast unicast = new AndroidUnicast();

        unicast.setAppMasterSecret(appMasterSecret);

        // And if you have many alias, you can also upload a file containing these alias, then
        // use file_id to send customized notification.
        try {
            unicast.setPredefinedKeyValue("appkey", appkey);
            unicast.setPredefinedKeyValue("timestamp", Integer.toString((int) (System
                    .currentTimeMillis() / 1000)));

            unicast.setPredefinedKeyValue("ticker", "Android customizedcast ticker");
            unicast.setPredefinedKeyValue("title", "中文的title");
            unicast.setPredefinedKeyValue("text", "Android customizedcast text");
            unicast.setPredefinedKeyValue("after_open", "go_app");
            unicast.setPredefinedKeyValue("display_type", "notification");

            unicast.setPredefinedKeyValue("production_mode", PRODUCTION_MODE);

            unicast.setPredefinedKeyValue("device_tokens",
                    "AoLk9ARkNoGWPKHdg9Z2CuzNV7qzoTR9x9vZRbhnSW1x");

            return unicast.send();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
