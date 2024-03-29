/*
 This file is part of Arcadeflex.

 Arcadeflex is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Arcadeflex is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Arcadeflex.  If not, see <http://www.gnu.org/licenses/>.
 */
package gr.codebb.arcadeflex.v036.platform;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.image.ImageProducer;
import java.awt.image.ImageConsumer;
import java.awt.image.DirectColorModel;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import static arcadeflex.v036.mame.mame.*;

public class software_gfx extends java.awt.Frame implements Runnable, ImageProducer, KeyListener, MouseListener, MouseMotionListener {
    /* Data. */

    boolean _scanlines;
    int _width;
    int _height;
    float ratio;
    int oldWidth;
    int oldHeight;
    Insets _insets;
    public int[] _pixels;
    public boolean[] key = new boolean[1024];
    public int readkey = 0;
    Image _image;
    public Thread _thread;
    ImageConsumer _consumer;
    DirectColorModel _model;
    BufferStrategy _strategy;

    public software_gfx(String title) {
        super(title);
    }

    public int[] resizeBilinear(int[] pixels, int w, int h, int w2, int h2) {
        int[] temp = new int[w2 * h2];
        int a, b, c, d, x, y, index;
        float x_ratio = ((float) (w - 1)) / w2;
        float y_ratio = ((float) (h - 1)) / h2;
        float x_diff, y_diff, blue, red, green;
        int offset = 0;
        for (int i = 0; i < h2; i++) {
            for (int j = 0; j < w2; j++) {
                x = (int) (x_ratio * j);
                y = (int) (y_ratio * i);
                x_diff = (x_ratio * j) - x;
                y_diff = (y_ratio * i) - y;
                index = (y * w + x);
                a = pixels[index];
                b = pixels[index + 1];
                c = pixels[index + w];
                d = pixels[index + w + 1];

            // blue element
                // Yb = Ab(1-w)(1-h) + Bb(w)(1-h) + Cb(h)(1-w) + Db(wh)
                blue = (a & 0xff) * (1 - x_diff) * (1 - y_diff) + (b & 0xff) * (x_diff) * (1 - y_diff)
                        + (c & 0xff) * (y_diff) * (1 - x_diff) + (d & 0xff) * (x_diff * y_diff);

            // green element
                // Yg = Ag(1-w)(1-h) + Bg(w)(1-h) + Cg(h)(1-w) + Dg(wh)
                green = ((a >> 8) & 0xff) * (1 - x_diff) * (1 - y_diff) + ((b >> 8) & 0xff) * (x_diff) * (1 - y_diff)
                        + ((c >> 8) & 0xff) * (y_diff) * (1 - x_diff) + ((d >> 8) & 0xff) * (x_diff * y_diff);

            // red element
                // Yr = Ar(1-w)(1-h) + Br(w)(1-h) + Cr(h)(1-w) + Dr(wh)
                red = ((a >> 16) & 0xff) * (1 - x_diff) * (1 - y_diff) + ((b >> 16) & 0xff) * (x_diff) * (1 - y_diff)
                        + ((c >> 16) & 0xff) * (y_diff) * (1 - x_diff) + ((d >> 16) & 0xff) * (x_diff * y_diff);

                temp[offset++]
                        = /*0xff000000 | // hardcode alpha*/ ((((int) red) << 16) & 0xff0000)
                        | ((((int) green) << 8) & 0xff00)
                        | ((int) blue);
            }
        }
        return temp;
    }

