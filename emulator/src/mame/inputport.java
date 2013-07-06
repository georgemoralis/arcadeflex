package mame;

import static mame.driverH.*;
import static mame.inputportH.*;
import static mame.inputH.*;
import static mame.input.*;

public class inputport {
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  inptport.c
/*TODO*///
/*TODO*///  Input ports handling
/*TODO*///
/*TODO*///TODO:	remove the 1 analog device per port limitation
/*TODO*///		support for inputports producing interrupts
/*TODO*///		support for extra "real" hardware (PC throttle's, spinners etc)
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///#include "driver.h"
/*TODO*///#include <math.h>
/*TODO*///
/*TODO*///
/*TODO*////* Use the MRU code for 4way joysticks */
/*TODO*///#define MRU_JOYSTICK
/*TODO*///
/*TODO*////* header identifying the version of the game.cfg file */
/*TODO*////* mame 0.36b11 */
/*TODO*///#define MAMECFGSTRING_V5 "MAMECFG\5"
/*TODO*///#define MAMEDEFSTRING_V5 "MAMEDEF\4"
/*TODO*///
/*TODO*////* mame 0.36b12 with multi key/joy extension */
/*TODO*///#define MAMECFGSTRING_V6 "MAMECFG\6"
/*TODO*///#define MAMEDEFSTRING_V6 "MAMEDEF\5"
/*TODO*///
/*TODO*////* mame 0.36b13 with and/or/not combination */
/*TODO*///#define MAMECFGSTRING_V7 "MAMECFG\7"
/*TODO*///#define MAMEDEFSTRING_V7 "MAMEDEF\6"
/*TODO*///
/*TODO*////* mame 0.36b16 with key/joy merge */
/*TODO*///#define MAMECFGSTRING_V8 "MAMECFG\x8"
/*TODO*///#define MAMEDEFSTRING_V8 "MAMEDEF\7"
/*TODO*///
/*TODO*///extern void *record;
/*TODO*///extern void *playback;
/*TODO*///
/*TODO*///extern unsigned int dispensed_tickets;
/*TODO*///extern unsigned int coins[COIN_COUNTERS];
/*TODO*///extern unsigned int lastcoin[COIN_COUNTERS];
/*TODO*///extern unsigned int coinlockedout[COIN_COUNTERS];

    static int[] input_port_value=new int[MAX_INPUT_PORTS];
    static int[] input_vblank=new int[MAX_INPUT_PORTS];

