package gr.codebb.arcadeflex.v036.mame;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import java.util.Arrays;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class memory {

    public static ReadHandlerPtr mrh_rom = new ReadHandlerPtr() {//fake??
        public int handler(int offset) {
            return cpu_bankbase[0].read(offset);
        }
    };

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

 

    public static int cpu_readmem20(int address) {
        UByte hw = new UByte();

        /* first-level lookup */
        hw.set(u8_cur_mrhard[address >>> (ABITS2_20 + ABITS_MIN_20)]);

        /* for compatibility with setbankhandler, 8-bit systems must call handlers */
        /* for banked memory reads/writes */
        if (hw.read() == HT_RAM) {
            return cpu_bankbase[HT_RAM].memory[cpu_bankbase[HT_RAM].offset + address];
        }

        /* second-level lookup */
        if (hw.read() >= MH_HARDMAX) {
            hw.set((char) (hw.read() - MH_HARDMAX));
            hw.set(u8_readhardware[(hw.read() << MH_SBITS) + ((address >>> ABITS_MIN_20) & MHMASK(ABITS2_20))]);

            /* for compatibility with setbankhandler, 8-bit systems must call handlers */
            /* for banked memory reads/writes */
            if (hw.read() == HT_RAM) {
                return cpu_bankbase[HT_RAM].read(address);
            }
        }
        /* fall back to handler */

        return memoryreadhandler[hw.read()].handler(address - memoryreadoffset[hw.read()]);
    }

    public static int cpu_readmem24(int address)																	
{																						
	UByte hw = new UByte();																			
																						
	/* first-level lookup */															
	hw.set(u8_cur_mrhard[address >>> (ABITS2_24 + ABITS_MIN_24)]);			
																																
	if (hw.read() <= HT_BANKMAX)										
	{																																		
           return cpu_bankbase[hw.read()].memory[BYTE_XOR_BE(address) - memoryreadoffset[hw.read()]];			
	}																					
																						
	/* second-level lookup */															
	if (hw.read() >= MH_HARDMAX)																
	{																					
		hw.set((char) (hw.read() - MH_HARDMAX));																
		hw.set(u8_readhardware[(hw.read() << MH_SBITS) + ((address >>> ABITS_MIN_24) & MHMASK(ABITS2_24))]);	
																																
		if (hw.read() <= HT_BANKMAX)									
		{																																
				return cpu_bankbase[hw.read()].memory[BYTE_XOR_BE(address) - memoryreadoffset[hw.read()]];	
		}																				
	}																																									
		int shift = (address & 1) << 3;													
		int data = (memoryreadhandler[hw.read()]).handler((address & ~1) - memoryreadoffset[hw.read()]);																
		return (data >> (shift ^ 8)) & 0xff;																															
}
    /*TODO*/ ///* generic byte-sized read handler */
/*TODO*/ //#define READBYTE(name,type,abits)														\
/*TODO*/ //int name(int address)																	\
/*TODO*/ //{																						\
/*TODO*/ //	MHELE hw;																			\
/*TODO*/ //																						\
/*TODO*/ //	/* first-level lookup */															\
/*TODO*/ //	hw = u8_cur_mrhard[(UINT32)address >> (ABITS2_##abits + ABITS_MIN_##abits)];			\
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
/*TODO*/ //		hw = u8_readhardware[(hw << MH_SBITS) + (((UINT32)address >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
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

    public static int cpu_readmem24_word(int address) {
        UByte hw = new UByte();	
        /* handle aligned case first */
        if ((address & 1) == 0) {
            /* first-level lookup */				
                hw.set(u8_cur_mrhard[address >>> (ABITS2_24 + ABITS_MIN_24)]);		
		if (hw.read() <= HT_BANKMAX)															
			return cpu_bankbase[hw.read()].READ_WORD(address - memoryreadoffset[hw.read()]);		
																						
		/* second-level lookup */														
		if (hw.read() >= MH_HARDMAX)															
		{																				
			hw.set((char) (hw.read() - MH_HARDMAX));															
			hw.set(u8_readhardware[(hw.read() << MH_SBITS) + ((address >>> ABITS_MIN_24) & MHMASK(ABITS2_24))]);	
			if (hw.read() <= HT_BANKMAX)														
				return cpu_bankbase[hw.read()].READ_WORD(address - memoryreadoffset[hw.read()]);	
		}																				
																						
		/* fall back to handler */														
		return (memoryreadhandler[hw.read()]).handler(address - memoryreadoffset[hw.read()]);	
        } /* unaligned case */ else {
            int data = cpu_readmem24(address) << 8;
            return data | (cpu_readmem24(address + 1) & 0xff);
        }

    }
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
/*TODO*/ //		hw = u8_cur_mrhard[(UINT32)address >> (ABITS2_##abits + ABITS_MIN_##abits)];		\
/*TODO*/ //		if (hw <= HT_BANKMAX)															\
/*TODO*/ //			return READ_WORD(&cpu_bankbase[hw][address - memoryreadoffset[hw]]);		\
/*TODO*/ //																						\
/*TODO*/ //		/* second-level lookup */														\
/*TODO*/ //		if (hw >= MH_HARDMAX)															\
/*TODO*/ //		{																				\
/*TODO*/ //			hw -= MH_HARDMAX;															\
/*TODO*/ //			hw = u8_readhardware[(hw << MH_SBITS) + (((UINT32)address >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
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
/*TODO*/ //#define READLONG(name,type,abits,align)			

    public static int cpu_readmem24_dword(int address) {
        int word1, word2;																
	UByte hw1 = new UByte();	
        UByte hw2 = new UByte();	
        if (/*align == ALWAYS_ALIGNED || */(address & 1) == 0) {
            //int address2 = (address + 2) & ADDRESS_MASK(24);								
	int address2 = (int)((address + 2) & ((1 << (ABITS1_24 + ABITS2_24 + ABITS_MIN_24 - 1)) | ((1 << (ABITS1_24 + ABITS2_24 + ABITS_MIN_24 - 1)) - 1)));																				
		/* first-level lookup */														
		hw1.set(u8_cur_mrhard[address >> (ABITS2_24 + ABITS_MIN_24)]);		
		hw2.set(u8_cur_mrhard[address2 >> (ABITS2_24 + ABITS_MIN_24)]);		
																						
		/* second-level lookup */														
		if (hw1.read() >= MH_HARDMAX)															
		{																				
			hw1.set((char) (hw1.read() - MH_HARDMAX));															
			hw1.set(u8_readhardware[(hw1.read() << MH_SBITS) + ((address >>> ABITS_MIN_24) & MHMASK(ABITS2_24))]);	
		}																				
		if (hw2.read() >= MH_HARDMAX)															
		{																				
			hw2.set((char) (hw2.read() - MH_HARDMAX));															
			hw2.set(u8_readhardware[(hw2.read() << MH_SBITS) + ((address2 >> ABITS_MIN_24) & MHMASK(ABITS2_24))]);	
		}																				
																						
		/* process each word */															
		if (hw1.read() <= HT_BANKMAX)															
			word1 = cpu_bankbase[hw1.read()].READ_WORD(address - memoryreadoffset[hw1.read()]);		
		else																			
			word1 = (memoryreadhandler[hw1.read()]).handler(address - memoryreadoffset[hw1.read()]);			
		if (hw2.read() <= HT_BANKMAX)															
			word2 = cpu_bankbase[hw2.read()].READ_WORD(address2 - memoryreadoffset[hw2.read()]);	
		else																			
			word2 = (memoryreadhandler[hw2.read()]).handler(address2 - memoryreadoffset[hw2.read()]);		
																						
		/* fall back to handler */																												
			return ((word1 << 16)) | (word2 & 0xffff);	
        } else {
            int data = cpu_readmem24(address) << 24;
            data |= cpu_readmem24_word(address + 1) << 8;
            return data | (cpu_readmem24(address + 3) & 0xff);
        }

    }
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
/*TODO*/ //		hw1 = u8_cur_mrhard[(UINT32)address >> (ABITS2_##abits + ABITS_MIN_##abits)];		\
/*TODO*/ //		hw2 = u8_cur_mrhard[(UINT32)address2 >> (ABITS2_##abits + ABITS_MIN_##abits)];		\
/*TODO*/ //																						\
/*TODO*/ //		/* second-level lookup */														\
/*TODO*/ //		if (hw1 >= MH_HARDMAX)															\
/*TODO*/ //		{																				\
/*TODO*/ //			hw1 -= MH_HARDMAX;															\
/*TODO*/ //			hw1 = u8_readhardware[(hw1 << MH_SBITS) + (((UINT32)address >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
/*TODO*/ //		}																				\
/*TODO*/ //		if (hw2 >= MH_HARDMAX)															\
/*TODO*/ //		{																				\
/*TODO*/ //			hw2 -= MH_HARDMAX;															\
/*TODO*/ //			hw2 = u8_readhardware[(hw2 << MH_SBITS) + (((UINT32)address2 >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
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


    public static void cpu_writemem20(int address, int data) {
        /* first-level lookup */
        UByte hw = new UByte();
        hw.set(u8_cur_mwhard[address >>> (ABITS2_20 + ABITS_MIN_20)]);

        /* for compatibility with setbankhandler, 8-bit systems must call handlers */
        /* for banked memory reads/writes */
        if (hw.read() == HT_RAM) {
            cpu_bankbase[HT_RAM].memory[cpu_bankbase[HT_RAM].offset + address] = (char) data;
            return;
        }

        /* second-level lookup */
        if (hw.read() >= MH_HARDMAX) {
            hw.set((char) (hw.read() - MH_HARDMAX));
            hw.set(u8_writehardware[(hw.read() << MH_SBITS) + ((address >>> ABITS_MIN_20) & MHMASK(ABITS2_20))]);
            /* for compatibility with setbankhandler, 8-bit systems must call handlers */
            /* for banked memory reads/writes */
            if (hw.read() == HT_RAM) {
                cpu_bankbase[HT_RAM].write(address, data);
                return;
            }
        }

        memorywritehandler[hw.read()].handler(address - memorywriteoffset[hw.read()], data);
    }

    public static void cpu_writemem24(int address, int data)														
    {																						
            UByte hw = new UByte();																			

            /* first-level lookup */															
            hw.set(u8_cur_mwhard[address >>> (ABITS2_24 + ABITS_MIN_24)]);
            if (hw.read() <= HT_BANKMAX)										
            {																																		
                            cpu_bankbase[hw.read()].memory[BYTE_XOR_BE(address) - memorywriteoffset[hw.read()]] = (char)data;				
                    return;																			
            }																					

            /* second-level lookup */															
            if (hw.read() >= MH_HARDMAX)																
            {																					
                    hw.set((char) (hw.read() - MH_HARDMAX));	
                    hw.set(u8_writehardware[(hw.read() << MH_SBITS) + ((address >>> ABITS_MIN_24) & MHMASK(ABITS2_24))]);
                    	

                    if (hw.read() <= HT_BANKMAX)									
                    {																																	
                            cpu_bankbase[hw.read()].memory[BYTE_XOR_BE(address) - memorywriteoffset[hw.read()]] = (char)data;	
                            return;																		
                    }																				
            }																					

            /* fall back to handler */																																			
                    int shift = (address & 1) << 3;													
                    shift ^= 8;																	
                    data = (0xff000000 >>> shift) | ((data & 0xff) << shift);	//unsigned??					
                    address &= ~1;																																					
            (memorywritehandler[hw.read()]).handler(address - memorywriteoffset[hw.read()], data);					
    }
    public static void cpu_writemem24_word(int address, int data)													
    {																						
            UByte hw = new UByte();																			

            /* handle aligned case first */														
            if ((address & 1)==0)										
            {																					
                    /* first-level lookup */														
                    hw.set(u8_cur_mwhard[address >>> (ABITS2_24 + ABITS_MIN_24)]);		
                    if (hw.read() <= HT_BANKMAX)															
                    {																				
                            cpu_bankbase[hw.read()].WRITE_WORD(address - memorywriteoffset[hw.read()], data);		
                            return;																		
                    }																				

                    /* second-level lookup */														
                    if (hw.read() >= MH_HARDMAX)															
                    {																				
                            hw.set((char) (hw.read() - MH_HARDMAX));															
                            hw.set(u8_writehardware[(hw.read() << MH_SBITS) + ((address >>> ABITS_MIN_24) & MHMASK(ABITS2_24))]); 
                            if (hw.read() <= HT_BANKMAX)														
                            {																			
                                    cpu_bankbase[hw.read()].WRITE_WORD(address - memorywriteoffset[hw.read()], data);	
                                    return;																	
                            }																			
                    }																				

                    /* fall back to handler */														
                    (memorywritehandler[hw.read()]).handler(address - memorywriteoffset[hw.read()], data & 0xffff);		
            }																					

            /* unaligned case */																
            else													
            {																					
                    cpu_writemem24(address, data >> 8);														
                    cpu_writemem24(address + 1, data & 0xff);													
            }																																									
    }
    public static void cpu_writemem24_dword(int address, int data)												
{																						
	int word1, word2;																
	UByte hw1 = new UByte();	
        UByte hw2 = new UByte();																	
																																											
	/* handle aligned case first */														
	if ((address & 1)==0)										
	{																							
                //int address2 = (address + 2) & ADDRESS_MASK(24);								
                int address2 = (int)((address + 2) & ((1 << (ABITS1_24 + ABITS2_24 + ABITS_MIN_24 - 1)) | ((1 << (ABITS1_24 + ABITS2_24 + ABITS_MIN_24 - 1)) - 1)));																				

																						
		/* first-level lookup */
                hw1.set(u8_cur_mwhard[address >>> (ABITS2_24 + ABITS_MIN_24)]);		
		hw2.set(u8_cur_mwhard[address2 >>> (ABITS2_24 + ABITS_MIN_24)]);
		
																						
		/* second-level lookup */	
                if (hw1.read() >= MH_HARDMAX)															
		{																				
			hw1.set((char) (hw1.read() - MH_HARDMAX));															
			hw1.set(u8_writehardware[(hw1.read() << MH_SBITS) + ((address >>> ABITS_MIN_24) & MHMASK(ABITS2_24))]);	
		}																				
		if (hw2.read() >= MH_HARDMAX)															
		{																				
			hw2.set((char) (hw2.read() - MH_HARDMAX));															
			hw2.set(u8_writehardware[(hw2.read() << MH_SBITS) + ((address2 >>> ABITS_MIN_24) & MHMASK(ABITS2_24))]);	
		}																				
		/* extract words */																
																			
			word1 = (data >> 16); //should be unsigned??															
			word2 = data & 0xffff;														
																
																						
		/* process each word */															
		if (hw1.read() <= HT_BANKMAX)															
			cpu_bankbase[hw1.read()].WRITE_WORD(address - memorywriteoffset[hw1.read()], word1);	
		else																			
			(memorywritehandler[hw1.read()]).handler(address - memorywriteoffset[hw1.read()], word1);		
		if (hw2.read() <= HT_BANKMAX)															
			cpu_bankbase[hw2.read()].WRITE_WORD(address2 - memorywriteoffset[hw2.read()], word2);	
		else																			
			(memorywritehandler[hw2.read()]).handler(address2 - memorywriteoffset[hw2.read()], word2);		
	}																					
																						
	/* unaligned case */																
	else													
	{																					
		cpu_writemem24(address, (data >> 24));														
		cpu_writemem24_word(address + 1, (data >> 8) & 0xffff);									
		cpu_writemem24(address + 3, data & 0xff);													
	}																					
																					
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
/*TODO*/ //	hw = u8_cur_mrhard[(UINT32)pc >> (ABITS2_##abits + ABITS_MIN_##abits)];				\
/*TODO*/ //	if (hw >= MH_HARDMAX)																\
/*TODO*/ //	{																					\
/*TODO*/ //		hw -= MH_HARDMAX;																\
/*TODO*/ //		hw = u8_readhardware[(hw << MH_SBITS) + (((UINT32)pc >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
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

    public static setopbase cpu_setOPbase20 = new setopbase() {
        public void handler(int pc) {
            UByte hw = new UByte();


            /* allow overrides */
            if (OPbasefunc != null) {
                pc = (int) OPbasefunc.handler((int) pc);
                if (pc == -1) {
                    return;
                }
            }

            /* perform the lookup */
            hw.set(u8_cur_mrhard[pc >>> (ABITS2_20 + ABITS_MIN_20)]);
            if (hw.read() >= MH_HARDMAX) {
                hw.set((char) (hw.read() - MH_HARDMAX));
                hw.set(u8_readhardware[(hw.read() << MH_SBITS) + ((pc >>> ABITS_MIN_20) & MHMASK(ABITS2_20))]);
            }
            u8_ophw = (char) (hw.read() & 0xFF);

            /* RAM or banked memory */
            if (hw.read() <= HT_BANKMAX) {
                SET_OP_RAMROM(new UBytePtr(cpu_bankbase[hw.read()], (-memoryreadoffset[hw.read()])));
                return;
            }

            /* do not support on callback memory region */
            printf("CPU #%d PC %04x: warning - op-code execute on mapped i/o\n", cpu_getactivecpu(), cpu_get_pc());
        }
    };
  
    public static setopbase cpu_setOPbase24 = new setopbase() {
        public void handler(int pc) {
            UByte hw = new UByte();

            /* allow overrides */
            if (OPbasefunc != null) {
                pc = (int) OPbasefunc.handler((int) pc);
                if (pc == -1) {
                    return;
                }
            }

            /* perform the lookup */
            hw.set(u8_cur_mrhard[pc >>> (ABITS2_24 + ABITS_MIN_24)]);
            if (hw.read() >= MH_HARDMAX) {
                hw.set((char) (hw.read() - MH_HARDMAX));
                hw.set(u8_readhardware[(hw.read() << MH_SBITS) + ((pc >>> ABITS_MIN_24) & MHMASK(ABITS2_24))]);
            }
            u8_ophw = (char) (hw.read() & 0xFF);

            /* RAM or banked memory */
            if (hw.read() <= HT_BANKMAX) {
                SET_OP_RAMROM(new UBytePtr(cpu_bankbase[hw.read()], (-memoryreadoffset[hw.read()])));
                return;
            }

            /* do not support on callback memory region */
            printf("CPU #%d PC %04x: warning - op-code execute on mapped i/o\n", cpu_getactivecpu(), cpu_get_pc());
        }
    };

    /**
     * *************************************************************************
     *
     * Perform an I/O port read. This function is called by the CPU emulation.
     *
     **************************************************************************
     */

    /*TODO*/ //
/*TODO*/ ///* set readmemory handler for bank memory  */
    public static void cpu_setbankhandler_r(int bank, ReadHandlerPtr _handler) {
        int offset = 0;
        UByte hardware = new UByte();

        if (_handler == mrh_ram || _handler == mrh_rom) {
            _handler = mrh_ram;
        } else if (_handler == mrh_bank1) {
            hardware.set((char) 1);
            _handler = bank_read_handler[hardware.read()];
            offset = bankreadoffset[hardware.read()];
        } else if (_handler == mrh_bank2) {
            hardware.set((char) 2);
            _handler = bank_read_handler[hardware.read()];
            offset = bankreadoffset[hardware.read()];
        } else if (_handler == mrh_bank3) {
            hardware.set((char) 3);
            _handler = bank_read_handler[hardware.read()];
            offset = bankreadoffset[hardware.read()];
        } else if (_handler == mrh_bank4) {
            hardware.set((char) 4);
            _handler = bank_read_handler[hardware.read()];
            offset = bankreadoffset[hardware.read()];
        } else if (_handler == mrh_bank5) {
            hardware.set((char) 5);
            _handler = bank_read_handler[hardware.read()];
            offset = bankreadoffset[hardware.read()];
        } else if (_handler == mrh_bank6) {
            hardware.set((char) 6);
            _handler = bank_read_handler[hardware.read()];
            offset = bankreadoffset[hardware.read()];
        } else if (_handler == mrh_bank7) {
            hardware.set((char) 7);
            _handler = bank_read_handler[hardware.read()];
            offset = bankreadoffset[hardware.read()];
        } else if (_handler == mrh_bank8) {
            hardware.set((char) 8);
            _handler = bank_read_handler[hardware.read()];
            offset = bankreadoffset[hardware.read()];
        } else if (_handler == mrh_bank9) {
            hardware.set((char) 9);
            _handler = bank_read_handler[hardware.read()];
            offset = bankreadoffset[hardware.read()];
        } else if (_handler == mrh_bank10) {
            hardware.set((char) 10);
            _handler = bank_read_handler[hardware.read()];
            offset = bankreadoffset[hardware.read()];
        } else if (_handler == mrh_bank11) {
            hardware.set((char) 11);
            _handler = bank_read_handler[hardware.read()];
            offset = bankreadoffset[hardware.read()];
        } else if (_handler == mrh_bank12) {
            hardware.set((char) 12);
            _handler = bank_read_handler[hardware.read()];
            offset = bankreadoffset[hardware.read()];
        } else if (_handler == mrh_bank13) {
            hardware.set((char) 13);
            _handler = bank_read_handler[hardware.read()];
            offset = bankreadoffset[hardware.read()];
        } else if (_handler == mrh_bank14) {
            hardware.set((char) 14);
            _handler = bank_read_handler[hardware.read()];
            offset = bankreadoffset[hardware.read()];
        } else if (_handler == mrh_bank15) {
            hardware.set((char) 15);
            _handler = bank_read_handler[hardware.read()];
            offset = bankreadoffset[hardware.read()];
        } else if (_handler == mrh_bank16) {
            hardware.set((char) 16);
            _handler = bank_read_handler[hardware.read()];
            offset = bankreadoffset[hardware.read()];
        } else {
            offset = bankreadoffset[bank];
        }

        memoryreadoffset[bank] = offset;
        memoryreadhandler[bank] = _handler;
    }
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

    public static void cpu_setbankhandler_w(int bank, WriteHandlerPtr _handler) {
        int offset = 0;
        UByte hardware = new UByte();

        if (_handler == mwh_ram) {
            _handler = mwh_ram;
        } else if (_handler == mwh_ramrom) {
            _handler = mwh_ramrom;
        } else if (_handler == mwh_rom) {
            _handler = mwh_rom;
        } else if (_handler == mwh_nop) {
            _handler = mwh_nop;
        } else if (_handler == mwh_bank1) {
            hardware.set((char) 1);
            _handler = bank_write_handler[hardware.read()];
            offset = bankwriteoffset[hardware.read()];
        } else if (_handler == mwh_bank2) {
            hardware.set((char) 2);
            _handler = bank_write_handler[hardware.read()];
            offset = bankwriteoffset[hardware.read()];
        } else if (_handler == mwh_bank3) {
            hardware.set((char) 3);
            _handler = bank_write_handler[hardware.read()];
            offset = bankwriteoffset[hardware.read()];
        } else if (_handler == mwh_bank4) {
            hardware.set((char) 4);
            _handler = bank_write_handler[hardware.read()];
            offset = bankwriteoffset[hardware.read()];
        } else if (_handler == mwh_bank5) {
            hardware.set((char) 5);
            _handler = bank_write_handler[hardware.read()];
            offset = bankwriteoffset[hardware.read()];
        } else if (_handler == mwh_bank6) {
            hardware.set((char) 6);
            _handler = bank_write_handler[hardware.read()];
            offset = bankwriteoffset[hardware.read()];
        } else if (_handler == mwh_bank7) {
            hardware.set((char) 7);
            _handler = bank_write_handler[hardware.read()];
            offset = bankwriteoffset[hardware.read()];
        } else if (_handler == mwh_bank8) {
            hardware.set((char) 8);
            _handler = bank_write_handler[hardware.read()];
            offset = bankwriteoffset[hardware.read()];
        } else if (_handler == mwh_bank9) {
            hardware.set((char) 9);
            _handler = bank_write_handler[hardware.read()];
            offset = bankwriteoffset[hardware.read()];
        } else if (_handler == mwh_bank10) {
            hardware.set((char) 10);
            _handler = bank_write_handler[hardware.read()];
            offset = bankwriteoffset[hardware.read()];
        } else if (_handler == mwh_bank11) {
            hardware.set((char) 11);
            _handler = bank_write_handler[hardware.read()];
            offset = bankwriteoffset[hardware.read()];
        } else if (_handler == mwh_bank12) {
            hardware.set((char) 12);
            _handler = bank_write_handler[hardware.read()];
            offset = bankwriteoffset[hardware.read()];
        } else if (_handler == mwh_bank13) {
            hardware.set((char) 13);
            _handler = bank_write_handler[hardware.read()];
            offset = bankwriteoffset[hardware.read()];
        } else if (_handler == mwh_bank14) {
            hardware.set((char) 14);
            _handler = bank_write_handler[hardware.read()];
            offset = bankwriteoffset[hardware.read()];
        } else if (_handler == mwh_bank15) {
            hardware.set((char) 15);
            _handler = bank_write_handler[hardware.read()];
            offset = bankwriteoffset[hardware.read()];
        } else if (_handler == mwh_bank16) {
            hardware.set((char) 16);
            _handler = bank_write_handler[hardware.read()];
            offset = bankwriteoffset[hardware.read()];
        } else {
            offset = bankwriteoffset[bank];
        }

        memorywriteoffset[bank] = offset;
        memorywritehandler[bank] = _handler;

    }
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

}
