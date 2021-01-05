
package gr.codebb.arcadeflex.v036.mame;

import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v036.platform.input.*;
import java.util.Arrays;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;

public class input {
    /* Codes */

    /* Subtype of codes */
    public static final int CODE_TYPE_NONE              = 0; /* code not assigned */
    public static final int CODE_TYPE_KEYBOARD_OS       = 1; /* os depend code */
    public static final int CODE_TYPE_KEYBOARD_STANDARD = 2; /* standard code */
    public static final int CODE_TYPE_JOYSTICK_OS       = 3; /* os depend code */
    public static final int CODE_TYPE_JOYSTICK_STANDARD = 4; /* standard code */

    /* Informations for every input code */
    public static class code_info
    {
        int memory;/* boolean memory */
        int oscode;/* osdepend code */
        int type;/* subtype */
    }
    
    /* Main code table, generic KEYCODE_*, JOYCODE_* are index in this table */
    public static code_info[] code_map;
    
    /* Element in the table */
    static int code_mac;
    
    public static ui_info[] ui_map= new ui_info[__ipt_max];
    
    /* Create the code table */        
    public static int code_init()
    {
    	int i;
    
    	/* allocate */
    	code_map = new code_info[(int)__code_max];//(struct code_info*)malloc( __code_max * sizeof(struct code_info) );
    	for(int k=0; k< code_map.length; k++) code_map[k]=new code_info();
        if (code_map==null)
    		return -1;
    
    	code_mac = 0;
    
    	/* insert all known codes */
    	for(i=0;i<__code_max;++i)
    	{
    		code_map[code_mac].memory = 0;
    		code_map[code_mac].oscode = 0; /* not used */
    
    		if (__code_key_first <= i && i <= __code_key_last)
    			code_map[code_mac].type = CODE_TYPE_KEYBOARD_STANDARD;
    		else if (__code_joy_first <= i && i <= __code_joy_last)
    			code_map[code_mac].type = CODE_TYPE_JOYSTICK_STANDARD;
    		else
    			code_map[code_mac].type = CODE_TYPE_NONE; /* never happen */
    		++code_mac;
    	}
        //(shadow) we should intialaze that at startup
        for(i=0; i<__ipt_max; i++)
                ui_map[i]=new ui_info();
            
    	return 0;
    }
    
    /* Delete the code table */
    public static void code_close()
    {
    	code_mac = 0;
    	code_map=null;
    }
    
    /* Find the OSD record of a specific standard oscode */
    public static KeyboardInfo internal_code_find_keyboard_standard_os(int oscode)
    {
    	KeyboardInfo[] keyinfo;
    	keyinfo = osd_get_key_list();
        int ptr=0;
    	while (keyinfo[ptr].name!=null)
    	{
    		if (keyinfo[ptr].code == oscode && keyinfo[ptr].standardcode != CODE_OTHER)
    			return keyinfo[ptr];
    		++ptr;
    	}
    	return null;
    }
    
    /*TODO*///INLINE const struct JoystickInfo* internal_code_find_joystick_standard_os(unsigned oscode)
    /*TODO*///{
    /*TODO*///	const struct JoystickInfo *joyinfo;
    /*TODO*///	joyinfo = osd_get_joy_list();
    /*TODO*///	while (joyinfo->name)
    /*TODO*///	{
    /*TODO*///		if (joyinfo->code == oscode && joyinfo->standardcode != CODE_OTHER)
    /*TODO*///			return joyinfo;
    /*TODO*///		++joyinfo;
    /*TODO*///	}
    /*TODO*///	return 0;
    /*TODO*///}
    /*TODO*///
    /* Find a osdepend code in the table */
    static int code_find_os(int oscode, int type)
    {
    	int i;
    	KeyboardInfo keyinfo;
    /*TODO*///	const struct JoystickInfo *joyinfo;
    
    	/* Search on the main table */
    	for(i=__code_max;i<code_mac;++i)
    		if (code_map[i].type == type && code_map[i].oscode == oscode)
    			return i;
    
   	/* Search in the OSD tables for a standard code */
    	switch (type)
   	{
    		case CODE_TYPE_KEYBOARD_OS :
    			keyinfo = internal_code_find_keyboard_standard_os(oscode);
    			if (keyinfo!=null)
    				return keyinfo.standardcode;
    			break;
    		case CODE_TYPE_JOYSTICK_OS :
                    throw new UnsupportedOperationException("CODE_TYPE unimplemented");
    /*TODO*///			joyinfo = internal_code_find_joystick_standard_os(oscode);
    /*TODO*///			if (joyinfo)
    /*TODO*///				return joyinfo->standardcode;
    /*TODO*///			break;
    	}

    	/* os code not found */
    	return CODE_NONE;
    }
    
