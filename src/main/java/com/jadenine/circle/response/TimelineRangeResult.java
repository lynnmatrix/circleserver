package com.jadenine.circle.response;

import java.util.Collections;
import java.util.List;

/**
 * Created by linym on 6/2/15.
 */
public class TimelineRangeResult<T> {
    private List<T> itemList;
    private boolean hasMore;
    private String nextId;

    public TimelineRangeResult(){}

    public TimelineRangeResult(List<T> itemList) {
        this(itemList, false, null);
    }

    public TimelineRangeResult(List<T> itemList, boolean hasMore, String nextId) {
        this.itemList = itemList;
        this.hasMore = hasMore;
        this.nextId = nextId;
    }

    public List<T> getItemList() {
        if(null == itemList) {
            return Collections.emptyList();
        }
        return itemList;
    }

    public boolean getHasMore(){
        return hasMore;
    }

    public String getNextId(){
        return nextId;
    }
}