package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.Message;
import com.jadenine.circle.entity.Topic;
import com.jadenine.circle.notification.NotificationService;
import com.jadenine.circle.response.JSONListWrapper;
import com.jadenine.circle.response.TimelineResult;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableBatchOperation;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableQuery;
import com.microsoft.azure.storage.table.TableServiceException;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.tools.ToolProvider;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
    private static final boolean DEBUG_DELETE = false;

    //test
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

        //TODO delete related messages

        return Response.ok().build();
    }

    @POST
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listTopic(@QueryParam("ap") String ap,
                              @QueryParam("count") @DefaultValue(DEFAULT_PAGE_SIZE) Integer count,
                              @QueryParam("since_topic_id") String sinceTopicId,
                              @QueryParam("since_timestamp") Long sinceTimestamp,
                              @QueryParam("before_topic_id") String beforeTopicId,
                              @QueryParam("before_timestamp") Long beforeTimestamp) {

        if(count > MAX_COUNT) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Max count is "
                    + MAX_COUNT).build();
        }

        if(null != sinceTopicId && null != beforeTopicId) {
            return Response.status(Response.Status.BAD_REQUEST).entity("since_topic_id, " +
                    "before_topic_id" +
                    " can not be specified at the same time.").build();
        }

        if((null == beforeTopicId) != (null == beforeTimestamp) ) {
            return Response.status(Response.Status.BAD_REQUEST).entity("before_timestamp and " +
                    "before_topic_id should be specified at the same time.").build();
        }

        if((null == sinceTopicId) != (null == sinceTimestamp) ) {
            return Response.status(Response.Status.BAD_REQUEST).entity("since_timestamp and " +
                    "since_topic_id should be specified at the same time.").build();
        }

        String filter = prepareFilter(ap, sinceTopicId, sinceTimestamp, beforeTopicId, beforeTimestamp);

        Integer takeCount = count + 1;// Used to judge whether has more result.
        TableQuery<Topic> query = TableQuery.from(Topic.class).where(filter).take(takeCount);

        CloudTable topicTable = Storage.getInstance().getTopicTable();

        boolean hasMore = false;
        String nextTopicId = null;
        Iterable<Topic> topicIterable = topicTable.execute(query);

        List<Topic> topics = new ArrayList<>();
        for(Topic topic : topicIterable){
            if(topics.size() == count){
                hasMore = true;
                nextTopicId = topic.getTopicId();
                break;
            }
            topics.add(topic);
        }

        CloudTable messageTable = Storage.getInstance().getMessageTable();

        TimelineResult<Topic> result = new TimelineResult<>(topics, new ArrayList(), hasMore, nextTopicId);

        Iterator<Topic> topicIterator = topics.iterator();
        while (topicIterator.hasNext()){
            Topic topic = topicIterator.next();
            String messageFilter = prepareMessageFilter(ap, topic.getTopicId(), sinceTimestamp,
                    beforeTimestamp);
            TableQuery<Message> msgQuery = TableQuery.from(Message.class).where(messageFilter).take
                    (takeCount);
            LinkedList<Message> messages = new LinkedList<>();
            for(Message message : messageTable.execute(msgQuery)) {
                messages.add(message);
            }
            topic.setMessages(messages);
            if(null != sinceTimestamp && (topic.getCreatedTimestamp().getTime() < sinceTimestamp
                    || topic.getTopicId() == sinceTopicId)) {
                topicIterator.remove();
                result.getUpdate().add(topic);
            }
        }

        return Response.ok().entity(result).build();
    }

    private String prepareFilter(String ap,
                                 String sinceId,
                                 Long sinceTimestamp,
                                 String beforeId,
                                 Long beforeTimestamp) {
        String apFilter = TableQuery.generateFilterCondition(Storage.PARTITION_KEY, TableQuery
                .QueryComparisons.EQUAL, ap);

        String filter = apFilter;

        if(null != sinceId) {
            String sinceFilter = TableQuery.generateFilterCondition(Storage.ROW_KEY, TableQuery
                    .QueryComparisons
                .LESS_THAN, sinceId);
            filter = TableQuery.combineFilters(filter, TableQuery.Operators.AND, sinceFilter);
        }


        if(null != beforeId) {
            String beforeFilter = TableQuery.generateFilterCondition(Storage.ROW_KEY,
                    TableQuery.QueryComparisons
                    .GREATER_THAN, beforeId);
            filter = TableQuery.combineFilters(filter, TableQuery.Operators.AND, beforeFilter);
        }

        filter = combineTimestampFilter(sinceTimestamp, beforeTimestamp, filter);

        return filter;
    }

    private String prepareMessageFilter(String ap, String topicId, Long sinceTimestamp, Long
            beforeTimestamp) {
        String apFilter = TableQuery.generateFilterCondition(Storage.PARTITION_KEY, TableQuery
                .QueryComparisons.EQUAL, ap);

        String filter = apFilter;

        String sinceFilter = TableQuery.generateFilterCondition(Storage.ROW_KEY, TableQuery
                .QueryComparisons
                .EQUAL, topicId);
        filter = TableQuery.combineFilters(filter, TableQuery.Operators.AND, sinceFilter);

        filter = combineTimestampFilter(sinceTimestamp, beforeTimestamp, filter);

        return filter;
    }

    private String combineTimestampFilter(Long sinceTimestamp, Long beforeTimestamp, String filter) {
        if(null != sinceTimestamp) {
            Date sinceDate = new Date(sinceTimestamp);
            String sinceTimestampFilter = TableQuery.generateFilterCondition(Storage.TIMESTAMP,
                    TableQuery.QueryComparisons.GREATER_THAN_OR_EQUAL, sinceDate);
            filter = TableQuery.combineFilters(filter, TableQuery.Operators.AND, sinceTimestampFilter);
        }

        if(null != beforeTimestamp) {
            Date sinceDate = new Date(beforeTimestamp);
            String beforeTimestampFilter = TableQuery.generateFilterCondition(Storage.TIMESTAMP,
                    TableQuery.QueryComparisons.LESS_THAN_OR_EQUAL, sinceDate);
            filter = TableQuery.combineFilters(filter, TableQuery.Operators.AND, beforeTimestampFilter);
        }
        return filter;
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
