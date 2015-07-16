package com.jadenine.circle.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jadenine.circle.Storage;
import com.microsoft.azure.storage.table.Ignore;
import com.microsoft.azure.storage.table.TableServiceEntity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by linym on 6/2/15.
 */
@JsonIgnoreProperties(value = {Storage.PARTITION_KEY, Storage.ROW_KEY}, ignoreUnknown = true)
public class Message extends TableServiceEntity {

    public static final String FIELD_AP = "ap";

    @NotNull
    private String ap;

    @NotNull
    private String user;

    @NotNull
    @Size(min=Constants.MIN_CONTENT_LENGTH, max=Constants.MAX_CONTENT_LENGTH)
    private String content;

    private String replyToUser;

    private boolean privacy;

    @Ignore
    public void setTopicId(String topic) {
        partitionKey = topic;
    }

    public String getTopicId(){
        return partitionKey;
    }

    @Ignore
    public void setMessageId(String messageId) {
        rowKey = messageId;
    }

    public String getMessageId(){
        return rowKey;
    }

    @JsonProperty(FIELD_AP)
    public String getAp() {
        return ap;
    }

    public void setAp(String ap) {
        this.ap = ap;
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

    public String getReplyToUser() {
        return replyToUser;
    }

    public void setReplyToUser(String replyToUser) {
        this.replyToUser = replyToUser;
    }

    public boolean getPrivacy() {
        return privacy;
    }

    public void setPrivacy(boolean privacy) {
        this.privacy = privacy;
    }

}
