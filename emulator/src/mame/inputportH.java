
package mame;

public class inputportH {
/*TODO*////* input ports handling */
/*TODO*///
/*TODO*////* Don't confuse this with the I/O ports in memory.h. This is used to handle game */
/*TODO*////* inputs (joystick, coin slots, etc). Typically, you will read them using */
/*TODO*////* input_port_[n]_r(), which you will associate to the appropriate memory */
/*TODO*////* address or I/O port. */
/*TODO*///
/*TODO*////***************************************************************************/
/*TODO*///
/*TODO*///struct InputPortTiny
/*TODO*///{
/*TODO*///	UINT16 mask;			/* bits affected */
/*TODO*///	UINT16 default_value;	/* default value for the bits affected */
/*TODO*///							/* you can also use one of the IP_ACTIVE defines below */
/*TODO*///	UINT32 type;			/* see defines below */
/*TODO*///	const char *name;		/* name to display */
/*TODO*///};
/*TODO*///
/*TODO*///struct InputPort
/*TODO*///{
/*TODO*///	UINT16 mask;			/* bits affected */
/*TODO*///	UINT16 default_value;	/* default value for the bits affected */
/*TODO*///							/* you can also use one of the IP_ACTIVE defines below */
/*TODO*///	UINT32 type;			/* see defines below */
/*TODO*///	const char *name;		/* name to display */
/*TODO*///	InputSeq seq;                  	/* input sequence affecting the input bits */
/*TODO*///#ifdef MESS
/*TODO*///	UINT32 arg;				/* extra argument needed in some cases */
/*TODO*///	UINT16 min, max;		/* for analog controls */
/*TODO*///#endif
/*TODO*///};
/*TODO*///
/*TODO*///
    public static final int IP_ACTIVE_HIGH = 0x0000;
    public static final int IP_ACTIVE_LOW  = 0xffff;
/*TODO*///
/*TODO*///enum { IPT_END=1,IPT_PORT,
/*TODO*///	/* use IPT_JOYSTICK for panels where the player has one single joystick */
/*TODO*///	IPT_JOYSTICK_UP, IPT_JOYSTICK_DOWN, IPT_JOYSTICK_LEFT, IPT_JOYSTICK_RIGHT,
/*TODO*///	/* use IPT_JOYSTICKLEFT and IPT_JOYSTICKRIGHT for dual joystick panels */
/*TODO*///	IPT_JOYSTICKRIGHT_UP, IPT_JOYSTICKRIGHT_DOWN, IPT_JOYSTICKRIGHT_LEFT, IPT_JOYSTICKRIGHT_RIGHT,
/*TODO*///	IPT_JOYSTICKLEFT_UP, IPT_JOYSTICKLEFT_DOWN, IPT_JOYSTICKLEFT_LEFT, IPT_JOYSTICKLEFT_RIGHT,
/*TODO*///	IPT_BUTTON1, IPT_BUTTON2, IPT_BUTTON3, IPT_BUTTON4,	/* action buttons */
/*TODO*///	IPT_BUTTON5, IPT_BUTTON6, IPT_BUTTON7, IPT_BUTTON8, IPT_BUTTON9,
/*TODO*///
/*TODO*///	/* analog inputs */
/*TODO*///	/* the "arg" field contains the default sensitivity expressed as a percentage */
/*TODO*///	/* (100 = default, 50 = half, 200 = twice) */
/*TODO*///	IPT_ANALOG_START,
/*TODO*///	IPT_PADDLE, IPT_PADDLE_V,
/*TODO*///	IPT_DIAL, IPT_DIAL_V,
/*TODO*///	IPT_TRACKBALL_X, IPT_TRACKBALL_Y,
/*TODO*///	IPT_AD_STICK_X, IPT_AD_STICK_Y,
/*TODO*///	IPT_PEDAL,
/*TODO*///	IPT_ANALOG_END,
/*TODO*///
/*TODO*///	IPT_START1, IPT_START2, IPT_START3, IPT_START4,	/* start buttons */
/*TODO*///	IPT_COIN1, IPT_COIN2, IPT_COIN3, IPT_COIN4,	/* coin slots */
/*TODO*///	IPT_SERVICE1, IPT_SERVICE2, IPT_SERVICE3, IPT_SERVICE4,	/* service coin */
/*TODO*///	IPT_SERVICE, IPT_TILT,
/*TODO*///	IPT_DIPSWITCH_NAME, IPT_DIPSWITCH_SETTING,
/*TODO*////* Many games poll an input bit to check for vertical blanks instead of using */
/*TODO*////* interrupts. This special value allows you to handle that. If you set one of the */
/*TODO*////* input bits to this, the bit will be inverted while a vertical blank is happening. */
/*TODO*///	IPT_VBLANK,
/*TODO*///	IPT_UNKNOWN,
/*TODO*///	IPT_EXTENSION,	/* this is an extension on the previous InputPort, not a real inputport. */
/*TODO*///					/* It is used to store additional parameters for analog inputs */
/*TODO*///
/*TODO*///	/* the following are special codes for user interface handling - not to be used by drivers! */
/*TODO*///	IPT_UI_CONFIGURE,
/*TODO*///	IPT_UI_ON_SCREEN_DISPLAY,
/*TODO*///	IPT_UI_PAUSE,
/*TODO*///	IPT_UI_RESET_MACHINE,
/*TODO*///	IPT_UI_SHOW_GFX,
/*TODO*///	IPT_UI_FRAMESKIP_DEC,
/*TODO*///	IPT_UI_FRAMESKIP_INC,
/*TODO*///	IPT_UI_THROTTLE,
/*TODO*///	IPT_UI_SHOW_FPS,
/*TODO*///	IPT_UI_SNAPSHOT,
/*TODO*///	IPT_UI_TOGGLE_CHEAT,
/*TODO*///	IPT_UI_UP,
/*TODO*///	IPT_UI_DOWN,
/*TODO*///	IPT_UI_LEFT,
/*TODO*///	IPT_UI_RIGHT,
/*TODO*///	IPT_UI_SELECT,
/*TODO*///	IPT_UI_CANCEL,
/*TODO*///	IPT_UI_PAN_UP, IPT_UI_PAN_DOWN, IPT_UI_PAN_LEFT, IPT_UI_PAN_RIGHT,
/*TODO*///	IPT_UI_SHOW_PROFILER,
/*TODO*///	IPT_UI_SHOW_COLORS,
/*TODO*///	IPT_UI_TOGGLE_UI,
/*TODO*///	__ipt_max
/*TODO*///};
/*TODO*///
    
