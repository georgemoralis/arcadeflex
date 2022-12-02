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
package arcadeflex.v036.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author george
 */
public class Convertor {

    static final String mameversion = "0.36";
    static final String convertorversion="0.10";
    static FileInputStream fInput; //input 
    static FileOutputStream fOutput;//output streams
    
    static byte inbuf[];
    static byte outbuf[];
    static int inpos;
    static int outpos;
    static String className;
    
    static String token[] = new String[1000];
    
   public static final String DATE_FORMAT_NOW = "dd-MM-yyyy HH:mm:ss";

   public static String timenow() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());

    }
  
    public static String makeDriverName(String s)
    {
        s = s.toLowerCase();
        if(s.charAt(0) >= '0' && s.charAt(0) <= '9')
        {
            s = (new StringBuilder()).append("_").append(s).toString();
        }
        return s;
    }
    
    //public static void main(String[] args) {
     public Convertor() {
            if(ConvertGUI.alldrivers)//all drivers found
            {
                File d = new File("c_code\\drivers\\");
                File[] contents= d.listFiles();
                for(int i=0; i<contents.length; i++)
                {
                    if(contents[i].getName().startsWith(".")) continue; //if .svn exists ignore it
                    if(contents[i].getName().startsWith("deleteme")) continue;
                    String drivername=contents[i].getName().substring(0, contents[i].getName().lastIndexOf('.'));
                    System.out.println("Convert driver : " + drivername);
                    if((fInput = fileutil.openReadFile((new StringBuilder()).append("c_code\\drivers\\").append(drivername).append(".c").toString())) == null)
                    {
                        return;
                    }
                    int k;
                    if((k = fileutil.getFileSize(fInput)) == -1)
                    {
                        return;
                    }
                    inbuf = new byte[k];
                    if(fileutil.readFile(fInput, inbuf) != k)
                    {
                        return;
                    }
                    if(!fileutil.closeReadFile(fInput))
                    {
                        return;
                    }
                    className = makeDriverName(drivername);
                    outbuf = new byte[k * 2];
                    driverConvert.Convertdriver();
                    if((fOutput = fileutil.openWriteFile((new StringBuilder()).append("java_code\\drivers\\").append(className).append(".java").toString())) == null)
                    {
                        return;
                    }
                    if(!fileutil.writeFile(fOutput, outbuf, outpos))
                    {
                        return;
                    }
                    if(!fileutil.closeWriteFile(fOutput))
                    {
                        return;
                    }
                    System.out.println("Convert driver : " + drivername + " DONE");
                }
                
            }
            if(ConvertGUI.allmachines)//all machines found
            {
                File d = new File("c_code\\machine\\");
                File[] contents= d.listFiles();
                for(int i=0; i<contents.length; i++)
                {
                    if(contents[i].getName().startsWith(".")) continue; //if .svn exists ignore it
                    if(contents[i].getName().startsWith("deleteme")) continue;
                    String drivername=contents[i].getName().substring(0, contents[i].getName().lastIndexOf('.'));
                    System.out.println("Convert machine : " + drivername);
                    if((fInput = fileutil.openReadFile((new StringBuilder()).append("c_code\\machine\\").append(drivername).append(".c").toString())) == null)
                    {
                        return;
                    }
                    int k;
                    if((k = fileutil.getFileSize(fInput)) == -1)
                    {
                        return;
                    }
                    inbuf = new byte[k];
                    if(fileutil.readFile(fInput, inbuf) != k)
                    {
                        return;
                    }
                    if(!fileutil.closeReadFile(fInput))
                    {
                        return;
                    }
                    className = makeDriverName(drivername);
                    outbuf = new byte[k * 2];
                    machineConvert.ConvertMachine();
                    if((fOutput = fileutil.openWriteFile((new StringBuilder()).append("java_code\\machine\\").append(className).append(".java").toString())) == null)
                    {
                        return;
                    }
                    if(!fileutil.writeFile(fOutput, outbuf, outpos))
                    {
                        return;
                    }
                    if(!fileutil.closeWriteFile(fOutput))
                    {
                        return;
                    }
                    System.out.println("Convert machine : " + drivername + " DONE");
                
                }
            }
            if(ConvertGUI.allvideos)//all videodrv found
            {
                File d = new File("c_code\\vidhrdw\\");
                File[] contents= d.listFiles();
                for(int i=0; i<contents.length; i++)
                {
                    if(contents[i].getName().startsWith(".")) continue; //if .svn exists ignore it
                    if(contents[i].getName().startsWith("deleteme")) continue;
                    String drivername=contents[i].getName().substring(0, contents[i].getName().lastIndexOf('.'));
                    System.out.println("Convert vidhrdw : " + drivername);
                    if((fInput = fileutil.openReadFile((new StringBuilder()).append("c_code\\vidhrdw\\").append(drivername).append(".c").toString())) == null)
                    {
                        return;
                    }
                    int k;
                    if((k = fileutil.getFileSize(fInput)) == -1)
                    {
                        return;
                    }
                    inbuf = new byte[k];
                    if(fileutil.readFile(fInput, inbuf) != k)
                    {
                        return;
                    }
                    if(!fileutil.closeReadFile(fInput))
                    {
                        return;
                    }
                    className = makeDriverName(drivername);
                    outbuf = new byte[k * 2];
                    vidConvert.ConvertVideo();
                    if((fOutput = fileutil.openWriteFile((new StringBuilder()).append("java_code\\vidhrdw\\").append(className).append(".java").toString())) == null)
                    {
                        return;
                    }
                    if(!fileutil.writeFile(fOutput, outbuf, outpos))
                    {
                        return;
                    }
                    if(!fileutil.closeWriteFile(fOutput))
                    {
                        return;
                    }
                    System.out.println("Convert vidhrdw : " + drivername + " DONE");
                }      
            }
            if(ConvertGUI.allsounds)//all sounddrv found
            {
                File d = new File("c_code\\sndhrdw\\");
                File[] contents= d.listFiles();
                for(int i=0; i<contents.length; i++)
                {
                    if(contents[i].getName().startsWith(".")) continue; //if .svn exists ignore it
                    if(contents[i].getName().startsWith("deleteme")) continue;
                    String drivername=contents[i].getName().substring(0, contents[i].getName().lastIndexOf('.'));
                    System.out.println("Convert sndhrdw : " + drivername);
                    if((fInput = fileutil.openReadFile((new StringBuilder()).append("c_code\\sndhrdw\\").append(drivername).append(".c").toString())) == null)
                    {
                        return;
                    }
                    int k;
                    if((k = fileutil.getFileSize(fInput)) == -1)
                    {
                        return;
                    }
                    inbuf = new byte[k];
                    if(fileutil.readFile(fInput, inbuf) != k)
                    {
                        return;
                    }
                    if(!fileutil.closeReadFile(fInput))
                    {
                        return;
                    }
                    className = makeDriverName(drivername);
                    outbuf = new byte[k * 2];
                    sndConvert.ConvertSound();
                    if((fOutput = fileutil.openWriteFile((new StringBuilder()).append("java_code\\sndhrdw\\").append(className).append(".java").toString())) == null)
                    {
                        return;
                    }
                    if(!fileutil.writeFile(fOutput, outbuf, outpos))
                    {
                        return;
                    }
                    if(!fileutil.closeWriteFile(fOutput))
                    {
                        return;
                    }
                    System.out.println("Convert sndhrdw : " + drivername + " DONE");
                }
            }
            
        
    }
}
