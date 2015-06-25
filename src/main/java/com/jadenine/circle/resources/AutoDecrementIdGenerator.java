package com.jadenine.circle.resources;

import java.util.Calendar;

/**
 * Created by linym on 6/25/15.
 */
public class AutoDecrementIdGenerator {
    public static long getNextId(){
        return Long.MAX_VALUE - Calendar.getInstance().getTimeInMillis();
    }
}
