package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.Message;
import com.jadenine.circle.entity.Topic;
import com.jadenine.circle.notification.NotificationService;
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
 * Created by linym on 6/9/15.
 */
@Path("/topic")
public class TopicResource {
    @GET
    @Path("/list/{ap}")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONListWrapper listTopic(@PathParam("ap") String ap) {
        TableQuery<Topic> query = TableQuery.from(Topic.class).where(TableQuery
                .generateFilterCondition(Storage.PARTITION_KEY, TableQuery.QueryComparisons
                        .EQUAL, ap));

        CloudTable topicTable = Storage.getInstance().getTopicTable();

        List<Topic> topics = new ArrayList<>();
        for(Topic topic : topicTable.execute(query)){
            topics.add(topic);
        }

        return new JSONListWrapper(topics);
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTopic(@Valid Topic topic) throws StorageException {
        String topicId = UUID.randomUUID().toString();
        topic.setTopicId(topicId);

        TableOperation topicUpdateOp = TableOperation.insert(topic);
        Storage.getInstance().getTopicTable().execute(topicUpdateOp);
        try {
            NotificationService.notifyNewTopic(topic);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.ok().entity(topic).build();
    }
}
