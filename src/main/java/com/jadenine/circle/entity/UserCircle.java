package com.jadenine.circle.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jadenine.circle.Storage;
import com.microsoft.azure.storage.table.Ignore;
import com.microsoft.azure.storage.table.TableServiceEntity;

/**
 * Created by linym on 8/19/15.
 */
@JsonIgnoreProperties(value = {Storage.PARTITION_KEY, Storage.ROW_KEY}, ignoreUnknown = true)
public class UserCircle extends TableServiceEntity{

    public UserCircle(){}

    public UserCircle(String user, String circleId) {
        setUser(user);
        setCircle(circleId);
    }

    public String getUser(){
        return getPartitionKey();
    }

    @Ignore
    public void setUser(String user){
        setPartitionKey(user);
    }

    public String getCircle(){
        return getRowKey();
    }

    @Ignore
    public void setCircle(String circle) {
        setRowKey(circle);
    }

}
