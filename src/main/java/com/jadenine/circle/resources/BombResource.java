package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.Bomb;

import javax.ws.rs.Path;

/**
 * Created by linym on 7/22/15.
 */
@Path("/bomb")
public class BombResource extends TimelineResource<Bomb> {
    public BombResource(int defaultPageSize, int
            maxPageSize) {
        super(Bomb.class, Storage.getInstance().getBombTable
                (), defaultPageSize, maxPageSize);
    }
}
