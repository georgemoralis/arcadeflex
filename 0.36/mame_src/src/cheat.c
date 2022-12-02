/*********************************************************************

  cheat.c

*********************************************************************/

/* Please don't move these #define : they'll be easier to find at the top of the file :) */

#define LAST_UPDATE "99.07.17"
#define LAST_CODER	"JCK"
#define CHEAT_VERSION	"v1.00"

/* JCK 981123 Please do not remove ! Just comment it out ! */
/* #define JCK */

/* JCK 990321 Please do not remove ! Just comment it out ! */
/* #define USENEWREADKEY */

/*|*\
|*|  Modifications by John Butler
|*|
|*| JB 980407:	Changes to support 68K processor and 24-bit addresses.
|*| 		This also provided a way to make the cheat system "aware" of
|*| 		RAM areas and have it ignores ROM areas.
|*|
|*| JB 980424:	Many changes for more flexibility and more safeguards:
|*| 		- RD_GAMERAM now calls cpu_readmemXX (after swapping in the
|*| 		  correct cpu memory context) for safer memory accesses.
|*| 		- References to addresses are limited to appropriate ranges
|*| 		  for the cpu involved.
|*| 		- Watches are still only allowed for cpu 0, but adding
|*| 		  support for other cpus will be much easier now.
|*| 		- I86 cpu (which uses 20-bit addresses) should be handled
|*| 		  properly now.
|*| 		- CPUs addressing more than 24-bits will not work as-is,
|*| 		  but support wouldn't be too difficult to add (if MAME
|*| 		  ever emulates a cpu like that).
|*| 		- Searches for new cheats are still only allowed for cpu 0,
|*| 		  but adding support for other cpus will be easier now.
|*| 		  However, cheats are allowed for other cpus, and should
|*| 		  always be correctly handled now.
|*| 		- Optimized some code for readability and code size.
|*|
|*| 		CAVEATS:
|*| 		  - Code is now heavily dependent on certain structures,
|*| 			defines, functions and assumptions in the main code,
|*| 			particulary memory-related. Watch out if they change!
|*|
|*| JB 980505:	Changes to help functions to make more readable, easier
|*| 		to maintain, less redundant.
|*|
|*|   JB 980506:	New tables KEYCODE_chars[] and KEYCODE_caps[], rewrite
|*| 		in EditCheat() to reduce code size, improve readability.
|*|
|*| Modifications/Thoughts By James R. Twine
|*|
|*| JRT1:	These modifications fix the redraw problem with the watches,
|*| 		a drawing bug that can sometimes cause a crash, and adjusts
|*| 		the drawing location when a watch is added, if the added
|*| 		watch would be drawn off screen.
|*|
|*| JRT2:	These modifications allow checking for Hex values when
|*| 		searching for cheats.  Having a decimal limit limits the
|*| 		values that can be found.
|*|
|*| JRT3:	This modification clears the screen after using <F7> to
|*| 		enable and disable cheats on the fly.  This prevents the
|*| 		"CHEAT ON/OFF" message from hanging around on the vector
|*| 		games.	It causes a slight "blink", but does no more harm
|*| 		than using "Pause" on the vector games.  It also prevents
|*| 		handling <F7> if no cheats are loaded.
|*|
|*| JRT5:	Geeze...  Where to start?  Changed some of the text/
|*| 		prompts to make terms consistant.  "Special" changed to
|*| 		"Type", because it IS the type of cheat.  Menu prompts
|*| 		changed, and spacing altered.  In-Application help added
|*| 		for the Cheat List and the Cheat Search pages.
|*|
|*| 		Support for <SHIFT> modified characters in Edit Mode, name
|*| 		limit reduced to 25 while editing, to prevent problems.
|*| 		When an edit is started, the existing name comes in and
|*| 		is the default new name.  Makes editing mistakes faster.
|*|
|*| 		Changes made that display different text depending on
|*| 		if a search is first continued (after initialization), or
|*| 		further down the line, to prevent confusion.
|*|
|*| 		Changed all UPPERCASE ONLY TEXT to Mixed Case Text, so
|*| 		that the prompts are a little more friendly.
|*|
|*| 		Some of the Menus that I modified could be better, if you
|*| 		have a better design, please...  IMPLEMENT IT!!! :)
|*|
|*| 		Most of the changes are bracketed by "JRT5", but I KNOW
|*| 		that I missed a few...	Sorry.
|*|
|*| JRT6:	Slight modifications to change display and manipulation
|*| 		of Addresses so that the user cannot manipulate addresses
|*| 		that are out of the CPUs memory space.
|*|
|*| 		Additional Variables created for data display and
|*| 		manipulation.
|*|
|*|
|*| Thoughts:
|*| 		Could the cheat system become "aware" of the RAM and ROM
|*| 		area(s) of the games it is working with?  If yes, it would
|*| 		prevent the cheat system from checking ROM areas (unless
|*| 		specifically requested to do so).  This would prevent false
|*| 		(static) locations from popping up in searches.
|*|
|*| 		Or, could the user specify a specific area to limit the
|*| 		search to?	This would provide the same effect, but would
|*| 		require the user to know what (s)he is doing.
|*|
|*| James R. Twine (JRT)
|*|
|*|  Modifications by JCK from The Ultimate Patchers
|*|
|*| JCK 980917: Possibility of "circular" values in search method 1 :
|*| 		  - if you press KEYCODE_LEFT or KEYCODE_DOWN when value is 0,
|*| 			it turns to 0xFF
|*| 		  - if you press KEYCODE_RIGHT or KEYCODE_UP when value is 0xFF,
|*| 			it turns to 0.
|*| 		Added new #define (MAX_LOADEDCHEATS, MAX_ACTIVECHEATS, MAX_DISPLAYCHEATS,
|*| 		  MAX_MATCHES and MAX_WATCHES) to improve readability and upgradibility
|*| 		  (changes where it was needed)
|*| 		Added the possibility of toggling the watches display
|*| 		  ON (KEYCODE_INSERT) and OFF (KEYCODE_DEL) .
|*| 		Possibility to work on another cheat file (CHEAT.DAT is just the default one)
|*| 		  (have a look in confic.c)
|*| 		Added new types of cheats : 20 to 24 and 40 to 44 (see below)
|*|
|*| JCK 981006: 15 active cheats instead of 10
|*| 		Possibility of searches in CPUs other than 0 :
|*| 		  a question is asked to the user to choose a CPU (0 default) when he starts
|*| 		  a new search and the game has more than one CPU
|*| 		Possibility to copy a cheat code by pressing KEYCODE_F4
|*| 		Possibility to save all the cheat codes to disk by pressing KEYCODE_F6
|*| 		Possibility to remove all the cheat codes from the active list
|*| 		  by pressing KEYCODE_F7
|*| 		In the edit cheat, on the data line :
|*| 		  - KEYCODE_HOME sets the value to 0
|*| 		  - KEYCODE_END sets the value to 0x80
|*| 		Possibility to use to use KEYCODE_HOME, KEYCODE_END, KEYCODE_PGDN and
|*| 		  KEYCODE_PGUP to select a cheat code in the list
|*|
|*| JCK 981008: Data is saved on 2 hex digits (%02X)
|*| 		Possibility to view watches for CPUs other than 0
|*| 		Free memory before the start of a new search
|*|
|*| JCK 981009: CPU is displayed with the watches
|*| 		Possibility to change the CPU of a watch by pressing KEYCODE_9 or KEYCODE_0
|*|
|*| JCK 981016: Added a new help for CheatListHelp and CheatListHelpEmpty
|*| 		Corrected a minor bug in SelectCheat for KEYCODE_HOME and KEYCODE_END
|*| 		Possibility to rename the cheat filename is now by pressing KEYCODE_F9
|*| 		Possibility to reload the cheat database by pressing KEYCODE_F8
|*| 		Possibility to rename the cheat filename and reload the cheat database
|*| 		  by pressing KEYCODE_F5 (same as KEYCODE_F9 + KEYCODE_F8)
|*| 		Moved while (keyboard_pressed(key)) after each case when there was the possibility
|*| 		  to use KEYCODE_F10 (this key wasn't removed from the buffer as well as the
|*| 		  other functions keys). The modified functions are EditCheat and SelectCheat
|*| 		Added while (keyboard_pressed(key)) in function ShowHelp
|*| 		Possibility to invoke help on StartCheat by pressing KEYCODE_F10
|*| 		New functions :
|*| 		  - int RenameCheatFile(void) : returns 1 if the cheat file has been renamed, else 0
|*| 		  - int IsBCD(int ParamValue) : returns 1 if ParamValue is a BCD value, else 0
|*| 		  - void LoadCheat(void) : loads the cheats for a game
|*| 			(this function is called by InitCheat and SelectCheat)
|*| 		Added Minimum and Maximum to struct cheat_struct (used by types 60-65 & 70-75)
|*|
|*| JCK 981020: Added new types of cheats : 60 to 65 and 70 to 75 (see below)
|*| 		Possibility to change the maximum value when the user edits a cheat
|*| 		New functions :
|*| 		  - int SelectValue(int v, int BCDOnly, int ZeroPoss, int Mini, int Maxi) :
|*| 			  returns the value selected
|*|
|*| JCK 981021: Added the corrections made by Brad Oliver :
|*| 		  - fixed the bug that didn't allow users to find cheats
|*| 		  - changed function build_tables : it has now int ParamCpuNo as parameter
|*| 		  - use KEYCODE_CHEAT_TOGGLE (= KEYCODE_F5) to to toggle cheats ON/OFF
|*|
|*| JCK 981022: Possibility to copy any watch is the first empty space by pressing KEYCODE_F4
|*| 		Possibility to add the selected watch to the cheat list by pressing KEYCODE_F1
|*| 		Possibility to add all the watches to the cheat list by pressing KEYCODE_F6
|*| 		Possibility to reset all the watches by pressing KEYCODE_F7
|*| 		Possibility to invoke help on ChooseWatch by pressing KEYCODE_F10
|*| 		Corrected help text in function CheatListHelp (F6 and F7 keys were inverted)
|*| 		Added while (keyboard_pressed(key)) to remove the function keys from the buffer
|*| 		  in the edit part of functions RenameCheatFile and EditCheat
|*| 		Matches and Watches are added to the cheat list with a new format if many CPUs :
|*| 		  %04X (%01X) = %02X   (Address (CPU) = Data)
|*| 		New variables :
|*| 		  - MachHeight = ( Machine -> uiheight );
|*| 		  - MachWidth  = ( Machine -> uiwidth );
|*| 		  - FontHeight = ( Machine -> uifont -> height );
|*| 		  - FontWidth  = ( Machine -> uifont -> width );
|*| 		  - ManyCpus   = ( cpu_gettotalcpu() > 1 );
|*| 		Changed function xprintf : it has now int ForEdit as first parameter
|*| 		  (if ForEdit=1 then add a cursor at the end of the text)
|*| 		Removed function xprintfForEdit (use now the new xprintf function)
|*| 		Renamed function DisplayCheats into DisplayActiveCheats
|*| 		Rewritten functions :
|*| 		  - int cheat_menu(void)
|*| 		  - void EditCheat(int CheatNo)
|*| 		  - void ChooseWatch(void)
|*| 		New functions :
|*| 		  - int EditCheatHeader(void)
|*| 		  - int ChooseWatchHeader(void)
|*| 		  - int ChooseWatchFooter(int y)
|*| 		  - void ChooseWatchHelp(void)
|*|
|*| JCK 981023: New #define : NOVALUE -0x0100 (if no value is selected)
|*| 		CPU is added to the watches only if many CPUs
|*| 		Changed keys in the matches list :
|*| 		  - KEYCODE_F1 adds the selected match to the cheat list (old key was KEYCODE_ENTER)
|*| 		  - KEYCODE_F6 adds all the matches to the cheat list	 (old key was KEYCODE_F2)
|*| 		Changed function SelectValue :
|*| 		  - int SelectValue(int v, int BCDOnly, int ZeroPoss, int WrapPoss, int DispTwice,
|*| 					  int Mini, int Maxi, char *fmt, char *msg, int ClrScr, int yPos)
|*| 			* if BCDOnly=1 it accepts only BCD values
|*| 			* if ZeroPoss=1 the user can select 0
|*| 			* if WrapPoss=1 the user can have wrap from Mini to Maxi and from Maxi to Mini
|*| 			* if DispTwice=1 the value is displayed twice when BCDOnly=0
|*| 			* fmt is the format of the number when BCDOnly=0
|*| 			* msg is displayed in the function
|*| 			* if ClrScr=1 it clears the screen
|*| 		  - it also returns NOVALUE if KEYCODE_ESC or KEYCODE_TAB is pressed
|*| 		Renamed functions :
|*| 		  - StartCheat into StartSearch
|*| 		  - ContinueCheat into ContinueSearch
|*| 		Rewritten functions :
|*| 		  - void StartSearch(void)
|*| 		  - void ContinueSearch(void)
|*| 		New functions :
|*| 		  - int SelectMenu(int *s, struct DisplayText *dt,
|*| 					 int ArrowsOnly, int WaitForKey,
|*| 					 int Mini, int Maxi, int ClrScr, int *done) :
|*| 			returns the key pressed as well as the selected item
|*| 		  - void AddCpuToWatch(int NoWatch,char *buffer)
|*| 		  - int StartSearchHeader(void)
|*| 		  - int ContinueSearchHeader(void)
|*| 		  - int ContinueSearchMatchHeader(int count)
|*| 		  - int ContinueSearchMatchFooter(int count, int idx, int y)
|*|
|*| JCK 981026: New #define : FIRSTPOS (FontHeight*3/2) (yPos of the 1st string displayed)
|*| 		Display [S] instead of (Saved) to fit the screen when KEYCODE_F1 is pressed
|*| 		Display [W] instead of (Watch) to fit the screen when KEYCODE_F2 is pressed
|*| 		Corrected digit change in function ChooseWatch : based on CPU instead of 0
|*| 		Matches are displayed using function pShortAddrTemplate
|*| 		Matches list and watches list are centered according to variable length of text
|*|
|*| JCK 981027: Trapped function build_tables()
|*| 		Modified functions to support 32 bits CPU :
|*| 		  - static char * pAddrTemplate (int cpu)
|*| 		  - static char * pShortAddrTemplate (int cpu)
|*| 		  - void EditCheat(int CheatNo)
|*| 		  - void ChooseWatch(void)
|*| 		  - int EditCheatHeader(void)
|*| 		  - int ChooseWatchHeader(void)
|*| 		  - void EditCheatHelp (void)
|*| 		  - void ChooseWatchHelp (void)
|*|
|*| JCK 981030: Fixed a bug to scroll through descriptions of cheats
|*| 		The active cheats are displayed with a check
|*| 		Added the possibility to add a watch when result of a search is displayed
|*| 		  by pressing KEYCODE_F2
|*| 		New functions :
|*| 		  - void AddCheckToName(int NoCheat,char *buffer)
|*|
|*| JCK 981123: Removed ParamCpuNo in function build_tables (now use the static SearchCpuNo)
|*| 		Added new #define : JCK - Please do not remove ! Just comment it out !
|*|
|*| JCK 981125: MEMORY_WRITE instead of memory_find_base in function write_gameram
|*| 		New functions :
|*| 		  - int ConvertAddr(int ParamCpuNo, int ParamAddr) :
|*| 			returns a converted address to fit low endianess
|*|
|*| JCK 981126: The region of ext tables is the one of the CPU (no more crash)
|*|
|*| JCK 981203: Change context before writing into memory
|*|
|*| JCK 981204: New text when cheats are saved to file or added as watch
|*| 		Changed type of cheat (special field in cheat_struct) to match
|*| 		  AddCheckToName function
|*|
|*| JCK 981206: Ext memory is scanned only if search in a different CPU
|*|
|*| JCK 981207: Possibility to get info on a cheat by pressing KEYCODE_F12
|*| 		Changed function CheatListHelp to include the new KEYCODE_F12 key
|*| 		Fixed display (not in function ChooseWatch because of rotation)
|*| 		New functions :
|*| 		  - void ClearTextLine(int addskip, int ypos)
|*| 		  - void ClearArea(int addskip, int ystart, int yend)
|*|
|*| JCK 981208: Integrated some of VM's changes (see below)
|*| 		Added new #define : FAST_SEARCH 1 (speed-up search of cheat codes)
|*| 		Speed-up search of cheat codes for TMS34010 and NEOGEO games
|*| 		Added messages to show that a search is in progress
|*| 		Error message in function build_tables is more user-friendly
|*|
|*| JCK 981209: Fixed display in ContinueSearch function
|*|
|*| JCK 981210: Integrated some of VM's changes : scan mwa instead of mra
|*| 		In searches, read memory instead of contents of ext. memories
|*| 		One loop to verify memory locations and count them
|*| 		New functions :
|*| 		  - void ShowSearchInProgress(void)
|*| 		  - void HideSearchInProgress(void)
|*| 		Removed functions :
|*| 		  - int ConvertAddr(int ParamCpuNo, int ParamAddr)
|*| 		  - static unsigned char read_ram (struct ExtMemory *table, int offset)
|*| 		Removed #define :
|*| 		  - RD_STARTRAM(a)	read_ram(StartRam,a)
|*| 		  - WR_STARTRAM(a,v)	write_ram(StartRam,a,v)
|*| 		  - RD_BACKUPRAM(a) read_ram(BackupRam,a)
|*| 		  - WR_BACKUPRAM(a,v)	write_ram(BackupRam,a,v)
|*| 		  - RD_FLAGTABLE(a) read_ram(FlagTable,a)
|*| 		  - WR_FLAGTABLE(a,v)	write_ram(FlagTable,a,v)
|*|
|*| JCK 981211: Added new #define : COMMENTCHEAT 999 (type of cheat for comments)
|*| 		Added new type of cheat : 999 (comment)
|*| 		Comment is marked # in function AddCheckToName
|*| 		Cheat structure for a comment :
|*| 		  - .Address	= 0
|*| 		  - .Data		= 0
|*| 		  - .Special	= COMMENTCHEAT
|*| 		  - other fields as usual
|*| 		New functions :
|*| 		  - void EditComment(int CheatNo, int ypos)
|*|
|*| JCK 981212: Added More to struct cheat_struct (more description on a cheat)
|*| 		Fields Name and More of struct cheat_struct are only 40 chars long
|*| 		Possibility to get a cheat comment by pressing KEYCODE_PLUS_PAD
|*| 	  KEYCODE_DEL initialises string in xedit function
|*| 	  Special is saved on 3 digits (%03d)
|*| 		Added linked cheats (types 100-144 same as types 0-44)
|*| 		Variable int fastsearch instead of #define FAST_SEARCH
|*| 		  (this variable can be modified via options or command-line)
|*| 		Renamed CheatTotal to ActiveCheatTotal (variable and struct)
|*| 		Possibility to add codes from another filename by pressing
|*| 		(KEYCODE_LEFT_SHIFT or KEYCODE_RIGHT_SHIFT) + KEYCODE_F5
|*| 	  15 matches instead of 10
|*| 		New functions :
|*| 		  - int xedit(int x,int y,char *inputs,int maxlen)
|*| 		  - static char *FormatAddr(int cpu, int addtext)
|*| 		  - void SearchInProgress(int yPos, int ShowMsg)
|*| 		  - void DisplayWatches(int ClrScr, int *x,int *y, char *buffer)
|*| 		  - void DeleteActiveCheatFromTable(int NoCheat)
|*| 		  - void DeleteLoadedCheatFromTable(int NoCheat)
|*| 		Removed functions :
|*| 		  - void EditComment(int CheatNo, int ypos)
|*| 		  - static char * pAddrTemplate (int cpu)
|*| 		  - static char * pShortAddrTemplate (int cpu)
|*| 		  - void ShowSearchInProgress(void)
|*| 		  - void HideSearchInProgress(void)
|*| 		Rewritten functions :
|*| 		  - void DoCheat(void)
|*| 		  - void LoadCheat(int merge, char *filename)
|*| 		  - int RenameCheatFile(int DisplayFileName, char *filename)
|*| 		  - void EditCheat(int CheatNo)
|*| 		  - void DisplayActiveCheats(int y)
|*|
|*| JCK 981213: Variable int sologame to load cheats for one player only
|*| 		  (this variable can be modified via options or command-line)
|*| 		Fixed display in EditCheat function
|*| 		Possibility to edit the More field in EditCheat function
|*| 		Possibility to toggle cheats for one player only ON/OFF
|*| 		  by pressing KEYCODE_F11
|*| 		Possibility to scan many banks of memory
|*| 		Possibility to press KEYCODE_F1 in SelectValue function
|*| 		New functions :
|*| 		  - int SkipBank(int CpuToScan, int *BankToScanTable, void (*handler)(int,int))
|*| 		Rewritten functions :
|*| 		  - static int build_tables (void)
|*|
|*| JCK 981214: Integrated some of VM's changes : function SelectCheat has been rewritten
|*| 		No more reference to HardRefresh in SelectCheat function (label has been deleted)
|*| 		One function to save one or all cheats to the cheatfile (SaveCheat)
|*| 		New #define :
|*| 		  - NEW_CHEAT ((struct cheat_struct *) -1)
|*| 		  - YHEAD_SELECT (FontHeight * 9)
|*| 		  - YFOOT_SELECT (MachHeight - (FontHeight * 2))
|*| 		  - YFOOT_MATCH  (MachHeight - (FontHeight * 8))
|*| 		  - YFOOT_WATCH  (MachHeight - (FontHeight * 3))
|*| 		Rewritten functions :
|*| 		  - void ContinueSearchMatchFooter(int count, int idx)
|*| 		  - void ChooseWatchFooter(void)
|*| 		New functions :
|*| 		  - void set_cheat(struct cheat_struct *dest, struct cheat_struct *src)
|*| 		  - int build_cheat_list(int Index, struct DisplayText ext_dt[60],
|*| 								char ext_str2[MAX_DISPLAYCHEATS + 1][40])
|*| 		  - int SelectCheatHeader(void)
|*| 		  - int SaveCheat(int NoCheat)
|*| 		  - int FindFreeWatch(void)
|*| 		  - void AddCpuToAddr(int cpu, int addr, int data, char *buffer)
|*| 		Modified functions to use the new set_cheat function :
|*| 		  - void DeleteActiveCheatFromTable(int NoCheat)
|*| 		  - void DeleteLoadedCheatFromTable(int NoCheat)
|*| 		  - void ContinueSearch(void)
|*| 		  - void ChooseWatch(void)
|*|
|*| JCK 981215: Fixed the -sologame option : cheats for players 2, 3 and 4 are not loaded
|*| 		dt strcuture is correctly filled in SelectCheat function
|*|
|*| JCK 981216: Partially fixed display for the watches : the last watch is now removed
|*| 		Fixed message when user wants to add a cheatfile (SHIFT + KEYCODE_F5)
|*| 		Name of an active cheat changes according to the name of the loaded cheat
|*| 		Added a new help for CheatListHelp and CheatListHelpEmpty
|*| 		Help is now centered according to the max. length of help text
|*| 		SelectCheatHeader is now correctly called
|*| 		Possibility to activate more than one cheat for types 20-24 and 40-44
|*| 		Possibility to change the speed of search in the "Start Search" menu
|*| 		Check is made to see that a user hasn't pressed KEYCODE_xSHIFT in combinaison
|*| 		  with the function keys (KEYCODE_Fx) in the following functions :
|*| 		  - void SelectCheat(void)
|*| 		  - void ContinueSearch(void)
|*| 		  - void ChooseWatch(void)
|*| 		New functions :
|*| 		  - int SelectFastSearchHeader(void)
|*| 		  - void SelectFastSearchHelp(void)
|*| 		Rewritten functions :
|*| 		  - void CheatListHelp (void)
|*| 		  - void CheatListHelpEmpty (void)
|*|
|*| JCK 981217: Use of keyboard_pressed_memory() instead of keyboard_pressed() to test
|*| 		  KEYCODE_CHEAT_TOGGLE, KEYCODE_INSERT and KEYCODE_DEL in DoCheat function
|*| 		dt strcuture is correctly filled in SelectCheat function
|*|
|*| JCK 990115: Fixed the AddCheckToName function (linked cheats)
|*| 		Sound OFF when entering the cheat menu and ON when exiting it
|*| 		Frameskip, autoframeskip, showfps and showprofile are set to 0
|*| 		  while in the cheat menu then set back to the original values
|*| 		Possibility to view the last results of a search
|*| 		Changed function ContinueSearch : it has now int ViewLast as parameter
|*| 		  (if ViewLast=1 then only the results are displayed)
|*|
|*| JCK 990120: Fixed bit-related linked cheats (types 120-124 and 140-144)
|*|
|*| JCK 990128: Type of cheat is displayed on 3 digits
|*| 		In the edit cheat, on the type line :
|*| 		  - KEYCODE_HOME adds 0x100
|*| 		  - KEYCODE_END subs 0x100
|*| 		In functions DoCheat and AddCheckToName, special cases linked
|*| 		  with types 5-11 and 60-65 use 1000+ instead of 200+
|*|
|*| JCK 990131: 200 cheats instead of 150
|*| 		New message if no match at the end of first search
|*| 		Number of match is correctly displayed when view last results
|*| 		Positions of the menus are saved
|*| 		Possibility of selecting specific memory areas :
|*| 		  - keys when the list is displayed :
|*| 			* KEYCODE_ENTER : toggle memory area scanned ON/OFF
|*| 			* KEYCODE_F6 : all memory areas scanned ON
|*| 			* KEYCODE_F7 : all memory areas scanned OFF
|*| 			* KEYCODE_F12 : display info on a memory area
|*| 		  - the tables are built each time the list is displayed
|*| 		New #define :
|*| 		  - MAX_DISPLAYMEM 16
|*| 		  - YHEAD_MEMORY (FontHeight * 8)
|*| 		  - YFOOT_MEMORY (MachHeight - (FontHeight * 2))
|*| 		New struct : memory_struct
|*| 		New functions :
|*| 		  - void AddCheckToMemArea(int NoMemArea,char *buffer)
|*| 		  - void InitMemoryAreas(void)
|*| 		  - int build_mem_list(int Index, struct DisplayText ext_dt[60],
|*| 					   char ext_str2[MAX_DISPLAYMEM + 1][40])
|*| 		  - int SelectMemoryHeader(void)
|*| 		  - void SelectMemoryAreas(void)
|*| 		Possibility to use KEYCODE_SPACE in the following functions :
|*| 		  - void SelectCheat(void)
|*| 		  - void SelectMemoryAreas(void)
|*|
|*| JCK 990201: Header correctly displayed in fuction SelectCheat
|*| 		Position of the selectcheat menu is saved
|*|
|*| JCK 990220: New cheat descriptions in the EditCheat window
|*| 		Rename the #define of the methods of search :
|*| 		  - SEARCH_VALUE  1   (old Method_1)
|*| 		  - SEARCH_TIME   2   (old Method_2)
|*| 		  - SEARCH_ENERGY 3   (old Method_3)
|*| 		  - SEARCH_BIT	  4   (old Method_4)
|*| 		  - SEARCH_BYTE   5   (old Method_5)
|*| 		Message displayed when KEYCODE_CHEAT_TOGGLE is pressed lasts for 0.5 seconds
|*| 		  (in fact, Machine->drv->frames_per_second / 2)
|*|
|*| JCK 990221: 20 watches instead of 10
|*| 		Possibility to select a starting value for search methods 3, 4 and 5
|*| 	  Possibility to restore the previous values of a search
|*| 	  Possibility to add al matches to the watches by pressing KEYCODE_F8
|*| 		New functions :
|*| 		  - int SelectSearchValue(void)
|*| 		  - int SelectSearchValueHeader(void)
|*| 		  - static void copy_ram (struct ExtMemory *dest, struct ExtMemory *src)
|*|
|*| JCK 990224: Added messages in the restoration of the previous values of a search
|*| 		New #define :
|*| 		  - #define RESTORE_NOINIT	0
|*| 		  - #define RESTORE_NOSAVE	1
|*| 		  - #define RESTORE_DONE	2
|*| 		  - #define RESTORE_OK		3
|*| 		New functions :
|*| 		  - void RestoreSearch(void)
|*| 		Rewritten functions :
|*| 		  - int cheat_readkey(void)
|*|
|*| JCK 990228: Fixed old key for menus
|*| 		Added new #define : WATCHCHEAT 998 (type of cheat for "watch-only" cheats)
|*| 		Added new type of cheat : 998 ("watch-only" cheats)
|*|
|*| JCK 990303: Added the corrections from the original MAME 035B4 source file :
|*| 		  unused variables have been removed
|*| 		New #define :
|*| 		  - LAST_UPDATE (date of the last modifications to the cheat engine)
|*| 		  - LAST_CODER	(initials (3 letters max) of the coder who made the modifications)
|*| 		  - CHEAT_VERSION (version of the cheat engine)
|*|
|*| JCK 990305: New functions :
|*| 		  - void DisplayVersion(void)
|*| 		  - void cheat_clearbitmap(void)
|*| 		Replaced calls to function osd_clearbitmap() by cheat_clearbitmap()
|*|
|*| JCK 990307: New functions :
|*| 		  - void LoadDatabases(int InCheat)
|*| 		Added to possibility to merge many cheatfiles :
|*| 		  - separate them by a semi-colon (;) in MAME.CFG (ex: cheatfile=CHEAT.DAT;CHEAT_UP.DAT)
|*| 		  - line must not exceed 127 chars
|*| 		Possibility to select all cheats by pressing
|*| 		  (KEYCODE_LEFT_SHIFT or KEYCODE_RIGHT_SHIFT) + KEYCODE_F7
|*| 		Possibility to select a starting value for search method 2
|*|
|*| JCK 990308: Smaller space between each watch to have more watches on screen
|*| 		Modified functions affected by the space between watches :
|*| 		  - void DisplayWatches(int ClrScr, int *x,int *y,char *buffer)
|*| 		  - void ChooseWatch(void)
|*|
|*| JCK 990312: General help in an external file (CHEAT.HLP)
|*| 		New #define :
|*| 		  - MAX_TEXT_LINE 1000
|*| 		New struct : TextLine
|*| 		New functions :
|*| 		  - static void reset_texttable (struct TextLine *table)
|*| 		  - void LoadHelp(char *filename, struct TextLine *table)
|*| 		  - void DisplayHelpFile(struct TextLine *table)
|*| 		Cheats, Matches and Memory Areas are limited to the height of the screen
|*| 		Possibility to access directly in the main menu by pressing KEYCODE_HOME
|*| 		Possibility to access directly in "Continue Search" sub-menu by pressing KEYCODE_END
|*| 		Changed function ContinueSearch : void ContinueSearch(int selected, int ViewLast)
|*|
|*| JCK 990316: New functions :
|*| 		  - void cheat_save_frameskips(void)
|*| 		  - void cheat_rest_frameskips(void)
|*| 		Sound is fixed when entering the cheat engine by pressing KEYCODE_HOME or KEYCODE_END
|*| 		No search of (value-1) for search methods other than 1 (value)
|*| 		Fixed the info display in function SelectMemoryAreas
|*|
|*| JCK 990318: Possibility to edit the addresses by pressing KEYCODE_F3
|*| 	  Changed function xedit : it has now int hexaonly as last parameter
|*| 		Modified functions to edit the addresses
|*| 		  - void EditCheat(int CheatNo)
|*| 		  - void ChooseWatch(void)
|*|
|*| JCK 990319: Fixed display for rotation and flip
|*| 		Modified variables :
|*| 		  - FontHeight = ( Machine -> uifontheight );
|*| 		  - FontWidth  = ( Machine -> uifontwidth );
|*| 	  Changed function DisplayWatches : it has now int highlight, dx and dy as parameters
|*| 		  - highlights watch highlight or none if highlight = MAX_WATCHES)
|*| 		  - dx and dy are the variations since last display
|*| 		Rewritten functions :
|*| 		  - void ClearTextLine(int addskip, int ypos)
|*| 		  - void ClearArea(int addskip, int ystart, int yend)
|*|
|*| JCK 990321: Added new #define : USENEWREADKEY - Please do not remove ! Just comment it out !
|*| 		New or changed keys in function xedit :
|*| 		  - KEYCODE_LEFT and KEYCODE_RIGHT moves the cursor one car. left/right
|*| 		  - KEYCODE_HOME and KEYCODE_END moves the cursor at the begining/end of the text
|*| 		  - KEYCODE_BACKSPACE dels the car. under the cursor
|*| 		  - KEYCODE_INSERT adds a space in the text
|*| 		  - KEYCODE_DEL erases all the text
|*|
|*| JCK 990404: Fixed function IsBCD
|*| 	  Fixed display in function ContinueSearch
|*| 	  Modified function LoadHelp : it now returns a int
|*| 		In the edit cheat, on the data line :
|*| 		  - KEYCODE_HOME subs 0x80 to the value
|*| 		  - KEYCODE_END adds 0x80 to the value
|*| 		Help has been completely rewritten
|*| 		New functions :
|*| 		  - int CreateHelp (char **paDisplayText, struct TextLine *table)
|*| 		Rewritten functions :
|*| 		  - void ShowHelp(int LastHelpLine, struct TextLine *table)
|*| 		  - void CheatListHelp (void)
|*| 		  - void CheatListHelpEmpty (void)
|*| 		  - void StartSearchHelp (void)
|*| 		  - void EditCheatHelp (void)
|*| 		  - void ChooseWatchHelp (void)
|*| 		  - void SelectFastSearchHelp (void)
|*| 		New #define :
|*| 		  - MAX_DT 130
|*| 		  - OFFSET_LINK_CHEAT 500
|*| 		Modified #define :
|*| 		  - TOTAL_CHEAT_TYPES 75
|*| 		Linked cheats are now 500+ instead of 100+
|*|
|*| JCK 990529: Fixed function ContinueSearch (frameskips)
|*|
|*| JCK 990701: Fixed cheat types 061 & 071
|*| 		Added a message in function SelectValue if value is out of range
|*|
|*| JCK 990717: Cheat types 060 to 075 are now correctly saved to the text file
|*| 		Watches are correctly dipslayed when you move them with the I,J,K,L keys
|*|
|*|
|*|  Modifications by Felipe de Almeida Leme (FAL)
|*|
|*| FAL 981029: Pointer checking to avoid segmentation fault when reading an incorrect cheat file
|*|
|*|  Modifications by MSH
|*|
|*| MSH 990217: New function int cheat_readkey(void) to fix the keyboard_read_sync under Windows
|*| 		Replaced calls to function keyboard_read_sync() by cheat_readkey() when it was necessary
|*|
|*| MSH 990310: Use of the #ifdef WIN32 to fix the keyboard under Windows
|*|
|*|
|*| MLR 991114: Support for gzipped cheat files
|*|
\*|*/

