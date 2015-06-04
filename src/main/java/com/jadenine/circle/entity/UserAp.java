package com.jadenine.circle.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.table.TableServiceEntity;

/**
 * Created by linym on 6/2/15.
 */
@JsonIgnoreProperties(value = {"rowKey", "partitionKey"}, ignoreUnknown = true)
public class UserAp extends TableServiceEntity {
    private String SSID;

    public UserAp(){}
    public UserAp(String user, String ap) {
        this.rowKey = user;
        this.partitionKey = ap;
    }

    public String getUser(){
        return rowKey;
    }

    public void setUser(String user){
        rowKey = user;
    }

    public String getAp(){
        return partitionKey;
    }

    public void setAp(String ap) {
        partitionKey = ap;
    }

    @JsonProperty("ssid")
    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

}
