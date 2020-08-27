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
import xyz.theasylum.zendarva.rle.particle.Particle;
import xyz.theasylum.zendarva.rle.particle.ParticleManager;
import xyz.theasylum.zendarva.rle.utility.PointF;
import xyz.theasylum.zendarva.rle.utility.PointUtilities;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static xyz.theasylum.zendarva.rle.Tile.darken;


public class Screen {
    protected static final Logger LOG = LogManager.getLogger(Screen.class);
    protected final Dimension dimensions;
    private Dimension windowSize;
    float xScaleFactor = 1;
    float yScaleFactor = 1;

    protected Thread gameThread;
    protected Consumer<Long> mainFunction;
    protected java.util.List<Component> componentList;
    protected boolean RUNNING = true;
    protected long lastTime = System.currentTimeMillis();
    @Getter
    @Setter
    String windowTitle = "AsylumRL";

    private Frame frame;
    private Font font;
    private Canvas canvas;
    private EventListener fallbackKeyHandler;
    private Component focusedComponent = null;
    private Point mouseLoc = new Point(0, 0);
    private Component hovered = null;
    private boolean stopGameProcessing = false;
    private ParticleManager particleManager = new ParticleManager();

    private Image[][] frameData;
    private Map<Point, Tile> extraTiles = new HashMap<>();

    public Screen(Consumer<Long> mainFunction) {
        this(new Dimension(80, 40), mainFunction);
        frameData = new Image[80][40];
    }


    public Screen(Dimension dimensions, Consumer<Long> mainFunction) {

        this.dimensions = dimensions;
        this.mainFunction = mainFunction;
        this.componentList = new ArrayList<>();
        try {
            font = new Font(new FontGenerator("Fonts/PxPlus_IBM_CGAthin.ttf", 10f));
        } catch (MissingFont missingFont) {
            LOG.error("Unable to construct engine due to missing default font: /Fonts/DejaVu Sans Mono/20pt/bitmap.png");
            System.exit(-1);
        }
        this.windowSize = calculateDimension();
        frameData = new Image[dimensions.width][dimensions.height];
    }

    public Screen(Dimension dimensions, Consumer<Long> mainFunction, Font font) {
        this.dimensions = dimensions;
        this.mainFunction = mainFunction;
        this.componentList = new ArrayList<>();
        this.font = font;
        windowSize = calculateDimension();
        frameData = new Image[dimensions.width][dimensions.height];
    }

    public Screen(Dimension dimensions, Consumer<Long> mainFunction, Font font, Dimension screenSize) {
        this.dimensions = dimensions;
        this.mainFunction = mainFunction;
        this.componentList = new ArrayList<>();
        this.font = font;
        windowSize = screenSize;
        xScaleFactor = ((float) (dimensions.width * font.charWidth) / (float) (windowSize.width));
        yScaleFactor = ((float) (dimensions.height * font.charHeight) / (float) (windowSize.height));
        frameData = new Image[dimensions.width][dimensions.height];
    }

    public Screen(Dimension dimensions, Consumer<Long> mainFunction, Dimension screenSize) {
        this.dimensions = dimensions;
        this.mainFunction = mainFunction;
        this.componentList = new ArrayList<>();
        try {
            font = new Font(new FontGenerator("Fonts/PxPlus_IBM_CGAthin.ttf", 10f));
        } catch (MissingFont missingFont) {
            LOG.error("Unable to construct engine due to missing default font: /Fonts/DejaVu Sans Mono/20pt/bitmap.png");
            System.exit(-1);
        }
        windowSize = screenSize;
        xScaleFactor = ((float) (dimensions.width * font.charWidth) / (float) (windowSize.width));
        yScaleFactor = ((float) (dimensions.height * font.charHeight) / (float) (windowSize.height));
        frameData = new Image[dimensions.width][dimensions.height];
    }

    public void addComponent(Component component) {
        componentList.add(0, component);
    }

    public List<Component> getComponents() {
        return Collections.unmodifiableList(componentList);
    }

    public void removeComponent(Component component) {
        if (focusedComponent == component) {
            focusedComponent = null;
        }
        componentList.remove(component);
    }

