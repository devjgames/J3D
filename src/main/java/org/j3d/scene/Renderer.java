package org.j3d.scene;

import java.util.LinkedList;
import java.util.Vector;

import org.j3d.BlendState;
import org.j3d.Game;
import org.j3d.Material;
import org.j3d.PixelLightMaterial;
import org.j3d.Utils;
import org.j3d.Light;
import org.j3d.scene.Batch.BatchRecord;
import org.joml.Vector3f;

public class Renderer {
    
    private final Vector<Batch> batches = new Vector<>();
    private final Vector<BatchRecord> batchRecords = new Vector<>();
    private final LinkedList<BatchRecord> batchRecordPool = new LinkedList<>();
    private final LinkedList<Batch> batchPool = new LinkedList<>();
    private final Vector<Node> lightNodes = new Vector<>();
    private final Vector<Light> lights1 = new Vector<>();
    private final Vector<Light> lights2 = new Vector<>();
    private Node linesNode = new Node();
    private int binds = 0;
    private int nodes = 0;
    private int lights = 0;
    private int objects = 0;
    private Node lineNode = new Node();

    public Renderer() {
        for(int i = 0; i != PixelLightMaterial.MAX_LIGHTS; i++) {
            lights1.add(new Light());
        }
    }

    public int getBinds() {
        return binds;
    }

    public int getNodes() {
        return nodes;
    }

    public int getLights() {
        return lights;
    }

    public int getObjects() {
        return objects;
    }

    public void render(Scene scene, Game game) throws Exception {
        scene.calcTransform(game);
        scene.root.calcBoundsAndTransform();
        scene.root.update(scene);
        scene.root.calcBoundsAndTransform();

        objects = 0;

        if(scene.lines != null) {
            Batch batch = createBatch();

            linesNode.renderable = scene.lines;
            scene.lines.scene = scene;
            scene.lines.selection = scene.selection;
            batch.records.add(createBatchRecord(lineNode, scene.lines, 0));
            batches.add(batch);
            scene.polygonOffsetEnabled = true;
            lineNode.renderable = scene.lines;
            lineNode.update(scene);
        } 

        addRenderablesAndLights(scene.root);

        int order = 0;

        for(Batch batch : batches) {
            for(BatchRecord record  : batch.records) {
                record.order = order;
                batchRecords.add(record);
            }
            order++;
        }
        clearBatches();

        batchRecords.sort((a, b) -> {
            if(a == b) {
                return 0;
            } else if(a.zOrder == b.zOrder) {
                if(a.node.blendState == BlendState.OPAQUE || b.node.blendState == BlendState.OPAQUE) {
                    return Integer.compare(a.order, b.order);
                } else {
                    float d1 = a.node.absolutePosition.distance(scene.eye);
                    float d2 = b.node.absolutePosition.distance(scene.eye);
    
                    return Float.compare(d2, d1);
                }
            } else {
                return Integer.compare(a.zOrder, b.zOrder);
            }
        });

        lightNodes.sort((a, b) -> {
            if(a == b) {
                return 0;
            } else {
                Vector3f point = (scene.sortLightsAroundTarget) ? scene.target : scene.eye;
                float d1 = a.absolutePosition.distance(point);
                float d2 = b.absolutePosition.distance(point);

                return Float.compare(d1, d2);
            }
        });

        int count = Math.min(lightNodes.size(), PixelLightMaterial.MAX_LIGHTS);

        for(int i = 0; i != count; i++) {
            Node lightNode = lightNodes.get(i);
            Light light = lights1.get(i);

            light.position.set(lightNode.absolutePosition);
            light.color.set(lightNode.lightColor);
            light.radius = lightNode.lightRadius;

            lights2.add(light);
        }

        Utils.clear(scene.backgroundColor.x, scene.backgroundColor.y, scene.backgroundColor.z, scene.backgroundColor.w);
        Utils.enablePolygonOffset(scene.polygonOffsetEnabled, scene.polygonOffsetFactor, scene.polygonOffsetUnits);

        BatchRecord last = null;

        binds = 0;
        lights = count;
        nodes = 0;

        for(BatchRecord record : batchRecords) {
            boolean bind = last == null;

            if(!bind) {
                bind = last.order != record.order;
            }
            if(bind) {
                if(last != null) {
                    last.material.end();
                }
                Utils.setDepthState(record.node.depthState);
                Utils.setBlendState(record.node.blendState);
                Utils.setCullState(record.node.cullState);
                record.material.begin(lights2);
                last = record;
                binds++;
            }
            record.material.render(scene.projection, scene.view, record.node.model);
            nodes++;
        }
        if(last != null) {
            last.material.end();
        }

        game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
        scene.root.pushSprites(this);
        game.getSpritePipeline().end();

        batchRecords.clear();
        lightNodes.clear();
        lights2.clear();
    }

    private void addRenderablesAndLights(Node node) {
        if(node.visible) {
            if(node.renderable != null) {
                for(int i = 0; i != node.renderable.getMaterialCount(); i++) {
                    Material material = node.renderable.materialAt(i);
                    boolean found = false;

                    for(Batch batch : batches) {
                        for(BatchRecord record : batch.records) {
                            if(
                                material.isEqualTo(record.material) && 
                                node.zOrder == record.zOrder && 
                                node.depthState == record.node.depthState && 
                                node.cullState == record.node.cullState && 
                                node.blendState == record.node.blendState) {

                                batch.records.add(createBatchRecord(node, material, node.zOrder));
                                found = true;
                                break;
                            }
                        }
                        if(found) {
                            break;
                        }
                    }
                    if(!found) {
                        Batch batch = createBatch();

                        batch.records.add(createBatchRecord(node, material, node.zOrder));
                        batches.add(batch);
                    }
                }
            }
            if(node.isLight) {
                lightNodes.add(node);
            }
            for(Node child : node) {
                addRenderablesAndLights(child);
            }
        }
    }

    private Batch createBatch() {
        if(!batchPool.isEmpty()) {
            Batch batch = batchPool.removeFirst();

            batch.records.removeAllElements();

            return batch;
        }
        objects++;

        return new Batch();
    }

    private BatchRecord createBatchRecord(Node node, Material material, int zOrder) {
        if(!batchRecordPool.isEmpty()) {
            BatchRecord record =  batchRecordPool.removeFirst();

            record.set(node, material, zOrder);

            return record;
        }
        objects++;

        return new BatchRecord(node, material, zOrder);
    }

    private void clearBatches() {
        for(Batch batch : batches) {
            batchPool.add(batch);
            for(BatchRecord record : batch.records) {
                batchRecordPool.add(record);
            }
        }
        batches.clear();
    }
}
