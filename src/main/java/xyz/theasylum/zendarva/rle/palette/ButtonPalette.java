package xyz.theasylum.zendarva.rle.palette;

import lombok.Getter;
import lombok.Setter;
import xyz.theasylum.zendarva.rle.component.Button;

import java.awt.*;

public class ButtonPalette extends Palette {
    @Getter @Setter Color hoverForeground;
    @Getter @Setter Color hoverBackground;
}
