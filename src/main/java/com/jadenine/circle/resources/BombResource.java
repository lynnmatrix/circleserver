package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.Bomb;
import com.jadenine.circle.entity.UserAp;
import com.jadenine.circle.notification.NotificationService;
import com.jadenine.circle.response.JSONListWrapper;
import com.microsoft.azure.storage.table.TableQuery;

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

    @POST
    @Path("/list/top")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listTop(@QueryParam("auth") @NotNull String auth) {
        if (null == auth || auth.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No auth").build();
        }

        List<UserAp> aps = ApLister.list(auth);
        if(aps.isEmpty()) {
            return Response.ok().entity(new JSONListWrapper(Collections.emptyList())).build();
        }

        String filter = prepareFilter(aps);

        System.out.println(filter);

        TableQuery<Bomb> query = TableQuery.from(Bomb.class).where
                (filter);

        Iterable<Bomb> bombIterable = Storage.getInstance().getBombTable().execute(query);
        HashMap<String, LinkedList<Bomb>> groupedBombs = groupBomb(bombIterable);
        if(groupedBombs.size() <= 0) {
            return Response.ok().entity(new JSONListWrapper(Collections.emptyList())).build();
        }
        MinHeap minHeap = buildMinHeap(groupedBombs);

        String[] topIds = minHeap.top();
        List<Bomb> resultBomb = new ArrayList<>();

        for(String topId : topIds) {
            resultBomb.addAll(groupedBombs.get(topId));
        }
        return Response.ok().entity(new JSONListWrapper(resultBomb, false, null)).build();
    }

    private String prepareFilter(List<UserAp> aps) {
        String timeFilter = TableQuery.generateFilterCondition(Storage.TIMESTAMP, TableQuery
                .QueryComparisons.GREATER_THAN, new Date(System.currentTimeMillis() - TimeUnit.DAYS
                .toMillis(1)));
        String filter = timeFilter;

        String apsFilter = genApFilter(aps);
        filter = TableQuery.combineFilters(filter, TableQuery.Operators.AND, apsFilter);
        return filter;
    }

    private String genApFilter(List<UserAp> aps) {
        String apsFilter = null;
        for(UserAp ap : aps) {
            String apFilter = TableQuery.generateFilterCondition(Storage.PARTITION_KEY, TableQuery

                    .QueryComparisons.EQUAL, ap.getAp());
            if (null == apsFilter) {
                apsFilter = apFilter;
            } else {
                apsFilter = TableQuery.combineFilters(apsFilter, TableQuery.Operators.OR, apFilter);
            }
        }
        return apsFilter;
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
