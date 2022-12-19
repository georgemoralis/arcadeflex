/*###################################################################################################
**
**	TMS34010: Portable Texas Instruments TMS34010 emulator
**
**	Copyright (C) Alex Pasadyn/Zsolt Vasvari 1998
**	 Parts based on code by Aaron Giles
**
**#################################################################################################*/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package cpu.tms34010;

public class _34010fld
{
	
	
	/*###################################################################################################
	**	FIELD WRITE FUNCTIONS
	**#################################################################################################*/
	
	public static WriteHandlerPtr wfield_01 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC(0x01,16);
	} };
	
	public static WriteHandlerPtr wfield_02 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC(0x03,15);
	} };
	
	public static WriteHandlerPtr wfield_03 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC(0x07,14);
	} };
	
	public static WriteHandlerPtr wfield_04 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC(0x0f,13);
	} };
	
	public static WriteHandlerPtr wfield_05 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC(0x1f,12);
	} };
	
	public static WriteHandlerPtr wfield_06 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC(0x3f,11);
	} };
	
	public static WriteHandlerPtr wfield_07 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC(0x7f,10);
	} };
	
	public static WriteHandlerPtr wfield_08 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC_8;
	} };
	
	public static WriteHandlerPtr wfield_09 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC(0x1ff,8);
	} };
	
	public static WriteHandlerPtr wfield_10 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC(0x3ff,7);
	} };
	
	public static WriteHandlerPtr wfield_11 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC(0x7ff,6);
	} };
	
	public static WriteHandlerPtr wfield_12 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC(0xfff,5);
	} };
	
	public static WriteHandlerPtr wfield_13 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC(0x1fff,4);
	} };
	
	public static WriteHandlerPtr wfield_14 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC(0x3fff,3);
	} };
	
	public static WriteHandlerPtr wfield_15 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC(0x7fff,2);
	} };
	
	public static WriteHandlerPtr wfield_16 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((offset & 0x0f) != 0)
		{
			WFIELDMAC(0xffff,1);
		}
		else
		{
			TMS34010_WRMEM_WORD(TOBYTE(offset),data);
		}
	} };
	
	public static WriteHandlerPtr wfield_17 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC(0x1ffff,0);
	} };
	
	public static WriteHandlerPtr wfield_18 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC_BIG(0x3ffff,15);
	} };
	
	public static WriteHandlerPtr wfield_19 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC_BIG(0x7ffff,14);
	} };
	
	public static WriteHandlerPtr wfield_20 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC_BIG(0xfffff,13);
	} };
	
	public static WriteHandlerPtr wfield_21 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC_BIG(0x1fffff,12);
	} };
	
	public static WriteHandlerPtr wfield_22 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC_BIG(0x3fffff,11);
	} };
	
	public static WriteHandlerPtr wfield_23 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC_BIG(0x7fffff,10);
	} };
	
	public static WriteHandlerPtr wfield_24 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC_BIG(0xffffff,9);
	} };
	
	public static WriteHandlerPtr wfield_25 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC_BIG(0x1ffffff,8);
	} };
	
	public static WriteHandlerPtr wfield_26 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC_BIG(0x3ffffff,7);
	} };
	
	public static WriteHandlerPtr wfield_27 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC_BIG(0x7ffffff,6);
	} };
	
	public static WriteHandlerPtr wfield_28 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC_BIG(0xfffffff,5);
	} };
	
	public static WriteHandlerPtr wfield_29 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC_BIG(0x1fffffff,4);
	} };
	
	public static WriteHandlerPtr wfield_30 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC_BIG(0x3fffffff,3);
	} };
	
	public static WriteHandlerPtr wfield_31 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC_BIG(0x7fffffff,2);
	} };
	
	public static WriteHandlerPtr wfield_32 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WFIELDMAC_32;
	} };
	
	
	
	/*###################################################################################################
	**	FIELD READ FUNCTIONS (ZERO-EXTEND)
	**#################################################################################################*/
	
	public static ReadHandlerPtr rfield_z_01  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x01,16);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_02  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x03,15);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_03  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x07,14);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_04  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x0f,13);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_05  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x1f,12);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_06  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x3f,11);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_07  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x7f,10);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_08  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_8;
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_09  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x1ff,8);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_10  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x3ff,7);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_11  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x7ff,6);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_12  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0xfff,5);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_13  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x1fff,4);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_14  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x3fff,3);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_15  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x7fff,2);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_16  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		if ((offset & 0x0f) != 0)
		{
			RFIELDMAC(0xffff,1);
		}
	
		else
			ret = TMS34010_RDMEM_WORD(TOBYTE(offset));
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_17  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x1ffff,0);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_18  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x3ffff,15);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_19  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x7ffff,14);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_20  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0xfffff,13);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_21  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x1fffff,12);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_22  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x3fffff,11);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_23  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x7fffff,10);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_24  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0xffffff,9);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_25  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x1ffffff,8);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_26  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x3ffffff,7);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_27  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x7ffffff,6);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_28  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0xfffffff,5);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_29  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x1fffffff,4);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_30  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x3fffffff,3);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_z_31  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x7fffffff,2);
		return ret;
	} };
	
	public static ReadHandlerPtr rfield_32  = new ReadHandlerPtr() { public int handler(int offset)
	{
		RFIELDMAC_32;
	} };
	
	
	
	/*###################################################################################################
	**	FIELD READ FUNCTIONS (SIGN-EXTEND)
	**#################################################################################################*/
	
	public static ReadHandlerPtr rfield_s_01  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x01,16);
		return ((INT32)(ret << 31)) >> 31;
	} };
	
	public static ReadHandlerPtr rfield_s_02  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x03,15);
		return ((INT32)(ret << 30)) >> 30;
	} };
	
	public static ReadHandlerPtr rfield_s_03  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x07,14);
		return ((INT32)(ret << 29)) >> 29;
	} };
	
	public static ReadHandlerPtr rfield_s_04  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x0f,13);
		return ((INT32)(ret << 28)) >> 28;
	} };
	
	public static ReadHandlerPtr rfield_s_05  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x1f,12);
		return ((INT32)(ret << 27)) >> 27;
	} };
	
	public static ReadHandlerPtr rfield_s_06  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x3f,11);
		return ((INT32)(ret << 26)) >> 26;
	} };
	
	public static ReadHandlerPtr rfield_s_07  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x7f,10);
		return ((INT32)(ret << 25)) >> 25;
	} };
	
	public static ReadHandlerPtr rfield_s_08  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		if ((offset & 0x07) != 0)											
		{															
			RFIELDMAC(0xff,9);
		}
																
		else														
			ret = TMS34010_RDMEM(TOBYTE(offset));					
		return (INT32)(INT8)ret;
	} };
	
	public static ReadHandlerPtr rfield_s_09  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x1ff,8);
		return ((INT32)(ret << 23)) >> 23;
	} };
	
	public static ReadHandlerPtr rfield_s_10  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x3ff,7);
		return ((INT32)(ret << 22)) >> 22;
	} };
	
	public static ReadHandlerPtr rfield_s_11  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x7ff,6);
		return ((INT32)(ret << 21)) >> 21;
	} };
	
	public static ReadHandlerPtr rfield_s_12  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0xfff,5);
		return ((INT32)(ret << 20)) >> 20;
	} };
	
	public static ReadHandlerPtr rfield_s_13  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x1fff,4);
		return ((INT32)(ret << 19)) >> 19;
	} };
	
	public static ReadHandlerPtr rfield_s_14  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x3fff,3);
		return ((INT32)(ret << 18)) >> 18;
	} };
	
	public static ReadHandlerPtr rfield_s_15  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x7fff,2);
		return ((INT32)(ret << 17)) >> 17;
	} };
	
	public static ReadHandlerPtr rfield_s_16  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		if ((offset & 0x0f) != 0)
		{
			RFIELDMAC(0xffff,1);
		}
	
		else
		{
			ret = TMS34010_RDMEM_WORD(TOBYTE(offset));
		}
	
		return (INT32)(INT16)ret;
	} };
	
	public static ReadHandlerPtr rfield_s_17  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC(0x1ffff,0);
		return ((INT32)(ret << 15)) >> 15;
	} };
	
	public static ReadHandlerPtr rfield_s_18  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x3ffff,15);
		return ((INT32)(ret << 14)) >> 14;
	} };
	
	public static ReadHandlerPtr rfield_s_19  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x7ffff,14);
		return ((INT32)(ret << 13)) >> 13;
	} };
	
	public static ReadHandlerPtr rfield_s_20  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0xfffff,13);
		return ((INT32)(ret << 12)) >> 12;
	} };
	
	public static ReadHandlerPtr rfield_s_21  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x1fffff,12);
		return ((INT32)(ret << 11)) >> 11;
	} };
	
	public static ReadHandlerPtr rfield_s_22  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x3fffff,11);
		return ((INT32)(ret << 10)) >> 10;
	} };
	
	public static ReadHandlerPtr rfield_s_23  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x7fffff,10);
		return ((INT32)(ret << 9)) >> 9;
	} };
	
	public static ReadHandlerPtr rfield_s_24  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0xffffff,9);
		return ((INT32)(ret << 8)) >> 8;
	} };
	
	public static ReadHandlerPtr rfield_s_25  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x1ffffff,8);
		return ((INT32)(ret << 7)) >> 7;
	} };
	
	public static ReadHandlerPtr rfield_s_26  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x3ffffff,7);
		return ((INT32)(ret << 6)) >> 6;
	} };
	
	public static ReadHandlerPtr rfield_s_27  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x7ffffff,6);
		return ((INT32)(ret << 5)) >> 5;
	} };
	
	public static ReadHandlerPtr rfield_s_28  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0xfffffff,5);
		return ((INT32)(ret << 4)) >> 4;
	} };
	
	public static ReadHandlerPtr rfield_s_29  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x1fffffff,4);
		return ((INT32)(ret << 3)) >> 3;
	} };
	
	public static ReadHandlerPtr rfield_s_30  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x3fffffff,3);
		return ((INT32)(ret << 2)) >> 2;
	} };
	
	public static ReadHandlerPtr rfield_s_31  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT32 ret;
		RFIELDMAC_BIG(0x7fffffff,2);
		return ((INT32)(ret << 1)) >> 1;
	} };
	
	
}
