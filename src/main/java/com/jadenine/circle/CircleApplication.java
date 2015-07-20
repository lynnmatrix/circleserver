package com.jadenine.circle;

import com.jadenine.circle.resources.ApResource;
import com.jadenine.circle.resources.DirectMessageResource;
import com.jadenine.circle.resources.ImageResource;
import com.jadenine.circle.resources.MessageResource;
import com.jadenine.circle.resources.TopicResource;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import io.dropwizard.Application;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;

/**
 * Created by linym on 6/1/15.
 */
public class CircleApplication extends Application<CircleConfiguration> {
    public static void main(String[] args) throws Exception {
        new CircleApplication().run(args);
        startStorage();
    }

    private static void startStorage() throws URISyntaxException, InvalidKeyException, StorageException {

        CloudTable userTable = Storage.getInstance().getUserTable();
        userTable.createIfNotExists();

        CloudTable userApTable = Storage.getInstance().getUserApTable();
        userApTable.createIfNotExists();

        CloudTable topicTable = Storage.getInstance().getTopicTable();
        topicTable.createIfNotExists();

        CloudTable messageTable = Storage.getInstance().getMessageTable();
        messageTable.createIfNotExists();

        CloudTable chatMessage = Storage.getInstance().getChatTable();
        chatMessage.createIfNotExists();
    }

    @Override
    public void run(CircleConfiguration circleConfiguration, Environment environment) throws
            Exception {
        JerseyEnvironment jersey = environment.jersey();
        jersey.register(new ApResource());
        jersey.register(new TopicResource());
        jersey.register(new MessageResource());
        jersey.register(new DirectMessageResource());
        jersey.register(new ImageResource());
    }

}
