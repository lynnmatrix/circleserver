package com.jadenine.circle.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.microsoft.azure.storage.table.TableServiceEntity;

import java.util.Date;

import javax.validation.constraints.NotNull;

/**
 * Created by linym on 6/9/15.
 */
@JsonIgnoreProperties(value = {"rowKey", "partitionKey"})
public class Topic extends TableServiceEntity{
    public static final String CREATED_TIMESTAMP = "CreatedTimestamp";

    @NotNull
    private String user;

    @NotNull
    private String topic;

    private String latestMessageId;

    private Date createdTimestamp;

    public void setAp(String ap) {
        this.partitionKey = ap;
    }

    public String getAp(){
        return partitionKey;
    }

    public String getTopicId(){
        return rowKey;
    }

    public void setTopicId(String topicId){
        this.rowKey = topicId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTopic(){
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getLatestMessageId(){
        return latestMessageId;
    }

    public void setLatestMessageId(String messageId) {
        this.latestMessageId = messageId;
    }

    public Date getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Date createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }
}
