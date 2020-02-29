package xyz.theasylum.zendarva.rle.component;

import xyz.theasylum.zendarva.rle.palette.OptionButtonPalette;
import xyz.theasylum.zendarva.rle.palette.Palette;
import xyz.theasylum.zendarva.rle.palette.PaletteManager;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class OptionButton extends Component {
    private final String text;
    private boolean checked;
    private List<OptionButton> peers;
    private OptionButtonPalette palette;
    //○●
    public OptionButton(Dimension dimension, String text) {
        super(dimension);
        this.text = text;
        peers = new LinkedList<>();
        palette= PaletteManager.getInstance().getPalette("default",OptionButtonPalette.class);
    }

    @Override
    public void update(Long time) {
        setTileForeground(0,0,palette.getBoxForeground());
        setTileBackground(0,0,palette.getBoxBackground());
        if (checked)
            setTileCharacter(0, 0, '●');
        else
            setTileCharacter(0,0,'○');
        for (int i= 1; i<dimension.width;i++){
            setTileForeground(i,0,palette.getForeground());
            setTileBackground(i,0,palette.getBackground());
        }
        for (int i = 0; i < text.length(); i++) {
            setTileCharacter(i+2,0,text.charAt(i));
        }
    }

    //Todo: Improve this.
    public void addPeer(OptionButton button) {
        if (!peers.contains(button)) {
            peers.add(button);
            button.addPeer(this);
        }
    }

    @Override
    public boolean mouseClicked(Point position, int button) {
         if (super.mouseClicked(position, button)){
             return true;
         }
         setChecked(true);
         return true;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        if(checked) {
            peers.forEach(f -> f.setChecked(false));
            setTileCharacter(0, 0, '●');
        }
        else {
            setTileCharacter(0,0,'○');
        }
        this.checked = checked;
        if (onChanged !=null){
            onChanged.accept(this);
        }
    }

    @Override
    public Palette getPalette() {
        return palette;
    }
}
