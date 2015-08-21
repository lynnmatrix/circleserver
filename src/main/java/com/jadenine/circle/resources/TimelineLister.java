package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.TimelineEntity;
import com.jadenine.circle.response.TimelineRangeResult;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableQuery;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linym on 8/21/15.
 */
public class TimelineLister<T extends TimelineEntity> {
    private final Class<T> clazzType;
    private final CloudTable table;
    private final int defaultPageSize;
    private final int maxPageSize;

    public TimelineLister(Class<T> clazzType, CloudTable table, int defaultPageSize, int
            maxPageSize) {
        this.clazzType = clazzType;
        this.table = table;
        this.defaultPageSize = defaultPageSize;
        this.maxPageSize = maxPageSize;
    }

    public TimelineRangeResult<T> list(String circle, Integer count, Long sinceId,
                                    Long beforeId) {
        String circleFilter = TableQuery.generateFilterCondition(Storage.PARTITION_KEY, TableQuery
                .QueryComparisons.EQUAL, circle);

        return listWithCustomFilter(circleFilter, count, sinceId, beforeId);
    }

    public TimelineRangeResult<T> listWithCustomFilter(String customFilter, Integer count, Long
    sinceId, Long beforeId){
        if(null != sinceId && null != beforeId && sinceId < beforeId) {
            throw new InvalidParameterException("since_id must be greater " +
                    "than before_id");
        }
        if(null == count) {
            count = defaultPageSize;
        }

        if(count > maxPageSize) {
            throw new InvalidParameterException("Max count is "
                    + maxPageSize);
        }

        String filter = prepareRangeFilterWithCustomFilter(customFilter, sinceId, beforeId);

        Integer takeCount = count + 1;
        TableQuery<T> query = TableQuery.from(clazzType).where
                (filter).take(takeCount);

        boolean hasMore = false;
        String nextId = null;

        List<T> messages = new ArrayList<>();
        for (T message : table.execute(query)) {
            if(messages.size() == count) {
                hasMore = true;
                nextId = message.getMessageId();
                break;
            }
            messages.add(message);
        }

        return new TimelineRangeResult<>(messages, hasMore, nextId);
    }

    private String prepareRangeFilterWithCustomFilter(String customFilter, Long sinceId, Long
            beforeId) {
        String filter = customFilter;

        if(null != sinceId) {
            String sinceFilter = TableQuery.generateFilterCondition(Storage.ROW_KEY, TableQuery
                    .QueryComparisons
                    .LESS_THAN, String.valueOf(sinceId));
            filter = TableQuery.combineFilters(filter, TableQuery.Operators.AND, sinceFilter);
        }

        if(null != beforeId) {
            String beforeFilter = TableQuery.generateFilterCondition(Storage.ROW_KEY,
                    TableQuery.QueryComparisons
                            .GREATER_THAN, String.valueOf(beforeId));
            filter = TableQuery.combineFilters(filter, TableQuery.Operators.AND, beforeFilter);
        }

        return filter;
    }
}
