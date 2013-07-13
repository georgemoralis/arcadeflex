
package mame;

import static mame.input.*;

public class inputH {
    public static class KeyboardInfo
    {
       public KeyboardInfo(String name, int code, int standardcode)
       {
                this.name = name; this.code = code; this.standardcode = standardcode;
       }
       public String name;/* OS dependant name; 0 terminates the list */
       public int code;/* OS dependant code */
       public int standardcode;	/* CODE_xxx equivalent from list below, or CODE_OTHER if n/a */
    }

/*TODO*///
/*TODO*///struct JoystickInfo
/*TODO*///{
/*TODO*///	char *name; /* OS dependant name; 0 terminates the list */
/*TODO*///	unsigned code; /* OS dependant code */
/*TODO*///	InputCode standardcode;	/* CODE_xxx equivalent from list below, or CODE_OTHER if n/a */
/*TODO*///};
/*TODO*///
    
    /* key */
    public static final int	KEYCODE_A = 0;
    public static final int	KEYCODE_B = 1;
    public static final int	KEYCODE_C = 2;
    public static final int	KEYCODE_D = 3;
    public static final int	KEYCODE_E = 4;
    public static final int KEYCODE_F     = 5;
    public static final int KEYCODE_G     = 6;
    public static final int KEYCODE_H     = 7;
    public static final int KEYCODE_I     = 8;
    public static final int KEYCODE_J     = 9;
    public static final int KEYCODE_K     =10; 
    public static final int KEYCODE_L     =11; 
    public static final int KEYCODE_M     =12; 
    public static final int KEYCODE_N     =13; 
    public static final int KEYCODE_O     =14; 
    public static final int KEYCODE_P     =15; 
    public static final int KEYCODE_Q     =16; 
    public static final int KEYCODE_R     =17; 
    public static final int KEYCODE_S     =18; 
    public static final int KEYCODE_T     =19; 
    public static final int KEYCODE_U     =20;
    public static final int KEYCODE_V     =21;
    public static final int KEYCODE_W     =22;
    public static final int KEYCODE_X     =23;
    public static final int KEYCODE_Y     =24;
    public static final int KEYCODE_Z     =25;
    public static final int KEYCODE_0     =26;
    public static final int KEYCODE_1     =27;
    public static final int KEYCODE_2     =28;
    public static final int KEYCODE_3     =29;
    public static final int KEYCODE_4     =30;
    public static final int KEYCODE_5     =31;
    public static final int KEYCODE_6     =32;
    public static final int KEYCODE_7     =33;
    public static final int KEYCODE_8     =34;
    public static final int KEYCODE_9     =35;
    public static final int KEYCODE_0_PAD =36;
    public static final int KEYCODE_1_PAD =37;
    public static final int KEYCODE_2_PAD =38;
    public static final int KEYCODE_3_PAD =39;
    public static final int KEYCODE_4_PAD =40;
    public static final int KEYCODE_5_PAD =41;
    public static final int KEYCODE_6_PAD =42;
    public static final int KEYCODE_7_PAD =43;
    public static final int KEYCODE_8_PAD =44;
    public static final int KEYCODE_9_PAD =45;
    public static final int KEYCODE_F1    =46;
    public static final int KEYCODE_F2    =47;
    public static final int KEYCODE_F3    =48;
    public static final int KEYCODE_F4    =49;
    public static final int KEYCODE_F5    =50;
    public static final int KEYCODE_F6    =51;
    public static final int KEYCODE_F7    =52;
    public static final int KEYCODE_F8    =53;
    public static final int KEYCODE_F9    =54;
    public static final int KEYCODE_F10   =55;
    public static final int KEYCODE_F11   =56;
    public static final int KEYCODE_F12   =57;
    public static final int KEYCODE_ESC   =58;
    public static final int KEYCODE_TILDE       =59;
    public static final int KEYCODE_MINUS       =60;
    public static final int KEYCODE_EQUALS      =61;
    public static final int KEYCODE_BACKSPACE   =62;
    public static final int KEYCODE_TAB         =63;
    public static final int KEYCODE_OPENBRACE   =64;
    public static final int KEYCODE_CLOSEBRACE  =65;
    public static final int KEYCODE_ENTER       =66;
    public static final int KEYCODE_COLON       =67;
    public static final int KEYCODE_QUOTE       =68;
    public static final int KEYCODE_BACKSLASH   =69;
    public static final int KEYCODE_BACKSLASH2  =70;
    public static final int KEYCODE_COMMA       =71;
    public static final int KEYCODE_STOP        =72;
    public static final int KEYCODE_SLASH       =73;
    public static final int KEYCODE_SPACE       =74;
    public static final int KEYCODE_INSERT      =75;
    public static final int KEYCODE_DEL         =76;
    public static final int KEYCODE_HOME        =77;
    public static final int KEYCODE_END         =78;
    public static final int KEYCODE_PGUP        =79;
    public static final int KEYCODE_PGDN        =80;
    public static final int KEYCODE_LEFT        =81;
    public static final int KEYCODE_RIGHT       =82;
    public static final int KEYCODE_UP          =83;
    public static final int KEYCODE_DOWN        =84;
    public static final int KEYCODE_SLASH_PAD   =85;
    public static final int KEYCODE_ASTERISK    =86;
    public static final int KEYCODE_MINUS_PAD   =87;
    public static final int KEYCODE_PLUS_PAD    =88;
    public static final int KEYCODE_DEL_PAD     =89;
    public static final int KEYCODE_ENTER_PAD   =90;
    public static final int KEYCODE_PRTSCR      =91;
    public static final int KEYCODE_PAUSE       =92;
    public static final int KEYCODE_LSHIFT      =93;
    public static final int KEYCODE_RSHIFT      =94;
    public static final int KEYCODE_LCONTROL    =95;
    public static final int KEYCODE_RCONTROL    =96;
    public static final int KEYCODE_LALT        =97;
    public static final int KEYCODE_RALT        =98;
    public static final int KEYCODE_SCRLOCK     =99;
    public static final int KEYCODE_NUMLOCK     =100;
    public static final int KEYCODE_CAPSLOCK    =101;
    public static final int KEYCODE_LWIN        =102;
    public static final int KEYCODE_RWIN        =103;
    public static final int KEYCODE_MENU        =104;


