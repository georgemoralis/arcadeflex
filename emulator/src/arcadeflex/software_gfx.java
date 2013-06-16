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


package arcadeflex;

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

public class software_gfx extends java.awt.Frame implements Runnable, ImageProducer, KeyListener, MouseListener, MouseMotionListener
{
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
    Thread _thread;
    ImageConsumer _consumer;
    DirectColorModel _model;
    BufferStrategy _strategy;

	public software_gfx(String title)
	{
		super(title);
	}

	public synchronized void blit()
        {
                if (this._consumer != null)/* Check consumer. */
                {
                    /* Set dimensions. */
                   this._consumer.setDimensions(this._width, this._height);
                   /* Copy integer pixel data to image consumer. */
                   this._consumer.setPixels(0, 0, this._width, this._height, this._model, this._pixels, 0, this._width);
                   /* Notify image consumer that the frame is done. */
                   this._consumer.imageComplete(ImageConsumer.SINGLEFRAMEDONE);
                }
                /* Handle resize events. */
                int i = getWidth() - this._insets.left - this._insets.right;
                int j = getHeight() - this._insets.top - this._insets.bottom;
                /* Draw image to graphics context. */
                Graphics2D localGraphics2D = (Graphics2D)this._strategy.getDrawGraphics();
                localGraphics2D.drawImage(this._image, this._insets.left, this._insets.top, i, j, null);
                this._strategy.show();
                Toolkit.getDefaultToolkit().sync();
        }

    public void start()
    {
        /* Check if thread exists. */
        if (_thread != null)
			return;

		/* Create and start thread. */
        _thread = new Thread(this);
        _thread.start();
    }

	public synchronized void setSize(boolean scanlines, int width, int height)
	{

                /* Setup options. */

		_scanlines = scanlines;

                 /* Setup pixel buffer and dimensions. */
		ratio = ((float) width) / ((float) height);
                _width = width;
                if (_scanlines)
		{
			width *= 2;
			height *= 2;
		}
		_height = height;
                _pixels = new int[_width * _height];
                 /* Setup frame dimensions. */
		_insets = getInsets();
                 super.setSize(width+ this._insets.left + this._insets.right, height + this._insets.top + this._insets.bottom);
                 super.createBufferStrategy(2);//double buffering
                 this._strategy = super.getBufferStrategy();
        }

	public void resizeVideo()
	{
             int i = getWidth() - this._insets.left - this._insets.right;
             int j = getHeight() - this._insets.top - this._insets.bottom;

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
            setSize(this.oldWidth + this._insets.left + this._insets.right, this.oldHeight + this._insets.top + this._insets.bottom);
	}
        public void run()
        {
            /* Setup color model. */
             this._model = new DirectColorModel(32, 0x00FF0000, 0x000FF00, 0x000000FF, 0);
            /* Create image using default toolkit. */
             this._image = Toolkit.getDefaultToolkit().createImage(this);
        }


	public synchronized void reinit()
	{
        /* Recreate image using default toolkit. */
        _image = Toolkit.getDefaultToolkit().createImage(this);
		_consumer = null;
	}

         public void stop()
        {
            /* Destroy thread. */
            _thread = null;
        }

    public synchronized void addConsumer(ImageConsumer ic)
    {
        /* Register image consumer. */
        _consumer = ic;

        /* Set image dimensions. */
        _consumer.setDimensions(_width, _height);

        /* Set image consumer hints for speed. */
        _consumer.setHints(ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.COMPLETESCANLINES | ImageConsumer.SINGLEPASS | ImageConsumer.SINGLEFRAME);

        /* Set image color model. */
        _consumer.setColorModel(_model);
    }

    public synchronized boolean isConsumer(ImageConsumer ic)
    {
        /* Check if consumer is registered. */
        return true;
    }

    public synchronized void removeConsumer(ImageConsumer ic)
    {
        /* Remove image consumer. */
    }

    public void startProduction(ImageConsumer ic)
    {
        /* Add consumer. */
        addConsumer(ic);
    }

    public void requestTopDownLeftRightResend(ImageConsumer ic)
    {
        /* Ignore resend request. */
    }

	/** Handle the key pressed event from the text field. */
    public void keyPressed(KeyEvent e)
	{
		readkey = e.getKeyCode();
		key[readkey] = true;
		e.consume();
    }

    /** Handle the key released event from the text field. */
    public void keyTyped(KeyEvent e)
	{
    }

    /** Handle the key released event from the text field. */
    public void keyReleased(KeyEvent e)
	{
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