/*\
|*|  TO DO list by JCK from The Ultimate Patchers
|*|
|*| JCK 990712: Possibility to search for BCD values in search method 2 (timer)
|*| 		Rewrite the help (general and other) and try to use the functions in DATAFILE.C
|*| 		Fix function DisplayWatches (more than one line of watches)
|*| 		Trap the Shift keys : correct bug when they are pressed in xedit function
|*| 		Merge functions DeleteActiveCheatFromTable and DeleteLoadedCheatFromTable
|*| 		Rewrite function FormatAddr ?
\*|*/

#include "driver.h"
#include <zlib.h>
#include <stdarg.h>
#include <ctype.h>	/* for toupper() and tolower() */

/*
 The CHEAT.DAT file:
 -This file should be in the same directory of MAME.EXE .
 -This file can be edited with a text editor, but keep the same format:
	all fields are separated by a colon (:)
   * Name of the game (short name)
   * No of the CPU
   * Address in Hexadecimal
   * Data to put at this address in Hexadecimal
   * Type of cheat
   * Description of the cheat (29 caracters max)
   * More description of the cheat (29 caracters max) - this field is optional

Types of cheats:
 000-Normal, update ram when DoCheat is Called
 001-Write in ram just one time then delete cheat from active list
 002-Wait a second between 2 writes
 003-Wait 2 second between 2 writes
 004-Wait 5 second between 2 writes
 005-When the original value is not like the cheat, wait 1 second then write it
	Needed by Tempest for the Super Zapper
 006-When the original value is not like the cheat, wait 2 second then write it
 007-When the original value is not like the cheat, wait 5 second then write it
 008-Do not change if value decrease by 1 each frames
 009-Do not change if value decrease by 2 each frames
 010-Do not change if value decrease by 3 each frames
 011-Do not change if value decrease by 4 each frames

JCK 980917
 020 to 024 set the specified bits (force bits to 1); same as 0 to 4 otherwize
 040 to 044 reset the specified bits (force bits to 0); same as 0 to 4 otherwize

JCK 981020
 060-Select a decimal value from 0 to maximum (0 is allowed - display 0 ... maximum)
	   this value is written in memory when it changes then delete cheat from active list
 061-Select a decimal value from 0 to maximum (0 isn't allowed - display 1 ... maximum+1)
	   this value is written in memory when it changes then delete cheat from active list
 062-Select a decimal value from 1 to maximum
	   this value is written in memory when it changes then delete cheat from active list
 063-Select a BCD value from 0 to maximum (0 is allowed - display 0 ... maximum)
	   this value is written in memory when it changes then delete cheat from active list
 064-Select a BCD value from 0 to maximum (0 isn't allowed - display 1 ... maximum+1)
	   this value is written in memory when it changes then delete cheat from active list
 065-Select a BCD value from 1 to maximum
	   this value is written in memory when it changes then delete cheat from active list
 070-Select a decimal value from 0 to maximum (0 is allowed - display 0 ... maximum)
	   this value is written once in memory then delete cheat from active list
 071-Select a decimal value from 0 to maximum (0 isn't allowed - display 1 ... maximum+1)
	   this value is written once in memory then delete cheat from active list
 072-Select a decimal value from 1 to maximum
	   this value is written once in memory then delete cheat from active list
 073-Select a BCD value from 0 to maximum (0 is allowed - display 0 ... maximum)
	   this value is written once in memory then delete cheat from active list
 074-Select a BCD value from 0 to maximum (0 isn't allowed - display 1 ... maximum+1)
	   this value is written once in memory then delete cheat from active list
 075-Select a BCD value from 1 to maximum
	   this value is written once in memory then delete cheat from active list

JCK 990404
 500 to 544-Used for linked cheats; same as 0 to 44 otherwize

JCK 990228
 998-"Watch-only" (added as a watch when activated - marked with a ? in the cheat list)

JCK 981212
 999-Comment (can't be activated - marked with a # in the cheat list)
*/

extern int frameskip;
extern int autoframeskip;
#ifdef JCK
extern int showfps;
extern int showprofile;
#endif

#ifndef NEOFREE
#ifndef TINY_COMPILE
extern struct GameDriver driver_neogeo;
#endif
#endif

extern unsigned char *memory_find_base (int cpu, int offset);

struct cheat_struct
{
  int CpuNo;
  int Address;
  int Data;
  int Special;
  int Count;
  int Backup;
  int Minimum;
  int Maximum;
  char Name[40];
  char More[40];
};

struct memory_struct
{
  int Enabled;
  char Name[24];
  void (*handler)(int,int);
};

#define MAX_TEXT_LINE 1000

struct TextLine
{
	int number;
	unsigned char *data;
};

static struct TextLine HelpLine[MAX_TEXT_LINE];


/* macros stolen from memory.c for our nefarious purposes: */
#define MEMORY_READ(index,offset)	((*cpuintf[Machine->drv->cpu[index].cpu_type & ~CPU_FLAGS_MASK].memory_read)(offset))
#define MEMORY_WRITE(index,offset,data) ((*cpuintf[Machine->drv->cpu[index].cpu_type & ~CPU_FLAGS_MASK].memory_write)(offset,data))
#define ADDRESS_BITS(index) 	(cpuintf[Machine->drv->cpu[index].cpu_type & ~CPU_FLAGS_MASK].address_bits)

#define MAX_ADDRESS(cpu)	(0xFFFFFFFF >> (32-ADDRESS_BITS(cpu)))

#define RD_GAMERAM(cpu,a)	read_gameram(cpu,a)
#define WR_GAMERAM(cpu,a,v) write_gameram(cpu,a,v)

#define MAX_LOADEDCHEATS	200
#define MAX_ACTIVECHEATS	15
#define MAX_WATCHES 	20
#define MAX_MATCHES 	(MachHeight / FontHeight - 13)
#define MAX_DISPLAYCHEATS	(MachHeight / FontHeight - 12)

#define MAX_DISPLAYMEM		  (MachHeight / FontHeight - 10)

#define MAX_DT			130

#define OFFSET_LINK_CHEAT	500

#define TOTAL_CHEAT_TYPES	75
#define CHEAT_NAME_MAXLEN	29
#define CHEAT_FILENAME_MAXLEN	255

#define SEARCH_VALUE	1
#define SEARCH_TIME 2
#define SEARCH_ENERGY	3
#define SEARCH_BIT	4
#define SEARCH_BYTE 5

#define RESTORE_NOINIT	0
#define RESTORE_NOSAVE	1
#define RESTORE_DONE	2
#define RESTORE_OK	3

#define NOVALUE 			-0x0100 	/* No value has been selected */

#define FIRSTPOS			(FontHeight*3/2)	/* yPos of the 1st string displayed */

#define COMMENTCHEAT		999 	  /* Type of cheat for comments */
#define WATCHCHEAT		998 	  /* Type of cheat for "watch-only" */

/* VM 981213 BEGIN */
/* Q: Would 0/NULL be better than -1? */
#define NEW_CHEAT ((struct cheat_struct *) -1)
/* VM 981213 END */

#define YHEAD_SELECT (FontHeight * 9)
#define YFOOT_SELECT (MachHeight - (FontHeight * 2))
#define YFOOT_MATCH  (MachHeight - (FontHeight * 8))
#define YFOOT_WATCH  (MachHeight - (FontHeight * 3))

#define YHEAD_MEMORY (FontHeight * 8)
#define YFOOT_MEMORY (MachHeight - (FontHeight * 2))

/* This variables are not static because of extern statement */
char *cheatfile = "CHEAT.DAT";
char database[CHEAT_FILENAME_MAXLEN+1];

char *helpfile = "CHEAT.HLP";

int fastsearch = 2;
int sologame   = 0;

int he_did_cheat;

void	CheatListHelp( void );
void	CheatListHelpEmpty( void );
void	EditCheatHelp( void );
void	StartSearchHelp( void );
void	ChooseWatchHelp( void );
void	SelectFastSearchHelp ( void );

void	DisplayHelpFile( void );

static int	iCheatInitialized = 0;

static struct ExtMemory StartRam[MAX_EXT_MEMORY];
static struct ExtMemory BackupRam[MAX_EXT_MEMORY];
static struct ExtMemory FlagTable[MAX_EXT_MEMORY];

static struct ExtMemory OldBackupRam[MAX_EXT_MEMORY];
static struct ExtMemory OldFlagTable[MAX_EXT_MEMORY];

static int ActiveCheatTotal;
static int LoadedCheatTotal;
static struct cheat_struct ActiveCheatTable[MAX_ACTIVECHEATS+1];
static struct cheat_struct LoadedCheatTable[MAX_LOADEDCHEATS+1];

static int MemoryAreasTotal;
static struct memory_struct MemToScanTable[MAX_EXT_MEMORY];

static unsigned int WatchesCpuNo[MAX_WATCHES];
static unsigned int Watches[MAX_WATCHES];
static int WatchesFlag;
static int WatchX,WatchY;

static int WatchGfxLen;
static int WatchGfxPos;

static int SaveMenu;
static int SaveStartSearch;
static int SaveContinueSearch;

static int SaveIndex;

static int StartValue;
static int CurrentMethod;
static int SaveMethod;

static int CheatEnabled;
static int WatchEnabled;

static int SearchCpuNo;
static int SearchCpuNoOld;

static int MallocFailure;

static int RebuildTables;

static int MemoryAreasSelected;

static int oldkey;

static int MachHeight;
static int MachWidth;
static int FontHeight;
static int FontWidth;

static int ManyCpus;

static int ValTmp;

static int MatchFound;
static int OldMatchFound;

static int RestoreStatus;

static int saveframeskip;
static int saveautoframeskip;
#ifdef JCK
static int saveshowfps;
static int saveshowprofile;
#endif

static char CCheck[2]	= "\x8C";
static char CNoCheck[2] = " ";
static char CComment[2] = "#";
static char CMore[2]	= "+";
static char CWatch[2]	= "?";

/* These variables are also declared in function displaymenu (USRINTRF.C) */
/* They should be moved somewhere else, so we could use them as extern :) */
static char lefthilight[2]	 = "\x1A";
static char righthilight[2]  = "\x1B";
static char uparrow[2]		 = "\x18";
static char downarrow[2]	 = "\x19";

static unsigned char KEYCODE_chars[] =
{
/* 0	1	 2	  3    4	5	 6	  7    8	9 */
   0 , 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 	/* 0 */
  'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 	/* 1 */
  't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', 	/* 2 */
  '3', '4', '5', '6', '7', '8', '9',  0 ,  0 ,	0 , 	/* 3 */
   0 ,	0 ,  0 ,  0 ,  0 ,	0 ,  0 ,  0 ,  0 ,	0 , 	/* 4 */
   0 ,	0 ,  0 ,  0 ,  0 ,	0 ,  0 ,  0 ,  0 ,	0 , 	/* 5 */
  '`', '-', '=',  0 ,  0 , '[', ']',  0 , ';', '\'',    /* 6 */
 '\\',	0 , ',', '.', '/', ' ',  0 ,  0 ,  0 ,	0 , 	/* 7 */
   0 ,	0 ,  0 ,  0 ,  0 ,	0 , '/', '*', '-', '+', 	/* 8 */
   0 ,	0 ,  0 ,  0 ,  0 ,	0 ,  0 ,  0 ,  0 ,	0 , 	/* 9 */
   0 ,	0 ,  0 ,  0 ,  0 ,	0							/* 10 */
};

static unsigned char KEYCODE_caps[] =
{
/* 0	1	 2	  3    4	5	 6	  7    8	9 */
   0 , 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 	/* 0 */
  'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 	/* 1 */
  'T', 'U', 'V', 'W', 'X', 'Y', 'Z', ')', '!', '@', 	/* 2 */
  '#', '$', '%', '^', '&', '*', '(',  0 ,  0 ,	0 , 	/* 3 */
   0 ,	0 ,  0 ,  0 ,  0 ,	0 ,  0 ,  0 ,  0 ,	0 , 	/* 4 */
   0 ,	0 ,  0 ,  0 ,  0 ,	0 ,  0 ,  0 ,  0 ,	0 , 	/* 5 */
  '~', '_', '+',  0 ,  0 , '{', '}',  0 , ':', '"',     /* 6 */
  '|',	0 , '<', '>', '?', ' ',  0 ,  0 ,  0 ,	0 , 	/* 7 */
   0 ,	0 ,  0 ,  0 ,  0 ,	0 , '/', '*', '-', '+', 	/* 8 */
   0 ,	0 ,  0 ,  0 ,  0 ,	0 ,  0 ,  0 ,  0 ,	0 , 	/* 9 */
   0 ,	0 ,  0 ,  0 ,  0 ,	0							/* 10 */
};

void cheat_save_frameskips(void)
{
	saveframeskip	  = frameskip;
	saveautoframeskip = autoframeskip;
	#ifdef JCK
	saveshowfps   = showfps;
	saveshowprofile   = showprofile;
	#endif
	frameskip	  = 0;
	autoframeskip	  = 0;
	#ifdef JCK
	showfps 	  = 0;
	showprofile   = 0;
	#endif
}

void cheat_rest_frameskips(void)
{
	frameskip	  = saveframeskip;
	autoframeskip = saveautoframeskip;
	#ifdef JCK
	showfps 	  = saveshowfps;
	showprofile   = saveshowprofile;
	#endif
}

/* MSH 990217 - JCK 990224 */
int cheat_readkey(void)
{
#ifdef WIN32	/* MSH 990310 */
	int key = keyboard_read_sync();
	while (keyboard_pressed(key));
	return key;
#else
  int key = 0;

  key = keyboard_read_sync();
  #ifndef USENEWREADKEY
  return key;
  #else
  if (key != oldkey)
  {
	oldkey = key;
	return key;
  }
  if (keyboard_pressed_memory_repeat(key,4))
	return key;
  return 0;
  #endif
#endif
}

void ClearTextLine(int addskip, int ypos)
{
  int i;
  int xpos = 0;
  int addx = 0, addy = 0;

  int trueorientation;
  /* hack: force the display into standard orientation to avoid */
  /* rotating the user interface */
  trueorientation = Machine->orientation;
  Machine->orientation = Machine->ui_orientation;

  if (addskip)
  {
	addx = Machine->uixmin;
	addy = Machine->uiymin;
  }

  for (i = 0; (i < (MachWidth / FontWidth)); i++)
  {
	drawgfx(Machine->scrbitmap,Machine->uifont,' ',DT_COLOR_WHITE,0,0,
			xpos+addx,ypos+addy,0,TRANSPARENCY_NONE,0);
	xpos += FontWidth;
  }

  Machine->orientation = trueorientation;
}

void ClearArea(int addskip, int ystart, int yend)
{
  int i;
  int addx = 0, addy = 0;

  if (addskip)
  {
	addx = Machine->uixmin;
	addy = Machine->uiymin;
  }

  for (i = ystart; (i < yend); i += FontHeight)
  {
	ClearTextLine (addskip,i);
  }
}

/* Print a string at the position x y
 * if x = 0 then center the string in the screen
 * if ForEdit = 0 then adds a cursor at the end of the text */
static void CLIB_DECL xprintf(int ForEdit,int x,int y,char *fmt,...)
{
  struct DisplayText dt[2];
  char s[80];
  va_list arg_ptr;
  char *format;

  va_start(arg_ptr,fmt);
  format=fmt;
  (void) vsprintf(s,format,arg_ptr);

  dt[0].text = s;
  dt[0].color = DT_COLOR_WHITE;
  if (x == 0)
	dt[0].x = (MachWidth - FontWidth * strlen(s)) / 2;
  else
	dt[0].x = x;
  if (dt[0].x < 0)
	dt[0].x = 0;
  if (dt[0].x > MachWidth)
	dt[0].x = 0;
  dt[0].y = y;
  dt[1].text = 0;
  displaytext(dt,0,1);

  if (ForEdit == 1)
  {
	dt[0].x += FontWidth * strlen(s);
	s[0] = '_';
	s[1] = 0;
	dt[0].color = DT_COLOR_YELLOW;
	displaytext(dt,0,1);
  }

}

void DisplayVersion(void)
{
  char buffer[30];

  int trueorientation;

  /* hack: force the display into standard orientation */
  trueorientation = Machine->orientation;
  Machine->orientation = Machine->ui_orientation;

  #ifdef MAME_DEBUG
  strcpy(buffer, LAST_CODER);
  strcat(buffer, " - ");
  strcat(buffer, LAST_UPDATE);
/*	xprintf(0, 1, MachHeight-FontHeight, buffer); */
  #else
  strcpy(buffer, CHEAT_VERSION);
/*	xprintf(0, 1, MachHeight-FontHeight, buffer); */
  #endif
  xprintf(0, 1, MachHeight-FontHeight, "");

  Machine->orientation = trueorientation;
}

void cheat_clearbitmap(void)
{
  osd_clearbitmap(Machine->scrbitmap);
  DisplayVersion();
}


/* Edit a string at the position x y
 * if x = 0 then center the string in the screen */
int xedit(int x,int y,char *inputs,int maxlen,int hexaonly)
{
  int i, key, length;
  int done = 0;
  char *buffer;

  int xpos, ypos;
  int CarPos;
  int CarColor = DT_COLOR_WHITE;

  int trueorientation;

  if ((buffer = malloc(maxlen+1)) == NULL)
	return(2);	  /* Cancel as if used pressed Esc */

  memset (buffer, '\0', sizeof(buffer));
  strncpy (buffer, inputs, maxlen+1);

  length = (int)strlen (buffer);
  CarPos = length;

  /* hack: force the display into standard orientation to avoid */
  /* rotating the user interface */
  trueorientation = Machine->orientation;
  Machine->orientation = Machine->ui_orientation;

  do
  {
	ClearTextLine(1, y);
	length = (int)strlen (buffer);
	xpos = ( x ? x : ((MachWidth - FontWidth * length) / 2) );
	ypos = y;
	for (i = 0;i <= length;i ++)
	{
		unsigned char c = buffer[i];

		if (!c)
			c = ' ';
		CarColor = (CarPos == i ? DT_COLOR_YELLOW : DT_COLOR_WHITE);
		drawgfx(Machine->scrbitmap,Machine->uifont,c,CarColor,0,0,
			xpos+Machine->uixmin+(i * FontWidth),ypos+Machine->uiymin,0,TRANSPARENCY_NONE,0);
	}
	DisplayVersion();	 /* Hack to update the video */

	key = keyboard_read_sync();
#ifdef WIN32	/* MSH 990310 */
	if (!keyboard_pressed_memory_repeat(key,8))
		key = 0;
#endif
	switch (key)
	{
		case KEYCODE_LEFT:
		case KEYCODE_4_PAD:
			if (CarPos)
				CarPos--;
			break;
		case KEYCODE_RIGHT:
		case KEYCODE_6_PAD:
			if (CarPos < length)
				CarPos++;
			break;
		case KEYCODE_HOME:
		case KEYCODE_7_PAD:
			CarPos = 0;
			break;
		case KEYCODE_END:
		case KEYCODE_1_PAD:
			CarPos = length;
			break;
		case KEYCODE_DEL:
		case KEYCODE_DEL_PAD:
			memset (buffer, '\0', sizeof(buffer));
			CarPos = 0;
			break;
		case KEYCODE_INSERT:
		case KEYCODE_0_PAD:
			if ((length < maxlen) && (CarPos != length))
			{
				for (i = length; i > CarPos; i --)
					buffer[i] = buffer[i-1];
				buffer[CarPos] = ' ';
			}
			break;
		case KEYCODE_BACKSPACE:
			if ((length) && (CarPos))
			{
				CarPos--;
				for (i = CarPos; i < length; i ++)
					buffer[i] = buffer[i+1];
			}
			break;

		case KEYCODE_ENTER:
		case KEYCODE_ENTER_PAD:
			while (keyboard_pressed(key));
			done = 1;
			strcpy (inputs, buffer);
			break;
		case KEYCODE_ESC:
		case KEYCODE_TAB:
			while (keyboard_pressed(key));
			done = 2;
			break;
		default:
			if (length < maxlen)
			{
				unsigned char c = 0;

				if (hexaonly)
				{
					switch (key)
					{
						case KEYCODE_0:
						case KEYCODE_1:
						case KEYCODE_2:
						case KEYCODE_3:
						case KEYCODE_4:
						case KEYCODE_5:
						case KEYCODE_6:
						case KEYCODE_7:
						case KEYCODE_8:
						case KEYCODE_9:
							c = KEYCODE_chars[key+1];
							break;
						case KEYCODE_A:
						case KEYCODE_B:
						case KEYCODE_C:
						case KEYCODE_D:
						case KEYCODE_E:
						case KEYCODE_F:
							c = KEYCODE_caps[key+1];
							break;
					}
				}
				else
				{
					if (keyboard_pressed (KEYCODE_LSHIFT) ||
						 keyboard_pressed (KEYCODE_RSHIFT))
						c = KEYCODE_caps[key+1];
					else
						c = KEYCODE_chars[key+1];
				}

				if (c)
				{
					buffer[CarPos++] = c;
				}
#ifndef WIN32	 /* MSH 990310 - Windows reports modifier keys as separate presses */
				else
					while (keyboard_pressed(key)) ;
#endif
			}
			break;
	}
  } while (done == 0);
  ClearTextLine(1, y);

  Machine->orientation = trueorientation;

  free(buffer);

  return(done);
}

/* Function to test if a value is BCD (returns 1) or not (returns 0) */
int IsBCD(int ParamValue)
{
  return(((ParamValue % 0x10 <= 9) & (ParamValue <= 0x99)) ? 1 : 0);
}

/* Function to create help (returns the number of lines) */
int CreateHelp (char **paDisplayText, struct TextLine *table)
{
  int i = 0;
  int flag = 0;
  int size = 0;
  char str[32];
  struct TextLine *txt;

  txt = table;

  while ((paDisplayText[i]) && (!flag))
  {
	strcpy (str, paDisplayText[i]);

	size = sizeof(str);

	txt->data = malloc (size + 1);
	if (txt->data == NULL)
	{
		flag = 1;
	}
	else
	{
		memset (txt->data, '\0', size + 1);
		memcpy (txt->data, str, size);
		txt->number = i++;

		txt++;
	}
  }

  return(i);
}

/* Function to create menus (returns the number of lines) */
int create_menu (char **paDisplayText, struct DisplayText *dt, int yPos)
{
  int i = 0;

  while (paDisplayText[i])
  {
	if(i)
	{
		if (paDisplayText[i][0])
		{
			dt[i].y = (dt[i - 1].y + (FontHeight + 2));
		}
		else
		{
			dt[i].y = (dt[i - 1].y + (FontHeight / 2));
		}
	}
	else
		dt[i].y = yPos;
	dt[i].color = DT_COLOR_WHITE;
	dt[i].text = paDisplayText[i];
	dt[i].x = (MachWidth - FontWidth * strlen(dt[i].text)) / 2;
	if(dt[i].x > MachWidth)
		dt[i].x = 0;
	i++;
  }
  dt[i].text = 0; /* terminate array */
  return(i);
}

