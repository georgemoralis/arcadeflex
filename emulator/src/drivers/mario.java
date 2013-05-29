
package drivers;

import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
import static mame.inputport.*;
import static vidhrdw.generic.*;
import static vidhrdw.mario.*;

public class mario {
    static int p[] = { 0,0xf0,0,0,0,0,0,0 };
    static int t[] = { 0,0 };

/*TODO*///
/*TODO*///
/*TODO*///#define ACTIVELOW_PORT_BIT(P,A,D)   ((P & (~(1 << A))) | ((D ^ 1) << A))
/*TODO*///#define ACTIVEHIGH_PORT_BIT(P,A,D)   ((P & (~(1 << A))) | (D << A))
/*TODO*///
/*TODO*///
/*TODO*///void mario_sh_growing(int offset, int data)    { t[1] = data; }
/*TODO*///void mario_sh_getcoin(int offset, int data)    { t[0] = data; }
/*TODO*///void mario_sh_crab(int offset, int data)       { p[1] = ACTIVEHIGH_PORT_BIT(p[1],0,data); }
/*TODO*///void mario_sh_turtle(int offset, int data)     { p[1] = ACTIVEHIGH_PORT_BIT(p[1],1,data); }
/*TODO*///void mario_sh_fly(int offset, int data)        { p[1] = ACTIVEHIGH_PORT_BIT(p[1],2,data); }
/*TODO*///static void mario_sh_tuneselect(int offset, int data) { soundlatch_w(offset,data); }
/*TODO*///
    public static ReadHandlerPtr mario_sh_getp1 = new ReadHandlerPtr() { public int handler(int offset) { return p[1]; }};
    public static ReadHandlerPtr mario_sh_getp2 = new ReadHandlerPtr() { public int handler(int offset) { return p[2]; }};
    public static ReadHandlerPtr mario_sh_gett0 = new ReadHandlerPtr() { public int handler(int offset) { return t[0]; }};
    public static ReadHandlerPtr mario_sh_gett1 = new ReadHandlerPtr() { public int handler(int offset) { return t[1]; }};
/*TODO*///static int  mario_sh_gettune(int offset) { return soundlatch_r(offset); }
/*TODO*///
/*TODO*///static void mario_sh_putsound(int offset, int data)
/*TODO*///{
/*TODO*///	DAC_data_w(0,data);
/*TODO*///}
/*TODO*///static void mario_sh_putp1(int offset, int data)
/*TODO*///{
/*TODO*///	p[1] = data;
/*TODO*///}
/*TODO*///static void mario_sh_putp2(int offset, int data)
/*TODO*///{
/*TODO*///	p[2] = data;
/*TODO*///}
/*TODO*///void masao_sh_irqtrigger_w(int offset,int data)
/*TODO*///{
/*TODO*///	static int last;
/*TODO*///
/*TODO*///
/*TODO*///	if (last == 1 && data == 0)
/*TODO*///	{
/*TODO*///		/* setting bit 0 high then low triggers IRQ on the sound CPU */
/*TODO*///		cpu_cause_interrupt(1,0xff);
/*TODO*///	}
/*TODO*///
/*TODO*///	last = data;
/*TODO*///}
/*TODO*///
    static MemoryReadAddress readmem[] =
    {
            new MemoryReadAddress( 0x0000, 0x5fff, MRA_ROM ),
            new MemoryReadAddress( 0x6000, 0x6fff, MRA_RAM ),
            new MemoryReadAddress( 0x7400, 0x77ff, MRA_RAM ),	/* video RAM */
            new MemoryReadAddress( 0x7c00, 0x7c00, input_port_0_r ),	/* IN0 */
            new MemoryReadAddress( 0x7c80, 0x7c80, input_port_1_r ),	/* IN1 */
            new MemoryReadAddress( 0x7f80, 0x7f80, input_port_2_r ),	/* DSW */
            new MemoryReadAddress( 0xf000, 0xffff, MRA_ROM ),
            new MemoryReadAddress( -1 )	/* end of table */
    };



