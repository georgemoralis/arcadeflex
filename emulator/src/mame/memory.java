package mame;

import static mame.driverH.*;
import static arcadeflex.osdepend.*;
import static arcadeflex.libc.*;
import static arcadeflex.libc_old.*;
import java.util.Arrays;
import static mame.memoryH.*;
import static mame.mame.*;
import static mame.cpuintrf.*;
import static mame.common.*;
import static mame.commonH.*;


public class memory {

    /* Convenience macros - not in cpuintrf.h because they shouldn't be used by everyone */
    public static int ADDRESS_BITS(int index) {
        return (cpuintf[Machine.drv.cpu[index].cpu_type & ~CPU_FLAGS_MASK].address_bits);
    }

    public static int ABITS1(int index) {
        return (cpuintf[Machine.drv.cpu[index].cpu_type & ~CPU_FLAGS_MASK].abits1);
    }

    public static int ABITS2(int index) {
        return (cpuintf[Machine.drv.cpu[index].cpu_type & ~CPU_FLAGS_MASK].abits2);
    }

    public static int ABITS3(int index) {
        return (0);
    }

    public static int ABITSMIN(int index) {
        return (cpuintf[Machine.drv.cpu[index].cpu_type & ~CPU_FLAGS_MASK].abitsmin);
    }
    /*TODO*/ //#if LSB_FIRST
/*TODO*/ //	#define BYTE_XOR_BE(a) ((a) ^ 1)
/*TODO*/ //	#define BYTE_XOR_LE(a) (a)
/*TODO*/ //#else
/*TODO*/ //	#define BYTE_XOR_BE(a) (a)
/*TODO*/ //	#define BYTE_XOR_LE(a) ((a) ^ 1)
/*TODO*/ //#endif
/*TODO*/ //
    public static UBytePtr OP_RAM = new UBytePtr();
    public static UBytePtr OP_ROM = new UBytePtr();
    
    static void SET_OP_RAMROM(UBytePtr _base)
    {
        OP_ROM = new UBytePtr(_base, (OP_ROM.base - OP_RAM.base));
        OP_RAM = new UBytePtr(_base);
    }

    public static UByte ophw = new UByte(); /* op-code hardware number */

    public static ExtMemory[] ext_memory = new ExtMemory[MAX_EXT_MEMORY];
    public static UBytePtr[] ramptr = new UBytePtr[MAX_CPU];
    public static UBytePtr[] romptr = new UBytePtr[MAX_CPU];

    /* element shift bits, mask bits */
    public static int[][] mhshift = new int[MAX_CPU][3];
    public static int[][] mhmask = new int[MAX_CPU][3];
    /* pointers to port structs */
    /* ASG: port speedup */
    static IOReadPort[][] readport = new IOReadPort[MAX_CPU][];
    static IOWritePort[][] writeport = new IOWritePort[MAX_CPU][];
    static int[] portmask = new int[MAX_CPU];
    static int[] readport_size = new int[MAX_CPU];
    static int[] writeport_size = new int[MAX_CPU];
    /* HJB 990210: removed 'static' for access by assembly CPU core memory handlers */
    static IOReadPort[]  cur_readport;
    static IOWritePort[] cur_writeport;
    static int cur_portmask;
    /* current hardware element map */
    public static UBytePtr[] cur_mr_element=new UBytePtr[MAX_CPU];
    public static UBytePtr[] cur_mw_element=new UBytePtr[MAX_CPU];


    /* sub memory/port hardware element map */
    /* HJB 990210: removed 'static' for access by assembly CPU core memory handlers */
    static UBytePtr readhardware = new UBytePtr(new char[MH_ELEMAX<<MH_SBITS]);
    static UBytePtr writehardware = new UBytePtr(new char[MH_ELEMAX<<MH_SBITS]);

    /* memory hardware element map */
    /* value:                      */
    static final int HT_RAM = 0;		/* RAM direct        */
    static final int HT_BANK1 = 1;		/* bank memory #1    */
    static final int HT_BANK2 = 2;		/* bank memory #2    */
    static final int HT_BANK3 = 3;		/* bank memory #3    */
    static final int HT_BANK4 = 4;		/* bank memory #4    */
    static final int HT_BANK5 = 5;		/* bank memory #5    */
    static final int HT_BANK6 = 6;		/* bank memory #6    */
    static final int HT_BANK7 = 7;		/* bank memory #7    */
    static final int HT_BANK8 = 8;		/* bank memory #8    */
    static final int HT_BANK9 = 9;		/* bank memory #9    */
    static final int HT_BANK10 = 10;	/* bank memory #10   */
    static final int HT_BANK11 = 11;	/* bank memory #11   */
    static final int HT_BANK12 = 12;	/* bank memory #12   */
    static final int HT_BANK13 = 13;	/* bank memory #13   */
    static final int HT_BANK14 = 14;	/* bank memory #14   */
    static final int HT_BANK15 = 15;	/* bank memory #15   */
    static final int HT_BANK16 = 16;	/* bank memory #16   */
    static final int HT_NON = 17;	/* non mapped memory */
    static final int HT_NOP = 18;	/* NOP memory        */
    static final int HT_RAMROM = 19;	/* RAM ROM memory    */
    static final int HT_ROM = 20;	/* ROM memory        */
    static final int HT_USER = 21;	/* user functions    */
    /* [MH_HARDMAX]-0xff	  link to sub memory element  */
    /*                        (value-MH_HARDMAX)<<MH_SBITS -> element bank */
    static final int HT_BANKMAX = (HT_BANK1 + MAX_BANKS - 1);

    /* memory hardware handler */
    /* HJB 990210: removed 'static' for access by assembly CPU core memory handlers */
    static ReadHandlerPtr[] memoryreadhandler = new ReadHandlerPtr[MH_HARDMAX]; //mem_read_handler memoryreadhandler[MH_HARDMAX];
    static int[] memoryreadoffset = new int[MH_HARDMAX];
    static WriteHandlerPtr[] memorywritehandler = new WriteHandlerPtr[MH_HARDMAX];//mem_write_handler memorywritehandler[MH_HARDMAX];
    static int[] memorywriteoffset = new int[MH_HARDMAX];
    /* bank ram base address; RAM is bank 0 */
    static UBytePtr[] cpu_bankbase = new UBytePtr[HT_BANKMAX + 1];

    static int[] bankreadoffset = new int[HT_BANKMAX + 1];
    static int[] bankwriteoffset = new int[HT_BANKMAX + 1];

     ///* override OP base handler */
    public static opbase_handlerPtr[] setOPbasefunc = new opbase_handlerPtr[MAX_CPU];
    public static opbase_handlerPtr OPbasefunc;

    /* current cpu current hardware element map point */
    public static char[] cur_mrhard; //TODO make it UByte
    public static char[] cur_mwhard; //TODO make it UByte

    /* empty port handler structures */
    public static IOReadPort[] empty_readport = {new IOReadPort(-1)};
    public static IOWritePort[] empty_writeport = {new IOWritePort(-1)};
    /**
     * *************************************************************************
     *
     * Memory read handling
     *
     **************************************************************************
     */
    public static ReadHandlerPtr mrh_ram = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[0].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank1 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[1].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank2 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[2].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank3 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[3].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank4 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[4].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank5 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[5].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank6 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[6].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank7 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[7].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank8 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[8].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank9 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[9].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank10 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[10].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank11 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[11].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank12 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[12].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank13 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[13].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank14 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[14].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank15 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[15].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank16 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[16].read(offset);
        }
    };
    static ReadHandlerPtr bank_read_handler[] = {
        mrh_ram, mrh_bank1, mrh_bank2, mrh_bank3, mrh_bank4, mrh_bank5, mrh_bank6, mrh_bank7,
        mrh_bank8, mrh_bank9, mrh_bank10, mrh_bank11, mrh_bank12, mrh_bank13, mrh_bank14, mrh_bank15,
        mrh_bank16
    };
    public static ReadHandlerPtr mrh_error = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (errorlog!=null) fprintf(errorlog,"CPU #%d PC %04x: warning - read %02x from unmapped memory address %04x\n",cpu_getactivecpu(),cpu_get_pc(),Integer.valueOf(cpu_bankbase[0].read(offset)),offset);
            return cpu_bankbase[0].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_error_sparse = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (errorlog!=null) fprintf(errorlog,"CPU #%d PC %08x: warning - read unmapped memory address %08x\n",cpu_getactivecpu(),cpu_get_pc(),offset);
            return 0;
        }
    };
    public static ReadHandlerPtr mrh_error_sparse_bit = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (errorlog!=null) fprintf(errorlog,"CPU #%d PC %08x: warning - read unmapped memory bit addr %08x (byte addr %08x)\n",cpu_getactivecpu(),cpu_get_pc(),offset<<3, offset);
            return 0;
        }
    };
    public static ReadHandlerPtr mrh_nop = new ReadHandlerPtr() {
        public int handler(int offset) {
            return 0;
        }
    };
    /**
     * *************************************************************************
     *
     * Memory write handling
     *
     **************************************************************************
     */
    public static WriteHandlerPtr mwh_ram = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[0].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank1 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[1].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank2 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[2].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank3 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[3].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank4 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[4].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank5 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[5].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank6 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[6].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank7 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[7].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank8 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[8].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank9 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[9].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank10 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[10].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank11 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[11].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank12 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[12].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank13 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[13].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank14 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[14].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank15 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[15].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank16 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[16].write(offset, data);
        }
    };
    static WriteHandlerPtr bank_write_handler[] = {
        mwh_ram, mwh_bank1, mwh_bank2, mwh_bank3, mwh_bank4, mwh_bank5, mwh_bank6, mwh_bank7,
        mwh_bank8, mwh_bank9, mwh_bank10, mwh_bank11, mwh_bank12, mwh_bank13, mwh_bank14, mwh_bank15,
        mwh_bank16
    };
    public static WriteHandlerPtr mwh_error = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (errorlog!=null) fprintf(errorlog,"CPU #%d PC %04x: warning - write %02x to unmapped memory address %04x\n",cpu_getactivecpu(),cpu_get_pc(),data,offset);
            cpu_bankbase[0].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_error_sparse = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
           if (errorlog!=null) fprintf(errorlog,"CPU #%d PC %08x: warning - write %02x to unmapped memory address %08x\n",cpu_getactivecpu(),cpu_get_pc(),data,offset);
        }
    };
    public static WriteHandlerPtr mwh_error_sparse_bit = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
             if (errorlog!=null) fprintf(errorlog,"CPU #%d PC %08x: warning - write %02x to unmapped memory bit addr %08x\n",cpu_getactivecpu(),cpu_get_pc(),data,offset<<3);
        }
    };
    public static WriteHandlerPtr mwh_rom = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
             if (errorlog!=null) fprintf(errorlog,"CPU #%d PC %04x: warning - write %02x to ROM address %04x\n",cpu_getactivecpu(),cpu_get_pc(),data,offset);
        }
    };
    public static WriteHandlerPtr mwh_ramrom = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*TODO*/ //	cpu_bankbase[0][offset] = cpu_bankbase[0][offset + (OP_ROM - OP_RAM)] = data;
