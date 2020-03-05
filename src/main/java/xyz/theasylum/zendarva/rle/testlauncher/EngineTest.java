package xyz.theasylum.zendarva.rle.testlauncher;

import xyz.theasylum.zendarva.rle.Screen;
import xyz.theasylum.zendarva.rle.component.*;
import xyz.theasylum.zendarva.rle.component.Button;

import java.awt.*;
import java.io.IOException;
import java.util.Random;

public class EngineTest {
    public static void main(String[] args) throws IOException, FontFormatException {
//┗┛┏┓
        Screen screen = new Screen(new Dimension(80,80),EngineTest::processGame);
        Layer layer = new Layer(new Dimension(80,20),new Point(0,0));

        for (int x = 0; x < 80; x++) {
            for (int y = 0; y < 20; y++) {
                layer.setTileCharacter(x,y, '@');
                layer.setTileForeground(x,y,Color.RED);
                layer.setTileBackground(x,y,Color.BLACK);
            }
        }
        screen.addComponent(layer);
        Button button = new Button(new Dimension(10,1),"Test");
        button.setLocation(new Point(10,10));
        button.setOnClick((loc,btn)->{if (btn == 1)System.out.println("Whoa!");});
        screen.addComponent(button);

        TextEntry entry = new TextEntry(new Dimension(10,1));
        entry.setLocation(new Point(10,15));
        screen.addComponent(entry);

        OptionButton button1 = new OptionButton(new Dimension(10,1),"Yes");
        button1.setLocation(new Point(30,10));
        OptionButton button2 = new OptionButton(new Dimension(10,1),"No");
        button2.setLocation(new Point(30,15));

        button1.addPeer(button2);

        screen.addComponent(button1);
        screen.addComponent(button2);

        NumberSpinner spinner = new NumberSpinner(255,0,128);
        spinner.setLocation(new Point(45,10));

        screen.addComponent(spinner);

        button.setEnabled(false);
        ListBox lb = new ListBox(new Dimension(10,10));
        lb.setLocation(new Point(50,0));
        screen.addComponent(lb);
        for (int i = 0; i < 15; i++) {
            lb.addEntry("Entry"+i);
        }

        screen.startEngine();
//        Screen screen = new Screen(EngineTest::processGame);
//        System.out.println("Success!");
//
//
//
//
//        Layer layer = new Layer(new Dimension(80,40), new Point(5,5));
//        Random rnd = new Random();
//        for (int x = 0; x < 80; x++) {
//            for (int y = 0; y < 40; y++) {
//                layer.setTileCharacter(x,y, (char) (rnd.nextInt(26)+97));
//                layer.setTileForeground(x,y,Color.BLACK);
//                layer.setTileBackground(x,y,getRandomColor());
//            }
//        }
//        screen.addComponent(layer);
//
//        Layer layer2 = new Layer(new Dimension(20,20));
//        for (int x = 0; x < 20; x++) {
//            for (int y = 0; y < 20; y++) {
//                layer2.setTileCharacter(x,y, '@');
//                layer2.setTileForeground(x,y,Color.RED);
//                layer2.setTileBackground(x,y,new Color(0,0,0,0));
//            }
//        }
//        screen.addComponent(layer2);
//        screen.startEngine();

//        Object obj1 = new Object();
//        Object obj2 = new Object();
//        Profile.start(obj1);
//        FontGenerator fg = new FontGenerator(new File("C:\\temp\\DejaVuSansMono.ttf"),50f);
//        Profile.stop(obj1);
//        Profile.start(obj2);
//        Java2DFont f = new Java2DFont(Paths.get("C:\\temp\\DejaVuSansMono.ttf"),20);
//        Profile.stop(obj2);
    }


    public static void processGame(Long time){
    }

    public static Color getRandomColor(){
        Random rnd = new Random();
        return new Color(rnd.nextInt(255),rnd.nextInt(255),rnd.nextInt(255));
    }


}
