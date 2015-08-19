package com.jadenine.circle.resources;

import com.google.common.base.Preconditions;
import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.Circle;
import com.jadenine.circle.entity.UserCircle;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableQuery;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.validation.constraints.NotNull;

/**
 * Created by linym on 8/19/15.
 */
public class CircleLister {

    public static List<Circle> list(@NotNull String user) {
        if(null == user || user.length() <= 0) {
            return Collections.emptyList();
        }

        TableQuery<UserCircle> query = TableQuery.from(UserCircle.class).where(TableQuery
                .generateFilterCondition(Storage.PARTITION_KEY, TableQuery.QueryComparisons.EQUAL, user));

        CloudTable userCircleTable = Storage.getInstance().getUserCircleTable();

        String circleFilter= null;

        for(UserCircle userCircle : userCircleTable.execute(query)){
            String localFilter = TableQuery.generateFilterCondition(Storage.ROW_KEY, TableQuery
                    .QueryComparisons.EQUAL, userCircle.getCircle());
            if(null == circleFilter) {
                circleFilter = localFilter;
            } else {
                circleFilter = TableQuery.combineFilters(circleFilter, TableQuery.Operators.OR,
                        localFilter);
            }
        }
        if(null == circleFilter) {
            return Collections.emptyList();
        }

        CloudTable circleTable = Storage.getInstance().getCircleTable();
        TableQuery<Circle> circleQuery = TableQuery.from(Circle.class).where(circleFilter);
        List<Circle> circles = new LinkedList<>();
        for(Circle circle : circleTable.execute(circleQuery)) {
            circles.add(circle);
        }
        return circles;
    }
}
