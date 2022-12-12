/***************************************************************************

  Capcom System ZN1/ZN2
  =====================

  Playstation Hardware with Q sound.

  Driver by smf. Currently only qsound is emulated.

Notes:

  This driver is based on information from the cps1/cps2 qsound driver,
  Miguel Angel Horna & Amuse. It works differently to the cps1/cps2 qsound,
  instead of shared memory two bytes are transferred using an i/o port.
  The 1/240th of a second timer either creates an nmi to tell the z80 there
  is data waiting or otherwise an irq.

  The I/O port used on zn1 always appears to be 0xf100. The zn2 games always
  set the bottom eight bits to zero, but the top eight bits are cycled
  0x00 - 0x1f for each access. This is probably to help synchronise the
  command queue. This is not currently emulated as it only uses 8 bit i/o port
  addressing.

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"
#include "cpu/z80/z80.h"

static int qcode;
static int qcode_last;
static int queue_data;
static int queue_len;

void qsound_queue_w( int offset, int data )
{
    queue_data = data;
    queue_len = 2;
}

static int zn_vh_start( void )
{
    return 0;
}

static void zn_vh_stop( void )
{
}

static void zn_vh_screenrefresh( struct osd_bitmap *bitmap, int full_refresh )
{
    int refresh = full_refresh;

    if( queue_len == 0 )
    {
        if( keyboard_pressed_memory( KEYCODE_UP ) )
        {
            qcode=( qcode & 0xff00 ) | ( ( qcode + 0x0001 ) & 0xff );
        }
        if( keyboard_pressed_memory( KEYCODE_DOWN ) )
        {
            qcode=( qcode & 0xff00 ) | ( ( qcode - 0x0001 ) & 0xff );
        }
        if( keyboard_pressed_memory( KEYCODE_RIGHT ) )
        {
            qcode=( ( qcode + 0x0100 ) & 0xff00 ) | ( qcode & 0xff );
        }
        if( keyboard_pressed_memory( KEYCODE_LEFT ) )
        {
            qcode=( ( qcode - 0x0100 ) & 0xff00 ) | ( qcode & 0xff );
        }
        if( qcode != qcode_last )
        {
            qsound_queue_w( 0, qcode );
            qcode_last = qcode;
            refresh = 1;
        }
    }

    if( refresh )
    {
        struct DisplayText dt[ 3 ];
        char *instructions = "SELECT WITH RIGHT&LEFT/UP&DN";
        char text1[ 256 ];
        sprintf( text1, "QSOUND CODE=%02x/%02x", qcode >> 8, qcode & 0xff );
        dt[ 0 ].text = text1;
        dt[ 0 ].color = DT_COLOR_RED;
        dt[ 0 ].x = ( Machine->uiwidth - Machine->uifontwidth * strlen( text1 ) ) / 2;
        dt[ 0 ].y = 8*23;
        dt[ 1 ].text = instructions;
        dt[ 1 ].color = DT_COLOR_WHITE;
        dt[ 1 ].x = ( Machine->uiwidth - Machine->uifontwidth * strlen( instructions ) ) / 2;
        dt[ 1 ].y = dt[ 0 ].y + 2 * Machine->uifontheight;

        dt[ 2 ].text = 0; /* terminate array */
        displaytext( dt, 0, 0 );
    }
}

static struct QSound_interface qsound_interface =
{
    QSOUND_CLOCK,
    REGION_SOUND1,
    { 100,100 }
};

static void qsound_banksw_w( int offset, int data )
{
    unsigned char *RAM = memory_region( REGION_CPU2 );
    cpu_setbank( 1, &RAM[ 0x10000 + ( ( data & 0x0f ) * 0x4000 ) ] );
}

static struct MemoryReadAddress qsound_readmem[] =
{
    { 0x0000, 0x7fff, MRA_ROM },
    { 0x8000, 0xbfff, MRA_BANK1 },  /* banked (contains music data) */
    { 0xd007, 0xd007, qsound_status_r },
    { 0xf000, 0xffff, MRA_RAM },
    { -1 }  /* end of table */
};

