package xyz.theasylum.zendarva.rle.palette;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.theasylum.zendarva.rle.palette.serialization.PaletteDeserializer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

    public void buildNewPalette(String name) {

        HashMap<String, Palette> newPalette= new HashMap<>();
        paletteData.put(name, newPalette);

        ButtonPalette bPalette = new ButtonPalette();
        bPalette.background = Color.GRAY;
        bPalette.foreground = Color.WHITE;
        bPalette.hoverBackground=Color.lightGray;
        bPalette.hoverForeground=Color.BLACK;
        newPalette.put(bPalette.getClass().getName(),bPalette);

        NumberSpinnerPalette nsp = new NumberSpinnerPalette();
        nsp.foreground=Color.GREEN;
        nsp.background=Color.black;
        nsp.arrowForeground =Color.darkGray;
        nsp.arrowBackground =new Color(0,0,0,0);
        newPalette.put(nsp.getClass().getName(),nsp);

        LayerPalette lp = new LayerPalette();
        lp.background=Color.black;
        lp.foreground=Color.GREEN;
        newPalette.put(lp.getClass().getName(),lp);

        ListBoxPalette lbp = new ListBoxPalette();
        lbp.foreground=Color.GREEN;
        lbp.background=new Color(0,0,0,0);
        lbp.textForeground=Color.GREEN;
        lbp.textBackground=Color.BLACK;
        lbp.selectionForeground=Color.BLACK;
        lbp.selectionBackground=Color.GREEN.darker();
        lbp.arrowBackground=Color.GREEN;
        lbp.arrowForeground=Color.BLACK;
        newPalette.put(lbp.getClass().getName(),lbp);

        OptionButtonPalette obp = new OptionButtonPalette();
        obp.foreground=Color.GREEN;
        obp.background=Color.BLACK;
        obp.boxBackground=Color.BLACK;
        obp.boxForeground=Color.GREEN;
        newPalette.put(obp.getClass().getName(), obp);

        TextEntryPalette tep = new TextEntryPalette();
        tep.caretBackground= new Color(0,128,0,128);
        tep.caretForeground=Color.black;
        tep.foreground=Color.BLACK;
        tep.background=Color.GREEN.darker();
        newPalette.put(tep.getClass().getName(),tep);

    }

    public static PaletteManager getInstance(){
        if (myInstance == null){
            myInstance=new PaletteManager();
        }
        return myInstance;
    }

    public <T extends Palette> T getPalette(String palette, Class<T> targClass){
        String targName=targClass.getName();
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

    public boolean loadPalette(String name){
        if (!loadPaletteResource(name))
            return loadPaletteFile(name);
        return true;
    }

    private boolean loadPaletteResource(String name){
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("Palette/"+name+".json");
        if (stream == null){
            LOG.info("Failed to load palette '{}' from jar, trying file.");
            return false;
        }
        ObjectMapper mapper = PaletteDeserializer.getDeserializer();
        TypeReference<HashMap<String,Palette>> typeRef = new TypeReference<HashMap<String, Palette>>() {};
        try {
            Object newPaletteData = mapper.readValue(stream,typeRef);
            paletteData.put(name, (Map<String, Palette>) newPaletteData);
            return true;
        } catch (IOException e) {
            LOG.error("Error when loading palette {} from jar: {}",name, e);
            return false;
        }
    }

    private boolean loadPaletteFile(String name){
        String curDir = System.getProperty("user.dir");

        File dataDir = new File(curDir,"data");

        if (!dataDir.exists()){
            LOG.error("Data directory missing when attempting to load palette data.");
            return false;
        }

        File inFile = new File(dataDir,name+".json");

        TypeReference<HashMap<String,Palette>> typeRef = new TypeReference<HashMap<String, Palette>>() {};

        try {
            ObjectMapper mapper = PaletteDeserializer.getDeserializer();
            Object newPaletteData = mapper.readValue(inFile,typeRef);
            paletteData.put(name, (Map<String, Palette>) newPaletteData);
            return true;
        } catch (IOException e) {
            LOG.error("Error loading palette data '{}. {}",name,e);
            return false;
        }
    }

    private String stripPaletteName(String name){
        String result=name;
        if (result.contains("/")){
            int index = result.lastIndexOf('/');
            result = result.substring(index+1);
        }
        result.replaceAll("\\.[Jj][Ss][oO][Nn]","");
        return result;
    }
}
