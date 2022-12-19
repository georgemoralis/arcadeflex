/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arcadeflex.v037b7.convertor;

/**
 * @author shadow
 */
public class convertMame {

    public static void ConvertMame() {
        Analyse();
        Convert();
    }

    public static void Analyse() {

    }

    static final int GAMEDRIVER = 0;
    static final int Samplesinterface = 1;
    static final int READHANDLER = 2;
    static final int WRITEHANDLER = 3;
    static final int MACHINE_INTERRUPT = 4;
    static final int DRIVER_INIT = 5;
    static final int MACHINE_INIT = 6;
    static final int VH_STOP = 7;
    static final int VH_START = 8;
    static final int VH_SCREENREFRESH = 9;
    static final int GFXLAYOUT = 10;
    static final int GFXDECODE = 11;
    static final int VLM5030interface = 12;
    static final int VH_CONVERT = 13;
    static final int SH_UPDATE = 14;
    static final int SH_START = 15;
    static final int SH_STOP = 16;
    static final int NESinterface = 17;
    static final int SN76496interface = 18;
    static final int DACinterface = 19;
    static final int PLOT_BOX = 20;
    static final int MARK_DIRTY = 21;
    static final int PLOT_PIXEL = 22;
    static final int VH_EOF = 23;
    static final int NVRAM_H = 24;
    static final int MACHINEDRIVER = 25;
    static final int AY8910interface = 26;
    static final int YM3812interface = 27;
    static final int YM3526interface = 28;
    static final int MSM5205interface = 29;
    static final int YM2203interface = 30;
    static final int K054539interface = 31;
    static final int EEPROM_interface = 32;
    static final int MEMORYREAD = 33;
    static final int MEMORYWRITE =34;
    static final int IOREAD = 35;
    static final int IOWRITE = 36;
    static final int OKIM6295interface = 37;
    static final int YM2413interface=38;
    static final int POKEYinterface=39;
    static final int YM2151interface=40;
    static final int UPD7759_interface=41;
    static final int CustomSound_interface=42;
    static final int YM2610interface=43;
    static final int TIMERCALLBACK=44;
    static final int C140interface=45;
    static final int astrocade_interface=46;
    static final int RF5C68interface=47;
    static final int k051649_interface=48;
    static final int hc55516_interface=49;
    static final int vclk_interruptPtr=50;
    static final int konami_cpu_setlines_callbackPtr=51;
    static final int namco_interface=52;
    static final int SN76477interface=53;
    static final int K052109=54;
    static final int K051960=55;
    static final int TILEINFO=56;

    //type2 fields
    static final int NEWINPUT = 130;
    static final int ROMDEF = 131;

