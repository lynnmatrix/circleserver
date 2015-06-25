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
import javax.ws.rs.DefaultValue;
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

    public static final int MAX_COUNT = 200;

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listTopic(@QueryParam("ap") String ap,
                                     @QueryParam("count") @DefaultValue("5") Integer count,
                                     @QueryParam("sinceTimestamp") @DefaultValue("-1") long sinceTimeStamp,
                                     @QueryParam("beforeTimestamp") @DefaultValue("-1") long beforeTimeStamp) {

        if(count > MAX_COUNT) {
            return Response.status(Response.Status.BAD_REQUEST).entity("max count is "
                    + MAX_COUNT).build();
        }

        String apFilter = TableQuery.generateFilterCondition(Storage.PARTITION_KEY, TableQuery
                .QueryComparisons.EQUAL, ap);

        String filter = apFilter;

        if(sinceTimeStamp > 0) {
            String sinceFilter = TableQuery.generateFilterCondition(Storage.TIMESTAMP, TableQuery.QueryComparisons
                .GREATER_THAN, sinceTimeStamp);
            filter = TableQuery.combineFilters(filter, TableQuery.Operators.AND, sinceFilter);
        }
        if(beforeTimeStamp > 0) {
            String beforeFilter = TableQuery.generateFilterCondition(Storage.TIMESTAMP, TableQuery.QueryComparisons
                    .LESS_THAN_OR_EQUAL, sinceTimeStamp);
            filter = TableQuery.combineFilters(filter, TableQuery.Operators.AND, beforeFilter);
        }

        TableQuery<Topic> query = TableQuery.from(Topic.class).where(filter).take(count);

        CloudTable topicTable = Storage.getInstance().getTopicTable();
        List<Topic> topics = new ArrayList<>();

        for(Topic topic : topicTable.execute(query)){
            topics.add(topic);
            if(topics.size() >= count){
                break;
            }
        }

        return Response.ok().entity(new JSONListWrapper(topics)).build();
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
