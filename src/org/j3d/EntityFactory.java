package org.j3d;

import java.util.Vector;

public class EntityFactory extends ClassLoader {
    private static EntityFactory instance = null;

    public static EntityFactory newInstance() {
        return new EntityFactory();
    }

    private Vector<Class<?>> entityTypes = new Vector<>();

    private EntityFactory() {
        if(instance != null) {
            entityTypes.addAll(instance.entityTypes);
        }
        instance = this;
    }

    public int count() {
        return entityTypes.size();
    }

    public Class<?> typeAt(int i) {
        return entityTypes.get(i);
    }

    public void addType(Class<?> type) {
        entityTypes.add(type);
    }

    public void removeAllTypes() {
        entityTypes.removeAllElements();
    }

    @Override
    public Class<?> findClass(String name) {
        try {
            return Class.forName(name);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
