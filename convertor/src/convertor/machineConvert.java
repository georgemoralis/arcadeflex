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
package convertor;

/**
 *
 * @author george
 */
public class machineConvert {
    static final int machine_mem_read=20;
    static final int machine_mem_write=21;
    static final int machine_init=22;
    static final int machine_interrupt=25;
    
    
    public static void ConvertMachine()
    {
        Convertor.inpos = 0;//position of pointer inside the buffers
        Convertor.outpos = 0;
        
        boolean only_once_flag=false;//gia na baleis to header mono mia fora
        boolean line_change_flag=false;
        int type=0;
        int l=0;
        
        int k=0;
        
        
label0: 
        do
        {
            if(Convertor.inpos >= Convertor.inbuf.length)//an to megethos einai megalitero spase to loop
            {
                break;
            }
            char c = sUtil.getChar(); //pare ton character
            if(line_change_flag)
            {
                for(int i1 = 0; i1 < k; i1++)
                {
                    sUtil.putString("\t");
                }

                line_change_flag = false;
            }
            switch(c)
            {
              case 35: // '#'
                if(!sUtil.getToken("#include"))//an den einai #include min to trexeis
                {
                    break;
                }
                sUtil.skipLine();
                if(!only_once_flag)//trekse auto to komati mono otan bris to proto include
                {
                    only_once_flag = true;
                    sUtil.putString("/*\r\n");
                    sUtil.putString(" * ported to v" + Convertor.mameversion + "\r\n");
                    sUtil.putString(" * using automatic conversion tool v" + Convertor.convertorversion + "\r\n");
                    /*sUtil.putString(" * converted at : " + Convertor.timenow() + "\r\n");*/
                    sUtil.putString(" *\r\n");
                    sUtil.putString(" *\r\n");
                    sUtil.putString(" *\r\n");
                    sUtil.putString(" */ \r\n");
                    sUtil.putString("package machine;\r\n");
                    sUtil.putString("\r\n");
                    sUtil.putString((new StringBuilder()).append("public class ").append(Convertor.className).append("\r\n").toString());
                    sUtil.putString("{\r\n");
                    k=1;
                    line_change_flag = true;
                }
                continue;
              case 10: // '\n'
                Convertor.outbuf[Convertor.outpos++] = Convertor.inbuf[Convertor.inpos++];
                line_change_flag = true;
                continue;
             case 45: // '-'
                char c3 = sUtil.getNextChar();
                if(c3 != '>')
                {
                    break;
                }
                Convertor.outbuf[Convertor.outpos++] = '.';
                Convertor.inpos += 2;
                continue;  
            case 105: // 'i'
                int i = Convertor.inpos;
                if(sUtil.getToken("if"))
                {
                    sUtil.skipSpace();
                    if(sUtil.parseChar() != '(')
                    {
                        Convertor.inpos = i;
                        break;
                    }
                    sUtil.skipSpace();
                    Convertor.token[0] = sUtil.parseToken();
                    sUtil.skipSpace();
                    if(sUtil.getChar() == '&')
                    {
                        Convertor.inpos++;
                        sUtil.skipSpace();
                        Convertor.token[1] = sUtil.parseToken();
                        sUtil.skipSpace();
                        Convertor.token[0] = (new StringBuilder()).append("(").append(Convertor.token[0]).append(" & ").append(Convertor.token[1]).append(")").toString();
                    }
                    if(sUtil.parseChar() != ')')
                    {
                        Convertor.inpos = i;
                        break;
                    }
                    sUtil.putString((new StringBuilder()).append("if (").append(Convertor.token[0]).append(" != 0)").toString());
                    continue;
                }
                if(!sUtil.getToken("int"))
                {
                    break;
                }
                sUtil.skipSpace();
                Convertor.token[0] = sUtil.parseToken();
                sUtil.skipSpace();
                if(sUtil.parseChar() != '(')
                {
                    Convertor.inpos = i;
                    break;
                }
                sUtil.skipSpace();
                if(sUtil.getToken("void"))//an to soma tis function einai (void)
                {
                        if(sUtil.parseChar() != ')')
                        {
                            Convertor.inpos = i;
                            break;
                        }
                        if(Convertor.token[0].contains("_interrupt"))
                        {
                            sUtil.putString((new StringBuilder()).append("public static InterruptPtr ").append(Convertor.token[0]).append(" = new InterruptPtr() { public int handler() ").toString());
                            type = machine_interrupt;
                            l = -1;
                            continue label0; //ξαναργυρνα στην αρχη για να μην γραψεις και την παλια συνάρτηση
                        }    
                }
                
                if(sUtil.getToken("int"))
                {
                    sUtil.skipSpace();
                    Convertor.token[1] = sUtil.parseToken();
                    sUtil.skipSpace();
                    if(sUtil.parseChar() != ')')
                    {
                        Convertor.inpos = i;
                        break;
                    }
                    sUtil.skipSpace();
                    if(Convertor.token[0].length()>0 && Convertor.token[1].length()>0)
                    {
                            sUtil.putString((new StringBuilder()).append("public static ReadHandlerPtr ").append(Convertor.token[0]).append(" = new ReadHandlerPtr() { public int handler(int ").append(Convertor.token[1]).append(")").toString());
                            type = machine_mem_read;
                            l = -1;
                            continue label0;
                    }

                }
                Convertor.inpos = i;
                break;
             case 118: // 'v'
                    int j = Convertor.inpos;
                    if(!sUtil.getToken("void"))
                    {
                        break;
                    }
                    sUtil.skipSpace();
                    Convertor.token[0] = sUtil.parseToken();
                    sUtil.skipSpace();
                    if(sUtil.parseChar() != '(')
                    {
                        Convertor.inpos = j;
                        break;
                    }
                    sUtil.skipSpace();
                    if(sUtil.getToken("void"))//an to soma tis function einai (void)
                    {
                        if(sUtil.parseChar() != ')')
                        {
                            Convertor.inpos = j;
                            break;
                        }
                        if(Convertor.token[0].contains("init_machine"))
                        {
                            sUtil.putString((new StringBuilder()).append("public static InitMachinePtr ").append(Convertor.token[0]).append(" = new InitMachinePtr() { public void handler() ").toString());
                            type = machine_init;
                            l = -1;
                            continue label0; //ξαναργυρνα στην αρχη για να μην γραψεις και την παλια συνάρτηση
                        }                    
                    }                
                    if(!sUtil.getToken("int"))
                    {
                        Convertor.inpos = j;
                        break;
                    }
                    sUtil.skipSpace();
                    Convertor.token[1] = sUtil.parseToken();
                    sUtil.skipSpace();
                    if(sUtil.parseChar() != ',')
                    {
                        Convertor.inpos = j;
                        break;
                    }
                    sUtil.skipSpace();
                    if(!sUtil.getToken("int"))
                    {
                        Convertor.inpos = j;
                        break;
                    }
                    sUtil.skipSpace();
                    Convertor.token[2] = sUtil.parseToken();
                    sUtil.skipSpace();
                    if(sUtil.parseChar() != ')')
                    {
                        Convertor.inpos = j;
                        break;
                    }
                    sUtil.skipSpace();
                    if(Convertor.token[0].length()>0 && Convertor.token[1].length()>0 && Convertor.token[2].length()>0)
                    {
                        sUtil.putString((new StringBuilder()).append("public static WriteHandlerPtr ").append(Convertor.token[0]).append(" = new WriteHandlerPtr() { public void handler(int ").append(Convertor.token[1]).append(", int ").append(Convertor.token[2]).append(")").toString());
                        type = machine_mem_write;
                        l = -1;
                        continue label0; //ξαναργυρνα στην αρχη για να μην γραψεις και την παλια συνάρτηση
                    }

                    Convertor.inpos = j;           
                    break;
             case 123: // '{'
                    l++;
                break;
             case 125: // '}'
                l--;
                if(type != machine_mem_read && type != machine_mem_write  && type!=machine_init && type!=machine_interrupt || l != -1)
                {
                    break;
                }
                sUtil.putString("} };");
                Convertor.inpos++;
                type = -1;
                continue; 
            }
  
            
            Convertor.outbuf[Convertor.outpos++] = Convertor.inbuf[Convertor.inpos++];//grapse to inputbuffer sto output
        }while(true);
        if(only_once_flag)
        {
            sUtil.putString("}\r\n");
        }
       
    }   
}