    /* Add a new osdepend code in the table */
    public static void code_add_os(int oscode, int type)
    {
//probably ok but throw an exception when you meet it so i can test it (shadow)
        throw new UnsupportedOperationException("code_add_os unimplemented");
    	/*code_info[] new_code_map;
        new_code_map = Arrays.copyOf(code_map,code_mac+1); //new_code_map = realloc( code_map, (code_mac+1) * sizeof(struct code_info) );
    	if (new_code_map!=null)
    	{
    		code_map = new_code_map;
    		code_map[code_mac].memory = 0;
    		code_map[code_mac].oscode = oscode;
    		code_map[code_mac].type = type;
    		++code_mac;
    	}*/
    }

    /* Find the record of a specific code type */
    
    public static KeyboardInfo internal_code_find_keyboard_standard(int code)
    {
    	KeyboardInfo[] keyinfo;
    	keyinfo = osd_get_key_list();
        int ptr=0;
    	while (keyinfo[ptr].name!=null)
    	{
    		if (keyinfo[ptr].standardcode == code)
    			return keyinfo[ptr];
    		++ptr;
    	}
    	return null;
    }
    
    public static KeyboardInfo internal_code_find_keyboard_os(int code)
    {
    	KeyboardInfo[] keyinfo;
    	keyinfo = osd_get_key_list();
        int ptr=0;
    	while (keyinfo[ptr].name!=null)
    	{
    		if (keyinfo[ptr].standardcode == CODE_OTHER && keyinfo[ptr].code == code_map[code].oscode)
    			return keyinfo[ptr];
          		++ptr;
    	}
    	return null;
    }
    /*TODO*///
    /*TODO*///INLINE const struct JoystickInfo* internal_code_find_joystick_standard(unsigned code)
    /*TODO*///{
    /*TODO*///	const struct JoystickInfo *joyinfo;
    /*TODO*///	joyinfo = osd_get_joy_list();
    /*TODO*///	while (joyinfo->name)
    /*TODO*///	{
    /*TODO*///		if (joyinfo->standardcode == code)
    /*TODO*///			return joyinfo;
    /*TODO*///		++joyinfo;
    /*TODO*///	}
    /*TODO*///	return 0;
    /*TODO*///}
    /*TODO*///
    /*TODO*///INLINE const struct JoystickInfo* internal_code_find_joystick_os(unsigned code)
    /*TODO*///{
    /*TODO*///	const struct JoystickInfo *joyinfo;
    /*TODO*///	joyinfo = osd_get_joy_list();
    /*TODO*///	while (joyinfo->name)
    /*TODO*///	{
    /*TODO*///		if (joyinfo->standardcode == CODE_OTHER && joyinfo->code == code_map[code].oscode)
    /*TODO*///			return joyinfo;
    /*TODO*///		++joyinfo;
    /*TODO*///	}
    /*TODO*///	return 0;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* Check if a code is pressed */
    public static int internal_code_pressed(int code)
    {
        KeyboardInfo keyinfo;

    /*TODO*///	const struct JoystickInfo *joyinfo;
    	switch (code_map[code].type)
    	{

    		case CODE_TYPE_KEYBOARD_STANDARD :
   			keyinfo = internal_code_find_keyboard_standard(code);
    			if (keyinfo!=null)
    				return osd_is_key_pressed(keyinfo.code);
    			break;
    		case CODE_TYPE_KEYBOARD_OS :        
    			keyinfo = internal_code_find_keyboard_os(code);
    			if (keyinfo!=null)
   				return osd_is_key_pressed(keyinfo.code);
   			break;
    		case CODE_TYPE_JOYSTICK_STANDARD :
                    //throw new UnsupportedOperationException("CODE_TYPE unimplemented");
    /*TODO*///			joyinfo = internal_code_find_joystick_standard(code);
    /*TODO*///			if (joyinfo)
    /*TODO*///				return osd_is_joy_pressed(joyinfo->code);
    			break;
    		case CODE_TYPE_JOYSTICK_OS :
                    //throw new UnsupportedOperationException("CODE_TYPE unimplemented");
    /*TODO*///			joyinfo = internal_code_find_joystick_os(code);
    /*TODO*///			if (joyinfo)
    /*TODO*///				return osd_is_joy_pressed(joyinfo->code);
    			break;
    	}
    	return 0;
    }
    
