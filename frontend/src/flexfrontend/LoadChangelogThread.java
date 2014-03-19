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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JTextArea;

/**
 *
 * @author nickblame
 */
public class LoadChangelogThread extends Thread {

    String filename;
    JTextArea changelog;

    public LoadChangelogThread(String filename, JTextArea changelog) {
        this.changelog = changelog;
        this.filename = filename;
    }

    @Override
    public void run() {
        //System.out.println("loading..");
        loadText();
    }

    private void loadText() {
        if (Settings.netMode) {
            
        } else {
            changelog.setText("");
            String fFileName=filename+"_changelog.txt";
                File file = new File(fFileName);
                StringBuffer contents = new StringBuffer();
                BufferedReader reader = null;

                try {
                    reader = new BufferedReader(new FileReader(file));
                    String text = null;

                    // repeat until all lines is read
                    while ((text = reader.readLine()) != null) {
                        contents.append(text)
                            .append(System.getProperty(
                                "line.separator"));
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // show file contents here
                changelog.setText("");
                changelog.append(contents.toString());
            }
            
        }
    

}
