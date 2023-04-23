package org.j3d.sample;

import org.j3d.Entity;

import java.util.Random;

import org.j3d.*;

public class FireLight extends Entity {

    private ParticleSystem particles = new ParticleSystem(25);
    private Particle particle = new Particle();
    private Random random = new Random(1000);
    
    @Override
    public void init() throws Exception {
        Node node = new Node();

        node.position.set(position);
        node.renderable = particles;
        node.texture = assets().load(IO.file("assets/maps/particle.png"));
        node.blendEnabled = true;
        node.additiveBlend = true;
        node.depthWriteEnabled = false;
        node.zOrder = 1;

        scene().root.add(node);
    }

    @Override
    public void update() {
        float v1 = 0.5f + random.nextFloat() * 0.5f;
        float v2 = 0.1f + random.nextFloat() * 0.1f;
        float v3 = 16 + random.nextFloat() * 32;
        float v4 = 2 + random.nextFloat() * 4;
        particles.emitPosition.set(0, (float)Math.sin(game().totalTime() * 2) * 25, 0);
        particle.velocityX = -8 + random.nextFloat() * 16;
        particle.velocityY = -8 + random.nextFloat() * 16;
        particle.velocityZ = -8 + random.nextFloat() * 16;
        particle.startR = particle.startG = particle.startB = v1;
        particle.endR = particle.endG = particle.endB = v2;
        particle.startX = particle.startY = v3;
        particle.endX = particle.endY = v4;
        particle.lifeSpan = 0.5f + random.nextFloat() * 1.5f;
        particles.emit(particle, game());
    }
}
