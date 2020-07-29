package xyz.theasylum.zendarva.rle.particle;

import lombok.Data;
import xyz.theasylum.zendarva.rle.utility.PointF;

import java.awt.*;

@Data
public class Particle {

    PointF loc;
    PointF velocity;
    int lifeSpan;
    Color color;
    boolean live;
    boolean stopTurn;

    protected Particle(PointF loc, PointF velocity, int lifeSpan, Color color, boolean stopTurn) {
        this.loc = loc;
        this.velocity = velocity;
        this.lifeSpan = lifeSpan;
        this.color = color;
        this.stopTurn = stopTurn;
    }
    protected void reUse(PointF loc, PointF velocity, int lifeSpan, Color color, boolean stopTurn){
        this.loc = loc;
        this.velocity = velocity;
        this.lifeSpan = lifeSpan;
        this.color = color;
        this.stopTurn = stopTurn;
    }

}
