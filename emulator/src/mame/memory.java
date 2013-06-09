/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mame;

/**
 *
 * @author nickblame
 */
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

    /*TODO*/ ///***************************************************************************
/*TODO*/ //
/*TODO*/ //  memory.c
/*TODO*/ //
/*TODO*/ //  Functions which handle the CPU memory and I/O port access.
/*TODO*/ //
/*TODO*/ //***************************************************************************/
    /*TODO*/ //
/*TODO*/ ///* #define MEM_DUMP */
/*TODO*/ //
/*TODO*/ //#ifdef MEM_DUMP
/*TODO*/ //static void mem_dump( void );
/*TODO*/ //#endif
/*TODO*/ //
/*TODO*/ ///* Convenience macros - not in cpuintrf.h because they shouldn't be used by everyone */
/*TODO*/ //#define ADDRESS_BITS(index)             (cpuintf[Machine->drv->cpu[index].cpu_type & ~CPU_FLAGS_MASK].address_bits)
/*TODO*/ //#define ABITS1(index)                   (cpuintf[Machine->drv->cpu[index].cpu_type & ~CPU_FLAGS_MASK].abits1)
/*TODO*/ //#define ABITS2(index)                   (cpuintf[Machine->drv->cpu[index].cpu_type & ~CPU_FLAGS_MASK].abits2)
/*TODO*/ //#define ABITS3(index)                   (0)
/*TODO*/ //#define ABITSMIN(index)                 (cpuintf[Machine->drv->cpu[index].cpu_type & ~CPU_FLAGS_MASK].abitsmin)
/*TODO*/ //
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
    /*TODO*/ //
/*TODO*/ ///* change bases preserving opcode/data shift for encrypted games */
/*TODO*/ //#define SET_OP_RAMROM(base)					\
/*TODO*/ //	OP_ROM = (base) + (OP_ROM - OP_RAM);	\
/*TODO*/ //	OP_RAM = (base);
/*TODO*/ //
/*TODO*/ //
    public static UByte ophw = new UByte(); /* op-code hardware number */

    public static ExtMemory[] ext_memory = new ExtMemory[MAX_EXT_MEMORY];
    public static UBytePtr[] ramptr = new UBytePtr[MAX_CPU];
    public static UBytePtr[] romptr = new UBytePtr[MAX_CPU];

    /*TODO*/ ///* element shift bits, mask bits */
/*TODO*/ //int mhshift[MAX_CPU][3], mhmask[MAX_CPU][3];
/*TODO*/ //
    /* pointers to port structs */
    /* ASG: port speedup */
    static IOReadPort[][] readport = new IOReadPort[MAX_CPU][];
    static IOWritePort[][] writeport = new IOWritePort[MAX_CPU][];
    static int[] portmask = new int[MAX_CPU];
    static int[] readport_size = new int[MAX_CPU];
    static int[] writeport_size = new int[MAX_CPU];
    /*TODO*/ ///* HJB 990210: removed 'static' for access by assembly CPU core memory handlers */
/*TODO*/ //const struct IOReadPort *cur_readport;
/*TODO*/ //const struct IOWritePort *cur_writeport;
    static int cur_portmask;
    /* current hardware element map */
    static UByte[][] cur_mr_element = new UByte[MAX_CPU][];
    static UByte[][] cur_mw_element = new UByte[MAX_CPU][];

    /*TODO*/ ///* sub memory/port hardware element map */
/*TODO*/ ///* HJB 990210: removed 'static' for access by assembly CPU core memory handlers */
/*TODO*/ //MHELE readhardware[MH_ELEMAX << MH_SBITS];	/* mem/port read  */
/*TODO*/ //MHELE writehardware[MH_ELEMAX << MH_SBITS]; /* mem/port write */
/*TODO*/ //
/*TODO*/ ///* memory hardware element map */
/*TODO*/ ///* value:                      */
/*TODO*/ //#define HT_RAM    0		/* RAM direct        */
/*TODO*/ //#define HT_BANK1  1		/* bank memory #1    */
/*TODO*/ //#define HT_BANK2  2		/* bank memory #2    */
/*TODO*/ //#define HT_BANK3  3		/* bank memory #3    */
/*TODO*/ //#define HT_BANK4  4		/* bank memory #4    */
/*TODO*/ //#define HT_BANK5  5		/* bank memory #5    */
/*TODO*/ //#define HT_BANK6  6		/* bank memory #6    */
/*TODO*/ //#define HT_BANK7  7		/* bank memory #7    */
/*TODO*/ //#define HT_BANK8  8		/* bank memory #8    */
/*TODO*/ //#define HT_BANK9  9		/* bank memory #9    */
/*TODO*/ //#define HT_BANK10 10	/* bank memory #10   */
/*TODO*/ //#define HT_BANK11 11	/* bank memory #11   */
/*TODO*/ //#define HT_BANK12 12	/* bank memory #12   */
/*TODO*/ //#define HT_BANK13 13	/* bank memory #13   */
/*TODO*/ //#define HT_BANK14 14	/* bank memory #14   */
/*TODO*/ //#define HT_BANK15 15	/* bank memory #15   */
/*TODO*/ //#define HT_BANK16 16	/* bank memory #16   */
/*TODO*/ //#define HT_NON    17	/* non mapped memory */
/*TODO*/ //#define HT_NOP    18	/* NOP memory        */
/*TODO*/ //#define HT_RAMROM 19	/* RAM ROM memory    */
/*TODO*/ //#define HT_ROM    20	/* ROM memory        */
/*TODO*/ //
    static final int HT_USER = 21;	/* user functions    */
    /*TODO*/ ///* [MH_HARDMAX]-0xff	  link to sub memory element  */
/*TODO*/ ///*                        (value-MH_HARDMAX)<<MH_SBITS -> element bank */
/*TODO*/ //
/*TODO*/ //#define HT_BANKMAX (HT_BANK1 + MAX_BANKS - 1)
/*TODO*/ //
    /* memory hardware handler */
    /* HJB 990210: removed 'static' for access by assembly CPU core memory handlers */

    static ReadHandlerPtr[] memoryreadhandler = new ReadHandlerPtr[MH_HARDMAX]; //mem_read_handler memoryreadhandler[MH_HARDMAX];
    static int[] memoryreadoffset = new int[MH_HARDMAX];
    static WriteHandlerPtr[] memorywritehandler = new WriteHandlerPtr[MH_HARDMAX];//mem_write_handler memorywritehandler[MH_HARDMAX];
    static int[] memorywriteoffset = new int[MH_HARDMAX];
    /*TODO*/ ///* bank ram base address; RAM is bank 0 */