    static MemoryWriteAddress writemem[] =
    {
            new MemoryWriteAddress( 0x0000, 0x5fff, MWA_ROM ),
            new MemoryWriteAddress( 0x6000, 0x68ff, MWA_RAM ),
            new MemoryWriteAddress( 0x6a80, 0x6fff, MWA_RAM ),
            new MemoryWriteAddress( 0x6900, 0x6a7f, MWA_RAM, spriteram, spriteram_size ),
            new MemoryWriteAddress( 0x7400, 0x77ff, videoram_w, videoram, videoram_size ),
/*TODO*///            new MemoryWriteAddress( 0x7c00, 0x7c00, mario_sh1_w ), /* Mario run sample */
/*TODO*///            new MemoryWriteAddress( 0x7c80, 0x7c80, mario_sh2_w ), /* Luigi run sample */
            new MemoryWriteAddress( 0x7d00, 0x7d00, MWA_RAM, mario_scrolly ),
            new MemoryWriteAddress( 0x7e80, 0x7e80, mario_gfxbank_w ),
            new MemoryWriteAddress( 0x7e83, 0x7e83, mario_palettebank_w ),
/*TODO*///            new MemoryWriteAddress( 0x7e84, 0x7e84, interrupt_enable_w ),
/*TODO*///            new MemoryWriteAddress( 0x7f00, 0x7f00, mario_sh_w ),	/* death */
/*TODO*///            new MemoryWriteAddress( 0x7f01, 0x7f01, mario_sh_getcoin ),
/*TODO*///            new MemoryWriteAddress( 0x7f03, 0x7f03, mario_sh_crab ),
/*TODO*///            new MemoryWriteAddress( 0x7f04, 0x7f04, mario_sh_turtle ),
/*TODO*///            new MemoryWriteAddress( 0x7f05, 0x7f05, mario_sh_fly ),
/*TODO*///            new MemoryWriteAddress( 0x7f00, 0x7f07, mario_sh3_w ), /* Misc discrete samples */
/*TODO*///            new MemoryWriteAddress( 0x7e00, 0x7e00, mario_sh_tuneselect ),
            new MemoryWriteAddress( 0x7000, 0x73ff, MWA_NOP ),	/* ??? */
    //	{ 0x7e85, 0x7e85, MWA_RAM },	/* Sets alternative 1 and 0 */
            new MemoryWriteAddress( 0xf000, 0xffff, MWA_ROM ),
            new MemoryWriteAddress( -1 )	/* end of table */
    };

    static MemoryWriteAddress masao_writemem[] =
    {
            new MemoryWriteAddress( 0x0000, 0x5fff, MWA_ROM ),
            new MemoryWriteAddress( 0x6000, 0x68ff, MWA_RAM ),
            new MemoryWriteAddress( 0x6a80, 0x6fff, MWA_RAM ),
            new MemoryWriteAddress( 0x6900, 0x6a7f, MWA_RAM, spriteram, spriteram_size ),
            new MemoryWriteAddress( 0x7400, 0x77ff, videoram_w, videoram, videoram_size ),
            new MemoryWriteAddress( 0x7d00, 0x7d00, MWA_RAM, mario_scrolly ),
/*TODO*///            new MemoryWriteAddress( 0x7e00, 0x7e00, soundlatch_w ),
            new MemoryWriteAddress( 0x7e80, 0x7e80, mario_gfxbank_w ),
            new MemoryWriteAddress( 0x7e83, 0x7e83, mario_palettebank_w ),
/*TODO*///            new MemoryWriteAddress( 0x7e84, 0x7e84, interrupt_enable_w ),
            new MemoryWriteAddress( 0x7000, 0x73ff, MWA_NOP ),	/* ??? */
/*TODO*///            new MemoryWriteAddress( 0x7f00, 0x7f00, masao_sh_irqtrigger_w ),
            new MemoryWriteAddress( 0xf000, 0xffff, MWA_ROM ),
            new MemoryWriteAddress( -1 )	/* end of table */
    };

