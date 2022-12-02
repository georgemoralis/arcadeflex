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

/**
 *
 * @author george
 */
public class driverConvert {

    //for type field

    static final int GAMEDRIVER = 0;
    static final int MACHINEDRIVER = 1;
    static final int MEMORYREAD = 2;
    static final int MEMORYWRITE = 3;
    static final int IOREAD = 4;
    static final int IOWRITE = 5;
    static final int INPUTPORT = 6;
    static final int GFXLAYOUT = 10;
    static final int GFXDECODE = 11;
    static final int HILOAD = 12;
    static final int HISAVE = 13;
    static final int DRIVER_INIT = 14;
    static final int MEM_READ = 15;
    static final int MEM_WRITE = 16;
    static final int MACHINE_INTERRUPT = 17;
    static final int SN76496interface = 18;
    static final int DACinterface = 19;
    static final int MACHINE_INIT = 20;

    //type2 fields
    static final int NEWINPUT = 12;
    static final int ROMDEF = 13;

    public static void Convertdriver() {
        Convertor.inpos = 0;//position of pointer inside the buffers
        Convertor.outpos = 0;

        boolean only_once_flag = false;//gia na baleis to header mono mia fora
        boolean line_change_flag = false;

        int k = 0;
        int i = 0;
        int type = 0;
        int type2 = 0;
        int i3 = -1;

        int[] insideagk = new int[10];//get the { that are inside functions
        int i8 = -1; //for checking ) in INPUT PORTS and ROM macros
        do {
            if (Convertor.inpos >= Convertor.inbuf.length)//an to megethos einai megalitero spase to loop
            {
                break;
            }
            char c = sUtil.getChar(); //pare ton character
            if (line_change_flag) {
                for (int i1 = 0; i1 < k; i1++) {
                    sUtil.putString("\t");
                }

                line_change_flag = false;
            }
            switch (c) {
                case 35: // '#'
                    if (!sUtil.getToken("#include"))//an den einai #include min to trexeis
                    {
                        break;
                    }
                    sUtil.skipLine();
                    if (!only_once_flag)//trekse auto to komati mono otan bris to proto include
                    {
                        only_once_flag = true;
                        sUtil.putString("/*\r\n");
                        sUtil.putString(" * ported to v" + Convertor.mameversion + "\r\n");
                        sUtil.putString(" * using automatic conversion tool v" + Convertor.convertorversion + "\r\n");
                        /*sUtil.putString(" * converted at : " + Convertor.timenow() + "\r\n");*/
                        sUtil.putString(" */ \r\n");
                        sUtil.putString("package drivers;\r\n");
                        sUtil.putString("\r\n");
                        sUtil.putString((new StringBuilder()).append("public class ").append(Convertor.className).append("\r\n").toString());
                        sUtil.putString("{\r\n");
                        k = 1;
                        line_change_flag = true;
                    }
                    continue;
                case 10: // '\n'
                    Convertor.outbuf[Convertor.outpos++] = Convertor.inbuf[Convertor.inpos++];
                    line_change_flag = true;
                    continue;
                case 'I':
                    int j = Convertor.inpos;
                    if (sUtil.getToken("INPUT_PORTS_START")) {
                        if (sUtil.parseChar() != '(') {
                            Convertor.inpos = j;
                            break;
                        }
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ')') {
                            Convertor.inpos = j;
                            break;
                        }
                        sUtil.putString((new StringBuilder()).append("static InputPortPtr input_ports_").append(Convertor.token[0]).append(" = new InputPortPtr(){ public void handler() { ").toString());
                    }
                    if (sUtil.getToken("INPUT_PORTS_END")) {
                        sUtil.putString((new StringBuilder()).append("INPUT_PORTS_END(); }}; ").toString());
                        continue;
                    }

                    break;
                case 'P':
                    if (sUtil.getToken("PORT_START")) {
                        sUtil.putString((new StringBuilder()).append("PORT_START(); ").toString());
                        continue;
                    }
                    int h = Convertor.inpos;
                    if (sUtil.getToken("PORT_DIPNAME") || sUtil.getToken("PORT_BIT") || sUtil.getToken("PORT_DIPSETTING") || sUtil.getToken("PORT_BITX") || sUtil.getToken("PORT_SERVICE") || sUtil.getToken("PORT_BIT_IMPULSE") || sUtil.getToken("PORT_ANALOG")) {
                        i8++;
                        type2 = NEWINPUT;
                        sUtil.skipSpace();
                        if (sUtil.parseChar() == '(') {
                            Convertor.inpos = h;
                        }
                    }
                    break;
                case 'G':
                    if (sUtil.getToken("GAME(") || sUtil.getToken("GAMEX(")) {
                        type = GAMEDRIVER;
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();//year
                        Convertor.inpos++;
                        sUtil.skipSpace();
                        Convertor.token[1] = sUtil.parseToken();//rom
                        Convertor.inpos++;
                        sUtil.skipSpace();
                        Convertor.token[2] = sUtil.parseToken();//parent
                        if (Convertor.token[2].matches("0")) {
                            Convertor.token[2] = "null";
                        } else {
                            Convertor.token[2] = "driver_" + Convertor.token[2];
                        }
                        Convertor.inpos++;
                        sUtil.skipSpace();
                        Convertor.token[3] = sUtil.parseToken();//machine
                        Convertor.inpos++;
                        sUtil.skipSpace();
                        Convertor.token[4] = sUtil.parseToken();//input
                        Convertor.inpos++;
                        sUtil.skipSpace();
                        Convertor.token[5] = sUtil.parseToken();//init
                        if (Convertor.token[5].matches("0")) {
                            Convertor.token[5] = "null";
                        } else {
                            Convertor.token[5] = "init_" + Convertor.token[5];
                        }
                        Convertor.inpos++;
                        sUtil.skipSpace();
                        Convertor.token[6] = sUtil.parseToken();//ROT
                        //Convertor.inpos++;
                        sUtil.skipSpace();
                        Convertor.token[7] = sUtil.parseToken();//comp
                        Convertor.inpos++;
                        sUtil.skipSpace();
                        Convertor.token[8] = sUtil.parseToken();//name
                        //public static GameDriver driver_ladybug  = new GameDriver("1981","ladybug"  ,"ladybug.java", rom_ladybug,  null,       machine_driver_ladybug, input_ports_ladybug,  null, ROT270, "Universal", "Lady Bug" );
                        sUtil.putString((new StringBuilder()).append("public static GameDriver driver_").append(Convertor.token[1]).append("\t   = new GameDriver(\"").append(Convertor.token[0]).append("\"\t,\"").append(Convertor.token[1]).append("\"\t,\"").append(Convertor.className).append(".java\"\t,rom_")
                                .append(Convertor.token[1]).append(",").append(Convertor.token[2])
                                .append("\t,machine_driver_").append(Convertor.token[3])
                                .append("\t,input_ports_").append(Convertor.token[4])
                                .append("\t,").append(Convertor.token[5])
                                .append("\t,").append(Convertor.token[6])
                                .append("\t,").append(Convertor.token[7])
                                .append("\t").append(Convertor.token[8])
                                .toString());
                        continue;
                    }
                    /*if(sUtil.getToken("GAMEX("))
                     {
                     sUtil.skipSpace();
                     Convertor.token[0] = sUtil.parseToken();//year
                     Convertor.inpos++;
                     sUtil.skipSpace();
                     Convertor.token[1] = sUtil.parseToken();//rom
                     Convertor.inpos++;
                     sUtil.skipSpace();
                     Convertor.token[2] = sUtil.parseToken();//parent
                     if(Convertor.token[2].matches("0")) Convertor.token[2]="null";
                     else Convertor.token[2] = "driver_" + Convertor.token[2];                    
                     Convertor.inpos++;
                     sUtil.skipSpace();
                     Convertor.token[3] = sUtil.parseToken();//machine
                     Convertor.inpos++;
                     sUtil.skipSpace();
                     Convertor.token[4] = sUtil.parseToken();//input
                     Convertor.inpos++;
                     sUtil.skipSpace();
                     Convertor.token[5] = sUtil.parseToken();//init
                     if(Convertor.token[5].matches("0")) Convertor.token[5]="null";
                     else Convertor.token[5] = "init_" + Convertor.token[5];            
                     Convertor.inpos++;
                     sUtil.skipSpace();
                     Convertor.token[6] = sUtil.parseToken();//ROT
                     //Convertor.inpos++;
                     sUtil.skipSpace();
                     Convertor.token[7] = sUtil.parseToken();//comp
                     Convertor.inpos++;
                     sUtil.skipSpace();
                     Convertor.token[8] = sUtil.parseToken();//name
                     Convertor.inpos++;
                     sUtil.skipSpace();
                     Convertor.token[9] = sUtil.parseToken();//flags       
                     sUtil.putString((new StringBuilder()).append("public static GameDriver driver_").append(Convertor.token[1]).append("\t   = new GameDriver(\"").append(Convertor.token[0]).append("\"\t,\"").append(Convertor.token[1]).append("\"\t,\"").append(Convertor.className).append(".java\"\t,rom_")
                     .append(Convertor.token[1]).append(",").append(Convertor.token[2])
                     .append("\t,machine_driver_").append(Convertor.token[3])
                     .append("\t,input_ports_").append(Convertor.token[4])
                     .append("\t,").append(Convertor.token[5])
                     .append("\t,").append(Convertor.token[6])
                     .append("\t,").append(Convertor.token[7])
                     .append("\t").append(Convertor.token[8])
                     .append("\t").append(Convertor.token[9])
                     .toString()); 
                     continue;
                     }*/
                    break;
                case 'R':
                    int r = Convertor.inpos;
                    if (sUtil.getToken("ROM_START")) {
                        if (sUtil.parseChar() != '(') {
                            Convertor.inpos = r;
                            break;
                        }
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ')') {
                            Convertor.inpos = r;
                            break;
                        }
                        sUtil.putString((new StringBuilder()).append("static RomLoadPtr rom_").append(Convertor.token[0]).append(" = new RomLoadPtr(){ public void handler(){ ").toString());
                    }
                    if (sUtil.getToken("ROM_END")) {
                        sUtil.putString((new StringBuilder()).append("ROM_END(); }}; ").toString());
                        continue;
                    }
                    if (sUtil.getToken("ROM_REGION") || sUtil.getToken("ROM_LOAD") || sUtil.getToken("ROM_RELOAD") || sUtil.getToken("ROM_CONTINUE") || sUtil.getToken("ROM_LOAD_GFX_ODD") || sUtil.getToken("ROM_LOAD_GFX_EVEN")) {
                        i8++;
                        type2 = ROMDEF;
                        sUtil.skipSpace();
                        if (sUtil.parseChar() == '(') {
                            Convertor.inpos = r;
                        }
                    }
                    break;
                case ')':
                    if (type2 == NEWINPUT) {
                        i8--;
                        Convertor.outbuf[(Convertor.outpos++)] = ')';
                        Convertor.outbuf[(Convertor.outpos++)] = ';';
                        Convertor.inpos += 2;
                        if (sUtil.getChar() == ')') {
                            Convertor.inpos += 1;
                        }
                        type2 = -1;
                        continue;
                    }
                    if (type2 == ROMDEF) {
                        i8--;
                        Convertor.outbuf[(Convertor.outpos++)] = ')';
                        Convertor.outbuf[(Convertor.outpos++)] = ';';
                        Convertor.inpos += 2;
                        type2 = -1;
                        continue;
                    }
                    break;
                case 'D':
                    if (type2 == NEWINPUT) {
                        i = Convertor.inpos;
                        if (sUtil.getToken("DEF_STR(")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.putString((new StringBuilder()).append("DEF_STR( \"").append(Convertor.token[0]).append("\")").toString());
                            i3 = -1;

                            continue;
                        }

                    }
                    break;
                case 's':
                    i = Convertor.inpos;
                    if (type == MEM_WRITE) {
                        if (sUtil.getToken("soundlatch_w")) {
                            sUtil.putString((new StringBuilder()).append("soundlatch_w.handler").toString());
                            continue;
                        }
                    }
                    if (sUtil.getToken("static")) {
                        sUtil.skipSpace();
                    }
                    if (!sUtil.getToken("struct")) //an einai static alla oxi static struct
                    {
                        if (sUtil.getToken("int")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            if (sUtil.parseChar() != '(') {
                                Convertor.inpos = i;
                                break;
                            }
                            sUtil.skipSpace();
                            if (sUtil.getToken("void"))//an to soma tis function einai (void)
                            {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ')') {
                                    Convertor.inpos = i;
                                    break;
                                }

                                if (Convertor.token[0].contains("_interrupt")) {
                                    sUtil.putString((new StringBuilder()).append("public static InterruptPtr ").append(Convertor.token[0]).append(" = new InterruptPtr() { public int handler() ").toString());
                                    type = MACHINE_INTERRUPT;
                                    i3 = -1;
                                    continue;
                                }

                            }
                            sUtil.skipSpace();
                            if (sUtil.getToken("int")) {
                                sUtil.skipSpace();
                                Convertor.token[1] = sUtil.parseToken();
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ')') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                sUtil.skipSpace();
                                if (Convertor.token[0].length() > 0 && Convertor.token[1].length() > 0) {
                                    sUtil.putString((new StringBuilder()).append("public static ReadHandlerPtr ").append(Convertor.token[0]).append(" = new ReadHandlerPtr() { public int handler(int ").append(Convertor.token[1]).append(")").toString());
                                    type = MEM_READ;
                                    i3 = -1;
                                    continue;
                                }

                            }
                            Convertor.inpos = i;
                            break;
                        }
                        if (sUtil.getToken("void")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            if (sUtil.parseChar() != '(') {
                                Convertor.inpos = i;
                                break;
                            }
                            sUtil.skipSpace();
                            if (sUtil.getToken("void"))//an to soma tis function einai (void)
                            {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ')') {
                                    Convertor.inpos = i;
                                    break;
                                }

                                if (Convertor.token[0].contains("hisave")) {
                                    sUtil.putString((new StringBuilder()).append("static HiscoreSavePtr ").append(Convertor.token[0]).append(" = new HiscoreSavePtr() { public void handler() ").toString());
                                    type = HISAVE;
                                    i3 = -1;
                                    continue;
                                }
                                if (Convertor.token[0].startsWith("init_")) {
                                    sUtil.putString((new StringBuilder()).append("public static InitDriverPtr ").append(Convertor.token[0]).append(" = new InitDriverPtr() { public void handler() ").toString());
                                    type = DRIVER_INIT;
                                    i3 = -1;
                                    continue;
                                }
                                if (Convertor.token[0].contains("_machine_init") || Convertor.token[0].contains("_init_machine")) {
                                    sUtil.putString((new StringBuilder()).append("public static InitMachinePtr ").append(Convertor.token[0]).append(" = new InitMachinePtr() { public void handler() ").toString());
                                    type = MACHINE_INIT;
                                    i3 = -1;
                                    continue;
                                }
                            }
                            if (!sUtil.getToken("int")) {
                                Convertor.inpos = i;
                                break;
                            }
                            sUtil.skipSpace();
                            Convertor.token[1] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != ',') {
                                Convertor.inpos = i;
                                break;
                            }
                            sUtil.skipSpace();
                            if (!sUtil.getToken("int")) {
                                Convertor.inpos = i;
                                break;
                            }
                            sUtil.skipSpace();
                            Convertor.token[2] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != ')') {
                                Convertor.inpos = i;
                                break;
                            }
                            sUtil.skipSpace();
                            if (Convertor.token[0].length() > 0 && Convertor.token[1].length() > 0 && Convertor.token[2].length() > 0) {
                                sUtil.putString((new StringBuilder()).append("public static WriteHandlerPtr ").append(Convertor.token[0]).append(" = new WriteHandlerPtr() { public void handler(int ").append(Convertor.token[1]).append(", int ").append(Convertor.token[2]).append(")").toString());
                                type = MEM_WRITE;
                                i3 = -1;
                                continue;
                            }

                            Convertor.inpos = i;
                            break;
                        }
                        Convertor.inpos = i;
                    } else {
                        sUtil.skipSpace();
                        if (sUtil.getToken("GameDriver")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("public static GameDriver " + Convertor.token[0] + " = new GameDriver");
                                type = GAMEDRIVER;
                                i3 = -1;
                                continue;
                            }
                        } else if (sUtil.getToken("MachineDriver")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static MachineDriver " + Convertor.token[0] + " = new MachineDriver");
                                type = MACHINEDRIVER;
                                i3 = -1;
                                continue;
                            }
                        } else if (sUtil.getToken("MemoryReadAddress")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '[') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ']') {
                                    Convertor.inpos = i;
                                } else {
                                    sUtil.skipSpace();
                                    if (sUtil.parseChar() != '=') {
                                        Convertor.inpos = i;
                                    } else {
                                        sUtil.skipSpace();
                                        sUtil.putString("static MemoryReadAddress " + Convertor.token[0] + "[] =");
                                        type = MEMORYREAD;
                                        i3 = -1;
                                        continue;
                                    }
                                }
                            }
                        } else if (sUtil.getToken("MemoryWriteAddress")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '[') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ']') {
                                    Convertor.inpos = i;
                                } else {
                                    sUtil.skipSpace();
                                    if (sUtil.parseChar() != '=') {
                                        Convertor.inpos = i;
                                    } else {
                                        sUtil.skipSpace();
                                        sUtil.putString("static MemoryWriteAddress " + Convertor.token[0] + "[] =");
                                        type = MEMORYWRITE;
                                        i3 = -1;
                                        continue;
                                    }
                                }
                            }
                        } else if (sUtil.getToken("IOReadPort")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '[') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ']') {
                                    Convertor.inpos = i;
                                } else {
                                    sUtil.skipSpace();
                                    if (sUtil.parseChar() != '=') {
                                        Convertor.inpos = i;
                                    } else {
                                        sUtil.skipSpace();
                                        sUtil.putString("static IOReadPort " + Convertor.token[0] + "[] =");
                                        type = IOREAD;
                                        i3 = -1;
                                        continue;
                                    }
                                }
                            }
                        } else if (sUtil.getToken("IOWritePort")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '[') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ']') {
                                    Convertor.inpos = i;
                                } else {
                                    sUtil.skipSpace();
                                    if (sUtil.parseChar() != '=') {
                                        Convertor.inpos = i;
                                    } else {
                                        sUtil.skipSpace();
                                        sUtil.putString("static IOWritePort " + Convertor.token[0] + "[] =");
                                        type = IOWRITE;
                                        i3 = -1;
                                        continue;
                                    }
                                }
                            }
                        } else if (sUtil.getToken("GfxLayout")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static GfxLayout " + Convertor.token[0] + " = new GfxLayout");
                                type = GFXLAYOUT;
                                i3 = -1;
                                continue;
                            }
                        } else if (sUtil.getToken("SN76496interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static SN76496interface " + Convertor.token[0] + " = new SN76496interface");
                                type = SN76496interface;
                                i3 = -1;
                                continue;
                            }
                        } else if (sUtil.getToken("DACinterface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static DACinterface " + Convertor.token[0] + " = new DACinterface");
                                type = DACinterface;
                                i3 = -1;
                                continue;
                            }
                        } else if (sUtil.getToken("GfxDecodeInfo")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '[') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ']') {
                                    Convertor.inpos = i;
                                } else {
                                    sUtil.skipSpace();
                                    if (sUtil.parseChar() != '=') {
                                        Convertor.inpos = i;
                                    } else {
                                        sUtil.skipSpace();
                                        sUtil.putString("static GfxDecodeInfo " + Convertor.token[0] + "[] =");
                                        type = GFXDECODE;
                                        i3 = -1;
                                        continue;
                                    }
                                }
                            }
                        } else if (sUtil.getToken("InputPort")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '[') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ']') {
                                    Convertor.inpos = i;
                                } else {
                                    sUtil.skipSpace();
                                    if (sUtil.parseChar() != '=') {
                                        Convertor.inpos = i;
                                    } else {
                                        sUtil.skipSpace();
                                        sUtil.putString("static InputPort " + Convertor.token[0] + "[] =");
                                        type = INPUTPORT;
                                        i3 = -1;
                                        continue;
                                    }
                                }
                            }
                        } else {
                            Convertor.inpos = i;
                        }
                    }
                    break;
                case '{':
                    if (type == GAMEDRIVER) {
                        i3++;
                        insideagk[i3] = 0;
                        Convertor.outbuf[(Convertor.outpos++)] = '(';
                        Convertor.inpos += 1;
                        continue;
                    }
                    if (type == MACHINEDRIVER) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 0) {
                            Convertor.outbuf[(Convertor.outpos++)] = 40;
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && (insideagk[0] == 0)) {
                            sUtil.putString("new MachineCPU[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && (insideagk[0] == 7)) {
                            sUtil.putString("new rectangle(");
                            Convertor.inpos += 1;
                            continue;
                        } else if ((i3 == 1) && (insideagk[0] == 8))//case of 1 CPU 
                        {
                            sUtil.putString("new rectangle(");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && (insideagk[0] == 21)) {
                            sUtil.putString("new MachineSound[] {");
                            Convertor.inpos += 1;
                            continue;
                        } else if ((i3 == 1) && (insideagk[0] == 22)) {
                            sUtil.putString("new MachineSound[] {");
                            Convertor.inpos += 1;
                            continue;
                        } else if ((i3 == 1) && (insideagk[0] == 23)) {
                            sUtil.putString("new MachineSound[] {");
                            Convertor.inpos += 1;
                            continue;
                        } else if ((i3 == 1) && (insideagk[0] == 24)) {
                            sUtil.putString("new MachineSound[] {");
                            Convertor.inpos += 1;
                            continue;
                        } else if ((i3 == 1) && (insideagk[0] == 25)) {
                            sUtil.putString("new MachineSound[] {");
                            Convertor.inpos += 1;
                            continue;
                        } else if ((i3 == 1) && (insideagk[0] == 26)) {
                            sUtil.putString("new MachineSound[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 2) && (insideagk[0] == 0)) {
                            sUtil.putString("new MachineCPU(");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 2) && (insideagk[0] == 21)) {
                            sUtil.putString("new MachineSound(");
                            Convertor.inpos += 1;
                            continue;
                        } else if ((i3 == 2) && (insideagk[0] == 22)) {
                            sUtil.putString("new MachineSound(");
                            Convertor.inpos += 1;
                            continue;
                        } else if ((i3 == 2) && (insideagk[0] == 23)) {
                            sUtil.putString("new MachineSound(");
                            Convertor.inpos += 1;
                            continue;
                        } else if ((i3 == 2) && (insideagk[0] == 24)) {
                            sUtil.putString("new MachineSound(");
                            Convertor.inpos += 1;
                            continue;
                        } else if ((i3 == 2) && (insideagk[0] == 25)) {
                            sUtil.putString("new MachineSound(");
                            Convertor.inpos += 1;
                            continue;
                        } else if ((i3 == 2) && (insideagk[0] == 26)) {
                            sUtil.putString("new MachineSound(");
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == MEMORYREAD) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 1) {
                            sUtil.putString("new MemoryReadAddress(");
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == MEMORYWRITE) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 1) {
                            sUtil.putString("new MemoryWriteAddress(");
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == IOREAD) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 1) {
                            sUtil.putString("new IOReadPort(");
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == IOWRITE) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 1) {
                            sUtil.putString("new IOWritePort(");
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == GFXDECODE) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 1) {
                            sUtil.putString("new GfxDecodeInfo(");
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == GFXLAYOUT) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 0) {
                            Convertor.outbuf[(Convertor.outpos++)] = '(';
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 4) || (insideagk[0] == 5) || (insideagk[0] == 6))) {
                            sUtil.putString("new int[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == SN76496interface) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 0) {
                            Convertor.outbuf[(Convertor.outpos++)] = '(';
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 2))) {
                            sUtil.putString("new int[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == DACinterface) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 0) {
                            Convertor.outbuf[(Convertor.outpos++)] = '(';
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1))) {
                            sUtil.putString("new int[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == INPUTPORT) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 1) {
                            sUtil.putString("new InputPort(");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 2) && (insideagk[1] == 1)) {
                            sUtil.putString("new int[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == DRIVER_INIT || type == MEM_READ || type == MEM_WRITE || type == MACHINE_INTERRUPT || type == MACHINE_INIT) {
                        i3++;
                    }
                    break;
                case '}':
                    if (type == GAMEDRIVER) {
                        i3--;
                        Convertor.outbuf[(Convertor.outpos++)] = ')';
                        Convertor.inpos += 1;
                        type = -1;
                        continue;
                    }
                    if (type == DRIVER_INIT || type == MEM_READ || type == MEM_WRITE || type == MACHINE_INTERRUPT || type == MACHINE_INIT) {
                        i3--;
                        if (i3 == -1) {
                            sUtil.putString("} };");
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    }
                    if (type == MACHINEDRIVER) {
                        i3--;
                        if (i3 == -1) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                        if ((i3 == 1) && (insideagk[0] == 0)) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && (insideagk[0] == 21)) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            continue;
                        } else if ((i3 == 1) && (insideagk[0] == 22)) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            continue;
                        } else if ((i3 == 1) && (insideagk[0] == 23)) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            continue;
                        } else if ((i3 == 1) && (insideagk[0] == 24)) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            continue;
                        } else if ((i3 == 1) && (insideagk[0] == 25)) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            continue;
                        } else if ((i3 == 1) && (insideagk[0] == 26)) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 0) && (insideagk[0] == 7)) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            continue;
                        } else if ((i3 == 0) && (insideagk[0] == 8))//for rectangle defination in single cpu only
                        {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if ((type == MEMORYREAD) || (type == MEMORYWRITE) || (type == IOREAD) || (type == IOWRITE) || (type == GFXDECODE)) {
                        i3--;
                        if (i3 == -1) {
                            type = -1;
                        } else if (i3 == 0) {
                            Convertor.outbuf[(Convertor.outpos++)] = ')';
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == GFXLAYOUT) {
                        i3--;
                        if (i3 == -1) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    } else if (type == SN76496interface) {
                        i3--;
                        if (i3 == -1) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    } else if (type == DACinterface) {
                        i3--;
                        if (i3 == -1) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    } else if (type == INPUTPORT) {
                        i3--;
                        if (i3 == -1) {
                            type = -1;
                        } else if (i3 == 0) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    break;
                case 'e':
                    if (sUtil.getToken("extern"))//if it starts with extern skip it
                    {
                        sUtil.skipLine();
                        continue;
                    }
                    i = Convertor.inpos;
                    if (sUtil.getToken("enum")) {
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != '{') {
                            Convertor.inpos = i;
                        } else {
                            sUtil.skipSpace();
                            int i5 = 0;
                            do {
                                Convertor.token[(i5++)] = sUtil.parseToken();
                                sUtil.skipSpace();
                                c = sUtil.parseChar();
                                if ((c != '}') && (c != ',')) {
                                    Convertor.inpos = i;
                                    break;
                                }
                                sUtil.skipSpace();
                            } while (c == ',');
                            if (sUtil.parseChar() != ';') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.putString("static final int ");
                                for (int i6 = 0; i6 < i5; i6++) {
                                    sUtil.putString(Convertor.token[i6] + " = " + i6);
                                    sUtil.putString(i6 == i5 - 1 ? ";" : ", ");
                                }
                                continue;
                            }
                        }
                    } else {
                        i = Convertor.inpos;
                    }

                    break;
                case '0':
                    i = Convertor.inpos;
                    if (sUtil.getToken("0")) {
                        Convertor.inpos = i;
                        if (type == GAMEDRIVER) {
                            sUtil.putString("null");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if (type == MACHINEDRIVER) {
                            if ((i3 == 0) && ((insideagk[i3] == 3) || (insideagk[i3] == 5) || (insideagk[i3] == 6) || (insideagk[i3] == 12) || (insideagk[i3] == 14) || (insideagk[i3] == 15) || (insideagk[i3] == 16))) {
                                sUtil.putString("null");
                                Convertor.inpos += 1;
                                continue;
                            } else if ((i3 == 0) /*&& (type3==1)*/ && ((insideagk[i3] == 4) || (insideagk[i3] == 8) || (insideagk[i3] == 11) || (insideagk[i3] == 13) || (insideagk[i3] == 14) || (insideagk[i3] == 15) || (insideagk[i3] == 16) || (insideagk[i3] == 17))) {
                                //case for single core cpus
                                sUtil.putString("null");
                                Convertor.inpos += 1;
                                continue;
                            }
                            if ((i3 == 2) && (insideagk[0] == 0) && ((insideagk[i3] == 4) || (insideagk[i3] == 6))) {
                                sUtil.putString("null");
                                Convertor.inpos += 1;
                            }
                        }

                    }
                    break;
                case '&':
                    if (type == GAMEDRIVER) {
                        Convertor.inpos += 1;
                        continue;
                    }
                    if (type == MEMORYREAD || type == MEMORYWRITE || type == IOREAD || type == IOWRITE) {
                        Convertor.inpos += 1;
                        continue;
                    }
                    if (type == GFXDECODE) {
                        Convertor.inpos += 1;
                        continue;
                    }
                    if (type == MACHINEDRIVER) {
                        Convertor.inpos += 1;
                        continue;
                    }
                    break;
                case ',':
                    if ((type != -1)) {
                        if (type == INPUTPORT) {
                            if ((i3 == 1) && (insideagk[i3] == 1)) {
                                sUtil.skipTo('}');

                                if (sUtil.getChar() != ',') {
                                    continue;
                                }
                                Convertor.inpos += 1;
                                continue;
                            }
                        }
                        if (i3 != -1) {
                            insideagk[i3] += 1;
                        }
                    }
                    break;
                case '-':
                    c = sUtil.getNextChar();
                    if (c == '>') {
                        Convertor.outbuf[(Convertor.outpos++)] = '.';
                        Convertor.inpos += 2;

                    }
                    break;
                case 'v':
                    i = Convertor.inpos;
                    if (sUtil.getToken("void")) {
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        if (sUtil.parseChar() != '(') {
                            Convertor.inpos = i;
                            break;
                        }
                        if (sUtil.getToken("void"))//an to soma tis function einai (void)
                        {
                            if (sUtil.parseChar() != ')') {
                                Convertor.inpos = i;
                                break;
                            }

                            if (Convertor.token[0].contains("hisave")) {
                                sUtil.putString((new StringBuilder()).append("static HiscoreSavePtr ").append(Convertor.token[0]).append(" = new HiscoreSavePtr() { public void handler() ").toString());
                                type = HISAVE;
                                i3 = -1;
                                continue;
                            }
                            if (Convertor.token[0].startsWith("init_")) {
                                sUtil.putString((new StringBuilder()).append("public static InitDriverPtr ").append(Convertor.token[0]).append(" = new InitDriverPtr() { public void handler() ").toString());
                                type = DRIVER_INIT;
                                i3 = -1;
                                continue;
                            }
                        }
                        if (!sUtil.getToken("int")) {
                            Convertor.inpos = i;
                            break;
                        }
                        sUtil.skipSpace();
                        Convertor.token[1] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ',') {
                            Convertor.inpos = i;
                            break;
                        }
                        sUtil.skipSpace();
                        if (!sUtil.getToken("int")) {
                            Convertor.inpos = i;
                            break;
                        }
                        sUtil.skipSpace();
                        Convertor.token[2] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ')') {
                            Convertor.inpos = i;
                            break;
                        }
                        sUtil.skipSpace();
                        if (Convertor.token[0].length() > 0 && Convertor.token[1].length() > 0 && Convertor.token[2].length() > 0) {
                            sUtil.putString((new StringBuilder()).append("public static WriteHandlerPtr ").append(Convertor.token[0]).append(" = new WriteHandlerPtr() { public void handler(int ").append(Convertor.token[1]).append(", int ").append(Convertor.token[2]).append(")").toString());
                            type = MEM_WRITE;
                            i3 = -1;
                            continue;
                        }

                        Convertor.inpos = i;
                        break;
                    }
                    break;
                case 'u':
                    
                    if (type == MEM_WRITE) {
                        if(i3==-1) break;//if is not inside a memwrite function break
                        i=Convertor.inpos;
                        if (sUtil.getToken("unsigned char")) {
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '*') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.putString((new StringBuilder()).append("UBytePtr ").toString());
                                continue;
                            }
                        }
                    }
                    if (type == HILOAD || type == HISAVE) {
                        i = Convertor.inpos;
                        if (sUtil.getToken("unsigned char")) {
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '*') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                Convertor.token[0] = sUtil.parseToken();
                                sUtil.skipSpace();
                                sUtil.putString("char[] " + Convertor.token[0]);
                            }
                        }
                    }
                    break;
                case 'o':
                    if (type == HILOAD || type == HISAVE) {
                        i = Convertor.inpos;
                        if (sUtil.getToken("osd_fread(f")) {
                            if (sUtil.parseChar() != ',') {
                                Convertor.inpos = i;
                            } else {
                                if (sUtil.parseChar() != '&') {
                                    Convertor.inpos = i;
                                } else {
                                    Convertor.token[0] = sUtil.parseToken();
                                    if (Convertor.token[0].contains("+"))//3stooges has RAM+offset ingnore conversion
                                    {
                                        Convertor.inpos = i;
                                        continue;
                                    }
                                    if (sUtil.parseChar() != '[') {
                                        Convertor.inpos = i;
                                    } else {
                                        //sUtil.skipSpace();
                                        Convertor.token[1] = sUtil.parseToken();
                                        //sUtil.skipSpace();
                                        if (sUtil.parseChar() != ']') {
                                            Convertor.inpos = i;
                                        } else {
                                            sUtil.skipSpace();
                                            sUtil.putString("osd_fread(f," + Convertor.token[0] + ", " + Convertor.token[1]);
                                            continue;
                                        }
                                    }
                                }
                            }
                        }
                        if (sUtil.getToken("osd_fwrite(f")) {
                            if (sUtil.parseChar() != ',') {
                                Convertor.inpos = i;
                            } else {
                                if (sUtil.parseChar() != '&') {
                                    Convertor.inpos = i;
                                } else {
                                    Convertor.token[0] = sUtil.parseToken();
                                    if (Convertor.token[0].contains("+"))//3stooges has RAM+offset ingnore conversion
                                    {
                                        Convertor.inpos = i;
                                        continue;
                                    }
                                    if (sUtil.parseChar() != '[') {
                                        Convertor.inpos = i;
                                    } else {
                                        //sUtil.skipSpace();
                                        Convertor.token[1] = sUtil.parseToken();
                                        //sUtil.skipSpace();
                                        if (sUtil.parseChar() != ']') {
                                            Convertor.inpos = i;
                                        } else {
                                            sUtil.skipSpace();
                                            sUtil.putString("osd_fwrite(f," + Convertor.token[0] + ", " + Convertor.token[1]);
                                            continue;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                case 'i':
                    i = Convertor.inpos;
                    if (sUtil.getToken("int")) {
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        if (sUtil.parseChar() != '(') {
                            Convertor.inpos = i;
                            break;
                        }
                        if (sUtil.getToken("void"))//an to soma tis function einai (void)
                        {
                            if (sUtil.parseChar() != ')') {
                                Convertor.inpos = i;
                                break;
                            }
                            if (Convertor.token[0].contains("_interrupt")) {
                                sUtil.putString((new StringBuilder()).append("public static InterruptPtr ").append(Convertor.token[0]).append(" = new InterruptPtr() { public int handler() ").toString());
                                type = MACHINE_INTERRUPT;
                                i3 = -1;
                                continue;
                            }

                        }
                        if (sUtil.getToken("int")) {
                            sUtil.skipSpace();
                            Convertor.token[1] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != ')') {
                                Convertor.inpos = i;
                                break;
                            }
                            sUtil.skipSpace();
                            if (Convertor.token[0].length() > 0 && Convertor.token[1].length() > 0) {
                                sUtil.putString((new StringBuilder()).append("public static ReadHandlerPtr ").append(Convertor.token[0]).append(" = new ReadHandlerPtr() { public int handler(int ").append(Convertor.token[1]).append(")").toString());
                                type = MEM_READ;
                                i3 = -1;
                                continue;
                            }

                        }
                        Convertor.inpos = i;
                        break;
                    }
                    break;
                case 'm':
                    if (type == HILOAD) {
                        i = Convertor.inpos;
                        if (sUtil.getToken("memcmp")) {
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '(') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != '&') {
                                    Convertor.inpos = i;
                                } else {
                                    sUtil.skipSpace();
                                    Convertor.token[0] = sUtil.parseToken();
                                    //sUtil.skipSpace();
                                    if (sUtil.parseChar() != '[') {
                                        Convertor.inpos = i;
                                    } else {
                                        //sUtil.skipSpace();
                                        Convertor.token[1] = sUtil.parseToken();
                                        //sUtil.skipSpace();
                                        if (sUtil.parseChar() != ']') {
                                            Convertor.inpos = i;
                                        } else {
                                            //sUtil.skipSpace();
                                            if (sUtil.parseChar() != ',') {
                                                Convertor.inpos = i;
                                            } else {
                                                //sUtil.skipSpace();
                                                if (sUtil.parseChar() != '"') {
                                                    Convertor.inpos = i;
                                                } else {
                                                    Convertor.token[2] = sUtil.parseToken();

                                                    if (sUtil.parseChar() != '"') {
                                                        Convertor.inpos = i;
                                                    } else {
                                                        //above code has uses so just leave it as it is...
                                                        Convertor.token[3] = Convertor.token[2];//remove it if above works
                                          /*Convertor.token[3] = null;
                                                         int i5 = 0;
                                         
                                                         while (i5 < Convertor.token[2].length())
                                                         {
                                                         c = Convertor.token[2].charAt(i5);
                                                         if (c != '\\')
                                                         {
                                              
                                                         Convertor.token[3] = null;
                                              
                                                         }
                                                         else {
                                                         i5++;
                                                         c = Convertor.token[2].charAt(i5);
                                                         System.out.println(c);
                                                         if (c == 'x')
                                                         {
                                                         Convertor.token[3] = (Convertor.token[3] + ", ");
                                                         //System.out.println(Convertor.token[3]);
                                                         i5++;
                                                         c = Convertor.token[2].charAt(i5);
                                                         int tmp7254_7253 = 3;
                                                         String[] tmp7254_7250 = Convertor.token; 
                                                         tmp7254_7250[tmp7254_7253] = (tmp7254_7250[tmp7254_7253] + "0x" + c);
                                                         i5++;
                                                         c = Convertor.token[2].charAt(i5);
                                                         if (((c < '0') || (c > '9')) && ((c < 'a') || (c > 'f')))
                                                         continue;
                                                         int tmp7327_7326 = 3;
                                                         String[] tmp7327_7323 = Convertor.token; 
                                                         tmp7327_7323[tmp7327_7326] = (tmp7327_7323[tmp7327_7326] + "" + c);
                                                         i5++;
                                                         continue;
                                                         }
                                                         if (c == '0')
                                                         {
                                               
                                                         Convertor.token[3] = (Convertor.token[3] + ", ");
                                                         int tmp7411_7410 = 3;
                                                         String[] tmp7411_7407 = Convertor.token; 
                                                         tmp7411_7407[tmp7411_7410] = (tmp7411_7407[tmp7411_7410] + "0x0");
                                                         i5++;
                                                         continue;
                                                         }

                                                         Convertor.token[3] = null;
                                                         }

                                                         }*/

                                                        //sUtil.skipSpace();
                                                        if (sUtil.parseChar() != ',') {
                                                            Convertor.inpos = i;
                                                        } else {
                                                            // sUtil.skipSpace();
                                                            Convertor.token[4] = sUtil.parseToken();
                                                            //sUtil.skipSpace();
                                                            if (sUtil.parseChar() != ')') {
                                                                Convertor.inpos = i;
                                                            } else {
                                                                if (Convertor.token[3] != null) {
                                                                    sUtil.putString("memcmp(" + Convertor.token[0] + ", " + Convertor.token[1] + ", new char[] { " + Convertor.token[3] + " }, " + Convertor.token[4] + ")");
                                                                    continue;
                                                                }
                                                                sUtil.putString("memcmp(" + Convertor.token[0] + ", " + Convertor.token[1] + ", \"" + Convertor.token[2] + "\", " + Convertor.token[4] + ")");
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;

            }

            Convertor.outbuf[Convertor.outpos++] = Convertor.inbuf[Convertor.inpos++];//grapse to inputbuffer sto output
        } while (true);
        if (only_once_flag) {
            sUtil.putString("}\r\n");
        }
    }
}
