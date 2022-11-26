package org.j3d;

import java.util.Vector;

public class ResourceManager extends Resource {

    private final Vector<Resource> resources = new Vector<>();

    public boolean isManaged(Resource resource) {
        return resources.contains(resource);
    }

    public <T extends Resource> T manage(T resource) {
        resources.add(resource);
        return resource;
    }

    public void unManage(Resource resource) throws Exception {
        resources.remove(resource);
        resource.destroy();
    }

    public void clear() throws Exception {
        for(Resource resource : resources) {
            resource.destroy();
        }
        resources.removeAllElements();
    }

    @Override
    public void destroy() throws Exception {
        clear();
        super.destroy();
    }
}
