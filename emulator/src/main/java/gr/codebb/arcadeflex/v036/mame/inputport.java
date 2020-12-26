package gr.codebb.arcadeflex.v036.mame;

import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import java.util.HashMap;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v036.mame.input.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;


public class inputport {

    static /*unsigned short*/ char[] input_port_value = new char[MAX_INPUT_PORTS];
    static /*unsigned short*/ char[] input_vblank = new char[MAX_INPUT_PORTS];

    /* Assuming a maxium of one analog input device per port BW 101297 */
    static int[] input_analog = new int[MAX_INPUT_PORTS];
    static int[] input_analog_current_value = new int[MAX_INPUT_PORTS];
    static int[] input_analog_previous_value = new int[MAX_INPUT_PORTS];
    static int[] input_analog_init = new int[MAX_INPUT_PORTS];

    static int[] mouse_delta_x = new int[OSD_MAX_JOY_ANALOG];
    static int[] mouse_delta_y = new int[OSD_MAX_JOY_ANALOG];
    static int[] analog_current_x = new int[OSD_MAX_JOY_ANALOG];
    static int[] analog_current_y = new int[OSD_MAX_JOY_ANALOG];
    static int[] analog_previous_x = new int[OSD_MAX_JOY_ANALOG];
    static int[] analog_previous_y = new int[OSD_MAX_JOY_ANALOG];

    /**
     * *************************************************************************
     *
     * Configuration load/save
     *
     **************************************************************************
     */

    /* this must match the enum in inptport.h */
    public static HashMap<String, String> ipdn_defaultstrings = new HashMap<String, String>() {
        {
            put("Off", "Off");
            put("On", "On");
            put("Yes", "Yes");
            put("Lives", "Lives");
            put("Bonus_Life", "Bonus Life");
            put("Difficulty", "Difficulty");
            put("Demo_Sounds", "Demo Sounds");
            put("Coinage", "Coinage");
            put("Coin_A", "Coin A");
            put("Coin_B", "Coin B");
            put("9C_1C", "9 Coins/1 Credit");
            put("8C_1C", "8 Coins/1 Credit");
            put("7C_1C", "7 Coins/1 Credit");
            put("6C_1C", "6 Coins/1 Credit");
            put("5C_1C", "5 Coins/1 Credit");
            put("4C_1C", "4 Coins/1 Credit");
            put("3C_1C", "3 Coins/1 Credit");
            put("8C_3C", "8 Coins/3 Credits");
            put("4C_2C", "4 Coins/2 Credits");
            put("2C_1C", "2 Coins/1 Credit");
            put("5C_3C", "5 Coins/3 Credits");
            put("3C_2C", "3 Coins/2 Credits");
            put("4C_3C", "4 Coins/3 Credits");
            put("4C_4C", "4 Coins/4 Credits");
            put("3C_3C", "3 Coins/3 Credits");
            put("2C_2C", "2 Coins/2 Credits");
            put("1C_1C", "1 Coin/1 Credit");
            put("4C_5C", "4 Coins/5 Credits");
            put("3C_4C", "3 Coins/4 Credits");
            put("2C_3C", "2 Coins/3 Credits");
            put("4C_7C", "4 Coins/7 Credits");
            put("2C_4C", "2 Coins/4 Credits");
            put("1C_2C", "1 Coin/2 Credits");
            put("2C_5C", "2 Coins/5 Credits");
            put("2C_6C", "2 Coins/6 Credits");
            put("1C_3C", "1 Coin/3 Credits");
            put("2C_7C", "2 Coins/7 Credits");
            put("2C_8C", "2 Coins/8 Credits");
            put("1C_4C", "1 Coin/4 Credits");
            put("1C_5C", "1 Coin/5 Credits");
            put("1C_6C", "1 Coin/6 Credits");
            put("1C_7C", "1 Coin/7 Credits");
            put("1C_8C", "1 Coin/8 Credits");
            put("1C_9C", "1 Coin/9 Credits");
            put("Free_Play", "Free Play");
            put("Cabinet", "Cabinet");
            put("Upright", "Upright");
            put("Cocktail", "Cocktail");
            put("Flip_Screen", "Flip Screen");
            put("Service_Mode", "Service Mode");
            put("Unused", "Unused");
            put("Unknown", "Unknown");

        }
    };

