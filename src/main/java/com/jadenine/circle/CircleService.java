package com.jadenine.circle;

import com.jadenine.circle.resources.HelloWorldResource;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

/**
 * Created by linym on 6/1/15.
 */
public class CircleService extends Application<CircleConfiguration> {
    public static void main(String[] args) throws Exception {
        new CircleService().run(args);
    }

    @Override
    public void run(CircleConfiguration circleConfiguration, Environment environment) throws
            Exception {
        environment.jersey().register(new HelloWorldResource());
    }
}
