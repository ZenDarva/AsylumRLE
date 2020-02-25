package xyz.theasylum.zendarva.rle;


import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.theasylum.zendarva.rle.exception.MissingFont;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Font;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FontGenerator {

    public static final int TILES_PER = 200;
    BufferedImage junkImage;
    @Getter BufferedImage finalImage;
    @Getter Map<Character, Rectangle> fontMap;
    @Getter int charWidth;
    @Getter int charHeight;
    private static final Logger LOG = LogManager.getLogger(FontGenerator.class);


    public FontGenerator(File file, float size) throws IOException, FontFormatException, MissingFont {

        if (!file.exists()){
            LOG.error("Attempting to load non-existant font: {}", file.getAbsolutePath());
            throw new MissingFont();
        }

        Font fnt = Font.createFont(Font.TRUETYPE_FONT, file).deriveFont(size);

        junkImage = new BufferedImage(100,100,BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = junkImage.createGraphics();
        g.setFont(fnt);
        FontMetrics fm = g.getFontMetrics();
        if (fm.charWidth('W')!= fm.charWidth('.')){
            LOG.warn("Loading a non-monospaced font.");
        }

        drawFont(fnt, fm);


    }

    public FontGenerator(String resourcePath, float size) throws MissingFont {

        InputStream stream = resourcePath.getClass().getResourceAsStream(resourcePath);

        if (stream == null){
            LOG.error("Attempting to load a non-existant jarFont: {}",resourcePath);
            throw new MissingFont();
        }

        Font fnt = null;
        try {
            fnt = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(size);
        } catch (FontFormatException e) {
            LOG.error("Error loading jarFont: {}",resourcePath);
            throw new MissingFont(e);
        } catch (IOException e) {
            LOG.error("Error loading jarFont: {}",resourcePath);
            throw new MissingFont(e);
        }

        junkImage = new BufferedImage(100,100,BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = junkImage.createGraphics();
        g.setFont(fnt);
        FontMetrics fm = g.getFontMetrics();
        if (fm.charWidth('W')!= fm.charWidth('.')){
            LOG.warn("Loading a non-monospaced font.");
        }

        drawFont(fnt, fm);

    }

    private void drawFont(Font fnt, FontMetrics fm){
        int width = fm.charWidth('A');
        int height = fm.getHeight();


        BufferedImage image = new BufferedImage(width* TILES_PER,height* TILES_PER,BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D) image.getGraphics();
        int cwidth = (int) fm.getMaxCharBounds(g).getWidth();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setFont(fnt);
        Point point = new Point(0,0);

        fontMap= new HashMap<>();

        g.setColor(Color.white);

        for (int i = 0; i <TILES_PER*TILES_PER; i++) {
            char c = (char) i;

            if (fnt.canDisplay(i)){
                g.drawString(Character.toString(c),point.x,point.y+fm.getAscent());
                fontMap.put(c,new Rectangle(point.x,point.y,width,height));
                point.x+=width;

            }
            if (point.x > width* TILES_PER){
                point.x =0;
                point.y+=height;
            }
        }

        g.dispose();

        finalImage = new BufferedImage(width* TILES_PER, point.y+height, BufferedImage.TYPE_4BYTE_ABGR);
        g= (Graphics2D) finalImage.getGraphics();
        g.drawImage(image,0,0,width* TILES_PER,point.y+height,0,0,width* TILES_PER,point.y+height,null);
        g.dispose();
        charWidth=width;
        charHeight=height;
    }
}
