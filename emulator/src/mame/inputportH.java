package mame;

import java.util.ArrayList;
import static mame.inputH.*;
import static mame.inputport.ipdn_defaultstrings;
import static mame.mame.*;

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

    public static class InputPortTiny {

        public InputPortTiny(int mk, int def, int tp, String nm) {
            mask = mk;
            default_value = def;
            type = tp;
            name = nm;
        }
        public int /*UINT16*/ mask;			/* bits affected */

        public int /*UINT16*/ default_value;	/* default value for the bits affected */
        /* you can also use one of the IP_ACTIVE defines below */

        public int /*UINT32*/ type;			/* see defines below */

        public String name;		/* name to display */

    }

    public static class InputPort {

        public int /*UINT16*/ mask;			/* bits affected */

        public int /*UINT16*/ default_value;	/* default value for the bits affected */
        /* you can also use one of the IP_ACTIVE defines below */

        public int /*UINT32*/ type;			/* see defines below */

        public String name;		/* name to display */

        public int[] seq = new int[SEQ_MAX]; /* input sequence affecting the input bits */


        public InputPort() {
            for (int i = 0; i < SEQ_MAX; i++) {
                seq[i] = 0;
            }
        }
    }
    public static final int IP_ACTIVE_HIGH = 0x0000;
    public static final int IP_ACTIVE_LOW = 0xffff;

    public static final int IPT_END = 1;
    public static final int IPT_PORT = 2;
    /* use IPT_JOYSTICK for panels where the player has one single joystick */
    public static final int IPT_JOYSTICK_UP = 3;
    public static final int IPT_JOYSTICK_DOWN = 4;
    public static final int IPT_JOYSTICK_LEFT = 5;
    public static final int IPT_JOYSTICK_RIGHT = 6;
    /* use IPT_JOYSTICKLEFT and IPT_JOYSTICKRIGHT for dual joystick panels */
    public static final int IPT_JOYSTICKRIGHT_UP = 7;
    public static final int IPT_JOYSTICKRIGHT_DOWN = 8;
    public static final int IPT_JOYSTICKRIGHT_LEFT = 9;
    public static final int IPT_JOYSTICKRIGHT_RIGHT = 10;
    public static final int IPT_JOYSTICKLEFT_UP = 11;
    public static final int IPT_JOYSTICKLEFT_DOWN = 12;
    public static final int IPT_JOYSTICKLEFT_LEFT = 13;
    public static final int IPT_JOYSTICKLEFT_RIGHT = 14;
    public static final int IPT_BUTTON1 = 15;
    public static final int IPT_BUTTON2 = 16;
    public static final int IPT_BUTTON3 = 17;
    public static final int IPT_BUTTON4 = 18;
    /* action buttons */
    public static final int IPT_BUTTON5 = 19;
    public static final int IPT_BUTTON6 = 20;
    public static final int IPT_BUTTON7 = 21;
    public static final int IPT_BUTTON8 = 22;
    public static final int IPT_BUTTON9 = 23;

    /* analog inputs */
    /* the "arg" field contains the default sensitivity expressed as a percentage */
    /* (100 = default, 50 = half, 200 = twice) */
    public static final int IPT_ANALOG_START = 24;
    public static final int IPT_PADDLE = 25;
    public static final int IPT_PADDLE_V = 26;
    public static final int IPT_DIAL = 27;
    public static final int IPT_DIAL_V = 28;
    public static final int IPT_TRACKBALL_X = 29;
    public static final int IPT_TRACKBALL_Y = 30;
    public static final int IPT_AD_STICK_X = 31;
    public static final int IPT_AD_STICK_Y = 32;
    public static final int IPT_PEDAL = 33;
    public static final int IPT_ANALOG_END = 34;

    public static final int IPT_START1 = 35;
    public static final int IPT_START2 = 36;
    public static final int IPT_START3 = 37;
    public static final int IPT_START4 = 38;	/* start buttons */

    public static final int IPT_COIN1 = 39;
    public static final int IPT_COIN2 = 40;
    public static final int IPT_COIN3 = 41;
    public static final int IPT_COIN4 = 42;	/* coin slots */

    public static final int IPT_SERVICE1 = 43;
    public static final int IPT_SERVICE2 = 44;
    public static final int IPT_SERVICE3 = 45;
    public static final int IPT_SERVICE4 = 46;	/* service coin */

    public static final int IPT_SERVICE = 47;
    public static final int IPT_TILT = 48;
    public static final int IPT_DIPSWITCH_NAME = 49;
    public static final int IPT_DIPSWITCH_SETTING = 50;
    /* Many games poll an input bit to check for vertical blanks instead of using */
    /* interrupts. This special value allows you to handle that. If you set one of the */
    /* input bits to this, the bit will be inverted while a vertical blank is happening. */
    public static final int IPT_VBLANK = 51;
    public static final int IPT_UNKNOWN = 52;
    public static final int IPT_EXTENSION = 53;	/* this is an extension on the previous InputPort, not a real inputport. */
    /* It is used to store additional parameters for analog inputs */

    /* the following are special codes for user interface handling - not to be used by drivers! */
    public static final int IPT_UI_CONFIGURE = 54;
    public static final int IPT_UI_ON_SCREEN_DISPLAY = 55;
    public static final int IPT_UI_PAUSE = 56;
    public static final int IPT_UI_RESET_MACHINE = 57;
    public static final int IPT_UI_SHOW_GFX = 58;
    public static final int IPT_UI_FRAMESKIP_DEC = 59;
    public static final int IPT_UI_FRAMESKIP_INC = 60;
    public static final int IPT_UI_THROTTLE = 61;
    public static final int IPT_UI_SHOW_FPS = 62;
    public static final int IPT_UI_SNAPSHOT = 63;
    public static final int IPT_UI_TOGGLE_CHEAT = 64;
    public static final int IPT_UI_UP = 65;
    public static final int IPT_UI_DOWN = 66;
    public static final int IPT_UI_LEFT = 67;
    public static final int IPT_UI_RIGHT = 68;
    public static final int IPT_UI_SELECT = 69;
    public static final int IPT_UI_CANCEL = 70;
    public static final int IPT_UI_PAN_UP = 71;
    public static final int IPT_UI_PAN_DOWN = 72;
    public static final int IPT_UI_PAN_LEFT = 73;
    public static final int IPT_UI_PAN_RIGHT = 74;
    public static final int IPT_UI_SHOW_PROFILER = 75;
    public static final int IPT_UI_SHOW_COLORS = 76;
    public static final int IPT_UI_TOGGLE_UI = 77;
    public static final int __ipt_max = 78;

    public static final int IPF_MASK = 0xffffff00;
    public static final int IPF_UNUSED = 0x80000000;/* The bit is not used by this game, but is used */

    public static final int IPT_UNUSED = IPF_UNUSED;
    public static final int IPT_SPECIAL = IPT_UNUSED;	/* special meaning handled by custom functions */
    /* by other games running on the same hardware. */
    /* This is different from IPT_UNUSED, which marks */
    /* bits not connected to anything. */

    public static final int IPF_PLAYERMASK = 0x00030000;	/* use IPF_PLAYERn if more than one person can */

    public static final int IPF_PLAYER1 = 0;         	/* play at the same time. The IPT_ should be the same */

    public static final int IPF_PLAYER2 = 0x00010000;	/* for all players (e.g. IPT_BUTTON1 | IPF_PLAYER2) */

    public static final int IPF_PLAYER3 = 0x00020000;	/* IPF_PLAYER1 is the default and can be left out to */

    public static final int IPF_PLAYER4 = 0x00030000;	/* increase readability. */

    public static final int IPF_COCKTAIL = IPF_PLAYER2;	/* the bit is used in cocktail mode only */

    public static final int IPF_CHEAT = 0x40000000;	/* Indicates that the input bit is a "cheat" key */
    /* (providing invulnerabilty, level advance, and */
    /* so on). MAME will not recognize it when the */
    /* -nocheat command line option is specified. */

    public static final int IPF_8WAY = 0;         	/* Joystick modes of operation. 8WAY is the default, */

    public static final int IPF_4WAY = 0x00080000;	/* it prevents left/right or up/down to be pressed at */

    public static final int IPF_2WAY = 0;         	/* the same time. 4WAY prevents diagonal directions. */
    /* 2WAY should be used for joysticks wich move only */
    /* on one axis (e.g. Battle Zone) */

    public static final int IPF_IMPULSE = 0x00100000;	/* When this is set, when the key corrisponding to */
    /* the input bit is pressed it will be reported as */
    /* pressed for a certain number of video frames and */
    /* then released, regardless of the real status of */
    /* the key. This is useful e.g. for some coin inputs. */
    /* The number of frames the signal should stay active */
    /* is specified in the "arg" field. */

    public static final int IPF_TOGGLE = 0x00200000;	/* When this is set, the key acts as a toggle - press */
    /* it once and it goes on, press it again and it goes off. */
    /* useful e.g. for sone Test Mode dip switches. */

    public static final int IPF_REVERSE = 0x00400000;	/* By default, analog inputs like IPT_TRACKBALL increase */
    /* when going right/up. This flag inverts them. */

    public static final int IPF_CENTER = 0x00800000;	/* always preload in->default, autocentering the STICK/TRACKBALL */

    public static final int IPF_CUSTOM_UPDATE = 0x01000000; /* normally, analog ports are updated when they are accessed. */
    /* When this flag is set, they are never updated automatically, */
    /* it is the responsibility of the driver to call */
    /* update_analog_port(int port). */

    public static final int IPF_RESETCPU = 0x02000000;	/* when the key is pressed, reset the first CPU */


    /*TODO*////* The "arg" field contains 4 bytes fields */
