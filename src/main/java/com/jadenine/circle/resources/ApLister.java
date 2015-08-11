package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.UserAp;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableQuery;

import java.util.ArrayList;
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
}