    /* Return the name of the code */
    public static String internal_code_name(int code)
    {
        KeyboardInfo keyinfo;
    /*TODO*///	const struct JoystickInfo *joyinfo;
    	switch (code_map[code].type)
    	{
    		case CODE_TYPE_KEYBOARD_STANDARD :
    			keyinfo = internal_code_find_keyboard_standard(code);
    			if (keyinfo!=null)
    				return keyinfo.name;
    			break;
    		case CODE_TYPE_KEYBOARD_OS :
    			keyinfo = internal_code_find_keyboard_os(code);
    			if (keyinfo!=null)
    				return keyinfo.name;
    			break;
    /*TODO*///		case CODE_TYPE_JOYSTICK_STANDARD :
    /*TODO*///			joyinfo = internal_code_find_joystick_standard(code);
    /*TODO*///			if (joyinfo)
    /*TODO*///				return joyinfo->name;
    /*TODO*///			break;
    /*TODO*///		case CODE_TYPE_JOYSTICK_OS :
    /*TODO*///			joyinfo = internal_code_find_joystick_os(code);
    /*TODO*///			if (joyinfo)
    /*TODO*///				return joyinfo->name;
    /*TODO*///			break;
    	}
    	return "n/a";
    }
    
    /* Update the code table */
    public static void internal_code_update()
    {
    	KeyboardInfo[] keyinfo;
    /*TODO*///	const struct JoystickInfo *joyinfo;
    
    	/* add only osdepend code because all standard codes are already present */
    
    	keyinfo = osd_get_key_list();
    	int ptr=0;
    	while (keyinfo[ptr].name!=null)
    	{
    		if (keyinfo[ptr].standardcode == CODE_OTHER)
    			if (code_find_os(keyinfo[ptr].code,CODE_TYPE_KEYBOARD_OS) == CODE_NONE)
    				code_add_os(keyinfo[ptr].code,CODE_TYPE_KEYBOARD_OS);
    		++ptr;
    	}
        
    /*TODO*///	joyinfo = osd_get_joy_list();
    /*TODO*///	while (joyinfo->name)
    /*TODO*///	{
    /*TODO*///		if (joyinfo->standardcode == CODE_OTHER)
    /*TODO*///                        if (code_find_os(joyinfo->code,CODE_TYPE_JOYSTICK_OS)==CODE_NONE)
    /*TODO*///				code_add_os(joyinfo->code,CODE_TYPE_JOYSTICK_OS);
    /*TODO*///		++joyinfo;
    /*TODO*///	}
    }
    
