/**
 * ported to 0.36
 */
package arcadeflex.v036.mame;

public class cheat {

    /*TODO*////* Please don't move these #define : they'll be easier to find at the top of the file :) */
/*TODO*///
/*TODO*///#define LAST_UPDATE "99.07.17"
/*TODO*///#define LAST_CODER	"JCK"
/*TODO*///#define CHEAT_VERSION	"v1.00"
/*TODO*///
/*TODO*////* JCK 981123 Please do not remove ! Just comment it out ! */
/*TODO*////* #define JCK */
/*TODO*///
/*TODO*////* JCK 990321 Please do not remove ! Just comment it out ! */
/*TODO*////* #define USENEWREADKEY */
/*TODO*///
/*TODO*///extern int frameskip;
/*TODO*///extern int autoframeskip;
/*TODO*///#ifdef JCK
/*TODO*///extern int showfps;
/*TODO*///extern int showprofile;
/*TODO*///#endif
/*TODO*///
/*TODO*///#ifndef NEOFREE
/*TODO*///#ifndef TINY_COMPILE
/*TODO*///extern struct GameDriver driver_neogeo;
/*TODO*///#endif
/*TODO*///#endif
/*TODO*///
/*TODO*///extern unsigned char *memory_find_base (int cpu, int offset);
/*TODO*///
/*TODO*///struct cheat_struct
/*TODO*///{
/*TODO*///  int CpuNo;
/*TODO*///  int Address;
/*TODO*///  int Data;
/*TODO*///  int Special;
/*TODO*///  int Count;
/*TODO*///  int Backup;
/*TODO*///  int Minimum;
/*TODO*///  int Maximum;
/*TODO*///  char Name[40];
/*TODO*///  char More[40];
/*TODO*///};
/*TODO*///
/*TODO*///struct memory_struct
/*TODO*///{
/*TODO*///  int Enabled;
/*TODO*///  char Name[24];
/*TODO*///  void (*handler)(int,int);
/*TODO*///};
/*TODO*///
/*TODO*///#define MAX_TEXT_LINE 1000
/*TODO*///
/*TODO*///struct TextLine
/*TODO*///{
/*TODO*///	int number;
/*TODO*///	unsigned char *data;
/*TODO*///};
/*TODO*///
/*TODO*///static struct TextLine HelpLine[MAX_TEXT_LINE];
/*TODO*///
/*TODO*///
/*TODO*////* macros stolen from memory.c for our nefarious purposes: */
/*TODO*///#define MEMORY_READ(index,offset)	((*cpuintf[Machine->drv->cpu[index].cpu_type & ~CPU_FLAGS_MASK].memory_read)(offset))
/*TODO*///#define MEMORY_WRITE(index,offset,data) ((*cpuintf[Machine->drv->cpu[index].cpu_type & ~CPU_FLAGS_MASK].memory_write)(offset,data))
/*TODO*///#define ADDRESS_BITS(index) 	(cpuintf[Machine->drv->cpu[index].cpu_type & ~CPU_FLAGS_MASK].address_bits)
/*TODO*///
/*TODO*///#define MAX_ADDRESS(cpu)	(0xFFFFFFFF >> (32-ADDRESS_BITS(cpu)))
/*TODO*///
/*TODO*///#define RD_GAMERAM(cpu,a)	read_gameram(cpu,a)
/*TODO*///#define WR_GAMERAM(cpu,a,v) write_gameram(cpu,a,v)
/*TODO*///
/*TODO*///#define MAX_LOADEDCHEATS	200
/*TODO*///#define MAX_ACTIVECHEATS	15
/*TODO*///#define MAX_WATCHES 	20
/*TODO*///#define MAX_MATCHES 	(MachHeight / FontHeight - 13)
/*TODO*///#define MAX_DISPLAYCHEATS	(MachHeight / FontHeight - 12)
/*TODO*///
/*TODO*///#define MAX_DISPLAYMEM		  (MachHeight / FontHeight - 10)
/*TODO*///
/*TODO*///#define MAX_DT			130
/*TODO*///
/*TODO*///#define OFFSET_LINK_CHEAT	500
/*TODO*///
/*TODO*///#define TOTAL_CHEAT_TYPES	75
/*TODO*///#define CHEAT_NAME_MAXLEN	29
/*TODO*///#define CHEAT_FILENAME_MAXLEN	255
/*TODO*///
/*TODO*///#define SEARCH_VALUE	1
/*TODO*///#define SEARCH_TIME 2
/*TODO*///#define SEARCH_ENERGY	3
/*TODO*///#define SEARCH_BIT	4
/*TODO*///#define SEARCH_BYTE 5
/*TODO*///
/*TODO*///#define RESTORE_NOINIT	0
/*TODO*///#define RESTORE_NOSAVE	1
/*TODO*///#define RESTORE_DONE	2
/*TODO*///#define RESTORE_OK	3
/*TODO*///
/*TODO*///#define NOVALUE 			-0x0100 	/* No value has been selected */
/*TODO*///
/*TODO*///#define FIRSTPOS			(FontHeight*3/2)	/* yPos of the 1st string displayed */
/*TODO*///
/*TODO*///#define COMMENTCHEAT		999 	  /* Type of cheat for comments */
/*TODO*///#define WATCHCHEAT		998 	  /* Type of cheat for "watch-only" */
/*TODO*///
/*TODO*////* VM 981213 BEGIN */
/*TODO*////* Q: Would 0/NULL be better than -1? */
/*TODO*///#define NEW_CHEAT ((struct cheat_struct *) -1)
/*TODO*////* VM 981213 END */
/*TODO*///
/*TODO*///#define YHEAD_SELECT (FontHeight * 9)
/*TODO*///#define YFOOT_SELECT (MachHeight - (FontHeight * 2))
/*TODO*///#define YFOOT_MATCH  (MachHeight - (FontHeight * 8))
/*TODO*///#define YFOOT_WATCH  (MachHeight - (FontHeight * 3))
/*TODO*///
/*TODO*///#define YHEAD_MEMORY (FontHeight * 8)
/*TODO*///#define YFOOT_MEMORY (MachHeight - (FontHeight * 2))
/*TODO*///
/*TODO*////* This variables are not static because of extern statement */
/*TODO*///char *cheatfile = "CHEAT.DAT";
/*TODO*///char database[CHEAT_FILENAME_MAXLEN+1];
/*TODO*///
/*TODO*///char *helpfile = "CHEAT.HLP";
/*TODO*///
/*TODO*///int fastsearch = 2;
/*TODO*///int sologame   = 0;
/*TODO*///
    public static int he_did_cheat;