    /* joy */
    public static final int JOYCODE_1_LEFT      =105;
    public static final int JOYCODE_1_RIGHT     =106;
    public static final int JOYCODE_1_UP        =107;
    public static final int JOYCODE_1_DOWN      =108;
    public static final int JOYCODE_1_BUTTON1   =109;
    public static final int JOYCODE_1_BUTTON2   =110;
    public static final int JOYCODE_1_BUTTON3   =111;
    public static final int JOYCODE_1_BUTTON4   =112;
    public static final int JOYCODE_1_BUTTON5   =113;
    public static final int JOYCODE_1_BUTTON6   =114;
    public static final int JOYCODE_2_LEFT      =115;
    public static final int JOYCODE_2_RIGHT     =116;
    public static final int JOYCODE_2_UP        =117;
    public static final int JOYCODE_2_DOWN      =118;
    public static final int JOYCODE_2_BUTTON1   =119;
    public static final int JOYCODE_2_BUTTON2   =120;
    public static final int JOYCODE_2_BUTTON3   =121;
    public static final int JOYCODE_2_BUTTON4   =122;
    public static final int JOYCODE_2_BUTTON5   =123;
    public static final int JOYCODE_2_BUTTON6   =124;
    public static final int JOYCODE_3_LEFT      =125;
    public static final int JOYCODE_3_RIGHT     =126;
    public static final int JOYCODE_3_UP        =127;
    public static final int JOYCODE_3_DOWN      =128;
    public static final int JOYCODE_3_BUTTON1   =129;
    public static final int JOYCODE_3_BUTTON2   =130;
    public static final int JOYCODE_3_BUTTON3   =131;
    public static final int JOYCODE_3_BUTTON4   =132;
    public static final int JOYCODE_3_BUTTON5   =133;
    public static final int JOYCODE_3_BUTTON6   =134;
    public static final int JOYCODE_4_LEFT      =135;
    public static final int JOYCODE_4_RIGHT     =136;
    public static final int JOYCODE_4_UP        =137;
    public static final int JOYCODE_4_DOWN      =138;
    public static final int JOYCODE_4_BUTTON1   =139;
    public static final int JOYCODE_4_BUTTON2   =140;
    public static final int JOYCODE_4_BUTTON3   =141;
    public static final int JOYCODE_4_BUTTON4   =142;
    public static final int JOYCODE_4_BUTTON5   =143;
    public static final int JOYCODE_4_BUTTON6   =144;

    
    public static final int __code_max = 145; /* Temination of standard code */

