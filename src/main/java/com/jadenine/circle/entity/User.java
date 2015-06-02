package com.jadenine.circle.entity;

import com.microsoft.azure.storage.table.TableServiceEntity;

/**
 * Created by linym on 6/2/15.
 */
public class User extends TableServiceEntity {

    public User(String userId, String mac){
        this.rowKey = userId;
        this.partitionKey = mac;
    }
}