    public static ipd[] inputport_defaults
            = {
                //changed to support BACKSPACE IN APPLET MODE AS WELL  new ipd( IPT_UI_CONFIGURE,         "Config Menu",       SEQ_DEF_1(KEYCODE_TAB) ),
                new ipd(IPT_UI_CONFIGURE, "Config Menu", SEQ_DEF_3(KEYCODE_TAB, CODE_OR, KEYCODE_BACKSPACE)),
                new ipd(IPT_UI_ON_SCREEN_DISPLAY, "On Screen Display", SEQ_DEF_1(KEYCODE_TILDE)),
                new ipd(IPT_UI_PAUSE, "Pause", SEQ_DEF_1(KEYCODE_P)),
                new ipd(IPT_UI_RESET_MACHINE, "Reset Game", SEQ_DEF_1(KEYCODE_F3)),
                new ipd(IPT_UI_SHOW_GFX, "Show Gfx", SEQ_DEF_1(KEYCODE_F4)),
                new ipd(IPT_UI_FRAMESKIP_DEC, "Frameskip Dec", SEQ_DEF_1(KEYCODE_F8)),
                new ipd(IPT_UI_FRAMESKIP_INC, "Frameskip Inc", SEQ_DEF_1(KEYCODE_F9)),
                new ipd(IPT_UI_THROTTLE, "Throttle", SEQ_DEF_1(KEYCODE_F10)),
                new ipd(IPT_UI_SHOW_FPS, "Show FPS", SEQ_DEF_5(KEYCODE_F11, CODE_NOT, KEYCODE_LCONTROL, CODE_NOT, KEYCODE_LSHIFT)),
                new ipd(IPT_UI_SHOW_PROFILER, "Show Profiler", SEQ_DEF_2(KEYCODE_F11, KEYCODE_LSHIFT)),
                new ipd(IPT_UI_SHOW_COLORS, "Show Colors", SEQ_DEF_2(KEYCODE_F11, KEYCODE_LCONTROL)),
                new ipd(IPT_UI_SNAPSHOT, "Save Snapshot", SEQ_DEF_1(KEYCODE_F12)),
                new ipd(IPT_UI_TOGGLE_CHEAT, "Toggle Cheat", SEQ_DEF_1(KEYCODE_F5)),
                new ipd(IPT_UI_UP, "UI Up", SEQ_DEF_3(KEYCODE_UP, CODE_OR, JOYCODE_1_UP)),
                new ipd(IPT_UI_DOWN, "UI Down", SEQ_DEF_3(KEYCODE_DOWN, CODE_OR, JOYCODE_1_DOWN)),
                new ipd(IPT_UI_LEFT, "UI Left", SEQ_DEF_3(KEYCODE_LEFT, CODE_OR, JOYCODE_1_LEFT)),
                new ipd(IPT_UI_RIGHT, "UI Right", SEQ_DEF_3(KEYCODE_RIGHT, CODE_OR, JOYCODE_1_RIGHT)),
                new ipd(IPT_UI_SELECT, "UI Select", SEQ_DEF_3(KEYCODE_ENTER, CODE_OR, JOYCODE_1_BUTTON1)),
                new ipd(IPT_UI_CANCEL, "UI Cancel", SEQ_DEF_1(KEYCODE_ESC)),
                new ipd(IPT_UI_PAN_UP, "Pan Up", SEQ_DEF_3(KEYCODE_PGUP, CODE_NOT, KEYCODE_LSHIFT)),
                new ipd(IPT_UI_PAN_DOWN, "Pan Down", SEQ_DEF_3(KEYCODE_PGDN, CODE_NOT, KEYCODE_LSHIFT)),
                new ipd(IPT_UI_PAN_LEFT, "Pan Left", SEQ_DEF_2(KEYCODE_PGUP, KEYCODE_LSHIFT)),
                new ipd(IPT_UI_PAN_RIGHT, "Pan Right", SEQ_DEF_2(KEYCODE_PGDN, KEYCODE_LSHIFT)),
                new ipd(IPT_START1, "1 Player Start", SEQ_DEF_1(KEYCODE_1)),
                new ipd(IPT_START2, "2 Players Start", SEQ_DEF_1(KEYCODE_2)),
                new ipd(IPT_START3, "3 Players Start", SEQ_DEF_1(KEYCODE_3)),
                new ipd(IPT_START4, "4 Players Start", SEQ_DEF_1(KEYCODE_4)),
                new ipd(IPT_COIN1, "Coin 1", SEQ_DEF_1(KEYCODE_5)),
                new ipd(IPT_COIN2, "Coin 2", SEQ_DEF_1(KEYCODE_6)),
                new ipd(IPT_COIN3, "Coin 3", SEQ_DEF_1(KEYCODE_7)),
                new ipd(IPT_COIN4, "Coin 4", SEQ_DEF_1(KEYCODE_8)),
                new ipd(IPT_SERVICE1, "Service 1", SEQ_DEF_1(KEYCODE_9)),
                new ipd(IPT_SERVICE2, "Service 2", SEQ_DEF_1(KEYCODE_0)),
                new ipd(IPT_SERVICE3, "Service 3", SEQ_DEF_1(KEYCODE_MINUS)),
                new ipd(IPT_SERVICE4, "Service 4", SEQ_DEF_1(KEYCODE_EQUALS)),
                new ipd(IPT_TILT, "Tilt", SEQ_DEF_1(KEYCODE_T)),
                new ipd(IPT_JOYSTICK_UP | IPF_PLAYER1, "P1 Up", SEQ_DEF_3(KEYCODE_UP, CODE_OR, JOYCODE_1_UP)),
                new ipd(IPT_JOYSTICK_DOWN | IPF_PLAYER1, "P1 Down", SEQ_DEF_3(KEYCODE_DOWN, CODE_OR, JOYCODE_1_DOWN)),
                new ipd(IPT_JOYSTICK_LEFT | IPF_PLAYER1, "P1 Left", SEQ_DEF_3(KEYCODE_LEFT, CODE_OR, JOYCODE_1_LEFT)),
                new ipd(IPT_JOYSTICK_RIGHT | IPF_PLAYER1, "P1 Right", SEQ_DEF_3(KEYCODE_RIGHT, CODE_OR, JOYCODE_1_RIGHT)),
                new ipd(IPT_BUTTON1 | IPF_PLAYER1, "P1 Button 1", SEQ_DEF_3(KEYCODE_LCONTROL, CODE_OR, JOYCODE_1_BUTTON1)),
                new ipd(IPT_BUTTON2 | IPF_PLAYER1, "P1 Button 2", SEQ_DEF_3(KEYCODE_LALT, CODE_OR, JOYCODE_1_BUTTON2)),
                new ipd(IPT_BUTTON3 | IPF_PLAYER1, "P1 Button 3", SEQ_DEF_3(KEYCODE_SPACE, CODE_OR, JOYCODE_1_BUTTON3)),
                new ipd(IPT_BUTTON4 | IPF_PLAYER1, "P1 Button 4", SEQ_DEF_3(KEYCODE_LSHIFT, CODE_OR, JOYCODE_1_BUTTON4)),
                new ipd(IPT_BUTTON5 | IPF_PLAYER1, "P1 Button 5", SEQ_DEF_3(KEYCODE_Z, CODE_OR, JOYCODE_1_BUTTON5)),
                new ipd(IPT_BUTTON6 | IPF_PLAYER1, "P1 Button 6", SEQ_DEF_3(KEYCODE_X, CODE_OR, JOYCODE_1_BUTTON6)),
                new ipd(IPT_BUTTON7 | IPF_PLAYER1, "P1 Button 7", SEQ_DEF_1(KEYCODE_C)),
                new ipd(IPT_BUTTON8 | IPF_PLAYER1, "P1 Button 8", SEQ_DEF_1(KEYCODE_V)),
                new ipd(IPT_BUTTON9 | IPF_PLAYER1, "P1 Button 9", SEQ_DEF_1(KEYCODE_B)),
                new ipd(IPT_JOYSTICKRIGHT_UP | IPF_PLAYER1, "P1 Right/Up", SEQ_DEF_3(KEYCODE_I, CODE_OR, JOYCODE_1_BUTTON2)),
                new ipd(IPT_JOYSTICKRIGHT_DOWN | IPF_PLAYER1, "P1 Right/Down", SEQ_DEF_3(KEYCODE_K, CODE_OR, JOYCODE_1_BUTTON3)),
                new ipd(IPT_JOYSTICKRIGHT_LEFT | IPF_PLAYER1, "P1 Right/Left", SEQ_DEF_3(KEYCODE_J, CODE_OR, JOYCODE_1_BUTTON1)),
                new ipd(IPT_JOYSTICKRIGHT_RIGHT | IPF_PLAYER1, "P1 Right/Right", SEQ_DEF_3(KEYCODE_L, CODE_OR, JOYCODE_1_BUTTON4)),
                new ipd(IPT_JOYSTICKLEFT_UP | IPF_PLAYER1, "P1 Left/Up", SEQ_DEF_3(KEYCODE_E, CODE_OR, JOYCODE_1_UP)),
                new ipd(IPT_JOYSTICKLEFT_DOWN | IPF_PLAYER1, "P1 Left/Down", SEQ_DEF_3(KEYCODE_D, CODE_OR, JOYCODE_1_DOWN)),
                new ipd(IPT_JOYSTICKLEFT_LEFT | IPF_PLAYER1, "P1 Left/Left", SEQ_DEF_3(KEYCODE_S, CODE_OR, JOYCODE_1_LEFT)),
                new ipd(IPT_JOYSTICKLEFT_RIGHT | IPF_PLAYER1, "P1 Left/Right", SEQ_DEF_3(KEYCODE_F, CODE_OR, JOYCODE_1_RIGHT)),
                new ipd(IPT_JOYSTICK_UP | IPF_PLAYER2, "P2 Up", SEQ_DEF_3(KEYCODE_R, CODE_OR, JOYCODE_2_UP)),
                new ipd(IPT_JOYSTICK_DOWN | IPF_PLAYER2, "P2 Down", SEQ_DEF_3(KEYCODE_F, CODE_OR, JOYCODE_2_DOWN)),
                new ipd(IPT_JOYSTICK_LEFT | IPF_PLAYER2, "P2 Left", SEQ_DEF_3(KEYCODE_D, CODE_OR, JOYCODE_2_LEFT)),
                new ipd(IPT_JOYSTICK_RIGHT | IPF_PLAYER2, "P2 Right", SEQ_DEF_3(KEYCODE_G, CODE_OR, JOYCODE_2_RIGHT)),
                new ipd(IPT_BUTTON1 | IPF_PLAYER2, "P2 Button 1", SEQ_DEF_3(KEYCODE_A, CODE_OR, JOYCODE_2_BUTTON1)),
                new ipd(IPT_BUTTON2 | IPF_PLAYER2, "P2 Button 2", SEQ_DEF_3(KEYCODE_S, CODE_OR, JOYCODE_2_BUTTON2)),
                new ipd(IPT_BUTTON3 | IPF_PLAYER2, "P2 Button 3", SEQ_DEF_3(KEYCODE_Q, CODE_OR, JOYCODE_2_BUTTON3)),
                new ipd(IPT_BUTTON4 | IPF_PLAYER2, "P2 Button 4", SEQ_DEF_3(KEYCODE_W, CODE_OR, JOYCODE_2_BUTTON4)),
                new ipd(IPT_BUTTON5 | IPF_PLAYER2, "P2 Button 5", SEQ_DEF_1(JOYCODE_2_BUTTON5)),
                new ipd(IPT_BUTTON6 | IPF_PLAYER2, "P2 Button 6", SEQ_DEF_1(JOYCODE_2_BUTTON6)),
                new ipd(IPT_BUTTON7 | IPF_PLAYER2, "P2 Button 7", SEQ_DEF_0()),
                new ipd(IPT_BUTTON8 | IPF_PLAYER2, "P2 Button 8", SEQ_DEF_0()),
                new ipd(IPT_BUTTON9 | IPF_PLAYER2, "P2 Button 9", SEQ_DEF_0()),
                new ipd(IPT_JOYSTICKRIGHT_UP | IPF_PLAYER2, "P2 Right/Up", SEQ_DEF_0()),
                new ipd(IPT_JOYSTICKRIGHT_DOWN | IPF_PLAYER2, "P2 Right/Down", SEQ_DEF_0()),
                new ipd(IPT_JOYSTICKRIGHT_LEFT | IPF_PLAYER2, "P2 Right/Left", SEQ_DEF_0()),
                new ipd(IPT_JOYSTICKRIGHT_RIGHT | IPF_PLAYER2, "P2 Right/Right", SEQ_DEF_0()),
                new ipd(IPT_JOYSTICKLEFT_UP | IPF_PLAYER2, "P2 Left/Up", SEQ_DEF_0()),
                new ipd(IPT_JOYSTICKLEFT_DOWN | IPF_PLAYER2, "P2 Left/Down", SEQ_DEF_0()),
                new ipd(IPT_JOYSTICKLEFT_LEFT | IPF_PLAYER2, "P2 Left/Left", SEQ_DEF_0()),
                new ipd(IPT_JOYSTICKLEFT_RIGHT | IPF_PLAYER2, "P2 Left/Right", SEQ_DEF_0()),
                new ipd(IPT_JOYSTICK_UP | IPF_PLAYER3, "P3 Up", SEQ_DEF_3(KEYCODE_I, CODE_OR, JOYCODE_3_UP)),
                new ipd(IPT_JOYSTICK_DOWN | IPF_PLAYER3, "P3 Down", SEQ_DEF_3(KEYCODE_K, CODE_OR, JOYCODE_3_DOWN)),
                new ipd(IPT_JOYSTICK_LEFT | IPF_PLAYER3, "P3 Left", SEQ_DEF_3(KEYCODE_J, CODE_OR, JOYCODE_3_LEFT)),
                new ipd(IPT_JOYSTICK_RIGHT | IPF_PLAYER3, "P3 Right", SEQ_DEF_3(KEYCODE_L, CODE_OR, JOYCODE_3_RIGHT)),
                new ipd(IPT_BUTTON1 | IPF_PLAYER3, "P3 Button 1", SEQ_DEF_3(KEYCODE_RCONTROL, CODE_OR, JOYCODE_3_BUTTON1)),
                new ipd(IPT_BUTTON2 | IPF_PLAYER3, "P3 Button 2", SEQ_DEF_3(KEYCODE_RSHIFT, CODE_OR, JOYCODE_3_BUTTON2)),
                new ipd(IPT_BUTTON3 | IPF_PLAYER3, "P3 Button 3", SEQ_DEF_3(KEYCODE_ENTER, CODE_OR, JOYCODE_3_BUTTON3)),
                new ipd(IPT_BUTTON4 | IPF_PLAYER3, "P3 Button 4", SEQ_DEF_1(JOYCODE_3_BUTTON4)),
                new ipd(IPT_JOYSTICK_UP | IPF_PLAYER4, "P4 Up", SEQ_DEF_1(JOYCODE_4_UP)),
                new ipd(IPT_JOYSTICK_DOWN | IPF_PLAYER4, "P4 Down", SEQ_DEF_1(JOYCODE_4_DOWN)),
                new ipd(IPT_JOYSTICK_LEFT | IPF_PLAYER4, "P4 Left", SEQ_DEF_1(JOYCODE_4_LEFT)),
                new ipd(IPT_JOYSTICK_RIGHT | IPF_PLAYER4, "P4 Right", SEQ_DEF_1(JOYCODE_4_RIGHT)),
                new ipd(IPT_BUTTON1 | IPF_PLAYER4, "P4 Button 1", SEQ_DEF_1(JOYCODE_4_BUTTON1)),
                new ipd(IPT_BUTTON2 | IPF_PLAYER4, "P4 Button 2", SEQ_DEF_1(JOYCODE_4_BUTTON2)),
                new ipd(IPT_BUTTON3 | IPF_PLAYER4, "P4 Button 3", SEQ_DEF_1(JOYCODE_4_BUTTON3)),
                new ipd(IPT_BUTTON4 | IPF_PLAYER4, "P4 Button 4", SEQ_DEF_1(JOYCODE_4_BUTTON4)),
                new ipd(IPT_PEDAL | IPF_PLAYER1, "Pedal 1", SEQ_DEF_3(KEYCODE_LCONTROL, CODE_OR, JOYCODE_1_BUTTON1)),
                new ipd((IPT_PEDAL + IPT_EXTENSION) | IPF_PLAYER1, "P1 Auto Release <Y/N>", SEQ_DEF_1(KEYCODE_Y)),
                new ipd(IPT_PEDAL | IPF_PLAYER2, "Pedal 2", SEQ_DEF_3(KEYCODE_A, CODE_OR, JOYCODE_2_BUTTON1)),
                new ipd((IPT_PEDAL + IPT_EXTENSION) | IPF_PLAYER2, "P2 Auto Release <Y/N>", SEQ_DEF_1(KEYCODE_Y)),
                new ipd(IPT_PEDAL | IPF_PLAYER3, "Pedal 3", SEQ_DEF_3(KEYCODE_RCONTROL, CODE_OR, JOYCODE_3_BUTTON1)),
                new ipd((IPT_PEDAL + IPT_EXTENSION) | IPF_PLAYER3, "P3 Auto Release <Y/N>", SEQ_DEF_1(KEYCODE_Y)),
                new ipd(IPT_PEDAL | IPF_PLAYER4, "Pedal 4", SEQ_DEF_1(JOYCODE_4_BUTTON1)),
                new ipd((IPT_PEDAL + IPT_EXTENSION) | IPF_PLAYER4, "P4 Auto Release <Y/N>", SEQ_DEF_1(KEYCODE_Y)),
                new ipd(IPT_PADDLE | IPF_PLAYER1, "Paddle", SEQ_DEF_3(KEYCODE_LEFT, CODE_OR, JOYCODE_1_LEFT)),
                new ipd((IPT_PADDLE | IPF_PLAYER1) + IPT_EXTENSION, "Paddle", SEQ_DEF_3(KEYCODE_RIGHT, CODE_OR, JOYCODE_1_RIGHT)),
                new ipd(IPT_PADDLE | IPF_PLAYER2, "Paddle 2", SEQ_DEF_3(KEYCODE_D, CODE_OR, JOYCODE_2_LEFT)),
                new ipd((IPT_PADDLE | IPF_PLAYER2) + IPT_EXTENSION, "Paddle 2", SEQ_DEF_3(KEYCODE_G, CODE_OR, JOYCODE_2_RIGHT)),
                new ipd(IPT_PADDLE | IPF_PLAYER3, "Paddle 3", SEQ_DEF_3(KEYCODE_J, CODE_OR, JOYCODE_3_LEFT)),
                new ipd((IPT_PADDLE | IPF_PLAYER3) + IPT_EXTENSION, "Paddle 3", SEQ_DEF_3(KEYCODE_L, CODE_OR, JOYCODE_3_RIGHT)),
                new ipd(IPT_PADDLE | IPF_PLAYER4, "Paddle 4", SEQ_DEF_1(JOYCODE_4_LEFT)),
                new ipd((IPT_PADDLE | IPF_PLAYER4) + IPT_EXTENSION, "Paddle 4", SEQ_DEF_1(JOYCODE_4_RIGHT)),
                new ipd(IPT_PADDLE_V | IPF_PLAYER1, "Paddle V", SEQ_DEF_3(KEYCODE_UP, CODE_OR, JOYCODE_1_UP)),
                new ipd((IPT_PADDLE_V | IPF_PLAYER1) + IPT_EXTENSION, "Paddle V", SEQ_DEF_3(KEYCODE_DOWN, CODE_OR, JOYCODE_1_DOWN)),
                new ipd(IPT_PADDLE_V | IPF_PLAYER2, "Paddle V 2", SEQ_DEF_3(KEYCODE_R, CODE_OR, JOYCODE_2_UP)),
                new ipd((IPT_PADDLE_V | IPF_PLAYER2) + IPT_EXTENSION, "Paddle V 2", SEQ_DEF_3(KEYCODE_F, CODE_OR, JOYCODE_2_DOWN)),
                new ipd(IPT_PADDLE_V | IPF_PLAYER3, "Paddle V 3", SEQ_DEF_3(KEYCODE_I, CODE_OR, JOYCODE_3_UP)),
                new ipd((IPT_PADDLE_V | IPF_PLAYER3) + IPT_EXTENSION, "Paddle V 3", SEQ_DEF_3(KEYCODE_K, CODE_OR, JOYCODE_3_DOWN)),
                new ipd(IPT_PADDLE_V | IPF_PLAYER4, "Paddle V 4", SEQ_DEF_1(JOYCODE_4_UP)),
                new ipd((IPT_PADDLE_V | IPF_PLAYER4) + IPT_EXTENSION, "Paddle V 4", SEQ_DEF_1(JOYCODE_4_DOWN)),
                new ipd(IPT_DIAL | IPF_PLAYER1, "Dial", SEQ_DEF_3(KEYCODE_LEFT, CODE_OR, JOYCODE_1_LEFT)),
                new ipd((IPT_DIAL | IPF_PLAYER1) + IPT_EXTENSION, "Dial", SEQ_DEF_3(KEYCODE_RIGHT, CODE_OR, JOYCODE_1_RIGHT)),
                new ipd(IPT_DIAL | IPF_PLAYER2, "Dial 2", SEQ_DEF_3(KEYCODE_D, CODE_OR, JOYCODE_2_LEFT)),
                new ipd((IPT_DIAL | IPF_PLAYER2) + IPT_EXTENSION, "Dial 2", SEQ_DEF_3(KEYCODE_G, CODE_OR, JOYCODE_2_RIGHT)),
                new ipd(IPT_DIAL | IPF_PLAYER3, "Dial 3", SEQ_DEF_3(KEYCODE_J, CODE_OR, JOYCODE_3_LEFT)),
                new ipd((IPT_DIAL | IPF_PLAYER3) + IPT_EXTENSION, "Dial 3", SEQ_DEF_3(KEYCODE_L, CODE_OR, JOYCODE_3_RIGHT)),
                new ipd(IPT_DIAL | IPF_PLAYER4, "Dial 4", SEQ_DEF_1(JOYCODE_4_LEFT)),
                new ipd((IPT_DIAL | IPF_PLAYER4) + IPT_EXTENSION, "Dial 4", SEQ_DEF_1(JOYCODE_4_RIGHT)),
                new ipd(IPT_DIAL_V | IPF_PLAYER1, "Dial V", SEQ_DEF_3(KEYCODE_UP, CODE_OR, JOYCODE_1_UP)),
                new ipd((IPT_DIAL_V | IPF_PLAYER1) + IPT_EXTENSION, "Dial V", SEQ_DEF_3(KEYCODE_DOWN, CODE_OR, JOYCODE_1_DOWN)),
                new ipd(IPT_DIAL_V | IPF_PLAYER2, "Dial V 2", SEQ_DEF_3(KEYCODE_R, CODE_OR, JOYCODE_2_UP)),
                new ipd((IPT_DIAL_V | IPF_PLAYER2) + IPT_EXTENSION, "Dial V 2", SEQ_DEF_3(KEYCODE_F, CODE_OR, JOYCODE_2_DOWN)),
                new ipd(IPT_DIAL_V | IPF_PLAYER3, "Dial V 3", SEQ_DEF_3(KEYCODE_I, CODE_OR, JOYCODE_3_UP)),
                new ipd((IPT_DIAL_V | IPF_PLAYER3) + IPT_EXTENSION, "Dial V 3", SEQ_DEF_3(KEYCODE_K, CODE_OR, JOYCODE_3_DOWN)),
                new ipd(IPT_DIAL_V | IPF_PLAYER4, "Dial V 4", SEQ_DEF_1(JOYCODE_4_UP)),
                new ipd((IPT_DIAL_V | IPF_PLAYER4) + IPT_EXTENSION, "Dial V 4", SEQ_DEF_1(JOYCODE_4_DOWN)),
                new ipd(IPT_TRACKBALL_X | IPF_PLAYER1, "Track X", SEQ_DEF_3(KEYCODE_LEFT, CODE_OR, JOYCODE_1_LEFT)),
                new ipd((IPT_TRACKBALL_X | IPF_PLAYER1) + IPT_EXTENSION, "Track X", SEQ_DEF_3(KEYCODE_RIGHT, CODE_OR, JOYCODE_1_RIGHT)),
                new ipd(IPT_TRACKBALL_X | IPF_PLAYER2, "Track X 2", SEQ_DEF_3(KEYCODE_D, CODE_OR, JOYCODE_2_LEFT)),
                new ipd((IPT_TRACKBALL_X | IPF_PLAYER2) + IPT_EXTENSION, "Track X 2", SEQ_DEF_3(KEYCODE_G, CODE_OR, JOYCODE_2_RIGHT)),
                new ipd(IPT_TRACKBALL_X | IPF_PLAYER3, "Track X 3", SEQ_DEF_3(KEYCODE_J, CODE_OR, JOYCODE_3_LEFT)),
                new ipd((IPT_TRACKBALL_X | IPF_PLAYER3) + IPT_EXTENSION, "Track X 3", SEQ_DEF_3(KEYCODE_L, CODE_OR, JOYCODE_3_RIGHT)),
                new ipd(IPT_TRACKBALL_X | IPF_PLAYER4, "Track X 4", SEQ_DEF_1(JOYCODE_4_LEFT)),
                new ipd((IPT_TRACKBALL_X | IPF_PLAYER4) + IPT_EXTENSION, "Track X 4", SEQ_DEF_1(JOYCODE_4_RIGHT)),
                new ipd(IPT_TRACKBALL_Y | IPF_PLAYER1, "Track Y", SEQ_DEF_3(KEYCODE_UP, CODE_OR, JOYCODE_1_UP)),
                new ipd((IPT_TRACKBALL_Y | IPF_PLAYER1) + IPT_EXTENSION, "Track Y", SEQ_DEF_3(KEYCODE_DOWN, CODE_OR, JOYCODE_1_DOWN)),
                new ipd(IPT_TRACKBALL_Y | IPF_PLAYER2, "Track Y 2", SEQ_DEF_3(KEYCODE_R, CODE_OR, JOYCODE_2_UP)),
                new ipd((IPT_TRACKBALL_Y | IPF_PLAYER2) + IPT_EXTENSION, "Track Y 2", SEQ_DEF_3(KEYCODE_F, CODE_OR, JOYCODE_2_DOWN)),
                new ipd(IPT_TRACKBALL_Y | IPF_PLAYER3, "Track Y 3", SEQ_DEF_3(KEYCODE_I, CODE_OR, JOYCODE_3_UP)),
                new ipd((IPT_TRACKBALL_Y | IPF_PLAYER3) + IPT_EXTENSION, "Track Y 3", SEQ_DEF_3(KEYCODE_K, CODE_OR, JOYCODE_3_DOWN)),
                new ipd(IPT_TRACKBALL_Y | IPF_PLAYER4, "Track Y 4", SEQ_DEF_1(JOYCODE_4_UP)),
                new ipd((IPT_TRACKBALL_Y | IPF_PLAYER4) + IPT_EXTENSION, "Track Y 4", SEQ_DEF_1(JOYCODE_4_DOWN)),
                new ipd(IPT_AD_STICK_X | IPF_PLAYER1, "AD Stick X", SEQ_DEF_3(KEYCODE_LEFT, CODE_OR, JOYCODE_1_LEFT)),
                new ipd((IPT_AD_STICK_X | IPF_PLAYER1) + IPT_EXTENSION, "AD Stick X", SEQ_DEF_3(KEYCODE_RIGHT, CODE_OR, JOYCODE_1_RIGHT)),
                new ipd(IPT_AD_STICK_X | IPF_PLAYER2, "AD Stick X 2", SEQ_DEF_3(KEYCODE_D, CODE_OR, JOYCODE_2_LEFT)),
                new ipd((IPT_AD_STICK_X | IPF_PLAYER2) + IPT_EXTENSION, "AD Stick X 2", SEQ_DEF_3(KEYCODE_G, CODE_OR, JOYCODE_2_RIGHT)),
                new ipd(IPT_AD_STICK_X | IPF_PLAYER3, "AD Stick X 3", SEQ_DEF_3(KEYCODE_J, CODE_OR, JOYCODE_3_LEFT)),
                new ipd((IPT_AD_STICK_X | IPF_PLAYER3) + IPT_EXTENSION, "AD Stick X 3", SEQ_DEF_3(KEYCODE_L, CODE_OR, JOYCODE_3_RIGHT)),
                new ipd(IPT_AD_STICK_X | IPF_PLAYER4, "AD Stick X 4", SEQ_DEF_1(JOYCODE_4_LEFT)),
                new ipd((IPT_AD_STICK_X | IPF_PLAYER4) + IPT_EXTENSION, "AD Stick X 4", SEQ_DEF_1(JOYCODE_4_RIGHT)),
                new ipd(IPT_AD_STICK_Y | IPF_PLAYER1, "AD Stick Y", SEQ_DEF_3(KEYCODE_UP, CODE_OR, JOYCODE_1_UP)),
                new ipd((IPT_AD_STICK_Y | IPF_PLAYER1) + IPT_EXTENSION, "AD Stick Y", SEQ_DEF_3(KEYCODE_DOWN, CODE_OR, JOYCODE_1_DOWN)),
                new ipd(IPT_AD_STICK_Y | IPF_PLAYER2, "AD Stick Y 2", SEQ_DEF_3(KEYCODE_R, CODE_OR, JOYCODE_2_UP)),
                new ipd((IPT_AD_STICK_Y | IPF_PLAYER2) + IPT_EXTENSION, "AD Stick Y 2", SEQ_DEF_3(KEYCODE_F, CODE_OR, JOYCODE_2_DOWN)),
                new ipd(IPT_AD_STICK_Y | IPF_PLAYER3, "AD Stick Y 3", SEQ_DEF_3(KEYCODE_I, CODE_OR, JOYCODE_3_UP)),
                new ipd((IPT_AD_STICK_Y | IPF_PLAYER3) + IPT_EXTENSION, "AD Stick Y 3", SEQ_DEF_3(KEYCODE_K, CODE_OR, JOYCODE_3_DOWN)),
                new ipd(IPT_AD_STICK_Y | IPF_PLAYER4, "AD Stick Y 4", SEQ_DEF_1(JOYCODE_4_UP)),
                new ipd((IPT_AD_STICK_Y | IPF_PLAYER4) + IPT_EXTENSION, "AD Stick Y 4", SEQ_DEF_1(JOYCODE_4_DOWN)),
                new ipd(IPT_UNKNOWN, "UNKNOWN", SEQ_DEF_0()),
                new ipd(IPT_END, null, SEQ_DEF_0()) /* returned when there is no match */};

