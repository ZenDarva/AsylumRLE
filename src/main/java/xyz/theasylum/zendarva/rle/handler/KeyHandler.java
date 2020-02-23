package xyz.theasylum.zendarva.rle.handler;

import java.awt.event.KeyEvent;

public interface KeyHandler {

    public boolean keyTyped(KeyEvent e);


    public boolean keyPressed(KeyEvent e);

    public boolean keyReleased(KeyEvent e);
}
