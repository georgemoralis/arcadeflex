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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 *
 * @author nickblame
 */
public class RunArcadeflexThread extends Thread {

    JTextArea ta;
    String cmd,game;

    public RunArcadeflexThread(String game, String cmd, JTextArea ta) {
        this.cmd = cmd;
        this.game=game;
        this.ta = ta;

    }

    @Override
    public void run() {
        // Set up list to capture command output lines
        //ArrayList list = new ArrayList();

        // Start command running
        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec(cmd);
        } catch (IOException ex) {
            Logger.getLogger(RunArcadeflexThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Get command's output stream and
        // put a buffered reader input stream on it
        InputStream istr = proc.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(istr));

        // Read output lines from command
        String str;
        try {
            while ((str = br.readLine()) != null) {
                ta.append("{"+game+" output:} "+str+"\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(RunArcadeflexThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Wait for command to terminate
        try {
            proc.waitFor();
        } catch (InterruptedException e) {
            System.err.println("Process was interrupted");
        }

        // Check its exit value
        if (proc.exitValue() != 0) {
            System.err.println("Exit value was non-zero");
        }
        try {
            // Close stream
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(RunArcadeflexThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Return list of strings to caller
        //return (String[]) list.toArray(new String[0]);
    }
}