/* Function to select a line from a menu (returns the key pressed) */
int SelectMenu(int *s, struct DisplayText *dt, int ArrowsOnly, int WaitForKey,
			int Mini, int Maxi, int ClrScr, int *done)
{
  int i,key;

  if (ClrScr == 1)
	cheat_clearbitmap();

  if (Maxi<Mini)
  {
	xprintf(0,0,0,"SM : Mini = %d - Maxi = %d",Mini,Maxi);
	key = keyboard_read_sync();
	while (keyboard_pressed(key)) ; /* wait for key release */
	*done = 2;
	*s = NOVALUE;
	return (NOVALUE);
  }

  *done = 0;
  do
  {
	for (i = 0;i < Maxi + 1;i++)
		dt[i].color = (i == *s) ? DT_COLOR_YELLOW : DT_COLOR_WHITE;
	displaytext(dt,0,1);

	/* key = keyboard_read_sync(); */
	key = cheat_readkey();	  /* MSH 990217 */
	switch (key)
	{
		case KEYCODE_DOWN:
		case KEYCODE_2_PAD:
			if (*s < Maxi)
				(*s)++;
			else
				*s = Mini;
			while ((*s < Maxi) && (!dt[*s].text[0]))	/* For Space In Menu */
				(*s)++;
			*done = 3;
			break;
		case KEYCODE_UP:
		case KEYCODE_8_PAD:
			if (*s > Mini)
				(*s)--;
			else
				*s = Maxi;
			while ((*s > Mini) && (!dt[*s].text[0]))	/* For Space In Menu */
				(*s)--;
			*done = 3;
			break;
		case KEYCODE_HOME:
		case KEYCODE_7_PAD:
			if (!ArrowsOnly)
				*s = Mini;
			else
				*done = 3;
			break;
		case KEYCODE_END:
		case KEYCODE_1_PAD:
			if (!ArrowsOnly)
				*s = Maxi;
			else
				*done = 3;
			break;
		case KEYCODE_ENTER:
		case KEYCODE_ENTER_PAD:
			while (keyboard_pressed(key));
			*done = 1;
			break;
		case KEYCODE_ESC:
		case KEYCODE_TAB:
			while (keyboard_pressed(key));
			*done = 2;
			break;
		default:
			*done = 3;
			break;
	}
	if ((*done != 0) && (WaitForKey))
		while (keyboard_pressed(key)) ; /* wait for key release */
  } while (*done == 0);

  return (key);
}

/* Function to select a value (returns the value or NOVALUE if KEYCODE_ESC or KEYCODE_TAB) */
int SelectValue(int v, int BCDOnly, int ZeroPoss, int WrapPoss, int DispTwice,
			int Mini, int Maxi, char *fmt, char *msg, int ClrScr, int yPos)
{
  int done,key,w,MiniIsZero;

  if (ClrScr == 1)
	cheat_clearbitmap();

  if (Maxi<Mini)
  {
	xprintf(0,0,0,"SV : Mini = %d - Maxi = %d",Mini,Maxi);
	key = keyboard_read_sync();
	while (keyboard_pressed(key)) ; /* wait for key release */
	return (NOVALUE);
  }

  /* align Mini and Maxi to the first BCD value */
  if (BCDOnly == 1)
  {
	while (IsBCD(Mini) == 0) Mini-- ;
	while (IsBCD(Maxi) == 0) Maxi-- ;
  }
  MiniIsZero = ((Mini == 0) && (ZeroPoss == 0));
  /* add 1 if Mini = 0 and 0 can't be selected */
  if (MiniIsZero == 1)
  {
	Mini ++;
	while ((IsBCD(Mini) == 0) && (BCDOnly == 1)) Mini++ ;	 /* JCK 990701 */
	Maxi ++;
	while ((IsBCD(Maxi) == 0) && (BCDOnly == 1))  Maxi++ ;	  /* JCK 990701 */
	w = v + 1;
	while ((IsBCD(w) == 0) && (BCDOnly == 1))  w++ ;	/* JCK 990701 */
  }
  else
	w = v;

  if (msg[0])
  {
  xprintf(0, 0, yPos, msg);
  yPos += FontHeight + 2;
  }

  xprintf(0, 0, yPos, "(Arrow Keys Change Value)");
  yPos += 2*FontHeight;

  oldkey = 0;

  done = 0;
  do

	/* JCK 990701 BEGIN */
  {
	if ((w < Mini) || (w > Maxi))
	{
	xprintf(0,0,0,"Value is out of range !");
	}
	else
	{
	ClearTextLine (1,0);
	}
	/* JCK 990701 END */

	if (BCDOnly == 0)
	{
		if (DispTwice == 0)
			xprintf(0, 0, yPos, fmt, w);
		else
			xprintf(0, 0, yPos, fmt, w, w);
	}
	else
	{
		if (w<=0xFF)
			xprintf(0, 0, yPos, "%02X", w);
		else
			xprintf(0, 0, yPos, "%03X", w);
	}

#ifdef WIN32	/* MSH 990310 */
	key = keyboard_read_sync();
	  if (!keyboard_pressed_memory_repeat(key,8))
	key = 0;
#else
	key = cheat_readkey();	  /* MSH 990217 */
#endif
	switch (key)
	{
		case KEYCODE_RIGHT:
		case KEYCODE_6_PAD:
		case KEYCODE_UP:
		case KEYCODE_8_PAD:
			if (w < Maxi)
			{
				w++;
				if (BCDOnly == 1)
					while (IsBCD(w) == 0) w++ ;
			}
			else
			{
				if (WrapPoss == 1)
					w = Mini;
			}
			break;
		case KEYCODE_LEFT:
		case KEYCODE_4_PAD:
		case KEYCODE_DOWN:
		case KEYCODE_2_PAD:
			if (w > Mini)
			{
				w--;
				if (BCDOnly == 1)
					while (IsBCD(w) == 0) w-- ;
			}
			else
			{
				if (WrapPoss == 1)
					w = Maxi;
			}
			break;
		case KEYCODE_HOME:
		case KEYCODE_7_PAD:
			w = Mini;
			break;
		case KEYCODE_END:
		case KEYCODE_1_PAD:
			w = Maxi;
			break;

		case KEYCODE_F1:
			done = 3;
			break;

		case KEYCODE_ENTER:
		case KEYCODE_ENTER_PAD:
			while (keyboard_pressed(key));
			done = 1;
			break;
		case KEYCODE_ESC:
		case KEYCODE_TAB:
			while (keyboard_pressed(key));
			done = 2;
			break;
		default:
			while (keyboard_pressed(key));
			break;
	}
  } while (done == 0);

  if (ClrScr == 1)
	cheat_clearbitmap();

  if (done == 2)
	return (NOVALUE);

  if (done == 3)
	return (2 * NOVALUE);

  /* sub 1 if Mini = 0 */
  if (MiniIsZero == 1)
  {
	v = w - 1;
	if (BCDOnly == 1)
		while (IsBCD(v) == 0) v-- ;
  }
  else
	v = w;
  return (v);
}


/* return a format specifier for printf based on cpu address range */
static char *FormatAddr(int cpu, int addtext)
{
	static char bufadr[10];
	static char buffer[18];
	  int i;

	memset (buffer, '\0', strlen(buffer));
	switch ((ADDRESS_BITS(cpu)+3) >> 2)
	{
		case 4:
			strcpy (bufadr, "%04X");
			break;
		case 5:
			strcpy (bufadr, "%05X");
			break;
		case 6:
			strcpy (bufadr, "%06X");
			break;
		case 7:
			strcpy (bufadr, "%07X");
			break;
		case 8:
			strcpy (bufadr, "%08X");
			break;
		default:
			strcpy (bufadr, "%X");
			break;
	}
	  if (addtext)
	  {
	strcpy (buffer, "Addr:  ");
		for (i = strlen(bufadr) + 1; i < 8; i ++)
		strcat (buffer, " ");
	  }
	  strcat (buffer,bufadr);
	return buffer;
}

void AddCpuToAddr(int cpu, int addr, int data, char *buffer)
{
	char fmt[32];

	strcpy (fmt, FormatAddr(cpu ,0));
	if (ManyCpus)
	{
		strcat(fmt," (%01X) = %02X");
		sprintf (buffer, fmt, addr, cpu, data);
	}
	else
	{
		strcat(fmt," = %02X");
		sprintf (buffer, fmt, addr, data);
	}
}

void AddCpuToWatch(int NoWatch,char *buffer)
{
  char bufadd[10];

  if ( Watches[NoWatch] > MAX_ADDRESS(WatchesCpuNo[NoWatch]))
	Watches[NoWatch] = MAX_ADDRESS(WatchesCpuNo[NoWatch]);

  sprintf(buffer, FormatAddr(WatchesCpuNo[NoWatch],0),
			Watches[NoWatch]);
  if (ManyCpus)
  {
	sprintf (bufadd, "  (%01X)", WatchesCpuNo[NoWatch]);
	strcat(buffer,bufadd);
  }
}

void AddCheckToName(int NoCheat,char *buffer)
{
  int i = 0;
  int flag = 0;
  int Special;

  if (LoadedCheatTable[NoCheat].Special == COMMENTCHEAT)
	strcpy(buffer,CComment);
  else if (LoadedCheatTable[NoCheat].Special == WATCHCHEAT)
	strcpy(buffer,CWatch);
  else
  {
	for (i = 0;i < ActiveCheatTotal && !flag;i ++)
	{
		Special = ActiveCheatTable[i].Special;
		if (Special >= 1000)
			Special -= 1000;

		if ((Special < 60) || (Special > 99))
		{
			if (	(ActiveCheatTable[i].Address	== LoadedCheatTable[NoCheat].Address)	&&
				(ActiveCheatTable[i].Data	== LoadedCheatTable[NoCheat].Data	)	&&
				(Special				== LoadedCheatTable[NoCheat].Special)	)
				flag = 1;
		}
		else
		{
			if (	(ActiveCheatTable[i].Address	== LoadedCheatTable[NoCheat].Address)	&&
				(Special				== LoadedCheatTable[NoCheat].Special)	)
				flag = 1;
		}
	}
	strcpy(buffer,(flag ? CCheck : CNoCheck));

	if (flag)
		strcpy(ActiveCheatTable[i-1].Name, LoadedCheatTable[NoCheat].Name);

  }

  if (LoadedCheatTable[NoCheat].More[0])
	strcat(buffer,CMore);
  else
	strcat(buffer," ");

  strcat(buffer,LoadedCheatTable[NoCheat].Name);
  for (i = strlen(buffer);i < CHEAT_NAME_MAXLEN+2;i ++)
	buffer[i]=' ';

  buffer[CHEAT_NAME_MAXLEN+2]=0;
}

void AddCheckToMemArea(int NoMemArea,char *buffer)
{
  int flag = 0;

  if (MemToScanTable[NoMemArea].Enabled)
	flag = 1;
  strcpy(buffer,(flag ? CCheck : CNoCheck));
  strcat(buffer," ");
  strcat(buffer,MemToScanTable[NoMemArea].Name);
}

/* read a byte from cpu at address <add> */
static unsigned char read_gameram (int cpu, int add)
{
	int save = cpu_getactivecpu();
	unsigned char data;

	memorycontextswap(cpu);

	data = MEMORY_READ(cpu,add);
	/* data = *(unsigned char *)memory_find_base (cpu, add); */

	if (cpu != save)
		memorycontextswap(save);

	return data;
}

/* write a byte to CPU 0 ram at address <add> */
static void write_gameram (int cpu, int add, unsigned char data)
{
	int save = cpu_getactivecpu();

	memorycontextswap(cpu);

	MEMORY_WRITE(cpu,add,data);
	/* *(unsigned char *)memory_find_base (cpu, add) = data; */

	if (cpu != save)
		memorycontextswap(save);
}

/* make a copy of a source ram table to a dest. ram table */
static void copy_ram (struct ExtMemory *dest, struct ExtMemory *src)
{
	struct ExtMemory *ext_dest, *ext_src;

	for (ext_src = src, ext_dest = dest; ext_src->data; ext_src++, ext_dest++)
	{
		memcpy (ext_dest->data, ext_src->data, ext_src->end - ext_src->start + 1);
	}
}

/* make a copy of each ram area from search CPU ram to the specified table */
static void backup_ram (struct ExtMemory *table)
{
	struct ExtMemory *ext;
	unsigned char *gameram;

	for (ext = table; ext->data; ext++)
	{
		int i;
		gameram = memory_find_base (SearchCpuNo, ext->start);
		memcpy (ext->data, gameram, ext->end - ext->start + 1);
		for (i=0; i <= ext->end - ext->start; i++)
			ext->data[i] = RD_GAMERAM(SearchCpuNo, i+ext->start);
	}
}

/* set every byte in specified table to data */
static void memset_ram (struct ExtMemory *table, unsigned char data)
{
	struct ExtMemory *ext;

	for (ext = table; ext->data; ext++)
		memset (ext->data, data, ext->end - ext->start + 1);
}

/* free all the memory and init the table */
static void reset_table (struct ExtMemory *table)
{
	struct ExtMemory *ext;

	for (ext = table; ext->data; ext++)
		free (ext->data);
	memset (table, 0, sizeof (struct ExtMemory) * MAX_EXT_MEMORY);
}

/* free all the memory and init the table */
static void reset_texttable (struct TextLine *table)
{
	struct TextLine *txt;

	for (txt = table; txt->data; txt++)
		free (txt->data);
	memset (table, 0, sizeof (struct TextLine) * MAX_TEXT_LINE);
}

/* Returns 1 if memory area has to be skipped */
int SkipBank(int CpuToScan, int *BankToScanTable, void (*handler)(int,int))
{
	int res = 0;

	if ((fastsearch == 1) || (fastsearch == 2))
	{
		switch ((FPTR)handler)
		{
			case (FPTR)MWA_RAM:
				res = !BankToScanTable[0];
				break;
			case (FPTR)MWA_BANK1:
				res = !BankToScanTable[1];
				break;
			case (FPTR)MWA_BANK2:
				res = !BankToScanTable[2];
				break;
			case (FPTR)MWA_BANK3:
				res = !BankToScanTable[3];
				break;
			case (FPTR)MWA_BANK4:
				res = !BankToScanTable[4];
				break;
			case (FPTR)MWA_BANK5:
				res = !BankToScanTable[5];
				break;
			case (FPTR)MWA_BANK6:
				res = !BankToScanTable[6];
				break;
			case (FPTR)MWA_BANK7:
				res = !BankToScanTable[7];
				break;
			case (FPTR)MWA_BANK8:
				res = !BankToScanTable[8];
				break;
			default:
				res = 1;
				break;
		}
	}
	return(res);
}

/* create tables for storing copies of all MWA_RAM areas */
static int build_tables (void)
{
	/* const struct MemoryReadAddress *mra = Machine->drv->cpu[SearchCpuNo].memory_read; */
	const struct MemoryWriteAddress *mwa = Machine->drv->cpu[SearchCpuNo].memory_write;

	int region = REGION_CPU1+SearchCpuNo;

	struct ExtMemory *ext_sr = StartRam;
	struct ExtMemory *ext_br = BackupRam;
	struct ExtMemory *ext_ft = FlagTable;

	struct ExtMemory *ext_obr = OldBackupRam;
	struct ExtMemory *ext_oft = OldFlagTable;

	int i, yPos;

	int NoMemArea = 0;

	  /* Trap memory allocation errors */
	int MemoryNeeded = 0;

	/* Search speedup : (the games should be dasmed to confirm this) */
	  /* Games based on Exterminator driver should scan BANK1		   */
	  /* Games based on SmashTV driver should scan BANK2		   */
	  /* NEOGEO games should only scan BANK1 (0x100000 -> 0x01FFFF)    */
	int CpuToScan = -1;
	  int BankToScanTable[9];	 /* 0 for RAM & 1-8 for Banks 1-8 */

	  for (i = 0; i < 9;i ++)
	BankToScanTable[i] = ( fastsearch != 2 );

#if (HAS_TMS34010)
	if ((Machine->drv->cpu[1].cpu_type & ~CPU_FLAGS_MASK) == CPU_TMS34010)
	{
		/* 2nd CPU is 34010: games based on Exterminator driver */
		CpuToScan = 0;
		BankToScanTable[1] = 1;
	}
	else if ((Machine->drv->cpu[0].cpu_type & ~CPU_FLAGS_MASK) == CPU_TMS34010)
	{
		/* 1st CPU but not 2nd is 34010: games based on SmashTV driver */
		CpuToScan = 0;
		BankToScanTable[2] = 1;
	}
#endif
#ifndef NEOFREE
#ifndef TINY_COMPILE
	if (Machine->gamedrv->clone_of == &driver_neogeo)
	{
		/* games based on NEOGEO driver */
		CpuToScan = 0;
		BankToScanTable[1] = 1;
	}
#endif
#endif

	  /* No CPU so we scan RAM & BANKn */
	  if ((CpuToScan == -1) && (fastsearch == 2))
	for (i = 0; i < 9;i ++)
		BankToScanTable[i] = 1;

	/* free memory that was previously allocated if no error occured */
	  /* it must also be there because mwa varies from one CPU to another */
	  if (!MallocFailure)
	  {
		reset_table (StartRam);
		reset_table (BackupRam);
		reset_table (FlagTable);

		reset_table (OldBackupRam);
		reset_table (OldFlagTable);
	  }

	  MallocFailure = 0;

	  /* Message to show that something is in progress */
	cheat_clearbitmap();
	yPos = (MachHeight - FontHeight) / 2;
	xprintf(0, 0, yPos, "Allocating Memory...");

	NoMemArea = 0;
	while (mwa->start != -1)
	{
		/* int (*handler)(int) = mra->handler; */
		void (*handler)(int,int) = mwa->handler;
		int size = (mwa->end - mwa->start) + 1;

		if (SkipBank(CpuToScan, BankToScanTable, handler))
		{
			NoMemArea++;
			mwa++;
			continue;
		}

		if ((fastsearch == 3) && (!MemToScanTable[NoMemArea].Enabled))
		{
			NoMemArea++;
			mwa++;
			continue;
		}

		/* time to allocate */
		if (!MallocFailure)
		{
			ext_sr->data = malloc (size);
			ext_br->data = malloc (size);
			ext_ft->data = malloc (size);

			ext_obr->data = malloc (size);
			ext_oft->data = malloc (size);

			if (ext_sr->data == NULL)
			{
				MallocFailure = 1;
				MemoryNeeded += size;
			}
			if (ext_br->data == NULL)
			{
				MallocFailure = 1;
				MemoryNeeded += size;
			}
			if (ext_ft->data == NULL)
			{
				MallocFailure = 1;
				MemoryNeeded += size;
			}

			if (ext_obr->data == NULL)
			{
				MallocFailure = 1;
				MemoryNeeded += size;
			}
			if (ext_oft->data == NULL)
			{
				MallocFailure = 1;
				MemoryNeeded += size;
			}

			if (!MallocFailure)
			{
				ext_sr->start = ext_br->start = ext_ft->start = mwa->start;
				ext_sr->end = ext_br->end = ext_ft->end = mwa->end;
				ext_sr->region = ext_br->region = ext_ft->region = region;
				ext_sr++, ext_br++, ext_ft++;

				ext_obr->start = ext_oft->start = mwa->start;
				ext_obr->end = ext_oft->end = mwa->end;
				ext_obr->region = ext_oft->region = region;
				ext_obr++, ext_oft++;
			}
		}
		else
			MemoryNeeded += (5 * size);

		NoMemArea++;
		mwa++;
	}

	/* free memory that was previously allocated if an error occured */
	  if (MallocFailure)
	  {
		int key;

		reset_table (StartRam);
		reset_table (BackupRam);
		reset_table (FlagTable);

		reset_table (OldBackupRam);
		reset_table (OldFlagTable);

		cheat_clearbitmap();
		yPos = (MachHeight - 10 * FontHeight) / 2;
		xprintf(0, 0, yPos, "Error while allocating memory !");
		yPos += (2 * FontHeight);
		xprintf(0, 0, yPos, "You need %d more bytes", MemoryNeeded);
		yPos += FontHeight;
		xprintf(0, 0, yPos, "(0x%X) of free memory", MemoryNeeded);
		yPos += (2 * FontHeight);
		xprintf(0, 0, yPos, "No search available for CPU %d", SearchCpuNo);
		yPos += (4 * FontHeight);
		xprintf(0, 0, yPos, "Press A Key To Continue...");
		key = keyboard_read_sync();
		while (keyboard_pressed(key)) ; /* wait for key release */
		cheat_clearbitmap();
	  }

	ClearTextLine (1, yPos);

	return MallocFailure;
}


/* Message to show that search is in progress */
void SearchInProgress(int ShowMsg, int yPos)
{
  if (ShowMsg)
	xprintf(0, 0, yPos, "Search in Progress...");
  else
	ClearTextLine (1, yPos);
}


/* Function to rename the cheatfile (returns 1 if the file has been renamed else 0)*/
int RenameCheatFile(int merge, int DisplayFileName, char *filename)
{
  int key;
  int done = 0;
  int EditYPos;
  char buffer[CHEAT_FILENAME_MAXLEN+1];

  EditYPos = (MachHeight - 7 * FontHeight) / 2;

  cheat_clearbitmap();

  if (merge < 0)
	xprintf (0, 0, EditYPos-(FontHeight*2),
			"Enter the Cheat Filename");
  else
	xprintf (0, 0, EditYPos-(FontHeight*2),
			"Enter the Filename to %s:",(merge ? "Add" : "Load"));

  memset (buffer, '\0', CHEAT_FILENAME_MAXLEN+1);
  strncpy (buffer, filename, CHEAT_FILENAME_MAXLEN);

  done = xedit(0, EditYPos, buffer, CHEAT_FILENAME_MAXLEN, 0);
  if (done == 1)
  {
	strcpy (filename, buffer);
	  if (DisplayFileName)
	  {
		cheat_clearbitmap();
		  xprintf (0, 0, EditYPos-(FontHeight*2), "Cheat Filename is now:");
		xprintf (0, 0, EditYPos, "%s", buffer);
		EditYPos += 4*FontHeight;
		xprintf(0, 0,EditYPos,"Press A Key To Continue...");
		key = keyboard_read_sync();
		while (keyboard_pressed(key)) ; /* wait for key release */
	  }
  }
  cheat_clearbitmap();
  return(done);
}

/* Function who loads the cheats for a game */
int SaveCheat(int NoCheat)
{
	gzFile *gz;
	FILE *f;
	char fmt[32];
	int i, c;
	int count = 0;
	int gz_magic[2] = {0x1f, 0x8b}; /* gzip magic header */
	int uncompressed = 0;

	if ((f = fopen(database,"rb")) != 0)
	{
		for (i = 0; i < 2; i++)
		{
			c = getc(f);
			if (c != gz_magic[i])
				uncompressed = 1;
		}
		fclose (f);
	}

	f = 0; gz = 0;

	if (uncompressed)
		f = fopen(database, "a");
	else
		gz = gzopen(database,"a");

	if (f || gz)
	{
		for (i = 0; i < LoadedCheatTotal; i++)
		{
		if ((NoCheat == i) || (NoCheat == -1))
		  {
			int addmore = (LoadedCheatTable[i].More[0]);

				/* form fmt string, adjusting length of address field for cpu address range */
				sprintf(fmt, "%%s:%%d:%s:%%02X:%%03d:%%s%s\n",
						FormatAddr(LoadedCheatTable[i].CpuNo,0),
					  (addmore ? ":%s" : ""));

				#ifdef macintosh
				if (uncompressed)
					fprintf(f, "\r");	  /* force DOS-style line enders */
				else
					gzprintf(gz, "\r"); /* force DOS-style line enders */
				#endif

				/* JCK 990717 BEGIN */
				if (	(LoadedCheatTable[LoadedCheatTotal].Special>=60)	&&
					(LoadedCheatTable[LoadedCheatTotal].Special<=75)	)
				{
					if (uncompressed)
					{
						if (fprintf(f, fmt, Machine->gamedrv->name,
									LoadedCheatTable[i].CpuNo,
									LoadedCheatTable[i].Address,
									LoadedCheatTable[i].Maximum,
									LoadedCheatTable[i].Special,
									LoadedCheatTable[i].Name,
									(addmore ? LoadedCheatTable[i].More : "")))
							count ++;
					}
					else
					{
						if (gzprintf(gz, fmt, Machine->gamedrv->name,
									 LoadedCheatTable[i].CpuNo,
									 LoadedCheatTable[i].Address,
									 LoadedCheatTable[i].Maximum,
									 LoadedCheatTable[i].Special,
									 LoadedCheatTable[i].Name,
									 (addmore ? LoadedCheatTable[i].More : "")))
							count ++;
					}
				}
				else
				{
					if (uncompressed)
					{
						if (fprintf(f, fmt, Machine->gamedrv->name,
									LoadedCheatTable[i].CpuNo,
									LoadedCheatTable[i].Address,
									LoadedCheatTable[i].Data,
									LoadedCheatTable[i].Special,
									LoadedCheatTable[i].Name,
									(addmore ? LoadedCheatTable[i].More : "")))
							count ++;
					}
					else
					{
						if (gzprintf(gz, fmt, Machine->gamedrv->name,
									 LoadedCheatTable[i].CpuNo,
									 LoadedCheatTable[i].Address,
									 LoadedCheatTable[i].Data,
									 LoadedCheatTable[i].Special,
									 LoadedCheatTable[i].Name,
									 (addmore ? LoadedCheatTable[i].More : "")))
							count ++;
					}
				}
				/* JCK 990717 END */

			}
		}
		if (uncompressed)
			fclose (f);
		else
			gzclose (gz);
	}
	return(count);
}

/* Function who loads the cheats for a game from a single database */
void LoadCheat(int merge, char *filename)
{
  gzFile *f;
  char *ptr;
  char str[90];    /* To support the add. description */

  int yPos;

  if (!merge)
  {
	ActiveCheatTotal = 0;
	LoadedCheatTotal = 0;
  }

  /* Load the cheats for that game */
  /* Ex.: pacman:0:4E14:06:000:1UP Unlimited lives:Coded on 1 byte */
  if ((f = gzopen(filename,"r")) != 0)
  {
	yPos = (MachHeight - FontHeight) / 2 - FontHeight;
/*	xprintf(0, 0, yPos, "Loading cheats from file"); */
	  yPos += FontHeight;
/*	xprintf(0, 0, yPos, "%s...",filename); */

	for(;;)
	{
		  if (gzgets(f,str,90) == NULL)
		break;

		#ifdef macintosh  /* JB 971004 */
		/* remove extraneous LF on Macs if it exists */
		if ( str[0] == '\r' )
			strcpy( str, &str[1] );
		#endif

		if (str[strlen(Machine->gamedrv->name)] != ':')
			continue;
		if (strncmp(str,Machine->gamedrv->name,strlen(Machine->gamedrv->name)) != 0)
			continue;
		if (str[0] == ';') /* Comments line */
			continue;

		if (LoadedCheatTotal >= MAX_LOADEDCHEATS)
		{
			break;
		}

		if (sologame)
			if (	(strstr(str, "2UP") != NULL) || (strstr(str, "PL2") != NULL)	||
				(strstr(str, "3UP") != NULL) || (strstr(str, "PL3") != NULL)	||
				(strstr(str, "4UP") != NULL) || (strstr(str, "PL4") != NULL)	||
				(strstr(str, "2up") != NULL) || (strstr(str, "pl2") != NULL)	||
				(strstr(str, "3up") != NULL) || (strstr(str, "pl3") != NULL)	||
				(strstr(str, "4up") != NULL) || (strstr(str, "pl4") != NULL)	)
			  continue;

		/* Reset the counter */
		LoadedCheatTable[LoadedCheatTotal].Count=0;

		/* Extract the fields from the string */
		ptr = strtok(str, ":");
		if ( ! ptr )
			continue;

		ptr = strtok(NULL, ":");
		if ( ! ptr )
			continue;
		sscanf(ptr,"%d", &LoadedCheatTable[LoadedCheatTotal].CpuNo);

		ptr = strtok(NULL, ":");
		if ( ! ptr )
			continue;
		sscanf(ptr,"%X", &LoadedCheatTable[LoadedCheatTotal].Address);

		ptr = strtok(NULL, ":");
		if ( ! ptr )
			continue;
		sscanf(ptr,"%x", &LoadedCheatTable[LoadedCheatTotal].Data);

		ptr = strtok(NULL, ":");
		if ( ! ptr )
			continue;
		sscanf(ptr,"%d", &LoadedCheatTable[LoadedCheatTotal].Special);

		ptr = strtok(NULL, ":");
		if ( ! ptr )
			continue;
		strcpy(LoadedCheatTable[LoadedCheatTotal].Name,ptr);

		strcpy(LoadedCheatTable[LoadedCheatTotal].More,"\n");
		ptr = strtok(NULL, ":");
		if (ptr)
			strcpy(LoadedCheatTable[LoadedCheatTotal].More,ptr);
		if (strstr(LoadedCheatTable[LoadedCheatTotal].Name,"\n") != NULL)
			LoadedCheatTable[LoadedCheatTotal].Name[strlen(LoadedCheatTable[LoadedCheatTotal].Name)-1] = 0;
		if (strstr(LoadedCheatTable[LoadedCheatTotal].More,"\n") != NULL)
			LoadedCheatTable[LoadedCheatTotal].More[strlen(LoadedCheatTable[LoadedCheatTotal].More)-1] = 0;

		/* Fill the new fields : Minimum and Maximum */
		if (	(LoadedCheatTable[LoadedCheatTotal].Special==62)	||
			(LoadedCheatTable[LoadedCheatTotal].Special==65)	||
			(LoadedCheatTable[LoadedCheatTotal].Special==72)	||
			(LoadedCheatTable[LoadedCheatTotal].Special==75)	)
			LoadedCheatTable[LoadedCheatTotal].Minimum = 1;
		else
			LoadedCheatTable[LoadedCheatTotal].Minimum = 0;
		if (	(LoadedCheatTable[LoadedCheatTotal].Special>=60)	&&
			(LoadedCheatTable[LoadedCheatTotal].Special<=75)	)
		{
			LoadedCheatTable[LoadedCheatTotal].Maximum = LoadedCheatTable[LoadedCheatTotal].Data;
			LoadedCheatTable[LoadedCheatTotal].Data = 0;
		}
		else
			LoadedCheatTable[LoadedCheatTotal].Maximum = 0xFF;

		if (LoadedCheatTable[LoadedCheatTotal].Special == COMMENTCHEAT)
		{
			LoadedCheatTable[LoadedCheatTotal].Address = 0;
			LoadedCheatTable[LoadedCheatTotal].Data = 0;
		}

		if (LoadedCheatTable[LoadedCheatTotal].Special == WATCHCHEAT)
		{
			LoadedCheatTable[LoadedCheatTotal].Data = 0;
		}

		LoadedCheatTotal++;
	}
	gzclose(f);

	ClearArea (1, yPos - FontHeight, yPos + FontHeight);
  }
}

