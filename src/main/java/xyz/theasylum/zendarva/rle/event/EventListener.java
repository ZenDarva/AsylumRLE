package xyz.theasylum.zendarva.rle.event;

import java.awt.*;
import java.awt.event.KeyEvent;

public interface EventListener {
    boolean mouseClicked(Point position, int button);
    boolean keyTyped(KeyEvent keyEvent);
    boolean keyPressed(KeyEvent keyEvent);
    boolean keyReleased(KeyEvent keyEvent);
    void mouseEntered();
    void mouseLeft();
}
