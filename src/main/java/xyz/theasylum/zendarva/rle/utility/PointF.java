package xyz.theasylum.zendarva.rle.utility;

import java.awt.*;

public class PointF {
    public float x,y;

    public PointF(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public PointF(Point point) {
        x=point.x;
        y=point.y;
    }

    public void move(float x, float y){
        this.x+=x;
        this.y+=y;
    }
    public void move(PointF point){
        move(point.x,point.y);
    }

    public Point toPoint(){
        return new Point((int)x,(int)y);
    }
}
