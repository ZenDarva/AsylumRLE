package xyz.theasylum.zendarva.rle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.theasylum.zendarva.rle.component.Button;
import xyz.theasylum.zendarva.rle.component.Component;
import xyz.theasylum.zendarva.rle.exception.MissingFont;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;
import java.util.function.Consumer;

public class Screen extends Thread {
    protected static final Logger LOG= LogManager.getLogger(Screen.class);
    protected final Dimension dimensions;

    protected Thread gameThread;
    protected Consumer<Long> mainFunction;
    protected java.util.List<Component> componentList;
    protected boolean RUNNING=true;
    protected long lastTime = System.currentTimeMillis();

    private Frame frame;
    private Font font;
    private Canvas canvas;
    private EventListener fallbackKeyHandler;

    private Point mouseLoc = new Point(0,0);
    private Component hovered = null;

    public Screen(Consumer<Long> mainFunction){
        this(new Dimension(80,40),mainFunction);
    }

    public Screen(Dimension dimensions, Consumer<Long> mainFunction){
        this.dimensions = dimensions;
        this.mainFunction = mainFunction;
        this.componentList=new ArrayList<>();
        try {
            font = new Font(new FontGenerator("/Fonts/DejaVuSansMono.ttf",50f));
        } catch (MissingFont missingFont) {
            LOG.error("Unable to construct engine due to missing default font: /Fonts/DejaVu Sans Mono/20pt/bitmap.png");
            System.exit(-1);
        }
    }

    public Screen(Dimension dimensions, Consumer<Long> mainFunction, Font font){
        this.dimensions = dimensions;
        this.mainFunction = mainFunction;
        this.componentList=new ArrayList<>();
        this.font=font;
    }

    public void addComponent(Component component){
        componentList.add(0,component);
    }

    public List<Component> getComponents(){
        return Collections.unmodifiableList(componentList);
    }
    public void removeComponent(Component component){
        componentList.remove(component);
    }

    public void startEngine(){
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
            g.fillRect(0,0,dimensions.width*font.charWidth,dimensions.height*font.charHeight);

            //Component list is stored newest to oldest for control interactions, but should be drawn
            //oldest to newest.
            for (int i = componentList.size() - 1; i >= 0; i--) {
                this.drawComponent(componentList.get(i),g);
            }


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

    private void startup(){
        frame = new Frame("");
        frame.setResizable(false);
        frame.setLayout(new FlowLayout());
        frame.setLocationRelativeTo(null);


        frame.setIgnoreRepaint(true);
        canvas = new Canvas();
        canvas.setIgnoreRepaint(true);
        canvas.setPreferredSize(calculateDimension());
        canvas.setLocation(new Point(0,0));
        frame.add(canvas);
        frame.setVisible(true);
        canvas.createBufferStrategy(2);

        frame.pack();
        canvas.setFocusable(true);
        canvas.requestFocus();

        frame.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent e) {
                RUNNING=false;
            }
        });

        canvas.addMouseListener(new LocalMouseHandler());
        canvas.addMouseMotionListener(new LocalMouseMotionListener());
        canvas.addKeyListener(new LocalKeyHandler());
    }
    private Dimension calculateDimension(){
        return new Dimension(dimensions.width*font.charWidth,dimensions.height*font.charHeight);
    }

    private void setFallbackKeyHandler(EventListener fallbackInputHandler){

        this.fallbackKeyHandler = fallbackKeyHandler;
    }

    public Point translateToGrid(Point point){
        return new Point(point.x/font.charWidth,point.y/font.charHeight);
    }
    private Component getHovered() {
        Component hovered = null;
        for (Component component : componentList) {
            if (component.contains(mouseLoc)){
                hovered = getHoveredChild(component);
                break;
            }
        }
        return hovered;
    }

    private Component getHoveredChild(Component targComponent){
        Component hovered = null;
        for (Component component : targComponent.getComponents()) {
            if (component.contains(mouseLoc)){
                hovered = getHoveredChild(component);
            }
        }
        if (hovered == null)
            hovered=targComponent;
        return hovered;
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

    private class LocalMouseHandler implements MouseListener{

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

            Point gridPoint = translateToGrid(e.getPoint());
            for (Component component : componentList) {
                if (!component.contains(gridPoint))
                    continue;
                if (component.mouseClicked(gridPoint,e.getButton()))
                    return;
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    private class LocalMouseMotionListener implements MouseMotionListener {

        @Override
        public void mouseDragged(MouseEvent e) {

        }

        @Override
        public void mouseMoved(MouseEvent e) {
            mouseLoc = translateToGrid(e.getPoint());
            Component newHovered = getHovered();
            if (newHovered != hovered){
                if (hovered !=null)
                    hovered.mouseLeft();
                if (newHovered !=null)
                    newHovered.mouseEntered();
                hovered=newHovered;
            }
        }
    }
}
