package com.jadenine.circle.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.table.TableServiceEntity;

/**
 * Created by linym on 6/2/15.
 */
public class UserAp extends TableServiceEntity {
    @JsonCreator
    public UserAp(@JsonProperty("user") String user, @JsonProperty("ap") String ap) {
        this.rowKey = user;
        this.partitionKey = ap;
    }

    @JsonProperty("user")
    public String getUser(){
        return rowKey;
    }

    @JsonProperty("ap")
    public String getAp(){
        return partitionKey;
    }
}
