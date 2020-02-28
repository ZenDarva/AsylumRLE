package xyz.theasylum.zendarva.rle.utility;

import java.awt.*;

public class PointUtilities {
    public static Point makeRelative(Point targ, Point to){
        return new Point(targ.x-to.x,targ.y-to.y);
    }
}
