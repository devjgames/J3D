package org.j3d;

public interface TriangleVisitor {
    
    void visit(Triangle triangle, Collidable collidable);
}
