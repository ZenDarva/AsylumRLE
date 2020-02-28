package xyz.theasylum.zendarva.rle.component;

import org.omg.CORBA.WStringValueHelper;
import xyz.theasylum.zendarva.rle.palette.NumberSpinnerPalette;
import xyz.theasylum.zendarva.rle.palette.Palette;
import xyz.theasylum.zendarva.rle.palette.PaletteManager;

import java.awt.*;

public class NumberSpinner extends Component {

    private int max;
    private int min;
    private int value;
    private NumberSpinnerPalette palette;

    //▲▼
    public NumberSpinner(Integer max, int min, int value) {
        super(new Dimension(max.toString().length(), 3));
        this.value = value;

        palette= PaletteManager.getInstance().getPalette("default",NumberSpinnerPalette.class);

        this.max = max;
        this.min = min;
        String strValue = String.valueOf(value);
        for (int y = 0; y < dimension.height; y++) {
            for (int x = 0; x < dimension.width; x++) {
                if (y == 0) {
                    setTileCharacter(x, y, '▲');
                    setTileForeground(x, y, palette.getArrowForeground());
                    setTileBackground(x, y, palette.getArrowBackground());
                }
                if (y == 2) {
                    setTileCharacter(x, y, '▼');
                    setTileForeground(x, y, palette.getArrowForeground());
                    setTileBackground(x, y, palette.getArrowBackground());
                }
                if (y == 1) {
                    setTileCharacter(x, y, strValue.charAt(x));
                    setTileForeground(x, y, palette.getForeground());
                    setTileBackground(x, y, palette.getBackground());
                }
            }
        }
    }

    @Override
    public void update(Long time) {
        if (!isDirty)
            return;
        int maxLength = String.valueOf(max).length();
        String formatString = "%0" + maxLength + "d";
        String strValue = String.format(formatString, value);
        for (int y = 0; y < dimension.height; y++) {
            for (int x = 0; x < dimension.width; x++) {
                if (y == 0) {
                    setTileCharacter(x, y, '▲');
                    setTileForeground(x, y, palette.getArrowForeground());
                    setTileBackground(x, y, palette.getArrowBackground());
                }
                if (y == 2) {
                    setTileCharacter(x, y, '▼');
                    setTileForeground(x, y, palette.getArrowForeground());
                    setTileBackground(x, y, palette.getArrowBackground());
                }
                if (y == 1) {
                    setTileCharacter(x, y, strValue.charAt(x));
                    setTileForeground(x, y, palette.getForeground());
                    setTileBackground(x, y, palette.getBackground());
                }
            }
        }
        isDirty=false;
    }

    @Override
    public boolean mouseClicked(Point position, int button) {

        if (position.y == 1)
            return false;
        if (super.mouseClicked(position, button))
            return true;
        int place = dimension.width - position.x;

        int toAdd = 1;
        for (int i = 1; i < place; i++) {
            toAdd *= 10;
        }

        if (position.y == 0)
            value = Math.min(value + toAdd, max);
        else
            value = Math.max(value - toAdd, min);
        setDirty();
        return true;
    }
}
