package com.jadenine.circle.notification;

/**
 * Created by linym on 8/28/15.
 */
public class AndroidGroupCast extends AndroidNotification {
    public AndroidGroupCast() {
        try {
            this.setPredefinedKeyValue("type", "groupcast");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}