/* Function who loads the cheats for a game from many databases */
void LoadDatabases(int InCheat)
{
  char *ptr;
  char str[CHEAT_FILENAME_MAXLEN+1];
  char filename[CHEAT_FILENAME_MAXLEN+1];

  int pos1, pos2;

  if (InCheat)
	cheat_clearbitmap();
  else
  {
	cheat_save_frameskips();
	osd_clearbitmap(Machine->scrbitmap);
  }

  ActiveCheatTotal = 0;
  LoadedCheatTotal = 0;

  strcpy(str, cheatfile);
  ptr = strtok(str, ";");
  strcpy(database, ptr);
  strcpy(str, cheatfile);
  str[strlen(str) + 1] = 0;
  pos1 = 0;
  while (str[pos1])
  {
	pos2 = pos1;
	while ((str[pos2]) && (str[pos2] != ';'))
		pos2++;
	if (pos1 != pos2)
	{
		memset (filename, '\0', sizeof(filename));
		strncpy(filename, &str[pos1], (pos2 - pos1));
		LoadCheat(1, filename);
		pos1 = pos2 + 1;
	}
  }

  if (!InCheat)
  {
	cheat_rest_frameskips();
  }

}

int LoadHelp(char *filename, struct TextLine *table)
{
  gzFile *f;
  char str[32];

  int yPos;
  int LineNumber = 0;
  int size;

  struct TextLine *txt;

  yPos = (MachHeight - FontHeight) / 2;
  xprintf(0, 0, yPos, "Loading help...");

  if ((f = gzopen(filename,"r")) != 0)
  {
	txt = table;
	for(;;)
	{
		  if (gzgets(f,str,32) == NULL)
		break;

		#ifdef macintosh  /* JB 971004 */
		/* remove extraneous LF on Macs if it exists */
		if ( str[0] == '\r' )
			strcpy( str, &str[1] );
		#endif

		if (str[0] == ';') /* Comments line */
			continue;

		str[strlen(str) - 1] = 0;

		size = sizeof(str);
		txt->data = malloc (size + 1);
		if (txt->data == NULL)
		break;

		memset (txt->data, '\0', size + 1);
		memcpy (txt->data, str, size);
		txt->number = LineNumber++;

		txt++;
	}
	gzclose(f);
  }

  ClearTextLine (1, yPos);

  return(LineNumber);
}

void InitMemoryAreas(void)
{
	const struct MemoryWriteAddress *mwa = Machine->drv->cpu[SearchCpuNo].memory_write;
	char buffer[40];

	MemoryAreasSelected = 0;
	  MemoryAreasTotal = 0;
	while (mwa->start != -1)
	{
		sprintf (buffer, FormatAddr(SearchCpuNo,0), mwa->start);
		strcpy (MemToScanTable[MemoryAreasTotal].Name, buffer);
		strcat (MemToScanTable[MemoryAreasTotal].Name," -> ");
		sprintf (buffer, FormatAddr(SearchCpuNo,0), mwa->end);
		strcat (MemToScanTable[MemoryAreasTotal].Name, buffer);
		MemToScanTable[MemoryAreasTotal].handler = mwa->handler;
		MemToScanTable[MemoryAreasTotal].Enabled = 0;
		MemoryAreasTotal++;
		mwa++;
	}
}

/* Init some variables */
void InitCheat(void)
{
  int i;

  he_did_cheat = 0;
  CheatEnabled = 0;

  WatchEnabled = 0;

  CurrentMethod = 0;
  SaveMethod	= 0;

  SearchCpuNoOld	= -1;
  MallocFailure = -1;

  MachHeight = ( Machine -> uiheight );
  MachWidth  = ( Machine -> uiwidth );

  FontHeight = ( Machine -> uifontheight );
  FontWidth  = ( Machine -> uifontwidth );

  ManyCpus	 = ( cpu_gettotalcpu() > 1 );

  SaveMenu = 0;
  SaveStartSearch = 0;
  SaveContinueSearch = 0;

  SaveIndex = 0;

  reset_table (StartRam);
  reset_table (BackupRam);
  reset_table (FlagTable);

  reset_table (OldBackupRam);
  reset_table (OldFlagTable);

  MatchFound = 0;
  OldMatchFound = 0;

  RestoreStatus = RESTORE_NOINIT;

  for (i = 0;i < MAX_WATCHES;i ++)
  {
	WatchesCpuNo[ i ] = 0;
	Watches[ i ] = MAX_ADDRESS(WatchesCpuNo[ i ]);
  }

  WatchX = Machine->uixmin;
  WatchY = Machine->uiymin;
  WatchesFlag = 0;

  WatchGfxLen = 0;
  WatchGfxPos = 0;

  LoadDatabases(0);

  RebuildTables = 0;
  SearchCpuNo = 0;
  InitMemoryAreas();

}

void DisplayActiveCheats(int y)
{
  int i;

  xprintf(0, 0, y, "Active Cheats: %d", ActiveCheatTotal);
  y += ( FontHeight * 3 / 2 );

  if (ActiveCheatTotal == 0)
	xprintf(0, 0, y, "--- None ---");

  for (i = 0;i < ActiveCheatTotal;i ++)
  {
	if (y > MachHeight - 3 * FontHeight)
	{
		xprintf(0, 0, y, downarrow);
		break;
	}
	xprintf(0, 0 ,y, "%-30s", ActiveCheatTable[i].Name);
	y += FontHeight;
  }
}

/* Display watches if there are some */
void DisplayWatches(int ClrScr, int *x,int *y,char *buffer,
				int highlight, int dx, int dy)
{
	  int i;
	  char bufadr[4];

	  int FirstWatch = 1;

	int WatchColor = DT_COLOR_WHITE;

	int trueorientation;

	/* hack: force the display into standard orientation */
	trueorientation = Machine->orientation;
	Machine->orientation = Machine->ui_orientation;

	if (ClrScr)
	{
		WatchGfxPos = 0;
		for (i = 0;i < (int)strlen(buffer);i ++)
		{
			if ((buffer[i] == '+') || (buffer[i] == '-'))
				continue;

			drawgfx(Machine->scrbitmap,Machine->uifont,' ',DT_COLOR_WHITE,0,0,
				*x+dx+(WatchGfxPos),*y+dy,0,TRANSPARENCY_NONE,0);

			if (buffer[i] == ' ')
				WatchGfxPos += (FontWidth / 2);
			else
				WatchGfxPos += (FontWidth);
		}
	}

	if ((WatchesFlag != 0) && (WatchEnabled != 0))
	{
		WatchGfxLen = 0;
		buffer[0] = 0;
		for (i = 0;i < MAX_WATCHES;i ++)
		{
			if ( Watches[i] != MAX_ADDRESS(WatchesCpuNo[i]))
			{
			if (!FirstWatch)	/* If not 1st watch add a space */
			{
				strcat(buffer," ");
				WatchGfxLen += (FontWidth / 2);
			}
				sprintf(bufadr,"%02X", RD_GAMERAM (WatchesCpuNo[i], Watches[i]));

				if (highlight == i)
					strcat(buffer,"+");
				strcat(buffer,bufadr);
				if (highlight == i)
					strcat(buffer,"-");

				WatchGfxLen += (FontWidth * 2);
				FirstWatch = 0;
			}
		}

		/* Adjust x offset to fit the screen */
		/* while (	(*x >= (MachWidth - (FontWidth * (int)strlen(buffer)))) && */
		while ( (*x >= (MachWidth - WatchGfxLen)) &&
			(*x > Machine->uixmin)	)
		(*x)--;

		WatchGfxPos = 0;
		for (i = 0;i < (int)strlen(buffer);i ++)
		{
			if (buffer[i] == '+')
			{
				WatchColor = DT_COLOR_YELLOW;
				continue;
			}
			if (buffer[i] == '-')
			{
				WatchColor = DT_COLOR_WHITE;
				continue;
			}

			drawgfx(Machine->scrbitmap,Machine->uifont,buffer[i],WatchColor,0,0,
				*x+(WatchGfxPos),*y,0,TRANSPARENCY_NONE,0);
			/*	*x+(i*FontWidth),*y,0,TRANSPARENCY_NONE,0); */

			if (buffer[i] == ' ')
				WatchGfxPos += (FontWidth / 2);
			else
				WatchGfxPos += (FontWidth);
		}
	}

	Machine->orientation = trueorientation;
}

/* copy one cheat structure to another */
void set_cheat(struct cheat_struct *dest, struct cheat_struct *src)
{
	/* Changed order to match structure - Added field More */
	struct cheat_struct new_cheat =
	{
		0,				/* CpuNo */
		0,				/* Address */
		0,				/* Data */
		0,				/* Special */
		0,				/* Count */
		0,				/* Backup */
		0,				/* Minimum */
		0xFF,				/* Maximum */
		"---- New Cheat ----",	/* Name */
		""				/* More */
	};

	if (src == NEW_CHEAT)
	{
		src = &new_cheat;
	}

	dest->CpuNo = src->CpuNo;
	dest->Address	= src->Address;
	dest->Data		= src->Data;
	dest->Special	= src->Special;
	dest->Count = src->Count;
	dest->Backup	= src->Backup;
	dest->Minimum	= src->Minimum;
	dest->Maximum	= src->Maximum;
	strcpy(dest->Name, src->Name);
	strcpy(dest->More, src->More);
}

void DeleteActiveCheatFromTable(int NoCheat)
{
  int i;
  if ((NoCheat > ActiveCheatTotal) || (NoCheat < 0))
	return;
  for (i = NoCheat; i < ActiveCheatTotal-1;i ++)
  {
	set_cheat(&ActiveCheatTable[i], &ActiveCheatTable[i + 1]);
  }
  ActiveCheatTotal --;
}

void DeleteLoadedCheatFromTable(int NoCheat)
{
  int i;
  if ((NoCheat > LoadedCheatTotal) || (NoCheat < 0))
	return;
  for (i = NoCheat; i < LoadedCheatTotal-1;i ++)
  {
	set_cheat(&LoadedCheatTable[i], &LoadedCheatTable[i + 1]);
  }
  LoadedCheatTotal --;
}

int FindFreeWatch(void)
{
  int i;
  for (i = 0;i < MAX_WATCHES;i ++)
  {
	if (Watches[i] == MAX_ADDRESS(WatchesCpuNo[i]))
		break;
  }
  return(i == MAX_WATCHES ? 0 : i+1);
}

int EditCheatHeader(void)
{
  int i = 0;
  char *paDisplayText[] = {
		"To edit a Cheat name, press",
		"<ENTER> when name is selected.",
		"To edit values or to select a",
		"pre-defined Cheat Name, use :",
		"<+> and Right arrow key: +1",
		"<-> and Left  arrow key: -1",
		"<1> ... <8>: +1 digit",
		"",
		"<F10>: Show Help + other keys",
		0 };

  struct DisplayText dt[20];

  while (paDisplayText[i])
  {
	if (i)
		dt[i].y = (dt[i - 1].y + FontHeight + 2);
	else
		dt[i].y = FIRSTPOS;
	dt[i].color = DT_COLOR_WHITE;
	dt[i].text = paDisplayText[i];
	dt[i].x = (MachWidth - FontWidth * strlen(dt[i].text)) / 2;
	if(dt[i].x > MachWidth)
		dt[i].x = 0;
	i++;
  }
  dt[i].text = 0; /* terminate array */
  displaytext(dt,0,1);
  return(dt[i-1].y + ( 3 * FontHeight ));
}

void EditCheat(int CheatNo)
{
  char *CheatNameList[] = {
	"Infinite Lives PL1",
	"Infinite Lives PL2",
	"Infinite Time",
	"Infinite Time PL1",
	"Infinite Time PL2",
	"Invincibility",
	"Invincibility PL1",
	"Invincibility PL2",
	"Infinite Energy",
	"Infinite Energy PL1",
	"Infinite Energy PL2",
	"Select Next Level",
	"Select Current level",
	"Infinite Ammo",
	"Infinite Ammo PL1",
	"Infinite Ammo PL2",
	"Infinite Bombs",
	"Infinite Bombs PL1",
	"Infinite Bombs PL2",
	"Select Score PL1",
	"Select Score PL2",
	"Drain all Energy Now! PL1",
	"Drain all Energy Now! PL2",
	"Infinite",
	"Always have",
	"Get",
	"Lose",
	"[                           ]",
	"---> <ENTER> To Edit <---",
	"\0" };

  int i,s,y,key,done;
  int total;
  struct DisplayText dt[20];
  char str2[6][40];
  int CurrentName;
  int EditYPos;

  char buffer[10];

  cheat_clearbitmap();

  y = EditCheatHeader();

  total = 0;

  if ((LoadedCheatTable[CheatNo].Special>=60) && (LoadedCheatTable[CheatNo].Special<=75))
	LoadedCheatTable[CheatNo].Data = LoadedCheatTable[CheatNo].Maximum;

  sprintf(str2[0],"Name: %s",LoadedCheatTable[CheatNo].Name);
  if ((FontWidth * (int)strlen(str2[0])) > MachWidth - Machine->uixmin)
	sprintf(str2[0],"%s",LoadedCheatTable[CheatNo].Name);
  sprintf(str2[1],"CPU:        %01X",LoadedCheatTable[CheatNo].CpuNo);

  sprintf(str2[2], FormatAddr(LoadedCheatTable[CheatNo].CpuNo,1),
				LoadedCheatTable[CheatNo].Address);

  sprintf(str2[3],"Value:    %03d  (0x%02X)",LoadedCheatTable[CheatNo].Data,LoadedCheatTable[CheatNo].Data);
  sprintf(str2[4],"Type:     %03d",LoadedCheatTable[CheatNo].Special);

  sprintf(str2[5],"More: %s",LoadedCheatTable[CheatNo].More);
  if ((FontWidth * (int)strlen(str2[5])) > MachWidth - Machine->uixmin)
	sprintf(str2[5],"%s",LoadedCheatTable[CheatNo].More);

  for (i = 0;i < 6;i ++)
  {
	dt[total].text = str2[i];
	dt[total].x = MachWidth / 2;
	if(MachWidth < 35*FontWidth)
		dt[total].x = 0;
	else
		dt[total].x -= 15*FontWidth;
	dt[total].y = y;
	dt[total].color = DT_COLOR_WHITE;
	total++;
	y += FontHeight;
  }

  dt[total].text = 0; /* terminate array */

  EditYPos = ( y + ( 4 * FontHeight ) );

  s = 0;
  CurrentName = -1;

  oldkey = 0;

  done = 0;
  do
  {

	for (i = 0;i < total;i++)
		dt[i].color = (i == s) ? DT_COLOR_YELLOW : DT_COLOR_WHITE;
	displaytext(dt,0,1);

	/* key = keyboard_read_sync(); */
	key = cheat_readkey();	  /* MSH 990217 */
	switch (key)
	{
		case KEYCODE_DOWN:
		case KEYCODE_2_PAD:
			if (s < total - 1)
				s++;
			else
				s = 0;
			break;

		case KEYCODE_UP:
		case KEYCODE_8_PAD:
			if (s > 0)
				s--;
			else
				s = total - 1;
			break;

		case KEYCODE_LEFT:
		case KEYCODE_4_PAD:
			switch (s)
			{
				case 0:    /* Name */

					if (CurrentName < 0)
						CurrentName = 0;

					if (CurrentName == 0)						/* wrap if necessary*/
						while (CheatNameList[CurrentName][0])
							CurrentName++;
					CurrentName--;
					strcpy (LoadedCheatTable[CheatNo].Name, CheatNameList[CurrentName]);
					sprintf (str2[0],"Name: %s", LoadedCheatTable[CheatNo].Name);
					ClearTextLine(1, dt[0].y);
					break;
				case 1:    /* CpuNo */
					if (ManyCpus)
					{
						if (LoadedCheatTable[CheatNo].CpuNo == 0)
							LoadedCheatTable[CheatNo].CpuNo = cpu_gettotalcpu() - 1;
						else
							LoadedCheatTable[CheatNo].CpuNo --;
						sprintf (str2[1], "CPU:        %01X", LoadedCheatTable[CheatNo].CpuNo);
					}
					break;
				case 2:    /* Address */
					if (LoadedCheatTable[CheatNo].Address == 0)
						LoadedCheatTable[CheatNo].Address = MAX_ADDRESS(LoadedCheatTable[CheatNo].CpuNo);
					else
						LoadedCheatTable[CheatNo].Address --;
					sprintf(str2[2], FormatAddr(LoadedCheatTable[CheatNo].CpuNo,1),
						LoadedCheatTable[CheatNo].Address);
					break;
				case 3:    /* Data */
					if (LoadedCheatTable[CheatNo].Data == 0)
						LoadedCheatTable[CheatNo].Data = 0xFF;
					else
						LoadedCheatTable[CheatNo].Data --;
					sprintf(str2[3], "Value:    %03d  (0x%02X)", LoadedCheatTable[CheatNo].Data,
						LoadedCheatTable[CheatNo].Data);
					break;
				case 4:    /* Special */
					if (LoadedCheatTable[CheatNo].Special <= 0)
						LoadedCheatTable[CheatNo].Special = TOTAL_CHEAT_TYPES + OFFSET_LINK_CHEAT;
					else
					switch (LoadedCheatTable[CheatNo].Special)
						{
							case 20:
								LoadedCheatTable[CheatNo].Special = 11;
								break;
							case 40:
								LoadedCheatTable[CheatNo].Special = 24;
								break;
							case 60:
								LoadedCheatTable[CheatNo].Special = 44;
								break;
							case 70:
								LoadedCheatTable[CheatNo].Special = 65;
								break;
							case OFFSET_LINK_CHEAT:
								LoadedCheatTable[CheatNo].Special = 75;
								break;
							case 20 + OFFSET_LINK_CHEAT:
								LoadedCheatTable[CheatNo].Special = 11 + OFFSET_LINK_CHEAT;
								break;
							case 40 + OFFSET_LINK_CHEAT:
								LoadedCheatTable[CheatNo].Special = 24 + OFFSET_LINK_CHEAT;
								break;
							case 60 + OFFSET_LINK_CHEAT:
								LoadedCheatTable[CheatNo].Special = 44 + OFFSET_LINK_CHEAT;
								break;
							case 70 + OFFSET_LINK_CHEAT:
								LoadedCheatTable[CheatNo].Special = 65 + OFFSET_LINK_CHEAT;
								break;
							default:
								LoadedCheatTable[CheatNo].Special --;
								break;
						}

					sprintf(str2[4],"Type:     %03d",LoadedCheatTable[CheatNo].Special);
					break;
			}
			break;

		case KEYCODE_RIGHT:
		case KEYCODE_6_PAD:
			switch (s)
			{
				case 0:    /* Name */
					CurrentName ++;
					if (CheatNameList[CurrentName][0] == 0)
						CurrentName = 0;
					strcpy (LoadedCheatTable[CheatNo].Name, CheatNameList[CurrentName]);
					sprintf (str2[0], "Name: %s", LoadedCheatTable[CheatNo].Name);
					ClearTextLine(1, dt[0].y);
					break;
				case 1:    /* CpuNo */
					if (ManyCpus)
					{
						LoadedCheatTable[CheatNo].CpuNo ++;
						if (LoadedCheatTable[CheatNo].CpuNo >= cpu_gettotalcpu())
							LoadedCheatTable[CheatNo].CpuNo = 0;
						sprintf(str2[1],"CPU:        %01X",LoadedCheatTable[CheatNo].CpuNo);
					}
					break;
				case 2:    /* Address */
					LoadedCheatTable[CheatNo].Address ++;
					if (LoadedCheatTable[CheatNo].Address > MAX_ADDRESS(LoadedCheatTable[CheatNo].CpuNo))
						LoadedCheatTable[CheatNo].Address = 0;
					sprintf (str2[2], FormatAddr(LoadedCheatTable[CheatNo].CpuNo,1),
						LoadedCheatTable[CheatNo].Address);
					break;
				case 3:    /* Data */
					if(LoadedCheatTable[CheatNo].Data == 0xFF)
						LoadedCheatTable[CheatNo].Data = 0;
					else
						LoadedCheatTable[CheatNo].Data ++;
					sprintf(str2[3],"Value:    %03d  (0x%02X)",LoadedCheatTable[CheatNo].Data,
						LoadedCheatTable[CheatNo].Data);
					break;
				case 4:    /* Special */
					if (LoadedCheatTable[CheatNo].Special >= TOTAL_CHEAT_TYPES + OFFSET_LINK_CHEAT)
						LoadedCheatTable[CheatNo].Special = 0;
					else
					switch (LoadedCheatTable[CheatNo].Special)
						{
							case 11:
								LoadedCheatTable[CheatNo].Special = 20;
								break;
							case 24:
								LoadedCheatTable[CheatNo].Special = 40;
								break;
							case 44:
								LoadedCheatTable[CheatNo].Special = 60;
								break;
							case 65:
								LoadedCheatTable[CheatNo].Special = 70;
								break;
							case 75:
								LoadedCheatTable[CheatNo].Special = OFFSET_LINK_CHEAT;
								break;
							case 11 + OFFSET_LINK_CHEAT:
								LoadedCheatTable[CheatNo].Special = 20 + OFFSET_LINK_CHEAT;
								break;
							case 24 + OFFSET_LINK_CHEAT:
								LoadedCheatTable[CheatNo].Special = 40 + OFFSET_LINK_CHEAT;
								break;
							case 44 + OFFSET_LINK_CHEAT:
								LoadedCheatTable[CheatNo].Special = 60 + OFFSET_LINK_CHEAT;
								break;
							case 65 + OFFSET_LINK_CHEAT:
								LoadedCheatTable[CheatNo].Special = 70 + OFFSET_LINK_CHEAT;
								break;
							default:
								LoadedCheatTable[CheatNo].Special ++;
								break;
						}

					sprintf(str2[4],"Type:     %03d",LoadedCheatTable[CheatNo].Special);
					break;
			}
			break;

		case KEYCODE_HOME:
		case KEYCODE_7_PAD:
			switch (s)
			{
				case 3: /* Data */
					if (LoadedCheatTable[CheatNo].Data >= 0x80)
						LoadedCheatTable[CheatNo].Data -= 0x80;
					sprintf(str2[3], "Value:    %03d  (0x%02X)", LoadedCheatTable[CheatNo].Data,
						LoadedCheatTable[CheatNo].Data);
					break;
				case 4: /* Special */
					if (LoadedCheatTable[CheatNo].Special >= OFFSET_LINK_CHEAT)
						LoadedCheatTable[CheatNo].Special -= OFFSET_LINK_CHEAT;
					sprintf(str2[4],"Type:     %03d",LoadedCheatTable[CheatNo].Special);
					break;
			}
			break;

		case KEYCODE_END:
		case KEYCODE_1_PAD:
			switch (s)
			{
				case 3: /* Data */
					if (LoadedCheatTable[CheatNo].Data < 0x80)
						LoadedCheatTable[CheatNo].Data += 0x80;
					sprintf(str2[3], "Value:    %03d  (0x%02X)", LoadedCheatTable[CheatNo].Data,
						LoadedCheatTable[CheatNo].Data);
					break;
				case 4: /* Special */
					if (LoadedCheatTable[CheatNo].Special < OFFSET_LINK_CHEAT)
						LoadedCheatTable[CheatNo].Special += OFFSET_LINK_CHEAT;
					sprintf(str2[4],"Type:     %03d",LoadedCheatTable[CheatNo].Special);
					break;
			}
			break;

		case KEYCODE_8:
			if (ADDRESS_BITS(LoadedCheatTable[CheatNo].CpuNo) < 29) break;
		case KEYCODE_7:
			if (ADDRESS_BITS(LoadedCheatTable[CheatNo].CpuNo) < 25) break;
		case KEYCODE_6:
			if (ADDRESS_BITS(LoadedCheatTable[CheatNo].CpuNo) < 21) break;
		case KEYCODE_5:
			if (ADDRESS_BITS(LoadedCheatTable[CheatNo].CpuNo) < 17) break;
		case KEYCODE_4:
			if (ADDRESS_BITS(LoadedCheatTable[CheatNo].CpuNo) < 13) break;
		case KEYCODE_3:
		case KEYCODE_2:
		case KEYCODE_1:
			/* these keys only apply to the address line */
			if (s == 2)
			{
				int addr = LoadedCheatTable[CheatNo].Address;	/* copy address*/
				int digit = (KEYCODE_8 - key);	/* if key is KEYCODE_8, digit = 0 */
				int mask;

				/* adjust digit based on cpu address range */
				digit -= (8 - (ADDRESS_BITS(LoadedCheatTable[CheatNo].CpuNo)+3) / 4);

				mask = 0xF << (digit * 4);	/* if digit is 1, mask = 0xf0 */

				do
				{
				if ((addr & mask) == mask)
					/* wrap hex digit around to 0 if necessary */
					addr &= ~mask;
				else
					/* otherwise bump hex digit by 1 */
					addr += (0x1 << (digit * 4));
				} while (addr > MAX_ADDRESS(LoadedCheatTable[CheatNo].CpuNo));

				LoadedCheatTable[CheatNo].Address = addr;
				sprintf(str2[2], FormatAddr(LoadedCheatTable[CheatNo].CpuNo,1),
					LoadedCheatTable[CheatNo].Address);
			}
			break;

		case KEYCODE_F3:
			while (keyboard_pressed(key));
		  oldkey = 0;
			switch (s)
			{
				case 2:    /* Address */
					for (i = 0; i < total; i++)
						dt[i].color = DT_COLOR_WHITE;
					displaytext (dt, 0,1);
					xprintf (0, 0, EditYPos-2*FontHeight, "Edit Cheat Address:");
					sprintf(buffer, FormatAddr(LoadedCheatTable[CheatNo].CpuNo,0),
						LoadedCheatTable[CheatNo].Address);
				xedit(0, EditYPos, buffer, strlen(buffer), 1);
					sscanf(buffer,"%X", &LoadedCheatTable[CheatNo].Address);
					if (LoadedCheatTable[CheatNo].Address > MAX_ADDRESS(LoadedCheatTable[CheatNo].CpuNo))
						LoadedCheatTable[CheatNo].Address = MAX_ADDRESS(LoadedCheatTable[CheatNo].CpuNo);
					sprintf(str2[2], FormatAddr(LoadedCheatTable[CheatNo].CpuNo,1),
						LoadedCheatTable[CheatNo].Address);
					cheat_clearbitmap();
					y = EditCheatHeader();
				  break;
			}
			break;

		case KEYCODE_F10:
			while (keyboard_pressed(key));
		  oldkey = 0;
			EditCheatHelp();
			y = EditCheatHeader();
			break;

		case KEYCODE_ENTER:
		case KEYCODE_ENTER_PAD:
			while (keyboard_pressed(key));
		  oldkey = 0;
			switch (s)
			{
				case 0:    /* Name */
					for (i = 0; i < total; i++)
						dt[i].color = DT_COLOR_WHITE;
					displaytext (dt, 0,1);
					xprintf (0, 0, EditYPos-2*FontHeight, "Edit Cheat Description:");
				xedit(0, EditYPos, LoadedCheatTable[CheatNo].Name, CHEAT_NAME_MAXLEN, 0);
					sprintf (str2[0], "Name: %s", LoadedCheatTable[CheatNo].Name);
					if ((FontWidth * (int)strlen(str2[0])) > MachWidth - Machine->uixmin)
						sprintf(str2[0],"%s",LoadedCheatTable[CheatNo].Name);
					cheat_clearbitmap();
					y = EditCheatHeader();
				  break;
				case 5:    /* More */
					for (i = 0; i < total; i++)
						dt[i].color = DT_COLOR_WHITE;
					displaytext (dt, 0,1);
					xprintf (0, 0, EditYPos-2*FontHeight, "Edit Cheat More Description:");
				xedit(0, EditYPos, LoadedCheatTable[CheatNo].More, CHEAT_NAME_MAXLEN, 0);
					sprintf (str2[5], "More: %s", LoadedCheatTable[CheatNo].More);
					if ((FontWidth * (int)strlen(str2[5])) > MachWidth - Machine->uixmin)
						sprintf(str2[5],"%s",LoadedCheatTable[CheatNo].More);
					cheat_clearbitmap();
					y = EditCheatHeader();
				  break;
			}
			break;

		case KEYCODE_ESC:
		case KEYCODE_TAB:
			while (keyboard_pressed(key));
			done = 1;
			break;
	}
  } while (done == 0);

  while (keyboard_pressed(key));

  if (	(LoadedCheatTable[CheatNo].Special==62) || (LoadedCheatTable[CheatNo].Special==65)	||
	(LoadedCheatTable[CheatNo].Special==72) || (LoadedCheatTable[CheatNo].Special==75)	)
	LoadedCheatTable[CheatNo].Minimum = 1;
  else
	LoadedCheatTable[CheatNo].Minimum = 0;
  if ((LoadedCheatTable[CheatNo].Special>=60) && (LoadedCheatTable[CheatNo].Special<=75))
  {
	LoadedCheatTable[CheatNo].Maximum = LoadedCheatTable[CheatNo].Data;
	LoadedCheatTable[CheatNo].Data = 0;
  }
  else
	LoadedCheatTable[CheatNo].Maximum = 0xFF;

  cheat_clearbitmap();
}