static struct MemoryWriteAddress qsound_writemem[] =
{
    { 0x0000, 0xbfff, MWA_ROM },
    { 0xd000, 0xd000, qsound_data_h_w },
    { 0xd001, 0xd001, qsound_data_l_w },
    { 0xd002, 0xd002, qsound_cmd_w },
    { 0xd003, 0xd003, qsound_banksw_w },
    { 0xf000, 0xffff, MWA_RAM },
    { -1 }  /* end of table */
};

static struct IOReadPort qsound_readport[] =
{
    { 0x00, 0x00, soundlatch_r },
    { -1 }
};

static struct GfxDecodeInfo zn_gfxdecodeinfo[] =
{
    { -1 } /* end of array */
};

static struct MemoryReadAddress zn_readmem[] =
{
    { 0x000000, 0x307ffff, MRA_ROM },
    { -1 }  /* end of table */
};

static struct MemoryWriteAddress zn_writemem[] =
{
    { -1 }  /* end of table */
};

int zn_interrupt( void )
{
    if( queue_len == 2 )
    {
        soundlatch_w( 0, queue_data >> 8 );
        queue_len--;
        return nmi_interrupt();
    }
    else if( queue_len == 1 )
    {
        soundlatch_w( 0, queue_data & 0xff );
        queue_len--;
        return nmi_interrupt();
    }
    return interrupt();
}

static void zn_init_machine( void )
{
    /* stop CPU1 as it's not really a z80. */
    timer_suspendcpu( 0, 1, SUSPEND_REASON_DISABLE );

    qcode = 0x0400;
    qcode_last = -1;
    queue_len = 0;
    qsound_banksw_w( 0, 0 );
}

static struct MachineDriver machine_driver_zn =
{
    /* basic machine hardware */
    {
        {
            CPU_Z80, /* CPU_MIPSR3000 ?? */
            33000000, /* 33mhz ?? */
            zn_readmem, zn_writemem, 0, 0,
            0, 1  /* ??? interrupts per frame */
        },
        {
            CPU_Z80 | CPU_AUDIO_CPU,
            8000000,  /* 8mhz ?? */
            qsound_readmem, qsound_writemem, qsound_readport, 0,
            zn_interrupt, 4 /* 4 interrupts per frame ?? */
        }
    },
    60, 0,
    1,
    zn_init_machine,

    /* video hardware */
    0x30*8+32*2, 0x1c*8+32*3, { 32, 32+0x30*8-1, 32+16, 32+16+0x1c*8-1 },
    zn_gfxdecodeinfo,
    4096,
    4096,
    0,

    VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
    0,
    zn_vh_start,
    zn_vh_stop,
    zn_vh_screenrefresh,

    /* sound hardware */
    SOUND_SUPPORTS_STEREO,0,0,0,
    { { SOUND_QSOUND, &qsound_interface } }
};

INPUT_PORTS_START( zn )
    PORT_START      /* IN0 */
    PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER1 )
    PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER2 )
    PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE )	/* pause */
    PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_SERVICE  )	/* pause */
    PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 )
    PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 )
    PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON6 | IPF_PLAYER1 )
    PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON6 | IPF_PLAYER2  )

    PORT_START      /* DSWA */
    PORT_DIPNAME( 0xff, 0xff, DEF_STR( Unknown ) )
    PORT_DIPSETTING(    0xff, DEF_STR( Off ) )
    PORT_DIPSETTING(    0x00, DEF_STR( On ) )

    PORT_START      /* DSWB */
    PORT_DIPNAME( 0xff, 0xff, DEF_STR( Unknown ) )
    PORT_DIPSETTING(    0xff, DEF_STR( Off ) )
    PORT_DIPSETTING(    0x00, DEF_STR( On ) )

    PORT_START      /* DSWC */
    PORT_DIPNAME( 0xff, 0xff, DEF_STR( Unknown ) )
    PORT_DIPSETTING(    0xff, DEF_STR( Off ) )
    PORT_DIPSETTING(    0x00, DEF_STR( On ) )

    PORT_START      /* Player 1 */
    PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 )
    PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1 )
    PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1 )
    PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1 )
    PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 )
    PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 )
    PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 )
    PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 )

    PORT_START      /* Player 2 */
    PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 )
    PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2 )
    PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2 )
    PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2 )
    PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 )
    PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 )
    PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 )
    PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 )
