package com.jadenine.circle.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.microsoft.azure.storage.table.TableServiceEntity;

/**
 * Created by linym on 6/9/15.
 */
@JsonIgnoreProperties(value = {"rowKey", "partitionKey"})
public class Topic extends TableServiceEntity{

    private String user;

    private String topic;

    private String latestMessageId;

    public Topic(){}

    public Topic(String ap, Message firstMessage) {
        partitionKey = ap;
        rowKey = firstMessage.getMessageId();

        user = firstMessage.getUser();
        topic = firstMessage.getContent();

        latestMessageId = firstMessage.getMessageId();
    }

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
}
