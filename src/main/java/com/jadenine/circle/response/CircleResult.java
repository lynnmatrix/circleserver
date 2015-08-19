package com.jadenine.circle.response;

import com.jadenine.circle.entity.AccessPoint;
import com.jadenine.circle.entity.Circle;

import java.util.List;

/**
 * Created by linym on 8/19/15.
 */
public class CircleResult {
    private List<Circle> circles;
    private List<AccessPoint> aps;

    public CircleResult(List<Circle> circles, List<AccessPoint> aps) {
        this.circles = circles;
        this.aps = aps;
    }

    public List<Circle> getCircles(){
        return circles;
    }

    public List<AccessPoint> getAps() {
        return aps;
    }
}
