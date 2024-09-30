package org.apocalypse.api.lobby;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apocalypse.api.location.Location;
import org.apocalypse.api.map.Map;
import org.apocalypse.api.map.area.Area;
import org.apocalypse.api.map.area.door.Door;
import org.apocalypse.api.map.area.loot.Loot;
import org.apocalypse.api.map.area.spawn.Spawn;
import org.apocalypse.api.map.area.spawn.barrier.Barrier;
import org.apocalypse.api.monster.Monster;
import org.apocalypse.api.monster.type.MonsterType;
import org.apocalypse.api.player.Survivor;
import org.apocalypse.api.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Lobby {

    private final List<Survivor> survivors;
    private final List<Monster> monsters = new ArrayList<>();
    private final List<? extends MonsterType> types;
    private final Map map;
    private final World world;
    private Loot loot;
    private int wave = 0;

    @SneakyThrows
    public Lobby(Class<? extends Map> map) {
        this.survivors = new ArrayList<>();
        this.map = map.getConstructor().newInstance();
        this.world = Bukkit.getServer().createWorld(new WorldCreator("worlds/" + map.getSimpleName().toLowerCase()));
        assert this.world != null;
        this.world.setAutoSave(false);
        this.world.setPVP(false);
        this.world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        this.types = this.map.getMonster().stream().map(monster -> {
            try {
                return monster.getConstructor().newInstance();
            } catch (Exception e) {
                return null;
            }
        }).toList();
    }

    public void add(Survivor survivor) {
        this.survivors.add(survivor);
    }

    public void remove(Survivor survivor) {
        this.survivors.remove(survivor);
    }

    public int size() {
        return this.survivors.size();
    }

    public void nextWave() {
        this.wave++;
        int amount = wave * 5;

        List<? extends MonsterType> monster = new ArrayList<>(this.types);
        monster.stream().filter(type -> type.getFirst() >= this.wave && type.getLast() <= this.wave).forEach(monster::remove);
        Bukkit.getServer().broadcastMessage(String.valueOf(monster));
    }

    public void win() {

    }

    public void lose() {

    }

    public Area getArea(Location location) {
        Area closest = null;
        double distance = Double.MAX_VALUE;
        for (Area area : this.map.getAreas()) {
            double current = area.getLocation().distance(location);
            if (current < distance) {
                closest = area;
                distance = current;
            }
        } return closest;
    }

    public Door getDoor(Location location) {
        for (Area area : this.map.getAreas()) {
            for (Door door : area.getDoors()) {
                if (LocationUtils.getMiddle(door.getFirst().get(location.getWorld()), door.getSecond().get(location.getWorld()))
                        .distance(location.get()) < 2)
                    return door;
            }
        } return null;
    }

    public Barrier getBarrier(Location location) {
        for (Area area : this.map.getAreas()) {
            for (Spawn spawn : area.getSpawns()) {
                if (spawn.getBarrier().getCenter(location.getWorld()).distance(location) < 5)
                    return spawn.getBarrier();
            }
        } return null;
    }

    public Loot getLoot(Location location) {
        for (Area area : this.map.getAreas()) {
            if (area.getLoot() == null) continue;
            if (area.getLoot().getLocation().get(location.getWorld()).distance(location.get()) < 2)
                return area.getLoot();
        } return null;
    }
}
