package org.j3d.demo;

import java.util.Random;

import org.j3d.IO;
import org.j3d.NodeComponent;
import org.j3d.Particle;
import org.j3d.ParticleSystem;

public class FireLight extends NodeComponent {
    
    private Particle particle = new Particle();
    private Random random = new Random(1000);

    @Override
    public void init() throws Exception {
        node().renderable = new ParticleSystem(25);
        node().blendEnabled = true;
        node().additiveBlend = true;
        node().depthWriteEnabled = false;
        node().zOrder = 100;
        node().texture = game().assets().load(IO.file("assets/particle.png"));
    }

    @Override
    public void update() throws Exception {
        float v1 = 0.5f + random.nextFloat() * 0.5f;
        float v2 = 0.1f + random.nextFloat() * 0.1f;
        float v3 = 16 + random.nextFloat() * 32;
        float v4 = 2 + random.nextFloat() * 4;
        ParticleSystem particles = node().getParticleSystem();
        
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
