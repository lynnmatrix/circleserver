package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.TimelineEntity;
import com.jadenine.circle.response.JSONListWrapper;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableQuery;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

/**
 * Created by linym on 7/24/15.
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

    public Response list(String customFilter, Integer count, Long sinceId, Long beforeId){
        if(null != sinceId && null != beforeId && sinceId < beforeId) {
            return Response.status(Response.Status.BAD_REQUEST).entity("since_id must be greater " +
                    "than before_id").build();
        }
        if(null == count) {
            count = defaultPageSize;
        }

        if(count > maxPageSize) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Max count is "
                    + maxPageSize).build();
        }

        String filter = prepareRangeFilter(customFilter, sinceId, beforeId);

        Integer takeCount = count +1;
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

        return Response.status(Response.Status.OK).entity(new JSONListWrapper(messages, hasMore,
                nextId)).build();
    }

    private String prepareRangeFilter(String customFilter, Long sinceId, Long beforeId) {
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
