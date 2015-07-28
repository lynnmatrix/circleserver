package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.Bomb;
import com.jadenine.circle.notification.NotificationService;
import com.microsoft.azure.storage.table.TableQuery;

import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by linym on 7/22/15.
 */
@Path("/bomb")
public class BombResource extends TimelineResource<Bomb> {
    private final TimelineLister<Bomb> lister;
    public BombResource(int defaultPageSize, int
            maxPageSize) {
        super(Bomb.class, Storage.getInstance().getBombTable
                (), defaultPageSize, maxPageSize);
        lister = new TimelineLister<>(Bomb.class, Storage.getInstance().getBombTable(),
                defaultPageSize, maxPageSize);
    }

    @POST
    @Path("/list/user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listMyBombs(@QueryParam("auth") @NotNull String auth,
                                @QueryParam("count") Integer count,
                                @QueryParam("since_id") Long sinceId,
                                @QueryParam("before_id") Long beforeId) {
        if (null == auth || auth.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No auth").build();
        }

        String authFilter = TableQuery.generateFilterCondition(Bomb.FIELD_ROOT_USER, TableQuery
                .QueryComparisons.EQUAL, auth);
        return lister.list(authFilter, count, sinceId, beforeId);
    }
    @POST
    @Path("/list/comments/from")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listMyComments(@QueryParam("auth") @NotNull String auth,
                                @QueryParam("count") Integer count,
                                @QueryParam("since_id") Long sinceId,
                                @QueryParam("before_id") Long beforeId) {
        if (null == auth || auth.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No auth").build();
        }

        String authFilter = TableQuery.generateFilterCondition(Bomb.FIELD_FROM, TableQuery
                .QueryComparisons.EQUAL, auth);
        return lister.list(authFilter, count, sinceId, beforeId);
    }

    @POST
    @Path("/list/comments/to")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listCommentsToMe(@QueryParam("auth") @NotNull String auth,
                                @QueryParam("count") Integer count,
                                @QueryParam("since_id") Long sinceId,
                                @QueryParam("before_id") Long beforeId) {

        if (null == auth || auth.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No auth").build();
        }

        String authFilter = TableQuery.generateFilterCondition(Bomb.FIELD_To, TableQuery
                .QueryComparisons.EQUAL, auth);
        return lister.list(authFilter, count, sinceId, beforeId);
    }

    @Override
    protected void notifyNewMessage(Bomb message) {
        try {
            NotificationService.notifyNewTopic(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