INPUT_PORTS_END

#define CODE_SIZE ( 0x3080000 )

ROM_START( rvschool )
    ROM_REGION( CODE_SIZE, REGION_CPU1 )      /* MIPS R3000 */
    ROM_LOAD( "jst-04a", 0x0000000, 0x080000, 0x034b1011 )
    ROM_LOAD( "jst-05m", 0x0080000, 0x400000, 0x723372b8 )
    ROM_LOAD( "jst-06m", 0x0480000, 0x400000, 0x4248988e )
    ROM_LOAD( "jst-07m", 0x0880000, 0x400000, 0xc84c5a16 )
    ROM_LOAD( "jst-08m", 0x0C80000, 0x400000, 0x791b57f3 )
    ROM_LOAD( "jst-09m", 0x1080000, 0x400000, 0x6df42048 )
    ROM_LOAD( "jst-10m", 0x1480000, 0x400000, 0xd7e22769 )
    ROM_LOAD( "jst-11m", 0x1880000, 0x400000, 0x0a033ac5 )
    ROM_LOAD( "jst-12m", 0x1c80000, 0x400000, 0x43bd2ddd )
    ROM_LOAD( "jst-13m", 0x2080000, 0x400000, 0x6b443235 )

    ROM_REGION( 0x50000, REGION_CPU2 ) /* 64k for the audio CPU (+banks) */
    ROM_LOAD( "jst-02",  0x00000, 0x08000, 0x7809e2c3 )
    ROM_CONTINUE(        0x10000, 0x18000 )
    ROM_LOAD( "jst-03",  0x28000, 0x20000, 0x860ff24d )

    ROM_REGION( 0x400000, REGION_SOUND1 ) /* Q Sound Samples */
    ROM_LOAD( "jst-01m", 0x0000000, 0x400000, 0x9a7c98f9 )
ROM_END

ROM_START( jgakuen )
    ROM_REGION( CODE_SIZE, REGION_CPU1 )      /* MIPS R3000 */
    ROM_LOAD( "jst-04j",  0x000000, 0x80000, 0x28b8000a )
    ROM_LOAD( "jst-05m", 0x0080000, 0x400000, 0x723372b8 )
    ROM_LOAD( "jst-06m", 0x0480000, 0x400000, 0x4248988e )
    ROM_LOAD( "jst-07m", 0x0880000, 0x400000, 0xc84c5a16 )
    ROM_LOAD( "jst-08m", 0x0C80000, 0x400000, 0x791b57f3 )
    ROM_LOAD( "jst-09m", 0x1080000, 0x400000, 0x6df42048 )
    ROM_LOAD( "jst-10m", 0x1480000, 0x400000, 0xd7e22769 )
    ROM_LOAD( "jst-11m", 0x1880000, 0x400000, 0x0a033ac5 )
    ROM_LOAD( "jst-12m", 0x1c80000, 0x400000, 0x43bd2ddd )
    ROM_LOAD( "jst-13m", 0x2080000, 0x400000, 0x6b443235 )

    ROM_REGION( 0x50000, REGION_CPU2 ) /* 64k for the audio CPU (+banks) */
    ROM_LOAD( "jst-02",  0x00000, 0x08000, 0x7809e2c3 )
    ROM_CONTINUE(        0x10000, 0x18000 )
    ROM_LOAD( "jst-03",  0x28000, 0x20000, 0x860ff24d )

    ROM_REGION( 0x400000, REGION_SOUND1 ) /* Q Sound Samples */
    ROM_LOAD( "jst-01m", 0x0000000, 0x400000, 0x9a7c98f9 )
ROM_END

