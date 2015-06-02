package com.jadenine.circle.entity;

import com.microsoft.azure.storage.table.TableServiceEntity;

/**
 * Created by linym on 6/2/15.
 */
public class UserAp extends TableServiceEntity {
    public UserAp(String user, String ap) {
        this.rowKey = user;
        this.partitionKey = ap;
    }
}