    /* Assuming a maxium of one analog input device per port BW 101297 */
    static InputPort[] input_analog=new InputPort[MAX_INPUT_PORTS];
    static int[] input_analog_current_value=new int[MAX_INPUT_PORTS];
    static int[] input_analog_previous_value=new int[MAX_INPUT_PORTS];
    static int[] input_analog_init=new int[MAX_INPUT_PORTS];
    
/*TODO*///static int mouse_delta_x[OSD_MAX_JOY_ANALOG], mouse_delta_y[OSD_MAX_JOY_ANALOG];
/*TODO*///static int analog_current_x[OSD_MAX_JOY_ANALOG], analog_current_y[OSD_MAX_JOY_ANALOG];
/*TODO*///static int analog_previous_x[OSD_MAX_JOY_ANALOG], analog_previous_y[OSD_MAX_JOY_ANALOG];
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Configuration load/save
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///char ipdn_defaultstrings[][MAX_DEFSTR_LEN] =
/*TODO*///{
/*TODO*///	"Off",
/*TODO*///	"On",
/*TODO*///	"No",
/*TODO*///	"Yes",
/*TODO*///	"Lives",
/*TODO*///	"Bonus Life",
/*TODO*///	"Difficulty",
/*TODO*///	"Demo Sounds",
/*TODO*///	"Coinage",
/*TODO*///	"Coin A",
/*TODO*///	"Coin B",
/*TODO*///	"9 Coins/1 Credit",
/*TODO*///	"8 Coins/1 Credit",
/*TODO*///	"7 Coins/1 Credit",
/*TODO*///	"6 Coins/1 Credit",
/*TODO*///	"5 Coins/1 Credit",
/*TODO*///	"4 Coins/1 Credit",
/*TODO*///	"3 Coins/1 Credit",
/*TODO*///	"8 Coins/3 Credits",
/*TODO*///	"4 Coins/2 Credits",
/*TODO*///	"2 Coins/1 Credit",
/*TODO*///	"5 Coins/3 Credits",
/*TODO*///	"3 Coins/2 Credits",
/*TODO*///	"4 Coins/3 Credits",
/*TODO*///	"4 Coins/4 Credits",
/*TODO*///	"3 Coins/3 Credits",
/*TODO*///	"2 Coins/2 Credits",
/*TODO*///	"1 Coin/1 Credit",
/*TODO*///	"4 Coins/5 Credits",
/*TODO*///	"3 Coins/4 Credits",
/*TODO*///	"2 Coins/3 Credits",
/*TODO*///	"4 Coins/7 Credits",
/*TODO*///	"2 Coins/4 Credits",
/*TODO*///	"1 Coin/2 Credits",
/*TODO*///	"2 Coins/5 Credits",
/*TODO*///	"2 Coins/6 Credits",
/*TODO*///	"1 Coin/3 Credits",
/*TODO*///	"2 Coins/7 Credits",
/*TODO*///	"2 Coins/8 Credits",
/*TODO*///	"1 Coin/4 Credits",
/*TODO*///	"1 Coin/5 Credits",
/*TODO*///	"1 Coin/6 Credits",
/*TODO*///	"1 Coin/7 Credits",
/*TODO*///	"1 Coin/8 Credits",
/*TODO*///	"1 Coin/9 Credits",
/*TODO*///	"Free Play",
/*TODO*///	"Cabinet",
/*TODO*///	"Upright",
/*TODO*///	"Cocktail",
/*TODO*///	"Flip Screen",
/*TODO*///	"Service Mode",
/*TODO*///	"Unused",
/*TODO*///	"Unknown"	/* must be the last one, mame.c relies on that */
/*TODO*///};
/*TODO*///
/*TODO*///struct ipd inputport_defaults[] =
/*TODO*///{
/*TODO*///	{ IPT_UI_CONFIGURE,         "Config Menu",       SEQ_DEF_1(KEYCODE_TAB) },
/*TODO*///	{ IPT_UI_ON_SCREEN_DISPLAY, "On Screen Display", SEQ_DEF_1(KEYCODE_TILDE) },
/*TODO*///	{ IPT_UI_PAUSE,             "Pause",             SEQ_DEF_1(KEYCODE_P) },
/*TODO*///	{ IPT_UI_RESET_MACHINE,     "Reset Game",        SEQ_DEF_1(KEYCODE_F3) },
/*TODO*///	{ IPT_UI_SHOW_GFX,          "Show Gfx",          SEQ_DEF_1(KEYCODE_F4) },
/*TODO*///	{ IPT_UI_FRAMESKIP_DEC,     "Frameskip Dec",     SEQ_DEF_1(KEYCODE_F8) },
/*TODO*///	{ IPT_UI_FRAMESKIP_INC,     "Frameskip Inc",     SEQ_DEF_1(KEYCODE_F9) },
/*TODO*///	{ IPT_UI_THROTTLE,          "Throttle",          SEQ_DEF_1(KEYCODE_F10) },
/*TODO*///	{ IPT_UI_SHOW_FPS,          "Show FPS",          SEQ_DEF_5(KEYCODE_F11, CODE_NOT, KEYCODE_LCONTROL, CODE_NOT, KEYCODE_LSHIFT) },
/*TODO*///	{ IPT_UI_SHOW_PROFILER,     "Show Profiler",     SEQ_DEF_2(KEYCODE_F11, KEYCODE_LSHIFT) },
/*TODO*///	{ IPT_UI_SHOW_COLORS,       "Show Colors",	 SEQ_DEF_2(KEYCODE_F11, KEYCODE_LCONTROL) },
/*TODO*///	{ IPT_UI_SNAPSHOT,          "Save Snapshot",     SEQ_DEF_1(KEYCODE_F12) },
/*TODO*///	{ IPT_UI_TOGGLE_CHEAT,      "Toggle Cheat",      SEQ_DEF_1(KEYCODE_F5) },
/*TODO*///	{ IPT_UI_UP,                "UI Up",             SEQ_DEF_3(KEYCODE_UP, CODE_OR, JOYCODE_1_UP) },
/*TODO*///	{ IPT_UI_DOWN,              "UI Down",           SEQ_DEF_3(KEYCODE_DOWN, CODE_OR, JOYCODE_1_DOWN) },
/*TODO*///	{ IPT_UI_LEFT,              "UI Left",           SEQ_DEF_3(KEYCODE_LEFT, CODE_OR, JOYCODE_1_LEFT) },
/*TODO*///	{ IPT_UI_RIGHT,             "UI Right",          SEQ_DEF_3(KEYCODE_RIGHT, CODE_OR, JOYCODE_1_RIGHT) },
/*TODO*///	{ IPT_UI_SELECT,            "UI Select",         SEQ_DEF_3(KEYCODE_ENTER, CODE_OR, JOYCODE_1_BUTTON1) },
/*TODO*///	{ IPT_UI_CANCEL,            "UI Cancel",         SEQ_DEF_1(KEYCODE_ESC) },
/*TODO*///	{ IPT_UI_PAN_UP,            "Pan Up",            SEQ_DEF_3(KEYCODE_PGUP, CODE_NOT, KEYCODE_LSHIFT) },
/*TODO*///	{ IPT_UI_PAN_DOWN,          "Pan Down",          SEQ_DEF_3(KEYCODE_PGDN, CODE_NOT, KEYCODE_LSHIFT) },
/*TODO*///	{ IPT_UI_PAN_LEFT,          "Pan Left",          SEQ_DEF_2(KEYCODE_PGUP, KEYCODE_LSHIFT) },
/*TODO*///	{ IPT_UI_PAN_RIGHT,         "Pan Right",         SEQ_DEF_2(KEYCODE_PGDN, KEYCODE_LSHIFT) },
/*TODO*///	{ IPT_START1, "1 Player Start",  SEQ_DEF_1(KEYCODE_1) },
/*TODO*///	{ IPT_START2, "2 Players Start", SEQ_DEF_1(KEYCODE_2) },
/*TODO*///	{ IPT_START3, "3 Players Start", SEQ_DEF_1(KEYCODE_3) },
/*TODO*///	{ IPT_START4, "4 Players Start", SEQ_DEF_1(KEYCODE_4) },
/*TODO*///	{ IPT_COIN1,  "Coin 1",          SEQ_DEF_1(KEYCODE_5) },
/*TODO*///	{ IPT_COIN2,  "Coin 2",          SEQ_DEF_1(KEYCODE_6) },
/*TODO*///	{ IPT_COIN3,  "Coin 3",          SEQ_DEF_1(KEYCODE_7) },
/*TODO*///	{ IPT_COIN4,  "Coin 4",          SEQ_DEF_1(KEYCODE_8) },
/*TODO*///	{ IPT_SERVICE1, "Service 1",     SEQ_DEF_1(KEYCODE_9) },
/*TODO*///	{ IPT_SERVICE2, "Service 2",     SEQ_DEF_1(KEYCODE_0) },
/*TODO*///	{ IPT_SERVICE3, "Service 3",     SEQ_DEF_1(KEYCODE_MINUS) },
/*TODO*///	{ IPT_SERVICE4, "Service 4",     SEQ_DEF_1(KEYCODE_EQUALS) },
/*TODO*///	{ IPT_TILT,   "Tilt",            SEQ_DEF_1(KEYCODE_T) },
/*TODO*///
/*TODO*///	{ IPT_JOYSTICK_UP         | IPF_PLAYER1, "P1 Up",          SEQ_DEF_3(KEYCODE_UP, CODE_OR, JOYCODE_1_UP)    },
/*TODO*///	{ IPT_JOYSTICK_DOWN       | IPF_PLAYER1, "P1 Down",        SEQ_DEF_3(KEYCODE_DOWN, CODE_OR, JOYCODE_1_DOWN)  },
/*TODO*///	{ IPT_JOYSTICK_LEFT       | IPF_PLAYER1, "P1 Left",        SEQ_DEF_3(KEYCODE_LEFT, CODE_OR, JOYCODE_1_LEFT)  },
/*TODO*///	{ IPT_JOYSTICK_RIGHT      | IPF_PLAYER1, "P1 Right",       SEQ_DEF_3(KEYCODE_RIGHT, CODE_OR, JOYCODE_1_RIGHT) },
/*TODO*///	{ IPT_BUTTON1             | IPF_PLAYER1, "P1 Button 1",    SEQ_DEF_3(KEYCODE_LCONTROL, CODE_OR, JOYCODE_1_BUTTON1) },
/*TODO*///	{ IPT_BUTTON2             | IPF_PLAYER1, "P1 Button 2",    SEQ_DEF_3(KEYCODE_LALT, CODE_OR, JOYCODE_1_BUTTON2) },
/*TODO*///	{ IPT_BUTTON3             | IPF_PLAYER1, "P1 Button 3",    SEQ_DEF_3(KEYCODE_SPACE, CODE_OR, JOYCODE_1_BUTTON3) },
/*TODO*///	{ IPT_BUTTON4             | IPF_PLAYER1, "P1 Button 4",    SEQ_DEF_3(KEYCODE_LSHIFT, CODE_OR, JOYCODE_1_BUTTON4) },
/*TODO*///	{ IPT_BUTTON5             | IPF_PLAYER1, "P1 Button 5",    SEQ_DEF_3(KEYCODE_Z, CODE_OR, JOYCODE_1_BUTTON5) },
/*TODO*///	{ IPT_BUTTON6             | IPF_PLAYER1, "P1 Button 6",    SEQ_DEF_3(KEYCODE_X, CODE_OR, JOYCODE_1_BUTTON6) },
/*TODO*///	{ IPT_BUTTON7             | IPF_PLAYER1, "P1 Button 7",    SEQ_DEF_1(KEYCODE_C) },
/*TODO*///	{ IPT_BUTTON8             | IPF_PLAYER1, "P1 Button 8",    SEQ_DEF_1(KEYCODE_V) },
/*TODO*///	{ IPT_BUTTON9             | IPF_PLAYER1, "P1 Button 9",    SEQ_DEF_1(KEYCODE_B) },
/*TODO*///	{ IPT_JOYSTICKRIGHT_UP    | IPF_PLAYER1, "P1 Right/Up",    SEQ_DEF_3(KEYCODE_I, CODE_OR, JOYCODE_1_BUTTON2) },
/*TODO*///	{ IPT_JOYSTICKRIGHT_DOWN  | IPF_PLAYER1, "P1 Right/Down",  SEQ_DEF_3(KEYCODE_K, CODE_OR, JOYCODE_1_BUTTON3) },
/*TODO*///	{ IPT_JOYSTICKRIGHT_LEFT  | IPF_PLAYER1, "P1 Right/Left",  SEQ_DEF_3(KEYCODE_J, CODE_OR, JOYCODE_1_BUTTON1) },
/*TODO*///	{ IPT_JOYSTICKRIGHT_RIGHT | IPF_PLAYER1, "P1 Right/Right", SEQ_DEF_3(KEYCODE_L, CODE_OR, JOYCODE_1_BUTTON4) },
/*TODO*///	{ IPT_JOYSTICKLEFT_UP     | IPF_PLAYER1, "P1 Left/Up",     SEQ_DEF_3(KEYCODE_E, CODE_OR, JOYCODE_1_UP) },
/*TODO*///	{ IPT_JOYSTICKLEFT_DOWN   | IPF_PLAYER1, "P1 Left/Down",   SEQ_DEF_3(KEYCODE_D, CODE_OR, JOYCODE_1_DOWN) },
/*TODO*///	{ IPT_JOYSTICKLEFT_LEFT   | IPF_PLAYER1, "P1 Left/Left",   SEQ_DEF_3(KEYCODE_S, CODE_OR, JOYCODE_1_LEFT) },
/*TODO*///	{ IPT_JOYSTICKLEFT_RIGHT  | IPF_PLAYER1, "P1 Left/Right",  SEQ_DEF_3(KEYCODE_F, CODE_OR, JOYCODE_1_RIGHT) },
/*TODO*///
/*TODO*///	{ IPT_JOYSTICK_UP         | IPF_PLAYER2, "P2 Up",          SEQ_DEF_3(KEYCODE_R, CODE_OR, JOYCODE_2_UP)    },
/*TODO*///	{ IPT_JOYSTICK_DOWN       | IPF_PLAYER2, "P2 Down",        SEQ_DEF_3(KEYCODE_F, CODE_OR, JOYCODE_2_DOWN)  },
/*TODO*///	{ IPT_JOYSTICK_LEFT       | IPF_PLAYER2, "P2 Left",        SEQ_DEF_3(KEYCODE_D, CODE_OR, JOYCODE_2_LEFT)  },
/*TODO*///	{ IPT_JOYSTICK_RIGHT      | IPF_PLAYER2, "P2 Right",       SEQ_DEF_3(KEYCODE_G, CODE_OR, JOYCODE_2_RIGHT) },
/*TODO*///	{ IPT_BUTTON1             | IPF_PLAYER2, "P2 Button 1",    SEQ_DEF_3(KEYCODE_A, CODE_OR, JOYCODE_2_BUTTON1) },
/*TODO*///	{ IPT_BUTTON2             | IPF_PLAYER2, "P2 Button 2",    SEQ_DEF_3(KEYCODE_S, CODE_OR, JOYCODE_2_BUTTON2) },
/*TODO*///	{ IPT_BUTTON3             | IPF_PLAYER2, "P2 Button 3",    SEQ_DEF_3(KEYCODE_Q, CODE_OR, JOYCODE_2_BUTTON3) },
/*TODO*///	{ IPT_BUTTON4             | IPF_PLAYER2, "P2 Button 4",    SEQ_DEF_3(KEYCODE_W, CODE_OR, JOYCODE_2_BUTTON4) },
/*TODO*///	{ IPT_BUTTON5             | IPF_PLAYER2, "P2 Button 5",    SEQ_DEF_1(JOYCODE_2_BUTTON5) },
/*TODO*///	{ IPT_BUTTON6             | IPF_PLAYER2, "P2 Button 6",    SEQ_DEF_1(JOYCODE_2_BUTTON6) },
/*TODO*///	{ IPT_BUTTON7             | IPF_PLAYER2, "P2 Button 7",    SEQ_DEF_0 },
/*TODO*///	{ IPT_BUTTON8             | IPF_PLAYER2, "P2 Button 8",    SEQ_DEF_0 },
/*TODO*///	{ IPT_BUTTON9             | IPF_PLAYER2, "P2 Button 9",    SEQ_DEF_0 },
/*TODO*///	{ IPT_JOYSTICKRIGHT_UP    | IPF_PLAYER2, "P2 Right/Up",    SEQ_DEF_0 },
/*TODO*///	{ IPT_JOYSTICKRIGHT_DOWN  | IPF_PLAYER2, "P2 Right/Down",  SEQ_DEF_0 },
/*TODO*///	{ IPT_JOYSTICKRIGHT_LEFT  | IPF_PLAYER2, "P2 Right/Left",  SEQ_DEF_0 },
/*TODO*///	{ IPT_JOYSTICKRIGHT_RIGHT | IPF_PLAYER2, "P2 Right/Right", SEQ_DEF_0 },
/*TODO*///	{ IPT_JOYSTICKLEFT_UP     | IPF_PLAYER2, "P2 Left/Up",     SEQ_DEF_0 },
/*TODO*///	{ IPT_JOYSTICKLEFT_DOWN   | IPF_PLAYER2, "P2 Left/Down",   SEQ_DEF_0 },
/*TODO*///	{ IPT_JOYSTICKLEFT_LEFT   | IPF_PLAYER2, "P2 Left/Left",   SEQ_DEF_0 },
/*TODO*///	{ IPT_JOYSTICKLEFT_RIGHT  | IPF_PLAYER2, "P2 Left/Right",  SEQ_DEF_0 },
/*TODO*///
/*TODO*///	{ IPT_JOYSTICK_UP         | IPF_PLAYER3, "P3 Up",          SEQ_DEF_3(KEYCODE_I, CODE_OR, JOYCODE_3_UP)    },
/*TODO*///	{ IPT_JOYSTICK_DOWN       | IPF_PLAYER3, "P3 Down",        SEQ_DEF_3(KEYCODE_K, CODE_OR, JOYCODE_3_DOWN)  },
/*TODO*///	{ IPT_JOYSTICK_LEFT       | IPF_PLAYER3, "P3 Left",        SEQ_DEF_3(KEYCODE_J, CODE_OR, JOYCODE_3_LEFT)  },
/*TODO*///	{ IPT_JOYSTICK_RIGHT      | IPF_PLAYER3, "P3 Right",       SEQ_DEF_3(KEYCODE_L, CODE_OR, JOYCODE_3_RIGHT) },
/*TODO*///	{ IPT_BUTTON1             | IPF_PLAYER3, "P3 Button 1",    SEQ_DEF_3(KEYCODE_RCONTROL, CODE_OR, JOYCODE_3_BUTTON1) },
/*TODO*///	{ IPT_BUTTON2             | IPF_PLAYER3, "P3 Button 2",    SEQ_DEF_3(KEYCODE_RSHIFT, CODE_OR, JOYCODE_3_BUTTON2) },
/*TODO*///	{ IPT_BUTTON3             | IPF_PLAYER3, "P3 Button 3",    SEQ_DEF_3(KEYCODE_ENTER, CODE_OR, JOYCODE_3_BUTTON3) },
/*TODO*///	{ IPT_BUTTON4             | IPF_PLAYER3, "P3 Button 4",    SEQ_DEF_1(JOYCODE_3_BUTTON4) },
/*TODO*///
/*TODO*///	{ IPT_JOYSTICK_UP         | IPF_PLAYER4, "P4 Up",          SEQ_DEF_1(JOYCODE_4_UP) },
/*TODO*///	{ IPT_JOYSTICK_DOWN       | IPF_PLAYER4, "P4 Down",        SEQ_DEF_1(JOYCODE_4_DOWN) },
/*TODO*///	{ IPT_JOYSTICK_LEFT       | IPF_PLAYER4, "P4 Left",        SEQ_DEF_1(JOYCODE_4_LEFT) },
/*TODO*///	{ IPT_JOYSTICK_RIGHT      | IPF_PLAYER4, "P4 Right",       SEQ_DEF_1(JOYCODE_4_RIGHT) },
/*TODO*///	{ IPT_BUTTON1             | IPF_PLAYER4, "P4 Button 1",    SEQ_DEF_1(JOYCODE_4_BUTTON1) },
/*TODO*///	{ IPT_BUTTON2             | IPF_PLAYER4, "P4 Button 2",    SEQ_DEF_1(JOYCODE_4_BUTTON2) },
/*TODO*///	{ IPT_BUTTON3             | IPF_PLAYER4, "P4 Button 3",    SEQ_DEF_1(JOYCODE_4_BUTTON3) },
/*TODO*///	{ IPT_BUTTON4             | IPF_PLAYER4, "P4 Button 4",    SEQ_DEF_1(JOYCODE_4_BUTTON4) },
/*TODO*///
/*TODO*///	{ IPT_PEDAL	                | IPF_PLAYER1, "Pedal 1",        SEQ_DEF_3(KEYCODE_LCONTROL, CODE_OR, JOYCODE_1_BUTTON1) },
/*TODO*///	{ (IPT_PEDAL+IPT_EXTENSION) | IPF_PLAYER1, "P1 Auto Release <Y/N>", SEQ_DEF_1(KEYCODE_Y) },
/*TODO*///	{ IPT_PEDAL                 | IPF_PLAYER2, "Pedal 2",        SEQ_DEF_3(KEYCODE_A, CODE_OR, JOYCODE_2_BUTTON1) },
/*TODO*///	{ (IPT_PEDAL+IPT_EXTENSION) | IPF_PLAYER2, "P2 Auto Release <Y/N>", SEQ_DEF_1(KEYCODE_Y) },
/*TODO*///	{ IPT_PEDAL                 | IPF_PLAYER3, "Pedal 3",        SEQ_DEF_3(KEYCODE_RCONTROL, CODE_OR, JOYCODE_3_BUTTON1) },
/*TODO*///	{ (IPT_PEDAL+IPT_EXTENSION) | IPF_PLAYER3, "P3 Auto Release <Y/N>", SEQ_DEF_1(KEYCODE_Y) },
/*TODO*///	{ IPT_PEDAL                 | IPF_PLAYER4, "Pedal 4",        SEQ_DEF_1(JOYCODE_4_BUTTON1) },
/*TODO*///	{ (IPT_PEDAL+IPT_EXTENSION) | IPF_PLAYER4, "P4 Auto Release <Y/N>", SEQ_DEF_1(KEYCODE_Y) },
/*TODO*///
/*TODO*///	{ IPT_PADDLE | IPF_PLAYER1,  "Paddle",        SEQ_DEF_3(KEYCODE_LEFT, CODE_OR, JOYCODE_1_LEFT) },
/*TODO*///	{ (IPT_PADDLE | IPF_PLAYER1)+IPT_EXTENSION,             "Paddle",        SEQ_DEF_3(KEYCODE_RIGHT, CODE_OR, JOYCODE_1_RIGHT)  },
/*TODO*///	{ IPT_PADDLE | IPF_PLAYER2,  "Paddle 2",      SEQ_DEF_3(KEYCODE_D, CODE_OR, JOYCODE_2_LEFT) },
/*TODO*///	{ (IPT_PADDLE | IPF_PLAYER2)+IPT_EXTENSION,             "Paddle 2",      SEQ_DEF_3(KEYCODE_G, CODE_OR, JOYCODE_2_RIGHT) },
/*TODO*///	{ IPT_PADDLE | IPF_PLAYER3,  "Paddle 3",      SEQ_DEF_3(KEYCODE_J, CODE_OR, JOYCODE_3_LEFT) },
/*TODO*///	{ (IPT_PADDLE | IPF_PLAYER3)+IPT_EXTENSION,             "Paddle 3",      SEQ_DEF_3(KEYCODE_L, CODE_OR, JOYCODE_3_RIGHT) },
/*TODO*///	{ IPT_PADDLE | IPF_PLAYER4,  "Paddle 4",      SEQ_DEF_1(JOYCODE_4_LEFT) },
/*TODO*///	{ (IPT_PADDLE | IPF_PLAYER4)+IPT_EXTENSION,             "Paddle 4",      SEQ_DEF_1(JOYCODE_4_RIGHT) },
/*TODO*///	{ IPT_PADDLE_V | IPF_PLAYER1,  "Paddle V",          SEQ_DEF_3(KEYCODE_UP, CODE_OR, JOYCODE_1_UP) },
/*TODO*///	{ (IPT_PADDLE_V | IPF_PLAYER1)+IPT_EXTENSION,             "Paddle V",          SEQ_DEF_3(KEYCODE_DOWN, CODE_OR, JOYCODE_1_DOWN) },
/*TODO*///	{ IPT_PADDLE_V | IPF_PLAYER2,  "Paddle V 2",        SEQ_DEF_3(KEYCODE_R, CODE_OR, JOYCODE_2_UP) },
/*TODO*///	{ (IPT_PADDLE_V | IPF_PLAYER2)+IPT_EXTENSION,             "Paddle V 2",      SEQ_DEF_3(KEYCODE_F, CODE_OR, JOYCODE_2_DOWN) },
/*TODO*///	{ IPT_PADDLE_V | IPF_PLAYER3,  "Paddle V 3",        SEQ_DEF_3(KEYCODE_I, CODE_OR, JOYCODE_3_UP) },
/*TODO*///	{ (IPT_PADDLE_V | IPF_PLAYER3)+IPT_EXTENSION,             "Paddle V 3",      SEQ_DEF_3(KEYCODE_K, CODE_OR, JOYCODE_3_DOWN) },
/*TODO*///	{ IPT_PADDLE_V | IPF_PLAYER4,  "Paddle V 4",        SEQ_DEF_1(JOYCODE_4_UP) },
/*TODO*///	{ (IPT_PADDLE_V | IPF_PLAYER4)+IPT_EXTENSION,             "Paddle V 4",      SEQ_DEF_1(JOYCODE_4_DOWN) },
/*TODO*///	{ IPT_DIAL | IPF_PLAYER1,    "Dial",          SEQ_DEF_3(KEYCODE_LEFT, CODE_OR, JOYCODE_1_LEFT) },
/*TODO*///	{ (IPT_DIAL | IPF_PLAYER1)+IPT_EXTENSION,               "Dial",          SEQ_DEF_3(KEYCODE_RIGHT, CODE_OR, JOYCODE_1_RIGHT) },
/*TODO*///	{ IPT_DIAL | IPF_PLAYER2,    "Dial 2",        SEQ_DEF_3(KEYCODE_D, CODE_OR, JOYCODE_2_LEFT) },
/*TODO*///	{ (IPT_DIAL | IPF_PLAYER2)+IPT_EXTENSION,               "Dial 2",      SEQ_DEF_3(KEYCODE_G, CODE_OR, JOYCODE_2_RIGHT) },
/*TODO*///	{ IPT_DIAL | IPF_PLAYER3,    "Dial 3",        SEQ_DEF_3(KEYCODE_J, CODE_OR, JOYCODE_3_LEFT) },
/*TODO*///	{ (IPT_DIAL | IPF_PLAYER3)+IPT_EXTENSION,               "Dial 3",      SEQ_DEF_3(KEYCODE_L, CODE_OR, JOYCODE_3_RIGHT) },
/*TODO*///	{ IPT_DIAL | IPF_PLAYER4,    "Dial 4",        SEQ_DEF_1(JOYCODE_4_LEFT) },
/*TODO*///	{ (IPT_DIAL | IPF_PLAYER4)+IPT_EXTENSION,               "Dial 4",      SEQ_DEF_1(JOYCODE_4_RIGHT) },
/*TODO*///	{ IPT_DIAL_V | IPF_PLAYER1,  "Dial V",          SEQ_DEF_3(KEYCODE_UP, CODE_OR, JOYCODE_1_UP) },
/*TODO*///	{ (IPT_DIAL_V | IPF_PLAYER1)+IPT_EXTENSION,             "Dial V",          SEQ_DEF_3(KEYCODE_DOWN, CODE_OR, JOYCODE_1_DOWN) },
/*TODO*///	{ IPT_DIAL_V | IPF_PLAYER2,  "Dial V 2",        SEQ_DEF_3(KEYCODE_R, CODE_OR, JOYCODE_2_UP) },
/*TODO*///	{ (IPT_DIAL_V | IPF_PLAYER2)+IPT_EXTENSION,             "Dial V 2",      SEQ_DEF_3(KEYCODE_F, CODE_OR, JOYCODE_2_DOWN) },
/*TODO*///	{ IPT_DIAL_V | IPF_PLAYER3,  "Dial V 3",        SEQ_DEF_3(KEYCODE_I, CODE_OR, JOYCODE_3_UP) },
/*TODO*///	{ (IPT_DIAL_V | IPF_PLAYER3)+IPT_EXTENSION,             "Dial V 3",      SEQ_DEF_3(KEYCODE_K, CODE_OR, JOYCODE_3_DOWN) },
/*TODO*///	{ IPT_DIAL_V | IPF_PLAYER4,  "Dial V 4",        SEQ_DEF_1(JOYCODE_4_UP) },
/*TODO*///	{ (IPT_DIAL_V | IPF_PLAYER4)+IPT_EXTENSION,             "Dial V 4",      SEQ_DEF_1(JOYCODE_4_DOWN) },
/*TODO*///
/*TODO*///	{ IPT_TRACKBALL_X | IPF_PLAYER1, "Track X",   SEQ_DEF_3(KEYCODE_LEFT, CODE_OR, JOYCODE_1_LEFT) },
/*TODO*///	{ (IPT_TRACKBALL_X | IPF_PLAYER1)+IPT_EXTENSION,                 "Track X",   SEQ_DEF_3(KEYCODE_RIGHT, CODE_OR, JOYCODE_1_RIGHT) },
/*TODO*///	{ IPT_TRACKBALL_X | IPF_PLAYER2, "Track X 2", SEQ_DEF_3(KEYCODE_D, CODE_OR, JOYCODE_2_LEFT) },
/*TODO*///	{ (IPT_TRACKBALL_X | IPF_PLAYER2)+IPT_EXTENSION,                 "Track X 2", SEQ_DEF_3(KEYCODE_G, CODE_OR, JOYCODE_2_RIGHT) },
/*TODO*///	{ IPT_TRACKBALL_X | IPF_PLAYER3, "Track X 3", SEQ_DEF_3(KEYCODE_J, CODE_OR, JOYCODE_3_LEFT) },
/*TODO*///	{ (IPT_TRACKBALL_X | IPF_PLAYER3)+IPT_EXTENSION,                 "Track X 3", SEQ_DEF_3(KEYCODE_L, CODE_OR, JOYCODE_3_RIGHT) },
/*TODO*///	{ IPT_TRACKBALL_X | IPF_PLAYER4, "Track X 4", SEQ_DEF_1(JOYCODE_4_LEFT) },
/*TODO*///	{ (IPT_TRACKBALL_X | IPF_PLAYER4)+IPT_EXTENSION,                 "Track X 4", SEQ_DEF_1(JOYCODE_4_RIGHT) },
/*TODO*///
/*TODO*///	{ IPT_TRACKBALL_Y | IPF_PLAYER1, "Track Y",   SEQ_DEF_3(KEYCODE_UP, CODE_OR, JOYCODE_1_UP) },
/*TODO*///	{ (IPT_TRACKBALL_Y | IPF_PLAYER1)+IPT_EXTENSION,                 "Track Y",   SEQ_DEF_3(KEYCODE_DOWN, CODE_OR, JOYCODE_1_DOWN) },
/*TODO*///	{ IPT_TRACKBALL_Y | IPF_PLAYER2, "Track Y 2", SEQ_DEF_3(KEYCODE_R, CODE_OR, JOYCODE_2_UP) },
/*TODO*///	{ (IPT_TRACKBALL_Y | IPF_PLAYER2)+IPT_EXTENSION,                 "Track Y 2", SEQ_DEF_3(KEYCODE_F, CODE_OR, JOYCODE_2_DOWN) },
/*TODO*///	{ IPT_TRACKBALL_Y | IPF_PLAYER3, "Track Y 3", SEQ_DEF_3(KEYCODE_I, CODE_OR, JOYCODE_3_UP) },
/*TODO*///	{ (IPT_TRACKBALL_Y | IPF_PLAYER3)+IPT_EXTENSION,                 "Track Y 3", SEQ_DEF_3(KEYCODE_K, CODE_OR, JOYCODE_3_DOWN) },
/*TODO*///	{ IPT_TRACKBALL_Y | IPF_PLAYER4, "Track Y 4", SEQ_DEF_1(JOYCODE_4_UP) },
/*TODO*///	{ (IPT_TRACKBALL_Y | IPF_PLAYER4)+IPT_EXTENSION,                 "Track Y 4", SEQ_DEF_1(JOYCODE_4_DOWN) },
/*TODO*///
/*TODO*///	{ IPT_AD_STICK_X | IPF_PLAYER1, "AD Stick X",   SEQ_DEF_3(KEYCODE_LEFT, CODE_OR, JOYCODE_1_LEFT) },
/*TODO*///	{ (IPT_AD_STICK_X | IPF_PLAYER1)+IPT_EXTENSION,                "AD Stick X",   SEQ_DEF_3(KEYCODE_RIGHT, CODE_OR, JOYCODE_1_RIGHT) },
/*TODO*///	{ IPT_AD_STICK_X | IPF_PLAYER2, "AD Stick X 2", SEQ_DEF_3(KEYCODE_D, CODE_OR, JOYCODE_2_LEFT) },
/*TODO*///	{ (IPT_AD_STICK_X | IPF_PLAYER2)+IPT_EXTENSION,                "AD Stick X 2", SEQ_DEF_3(KEYCODE_G, CODE_OR, JOYCODE_2_RIGHT) },
/*TODO*///	{ IPT_AD_STICK_X | IPF_PLAYER3, "AD Stick X 3", SEQ_DEF_3(KEYCODE_J, CODE_OR, JOYCODE_3_LEFT) },
/*TODO*///	{ (IPT_AD_STICK_X | IPF_PLAYER3)+IPT_EXTENSION,                "AD Stick X 3", SEQ_DEF_3(KEYCODE_L, CODE_OR, JOYCODE_3_RIGHT) },
/*TODO*///	{ IPT_AD_STICK_X | IPF_PLAYER4, "AD Stick X 4", SEQ_DEF_1(JOYCODE_4_LEFT) },
/*TODO*///	{ (IPT_AD_STICK_X | IPF_PLAYER4)+IPT_EXTENSION,                "AD Stick X 4", SEQ_DEF_1(JOYCODE_4_RIGHT) },
/*TODO*///
/*TODO*///	{ IPT_AD_STICK_Y | IPF_PLAYER1, "AD Stick Y",   SEQ_DEF_3(KEYCODE_UP, CODE_OR, JOYCODE_1_UP) },
/*TODO*///	{ (IPT_AD_STICK_Y | IPF_PLAYER1)+IPT_EXTENSION,                "AD Stick Y",   SEQ_DEF_3(KEYCODE_DOWN, CODE_OR, JOYCODE_1_DOWN) },
/*TODO*///	{ IPT_AD_STICK_Y | IPF_PLAYER2, "AD Stick Y 2", SEQ_DEF_3(KEYCODE_R, CODE_OR, JOYCODE_2_UP) },
/*TODO*///	{ (IPT_AD_STICK_Y | IPF_PLAYER2)+IPT_EXTENSION,                "AD Stick Y 2", SEQ_DEF_3(KEYCODE_F, CODE_OR, JOYCODE_2_DOWN) },
/*TODO*///	{ IPT_AD_STICK_Y | IPF_PLAYER3, "AD Stick Y 3", SEQ_DEF_3(KEYCODE_I, CODE_OR, JOYCODE_3_UP) },
/*TODO*///	{ (IPT_AD_STICK_Y | IPF_PLAYER3)+IPT_EXTENSION,                "AD Stick Y 3", SEQ_DEF_3(KEYCODE_K, CODE_OR, JOYCODE_3_DOWN) },
/*TODO*///	{ IPT_AD_STICK_Y | IPF_PLAYER4, "AD Stick Y 4", SEQ_DEF_1(JOYCODE_4_UP) },
/*TODO*///	{ (IPT_AD_STICK_Y | IPF_PLAYER4)+IPT_EXTENSION,                "AD Stick Y 4", SEQ_DEF_1(JOYCODE_4_DOWN) },
/*TODO*///
/*TODO*///	{ IPT_UNKNOWN,             "UNKNOWN",         SEQ_DEF_0 },
/*TODO*///	{ IPT_END,                 0,                 SEQ_DEF_0 }	/* returned when there is no match */
/*TODO*///};
/*TODO*///
/*TODO*///struct ipd inputport_defaults_backup[sizeof(inputport_defaults)/sizeof(struct ipd)];
/*TODO*///
/*TODO*////***************************************************************************/
/*TODO*////* Generic IO */
/*TODO*///
/*TODO*///static int readint(void *f,UINT32 *num)
/*TODO*///{
/*TODO*///	unsigned i;
/*TODO*///
/*TODO*///	*num = 0;
/*TODO*///	for (i = 0;i < sizeof(UINT32);i++)
/*TODO*///	{
/*TODO*///		unsigned char c;
/*TODO*///
/*TODO*///
/*TODO*///		*num <<= 8;
/*TODO*///		if (osd_fread(f,&c,1) != 1)
/*TODO*///			return -1;
/*TODO*///		*num |= c;
/*TODO*///	}
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///static void writeint(void *f,UINT32 num)
/*TODO*///{
/*TODO*///	unsigned i;
/*TODO*///
/*TODO*///	for (i = 0;i < sizeof(UINT32);i++)
/*TODO*///	{
/*TODO*///		unsigned char c;
/*TODO*///
/*TODO*///
/*TODO*///		c = (num >> 8 * (sizeof(UINT32)-1)) & 0xff;
/*TODO*///		osd_fwrite(f,&c,1);
/*TODO*///		num <<= 8;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static int readword(void *f,UINT16 *num)
/*TODO*///{
/*TODO*///	unsigned i;
/*TODO*///	int res;
/*TODO*///
/*TODO*///	res = 0;
/*TODO*///	for (i = 0;i < sizeof(UINT16);i++)
/*TODO*///	{
/*TODO*///		unsigned char c;
/*TODO*///
/*TODO*///
/*TODO*///		res <<= 8;
/*TODO*///		if (osd_fread(f,&c,1) != 1)
/*TODO*///			return -1;
/*TODO*///		res |= c;
/*TODO*///	}
/*TODO*///
/*TODO*///	*num = res;
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///static void writeword(void *f,UINT16 num)
/*TODO*///{
/*TODO*///	unsigned i;
/*TODO*///
/*TODO*///	for (i = 0;i < sizeof(UINT16);i++)
/*TODO*///	{
/*TODO*///		unsigned char c;
/*TODO*///
/*TODO*///
/*TODO*///		c = (num >> 8 * (sizeof(UINT16)-1)) & 0xff;
/*TODO*///		osd_fwrite(f,&c,1);
/*TODO*///		num <<= 8;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static int seq_read_ver_8(void* f, InputSeq* seq)
/*TODO*///{
/*TODO*///	int j,len;
/*TODO*///	UINT32 i;
/*TODO*///	UINT16 w;
/*TODO*///
/*TODO*///	if (readword(f,&w) != 0)
/*TODO*///		return -1;
/*TODO*///
/*TODO*///	len = w;
/*TODO*///	seq_set_0(seq);
/*TODO*///	for(j=0;j<len;++j)
/*TODO*///	{
/*TODO*///		if (readint(f,&i) != 0)
/*TODO*/// 			return -1;
/*TODO*///		(*seq)[j] = savecode_to_code(i);
/*TODO*/// 	}
/*TODO*///
/*TODO*/// 	return 0;
/*TODO*///  }
/*TODO*///
/*TODO*///static int seq_read(void* f, InputSeq* seq, int ver)
/*TODO*///  {
/*TODO*///		return seq_read_ver_8(f,seq);
/*TODO*///  }
/*TODO*///
/*TODO*///static void seq_write(void* f, InputSeq* seq)
/*TODO*///  {
/*TODO*///	int j,len;
/*TODO*///        for(len=0;len<SEQ_MAX;++len)
/*TODO*///		if ((*seq)[len] == CODE_NONE)
/*TODO*///			break;
/*TODO*///	writeword(f,len);
/*TODO*///	for(j=0;j<len;++j)
/*TODO*///		writeint(f, code_to_savecode( (*seq)[j] ));
/*TODO*///  }
/*TODO*///
/*TODO*////***************************************************************************/
/*TODO*////* Load */
/*TODO*///
/*TODO*///static void load_default_keys(void)
/*TODO*///{
/*TODO*///	void *f;
/*TODO*///
/*TODO*///
/*TODO*///	osd_customize_inputport_defaults(inputport_defaults);
/*TODO*///	memcpy(inputport_defaults_backup,inputport_defaults,sizeof(inputport_defaults));
/*TODO*///
/*TODO*///	if ((f = osd_fopen("default",0,OSD_FILETYPE_CONFIG,0)) != 0)
/*TODO*///	{
/*TODO*///		char buf[8];
/*TODO*///		int version;
/*TODO*///
/*TODO*///		/* read header */
/*TODO*///		if (osd_fread(f,buf,8) != 8)
/*TODO*///			goto getout;
/*TODO*///
/*TODO*///		if (memcmp(buf,MAMEDEFSTRING_V5,8) == 0)
/*TODO*///			version = 5;
/*TODO*///		else if (memcmp(buf,MAMEDEFSTRING_V6,8) == 0)
/*TODO*///			version = 6;
/*TODO*///		else if (memcmp(buf,MAMEDEFSTRING_V7,8) == 0)
/*TODO*///			version = 7;
/*TODO*///		else if (memcmp(buf,MAMEDEFSTRING_V8,8) == 0)
/*TODO*///			version = 8;
/*TODO*///		else
/*TODO*///			goto getout;	/* header invalid */
/*TODO*///
/*TODO*///		for (;;)
/*TODO*///		{
/*TODO*///			UINT32 type;
/*TODO*///			InputSeq def_seq;
/*TODO*///			InputSeq seq;
/*TODO*///			int i;
/*TODO*///
/*TODO*///			if (readint(f,&type) != 0)
/*TODO*///				goto getout;
/*TODO*///
/*TODO*///			if (seq_read(f,&def_seq,version)!=0)
/*TODO*///				goto getout;
/*TODO*///			if (seq_read(f,&seq,version)!=0)
/*TODO*///				goto getout;
/*TODO*///
/*TODO*///			i = 0;
/*TODO*///			while (inputport_defaults[i].type != IPT_END)
/*TODO*///			{
/*TODO*///				if (inputport_defaults[i].type == type)
/*TODO*///				{
/*TODO*///					/* load stored settings only if the default hasn't changed */
/*TODO*///					if (seq_cmp(&inputport_defaults[i].seq,&def_seq)==0)
/*TODO*///						seq_copy(&inputport_defaults[i].seq,&seq);
/*TODO*///				}
/*TODO*///
/*TODO*///				i++;
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///getout:
/*TODO*///		osd_fclose(f);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void save_default_keys(void)
/*TODO*///{
/*TODO*///	void *f;
/*TODO*///
/*TODO*///
/*TODO*///	if ((f = osd_fopen("default",0,OSD_FILETYPE_CONFIG,1)) != 0)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///
/*TODO*///
/*TODO*///		/* write header */
/*TODO*///		osd_fwrite(f,MAMEDEFSTRING_V8,8);
/*TODO*///
/*TODO*///		i = 0;
/*TODO*///		while (inputport_defaults[i].type != IPT_END)
/*TODO*///		{
/*TODO*///			writeint(f,inputport_defaults[i].type);
/*TODO*///
/*TODO*///			seq_write(f,&inputport_defaults_backup[i].seq);
/*TODO*///			seq_write(f,&inputport_defaults[i].seq);
/*TODO*///
/*TODO*///			i++;
/*TODO*///		}
/*TODO*///
/*TODO*///		osd_fclose(f);
/*TODO*///	}
/*TODO*///	memcpy(inputport_defaults,inputport_defaults_backup,sizeof(inputport_defaults_backup));
/*TODO*///}
/*TODO*///
/*TODO*///static int input_port_read_ver_8(void *f,struct InputPort *in)
/*TODO*///{
/*TODO*///	UINT32 i;
/*TODO*///	UINT16 w;
/*TODO*///	if (readint(f,&i) != 0)
/*TODO*///		return -1;
/*TODO*///	in->type = i;
/*TODO*///
/*TODO*///	if (readword(f,&w) != 0)
/*TODO*///		return -1;
/*TODO*///	in->mask = w;
/*TODO*///
/*TODO*///	if (readword(f,&w) != 0)
/*TODO*///		return -1;
/*TODO*///	in->default_value = w;
/*TODO*///
/*TODO*///	if (seq_read_ver_8(f,&in->seq) != 0)
/*TODO*///		return -1;
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///static int input_port_read(void *f,struct InputPort *in, int ver)
/*TODO*///{
/*TODO*///		return input_port_read_ver_8(f,in);
/*TODO*///}
/*TODO*///
/*TODO*///static void input_port_write(void *f,struct InputPort *in)
/*TODO*///{
/*TODO*///	writeint(f,in->type);
/*TODO*///	writeword(f,in->mask);
/*TODO*///	writeword(f,in->default_value);
/*TODO*///	seq_write(f,&in->seq);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///int load_input_port_settings(void)
/*TODO*///{
/*TODO*///	void *f;
/*TODO*///
/*TODO*///	load_default_keys();
/*TODO*///
/*TODO*///	if ((f = osd_fopen(Machine->gamedrv->name,0,OSD_FILETYPE_CONFIG,0)) != 0)
/*TODO*///	{
/*TODO*///		struct InputPort *in;
/*TODO*///
/*TODO*///		unsigned int total,savedtotal;
/*TODO*///		char buf[8];
/*TODO*///		int i;
/*TODO*///		int version;
/*TODO*///
/*TODO*///		in = Machine->input_ports_default;
/*TODO*///
/*TODO*///		/* calculate the size of the array */
/*TODO*///		total = 0;
/*TODO*///		while (in->type != IPT_END)
/*TODO*///		{
/*TODO*///			total++;
/*TODO*///			in++;
/*TODO*///		}
/*TODO*///
/*TODO*///		/* read header */
/*TODO*///		if (osd_fread(f,buf,8) != 8)
/*TODO*///			goto getout;
/*TODO*///
/*TODO*///		if (memcmp(buf,MAMECFGSTRING_V5,8) == 0)
/*TODO*///			version = 5;
/*TODO*///		else if (memcmp(buf,MAMECFGSTRING_V6,8) == 0)
/*TODO*///			version = 6;
/*TODO*///		else if (memcmp(buf,MAMECFGSTRING_V7,8) == 0)
/*TODO*///			version = 7;
/*TODO*///		else if (memcmp(buf,MAMECFGSTRING_V8,8) == 0)
/*TODO*///			version = 8;
/*TODO*///		else
/*TODO*///			goto getout;	/* header invalid */
/*TODO*///
/*TODO*///		/* read array size */
/*TODO*///		if (readint(f,&savedtotal) != 0)
/*TODO*///			goto getout;
/*TODO*///		if (total != savedtotal)
/*TODO*///			goto getout;	/* different size */
/*TODO*///
/*TODO*///		/* read the original settings and compare them with the ones defined in the driver */
/*TODO*///		in = Machine->input_ports_default;
/*TODO*///		while (in->type != IPT_END)
/*TODO*///		{
/*TODO*///			struct InputPort saved;
/*TODO*///
/*TODO*///			if (input_port_read(f,&saved,version) != 0)
/*TODO*///				goto getout;
/*TODO*///
/*TODO*///			if (in->mask != saved.mask ||
/*TODO*///				in->default_value != saved.default_value ||
/*TODO*///				in->type != saved.type ||
/*TODO*///				seq_cmp(&in->seq,&saved.seq) !=0 )
/*TODO*///			goto getout;	/* the default values are different */
/*TODO*///
/*TODO*///			in++;
/*TODO*///		}
/*TODO*///
/*TODO*///		/* read the current settings */
/*TODO*///		in = Machine->input_ports;
/*TODO*///		while (in->type != IPT_END)
/*TODO*///		{
/*TODO*///			if (input_port_read(f,in,version) != 0)
/*TODO*///				goto getout;
/*TODO*///			in++;
/*TODO*///		}
/*TODO*///
/*TODO*///		/* Clear the coin & ticket counters/flags - LBO 042898 */
/*TODO*///		for (i = 0; i < COIN_COUNTERS; i ++)
/*TODO*///			coins[i] = lastcoin[i] = coinlockedout[i] = 0;
/*TODO*///		dispensed_tickets = 0;
/*TODO*///
/*TODO*///		/* read in the coin/ticket counters */
/*TODO*///		for (i = 0; i < COIN_COUNTERS; i ++)
/*TODO*///		{
/*TODO*///			if (readint(f,&coins[i]) != 0)
/*TODO*///				goto getout;
/*TODO*///		}
/*TODO*///		if (readint(f,&dispensed_tickets) != 0)
/*TODO*///			goto getout;
/*TODO*///
/*TODO*///		mixer_read_config(f);
/*TODO*///
/*TODO*///getout:
/*TODO*///		osd_fclose(f);
/*TODO*///	}
/*TODO*///
/*TODO*///	/* All analog ports need initialization */
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///		for (i = 0; i < MAX_INPUT_PORTS; i++)
/*TODO*///			input_analog_init[i] = 1;
/*TODO*///	}
/*TODO*///	update_input_ports();
/*TODO*///
/*TODO*///	/* if we didn't find a saved config, return 0 so the main core knows that it */
/*TODO*///	/* is the first time the game is run and it should diplay the disclaimer. */
/*TODO*///	if (f) return 1;
/*TODO*///	else return 0;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************/
/*TODO*////* Save */
/*TODO*///
/*TODO*///void save_input_port_settings(void)
/*TODO*///{
/*TODO*///	void *f;
/*TODO*///
/*TODO*///	save_default_keys();
/*TODO*///
/*TODO*///	if ((f = osd_fopen(Machine->gamedrv->name,0,OSD_FILETYPE_CONFIG,1)) != 0)
/*TODO*///	{
/*TODO*///
/*TODO*///		struct InputPort *in;
/*TODO*///		int total;
/*TODO*///		int i;
/*TODO*///
/*TODO*///
/*TODO*///		in = Machine->input_ports_default;
/*TODO*///
/*TODO*///		/* calculate the size of the array */
/*TODO*///		total = 0;
/*TODO*///		while (in->type != IPT_END)
/*TODO*///		{
/*TODO*///			total++;
/*TODO*///			in++;
/*TODO*///		}
/*TODO*///
/*TODO*///		/* write header */
/*TODO*///		osd_fwrite(f,MAMECFGSTRING_V8,8);
/*TODO*///		/* write array size */
/*TODO*///		writeint(f,total);
/*TODO*///		/* write the original settings as defined in the driver */
/*TODO*///		in = Machine->input_ports_default;
/*TODO*///		while (in->type != IPT_END)
/*TODO*///		{
/*TODO*///			input_port_write(f,in);
/*TODO*///			in++;
/*TODO*///		}
/*TODO*///		/* write the current settings */
/*TODO*///		in = Machine->input_ports;
/*TODO*///		while (in->type != IPT_END)
/*TODO*///		{
/*TODO*///			input_port_write(f,in);
/*TODO*///			in++;
/*TODO*///		}
/*TODO*///
/*TODO*///		/* write out the coin/ticket counters for this machine - LBO 042898 */
/*TODO*///		for (i = 0; i < COIN_COUNTERS; i ++)
/*TODO*///			writeint(f,coins[i]);
/*TODO*///		writeint(f,dispensed_tickets);
/*TODO*///
/*TODO*///		mixer_write_config(f);
/*TODO*///
/*TODO*///		osd_fclose(f);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////* Note that the following 3 routines have slightly different meanings with analog ports */
/*TODO*///const char *input_port_name(const struct InputPort *in)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	unsigned type;
/*TODO*///
/*TODO*///	if (in->name != IP_NAME_DEFAULT) return in->name;
/*TODO*///
/*TODO*///	i = 0;
/*TODO*///
/*TODO*///	if ((in->type & ~IPF_MASK) == IPT_EXTENSION)
/*TODO*///		type = (in-1)->type & (~IPF_MASK | IPF_PLAYERMASK);
/*TODO*///	else
/*TODO*///		type = in->type & (~IPF_MASK | IPF_PLAYERMASK);
/*TODO*///
/*TODO*///	while (inputport_defaults[i].type != IPT_END &&
/*TODO*///			inputport_defaults[i].type != type)
/*TODO*///		i++;
/*TODO*///
/*TODO*///	if ((in->type & ~IPF_MASK) == IPT_EXTENSION)
/*TODO*///		return inputport_defaults[i+1].name;
/*TODO*///	else
/*TODO*///		return inputport_defaults[i].name;
/*TODO*///}
/*TODO*///
/*TODO*///InputSeq* input_port_type_seq(int type)
/*TODO*///{
/*TODO*///	unsigned i;
/*TODO*///
/*TODO*///	i = 0;
/*TODO*///
/*TODO*///	while (inputport_defaults[i].type != IPT_END &&
/*TODO*///			inputport_defaults[i].type != type)
/*TODO*///		i++;
/*TODO*///
/*TODO*///	return &inputport_defaults[i].seq;
/*TODO*///}
/*TODO*///
/*TODO*///InputSeq* input_port_seq(const struct InputPort *in)
/*TODO*///{
/*TODO*///	int i,type;
/*TODO*///
/*TODO*///	static InputSeq ip_none = SEQ_DEF_1(CODE_NONE);
/*TODO*///
/*TODO*///	while (seq_get_1((InputSeq*)&in->seq) == CODE_PREVIOUS) in--;
/*TODO*///
/*TODO*///	if ((in->type & ~IPF_MASK) == IPT_EXTENSION)
/*TODO*///	{
/*TODO*///		type = (in-1)->type & (~IPF_MASK | IPF_PLAYERMASK);
/*TODO*///		/* if port is disabled, or cheat with cheats disabled, return no key */
/*TODO*///		if (((in-1)->type & IPF_UNUSED) || (!options.cheat && ((in-1)->type & IPF_CHEAT)))
/*TODO*///			return &ip_none;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		type = in->type & (~IPF_MASK | IPF_PLAYERMASK);
/*TODO*///		/* if port is disabled, or cheat with cheats disabled, return no key */
/*TODO*///		if ((in->type & IPF_UNUSED) || (!options.cheat && (in->type & IPF_CHEAT)))
/*TODO*///			return &ip_none;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (seq_get_1((InputSeq*)&in->seq) != CODE_DEFAULT)
/*TODO*///		return (InputSeq*)&in->seq;
/*TODO*///
/*TODO*///	i = 0;
/*TODO*///
/*TODO*///	while (inputport_defaults[i].type != IPT_END &&
/*TODO*///			inputport_defaults[i].type != type)
/*TODO*///		i++;
/*TODO*///
/*TODO*///	if ((in->type & ~IPF_MASK) == IPT_EXTENSION)
/*TODO*///		return &inputport_defaults[i+1].seq;
/*TODO*///	else
/*TODO*///		return &inputport_defaults[i].seq;
/*TODO*///}
/*TODO*///
/*TODO*///void update_analog_port(int port)
/*TODO*///{
/*TODO*///	struct InputPort *in;
/*TODO*///	int current, delta, type, sensitivity, min, max, default_value;
/*TODO*///	int axis, is_stick, check_bounds;
/*TODO*///	InputSeq* incseq;
/*TODO*///	InputSeq* decseq;
/*TODO*///	int keydelta;
/*TODO*///	int player;
/*TODO*///
/*TODO*///	/* get input definition */
/*TODO*///	in = input_analog[port];
/*TODO*///
/*TODO*///	/* if we're not cheating and this is a cheat-only port, bail */
/*TODO*///	if (!options.cheat && (in->type & IPF_CHEAT)) return;
/*TODO*///	type=(in->type & ~IPF_MASK);
/*TODO*///
/*TODO*///	decseq = input_port_seq(in);
/*TODO*///	incseq = input_port_seq(in+1);
/*TODO*///
/*TODO*///	keydelta = IP_GET_DELTA(in);
/*TODO*///
/*TODO*///	switch (type)
/*TODO*///	{
/*TODO*///		case IPT_PADDLE:
/*TODO*///			axis = X_AXIS; is_stick = 0; check_bounds = 1; break;
/*TODO*///		case IPT_PADDLE_V:
/*TODO*///			axis = Y_AXIS; is_stick = 0; check_bounds = 1; break;
/*TODO*///		case IPT_DIAL:
/*TODO*///			axis = X_AXIS; is_stick = 0; check_bounds = 0; break;
/*TODO*///		case IPT_DIAL_V:
/*TODO*///			axis = Y_AXIS; is_stick = 0; check_bounds = 0; break;
/*TODO*///		case IPT_TRACKBALL_X:
/*TODO*///			axis = X_AXIS; is_stick = 0; check_bounds = 0; break;
/*TODO*///		case IPT_TRACKBALL_Y:
/*TODO*///			axis = Y_AXIS; is_stick = 0; check_bounds = 0; break;
/*TODO*///		case IPT_AD_STICK_X:
/*TODO*///			axis = X_AXIS; is_stick = 1; check_bounds = 1; break;
/*TODO*///		case IPT_AD_STICK_Y:
/*TODO*///			axis = Y_AXIS; is_stick = 1; check_bounds = 1; break;
/*TODO*///		case IPT_PEDAL:
/*TODO*///			axis = Y_AXIS; is_stick = 0; check_bounds = 1; break;
/*TODO*///		default:
/*TODO*///			/* Use some defaults to prevent crash */
/*TODO*///			axis = X_AXIS; is_stick = 0; check_bounds = 0;
/*TODO*///			if (errorlog)
/*TODO*///				fprintf (errorlog,"Oops, polling non analog device in update_analog_port()????\n");
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	sensitivity = IP_GET_SENSITIVITY(in);
/*TODO*///	min = IP_GET_MIN(in);
/*TODO*///	max = IP_GET_MAX(in);
/*TODO*///	default_value = in->default_value * 100 / sensitivity;
/*TODO*///	/* extremes can be either signed or unsigned */
/*TODO*///	if (min > max)
/*TODO*///	{
/*TODO*///		if (in->mask > 0xff) min = min - 0x10000;
/*TODO*///		else min = min - 0x100;
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	input_analog_previous_value[port] = input_analog_current_value[port];
/*TODO*///
/*TODO*///	/* if IPF_CENTER go back to the default position */
/*TODO*///	/* sticks are handled later... */
/*TODO*///	if ((in->type & IPF_CENTER) && (!is_stick))
/*TODO*///		input_analog_current_value[port] = in->default_value * 100 / sensitivity;
/*TODO*///
/*TODO*///	current = input_analog_current_value[port];
/*TODO*///
/*TODO*///	delta = 0;
/*TODO*///
/*TODO*///	switch (in->type & IPF_PLAYERMASK)
/*TODO*///	{
/*TODO*///		case IPF_PLAYER2:          player = 1; break;
/*TODO*///		case IPF_PLAYER3:          player = 2; break;
/*TODO*///		case IPF_PLAYER4:          player = 3; break;
/*TODO*///		case IPF_PLAYER1: default: player = 0; break;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (axis == X_AXIS)
/*TODO*///		delta = mouse_delta_x[player];
/*TODO*///	else
/*TODO*///		delta = mouse_delta_y[player];
/*TODO*///
/*TODO*///	if (seq_pressed(decseq)) delta -= keydelta;
/*TODO*///
/*TODO*///	if (type != IPT_PEDAL)
/*TODO*///	{
/*TODO*///		if (seq_pressed(incseq)) delta += keydelta;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		/* is this cheesy or what? */
/*TODO*///		if (!delta && seq_get_1(incseq) == KEYCODE_Y) delta += keydelta;
/*TODO*///		delta = -delta;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (in->type & IPF_REVERSE) delta = -delta;
/*TODO*///
/*TODO*///	if (is_stick)
/*TODO*///	{
/*TODO*///		int new, prev;
/*TODO*///
/*TODO*///		/* center stick */
/*TODO*///		if ((delta == 0) && (in->type & IPF_CENTER))
/*TODO*///		{
/*TODO*///			if (current > default_value)
/*TODO*///			delta = -100 / sensitivity;
/*TODO*///			if (current < default_value)
/*TODO*///			delta = 100 / sensitivity;
/*TODO*///		}
/*TODO*///
/*TODO*///		/* An analog joystick which is not at zero position (or has just */
/*TODO*///		/* moved there) takes precedence over all other computations */
/*TODO*///		/* analog_x/y holds values from -128 to 128 (yes, 128, not 127) */
/*TODO*///
/*TODO*///		if (axis == X_AXIS)
/*TODO*///		{
/*TODO*///			new  = analog_current_x[player];
/*TODO*///			prev = analog_previous_x[player];
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			new  = analog_current_y[player];
/*TODO*///			prev = analog_previous_y[player];
/*TODO*///		}
/*TODO*///
/*TODO*///		if ((new != 0) || (new-prev != 0))
/*TODO*///		{
/*TODO*///			delta=0;
/*TODO*///
/*TODO*///			if (in->type & IPF_REVERSE)
/*TODO*///			{
/*TODO*///				new  = -new;
/*TODO*///				prev = -prev;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* apply sensitivity using a logarithmic scale */
/*TODO*///			if (in->mask > 0xff)
/*TODO*///			{
/*TODO*///				if (new > 0)
/*TODO*///				{
/*TODO*///					current = (pow(new / 32768.0, 100.0 / sensitivity) * (max-in->default_value)
/*TODO*///							+ in->default_value) * 100 / sensitivity;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					current = (pow(-new / 32768.0, 100.0 / sensitivity) * (min-in->default_value)
/*TODO*///							+ in->default_value) * 100 / sensitivity;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				if (new > 0)
/*TODO*///				{
/*TODO*///					current = (pow(new / 128.0, 100.0 / sensitivity) * (max-in->default_value)
/*TODO*///							+ in->default_value) * 100 / sensitivity;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					current = (pow(-new / 128.0, 100.0 / sensitivity) * (min-in->default_value)
/*TODO*///							+ in->default_value) * 100 / sensitivity;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	current += delta;
/*TODO*///
/*TODO*///	if (check_bounds)
/*TODO*///	{
/*TODO*///		if ((current * sensitivity + 50) / 100 < min)
/*TODO*///			current = (min * 100 + sensitivity/2) / sensitivity;
/*TODO*///		if ((current * sensitivity + 50) / 100 > max)
/*TODO*///			current = (max * 100 + sensitivity/2) / sensitivity;
/*TODO*///	}
/*TODO*///
/*TODO*///	input_analog_current_value[port] = current;
/*TODO*///}
/*TODO*///
/*TODO*///static void scale_analog_port(int port)
/*TODO*///{
/*TODO*///	struct InputPort *in;
/*TODO*///	int delta,current,sensitivity;
/*TODO*///
/*TODO*///profiler_mark(PROFILER_INPUT);
/*TODO*///	in = input_analog[port];
/*TODO*///	sensitivity = IP_GET_SENSITIVITY(in);
/*TODO*///
/*TODO*///	delta = cpu_scalebyfcount(input_analog_current_value[port] - input_analog_previous_value[port]);
/*TODO*///
/*TODO*///	current = input_analog_previous_value[port] + delta;
/*TODO*///
/*TODO*///	input_port_value[port] &= ~in->mask;
/*TODO*///	input_port_value[port] |= ((current * sensitivity + 50) / 100) & in->mask;
/*TODO*///
/*TODO*///	if (playback)
/*TODO*///		readword(playback,&input_port_value[port]);
/*TODO*///	if (record)
/*TODO*///		writeword(record,input_port_value[port]);
/*TODO*///
/*TODO*///profiler_mark(PROFILER_END);
/*TODO*///}
/*TODO*///
/*TODO*///
    public static void update_input_ports()
    {
/*TODO*///	int port,ib;
/*TODO*///	struct InputPort *in;
/*TODO*///#define MAX_INPUT_BITS 1024
/*TODO*///static int impulsecount[MAX_INPUT_BITS];
/*TODO*///static int waspressed[MAX_INPUT_BITS];
/*TODO*///#define MAX_JOYSTICKS 3
/*TODO*///#define MAX_PLAYERS 4
/*TODO*///#ifdef MRU_JOYSTICK
/*TODO*///static int update_serial_number = 1;
/*TODO*///static int joyserial[MAX_JOYSTICKS*MAX_PLAYERS][4];
/*TODO*///#else
/*TODO*///int joystick[MAX_JOYSTICKS*MAX_PLAYERS][4];
/*TODO*///#endif
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///profiler_mark(PROFILER_INPUT);
/*TODO*///
/*TODO*///	/* clear all the values before proceeding */
/*TODO*///	for (port = 0;port < MAX_INPUT_PORTS;port++)
/*TODO*///	{
/*TODO*///		input_port_value[port] = 0;
/*TODO*///		input_vblank[port] = 0;
/*TODO*///		input_analog[port] = 0;
/*TODO*///	}
/*TODO*///
/*TODO*///#ifndef MRU_JOYSTICK
/*TODO*///	for (i = 0;i < 4*MAX_JOYSTICKS*MAX_PLAYERS;i++)
/*TODO*///		joystick[i/4][i%4] = 0;
/*TODO*///#endif
/*TODO*///
/*TODO*///	in = Machine->input_ports;
/*TODO*///
/*TODO*///	if (in->type == IPT_END) return; 	/* nothing to do */
/*TODO*///
/*TODO*///	/* make sure the InputPort definition is correct */
/*TODO*///	if (in->type != IPT_PORT)
/*TODO*///	{
/*TODO*///		if (errorlog) fprintf(errorlog,"Error in InputPort definition: expecting PORT_START\n");
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	else in++;
/*TODO*///
/*TODO*///#ifdef MRU_JOYSTICK
/*TODO*///	/* scan all the joystick ports */
/*TODO*///	port = 0;
/*TODO*///	while (in->type != IPT_END && port < MAX_INPUT_PORTS)
/*TODO*///	{
/*TODO*///		while (in->type != IPT_END && in->type != IPT_PORT)
/*TODO*///		{
/*TODO*///			if ((in->type & ~IPF_MASK) >= IPT_JOYSTICK_UP &&
/*TODO*///				(in->type & ~IPF_MASK) <= IPT_JOYSTICKLEFT_RIGHT)
/*TODO*///			{
/*TODO*///				InputSeq* seq;
/*TODO*///
/*TODO*///				seq = input_port_seq(in);
/*TODO*///
/*TODO*///				if (seq_get_1(seq) != 0 && seq_get_1(seq) != CODE_NONE)
/*TODO*///				{
/*TODO*///					int joynum,joydir,player;
/*TODO*///
/*TODO*///					player = 0;
/*TODO*///					if ((in->type & IPF_PLAYERMASK) == IPF_PLAYER2)
/*TODO*///						player = 1;
/*TODO*///					else if ((in->type & IPF_PLAYERMASK) == IPF_PLAYER3)
/*TODO*///						player = 2;
/*TODO*///					else if ((in->type & IPF_PLAYERMASK) == IPF_PLAYER4)
/*TODO*///						player = 3;
/*TODO*///
/*TODO*///					joynum = player * MAX_JOYSTICKS +
/*TODO*///							 ((in->type & ~IPF_MASK) - IPT_JOYSTICK_UP) / 4;
/*TODO*///					joydir = ((in->type & ~IPF_MASK) - IPT_JOYSTICK_UP) % 4;
/*TODO*///
/*TODO*///					if (seq_pressed(seq))
/*TODO*///					{
/*TODO*///						if (joyserial[joynum][joydir] == 0)
/*TODO*///							joyserial[joynum][joydir] = update_serial_number;
/*TODO*///					}
/*TODO*///					else
/*TODO*///						joyserial[joynum][joydir] = 0;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			in++;
/*TODO*///		}
/*TODO*///
/*TODO*///		port++;
/*TODO*///		if (in->type == IPT_PORT) in++;
/*TODO*///	}
/*TODO*///	update_serial_number += 1;
/*TODO*///
/*TODO*///	in = Machine->input_ports;
/*TODO*///
/*TODO*///	/* already made sure the InputPort definition is correct */
/*TODO*///	in++;
/*TODO*///#endif
/*TODO*///
/*TODO*///
/*TODO*///	/* scan all the input ports */
/*TODO*///	port = 0;
/*TODO*///	ib = 0;
/*TODO*///	while (in->type != IPT_END && port < MAX_INPUT_PORTS)
/*TODO*///	{
/*TODO*///		struct InputPort *start;
/*TODO*///
/*TODO*///
/*TODO*///		/* first of all, scan the whole input port definition and build the */
/*TODO*///		/* default value. I must do it before checking for input because otherwise */
/*TODO*///		/* multiple keys associated with the same input bit wouldn't work (the bit */
/*TODO*///		/* would be reset to its default value by the second entry, regardless if */
/*TODO*///		/* the key associated with the first entry was pressed) */
/*TODO*///		start = in;
/*TODO*///		while (in->type != IPT_END && in->type != IPT_PORT)
/*TODO*///		{
/*TODO*///			if ((in->type & ~IPF_MASK) != IPT_DIPSWITCH_SETTING &&	/* skip dipswitch definitions */
/*TODO*///				(in->type & ~IPF_MASK) != IPT_EXTENSION)			/* skip analog extension fields */
/*TODO*///			{
/*TODO*///				input_port_value[port] =
/*TODO*///						(input_port_value[port] & ~in->mask) | (in->default_value & in->mask);
/*TODO*///			}
/*TODO*///
/*TODO*///			in++;
/*TODO*///		}
/*TODO*///
/*TODO*///		/* now get back to the beginning of the input port and check the input bits. */
/*TODO*///		for (in = start;
/*TODO*///			 in->type != IPT_END && in->type != IPT_PORT;
/*TODO*///			 in++, ib++)
/*TODO*///		{
/*TODO*///			if ((in->type & ~IPF_MASK) != IPT_DIPSWITCH_SETTING &&	/* skip dipswitch definitions */
/*TODO*///					(in->type & ~IPF_MASK) != IPT_EXTENSION)		/* skip analog extension fields */
/*TODO*///			{
/*TODO*///				if ((in->type & ~IPF_MASK) == IPT_VBLANK)
/*TODO*///				{
/*TODO*///					input_vblank[port] ^= in->mask;
/*TODO*///					input_port_value[port] ^= in->mask;
/*TODO*///if (errorlog && Machine->drv->vblank_duration == 0)
/*TODO*///	fprintf(errorlog,"Warning: you are using IPT_VBLANK with vblank_duration = 0. You need to increase vblank_duration for IPT_VBLANK to work.\n");
/*TODO*///				}
/*TODO*///				/* If it's an analog control, handle it appropriately */
/*TODO*///				else if (((in->type & ~IPF_MASK) > IPT_ANALOG_START)
/*TODO*///					  && ((in->type & ~IPF_MASK) < IPT_ANALOG_END  )) /* LBO 120897 */
/*TODO*///				{
/*TODO*///					input_analog[port]=in;
/*TODO*///					/* reset the analog port on first access */
/*TODO*///					if (input_analog_init[port])
/*TODO*///					{
/*TODO*///						input_analog_init[port] = 0;
/*TODO*///						input_analog_current_value[port] = input_analog_previous_value[port]
/*TODO*///							= in->default_value * 100 / IP_GET_SENSITIVITY(in);
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					InputSeq* seq;
/*TODO*///
/*TODO*///					seq = input_port_seq(in);
/*TODO*///
/*TODO*///					if (seq_pressed(seq))
/*TODO*///					{
/*TODO*///						/* skip if coin input and it's locked out */
/*TODO*///						if ((in->type & ~IPF_MASK) >= IPT_COIN1 &&
/*TODO*///							(in->type & ~IPF_MASK) <= IPT_COIN4 &&
/*TODO*///                            coinlockedout[(in->type & ~IPF_MASK) - IPT_COIN1])
/*TODO*///						{
/*TODO*///							continue;
/*TODO*///						}
/*TODO*///
/*TODO*///						/* if IPF_RESET set, reset the first CPU */
/*TODO*///						if ((in->type & IPF_RESETCPU) && waspressed[ib] == 0)
/*TODO*///							cpu_set_reset_line(0,PULSE_LINE);
/*TODO*///
/*TODO*///						if (in->type & IPF_IMPULSE)
/*TODO*///						{
/*TODO*///if (errorlog && IP_GET_IMPULSE(in) == 0)
/*TODO*///	fprintf(errorlog,"error in input port definition: IPF_IMPULSE with length = 0\n");
/*TODO*///							if (waspressed[ib] == 0)
/*TODO*///								impulsecount[ib] = IP_GET_IMPULSE(in);
/*TODO*///								/* the input bit will be toggled later */
/*TODO*///						}
/*TODO*///						else if (in->type & IPF_TOGGLE)
/*TODO*///						{
/*TODO*///							if (waspressed[ib] == 0)
/*TODO*///							{
/*TODO*///								in->default_value ^= in->mask;
/*TODO*///								input_port_value[port] ^= in->mask;
/*TODO*///							}
/*TODO*///						}
/*TODO*///						else if ((in->type & ~IPF_MASK) >= IPT_JOYSTICK_UP &&
/*TODO*///								(in->type & ~IPF_MASK) <= IPT_JOYSTICKLEFT_RIGHT)
/*TODO*///						{
/*TODO*///							int joynum,joydir,mask,player;
/*TODO*///
/*TODO*///
/*TODO*///							player = 0;
/*TODO*///							if ((in->type & IPF_PLAYERMASK) == IPF_PLAYER2) player = 1;
/*TODO*///							else if ((in->type & IPF_PLAYERMASK) == IPF_PLAYER3) player = 2;
/*TODO*///							else if ((in->type & IPF_PLAYERMASK) == IPF_PLAYER4) player = 3;
/*TODO*///							joynum = player * MAX_JOYSTICKS +
/*TODO*///									((in->type & ~IPF_MASK) - IPT_JOYSTICK_UP) / 4;
/*TODO*///							joydir = ((in->type & ~IPF_MASK) - IPT_JOYSTICK_UP) % 4;
/*TODO*///
/*TODO*///							mask = in->mask;
/*TODO*///
/*TODO*///#ifndef MRU_JOYSTICK
/*TODO*///							/* avoid movement in two opposite directions */
/*TODO*///							if (joystick[joynum][joydir ^ 1] != 0)
/*TODO*///								mask = 0;
/*TODO*///							else if (in->type & IPF_4WAY)
/*TODO*///							{
/*TODO*///								int dir;
/*TODO*///
/*TODO*///
/*TODO*///								/* avoid diagonal movements */
/*TODO*///								for (dir = 0;dir < 4;dir++)
/*TODO*///								{
/*TODO*///									if (joystick[joynum][dir] != 0)
/*TODO*///										mask = 0;
/*TODO*///								}
/*TODO*///							}
/*TODO*///
/*TODO*///							joystick[joynum][joydir] = 1;
/*TODO*///#else
/*TODO*///							/* avoid movement in two opposite directions */
/*TODO*///							if (joyserial[joynum][joydir ^ 1] != 0)
/*TODO*///								mask = 0;
/*TODO*///							else if (in->type & IPF_4WAY)
/*TODO*///							{
/*TODO*///								int mru_dir = joydir;
/*TODO*///								int mru_serial = 0;
/*TODO*///								int dir;
/*TODO*///
/*TODO*///
/*TODO*///								/* avoid diagonal movements, use mru button */
/*TODO*///								for (dir = 0;dir < 4;dir++)
/*TODO*///								{
/*TODO*///									if (joyserial[joynum][dir] > mru_serial)
/*TODO*///									{
/*TODO*///										mru_serial = joyserial[joynum][dir];
/*TODO*///										mru_dir = dir;
/*TODO*///									}
/*TODO*///								}
/*TODO*///
/*TODO*///								if (mru_dir != joydir)
/*TODO*///									mask = 0;
/*TODO*///							}
/*TODO*///#endif
/*TODO*///
/*TODO*///							input_port_value[port] ^= mask;
/*TODO*///						}
/*TODO*///						else
/*TODO*///							input_port_value[port] ^= in->mask;
/*TODO*///
/*TODO*///						waspressed[ib] = 1;
/*TODO*///					}
/*TODO*///					else
/*TODO*///						waspressed[ib] = 0;
/*TODO*///
/*TODO*///					if ((in->type & IPF_IMPULSE) && impulsecount[ib] > 0)
/*TODO*///					{
/*TODO*///						impulsecount[ib]--;
/*TODO*///						waspressed[ib] = 1;
/*TODO*///						input_port_value[port] ^= in->mask;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		port++;
/*TODO*///		if (in->type == IPT_PORT) in++;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (playback)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///
/*TODO*///		for (i = 0; i < MAX_INPUT_PORTS; i ++)
/*TODO*///			readword(playback,&input_port_value[i]);
/*TODO*///	}
/*TODO*///	if (record)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///
/*TODO*///		for (i = 0; i < MAX_INPUT_PORTS; i ++)
/*TODO*///			writeword(record,input_port_value[i]);
/*TODO*///	}
    }
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////* used the the CPU interface to notify that VBlank has ended, so we can update */
/*TODO*////* IPT_VBLANK input ports. */
    public static void inputport_vblank_end()
    {
/*TODO*///	int port;
/*TODO*///	int i;
/*TODO*///
/*TODO*///
/*TODO*///	for (port = 0;port < MAX_INPUT_PORTS;port++)
/*TODO*///	{
/*TODO*///		if (input_vblank[port])
/*TODO*///		{
/*TODO*///			input_port_value[port] ^= input_vblank[port];
/*TODO*///			input_vblank[port] = 0;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* poll all the analog joysticks */
/*TODO*///	osd_poll_joysticks();
/*TODO*///
/*TODO*///	/* update the analog devices */
/*TODO*///	for (i = 0;i < OSD_MAX_JOY_ANALOG;i++)
/*TODO*///	{
/*TODO*///		/* update the analog joystick position */
/*TODO*///		analog_previous_x[i] = analog_current_x[i];
/*TODO*///		analog_previous_y[i] = analog_current_y[i];
/*TODO*///		osd_analogjoy_read (i, &(analog_current_x[i]), &(analog_current_y[i]));
/*TODO*///
/*TODO*///		/* update mouse/trackball position */
/*TODO*///		osd_trak_read (i, &mouse_delta_x[i], &mouse_delta_y[i]);
/*TODO*///	}
/*TODO*///
/*TODO*///	for (i = 0;i < MAX_INPUT_PORTS;i++)
/*TODO*///	{
/*TODO*///		struct InputPort *in;
/*TODO*///
/*TODO*///		in=input_analog[i];
/*TODO*///		if (in)
/*TODO*///		{
/*TODO*///			update_analog_port(i);
/*TODO*///		}
/*TODO*///	}
    }
/*TODO*///
/*TODO*///
/*TODO*///
        public static int readinputport(int port)
        {
    /*TODO*///	struct InputPort *in;
    /*TODO*///
    /*TODO*///	/* Update analog ports on demand */
    /*TODO*///	in=input_analog[port];
    /*TODO*///	if (in)
    /*TODO*///	{
    /*TODO*///		scale_analog_port(port);
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	return input_port_value[port];
          //throw new UnsupportedOperationException("readinputport unimplemented"); 
            System.out.println("TODO readinputport no : " + port);
            return 0;//TEMP
        }

