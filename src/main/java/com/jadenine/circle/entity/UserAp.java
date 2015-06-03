package com.jadenine.circle.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.table.TableServiceEntity;

/**
 * Created by linym on 6/2/15.
 */
@JsonIgnoreProperties(value = {"rowKey", "partitionKey"}, ignoreUnknown = true)
public class UserAp extends TableServiceEntity {
    public UserAp(){}
    public UserAp(String user, String ap) {
        this.rowKey = user;
        this.partitionKey = ap;
    }

    @JsonProperty
    public String getUser(){
        return rowKey;
    }

    @JsonProperty
    public String getAp(){
        return partitionKey;
    }

    @JsonProperty
    public void setUser(String user){
        rowKey = user;
    }

    @JsonProperty
    public void setAp(String ap) {
        partitionKey = ap;
    }
}
