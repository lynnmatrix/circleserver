package com.jadenine.circle.response;

import com.jadenine.circle.entity.UserAp;

import java.util.List;

/**
 * Created by linym on 6/2/15.
 */
public class UserApList {
    private List<UserAp> userAps;

    public UserApList(List<UserAp> userAps) {
        this.userAps = userAps;
    }
}
