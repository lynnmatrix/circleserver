package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.Topic;
import com.jadenine.circle.response.JSONListWrapper;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableQuery;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
}
