package com.jadenine.circle.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.microsoft.azure.storage.table.Ignore;
import com.microsoft.azure.storage.table.TableServiceEntity;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by linym on 6/9/15.
 */
@JsonIgnoreProperties(value = {"rowKey", "partitionKey"})
public class Topic extends TableServiceEntity{
    public static final String CREATED_TIMESTAMP = "CreatedTimestamp";
    public static final String IMAGE_DELIMITER = ",";

    @NotNull
    private String user;

    @NotNull
    @Size(min=Constants.MIN_CONTENT_LENGTH, max=Constants.MAX_CONTENT_LENGTH)
    private String topic;

    private String images;

    private Date createdTimestamp;

    private String latestMessageId;

    private Integer messageCount = 0;

    List<Message> messages;

    @Ignore
    public void setAp(String ap) {
        this.partitionKey = ap;
    }

    public String getAp(){
        return partitionKey;
    }

    @Ignore
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

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    @Ignore
    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
