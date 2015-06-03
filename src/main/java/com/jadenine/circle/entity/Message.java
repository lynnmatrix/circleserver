package com.jadenine.circle.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.table.TableServiceEntity;

/**
 * Created by linym on 6/2/15.
 */
@JsonIgnoreProperties(value = {"rowKey", "partitionKey"}, ignoreUnknown = true)
public class Message extends TableServiceEntity {
    private String content;
    private String user;

    public Message(){}

    @JsonProperty
    public void setMessageId(String messageId) {
        rowKey = messageId;
    }

    @JsonProperty
    public String getMessageId(){
        return rowKey;
    }

    @JsonProperty
    public void setAp(String ap) {
        partitionKey = ap;
    }

    @JsonProperty
    public String getAp(){
        return partitionKey;
    }

    @JsonProperty
    public void setContent(String content) {
        this.content = content;
    }

    @JsonProperty
    public void setUser(String user){
        this.user = user;
    }

    @JsonProperty
    public String getContent(){
        return content;
    }

    @JsonProperty
    public String getUser(){
        return user;
    }
}