//TODO recheck probably OK but not sure.....
            cpu_bankbase[0].write(offset+(OP_ROM.base-OP_RAM.base),data);
            cpu_bankbase[0].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_nop = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
        }
    };

    /***************************************************************************

      Memory structure building

    ***************************************************************************/

    /* return element offset */
    static UBytePtr get_element(UBytePtr element, int ad, int elemask, UBytePtr subelement, int []ele_max)
    {
            UByte hw= new UByte();
            hw.set(element.read(ad));
            int i, ele;
            int banks = (elemask / (1 << MH_SBITS)) + 1;

            if (hw.read() >= MH_HARDMAX) 
            {
                return new UBytePtr(subelement, (hw.read() - MH_HARDMAX) << MH_SBITS);   
            }

            /* create new element block */
            if ((ele_max[0]) + banks > MH_ELEMAX)
            {
                if (errorlog!=null) fprintf(errorlog,"memory element size over \n");
                return null;
            }
            /* get new element nunber */
            ele = ele_max[0];
            (ele_max[0]) += banks;

            if (errorlog!=null) fprintf(errorlog,"create element %2d(%2d)\n",ele,banks);
            
            /* set link mark to current element */
            element.write(ad,(ele + MH_HARDMAX));
            /* get next subelement top */
             subelement = new UBytePtr(subelement, ele << MH_SBITS);
            /* initialize new block */
            for (i = 0; i < (1 << MH_SBITS); i++)
                subelement.write(i,hw.read());

            return subelement;
        }
    static void set_element(int cpu, UBytePtr celement, int sp, int ep, UByte type, UBytePtr subelement, int[] ele_max) {
        int i;
        int edepth = 0;
        int shift, mask;
        UBytePtr eele = celement;
        UBytePtr sele = celement;
        UBytePtr ele = new UBytePtr();
        int ss,sb,eb,ee;


         if (errorlog!=null) fprintf(errorlog,"set_element %8X-%8X = %2X\n",sp,ep,Integer.valueOf(type.read()));

         if( /*(unsigned int)*/ sp > /*(unsigned int)*/ ep ) return;
         do{
         mask  = mhmask[cpu][edepth];
         shift = mhshift[cpu][edepth];

         /* center element */
         ss = /*(unsigned int)*/ sp >> shift;
         sb = /*(unsigned int)*/ sp!=0 ? (/*(unsigned int)*/ (sp-1) >> shift) + 1 : 0;
         eb = (/*(unsigned int)*/ (ep+1) >> shift) - 1;
         ee = /*(unsigned int)*/ ep >> shift;
         
   /*tempdebug*/       //if (errorlog!=null) fprintf(errorlog,"center_element ss=%8X sb=%8X eb=%8X ee=%8X\n",ss,sb,eb,ee);

         if( sb <= eb )
         {
            if( (sb|mask)==(eb|mask) )
            {
                /* same reasion */
                ele = (sele!=null ? sele : eele);
                for( i = sb ; i <= eb ; i++ ){
                    ele.write(i & mask,type.read());
                }
            }
            else
            {
                if( sele!=null ) for( i = sb ; i <= (sb|mask) ; i++ )
                sele.write(i & mask,type.read());
                if( eele!=null ) for( i = eb&(~mask) ; i <= eb ; i++ )
                eele.write(i & mask,type.read());
            }
         }

         edepth++;

         if( ss == sb ) 
         {
             sele = null;
         }
         else 
         {
             sele = get_element( sele , ss & mask , mhmask[cpu][edepth] ,subelement , ele_max );
         }
         if( ee == eb ) 
         {
             eele = null;
         }
         else 
         {
             eele = get_element( eele , ee & mask , mhmask[cpu][edepth] ,subelement , ele_max );
         }

         }while( sele!=null || eele!=null );
    }


    /* ASG 980121 -- allocate all the external memory */
    public static int memory_allocate_ext() {
        int ext_ptr = 0;
        int cpu;

        /* a change for MESS */
        if (Machine.gamedrv.rom == null) {
            return 1;
        }

        /* loop over all CPUs */
        for (cpu = 0; cpu < cpu_gettotalcpu(); cpu++) {
            //MemoryReadAddress mra;
            //MemoryWriteAddress mwa;

            int region = REGION_CPU1 + cpu;
            int size = memory_region_length(region);

            /* now it's time to loop */
            while (true) {
                int lowest = 0x7fffffff, end, lastend;

                /* find the base of the lowest memory region that extends past the end */

                for (int mra = 0; Machine.drv.cpu[cpu].memory_read[mra].start != -1; mra++) {
                    if (Machine.drv.cpu[cpu].memory_read[mra].end >= size
                            && Machine.drv.cpu[cpu].memory_read[mra].start < lowest) {
                        lowest = Machine.drv.cpu[cpu].memory_read[mra].start;
                    }
                }

                for (int mwa = 0; Machine.drv.cpu[cpu].memory_write[mwa].start != -1; mwa++) {
                    if (Machine.drv.cpu[cpu].memory_write[mwa].end >= size
                            && Machine.drv.cpu[cpu].memory_write[mwa].start < lowest) {
                        lowest = Machine.drv.cpu[cpu].memory_write[mwa].start;
                    }
                }

                /* done if nothing found */
                if (lowest == 0x7fffffff) {
                    break;
                }

                /* now loop until we find the end of this contiguous block of memory */
                lastend = -1;
                end = lowest;
                while (end != lastend) {
                    lastend = end;

                    /* find the base of the lowest memory region that extends past the end */
                    for (int mra = 0; Machine.drv.cpu[cpu].memory_read[mra].start != -1; mra++) {
                        if (Machine.drv.cpu[cpu].memory_read[mra].start <= end && Machine.drv.cpu[cpu].memory_read[mra].end > end) {
                            end = Machine.drv.cpu[cpu].memory_read[mra].end + 1;
                        }
                    }

                    for (int mwa = 0; Machine.drv.cpu[cpu].memory_write[mwa].start != -1; mwa++) {
                        if (Machine.drv.cpu[cpu].memory_write[mwa].start <= end && Machine.drv.cpu[cpu].memory_write[mwa].end > end) {
                            end = Machine.drv.cpu[cpu].memory_write[mwa].end + 1;
                        }
                    }
                }
                /* time to allocate */
                ext_memory[ext_ptr].start = lowest;
                ext_memory[ext_ptr].end = end - 1;
                ext_memory[ext_ptr].region = region;
                ext_memory[ext_ptr].data = new UBytePtr(end - lowest);

                /* if that fails, we're through 
                 if (!ext - > data) {
                 return 0;
                 }
                 */
                /* reset the memory */
                memset(ext_memory[ext_ptr].data, 0, end - lowest);
                size = ext_memory[ext_ptr].end + 1;
                ext_ptr++;
            }
        }

        return 1;
    }
    /*TODO*/ //unsigned char *findmemorychunk(int cpu, int offset, int *chunkstart, int *chunkend)
