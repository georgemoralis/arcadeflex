
package mame;


public class inputH {
/*TODO*////*TODO*///#ifndef INPUT_H
/*TODO*///#define INPUT_H
/*TODO*///
/*TODO*///typedef unsigned InputCode;
/*TODO*///
/*TODO*///struct KeyboardInfo
/*TODO*///{
/*TODO*///	char *name; /* OS dependant name; 0 terminates the list */
/*TODO*///	unsigned code; /* OS dependant code */
/*TODO*///	InputCode standardcode;	/* CODE_xxx equivalent from list below, or CODE_OTHER if n/a */
/*TODO*///};
/*TODO*///
/*TODO*///struct JoystickInfo
/*TODO*///{
/*TODO*///	char *name; /* OS dependant name; 0 terminates the list */
/*TODO*///	unsigned code; /* OS dependant code */
/*TODO*///	InputCode standardcode;	/* CODE_xxx equivalent from list below, or CODE_OTHER if n/a */
/*TODO*///};
/*TODO*///
/*TODO*///enum
/*TODO*///{
/*TODO*///	/* key */
/*TODO*///	KEYCODE_A, KEYCODE_B, KEYCODE_C, KEYCODE_D, KEYCODE_E, KEYCODE_F,
/*TODO*///	KEYCODE_G, KEYCODE_H, KEYCODE_I, KEYCODE_J, KEYCODE_K, KEYCODE_L,
/*TODO*///	KEYCODE_M, KEYCODE_N, KEYCODE_O, KEYCODE_P, KEYCODE_Q, KEYCODE_R,
/*TODO*///	KEYCODE_S, KEYCODE_T, KEYCODE_U, KEYCODE_V, KEYCODE_W, KEYCODE_X,
/*TODO*///	KEYCODE_Y, KEYCODE_Z, KEYCODE_0, KEYCODE_1, KEYCODE_2, KEYCODE_3,
/*TODO*///	KEYCODE_4, KEYCODE_5, KEYCODE_6, KEYCODE_7, KEYCODE_8, KEYCODE_9,
/*TODO*///	KEYCODE_0_PAD, KEYCODE_1_PAD, KEYCODE_2_PAD, KEYCODE_3_PAD, KEYCODE_4_PAD,
/*TODO*///	KEYCODE_5_PAD, KEYCODE_6_PAD, KEYCODE_7_PAD, KEYCODE_8_PAD, KEYCODE_9_PAD,
/*TODO*///	KEYCODE_F1, KEYCODE_F2, KEYCODE_F3, KEYCODE_F4, KEYCODE_F5,
/*TODO*///	KEYCODE_F6, KEYCODE_F7, KEYCODE_F8, KEYCODE_F9, KEYCODE_F10,
/*TODO*///	KEYCODE_F11, KEYCODE_F12,
/*TODO*///	KEYCODE_ESC, KEYCODE_TILDE, KEYCODE_MINUS, KEYCODE_EQUALS, KEYCODE_BACKSPACE,
/*TODO*///	KEYCODE_TAB, KEYCODE_OPENBRACE, KEYCODE_CLOSEBRACE, KEYCODE_ENTER, KEYCODE_COLON,
/*TODO*///	KEYCODE_QUOTE, KEYCODE_BACKSLASH, KEYCODE_BACKSLASH2, KEYCODE_COMMA, KEYCODE_STOP,
/*TODO*///	KEYCODE_SLASH, KEYCODE_SPACE, KEYCODE_INSERT, KEYCODE_DEL,
/*TODO*///	KEYCODE_HOME, KEYCODE_END, KEYCODE_PGUP, KEYCODE_PGDN, KEYCODE_LEFT,
/*TODO*///	KEYCODE_RIGHT, KEYCODE_UP, KEYCODE_DOWN,
/*TODO*///	KEYCODE_SLASH_PAD, KEYCODE_ASTERISK, KEYCODE_MINUS_PAD, KEYCODE_PLUS_PAD,
/*TODO*///	KEYCODE_DEL_PAD, KEYCODE_ENTER_PAD, KEYCODE_PRTSCR, KEYCODE_PAUSE,
/*TODO*///	KEYCODE_LSHIFT, KEYCODE_RSHIFT, KEYCODE_LCONTROL, KEYCODE_RCONTROL,
/*TODO*///	KEYCODE_LALT, KEYCODE_RALT, KEYCODE_SCRLOCK, KEYCODE_NUMLOCK, KEYCODE_CAPSLOCK,
/*TODO*///	KEYCODE_LWIN, KEYCODE_RWIN, KEYCODE_MENU,
/*TODO*///#define __code_key_first KEYCODE_A
/*TODO*///#define __code_key_last KEYCODE_MENU
/*TODO*///
/*TODO*///	/* joy */
/*TODO*///	JOYCODE_1_LEFT,JOYCODE_1_RIGHT,JOYCODE_1_UP,JOYCODE_1_DOWN,
/*TODO*///	JOYCODE_1_BUTTON1,JOYCODE_1_BUTTON2,JOYCODE_1_BUTTON3,
/*TODO*///	JOYCODE_1_BUTTON4,JOYCODE_1_BUTTON5,JOYCODE_1_BUTTON6,
/*TODO*///	JOYCODE_2_LEFT,JOYCODE_2_RIGHT,JOYCODE_2_UP,JOYCODE_2_DOWN,
/*TODO*///	JOYCODE_2_BUTTON1,JOYCODE_2_BUTTON2,JOYCODE_2_BUTTON3,
/*TODO*///	JOYCODE_2_BUTTON4,JOYCODE_2_BUTTON5,JOYCODE_2_BUTTON6,
/*TODO*///	JOYCODE_3_LEFT,JOYCODE_3_RIGHT,JOYCODE_3_UP,JOYCODE_3_DOWN,
/*TODO*///	JOYCODE_3_BUTTON1,JOYCODE_3_BUTTON2,JOYCODE_3_BUTTON3,
/*TODO*///	JOYCODE_3_BUTTON4,JOYCODE_3_BUTTON5,JOYCODE_3_BUTTON6,
/*TODO*///	JOYCODE_4_LEFT,JOYCODE_4_RIGHT,JOYCODE_4_UP,JOYCODE_4_DOWN,
/*TODO*///	JOYCODE_4_BUTTON1,JOYCODE_4_BUTTON2,JOYCODE_4_BUTTON3,
/*TODO*///	JOYCODE_4_BUTTON4,JOYCODE_4_BUTTON5,JOYCODE_4_BUTTON6,
/*TODO*///#define __code_joy_first JOYCODE_1_LEFT
/*TODO*///#define __code_joy_last JOYCODE_4_BUTTON6
/*TODO*///
/*TODO*///	__code_max, /* Temination of standard code */
/*TODO*///
/*TODO*///	/* special */
/*TODO*///	CODE_NONE = 0x8000, /* no code, also marker of sequence end */
/*TODO*///	CODE_OTHER, /* OS code not mapped to any other code */
/*TODO*///	CODE_DEFAULT, /* special for input port definitions */
/*TODO*///        CODE_PREVIOUS, /* special for input port definitions */
/*TODO*///	CODE_NOT, /* operators for sequences */
/*TODO*///	CODE_OR /* operators for sequences */
/*TODO*///};
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
/*TODO*///INLINE int keyboard_pressed_memory(int code)
/*TODO*///{
/*TODO*///	return code_pressed_memory(code);
/*TODO*///}
/*TODO*///
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
/*TODO*////* NOTE: If you modify this value you need also to modify the SEQ_DEF declarations */
/*TODO*///#define SEQ_MAX 16
/*TODO*///
/*TODO*///typedef InputCode InputSeq[SEQ_MAX];
/*TODO*///
/*TODO*///INLINE InputCode seq_get_1(InputSeq* a) {
/*TODO*///	return (*a)[0];
/*TODO*///}
/*TODO*///
/*TODO*///void seq_set_0(InputSeq* seq);
/*TODO*///void seq_set_1(InputSeq* seq, InputCode code);
/*TODO*///void seq_set_2(InputSeq* seq, InputCode code1, InputCode code2);
/*TODO*///void seq_set_3(InputSeq* seq, InputCode code1, InputCode code2, InputCode code3);
/*TODO*///void seq_copy(InputSeq* seqdst, InputSeq* seqsrc);
/*TODO*///int seq_cmp(InputSeq* seq1, InputSeq* seq2);
/*TODO*///void seq_name(InputSeq* seq, char* buffer, unsigned max);
/*TODO*///int seq_pressed(InputSeq* seq);
/*TODO*///void seq_read_async_start(void);
/*TODO*///int seq_read_async(InputSeq* code, int first);
/*TODO*///
/*TODO*////* NOTE: It's very important that this sequence is EXACLY long SEQ_MAX */
/*TODO*///#define SEQ_DEF_6(a,b,c,d,e,f) { a, b, c, d, e, f, CODE_NONE, CODE_NONE, CODE_NONE, CODE_NONE, CODE_NONE, CODE_NONE, CODE_NONE, CODE_NONE, CODE_NONE, CODE_NONE }
/*TODO*///#define SEQ_DEF_5(a,b,c,d,e) SEQ_DEF_6(a,b,c,d,e,CODE_NONE)
/*TODO*///#define SEQ_DEF_4(a,b,c,d) SEQ_DEF_5(a,b,c,d,CODE_NONE)
/*TODO*///#define SEQ_DEF_3(a,b,c) SEQ_DEF_4(a,b,c,CODE_NONE)
/*TODO*///#define SEQ_DEF_2(a,b) SEQ_DEF_3(a,b,CODE_NONE)
/*TODO*///#define SEQ_DEF_1(a) SEQ_DEF_2(a,CODE_NONE)
/*TODO*///#define SEQ_DEF_0 SEQ_DEF_1(CODE_NONE)
/*TODO*///
/*TODO*////***************************************************************************/
/*TODO*////* input_ui */
/*TODO*///
/*TODO*///int input_ui_pressed(int code);
/*TODO*///int input_ui_pressed_repeat(int code, int speed);
/*TODO*///
/*TODO*///#endif
/*TODO*///    
}