/*TODO*///#define IPF_SENSITIVITY(percent)	((percent & 0xff) << 8)
/*TODO*///#define IPF_DELTA(val)				((val & 0xff) << 16)
/*TODO*///
    public static int IPF_SENSITIVITY(int percent) { return ((percent & 0xff) << 8); }
    public static int IPF_DELTA(int val) { return ((val & 0xff) << 16); }
    public static int IP_GET_IMPULSE(int port) {
        return ((Machine.input_ports[port].type >> 8) & 0xff);
    }
    /*TODO*///#define IP_GET_IMPULSE(port) (((port)->type >> 8) & 0xff)

    public static int IP_GET_SENSITIVITY(int port) {
        return (Machine.input_ports[port + 1].type >> 8) & 0xff;
    }

    public static int IP_GET_SENSITIVITY(InputPort[] ports, int index) {
        return (ports[index + 1].type >> 8) & 0xff;
    }
    /*TODO*///#define IP_SET_SENSITIVITY(port,val) ((port)+1)->type = ((port+1)->type & 0xffff00ff)|((val&0xff)<<8)
/*TODO*///#define IP_GET_DELTA(port) ((((port)+1)->type >> 16) & 0xff)

    public static int IP_GET_DELTA(InputPort[] ports, int index) {
        return (ports[index + 1].type >> 16) & 0xff;
    }
    /*TODO*///#define IP_SET_DELTA(port,val) ((port)+1)->type = ((port+1)->type & 0xff00ffff)|((val&0xff)<<16)
