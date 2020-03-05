package xyz.theasylum.zendarva.rle;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.theasylum.zendarva.rle.component.Component;
import xyz.theasylum.zendarva.rle.event.EventListener;
import xyz.theasylum.zendarva.rle.event.EventQueueManager;
import xyz.theasylum.zendarva.rle.event.event.GuiEvent;
import xyz.theasylum.zendarva.rle.exception.MissingFont;
import xyz.theasylum.zendarva.rle.utility.PointUtilities;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static xyz.theasylum.zendarva.rle.Tile.darken;


public class Screen extends Thread {
    protected static final Logger LOG= LogManager.getLogger(Screen.class);
    protected final Dimension dimensions;

    protected Thread gameThread;
    protected Consumer<Long> mainFunction;
    protected java.util.List<Component> componentList;
    protected boolean RUNNING=true;
    protected long lastTime = System.currentTimeMillis();
    @Getter @Setter String windowTitle ="AsylumRL";

    private Frame frame;
    private Font font;
    private Canvas canvas;
    private EventListener fallbackKeyHandler;

    private Component focusedComponent = null;

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
            font = new Font(new FontGenerator("Fonts/PxPlus_IBM_CGAthin.ttf",12f));
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
            long gameTime = System.currentTimeMillis() - lastTime;
            lastTime=System.currentTimeMillis();
            mainFunction.accept(gameTime);
            componentList.forEach(f->updateComponent(f,gameTime));
            draw();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                LOG.info("Main engine thread interrupted. {}", e);
            }

        }
    }

    private void updateComponent(Component targComponent, long gameTime){
        targComponent.update(gameTime);
        for (Component component : targComponent.getComponents()) {
            updateComponent(component,gameTime);
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
                if (componentList.get(i).isEnabled())
                    this.drawComponent(componentList.get(i), new Point(0,0),g,null);
                else
                    this.drawComponent(componentList.get(i), new Point(0,0),g, darken);
            }


            g.dispose();
        } while (strat.contentsRestored());
        strat.show();

    }
    private void drawComponent(Component component, Point offset, Graphics2D g, Tile.TileTransform transform){
        if (!component.isVisible()){
            return;
        }
        if (!component.isEnabled() && transform !=darken)
            transform = darken;

        int offsetX=offset.x * font.getCharWidth() + component.getLocation().x *font.getCharWidth();
        int offsetY=offset.y * font.getCharHeight() + component.getLocation().y * font.getCharHeight();



        for (int x = 0; x < component.getWidth(); x++) {
            for (int y = 0; y < component.getHeight(); y++) {
                int lX = x*font.getCharWidth()+offsetX;
                int lY = y*font.getCharHeight()+offsetY;
                if (transform!=null)
                    font.draw(component.getTiles()[x][y], lX, lY, g, transform);
                else
                    font.draw(component.getTiles()[x][y], lX, lY, g);
            }
        }
        component.getExtras().keySet().stream().forEach(f->font.draw(component.getExtras().get(f),offsetX + f.x * font.charWidth,offsetY+f.y *font.charHeight,g));
        for (Component child : component.getComponents()) {
            drawComponent(child,component.getLocation(),g,transform);
        }
    }

    private void startup(){
        frame = new Frame(windowTitle);
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
        EventQueueManager.instance().subscribeQueue(EventQueueManager.guiEventQueue,new InternalEventHandler());
    }

    public void shutdown(){
        RUNNING=false;
    }
    public Point translateToGrid(Point point){
        return new Point(point.x/font.charWidth,point.y/font.charHeight);
    }
    private Dimension calculateDimension(){
        return new Dimension(dimensions.width*font.charWidth,dimensions.height*font.charHeight);
    }

    private void passKey(int keycode){

    }
    public void setKeyHandler(EventListener fallbackInputHandler){
        this.fallbackKeyHandler = fallbackInputHandler;
    }

    private class InternalEventHandler{
        private void setFocus(GuiEvent.SetFocus event){
            focusedComponent=event.getComponent();
        }
    }


    private Component getHovered() {
        Component hovered = null;
        for (Component component : componentList) {
            if (!component.isEnabled())
                continue;
            if (component.contains(PointUtilities.makeRelative(mouseLoc,component.getLocation()))){
                hovered = getHoveredChild(component);
                break;
            }
        }
        return hovered;
    }

    private Component getHoveredChild(Component targComponent){
        Component hovered = null;
        for (Component component : targComponent.getComponents()) {
            if (!component.isEnabled())
                continue;
            if (component.contains(PointUtilities.makeRelative(mouseLoc,component.getLocation()))){
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

            if (focusedComponent != null){
                focusedComponent.keyTyped(e);
            }
            else{
                if (fallbackKeyHandler != null)
                fallbackKeyHandler.keyTyped(e);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (focusedComponent != null){
                focusedComponent.keyPressed(e);
            }
            else{
                if (fallbackKeyHandler != null)
                fallbackKeyHandler.keyPressed(e);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (focusedComponent != null){
                focusedComponent.keyReleased(e);
            }
            else{
                if (fallbackKeyHandler != null)
                fallbackKeyHandler.keyReleased(e);
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
                if (!component.isEnabled())
                    continue;
                if (!component.contains(PointUtilities.makeRelative(gridPoint,component.getLocation())))
                    continue;
                if (component.mouseClicked(PointUtilities.makeRelative(gridPoint,component.getLocation()),e.getButton()))
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
            if (newHovered!= null && !newHovered.isEnabled()){
                newHovered=null;
            }
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
