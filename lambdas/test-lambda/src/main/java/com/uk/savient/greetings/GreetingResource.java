package com.uk.savient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/greetings")
public class GreetingResource {

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello from jaxrs";
    }

    @GET
    @Path("/evening")
    @Produces(MediaType.TEXT_PLAIN)
    public String evenin() { return "evenin squire!"; }
}
