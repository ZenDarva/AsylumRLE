package xyz.theasylum.zendarva.rle.component;

import xyz.theasylum.zendarva.rle.Tile;
import xyz.theasylum.zendarva.rle.palette.LayerPalette;
import xyz.theasylum.zendarva.rle.palette.Palette;
import xyz.theasylum.zendarva.rle.palette.PaletteManager;

import java.awt.*;

public class Layer extends Component {

    private LayerPalette palette;
    public Layer(Dimension dimension) {
        this(dimension,new Point(0,0));
    }



    public Layer(Dimension dimension, Point location, String paletteGroup){
        super(dimension);
        this.dimension = dimension;
        this.location = location;
        this.tileGrid=new TileGrid(dimension);
        palette= PaletteManager.getInstance().getPalette(paletteGroup,LayerPalette.class);
    }
    public Layer(Dimension dimension, Point location) {
        this(dimension,location,"default");
    }

    @Override
    public void update(Long time) {
        if (!isDirty){
            return;
        }

        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                setTileBackground(x,y,palette.getBackground());
                setTileForeground(x,y,palette.getForeground());
            }

        }
        isDirty=false;
    }

    @Override
    public Palette getPalette() {
        return palette;
    }

    public void setPalette(LayerPalette palette) {
        this.palette=palette;
    }

}
