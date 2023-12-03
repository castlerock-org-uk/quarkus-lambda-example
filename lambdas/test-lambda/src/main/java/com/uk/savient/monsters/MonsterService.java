package com.uk.savient.monsters;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
@Transactional
public class MonsterService {

    private final MonsterRepository monsterRepository;

    public MonsterService(final MonsterRepository monsterRepository) {
        this.monsterRepository = monsterRepository;
    }

    public UUID addMonster(Monster monster) {
        monsterRepository.persist(monster);
        return monster.getId();
    }

    public List<Monster> getAllMonsters() {
        return monsterRepository.listAll();
    }

}
