package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.Message;
import com.jadenine.circle.entity.Topic;
import com.jadenine.circle.response.JSONListWrapper;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableQuery;
import com.microsoft.azure.storage.table.TableServiceException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by linym on 6/3/15.
 */
@Path("/message")
public class MessageResource {
    @POST
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONListWrapper listMessages(@QueryParam("auth") @NotNull String auth,
                                        @QueryParam("ap") @NotNull String ap,
                                        @QueryParam("topic") @NotNull String topic) {

        String topicFilter = TableQuery
                .generateFilterCondition(Storage.PARTITION_KEY, TableQuery.QueryComparisons
                        .EQUAL, topic);

        TableQuery<Message> query = TableQuery.from(Message.class).where(topicFilter);

        CloudTable messageTable = Storage.getInstance().getMessageTable();

        List<Message> messages = new ArrayList<>();
        for (Message message : messageTable.execute(query)) {
            if ((null == message.getAp() || message.getAp().equals(ap))
                    && (!message.getPrivary() || message.getReplyToUser().equals(auth) || message
                    .getUser().equals(auth))) {
                messages.add(message);
            }
        }

        return new JSONListWrapper(messages);
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addMessage(@Valid Message message) throws
            StorageException {
        if(null != message.getMessageId() && !message.getMessageId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if(null == message.getAp() || message.getAp().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Topic topic = queryTopic(message.getAp(), message.getTopicId());
        if(null == topic) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        return tryAddMessage(topic, message, 1);
    }

    private Response tryAddMessage(Topic topic, Message message, int currentTryCount)
            throws StorageException {

        String messageId = String.valueOf(AutoDecrementIdGenerator.getNextId());
        message.setMessageId(messageId);

        TableOperation addOp = TableOperation.insert(message);
        try {
            Storage.getInstance().getMessageTable().execute(addOp);
        } catch (TableServiceException e) {
            boolean conflict = Response.Status.CONFLICT.getStatusCode() == e.getHttpStatusCode()
                    /*&& e.getErrorCode().contains("EntityAlreadyExists")*/;

            if (conflict && currentTryCount++ < 2) {
                return tryAddMessage(topic, message, currentTryCount);
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }

        topic.setLatestMessageId(message.getMessageId());
        //There is concurrence issue, but doesn't matter, message count is not that important.
        // We may correct it after all messages of the topic retrieved in listMessages.
        topic.setMessageCount(topic.getMessageCount() + 1);

        TableOperation topicUpdateOp = TableOperation.replace(topic);
        Storage.getInstance().getTopicTable().execute(topicUpdateOp);

        return Response.ok().entity(message).build();
    }

    private Topic queryTopic(String ap, String topicId) {
        TableQuery<Topic> topicQuery = TableQuery.from(Topic.class).where(
                TableQuery.combineFilters(
                        TableQuery.generateFilterCondition(Storage.PARTITION_KEY,
                                TableQuery.QueryComparisons.EQUAL,
                                ap),
                        TableQuery.Operators.AND,
                        TableQuery.generateFilterCondition(Storage.ROW_KEY,
                                TableQuery.QueryComparisons.EQUAL,
                                topicId)));

        Iterable<Topic> topicIterable = Storage.getInstance().getTopicTable().execute(topicQuery);
        if (topicIterable.iterator().hasNext()) {
            return topicIterable.iterator().next();
        }
        return null;
    }
}