    static IOWritePort mario_writeport[] =
    {
            new IOWritePort( 0x00,   0x00,   IOWP_NOP ),  /* unknown... is this a trigger? */
            new IOWritePort( -1 )	/* end of table */
    };

    static MemoryReadAddress readmem_sound[] =
    {
            new MemoryReadAddress( 0x0000, 0x0fff, MRA_ROM ),
            new MemoryReadAddress( -1 )	/* end of table */
    };
    static MemoryWriteAddress writemem_sound[] =
    {
            new MemoryWriteAddress( 0x0000, 0x0fff, MWA_ROM ),
            new MemoryWriteAddress( -1 )	/* end of table */
    };
    static IOReadPort readport_sound[] =
    {
/*TODO*///            new IOReadPort(  0x00,     0xff,     mario_sh_gettune ),
 /*TODO*///           { I8039_p1, I8039_p1, mario_sh_getp1 },
 /*TODO*///           { I8039_p2, I8039_p2, mario_sh_getp2 },
 /*TODO*///           { I8039_t0, I8039_t0, mario_sh_gett0 },
 /*TODO*///           { I8039_t1, I8039_t1, mario_sh_gett1 },
            new IOReadPort( -1 )	/* end of table */
    };
    static IOWritePort writeport_sound[] =
    {
/*TODO*///           new IOWritePort( 0x00,     0xff,     mario_sh_putsound ),
/*TODO*///           new IOWritePort( I8039_p1, I8039_p1, mario_sh_putp1 ),
/*TODO*///           new IOWritePort( I8039_p2, I8039_p2, mario_sh_putp2 ),
           new IOWritePort( -1 )	/* end of table */
    };



/*TEMPHACK*/       static InputPortPtr input_ports_mario = new InputPortPtr(){ public void handler() { }};

/*TODO*///INPUT_PORTS_START( mario )
/*TODO*///	PORT_START      /* IN0 */
/*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY )
/*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY )
/*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
/*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
/*TODO*///	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
/*TODO*///	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_START1 )
/*TODO*///	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_START2 )
/*TODO*///	PORT_BITX(0x80, IP_ACTIVE_HIGH, IPT_SERVICE, DEF_STR( Service_Mode ), KEYCODE_F2, IP_JOY_NONE )
/*TODO*///
/*TODO*///	PORT_START      /* IN1 */
/*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
/*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2 )
/*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
/*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
/*TODO*///	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 )
/*TODO*///	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_COIN2 )
/*TODO*///	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_COIN1 )
/*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )
/*TODO*///
/*TODO*///	PORT_START      /* DSW0 */
/*TODO*///	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Lives ) )
/*TODO*///	PORT_DIPSETTING(    0x00, "3" )
/*TODO*///	PORT_DIPSETTING(    0x01, "4" )
/*TODO*///	PORT_DIPSETTING(    0x02, "5" )
/*TODO*///	PORT_DIPSETTING(    0x03, "6" )
/*TODO*///	PORT_DIPNAME( 0x0c, 0x00, DEF_STR( Coinage ) )
/*TODO*///	PORT_DIPSETTING(    0x04, DEF_STR( 2C_1C ) )
/*TODO*///	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
/*TODO*///	PORT_DIPSETTING(    0x08, DEF_STR( 1C_2C ) )
/*TODO*///	PORT_DIPSETTING(    0x0c, DEF_STR( 1C_3C ) )
/*TODO*///	PORT_DIPNAME( 0x30, 0x00, DEF_STR( Bonus_Life ) )
/*TODO*///	PORT_DIPSETTING(    0x00, "20000" )
/*TODO*///	PORT_DIPSETTING(    0x10, "30000" )
/*TODO*///	PORT_DIPSETTING(    0x20, "40000" )
/*TODO*///	PORT_DIPSETTING(    0x30, "None" )
/*TODO*///	PORT_DIPNAME( 0xc0, 0x00, DEF_STR( Difficulty ) )
/*TODO*///	PORT_DIPSETTING(    0x00, "Easy" )
/*TODO*///	PORT_DIPSETTING(    0x40, "Medium" )
/*TODO*///	PORT_DIPSETTING(    0x80, "Hard" )
/*TODO*///	PORT_DIPSETTING(    0xc0, "Hardest" )
/*TODO*///INPUT_PORTS_END
/*TODO*///
/*TODO*///INPUT_PORTS_START( mariojp )
/*TODO*///	PORT_START      /* IN0 */
/*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY )
/*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY )
/*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
/*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
/*TODO*///	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
/*TODO*///	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_START1 )
/*TODO*///	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_START2 )
/*TODO*///	PORT_BITX(0x80, IP_ACTIVE_HIGH, IPT_SERVICE, DEF_STR( Service_Mode ), KEYCODE_F2, IP_JOY_NONE )
/*TODO*///
/*TODO*///	PORT_START      /* IN1 */
/*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
/*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2 )
/*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
/*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
/*TODO*///	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 )
/*TODO*///	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_COIN1 )
/*TODO*///	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_COIN2 )	/* doesn't work in game, but does in service mode */
/*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )
/*TODO*///
/*TODO*///	PORT_START      /* DSW0 */
/*TODO*///	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Lives ) )
/*TODO*///	PORT_DIPSETTING(    0x00, "3" )
/*TODO*///	PORT_DIPSETTING(    0x01, "4" )
/*TODO*///	PORT_DIPSETTING(    0x02, "5" )
/*TODO*///	PORT_DIPSETTING(    0x03, "6" )
/*TODO*///	PORT_DIPNAME( 0x1c, 0x00, DEF_STR( Coinage ) )
/*TODO*///	PORT_DIPSETTING(    0x08, DEF_STR( 3C_1C ) )
/*TODO*///	PORT_DIPSETTING(    0x10, DEF_STR( 2C_1C ) )
/*TODO*///	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
/*TODO*///	PORT_DIPSETTING(    0x18, DEF_STR( 1C_2C ) )
/*TODO*///	PORT_DIPSETTING(    0x04, DEF_STR( 1C_3C ) )
/*TODO*///	PORT_DIPSETTING(    0x0c, DEF_STR( 1C_4C ) )
/*TODO*///	PORT_DIPSETTING(    0x14, DEF_STR( 1C_5C ) )
/*TODO*///	PORT_DIPSETTING(    0x1c, DEF_STR( 1C_6C ) )
/*TODO*///	PORT_DIPNAME( 0x20, 0x20, "2 Players Game" )
/*TODO*///	PORT_DIPSETTING(    0x00, "1 Credit" )
/*TODO*///	PORT_DIPSETTING(    0x20, "2 Credits" )
/*TODO*///	PORT_DIPNAME( 0xc0, 0x00, DEF_STR( Bonus_Life ) )
/*TODO*///	PORT_DIPSETTING(    0x00, "20000" )
/*TODO*///	PORT_DIPSETTING(    0x40, "30000" )
/*TODO*///	PORT_DIPSETTING(    0x80, "40000" )
/*TODO*///	PORT_DIPSETTING(    0xc0, "None" )
/*TODO*///INPUT_PORTS_END
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///static struct GfxLayout charlayout =
/*TODO*///{
/*TODO*///	8,8,	/* 8*8 characters */
/*TODO*///	512,	/* 512 characters */
/*TODO*///	2,	/* 2 bits per pixel */
/*TODO*///	{ 512*8*8, 0 },	/* the bitplanes are separated */
/*TODO*///	{ 0, 1, 2, 3, 4, 5, 6, 7 },	/* pretty straightforward layout */
/*TODO*///	{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
/*TODO*///	8*8	/* every char takes 8 consecutive bytes */
/*TODO*///};
/*TODO*///
/*TODO*///
/*TODO*///static struct GfxLayout spritelayout =
/*TODO*///{
/*TODO*///	16,16,	/* 16*16 sprites */
/*TODO*///	256,	/* 256 sprites */
/*TODO*///	3,	/* 3 bits per pixel */
/*TODO*///	{ 2*256*16*16, 256*16*16, 0 },	/* the bitplanes are separated */
/*TODO*///	{ 0, 1, 2, 3, 4, 5, 6, 7,		/* the two halves of the sprite are separated */
/*TODO*///			256*16*8+0, 256*16*8+1, 256*16*8+2, 256*16*8+3, 256*16*8+4, 256*16*8+5, 256*16*8+6, 256*16*8+7 },
/*TODO*///	{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
/*TODO*///			8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
/*TODO*///	16*8	/* every sprite takes 16 consecutive bytes */
/*TODO*///};
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///static struct GfxDecodeInfo gfxdecodeinfo[] =
/*TODO*///{
/*TODO*///	{ REGION_GFX1, 0, &charlayout,      0, 16 },
/*TODO*///	{ REGION_GFX2, 0, &spritelayout, 16*4, 32 },
/*TODO*///	{ -1 } /* end of array */
/*TODO*///};
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///static struct DACinterface dac_interface =
/*TODO*///{
/*TODO*///	1,
/*TODO*///	{ 100 }
/*TODO*///};
/*TODO*///
/*TODO*///static const char *mario_sample_names[] =
/*TODO*///{
/*TODO*///	"*mario",
/*TODO*///
/*TODO*///	/* 7f01 - 7f07 sounds */
/*TODO*///	"ice.wav",    /* 0x02 ice appears (formerly effect0.wav) */
/*TODO*///	"coin.wav",   /* 0x06 coin appears (formerly effect1.wav) */
/*TODO*///	"skid.wav",   /* 0x07 skid */
/*TODO*///
/*TODO*///	/* 7c00 */
/*TODO*///	"run.wav",        /* 03, 02, 01 - 0x1b */
/*TODO*///
/*TODO*///	/* 7c80 */
/*TODO*///	"luigirun.wav",   /* 03, 02, 01 - 0x1c */
/*TODO*///
/*TODO*///    0	/* end of array */
/*TODO*///};
/*TODO*///
/*TODO*///static struct Samplesinterface samples_interface =
/*TODO*///{
/*TODO*///	3,	/* 3 channels */
/*TODO*///	25,	/* volume */
/*TODO*///	mario_sample_names
/*TODO*///};
/*TODO*///
/*TODO*///static struct AY8910interface ay8910_interface =
/*TODO*///{
/*TODO*///	1,      /* 1 chip */
/*TODO*///	14318000/6,	/* ? */
/*TODO*///	{ 50 },
/*TODO*///	{ soundlatch_r },
/*TODO*///	{ 0 },
/*TODO*///	{ 0 },
/*TODO*///	{ 0 }
/*TODO*///};
/*TODO*///
/*TODO*///static struct MemoryReadAddress masao_sound_readmem[] =
/*TODO*///{
/*TODO*///	{ 0x0000, 0x0fff, MRA_ROM },
/*TODO*///	{ 0x2000, 0x23ff, MRA_RAM },
/*TODO*///	{ 0x4000, 0x4000, AY8910_read_port_0_r },
/*TODO*///	{ -1 }  /* end of table */
/*TODO*///};
/*TODO*///
/*TODO*///static struct MemoryWriteAddress masao_sound_writemem[] =
/*TODO*///{
/*TODO*///	{ 0x0000, 0x0fff, MWA_ROM },
/*TODO*///	{ 0x2000, 0x23ff, MWA_RAM },
/*TODO*///	{ 0x6000, 0x6000, AY8910_control_port_0_w },
/*TODO*///	{ 0x4000, 0x4000, AY8910_write_port_0_w },
/*TODO*///	{ -1 }  /* end of table */
/*TODO*///};
/*TODO*///
/*TODO*///
/*TEMPHACK*//*dummy machine drivers*/
    static MachineDriver machine_driver_mario = new MachineDriver
    (
        new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				3072000,	/* 3.072 Mhz (?) */
				readmem, writemem, null, null,
				null, 1
			),
                new MachineCPU(
			CPU_I8039 | CPU_AUDIO_CPU,
                        730000,         /* 730 khz */
			null,null,null,null,
			null,1
		)
		},
		60,0  
    );
