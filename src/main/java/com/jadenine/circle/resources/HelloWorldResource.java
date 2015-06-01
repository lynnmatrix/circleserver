package com.jadenine.circle.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Created by linym on 6/1/15.
 */
@Path("/greetings/{name}")
public class HelloWorldResource {
    @GET
    public String getGreeting(@PathParam("name") String name){
        return "Hello, " + name + "!";
    }
}
