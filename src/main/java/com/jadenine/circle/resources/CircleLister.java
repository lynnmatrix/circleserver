package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.Circle;
import com.jadenine.circle.entity.Constants;
import com.jadenine.circle.entity.UserCircle;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableQuery;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Created by linym on 8/19/15.
 */
public class CircleLister {

    public static List<UserCircle> listUserCircle(@NotNull String user) {
        if(null == user || user.length() <= 0) {
            return Collections.emptyList();
        }

        TableQuery<UserCircle> query = TableQuery.from(UserCircle.class).where(TableQuery
                .generateFilterCondition(Storage.PARTITION_KEY, TableQuery.QueryComparisons.EQUAL, user));

        CloudTable userCircleTable = Storage.getInstance().getUserCircleTable();

        List<UserCircle> userCircles = new LinkedList<>();
        for(UserCircle userCircle : userCircleTable.execute(query)) {
            userCircles.add(userCircle);
        }
        return userCircles;
    }

    public static List<UserCircle> listUserCircleByCircle(List<String> circles) {
        if (null == circles || circles.size() <= 0) {
            return Collections.emptyList();
        }

        String userCircleFilter = null;
        for (String circle : circles) {

            String localFilter = TableQuery
                    .generateFilterCondition(Storage.ROW_KEY, TableQuery.QueryComparisons.EQUAL,
                            circle);
            if (null == userCircleFilter) {
                userCircleFilter = localFilter;
            } else {
                userCircleFilter = TableQuery.combineFilters(userCircleFilter, TableQuery
                                .Operators.OR,
                        localFilter);
            }
        }

        if (null == userCircleFilter) {
            return Collections.emptyList();
        }

        TableQuery<UserCircle> query = TableQuery.from(UserCircle.class).where(userCircleFilter);

        CloudTable userCircleTable = Storage.getInstance().getUserCircleTable();

        List<UserCircle> userCircles = new LinkedList<>();
        for (UserCircle userCircle : userCircleTable.execute(query)) {
            userCircles.add(userCircle);
        }
        return userCircles;
    }

    public static List<Circle> listCircle(@NotNull String user) {
        if(null == user || user.length() <= 0) {
            return Collections.emptyList();
        }

        String circleFilter= null;
        for(UserCircle userCircle : listUserCircle(user)){
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

    public static @Nullable Circle getCircle(String circleId) {
        String filter = TableQuery.generateFilterCondition
                (Storage.ROW_KEY, TableQuery.QueryComparisons.EQUAL, circleId);
        filter = TableQuery.combineFilters(filter, TableQuery.Operators.AND, TableQuery
                .generateFilterCondition
                (Storage.PARTITION_KEY, TableQuery.QueryComparisons.EQUAL, Constants.DEFAULT_CIRCLE_TAG));
        TableQuery query = TableQuery.from(Circle.class).where(filter);
        Iterator<Circle> iterator = Storage.getInstance().getCircleTable().execute(query).iterator();
        Circle circle=null;
        if(iterator.hasNext()) {
            circle = iterator.next();
        }
        return circle;
    }
}