    public synchronized void blit() {
        if (this._consumer != null)/* Check consumer. */ {
            /* Set dimensions. */
            this._consumer.setDimensions(this._width, this._height);
            /* Copy integer pixel data to image consumer. */
            //int[] px = resizeBilinear(_pixels, this._width, this._height, this._width, this._height);
            this._consumer.setPixels(0, 0, this._width, this._height, this._model, this._pixels /*px*/, 0, this._width);
            /* Notify image consumer that the frame is done. */
            this._consumer.imageComplete(ImageConsumer.SINGLEFRAMEDONE);
        }
        /* Handle resize events. */
        int i = getWidth() - this._insets.left - this._insets.right;
        int j = getHeight() - this._insets.top - this._insets.bottom;
        /* Draw image to graphics context. */
        Graphics2D localGraphics2D = (Graphics2D) this._strategy.getDrawGraphics();
        if (Machine.gamedrv.source_file.equals("kyugo.java")) {
            if (Machine.gamedrv.name.equals("airwolf") || Machine.gamedrv.name.equals("flashgal") || Machine.gamedrv.name.equals("skywolf") || Machine.gamedrv.name.equals("skywolf2")) {//temp hack for airwolf and flashgal
                localGraphics2D.drawImage(this._image, this._insets.left, this._insets.top, i + (int) (i * 0.78), j, null);
            } else {
                localGraphics2D.drawImage(this._image, this._insets.left, this._insets.top, i, j + (int) (i * 0.78), null);

            }
        } else if (Machine.gamedrv.source_file.equals("system1.java")) {
            if (Machine.gamedrv.name.equals("starjack") || Machine.gamedrv.name.equals("starjacs") || Machine.gamedrv.name.equals("regulus") || Machine.gamedrv.name.equals("regulusu") || Machine.gamedrv.name.equals("upndown") || Machine.gamedrv.name.equals("mrviking") || Machine.gamedrv.name.equals("mrvikinj") || Machine.gamedrv.name.equals("swat")) {
                localGraphics2D.drawImage(this._image, this._insets.left, this._insets.top, i + (int) (i * 0.15), j, null);
            } else {
                localGraphics2D.drawImage(this._image, this._insets.left, this._insets.top, i, j + (int) (j * 0.14), null);
            }
        } else if (Machine.gamedrv.name.equals("tnk3") || Machine.gamedrv.name.equals("tnk3j")) {
            localGraphics2D.drawImage(this._image, this._insets.left - (int) (i * 0.02), this._insets.top, i, j, null);
        } else if (Machine.gamedrv.name.equals("tdfever") || Machine.gamedrv.name.equals("tdfeverj") || Machine.gamedrv.name.equals("chopper") || Machine.gamedrv.name.equals("legofair") || Machine.gamedrv.name.equals("gwar") || Machine.gamedrv.name.equals("gwarj") || Machine.gamedrv.name.equals("gwara") || Machine.gamedrv.name.equals("gwarb")) {
            localGraphics2D.drawImage(this._image, this._insets.left + (int) (i * 0.03), this._insets.top, i, j, null);
        } else if (Machine.gamedrv.source_file.equals("srumbler.java")) {
            localGraphics2D.drawImage(this._image, this._insets.left - (int) (i * 0.035), this._insets.top, i + (int) (i * 0.07), j, null);
        } else if (Machine.gamedrv.source_file.equals("simpsons.java")) {
            localGraphics2D.drawImage(this._image, this._insets.left - (int) (i * 0.39), this._insets.top, i + (int) (i * 0.78), j, null);
        } else if (Machine.gamedrv.source_file.equals("vendetta.java")) {
            localGraphics2D.drawImage(this._image, this._insets.left - (int) (i * 0.35), this._insets.top, i + (int) (i * 0.70), j, null);
        } else if (Machine.gamedrv.source_file.equals("surpratk.java")) {
            localGraphics2D.drawImage(this._image, this._insets.left - (int) (i * 0.39), this._insets.top, i + (int) (i * 0.78), j, null);
        } else if (Machine.gamedrv.source_file.equals("aliens.java")) {
            localGraphics2D.drawImage(this._image, this._insets.left - (int) (i * 0.39), this._insets.top, i + (int) (i * 0.78), j, null);
        } else if (Machine.gamedrv.source_file.equals("crimfght.java")) {
            localGraphics2D.drawImage(this._image, this._insets.left - (int) (i * 0.35), this._insets.top, i + (int) (i * 0.70), j, null);
        } else if (Machine.gamedrv.source_file.equals("m72.java")) {
            if (Machine.gamedrv.name.equals("imgfight")) {
                localGraphics2D.drawImage(this._image, this._insets.left - (int) (i * 0.18), this._insets.top - (int) (j * 0.18), i + (int) (i * 0.35), j + (int) (j * 0.35), null);
            } else if (Machine.gamedrv.name.equals("gallop")) {
                localGraphics2D.drawImage(this._image, this._insets.left - (int) (i * 0.17), this._insets.top - (int) (j * 0.25), i + (int) (i * 0.34), j + (int) (j * 0.50), null);
            } else {
                localGraphics2D.drawImage(this._image, this._insets.left - (int) (i * 0.18), this._insets.top - (int) (j * 0.25), i + (int) (i * 0.35), j + (int) (j * 0.50), null);
            }
        } else if (Machine.gamedrv.source_file.equals("raiden.java")) {
            localGraphics2D.drawImage(this._image, this._insets.left - (int) (i * 0.07), this._insets.top, i + (int) (i * 0.14), j, null);
        } else if (Machine.gamedrv.source_file.equals("cps1.java")) {
            localGraphics2D.drawImage(this._image, this._insets.left - (int) (i * 0.085), this._insets.top - (int) (j * 0.215), i + (int) (i * 0.167), j + (int) (j * 0.43), null);
        } else {
            localGraphics2D.drawImage(this._image, this._insets.left, this._insets.top, i, j, null);
        }
        this._strategy.show();
        Toolkit.getDefaultToolkit().sync();
    }

