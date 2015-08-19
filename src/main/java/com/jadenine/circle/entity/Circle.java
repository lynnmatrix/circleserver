package com.jadenine.circle.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jadenine.circle.Storage;
import com.microsoft.azure.storage.table.Ignore;
import com.microsoft.azure.storage.table.TableServiceEntity;

/**
 * Created by linym on 8/19/15.
 */@JsonIgnoreProperties(value = {Storage.PARTITION_KEY, Storage.ROW_KEY}, ignoreUnknown = true)
public class Circle extends TableServiceEntity{

    private String name;

    public Circle() {}

    public Circle(String tag, String rowKey, String name) {
        setTag(tag);
        setRowKey(rowKey);
        setName(name);
    }

    @Ignore
    public String getTag() {
        return getPartitionKey();
    }

    public void setTag(String tag){
        setPartitionKey(tag);
    }

    @Ignore
    public String getCircleId() {
        return getRowKey();
    }

    public void setCircleId(String circleId){
        setRowKey(circleId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
