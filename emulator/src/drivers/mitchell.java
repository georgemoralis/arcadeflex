
package drivers;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
import static mame.inputport.*;
import static mame.drawgfxH.*;
import static vidhrdw.generic.*;
import static mame.sndintrfH.*;
import static mame.cpuintrf.*;
import static mame.common.*;
import static arcadeflex.input.*;
import static mame.inputportH.*;
import static mame.inputH.*;
import static arcadeflex.libc.*;
import static arcadeflex.libc_old.*;
import static vidhrdw._1942.*;
import static sound.samplesH.*;
import static mame.memory.*;
import static sound.ay8910.*;
import static sound.ay8910H.*;
import static mame.mame.*;
import static mame.sndintrf.*;
import static vidhrdw.mitchell.*;
import static machine.eepromH.*;
import static machine.eeprom.*;
import static mame.cpuintrfH.*;
import static arcadeflex.libc_old.*;
import static arcadeflex.fileio.*;


import static machine.kabuki.*;

public class mitchell {

    
    public static WriteHandlerPtr pang_bankswitch_w= new WriteHandlerPtr() { public void handler(int offset, int data)
    {
    	int bankaddress;
    	UBytePtr RAM = memory_region(REGION_CPU1);
    
    	bankaddress = 0x10000 + (data & 0x0f) * 0x4000;
    
    	cpu_setbank(1,new UBytePtr(RAM,bankaddress));
    }};
    
    
    
    /***************************************************************************
    
      EEPROM
    
    ***************************************************************************/
    
    static EEPROM_interface eeprom_interface = new EEPROM_interface
    (
    	6,		/* address bits */
    	16,		/* data bits */
    	"0110",	/*  read command */
    	"0101",	/* write command */
    	"0111"	/* erase command */
    );
    
    static CharPtr nvram=new CharPtr();
    static int nvram_size;
    static int init_eeprom_count;
    
    public static nvramPtr nvram_handler = new nvramPtr(){ public void handler(Object file,int read_or_write)
    {
    	if (read_or_write!=0)
    	{
    		EEPROM_save(file);					/* EEPROM */
    		if (nvram_size!=0)	/* Super Pang, Block Block */
    			osd_fwrite(file,nvram,nvram_size);	/* NVRAM */
    	}
    	else
    	{
    		EEPROM_init(eeprom_interface);
    
    		if (file!=null)
    		{
    			init_eeprom_count = 0;
    			EEPROM_load(file);					/* EEPROM */
    			if (nvram_size!=0)	/* Super Pang, Block Block */
    			osd_fread(file,nvram,nvram_size);	/* NVRAM */
    		}
    		else
    			init_eeprom_count = 1000;	/* for Super Pang */
    	}   
    }};
    public static ReadHandlerPtr pang_port5_r = new ReadHandlerPtr() { public int handler(int offset)
    {
    	int bit;
    /*TODO*///	extern struct GameDriver driver_mgakuen2;
    /*TODO*///
    	bit = EEPROM_read_bit() << 7;
    /*TODO*///
    /*TODO*///	/* bits 0 and (sometimes) 3 are checked in the interrupt handler. */
    /*TODO*///	/* Maybe they are vblank related, but I'm not sure. */
    /*TODO*///	/* bit 3 is checked before updating the palette so it really seems to be vblank. */
    /*TODO*///	/* Many games require two interrupts per frame and for these bits to toggle, */
    /*TODO*///	/* otherwise music doesn't work. */
    	if ((cpu_getiloops() & 1)!=0) bit |= 0x01;
    	else bit |= 0x08;
    /*TODO*///if (Machine->gamedrv == &driver_mgakuen2)	/* hack... music doesn't work otherwise */
    /*TODO*///	bit ^= 0x08;
    /*TODO*///
    	return (input_port_0_r.handler(0) & 0x76) | bit;
    }};
    
    public static WriteHandlerPtr eeprom_cs_w= new WriteHandlerPtr() { public void handler(int offset, int data)
    {
    	EEPROM_set_cs_line(data!=0 ? CLEAR_LINE : ASSERT_LINE);
    }};
    
    public static WriteHandlerPtr eeprom_clock_w= new WriteHandlerPtr() { public void handler(int offset, int data)
    {
    	EEPROM_set_clock_line(data!=0 ? CLEAR_LINE : ASSERT_LINE);
    }};
    
    public static WriteHandlerPtr eeprom_serial_w= new WriteHandlerPtr() { public void handler(int offset, int data)
    {
    	EEPROM_write_bit(data);
    }};
    
    
    
    /*TODO*////***************************************************************************
    /*TODO*///
    /*TODO*///  Input handling
    /*TODO*///
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///static int dial[2],dial_selected;
    /*TODO*///
    /*TODO*///static int block_input_r(int offset)
    /*TODO*///{
    /*TODO*///	static int dir[2];
    /*TODO*///
    /*TODO*///	if (dial_selected)
    /*TODO*///	{
    /*TODO*///		int delta;
    /*TODO*///
    /*TODO*///		delta = (readinputport(4 + offset) - dial[offset]) & 0xff;
    /*TODO*///		if (delta & 0x80)
    /*TODO*///		{
    /*TODO*///			delta = (-delta) & 0xff;
    /*TODO*///			if (dir[offset])
    /*TODO*///			{
    /*TODO*///			/* don't report movement on a direction change, otherwise it will stutter */
    /*TODO*///				dir[offset] = 0;
    /*TODO*///				delta = 0;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///		else if (delta > 0)
    /*TODO*///		{
    /*TODO*///			if (dir[offset] == 0)
    /*TODO*///			{
    /*TODO*///			/* don't report movement on a direction change, otherwise it will stutter */
    /*TODO*///				dir[offset] = 1;
    /*TODO*///				delta = 0;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///		if (delta > 0x3f) delta = 0x3f;
    /*TODO*///		return delta << 2;
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		int res;
    /*TODO*///
    /*TODO*///		res = readinputport(2 + offset) & 0xf7;
    /*TODO*///		if (dir[offset]) res |= 0x08;
    /*TODO*///
    /*TODO*///		return res;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void block_dial_control_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	if (data == 0x08)
    /*TODO*///	{
    /*TODO*///		/* reset the dial counters */
    /*TODO*///		dial[0] = readinputport(4);
    /*TODO*///		dial[1] = readinputport(5);
    /*TODO*///	}
    /*TODO*///	else if (data == 0x80)
    /*TODO*///		dial_selected = 0;
    /*TODO*///	else
    /*TODO*///		dial_selected = 1;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    static int keymatrix;
    
    static int mahjong_input_r(int offset)
    {
    	int i;
    
    	for (i = 0;i < 5;i++)
    		if ((keymatrix & (0x80 >> i))!=0) return readinputport(2 + 5 * offset + i);
    
    	return 0xff;
    }
    
    static void mahjong_input_select_w(int offset,int data)
    {
    	keymatrix = data;
    }
    
    
    static int input_type;

    public static ReadHandlerPtr input_r = new ReadHandlerPtr() { public int handler(int offset)
    {
        
    	switch (input_type)
    	{
    		case 0:
    		default:
    			return readinputport(1 + offset);
    /*TODO*///			break;
    		case 1:	/* Mahjong games */
    			if (offset!=0) return mahjong_input_r(offset-1);
    			else return readinputport(1);
    			//break;
    		case 2:	/* Block Block - dial control */
                    throw new UnsupportedOperationException("unsupported");
    /*TODO*///			if (offset) return block_input_r(offset-1);
    /*TODO*///			else return readinputport(1);
    /*TODO*///			break;
    		case 3:	/* Super Pang - simulate START 1 press to initialize EEPROM */
                    throw new UnsupportedOperationException("unsupported");
    /*TODO*///			if (offset || init_eeprom_count == 0) return readinputport(1 + offset);
    /*TODO*///			else
    /*TODO*///			{
    /*TODO*///				init_eeprom_count--;
    /*TODO*///				return readinputport(1) & ~0x08;
    /*TODO*///			}
    /*TODO*///			break;
	}
    }};

