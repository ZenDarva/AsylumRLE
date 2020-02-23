package xyz.theasylum.zendarva.rle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import xyz.theasylum.zendarva.rle.component.Component;
import xyz.theasylum.zendarva.rle.exception.MissingFont;
import xyz.theasylum.zendarva.rle.handler.KeyHandler;

public class Screen extends Thread {
    protected static final Logger LOG= LogManager.getLogger(Screen.class);
    protected final Dimension dimensions;

    protected Thread gameThread;

    protected Consumer<Long> mainFunction;

    protected java.util.List<Component> componentList;

    protected boolean RUNNING=true;

    protected boolean mainLoop = true;

    protected long lastTime = System.currentTimeMillis();

    private JFrame frame;
    private Font font;
    private Canvas canvas;
    private KeyHandler fallbackKeyHandler;

    public Screen(Consumer<Long> mainFunction){
        this(new Dimension(800,600),mainFunction);
    }

    public Screen(Dimension dimensions, Consumer<Long> mainFunction){
        this.dimensions = dimensions;
        this.mainFunction = mainFunction;
        this.componentList=new ArrayList<>();
        try {
            font = new Font("/Fonts/DejaVu Sans Mono/20pt/bitmap.png");
        } catch (MissingFont missingFont) {
            LOG.error("Unable to construct engine due to missing default font: /Fonts/DejaVu Sans Mono/20pt/bitmap.png");
            System.exit(-1);
        }
    }

    public void addComponent(Component component){
        componentList.add(component);
    }

    public List<Component> getComponents(){
        return Collections.unmodifiableList(componentList);
    }
    public void removeComponent(Component component){
        componentList.remove(component);
    }

    public void startEngine(){
        if (mainLoop) {
            gameThread = new Thread() {
                @Override
                public void run() {
                    startup();
                    LOG.info("Starting main engine thread.");
                    mainLoop();
                    LOG.info("Stopping main engine thread ");
                    System.exit(0);
                }
            };
            gameThread.start();
        }
        else {
            startup();
        }
    }


    private void mainLoop(){

        while(RUNNING){
            mainFunction.accept(System.currentTimeMillis() - lastTime);
            draw();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOG.info("Main engine thread interrupted. {}", e);
            }
            lastTime=System.currentTimeMillis();
        }
    }


    private void draw(){
        BufferStrategy strat = canvas.getBufferStrategy();
        do {
            Graphics2D g = (Graphics2D) strat.getDrawGraphics();

            g.setColor(Color.BLACK);
            g.fillRect(0,0,dimensions.width,dimensions.height);

            componentList.forEach(component ->this.drawComponent(component,g));


            g.dispose();
        } while (strat.contentsRestored());
        strat.show();

    }
    private void drawComponent(Component component, Graphics2D g){
        int offsetX=component.getLocation().x * font.getCharWidth();
        int offsetY=component.getLocation().y * font.getCharHeight();

        for (int x = 0; x < component.getWidth(); x++) {
            for (int y = 0; y < component.getHeight(); y++) {
                int lX = x*font.getCharWidth()+offsetX;
                int lY = y*font.getCharHeight()+offsetY;
                //font.draw(component.getTileCharacter(x,y),lX,lY + offsetY,Color.red,Color.GREEN,g);
                font.draw(component.getTiles()[x][y],lX,lY,g);
            }
        }
        component.getComponents().forEach(child -> drawComponent(child,g));
    }

    protected void startup(){
        frame = new JFrame("");
        frame.setSize(dimensions);
        frame.setIgnoreRepaint(true);
        canvas = new Canvas();
        canvas.setIgnoreRepaint(true);
        canvas.setSize(dimensions);
        frame.add(canvas);
        frame.setVisible(true);
        canvas.createBufferStrategy(2);
        frame.addKeyListener(new LocalKeyHandler());
    }

    private void setFallbackKeyHandler(KeyHandler fallbackKeyHandler){

        this.fallbackKeyHandler = fallbackKeyHandler;
    }

    private class LocalKeyHandler implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {

        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
                RUNNING=false;
            }

        }
    }
}