ROM_START( sfex )
    ROM_REGION( CODE_SIZE, REGION_CPU1 )      /* MIPS R3000 */
    ROM_LOAD( "sfe-04a", 0x0000000, 0x080000, 0x08247bd4 )
    ROM_LOAD( "sfe-05m", 0x0080000, 0x400000, 0xeab781fe )
    ROM_LOAD( "sfe-06m", 0x0480000, 0x400000, 0x999de60c )
    ROM_LOAD( "sfe-07m", 0x0880000, 0x400000, 0x76117b0a )
    ROM_LOAD( "sfe-08m", 0x0C80000, 0x400000, 0xa36bbec5 )
    ROM_LOAD( "sfe-09m", 0x1080000, 0x400000, 0x62c424cc )
    ROM_LOAD( "sfe-10m", 0x1480000, 0x400000, 0x83791a8b )

    ROM_REGION( 0x50000, REGION_CPU2 ) /* 64k for the audio CPU (+banks) */
    ROM_LOAD( "sfe-02",  0x00000, 0x08000, 0x1908475c )
    ROM_CONTINUE(        0x10000, 0x18000 )
    ROM_LOAD( "sfe-03",  0x28000, 0x20000, 0x95c1e2e0 )

    ROM_REGION( 0x400000, REGION_SOUND1 ) /* Q Sound Samples */
    ROM_LOAD( "sfe-01m", 0x0000000, 0x400000, 0xf5afff0d )
ROM_END

ROM_START( sfexj )
    ROM_REGION( CODE_SIZE, REGION_CPU1 )      /* MIPS R3000 */
    ROM_LOAD( "sfe-04j", 0x0000000, 0x080000, 0xea100607 )
    ROM_LOAD( "sfe-05m", 0x0080000, 0x400000, 0xeab781fe )
    ROM_LOAD( "sfe-06m", 0x0480000, 0x400000, 0x999de60c )
    ROM_LOAD( "sfe-07m", 0x0880000, 0x400000, 0x76117b0a )
    ROM_LOAD( "sfe-08m", 0x0C80000, 0x400000, 0xa36bbec5 )
    ROM_LOAD( "sfe-09m", 0x1080000, 0x400000, 0x62c424cc )
    ROM_LOAD( "sfe-10m", 0x1480000, 0x400000, 0x83791a8b )

    ROM_REGION( 0x50000, REGION_CPU2 ) /* 64k for the audio CPU (+banks) */
    ROM_LOAD( "sfe-02",  0x00000, 0x08000, 0x1908475c )
    ROM_CONTINUE(        0x10000, 0x18000 )
    ROM_LOAD( "sfe-03",  0x28000, 0x20000, 0x95c1e2e0 )

    ROM_REGION( 0x400000, REGION_SOUND1 ) /* Q Sound Samples */
    ROM_LOAD( "sfe-01m", 0x0000000, 0x400000, 0xf5afff0d )
ROM_END

ROM_START( sfexp )
    ROM_REGION( CODE_SIZE, REGION_CPU1 )      /* MIPS R3000 */
    ROM_LOAD( "sfp-04e", 0x0000000, 0x080000, 0x305e4ec0 )
    ROM_LOAD( "sfp-05",  0x0080000, 0x400000, 0xac7dcc5e )
    ROM_LOAD( "sfp-06",  0x0480000, 0x400000, 0x1d504758 )
    ROM_LOAD( "sfp-07",  0x0880000, 0x400000, 0x0f585f30 )
    ROM_LOAD( "sfp-08",  0x0C80000, 0x400000, 0x65eabc61 )
    ROM_LOAD( "sfp-09",  0x1080000, 0x400000, 0x15f8b71e )
    ROM_LOAD( "sfp-10",  0x1480000, 0x400000, 0xc1ecf652 )

    ROM_REGION( 0x50000, REGION_CPU2 ) /* 64k for the audio CPU (+banks) */
    ROM_LOAD( "sfe-02",  0x00000, 0x08000, 0x1908475c )
    ROM_CONTINUE(        0x10000, 0x18000 )
    ROM_LOAD( "sfe-03",  0x28000, 0x20000, 0x95c1e2e0 )

    ROM_REGION( 0x400000, REGION_SOUND1 ) /* Q Sound Samples */
    ROM_LOAD( "sfe-01m", 0x0000000, 0x400000, 0xf5afff0d )
