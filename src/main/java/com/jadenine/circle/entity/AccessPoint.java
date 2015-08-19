package com.jadenine.circle.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jadenine.circle.Storage;
import com.microsoft.azure.storage.table.Ignore;
import com.microsoft.azure.storage.table.TableServiceEntity;

/**
 * Created by linym on 8/19/15.
 */
@JsonIgnoreProperties(value = {Storage.PARTITION_KEY, Storage.ROW_KEY}, ignoreUnknown = true)
public class AccessPoint extends TableServiceEntity {
    private String SSID;

    @Ignore
    public String getCircle() {
        return partitionKey;
    }

    public void setCircle(String circle) {
         setPartitionKey(circle);
    }

    @Ignore
    public String getMac(){
        return getRowKey();
    }

    public void setMac(String mac) {
        setRowKey(mac);
    }

    @JsonProperty("ssid")
    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

}
