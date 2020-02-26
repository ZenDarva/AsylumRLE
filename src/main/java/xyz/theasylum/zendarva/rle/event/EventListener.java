package xyz.theasylum.zendarva.rle.event;

import java.awt.*;

public interface EventListener {
    boolean mouseClicked(Point position, int button);
    boolean keyTyped(int keycode);
    void mouseEntered();
    void mouseLeft();
}