/* VM 981213 BEGIN */
/*
 * I don't fully understand how dt and str2 are utilized, so for now I pass them as parameters
 * until I can figure out which function they ideally belong in.
 * They should not be turned into globals; this program has too many globals as it is.
 */
int build_cheat_list(int Index, struct DisplayText *ext_dt, char ext_str2[MAX_DT + 1][40])
{
	int total = 0;
	while (total < MAX_DISPLAYCHEATS)
	{
		if (Index + total >= LoadedCheatTotal)
		{
			break;
		}

		AddCheckToName(total + Index, ext_str2[total]);
		ext_dt[total].text = ext_str2[total];

		total++;
	}
	ext_dt[total].text = 0; /* terminate array */

	/* Clear old list */
	ClearArea(1, YHEAD_SELECT, YFOOT_SELECT-1);

	return total;
}
/* VM 981213 END */

int SelectCheatHeader(void)
{
  int y = 0;

  cheat_clearbitmap();

  if (LoadedCheatTotal == 0)
  {
	xprintf(0, 0, y, "<INS>: Add New Cheat" );
	y += (FontHeight);
	xprintf(0, 0, y, "<F10>: Show Help + other keys");
	y += (FontHeight * 3);
	xprintf(0, 0, y, "No Cheats Available!");
  }
  else
  {
	xprintf(0, 0, y, "<DEL>: Delete  <INS>: Add" );
	y += (FontHeight);
	xprintf(0, 0, y, "<F1>: Save  <F2>: Watch");
	y += (FontHeight + 0);
	xprintf(0, 0, y, "<F3>: Edit  <F6>: Save all");
	y += (FontHeight);
	xprintf(0, 0, y, "<F4>: Copy  <F7>: All off");
	y += (FontHeight);
	xprintf(0, 0, y, "<ENTER>: Enable/Disable");
	y += (FontHeight);
	xprintf(0, 0, y, "<F10>: Show Help + other keys");
	y += (FontHeight + 4);
	xprintf(0, 0, y, "Select a Cheat (%d Total)", LoadedCheatTotal);
	y += (FontHeight + 4);
  }
  return(YHEAD_SELECT);
}

void SelectCheat(void)
{
	int i, x, y, highlighted, key, done, total;
	struct DisplayText dt[MAX_DT + 1];
	int flag;
	int Index;

	int BCDOnly, ZeroPoss;

	char str2[60][40];
	int j;

	char fmt[32];
	char buf[CHEAT_FILENAME_MAXLEN+1];

	cheat_clearbitmap();

	if (MachWidth < FontWidth * 35)
	{
		x = 0;
	}
	else
	{
		x = (MachWidth / 2) - (FontWidth * 16);
	}
	y = SelectCheatHeader();

	/* Make the list */
	for (i = 0; i < MAX_DISPLAYCHEATS; i++)
	{
		dt[i].x = x;
		dt[i].y = y;
		dt[i].color = DT_COLOR_WHITE;
		y += FontHeight;
	}

	Index = 0;
	highlighted = 0;

	Index = (SaveIndex / MAX_DISPLAYCHEATS) * MAX_DISPLAYCHEATS;
	highlighted = SaveIndex - Index;

	total = build_cheat_list(Index, dt, str2);

	y += (FontHeight * 2);

	oldkey = 0;

	done = 0;
	do
	{
		for (i = 0; i < total; i++)
		{
			dt[i].color = (i == highlighted) ? DT_COLOR_YELLOW : DT_COLOR_WHITE;
		}

		displaytext(dt, 0, 1);

		/* key = keyboard_read_sync(); */
		key = cheat_readkey();	  /* MSH 990217 */
		ClearTextLine(1, YFOOT_SELECT);
		switch (key)
		{
			case KEYCODE_DOWN:
			case KEYCODE_2_PAD:

				if (highlighted < total - 1)
				{
					highlighted++;
				}
				else
				{
					highlighted = 0;

					if (LoadedCheatTotal <= MAX_DISPLAYCHEATS)
					{
						break;
					}

					if (LoadedCheatTotal > Index + MAX_DISPLAYCHEATS)
					{
						Index += MAX_DISPLAYCHEATS;
					}
					else
					{
						Index = 0;

					}

					total = build_cheat_list(Index, dt, str2);
				}
				break;

			case KEYCODE_UP:
			case KEYCODE_8_PAD:
				if (highlighted > 0)
				{
					highlighted--;
				}
				else
				{
					highlighted = total - 1;
					if (LoadedCheatTotal <= MAX_DISPLAYCHEATS)
					{
						break;
					}

					if (Index == 0)
					{
						Index = ((LoadedCheatTotal - 1) / MAX_DISPLAYCHEATS) * MAX_DISPLAYCHEATS;
					}
					else if (Index > MAX_DISPLAYCHEATS)
					{
						Index -= MAX_DISPLAYCHEATS;
					}
					else
					{
						Index = 0;
					}

					total = build_cheat_list(Index, dt, str2);
					highlighted = total - 1;
				}
				break;

			case KEYCODE_HOME:
			case KEYCODE_7_PAD:
				Index = 0;
				total = build_cheat_list(Index, dt, str2);
				highlighted = 0;
				break;

			case KEYCODE_END:
			case KEYCODE_1_PAD:
				Index = ((LoadedCheatTotal - 1) / MAX_DISPLAYCHEATS) * MAX_DISPLAYCHEATS;
				total = build_cheat_list(Index, dt, str2);
				highlighted = total - 1;
				break;

			case KEYCODE_PGDN:
			case KEYCODE_3_PAD:
				if (highlighted + Index >= LoadedCheatTotal - MAX_DISPLAYCHEATS)
				{
					Index = ((LoadedCheatTotal - 1) / MAX_DISPLAYCHEATS) * MAX_DISPLAYCHEATS;
					highlighted = (LoadedCheatTotal - 1) - Index;
				}
				else
				{
					Index += MAX_DISPLAYCHEATS;
				}

				total = build_cheat_list(Index, dt, str2);

				break;

			case KEYCODE_PGUP:
			case KEYCODE_9_PAD:
				if (highlighted + Index <= MAX_DISPLAYCHEATS)
				{
					Index = 0;
					highlighted = 0;
				}
				else
				{
					Index -= MAX_DISPLAYCHEATS;
				}

				total = build_cheat_list(Index, dt, str2);

				break;

			case KEYCODE_INSERT:	/* Add a new empty cheat */
			case KEYCODE_0_PAD:
				while (keyboard_pressed(key));
			  oldkey = 0;

				if (LoadedCheatTotal > MAX_LOADEDCHEATS -1)
				{
					xprintf(0, 0, YFOOT_SELECT, "(Cheat List Is Full.)" );
					break;
				}

				set_cheat(&LoadedCheatTable[LoadedCheatTotal], NEW_CHEAT);
				LoadedCheatTotal++;

				total = build_cheat_list(Index, dt, str2);

				SelectCheatHeader();

				break;

			case KEYCODE_DEL:
			case KEYCODE_DEL_PAD:
				while (keyboard_pressed(key));
			  oldkey = 0;
				if (LoadedCheatTotal == 0)
				{
					break;
				}

				/* Erase the current cheat from the list */
				/* But before, erase it from the active list if it is there */
				for (i = 0;i < ActiveCheatTotal;i ++)
				{
					if (ActiveCheatTable[i].Address == LoadedCheatTable[highlighted + Index].Address)
						if (ActiveCheatTable[i].Data == LoadedCheatTable[highlighted + Index].Data)
						{
							/* The selected Cheat is already in the list then delete it.*/
							DeleteActiveCheatFromTable(i);
							break;
						}
				}
				/* Delete entry from list */
				DeleteLoadedCheatFromTable(highlighted + Index);

				total = build_cheat_list(Index, dt, str2);
				if (total <= highlighted)
				{
					highlighted = total - 1;
				}

				if ((total == 0) && (Index != 0))
				{
					/* The page is empty so backup one page */
					if (Index == 0)
					{
						Index = ((LoadedCheatTotal - 1) / MAX_DISPLAYCHEATS) * MAX_DISPLAYCHEATS;
					}
					else if (Index > MAX_DISPLAYCHEATS)
					{
						Index -= MAX_DISPLAYCHEATS;
					}
					else
					{
						Index = 0;
					}

					total = build_cheat_list(Index, dt, str2);
					highlighted = total - 1;
				}

				SelectCheatHeader();

				break;

			case KEYCODE_BACKSPACE:
				/* Display comment about a cheat */
				while (keyboard_pressed(key)) ; /* wait for key release */
				if (LoadedCheatTable[highlighted + Index].More[0])
					xprintf (0, 0, YFOOT_SELECT, "%s", LoadedCheatTable[highlighted + Index].More);
				break;

			case KEYCODE_F1:	/* Save cheat to file */
				while (keyboard_pressed(key));
			  oldkey = 0;
				if (LoadedCheatTotal == 0)
				{
					break;
				}

				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
				{
					break;
				}

			j = SaveCheat(highlighted + Index);
				xprintf(0, 0, YFOOT_SELECT, "Cheat %sSaved to File %s", (j ? "" : "NOT "), database);

				break;

			case KEYCODE_F2:	 /* Add to watch list */
				while (keyboard_pressed(key));
			  oldkey = 0;
				if (LoadedCheatTotal == 0)
				{
					break;
				}

				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
				{
					break;
				}

				if (LoadedCheatTable[highlighted + Index].Special == COMMENTCHEAT)
				{
					xprintf (0, 0, YFOOT_SELECT, "Comment NOT Added as Watch");
					break;
				}

				if (LoadedCheatTable[highlighted + Index].CpuNo < cpu_gettotalcpu())
				{
					for (i = 0; i < MAX_WATCHES; i++)
					{
						if (Watches[i] == MAX_ADDRESS(WatchesCpuNo[i]))
						{
							WatchesCpuNo[i] = LoadedCheatTable[highlighted + Index].CpuNo;
							Watches[i] = LoadedCheatTable[highlighted + Index].Address;

							strcpy(fmt, FormatAddr(SearchCpuNo,0));
							sprintf (buf, fmt, Watches[i]);
							xprintf (0, 0, YFOOT_SELECT, "%s Added as Watch %d",buf,i+1);

							WatchesFlag = 1;
							WatchEnabled = 1;

							break;
						}
					}
				}
				break;

			case KEYCODE_F3:	/* Edit current cheat */
				while (keyboard_pressed(key));
			  oldkey = 0;
				if (LoadedCheatTotal == 0)
				{
					break;
				}

				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
				{
					break;
				}

				if (	(LoadedCheatTable[highlighted + Index].Special == COMMENTCHEAT) ||
					(LoadedCheatTable[highlighted + Index].Special == WATCHCHEAT)	)
					xedit(0, YFOOT_SELECT, LoadedCheatTable[highlighted + Index].Name,
						CHEAT_NAME_MAXLEN, 0);
				else
					EditCheat(highlighted+Index);

				total = build_cheat_list(Index, dt, str2);

			SelectCheatHeader();

				break;

			case KEYCODE_F4:	/* Copy the current cheat */
				while (keyboard_pressed(key));
			  oldkey = 0;
				if (LoadedCheatTotal == 0)
				{
					break;
				}

				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
				{
					break;
				}

				if (LoadedCheatTotal > MAX_LOADEDCHEATS - 1)
				{
					xprintf(0, 0, YFOOT_SELECT, "(Cheat List Is Full.)" );
					break;
				}

				set_cheat(&LoadedCheatTable[LoadedCheatTotal], &LoadedCheatTable[highlighted + Index]);
				LoadedCheatTable[LoadedCheatTotal].Count = 0;
				LoadedCheatTotal++;

				total = build_cheat_list(Index, dt, str2);

				SelectCheatHeader();

				break;

			case KEYCODE_F5:	/* Rename the cheatfile and reload the database */
				while (keyboard_pressed(key));
			  oldkey = 0;

				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
				{
					strcpy(buf, database);
					if (RenameCheatFile(1, 0, buf) == 1)
					{
						LoadCheat(1, buf);
						Index = 0;
						total = build_cheat_list(Index, dt, str2);
						highlighted = 0;
					}
				}
				else
				{
					if (RenameCheatFile(0, 1, database) == 1)
					{
						LoadCheat(0, database);
						Index = 0;
						total = build_cheat_list(Index, dt, str2);
						highlighted = 0;
					}
				}

			SelectCheatHeader();

				break;

			case KEYCODE_F6:	/* Save all cheats to file */
				while (keyboard_pressed(key));
			  oldkey = 0;
				if (LoadedCheatTotal == 0)
				{
					break;
				}

				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
				{
					break;
				}

			j = SaveCheat(-1);
				xprintf(0, 0, YFOOT_SELECT, "%d Cheats Saved to File %s", j, database);

				break;

			case KEYCODE_F7:	/* Remove all active cheats from the list */
				while (keyboard_pressed(key));
			  oldkey = 0;
				if (LoadedCheatTotal == 0)
				{
					break;
				}

				ActiveCheatTotal = 0;

				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
				{
					for (i = 0;i < LoadedCheatTotal;i ++)
					{
						if (ActiveCheatTotal > MAX_ACTIVECHEATS-1)
						{
							xprintf( 0, 0, YFOOT_SELECT, "(Limit Of Active Cheats)" );
							break;
						}

						if (	(LoadedCheatTable[i].Special != COMMENTCHEAT)	&&
							(LoadedCheatTable[i].Special != WATCHCHEAT) )
						{
							set_cheat(&ActiveCheatTable[ActiveCheatTotal], &LoadedCheatTable[i]);
							ActiveCheatTable[ActiveCheatTotal].Count = 0;

							ActiveCheatTotal ++;
							CheatEnabled = 1;
							he_did_cheat = 1;
						}
					}
				}

				total = build_cheat_list(Index, dt, str2);
				break;

			case KEYCODE_F8:	/* Reload the database */
				while (keyboard_pressed(key));
			  oldkey = 0;

				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
				{
					break;
				}

				LoadDatabases(1);

				Index = 0;
				total = build_cheat_list(Index, dt, str2);
				highlighted = 0;

			SelectCheatHeader();

				break;

			case KEYCODE_F9:	/* Rename the cheatfile */
				while (keyboard_pressed(key));
			  oldkey = 0;

				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
				{
					break;
				}

				RenameCheatFile(-1, 1, database);

			SelectCheatHeader();

				break;

			case KEYCODE_F10:	 /* Invoke help */
				while (keyboard_pressed(key));
			  oldkey = 0;

				if (LoadedCheatTotal == 0)
				{
					CheatListHelpEmpty();
				}
				else
				{
					CheatListHelp();
				}

				SelectCheatHeader();

				break;

			case KEYCODE_F11:	 /* Toggle sologame ON/OFF then reload the database */
				while (keyboard_pressed(key));
			  oldkey = 0;

				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
				{
					break;
				}

				sologame ^= 1;
				LoadDatabases(1);

				Index = 0;
				total = build_cheat_list(Index, dt, str2);
				highlighted = 0;

			SelectCheatHeader();

				break;

			case KEYCODE_F12:	 /* Display info about a cheat */
				while (keyboard_pressed(key));
			  oldkey = 0;
				if (LoadedCheatTotal == 0)
				{
					break;
				}

				if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
				{
					break;
				}

				if (LoadedCheatTable[highlighted + Index].Special == COMMENTCHEAT)
				{
					xprintf (0, 0, YFOOT_SELECT, "This is a Comment");
					break;
				}

				if (LoadedCheatTable[highlighted + Index].Special == WATCHCHEAT)
				{
					AddCpuToAddr(LoadedCheatTable[highlighted + Index].CpuNo,
							 LoadedCheatTable[highlighted + Index].Address,
							 RD_GAMERAM (LoadedCheatTable[highlighted + Index].CpuNo,
									 LoadedCheatTable[highlighted + Index].Address), buf);
					strcat(buf," [Watch]");
				}
				else
				{
					AddCpuToAddr(LoadedCheatTable[highlighted + Index].CpuNo,
							 LoadedCheatTable[highlighted + Index].Address,
							 LoadedCheatTable[highlighted + Index].Data, fmt);
					strcat(fmt," [Type %d]");
					sprintf(buf, fmt, LoadedCheatTable[highlighted + Index].Special);
				}

				xprintf(0, 0, YFOOT_SELECT, "%s", buf);

				break;

			case KEYCODE_ENTER:
			case KEYCODE_ENTER_PAD:
			case KEYCODE_SPACE:
				while (keyboard_pressed(key));
			  oldkey = 0;
				if (total == 0)
				{
					break;
				}

				if (LoadedCheatTable[highlighted + Index].Special == COMMENTCHEAT)
				{
					xprintf (0, 0, YFOOT_SELECT, "Comment NOT Activated");
					break;
				}

				if (LoadedCheatTable[highlighted + Index].Special == WATCHCHEAT)
				{
					if (LoadedCheatTable[highlighted + Index].CpuNo < cpu_gettotalcpu())  /* watches are for all cpus */
					{
						for (i = 0; i < MAX_WATCHES; i++)
						{
							if (Watches[i] == MAX_ADDRESS(WatchesCpuNo[i]))
							{
								WatchesCpuNo[i] = LoadedCheatTable[highlighted + Index].CpuNo;Watches[i] = LoadedCheatTable[highlighted + Index].Address;

								strcpy(fmt, FormatAddr(SearchCpuNo,0));
								sprintf (buf, fmt, Watches[i]);
								xprintf (0, 0, YFOOT_SELECT, "%s Added as Watch %d",buf,i+1);

								WatchesFlag = 1;
								WatchEnabled = 1;
								break;
							}
						}
					}
					break;
				}

				flag = 0;
				for (i = 0;i < ActiveCheatTotal;i ++)
				{
					if (ActiveCheatTable[i].Address == LoadedCheatTable[highlighted + Index].Address)
					{
						if (	((ActiveCheatTable[i].Special >=  20) && (ActiveCheatTable[i].Special <=  24))	||
							((ActiveCheatTable[i].Special >=  40) && (ActiveCheatTable[i].Special <=  44))	||
							((ActiveCheatTable[i].Special >=  20 + OFFSET_LINK_CHEAT) && (ActiveCheatTable[i].Special <= 24 + OFFSET_LINK_CHEAT))	||
							((ActiveCheatTable[i].Special >=  40 + OFFSET_LINK_CHEAT) && (ActiveCheatTable[i].Special <= 44 + OFFSET_LINK_CHEAT))	)
						{
							if (	(ActiveCheatTable[i].Special	!= LoadedCheatTable[highlighted + Index].Special)	||
								(ActiveCheatTable[i].Data	!= LoadedCheatTable[highlighted + Index].Data	)	)
								continue;
						}

						/* The selected Cheat is already in the list then delete it.*/
						DeleteActiveCheatFromTable(i);
						flag = 1;

						/* Delete linked cheats */
						while (i < ActiveCheatTotal)
						{
							if ((ActiveCheatTable[i].Special < OFFSET_LINK_CHEAT) ||
									(ActiveCheatTable[i].Special == COMMENTCHEAT) ||
									(ActiveCheatTable[i].Special == WATCHCHEAT))
								break;
							DeleteActiveCheatFromTable(i);
						}

						break;
					}
				}

				/* Add the selected cheat to the active cheats list if it was not already there */
				if (flag == 0)
				{
					/* No more than MAX_ACTIVECHEATS cheat at the time */
					if (ActiveCheatTotal > MAX_ACTIVECHEATS-1)
					{
						xprintf( 0, 0, YFOOT_SELECT, "(Limit Of Active Cheats)" );
						break;
					}

					set_cheat(&ActiveCheatTable[ActiveCheatTotal], &LoadedCheatTable[highlighted + Index]);
					ActiveCheatTable[ActiveCheatTotal].Count = 0;
					ValTmp = 0;

					if (	(ActiveCheatTable[ActiveCheatTotal].Special>=60)	&&
						(ActiveCheatTable[ActiveCheatTotal].Special<=75)	)
					{
						ActiveCheatTable[ActiveCheatTotal].Data =
							RD_GAMERAM (ActiveCheatTable[ActiveCheatTotal].CpuNo,
									ActiveCheatTable[ActiveCheatTotal].Address);
						BCDOnly = ( (ActiveCheatTable[ActiveCheatTotal].Special == 63)	||
								(ActiveCheatTable[ActiveCheatTotal].Special == 64)	||
								(ActiveCheatTable[ActiveCheatTotal].Special == 65)	||
								(ActiveCheatTable[ActiveCheatTotal].Special == 73)	||
								(ActiveCheatTable[ActiveCheatTotal].Special == 74)	||
								(ActiveCheatTable[ActiveCheatTotal].Special == 75)	);
						ZeroPoss = ((ActiveCheatTable[ActiveCheatTotal].Special == 60)	||
								(ActiveCheatTable[ActiveCheatTotal].Special == 63)	||
								(ActiveCheatTable[ActiveCheatTotal].Special == 70)	||
								(ActiveCheatTable[ActiveCheatTotal].Special == 73)	);

						ValTmp = SelectValue(ActiveCheatTable[ActiveCheatTotal].Data,
										BCDOnly, ZeroPoss, 0, 0,
										ActiveCheatTable[ActiveCheatTotal].Minimum,
										ActiveCheatTable[ActiveCheatTotal].Maximum,
										"%03d", "Enter the new Value:", 1,
										FIRSTPOS + 3 * FontHeight);
						if (ValTmp > NOVALUE)
							ActiveCheatTable[ActiveCheatTotal].Data = ValTmp;
					}

					if (ValTmp > NOVALUE)
					{
						ActiveCheatTotal ++;
						CheatEnabled = 1;
						he_did_cheat = 1;

						/* Activate linked cheats */
						for (i = highlighted + Index + 1; i < LoadedCheatTotal && ActiveCheatTotal < MAX_ACTIVECHEATS; i++)
						{
							if ((LoadedCheatTable[i].Special < OFFSET_LINK_CHEAT) ||
									(LoadedCheatTable[i].Special == COMMENTCHEAT) ||
									(LoadedCheatTable[i].Special == WATCHCHEAT))
								break;
							set_cheat(&ActiveCheatTable[ActiveCheatTotal], &LoadedCheatTable[i]);
							ActiveCheatTable[ActiveCheatTotal].Count = 0;
							ActiveCheatTotal ++;
						}
					}
				}

				cheat_clearbitmap();

			SelectCheatHeader();

				total = build_cheat_list(Index, dt, str2);

				break;

			case KEYCODE_ESC:
			case KEYCODE_TAB:
				while (keyboard_pressed(key));

				done = 1;
				break;
		}
	}
	while (done == 0);

	while (keyboard_pressed(key));

	  SaveIndex = Index + highlighted;

	/* clear the screen before returning */
	cheat_clearbitmap();
}

int SelectSearchValueHeader(void)
{
  int y = FIRSTPOS + (2 * FontHeight);

  cheat_clearbitmap();

  xprintf(0, 0,y,"Choose One Of The Following:");
  y += 2*FontHeight;
  return(y);
}

int StartSearchHeader(void)
{
  int y = FIRSTPOS;

  cheat_clearbitmap();

  xprintf(0, 0, y, "<F10>: Show Help");
  y += 2*FontHeight;
  xprintf(0, 0,y,"Choose One Of The Following:");
  y += 2*FontHeight;
  return(y);
}

int ContinueSearchHeader(void)
{
  int y = FIRSTPOS;

  cheat_clearbitmap();

  xprintf(0, 0, y, "<F1>: Start A New Search");
  y += 2*FontHeight;

  switch (CurrentMethod)
  {
	case SEARCH_VALUE:
		xprintf(0, 0,y,"Enter The New Value:");
		break;
	case SEARCH_TIME:
		xprintf(0, 0,y,"Enter How Much The Value");
		y += FontHeight;
		if ( iCheatInitialized )
		{
			xprintf(0, 0,y,"Has Changed Since You");
			y += FontHeight;
			xprintf(0, 0,y,"Started The Search:");
		}
		else
		{
			xprintf(0, 0,y,"Has Changed Since The");
			y += FontHeight;
			xprintf(0, 0,y,"Last Check:");
		}
		break;
	case SEARCH_ENERGY:
		xprintf(0, 0,y,"Choose The Expression That");
		y += FontHeight;
		xprintf(0, 0,y,"Specifies What Occured Since");
		y += FontHeight;
		if ( iCheatInitialized )
			xprintf(0, 0,y,"You Started The Search:");
		else
			xprintf(0, 0,y,"The Last Check:");
		break;
	case SEARCH_BIT:
		xprintf(0, 0,y,"Choose One Of The Following:");
		break;
	case SEARCH_BYTE:
		xprintf(0, 0,y,"Choose One Of The Following:");
		break;
  }

  y += 2*FontHeight;
  return(y);
}

int ContinueSearchMatchHeader(int count)
{
  int y = 0;
  char *str;

  xprintf(0, 0,y,"Matches Found: %d",count);
  if (count > MAX_MATCHES)
	str = "Here Are some Matches:";
  else
	if (count != 0)
		str = "Here Is The List:";
	else
		str = "(No Matches Found)";
  y += 2*FontHeight;
  xprintf(0, 0,y,"%s",str);
  return(y);
}

void ContinueSearchMatchFooter(int count, int idx)
{
  int y = YFOOT_MATCH;

  y += FontHeight;
  if (LoadedCheatTotal >= MAX_LOADEDCHEATS)
	xprintf(0, 0,y,"(Cheat List Is Full.)");
  else
	ClearTextLine(1, y);
  y += 2 * FontHeight;

  if ((count > MAX_MATCHES) && (idx != 0))
	xprintf(0, 0,y,"<HOME>: First page");
  else
	ClearTextLine(1, y);
  y += FontHeight;

  if (count > idx+MAX_MATCHES)
	xprintf(0, 0,y,"<PAGE DOWN>: Scroll");
  else
	ClearTextLine(1, y);
  y += FontHeight;

  if (FindFreeWatch())
	xprintf(0, 0,y,"<F2>/<F8>: Add One/All To Watches");
  else
	ClearTextLine(1, y);
  y += FontHeight;

  if (LoadedCheatTotal < MAX_LOADEDCHEATS)
	xprintf(0, 0,y,"<F1>/<F6>: Add One/All To List");
  else
	ClearTextLine(1, y);
}

int build_mem_list(int Index, struct DisplayText *ext_dt, char ext_str2[MAX_DT + 1][40])
{
	int total = 0;
	while (total < MAX_DISPLAYMEM)
	{
		if (Index + total >= MemoryAreasTotal)
		{
			break;
		}
		AddCheckToMemArea(total + Index, ext_str2[total]);
		ext_dt[total].text = ext_str2[total];
		ext_dt[total].x = (MachWidth - FontWidth * strlen(ext_dt[total].text)) / 2;
		total++;
	}
	ext_dt[total].text = 0; /* terminate array */

	/* Clear old list */
	ClearArea(1, YHEAD_MEMORY, YFOOT_MEMORY-1);

	return total;
}

