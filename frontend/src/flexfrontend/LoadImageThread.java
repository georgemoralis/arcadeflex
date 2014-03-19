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
package flexfrontend;

import java.awt.MediaTracker;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 *
 * @author nickblame
 */
public class LoadImageThread extends Thread {

    String filename;
    JLabel label;

    public LoadImageThread(String filename, JLabel label) {
        this.label = label;
        this.filename = filename;
    }

    @Override
    public void run() {
        label.setIcon(null);
        label.setText("loading..");
        loadFlyer();
    }

    private void loadFlyer() {
        if (Settings.netMode) {
            URL u = null;
            try {
                //test flyer load
                u = new URL("http://arcadeflex.com/" + filename);
            } catch (MalformedURLException ex) {
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
                label.setText("failed");
            }
            ImageIcon icon = new ImageIcon(u);
            //flyerLabel.setSize(m.getIconHeight(),m.getIconWidth());
            if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                label.setText("");
                label.setIcon(icon);
            } else {
                label.setText("no icon");
            }
        } else {
            ImageIcon icon = new ImageIcon(filename);
            if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                label.setText("");
                label.setIcon(icon);
            } else {
                label.setText("no icon");
            }
        }

    }
}
