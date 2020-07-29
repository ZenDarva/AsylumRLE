package xyz.theasylum.zendarva.rle.particle;

import xyz.theasylum.zendarva.rle.utility.PointF;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ParticleManager {
    private List<Particle> deadParticles = new LinkedList<>();
    private List<Particle> liveParticles = new LinkedList<>();

    public List<Particle> getLiveParticles() {
        return Collections.unmodifiableList(liveParticles);
    }

    public void spawnParticle(Point location, PointF velocity, int lifespan, Color color, boolean stopTurn){
        PointF loc = new PointF(location);
        if (deadParticles.isEmpty()) {
            Particle particle = new Particle(loc, velocity, lifespan, color, stopTurn);
            liveParticles.add(particle);
        } else {
            Particle particle = deadParticles.remove(0);
            particle.reUse(loc, velocity, lifespan, color, stopTurn);
            liveParticles.add(particle);
        }
    }

    public void spawnParticle(Point location, PointF velocity, Color color, boolean stopTurn) {
        spawnParticle(location,velocity,10000,color, stopTurn);
    }

    public void update(Long time) {
        List<Particle> toRemove = new LinkedList<>();
        for (Particle liveParticle : liveParticles) {
            liveParticle.lifeSpan -= 1;

            if (liveParticle.getLifeSpan() <= 0) {
                toRemove.add(liveParticle);
            }
            liveParticle.getLoc().move(liveParticle.velocity.x, liveParticle.velocity.y);
        }
        deadParticles.addAll(toRemove);
        liveParticles.removeAll(toRemove);
    }
}
