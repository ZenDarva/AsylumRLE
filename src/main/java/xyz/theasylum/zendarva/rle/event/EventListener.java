package xyz.theasylum.zendarva.rle.event;

import java.awt.*;
import java.awt.event.KeyEvent;

public interface EventListener {
    boolean mouseClicked(Point position, int button);
    boolean keyTyped(KeyEvent keyEvent);
    void mouseEntered();
    void mouseLeft();
}
