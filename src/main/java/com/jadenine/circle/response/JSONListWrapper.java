package com.jadenine.circle.response;

import java.util.List;

/**
 * Created by linym on 6/2/15.
 */
public class JSONListWrapper {
    public boolean hasMore;
    public List<?> itemList;

    public JSONListWrapper(List<?> itemlist) {
        this(itemlist, false);
    }

    public JSONListWrapper(List<?> itemlist, boolean hasMore) {
        this.itemList = itemlist;
        this.hasMore = hasMore;
    }
}