/*TODO*///static struct MachineDriver machine_driver_mario =
/*TODO*///{
/*TODO*///	/* basic machine hardware */
/*TODO*///	{
/*TODO*///		{
/*TODO*///			CPU_Z80,
/*TODO*///			3072000,	/* 3.072 Mhz (?) */
/*TODO*///			readmem,writemem,0,mario_writeport,
/*TODO*///			nmi_interrupt,1
/*TODO*///		},
/*TODO*///		{
/*TODO*///			CPU_I8039 | CPU_AUDIO_CPU,
/*TODO*///                        730000,         /* 730 khz */
/*TODO*///			readmem_sound,writemem_sound,readport_sound,writeport_sound,
/*TODO*///			ignore_interrupt,1
/*TODO*///		}
/*TODO*///	},
/*TODO*///	60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
/*TODO*///	1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
/*TODO*///	0,
/*TODO*///
/*TODO*///	/* video hardware */
/*TODO*///	32*8, 32*8, { 0*8, 32*8-1, 2*8, 30*8-1 },
/*TODO*///	gfxdecodeinfo,
/*TODO*///	256,16*4+32*8,
/*TODO*///	mario_vh_convert_color_prom,
/*TODO*///
/*TODO*///	VIDEO_TYPE_RASTER,
/*TODO*///	0,
/*TODO*///	generic_vh_start,
/*TODO*///	generic_vh_stop,
/*TODO*///	mario_vh_screenrefresh,
/*TODO*///
/*TODO*///	/* sound hardware */
/*TODO*///	0,0,0,0,
/*TODO*///	{
/*TODO*///		{
/*TODO*///			SOUND_DAC,
/*TODO*///			&dac_interface
/*TODO*///		},
/*TODO*///		{
/*TODO*///			SOUND_SAMPLES,
/*TODO*///			&samples_interface
/*TODO*///		}
/*TODO*///	}
/*TODO*///};
/*TODO*///
/*TEMPHACK*//*dummy machine drivers*/
     static MachineDriver machine_driver_masao = new MachineDriver
    (
                  new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				3072000,	/* 3.072 Mhz (?) */
				readmem, writemem, null, null,
				null, 1
			),
                        new MachineCPU(
			CPU_Z80 | CPU_AUDIO_CPU,
                        730000,         /* 730 khz */
			null,null,null,null,
			null,1
                        )
		},
		60,0  
     );   
