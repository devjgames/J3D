package org.j3d;

import java.util.*;
import java.io.*;

public abstract class Entity {

    public final Vec3 position = new Vec3();
    public final Vec3 size = new Vec3(16, 32, 16);

    public Object tag = null;

    private Game game;
    private Scene scene;
    private Vector<Entity> entities;

    public Game game() {
        return game;
    }

    public AssetManager assets() {
        return game.assets();
    }

    public Renderer renderer() {
        return game.renderer();
    }

    public Scene scene() {
        return scene;
    }

    public Camera camera() {
        return scene.camera;
    }

    public int entityCount() {
        return entities.size();
    }

    public Entity entityAt(int i) {
        return entities.get(i);
    }

    public Entity find(Class<?> type) {
        for(Entity entity : entities) {
            if(type.isAssignableFrom(entity.getClass())) {
                return entity;
            }
        }
        return null;
    }

    public void find(Class<?> type, Vector<Entity> entities) {
        for(Entity entity : this.entities) {
            if(type.isAssignableFrom(entity.getClass())) {
                entities.add(entity);
            }
        }
    }

    void init(Game game, Scene scene, Vector<Entity> entities) {
        this.game = game;
        this.scene = scene;
        this.entities = entities;
    }

    public void init() throws Exception {
    }

    public void start() throws Exception {
    }

    public void renderSprites() throws Exception {
    }

    public void update() throws Exception {
    }

    public File loadFile() {
        return null;
    }

    public Entity newInstance() throws Exception {
        Entity entity = (Entity)getClass().getConstructors()[0].newInstance();

        Utils.copy(this, entity);

        return entity;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}