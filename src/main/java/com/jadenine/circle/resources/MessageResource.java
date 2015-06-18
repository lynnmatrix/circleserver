package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.Message;
import com.jadenine.circle.entity.Topic;
import com.jadenine.circle.response.JSONListWrapper;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
    @GET
    @Path("/list/{topic}")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONListWrapper listMessages(@PathParam("topic") String topic) {
        TableQuery<Message> query = TableQuery.from(Message.class).where(TableQuery
                .generateFilterCondition(Storage.PARTITION_KEY, TableQuery.QueryComparisons
                        .EQUAL, topic));

        CloudTable messageTable = Storage.getInstance().getMessageTable();

        List<Message> messages = new ArrayList<>();
        for(Message message : messageTable.execute(query)){
            messages.add(message);
        }
        return new JSONListWrapper(messages);
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addMessage(@QueryParam("ap") String ap, @Valid Message message) throws
            StorageException {

        if(null != message.getMessageId() && !message.getMessageId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        String messageId = UUID.randomUUID().toString();
        message.setMessageId(messageId);

        Topic topic = queryTopic(ap, message.getTopicId());
        if(null == topic) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        topic.setLatestMessageId(message.getMessageId());

        TableOperation addOp = TableOperation.insert(message);
        Storage.getInstance().getMessageTable().execute(addOp);

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
