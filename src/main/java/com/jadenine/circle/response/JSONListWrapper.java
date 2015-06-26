package com.jadenine.circle.response;

import java.util.List;

/**
 * Created by linym on 6/2/15.
 */
public class JSONListWrapper {
    public List<?> itemList;
    public boolean hasMore;
    public String nextId;

    public JSONListWrapper(List<?> itemlist) {
        this(itemlist, false, null);
    }

    public JSONListWrapper(List<?> itemlist, boolean hasMore, String nextId) {
        this.itemList = itemlist;
        this.hasMore = hasMore;
        this.nextId = nextId;
    }
}