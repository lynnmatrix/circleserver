package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.TimelineEntity;
import com.jadenine.circle.notification.NotificationService;
import com.jadenine.circle.response.JSONListWrapper;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableQuery;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by linym on 7/21/15.
 */
public class TimelineResource<T extends TimelineEntity> {
    private final Class<T> clazzType;
    private final CloudTable table;
    private final int defaultPageSize;
    private final int maxPageSize;
    
    public TimelineResource(Class<T> clazzType, CloudTable table, int defaultPageSize, int
            maxPageSize) {
        this.clazzType = clazzType;
        this.table = table;
        this.defaultPageSize = defaultPageSize;
        this.maxPageSize = maxPageSize;
    }

    /**
     * Returns a collection of the most recent timeline messages under the specified ap.
     *
     * @param circle
     * @param count (optional) Specifies the number of messages to try and retrieve, up to a maximum
     *              of 200. The value of count is best thought of as a limit to the number of messages
     *              to return because suspended or deleted content is removed after the count has
     *              been applied.
     * @param sinceId (optional) Returns results with an ID less than (that is, more recent than)
     *                the specified ID.
     * @param beforeId (optional) Returns results with an ID greater than (that is, older than) to the specified ID.
     */
    @POST
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listMessages(@QueryParam("circle") @NotNull String circle,
                                 @QueryParam("count") Integer count,
                                 @QueryParam("since_id") Long sinceId,
                                 @QueryParam("before_id") Long beforeId) {
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

        String filter = prepareListFilter(circle, sinceId, beforeId);

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

    private String prepareListFilter(String circle, Long sinceId, Long beforeId) {
        String circleFilter = TableQuery.generateFilterCondition(Storage.PARTITION_KEY, TableQuery
                .QueryComparisons.EQUAL, circle);
        String filter = circleFilter;

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

    /**
     * Send a  message
     * @param message
     * @return the sent message if succeed.
     * @throws StorageException
     */
    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addMessage(@Valid final T message) throws StorageException {
        if (null != message.getMessageId() && !message.getMessageId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        boolean newMessage = null == message.getRootMessageId() || message.getRootMessageId()
                .isEmpty();
        if(newMessage) {
            message.setRootUser(message.getFrom());
        }

        final T insertedMessage = Storage.tryInsert(table,
                message, 1, newMessage? new Storage.IdSetter() {
                    @Override
                    public void beforeTryRowKey(String rowKey) {
                        message.setRootMessageId(rowKey);
                    }
                }:null);
        if(null == insertedMessage) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } else {
            notifyNewMessage(message);
            return Response.ok().entity(insertedMessage).build();
        }
    }

    protected void notifyNewMessage(T message) {

    }

}