    public static final int IPF_MASK      = 0xffffff00;
    public static final int IPF_UNUSED    = 0x80000000;/* The bit is not used by this game, but is used */
    
    public static final int IPT_UNUSED    =  IPF_UNUSED;
    public static final int IPT_SPECIAL   =  IPT_UNUSED;	/* special meaning handled by custom functions */
									/* by other games running on the same hardware. */
									/* This is different from IPT_UNUSED, which marks */
									/* bits not connected to anything. */

    public static final int IPF_PLAYERMASK = 0x00030000;	/* use IPF_PLAYERn if more than one person can */
    public static final int IPF_PLAYER1    = 0;         	/* play at the same time. The IPT_ should be the same */
    public static final int IPF_PLAYER2    = 0x00010000;	/* for all players (e.g. IPT_BUTTON1 | IPF_PLAYER2) */
    public static final int IPF_PLAYER3    = 0x00020000;	/* IPF_PLAYER1 is the default and can be left out to */
    public static final int IPF_PLAYER4    = 0x00030000;	/* increase readability. */
    
    public static final int IPF_COCKTAIL  = IPF_PLAYER2;	/* the bit is used in cocktail mode only */

    public static final int IPF_CHEAT     = 0x40000000;	/* Indicates that the input bit is a "cheat" key */
									/* (providing invulnerabilty, level advance, and */
									/* so on). MAME will not recognize it when the */
									/* -nocheat command line option is specified. */