    public void start() {
        /* Check if thread exists. */
        if (_thread != null) {
            return;
        }

        /* Create and start thread. */
        _thread = new Thread(this);
        _thread.start();
    }

    public synchronized void setSize(boolean scanlines, int width, int height) {

        /* Setup options. */
        _scanlines = scanlines;

        /* Setup pixel buffer and dimensions. */
        ratio = ((float) width) / ((float) height);

        _width = width;
        if (_scanlines) {
            width *= 2;
            height *= 2;
        }
        _height = height;
        _pixels = new int[width * height];
        /* Setup frame dimensions. */
        _insets = getInsets();
        // super.setSize(width+ this._insets.left + this._insets.right, height + this._insets.top + this._insets.bottom);
        //hacked width height x2
        if (Machine.gamedrv.source_file.equals("kyugo.java")) {
            if (Machine.gamedrv.name.equals("airwolf") || Machine.gamedrv.name.equals("flashgal") || Machine.gamedrv.name.equals("skywolf") || Machine.gamedrv.name.equals("skywolf2")) {//temp hack for airwolf and flashgal
                super.setSize(width + this._insets.left + this._insets.right, height * 2 + this._insets.top + this._insets.bottom);
            } else {
                super.setSize(width * 2 + this._insets.left + this._insets.right, height + this._insets.top + this._insets.bottom);
            }
        } else if (Machine.gamedrv.source_file.equals("srumbler.java")) {
            super.setSize(width * 2 + this._insets.left + this._insets.right, height + (int) (height * 0.50) + this._insets.top + this._insets.bottom);
        } else if (Machine.gamedrv.source_file.equals("simpsons.java")) {
            super.setSize(width * 2 + this._insets.left + this._insets.right - (int) (width * 0.78), height * 2 + this._insets.top + this._insets.bottom);
        } else if (Machine.gamedrv.source_file.equals("vendetta.java")) {
            super.setSize(width * 2 + this._insets.left + this._insets.right - (int) (width * 0.70), height * 2 + this._insets.top + this._insets.bottom);
        } else if (Machine.gamedrv.source_file.equals("surpratk.java")) {
            super.setSize(width * 2 + this._insets.left + this._insets.right - (int) (width * 0.70), height * 2 + this._insets.top + this._insets.bottom);
        } else if (Machine.gamedrv.source_file.equals("aliens.java")) {
            super.setSize(width * 2 + this._insets.left + this._insets.right - (int) (width * 0.70), height * 2 + this._insets.top + this._insets.bottom);
        } else if (Machine.gamedrv.source_file.equals("crimfght.java")) {
            super.setSize(width * 2 + this._insets.left + this._insets.right - (int) (width * 0.78), height * 2 + this._insets.top + this._insets.bottom);
        } else if (Machine.gamedrv.source_file.equals("m72.java")) {
            if (Machine.gamedrv.name.equals("imgfight")) {
                super.setSize(width * 2 + this._insets.left + this._insets.right - (int) (width * 0.35), height + (int) (height * 0.35) + this._insets.top + this._insets.bottom);
            } else if (Machine.gamedrv.name.equals("gallop")) {
                super.setSize(width * 2 + this._insets.left + this._insets.right - (int) (width * 0.34), height + (int) (height * 0.50) + this._insets.top + this._insets.bottom);
            } else {
                super.setSize(width * 2 + this._insets.left + this._insets.right - (int) (width * 0.35), height + (int) (height * 0.50) + this._insets.top + this._insets.bottom);
            }
        } else if (Machine.gamedrv.source_file.equals("raiden.java")) {
            super.setSize(width * 2 + this._insets.left + this._insets.right - (int) (width * 0.14), height * 2 + this._insets.top + this._insets.bottom);
        } else {
            super.setSize(width * 2 + this._insets.left + this._insets.right, height * 2 + this._insets.top + this._insets.bottom);
        }
        System.out.println(this.getWidth() + "x" + this.getHeight());
        super.createBufferStrategy(2);//double buffering
        this._strategy = super.getBufferStrategy();
    }

