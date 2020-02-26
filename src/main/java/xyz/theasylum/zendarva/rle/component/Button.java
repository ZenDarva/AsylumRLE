package xyz.theasylum.zendarva.rle.component;

import xyz.theasylum.zendarva.rle.event.EventProcedure;

import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Button extends Component {
    private BiConsumer<Point, Integer> procedure;

    public Button(Dimension dimension, String text) {
        super(dimension);
        for (int x = 0; x < dimension.width; x++) {
            for (int y = 0; y < dimension.height; y++) {
                this.setTileBackground(x,y,Color.GRAY);
                this.setTileForeground(x,y,Color.white);
            }
        }
        drawText(text,new Point(dimension.width/2 - text.length()/2,dimension.height/2),this);
    }

    public void setOnClick(BiConsumer<Point, Integer> procedure){
        this.procedure = procedure;
    }

    private void drawText(String text, Point location, TileGrid target){
        for (int x = 0; x < text.length(); x++) {
            target.setTileCharacter(x+location.x,location.y,text.toCharArray()[x]);
        }
    }

    @Override
    public boolean mouseClicked(Point position, int button) {
        boolean handled =  super.mouseClicked(position, button);
        if (handled==false){
            procedure.accept(position, button);
            return true;
        }
        return handled;
    }

    @Override
    public void mouseEntered() {
        for (int x = 0; x < dimension.width; x++) {
            for (int y = 0; y < dimension.height; y++) {
                this.setTileBackground(x,y,Color.lightGray);
                this.setTileForeground(x,y,Color.BLACK);
            }
        }
    }

    @Override
    public void mouseLeft() {
        for (int x = 0; x < dimension.width; x++) {
            for (int y = 0; y < dimension.height; y++) {
                this.setTileBackground(x,y,Color.GRAY);
                this.setTileForeground(x,y,Color.white);
            }
        }
    }
}
