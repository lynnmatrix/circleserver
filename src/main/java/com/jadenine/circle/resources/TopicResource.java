package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.Message;
import com.jadenine.circle.entity.Topic;
import com.jadenine.circle.notification.NotificationService;
import com.jadenine.circle.response.JSONListWrapper;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.LazySegmentedIterator;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableBatchOperation;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableQuery;
import com.microsoft.azure.storage.table.TableRequestOptions;
import com.microsoft.azure.storage.table.TableResult;
import com.microsoft.azure.storage.table.TableServiceException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    public static final String DEFAULT_PAGE_SIZE = "20";
    private static final boolean DEBUG_DELETE = true;

    @GET
    @Path("/delete")
    public Response deleteTopic(@QueryParam("ap") String ap) throws StorageException {
        if(!DEBUG_DELETE) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        String apFilter = TableQuery.generateFilterCondition(Storage.PARTITION_KEY, TableQuery
                .QueryComparisons.EQUAL, ap);

        String filter = apFilter;
        TableQuery<Topic> query = TableQuery.from(Topic.class).where(filter);

        CloudTable topicTable = Storage.getInstance().getTopicTable();

        Iterable<Topic> topicIterable = topicTable.execute(query);
        TableBatchOperation op = new TableBatchOperation();
        for(Topic topic : topicIterable){
            op.delete(topic);
        }
        topicTable.execute(op);

        return Response.ok().build();
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listTopic(@QueryParam("ap") String ap,
                              @QueryParam("count") @DefaultValue(DEFAULT_PAGE_SIZE) Integer count,
                              @QueryParam("since_id") String sinceId,
                              @QueryParam("since_timestamp") Long sinceTimestamp,
                              @QueryParam("before_id") String beforeId) {

        if(count > MAX_COUNT) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Max count is "
                    + MAX_COUNT).build();
        }

        if(null != sinceId && null != sinceTimestamp) {
            return Response.status(Response.Status.BAD_REQUEST).entity("since_id, since_timestamp" +
                    " can not be specified at the same time.").build();
        }

        String apFilter = TableQuery.generateFilterCondition(Storage.PARTITION_KEY, TableQuery
                .QueryComparisons.EQUAL, ap);

        String filter = apFilter;

        if(null != sinceId) {
            String sinceFilter = TableQuery.generateFilterCondition(Storage.ROW_KEY, TableQuery
                    .QueryComparisons
                .LESS_THAN, sinceId);
            filter = TableQuery.combineFilters(filter, TableQuery.Operators.AND, sinceFilter);
        }

        if(null != sinceTimestamp) {
            Date sinceDate = new Date(sinceTimestamp);
            String sinceTimestampFilter = TableQuery.generateFilterCondition(Storage.TIMESTAMP,
                    TableQuery.QueryComparisons.GREATER_THAN_OR_EQUAL, sinceDate);
            filter = TableQuery.combineFilters(filter, TableQuery.Operators.AND, sinceTimestampFilter);
        }

        if(null != beforeId) {
            String beforeFilter = TableQuery.generateFilterCondition(Storage.ROW_KEY,
                    TableQuery.QueryComparisons
                    .GREATER_THAN, beforeId);
            filter = TableQuery.combineFilters(filter, TableQuery.Operators.AND, beforeFilter);
        }


        Integer takeCount = count + 1;// Used to judge whether has more result.
        TableQuery<Topic> query = TableQuery.from(Topic.class).where(filter).take(takeCount);

        CloudTable topicTable = Storage.getInstance().getTopicTable();
        List<Topic> topics = new ArrayList<>();

        boolean hasMore = false;
        String nextTopicId = null;
        Iterable<Topic> topicIterable = topicTable.execute(query);
        for(Topic topic : topicIterable){
            if(topics.size() == count){
                hasMore = true;
                nextTopicId = topic.getTopicId();
                break;
            }
            topics.add(topic);
        }

        return Response.ok().entity(new JSONListWrapper(topics, hasMore, nextTopicId)).build();
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTopic(@Valid Topic topic) throws StorageException {
        return tryAddTopic(topic, 1);
    }

    private Response tryAddTopic(Topic topic, int currentTryCount) throws StorageException {
        String topicId = String.valueOf(AutoDecrementIdGenerator.getNextId());
        topic.setTopicId(topicId);
        topic.setCreatedTimestamp(topic.getTimestamp());

        TableOperation topicUpdateOp = TableOperation.insert(topic);
        try {
            Storage.getInstance().getTopicTable().execute(topicUpdateOp);
        } catch (TableServiceException e) {
            boolean conflict = Response.Status.CONFLICT.getStatusCode() == e.getHttpStatusCode()
                    /*&& e.getErrorCode().contains("EntityAlreadyExists")*/;

            if (conflict && currentTryCount++ < 2) {
                return tryAddTopic(topic, currentTryCount);
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }

        try {
            NotificationService.notifyNewTopic(topic);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.ok().entity(topic).build();
    }
}
