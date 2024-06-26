package org.j3d;

import java.util.Hashtable;
import java.util.Vector;

public class MeshBuilder {
    
    private class Vertex {

        public int index;
        public int edge = -1;
        public final Vec3 position = new Vec3();
        public final Vec3 normal = new Vec3();

        public Vertex(int index) {
            this.index = index;
        }

        public Vertex(Vertex vertex) {
            index = vertex.index;
            edge = vertex.edge;
            position.set(vertex.position);
            normal.set(vertex.normal);
        }
    }

    private class Edge {

        public int index;
        public int prev = -1;
        public int next = -1;
        public int pair = -1;
        public int vertex = -1;
        public int face = -1;
        public final Vec2 textureCoordinate = new Vec2();
        public final Vec3 normal = new Vec3();

        public Edge(int index) {
            this.index = index;
        }

        public Edge(Edge edge) {
            index = edge.index;
            prev = edge.prev;
            next = edge.next;
            pair = edge.pair;
            vertex = edge.vertex;
            face = edge.face;
            textureCoordinate.set(edge.textureCoordinate);
            normal.set(edge.normal);
        }
    }

    private class Face {

        public int index;
        public int edge = -1;
        public final Vec3 normal = new Vec3();

        public Face(int index) {
            this.index = index;
        }

        public Face(Face face) {
            index = face.index;
            edge = face.edge;
            normal.set(face.normal);
        }
    }

    private final Vector<Vertex> vertices = new Vector<>();
    private final Vector<Edge> edges = new Vector<>();
    private final Vector<Face> faces = new Vector<>();
    private final Hashtable<String, Edge> vertexEdges = new Hashtable<>();

    public MeshBuilder() {
    }

    public MeshBuilder(MeshBuilder builder) {
        set(builder);
    }

    public int getVertexCount() {
        return vertices.size();
    }

    public int getVertexEdge(int i) {
        return vertices.get(i).edge;
    }

    public Vec3 getVertexPosition(int i) {
        return vertices.get(i).position;
    }

    public Vec3 getVertexNormal(int i) {
        return vertices.get(i).normal;
    }

    public int addVertex(float x, float y, float z) {
        Vertex v = new Vertex(vertices.size());

        v.position.set(x, y, z);
        vertices.add(v);

        return v.index;
    }

    public int addVertex(Vec3 position) {
        return addVertex(position.x, position.y, position.z);
    }

    public int getEdgeCount() {
        return edges.size();
    }

    public int getEdgePrev(int i) {
        return edges.get(i).prev;
    }

    public int getEdgeNext(int i) {
        return edges.get(i).next;
    }

    public int getEdgePair(int i) {
        return edges.get(i).pair;
    }

    public int getEdgeVertex(int i) {
        return edges.get(i).vertex;
    }

    public int getEdgeFace(int i) {
        return edges.get(i).face;
    }

    public Vec2 getEdgeTextureCoordinate(int i) {
        return edges.get(i).textureCoordinate;
    }

    public Vec3 getEdgeNormal(int i) {
        return edges.get(i).normal;
    }

    public int getFaceCount() {
        return faces.size();
    }

    public int getFaceEdgeCount(int i) {
        int e1 = getFaceEdge(i);
        int e2 = e1;
        int count = 0;

        do {
            count++;
            e1 = getEdgeNext(e1);
        } while(e1 != e2);

        return count;
    }

    public int getFaceEdge(int i) {
        return faces.get(i).edge;
    }

    public Vec3 getFaceNormal(int i) {
        return faces.get(i).normal;
    }
    
    public int addFace(boolean flip, int ... indices) throws Exception {
        if(indices.length < 3) {
            throw new Exception("invalid index count, index count < 3");
        }

        Face f = new Face(faces.size());

        if(flip) {
            int[] temp = new int[indices.length];

            for(int i = 0; i != indices.length; i++) {
                temp[i] = indices[indices.length - i - 1];
            }
            indices = temp;
        }

        for(int i = 0; i != indices.length; i++) {
            if(indices[i] < 0 || indices[i] >= vertices.size()) {
                throw new Exception("invalid vertex index");
            }
            Vertex v = vertices.get(indices[i]);
            Edge e = new Edge(edges.size());

            if(v.edge == -1) {
                v.edge = e.index;
            }
            e.face = f.index;
            e.vertex = v.index;

            if(i == 0) {
                f.edge = e.index;
            } else {
                e.prev = edges.get(f.edge + i - 1).index;
                edges.get(f.edge + i - 1).next = e.index;
            }
            if(i == indices.length - 1) {
                edges.get(f.edge).prev = e.index;
                e.next = edges.get(f.edge).index;
            }
            edges.add(e);
        }

        for(int i = 0; i != indices.length; i++) {
            int v1 = indices[i];
            int v2 = indices[(i + 1) % indices.length];
            String key = keyFor(v1, v2);
            Edge e = edges.get(f.edge + i);

            if(vertexEdges.containsKey(key)) {
                Edge pair = vertexEdges.get(key);

                if(pair.pair != -1) {
                    throw new Exception("edge already has a pair");
                }
                pair.pair = e.index;
                e.pair = pair.index;
            } else {
                vertexEdges.put(key, e);
            }
        }
        faces.add(f);

        return f.index;
    }