    public static final int IPF_8WAY    =   0;         	/* Joystick modes of operation. 8WAY is the default, */
    public static final int IPF_4WAY    =   0x00080000;	/* it prevents left/right or up/down to be pressed at */
    public static final int IPF_2WAY    =   0;         	/* the same time. 4WAY prevents diagonal directions. */
/*TODO*///									/* 2WAY should be used for joysticks wich move only */
/*TODO*///                                 	/* on one axis (e.g. Battle Zone) */
/*TODO*///
/*TODO*///#define IPF_IMPULSE    0x00100000	/* When this is set, when the key corrisponding to */
/*TODO*///									/* the input bit is pressed it will be reported as */
/*TODO*///									/* pressed for a certain number of video frames and */
/*TODO*///									/* then released, regardless of the real status of */
/*TODO*///									/* the key. This is useful e.g. for some coin inputs. */
/*TODO*///									/* The number of frames the signal should stay active */
/*TODO*///									/* is specified in the "arg" field. */
/*TODO*///#define IPF_TOGGLE     0x00200000	/* When this is set, the key acts as a toggle - press */
/*TODO*///									/* it once and it goes on, press it again and it goes off. */
/*TODO*///									/* useful e.g. for sone Test Mode dip switches. */
/*TODO*///#define IPF_REVERSE    0x00400000	/* By default, analog inputs like IPT_TRACKBALL increase */
/*TODO*///									/* when going right/up. This flag inverts them. */
/*TODO*///
/*TODO*///#define IPF_CENTER     0x00800000	/* always preload in->default, autocentering the STICK/TRACKBALL */
/*TODO*///
/*TODO*///#define IPF_CUSTOM_UPDATE 0x01000000 /* normally, analog ports are updated when they are accessed. */
/*TODO*///									/* When this flag is set, they are never updated automatically, */
/*TODO*///									/* it is the responsibility of the driver to call */
/*TODO*///									/* update_analog_port(int port). */
/*TODO*///
/*TODO*///#define IPF_RESETCPU   0x02000000	/* when the key is pressed, reset the first CPU */
/*TODO*///
/*TODO*///
/*TODO*////* The "arg" field contains 4 bytes fields */
/*TODO*///#define IPF_SENSITIVITY(percent)	((percent & 0xff) << 8)
/*TODO*///#define IPF_DELTA(val)				((val & 0xff) << 16)
/*TODO*///
/*TODO*///#define IP_GET_IMPULSE(port) (((port)->type >> 8) & 0xff)
/*TODO*///#define IP_GET_SENSITIVITY(port) ((((port)+1)->type >> 8) & 0xff)
/*TODO*///#define IP_SET_SENSITIVITY(port,val) ((port)+1)->type = ((port+1)->type & 0xffff00ff)|((val&0xff)<<8)
/*TODO*///#define IP_GET_DELTA(port) ((((port)+1)->type >> 16) & 0xff)
/*TODO*///#define IP_SET_DELTA(port,val) ((port)+1)->type = ((port+1)->type & 0xff00ffff)|((val&0xff)<<16)
/*TODO*///#define IP_GET_MIN(port) (((port)+1)->mask)
/*TODO*///#define IP_GET_MAX(port) (((port)+1)->default_value)
/*TODO*///#define IP_GET_CODE_OR1(port) ((port)->mask)
/*TODO*///#define IP_GET_CODE_OR2(port) ((port)->default_value)
/*TODO*///
/*TODO*///#define IP_NAME_DEFAULT ((const char *)-1)
/*TODO*///
/*TODO*////* Wrapper for compatibility */
/*TODO*///#define IP_KEY_DEFAULT CODE_DEFAULT
/*TODO*///#define IP_JOY_DEFAULT CODE_DEFAULT
/*TODO*///#define IP_KEY_PREVIOUS CODE_PREVIOUS
/*TODO*///#define IP_JOY_PREVIOUS CODE_PREVIOUS
/*TODO*///#define IP_KEY_NONE CODE_NONE
/*TODO*///#define IP_JOY_NONE CODE_NONE
/*TODO*///
/*TODO*////* start of table */
/*TODO*///#define INPUT_PORTS_START(name) \
/*TODO*///	static struct InputPortTiny input_ports_##name[] = {
/*TODO*///
/*TODO*////* end of table */
/*TODO*///#define INPUT_PORTS_END \
/*TODO*///	{ 0, 0, IPT_END, 0  } \
/*TODO*///	};
/*TODO*////* start of a new input port */
/*TODO*///#define PORT_START \
/*TODO*///	{ 0, 0, IPT_PORT, 0 },
/*TODO*///
/*TODO*////* input bit definition */
/*TODO*///#define PORT_BIT(mask,default,type) \
/*TODO*///	{ mask, default, type, IP_NAME_DEFAULT },
/*TODO*///
/*TODO*////* impulse input bit definition */
/*TODO*///#define PORT_BIT_IMPULSE(mask,default,type,duration) \
/*TODO*///	{ mask, default, type | IPF_IMPULSE | ((duration & 0xff) << 8), IP_NAME_DEFAULT },
/*TODO*///
/*TODO*////* key/joy code specification */
/*TODO*///#define PORT_CODE(key,joy) \
/*TODO*///	{ key, joy, IPT_EXTENSION, 0 },
/*TODO*///
/*TODO*////* input bit definition with extended fields */
/*TODO*///#define PORT_BITX(mask,default,type,name,key,joy) \
/*TODO*///	{ mask, default, type, name }, \
/*TODO*///	PORT_CODE(key,joy)
/*TODO*///
/*TODO*////* analog input */
/*TODO*///#define PORT_ANALOG(mask,default,type,sensitivity,delta,min,max) \
/*TODO*///	{ mask, default, type, IP_NAME_DEFAULT }, \
/*TODO*///	{ min, max, IPT_EXTENSION | IPF_SENSITIVITY(sensitivity) | IPF_DELTA(delta), IP_NAME_DEFAULT },
/*TODO*///
/*TODO*///#define PORT_ANALOGX(mask,default,type,sensitivity,delta,min,max,keydec,keyinc,joydec,joyinc) \
/*TODO*///	{ mask, default, type, IP_NAME_DEFAULT  }, \
/*TODO*///	{ min, max, IPT_EXTENSION | IPF_SENSITIVITY(sensitivity) | IPF_DELTA(delta), IP_NAME_DEFAULT }, \
/*TODO*///	PORT_CODE(keydec,joydec) \
/*TODO*///	PORT_CODE(keyinc,joyinc)
/*TODO*///
/*TODO*////* dip switch definition */
/*TODO*///#define PORT_DIPNAME(mask,default,name) \
/*TODO*///	{ mask, default, IPT_DIPSWITCH_NAME, name },
/*TODO*///
/*TODO*///#define PORT_DIPSETTING(default,name) \
/*TODO*///	{ 0, default, IPT_DIPSWITCH_SETTING, name },
/*TODO*///
/*TODO*///
/*TODO*///#define PORT_SERVICE(mask,default)	\
/*TODO*///	PORT_BITX(    mask, mask & default, IPT_DIPSWITCH_NAME | IPF_TOGGLE, DEF_STR( Service_Mode ), KEYCODE_F2, IP_JOY_NONE )	\
/*TODO*///	PORT_DIPSETTING(    mask & default, DEF_STR( Off ) )	\
/*TODO*///	PORT_DIPSETTING(    mask &~default, DEF_STR( On ) )
/*TODO*///
/*TODO*///#define MAX_DEFSTR_LEN 20
/*TODO*///extern char ipdn_defaultstrings[][MAX_DEFSTR_LEN];
/*TODO*///
/*TODO*///enum {
/*TODO*///	STR_Off,
/*TODO*///	STR_On,
/*TODO*///	STR_No,
/*TODO*///	STR_Yes,
/*TODO*///	STR_Lives,
/*TODO*///	STR_Bonus_Life,
/*TODO*///	STR_Difficulty,
/*TODO*///	STR_Demo_Sounds,
/*TODO*///	STR_Coinage,
/*TODO*///	STR_Coin_A,
/*TODO*///	STR_Coin_B,
/*TODO*///	STR_9C_1C,
/*TODO*///	STR_8C_1C,
/*TODO*///	STR_7C_1C,
/*TODO*///	STR_6C_1C,
/*TODO*///	STR_5C_1C,
/*TODO*///	STR_4C_1C,
/*TODO*///	STR_3C_1C,
/*TODO*///	STR_8C_3C,
/*TODO*///	STR_4C_2C,
/*TODO*///	STR_2C_1C,
/*TODO*///	STR_5C_3C,
/*TODO*///	STR_3C_2C,
/*TODO*///	STR_4C_3C,
/*TODO*///	STR_4C_4C,
/*TODO*///	STR_3C_3C,
/*TODO*///	STR_2C_2C,
/*TODO*///	STR_1C_1C,
/*TODO*///	STR_4C_5C,
/*TODO*///	STR_3C_4C,
/*TODO*///	STR_2C_3C,
/*TODO*///	STR_4C_7C,
/*TODO*///	STR_2C_4C,
/*TODO*///	STR_1C_2C,
/*TODO*///	STR_2C_5C,
/*TODO*///	STR_2C_6C,
/*TODO*///	STR_1C_3C,
/*TODO*///	STR_2C_7C,
/*TODO*///	STR_2C_8C,
/*TODO*///	STR_1C_4C,
/*TODO*///	STR_1C_5C,
/*TODO*///	STR_1C_6C,
/*TODO*///	STR_1C_7C,
/*TODO*///	STR_1C_8C,
/*TODO*///	STR_1C_9C,
/*TODO*///	STR_Free_Play,
/*TODO*///	STR_Cabinet,
/*TODO*///	STR_Upright,
/*TODO*///	STR_Cocktail,
/*TODO*///	STR_Flip_Screen,
/*TODO*///	STR_Service_Mode,
/*TODO*///	STR_Unused,
/*TODO*///	STR_Unknown,
/*TODO*///	STR_TOTAL
/*TODO*///};
/*TODO*///
/*TODO*///#define DEF_STR(str_num) (ipdn_defaultstrings[STR_##str_num])
/*TODO*///
    public static final int MAX_INPUT_PORTS =20;
/*TODO*///
/*TODO*///
/*TODO*///int load_input_port_settings(void);
/*TODO*///void save_input_port_settings(void);
/*TODO*///
/*TODO*///const char *input_port_name(const struct InputPort *in);
/*TODO*///InputSeq* input_port_type_seq(int type);
/*TODO*///InputSeq* input_port_seq(const struct InputPort *in);
/*TODO*///
/*TODO*///struct InputPort* input_port_allocate(const struct InputPortTiny *src);
/*TODO*///void input_port_free(struct InputPort* dst);
/*TODO*///
/*TODO*///
/*TODO*///void update_analog_port(int port);
/*TODO*///void update_input_ports(void);	/* called by cpuintrf.c - not for external use */
/*TODO*///void inputport_vblank_end(void);	/* called by cpuintrf.c - not for external use */
/*TODO*///
/*TODO*///int readinputport(int port);
/*TODO*///int input_port_0_r(int offset);
/*TODO*///int input_port_1_r(int offset);
/*TODO*///int input_port_2_r(int offset);
/*TODO*///int input_port_3_r(int offset);
/*TODO*///int input_port_4_r(int offset);
/*TODO*///int input_port_5_r(int offset);
/*TODO*///int input_port_6_r(int offset);
/*TODO*///int input_port_7_r(int offset);
/*TODO*///int input_port_8_r(int offset);
/*TODO*///int input_port_9_r(int offset);
/*TODO*///int input_port_10_r(int offset);
/*TODO*///int input_port_11_r(int offset);
/*TODO*///int input_port_12_r(int offset);
/*TODO*///int input_port_13_r(int offset);
/*TODO*///int input_port_14_r(int offset);
/*TODO*///int input_port_15_r(int offset);
/*TODO*///
/*TODO*///struct ipd
/*TODO*///{
/*TODO*///	UINT32 type;
/*TODO*///	const char *name;
/*TODO*///	InputSeq seq;
/*TODO*///};
/*TODO*///
}