/*TODO*/ //{
/*TODO*/ //	int region = REGION_CPU1+cpu;
/*TODO*/ //	struct ExtMemory *ext;
/*TODO*/ //
/*TODO*/ //	/* look in external memory first */
/*TODO*/ //	for (ext = ext_memory; ext->data; ext++)
/*TODO*/ //		if (ext->region == region && ext->start <= offset && ext->end >= offset)
/*TODO*/ //		{
/*TODO*/ //			*chunkstart = ext->start;
/*TODO*/ //			*chunkend = ext->end;
/*TODO*/ //			return ext->data;
/*TODO*/ //		}
/*TODO*/ //
/*TODO*/ //	/* return RAM */
/*TODO*/ //	*chunkstart = 0;
/*TODO*/ //	*chunkend = memory_region_length(region) - 1;
/*TODO*/ //	return ramptr[cpu];
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ //

    public static UBytePtr memory_find_base(int cpu, int offset) {

        int region = REGION_CPU1 + cpu;

        /* look in external memory first */
        for (ExtMemory ext : ext_memory) {
            if (ext.data == null) {
                break;
            }
            if (ext.region == region && ext.start <= offset && ext.end >= offset) {
                return new UBytePtr(ext.data, (offset - ext.start));
            }
        }
        return new UBytePtr(ramptr[cpu], offset);
    }

    /* make these static so they can be used in a callback by game drivers */
    static int rdelement_max = 0;
    static int wrelement_max = 0;
    static int rdhard_max = HT_USER;
    static int wrhard_max = HT_USER;

    // return = FALSE:can't allocate element memory
    public static int memory_init() {
        /*java code intialaze ext_memory stuff elements (shadow) */
        for (int x = 0; x < MAX_EXT_MEMORY; x++) {
            ext_memory[x] = new ExtMemory();
        }
        /*for (int x = 0; x < readhardware.length; x++) {
            readhardware[x] = new UByte();
        }
        for (int x = 0; x < writehardware.length; x++) {
            writehardware[x] = new UByte();
        }*/
        /*end of java code */
        int i, cpu;
        MemoryReadAddress memoryread;
        MemoryWriteAddress memorywrite;
        MemoryReadAddress mra;
        MemoryWriteAddress mwa;
        IOReadPort ioread;
        IOWritePort iowrite;
        UByte hardware = new UByte();
        int abits1, abits2, abits3, abitsmin;
        rdelement_max = 0;
        wrelement_max = 0;
        rdhard_max = HT_USER;
        wrhard_max = HT_USER;

        for (cpu = 0; cpu < MAX_CPU; cpu++) {
            cur_mr_element[cpu] = cur_mw_element[cpu] = null;
        }


        ophw.set((char) 0xff);


        /* ASG 980121 -- allocate external memory */
        if (memory_allocate_ext() == 0) {
            return 0;
        }


        for (cpu = 0; cpu < cpu_gettotalcpu(); cpu++) {
            setOPbasefunc[cpu] = null;

            ramptr[cpu] = romptr[cpu] = memory_region(REGION_CPU1 + cpu);

            /* initialize the memory base pointers for memory hooks */
            int _mra = 0;
            if (Machine.drv.cpu[cpu].memory_read != null && Machine.drv.cpu[cpu].memory_read[_mra] != null) {
                while (Machine.drv.cpu[cpu].memory_read[_mra].start != -1) {
                    //                              if (_mra.base) *_mra.base = memory_find_base (cpu, _mra.start);
                    //                              if (_mra.size) *_mra.size = _mra.end - _mra.start + 1;
                    _mra++;
                }
            }

            int _mwa = 0;
            if (Machine.drv.cpu[cpu].memory_write != null && Machine.drv.cpu[cpu].memory_write[_mwa] != null) {
                while (Machine.drv.cpu[cpu].memory_write[_mwa].start != -1) {
                    if (Machine.drv.cpu[cpu].memory_write[_mwa].base != null) {
                        UBytePtr b = memory_find_base(cpu, Machine.drv.cpu[cpu].memory_write[_mwa].start);
                        Machine.drv.cpu[cpu].memory_write[_mwa].base.memory = b.memory;
                        Machine.drv.cpu[cpu].memory_write[_mwa].base.base = b.base;
                    }
                    if (Machine.drv.cpu[cpu].memory_write[_mwa].size != null) {
                        Machine.drv.cpu[cpu].memory_write[_mwa].size[0] = Machine.drv.cpu[cpu].memory_write[_mwa].end - Machine.drv.cpu[cpu].memory_write[_mwa].start + 1;
                    }
                    _mwa++;
                }
            }



            /* initialize port structures */
            readport_size[cpu] = 0;
            writeport_size[cpu] = 0;
            readport[cpu] = null;
            writeport[cpu] = null;

            /* install port handlers - at least an empty one */
            int ioread_ptr = 0;
            if (Machine.drv.cpu[cpu].port_read == null) {
                Machine.drv.cpu[cpu].port_read = empty_readport;
            }
            while (true) {
                if (install_port_read_handler_common(cpu, Machine.drv.cpu[cpu].port_read[ioread_ptr].start, Machine.drv.cpu[cpu].port_read[ioread_ptr].end, Machine.drv.cpu[cpu].port_read[ioread_ptr].handler, Machine.drv.cpu[cpu].port_read[ioread_ptr]._handler, 0) == null) {
                    memory_shutdown();
                    return 0;
                }

                if (Machine.drv.cpu[cpu].port_read[ioread_ptr].start == -1) {
                    break;
                }

                ioread_ptr++;
            }
            int iowrite_ptr = 0;
            if (Machine.drv.cpu[cpu].port_write == null) {
                Machine.drv.cpu[cpu].port_write = empty_writeport;
            }

            while (true) {
                if (install_port_write_handler_common(cpu, Machine.drv.cpu[cpu].port_write[iowrite_ptr].start, Machine.drv.cpu[cpu].port_write[iowrite_ptr].end, Machine.drv.cpu[cpu].port_write[iowrite_ptr].handler, Machine.drv.cpu[cpu].port_write[iowrite_ptr]._handler, 0) == null) {
                    memory_shutdown();
                    return 0;
                }

                if (Machine.drv.cpu[cpu].port_write[iowrite_ptr].start == -1) {
                    break;
                }

                iowrite_ptr++;
            }

            portmask[cpu] = 0xffff;

            if ((Machine.drv.cpu[cpu].cpu_type & ~CPU_FLAGS_MASK) == CPU_Z80
                    && (Machine.drv.cpu[cpu].cpu_type & CPU_16BIT_PORT) == 0) {
                portmask[cpu] = 0xff;
            }

        }
        /* initialize grobal handler */
        for (i = 0; i < MH_HARDMAX; i++) {
            memoryreadoffset[i] = 0;
            memorywriteoffset[i] = 0;
        }
        /* bank memory */
        for (i = 1; i <= MAX_BANKS; i++) {
            memoryreadhandler[i] = bank_read_handler[i];
            memorywritehandler[i] = bank_write_handler[i];
        }
        /* non map memory */
        memoryreadhandler[HT_NON] = mrh_error;
        memorywritehandler[HT_NON] = mwh_error;
        /* NOP memory */
        memoryreadhandler[HT_NOP] = mrh_nop;
        memorywritehandler[HT_NOP] = mwh_nop;
        /* RAMROM memory */
        memorywritehandler[HT_RAMROM] = mwh_ramrom;
        /* ROM memory */
        memorywritehandler[HT_ROM] = mwh_rom;

        /* if any CPU is 21-bit or more, we change the error handlers to be more benign */
        for (cpu = 0; cpu < cpu_gettotalcpu(); cpu++) {
            if (ADDRESS_BITS(cpu) >= 21) {
                memoryreadhandler[HT_NON] = mrh_error_sparse;
                memorywritehandler[HT_NON] = mwh_error_sparse;

                if ((Machine.drv.cpu[cpu].cpu_type & ~CPU_FLAGS_MASK) == CPU_TMS34010) {
                    memoryreadhandler[HT_NON] = mrh_error_sparse_bit;
                    memorywritehandler[HT_NON] = mwh_error_sparse_bit;
                }

            }
        }

        for (cpu = 0; cpu < cpu_gettotalcpu(); cpu++) {
            /* cpu selection */
            abits1 = ABITS1(cpu);
            abits2 = ABITS2(cpu);
            abits3 = ABITS3(cpu);
            abitsmin = ABITSMIN(cpu);

            /* element shifter , mask set */
            mhshift[cpu][0] = (abits2 + abits3);
            mhshift[cpu][1] = abits3;			/* 2nd */
            mhshift[cpu][2] = 0;				/* 3rd (used by set_element)*/
            mhmask[cpu][0] = MHMASK(abits1);		/*1st(used by set_element)*/
            mhmask[cpu][1] = MHMASK(abits2);		/*2nd*/
            mhmask[cpu][2] = MHMASK(abits3);		/*3rd*/

            /* allocate current element */
            if ((cur_mr_element[cpu] = new UBytePtr(new char[1<<abits1])) == null)//if( (cur_mr_element[cpu] = (MHELE *)malloc(sizeof(MHELE)<<abits1)) == 0 )
            {
                memory_shutdown();
                return 0;
            }

            if ((cur_mw_element[cpu] = new UBytePtr(new char[1<<abits1])) == null)//if( (cur_mw_element[cpu] = (MHELE *)malloc(sizeof(MHELE)<<abits1)) == 0 )
            {
                memory_shutdown();
                return 0;
            }

            /* initialize curent element table */
            for (i = 0; i < (1 << abits1); i++) {
                //cur_mr_element[cpu][i] = new UByte();
                //cur_mw_element[cpu][i] = new UByte();
                cur_mr_element[cpu].write(i, HT_NON);	/* no map memory */
                cur_mw_element[cpu].write(i, HT_NON);	/* no map memory */
            }

            /* memory read handler build */
            if (Machine.drv.cpu[cpu].memory_read != null) {
                int mra_ptr = 0;
                while (Machine.drv.cpu[cpu].memory_read[mra_ptr].start != -1) {
                    mra_ptr++;
                }
                mra_ptr--;
                while (mra_ptr >= 0) {
                    ReadHandlerPtr _handler = Machine.drv.cpu[cpu].memory_read[mra_ptr]._handler;
                    int handler = Machine.drv.cpu[cpu].memory_read[mra_ptr].handler;
                    switch (handler) {
                        case MRA_RAM:
                        case MRA_ROM:
                            hardware.set((char) HT_RAM);      /* sprcial case ram read */
                            break;
                        case MRA_BANK1:
                        case MRA_BANK2:
                        case MRA_BANK3:
                        case MRA_BANK4:
                        case MRA_BANK5:
                        case MRA_BANK6:
                        case MRA_BANK7:
                        case MRA_BANK8:
                        case MRA_BANK9:
                        case MRA_BANK10:
                        case MRA_BANK11:
                        case MRA_BANK12:
                        case MRA_BANK13:
                        case MRA_BANK14:
                        case MRA_BANK15:
                        case MRA_BANK16: {
                            hardware.set((char)(MRA_BANK1 - (int)handler + 1));
                            memoryreadoffset[hardware.read()] = bankreadoffset[hardware.read()] = Machine.drv.cpu[cpu].memory_read[mra_ptr].start;
                            cpu_bankbase[hardware.read()] = memory_find_base(cpu, Machine.drv.cpu[cpu].memory_read[mra_ptr].start);
                            break;
                        }
                        case MRA_NOP:
                            hardware.set((char) HT_NOP);
                            break;
                        default:

                            /* create newer hardware handler */
                            if (rdhard_max == MH_HARDMAX) {
                                if (errorlog != null) {
                                    fprintf(errorlog, "read memory hardware pattern over !\n");
                                }
                                hardware.set((char) 0);
                            } else {
                                /* regist hardware function */
                                hardware.set((char) rdhard_max++);
                                memoryreadhandler[hardware.read()] = _handler;
                                memoryreadoffset[hardware.read()] = Machine.drv.cpu[cpu].memory_read[mra_ptr].start;
                            }
                            break;
                    }

                    /* hardware element table make */
                    int temp_rdelement_max[] = new int[1]; //i can't pass a reference so here you go (shadow)
                    temp_rdelement_max[0] = rdelement_max;
                    //UBytePtr tem1 = new UBytePtr(cur_mr_element[cpu]);
                    //UBytePtr tem2 = new UBytePtr(readhardware);
                    set_element(cpu, cur_mr_element[cpu],
                            (int) ((Machine.drv.cpu[cpu].memory_read[mra_ptr].start) >>> abitsmin), /*TODO checked unsigned if it's correct */
                            (int) ((Machine.drv.cpu[cpu].memory_read[mra_ptr].end) >>> abitsmin), /*TODO checked unsigned if it's correct */
                            hardware, readhardware, temp_rdelement_max);
                    //cur_mr_element[cpu] = tem1.getUBytes();
                    //readhardware = tem2.getUBytes();
                    rdelement_max = temp_rdelement_max[0];
                    mra_ptr--;
                }
            }
            /* memory write handler build */
            if (Machine.drv.cpu[cpu].memory_write != null) {
                int mwa_ptr = 0;
                while (Machine.drv.cpu[cpu].memory_write[mwa_ptr].start != -1) {
                    mwa_ptr++;
                }
                mwa_ptr--;

                while (mwa_ptr >= 0) {
                    WriteHandlerPtr _handler = Machine.drv.cpu[cpu].memory_write[mwa_ptr]._handler;
                    int handler = Machine.drv.cpu[cpu].memory_write[mwa_ptr].handler;
                    switch (handler) {
                        case MWA_RAM:
                            hardware.set((char) HT_RAM);	/* sprcial case ram write */
                            break;
                        case MWA_BANK1:
                        case MWA_BANK2:
                        case MWA_BANK3:
                        case MWA_BANK4:
                        case MWA_BANK5:
                        case MWA_BANK6:
                        case MWA_BANK7:
                        case MWA_BANK8:
                        case MWA_BANK9:
                        case MWA_BANK10:
                        case MWA_BANK11:
                        case MWA_BANK12:
                        case MWA_BANK13:
                        case MWA_BANK14:
                        case MWA_BANK15:
                        case MWA_BANK16: 
                        {
                           hardware.set((char)((int)MWA_BANK1 - (int)handler + 1));
                          memorywriteoffset[hardware.read()] = bankwriteoffset[hardware.read()] = Machine.drv.cpu[cpu].memory_write[mwa_ptr].start;
			  cpu_bankbase[hardware.read()] = memory_find_base(cpu, Machine.drv.cpu[cpu].memory_write[mwa_ptr].start);
                          break;
                           
                        }
                        case MWA_NOP:
                            hardware.set((char) HT_NOP);
                            break;
                        case MWA_RAMROM:
                            hardware.set((char) HT_RAMROM);
                            break;
                        case MWA_ROM:
                            hardware.set((char) HT_ROM);
                            break;
                        default:
                            /* create newer hardware handler */
                            if (wrhard_max == MH_HARDMAX) {
                                if (errorlog != null) {
                                    fprintf(errorlog, "write memory hardware pattern over !\n");
                                }
                                hardware.set((char) 0);
                            } else {
                                /* regist hardware function */
                                hardware.set((char) wrhard_max++);
                                memorywritehandler[hardware.read()] = _handler;
                                memorywriteoffset[hardware.read()] = Machine.drv.cpu[cpu].memory_write[mwa_ptr].start;
                            }
                            break;
                    }
                    /* hardware element table make */
                    int temp_wrelement_max[] = new int[1]; //i can't pass a reference so here you go (shadow)
                    temp_wrelement_max[0] = wrelement_max;
                    set_element(cpu, cur_mw_element[cpu],
                            (int) ((Machine.drv.cpu[cpu].memory_write[mwa_ptr].start) >>> abitsmin), /*TODO checked unsigned if it's correct */
                            (int) ((Machine.drv.cpu[cpu].memory_write[mwa_ptr].end) >>> abitsmin), /*TODO checked unsigned if it's correct */
                            hardware, writehardware, temp_wrelement_max);

                    wrelement_max = temp_wrelement_max[0];
                    mwa_ptr--;
                }
            }
        }

        if (errorlog != null) {
            fprintf(errorlog, "used read  elements %d/%d , functions %d/%d\n", rdelement_max, MH_ELEMAX, rdhard_max, MH_HARDMAX);
            fprintf(errorlog, "used write elements %d/%d , functions %d/%d\n", wrelement_max, MH_ELEMAX, wrhard_max, MH_HARDMAX);
        }
        mem_dump();
        return 1;	/* ok */
    }

    public static void memory_set_opcode_base(int cpu,UBytePtr base)
    {
	romptr[cpu] = base;
    }
    public static void memorycontextswap(int activecpu)
    {
            cpu_bankbase[0] = ramptr[activecpu];

            cur_mrhard = cur_mr_element[activecpu].memory;
            cur_mwhard = cur_mw_element[activecpu].memory;

            /* ASG: port speedup */
            cur_readport = readport[activecpu];
            cur_writeport = writeport[activecpu];
            cur_portmask = portmask[activecpu];

            OPbasefunc = setOPbasefunc[activecpu];

            /* op code memory pointer */
            ophw.set((char)HT_RAM);
            OP_RAM = cpu_bankbase[0];
            OP_ROM = romptr[activecpu];
    }

    public static void memory_shutdown() {
        //normally we shouldn't even reach here yet (shadow)
        throw new UnsupportedOperationException("memory shutdown?? I didn't call you!");
        /*TODO*/ //	struct ExtMemory *ext;
/*TODO*/ //	int cpu;
/*TODO*/ //
/*TODO*/ //	for( cpu = 0 ; cpu < MAX_CPU ; cpu++ )
/*TODO*/ //	{
/*TODO*/ //		if( cur_mr_element[cpu] != 0 )
/*TODO*/ //		{
/*TODO*/ //			free( cur_mr_element[cpu] );
/*TODO*/ //			cur_mr_element[cpu] = 0;
/*TODO*/ //		}
/*TODO*/ //		if( cur_mw_element[cpu] != 0 )
/*TODO*/ //		{
/*TODO*/ //			free( cur_mw_element[cpu] );
/*TODO*/ //			cur_mw_element[cpu] = 0;
/*TODO*/ //		}
/*TODO*/ //
/*TODO*/ //		if (readport[cpu] != 0)
/*TODO*/ //		{
/*TODO*/ //			free(readport[cpu]);
/*TODO*/ //			readport[cpu] = 0;
/*TODO*/ //		}
/*TODO*/ //
/*TODO*/ //		if (writeport[cpu] != 0)
/*TODO*/ //		{
/*TODO*/ //			free(writeport[cpu]);
/*TODO*/ //			writeport[cpu] = 0;
/*TODO*/ //		}
/*TODO*/ //	}
/*TODO*/ //
/*TODO*/ //	/* ASG 980121 -- free all the external memory */
/*TODO*/ //	for (ext = ext_memory; ext->data; ext++)
/*TODO*/ //		free (ext->data);
/*TODO*/ //	memset (ext_memory, 0, sizeof (ext_memory));
    }
    /*TODO*/ //
