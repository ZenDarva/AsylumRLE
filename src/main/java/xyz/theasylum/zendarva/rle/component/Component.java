package xyz.theasylum.zendarva.rle.component;

import lombok.Getter;
import lombok.Setter;
import xyz.theasylum.zendarva.rle.event.EventListener;
import xyz.theasylum.zendarva.rle.palette.Palette;
import xyz.theasylum.zendarva.rle.utility.PointUtilities;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class Component extends TileGrid implements EventListener {
    private final String paletteGroup;
    private List<Component> componentList = new ArrayList<>();
    protected TileGrid tileGrid;
    protected Point location = new Point(0,0);
    protected Rectangle rect;

    @Getter @Setter Consumer<Component> onChanged;
    @Getter @Setter boolean visible=true;
    @Getter @Setter boolean enabled=true;

    protected boolean isDirty = true;




    public Component(Dimension dimension) {
        this(dimension,"default");

    }
    public Component(Dimension dimension, String paletteGroup){
        super(dimension);

        this.paletteGroup = paletteGroup;
        rect = new Rectangle(0, 0, getWidth(),getHeight());
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
        rect = new Rectangle(0, 0, getWidth(),getHeight());
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



    public void update(Long time){}

    @Override
    public boolean mouseClicked(Point position, int button) {
        for (Component component : componentList) {
            if (!component.isEnabled())
                continue;
            if (component.contains(PointUtilities.makeRelative(position,component.location))){
                if (component.mouseClicked(PointUtilities.makeRelative(position,component.location),button))
                        return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyTyped(KeyEvent keyEvent) {
        return false;
    }

    public boolean contains(Point point){
        return rect.contains(point);
    }

    @Override
    public void mouseEntered() {

    }

    @Override
    public void mouseLeft() {

    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        return false;
    }

    @Override
    public boolean keyReleased(KeyEvent keyEvent) {
        return false;
    }

    public void drawText(int x, int y, String text, Color foreground, Color background, int maxLength){
        for (int i = 0; i < Math.min(maxLength,text.length()); i++) {
            setTileCharacter(x+i,y,text.charAt(i));
            setTileBackground(x+i,y,background);
            setTileForeground(x+i,y,foreground);
        }
    }

    public void drawText(int x, int y, String text, Color foreground, Color background){
        drawText(x,y,text,foreground,background,text.length());
    }

    public void drawText(int x, int y, String text, Color foreground, Color background, int highlight, Color highlightColor){
        for (int i = 0; i < text.length(); i++) {
            setTileCharacter(x+i,y,text.charAt(i));
            if (i == highlight)
                setTileBackground(x+i,y,highlightColor);
            else
                setTileBackground(x+i,y,background);
            setTileForeground(x+i,y,foreground);
        }
    }

    protected void setDirty(){
        isDirty=true;
    }

    public abstract Palette getPalette();
}