    public static WriteHandlerPtr input_w= new WriteHandlerPtr() { public void handler(int offset, int data)
    {
	switch (input_type)
    	{
    		case 0:
                    throw new UnsupportedOperationException("unsupported");
    /*TODO*///		default:
    /*TODO*///if (errorlog) fprintf(errorlog,"PC %04x: write %02x to port 01\n",cpu_get_pc(),data);
    			//break;
    		case 1:
    			mahjong_input_select_w(offset,data);
    			break;
    		case 2:
                    throw new UnsupportedOperationException("unsupported");
    /*TODO*///			block_dial_control_w(offset,data);
    /*TODO*///			break;
    	}
    }};
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///
    /*TODO*///  Memory handlers
    /*TODO*///
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///static struct MemoryReadAddress mgakuen_readmem[] =
    /*TODO*///{
    /*TODO*///	{ 0x0000, 0x7fff, MRA_ROM },
    /*TODO*///	{ 0x8000, 0xbfff, MRA_BANK1 },
    /*TODO*///	{ 0xc000, 0xc7ff, mgakuen_paletteram_r },	/* palette RAM */
    /*TODO*///	{ 0xc800, 0xcfff, pang_colorram_r },	/* Attribute RAM */
    /*TODO*///	{ 0xd000, 0xdfff, mgakuen_videoram_r },	/* char RAM */
    /*TODO*///	{ 0xe000, 0xefff, MRA_RAM },	/* Work RAM */
    /*TODO*///	{ 0xf000, 0xffff, mgakuen_objram_r },	/* OBJ RAM */
    /*TODO*///	{ -1 }  /* end of table */
    /*TODO*///};
    /*TODO*///
    /*TODO*///static struct MemoryWriteAddress mgakuen_writemem[] =
    /*TODO*///{
    /*TODO*///	{ 0x0000, 0xbfff, MWA_ROM },
    /*TODO*///	{ 0xc000, 0xc7ff, mgakuen_paletteram_w },
    /*TODO*///	{ 0xc800, 0xcfff, pang_colorram_w, &pang_colorram },
    /*TODO*///	{ 0xd000, 0xdfff, mgakuen_videoram_w, &pang_videoram, &pang_videoram_size },
    /*TODO*///	{ 0xe000, 0xefff, MWA_RAMROM },
    /*TODO*///	{ 0xf000, 0xffff, mgakuen_objram_w },	/* OBJ RAM */
    /*TODO*///	{ -1 }  /* end of table */
    /*TODO*///};
    /*TODO*///
 	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0xbfff, MRA_BANK1 ),
		new MemoryReadAddress( 0xc000, 0xc7ff, pang_paletteram_r ),	/* Banked palette RAM */
		new MemoryReadAddress( 0xc800, 0xcfff, pang_colorram_r ),	/* Attribute RAM */
		new MemoryReadAddress( 0xd000, 0xdfff, pang_videoram_r ),	/* Banked char / OBJ RAM */
		new MemoryReadAddress( 0xe000, 0xffff, MRA_RAM ),	/* Work RAM */
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( 0xc000, 0xc7ff, pang_paletteram_w ),
		new MemoryWriteAddress( 0xc800, 0xcfff, pang_colorram_w, pang_colorram ),
		new MemoryWriteAddress( 0xd000, 0xdfff, pang_videoram_w, pang_videoram, pang_videoram_size ),
		new MemoryWriteAddress( 0xe000, 0xffff, MWA_RAMROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};

	static IOReadPort readport[] =
	{
    		new IOReadPort( 0x00, 0x02, input_r ),	/* Super Pang needs a kludge to initialize EEPROM;
    							the Mahjong games and Block Block need special input treatment */
		new IOReadPort( 0x03, 0x03, input_port_12_r ),	/* mgakuen only */
		new IOReadPort( 0x04, 0x04, input_port_13_r ),	/* mgakuen only */
    		new IOReadPort( 0x05, 0x05, pang_port5_r ),
		new IOReadPort( -1 )  /* end of table */
	};
	
	static IOWritePort writeport[] =
	{
		new IOWritePort( 0x00, 0x00, pang_gfxctrl_w ),    /* Palette bank, layer enable, coin counters, more */
    		new IOWritePort( 0x01, 0x01, input_w ),
    		new IOWritePort( 0x02, 0x02, pang_bankswitch_w ),      /* Code bank register */
/*TODO*///		new IOWritePort( 0x03, 0x03, YM2413_data_port_0_w ),
/*TODO*///		new IOWritePort( 0x04, 0x04, YM2413_register_port_0_w ),
/*TODO*///		new IOWritePort( 0x05, 0x05, OKIM6295_data_0_w ),
		new IOWritePort( 0x06, 0x06, MWA_NOP ),	/* watchdog? irq ack? */
		new IOWritePort( 0x07, 0x07, pang_video_bank_w ),      /* Video RAM bank register */
    		new IOWritePort( 0x08, 0x08, eeprom_cs_w ),
    		new IOWritePort( 0x10, 0x10, eeprom_clock_w ),
    		new IOWritePort( 0x18, 0x18, eeprom_serial_w ),
		new IOWritePort( -1 )  /* end of table */
	};
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///INPUT_PORTS_START( mgakuen )
    /*TODO*///	PORT_START      /* DSW */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* USED - handled in port5_r */
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* unused? */
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* USED - handled in port5_r */
    /*TODO*///	PORT_BIT( 0x70, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* unused? */
    /*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* data from EEPROM */
    /*TODO*///
    /*TODO*///	PORT_START      /* IN0 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN1 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 )
    /*TODO*///	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Kan", KEYCODE_LCONTROL, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 M", KEYCODE_M, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P1 I", KEYCODE_I, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "P1 E", KEYCODE_E, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P1 A", KEYCODE_A, IP_JOY_NONE )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN1 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Reach", KEYCODE_LSHIFT, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 N", KEYCODE_N, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P1 J", KEYCODE_J, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "P1 F", KEYCODE_F, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P1 B", KEYCODE_B, IP_JOY_NONE )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN1 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Ron", KEYCODE_Z, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Chi", KEYCODE_SPACE, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P1 K", KEYCODE_K, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "P1 G", KEYCODE_G, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P1 C", KEYCODE_C, IP_JOY_NONE )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN1 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Pon", KEYCODE_LALT, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P1 L", KEYCODE_L, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "P1 H", KEYCODE_H, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P1 D", KEYCODE_D, IP_JOY_NONE )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN1 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Flip", KEYCODE_X, IP_JOY_NONE )
    /*TODO*///	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN2 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START2 )
    /*TODO*///	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P2 Kan", KEYCODE_LCONTROL, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P2 M", KEYCODE_M, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P2 I", KEYCODE_I, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "P2 E", KEYCODE_E, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P2 A", KEYCODE_A, IP_JOY_NONE )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN2 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P2 Reach", KEYCODE_LSHIFT, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P2 N", KEYCODE_N, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P2 J", KEYCODE_J, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "P2 F", KEYCODE_F, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P2 B", KEYCODE_B, IP_JOY_NONE )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN2 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P2 Ron", KEYCODE_Z, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P2 Chi", KEYCODE_SPACE, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P2 K", KEYCODE_K, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "P2 G", KEYCODE_G, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P2 C", KEYCODE_C, IP_JOY_NONE )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN2 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P2 Pon", KEYCODE_LALT, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P2 L", KEYCODE_L, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "P2 H", KEYCODE_H, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P2 D", KEYCODE_D, IP_JOY_NONE )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN2 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P2 Flip", KEYCODE_X, IP_JOY_NONE )
    /*TODO*///	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///
    /*TODO*///	PORT_START	/* DSW1 */
    /*TODO*///	PORT_DIPNAME( 0x07, 0x07, DEF_STR( Coinage ) )
    /*TODO*///	PORT_DIPSETTING(    0x00, DEF_STR( 4C_1C ) )
    /*TODO*///	PORT_DIPSETTING(    0x01, DEF_STR( 3C_1C ) )
    /*TODO*///	PORT_DIPSETTING(    0x02, DEF_STR( 2C_1C ) )
    /*TODO*///	PORT_DIPSETTING(    0x07, DEF_STR( 1C_1C ) )
    /*TODO*///	PORT_DIPSETTING(    0x06, DEF_STR( 1C_2C ) )
    /*TODO*///	PORT_DIPSETTING(    0x05, DEF_STR( 1C_3C ) )
    /*TODO*///	PORT_DIPSETTING(    0x04, DEF_STR( 1C_4C ) )
    /*TODO*///	PORT_DIPSETTING(    0x03, DEF_STR( 1C_6C ) )
    /*TODO*///	PORT_DIPNAME( 0x08, 0x08, "Rules" )
    /*TODO*///	PORT_DIPSETTING(    0x08, "Kantou" )
    /*TODO*///	PORT_DIPSETTING(    0x00, "Kansai" )
    /*TODO*///	PORT_DIPNAME( 0x10, 0x00, "Harness Type" )
    /*TODO*///	PORT_DIPSETTING(    0x10, "Generic" )
    /*TODO*///	PORT_DIPSETTING(    0x00, "Royal Mahjong" )
    /*TODO*///	PORT_DIPNAME( 0x20, 0x20, DEF_STR( Flip_Screen ) )
    /*TODO*///	PORT_DIPSETTING(    0x20, DEF_STR( Off ) )
    /*TODO*///	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
    /*TODO*///	PORT_DIPNAME( 0x40, 0x40, DEF_STR( Unknown ) )
    /*TODO*///	PORT_DIPSETTING(    0x40, DEF_STR( Off ) )
    /*TODO*///	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
    /*TODO*///	PORT_SERVICE( 0x80, IP_ACTIVE_LOW )
    /*TODO*///
    /*TODO*///	PORT_START	/* DSW2 */
    /*TODO*///	PORT_DIPNAME( 0x03, 0x03, "Player 1 Skill" )
    /*TODO*///	PORT_DIPSETTING(    0x03, "Weak" )
    /*TODO*///	PORT_DIPSETTING(    0x02, "Normal" )
    /*TODO*///	PORT_DIPSETTING(    0x01, "Strong" )
    /*TODO*///	PORT_DIPSETTING(    0x00, "Very Strong" )
    /*TODO*///	PORT_DIPNAME( 0x0c, 0x0c, "Player 1 Skill" )
    /*TODO*///	PORT_DIPSETTING(    0x0c, "Weak" )
    /*TODO*///	PORT_DIPSETTING(    0x08, "Normal" )
    /*TODO*///	PORT_DIPSETTING(    0x04, "Strong" )
    /*TODO*///	PORT_DIPSETTING(    0x00, "Very Strong" )
    /*TODO*///	PORT_DIPNAME( 0x10, 0x00, "Music" )
    /*TODO*///	PORT_DIPSETTING(    0x10, DEF_STR( Off ) )
    /*TODO*///	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
    /*TODO*///	PORT_DIPNAME( 0x20, 0x00, DEF_STR( Demo_Sounds ) )
    /*TODO*///	PORT_DIPSETTING(    0x20, DEF_STR( Off ) )
    /*TODO*///	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
    /*TODO*///	PORT_DIPNAME( 0x40, 0x00, "Help Mode" )
    /*TODO*///	PORT_DIPSETTING(    0x40, DEF_STR( Off ) )
    /*TODO*///	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
    /*TODO*///	PORT_DIPNAME( 0x80, 0x80, DEF_STR( Unknown ) )
    /*TODO*///	PORT_DIPSETTING(    0x80, DEF_STR( Off ) )
    /*TODO*///	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
    /*TODO*///INPUT_PORTS_END
    /*TODO*///
    /*TODO*///INPUT_PORTS_START( marukin )
    /*TODO*///	PORT_START      /* DSW */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* USED - handled in port5_r */
    /*TODO*///	PORT_BITX(0x02, 0x02, IPT_SERVICE, DEF_STR( Service_Mode ), KEYCODE_F2, IP_JOY_NONE )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* unused? */
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* USED - handled in port5_r */
    /*TODO*///	PORT_BIT( 0x70, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* unused? */
    /*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* data from EEPROM */
    /*TODO*///
    /*TODO*///	PORT_START      /* IN0 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_SERVICE )	/* same as the service mode farther down */
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN1 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 )
    /*TODO*///	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Kan", KEYCODE_LCONTROL, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 M", KEYCODE_M, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P1 I", KEYCODE_I, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "P1 E", KEYCODE_E, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P1 A", KEYCODE_A, IP_JOY_NONE )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN1 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Reach", KEYCODE_LSHIFT, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 N", KEYCODE_N, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P1 J", KEYCODE_J, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "P1 F", KEYCODE_F, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P1 B", KEYCODE_B, IP_JOY_NONE )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN1 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Ron", KEYCODE_Z, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Chi", KEYCODE_SPACE, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P1 K", KEYCODE_K, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "P1 G", KEYCODE_G, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P1 C", KEYCODE_C, IP_JOY_NONE )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN1 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Pon", KEYCODE_LALT, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P1 L", KEYCODE_L, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "P1 H", KEYCODE_H, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P1 D", KEYCODE_D, IP_JOY_NONE )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN1 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Flip", KEYCODE_X, IP_JOY_NONE )
    /*TODO*///	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN2 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START2 )
    /*TODO*///	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P2 Kan", KEYCODE_LCONTROL, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P2 M", KEYCODE_M, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P2 I", KEYCODE_I, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "P2 E", KEYCODE_E, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P2 A", KEYCODE_A, IP_JOY_NONE )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN2 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P2 Reach", KEYCODE_LSHIFT, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P2 N", KEYCODE_N, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P2 J", KEYCODE_J, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "P2 F", KEYCODE_F, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P2 B", KEYCODE_B, IP_JOY_NONE )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN2 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P2 Ron", KEYCODE_Z, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P2 Chi", KEYCODE_SPACE, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P2 K", KEYCODE_K, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "P2 G", KEYCODE_G, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P2 C", KEYCODE_C, IP_JOY_NONE )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN2 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P2 Pon", KEYCODE_LALT, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P2 L", KEYCODE_L, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "P2 H", KEYCODE_H, IP_JOY_NONE )
    /*TODO*///	PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P2 D", KEYCODE_D, IP_JOY_NONE )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN2 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P2 Flip", KEYCODE_X, IP_JOY_NONE )
    /*TODO*///	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///INPUT_PORTS_END
    /*TODO*///
 	static InputPortPtr input_ports_pkladies = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* DSW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );/* USED - handled in port5_r */
		PORT_BITX(0x02, 0x02, IPT_SERVICE, DEF_STR( "Service_Mode" ), KEYCODE_F2, IP_JOY_NONE );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );/* unused? */
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );/* USED - handled in port5_r */
		PORT_BIT( 0x70, IP_ACTIVE_LOW, IPT_UNKNOWN );/* unused? */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* data from EEPROM */
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_SERVICE );/* same as the service mode farther down */
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P1 Deal", KEYCODE_LCONTROL, IP_JOY_NONE );
		PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "P1 E", KEYCODE_E, IP_JOY_NONE );
		PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P1 A", KEYCODE_A, IP_JOY_NONE );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P1 Cancel", KEYCODE_LALT, IP_JOY_NONE );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P1 B", KEYCODE_B, IP_JOY_NONE );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P1 Flip", KEYCODE_SPACE, IP_JOY_NONE );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P1 C", KEYCODE_C, IP_JOY_NONE );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P1 D", KEYCODE_D, IP_JOY_NONE );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P2 Deal", KEYCODE_LCONTROL, IP_JOY_NONE );
		PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "P2 E", KEYCODE_E, IP_JOY_NONE );
		PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P2 A", KEYCODE_A, IP_JOY_NONE );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P2 Cancel", KEYCODE_LALT, IP_JOY_NONE );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P2 B", KEYCODE_B, IP_JOY_NONE );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P2 Flip", KEYCODE_SPACE, IP_JOY_NONE );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P2 C", KEYCODE_C, IP_JOY_NONE );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P2 D", KEYCODE_D, IP_JOY_NONE );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
        
    static InputPortPtr input_ports_pang = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* DSW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );/* USED - handled in port5_r */
		PORT_BITX(0x02, 0x02, IPT_SERVICE, DEF_STR( "Service_Mode") , KEYCODE_F2, IP_JOY_NONE );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );/* unused? */
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );/* USED - handled in port5_r */
		PORT_BIT( 0x70, IP_ACTIVE_LOW, IPT_UNKNOWN );/* unused? */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* data from EEPROM */
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );   /* probably unused */
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );   /* probably unused */
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );   /* probably unused */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
	INPUT_PORTS_END(); }}; 
    /*TODO*///
    /*TODO*///INPUT_PORTS_START( qtono1 )
    /*TODO*///	PORT_START      /* DSW */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* USED - handled in port5_r */
    /*TODO*///	PORT_BITX(0x02, 0x02, IPT_SERVICE, DEF_STR( Service_Mode ), KEYCODE_F2, IP_JOY_NONE )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* unused? */
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* USED - handled in port5_r */
    /*TODO*///	PORT_BIT( 0x70, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* unused? */
    /*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* data from EEPROM */
    /*TODO*///
    /*TODO*///	PORT_START      /* IN0 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_SERVICE )	/* same as the service mode farther down */
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 )
    /*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN1 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 )
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON4 )
    /*TODO*///	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 )
    /*TODO*///	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 )
    /*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN2 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START2 )
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 )
    /*TODO*///	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 )
    /*TODO*///	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 )
    /*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 )
    /*TODO*///INPUT_PORTS_END
    /*TODO*///
    /*TODO*///INPUT_PORTS_START( block )
    /*TODO*///	PORT_START      /* DSW */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* USED - handled in port5_r */
    /*TODO*///	PORT_BITX(0x02, 0x02, IPT_SERVICE, DEF_STR( Service_Mode ), KEYCODE_F2, IP_JOY_NONE )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* unused? */
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* USED - handled in port5_r */
    /*TODO*///	PORT_BIT( 0x70, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* unused? */
    /*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* data from EEPROM */
    /*TODO*///
    /*TODO*///	PORT_START      /* IN0 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )    /* probably unused */
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 )
    /*TODO*///	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN )    /* probably unused */
    /*TODO*///	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )    /* probably unused */
    /*TODO*///	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 )
    /*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN1 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* dial direction */
    /*TODO*///	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN2 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )	/* dial direction */
    /*TODO*///	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 )
    /*TODO*///
    /*TODO*///	PORT_START      /* DIAL1 */
    /*TODO*///	PORT_ANALOG( 0xff, 0x00, IPT_DIAL, 50, 20, 0, 0)
    /*TODO*///
    /*TODO*///	PORT_START      /* DIAL2 */
    /*TODO*///	PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER2, 50, 20, 0, 0)
    /*TODO*///INPUT_PORTS_END
    /*TODO*///
    /*TODO*///
    /*TODO*///
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		32768,	/* 32768 characters */
		4,		/* 4 bits per pixel */
		new int[] { 32768*16*8+4, 32768*16*8+0,4, 0 },
		new int[] { 0, 1, 2, 3, 8+0, 8+1, 8+2, 8+3 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		16*8    /* every char takes 16 consecutive bytes */
	);
    /*TODO*///
	static GfxLayout marukin_charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		65536,	/* 65536 characters */
		4,		/* 4 bits per pixel */
		new int[] { 3*4, 2*4, 1*4, 0*4 },
		new int[] { 0, 1, 2, 3, 16+0, 16+1, 16+2, 16+3 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8    /* every char takes 32 consecutive bytes */
	);
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,  /* 16*16 sprites */
		2048,   /* 2048 sprites */
		4,      /* 4 bits per pixel */
		new int[] { 2048*64*8+4, 2048*64*8+0, 4, 0 },
		new int[] { 0, 1, 2, 3, 8+0, 8+1, 8+2, 8+3,
				32*8+0, 32*8+1, 32*8+2, 32*8+3, 33*8+0, 33*8+1, 33*8+2, 33*8+3 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
				8*16, 9*16, 10*16, 11*16, 12*16, 13*16, 14*16, 15*16 },
		64*8    /* every sprite takes 64 consecutive bytes */
	);
    /*TODO*///
    /*TODO*///static struct GfxDecodeInfo mgakuen_gfxdecodeinfo[] =
    /*TODO*///{
    /*TODO*///	{ REGION_GFX1, 0, &marukin_charlayout, 0,  64 }, /* colors 0-1023 */
    /*TODO*///	{ REGION_GFX2, 0, &spritelayout,       0,  16 }, /* colors 0- 255 */
    /*TODO*///	{ -1 } /* end of array */
    /*TODO*///};
    /*TODO*///
	static GfxDecodeInfo marukin_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, marukin_charlayout, 0, 128 ), /* colors 0-2047 */
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout,       0,  16 ), /* colors 0- 255 */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
  
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,     0, 128 ), /* colors 0-2047 */
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout,   0,  16 ), /* colors 0- 255 */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///static struct YM2413interface ym2413_interface=
    /*TODO*///{
    /*TODO*///	1,	/* 1 chip */
    /*TODO*///	8000000,	/* 8MHz ??? (hand tuned) */
    /*TODO*///	{ 50 },	/* Volume */
    /*TODO*///};
    /*TODO*///
    /*TODO*///static struct OKIM6295interface okim6295_interface =
    /*TODO*///{
    /*TODO*///	1,			/* 1 chip */
    /*TODO*///	{ 8000 },	/* 8000Hz ??? */
    /*TODO*///	{ REGION_SOUND1 },		/* memory region 2 */
    /*TODO*///	{ 50 }
    /*TODO*///};
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///static struct MachineDriver machine_driver_mgakuen =
    /*TODO*///{
    /*TODO*///	{
    /*TODO*///		{
    /*TODO*///			CPU_Z80,
    /*TODO*///			6000000,	/* ??? */
    /*TODO*///			mgakuen_readmem,mgakuen_writemem,readport,writeport,
    /*TODO*///			interrupt,2	/* ??? one extra irq seems to be needed for music (see input5_r) */
    /*TODO*///		},
    /*TODO*///	},
    /*TODO*///	60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
    /*TODO*///	1,
    /*TODO*///	0,
    /*TODO*///
    /*TODO*///	64*8, 32*8, { 8*8, (64-8)*8-1, 1*8, 31*8-1 },
    /*TODO*///	mgakuen_gfxdecodeinfo,
    /*TODO*///	1024, 1024,	/* less colors than the others */
    /*TODO*///	0,
    /*TODO*///	VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
    /*TODO*///	0,
    /*TODO*///	pang_vh_start,
    /*TODO*///	pang_vh_stop,
    /*TODO*///	pang_vh_screenrefresh,
    /*TODO*///	0,0,0,0,
    /*TODO*///	{
    /*TODO*///		{
    /*TODO*///			SOUND_OKIM6295,
    /*TODO*///			&okim6295_interface
    /*TODO*///		},
    /*TODO*///		{
    /*TODO*///			SOUND_YM2413,
    /*TODO*///			&ym2413_interface
    /*TODO*///		},
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* no EEPROM */
    /*TODO*///};
    /*TODO*///
    	static MachineDriver machine_driver_pang = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				8000000,	/* Super Pang says 8MHZ ORIGINAL BOARD */
				readmem,writemem,readport,writeport,
				interrupt,2	/* ??? one extra irq seems to be needed for music (see input5_r) */
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		null,
	
		64*8, 32*8, new rectangle( 8*8, (64-8)*8-1, 1*8, 31*8-1 ),
		gfxdecodeinfo,
		2048, 2048,
		null,
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		pang_vh_start,
		pang_vh_stop,
		pang_vh_screenrefresh,
		0,0,0,0,