/*TODO*/ //
/*TODO*/ //
/*TODO*/ ///***************************************************************************
/*TODO*/ //
/*TODO*/ //  Perform a memory read. This function is called by the CPU emulation.
/*TODO*/ //
/*TODO*/ //***************************************************************************/
/*TODO*/ //
/*TODO*/ ///* use these constants to define which type of memory handler to build */
/*TODO*/ //#define TYPE_8BIT					0		/* 8-bit aligned */
/*TODO*/ //#define TYPE_16BIT_BE				1		/* 16-bit aligned, big-endian */
/*TODO*/ //#define TYPE_16BIT_LE				2		/* 16-bit aligned, little-endian */
/*TODO*/ //
/*TODO*/ //#define CAN_BE_MISALIGNED			0		/* word/dwords can be read on non-16-bit boundaries */
/*TODO*/ //#define ALWAYS_ALIGNED				1		/* word/dwords are always read on 16-bit boundaries */
/*TODO*/ //
/*TODO*/ ///* stupid workarounds so that we can generate an address mask that works even for 32 bits */
/*TODO*/ //#define ADDRESS_TOPBIT(abits)		(1UL << (ABITS1_##abits + ABITS2_##abits + ABITS_MIN_##abits - 1))
/*TODO*/ //#define ADDRESS_MASK(abits)			(ADDRESS_TOPBIT(abits) | (ADDRESS_TOPBIT(abits) - 1))
/*TODO*/ //
/*TODO*/ //
    public static int cpu_readmem16(int address)
    {
        UByte hw=new UByte();
        
        /* first-level lookup */
        hw.set(cur_mrhard[address >>> (ABITS2_16 + ABITS_MIN_16)]);
																				
	/* for compatibility with setbankhandler, 8-bit systems must call handlers */		
	/* for banked memory reads/writes */												
	if (hw.read() == HT_RAM)												
		return cpu_bankbase[HT_RAM].memory[cpu_bankbase[HT_RAM].base + address];											
																																							
        /* second-level lookup */
        if (hw.read() >= MH_HARDMAX)
        {
                hw.set((char)(hw.read() - MH_HARDMAX));
                hw.set(readhardware.read((hw.read() << MH_SBITS) + ((address >>> ABITS_MIN_16) & MHMASK(ABITS2_16))));

                /* for compatibility with setbankhandler, 8-bit systems must call handlers */
                /* for banked memory reads/writes */
                if (hw.read() == HT_RAM)
                    return cpu_bankbase[HT_RAM].read(address);
        }
        /* fall back to handler */

        return memoryreadhandler[hw.read()].handler(address - memoryreadoffset[hw.read()]);      																					
    }
/*TODO*/ ///* generic byte-sized read handler */
/*TODO*/ //#define READBYTE(name,type,abits)														\
/*TODO*/ //int name(int address)																	\
/*TODO*/ //{																						\
/*TODO*/ //	MHELE hw;																			\
/*TODO*/ //																						\
/*TODO*/ //	/* first-level lookup */															\
/*TODO*/ //	hw = cur_mrhard[(UINT32)address >> (ABITS2_##abits + ABITS_MIN_##abits)];			\
/*TODO*/ //																						\
/*TODO*/ //	/* for compatibility with setbankhandler, 8-bit systems must call handlers */		\
/*TODO*/ //	/* for banked memory reads/writes */												\
/*TODO*/ //	if (type == TYPE_8BIT && hw == HT_RAM)												\
/*TODO*/ //		return cpu_bankbase[HT_RAM][address];											\
/*TODO*/ //	else if (type != TYPE_8BIT && hw <= HT_BANKMAX)										\
/*TODO*/ //	{																					\
/*TODO*/ //		if (type == TYPE_16BIT_BE)														\
/*TODO*/ //			return cpu_bankbase[hw][BYTE_XOR_BE(address) - memoryreadoffset[hw]];		\
/*TODO*/ //		else if (type == TYPE_16BIT_LE)													\
/*TODO*/ //			return cpu_bankbase[hw][BYTE_XOR_LE(address) - memoryreadoffset[hw]];		\
/*TODO*/ //	}																					\
/*TODO*/ //																						\
/*TODO*/ //	/* second-level lookup */															\
/*TODO*/ //	if (hw >= MH_HARDMAX)																\
/*TODO*/ //	{																					\
/*TODO*/ //		hw -= MH_HARDMAX;																\
/*TODO*/ //		hw = readhardware[(hw << MH_SBITS) + (((UINT32)address >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
/*TODO*/ //																						\
/*TODO*/ //		/* for compatibility with setbankhandler, 8-bit systems must call handlers */	\
/*TODO*/ //		/* for banked memory reads/writes */											\
/*TODO*/ //		if (type == TYPE_8BIT && hw == HT_RAM)											\
/*TODO*/ //			return cpu_bankbase[HT_RAM][address];										\
/*TODO*/ //		else if (type != TYPE_8BIT && hw <= HT_BANKMAX)									\
/*TODO*/ //		{																				\
/*TODO*/ //			if (type == TYPE_16BIT_BE)													\
/*TODO*/ //				return cpu_bankbase[hw][BYTE_XOR_BE(address) - memoryreadoffset[hw]];	\
/*TODO*/ //			else if (type == TYPE_16BIT_LE)												\
/*TODO*/ //				return cpu_bankbase[hw][BYTE_XOR_LE(address) - memoryreadoffset[hw]];	\
/*TODO*/ //		}																				\
/*TODO*/ //	}																					\
/*TODO*/ //																						\
/*TODO*/ //	/* fall back to handler */															\
/*TODO*/ //	if (type == TYPE_8BIT)																\
/*TODO*/ //		return (*memoryreadhandler[hw])(address - memoryreadoffset[hw]);				\
/*TODO*/ //	else																				\
/*TODO*/ //	{																					\
/*TODO*/ //		int shift = (address & 1) << 3;													\
/*TODO*/ //		int data = (*memoryreadhandler[hw])((address & ~1) - memoryreadoffset[hw]);		\
/*TODO*/ //		if (type == TYPE_16BIT_BE)														\
/*TODO*/ //			return (data >> (shift ^ 8)) & 0xff;										\
/*TODO*/ //		else if (type == TYPE_16BIT_LE)													\
/*TODO*/ //			return (data >> shift) & 0xff;												\
/*TODO*/ //	}																					\
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ ///* generic word-sized read handler (16-bit aligned only!) */
/*TODO*/ //#define READWORD(name,type,abits,align)													\
/*TODO*/ //int name##_word(int address)															\
/*TODO*/ //{																						\
/*TODO*/ //	MHELE hw;																			\
/*TODO*/ //																						\
/*TODO*/ //	/* only supports 16-bit memory systems */											\
/*TODO*/ //	if (type == TYPE_8BIT)																\
/*TODO*/ //		printf("Unsupported type for READWORD macro!\n");								\
/*TODO*/ //																						\
/*TODO*/ //	/* handle aligned case first */														\
/*TODO*/ //	if (align == ALWAYS_ALIGNED || !(address & 1))										\
/*TODO*/ //	{																					\
/*TODO*/ //		/* first-level lookup */														\
/*TODO*/ //		hw = cur_mrhard[(UINT32)address >> (ABITS2_##abits + ABITS_MIN_##abits)];		\
/*TODO*/ //		if (hw <= HT_BANKMAX)															\
/*TODO*/ //			return READ_WORD(&cpu_bankbase[hw][address - memoryreadoffset[hw]]);		\
/*TODO*/ //																						\
/*TODO*/ //		/* second-level lookup */														\
/*TODO*/ //		if (hw >= MH_HARDMAX)															\
/*TODO*/ //		{																				\
/*TODO*/ //			hw -= MH_HARDMAX;															\
/*TODO*/ //			hw = readhardware[(hw << MH_SBITS) + (((UINT32)address >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
/*TODO*/ //			if (hw <= HT_BANKMAX)														\
/*TODO*/ //				return READ_WORD(&cpu_bankbase[hw][address - memoryreadoffset[hw]]);	\
/*TODO*/ //		}																				\
/*TODO*/ //																						\
/*TODO*/ //		/* fall back to handler */														\
/*TODO*/ //		return (*memoryreadhandler[hw])(address - memoryreadoffset[hw]);				\
/*TODO*/ //	}																					\
/*TODO*/ //																						\
/*TODO*/ //	/* unaligned case */																\
/*TODO*/ //	else if (type == TYPE_16BIT_BE)														\
/*TODO*/ //	{																					\
/*TODO*/ //		int data = name(address) << 8;													\
/*TODO*/ //		return data | (name(address + 1) & 0xff);										\
/*TODO*/ //	}																					\
/*TODO*/ //	else if (type == TYPE_16BIT_LE)														\
/*TODO*/ //	{																					\
/*TODO*/ //		int data = name(address) & 0xff;												\
/*TODO*/ //		return data | (name(address + 1) << 8);											\
/*TODO*/ //	}																					\
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ ///* generic dword-sized read handler (16-bit aligned only!) */
/*TODO*/ //#define READLONG(name,type,abits,align)													\
/*TODO*/ //int name##_dword(int address)															\
/*TODO*/ //{																						\
/*TODO*/ //	UINT16 word1, word2;																\
/*TODO*/ //	MHELE hw1, hw2;																		\
/*TODO*/ //																						\
/*TODO*/ //	/* only supports 16-bit memory systems */											\
/*TODO*/ //	if (type == TYPE_8BIT)																\
/*TODO*/ //		printf("Unsupported type for READWORD macro!\n");								\
/*TODO*/ //																						\
/*TODO*/ //	/* handle aligned case first */														\
/*TODO*/ //	if (align == ALWAYS_ALIGNED || !(address & 1))										\
/*TODO*/ //	{																					\
/*TODO*/ //		int address2 = (address + 2) & ADDRESS_MASK(abits);								\
/*TODO*/ //																						\
/*TODO*/ //		/* first-level lookup */														\
/*TODO*/ //		hw1 = cur_mrhard[(UINT32)address >> (ABITS2_##abits + ABITS_MIN_##abits)];		\
/*TODO*/ //		hw2 = cur_mrhard[(UINT32)address2 >> (ABITS2_##abits + ABITS_MIN_##abits)];		\
/*TODO*/ //																						\
/*TODO*/ //		/* second-level lookup */														\
/*TODO*/ //		if (hw1 >= MH_HARDMAX)															\
/*TODO*/ //		{																				\
/*TODO*/ //			hw1 -= MH_HARDMAX;															\
/*TODO*/ //			hw1 = readhardware[(hw1 << MH_SBITS) + (((UINT32)address >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
/*TODO*/ //		}																				\
/*TODO*/ //		if (hw2 >= MH_HARDMAX)															\
/*TODO*/ //		{																				\
/*TODO*/ //			hw2 -= MH_HARDMAX;															\
/*TODO*/ //			hw2 = readhardware[(hw2 << MH_SBITS) + (((UINT32)address2 >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
/*TODO*/ //		}																				\
/*TODO*/ //																						\
/*TODO*/ //		/* process each word */															\
/*TODO*/ //		if (hw1 <= HT_BANKMAX)															\
/*TODO*/ //			word1 = READ_WORD(&cpu_bankbase[hw1][address - memoryreadoffset[hw1]]);		\
/*TODO*/ //		else																			\
/*TODO*/ //			word1 = (*memoryreadhandler[hw1])(address - memoryreadoffset[hw1]);			\
/*TODO*/ //		if (hw2 <= HT_BANKMAX)															\
/*TODO*/ //			word2 = READ_WORD(&cpu_bankbase[hw2][address2 - memoryreadoffset[hw2]]);	\
/*TODO*/ //		else																			\
/*TODO*/ //			word2 = (*memoryreadhandler[hw2])(address2 - memoryreadoffset[hw2]);		\
/*TODO*/ //																						\
/*TODO*/ //		/* fall back to handler */														\
/*TODO*/ //		if (type == TYPE_16BIT_BE)														\
/*TODO*/ //			return (word1 << 16) | (word2 & 0xffff);									\
/*TODO*/ //		else if (type == TYPE_16BIT_LE)													\
/*TODO*/ //			return (word1 & 0xffff) | (word2 << 16);									\
/*TODO*/ //	}																					\
/*TODO*/ //																						\
/*TODO*/ //	/* unaligned case */																\
/*TODO*/ //	else if (type == TYPE_16BIT_BE)														\
/*TODO*/ //	{																					\
/*TODO*/ //		int data = name(address) << 24;													\
/*TODO*/ //		data |= name##_word(address + 1) << 8;											\
/*TODO*/ //		return data | (name(address + 3) & 0xff);										\
/*TODO*/ //	}																					\
/*TODO*/ //	else if (type == TYPE_16BIT_LE)														\
/*TODO*/ //	{																					\
/*TODO*/ //		int data = name(address) & 0xff;												\
/*TODO*/ //		data |= name##_word(address + 1) << 8;											\
/*TODO*/ //		return data | (name(address + 3) << 24);										\
/*TODO*/ //	}																					\
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ //
/*TODO*/ ///* the handlers we need to generate */
/*TODO*/ //READBYTE(cpu_readmem16,    TYPE_8BIT,     16)
/*TODO*/ //READBYTE(cpu_readmem20,    TYPE_8BIT,     20)
/*TODO*/ //READBYTE(cpu_readmem21,    TYPE_8BIT,     21)
/*TODO*/ //
/*TODO*/ //READBYTE(cpu_readmem16bew, TYPE_16BIT_BE, 16BEW)
/*TODO*/ //READWORD(cpu_readmem16bew, TYPE_16BIT_BE, 16BEW, ALWAYS_ALIGNED)
/*TODO*/ //
/*TODO*/ //READBYTE(cpu_readmem16lew, TYPE_16BIT_LE, 16LEW)
/*TODO*/ //READWORD(cpu_readmem16lew, TYPE_16BIT_LE, 16LEW, ALWAYS_ALIGNED)
/*TODO*/ //
/*TODO*/ //READBYTE(cpu_readmem24,    TYPE_16BIT_BE, 24)
/*TODO*/ //READWORD(cpu_readmem24,    TYPE_16BIT_BE, 24,    CAN_BE_MISALIGNED)
/*TODO*/ //READLONG(cpu_readmem24,    TYPE_16BIT_BE, 24,    CAN_BE_MISALIGNED)
/*TODO*/ //
/*TODO*/ //READBYTE(cpu_readmem29,    TYPE_16BIT_LE, 29)
/*TODO*/ //READWORD(cpu_readmem29,    TYPE_16BIT_LE, 29,    CAN_BE_MISALIGNED)
/*TODO*/ //READLONG(cpu_readmem29,    TYPE_16BIT_LE, 29,    CAN_BE_MISALIGNED)
/*TODO*/ //
/*TODO*/ //READBYTE(cpu_readmem32,    TYPE_16BIT_BE, 32)
/*TODO*/ //READWORD(cpu_readmem32,    TYPE_16BIT_BE, 32,    CAN_BE_MISALIGNED)
/*TODO*/ //READLONG(cpu_readmem32,    TYPE_16BIT_BE, 32,    CAN_BE_MISALIGNED)
/*TODO*/ //
/*TODO*/ //
/*TODO*/ ///***************************************************************************
/*TODO*/ //
/*TODO*/ //  Perform a memory write. This function is called by the CPU emulation.
/*TODO*/ //
/*TODO*/ //***************************************************************************/
/*TODO*/ //
/*TODO*/ ///* generic byte-sized write handler */
/*TODO*/ //#define WRITEBYTE(name,type,abits)		
 /*TODO*/ 
   //TODO CHECK IF IT IS Valid (added it only for testing) (shadow)
   public static void cpu_writemem16(int address, int data)
   {
            /* first-level lookup */
            UByte hw=new UByte();
            hw.set(cur_mwhard[address >>> (ABITS2_16 + ABITS_MIN_16)]);

            /* for compatibility with setbankhandler, 8-bit systems must call handlers */
            /* for banked memory reads/writes */
            if (hw.read() == HT_RAM)
            {
                cpu_bankbase[HT_RAM].memory[cpu_bankbase[HT_RAM].base+address] = (char)data;
                return;
            }

            /* second-level lookup */
            if (hw.read() >= MH_HARDMAX)
            {
                hw.set((char)(hw.read() - MH_HARDMAX));
                hw.set(writehardware.read((hw.read() << MH_SBITS) + ((address >>> ABITS_MIN_16) & MHMASK(ABITS2_16))));    
                /* for compatibility with setbankhandler, 8-bit systems must call handlers */
                /* for banked memory reads/writes */
                if (hw.read() == HT_RAM)
                {
                    cpu_bankbase[HT_RAM].write(address, data);
                    return;
                }
            }

            memorywritehandler[hw.read()].handler(address - memorywriteoffset[hw.read()], data);
   }

