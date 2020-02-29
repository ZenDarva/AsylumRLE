package xyz.theasylum.zendarva.rle.palette;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PaletteManager {

    protected static final Logger LOG= LogManager.getLogger(PaletteManager.class);
    private Map<String, Map<String, Palette>> paletteData;

    private static PaletteManager myInstance;

    private PaletteManager(){
        paletteData= new HashMap();
        buildNewPalette("default");
    }

    private void buildNewPalette(String name) {

        HashMap<String, Palette> newPalette= new HashMap<>();
        paletteData.put(name, newPalette);

        ButtonPalette bPalette = new ButtonPalette();
        bPalette.background = Color.GRAY;
        bPalette.foreground = Color.WHITE;
        bPalette.hoverBackground=Color.lightGray;
        bPalette.hoverForeground=Color.BLACK;
        newPalette.put(bPalette.getClass().getSimpleName(),bPalette);

        NumberSpinnerPalette nsp = new NumberSpinnerPalette();
        nsp.foreground=Color.GREEN;
        nsp.background=Color.black;
        nsp.ArrowForeground=Color.darkGray;
        nsp.ArrowBackground=new Color(0,0,0,0);
        newPalette.put(nsp.getClass().getSimpleName(),nsp);

        LayerPalette lp = new LayerPalette();
        lp.background=Color.black;
        lp.foreground=Color.GREEN;
        newPalette.put(lp.getClass().getSimpleName(),lp);

        ListBoxPalette lbp = new ListBoxPalette();
        lbp.foreground=Color.GREEN;
        lbp.background=new Color(0,0,0,0);
        lbp.textForeground=Color.GREEN;
        lbp.textBackground=Color.BLACK;
        lbp.selectionForeground=Color.BLACK;
        lbp.selectionBackground=Color.GREEN.darker();
        newPalette.put(lbp.getClass().getSimpleName(),lbp);
    }

    public static PaletteManager getInstance(){
        if (myInstance == null){
            myInstance=new PaletteManager();
        }
        return myInstance;
    }

    public <T extends Palette> T getPalette(String palette, Class<T> targClass){
        String targName=targClass.getSimpleName();
        if (!paletteData.containsKey(palette)){
            LOG.error("Request for nonexistant palette: {}",palette);
            return null;
        }
        if (!paletteData.get(palette).containsKey(targName)){
            LOG.error("Request for nonexistant palette object '{}' in palette '{}'",palette);
            return null;
        }
        return (T) paletteData.get(palette).get(targName);

    }
    public Map<String, Palette> getPalette(String palette){
        if (!paletteData.containsKey(palette)){
            LOG.error("Request for nonexistant palette: {}",palette);
            return null;
        }
        return paletteData.get(palette);
    }
}
