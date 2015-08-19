package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.AccessPoint;
import com.jadenine.circle.entity.Circle;
import com.jadenine.circle.entity.User;
import com.jadenine.circle.entity.UserCircle;
import com.jadenine.circle.response.JSONListWrapper;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableQuery;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by linym on 8/19/15.
 */
@Path("/circle")
public class CircleResource {

    public static final String DEFAULT_CIRCLE_TAG = "DEFAULT";

    @POST
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONListWrapper listCircle(@QueryParam("user") String user) {
        return new JSONListWrapper(CircleLister.list(user));
    }

    @POST
    @Path("/ap/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addAp(@QueryParam("user") String user, AccessPoint ap) throws StorageException {
        if(null == user || user.length() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid user").build();
        }

        CloudTable apTable = Storage.getInstance().getApTable();
        TableQuery<AccessPoint> apQuery = TableQuery.from(AccessPoint.class).where(TableQuery
                .generateFilterCondition(Storage.ROW_KEY, TableQuery.QueryComparisons.EQUAL, ap
                        .getMac()));
        AccessPoint serverAp = null;
        for(AccessPoint tmpAp: apTable.execute(apQuery)) {
            if(null == serverAp) {
                serverAp = tmpAp;
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Multiple " +
                        "access point found with mac" + ap.getMac()).build();
            }
        }

        CloudTable userCircleTable = Storage.getInstance().getUserCircleTable();

        if(null == serverAp) {
            Circle circle = new Circle(DEFAULT_CIRCLE_TAG, ap.getMac(), ap.getSSID());
            Storage.getInstance().getCircleTable().execute(TableOperation.insert(circle));
            ap.setCircle(circle.getCircleId());
            Storage.getInstance().getApTable().execute(TableOperation.insert(ap));
            UserCircle userCircle = new UserCircle(user, circle.getCircleId());
            userCircleTable.execute(TableOperation.insert(userCircle));
        } else {
            if(!serverAp.getSSID().equals(ap.getSSID())) {
                serverAp.setSSID(ap.getSSID());
                Storage.getInstance().getApTable().execute(TableOperation.replace(serverAp));
            }

            TableQuery<UserCircle> userCircleQuery = TableQuery.from(UserCircle.class).where(TableQuery
                    .generateFilterCondition(Storage.ROW_KEY, TableQuery.QueryComparisons.EQUAL,
                            serverAp.getCircle()));

            boolean alreadyBindCircle = !userCircleTable.execute(userCircleQuery).iterator().hasNext();

            if (!alreadyBindCircle){
                UserCircle userCircle = new UserCircle(user, serverAp.getCircle());
                userCircleTable.execute(TableOperation.insert(userCircle));
            }
        }

        return Response.ok().entity(CircleLister.list(user)).build();
    }
}
