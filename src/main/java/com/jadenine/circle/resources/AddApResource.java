package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.UserAp;
import com.jadenine.circle.response.JSONListWrapper;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableQuery;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Created by linym on 6/2/15.
 */
@Path("/addAp")
@Produces(MediaType.APPLICATION_JSON)
public class AddApResource {
    @GET
    public JSONListWrapper addAp(@QueryParam("user") String user, @QueryParam("ap")String ap) {
        UserAp userAP = new UserAp(user, ap);
        CloudTable userApTable = Storage.getInstance().getUserApTable();
        TableOperation insertUserAp = TableOperation.insert(userAP);
        try {
            userApTable.execute(insertUserAp);
        } catch (StorageException e) {
            e.printStackTrace();
        }

        TableQuery<UserAp> query = TableQuery.from(UserAp.class).where(TableQuery
                .generateFilterCondition(Storage.ROW_KEY, TableQuery.QueryComparisons.EQUAL, user));

        List<UserAp> userAps = new ArrayList<>();
        for(UserAp userAp : userApTable.execute(query)){
            userAps.add(userAp);
        }
        return new JSONListWrapper(userAps);
    }

}