	/* special */
    public static final int CODE_NONE    = 0x8000; /* no code, also marker of sequence end */
    public static final int CODE_OTHER   = 0x8001; /* OS code not mapped to any other code */
    public static final int CODE_DEFAULT = 0x8002; /* special for input port definitions */
    public static final int CODE_PREVIOUS= 0x8003; /* special for input port definitions */
    public static final int CODE_NOT     = 0x8004; /* operators for sequences */
    public static final int CODE_OR      = 0x8005; /* operators for sequences */


 public static final int __code_key_first = KEYCODE_A;
 public static final int __code_key_last  = KEYCODE_MENU;
 public static final int __code_joy_first = JOYCODE_1_LEFT;
 public static final int __code_joy_last  = JOYCODE_4_BUTTON6;

/*TODO*///
/*TODO*////* Wrapper for compatibility */
/*TODO*///#define KEYCODE_OTHER CODE_OTHER
/*TODO*///#define JOYCODE_OTHER CODE_OTHER
/*TODO*///#define KEYCODE_NONE CODE_NONE
/*TODO*///#define JOYCODE_NONE CODE_NONE
/*TODO*///
/*TODO*////***************************************************************************/
/*TODO*////* Single code functions */
/*TODO*///
/*TODO*///
/*TODO*///INLINE const char* keyboard_name(int code)
/*TODO*///{
/*TODO*///	return code_name(code);
/*TODO*///}
/*TODO*///
/*TODO*////* Wrapper for compatibility */
/*TODO*///INLINE int keyboard_pressed(int code)
/*TODO*///{
/*TODO*///	return code_pressed(code);
/*TODO*///}
/*TODO*///
    public static int keyboard_pressed_memory(int code)
    {
            return code_pressed_memory(code);
    }

/*TODO*///INLINE int keyboard_pressed_memory_repeat(int code, int speed)
/*TODO*///{
/*TODO*///	return code_pressed_memory_repeat(code,speed);
/*TODO*///}
/*TODO*///
/*TODO*///INLINE int keyboard_read_async(void)
/*TODO*///{
/*TODO*///	return code_read_async();
/*TODO*///}
/*TODO*///
/*TODO*///INLINE int keyboard_read_sync(void)
/*TODO*///{
/*TODO*///	return code_read_sync();
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************/
/*TODO*////* Sequence code funtions */
/*TODO*///
    /* NOTE: If you modify this value you need also to modify the SEQ_DEF declarations */
    public static final int SEQ_MAX= 16;
/*TODO*///
/*TODO*///typedef InputCode InputSeq[SEQ_MAX];
/*TODO*///

    public static int seq_get_1(int[] a)
    {
            return a[0];
    }
    /* NOTE: It's very important that this sequence is EXACLY long SEQ_MAX */
    public static int[] SEQ_DEF_6(int a,int b,int c, int d, int e , int f)
    {
        return new int[] { a, b, c, d, e, f, CODE_NONE, CODE_NONE, CODE_NONE, CODE_NONE, CODE_NONE, CODE_NONE, CODE_NONE, CODE_NONE, CODE_NONE, CODE_NONE };
    }
    public static int[] SEQ_DEF_5(int a,int b,int c, int d,int e)
    {
        return SEQ_DEF_6(a,b,c,d,e,CODE_NONE);
    }
    public static int[] SEQ_DEF_4(int a,int b,int c, int d)
    {
        return SEQ_DEF_5(a,b,c,d,CODE_NONE);
    }
    public static int[] SEQ_DEF_3(int a,int b,int c)
    {
        return SEQ_DEF_4(a,b,c,CODE_NONE);
    }
    public static int[] SEQ_DEF_2(int a,int b)
    {
        return SEQ_DEF_3(a,b,CODE_NONE);
    }  
    public static int[] SEQ_DEF_1(int a)
    {
        return SEQ_DEF_2(a,CODE_NONE);
    }   
    public static int[] SEQ_DEF_0()
    {
        return SEQ_DEF_1(CODE_NONE);
    }       
}
