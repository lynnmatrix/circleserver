package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.Message;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by linym on 6/3/15.
 */
@Path("/message")
public class MessageResource {
    @GET
    @Path("/list/{ap}")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONListWrapper listMessages(@PathParam("ap") String ap) {
        TableQuery<Message> query = TableQuery.from(Message.class).where(TableQuery
                .generateFilterCondition(Storage.PARTITION_KEY, TableQuery.QueryComparisons
                        .EQUAL, ap));

        CloudTable messageTable = Storage.getInstance().getMessageTable();

        List<Message> messages = new ArrayList<>();
        for(Message userAp : messageTable.execute(query)){
            messages.add(userAp);
        }
        return new JSONListWrapper(messages);
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addMessage(@Valid Message message) throws StorageException {
        if(null == message.getMessageId() || message.getMessageId().isEmpty()) {
            message.setMessageId(UUID.randomUUID().toString());
        }
        TableOperation addOp = TableOperation.insert(message);
        Storage.getInstance().getMessageTable().execute(addOp);
        return Response.ok().build();
    }
}