int SelectMemoryHeader(void)
{
  int y = 0;

  cheat_clearbitmap();

  xprintf(0, 0, y, "<F6>: All on  <F7>: All off");
  y += (FontHeight);
  xprintf(0, 0, y, "<F12>: Display Info on Area");
  y += (FontHeight);
  xprintf(0, 0, y, "<ENTER>: Enable/Disable");
  y += (FontHeight * 2);
  xprintf(0, 0, y, "Select Memory Areas to Scan");
  y += (FontHeight);
  xprintf(0, 0, y, "for CPU %d (%d Total)", SearchCpuNo, MemoryAreasTotal);
  y += (FontHeight + 2);

  return(YHEAD_MEMORY);
}

void SelectMemoryAreas(void)
{
	  int SaveMemoryAreas[MAX_EXT_MEMORY];

	int i, x, y, highlighted, key, done, total;
	struct DisplayText dt[MAX_DT + 1];
	int Index;

	char str2[60][40];

	  char buffer[40];

	for (i = 0; i < MemoryAreasTotal; i++)
		  SaveMemoryAreas[i] = MemToScanTable[i].Enabled;

	cheat_clearbitmap();

	if (MachWidth < FontWidth * 35)
	{
		x = 0;
	}
	else
	{
		x = (MachWidth - (FontWidth * (2 * strlen(FormatAddr(SearchCpuNo,0)) + 7))) / 2;
	}
	y = SelectMemoryHeader();

	/* Make the list */
	for (i = 0; i < MAX_DISPLAYMEM; i++)
	{
		dt[i].x = x;
		dt[i].y = y;
		dt[i].color = DT_COLOR_WHITE;
		y += FontHeight;
	}

	Index = 0;
	highlighted = 0;

	total = build_mem_list(Index, dt, str2);

	y += (FontHeight * 2);

	oldkey = 0;

	done = 0;
	do
	{
		for (i = 0; i < total; i++)
		{
			dt[i].color = (i == highlighted) ? DT_COLOR_YELLOW : DT_COLOR_WHITE;
		}

		displaytext(dt, 0, 1);

		/* key = keyboard_read_sync(); */
		key = cheat_readkey();	  /* MSH 990217 */
		ClearTextLine(1, YFOOT_MEMORY);
		switch (key)
		{
			case KEYCODE_DOWN:
			case KEYCODE_2_PAD:
				if (highlighted < total - 1)
				{
					highlighted++;
				}
				else
				{
					highlighted = 0;

					if (MemoryAreasTotal <= MAX_DISPLAYMEM)
					{
						break;
					}

					if (MemoryAreasTotal > Index + MAX_DISPLAYMEM)
					{
						Index += MAX_DISPLAYMEM;
					}
					else
					{
						Index = 0;
					}
					total = build_mem_list(Index, dt, str2);
				}
				break;

			case KEYCODE_UP:
			case KEYCODE_8_PAD:
				if (highlighted > 0)
				{
					highlighted--;
				}
				else
				{
					highlighted = total - 1;
					if (MemoryAreasTotal <= MAX_DISPLAYMEM)
					{
						break;
					}

					if (Index == 0)
					{
						Index = ((MemoryAreasTotal - 1) / MAX_DISPLAYMEM) * MAX_DISPLAYMEM;
					}
					else if (Index > MAX_DISPLAYMEM)
					{
						Index -= MAX_DISPLAYMEM;
					}
					else
					{
						Index = 0;
					}
					total = build_mem_list(Index, dt, str2);
					highlighted = total - 1;
				}
				break;

			case KEYCODE_HOME:
			case KEYCODE_7_PAD:
				Index = 0;
				total = build_mem_list(Index, dt, str2);
				highlighted = 0;
				break;

			case KEYCODE_END:
			case KEYCODE_1_PAD:
				Index = ((MemoryAreasTotal - 1) / MAX_DISPLAYMEM) * MAX_DISPLAYMEM;
				total = build_mem_list(Index, dt, str2);
				highlighted = total - 1;
				break;

			case KEYCODE_PGDN:
			case KEYCODE_3_PAD:
				if (highlighted + Index >= MemoryAreasTotal - MAX_DISPLAYMEM)
				{
					Index = ((MemoryAreasTotal - 1) / MAX_DISPLAYMEM) * MAX_DISPLAYMEM;
					highlighted = (MemoryAreasTotal - 1) - Index;
				}
				else
				{
					Index += MAX_DISPLAYMEM;
				}
				total = build_mem_list(Index, dt, str2);
				break;

			case KEYCODE_PGUP:
			case KEYCODE_9_PAD:
				if (highlighted + Index <= MAX_DISPLAYMEM)
				{
					Index = 0;
					highlighted = 0;
				}
				else
				{
					Index -= MAX_DISPLAYMEM;
				}
				total = build_mem_list(Index, dt, str2);
				break;

			case KEYCODE_F6:
				while (keyboard_pressed(key));
			  oldkey = 0;
				for (i = 0; i < MemoryAreasTotal; i++)
				MemToScanTable[i].Enabled = 1;
				total = build_mem_list(Index, dt, str2);
				break;

			case KEYCODE_F7:
				while (keyboard_pressed(key));
			  oldkey = 0;
				for (i = 0; i < MemoryAreasTotal; i++)
				MemToScanTable[i].Enabled = 0;
				total = build_mem_list(Index, dt, str2);
				break;

			case KEYCODE_F12:	 /* Display info about a cheat */
				while (keyboard_pressed(key));
			  oldkey = 0;
			strcpy (buffer,str2[Index + highlighted]);
			strcat (buffer," : ");
				switch ((FPTR)MemToScanTable[Index + highlighted].handler)
				{
					case (FPTR)MWA_NOP:
						strcat (buffer,"NOP");
						break;
					case (FPTR)MWA_RAM:
						strcat (buffer,"RAM");
						break;
					case (FPTR)MWA_ROM:
						strcat (buffer,"ROM");
						break;
					case (FPTR)MWA_RAMROM:
						strcat (buffer,"RAMROM");
						break;
					case (FPTR)MWA_BANK1:
						strcat (buffer,"BANK1");
						break;
					case (FPTR)MWA_BANK2:
						strcat (buffer,"BANK2");
						break;
					case (FPTR)MWA_BANK3:
						strcat (buffer,"BANK3");
						break;
					case (FPTR)MWA_BANK4:
						strcat (buffer,"BANK4");
						break;
					case (FPTR)MWA_BANK5:
						strcat (buffer,"BANK5");
						break;
					case (FPTR)MWA_BANK6:
						strcat (buffer,"BANK6");
						break;
					case (FPTR)MWA_BANK7:
						strcat (buffer,"BANK7");
						break;
					case (FPTR)MWA_BANK8:
						strcat (buffer,"BANK8");
						break;
					default:
						strcat (buffer,"user-defined");
						break;
				}
				xprintf(0, 0, YFOOT_MEMORY, "%s", &buffer[2]);
				break;

			case KEYCODE_ENTER:
			case KEYCODE_ENTER_PAD:
			case KEYCODE_SPACE:
				while (keyboard_pressed(key));
			  oldkey = 0;
			MemToScanTable[Index + highlighted].Enabled ^= 1;
				total = build_mem_list(Index, dt, str2);
				break;

			case KEYCODE_ESC:
			case KEYCODE_TAB:
				while (keyboard_pressed(key));
				done = 1;
				break;
		}
	}
	while (done == 0);

	while (keyboard_pressed(key)) ; /* wait for key release */

	/* clear the screen before returning */
	cheat_clearbitmap();

	  MemoryAreasSelected = 0;
	for (i = 0; i < MemoryAreasTotal; i++)
	{
		  if (SaveMemoryAreas[i] != MemToScanTable[i].Enabled)
		{
			RebuildTables = 1;
		}
		  if (MemToScanTable[i].Enabled)
		{
			MemoryAreasSelected = 1;
		}
	}

	if (!MemoryAreasSelected)
	{
		y = (MachHeight - 8 * FontHeight) / 2;
		xprintf(0, 0,y,"WARNING !");
		y += 2*FontHeight;
		xprintf(0, 0,y,"No Memory Area Selected !");
		y += 4*FontHeight;
		xprintf(0, 0,y,"Press A Key To Continue...");
		key = keyboard_read_sync();
		while (keyboard_pressed(key)) ; /* wait for key release */
		cheat_clearbitmap();
	}

}

int SelectFastSearchHeader(void)
{
  int y = FIRSTPOS;

  cheat_clearbitmap();

  xprintf(0, 0, y, "<F10>: Show Help");
  y += 2*FontHeight;
  xprintf(0, 0,y,"Choose One Of The Following:");
  y += 2*FontHeight;
  return(y);
}

void SelectFastSearch(void)
{
  int y;
  int s,key,done;
  int total;

  char *paDisplayText[] = {
		"Scan All Memory (Slow But Sure)",
		"Scan all RAM and BANKS (Normal)",
		"Scan one BANK (Fastest Search)",
		"Select Memory Areas (Manual Search)",
		"",
		"Return To Start Search Menu",
		0 };

  struct DisplayText dt[10];

  y = SelectFastSearchHeader();

  total = create_menu(paDisplayText, dt, y);

  oldkey = 0;

  s = fastsearch;
  done = 0;
  do
  {
	key = SelectMenu (&s, dt, 0, 0, 0, total-1, 0, &done);
	switch (key)
	{
		case KEYCODE_F10:
			SelectFastSearchHelp();
			done = 0;
			StartSearchHeader();
			break;

		case KEYCODE_ENTER:
		case KEYCODE_ENTER_PAD:
			switch (s)
			{
				case 0:
					if (fastsearch != 0)
					{
						SearchCpuNoOld = -1;	/* Force tables to be built */
						InitMemoryAreas();
					}
					fastsearch = 0;
					done = 1;
					break;

				case 1:
					if (fastsearch != 1)
					{
						SearchCpuNoOld = -1;	/* Force tables to be built */
						InitMemoryAreas();
					}
					fastsearch = 1;
					done = 1;
					break;

				case 2:
					if (fastsearch != 2)
					{
						SearchCpuNoOld = -1;	/* Force tables to be built */
						InitMemoryAreas();
					}
					fastsearch = 2;
					done = 1;
					break;

				case 3:
					fastsearch = 3;
				  SelectMemoryAreas();
					done = 1;
					break;

				case 5:
					done = 2;
					break;
			}
			break;
	}
  } while ((done != 1) && (done != 2));

  cheat_clearbitmap();
}

int SelectSearchValue(void)
{
  int y;
  int s,key,done;
  int total;

  struct DisplayText dt[10];

  char *paDisplayText[] =
  {
	"Unknown starting value",
	"Select starting value",
	0
  };

  y = SelectSearchValueHeader();
  total = create_menu(paDisplayText, dt, y);
  y = dt[total-1].y + ( 3 * FontHeight );

  oldkey = 0;

  s = 0;
  done = 0;
  do
  {
	key = SelectMenu (&s, dt, 0, 0, 0, total-1, 0, &done);
  } while ((done != 1) && (done != 2));

  if (done == 2)
  {
	return(done);
  }
  else
  {
	return(s);
  }
}

/*****************
 * Start a cheat search
 * If the method 1 is selected, ask the user a number
 * In all cases, backup the ram.
 *
 * Ask the user to select one of the following:
 *	1 - Lives or other number (byte) (exact)	   ask a start value , ask new value
 *	2 - Timers (byte) (+ or - X)		   nothing at start, ask +-X
 *	3 - Energy (byte) (less, equal or greater)	   nothing at start, ask less, equal or greater
 *	4 - Status (bit)  (true or false)		   nothing at start, ask same or opposite
 *	5 - Slow but sure (Same as start or different) nothing at start, ask same or different
 *
 * Another method is used in the Pro action Replay the Energy method
 *	you can tell that the value is now 25%/50%/75%/100% as the start
 *	the problem is that I probably cannot search for exactly 50%, so
 *	that do I do? search +/- 10% ?
 * If you think of other way to search for codes, let me know.
 */

void StartSearch(void)
{
  int i = 0;
  int y;
  int s,key,done,count;
  int total;

  int StartValueNeeded = 0;

  char *paDisplayText[] = {
		"Lives, Or Some Other Value",
		"Timers (+/- Some Value)",
		"Energy (Greater Or Less)",
		"Status (A Bit Or Flag)",
		"Slow But Sure (Changed Or Not)",
		"",
		"Change Search Speed",
		"",
		"Return To Cheat Menu",
		0 };

  struct DisplayText dt[10];

  y = StartSearchHeader();

  total = create_menu(paDisplayText, dt, y);
  y = dt[total-1].y + ( 3 * FontHeight );

  count = 1;

  oldkey = 0;

  s = SaveStartSearch;
  done = 0;
  do
  {
	key = SelectMenu (&s, dt, 0, 0, 0, total-1, 0, &done);
	switch (key)
	{
		case KEYCODE_F10:
			StartSearchHelp();								/* Show Help */
			done = 0;
			StartSearchHeader();
			break;

		case KEYCODE_ENTER:
		case KEYCODE_ENTER_PAD:
			switch (s)
			{
				case 0:
					SaveMethod = CurrentMethod;
					CurrentMethod = SEARCH_VALUE;
				  StartValueNeeded = 1;
					done = 1;
					break;

				case 1:
					SaveMethod = CurrentMethod;
					CurrentMethod = SEARCH_TIME;
				  StartValueNeeded = SelectSearchValue();
				  if (StartValueNeeded == 2)
				  {
						done = 0;
				  }
				  else
				  {
						done = 1;
				  }
					break;

				case 2:
					SaveMethod = CurrentMethod;
					CurrentMethod = SEARCH_ENERGY;
				  StartValueNeeded = SelectSearchValue();
				  if (StartValueNeeded == 2)
				  {
						done = 0;
				  }
				  else
				  {
						done = 1;
				  }
					break;

				case 3:
					SaveMethod = CurrentMethod;
					CurrentMethod = SEARCH_BIT;
				  StartValueNeeded = SelectSearchValue();
				  if (StartValueNeeded == 2)
				  {
						done = 0;
				  }
				  else
				  {
						done = 1;
				  }
					break;

				case 4:
					SaveMethod = CurrentMethod;
					CurrentMethod = SEARCH_BYTE;
				  StartValueNeeded = SelectSearchValue();
				  if (StartValueNeeded == 2)
				  {
						done = 0;
				  }
				  else
				  {
						done = 1;
				  }
					break;

				case 6:
					SelectFastSearch();
					done = 0;
					StartSearchHeader();
					break;

				case 8:
					done = 2;
				  s = 0;
					break;
			}
			break;
	}
  } while ((done != 1) && (done != 2));

  SaveStartSearch = s;

  cheat_clearbitmap();

  /* User select to return to the previous menu */
  if (done == 2)
	return;

  if (ManyCpus)
  {
	ValTmp = SelectValue(SearchCpuNo, 0, 1, 1, 0, 0, cpu_gettotalcpu()-1,
					"%01X", "Enter CPU To Search In:", 1,
				  FIRSTPOS + 3 * FontHeight);

	cheat_clearbitmap();

	if (ValTmp == NOVALUE)
	{
		CurrentMethod = SaveMethod;
		return;
	}

	if (ValTmp == 2 * NOVALUE)
	{
		CurrentMethod = SaveMethod;
		StartSearch();
		return;
	}

	s = ValTmp;
  }
  else
  {
	s = 0;
  }
  SearchCpuNo = s;

  cheat_clearbitmap();

  /* User select to return to the previous menu */
  if (done == 2)
	return;

  SaveContinueSearch = 0;

  if (SearchCpuNoOld != SearchCpuNo)
  {
	RebuildTables = 1;
	  if (SearchCpuNoOld != -1)
	  {
		InitMemoryAreas();
	  }
  }
  if ((fastsearch == 3) && (!MemoryAreasSelected))
	SelectMemoryAreas();
  if (RebuildTables)
  {
	if (!build_tables())
		SearchCpuNoOld = SearchCpuNo;
	  else
	{
		CurrentMethod = 0;
		return;
	}
  }
  RebuildTables = 0;

  /* If the method 1 is selected, ask for a number */
  /* if (CurrentMethod == SEARCH_VALUE */
  if (StartValueNeeded)
  {
	ValTmp = SelectValue(0, 0, 1, 1, 1, 0, 0xFF,
					"%03d  (0x%02X)", "Enter Value To Search For:", 1, FIRSTPOS + 3 * FontHeight);

	cheat_clearbitmap();

	if (ValTmp == NOVALUE)
	{
		CurrentMethod = SaveMethod;
		return;
	}

	if (ValTmp == 2 * NOVALUE)
	{
		CurrentMethod = SaveMethod;
		StartSearch();
		return;
	}

	s = ValTmp;
  }
  else
  {
	s = 0;
  }
  StartValue = s;

  cheat_clearbitmap();

  y = (MachHeight - FontHeight) / 2 - FontHeight;
  SearchInProgress(1,y);

  /* Backup the ram */
  backup_ram (StartRam);
  backup_ram (BackupRam);
  memset_ram (FlagTable, 0xFF); /* At start, all location are good */

  /* Flag the location that match the initial value if method 1 */
  /* if (CurrentMethod == SEARCH_VALUE */
  if (StartValueNeeded)
  {
	struct ExtMemory *ext;
	  count = 0;
	for (ext = FlagTable; ext->data; ext++)
	{
		for (i=0; i <= ext->end - ext->start; i++)
			if (ext->data[i] != 0)
		  {
				if (	(RD_GAMERAM(SearchCpuNo, i+ext->start) != s)			&&
					(	(RD_GAMERAM(SearchCpuNo, i+ext->start) != s-1)		||
						(CurrentMethod != SEARCH_VALUE) 		)	)
					ext->data[i] = 0;
			else
					count ++;
		  }
	}

	SearchInProgress(0,y);
  }
  else
  {
	struct ExtMemory *ext;
	  count = 0;
	for (ext = FlagTable; ext->data; ext++)
		count += ext->end - ext->start + 1;

	SearchInProgress(0,y);
	iCheatInitialized = 1;
  }

  /* Copy the tables */
  copy_ram (OldBackupRam, BackupRam);
  copy_ram (OldFlagTable, FlagTable);

  if (count == 0)
  {
	SaveMethod = CurrentMethod;
	CurrentMethod = 0;
  }

  MatchFound = count;
  OldMatchFound = MatchFound;

  RestoreStatus = RESTORE_NOSAVE;

  y -= 2 * FontHeight;
  if (count == 0)
  {
	xprintf(0, 0,y,"No Matches Found");
  }
  else
  {
	xprintf(0, 0,y,"Search Initialized.");
	y += FontHeight;
	xprintf(0, 0,y,"(Matches Found: %d)",count);
  }

  y += 4 * FontHeight;
  xprintf(0, 0,y,"Press A Key To Continue...");
  key = keyboard_read_sync();
  while (keyboard_pressed(key)) ; /* wait for key release */
  cheat_clearbitmap();
}

void ContinueSearch(int selected, int ViewLast)
{
  char str2[MAX_DT + 1][40];

  int i,j,y,count,s,key,done;

  struct DisplayText dt[MAX_DT + 1];

  int total;
  int Continue;

  struct ExtMemory *ext;
  struct ExtMemory *ext_br;
  struct ExtMemory *ext_sr;

  int Index = 0;
  int countAdded;

  char fmt[40];
  char buf[60];
  char *ptr;

  int TrueAddr, TrueData;

  if (!selected)
  {
	cheat_save_frameskips();
  }

  cheat_clearbitmap();

  oldkey = 0;

  if (!ViewLast)
  {
	if (CurrentMethod == 0)
	{
		StartSearch();
		/* JCK 990529 BEGIN */
		if (!selected)
		{
			cheat_rest_frameskips();
		}
		/* JCK 990529 END */
		return;
	}

	count = 0;
	y = ContinueSearchHeader();

	/******** Method 1 ***********/
	/* Ask new value if method 1 */
	if (CurrentMethod == SEARCH_VALUE)
	{
		ValTmp = SelectValue(StartValue, 0, 1, 1, 1, 0, 0xFF, "%03d  (0x%02X)", "", 0, y);

		cheat_clearbitmap();

		if (ValTmp == NOVALUE)
		{
			/* JCK 990529 BEGIN */
			if (!selected)
			{
				cheat_rest_frameskips();
			}
			/* JCK 990529 END */
			return;
		}

		if (ValTmp == 2 * NOVALUE)
		{
			CurrentMethod = SaveMethod;
			StartSearch();
			/* JCK 990529 BEGIN */
			if (!selected)
			{
				cheat_rest_frameskips();
			}
			/* JCK 990529 END */
			return;
		}

		s = ValTmp;

		StartValue = s; /* Save the value for when continue */
	}


	/******** Method 2 ***********/
	/* Ask new value if method 2 */
	if (CurrentMethod == SEARCH_TIME)
	{
		ValTmp = SelectValue(StartValue, 0, 1, 0, 0, -127, 128, "%+04d", "", 0, y);

		cheat_clearbitmap();

		if (ValTmp == NOVALUE)
		{
			/* JCK 990529 BEGIN */
			if (!selected)
			{
				cheat_rest_frameskips();
			}
			/* JCK 990529 END */
			return;
		}

		if (ValTmp == 2 * NOVALUE)
		{
			CurrentMethod = SaveMethod;
			StartSearch();
			/* JCK 990529 BEGIN */
			if (!selected)
			{
				cheat_rest_frameskips();
			}
			/* JCK 990529 END */
			return;
		}

		s = ValTmp;

		iCheatInitialized = 0;

		StartValue = s; /* Save the value for when continue */
	}


	/******** Method 3 ***********/
	if (CurrentMethod == SEARCH_ENERGY)
	{
		char *paDisplayText[] =
		  {
			"New Value is Less",
			"New Value is Equal",
			"New Value is Greater",
			"",
			"Return To Cheat Menu",
			0
		  };

		total = create_menu(paDisplayText, dt, y);
		y = dt[total-1].y + ( 3 * FontHeight );

		s = SaveContinueSearch;
		done = 0;
		do
		{
			key = SelectMenu (&s, dt, 0, 0, 0, total-1, 0, &done);
			switch (key)
			{
				case KEYCODE_F1:
					StartSearch();
					/* JCK 990529 BEGIN */
					if (!selected)
					{
						cheat_rest_frameskips();
					}
					/* JCK 990529 END */
					return;
					break;

				case KEYCODE_ENTER:
				case KEYCODE_ENTER_PAD:
					if (s == total-1)
						done = 2;
					break;
			}
		} while ((done != 1) && (done != 2));

		cheat_clearbitmap();

		SaveContinueSearch = s;

		/* User select to return to the previous menu */
		if (done == 2)
		{
			/* JCK 990529 BEGIN */
			if (!selected)
			{
				cheat_rest_frameskips();
			}
			/* JCK 990529 END */
			return;
		}

		iCheatInitialized = 0;
	}


	/******** Method 4 ***********/
	/* Ask if the value is the same as when we start or the opposite */
	if (CurrentMethod == SEARCH_BIT)
	{
		char *paDisplayText[] =
		  {
			"Bit is Same as Start",
			"Bit is Opposite from Start",
			"",
			"Return To Cheat Menu",
			0
		  };

		total = create_menu(paDisplayText, dt, y);
		y = dt[total-1].y + ( 3 * FontHeight );

		s = SaveContinueSearch;
		done = 0;
		do
		{
			key = SelectMenu (&s, dt, 0, 0, 0, total-1, 0, &done);
			switch (key)
			{
				case KEYCODE_F1:
					StartSearch();
					/* JCK 990529 BEGIN */
					if (!selected)
					{
						cheat_rest_frameskips();
					}
					/* JCK 990529 END */
					return;
					break;

				case KEYCODE_ENTER:
				case KEYCODE_ENTER_PAD:
					if (s == total-1)
						done = 2;
					break;
			}
		} while ((done != 1) && (done != 2));

		SaveContinueSearch = s;

		cheat_clearbitmap();

		/* User select to return to the previous menu */
		if (done == 2)
		{
			/* JCK 990529 BEGIN */
			if (!selected)
			{
				cheat_rest_frameskips();
			}
			/* JCK 990529 END */
			return;
		}

		iCheatInitialized = 0;
	}


	/******** Method 5 ***********/
	/* Ask if the value is the same as when we start or different */
	if (CurrentMethod == SEARCH_BYTE)
	{
		char *paDisplayText[] =
		  {
			"Memory is Same as Start",
			"Memory is Different from Start",
			"",
			"Return To Cheat Menu",
			0
		  };

		total = create_menu(paDisplayText, dt, y);
		y = dt[total-1].y + ( 3 * FontHeight );

		s = SaveContinueSearch;
		done = 0;
		do
		{
			key = SelectMenu (&s, dt, 0, 0, 0, total-1, 0, &done);
			switch (key)
			{
				case KEYCODE_F1:
					StartSearch();
					/* JCK 990529 BEGIN */
					if (!selected)
					{
						cheat_rest_frameskips();
					}
					/* JCK 990529 END */
					return;
					break;

				case KEYCODE_ENTER:
				case KEYCODE_ENTER_PAD:
					if (s == total-1)
						done = 2;
					break;
			}
		} while ((done != 1) && (done != 2));

		cheat_clearbitmap();

		SaveContinueSearch = s;

		/* User select to return to the previous menu */
		if (done == 2)
		{
			/* JCK 990529 BEGIN */
			if (!selected)
			{
				cheat_rest_frameskips();
			}
			/* JCK 990529 END */
			return;
		}

		iCheatInitialized = 0;
	}

	y = FIRSTPOS + (10 * FontHeight);
	SearchInProgress(1,y);

	/* Copy the tables */
	copy_ram (OldBackupRam, BackupRam);
	copy_ram (OldFlagTable, FlagTable);
	OldMatchFound = MatchFound;

	RestoreStatus = RESTORE_OK;

	count = 0;
	for (ext = FlagTable, ext_sr = StartRam, ext_br = BackupRam; ext->data; ext++, ext_sr++, ext_br++)
	{
		for (i=0; i <= ext->end - ext->start; i++)
		  {
			if (ext->data[i] != 0)
			{
			int ValRead = RD_GAMERAM(SearchCpuNo, i+ext->start);
				switch (CurrentMethod)
				{
					case SEARCH_VALUE:			   /* Value */
						if ((ValRead != s) && (ValRead != s-1))
							ext->data[i] = 0;
						break;
					case SEARCH_TIME:			   /* Timer */
						if (ValRead != (ext_br->data[i] + s))
							ext->data[i] = 0;
						break;
					case SEARCH_ENERGY: 		   /* Energy */
					switch (s)
					{
						case 0:    /* Less */
								if (ValRead >= ext_br->data[i])
									ext->data[i] = 0;
						break;
						case 1:    /* Equal */
								if (ValRead != ext_br->data[i])
									ext->data[i] = 0;
						break;
						case 2:    /* Greater */
								if (ValRead <= ext_br->data[i])
									ext->data[i] = 0;
						break;
					}
						break;
					case SEARCH_BIT:			   /* Bit */
					switch (s)
					{
						case 0:    /* Same */
								j = ValRead ^ (ext_sr->data[i] ^ 0xFF);
								ext->data[i] = j & ext->data[i];
						break;
						case 1:    /* Opposite */
								j = ValRead ^ ext_sr->data[i];
								ext->data[i] = j & ext->data[i];
						break;
					}
						break;
					case SEARCH_BYTE:			   /* Byte */
					switch (s)
					{
						case 0:    /* Same */
								if (ValRead != ext_sr->data[i])
									ext->data[i] = 0;
						break;
						case 1:    /* Different */
								if (ValRead == ext_sr->data[i])
									ext->data[i] = 0;
						break;
					}
						break;
					default:
						ext->data[i] = 0;
						break;
				}
			}
			if (ext->data[i] != 0)
			count ++;
		  }
	}
	if ((CurrentMethod == SEARCH_TIME) || (CurrentMethod == SEARCH_ENERGY))
		backup_ram (BackupRam);
	SearchInProgress(0,y);
  }
  else
  {
  count = MatchFound;
  }

  /* For all methods:
	- Display how much locations we have found
	- Display them
	- The user can press F2 to add one to the watches
	- The user can press F1 to add one to the cheat list
	- The user can press F6 to add all to the cheat list */

  cheat_clearbitmap();

  countAdded = 0;

  MatchFound = count;

  y = ContinueSearchMatchHeader(count);

  if (count == 0)
  {
	y += 4*FontHeight;
	xprintf(0, 0,y,"Press A Key To Continue...");
	key = keyboard_read_sync();
	while (keyboard_pressed(key)) ; /* wait for key release */
	cheat_clearbitmap();
	SaveMethod = CurrentMethod;
	CurrentMethod = 0;
	/* JCK 990529 BEGIN */
	if (!selected)
	{
		cheat_rest_frameskips();
	}
	/* JCK 990529 END */
	return;
  }

  y += 2*FontHeight;

  total = 0;
  Continue = 0;

  for (ext = FlagTable, ext_sr = StartRam; ext->data && Continue==0; ext++, ext_sr++)
  {
	for (i=0; i <= ext->end - ext->start; i++)
		if (ext->data[i] != 0)
		{
			strcpy(fmt, FormatAddr(SearchCpuNo,0));
			strcat(fmt," = %02X");

			TrueAddr = i+ext->start;
			TrueData = ext_sr->data[i];
			sprintf (str2[total], fmt, TrueAddr, TrueData);

			dt[total].text = str2[total];
			dt[total].x = (MachWidth - FontWidth * strlen(dt[total].text)) / 2;
			dt[total].y = y;
			dt[total].color = DT_COLOR_WHITE;
			total++;
			y += FontHeight;
			if (total >= MAX_MATCHES)
			{
				Continue = i+ext->start;
				break;
			}
		}
  }

  dt[total].text = 0; /* terminate array */

  ContinueSearchMatchHeader(count);

  oldkey = 0;

  s = 0;
  done = 0;
  do
  {
	int Begin = 0;

	ContinueSearchMatchFooter(count, Index);

	key = SelectMenu (&s, dt, 1, 0, 0, total-1, 0, &done);

	ClearTextLine(1, YFOOT_MATCH);

	switch (key)
	{
		case KEYCODE_HOME:
		case KEYCODE_7_PAD:
			if (count == 0)
				break;
			if (count <= MAX_MATCHES)
				break;
			if (Index == 0)
				break;

			cheat_clearbitmap();

			ContinueSearchMatchHeader(count);

			s = 0;
			Index = 0;

			total = 0;

			Continue = 0;
			for (ext = FlagTable, ext_sr = StartRam; ext->data && Continue==0; ext++, ext_sr++)
			{
				for (i=0; i <= ext->end - ext->start; i++)
					if ((ext->data[i] != 0) && (total < MAX_MATCHES))
					{
						strcpy(fmt, FormatAddr(SearchCpuNo,0));
						strcat(fmt," = %02X");

						TrueAddr = i+ext->start;
						TrueData = ext_sr->data[i];
						sprintf (str2[total], fmt, TrueAddr, TrueData);

						dt[total].text = str2[total];
						dt[total].x = (MachWidth - FontWidth * strlen(dt[total].text)) / 2;
						total++;
						if (total >= MAX_MATCHES)
						{
							Continue = i+ext->start;
							break;
						}
					}
			}

			dt[total].text = 0; /* terminate array */
			break;

		case KEYCODE_PGDN:
		case KEYCODE_3_PAD:
			if (count == 0)
				break;
			if (count <= Index+MAX_MATCHES)
				break;

			cheat_clearbitmap();
			ContinueSearchMatchHeader(count);

			s = 0;
			Index += MAX_MATCHES;

			total = 0;

			Begin = Continue+1;
			Continue = 0;

			for (ext = FlagTable, ext_sr = StartRam; ext->data && Continue==0; ext++, ext_sr++)
			{
				if (ext->start <= Begin && ext->end >= Begin)
				{
					for (i = Begin - ext->start; i <= ext->end - ext->start; i++)
						if ((ext->data[i] != 0) && (total < MAX_MATCHES))
						{
							strcpy(fmt, FormatAddr(SearchCpuNo,0));
							strcat(fmt," = %02X");

							TrueAddr = i+ext->start;
							TrueData = ext_sr->data[i];
							sprintf (str2[total], fmt, TrueAddr, TrueData);

							dt[total].text = str2[total];
							dt[total].x = (MachWidth - FontWidth * strlen(dt[total].text)) / 2;
							total++;
							if (total >= MAX_MATCHES)
							{
								Continue = i+ext->start;
								break;
							}
						}
				}
				else if (ext->start > Begin)
				{
					for (i=0; i <= ext->end - ext->start; i++)
						if ((ext->data[i] != 0) && (total < MAX_MATCHES))
						{
							strcpy(fmt, FormatAddr(SearchCpuNo,0));
							strcat(fmt," = %02X");

							TrueAddr = i+ext->start;
							TrueData = ext_sr->data[i];
							sprintf (str2[total], fmt, TrueAddr, TrueData);

							dt[total].text = str2[total];
							dt[total].x = (MachWidth - FontWidth * strlen(dt[total].text)) / 2;
							total++;
							if (total >= MAX_MATCHES)
							{
								Continue = i+ext->start;
								break;
							}
						}
				}
			}

			dt[total].text = 0; /* terminate array */
			break;

		case KEYCODE_F1:
			while (keyboard_pressed(key));
		  oldkey = 0;

			if (count == 0)
				break;

			if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
			{
				break;
			}

			strcpy(buf, dt[s].text);
			ptr = strtok(buf, "=");
			sscanf(ptr,"%X", &TrueAddr);
			ptr = strtok(NULL, "=");
			sscanf(ptr,"%02X", &TrueData);

		  AddCpuToAddr(SearchCpuNo, TrueAddr, TrueData, str2[MAX_DT]);

			/* Add the selected address to the LoadedCheatTable */
			if (LoadedCheatTotal < MAX_LOADEDCHEATS)
			{
				set_cheat(&LoadedCheatTable[LoadedCheatTotal], NEW_CHEAT);
				LoadedCheatTable[LoadedCheatTotal].CpuNo   = SearchCpuNo;
				LoadedCheatTable[LoadedCheatTotal].Address = TrueAddr;
				LoadedCheatTable[LoadedCheatTotal].Data    = TrueData;
				strcpy(LoadedCheatTable[LoadedCheatTotal].Name, str2[MAX_DT]);
				LoadedCheatTotal++;
				xprintf(0, 0,YFOOT_MATCH,"%s Added to List",str2[MAX_DT]);
			}
			else
				xprintf(0, 0,YFOOT_MATCH,"%s Not Added to List",str2[MAX_DT]);

			break;

		case KEYCODE_F2:
			while (keyboard_pressed(key));
		  oldkey = 0;

			if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
			{
				break;
			}

		  j = FindFreeWatch();
			if (j)
			{
				strcpy(buf, dt[s].text);
				ptr = strtok(buf, "=");
				sscanf(ptr,"%X", &TrueAddr);

				WatchesCpuNo[j-1] = SearchCpuNo;
				Watches[j-1] = TrueAddr;
				WatchesFlag = 1;
				WatchEnabled = 1;
				strcpy(fmt, FormatAddr(SearchCpuNo,0));
				sprintf (str2[MAX_DT], fmt, Watches[j-1]);
				xprintf(0, 0,YFOOT_MATCH,"%s Added as Watch %d",str2[MAX_DT],j);
			}

			break;

		case KEYCODE_F6:
			while (keyboard_pressed(key));
		  oldkey = 0;

			if (count == 0)
				break;

			if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
			{
				break;
			}

			countAdded = 0;

			for (ext = FlagTable, ext_sr = StartRam; ext->data; ext++, ext_sr++)
			{
				for (i = 0; i <= ext->end - ext->start; i++)
				{
					if (LoadedCheatTotal > MAX_LOADEDCHEATS-1)
						break;
					if (ext->data[i] != 0)
					{
						countAdded++;

						TrueAddr = i+ext->start;
						TrueData = ext_sr->data[i];

					AddCpuToAddr(SearchCpuNo, TrueAddr, TrueData, str2[MAX_DT]);

						set_cheat(&LoadedCheatTable[LoadedCheatTotal], NEW_CHEAT);
						LoadedCheatTable[LoadedCheatTotal].CpuNo   = SearchCpuNo;
						LoadedCheatTable[LoadedCheatTotal].Address = TrueAddr;
						LoadedCheatTable[LoadedCheatTotal].Data    = TrueData;
						strcpy(LoadedCheatTable[LoadedCheatTotal].Name,str2[MAX_DT]);
						LoadedCheatTotal++;
					}
				}
				if (LoadedCheatTotal > MAX_LOADEDCHEATS)
					break;
			}

			xprintf(0, 0,YFOOT_MATCH,"%d Added to List",countAdded);

			break;

		case KEYCODE_F8:
			while (keyboard_pressed(key));
		  oldkey = 0;

			if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
			{
				break;
			}

			countAdded = 0;

			for (ext = FlagTable, ext_sr = StartRam; ext->data; ext++, ext_sr++)
			{
				for (i = 0; i <= ext->end - ext->start; i++)
				{
				  j = FindFreeWatch();
					if (!j)
						break;
					if (ext->data[i] != 0)
					{
						countAdded++;

						TrueAddr = i+ext->start;

						WatchesCpuNo[j-1] = SearchCpuNo;
						Watches[j-1] = TrueAddr;
						WatchesFlag = 1;
						WatchEnabled = 1;
						strcpy(fmt, FormatAddr(SearchCpuNo,0));
						sprintf (str2[MAX_DT], fmt, Watches[j-1]);
					}
				}
			}

			xprintf(0, 0,YFOOT_MATCH,"%d Added as Watches",countAdded);

			break;

	}
  } while (done != 2);

  cheat_clearbitmap();

  if (!selected)
  {
	cheat_rest_frameskips();
  }

}