    /*TODO*////***************************************************************************/
    /*TODO*////* Save support */
    /*TODO*///
    /*TODO*////* Flags used in saving codes to file */
    /*TODO*///#define SAVECODE_FLAGS_TYPE_NONE        0x00000000
    /*TODO*///#define SAVECODE_FLAGS_TYPE_STANDARD    0x10000000 /* standard code */
    /*TODO*///#define SAVECODE_FLAGS_TYPE_KEYBOARD_OS 0x20000000 /* keyboard os depend code */
    /*TODO*///#define SAVECODE_FLAGS_TYPE_JOYSTICK_OS 0x30000000 /* joystick os depend code */
    /*TODO*///#define SAVECODE_FLAGS_TYPE_MASK        0xF0000000
    /*TODO*///
    /*TODO*////* Convert one key osdepend code to one standard code */
    /*TODO*///InputCode keyoscode_to_code(unsigned oscode)
    /*TODO*///{
    /*TODO*///	InputCode code;
    /*TODO*///
    /*TODO*///	if (oscode == OSD_KEY_NONE)
    /*TODO*///		return CODE_NONE;
    /*TODO*///
    /*TODO*///	code = code_find_os(oscode,CODE_TYPE_KEYBOARD_OS);
    /*TODO*///
    /*TODO*///	/* insert if missing */
    /*TODO*///	if (code == CODE_NONE)
    /*TODO*///	{
    /*TODO*///		code_add_os(oscode,CODE_TYPE_KEYBOARD_OS);
    /*TODO*///		/* this fail only if the realloc call in code_add_os fail */
    /*TODO*///		code = code_find_os(oscode,CODE_TYPE_KEYBOARD_OS);
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	return code;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* Convert one joystick osdepend code to one code */
    /*TODO*///InputCode joyoscode_to_code(unsigned oscode)
    /*TODO*///{
    /*TODO*///	InputCode code = code_find_os(oscode,CODE_TYPE_JOYSTICK_OS);
    /*TODO*///
    /*TODO*///	/* insert if missing */
    /*TODO*///	if (code == CODE_NONE)
    /*TODO*///	{
    /*TODO*///		code_add_os(oscode,CODE_TYPE_JOYSTICK_OS);
    /*TODO*///		/* this fail only if the realloc call in code_add_os fail */
    /*TODO*///		code = code_find_os(oscode,CODE_TYPE_JOYSTICK_OS);
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	return code;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* Convert one saved code to one code */
    /*TODO*///InputCode savecode_to_code(unsigned savecode)
    /*TODO*///{
    /*TODO*///	unsigned type = savecode & SAVECODE_FLAGS_TYPE_MASK;
    /*TODO*///	unsigned code = savecode & ~SAVECODE_FLAGS_TYPE_MASK;
    /*TODO*///
    /*TODO*///	switch (type)
    /*TODO*///	{
    /*TODO*///		case SAVECODE_FLAGS_TYPE_STANDARD :
    /*TODO*///			return code;
    /*TODO*///		case SAVECODE_FLAGS_TYPE_KEYBOARD_OS :
    /*TODO*///			return keyoscode_to_code(code);
    /*TODO*///		case SAVECODE_FLAGS_TYPE_JOYSTICK_OS :
    /*TODO*///			return joyoscode_to_code(code);
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* never happen */
    /*TODO*///
    /*TODO*///	return CODE_NONE;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* Convert one code to one saved code */
    /*TODO*///unsigned code_to_savecode(InputCode code)
    /*TODO*///{
    /*TODO*///	if (code < __code_max || code >= code_mac)
    /*TODO*///               	/* if greather than code_mac is a special CODE like CODE_OR */
    /*TODO*///		return code | SAVECODE_FLAGS_TYPE_STANDARD;
    /*TODO*///
    /*TODO*///	switch (code_map[code].type)
    /*TODO*///	{
    /*TODO*///		case CODE_TYPE_KEYBOARD_OS : return code_map[code].oscode | SAVECODE_FLAGS_TYPE_KEYBOARD_OS;
    /*TODO*///		case CODE_TYPE_JOYSTICK_OS : return code_map[code].oscode | SAVECODE_FLAGS_TYPE_JOYSTICK_OS;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* never happen */
    /*TODO*///
    /*TODO*///	return 0;
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************************/
    /*TODO*////* Interface */
    /*TODO*///
    public static String code_name(int code)
    {
         if (code < code_mac)
            return internal_code_name(code);

         switch (code)
         {
                case CODE_NONE: return "None";
                case CODE_NOT: return "not";
                case CODE_OR: return "or";
        }

        return "n/a";
    }
    public static int code_pressed(int code)
    {
    	int pressed;
       
    	pressed = internal_code_pressed(code);
      
    	return pressed;
    }

