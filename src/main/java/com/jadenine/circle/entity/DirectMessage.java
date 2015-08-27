package com.jadenine.circle.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jadenine.circle.Storage;
import com.microsoft.azure.storage.table.Ignore;
import com.microsoft.azure.storage.table.StoreAs;
import com.microsoft.azure.storage.table.TableServiceEntity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by linym on 7/15/15.
 */
@JsonIgnoreProperties(value = {Storage.PARTITION_KEY, Storage.ROW_KEY}, ignoreUnknown = true)
public class DirectMessage extends TableServiceEntity implements TimelineEntity {
    public static final String FIELD_FROM = "From";//NOTE: Field name is case sensitive!!
    public static final String FIELD_TO = "To";

    //circle = partitionKey
    //messageId = rowKey

    @NotNull
    private String topicId;

    private String rootMessageId;
    private String rootUser;

    @NotNull
    private String from;
    private String to;

    @NotNull
    @Size(min=Constants.MIN_CONTENT_LENGTH)
    private String content;

    @Ignore
    public void setMessageId(String messageId) {
        this.rowKey = messageId;
    }

    public String getMessageId() {
        return rowKey;
    }

    @Override
    @Ignore
    public String getCircle() {
        return partitionKey;
    }
    @Override
    public void setCircle(String circle) {
        setPartitionKey(circle);
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public String getRootMessageId() {
        return rootMessageId;
    }
    public void setRootMessageId(String rootMessageId) {
        this.rootMessageId = rootMessageId;
    }

    public String getRootUser() {
        return rootUser;
    }

    public void setRootUser(String rootUser) {
        this.rootUser = rootUser;
    }

    public void setTo(String to) {
        this.to = to;
    }
    public String getTo() {
        return to;
    }

    @StoreAs(name = FIELD_FROM)
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
}