    public void resizeVideo() {
        /*
         int i = getWidth() - this._insets.left - this._insets.right;
         int j = getHeight() - this._insets.top - this._insets.bottom;
         System.out.println("before width=" + i + " height="+ j);
         if ((j < this._height) || (i < this._width))
         {
         i = this._width;
         j = this._height;
         }
         if ((j != this.oldHeight) || (i != this.oldWidth))
         {
         //keep aspect ratio
         if (Math.abs(this.oldHeight - j) >= Math.abs(this.oldWidth - i))
         {
         this.oldHeight = j;
         this.oldWidth = (int)(this.ratio * j);
         }
         else
         {
         this.oldWidth = i;
         this.oldHeight = (int)(i / this.ratio);
         }
         }

         System.out.println("after  width=" + this.oldWidth + " height="+ this.oldHeight);
           
         setSize(this.oldWidth + this._insets.left + this._insets.right, this.oldHeight + this._insets.top + this._insets.bottom);
         */
    }

    public void run() {
        /* Setup color model. */
        this._model = new DirectColorModel(32, 0x00FF0000, 0x000FF00, 0x000000FF, 0);
        /* Create image using default toolkit. */
        this._image = Toolkit.getDefaultToolkit().createImage(this);
    }

    public synchronized void reinit() {
        /* Recreate image using default toolkit. */
        _image = Toolkit.getDefaultToolkit().createImage(this);
        _consumer = null;
    }

    public void stop() {
        /* Destroy thread. */
        _thread = null;
    }

    public synchronized void addConsumer(ImageConsumer ic) {
        /* Register image consumer. */
        _consumer = ic;

        /* Set image dimensions. */
        _consumer.setDimensions(_width * 2, _height * 2);

        /* Set image consumer hints for speed. */
        _consumer.setHints(ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.COMPLETESCANLINES | ImageConsumer.SINGLEPASS | ImageConsumer.SINGLEFRAME);

        /* Set image color model. */
        _consumer.setColorModel(_model);
    }

    public synchronized boolean isConsumer(ImageConsumer ic) {
        /* Check if consumer is registered. */
        return true;
    }

    public synchronized void removeConsumer(ImageConsumer ic) {
        /* Remove image consumer. */
    }

    public void startProduction(ImageConsumer ic) {
        /* Add consumer. */
        addConsumer(ic);
    }

    public void requestTopDownLeftRightResend(ImageConsumer ic) {
        /* Ignore resend request. */
    }

    /**
     * Handle the key pressed event from the text field.
     */
    public void keyPressed(KeyEvent e) {
        readkey = e.getKeyCode();
        key[readkey] = true;
        e.consume();
    }

    /**
     * Handle the key released event from the text field.
     */
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Handle the key released event from the text field.
     */
    public void keyReleased(KeyEvent e) {
        key[e.getKeyCode()] = false;
        e.consume();
    }

    public void mouseClicked(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mousePressed(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseReleased(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseEntered(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseExited(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseDragged(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseMoved(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
