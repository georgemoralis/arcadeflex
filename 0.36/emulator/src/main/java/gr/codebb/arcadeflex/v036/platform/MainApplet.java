/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.codebb.arcadeflex.v036.platform;

import static gr.codebb.arcadeflex.v036.platform.libc_old.ConvertArguments;
import static gr.codebb.arcadeflex.v036.platform.libc_old.argc;
import static gr.codebb.arcadeflex.v036.platform.libc_old.argv;
import java.applet.Applet;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.VK_INSERT;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import static arcadeflex.v036.mame.mame.*;

/**
 *
 * @author nickblame
 */
public class MainApplet extends Applet implements Runnable, ImageProducer, KeyListener, MouseListener, MouseMotionListener {

    /**
     * Initialization method that will be called after the applet is loaded into
     * the browser.
     */
    public static MainApplet inst;
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
    private int i = 0, j = 0;
    private int x = 0, y = 0;
    private ScreenshotUploader suploader = null;
    //BufferStrategy _strategy;
    //public static debug d;

    public void init() {
        //d = new debug(this);
        //d.setVisible(true);
        //d.log("test");
        inst = this;

        String param1 = getParameter("PARAM1");

        // TODO start asynchronous download of heavy resources
        String[] args = new String[1];
        if (param1 != null) {
            args[0] = param1;
        } else {
            args[0] = "snowbros";
        }

        ConvertArguments("arcadeflex", args);
        args = null;
        Runnable r = new Runnable() {
            public void run() {
                System.exit(osdepend.main(argc, argv));
            }
        };

        new Thread(r).start();

    }

    @Override
    public void start() {
        /* Check if thread exists. */
        if (_thread != null) {
            return;
        }

        /* Create and start thread. */
        _thread = new Thread(this);
        _thread.start();
    }

    public void getFocus() {
        requestFocus();
        requestFocusInWindow();
    }

    public BufferedImage screenshot() {
        BufferedImage image = new BufferedImage(
                getWidth() - this._insets.left - this._insets.right,
                getHeight() - this._insets.top - this._insets.bottom,
                BufferedImage.TYPE_INT_RGB
        );
        image.getGraphics().drawImage(_image, this._insets.left, this._insets.top, i, j, null);
        return image;
    }

    private void updateOffsets() {
        i = getWidth() - this._insets.left - this._insets.right;
        j = getHeight() - this._insets.top - this._insets.bottom;

        if (Machine.gamedrv.source_file.equals("kyugo.java")) {
            if (Machine.gamedrv.name.equals("airwolf") || Machine.gamedrv.name.equals("flashgal") || Machine.gamedrv.name.equals("skywolf") || Machine.gamedrv.name.equals("skywolf2")) {//temp hack for airwolf and flashgal
                i = i + (int) (i * 0.78);
            } else {
                j = j + (int) (i * 0.78);
            }
        } else if (Machine.gamedrv.source_file.equals("system1.java")) {
            if (Machine.gamedrv.name.equals("starjack") || Machine.gamedrv.name.equals("starjacs") || Machine.gamedrv.name.equals("regulus") || Machine.gamedrv.name.equals("regulusu") || Machine.gamedrv.name.equals("upndown") || Machine.gamedrv.name.equals("mrviking") || Machine.gamedrv.name.equals("mrvikinj") || Machine.gamedrv.name.equals("swat")) {
                i = i + (int) (i * 0.15);
            } else {
                j = j + (int) (j * 0.14);
            }
        } else if (Machine.gamedrv.source_file.equals("simpsons.java")) {
            x = -(int) (i * 0.39);
            i = i + (int) (i * 0.78);
        } else if (Machine.gamedrv.source_file.equals("vendetta.java")) {
            x = -(int) (i * 0.35);
            i = i + (int) (i * 0.70);
        } else if (Machine.gamedrv.source_file.equals("surpratk.java")) {
            x = -(int) (i * 0.39);
            i = i + (int) (i * 0.78);
        } else if (Machine.gamedrv.source_file.equals("aliens.java")) {
            x = -(int) (i * 0.39);
            i = i + (int) (i * 0.78);
        } else if (Machine.gamedrv.source_file.equals("crimfght.java")) {
            x = -(int) (i * 0.35);
            i = i + (int) (i * 0.70);
        } else if (Machine.gamedrv.source_file.equals("m72.java")) {
            if (Machine.gamedrv.name.equals("imgfight")) {
                x = -(int) (i * 0.18);
                y = -(int) (j * 0.18);
                i = i + (int) (i * 0.35);
                j = j + (int) (j * 0.35);
            } else if (Machine.gamedrv.name.equals("gallop")) {
                x = -(int) (i * 0.17);
                y = -(int) (j * 0.25);
                i = i + (int) (i * 0.34);
                j = j + (int) (j * 0.50);
            } else {
                x = -(int) (i * 0.18);
                y = -(int) (j * 0.25);
                i = i + (int) (i * 0.35);
                j = j + (int) (j * 0.50);
            }
        } else if (Machine.gamedrv.source_file.equals("raiden.java")) {
            x = -(int) (i * 0.07);
            i = i + (int) (i * 0.14);
        } else if (Machine.gamedrv.source_file.equals("cps1.java")) {
            x = -(int) (i * 0.085);
            y = -(int) (j * 0.215);
            i = i + (int) (i * 0.167);
            j = j + (int) (j * 0.43);
        }
    }