    public void startEngine() {
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


    private void mainLoop() {
        try {
            while (RUNNING) {
                long gameTime = System.currentTimeMillis() - lastTime;
                lastTime = System.currentTimeMillis();
                if (!stopGameProcessing)
                    mainFunction.accept(gameTime);
                componentList.forEach(f -> updateComponent(f, gameTime));
                particleManager.update(gameTime);
                draw();
                //Thread.sleep(10);
                EventQueueManager.instance().processEvents();

            }
        } catch (
                Exception e) {
            LOG.info("Main engine thread interrupted. {}", e);
            Thread.currentThread().interrupt();
        }
    }

    private void updateComponent(Component targComponent, long gameTime) {
        targComponent.update(gameTime);
        for (Component component : targComponent.getComponents()) {
            updateComponent(component, gameTime);
        }
    }

    private void draw() {
        Dimension screenSize = calculateDimension();
        GraphicsConfiguration gfxConfig = GraphicsEnvironment.
                getLocalGraphicsEnvironment().getDefaultScreenDevice().
                getDefaultConfiguration();
        BufferedImage image = gfxConfig.createCompatibleImage(screenSize.width, screenSize.height, Transparency.OPAQUE);

        Graphics2D g = (Graphics2D) image.getGraphics();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, screenSize.width, screenSize.height);

        //Component list is stored newest to oldest for control interactions, but should be drawn
        //oldest to newest.
        for (int i = componentList.size() - 1; i >= 0; i--) {
            if (componentList.get(i).isEnabled())
                this.bakeComponent(componentList.get(i), new Point(0, 0), g, null);
            else
                this.bakeComponent(componentList.get(i), new Point(0, 0), g, darken);
        }
        drawFrame(g);
        stopGameProcessing=false;
        for (Particle liveParticle : particleManager.getLiveParticles()) {
            g.setColor(liveParticle.getColor());
            g.drawRect((int)liveParticle.getLoc().x,(int)liveParticle.getLoc().y,3,3);
            if (liveParticle.isStopTurn())
                stopGameProcessing=true;
        }

        g.dispose();
        BufferStrategy strat = canvas.getBufferStrategy();
        do {
            g = (Graphics2D) strat.getDrawGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
            g.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight(), null);
        } while (strat.contentsRestored());
        strat.show();
    }

    public void spawnParticle(Point location, PointF velocity, Color color){
        Point center = translateFromGrid(location);
        center.translate(font.getCharWidth()/2,font.getCharHeight()/2);
        particleManager.spawnParticle(center, velocity,color, false);
    }
    public void spawnParticle(Point location, PointF velocity, Color color, BiFunction<Dimension, Point, Point> startFuzz){
        particleManager.spawnParticle(startFuzz.apply(new Dimension(font.charWidth,font.charHeight), translateFromGrid(location)), velocity,color, false);
    }

    public void spawnParticle(Point start, Point dest, Color color, boolean stopTurn){
        start = translateFromGrid(start);
        start.translate(font.getCharWidth()/2,font.getCharHeight()/2);
        dest = translateFromGrid(dest);
        dest.translate(font.getCharWidth()/2,font.getCharHeight()/2);
        PointF vel = new PointF((dest.x-start.x)/250f,(dest.y-start.y)/250f);

        particleManager.spawnParticle(start,vel,250,color, stopTurn);
    }

    private void bakeComponent(Component component, Point offset, Graphics2D g, Tile.TileTransform transform) {
        if (!component.isVisible()) {
            return;
        }
        if (!component.isEnabled() && transform != darken)
            transform = darken;

        for (Component child : component.getComponents()) {
            bakeComponent(child, component.getLocation(), g, transform);
        }

        int offsetX = offset.x +component.getLocation().x;
        int offsetY = offset.y + component.getLocation().y;

        Map<Point, Tile> extras = component.getExtras();
        Point extraPoint = new Point(0,0);
        for (int x = 0; x < component.getWidth(); x++) {
            for (int y = 0; y < component.getHeight(); y++) {
                if (frameData[x][y] != null)
                    continue;  //It's already been drawn to, we don't get to.
                if (transform != null)
                    frameData[x+offsetX][y+offsetY]=font.getGlyph(component.getTiles()[x][y],transform);
                else
                    frameData[x+offsetX][y+offsetY]=font.getGlyph(component.getTiles()[x][y]);
                extraPoint.move(x,y);
                if (extras.containsKey(extraPoint)){
                    extraTiles.put(new Point(offsetX+x,offsetY+y),extras.get(extraPoint));
                }
            }
        }
    }

    private void drawFrame(Graphics2D g){
        Point extraPoint = new Point(0,0);
        for (int x = 0; x < dimensions.width; x++) {
            for (int y = 0; y < dimensions.height; y++) {
                g.drawImage(frameData[x][y],x*font.getCharWidth(),y*font.charHeight,null);
                frameData[x][y]=null;
                extraPoint.move(x,y);
                if (extraTiles.containsKey(extraPoint)){
                    font.draw(extraTiles.get(extraPoint),x*font.getCharWidth(),y *font.getCharHeight(),g);
                }
            }
        }
        extraTiles.clear();

    }

    private void startup() {
        frame = new Frame(windowTitle);
        frame.setResizable(false);
        frame.setLayout(new FlowLayout());
        frame.setLocationRelativeTo(null);


        frame.setIgnoreRepaint(true);
        canvas = new Canvas();
        canvas.setIgnoreRepaint(true);
        canvas.setPreferredSize(windowSize);
        LOG.info("Starting up with {} resolution", windowSize);
        canvas.setLocation(new Point(0, 0));
        frame.add(canvas);
        frame.setVisible(true);
        canvas.createBufferStrategy(2);

        frame.pack();
        canvas.setFocusable(true);
        canvas.requestFocus();

        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                RUNNING = false;
            }
        });

        canvas.addMouseListener(new LocalMouseHandler());
        canvas.addMouseMotionListener(new LocalMouseMotionListener());
        canvas.addKeyListener(new LocalKeyHandler());
        EventQueueManager.instance().subscribeQueue(EventQueueManager.guiEventQueue, new InternalEventHandler());
    }

    public void shutdown() {
        RUNNING = false;
    }

    public Point translateToGrid(Point point) {
        return new Point((int) (point.x * xScaleFactor) / font.charWidth, (int) (point.y * yScaleFactor) / font.charHeight);
    }
    public Point translateFromGrid(Point point) {
        return new Point(point.x *font.charWidth, point.y *font.charHeight);
    }

    private Dimension calculateDimension() {
        return new Dimension(dimensions.width * font.charWidth, dimensions.height * font.charHeight);
    }

    private void passKey(int keycode) {

    }

    public void setKeyHandler(EventListener fallbackInputHandler) {
        this.fallbackKeyHandler = fallbackInputHandler;
    }

    private class InternalEventHandler {
        private void setFocus(GuiEvent.SetFocus event) {
            focusedComponent = event.getComponent();
        }
    }


    private Component getHovered() {
        Component hovered = null;
        for (Component component : componentList) {
            if (!component.isEnabled())
                continue;
            if (component.contains(PointUtilities.makeRelative(mouseLoc, component.getLocation()))) {
                hovered = getHoveredChild(component);
                break;
            }
        }
        return hovered;
    }

    private Component getHoveredChild(Component targComponent) {
        Component hovered = null;
        for (Component component : targComponent.getComponents()) {
            if (!component.isEnabled())
                continue;
            if (component.contains(PointUtilities.makeRelative(mouseLoc, component.getLocation()))) {
                hovered = getHoveredChild(component);
            }
        }
        if (hovered == null)
            hovered = targComponent;
        return hovered;
    }

    public void setWindowSize(Dimension newSize) {
        Dimension screenSize = calculateDimension();
        if (newSize.width / newSize.height != screenSize.width / screenSize.height) {
            LOG.error("Incorrect screen ratio supplied. current: {} new: {}", screenSize.width / screenSize.height, newSize.width / newSize.height);
            return;
        }
        LOG.info("Changing to {}x{} size", newSize.width, newSize.height);
        xScaleFactor = ((float) (dimensions.width * font.charWidth) / (float) (windowSize.width));
        yScaleFactor = ((float) (dimensions.height * font.charHeight) / (float) (windowSize.height));
        canvas.setSize(newSize);
        frame.pack();
    }


    private class LocalKeyHandler implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {

            if (focusedComponent != null) {
                focusedComponent.keyTyped(e);
            } else {
                if (fallbackKeyHandler != null)
                    fallbackKeyHandler.keyTyped(e);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (focusedComponent != null) {
                focusedComponent.keyPressed(e);
            } else {
                if (fallbackKeyHandler != null)
                    fallbackKeyHandler.keyPressed(e);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (focusedComponent != null) {
                focusedComponent.keyReleased(e);
            } else {
                if (fallbackKeyHandler != null)
                    fallbackKeyHandler.keyReleased(e);
            }

        }
    }

    private class LocalMouseHandler implements MouseListener {

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
                if (!component.contains(PointUtilities.makeRelative(gridPoint, component.getLocation())))
                    continue;
                if (component.mouseClicked(PointUtilities.makeRelative(gridPoint, component.getLocation()), e.getButton()))
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
            if (newHovered != null && !newHovered.isEnabled()) {
                newHovered = null;
            }
            if (newHovered != hovered) {
                if (hovered != null)
                    hovered.mouseLeft();
                if (newHovered != null)
                    newHovered.mouseEntered();
                hovered = newHovered;
            }
        }
    }
    protected static BufferedImage makeCompatible(BufferedImage image){
        // obtain the current system graphical settings
        GraphicsConfiguration gfxConfig = GraphicsEnvironment.
                getLocalGraphicsEnvironment().getDefaultScreenDevice().
                getDefaultConfiguration();

        /*
         * if image is already compatible and optimized for current system
         * settings, simply return it
         */
        if (image.getColorModel().equals(gfxConfig.getColorModel()))
            return image;

        // image is not optimized, so create a new image that is
        BufferedImage newImage = gfxConfig.createCompatibleImage(
                image.getWidth(), image.getHeight(), image.getTransparency());
        newImage.setAccelerationPriority(1);
        // get the graphics context of the new image to draw the old image on
        Graphics2D g2d = newImage.createGraphics();

        // actually draw the image and dispose of context no longer needed
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();


        // return the new optimized image
        return newImage;
    }
}