    /*TODO*///
/*TODO*///void	CheatListHelp( void );
/*TODO*///void	CheatListHelpEmpty( void );
/*TODO*///void	EditCheatHelp( void );
/*TODO*///void	StartSearchHelp( void );
/*TODO*///void	ChooseWatchHelp( void );
/*TODO*///void	SelectFastSearchHelp ( void );
/*TODO*///
/*TODO*///void	DisplayHelpFile( void );
/*TODO*///
/*TODO*///static int	iCheatInitialized = 0;
/*TODO*///
/*TODO*///static struct ExtMemory StartRam[MAX_EXT_MEMORY];
/*TODO*///static struct ExtMemory BackupRam[MAX_EXT_MEMORY];
/*TODO*///static struct ExtMemory FlagTable[MAX_EXT_MEMORY];
/*TODO*///
/*TODO*///static struct ExtMemory OldBackupRam[MAX_EXT_MEMORY];
/*TODO*///static struct ExtMemory OldFlagTable[MAX_EXT_MEMORY];
/*TODO*///
/*TODO*///static int ActiveCheatTotal;
/*TODO*///static int LoadedCheatTotal;
/*TODO*///static struct cheat_struct ActiveCheatTable[MAX_ACTIVECHEATS+1];
/*TODO*///static struct cheat_struct LoadedCheatTable[MAX_LOADEDCHEATS+1];
/*TODO*///
/*TODO*///static int MemoryAreasTotal;
/*TODO*///static struct memory_struct MemToScanTable[MAX_EXT_MEMORY];
/*TODO*///
/*TODO*///static unsigned int WatchesCpuNo[MAX_WATCHES];
/*TODO*///static unsigned int Watches[MAX_WATCHES];
/*TODO*///static int WatchesFlag;
/*TODO*///static int WatchX,WatchY;
/*TODO*///
/*TODO*///static int WatchGfxLen;
/*TODO*///static int WatchGfxPos;
/*TODO*///
/*TODO*///static int SaveMenu;
/*TODO*///static int SaveStartSearch;
/*TODO*///static int SaveContinueSearch;
/*TODO*///
/*TODO*///static int SaveIndex;
/*TODO*///
/*TODO*///static int StartValue;
/*TODO*///static int CurrentMethod;
/*TODO*///static int SaveMethod;
/*TODO*///
/*TODO*///static int CheatEnabled;
/*TODO*///static int WatchEnabled;
/*TODO*///
/*TODO*///static int SearchCpuNo;
/*TODO*///static int SearchCpuNoOld;
/*TODO*///
/*TODO*///static int MallocFailure;
/*TODO*///
/*TODO*///static int RebuildTables;
/*TODO*///
/*TODO*///static int MemoryAreasSelected;
/*TODO*///
/*TODO*///static int oldkey;
/*TODO*///
/*TODO*///static int MachHeight;
/*TODO*///static int MachWidth;
/*TODO*///static int FontHeight;
/*TODO*///static int FontWidth;
/*TODO*///
/*TODO*///static int ManyCpus;
/*TODO*///
/*TODO*///static int ValTmp;
/*TODO*///
/*TODO*///static int MatchFound;
/*TODO*///static int OldMatchFound;
/*TODO*///
/*TODO*///static int RestoreStatus;
/*TODO*///
/*TODO*///static int saveframeskip;
/*TODO*///static int saveautoframeskip;
/*TODO*///#ifdef JCK
/*TODO*///static int saveshowfps;
/*TODO*///static int saveshowprofile;
/*TODO*///#endif
/*TODO*///
/*TODO*///static char CCheck[2]	= "\x8C";
/*TODO*///static char CNoCheck[2] = " ";
/*TODO*///static char CComment[2] = "#";
/*TODO*///static char CMore[2]	= "+";
/*TODO*///static char CWatch[2]	= "?";
/*TODO*///
/*TODO*////* These variables are also declared in function displaymenu (USRINTRF.C) */
/*TODO*////* They should be moved somewhere else, so we could use them as extern :) */
/*TODO*///static char lefthilight[2]	 = "\x1A";
/*TODO*///static char righthilight[2]  = "\x1B";
/*TODO*///static char uparrow[2]		 = "\x18";
/*TODO*///static char downarrow[2]	 = "\x19";
/*TODO*///
/*TODO*///static unsigned char KEYCODE_chars[] =
/*TODO*///{
/*TODO*////* 0	1	 2	  3    4	5	 6	  7    8	9 */
/*TODO*///   0 , 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 	/* 0 */
/*TODO*///  'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 	/* 1 */
/*TODO*///  't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', 	/* 2 */
/*TODO*///  '3', '4', '5', '6', '7', '8', '9',  0 ,  0 ,	0 , 	/* 3 */
/*TODO*///   0 ,	0 ,  0 ,  0 ,  0 ,	0 ,  0 ,  0 ,  0 ,	0 , 	/* 4 */
/*TODO*///   0 ,	0 ,  0 ,  0 ,  0 ,	0 ,  0 ,  0 ,  0 ,	0 , 	/* 5 */
/*TODO*///  '`', '-', '=',  0 ,  0 , '[', ']',  0 , ';', '\'',    /* 6 */
/*TODO*/// '\\',	0 , ',', '.', '/', ' ',  0 ,  0 ,  0 ,	0 , 	/* 7 */
/*TODO*///   0 ,	0 ,  0 ,  0 ,  0 ,	0 , '/', '*', '-', '+', 	/* 8 */
/*TODO*///   0 ,	0 ,  0 ,  0 ,  0 ,	0 ,  0 ,  0 ,  0 ,	0 , 	/* 9 */
/*TODO*///   0 ,	0 ,  0 ,  0 ,  0 ,	0							/* 10 */
/*TODO*///};
/*TODO*///
/*TODO*///static unsigned char KEYCODE_caps[] =
/*TODO*///{
/*TODO*////* 0	1	 2	  3    4	5	 6	  7    8	9 */
/*TODO*///   0 , 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 	/* 0 */
/*TODO*///  'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 	/* 1 */
/*TODO*///  'T', 'U', 'V', 'W', 'X', 'Y', 'Z', ')', '!', '@', 	/* 2 */
/*TODO*///  '#', '$', '%', '^', '&', '*', '(',  0 ,  0 ,	0 , 	/* 3 */
/*TODO*///   0 ,	0 ,  0 ,  0 ,  0 ,	0 ,  0 ,  0 ,  0 ,	0 , 	/* 4 */
/*TODO*///   0 ,	0 ,  0 ,  0 ,  0 ,	0 ,  0 ,  0 ,  0 ,	0 , 	/* 5 */
/*TODO*///  '~', '_', '+',  0 ,  0 , '{', '}',  0 , ':', '"',     /* 6 */
/*TODO*///  '|',	0 , '<', '>', '?', ' ',  0 ,  0 ,  0 ,	0 , 	/* 7 */
/*TODO*///   0 ,	0 ,  0 ,  0 ,  0 ,	0 , '/', '*', '-', '+', 	/* 8 */
/*TODO*///   0 ,	0 ,  0 ,  0 ,  0 ,	0 ,  0 ,  0 ,  0 ,	0 , 	/* 9 */
/*TODO*///   0 ,	0 ,  0 ,  0 ,  0 ,	0							/* 10 */
/*TODO*///};
/*TODO*///
/*TODO*///void cheat_save_frameskips(void)
/*TODO*///{
/*TODO*///	saveframeskip	  = frameskip;
/*TODO*///	saveautoframeskip = autoframeskip;
/*TODO*///	#ifdef JCK
/*TODO*///	saveshowfps   = showfps;
/*TODO*///	saveshowprofile   = showprofile;
/*TODO*///	#endif
/*TODO*///	frameskip	  = 0;
/*TODO*///	autoframeskip	  = 0;
/*TODO*///	#ifdef JCK
/*TODO*///	showfps 	  = 0;
/*TODO*///	showprofile   = 0;
/*TODO*///	#endif
/*TODO*///}
/*TODO*///
/*TODO*///void cheat_rest_frameskips(void)
/*TODO*///{
/*TODO*///	frameskip	  = saveframeskip;
/*TODO*///	autoframeskip = saveautoframeskip;
/*TODO*///	#ifdef JCK
/*TODO*///	showfps 	  = saveshowfps;
/*TODO*///	showprofile   = saveshowprofile;
/*TODO*///	#endif
/*TODO*///}
/*TODO*///
/*TODO*////* MSH 990217 - JCK 990224 */
/*TODO*///int cheat_readkey(void)
/*TODO*///{
/*TODO*///#ifdef WIN32	/* MSH 990310 */
/*TODO*///	int key = keyboard_read_sync();
/*TODO*///	while (keyboard_pressed(key));
/*TODO*///	return key;
/*TODO*///#else
/*TODO*///  int key = 0;
/*TODO*///
/*TODO*///  key = keyboard_read_sync();
/*TODO*///  #ifndef USENEWREADKEY
/*TODO*///  return key;
/*TODO*///  #else
/*TODO*///  if (key != oldkey)
/*TODO*///  {
/*TODO*///	oldkey = key;
/*TODO*///	return key;
/*TODO*///  }
/*TODO*///  if (keyboard_pressed_memory_repeat(key,4))
/*TODO*///	return key;
/*TODO*///  return 0;
/*TODO*///  #endif
/*TODO*///#endif
/*TODO*///}
/*TODO*///
/*TODO*///void ClearTextLine(int addskip, int ypos)
/*TODO*///{
/*TODO*///  int i;
/*TODO*///  int xpos = 0;
/*TODO*///  int addx = 0, addy = 0;
/*TODO*///
/*TODO*///  int trueorientation;
/*TODO*///  /* hack: force the display into standard orientation to avoid */
/*TODO*///  /* rotating the user interface */
/*TODO*///  trueorientation = Machine->orientation;
/*TODO*///  Machine->orientation = Machine->ui_orientation;
/*TODO*///
/*TODO*///  if (addskip)
/*TODO*///  {
/*TODO*///	addx = Machine->uixmin;
/*TODO*///	addy = Machine->uiymin;
/*TODO*///  }
/*TODO*///
/*TODO*///  for (i = 0; (i < (MachWidth / FontWidth)); i++)
/*TODO*///  {
/*TODO*///	drawgfx(Machine->scrbitmap,Machine->uifont,' ',DT_COLOR_WHITE,0,0,
/*TODO*///			xpos+addx,ypos+addy,0,TRANSPARENCY_NONE,0);
/*TODO*///	xpos += FontWidth;
/*TODO*///  }
/*TODO*///
/*TODO*///  Machine->orientation = trueorientation;
/*TODO*///}
/*TODO*///
/*TODO*///void ClearArea(int addskip, int ystart, int yend)
/*TODO*///{
/*TODO*///  int i;
/*TODO*///  int addx = 0, addy = 0;
/*TODO*///
/*TODO*///  if (addskip)
/*TODO*///  {
/*TODO*///	addx = Machine->uixmin;
/*TODO*///	addy = Machine->uiymin;
/*TODO*///  }
/*TODO*///
/*TODO*///  for (i = ystart; (i < yend); i += FontHeight)
/*TODO*///  {
/*TODO*///	ClearTextLine (addskip,i);
/*TODO*///  }
/*TODO*///}
/*TODO*///
/*TODO*////* Print a string at the position x y
/*TODO*/// * if x = 0 then center the string in the screen
/*TODO*/// * if ForEdit = 0 then adds a cursor at the end of the text */
/*TODO*///static void CLIB_DECL xprintf(int ForEdit,int x,int y,char *fmt,...)
/*TODO*///{
/*TODO*///  struct DisplayText dt[2];
/*TODO*///  char s[80];
/*TODO*///  va_list arg_ptr;
/*TODO*///  char *format;
/*TODO*///
/*TODO*///  va_start(arg_ptr,fmt);
/*TODO*///  format=fmt;
/*TODO*///  (void) vsprintf(s,format,arg_ptr);
/*TODO*///
/*TODO*///  dt[0].text = s;
/*TODO*///  dt[0].color = DT_COLOR_WHITE;
/*TODO*///  if (x == 0)
/*TODO*///	dt[0].x = (MachWidth - FontWidth * strlen(s)) / 2;
/*TODO*///  else
/*TODO*///	dt[0].x = x;
/*TODO*///  if (dt[0].x < 0)
/*TODO*///	dt[0].x = 0;
/*TODO*///  if (dt[0].x > MachWidth)
/*TODO*///	dt[0].x = 0;
/*TODO*///  dt[0].y = y;
/*TODO*///  dt[1].text = 0;
/*TODO*///  displaytext(dt,0,1);
/*TODO*///
/*TODO*///  if (ForEdit == 1)
/*TODO*///  {
/*TODO*///	dt[0].x += FontWidth * strlen(s);
/*TODO*///	s[0] = '_';
/*TODO*///	s[1] = 0;
/*TODO*///	dt[0].color = DT_COLOR_YELLOW;
/*TODO*///	displaytext(dt,0,1);
/*TODO*///  }
/*TODO*///
/*TODO*///}
/*TODO*///
/*TODO*///void DisplayVersion(void)
/*TODO*///{
/*TODO*///  char buffer[30];
/*TODO*///
/*TODO*///  int trueorientation;
/*TODO*///
/*TODO*///  /* hack: force the display into standard orientation */
/*TODO*///  trueorientation = Machine->orientation;
/*TODO*///  Machine->orientation = Machine->ui_orientation;
/*TODO*///
/*TODO*///  #ifdef MAME_DEBUG
/*TODO*///  strcpy(buffer, LAST_CODER);
/*TODO*///  strcat(buffer, " - ");
/*TODO*///  strcat(buffer, LAST_UPDATE);
/*TODO*////*	xprintf(0, 1, MachHeight-FontHeight, buffer); */
/*TODO*///  #else
/*TODO*///  strcpy(buffer, CHEAT_VERSION);
/*TODO*////*	xprintf(0, 1, MachHeight-FontHeight, buffer); */
/*TODO*///  #endif
/*TODO*///  xprintf(0, 1, MachHeight-FontHeight, "");
/*TODO*///
/*TODO*///  Machine->orientation = trueorientation;
/*TODO*///}
/*TODO*///
/*TODO*///void cheat_clearbitmap(void)
/*TODO*///{
/*TODO*///  osd_clearbitmap(Machine->scrbitmap);
/*TODO*///  DisplayVersion();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////* Edit a string at the position x y
/*TODO*/// * if x = 0 then center the string in the screen */
/*TODO*///int xedit(int x,int y,char *inputs,int maxlen,int hexaonly)
/*TODO*///{
/*TODO*///  int i, key, length;
/*TODO*///  int done = 0;
/*TODO*///  char *buffer;
/*TODO*///
/*TODO*///  int xpos, ypos;
/*TODO*///  int CarPos;
/*TODO*///  int CarColor = DT_COLOR_WHITE;
/*TODO*///
/*TODO*///  int trueorientation;
/*TODO*///
/*TODO*///  if ((buffer = malloc(maxlen+1)) == NULL)
/*TODO*///	return(2);	  /* Cancel as if used pressed Esc */
/*TODO*///
/*TODO*///  memset (buffer, '\0', sizeof(buffer));
/*TODO*///  strncpy (buffer, inputs, maxlen+1);
/*TODO*///
/*TODO*///  length = (int)strlen (buffer);
/*TODO*///  CarPos = length;
/*TODO*///
/*TODO*///  /* hack: force the display into standard orientation to avoid */
/*TODO*///  /* rotating the user interface */
/*TODO*///  trueorientation = Machine->orientation;
/*TODO*///  Machine->orientation = Machine->ui_orientation;
/*TODO*///
/*TODO*///  do
/*TODO*///  {
/*TODO*///	ClearTextLine(1, y);
/*TODO*///	length = (int)strlen (buffer);
/*TODO*///	xpos = ( x ? x : ((MachWidth - FontWidth * length) / 2) );
/*TODO*///	ypos = y;
/*TODO*///	for (i = 0;i <= length;i ++)
/*TODO*///	{
/*TODO*///		unsigned char c = buffer[i];
/*TODO*///
/*TODO*///		if (!c)
/*TODO*///			c = ' ';
/*TODO*///		CarColor = (CarPos == i ? DT_COLOR_YELLOW : DT_COLOR_WHITE);
/*TODO*///		drawgfx(Machine->scrbitmap,Machine->uifont,c,CarColor,0,0,
/*TODO*///			xpos+Machine->uixmin+(i * FontWidth),ypos+Machine->uiymin,0,TRANSPARENCY_NONE,0);
/*TODO*///	}
/*TODO*///	DisplayVersion();	 /* Hack to update the video */
/*TODO*///
/*TODO*///	key = keyboard_read_sync();
/*TODO*///#ifdef WIN32	/* MSH 990310 */
/*TODO*///	if (!keyboard_pressed_memory_repeat(key,8))
/*TODO*///		key = 0;
/*TODO*///#endif
/*TODO*///	switch (key)
/*TODO*///	{
/*TODO*///		case KEYCODE_LEFT:
/*TODO*///		case KEYCODE_4_PAD:
/*TODO*///			if (CarPos)
/*TODO*///				CarPos--;
/*TODO*///			break;
/*TODO*///		case KEYCODE_RIGHT:
/*TODO*///		case KEYCODE_6_PAD:
/*TODO*///			if (CarPos < length)
/*TODO*///				CarPos++;
/*TODO*///			break;
/*TODO*///		case KEYCODE_HOME:
/*TODO*///		case KEYCODE_7_PAD:
/*TODO*///			CarPos = 0;
/*TODO*///			break;
/*TODO*///		case KEYCODE_END:
/*TODO*///		case KEYCODE_1_PAD:
/*TODO*///			CarPos = length;
/*TODO*///			break;
/*TODO*///		case KEYCODE_DEL:
/*TODO*///		case KEYCODE_DEL_PAD:
/*TODO*///			memset (buffer, '\0', sizeof(buffer));
/*TODO*///			CarPos = 0;
/*TODO*///			break;
/*TODO*///		case KEYCODE_INSERT:
/*TODO*///		case KEYCODE_0_PAD:
/*TODO*///			if ((length < maxlen) && (CarPos != length))
/*TODO*///			{
/*TODO*///				for (i = length; i > CarPos; i --)
/*TODO*///					buffer[i] = buffer[i-1];
/*TODO*///				buffer[CarPos] = ' ';
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		case KEYCODE_BACKSPACE:
/*TODO*///			if ((length) && (CarPos))
/*TODO*///			{
/*TODO*///				CarPos--;
/*TODO*///				for (i = CarPos; i < length; i ++)
/*TODO*///					buffer[i] = buffer[i+1];
/*TODO*///			}
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_ENTER:
/*TODO*///		case KEYCODE_ENTER_PAD:
/*TODO*///			while (keyboard_pressed(key));
/*TODO*///			done = 1;
/*TODO*///			strcpy (inputs, buffer);
/*TODO*///			break;
/*TODO*///		case KEYCODE_ESC:
/*TODO*///		case KEYCODE_TAB:
/*TODO*///			while (keyboard_pressed(key));
/*TODO*///			done = 2;
/*TODO*///			break;
/*TODO*///		default:
/*TODO*///			if (length < maxlen)
/*TODO*///			{
/*TODO*///				unsigned char c = 0;
/*TODO*///
/*TODO*///				if (hexaonly)
/*TODO*///				{
/*TODO*///					switch (key)
/*TODO*///					{
/*TODO*///						case KEYCODE_0:
/*TODO*///						case KEYCODE_1:
/*TODO*///						case KEYCODE_2:
/*TODO*///						case KEYCODE_3:
/*TODO*///						case KEYCODE_4:
/*TODO*///						case KEYCODE_5:
/*TODO*///						case KEYCODE_6:
/*TODO*///						case KEYCODE_7:
/*TODO*///						case KEYCODE_8:
/*TODO*///						case KEYCODE_9:
/*TODO*///							c = KEYCODE_chars[key+1];
/*TODO*///							break;
/*TODO*///						case KEYCODE_A:
/*TODO*///						case KEYCODE_B:
/*TODO*///						case KEYCODE_C:
/*TODO*///						case KEYCODE_D:
/*TODO*///						case KEYCODE_E:
/*TODO*///						case KEYCODE_F:
/*TODO*///							c = KEYCODE_caps[key+1];
/*TODO*///							break;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if (keyboard_pressed (KEYCODE_LSHIFT) ||
/*TODO*///						 keyboard_pressed (KEYCODE_RSHIFT))
/*TODO*///						c = KEYCODE_caps[key+1];
/*TODO*///					else
/*TODO*///						c = KEYCODE_chars[key+1];
/*TODO*///				}
/*TODO*///
/*TODO*///				if (c)
/*TODO*///				{
/*TODO*///					buffer[CarPos++] = c;
/*TODO*///				}
/*TODO*///#ifndef WIN32	 /* MSH 990310 - Windows reports modifier keys as separate presses */
/*TODO*///				else
/*TODO*///					while (keyboard_pressed(key)) ;
/*TODO*///#endif
/*TODO*///			}
/*TODO*///			break;
/*TODO*///	}
/*TODO*///  } while (done == 0);
/*TODO*///  ClearTextLine(1, y);
/*TODO*///
/*TODO*///  Machine->orientation = trueorientation;
/*TODO*///
/*TODO*///  free(buffer);
/*TODO*///
/*TODO*///  return(done);
/*TODO*///}
/*TODO*///
/*TODO*////* Function to test if a value is BCD (returns 1) or not (returns 0) */
/*TODO*///int IsBCD(int ParamValue)
/*TODO*///{
/*TODO*///  return(((ParamValue % 0x10 <= 9) & (ParamValue <= 0x99)) ? 1 : 0);
/*TODO*///}
/*TODO*///
/*TODO*////* Function to create help (returns the number of lines) */
/*TODO*///int CreateHelp (char **paDisplayText, struct TextLine *table)
/*TODO*///{
/*TODO*///  int i = 0;
/*TODO*///  int flag = 0;
/*TODO*///  int size = 0;
/*TODO*///  char str[32];
/*TODO*///  struct TextLine *txt;
/*TODO*///
/*TODO*///  txt = table;
/*TODO*///
/*TODO*///  while ((paDisplayText[i]) && (!flag))
/*TODO*///  {
/*TODO*///	strcpy (str, paDisplayText[i]);
/*TODO*///
/*TODO*///	size = sizeof(str);
/*TODO*///
/*TODO*///	txt->data = malloc (size + 1);
/*TODO*///	if (txt->data == NULL)
/*TODO*///	{
/*TODO*///		flag = 1;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		memset (txt->data, '\0', size + 1);
/*TODO*///		memcpy (txt->data, str, size);
/*TODO*///		txt->number = i++;
/*TODO*///
/*TODO*///		txt++;
/*TODO*///	}
/*TODO*///  }
/*TODO*///
/*TODO*///  return(i);
/*TODO*///}
/*TODO*///
/*TODO*////* Function to create menus (returns the number of lines) */
/*TODO*///int create_menu (char **paDisplayText, struct DisplayText *dt, int yPos)
/*TODO*///{
/*TODO*///  int i = 0;
/*TODO*///
/*TODO*///  while (paDisplayText[i])
/*TODO*///  {
/*TODO*///	if(i)
/*TODO*///	{
/*TODO*///		if (paDisplayText[i][0])
/*TODO*///		{
/*TODO*///			dt[i].y = (dt[i - 1].y + (FontHeight + 2));
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			dt[i].y = (dt[i - 1].y + (FontHeight / 2));
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///		dt[i].y = yPos;
/*TODO*///	dt[i].color = DT_COLOR_WHITE;
/*TODO*///	dt[i].text = paDisplayText[i];
/*TODO*///	dt[i].x = (MachWidth - FontWidth * strlen(dt[i].text)) / 2;
/*TODO*///	if(dt[i].x > MachWidth)
/*TODO*///		dt[i].x = 0;
/*TODO*///	i++;
/*TODO*///  }
/*TODO*///  dt[i].text = 0; /* terminate array */
/*TODO*///  return(i);
/*TODO*///}
/*TODO*///
/*TODO*////* Function to select a line from a menu (returns the key pressed) */
/*TODO*///int SelectMenu(int *s, struct DisplayText *dt, int ArrowsOnly, int WaitForKey,
/*TODO*///			int Mini, int Maxi, int ClrScr, int *done)
/*TODO*///{
/*TODO*///  int i,key;
/*TODO*///
/*TODO*///  if (ClrScr == 1)
/*TODO*///	cheat_clearbitmap();
/*TODO*///
/*TODO*///  if (Maxi<Mini)
/*TODO*///  {
/*TODO*///	xprintf(0,0,0,"SM : Mini = %d - Maxi = %d",Mini,Maxi);
/*TODO*///	key = keyboard_read_sync();
/*TODO*///	while (keyboard_pressed(key)) ; /* wait for key release */
/*TODO*///	*done = 2;
/*TODO*///	*s = NOVALUE;
/*TODO*///	return (NOVALUE);
/*TODO*///  }
/*TODO*///
/*TODO*///  *done = 0;
/*TODO*///  do
/*TODO*///  {
/*TODO*///	for (i = 0;i < Maxi + 1;i++)
/*TODO*///		dt[i].color = (i == *s) ? DT_COLOR_YELLOW : DT_COLOR_WHITE;
/*TODO*///	displaytext(dt,0,1);
/*TODO*///
/*TODO*///	/* key = keyboard_read_sync(); */
/*TODO*///	key = cheat_readkey();	  /* MSH 990217 */
/*TODO*///	switch (key)
/*TODO*///	{
/*TODO*///		case KEYCODE_DOWN:
/*TODO*///		case KEYCODE_2_PAD:
/*TODO*///			if (*s < Maxi)
/*TODO*///				(*s)++;
/*TODO*///			else
/*TODO*///				*s = Mini;
/*TODO*///			while ((*s < Maxi) && (!dt[*s].text[0]))	/* For Space In Menu */
/*TODO*///				(*s)++;
/*TODO*///			*done = 3;
/*TODO*///			break;
/*TODO*///		case KEYCODE_UP:
/*TODO*///		case KEYCODE_8_PAD:
/*TODO*///			if (*s > Mini)
/*TODO*///				(*s)--;
/*TODO*///			else
/*TODO*///				*s = Maxi;
/*TODO*///			while ((*s > Mini) && (!dt[*s].text[0]))	/* For Space In Menu */
/*TODO*///				(*s)--;
/*TODO*///			*done = 3;
/*TODO*///			break;
/*TODO*///		case KEYCODE_HOME:
/*TODO*///		case KEYCODE_7_PAD:
/*TODO*///			if (!ArrowsOnly)
/*TODO*///				*s = Mini;
/*TODO*///			else
/*TODO*///				*done = 3;
/*TODO*///			break;
/*TODO*///		case KEYCODE_END:
/*TODO*///		case KEYCODE_1_PAD:
/*TODO*///			if (!ArrowsOnly)
/*TODO*///				*s = Maxi;
/*TODO*///			else
/*TODO*///				*done = 3;
/*TODO*///			break;
/*TODO*///		case KEYCODE_ENTER:
/*TODO*///		case KEYCODE_ENTER_PAD:
/*TODO*///			while (keyboard_pressed(key));
/*TODO*///			*done = 1;
/*TODO*///			break;
/*TODO*///		case KEYCODE_ESC:
/*TODO*///		case KEYCODE_TAB:
/*TODO*///			while (keyboard_pressed(key));
/*TODO*///			*done = 2;
/*TODO*///			break;
/*TODO*///		default:
/*TODO*///			*done = 3;
/*TODO*///			break;
/*TODO*///	}
/*TODO*///	if ((*done != 0) && (WaitForKey))
/*TODO*///		while (keyboard_pressed(key)) ; /* wait for key release */
/*TODO*///  } while (*done == 0);
/*TODO*///
/*TODO*///  return (key);
/*TODO*///}
/*TODO*///
/*TODO*////* Function to select a value (returns the value or NOVALUE if KEYCODE_ESC or KEYCODE_TAB) */
/*TODO*///int SelectValue(int v, int BCDOnly, int ZeroPoss, int WrapPoss, int DispTwice,
/*TODO*///			int Mini, int Maxi, char *fmt, char *msg, int ClrScr, int yPos)
/*TODO*///{
/*TODO*///  int done,key,w,MiniIsZero;
/*TODO*///
/*TODO*///  if (ClrScr == 1)
/*TODO*///	cheat_clearbitmap();
/*TODO*///
/*TODO*///  if (Maxi<Mini)
/*TODO*///  {
/*TODO*///	xprintf(0,0,0,"SV : Mini = %d - Maxi = %d",Mini,Maxi);
/*TODO*///	key = keyboard_read_sync();
/*TODO*///	while (keyboard_pressed(key)) ; /* wait for key release */
/*TODO*///	return (NOVALUE);
/*TODO*///  }
/*TODO*///
/*TODO*///  /* align Mini and Maxi to the first BCD value */
/*TODO*///  if (BCDOnly == 1)
/*TODO*///  {
/*TODO*///	while (IsBCD(Mini) == 0) Mini-- ;
/*TODO*///	while (IsBCD(Maxi) == 0) Maxi-- ;
/*TODO*///  }
/*TODO*///  MiniIsZero = ((Mini == 0) && (ZeroPoss == 0));
/*TODO*///  /* add 1 if Mini = 0 and 0 can't be selected */
/*TODO*///  if (MiniIsZero == 1)
/*TODO*///  {
/*TODO*///	Mini ++;
/*TODO*///	while ((IsBCD(Mini) == 0) && (BCDOnly == 1)) Mini++ ;	 /* JCK 990701 */
/*TODO*///	Maxi ++;
/*TODO*///	while ((IsBCD(Maxi) == 0) && (BCDOnly == 1))  Maxi++ ;	  /* JCK 990701 */
/*TODO*///	w = v + 1;
/*TODO*///	while ((IsBCD(w) == 0) && (BCDOnly == 1))  w++ ;	/* JCK 990701 */
/*TODO*///  }
/*TODO*///  else
/*TODO*///	w = v;
/*TODO*///
/*TODO*///  if (msg[0])
/*TODO*///  {
/*TODO*///  xprintf(0, 0, yPos, msg);
/*TODO*///  yPos += FontHeight + 2;
/*TODO*///  }
/*TODO*///
/*TODO*///  xprintf(0, 0, yPos, "(Arrow Keys Change Value)");
/*TODO*///  yPos += 2*FontHeight;
/*TODO*///
/*TODO*///  oldkey = 0;
/*TODO*///
/*TODO*///  done = 0;
/*TODO*///  do
/*TODO*///
/*TODO*///	/* JCK 990701 BEGIN */
/*TODO*///  {
/*TODO*///	if ((w < Mini) || (w > Maxi))
/*TODO*///	{
/*TODO*///	xprintf(0,0,0,"Value is out of range !");
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///	ClearTextLine (1,0);
/*TODO*///	}
/*TODO*///	/* JCK 990701 END */
/*TODO*///
/*TODO*///	if (BCDOnly == 0)
/*TODO*///	{
/*TODO*///		if (DispTwice == 0)
/*TODO*///			xprintf(0, 0, yPos, fmt, w);
/*TODO*///		else
/*TODO*///			xprintf(0, 0, yPos, fmt, w, w);
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		if (w<=0xFF)
/*TODO*///			xprintf(0, 0, yPos, "%02X", w);
/*TODO*///		else
/*TODO*///			xprintf(0, 0, yPos, "%03X", w);
/*TODO*///	}
/*TODO*///
/*TODO*///#ifdef WIN32	/* MSH 990310 */
/*TODO*///	key = keyboard_read_sync();
/*TODO*///	  if (!keyboard_pressed_memory_repeat(key,8))
/*TODO*///	key = 0;
/*TODO*///#else
/*TODO*///	key = cheat_readkey();	  /* MSH 990217 */
/*TODO*///#endif
/*TODO*///	switch (key)
/*TODO*///	{
/*TODO*///		case KEYCODE_RIGHT:
/*TODO*///		case KEYCODE_6_PAD:
/*TODO*///		case KEYCODE_UP:
/*TODO*///		case KEYCODE_8_PAD:
/*TODO*///			if (w < Maxi)
/*TODO*///			{
/*TODO*///				w++;
/*TODO*///				if (BCDOnly == 1)
/*TODO*///					while (IsBCD(w) == 0) w++ ;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				if (WrapPoss == 1)
/*TODO*///					w = Mini;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		case KEYCODE_LEFT:
/*TODO*///		case KEYCODE_4_PAD:
/*TODO*///		case KEYCODE_DOWN:
/*TODO*///		case KEYCODE_2_PAD:
/*TODO*///			if (w > Mini)
/*TODO*///			{
/*TODO*///				w--;
/*TODO*///				if (BCDOnly == 1)
/*TODO*///					while (IsBCD(w) == 0) w-- ;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				if (WrapPoss == 1)
/*TODO*///					w = Maxi;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		case KEYCODE_HOME:
/*TODO*///		case KEYCODE_7_PAD:
/*TODO*///			w = Mini;
/*TODO*///			break;
/*TODO*///		case KEYCODE_END:
/*TODO*///		case KEYCODE_1_PAD:
/*TODO*///			w = Maxi;
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_F1:
/*TODO*///			done = 3;
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_ENTER:
/*TODO*///		case KEYCODE_ENTER_PAD:
/*TODO*///			while (keyboard_pressed(key));
/*TODO*///			done = 1;
/*TODO*///			break;
/*TODO*///		case KEYCODE_ESC:
/*TODO*///		case KEYCODE_TAB:
/*TODO*///			while (keyboard_pressed(key));
/*TODO*///			done = 2;
/*TODO*///			break;
/*TODO*///		default:
/*TODO*///			while (keyboard_pressed(key));
/*TODO*///			break;
/*TODO*///	}
/*TODO*///  } while (done == 0);
/*TODO*///
/*TODO*///  if (ClrScr == 1)
/*TODO*///	cheat_clearbitmap();
/*TODO*///
/*TODO*///  if (done == 2)
/*TODO*///	return (NOVALUE);
/*TODO*///
/*TODO*///  if (done == 3)
/*TODO*///	return (2 * NOVALUE);
/*TODO*///
/*TODO*///  /* sub 1 if Mini = 0 */
/*TODO*///  if (MiniIsZero == 1)
/*TODO*///  {
/*TODO*///	v = w - 1;
/*TODO*///	if (BCDOnly == 1)
/*TODO*///		while (IsBCD(v) == 0) v-- ;
/*TODO*///  }
/*TODO*///  else
/*TODO*///	v = w;
/*TODO*///  return (v);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////* return a format specifier for printf based on cpu address range */
/*TODO*///static char *FormatAddr(int cpu, int addtext)
/*TODO*///{
/*TODO*///	static char bufadr[10];
/*TODO*///	static char buffer[18];
/*TODO*///	  int i;
/*TODO*///
/*TODO*///	memset (buffer, '\0', strlen(buffer));
/*TODO*///	switch ((ADDRESS_BITS(cpu)+3) >> 2)
/*TODO*///	{
/*TODO*///		case 4:
/*TODO*///			strcpy (bufadr, "%04X");
/*TODO*///			break;
/*TODO*///		case 5:
/*TODO*///			strcpy (bufadr, "%05X");
/*TODO*///			break;
/*TODO*///		case 6:
/*TODO*///			strcpy (bufadr, "%06X");
/*TODO*///			break;
/*TODO*///		case 7:
/*TODO*///			strcpy (bufadr, "%07X");
/*TODO*///			break;
/*TODO*///		case 8:
/*TODO*///			strcpy (bufadr, "%08X");
/*TODO*///			break;
/*TODO*///		default:
/*TODO*///			strcpy (bufadr, "%X");
/*TODO*///			break;
/*TODO*///	}
/*TODO*///	  if (addtext)
/*TODO*///	  {
/*TODO*///	strcpy (buffer, "Addr:  ");
/*TODO*///		for (i = strlen(bufadr) + 1; i < 8; i ++)
/*TODO*///		strcat (buffer, " ");
/*TODO*///	  }
/*TODO*///	  strcat (buffer,bufadr);
/*TODO*///	return buffer;
/*TODO*///}
/*TODO*///
/*TODO*///void AddCpuToAddr(int cpu, int addr, int data, char *buffer)
/*TODO*///{
/*TODO*///	char fmt[32];
/*TODO*///
/*TODO*///	strcpy (fmt, FormatAddr(cpu ,0));
/*TODO*///	if (ManyCpus)
/*TODO*///	{
/*TODO*///		strcat(fmt," (%01X) = %02X");
/*TODO*///		sprintf (buffer, fmt, addr, cpu, data);
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		strcat(fmt," = %02X");
/*TODO*///		sprintf (buffer, fmt, addr, data);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void AddCpuToWatch(int NoWatch,char *buffer)
/*TODO*///{
/*TODO*///  char bufadd[10];
/*TODO*///
/*TODO*///  if ( Watches[NoWatch] > MAX_ADDRESS(WatchesCpuNo[NoWatch]))
/*TODO*///	Watches[NoWatch] = MAX_ADDRESS(WatchesCpuNo[NoWatch]);
/*TODO*///
/*TODO*///  sprintf(buffer, FormatAddr(WatchesCpuNo[NoWatch],0),
/*TODO*///			Watches[NoWatch]);
/*TODO*///  if (ManyCpus)
/*TODO*///  {
/*TODO*///	sprintf (bufadd, "  (%01X)", WatchesCpuNo[NoWatch]);
/*TODO*///	strcat(buffer,bufadd);
/*TODO*///  }
/*TODO*///}
/*TODO*///
/*TODO*///void AddCheckToName(int NoCheat,char *buffer)
/*TODO*///{
/*TODO*///  int i = 0;
/*TODO*///  int flag = 0;
/*TODO*///  int Special;
/*TODO*///
/*TODO*///  if (LoadedCheatTable[NoCheat].Special == COMMENTCHEAT)
/*TODO*///	strcpy(buffer,CComment);
/*TODO*///  else if (LoadedCheatTable[NoCheat].Special == WATCHCHEAT)
/*TODO*///	strcpy(buffer,CWatch);
/*TODO*///  else
/*TODO*///  {
/*TODO*///	for (i = 0;i < ActiveCheatTotal && !flag;i ++)
/*TODO*///	{
/*TODO*///		Special = ActiveCheatTable[i].Special;
/*TODO*///		if (Special >= 1000)
/*TODO*///			Special -= 1000;
/*TODO*///
/*TODO*///		if ((Special < 60) || (Special > 99))
/*TODO*///		{
/*TODO*///			if (	(ActiveCheatTable[i].Address	== LoadedCheatTable[NoCheat].Address)	&&
/*TODO*///				(ActiveCheatTable[i].Data	== LoadedCheatTable[NoCheat].Data	)	&&
/*TODO*///				(Special				== LoadedCheatTable[NoCheat].Special)	)
/*TODO*///				flag = 1;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if (	(ActiveCheatTable[i].Address	== LoadedCheatTable[NoCheat].Address)	&&
/*TODO*///				(Special				== LoadedCheatTable[NoCheat].Special)	)
/*TODO*///				flag = 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	strcpy(buffer,(flag ? CCheck : CNoCheck));
/*TODO*///
/*TODO*///	if (flag)
/*TODO*///		strcpy(ActiveCheatTable[i-1].Name, LoadedCheatTable[NoCheat].Name);
/*TODO*///
/*TODO*///  }
/*TODO*///
/*TODO*///  if (LoadedCheatTable[NoCheat].More[0])
/*TODO*///	strcat(buffer,CMore);
/*TODO*///  else
/*TODO*///	strcat(buffer," ");
/*TODO*///
/*TODO*///  strcat(buffer,LoadedCheatTable[NoCheat].Name);
/*TODO*///  for (i = strlen(buffer);i < CHEAT_NAME_MAXLEN+2;i ++)
/*TODO*///	buffer[i]=' ';
/*TODO*///
/*TODO*///  buffer[CHEAT_NAME_MAXLEN+2]=0;
/*TODO*///}
/*TODO*///
/*TODO*///void AddCheckToMemArea(int NoMemArea,char *buffer)
/*TODO*///{
/*TODO*///  int flag = 0;
/*TODO*///
/*TODO*///  if (MemToScanTable[NoMemArea].Enabled)
/*TODO*///	flag = 1;
/*TODO*///  strcpy(buffer,(flag ? CCheck : CNoCheck));
/*TODO*///  strcat(buffer," ");
/*TODO*///  strcat(buffer,MemToScanTable[NoMemArea].Name);
/*TODO*///}
/*TODO*///
/*TODO*////* read a byte from cpu at address <add> */
/*TODO*///static unsigned char read_gameram (int cpu, int add)
/*TODO*///{
/*TODO*///	int save = cpu_getactivecpu();
/*TODO*///	unsigned char data;
/*TODO*///
/*TODO*///	memorycontextswap(cpu);
/*TODO*///
/*TODO*///	data = MEMORY_READ(cpu,add);
/*TODO*///	/* data = *(unsigned char *)memory_find_base (cpu, add); */
/*TODO*///
/*TODO*///	if (cpu != save)
/*TODO*///		memorycontextswap(save);
/*TODO*///
/*TODO*///	return data;
/*TODO*///}
/*TODO*///
/*TODO*////* write a byte to CPU 0 ram at address <add> */
/*TODO*///static void write_gameram (int cpu, int add, unsigned char data)
/*TODO*///{
/*TODO*///	int save = cpu_getactivecpu();
/*TODO*///
/*TODO*///	memorycontextswap(cpu);
/*TODO*///
/*TODO*///	MEMORY_WRITE(cpu,add,data);
/*TODO*///	/* *(unsigned char *)memory_find_base (cpu, add) = data; */
/*TODO*///
/*TODO*///	if (cpu != save)
/*TODO*///		memorycontextswap(save);
/*TODO*///}
/*TODO*///
/*TODO*////* make a copy of a source ram table to a dest. ram table */
/*TODO*///static void copy_ram (struct ExtMemory *dest, struct ExtMemory *src)
/*TODO*///{
/*TODO*///	struct ExtMemory *ext_dest, *ext_src;
/*TODO*///
/*TODO*///	for (ext_src = src, ext_dest = dest; ext_src->data; ext_src++, ext_dest++)
/*TODO*///	{
/*TODO*///		memcpy (ext_dest->data, ext_src->data, ext_src->end - ext_src->start + 1);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* make a copy of each ram area from search CPU ram to the specified table */
/*TODO*///static void backup_ram (struct ExtMemory *table)
/*TODO*///{
/*TODO*///	struct ExtMemory *ext;
/*TODO*///	unsigned char *gameram;
/*TODO*///
/*TODO*///	for (ext = table; ext->data; ext++)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///		gameram = memory_find_base (SearchCpuNo, ext->start);
/*TODO*///		memcpy (ext->data, gameram, ext->end - ext->start + 1);
/*TODO*///		for (i=0; i <= ext->end - ext->start; i++)
/*TODO*///			ext->data[i] = RD_GAMERAM(SearchCpuNo, i+ext->start);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* set every byte in specified table to data */
/*TODO*///static void memset_ram (struct ExtMemory *table, unsigned char data)
/*TODO*///{
/*TODO*///	struct ExtMemory *ext;
/*TODO*///
/*TODO*///	for (ext = table; ext->data; ext++)
/*TODO*///		memset (ext->data, data, ext->end - ext->start + 1);
/*TODO*///}
/*TODO*///
/*TODO*////* free all the memory and init the table */
/*TODO*///static void reset_table (struct ExtMemory *table)
/*TODO*///{
/*TODO*///	struct ExtMemory *ext;
/*TODO*///
/*TODO*///	for (ext = table; ext->data; ext++)
/*TODO*///		free (ext->data);
/*TODO*///	memset (table, 0, sizeof (struct ExtMemory) * MAX_EXT_MEMORY);
/*TODO*///}
/*TODO*///
/*TODO*////* free all the memory and init the table */
/*TODO*///static void reset_texttable (struct TextLine *table)
/*TODO*///{
/*TODO*///	struct TextLine *txt;
/*TODO*///
/*TODO*///	for (txt = table; txt->data; txt++)
/*TODO*///		free (txt->data);
/*TODO*///	memset (table, 0, sizeof (struct TextLine) * MAX_TEXT_LINE);
/*TODO*///}
/*TODO*///
/*TODO*////* Returns 1 if memory area has to be skipped */
/*TODO*///int SkipBank(int CpuToScan, int *BankToScanTable, void (*handler)(int,int))
/*TODO*///{
/*TODO*///	int res = 0;
/*TODO*///
/*TODO*///	if ((fastsearch == 1) || (fastsearch == 2))
/*TODO*///	{
/*TODO*///		switch ((FPTR)handler)
/*TODO*///		{
/*TODO*///			case (FPTR)MWA_RAM:
/*TODO*///				res = !BankToScanTable[0];
/*TODO*///				break;
/*TODO*///			case (FPTR)MWA_BANK1:
/*TODO*///				res = !BankToScanTable[1];
/*TODO*///				break;
/*TODO*///			case (FPTR)MWA_BANK2:
/*TODO*///				res = !BankToScanTable[2];
/*TODO*///				break;
/*TODO*///			case (FPTR)MWA_BANK3:
/*TODO*///				res = !BankToScanTable[3];
/*TODO*///				break;
/*TODO*///			case (FPTR)MWA_BANK4:
/*TODO*///				res = !BankToScanTable[4];
/*TODO*///				break;
/*TODO*///			case (FPTR)MWA_BANK5:
/*TODO*///				res = !BankToScanTable[5];
/*TODO*///				break;
/*TODO*///			case (FPTR)MWA_BANK6:
/*TODO*///				res = !BankToScanTable[6];
/*TODO*///				break;
/*TODO*///			case (FPTR)MWA_BANK7:
/*TODO*///				res = !BankToScanTable[7];
/*TODO*///				break;
/*TODO*///			case (FPTR)MWA_BANK8:
/*TODO*///				res = !BankToScanTable[8];
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				res = 1;
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return(res);
/*TODO*///}
/*TODO*///
/*TODO*////* create tables for storing copies of all MWA_RAM areas */
/*TODO*///static int build_tables (void)
/*TODO*///{
/*TODO*///	/* const struct MemoryReadAddress *mra = Machine->drv->cpu[SearchCpuNo].memory_read; */
/*TODO*///	const struct MemoryWriteAddress *mwa = Machine->drv->cpu[SearchCpuNo].memory_write;
/*TODO*///
/*TODO*///	int region = REGION_CPU1+SearchCpuNo;
/*TODO*///
/*TODO*///	struct ExtMemory *ext_sr = StartRam;
/*TODO*///	struct ExtMemory *ext_br = BackupRam;
/*TODO*///	struct ExtMemory *ext_ft = FlagTable;
/*TODO*///
/*TODO*///	struct ExtMemory *ext_obr = OldBackupRam;
/*TODO*///	struct ExtMemory *ext_oft = OldFlagTable;
/*TODO*///
/*TODO*///	int i, yPos;
/*TODO*///
/*TODO*///	int NoMemArea = 0;
/*TODO*///
/*TODO*///	  /* Trap memory allocation errors */
/*TODO*///	int MemoryNeeded = 0;
/*TODO*///
/*TODO*///	/* Search speedup : (the games should be dasmed to confirm this) */
/*TODO*///	  /* Games based on Exterminator driver should scan BANK1		   */
/*TODO*///	  /* Games based on SmashTV driver should scan BANK2		   */
/*TODO*///	  /* NEOGEO games should only scan BANK1 (0x100000 -> 0x01FFFF)    */
/*TODO*///	int CpuToScan = -1;
/*TODO*///	  int BankToScanTable[9];	 /* 0 for RAM & 1-8 for Banks 1-8 */
/*TODO*///
/*TODO*///	  for (i = 0; i < 9;i ++)
/*TODO*///	BankToScanTable[i] = ( fastsearch != 2 );
/*TODO*///
/*TODO*///#if (HAS_TMS34010)
/*TODO*///	if ((Machine->drv->cpu[1].cpu_type & ~CPU_FLAGS_MASK) == CPU_TMS34010)
/*TODO*///	{
/*TODO*///		/* 2nd CPU is 34010: games based on Exterminator driver */
/*TODO*///		CpuToScan = 0;
/*TODO*///		BankToScanTable[1] = 1;
/*TODO*///	}
/*TODO*///	else if ((Machine->drv->cpu[0].cpu_type & ~CPU_FLAGS_MASK) == CPU_TMS34010)
/*TODO*///	{
/*TODO*///		/* 1st CPU but not 2nd is 34010: games based on SmashTV driver */
/*TODO*///		CpuToScan = 0;
/*TODO*///		BankToScanTable[2] = 1;
/*TODO*///	}
/*TODO*///#endif
/*TODO*///#ifndef NEOFREE
/*TODO*///#ifndef TINY_COMPILE
/*TODO*///	if (Machine->gamedrv->clone_of == &driver_neogeo)
/*TODO*///	{
/*TODO*///		/* games based on NEOGEO driver */
/*TODO*///		CpuToScan = 0;
/*TODO*///		BankToScanTable[1] = 1;
/*TODO*///	}
/*TODO*///#endif
/*TODO*///#endif
/*TODO*///
/*TODO*///	  /* No CPU so we scan RAM & BANKn */
/*TODO*///	  if ((CpuToScan == -1) && (fastsearch == 2))
/*TODO*///	for (i = 0; i < 9;i ++)
/*TODO*///		BankToScanTable[i] = 1;
/*TODO*///
/*TODO*///	/* free memory that was previously allocated if no error occured */
/*TODO*///	  /* it must also be there because mwa varies from one CPU to another */
/*TODO*///	  if (!MallocFailure)
/*TODO*///	  {
/*TODO*///		reset_table (StartRam);
/*TODO*///		reset_table (BackupRam);
/*TODO*///		reset_table (FlagTable);
/*TODO*///
/*TODO*///		reset_table (OldBackupRam);
/*TODO*///		reset_table (OldFlagTable);
/*TODO*///	  }
/*TODO*///
/*TODO*///	  MallocFailure = 0;
/*TODO*///
/*TODO*///	  /* Message to show that something is in progress */
/*TODO*///	cheat_clearbitmap();
/*TODO*///	yPos = (MachHeight - FontHeight) / 2;
/*TODO*///	xprintf(0, 0, yPos, "Allocating Memory...");
/*TODO*///
/*TODO*///	NoMemArea = 0;
/*TODO*///	while (mwa->start != -1)
/*TODO*///	{
/*TODO*///		/* int (*handler)(int) = mra->handler; */
/*TODO*///		void (*handler)(int,int) = mwa->handler;
/*TODO*///		int size = (mwa->end - mwa->start) + 1;
/*TODO*///
/*TODO*///		if (SkipBank(CpuToScan, BankToScanTable, handler))
/*TODO*///		{
/*TODO*///			NoMemArea++;
/*TODO*///			mwa++;
/*TODO*///			continue;
/*TODO*///		}
/*TODO*///
/*TODO*///		if ((fastsearch == 3) && (!MemToScanTable[NoMemArea].Enabled))
/*TODO*///		{
/*TODO*///			NoMemArea++;
/*TODO*///			mwa++;
/*TODO*///			continue;
/*TODO*///		}
/*TODO*///
/*TODO*///		/* time to allocate */
/*TODO*///		if (!MallocFailure)
/*TODO*///		{
/*TODO*///			ext_sr->data = malloc (size);
/*TODO*///			ext_br->data = malloc (size);
/*TODO*///			ext_ft->data = malloc (size);
/*TODO*///
/*TODO*///			ext_obr->data = malloc (size);
/*TODO*///			ext_oft->data = malloc (size);
/*TODO*///
/*TODO*///			if (ext_sr->data == NULL)
/*TODO*///			{
/*TODO*///				MallocFailure = 1;
/*TODO*///				MemoryNeeded += size;
/*TODO*///			}
/*TODO*///			if (ext_br->data == NULL)
/*TODO*///			{
/*TODO*///				MallocFailure = 1;
/*TODO*///				MemoryNeeded += size;
/*TODO*///			}
/*TODO*///			if (ext_ft->data == NULL)
/*TODO*///			{
/*TODO*///				MallocFailure = 1;
/*TODO*///				MemoryNeeded += size;
/*TODO*///			}
/*TODO*///
/*TODO*///			if (ext_obr->data == NULL)
/*TODO*///			{
/*TODO*///				MallocFailure = 1;
/*TODO*///				MemoryNeeded += size;
/*TODO*///			}
/*TODO*///			if (ext_oft->data == NULL)
/*TODO*///			{
/*TODO*///				MallocFailure = 1;
/*TODO*///				MemoryNeeded += size;
/*TODO*///			}
/*TODO*///
/*TODO*///			if (!MallocFailure)
/*TODO*///			{
/*TODO*///				ext_sr->start = ext_br->start = ext_ft->start = mwa->start;
/*TODO*///				ext_sr->end = ext_br->end = ext_ft->end = mwa->end;
/*TODO*///				ext_sr->region = ext_br->region = ext_ft->region = region;
/*TODO*///				ext_sr++, ext_br++, ext_ft++;
/*TODO*///
/*TODO*///				ext_obr->start = ext_oft->start = mwa->start;
/*TODO*///				ext_obr->end = ext_oft->end = mwa->end;
/*TODO*///				ext_obr->region = ext_oft->region = region;
/*TODO*///				ext_obr++, ext_oft++;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///			MemoryNeeded += (5 * size);
/*TODO*///
/*TODO*///		NoMemArea++;
/*TODO*///		mwa++;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* free memory that was previously allocated if an error occured */
/*TODO*///	  if (MallocFailure)
/*TODO*///	  {
/*TODO*///		int key;
/*TODO*///
/*TODO*///		reset_table (StartRam);
/*TODO*///		reset_table (BackupRam);
/*TODO*///		reset_table (FlagTable);
/*TODO*///
/*TODO*///		reset_table (OldBackupRam);
/*TODO*///		reset_table (OldFlagTable);
/*TODO*///
/*TODO*///		cheat_clearbitmap();
/*TODO*///		yPos = (MachHeight - 10 * FontHeight) / 2;
/*TODO*///		xprintf(0, 0, yPos, "Error while allocating memory !");
/*TODO*///		yPos += (2 * FontHeight);
/*TODO*///		xprintf(0, 0, yPos, "You need %d more bytes", MemoryNeeded);
/*TODO*///		yPos += FontHeight;
/*TODO*///		xprintf(0, 0, yPos, "(0x%X) of free memory", MemoryNeeded);
/*TODO*///		yPos += (2 * FontHeight);
/*TODO*///		xprintf(0, 0, yPos, "No search available for CPU %d", SearchCpuNo);
/*TODO*///		yPos += (4 * FontHeight);
/*TODO*///		xprintf(0, 0, yPos, "Press A Key To Continue...");
/*TODO*///		key = keyboard_read_sync();
/*TODO*///		while (keyboard_pressed(key)) ; /* wait for key release */
/*TODO*///		cheat_clearbitmap();
/*TODO*///	  }
/*TODO*///
/*TODO*///	ClearTextLine (1, yPos);
/*TODO*///
/*TODO*///	return MallocFailure;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////* Message to show that search is in progress */
/*TODO*///void SearchInProgress(int ShowMsg, int yPos)
/*TODO*///{
/*TODO*///  if (ShowMsg)
/*TODO*///	xprintf(0, 0, yPos, "Search in Progress...");
/*TODO*///  else
/*TODO*///	ClearTextLine (1, yPos);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////* Function to rename the cheatfile (returns 1 if the file has been renamed else 0)*/
/*TODO*///int RenameCheatFile(int merge, int DisplayFileName, char *filename)
/*TODO*///{
/*TODO*///  int key;
/*TODO*///  int done = 0;
/*TODO*///  int EditYPos;
/*TODO*///  char buffer[CHEAT_FILENAME_MAXLEN+1];
/*TODO*///
/*TODO*///  EditYPos = (MachHeight - 7 * FontHeight) / 2;
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///
/*TODO*///  if (merge < 0)
/*TODO*///	xprintf (0, 0, EditYPos-(FontHeight*2),
/*TODO*///			"Enter the Cheat Filename");
/*TODO*///  else
/*TODO*///	xprintf (0, 0, EditYPos-(FontHeight*2),
/*TODO*///			"Enter the Filename to %s:",(merge ? "Add" : "Load"));
/*TODO*///
/*TODO*///  memset (buffer, '\0', CHEAT_FILENAME_MAXLEN+1);
/*TODO*///  strncpy (buffer, filename, CHEAT_FILENAME_MAXLEN);
/*TODO*///
/*TODO*///  done = xedit(0, EditYPos, buffer, CHEAT_FILENAME_MAXLEN, 0);
/*TODO*///  if (done == 1)
/*TODO*///  {
/*TODO*///	strcpy (filename, buffer);
/*TODO*///	  if (DisplayFileName)
/*TODO*///	  {
/*TODO*///		cheat_clearbitmap();
/*TODO*///		  xprintf (0, 0, EditYPos-(FontHeight*2), "Cheat Filename is now:");
/*TODO*///		xprintf (0, 0, EditYPos, "%s", buffer);
/*TODO*///		EditYPos += 4*FontHeight;
/*TODO*///		xprintf(0, 0,EditYPos,"Press A Key To Continue...");
/*TODO*///		key = keyboard_read_sync();
/*TODO*///		while (keyboard_pressed(key)) ; /* wait for key release */
/*TODO*///	  }
/*TODO*///  }
/*TODO*///  cheat_clearbitmap();
/*TODO*///  return(done);
/*TODO*///}
/*TODO*///
/*TODO*////* Function who loads the cheats for a game */
/*TODO*///int SaveCheat(int NoCheat)
/*TODO*///{
/*TODO*///	gzFile *gz;
/*TODO*///	FILE *f;
/*TODO*///	char fmt[32];
/*TODO*///	int i, c;
/*TODO*///	int count = 0;
/*TODO*///	int gz_magic[2] = {0x1f, 0x8b}; /* gzip magic header */
/*TODO*///	int uncompressed = 0;
/*TODO*///
/*TODO*///	if ((f = fopen(database,"rb")) != 0)
/*TODO*///	{
/*TODO*///		for (i = 0; i < 2; i++)
/*TODO*///		{
/*TODO*///			c = getc(f);
/*TODO*///			if (c != gz_magic[i])
/*TODO*///				uncompressed = 1;
/*TODO*///		}
/*TODO*///		fclose (f);
/*TODO*///	}
/*TODO*///
/*TODO*///	f = 0; gz = 0;
/*TODO*///
/*TODO*///	if (uncompressed)
/*TODO*///		f = fopen(database, "a");
/*TODO*///	else
/*TODO*///		gz = gzopen(database,"a");
/*TODO*///
/*TODO*///	if (f || gz)
/*TODO*///	{
/*TODO*///		for (i = 0; i < LoadedCheatTotal; i++)
/*TODO*///		{
/*TODO*///		if ((NoCheat == i) || (NoCheat == -1))
/*TODO*///		  {
/*TODO*///			int addmore = (LoadedCheatTable[i].More[0]);
/*TODO*///
/*TODO*///				/* form fmt string, adjusting length of address field for cpu address range */
/*TODO*///				sprintf(fmt, "%%s:%%d:%s:%%02X:%%03d:%%s%s\n",
/*TODO*///						FormatAddr(LoadedCheatTable[i].CpuNo,0),
/*TODO*///					  (addmore ? ":%s" : ""));
/*TODO*///
/*TODO*///				#ifdef macintosh
/*TODO*///				if (uncompressed)
/*TODO*///					fprintf(f, "\r");	  /* force DOS-style line enders */
/*TODO*///				else
/*TODO*///					gzprintf(gz, "\r"); /* force DOS-style line enders */
/*TODO*///				#endif
/*TODO*///
/*TODO*///				/* JCK 990717 BEGIN */
/*TODO*///				if (	(LoadedCheatTable[LoadedCheatTotal].Special>=60)	&&
/*TODO*///					(LoadedCheatTable[LoadedCheatTotal].Special<=75)	)
/*TODO*///				{
/*TODO*///					if (uncompressed)
/*TODO*///					{
/*TODO*///						if (fprintf(f, fmt, Machine->gamedrv->name,
/*TODO*///									LoadedCheatTable[i].CpuNo,
/*TODO*///									LoadedCheatTable[i].Address,
/*TODO*///									LoadedCheatTable[i].Maximum,
/*TODO*///									LoadedCheatTable[i].Special,
/*TODO*///									LoadedCheatTable[i].Name,
/*TODO*///									(addmore ? LoadedCheatTable[i].More : "")))
/*TODO*///							count ++;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						if (gzprintf(gz, fmt, Machine->gamedrv->name,
/*TODO*///									 LoadedCheatTable[i].CpuNo,
/*TODO*///									 LoadedCheatTable[i].Address,
/*TODO*///									 LoadedCheatTable[i].Maximum,
/*TODO*///									 LoadedCheatTable[i].Special,
/*TODO*///									 LoadedCheatTable[i].Name,
/*TODO*///									 (addmore ? LoadedCheatTable[i].More : "")))
/*TODO*///							count ++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if (uncompressed)
/*TODO*///					{
/*TODO*///						if (fprintf(f, fmt, Machine->gamedrv->name,
/*TODO*///									LoadedCheatTable[i].CpuNo,
/*TODO*///									LoadedCheatTable[i].Address,
/*TODO*///									LoadedCheatTable[i].Data,
/*TODO*///									LoadedCheatTable[i].Special,
/*TODO*///									LoadedCheatTable[i].Name,
/*TODO*///									(addmore ? LoadedCheatTable[i].More : "")))
/*TODO*///							count ++;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						if (gzprintf(gz, fmt, Machine->gamedrv->name,
/*TODO*///									 LoadedCheatTable[i].CpuNo,
/*TODO*///									 LoadedCheatTable[i].Address,
/*TODO*///									 LoadedCheatTable[i].Data,
/*TODO*///									 LoadedCheatTable[i].Special,
/*TODO*///									 LoadedCheatTable[i].Name,
/*TODO*///									 (addmore ? LoadedCheatTable[i].More : "")))
/*TODO*///							count ++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				/* JCK 990717 END */
/*TODO*///
/*TODO*///			}
/*TODO*///		}
/*TODO*///		if (uncompressed)
/*TODO*///			fclose (f);
/*TODO*///		else
/*TODO*///			gzclose (gz);
/*TODO*///	}
/*TODO*///	return(count);
/*TODO*///}
/*TODO*///
/*TODO*////* Function who loads the cheats for a game from a single database */
/*TODO*///void LoadCheat(int merge, char *filename)
/*TODO*///{
/*TODO*///  gzFile *f;
/*TODO*///  char *ptr;
/*TODO*///  char str[90];    /* To support the add. description */
/*TODO*///
/*TODO*///  int yPos;
/*TODO*///
/*TODO*///  if (!merge)
/*TODO*///  {
/*TODO*///	ActiveCheatTotal = 0;
/*TODO*///	LoadedCheatTotal = 0;
/*TODO*///  }
/*TODO*///
/*TODO*///  /* Load the cheats for that game */
/*TODO*///  /* Ex.: pacman:0:4E14:06:000:1UP Unlimited lives:Coded on 1 byte */
/*TODO*///  if ((f = gzopen(filename,"r")) != 0)
/*TODO*///  {
/*TODO*///	yPos = (MachHeight - FontHeight) / 2 - FontHeight;
/*TODO*////*	xprintf(0, 0, yPos, "Loading cheats from file"); */
/*TODO*///	  yPos += FontHeight;
/*TODO*////*	xprintf(0, 0, yPos, "%s...",filename); */
/*TODO*///
/*TODO*///	for(;;)
/*TODO*///	{
/*TODO*///		  if (gzgets(f,str,90) == NULL)
/*TODO*///		break;
/*TODO*///
/*TODO*///		#ifdef macintosh  /* JB 971004 */
/*TODO*///		/* remove extraneous LF on Macs if it exists */
/*TODO*///		if ( str[0] == '\r' )
/*TODO*///			strcpy( str, &str[1] );
/*TODO*///		#endif
/*TODO*///
/*TODO*///		if (str[strlen(Machine->gamedrv->name)] != ':')
/*TODO*///			continue;
/*TODO*///		if (strncmp(str,Machine->gamedrv->name,strlen(Machine->gamedrv->name)) != 0)
/*TODO*///			continue;
/*TODO*///		if (str[0] == ';') /* Comments line */
/*TODO*///			continue;
/*TODO*///
/*TODO*///		if (LoadedCheatTotal >= MAX_LOADEDCHEATS)
/*TODO*///		{
/*TODO*///			break;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (sologame)
/*TODO*///			if (	(strstr(str, "2UP") != NULL) || (strstr(str, "PL2") != NULL)	||
/*TODO*///				(strstr(str, "3UP") != NULL) || (strstr(str, "PL3") != NULL)	||
/*TODO*///				(strstr(str, "4UP") != NULL) || (strstr(str, "PL4") != NULL)	||
/*TODO*///				(strstr(str, "2up") != NULL) || (strstr(str, "pl2") != NULL)	||
/*TODO*///				(strstr(str, "3up") != NULL) || (strstr(str, "pl3") != NULL)	||
/*TODO*///				(strstr(str, "4up") != NULL) || (strstr(str, "pl4") != NULL)	)
/*TODO*///			  continue;
/*TODO*///
/*TODO*///		/* Reset the counter */
/*TODO*///		LoadedCheatTable[LoadedCheatTotal].Count=0;
/*TODO*///
/*TODO*///		/* Extract the fields from the string */
/*TODO*///		ptr = strtok(str, ":");
/*TODO*///		if ( ! ptr )
/*TODO*///			continue;
/*TODO*///
/*TODO*///		ptr = strtok(NULL, ":");
/*TODO*///		if ( ! ptr )
/*TODO*///			continue;
/*TODO*///		sscanf(ptr,"%d", &LoadedCheatTable[LoadedCheatTotal].CpuNo);
/*TODO*///
/*TODO*///		ptr = strtok(NULL, ":");
/*TODO*///		if ( ! ptr )
/*TODO*///			continue;
/*TODO*///		sscanf(ptr,"%X", &LoadedCheatTable[LoadedCheatTotal].Address);
/*TODO*///
/*TODO*///		ptr = strtok(NULL, ":");
/*TODO*///		if ( ! ptr )
/*TODO*///			continue;
/*TODO*///		sscanf(ptr,"%x", &LoadedCheatTable[LoadedCheatTotal].Data);
/*TODO*///
/*TODO*///		ptr = strtok(NULL, ":");
/*TODO*///		if ( ! ptr )
/*TODO*///			continue;
/*TODO*///		sscanf(ptr,"%d", &LoadedCheatTable[LoadedCheatTotal].Special);
/*TODO*///
/*TODO*///		ptr = strtok(NULL, ":");
/*TODO*///		if ( ! ptr )
/*TODO*///			continue;
/*TODO*///		strcpy(LoadedCheatTable[LoadedCheatTotal].Name,ptr);
/*TODO*///
/*TODO*///		strcpy(LoadedCheatTable[LoadedCheatTotal].More,"\n");
/*TODO*///		ptr = strtok(NULL, ":");
/*TODO*///		if (ptr)
/*TODO*///			strcpy(LoadedCheatTable[LoadedCheatTotal].More,ptr);
/*TODO*///		if (strstr(LoadedCheatTable[LoadedCheatTotal].Name,"\n") != NULL)
/*TODO*///			LoadedCheatTable[LoadedCheatTotal].Name[strlen(LoadedCheatTable[LoadedCheatTotal].Name)-1] = 0;
/*TODO*///		if (strstr(LoadedCheatTable[LoadedCheatTotal].More,"\n") != NULL)
/*TODO*///			LoadedCheatTable[LoadedCheatTotal].More[strlen(LoadedCheatTable[LoadedCheatTotal].More)-1] = 0;
/*TODO*///
/*TODO*///		/* Fill the new fields : Minimum and Maximum */
/*TODO*///		if (	(LoadedCheatTable[LoadedCheatTotal].Special==62)	||
/*TODO*///			(LoadedCheatTable[LoadedCheatTotal].Special==65)	||
/*TODO*///			(LoadedCheatTable[LoadedCheatTotal].Special==72)	||
/*TODO*///			(LoadedCheatTable[LoadedCheatTotal].Special==75)	)
/*TODO*///			LoadedCheatTable[LoadedCheatTotal].Minimum = 1;
/*TODO*///		else
/*TODO*///			LoadedCheatTable[LoadedCheatTotal].Minimum = 0;
/*TODO*///		if (	(LoadedCheatTable[LoadedCheatTotal].Special>=60)	&&
/*TODO*///			(LoadedCheatTable[LoadedCheatTotal].Special<=75)	)
/*TODO*///		{
/*TODO*///			LoadedCheatTable[LoadedCheatTotal].Maximum = LoadedCheatTable[LoadedCheatTotal].Data;
/*TODO*///			LoadedCheatTable[LoadedCheatTotal].Data = 0;
/*TODO*///		}
/*TODO*///		else
/*TODO*///			LoadedCheatTable[LoadedCheatTotal].Maximum = 0xFF;
/*TODO*///
/*TODO*///		if (LoadedCheatTable[LoadedCheatTotal].Special == COMMENTCHEAT)
/*TODO*///		{
/*TODO*///			LoadedCheatTable[LoadedCheatTotal].Address = 0;
/*TODO*///			LoadedCheatTable[LoadedCheatTotal].Data = 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (LoadedCheatTable[LoadedCheatTotal].Special == WATCHCHEAT)
/*TODO*///		{
/*TODO*///			LoadedCheatTable[LoadedCheatTotal].Data = 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		LoadedCheatTotal++;
/*TODO*///	}
/*TODO*///	gzclose(f);
/*TODO*///
/*TODO*///	ClearArea (1, yPos - FontHeight, yPos + FontHeight);
/*TODO*///  }
/*TODO*///}
/*TODO*///
/*TODO*////* Function who loads the cheats for a game from many databases */
/*TODO*///void LoadDatabases(int InCheat)
/*TODO*///{
/*TODO*///  char *ptr;
/*TODO*///  char str[CHEAT_FILENAME_MAXLEN+1];
/*TODO*///  char filename[CHEAT_FILENAME_MAXLEN+1];
/*TODO*///
/*TODO*///  int pos1, pos2;
/*TODO*///
/*TODO*///  if (InCheat)
/*TODO*///	cheat_clearbitmap();
/*TODO*///  else
/*TODO*///  {
/*TODO*///	cheat_save_frameskips();
/*TODO*///	osd_clearbitmap(Machine->scrbitmap);
/*TODO*///  }
/*TODO*///
/*TODO*///  ActiveCheatTotal = 0;
/*TODO*///  LoadedCheatTotal = 0;
/*TODO*///
/*TODO*///  strcpy(str, cheatfile);
/*TODO*///  ptr = strtok(str, ";");
/*TODO*///  strcpy(database, ptr);
/*TODO*///  strcpy(str, cheatfile);
/*TODO*///  str[strlen(str) + 1] = 0;
/*TODO*///  pos1 = 0;
/*TODO*///  while (str[pos1])
/*TODO*///  {
/*TODO*///	pos2 = pos1;
/*TODO*///	while ((str[pos2]) && (str[pos2] != ';'))
/*TODO*///		pos2++;
/*TODO*///	if (pos1 != pos2)
/*TODO*///	{
/*TODO*///		memset (filename, '\0', sizeof(filename));
/*TODO*///		strncpy(filename, &str[pos1], (pos2 - pos1));
/*TODO*///		LoadCheat(1, filename);
/*TODO*///		pos1 = pos2 + 1;
/*TODO*///	}
/*TODO*///  }
/*TODO*///
/*TODO*///  if (!InCheat)
/*TODO*///  {
/*TODO*///	cheat_rest_frameskips();
/*TODO*///  }
/*TODO*///
/*TODO*///}
/*TODO*///
/*TODO*///int LoadHelp(char *filename, struct TextLine *table)
/*TODO*///{
/*TODO*///  gzFile *f;
/*TODO*///  char str[32];
/*TODO*///
/*TODO*///  int yPos;
/*TODO*///  int LineNumber = 0;
/*TODO*///  int size;
/*TODO*///
/*TODO*///  struct TextLine *txt;
/*TODO*///
/*TODO*///  yPos = (MachHeight - FontHeight) / 2;
/*TODO*///  xprintf(0, 0, yPos, "Loading help...");
/*TODO*///
/*TODO*///  if ((f = gzopen(filename,"r")) != 0)
/*TODO*///  {
/*TODO*///	txt = table;
/*TODO*///	for(;;)
/*TODO*///	{
/*TODO*///		  if (gzgets(f,str,32) == NULL)
/*TODO*///		break;
/*TODO*///
/*TODO*///		#ifdef macintosh  /* JB 971004 */
/*TODO*///		/* remove extraneous LF on Macs if it exists */
/*TODO*///		if ( str[0] == '\r' )
/*TODO*///			strcpy( str, &str[1] );
/*TODO*///		#endif
/*TODO*///
/*TODO*///		if (str[0] == ';') /* Comments line */
/*TODO*///			continue;
/*TODO*///
/*TODO*///		str[strlen(str) - 1] = 0;
/*TODO*///
/*TODO*///		size = sizeof(str);
/*TODO*///		txt->data = malloc (size + 1);
/*TODO*///		if (txt->data == NULL)
/*TODO*///		break;
/*TODO*///
/*TODO*///		memset (txt->data, '\0', size + 1);
/*TODO*///		memcpy (txt->data, str, size);
/*TODO*///		txt->number = LineNumber++;
/*TODO*///
/*TODO*///		txt++;
/*TODO*///	}
/*TODO*///	gzclose(f);
/*TODO*///  }
/*TODO*///
/*TODO*///  ClearTextLine (1, yPos);
/*TODO*///
/*TODO*///  return(LineNumber);
/*TODO*///}
/*TODO*///
/*TODO*///void InitMemoryAreas(void)
/*TODO*///{
/*TODO*///	const struct MemoryWriteAddress *mwa = Machine->drv->cpu[SearchCpuNo].memory_write;
/*TODO*///	char buffer[40];
/*TODO*///
/*TODO*///	MemoryAreasSelected = 0;
/*TODO*///	  MemoryAreasTotal = 0;
/*TODO*///	while (mwa->start != -1)
/*TODO*///	{
/*TODO*///		sprintf (buffer, FormatAddr(SearchCpuNo,0), mwa->start);
/*TODO*///		strcpy (MemToScanTable[MemoryAreasTotal].Name, buffer);
/*TODO*///		strcat (MemToScanTable[MemoryAreasTotal].Name," -> ");
/*TODO*///		sprintf (buffer, FormatAddr(SearchCpuNo,0), mwa->end);
/*TODO*///		strcat (MemToScanTable[MemoryAreasTotal].Name, buffer);
/*TODO*///		MemToScanTable[MemoryAreasTotal].handler = mwa->handler;
/*TODO*///		MemToScanTable[MemoryAreasTotal].Enabled = 0;
/*TODO*///		MemoryAreasTotal++;
/*TODO*///		mwa++;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* Init some variables */
/*TODO*///void InitCheat(void)
/*TODO*///{
/*TODO*///  int i;
/*TODO*///
/*TODO*///  he_did_cheat = 0;
/*TODO*///  CheatEnabled = 0;
/*TODO*///
/*TODO*///  WatchEnabled = 0;
/*TODO*///
/*TODO*///  CurrentMethod = 0;
/*TODO*///  SaveMethod	= 0;
/*TODO*///
/*TODO*///  SearchCpuNoOld	= -1;
/*TODO*///  MallocFailure = -1;
/*TODO*///
/*TODO*///  MachHeight = ( Machine -> uiheight );
/*TODO*///  MachWidth  = ( Machine -> uiwidth );
/*TODO*///
/*TODO*///  FontHeight = ( Machine -> uifontheight );
/*TODO*///  FontWidth  = ( Machine -> uifontwidth );
/*TODO*///
/*TODO*///  ManyCpus	 = ( cpu_gettotalcpu() > 1 );
/*TODO*///
/*TODO*///  SaveMenu = 0;
/*TODO*///  SaveStartSearch = 0;
/*TODO*///  SaveContinueSearch = 0;
/*TODO*///
/*TODO*///  SaveIndex = 0;
/*TODO*///
/*TODO*///  reset_table (StartRam);
/*TODO*///  reset_table (BackupRam);
/*TODO*///  reset_table (FlagTable);
/*TODO*///
/*TODO*///  reset_table (OldBackupRam);
/*TODO*///  reset_table (OldFlagTable);
/*TODO*///
/*TODO*///  MatchFound = 0;
/*TODO*///  OldMatchFound = 0;
/*TODO*///
/*TODO*///  RestoreStatus = RESTORE_NOINIT;
/*TODO*///
/*TODO*///  for (i = 0;i < MAX_WATCHES;i ++)
/*TODO*///  {
/*TODO*///	WatchesCpuNo[ i ] = 0;
/*TODO*///	Watches[ i ] = MAX_ADDRESS(WatchesCpuNo[ i ]);
/*TODO*///  }
/*TODO*///
/*TODO*///  WatchX = Machine->uixmin;
/*TODO*///  WatchY = Machine->uiymin;
/*TODO*///  WatchesFlag = 0;
/*TODO*///
/*TODO*///  WatchGfxLen = 0;
/*TODO*///  WatchGfxPos = 0;
/*TODO*///
/*TODO*///  LoadDatabases(0);
/*TODO*///
/*TODO*///  RebuildTables = 0;
/*TODO*///  SearchCpuNo = 0;
/*TODO*///  InitMemoryAreas();
/*TODO*///
/*TODO*///}
/*TODO*///
/*TODO*///void DisplayActiveCheats(int y)
/*TODO*///{
/*TODO*///  int i;
/*TODO*///
/*TODO*///  xprintf(0, 0, y, "Active Cheats: %d", ActiveCheatTotal);
/*TODO*///  y += ( FontHeight * 3 / 2 );
/*TODO*///
/*TODO*///  if (ActiveCheatTotal == 0)
/*TODO*///	xprintf(0, 0, y, "--- None ---");
/*TODO*///
/*TODO*///  for (i = 0;i < ActiveCheatTotal;i ++)
/*TODO*///  {
/*TODO*///	if (y > MachHeight - 3 * FontHeight)
/*TODO*///	{
/*TODO*///		xprintf(0, 0, y, downarrow);
/*TODO*///		break;
/*TODO*///	}
/*TODO*///	xprintf(0, 0 ,y, "%-30s", ActiveCheatTable[i].Name);
/*TODO*///	y += FontHeight;
/*TODO*///  }
/*TODO*///}
/*TODO*///
/*TODO*////* Display watches if there are some */
/*TODO*///void DisplayWatches(int ClrScr, int *x,int *y,char *buffer,
/*TODO*///				int highlight, int dx, int dy)
/*TODO*///{
/*TODO*///	  int i;
/*TODO*///	  char bufadr[4];
/*TODO*///
/*TODO*///	  int FirstWatch = 1;
/*TODO*///
/*TODO*///	int WatchColor = DT_COLOR_WHITE;
/*TODO*///
/*TODO*///	int trueorientation;
/*TODO*///
/*TODO*///	/* hack: force the display into standard orientation */
/*TODO*///	trueorientation = Machine->orientation;
/*TODO*///	Machine->orientation = Machine->ui_orientation;
/*TODO*///
/*TODO*///	if (ClrScr)
/*TODO*///	{
/*TODO*///		WatchGfxPos = 0;
/*TODO*///		for (i = 0;i < (int)strlen(buffer);i ++)
/*TODO*///		{
/*TODO*///			if ((buffer[i] == '+') || (buffer[i] == '-'))
/*TODO*///				continue;
/*TODO*///
/*TODO*///			drawgfx(Machine->scrbitmap,Machine->uifont,' ',DT_COLOR_WHITE,0,0,
/*TODO*///				*x+dx+(WatchGfxPos),*y+dy,0,TRANSPARENCY_NONE,0);
/*TODO*///
/*TODO*///			if (buffer[i] == ' ')
/*TODO*///				WatchGfxPos += (FontWidth / 2);
/*TODO*///			else
/*TODO*///				WatchGfxPos += (FontWidth);
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	if ((WatchesFlag != 0) && (WatchEnabled != 0))
/*TODO*///	{
/*TODO*///		WatchGfxLen = 0;
/*TODO*///		buffer[0] = 0;
/*TODO*///		for (i = 0;i < MAX_WATCHES;i ++)
/*TODO*///		{
/*TODO*///			if ( Watches[i] != MAX_ADDRESS(WatchesCpuNo[i]))
/*TODO*///			{
/*TODO*///			if (!FirstWatch)	/* If not 1st watch add a space */
/*TODO*///			{
/*TODO*///				strcat(buffer," ");
/*TODO*///				WatchGfxLen += (FontWidth / 2);
/*TODO*///			}
/*TODO*///				sprintf(bufadr,"%02X", RD_GAMERAM (WatchesCpuNo[i], Watches[i]));
/*TODO*///
/*TODO*///				if (highlight == i)
/*TODO*///					strcat(buffer,"+");
/*TODO*///				strcat(buffer,bufadr);
/*TODO*///				if (highlight == i)
/*TODO*///					strcat(buffer,"-");
/*TODO*///
/*TODO*///				WatchGfxLen += (FontWidth * 2);
/*TODO*///				FirstWatch = 0;
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		/* Adjust x offset to fit the screen */
/*TODO*///		/* while (	(*x >= (MachWidth - (FontWidth * (int)strlen(buffer)))) && */
/*TODO*///		while ( (*x >= (MachWidth - WatchGfxLen)) &&
/*TODO*///			(*x > Machine->uixmin)	)
/*TODO*///		(*x)--;
/*TODO*///
/*TODO*///		WatchGfxPos = 0;
/*TODO*///		for (i = 0;i < (int)strlen(buffer);i ++)
/*TODO*///		{
/*TODO*///			if (buffer[i] == '+')
/*TODO*///			{
/*TODO*///				WatchColor = DT_COLOR_YELLOW;
/*TODO*///				continue;
/*TODO*///			}
/*TODO*///			if (buffer[i] == '-')
/*TODO*///			{
/*TODO*///				WatchColor = DT_COLOR_WHITE;
/*TODO*///				continue;
/*TODO*///			}
/*TODO*///
/*TODO*///			drawgfx(Machine->scrbitmap,Machine->uifont,buffer[i],WatchColor,0,0,
/*TODO*///				*x+(WatchGfxPos),*y,0,TRANSPARENCY_NONE,0);
/*TODO*///			/*	*x+(i*FontWidth),*y,0,TRANSPARENCY_NONE,0); */
/*TODO*///
/*TODO*///			if (buffer[i] == ' ')
/*TODO*///				WatchGfxPos += (FontWidth / 2);
/*TODO*///			else
/*TODO*///				WatchGfxPos += (FontWidth);
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	Machine->orientation = trueorientation;
/*TODO*///}
/*TODO*///
/*TODO*////* copy one cheat structure to another */
/*TODO*///void set_cheat(struct cheat_struct *dest, struct cheat_struct *src)
/*TODO*///{
/*TODO*///	/* Changed order to match structure - Added field More */
/*TODO*///	struct cheat_struct new_cheat =
/*TODO*///	{
/*TODO*///		0,				/* CpuNo */
/*TODO*///		0,				/* Address */
/*TODO*///		0,				/* Data */
/*TODO*///		0,				/* Special */
/*TODO*///		0,				/* Count */
/*TODO*///		0,				/* Backup */
/*TODO*///		0,				/* Minimum */
/*TODO*///		0xFF,				/* Maximum */
/*TODO*///		"---- New Cheat ----",	/* Name */
/*TODO*///		""				/* More */
/*TODO*///	};
/*TODO*///
/*TODO*///	if (src == NEW_CHEAT)
/*TODO*///	{
/*TODO*///		src = &new_cheat;
/*TODO*///	}
/*TODO*///
/*TODO*///	dest->CpuNo = src->CpuNo;
/*TODO*///	dest->Address	= src->Address;
/*TODO*///	dest->Data		= src->Data;
/*TODO*///	dest->Special	= src->Special;
/*TODO*///	dest->Count = src->Count;
/*TODO*///	dest->Backup	= src->Backup;
/*TODO*///	dest->Minimum	= src->Minimum;
/*TODO*///	dest->Maximum	= src->Maximum;
/*TODO*///	strcpy(dest->Name, src->Name);
/*TODO*///	strcpy(dest->More, src->More);
/*TODO*///}
/*TODO*///
/*TODO*///void DeleteActiveCheatFromTable(int NoCheat)
/*TODO*///{
/*TODO*///  int i;
/*TODO*///  if ((NoCheat > ActiveCheatTotal) || (NoCheat < 0))
/*TODO*///	return;
/*TODO*///  for (i = NoCheat; i < ActiveCheatTotal-1;i ++)
/*TODO*///  {
/*TODO*///	set_cheat(&ActiveCheatTable[i], &ActiveCheatTable[i + 1]);
/*TODO*///  }
/*TODO*///  ActiveCheatTotal --;
/*TODO*///}
/*TODO*///
/*TODO*///void DeleteLoadedCheatFromTable(int NoCheat)
/*TODO*///{
/*TODO*///  int i;
/*TODO*///  if ((NoCheat > LoadedCheatTotal) || (NoCheat < 0))
/*TODO*///	return;
/*TODO*///  for (i = NoCheat; i < LoadedCheatTotal-1;i ++)
/*TODO*///  {
/*TODO*///	set_cheat(&LoadedCheatTable[i], &LoadedCheatTable[i + 1]);
/*TODO*///  }
/*TODO*///  LoadedCheatTotal --;
/*TODO*///}
/*TODO*///
/*TODO*///int FindFreeWatch(void)
/*TODO*///{
/*TODO*///  int i;
/*TODO*///  for (i = 0;i < MAX_WATCHES;i ++)
/*TODO*///  {
/*TODO*///	if (Watches[i] == MAX_ADDRESS(WatchesCpuNo[i]))
/*TODO*///		break;
/*TODO*///  }
/*TODO*///  return(i == MAX_WATCHES ? 0 : i+1);
/*TODO*///}
/*TODO*///
/*TODO*///int EditCheatHeader(void)
/*TODO*///{
/*TODO*///  int i = 0;
/*TODO*///  char *paDisplayText[] = {
/*TODO*///		"To edit a Cheat name, press",
/*TODO*///		"<ENTER> when name is selected.",
/*TODO*///		"To edit values or to select a",
/*TODO*///		"pre-defined Cheat Name, use :",
/*TODO*///		"<+> and Right arrow key: +1",
/*TODO*///		"<-> and Left  arrow key: -1",
/*TODO*///		"<1> ... <8>: +1 digit",
/*TODO*///		"",
/*TODO*///		"<F10>: Show Help + other keys",
/*TODO*///		0 };
/*TODO*///
/*TODO*///  struct DisplayText dt[20];
/*TODO*///
/*TODO*///  while (paDisplayText[i])
/*TODO*///  {
/*TODO*///	if (i)
/*TODO*///		dt[i].y = (dt[i - 1].y + FontHeight + 2);
/*TODO*///	else
/*TODO*///		dt[i].y = FIRSTPOS;
/*TODO*///	dt[i].color = DT_COLOR_WHITE;
/*TODO*///	dt[i].text = paDisplayText[i];
/*TODO*///	dt[i].x = (MachWidth - FontWidth * strlen(dt[i].text)) / 2;
/*TODO*///	if(dt[i].x > MachWidth)
/*TODO*///		dt[i].x = 0;
/*TODO*///	i++;
/*TODO*///  }
/*TODO*///  dt[i].text = 0; /* terminate array */
/*TODO*///  displaytext(dt,0,1);
/*TODO*///  return(dt[i-1].y + ( 3 * FontHeight ));
/*TODO*///}
/*TODO*///
/*TODO*///void EditCheat(int CheatNo)
/*TODO*///{
/*TODO*///  char *CheatNameList[] = {
/*TODO*///	"Infinite Lives PL1",
/*TODO*///	"Infinite Lives PL2",
/*TODO*///	"Infinite Time",
/*TODO*///	"Infinite Time PL1",
/*TODO*///	"Infinite Time PL2",
/*TODO*///	"Invincibility",
/*TODO*///	"Invincibility PL1",
/*TODO*///	"Invincibility PL2",
/*TODO*///	"Infinite Energy",
/*TODO*///	"Infinite Energy PL1",
/*TODO*///	"Infinite Energy PL2",
/*TODO*///	"Select Next Level",
/*TODO*///	"Select Current level",
/*TODO*///	"Infinite Ammo",
/*TODO*///	"Infinite Ammo PL1",
/*TODO*///	"Infinite Ammo PL2",
/*TODO*///	"Infinite Bombs",
/*TODO*///	"Infinite Bombs PL1",
/*TODO*///	"Infinite Bombs PL2",
/*TODO*///	"Select Score PL1",
/*TODO*///	"Select Score PL2",
/*TODO*///	"Drain all Energy Now! PL1",
/*TODO*///	"Drain all Energy Now! PL2",
/*TODO*///	"Infinite",
/*TODO*///	"Always have",
/*TODO*///	"Get",
/*TODO*///	"Lose",
/*TODO*///	"[                           ]",
/*TODO*///	"---> <ENTER> To Edit <---",
/*TODO*///	"\0" };
/*TODO*///
/*TODO*///  int i,s,y,key,done;
/*TODO*///  int total;
/*TODO*///  struct DisplayText dt[20];
/*TODO*///  char str2[6][40];
/*TODO*///  int CurrentName;
/*TODO*///  int EditYPos;
/*TODO*///
/*TODO*///  char buffer[10];
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///
/*TODO*///  y = EditCheatHeader();
/*TODO*///
/*TODO*///  total = 0;
/*TODO*///
/*TODO*///  if ((LoadedCheatTable[CheatNo].Special>=60) && (LoadedCheatTable[CheatNo].Special<=75))
/*TODO*///	LoadedCheatTable[CheatNo].Data = LoadedCheatTable[CheatNo].Maximum;
/*TODO*///
/*TODO*///  sprintf(str2[0],"Name: %s",LoadedCheatTable[CheatNo].Name);
/*TODO*///  if ((FontWidth * (int)strlen(str2[0])) > MachWidth - Machine->uixmin)
/*TODO*///	sprintf(str2[0],"%s",LoadedCheatTable[CheatNo].Name);
/*TODO*///  sprintf(str2[1],"CPU:        %01X",LoadedCheatTable[CheatNo].CpuNo);
/*TODO*///
/*TODO*///  sprintf(str2[2], FormatAddr(LoadedCheatTable[CheatNo].CpuNo,1),
/*TODO*///				LoadedCheatTable[CheatNo].Address);
/*TODO*///
/*TODO*///  sprintf(str2[3],"Value:    %03d  (0x%02X)",LoadedCheatTable[CheatNo].Data,LoadedCheatTable[CheatNo].Data);
/*TODO*///  sprintf(str2[4],"Type:     %03d",LoadedCheatTable[CheatNo].Special);
/*TODO*///
/*TODO*///  sprintf(str2[5],"More: %s",LoadedCheatTable[CheatNo].More);
/*TODO*///  if ((FontWidth * (int)strlen(str2[5])) > MachWidth - Machine->uixmin)
/*TODO*///	sprintf(str2[5],"%s",LoadedCheatTable[CheatNo].More);
/*TODO*///
/*TODO*///  for (i = 0;i < 6;i ++)
/*TODO*///  {
/*TODO*///	dt[total].text = str2[i];
/*TODO*///	dt[total].x = MachWidth / 2;
/*TODO*///	if(MachWidth < 35*FontWidth)
/*TODO*///		dt[total].x = 0;
/*TODO*///	else
/*TODO*///		dt[total].x -= 15*FontWidth;
/*TODO*///	dt[total].y = y;
/*TODO*///	dt[total].color = DT_COLOR_WHITE;
/*TODO*///	total++;
/*TODO*///	y += FontHeight;
/*TODO*///  }
/*TODO*///
/*TODO*///  dt[total].text = 0; /* terminate array */
/*TODO*///
/*TODO*///  EditYPos = ( y + ( 4 * FontHeight ) );
/*TODO*///
/*TODO*///  s = 0;
/*TODO*///  CurrentName = -1;
/*TODO*///
/*TODO*///  oldkey = 0;
/*TODO*///
/*TODO*///  done = 0;
/*TODO*///  do
/*TODO*///  {
/*TODO*///
/*TODO*///	for (i = 0;i < total;i++)
/*TODO*///		dt[i].color = (i == s) ? DT_COLOR_YELLOW : DT_COLOR_WHITE;
/*TODO*///	displaytext(dt,0,1);
/*TODO*///
/*TODO*///	/* key = keyboard_read_sync(); */
/*TODO*///	key = cheat_readkey();	  /* MSH 990217 */
/*TODO*///	switch (key)
/*TODO*///	{
/*TODO*///		case KEYCODE_DOWN:
/*TODO*///		case KEYCODE_2_PAD:
/*TODO*///			if (s < total - 1)
/*TODO*///				s++;
/*TODO*///			else
/*TODO*///				s = 0;
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_UP:
/*TODO*///		case KEYCODE_8_PAD:
/*TODO*///			if (s > 0)
/*TODO*///				s--;
/*TODO*///			else
/*TODO*///				s = total - 1;
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_LEFT:
/*TODO*///		case KEYCODE_4_PAD:
/*TODO*///			switch (s)
/*TODO*///			{
/*TODO*///				case 0:    /* Name */
/*TODO*///
/*TODO*///					if (CurrentName < 0)
/*TODO*///						CurrentName = 0;
/*TODO*///
/*TODO*///					if (CurrentName == 0)						/* wrap if necessary*/
/*TODO*///						while (CheatNameList[CurrentName][0])
/*TODO*///							CurrentName++;
/*TODO*///					CurrentName--;
/*TODO*///					strcpy (LoadedCheatTable[CheatNo].Name, CheatNameList[CurrentName]);
/*TODO*///					sprintf (str2[0],"Name: %s", LoadedCheatTable[CheatNo].Name);
/*TODO*///					ClearTextLine(1, dt[0].y);
/*TODO*///					break;
/*TODO*///				case 1:    /* CpuNo */
/*TODO*///					if (ManyCpus)
/*TODO*///					{
/*TODO*///						if (LoadedCheatTable[CheatNo].CpuNo == 0)
/*TODO*///							LoadedCheatTable[CheatNo].CpuNo = cpu_gettotalcpu() - 1;
/*TODO*///						else
/*TODO*///							LoadedCheatTable[CheatNo].CpuNo --;
/*TODO*///						sprintf (str2[1], "CPU:        %01X", LoadedCheatTable[CheatNo].CpuNo);
/*TODO*///					}
/*TODO*///					break;
/*TODO*///				case 2:    /* Address */
/*TODO*///					if (LoadedCheatTable[CheatNo].Address == 0)
/*TODO*///						LoadedCheatTable[CheatNo].Address = MAX_ADDRESS(LoadedCheatTable[CheatNo].CpuNo);
/*TODO*///					else
/*TODO*///						LoadedCheatTable[CheatNo].Address --;
/*TODO*///					sprintf(str2[2], FormatAddr(LoadedCheatTable[CheatNo].CpuNo,1),
/*TODO*///						LoadedCheatTable[CheatNo].Address);
/*TODO*///					break;
/*TODO*///				case 3:    /* Data */
/*TODO*///					if (LoadedCheatTable[CheatNo].Data == 0)
/*TODO*///						LoadedCheatTable[CheatNo].Data = 0xFF;
/*TODO*///					else
/*TODO*///						LoadedCheatTable[CheatNo].Data --;
/*TODO*///					sprintf(str2[3], "Value:    %03d  (0x%02X)", LoadedCheatTable[CheatNo].Data,
/*TODO*///						LoadedCheatTable[CheatNo].Data);
/*TODO*///					break;
/*TODO*///				case 4:    /* Special */
/*TODO*///					if (LoadedCheatTable[CheatNo].Special <= 0)
/*TODO*///						LoadedCheatTable[CheatNo].Special = TOTAL_CHEAT_TYPES + OFFSET_LINK_CHEAT;
/*TODO*///					else
/*TODO*///					switch (LoadedCheatTable[CheatNo].Special)
/*TODO*///						{
/*TODO*///							case 20:
/*TODO*///								LoadedCheatTable[CheatNo].Special = 11;
/*TODO*///								break;
/*TODO*///							case 40:
/*TODO*///								LoadedCheatTable[CheatNo].Special = 24;
/*TODO*///								break;
/*TODO*///							case 60:
/*TODO*///								LoadedCheatTable[CheatNo].Special = 44;
/*TODO*///								break;
/*TODO*///							case 70:
/*TODO*///								LoadedCheatTable[CheatNo].Special = 65;
/*TODO*///								break;
/*TODO*///							case OFFSET_LINK_CHEAT:
/*TODO*///								LoadedCheatTable[CheatNo].Special = 75;
/*TODO*///								break;
/*TODO*///							case 20 + OFFSET_LINK_CHEAT:
/*TODO*///								LoadedCheatTable[CheatNo].Special = 11 + OFFSET_LINK_CHEAT;
/*TODO*///								break;
/*TODO*///							case 40 + OFFSET_LINK_CHEAT:
/*TODO*///								LoadedCheatTable[CheatNo].Special = 24 + OFFSET_LINK_CHEAT;
/*TODO*///								break;
/*TODO*///							case 60 + OFFSET_LINK_CHEAT:
/*TODO*///								LoadedCheatTable[CheatNo].Special = 44 + OFFSET_LINK_CHEAT;
/*TODO*///								break;
/*TODO*///							case 70 + OFFSET_LINK_CHEAT:
/*TODO*///								LoadedCheatTable[CheatNo].Special = 65 + OFFSET_LINK_CHEAT;
/*TODO*///								break;
/*TODO*///							default:
/*TODO*///								LoadedCheatTable[CheatNo].Special --;
/*TODO*///								break;
/*TODO*///						}
/*TODO*///
/*TODO*///					sprintf(str2[4],"Type:     %03d",LoadedCheatTable[CheatNo].Special);
/*TODO*///					break;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_RIGHT:
/*TODO*///		case KEYCODE_6_PAD:
/*TODO*///			switch (s)
/*TODO*///			{
/*TODO*///				case 0:    /* Name */
/*TODO*///					CurrentName ++;
/*TODO*///					if (CheatNameList[CurrentName][0] == 0)
/*TODO*///						CurrentName = 0;
/*TODO*///					strcpy (LoadedCheatTable[CheatNo].Name, CheatNameList[CurrentName]);
/*TODO*///					sprintf (str2[0], "Name: %s", LoadedCheatTable[CheatNo].Name);
/*TODO*///					ClearTextLine(1, dt[0].y);
/*TODO*///					break;
/*TODO*///				case 1:    /* CpuNo */
/*TODO*///					if (ManyCpus)
/*TODO*///					{
/*TODO*///						LoadedCheatTable[CheatNo].CpuNo ++;
/*TODO*///						if (LoadedCheatTable[CheatNo].CpuNo >= cpu_gettotalcpu())
/*TODO*///							LoadedCheatTable[CheatNo].CpuNo = 0;
/*TODO*///						sprintf(str2[1],"CPU:        %01X",LoadedCheatTable[CheatNo].CpuNo);
/*TODO*///					}
/*TODO*///					break;
/*TODO*///				case 2:    /* Address */
/*TODO*///					LoadedCheatTable[CheatNo].Address ++;
/*TODO*///					if (LoadedCheatTable[CheatNo].Address > MAX_ADDRESS(LoadedCheatTable[CheatNo].CpuNo))
/*TODO*///						LoadedCheatTable[CheatNo].Address = 0;
/*TODO*///					sprintf (str2[2], FormatAddr(LoadedCheatTable[CheatNo].CpuNo,1),
/*TODO*///						LoadedCheatTable[CheatNo].Address);
/*TODO*///					break;
/*TODO*///				case 3:    /* Data */
/*TODO*///					if(LoadedCheatTable[CheatNo].Data == 0xFF)
/*TODO*///						LoadedCheatTable[CheatNo].Data = 0;
/*TODO*///					else
/*TODO*///						LoadedCheatTable[CheatNo].Data ++;
/*TODO*///					sprintf(str2[3],"Value:    %03d  (0x%02X)",LoadedCheatTable[CheatNo].Data,
/*TODO*///						LoadedCheatTable[CheatNo].Data);
/*TODO*///					break;
/*TODO*///				case 4:    /* Special */
/*TODO*///					if (LoadedCheatTable[CheatNo].Special >= TOTAL_CHEAT_TYPES + OFFSET_LINK_CHEAT)
/*TODO*///						LoadedCheatTable[CheatNo].Special = 0;
/*TODO*///					else
/*TODO*///					switch (LoadedCheatTable[CheatNo].Special)
/*TODO*///						{
/*TODO*///							case 11:
/*TODO*///								LoadedCheatTable[CheatNo].Special = 20;
/*TODO*///								break;
/*TODO*///							case 24:
/*TODO*///								LoadedCheatTable[CheatNo].Special = 40;
/*TODO*///								break;
/*TODO*///							case 44:
/*TODO*///								LoadedCheatTable[CheatNo].Special = 60;
/*TODO*///								break;
/*TODO*///							case 65:
/*TODO*///								LoadedCheatTable[CheatNo].Special = 70;
/*TODO*///								break;
/*TODO*///							case 75:
/*TODO*///								LoadedCheatTable[CheatNo].Special = OFFSET_LINK_CHEAT;
/*TODO*///								break;
/*TODO*///							case 11 + OFFSET_LINK_CHEAT:
/*TODO*///								LoadedCheatTable[CheatNo].Special = 20 + OFFSET_LINK_CHEAT;
/*TODO*///								break;
/*TODO*///							case 24 + OFFSET_LINK_CHEAT:
/*TODO*///								LoadedCheatTable[CheatNo].Special = 40 + OFFSET_LINK_CHEAT;
/*TODO*///								break;
/*TODO*///							case 44 + OFFSET_LINK_CHEAT:
/*TODO*///								LoadedCheatTable[CheatNo].Special = 60 + OFFSET_LINK_CHEAT;
/*TODO*///								break;
/*TODO*///							case 65 + OFFSET_LINK_CHEAT:
/*TODO*///								LoadedCheatTable[CheatNo].Special = 70 + OFFSET_LINK_CHEAT;
/*TODO*///								break;
/*TODO*///							default:
/*TODO*///								LoadedCheatTable[CheatNo].Special ++;
/*TODO*///								break;
/*TODO*///						}
/*TODO*///
/*TODO*///					sprintf(str2[4],"Type:     %03d",LoadedCheatTable[CheatNo].Special);
/*TODO*///					break;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_HOME:
/*TODO*///		case KEYCODE_7_PAD:
/*TODO*///			switch (s)
/*TODO*///			{
/*TODO*///				case 3: /* Data */
/*TODO*///					if (LoadedCheatTable[CheatNo].Data >= 0x80)
/*TODO*///						LoadedCheatTable[CheatNo].Data -= 0x80;
/*TODO*///					sprintf(str2[3], "Value:    %03d  (0x%02X)", LoadedCheatTable[CheatNo].Data,
/*TODO*///						LoadedCheatTable[CheatNo].Data);
/*TODO*///					break;
/*TODO*///				case 4: /* Special */
/*TODO*///					if (LoadedCheatTable[CheatNo].Special >= OFFSET_LINK_CHEAT)
/*TODO*///						LoadedCheatTable[CheatNo].Special -= OFFSET_LINK_CHEAT;
/*TODO*///					sprintf(str2[4],"Type:     %03d",LoadedCheatTable[CheatNo].Special);
/*TODO*///					break;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_END:
/*TODO*///		case KEYCODE_1_PAD:
/*TODO*///			switch (s)
/*TODO*///			{
/*TODO*///				case 3: /* Data */
/*TODO*///					if (LoadedCheatTable[CheatNo].Data < 0x80)
/*TODO*///						LoadedCheatTable[CheatNo].Data += 0x80;
/*TODO*///					sprintf(str2[3], "Value:    %03d  (0x%02X)", LoadedCheatTable[CheatNo].Data,
/*TODO*///						LoadedCheatTable[CheatNo].Data);
/*TODO*///					break;
/*TODO*///				case 4: /* Special */
/*TODO*///					if (LoadedCheatTable[CheatNo].Special < OFFSET_LINK_CHEAT)
/*TODO*///						LoadedCheatTable[CheatNo].Special += OFFSET_LINK_CHEAT;
/*TODO*///					sprintf(str2[4],"Type:     %03d",LoadedCheatTable[CheatNo].Special);
/*TODO*///					break;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_8:
/*TODO*///			if (ADDRESS_BITS(LoadedCheatTable[CheatNo].CpuNo) < 29) break;
/*TODO*///		case KEYCODE_7:
/*TODO*///			if (ADDRESS_BITS(LoadedCheatTable[CheatNo].CpuNo) < 25) break;
/*TODO*///		case KEYCODE_6:
/*TODO*///			if (ADDRESS_BITS(LoadedCheatTable[CheatNo].CpuNo) < 21) break;
/*TODO*///		case KEYCODE_5:
/*TODO*///			if (ADDRESS_BITS(LoadedCheatTable[CheatNo].CpuNo) < 17) break;
/*TODO*///		case KEYCODE_4:
/*TODO*///			if (ADDRESS_BITS(LoadedCheatTable[CheatNo].CpuNo) < 13) break;
/*TODO*///		case KEYCODE_3:
/*TODO*///		case KEYCODE_2:
/*TODO*///		case KEYCODE_1:
/*TODO*///			/* these keys only apply to the address line */
/*TODO*///			if (s == 2)
/*TODO*///			{
/*TODO*///				int addr = LoadedCheatTable[CheatNo].Address;	/* copy address*/
/*TODO*///				int digit = (KEYCODE_8 - key);	/* if key is KEYCODE_8, digit = 0 */
/*TODO*///				int mask;
/*TODO*///
/*TODO*///				/* adjust digit based on cpu address range */
/*TODO*///				digit -= (8 - (ADDRESS_BITS(LoadedCheatTable[CheatNo].CpuNo)+3) / 4);
/*TODO*///
/*TODO*///				mask = 0xF << (digit * 4);	/* if digit is 1, mask = 0xf0 */
/*TODO*///
/*TODO*///				do
/*TODO*///				{
/*TODO*///				if ((addr & mask) == mask)
/*TODO*///					/* wrap hex digit around to 0 if necessary */
/*TODO*///					addr &= ~mask;
/*TODO*///				else
/*TODO*///					/* otherwise bump hex digit by 1 */
/*TODO*///					addr += (0x1 << (digit * 4));
/*TODO*///				} while (addr > MAX_ADDRESS(LoadedCheatTable[CheatNo].CpuNo));
/*TODO*///
/*TODO*///				LoadedCheatTable[CheatNo].Address = addr;
/*TODO*///				sprintf(str2[2], FormatAddr(LoadedCheatTable[CheatNo].CpuNo,1),
/*TODO*///					LoadedCheatTable[CheatNo].Address);
/*TODO*///			}
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_F3:
/*TODO*///			while (keyboard_pressed(key));
/*TODO*///		  oldkey = 0;
/*TODO*///			switch (s)
/*TODO*///			{
/*TODO*///				case 2:    /* Address */
/*TODO*///					for (i = 0; i < total; i++)
/*TODO*///						dt[i].color = DT_COLOR_WHITE;
/*TODO*///					displaytext (dt, 0,1);
/*TODO*///					xprintf (0, 0, EditYPos-2*FontHeight, "Edit Cheat Address:");
/*TODO*///					sprintf(buffer, FormatAddr(LoadedCheatTable[CheatNo].CpuNo,0),
/*TODO*///						LoadedCheatTable[CheatNo].Address);
/*TODO*///				xedit(0, EditYPos, buffer, strlen(buffer), 1);
/*TODO*///					sscanf(buffer,"%X", &LoadedCheatTable[CheatNo].Address);
/*TODO*///					if (LoadedCheatTable[CheatNo].Address > MAX_ADDRESS(LoadedCheatTable[CheatNo].CpuNo))
/*TODO*///						LoadedCheatTable[CheatNo].Address = MAX_ADDRESS(LoadedCheatTable[CheatNo].CpuNo);
/*TODO*///					sprintf(str2[2], FormatAddr(LoadedCheatTable[CheatNo].CpuNo,1),
/*TODO*///						LoadedCheatTable[CheatNo].Address);
/*TODO*///					cheat_clearbitmap();
/*TODO*///					y = EditCheatHeader();
/*TODO*///				  break;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_F10:
/*TODO*///			while (keyboard_pressed(key));
/*TODO*///		  oldkey = 0;
/*TODO*///			EditCheatHelp();
/*TODO*///			y = EditCheatHeader();
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_ENTER:
/*TODO*///		case KEYCODE_ENTER_PAD:
/*TODO*///			while (keyboard_pressed(key));
/*TODO*///		  oldkey = 0;
/*TODO*///			switch (s)
/*TODO*///			{
/*TODO*///				case 0:    /* Name */
/*TODO*///					for (i = 0; i < total; i++)
/*TODO*///						dt[i].color = DT_COLOR_WHITE;
/*TODO*///					displaytext (dt, 0,1);
/*TODO*///					xprintf (0, 0, EditYPos-2*FontHeight, "Edit Cheat Description:");
/*TODO*///				xedit(0, EditYPos, LoadedCheatTable[CheatNo].Name, CHEAT_NAME_MAXLEN, 0);
/*TODO*///					sprintf (str2[0], "Name: %s", LoadedCheatTable[CheatNo].Name);
/*TODO*///					if ((FontWidth * (int)strlen(str2[0])) > MachWidth - Machine->uixmin)
/*TODO*///						sprintf(str2[0],"%s",LoadedCheatTable[CheatNo].Name);
/*TODO*///					cheat_clearbitmap();
/*TODO*///					y = EditCheatHeader();
/*TODO*///				  break;
/*TODO*///				case 5:    /* More */
/*TODO*///					for (i = 0; i < total; i++)
/*TODO*///						dt[i].color = DT_COLOR_WHITE;
/*TODO*///					displaytext (dt, 0,1);
/*TODO*///					xprintf (0, 0, EditYPos-2*FontHeight, "Edit Cheat More Description:");
/*TODO*///				xedit(0, EditYPos, LoadedCheatTable[CheatNo].More, CHEAT_NAME_MAXLEN, 0);
/*TODO*///					sprintf (str2[5], "More: %s", LoadedCheatTable[CheatNo].More);
/*TODO*///					if ((FontWidth * (int)strlen(str2[5])) > MachWidth - Machine->uixmin)
/*TODO*///						sprintf(str2[5],"%s",LoadedCheatTable[CheatNo].More);
/*TODO*///					cheat_clearbitmap();
/*TODO*///					y = EditCheatHeader();
/*TODO*///				  break;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_ESC:
/*TODO*///		case KEYCODE_TAB:
/*TODO*///			while (keyboard_pressed(key));
/*TODO*///			done = 1;
/*TODO*///			break;
/*TODO*///	}
/*TODO*///  } while (done == 0);
/*TODO*///
/*TODO*///  while (keyboard_pressed(key));
/*TODO*///
/*TODO*///  if (	(LoadedCheatTable[CheatNo].Special==62) || (LoadedCheatTable[CheatNo].Special==65)	||
/*TODO*///	(LoadedCheatTable[CheatNo].Special==72) || (LoadedCheatTable[CheatNo].Special==75)	)
/*TODO*///	LoadedCheatTable[CheatNo].Minimum = 1;
/*TODO*///  else
/*TODO*///	LoadedCheatTable[CheatNo].Minimum = 0;
/*TODO*///  if ((LoadedCheatTable[CheatNo].Special>=60) && (LoadedCheatTable[CheatNo].Special<=75))
/*TODO*///  {
/*TODO*///	LoadedCheatTable[CheatNo].Maximum = LoadedCheatTable[CheatNo].Data;
/*TODO*///	LoadedCheatTable[CheatNo].Data = 0;
/*TODO*///  }
/*TODO*///  else
/*TODO*///	LoadedCheatTable[CheatNo].Maximum = 0xFF;
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////* VM 981213 BEGIN */
/*TODO*////*
/*TODO*/// * I don't fully understand how dt and str2 are utilized, so for now I pass them as parameters
/*TODO*/// * until I can figure out which function they ideally belong in.
/*TODO*/// * They should not be turned into globals; this program has too many globals as it is.
/*TODO*/// */
/*TODO*///int build_cheat_list(int Index, struct DisplayText *ext_dt, char ext_str2[MAX_DT + 1][40])
/*TODO*///{
/*TODO*///	int total = 0;
/*TODO*///	while (total < MAX_DISPLAYCHEATS)
/*TODO*///	{
/*TODO*///		if (Index + total >= LoadedCheatTotal)
/*TODO*///		{
/*TODO*///			break;
/*TODO*///		}
/*TODO*///
/*TODO*///		AddCheckToName(total + Index, ext_str2[total]);
/*TODO*///		ext_dt[total].text = ext_str2[total];
/*TODO*///
/*TODO*///		total++;
/*TODO*///	}
/*TODO*///	ext_dt[total].text = 0; /* terminate array */
/*TODO*///
/*TODO*///	/* Clear old list */
/*TODO*///	ClearArea(1, YHEAD_SELECT, YFOOT_SELECT-1);
/*TODO*///
/*TODO*///	return total;
/*TODO*///}
/*TODO*////* VM 981213 END */
/*TODO*///
/*TODO*///int SelectCheatHeader(void)
/*TODO*///{
/*TODO*///  int y = 0;
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///
/*TODO*///  if (LoadedCheatTotal == 0)
/*TODO*///  {
/*TODO*///	xprintf(0, 0, y, "<INS>: Add New Cheat" );
/*TODO*///	y += (FontHeight);
/*TODO*///	xprintf(0, 0, y, "<F10>: Show Help + other keys");
/*TODO*///	y += (FontHeight * 3);
/*TODO*///	xprintf(0, 0, y, "No Cheats Available!");
/*TODO*///  }
/*TODO*///  else
/*TODO*///  {
/*TODO*///	xprintf(0, 0, y, "<DEL>: Delete  <INS>: Add" );
/*TODO*///	y += (FontHeight);
/*TODO*///	xprintf(0, 0, y, "<F1>: Save  <F2>: Watch");
/*TODO*///	y += (FontHeight + 0);
/*TODO*///	xprintf(0, 0, y, "<F3>: Edit  <F6>: Save all");
/*TODO*///	y += (FontHeight);
/*TODO*///	xprintf(0, 0, y, "<F4>: Copy  <F7>: All off");
/*TODO*///	y += (FontHeight);
/*TODO*///	xprintf(0, 0, y, "<ENTER>: Enable/Disable");
/*TODO*///	y += (FontHeight);
/*TODO*///	xprintf(0, 0, y, "<F10>: Show Help + other keys");
/*TODO*///	y += (FontHeight + 4);
/*TODO*///	xprintf(0, 0, y, "Select a Cheat (%d Total)", LoadedCheatTotal);
/*TODO*///	y += (FontHeight + 4);
/*TODO*///  }
/*TODO*///  return(YHEAD_SELECT);
/*TODO*///}
/*TODO*///
/*TODO*///void SelectCheat(void)
/*TODO*///{
/*TODO*///	int i, x, y, highlighted, key, done, total;
/*TODO*///	struct DisplayText dt[MAX_DT + 1];
/*TODO*///	int flag;
/*TODO*///	int Index;
/*TODO*///
/*TODO*///	int BCDOnly, ZeroPoss;
/*TODO*///
/*TODO*///	char str2[60][40];
/*TODO*///	int j;
/*TODO*///
/*TODO*///	char fmt[32];
/*TODO*///	char buf[CHEAT_FILENAME_MAXLEN+1];
/*TODO*///
/*TODO*///	cheat_clearbitmap();
/*TODO*///
/*TODO*///	if (MachWidth < FontWidth * 35)
/*TODO*///	{
/*TODO*///		x = 0;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		x = (MachWidth / 2) - (FontWidth * 16);
/*TODO*///	}
/*TODO*///	y = SelectCheatHeader();
/*TODO*///
/*TODO*///	/* Make the list */
/*TODO*///	for (i = 0; i < MAX_DISPLAYCHEATS; i++)
/*TODO*///	{
/*TODO*///		dt[i].x = x;
/*TODO*///		dt[i].y = y;
/*TODO*///		dt[i].color = DT_COLOR_WHITE;
/*TODO*///		y += FontHeight;
/*TODO*///	}
/*TODO*///
/*TODO*///	Index = 0;
/*TODO*///	highlighted = 0;
/*TODO*///
/*TODO*///	Index = (SaveIndex / MAX_DISPLAYCHEATS) * MAX_DISPLAYCHEATS;
/*TODO*///	highlighted = SaveIndex - Index;
/*TODO*///
/*TODO*///	total = build_cheat_list(Index, dt, str2);
/*TODO*///
/*TODO*///	y += (FontHeight * 2);
/*TODO*///
/*TODO*///	oldkey = 0;
/*TODO*///
/*TODO*///	done = 0;
/*TODO*///	do
/*TODO*///	{
/*TODO*///		for (i = 0; i < total; i++)
/*TODO*///		{
/*TODO*///			dt[i].color = (i == highlighted) ? DT_COLOR_YELLOW : DT_COLOR_WHITE;
/*TODO*///		}
/*TODO*///
/*TODO*///		displaytext(dt, 0, 1);
/*TODO*///
/*TODO*///		/* key = keyboard_read_sync(); */
/*TODO*///		key = cheat_readkey();	  /* MSH 990217 */
/*TODO*///		ClearTextLine(1, YFOOT_SELECT);
/*TODO*///		switch (key)
/*TODO*///		{
/*TODO*///			case KEYCODE_DOWN:
/*TODO*///			case KEYCODE_2_PAD:
/*TODO*///
/*TODO*///				if (highlighted < total - 1)
/*TODO*///				{
/*TODO*///					highlighted++;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					highlighted = 0;
/*TODO*///
/*TODO*///					if (LoadedCheatTotal <= MAX_DISPLAYCHEATS)
/*TODO*///					{
/*TODO*///						break;
/*TODO*///					}
/*TODO*///
/*TODO*///					if (LoadedCheatTotal > Index + MAX_DISPLAYCHEATS)
/*TODO*///					{
/*TODO*///						Index += MAX_DISPLAYCHEATS;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						Index = 0;
/*TODO*///
/*TODO*///					}
/*TODO*///
/*TODO*///					total = build_cheat_list(Index, dt, str2);
/*TODO*///				}
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_UP:
/*TODO*///			case KEYCODE_8_PAD:
/*TODO*///				if (highlighted > 0)
/*TODO*///				{
/*TODO*///					highlighted--;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					highlighted = total - 1;
/*TODO*///					if (LoadedCheatTotal <= MAX_DISPLAYCHEATS)
/*TODO*///					{
/*TODO*///						break;
/*TODO*///					}
/*TODO*///
/*TODO*///					if (Index == 0)
/*TODO*///					{
/*TODO*///						Index = ((LoadedCheatTotal - 1) / MAX_DISPLAYCHEATS) * MAX_DISPLAYCHEATS;
/*TODO*///					}
/*TODO*///					else if (Index > MAX_DISPLAYCHEATS)
/*TODO*///					{
/*TODO*///						Index -= MAX_DISPLAYCHEATS;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						Index = 0;
/*TODO*///					}
/*TODO*///
/*TODO*///					total = build_cheat_list(Index, dt, str2);
/*TODO*///					highlighted = total - 1;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_HOME:
/*TODO*///			case KEYCODE_7_PAD:
/*TODO*///				Index = 0;
/*TODO*///				total = build_cheat_list(Index, dt, str2);
/*TODO*///				highlighted = 0;
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_END:
/*TODO*///			case KEYCODE_1_PAD:
/*TODO*///				Index = ((LoadedCheatTotal - 1) / MAX_DISPLAYCHEATS) * MAX_DISPLAYCHEATS;
/*TODO*///				total = build_cheat_list(Index, dt, str2);
/*TODO*///				highlighted = total - 1;
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_PGDN:
/*TODO*///			case KEYCODE_3_PAD:
/*TODO*///				if (highlighted + Index >= LoadedCheatTotal - MAX_DISPLAYCHEATS)
/*TODO*///				{
/*TODO*///					Index = ((LoadedCheatTotal - 1) / MAX_DISPLAYCHEATS) * MAX_DISPLAYCHEATS;
/*TODO*///					highlighted = (LoadedCheatTotal - 1) - Index;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					Index += MAX_DISPLAYCHEATS;
/*TODO*///				}
/*TODO*///
/*TODO*///				total = build_cheat_list(Index, dt, str2);
/*TODO*///
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_PGUP:
/*TODO*///			case KEYCODE_9_PAD:
/*TODO*///				if (highlighted + Index <= MAX_DISPLAYCHEATS)
/*TODO*///				{
/*TODO*///					Index = 0;
/*TODO*///					highlighted = 0;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					Index -= MAX_DISPLAYCHEATS;
/*TODO*///				}
/*TODO*///
/*TODO*///				total = build_cheat_list(Index, dt, str2);
/*TODO*///
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_INSERT:	/* Add a new empty cheat */
/*TODO*///			case KEYCODE_0_PAD:
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///			  oldkey = 0;
/*TODO*///
/*TODO*///				if (LoadedCheatTotal > MAX_LOADEDCHEATS -1)
/*TODO*///				{
/*TODO*///					xprintf(0, 0, YFOOT_SELECT, "(Cheat List Is Full.)" );
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				set_cheat(&LoadedCheatTable[LoadedCheatTotal], NEW_CHEAT);
/*TODO*///				LoadedCheatTotal++;
/*TODO*///
/*TODO*///				total = build_cheat_list(Index, dt, str2);
/*TODO*///
/*TODO*///				SelectCheatHeader();
/*TODO*///
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_DEL:
/*TODO*///			case KEYCODE_DEL_PAD:
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///			  oldkey = 0;
/*TODO*///				if (LoadedCheatTotal == 0)
/*TODO*///				{
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				/* Erase the current cheat from the list */
/*TODO*///				/* But before, erase it from the active list if it is there */
/*TODO*///				for (i = 0;i < ActiveCheatTotal;i ++)
/*TODO*///				{
/*TODO*///					if (ActiveCheatTable[i].Address == LoadedCheatTable[highlighted + Index].Address)
/*TODO*///						if (ActiveCheatTable[i].Data == LoadedCheatTable[highlighted + Index].Data)
/*TODO*///						{
/*TODO*///							/* The selected Cheat is already in the list then delete it.*/
/*TODO*///							DeleteActiveCheatFromTable(i);
/*TODO*///							break;
/*TODO*///						}
/*TODO*///				}
/*TODO*///				/* Delete entry from list */
/*TODO*///				DeleteLoadedCheatFromTable(highlighted + Index);
/*TODO*///
/*TODO*///				total = build_cheat_list(Index, dt, str2);
/*TODO*///				if (total <= highlighted)
/*TODO*///				{
/*TODO*///					highlighted = total - 1;
/*TODO*///				}
/*TODO*///
/*TODO*///				if ((total == 0) && (Index != 0))
/*TODO*///				{
/*TODO*///					/* The page is empty so backup one page */
/*TODO*///					if (Index == 0)
/*TODO*///					{
/*TODO*///						Index = ((LoadedCheatTotal - 1) / MAX_DISPLAYCHEATS) * MAX_DISPLAYCHEATS;
/*TODO*///					}
/*TODO*///					else if (Index > MAX_DISPLAYCHEATS)
/*TODO*///					{
/*TODO*///						Index -= MAX_DISPLAYCHEATS;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						Index = 0;
/*TODO*///					}
/*TODO*///
/*TODO*///					total = build_cheat_list(Index, dt, str2);
/*TODO*///					highlighted = total - 1;
/*TODO*///				}
/*TODO*///
/*TODO*///				SelectCheatHeader();
/*TODO*///
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_BACKSPACE:
/*TODO*///				/* Display comment about a cheat */
/*TODO*///				while (keyboard_pressed(key)) ; /* wait for key release */
/*TODO*///				if (LoadedCheatTable[highlighted + Index].More[0])
/*TODO*///					xprintf (0, 0, YFOOT_SELECT, "%s", LoadedCheatTable[highlighted + Index].More);
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_F1:	/* Save cheat to file */
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///			  oldkey = 0;
/*TODO*///				if (LoadedCheatTotal == 0)
/*TODO*///				{
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
/*TODO*///				{
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///			j = SaveCheat(highlighted + Index);
/*TODO*///				xprintf(0, 0, YFOOT_SELECT, "Cheat %sSaved to File %s", (j ? "" : "NOT "), database);
/*TODO*///
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_F2:	 /* Add to watch list */
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///			  oldkey = 0;
/*TODO*///				if (LoadedCheatTotal == 0)
/*TODO*///				{
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
/*TODO*///				{
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (LoadedCheatTable[highlighted + Index].Special == COMMENTCHEAT)
/*TODO*///				{
/*TODO*///					xprintf (0, 0, YFOOT_SELECT, "Comment NOT Added as Watch");
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (LoadedCheatTable[highlighted + Index].CpuNo < cpu_gettotalcpu())
/*TODO*///				{
/*TODO*///					for (i = 0; i < MAX_WATCHES; i++)
/*TODO*///					{
/*TODO*///						if (Watches[i] == MAX_ADDRESS(WatchesCpuNo[i]))
/*TODO*///						{
/*TODO*///							WatchesCpuNo[i] = LoadedCheatTable[highlighted + Index].CpuNo;
/*TODO*///							Watches[i] = LoadedCheatTable[highlighted + Index].Address;
/*TODO*///
/*TODO*///							strcpy(fmt, FormatAddr(SearchCpuNo,0));
/*TODO*///							sprintf (buf, fmt, Watches[i]);
/*TODO*///							xprintf (0, 0, YFOOT_SELECT, "%s Added as Watch %d",buf,i+1);
/*TODO*///
/*TODO*///							WatchesFlag = 1;
/*TODO*///							WatchEnabled = 1;
/*TODO*///
/*TODO*///							break;
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_F3:	/* Edit current cheat */
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///			  oldkey = 0;
/*TODO*///				if (LoadedCheatTotal == 0)
/*TODO*///				{
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
/*TODO*///				{
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (	(LoadedCheatTable[highlighted + Index].Special == COMMENTCHEAT) ||
/*TODO*///					(LoadedCheatTable[highlighted + Index].Special == WATCHCHEAT)	)
/*TODO*///					xedit(0, YFOOT_SELECT, LoadedCheatTable[highlighted + Index].Name,
/*TODO*///						CHEAT_NAME_MAXLEN, 0);
/*TODO*///				else
/*TODO*///					EditCheat(highlighted+Index);
/*TODO*///
/*TODO*///				total = build_cheat_list(Index, dt, str2);
/*TODO*///
/*TODO*///			SelectCheatHeader();
/*TODO*///
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_F4:	/* Copy the current cheat */
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///			  oldkey = 0;
/*TODO*///				if (LoadedCheatTotal == 0)
/*TODO*///				{
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
/*TODO*///				{
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (LoadedCheatTotal > MAX_LOADEDCHEATS - 1)
/*TODO*///				{
/*TODO*///					xprintf(0, 0, YFOOT_SELECT, "(Cheat List Is Full.)" );
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				set_cheat(&LoadedCheatTable[LoadedCheatTotal], &LoadedCheatTable[highlighted + Index]);
/*TODO*///				LoadedCheatTable[LoadedCheatTotal].Count = 0;
/*TODO*///				LoadedCheatTotal++;
/*TODO*///
/*TODO*///				total = build_cheat_list(Index, dt, str2);
/*TODO*///
/*TODO*///				SelectCheatHeader();
/*TODO*///
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_F5:	/* Rename the cheatfile and reload the database */
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///			  oldkey = 0;
/*TODO*///
/*TODO*///				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
/*TODO*///				{
/*TODO*///					strcpy(buf, database);
/*TODO*///					if (RenameCheatFile(1, 0, buf) == 1)
/*TODO*///					{
/*TODO*///						LoadCheat(1, buf);
/*TODO*///						Index = 0;
/*TODO*///						total = build_cheat_list(Index, dt, str2);
/*TODO*///						highlighted = 0;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if (RenameCheatFile(0, 1, database) == 1)
/*TODO*///					{
/*TODO*///						LoadCheat(0, database);
/*TODO*///						Index = 0;
/*TODO*///						total = build_cheat_list(Index, dt, str2);
/*TODO*///						highlighted = 0;
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///			SelectCheatHeader();
/*TODO*///
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_F6:	/* Save all cheats to file */
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///			  oldkey = 0;
/*TODO*///				if (LoadedCheatTotal == 0)
/*TODO*///				{
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
/*TODO*///				{
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///			j = SaveCheat(-1);
/*TODO*///				xprintf(0, 0, YFOOT_SELECT, "%d Cheats Saved to File %s", j, database);
/*TODO*///
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_F7:	/* Remove all active cheats from the list */
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///			  oldkey = 0;
/*TODO*///				if (LoadedCheatTotal == 0)
/*TODO*///				{
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				ActiveCheatTotal = 0;
/*TODO*///
/*TODO*///				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
/*TODO*///				{
/*TODO*///					for (i = 0;i < LoadedCheatTotal;i ++)
/*TODO*///					{
/*TODO*///						if (ActiveCheatTotal > MAX_ACTIVECHEATS-1)
/*TODO*///						{
/*TODO*///							xprintf( 0, 0, YFOOT_SELECT, "(Limit Of Active Cheats)" );
/*TODO*///							break;
/*TODO*///						}
/*TODO*///
/*TODO*///						if (	(LoadedCheatTable[i].Special != COMMENTCHEAT)	&&
/*TODO*///							(LoadedCheatTable[i].Special != WATCHCHEAT) )
/*TODO*///						{
/*TODO*///							set_cheat(&ActiveCheatTable[ActiveCheatTotal], &LoadedCheatTable[i]);
/*TODO*///							ActiveCheatTable[ActiveCheatTotal].Count = 0;
/*TODO*///
/*TODO*///							ActiveCheatTotal ++;
/*TODO*///							CheatEnabled = 1;
/*TODO*///							he_did_cheat = 1;
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				total = build_cheat_list(Index, dt, str2);
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_F8:	/* Reload the database */
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///			  oldkey = 0;
/*TODO*///
/*TODO*///				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
/*TODO*///				{
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				LoadDatabases(1);
/*TODO*///
/*TODO*///				Index = 0;
/*TODO*///				total = build_cheat_list(Index, dt, str2);
/*TODO*///				highlighted = 0;
/*TODO*///
/*TODO*///			SelectCheatHeader();
/*TODO*///
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_F9:	/* Rename the cheatfile */
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///			  oldkey = 0;
/*TODO*///
/*TODO*///				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
/*TODO*///				{
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				RenameCheatFile(-1, 1, database);
/*TODO*///
/*TODO*///			SelectCheatHeader();
/*TODO*///
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_F10:	 /* Invoke help */
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///			  oldkey = 0;
/*TODO*///
/*TODO*///				if (LoadedCheatTotal == 0)
/*TODO*///				{
/*TODO*///					CheatListHelpEmpty();
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					CheatListHelp();
/*TODO*///				}
/*TODO*///
/*TODO*///				SelectCheatHeader();
/*TODO*///
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_F11:	 /* Toggle sologame ON/OFF then reload the database */
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///			  oldkey = 0;
/*TODO*///
/*TODO*///				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
/*TODO*///				{
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				sologame ^= 1;
/*TODO*///				LoadDatabases(1);
/*TODO*///
/*TODO*///				Index = 0;
/*TODO*///				total = build_cheat_list(Index, dt, str2);
/*TODO*///				highlighted = 0;
/*TODO*///
/*TODO*///			SelectCheatHeader();
/*TODO*///
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_F12:	 /* Display info about a cheat */
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///			  oldkey = 0;
/*TODO*///				if (LoadedCheatTotal == 0)
/*TODO*///				{
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
/*TODO*///				{
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (LoadedCheatTable[highlighted + Index].Special == COMMENTCHEAT)
/*TODO*///				{
/*TODO*///					xprintf (0, 0, YFOOT_SELECT, "This is a Comment");
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (LoadedCheatTable[highlighted + Index].Special == WATCHCHEAT)
/*TODO*///				{
/*TODO*///					AddCpuToAddr(LoadedCheatTable[highlighted + Index].CpuNo,
/*TODO*///							 LoadedCheatTable[highlighted + Index].Address,
/*TODO*///							 RD_GAMERAM (LoadedCheatTable[highlighted + Index].CpuNo,
/*TODO*///									 LoadedCheatTable[highlighted + Index].Address), buf);
/*TODO*///					strcat(buf," [Watch]");
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					AddCpuToAddr(LoadedCheatTable[highlighted + Index].CpuNo,
/*TODO*///							 LoadedCheatTable[highlighted + Index].Address,
/*TODO*///							 LoadedCheatTable[highlighted + Index].Data, fmt);
/*TODO*///					strcat(fmt," [Type %d]");
/*TODO*///					sprintf(buf, fmt, LoadedCheatTable[highlighted + Index].Special);
/*TODO*///				}
/*TODO*///
/*TODO*///				xprintf(0, 0, YFOOT_SELECT, "%s", buf);
/*TODO*///
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_ENTER:
/*TODO*///			case KEYCODE_ENTER_PAD:
/*TODO*///			case KEYCODE_SPACE:
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///			  oldkey = 0;
/*TODO*///				if (total == 0)
/*TODO*///				{
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (LoadedCheatTable[highlighted + Index].Special == COMMENTCHEAT)
/*TODO*///				{
/*TODO*///					xprintf (0, 0, YFOOT_SELECT, "Comment NOT Activated");
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (LoadedCheatTable[highlighted + Index].Special == WATCHCHEAT)
/*TODO*///				{
/*TODO*///					if (LoadedCheatTable[highlighted + Index].CpuNo < cpu_gettotalcpu())  /* watches are for all cpus */
/*TODO*///					{
/*TODO*///						for (i = 0; i < MAX_WATCHES; i++)
/*TODO*///						{
/*TODO*///							if (Watches[i] == MAX_ADDRESS(WatchesCpuNo[i]))
/*TODO*///							{
/*TODO*///								WatchesCpuNo[i] = LoadedCheatTable[highlighted + Index].CpuNo;Watches[i] = LoadedCheatTable[highlighted + Index].Address;
/*TODO*///
/*TODO*///								strcpy(fmt, FormatAddr(SearchCpuNo,0));
/*TODO*///								sprintf (buf, fmt, Watches[i]);
/*TODO*///								xprintf (0, 0, YFOOT_SELECT, "%s Added as Watch %d",buf,i+1);
/*TODO*///
/*TODO*///								WatchesFlag = 1;
/*TODO*///								WatchEnabled = 1;
/*TODO*///								break;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///					break;
/*TODO*///				}
/*TODO*///
/*TODO*///				flag = 0;
/*TODO*///				for (i = 0;i < ActiveCheatTotal;i ++)
/*TODO*///				{
/*TODO*///					if (ActiveCheatTable[i].Address == LoadedCheatTable[highlighted + Index].Address)
/*TODO*///					{
/*TODO*///						if (	((ActiveCheatTable[i].Special >=  20) && (ActiveCheatTable[i].Special <=  24))	||
/*TODO*///							((ActiveCheatTable[i].Special >=  40) && (ActiveCheatTable[i].Special <=  44))	||
/*TODO*///							((ActiveCheatTable[i].Special >=  20 + OFFSET_LINK_CHEAT) && (ActiveCheatTable[i].Special <= 24 + OFFSET_LINK_CHEAT))	||
/*TODO*///							((ActiveCheatTable[i].Special >=  40 + OFFSET_LINK_CHEAT) && (ActiveCheatTable[i].Special <= 44 + OFFSET_LINK_CHEAT))	)
/*TODO*///						{
/*TODO*///							if (	(ActiveCheatTable[i].Special	!= LoadedCheatTable[highlighted + Index].Special)	||
/*TODO*///								(ActiveCheatTable[i].Data	!= LoadedCheatTable[highlighted + Index].Data	)	)
/*TODO*///								continue;
/*TODO*///						}
/*TODO*///
/*TODO*///						/* The selected Cheat is already in the list then delete it.*/
/*TODO*///						DeleteActiveCheatFromTable(i);
/*TODO*///						flag = 1;
/*TODO*///
/*TODO*///						/* Delete linked cheats */
/*TODO*///						while (i < ActiveCheatTotal)
/*TODO*///						{
/*TODO*///							if ((ActiveCheatTable[i].Special < OFFSET_LINK_CHEAT) ||
/*TODO*///									(ActiveCheatTable[i].Special == COMMENTCHEAT) ||
/*TODO*///									(ActiveCheatTable[i].Special == WATCHCHEAT))
/*TODO*///								break;
/*TODO*///							DeleteActiveCheatFromTable(i);
/*TODO*///						}
/*TODO*///
/*TODO*///						break;
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				/* Add the selected cheat to the active cheats list if it was not already there */
/*TODO*///				if (flag == 0)
/*TODO*///				{
/*TODO*///					/* No more than MAX_ACTIVECHEATS cheat at the time */
/*TODO*///					if (ActiveCheatTotal > MAX_ACTIVECHEATS-1)
/*TODO*///					{
/*TODO*///						xprintf( 0, 0, YFOOT_SELECT, "(Limit Of Active Cheats)" );
/*TODO*///						break;
/*TODO*///					}
/*TODO*///
/*TODO*///					set_cheat(&ActiveCheatTable[ActiveCheatTotal], &LoadedCheatTable[highlighted + Index]);
/*TODO*///					ActiveCheatTable[ActiveCheatTotal].Count = 0;
/*TODO*///					ValTmp = 0;
/*TODO*///
/*TODO*///					if (	(ActiveCheatTable[ActiveCheatTotal].Special>=60)	&&
/*TODO*///						(ActiveCheatTable[ActiveCheatTotal].Special<=75)	)
/*TODO*///					{
/*TODO*///						ActiveCheatTable[ActiveCheatTotal].Data =
/*TODO*///							RD_GAMERAM (ActiveCheatTable[ActiveCheatTotal].CpuNo,
/*TODO*///									ActiveCheatTable[ActiveCheatTotal].Address);
/*TODO*///						BCDOnly = ( (ActiveCheatTable[ActiveCheatTotal].Special == 63)	||
/*TODO*///								(ActiveCheatTable[ActiveCheatTotal].Special == 64)	||
/*TODO*///								(ActiveCheatTable[ActiveCheatTotal].Special == 65)	||
/*TODO*///								(ActiveCheatTable[ActiveCheatTotal].Special == 73)	||
/*TODO*///								(ActiveCheatTable[ActiveCheatTotal].Special == 74)	||
/*TODO*///								(ActiveCheatTable[ActiveCheatTotal].Special == 75)	);
/*TODO*///						ZeroPoss = ((ActiveCheatTable[ActiveCheatTotal].Special == 60)	||
/*TODO*///								(ActiveCheatTable[ActiveCheatTotal].Special == 63)	||
/*TODO*///								(ActiveCheatTable[ActiveCheatTotal].Special == 70)	||
/*TODO*///								(ActiveCheatTable[ActiveCheatTotal].Special == 73)	);
/*TODO*///
/*TODO*///						ValTmp = SelectValue(ActiveCheatTable[ActiveCheatTotal].Data,
/*TODO*///										BCDOnly, ZeroPoss, 0, 0,
/*TODO*///										ActiveCheatTable[ActiveCheatTotal].Minimum,
/*TODO*///										ActiveCheatTable[ActiveCheatTotal].Maximum,
/*TODO*///										"%03d", "Enter the new Value:", 1,
/*TODO*///										FIRSTPOS + 3 * FontHeight);
/*TODO*///						if (ValTmp > NOVALUE)
/*TODO*///							ActiveCheatTable[ActiveCheatTotal].Data = ValTmp;
/*TODO*///					}
/*TODO*///
/*TODO*///					if (ValTmp > NOVALUE)
/*TODO*///					{
/*TODO*///						ActiveCheatTotal ++;
/*TODO*///						CheatEnabled = 1;
/*TODO*///						he_did_cheat = 1;
/*TODO*///
/*TODO*///						/* Activate linked cheats */
/*TODO*///						for (i = highlighted + Index + 1; i < LoadedCheatTotal && ActiveCheatTotal < MAX_ACTIVECHEATS; i++)
/*TODO*///						{
/*TODO*///							if ((LoadedCheatTable[i].Special < OFFSET_LINK_CHEAT) ||
/*TODO*///									(LoadedCheatTable[i].Special == COMMENTCHEAT) ||
/*TODO*///									(LoadedCheatTable[i].Special == WATCHCHEAT))
/*TODO*///								break;
/*TODO*///							set_cheat(&ActiveCheatTable[ActiveCheatTotal], &LoadedCheatTable[i]);
/*TODO*///							ActiveCheatTable[ActiveCheatTotal].Count = 0;
/*TODO*///							ActiveCheatTotal ++;
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				cheat_clearbitmap();
/*TODO*///
/*TODO*///			SelectCheatHeader();
/*TODO*///
/*TODO*///				total = build_cheat_list(Index, dt, str2);
/*TODO*///
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_ESC:
/*TODO*///			case KEYCODE_TAB:
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///
/*TODO*///				done = 1;
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	while (done == 0);
/*TODO*///
/*TODO*///	while (keyboard_pressed(key));
/*TODO*///
/*TODO*///	  SaveIndex = Index + highlighted;
/*TODO*///
/*TODO*///	/* clear the screen before returning */
/*TODO*///	cheat_clearbitmap();
/*TODO*///}
/*TODO*///
/*TODO*///int SelectSearchValueHeader(void)
/*TODO*///{
/*TODO*///  int y = FIRSTPOS + (2 * FontHeight);
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///
/*TODO*///  xprintf(0, 0,y,"Choose One Of The Following:");
/*TODO*///  y += 2*FontHeight;
/*TODO*///  return(y);
/*TODO*///}
/*TODO*///
/*TODO*///int StartSearchHeader(void)
/*TODO*///{
/*TODO*///  int y = FIRSTPOS;
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///
/*TODO*///  xprintf(0, 0, y, "<F10>: Show Help");
/*TODO*///  y += 2*FontHeight;
/*TODO*///  xprintf(0, 0,y,"Choose One Of The Following:");
/*TODO*///  y += 2*FontHeight;
/*TODO*///  return(y);
/*TODO*///}
/*TODO*///
/*TODO*///int ContinueSearchHeader(void)
/*TODO*///{
/*TODO*///  int y = FIRSTPOS;
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///
/*TODO*///  xprintf(0, 0, y, "<F1>: Start A New Search");
/*TODO*///  y += 2*FontHeight;
/*TODO*///
/*TODO*///  switch (CurrentMethod)
/*TODO*///  {
/*TODO*///	case SEARCH_VALUE:
/*TODO*///		xprintf(0, 0,y,"Enter The New Value:");
/*TODO*///		break;
/*TODO*///	case SEARCH_TIME:
/*TODO*///		xprintf(0, 0,y,"Enter How Much The Value");
/*TODO*///		y += FontHeight;
/*TODO*///		if ( iCheatInitialized )
/*TODO*///		{
/*TODO*///			xprintf(0, 0,y,"Has Changed Since You");
/*TODO*///			y += FontHeight;
/*TODO*///			xprintf(0, 0,y,"Started The Search:");
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			xprintf(0, 0,y,"Has Changed Since The");
/*TODO*///			y += FontHeight;
/*TODO*///			xprintf(0, 0,y,"Last Check:");
/*TODO*///		}
/*TODO*///		break;
/*TODO*///	case SEARCH_ENERGY:
/*TODO*///		xprintf(0, 0,y,"Choose The Expression That");
/*TODO*///		y += FontHeight;
/*TODO*///		xprintf(0, 0,y,"Specifies What Occured Since");
/*TODO*///		y += FontHeight;
/*TODO*///		if ( iCheatInitialized )
/*TODO*///			xprintf(0, 0,y,"You Started The Search:");
/*TODO*///		else
/*TODO*///			xprintf(0, 0,y,"The Last Check:");
/*TODO*///		break;
/*TODO*///	case SEARCH_BIT:
/*TODO*///		xprintf(0, 0,y,"Choose One Of The Following:");
/*TODO*///		break;
/*TODO*///	case SEARCH_BYTE:
/*TODO*///		xprintf(0, 0,y,"Choose One Of The Following:");
/*TODO*///		break;
/*TODO*///  }
/*TODO*///
/*TODO*///  y += 2*FontHeight;
/*TODO*///  return(y);
/*TODO*///}
/*TODO*///
/*TODO*///int ContinueSearchMatchHeader(int count)
/*TODO*///{
/*TODO*///  int y = 0;
/*TODO*///  char *str;
/*TODO*///
/*TODO*///  xprintf(0, 0,y,"Matches Found: %d",count);
/*TODO*///  if (count > MAX_MATCHES)
/*TODO*///	str = "Here Are some Matches:";
/*TODO*///  else
/*TODO*///	if (count != 0)
/*TODO*///		str = "Here Is The List:";
/*TODO*///	else
/*TODO*///		str = "(No Matches Found)";
/*TODO*///  y += 2*FontHeight;
/*TODO*///  xprintf(0, 0,y,"%s",str);
/*TODO*///  return(y);
/*TODO*///}
/*TODO*///
/*TODO*///void ContinueSearchMatchFooter(int count, int idx)
/*TODO*///{
/*TODO*///  int y = YFOOT_MATCH;
/*TODO*///
/*TODO*///  y += FontHeight;
/*TODO*///  if (LoadedCheatTotal >= MAX_LOADEDCHEATS)
/*TODO*///	xprintf(0, 0,y,"(Cheat List Is Full.)");
/*TODO*///  else
/*TODO*///	ClearTextLine(1, y);
/*TODO*///  y += 2 * FontHeight;
/*TODO*///
/*TODO*///  if ((count > MAX_MATCHES) && (idx != 0))
/*TODO*///	xprintf(0, 0,y,"<HOME>: First page");
/*TODO*///  else
/*TODO*///	ClearTextLine(1, y);
/*TODO*///  y += FontHeight;
/*TODO*///
/*TODO*///  if (count > idx+MAX_MATCHES)
/*TODO*///	xprintf(0, 0,y,"<PAGE DOWN>: Scroll");
/*TODO*///  else
/*TODO*///	ClearTextLine(1, y);
/*TODO*///  y += FontHeight;
/*TODO*///
/*TODO*///  if (FindFreeWatch())
/*TODO*///	xprintf(0, 0,y,"<F2>/<F8>: Add One/All To Watches");
/*TODO*///  else
/*TODO*///	ClearTextLine(1, y);
/*TODO*///  y += FontHeight;
/*TODO*///
/*TODO*///  if (LoadedCheatTotal < MAX_LOADEDCHEATS)
/*TODO*///	xprintf(0, 0,y,"<F1>/<F6>: Add One/All To List");
/*TODO*///  else
/*TODO*///	ClearTextLine(1, y);
/*TODO*///}
/*TODO*///
/*TODO*///int build_mem_list(int Index, struct DisplayText *ext_dt, char ext_str2[MAX_DT + 1][40])
/*TODO*///{
/*TODO*///	int total = 0;
/*TODO*///	while (total < MAX_DISPLAYMEM)
/*TODO*///	{
/*TODO*///		if (Index + total >= MemoryAreasTotal)
/*TODO*///		{
/*TODO*///			break;
/*TODO*///		}
/*TODO*///		AddCheckToMemArea(total + Index, ext_str2[total]);
/*TODO*///		ext_dt[total].text = ext_str2[total];
/*TODO*///		ext_dt[total].x = (MachWidth - FontWidth * strlen(ext_dt[total].text)) / 2;
/*TODO*///		total++;
/*TODO*///	}
/*TODO*///	ext_dt[total].text = 0; /* terminate array */
/*TODO*///
/*TODO*///	/* Clear old list */
/*TODO*///	ClearArea(1, YHEAD_MEMORY, YFOOT_MEMORY-1);
/*TODO*///
/*TODO*///	return total;
/*TODO*///}
/*TODO*///
/*TODO*///int SelectMemoryHeader(void)
/*TODO*///{
/*TODO*///  int y = 0;
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///
/*TODO*///  xprintf(0, 0, y, "<F6>: All on  <F7>: All off");
/*TODO*///  y += (FontHeight);
/*TODO*///  xprintf(0, 0, y, "<F12>: Display Info on Area");
/*TODO*///  y += (FontHeight);
/*TODO*///  xprintf(0, 0, y, "<ENTER>: Enable/Disable");
/*TODO*///  y += (FontHeight * 2);
/*TODO*///  xprintf(0, 0, y, "Select Memory Areas to Scan");
/*TODO*///  y += (FontHeight);
/*TODO*///  xprintf(0, 0, y, "for CPU %d (%d Total)", SearchCpuNo, MemoryAreasTotal);
/*TODO*///  y += (FontHeight + 2);
/*TODO*///
/*TODO*///  return(YHEAD_MEMORY);
/*TODO*///}
/*TODO*///
/*TODO*///void SelectMemoryAreas(void)
/*TODO*///{
/*TODO*///	  int SaveMemoryAreas[MAX_EXT_MEMORY];
/*TODO*///
/*TODO*///	int i, x, y, highlighted, key, done, total;
/*TODO*///	struct DisplayText dt[MAX_DT + 1];
/*TODO*///	int Index;
/*TODO*///
/*TODO*///	char str2[60][40];
/*TODO*///
/*TODO*///	  char buffer[40];
/*TODO*///
/*TODO*///	for (i = 0; i < MemoryAreasTotal; i++)
/*TODO*///		  SaveMemoryAreas[i] = MemToScanTable[i].Enabled;
/*TODO*///
/*TODO*///	cheat_clearbitmap();
/*TODO*///
/*TODO*///	if (MachWidth < FontWidth * 35)
/*TODO*///	{
/*TODO*///		x = 0;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		x = (MachWidth - (FontWidth * (2 * strlen(FormatAddr(SearchCpuNo,0)) + 7))) / 2;
/*TODO*///	}
/*TODO*///	y = SelectMemoryHeader();
/*TODO*///
/*TODO*///	/* Make the list */
/*TODO*///	for (i = 0; i < MAX_DISPLAYMEM; i++)
/*TODO*///	{
/*TODO*///		dt[i].x = x;
/*TODO*///		dt[i].y = y;
/*TODO*///		dt[i].color = DT_COLOR_WHITE;
/*TODO*///		y += FontHeight;
/*TODO*///	}
/*TODO*///
/*TODO*///	Index = 0;
/*TODO*///	highlighted = 0;
/*TODO*///
/*TODO*///	total = build_mem_list(Index, dt, str2);
/*TODO*///
/*TODO*///	y += (FontHeight * 2);
/*TODO*///
/*TODO*///	oldkey = 0;
/*TODO*///
/*TODO*///	done = 0;
/*TODO*///	do
/*TODO*///	{
/*TODO*///		for (i = 0; i < total; i++)
/*TODO*///		{
/*TODO*///			dt[i].color = (i == highlighted) ? DT_COLOR_YELLOW : DT_COLOR_WHITE;
/*TODO*///		}
/*TODO*///
/*TODO*///		displaytext(dt, 0, 1);
/*TODO*///
/*TODO*///		/* key = keyboard_read_sync(); */
/*TODO*///		key = cheat_readkey();	  /* MSH 990217 */
/*TODO*///		ClearTextLine(1, YFOOT_MEMORY);
/*TODO*///		switch (key)
/*TODO*///		{
/*TODO*///			case KEYCODE_DOWN:
/*TODO*///			case KEYCODE_2_PAD:
/*TODO*///				if (highlighted < total - 1)
/*TODO*///				{
/*TODO*///					highlighted++;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					highlighted = 0;
/*TODO*///
/*TODO*///					if (MemoryAreasTotal <= MAX_DISPLAYMEM)
/*TODO*///					{
/*TODO*///						break;
/*TODO*///					}
/*TODO*///
/*TODO*///					if (MemoryAreasTotal > Index + MAX_DISPLAYMEM)
/*TODO*///					{
/*TODO*///						Index += MAX_DISPLAYMEM;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						Index = 0;
/*TODO*///					}
/*TODO*///					total = build_mem_list(Index, dt, str2);
/*TODO*///				}
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_UP:
/*TODO*///			case KEYCODE_8_PAD:
/*TODO*///				if (highlighted > 0)
/*TODO*///				{
/*TODO*///					highlighted--;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					highlighted = total - 1;
/*TODO*///					if (MemoryAreasTotal <= MAX_DISPLAYMEM)
/*TODO*///					{
/*TODO*///						break;
/*TODO*///					}
/*TODO*///
/*TODO*///					if (Index == 0)
/*TODO*///					{
/*TODO*///						Index = ((MemoryAreasTotal - 1) / MAX_DISPLAYMEM) * MAX_DISPLAYMEM;
/*TODO*///					}
/*TODO*///					else if (Index > MAX_DISPLAYMEM)
/*TODO*///					{
/*TODO*///						Index -= MAX_DISPLAYMEM;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						Index = 0;
/*TODO*///					}
/*TODO*///					total = build_mem_list(Index, dt, str2);
/*TODO*///					highlighted = total - 1;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_HOME:
/*TODO*///			case KEYCODE_7_PAD:
/*TODO*///				Index = 0;
/*TODO*///				total = build_mem_list(Index, dt, str2);
/*TODO*///				highlighted = 0;
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_END:
/*TODO*///			case KEYCODE_1_PAD:
/*TODO*///				Index = ((MemoryAreasTotal - 1) / MAX_DISPLAYMEM) * MAX_DISPLAYMEM;
/*TODO*///				total = build_mem_list(Index, dt, str2);
/*TODO*///				highlighted = total - 1;
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_PGDN:
/*TODO*///			case KEYCODE_3_PAD:
/*TODO*///				if (highlighted + Index >= MemoryAreasTotal - MAX_DISPLAYMEM)
/*TODO*///				{
/*TODO*///					Index = ((MemoryAreasTotal - 1) / MAX_DISPLAYMEM) * MAX_DISPLAYMEM;
/*TODO*///					highlighted = (MemoryAreasTotal - 1) - Index;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					Index += MAX_DISPLAYMEM;
/*TODO*///				}
/*TODO*///				total = build_mem_list(Index, dt, str2);
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_PGUP:
/*TODO*///			case KEYCODE_9_PAD:
/*TODO*///				if (highlighted + Index <= MAX_DISPLAYMEM)
/*TODO*///				{
/*TODO*///					Index = 0;
/*TODO*///					highlighted = 0;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					Index -= MAX_DISPLAYMEM;
/*TODO*///				}
/*TODO*///				total = build_mem_list(Index, dt, str2);
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_F6:
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///			  oldkey = 0;
/*TODO*///				for (i = 0; i < MemoryAreasTotal; i++)
/*TODO*///				MemToScanTable[i].Enabled = 1;
/*TODO*///				total = build_mem_list(Index, dt, str2);
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_F7:
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///			  oldkey = 0;
/*TODO*///				for (i = 0; i < MemoryAreasTotal; i++)
/*TODO*///				MemToScanTable[i].Enabled = 0;
/*TODO*///				total = build_mem_list(Index, dt, str2);
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_F12:	 /* Display info about a cheat */
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///			  oldkey = 0;
/*TODO*///			strcpy (buffer,str2[Index + highlighted]);
/*TODO*///			strcat (buffer," : ");
/*TODO*///				switch ((FPTR)MemToScanTable[Index + highlighted].handler)
/*TODO*///				{
/*TODO*///					case (FPTR)MWA_NOP:
/*TODO*///						strcat (buffer,"NOP");
/*TODO*///						break;
/*TODO*///					case (FPTR)MWA_RAM:
/*TODO*///						strcat (buffer,"RAM");
/*TODO*///						break;
/*TODO*///					case (FPTR)MWA_ROM:
/*TODO*///						strcat (buffer,"ROM");
/*TODO*///						break;
/*TODO*///					case (FPTR)MWA_RAMROM:
/*TODO*///						strcat (buffer,"RAMROM");
/*TODO*///						break;
/*TODO*///					case (FPTR)MWA_BANK1:
/*TODO*///						strcat (buffer,"BANK1");
/*TODO*///						break;
/*TODO*///					case (FPTR)MWA_BANK2:
/*TODO*///						strcat (buffer,"BANK2");
/*TODO*///						break;
/*TODO*///					case (FPTR)MWA_BANK3:
/*TODO*///						strcat (buffer,"BANK3");
/*TODO*///						break;
/*TODO*///					case (FPTR)MWA_BANK4:
/*TODO*///						strcat (buffer,"BANK4");
/*TODO*///						break;
/*TODO*///					case (FPTR)MWA_BANK5:
/*TODO*///						strcat (buffer,"BANK5");
/*TODO*///						break;
/*TODO*///					case (FPTR)MWA_BANK6:
/*TODO*///						strcat (buffer,"BANK6");
/*TODO*///						break;
/*TODO*///					case (FPTR)MWA_BANK7:
/*TODO*///						strcat (buffer,"BANK7");
/*TODO*///						break;
/*TODO*///					case (FPTR)MWA_BANK8:
/*TODO*///						strcat (buffer,"BANK8");
/*TODO*///						break;
/*TODO*///					default:
/*TODO*///						strcat (buffer,"user-defined");
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				xprintf(0, 0, YFOOT_MEMORY, "%s", &buffer[2]);
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_ENTER:
/*TODO*///			case KEYCODE_ENTER_PAD:
/*TODO*///			case KEYCODE_SPACE:
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///			  oldkey = 0;
/*TODO*///			MemToScanTable[Index + highlighted].Enabled ^= 1;
/*TODO*///				total = build_mem_list(Index, dt, str2);
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_ESC:
/*TODO*///			case KEYCODE_TAB:
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///				done = 1;
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	while (done == 0);
/*TODO*///
/*TODO*///	while (keyboard_pressed(key)) ; /* wait for key release */
/*TODO*///
/*TODO*///	/* clear the screen before returning */
/*TODO*///	cheat_clearbitmap();
/*TODO*///
/*TODO*///	  MemoryAreasSelected = 0;
/*TODO*///	for (i = 0; i < MemoryAreasTotal; i++)
/*TODO*///	{
/*TODO*///		  if (SaveMemoryAreas[i] != MemToScanTable[i].Enabled)
/*TODO*///		{
/*TODO*///			RebuildTables = 1;
/*TODO*///		}
/*TODO*///		  if (MemToScanTable[i].Enabled)
/*TODO*///		{
/*TODO*///			MemoryAreasSelected = 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	if (!MemoryAreasSelected)
/*TODO*///	{
/*TODO*///		y = (MachHeight - 8 * FontHeight) / 2;
/*TODO*///		xprintf(0, 0,y,"WARNING !");
/*TODO*///		y += 2*FontHeight;
/*TODO*///		xprintf(0, 0,y,"No Memory Area Selected !");
/*TODO*///		y += 4*FontHeight;
/*TODO*///		xprintf(0, 0,y,"Press A Key To Continue...");
/*TODO*///		key = keyboard_read_sync();
/*TODO*///		while (keyboard_pressed(key)) ; /* wait for key release */
/*TODO*///		cheat_clearbitmap();
/*TODO*///	}
/*TODO*///
/*TODO*///}
/*TODO*///
/*TODO*///int SelectFastSearchHeader(void)
/*TODO*///{
/*TODO*///  int y = FIRSTPOS;
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///
/*TODO*///  xprintf(0, 0, y, "<F10>: Show Help");
/*TODO*///  y += 2*FontHeight;
/*TODO*///  xprintf(0, 0,y,"Choose One Of The Following:");
/*TODO*///  y += 2*FontHeight;
/*TODO*///  return(y);
/*TODO*///}
/*TODO*///
/*TODO*///void SelectFastSearch(void)
/*TODO*///{
/*TODO*///  int y;
/*TODO*///  int s,key,done;
/*TODO*///  int total;
/*TODO*///
/*TODO*///  char *paDisplayText[] = {
/*TODO*///		"Scan All Memory (Slow But Sure)",
/*TODO*///		"Scan all RAM and BANKS (Normal)",
/*TODO*///		"Scan one BANK (Fastest Search)",
/*TODO*///		"Select Memory Areas (Manual Search)",
/*TODO*///		"",
/*TODO*///		"Return To Start Search Menu",
/*TODO*///		0 };
/*TODO*///
/*TODO*///  struct DisplayText dt[10];
/*TODO*///
/*TODO*///  y = SelectFastSearchHeader();
/*TODO*///
/*TODO*///  total = create_menu(paDisplayText, dt, y);
/*TODO*///
/*TODO*///  oldkey = 0;
/*TODO*///
/*TODO*///  s = fastsearch;
/*TODO*///  done = 0;
/*TODO*///  do
/*TODO*///  {
/*TODO*///	key = SelectMenu (&s, dt, 0, 0, 0, total-1, 0, &done);
/*TODO*///	switch (key)
/*TODO*///	{
/*TODO*///		case KEYCODE_F10:
/*TODO*///			SelectFastSearchHelp();
/*TODO*///			done = 0;
/*TODO*///			StartSearchHeader();
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_ENTER:
/*TODO*///		case KEYCODE_ENTER_PAD:
/*TODO*///			switch (s)
/*TODO*///			{
/*TODO*///				case 0:
/*TODO*///					if (fastsearch != 0)
/*TODO*///					{
/*TODO*///						SearchCpuNoOld = -1;	/* Force tables to be built */
/*TODO*///						InitMemoryAreas();
/*TODO*///					}
/*TODO*///					fastsearch = 0;
/*TODO*///					done = 1;
/*TODO*///					break;
/*TODO*///
/*TODO*///				case 1:
/*TODO*///					if (fastsearch != 1)
/*TODO*///					{
/*TODO*///						SearchCpuNoOld = -1;	/* Force tables to be built */
/*TODO*///						InitMemoryAreas();
/*TODO*///					}
/*TODO*///					fastsearch = 1;
/*TODO*///					done = 1;
/*TODO*///					break;
/*TODO*///
/*TODO*///				case 2:
/*TODO*///					if (fastsearch != 2)
/*TODO*///					{
/*TODO*///						SearchCpuNoOld = -1;	/* Force tables to be built */
/*TODO*///						InitMemoryAreas();
/*TODO*///					}
/*TODO*///					fastsearch = 2;
/*TODO*///					done = 1;
/*TODO*///					break;
/*TODO*///
/*TODO*///				case 3:
/*TODO*///					fastsearch = 3;
/*TODO*///				  SelectMemoryAreas();
/*TODO*///					done = 1;
/*TODO*///					break;
/*TODO*///
/*TODO*///				case 5:
/*TODO*///					done = 2;
/*TODO*///					break;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///	}
/*TODO*///  } while ((done != 1) && (done != 2));
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///}
/*TODO*///
/*TODO*///int SelectSearchValue(void)
/*TODO*///{
/*TODO*///  int y;
/*TODO*///  int s,key,done;
/*TODO*///  int total;
/*TODO*///
/*TODO*///  struct DisplayText dt[10];
/*TODO*///
/*TODO*///  char *paDisplayText[] =
/*TODO*///  {
/*TODO*///	"Unknown starting value",
/*TODO*///	"Select starting value",
/*TODO*///	0
/*TODO*///  };
/*TODO*///
/*TODO*///  y = SelectSearchValueHeader();
/*TODO*///  total = create_menu(paDisplayText, dt, y);
/*TODO*///  y = dt[total-1].y + ( 3 * FontHeight );
/*TODO*///
/*TODO*///  oldkey = 0;
/*TODO*///
/*TODO*///  s = 0;
/*TODO*///  done = 0;
/*TODO*///  do
/*TODO*///  {
/*TODO*///	key = SelectMenu (&s, dt, 0, 0, 0, total-1, 0, &done);
/*TODO*///  } while ((done != 1) && (done != 2));
/*TODO*///
/*TODO*///  if (done == 2)
/*TODO*///  {
/*TODO*///	return(done);
/*TODO*///  }
/*TODO*///  else
/*TODO*///  {
/*TODO*///	return(s);
/*TODO*///  }
/*TODO*///}
/*TODO*///
/*TODO*////*****************
/*TODO*/// * Start a cheat search
/*TODO*/// * If the method 1 is selected, ask the user a number
/*TODO*/// * In all cases, backup the ram.
/*TODO*/// *
/*TODO*/// * Ask the user to select one of the following:
/*TODO*/// *	1 - Lives or other number (byte) (exact)	   ask a start value , ask new value
/*TODO*/// *	2 - Timers (byte) (+ or - X)		   nothing at start, ask +-X
/*TODO*/// *	3 - Energy (byte) (less, equal or greater)	   nothing at start, ask less, equal or greater
/*TODO*/// *	4 - Status (bit)  (true or false)		   nothing at start, ask same or opposite
/*TODO*/// *	5 - Slow but sure (Same as start or different) nothing at start, ask same or different
/*TODO*/// *
/*TODO*/// * Another method is used in the Pro action Replay the Energy method
/*TODO*/// *	you can tell that the value is now 25%/50%/75%/100% as the start
/*TODO*/// *	the problem is that I probably cannot search for exactly 50%, so
/*TODO*/// *	that do I do? search +/- 10% ?
/*TODO*/// * If you think of other way to search for codes, let me know.
/*TODO*/// */
/*TODO*///
/*TODO*///void StartSearch(void)
/*TODO*///{
/*TODO*///  int i = 0;
/*TODO*///  int y;
/*TODO*///  int s,key,done,count;
/*TODO*///  int total;
/*TODO*///
/*TODO*///  int StartValueNeeded = 0;
/*TODO*///
/*TODO*///  char *paDisplayText[] = {
/*TODO*///		"Lives, Or Some Other Value",
/*TODO*///		"Timers (+/- Some Value)",
/*TODO*///		"Energy (Greater Or Less)",
/*TODO*///		"Status (A Bit Or Flag)",
/*TODO*///		"Slow But Sure (Changed Or Not)",
/*TODO*///		"",
/*TODO*///		"Change Search Speed",
/*TODO*///		"",
/*TODO*///		"Return To Cheat Menu",
/*TODO*///		0 };
/*TODO*///
/*TODO*///  struct DisplayText dt[10];
/*TODO*///
/*TODO*///  y = StartSearchHeader();
/*TODO*///
/*TODO*///  total = create_menu(paDisplayText, dt, y);
/*TODO*///  y = dt[total-1].y + ( 3 * FontHeight );
/*TODO*///
/*TODO*///  count = 1;
/*TODO*///
/*TODO*///  oldkey = 0;
/*TODO*///
/*TODO*///  s = SaveStartSearch;
/*TODO*///  done = 0;
/*TODO*///  do
/*TODO*///  {
/*TODO*///	key = SelectMenu (&s, dt, 0, 0, 0, total-1, 0, &done);
/*TODO*///	switch (key)
/*TODO*///	{
/*TODO*///		case KEYCODE_F10:
/*TODO*///			StartSearchHelp();								/* Show Help */
/*TODO*///			done = 0;
/*TODO*///			StartSearchHeader();
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_ENTER:
/*TODO*///		case KEYCODE_ENTER_PAD:
/*TODO*///			switch (s)
/*TODO*///			{
/*TODO*///				case 0:
/*TODO*///					SaveMethod = CurrentMethod;
/*TODO*///					CurrentMethod = SEARCH_VALUE;
/*TODO*///				  StartValueNeeded = 1;
/*TODO*///					done = 1;
/*TODO*///					break;
/*TODO*///
/*TODO*///				case 1:
/*TODO*///					SaveMethod = CurrentMethod;
/*TODO*///					CurrentMethod = SEARCH_TIME;
/*TODO*///				  StartValueNeeded = SelectSearchValue();
/*TODO*///				  if (StartValueNeeded == 2)
/*TODO*///				  {
/*TODO*///						done = 0;
/*TODO*///				  }
/*TODO*///				  else
/*TODO*///				  {
/*TODO*///						done = 1;
/*TODO*///				  }
/*TODO*///					break;
/*TODO*///
/*TODO*///				case 2:
/*TODO*///					SaveMethod = CurrentMethod;
/*TODO*///					CurrentMethod = SEARCH_ENERGY;
/*TODO*///				  StartValueNeeded = SelectSearchValue();
/*TODO*///				  if (StartValueNeeded == 2)
/*TODO*///				  {
/*TODO*///						done = 0;
/*TODO*///				  }
/*TODO*///				  else
/*TODO*///				  {
/*TODO*///						done = 1;
/*TODO*///				  }
/*TODO*///					break;
/*TODO*///
/*TODO*///				case 3:
/*TODO*///					SaveMethod = CurrentMethod;
/*TODO*///					CurrentMethod = SEARCH_BIT;
/*TODO*///				  StartValueNeeded = SelectSearchValue();
/*TODO*///				  if (StartValueNeeded == 2)
/*TODO*///				  {
/*TODO*///						done = 0;
/*TODO*///				  }
/*TODO*///				  else
/*TODO*///				  {
/*TODO*///						done = 1;
/*TODO*///				  }
/*TODO*///					break;
/*TODO*///
/*TODO*///				case 4:
/*TODO*///					SaveMethod = CurrentMethod;
/*TODO*///					CurrentMethod = SEARCH_BYTE;
/*TODO*///				  StartValueNeeded = SelectSearchValue();
/*TODO*///				  if (StartValueNeeded == 2)
/*TODO*///				  {
/*TODO*///						done = 0;
/*TODO*///				  }
/*TODO*///				  else
/*TODO*///				  {
/*TODO*///						done = 1;
/*TODO*///				  }
/*TODO*///					break;
/*TODO*///
/*TODO*///				case 6:
/*TODO*///					SelectFastSearch();
/*TODO*///					done = 0;
/*TODO*///					StartSearchHeader();
/*TODO*///					break;
/*TODO*///
/*TODO*///				case 8:
/*TODO*///					done = 2;
/*TODO*///				  s = 0;
/*TODO*///					break;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///	}
/*TODO*///  } while ((done != 1) && (done != 2));
/*TODO*///
/*TODO*///  SaveStartSearch = s;
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///
/*TODO*///  /* User select to return to the previous menu */
/*TODO*///  if (done == 2)
/*TODO*///	return;
/*TODO*///
/*TODO*///  if (ManyCpus)
/*TODO*///  {
/*TODO*///	ValTmp = SelectValue(SearchCpuNo, 0, 1, 1, 0, 0, cpu_gettotalcpu()-1,
/*TODO*///					"%01X", "Enter CPU To Search In:", 1,
/*TODO*///				  FIRSTPOS + 3 * FontHeight);
/*TODO*///
/*TODO*///	cheat_clearbitmap();
/*TODO*///
/*TODO*///	if (ValTmp == NOVALUE)
/*TODO*///	{
/*TODO*///		CurrentMethod = SaveMethod;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (ValTmp == 2 * NOVALUE)
/*TODO*///	{
/*TODO*///		CurrentMethod = SaveMethod;
/*TODO*///		StartSearch();
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	s = ValTmp;
/*TODO*///  }
/*TODO*///  else
/*TODO*///  {
/*TODO*///	s = 0;
/*TODO*///  }
/*TODO*///  SearchCpuNo = s;
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///
/*TODO*///  /* User select to return to the previous menu */
/*TODO*///  if (done == 2)
/*TODO*///	return;
/*TODO*///
/*TODO*///  SaveContinueSearch = 0;
/*TODO*///
/*TODO*///  if (SearchCpuNoOld != SearchCpuNo)
/*TODO*///  {
/*TODO*///	RebuildTables = 1;
/*TODO*///	  if (SearchCpuNoOld != -1)
/*TODO*///	  {
/*TODO*///		InitMemoryAreas();
/*TODO*///	  }
/*TODO*///  }
/*TODO*///  if ((fastsearch == 3) && (!MemoryAreasSelected))
/*TODO*///	SelectMemoryAreas();
/*TODO*///  if (RebuildTables)
/*TODO*///  {
/*TODO*///	if (!build_tables())
/*TODO*///		SearchCpuNoOld = SearchCpuNo;
/*TODO*///	  else
/*TODO*///	{
/*TODO*///		CurrentMethod = 0;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///  }
/*TODO*///  RebuildTables = 0;
/*TODO*///
/*TODO*///  /* If the method 1 is selected, ask for a number */
/*TODO*///  /* if (CurrentMethod == SEARCH_VALUE */
/*TODO*///  if (StartValueNeeded)
/*TODO*///  {
/*TODO*///	ValTmp = SelectValue(0, 0, 1, 1, 1, 0, 0xFF,
/*TODO*///					"%03d  (0x%02X)", "Enter Value To Search For:", 1, FIRSTPOS + 3 * FontHeight);
/*TODO*///
/*TODO*///	cheat_clearbitmap();
/*TODO*///
/*TODO*///	if (ValTmp == NOVALUE)
/*TODO*///	{
/*TODO*///		CurrentMethod = SaveMethod;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (ValTmp == 2 * NOVALUE)
/*TODO*///	{
/*TODO*///		CurrentMethod = SaveMethod;
/*TODO*///		StartSearch();
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	s = ValTmp;
/*TODO*///  }
/*TODO*///  else
/*TODO*///  {
/*TODO*///	s = 0;
/*TODO*///  }
/*TODO*///  StartValue = s;
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///
/*TODO*///  y = (MachHeight - FontHeight) / 2 - FontHeight;
/*TODO*///  SearchInProgress(1,y);
/*TODO*///
/*TODO*///  /* Backup the ram */
/*TODO*///  backup_ram (StartRam);
/*TODO*///  backup_ram (BackupRam);
/*TODO*///  memset_ram (FlagTable, 0xFF); /* At start, all location are good */
/*TODO*///
/*TODO*///  /* Flag the location that match the initial value if method 1 */
/*TODO*///  /* if (CurrentMethod == SEARCH_VALUE */
/*TODO*///  if (StartValueNeeded)
/*TODO*///  {
/*TODO*///	struct ExtMemory *ext;
/*TODO*///	  count = 0;
/*TODO*///	for (ext = FlagTable; ext->data; ext++)
/*TODO*///	{
/*TODO*///		for (i=0; i <= ext->end - ext->start; i++)
/*TODO*///			if (ext->data[i] != 0)
/*TODO*///		  {
/*TODO*///				if (	(RD_GAMERAM(SearchCpuNo, i+ext->start) != s)			&&
/*TODO*///					(	(RD_GAMERAM(SearchCpuNo, i+ext->start) != s-1)		||
/*TODO*///						(CurrentMethod != SEARCH_VALUE) 		)	)
/*TODO*///					ext->data[i] = 0;
/*TODO*///			else
/*TODO*///					count ++;
/*TODO*///		  }
/*TODO*///	}
/*TODO*///
/*TODO*///	SearchInProgress(0,y);
/*TODO*///  }
/*TODO*///  else
/*TODO*///  {
/*TODO*///	struct ExtMemory *ext;
/*TODO*///	  count = 0;
/*TODO*///	for (ext = FlagTable; ext->data; ext++)
/*TODO*///		count += ext->end - ext->start + 1;
/*TODO*///
/*TODO*///	SearchInProgress(0,y);
/*TODO*///	iCheatInitialized = 1;
/*TODO*///  }
/*TODO*///
/*TODO*///  /* Copy the tables */
/*TODO*///  copy_ram (OldBackupRam, BackupRam);
/*TODO*///  copy_ram (OldFlagTable, FlagTable);
/*TODO*///
/*TODO*///  if (count == 0)
/*TODO*///  {
/*TODO*///	SaveMethod = CurrentMethod;
/*TODO*///	CurrentMethod = 0;
/*TODO*///  }
/*TODO*///
/*TODO*///  MatchFound = count;
/*TODO*///  OldMatchFound = MatchFound;
/*TODO*///
/*TODO*///  RestoreStatus = RESTORE_NOSAVE;
/*TODO*///
/*TODO*///  y -= 2 * FontHeight;
/*TODO*///  if (count == 0)
/*TODO*///  {
/*TODO*///	xprintf(0, 0,y,"No Matches Found");
/*TODO*///  }
/*TODO*///  else
/*TODO*///  {
/*TODO*///	xprintf(0, 0,y,"Search Initialized.");
/*TODO*///	y += FontHeight;
/*TODO*///	xprintf(0, 0,y,"(Matches Found: %d)",count);
/*TODO*///  }
/*TODO*///
/*TODO*///  y += 4 * FontHeight;
/*TODO*///  xprintf(0, 0,y,"Press A Key To Continue...");
/*TODO*///  key = keyboard_read_sync();
/*TODO*///  while (keyboard_pressed(key)) ; /* wait for key release */
/*TODO*///  cheat_clearbitmap();
/*TODO*///}
/*TODO*///
/*TODO*///void ContinueSearch(int selected, int ViewLast)
/*TODO*///{
/*TODO*///  char str2[MAX_DT + 1][40];
/*TODO*///
/*TODO*///  int i,j,y,count,s,key,done;
/*TODO*///
/*TODO*///  struct DisplayText dt[MAX_DT + 1];
/*TODO*///
/*TODO*///  int total;
/*TODO*///  int Continue;
/*TODO*///
/*TODO*///  struct ExtMemory *ext;
/*TODO*///  struct ExtMemory *ext_br;
/*TODO*///  struct ExtMemory *ext_sr;
/*TODO*///
/*TODO*///  int Index = 0;
/*TODO*///  int countAdded;
/*TODO*///
/*TODO*///  char fmt[40];
/*TODO*///  char buf[60];
/*TODO*///  char *ptr;
/*TODO*///
/*TODO*///  int TrueAddr, TrueData;
/*TODO*///
/*TODO*///  if (!selected)
/*TODO*///  {
/*TODO*///	cheat_save_frameskips();
/*TODO*///  }
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///
/*TODO*///  oldkey = 0;
/*TODO*///
/*TODO*///  if (!ViewLast)
/*TODO*///  {
/*TODO*///	if (CurrentMethod == 0)
/*TODO*///	{
/*TODO*///		StartSearch();
/*TODO*///		/* JCK 990529 BEGIN */
/*TODO*///		if (!selected)
/*TODO*///		{
/*TODO*///			cheat_rest_frameskips();
/*TODO*///		}
/*TODO*///		/* JCK 990529 END */
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	count = 0;
/*TODO*///	y = ContinueSearchHeader();
/*TODO*///
/*TODO*///	/******** Method 1 ***********/
/*TODO*///	/* Ask new value if method 1 */
/*TODO*///	if (CurrentMethod == SEARCH_VALUE)
/*TODO*///	{
/*TODO*///		ValTmp = SelectValue(StartValue, 0, 1, 1, 1, 0, 0xFF, "%03d  (0x%02X)", "", 0, y);
/*TODO*///
/*TODO*///		cheat_clearbitmap();
/*TODO*///
/*TODO*///		if (ValTmp == NOVALUE)
/*TODO*///		{
/*TODO*///			/* JCK 990529 BEGIN */
/*TODO*///			if (!selected)
/*TODO*///			{
/*TODO*///				cheat_rest_frameskips();
/*TODO*///			}
/*TODO*///			/* JCK 990529 END */
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (ValTmp == 2 * NOVALUE)
/*TODO*///		{
/*TODO*///			CurrentMethod = SaveMethod;
/*TODO*///			StartSearch();
/*TODO*///			/* JCK 990529 BEGIN */
/*TODO*///			if (!selected)
/*TODO*///			{
/*TODO*///				cheat_rest_frameskips();
/*TODO*///			}
/*TODO*///			/* JCK 990529 END */
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		s = ValTmp;
/*TODO*///
/*TODO*///		StartValue = s; /* Save the value for when continue */
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	/******** Method 2 ***********/
/*TODO*///	/* Ask new value if method 2 */
/*TODO*///	if (CurrentMethod == SEARCH_TIME)
/*TODO*///	{
/*TODO*///		ValTmp = SelectValue(StartValue, 0, 1, 0, 0, -127, 128, "%+04d", "", 0, y);
/*TODO*///
/*TODO*///		cheat_clearbitmap();
/*TODO*///
/*TODO*///		if (ValTmp == NOVALUE)
/*TODO*///		{
/*TODO*///			/* JCK 990529 BEGIN */
/*TODO*///			if (!selected)
/*TODO*///			{
/*TODO*///				cheat_rest_frameskips();
/*TODO*///			}
/*TODO*///			/* JCK 990529 END */
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (ValTmp == 2 * NOVALUE)
/*TODO*///		{
/*TODO*///			CurrentMethod = SaveMethod;
/*TODO*///			StartSearch();
/*TODO*///			/* JCK 990529 BEGIN */
/*TODO*///			if (!selected)
/*TODO*///			{
/*TODO*///				cheat_rest_frameskips();
/*TODO*///			}
/*TODO*///			/* JCK 990529 END */
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		s = ValTmp;
/*TODO*///
/*TODO*///		iCheatInitialized = 0;
/*TODO*///
/*TODO*///		StartValue = s; /* Save the value for when continue */
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	/******** Method 3 ***********/
/*TODO*///	if (CurrentMethod == SEARCH_ENERGY)
/*TODO*///	{
/*TODO*///		char *paDisplayText[] =
/*TODO*///		  {
/*TODO*///			"New Value is Less",
/*TODO*///			"New Value is Equal",
/*TODO*///			"New Value is Greater",
/*TODO*///			"",
/*TODO*///			"Return To Cheat Menu",
/*TODO*///			0
/*TODO*///		  };
/*TODO*///
/*TODO*///		total = create_menu(paDisplayText, dt, y);
/*TODO*///		y = dt[total-1].y + ( 3 * FontHeight );
/*TODO*///
/*TODO*///		s = SaveContinueSearch;
/*TODO*///		done = 0;
/*TODO*///		do
/*TODO*///		{
/*TODO*///			key = SelectMenu (&s, dt, 0, 0, 0, total-1, 0, &done);
/*TODO*///			switch (key)
/*TODO*///			{
/*TODO*///				case KEYCODE_F1:
/*TODO*///					StartSearch();
/*TODO*///					/* JCK 990529 BEGIN */
/*TODO*///					if (!selected)
/*TODO*///					{
/*TODO*///						cheat_rest_frameskips();
/*TODO*///					}
/*TODO*///					/* JCK 990529 END */
/*TODO*///					return;
/*TODO*///					break;
/*TODO*///
/*TODO*///				case KEYCODE_ENTER:
/*TODO*///				case KEYCODE_ENTER_PAD:
/*TODO*///					if (s == total-1)
/*TODO*///						done = 2;
/*TODO*///					break;
/*TODO*///			}
/*TODO*///		} while ((done != 1) && (done != 2));
/*TODO*///
/*TODO*///		cheat_clearbitmap();
/*TODO*///
/*TODO*///		SaveContinueSearch = s;
/*TODO*///
/*TODO*///		/* User select to return to the previous menu */
/*TODO*///		if (done == 2)
/*TODO*///		{
/*TODO*///			/* JCK 990529 BEGIN */
/*TODO*///			if (!selected)
/*TODO*///			{
/*TODO*///				cheat_rest_frameskips();
/*TODO*///			}
/*TODO*///			/* JCK 990529 END */
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		iCheatInitialized = 0;
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	/******** Method 4 ***********/
/*TODO*///	/* Ask if the value is the same as when we start or the opposite */
/*TODO*///	if (CurrentMethod == SEARCH_BIT)
/*TODO*///	{
/*TODO*///		char *paDisplayText[] =
/*TODO*///		  {
/*TODO*///			"Bit is Same as Start",
/*TODO*///			"Bit is Opposite from Start",
/*TODO*///			"",
/*TODO*///			"Return To Cheat Menu",
/*TODO*///			0
/*TODO*///		  };
/*TODO*///
/*TODO*///		total = create_menu(paDisplayText, dt, y);
/*TODO*///		y = dt[total-1].y + ( 3 * FontHeight );
/*TODO*///
/*TODO*///		s = SaveContinueSearch;
/*TODO*///		done = 0;
/*TODO*///		do
/*TODO*///		{
/*TODO*///			key = SelectMenu (&s, dt, 0, 0, 0, total-1, 0, &done);
/*TODO*///			switch (key)
/*TODO*///			{
/*TODO*///				case KEYCODE_F1:
/*TODO*///					StartSearch();
/*TODO*///					/* JCK 990529 BEGIN */
/*TODO*///					if (!selected)
/*TODO*///					{
/*TODO*///						cheat_rest_frameskips();
/*TODO*///					}
/*TODO*///					/* JCK 990529 END */
/*TODO*///					return;
/*TODO*///					break;
/*TODO*///
/*TODO*///				case KEYCODE_ENTER:
/*TODO*///				case KEYCODE_ENTER_PAD:
/*TODO*///					if (s == total-1)
/*TODO*///						done = 2;
/*TODO*///					break;
/*TODO*///			}
/*TODO*///		} while ((done != 1) && (done != 2));
/*TODO*///
/*TODO*///		SaveContinueSearch = s;
/*TODO*///
/*TODO*///		cheat_clearbitmap();
/*TODO*///
/*TODO*///		/* User select to return to the previous menu */
/*TODO*///		if (done == 2)
/*TODO*///		{
/*TODO*///			/* JCK 990529 BEGIN */
/*TODO*///			if (!selected)
/*TODO*///			{
/*TODO*///				cheat_rest_frameskips();
/*TODO*///			}
/*TODO*///			/* JCK 990529 END */
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		iCheatInitialized = 0;
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	/******** Method 5 ***********/
/*TODO*///	/* Ask if the value is the same as when we start or different */
/*TODO*///	if (CurrentMethod == SEARCH_BYTE)
/*TODO*///	{
/*TODO*///		char *paDisplayText[] =
/*TODO*///		  {
/*TODO*///			"Memory is Same as Start",
/*TODO*///			"Memory is Different from Start",
/*TODO*///			"",
/*TODO*///			"Return To Cheat Menu",
/*TODO*///			0
/*TODO*///		  };
/*TODO*///
/*TODO*///		total = create_menu(paDisplayText, dt, y);
/*TODO*///		y = dt[total-1].y + ( 3 * FontHeight );
/*TODO*///
/*TODO*///		s = SaveContinueSearch;
/*TODO*///		done = 0;
/*TODO*///		do
/*TODO*///		{
/*TODO*///			key = SelectMenu (&s, dt, 0, 0, 0, total-1, 0, &done);
/*TODO*///			switch (key)
/*TODO*///			{
/*TODO*///				case KEYCODE_F1:
/*TODO*///					StartSearch();
/*TODO*///					/* JCK 990529 BEGIN */
/*TODO*///					if (!selected)
/*TODO*///					{
/*TODO*///						cheat_rest_frameskips();
/*TODO*///					}
/*TODO*///					/* JCK 990529 END */
/*TODO*///					return;
/*TODO*///					break;
/*TODO*///
/*TODO*///				case KEYCODE_ENTER:
/*TODO*///				case KEYCODE_ENTER_PAD:
/*TODO*///					if (s == total-1)
/*TODO*///						done = 2;
/*TODO*///					break;
/*TODO*///			}
/*TODO*///		} while ((done != 1) && (done != 2));
/*TODO*///
/*TODO*///		cheat_clearbitmap();
/*TODO*///
/*TODO*///		SaveContinueSearch = s;
/*TODO*///
/*TODO*///		/* User select to return to the previous menu */
/*TODO*///		if (done == 2)
/*TODO*///		{
/*TODO*///			/* JCK 990529 BEGIN */
/*TODO*///			if (!selected)
/*TODO*///			{
/*TODO*///				cheat_rest_frameskips();
/*TODO*///			}
/*TODO*///			/* JCK 990529 END */
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		iCheatInitialized = 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	y = FIRSTPOS + (10 * FontHeight);
/*TODO*///	SearchInProgress(1,y);
/*TODO*///
/*TODO*///	/* Copy the tables */
/*TODO*///	copy_ram (OldBackupRam, BackupRam);
/*TODO*///	copy_ram (OldFlagTable, FlagTable);
/*TODO*///	OldMatchFound = MatchFound;
/*TODO*///
/*TODO*///	RestoreStatus = RESTORE_OK;
/*TODO*///
/*TODO*///	count = 0;
/*TODO*///	for (ext = FlagTable, ext_sr = StartRam, ext_br = BackupRam; ext->data; ext++, ext_sr++, ext_br++)
/*TODO*///	{
/*TODO*///		for (i=0; i <= ext->end - ext->start; i++)
/*TODO*///		  {
/*TODO*///			if (ext->data[i] != 0)
/*TODO*///			{
/*TODO*///			int ValRead = RD_GAMERAM(SearchCpuNo, i+ext->start);
/*TODO*///				switch (CurrentMethod)
/*TODO*///				{
/*TODO*///					case SEARCH_VALUE:			   /* Value */
/*TODO*///						if ((ValRead != s) && (ValRead != s-1))
/*TODO*///							ext->data[i] = 0;
/*TODO*///						break;
/*TODO*///					case SEARCH_TIME:			   /* Timer */
/*TODO*///						if (ValRead != (ext_br->data[i] + s))
/*TODO*///							ext->data[i] = 0;
/*TODO*///						break;
/*TODO*///					case SEARCH_ENERGY: 		   /* Energy */
/*TODO*///					switch (s)
/*TODO*///					{
/*TODO*///						case 0:    /* Less */
/*TODO*///								if (ValRead >= ext_br->data[i])
/*TODO*///									ext->data[i] = 0;
/*TODO*///						break;
/*TODO*///						case 1:    /* Equal */
/*TODO*///								if (ValRead != ext_br->data[i])
/*TODO*///									ext->data[i] = 0;
/*TODO*///						break;
/*TODO*///						case 2:    /* Greater */
/*TODO*///								if (ValRead <= ext_br->data[i])
/*TODO*///									ext->data[i] = 0;
/*TODO*///						break;
/*TODO*///					}
/*TODO*///						break;
/*TODO*///					case SEARCH_BIT:			   /* Bit */
/*TODO*///					switch (s)
/*TODO*///					{
/*TODO*///						case 0:    /* Same */
/*TODO*///								j = ValRead ^ (ext_sr->data[i] ^ 0xFF);
/*TODO*///								ext->data[i] = j & ext->data[i];
/*TODO*///						break;
/*TODO*///						case 1:    /* Opposite */
/*TODO*///								j = ValRead ^ ext_sr->data[i];
/*TODO*///								ext->data[i] = j & ext->data[i];
/*TODO*///						break;
/*TODO*///					}
/*TODO*///						break;
/*TODO*///					case SEARCH_BYTE:			   /* Byte */
/*TODO*///					switch (s)
/*TODO*///					{
/*TODO*///						case 0:    /* Same */
/*TODO*///								if (ValRead != ext_sr->data[i])
/*TODO*///									ext->data[i] = 0;
/*TODO*///						break;
/*TODO*///						case 1:    /* Different */
/*TODO*///								if (ValRead == ext_sr->data[i])
/*TODO*///									ext->data[i] = 0;
/*TODO*///						break;
/*TODO*///					}
/*TODO*///						break;
/*TODO*///					default:
/*TODO*///						ext->data[i] = 0;
/*TODO*///						break;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			if (ext->data[i] != 0)
/*TODO*///			count ++;
/*TODO*///		  }
/*TODO*///	}
/*TODO*///	if ((CurrentMethod == SEARCH_TIME) || (CurrentMethod == SEARCH_ENERGY))
/*TODO*///		backup_ram (BackupRam);
/*TODO*///	SearchInProgress(0,y);
/*TODO*///  }
/*TODO*///  else
/*TODO*///  {
/*TODO*///  count = MatchFound;
/*TODO*///  }
/*TODO*///
/*TODO*///  /* For all methods:
/*TODO*///	- Display how much locations we have found
/*TODO*///	- Display them
/*TODO*///	- The user can press F2 to add one to the watches
/*TODO*///	- The user can press F1 to add one to the cheat list
/*TODO*///	- The user can press F6 to add all to the cheat list */
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///
/*TODO*///  countAdded = 0;
/*TODO*///
/*TODO*///  MatchFound = count;
/*TODO*///
/*TODO*///  y = ContinueSearchMatchHeader(count);
/*TODO*///
/*TODO*///  if (count == 0)
/*TODO*///  {
/*TODO*///	y += 4*FontHeight;
/*TODO*///	xprintf(0, 0,y,"Press A Key To Continue...");
/*TODO*///	key = keyboard_read_sync();
/*TODO*///	while (keyboard_pressed(key)) ; /* wait for key release */
/*TODO*///	cheat_clearbitmap();
/*TODO*///	SaveMethod = CurrentMethod;
/*TODO*///	CurrentMethod = 0;
/*TODO*///	/* JCK 990529 BEGIN */
/*TODO*///	if (!selected)
/*TODO*///	{
/*TODO*///		cheat_rest_frameskips();
/*TODO*///	}
/*TODO*///	/* JCK 990529 END */
/*TODO*///	return;
/*TODO*///  }
/*TODO*///
/*TODO*///  y += 2*FontHeight;
/*TODO*///
/*TODO*///  total = 0;
/*TODO*///  Continue = 0;
/*TODO*///
/*TODO*///  for (ext = FlagTable, ext_sr = StartRam; ext->data && Continue==0; ext++, ext_sr++)
/*TODO*///  {
/*TODO*///	for (i=0; i <= ext->end - ext->start; i++)
/*TODO*///		if (ext->data[i] != 0)
/*TODO*///		{
/*TODO*///			strcpy(fmt, FormatAddr(SearchCpuNo,0));
/*TODO*///			strcat(fmt," = %02X");
/*TODO*///
/*TODO*///			TrueAddr = i+ext->start;
/*TODO*///			TrueData = ext_sr->data[i];
/*TODO*///			sprintf (str2[total], fmt, TrueAddr, TrueData);
/*TODO*///
/*TODO*///			dt[total].text = str2[total];
/*TODO*///			dt[total].x = (MachWidth - FontWidth * strlen(dt[total].text)) / 2;
/*TODO*///			dt[total].y = y;
/*TODO*///			dt[total].color = DT_COLOR_WHITE;
/*TODO*///			total++;
/*TODO*///			y += FontHeight;
/*TODO*///			if (total >= MAX_MATCHES)
/*TODO*///			{
/*TODO*///				Continue = i+ext->start;
/*TODO*///				break;
/*TODO*///			}
/*TODO*///		}
/*TODO*///  }
/*TODO*///
/*TODO*///  dt[total].text = 0; /* terminate array */
/*TODO*///
/*TODO*///  ContinueSearchMatchHeader(count);
/*TODO*///
/*TODO*///  oldkey = 0;
/*TODO*///
/*TODO*///  s = 0;
/*TODO*///  done = 0;
/*TODO*///  do
/*TODO*///  {
/*TODO*///	int Begin = 0;
/*TODO*///
/*TODO*///	ContinueSearchMatchFooter(count, Index);
/*TODO*///
/*TODO*///	key = SelectMenu (&s, dt, 1, 0, 0, total-1, 0, &done);
/*TODO*///
/*TODO*///	ClearTextLine(1, YFOOT_MATCH);
/*TODO*///
/*TODO*///	switch (key)
/*TODO*///	{
/*TODO*///		case KEYCODE_HOME:
/*TODO*///		case KEYCODE_7_PAD:
/*TODO*///			if (count == 0)
/*TODO*///				break;
/*TODO*///			if (count <= MAX_MATCHES)
/*TODO*///				break;
/*TODO*///			if (Index == 0)
/*TODO*///				break;
/*TODO*///
/*TODO*///			cheat_clearbitmap();
/*TODO*///
/*TODO*///			ContinueSearchMatchHeader(count);
/*TODO*///
/*TODO*///			s = 0;
/*TODO*///			Index = 0;
/*TODO*///
/*TODO*///			total = 0;
/*TODO*///
/*TODO*///			Continue = 0;
/*TODO*///			for (ext = FlagTable, ext_sr = StartRam; ext->data && Continue==0; ext++, ext_sr++)
/*TODO*///			{
/*TODO*///				for (i=0; i <= ext->end - ext->start; i++)
/*TODO*///					if ((ext->data[i] != 0) && (total < MAX_MATCHES))
/*TODO*///					{
/*TODO*///						strcpy(fmt, FormatAddr(SearchCpuNo,0));
/*TODO*///						strcat(fmt," = %02X");
/*TODO*///
/*TODO*///						TrueAddr = i+ext->start;
/*TODO*///						TrueData = ext_sr->data[i];
/*TODO*///						sprintf (str2[total], fmt, TrueAddr, TrueData);
/*TODO*///
/*TODO*///						dt[total].text = str2[total];
/*TODO*///						dt[total].x = (MachWidth - FontWidth * strlen(dt[total].text)) / 2;
/*TODO*///						total++;
/*TODO*///						if (total >= MAX_MATCHES)
/*TODO*///						{
/*TODO*///							Continue = i+ext->start;
/*TODO*///							break;
/*TODO*///						}
/*TODO*///					}
/*TODO*///			}
/*TODO*///
/*TODO*///			dt[total].text = 0; /* terminate array */
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_PGDN:
/*TODO*///		case KEYCODE_3_PAD:
/*TODO*///			if (count == 0)
/*TODO*///				break;
/*TODO*///			if (count <= Index+MAX_MATCHES)
/*TODO*///				break;
/*TODO*///
/*TODO*///			cheat_clearbitmap();
/*TODO*///			ContinueSearchMatchHeader(count);
/*TODO*///
/*TODO*///			s = 0;
/*TODO*///			Index += MAX_MATCHES;
/*TODO*///
/*TODO*///			total = 0;
/*TODO*///
/*TODO*///			Begin = Continue+1;
/*TODO*///			Continue = 0;
/*TODO*///
/*TODO*///			for (ext = FlagTable, ext_sr = StartRam; ext->data && Continue==0; ext++, ext_sr++)
/*TODO*///			{
/*TODO*///				if (ext->start <= Begin && ext->end >= Begin)
/*TODO*///				{
/*TODO*///					for (i = Begin - ext->start; i <= ext->end - ext->start; i++)
/*TODO*///						if ((ext->data[i] != 0) && (total < MAX_MATCHES))
/*TODO*///						{
/*TODO*///							strcpy(fmt, FormatAddr(SearchCpuNo,0));
/*TODO*///							strcat(fmt," = %02X");
/*TODO*///
/*TODO*///							TrueAddr = i+ext->start;
/*TODO*///							TrueData = ext_sr->data[i];
/*TODO*///							sprintf (str2[total], fmt, TrueAddr, TrueData);
/*TODO*///
/*TODO*///							dt[total].text = str2[total];
/*TODO*///							dt[total].x = (MachWidth - FontWidth * strlen(dt[total].text)) / 2;
/*TODO*///							total++;
/*TODO*///							if (total >= MAX_MATCHES)
/*TODO*///							{
/*TODO*///								Continue = i+ext->start;
/*TODO*///								break;
/*TODO*///							}
/*TODO*///						}
/*TODO*///				}
/*TODO*///				else if (ext->start > Begin)
/*TODO*///				{
/*TODO*///					for (i=0; i <= ext->end - ext->start; i++)
/*TODO*///						if ((ext->data[i] != 0) && (total < MAX_MATCHES))
/*TODO*///						{
/*TODO*///							strcpy(fmt, FormatAddr(SearchCpuNo,0));
/*TODO*///							strcat(fmt," = %02X");
/*TODO*///
/*TODO*///							TrueAddr = i+ext->start;
/*TODO*///							TrueData = ext_sr->data[i];
/*TODO*///							sprintf (str2[total], fmt, TrueAddr, TrueData);
/*TODO*///
/*TODO*///							dt[total].text = str2[total];
/*TODO*///							dt[total].x = (MachWidth - FontWidth * strlen(dt[total].text)) / 2;
/*TODO*///							total++;
/*TODO*///							if (total >= MAX_MATCHES)
/*TODO*///							{
/*TODO*///								Continue = i+ext->start;
/*TODO*///								break;
/*TODO*///							}
/*TODO*///						}
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			dt[total].text = 0; /* terminate array */
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_F1:
/*TODO*///			while (keyboard_pressed(key));
/*TODO*///		  oldkey = 0;
/*TODO*///
/*TODO*///			if (count == 0)
/*TODO*///				break;
/*TODO*///
/*TODO*///			if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
/*TODO*///			{
/*TODO*///				break;
/*TODO*///			}
/*TODO*///
/*TODO*///			strcpy(buf, dt[s].text);
/*TODO*///			ptr = strtok(buf, "=");
/*TODO*///			sscanf(ptr,"%X", &TrueAddr);
/*TODO*///			ptr = strtok(NULL, "=");
/*TODO*///			sscanf(ptr,"%02X", &TrueData);
/*TODO*///
/*TODO*///		  AddCpuToAddr(SearchCpuNo, TrueAddr, TrueData, str2[MAX_DT]);
/*TODO*///
/*TODO*///			/* Add the selected address to the LoadedCheatTable */
/*TODO*///			if (LoadedCheatTotal < MAX_LOADEDCHEATS)
/*TODO*///			{
/*TODO*///				set_cheat(&LoadedCheatTable[LoadedCheatTotal], NEW_CHEAT);
/*TODO*///				LoadedCheatTable[LoadedCheatTotal].CpuNo   = SearchCpuNo;
/*TODO*///				LoadedCheatTable[LoadedCheatTotal].Address = TrueAddr;
/*TODO*///				LoadedCheatTable[LoadedCheatTotal].Data    = TrueData;
/*TODO*///				strcpy(LoadedCheatTable[LoadedCheatTotal].Name, str2[MAX_DT]);
/*TODO*///				LoadedCheatTotal++;
/*TODO*///				xprintf(0, 0,YFOOT_MATCH,"%s Added to List",str2[MAX_DT]);
/*TODO*///			}
/*TODO*///			else
/*TODO*///				xprintf(0, 0,YFOOT_MATCH,"%s Not Added to List",str2[MAX_DT]);
/*TODO*///
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_F2:
/*TODO*///			while (keyboard_pressed(key));
/*TODO*///		  oldkey = 0;
/*TODO*///
/*TODO*///			if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
/*TODO*///			{
/*TODO*///				break;
/*TODO*///			}
/*TODO*///
/*TODO*///		  j = FindFreeWatch();
/*TODO*///			if (j)
/*TODO*///			{
/*TODO*///				strcpy(buf, dt[s].text);
/*TODO*///				ptr = strtok(buf, "=");
/*TODO*///				sscanf(ptr,"%X", &TrueAddr);
/*TODO*///
/*TODO*///				WatchesCpuNo[j-1] = SearchCpuNo;
/*TODO*///				Watches[j-1] = TrueAddr;
/*TODO*///				WatchesFlag = 1;
/*TODO*///				WatchEnabled = 1;
/*TODO*///				strcpy(fmt, FormatAddr(SearchCpuNo,0));
/*TODO*///				sprintf (str2[MAX_DT], fmt, Watches[j-1]);
/*TODO*///				xprintf(0, 0,YFOOT_MATCH,"%s Added as Watch %d",str2[MAX_DT],j);
/*TODO*///			}
/*TODO*///
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_F6:
/*TODO*///			while (keyboard_pressed(key));
/*TODO*///		  oldkey = 0;
/*TODO*///
/*TODO*///			if (count == 0)
/*TODO*///				break;
/*TODO*///
/*TODO*///			if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
/*TODO*///			{
/*TODO*///				break;
/*TODO*///			}
/*TODO*///
/*TODO*///			countAdded = 0;
/*TODO*///
/*TODO*///			for (ext = FlagTable, ext_sr = StartRam; ext->data; ext++, ext_sr++)
/*TODO*///			{
/*TODO*///				for (i = 0; i <= ext->end - ext->start; i++)
/*TODO*///				{
/*TODO*///					if (LoadedCheatTotal > MAX_LOADEDCHEATS-1)
/*TODO*///						break;
/*TODO*///					if (ext->data[i] != 0)
/*TODO*///					{
/*TODO*///						countAdded++;
/*TODO*///
/*TODO*///						TrueAddr = i+ext->start;
/*TODO*///						TrueData = ext_sr->data[i];
/*TODO*///
/*TODO*///					AddCpuToAddr(SearchCpuNo, TrueAddr, TrueData, str2[MAX_DT]);
/*TODO*///
/*TODO*///						set_cheat(&LoadedCheatTable[LoadedCheatTotal], NEW_CHEAT);
/*TODO*///						LoadedCheatTable[LoadedCheatTotal].CpuNo   = SearchCpuNo;
/*TODO*///						LoadedCheatTable[LoadedCheatTotal].Address = TrueAddr;
/*TODO*///						LoadedCheatTable[LoadedCheatTotal].Data    = TrueData;
/*TODO*///						strcpy(LoadedCheatTable[LoadedCheatTotal].Name,str2[MAX_DT]);
/*TODO*///						LoadedCheatTotal++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				if (LoadedCheatTotal > MAX_LOADEDCHEATS)
/*TODO*///					break;
/*TODO*///			}
/*TODO*///
/*TODO*///			xprintf(0, 0,YFOOT_MATCH,"%d Added to List",countAdded);
/*TODO*///
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_F8:
/*TODO*///			while (keyboard_pressed(key));
/*TODO*///		  oldkey = 0;
/*TODO*///
/*TODO*///			if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
/*TODO*///			{
/*TODO*///				break;
/*TODO*///			}
/*TODO*///
/*TODO*///			countAdded = 0;
/*TODO*///
/*TODO*///			for (ext = FlagTable, ext_sr = StartRam; ext->data; ext++, ext_sr++)
/*TODO*///			{
/*TODO*///				for (i = 0; i <= ext->end - ext->start; i++)
/*TODO*///				{
/*TODO*///				  j = FindFreeWatch();
/*TODO*///					if (!j)
/*TODO*///						break;
/*TODO*///					if (ext->data[i] != 0)
/*TODO*///					{
/*TODO*///						countAdded++;
/*TODO*///
/*TODO*///						TrueAddr = i+ext->start;
/*TODO*///
/*TODO*///						WatchesCpuNo[j-1] = SearchCpuNo;
/*TODO*///						Watches[j-1] = TrueAddr;
/*TODO*///						WatchesFlag = 1;
/*TODO*///						WatchEnabled = 1;
/*TODO*///						strcpy(fmt, FormatAddr(SearchCpuNo,0));
/*TODO*///						sprintf (str2[MAX_DT], fmt, Watches[j-1]);
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			xprintf(0, 0,YFOOT_MATCH,"%d Added as Watches",countAdded);
/*TODO*///
/*TODO*///			break;
/*TODO*///
/*TODO*///	}
/*TODO*///  } while (done != 2);
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///
/*TODO*///  if (!selected)
/*TODO*///  {
/*TODO*///	cheat_rest_frameskips();
/*TODO*///  }
/*TODO*///
/*TODO*///}
/*TODO*///
/*TODO*///void RestoreSearch(void)
/*TODO*///{
/*TODO*///  int key;
/*TODO*///  int y = (MachHeight - 8 * FontHeight) / 2;
/*TODO*///
/*TODO*///  char msg[40];
/*TODO*///  char msg2[40];
/*TODO*///
/*TODO*///  switch (RestoreStatus)
/*TODO*///  {
/*TODO*///	case RESTORE_NOINIT:
/*TODO*///		strcpy(msg, "Search not initialised");
/*TODO*///		break;
/*TODO*///	case RESTORE_NOSAVE:
/*TODO*///		strcpy(msg, "No Previous Values Saved");
/*TODO*///		break;
/*TODO*///	case RESTORE_DONE:
/*TODO*///		strcpy(msg, "Previous Values Already Restored");
/*TODO*///		break;
/*TODO*///	case RESTORE_OK:
/*TODO*///		strcpy(msg, "Previous Values Correctly Restored");
/*TODO*///		break;
/*TODO*///  }
/*TODO*///
/*TODO*///  if (RestoreStatus == RESTORE_OK)
/*TODO*///  {
/*TODO*///	/* Restore the tables */
/*TODO*///	copy_ram (BackupRam, OldBackupRam);
/*TODO*///	copy_ram (FlagTable, OldFlagTable);
/*TODO*///	MatchFound = OldMatchFound;
/*TODO*///	CurrentMethod = SaveMethod;
/*TODO*///	  RestoreStatus = RESTORE_DONE;
/*TODO*///	strcpy(msg2, "Restoration Successful :)");
/*TODO*///  }
/*TODO*///  else
/*TODO*///  {
/*TODO*///	strcpy(msg2, "Restoration Unsuccessful :(");
/*TODO*///  }
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///  xprintf(0, 0,y,"%s",msg);
/*TODO*///  y += 2 * FontHeight;
/*TODO*///  xprintf(0, 0,y,"%s",msg2);
/*TODO*///  y += 4 * FontHeight;
/*TODO*///  xprintf(0, 0,y,"Press A Key To Continue...");
/*TODO*///  key = keyboard_read_sync();
/*TODO*///  while (keyboard_pressed(key)) ; /* wait for key release */
/*TODO*///  cheat_clearbitmap();
/*TODO*///}
/*TODO*///
/*TODO*///int ChooseWatchHeader(void)
/*TODO*///{
/*TODO*///  int i = 0;
/*TODO*///  char *paDisplayText[] = {
/*TODO*///		"<+>: +1 byte    <->: -1 byte",
/*TODO*///		"<1> ... <8>: +1 digit",
/*TODO*///		"<9>: Prev CPU  <0>: Next CPU",
/*TODO*///		"<Delete>: disable a watch",
/*TODO*///		"<Enter>: copy previous watch",
/*TODO*///		"<I><J><K><L>: move watch pos.",
/*TODO*///		"(All \"F\"s = watch disabled)",
/*TODO*///		"",
/*TODO*///		"<F10>: Show Help + other keys",
/*TODO*///		0 };
/*TODO*///
/*TODO*///  struct DisplayText dt[20];
/*TODO*///
/*TODO*///  while (paDisplayText[i])
/*TODO*///  {
/*TODO*///	if(i)
/*TODO*///		dt[i].y = (dt[i - 1].y + FontHeight + 2);
/*TODO*///	else
/*TODO*///		dt[i].y = FIRSTPOS;
/*TODO*///	dt[i].color = DT_COLOR_WHITE;
/*TODO*///	dt[i].text = paDisplayText[i];
/*TODO*///	dt[i].x = (MachWidth - FontWidth * strlen(dt[i].text)) / 2;
/*TODO*///	if(dt[i].x > MachWidth)
/*TODO*///		dt[i].x = 0;
/*TODO*///	i++;
/*TODO*///  }
/*TODO*///  dt[i].text = 0; /* terminate array */
/*TODO*///  displaytext(dt,0,1);
/*TODO*///  return(dt[i-1].y + ( 3 * FontHeight ));
/*TODO*///}
/*TODO*///
/*TODO*///void ChooseWatchFooter(void)
/*TODO*///{
/*TODO*///  int y = YFOOT_WATCH;
/*TODO*///
/*TODO*///  y += FontHeight;
/*TODO*///  if (LoadedCheatTotal > MAX_LOADEDCHEATS-1)
/*TODO*///	xprintf(0, 0,y,"(Cheat List Is Full.)");
/*TODO*///}
/*TODO*///
/*TODO*///void ChooseWatch(void)
/*TODO*///{
/*TODO*///  int i,s,y,key,done;
/*TODO*///  int total;
/*TODO*///  int savey;
/*TODO*///  struct DisplayText dt[MAX_WATCHES+1];
/*TODO*///  char str2[MAX_WATCHES+1][15];
/*TODO*///  char buf[80];
/*TODO*///  char buffer[10];
/*TODO*///  int countAdded;
/*TODO*///  int OldCpuNo = 0;
/*TODO*///
/*TODO*///  /* JCK 990717 BEGIN */
/*TODO*///  int dx = 0;
/*TODO*///  int dy = 0;
/*TODO*///  /* JCK 990717 END */
/*TODO*///
/*TODO*///  int trueorientation;
/*TODO*///  /* hack: force the display into standard orientation to avoid */
/*TODO*///  /* rotating the user interface */
/*TODO*///  trueorientation = Machine->orientation;
/*TODO*///  Machine->orientation = Machine->ui_orientation;
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///
/*TODO*///  y = ChooseWatchHeader();
/*TODO*///  savey = y;
/*TODO*///
/*TODO*///  total = 0;
/*TODO*///
/*TODO*///  for (i=0;i<MAX_WATCHES;i++)
/*TODO*///  {
/*TODO*///	AddCpuToWatch(i, str2[ i ]);
/*TODO*///
/*TODO*///	dt[total].text = str2[ i ];
/*TODO*///
/*TODO*///	  if (i < MAX_WATCHES / 2)
/*TODO*///	  {
/*TODO*///		dt[total].x = ((MachWidth / 2) - FontWidth * strlen(dt[total].text)) / 2;
/*TODO*///	  }
/*TODO*///	  else
/*TODO*///	  {
/*TODO*///		dt[total].x = ((MachWidth / 2) - FontWidth * strlen(dt[total].text)) / 2 + (MachWidth / 2);
/*TODO*///	  }
/*TODO*///
/*TODO*///	dt[total].y = y;
/*TODO*///	dt[total].color = DT_COLOR_WHITE;
/*TODO*///	total++;
/*TODO*///	y += FontHeight;
/*TODO*///	  if (i == (MAX_WATCHES / 2 - 1))
/*TODO*///		y = savey;
/*TODO*///  }
/*TODO*///  dt[total].text = 0; /* terminate array */
/*TODO*///
/*TODO*///  oldkey = 0;
/*TODO*///
/*TODO*///  s = 0;
/*TODO*///  done = 0;
/*TODO*///  do
/*TODO*///  {
/*TODO*///	DisplayWatches(1, &WatchX, &WatchY, buf, s, dx, dy);
/*TODO*///	ChooseWatchFooter();
/*TODO*///	countAdded = 0;
/*TODO*///	key = SelectMenu (&s, dt, 1, 0, 0, total-1, 0, &done);
/*TODO*///	ClearTextLine(1, YFOOT_WATCH);
/*TODO*///	switch (key)
/*TODO*///	{
/*TODO*///		case KEYCODE_J:
/*TODO*///		  oldkey = 0;
/*TODO*///			if (WatchX > Machine->uixmin)
/*TODO*///			{
/*TODO*///				dx = 1;
/*TODO*///				dy = 0;
/*TODO*///				WatchX--;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_L:
/*TODO*///		  oldkey = 0;
/*TODO*///			/* if (WatchX <= ( MachWidth - ( FontWidth * (int)strlen( buf ) ) ) ) */
/*TODO*///			if (WatchX <= ( MachWidth -WatchGfxLen ) )
/*TODO*///			{
/*TODO*///				dx = -1;
/*TODO*///				dy = 0;
/*TODO*///				WatchX++;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_K:
/*TODO*///		  oldkey = 0;
/*TODO*///			if (WatchY <= (MachHeight - FontHeight) - 1)
/*TODO*///			{
/*TODO*///				dx = 0;
/*TODO*///				dy = -1;
/*TODO*///				WatchY++;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_I:
/*TODO*///		  oldkey = 0;
/*TODO*///			if (WatchY > Machine->uiymin)
/*TODO*///			{
/*TODO*///				dx = 0;
/*TODO*///				dy = 1;
/*TODO*///				WatchY--;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_LEFT:
/*TODO*///		case KEYCODE_4_PAD:
/*TODO*///			/* JCK 990717 BEGIN */
/*TODO*///			dx = 0;
/*TODO*///			dy = 0;
/*TODO*///			/* JCK 990717 END */
/*TODO*///			if (Watches[ s ] <= 0)
/*TODO*///				Watches[ s ] = MAX_ADDRESS(WatchesCpuNo[ s ]);
/*TODO*///			else
/*TODO*///				Watches[ s ]--;
/*TODO*///			AddCpuToWatch(s, str2[ s ]);
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_RIGHT:
/*TODO*///		case KEYCODE_6_PAD:
/*TODO*///			/* JCK 990717 BEGIN */
/*TODO*///			dx = 0;
/*TODO*///			dy = 0;
/*TODO*///			/* JCK 990717 END */
/*TODO*///			if (Watches[ s ] >= MAX_ADDRESS(WatchesCpuNo[ s ]))
/*TODO*///				Watches[ s ] = 0;
/*TODO*///			else
/*TODO*///				Watches[ s ]++;
/*TODO*///			AddCpuToWatch(s, str2[ s ]);
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_PGDN:
/*TODO*///		case KEYCODE_3_PAD:
/*TODO*///			/* JCK 990717 BEGIN */
/*TODO*///			dx = 0;
/*TODO*///			dy = 0;
/*TODO*///			/* JCK 990717 END */
/*TODO*///			if (Watches[ s ] <= 0x100)
/*TODO*///				Watches[ s ] |= (0xFFFFFF00 & MAX_ADDRESS(WatchesCpuNo[ s ]));
/*TODO*///			else
/*TODO*///				Watches[ s ] -= 0x100;
/*TODO*///			AddCpuToWatch(s, str2[ s ]);
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_PGUP:
/*TODO*///		case KEYCODE_9_PAD:
/*TODO*///			/* JCK 990717 BEGIN */
/*TODO*///			dx = 0;
/*TODO*///			dy = 0;
/*TODO*///			/* JCK 990717 END */
/*TODO*///			if (Watches[ s ] >= 0xFF00)
/*TODO*///				Watches[ s ] |= (0xFFFF00FF & MAX_ADDRESS(WatchesCpuNo[ s ]));
/*TODO*///			else
/*TODO*///				Watches[ s ] += 0x100;
/*TODO*///			AddCpuToWatch(s, str2[ s ]);
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_8:
/*TODO*///			/* JCK 990717 BEGIN */
/*TODO*///			dx = 0;
/*TODO*///			dy = 0;
/*TODO*///			/* JCK 990717 END */
/*TODO*///			if (ADDRESS_BITS(WatchesCpuNo[s]) < 29) break;
/*TODO*///		case KEYCODE_7:
/*TODO*///			/* JCK 990717 BEGIN */
/*TODO*///			dx = 0;
/*TODO*///			dy = 0;
/*TODO*///			/* JCK 990717 END */
/*TODO*///			if (ADDRESS_BITS(WatchesCpuNo[s]) < 25) break;
/*TODO*///		case KEYCODE_6:
/*TODO*///			/* JCK 990717 BEGIN */
/*TODO*///			dx = 0;
/*TODO*///			dy = 0;
/*TODO*///			/* JCK 990717 END */
/*TODO*///			if (ADDRESS_BITS(WatchesCpuNo[s]) < 21) break;
/*TODO*///		case KEYCODE_5:
/*TODO*///			/* JCK 990717 BEGIN */
/*TODO*///			dx = 0;
/*TODO*///			dy = 0;
/*TODO*///			/* JCK 990717 END */
/*TODO*///			if (ADDRESS_BITS(WatchesCpuNo[s]) < 17) break;
/*TODO*///		case KEYCODE_4:
/*TODO*///			/* JCK 990717 BEGIN */
/*TODO*///			dx = 0;
/*TODO*///			dy = 0;
/*TODO*///			/* JCK 990717 END */
/*TODO*///			if (ADDRESS_BITS(WatchesCpuNo[s]) < 13) break;
/*TODO*///		case KEYCODE_3:
/*TODO*///		case KEYCODE_2:
/*TODO*///		case KEYCODE_1:
/*TODO*///			{
/*TODO*///				int addr = Watches[ s ];	/* copy address*/
/*TODO*///				int digit = (KEYCODE_8 - key);	/* if key is KEYCODE_8, digit = 0 */
/*TODO*///				int mask;
/*TODO*///
/*TODO*///				/* JCK 990717 BEGIN */
/*TODO*///				dx = 0;
/*TODO*///				dy = 0;
/*TODO*///				/* JCK 990717 END */
/*TODO*///
/*TODO*///				/* adjust digit based on cpu address range */
/*TODO*///				/* digit -= (6 - ADDRESS_BITS(0) / 4); */
/*TODO*///				digit -= (8 - (ADDRESS_BITS(WatchesCpuNo[s])+3) / 4);
/*TODO*///
/*TODO*///				mask = 0xF << (digit * 4);	/* if digit is 1, mask = 0xf0*/
/*TODO*///
/*TODO*///				do
/*TODO*///				{
/*TODO*///				if ((addr & mask) == mask)
/*TODO*///					/* wrap hex digit around to 0 if necessary */
/*TODO*///					addr &= ~mask;
/*TODO*///				else
/*TODO*///					/* otherwise bump hex digit by 1 */
/*TODO*///					addr += (0x1 << (digit * 4));
/*TODO*///				} while (addr > MAX_ADDRESS(WatchesCpuNo[s]));
/*TODO*///
/*TODO*///				Watches[ s ] = addr;
/*TODO*///				AddCpuToWatch(s, str2[ s ]);
/*TODO*///
/*TODO*///			}
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_9:
/*TODO*///			/* JCK 990717 BEGIN */
/*TODO*///			dx = 0;
/*TODO*///			dy = 0;
/*TODO*///			/* JCK 990717 END */
/*TODO*///			if (ManyCpus)
/*TODO*///			{
/*TODO*///				OldCpuNo = WatchesCpuNo[ s ];
/*TODO*///				if(WatchesCpuNo[ s ] == 0)
/*TODO*///					WatchesCpuNo[ s ] = cpu_gettotalcpu()-1;
/*TODO*///				else
/*TODO*///					WatchesCpuNo[ s ]--;
/*TODO*///
/*TODO*///				if (Watches[s] == MAX_ADDRESS(OldCpuNo))
/*TODO*///					Watches[s] = MAX_ADDRESS(WatchesCpuNo[s]);
/*TODO*///
/*TODO*///				AddCpuToWatch(s, str2[ s ]);
/*TODO*///
/*TODO*///				if (MAX_ADDRESS(WatchesCpuNo[s]) != MAX_ADDRESS(OldCpuNo))
/*TODO*///				{
/*TODO*///					ClearTextLine(0, dt[s].y);
/*TODO*///
/*TODO*///					/* dt[s].x = (MachWidth - FontWidth * strlen(str2[ s ])) / 2; */
/*TODO*///					  if (s < MAX_WATCHES / 2)
/*TODO*///					  {
/*TODO*///						dt[s].x = ((MachWidth / 2) - FontWidth * strlen(str2[ s ])) / 2;
/*TODO*///					  }
/*TODO*///					  else
/*TODO*///					  {
/*TODO*///						dt[s].x = ((MachWidth / 2) - FontWidth * strlen(str2[ s ])) / 2 + (MachWidth / 2);
/*TODO*///					  }
/*TODO*///
/*TODO*///					cheat_clearbitmap();
/*TODO*///
/*TODO*///					y = ChooseWatchHeader();
/*TODO*///				}
/*TODO*///			}
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_0:
/*TODO*///			/* JCK 990717 BEGIN */
/*TODO*///			dx = 0;
/*TODO*///			dy = 0;
/*TODO*///			/* JCK 990717 END */
/*TODO*///			if (ManyCpus)
/*TODO*///			{
/*TODO*///				OldCpuNo = WatchesCpuNo[ s ];
/*TODO*///				if(WatchesCpuNo[ s ] >= cpu_gettotalcpu()-1)
/*TODO*///					WatchesCpuNo[ s ] = 0;
/*TODO*///				else
/*TODO*///					WatchesCpuNo[ s ]++;
/*TODO*///
/*TODO*///				if (Watches[s] == MAX_ADDRESS(OldCpuNo))
/*TODO*///					Watches[s] = MAX_ADDRESS(WatchesCpuNo[s]);
/*TODO*///
/*TODO*///				AddCpuToWatch(s, str2[ s ]);
/*TODO*///
/*TODO*///				if (MAX_ADDRESS(WatchesCpuNo[s]) != MAX_ADDRESS(OldCpuNo))
/*TODO*///				{
/*TODO*///					ClearTextLine(0, dt[s].y);
/*TODO*///
/*TODO*///					/* dt[s].x = (MachWidth - FontWidth * strlen(str2[ s ])) / 2; */
/*TODO*///					  if (s < MAX_WATCHES / 2)
/*TODO*///					  {
/*TODO*///						dt[s].x = ((MachWidth / 2) - FontWidth * strlen(str2[ s ])) / 2;
/*TODO*///					  }
/*TODO*///					  else
/*TODO*///					  {
/*TODO*///						dt[s].x = ((MachWidth / 2) - FontWidth * strlen(str2[ s ])) / 2 + (MachWidth / 2);
/*TODO*///					  }
/*TODO*///
/*TODO*///					cheat_clearbitmap();
/*TODO*///
/*TODO*///					y = ChooseWatchHeader();
/*TODO*///				}
/*TODO*///			}
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_DEL:
/*TODO*///		case KEYCODE_DEL_PAD:
/*TODO*///			while (keyboard_pressed(key)); /* wait for key release */
/*TODO*///			/* JCK 990717 BEGIN */
/*TODO*///			dx = 0;
/*TODO*///			dy = 0;
/*TODO*///			/* JCK 990717 END */
/*TODO*///			OldCpuNo = WatchesCpuNo[ s ];
/*TODO*///			WatchesCpuNo[ s ] = 0;
/*TODO*///			Watches[ s ] = MAX_ADDRESS(WatchesCpuNo[ s ]);
/*TODO*///			AddCpuToWatch(s, str2[ s ]);
/*TODO*///
/*TODO*///			if (MAX_ADDRESS(WatchesCpuNo[s]) != MAX_ADDRESS(OldCpuNo))
/*TODO*///			{
/*TODO*///				ClearTextLine(0, dt[s].y);
/*TODO*///
/*TODO*///				/* dt[s].x = (MachWidth - FontWidth * strlen(str2[ s ])) / 2; */
/*TODO*///				  if (s < MAX_WATCHES / 2)
/*TODO*///				  {
/*TODO*///					dt[s].x = ((MachWidth / 2) - FontWidth * strlen(str2[ s ])) / 2;
/*TODO*///				  }
/*TODO*///				  else
/*TODO*///				  {
/*TODO*///					dt[s].x = ((MachWidth / 2) - FontWidth * strlen(str2[ s ])) / 2 + (MachWidth / 2);
/*TODO*///				  }
/*TODO*///
/*TODO*///				cheat_clearbitmap();
/*TODO*///
/*TODO*///				y = ChooseWatchHeader();
/*TODO*///			}
/*TODO*///
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_F1:
/*TODO*///			while (keyboard_pressed(key)); /* wait for key release */
/*TODO*///			/* JCK 990717 BEGIN */
/*TODO*///			dx = 0;
/*TODO*///			dy = 0;
/*TODO*///			/* JCK 990717 END */
/*TODO*///		  oldkey = 0;
/*TODO*///			if (Watches[s] == MAX_ADDRESS(WatchesCpuNo[s]))
/*TODO*///				break;
/*TODO*///
/*TODO*///			if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
/*TODO*///			{
/*TODO*///				break;
/*TODO*///			}
/*TODO*///
/*TODO*///			AddCpuToAddr(WatchesCpuNo[s], Watches[s],
/*TODO*///					RD_GAMERAM(WatchesCpuNo[s], Watches[s]), buf);
/*TODO*///
/*TODO*///			if (LoadedCheatTotal < MAX_LOADEDCHEATS-1)
/*TODO*///			{
/*TODO*///				set_cheat(&LoadedCheatTable[LoadedCheatTotal], NEW_CHEAT);
/*TODO*///				LoadedCheatTable[LoadedCheatTotal].CpuNo   = WatchesCpuNo[s];
/*TODO*///				LoadedCheatTable[LoadedCheatTotal].Address = Watches[s];
/*TODO*///				LoadedCheatTable[LoadedCheatTotal].Data    = RD_GAMERAM(WatchesCpuNo[s], Watches[s]);
/*TODO*///				strcpy(LoadedCheatTable[LoadedCheatTotal].Name, buf);
/*TODO*///				LoadedCheatTotal++;
/*TODO*///				xprintf(0, 0,YFOOT_WATCH,"%s Added",buf);
/*TODO*///			}
/*TODO*///			else
/*TODO*///				xprintf(0, 0,YFOOT_WATCH,"%s Not Added",buf);
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_F3:
/*TODO*///			while (keyboard_pressed(key));
/*TODO*///			/* JCK 990717 BEGIN */
/*TODO*///			dx = 0;
/*TODO*///			dy = 0;
/*TODO*///			/* JCK 990717 END */
/*TODO*///		  oldkey = 0;
/*TODO*///
/*TODO*///			if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
/*TODO*///			{
/*TODO*///				break;
/*TODO*///			}
/*TODO*///
/*TODO*///			for (i = 0; i < total; i++)
/*TODO*///				dt[i].color = DT_COLOR_WHITE;
/*TODO*///			displaytext (dt, 0,1);
/*TODO*///			sprintf(buffer, FormatAddr(WatchesCpuNo[s],0), Watches[s]);
/*TODO*///			xedit(0, YFOOT_WATCH, buffer, strlen(buffer), 1);
/*TODO*///			sscanf(buffer,"%X", &Watches[s]);
/*TODO*///			if (Watches[s] > MAX_ADDRESS(WatchesCpuNo[s]))
/*TODO*///				Watches[s] = MAX_ADDRESS(WatchesCpuNo[s]);
/*TODO*///			AddCpuToWatch(s, str2[ s ]);
/*TODO*///		  break;
/*TODO*///
/*TODO*///		case KEYCODE_F4:
/*TODO*///			while (keyboard_pressed(key)); /* wait for key release */
/*TODO*///			/* JCK 990717 BEGIN */
/*TODO*///			dx = 0;
/*TODO*///			dy = 0;
/*TODO*///			/* JCK 990717 END */
/*TODO*///		  oldkey = 0;
/*TODO*///
/*TODO*///			if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
/*TODO*///			{
/*TODO*///				break;
/*TODO*///			}
/*TODO*///
/*TODO*///			for (i=0;i<MAX_WATCHES;i++)
/*TODO*///			{
/*TODO*///				if (Watches[i] == MAX_ADDRESS(WatchesCpuNo[i]))
/*TODO*///				{
/*TODO*///					OldCpuNo = WatchesCpuNo[ i ];
/*TODO*///					WatchesCpuNo[i] = WatchesCpuNo[s];
/*TODO*///					Watches[i] = Watches[s];
/*TODO*///
/*TODO*///					AddCpuToWatch(i, str2[ i ]);
/*TODO*///
/*TODO*///					if (MAX_ADDRESS(WatchesCpuNo[i]) != MAX_ADDRESS(OldCpuNo))
/*TODO*///					{
/*TODO*///						ClearTextLine(0, dt[i].y);
/*TODO*///
/*TODO*///						/* dt[i].x = (MachWidth - FontWidth * strlen(str2[ i ])) / 2; */
/*TODO*///						  if (i < MAX_WATCHES / 2)
/*TODO*///						  {
/*TODO*///							dt[i].x = ((MachWidth / 2) - FontWidth * strlen(str2[ i ])) / 2;
/*TODO*///						  }
/*TODO*///						  else
/*TODO*///						  {
/*TODO*///							dt[i].x = ((MachWidth / 2) - FontWidth * strlen(str2[ i ])) / 2 + (MachWidth / 2);
/*TODO*///						  }
/*TODO*///
/*TODO*///						cheat_clearbitmap();
/*TODO*///
/*TODO*///						y = ChooseWatchHeader();
/*TODO*///					}
/*TODO*///
/*TODO*///					break;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_F6:
/*TODO*///			while (keyboard_pressed(key)); /* wait for key release */
/*TODO*///			/* JCK 990717 BEGIN */
/*TODO*///			dx = 0;
/*TODO*///			dy = 0;
/*TODO*///			/* JCK 990717 END */
/*TODO*///		  oldkey = 0;
/*TODO*///
/*TODO*///			if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
/*TODO*///			{
/*TODO*///				break;
/*TODO*///			}
/*TODO*///
/*TODO*///			for (i=0;i<MAX_WATCHES;i++)
/*TODO*///			{
/*TODO*///				if(LoadedCheatTotal > MAX_LOADEDCHEATS-1)
/*TODO*///					break;
/*TODO*///				if (Watches[i] != MAX_ADDRESS(WatchesCpuNo[i]))
/*TODO*///				{
/*TODO*///					countAdded++;
/*TODO*///
/*TODO*///					AddCpuToAddr(WatchesCpuNo[i], Watches[i],
/*TODO*///							RD_GAMERAM(WatchesCpuNo[i], Watches[i]), buf);
/*TODO*///
/*TODO*///					set_cheat(&LoadedCheatTable[LoadedCheatTotal], NEW_CHEAT);
/*TODO*///					LoadedCheatTable[LoadedCheatTotal].CpuNo   = WatchesCpuNo[i];
/*TODO*///					LoadedCheatTable[LoadedCheatTotal].Address = Watches[i];
/*TODO*///					LoadedCheatTable[LoadedCheatTotal].Data    = RD_GAMERAM(WatchesCpuNo[i], Watches[i]);
/*TODO*///					strcpy(LoadedCheatTable[LoadedCheatTotal].Name,buf);
/*TODO*///					LoadedCheatTotal++;
/*TODO*///				}
/*TODO*///
/*TODO*///			}
/*TODO*///			xprintf(0, 0,YFOOT_WATCH,"%d Added",countAdded);
/*TODO*///
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_F7:
/*TODO*///			while (keyboard_pressed(key)); /* wait for key release */
/*TODO*///			/* JCK 990717 BEGIN */
/*TODO*///			dx = 0;
/*TODO*///			dy = 0;
/*TODO*///			/* JCK 990717 END */
/*TODO*///		  oldkey = 0;
/*TODO*///
/*TODO*///			if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
/*TODO*///			{
/*TODO*///				break;
/*TODO*///			}
/*TODO*///
/*TODO*///			for (i=0;i<MAX_WATCHES;i++)
/*TODO*///			{
/*TODO*///				WatchesCpuNo[ i ] = 0;
/*TODO*///				Watches[ i ] = MAX_ADDRESS(WatchesCpuNo[ i ]);
/*TODO*///				AddCpuToWatch(i, str2[ i ]);
/*TODO*///			}
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_F10:
/*TODO*///			while (keyboard_pressed(key)); /* wait for key release */
/*TODO*///			/* JCK 990717 BEGIN */
/*TODO*///			dx = 0;
/*TODO*///			dy = 0;
/*TODO*///			/* JCK 990717 END */
/*TODO*///		  oldkey = 0;
/*TODO*///			ChooseWatchHelp();								/* Show Help */
/*TODO*///			y = ChooseWatchHeader();
/*TODO*///			break;
/*TODO*///
/*TODO*///		case KEYCODE_ENTER:
/*TODO*///		case KEYCODE_ENTER_PAD:
/*TODO*///			/* JCK 990717 BEGIN */
/*TODO*///			dx = 0;
/*TODO*///			dy = 0;
/*TODO*///			/* JCK 990717 END */
/*TODO*///			if (s == 0)
/*TODO*///				break;
/*TODO*///			OldCpuNo = WatchesCpuNo[ s ];
/*TODO*///			WatchesCpuNo[ s ] = WatchesCpuNo[ s - 1 ];
/*TODO*///			Watches[ s ] = Watches[ s - 1 ];
/*TODO*///			AddCpuToWatch(s, str2[ s ]);
/*TODO*///
/*TODO*///			if (MAX_ADDRESS(WatchesCpuNo[s]) != MAX_ADDRESS(OldCpuNo))
/*TODO*///			{
/*TODO*///				ClearTextLine(0, dt[s].y);
/*TODO*///
/*TODO*///				/* dt[s].x = (MachWidth - FontWidth * strlen(str2[ s ])) / 2; */
/*TODO*///				  if (s < MAX_WATCHES / 2)
/*TODO*///				  {
/*TODO*///					dt[s].x = ((MachWidth / 2) - FontWidth * strlen(str2[ s ])) / 2;
/*TODO*///				  }
/*TODO*///				  else
/*TODO*///				  {
/*TODO*///					dt[s].x = ((MachWidth / 2) - FontWidth * strlen(str2[ s ])) / 2 + (MachWidth / 2);
/*TODO*///				  }
/*TODO*///
/*TODO*///				cheat_clearbitmap();
/*TODO*///
/*TODO*///				y = ChooseWatchHeader();
/*TODO*///			}
/*TODO*///
/*TODO*///			break;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* Set Watch Flag */
/*TODO*///	WatchesFlag = 0;
/*TODO*///	for(i = 0;i < MAX_WATCHES;i ++)
/*TODO*///		if(Watches[i] != MAX_ADDRESS(WatchesCpuNo[i]))
/*TODO*///		{
/*TODO*///			WatchesFlag = 1;
/*TODO*///			WatchEnabled = 1;
/*TODO*///		}
/*TODO*///
/*TODO*///  } while (done != 2);
/*TODO*///
/*TODO*///  while (keyboard_pressed(key)) ; /* wait for key release */
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///
/*TODO*///  Machine->orientation = trueorientation;
/*TODO*///}
/*TODO*///
/*TODO*///
    public static int cheat_menu() {
        /*TODO*///  int x,y;
/*TODO*///  int s,key,done;
/*TODO*///  int total;
/*TODO*///
/*TODO*///  char *paDisplayText[] = {
/*TODO*///		"Load And/Or Enable A Cheat",
/*TODO*///		"Start A New Cheat Search",
/*TODO*///		"Continue Search",
/*TODO*///		"View Last Results",
/*TODO*///		"Restore Previous Results",
/*TODO*///		"Memory Watch",
/*TODO*///		"",
/*TODO*///		"General Help",
/*TODO*///		"",
/*TODO*///		"Return To Main Menu",
/*TODO*///		0 };
/*TODO*///
/*TODO*///  struct DisplayText dt[20];
/*TODO*///
/*TODO*///  cheat_save_frameskips();
/*TODO*///
/*TODO*///  total = create_menu(paDisplayText, dt, FIRSTPOS);
/*TODO*///  x = dt[total-1].x;
/*TODO*///  y = dt[total-1].y + 2 * FontHeight;
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///
/*TODO*///  DisplayActiveCheats(y);
/*TODO*///
/*TODO*///  oldkey = 0;
/*TODO*///
/*TODO*///  s = SaveMenu;
/*TODO*///  done = 0;
/*TODO*///  do
/*TODO*///  {
/*TODO*///	key = SelectMenu (&s, dt, 0, 0, 0, total-1, 0, &done);
/*TODO*///	switch (key)
/*TODO*///	{
/*TODO*///		case KEYCODE_ENTER:
/*TODO*///		case KEYCODE_ENTER_PAD:
/*TODO*///			switch (s)
/*TODO*///			{
/*TODO*///				case 0:
/*TODO*///					SelectCheat();
/*TODO*///					done = 0;
/*TODO*///					DisplayActiveCheats(y);
/*TODO*///					break;
/*TODO*///
/*TODO*///				case 1:
/*TODO*///					StartSearch();
/*TODO*///					done = 0;
/*TODO*///					DisplayActiveCheats(y);
/*TODO*///					break;
/*TODO*///
/*TODO*///				case 2:
/*TODO*///					ContinueSearch(1, 0);
/*TODO*///					done = 0;
/*TODO*///					DisplayActiveCheats(y);
/*TODO*///					break;
/*TODO*///
/*TODO*///				case 3:
/*TODO*///					ContinueSearch(1, 1);
/*TODO*///					done = 0;
/*TODO*///					DisplayActiveCheats(y);
/*TODO*///					break;
/*TODO*///
/*TODO*///				case 4:
/*TODO*///					RestoreSearch();
/*TODO*///					done = 0;
/*TODO*///					DisplayActiveCheats(y);
/*TODO*///					break;
/*TODO*///
/*TODO*///				case 5:
/*TODO*///					ChooseWatch();
/*TODO*///					done = 0;
/*TODO*///					DisplayActiveCheats(y);
/*TODO*///					break;
/*TODO*///
/*TODO*///				case 7:
/*TODO*///					DisplayHelpFile();
/*TODO*///					done = 0;
/*TODO*///					DisplayActiveCheats(y);
/*TODO*///					break;
/*TODO*///
/*TODO*///				case 9:
/*TODO*///					done = 1;
/*TODO*///				  s = 0;
/*TODO*///					break;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///	}
/*TODO*///  } while ((done != 1) && (done != 2));
/*TODO*///
/*TODO*///  SaveMenu = s;
/*TODO*///
/*TODO*///  /* clear the screen before returning */
/*TODO*///  osd_clearbitmap(Machine->scrbitmap);
/*TODO*///
/*TODO*///  cheat_rest_frameskips();
/*TODO*///
/*TODO*///  if (done == 2)
/*TODO*///	return 1;
/*TODO*///  else
	return 0;
    }

    /*TODO*///
/*TODO*////* Free allocated arrays */
/*TODO*///void StopCheat(void)
/*TODO*///{
/*TODO*///  reset_table (StartRam);
/*TODO*///  reset_table (BackupRam);
/*TODO*///  reset_table (FlagTable);
/*TODO*///
/*TODO*///  reset_table (OldBackupRam);
/*TODO*///  reset_table (OldFlagTable);
/*TODO*///
/*TODO*///  reset_texttable (HelpLine);
/*TODO*///}
/*TODO*///
    public static void DoCheat() {
        /*TODO*///	int i;
/*TODO*///	char buf[80];
/*TODO*///
/*TODO*///
/*TODO*///	DisplayWatches(0, &WatchX, &WatchY, buf, MAX_WATCHES, 0, 0);
/*TODO*///
/*TODO*///	/* Affect the memory */
/*TODO*///	for (i = 0; CheatEnabled == 1 && i < ActiveCheatTotal;i ++)
/*TODO*///	{
/*TODO*///		if (	(ActiveCheatTable[i].Special == 0)		||
/*TODO*///			(ActiveCheatTable[i].Special == OFFSET_LINK_CHEAT)	)
/*TODO*///		{
/*TODO*///			WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///				ActiveCheatTable[i].Address,
/*TODO*///				ActiveCheatTable[i].Data);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if (ActiveCheatTable[i].Count == 0)
/*TODO*///			{
/*TODO*///				/* Check special function */
/*TODO*///				switch(ActiveCheatTable[i].Special)
/*TODO*///				{
/*TODO*///					case 1:
/*TODO*///					case 1 + OFFSET_LINK_CHEAT:
/*TODO*///						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///						ActiveCheatTable[i].Address,
/*TODO*///								ActiveCheatTable[i].Data);
/*TODO*///					DeleteActiveCheatFromTable(i);
/*TODO*///						break;
/*TODO*///					case 2:
/*TODO*///					case 2 + OFFSET_LINK_CHEAT:
/*TODO*///						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///						ActiveCheatTable[i].Address,
/*TODO*///								ActiveCheatTable[i].Data);
/*TODO*///						ActiveCheatTable[i].Count = 1*60;
/*TODO*///						break;
/*TODO*///					case 3:
/*TODO*///					case 3 + OFFSET_LINK_CHEAT:
/*TODO*///						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///						ActiveCheatTable[i].Address,
/*TODO*///								ActiveCheatTable[i].Data);
/*TODO*///						ActiveCheatTable[i].Count = 2*60;
/*TODO*///						break;
/*TODO*///					case 4:
/*TODO*///					case 4 + OFFSET_LINK_CHEAT:
/*TODO*///						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///						ActiveCheatTable[i].Address,
/*TODO*///								ActiveCheatTable[i].Data);
/*TODO*///						ActiveCheatTable[i].Count = 5*60;
/*TODO*///						break;
/*TODO*///
/*TODO*///					/* 5,6,7 check if the value has changed, if yes, start a timer
/*TODO*///						when the timer end, change the location*/
/*TODO*///					case 5:
/*TODO*///					case 5 + OFFSET_LINK_CHEAT:
/*TODO*///						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///							ActiveCheatTable[i].Address) !=
/*TODO*///						ActiveCheatTable[i].Data)
/*TODO*///						{
/*TODO*///							ActiveCheatTable[i].Count = 1*60;
/*TODO*///							ActiveCheatTable[i].Special += 1000;
/*TODO*///						}
/*TODO*///						break;
/*TODO*///					case 6:
/*TODO*///					case 6 + OFFSET_LINK_CHEAT:
/*TODO*///						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///							ActiveCheatTable[i].Address) !=
/*TODO*///						ActiveCheatTable[i].Data)
/*TODO*///						{
/*TODO*///							ActiveCheatTable[i].Count = 2*60;
/*TODO*///							ActiveCheatTable[i].Special += 1000;
/*TODO*///						}
/*TODO*///						break;
/*TODO*///					case 7:
/*TODO*///					case 7 + OFFSET_LINK_CHEAT:
/*TODO*///						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///							ActiveCheatTable[i].Address) !=
/*TODO*///						ActiveCheatTable[i].Data)
/*TODO*///						{
/*TODO*///							ActiveCheatTable[i].Count = 5*60;
/*TODO*///							ActiveCheatTable[i].Special += 1000;
/*TODO*///						}
/*TODO*///						break;
/*TODO*///
/*TODO*///					/* 8,9,10,11 do not change the location if the value change by X every frames
/*TODO*///					   This is to try to not change the value of an energy bar
/*TODO*///					   when a bonus is awarded to it at the end of a level
/*TODO*///					   See Kung Fu Master*/
/*TODO*///					case 8:
/*TODO*///					case 8 + OFFSET_LINK_CHEAT:
/*TODO*///						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///							ActiveCheatTable[i].Address) !=
/*TODO*///						ActiveCheatTable[i].Data)
/*TODO*///						{
/*TODO*///							ActiveCheatTable[i].Count = 1;
/*TODO*///							ActiveCheatTable[i].Special += 1000;
/*TODO*///							ActiveCheatTable[i].Backup =
/*TODO*///								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///								ActiveCheatTable[i].Address);
/*TODO*///						}
/*TODO*///						break;
/*TODO*///					case 9:
/*TODO*///					case 9 + OFFSET_LINK_CHEAT:
/*TODO*///						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///							ActiveCheatTable[i].Address) !=
/*TODO*///						ActiveCheatTable[i].Data)
/*TODO*///						{
/*TODO*///							ActiveCheatTable[i].Count = 1;
/*TODO*///							ActiveCheatTable[i].Special += 1000;
/*TODO*///							ActiveCheatTable[i].Backup =
/*TODO*///								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///								ActiveCheatTable[i].Address);
/*TODO*///						}
/*TODO*///						break;
/*TODO*///					case 10:
/*TODO*///					case 10 + OFFSET_LINK_CHEAT:
/*TODO*///						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///							ActiveCheatTable[i].Address) !=
/*TODO*///						ActiveCheatTable[i].Data)
/*TODO*///						{
/*TODO*///							ActiveCheatTable[i].Count = 1;
/*TODO*///							ActiveCheatTable[i].Special += 1000;
/*TODO*///							ActiveCheatTable[i].Backup =
/*TODO*///								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///								ActiveCheatTable[i].Address);
/*TODO*///						}
/*TODO*///						break;
/*TODO*///					case 11:
/*TODO*///					case 11 + OFFSET_LINK_CHEAT:
/*TODO*///						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///							ActiveCheatTable[i].Address) !=
/*TODO*///						ActiveCheatTable[i].Data)
/*TODO*///						{
/*TODO*///							ActiveCheatTable[i].Count = 1;
/*TODO*///							ActiveCheatTable[i].Special += 1000;
/*TODO*///							ActiveCheatTable[i].Backup =
/*TODO*///								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///								ActiveCheatTable[i].Address);
/*TODO*///						}
/*TODO*///						break;
/*TODO*///
/*TODO*///					case 20:
/*TODO*///					case 20 + OFFSET_LINK_CHEAT:
/*TODO*///						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///						ActiveCheatTable[i].Address,
/*TODO*///								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///								ActiveCheatTable[i].Address) |
/*TODO*///									ActiveCheatTable[i].Data);
/*TODO*///						break;
/*TODO*///					case 21:
/*TODO*///					case 21 + OFFSET_LINK_CHEAT:
/*TODO*///						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///						ActiveCheatTable[i].Address,
/*TODO*///								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///								ActiveCheatTable[i].Address) |
/*TODO*///									ActiveCheatTable[i].Data);
/*TODO*///					DeleteActiveCheatFromTable(i);
/*TODO*///						break;
/*TODO*///					case 22:
/*TODO*///					case 22 + OFFSET_LINK_CHEAT:
/*TODO*///						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///						ActiveCheatTable[i].Address,
/*TODO*///								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///								ActiveCheatTable[i].Address) |
/*TODO*///								ActiveCheatTable[i].Data);
/*TODO*///						ActiveCheatTable[i].Count = 1*60;
/*TODO*///						break;
/*TODO*///					case 23:
/*TODO*///					case 23 + OFFSET_LINK_CHEAT:
/*TODO*///						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///						ActiveCheatTable[i].Address,
/*TODO*///								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///								ActiveCheatTable[i].Address) |
/*TODO*///								ActiveCheatTable[i].Data);
/*TODO*///						ActiveCheatTable[i].Count = 2*60;
/*TODO*///						break;
/*TODO*///					case 24:
/*TODO*///					case 24 + OFFSET_LINK_CHEAT:
/*TODO*///						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///						ActiveCheatTable[i].Address,
/*TODO*///								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///								ActiveCheatTable[i].Address) |
/*TODO*///								ActiveCheatTable[i].Data);
/*TODO*///						ActiveCheatTable[i].Count = 5*60;
/*TODO*///						break;
/*TODO*///					case 40:
/*TODO*///					case 40 + OFFSET_LINK_CHEAT:
/*TODO*///						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///						ActiveCheatTable[i].Address,
/*TODO*///								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///								ActiveCheatTable[i].Address) &
/*TODO*///									~ActiveCheatTable[i].Data);
/*TODO*///						break;
/*TODO*///					case 41:
/*TODO*///					case 41 + OFFSET_LINK_CHEAT:
/*TODO*///						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///						ActiveCheatTable[i].Address,
/*TODO*///								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///								ActiveCheatTable[i].Address) &
/*TODO*///									~ActiveCheatTable[i].Data);
/*TODO*///					DeleteActiveCheatFromTable(i);
/*TODO*///						break;
/*TODO*///					case 42:
/*TODO*///					case 42 + OFFSET_LINK_CHEAT:
/*TODO*///						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///						ActiveCheatTable[i].Address,
/*TODO*///								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///								ActiveCheatTable[i].Address) &
/*TODO*///								~ActiveCheatTable[i].Data);
/*TODO*///						ActiveCheatTable[i].Count = 1*60;
/*TODO*///						break;
/*TODO*///					case 43:
/*TODO*///					case 43 + OFFSET_LINK_CHEAT:
/*TODO*///						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///						ActiveCheatTable[i].Address,
/*TODO*///								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///								ActiveCheatTable[i].Address) &
/*TODO*///									~ActiveCheatTable[i].Data);
/*TODO*///						ActiveCheatTable[i].Count = 2*60;
/*TODO*///						break;
/*TODO*///					case 44:
/*TODO*///					case 44 + OFFSET_LINK_CHEAT:
/*TODO*///						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///						ActiveCheatTable[i].Address,
/*TODO*///								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///								ActiveCheatTable[i].Address) &
/*TODO*///								~ActiveCheatTable[i].Data);
/*TODO*///						ActiveCheatTable[i].Count = 5*60;
/*TODO*///						break;
/*TODO*///
/*TODO*///					case 60:
/*TODO*///					case 61:
/*TODO*///					case 62:
/*TODO*///					case 63:
/*TODO*///					case 64:
/*TODO*///					case 65:
/*TODO*///						ActiveCheatTable[i].Count = 1;
/*TODO*///						ActiveCheatTable[i].Special += 1000;
/*TODO*///						ActiveCheatTable[i].Backup =
/*TODO*///							RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///							ActiveCheatTable[i].Address);
/*TODO*///						break;
/*TODO*///					case 70:
/*TODO*///					case 71:
/*TODO*///					case 72:
/*TODO*///					case 73:
/*TODO*///					case 74:
/*TODO*///					case 75:
/*TODO*///						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///						ActiveCheatTable[i].Address,
/*TODO*///								ActiveCheatTable[i].Data);
/*TODO*///					DeleteActiveCheatFromTable(i);
/*TODO*///						break;
/*TODO*///
/*TODO*///						/*Special case, linked with 5,6,7 */
/*TODO*///					case 1005:
/*TODO*///					case 1005 + OFFSET_LINK_CHEAT:	  /* Linked cheat */
/*TODO*///						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///						ActiveCheatTable[i].Address,
/*TODO*///								ActiveCheatTable[i].Data);
/*TODO*///						ActiveCheatTable[i].Special -= 1000;
/*TODO*///						break;
/*TODO*///					case 1006:
/*TODO*///					case 1006 + OFFSET_LINK_CHEAT:	  /* Linked cheat */
/*TODO*///						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///						ActiveCheatTable[i].Address,
/*TODO*///								ActiveCheatTable[i].Data);
/*TODO*///						ActiveCheatTable[i].Special -= 1000;
/*TODO*///						break;
/*TODO*///					case 1007:
/*TODO*///					case 1007 + OFFSET_LINK_CHEAT:	  /* Linked cheat */
/*TODO*///						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///						ActiveCheatTable[i].Address,
/*TODO*///								ActiveCheatTable[i].Data);
/*TODO*///						ActiveCheatTable[i].Special -= 1000;
/*TODO*///						break;
/*TODO*///
/*TODO*///					/*Special case, linked with 8,9,10,11 */
/*TODO*///					/* Change the memory only if the memory decreased by X */
/*TODO*///					case 1008:
/*TODO*///					case 1008 + OFFSET_LINK_CHEAT:	  /* Linked cheat */
/*TODO*///						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///								ActiveCheatTable[i].Address) !=
/*TODO*///						ActiveCheatTable[i].Backup-1)
/*TODO*///							WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///							ActiveCheatTable[i].Address,
/*TODO*///									ActiveCheatTable[i].Data);
/*TODO*///						ActiveCheatTable[i].Special -= 1000;
/*TODO*///						break;
/*TODO*///					case 1009:
/*TODO*///					case 1009 + OFFSET_LINK_CHEAT:	  /* Linked cheat */
/*TODO*///						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///							ActiveCheatTable[i].Address) !=
/*TODO*///						ActiveCheatTable[i].Backup-2)
/*TODO*///							WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///							ActiveCheatTable[i].Address,
/*TODO*///									ActiveCheatTable[i].Data);
/*TODO*///						ActiveCheatTable[i].Special -= 1000;
/*TODO*///						break;
/*TODO*///					case 1010:
/*TODO*///					case 1010 + OFFSET_LINK_CHEAT:	  /* Linked cheat */
/*TODO*///						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///							ActiveCheatTable[i].Address) !=
/*TODO*///						ActiveCheatTable[i].Backup-3)
/*TODO*///							WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///							ActiveCheatTable[i].Address,
/*TODO*///									ActiveCheatTable[i].Data);
/*TODO*///						ActiveCheatTable[i].Special -= 1000;
/*TODO*///						break;
/*TODO*///					case 1011:
/*TODO*///					case 1011 + OFFSET_LINK_CHEAT:	  /* Linked cheat */
/*TODO*///						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///							ActiveCheatTable[i].Address) !=
/*TODO*///						ActiveCheatTable[i].Backup-4)
/*TODO*///							WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///							ActiveCheatTable[i].Address,
/*TODO*///									ActiveCheatTable[i].Data);
/*TODO*///						ActiveCheatTable[i].Special -= 1000;
/*TODO*///						break;
/*TODO*///
/*TODO*///					/*Special case, linked with 60 .. 65 */
/*TODO*///					/* Change the memory only if the memory has changed since the last backup */
/*TODO*///					case 1060:
/*TODO*///					case 1061:
/*TODO*///					case 1062:
/*TODO*///					case 1063:
/*TODO*///					case 1064:
/*TODO*///					case 1065:
/*TODO*///						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///							ActiveCheatTable[i].Address) !=
/*TODO*///						ActiveCheatTable[i].Backup)
/*TODO*///						{
/*TODO*///							WR_GAMERAM (ActiveCheatTable[i].CpuNo,
/*TODO*///							ActiveCheatTable[i].Address,
/*TODO*///									ActiveCheatTable[i].Data);
/*TODO*///					DeleteActiveCheatFromTable(i);
/*TODO*///						}
/*TODO*///						break;
/*TODO*///
/*TODO*///			}	/* end switch */
/*TODO*///		} /* end if (ActiveCheatTable[i].Count == 0) */
/*TODO*///		else
/*TODO*///		{
/*TODO*///			ActiveCheatTable[i].Count--;
/*TODO*///		}
/*TODO*///		} /* end else */
/*TODO*///	} /* end for */
/*TODO*///
/*TODO*///  /* KEYCODE_CHEAT_TOGGLE Enable/Disable the active cheats on the fly. Required for some cheat */
/*TODO*///  if (input_ui_pressed(IPT_UI_TOGGLE_CHEAT) && ActiveCheatTotal)
/*TODO*///  {
/*TODO*///	  CheatEnabled ^= 1;
/*TODO*///	  usrintf_showmessage("Cheats %s", (CheatEnabled ? "On" : "Off"));
/*TODO*///  }
/*TODO*///
/*TODO*///  /* KEYCODE_INSERT toggles the Watch display ON */
/*TODO*///  if ( keyboard_pressed_memory( KEYCODE_INSERT ) && (WatchEnabled == 0) )
/*TODO*///  {
/*TODO*///	WatchEnabled = 1;
/*TODO*///  }
/*TODO*///  /* KEYCODE_DEL toggles the Watch display OFF */
/*TODO*///  if ( keyboard_pressed_memory( KEYCODE_DEL ) && (WatchEnabled != 0) ){
/*TODO*///	WatchEnabled = 0;
/*TODO*///  }
/*TODO*///
/*TODO*///  /* KEYCODE_HOME loads the main menu of the cheat engine */
/*TODO*///  if ( keyboard_pressed_memory( KEYCODE_HOME ) )
/*TODO*///  {
/*TODO*///	osd_sound_enable(0);
/*TODO*///	cheat_menu();
/*TODO*///	osd_sound_enable(1);
/*TODO*///  }
/*TODO*///
/*TODO*///  /* KEYCODE_END loads the "Continue Search" sub-menu of the cheat engine */
/*TODO*///  if ( keyboard_pressed_memory( KEYCODE_END ) )
/*TODO*///  {
/*TODO*///	osd_sound_enable(0);
/*TODO*///	ContinueSearch(0, 0);
/*TODO*///	osd_sound_enable(1);
/*TODO*///  }
/*TODO*///
    }
    /*TODO*///
/*TODO*///
/*TODO*///void ShowHelp(int LastHelpLine, struct TextLine *table)
/*TODO*///{
/*TODO*///  int LineNumber = 0;
/*TODO*///  int LinePerPage = MachHeight / FontHeight - 6;
/*TODO*///  int yFirst = 0;
/*TODO*///  int yPos;
/*TODO*///  int key = 0;
/*TODO*///  int done = 0;
/*TODO*///  struct TextLine *txt;
/*TODO*///  char buffer[40];
/*TODO*///  struct DisplayText dt[2];
/*TODO*///
/*TODO*///  sprintf(buffer, "%s Return to Main Menu %s", lefthilight, righthilight);
/*TODO*///  dt[0].text = buffer;
/*TODO*///  dt[0].color = DT_COLOR_WHITE;
/*TODO*///  dt[0].x = (MachWidth - FontWidth * strlen(buffer)) / 2;
/*TODO*///  dt[0].y = MachHeight - 3 * FontHeight;
/*TODO*///  dt[1].text = 0;
/*TODO*///
/*TODO*///  oldkey = 0;
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///
/*TODO*///  if (!LastHelpLine)
/*TODO*///  {
/*TODO*///	yPos = (MachHeight - FontHeight) / 2 - FontHeight;
/*TODO*///	xprintf(0, 0, yPos, "No Help Available !");
/*TODO*///	displaytext(dt,0,1);
/*TODO*///	key = keyboard_read_sync();
/*TODO*///	while (keyboard_pressed(key));
/*TODO*///  }
/*TODO*///  else
/*TODO*///  {
/*TODO*///	if (LastHelpLine < LinePerPage)
/*TODO*///	{
/*TODO*///		yFirst = ((LinePerPage - LastHelpLine) / 2) * FontHeight;
/*TODO*///		LinePerPage = LastHelpLine;
/*TODO*///	}
/*TODO*///	done = 0;
/*TODO*///	do
/*TODO*///	{
/*TODO*///		if (key != NOVALUE)
/*TODO*///		{
/*TODO*///			cheat_clearbitmap();
/*TODO*///			yPos = yFirst;
/*TODO*///			if ((LineNumber > 0) && (LastHelpLine > LinePerPage))
/*TODO*///			{
/*TODO*///				xprintf (0, 0, yPos, uparrow);
/*TODO*///			}
/*TODO*///			yPos += FontHeight;
/*TODO*///			for (txt = table; txt->data; txt++)
/*TODO*///			{
/*TODO*///				if (txt->number >= LineNumber + LinePerPage)
/*TODO*///					break;
/*TODO*///				if (txt->number >= LineNumber)
/*TODO*///				{
/*TODO*///					xprintf (0, 0, yPos, "%-30s", txt->data);
/*TODO*///					yPos += FontHeight;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			if (LineNumber < LastHelpLine - LinePerPage)
/*TODO*///			{
/*TODO*///				xprintf (0, 0, yPos, downarrow);
/*TODO*///			}
/*TODO*///			displaytext(dt,0,1);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			oldkey = 0;
/*TODO*///		}
/*TODO*///		key = cheat_readkey();	  /* MSH 990217 */
/*TODO*///		switch (key)
/*TODO*///		{
/*TODO*///			case KEYCODE_DOWN:
/*TODO*///			case KEYCODE_2_PAD:
/*TODO*///				if (LineNumber < LastHelpLine - LinePerPage)
/*TODO*///				{
/*TODO*///					LineNumber ++;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					key = NOVALUE;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_UP:
/*TODO*///			case KEYCODE_8_PAD:
/*TODO*///				if (LineNumber > 0)
/*TODO*///				{
/*TODO*///					LineNumber --;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					key = NOVALUE;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_HOME:
/*TODO*///			case KEYCODE_7_PAD:
/*TODO*///				if (LineNumber != 0)
/*TODO*///				{
/*TODO*///					LineNumber = 0;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					key = NOVALUE;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_END:
/*TODO*///			case KEYCODE_1_PAD:
/*TODO*///				if (LineNumber != LastHelpLine - LinePerPage)
/*TODO*///				{
/*TODO*///					LineNumber = LastHelpLine - LinePerPage;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					key = NOVALUE;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_PGDN:
/*TODO*///			case KEYCODE_3_PAD:
/*TODO*///				while (keyboard_pressed(key)); /* wait for key release */
/*TODO*///				if (LineNumber < LastHelpLine - LinePerPage)
/*TODO*///				{
/*TODO*///					LineNumber += LinePerPage;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					key = NOVALUE;
/*TODO*///				}
/*TODO*///				if (LineNumber > LastHelpLine - LinePerPage)
/*TODO*///				{
/*TODO*///					LineNumber = LastHelpLine - LinePerPage;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///
/*TODO*///			case KEYCODE_PGUP:
/*TODO*///			case KEYCODE_9_PAD:
/*TODO*///				while (keyboard_pressed(key)); /* wait for key release */
/*TODO*///				if (LineNumber > 0)
/*TODO*///				{
/*TODO*///					LineNumber -= LinePerPage;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					key = NOVALUE;
/*TODO*///				}
/*TODO*///				if (LineNumber < 0)
/*TODO*///				{
/*TODO*///					LineNumber = 0;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case KEYCODE_ESC:
/*TODO*///			case KEYCODE_TAB:
/*TODO*///			case KEYCODE_ENTER:
/*TODO*///			case KEYCODE_ENTER_PAD:
/*TODO*///				while (keyboard_pressed(key));
/*TODO*///				oldkey = 0;
/*TODO*///				done = 1;
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				key = NOVALUE;
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	} while (done == 0);
/*TODO*///
/*TODO*///	while (keyboard_pressed(key));
/*TODO*///  }
/*TODO*///
/*TODO*///  cheat_clearbitmap();
/*TODO*///}
/*TODO*///
/*TODO*///void CheatListHelp (void)
/*TODO*///{
/*TODO*///	int LastHelpLine;
/*TODO*///	char *paDisplayText[] = {
/*TODO*///		"        Cheat List Help" ,
/*TODO*///		"" ,
/*TODO*///		"Delete:" ,
/*TODO*///		"  Delete the selected Cheat" ,
/*TODO*///		"  from the Cheat List." ,
/*TODO*///		"  (Not from the Cheat File!)" ,
/*TODO*///		"" ,
/*TODO*///		"Add:" ,
/*TODO*///		"  Add a new (blank) Cheat to" ,
/*TODO*///		"  the Cheat List." ,
/*TODO*///		"" ,
/*TODO*///		"Save (F1):" ,
/*TODO*///		"  Save the selected Cheat in" ,
/*TODO*///		"  the Cheat File." ,
/*TODO*///		"" ,
/*TODO*///		"Watch (F2):" ,
/*TODO*///		"  Activate a Memory Watcher" ,
/*TODO*///		"  at the address that the" ,
/*TODO*///		"  selected Cheat modifies." ,
/*TODO*///		"" ,
/*TODO*///		"Edit (F3):" ,
/*TODO*///		"  Edit the Properties of the" ,
/*TODO*///		"  selected Cheat." ,
/*TODO*///		"" ,
/*TODO*///		"Copy (F4):" ,
/*TODO*///		"  Copy the selected Cheat" ,
/*TODO*///		"  to the Cheat List." ,
/*TODO*///		"" ,
/*TODO*///		"Load (F5):" ,
/*TODO*///		"  Load a Cheat Database" ,
/*TODO*///		"" ,
/*TODO*///		"Save All (F6):" ,
/*TODO*///		"  Save all the Cheats in" ,
/*TODO*///		"  the Cheat File." ,
/*TODO*///		"" ,
/*TODO*///		"Del All (F7):" ,
/*TODO*///		"  Remove all the active Cheats" ,
/*TODO*///		"" ,
/*TODO*///		"Reload (F8):" ,
/*TODO*///		"  Reload the Cheat Database" ,
/*TODO*///		"" ,
/*TODO*///		"Rename (F9):" ,
/*TODO*///		"  Rename the Cheat Database" ,
/*TODO*///		"" ,
/*TODO*///		"Help (F10):" ,
/*TODO*///		"  Display this help" ,
/*TODO*///		"" ,
/*TODO*///		"Sologame ON/OFF (F11):" ,
/*TODO*///		"  Toggles this option ON/OFF." ,
/*TODO*///		"  When Sologame is ON, only" ,
/*TODO*///		"  Cheats for Player 1 are" ,
/*TODO*///		"  Loaded from the Cheat File.",
/*TODO*///		"" ,
/*TODO*///		"Info (F12):" ,
/*TODO*///		"  Display Info on a Cheat" ,
/*TODO*///		"" ,
/*TODO*///		"More info (+):" ,
/*TODO*///		"  Display the Extra Description" ,
/*TODO*///		"  of a Cheat if any." ,
/*TODO*///		"" ,
/*TODO*///		"Add from file (Shift+F5):" ,
/*TODO*///		"  Add the Cheats from a Cheat" ,
/*TODO*///		"  Database to the current" ,
/*TODO*///		"  Cheat Database." ,
/*TODO*///		"  (Only In Memory !)" ,
/*TODO*///		0 };
/*TODO*///
/*TODO*///	LastHelpLine = CreateHelp(paDisplayText, HelpLine);
/*TODO*///	ShowHelp(LastHelpLine, HelpLine);
/*TODO*///	reset_texttable (HelpLine);
/*TODO*///}
/*TODO*///
/*TODO*///void CheatListHelpEmpty (void)
/*TODO*///{
/*TODO*///	int LastHelpLine;
/*TODO*///	char *paDisplayText[] = {
/*TODO*///		"       Cheat List Help",
/*TODO*///		"",
/*TODO*///		"Add:",
/*TODO*///		"  Add a new (blank) Cheat to",
/*TODO*///		"  the Cheat List.",
/*TODO*///		"",
/*TODO*///		"Load (F5):",
/*TODO*///		"  Load a Cheat Database",
/*TODO*///		"",
/*TODO*///		"Reload (F8):",
/*TODO*///		"  Reload the Cheat Database",
/*TODO*///		"",
/*TODO*///		"Rename (F9):",
/*TODO*///		"  Rename the Cheat Database",
/*TODO*///		"",
/*TODO*///		"Help (F10):",
/*TODO*///		"  Display this help",
/*TODO*///		"",
/*TODO*///		"Sologame ON/OFF (F11):",
/*TODO*///		"  Toggles this option ON/OFF.",
/*TODO*///		"  When Sologame is ON, only",
/*TODO*///		"  Cheats for Player 1 are",
/*TODO*///		"  Loaded from the Cheat File.",
/*TODO*///		0 };
/*TODO*///
/*TODO*///	LastHelpLine = CreateHelp(paDisplayText, HelpLine);
/*TODO*///	ShowHelp(LastHelpLine, HelpLine);
/*TODO*///	reset_texttable (HelpLine);
/*TODO*///}
/*TODO*///
/*TODO*///void StartSearchHelp (void)
/*TODO*///{
/*TODO*///	int LastHelpLine;
/*TODO*///	char *paDisplayText[] = {
/*TODO*///		"    Cheat Search Help" ,
/*TODO*///		"" ,
/*TODO*///		"Lives Or Some Other Value:" ,
/*TODO*///		" Searches for a specific" ,
/*TODO*///		" value that you specify." ,
/*TODO*///		"" ,
/*TODO*///		"Timers:" ,
/*TODO*///		" Starts by storing all of" ,
/*TODO*///		" the game's memory, and then" ,
/*TODO*///		" looking for values that" ,
/*TODO*///		" have changed by a specific" ,
/*TODO*///		" amount from the value that" ,
/*TODO*///		" was stored when the search" ,
/*TODO*///		" was started or continued." ,
/*TODO*///		"" ,
/*TODO*///		"Energy:" ,
/*TODO*///		" Similar to Timers. Searches" ,
/*TODO*///		" for values that are Greater" ,
/*TODO*///		" than, Less than, or Equal" ,
/*TODO*///		" to the values stored when" ,
/*TODO*///		" the search was started or" ,
/*TODO*///		" continued." ,
/*TODO*///		"" ,
/*TODO*///		"Status:" ,
/*TODO*///		"  Searches for a Bit or Flag" ,
/*TODO*///		"  that may or may not have" ,
/*TODO*///		"  toggled its value since" ,
/*TODO*///		"  the search was started." ,
/*TODO*///		"" ,
/*TODO*///		"Slow But Sure:" ,
/*TODO*///		"  This search stores all of" ,
/*TODO*///		"  the game's memory, and then" ,
/*TODO*///		"  looks for values that are" ,
/*TODO*///		"  the Same As, or Different" ,
/*TODO*///		"  from the values stored when" ,
/*TODO*///		"  the search was started." ,
/*TODO*///		"" ,
/*TODO*///		"Select Search Speed:" ,
/*TODO*///		"  This allow you scan all" ,
/*TODO*///		"  or part of memory areas" ,
/*TODO*///		0 };
/*TODO*///
/*TODO*///	LastHelpLine = CreateHelp(paDisplayText, HelpLine);
/*TODO*///	ShowHelp(LastHelpLine, HelpLine);
/*TODO*///	reset_texttable (HelpLine);
/*TODO*///}
/*TODO*///
/*TODO*///void EditCheatHelp (void)
/*TODO*///{
/*TODO*///	int LastHelpLine;
/*TODO*///	char *paDisplayText[] = {
/*TODO*///		"      Edit Cheat Help" ,
/*TODO*///		"" ,
/*TODO*///		"Name:" ,
/*TODO*///		"  Displays the Name of this" ,
/*TODO*///		"  Cheat. It can be edited by" ,
/*TODO*///		"  hitting <ENTER> while it is" ,
/*TODO*///		"  selected.  Cheat Names are" ,
/*TODO*///		"  limited to 29 characters." ,
/*TODO*///		"  You can use <SHIFT> to" ,
/*TODO*///		"  uppercase a character, but" ,
/*TODO*///		"  only one character at a" ,
/*TODO*///		"  time!" ,
/*TODO*///		"" ,
/*TODO*///		"CPU:" ,
/*TODO*///		"  Specifies the CPU (memory" ,
/*TODO*///		"  region) that gets affected." ,
/*TODO*///		"" ,
/*TODO*///		"Address:" ,
/*TODO*///		"  The Address of the location" ,
/*TODO*///		"  in memory that gets set to" ,
/*TODO*///		"  the new value." ,
/*TODO*///		"" ,
/*TODO*///		"Value:" ,
/*TODO*///		"  The new value that gets" ,
/*TODO*///		"  placed into the specified" ,
/*TODO*///		"  Address while the Cheat is" ,
/*TODO*///		"  active." ,
/*TODO*///		"" ,
/*TODO*///		"Type:" ,
/*TODO*///		"  Specifies how the Cheat" ,
/*TODO*///		"  will actually work. See the" ,
/*TODO*///		"  general help for details." ,
/*TODO*///		"" ,
/*TODO*///		"More:" ,
/*TODO*///		"  Same as Name. This is" ,
/*TODO*///		"  the extra description." ,
/*TODO*///		"" ,
/*TODO*///		"Notes:" ,
/*TODO*///		"  Use the Right and Left" ,
/*TODO*///		"  arrow keys to increment and" ,
/*TODO*///		"  decrement values, or to" ,
/*TODO*///		"  select from pre-defined" ,
/*TODO*///		"  Cheat Names." ,
/*TODO*///		"  The <1> ... <8> keys are used" ,
/*TODO*///		"  to increment the number in" ,
/*TODO*///		"  that specific column of a" ,
/*TODO*///		"  value." ,
/*TODO*///		0 };
/*TODO*///
/*TODO*///	LastHelpLine = CreateHelp(paDisplayText, HelpLine);
/*TODO*///	ShowHelp(LastHelpLine, HelpLine);
/*TODO*///	reset_texttable (HelpLine);
/*TODO*///}
/*TODO*///
/*TODO*///void ChooseWatchHelp (void)
/*TODO*///{
/*TODO*///	int LastHelpLine;
/*TODO*///	char *paDisplayText[] = {
/*TODO*///		"      Choose Watch Help" ,
/*TODO*///		"" ,
/*TODO*///		"Delete:" ,
/*TODO*///		"  Delete the selected Watch" ,
/*TODO*///		"" ,
/*TODO*///		"Copy (Enter):" ,
/*TODO*///		"  Copy the previous Watch" ,
/*TODO*///		"" ,
/*TODO*///		"Notes:" ,
/*TODO*///		"  Use the Right and Left" ,
/*TODO*///		"  arrow keys to increment and" ,
/*TODO*///		"  decrement values.",
/*TODO*///		"  The <1> ... <8> keys are used" ,
/*TODO*///		"  to increment the number in" ,
/*TODO*///		"  that specific column of a" ,
/*TODO*///		"  value. The <9> and <0> keys" ,
/*TODO*///		"  are used to decrement/increment",
/*TODO*///		"  the number of the CPU." ,
/*TODO*///		"  The <I><J><K><L> keys are used" ,
/*TODO*///		"  to move the watches up, left," ,
/*TODO*///		"  down and right." ,
/*TODO*///		"" ,
/*TODO*///		"Save (F1):" ,
/*TODO*///		"  Save the selected Watch" ,
/*TODO*///		"  as a Cheat in the Cheat List" ,
/*TODO*///		"" ,
/*TODO*///		"Edit (F3):" ,
/*TODO*///		"  Edit the Address of the Watch" ,
/*TODO*///		"" ,
/*TODO*///		"Far Copy (F4):" ,
/*TODO*///		"  Copy the selected Watch" ,
/*TODO*///		"" ,
/*TODO*///		"Save All (F6):" ,
/*TODO*///		"  Save all the Watches" ,
/*TODO*///		"  as Cheats in the Cheat List" ,
/*TODO*///		"" ,
/*TODO*///		"Del All (F7):" ,
/*TODO*///		"  Remove all the Watches" ,
/*TODO*///		"" ,
/*TODO*///		"Help (F10):" ,
/*TODO*///		"  Display this help" ,
/*TODO*///		0 };
/*TODO*///
/*TODO*///	LastHelpLine = CreateHelp(paDisplayText, HelpLine);
/*TODO*///	ShowHelp(LastHelpLine, HelpLine);
/*TODO*///	reset_texttable (HelpLine);
/*TODO*///}
/*TODO*///
/*TODO*///void SelectFastSearchHelp (void)
/*TODO*///{
/*TODO*///	int LastHelpLine;
/*TODO*///	char *paDisplayText[] = {
/*TODO*///		"   Select Search Speed Help" ,
/*TODO*///		"" ,
/*TODO*///		"Slow search:" ,
/*TODO*///		"  Scan all memory to find" ,
/*TODO*///		"  cheats. Large amount of" ,
/*TODO*///		"  memory might be needed." ,
/*TODO*///		"" ,
/*TODO*///		"Normal search:" ,
/*TODO*///		"  Scan all memory areas" ,
/*TODO*///		"  which are labelled RAM" ,
/*TODO*///		"  or BANK1 to BANK8." ,
/*TODO*///		"" ,
/*TODO*///		"Fastest search:" ,
/*TODO*///		"  Scan the useful memory" ,
/*TODO*///		"  area. Used to scan NEOGEO" ,
/*TODO*///		"  games and the ones with" ,
/*TODO*///		"  TM34010 CPU(s)." ,
/*TODO*///		"" ,
/*TODO*///		"Select Memory Area:" ,
/*TODO*///		"  Scan the memory areas" ,
/*TODO*///		"  selected by the user." ,
/*TODO*///		"" ,
/*TODO*///		"Help (F10):" ,
/*TODO*///		"  Display this help" ,
/*TODO*///		0 };
/*TODO*///
/*TODO*///	LastHelpLine = CreateHelp(paDisplayText, HelpLine);
/*TODO*///	ShowHelp(LastHelpLine, HelpLine);
/*TODO*///	reset_texttable (HelpLine);
/*TODO*///}
/*TODO*///
/*TODO*///void DisplayHelpFile(void)
/*TODO*///{
/*TODO*///  int LastHelpLine;
/*TODO*///
/*TODO*///  LastHelpLine = LoadHelp(helpfile, HelpLine);
/*TODO*///  ShowHelp(LastHelpLine, HelpLine);
/*TODO*///  reset_texttable (HelpLine);
/*TODO*///}
/*TODO*///
}
