package com.uk.savient.monsters;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@Path("/monsters")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MonsterResource {

    private final MonsterService monsterService;

    public MonsterResource(final MonsterService monsterService) {
        this.monsterService = monsterService;
    }

    @POST
    @Path("/add")
    public UUID addMonster(Monster monster) {
        return monsterService.addMonster(monster);
    }

    @GET
    @Path("/list")
    public List<Monster> listMonsters() {
        return monsterService.getAllMonsters();
    }

}
