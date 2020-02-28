package xyz.theasylum.zendarva.rle.utility;

import xyz.theasylum.zendarva.rle.Screen;
import xyz.theasylum.zendarva.rle.component.Layer;
import xyz.theasylum.zendarva.rle.testlauncher.EngineTest;

import java.awt.*;

public class PaletteEditor {
    public static void main(String[] args){


        Screen screen = new Screen(new Dimension(80,40), PaletteEditor::processTick);

        Layer backLayer = new Layer(new Dimension(80,40),new Point(0,0));

        for (int x = 0; x < 80; x++) {
            backLayer.setTileCharacter(x,0,'█');
            backLayer.setTileCharacter(x,39,'█');
        }

        for (int y = 0; y < 40; y++) {
            backLayer.setTileCharacter(0,y,'█');
            backLayer.setTileCharacter(79,y,'█');
        }

        screen.addComponent(backLayer);
        screen.startEngine();
    }

    public static void processTick(Long time){

    }
}
