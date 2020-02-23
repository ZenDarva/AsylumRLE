package xyz.theasylum.zendarva.rle.testlauncher;

import xyz.theasylum.zendarva.rle.Screen;
import xyz.theasylum.zendarva.rle.component.Layer;

import java.awt.*;
import java.util.Random;

public class EngineTest {
    public static void main(String[] args){
        Screen screen = new Screen(EngineTest::processGame);
        System.out.println("Success!");




        Layer layer = new Layer(new Dimension(80,40), new Point(5,5));
        Random rnd = new Random();
        for (int x = 0; x < 80; x++) {
            for (int y = 0; y < 40; y++) {
                layer.setTileCharacter(x,y, (char) (rnd.nextInt(26)+97));
                layer.setTileForeground(x,y,Color.BLACK);
                layer.setTileBackground(x,y,getRandomColor());
            }
        }
        screen.addComponent(layer);

        Layer layer2 = new Layer(new Dimension(20,20));
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 20; y++) {
                layer2.setTileCharacter(x,y, '@');
                layer2.setTileForeground(x,y,Color.RED);
                layer2.setTileBackground(x,y,new Color(0,0,0,0));
            }
        }
        screen.addComponent(layer2);
        screen.startEngine();
    }

    public static void processGame(Long time){

    }

    public static Color getRandomColor(){
        Random rnd = new Random();
        return new Color(rnd.nextInt(255),rnd.nextInt(255),rnd.nextInt(255));
    }
}
