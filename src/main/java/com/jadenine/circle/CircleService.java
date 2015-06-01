package com.jadenine.circle;

import com.jadenine.circle.resources.HelloWorldResource;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

/**
 * Created by linym on 6/1/15.
 */
public class CircleService extends Service<CircleConfiguration> {
    public static void main(String[] args) throws Exception {
        new CircleService().run(args);
    }

    @Override
    public void initialize(Bootstrap<CircleConfiguration> bootstrap) {

    }

    @Override
    public void run(CircleConfiguration circleConfiguration, Environment environment) throws Exception {
            environment.addResource(new HelloWorldResource());
    }
}