    /*TODO*///    struct ipd inputport_defaults_backup[sizeof(inputport_defaults)/sizeof(struct ipd)];
    /**
     * ************************************************************************
     */
    /* Generic IO */

    /*TODO*///   static int readint(void *f,UINT32 *num)
/*TODO*///    {
/*TODO*///            unsigned i;
/*TODO*///
/*TODO*///            *num = 0;
/*TODO*///            for (i = 0;i < sizeof(UINT32);i++)
/*TODO*///            {
/*TODO*///                    unsigned char c;
/*TODO*///
/*TODO*///
/*TODO*///                    *num <<= 8;
/*TODO*///                    if (osd_fread(f,&c,1) != 1)
/*TODO*///                            return -1;
/*TODO*///                    *num |= c;
/*TODO*///            }
/*TODO*///
/*TODO*///            return 0;
/*TODO*///    }

    /*TODO*///    static void writeint(void *f,UINT32 num)
/*TODO*///    {
/*TODO*///            unsigned i;
/*TODO*///
/*TODO*///            for (i = 0;i < sizeof(UINT32);i++)
/*TODO*///            {
/*TODO*///                    unsigned char c;
/*TODO*///
/*TODO*///
/*TODO*///                    c = (num >> 8 * (sizeof(UINT32)-1)) & 0xff;
/*TODO*///                    osd_fwrite(f,&c,1);
/*TODO*///                    num <<= 8;
/*TODO*///            }
/*TODO*///    }