      public static ReadHandlerPtr input_port_0_r = new ReadHandlerPtr() { public int handler(int offset) {return readinputport(0);}};
      public static ReadHandlerPtr input_port_1_r = new ReadHandlerPtr() { public int handler(int offset) {return readinputport(1);}};
      public static ReadHandlerPtr input_port_2_r = new ReadHandlerPtr() { public int handler(int offset) {return readinputport(2);}};
      public static ReadHandlerPtr input_port_3_r = new ReadHandlerPtr() { public int handler(int offset) {return readinputport(3);}};
      public static ReadHandlerPtr input_port_4_r = new ReadHandlerPtr() { public int handler(int offset) {return readinputport(4);}};
      public static ReadHandlerPtr input_port_5_r = new ReadHandlerPtr() { public int handler(int offset) {return readinputport(5);}};
      public static ReadHandlerPtr input_port_6_r = new ReadHandlerPtr() { public int handler(int offset) {return readinputport(6);}};
      public static ReadHandlerPtr input_port_7_r = new ReadHandlerPtr() { public int handler(int offset) {return readinputport(7);}};
      public static ReadHandlerPtr input_port_8_r = new ReadHandlerPtr() { public int handler(int offset) {return readinputport(8);}};
      public static ReadHandlerPtr input_port_9_r = new ReadHandlerPtr() { public int handler(int offset) {return readinputport(9);}};
      public static ReadHandlerPtr input_port_10_r = new ReadHandlerPtr(){ public int handler(int offset) {return readinputport(10);}};
      public static ReadHandlerPtr input_port_11_r = new ReadHandlerPtr(){ public int handler(int offset) {return readinputport(11);}};
      public static ReadHandlerPtr input_port_12_r = new ReadHandlerPtr(){ public int handler(int offset) {return readinputport(12);}};
      public static ReadHandlerPtr input_port_13_r = new ReadHandlerPtr(){ public int handler(int offset) {return readinputport(13);}};
      public static ReadHandlerPtr input_port_14_r = new ReadHandlerPtr(){ public int handler(int offset) {return readinputport(14);}};
      public static ReadHandlerPtr input_port_15_r = new ReadHandlerPtr(){ public int handler(int offset) {return readinputport(15);}};
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************/