/*TODO*/ //unsigned char *cpu_bankbase[HT_BANKMAX + 1];
/*TODO*/ //static int bankreadoffset[HT_BANKMAX + 1];
/*TODO*/ //static int bankwriteoffset[HT_BANKMAX + 1];
/*TODO*/ //
/*TODO*/ ///* override OP base handler */
    public static opbase_handlerPtr[] setOPbasefunc = new opbase_handlerPtr[MAX_CPU];
    /*TODO*/ //static opbase_handler OPbasefunc;
/*TODO*/ //
/*TODO*/ ///* current cpu current hardware element map point */
/*TODO*/ //MHELE *cur_mrhard;
/*TODO*/ //MHELE *cur_mwhard;
/*TODO*/ //
    /* empty port handler structures */
    public static IOReadPort[] empty_readport = {new IOReadPort(-1)};
    public static IOWritePort[] empty_writeport = {new IOWritePort(-1)};

    /*TODO*/ //static void *install_port_read_handler_common(int cpu, int start, int end, mem_read_handler handler, int install_at_beginning);
/*TODO*/ //static void *install_port_write_handler_common(int cpu, int start, int end, mem_write_handler handler, int install_at_beginning);
/*TODO*/ //
/*TODO*/ //
/*TODO*/ ///***************************************************************************
/*TODO*/ //
/*TODO*/ //  Memory read handling
/*TODO*/ //
/*TODO*/ //***************************************************************************/
/*TODO*/ //
/*TODO*/ //READ_HANDLER(mrh_ram)		{ return cpu_bankbase[0][offset]; }
/*TODO*/ //READ_HANDLER(mrh_bank1)		{ return cpu_bankbase[1][offset]; }
/*TODO*/ //READ_HANDLER(mrh_bank2)		{ return cpu_bankbase[2][offset]; }
/*TODO*/ //READ_HANDLER(mrh_bank3)		{ return cpu_bankbase[3][offset]; }
/*TODO*/ //READ_HANDLER(mrh_bank4)		{ return cpu_bankbase[4][offset]; }
/*TODO*/ //READ_HANDLER(mrh_bank5)		{ return cpu_bankbase[5][offset]; }
/*TODO*/ //READ_HANDLER(mrh_bank6)		{ return cpu_bankbase[6][offset]; }
/*TODO*/ //READ_HANDLER(mrh_bank7)		{ return cpu_bankbase[7][offset]; }
/*TODO*/ //READ_HANDLER(mrh_bank8)		{ return cpu_bankbase[8][offset]; }
/*TODO*/ //READ_HANDLER(mrh_bank9)		{ return cpu_bankbase[9][offset]; }
/*TODO*/ //READ_HANDLER(mrh_bank10)	{ return cpu_bankbase[10][offset]; }
/*TODO*/ //READ_HANDLER(mrh_bank11)	{ return cpu_bankbase[11][offset]; }
/*TODO*/ //READ_HANDLER(mrh_bank12)	{ return cpu_bankbase[12][offset]; }
/*TODO*/ //READ_HANDLER(mrh_bank13)	{ return cpu_bankbase[13][offset]; }
/*TODO*/ //READ_HANDLER(mrh_bank14)	{ return cpu_bankbase[14][offset]; }
/*TODO*/ //READ_HANDLER(mrh_bank15)	{ return cpu_bankbase[15][offset]; }
/*TODO*/ //READ_HANDLER(mrh_bank16)	{ return cpu_bankbase[16][offset]; }
/*TODO*/ //static mem_read_handler bank_read_handler[] =
/*TODO*/ //{
/*TODO*/ //	mrh_ram,   mrh_bank1,  mrh_bank2,  mrh_bank3,  mrh_bank4,  mrh_bank5,  mrh_bank6,  mrh_bank7,
/*TODO*/ //	mrh_bank8, mrh_bank9,  mrh_bank10, mrh_bank11, mrh_bank12, mrh_bank13, mrh_bank14, mrh_bank15,
/*TODO*/ //	mrh_bank16
/*TODO*/ //};
/*TODO*/ //
/*TODO*/ //READ_HANDLER(mrh_error)
/*TODO*/ //{
/*TODO*/ //	if (errorlog) fprintf(errorlog,"CPU #%d PC %04x: warning - read %02x from unmapped memory address %04x\n",cpu_getactivecpu(),cpu_get_pc(),cpu_bankbase[0][offset],offset);
/*TODO*/ //	return cpu_bankbase[0][offset];
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ //READ_HANDLER(mrh_error_sparse)
/*TODO*/ //{
/*TODO*/ //	if (errorlog) fprintf(errorlog,"CPU #%d PC %08x: warning - read unmapped memory address %08x\n",cpu_getactivecpu(),cpu_get_pc(),offset);
/*TODO*/ //	return 0;
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ //READ_HANDLER(mrh_error_sparse_bit)
/*TODO*/ //{
/*TODO*/ //	if (errorlog) fprintf(errorlog,"CPU #%d PC %08x: warning - read unmapped memory bit addr %08x (byte addr %08x)\n",cpu_getactivecpu(),cpu_get_pc(),offset<<3, offset);
/*TODO*/ //	return 0;
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ //READ_HANDLER(mrh_nop)
/*TODO*/ //{
/*TODO*/ //	return 0;
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ //
/*TODO*/ ///***************************************************************************
/*TODO*/ //
/*TODO*/ //  Memory write handling
/*TODO*/ //
/*TODO*/ //***************************************************************************/
/*TODO*/ //
/*TODO*/ //WRITE_HANDLER(mwh_ram)		{ cpu_bankbase[0][offset] = data;}
/*TODO*/ //WRITE_HANDLER(mwh_bank1)	{ cpu_bankbase[1][offset] = data; }
/*TODO*/ //WRITE_HANDLER(mwh_bank2)	{ cpu_bankbase[2][offset] = data; }
/*TODO*/ //WRITE_HANDLER(mwh_bank3)	{ cpu_bankbase[3][offset] = data; }
/*TODO*/ //WRITE_HANDLER(mwh_bank4)	{ cpu_bankbase[4][offset] = data; }
/*TODO*/ //WRITE_HANDLER(mwh_bank5)	{ cpu_bankbase[5][offset] = data; }
/*TODO*/ //WRITE_HANDLER(mwh_bank6)	{ cpu_bankbase[6][offset] = data; }
/*TODO*/ //WRITE_HANDLER(mwh_bank7)	{ cpu_bankbase[7][offset] = data; }
/*TODO*/ //WRITE_HANDLER(mwh_bank8)	{ cpu_bankbase[8][offset] = data; }
/*TODO*/ //WRITE_HANDLER(mwh_bank9)	{ cpu_bankbase[9][offset] = data; }
/*TODO*/ //WRITE_HANDLER(mwh_bank10)	{ cpu_bankbase[10][offset] = data; }
/*TODO*/ //WRITE_HANDLER(mwh_bank11)	{ cpu_bankbase[11][offset] = data; }
/*TODO*/ //WRITE_HANDLER(mwh_bank12)	{ cpu_bankbase[12][offset] = data; }
/*TODO*/ //WRITE_HANDLER(mwh_bank13)	{ cpu_bankbase[13][offset] = data; }
/*TODO*/ //WRITE_HANDLER(mwh_bank14)	{ cpu_bankbase[14][offset] = data; }
/*TODO*/ //WRITE_HANDLER(mwh_bank15)	{ cpu_bankbase[15][offset] = data; }
/*TODO*/ //WRITE_HANDLER(mwh_bank16)	{ cpu_bankbase[16][offset] = data; }
/*TODO*/ //static mem_write_handler bank_write_handler[] =
/*TODO*/ //{
/*TODO*/ //	mwh_ram,   mwh_bank1,  mwh_bank2,  mwh_bank3,  mwh_bank4,  mwh_bank5,  mwh_bank6,  mwh_bank7,
/*TODO*/ //	mwh_bank8, mwh_bank9,  mwh_bank10, mwh_bank11, mwh_bank12, mwh_bank13, mwh_bank14, mwh_bank15,
/*TODO*/ //	mwh_bank16
/*TODO*/ //};
/*TODO*/ //
/*TODO*/ //WRITE_HANDLER(mwh_error)
/*TODO*/ //{
/*TODO*/ //	if (errorlog) fprintf(errorlog,"CPU #%d PC %04x: warning - write %02x to unmapped memory address %04x\n",cpu_getactivecpu(),cpu_get_pc(),data,offset);
/*TODO*/ //	cpu_bankbase[0][offset] = data;
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ //WRITE_HANDLER(mwh_error_sparse)
/*TODO*/ //{
/*TODO*/ //	if (errorlog) fprintf(errorlog,"CPU #%d PC %08x: warning - write %02x to unmapped memory address %08x\n",cpu_getactivecpu(),cpu_get_pc(),data,offset);
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ //WRITE_HANDLER(mwh_error_sparse_bit)
/*TODO*/ //{
/*TODO*/ //	if (errorlog) fprintf(errorlog,"CPU #%d PC %08x: warning - write %02x to unmapped memory bit addr %08x\n",cpu_getactivecpu(),cpu_get_pc(),data,offset<<3);
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ //WRITE_HANDLER(mwh_rom)
/*TODO*/ //{
/*TODO*/ //	if (errorlog) fprintf(errorlog,"CPU #%d PC %04x: warning - write %02x to ROM address %04x\n",cpu_getactivecpu(),cpu_get_pc(),data,offset);
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ //WRITE_HANDLER(mwh_ramrom)
/*TODO*/ //{
/*TODO*/ //	cpu_bankbase[0][offset] = cpu_bankbase[0][offset + (OP_ROM - OP_RAM)] = data;
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ //WRITE_HANDLER(mwh_nop)
/*TODO*/ //{
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ //
/*TODO*/ ///***************************************************************************
/*TODO*/ //
/*TODO*/ //  Memory structure building
/*TODO*/ //
/*TODO*/ //***************************************************************************/
/*TODO*/ //
/*TODO*/ ///* return element offset */
/*TODO*/ //static MHELE *get_element( MHELE *element , int ad , int elemask ,
/*TODO*/ //                        MHELE *subelement , int *ele_max )
/*TODO*/ //{
/*TODO*/ //	MHELE hw = element[ad];
/*TODO*/ //	int i,ele;
/*TODO*/ //	int banks = ( elemask / (1<<MH_SBITS) ) + 1;
/*TODO*/ //
/*TODO*/ //	if( hw >= MH_HARDMAX ) return &subelement[(hw-MH_HARDMAX)<<MH_SBITS];
/*TODO*/ //
/*TODO*/ //	/* create new element block */
/*TODO*/ //	if( (*ele_max)+banks > MH_ELEMAX )
/*TODO*/ //	{
/*TODO*/ //		if (errorlog) fprintf(errorlog,"memory element size over \n");
/*TODO*/ //		return 0;
/*TODO*/ //	}
/*TODO*/ //	/* get new element nunber */
/*TODO*/ //	ele = *ele_max;
/*TODO*/ //	(*ele_max)+=banks;
/*TODO*/ //#ifdef MEM_DUMP
/*TODO*/ //	if (errorlog) fprintf(errorlog,"create element %2d(%2d)\n",ele,banks);
/*TODO*/ //#endif
/*TODO*/ //	/* set link mark to current element */
/*TODO*/ //	element[ad] = ele + MH_HARDMAX;
/*TODO*/ //	/* get next subelement top */
/*TODO*/ //	subelement  = &subelement[ele<<MH_SBITS];
/*TODO*/ //	/* initialize new block */
/*TODO*/ //	for( i = 0 ; i < (1<<MH_SBITS) ; i++ )
/*TODO*/ //		subelement[i] = hw;
/*TODO*/ //
/*TODO*/ //	return subelement;
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ //static void set_element( int cpu , MHELE *celement , int sp , int ep , MHELE type , MHELE *subelement , int *ele_max )
/*TODO*/ //{
/*TODO*/ //	int i;
/*TODO*/ //	int edepth = 0;
/*TODO*/ //	int shift,mask;
/*TODO*/ //	MHELE *eele = celement;
/*TODO*/ //	MHELE *sele = celement;
/*TODO*/ //	MHELE *ele;
/*TODO*/ //	int ss,sb,eb,ee;
/*TODO*/ //
/*TODO*/ //#ifdef MEM_DUMP
/*TODO*/ //	if (errorlog) fprintf(errorlog,"set_element %8X-%8X = %2X\n",sp,ep,type);
/*TODO*/ //#endif
/*TODO*/ //	if( (unsigned int) sp > (unsigned int) ep ) return;
/*TODO*/ //	do{
/*TODO*/ //		mask  = mhmask[cpu][edepth];
/*TODO*/ //		shift = mhshift[cpu][edepth];
/*TODO*/ //
/*TODO*/ //		/* center element */
/*TODO*/ //		ss = (unsigned int) sp >> shift;
/*TODO*/ //		sb = (unsigned int) sp ? ((unsigned int) (sp-1) >> shift) + 1 : 0;
/*TODO*/ //		eb = ((unsigned int) (ep+1) >> shift) - 1;
/*TODO*/ //		ee = (unsigned int) ep >> shift;
/*TODO*/ //
/*TODO*/ //		if( sb <= eb )
/*TODO*/ //		{
/*TODO*/ //			if( (sb|mask)==(eb|mask) )
/*TODO*/ //			{
/*TODO*/ //				/* same reasion */
/*TODO*/ //				ele = (sele ? sele : eele);
/*TODO*/ //				for( i = sb ; i <= eb ; i++ ){
/*TODO*/ //				 	ele[i & mask] = type;
/*TODO*/ //				}
/*TODO*/ //			}
/*TODO*/ //			else
/*TODO*/ //			{
/*TODO*/ //				if( sele ) for( i = sb ; i <= (sb|mask) ; i++ )
/*TODO*/ //				 	sele[i & mask] = type;
/*TODO*/ //				if( eele ) for( i = eb&(~mask) ; i <= eb ; i++ )
/*TODO*/ //				 	eele[i & mask] = type;
/*TODO*/ //			}
/*TODO*/ //		}
/*TODO*/ //
/*TODO*/ //		edepth++;
/*TODO*/ //
/*TODO*/ //		if( ss == sb ) sele = 0;
/*TODO*/ //		else sele = get_element( sele , ss & mask , mhmask[cpu][edepth] ,
/*TODO*/ //									subelement , ele_max );
/*TODO*/ //		if( ee == eb ) eele = 0;
/*TODO*/ //		else eele = get_element( eele , ee & mask , mhmask[cpu][edepth] ,
/*TODO*/ //									subelement , ele_max );
/*TODO*/ //
/*TODO*/ //	}while( sele || eele );
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ //
/*TODO*/ ///* ASG 980121 -- allocate all the external memory */
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
                        Machine.drv.cpu[cpu].memory_write[_mwa].base.base = b.offset;
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
                if (install_port_read_handler_common(cpu, Machine.drv.cpu[cpu].port_read[ioread_ptr].start, Machine.drv.cpu[cpu].port_read[ioread_ptr].end, Machine.drv.cpu[cpu].port_read[ioread_ptr]._handler, 0) == null) {
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
                if (install_port_write_handler_common(cpu, Machine.drv.cpu[cpu].port_write[iowrite_ptr].start, Machine.drv.cpu[cpu].port_write[iowrite_ptr].end, Machine.drv.cpu[cpu].port_write[iowrite_ptr]._handler, 0) == null) {
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
	for( i = 0 ; i < MH_HARDMAX ; i++ ){
		memoryreadoffset[i] = 0;
		memorywriteoffset[i] = 0;
	}
	/* bank memory */
	for (i = 1; i <= MAX_BANKS; i++)
	{
/*TODO*/ //		memoryreadhandler[i] = bank_read_handler[i];
/*TODO*/ //		memorywritehandler[i] = bank_write_handler[i];
	}
/*TODO*/ //	/* non map memory */
/*TODO*/ //	memoryreadhandler[HT_NON] = mrh_error;
/*TODO*/ //	memorywritehandler[HT_NON] = mwh_error;
/*TODO*/ //	/* NOP memory */
/*TODO*/ //	memoryreadhandler[HT_NOP] = mrh_nop;
/*TODO*/ //	memorywritehandler[HT_NOP] = mwh_nop;
/*TODO*/ //	/* RAMROM memory */
/*TODO*/ //	memorywritehandler[HT_RAMROM] = mwh_ramrom;
/*TODO*/ //	/* ROM memory */
/*TODO*/ //	memorywritehandler[HT_ROM] = mwh_rom;
/*TODO*/ //
/*TODO*/ //	/* if any CPU is 21-bit or more, we change the error handlers to be more benign */
/*TODO*/ //	for (cpu = 0; cpu < cpu_gettotalcpu(); cpu++)
/*TODO*/ //		if (ADDRESS_BITS (cpu) >= 21)
/*TODO*/ //		{
/*TODO*/ //			memoryreadhandler[HT_NON] = mrh_error_sparse;
/*TODO*/ //			memorywritehandler[HT_NON] = mwh_error_sparse;
/*TODO*/ //#if HAS_TMS34010
/*TODO*/ //            if ((Machine->drv->cpu[cpu].cpu_type & ~CPU_FLAGS_MASK)==CPU_TMS34010)
/*TODO*/ //			{
/*TODO*/ //				memoryreadhandler[HT_NON] = mrh_error_sparse_bit;
/*TODO*/ //				memorywritehandler[HT_NON] = mwh_error_sparse_bit;
/*TODO*/ //			}
/*TODO*/ //#endif
/*TODO*/ //        }
/*TODO*/ //
/*TODO*/ //	for( cpu = 0 ; cpu < cpu_gettotalcpu() ; cpu++ )
/*TODO*/ //	{
/*TODO*/ //		/* cpu selection */
/*TODO*/ //		abits1 = ABITS1 (cpu);
/*TODO*/ //		abits2 = ABITS2 (cpu);
/*TODO*/ //		abits3 = ABITS3 (cpu);
/*TODO*/ //		abitsmin = ABITSMIN (cpu);
/*TODO*/ //
/*TODO*/ //		/* element shifter , mask set */
/*TODO*/ //		mhshift[cpu][0] = (abits2+abits3);
/*TODO*/ //		mhshift[cpu][1] = abits3;			/* 2nd */
/*TODO*/ //		mhshift[cpu][2] = 0;				/* 3rd (used by set_element)*/
/*TODO*/ //		mhmask[cpu][0]  = MHMASK(abits1);		/*1st(used by set_element)*/
/*TODO*/ //		mhmask[cpu][1]  = MHMASK(abits2);		/*2nd*/
/*TODO*/ //		mhmask[cpu][2]  = MHMASK(abits3);		/*3rd*/
/*TODO*/ //
/*TODO*/ //		/* allocate current element */
/*TODO*/ //		if( (cur_mr_element[cpu] = (MHELE *)malloc(sizeof(MHELE)<<abits1)) == 0 )
/*TODO*/ //		{
/*TODO*/ //			memory_shutdown();
/*TODO*/ //			return 0;
/*TODO*/ //		}
/*TODO*/ //		if( (cur_mw_element[cpu] = (MHELE *)malloc(sizeof(MHELE)<<abits1)) == 0 )
/*TODO*/ //		{
/*TODO*/ //			memory_shutdown();
/*TODO*/ //			return 0;
/*TODO*/ //		}
/*TODO*/ //
/*TODO*/ //		/* initialize curent element table */
/*TODO*/ //		for( i = 0 ; i < (1<<abits1) ; i++ )
/*TODO*/ //		{
/*TODO*/ //			cur_mr_element[cpu][i] = HT_NON;	/* no map memory */
/*TODO*/ //			cur_mw_element[cpu][i] = HT_NON;	/* no map memory */
/*TODO*/ //		}
/*TODO*/ //
/*TODO*/ //		memoryread = Machine->drv->cpu[cpu].memory_read;
/*TODO*/ //		memorywrite = Machine->drv->cpu[cpu].memory_write;
/*TODO*/ //
/*TODO*/ //		/* memory read handler build */
/*TODO*/ //		if (memoryread)
/*TODO*/ //		{
/*TODO*/ //			mra = memoryread;
/*TODO*/ //			while (mra->start != -1) mra++;
/*TODO*/ //			mra--;
/*TODO*/ //
/*TODO*/ //			while (mra >= memoryread)
/*TODO*/ //			{
/*TODO*/ //				mem_read_handler handler = mra->handler;
/*TODO*/ //
/*TODO*/ ///* work around a compiler bug */
/*TODO*/ //#ifdef SGI_FIX_MWA_NOP
/*TODO*/ //				if ((FPTR)handler == (FPTR)MRA_NOP) {
/*TODO*/ //					hardware = HT_NOP;
/*TODO*/ //				} else {
/*TODO*/ //#endif
/*TODO*/ //				switch ((FPTR)handler)
/*TODO*/ //				{
/*TODO*/ //				case (FPTR)MRA_RAM:
/*TODO*/ //				case (FPTR)MRA_ROM:
/*TODO*/ //					hardware = HT_RAM;	/* sprcial case ram read */
/*TODO*/ //					break;
/*TODO*/ //				case (FPTR)MRA_BANK1:
/*TODO*/ //				case (FPTR)MRA_BANK2:
/*TODO*/ //				case (FPTR)MRA_BANK3:
/*TODO*/ //				case (FPTR)MRA_BANK4:
/*TODO*/ //				case (FPTR)MRA_BANK5:
/*TODO*/ //				case (FPTR)MRA_BANK6:
/*TODO*/ //				case (FPTR)MRA_BANK7:
/*TODO*/ //				case (FPTR)MRA_BANK8:
/*TODO*/ //				case (FPTR)MRA_BANK9:
/*TODO*/ //				case (FPTR)MRA_BANK10:
/*TODO*/ //				case (FPTR)MRA_BANK11:
/*TODO*/ //				case (FPTR)MRA_BANK12:
/*TODO*/ //				case (FPTR)MRA_BANK13:
/*TODO*/ //				case (FPTR)MRA_BANK14:
/*TODO*/ //				case (FPTR)MRA_BANK15:
/*TODO*/ //				case (FPTR)MRA_BANK16:
/*TODO*/ //				{
/*TODO*/ //					hardware = (int)MRA_BANK1 - (int)handler + 1;
/*TODO*/ //					memoryreadoffset[hardware] = bankreadoffset[hardware] = mra->start;
/*TODO*/ //					cpu_bankbase[hardware] = memory_find_base(cpu, mra->start);
/*TODO*/ //					break;
/*TODO*/ //				}
/*TODO*/ //				case (FPTR)MRA_NOP:
/*TODO*/ //					hardware = HT_NOP;
/*TODO*/ //					break;
/*TODO*/ //				default:
/*TODO*/ //					/* create newer hardware handler */
/*TODO*/ //					if( rdhard_max == MH_HARDMAX )
/*TODO*/ //					{
/*TODO*/ //						if (errorlog)
/*TODO*/ //						 fprintf(errorlog,"read memory hardware pattern over !\n");
/*TODO*/ //						hardware = 0;
/*TODO*/ //					}
/*TODO*/ //					else
/*TODO*/ //					{
/*TODO*/ //						/* regist hardware function */
/*TODO*/ //						hardware = rdhard_max++;
/*TODO*/ //						memoryreadhandler[hardware] = handler;
/*TODO*/ //						memoryreadoffset[hardware] = mra->start;
/*TODO*/ //					}
/*TODO*/ //				}
/*TODO*/ //#ifdef SGI_FIX_MWA_NOP
/*TODO*/ //				}
/*TODO*/ //#endif
/*TODO*/ //				/* hardware element table make */
/*TODO*/ //				set_element( cpu , cur_mr_element[cpu] ,
/*TODO*/ //					(((unsigned int) mra->start) >> abitsmin) ,
/*TODO*/ //					(((unsigned int) mra->end) >> abitsmin) ,
/*TODO*/ //					hardware , readhardware , &rdelement_max );
/*TODO*/ //
/*TODO*/ //				mra--;
/*TODO*/ //			}
/*TODO*/ //		}
/*TODO*/ //
/*TODO*/ //		/* memory write handler build */
/*TODO*/ //		if (memorywrite)
/*TODO*/ //		{
/*TODO*/ //			mwa = memorywrite;
/*TODO*/ //			while (mwa->start != -1) mwa++;
/*TODO*/ //			mwa--;
/*TODO*/ //
/*TODO*/ //			while (mwa >= memorywrite)
/*TODO*/ //			{
/*TODO*/ //				mem_write_handler handler = mwa->handler;
/*TODO*/ //#ifdef SGI_FIX_MWA_NOP
/*TODO*/ //				if ((FPTR)handler == (FPTR)MWA_NOP) {
/*TODO*/ //					hardware = HT_NOP;
/*TODO*/ //				} else {
/*TODO*/ //#endif
/*TODO*/ //				switch( (FPTR)handler )
/*TODO*/ //				{
/*TODO*/ //				case (FPTR)MWA_RAM:
/*TODO*/ //					hardware = HT_RAM;	/* sprcial case ram write */
/*TODO*/ //					break;
/*TODO*/ //				case (FPTR)MWA_BANK1:
/*TODO*/ //				case (FPTR)MWA_BANK2:
/*TODO*/ //				case (FPTR)MWA_BANK3:
/*TODO*/ //				case (FPTR)MWA_BANK4:
/*TODO*/ //				case (FPTR)MWA_BANK5:
/*TODO*/ //				case (FPTR)MWA_BANK6:
/*TODO*/ //				case (FPTR)MWA_BANK7:
/*TODO*/ //				case (FPTR)MWA_BANK8:
/*TODO*/ //				case (FPTR)MWA_BANK9:
/*TODO*/ //				case (FPTR)MWA_BANK10:
/*TODO*/ //				case (FPTR)MWA_BANK11:
/*TODO*/ //				case (FPTR)MWA_BANK12:
/*TODO*/ //				case (FPTR)MWA_BANK13:
/*TODO*/ //				case (FPTR)MWA_BANK14:
/*TODO*/ //				case (FPTR)MWA_BANK15:
/*TODO*/ //				case (FPTR)MWA_BANK16:
/*TODO*/ //				{
/*TODO*/ //					hardware = (int)MWA_BANK1 - (int)handler + 1;
/*TODO*/ //					memorywriteoffset[hardware] = bankwriteoffset[hardware] = mwa->start;
/*TODO*/ //					cpu_bankbase[hardware] = memory_find_base(cpu, mwa->start);
/*TODO*/ //					break;
/*TODO*/ //				}
/*TODO*/ //				case (FPTR)MWA_NOP:
/*TODO*/ //					hardware = HT_NOP;
/*TODO*/ //					break;
/*TODO*/ //				case (FPTR)MWA_RAMROM:
/*TODO*/ //					hardware = HT_RAMROM;
/*TODO*/ //					break;
/*TODO*/ //				case (FPTR)MWA_ROM:
/*TODO*/ //					hardware = HT_ROM;
/*TODO*/ //					break;
/*TODO*/ //				default:
/*TODO*/ //					/* create newer hardware handler */
/*TODO*/ //					if( wrhard_max == MH_HARDMAX ){
/*TODO*/ //						if (errorlog)
/*TODO*/ //						 fprintf(errorlog,"write memory hardware pattern over !\n");
/*TODO*/ //						hardware = 0;
/*TODO*/ //					}else{
/*TODO*/ //						/* regist hardware function */
/*TODO*/ //						hardware = wrhard_max++;
/*TODO*/ //						memorywritehandler[hardware] = handler;
/*TODO*/ //						memorywriteoffset[hardware]  = mwa->start;
/*TODO*/ //					}
/*TODO*/ //				}
/*TODO*/ //#ifdef SGI_FIX_MWA_NOP
/*TODO*/ //				}
/*TODO*/ //#endif
/*TODO*/ //				/* hardware element table make */
/*TODO*/ //				set_element( cpu , cur_mw_element[cpu] ,
/*TODO*/ //					(int) (((unsigned int) mwa->start) >> abitsmin) ,
/*TODO*/ //					(int) (((unsigned int) mwa->end) >> abitsmin) ,
/*TODO*/ //					hardware , (MHELE *)writehardware , &wrelement_max );
/*TODO*/ //
/*TODO*/ //				mwa--;
/*TODO*/ //			}
/*TODO*/ //		}
/*TODO*/ //    }
/*TODO*/ //
/*TODO*/ //	if (errorlog){
/*TODO*/ //		fprintf(errorlog,"used read  elements %d/%d , functions %d/%d\n"
/*TODO*/ //		    ,rdelement_max,MH_ELEMAX , rdhard_max,MH_HARDMAX );
/*TODO*/ //		fprintf(errorlog,"used write elements %d/%d , functions %d/%d\n"
/*TODO*/ //		    ,wrelement_max,MH_ELEMAX , wrhard_max,MH_HARDMAX );
/*TODO*/ //	}
/*TODO*/ //#ifdef MEM_DUMP
/*TODO*/ //	mem_dump();
/*TODO*/ //#endif
        return 1;	/* ok */
    }
    /*TODO*/ //
/*TODO*/ //void memory_set_opcode_base(int cpu,unsigned char *base)
/*TODO*/ //{
/*TODO*/ //	romptr[cpu] = base;
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ //
/*TODO*/ //void memorycontextswap(int activecpu)
/*TODO*/ //{
/*TODO*/ //	cpu_bankbase[0] = ramptr[activecpu];
/*TODO*/ //
/*TODO*/ //	cur_mrhard = cur_mr_element[activecpu];
/*TODO*/ //	cur_mwhard = cur_mw_element[activecpu];
/*TODO*/ //
/*TODO*/ //	/* ASG: port speedup */
/*TODO*/ //	cur_readport = readport[activecpu];
/*TODO*/ //	cur_writeport = writeport[activecpu];
/*TODO*/ //	cur_portmask = portmask[activecpu];
/*TODO*/ //
/*TODO*/ //	OPbasefunc = setOPbasefunc[activecpu];
/*TODO*/ //
/*TODO*/ //	/* op code memory pointer */
/*TODO*/ //	ophw = HT_RAM;
/*TODO*/ //	OP_RAM = cpu_bankbase[0];
/*TODO*/ //	OP_ROM = romptr[activecpu];
/*TODO*/ //}
/*TODO*/ //

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
/*TODO*/ //#define WRITEBYTE(name,type,abits)														\
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
/*TODO*/ //
/*TODO*/ ///***************************************************************************
/*TODO*/ //
/*TODO*/ //  Perform an I/O port read. This function is called by the CPU emulation.
/*TODO*/ //
/*TODO*/ //***************************************************************************/
/*TODO*/ //int cpu_readport(int port)
/*TODO*/ //{
/*TODO*/ //	const struct IOReadPort *iorp = cur_readport;
/*TODO*/ //
/*TODO*/ //	port &= cur_portmask;
/*TODO*/ //
/*TODO*/ //	/* search the handlers. The order is as follows: first the dynamically installed
/*TODO*/ //	   handlers are searched, followed by the static ones in whatever order they were
/*TODO*/ //	   specified in the driver */
/*TODO*/ //	while (iorp->start != -1)
/*TODO*/ //	{
/*TODO*/ //		if (port >= iorp->start && port <= iorp->end)
/*TODO*/ //		{
/*TODO*/ //			mem_read_handler handler = iorp->handler;
/*TODO*/ //
/*TODO*/ //
/*TODO*/ //			if (handler == IORP_NOP) return 0;
/*TODO*/ //			else return (*handler)(port - iorp->start);
/*TODO*/ //		}
/*TODO*/ //
/*TODO*/ //		iorp++;
/*TODO*/ //	}
/*TODO*/ //
/*TODO*/ //	if (errorlog) fprintf(errorlog,"CPU #%d PC %04x: warning - read unmapped I/O port %02x\n",cpu_getactivecpu(),cpu_get_pc(),port);
/*TODO*/ //	return 0;
/*TODO*/ //}
/*TODO*/ //
/*TODO*/ //
/*TODO*/ ///***************************************************************************
/*TODO*/ //
/*TODO*/ //  Perform an I/O port write. This function is called by the CPU emulation.
/*TODO*/ //
/*TODO*/ //***************************************************************************/
/*TODO*/ //void cpu_writeport(int port, int value)
/*TODO*/ //{
/*TODO*/ //	const struct IOWritePort *iowp = cur_writeport;
/*TODO*/ //
/*TODO*/ //	port &= cur_portmask;
/*TODO*/ //
/*TODO*/ //	/* search the handlers. The order is as follows: first the dynamically installed
/*TODO*/ //	   handlers are searched, followed by the static ones in whatever order they were
/*TODO*/ //	   specified in the driver */
/*TODO*/ //	while (iowp->start != -1)
/*TODO*/ //	{
/*TODO*/ //		if (port >= iowp->start && port <= iowp->end)
/*TODO*/ //		{
/*TODO*/ //			mem_write_handler handler = iowp->handler;
/*TODO*/ //
/*TODO*/ //
/*TODO*/ //			if (handler == IOWP_NOP) return;
/*TODO*/ //			else (*handler)(port - iowp->start,value);
/*TODO*/ //
/*TODO*/ //			return;
/*TODO*/ //		}
/*TODO*/ //
/*TODO*/ //		iowp++;
/*TODO*/ //	}
/*TODO*/ //
/*TODO*/ //	if (errorlog) fprintf(errorlog,"CPU #%d PC %04x: warning - write %02x to unmapped I/O port %02x\n",cpu_getactivecpu(),cpu_get_pc(),value,port);
/*TODO*/ //}
/*TODO*/ //
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
/*TODO*/ //void *install_mem_read_handler(int cpu, int start, int end, mem_read_handler handler)
/*TODO*/ //{
/*TODO*/ //	MHELE hardware = 0;
/*TODO*/ //	int abitsmin;
/*TODO*/ //	int i, hw_set;
/*TODO*/ //	if (errorlog) fprintf(errorlog, "Install new memory read handler:\n");
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
/*TODO*/ //		if (( memoryreadhandler[i] == handler ) &&
/*TODO*/ //			(  memoryreadoffset[i] == start))
/*TODO*/ //		{
/*TODO*/ //			if (errorlog) fprintf(errorlog,"handler match - use old one\n");
/*TODO*/ //			hardware = i;
/*TODO*/ //			hw_set = 1;
/*TODO*/ //		}
/*TODO*/ //	}
/*TODO*/ //	switch ((FPTR)handler)
/*TODO*/ //	{
/*TODO*/ //		case (FPTR)MRA_RAM:
/*TODO*/ //		case (FPTR)MRA_ROM:
/*TODO*/ //			hardware = HT_RAM;	/* sprcial case ram read */
/*TODO*/ //			hw_set = 1;
/*TODO*/ //			break;
/*TODO*/ //		case (FPTR)MRA_BANK1:
/*TODO*/ //		case (FPTR)MRA_BANK2:
/*TODO*/ //		case (FPTR)MRA_BANK3:
/*TODO*/ //		case (FPTR)MRA_BANK4:
/*TODO*/ //		case (FPTR)MRA_BANK5:
/*TODO*/ //		case (FPTR)MRA_BANK6:
/*TODO*/ //		case (FPTR)MRA_BANK7:
/*TODO*/ //		case (FPTR)MRA_BANK8:
/*TODO*/ //		case (FPTR)MRA_BANK9:
/*TODO*/ //		case (FPTR)MRA_BANK10:
/*TODO*/ //		case (FPTR)MRA_BANK11:
/*TODO*/ //		case (FPTR)MRA_BANK12:
/*TODO*/ //		case (FPTR)MRA_BANK13:
/*TODO*/ //		case (FPTR)MRA_BANK14:
/*TODO*/ //		case (FPTR)MRA_BANK15:
/*TODO*/ //		case (FPTR)MRA_BANK16:
/*TODO*/ //		{
/*TODO*/ //			hardware = (int)MRA_BANK1 - (int)handler + 1;
/*TODO*/ //			hw_set = 1;
/*TODO*/ //			break;
/*TODO*/ //		}
/*TODO*/ //		case (FPTR)MRA_NOP:
/*TODO*/ //			hardware = HT_NOP;
/*TODO*/ //			hw_set = 1;
/*TODO*/ //			break;
/*TODO*/ //	}
/*TODO*/ //	if (!hw_set)  /* no match */
/*TODO*/ //	{
/*TODO*/ //		/* create newer hardware handler */
/*TODO*/ //		if( rdhard_max == MH_HARDMAX )
/*TODO*/ //		{
/*TODO*/ //			if (errorlog) fprintf(errorlog, "read memory hardware pattern over !\n");
/*TODO*/ //			if (errorlog) fprintf(errorlog, "Failed to install new memory handler.\n");
/*TODO*/ //			return memory_find_base(cpu, start);
/*TODO*/ //		}
/*TODO*/ //		else
/*TODO*/ //		{
/*TODO*/ //			/* register hardware function */
/*TODO*/ //			hardware = rdhard_max++;
/*TODO*/ //			memoryreadhandler[hardware] = handler;
/*TODO*/ //			memoryreadoffset[hardware] = start;
/*TODO*/ //		}
/*TODO*/ //	}
/*TODO*/ //	/* set hardware element table entry */
/*TODO*/ //	set_element( cpu , cur_mr_element[cpu] ,
/*TODO*/ //		(((unsigned int) start) >> abitsmin) ,
/*TODO*/ //		(((unsigned int) end) >> abitsmin) ,
/*TODO*/ //		hardware , readhardware , &rdelement_max );
/*TODO*/ //	if (errorlog) fprintf(errorlog, "Done installing new memory handler.\n");
/*TODO*/ //	if (errorlog){
/*TODO*/ //		fprintf(errorlog,"used read  elements %d/%d , functions %d/%d\n"
/*TODO*/ //		    ,rdelement_max,MH_ELEMAX , rdhard_max,MH_HARDMAX );
/*TODO*/ //	}
/*TODO*/ //	return memory_find_base(cpu, start);
/*TODO*/ //}
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

    public static IOReadPort[] install_port_read_handler_common(int cpu, int start, int end, ReadHandlerPtr handler, int install_at_beginning) {
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
        readport[cpu][i]._handler = handler;

        return readport[cpu];
    }

    public static IOWritePort[] install_port_write_handler_common(int cpu, int start, int end, WriteHandlerPtr handler, int install_at_beginning) {
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
        writeport[cpu][i]._handler = handler;

        return writeport[cpu];

    }

    public static void mem_dump() {
        /*TODO*/ //	extern int totalcpu;
        int cpu;
        int naddr, addr;
        /*TODO*/ //MHELE nhw,hw;
        /*TODO*/ //
/*TODO*/ //	FILE *temp = fopen ("memdump.log", "w");
/*TODO*/ //
/*TODO*/ //	if (!temp) return;
/*TODO*/ //
/*TODO*/ //	for( cpu = 0 ; cpu < 1 ; cpu++ )
/*TODO*/ //	{
/*TODO*/ //		fprintf(temp,"cpu %d read memory \n",cpu);
/*TODO*/ //		addr = 0;
/*TODO*/ //		naddr = 0;
/*TODO*/ //		nhw = 0xff;
/*TODO*/ //		while( (addr >> mhshift[cpu][0]) <= mhmask[cpu][0] ){
/*TODO*/ //			hw = cur_mr_element[cpu][addr >> mhshift[cpu][0]];
/*TODO*/ //			if( hw >= MH_HARDMAX )
/*TODO*/ //			{	/* 2nd element link */
/*TODO*/ //				hw = readhardware[((hw-MH_HARDMAX)<<MH_SBITS) + ((addr>>mhshift[cpu][1]) & mhmask[cpu][1])];
/*TODO*/ //				if( hw >= MH_HARDMAX )
/*TODO*/ //					hw = readhardware[((hw-MH_HARDMAX)<<MH_SBITS) + (addr & mhmask[cpu][2])];
/*TODO*/ //			}
/*TODO*/ //			if( nhw != hw )
/*TODO*/ //			{
/*TODO*/ //				if( addr )
/*TODO*/ //	fprintf(temp,"  %08x(%08x) - %08x = %02x\n",naddr,memoryreadoffset[nhw],addr-1,nhw);
/*TODO*/ //				nhw = hw;
/*TODO*/ //				naddr = addr;
/*TODO*/ //			}
/*TODO*/ //			addr++;
/*TODO*/ //		}
/*TODO*/ //		fprintf(temp,"  %08x(%08x) - %08x = %02x\n",naddr,memoryreadoffset[nhw],addr-1,nhw);
/*TODO*/ //
/*TODO*/ //		fprintf(temp,"cpu %d write memory \n",cpu);
/*TODO*/ //		naddr = 0;
/*TODO*/ //		addr = 0;
/*TODO*/ //		nhw = 0xff;
/*TODO*/ //		while( (addr >> mhshift[cpu][0]) <= mhmask[cpu][0] ){
/*TODO*/ //			hw = cur_mw_element[cpu][addr >> mhshift[cpu][0]];
/*TODO*/ //			if( hw >= MH_HARDMAX )
/*TODO*/ //			{	/* 2nd element link */
/*TODO*/ //				hw = writehardware[((hw-MH_HARDMAX)<<MH_SBITS) + ((addr>>mhshift[cpu][1]) & mhmask[cpu][1])];
/*TODO*/ //				if( hw >= MH_HARDMAX )
/*TODO*/ //					hw = writehardware[((hw-MH_HARDMAX)<<MH_SBITS) + (addr & mhmask[cpu][2])];
/*TODO*/ //			}
/*TODO*/ //			if( nhw != hw )
/*TODO*/ //			{
/*TODO*/ //				if( addr )
/*TODO*/ //	fprintf(temp,"  %08x(%08x) - %08x = %02x\n",naddr,memorywriteoffset[nhw],addr-1,nhw);
/*TODO*/ //				nhw = hw;
/*TODO*/ //				naddr = addr;
/*TODO*/ //			}
/*TODO*/ //			addr++;
/*TODO*/ //		}
/*TODO*/ //	fprintf(temp,"  %08x(%08x) - %08x = %02x\n",naddr,memorywriteoffset[nhw],addr-1,nhw);
/*TODO*/ //	}
/*TODO*/ //	fclose(temp);
    }
    /*TODO*/ //#endif
/*TODO*/ //
/*TODO*/ //
}
