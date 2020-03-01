package xyz.theasylum.zendarva.rle.utility;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import xyz.theasylum.zendarva.rle.Screen;
import xyz.theasylum.zendarva.rle.component.Button;
import xyz.theasylum.zendarva.rle.component.Component;
import xyz.theasylum.zendarva.rle.component.*;
import xyz.theasylum.zendarva.rle.palette.LayerPalette;
import xyz.theasylum.zendarva.rle.palette.Palette;
import xyz.theasylum.zendarva.rle.palette.PaletteManager;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public class PaletteEditor {
    static NumberSpinner redNS;
    static NumberSpinner greenNS;
    static NumberSpinner blueNS;
    static NumberSpinner alphaNS;
    static Layer backLayer;
    static Layer controlLayer;
    static ListBox controlListBox;
    static ListBox paletteDetailListBox;
    static Component componentEditing;
    static Button saveButton;
    static Button loadButton;
    static Button resetButton;
    static Button newButton;
    static String paletteEditing= "default";
    static Screen screen;

    public static void main(String[] args){


        screen = new Screen(new Dimension(76,18), PaletteEditor::processTick);
        backLayer = new Layer(new Dimension(76,18), new Point(0,0));
        controlLayer = new Layer(new Dimension(30,14),new Point(0,0));

        for (int x = 0; x < controlLayer.getWidth(); x++) {
            controlLayer.setTileCharacter(x,0,'█');
            controlLayer.setTileCharacter(x,13,'█');
        }

        for (int y = 0; y < controlLayer.getHeight(); y++) {
            controlLayer.setTileCharacter(0,y,'█');
            controlLayer.setTileCharacter(29,y,'█');
        }
        backLayer.addComponent(controlLayer);


        redNS = new NumberSpinner(255,0,0);
        redNS.setLocation(new Point(2,15));
        redNS.setOnChanged(PaletteEditor::onNumberSpinnerChange);


        greenNS = new NumberSpinner(255,0,0);
        greenNS.setLocation(new Point(6,15));
        greenNS.setOnChanged(PaletteEditor::onNumberSpinnerChange);

        blueNS = new NumberSpinner(255,0,0);
        blueNS.setLocation(new Point(10,15));
        blueNS.setOnChanged(PaletteEditor::onNumberSpinnerChange);

        alphaNS = new NumberSpinner(255,0,255);
        alphaNS.setLocation(new Point(14,15));
        alphaNS.setOnChanged(PaletteEditor::onNumberSpinnerChange);

        backLayer.addComponent(redNS);
        backLayer.addComponent(blueNS);
        backLayer.addComponent(greenNS);
        backLayer.addComponent(alphaNS);


        backLayer.addComponent(createPaletteEntryList());

        paletteDetailListBox = new ListBox(new Dimension(22,13));
        paletteDetailListBox.setLocation(new Point(54,5));
        paletteDetailListBox.setVisible(false);
        paletteDetailListBox.setOnSelectionChanged(PaletteEditor::onDetailListSelectionChanged);
        backLayer.addComponent(paletteDetailListBox);

        saveButton = new Button(new Dimension(10,3),"Save");
        saveButton.setLocation(new Point(66,1));
        saveButton.setOnClick(PaletteEditor::onSavePressed);
        backLayer.addComponent(saveButton);

        resetButton = new Button(new Dimension(10,3),"Reset");
        resetButton.setLocation(new Point(42,1));
        resetButton.setOnClick(PaletteEditor::OnResetPressed);
        backLayer.addComponent(resetButton);

        newButton = new Button(new Dimension(10,3),"New");
        newButton.setLocation(new Point(31,1));
        newButton.setOnClick(PaletteEditor::OnNewPressed);
        backLayer.addComponent(newButton);

        loadButton = new Button(new Dimension(10,3),"Load");
        loadButton.setLocation(new Point(55,1));
        loadButton.setOnClick(PaletteEditor::OnLoadPressed);
        backLayer.addComponent(loadButton);

        screen.addComponent(backLayer);
        screen.startEngine();
    }



    public static void processTick(Long time){

    }

    private static ListBox createPaletteEntryList(){

        controlListBox = new ListBox(new Dimension(22,13));
        controlListBox.setLocation(new Point(31, 5));
        controlListBox.addEntry("Button");
        controlListBox.addEntry("Layer");
        controlListBox.addEntry("ListBox");
        controlListBox.addEntry("NumberSpinner");
        controlListBox.addEntry("OptionButton");
        controlListBox.addEntry("TextEntry");
        controlListBox.setOnSelectionChanged(PaletteEditor::onControlListBoxSelectionChanged);
        return controlListBox;
    }

    private static void configureDetailList(Palette palette){
        paletteDetailListBox.setVisible(true);
        for (Field declaredField : palette.getClass().getSuperclass().getDeclaredFields()) {
            paletteDetailListBox.addEntry(declaredField.getName());
        }

        for (Field declaredField : palette.getClass().getDeclaredFields()) {
            paletteDetailListBox.addEntry(declaredField.getName());
        }

    }



    public static void onNumberSpinnerChange(Component ns){
        Color newColor = new Color(redNS.getValue(),greenNS.getValue(),blueNS.getValue(),alphaNS.getValue());
        for (int x = 18; x < 30; x++) {
            for (int y = 15; y < 18; y++) {
                backLayer.setTileBackground(x,y,newColor);
            }
        }

        if (componentEditing != null && paletteDetailListBox.getSelected() != null){
            Field field = getField(componentEditing.getPalette().getClass(),paletteDetailListBox.getSelected());
            if (field == null)
                return;
            field.setAccessible(true);
            Field targField = getField(componentEditing.getClass(),"isDirty");
            try {
                field.set(componentEditing.getPalette(),newColor);
                targField.setAccessible(true);
                targField.set(componentEditing,true);

            } catch (IllegalAccessException e) {

            }
        }

    }

    public static void onControlListBoxSelectionChanged(String entry){
        if (componentEditing != null && componentEditing != controlLayer){
            controlLayer.removeComponent(componentEditing);
            paletteDetailListBox.setVisible(false);
            paletteDetailListBox.clear();
        }
        if (entry == null){
            return;
        }
        switch (entry){
            case "Button":
                Button button = new Button(new Dimension(9,3), "EXAMPLE",paletteEditing);
                button.setLocation(new Point (controlLayer.getWidth()/2 - 4,controlLayer.getHeight()/2-2));
                componentEditing=button;
                controlLayer.addComponent(button);
                configureDetailList(button.getPalette());
                break;
            case "Layer":
                componentEditing=controlLayer;
                configureDetailList(controlLayer.getPalette());
                controlLayer.setPalette(PaletteManager.getInstance().getPalette(paletteEditing, LayerPalette.class));
                break;
            case "ListBox":
                ListBox lb = new ListBox(new Dimension(10,6),paletteEditing);
                lb.setLocation(new Point (controlLayer.getWidth()/2 - 10,controlLayer.getHeight()/2-3));
                componentEditing=lb;
                controlLayer.addComponent(lb);
                lb.addEntry("Sample1");
                lb.addEntry("Sample2");
                lb.addEntry("Sample Three");
                configureDetailList(lb.getPalette());
                break;
            case "NumberSpinner":
                NumberSpinner ns = new NumberSpinner(2000,0,128,paletteEditing);
                ns.setLocation(new Point (controlLayer.getWidth()/2 - 2,controlLayer.getHeight()/2-3));
                componentEditing=ns;
                controlLayer.addComponent(ns);
                configureDetailList(ns.getPalette());
                break;
            case "OptionButton":
                OptionButton ob = new OptionButton(new Dimension(10,1),"Example",paletteEditing);
                ob.setLocation(new Point (controlLayer.getWidth()/2 - 2,controlLayer.getHeight()/2));
                componentEditing=ob;
                controlLayer.addComponent(ob);
                configureDetailList(ob.getPalette());
                break;
            case "TextEntry":
                TextEntry te = new TextEntry(new Dimension(10,1),paletteEditing);
                te.setLocation(new Point (controlLayer.getWidth()/2 - 2,controlLayer.getHeight()/2));
                te.setText("Example");
                componentEditing=te;
                controlLayer.addComponent(te);
                configureDetailList(te.getPalette());
                break;
            default:

        }
    }

    public static void onDetailListSelectionChanged(String entry){
        if (entry == null){
            redNS.setValue(0);
            blueNS.setValue(0);
            greenNS.setValue(0);
            alphaNS.setValue(255);
            return;
        }
        Palette palette= componentEditing.getPalette();
        try {
            Field field = getField(palette.getClass(),entry);
            field.setAccessible(true);
            Color color = (Color) field.get(palette);
            redNS.setValue(color.getRed());
            blueNS.setValue(color.getBlue());
            greenNS.setValue(color.getGreen());
            alphaNS.setValue(color.getAlpha());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static Field getField(Class<? > clazz, String name){
        for (Field declaredField : clazz.getDeclaredFields()) {
            if (declaredField.getName()==name)
                return declaredField;
        }
        if (clazz.getSuperclass() != null && !(clazz.getSuperclass().isAssignableFrom(Object.class))){
            return getField(clazz.getSuperclass(),name);
        }
        return null;
    }

    private static void onSavePressed(Point point, int button){


        ObjectMapper mapper = new ObjectMapper();

        String curDir = System.getProperty("user.dir");
        File dataDir = new File(curDir,"data");
        if (!dataDir.exists()){
            dataDir.mkdir();
        }
        File outFile =new File(dataDir,paletteEditing+".json");
        try {
            abstract class Mixin{
                @JsonIgnore public abstract ColorSpace getColorSpace();
                @JsonIgnore private ColorSpace cs;
                @JsonIgnore public abstract int getRGB();
                @JsonIgnore public abstract int getTransparency();
            }

            mapper.addMixIn(Color.class,Mixin.class).writerWithDefaultPrettyPrinter().writeValue(outFile,PaletteManager.getInstance().getPalette(paletteEditing));
        } catch (IOException e) {
            e.printStackTrace();
        };
//        try (FileOutputStream fos = new FileOutputStream(outFile)){
//            fos.write(str.getBytes());
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


    }
    private static void OnResetPressed(Point point, Integer button) {
        paletteEditing="default";
        PaletteManager.getInstance().buildNewPalette("default");
        controlLayer.removeComponent(componentEditing);
        paletteDetailListBox.setVisible(false);
        paletteDetailListBox.clear();
        controlListBox.setSelected(-1);
    }
    private static void OnLoadPressed(Point point, Integer btn) {
        if (btn ==1 ){
            loadPopup();
        }
    }

    private static void OnNewPressed(Point point, Integer btn) {
        if (btn == 1){
            newPopup();
        }
    }



    //Here comes the duplication!  Fun.
    private static void newPopup(){
        backLayer.setEnabled(false);
        Layer popupLayer = new Layer(new Dimension(30,10));
        popupLayer.setLocation(new Point(backLayer.getWidth()/2 - popupLayer.getWidth()/2, backLayer.getHeight()/2 - popupLayer.getHeight()/2));
        screen.addComponent(popupLayer);
        for (int x = 0; x < popupLayer.getWidth(); x++) {
            popupLayer.setTileCharacter(x,0,'█');
            popupLayer.setTileCharacter(x,popupLayer.getHeight()-1,'█');
        }

        for (int y = 0; y < popupLayer.getHeight(); y++) {
            popupLayer.setTileCharacter(0,y,'█');
            popupLayer.setTileCharacter(popupLayer.getWidth()-1,y,'█');
        }
        int x = popupLayer.getWidth()/2 - 2;
        popupLayer.drawText(x,0,"New",popupLayer.getPalette().getForeground(),popupLayer.getPalette().getForeground());
        popupLayer.drawText(1,popupLayer.getHeight()/2,"Name",popupLayer.getPalette().getForeground(),popupLayer.getPalette().getForeground());
        TextEntry te = new TextEntry(new Dimension(15,1));
        te.setLocation(new Point(6,popupLayer.getHeight()/2));
        popupLayer.addComponent(te);
        te.mouseClicked(new Point(0,0),1); //Hack.
        Button okButton = new Button(new Dimension(5,1), "Ok");
        okButton.setLocation(new Point(22,popupLayer.getHeight()/2));
        popupLayer.addComponent(okButton);
        okButton.setOnClick((point, btn) ->{
            if (btn != 1)
                return;
            if (te.getText() != "" && te.getText() !="default") {
                paletteEditing = te.getText();
                screen.removeComponent(popupLayer);
                popupLayer.setVisible(false);
                backLayer.setEnabled(true);
                PaletteManager.getInstance().buildNewPalette(paletteEditing);

                controlLayer.removeComponent(componentEditing);
                paletteDetailListBox.setVisible(false);
                paletteDetailListBox.clear();
                controlListBox.setSelected(-1);

            }
        });

        Button cancelButton = new Button(new Dimension(24,1), "Cancel");
        cancelButton.setLocation(new Point(3,popupLayer.getHeight()/2+2));
        popupLayer.addComponent(cancelButton);
        cancelButton.setOnClick((point, btn)->{
            screen.removeComponent(popupLayer);
            backLayer.setEnabled(true);
        });

    }

    private static void loadPopup(){
        backLayer.setEnabled(false);
        Layer popupLayer = new Layer(new Dimension(30,10));
        popupLayer.setLocation(new Point(backLayer.getWidth()/2 - popupLayer.getWidth()/2, backLayer.getHeight()/2 - popupLayer.getHeight()/2));
        screen.addComponent(popupLayer);
        for (int x = 0; x < popupLayer.getWidth(); x++) {
            popupLayer.setTileCharacter(x,0,'█');
            popupLayer.setTileCharacter(x,popupLayer.getHeight()-1,'█');
        }

        for (int y = 0; y < popupLayer.getHeight(); y++) {
            popupLayer.setTileCharacter(0,y,'█');
            popupLayer.setTileCharacter(popupLayer.getWidth()-1,y,'█');
        }
        int x = popupLayer.getWidth()/2 - 2;
        popupLayer.drawText(x,0,"Load",popupLayer.getPalette().getForeground(),popupLayer.getPalette().getForeground());
        popupLayer.drawText(1,popupLayer.getHeight()/2,"Name",popupLayer.getPalette().getForeground(),popupLayer.getPalette().getForeground());
        TextEntry te = new TextEntry(new Dimension(15,1));
        te.setLocation(new Point(6,popupLayer.getHeight()/2));
        popupLayer.addComponent(te);
        te.mouseClicked(new Point(0,0),1); //Hack.
        Button okButton = new Button(new Dimension(5,1), "Ok");
        okButton.setLocation(new Point(22,popupLayer.getHeight()/2));
        popupLayer.addComponent(okButton);
        okButton.setOnClick((point, btn) ->{
            if (btn != 1)
                return;
            if (te.getText() != "" && te.getText() !="default") {
                paletteEditing = te.getText();
                screen.removeComponent(popupLayer);
                popupLayer.setVisible(false);
                backLayer.setEnabled(true);
                PaletteManager.getInstance().loadPalette(paletteEditing);

                controlLayer.removeComponent(componentEditing);
                paletteDetailListBox.setVisible(false);
                paletteDetailListBox.clear();
                controlListBox.setSelected(-1);

            }
        });

        Button cancelButton = new Button(new Dimension(24,1), "Cancel");
        cancelButton.setLocation(new Point(3,popupLayer.getHeight()/2+2));
        popupLayer.addComponent(cancelButton);
        cancelButton.setOnClick((point, btn)->{
            screen.removeComponent(popupLayer);
            backLayer.setEnabled(true);
        });

    }




}
