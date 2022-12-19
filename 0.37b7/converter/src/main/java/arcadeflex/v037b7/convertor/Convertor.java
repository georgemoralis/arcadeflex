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
package arcadeflex.v037b7.convertor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author george
 */
public class Convertor {

    static final String mameversion = "0.37b7";
    static final String convertorversion="0.01";
    static FileInputStream fInput; //input 
    static FileOutputStream fOutput;//output streams
    
    static byte inbuf[];
    static byte outbuf[];
    static int inpos;
    static int outpos;
    static String className;
    static String packageName;
    
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
         ArrayList<File> files = new ArrayList<>();
        listf("c_code",files);
        for(File f : files)
        {
            //System.out.println(f.getPath());
            if(f.getName().matches("makefile")) continue;
            String drivername=f.getName().substring(0, f.getName().lastIndexOf('.'));
            String getextension= f.getName().substring(f.getName().lastIndexOf('.'),f.getName().length());
            if(getextension.matches(".asm")) continue;
            if(getextension.matches(".mak")) continue;
            if(getextension.matches(".txt")) continue;
            String sourcepath=f.getPath().substring(0, f.getPath().lastIndexOf('\\'));
            String destpath = sourcepath.replace("c_code", "java_code");
            if((fInput = fileutil.openReadFile((new StringBuilder()).append(sourcepath).append("\\").append(drivername).append(getextension).toString())) == null)
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
                    if(getextension.matches(".h"))
                    {
                       className = makeDriverName(drivername) + "H"; 
                    }
                    else
                    {
                       className = makeDriverName(drivername);
                    }             
                    outbuf = new byte[k * 4];
                    
                    if(destpath.indexOf('\\')==-1)//if it's parent dir just place them into mame subdir
                    {
                        destpath= destpath+"\\mame";
                    }
                    packageName = destpath.replace("\\", ".");
                    packageName = packageName.replace("java_code.", "");
                    System.out.println("starting : " + packageName + " " + drivername);
                    convertMame.ConvertMame();
                    new File(destpath).mkdirs();
                    if((fOutput = fileutil.openWriteFile((new StringBuilder()).append(destpath).append("\\").append(className).append(".java").toString())) == null)
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
                    System.out.println("Converted : " + drivername + " DONE");
                    
                    
        }
        System.out.println("Done");
        //secondpass();
    }
    public void secondpass()
    {
        ArrayList<File> files = new ArrayList<>();
        listf("java_code",files);
        for(File f : files)
        {
            //System.out.println(f.getPath());
            //if(f.getName().matches("makefile")) continue;
            String drivername=f.getName().substring(0, f.getName().lastIndexOf('.'));
            String getextension= f.getName().substring(f.getName().lastIndexOf('.'),f.getName().length());
            //if(getextension.matches(".asm")) continue;
            //if(getextension.matches(".mak")) continue;
           // if(getextension.matches(".txt")) continue;
            String sourcepath=f.getPath().substring(0, f.getPath().lastIndexOf('\\'));
            String destpath = sourcepath;//.replace("c_code", "java_code");
            if((fInput = fileutil.openReadFile((new StringBuilder()).append(sourcepath).append("\\").append(drivername).append(getextension).toString())) == null)
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

            outbuf = new byte[k * 4];

            if(destpath.indexOf('\\')==-1)//if it's parent dir just place them into mame subdir
            {
                destpath= destpath+"\\mame";
            }
            packageName = destpath.replace("\\", ".");
            packageName = packageName.replace("java_code.", "");
            System.out.println("starting : " + packageName + " " + drivername);
            convertMame.ConvertMame();
            new File(destpath).mkdirs();
            if((fOutput = fileutil.openWriteFile((new StringBuilder()).append(destpath).append("\\").append(className).append(".java").toString())) == null)
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
            System.out.println("Converted : " + drivername + " DONE");


        }
        System.out.println("Done");
    }

    public void listf(String directoryName, ArrayList<File> files) {
        File directory = new File(directoryName);

        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                //System.out.println(file.getAbsolutePath());
                listf(file.getPath(), files);
            }
        }
    }
}