    public static int code_pressed_memory(int code)
    {
    	int pressed;
      
    	pressed = internal_code_pressed(code);
    
    	if (pressed!=0)
    	{
    		if (code_map[code].memory == 0)
    		{
    			code_map[code].memory = 1;
    		} else
    			pressed = 0;
    	} else
    		code_map[code].memory = 0;
       
    	return pressed;
    }
    /*TODO*///
    /*TODO*///int code_pressed_memory_repeat(InputCode code, int speed)
    /*TODO*///{
    /*TODO*///	static int counter;
    /*TODO*///	static int keydelay;
    /*TODO*///	int pressed;
    /*TODO*///
    /*TODO*///	profiler_mark(PROFILER_INPUT);
    /*TODO*///
    /*TODO*///	pressed = internal_code_pressed(code);
    /*TODO*///
    /*TODO*///	if (pressed)
    /*TODO*///	{
    /*TODO*///		if (code_map[code].memory == 0)
    /*TODO*///		{
    /*TODO*///			code_map[code].memory = 1;
    /*TODO*///			keydelay = 3;
    /*TODO*///			counter = 0;
    /*TODO*///		}
    /*TODO*///		else if (++counter > keydelay * speed * Machine->drv->frames_per_second / 60)
    /*TODO*///		{
    /*TODO*///			keydelay = 1;
    /*TODO*///			counter = 0;
    /*TODO*///		} else
    /*TODO*///			pressed = 0;
    /*TODO*///	} else
    /*TODO*///		code_map[code].memory = 0;
    /*TODO*///
    /*TODO*///	profiler_mark(PROFILER_END);
    /*TODO*///
    /*TODO*///	return pressed;
    /*TODO*///}
    
    public static int code_read_async()
    {
    	int i;
       
    	/* Update the table */
    	internal_code_update();
    
    	for(i=0;i<code_mac;++i)
    		if (code_pressed_memory(i)!=0)
    			return i;
       
    	return CODE_NONE;
    }
    /*TODO*///
    /*TODO*///InputCode code_read_sync(void)
    /*TODO*///{
    /*TODO*///	InputCode code;
    /*TODO*///	unsigned oscode;
    /*TODO*///
    /*TODO*///	/* now let the OS process it */
    /*TODO*///	oscode = osd_wait_keypress();
    /*TODO*///
    /*TODO*///	/* convert the code */
    /*TODO*///	code = keyoscode_to_code(oscode);
    /*TODO*///
    /*TODO*///	while (code == CODE_NONE)
    /*TODO*///		code = code_read_async();
    /*TODO*///
    /*TODO*///	return code;
    /*TODO*///}

    
    /***************************************************************************/
    /* Sequences */

