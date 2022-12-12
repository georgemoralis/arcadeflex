/*** TMS34010: Portable TMS34010 emulator ************************************

	Copyright (C) Alex Pasadyn/Zsolt Vasvari 1998

    Field Handling

*****************************************************************************/

#include <stdio.h>
#include "driver.h"
#include "osd_cpu.h"
#include "mamedbg.h"
#include "tms34010.h"
#include "34010ops.h"

#ifdef MAME_DEBUG
extern int debug_key_pressed;
#endif

void wfield_01(UINT32 bitaddr, UINT32 data)
{
	WFIELDMAC(0x01,16);
}
void wfield_02(UINT32 bitaddr, UINT32 data)
{
	WFIELDMAC(0x03,15);
}
void wfield_03(UINT32 bitaddr, UINT32 data)
{
	WFIELDMAC(0x07,14);
}
void wfield_04(UINT32 bitaddr, UINT32 data)
{
	WFIELDMAC(0x0f,13);
}
void wfield_05(UINT32 bitaddr, UINT32 data)
{
	WFIELDMAC(0x1f,12);
}
void wfield_06(UINT32 bitaddr, UINT32 data)
{
	WFIELDMAC(0x3f,11);
}
void wfield_07(UINT32 bitaddr, UINT32 data)
{
	WFIELDMAC(0x7f,10);
}
void wfield_08(UINT32 bitaddr, UINT32 data)
{
	WFIELDMAC_8;
}
void wfield_09(UINT32 bitaddr, UINT32 data)
{
	WFIELDMAC(0x1ff,8);
}
void wfield_10(UINT32 bitaddr, UINT32 data)
{
	WFIELDMAC(0x3ff,7);
}
void wfield_11(UINT32 bitaddr, UINT32 data)
{
	WFIELDMAC(0x7ff,6);
}
void wfield_12(UINT32 bitaddr, UINT32 data)
{
	WFIELDMAC(0xfff,5);
}
void wfield_13(UINT32 bitaddr, UINT32 data)
{
	WFIELDMAC(0x1fff,4);
}
void wfield_14(UINT32 bitaddr, UINT32 data)
{
	WFIELDMAC(0x3fff,3);
}
void wfield_15(UINT32 bitaddr, UINT32 data)
{
	WFIELDMAC(0x7fff,2);
}
void wfield_16(UINT32 bitaddr, UINT32 data)
{
	if (bitaddr&0x0f)
	{
		WFIELDMAC(0xffff,1);
	}
	else
	{
		TMS34010_WRMEM_WORD(TOBYTE(bitaddr),data);
	}
}
void wfield_17(UINT32 bitaddr, UINT32 data)
{
	if (errorlog) fprintf(errorlog,"17-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
}
void wfield_18(UINT32 bitaddr, UINT32 data)
{
	if (errorlog) fprintf(errorlog,"18-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
}
void wfield_19(UINT32 bitaddr, UINT32 data)
{
	if (errorlog) fprintf(errorlog,"19-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
}
void wfield_20(UINT32 bitaddr, UINT32 data)
{
	if (errorlog) fprintf(errorlog,"20-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
}
void wfield_21(UINT32 bitaddr, UINT32 data)
{
	if (errorlog) fprintf(errorlog,"21-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
}
void wfield_22(UINT32 bitaddr, UINT32 data)
{
	if (errorlog) fprintf(errorlog,"22-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
}
void wfield_23(UINT32 bitaddr, UINT32 data)
{
	if (errorlog) fprintf(errorlog,"23-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
}
void wfield_24(UINT32 bitaddr, UINT32 data)
{
	if (errorlog) fprintf(errorlog,"24-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
}
void wfield_25(UINT32 bitaddr, UINT32 data)
{
	if (errorlog) fprintf(errorlog,"25-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
}
void wfield_26(UINT32 bitaddr, UINT32 data)
{
	if (errorlog) fprintf(errorlog,"26-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
}
void wfield_27(UINT32 bitaddr, UINT32 data)
{
	if (errorlog) fprintf(errorlog,"27-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
}
void wfield_28(UINT32 bitaddr, UINT32 data)
{
	if (errorlog) fprintf(errorlog,"28-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
}
void wfield_29(UINT32 bitaddr, UINT32 data)
{
	if (errorlog) fprintf(errorlog,"29-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
}
void wfield_30(UINT32 bitaddr, UINT32 data)
{
	if (errorlog) fprintf(errorlog,"30-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
}
void wfield_31(UINT32 bitaddr, UINT32 data)
{
	if (errorlog) fprintf(errorlog,"31-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
}
void wfield_32(UINT32 bitaddr, UINT32 data)
{
	WFIELDMAC_32;
}


/* Read field zero extended */

INT32 rfield_z_01(UINT32 bitaddr)
{
	RFIELDMAC_Z(0x01,16);
}
INT32 rfield_z_02(UINT32 bitaddr)
{
	RFIELDMAC_Z(0x03,15);
}
INT32 rfield_z_03(UINT32 bitaddr)
{
	RFIELDMAC_Z(0x07,14);
}
INT32 rfield_z_04(UINT32 bitaddr)
{
	RFIELDMAC_Z(0x0f,13);
}
INT32 rfield_z_05(UINT32 bitaddr)
{
	RFIELDMAC_Z(0x1f,12);
}
INT32 rfield_z_06(UINT32 bitaddr)
{
	RFIELDMAC_Z(0x3f,11);
}
INT32 rfield_z_07(UINT32 bitaddr)
{
	RFIELDMAC_Z(0x7f,10);
}
INT32 rfield_z_08(UINT32 bitaddr)
{
	RFIELDMAC_Z_8;
}
INT32 rfield_z_09(UINT32 bitaddr)
{
	RFIELDMAC_Z(0x1ff,8);
}
INT32 rfield_z_10(UINT32 bitaddr)
{
	RFIELDMAC_Z(0x3ff,7);
}
INT32 rfield_z_11(UINT32 bitaddr)
{
	RFIELDMAC_Z(0x7ff,6);
}
INT32 rfield_z_12(UINT32 bitaddr)
{
	RFIELDMAC_Z(0xfff,5);
}
INT32 rfield_z_13(UINT32 bitaddr)
{
	RFIELDMAC_Z(0x1fff,4);
}
INT32 rfield_z_14(UINT32 bitaddr)
{
	RFIELDMAC_Z(0x3fff,3);
}
INT32 rfield_z_15(UINT32 bitaddr)
{
	RFIELDMAC_Z(0x7fff,2);
}
INT32 rfield_z_16(UINT32 bitaddr)
{
	if (bitaddr&0x0f)
	{
		RFIELDMAC_Z(0xffff,1);
	}
	else
	{
		return TMS34010_RDMEM_WORD(TOBYTE(bitaddr));
	}
}
INT32 rfield_z_17(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"17-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_z_18(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"18-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_z_19(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"19-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_z_20(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"20-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_z_21(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"21-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_z_22(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"22-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_z_23(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"23-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_z_24(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"24-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_z_25(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"25-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_z_26(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"26-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_z_27(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"27-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_z_28(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"28-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_z_29(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"29-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_z_30(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"30-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_z_31(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"31-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_32(UINT32 bitaddr)
{
	RFIELDMAC_32;
}




/* Read field sign extended */

INT32 rfield_s_01(UINT32 bitaddr)
{
	RFIELDMAC_S(0x01,16);
	if (ret & 0x01)  ret |= 0xfffffffe;
	return ret;
}
INT32 rfield_s_02(UINT32 bitaddr)
{
	RFIELDMAC_S(0x03,15);
	if (ret & 0x02)  ret |= 0xfffffffc;
	return ret;
}
INT32 rfield_s_03(UINT32 bitaddr)
{
	RFIELDMAC_S(0x07,14);
	if (ret & 0x04)  ret |= 0xfffffff8;
	return ret;
}
INT32 rfield_s_04(UINT32 bitaddr)
{
	RFIELDMAC_S(0x0f,13);
	if (ret & 0x08)  ret |= 0xfffffff0;
	return ret;
}
INT32 rfield_s_05(UINT32 bitaddr)
{
	RFIELDMAC_S(0x1f,12);
	if (ret & 0x10)  ret |= 0xffffffe0;
	return ret;
}
INT32 rfield_s_06(UINT32 bitaddr)
{
	RFIELDMAC_S(0x3f,11);
	if (ret & 0x20)  ret |= 0xffffffc0;
	return ret;
}
INT32 rfield_s_07(UINT32 bitaddr)
{
	RFIELDMAC_S(0x7f,10);
	if (ret & 0x40)  ret |= 0xffffff80;
	return ret;
}
INT32 rfield_s_08(UINT32 bitaddr)
{
	if (bitaddr&0x07)											
	{															
		RFIELDMAC_S(0xff,9);
		return (INT32)(INT8)ret;
	}															
	else														
	{															
		return (INT32)(INT8)TMS34010_RDMEM(TOBYTE(bitaddr));					
	}
}
INT32 rfield_s_09(UINT32 bitaddr)
{
	RFIELDMAC_S(0x1ff,8);
	if (ret & 0x100)  ret |= 0xfffffe00;
	return ret;
}
INT32 rfield_s_10(UINT32 bitaddr)
{
	RFIELDMAC_S(0x3ff,7);
	if (ret & 0x200)  ret |= 0xfffffc00;
	return ret;
}
INT32 rfield_s_11(UINT32 bitaddr)
{
	RFIELDMAC_S(0x7ff,6);
	if (ret & 0x400)  ret |= 0xfffff800;
	return ret;
}
INT32 rfield_s_12(UINT32 bitaddr)
{
	RFIELDMAC_S(0xfff,5);
	if (ret & 0x800)  ret |= 0xfffff000;
	return ret;
}
INT32 rfield_s_13(UINT32 bitaddr)
{
	RFIELDMAC_S(0x1fff,4);
	if (ret & 0x1000)  ret |= 0xffffe000;
	return ret;
}
INT32 rfield_s_14(UINT32 bitaddr)
{
	RFIELDMAC_S(0x3fff,3);
	if (ret & 0x2000)  ret |= 0xffffc000;
	return ret;
}
INT32 rfield_s_15(UINT32 bitaddr)
{
	RFIELDMAC_S(0x7fff,2);
	if (ret & 0x4000)  ret |= 0xffff8000;
	return ret;
}
INT32 rfield_s_16(UINT32 bitaddr)
{
	if (bitaddr&0x0f)
	{
		RFIELDMAC_S(0xffff,1);
		return (INT32)(INT16)ret;
	}
	else
	{
		return (INT32)(INT16)TMS34010_RDMEM_WORD(TOBYTE(bitaddr));
	}
}
INT32 rfield_s_17(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"17-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_s_18(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"18-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_s_19(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"19-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_s_20(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"20-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_s_21(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"21-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_s_22(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"22-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_s_23(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"23-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_s_24(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"24-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_s_25(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"25-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_s_26(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"26-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_s_27(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"27-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_s_28(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"28-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_s_29(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"29-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_s_30(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"30-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}
INT32 rfield_s_31(UINT32 bitaddr)
{
	if (errorlog) fprintf(errorlog,"31-bit fields are not implemented!\n");
#ifdef MAME_DEBUG
	debug_key_pressed=1;
#endif
	return 0;
}

