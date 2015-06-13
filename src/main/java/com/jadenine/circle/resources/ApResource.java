package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.UserAp;
import com.jadenine.circle.notification.NotificationService;
import com.jadenine.circle.response.JSONListWrapper;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableQuery;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by linym on 6/2/15.
 */
@Path("/ap")
public class ApResource {
    @GET
    @Path("/list/t/{user}")
    public String list(@PathParam("user") String user) {
        boolean success = false;
        Exception le = null;
        try {
            success = NotificationService.testNotifyDevice();
        } catch (Exception e) {
            le = e;
            e.printStackTrace();
        }
        String result = "Hello, list for " + user + (success ? " SUCCESS" : "FAIL");
        if (null != le) {
            result += "\n" + formatCallStack(le
                    .getStackTrace());
        }
        return result;

    }

    public static String formatCallStack(StackTraceElement[] stack) {

        StringBuilder builder = new StringBuilder();
        for (StackTraceElement element : stack) {
            builder.append("\tat ");
            builder.append(element.toString());
            builder.append("\n");
        }

        return builder.toString();
    }

    @GET
    @Path("/list/{user}")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONListWrapper listAp(@PathParam("user") String user) {
        TableQuery<UserAp> query = TableQuery.from(UserAp.class).where(TableQuery
                .generateFilterCondition(Storage.ROW_KEY, TableQuery.QueryComparisons.EQUAL, user));

        CloudTable userApTable = Storage.getInstance().getUserApTable();

        List<UserAp> userAps = new ArrayList<>();
        for(UserAp userAp : userApTable.execute(query)){
            userAps.add(userAp);
        }
        return new JSONListWrapper(userAps);
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONListWrapper addAp(@Valid UserAp userAP) {
        CloudTable userApTable = Storage.getInstance().getUserApTable();
        TableOperation insertUserAp = TableOperation.insertOrMerge(userAP);
        try {
            userApTable.execute(insertUserAp);
        } catch (StorageException e) {
            e.printStackTrace();
        }

        TableQuery<UserAp> query = TableQuery.from(UserAp.class).where(TableQuery
                .generateFilterCondition(Storage.ROW_KEY, TableQuery.QueryComparisons.EQUAL,
                        userAP.getUser()));

        List<UserAp> userAps = new ArrayList<>();
        for(UserAp userAp : userApTable.execute(query)){
            userAps.add(userAp);
        }
        return new JSONListWrapper(userAps);
    }

}
