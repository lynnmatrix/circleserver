package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.Bomb;
import com.jadenine.circle.entity.DirectMessage;
import com.jadenine.circle.entity.UserCircle;
import com.jadenine.circle.notification.NotificationService;
import com.jadenine.circle.response.TimelineRangeResult;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableQuery;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by linym on 7/18/15.
 */
@Path("/chat")
public class DirectMessageResource {
    public static final int MAX_COUNT = 200;
    public static final String DEFAULT_PAGE_SIZE = "50";

    /**
     * Returns a collection of the most recent chat messages sended from or to the authenticating
     * user.
     *
     * @param auth user
     * @param count (optional) Specifies the number of chats to try and retrieve, up to a maximum
     *              of 200. The value of count is best thought of as a limit to the number of chat
     *              to return because suspended or deleted content is removed after the count has
     *              been applied.
     * @param sinceId (optional) Returns results with an ID less than (that is, more recent than)
     *                the specified ID.
     * @param beforeId (optional) Returns results with an ID greater than (that is, older than) to the specified ID.
     */
    @POST
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listMessages(@QueryParam("auth") @NotNull String auth,
                                 @QueryParam("count") @DefaultValue(DEFAULT_PAGE_SIZE) Integer count,
                                 @QueryParam("since_id") Long sinceId,
                                 @QueryParam("before_id") Long beforeId) {
        if(null != sinceId && null != beforeId && sinceId < beforeId) {
            return Response.status(Response.Status.BAD_REQUEST).entity("since_id must be greater " +
                    "than before_id").build();
        }
        if(count > MAX_COUNT) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Max count is "
                    + MAX_COUNT).build();
        }

        String filter = prepareListFilter(auth, sinceId, beforeId);
        Integer takeCount = count +1;
        TableQuery<DirectMessage> chatQuery = TableQuery.from(DirectMessage.class).where
                (filter).take(takeCount);

        CloudTable table = Storage.getInstance().getChatTable();
        boolean hasMore = false;
        String nextId = null;

        List<DirectMessage> messages = new ArrayList<>();
        for (DirectMessage message : table.execute(chatQuery)) {
            if(messages.size() == count) {
                hasMore = true;
                nextId = message.getMessageId();
                break;
            }
            messages.add(message);
        }

        return Response.status(Response.Status.OK).entity(new TimelineRangeResult(messages, hasMore,
                nextId)).build();
    }

    private String prepareListFilter(String auth, Long sinceId, Long beforeId) {
        List<UserCircle> circles = CircleLister.listUserCircle(auth);

        String filter = genCircleFilter(circles);

        String authFilter = TableQuery.combineFilters(
                TableQuery.generateFilterCondition(Storage.PARTITION_KEY, TableQuery
                        .QueryComparisons.EQUAL, auth),
                TableQuery.Operators.OR,
                TableQuery.generateFilterCondition(DirectMessage.FIELD_FROM, TableQuery
                        .QueryComparisons.EQUAL, auth));
        filter = TableQuery.combineFilters(filter, TableQuery.Operators.AND, authFilter);

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

    private String genCircleFilter(List<UserCircle> userCircles) {
        String circleFilter = null;
        for(UserCircle userCircle : userCircles) {
            String apFilter = TableQuery.generateFilterCondition(Storage.PARTITION_KEY, TableQuery
                    .QueryComparisons.EQUAL, userCircle.getCircle());
            if (null == circleFilter) {
                circleFilter = apFilter;
            } else {
                circleFilter = TableQuery.combineFilters(circleFilter, TableQuery.Operators.OR, apFilter);
            }
        }
        return circleFilter;
    }

    /**
     * Send a chat message
     * @param message
     * @return the sent message if succeed.
     * @throws StorageException
     */
    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addMessage(@Valid final DirectMessage message) throws StorageException {
        if (null != message.getMessageId() && !message.getMessageId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        boolean newChat = null == message.getRootMessageId() || message.getRootMessageId()
                .isEmpty();
        if(newChat) {
            message.setRootUser(message.getFrom());
        }

        final DirectMessage insertedMessage = Storage.tryInsert(Storage.getInstance().getChatTable(),
                message, 1, newChat? new Storage.IdSetter() {
                    @Override
                    public void beforeTryRowKey(String rowKey) {
                        message.setRootMessageId(rowKey);
                    }
                }:null);
        if(null == insertedMessage) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } else {
            try {
                NotificationService.notifyNewChat(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Response.ok().entity(insertedMessage).build();
        }
    }
}
