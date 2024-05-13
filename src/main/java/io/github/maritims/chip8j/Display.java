package io.github.maritims.chip8j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Display extends JPanel {
    private static final Logger log = LoggerFactory.getLogger(Display.class);

    private final int           columns;
    private final int           scale;
    private final BufferedImage canvas;

    public Display(int columns, int rows, int scale) {
        this.columns = columns;
        this.scale   = scale;
        this.canvas  = new BufferedImage(this.columns * scale, rows * scale, BufferedImage.TYPE_INT_ARGB);

        setPreferredSize(new Dimension(this.columns * scale, rows * scale));
        setDoubleBuffered(true);

        clear();
    }

    public void clear() {
        for (var x = 0; x < canvas.getWidth(); x++) {
            for (var y = 0; y < canvas.getHeight(); y++) {
                canvas.setRGB(x, y, Color.BLACK.getRGB());
            }
        }
        repaint();
    }

    private void draw(int[] pixelBuffer) {
        for (var i = 0; i < pixelBuffer.length; i++) {
            var x = (i % columns) * scale;
            var y = ((int) Math.floor((double) i / columns)) * scale;

            if (pixelBuffer[i] != 1) {
                continue;
            }

            var graphics = canvas.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(x, y, scale, scale);
            repaint();
        }
    }

    public void render(int[] pixels) {
        clear();
        draw(pixels);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(canvas, 0, 0, null);
    }
}