void RestoreSearch(void)
{
  int key;
  int y = (MachHeight - 8 * FontHeight) / 2;

  char msg[40];
  char msg2[40];

  switch (RestoreStatus)
  {
	case RESTORE_NOINIT:
		strcpy(msg, "Search not initialised");
		break;
	case RESTORE_NOSAVE:
		strcpy(msg, "No Previous Values Saved");
		break;
	case RESTORE_DONE:
		strcpy(msg, "Previous Values Already Restored");
		break;
	case RESTORE_OK:
		strcpy(msg, "Previous Values Correctly Restored");
		break;
  }

  if (RestoreStatus == RESTORE_OK)
  {
	/* Restore the tables */
	copy_ram (BackupRam, OldBackupRam);
	copy_ram (FlagTable, OldFlagTable);
	MatchFound = OldMatchFound;
	CurrentMethod = SaveMethod;
	  RestoreStatus = RESTORE_DONE;
	strcpy(msg2, "Restoration Successful :)");
  }
  else
  {
	strcpy(msg2, "Restoration Unsuccessful :(");
  }

  cheat_clearbitmap();
  xprintf(0, 0,y,"%s",msg);
  y += 2 * FontHeight;
  xprintf(0, 0,y,"%s",msg2);
  y += 4 * FontHeight;
  xprintf(0, 0,y,"Press A Key To Continue...");
  key = keyboard_read_sync();
  while (keyboard_pressed(key)) ; /* wait for key release */
  cheat_clearbitmap();
}

int ChooseWatchHeader(void)
{
  int i = 0;
  char *paDisplayText[] = {
		"<+>: +1 byte    <->: -1 byte",
		"<1> ... <8>: +1 digit",
		"<9>: Prev CPU  <0>: Next CPU",
		"<Delete>: disable a watch",
		"<Enter>: copy previous watch",
		"<I><J><K><L>: move watch pos.",
		"(All \"F\"s = watch disabled)",
		"",
		"<F10>: Show Help + other keys",
		0 };

  struct DisplayText dt[20];

  while (paDisplayText[i])
  {
	if(i)
		dt[i].y = (dt[i - 1].y + FontHeight + 2);
	else
		dt[i].y = FIRSTPOS;
	dt[i].color = DT_COLOR_WHITE;
	dt[i].text = paDisplayText[i];
	dt[i].x = (MachWidth - FontWidth * strlen(dt[i].text)) / 2;
	if(dt[i].x > MachWidth)
		dt[i].x = 0;
	i++;
  }
  dt[i].text = 0; /* terminate array */
  displaytext(dt,0,1);
  return(dt[i-1].y + ( 3 * FontHeight ));
}

void ChooseWatchFooter(void)
{
  int y = YFOOT_WATCH;

  y += FontHeight;
  if (LoadedCheatTotal > MAX_LOADEDCHEATS-1)
	xprintf(0, 0,y,"(Cheat List Is Full.)");
}

void ChooseWatch(void)
{
  int i,s,y,key,done;
  int total;
  int savey;
  struct DisplayText dt[MAX_WATCHES+1];
  char str2[MAX_WATCHES+1][15];
  char buf[80];
  char buffer[10];
  int countAdded;
  int OldCpuNo = 0;

  /* JCK 990717 BEGIN */
  int dx = 0;
  int dy = 0;
  /* JCK 990717 END */

  int trueorientation;
  /* hack: force the display into standard orientation to avoid */
  /* rotating the user interface */
  trueorientation = Machine->orientation;
  Machine->orientation = Machine->ui_orientation;

  cheat_clearbitmap();

  y = ChooseWatchHeader();
  savey = y;

  total = 0;

  for (i=0;i<MAX_WATCHES;i++)
  {
	AddCpuToWatch(i, str2[ i ]);

	dt[total].text = str2[ i ];

	  if (i < MAX_WATCHES / 2)
	  {
		dt[total].x = ((MachWidth / 2) - FontWidth * strlen(dt[total].text)) / 2;
	  }
	  else
	  {
		dt[total].x = ((MachWidth / 2) - FontWidth * strlen(dt[total].text)) / 2 + (MachWidth / 2);
	  }

	dt[total].y = y;
	dt[total].color = DT_COLOR_WHITE;
	total++;
	y += FontHeight;
	  if (i == (MAX_WATCHES / 2 - 1))
		y = savey;
  }
  dt[total].text = 0; /* terminate array */

  oldkey = 0;

  s = 0;
  done = 0;
  do
  {
	DisplayWatches(1, &WatchX, &WatchY, buf, s, dx, dy);
	ChooseWatchFooter();
	countAdded = 0;
	key = SelectMenu (&s, dt, 1, 0, 0, total-1, 0, &done);
	ClearTextLine(1, YFOOT_WATCH);
	switch (key)
	{
		case KEYCODE_J:
		  oldkey = 0;
			if (WatchX > Machine->uixmin)
			{
				dx = 1;
				dy = 0;
				WatchX--;
			}
			break;

		case KEYCODE_L:
		  oldkey = 0;
			/* if (WatchX <= ( MachWidth - ( FontWidth * (int)strlen( buf ) ) ) ) */
			if (WatchX <= ( MachWidth -WatchGfxLen ) )
			{
				dx = -1;
				dy = 0;
				WatchX++;
			}
			break;

		case KEYCODE_K:
		  oldkey = 0;
			if (WatchY <= (MachHeight - FontHeight) - 1)
			{
				dx = 0;
				dy = -1;
				WatchY++;
			}
			break;

		case KEYCODE_I:
		  oldkey = 0;
			if (WatchY > Machine->uiymin)
			{
				dx = 0;
				dy = 1;
				WatchY--;
			}
			break;

		case KEYCODE_LEFT:
		case KEYCODE_4_PAD:
			/* JCK 990717 BEGIN */
			dx = 0;
			dy = 0;
			/* JCK 990717 END */
			if (Watches[ s ] <= 0)
				Watches[ s ] = MAX_ADDRESS(WatchesCpuNo[ s ]);
			else
				Watches[ s ]--;
			AddCpuToWatch(s, str2[ s ]);
			break;

		case KEYCODE_RIGHT:
		case KEYCODE_6_PAD:
			/* JCK 990717 BEGIN */
			dx = 0;
			dy = 0;
			/* JCK 990717 END */
			if (Watches[ s ] >= MAX_ADDRESS(WatchesCpuNo[ s ]))
				Watches[ s ] = 0;
			else
				Watches[ s ]++;
			AddCpuToWatch(s, str2[ s ]);
			break;

		case KEYCODE_PGDN:
		case KEYCODE_3_PAD:
			/* JCK 990717 BEGIN */
			dx = 0;
			dy = 0;
			/* JCK 990717 END */
			if (Watches[ s ] <= 0x100)
				Watches[ s ] |= (0xFFFFFF00 & MAX_ADDRESS(WatchesCpuNo[ s ]));
			else
				Watches[ s ] -= 0x100;
			AddCpuToWatch(s, str2[ s ]);
			break;

		case KEYCODE_PGUP:
		case KEYCODE_9_PAD:
			/* JCK 990717 BEGIN */
			dx = 0;
			dy = 0;
			/* JCK 990717 END */
			if (Watches[ s ] >= 0xFF00)
				Watches[ s ] |= (0xFFFF00FF & MAX_ADDRESS(WatchesCpuNo[ s ]));
			else
				Watches[ s ] += 0x100;
			AddCpuToWatch(s, str2[ s ]);
			break;

		case KEYCODE_8:
			/* JCK 990717 BEGIN */
			dx = 0;
			dy = 0;
			/* JCK 990717 END */
			if (ADDRESS_BITS(WatchesCpuNo[s]) < 29) break;
		case KEYCODE_7:
			/* JCK 990717 BEGIN */
			dx = 0;
			dy = 0;
			/* JCK 990717 END */
			if (ADDRESS_BITS(WatchesCpuNo[s]) < 25) break;
		case KEYCODE_6:
			/* JCK 990717 BEGIN */
			dx = 0;
			dy = 0;
			/* JCK 990717 END */
			if (ADDRESS_BITS(WatchesCpuNo[s]) < 21) break;
		case KEYCODE_5:
			/* JCK 990717 BEGIN */
			dx = 0;
			dy = 0;
			/* JCK 990717 END */
			if (ADDRESS_BITS(WatchesCpuNo[s]) < 17) break;
		case KEYCODE_4:
			/* JCK 990717 BEGIN */
			dx = 0;
			dy = 0;
			/* JCK 990717 END */
			if (ADDRESS_BITS(WatchesCpuNo[s]) < 13) break;
		case KEYCODE_3:
		case KEYCODE_2:
		case KEYCODE_1:
			{
				int addr = Watches[ s ];	/* copy address*/
				int digit = (KEYCODE_8 - key);	/* if key is KEYCODE_8, digit = 0 */
				int mask;

				/* JCK 990717 BEGIN */
				dx = 0;
				dy = 0;
				/* JCK 990717 END */

				/* adjust digit based on cpu address range */
				/* digit -= (6 - ADDRESS_BITS(0) / 4); */
				digit -= (8 - (ADDRESS_BITS(WatchesCpuNo[s])+3) / 4);

				mask = 0xF << (digit * 4);	/* if digit is 1, mask = 0xf0*/

				do
				{
				if ((addr & mask) == mask)
					/* wrap hex digit around to 0 if necessary */
					addr &= ~mask;
				else
					/* otherwise bump hex digit by 1 */
					addr += (0x1 << (digit * 4));
				} while (addr > MAX_ADDRESS(WatchesCpuNo[s]));

				Watches[ s ] = addr;
				AddCpuToWatch(s, str2[ s ]);

			}
			break;

		case KEYCODE_9:
			/* JCK 990717 BEGIN */
			dx = 0;
			dy = 0;
			/* JCK 990717 END */
			if (ManyCpus)
			{
				OldCpuNo = WatchesCpuNo[ s ];
				if(WatchesCpuNo[ s ] == 0)
					WatchesCpuNo[ s ] = cpu_gettotalcpu()-1;
				else
					WatchesCpuNo[ s ]--;

				if (Watches[s] == MAX_ADDRESS(OldCpuNo))
					Watches[s] = MAX_ADDRESS(WatchesCpuNo[s]);

				AddCpuToWatch(s, str2[ s ]);

				if (MAX_ADDRESS(WatchesCpuNo[s]) != MAX_ADDRESS(OldCpuNo))
				{
					ClearTextLine(0, dt[s].y);

					/* dt[s].x = (MachWidth - FontWidth * strlen(str2[ s ])) / 2; */
					  if (s < MAX_WATCHES / 2)
					  {
						dt[s].x = ((MachWidth / 2) - FontWidth * strlen(str2[ s ])) / 2;
					  }
					  else
					  {
						dt[s].x = ((MachWidth / 2) - FontWidth * strlen(str2[ s ])) / 2 + (MachWidth / 2);
					  }

					cheat_clearbitmap();

					y = ChooseWatchHeader();
				}
			}
			break;

		case KEYCODE_0:
			/* JCK 990717 BEGIN */
			dx = 0;
			dy = 0;
			/* JCK 990717 END */
			if (ManyCpus)
			{
				OldCpuNo = WatchesCpuNo[ s ];
				if(WatchesCpuNo[ s ] >= cpu_gettotalcpu()-1)
					WatchesCpuNo[ s ] = 0;
				else
					WatchesCpuNo[ s ]++;

				if (Watches[s] == MAX_ADDRESS(OldCpuNo))
					Watches[s] = MAX_ADDRESS(WatchesCpuNo[s]);

				AddCpuToWatch(s, str2[ s ]);

				if (MAX_ADDRESS(WatchesCpuNo[s]) != MAX_ADDRESS(OldCpuNo))
				{
					ClearTextLine(0, dt[s].y);

					/* dt[s].x = (MachWidth - FontWidth * strlen(str2[ s ])) / 2; */
					  if (s < MAX_WATCHES / 2)
					  {
						dt[s].x = ((MachWidth / 2) - FontWidth * strlen(str2[ s ])) / 2;
					  }
					  else
					  {
						dt[s].x = ((MachWidth / 2) - FontWidth * strlen(str2[ s ])) / 2 + (MachWidth / 2);
					  }

					cheat_clearbitmap();

					y = ChooseWatchHeader();
				}
			}
			break;

		case KEYCODE_DEL:
		case KEYCODE_DEL_PAD:
			while (keyboard_pressed(key)); /* wait for key release */
			/* JCK 990717 BEGIN */
			dx = 0;
			dy = 0;
			/* JCK 990717 END */
			OldCpuNo = WatchesCpuNo[ s ];
			WatchesCpuNo[ s ] = 0;
			Watches[ s ] = MAX_ADDRESS(WatchesCpuNo[ s ]);
			AddCpuToWatch(s, str2[ s ]);

			if (MAX_ADDRESS(WatchesCpuNo[s]) != MAX_ADDRESS(OldCpuNo))
			{
				ClearTextLine(0, dt[s].y);

				/* dt[s].x = (MachWidth - FontWidth * strlen(str2[ s ])) / 2; */
				  if (s < MAX_WATCHES / 2)
				  {
					dt[s].x = ((MachWidth / 2) - FontWidth * strlen(str2[ s ])) / 2;
				  }
				  else
				  {
					dt[s].x = ((MachWidth / 2) - FontWidth * strlen(str2[ s ])) / 2 + (MachWidth / 2);
				  }

				cheat_clearbitmap();

				y = ChooseWatchHeader();
			}

			break;

		case KEYCODE_F1:
			while (keyboard_pressed(key)); /* wait for key release */
			/* JCK 990717 BEGIN */
			dx = 0;
			dy = 0;
			/* JCK 990717 END */
		  oldkey = 0;
			if (Watches[s] == MAX_ADDRESS(WatchesCpuNo[s]))
				break;

			if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
			{
				break;
			}

			AddCpuToAddr(WatchesCpuNo[s], Watches[s],
					RD_GAMERAM(WatchesCpuNo[s], Watches[s]), buf);

			if (LoadedCheatTotal < MAX_LOADEDCHEATS-1)
			{
				set_cheat(&LoadedCheatTable[LoadedCheatTotal], NEW_CHEAT);
				LoadedCheatTable[LoadedCheatTotal].CpuNo   = WatchesCpuNo[s];
				LoadedCheatTable[LoadedCheatTotal].Address = Watches[s];
				LoadedCheatTable[LoadedCheatTotal].Data    = RD_GAMERAM(WatchesCpuNo[s], Watches[s]);
				strcpy(LoadedCheatTable[LoadedCheatTotal].Name, buf);
				LoadedCheatTotal++;
				xprintf(0, 0,YFOOT_WATCH,"%s Added",buf);
			}
			else
				xprintf(0, 0,YFOOT_WATCH,"%s Not Added",buf);
			break;

		case KEYCODE_F3:
			while (keyboard_pressed(key));
			/* JCK 990717 BEGIN */
			dx = 0;
			dy = 0;
			/* JCK 990717 END */
		  oldkey = 0;

			if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
			{
				break;
			}

			for (i = 0; i < total; i++)
				dt[i].color = DT_COLOR_WHITE;
			displaytext (dt, 0,1);
			sprintf(buffer, FormatAddr(WatchesCpuNo[s],0), Watches[s]);
			xedit(0, YFOOT_WATCH, buffer, strlen(buffer), 1);
			sscanf(buffer,"%X", &Watches[s]);
			if (Watches[s] > MAX_ADDRESS(WatchesCpuNo[s]))
				Watches[s] = MAX_ADDRESS(WatchesCpuNo[s]);
			AddCpuToWatch(s, str2[ s ]);
		  break;

		case KEYCODE_F4:
			while (keyboard_pressed(key)); /* wait for key release */
			/* JCK 990717 BEGIN */
			dx = 0;
			dy = 0;
			/* JCK 990717 END */
		  oldkey = 0;

			if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
			{
				break;
			}

			for (i=0;i<MAX_WATCHES;i++)
			{
				if (Watches[i] == MAX_ADDRESS(WatchesCpuNo[i]))
				{
					OldCpuNo = WatchesCpuNo[ i ];
					WatchesCpuNo[i] = WatchesCpuNo[s];
					Watches[i] = Watches[s];

					AddCpuToWatch(i, str2[ i ]);

					if (MAX_ADDRESS(WatchesCpuNo[i]) != MAX_ADDRESS(OldCpuNo))
					{
						ClearTextLine(0, dt[i].y);

						/* dt[i].x = (MachWidth - FontWidth * strlen(str2[ i ])) / 2; */
						  if (i < MAX_WATCHES / 2)
						  {
							dt[i].x = ((MachWidth / 2) - FontWidth * strlen(str2[ i ])) / 2;
						  }
						  else
						  {
							dt[i].x = ((MachWidth / 2) - FontWidth * strlen(str2[ i ])) / 2 + (MachWidth / 2);
						  }

						cheat_clearbitmap();

						y = ChooseWatchHeader();
					}

					break;
				}
			}
			break;

		case KEYCODE_F6:
			while (keyboard_pressed(key)); /* wait for key release */
			/* JCK 990717 BEGIN */
			dx = 0;
			dy = 0;
			/* JCK 990717 END */
		  oldkey = 0;

			if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
			{
				break;
			}

			for (i=0;i<MAX_WATCHES;i++)
			{
				if(LoadedCheatTotal > MAX_LOADEDCHEATS-1)
					break;
				if (Watches[i] != MAX_ADDRESS(WatchesCpuNo[i]))
				{
					countAdded++;

					AddCpuToAddr(WatchesCpuNo[i], Watches[i],
							RD_GAMERAM(WatchesCpuNo[i], Watches[i]), buf);

					set_cheat(&LoadedCheatTable[LoadedCheatTotal], NEW_CHEAT);
					LoadedCheatTable[LoadedCheatTotal].CpuNo   = WatchesCpuNo[i];
					LoadedCheatTable[LoadedCheatTotal].Address = Watches[i];
					LoadedCheatTable[LoadedCheatTotal].Data    = RD_GAMERAM(WatchesCpuNo[i], Watches[i]);
					strcpy(LoadedCheatTable[LoadedCheatTotal].Name,buf);
					LoadedCheatTotal++;
				}

			}
			xprintf(0, 0,YFOOT_WATCH,"%d Added",countAdded);

			break;

		case KEYCODE_F7:
			while (keyboard_pressed(key)); /* wait for key release */
			/* JCK 990717 BEGIN */
			dx = 0;
			dy = 0;
			/* JCK 990717 END */
		  oldkey = 0;

			if (keyboard_pressed (KEYCODE_LSHIFT) || keyboard_pressed (KEYCODE_RSHIFT))
			{
				break;
			}

			for (i=0;i<MAX_WATCHES;i++)
			{
				WatchesCpuNo[ i ] = 0;
				Watches[ i ] = MAX_ADDRESS(WatchesCpuNo[ i ]);
				AddCpuToWatch(i, str2[ i ]);
			}
			break;

		case KEYCODE_F10:
			while (keyboard_pressed(key)); /* wait for key release */
			/* JCK 990717 BEGIN */
			dx = 0;
			dy = 0;
			/* JCK 990717 END */
		  oldkey = 0;
			ChooseWatchHelp();								/* Show Help */
			y = ChooseWatchHeader();
			break;

		case KEYCODE_ENTER:
		case KEYCODE_ENTER_PAD:
			/* JCK 990717 BEGIN */
			dx = 0;
			dy = 0;
			/* JCK 990717 END */
			if (s == 0)
				break;
			OldCpuNo = WatchesCpuNo[ s ];
			WatchesCpuNo[ s ] = WatchesCpuNo[ s - 1 ];
			Watches[ s ] = Watches[ s - 1 ];
			AddCpuToWatch(s, str2[ s ]);

			if (MAX_ADDRESS(WatchesCpuNo[s]) != MAX_ADDRESS(OldCpuNo))
			{
				ClearTextLine(0, dt[s].y);

				/* dt[s].x = (MachWidth - FontWidth * strlen(str2[ s ])) / 2; */
				  if (s < MAX_WATCHES / 2)
				  {
					dt[s].x = ((MachWidth / 2) - FontWidth * strlen(str2[ s ])) / 2;
				  }
				  else
				  {
					dt[s].x = ((MachWidth / 2) - FontWidth * strlen(str2[ s ])) / 2 + (MachWidth / 2);
				  }

				cheat_clearbitmap();

				y = ChooseWatchHeader();
			}

			break;
	}

	/* Set Watch Flag */
	WatchesFlag = 0;
	for(i = 0;i < MAX_WATCHES;i ++)
		if(Watches[i] != MAX_ADDRESS(WatchesCpuNo[i]))
		{
			WatchesFlag = 1;
			WatchEnabled = 1;
		}

  } while (done != 2);

  while (keyboard_pressed(key)) ; /* wait for key release */

  cheat_clearbitmap();

  Machine->orientation = trueorientation;
}