    public static void seq_set_0(int[] a)
    {
        for (int j = 0; j < SEQ_MAX; ++j)
            a[j] = CODE_TYPE_NONE;
    }
    public static void seq_set_1(int[] a, int code)
    {
            int j;
            a[0] = code;
            for (j = 1; j < SEQ_MAX; ++j)
                a[j] = CODE_NONE;
    }
    public static void seq_set_2(int[] a, int code1, int code2)
    {
            int j;
            a[0] = code1;
            a[1] = code2;
            for (j = 2; j < SEQ_MAX; ++j)
                a[j] = CODE_NONE;
    }
    public static void seq_set_3(int[] a, int code1, int code2, int code3)
    {
            int j;
            a[0] = code1;
            a[1] = code2;
            a[2] = code3;
            for (j = 3; j < SEQ_MAX; ++j)
                a[j] = CODE_NONE;
    }
    /*TODO*///void seq_copy(InputSeq* a, InputSeq* b)
    /*TODO*///{
    /*TODO*///	int j;
    /*TODO*///	for(j=0;j<SEQ_MAX;++j)
    /*TODO*///		(*a)[j] = (*b)[j];
    /*TODO*///}
    /*TODO*///
    /*TODO*///int seq_cmp(InputSeq* a, InputSeq* b)
    /*TODO*///{
    /*TODO*///	int j;
    /*TODO*///	for(j=0;j<SEQ_MAX;++j)
    /*TODO*///		if ((*a)[j] != (*b)[j])
    /*TODO*///			return -1;
    /*TODO*///	return 0;
    /*TODO*///}
    /*TODO*///
    static String seq_name(int[] code, int max)
    {
    	int j;
        String buffer="";
    	StringBuilder dest = new StringBuilder();
    	for(j=0;j<SEQ_MAX;++j)
    	{
    		String name;
    
    		if ((code)[j] ==CODE_NONE)
    			break;
    
    		if (j != 0 && 1 + 1 <= max)
    		{
    			dest.append(' ');//*dest = ' ';
    			//dest += 1;
    			max -= 1;
    		}
    
    		name = code_name(code[j]);
    		if (name==null)
    			break;
    
    		if (name.length() + 1 <= max)//if (strlen(name) + 1 <= max)
    		{
    			dest.append(name);
    			//dest += strlen(name);
    			max -= strlen(name);
    		}
    	}
        if (dest.toString().equals(buffer) && 4 + 1 <= max)
             return dest.append("None").toString();
        else
            return dest.toString();
    }
    
    public static boolean seq_pressed(int[] code)
    {
            int j;
            boolean res = true;
            boolean invert = false;
            int count = 0;

            for (j = 0; j < SEQ_MAX; ++j)
            {
                switch (code[j])
                {
                    case CODE_NONE:
                        return res && count != 0;
                    case CODE_OR:
                        if (res && count != 0)
                            return true;
                        res = true;
                        count = 0;
                        break;
                    case CODE_NOT:
                        invert = !invert;
                        break;
                    default:
                        if (res && (code_pressed(code[j])!=0) == invert)
                            res = false;
                        invert = false;
                        ++count;
                        break;
                }
            }
            return res && count != 0;
        }
    
    /* Static informations used in key/joy recording */
    static int[] record_seq=new int[SEQ_MAX]; /* buffer for key recording */
    static int record_count; /* number of key/joy press recorded */
    static long record_last; /* time of last key/joy press */
    
    public static final int RECORD_TIME= (UCLOCKS_PER_SEC*2/3); /* max time between key press */
    
    /* Start a sequence recording */
    public static void seq_read_async_start()
    {
    	int i;
    
    	record_count = 0;
    	record_last = uclock();
    
    	/* reset code memory, otherwise this memory may interferes with the input memory */
    	for(i=0;i<code_mac;++i)
    		code_map[i].memory = 1;
    }
    
