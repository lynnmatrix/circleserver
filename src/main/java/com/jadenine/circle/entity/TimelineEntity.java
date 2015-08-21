package com.jadenine.circle.entity;

import com.microsoft.azure.storage.table.TableEntity;

/**
 * Created by linym on 7/21/15.
 */
public interface TimelineEntity extends TableEntity, WithinCircle {
    String getMessageId();
    String getRootMessageId();
    String getFrom();

    void setRootMessageId(String rootMessageId);
    void setRootUser(String rootUser);
}
