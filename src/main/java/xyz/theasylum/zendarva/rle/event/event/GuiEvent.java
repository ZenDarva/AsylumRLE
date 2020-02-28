package xyz.theasylum.zendarva.rle.event.event;


import lombok.Getter;
import xyz.theasylum.zendarva.rle.component.Component;

public  class GuiEvent implements Event {

    public static class SetFocus extends GuiEvent{
        @Getter private Component component;

        public SetFocus(Component component){
            this.component = component;
        }
    }
}
