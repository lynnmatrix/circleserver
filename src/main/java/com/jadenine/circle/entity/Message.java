package com.jadenine.circle.entity;

import com.microsoft.azure.storage.table.TableServiceEntity;

/**
 * Created by linym on 6/2/15.
 */
public class Message extends TableServiceEntity {
    private String content;
    private String user;

    public Message( String msgId, String ap){
        this.rowKey = msgId;
        this.partitionKey = ap;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUser(String user){
        this.user = user;
    }
}
