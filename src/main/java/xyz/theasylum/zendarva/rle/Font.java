package xyz.theasylum.zendarva.rle;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.theasylum.zendarva.rle.exception.MissingFont;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Font {
    private static final Logger LOG = LogManager.getLogger(Font.class);

    private Image fontTexture;

    @Getter
    protected int charWidth;
    @Getter
    protected int charHeight;

    protected Map<Character, Rectangle> fontMap;


    Cache<Tile.TileTransform, Map<Integer, Image>> tintCache;


    public Font(FontGenerator fg) {
        fontTexture = fg.finalImage;
        fontMap = fg.fontMap;
        tintCache = Caffeine.newBuilder().maximumSize(500).expireAfterAccess(1, TimeUnit.MINUTES).build();
        charWidth = fg.charWidth;
        charHeight = fg.charHeight;
    }

    public Font(String jarFont, float size) throws MissingFont {
        FontGenerator fg = new FontGenerator(jarFont, size);
        fontTexture = fg.finalImage;
        fontMap = fg.fontMap;
        tintCache = Caffeine.newBuilder().maximumSize(500).expireAfterAccess(1, TimeUnit.MINUTES).build();
        charWidth = fg.charWidth;
        charHeight = fg.charHeight;
    }


    public Font(File fontFile, float size) throws MissingFont {
        FontGenerator fg = new FontGenerator(fontFile, size);
        fontTexture = fg.finalImage;
        fontMap = fg.fontMap;
        tintCache = Caffeine.newBuilder().maximumSize(500).expireAfterAccess(1, TimeUnit.MINUTES).build();
        charWidth = fg.charWidth;
        charHeight = fg.charHeight;
    }


    public void draw(Tile tile, int x, int y, Graphics g) {
        draw(tile, x, y, g, Tile.none);
    }

    public void draw(Tile tile, int x, int y, Graphics g, Tile.TileTransform transform) {
        if (tile.getCharacter() == 0)
            return;
        Image glyph = getGlyph(tile, transform);
        g.setColor(transform.transform(tile.getBackground()));
        g.drawImage(glyph, x, y, null);
    }

    private Image getGlyph(Tile tile) {
        return getGlyph(tile, Tile.none);
    }

    private Image getGlyph(Tile tile, Tile.TileTransform transform) {
        Image glyph;
        Map<Integer, Image> tintMap = tintCache.getIfPresent(transform);
        if (tintMap != null) {
            glyph = tintCache.getIfPresent(transform).get(tile.hashCode());
            if (glyph != null) {
                return glyph;
            }
        }
        glyph = new BufferedImage(charWidth, charHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D) glyph.getGraphics();
        Rectangle rect = fontMap.get(tile.getCharacter());
        if (rect == null){
            LOG.error("Missing character '{}' in font.", tile.getCharacter());
            rect = fontMap.get('a');
        }
        g.setColor(tile.getBackground());

        g.fillRect(0, 0, rect.width, rect.height);

        g.drawImage(fontTexture, 0, 0, rect.width, rect.height, rect.x, rect.y, rect.x + rect.width, rect.y + rect.height, null);
        Color foreColor = new Color(tile.getForeground().getRed(), tile.getForeground().getGreen(), tile.getForeground().getBlue(), tile.getBackground().getAlpha());
        foreColor = transform.transform(foreColor);
        g.setXORMode(foreColor);
        g.drawImage(fontTexture, 0, 0, rect.width, rect.height, rect.x, rect.y, rect.x + rect.width, rect.y + rect.height, null);
        g.dispose();
        if (tintCache.getIfPresent(transform) == null) {
            tintCache.put(transform, new HashMap<>());
        }
        tintCache.getIfPresent(transform).put(tile.hashCode(), glyph);
        return glyph;
    }

    private void loadFontResource(String resourceName) throws MissingFont {
        InputStream stream = Image.class.getResourceAsStream(resourceName);

        if (stream == null) {
            LOG.error("Attempted to load nonexistant jarFont description {}.", resourceName);
            throw new MissingFont();
        }
        InputStreamReader isr = new InputStreamReader(stream);
        BufferedReader reader = new BufferedReader(isr);
        processFontFileLines(reader.lines().filter(line -> line.startsWith("char ")).collect(Collectors.toList()), resourceName);
    }

    private void loadFontFile(File file) throws IOException, MissingFont {
        if (!file.exists()) {
            LOG.error("Attempted to load a font that does not exist: {}.", file);
            throw new MissingFont();
        }
        List<String> lines;
        try (Stream<String> s = Files.lines(file.toPath())){
            lines = s.filter(line->line.startsWith("char " )).collect(Collectors.toList());
            processFontFileLines(lines, file.getName());
        }


    }

    private void processFontFileLines(List<String> lines, String file) {
        fontMap = new HashMap<>();
        for (String line : lines) {
            String[] token = line.split("\\s+");
            int id = Integer.parseInt(token[1].replaceAll(".*=", ""));
            if (id > 100000) {
                continue;
            }
            Character ch = (char) id;
            int x = Integer.parseInt(token[2].replaceAll(".*=", ""));
            int y = Integer.parseInt(token[3].replaceAll(".*=", ""));
            int width = Integer.parseInt(token[4].replaceAll(".*=", ""));
            int height = Integer.parseInt(token[5].replaceAll(".*=", ""));

            //Yeah yeah, i'm gonna do this a bunch of unnecessary times.  I don't care.
            charWidth = width;
            charHeight = height;

            if (fontMap.containsKey(ch))
                LOG.info("Duplicate character detected in font {}, character is {}", file, ch);
            fontMap.put(ch, new Rectangle(x, y, width, height));
        }
    }
}