ROM_END

ROM_START( sfexpj )
    ROM_REGION( CODE_SIZE, REGION_CPU1 )      /* MIPS R3000 */
    ROM_LOAD( "sfp-04j", 0x0000000, 0x080000, 0x18d043f5 )
    ROM_LOAD( "sfp-05",  0x0080000, 0x400000, 0xac7dcc5e )
    ROM_LOAD( "sfp-06",  0x0480000, 0x400000, 0x1d504758 )
    ROM_LOAD( "sfp-07",  0x0880000, 0x400000, 0x0f585f30 )
    ROM_LOAD( "sfp-08",  0x0C80000, 0x400000, 0x65eabc61 )
    ROM_LOAD( "sfp-09",  0x1080000, 0x400000, 0x15f8b71e )
    ROM_LOAD( "sfp-10",  0x1480000, 0x400000, 0xc1ecf652 )

    ROM_REGION( 0x50000, REGION_CPU2 ) /* 64k for the audio CPU (+banks) */
    ROM_LOAD( "sfe-02",  0x00000, 0x08000, 0x1908475c )
    ROM_CONTINUE(        0x10000, 0x18000 )
    ROM_LOAD( "sfe-03",  0x28000, 0x20000, 0x95c1e2e0 )

    ROM_REGION( 0x400000, REGION_SOUND1 ) /* Q Sound Samples */
    ROM_LOAD( "sfe-01m", 0x0000000, 0x400000, 0xf5afff0d )
ROM_END

ROM_START( sfex2 )
    ROM_REGION( CODE_SIZE, REGION_CPU1 )      /* MIPS R3000 */
    ROM_LOAD( "ex2j-04", 0x0000000, 0x080000, 0x5d603586 )
    ROM_LOAD( "ex2-05m", 0x0080000, 0x800000, 0x78726b17 )
    ROM_LOAD( "ex2-06m", 0x0880000, 0x800000, 0xbe1075ed )
    ROM_LOAD( "ex2-07m", 0x1080000, 0x800000, 0x6496c6ed )
    ROM_LOAD( "ex2-08m", 0x1880000, 0x800000, 0x3194132e )
    ROM_LOAD( "ex2-09m", 0x2080000, 0x400000, 0x075ae585 )

    ROM_REGION( 0x50000, REGION_CPU2 ) /* 64k for the audio CPU (+banks) */
    ROM_LOAD( "ex2-02",  0x00000, 0x08000, 0x9489875e )
    ROM_CONTINUE(        0x10000, 0x18000 )

    ROM_REGION( 0x400000, REGION_SOUND1 ) /* Q Sound Samples */
    ROM_LOAD( "ex2-01m", 0x0000000, 0x400000, 0x14a5bb0e )
ROM_END

ROM_START( sfex2p )
    ROM_REGION( CODE_SIZE, REGION_CPU1 )      /* MIPS R3000 */
    ROM_LOAD( "sf2p-04", 0x0000000, 0x080000, 0xc6d0aea3 )
    ROM_LOAD( "sf2p-05", 0x0080000, 0x800000, 0x4ee3110f )
    ROM_LOAD( "sf2p-06", 0x0880000, 0x800000, 0x4cd53a45 )
    ROM_LOAD( "sf2p-07", 0x1080000, 0x800000, 0x11207c2a )
    ROM_LOAD( "sf2p-08", 0x1880000, 0x800000, 0x3560c2cc )
    ROM_LOAD( "sf2p-09", 0x2080000, 0x800000, 0x344aa227 )
    ROM_LOAD( "sf2p-10", 0x2880000, 0x800000, 0x2eef5931 )

    ROM_REGION( 0x50000, REGION_CPU2 ) /* 64k for the audio CPU (+banks) */
    ROM_LOAD( "sf2p-02", 0x00000, 0x08000, 0x3705de5e )
    ROM_CONTINUE(        0x10000, 0x18000 )
    ROM_LOAD( "sf2p-03", 0x28000, 0x20000, 0x6ae828f6 )

    ROM_REGION( 0x400000, REGION_SOUND1 ) /* Q Sound Samples */
    ROM_LOAD( "ex2-01m", 0x0000000, 0x400000, 0x14a5bb0e )
