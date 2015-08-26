package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.Bomb;
import com.jadenine.circle.entity.UserCircle;
import com.jadenine.circle.notification.NotificationService;
import com.jadenine.circle.response.TimelineRangeResult;
import com.microsoft.azure.storage.table.TableQuery;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

        List<UserCircle> circles = CircleLister.listUserCircle(auth);

        String filter = genCircleFilter(circles);

        String authFilter = TableQuery.generateFilterCondition(Bomb.FIELD_ROOT_USER, TableQuery
                .QueryComparisons.EQUAL, auth);
        filter = TableQuery.combineFilters(filter, TableQuery.Operators.AND, authFilter);

        try {
            return Response.ok().entity(lister.listWithCustomFilter(filter, count, sinceId,
                    beforeId)).build();
        }catch (InvalidParameterException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
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

        List<UserCircle> circles = CircleLister.listUserCircle(auth);

        String filter = genCircleFilter(circles);

        String authFilter = TableQuery.generateFilterCondition(Bomb.FIELD_FROM, TableQuery
                .QueryComparisons.EQUAL, auth);
        filter = TableQuery.combineFilters(filter, TableQuery.Operators.AND, authFilter);

        try {
            return Response.ok().entity(lister.listWithCustomFilter(filter, count, sinceId,
                    beforeId)).build();
        } catch (InvalidParameterException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
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

        List<UserCircle> circles = CircleLister.listUserCircle(auth);

        String filter = genCircleFilter(circles);

        String authFilter = TableQuery.generateFilterCondition(Bomb.FIELD_To, TableQuery
                .QueryComparisons.EQUAL, auth);

        filter = TableQuery.combineFilters(filter, TableQuery.Operators.AND, authFilter);

        try {
            return Response.ok().entity(lister.listWithCustomFilter(filter, count, sinceId,
                    beforeId)).build();
        } catch (InvalidParameterException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/list/top")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listTop(@QueryParam("auth") @NotNull String auth) {
        if (null == auth || auth.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No auth").build();
        }

        List<UserCircle> circles = CircleLister.listUserCircle(auth);
        if(circles.isEmpty()) {
            return Response.ok().entity(new TimelineRangeResult<>(Collections.<Bomb>emptyList()))
                    .build();
        }

        String filter = prepareFilter(circles);

        TableQuery<Bomb> query = TableQuery.from(Bomb.class).where
                (filter);

        Iterable<Bomb> bombIterable = Storage.getInstance().getBombTable().execute(query);
        HashMap<String, LinkedList<Bomb>> groupedBombs = groupBomb(bombIterable);
        if(groupedBombs.size() <= 0) {
            return Response.ok().entity(new TimelineRangeResult(Collections.emptyList())).build();
        }
        MinHeap minHeap = buildMinHeap(groupedBombs);

        String[] topIds = minHeap.top();
        List<Bomb> resultBomb = new ArrayList<>();

        for(String topId : topIds) {
            //TODO if no root found for topic, find the root and the remaining bombs
            resultBomb.addAll(groupedBombs.get(topId));
        }
        return Response.ok().entity(new TimelineRangeResult(resultBomb, false, null)).build();
    }

    private String prepareFilter(List<UserCircle> circles) {
        String timeFilter = TableQuery.generateFilterCondition(Storage.TIMESTAMP, TableQuery
                .QueryComparisons.GREATER_THAN, new Date(System.currentTimeMillis() - 2*TimeUnit
                .DAYS
                .toMillis(1)));
        String filter = timeFilter;

        String circleFilter = genCircleFilter(circles);
        filter = TableQuery.combineFilters(filter, TableQuery.Operators.AND, circleFilter);
        return filter;
    }

    private String genCircleFilter(List<UserCircle> userCircles) {
        String circleFilter = null;
        for(UserCircle userCircle : userCircles) {
            String apFilter = TableQuery.generateFilterCondition(Storage.PARTITION_KEY, TableQuery
                    .QueryComparisons.EQUAL, userCircle.getCircle());
            if (null == circleFilter) {
                circleFilter = apFilter;
            } else {
                circleFilter = TableQuery.combineFilters(circleFilter, TableQuery.Operators.OR, apFilter);
            }
        }
        return circleFilter;
    }

    private HashMap<String, LinkedList<Bomb>> groupBomb(Iterable<Bomb> bombIterable) {
        HashMap<String, LinkedList<Bomb>> groupedBombs = new HashMap<>();
        for(Bomb bomb : bombIterable){
            LinkedList bombs = groupedBombs.get(bomb.getRootMessageId());
            if(null == bombs) {
                bombs = new LinkedList<>();
                groupedBombs.put(bomb.getRootMessageId(), bombs);
            }
            bombs.add(bomb);
        }

        return groupedBombs;
    }

    private MinHeap buildMinHeap(HashMap<String, LinkedList<Bomb>> groupedBombs) {
        Iterator<Map.Entry<String, LinkedList<Bomb>>> iterator = groupedBombs.entrySet().iterator();
        final int k = Math.min(10, groupedBombs.size());
        ArrayList<Map.Entry<String, LinkedList<Bomb>>> heap = new ArrayList<>();
        MinHeap minHeap = null;

        while (iterator.hasNext()) {
            Map.Entry<String, LinkedList<Bomb>> next = iterator.next();
            if(heap.size() < k) {
                heap.add(next);
            } else {
                minHeap.offer(next);
            }

            if(k == heap.size()) {
                minHeap = new MinHeap(heap);
            }
        }
        return minHeap;
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
