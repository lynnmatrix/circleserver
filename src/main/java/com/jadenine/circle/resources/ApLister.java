package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.AccessPoint;
import com.jadenine.circle.entity.Circle;
import com.jadenine.circle.entity.UserAp;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by linym on 8/11/15.
 */
public class ApLister {
    public static List<UserAp>  list(String user) {
        TableQuery<UserAp> query = TableQuery.from(UserAp.class).where(TableQuery
                .generateFilterCondition(Storage.ROW_KEY, TableQuery.QueryComparisons.EQUAL, user));

        CloudTable userApTable = Storage.getInstance().getUserApTable();

        List<UserAp> userAps = new ArrayList<>();
        for(UserAp userAp : userApTable.execute(query)){
            userAps.add(userAp);
        }
        return userAps;
    }

    public static List<AccessPoint> list(List<Circle> circles) {
        if(circles.isEmpty()) {
            return Collections.emptyList();
        }

        String filter = null;
        for(Circle circle : circles) {
            String circleFilter = TableQuery.generateFilterCondition(Storage.PARTITION_KEY,
                    TableQuery.QueryComparisons.EQUAL, circle.getCircleId());
            if(null == filter) {
                filter = circleFilter;
            } else {
                filter = TableQuery.combineFilters(filter, TableQuery.Operators.OR, circleFilter);
            }
        }
        TableQuery<AccessPoint> query = TableQuery.from(AccessPoint.class).where(filter);

        CloudTable apTable = Storage.getInstance().getApTable();

        List<AccessPoint> aps = new ArrayList<>();
        for(AccessPoint ap : apTable.execute(query)){
            aps.add(ap);
        }
        return aps;
    }
}
