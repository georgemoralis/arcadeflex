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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author nickblame
 */
public class LoadTextThread extends Thread {

    String filename;
    JTextArea descr;
    private final JTextField cpu,audio;

    public LoadTextThread(String filename, JTextArea descr, JTextField cpu, JTextField audio) {
        this.descr = descr;
        this.cpu=cpu;
        this.audio=audio;
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
            //System.out.println(filename);
            try {
                String ini=deserializeString(new File(filename));
                String[] ass=ini.split("\\r?\\n");

                descr.setText(findTag(ass,"[description]"));
                cpu.setText(findTag(ass,"[cpu]"));
                audio.setText(findTag(ass,"[audio]"));
            } catch (IOException ex) {
                //Logger.getLogger(LoadTextThread.class.getName()).log(Level.SEVERE, null, ex);
                //System.out.println("loading info file failure");
                descr.setText("no data");
                cpu.setText("no data");
                audio.setText("no data");
            }
        }
    }

    public String findTag(String[] text,String tag){
        String out="";
        for(int i=0;i<text.length;i++){
            if(text[i].contains(tag)){
                if(i+1<text.length){
                    out=text[i+1];
                }
            }
        }
        return out;
    }

    public String deserializeString(File file) throws IOException {
        int len;
        char[] chr = new char[4096];
        final StringBuffer buffer = new StringBuffer();
        final FileReader reader = new FileReader(file);
        try {
            while ((len = reader.read(chr)) > 0) {
                buffer.append(chr, 0, len);
            }
        } finally {
            reader.close();
        }
        return buffer.toString();
    }
}