/*todo*/		null
                /*{
			{
				SOUND_OKIM6295,
				&okim6295_interface
			},
			{
				SOUND_YM2413,
				&ym2413_interface
			},
		},*/
	
		,nvram_handler
	);
    /*TODO*///static struct MachineDriver machine_driver_pang =
    /*TODO*///{
    /*TODO*///	{
    /*TODO*///		{
    /*TODO*///			CPU_Z80,
    /*TODO*///			8000000,	/* Super Pang says 8MHZ ORIGINAL BOARD */
    /*TODO*///			readmem,writemem,readport,writeport,
    /*TODO*///			interrupt,2	/* ??? one extra irq seems to be needed for music (see input5_r) */
    /*TODO*///		},
    /*TODO*///	},
    /*TODO*///	60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
    /*TODO*///	1,
    /*TODO*///	0,
    /*TODO*///
    /*TODO*///	64*8, 32*8, { 8*8, (64-8)*8-1, 1*8, 31*8-1 },
    /*TODO*///	gfxdecodeinfo,
    /*TODO*///	2048, 2048,
    /*TODO*///	0,
    /*TODO*///	VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
    /*TODO*///	0,
    /*TODO*///	pang_vh_start,
    /*TODO*///	pang_vh_stop,
    /*TODO*///	pang_vh_screenrefresh,
    /*TODO*///	0,0,0,0,
    /*TODO*///	{
    /*TODO*///		{
    /*TODO*///			SOUND_OKIM6295,
    /*TODO*///			&okim6295_interface
    /*TODO*///		},
    /*TODO*///		{
    /*TODO*///			SOUND_YM2413,
    /*TODO*///			&ym2413_interface
    /*TODO*///		},
    /*TODO*///	},
    /*TODO*///
    /*TODO*///	nvram_handler
    /*TODO*///};
    /*TODO*///
    /*TODO*///static struct MachineDriver machine_driver_marukin =
    /*TODO*///{
    /*TODO*///	{
    /*TODO*///		{
    /*TODO*///			CPU_Z80,
    /*TODO*///			8000000,	/* Super Pang says 8MHZ ORIGINAL BOARD */
    /*TODO*///			readmem,writemem,readport,writeport,
    /*TODO*///			interrupt,2	/* ??? one extra irq seems to be needed for music (see input5_r) */
    /*TODO*///		},
    /*TODO*///	},
    /*TODO*///	60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
    /*TODO*///	1,
    /*TODO*///	0,
    /*TODO*///
    /*TODO*///	64*8, 32*8, { 8*8, (64-8)*8-1, 1*8, 31*8-1 },
    /*TODO*///	marukin_gfxdecodeinfo,
    /*TODO*///	2048, 2048,
    /*TODO*///	0,
    /*TODO*///	VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
    /*TODO*///	0,
    /*TODO*///	pang_vh_start,
    /*TODO*///	pang_vh_stop,
    /*TODO*///	pang_vh_screenrefresh,
    /*TODO*///	0,0,0,0,
    /*TODO*///	{
    /*TODO*///		{
    /*TODO*///			SOUND_OKIM6295,
    /*TODO*///			&okim6295_interface
    /*TODO*///		},
    /*TODO*///		{
    /*TODO*///			SOUND_YM2413,
    /*TODO*///			&ym2413_interface
    /*TODO*///		},
    /*TODO*///	},
    /*TODO*///
    /*TODO*///	nvram_handler
    /*TODO*///};
    /*TODO*///
        static MachineDriver machine_driver_marukin = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				8000000,	/* Super Pang says 8MHZ ORIGINAL BOARD */
				readmem,writemem,readport,writeport,
				interrupt,2	/* ??? one extra irq seems to be needed for music (see input5_r) */
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		null,
	
		64*8, 32*8, new rectangle( 8*8, (64-8)*8-1, 1*8, 31*8-1 ),
		marukin_gfxdecodeinfo,
		2048, 2048,
		null,
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		pang_vh_start,
		pang_vh_stop,
		pang_vh_screenrefresh,
		0,0,0,0,
