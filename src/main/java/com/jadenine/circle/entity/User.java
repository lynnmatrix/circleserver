package com.jadenine.circle.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.microsoft.azure.storage.table.TableServiceEntity;

/**
 * Created by linym on 6/2/15.
 */
@JsonIgnoreProperties(value = {"rowKey", "partitionKey"}, ignoreUnknown = true)
public class User extends TableServiceEntity {

    public User(){}

    public void setUserId(String userId){
        rowKey = userId;
    }

    public String getUserId() {
        return rowKey;
    }

    public void setMac(String mac) {
        this.partitionKey = mac;
    }

    public String getMac() {
        return partitionKey;
    }
}