    public int addFace(int ... indices) throws Exception {
        return addFace(false, indices);
    }

    public String keyFor(int x, int y) {
        if(x > y) {
            int temp = x;

            x = y;
            y = temp;
        }
        return x + ":" + y;
    }

    public void clear() {
        vertices.clear();
        edges.clear();
        faces.clear();
        vertexEdges.clear();
    }

    public void set(MeshBuilder builder) {
        clear();

        for(Vertex v : builder.vertices) {
            vertices.add(new Vertex(v));
        }
        for(Edge e : builder.edges) {
            edges.add(new Edge(e));
        }
        for(Face f : builder.faces) {
            faces.add(new Face(f));
        }
        for(String key : builder.vertexEdges.keySet()) {
            vertexEdges.put(key, edges.get(builder.vertexEdges.get(key).index));
        }
    }

    public void calcTextureCoordinates(float x, float y, float z, float units) {
        for(int i = 0; i != getEdgeCount(); i++) {
            int f = getEdgeFace(i);
            Vec3 n = getFaceNormal(f);
            Vec3 p = getVertexPosition(getEdgeVertex(i));
            Vec2 t = getEdgeTextureCoordinate(i);
            float nx = Math.abs(n.x);
            float ny = Math.abs(n.y);
            float nz = Math.abs(n.z);

            if(nx > ny && nx > nz) {
                t.set(p.z + z, p.y + y).div(units);
            } else if(ny > nx && ny > nz) {
                t.set(p.x + x, p.z + z).div(units);
            } else {
                t.set(p.x + x, p.y + y).div(units);
            }
        }
    }

    public void calcNormals(boolean smooth) {
        for(int i = 0; i != getFaceCount(); i++) {
            int e1 = getFaceEdge(i);
            int e2 = e1;

            do {
                Vec3 p1 = getVertexPosition(getEdgeVertex(e1));
                Vec3 p2 = getVertexPosition(getEdgeVertex(getEdgeNext(e1)));
                Vec3 p3 = getVertexPosition(getEdgeVertex(getEdgeNext(getEdgeNext(e1))));
                Vec3 v1 = p2.sub(p1, new Vec3());
                Vec3 v2 = p3.sub(p2, new Vec3());
                Vec3 n = v1.cross(v2, new Vec3());

                if(n.length() > 0.0000001) {
                    getFaceNormal(i).set(n.normalize());
                    break;
                }
                e1 = getEdgeNext(e1);
            } while(e1 != e2);
        }
        for(int i = 0; i != getVertexCount(); i++) {
            int e1 = getVertexEdge(i);
            int e2 = e1;
            Vec3 n = getVertexNormal(i);

            n.set(0, 0, 0);
            do {
                n.add(getFaceNormal(getEdgeFace(e1)));
                if(getEdgePair(e1) != -1) {
                    e1 = getEdgePair(e1);
                } else {
                    break;
                }
                e1 = getEdgeNext(e1);
            } while(e1 != e2);

            if(n.length() > 0.0000001) {
                n.normalize();
            }
        }
        for(int i = 0; i != getEdgeCount(); i++) {
            if(smooth) {
                getEdgeNormal(i).set(getVertexNormal(getEdgeVertex(i)));
            } else {
                getEdgeNormal(i).set(getFaceNormal(getEdgeFace(i)));
            }
        }
    }