/*TODO*///#define IP_GET_MIN(port) (((port)+1)->mask)
/*TODO*///#define IP_GET_MAX(port) (((port)+1)->default_value)

    public static int IP_GET_MIN(InputPort[] ports, int index) {
        return ports[index + 1].mask;
    }

    public static int IP_GET_MAX(InputPort[] ports, int index) {
        return (ports[index + 1].default_value);
    }

    public static int IP_GET_CODE_OR1(InputPortTiny port) {
        return port.mask;
    }

    public static int IP_GET_CODE_OR2(InputPortTiny port) {
        return port.default_value;
    }
    /*TODO*///
    public static String IP_NAME_DEFAULT = "-1";
    /*TODO*////* Wrapper for compatibility */
    public static final int IP_KEY_DEFAULT = CODE_DEFAULT;
    public static final int IP_JOY_DEFAULT = CODE_DEFAULT;
    public static final int IP_KEY_PREVIOUS = CODE_PREVIOUS;
    public static final int IP_JOY_PREVIOUS = CODE_PREVIOUS;
    public static final int IP_KEY_NONE = 0x8000; //CODE_NONE
    public static final int IP_JOY_NONE = 0x8000; //CODE_NONE

    /* start of table */
    static InputPortTiny[] input_macro = null;
    static ArrayList<InputPortTiny> inputload = new ArrayList<InputPortTiny>();

    /* end of table */
    public static void INPUT_PORTS_END() {
        inputload.add(new InputPortTiny(0, 0, IPT_END, null));
        input_macro = inputload.toArray(new InputPortTiny[inputload.size()]);
        inputload.clear();
    }
    /* start of a new input port */

    public static void PORT_START() {
        inputload.add(new InputPortTiny(0, 0, IPT_PORT, null));
    }

    public static void PORT_BIT(int mask, int default_value, int type) {
        inputload.add(new InputPortTiny(mask, default_value, type, IP_NAME_DEFAULT));
    }
    /* impulse input bit definition */

    public static void PORT_BIT_IMPULSE(int mask, int default_value, int type, int duration) {
        inputload.add(new InputPortTiny(mask, default_value, type | IPF_IMPULSE | ((duration & 0xff) << 8), IP_NAME_DEFAULT)); //{ mask, default, type | IPF_IMPULSE | ((duration & 0xff) << 8), IP_NAME_DEFAULT },
    }
    /* key/joy code specification */

    public static void PORT_CODE(int key, int joy) {
        inputload.add(new InputPortTiny(key, joy, IPT_EXTENSION, null));//{ key, joy, IPT_EXTENSION, 0 },
    }
    /* input bit definition with extended fields */

    public static void PORT_BITX(int mask, int default_value, int type, String name, int key, int joy) {
        inputload.add(new InputPortTiny(mask, default_value, type, name));
        PORT_CODE(key, joy);
    }
    /* analog input */

    public static void PORT_ANALOG(int mask, int default_value, int type, int sensitivity, int delta, int min, int max) {
        inputload.add(new InputPortTiny(mask, default_value, type, IP_NAME_DEFAULT));
        inputload.add(new InputPortTiny(min, max, IPT_EXTENSION | IPF_SENSITIVITY(sensitivity) | IPF_DELTA(delta), IP_NAME_DEFAULT));
    }

    public static void PORT_ANALOGX(int mask, int default_value, int type, int sensitivity, int delta, int min, int max, int keydec, int keyinc, int joydec, int joyinc) {
        inputload.add(new InputPortTiny(mask, default_value, type, IP_NAME_DEFAULT));
        inputload.add(new InputPortTiny(min, max, IPT_EXTENSION | IPF_SENSITIVITY(sensitivity) | IPF_DELTA(delta), IP_NAME_DEFAULT));
        PORT_CODE(keydec,joydec);
        PORT_CODE(keyinc,joyinc);
    }
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
    /* dip switch definition */

    public static void PORT_DIPNAME(int mask, int default_value, String name) {
        inputload.add(new InputPortTiny(mask, default_value, IPT_DIPSWITCH_NAME, name));//{ mask, default, IPT_DIPSWITCH_NAME, name }
    }

    public static void PORT_DIPSETTING(int default_value, String name) {
        inputload.add(new InputPortTiny(0, default_value, IPT_DIPSWITCH_SETTING, name));//{ 0, default, IPT_DIPSWITCH_SETTING, name }
    }

    public static void PORT_SERVICE(int mask, int default_value) {
        PORT_BITX(mask, mask & default_value, IPT_DIPSWITCH_NAME | IPF_TOGGLE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);
        PORT_DIPSETTING(mask & default_value, DEF_STR("Off"));
        PORT_DIPSETTING(mask & ~default_value, DEF_STR("On"));
    }

    public static String DEF_STR(String num) {
        return ipdn_defaultstrings.get(num);
    }
    public static final int MAX_INPUT_PORTS = 20;

    public static class ipd {

        public /*UINT32 */ int type;
        public String name;
        public /*InputSeq*/ int[] seq;

        public ipd(int type, String name, int[] seq) {
            this.type = type;
            this.name = name;
            this.seq = seq;
        }
    }
}
