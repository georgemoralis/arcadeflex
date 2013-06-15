/*
 * 
 * 
 */
package arcadeflex;
import static arcadeflex.osdepend.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static arcadeflex.video.*;

public class config {

    /*these are used for ini reading*/
    private static Pattern _section = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");
    private static Pattern _keyValue = Pattern.compile("\\s*([^=]*)=(.*)");
    private static Map< String, Map< String, String>> _entries = new HashMap<>();
    /*endof these are used for ini reading*/

    //code from : http://stackoverflow.com/questions/190629/what-is-the-easiest-way-to-parse-an-ini-file-in-java
    public static void load_ini(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            String section = null;
            while ((line = br.readLine()) != null) {
                Matcher m = _section.matcher(line);
                if (m.matches()) {
                    section = m.group(1).trim();
                } else if (section != null) {
                    m = _keyValue.matcher(line);
                    if (m.matches()) {
                        String key = m.group(1).trim();
                        String value = m.group(2).trim();
                        Map< String, String> kv = _entries.get(section);
                        if (kv == null) {
                            _entries.put(section, kv = new HashMap<>());
                        }
                        kv.put(key, value);
                    }
                }
            }
        }
    }
    /*TODO*////* from main() */
    /*TODO*///extern int ignorecfg;
    /*TODO*///
    /*TODO*////* from video.c */
    /*TODO*///extern int frameskip,autoframeskip;
    /*TODO*///extern int scanlines, use_tweaked, video_sync, wait_vsync, use_triplebuf;
    /*TODO*///extern int stretch, use_mmx, use_dirty;
    /*TODO*///extern int vgafreq, always_synced, skiplines, skipcolumns;
    /*TODO*///extern float osd_gamma_correction;
    /*TODO*///extern int gfx_mode, gfx_width, gfx_height;
    /*TODO*///
    /*TODO*///extern int monitor_type;
    /*TODO*///
    /*TODO*///extern unsigned char tw224x288_h, tw224x288_v;
    /*TODO*///extern unsigned char tw240x256_h, tw240x256_v;
    /*TODO*///extern unsigned char tw256x240_h, tw256x240_v;
    /*TODO*///extern unsigned char tw256x256_h, tw256x256_v;
    /*TODO*///extern unsigned char tw256x256_hor_h, tw256x256_hor_v;
    /*TODO*///extern unsigned char tw288x224_h, tw288x224_v;
    /*TODO*///extern unsigned char tw240x320_h, tw240x320_v;
    /*TODO*///extern unsigned char tw320x240_h, tw320x240_v;
    /*TODO*///extern unsigned char tw336x240_h, tw336x240_v;
    /*TODO*///extern unsigned char tw384x224_h, tw384x224_v;
    /*TODO*///extern unsigned char tw384x240_h, tw384x240_v;
    /*TODO*///extern unsigned char tw384x256_h, tw384x256_v;
    /*TODO*///extern unsigned char tw400x256_h, tw400x256_v;
    /*TODO*///
    /*TODO*///
    /*TODO*////* Tweak values for 15.75KHz arcade/ntsc/pal modes */
    /*TODO*////* from video.c */
    /*TODO*///extern unsigned char tw224x288arc_h, tw224x288arc_v, tw288x224arc_h, tw288x224arc_v;
    /*TODO*///extern unsigned char tw256x256arc_h, tw256x256arc_v, tw256x240arc_h, tw256x240arc_v;
    /*TODO*///extern unsigned char tw320x240arc_h, tw320x240arc_v, tw320x256arc_h, tw320x256arc_v;
    /*TODO*///extern unsigned char tw352x240arc_h, tw352x240arc_v, tw352x256arc_h, tw352x256arc_v;
    /*TODO*///extern unsigned char tw368x224arc_h, tw368x224arc_v;
    /*TODO*///extern unsigned char tw368x240arc_h, tw368x240arc_v, tw368x256arc_h, tw368x256arc_v;
    /*TODO*///extern unsigned char tw512x224arc_h, tw512x224arc_v, tw512x256arc_h, tw512x256arc_v;
    /*TODO*///extern unsigned char tw512x448arc_h, tw512x448arc_v, tw512x512arc_h, tw512x512arc_v;
    /*TODO*///extern unsigned char tw640x480arc_h, tw640x480arc_v;
    /*TODO*///
    /*TODO*///
    /*TODO*////* from sound.c */
    /*TODO*///extern int soundcard, usestereo,attenuation;
    /*TODO*///
    /*TODO*////* from input.c */
    /*TODO*///extern int use_mouse, joystick, use_hotrod;
    /*TODO*///
    /*TODO*////* from cheat.c */
    /*TODO*///extern char *cheatfile;
    /*TODO*///
    /*TODO*////* from datafile.c */
    /*TODO*///extern char *history_filename,*mameinfo_filename;
    /*TODO*///
    /*TODO*////* from fileio.c */
    /*TODO*///void decompose_rom_sample_path (char *rompath, char *samplepath);
    /*TODO*///extern char *nvdir, *hidir, *cfgdir, *inpdir, *stadir, *memcarddir;
    /*TODO*///extern char *artworkdir, *screenshotdir, *alternate_name;
    /*TODO*///
    /*TODO*///#ifdef MESS
    /*TODO*////* path to the CRC database files */
    /*TODO*///char *crcdir;
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* from video.c, for centering tweaked modes */
    /*TODO*///extern int center_x;
    /*TODO*///extern int center_y;
    /*TODO*///
    /*TODO*////*from video.c flag for 15.75KHz modes (req. for 15.75KHz Arcade Monitor Modes)*/
    /*TODO*///extern int arcade_mode;
    /*TODO*///
    /*TODO*////*from svga15kh.c flag to allow delay for odd/even fields for interlaced displays (req. for 15.75KHz Arcade Monitor Modes)*/
    /*TODO*///extern int wait_interlace;
    /*TODO*///
    /*TODO*///static int mame_argc;
    /*TODO*///static char **mame_argv;
    /*TODO*///static int game;
    /*TODO*///char *rompath, *samplepath;
    /*TODO*///
    /*TODO*///struct { char *name; int id; } joy_table[] =
    /*TODO*///{
    /*TODO*///	{ "none",               JOY_TYPE_NONE },
    /*TODO*///	{ "auto",               JOY_TYPE_AUTODETECT },
    /*TODO*///	{ "standard",           JOY_TYPE_STANDARD },
    /*TODO*///	{ "dual",               JOY_TYPE_2PADS },
    /*TODO*///	{ "4button",            JOY_TYPE_4BUTTON },
    /*TODO*///	{ "6button",            JOY_TYPE_6BUTTON },
    /*TODO*///	{ "8button",            JOY_TYPE_8BUTTON },
    /*TODO*///	{ "fspro",              JOY_TYPE_FSPRO },
    /*TODO*///	{ "wingex",             JOY_TYPE_WINGEX },
    /*TODO*///	{ "sidewinder",         JOY_TYPE_SIDEWINDER },
    /*TODO*///	{ "gamepadpro",         JOY_TYPE_GAMEPAD_PRO },
    /*TODO*///	{ "grip",               JOY_TYPE_GRIP },
    /*TODO*///	{ "grip4",              JOY_TYPE_GRIP4 },
    /*TODO*///	{ "sneslpt1",           JOY_TYPE_SNESPAD_LPT1 },
    /*TODO*///	{ "sneslpt2",           JOY_TYPE_SNESPAD_LPT2 },
    /*TODO*///	{ "sneslpt3",           JOY_TYPE_SNESPAD_LPT3 },
    /*TODO*///	{ "psxlpt1",            JOY_TYPE_PSXPAD_LPT1 },
    /*TODO*///	{ "psxlpt2",            JOY_TYPE_PSXPAD_LPT2 },
    /*TODO*///	{ "psxlpt3",            JOY_TYPE_PSXPAD_LPT3 },
    /*TODO*///	{ "n64lpt1",            JOY_TYPE_N64PAD_LPT1 },
    /*TODO*///	{ "n64lpt2",            JOY_TYPE_N64PAD_LPT2 },
    /*TODO*///	{ "n64lpt3",            JOY_TYPE_N64PAD_LPT3 },
    /*TODO*///	{ "wingwarrior",        JOY_TYPE_WINGWARRIOR },
    /*TODO*///	{ "segaisa",            JOY_TYPE_IFSEGA_ISA },
    /*TODO*///	{ "segapci",            JOY_TYPE_IFSEGA_PCI },
    /*TODO*///	{ 0, 0 }
    /*TODO*///} ;
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*////* monitor type */
    /*TODO*///struct { char *name; int id; } monitor_table[] =
    /*TODO*///{
    /*TODO*///	{ "standard",   MONITOR_TYPE_STANDARD},
    /*TODO*///	{ "ntsc",       MONITOR_TYPE_NTSC},
    /*TODO*///	{ "pal",                MONITOR_TYPE_PAL},
    /*TODO*///	{ "arcade",             MONITOR_TYPE_ARCADE},
    /*TODO*///	{ NULL, NULL }
    /*TODO*///} ;
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// * gets some boolean config value.
    /*TODO*/// * 0 = false, >0 = true, <0 = auto
    /*TODO*/// * the shortcut can only be used on the commandline
    /*TODO*/// */
    /*TODO*///static int get_bool (char *section, char *option, char *shortcut, int def)
    /*TODO*///{
    /*TODO*///	char *yesnoauto;
    /*TODO*///	int res, i;
    /*TODO*///
    /*TODO*///	res = def;
    /*TODO*///
    /*TODO*///	if (ignorecfg) goto cmdline;
    /*TODO*///
    /*TODO*///	/* look into mame.cfg, [section] */
    /*TODO*///	if (def == 0)
    /*TODO*///		yesnoauto = get_config_string(section, option, "no");
    /*TODO*///	else if (def > 0)
    /*TODO*///		yesnoauto = get_config_string(section, option, "yes");
    /*TODO*///	else /* def < 0 */
    /*TODO*///		yesnoauto = get_config_string(section, option, "auto");
    /*TODO*///
    /*TODO*///	/* if the option doesn't exist in the cfgfile, create it */
    /*TODO*///	if (get_config_string(section, option, "#") == "#")
    /*TODO*///		set_config_string(section, option, yesnoauto);
    /*TODO*///
    /*TODO*///	/* look into mame.cfg, [gamename] */
    /*TODO*///	yesnoauto = get_config_string((char *)drivers[game]->name, option, yesnoauto);
    /*TODO*///
    /*TODO*///	/* also take numerical values instead of "yes", "no" and "auto" */
    /*TODO*///	if      (stricmp(yesnoauto, "no"  ) == 0) res = 0;
    /*TODO*///	else if (stricmp(yesnoauto, "yes" ) == 0) res = 1;
    /*TODO*///	else if (stricmp(yesnoauto, "auto") == 0) res = -1;
    /*TODO*///	else    res = atoi (yesnoauto);
    /*TODO*///
    /*TODO*///cmdline:
    /*TODO*///	/* check the commandline */
    /*TODO*///	for (i = 1; i < mame_argc; i++)
    /*TODO*///	{
    /*TODO*///		if (mame_argv[i][0] != '-') continue;
    /*TODO*///		/* look for "-option" */
    /*TODO*///		if (stricmp(&mame_argv[i][1], option) == 0)
    /*TODO*///			res = 1;
    /*TODO*///		/* look for "-shortcut" */
    /*TODO*///		if (shortcut && (stricmp(&mame_argv[i][1], shortcut) == 0))
    /*TODO*///			res = 1;
    /*TODO*///		/* look for "-nooption" */
    /*TODO*///		if (strnicmp(&mame_argv[i][1], "no", 2) == 0)
    /*TODO*///		{
    /*TODO*///			if (stricmp(&mame_argv[i][3], option) == 0)
    /*TODO*///				res = 0;
    /*TODO*///			if (shortcut && (stricmp(&mame_argv[i][3], shortcut) == 0))
    /*TODO*///				res = 0;
    /*TODO*///		}
    /*TODO*///		/* look for "-autooption" */
    /*TODO*///		if (strnicmp(&mame_argv[i][1], "auto", 4) == 0)
    /*TODO*///		{
    /*TODO*///			if (stricmp(&mame_argv[i][5], option) == 0)
    /*TODO*///				res = -1;
    /*TODO*///			if (shortcut && (stricmp(&mame_argv[i][5], shortcut) == 0))
    /*TODO*///				res = -1;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	return res;
    /*TODO*///}
    public static int get_int(String section, String option, String shortcut, int def) 
    {
        Map< String, String> kv = _entries.get(section);
    	int res,i;
    
    	res = def;
    
    	if (ignorecfg==0)
    	{
    /*TODO*///		/* if the option does not exist, create it */
    /*TODO*///		if (get_config_int (section, option, -777) == -777)
    /*TODO*///			set_config_int (section, option, def);
    /*TODO*///
    		/* look into mame.cfg, [section] */
                if( kv != null ) res = Integer.parseInt( kv.get( option ));
    /*TODO*///
    /*TODO*///		/* look into mame.cfg, [gamename] */
    /*TODO*///		res = get_config_int ((char *)drivers[game]->name, option, res);
    	}
    /*TODO*///
    /*TODO*///	/* get it from the commandline */
    /*TODO*///	for (i = 1; i < mame_argc; i++)
    /*TODO*///	{
    /*TODO*///		if (mame_argv[i][0] != '-')
    /*TODO*///			continue;
    /*TODO*///		if ((stricmp(&mame_argv[i][1], option) == 0) ||
    /*TODO*///			(shortcut && (stricmp(&mame_argv[i][1], shortcut ) == 0)))
    /*TODO*///		{
    /*TODO*///			i++;
    /*TODO*///			if (i < mame_argc) res = atoi (mame_argv[i]);
    /*TODO*///		}
    /*TODO*///	}
    	return res;
    }

    /*TODO*///static float get_float (char *section, char *option, char *shortcut, float def)
    /*TODO*///{
    /*TODO*///	int i;
    /*TODO*///	float res;
    /*TODO*///
    /*TODO*///	res = def;
    /*TODO*///
    /*TODO*///	if (!ignorecfg)
    /*TODO*///	{
    /*TODO*///		/* if the option does not exist, create it */
    /*TODO*///		if (get_config_float (section, option, 9999.0) == 9999.0)
    /*TODO*///			set_config_float (section, option, def);
    /*TODO*///
    /*TODO*///		/* look into mame.cfg, [section] */
    /*TODO*///		res = get_config_float (section, option, def);
    /*TODO*///
    /*TODO*///		/* look into mame.cfg, [gamename] */
    /*TODO*///		res = get_config_float ((char *)drivers[game]->name, option, res);
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* get it from the commandline */
    /*TODO*///	for (i = 1; i < mame_argc; i++)
    /*TODO*///	{
    /*TODO*///		if (mame_argv[i][0] != '-')
    /*TODO*///			continue;
    /*TODO*///		if ((stricmp(&mame_argv[i][1], option) == 0) ||
    /*TODO*///			(shortcut && (stricmp(&mame_argv[i][1], shortcut ) == 0)))
    /*TODO*///		{
    /*TODO*///			i++;
    /*TODO*///			if (i < mame_argc) res = atof (mame_argv[i]);
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	return res;
    /*TODO*///}
    /*TODO*///

    public static String get_string(String section, String option, String shortcut, String def) {
        Map< String, String> kv = _entries.get(section);
        String res=def;
        
    	if (ignorecfg==0)
    	{
    /*TODO*///		/* if the option does not exist, create it */
    /*TODO*///		if (get_config_string (section, option, "#") == "#" )
    /*TODO*///			set_config_string (section, option, def);
    /*TODO*///
    		/* look into mame.cfg, [section] */
                if( kv != null ) res = kv.get( option );
    /*TODO*///
    /*TODO*///		/* look into mame.cfg, [gamename] */
    /*TODO*///		res = get_config_string((char*)drivers[game]->name, option, res);
    	}
    /*TODO*///
    /*TODO*///	/* get it from the commandline */
    /*TODO*///	for (i = 1; i < mame_argc; i++)
    /*TODO*///	{
    /*TODO*///		if (mame_argv[i][0] != '-')
    /*TODO*///			continue;
    /*TODO*///
    /*TODO*///		if ((stricmp(&mame_argv[i][1], option) == 0) ||
    /*TODO*///			(shortcut && (stricmp(&mame_argv[i][1], shortcut)  == 0)))
    /*TODO*///		{
    /*TODO*///			i++;
    /*TODO*///			if (i < mame_argc) res = mame_argv[i];
    /*TODO*///		}
    /*TODO*///	}
    	return res;
    }
    /*TODO*///
    /*TODO*///void get_rom_sample_path (int argc, char **argv, int game_index)
    /*TODO*///{
    /*TODO*///	int i;
    /*TODO*///
    /*TODO*///	alternate_name = 0;
    /*TODO*///	mame_argc = argc;
    /*TODO*///	mame_argv = argv;
    /*TODO*///	game = game_index;
    /*TODO*///
    /*TODO*///	rompath    = get_string ("directory", "rompath",    NULL, ".;ROMS");
    /*TODO*///	samplepath = get_string ("directory", "samplepath", NULL, ".;SAMPLES");
    /*TODO*///
    /*TODO*///	/* handle '-romdir' hack. We should get rid of this BW */
    /*TODO*///	alternate_name = 0;
    /*TODO*///	for (i = 1; i < argc; i++)
    /*TODO*///	{
    /*TODO*///		if (stricmp (argv[i], "-romdir") == 0)
    /*TODO*///		{
    /*TODO*///			i++;
    /*TODO*///			if (i < argc) alternate_name = argv[i];
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* decompose paths into components (handled by fileio.c) */
    /*TODO*///	decompose_rom_sample_path (rompath, samplepath);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* for playback of .inp files */
    /*TODO*///void init_inpdir(void)
    /*TODO*///{
    /*TODO*///    inpdir = get_string ("directory", "inp",     NULL, "INP");
    /*TODO*///}
    /*TODO*///

    public static void parse_cmdline(int argc, String[] argv, int game_index) {
        try {
            load_ini("arcadeflex.cfg");
        } catch (IOException ex) {
            //try create a new file if not found
            File config = new File("arcadeflex.cfg");
            try {
                config.createNewFile();
                //and reload the file
                load_ini("arcadeflex.cfg");
            } catch (IOException ex1) {
                System.out.println("Can't create an configuration file!");
            }
        }
        /*TODO*///	static float f_beam, f_flicker;
            /*TODO*///	char *resolution;
            /*TODO*///	char *vesamode;
            /*TODO*///	char *joyname;
            /*TODO*///	char tmpres[10];
            /*TODO*///	int i;
            String tmpstr;
            /*TODO*///	char *monitorname;
            /*TODO*///
            /*TODO*///	mame_argc = argc;
            /*TODO*///	mame_argv = argv;
            /*TODO*///	game = game_index;
            /*TODO*///
            /*TODO*///
            /*TODO*///	/* force third mouse button emulation to "no" otherwise Allegro will default to "yes" */
            /*TODO*///	set_config_string(0,"emulate_three","no");
            /*TODO*///
            /*TODO*///	/* read graphic configuration */
            /*TODO*///	scanlines   = get_bool   ("config", "scanlines",    NULL,  1);
  /*temphack!!!! */              scanlines = 1;
  /*temphack!!!! */              stretch =1;
            /*TODO*///	stretch     = get_bool   ("config", "stretch",		NULL,  1);
            /*TODO*///	options.use_artwork = get_bool   ("config", "artwork",	NULL,  1);
            /*TODO*///	options.use_samples = get_bool   ("config", "samples",	NULL,  1);
            /*TODO*///	video_sync  = get_bool   ("config", "vsync",        NULL,  0);
            /*TODO*///	wait_vsync  = get_bool   ("config", "waitvsync",    NULL,  0);
            /*TODO*///	use_triplebuf = get_bool ("config", "triplebuffer",	NULL,  0);
            /*TODO*///	use_tweaked = get_bool   ("config", "tweak",		NULL,  0);
            /*TODO*///	vesamode    = get_string ("config", "vesamode",	NULL,	"vesa3");
            /*TODO*///	use_mmx		= get_bool   ("config", "mmx", 		NULL,	-1);
            /*TODO*///	use_dirty	= get_bool	 ("config", "dirty",	NULL,	-1);
 /*temphack!!!! */           use_dirty=-1;
            /*TODO*///	options.antialias   = get_bool   ("config", "antialias",    NULL,  1);
            /*TODO*///	options.translucency = get_bool    ("config", "translucency", NULL, 1);
            /*TODO*///
            /*TODO*///	vgafreq     = get_int    ("config", "vgafreq",      NULL,  -1);
            /*TODO*///	always_synced = get_bool ("config", "alwayssynced", NULL, 0);
            /*TODO*///
            /*TODO*///	tmpstr             = get_string ("config", "depth", NULL, "auto");
            /*TODO*///	options.color_depth = atoi(tmpstr);
            /*TODO*///	if (options.color_depth != 8 && options.color_depth != 16) options.color_depth = 0;	/* auto */
            /*TODO*///
            /*TODO*///	skiplines   = get_int    ("config", "skiplines",    NULL, 0);
            /*TODO*///	skipcolumns = get_int    ("config", "skipcolumns",  NULL, 0);
            /*TODO*///	f_beam      = get_float  ("config", "beam",         NULL, 1.0);
            /*TODO*///	if (f_beam < 1.0) f_beam = 1.0;
            /*TODO*///	if (f_beam > 16.0) f_beam = 16.0;
            /*TODO*///	f_flicker   = get_float  ("config", "flicker",      NULL, 0.0);
            /*TODO*///	if (f_flicker < 0.0) f_flicker = 0.0;
            /*TODO*///	if (f_flicker > 100.0) f_flicker = 100.0;
            /*TODO*///	osd_gamma_correction = get_float ("config", "gamma",   NULL, 1.0);
            /*TODO*///	if (osd_gamma_correction < 0.5) osd_gamma_correction = 0.5;
            /*TODO*///	if (osd_gamma_correction > 2.0) osd_gamma_correction = 2.0;
            /*TODO*///
            	tmpstr             = get_string ("config", "frameskip", "fs", "auto");
            	if (tmpstr.compareTo("auto")==0)//(!stricmp(tmpstr,"auto"))
            	{
            		frameskip = 0;
            		autoframeskip = 1;
            	}
            /*TODO*///	else
            /*TODO*///	{
            /*TODO*///		frameskip = atoi(tmpstr);
            /*TODO*///		autoframeskip = 0;
            /*TODO*///	}
            /*TODO*///	options.norotate  = get_bool ("config", "norotate",  NULL, 0);
            /*TODO*///	options.ror       = get_bool ("config", "ror",       NULL, 0);
            /*TODO*///	options.rol       = get_bool ("config", "rol",       NULL, 0);
            /*TODO*///	options.flipx     = get_bool ("config", "flipx",     NULL, 0);
            /*TODO*///	options.flipy     = get_bool ("config", "flipy",     NULL, 0);
            /*TODO*///
            /*TODO*///	/* read sound configuration */
            /*TODO*///	soundcard           = get_int  ("config", "soundcard",  NULL, -1);
            /*TODO*///	options.use_emulated_ym3812 = !get_bool ("config", "ym3812opl",  NULL,  0);
            /*TODO*///	options.samplerate = get_int  ("config", "samplerate", "sr", 22050);
            /*TODO*///	if (options.samplerate < 5000) options.samplerate = 5000;
            /*TODO*///	if (options.samplerate > 50000) options.samplerate = 50000;
            /*TODO*///	usestereo           = get_bool ("config", "stereo",  NULL,  1);
            /*TODO*///	attenuation         = get_int  ("config", "volume",  NULL,  0);
            /*TODO*///	if (attenuation < -32) attenuation = -32;
            /*TODO*///	if (attenuation > 0) attenuation = 0;
            /*TODO*///
            /*TODO*///	/* read input configuration */
            /*TODO*///	use_mouse = get_bool   ("config", "mouse",   NULL,  1);
            /*TODO*///	joyname   = get_string ("config", "joystick", "joy", "none");
            /*TODO*///	use_hotrod = 0;
            /*TODO*///	if (get_bool  ("config", "hotrod",   NULL,  0)) use_hotrod = 1;
            /*TODO*///	if (get_bool  ("config", "hotrodse",   NULL,  0)) use_hotrod = 2;
            /*TODO*///
            /*TODO*///	/* misc configuration */
            /*TODO*///	options.cheat      = get_bool ("config", "cheat", NULL, 0);
            /*TODO*///	options.mame_debug = get_bool ("config", "debug", NULL, 0);
            /*TODO*///	cheatfile  = get_string ("config", "cheatfile", "cf", "CHEAT.DAT");    /* JCK 980917 */
            /*TODO*///
            /*TODO*/// 	#ifndef MESS
            /*TODO*/// 	history_filename  = get_string ("config", "historyfile", NULL, "HISTORY.DAT");    /* JCK 980917 */
            /*TODO*/// 	#else
            /*TODO*/// 	history_filename  = get_string ("config", "historyfile", NULL, "SYSINFO.DAT");
            /*TODO*/// 	#endif
            /*TODO*///
            /*TODO*///	mameinfo_filename  = get_string ("config", "mameinfofile", NULL, "MAMEINFO.DAT");    /* JCK 980917 */
            /*TODO*///
            /*TODO*///	/* get resolution */
            /*TODO*///	resolution  = get_string ("config", "resolution", NULL, "auto");
            /*TODO*///
            /*TODO*///	/* set default subdirectories */
            /*TODO*///	nvdir      = get_string ("directory", "nvram",   NULL, "NVRAM");
            /*TODO*///	hidir      = get_string ("directory", "hi",      NULL, "HI");
            /*TODO*///	cfgdir     = get_string ("directory", "cfg",     NULL, "CFG");
            /*TODO*///	screenshotdir = get_string ("directory", "snap",     NULL, "SNAP");
            /*TODO*///	memcarddir = get_string ("directory", "memcard", NULL, "MEMCARD");
            /*TODO*///	stadir     = get_string ("directory", "sta",     NULL, "STA");
            /*TODO*///	artworkdir = get_string ("directory", "artwork", NULL, "ARTWORK");
            /*TODO*/// 	#ifdef MESS
            /*TODO*/// 		crcdir = get_string ("directory", "crc", NULL, "CRC");
            /*TODO*/// 	#endif
            /*TODO*///
            /*TODO*///	/* get tweaked modes info */
            /*TODO*///	tw224x288_h			= get_int ("tweaked", "224x288_h",              NULL, 0x5f);
            /*TODO*///	tw224x288_v     	= get_int ("tweaked", "224x288_v",              NULL, 0x54);
            /*TODO*///	tw240x256_h     = get_int ("tweaked", "240x256_h",              NULL, 0x67);
            /*TODO*///	tw240x256_v     = get_int ("tweaked", "240x256_v",              NULL, 0x23);
            /*TODO*///	tw256x240_h     = get_int ("tweaked", "256x240_h",              NULL, 0x55);
            /*TODO*///	tw256x240_v     = get_int ("tweaked", "256x240_v",              NULL, 0x43);
            /*TODO*///	tw256x256_h     = get_int ("tweaked", "256x256_h",              NULL, 0x6c);
            /*TODO*///	tw256x256_v     = get_int ("tweaked", "256x256_v",              NULL, 0x23);
            /*TODO*///	tw256x256_hor_h = get_int ("tweaked", "256x256_hor_h",  NULL, 0x55);
            /*TODO*///	tw256x256_hor_v = get_int ("tweaked", "256x256_hor_v",  NULL, 0x60);
            /*TODO*///	tw288x224_h     = get_int ("tweaked", "288x224_h",              NULL, 0x5f);
            /*TODO*///	tw288x224_v     = get_int ("tweaked", "288x224_v",              NULL, 0x0c);
            /*TODO*///	tw240x320_h             = get_int ("tweaked", "240x320_h",              NULL, 0x5a);
            /*TODO*///	tw240x320_v             = get_int ("tweaked", "240x320_v",              NULL, 0x8c);
            /*TODO*///	tw320x240_h             = get_int ("tweaked", "320x240_h",              NULL, 0x5f);
            /*TODO*///	tw320x240_v             = get_int ("tweaked", "320x240_v",              NULL, 0x0c);
            /*TODO*///	tw336x240_h             = get_int ("tweaked", "336x240_h",              NULL, 0x5f);
            /*TODO*///	tw336x240_v             = get_int ("tweaked", "336x240_v",              NULL, 0x0c);
            /*TODO*///	tw384x224_h             = get_int ("tweaked", "384x224_h",              NULL, 0x6c);
            /*TODO*///	tw384x224_v             = get_int ("tweaked", "384x224_v",              NULL, 0x0c);
            /*TODO*///	tw384x240_h             = get_int ("tweaked", "384x240_h",              NULL, 0x6c);
            /*TODO*///	tw384x240_v             = get_int ("tweaked", "384x240_v",              NULL, 0x0c);
            /*TODO*///	tw384x256_h             = get_int ("tweaked", "384x256_h",              NULL, 0x6c);
            /*TODO*///	tw384x256_v             = get_int ("tweaked", "384x256_v",              NULL, 0x23);
            /*TODO*///
            /*TODO*///	/* Get 15.75KHz tweak values */
            /*TODO*///	tw224x288arc_h          = get_int ("tweaked", "224x288arc_h",   NULL, 0x5d);
            /*TODO*///	tw224x288arc_v          = get_int ("tweaked", "224x288arc_v",   NULL, 0x38);
            /*TODO*///	tw288x224arc_h          = get_int ("tweaked", "288x224arc_h",   NULL, 0x5d);
            /*TODO*///	tw288x224arc_v          = get_int ("tweaked", "288x224arc_v",   NULL, 0x09);
            /*TODO*///	tw256x240arc_h          = get_int ("tweaked", "256x240arc_h",   NULL, 0x5d);
            /*TODO*///	tw256x240arc_v          = get_int ("tweaked", "256x240arc_v",   NULL, 0x09);
            /*TODO*///	tw256x256arc_h          = get_int ("tweaked", "256x256arc_h",   NULL, 0x5d);
            /*TODO*///	tw256x256arc_v          = get_int ("tweaked", "256x256arc_v",   NULL, 0x17);
            /*TODO*///	tw320x240arc_h          = get_int ("tweaked", "320x240arc_h",   NULL, 0x69);
            /*TODO*///	tw320x240arc_v          = get_int ("tweaked", "320x240arc_v",   NULL, 0x09);
            /*TODO*///	tw320x256arc_h          = get_int ("tweaked", "320x256arc_h",   NULL, 0x69);
            /*TODO*///	tw320x256arc_v          = get_int ("tweaked", "320x256arc_v",   NULL, 0x17);
            /*TODO*///	tw352x240arc_h          = get_int ("tweaked", "352x240arc_h",   NULL, 0x6a);
            /*TODO*///	tw352x240arc_v          = get_int ("tweaked", "352x240arc_v",   NULL, 0x09);
            /*TODO*///	tw352x256arc_h          = get_int ("tweaked", "352x256arc_h",   NULL, 0x6a);
            /*TODO*///	tw352x256arc_v          = get_int ("tweaked", "352x256arc_v",   NULL, 0x17);
            /*TODO*///	tw368x224arc_h          = get_int ("tweaked", "368x224arc_h",   NULL, 0x6a);
            /*TODO*///	tw368x224arc_v          = get_int ("tweaked", "368x224arc_v",   NULL, 0x09);
            /*TODO*///	tw368x240arc_h          = get_int ("tweaked", "368x240arc_h",   NULL, 0x6a);
            /*TODO*///	tw368x240arc_v          = get_int ("tweaked", "368x240arc_v",   NULL, 0x09);
            /*TODO*///	tw368x256arc_h          = get_int ("tweaked", "368x256arc_h",   NULL, 0x6a);
            /*TODO*///	tw368x256arc_v          = get_int ("tweaked", "368x256arc_v",   NULL, 0x17);
            /*TODO*///	tw512x224arc_h          = get_int ("tweaked", "512x224arc_h",   NULL, 0xbf);
            /*TODO*///	tw512x224arc_v          = get_int ("tweaked", "512x224arc_v",   NULL, 0x09);
            /*TODO*///	tw512x256arc_h          = get_int ("tweaked", "512x256arc_h",   NULL, 0xbf);
            /*TODO*///	tw512x256arc_v          = get_int ("tweaked", "512x256arc_v",   NULL, 0x17);
            /*TODO*///	tw512x448arc_h          = get_int ("tweaked", "512x448arc_h",   NULL, 0xbf);
            /*TODO*///	tw512x448arc_v          = get_int ("tweaked", "512x448arc_v",   NULL, 0x09);
            /*TODO*///	tw512x512arc_h          = get_int ("tweaked", "512x512arc_h",   NULL, 0xbf);
            /*TODO*///	tw512x512arc_v          = get_int ("tweaked", "512x512arc_v",   NULL, 0x17);
            /*TODO*///	tw640x480arc_h          = get_int ("tweaked", "640x480arc_h",   NULL, 0xc1);
            /*TODO*///	tw640x480arc_v          = get_int ("tweaked", "640x480arc_v",   NULL, 0x09);
            /*TODO*///
            /*TODO*///	/* this is handled externally cause the audit stuff needs it, too */
            /*TODO*///	get_rom_sample_path (argc, argv, game_index);
            /*TODO*///
            /*TODO*///	/* get the monitor type */
            /*TODO*///	monitorname = get_string ("config", "monitor", NULL, "standard");
            /*TODO*///	/* get -centerx */
            /*TODO*///	center_x = get_int ("config", "centerx", NULL,  0);
            /*TODO*///	/* get -centery */
            /*TODO*///	center_y = get_int ("config", "centery", NULL,  0);
            /*TODO*///	/* get -waitinterlace */
            /*TODO*///	wait_interlace = get_bool ("config", "waitinterlace", NULL,  0);
            /*TODO*///
            /*TODO*///	/* process some parameters */
            /*TODO*///	options.beam = (int)(f_beam * 0x00010000);
            /*TODO*///	if (options.beam < 0x00010000)
            /*TODO*///		options.beam = 0x00010000;
            /*TODO*///	if (options.beam > 0x00100000)
            /*TODO*///		options.beam = 0x00100000;
            /*TODO*///
            /*TODO*///	options.flicker = (int)(f_flicker * 2.55);
            /*TODO*///	if (options.flicker < 0)
            /*TODO*///		options.flicker = 0;
            /*TODO*///	if (options.flicker > 255)
            /*TODO*///		options.flicker = 255;
            /*TODO*///
            /*TODO*///	if (stricmp (vesamode, "vesa1") == 0)
            /*TODO*///		gfx_mode = GFX_VESA1;
            /*TODO*///	else if (stricmp (vesamode, "vesa2b") == 0)
            /*TODO*///		gfx_mode = GFX_VESA2B;
            /*TODO*///	else if (stricmp (vesamode, "vesa2l") == 0)
            /*TODO*///		gfx_mode = GFX_VESA2L;
            /*TODO*///	else if (stricmp (vesamode, "vesa3") == 0)
            /*TODO*///		gfx_mode = GFX_VESA3;
            /*TODO*///	else
            /*TODO*///	{
            /*TODO*///		if (errorlog)
            /*TODO*///			fprintf (errorlog, "%s is not a valid entry for vesamode\n",
            /*TODO*///					vesamode);
            /*TODO*///		gfx_mode = GFX_VESA3; /* default to VESA2L */
            /*TODO*///	}
            /*TODO*///
            /*TODO*///	/* any option that starts with a digit is taken as a resolution option */
            /*TODO*///	/* this is to handle the old "-wxh" commandline option. */
            /*TODO*///	for (i = 1; i < argc; i++)
            /*TODO*///	{
            /*TODO*///		if (argv[i][0] == '-' && isdigit(argv[i][1]) &&
            /*TODO*///				(strstr(argv[i],"x") || strstr(argv[i],"X")))
            /*TODO*///			resolution = &argv[i][1];
            /*TODO*///	}
            /*TODO*///
            	/* break up resolution into gfx_width and gfx_height */
            	gfx_height = gfx_width = 0;
            /*TODO*///	if (stricmp (resolution, "auto") != 0)
            /*TODO*///	{
            /*TODO*///		char *tmp;
            /*TODO*///		strncpy (tmpres, resolution, 10);
            /*TODO*///		tmp = strtok (tmpres, "xX");
            /*TODO*///		gfx_width = atoi (tmp);
            /*TODO*///		tmp = strtok (0, "xX");
            /*TODO*///		if (tmp)
            /*TODO*///			gfx_height = atoi (tmp);
            /*TODO*///	}
            /*TODO*///
            /*TODO*///	/* convert joystick name into an Allegro-compliant joystick signature */
            /*TODO*///	joystick = -2; /* default to invalid value */
            /*TODO*///
            /*TODO*///	for (i = 0; joy_table[i].name != NULL; i++)
            /*TODO*///	{
            /*TODO*///		if (stricmp (joy_table[i].name, joyname) == 0)
            /*TODO*///		{
            /*TODO*///			joystick = joy_table[i].id;
            /*TODO*///			if (errorlog)
            /*TODO*///				fprintf (errorlog, "using joystick %s = %08x\n",
            /*TODO*///						joyname,joy_table[i].id);
            /*TODO*///			break;
            /*TODO*///		}
            /*TODO*///	}
            /*TODO*///
            /*TODO*///	if (joystick == -2)
            /*TODO*///	{
            /*TODO*///		if (errorlog)
            /*TODO*///			fprintf (errorlog, "%s is not a valid entry for a joystick\n",
            /*TODO*///					joyname);
            /*TODO*///		joystick = JOY_TYPE_NONE;
            /*TODO*///	}
            /*TODO*///
            /*TODO*///	/* get monitor type from supplied name */
            /*TODO*///	monitor_type = MONITOR_TYPE_STANDARD; /* default to PC monitor */
            /*TODO*///
            /*TODO*///	for (i = 0; monitor_table[i].name != NULL; i++)
            /*TODO*///	{
            /*TODO*///		if ((stricmp (monitor_table[i].name, monitorname) == 0))
            /*TODO*///		{
            /*TODO*///			monitor_type = monitor_table[i].id;
            /*TODO*///			break;
            /*TODO*///		}
            /*TODO*///	}


    }
    /*TODO*///
    /*TODO*///
}
