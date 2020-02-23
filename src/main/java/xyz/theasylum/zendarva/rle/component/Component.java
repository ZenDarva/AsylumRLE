package xyz.theasylum.zendarva.rle.component;

import lombok.Getter;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Component extends TileGrid {
    private List<Component> componentList = new ArrayList<>();
    protected TileGrid tileGrid;
    @Getter protected Point location;


    public Component(Dimension dimension) {
        super(dimension);
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

    public boolean isFocusable;


}
