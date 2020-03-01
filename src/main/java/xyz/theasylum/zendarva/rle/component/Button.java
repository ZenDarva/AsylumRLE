package xyz.theasylum.zendarva.rle.component;

import xyz.theasylum.zendarva.rle.palette.ButtonPalette;
import xyz.theasylum.zendarva.rle.palette.Palette;
import xyz.theasylum.zendarva.rle.palette.PaletteManager;

import java.awt.*;
import java.util.function.BiConsumer;

public class Button extends Component {
    private final String text;
    private BiConsumer<Point, Integer> procedure;
    ButtonPalette palette;
    private String paletteGroup;

    public Button(Dimension dimension, String text, String paletteGroup) {
        super(dimension);
        this.text = text;
        palette= PaletteManager.getInstance().getPalette(paletteGroup,ButtonPalette.class);
        this.paletteGroup = paletteGroup;
        for (int x = 0; x < dimension.width; x++) {
            for (int y = 0; y < dimension.height; y++) {
                this.setTileBackground(x,y,palette.getBackground());
                this.setTileForeground(x,y,palette.getForeground());
            }
        }
        drawText(text,new Point(dimension.width/2 - text.length()/2,dimension.height/2),this);
    }
    public Button(Dimension dimension, String text){
        this(dimension,text,"default");
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
        //Hack!
        palette= PaletteManager.getInstance().getPalette(paletteGroup,ButtonPalette.class);
        for (int x = 0; x < dimension.width; x++) {
            for (int y = 0; y < dimension.height; y++) {
                this.setTileBackground(x,y,palette.getHoverBackground());
                this.setTileForeground(x,y,palette.getHoverForeground());
            }
        }
    }

    @Override
    public void mouseLeft() {
        //Hack!
        palette= PaletteManager.getInstance().getPalette(paletteGroup,ButtonPalette.class);
        for (int x = 0; x < dimension.width; x++) {
            for (int y = 0; y < dimension.height; y++) {
                this.setTileBackground(x,y,palette.getBackground());
                this.setTileForeground(x,y,palette.getForeground());
            }
        }
    }

    @Override
    public Palette getPalette() {
        return palette;
    }

    @Override
    public void update(Long time) {
        if (!isDirty)
            return;
        palette= PaletteManager.getInstance().getPalette(paletteGroup,ButtonPalette.class);
        for (int x = 0; x < dimension.width; x++) {
            for (int y = 0; y < dimension.height; y++) {
                this.setTileBackground(x,y,palette.getBackground());
                this.setTileForeground(x,y,palette.getForeground());
            }
        }
        drawText(text,new Point(dimension.width/2 - text.length()/2,dimension.height/2),this);
        isDirty=false;
    }
}