/*TODO*/ //void name(int address, int data)														\
/*TODO*/ //{																						\
/*TODO*/ //	MHELE hw;																			\
/*TODO*/ //																						\
/*TODO*/ //	/* first-level lookup */															\
/*TODO*/ //	hw = cur_mwhard[(UINT32)address >> (ABITS2_##abits + ABITS_MIN_##abits)];			\
/*TODO*/ //																						\
/*TODO*/ //	/* for compatibility with setbankhandler, 8-bit systems must call handlers */		\
/*TODO*/ //	/* for banked memory reads/writes */												\
/*TODO*/ //	if (type == TYPE_8BIT && hw == HT_RAM)												\
/*TODO*/ //	{																					\
/*TODO*/ //		cpu_bankbase[HT_RAM][address] = data;											\
/*TODO*/ //		return;																			\
/*TODO*/ //	}																					\
/*TODO*/ //	else if (type != TYPE_8BIT && hw <= HT_BANKMAX)										\
/*TODO*/ //	{																					\
/*TODO*/ //		if (type == TYPE_16BIT_BE)														\
/*TODO*/ //			cpu_bankbase[hw][BYTE_XOR_BE(address) - memorywriteoffset[hw]] = data;		\
/*TODO*/ //		else if (type == TYPE_16BIT_LE)													\
/*TODO*/ //			cpu_bankbase[hw][BYTE_XOR_LE(address) - memorywriteoffset[hw]] = data;		\
/*TODO*/ //		return;																			\
/*TODO*/ //	}																					\
/*TODO*/ //																						\
/*TODO*/ //	/* second-level lookup */															\
/*TODO*/ //	if (hw >= MH_HARDMAX)																\
/*TODO*/ //	{																					\
/*TODO*/ //		hw -= MH_HARDMAX;																\
/*TODO*/ //		hw = writehardware[(hw << MH_SBITS) + (((UINT32)address >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
/*TODO*/ //																						\
/*TODO*/ //		/* for compatibility with setbankhandler, 8-bit systems must call handlers */	\
/*TODO*/ //		/* for banked memory reads/writes */											\
/*TODO*/ //		if (type == TYPE_8BIT && hw == HT_RAM)											\
/*TODO*/ //		{																				\
/*TODO*/ //			cpu_bankbase[HT_RAM][address] = data;										\
/*TODO*/ //			return;																		\
/*TODO*/ //		}																				\
/*TODO*/ //		else if (type != TYPE_8BIT && hw <= HT_BANKMAX)									\
/*TODO*/ //		{																				\
/*TODO*/ //			if (type == TYPE_16BIT_BE)													\
/*TODO*/ //				cpu_bankbase[hw][BYTE_XOR_BE(address) - memorywriteoffset[hw]] = data;	\
/*TODO*/ //			else if (type == TYPE_16BIT_LE)												\
/*TODO*/ //				cpu_bankbase[hw][BYTE_XOR_LE(address) - memorywriteoffset[hw]] = data;	\
/*TODO*/ //			return;																		\
/*TODO*/ //		}																				\
/*TODO*/ //	}																					\
/*TODO*/ //																						\
/*TODO*/ //	/* fall back to handler */															\
/*TODO*/ //	if (type != TYPE_8BIT)																\
/*TODO*/ //	{																					\
/*TODO*/ //		int shift = (address & 1) << 3;													\
/*TODO*/ //		if (type == TYPE_16BIT_BE)														\
/*TODO*/ //			shift ^= 8;																	\
/*TODO*/ //		data = (0xff000000 >> shift) | ((data & 0xff) << shift);						\
/*TODO*/ //		address &= ~1;																	\
/*TODO*/ //	}																					\
/*TODO*/ //	(*memorywritehandler[hw])(address - memorywriteoffset[hw], data);					\
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ ///* generic word-sized write handler (16-bit aligned only!) */
/*TODO*/ //#define WRITEWORD(name,type,abits,align)												\
/*TODO*/ //void name##_word(int address, int data)													\
/*TODO*/ //{																						\
/*TODO*/ //	MHELE hw;																			\
/*TODO*/ //																						\
/*TODO*/ //	/* only supports 16-bit memory systems */											\
/*TODO*/ //	if (type == TYPE_8BIT)																\
/*TODO*/ //		printf("Unsupported type for WRITEWORD macro!\n");								\
/*TODO*/ //																						\
/*TODO*/ //	/* handle aligned case first */														\
/*TODO*/ //	if (align == ALWAYS_ALIGNED || !(address & 1))										\
/*TODO*/ //	{																					\
/*TODO*/ //		/* first-level lookup */														\
/*TODO*/ //		hw = cur_mwhard[(UINT32)address >> (ABITS2_##abits + ABITS_MIN_##abits)];		\
/*TODO*/ //		if (hw <= HT_BANKMAX)															\
/*TODO*/ //		{																				\
/*TODO*/ //			WRITE_WORD(&cpu_bankbase[hw][address - memorywriteoffset[hw]], data);		\
/*TODO*/ //			return;																		\
/*TODO*/ //		}																				\
/*TODO*/ //																						\
/*TODO*/ //		/* second-level lookup */														\
/*TODO*/ //		if (hw >= MH_HARDMAX)															\
/*TODO*/ //		{																				\
/*TODO*/ //			hw -= MH_HARDMAX;															\
/*TODO*/ //			hw = writehardware[(hw << MH_SBITS) + (((UINT32)address >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))]; \
/*TODO*/ //			if (hw <= HT_BANKMAX)														\
/*TODO*/ //			{																			\
/*TODO*/ //				WRITE_WORD(&cpu_bankbase[hw][address - memorywriteoffset[hw]], data);	\
/*TODO*/ //				return;																	\
/*TODO*/ //			}																			\
/*TODO*/ //		}																				\
/*TODO*/ //																						\
/*TODO*/ //		/* fall back to handler */														\
/*TODO*/ //		(*memorywritehandler[hw])(address - memorywriteoffset[hw], data & 0xffff);		\
/*TODO*/ //	}																					\
/*TODO*/ //																						\
/*TODO*/ //	/* unaligned case */																\
/*TODO*/ //	else if (type == TYPE_16BIT_BE)														\
/*TODO*/ //	{																					\
/*TODO*/ //		name(address, data >> 8);														\
/*TODO*/ //		name(address + 1, data & 0xff);													\
/*TODO*/ //	}																					\
/*TODO*/ //	else if (type == TYPE_16BIT_LE)														\
/*TODO*/ //	{																					\
/*TODO*/ //		name(address, data & 0xff);														\
/*TODO*/ //		name(address + 1, data >> 8);													\
/*TODO*/ //	}																					\
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ ///* generic dword-sized write handler (16-bit aligned only!) */
/*TODO*/ //#define WRITELONG(name,type,abits,align)												\
/*TODO*/ //void name##_dword(int address, int data)												\
/*TODO*/ //{																						\
/*TODO*/ //	UINT16 word1, word2;																\
/*TODO*/ //	MHELE hw1, hw2;																		\
/*TODO*/ //																						\
/*TODO*/ //	/* only supports 16-bit memory systems */											\
/*TODO*/ //	if (type == TYPE_8BIT)																\
/*TODO*/ //		printf("Unsupported type for WRITEWORD macro!\n");								\
/*TODO*/ //																						\
/*TODO*/ //	/* handle aligned case first */														\
/*TODO*/ //	if (align == ALWAYS_ALIGNED || !(address & 1))										\
/*TODO*/ //	{																					\
/*TODO*/ //		int address2 = (address + 2) & ADDRESS_MASK(abits);								\
/*TODO*/ //																						\
/*TODO*/ //		/* first-level lookup */														\
/*TODO*/ //		hw1 = cur_mwhard[(UINT32)address >> (ABITS2_##abits + ABITS_MIN_##abits)];		\
/*TODO*/ //		hw2 = cur_mwhard[(UINT32)address2 >> (ABITS2_##abits + ABITS_MIN_##abits)];		\
/*TODO*/ //																						\
/*TODO*/ //		/* second-level lookup */														\
/*TODO*/ //		if (hw1 >= MH_HARDMAX)															\
/*TODO*/ //		{																				\
/*TODO*/ //			hw1 -= MH_HARDMAX;															\
/*TODO*/ //			hw1 = writehardware[(hw1 << MH_SBITS) + (((UINT32)address >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
/*TODO*/ //		}																				\
/*TODO*/ //		if (hw2 >= MH_HARDMAX)															\
/*TODO*/ //		{																				\
/*TODO*/ //			hw2 -= MH_HARDMAX;															\
/*TODO*/ //			hw2 = writehardware[(hw2 << MH_SBITS) + (((UINT32)address2 >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
/*TODO*/ //		}																				\
/*TODO*/ //																						\
/*TODO*/ //		/* extract words */																\
/*TODO*/ //		if (type == TYPE_16BIT_BE)														\
/*TODO*/ //		{																				\
/*TODO*/ //			word1 = data >> 16;															\
/*TODO*/ //			word2 = data & 0xffff;														\
/*TODO*/ //		}																				\
/*TODO*/ //		else if (type == TYPE_16BIT_LE)													\
/*TODO*/ //		{																				\
/*TODO*/ //			word1 = data & 0xffff;														\
/*TODO*/ //			word2 = data >> 16;															\
/*TODO*/ //		}																				\
/*TODO*/ //																						\
/*TODO*/ //		/* process each word */															\
/*TODO*/ //		if (hw1 <= HT_BANKMAX)															\
/*TODO*/ //			WRITE_WORD(&cpu_bankbase[hw1][address - memorywriteoffset[hw1]], word1);	\
/*TODO*/ //		else																			\
/*TODO*/ //			(*memorywritehandler[hw1])(address - memorywriteoffset[hw1], word1);		\
/*TODO*/ //		if (hw2 <= HT_BANKMAX)															\
/*TODO*/ //			WRITE_WORD(&cpu_bankbase[hw2][address2 - memorywriteoffset[hw2]], word2);	\
/*TODO*/ //		else																			\
/*TODO*/ //			(*memorywritehandler[hw2])(address2 - memorywriteoffset[hw2], word2);		\
/*TODO*/ //	}																					\
/*TODO*/ //																						\
/*TODO*/ //	/* unaligned case */																\
/*TODO*/ //	else if (type == TYPE_16BIT_BE)														\
/*TODO*/ //	{																					\
/*TODO*/ //		name(address, data >> 24);														\
/*TODO*/ //		name##_word(address + 1, (data >> 8) & 0xffff);									\
/*TODO*/ //		name(address + 3, data & 0xff);													\
/*TODO*/ //	}																					\
/*TODO*/ //	else if (type == TYPE_16BIT_LE)														\
/*TODO*/ //	{																					\
/*TODO*/ //		name(address, data & 0xff);														\
/*TODO*/ //		name##_word(address + 1, (data >> 8) & 0xffff);									\
/*TODO*/ //		name(address + 3, data >> 24);													\
/*TODO*/ //	}																					\
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ //
/*TODO*/ ///* the handlers we need to generate */
/*TODO*/ //WRITEBYTE(cpu_writemem16,    TYPE_8BIT,     16)
/*TODO*/ //WRITEBYTE(cpu_writemem20,    TYPE_8BIT,     20)
/*TODO*/ //WRITEBYTE(cpu_writemem21,    TYPE_8BIT,     21)
/*TODO*/ //
/*TODO*/ //WRITEBYTE(cpu_writemem16bew, TYPE_16BIT_BE, 16BEW)
/*TODO*/ //WRITEWORD(cpu_writemem16bew, TYPE_16BIT_BE, 16BEW, ALWAYS_ALIGNED)
/*TODO*/ //
/*TODO*/ //WRITEBYTE(cpu_writemem16lew, TYPE_16BIT_LE, 16LEW)
/*TODO*/ //WRITEWORD(cpu_writemem16lew, TYPE_16BIT_LE, 16LEW, ALWAYS_ALIGNED)
/*TODO*/ //
/*TODO*/ //WRITEBYTE(cpu_writemem24,    TYPE_16BIT_BE, 24)
/*TODO*/ //WRITEWORD(cpu_writemem24,    TYPE_16BIT_BE, 24,    CAN_BE_MISALIGNED)
/*TODO*/ //WRITELONG(cpu_writemem24,    TYPE_16BIT_BE, 24,    CAN_BE_MISALIGNED)
/*TODO*/ //
/*TODO*/ //WRITEBYTE(cpu_writemem29,    TYPE_16BIT_LE, 29)
/*TODO*/ //WRITEWORD(cpu_writemem29,    TYPE_16BIT_LE, 29,    CAN_BE_MISALIGNED)
/*TODO*/ //WRITELONG(cpu_writemem29,    TYPE_16BIT_LE, 29,    CAN_BE_MISALIGNED)
/*TODO*/ //
/*TODO*/ //WRITEBYTE(cpu_writemem32,    TYPE_16BIT_BE, 32)
/*TODO*/ //WRITEWORD(cpu_writemem32,    TYPE_16BIT_BE, 32,    CAN_BE_MISALIGNED)
/*TODO*/ //WRITELONG(cpu_writemem32,    TYPE_16BIT_BE, 32,    CAN_BE_MISALIGNED)
/*TODO*/ //
/*TODO*/ //
/*TODO*/ ///***************************************************************************
/*TODO*/ //
/*TODO*/ //  Opcode base changers. This function is called by the CPU emulation.
/*TODO*/ //
/*TODO*/ //***************************************************************************/
/*TODO*/ //
/*TODO*/ ///* generic opcode base changer */
/*TODO*/ //#define SETOPBASE(name,abits,shift)														\
/*TODO*/ //void name(int pc)																		\
/*TODO*/ //{																						\
/*TODO*/ //	MHELE hw;																			\
/*TODO*/ //																						\
/*TODO*/ //	pc = (UINT32)pc >> shift;															\
/*TODO*/ //																						\
/*TODO*/ //	/* allow overrides */																\
/*TODO*/ //	if (OPbasefunc)																		\
/*TODO*/ //	{																					\
/*TODO*/ //		pc = OPbasefunc(pc);															\
/*TODO*/ //		if (pc == -1)																	\
/*TODO*/ //			return;																		\
/*TODO*/ //	}																					\
/*TODO*/ //																						\
/*TODO*/ //	/* perform the lookup */															\
/*TODO*/ //	hw = cur_mrhard[(UINT32)pc >> (ABITS2_##abits + ABITS_MIN_##abits)];				\
/*TODO*/ //	if (hw >= MH_HARDMAX)																\
/*TODO*/ //	{																					\
/*TODO*/ //		hw -= MH_HARDMAX;																\
/*TODO*/ //		hw = readhardware[(hw << MH_SBITS) + (((UINT32)pc >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
/*TODO*/ //	}																					\
/*TODO*/ //	ophw = hw;																			\
/*TODO*/ //																						\
/*TODO*/ //	/* RAM or banked memory */															\
/*TODO*/ //	if (hw <= HT_BANKMAX)																\
/*TODO*/ //	{																					\
/*TODO*/ //		SET_OP_RAMROM(cpu_bankbase[hw] - memoryreadoffset[hw])							\
/*TODO*/ //		return;																			\
/*TODO*/ //	}																					\
/*TODO*/ //																						\
/*TODO*/ //	/* do not support on callback memory region */										\
/*TODO*/ //	if (errorlog)																		\
/*TODO*/ //		fprintf(errorlog, "CPU #%d PC %04x: warning - op-code execute on mapped i/o\n",	\
/*TODO*/ //					cpu_getactivecpu(),cpu_get_pc());									\
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ //
/*TODO*/ ///* the handlers we need to generate */
/*TODO*/ //SETOPBASE(cpu_setOPbase16,    16,    0)
/*TODO*/ //SETOPBASE(cpu_setOPbase16bew, 16BEW, 0)
/*TODO*/ //SETOPBASE(cpu_setOPbase16lew, 16LEW, 0)
/*TODO*/ //SETOPBASE(cpu_setOPbase20,    20,    0)
/*TODO*/ //SETOPBASE(cpu_setOPbase21,    21,    0)
/*TODO*/ //SETOPBASE(cpu_setOPbase24,    24,    0)
/*TODO*/ //SETOPBASE(cpu_setOPbase29,    29,    3)
/*TODO*/ //SETOPBASE(cpu_setOPbase32,    32,    0)
/*TODO*/ //
   
    public static setopbase cpu_setOPbase16 =new setopbase(){ public void handler(int pc, int shift)
    {
      UByte hw=new UByte();

      pc = (int)(pc >>> shift);

            /* allow overrides */
      if (OPbasefunc != null)
      {
         pc = (int)OPbasefunc.handler((int)pc);
         if (pc == -1)
            return;
      }

      /* perform the lookup */
      hw.set(cur_mrhard[pc >>> (ABITS2_16 + ABITS_MIN_16)]);
      if (hw.read() >= MH_HARDMAX)
      {   															
         hw.set((char)(hw.read() - MH_HARDMAX));
         hw.set(readhardware.read((hw.read() << MH_SBITS) + ((pc >>> ABITS_MIN_16) & MHMASK(ABITS2_16))));
      }
            ophw.set(hw.read());

            /* RAM or banked memory */
            if (hw.read() <= HT_BANKMAX)
            {
               SET_OP_RAMROM(new UBytePtr(cpu_bankbase[hw.read()], (-memoryreadoffset[hw.read()])));
                return;
            }

            /* do not support on callback memory region */
            printf("CPU #%d PC %04x: warning - op-code execute on mapped i/o\n",        cpu_getactivecpu(),cpu_get_pc());                                                                      
    }};

    /***************************************************************************

      Perform an I/O port read. This function is called by the CPU emulation.

    ***************************************************************************/
    public static int cpu_readport(int port)
    {
            int iorp=0;//const struct IOReadPort *iorp = cur_readport;

            port &= cur_portmask;

            /* search the handlers. The order is as follows: first the dynamically installed
               handlers are searched, followed by the static ones in whatever order they were
               specified in the driver */
            while (cur_readport[iorp].start != -1)
            {
                    if (port >= cur_readport[iorp].start && port <= cur_readport[iorp].end)
                    {
                           int handler = cur_readport[iorp].handler;


                            if (handler == IORP_NOP) return 0;
                            else return cur_readport[iorp]._handler.handler(port - cur_readport[iorp].start);
                    }

                    iorp++;
            }

            if (errorlog!=null) fprintf(errorlog,"CPU #%d PC %04x: warning - read unmapped I/O port %02x\n",cpu_getactivecpu(),cpu_get_pc(),port);
            return 0;
    }


    /***************************************************************************

      Perform an I/O port write. This function is called by the CPU emulation.

    ***************************************************************************/
    public static void cpu_writeport(int port, int value)
    {
            int iowp=0;//const struct IOWritePort *iowp = cur_writeport;

            port &= cur_portmask;

            /* search the handlers. The order is as follows: first the dynamically installed
               handlers are searched, followed by the static ones in whatever order they were
               specified in the driver */
            while (cur_writeport[iowp].start != -1)
            {
                    if (port >= cur_writeport[iowp].start && port <= cur_writeport[iowp].end)
                    {
                            int handler = cur_writeport[iowp].handler;


                            if (handler == IOWP_NOP) return;
                            else cur_writeport[iowp]._handler.handler(port - cur_writeport[iowp].start, value);
                            return;
                    }

                    iowp++;
            }
            if (errorlog!=null) fprintf(errorlog,"CPU #%d PC %04x: warning - write %02x to unmapped I/O port %02x\n",cpu_getactivecpu(),cpu_get_pc(),value,port);
    }

