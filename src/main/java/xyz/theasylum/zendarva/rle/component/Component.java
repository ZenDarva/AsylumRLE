package xyz.theasylum.zendarva.rle.component;

import lombok.Getter;
import lombok.Setter;
import xyz.theasylum.zendarva.rle.event.EventListener;
import xyz.theasylum.zendarva.rle.utility.PointUtilities;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Component extends TileGrid implements EventListener {
    private List<Component> componentList = new ArrayList<>();
    protected TileGrid tileGrid;
    protected Point location = new Point(0,0);
    protected Rectangle rect;

    protected boolean isDirty = true;




    public Component(Dimension dimension) {
        super(dimension);
        rect = new Rectangle(0, 0, getWidth(),getHeight());
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
        rect = new Rectangle(location.x, location.y, getWidth(),getHeight());
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

    protected void setDirty(){
        isDirty=true;
    }
}