int cheat_menu(void)
{
  int x,y;
  int s,key,done;
  int total;

  char *paDisplayText[] = {
		"Load And/Or Enable A Cheat",
		"Start A New Cheat Search",
		"Continue Search",
		"View Last Results",
		"Restore Previous Results",
		"Memory Watch",
		"",
		"General Help",
		"",
		"Return To Main Menu",
		0 };

  struct DisplayText dt[20];

  cheat_save_frameskips();

  total = create_menu(paDisplayText, dt, FIRSTPOS);
  x = dt[total-1].x;
  y = dt[total-1].y + 2 * FontHeight;

  cheat_clearbitmap();

  DisplayActiveCheats(y);

  oldkey = 0;

  s = SaveMenu;
  done = 0;
  do
  {
	key = SelectMenu (&s, dt, 0, 0, 0, total-1, 0, &done);
	switch (key)
	{
		case KEYCODE_ENTER:
		case KEYCODE_ENTER_PAD:
			switch (s)
			{
				case 0:
					SelectCheat();
					done = 0;
					DisplayActiveCheats(y);
					break;

				case 1:
					StartSearch();
					done = 0;
					DisplayActiveCheats(y);
					break;

				case 2:
					ContinueSearch(1, 0);
					done = 0;
					DisplayActiveCheats(y);
					break;

				case 3:
					ContinueSearch(1, 1);
					done = 0;
					DisplayActiveCheats(y);
					break;

				case 4:
					RestoreSearch();
					done = 0;
					DisplayActiveCheats(y);
					break;

				case 5:
					ChooseWatch();
					done = 0;
					DisplayActiveCheats(y);
					break;

				case 7:
					DisplayHelpFile();
					done = 0;
					DisplayActiveCheats(y);
					break;

				case 9:
					done = 1;
				  s = 0;
					break;
			}
			break;
	}
  } while ((done != 1) && (done != 2));

  SaveMenu = s;

  /* clear the screen before returning */
  osd_clearbitmap(Machine->scrbitmap);

  cheat_rest_frameskips();

  if (done == 2)
	return 1;
  else
	return 0;
}

/* Free allocated arrays */
void StopCheat(void)
{
  reset_table (StartRam);
  reset_table (BackupRam);
  reset_table (FlagTable);

  reset_table (OldBackupRam);
  reset_table (OldFlagTable);

  reset_texttable (HelpLine);
}

void DoCheat(void)
{
	int i;
	char buf[80];


	DisplayWatches(0, &WatchX, &WatchY, buf, MAX_WATCHES, 0, 0);

	/* Affect the memory */
	for (i = 0; CheatEnabled == 1 && i < ActiveCheatTotal;i ++)
	{
		if (	(ActiveCheatTable[i].Special == 0)		||
			(ActiveCheatTable[i].Special == OFFSET_LINK_CHEAT)	)
		{
			WR_GAMERAM (ActiveCheatTable[i].CpuNo,
				ActiveCheatTable[i].Address,
				ActiveCheatTable[i].Data);
		}
		else
		{
			if (ActiveCheatTable[i].Count == 0)
			{
				/* Check special function */
				switch(ActiveCheatTable[i].Special)
				{
					case 1:
					case 1 + OFFSET_LINK_CHEAT:
						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
						ActiveCheatTable[i].Address,
								ActiveCheatTable[i].Data);
					DeleteActiveCheatFromTable(i);
						break;
					case 2:
					case 2 + OFFSET_LINK_CHEAT:
						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
						ActiveCheatTable[i].Address,
								ActiveCheatTable[i].Data);
						ActiveCheatTable[i].Count = 1*60;
						break;
					case 3:
					case 3 + OFFSET_LINK_CHEAT:
						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
						ActiveCheatTable[i].Address,
								ActiveCheatTable[i].Data);
						ActiveCheatTable[i].Count = 2*60;
						break;
					case 4:
					case 4 + OFFSET_LINK_CHEAT:
						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
						ActiveCheatTable[i].Address,
								ActiveCheatTable[i].Data);
						ActiveCheatTable[i].Count = 5*60;
						break;

					/* 5,6,7 check if the value has changed, if yes, start a timer
						when the timer end, change the location*/
					case 5:
					case 5 + OFFSET_LINK_CHEAT:
						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
							ActiveCheatTable[i].Address) !=
						ActiveCheatTable[i].Data)
						{
							ActiveCheatTable[i].Count = 1*60;
							ActiveCheatTable[i].Special += 1000;
						}
						break;
					case 6:
					case 6 + OFFSET_LINK_CHEAT:
						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
							ActiveCheatTable[i].Address) !=
						ActiveCheatTable[i].Data)
						{
							ActiveCheatTable[i].Count = 2*60;
							ActiveCheatTable[i].Special += 1000;
						}
						break;
					case 7:
					case 7 + OFFSET_LINK_CHEAT:
						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
							ActiveCheatTable[i].Address) !=
						ActiveCheatTable[i].Data)
						{
							ActiveCheatTable[i].Count = 5*60;
							ActiveCheatTable[i].Special += 1000;
						}
						break;

					/* 8,9,10,11 do not change the location if the value change by X every frames
					   This is to try to not change the value of an energy bar
					   when a bonus is awarded to it at the end of a level
					   See Kung Fu Master*/
					case 8:
					case 8 + OFFSET_LINK_CHEAT:
						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
							ActiveCheatTable[i].Address) !=
						ActiveCheatTable[i].Data)
						{
							ActiveCheatTable[i].Count = 1;
							ActiveCheatTable[i].Special += 1000;
							ActiveCheatTable[i].Backup =
								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
								ActiveCheatTable[i].Address);
						}
						break;
					case 9:
					case 9 + OFFSET_LINK_CHEAT:
						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
							ActiveCheatTable[i].Address) !=
						ActiveCheatTable[i].Data)
						{
							ActiveCheatTable[i].Count = 1;
							ActiveCheatTable[i].Special += 1000;
							ActiveCheatTable[i].Backup =
								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
								ActiveCheatTable[i].Address);
						}
						break;
					case 10:
					case 10 + OFFSET_LINK_CHEAT:
						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
							ActiveCheatTable[i].Address) !=
						ActiveCheatTable[i].Data)
						{
							ActiveCheatTable[i].Count = 1;
							ActiveCheatTable[i].Special += 1000;
							ActiveCheatTable[i].Backup =
								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
								ActiveCheatTable[i].Address);
						}
						break;
					case 11:
					case 11 + OFFSET_LINK_CHEAT:
						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
							ActiveCheatTable[i].Address) !=
						ActiveCheatTable[i].Data)
						{
							ActiveCheatTable[i].Count = 1;
							ActiveCheatTable[i].Special += 1000;
							ActiveCheatTable[i].Backup =
								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
								ActiveCheatTable[i].Address);
						}
						break;

					case 20:
					case 20 + OFFSET_LINK_CHEAT:
						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
						ActiveCheatTable[i].Address,
								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
								ActiveCheatTable[i].Address) |
									ActiveCheatTable[i].Data);
						break;
					case 21:
					case 21 + OFFSET_LINK_CHEAT:
						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
						ActiveCheatTable[i].Address,
								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
								ActiveCheatTable[i].Address) |
									ActiveCheatTable[i].Data);
					DeleteActiveCheatFromTable(i);
						break;
					case 22:
					case 22 + OFFSET_LINK_CHEAT:
						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
						ActiveCheatTable[i].Address,
								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
								ActiveCheatTable[i].Address) |
								ActiveCheatTable[i].Data);
						ActiveCheatTable[i].Count = 1*60;
						break;
					case 23:
					case 23 + OFFSET_LINK_CHEAT:
						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
						ActiveCheatTable[i].Address,
								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
								ActiveCheatTable[i].Address) |
								ActiveCheatTable[i].Data);
						ActiveCheatTable[i].Count = 2*60;
						break;
					case 24:
					case 24 + OFFSET_LINK_CHEAT:
						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
						ActiveCheatTable[i].Address,
								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
								ActiveCheatTable[i].Address) |
								ActiveCheatTable[i].Data);
						ActiveCheatTable[i].Count = 5*60;
						break;
					case 40:
					case 40 + OFFSET_LINK_CHEAT:
						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
						ActiveCheatTable[i].Address,
								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
								ActiveCheatTable[i].Address) &
									~ActiveCheatTable[i].Data);
						break;
					case 41:
					case 41 + OFFSET_LINK_CHEAT:
						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
						ActiveCheatTable[i].Address,
								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
								ActiveCheatTable[i].Address) &
									~ActiveCheatTable[i].Data);
					DeleteActiveCheatFromTable(i);
						break;
					case 42:
					case 42 + OFFSET_LINK_CHEAT:
						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
						ActiveCheatTable[i].Address,
								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
								ActiveCheatTable[i].Address) &
								~ActiveCheatTable[i].Data);
						ActiveCheatTable[i].Count = 1*60;
						break;
					case 43:
					case 43 + OFFSET_LINK_CHEAT:
						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
						ActiveCheatTable[i].Address,
								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
								ActiveCheatTable[i].Address) &
									~ActiveCheatTable[i].Data);
						ActiveCheatTable[i].Count = 2*60;
						break;
					case 44:
					case 44 + OFFSET_LINK_CHEAT:
						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
						ActiveCheatTable[i].Address,
								RD_GAMERAM (ActiveCheatTable[i].CpuNo,
								ActiveCheatTable[i].Address) &
								~ActiveCheatTable[i].Data);
						ActiveCheatTable[i].Count = 5*60;
						break;

					case 60:
					case 61:
					case 62:
					case 63:
					case 64:
					case 65:
						ActiveCheatTable[i].Count = 1;
						ActiveCheatTable[i].Special += 1000;
						ActiveCheatTable[i].Backup =
							RD_GAMERAM (ActiveCheatTable[i].CpuNo,
							ActiveCheatTable[i].Address);
						break;
					case 70:
					case 71:
					case 72:
					case 73:
					case 74:
					case 75:
						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
						ActiveCheatTable[i].Address,
								ActiveCheatTable[i].Data);
					DeleteActiveCheatFromTable(i);
						break;

						/*Special case, linked with 5,6,7 */
					case 1005:
					case 1005 + OFFSET_LINK_CHEAT:	  /* Linked cheat */
						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
						ActiveCheatTable[i].Address,
								ActiveCheatTable[i].Data);
						ActiveCheatTable[i].Special -= 1000;
						break;
					case 1006:
					case 1006 + OFFSET_LINK_CHEAT:	  /* Linked cheat */
						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
						ActiveCheatTable[i].Address,
								ActiveCheatTable[i].Data);
						ActiveCheatTable[i].Special -= 1000;
						break;
					case 1007:
					case 1007 + OFFSET_LINK_CHEAT:	  /* Linked cheat */
						WR_GAMERAM (ActiveCheatTable[i].CpuNo,
						ActiveCheatTable[i].Address,
								ActiveCheatTable[i].Data);
						ActiveCheatTable[i].Special -= 1000;
						break;

					/*Special case, linked with 8,9,10,11 */
					/* Change the memory only if the memory decreased by X */
					case 1008:
					case 1008 + OFFSET_LINK_CHEAT:	  /* Linked cheat */
						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
								ActiveCheatTable[i].Address) !=
						ActiveCheatTable[i].Backup-1)
							WR_GAMERAM (ActiveCheatTable[i].CpuNo,
							ActiveCheatTable[i].Address,
									ActiveCheatTable[i].Data);
						ActiveCheatTable[i].Special -= 1000;
						break;
					case 1009:
					case 1009 + OFFSET_LINK_CHEAT:	  /* Linked cheat */
						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
							ActiveCheatTable[i].Address) !=
						ActiveCheatTable[i].Backup-2)
							WR_GAMERAM (ActiveCheatTable[i].CpuNo,
							ActiveCheatTable[i].Address,
									ActiveCheatTable[i].Data);
						ActiveCheatTable[i].Special -= 1000;
						break;
					case 1010:
					case 1010 + OFFSET_LINK_CHEAT:	  /* Linked cheat */
						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
							ActiveCheatTable[i].Address) !=
						ActiveCheatTable[i].Backup-3)
							WR_GAMERAM (ActiveCheatTable[i].CpuNo,
							ActiveCheatTable[i].Address,
									ActiveCheatTable[i].Data);
						ActiveCheatTable[i].Special -= 1000;
						break;
					case 1011:
					case 1011 + OFFSET_LINK_CHEAT:	  /* Linked cheat */
						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
							ActiveCheatTable[i].Address) !=
						ActiveCheatTable[i].Backup-4)
							WR_GAMERAM (ActiveCheatTable[i].CpuNo,
							ActiveCheatTable[i].Address,
									ActiveCheatTable[i].Data);
						ActiveCheatTable[i].Special -= 1000;
						break;

					/*Special case, linked with 60 .. 65 */
					/* Change the memory only if the memory has changed since the last backup */
					case 1060:
					case 1061:
					case 1062:
					case 1063:
					case 1064:
					case 1065:
						if (	RD_GAMERAM (ActiveCheatTable[i].CpuNo,
							ActiveCheatTable[i].Address) !=
						ActiveCheatTable[i].Backup)
						{
							WR_GAMERAM (ActiveCheatTable[i].CpuNo,
							ActiveCheatTable[i].Address,
									ActiveCheatTable[i].Data);
					DeleteActiveCheatFromTable(i);
						}
						break;

			}	/* end switch */
		} /* end if (ActiveCheatTable[i].Count == 0) */
		else
		{
			ActiveCheatTable[i].Count--;
		}
		} /* end else */
	} /* end for */

  /* KEYCODE_CHEAT_TOGGLE Enable/Disable the active cheats on the fly. Required for some cheat */
  if (input_ui_pressed(IPT_UI_TOGGLE_CHEAT) && ActiveCheatTotal)
  {
	  CheatEnabled ^= 1;
	  usrintf_showmessage("Cheats %s", (CheatEnabled ? "On" : "Off"));
  }

  /* KEYCODE_INSERT toggles the Watch display ON */
  if ( keyboard_pressed_memory( KEYCODE_INSERT ) && (WatchEnabled == 0) )
  {
	WatchEnabled = 1;
  }
  /* KEYCODE_DEL toggles the Watch display OFF */
  if ( keyboard_pressed_memory( KEYCODE_DEL ) && (WatchEnabled != 0) ){
	WatchEnabled = 0;
  }

  /* KEYCODE_HOME loads the main menu of the cheat engine */
  if ( keyboard_pressed_memory( KEYCODE_HOME ) )
  {
	osd_sound_enable(0);
	cheat_menu();
	osd_sound_enable(1);
  }

  /* KEYCODE_END loads the "Continue Search" sub-menu of the cheat engine */
  if ( keyboard_pressed_memory( KEYCODE_END ) )
  {
	osd_sound_enable(0);
	ContinueSearch(0, 0);
	osd_sound_enable(1);
  }

}


void ShowHelp(int LastHelpLine, struct TextLine *table)
{
  int LineNumber = 0;
  int LinePerPage = MachHeight / FontHeight - 6;
  int yFirst = 0;
  int yPos;
  int key = 0;
  int done = 0;
  struct TextLine *txt;
  char buffer[40];
  struct DisplayText dt[2];

  sprintf(buffer, "%s Return to Main Menu %s", lefthilight, righthilight);
  dt[0].text = buffer;
  dt[0].color = DT_COLOR_WHITE;
  dt[0].x = (MachWidth - FontWidth * strlen(buffer)) / 2;
  dt[0].y = MachHeight - 3 * FontHeight;
  dt[1].text = 0;

  oldkey = 0;

  cheat_clearbitmap();

  if (!LastHelpLine)
  {
	yPos = (MachHeight - FontHeight) / 2 - FontHeight;
	xprintf(0, 0, yPos, "No Help Available !");
	displaytext(dt,0,1);
	key = keyboard_read_sync();
	while (keyboard_pressed(key));
  }
  else
  {
	if (LastHelpLine < LinePerPage)
	{
		yFirst = ((LinePerPage - LastHelpLine) / 2) * FontHeight;
		LinePerPage = LastHelpLine;
	}
	done = 0;
	do
	{
		if (key != NOVALUE)
		{
			cheat_clearbitmap();
			yPos = yFirst;
			if ((LineNumber > 0) && (LastHelpLine > LinePerPage))
			{
				xprintf (0, 0, yPos, uparrow);
			}
			yPos += FontHeight;
			for (txt = table; txt->data; txt++)
			{
				if (txt->number >= LineNumber + LinePerPage)
					break;
				if (txt->number >= LineNumber)
				{
					xprintf (0, 0, yPos, "%-30s", txt->data);
					yPos += FontHeight;
				}
			}
			if (LineNumber < LastHelpLine - LinePerPage)
			{
				xprintf (0, 0, yPos, downarrow);
			}
			displaytext(dt,0,1);
		}
		else
		{
			oldkey = 0;
		}
		key = cheat_readkey();	  /* MSH 990217 */
		switch (key)
		{
			case KEYCODE_DOWN:
			case KEYCODE_2_PAD:
				if (LineNumber < LastHelpLine - LinePerPage)
				{
					LineNumber ++;
				}
				else
				{
					key = NOVALUE;
				}
				break;

			case KEYCODE_UP:
			case KEYCODE_8_PAD:
				if (LineNumber > 0)
				{
					LineNumber --;
				}
				else
				{
					key = NOVALUE;
				}
				break;

			case KEYCODE_HOME:
			case KEYCODE_7_PAD:
				if (LineNumber != 0)
				{
					LineNumber = 0;
				}
				else
				{
					key = NOVALUE;
				}
				break;

			case KEYCODE_END:
			case KEYCODE_1_PAD:
				if (LineNumber != LastHelpLine - LinePerPage)
				{
					LineNumber = LastHelpLine - LinePerPage;
				}
				else
				{
					key = NOVALUE;
				}
				break;

			case KEYCODE_PGDN:
			case KEYCODE_3_PAD:
				while (keyboard_pressed(key)); /* wait for key release */
				if (LineNumber < LastHelpLine - LinePerPage)
				{
					LineNumber += LinePerPage;
				}
				else
				{
					key = NOVALUE;
				}
				if (LineNumber > LastHelpLine - LinePerPage)
				{
					LineNumber = LastHelpLine - LinePerPage;
				}
				break;

			case KEYCODE_PGUP:
			case KEYCODE_9_PAD:
				while (keyboard_pressed(key)); /* wait for key release */
				if (LineNumber > 0)
				{
					LineNumber -= LinePerPage;
				}
				else
				{
					key = NOVALUE;
				}
				if (LineNumber < 0)
				{
					LineNumber = 0;
				}
				break;
			case KEYCODE_ESC:
			case KEYCODE_TAB:
			case KEYCODE_ENTER:
			case KEYCODE_ENTER_PAD:
				while (keyboard_pressed(key));
				oldkey = 0;
				done = 1;
				break;
			default:
				key = NOVALUE;
				break;
		}
	} while (done == 0);

	while (keyboard_pressed(key));
  }

  cheat_clearbitmap();
}

void CheatListHelp (void)
{
	int LastHelpLine;
	char *paDisplayText[] = {
		"        Cheat List Help" ,
		"" ,
		"Delete:" ,
		"  Delete the selected Cheat" ,
		"  from the Cheat List." ,
		"  (Not from the Cheat File!)" ,
		"" ,
		"Add:" ,
		"  Add a new (blank) Cheat to" ,
		"  the Cheat List." ,
		"" ,
		"Save (F1):" ,
		"  Save the selected Cheat in" ,
		"  the Cheat File." ,
		"" ,
		"Watch (F2):" ,
		"  Activate a Memory Watcher" ,
		"  at the address that the" ,
		"  selected Cheat modifies." ,
		"" ,
		"Edit (F3):" ,
		"  Edit the Properties of the" ,
		"  selected Cheat." ,
		"" ,
		"Copy (F4):" ,
		"  Copy the selected Cheat" ,
		"  to the Cheat List." ,
		"" ,
		"Load (F5):" ,
		"  Load a Cheat Database" ,
		"" ,
		"Save All (F6):" ,
		"  Save all the Cheats in" ,
		"  the Cheat File." ,
		"" ,
		"Del All (F7):" ,
		"  Remove all the active Cheats" ,
		"" ,
		"Reload (F8):" ,
		"  Reload the Cheat Database" ,
		"" ,
		"Rename (F9):" ,
		"  Rename the Cheat Database" ,
		"" ,
		"Help (F10):" ,
		"  Display this help" ,
		"" ,
		"Sologame ON/OFF (F11):" ,
		"  Toggles this option ON/OFF." ,
		"  When Sologame is ON, only" ,
		"  Cheats for Player 1 are" ,
		"  Loaded from the Cheat File.",
		"" ,
		"Info (F12):" ,
		"  Display Info on a Cheat" ,
		"" ,
		"More info (+):" ,
		"  Display the Extra Description" ,
		"  of a Cheat if any." ,
		"" ,
		"Add from file (Shift+F5):" ,
		"  Add the Cheats from a Cheat" ,
		"  Database to the current" ,
		"  Cheat Database." ,
		"  (Only In Memory !)" ,
		0 };

	LastHelpLine = CreateHelp(paDisplayText, HelpLine);
	ShowHelp(LastHelpLine, HelpLine);
	reset_texttable (HelpLine);
}

void CheatListHelpEmpty (void)
{
	int LastHelpLine;
	char *paDisplayText[] = {
		"       Cheat List Help",
		"",
		"Add:",
		"  Add a new (blank) Cheat to",
		"  the Cheat List.",
		"",
		"Load (F5):",
		"  Load a Cheat Database",
		"",
		"Reload (F8):",
		"  Reload the Cheat Database",
		"",
		"Rename (F9):",
		"  Rename the Cheat Database",
		"",
		"Help (F10):",
		"  Display this help",
		"",
		"Sologame ON/OFF (F11):",
		"  Toggles this option ON/OFF.",
		"  When Sologame is ON, only",
		"  Cheats for Player 1 are",
		"  Loaded from the Cheat File.",
		0 };

	LastHelpLine = CreateHelp(paDisplayText, HelpLine);
	ShowHelp(LastHelpLine, HelpLine);
	reset_texttable (HelpLine);
}

void StartSearchHelp (void)
{
	int LastHelpLine;
	char *paDisplayText[] = {
		"    Cheat Search Help" ,
		"" ,
		"Lives Or Some Other Value:" ,
		" Searches for a specific" ,
		" value that you specify." ,
		"" ,
		"Timers:" ,
		" Starts by storing all of" ,
		" the game's memory, and then" ,
		" looking for values that" ,
		" have changed by a specific" ,
		" amount from the value that" ,
		" was stored when the search" ,
		" was started or continued." ,
		"" ,
		"Energy:" ,
		" Similar to Timers. Searches" ,
		" for values that are Greater" ,
		" than, Less than, or Equal" ,
		" to the values stored when" ,
		" the search was started or" ,
		" continued." ,
		"" ,
		"Status:" ,
		"  Searches for a Bit or Flag" ,
		"  that may or may not have" ,
		"  toggled its value since" ,
		"  the search was started." ,
		"" ,
		"Slow But Sure:" ,
		"  This search stores all of" ,
		"  the game's memory, and then" ,
		"  looks for values that are" ,
		"  the Same As, or Different" ,
		"  from the values stored when" ,
		"  the search was started." ,
		"" ,
		"Select Search Speed:" ,
		"  This allow you scan all" ,
		"  or part of memory areas" ,
		0 };

	LastHelpLine = CreateHelp(paDisplayText, HelpLine);
	ShowHelp(LastHelpLine, HelpLine);
	reset_texttable (HelpLine);
}

void EditCheatHelp (void)
{
	int LastHelpLine;
	char *paDisplayText[] = {
		"      Edit Cheat Help" ,
		"" ,
		"Name:" ,
		"  Displays the Name of this" ,
		"  Cheat. It can be edited by" ,
		"  hitting <ENTER> while it is" ,
		"  selected.  Cheat Names are" ,
		"  limited to 29 characters." ,
		"  You can use <SHIFT> to" ,
		"  uppercase a character, but" ,
		"  only one character at a" ,
		"  time!" ,
		"" ,
		"CPU:" ,
		"  Specifies the CPU (memory" ,
		"  region) that gets affected." ,
		"" ,
		"Address:" ,
		"  The Address of the location" ,
		"  in memory that gets set to" ,
		"  the new value." ,
		"" ,
		"Value:" ,
		"  The new value that gets" ,
		"  placed into the specified" ,
		"  Address while the Cheat is" ,
		"  active." ,
		"" ,
		"Type:" ,
		"  Specifies how the Cheat" ,
		"  will actually work. See the" ,
		"  general help for details." ,
		"" ,
		"More:" ,
		"  Same as Name. This is" ,
		"  the extra description." ,
		"" ,
		"Notes:" ,
		"  Use the Right and Left" ,
		"  arrow keys to increment and" ,
		"  decrement values, or to" ,
		"  select from pre-defined" ,
		"  Cheat Names." ,
		"  The <1> ... <8> keys are used" ,
		"  to increment the number in" ,
		"  that specific column of a" ,
		"  value." ,
		0 };

	LastHelpLine = CreateHelp(paDisplayText, HelpLine);
	ShowHelp(LastHelpLine, HelpLine);
	reset_texttable (HelpLine);
}

void ChooseWatchHelp (void)
{
	int LastHelpLine;
	char *paDisplayText[] = {
		"      Choose Watch Help" ,
		"" ,
		"Delete:" ,
		"  Delete the selected Watch" ,
		"" ,
		"Copy (Enter):" ,
		"  Copy the previous Watch" ,
		"" ,
		"Notes:" ,
		"  Use the Right and Left" ,
		"  arrow keys to increment and" ,
		"  decrement values.",
		"  The <1> ... <8> keys are used" ,
		"  to increment the number in" ,
		"  that specific column of a" ,
		"  value. The <9> and <0> keys" ,
		"  are used to decrement/increment",
		"  the number of the CPU." ,
		"  The <I><J><K><L> keys are used" ,
		"  to move the watches up, left," ,
		"  down and right." ,
		"" ,
		"Save (F1):" ,
		"  Save the selected Watch" ,
		"  as a Cheat in the Cheat List" ,
		"" ,
		"Edit (F3):" ,
		"  Edit the Address of the Watch" ,
		"" ,
		"Far Copy (F4):" ,
		"  Copy the selected Watch" ,
		"" ,
		"Save All (F6):" ,
		"  Save all the Watches" ,
		"  as Cheats in the Cheat List" ,
		"" ,
		"Del All (F7):" ,
		"  Remove all the Watches" ,
		"" ,
		"Help (F10):" ,
		"  Display this help" ,
		0 };

	LastHelpLine = CreateHelp(paDisplayText, HelpLine);
	ShowHelp(LastHelpLine, HelpLine);
	reset_texttable (HelpLine);
}

void SelectFastSearchHelp (void)
{
	int LastHelpLine;
	char *paDisplayText[] = {
		"   Select Search Speed Help" ,
		"" ,
		"Slow search:" ,
		"  Scan all memory to find" ,
		"  cheats. Large amount of" ,
		"  memory might be needed." ,
		"" ,
		"Normal search:" ,
		"  Scan all memory areas" ,
		"  which are labelled RAM" ,
		"  or BANK1 to BANK8." ,
		"" ,
		"Fastest search:" ,
		"  Scan the useful memory" ,
		"  area. Used to scan NEOGEO" ,
		"  games and the ones with" ,
		"  TM34010 CPU(s)." ,
		"" ,
		"Select Memory Area:" ,
		"  Scan the memory areas" ,
		"  selected by the user." ,
		"" ,
		"Help (F10):" ,
		"  Display this help" ,
		0 };

	LastHelpLine = CreateHelp(paDisplayText, HelpLine);
	ShowHelp(LastHelpLine, HelpLine);
	reset_texttable (HelpLine);
}

void DisplayHelpFile(void)
{
  int LastHelpLine;

  LastHelpLine = LoadHelp(helpfile, HelpLine);
  ShowHelp(LastHelpLine, HelpLine);
  reset_texttable (HelpLine);
}