    /*TODO*///    static int readword(void *f,UINT16 *num)
/*TODO*///    {
/*TODO*///            unsigned i;
/*TODO*///            int res;
/*TODO*///
/*TODO*///            res = 0;
/*TODO*///            for (i = 0;i < sizeof(UINT16);i++)
/*TODO*///            {
/*TODO*///                    unsigned char c;
/*TODO*///
/*TODO*///
/*TODO*///                    res <<= 8;
/*TODO*///                    if (osd_fread(f,&c,1) != 1)
/*TODO*///                            return -1;
/*TODO*///                    res |= c;
/*TODO*///            }

    /*TODO*///            *num = res;
/*TODO*///            return 0;
/*TODO*///    }

    /*TODO*///    static void writeword(void *f,UINT16 num)
/*TODO*///    {
/*TODO*///            unsigned i;
/*TODO*///
/*TODO*///            for (i = 0;i < sizeof(UINT16);i++)
/*TODO*///            {
/*TODO*///                    unsigned char c;
/*TODO*///
/*TODO*///
/*TODO*///                    c = (num >> 8 * (sizeof(UINT16)-1)) & 0xff;
/*TODO*///                    osd_fwrite(f,&c,1);
/*TODO*///                    num <<= 8;
/*TODO*///            }
/*TODO*///    }

    /*TODO*///    static int seq_read_ver_8(void* f, InputSeq* seq)
/*TODO*///    {
/*TODO*///            int j,len;
/*TODO*///            UINT32 i;
/*TODO*///            UINT16 w;
/*TODO*///
/*TODO*///            if (readword(f,&w) != 0)
/*TODO*///                    return -1;

    /*TODO*///            len = w;
/*TODO*///            seq_set_0(seq);
/*TODO*///            for(j=0;j<len;++j)
/*TODO*///            {
/*TODO*///                    if (readint(f,&i) != 0)
/*TODO*///                            return -1;
/*TODO*///                    (*seq)[j] = savecode_to_code(i);
/*TODO*///            }
/*TODO*///
/*TODO*///            return 0;
/*TODO*///      }

    /*TODO*///    static int seq_read(void* f, InputSeq* seq, int ver)
/*TODO*///      {
/*TODO*///                    return seq_read_ver_8(f,seq);
/*TODO*///      }