    public static void Convert() {
        Convertor.inpos = 0;//position of pointer inside the buffers
        Convertor.outpos = 0;
        boolean only_once_flag = false;//gia na baleis to header mono mia fora
        boolean line_change_flag = false;

        int kapa = 0;
        int i = 0;
        int type = 0;
        int i3 = -1;
        int i8 = -1;
        int type2 = 0;
        int[] insideagk = new int[10];//get the { that are inside functions

        do {
            if (Convertor.inpos >= Convertor.inbuf.length)//an to megethos einai megalitero spase to loop
            {
                break;
            }
            char c = sUtil.getChar(); //pare ton character
            if (line_change_flag) {
                for (int i1 = 0; i1 < kapa; i1++) {
                    sUtil.putString("\t");
                }

                line_change_flag = false;
            }
            switch (c) {
                case 35: // '#'
                {
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
                        sUtil.putString("package " + Convertor.packageName + ";\r\n");
                        sUtil.putString("\r\n");
                        sUtil.putString((new StringBuilder()).append("public class ").append(Convertor.className).append("\r\n").toString());
                        sUtil.putString("{\r\n");
                        kapa = 1;
                        line_change_flag = true;
                    }
                    continue;
                }
                case 10: // '\n'
                {
                    Convertor.outbuf[Convertor.outpos++] = Convertor.inbuf[Convertor.inpos++];
                    line_change_flag = true;
                    continue;
                }
                case 'b':
                {
                    i = Convertor.inpos;
                    if(sUtil.getToken("buffered_spriteram"))
                    {
                        if (sUtil.parseChar() != '[') {
                            Convertor.inpos = i;
                            break;
                        }
                        Convertor.token[0] = sUtil.parseToken(']');
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ']') {
                            Convertor.inpos = i;
                            break;
                        }
                        else {
                            sUtil.skipSpace();
                            if (sUtil.parseChar() == '=') {
                                int g=Convertor.inpos;
                                if (sUtil.parseChar() == '=') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                Convertor.inpos=g;
                                sUtil.skipSpace();
                                Convertor.token[1] = sUtil.parseToken(';');
                                sUtil.putString((new StringBuilder()).append("buffered_spriteram.write(").append(Convertor.token[0]).append(",").append(Convertor.token[1]).append(");").toString());
                                Convertor.inpos +=1;
                                break;
                            }
                            sUtil.putString((new StringBuilder()).append("buffered_spriteram.read(").append(Convertor.token[0]).append(")").toString());
                            Convertor.inpos -= 1;
                            continue;
                        }
                    }
                    if(sUtil.getToken("buffered_spriteram_2"))
                    {
                        if (sUtil.parseChar() != '[') {
                            Convertor.inpos = i;
                            break;
                        }
                        Convertor.token[0] = sUtil.parseToken(']');
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ']') {
                            Convertor.inpos = i;
                            break;
                        }
                        else {
                            sUtil.skipSpace();
                            if (sUtil.parseChar() == '=') {
                                int g=Convertor.inpos;
                                if (sUtil.parseChar() == '=') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                Convertor.inpos=g;
                                sUtil.skipSpace();
                                Convertor.token[1] = sUtil.parseToken(';');
                                sUtil.putString((new StringBuilder()).append("buffered_spriteram_2.write(").append(Convertor.token[0]).append(",").append(Convertor.token[1]).append(");").toString());
                                Convertor.inpos +=1;
                                break;
                            }
                            sUtil.putString((new StringBuilder()).append("buffered_spriteram_2.read(").append(Convertor.token[0]).append(")").toString());
                            Convertor.inpos -= 1;
                            continue;
                        }
                    }
                    break;
                }
                case 's': {
                    if (type == WRITEHANDLER || type == VH_SCREENREFRESH || type == VH_START || type == READHANDLER) {
                        if(i3==-1) break;//if is not inside a memwrite function break
                        i=Convertor.inpos;
                        if (sUtil.getToken("spriteram_size")) {
                            sUtil.putString((new StringBuilder()).append("spriteram_size[0]").toString());
                            continue;
                        }
                    }
                    i = Convertor.inpos;
                    if(sUtil.getToken("spriteram"))
                    {
                        if (sUtil.parseChar() != '[') {
                            Convertor.inpos = i;
                            break;
                        }
                        Convertor.token[0] = sUtil.parseToken(']');
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ']') {
                            Convertor.inpos = i;
                            break;
                        }
                        else {
                            sUtil.skipSpace();
                            if (sUtil.parseChar() == '=') {
                                int g=Convertor.inpos;
                                if (sUtil.parseChar() == '=') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                Convertor.inpos=g;
                                sUtil.skipSpace();
                                Convertor.token[1] = sUtil.parseToken(';');
                                sUtil.putString((new StringBuilder()).append("spriteram.write(").append(Convertor.token[0]).append(",").append(Convertor.token[1]).append(");").toString());
                                Convertor.inpos +=1;
                                break;
                            }
                            sUtil.putString((new StringBuilder()).append("spriteram.read(").append(Convertor.token[0]).append(")").toString());
                            Convertor.inpos -= 1;
                            continue;
                        }
                    }
                    if(sUtil.getToken("spriteram_2"))
                    {
                        if (sUtil.parseChar() != '[') {
                            Convertor.inpos = i;
                            break;
                        }
                        Convertor.token[0] = sUtil.parseToken(']');
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ']') {
                            Convertor.inpos = i;
                            break;
                        }
                        else {
                            sUtil.skipSpace();
                            if (sUtil.parseChar() == '=') {
                                int g=Convertor.inpos;
                                if (sUtil.parseChar() == '=') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                Convertor.inpos=g;
                                sUtil.skipSpace();
                                Convertor.token[1] = sUtil.parseToken(';');
                                sUtil.putString((new StringBuilder()).append("spriteram_2.write(").append(Convertor.token[0]).append(",").append(Convertor.token[1]).append(");").toString());
                                Convertor.inpos +=1;
                                break;
                            }
                            sUtil.putString((new StringBuilder()).append("spriteram_2.read(").append(Convertor.token[0]).append(")").toString());
                            Convertor.inpos -= 1;
                            continue;
                        }
                    }
                    if(sUtil.getToken("spriteram_3"))
                    {
                        if (sUtil.parseChar() != '[') {
                            Convertor.inpos = i;
                            break;
                        }
                        Convertor.token[0] = sUtil.parseToken(']');
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ']') {
                            Convertor.inpos = i;
                            break;
                        }
                        else {
                            sUtil.skipSpace();
                            if (sUtil.parseChar() == '=') {
                                int g=Convertor.inpos;
                                if (sUtil.parseChar() == '=') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                Convertor.inpos=g;
                                sUtil.skipSpace();
                                Convertor.token[1] = sUtil.parseToken(';');
                                sUtil.putString((new StringBuilder()).append("spriteram_3.write(").append(Convertor.token[0]).append(",").append(Convertor.token[1]).append(");").toString());
                                Convertor.inpos +=1;
                                break;
                            }
                            sUtil.putString((new StringBuilder()).append("spriteram_3.read(").append(Convertor.token[0]).append(")").toString());
                            Convertor.inpos -= 1;
                            continue;
                        }
                    }
                    if (type == WRITEHANDLER) {
                        if (sUtil.getToken("soundlatch_w")) {
                            sUtil.putString((new StringBuilder()).append("soundlatch_w.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("soundlatch2_w")) {
                            sUtil.putString((new StringBuilder()).append("soundlatch2_w.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("soundlatch3_w")) {
                            sUtil.putString((new StringBuilder()).append("soundlatch3_w.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("soundlatch4_w")) {
                            sUtil.putString((new StringBuilder()).append("soundlatch4_w.handler").toString());
                            continue;
                        }
                    }
                    if (sUtil.getToken("static")) {
                        sUtil.skipSpace();
                    }
                    if (!sUtil.getToken("struct")) //static but not static struct
                    {
                        if (sUtil.getToken("int")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
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
                                if (Convertor.token[0].contains("_irq")) {
                                    sUtil.putString((new StringBuilder()).append("public static InterruptPtr ").append(Convertor.token[0]).append(" = new InterruptPtr() { public int handler() ").toString());
                                    type = MACHINE_INTERRUPT;
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
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '(') {
                                Convertor.inpos = i;
                                break;
                            }
                            sUtil.skipSpace();
                            if(sUtil.getToken("int param"))
                            {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ')') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                if (sUtil.getChar() == ';') {
                                    sUtil.skipLine();
                                    continue;
                                }
                                if (Convertor.token[0].contains("_callback")) {
                                    sUtil.putString((new StringBuilder()).append("public static timer_callback ").append(Convertor.token[0]).append(" = new timer_callback() { public void handler(int param) ").toString());
                                    type = TIMERCALLBACK;
                                    i3 = -1;
                                    continue;
                                }
                            }
                            if(sUtil.getToken("int tile_index"))
                            {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ')') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                if (sUtil.getChar() == ';') {
                                    sUtil.skipLine();
                                    continue;
                                }
                                if (Convertor.token[0].contains("tile")) {
                                    sUtil.putString((new StringBuilder()).append("public static GetTileInfoPtr ").append(Convertor.token[0]).append(" = new GetTileInfoPtr() { public void handler(int tile_index) ").toString());
                                    type = TILEINFO;
                                    i3 = -1;
                                    continue;
                                }
                            }
                            if(sUtil.getToken("int layer,int bank,int *code,int *color"))
                            {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ')') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                if (sUtil.getChar() == ';') {
                                    sUtil.skipLine();
                                    continue;
                                }
                                if (Convertor.token[0].contains("_callback")) {
                                    sUtil.putString((new StringBuilder()).append("public static K052109_callbackProcPtr ").append(Convertor.token[0]).append(" = new K052109_callbackProcPtr() { public void handler(int layer,int bank,int[] code,int[] color) ").toString());
                                    type = K052109;
                                    i3 = -1;
                                    continue;
                                }
                            }
                            if(sUtil.getToken("int *code,int *color,int *priority_mask,int *shadow"))
                            {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ')') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                if (sUtil.getChar() == ';') {
                                    sUtil.skipLine();
                                    continue;
                                }
                                if (Convertor.token[0].contains("_callback")) {
                                    sUtil.putString((new StringBuilder()).append("public static K051960_callbackProcPtr ").append(Convertor.token[0]).append(" = new K051960_callbackProcPtr() { public void handler(int[] code,int[] color,int[] priority) ").toString());
                                    type = K051960;
                                    i3 = -1;
                                    continue;
                                }
                            }
                            if(sUtil.getToken("int"))
                            {
                                sUtil.skipSpace();
                                int pos = Convertor.inpos;
                                Convertor.token[1]=sUtil.parseToken();
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ')') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                if (sUtil.getChar() == ';') {
                                    sUtil.skipLine();
                                    continue;
                                }
                                if (Convertor.token[0].contains("_adpcm_int")) {
                                    sUtil.putString((new StringBuilder()).append("public static vclk_interruptPtr ").append(Convertor.token[0]).append(" = new vclk_interruptPtr() { public void handler(int ").append(Convertor.token[1]).append(") ").toString());
                                    type = vclk_interruptPtr;
                                    i3 = -1;
                                    continue;
                                }
                                else if (Convertor.token[0].contains("_banking") && Convertor.token[1].contains("lines")) {
                                    sUtil.putString((new StringBuilder()).append("public static konami_cpu_setlines_callbackPtr ").append(Convertor.token[0]).append(" = new konami_cpu_setlines_callbackPtr() { public void handler(int lines) ").toString());
                                    type = konami_cpu_setlines_callbackPtr;
                                    i3 = -1;
                                    continue;
                                }
                                else
                                {
                                    Convertor.inpos=pos;
                                }
                            }
                            if (sUtil.getToken("struct osd_bitmap *b,int x,int y,int w,int h,int p")) {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ')') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                if (sUtil.getChar() == ';') {
                                    sUtil.skipLine();
                                    continue;
                                }
                                if (Convertor.token[0].contains("pb_")) {
                                    sUtil.putString((new StringBuilder()).append("public static plot_box_procPtr ").append(Convertor.token[0]).append("  = new plot_box_procPtr() { public void handler(osd_bitmap b, int x, int y, int w, int h, int p) ").toString());
                                    type = PLOT_BOX;
                                    i3 = -1;
                                    continue;
                                }

                            }
                            if (sUtil.getToken("struct osd_bitmap *b,int x,int y,int p")) {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ')') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                if (sUtil.getChar() == ';') {
                                    sUtil.skipLine();
                                    continue;
                                }
                                if (Convertor.token[0].contains("pp_")) {
                                    sUtil.putString((new StringBuilder()).append("public static plot_pixel_procPtr ").append(Convertor.token[0]).append("  = new plot_pixel_procPtr() { public void handler(osd_bitmap b,int x,int y,int p) ").toString());
                                    type = PLOT_PIXEL;
                                    i3 = -1;
                                    continue;
                                }

                            }
                            if (sUtil.getToken("void *file,int read_or_write")) {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ')') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                if (sUtil.getChar() == ';') {
                                    sUtil.skipLine();
                                    continue;
                                }
                                //if (Convertor.token[0].contains("nvram")) {
                                sUtil.putString((new StringBuilder()).append("public static nvramPtr ").append(Convertor.token[0]).append("  = new nvramPtr() { public void handler(Object file, int read_or_write) ").toString());
                                type = NVRAM_H;
                                i3 = -1;
                                continue;
                                //}

                            }
                            if (sUtil.getToken("int sx,int sy,int ex,int ey")) {
                                sUtil.skipSpace();
                                if (sUtil.parseChar() != ')') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                if (sUtil.getChar() == ';') {
                                    sUtil.skipLine();
                                    continue;
                                }
                                if (Convertor.token[0].contains("md")) {
                                    sUtil.putString((new StringBuilder()).append("public static mark_dirty_procPtr ").append(Convertor.token[0]).append("  = new mark_dirty_procPtr() { public void handler(int sx,int sy,int ex,int ey) ").toString());
                                    type = MARK_DIRTY;
                                    i3 = -1;
                                    continue;
                                }

                            }

                        } else if (sUtil.getToken("READ_HANDLER(")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.getToken(");"))//if it is front function skip it
                            {
                                sUtil.skipLine();
                                continue;
                            } else {
                                sUtil.putString("public static ReadHandlerPtr " + Convertor.token[0] + "  = new ReadHandlerPtr() { public int handler(int offset)");
                                type = READHANDLER;
                                i3 = -1;
                                Convertor.inpos += 1;
                                continue;
                            }
                        } else if (sUtil.getToken("WRITE_HANDLER(")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.getToken(");"))//if it is a front function skip it
                            {
                                sUtil.skipLine();
                                continue;
                            } else {
                                sUtil.putString("public static WriteHandlerPtr " + Convertor.token[0] + " = new WriteHandlerPtr() {public void handler(int offset, int data)");
                                type = WRITEHANDLER;
                                i3 = -1;
                                Convertor.inpos += 1;
                                continue;
                            }
                        } else if (sUtil.getToken("const")) {
                            sUtil.skipSpace();
                            if (sUtil.getToken("struct")) {
                                sUtil.skipSpace();
                                if (sUtil.getToken("MachineDriver")) {
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
                                }
                            }
                        }
                        Convertor.inpos = i;
                    } else {
                        sUtil.skipSpace();
                        if (sUtil.getToken("GfxLayout")) {
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
                        }
                        else if (sUtil.getToken("MemoryReadAddress")) {
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
                        }
                        else if (sUtil.getToken("MemoryWriteAddress")) {
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
                        }
                        else if (sUtil.getToken("MachineDriver")) {
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
                        } else if (sUtil.getToken("VLM5030interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static VLM5030interface " + Convertor.token[0] + " = new VLM5030interface");
                                type = VLM5030interface;
                                i3 = -1;
                                continue;
                            }
                        }
                        else if (sUtil.getToken("hc55516_interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static hc55516_interface " + Convertor.token[0] + " = new hc55516_interface");
                                type = hc55516_interface;
                                i3 = -1;
                                continue;
                            }
                        }
                        else if (sUtil.getToken("SN76477interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static SN76477interface " + Convertor.token[0] + " = new SN76477interface");
                                type = SN76477interface;
                                i3 = -1;
                                continue;
                            }
                        }
                        else if (sUtil.getToken("namco_interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static namco_interface " + Convertor.token[0] + " = new namco_interface");
                                type = namco_interface;
                                i3 = -1;
                                continue;
                            }
                        }
                        else if (sUtil.getToken("k051649_interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static k051649_interface " + Convertor.token[0] + " = new k051649_interface");
                                type = k051649_interface;
                                i3 = -1;
                                continue;
                            }
                        }
                        else if (sUtil.getToken("RF5C68interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static RF5C68interface " + Convertor.token[0] + " = new RF5C68interface");
                                type = RF5C68interface;
                                i3 = -1;
                                continue;
                            }
                        }
                        else if (sUtil.getToken("astrocade_interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static astrocade_interface " + Convertor.token[0] + " = new astrocade_interface");
                                type = astrocade_interface;
                                i3 = -1;
                                continue;
                            }
                        }
                        else if (sUtil.getToken("C140interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static C140interface " + Convertor.token[0] + " = new C140interface");
                                type = C140interface;
                                i3 = -1;
                                continue;
                            }
                        }else if (sUtil.getToken("NESinterface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static NESinterface " + Convertor.token[0] + " = new NESinterface");
                                type = NESinterface;
                                i3 = -1;
                                continue;
                            }
                        } else if (sUtil.getToken("Samplesinterface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static Samplesinterface " + Convertor.token[0] + " = new Samplesinterface");
                                type = Samplesinterface;
                                i3 = -1;
                                continue;
                            }
                        } else if (sUtil.getToken("AY8910interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static AY8910interface " + Convertor.token[0] + " = new AY8910interface");
                                type = AY8910interface;
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
                        } else if (sUtil.getToken("K054539interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static K054539interface " + Convertor.token[0] + " = new K054539interface");
                                type = K054539interface;
                                i3 = -1;
                                continue;
                            }
                        } else if (sUtil.getToken("EEPROM_interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static EEPROM_interface " + Convertor.token[0] + " = new EEPROM_interface");
                                type = EEPROM_interface;
                                i3 = -1;
                                continue;
                            }
                        } else if (sUtil.getToken("YM3812interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static YM3812interface " + Convertor.token[0] + " = new YM3812interface");
                                type = YM3812interface;
                                i3 = -1;
                                continue;
                            }
                        }
                        else if (sUtil.getToken("OKIM6295interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static OKIM6295interface " + Convertor.token[0] + " = new OKIM6295interface");
                                type = OKIM6295interface;
                                i3 = -1;
                                continue;
                            }
                        }
                        else if (sUtil.getToken("POKEYinterface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static POKEYinterface " + Convertor.token[0] + " = new POKEYinterface");
                                type = POKEYinterface;
                                i3 = -1;
                                continue;
                            }
                        }
                        else if (sUtil.getToken("YM2413interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static YM2413interface " + Convertor.token[0] + " = new YM2413interface");
                                type = YM2413interface;
                                i3 = -1;
                                continue;
                            }
                        }
                        else if (sUtil.getToken("YM2203interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static YM2203interface " + Convertor.token[0] + " = new YM2203interface");
                                type = YM2203interface;
                                i3 = -1;
                                continue;
                            }
                        }
                        else if (sUtil.getToken("YM2151interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static YM2151interface " + Convertor.token[0] + " = new YM2151interface");
                                type = YM2151interface;
                                i3 = -1;
                                continue;
                            }
                        }
                        else if (sUtil.getToken("YM2610interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static YM2610interface " + Convertor.token[0] + " = new YM2610interface");
                                type = YM2610interface;
                                i3 = -1;
                                continue;
                            }
                        }
                        else if (sUtil.getToken("UPD7759_interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static UPD7759_interface " + Convertor.token[0] + " = new UPD7759_interface");
                                type = UPD7759_interface;
                                i3 = -1;
                                continue;
                            }
                        }
                        else if (sUtil.getToken("CustomSound_interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static CustomSound_interface " + Convertor.token[0] + " = new CustomSound_interface");
                                type = CustomSound_interface;
                                i3 = -1;
                                continue;
                            }
                        }
                        else if (sUtil.getToken("YM3526interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static YM3526interface " + Convertor.token[0] + " = new YM3526interface");
                                type = YM3526interface;
                                i3 = -1;
                                continue;
                            }
                        } else if (sUtil.getToken("MSM5205interface")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != '=') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.skipSpace();
                                sUtil.putString("static MSM5205interface " + Convertor.token[0] + " = new MSM5205interface");
                                type = MSM5205interface;
                                i3 = -1;
                                continue;
                            }
                        } else {
                            Convertor.inpos = i;
                        }
                    }
                }
                break;
                case 'c':
                    int sd=Convertor.inpos;
                    if(sUtil.getToken("color_prom"))
                    {
                        if (sUtil.parseChar() != '[') {
                            Convertor.inpos = sd;
                            break;
                        }
                        Convertor.token[0] = sUtil.parseToken(']');
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ']') {
                            Convertor.inpos = sd;
                            break;
                        }
                        else {
                            sUtil.skipSpace();
                            if (sUtil.parseChar() == '=') {
                                int g=Convertor.inpos;
                                if (sUtil.parseChar() == '=') {
                                    Convertor.inpos = sd;
                                    break;
                                }
                                Convertor.inpos=g;
                                sUtil.skipSpace();
                                Convertor.token[1] = sUtil.parseToken(';');
                                sUtil.putString((new StringBuilder()).append("color_prom.write(").append(Convertor.token[0]).append(",").append(Convertor.token[1]).append(");").toString());
                                Convertor.inpos +=1;
                                break;
                            }
                            sUtil.putString((new StringBuilder()).append("color_prom.read(").append(Convertor.token[0]).append(")").toString());
                            Convertor.inpos -= 1;
                            continue;
                        }
                    }
                    if(sUtil.getToken("colorram"))
                    {
                        if (sUtil.parseChar() != '[') {
                            Convertor.inpos = sd;
                            break;
                        }
                        Convertor.token[0] = sUtil.parseToken(']');
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ']') {
                            Convertor.inpos = sd;
                            break;
                        }
                        else {
                            sUtil.skipSpace();

                            if (sUtil.parseChar() == '=') {
                                int g=Convertor.inpos;
                                if (sUtil.parseChar() == '=') {
                                    Convertor.inpos = sd;
                                    break;
                                }
                                Convertor.inpos=g;
                                sUtil.skipSpace();
                                Convertor.token[1] = sUtil.parseToken(';');
                                sUtil.putString((new StringBuilder()).append("colorram.write(").append(Convertor.token[0]).append(",").append(Convertor.token[1]).append(");").toString());
                                Convertor.inpos +=1;
                                break;
                            }
                            sUtil.putString((new StringBuilder()).append("colorram.read(").append(Convertor.token[0]).append(")").toString());
                            Convertor.inpos -= 1;
                            continue;
                        }
                    }
                    if (type == WRITEHANDLER) {
                        if (sUtil.getToken("coin_counter_w")) {
                            sUtil.putString((new StringBuilder()).append("coin_counter_w.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("coin_lockout_w")) {
                            sUtil.putString((new StringBuilder()).append("coin_lockout_w.handler").toString());
                            continue;
                        }
                    }
                    break;
                case 'i':
                    i = Convertor.inpos;
                    if (type == READHANDLER) {
                        if (sUtil.getToken("input_port_0_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_0_r.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("input_port_1_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_1_r.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("input_port_2_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_2_r.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("input_port_3_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_3_r.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("input_port_4_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_4_r.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("input_port_5_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_5_r.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("input_port_6_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_6_r.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("input_port_7_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_7_r.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("input_port_8_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_8_r.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("input_port_9_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_9_r.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("input_port_10_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_10_r.handler").toString());
                            continue;
                        }
                    }
                    if (sUtil.getToken("if")) {
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != '(') {
                            Convertor.inpos = i;
                            break;
                        }
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.getChar() == '&') {
                            Convertor.inpos++;
                            sUtil.skipSpace();
                            Convertor.token[1] = sUtil.parseToken();
                            sUtil.skipSpace();
                            Convertor.token[0] = (new StringBuilder()).append("(").append(Convertor.token[0]).append(" & ").append(Convertor.token[1]).append(")").toString();
                        }
                        if (sUtil.parseChar() != ')') {
                            Convertor.inpos = i;
                            break;
                        }
                        sUtil.putString((new StringBuilder()).append("if (").append(Convertor.token[0]).append(" != 0)").toString());
                        continue;
                    }
                    if (sUtil.getToken("int")) {
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != '(') {
                            Convertor.inpos = i;
                            break;
                        }

                        if (sUtil.getToken("const struct MachineSound *msound")) {
                            sUtil.skipSpace();
                            if (sUtil.parseChar() != ')') {
                                Convertor.inpos = i;
                                break;
                            }
                            if (sUtil.getChar() == ';') {
                                sUtil.skipLine();
                                continue;
                            }
                            if (Convertor.token[0].contains("sh_start")) {
                                sUtil.putString((new StringBuilder()).append("public static ShStartPtr ").append(Convertor.token[0]).append(" = new ShStartPtr() { public int handler(MachineSound msound) ").toString());
                                type = SH_START;
                                i3 = -1;
                                continue;
                            }
                        }
                        if (sUtil.getToken("void);")) {
                            sUtil.skipLine();
                            continue;
                        }
                        if (sUtil.getToken(" void );")) {
                            sUtil.skipLine();
                            continue;
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
                            } else if (Convertor.token[0].contains("vh_start")) {
                                sUtil.putString((new StringBuilder()).append("public static VhStartPtr ").append(Convertor.token[0]).append(" = new VhStartPtr() { public int handler() ").toString());
                                type = VH_START;
                                i3 = -1;
                                continue;
                            }
                            else if (Convertor.token[0].contains("_irq")) {
                                sUtil.putString((new StringBuilder()).append("public static InterruptPtr ").append(Convertor.token[0]).append(" = new InterruptPtr() { public int handler() ").toString());
                                type = MACHINE_INTERRUPT;
                                i3 = -1;
                                continue;
                            }


                        }
                        Convertor.inpos = i;
                        break;
                    }
                    break;
                case 'v': {
                    if (type == WRITEHANDLER || type == VH_SCREENREFRESH || type == VH_START || type == READHANDLER) {
                        if(i3==-1) break;//if is not inside a memwrite function break
                        i=Convertor.inpos;
                        if (sUtil.getToken("videoram_size")) {
                                sUtil.putString((new StringBuilder()).append("videoram_size[0]").toString());
                                continue;
                        }
                    }
                    int j = Convertor.inpos;
                    if(sUtil.getToken("videoram"))
                    {
                        if (sUtil.parseChar() != '[') {
                            Convertor.inpos = j;
                            break;
                        }
                        Convertor.token[0] = sUtil.parseToken(']');
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ']') {
                            Convertor.inpos = j;
                            break;
                        }
                        else {
                            sUtil.skipSpace();
                            if (sUtil.parseChar() == '=') {
                                int g=Convertor.inpos;
                                if (sUtil.parseChar() == '=') {
                                    Convertor.inpos = j;
                                    break;
                                }
                                Convertor.inpos=g;
                                sUtil.skipSpace();
                                Convertor.token[1] = sUtil.parseToken(';');
                                sUtil.putString((new StringBuilder()).append("videoram.write(").append(Convertor.token[0]).append(",").append(Convertor.token[1]).append(");").toString());
                                Convertor.inpos +=1;
                                break;
                            }
                            sUtil.putString((new StringBuilder()).append("videoram.read(").append(Convertor.token[0]).append(")").toString());
                            Convertor.inpos -= 1;
                            continue;
                        }
                    }
                    Convertor.inpos=j;
                    if (!sUtil.getToken("void")) {
                        break;
                    }
                    sUtil.skipSpace();
                    Convertor.token[0] = sUtil.parseToken();
                    sUtil.skipSpace();
                    if (sUtil.parseChar() != '(') {
                        Convertor.inpos = j;
                        break;
                    }
                    sUtil.skipSpace();
                    if (sUtil.getToken("struct osd_bitmap *bitmap,int full_refresh")
                            || sUtil.getToken("struct osd_bitmap *bitmap, int full_refresh")
                            || sUtil.getToken("struct osd_bitmap *bitmap, int fullrefresh")) {
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ')') {
                            Convertor.inpos = j;
                            break;
                        }
                        if (sUtil.getChar() == ';') {
                            sUtil.skipLine();
                            continue;
                        }
                        if (Convertor.token[0].contains("vh_screenrefresh")) {
                            sUtil.putString((new StringBuilder()).append("public static VhUpdatePtr ").append(Convertor.token[0]).append(" = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) ").toString());
                            type = VH_SCREENREFRESH;
                            i3 = -1;
                            continue;
                        }

                    }

                    if (sUtil.getToken("unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom")) {
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ')') {
                            Convertor.inpos = j;
                            break;
                        }
                        if (sUtil.getChar() == ';') {
                            sUtil.skipLine();
                            continue;
                        }
                        if (Convertor.token[0].contains("vh_convert_color_prom")) {
                            sUtil.putString((new StringBuilder()).append("public static VhConvertColorPromPtr ").append(Convertor.token[0]).append(" = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) ").toString());
                            type = VH_CONVERT;
                            i3 = -1;
                            continue;
                        }
                        if (Convertor.token[0].contains("init_colors")) {
                            sUtil.putString((new StringBuilder()).append("public static VhConvertColorPromPtr ").append(Convertor.token[0]).append(" = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) ").toString());
                            type = VH_CONVERT;
                            i3 = -1;
                            continue;
                        }
                        if (Convertor.token[0].contains("init_palette")) {
                            sUtil.putString((new StringBuilder()).append("public static VhConvertColorPromPtr ").append(Convertor.token[0]).append(" = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) ").toString());
                            type = VH_CONVERT;
                            i3 = -1;
                            continue;
                        }

                    }
                    if (sUtil.getToken("unsigned char *palette, unsigned short *colortable, const unsigned char *color_prom")) {
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ')') {
                            Convertor.inpos = j;
                            break;
                        }
                        if (sUtil.getChar() == ';') {
                            sUtil.skipLine();
                            continue;
                        }
                        if (Convertor.token[0].contains("vh_convert_color_prom")) {
                            sUtil.putString((new StringBuilder()).append("public static VhConvertColorPromPtr ").append(Convertor.token[0]).append(" = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) ").toString());
                            type = VH_CONVERT;
                            i3 = -1;
                            continue;
                        }
                        if (Convertor.token[0].contains("init_colors")) {
                            sUtil.putString((new StringBuilder()).append("public static VhConvertColorPromPtr ").append(Convertor.token[0]).append(" = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) ").toString());
                            type = VH_CONVERT;
                            i3 = -1;
                            continue;
                        }
                        if (Convertor.token[0].contains("init_palette")) {
                            sUtil.putString((new StringBuilder()).append("public static VhConvertColorPromPtr ").append(Convertor.token[0]).append(" = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) ").toString());
                            type = VH_CONVERT;
                            i3 = -1;
                            continue;
                        }

                    }
                    if (sUtil.getToken("void *file,int read_or_write")|| sUtil.getToken("void *file, int read_or_write")) {
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ')') {
                            Convertor.inpos = i;
                            break;
                        }
                        if (sUtil.getChar() == ';') {
                            sUtil.skipLine();
                            continue;
                        }
                        //if (Convertor.token[0].contains("nvram")) {
                        sUtil.putString((new StringBuilder()).append("public static nvramPtr ").append(Convertor.token[0]).append("  = new nvramPtr() { public void handler(Object file, int read_or_write) ").toString());
                        type = NVRAM_H;
                        i3 = -1;
                        continue;
                        //}

                    }
                    if (sUtil.getToken("void"))//an to soma tis function einai (void)
                    {
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ')') {
                            Convertor.inpos = j;
                            break;
                        }
                        if (sUtil.getChar() == ';') {
                            sUtil.skipLine();
                            continue;
                        }

                        if (Convertor.token[0].contains("init_machine")) {
                            sUtil.putString((new StringBuilder()).append("public static InitMachinePtr ").append(Convertor.token[0]).append(" = new InitMachinePtr() { public void handler() ").toString());
                            type = MACHINE_INIT;
                            i3 = -1;
                            continue;
                        }
                        else if (Convertor.token[0].contains("machine_init")) {
                            sUtil.putString((new StringBuilder()).append("public static InitMachinePtr ").append(Convertor.token[0]).append(" = new InitMachinePtr() { public void handler() ").toString());
                            type = MACHINE_INIT;
                            i3 = -1;
                            continue;
                        }
                        else if (Convertor.token[0].startsWith("init_") && !Convertor.token[0].contains("table")) {
                            sUtil.putString((new StringBuilder()).append("public static InitDriverPtr ").append(Convertor.token[0]).append(" = new InitDriverPtr() { public void handler() ").toString());
                            type = DRIVER_INIT;
                            i3 = -1;
                            continue;
                        } else if (Convertor.token[0].contains("vh_stop")) {
                            sUtil.putString((new StringBuilder()).append("public static VhStopPtr ").append(Convertor.token[0]).append(" = new VhStopPtr() { public void handler() ").toString());
                            type = VH_STOP;
                            i3 = -1;
                            continue;
                        } else if (Convertor.token[0].contains("eof_callback")) {
                            sUtil.putString((new StringBuilder()).append("public static VhEofCallbackPtr ").append(Convertor.token[0]).append(" = new VhEofCallbackPtr() { public void handler() ").toString());
                            type = VH_EOF;
                            i3 = -1;
                            continue;
                        } else if (Convertor.token[0].contains("sh_update")) {
                            sUtil.putString((new StringBuilder()).append("public static ShUpdatePtr ").append(Convertor.token[0]).append(" = new ShUpdatePtr() { public void handler() ").toString());
                            type = SH_UPDATE;
                            i3 = -1;
                            continue;
                        } else if (Convertor.token[0].contains("sh_stop")) {
                            sUtil.putString((new StringBuilder()).append("public static ShStopPtr ").append(Convertor.token[0]).append(" = new ShStopPtr() { public void handler() ").toString());
                            type = SH_STOP;
                            i3 = -1;
                            continue;
                        }
                    }
                    Convertor.inpos = j;
                }
                break;
                case 'u':
                {
                    i = Convertor.inpos;
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
                break;
                case 'R': {
                    i = Convertor.inpos;
                    if (sUtil.getToken("ROM_START")) {
                        if (sUtil.parseChar() != '(') {
                            Convertor.inpos = i;
                            break;
                        }
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ')') {
                            Convertor.inpos = i;
                            break;
                        }
                        sUtil.putString((new StringBuilder()).append("static RomLoadPtr rom_").append(Convertor.token[0]).append(" = new RomLoadPtr(){ public void handler(){ ").toString());
                        continue;
                    }
                    if (sUtil.getToken("ROM_END")) {
                        sUtil.putString((new StringBuilder()).append("ROM_END(); }}; ").toString());
                        continue;
                    }
                    if (sUtil.getToken("ROM_REGION") || sUtil.getToken("ROM_LOAD")
                            || sUtil.getToken("ROM_RELOAD") || sUtil.getToken("ROM_CONTINUE")
                            || sUtil.getToken("ROM_LOAD_NIB_LOW") || sUtil.getToken("ROM_LOAD_NIB_HIGH")
                            || sUtil.getToken("ROM_RELOAD_NIB_LOW") || sUtil.getToken("ROM_RELOAD_NIB_HIGH")
                            || sUtil.getToken("ROM_LOAD_EVEN") || sUtil.getToken("ROM_RELOAD_EVEN")
                            || sUtil.getToken("ROM_LOAD_ODD") || sUtil.getToken("ROM_RELOAD_ODD")
                            || sUtil.getToken("ROM_LOAD_WIDE") || sUtil.getToken("ROM_RELOAD_WIDE")
                            || sUtil.getToken("ROM_LOAD_WIDE_SWAP") || sUtil.getToken("ROM_RELOAD_WIDE_SWAP")
                            || sUtil.getToken("ROM_LOAD_QUAD") || sUtil.getToken("ROM_LOAD_OPTIONAL")) {
                        i8++;
                        type2 = ROMDEF;
                        sUtil.skipSpace();
                        if (sUtil.parseChar() == '(') {
                            Convertor.inpos = i;
                        }
                    }
                    if (sUtil.getToken("READ_HANDLER(")) {
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.getToken(");"))//if it is front function skip it
                        {
                            sUtil.skipLine();
                            continue;
                        } else {
                            sUtil.putString("public static ReadHandlerPtr " + Convertor.token[0] + "  = new ReadHandlerPtr() { public int handler(int offset)");
                            type = READHANDLER;
                            i3 = -1;
                            Convertor.inpos += 1;
                            continue;
                        }
                    }

                    Convertor.inpos = i;
                }
                break;
                case 'p':
                {
                    int sd1=Convertor.inpos;
                    if(sUtil.getToken("paletteram"))
                    {
                        if (sUtil.parseChar() != '[') {
                            Convertor.inpos = sd1;
                            break;
                        }
                        Convertor.token[0] = sUtil.parseToken(']');
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ']') {
                            Convertor.inpos = sd1;
                            break;
                        }
                        else {
                            sUtil.skipSpace();

                            if (sUtil.parseChar() == '=') {
                                int g=Convertor.inpos;
                                if (sUtil.parseChar() == '=') {
                                    Convertor.inpos = sd1;
                                    break;
                                }
                                Convertor.inpos=g;
                                sUtil.skipSpace();
                                Convertor.token[1] = sUtil.parseToken(';');
                                sUtil.putString((new StringBuilder()).append("paletteram.write(").append(Convertor.token[0]).append(",").append(Convertor.token[1]).append(");").toString());
                                Convertor.inpos +=1;
                                break;
                            }
                            sUtil.putString((new StringBuilder()).append("paletteram.read(").append(Convertor.token[0]).append(")").toString());
                            Convertor.inpos -= 1;
                            continue;
                        }
                    }
                    int sd2=Convertor.inpos;
                    if(sUtil.getToken("paletteram_2"))
                    {
                        if (sUtil.parseChar() != '[') {
                            Convertor.inpos = sd2;
                            break;
                        }
                        Convertor.token[0] = sUtil.parseToken(']');
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ']') {
                            Convertor.inpos = sd2;
                            break;
                        }
                        else {
                            sUtil.skipSpace();
                            if (sUtil.parseChar() == '=') {
                                int g=Convertor.inpos;
                                if (sUtil.parseChar() == '=') {
                                    Convertor.inpos = sd2;
                                    break;
                                }
                                Convertor.inpos=g;
                                sUtil.skipSpace();
                                Convertor.token[1] = sUtil.parseToken(';');
                                sUtil.putString((new StringBuilder()).append("paletteram_2.write(").append(Convertor.token[0]).append(",").append(Convertor.token[1]).append(");").toString());
                                Convertor.inpos +=1;
                                break;
                            }
                            sUtil.putString((new StringBuilder()).append("paletteram_2.read(").append(Convertor.token[0]).append(")").toString());
                            Convertor.inpos -= 1;
                            continue;
                        }
                    }
                }
                break;
                case 'P':

                    if (sUtil.getToken("PORT_START")) {
                        sUtil.putString((new StringBuilder()).append("PORT_START(); ").toString());
                        continue;
                    }
                    int h = Convertor.inpos;
                    if (sUtil.getToken("PORT_DIPNAME") || sUtil.getToken("PORT_BIT") || sUtil.getToken("PORT_DIPSETTING") || sUtil.getToken("PORT_BITX") || sUtil.getToken("PORT_SERVICE") || sUtil.getToken("PORT_BIT_IMPULSE") || sUtil.getToken("PORT_ANALOG")|| sUtil.getToken("PORT_ANALOGX")) {
                        i8++;
                        type2 = NEWINPUT;
                        sUtil.skipSpace();
                        if (sUtil.parseChar() == '(') {
                            Convertor.inpos = h;
                        }
                    }
                    break;
                case 'W': {
                    i = Convertor.inpos;
                    if (sUtil.getToken("WRITE_HANDLER(")) {
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.getToken(");"))//if it is a front function skip it
                        {
                            sUtil.skipLine();
                            continue;
                        } else {
                            sUtil.putString("public static WriteHandlerPtr " + Convertor.token[0] + " = new WriteHandlerPtr() {public void handler(int offset, int data)");
                            type = WRITEHANDLER;
                            i3 = -1;
                            Convertor.inpos += 1;
                            continue;
                        }
                    }

                    Convertor.inpos = i;
                }
                break;
                case '{': {
                    if (type == MEMORYREAD) {
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
                    }
                    else if (type == GFXLAYOUT) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 0) {
                            Convertor.outbuf[(Convertor.outpos++)] = '(';
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 4) || (insideagk[0] == 5) || (insideagk[0] == 6)|| (insideagk[0] == 7))) {
                            sUtil.putString("new int[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == YM3812interface) {
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
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 3))) {
                            sUtil.putString("new WriteYmHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    else if (type == OKIM6295interface) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 0) {
                            Convertor.outbuf[(Convertor.outpos++)] = '(';
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 1))) {
                            sUtil.putString("new int[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 2))) {
                            sUtil.putString("new int[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 3))) {
                            sUtil.putString("new int[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    else if (type == YM2413interface) {
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
                    }
                    else if (type == POKEYinterface) {
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
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 3))) {
                            sUtil.putString("new ReadHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 4))) {
                            sUtil.putString("new ReadHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 5))) {
                            sUtil.putString("new ReadHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 6))) {
                            sUtil.putString("new ReadHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 7))) {
                            sUtil.putString("new ReadHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 8))) {
                            sUtil.putString("new ReadHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 9))) {
                            sUtil.putString("new ReadHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 10))) {
                            sUtil.putString("new ReadHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 11))) {
                            sUtil.putString("new ReadHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }

                    }
                    else if (type == YM2203interface) {
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
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 3))) {
                            sUtil.putString("new ReadHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 4))) {
                            sUtil.putString("new ReadHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 5))) {
                            sUtil.putString("new WriteHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 6))) {
                            sUtil.putString("new WriteHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 7))) {
                            sUtil.putString("new WriteYmHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    else if (type == YM2151interface) {
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
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 3))) {
                            sUtil.putString("new WriteYmHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 4))) {
                            sUtil.putString("new WriteHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    else if (type == YM2610interface) {
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
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 3))) {
                            sUtil.putString("new ReadHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 4))) {
                            sUtil.putString("new ReadHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 5))) {
                            sUtil.putString("new WriteHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 6))) {
                            sUtil.putString("new WriteHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 7))) {
                            sUtil.putString("new WriteYmHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 8))) {
                            sUtil.putString("new int[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 9))) {
                            sUtil.putString("new int[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 10))) {
                            sUtil.putString("new int[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    else if (type == UPD7759_interface) {
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
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 3))) {
                            sUtil.putString("new int[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 5))) {
                            sUtil.putString("new irqcallbackPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    else if (type == CustomSound_interface) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 0) {
                            Convertor.outbuf[(Convertor.outpos++)] = '(';
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    else if (type == YM3526interface) {
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
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 3))) {
                            sUtil.putString("new WriteYmHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == MSM5205interface) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 0) {
                            Convertor.outbuf[(Convertor.outpos++)] = '(';
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 2))) {
                            sUtil.putString("new vclk_interruptPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 3))) {
                            sUtil.putString("new int[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 4))) {
                            sUtil.putString("new int[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == AY8910interface) {
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
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 3))) {
                            sUtil.putString("new ReadHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 4))) {
                            sUtil.putString("new ReadHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 5))) {
                            sUtil.putString("new WriteHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 1) || (insideagk[0] == 6))) {
                            sUtil.putString("new WriteHandlerPtr[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == MACHINEDRIVER) {
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
                        else if ((i3 == 1) && (insideagk[0] == 9))
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
                    } else if (type == GFXDECODE) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 1) {
                            sUtil.putString("new GfxDecodeInfo(");
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
                    } else if (type == NESinterface) {
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
                    } else if (type == K054539interface) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 0) {
                            Convertor.outbuf[(Convertor.outpos++)] = '(';
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == EEPROM_interface) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 0) {
                            Convertor.outbuf[(Convertor.outpos++)] = '(';
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == Samplesinterface) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 0) {
                            Convertor.outbuf[(Convertor.outpos++)] = '(';
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (type == VLM5030interface
                            || type == RF5C68interface
                            || type == k051649_interface
                            || type == namco_interface) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 0) {
                            Convertor.outbuf[(Convertor.outpos++)] = '(';
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    else if (type == C140interface) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 0) {
                            Convertor.outbuf[(Convertor.outpos++)] = '(';
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    else if (type == astrocade_interface) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 0) {
                            Convertor.outbuf[(Convertor.outpos++)] = '(';
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 2))) {
                            sUtil.putString("new int[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    else if (type == hc55516_interface) {
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
                    }
                    else if (type == SN76477interface) {
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
                        if ((i3 == 1) && ((insideagk[0] == 2))) {
                            sUtil.putString("new double[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 3))) {
                            sUtil.putString("new double[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 4))) {
                            sUtil.putString("new double[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 5))) {
                            sUtil.putString("new double[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 6))) {
                            sUtil.putString("new double[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 7))) {
                            sUtil.putString("new double[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 8))) {
                            sUtil.putString("new double[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 9))) {
                            sUtil.putString("new double[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 10))) {
                            sUtil.putString("new double[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 11))) {
                            sUtil.putString("new double[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 12))) {
                            sUtil.putString("new double[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 13))) {
                            sUtil.putString("new double[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 14))) {
                            sUtil.putString("new double[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 15))) {
                            sUtil.putString("new double[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 16))) {
                            sUtil.putString("new double[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                        if ((i3 == 1) && ((insideagk[0] == 17))) {
                            sUtil.putString("new double[] {");
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    else if (type == DACinterface) {
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
                    } else if (type == PLOT_PIXEL || type == MARK_DIRTY ||
                            type == PLOT_BOX || type == SH_START ||
                            type == SH_STOP || type == SH_UPDATE ||
                            type == VH_CONVERT || type == VH_SCREENREFRESH ||
                            type == VH_START || type == DRIVER_INIT ||
                            type == READHANDLER || type == WRITEHANDLER ||
                            type == MACHINE_INTERRUPT || type == MACHINE_INIT ||
                            type == VH_STOP || type == VH_EOF || type == NVRAM_H || type==TIMERCALLBACK || type==TILEINFO
                            || type == vclk_interruptPtr
                            || type == K052109
                            || type == K051960
                            || type == konami_cpu_setlines_callbackPtr) {
                        i3++;
                    }
                }
                break;
                case '}': {
                    if ((type == MEMORYREAD) || (type == MEMORYWRITE) || (type == IOREAD) || (type == IOWRITE) || (type == GFXDECODE)) {
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
                    } else if (type == YM3812interface) {
                        i3--;
                        if (i3 == -1) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    }
                    else if (type == OKIM6295interface) {
                        i3--;
                        if (i3 == -1) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    }
                    else if (type == YM2413interface) {
                        i3--;
                        if (i3 == -1) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    }
                    else if (type == POKEYinterface) {
                        i3--;
                        if (i3 == -1) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    }
                    else if (type == YM2203interface) {
                        i3--;
                        if (i3 == -1) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    }
                    else if (type == UPD7759_interface) {
                        i3--;
                        if (i3 == -1) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    }
                    else if (type == CustomSound_interface) {
                        i3--;
                        if (i3 == -1) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    }
                    else if (type == YM2151interface) {
                        i3--;
                        if (i3 == -1) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    }
                    else if (type == YM2610interface) {
                        i3--;
                        if (i3 == -1) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    }
                    else if (type == YM3526interface) {
                        i3--;
                        if (i3 == -1) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    } else if (type == MSM5205interface) {
                        i3--;
                        if (i3 == -1) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    } else if (type == AY8910interface) {
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
                    } else if (type == VLM5030interface
                            || type ==C140interface
                            || type == NESinterface
                            || type == SN76496interface
                            || type == K054539interface
                            || type == EEPROM_interface
                            || type == astrocade_interface
                            || type == RF5C68interface
                            || type == k051649_interface
                            || type == hc55516_interface
                            || type == namco_interface
                            || type == SN76477interface) {
                        i3--;
                        if (i3 == -1) {
                            Convertor.outbuf[(Convertor.outpos++)] = ')';
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    }    else if (type == Samplesinterface) {
                        i3--;
                        if (i3 == -1) {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    } else if (type == TIMERCALLBACK || type == TILEINFO ||
                            type == vclk_interruptPtr
                            || type == K052109
                            || type == K051960
                            || type == konami_cpu_setlines_callbackPtr
                            || type == NVRAM_H
                            || type == VH_EOF
                            || type == PLOT_PIXEL
                            || type == MARK_DIRTY || type == PLOT_BOX || type == SH_START || type == SH_STOP || type == SH_UPDATE || type == VH_CONVERT || type == VH_SCREENREFRESH || type == VH_START || type == READHANDLER || type == WRITEHANDLER || type == MACHINE_INTERRUPT || type == MACHINE_INIT || type == DRIVER_INIT || type == VH_STOP) {
                        i3--;
                        if (i3 == -1) {
                            sUtil.putString("} };");
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    } else if (type == MACHINEDRIVER) {
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
                        else if ((i3 == 0) && (insideagk[0] == 9))
                        {
                            Convertor.outbuf[(Convertor.outpos++)] = 41;
                            Convertor.inpos += 1;
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
                    }

                }
                break;
                case ',':
                    if ((type != -1)) {
                        if (i3 != -1) {
                            insideagk[i3] += 1;
                        }
                    }
                    break;
                case '&': {
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
                }
                break;
                case '0':
                    i = Convertor.inpos;
                    if (sUtil.getToken("0")) {
                        Convertor.inpos = i;
                        /*if (type == GAMEDRIVER) {
                            sUtil.putString("null");
                            Convertor.inpos += 1;
                            continue;
                        }*/
                        if (type == MACHINEDRIVER) {
                            if ((i3 == 0) && ((insideagk[i3] == 3) || (insideagk[i3] == 5) || (insideagk[i3] == 6) ||  (insideagk[i3] == 10) || (insideagk[i3] == 12) || (insideagk[i3] == 14) || (insideagk[i3] == 15))) {
                                sUtil.putString("null");
                                Convertor.inpos += 1;
                                continue;
                            } else if ((i3 == 0) /*&& (type3==1)*/ && ((insideagk[i3] == 4) || (insideagk[i3] == 8) || (insideagk[i3] == 9) || (insideagk[i3] == 11)  || (insideagk[i3] == 13) || (insideagk[i3] == 14) || (insideagk[i3] == 15) || (insideagk[i3] == 16))) {
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
                        if(type == CustomSound_interface)
                        {
                            sUtil.putString("null");
                            Convertor.inpos += 1;
                        }
                        if(type == POKEYinterface)
                        {
                            sUtil.putString("null");
                            Convertor.inpos += 1;
                        }

                    }
                    break;
                case '-':
                    if (type == PLOT_BOX)//workaround
                        break;
                    char c3 = sUtil.getNextChar();
                    if (c3 != '>') {
                        break;
                    }
                    Convertor.outbuf[Convertor.outpos++] = '.';
                    Convertor.inpos += 2;
                    break;
                case 'e': {
                    /*if (sUtil.getToken("extern"))//if it starts with extern skip it
                    {
                        sUtil.skipLine();
                        continue;
                    }*/
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
                }
                break;
                case ')': {
                    if (type2 == ROMDEF) {
                        i8--;
                        Convertor.outbuf[(Convertor.outpos++)] = ')';
                        Convertor.outbuf[(Convertor.outpos++)] = ';';
                        Convertor.inpos += 2;
                        if (sUtil.getChar() == ')') {//fix for badcrc case
                            Convertor.outpos -= 1;
                            Convertor.outbuf[(Convertor.outpos++)] = ')';
                            Convertor.outbuf[(Convertor.outpos++)] = ';';
                            Convertor.inpos += 1;
                        }
                        type2 = -1;
                        continue;
                    }
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
                }
                break;
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
                case 'G': {
                    i = Convertor.inpos;
                    if (sUtil.getToken("GAME") || sUtil.getToken("GAMEX")) {
                        sUtil.skipSpace();
                        if(sUtil.getChar()!='(')
                        {
                            Convertor.inpos = i;
                            break;
                        }
                        else
                        {
                            Convertor.inpos+=1;
                        }
                        if (sUtil.getChar() == ')')//fix an issue in driverH
                        {
                            Convertor.inpos = i;
                            break;
                        }
                        type = GAMEDRIVER;
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseTokenGameDriv();//year
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