    /* InputPort conversion */
    public static int input_port_count(InputPortTiny[] src)
    {
	int total;
        int ptr=0;
	total = 0;
	while (src[ptr].type != IPT_END)
	{
		int type = src[ptr].type & ~IPF_MASK;
		if (type > IPT_ANALOG_START && type < IPT_ANALOG_END)
			total += 2;
		else if (type != IPT_EXTENSION)
			++total;
		++ptr;//++src;
	}

	++total; /* for IPT_END */

	return total;
    }
    public static InputPort[] input_port_allocate(InputPortTiny[] src)
    {
        int dst; //struct InputPort* dst;
        int inp_ptr = 0;
        InputPort[] base;
        int total;
        
        total = input_port_count(src);
        
        base = new InputPort[total];
        dst = 0; //dst = base;
        
	while (src[inp_ptr].type != IPT_END)
	{
		int type = src[inp_ptr].type & ~IPF_MASK;
		int ext;//const struct InputPortTiny *ext;
		int src_end;//const struct InputPortTiny *src_end;
		int/*InputCode*/ seq_default;

		if (type > IPT_ANALOG_START && type < IPT_ANALOG_END)
			src_end = inp_ptr + 2;
		else
			src_end = inp_ptr + 1;

		switch (type)
		{
			case IPT_END :
			case IPT_PORT :
			case IPT_DIPSWITCH_NAME :
			case IPT_DIPSWITCH_SETTING :
				seq_default = CODE_NONE;
			break;
			default:
				seq_default = CODE_DEFAULT;
		}

		ext = src_end;
		while (inp_ptr != src_end)
		{
                    base[dst] = new InputPort();
                    base[dst].type = src[inp_ptr].type;//dst->type = src->type;
                    base[dst].mask = src[inp_ptr].mask;//dst->mask = src->mask;
                    base[dst].default_value = src[inp_ptr].default_value;//dst->default_value = src->default_value;
                    base[dst].name = src[inp_ptr].name;//dst->name = src->name;

                    if (src[ext].type == IPT_EXTENSION)
                    {
			int or1 = IP_GET_CODE_OR1(src[ext]);
			int or2 = IP_GET_CODE_OR2(src[ext]);
                        
                        if (or1 < __code_max)
                        {
				if (or2 < __code_max)
                                    seq_set_3(base[dst].seq, or1, CODE_OR, or2);//seq_set_3(&dst->seq, or1, CODE_OR, or2);
				else
                                    seq_set_1(base[dst].seq, or1);//seq_set_1(&dst->seq, or1);
                        } else {
				if (or1 == CODE_NONE)
                                    seq_set_1(base[dst].seq, or2);//seq_set_1(&dst->seq, or2);
				else
                                    seq_set_1(base[dst].seq, or1);//seq_set_1(&dst->seq, or1);
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

    public static void input_port_free(InputPort[] dst)
    {
	dst=null;
    }
}
