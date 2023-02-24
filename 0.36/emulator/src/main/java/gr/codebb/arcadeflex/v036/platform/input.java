package gr.codebb.arcadeflex.v036.platform;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;

import java.awt.event.KeyEvent;
import static arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;

public class input {

    static KeyboardInfo[] keylist = {
        new KeyboardInfo("A", KeyEvent.VK_A, KEYCODE_A),
        new KeyboardInfo("B", KeyEvent.VK_B, KEYCODE_B),
        new KeyboardInfo("C", KeyEvent.VK_C, KEYCODE_C),
        new KeyboardInfo("D", KeyEvent.VK_D, KEYCODE_D),
        new KeyboardInfo("E", KeyEvent.VK_E, KEYCODE_E),
        new KeyboardInfo("F", KeyEvent.VK_F, KEYCODE_F),
        new KeyboardInfo("G", KeyEvent.VK_G, KEYCODE_G),
        new KeyboardInfo("H", KeyEvent.VK_H, KEYCODE_H),
        new KeyboardInfo("I", KeyEvent.VK_I, KEYCODE_I),
        new KeyboardInfo("J", KeyEvent.VK_J, KEYCODE_J),
        new KeyboardInfo("K", KeyEvent.VK_K, KEYCODE_K),
        new KeyboardInfo("L", KeyEvent.VK_L, KEYCODE_L),
        new KeyboardInfo("M", KeyEvent.VK_M, KEYCODE_M),
        new KeyboardInfo("N", KeyEvent.VK_N, KEYCODE_N),
        new KeyboardInfo("O", KeyEvent.VK_O, KEYCODE_O),
        new KeyboardInfo("P", KeyEvent.VK_P, KEYCODE_P),
        new KeyboardInfo("Q", KeyEvent.VK_Q, KEYCODE_Q),
        new KeyboardInfo("R", KeyEvent.VK_R, KEYCODE_R),
        new KeyboardInfo("S", KeyEvent.VK_S, KEYCODE_S),
        new KeyboardInfo("T", KeyEvent.VK_T, KEYCODE_T),
        new KeyboardInfo("U", KeyEvent.VK_U, KEYCODE_U),
        new KeyboardInfo("V", KeyEvent.VK_V, KEYCODE_V),
        new KeyboardInfo("W", KeyEvent.VK_W, KEYCODE_W),
        new KeyboardInfo("X", KeyEvent.VK_X, KEYCODE_X),
        new KeyboardInfo("Y", KeyEvent.VK_Y, KEYCODE_Y),
        new KeyboardInfo("Z", KeyEvent.VK_Z, KEYCODE_Z),
        new KeyboardInfo("0", KeyEvent.VK_0, KEYCODE_0),
        new KeyboardInfo("1", KeyEvent.VK_1, KEYCODE_1),
        new KeyboardInfo("2", KeyEvent.VK_2, KEYCODE_2),
        new KeyboardInfo("3", KeyEvent.VK_3, KEYCODE_3),
        new KeyboardInfo("4", KeyEvent.VK_4, KEYCODE_4),
        new KeyboardInfo("5", KeyEvent.VK_5, KEYCODE_5),
        new KeyboardInfo("6", KeyEvent.VK_6, KEYCODE_6),
        new KeyboardInfo("7", KeyEvent.VK_7, KEYCODE_7),
        new KeyboardInfo("8", KeyEvent.VK_8, KEYCODE_8),
        new KeyboardInfo("9", KeyEvent.VK_9, KEYCODE_9),
        new KeyboardInfo("0 PAD", KeyEvent.VK_NUMPAD0, KEYCODE_0_PAD),
        new KeyboardInfo("1 PAD", KeyEvent.VK_NUMPAD1, KEYCODE_1_PAD),
        new KeyboardInfo("2 PAD", KeyEvent.VK_NUMPAD2, KEYCODE_2_PAD),
        new KeyboardInfo("3 PAD", KeyEvent.VK_NUMPAD3, KEYCODE_3_PAD),
        new KeyboardInfo("4 PAD", KeyEvent.VK_NUMPAD4, KEYCODE_4_PAD),
        new KeyboardInfo("5 PAD", KeyEvent.VK_NUMPAD5, KEYCODE_5_PAD),
        new KeyboardInfo("6 PAD", KeyEvent.VK_NUMPAD6, KEYCODE_6_PAD),
        new KeyboardInfo("7 PAD", KeyEvent.VK_NUMPAD7, KEYCODE_7_PAD),
        new KeyboardInfo("8 PAD", KeyEvent.VK_NUMPAD8, KEYCODE_8_PAD),
        new KeyboardInfo("9 PAD", KeyEvent.VK_NUMPAD9, KEYCODE_9_PAD),
        new KeyboardInfo("F1", KeyEvent.VK_F1, KEYCODE_F1),
        new KeyboardInfo("F2", KeyEvent.VK_F2, KEYCODE_F2),
        new KeyboardInfo("F3", KeyEvent.VK_F3, KEYCODE_F3),
        new KeyboardInfo("F4", KeyEvent.VK_F4, KEYCODE_F4),
        new KeyboardInfo("F5", KeyEvent.VK_F5, KEYCODE_F5),
        new KeyboardInfo("F6", KeyEvent.VK_F6, KEYCODE_F6),
        new KeyboardInfo("F7", KeyEvent.VK_F7, KEYCODE_F7),
        new KeyboardInfo("F8", KeyEvent.VK_F8, KEYCODE_F8),
        new KeyboardInfo("F9", KeyEvent.VK_F9, KEYCODE_F9),
        new KeyboardInfo("F10", KeyEvent.VK_F10, KEYCODE_F10),
        new KeyboardInfo("F11", KeyEvent.VK_F11, KEYCODE_F11),
        new KeyboardInfo("F12", KeyEvent.VK_F12, KEYCODE_F12),
        new KeyboardInfo("ESC", KeyEvent.VK_ESCAPE, KEYCODE_ESC),
        new KeyboardInfo("~", KeyEvent.VK_BACK_QUOTE, KEYCODE_TILDE),
        /*TODO the rest codes */
        /*
	new KeyboardInfo( "-",          KEY_MINUS,          KEYCODE_MINUS ),
	new KeyboardInfo( "=",          KEY_EQUALS,         KEYCODE_EQUALS ),*/
        new KeyboardInfo("BKSPACE", KeyEvent.VK_BACK_SPACE, KEYCODE_BACKSPACE),
        new KeyboardInfo("TAB", KeyEvent.VK_TAB, KEYCODE_TAB),
        /*new KeyboardInfo( "[",          KEY_OPENBRACE,      KEYCODE_OPENBRACE ),
	new KeyboardInfo( "]",          KEY_CLOSEBRACE,     KEYCODE_CLOSEBRACE ),*/
        new KeyboardInfo("ENTER", KeyEvent.VK_ENTER, KEYCODE_ENTER),
        /*new KeyboardInfo( ";",          KEY_COLON,          KEYCODE_COLON ),
	new KeyboardInfo( ":",          KEY_QUOTE,          KEYCODE_QUOTE ),
	new KeyboardInfo( "\\",         KEY_BACKSLASH,      KEYCODE_BACKSLASH ),
	new KeyboardInfo( "<",          KEY_BACKSLASH2,     KEYCODE_BACKSLASH2 ),
	new KeyboardInfo( ",",          KEY_COMMA,          KEYCODE_COMMA ),
	new KeyboardInfo( ".",          KEY_STOP,           KEYCODE_STOP ),
	new KeyboardInfo( "/",          KEY_SLASH,          KEYCODE_SLASH ),*/
        new KeyboardInfo("SPACE", KeyEvent.VK_SPACE, KEYCODE_SPACE),
        /*new KeyboardInfo( "INS",		KEY_INSERT,			KEYCODE_INSERT ),
	new KeyboardInfo( "DEL",		KEY_DEL,			KEYCODE_DEL ),
	new KeyboardInfo( "HOME",		KEY_HOME,			KEYCODE_HOME ),
	new KeyboardInfo( "END",		KEY_END,			KEYCODE_END ),*/
        new KeyboardInfo("PGUP", KeyEvent.VK_PAGE_UP, KEYCODE_PGUP),
        new KeyboardInfo("PGDN", KeyEvent.VK_PAGE_DOWN, KEYCODE_PGDN),
        new KeyboardInfo("LEFT", KeyEvent.VK_LEFT, KEYCODE_LEFT),
        new KeyboardInfo("RIGHT", KeyEvent.VK_RIGHT, KEYCODE_RIGHT),
        new KeyboardInfo("UP", KeyEvent.VK_UP, KEYCODE_UP),
        new KeyboardInfo("DOWN", KeyEvent.VK_DOWN, KEYCODE_DOWN),
        /*new KeyboardInfo( "/ PAD",      KEY_SLASH_PAD,      KEYCODE_SLASH_PAD ),
	new KeyboardInfo( "* PAD",      KEY_ASTERISK,       KEYCODE_ASTERISK ),
	new KeyboardInfo( "- PAD",      KEY_MINUS_PAD,      KEYCODE_MINUS_PAD ),
	new KeyboardInfo( "+ PAD",      KEY_PLUS_PAD,       KEYCODE_PLUS_PAD ),
	new KeyboardInfo( ". PAD",      KEY_DEL_PAD,        KEYCODE_DEL_PAD ),
	new KeyboardInfo( "ENTER PAD",  KEY_ENTER_PAD,      KEYCODE_ENTER_PAD ),
	new KeyboardInfo( "PRTSCR",     KEY_PRTSCR,         KEYCODE_PRTSCR ),
	new KeyboardInfo( "PAUSE",      KEY_PAUSE,          KEYCODE_PAUSE ),*/
        //LSHIFT + RSHOFT SAME IN JAVA
        new KeyboardInfo("LSHIFT", KeyEvent.VK_SHIFT, KEYCODE_LSHIFT),
        new KeyboardInfo("RSHIFT", KeyEvent.VK_SHIFT, KEYCODE_RSHIFT),
        //LCONTROL + RCONTROL ARE THE SAME IN JAVA....       
        new KeyboardInfo("LCTRL", KeyEvent.VK_CONTROL, KEYCODE_LCONTROL),
        new KeyboardInfo("RCTRL", KeyEvent.VK_CONTROL, KEYCODE_RCONTROL),
        //RALT - LALT ARE THE SAME IN JAVA
        new KeyboardInfo("ALT", KeyEvent.VK_ALT, KEYCODE_LALT),
        new KeyboardInfo("ALTGR", KeyEvent.VK_ALT, KEYCODE_RALT),
        /*new KeyboardInfo( "LWIN",		KEY_LWIN,			KEYCODE_OTHER ),
	new KeyboardInfo( "RWIN",		KEY_RWIN,			KEYCODE_OTHER ),
	new KeyboardInfo( "MENU",		KEY_MENU,			KEYCODE_OTHER ),
	new KeyboardInfo( "SCRLOCK",    KEY_SCRLOCK,        KEYCODE_SCRLOCK ),
	new KeyboardInfo( "NUMLOCK",    KEY_NUMLOCK,        KEYCODE_NUMLOCK ),
	new KeyboardInfo( "CAPSLOCK",   KEY_CAPSLOCK,       KEYCODE_CAPSLOCK ),*/
        new KeyboardInfo(null, 0, 0) /* end of table */};

    /* return a list of all available keys */
    public static KeyboardInfo[] osd_get_key_list() {
        return keylist;
    }

    public static int osd_is_key_pressed(int keycode) {
        if (keycode >= 256) {
            return 0;
        }

        //TODO          /*if (keycode == KEY_PAUSE)
        /*         {
                    static int pressed,counter;
                    int res;

                    res = key[KEY_PAUSE] ^ pressed;
                    if (res)
                    {
                            if (counter > 0)
                            {
                                    if (--counter == 0)
                                            pressed = key[KEY_PAUSE];
                            }
                            else counter = 10;
                    }

                    return res;
            }*/
        return screen.key[keycode] ? 1 : 0;

    }

    public static WriteHandlerPtr osd_led_w = new WriteHandlerPtr() {
        public void handler(int led, int on) {
            //throw new UnsupportedOperationException("Unsupported osd_led_w");
        }
    };
}
