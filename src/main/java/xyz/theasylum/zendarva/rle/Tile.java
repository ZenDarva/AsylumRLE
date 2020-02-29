package xyz.theasylum.zendarva.rle;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@EqualsAndHashCode
public class Tile {



    @Getter @Setter private char character =' ';
    @Getter @Setter private Color foreground = Color.red;
    @Getter @Setter private Color background = Color.BLUE;


    public static abstract class TileTransform{
        public abstract Color transform(Color color);
    }

    public static TileTransform darken = new TileTransform(){

        @Override
        public Color transform(Color color) {
            return color.darker().darker();
        }
    };
    public static TileTransform none = new TileTransform(){

        @Override
        public Color transform(Color color) {
            return color;
        }
    };
}
