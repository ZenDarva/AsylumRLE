package xyz.theasylum.zendarva.rle.component;

import java.awt.*;

public class Button extends Component {
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

    private void drawText(String text, Point location, TileGrid target){
        for (int x = 0; x < text.length(); x++) {
            target.setTileCharacter(x+location.x,location.y,text.toCharArray()[x]);
        }
    }
}