    public void addBox(float px, float py, float pz, float sx, float sy, float sz, int xd, int yd, int zd, float units, boolean flip) throws Exception {
        Hashtable<String, Integer> v = new Hashtable<>();

        for(int x = 0; x != xd + 1; x++) {
            for(int y = 0; y != yd + 1; y++) {
                for(int z = 0; z != zd + 1; z++) {
                    if(x == 0 || x == xd || y == 0 || y == yd || z == 0 || z == zd) {
                        v.put(x + ":" + y + ":" + z, addVertex(px - sx / 2 + x / (float)xd * sx, py - sy / 2 + y / (float)yd * sy, pz - sz / 2 + z / (float)zd * sz));
                    }
                }
            }
        }

        for(int x = 0; x != xd; x++) {
            for(int y = 0; y != yd; y++) {
                for(int z = 0; z != zd; z++) {
                    if(x == 0) {
                        addFace(
                            flip,
                            v.get(x + ":" + y + ":" + z),
                            v.get(x + ":" + y + ":" + (z + 1)),
                            v.get(x + ":" + (y + 1) + ":" + (z + 1)),
                            v.get(x + ":" + (y + 1) + ":" + z)
                        );
                    }
                    if(x == xd - 1) {
                        addFace(
                            flip,
                            v.get((x + 1) + ":" + y + ":" + z),
                            v.get((x + 1) + ":" + (y + 1) + ":" + z),
                            v.get((x + 1) + ":" + (y + 1) + ":" + (z + 1)),
                            v.get((x + 1) + ":" + y + ":" + (z + 1))
                        );
                    }
                    if(y == 0) {
                        addFace(
                            flip,
                            v.get(x + ":" + y + ":" + z),
                            v.get((x + 1) + ":" + y + ":" + z),
                            v.get((x + 1) + ":" + y + ":" + (z + 1)),
                            v.get(x + ":" + y + ":" + (z + 1))
                        );
                    }
                    if(y == yd - 1) {
                        addFace(
                            flip,
                            v.get(x + ":" + (y + 1) + ":" + z),
                            v.get(x + ":" + (y + 1) + ":" + (z + 1)),
                            v.get((x + 1) + ":" + (y + 1) + ":" + (z + 1)),
                            v.get((x + 1) + ":" + (y + 1) + ":" + z)
                        );
                    }
                    if(z == 0) {
                        addFace(
                            flip,
                            v.get(x + ":" + y + ":" + z),
                            v.get(x + ":" + (y + 1) + ":" + z),
                            v.get((x + 1) + ":" + (y + 1) + ":" + z),
                            v.get((x + 1) + ":" + y + ":" + z)
                        );
                    }
                    if(z == zd - 1) {
                        addFace(
                            flip,
                            v.get(x + ":" + y + ":" + (z + 1)),
                            v.get((x + 1) + ":" + y + ":" + (z + 1)),
                            v.get((x + 1) + ":" + (y + 1) + ":" + (z + 1)),
                            v.get(x + ":" + (y + 1) + ":" + (z + 1))
                        );
                    }
                }
            }
        }
        calcNormals(false);
        calcTextureCoordinates(sx / 2, sy / 2, sz / 2, units);
    }

    public void smooth() {
        Vec3[] positions = new Vec3[getVertexCount()];
        float[] d = new float[getVertexCount()];

        for(int i = 0; i != getVertexCount(); i++) {
            int e1 = getVertexEdge(i);
            int e2 = e1;
            Vec3 position = new Vec3();
            int count1 = 0;

            do {
                int e = e1;
                int count2 = 0;
                Vec3 center = new Vec3();

                do {
                    center.add(getVertexPosition(getEdgeVertex(e)));
                    count2++;
                    e = getEdgeNext(e);
                } while(e != e1);

                center.div(count2);
                position.add(center);
                count1++;

                if(getEdgePair(e1) != -1) {
                    e1 = getEdgePair(e1);
                } else {
                    break;
                }
                e1 = getEdgeNext(e1);
            } while(e1 != e2);

            positions[i] = position.div(count1);
            d[i] = count1;
        }
        for(int i = 0; i != getVertexCount(); i++) {
            Vec3 position1 = getVertexPosition(i);
            Vec3 position2 = positions[i];
            
            position1.lerp(position2, 4 / d[i], position1);
        }
    }

    public Mesh build() {
        Vector<SceneVertex> meshVertices = new Vector<>();
        Vector<Integer> meshIndices = new Vector<>();
        Vector<int[]> meshPolygons = new Vector<>();

        for(int i = 0; i != getFaceCount(); i++) {
            int count = getFaceEdgeCount(i);
            int tris = count - 2;
            int e1 = getFaceEdge(i);
            int e2 = e1;
            int[] polygon = new int[count];
            int baseVertex = meshVertices.size();

            do {
                int j = e1 - e2;
                SceneVertex v = new SceneVertex();
                Vec3 position = getVertexPosition(getEdgeVertex(e1));

                v.position.set(position, 1);
                v.textureCoordinate.set(getEdgeTextureCoordinate(e1));
                v.normal.set(getEdgeNormal(e1));
                meshVertices.add(v);

                polygon[e1 - e2] = baseVertex + j;
                e1 = getEdgeNext(e1);
            } while(e1 != e2);

            meshPolygons.add(polygon);

            for(int j = 0; j !=  tris; j++) {
                meshIndices.add(baseVertex);
                meshIndices.add(baseVertex + j + 1);
                meshIndices.add(baseVertex + j + 2);
            }
        }

        int[] indices = new int[meshIndices.size()];
        int[][] polygons = new int[meshPolygons.size()][];

        for(int i = 0; i != indices.length; i++) {
            indices[i] = meshIndices.get(i);
        }
        for(int i = 0; i != polygons.length; i++) {
            polygons[i] = meshPolygons.get(i);
        }

        return new Mesh(meshVertices.toArray(new SceneVertex[meshVertices.size()]), indices, polygons);
    }
}
