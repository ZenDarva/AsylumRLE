package xyz.theasylum.zendarva.rle.component;

import lombok.Setter;
import xyz.theasylum.zendarva.rle.palette.ListBoxPalette;
import xyz.theasylum.zendarva.rle.palette.Palette;
import xyz.theasylum.zendarva.rle.palette.PaletteManager;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class ListBox extends Component {
    List<String> entries;
    int selected = -1;
    int drawOffset = 0;
    @Setter
    Consumer<String> onSelectionChanged;
    ListBoxPalette palette;

    public ListBox(Dimension dimension) {
        super(dimension);
        entries = new LinkedList<>();
        palette = PaletteManager.getInstance().getPalette("default", ListBoxPalette.class);
    }


    @Override
    public void update(Long time) {
        if (!isDirty)
            return;
        for (int x = 0; x < dimension.width; x++) {
            for (int y = 0; y < dimension.height; y++) {
                setTileBackground(x, y, palette.getTextBackground());
                setTileForeground(x, y, palette.getTextForeground());
                setTileCharacter(x,y,' ');
            }
        }
        for (int x = 0; x < dimension.width; x++) {
            setTileCharacter(x, 0, '█');
            setTileBackground(x, 0, palette.getBackground());
            setTileForeground(x, 0, palette.getForeground());

            setTileCharacter(x, dimension.height - 1, '█');
            setTileBackground(x, dimension.height - 1, palette.getBackground());
            setTileForeground(x, dimension.height - 1, palette.getForeground());
        }

        for (int y = 0; y < dimension.height; y++) {
            setTileCharacter(0, y, '█');
            setTileBackground(0, y, palette.getBackground());
            setTileForeground(0, y, palette.getForeground());

            setTileCharacter(dimension.width - 1, y, '█');
            setTileBackground(dimension.width - 1, y, palette.getBackground());
            setTileForeground(dimension.width - 1, y, palette.getForeground());
        }

        for (int i = 1; i < Math.min(dimension.height - 1, entries.size() + 1); i++) {
            drawText(1, i, entries.get(i - 1+drawOffset), palette.getTextForeground(), palette.getTextBackground(), dimension.width - 2);
        }

        if (selected != -1 && selected >= drawOffset) {
            int y = selected - drawOffset +1;
            for (int x = 1; x < dimension.width - 1; x++) {
                setTileForeground(x, y, palette.getSelectionForeground());
                setTileBackground(x, y, palette.getSelectionBackground());
            }
        }

        if (entries.size() > dimension.height -2){
            //Scrolling!
            setTileCharacter(dimension.width-1,0,'▲');
            setTileForeground(dimension.width-1,0,palette.getArrowForeground());
            setTileBackground(dimension.width-1,0,palette.getArrowBackground());

            setTileCharacter(dimension.width-1,dimension.height-1,'▼');
            setTileForeground(dimension.width-1,dimension.height-1,palette.getArrowForeground());
            setTileBackground(dimension.width-1,dimension.height-1,palette.getArrowBackground());

        }
        isDirty = false;
    }

    @Override
    public boolean mouseClicked(Point position, int button) {
        if (super.mouseClicked(position, button))
            return true;

        if (entries.size() > dimension.height){
            if (position.x == dimension.width-1 && position.y==0) { //Up arrow.
                drawOffset = Math.max(drawOffset-1,0);
                this.setDirty();
                return true;
            }
            if (position.x == dimension.width-1 && position.y==dimension.height-1) { //Down arrow.
                drawOffset = Math.min(drawOffset+1,entries.size()-(dimension.height-2));
                this.setDirty();
                return true;
            }
        }


        if (position.y ==0 || position.y == dimension.height-1 ||
        position.x == 0 || position.x ==dimension.width-1){
            return true;
        }

        int possiblySelected = drawOffset + position.y;
        if (entries.size() + 1 <= possiblySelected) {
            selected = -1;
            this.setDirty();
            if (onSelectionChanged != null)
                onSelectionChanged.accept(null);
            return true;
        }
        selected = drawOffset + position.y -1;
        if (onSelectionChanged != null)
            onSelectionChanged.accept(entries.get(selected));
        this.setDirty();
        return true;
    }

    public void addEntry(String str) {
        entries.add(str);
        this.setDirty();
    }
    public void clear(){
        entries.clear();
        selected=-1;
        if (onSelectionChanged != null)
            onSelectionChanged.accept(null);
        this.setDirty();
    }

    @Override
    public Palette getPalette() {
        return palette;
    }
    public String getSelected(){
        if (selected ==-1){
            return null;
        }
        return entries.get(selected);
    }

}