/*TODO*/ //
/*TODO*/ ///* set readmemory handler for bank memory  */
/*TODO*/ //void cpu_setbankhandler_r(int bank, mem_read_handler handler)
/*TODO*/ //{
/*TODO*/ //	int offset = 0;
/*TODO*/ //	MHELE hardware;
/*TODO*/ //
/*TODO*/ //	switch( (FPTR)handler )
/*TODO*/ //	{
/*TODO*/ //	case (FPTR)MRA_RAM:
/*TODO*/ //	case (FPTR)MRA_ROM:
/*TODO*/ //		handler = mrh_ram;
/*TODO*/ //		break;
/*TODO*/ //	case (FPTR)MRA_BANK1:
/*TODO*/ //	case (FPTR)MRA_BANK2:
/*TODO*/ //	case (FPTR)MRA_BANK3:
/*TODO*/ //	case (FPTR)MRA_BANK4:
/*TODO*/ //	case (FPTR)MRA_BANK5:
/*TODO*/ //	case (FPTR)MRA_BANK6:
/*TODO*/ //	case (FPTR)MRA_BANK7:
/*TODO*/ //	case (FPTR)MRA_BANK8:
/*TODO*/ //	case (FPTR)MRA_BANK9:
/*TODO*/ //	case (FPTR)MRA_BANK10:
/*TODO*/ //	case (FPTR)MRA_BANK11:
/*TODO*/ //	case (FPTR)MRA_BANK12:
/*TODO*/ //	case (FPTR)MRA_BANK13:
/*TODO*/ //	case (FPTR)MRA_BANK14:
/*TODO*/ //	case (FPTR)MRA_BANK15:
/*TODO*/ //	case (FPTR)MRA_BANK16:
/*TODO*/ //		hardware = (int)MWA_BANK1 - (int)handler + 1;
/*TODO*/ //		handler = bank_read_handler[hardware];
/*TODO*/ //		offset = bankreadoffset[hardware];
/*TODO*/ //		break;
/*TODO*/ //	case (FPTR)MRA_NOP:
/*TODO*/ //		handler = mrh_nop;
/*TODO*/ //		break;
/*TODO*/ //	default:
/*TODO*/ //		offset = bankreadoffset[bank];
/*TODO*/ //		break;
/*TODO*/ //	}
/*TODO*/ //	memoryreadoffset[bank] = offset;
/*TODO*/ //	memoryreadhandler[bank] = handler;
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ ///* set writememory handler for bank memory  */
/*TODO*/ //void cpu_setbankhandler_w(int bank, mem_write_handler handler)
/*TODO*/ //{
/*TODO*/ //	int offset = 0;
/*TODO*/ //	MHELE hardware;
/*TODO*/ //
/*TODO*/ //	switch( (FPTR)handler )
/*TODO*/ //	{
/*TODO*/ //	case (FPTR)MWA_RAM:
/*TODO*/ //		handler = mwh_ram;
/*TODO*/ //		break;
/*TODO*/ //	case (FPTR)MWA_BANK1:
/*TODO*/ //	case (FPTR)MWA_BANK2:
/*TODO*/ //	case (FPTR)MWA_BANK3:
/*TODO*/ //	case (FPTR)MWA_BANK4:
/*TODO*/ //	case (FPTR)MWA_BANK5:
/*TODO*/ //	case (FPTR)MWA_BANK6:
/*TODO*/ //	case (FPTR)MWA_BANK7:
/*TODO*/ //	case (FPTR)MWA_BANK8:
/*TODO*/ //	case (FPTR)MWA_BANK9:
/*TODO*/ //	case (FPTR)MWA_BANK10:
/*TODO*/ //	case (FPTR)MWA_BANK11:
/*TODO*/ //	case (FPTR)MWA_BANK12:
/*TODO*/ //	case (FPTR)MWA_BANK13:
/*TODO*/ //	case (FPTR)MWA_BANK14:
/*TODO*/ //	case (FPTR)MWA_BANK15:
/*TODO*/ //	case (FPTR)MWA_BANK16:
/*TODO*/ //		hardware = (int)MWA_BANK1 - (int)handler + 1;
/*TODO*/ //		handler = bank_write_handler[hardware];
/*TODO*/ //		offset = bankwriteoffset[hardware];
/*TODO*/ //		break;
/*TODO*/ //	case (FPTR)MWA_NOP:
/*TODO*/ //		handler = mwh_nop;
/*TODO*/ //		break;
/*TODO*/ //	case (FPTR)MWA_RAMROM:
/*TODO*/ //		handler = mwh_ramrom;
/*TODO*/ //		break;
/*TODO*/ //	case (FPTR)MWA_ROM:
/*TODO*/ //		handler = mwh_rom;
/*TODO*/ //		break;
/*TODO*/ //	default:
/*TODO*/ //		offset = bankwriteoffset[bank];
/*TODO*/ //		break;
/*TODO*/ //	}
/*TODO*/ //	memorywriteoffset[bank] = offset;
/*TODO*/ //	memorywritehandler[bank] = handler;
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ ///* cpu change op-code memory base */
/*TODO*/ //void cpu_setOPbaseoverride (int cpu,opbase_handler function)
/*TODO*/ //{
/*TODO*/ //	setOPbasefunc[cpu] = function;
/*TODO*/ //	if (cpu == cpu_getactivecpu())
/*TODO*/ //		OPbasefunc = function;
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ //
public static UBytePtr install_mem_read_handler(int cpu,int start,int end,ReadHandlerPtr _handler)
{
	UByte hardware = new UByte();
	int abitsmin;
	int i, hw_set;
	if (errorlog!=null) fprintf(errorlog, "Install new memory read handler:\n");
	if (errorlog!=null) fprintf(errorlog, "             cpu: %d\n", cpu);
	if (errorlog!=null) fprintf(errorlog, "           start: 0x%08x\n", start);
	if (errorlog!=null) fprintf(errorlog, "             end: 0x%08x\n", end);

	if (errorlog!=null) fprintf(errorlog, " handler address: 0x%08x\n", _handler);

	abitsmin = ABITSMIN (cpu);

	/* see if this function is already registered */
	hw_set = 0;
	for ( i = 0 ; i < MH_HARDMAX ; i++)
	{
		/* record it if it matches */
		if (( memoryreadhandler[i] == _handler ) &&
			(  memoryreadoffset[i] == start))
		{
			if (errorlog!=null) fprintf(errorlog,"handler match - use old one\n");
			hardware.set((char)i);                  
			hw_set = 1;
                        //we don't handle it properly place a holder to check it later
                        throw new UnsupportedOperationException("Unsupported install_mem_read_handler for exsting match");
		}
	}
	/*switch (handler)//TODO
	{
		case MRA_RAM:
		case MRA_ROM:
			hardware.set((char) HT_RAM);	/* sprcial case ram read */
	/*		hw_set = 1;
			break;
		case MRA_BANK1:
		case MRA_BANK2:
		case MRA_BANK3:
		case MRA_BANK4:
		case MRA_BANK5:
		case MRA_BANK6:
		case MRA_BANK7:
		case MRA_BANK8:
		case MRA_BANK9:
		case MRA_BANK10:
		case MRA_BANK11:
		case MRA_BANK12:
		case MRA_BANK13:
		case MRA_BANK14:
		case MRA_BANK15:
		case MRA_BANK16:
		{
			hardware = (int)MRA_BANK1 - (int)handler + 1;
			hw_set = 1;
			break;
		}
		case MRA_NOP:
			hardware.set((char) HT_NOP);
			hw_set = 1;
			break;
	}*/
	if (hw_set==0)  /* no match */
	{
		/* create newer hardware handler */
		if( rdhard_max == MH_HARDMAX )
		{
			if (errorlog!=null) fprintf(errorlog, "read memory hardware pattern over !\n");
			if (errorlog!=null) fprintf(errorlog, "Failed to install new memory handler.\n");
			return memory_find_base(cpu, start);
		}
		else
		{
			/* register hardware function */
                        hardware.set((char) rdhard_max++);
                        memoryreadhandler[hardware.read()] = _handler;
                        memoryreadoffset[hardware.read()] = start;
		}
	}
	/* set hardware element table entry */
                    int temp_rdelement_max[] = new int[1]; //i can't pass a reference so here you go (shadow)
                    temp_rdelement_max[0] = rdelement_max;
                    set_element(cpu, cur_mr_element[cpu],
                            (int) ((start) >>> abitsmin), /*TODO checked unsigned if it's correct */
                            (int) ((end) >>> abitsmin), /*TODO checked unsigned if it's correct */
                            hardware, readhardware, temp_rdelement_max);
                    //cur_mr_element[cpu] = tem1.getUBytes();
                    //readhardware = tem2.getUBytes();
                    rdelement_max = temp_rdelement_max[0];
	if (errorlog!=null) fprintf(errorlog, "Done installing new memory handler.\n");
	if (errorlog!=null){
		fprintf(errorlog,"used read  elements %d/%d , functions %d/%d\n"
		    ,rdelement_max,MH_ELEMAX , rdhard_max,MH_HARDMAX );
	}
	return memory_find_base(cpu, start);
}
/*TODO*/ //
/*TODO*/ //void *install_mem_write_handler(int cpu, int start, int end, mem_write_handler handler)
/*TODO*/ //{
/*TODO*/ //	MHELE hardware = 0;
/*TODO*/ //	int abitsmin;
/*TODO*/ //	int i, hw_set;
/*TODO*/ //	if (errorlog) fprintf(errorlog, "Install new memory write handler:\n");
/*TODO*/ //	if (errorlog) fprintf(errorlog, "             cpu: %d\n", cpu);
/*TODO*/ //	if (errorlog) fprintf(errorlog, "           start: 0x%08x\n", start);
/*TODO*/ //	if (errorlog) fprintf(errorlog, "             end: 0x%08x\n", end);
/*TODO*/ //#ifdef __LP64__
/*TODO*/ //	if (errorlog) fprintf(errorlog, " handler address: 0x%016lx\n", (unsigned long) handler);
/*TODO*/ //#else
/*TODO*/ //	if (errorlog) fprintf(errorlog, " handler address: 0x%08x\n", (unsigned int) handler);
/*TODO*/ //#endif
/*TODO*/ //	abitsmin = ABITSMIN (cpu);
/*TODO*/ //
/*TODO*/ //	/* see if this function is already registered */
/*TODO*/ //	hw_set = 0;
/*TODO*/ //	for ( i = 0 ; i < MH_HARDMAX ; i++)
/*TODO*/ //	{
/*TODO*/ //		/* record it if it matches */
/*TODO*/ //		if (( memorywritehandler[i] == handler ) &&
/*TODO*/ //			(  memorywriteoffset[i] == start))
/*TODO*/ //		{
/*TODO*/ //			if (errorlog) fprintf(errorlog,"handler match - use old one\n");
/*TODO*/ //			hardware = i;
/*TODO*/ //			hw_set = 1;
/*TODO*/ //		}
/*TODO*/ //	}
/*TODO*/ //
/*TODO*/ //	switch( (FPTR)handler )
/*TODO*/ //	{
/*TODO*/ //		case (FPTR)MWA_RAM:
/*TODO*/ //			hardware = HT_RAM;	/* sprcial case ram write */
/*TODO*/ //			hw_set = 1;
/*TODO*/ //			break;
/*TODO*/ //		case (FPTR)MWA_BANK1:
/*TODO*/ //		case (FPTR)MWA_BANK2:
/*TODO*/ //		case (FPTR)MWA_BANK3:
/*TODO*/ //		case (FPTR)MWA_BANK4:
/*TODO*/ //		case (FPTR)MWA_BANK5:
/*TODO*/ //		case (FPTR)MWA_BANK6:
/*TODO*/ //		case (FPTR)MWA_BANK7:
/*TODO*/ //		case (FPTR)MWA_BANK8:
/*TODO*/ //		case (FPTR)MWA_BANK9:
/*TODO*/ //		case (FPTR)MWA_BANK10:
/*TODO*/ //		case (FPTR)MWA_BANK11:
/*TODO*/ //		case (FPTR)MWA_BANK12:
/*TODO*/ //		case (FPTR)MWA_BANK13:
/*TODO*/ //		case (FPTR)MWA_BANK14:
/*TODO*/ //		case (FPTR)MWA_BANK15:
/*TODO*/ //		case (FPTR)MWA_BANK16:
/*TODO*/ //		{
/*TODO*/ //			hardware = (int)MWA_BANK1 - (int)handler + 1;
/*TODO*/ //			hw_set = 1;
/*TODO*/ //			break;
/*TODO*/ //		}
/*TODO*/ //		case (FPTR)MWA_NOP:
/*TODO*/ //			hardware = HT_NOP;
/*TODO*/ //			hw_set = 1;
/*TODO*/ //			break;
/*TODO*/ //		case (FPTR)MWA_RAMROM:
/*TODO*/ //			hardware = HT_RAMROM;
/*TODO*/ //			hw_set = 1;
/*TODO*/ //			break;
/*TODO*/ //		case (FPTR)MWA_ROM:
/*TODO*/ //			hardware = HT_ROM;
/*TODO*/ //			hw_set = 1;
/*TODO*/ //			break;
/*TODO*/ //	}
/*TODO*/ //	if (!hw_set)  /* no match */
/*TODO*/ //	{
/*TODO*/ //		/* create newer hardware handler */
/*TODO*/ //		if( wrhard_max == MH_HARDMAX )
/*TODO*/ //		{
/*TODO*/ //			if (errorlog) fprintf(errorlog, "write memory hardware pattern over !\n");
/*TODO*/ //			if (errorlog) fprintf(errorlog, "Failed to install new memory handler.\n");
/*TODO*/ //
/*TODO*/ //			return memory_find_base(cpu, start);
/*TODO*/ //		}
/*TODO*/ //		else
/*TODO*/ //		{
/*TODO*/ //			/* register hardware function */
/*TODO*/ //			hardware = wrhard_max++;
/*TODO*/ //			memorywritehandler[hardware] = handler;
/*TODO*/ //			memorywriteoffset[hardware] = start;
/*TODO*/ //		}
/*TODO*/ //	}
/*TODO*/ //	/* set hardware element table entry */
/*TODO*/ //	set_element( cpu , cur_mw_element[cpu] ,
/*TODO*/ //		(((unsigned int) start) >> abitsmin) ,
/*TODO*/ //		(((unsigned int) end) >> abitsmin) ,
/*TODO*/ //		hardware , writehardware , &wrelement_max );
/*TODO*/ //	if (errorlog) fprintf(errorlog, "Done installing new memory handler.\n");
/*TODO*/ //	if (errorlog){
/*TODO*/ //		fprintf(errorlog,"used write elements %d/%d , functions %d/%d\n"
/*TODO*/ //		    ,wrelement_max,MH_ELEMAX , wrhard_max,MH_HARDMAX );
/*TODO*/ //	}
/*TODO*/ //	return memory_find_base(cpu, start);
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ //void *install_port_read_handler(int cpu, int start, int end, mem_read_handler handler)
/*TODO*/ //{
/*TODO*/ //	return install_port_read_handler_common(cpu, start, end, handler, 1);
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ //void *install_port_write_handler(int cpu, int start, int end, mem_write_handler handler)
/*TODO*/ //{
/*TODO*/ //	return install_port_write_handler_common(cpu, start, end, handler, 1);
/*TODO*/ //}
/*TODO*/ //

    public static IOReadPort[] install_port_read_handler_common(int cpu, int start, int end, int handler, ReadHandlerPtr _handler, int install_at_beginning) {
        int i, oldsize;

        oldsize = readport_size[cpu];
        readport_size[cpu]++;//readport_size[cpu] += sizeof(struct IOReadPort);

        if (readport[cpu] == null) {
            readport[cpu] = new IOReadPort[readport_size[cpu]];//readport[cpu] = malloc(readport_size[cpu]);
        } else {
            /*TODO CHECKIF IT'S OKAY*/                 //readport[cpu] = realloc(readport[cpu], readport_size[cpu]);
            IOReadPort[] temp = Arrays.copyOf(readport[cpu], readport_size[cpu]);
            readport[cpu] = temp;
        }
        if (readport[cpu] == null) {
            return null;
        }


        if (install_at_beginning != 0) {
            /* can't do a single memcpy because it doesn't handle overlapping regions correctly??? */
            for (i = oldsize; i >= 1; i--) //for (i = oldsize / sizeof(struct IOReadPort); i >= 1; i--)
            {
                System.arraycopy(readport[cpu], i - 1, readport[cpu], i, 1); //memcpy(&readport[cpu][i], &readport[cpu][i - 1], sizeof(struct IOReadPort));

            }
            i = 0;
        } else {
            i = oldsize; //i = oldsize / sizeof(struct IOReadPort);
        }


        if (errorlog != null) {
            fprintf(errorlog, "Installing port read handler: cpu %d  slot %X  start %X  end %X\n", cpu, i, start, end);
        }

        readport[cpu][i] = new IOReadPort();
        readport[cpu][i].start = start;
        readport[cpu][i].end = end;
        readport[cpu][i].handler = handler;
        readport[cpu][i]._handler = _handler;

        return readport[cpu];
    }

    public static IOWritePort[] install_port_write_handler_common(int cpu, int start, int end, int handler, WriteHandlerPtr _handler, int install_at_beginning) {
        int i, oldsize;

        oldsize = writeport_size[cpu];
        writeport_size[cpu]++;  //writeport_size[cpu] += sizeof(struct IOWritePort);

        if (writeport[cpu] == null) {
            writeport[cpu] = new IOWritePort[writeport_size[cpu]];
        } else {
            /*TODO CHECKIF IT'S OKAY*/                  //writeport[cpu] = realloc(writeport[cpu], writeport_size[cpu]);
            IOWritePort[] temp = Arrays.copyOf(writeport[cpu], writeport_size[cpu]);
            writeport[cpu] = temp;
        }

        if (writeport[cpu] == null) {
            return null;
        }

        if (install_at_beginning != 0) {
            /* can't do a single memcpy because it doesn't handle overlapping regions correctly??? */
            for (i = oldsize; i >= 1; i--)//for (i = oldsize / sizeof(struct IOWritePort); i >= 1; i--)
            {
                System.arraycopy(writeport[cpu], i - 1, writeport[cpu], i, 1); //memcpy(&writeport[cpu][i], &writeport[cpu][i - 1], sizeof(struct IOWritePort));
            }

            i = 0;
        } else {
            i = oldsize;
        }
        if (errorlog != null) {
            fprintf(errorlog, "Installing port write handler: cpu %d  slot %X  start %X  end %X\n", cpu, i, start, end);
        }

        writeport[cpu][i] = new IOWritePort();
        writeport[cpu][i].start = start;
        writeport[cpu][i].end = end;
        writeport[cpu][i].handler = handler;
        writeport[cpu][i]._handler = _handler;
        return writeport[cpu];

    }

    public static void mem_dump() {
        /*TODO*/ //	extern int totalcpu;
        int cpu;
        int naddr, addr;
        UByte nhw = new UByte();
        UByte hw = new UByte();
        FILE temp = fopen("memdump.log", "wa");

        if (temp == null) {
            return;
        }
        for (cpu = 0; cpu < cpu_gettotalcpu(); cpu++) {
            fprintf(temp, "cpu %d read memory \n", cpu);
            addr = 0;
            naddr = 0;
            nhw.set((char) 0xff);
            while ((addr >> mhshift[cpu][0]) <= mhmask[cpu][0]) {
                hw.set(cur_mr_element[cpu].read(addr >> mhshift[cpu][0]));
                if (hw.read() >= MH_HARDMAX) {	/* 2nd element link */
                    hw.set(readhardware.read(((hw.read()-MH_HARDMAX)<<MH_SBITS) + ((addr>>mhshift[cpu][1]) & mhmask[cpu][1])));
                    if (hw.read() >= MH_HARDMAX) {
                        hw.set(readhardware.read(((hw.read()-MH_HARDMAX)<<MH_SBITS) + (addr & mhmask[cpu][2])));
                    }
                }
                if (nhw.read() != hw.read()) {
                    if (addr != 0) {
                        fprintf(temp, "  %08x(%08x) - %08x = %02x\n", naddr, memoryreadoffset[nhw.read()], addr - 1, Integer.valueOf(nhw.read()));
                    }
                    nhw.set(hw.read());
                    naddr = addr;
                }
                addr++;
            }
            fprintf(temp, "  %08x(%08x) - %08x = %02x\n", naddr, memoryreadoffset[nhw.read()], addr - 1, Integer.valueOf(nhw.read()));

            fprintf(temp, "cpu %d write memory \n", cpu);
            naddr = 0;
            addr = 0;
            nhw.set((char) 0xff);
            while ((addr >> mhshift[cpu][0]) <= mhmask[cpu][0]) {
                hw.set(cur_mw_element[cpu].read(addr >> mhshift[cpu][0]));
                if (hw.read() >= MH_HARDMAX) {	/* 2nd element link */
                    hw.set(writehardware.read(((hw.read()-MH_HARDMAX)<<MH_SBITS) + ((addr>>mhshift[cpu][1]) & mhmask[cpu][1])));
                    if (hw.read() >= MH_HARDMAX) {
                        hw.set(writehardware.read(((hw.read()-MH_HARDMAX)<<MH_SBITS) + (addr & mhmask[cpu][2])));
                    }
                }
                if (nhw.read() != hw.read()) {
                    if (addr != 0) {
                        fprintf(temp, "  %08x(%08x) - %08x = %02x\n", naddr, memorywriteoffset[nhw.read()], addr - 1, Integer.valueOf(nhw.read()));
                    }
                    nhw.set(hw.read());
                    naddr = addr;
                }
                addr++;
            }
            fprintf(temp, "  %08x(%08x) - %08x = %02x\n", naddr, memorywriteoffset[nhw.read()], addr - 1, Integer.valueOf(nhw.read()));
        }
        fclose(temp);
    }
}
