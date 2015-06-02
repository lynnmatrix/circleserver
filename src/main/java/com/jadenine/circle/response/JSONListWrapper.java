package com.jadenine.circle.response;

import java.util.List;

/**
 * Created by linym on 6/2/15.
 */
public class JSONListWrapper {
    public List<?> itemlist;
    public JSONListWrapper(List<?> itemlist) {
        this.itemlist = itemlist;
    }
}