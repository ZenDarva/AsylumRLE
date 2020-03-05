package xyz.theasylum.zendarva.rle.utility;

import xyz.theasylum.zendarva.rle.Font;
import xyz.theasylum.zendarva.rle.Tile;
import xyz.theasylum.zendarva.rle.component.TileGrid;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;

public class GridImager<T> extends TileGrid {
    public GridImager(T[][] map, Function<T, Tile> converter) {
        super(new Dimension(map.length,map[0].length));
        for (int x = 0; x < dimension.width; x++) {
            for (int y = 0; y < dimension.height; y++) {
                getTiles()[x][y]=converter.apply(map[x][y]);
            }
        }
    }

    public void Output(File file, Font font){
        BufferedImage image = new BufferedImage(dimension.width*font.getCharWidth(),dimension.height*font.getCharHeight(), BufferedImage.TYPE_4BYTE_ABGR);

        Graphics2D g = image.createGraphics();

        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int lX = x*font.getCharWidth();
                int lY = y*font.getCharHeight();
                font.draw(getTiles()[x][y], lX, lY, g);
            }
        }

        g.dispose();

        try {
            ImageIO.write(image,"PNG",file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