ROM_END

ROM_START( tgmj )
    ROM_REGION( CODE_SIZE, REGION_CPU1 )      /* MIPS R3000 */
    ROM_LOAD( "ate-04j", 0x0000000, 0x080000, 0xbb4bbb96 )
    ROM_LOAD( "ate-05",  0x0080000, 0x400000, 0x50977f5a )
    ROM_LOAD( "ate-06",  0x0480000, 0x400000, 0x05973f16 )

    ROM_REGION( 0x50000, REGION_CPU2 ) /* 64k for the audio CPU (+banks) */
    ROM_LOAD( "ate-02",  0x00000, 0x08000, 0xf4f6e82f )
    ROM_CONTINUE(        0x10000, 0x18000 )

    ROM_REGION( 0x400000, REGION_SOUND1 ) /* Q Sound Samples */
    ROM_LOAD( "ate-01",  0x0000000, 0x400000, 0xa21c6521 )
ROM_END

ROM_START( ts2j )
    ROM_REGION( CODE_SIZE, REGION_CPU1 )      /* MIPS R3000 */
    ROM_LOAD( "ts2j-04", 0x0000000, 0x080000, 0x4aba8c5e )
    ROM_LOAD( "ts2-05",  0x0080000, 0x400000, 0x7f4228e2 )
    ROM_LOAD( "ts2-06m", 0x0480000, 0x400000, 0xcd7e0a27 )
    ROM_LOAD( "ts2-08m", 0x0880000, 0x400000, 0xb1f7f115 )
    ROM_LOAD( "ts2-10",  0x0C80000, 0x200000, 0xad90679a )

    ROM_REGION( 0x50000, REGION_CPU2 ) /* 64k for the audio CPU (+banks) */
    ROM_LOAD( "ts2-02",  0x00000, 0x08000, 0x2f45c461 )
    ROM_CONTINUE(        0x10000, 0x18000 )

    ROM_REGION( 0x400000, REGION_SOUND1 ) /* Q Sound Samples */
    ROM_LOAD( "ts2-01",  0x0000000, 0x400000, 0xd7a505e0 )
ROM_END


GAME( 1995, ts2j,     0,        zn, zn, 0, ROT0, "Capcom/Takara", "Battle Arena Toshinden 2 (JAPAN 951124)" )
GAME( 1996, sfex,     0,        zn, zn, 0, ROT0, "Capcom/Arika", "Street Fighter EX (ASIA 961219)" )
GAME( 1996, sfexj,    sfex,     zn, zn, 0, ROT0, "Capcom/Arika", "Street Fighter EX (JAPAN 961130)" )
GAME( 1997, sfexp,    0,        zn, zn, 0, ROT0, "Capcom/Arika", "Street Fighter EX Plus (USA 970311)" )
GAME( 1997, sfexpj,   sfexp,    zn, zn, 0, ROT0, "Capcom/Arika", "Street Fighter EX Plus (JAPAN 970311)" )
GAME( 1997, rvschool, 0,        zn, zn, 0, ROT0, "Capcom", "Rival Schools (ASIA 971117)" )
GAME( 1997, jgakuen,  rvschool, zn, zn, 0, ROT0, "Capcom", "Justice Gakuen (JAPAN 971117)" )
GAME( 1998, sfex2,    0,        zn, zn, 0, ROT0, "Capcom/Arika", "Street Fighter EX 2 (JAPAN 980312)" )
GAME( 1998, tgmj,     0,        zn, zn, 0, ROT0, "Capcom/Akira", "Tetris The Grand Master (JAPAN 980710)" )
GAME( 1999, sfex2p,   sfex2,    zn, zn, 0, ROT0, "Capcom/Arika", "Street Fighter EX 2 Plus (JAPAN 990611)" )
