package xyz.theasylum.zendarva.rle.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import xyz.theasylum.zendarva.rle.Screen;
import xyz.theasylum.zendarva.rle.component.Button;
import xyz.theasylum.zendarva.rle.component.Component;
import xyz.theasylum.zendarva.rle.component.Layer;
import xyz.theasylum.zendarva.rle.component.ListBox;
import xyz.theasylum.zendarva.rle.component.NumberSpinner;
import xyz.theasylum.zendarva.rle.palette.Palette;
import xyz.theasylum.zendarva.rle.palette.PaletteManager;
import xyz.theasylum.zendarva.rle.testlauncher.EngineTest;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

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

    public static void main(String[] args){


        Screen screen = new Screen(new Dimension(80,40), PaletteEditor::processTick);

        backLayer = new Layer(new Dimension(80,40), new Point(0,0));


        controlLayer = new Layer(new Dimension(40,20),new Point(0,0));

        for (int x = 0; x < 40; x++) {
            controlLayer.setTileCharacter(x,0,'█');
            controlLayer.setTileCharacter(x,19,'█');
        }

        for (int y = 0; y < 20; y++) {
            controlLayer.setTileCharacter(0,y,'█');
            controlLayer.setTileCharacter(39,y,'█');
        }
        backLayer.addComponent(controlLayer);


        redNS = new NumberSpinner(255,0,0);
        redNS.setLocation(new Point(42,10));
        redNS.setOnChanged(PaletteEditor::onNumberSpinnerChange);


        greenNS = new NumberSpinner(255,0,0);
        greenNS.setLocation(new Point(46,10));
        greenNS.setOnChanged(PaletteEditor::onNumberSpinnerChange);

        blueNS = new NumberSpinner(255,0,0);
        blueNS.setLocation(new Point(50,10));
        blueNS.setOnChanged(PaletteEditor::onNumberSpinnerChange);

        alphaNS = new NumberSpinner(255,0,255);
        alphaNS.setLocation(new Point(54,10));
        alphaNS.setOnChanged(PaletteEditor::onNumberSpinnerChange);

        backLayer.addComponent(redNS);
        backLayer.addComponent(blueNS);
        backLayer.addComponent(greenNS);
        backLayer.addComponent(alphaNS);


        backLayer.addComponent(createPaletteEntryList());
        paletteDetailListBox = new ListBox(new Dimension(20,15));
        paletteDetailListBox.setLocation(new Point(58,21));
        paletteDetailListBox.setVisible(false);
        paletteDetailListBox.setOnSelectionChanged(PaletteEditor::onDetailListSelectionChanged);
        backLayer.addComponent(paletteDetailListBox);

        saveButton = new Button(new Dimension(10,3),"Save");
        saveButton.setLocation(new Point(42,1));
        saveButton.setOnClick(PaletteEditor::onSavePressed);
        backLayer.addComponent(saveButton);

        resetButton = new Button(new Dimension(10,3),"Reset");
        resetButton.setLocation(new Point(53,1));
        resetButton.setOnClick(PaletteEditor::OnResetPressed);
        backLayer.addComponent(resetButton);

        loadButton = new Button(new Dimension(10,3),"Load");
        loadButton.setLocation(new Point(64,1));
        backLayer.addComponent(loadButton);

        screen.addComponent(backLayer);
        screen.startEngine();
    }



    public static void processTick(Long time){

    }

    private static ListBox createPaletteEntryList(){

        controlListBox = new ListBox(new Dimension(20,15));
        controlListBox.setLocation(new Point(58, 5));
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

        for (int x = 42; x < 57; x++) {
            for (int y = 5; y < 7; y++) {
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
                Button button = new Button(new Dimension(9,3), "EXAMPLE");
                button.setLocation(new Point (controlLayer.getWidth()/2 - 4,controlLayer.getHeight()/2-2));
                componentEditing=button;
                controlLayer.addComponent(button);
                configureDetailList(button.getPalette());
                break;
            case "Layer":
                componentEditing=controlLayer;
                configureDetailList(controlLayer.getPalette());
                break;
            case "ListBox":
                ListBox lb = new ListBox(new Dimension(10,6));
                lb.setLocation(new Point (controlLayer.getWidth()/2 - 10,controlLayer.getHeight()/2-3));
                componentEditing=lb;
                controlLayer.addComponent(lb);
                lb.addEntry("Sample1");
                lb.addEntry("Sample2");
                lb.addEntry("Sample Three");
                configureDetailList(lb.getPalette());
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
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        String str = gson.toJson(PaletteManager.getInstance().getPalette("default"));
        System.out.println(str);
        Type type = new TypeToken<Map<String, Palette>>(){}.getType();
        Object obj = gson.fromJson(str,type);

    }
    private static void OnResetPressed(Point point, Integer button) {

    }
    private static void OnLoadPressed(Point point, Integer button) {

    }
}