    /*TODO*///    static void seq_write(void* f, InputSeq* seq)
/*TODO*///      {
/*TODO*///            int j,len;
/*TODO*///            for(len=0;len<SEQ_MAX;++len)
/*TODO*///                    if ((*seq)[len] == CODE_NONE)
/*TODO*///                            break;
/*TODO*///            writeword(f,len);
/*TODO*///            for(j=0;j<len;++j)
/*TODO*///                    writeint(f, code_to_savecode( (*seq)[j] ));
/*TODO*///      }
    /**
     * ************************************************************************
     */
    /* Load */
    public static void load_default_keys() {
        /*TODO*///            void *f;
/*TODO*///
/*TODO*///
/*TODO*///            osd_customize_inputport_defaults(inputport_defaults);
/*TODO*///            memcpy(inputport_defaults_backup,inputport_defaults,sizeof(inputport_defaults));
/*TODO*///
/*TODO*///            if ((f = osd_fopen("default",0,OSD_FILETYPE_CONFIG,0)) != 0)
/*TODO*///            {
/*TODO*///                    char buf[8];
/*TODO*///                    int version;

        /* read header */
        /*TODO*///                   if (osd_fread(f,buf,8) != 8)
/*TODO*///                            goto getout;
/*TODO*///
/*TODO*///                    if (memcmp(buf,MAMEDEFSTRING_V5,8) == 0)
/*TODO*///                            version = 5;
/*TODO*///                    else if (memcmp(buf,MAMEDEFSTRING_V6,8) == 0)
/*TODO*///                            version = 6;
/*TODO*///                    else if (memcmp(buf,MAMEDEFSTRING_V7,8) == 0)
/*TODO*///                            version = 7;
/*TODO*///                    else if (memcmp(buf,MAMEDEFSTRING_V8,8) == 0)
/*TODO*///                            version = 8;
/*TODO*///                    else
/*TODO*///                            goto getout;	/* header invalid */

        /*TODO*///                    for (;;)
/*TODO*///                    {
/*TODO*///                            UINT32 type;
/*TODO*///                            InputSeq def_seq;
/*TODO*///                            InputSeq seq;
/*TODO*///                            int i;

        /*TODO*///                            if (readint(f,&type) != 0)
/*TODO*///                                    goto getout;

        /*TODO*///                            if (seq_read(f,&def_seq,version)!=0)
/*TODO*///                                    goto getout;
/*TODO*///                            if (seq_read(f,&seq,version)!=0)
/*TODO*///                                    goto getout;

        /*TODO*///                            i = 0;
/*TODO*///                            while (inputport_defaults[i].type != IPT_END)
/*TODO*///                            {
/*TODO*///                                    if (inputport_defaults[i].type == type)
/*TODO*///                                    {
/*TODO*///                                            /* load stored settings only if the default hasn't changed */
/*TODO*///                                            if (seq_cmp(&inputport_defaults[i].seq,&def_seq)==0)
/*TODO*///                                                    seq_copy(&inputport_defaults[i].seq,&seq);
/*TODO*///                                    }
/*TODO*///
/*TODO*///                                    i++;
/*TODO*///                            }
/*TODO*///                    }
/*TODO*///
/*TODO*///    getout:
/*TODO*///                    osd_fclose(f);
/*TODO*////*TODO*///            }
    }

    public static void save_default_keys() {
        /*TODO*///            void *f;


        /*TODO*///            if ((f = osd_fopen("default",0,OSD_FILETYPE_CONFIG,1)) != 0)
/*TODO*///            {
/*TODO*///                    int i;
        /* write header */
        /*TODO*///                    osd_fwrite(f,MAMEDEFSTRING_V8,8);

        /*TODO*///                    i = 0;
/*TODO*///                    while (inputport_defaults[i].type != IPT_END)
/*TODO*///                    {
/*TODO*///                            writeint(f,inputport_defaults[i].type);

        /*TODO*///                            seq_write(f,&inputport_defaults_backup[i].seq);
/*TODO*///                            seq_write(f,&inputport_defaults[i].seq);

        /*TODO*///                            i++;
/*TODO*///                    }

        /*TODO*///                    osd_fclose(f);
/*TODO*///            }
/*TODO*///            memcpy(inputport_defaults,inputport_defaults_backup,sizeof(inputport_defaults_backup));
    }

    /*TODO*///    static int input_port_read_ver_8(void *f,struct InputPort *in)
/*TODO*///    {
/*TODO*///            UINT32 i;
/*TODO*///            UINT16 w;
/*TODO*///            if (readint(f,&i) != 0)
/*TODO*///                    return -1;
/*TODO*///            in[in_ptr].type = i;
/*TODO*///
/*TODO*///            if (readword(f,&w) != 0)
/*TODO*///                    return -1;
/*TODO*///            in[in_ptr].mask = w;

    /*TODO*///            if (readword(f,&w) != 0)
/*TODO*///                    return -1;
/*TODO*///            in[in_ptr].default_value = w;

    /*TODO*///            if (seq_read_ver_8(f,&in[in_ptr].seq) != 0)
/*TODO*///                    return -1;

    /*TODO*///            return 0;
/*TODO*///    }

    /*TODO*///    static int input_port_read(void *f,struct InputPort *in, int ver)
/*TODO*///    {
/*TODO*///    #ifdef NOLEGACY
/*TODO*///            if (ver==8)
/*TODO*///                    return input_port_read_ver_8(f,in);
/*TODO*///    #else
/*TODO*///            switch (ver) {
/*TODO*///                    case 5 : return	input_port_read_ver_5(f,in);
/*TODO*///                    case 6 : return	input_port_read_ver_6(f,in);
/*TODO*///                    case 7 : return	input_port_read_ver_7(f,in);
/*TODO*///                    case 8 : return	input_port_read_ver_8(f,in);
/*TODO*///            }
/*TODO*///    #endif
/*TODO*///            return -1;
/*TODO*///    }

    /*TODO*///    static void input_port_write(void *f,struct InputPort *in)
/*TODO*///    {
/*TODO*///            writeint(f,in[in_ptr].type);
/*TODO*///            writeword(f,in[in_ptr].mask);
/*TODO*///            writeword(f,in[in_ptr].default_value);
/*TODO*///            seq_write(f,&in[in_ptr].seq);
/*TODO*///    }
    public static int load_input_port_settings() {
        /*TODO*///            void *f;


        /*TODO*///            load_default_keys();

        /*TODO*///            if ((f = osd_fopen(Machine->gamedrv->name,0,OSD_FILETYPE_CONFIG,0)) != 0)
/*TODO*///            {

        /*TODO*///                    struct InputPort *in;

        /*TODO*///                    unsigned int total,savedtotal;
/*TODO*///                    char buf[8];
/*TODO*///                    int i;
/*TODO*///                    int version;

        /*TODO*///                    in = Machine->input_ports_default;

        /* calculate the size of the array */
        /*TODO*///                    total = 0;
/*TODO*///                    while (in[in_ptr].type != IPT_END)
/*TODO*///                    {
/*TODO*///                            total++;
/*TODO*///                            in++;
/*TODO*///                    }

        /* read header */
        /*TODO*///                    if (osd_fread(f,buf,8) != 8)
/*TODO*///                            goto getout;

        /*TODO*///                    if (memcmp(buf,MAMECFGSTRING_V5,8) == 0)
/*TODO*///                            version = 5;
/*TODO*///                    else if (memcmp(buf,MAMECFGSTRING_V6,8) == 0)
/*TODO*///                            version = 6;
/*TODO*///                    else if (memcmp(buf,MAMECFGSTRING_V7,8) == 0)
/*TODO*///                            version = 7;
/*TODO*///                    else if (memcmp(buf,MAMECFGSTRING_V8,8) == 0)
/*TODO*///                            version = 8;
/*TODO*///                    else
/*TODO*///                            goto getout;	/* header invalid */

        /* read array size */
        /*TODO*///                    if (readint(f,&savedtotal) != 0)
/*TODO*///                            goto getout;
/*TODO*///                    if (total != savedtotal)
/*TODO*///                            goto getout;	/* different size */

        /* read the original settings and compare them with the ones defined in the driver */
        /*TODO*///                    in = Machine->input_ports_default;
/*TODO*///                    while (in[in_ptr].type != IPT_END)
/*TODO*///                    {
/*TODO*///                            struct InputPort saved;
/*TODO*///
/*TODO*///                            if (input_port_read(f,&saved,version) != 0)
/*TODO*///                                    goto getout;

        /*TODO*///                            if (in[in_ptr].mask != saved.mask ||
/*TODO*///                                    in[in_ptr].default_value != saved.default_value ||
/*TODO*///                                    in[in_ptr].type != saved.type ||
/*TODO*///                                    seq_cmp(&in[in_ptr].seq,&saved.seq) !=0 )
/*TODO*///                            goto getout;	/* the default values are different */

        /*TODO*///                            in++;
/*TODO*///                    }

        /* read the current settings */
        /*TODO*///                    in = Machine->input_ports;
/*TODO*///                    while (in[in_ptr].type != IPT_END)
/*TODO*///                    {
/*TODO*///                            if (input_port_read(f,in,version) != 0)
/*TODO*///                                    goto getout;
/*TODO*///                            in++;
/*TODO*///                    }

        /* Clear the coin & ticket counters/flags - LBO 042898 */
        for (int i = 0; i < COIN_COUNTERS; i++) {
            coins[i] = lastcoin[i] = coinlockedout[i] = 0;
        }
        dispensed_tickets = 0;

        /*TODO*///                   /* read in the coin/ticket counters */
/*TODO*///                    for (i = 0; i < COIN_COUNTERS; i ++)
/*TODO*///                    {
/*TODO*///                            if (readint(f,&coins[i]) != 0)
/*TODO*///                                    goto getout;
/*TODO*///                    }
/*TODO*///                    if (readint(f,&dispensed_tickets) != 0)
/*TODO*///                            goto getout;
/*TODO*///
/*TODO*///                    mixer_read_config(f);
/*TODO*///
/*TODO*///    getout:
/*TODO*///                    osd_fclose(f);
/*TODO*///            }

        /* All analog ports need initialization */
        {
            int i;
            for (i = 0; i < MAX_INPUT_PORTS; i++) {
                input_analog_init[i] = 1;
            }
        }

        /*TODO*///            update_input_ports();

        /* if we didn't find a saved config, return 0 so the main core knows that it */
        /* is the first time the game is run and it should diplay the disclaimer. */
        /*TODO*///            if (f) return 1;
/*TODO*///            else 
        return 0;
    }

