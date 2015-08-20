package com.jadenine.circle;

import com.jadenine.circle.notification.NotificationService;
import com.jadenine.circle.resources.BombResource;
import com.jadenine.circle.resources.CircleResource;
import com.jadenine.circle.resources.DirectMessageResource;
import com.jadenine.circle.resources.ImageResource;

import io.dropwizard.Application;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;

/**
 * Created by linym on 6/1/15.
 */
public class CircleApplication extends Application<CircleConfiguration> {
    public static void main(String[] args) throws Exception {
        new CircleApplication().run(args);
    }

    @Override
    public void run(CircleConfiguration circleConfiguration, Environment environment) throws
            Exception {
        NotificationService.setObjectMapper(environment.getObjectMapper());

        JerseyEnvironment jersey = environment.jersey();
        jersey.register(new CircleResource());
        jersey.register(new DirectMessageResource());
        jersey.register(new BombResource(50/*default_page_count*/, 200/*max_page_count*/));
        jersey.register(new ImageResource());
    }

}
