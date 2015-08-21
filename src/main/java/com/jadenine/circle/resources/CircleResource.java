package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.AccessPoint;
import com.jadenine.circle.entity.Bomb;
import com.jadenine.circle.entity.Circle;
import com.jadenine.circle.entity.Constants;
import com.jadenine.circle.entity.DirectMessage;
import com.jadenine.circle.entity.TimelineEntity;
import com.jadenine.circle.entity.UserCircle;
import com.jadenine.circle.response.CircleResult;
import com.jadenine.circle.response.TimelineRangeResult;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableBatchOperation;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

    @POST
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public CircleResult listCircle(@QueryParam("user") String user) {
        List<Circle> circles = CircleLister.listCircle(user);
        List<String> circleIds = new ArrayList<>(circles.size());
        for(Circle circle : circles) {
            circleIds.add(circle.getCircleId());
        }
        List<AccessPoint> aps = ApLister.list(circleIds);
        return new CircleResult(circles, aps);
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
            Circle circle = new Circle(Constants.DEFAULT_CIRCLE_TAG, ap.getMac(), ap.getSSID());
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

        return Response.ok().entity(CircleLister.listCircle(user)).build();
    }

    @POST
    @Path("/merge")
    @Produces(MediaType.APPLICATION_JSON)
    public Response mergeCircle(@QueryParam("target_circle") String targetCircle, @QueryParam
            ("from_circle") String fromCircle, @QueryParam("name") String name) {

        try {
            TimelineLister<Bomb> bombLister = new TimelineLister<>(Bomb.class, Storage.getInstance()
                    .getBombTable(), 200, 1000);
            updateCircle(bombLister, fromCircle, targetCircle);

            TimelineLister<DirectMessage> chatLister = new TimelineLister<>(DirectMessage.class, Storage
                    .getInstance()
                    .getBombTable(), 200, 1000);
            updateCircle(chatLister, fromCircle, targetCircle);

            List<String> fromCircles = Collections.singletonList(fromCircle);
            updateAccessPoints(fromCircles, targetCircle);

            List<UserCircle> userCircles = CircleLister.listUserCircleByCircle(fromCircles);
            TableBatchOperation userCircleBatchOperation = new TableBatchOperation();
            List<String> usersInOriginCircles = new LinkedList<>();

            for (UserCircle userCircle : userCircles) {
                userCircleBatchOperation.add(TableOperation.delete(userCircle));
                usersInOriginCircles.add(userCircle.getUser());
            }

            if(!usersInOriginCircles.isEmpty()) {
                List<UserCircle> userCircleInTargetCircle = CircleLister.listUserCircleByCircle
                        (Collections.singletonList(targetCircle));
                Set<String> usersInTargetCircle = new LinkedHashSet<>(userCircleInTargetCircle
                        .size());

                for(UserCircle userCircle : userCircleInTargetCircle) {
                    if(usersInTargetCircle.contains(userCircle.getUser())) {
                        continue;
                    }
                    usersInTargetCircle.add(userCircle.getUser());
                }

                for( String user : usersInOriginCircles) {
                    if(!usersInTargetCircle.contains(user)) {
                        UserCircle userCircle = new UserCircle(user, targetCircle);
                        userCircleBatchOperation.add(TableOperation.insert(userCircle));
                        usersInTargetCircle.add(user);
                    }
                }
            }

            if (!userCircleBatchOperation.isEmpty()) {
                Storage.getInstance().getUserCircleTable().execute(userCircleBatchOperation);
            }

            if(null != name && !name.isEmpty()) {
                Circle circle = CircleLister.getCircle(targetCircle);
                if(null != circle && !circle.getName().equals(name)) {
                    circle.setName(name);
                    Storage.getInstance().getCircleTable().execute(TableOperation.replace(circle));
                }
            }

        } catch (StorageException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.ok().build();
    }

    private <T extends TimelineEntity> void updateCircle(TimelineLister<T> bombLister, String
            fromCircle, String targetCircle)
            throws StorageException {
        boolean hasMore = true;
        Long beforeId = null;
        while (hasMore) {
            TableBatchOperation batchOperation = new TableBatchOperation();
            TimelineRangeResult<T> bombs = bombLister.list(fromCircle, 1000, null, beforeId);
            hasMore = bombs.hasMore();
            for (T bomb : bombs.getAll()) {
                bomb.setCircle(targetCircle);
                batchOperation.add(TableOperation.replace(bomb));
            }
            if(!batchOperation.isEmpty()) {
                Storage.getInstance().getBombTable().execute(batchOperation);
            }
        }
    }

    private void updateAccessPoints(List<String> fromCircles, String targetCircle) throws
            StorageException {

        List<AccessPoint> aps = ApLister.list(fromCircles);
        TableBatchOperation apBatchOperation = new TableBatchOperation();
        for(AccessPoint ap : aps) {
            ap.setCircle(targetCircle);
            apBatchOperation.add(TableOperation.replace(ap));
        }
        if(!apBatchOperation.isEmpty()) {
            Storage.getInstance().getApTable().execute(apBatchOperation);
        }
    }
}