    /**
     * ************************************************************************
     */
    /* Save */
    public static void save_input_port_settings() {
        /*TODO*///            void *f;

        /*TODO*///            save_default_keys();

        /*TODO*///            if ((f = osd_fopen(Machine->gamedrv->name,0,OSD_FILETYPE_CONFIG,1)) != 0)
/*TODO*///            {
/*TODO*///
/*TODO*///                    struct InputPort *in;

        /*TODO*///                    int total;
/*TODO*///                    int i;
        /*TODO*///                    in = Machine->input_ports_default;

        /* calculate the size of the array */
        /*TODO*///                    total = 0;
/*TODO*///                    while (in[in_ptr].type != IPT_END)
/*TODO*///                    {
/*TODO*///                            total++;
/*TODO*///                            in++;
/*TODO*///                    }

        /* write header */
        /*TODO*///                    osd_fwrite(f,MAMECFGSTRING_V8,8);
                    /* write array size */
        /*TODO*///                    writeint(f,total);
                    /* write the original settings as defined in the driver */
        /*TODO*///                    in = Machine->input_ports_default;
/*TODO*///                    while (in[in_ptr].type != IPT_END)
/*TODO*///                    {
/*TODO*///                            input_port_write(f,in);
/*TODO*///                            in++;
/*TODO*///                    }
                    /* write the current settings */
        /*TODO*///                    in = Machine->input_ports;
/*TODO*///                    while (in[in_ptr].type != IPT_END)
/*TODO*///                    {
/*TODO*///                            input_port_write(f,in);
/*TODO*///                            in++;
/*TODO*///                    }

        /* write out the coin/ticket counters for this machine - LBO 042898 */
        /*TODO*///                    for (i = 0; i < COIN_COUNTERS; i ++)
/*TODO*///                            writeint(f,coins[i]);
/*TODO*///                    writeint(f,dispensed_tickets);

        /*TODO*///                    mixer_write_config(f);

        /*TODO*///                    osd_fclose(f);
/*TODO*///            }
    }

    /* Note that the following 3 routines have slightly different meanings with analog ports */
    public static String input_port_name(InputPort[] in, int in_ptr) {
        int i;
        /*unsigned*/
        int type;

        if (in[in_ptr].name != IP_NAME_DEFAULT) {
            return in[in_ptr].name;
        }

        i = 0;

        if ((in[in_ptr].type & ~IPF_MASK) == IPT_EXTENSION) {
            type = in[in_ptr - 1].type & (~IPF_MASK | IPF_PLAYERMASK);
        } else {
            type = in[in_ptr].type & (~IPF_MASK | IPF_PLAYERMASK);
        }

        while (inputport_defaults[i].type != IPT_END
                && inputport_defaults[i].type != type) {
            i++;
        }

        if ((in[in_ptr].type & ~IPF_MASK) == IPT_EXTENSION) {
            return inputport_defaults[i + 1].name;
        } else {
            return inputport_defaults[i].name;
        }
    }

    public static int[] input_port_type_seq(int type) {
        int i = 0;

        while (inputport_defaults[i].type != IPT_END
                && inputport_defaults[i].type != type) {
            i++;
        }

        return inputport_defaults[i].seq;
    }
    public static int[] ip_none = SEQ_DEF_1(CODE_NONE);

    public static int[] input_port_seq(InputPort[] in, int in_ptr) {
        int i, type;

        while (seq_get_1(in[in_ptr].seq) == CODE_PREVIOUS) {
            in_ptr--;
        }

        if ((in[in_ptr].type & ~IPF_MASK) == IPT_EXTENSION) {
            type = in[in_ptr - 1].type & (~IPF_MASK | IPF_PLAYERMASK);
            /* if port is disabled, or cheat with cheats disabled, return no key */
            if ((in[in_ptr - 1].type & IPF_UNUSED) != 0 || (options.cheat == 0 && (in[in_ptr - 1].type & IPF_CHEAT) != 0)) {
                return ip_none;
            }
        } else {
            type = in[in_ptr].type & (~IPF_MASK | IPF_PLAYERMASK);
            /* if port is disabled, or cheat with cheats disabled, return no key */
            if ((in[in_ptr].type & IPF_UNUSED) != 0 || (options.cheat == 0 && (in[in_ptr].type & IPF_CHEAT) != 0)) {
                return ip_none;
            }
        }

        if (seq_get_1(in[in_ptr].seq) != CODE_DEFAULT) {
            return in[in_ptr].seq;
        }

        i = 0;

        while (inputport_defaults[i].type != IPT_END
                && inputport_defaults[i].type != type) {
            i++;
        }

        if ((in[in_ptr].type & ~IPF_MASK) == IPT_EXTENSION) {
            return inputport_defaults[i + 1].seq;
        } else {
            return inputport_defaults[i].seq;
        }
    }

    public static void update_analog_port(int port) {
        //InputPort in;
        int current, delta, type, sensitivity, min, max, default_value;
        int axis, is_stick, check_bounds;
        int[] incseq;
        int[] decseq;
        int keydelta;
        int player;

        /* get input definition */
            //in = input_analog[port];

        /* if we're not cheating and this is a cheat-only port, bail */
        if (options.cheat == 0 && (Machine.input_ports[input_analog[port]].type & IPF_CHEAT) != 0) {
            return;
        }
        type = (Machine.input_ports[input_analog[port]].type & ~IPF_MASK);

        decseq = input_port_seq(Machine.input_ports, input_analog[port]);
        incseq = input_port_seq(Machine.input_ports, input_analog[port] + 1);

        keydelta = IP_GET_DELTA(Machine.input_ports, input_analog[port]);

        switch (type) {
            case IPT_PADDLE:
                axis = X_AXIS;
                is_stick = 0;
                check_bounds = 1;
                break;
            case IPT_PADDLE_V:
                axis = Y_AXIS;
                is_stick = 0;
                check_bounds = 1;
                break;
            case IPT_DIAL:
                axis = X_AXIS;
                is_stick = 0;
                check_bounds = 0;
                break;
            case IPT_DIAL_V:
                axis = Y_AXIS;
                is_stick = 0;
                check_bounds = 0;
                break;
            case IPT_TRACKBALL_X:
                axis = X_AXIS;
                is_stick = 0;
                check_bounds = 0;
                break;
            case IPT_TRACKBALL_Y:
                axis = Y_AXIS;
                is_stick = 0;
                check_bounds = 0;
                break;
            case IPT_AD_STICK_X:
                axis = X_AXIS;
                is_stick = 1;
                check_bounds = 1;
                break;
            case IPT_AD_STICK_Y:
                axis = Y_AXIS;
                is_stick = 1;
                check_bounds = 1;
                break;
            case IPT_PEDAL:
                axis = Y_AXIS;
                is_stick = 0;
                check_bounds = 1;
                break;
            default:
                /* Use some defaults to prevent crash */
                axis = X_AXIS;
                is_stick = 0;
                check_bounds = 0;
                if (errorlog!=null)
		    fprintf (errorlog,"Oops, polling non analog device in update_analog_port()????\n");

        }

        sensitivity = IP_GET_SENSITIVITY(Machine.input_ports, input_analog[port]);
        min = IP_GET_MIN(Machine.input_ports, input_analog[port]);
        max = IP_GET_MAX(Machine.input_ports, input_analog[port]);
        default_value = Machine.input_ports[input_analog[port]].default_value * 100 / sensitivity;
        /* extremes can be either signed or unsigned */
        if (min > max) {
            if (Machine.input_ports[input_analog[port]].mask > 0xff) {
                min = min - 0x10000;
            } else {
                min = min - 0x100;
            }
        }

        input_analog_previous_value[port] = input_analog_current_value[port];

        /* if IPF_CENTER go back to the default position */
        /* sticks are handled later... */
        if ((Machine.input_ports[input_analog[port]].type & IPF_CENTER) != 0 && (is_stick == 0)) {
            input_analog_current_value[port] = Machine.input_ports[input_analog[port]].default_value * 100 / sensitivity;
        }

        current = input_analog_current_value[port];

        delta = 0;

        switch (Machine.input_ports[input_analog[port]].type & IPF_PLAYERMASK) {
            case IPF_PLAYER2:
                player = 1;
                break;
            case IPF_PLAYER3:
                player = 2;
                break;
            case IPF_PLAYER4:
                player = 3;
                break;
            case IPF_PLAYER1:
            default:
                player = 0;
                break;
        }

        if (axis == X_AXIS) {
            delta = mouse_delta_x[player];
        } else {
            delta = mouse_delta_y[player];
        }

        if (seq_pressed(decseq)) {
            delta -= keydelta;
        }

        if (type != IPT_PEDAL) {
            if (seq_pressed(incseq)) {
                delta += keydelta;
            }
        } else {
            /* is this cheesy or what? */
            if (delta == 0 && seq_get_1(incseq) == KEYCODE_Y) {
                delta += keydelta;
            }
            delta = -delta;
        }

        if ((Machine.input_ports[input_analog[port]].type & IPF_REVERSE) != 0) {
            delta = -delta;
        }

        if (is_stick == 0) {
            int _new, prev;

            /* center stick */
            if ((delta == 0) && (Machine.input_ports[input_analog[port]].type & IPF_CENTER) != 0) {
                if (current > default_value) {
                    delta = -100 / sensitivity;
                }
                if (current < default_value) {
                    delta = 100 / sensitivity;
                }
            }

            /* An analog joystick which is not at zero position (or has just */
            /* moved there) takes precedence over all other computations */
            /* analog_x/y holds values from -128 to 128 (yes, 128, not 127) */
            if (axis == X_AXIS) {
                _new = analog_current_x[player];
                prev = analog_previous_x[player];
            } else {
                _new = analog_current_y[player];
                prev = analog_previous_y[player];
            }

            if ((_new != 0) || (_new - prev != 0)) {
                delta = 0;

                if ((Machine.input_ports[input_analog[port]].type & IPF_REVERSE) != 0) {
                    _new = -_new;
                    prev = -prev;
                }

                /* apply sensitivity using a logarithmic scale */
                if (Machine.input_ports[input_analog[port]].mask > 0xff) {
                    if (_new > 0) {
                        current = (int) (Math.pow(_new / 32768.0, 100.0 / sensitivity) * (max - Machine.input_ports[input_analog[port]].default_value)
                                + Machine.input_ports[input_analog[port]].default_value) * 100 / sensitivity;
                    } else {
                        current = (int) (Math.pow(-_new / 32768.0, 100.0 / sensitivity) * (min - Machine.input_ports[input_analog[port]].default_value)
                                + Machine.input_ports[input_analog[port]].default_value) * 100 / sensitivity;
                    }
                } else {
                    if (_new > 0) {
                        current = (int) (Math.pow(_new / 128.0, 100.0 / sensitivity) * (max - Machine.input_ports[input_analog[port]].default_value)
                                + Machine.input_ports[input_analog[port]].default_value) * 100 / sensitivity;
                    } else {
                        current = (int) (Math.pow(-_new / 128.0, 100.0 / sensitivity) * (min - Machine.input_ports[input_analog[port]].default_value)
                                + Machine.input_ports[input_analog[port]].default_value) * 100 / sensitivity;
                    }
                }
            }
        }

        current += delta;

        if (check_bounds != 0) {
            if ((current * sensitivity + 50) / 100 < min) {
                current = (min * 100 + sensitivity / 2) / sensitivity;
            }
            if ((current * sensitivity + 50) / 100 > max) {
                current = (max * 100 + sensitivity / 2) / sensitivity;
            }
        }

        input_analog_current_value[port] = current;
    }