/*todo*/		null
		/*{
			{
				SOUND_OKIM6295,
				&okim6295_interface
			},
			{
				SOUND_YM2413,
				&ym2413_interface
			},
		},*/
                ,
		nvram_handler
	);
    /*TODO*///
    /*TODO*///
    /*TODO*///ROM_START( mgakuen )
    /*TODO*///	ROM_REGION( 0x30000, REGION_CPU1 )	/* 192k for code */
    /*TODO*///	ROM_LOAD( "mg-1.1j",      0x00000, 0x08000, 0xbf02ea6b )
    /*TODO*///	ROM_LOAD( "mg-2.1l",      0x10000, 0x20000, 0x64141b0c )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "mg-1.13h",     0x000000, 0x80000, 0xfd6a0805 )	/* chars */
    /*TODO*///	ROM_LOAD( "mg-2.14h",     0x080000, 0x80000, 0xe26e871e )
    /*TODO*///	ROM_LOAD( "mg-3.16h",     0x100000, 0x80000, 0xdd781d9a )
    /*TODO*///	ROM_LOAD( "mg-4.17h",     0x180000, 0x80000, 0x97afcc79 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x040000, REGION_GFX2 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "mg-6.4l",      0x000000, 0x20000, 0x34594e62 )	/* sprites */
    /*TODO*///	ROM_LOAD( "mg-7.6l",      0x020000, 0x20000, 0xf304c806 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x80000, REGION_SOUND1 )	/* OKIM */
    /*TODO*///	ROM_LOAD( "mg-5.1c",      0x00000, 0x80000, 0x170332f1 )	/* banked */
    /*TODO*///ROM_END
    /*TODO*///
    /*TODO*///ROM_START( mgakuen2 )
    /*TODO*///	ROM_REGION( 2*0x50000, REGION_CPU1 )	/* 320k for code + 320k for decrypted opcodes */
    /*TODO*///	ROM_LOAD( "mg2-xf.1j",    0x00000, 0x08000, 0xc8165d2d )
    /*TODO*///	ROM_LOAD( "mg2-y.1l",     0x10000, 0x20000, 0x75bbcc14 )
    /*TODO*///	ROM_LOAD( "mg2-z.3l",     0x30000, 0x20000, 0xbfdba961 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "mg2-a.13h",    0x000000, 0x80000, 0x31a0c55e )	/* chars */
    /*TODO*///	ROM_LOAD( "mg2-b.14h",    0x080000, 0x80000, 0xc18488fa )
    /*TODO*///	ROM_LOAD( "mg2-c.16h",    0x100000, 0x80000, 0x9425b364 )
    /*TODO*///	ROM_LOAD( "mg2-d.17h",    0x180000, 0x80000, 0x6cc9eeba )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x040000, REGION_GFX2 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "mg2-f.4l",     0x000000, 0x20000, 0x3172c9fe )	/* sprites */
    /*TODO*///	ROM_LOAD( "mg2-g.6l",     0x020000, 0x20000, 0x19b8b61c )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x80000, REGION_SOUND1 )	/* OKIM */
    /*TODO*///	ROM_LOAD( "mg2-e.1c",     0x00000, 0x80000, 0x70fd0809 )	/* banked */
    /*TODO*///ROM_END
    /*TODO*///
        static RomLoadPtr rom_pkladies = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x20000, REGION_CPU1 );/* 128k for code + 128k for decrypted opcodes */
		ROM_LOAD( "pko-prg1.14f", 0x00000, 0x08000, 0x86585a94 );
		ROM_LOAD( "pko-prg2.15f", 0x10000, 0x10000, 0x86cbe82d );
	
		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD_GFX_EVEN( "pko-001.8h",   0x000000, 0x80000, 0x1ead5d9b );	/* chars */
		ROM_LOAD_GFX_ODD ( "pko-003.8j",   0x000000, 0x80000, 0x339ab4e6 );
		ROM_LOAD_GFX_EVEN( "pko-002.9h",   0x100000, 0x80000, 0x1cf02586 );
		ROM_LOAD_GFX_ODD ( "pko-004.9j",   0x100000, 0x80000, 0x09ccb442 );
	
		ROM_REGION( 0x040000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "pko-chr1.2j",  0x000000, 0x20000, 0x31ce33cd );/* sprites */
		ROM_LOAD( "pko-chr2.3j",  0x020000, 0x20000, 0xad7e055f );
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* OKIM */
		ROM_LOAD( "pko-voi1.2d",  0x00000, 0x20000, 0x07e0f531 );
		ROM_LOAD( "pko-voi2.3d",  0x20000, 0x20000, 0x18398bf6 );
	ROM_END(); }}; 
    /*TODO*///
    /*TODO*///ROM_START( dokaben )
    /*TODO*///	ROM_REGION( 2*0x50000, REGION_CPU1 )	/* 320k for code + 320k for decrypted opcodes */
    /*TODO*///	ROM_LOAD( "db06.11h",     0x00000, 0x08000, 0x413e0886 )
    /*TODO*///	ROM_LOAD( "db07.13h",     0x10000, 0x20000, 0x8bdcf49e )
    /*TODO*///	ROM_LOAD( "db08.14h",     0x30000, 0x20000, 0x1643bdd9 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "db02.1e",      0x000000, 0x20000, 0x9aa8470c )	/* chars */
    /*TODO*///	ROM_LOAD( "db03.2e",      0x020000, 0x20000, 0x3324e43d )
    /*TODO*///	/* 40000-7ffff empty */
    /*TODO*///	ROM_LOAD( "db04.1g",      0x080000, 0x20000, 0xc0c5b6c2 )
    /*TODO*///	ROM_LOAD( "db05.2g",      0x0a0000, 0x20000, 0xd2ab25f2 )
    /*TODO*///	/* c0000-fffff empty */
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x040000, REGION_GFX2 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "db10.2k",      0x000000, 0x20000, 0x9e70f7ae )	/* sprites */
    /*TODO*///	ROM_LOAD( "db09.1k",      0x020000, 0x20000, 0x2d9263f7 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x80000, REGION_SOUND1 )	/* OKIM */
    /*TODO*///	ROM_LOAD( "db01.1d",      0x00000, 0x20000, 0x62fa6b81 )
    /*TODO*///ROM_END
    /*TODO*///
    	static RomLoadPtr rom_pang = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x30000, REGION_CPU1 );/* 192k for code + 192k for decrypted opcodes */
		ROM_LOAD( "pang6.bin",    0x00000, 0x08000, 0x68be52cd );
		ROM_LOAD( "pang7.bin",    0x10000, 0x20000, 0x4a2e70f6 );
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "pang_09.bin",  0x000000, 0x20000, 0x3a5883f5 );/* chars */
		ROM_LOAD( "bb3.bin",      0x020000, 0x20000, 0x79a8ed08 );
		/* 40000-7ffff empty */
		ROM_LOAD( "pang_11.bin",  0x080000, 0x20000, 0x166a16ae );
		ROM_LOAD( "bb5.bin",      0x0a0000, 0x20000, 0x2fb3db6c );
		/* c0000-fffff empty */
	
		ROM_REGION( 0x040000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "bb10.bin",     0x000000, 0x20000, 0xfdba4f6e );/* sprites */
		ROM_LOAD( "bb9.bin",      0x020000, 0x20000, 0x39f47a63 );
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* OKIM */
		ROM_LOAD( "bb1.bin",      0x00000, 0x20000, 0xc52e5b8e );
	ROM_END(); }}; 
    /*TODO*///
    /*TODO*///ROM_START( pangb )
    /*TODO*///	ROM_REGION( 2*0x30000, REGION_CPU1 )	/* 192k for code + 192k for decrypted opcodes */
    /*TODO*///	ROM_LOAD( "pang_04.bin",  0x30000, 0x08000, 0xf68f88a5 )   /* Decrypted opcode + data */
    /*TODO*///	ROM_CONTINUE(             0x00000, 0x08000 )
    /*TODO*///	ROM_LOAD( "pang_02.bin",  0x40000, 0x20000, 0x3f15bb61 )   /* Decrypted op codes */
    /*TODO*///	ROM_LOAD( "pang_03.bin",  0x10000, 0x20000, 0x0c8477ae )   /* Decrypted data */
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "pang_09.bin",  0x000000, 0x20000, 0x3a5883f5 )	/* chars */
    /*TODO*///	ROM_LOAD( "bb3.bin",      0x020000, 0x20000, 0x79a8ed08 )
    /*TODO*///	/* 40000-7ffff empty */
    /*TODO*///	ROM_LOAD( "pang_11.bin",  0x080000, 0x20000, 0x166a16ae )
    /*TODO*///	ROM_LOAD( "bb5.bin",      0x0a0000, 0x20000, 0x2fb3db6c )
    /*TODO*///	/* c0000-fffff empty */
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x040000, REGION_GFX2 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "bb10.bin",     0x000000, 0x20000, 0xfdba4f6e )	/* sprites */
    /*TODO*///	ROM_LOAD( "bb9.bin",      0x020000, 0x20000, 0x39f47a63 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x80000, REGION_SOUND1 )	/* OKIM */
    /*TODO*///	ROM_LOAD( "bb1.bin",      0x00000, 0x20000, 0xc52e5b8e )
    /*TODO*///ROM_END
    /*TODO*///
    /*TODO*///ROM_START( bbros )
    /*TODO*///	ROM_REGION( 2*0x30000, REGION_CPU1 )	/* 192k for code + 192k for decrypted opcodes */
    /*TODO*///	ROM_LOAD( "bb6.bin",      0x00000, 0x08000, 0xa3041ca4 )
    /*TODO*///	ROM_LOAD( "bb7.bin",      0x10000, 0x20000, 0x09231c68 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "bb2.bin",      0x000000, 0x20000, 0x62f29992 )	/* chars */
    /*TODO*///	ROM_LOAD( "bb3.bin",      0x020000, 0x20000, 0x79a8ed08 )
    /*TODO*///	/* 40000-7ffff empty */
    /*TODO*///	ROM_LOAD( "bb4.bin",      0x080000, 0x20000, 0xf705aa89 )
    /*TODO*///	ROM_LOAD( "bb5.bin",      0x0a0000, 0x20000, 0x2fb3db6c )
    /*TODO*///	/* c0000-fffff empty */
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x040000, REGION_GFX2 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "bb10.bin",     0x000000, 0x20000, 0xfdba4f6e )	/* sprites */
    /*TODO*///	ROM_LOAD( "bb9.bin",      0x020000, 0x20000, 0x39f47a63 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x80000, REGION_SOUND1 )	/* OKIM */
    /*TODO*///	ROM_LOAD( "bb1.bin",      0x00000, 0x20000, 0xc52e5b8e )
    /*TODO*///ROM_END
    /*TODO*///
    /*TODO*///ROM_START( pompingw )
    /*TODO*///	ROM_REGION( 2*0x30000, REGION_CPU1 )	/* 192k for code + 192k for decrypted opcodes */
    /*TODO*///	ROM_LOAD( "pwj_06.11h",   0x00000, 0x08000, 0x4a0a6426 )
    /*TODO*///	ROM_LOAD( "pwj_07.13h",   0x10000, 0x20000, 0xa9402420 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "pw_02.1e",     0x000000, 0x20000, 0x4b5992e4 )	/* chars */
    /*TODO*///	ROM_LOAD( "bb3.bin",      0x020000, 0x20000, 0x79a8ed08 )
    /*TODO*///	/* 40000-7ffff empty */
    /*TODO*///	ROM_LOAD( "pwj_04.1g",    0x080000, 0x20000, 0x01e49081 )
    /*TODO*///	ROM_LOAD( "bb5.bin",      0x0a0000, 0x20000, 0x2fb3db6c )
    /*TODO*///	/* c0000-fffff empty */
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x040000, REGION_GFX2 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "bb10.bin",     0x000000, 0x20000, 0xfdba4f6e )	/* sprites */
    /*TODO*///	ROM_LOAD( "bb9.bin",      0x020000, 0x20000, 0x39f47a63 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x80000, REGION_SOUND1 )	/* OKIM */
    /*TODO*///	ROM_LOAD( "bb1.bin",      0x00000, 0x20000, 0xc52e5b8e )
    /*TODO*///ROM_END
    /*TODO*///
    /*TODO*///ROM_START( cworld )
    /*TODO*///	ROM_REGION( 2*0x50000, REGION_CPU1 )	/* 320k for code + 320k for decrypted opcodes */
    /*TODO*///	ROM_LOAD( "cw05.bin",     0x00000, 0x08000, 0xd3c1723d )
    /*TODO*///	ROM_LOAD( "cw06.bin",     0x10000, 0x20000, 0xd71ed4a3 )
    /*TODO*///	ROM_LOAD( "cw07.bin",     0x30000, 0x20000, 0xd419ce08 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "cw08.bin",     0x000000, 0x20000, 0x6c80da3c )	/* chars */
    /*TODO*///	ROM_LOAD( "cw09.bin",     0x020000, 0x20000, 0x7607da71 )
    /*TODO*///	ROM_LOAD( "cw10.bin",     0x040000, 0x20000, 0x6f0e639f )
    /*TODO*///	ROM_LOAD( "cw11.bin",     0x060000, 0x20000, 0x130bd7c0 )
    /*TODO*///	ROM_LOAD( "cw18.bin",     0x080000, 0x20000, 0xbe6ee0c9 )
    /*TODO*///	ROM_LOAD( "cw19.bin",     0x0a0000, 0x20000, 0x51fc5532 )
    /*TODO*///	ROM_LOAD( "cw20.bin",     0x0c0000, 0x20000, 0x58381d58 )
    /*TODO*///	ROM_LOAD( "cw21.bin",     0x0e0000, 0x20000, 0x910cc753 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x040000, REGION_GFX2 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "cw16.bin",     0x000000, 0x20000, 0xf90217d1 )	/* sprites */
    /*TODO*///	ROM_LOAD( "cw17.bin",     0x020000, 0x20000, 0xc953c702 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x80000, REGION_SOUND1 )	/* OKIM */
    /*TODO*///	ROM_LOAD( "cw01.bin",     0x00000, 0x20000, 0xf4368f5b )
    /*TODO*///ROM_END
    /*TODO*///
    /*TODO*///ROM_START( hatena )
    /*TODO*///	ROM_REGION( 2*0x50000, REGION_CPU1 )	/* 320k for code + 320k for decrypted opcodes */
    /*TODO*///	ROM_LOAD( "q2-05.rom",    0x00000, 0x08000, 0x66c9e1da )
    /*TODO*///	ROM_LOAD( "q2-06.rom",    0x10000, 0x20000, 0x5fc39916 )
    /*TODO*///	ROM_LOAD( "q2-07.rom",    0x30000, 0x20000, 0xec6d5e5e )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "q2-08.rom",    0x000000, 0x20000, 0x6c80da3c )	/* chars */
    /*TODO*///	ROM_LOAD( "q2-09.rom",    0x020000, 0x20000, 0xabe3e15c )
    /*TODO*///	ROM_LOAD( "q2-10.rom",    0x040000, 0x20000, 0x6963450d )
    /*TODO*///	ROM_LOAD( "q2-11.rom",    0x060000, 0x20000, 0x1e319fa2 )
    /*TODO*///	ROM_LOAD( "q2-18.rom",    0x080000, 0x20000, 0xbe6ee0c9 )
    /*TODO*///	ROM_LOAD( "q2-19.rom",    0x0a0000, 0x20000, 0x70300445 )
    /*TODO*///	ROM_LOAD( "q2-20.rom",    0x0c0000, 0x20000, 0x21a6ff42 )
    /*TODO*///	ROM_LOAD( "q2-21.rom",    0x0e0000, 0x20000, 0x076280c9 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x040000, REGION_GFX2 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "q2-16.rom",    0x000000, 0x20000, 0xec19b2f0 )	/* sprites */
    /*TODO*///	ROM_LOAD( "q2-17.rom",    0x020000, 0x20000, 0xecd69d92 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x80000, REGION_SOUND1 )	/* OKIM */
    /*TODO*///	ROM_LOAD( "q2-01.rom",    0x00000, 0x20000, 0x149e7a89 )
    /*TODO*///ROM_END
    /*TODO*///
    /*TODO*///ROM_START( spang )
    /*TODO*///	ROM_REGION( 2*0x50000, REGION_CPU1 )	/* 320k for code + 320k for decrypted opcodes */
    /*TODO*///	ROM_LOAD( "spe_06.rom",   0x00000, 0x08000, 0x1af106fb )
    /*TODO*///	ROM_LOAD( "spe_07.rom",   0x10000, 0x20000, 0x208b5f54 )
    /*TODO*///	ROM_LOAD( "spe_08.rom",   0x30000, 0x20000, 0x2bc03ade )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "spe_02.rom",   0x000000, 0x20000, 0x63c9dfd2 )	/* chars */
    /*TODO*///	ROM_LOAD( "03.f2",        0x020000, 0x20000, 0x3ae28bc1 )
    /*TODO*///	/* 40000-7ffff empty */
    /*TODO*///	ROM_LOAD( "spe_04.rom",   0x080000, 0x20000, 0x9d7b225b )
    /*TODO*///	ROM_LOAD( "05.g2",        0x0a0000, 0x20000, 0x4a060884 )
    /*TODO*///	/* c0000-fffff empty */
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x040000, REGION_GFX2 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "spe_10.rom",   0x000000, 0x20000, 0xeedd0ade )	/* sprites */
    /*TODO*///	ROM_LOAD( "spe_09.rom",   0x020000, 0x20000, 0x04b41b75 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x80000, REGION_SOUND1 )	/* OKIM */
    /*TODO*///	ROM_LOAD( "spe_01.rom",   0x00000, 0x20000, 0x2d19c133 )
    /*TODO*///ROM_END
    /*TODO*///
    /*TODO*///ROM_START( sbbros )
    /*TODO*///	ROM_REGION( 2*0x50000, REGION_CPU1 )	/* 320k for code + 320k for decrypted opcodes */
    /*TODO*///	ROM_LOAD( "06.j12",       0x00000, 0x08000, 0x292eee6a )
    /*TODO*///	ROM_LOAD( "07.j13",       0x10000, 0x20000, 0xf46b698d )
    /*TODO*///	ROM_LOAD( "08.j14",       0x30000, 0x20000, 0xa75e7fbe )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "02.f1",        0x000000, 0x20000, 0x0c22ffc6 )	/* chars */
    /*TODO*///	ROM_LOAD( "03.f2",        0x020000, 0x20000, 0x3ae28bc1 )
    /*TODO*///	/* 40000-7ffff empty */
    /*TODO*///	ROM_LOAD( "04.g2",        0x080000, 0x20000, 0xbb3dee5b )
    /*TODO*///	ROM_LOAD( "05.g2",        0x0a0000, 0x20000, 0x4a060884 )
    /*TODO*///	/* c0000-fffff empty */
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x040000, REGION_GFX2 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "10.l2",        0x000000, 0x20000, 0xd6675d8f )	/* sprites */
    /*TODO*///	ROM_LOAD( "09.l1",        0x020000, 0x20000, 0x8f678bc8 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x80000, REGION_SOUND1 )	/* OKIM */
    /*TODO*///	ROM_LOAD( "01.d1",        0x00000, 0x20000, 0xb96ea126 )
    /*TODO*///ROM_END
    /*TODO*///
    /*TODO*///ROM_START( marukin )
    /*TODO*///	ROM_REGION( 2*0x30000, REGION_CPU1 )	/* 192k for code + 192k for decrypted opcodes */
    /*TODO*///	ROM_LOAD( "mg3-01.9d",    0x00000, 0x08000, 0x04357973 )
    /*TODO*///	ROM_LOAD( "mg3-02.10d",   0x10000, 0x20000, 0x50d08da0 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "mg3-a.3k",     0x000000, 0x80000, 0x420f1de7 )	/* chars */
    /*TODO*///	ROM_LOAD( "mg3-b.4k",     0x080000, 0x80000, 0xd8de13fa )
    /*TODO*///	ROM_LOAD( "mg3-c.6k",     0x100000, 0x80000, 0xfbeb66e8 )
    /*TODO*///	ROM_LOAD( "mg3-d.7k",     0x180000, 0x80000, 0x8f6bd831 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x040000, REGION_GFX2 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "mg3-05.2g",    0x000000, 0x20000, 0x7a738d2d )	/* sprites */
    /*TODO*///	ROM_LOAD( "mg3-04.1g",    0x020000, 0x20000, 0x56f30515 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x80000, REGION_SOUND1 )	/* OKIM */
    /*TODO*///	ROM_LOAD( "mg3-e.1d",     0x00000, 0x80000, 0x106c2fa9 )	/* banked */
    /*TODO*///ROM_END
    /*TODO*///
    /*TODO*///ROM_START( qtono1 )
    /*TODO*///	ROM_REGION( 2*0x50000, REGION_CPU1 )	/* 320k for code + 320k for decrypted opcodes */
    /*TODO*///	ROM_LOAD( "q3-05.rom",    0x00000, 0x08000, 0x1dd0a344 )
    /*TODO*///	ROM_LOAD( "q3-06.rom",    0x10000, 0x20000, 0xbd6a2110 )
    /*TODO*///	ROM_LOAD( "q3-07.rom",    0x30000, 0x20000, 0x61e53c4f )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "q3-08.rom",    0x000000, 0x20000, 0x1533b978 )	/* chars */
    /*TODO*///	ROM_LOAD( "q3-09.rom",    0x020000, 0x20000, 0xa32db2f2 )
    /*TODO*///	ROM_LOAD( "q3-10.rom",    0x040000, 0x20000, 0xed681aa8 )
    /*TODO*///	ROM_LOAD( "q3-11.rom",    0x060000, 0x20000, 0x38b2fd10 )
    /*TODO*///	ROM_LOAD( "q3-18.rom",    0x080000, 0x20000, 0x9e4292ac )
    /*TODO*///	ROM_LOAD( "q3-19.rom",    0x0a0000, 0x20000, 0xb7f6d40f )
    /*TODO*///	ROM_LOAD( "q3-20.rom",    0x0c0000, 0x20000, 0x6cd7f38d )
    /*TODO*///	ROM_LOAD( "q3-21.rom",    0x0e0000, 0x20000, 0xb4aa6b4b )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x040000, REGION_GFX2 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "q3-16.rom",    0x000000, 0x20000, 0x863d6836 )	/* sprites */
    /*TODO*///	ROM_LOAD( "q3-17.rom",    0x020000, 0x20000, 0x459bf59c )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x80000, REGION_SOUND1 )	/* OKIM */
    /*TODO*///	ROM_LOAD( "q3-01.rom",    0x00000, 0x20000, 0x6c1be591 )
    /*TODO*///ROM_END
    /*TODO*///
    /*TODO*///ROM_START( qsangoku )
    /*TODO*///	ROM_REGION( 2*0x50000, REGION_CPU1 )	/* 320k for code + 320k for decrypted opcodes */
    /*TODO*///	ROM_LOAD( "q4-05c.rom",   0x00000, 0x08000, 0xe1d010b4 )
    /*TODO*///	ROM_LOAD( "q4-06.rom",    0x10000, 0x20000, 0xa0301849 )
    /*TODO*///	ROM_LOAD( "q4-07.rom",    0x30000, 0x20000, 0x2941ef5b )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "q4-08.rom",    0x000000, 0x20000, 0xdc84c6cb )	/* chars */
    /*TODO*///	ROM_LOAD( "q4-09.rom",    0x020000, 0x20000, 0xcbb6234c )
    /*TODO*///	ROM_LOAD( "q4-10.rom",    0x040000, 0x20000, 0xc20a27a8 )
    /*TODO*///	ROM_LOAD( "q4-11.rom",    0x060000, 0x20000, 0x4ff66aed )
    /*TODO*///	ROM_LOAD( "q4-18.rom",    0x080000, 0x20000, 0xca3acea5 )
    /*TODO*///	ROM_LOAD( "q4-19.rom",    0x0a0000, 0x20000, 0x1fd92b7d )
    /*TODO*///	ROM_LOAD( "q4-20.rom",    0x0c0000, 0x20000, 0xb02dc6a1 )
    /*TODO*///	ROM_LOAD( "q4-21.rom",    0x0e0000, 0x20000, 0x432b1dc1 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x040000, REGION_GFX2 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "q4-16.rom",    0x000000, 0x20000, 0x77342320 )	/* sprites */
    /*TODO*///	ROM_LOAD( "q4-17.rom",    0x020000, 0x20000, 0x1275c436 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x80000, REGION_SOUND1 )	/* OKIM */
    /*TODO*///	ROM_LOAD( "q4-01.rom",    0x00000, 0x20000, 0x5d0d07d8 )
    /*TODO*///ROM_END
    /*TODO*///
    /*TODO*///ROM_START( block )
    /*TODO*///	ROM_REGION( 2*0x50000, REGION_CPU1 )	/* 320k for code + 320k for decrypted opcodes */
    /*TODO*///	ROM_LOAD( "ble_05.rom",   0x00000, 0x08000, 0xc12e7f4c )
    /*TODO*///	ROM_LOAD( "ble_06.rom",   0x10000, 0x20000, 0xcdb13d55 )
    /*TODO*///	ROM_LOAD( "ble_07.rom",   0x30000, 0x20000, 0x1d114f13 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "bl_08.rom",    0x000000, 0x20000, 0xaa0f4ff1 )	/* chars */
    /*TODO*///	ROM_RELOAD(               0x040000, 0x20000 )
    /*TODO*///	ROM_LOAD( "bl_09.rom",    0x020000, 0x20000, 0x6fa8c186 )
    /*TODO*///	ROM_RELOAD(               0x060000, 0x20000 )
    /*TODO*///	ROM_LOAD( "bl_18.rom",    0x080000, 0x20000, 0xc0acafaf )
    /*TODO*///	ROM_RELOAD(               0x0c0000, 0x20000 )
    /*TODO*///	ROM_LOAD( "bl_19.rom",    0x0a0000, 0x20000, 0x1ae942f5 )
    /*TODO*///	ROM_RELOAD(               0x0e0000, 0x20000 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x040000, REGION_GFX2 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "bl_16.rom",    0x000000, 0x20000, 0xfadcaff7 )	/* sprites */
    /*TODO*///	ROM_LOAD( "bl_17.rom",    0x020000, 0x20000, 0x5f8cab42 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x80000, REGION_SOUND1 )	/* OKIM */
    /*TODO*///	ROM_LOAD( "bl_01.rom",    0x00000, 0x20000, 0xc2ec2abb )
    /*TODO*///ROM_END
    /*TODO*///
    /*TODO*///ROM_START( blockj )
    /*TODO*///	ROM_REGION( 2*0x50000, REGION_CPU1 )	/* 320k for code + 320k for decrypted opcodes */
    /*TODO*///	ROM_LOAD( "blj_05.rom",   0x00000, 0x08000, 0x3b55969a )
    /*TODO*///	ROM_LOAD( "ble_06.rom",   0x10000, 0x20000, 0xcdb13d55 )
    /*TODO*///	ROM_LOAD( "blj_07.rom",   0x30000, 0x20000, 0x1723883c )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "bl_08.rom",    0x000000, 0x20000, 0xaa0f4ff1 )	/* chars */
    /*TODO*///	ROM_RELOAD(               0x040000, 0x20000 )
    /*TODO*///	ROM_LOAD( "bl_09.rom",    0x020000, 0x20000, 0x6fa8c186 )
    /*TODO*///	ROM_RELOAD(               0x060000, 0x20000 )
    /*TODO*///	ROM_LOAD( "bl_18.rom",    0x080000, 0x20000, 0xc0acafaf )
    /*TODO*///	ROM_RELOAD(               0x0c0000, 0x20000 )
    /*TODO*///	ROM_LOAD( "bl_19.rom",    0x0a0000, 0x20000, 0x1ae942f5 )
    /*TODO*///	ROM_RELOAD(               0x0e0000, 0x20000 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x040000, REGION_GFX2 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "bl_16.rom",    0x000000, 0x20000, 0xfadcaff7 )	/* sprites */
    /*TODO*///	ROM_LOAD( "bl_17.rom",    0x020000, 0x20000, 0x5f8cab42 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x80000, REGION_SOUND1 )	/* OKIM */
    /*TODO*///	ROM_LOAD( "bl_01.rom",    0x00000, 0x20000, 0xc2ec2abb )
    /*TODO*///ROM_END
    /*TODO*///
    /*TODO*///ROM_START( blockbl )
    /*TODO*///	ROM_REGION( 2*0x50000, REGION_CPU1 )	/* 320k for code + 320k for decrypted opcodes */
    /*TODO*///	ROM_LOAD( "m7.l6",        0x50000, 0x08000, 0x3b576fd9 )   /* Decrypted opcode + data */
    /*TODO*///	ROM_CONTINUE(             0x00000, 0x08000 )
    /*TODO*///	ROM_LOAD( "m5.l3",        0x60000, 0x20000, 0x7c988bb7 )   /* Decrypted opcode + data */
    /*TODO*///	ROM_CONTINUE(             0x10000, 0x20000 )
    /*TODO*///	ROM_LOAD( "m6.l5",        0x30000, 0x20000, 0x5768d8eb )   /* Decrypted data */
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "m12.o10",      0x000000, 0x20000, 0x963154d9 )	/* chars */
    /*TODO*///	ROM_RELOAD(               0x040000, 0x20000 )
    /*TODO*///	ROM_LOAD( "m13.o14",      0x020000, 0x20000, 0x069480bb )
    /*TODO*///	ROM_RELOAD(               0x060000, 0x20000 )
    /*TODO*///	ROM_LOAD( "m4.j17",       0x080000, 0x20000, 0x9e3b6f4f )
    /*TODO*///	ROM_RELOAD(               0x0c0000, 0x20000 )
    /*TODO*///	ROM_LOAD( "m3.j20",       0x0a0000, 0x20000, 0x629d58fe )
    /*TODO*///	ROM_RELOAD(               0x0e0000, 0x20000 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x040000, REGION_GFX2 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "m11.o7",       0x000000, 0x10000, 0x255180a5 )	/* sprites */
    /*TODO*///	ROM_LOAD( "m10.o5",       0x010000, 0x10000, 0x3201c088 )
    /*TODO*///	ROM_LOAD( "m9.o3",        0x020000, 0x10000, 0x29357fe4 )
    /*TODO*///	ROM_LOAD( "m8.o2",        0x030000, 0x10000, 0xabd665d1 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x80000, REGION_SOUND1 )	/* OKIM */
    /*TODO*///	ROM_LOAD( "bl_01.rom",    0x00000, 0x20000, 0xc2ec2abb )
    /*TODO*///ROM_END
    /*TODO*///
    /*TODO*///
    /*TODO*///static void bootleg_decode(void)
    /*TODO*///{
    /*TODO*///	unsigned char *rom = memory_region(REGION_CPU1);
    /*TODO*///	int diff = memory_region_length(REGION_CPU1) / 2;
    /*TODO*///
    /*TODO*///	memory_set_opcode_base(0,rom+diff);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///static void init_dokaben(void)
    /*TODO*///{
    /*TODO*///	input_type = 0;
    /*TODO*///	nvram_size = 0;
    /*TODO*///	mgakuen2_decode();
    /*TODO*///}
	public static InitDriverPtr init_pang = new InitDriverPtr() { public void handler() 
	{
		input_type = 0;
		nvram_size = 0;
		pang_decode();
	} };
    /*TODO*///static void init_pangb(void)
    /*TODO*///{
    /*TODO*///	input_type = 0;
    /*TODO*///	nvram_size = 0;
    /*TODO*///	bootleg_decode();
    /*TODO*///}
    /*TODO*///static void init_cworld(void)
    /*TODO*///{
    /*TODO*///	input_type = 0;
    /*TODO*///	nvram_size = 0;
    /*TODO*///	cworld_decode();
    /*TODO*///}
    /*TODO*///static void init_hatena(void)
    /*TODO*///{
    /*TODO*///	input_type = 0;
    /*TODO*///	nvram_size = 0;
    /*TODO*///	hatena_decode();
    /*TODO*///}
    /*TODO*///static void init_spang(void)
    /*TODO*///{
    /*TODO*///	input_type = 3;
    /*TODO*///	nvram_size = 0x80;
    /*TODO*///	nvram = &memory_region(REGION_CPU1)[0xe000];	/* NVRAM */
    /*TODO*///	spang_decode();
    /*TODO*///}
    /*TODO*///static void init_sbbros(void)
    /*TODO*///{
    /*TODO*///	input_type = 3;
    /*TODO*///	nvram_size = 0x80;
    /*TODO*///	nvram = &memory_region(REGION_CPU1)[0xe000];	/* NVRAM */
    /*TODO*///	sbbros_decode();
    /*TODO*///}
    /*TODO*///static void init_qtono1(void)
    /*TODO*///{
    /*TODO*///	input_type = 0;
    /*TODO*///	nvram_size = 0;
    /*TODO*///	qtono1_decode();
    /*TODO*///}
    /*TODO*///static void init_qsangoku(void)
    /*TODO*///{
    /*TODO*///	input_type = 0;
    /*TODO*///	nvram_size = 0;
    /*TODO*///	qsangoku_decode();
    /*TODO*///}
    /*TODO*///static void init_mgakuen(void)
    /*TODO*///{
    /*TODO*///	input_type = 1;
    /*TODO*///}
        public static InitDriverPtr init_mgakuen2 = new InitDriverPtr() { public void handler() 
	{
            input_type = 1;
            nvram_size = 0;
            mgakuen2_decode();
        }};
    /*TODO*///static void init_marukin(void)
    /*TODO*///{
    /*TODO*///	input_type = 1;
    /*TODO*///	nvram_size = 0;
    /*TODO*///	marukin_decode();
    /*TODO*///}
    /*TODO*///static void init_block(void)
    /*TODO*///{
    /*TODO*///	input_type = 2;
    /*TODO*///	nvram_size = 0x80;
    /*TODO*///	nvram = &memory_region(REGION_CPU1)[0xff80];	/* NVRAM */
    /*TODO*///	block_decode();
    /*TODO*///}
    /*TODO*///static void init_blockbl(void)
    /*TODO*///{
    /*TODO*///	input_type = 2;
    /*TODO*///	nvram_size = 0x80;
    /*TODO*///	nvram = &memory_region(REGION_CPU1)[0xff80];	/* NVRAM */
    /*TODO*///	bootleg_decode();
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///GAME( 1988, mgakuen,  0,     mgakuen, mgakuen,  mgakuen,  ROT0,   "Yuga", "Mahjong Gakuen" )
    /*TODO*///GAME( 1989, mgakuen2, 0,     marukin, marukin,  mgakuen2, ROT0,   "Face", "Mahjong Gakuen 2 Gakuen-chou no Fukushuu" )
    public static GameDriver driver_pkladies	   = new GameDriver("1989"	,"pkladies"	,"mitchell.java"	,rom_pkladies,null	,machine_driver_marukin	,input_ports_pkladies	,init_mgakuen2	,ROT0	,	"Mitchell", "Poker Ladies" );
    /*TODO*///GAME( 1989, dokaben,  0,     pang,    pang,     dokaben,  ROT0,   "Capcom", "Dokaben (Japan)" )
    public static GameDriver driver_pang	   = new GameDriver("1989"	,"pang"	,"mitchell.java"	,rom_pang,null	,machine_driver_pang	,input_ports_pang	,init_pang	,ROT0	,	"Mitchell", "Pang (World)" );
    /*TODO*///GAME( 1989, pangb,    pang,  pang,    pang,     pangb,    ROT0,   "bootleg", "Pang (bootleg)" )
    /*TODO*///GAME( 1989, bbros,    pang,  pang,    pang,     pang,     ROT0,   "Capcom", "Buster Bros (US)" )
    /*TODO*///GAME( 1989, pompingw, pang,  pang,    pang,     pang,     ROT0,   "Mitchell", "Pomping World (Japan)" )
    /*TODO*///GAME( 1989, cworld,   0,     pang,    qtono1,   cworld,   ROT0,   "Capcom", "Capcom World (Japan)" )
    /*TODO*///GAME( 1990, hatena,   0,     pang,    qtono1,   hatena,   ROT0,   "Capcom", "Adventure Quiz 2 Hatena Hatena no Dai-Bouken (Japan)" )
    /*TODO*///GAME( 1990, spang,    0,     pang,    pang,     spang,    ROT0,   "Mitchell", "Super Pang (World)" )
    /*TODO*///GAME( 1990, sbbros,   spang, pang,    pang,     sbbros,   ROT0,   "Mitchell + Capcom", "Super Buster Bros (US)" )
    /*TODO*///GAME( 1990, marukin,  0,     marukin, marukin,  marukin,  ROT0,   "Yuga", "Super Marukin-Ban" )
    /*TODO*///GAME( 1991, qtono1,   0,     pang,    qtono1,   qtono1,   ROT0,   "Capcom", "Quiz Tonosama no Yabou (Japan)" )
    /*TODO*///GAME( 1991, qsangoku, 0,     pang,    qtono1,   qsangoku, ROT0,   "Capcom", "Quiz Sangokushi (Japan)" )
    /*TODO*///GAME( 1991, block,    0,     pang,    block,    block,    ROT270, "Capcom", "Block Block (World)" )
    /*TODO*///GAME( 1991, blockj,   block, pang,    block,    block,    ROT270, "Capcom", "Block Block (Japan)" )
    /*TODO*///GAME( 1991, blockbl,  block, pang,    block,    blockbl,  ROT270, "bootleg", "Block Block (bootleg)" )
    /*TODO*///    
}