    /* Check that almost one key/joy must be pressed */
    static boolean seq_valid(int[] seq)
    {
    	int j;
    	boolean positive = false;
    	boolean pred_not = false;
    	boolean operand = false;
    	for(j=0;j<SEQ_MAX;++j)
    	{
    		switch ((seq)[j])
    		{
    			case CODE_NONE :
    				break;
    			case CODE_OR :
    				if (!operand || !positive)
    					return false;
    				pred_not = false;
    				positive = false;
    				operand = false;
    				break;
    			case CODE_NOT :
    				if (pred_not)
    					return false;
    				pred_not = !pred_not;
    				operand = false;
    				break;
    			default:
    				if (!pred_not)
    					positive = true;
    				pred_not = false;
    				operand = true;
    				break;
    		}
    	}
    	return positive && operand;
    }
    
    /* Record a key/joy sequence
    	return <0 if more input is needed
    	return ==0 if sequence succesfully recorded
    	return >0 if aborted
    */
    static int seq_read_async(int[] seq, int first)
    {
    	int newkey;
    
    	if (input_ui_pressed(IPT_UI_CANCEL)!=0)
    		return 1;
    
    	if (record_count == SEQ_MAX
    		|| (record_count > 0 && uclock() > record_last + RECORD_TIME))	{
    		int k = 0;
    		if (first==0)
    		{
    			/* search the first space free */
    			while (k < SEQ_MAX && (seq)[k] != CODE_NONE)
    				++k;
    		}
    
    		/* if no space restart */
    		if (k + record_count + ((k!=0)? 1:0) > SEQ_MAX)
    			k = 0;
    
    		/* insert */
    		if (k + record_count + ((k!=0)? 1:0) <= SEQ_MAX)
    		{
    			int j;
    			if (k!=0)
    				(seq)[k++] = CODE_OR;
    			for(j=0;j<record_count;++j,++k)
    				(seq)[k] = record_seq[j];
    		}
    		/* fill to end */
    		while (k < SEQ_MAX)
    		{
    			(seq)[k] = CODE_NONE;
    			++k;
    		}
    
    		if (!seq_valid(seq))
    			seq_set_1(seq,CODE_NONE);
    
    		return 0;
    	}
    
    	newkey = code_read_async();
    
    	if (newkey != CODE_NONE)
    	{
    		/* if code is duplicate negate the code */
    		if (record_count!=0 && newkey == record_seq[record_count-1])
    			record_seq[record_count-1] = CODE_NOT;
    
    		record_seq[record_count++] = newkey;
    		record_last = uclock();
    	}
    
    	return -1;
    }
    
    /*TODO*////***************************************************************************/
    /*TODO*////* input ui */
    /*TODO*///
    /*TODO*////* Static buffer for memory input */
    /*TODO*///struct ui_info {
    /*TODO*///	int memory;
    /*TODO*///};
    /*TODO*///
    public static class ui_info
    {
        int memory;
    }
    

    public static int input_ui_pressed(int code)
    {

    	int pressed;
    
    	pressed = seq_pressed(input_port_type_seq(code)) ? 1 :0;
    
    	if (pressed!=0)
    	{
    		if (ui_map[code].memory == 0)
    		{
                            ui_map[code].memory = 1;
    		} else
    			pressed = 0;
    	} else
    		ui_map[code].memory = 0;
    	return pressed;
    }
    static int repeat_counter,repeat_inputdelay;
    public static int input_ui_pressed_repeat(int code,int speed)
    {
    	
    	int pressed;
    

    	pressed = seq_pressed(input_port_type_seq(code))  ? 1 :0;
    
    	if (pressed!=0)
    	{
    		if (ui_map[code].memory == 0)
    		{
    			ui_map[code].memory = 1;
    			repeat_inputdelay = 3;
    			repeat_counter = 0;
    		}
    		else if (++repeat_counter > repeat_inputdelay * speed * Machine.drv.frames_per_second / 60)
    		{
    			repeat_inputdelay = 1;
    			repeat_counter = 0;
    		} else
    			pressed = 0;
    	} else
    		ui_map[code].memory = 0;
    
    	return pressed;
    }    
}
