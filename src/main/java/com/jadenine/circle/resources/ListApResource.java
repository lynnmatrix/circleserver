package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.UserAp;
import com.jadenine.circle.response.UserApList;
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
 * Created by linym on 6/2/15.
 */
@Path("/listAp")
public class ListApResource {
    @Path("/t/{user}")
    @GET
    public String list(@PathParam("user") String user) {
        return "Hello, list for " + user;
    }

    @Path("/{user}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public UserApList listAp(@PathParam("user") String user) {
//        TableQuery<UserAp> query = TableQuery.from(UserAp.class).where(TableQuery
//                .generateFilterCondition(Storage.ROW_KEY, TableQuery.QueryComparisons.EQUAL, user));
//
//        CloudTable userApTable = Storage.getInstance().getUserApTable();

        List<UserAp> userAps = new ArrayList<>();
        userAps.add(new UserAp("user1", "ap1"));
//        for(UserAp userAp : userApTable.execute(query)){
//            userAps.add(userAp);
//        }
        return new UserApList(userAps);
    }
}
