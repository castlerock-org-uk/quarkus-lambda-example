package com.uk.savient.monsters;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MonsterRepository implements PanacheRepository<Monster> {
}
