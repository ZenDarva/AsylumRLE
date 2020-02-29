package xyz.theasylum.zendarva.rle.component;

import xyz.theasylum.zendarva.rle.Tile;
import xyz.theasylum.zendarva.rle.event.EventQueue;
import xyz.theasylum.zendarva.rle.event.EventQueueManager;
import xyz.theasylum.zendarva.rle.event.event.GuiEvent;
import xyz.theasylum.zendarva.rle.palette.Palette;
import xyz.theasylum.zendarva.rle.palette.PaletteManager;
import xyz.theasylum.zendarva.rle.palette.TextEntryPalette;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Map;

public class TextEntry extends Component {

    int caretLoc = 0;
    private Tile caret;
    private boolean isFocused;
    char[] text;
    long flashCounter=0;
    TextEntryPalette palette;

    public TextEntry(Dimension dimension) {
        super(dimension);
        palette = PaletteManager.getInstance().getPalette("default",TextEntryPalette.class);



        caret = new Tile();
        caret.setCharacter('_');

        text = new char[dimension.width];
        for (int i = 0; i < text.length; i++) {
            text[i]=' ';
        }


        EventQueueManager.instance().subscribeQueue(EventQueueManager.guiEventQueue,this);
    }

    @Override
    public boolean keyTyped(KeyEvent keyEvent) {
        if (!isFocused)
                return false;
        if (keyEvent.getKeyChar() == 0){
            System.out.println("Not a char");
        }
        Character ch = (char)keyEvent.getKeyChar();
        if (Character.isLetterOrDigit(ch)){
            if (caretLoc == dimension.width){
                return true;
            }
            if (text[text.length-1]!=' ')//Buffer is full.
                return true;

            for (int i = text.length - 1; i >= caretLoc+1; i--) {
                text[i]=text[i-1];
            }
            text[caretLoc] =ch;
            caretLoc++;
            if (caretLoc >= dimension.width){
                caretLoc=dimension.width-1;
            }
            return true;
        }
        else{
            switch (keyEvent.getKeyCode()){
                case KeyEvent.VK_LEFT:
                    caretLoc--;
                    if (caretLoc<0)
                        caretLoc=0;
                    return true;
                case KeyEvent.VK_RIGHT:
                    if (text[caretLoc+1] == ' ' && text[caretLoc]==' '){
                        return true;
                    }
                    caretLoc++;
                    if (caretLoc >= dimension.width){
                        caretLoc=dimension.width -1;
                    }
                    return true;
                case KeyEvent.VK_BACK_SPACE:
                    if (caretLoc >0){
                        caretLoc--;
                        for(int i = caretLoc;i<dimension.width-1;i++){
                            text[i]=text[i+1];
                        }
                        text[dimension.width-1]= ' ';
                    }
                    return true;
                case KeyEvent.VK_DELETE:
                    if (caretLoc < dimension.width){
                        for(int i = caretLoc;i<dimension.width-1;i++){
                            text[i]=text[i+1];
                        }
                        text[dimension.width-1]= ' ';
                    }
                    return true;
                case KeyEvent.VK_HOME:
                    caretLoc=0;
                    return true;
                case KeyEvent.VK_END:
                    caretLoc=dimension.width-1;
                    for (int i = 0; i < text.length; i++) {
                        if (text[i] == ' ') {
                            caretLoc = i;
                            break;
                        }
                    }
                    return true;
                case KeyEvent.VK_SPACE:
                    text[caretLoc]= ' ';
                    caretLoc++;
                    if (caretLoc >= dimension.width){
                        caretLoc=dimension.width -1;
                    }
            }

        }

        return false;
    }

    @Override
    public boolean mouseClicked(Point position, int button) {
        if (super.mouseClicked(position,button))
            return true;
        if (!isFocused) {
            isFocused = true;
            EventQueueManager.instance().raiseEvent(EventQueueManager.guiEventQueue, new GuiEvent.SetFocus(this));
            return true;
        }
        return false;
    }

    @Override
    public void update(Long time) {
        flashCounter+=time;
        if (!isDirty)
            return;
        for (int i = 0; i < dimension.width; i++) {
            setTileCharacter(i,0,text[i]);
        }
        for (int x = 0; x < dimension.width; x++) {
            for (int y = 0; y < dimension.height; y++) {
                setTileBackground(x,y,palette.getBackground());
                setTileForeground(x,y,palette.getForeground());
            }
        }
        caret.setForeground(palette.getCaretForeground());
        caret.setBackground(palette.getCaretBackground());

    }

    @Override
    public Map<Point, Tile> getExtras() {
        if (isFocused && flashCounter <801) {
            return Collections.singletonMap(new Point(caretLoc, 0), caret);
        }
        if (flashCounter>=1600)
            flashCounter=0;
        return super.getExtras();
    }

    public void HandleSetFocus(GuiEvent.SetFocus event){
        if (event.getComponent() != this && this.isFocused == true){
            this.isFocused=false;
        }
    }

    public void setText(String newText){
        for (int i = 0; i < text.length; i++) {
            if (i < newText.length())
                text[i]= newText.charAt(i);
            else
                text[i]=' ';
        }
    }

    @Override
    public Palette getPalette() {
        return palette;
    }
}