    static void scale_analog_port(int port) {
        //InputPort in;
        int delta, current, sensitivity;

        //in = input_analog[port];
        sensitivity = IP_GET_SENSITIVITY(Machine.input_ports, input_analog[port]);

        delta = cpu_scalebyfcount(input_analog_current_value[port] - input_analog_previous_value[port]);

        current = input_analog_previous_value[port] + delta;

        input_port_value[port] &= ~Machine.input_ports[input_analog[port]].mask;
        input_port_value[port] |= ((current * sensitivity + 50) / 100) & Machine.input_ports[input_analog[port]].mask;

        /*TODO*///            if (playback)
/*TODO*///                     readword(playback,&input_port_value[port]);
/*TODO*///             if (record)
/*TODO*///                     writeword(record,input_port_value[port]);
    }
    public static final int MAX_INPUT_BITS = 1024;
    static int[] impulsecount = new int[MAX_INPUT_BITS];
    static int[] waspressed = new int[MAX_INPUT_BITS];
    public static final int MAX_JOYSTICKS = 3;
    public static final int MAX_PLAYERS = 4;
    static int update_serial_number = 1;
    static int[][] joyserial = new int[MAX_JOYSTICKS * MAX_PLAYERS][4];

    public static void update_input_ports() {
        int port, ib;
        InputPort[] in;
        int in_ptr = 0;

        /* clear all the values before proceeding */
        for (port = 0; port < MAX_INPUT_PORTS; port++) {
            input_port_value[port] = 0;
            input_vblank[port] = 0;
            input_analog[port] = 0;
        }

        in = Machine.input_ports;

        if (in[in_ptr].type == IPT_END) {
            return; 	/* nothing to do */
        }

        /* make sure the InputPort definition is correct */
        if (in[in_ptr].type != IPT_PORT) {
            if (errorlog!=null) fprintf(errorlog,"Error in InputPort definition: expecting PORT_START\n");
            return;
        } else {
            in_ptr++;
        }

        //#ifdef MRU_JOYSTICK
            /* scan all the joystick ports */
        port = 0;
        while (in[in_ptr].type != IPT_END && port < MAX_INPUT_PORTS) {
            while (in[in_ptr].type != IPT_END && in[in_ptr].type != IPT_PORT) {
                if ((in[in_ptr].type & ~IPF_MASK) >= IPT_JOYSTICK_UP
                        && (in[in_ptr].type & ~IPF_MASK) <= IPT_JOYSTICKLEFT_RIGHT) {
                    int[] seq;

                    seq = input_port_seq(in, in_ptr);

                    if (seq_get_1(seq) != 0 && seq_get_1(seq) != CODE_NONE) {
                        int joynum, joydir, player;

                        player = 0;
                        if ((in[in_ptr].type & IPF_PLAYERMASK) == IPF_PLAYER2) {
                            player = 1;
                        } else if ((in[in_ptr].type & IPF_PLAYERMASK) == IPF_PLAYER3) {
                            player = 2;
                        } else if ((in[in_ptr].type & IPF_PLAYERMASK) == IPF_PLAYER4) {
                            player = 3;
                        }

                        joynum = player * MAX_JOYSTICKS
                                + ((in[in_ptr].type & ~IPF_MASK) - IPT_JOYSTICK_UP) / 4;
                        joydir = ((in[in_ptr].type & ~IPF_MASK) - IPT_JOYSTICK_UP) % 4;

                        if (seq_pressed(seq)) {
                            if (joyserial[joynum][joydir] == 0) {
                                joyserial[joynum][joydir] = update_serial_number;
                            }
                        } else {
                            joyserial[joynum][joydir] = 0;
                        }
                    }
                }
                in_ptr++;
            }

            port++;
            if (in[in_ptr].type == IPT_PORT) {
                in_ptr++;
            }
        }
        update_serial_number += 1;

        //in = Machine.input_ports;
        in_ptr = 0;

        /* already made sure the InputPort definition is correct */
        in_ptr++;
    //#endif


        /* scan all the input ports */
        port = 0;
        ib = 0;
        int save_ptr = 0;
        while (in[in_ptr].type != IPT_END && port < MAX_INPUT_PORTS) {
                    //InputPort start;


            /* first of all, scan the whole input port definition and build the */
            /* default value. I must do it before checking for input because otherwise */
            /* multiple keys associated with the same input bit wouldn't work (the bit */
            /* would be reset to its default value by the second entry, regardless if */
            /* the key associated with the first entry was pressed) */
            save_ptr = in_ptr;
            while (in[in_ptr].type != IPT_END && in[in_ptr].type != IPT_PORT) {
                if ((in[in_ptr].type & ~IPF_MASK) != IPT_DIPSWITCH_SETTING
                        && /* skip dipswitch definitions */ (in[in_ptr].type & ~IPF_MASK) != IPT_EXTENSION) /* skip analog extension fields */ {
                    input_port_value[port]
                            = (char) ((input_port_value[port] & ~in[in_ptr].mask) | (in[in_ptr].default_value & in[in_ptr].mask));
                }

                in_ptr++;
            }

            /* now get back to the beginning of the input port and check the input bits. */
            for (in_ptr = save_ptr;
                    in[in_ptr].type != IPT_END && in[in_ptr].type != IPT_PORT;
                    in_ptr++, ib++) {
                if ((in[in_ptr].type & ~IPF_MASK) != IPT_DIPSWITCH_SETTING
                        && /* skip dipswitch definitions */ (in[in_ptr].type & ~IPF_MASK) != IPT_EXTENSION) /* skip analog extension fields */ {
                    if ((in[in_ptr].type & ~IPF_MASK) == IPT_VBLANK) {
                        input_vblank[port] ^= in[in_ptr].mask;
                        input_port_value[port] ^= in[in_ptr].mask;
                        if (errorlog!=null && Machine.drv.vblank_duration == 0) 
                            fprintf(errorlog,"Warning: you are using IPT_VBLANK with vblank_duration = 0. You need to increase vblank_duration for IPT_VBLANK to work.\n");

                    } /* If it's an analog control, handle it appropriately */ else if (((in[in_ptr].type & ~IPF_MASK) > IPT_ANALOG_START)
                            && ((in[in_ptr].type & ~IPF_MASK) < IPT_ANALOG_END)) /* LBO 120897 */ {
                        input_analog[port] = in_ptr;
                        /* reset the analog port on first access */
                        if (input_analog_init[port] != 0) {
                            input_analog_init[port] = 0;
                            input_analog_current_value[port] = input_analog_previous_value[port]
                                    = in[in_ptr].default_value * 100 / IP_GET_SENSITIVITY(in, in_ptr);
                        }
                    } else {
                        int[] seq;

                        seq = input_port_seq(in, in_ptr);

                        if (seq_pressed(seq)) {
                            /* skip if coin input and it's locked out */
                            if ((in[in_ptr].type & ~IPF_MASK) >= IPT_COIN1
                                    && (in[in_ptr].type & ~IPF_MASK) <= IPT_COIN4
                                    && coinlockedout[(in[in_ptr].type & ~IPF_MASK) - IPT_COIN1] != 0) {
                                continue;
                            }

                            /* if IPF_RESET set, reset the first CPU */
                            if ((in[in_ptr].type & IPF_RESETCPU) != 0 && waspressed[ib] == 0) {
                                cpu_set_reset_line(0, PULSE_LINE);
                            }

                            if ((in[in_ptr].type & IPF_IMPULSE) != 0) {
                                if (errorlog!=null && IP_GET_IMPULSE(in,in_ptr) == 0){
                                    fprintf(errorlog,"error in input port definition: IPF_IMPULSE with length = 0\n");
                                }
                                if (waspressed[ib] == 0) {
                                    impulsecount[ib] = IP_GET_IMPULSE(in, in_ptr);
                                }
                                /* the input bit will be toggled later */
                            } else if ((in[in_ptr].type & IPF_TOGGLE) != 0) {
                                if (waspressed[ib] == 0) {
                                    in[in_ptr].default_value ^= in[in_ptr].mask;
                                    input_port_value[port] ^= in[in_ptr].mask;
                                }
                            } else if ((in[in_ptr].type & ~IPF_MASK) >= IPT_JOYSTICK_UP
                                    && (in[in_ptr].type & ~IPF_MASK) <= IPT_JOYSTICKLEFT_RIGHT) {
                                int joynum, joydir, mask, player;

                                player = 0;
                                if ((in[in_ptr].type & IPF_PLAYERMASK) == IPF_PLAYER2) {
                                    player = 1;
                                } else if ((in[in_ptr].type & IPF_PLAYERMASK) == IPF_PLAYER3) {
                                    player = 2;
                                } else if ((in[in_ptr].type & IPF_PLAYERMASK) == IPF_PLAYER4) {
                                    player = 3;
                                }

                                joynum = player * MAX_JOYSTICKS
                                        + ((in[in_ptr].type & ~IPF_MASK) - IPT_JOYSTICK_UP) / 4;
                                joydir = ((in[in_ptr].type & ~IPF_MASK) - IPT_JOYSTICK_UP) % 4;

                                mask = in[in_ptr].mask;

                                /* avoid movement in two opposite directions */
                                if (joyserial[joynum][joydir ^ 1] != 0) {
                                    mask = 0;
                                } else if ((in[in_ptr].type & IPF_4WAY) != 0) {
                                    int mru_dir = joydir;
                                    int mru_serial = 0;
                                    int dir;


                                    /* avoid diagonal movements, use mru button */
                                    for (dir = 0; dir < 4; dir++) {
                                        if (joyserial[joynum][dir] > mru_serial) {
                                            mru_serial = joyserial[joynum][dir];
                                            mru_dir = dir;
                                        }
                                    }

                                    if (mru_dir != joydir) {
                                        mask = 0;
                                    }
                                }

                                input_port_value[port] ^= mask;
                            } else {
                                input_port_value[port] ^= in[in_ptr].mask;
                            }

                            waspressed[ib] = 1;
                        } else {
                            waspressed[ib] = 0;
                        }

                        if (((in[in_ptr].type & IPF_IMPULSE) != 0) && impulsecount[ib] > 0) {
                            impulsecount[ib]--;
                            waspressed[ib] = 1;
                            input_port_value[port] ^= in[in_ptr].mask;
                        }
                    }
                }
            }

            port++;
            if (in[in_ptr].type == IPT_PORT) {
                in_ptr++;
            }
        }

        /*TODO*///            if (playback)
/*TODO*///            {
/*TODO*///                    int i;
/*TODO*///
/*TODO*///                    for (i = 0; i < MAX_INPUT_PORTS; i ++)
/*TODO*///                            readword(playback,&input_port_value[i]);
/*TODO*///            }
/*TODO*///            if (record)
/*TODO*///            {
/*TODO*///                    int i;
/*TODO*///
/*TODO*///                    for (i = 0; i < MAX_INPUT_PORTS; i ++)
/*TODO*///                            writeword(record,input_port_value[i]);
/*TODO*///            }
    }
    /* used the the CPU interface to notify that VBlank has ended, so we can update */
    /* IPT_VBLANK input ports. */