/*TODO*///static struct MachineDriver machine_driver_masao =
/*TODO*///{
/*TODO*///	/* basic machine hardware */
/*TODO*///	{
/*TODO*///		{
/*TODO*///			CPU_Z80,
/*TODO*///			4000000,        /* 4.000 Mhz (?) */
/*TODO*///			readmem,masao_writemem,0,0,
/*TODO*///			nmi_interrupt,1
/*TODO*///		},
/*TODO*///		{
/*TODO*///			CPU_Z80 | CPU_AUDIO_CPU,
/*TODO*///			24576000/16,	/* ???? */
/*TODO*///			masao_sound_readmem,masao_sound_writemem,0,0,
/*TODO*///			ignore_interrupt,1
/*TODO*///		}
/*TODO*///
/*TODO*///		},
/*TODO*///	60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
/*TODO*///	1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
/*TODO*///	0,
/*TODO*///
/*TODO*///	/* video hardware */
/*TODO*///	32*8, 32*8, { 0*8, 32*8-1, 2*8, 30*8-1 },
/*TODO*///	gfxdecodeinfo,
/*TODO*///	256,16*4+32*8,
/*TODO*///	mario_vh_convert_color_prom,
/*TODO*///
/*TODO*///	VIDEO_TYPE_RASTER,
/*TODO*///	0,
/*TODO*///	generic_vh_start,
/*TODO*///	generic_vh_stop,
/*TODO*///	mario_vh_screenrefresh,
/*TODO*///
/*TODO*///	/* sound hardware */
/*TODO*///	0,0,0,0,
/*TODO*///	{
/*TODO*///		{
/*TODO*///			SOUND_AY8910,
/*TODO*///			&ay8910_interface
/*TODO*///		}
/*TODO*///	}
/*TODO*///};
/*TODO*///
/*TODO*///
/*TODO*///
    /***************************************************************************

      Game driver(s)

    /***************************************************************************/
    static RomLoadPtr rom_mario = new RomLoadPtr(){ public void handler(){
	ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for code */
	ROM_LOAD( "mario.7f",     0x0000, 0x2000, 0xc0c6e014 );
	ROM_LOAD( "mario.7e",     0x2000, 0x2000, 0x116b3856 );
	ROM_LOAD( "mario.7d",     0x4000, 0x2000, 0xdcceb6c1 );
	ROM_LOAD( "mario.7c",     0xf000, 0x1000, 0x4a63d96b );

	ROM_REGION( 0x1000, REGION_CPU2 );	/* sound */
	ROM_LOAD( "tma1c-a.6k",   0x0000, 0x1000, 0x06b9ff85 );

	ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );
	ROM_LOAD( "mario.3f",     0x0000, 0x1000, 0x28b0c42c );
	ROM_LOAD( "mario.3j",     0x1000, 0x1000, 0x0c8cc04d );

	ROM_REGION( 0x6000, REGION_GFX2 | REGIONFLAG_DISPOSE );
	ROM_LOAD( "mario.7m",     0x0000, 0x1000, 0x22b7372e );
	ROM_LOAD( "mario.7n",     0x1000, 0x1000, 0x4f3a1f47 );
	ROM_LOAD( "mario.7p",     0x2000, 0x1000, 0x56be6ccd );
	ROM_LOAD( "mario.7s",     0x3000, 0x1000, 0x56f1d613 );
	ROM_LOAD( "mario.7t",     0x4000, 0x1000, 0x641f0008 );
	ROM_LOAD( "mario.7u",     0x5000, 0x1000, 0x7baf5309 );

	ROM_REGION( 0x0200, REGION_PROMS );
	ROM_LOAD( "mario.4p",     0x0000, 0x0200, 0xafc9bd41 );
        ROM_END();
    }};

    static RomLoadPtr rom_mariojp = new RomLoadPtr(){ public void handler(){
    	ROM_REGION( 0x10000, REGION_CPU1 ); /* 64k for code */
	ROM_LOAD( "tma1c-a1.7f",  0x0000, 0x2000, 0xb64b6330 );
	ROM_LOAD( "tma1c-a2.7e",  0x2000, 0x2000, 0x290c4977 );
	ROM_LOAD( "tma1c-a1.7d",  0x4000, 0x2000, 0xf8575f31 );
	ROM_LOAD( "tma1c-a2.7c",  0xf000, 0x1000, 0xa3c11e9e );

	ROM_REGION( 0x1000, REGION_CPU2 );	/* sound */
	ROM_LOAD( "tma1c-a.6k",   0x0000, 0x1000, 0x06b9ff85 );

	ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );
	ROM_LOAD( "tma1v-a.3f",   0x0000, 0x1000, 0xadf49ee0 );
	ROM_LOAD( "tma1v-a.3j",   0x1000, 0x1000, 0xa5318f2d );

	ROM_REGION( 0x6000, REGION_GFX2 | REGIONFLAG_DISPOSE );
	ROM_LOAD( "tma1v-a.7m",   0x0000, 0x1000, 0x186762f8 );
	ROM_LOAD( "tma1v-a.7n",   0x1000, 0x1000, 0xe0e08bba );
	ROM_LOAD( "tma1v-a.7p",   0x2000, 0x1000, 0x7b27c8c1 );
	ROM_LOAD( "tma1v-a.7s",   0x3000, 0x1000, 0x912ba80a );
	ROM_LOAD( "tma1v-a.7t",   0x4000, 0x1000, 0x5cbb92a5 );
	ROM_LOAD( "tma1v-a.7u",   0x5000, 0x1000, 0x13afb9ed );

	ROM_REGION( 0x0200, REGION_PROMS );
	ROM_LOAD( "mario.4p",     0x0000, 0x0200, 0xafc9bd41 );
        ROM_END();
    }};

    static RomLoadPtr rom_masao = new RomLoadPtr(){ public void handler(){
	ROM_REGION( 0x10000, REGION_CPU1 ); /* 64k for code */
	ROM_LOAD( "masao-4.rom",  0x0000, 0x2000, 0x07a75745 );
	ROM_LOAD( "masao-3.rom",  0x2000, 0x2000, 0x55c629b6 );
	ROM_LOAD( "masao-2.rom",  0x4000, 0x2000, 0x42e85240 );
	ROM_LOAD( "masao-1.rom",  0xf000, 0x1000, 0xb2817af9 );

	ROM_REGION( 0x10000, REGION_CPU2 ); /* 64k for sound */
	ROM_LOAD( "masao-5.rom",  0x0000, 0x1000, 0xbd437198 );

	ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );
	ROM_LOAD( "masao-6.rom",  0x0000, 0x1000, 0x1c9e0be2 );
	ROM_LOAD( "masao-7.rom",  0x1000, 0x1000, 0x747c1349 );

	ROM_REGION( 0x6000, REGION_GFX2 | REGIONFLAG_DISPOSE );
	ROM_LOAD( "tma1v-a.7m",   0x0000, 0x1000, 0x186762f8 );
	ROM_LOAD( "masao-9.rom",  0x1000, 0x1000, 0x50be3918 );
	ROM_LOAD( "mario.7p",     0x2000, 0x1000, 0x56be6ccd );
	ROM_LOAD( "tma1v-a.7s",   0x3000, 0x1000, 0x912ba80a );
	ROM_LOAD( "tma1v-a.7t",   0x4000, 0x1000, 0x5cbb92a5 );
	ROM_LOAD( "tma1v-a.7u",   0x5000, 0x1000, 0x13afb9ed );

	ROM_REGION( 0x0200, REGION_PROMS );
	ROM_LOAD( "mario.4p",     0x0000, 0x0200, 0xafc9bd41 );
        ROM_END();
    }};

    public static GameDriver driver_mario   = new GameDriver("1983","mario"  ,"mario.java", rom_mario  , null        , machine_driver_mario, input_ports_mario, null, ROT180, "Nintendo of America", "Mario Bros. (US)" );
    public static GameDriver driver_mariojp = new GameDriver("1983","mariojp","mario.java", rom_mariojp, driver_mario, machine_driver_mario, input_ports_mario, null, ROT180, "Nintendo"           , "Mario Bros. (Japan)");
    public static GameDriver driver_masao   = new GameDriver("1983","masao"  ,"mario.java", rom_masao  , driver_mario, machine_driver_masao, input_ports_mario, null, ROT180, "bootleg"            , "Masao" );        

}