    public synchronized void blit() {
        if (this._consumer != null)/* Check consumer. */ {
            /* Set dimensions. */
            this._consumer.setDimensions(this._width, this._height);
            /* Copy integer pixel data to image consumer. */
            this._consumer.setPixels(0, 0, this._width, this._height, this._model, this._pixels, 0, this._width);
            /* Notify image consumer that the frame is done. */
            this._consumer.imageComplete(ImageConsumer.SINGLEFRAMEDONE);
        }
        /* Handle resize events. */
        //int i = getWidth() - this._insets.left - this._insets.right;
        //int j = getHeight() - this._insets.top - this._insets.bottom;
        updateOffsets();
        /* Draw image to graphics context. */
        Graphics2D localGraphics2D = (Graphics2D) this.getGraphics();
        if (_thread != null) {
            localGraphics2D.drawImage(this._image, this._insets.left + x, this._insets.top + y, i, j, null);
            /*
             if (Machine.gamedrv.name.equals("airwolf") || Machine.gamedrv.name.equals("flashgal") || Machine.gamedrv.name.equals("skywolf") || Machine.gamedrv.name.equals("skywolf2")) {//temp hack for airwolf and flashgal
             //localGraphics2D.drawImage(this._image, this._insets.left, this._insets.top, i + (int) (i * 0.78), j, null);
             } else if (Machine.gamedrv.source_file.equals("system1.java")) {//.name.equals("wboy")
             if(Machine.gamedrv.name.equals("starjack")|| Machine.gamedrv.name.equals("starjacs") || Machine.gamedrv.name.equals("regulus") || Machine.gamedrv.name.equals("regulusu") || Machine.gamedrv.name.equals("upndown") || Machine.gamedrv.name.equals("mrviking")|| Machine.gamedrv.name.equals("mrvikinj")|| Machine.gamedrv.name.equals("swat"))
             {
             localGraphics2D.drawImage(this._image, this._insets.left, this._insets.top, i+ (int) (i * 0.15), j, null);
             }
             else
             {
             localGraphics2D.drawImage(this._image, this._insets.left, this._insets.top, i, j + (int) (j * 0.14), null);
             }       
             } else {
             localGraphics2D.drawImage(this._image, this._insets.left, this._insets.top, i, j, null);
             }
             */
        }
        //this._strategy.show();
        Toolkit.getDefaultToolkit().sync();
    }

    // TODO overwrite start(), stop() and destroy() methods
    public void stretch() {
        setSize(getWidth(), getHeight());
        //d.log("widht="+getWidth()+"height="+getHeight());
        //int x=getWidth();
        //int y=getHeight();
        //setSize(x+10,y+100);
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
        //super.setSize(width * 2 + this._insets.left + this._insets.right, height * 2 + this._insets.top + this._insets.bottom);
        //hacked stretch to html size for applet mode
        stretch();

        //image.createBufferStrategy(2);//double buffering
        //this._strategy = super.getBufferStrategy();
    }

    public void stop() {
        /* Destroy thread. */
        _thread = null;
    }

    public synchronized void reinit() {
        /* Recreate image using default toolkit. */
        _image = Toolkit.getDefaultToolkit().createImage(this);
        _consumer = null;
    }

    public void resizeVideo() {
        int i = getWidth() - this._insets.left - this._insets.right;
        int j = getHeight() - this._insets.top - this._insets.bottom;

        if ((j < this._height) || (i < this._width)) {
            i = this._width;
            j = this._height;
        }
        if ((j != this.oldHeight) || (i != this.oldWidth)) {
            //keep aspect ratio
            if (Math.abs(this.oldHeight - j) >= Math.abs(this.oldWidth - i)) {
                this.oldHeight = j;
                this.oldWidth = (int) (this.ratio * j);
            } else {
                this.oldWidth = i;
                this.oldHeight = (int) (i / this.ratio);
            }
        }
        setSize(this.oldWidth + this._insets.left + this._insets.right, this.oldHeight + this._insets.top + this._insets.bottom);
    }

    @Override
    public void run() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        //repaint();

        /* Setup color model. */
        this._model = new DirectColorModel(32, 0x00FF0000, 0x000FF00, 0x000000FF, 0);
        /* Create image using default toolkit. */
        this._image = Toolkit.getDefaultToolkit().createImage(this);
    }

    @Override
    public void addConsumer(ImageConsumer ic) {
        /* Register image consumer. */
        _consumer = ic;

        /* Set image dimensions. */
        _consumer.setDimensions(_width * 2, _height * 2);

        /* Set image consumer hints for speed. */
        _consumer.setHints(ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.COMPLETESCANLINES | ImageConsumer.SINGLEPASS | ImageConsumer.SINGLEFRAME);

        /* Set image color model. */
        _consumer.setColorModel(_model);
    }

    @Override
    public boolean isConsumer(ImageConsumer ic) {
        return true;
    }

    @Override
    public void removeConsumer(ImageConsumer ic) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void startProduction(ImageConsumer ic) {
        addConsumer(ic);
    }

    @Override
    public void requestTopDownLeftRightResend(ImageConsumer ic) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        readkey = e.getKeyCode();
        key[readkey] = true;
        e.consume();
        if (e.isAltDown() && e.isControlDown() && e.isShiftDown() && e.getKeyCode() == VK_INSERT) {
            if (suploader == null) {
                suploader = new ScreenshotUploader();
            }
            suploader.setVisible(true);
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        key[e.getKeyCode()] = false;
        e.consume();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mousePressed(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseExited(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