    public static void inputport_vblank_end() {
        int port;
        int i;

        for (port = 0; port < MAX_INPUT_PORTS; port++) {
            if (input_vblank[port] != 0) {
                input_port_value[port] ^= input_vblank[port];
                input_vblank[port] = 0;
            }
        }

        /* poll all the analog joysticks */
        /*TODO*///            osd_poll_joysticks();

        /* update the analog devices */
        for (i = 0; i < OSD_MAX_JOY_ANALOG; i++) {
            /* update the analog joystick position */
            analog_previous_x[i] = analog_current_x[i];
            analog_previous_y[i] = analog_current_y[i];
            /*TODO*///                    osd_analogjoy_read (i, &(analog_current_x[i]), &(analog_current_y[i]));

            /* update mouse/trackball position */
            /*TODO*///                    osd_trak_read (i, &mouse_delta_x[i], &mouse_delta_y[i]);
        }

        for (i = 0; i < MAX_INPUT_PORTS; i++) {
            if (input_analog[i] != 0) {
                update_analog_port(i);
            }
        }
    }

    public static int readinputport(int port) {
        InputPort _in;

        /* Update analog ports on demand */
        _in = Machine.input_ports[input_analog[port]];
        if (_in != null) {
            scale_analog_port(port);
        }

        return input_port_value[port];
    }
    public static ReadHandlerPtr input_port_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return readinputport(0);
        }
    };
    public static ReadHandlerPtr input_port_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return readinputport(1);
        }
    };
    public static ReadHandlerPtr input_port_2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return readinputport(2);
        }
    };
    public static ReadHandlerPtr input_port_3_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return readinputport(3);
        }
    };
    public static ReadHandlerPtr input_port_4_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return readinputport(4);
        }
    };
    public static ReadHandlerPtr input_port_5_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return readinputport(5);
        }
    };
    public static ReadHandlerPtr input_port_6_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return readinputport(6);
        }
    };
    public static ReadHandlerPtr input_port_7_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return readinputport(7);
        }
    };
    public static ReadHandlerPtr input_port_8_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return readinputport(8);
        }
    };
    public static ReadHandlerPtr input_port_9_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return readinputport(9);
        }
    };
    public static ReadHandlerPtr input_port_10_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return readinputport(10);
        }
    };
    public static ReadHandlerPtr input_port_11_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return readinputport(11);
        }
    };
    public static ReadHandlerPtr input_port_12_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return readinputport(12);
        }
    };
    public static ReadHandlerPtr input_port_13_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return readinputport(13);
        }
    };
    public static ReadHandlerPtr input_port_14_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return readinputport(14);
        }
    };
    public static ReadHandlerPtr input_port_15_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return readinputport(15);
        }
    };
    /**
     * ************************************************************************
     */
    /* InputPort conversion */

    public static int input_port_count(InputPortTiny[] src) {
        /*unsigned*/
        int total;
        int ptr = 0;
        total = 0;
        while (src[ptr].type != IPT_END) {
            int type = src[ptr].type & ~IPF_MASK;
            if (type > IPT_ANALOG_START && type < IPT_ANALOG_END) {
                total += 2;
            } else if (type != IPT_EXTENSION) {
                ++total;
            }
            ++ptr;//++src;
        }

        ++total; /* for IPT_END */

        return total;
    }

    public static InputPort[] input_port_allocate(InputPortTiny[] src) {
        int dst; //struct InputPort* dst;
        int inp_ptr = 0;
        InputPort[] base;
        int total;

        total = input_port_count(src);

        base = new InputPort[total];
        dst = 0; //dst = base;

        while (src[inp_ptr].type != IPT_END) {
            int type = src[inp_ptr].type & ~IPF_MASK;
            int ext;//const struct InputPortTiny *ext;
            int src_end;//const struct InputPortTiny *src_end;
            int/*InputCode*/ seq_default;

            if (type > IPT_ANALOG_START && type < IPT_ANALOG_END) {
                src_end = inp_ptr + 2;
            } else {
                src_end = inp_ptr + 1;
            }

            switch (type) {
                case IPT_END:
                case IPT_PORT:
                case IPT_DIPSWITCH_NAME:
                case IPT_DIPSWITCH_SETTING:
                    seq_default = CODE_NONE;
                    break;
                default:
                    seq_default = CODE_DEFAULT;
            }

            ext = src_end;
            while (inp_ptr != src_end) {
                base[dst] = new InputPort();
                base[dst].type = src[inp_ptr].type;//dst->type = src->type;
                base[dst].mask = src[inp_ptr].mask;//dst->mask = src->mask;
                base[dst].default_value = src[inp_ptr].default_value;//dst->default_value = src->default_value;
                base[dst].name = src[inp_ptr].name;//dst->name = src->name;

                if (src[ext].type == IPT_EXTENSION) {
                    int or1 = IP_GET_CODE_OR1(src[ext]);
                    int or2 = IP_GET_CODE_OR2(src[ext]);

                    if (or1 < __code_max) {
                        if (or2 < __code_max) {
                            seq_set_3(base[dst].seq, or1, CODE_OR, or2);//seq_set_3(&dst->seq, or1, CODE_OR, or2);
                        } else {
                            seq_set_1(base[dst].seq, or1);//seq_set_1(&dst->seq, or1);
                        }
                    } else {
                        if (or1 == CODE_NONE) {
                            seq_set_1(base[dst].seq, or2);//seq_set_1(&dst->seq, or2);
                        } else {
                            seq_set_1(base[dst].seq, or1);//seq_set_1(&dst->seq, or1);
                        }
                    }

                    ++ext;
                } else {
                    seq_set_1(base[dst].seq, seq_default);//seq_set_1(&dst->seq,seq_default);
                }

                ++inp_ptr;
                ++dst;
            }

            inp_ptr = ext;
        }
        base[dst] = new InputPort();
        base[dst].type = IPT_END;//dst->type = IPT_END;

        return base;
    }

    public static void input_port_free(InputPort[] dst) {
        dst = null;
    }
}
