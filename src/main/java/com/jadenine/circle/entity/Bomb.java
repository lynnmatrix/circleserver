package com.jadenine.circle.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jadenine.circle.Storage;
import com.microsoft.azure.storage.table.Ignore;
import com.microsoft.azure.storage.table.TableServiceEntity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by linym on 7/21/15.
 */
@JsonIgnoreProperties(value = {Storage.PARTITION_KEY, Storage.ROW_KEY}, ignoreUnknown = true)
public class Bomb extends TableServiceEntity implements TimelineEntity {
    public static final String FIELD_ROOT_USER = "RootUser";
    public static final String FIELD_FROM = "From";
    public static final String FIELD_To = "To";
    public static final String FIELD_ROOT_MESSAGE_ID = "RootMessageId";

    //circle = partitionKey
    //messageId = rowKey

    private String rootMessageId;
    private String rootUser;

    @NotNull
    private String from;
    private String to;

    @NotNull
    @Size(min=Constants.MIN_CONTENT_LENGTH)
    private String content;

    private String images;

    @Ignore
    public String getCircle() {
        return partitionKey;
    }
    public void setCircle(String circle) {
        this.partitionKey = circle;
    }

    @Ignore
    public void setMessageId(String messageId) {
        this.rowKey = messageId;
    }

    @Override
    public String getMessageId() {
        return rowKey;
    }

    @Override
    public String getRootMessageId() {
        return rootMessageId;
    }
    @Override
    public void setRootMessageId(String rootMessageId) {
        this.rootMessageId = rootMessageId;
    }

    public String getRootUser() {
        return rootUser;
    }

    @Override
    public void setRootUser(String rootUser) {
        this.rootUser = rootUser;
    }

    @Override
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }
    public String getTo() {
        return to;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

}
