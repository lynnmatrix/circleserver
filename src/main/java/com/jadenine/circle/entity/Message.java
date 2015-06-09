package com.jadenine.circle.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jadenine.circle.Storage;
import com.microsoft.azure.storage.table.TableServiceEntity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by linym on 6/2/15.
 */
@JsonIgnoreProperties(value = {Storage.PARTITION_KEY, Storage.ROW_KEY}, ignoreUnknown = true)
public class Message extends TableServiceEntity {
    @NotNull
    @Size(min=1, max=256)
    private String content;

    @NotNull
    private String user;

    public void setTopic(String topic) {
        partitionKey = topic;
    }

    public String getTopic(){
        return partitionKey;
    }

    public void setMessageId(String messageId) {
        rowKey = messageId;
    }

    public String getMessageId(){
        return rowKey;
    }

    public void setUser(String user){
        this.user = user;
    }

    public String getUser(){
        return user;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent(){
        return content;
    }
}
