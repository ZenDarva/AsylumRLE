package xyz.theasylum.zendarva.rle.component;

import lombok.Getter;
import xyz.theasylum.zendarva.rle.Tile;

import java.awt.*;
import java.util.Collections;
import java.util.Map;

public class TileGrid {
    private Tile[][] tiles;
    protected Dimension dimension;

    @Getter private boolean changed = true;

    public TileGrid(Dimension dimension){

        this.dimension = dimension;
        tiles = new Tile[dimension.width][dimension.height];
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                tiles[x][y]= new Tile();
            }

        }
    }

    public void setTileBackground(int x, int y, Color color){
        tiles[x][y].setBackground(color);
    }

    public void setTileForeground(int x, int y, Color color){
        tiles[x][y].setForeground(color);
    }

    public void setTileCharacter(int x, int y, char ch){
        if (x<0 || x > dimension.width || y < 0 || y > dimension.height)
            return;
        tiles[x][y].setCharacter(ch);
    }
    public char getTileCharacter(int x, int y){
        if (x<0 || x > dimension.width || y < 0 || y > dimension.height)
            return (char) -1;
        return tiles[x][y].getCharacter();
    }

    public int getWidth(){
        return dimension.width;
    }
    public int getHeight(){
        return dimension.height;
    }

    public Tile[][] getTiles(){
        changed = false;
        return tiles;
    }

    public Map<Point, Tile> getExtras(){return Collections.emptyMap();}
}
