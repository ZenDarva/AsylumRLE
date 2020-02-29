package xyz.theasylum.zendarva.rle.palette;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;

public class ListBoxPalette extends Palette {

    @Getter @Setter Color textForeground;
    @Getter @Setter Color textBackground;
    @Getter @Setter Color selectionBackground;
    @Getter @Setter Color selectionForeground;

}
