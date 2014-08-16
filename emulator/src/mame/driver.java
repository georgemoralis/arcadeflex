/*
This file is part of Arcadeflex.

Arcadeflex is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Arcadeflex is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Arcadeflex.  If not, see <http://www.gnu.org/licenses/>.
 */
package mame;

import static mame.driverH.*;

//drivers imports
import static drivers.pacman.*;
import static drivers.minivadr.*;
import static drivers.pengo.*;
import static drivers.zaxxon.*;
import static drivers.rallyx.*;
import static drivers.ladybug.*;
import static drivers.z80bw.*;
import static drivers.arkanoid.*;
import static drivers.congo.*;
import static drivers.yamato.*;
import static drivers.cclimber.*;
import static drivers.bombjack.*;
import static drivers._1942.*;
import static drivers.system1.*;
import static drivers.gberet.*;
import static drivers.locomotn.*;
import static drivers.pooyan.*;
import static drivers.frogger.*;
import static drivers.snk.*;
import static drivers.mrdo.*;
import static drivers.mitchell.*;
import static drivers.bankp.*;
import static drivers.superqix.*;
import static drivers.troangel.*;
import static drivers.ddragon.*;
import static drivers.contra.*;
import static drivers.travrusa.*;
import static drivers.pingpong.*;
import static drivers.tecmo.*;
import static drivers.sidepckt.*;
import static drivers.vigilant.*;
import static drivers.rocnrope.*;
import static drivers.mikie.*;
import static drivers.dec8.*;
import static drivers.hexa.*;
import static drivers.sonson.*;
import static drivers.amidar.*;
import static drivers.bagman.*;
import static drivers.pbaction.*;
import static drivers.kangaroo.*;
import static drivers.espial.*;
import static drivers.m62.*;
import static drivers.timeplt.*;
import static drivers.gng.*;
import static drivers.gundealr.*;
import static drivers.circusc.*;
import static drivers.combatsc.*;
import static drivers.tp84.*;
import static drivers.ironhors.*;
import static drivers.gunsmoke.*;
import static drivers.lwings.*;
import static drivers.trackfld.*;
import static drivers.commando.*;
import static drivers.citycon.*;
import static drivers.jackal.*;
import static drivers._1943.*;
import static drivers.blktiger.*;
import static drivers.brkthru.*;
import static drivers.jailbrek.*;
import static drivers.sidearms.*;
import static drivers.retofinv.*;
import static drivers.kyugo.*;
import static drivers.kingobox.*;
import static drivers.wc90.*;
import static drivers.atetris.*;
import static drivers.mystston.*;
import static drivers.jack.*;
import static drivers.copsnrob.*;
import static drivers.pcktgal.*;
import static drivers.bogeyman.*;
import static drivers.btime.*;
import static drivers.scregg.*;
import static drivers.exprraid.*;
import static drivers.matmania.*;
import static drivers.renegade.*;
import static drivers.funkybee.*;
import static drivers.hyperspt.*;
import static drivers.flkatck.*;
import static drivers.tnzs.*;
import static drivers.airbustr.*;
import static drivers.xain.*;
import static drivers.vulgus.*;
import static drivers.wiz.*;
import static drivers.ddrible.*;
import static drivers.lkage.*;
import static drivers.shaolins.*;
import static drivers.mainevt.*;


public class driver {
    public static GameDriver drivers[] =
    {
        
        /**
        *
        *   Perfect games
        */
        /*copsnrob*/driver_copsnrob, 
        
        /**
         * Games that only misses sound
         * 
         */
           
        /*lwings*/driver_sectionz,
        /*lwings*/driver_sctionza,
        /*lwings*/driver_trojan,
        /*lwings*/driver_trojanr,
        /*lwings*/driver_trojanj,
        /*lwings*/driver_lwings,
        /*lwings*/driver_lwings2,
        /*lwings*/driver_lwingsjp,
        /*trackfld*/driver_trackfld,
        /*trackfld*/driver_trackflc,
        /*trackfld*/driver_hyprolym,
        /*trackfld*/driver_hyprolyb,
        /*commando*/driver_commando,
        /*commando*/driver_commandu,
        /*commando*/driver_commandj,
        /*commando*/driver_spaceinv,         
        /*citycon*/driver_citycon,
        /*citycon*/driver_citycona,
        /*citycon*/driver_cruisin,
        /*_1943*/driver_1943,
        /*_1943*/driver_1943j,
        /*_1943*/driver_1943kai,/*background islands doesn't draw properly*/
        /*blktiger*/driver_blktiger,
        /*blktiger*/driver_bktigerb,
        /*blktiger*/driver_blkdrgon,
        /*blktiger*/driver_blkdrgnb,
        /*brkthru*/driver_brkthru,
        /*brkthru*/driver_brkthruj,
        /*brkthru*/driver_darwin,     
        /*sidearms*/driver_sidearms,
        /*sidearms*/driver_sidearmr,
        /*sidearms*/driver_sidearjp,
        /*sidearms*/driver_turtship,
        /*sidearms*/driver_dyger, //screen height is too big      
        /*retofinv*/driver_retofinv,
        /*retofinv*/driver_retofin1,
        /*retofinv*/driver_retofin2,
        /*kyugo*/driver_gyrodine,
        /*kyugo*/driver_sonofphx,
        /*kyugo*/driver_repulse,
        /*kyugo*/driver_99lstwar,
        /*kyugo*/driver_99lstwra,
        /*kyugo*/driver_flashgal,
        /*kyugo*/driver_srdmissn,
        /*kyugo*/driver_airwolf,
        /*kyugo*/driver_skywolf,
        /*kyugo*/driver_skywolf2,
        /*kingobox*/driver_kingofb,
        /*kingobox*/driver_ringking,
        /*kingobox*/driver_ringkin3,
        /*wc90*/driver_wc90,
        /*jack*/driver_jack,
        /*jack*/driver_jack2,
        /*jack*/driver_jack3,
        /*jack*/driver_treahunt,
        /*jack*/driver_zzyzzyxx,
        /*jack*/driver_zzyzzyx2,
        /*jack*/driver_brix,
        /*jack*/driver_freeze,
        /*jack*/driver_sucasino,
        /*bogeyman*/driver_bogeyman, 
        /*pcktgal*/driver_pcktgal,	
        /*pcktgal*/driver_pcktgalb,	
        /*pcktgal*/driver_pcktgal2,	
        /*pcktgal*/driver_spool3,	
        /*pcktgal*/driver_spool3i,
        /*scregg*/driver_scregg,
        /*scregg*/driver_eggs,
        /*funkybee*/driver_funkybee,
        /*funkybee*/driver_skylancr,       
        /*hyperspt*/driver_hyperspt,
        /*hyperspt*/driver_hpolym84,
        /*hyperspt*/driver_roadf,
        /*vulgus*/driver_vulgus,
        /*vulgus*/driver_vulgus2,
        /*vulgus*/driver_vulgusj, 
        /*wiz*/driver_stinger,
        /*wiz*/driver_scion,
        /*wiz*/driver_scionc,
        /*wiz*/driver_wiz,
        /*wiz*/driver_wizt,
        /*ddragon*/driver_ddragonb,
        /*ddragon*/driver_ddragon2,
        
        /**
         *  No inputs working
         */
        /*btime*/driver_lnc,
        /*btime*/driver_zoar,
        /*btime*/driver_btime,
        /*btime*/driver_btime2,
        /*btime*/driver_btimem,
        /*btime*/driver_wtennis,
        /*btime*/driver_brubber,
        /*btime*/driver_bnj,
        /*btime*/driver_caractn,
        /*btime*/driver_disco,
        /*btime*/driver_mmonkey,
        /*btime*/driver_cookrace,
        /*mystston*/driver_mystston, //no controls
        
        /**
         *  Small tilemap issues
         */
        /*gberet*/driver_gberet,   //garbage remains in left and right of screen (clipping?)
        /*gberet*/driver_rushatck, //garbage remains in left and right of screen (clipping?)
        /*gberet*/driver_gberetb,  //garbage remains in left and right of screen (clipping?)	
        /*gberet*/driver_mrgoemon, //garbage remains in left and right of screen (clipping?)
        /*lkage*/driver_lkage,//garbage remains in left and right of screen (clipping?)
        /*lkage*/driver_lkageb,//garbage remains in left and right of screen (clipping?)
        /*mainevt*/driver_mainevt,//small garbage left and right of screen
        /*mainevt*/driver_mainevt2,//small garbage left and right of screen
        /*mainevt*/driver_ringohja,//small garbage left and right of screen
        /*mainevt*/driver_devstors,//small garbage in right of screen
        /*mainevt*/driver_devstor2,//small garbage in right of screen
        /*mainevt*/driver_devstor3,//small garbage in right of screen
        /*mainevt*/driver_garuka,//small garbage in right of screen

        /**
         *  Tilemaps issues
         */
        /*xain*/driver_xsleena,  //background image looks not to spilt ok (doubling??)
        /*xain*/driver_xsleenab, //background image looks not to spilt ok (doubling??)
        /*xain*/driver_solarwar, //background image looks not to spilt ok (doubling??)
        
        
        /**
         * Games not working (various reasons)
        */
        
        /*jackal*/driver_jackal, //boots but doesn't start
        /*jackal*/driver_topgunr,//boots but doesn't start
        /*jackal*/driver_jackalj,//boots but doesn't  start
        /*jackal*/driver_topgunbl, //Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: 51 at vidhrdw.jackal$1.handler(jackal.java:45)
        /*jailbrek*/driver_jailbrek, //reboots when i try to start game?
        /*exprraid*/driver_exprraid, // Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: 4110 at mame.memory.cpu_writemem16(memory.java:1279)
        /*exprraid*/driver_wexpress, // Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: 4110 at mame.memory.cpu_writemem16(memory.java:1279)
        /*exprraid*/driver_wexpresb, // Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: 4110 at mame.memory.cpu_writemem16(memory.java:1279)
        /*hyperspt*/driver_roadf2, //Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: 65536 at arcadeflex.libc$UBytePtr.read(libc.java:86)
        /*ddrible*/driver_ddribble, //fails on self booting
        /*ddragon*/driver_ddragon,  //missing HD63701 cpu
        /*shaolins*/driver_kicker,//Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: 1024 at vidhrdw.shaolins$3.handler(shaolins.java:155)
        /*shaolins*/driver_shaolins,//Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: 1024 at vidhrdw.shaolins$3.handler(shaolins.java:155)
    
        /**
         * Games not working in mame 0.36 either
         */
        
        /*lwings*/driver_avengers,
        /*lwings*/driver_avenger2,
        /*kingobox*/driver_ringkin2,

        /**
         *  Test (Not working) drivers
         */
        /*btime*/ //TESTDRIVER( decocass )
        
        
        /**
         * WIP section
         */

        
        /*tnzs*/driver_extrmatn,
	/*tnzs*/driver_arkanoi2,  
	/*tnzs*/driver_ark2us,   
	/*tnzs*/driver_ark2jp,   
	/*tnzs*/driver_plumppop, 
	/*tnzs*/driver_drtoppel, 
	/*tnzs*/driver_chukatai, 	
	/*tnzs*/driver_tnzs,	
	/*tnzs*/driver_tnzsb,	
	/*tnzs*/driver_tnzs2,	
	/*tnzs*/driver_insectx,
	/*tnzs*/driver_kageki,	
	/*tnzs*/driver_kagekij,

        /*atetris*/driver_atetris,
        /*atetris*/driver_atetrisa,
        /*atetris*/driver_atetrisb,
        /*atetris*/driver_atetcktl,
        /*atetris*/driver_atetckt2,
       
        /*matmania*/driver_matmania,
        /*matmania*/driver_excthour,
        /*matmania*/driver_maniach,
        /*matmania*/driver_maniach2,
        
        /*renegade*/driver_renegade,//no sprites?
        /*renegade*/driver_kuniokun,//no sprites?
        /*renegade*/driver_kuniokub,//no sprites?
        

        
        /*flkatck*/driver_mx5000,
        /*flkatck*/driver_flkatck,

        /*airbustr*/driver_airbustr,	/* (c) 1990 Kaneko */
        
        /* "Pacman hardware" games */
        driver_pacman,     /* (c) 1980 Namco */
        driver_pacmanjp,   /* (c) 1980 Namco */
        driver_pacmanm,	   /* (c) 1980 Midway */
        driver_npacmod,	   /* (c) 1981 Namco */
        driver_pacmod,	   /* (c) 1981 Midway */
        driver_hangly,	   /* hack */
        driver_hangly2,	   /* hack */
        driver_puckman,	   /* hack */
        driver_pacheart,   /* hack */
        driver_piranha,	   /* hack */
        driver_pacplus,
        driver_mspacman,   /* (c) 1981 Midway (but it's a bootleg) */	/* made by Gencomp */
        driver_mspacatk,   /* hack */
        driver_pacgal,	   /* hack */
        driver_maketrax,	/* (c) 1981 Williams, high score table says KRL (fur Kural) */
        driver_crush,	   /* (c) 1981 Kural Samno Electric Ltd */
        driver_crush2,	/* (c) 1981 Kural Esco Electric Ltd - bootleg? */
        driver_crush3,	/* Kural Electric Ltd - bootleg? */
        driver_mbrush,	  /* 1981 bootleg */
        driver_paintrlr,  /* 1981 bootleg */
        driver_eyes,		/* (c) 1982 Digitrex Techstar + "Rockola presents" */
        driver_eyes2,		/* (c) 1982 Techstar + "Rockola presents" */
        driver_mrtnt,		/* (c) 1983 Telko */
        driver_ponpoko,	/* (c) 1982 Sigma Ent. Inc. */
        driver_ponpokov,	/* (c) 1982 Sigma Ent. Inc. + Venture Line license */
        driver_lizwiz,	/* (c) 1985 Techstar + "Sunn presents" */
        driver_theglob,	/* (c) 1983 Epos Corporation */
        driver_beastf,	/* (c) 1984 Epos Corporation */
        driver_jumpshot,
        driver_dremshpr,/* (c) 1982 Sanritsu */
        driver_vanvan,	/* (c) 1983 Karateco (bootleg?) */
        driver_vanvans,	/* (c) 1983 Sanritsu */
        driver_alibaba,	/* (c) 1982 Sega */
        driver_pengo,	/* 834-0386 (c) 1982 Sega */
        driver_pengo2,	/* 834-0386 (c) 1982 Sega */
        driver_pengo2u,	/* 834-0386 (c) 1982 Sega */
        driver_penta,		/* bootleg */
/*TODO*/ //	DRIVER( jrpacman )	/* (c) 1983 Midway */

	/* "Galaxian hardware" games */
/*TODO*/ //	DRIVER( galaxian )	/* (c) Namco */
/*TODO*/ //	DRIVER( galmidw )	/* (c) Midway */
/*TODO*/ //	DRIVER( superg )	/* hack */
/*TODO*/ //	DRIVER( galaxb )	/* bootleg */
/*TODO*/ //	DRIVER( galapx )	/* hack */
/*TODO*/ //	DRIVER( galap1 )	/* hack */
/*TODO*/ //	DRIVER( galap4 )	/* hack */
/*TODO*/ //	DRIVER( galturbo )	/* hack */
/*TODO*/ //	DRIVER( swarm )		/* hack */
/*TODO*/ //	DRIVER( pisces )	/* ? */
/*TODO*/ //	DRIVER( uniwars )	/* (c) Irem */
/*TODO*/ //	DRIVER( gteikoku )	/* (c) Irem */
/*TODO*/ //	DRIVER( spacbatt )	/* bootleg */
/*TODO*/ //	DRIVER( warofbug )	/* (c) 1981 Armenia */
/*TODO*/ //	DRIVER( redufo )	/* ? */
/*TODO*/ //	DRIVER( pacmanbl )	/* bootleg */
/*TODO*/ //	DRIVER( devilfsg )	/* (c) 1984 Vision / Artic (bootleg?) */
/*TODO*/ //	DRIVER( zigzag )	/* (c) 1982 LAX */
/*TODO*/ //	DRIVER( zigzag2 )	/* (c) 1982 LAX */
/*TODO*/ //	DRIVER( jumpbug )	/* (c) 1981 Rock-ola */
/*TODO*/ //	DRIVER( jumpbugb )	/* (c) 1981 Sega */
/*TODO*/ //	DRIVER( levers )	/* (c) 1983 Rock-ola */
/*TODO*/ //	DRIVER( azurian )	/* (c) 1982 Rait Electronics Ltd */
/*TODO*/ //	DRIVER( orbitron )	/* Signatron USA */
/*TODO*/ //	DRIVER( mooncrgx )	/* bootleg */
/*TODO*/ //	DRIVER( mooncrst )	/* (c) 1980 Nichibutsu */
/*TODO*/ //	DRIVER( mooncrsg )	/* (c) 1980 Gremlin */
/*TODO*/ //	DRIVER( smooncrs )	/* Gremlin */
/*TODO*/ //	DRIVER( mooncrsb )	/* bootleg */
/*TODO*/ //	DRIVER( mooncrs2 )	/* bootleg */
/*TODO*/ //	DRIVER( fantazia )	/* bootleg */
/*TODO*/ //	DRIVER( eagle )		/* (c) Centuri */
/*TODO*/ //	DRIVER( eagle2 )	/* (c) Centuri */
/*TODO*/ //	DRIVER( moonqsr )	/* (c) 1980 Nichibutsu */
/*TODO*/ //	DRIVER( checkman )	/* (c) 1982 Zilec-Zenitone */
/*TODO*/ //	DRIVER( checkmaj )	/* (c) 1982 Jaleco (Zenitone/Zilec in ROM CM4, and the programmer names) */
/*TODO*/ //	DRIVER( streakng )	/* [1980] Shoei */
/*TODO*/ //	DRIVER( blkhole )	/* TDS (Tokyo Denshi Sekkei) */
/*TODO*/ //	DRIVER( moonal2 )	/* Nichibutsu */
/*TODO*/ //	DRIVER( moonal2b )	/* Nichibutsu */
/*TODO*/ //	DRIVER( kingball )	/* (c) 1980 Namco */
/*TODO*/ //	DRIVER( kingbalj )	/* (c) 1980 Namco */

	/* "Scramble hardware" (and variations) games */
/*TODO*/ //	DRIVER( scramble )	/* GX387 (c) 1981 Konami */
/*TODO*/ //	DRIVER( scrambls )	/* GX387 (c) 1981 Stern */
/*TODO*/ //	DRIVER( scramblb )	/* bootleg */
/*TODO*/ //	DRIVER( atlantis )	/* (c) 1981 Comsoft */
/*TODO*/ //	DRIVER( atlants2 )	/* (c) 1981 Comsoft */
/*TODO*/ //	DRIVER( theend )	/* (c) 1980 Konami */
/*TODO*/ //	DRIVER( theends )	/* (c) 1980 Stern */
/*TODO*/ //	DRIVER( ckongs )	/* bootleg */
/*TODO*/ //	DRIVER( froggers )	/* bootleg */
/*TODO*/ //	DRIVER( amidars )	/* (c) 1982 Konami */
/*TODO*/ //	DRIVER( triplep )	/* (c) 1982 KKI */
/*TODO*/ //	DRIVER( knockout )	/* (c) 1982 KKK */
/*TODO*/ //	DRIVER( mariner )	/* (c) 1981 Amenip */
/*TODO*/ //	DRIVER( mars )		/* (c) 1981 Artic */
/*TODO*/ //	DRIVER( devilfsh )	/* (c) 1982 Artic */
/*TODO*/ //	DRIVER( newsin7 )	/* (c) 1983 ATW USA, Inc. */
/*TODO*/ //	DRIVER( hotshock )	/* (c) 1982 E.G. Felaco */
/*TODO*/ //	DRIVER( hunchbks )	/* (c) 1983 Century */
/*TODO*/ //	DRIVER( scobra )	/* GX316 (c) 1981 Konami */
/*TODO*/ //	DRIVER( scobras )	/* GX316 (c) 1981 Stern */
/*TODO*/ //	DRIVER( scobrab )	/* GX316 (c) 1981 Karateco (bootleg?) */
/*TODO*/ //	DRIVER( stratgyx )	/* GX306 (c) 1981 Konami */
/*TODO*/ //	DRIVER( stratgys )	/* GX306 (c) 1981 Stern */
/*TODO*/ //	DRIVER( armorcar )	/* (c) 1981 Stern */
/*TODO*/ //	DRIVER( armorca2 )	/* (c) 1981 Stern */
/*TODO*/ //	DRIVER( moonwar2 )	/* (c) 1981 Stern */
/*TODO*/ //	DRIVER( monwar2a )	/* (c) 1981 Stern */
/*TODO*/ //	DRIVER( spdcoin )	/* (c) 1984 Stern */
/*TODO*/ //	DRIVER( darkplnt )	/* (c) 1982 Stern */
/*TODO*/ //	DRIVER( tazmania )	/* (c) 1982 Stern */
/*TODO*/ //	DRIVER( tazmani2 )	/* (c) 1982 Stern */
/*TODO*/ //	DRIVER( calipso )	/* (c) 1982 Tago */
/*TODO*/ //	DRIVER( anteater )	/* (c) 1982 Tago */
/*TODO*/ //	DRIVER( rescue )	/* (c) 1982 Stern */
/*TODO*/ //	DRIVER( minefld )	/* (c) 1983 Stern */
/*TODO*/ //	DRIVER( losttomb )	/* (c) 1982 Stern */
/*TODO*/ //	DRIVER( losttmbh )	/* (c) 1982 Stern */
/*TODO*/ //	DRIVER( superbon )	/* bootleg */
/*TODO*/ //	DRIVER( hustler )	/* GX343 (c) 1981 Konami */
/*TODO*/ //	DRIVER( billiard )	/* bootleg */
/*TODO*/ //	DRIVER( hustlerb )	/* bootleg */
            driver_frogger ,	/* GX392 (c) 1981 Konami */
            driver_frogseg1 ,	/* (c) 1981 Sega */
            driver_frogseg2 ,	/* 834-0068 (c) 1981 Sega */
            driver_froggrmc ,	/* 800-3110 (c) 1981 Sega */
            driver_amidar,	/* GX337 (c) 1981 Konami */
            driver_amidaru,	/* GX337 (c) 1982 Konami + Stern license */
            driver_amidaro,	/* GX337 (c) 1982 Konami + Olympia license */
            driver_amigo,		/* bootleg */
            driver_turtles,	/* (c) 1981 Stern */
            driver_turpin,	/* (c) 1981 Sega */
            driver_600,		/* GX353 (c) 1981 Konami */
/*TODO*/ //	DRIVER( flyboy )	/* (c) 1982 Kaneko */
/*TODO*/ //	DRIVER( flyboyb )	/* bootleg */
/*TODO*/ //	DRIVER( fastfred )	/* (c) 1982 Atari */
/*TODO*/ //	DRIVER( jumpcoas )	/* (c) 1983 Kaneko */

	/* "Crazy Climber hardware" games */
        driver_cclimber, /* (c) 1980 Nichibutsu */
        driver_cclimbrj,/* (c) 1980 Nichibutsu */
        driver_ccboot, 	/* bootleg */
        driver_ccboot2,	/* bootleg */
        driver_ckong,	/* (c) 1981 Falcon */
        driver_ckonga,	/* (c) 1981 Falcon */
        driver_ckongjeu,/* bootleg */
        driver_ckongo,	/* bootleg */
        driver_ckongalc,/* bootleg */
        driver_monkeyd,	/* bootleg */
        driver_rpatrolb,	/* bootleg */
        driver_silvland,	/* Falcon */
        driver_yamato,	/* (c) 1983 Sega */
        driver_yamato2,	/* (c) 1983 Sega */
        driver_swimmer,	/* (c) 1982 Tehkan */
        driver_swimmera,/* (c) 1982 Tehkan */
        driver_guzzler,	/* (c) 1983 Tehkan */

	/* Nichibutsu games */
/*TODO*/ //	DRIVER( friskyt )	/* (c) 1981 */
/*TODO*/ //	DRIVER( radrad )	/* (c) 1982 Nichibutsu USA */
/*TODO*/ //	DRIVER( seicross )	/* (c) 1984 + Alice */
/*TODO*/ //	DRIVER( sectrzon )	/* (c) 1984 + Alice */
/*TODO*/ //	DRIVER( wiping )	/* (c) 1982 */
/*TODO*/ //	DRIVER( rugrats )	/* (c) 1983 */
/*TODO*/ //	DRIVER( cop01 )		/* (c) 1985 */
/*TODO*/ //	DRIVER( cop01a )	/* (c) 1985 */
/*TODO*/ //	DRIVER( terracre )	/* (c) 1985 */
/*TODO*/ //	DRIVER( terracrb )	/* (c) 1985 */
/*TODO*/ //	DRIVER( terracra )	/* (c) 1985 */
/*TODO*/ //	DRIVER( galivan )	/* (c) 1985 */
/*TODO*/ //	DRIVER( galivan2 )	/* (c) 1985 */
/*TODO*/ //	DRIVER( dangar )	/* (c) 1986 */
/*TODO*/ //	DRIVER( dangar2 )	/* (c) 1986 */
/*TODO*/ //	DRIVER( dangarb )	/* bootleg */
/*TODO*/ //	DRIVER( ninjemak )	/* (c) 1986 (US?) */
/*TODO*/ //	DRIVER( youma )		/* (c) 1986 (Japan) */
/*TODO*/ //	DRIVER( terraf )	/* (c) 1987 */
/*TODO*/ //	DRIVER( terrafu )	/* (c) 1987 Nichibutsu USA */
/*TODO*/ //	DRIVER( kodure )	/* (c) 1987 (Japan) */
/*TODO*/ //	DRIVER( armedf )	/* (c) 1988 */
/*TODO*/ //	DRIVER( cclimbr2 )	/* (c) 1988 (Japan) */

	/* "Phoenix hardware" (and variations) games */
/*TODO*/ //	DRIVER( phoenix )	/* (c) 1980 Amstar */
/*TODO*/ //	DRIVER( phoenixa )	/* (c) 1980 Amstar + Centuri license */
/*TODO*/ //	DRIVER( phoenixt )	/* (c) 1980 Taito */
/*TODO*/ //	DRIVER( phoenix3 )	/* bootleg */
/*TODO*/ //	DRIVER( phoenixc )	/* bootleg */
/*TODO*/ //	DRIVER( pleiads )	/* (c) 1981 Tehkan */
/*TODO*/ //	DRIVER( pleiadbl )	/* bootleg */
/*TODO*/ //	DRIVER( pleiadce )	/* (c) 1981 Centuri + Tehkan */
/*TODO*/ //	DRIVER( naughtyb )	/* (c) 1982 Jaleco */
/*TODO*/ //	DRIVER( naughtya )	/* bootleg */
/*TODO*/ //	DRIVER( naughtyc )	/* (c) 1982 Jaleco + Cinematronics */
/*TODO*/ //	DRIVER( popflame )	/* (c) 1982 Jaleco */
/*TODO*/ //	DRIVER( popflama )	/* (c) 1982 Jaleco */
/*TODO*/ //TESTDRIVER( popflamb )

	/* Namco games (plus some intruders on similar hardware) */
/*TODO*/ //	DRIVER( geebee )	/* [1978] Namco */
/*TODO*/ //	DRIVER( geebeeg )	/* [1978] Gremlin */
/*TODO*/ //	DRIVER( bombbee )	/* [1979] Namco */
/*TODO*/ //	DRIVER( cutieq )	/* (c) 1979 Namco */
/*TODO*/ //	DRIVER( navalone )	/* (c) 1980 Namco */
/*TODO*/ //	DRIVER( kaitei )	/* [1980] K.K. Tokki */
/*TODO*/ //	DRIVER( kaitein )	/* [1980] Namco */
/*TODO*/ //	DRIVER( sos )		/* [1980] Namco */
/*TODO*/ //	DRIVER( tankbatt )	/* (c) 1980 Namco */
/*TODO*/ //	DRIVER( warpwarp )	/* (c) 1981 Namco */
/*TODO*/ //	DRIVER( warpwarr )	/* (c) 1981 Rock-ola - the high score table says "NAMCO" */
/*TODO*/ //	DRIVER( warpwar2 )	/* (c) 1981 Rock-ola - the high score table says "NAMCO" */
        driver_rallyx,/* (c) 1980 Namco */
        driver_rallyxm, /* (c) 1980 Midway */
        driver_nrallyx, /* (c) 1981 Namco */
        driver_jungler,	/* GX327 (c) 1981 Konami */
        driver_junglers,/* GX327 (c) 1981 Stern */
        driver_locomotn,/* GX359 (c) 1982 Konami + Centuri license */
        driver_gutangtn,/* GX359 (c) 1982 Konami + Sega license */
        driver_cottong,	/* bootleg */
        driver_commsega,/* (c) 1983 Sega */
	/* the following ones all have a custom I/O chip */
/*TODO*/ //	DRIVER( bosco )		/* (c) 1981 */
/*TODO*/ //	DRIVER( boscoo )	/* (c) 1981 */
/*TODO*/ //	DRIVER( boscomd )	/* (c) 1981 Midway */
/*TODO*/ //	DRIVER( boscomdo )	/* (c) 1981 Midway */
/*TODO*/ //	DRIVER( galaga )	/* (c) 1981 */
/*TODO*/ //	DRIVER( galagamw )	/* (c) 1981 Midway */
/*TODO*/ //	DRIVER( galagads )	/* hack */
/*TODO*/ //	DRIVER( gallag )	/* bootleg */
/*TODO*/ //	DRIVER( galagab2 )	/* bootleg */
/*TODO*/ //	DRIVER( galaga84 )	/* hack */
/*TODO*/ //	DRIVER( nebulbee )	/* hack */
/*TODO*/ //	DRIVER( digdug )	/* (c) 1982 */
/*TODO*/ //	DRIVER( digdugb )	/* (c) 1982 */
/*TODO*/ //	DRIVER( digdugat )	/* (c) 1982 Atari */
/*TODO*/ //	DRIVER( dzigzag )	/* bootleg */
/*TODO*/ //	DRIVER( xevious )	/* (c) 1982 */
/*TODO*/ //	DRIVER( xeviousa )	/* (c) 1982 + Atari license */
/*TODO*/ //	DRIVER( xevios )	/* bootleg */
/*TODO*/ //	DRIVER( sxevious )	/* (c) 1984 */
/*TODO*/ //	DRIVER( superpac )	/* (c) 1982 */
/*TODO*/ //	DRIVER( superpcm )	/* (c) 1982 Midway */
/*TODO*/ //	DRIVER( pacnpal )	/* (c) 1983 */
/*TODO*/ //	DRIVER( pacnchmp )	/* (c) 1983 */
/*TODO*/ //	DRIVER( phozon )	/* (c) 1983 */
/*TODO*/ //	DRIVER( mappy )		/* (c) 1983 */
/*TODO*/ //	DRIVER( mappyjp )	/* (c) 1983 */
/*TODO*/ //	DRIVER( digdug2 )	/* (c) 1985 */
/*TODO*/ //	DRIVER( digdug2a )	/* (c) 1985 */
/*TODO*/ //	DRIVER( todruaga )	/* (c) 1984 */
/*TODO*/ //	DRIVER( todruagb )	/* (c) 1984 */
/*TODO*/ //	DRIVER( motos )		/* (c) 1985 */
/*TODO*/ //	DRIVER( grobda )	/* (c) 1984 */
/*TODO*/ //	DRIVER( grobda2 )	/* (c) 1984 */
/*TODO*/ //	DRIVER( grobda3 )	/* (c) 1984 */
/*TODO*/ //	DRIVER( gaplus )	/* (c) 1984 */
/*TODO*/ //	DRIVER( gaplusa )	/* (c) 1984 */
/*TODO*/ //	DRIVER( galaga3 )	/* (c) 1984 */
/*TODO*/ //	DRIVER( galaga3a )	/* (c) 1984 */
	/* Z8000 games */
/*TODO*/ //	DRIVER( polepos )	/* (c) 1982  */
/*TODO*/ //	DRIVER( poleposa )	/* (c) 1982 + Atari license */
/*TODO*/ //	DRIVER( polepos1 )	/* (c) 1982 Atari */
/*TODO*/ //	DRIVER( topracer )	/* bootleg */
/*TODO*/ //	DRIVER( polepos2 )	/* (c) 1983 */
/*TODO*/ //	DRIVER( poleps2a )	/* (c) 1983 + Atari license */
/*TODO*/ //	DRIVER( poleps2b )	/* bootleg */
/*TODO*/ //	DRIVER( poleps2c )	/* bootleg */
	/* no custom I/O in the following, HD63701 (or compatible) microcontroller instead */
/*TODO*/ //	DRIVER( pacland )	/* (c) 1984 */
/*TODO*/ //	DRIVER( pacland2 )	/* (c) 1984 */
/*TODO*/ //	DRIVER( pacland3 )	/* (c) 1984 */
/*TODO*/ //	DRIVER( paclandm )	/* (c) 1984 Midway */
/*TODO*/ //	DRIVER( drgnbstr )	/* (c) 1984 */
/*TODO*/ //	DRIVER( skykid )	/* (c) 1985 */
/*TODO*/ //	DRIVER( baraduke )	/* (c) 1985 */
/*TODO*/ //	DRIVER( metrocrs )	/* (c) 1985 */

	/* Namco System 86 games */
/*TODO*/ //	DRIVER( hopmappy )	/* (c) 1986 */
/*TODO*/ //	DRIVER( skykiddx )	/* (c) 1986 */
/*TODO*/ //	DRIVER( skykiddo )	/* (c) 1986 */
/*TODO*/ //	DRIVER( roishtar )	/* (c) 1986 */
/*TODO*/ //	DRIVER( genpeitd )	/* (c) 1986 */
/*TODO*/ //	DRIVER( rthunder )	/* (c) 1986 new version */
/*TODO*/ //	DRIVER( rthundro )	/* (c) 1986 old version */
/*TODO*/ //	DRIVER( wndrmomo )	/* (c) 1987 */

	/* Namco System 1 games */
/*TODO*/ //	DRIVER( shadowld )	/* (c) 1987 */
/*TODO*/ //	DRIVER( youkaidk )	/* (c) 1987 (Japan new version) */
/*TODO*/ //	DRIVER( yokaidko )	/* (c) 1987 (Japan old version) */
/*TODO*/ //	DRIVER( dspirit )	/* (c) 1987 new version */
/*TODO*/ //	DRIVER( dspirito )	/* (c) 1987 old version */
/*TODO*/ //	DRIVER( blazer )	/* (c) 1987 (Japan) */
/*TODO*/ //	DRIVER( quester )	/* (c) 1987 (Japan) */
/*TODO*/ //	DRIVER( pacmania )	/* (c) 1987 */
/*TODO*/ //	DRIVER( pacmanij )	/* (c) 1987 (Japan) */
/*TODO*/ //	DRIVER( galaga88 )	/* (c) 1987 */
/*TODO*/ //	DRIVER( galag88b )	/* (c) 1987 */
/*TODO*/ //	DRIVER( galag88j )	/* (c) 1987 (Japan) */
/*TODO*/ //	DRIVER( ws )		/* (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( berabohm )	/* (c) 1988 (Japan) */
	/* 1988 Alice in Wonderland (English version of Marchen maze) */
/*TODO*/ //	DRIVER( mmaze )		/* (c) 1988 (Japan) */
/*TODO*/ //TESTDRIVER( bakutotu )	/* (c) 1988 */
/*TODO*/ //	DRIVER( wldcourt )	/* (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( splatter )	/* (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( faceoff )	/* (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( rompers )	/* (c) 1989 (Japan) */
/*TODO*/ //	DRIVER( romperso )	/* (c) 1989 (Japan) */
/*TODO*/ //	DRIVER( blastoff )	/* (c) 1989 (Japan) */
/*TODO*/ //	DRIVER( ws89 )		/* (c) 1989 (Japan) */
/*TODO*/ //	DRIVER( dangseed )	/* (c) 1989 (Japan) */
/*TODO*/ //	DRIVER( ws90 )		/* (c) 1990 (Japan) */
/*TODO*/ //	DRIVER( pistoldm )	/* (c) 1990 (Japan) */
/*TODO*/ //	DRIVER( soukobdx )	/* (c) 1990 (Japan) */
/*TODO*/ //	DRIVER( puzlclub )	/* (c) 1990 (Japan) */
/*TODO*/ //	DRIVER( tankfrce )	/* (c) 1991 (US) */
/*TODO*/ //	DRIVER( tankfrcj )	/* (c) 1991 (Japan) */

	/* Namco System 2 games */
/*TODO*/ //TESTDRIVER( finallap )	/* 87.12 Final Lap */
/*TODO*/ //TESTDRIVER( finalapd )	/* 87.12 Final Lap */
/*TODO*/ //TESTDRIVER( finalapc )	/* 87.12 Final Lap */
/*TODO*/ //TESTDRIVER( finlapjc )	/* 87.12 Final Lap */
/*TODO*/ //TESTDRIVER( finlapjb )	/* 87.12 Final Lap */
/*TODO*/ //	DRIVER( assault )	/* (c) 1988 */
/*TODO*/ //	DRIVER( assaultj )	/* (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( assaultp )	/* (c) 1988 (Japan) */
/*TODO*/ //TESTDRIVER( metlhawk )	/* (c) 1988 */
/*TODO*/ //	DRIVER( mirninja )	/* (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( ordyne )	/* (c) 1988 */
/*TODO*/ //	DRIVER( phelios )	/* (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( burnforc )	/* (c) 1989 (Japan) */
/*TODO*/ //TESTDRIVER( dirtfoxj )	/* (c) 1989 (Japan) */
/*TODO*/ //	DRIVER( finehour )	/* (c) 1989 (Japan) */
/*TODO*/ //TESTDRIVER( fourtrax )	/* 89.11 */
/*TODO*/ //	DRIVER( marvland )	/* (c) 1989 (US) */
/*TODO*/ //	DRIVER( marvlanj )	/* (c) 1989 (Japan) */
/*TODO*/ //	DRIVER( valkyrie )	/* (c) 1989 (Japan) */
/*TODO*/ //	DRIVER ( kyukaidk )	/* (c) 1990 (Japan) */
/*TODO*/ //	DRIVER ( kyukaido )	/* (c) 1990 (Japan) */
/*TODO*/ //	DRIVER( dsaber )	/* (c) 1990 */
/*TODO*/ //	DRIVER( dsaberj )	/* (c) 1990 (Japan) */
/*TODO*/ //	DRIVER( rthun2 )	/* (c) 1990 */
/*TODO*/ //	DRIVER( rthun2j )	/* (c) 1990 (Japan) */
/*TODO*/ //TESTDRIVER( finalap2 )	/* 90.8  Final Lap 2 */
/*TODO*/ //TESTDRIVER( finalp2j )	/* 90.8  Final Lap 2 (Japan) */
	/* 91.3  Steel Gunner */
	/* 91.7  Golly Ghost */
	/* 91.9  Super World Stadium */
/*TODO*/ //TESTDRIVER( sgunner2 )	/* (c) 1991 (Japan) */
/*TODO*/ //	DRIVER( cosmogng )	/* (c) 1991 (US) */
/*TODO*/ //	DRIVER( cosmognj )	/* (c) 1991 (Japan) */
/*TODO*/ //TESTDRIVER( finalap3 )	/* 92.9  Final Lap 3 */
/*TODO*/ //TESTDRIVER( suzuka8h )
	/* 92.8  Bubble Trouble */
/*TODO*/ //	DRIVER( sws92 )		/* (c) 1992 (Japan) */
	/* 93.4  Lucky & Wild */
/*TODO*/ //TESTDRIVER( suzuk8h2 )
/*TODO*/ //	DRIVER( sws93 )		/* (c) 1993 (Japan) */
	/* 93.6  Super World Stadium '93 */

	/* Universal games */
/*TODO*/ //	DRIVER( cosmicg )	/* 7907 (c) 1979 */
/*TODO*/ //	DRIVER( cosmica )	/* 7910 (c) [1979] */
/*TODO*/ //	DRIVER( cosmica2 )	/* 7910 (c) 1979 */
/*TODO*/ //	DRIVER( panic )		/* (c) 1980 */
/*TODO*/ //	DRIVER( panica )	/* (c) 1980 */
/*TODO*/ //	DRIVER( panicger )	/* (c) 1980 */
/*TODO*/ //	DRIVER( magspot2 )	/* 8013 (c) [1980] */
/*TODO*/ //	DRIVER( devzone )	/* 8022 (c) [1980] */
/*TODO*/ //	DRIVER( nomnlnd )	/* (c) [1980?] */
/*TODO*/ //	DRIVER( nomnlndg )	/* (c) [1980?] + Gottlieb */
/*TODO*/ //	DRIVER( cheekyms )	/* (c) [1980?] */
    driver_ladybug,	/* (c) 1981 */
    driver_ladybugb,	/* bootleg */
    driver_snapjack,	/* (c) */
    driver_cavenger,	/* (c) 1981 */
    driver_mrdo,		/* (c) 1982 */
    driver_mrdot,		/* (c) 1982 + Taito license */
    driver_mrdofix,	/* (c) 1982 + Taito license */
    driver_mrlo,		/* bootleg */
    driver_mrdu,		/* bootleg */
    driver_mrdoy,		/* bootleg */
    driver_yankeedo,	       /* bootleg */
/*TODO*/ //	DRIVER( docastle )	/* (c) 1983 */
/*TODO*/ //	DRIVER( docastl2 )	/* (c) 1983 */
/*TODO*/ //	DRIVER( douni )		/* (c) 1983 */
/*TODO*/ //	DRIVER( dorunrun )	/* (c) 1984 */
/*TODO*/ //	DRIVER( dorunru2 )	/* (c) 1984 */
/*TODO*/ //	DRIVER( dorunruc )	/* (c) 1984 */
/*TODO*/ //	DRIVER( spiero )	/* (c) 1987 */
/*TODO*/ //	DRIVER( dowild )	/* (c) 1984 */
/*TODO*/ //	DRIVER( jjack )		/* (c) 1984 */
/*TODO*/ //	DRIVER( kickridr )	/* (c) 1984 */

	/* Nintendo games */
/*TODO*/ //	DRIVER( radarscp )	/* (c) 1980 Nintendo */
/*TODO*/ //	DRIVER( dkong )		/* (c) 1981 Nintendo of America */
/*TODO*/ //	DRIVER( dkongjp )	/* (c) 1981 Nintendo */
/*TODO*/ //	DRIVER( dkongjpo )	/* (c) 1981 Nintendo */
/*TODO*/ //	DRIVER( dkongjr )	/* (c) 1982 Nintendo of America */
/*TODO*/ //	DRIVER( dkngjrjp )	/* no copyright notice */
/*TODO*/ //	DRIVER( dkjrjp )	/* (c) 1982 Nintendo */
/*TODO*/ //	DRIVER( dkjrbl )	/* (c) 1982 Nintendo of America */
/*TODO*/ //	DRIVER( dkong3 )	/* (c) 1983 Nintendo of America */
/*TODO*/ //	DRIVER( dkong3j )	/* (c) 1983 Nintendo */
/*TODO*/ //                driver_mario,	/* (c) 1983 Nintendo of America */
/*TODO*/ //                driver_mariojp,	/* (c) 1983 Nintendo */
/*TODO*/ //                driver_masao,	/* bootleg */
/*TODO*/ //	DRIVER( hunchbkd )	/* (c) 1983 Century */
/*TODO*/ //	DRIVER( herbiedk )	/* (c) 1984 CVS */
/*TODO*/ //TESTDRIVER( herocast )
/*TODO*/ //	DRIVER( popeye )
/*TODO*/ //	DRIVER( popeye2 )
/*TODO*/ //	DRIVER( popeyebl )	/* bootleg */
/*TODO*/ //	DRIVER( punchout )	/* (c) 1984 */
/*TODO*/ //	DRIVER( spnchout )	/* (c) 1984 */
/*TODO*/ //	DRIVER( spnchotj )	/* (c) 1984 (Japan) */
/*TODO*/ //	DRIVER( armwrest )	/* (c) 1985 */

	/* Midway 8080 b/w games */
/*TODO*/ //	DRIVER( seawolf )	/* 596 [1976] */
/*TODO*/ //	DRIVER( gunfight )	/* 597 [1975] */
	/* 603 - Top Gun [1976] */
/*TODO*/ //	DRIVER( tornbase )	/* 605 [1976] */
/*TODO*/ //	DRIVER( 280zzzap )	/* 610 [1976] */
/*TODO*/ //	DRIVER( maze )		/* 611 [1976] */
/*TODO*/ //	DRIVER( boothill )	/* 612 [1977] */
/*TODO*/ //	DRIVER( checkmat )	/* 615 [1977] */
/*TODO*/ //	DRIVER( desertgu )	/* 618 [1977] */
/*TODO*/ //	DRIVER( dplay )		/* 619 [1977] */
/*TODO*/ //	DRIVER( lagunar )	/* 622 [1977] */
/*TODO*/ //	DRIVER( gmissile )	/* 623 [1977] */
/*TODO*/ //	DRIVER( m4 )		/* 626 [1977] */
/*TODO*/ //	DRIVER( clowns )	/* 630 [1978] */
	/* 640 - Space Walk [1978] */
/*TODO*/ //	DRIVER( einnings )	/* 642 [1978] Midway */
/*TODO*/ //	DRIVER( shuffle )	/* 643 [1978] */
/*TODO*/ //	DRIVER( dogpatch )	/* 644 [1977] */
/*TODO*/ //	DRIVER( spcenctr )	/* 645 (c) 1980 Midway */
/*TODO*/ //	DRIVER( phantom2 )	/* 652 [1979] */
/*TODO*/ //	DRIVER( bowler )	/* 730 [1978] Midway */
/*TODO*/ //	DRIVER( invaders )	/* 739 [1979] */
/*TODO*/ //	DRIVER( blueshrk )	/* 742 [1978] */
/*TODO*/ //	DRIVER( invad2ct )	/* 851 (c) 1980 Midway */
/*TODO*/ //	DRIVER( invadpt2 )	/* 852 [1980] Taito */
/*TODO*/ //	DRIVER( invdpt2m )	/* 852 [1980] Midway */
	/* 870 - Space Invaders Deluxe cocktail */
/*TODO*/ //	DRIVER( earthinv )
/*TODO*/ //	DRIVER( spaceatt )
/*TODO*/ //	DRIVER( sinvemag )
/*TODO*/ //	DRIVER( jspecter )
/*TODO*/ //	DRIVER( invrvnge )
/*TODO*/ //	DRIVER( invrvnga )
/*TODO*/ //	DRIVER( galxwars )
/*TODO*/ //	DRIVER( starw )
/*TODO*/ //	DRIVER( lrescue )	/* (c) 1979 Taito */
/*TODO*/ //	DRIVER( grescue )	/* bootleg? */
/*TODO*/ //	DRIVER( desterth )	/* bootleg */
/*TODO*/ //	DRIVER( cosmicmo )	/* Universal */
/*TODO*/ //	DRIVER( rollingc )	/* Nichibutsu */
/*TODO*/ //	DRIVER( bandido )	/* (c) Exidy */
/*TODO*/ //	DRIVER( ozmawars )	/* Shin Nihon Kikaku (SNK) */
/*TODO*/ //	DRIVER( solfight )	/* bootleg */
/*TODO*/ //	DRIVER( spaceph )	/* Zilec Games */
/*TODO*/ //	DRIVER( schaser )	/* Taito */
/*TODO*/ //	DRIVER( lupin3 )	/* (c) 1980 Taito */
/*TODO*/ //	DRIVER( helifire )	/* (c) Nintendo */
/*TODO*/ //	DRIVER( helifira )	/* (c) Nintendo */
/*TODO*/ //	DRIVER( spacefev )
/*TODO*/ //	DRIVER( sfeverbw )
/*TODO*/ //	DRIVER( spclaser )
/*TODO*/ //	DRIVER( laser )
/*TODO*/ //	DRIVER( spcewarl )
/*TODO*/ //	DRIVER( polaris )	/* (c) 1980 Taito */
/*TODO*/ //	DRIVER( polarisa )	/* (c) 1980 Taito */
/*TODO*/ //	DRIVER( ballbomb )	/* (c) 1980 Taito */
/*TODO*/ //	DRIVER( m79amb )
/*TODO*/ //	DRIVER( alieninv )
/*TODO*/ //	DRIVER( sitv )
/*TODO*/ //	DRIVER( sicv )
/*TODO*/ //	DRIVER( sisv )
/*TODO*/ //	DRIVER( sisv2 )
/*TODO*/ //	DRIVER( spacewr3 )
/*TODO*/ //	DRIVER( invaderl )
/*TODO*/ //	DRIVER( yosakdon )
/*TODO*/ //	DRIVER( spceking )
/*TODO*/ //	DRIVER( spcewars )

    /* "Midway" Z80 b/w games */
    driver_astinvad,	/* (c) 1980 Stern */
    driver_kamikaze,	/* Leijac Corporation */
    driver_spaceint,	/* [1980] Shoei */

	/* Meadows S2650 games */
/*TODO*/ //	DRIVER( lazercmd )	/* [1976?] */
/*TODO*/ //	DRIVER( deadeye )	/* [1978?] */
/*TODO*/ //	DRIVER( gypsyjug )	/* [1978?] */
/*TODO*/ //	DRIVER( medlanes )	/* [1977?] */

	/* Midway "Astrocade" games */
/*TODO*/ //	DRIVER( wow )		/* (c) 1980 */
/*TODO*/ //	DRIVER( robby )		/* (c) 1981 */
/*TODO*/ //	DRIVER( gorf )		/* (c) 1981 */
/*TODO*/ //	DRIVER( gorfpgm1 )	/* (c) 1981 */
/*TODO*/ //	DRIVER( seawolf2 )
/*TODO*/ //	DRIVER( spacezap )	/* (c) 1980 */
/*TODO*/ //	DRIVER( ebases )

	/* Bally Midway MCR games */
	/* MCR1 */
/*TODO*/ //	DRIVER( solarfox )	/* (c) 1981 */
/*TODO*/ //	DRIVER( kick )		/* (c) 1981 */
/*TODO*/ //	DRIVER( kicka )		/* bootleg? */
	/* MCR2 */
/*TODO*/ //	DRIVER( shollow )	/* (c) 1981 */
/*TODO*/ //	DRIVER( shollow2 )	/* (c) 1981 */
/*TODO*/ //	DRIVER( tron )		/* (c) 1982 */
/*TODO*/ //	DRIVER( tron2 )		/* (c) 1982 */
/*TODO*/ //	DRIVER( kroozr )	/* (c) 1982 */
/*TODO*/ //	DRIVER( domino )	/* (c) 1982 */
/*TODO*/ //	DRIVER( wacko )		/* (c) 1982 */
/*TODO*/ //	DRIVER( twotiger )	/* (c) 1984 */
	/* MCR2 + MCR3 sprites */
/*TODO*/ //	DRIVER( journey )	/* (c) 1983 */
	/* MCR3 */
/*TODO*/ //	DRIVER( tapper )	/* (c) 1983 */
/*TODO*/ //	DRIVER( tappera )	/* (c) 1983 */
/*TODO*/ //	DRIVER( sutapper )	/* (c) 1983 */
/*TODO*/ //	DRIVER( rbtapper )	/* (c) 1984 */
/*TODO*/ //	DRIVER( timber )	/* (c) 1984 */
/*TODO*/ //	DRIVER( dotron )	/* (c) 1983 */
/*TODO*/ //	DRIVER( dotrone )	/* (c) 1983 */
/*TODO*/ //	DRIVER( destderb )	/* (c) 1984 */
/*TODO*/ //	DRIVER( destderm )	/* (c) 1984 */
/*TODO*/ //	DRIVER( sarge )		/* (c) 1985 */
/*TODO*/ //	DRIVER( rampage )	/* (c) 1986 */
/*TODO*/ //	DRIVER( rampage2 )	/* (c) 1986 */
/*TODO*/ //	DRIVER( powerdrv )	/* (c) 1986 */
/*TODO*/ //	DRIVER( maxrpm )	/* (c) 1986 */
/*TODO*/ //	DRIVER( spyhunt )	/* (c) 1983 */
/*TODO*/ //	DRIVER( turbotag )	/* (c) 1985 */
/*TODO*/ //	DRIVER( crater )	/* (c) 1984 */
	/* MCR 68000 */
/*TODO*/ //	DRIVER( zwackery )	/* (c) 1984 */
/*TODO*/ //	DRIVER( xenophob )	/* (c) 1987 */
/*TODO*/ //	DRIVER( spyhunt2 )	/* (c) 1987 */
/*TODO*/ //	DRIVER( blasted )	/* (c) 1988 */
/*TODO*/ //	DRIVER( archrivl )	/* (c) 1989 */
/*TODO*/ //	DRIVER( archriv2 )	/* (c) 1989 */
/*TODO*/ //	DRIVER( trisport )	/* (c) 1989 */
/*TODO*/ //	DRIVER( pigskin )	/* (c) 1990 */

	/* Bally / Sente games */
/*TODO*/ //	DRIVER( sentetst )
/*TODO*/ //	DRIVER( cshift )	/* (c) 1984 */
/*TODO*/ //	DRIVER( gghost )	/* (c) 1984 */
/*TODO*/ //	DRIVER( hattrick )	/* (c) 1984 */
/*TODO*/ //	DRIVER( otwalls )	/* (c) 1984 */
/*TODO*/ //	DRIVER( snakepit )	/* (c) 1984 */
/*TODO*/ //	DRIVER( snakjack )	/* (c) 1984 */
/*TODO*/ //	DRIVER( stocker )	/* (c) 1984 */
/*TODO*/ //	DRIVER( triviag1 )	/* (c) 1984 */
/*TODO*/ //	DRIVER( triviag2 )	/* (c) 1984 */
/*TODO*/ //	DRIVER( triviasp )	/* (c) 1984 */
/*TODO*/ //	DRIVER( triviayp )	/* (c) 1984 */
/*TODO*/ //	DRIVER( triviabb )	/* (c) 1984 */
/*TODO*/ //	DRIVER( gimeabrk )	/* (c) 1985 */
/*TODO*/ //	DRIVER( minigolf )	/* (c) 1985 */
/*TODO*/ //	DRIVER( minigol2 )	/* (c) 1985 */
/*TODO*/ //	DRIVER( toggle )	/* (c) 1985 */
/*TODO*/ //	DRIVER( nametune )	/* (c) 1986 */
/*TODO*/ //	DRIVER( nstocker )	/* (c) 1986 */
/*TODO*/ //	DRIVER( sfootbal )	/* (c) 1986 */
/*TODO*/ //	DRIVER( spiker )	/* (c) 1986 */
/*TODO*/ //	DRIVER( rescraid )	/* (c) 1987 */

	/* Irem games */
	/* trivia: IREM means "International Rental Electronics Machines" */
/*TODO*/ //	DRIVER( skychut )	/* (c) [1980] */
/*TODO*/ //	DRIVER( mpatrol )	/* (c) 1982 */
/*TODO*/ //	DRIVER( mpatrolw )	/* (c) 1982 + Williams license */
/*TODO*/ //	DRIVER( mranger )	/* bootleg */
            driver_troangel,	/* (c) 1983 */
/*TODO*/ //	DRIVER( yard )		/* (c) 1983 */
/*TODO*/ //	DRIVER( vsyard )	/* (c) 1983/1984 */
/*TODO*/ //	DRIVER( vsyard2 )	/* (c) 1983/1984 */
            driver_travrusa,	/* (c) 1983 */
            driver_motorace,	/* (c) 1983 Williams license */
	/* M62 */
            driver_kungfum,	/* (c) 1984 */
            driver_kungfud,	/* (c) 1984 + Data East license */
            driver_spartanx,	/* (c) 1984 */
            driver_kungfub,	/* bootleg */
            driver_kungfub2,	/* bootleg */
            driver_battroad,	/* (c) 1984 */
            driver_ldrun,		/* (c) 1984 licensed from Broderbund */
            driver_ldruna,	/* (c) 1984 licensed from Broderbund */
            driver_ldrun2,	/* (c) 1984 licensed from Broderbund */
            driver_ldrun3,	/* (c) 1985 licensed from Broderbund */
            driver_ldrun4,	/* (c) 1986 licensed from Broderbund */
            driver_lotlot,	/* (c) 1985 licensed from Tokuma Shoten */
            driver_kidniki,	/* (c) 1986 + Data East USA license */
            driver_yanchamr,	/* (c) 1986 (Japan) */
            driver_spelunkr,	/* (c) 1985 licensed from Broderbund */
            driver_spelunk2,	/* (c) 1986 licensed from Broderbund */

        driver_vigilant,	/* (c) 1988 (World) */
        driver_vigilntu,	/* (c) 1988 (US) */
        driver_vigilntj,	/* (c) 1988 (Japan) */
        driver_kikcubic,	/* (c) 1988 (Japan) */
	/* M72 (and derivatives) */
/*TODO*/ //	DRIVER( rtype )		/* (c) 1987 (Japan) */
/*TODO*/ //	DRIVER( rtypeu )	/* (c) 1987 + Nintendo USA license (US) */
/*TODO*/ //	DRIVER( rtypeb )	/* bootleg */
/*TODO*/ //	DRIVER( bchopper )	/* (c) 1987 */
/*TODO*/ //	DRIVER( mrheli )	/* (c) 1987 (Japan) */
/*TODO*/ //	DRIVER( nspirit )	/* (c) 1988 */
/*TODO*/ //	DRIVER( nspiritj )	/* (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( imgfight )	/* (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( loht )		/* (c) 1989 */
/*TODO*/ //	DRIVER( xmultipl )	/* (c) 1989 (Japan) */
/*TODO*/ //	DRIVER( dbreed )	/* (c) 1989 */
/*TODO*/ //	DRIVER( rtype2 )	/* (c) 1989 */
/*TODO*/ //	DRIVER( rtype2j )	/* (c) 1989 (Japan) */
/*TODO*/ //	DRIVER( majtitle )	/* (c) 1990 (Japan) */
/*TODO*/ //	DRIVER( hharry )	/* (c) 1990 (World) */
/*TODO*/ //	DRIVER( hharryu )	/* (c) 1990 Irem America (US) */
/*TODO*/ //	DRIVER( dkgensan )	/* (c) 1990 (Japan) */
/*TODO*/ //TESTDRIVER( kengo )
/*TODO*/ //	DRIVER( gallop )	/* (c) 1991 (Japan) */
/*TODO*/ //TESTDRIVER( poundfor )
	/* not M72, but same sound hardware */
/*TODO*/ //	DRIVER( sichuan2 )	/* (c) 1989 Tamtex */
/*TODO*/ //	DRIVER( sichuana )	/* (c) 1989 Tamtex */
/*TODO*/ //	DRIVER( shisen )	/* (c) 1989 Tamtex */
	/* M92 */
/*TODO*/ //	DRIVER( bmaster )	/* (c) 1991 Irem */
/*TODO*/ //	DRIVER( gunforce )	/* (c) 1991 Irem (World) */
/*TODO*/ //	DRIVER( gunforcu )	/* (c) 1991 Irem America (US) */
/*TODO*/ //	DRIVER( hook )		/* (c) 1992 Irem (World) */
/*TODO*/ //	DRIVER( hooku )		/* (c) 1992 Irem America (US) */
/*TODO*/ //	DRIVER( mysticri )	/* (c) 1992 Irem (World) */
/*TODO*/ //	DRIVER( gunhohki )	/* (c) 1992 Irem (Japan) */
/*TODO*/ //	DRIVER( uccops )	/* (c) 1992 Irem (World) */
/*TODO*/ //	DRIVER( uccopsj )	/* (c) 1992 Irem (Japan) */
/*TODO*/ //	DRIVER( rtypeleo )	/* (c) 1992 Irem (Japan) */
/*TODO*/ //	DRIVER( majtitl2 )	/* (c) 1992 Irem (World) */
/*TODO*/ //	DRIVER( skingame )	/* (c) 1992 Irem America (US) */
/*TODO*/ //	DRIVER( skingam2 )	/* (c) 1992 Irem America (US) */
/*TODO*/ //	DRIVER( inthunt )	/* (c) 1993 Irem (World) */
/*TODO*/ //	DRIVER( kaiteids )	/* (c) 1993 Irem (Japan) */
/*TODO*/ //TESTDRIVER( nbbatman )	/* (c) 1993 Irem America (US) */
/*TODO*/ //TESTDRIVER( leaguemn )	/* (c) 1993 Irem (Japan) */
/*TODO*/ //	DRIVER( lethalth )	/* (c) 1991 Irem (World) */
/*TODO*/ //	DRIVER( thndblst )	/* (c) 1991 Irem (Japan) */
/*TODO*/ //	DRIVER( psoldier )	/* (c) 1993 Irem (Japan) */
	/* M97 */
/*TODO*/ //TESTDRIVER( riskchal )
/*TODO*/ //TESTDRIVER( gussun )
/*TODO*/ //TESTDRIVER( shisen2 )
/*TODO*/ //TESTDRIVER( quizf1 )
/*TODO*/ //TESTDRIVER( atompunk )
/*TODO*/ //TESTDRIVER( bbmanw )
	/* M107 */
/*TODO*/ //TESTDRIVER( firebarr )	/* (c) 1993 Irem (Japan) */
/*TODO*/ //	DRIVER( dsoccr94 )	/* (c) 1994 Irem (Data East Corporation license) */

	/* Gottlieb/Mylstar games (Gottlieb became Mylstar in 1983) */
/*TODO*/ //	DRIVER( reactor )	/* GV-100 (c) 1982 Gottlieb */
/*TODO*/ //	DRIVER( mplanets )	/* GV-102 (c) 1983 Gottlieb */
/*TODO*/ //	DRIVER( qbert )		/* GV-103 (c) 1982 Gottlieb */
/*TODO*/ //	DRIVER( qbertjp )	/* GV-103 (c) 1982 Gottlieb + Konami license */
/*TODO*/ //	DRIVER( sqbert )	/* (c) 1983 Mylstar - never released */
/*TODO*/ //	DRIVER( krull )		/* GV-105 (c) 1983 Gottlieb */
/*TODO*/ //	DRIVER( mach3 )		/* GV-109 (c) 1983 Mylstar */
/*TODO*/ //	DRIVER( usvsthem )	/* GV-??? (c) 198? Mylstar */
/*TODO*/ //	DRIVER( 3stooges )	/* GV-113 (c) 1984 Mylstar */
/*TODO*/ //	DRIVER( qbertqub )	/* GV-119 (c) 1983 Mylstar */
/*TODO*/ //	DRIVER( curvebal )	/* GV-134 (c) 1984 Mylstar */

	/* older Taito games */
/*TODO*/ //	DRIVER( crbaloon )	/* (c) 1980 Taito Corporation */
/*TODO*/ //	DRIVER( crbalon2 )	/* (c) 1980 Taito Corporation */

	/* Taito "Qix hardware" games */
/*TODO*/ //	DRIVER( qix )		/* (c) 1981 Taito America Corporation */
/*TODO*/ //	DRIVER( qixa )		/* (c) 1981 Taito America Corporation */
/*TODO*/ //	DRIVER( qixb )		/* (c) 1981 Taito America Corporation */
/*TODO*/ //	DRIVER( qix2 )		/* (c) 1981 Taito America Corporation */
/*TODO*/ //	DRIVER( sdungeon )	/* (c) 1981 Taito America Corporation */
/*TODO*/ //	DRIVER( elecyoyo )	/* (c) 1982 Taito America Corporation */
/*TODO*/ //	DRIVER( elecyoy2 )	/* (c) 1982 Taito America Corporation */
/*TODO*/ //	DRIVER( kram )		/* (c) 1982 Taito America Corporation */
/*TODO*/ //	DRIVER( kram2 )		/* (c) 1982 Taito America Corporation */
/*TODO*/ //	DRIVER( zookeep )	/* (c) 1982 Taito America Corporation */
/*TODO*/ //	DRIVER( zookeep2 )	/* (c) 1982 Taito America Corporation */
/*TODO*/ //	DRIVER( zookeep3 )	/* (c) 1982 Taito America Corporation */

	/* Taito SJ System games */
/*TODO*/ //	DRIVER( spaceskr )	/* (c) 1981 Taito Corporation */
/*TODO*/ //	DRIVER( junglek )	/* (c) 1982 Taito Corporation */
/*TODO*/ //	DRIVER( junglkj2 )	/* (c) 1982 Taito Corporation */
/*TODO*/ //	DRIVER( jungleh )	/* (c) 1982 Taito America Corporation */
/*TODO*/ //	DRIVER( alpine )	/* (c) 1982 Taito Corporation */
/*TODO*/ //	DRIVER( alpinea )	/* (c) 1982 Taito Corporation */
/*TODO*/ //	DRIVER( timetunl )	/* (c) 1982 Taito Corporation */
/*TODO*/ //	DRIVER( wwestern )	/* (c) 1982 Taito Corporation */
/*TODO*/ //	DRIVER( wwester1 )	/* (c) 1982 Taito Corporation */
/*TODO*/ //	DRIVER( frontlin )	/* (c) 1982 Taito Corporation */
/*TODO*/ //	DRIVER( elevator )	/* (c) 1983 Taito Corporation */
/*TODO*/ //	DRIVER( elevatob )	/* bootleg */
/*TODO*/ //	DRIVER( tinstar )	/* (c) 1983 Taito Corporation */
/*TODO*/ //	DRIVER( waterski )	/* (c) 1983 Taito Corporation */
/*TODO*/ //	DRIVER( bioatack )	/* (c) 1983 Taito Corporation + Fox Video Games license */
/*TODO*/ //	DRIVER( hwrace )	/* (c) 1983 Taito Corporation */
/*TODO*/ //	DRIVER( sfposeid )	/* 1984 */
/*TODO*/ //	DRIVER( kikstart )

	/* other Taito games */
/*TODO*/ //	DRIVER( bking2 )	/* (c) 1983 Taito Corporation */
/*TODO*/ //	DRIVER( gsword )	/* (c) 1984 Taito Corporation */
/*TODO*/ //	DRIVER( tsamurai )	/* (c) 1985 Taito */
/*TODO*/ //	DRIVER( tsamura2 )	/* (c) 1985 Taito */
/*TODO*/ //	DRIVER( nunchaku )	/* (c) 1985 Taito */
/*TODO*/ //	DRIVER( yamagchi )	/* (c) 1985 Taito */
/*TODO*/ //TESTDRIVER( flstory )	/* (c) 1985 Taito Corporation */
/*TODO*/ //TESTDRIVER( flstoryj )	/* (c) 1985 Taito Corporation (Japan) */
/*TODO*/ //	DRIVER( gladiatr )	/* (c) 1986 Taito America Corporation (US) */
/*TODO*/ //	DRIVER( ogonsiro )	/* (c) 1986 Taito Corporation (Japan) */
/*TODO*/ //	DRIVER( bublbobl )	/* (c) 1986 Taito Corporation */
/*TODO*/ //	DRIVER( bublbobr )	/* (c) 1986 Taito America Corporation + Romstar license */
/*TODO*/ //	DRIVER( bubbobr1 )	/* (c) 1986 Taito America Corporation + Romstar license */
/*TODO*/ //	DRIVER( boblbobl )	/* bootleg */
/*TODO*/ //	DRIVER( sboblbob )	/* bootleg */
/*TODO*/ //	DRIVER( tokio )		/* 1986 */
/*TODO*/ //	DRIVER( tokiob )	/* bootleg */
/*TODO*/ //	DRIVER( kicknrun )	/* (c) 1986 Taito Corporation */
/*TODO*/ //	DRIVER( mexico86 )	/* bootleg (Micro Research) */
/*TODO*/ //	DRIVER( kikikai )	/* (c) 1986 Taito Corporation */
/*TODO*/ //	DRIVER( rastan )	/* (c) 1987 Taito Corporation Japan (World) */
/*TODO*/ //	DRIVER( rastanu )	/* (c) 1987 Taito America Corporation (US) */
/*TODO*/ //	DRIVER( rastanu2 )	/* (c) 1987 Taito America Corporation (US) */
/*TODO*/ //	DRIVER( rastsaga )	/* (c) 1987 Taito Corporation (Japan)*/
/*TODO*/ //	DRIVER( rainbow )	/* (c) 1987 Taito Corporation */
/*TODO*/ //	DRIVER( rainbowe )	/* (c) 1988 Taito Corporation */
/*TODO*/ //	DRIVER( jumping )	/* bootleg */
        driver_arkanoid,	/* (c) 1986 Taito Corporation Japan (World) */
/*TODO*/ //	DRIVER( arknoidu )	/* (c) 1986 Taito America Corporation + Romstar license (US) */
/*TODO*/ //	DRIVER( arknoidj )	/* (c) 1986 Taito Corporation (Japan) */
/*TODO*/ //	DRIVER( arkbl2 )	/* bootleg */
/*TODO*/ //TESTDRIVER( arkbl3 )	/* bootleg */
        driver_arkatayt,	/* bootleg */
/*TODO*/ //TESTDRIVER( arkblock )	/* bootleg */
        driver_arkbloc2,	/* bootleg */
/*TODO*/ //	DRIVER( arkangc )	/* bootleg */
        driver_superqix,	/* 1987 */
        driver_sqixbl,	/* bootleg? but (c) 1987 */
/*TODO*/ //	DRIVER( superman )	/* (c) 1988 Taito Corporation */
/*TODO*/ //TESTDRIVER( footchmp )	/* (c) 1990 Taito Corporation Japan (World) */
        driver_minivadr,	/* cabinet test board */

        
	/* Taito L-System games */
/*TODO*/ //	DRIVER( fhawk )		/* (c) 1988 Taito Corporation (Japan) */
/*TODO*/ //	DRIVER( raimais )	/* (c) 1988 Taito Corporation (Japan) */
/*TODO*/ //	DRIVER( champwr )	/* (c) 1989 Taito Corporation Japan (World) */
/*TODO*/ //	DRIVER( champwru )	/* (c) 1989 Taito America Corporation (US) */
/*TODO*/ //	DRIVER( champwrj )	/* (c) 1989 Taito Corporation (Japan) */
/*TODO*/ //	DRIVER( puzznic )	/* (c) 1989 Taito Corporation (Japan) */
/*TODO*/ //	DRIVER( plotting )	/* (c) 1989 Taito Corporation Japan (World) */
/*TODO*/ //	DRIVER( palamed )	/* (c) 1990 Taito Corporation (Japan) */
/*TODO*/ //	DRIVER( horshoes )	/* (c) 1990 Taito America Corporation (US) */
/*TODO*/ //	DRIVER( cachat )	/* (c) 1993 Taito Corporation (Japan) */

	/* Taito F2 games */
/*TODO*/ //	DRIVER( ssi )		/* (c) 1990 Taito Corporation Japan (World) */
	/* Majestic 12 (c) 1990 Taito America Corporation (US) */
/*TODO*/ //	DRIVER( majest12 )	/* (c) 1990 Taito Corporation (Japan) */
/*TODO*/ //TESTDRIVER( finalb )
/*TODO*/ //TESTDRIVER( megab )
/*TODO*/ //	DRIVER( liquidk )	/* (c) 1990 Taito Corporation Japan (World) */
/*TODO*/ //	DRIVER( liquidku )	/* (c) 1990 Taito America Corporation (US) */
/*TODO*/ //	DRIVER( mizubaku )	/* (c) 1990 Taito Corporation (Japan) */
/*TODO*/ //	DRIVER( growl )		/* (c) 1990 Taito Corporation Japan (World) */
/*TODO*/ //	DRIVER( growlu )	/* (c) 1990 Taito America Corporation (US) */
/*TODO*/ //	DRIVER( runark )	/* (c) 1990 Taito Corporation (Japan) */

	/* Toaplan games */
/*TODO*/ //	DRIVER( tigerh )	/* GX-551 [not a Konami board!] */
/*TODO*/ //	DRIVER( tigerh2 )	/* GX-551 [not a Konami board!] */
/*TODO*/ //	DRIVER( tigerhj )	/* GX-551 [not a Konami board!] */
/*TODO*/ //	DRIVER( tigerhb1 )	/* bootleg but (c) 1985 Taito Corporation */
/*TODO*/ //	DRIVER( tigerhb2 )	/* bootleg but (c) 1985 Taito Corporation */
/*TODO*/ //	DRIVER( slapfigh )	/* TP-??? */
/*TODO*/ //	DRIVER( slapbtjp )	/* bootleg but (c) 1986 Taito Corporation */
/*TODO*/ //	DRIVER( slapbtuk )	/* bootleg but (c) 1986 Taito Corporation */
/*TODO*/ //	DRIVER( alcon )		/* TP-??? */
/*TODO*/ //	DRIVER( getstar )
/*TODO*/ //	DRIVER( getstarj )
/*TODO*/ //	DRIVER( getstarb )	/* GX-006 bootleg but (c) 1986 Taito Corporation */

/*TODO*/ //	DRIVER( fshark )	/* TP-007 (c) 1987 Taito Corporation (World) */
/*TODO*/ //	DRIVER( skyshark )	/* TP-007 (c) 1987 Taito America Corporation + Romstar license (US) */
/*TODO*/ //	DRIVER( hishouza )	/* TP-007 (c) 1987 Taito Corporation (Japan) */
/*TODO*/ //	DRIVER( fsharkbt )	/* bootleg */
/*TODO*/ //	DRIVER( wardner )	/* TP-009 (c) 1987 Taito Corporation Japan (World) */
/*TODO*/ //	DRIVER( pyros )		/* TP-009 (c) 1987 Taito America Corporation (US) */
/*TODO*/ //	DRIVER( wardnerj )	/* TP-009 (c) 1987 Taito Corporation (Japan) */
/*TODO*/ //	DRIVER( twincobr )	/* TP-011 (c) 1987 Taito Corporation (World) */
/*TODO*/ //	DRIVER( twincobu )	/* TP-011 (c) 1987 Taito America Corporation + Romstar license (US) */
/*TODO*/ //	DRIVER( ktiger )	/* TP-011 (c) 1987 Taito Corporation (Japan) */

/*TODO*/ //	DRIVER( rallybik )	/* TP-012 (c) 1988 Taito */
/*TODO*/ //	DRIVER( truxton )	/* TP-013B (c) 1988 Taito */
/*TODO*/ //	DRIVER( hellfire )	/* TP-??? (c) 1989 Toaplan + Taito license */
/*TODO*/ //	DRIVER( zerowing )	/* TP-015 (c) 1989 Toaplan */
/*TODO*/ //	DRIVER( demonwld )	/* TP-016 (c) 1989 Toaplan + Taito license */
/*TODO*/ //	DRIVER( fireshrk )	/* TP-017 (c) 1990 Toaplan */
/*TODO*/ //	DRIVER( samesame )	/* TP-017 (c) 1989 Toaplan */
/*TODO*/ //	DRIVER( outzone )	/* TP-018 (c) 1990 Toaplan */
/*TODO*/ //	DRIVER( outzonep )	/* bootleg */
/*TODO*/ //	DRIVER( vimana )	/* TP-019 (c) 1991 Toaplan (+ Tecmo license when set to Japan) */
/*TODO*/ //	DRIVER( vimana2 )	/* TP-019 (c) 1991 Toaplan (+ Tecmo license when set to Japan)  */
/*TODO*/ //	DRIVER( vimanan )	/* TP-019 (c) 1991 Toaplan (+ Nova Apparate GMBH & Co license) */
/*TODO*/ //	DRIVER( snowbros )	/* MIN16-02 (c) 1990 Toaplan + Romstar license */
/*TODO*/ //	DRIVER( snowbroa )	/* MIN16-02 (c) 1990 Toaplan + Romstar license */
/*TODO*/ //	DRIVER( snowbrob )	/* MIN16-02 (c) 1990 Toaplan + Romstar license */
/*TODO*/ //	DRIVER( snowbroj )	/* MIN16-02 (c) 1990 Toaplan */

/*TODO*/ //	DRIVER( tekipaki )	/* TP-020 (c) 1991 Toaplan */
/*TODO*/ //	DRIVER( ghox )		/* TP-021 (c) 1991 Toaplan */
/*TODO*/ //	DRIVER( dogyuun )	/* TP-022 (c) 1992 Toaplan */
/*TODO*/ //	DRIVER( kbash )		/* TP-023 (c) 1993 Toaplan */
/*TODO*/ //TESTDRIVER( tatsujn2 )	/* TP-024 */
/*TODO*/ //	DRIVER( pipibibs )	/* TP-025 */
/*TODO*/ //TESTDRIVER( pipibibi )	/* bootleg */
/*TODO*/ //	DRIVER( whoopee )	/* TP-025 */
/*TODO*/ //TESTDRIVER( fixeight )	/* TP-026 (c) 1992 + Taito license */
/*TODO*/ //	DRIVER( vfive )		/* TP-027 (c) 1993 Toaplan */
/*TODO*/ //	DRIVER( batsugun )	/* TP-030 (c) 1993 Toaplan */
/*TODO*/ //	DRIVER( snowbro2 )	/* TP-??? (c) 1994 Hanafram */

	/* Williams games */
/*TODO*/ //	DRIVER( defender )	/* (c) 1980 */
/*TODO*/ //	DRIVER( defendg )	/* (c) 1980 */
/*TODO*/ //	DRIVER( defendw )	/* (c) 1980 */
/*TODO*/ //TESTDRIVER( defndjeu )	/* bootleg */
/*TODO*/ //	DRIVER( defcmnd )	/* bootleg */
/*TODO*/ //TESTDRIVER( defcomnd )	/* bootleg */
/*TODO*/ //	DRIVER( defence )	/* bootleg */
/*TODO*/ //	DRIVER( mayday )
/*TODO*/ //	DRIVER( maydaya )
/*TODO*/ //	DRIVER( colony7 )	/* (c) 1981 Taito */
/*TODO*/ //	DRIVER( colony7a )	/* (c) 1981 Taito */
/*TODO*/ //	DRIVER( stargate )	/* (c) 1981 */
/*TODO*/ //	DRIVER( robotron )	/* (c) 1982 */
/*TODO*/ //	DRIVER( robotryo )	/* (c) 1982 */
/*TODO*/ //	DRIVER( joust )		/* (c) 1982 */
/*TODO*/ //	DRIVER( joustr )	/* (c) 1982 */
/*TODO*/ //	DRIVER( joustwr )	/* (c) 1982 */
/*TODO*/ //	DRIVER( bubbles )	/* (c) 1982 */
/*TODO*/ //	DRIVER( bubblesr )	/* (c) 1982 */
/*TODO*/ //	DRIVER( splat )		/* (c) 1982 */
/*TODO*/ //	DRIVER( sinistar )	/* (c) 1982 */
/*TODO*/ //	DRIVER( sinista1 )	/* (c) 1982 */
/*TODO*/ //	DRIVER( sinista2 )	/* (c) 1982 */
/*TODO*/ //	DRIVER( blaster )	/* (c) 1983 */
/*TODO*/ //	DRIVER( mysticm )	/* (c) 1983 */
/*TODO*/ //	DRIVER( tshoot )	/* (c) 1984 */
/*TODO*/ //	DRIVER( inferno )	/* (c) 1984 */
/*TODO*/ //	DRIVER( joust2 )	/* (c) 1986 */

	/* Capcom games */
	/* The following is a COMPLETE list of the Capcom games up to 1997, as shown on */
	/* their web site. The list is sorted by production date. */

            driver_sonson,	/*  7/1984 (c) 1984 */
/*TODO*/ //	DRIVER( higemaru )	/*  9/1984 (c) 1984 */
            driver_1942,		/* 12/1984 (c) 1984 */
            driver_1942a,		/* 12/1984 (c) 1984 */
            driver_1942b,		/* 12/1984 (c) 1984 */
/*TODO*/ //	DRIVER( exedexes )	/*  2/1985 (c) 1985 */
/*TODO*/ //	DRIVER( savgbees )	/*  2/1985 (c) 1985 + Memetron license */

            driver_gng,		/*  9/1985 (c) 1985 */
            driver_gnga,		/*  9/1985 (c) 1985 */
            driver_gngt,		/*  9/1985 (c) 1985 */
            driver_makaimur,	/*  9/1985 (c) 1985 */
            driver_makaimuc,	/*  9/1985 (c) 1985 */
            driver_makaimug,	/*  9/1985 (c) 1985 */
            driver_diamond,	/* (c) 1989 KH Video (NOT A CAPCOM GAME but runs on GnG hardware) */
            driver_gunsmoke,	/* 11/1985 (c) 1985 (World) */
            driver_gunsmrom,	/* 11/1985 (c) 1985 + Romstar (US) */
            driver_gunsmoka,	/* 11/1985 (c) 1985 (US) */
            driver_gunsmokj,	/* 11/1985 (c) 1985 (Japan) */

            
/*TODO*/ //	DRIVER( srumbler )	/*  9/1986 (c) 1986 */
/*TODO*/ //	DRIVER( srumblr2 )	/*  9/1986 (c) 1986 */
/*TODO*/ //	DRIVER( rushcrsh )	/*  9/1986 (c) 1986 */
/*TODO*/ //	DRIVER( bionicc )	/*  3/1987 (c) 1987 (US) */
/*TODO*/ //	DRIVER( bionicc2 )	/*  3/1987 (c) 1987 (US) */
/*TODO*/ //	DRIVER( topsecrt )	/*  3/1987 (c) 1987 (Japan) */
/*TODO*/ //	DRIVER( sf1 )		/*  8/1987 (c) 1987 (World) */
/*TODO*/ //	DRIVER( sf1us )		/*  8/1987 (c) 1987 (US) */
/*TODO*/ //	DRIVER( sf1jp )		/*  8/1987 (c) 1987 (Japan) */
/*TODO*/ //	DRIVER( tigeroad )	/* 11/1987 (c) 1987 + Romstar (US) */
/*TODO*/ //	DRIVER( toramich )	/* 11/1987 (c) 1987 (Japan) */
/*TODO*/ //	DRIVER( f1dream )	/*  4/1988 (c) 1988 + Romstar */
/*TODO*/ //	DRIVER( f1dreamb )	/* bootleg */
/*TODO*/ //	DRIVER( lastduel )	/*  7/1988 (c) 1988 (US) */
/*TODO*/ //	DRIVER( lstduela )	/*  7/1988 (c) 1988 (US) */
/*TODO*/ //	DRIVER( lstduelb )	/* bootleg */
/*TODO*/ //	DRIVER( madgear )	/*  2/1989 (c) 1989 (US) */
/*TODO*/ //	DRIVER( madgearj )	/*  2/1989 (c) 1989 (Japan) */
/*TODO*/ //	DRIVER( ledstorm )	/*  2/1989 (c) 1989 (US) */

	/* Capcom CPS1 games */
/*TODO*/ //	DRIVER( forgottn )	/*  7/1988 (c) 1988 (US) */
/*TODO*/ //	DRIVER( lostwrld )	/*  7/1988 (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( ghouls )	/* 12/1988 (c) 1988 (World) */
/*TODO*/ //	DRIVER( ghoulsu )	/* 12/1988 (c) 1988 (US) */
/*TODO*/ //	DRIVER( ghoulsj )	/* 12/1988 (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( strider )	/*  3/1989 (c) 1989 */
/*TODO*/ //	DRIVER( striderj )	/*  3/1989 (c) 1989 */
/*TODO*/ //	DRIVER( stridrja )	/*  3/1989 (c) 1989 */
/*TODO*/ //	DRIVER( dwj )		/*  4/1989 (c) 1989 (Japan) */
/*TODO*/ //	DRIVER( willow )	/*  6/1989 (c) 1989 (Japan) */
/*TODO*/ //	DRIVER( willowj )	/*  6/1989 (c) 1989 (Japan) */
/*TODO*/ //	DRIVER( unsquad )	/*  8/1989 (c) 1989 */
/*TODO*/ //	DRIVER( area88 )	/*  8/1989 (c) 1989 */
/*TODO*/ //	DRIVER( ffight )	/* 12/1989 (c) (World) */
/*TODO*/ //	DRIVER( ffightu )	/* 12/1989 (c) (US)    */
/*TODO*/ //	DRIVER( ffightj )	/* 12/1989 (c) (Japan) */
/*TODO*/ //	DRIVER( 1941 )		/*  2/1990 (c) 1990 (World) */
/*TODO*/ //	DRIVER( 1941j )		/*  2/1990 (c) 1990 (Japan) */
/*TODO*/ //	DRIVER( mercs )		/*  3/ 2/1990 (c) 1990 (World) */
/*TODO*/ //	DRIVER( mercsu )	/*  3/ 2/1990 (c) 1990 (US)    */
/*TODO*/ //	DRIVER( mercsj )	/*  3/ 2/1990 (c) 1990 (Japan) */
/*TODO*/ //	DRIVER( mtwins )	/*  6/19/1990 (c) 1990 (World) */
/*TODO*/ //	DRIVER( chikij )	/*  6/19/1990 (c) 1990 (Japan) */
/*TODO*/ //	DRIVER( msword )	/*  7/25/1990 (c) 1990 (World) */
/*TODO*/ //	DRIVER( mswordu )	/*  7/25/1990 (c) 1990 (US)    */
/*TODO*/ //	DRIVER( mswordj )	/*  6/23/1990 (c) 1990 (Japan) */
/*TODO*/ //	DRIVER( cawing )	/* 10/12/1990 (c) 1990 (World) */
/*TODO*/ //	DRIVER( cawingj )	/* 10/12/1990 (c) 1990 (Japan) */
/*TODO*/ //	DRIVER( nemo )		/* 11/30/1990 (c) 1990 (World) */
/*TODO*/ //	DRIVER( nemoj )		/* 11/20/1990 (c) 1990 (Japan) */
/*TODO*/ //	DRIVER( sf2 )		/*  2/14/1991 (c) 1991 (World) */
/*TODO*/ //	DRIVER( sf2a )		/*  2/ 6/1991 (c) 1991 (US)    */
/*TODO*/ //	DRIVER( sf2b )		/*  2/14/1991 (c) 1991 (US)    */
/*TODO*/ //	DRIVER( sf2e )		/*  2/28/1991 (c) 1991 (US)    */
/*TODO*/ //	DRIVER( sf2j )		/* 12/10/1991 (c) 1991 (Japan) */
/*TODO*/ //	DRIVER( sf2jb )		/*  2/14/1991 (c) 1991 (Japan) */
/*TODO*/ //	DRIVER( 3wonders )	/*  5/20/1991 (c) 1991 (US) */
/*TODO*/ //	DRIVER( wonder3 )	/*  5/20/1991 (c) 1991 (Japan) */
/*TODO*/ //	DRIVER( kod )		/*  7/11/1991 (c) 1991 (World) */
/*TODO*/ //	DRIVER( kodj )		/*  8/ 5/1991 (c) 1991 (Japan) */
/*TODO*/ //	DRIVER( kodb )		/* bootleg */
/*TODO*/ //	DRIVER( captcomm )	/* 10/14/1991 (c) 1991 (World) */
/*TODO*/ //	DRIVER( captcomu )	/*  9/28/1991 (c) 1991 (US)    */
/*TODO*/ //	DRIVER( captcomj )	/* 12/ 2/1991 (c) 1991 (Japan) */
/*TODO*/ //	DRIVER( knights )	/* 11/27/1991 (c) 1991 (World) */
/*TODO*/ //	DRIVER( knightsj )	/* 11/27/1991 (c) 1991 (Japan) */
/*TODO*/ //	DRIVER( sf2ce )		/*  3/13/1992 (c) 1992 (World) */
/*TODO*/ //	DRIVER( sf2cea )	/*  3/13/1992 (c) 1992 (US)    */
/*TODO*/ //	DRIVER( sf2ceb )	/*  5/13/1992 (c) 1992 (US)    */
/*TODO*/ //	DRIVER( sf2cej )	/*  5/13/1992 (c) 1992 (Japan) */
/*TODO*/ //	DRIVER( sf2rb )		/* hack */
/*TODO*/ //	DRIVER( sf2red )	/* hack */
/*TODO*/ //	DRIVER( sf2accp2 )	/* hack */
/*TODO*/ //	DRIVER( varth )		/*  6/12/1992 (c) 1992 (World) */
/*TODO*/ //	DRIVER( varthu )	/*  6/12/1992 (c) 1992 (US) */
/*TODO*/ //	DRIVER( varthj )	/*  7/14/1992 (c) 1992 (Japan) */
/*TODO*/ //	DRIVER( cworld2j )	/*  6/11/1992 (QUIZ 5) (c) 1992 (Japan) */
/*TODO*/ //	DRIVER( wof )		/* 10/ 2/1992 (c) 1992 (World) (CPS1 + QSound) */
/*TODO*/ //	DRIVER( wofj )		/* 10/31/1992 (c) 1992 (Japan) (CPS1 + QSound) */
/*TODO*/ //	DRIVER( sf2t )		/* 12/ 9/1992 (c) 1992 (US)    */
/*TODO*/ //	DRIVER( sf2tj )		/* 12/ 9/1992 (c) 1992 (Japan) */
/*TODO*/ //	DRIVER( dino )		/*  2/ 1/1993 (c) 1993 (World) (CPS1 + QSound) */
/*TODO*/ //	DRIVER( dinoj )		/*  2/ 1/1993 (c) 1993 (Japan) (CPS1 + QSound) */
/*TODO*/ //	DRIVER( punisher )	/*  4/22/1993 (c) 1993 (World) (CPS1 + QSound) */
/*TODO*/ //	DRIVER( punishru )	/*  4/22/1993 (c) 1993 (US)    (CPS1 + QSound) */
/*TODO*/ //	DRIVER( punishrj )	/*  4/22/1993 (c) 1993 (Japan) (CPS1 + QSound) */
/*TODO*/ //	DRIVER( slammast )	/*  7/13/1993 (c) 1993 (World) (CPS1 + QSound) */
/*TODO*/ //	DRIVER( mbomberj )	/*  7/13/1993 (c) 1993 (Japan) (CPS1 + QSound) */
/*TODO*/ //	DRIVER( mbombrd )	/* 12/ 6/1993 (c) 1993 (World) (CPS1 + QSound) */
/*TODO*/ //	DRIVER( mbombrdj )	/* 12/ 6/1993 (c) 1993 (Japan) (CPS1 + QSound) */
/*TODO*/ //	DRIVER( pnickj )	/*  6/ 8/1994 (c) 1994 + Compile license (Japan) not listed on Capcom's site */
/*TODO*/ //	DRIVER( qad )		/*  7/ 1/1992 (c) 1992 (US)    */
/*TODO*/ //	DRIVER( qadj )		/*  9/21/1994 (c) 1994 (Japan) */
/*TODO*/ //	DRIVER( qtono2 )	/*  1/23/1995 (c) 1995 (Japan) */
/*TODO*/ //	DRIVER( pang3 )		/*  5/11/1995 (c) 1995 Mitchell (Japan) not listed on Capcom's site */
/*TODO*/ //	DRIVER( megaman )	/* 10/ 6/1995 (c) 1995 (Asia)  */
/*TODO*/ //	DRIVER( rockmanj )	/*  9/22/1995 (c) 1995 (Japan) */
/*TODO*/ ////	DRIVER( sfzch )		/* 10/20/1995 (c) 1995 (Japan) (CPS Changer) */

	/* Capcom CPS2 games */
	/* list completed by CPS2Shock */
	/* http://cps2shock.retrogames.com */
/*TODO*/ //TESTDRIVER( ssf2 )		/* Super Street Fighter 2: The New Challengers (USA 930911) */
/*TODO*/ //TESTDRIVER( ssf2a )		/* Super Street Fighter 2: The New Challengers (Asia 930911) */
/*TODO*/ //TESTDRIVER( ssf2j )		/* Super Street Fighter 2: The New Challengers (Japan 930910) */
/*TODO*/ //TESTDRIVER( ecofe )		/* Eco Fighters (Etc 931203) */
/*TODO*/ //TESTDRIVER( ddtod )		/* Dungeons & Dragons: Tower of Doom (USA 940113) */
/*TODO*/ //TESTDRIVER( ddtoda )	/* Dungeons & Dragons: Tower of Doom (Asia 940113) */
/*TODO*/ //TESTDRIVER( ddtodr1 )	/* Dungeons & Dragons: Tower of Doom (USA 940125) */
/*TODO*/ //TESTDRIVER( ssf2t )		/* Super Street Fighter 2 Turbo (USA 940223) */
/*TODO*/ //TESTDRIVER( ssf2xj )	/* Super Street Fighter 2 X: Grand Master Challenge (Japan 940223) */
/*TODO*/ //TESTDRIVER( avsp )		/* Aliens Vs. Predator (USA 940520) */
/*TODO*/ //TESTDRIVER( vampj )		/* Vampire: The Night Warriors (Japan 940705) */
/*TODO*/ //TESTDRIVER( vampa )		/* Vampire: The Night Warriors (Asia 940705) */
/*TODO*/ //TESTDRIVER( dstlk )		/* DarkStalkers: The Night Warriors (USA 940818) */
/*TODO*/ //TESTDRIVER( slam2e )	/* Saturday Night Slammasters II: Ring of Destruction (Euro 940902) */
/*TODO*/ //TESTDRIVER( armwara )	/* Armoured Warriors (Asia 940920) */
/*TODO*/ //TESTDRIVER( xmcotaj )	/* X-Men: Children of the Atom (Japan 941219) */
/*TODO*/ //TESTDRIVER( xmcota )	/* X-Men: Children of the Atom (USA 950105) */
/*TODO*/ //TESTDRIVER( vhuntj )	/* Vampire Hunter: Darkstalkers 2 (Japan 950302) */
/*TODO*/ //TESTDRIVER( nwarr )		/* Night Warriors: DarkStalkers Revenge (USA 950406) */
/*TODO*/ //TESTDRIVER( cybotsj )	/* Cyberbots: Full Metal Madness (Japan 950420) */
/*TODO*/ //TESTDRIVER( sfa )		/* Street Fighter Alpha: The Warriors Dream (USA 950627) */
/*TODO*/ //TESTDRIVER( sfar1 )		/* Street Fighter Alpha: The Warriors Dream (USA 950727) */
/*TODO*/ //TESTDRIVER( sfzj )		/* Street Fighter Zero (Japan 950627) */
/*TODO*/ //TESTDRIVER( sfzjr1 )	/* Street Fighter Zero (Japan 950727) */
/*TODO*/ //TESTDRIVER( msh )		/* Marvel Super Heroes (USA 951024) */
/*TODO*/ //TESTDRIVER( 19xx )		/* 19XX: The Battle Against Destiny (USA 951207) */
/*TODO*/ //TESTDRIVER( ddsom )		/* Dungeons & Dragons 2: Shadow over Mystara (USA 960209) */
/*TODO*/ //TESTDRIVER( sfz2j )		/* Street Fighter Zero 2 (Japan 960227) */
/*TODO*/ //TESTDRIVER( spf2xj )	/* Super Puzzle Fighter 2 X (Japan 960531) */
/*TODO*/ //TESTDRIVER( spf2t )		/* Super Puzzle Fighter 2 Turbo (USA 960620) */
/*TODO*/ //TESTDRIVER( rckman2j )	/* Rockman 2: The Power Fighters (Japan 960708) */
/*TODO*/ //TESTDRIVER( sfz2a )		/* Street Fighter Zero 2 Alpha (Japan 960805) */
						/*  9/1996 Quiz Naneiro Dreams */
/*TODO*/ //TESTDRIVER( xmvsf )		/* X-Men Vs. Street Fighter (USA 961004) */
/*TODO*/ //TESTDRIVER( batcirj )	/* Battle Circuit (Japan 970319) */
/*TODO*/ //TESTDRIVER( batcira )	/* Battle Circuit (Asia 970319) */
/*TODO*/ //TESTDRIVER( vsav )		/* Vampire Savior: The Lord of Vampire (USA 970519) */
/*TODO*/ //TESTDRIVER( vsavj )		/* Vampire Savior: The Lord of Vampire (Japan 970519) */
/*TODO*/ //TESTDRIVER( mshvsf )	/* Marvel Super Heroes Vs. Street Fighter (USA 970625) */
/*TODO*/ //TESTDRIVER( mshvsfj )	/* Marvel Super Heroes Vs. Street Fighter (Japan 970707) */
/*TODO*/ //TESTDRIVER( vhunt2 )	/* Vampire Hunter 2: Darkstalkers Revenge (Japan 970828) */
/*TODO*/ //TESTDRIVER( sgemf )		/* Super Gem Fighter Mini Mix (USA 970904) */
/*TODO*/ //TESTDRIVER( pfghtj )	/* Pocket Fighter (Japan 970904) */
/*TODO*/ //TESTDRIVER( vsav2 )		/* Vampire Savior 2: The Lord of Vampire (Japan 970913) */
/*TODO*/ //TESTDRIVER( mvsc )		/* Marvel Super Heroes vs. Capcom: Clash of Super Heroes (USA 980123) */
/*TODO*/ //TESTDRIVER( sfa3 )		/* Street Fighter Alpha 3 (USA 980629) */
						/* 1999 Giga Wing */
						/* Gulum Pa! */

	/* Capcom ZN1/ZN2 games */
/*TODO*/ //TESTDRIVER( ts2j )		/*  Battle Arena Toshinden 2 (JAPAN 951124) */
						/*  7/1996 Star Gladiator */
/*TODO*/ //TESTDRIVER( sfex )		/*  Street Fighter EX (ASIA 961219) */
/*TODO*/ //TESTDRIVER( sfexj )		/*  Street Fighter EX (JAPAN 961130) */
/*TODO*/ //TESTDRIVER( sfexp )		/*  Street Fighter EX Plus (USA 970311) */
/*TODO*/ //TESTDRIVER( sfexpj )	/*  Street Fighter EX Plus (JAPAN 970311) */
/*TODO*/ //TESTDRIVER( rvschool )	/*  Rival Schools (ASIA 971117) */
/*TODO*/ //TESTDRIVER( jgakuen )	/*  Justice Gakuen (JAPAN 971117) */
/*TODO*/ //TESTDRIVER( sfex2 )		/*  Street Fighter EX 2 (JAPAN 980312) */
/*TODO*/ //TESTDRIVER( tgmj )		/*  Tetris The Grand Master (JAPAN 980710) */
/*TODO*/ //TESTDRIVER( sfex2p )	/*  Street Fighter EX 2 Plus (JAPAN 990611) */
						/*  Star Gladiator 2 */
						/*  Rival Schools 2 */
	/* Mitchell games */
        driver_mgakuen,	/* (c) 1988 Yuga */
        driver_mgakuen2,	/* (c) 1989 Face */
        driver_pkladies,	/* (c) 1989 Mitchell */
        driver_dokaben,	/*  3/1989 (c) 1989 Capcom (Japan) */
	/*  8/1989 Dokaben 2 (baseball) */
        driver_pang,		/* (c) 1989 Mitchell (World) */
        driver_pangb,		/* bootleg */
        driver_bbros,		/* (c) 1989 Capcom (US) not listed on Capcom's site */
        driver_pompingw,	/* (c) 1989 Mitchell (Japan) */
  /*TODO*///      driver_cbasebal,	/* 10/1989 (c) 1989 Capcom (Japan) (different hardware) */
        driver_cworld,	/* 11/1989 (QUIZ 1) (c) 1989 Capcom */
        driver_hatena,/*  2/28/1990 (QUIZ 2) (c) 1990 Capcom (Japan) */
        driver_spang,	/*  9/14/1990 (c) 1990 Mitchell (World) */
        driver_sbbros,	/* 10/ 1/1990 (c) 1990 Mitchell + Capcom (US) not listed on Capcom's site */
        driver_marukin,	/* 10/17/1990 (c) 1990 Yuga (Japan) */
        driver_qtono1,	/* 12/25/1990 (QUIZ 3) (c) 1991 Capcom (Japan) */
	/*  4/1991 Ashita Tenki ni Naare (golf) */
        driver_qsangoku,	/*  6/ 7/1991 (QUIZ 4) (c) 1991 Capcom (Japan) */
        driver_block, 	/*  9/10/1991 (c) 1991 Capcom (World) */
        driver_blockj,	/*  9/10/1991 (c) 1991 Capcom (Japan) */
        driver_blockbl,	/* bootleg */

	/* Incredible Technologies games */
/*TODO*/ //	DRIVER( capbowl )	/* (c) 1988 Incredible Technologies */
/*TODO*/ //	DRIVER( capbowl2 )	/* (c) 1988 Incredible Technologies */
/*TODO*/ //	DRIVER( clbowl )	/* (c) 1989 Incredible Technologies */
/*TODO*/ //	DRIVER( bowlrama )	/* (c) 1991 P & P Marketing */

	/* Leland games */
/*TODO*/ //TESTDRIVER( mayhem )	/* (c) 1985 Cinematronics */
/*TODO*/ //TESTDRIVER( wseries )	/* (c) 1985 Cinematronics Inc. */
/*TODO*/ //TESTDRIVER( dangerz )	/* (c) 1986 Cinematronics USA Inc. */
/*TODO*/ //TESTDRIVER( basebal2 )	/* (c) 1987 Cinematronics Inc. */
/*TODO*/ //TESTDRIVER( dblplay )	/* (c) 1987 Tradewest / The Leland Corp. */
/*TODO*/ //TESTDRIVER( teamqb )	/* (c) 1988 Leland Corp. */
/*TODO*/ //TESTDRIVER( strkzone )	/* (c) 1988 The Leland Corporation */
/*TODO*/ //TESTDRIVER( offroad )	/* (c) 1989 Leland Corp. */
/*TODO*/ //TESTDRIVER( offroadt )
/*TODO*/ //TESTDRIVER( pigout )	/* (c) 1990 The Leland Corporation */
/*TODO*/ //TESTDRIVER( pigoutj )	/* (c) 1990 The Leland Corporation */
/*TODO*/ //TESTDRIVER( redlin2p )
/*TODO*/ //TESTDRIVER( viper )
/*TODO*/ //TESTDRIVER( aafb )
/*TODO*/ //TESTDRIVER( aafb2p )
/*TODO*/ //TESTDRIVER( aafbu )
/*TODO*/ //TESTDRIVER( alleymas )
/*TODO*/ //TESTDRIVER( cerberus )
/*TODO*/ //TESTDRIVER( ataxx )
/*TODO*/ //TESTDRIVER( ataxxa )
/*TODO*/ //TESTDRIVER( indyheat )
/*TODO*/ //TESTDRIVER( wsf )

	/* Gremlin 8080 games */
	/* the numbers listed are the range of ROM part numbers */
/*TODO*/ //	DRIVER( blockade )	/* 1-4 [1977 Gremlin] */
/*TODO*/ //	DRIVER( comotion )	/* 5-7 [1977 Gremlin] */
/*TODO*/ //	DRIVER( hustle )	/* 16-21 [1977 Gremlin] */
/*TODO*/ //	DRIVER( blasto )	/* [1978 Gremlin] */

	/* Gremlin/Sega "VIC dual game board" games */
	/* the numbers listed are the range of ROM part numbers */
/*TODO*/ //	DRIVER( depthch )	/* 50-55 [1977 Gremlin?] */
/*TODO*/ //	DRIVER( safari )	/* 57-66 [1977 Gremlin?] */
/*TODO*/ //	DRIVER( frogs )		/* 112-119 [1978 Gremlin?] */
/*TODO*/ //	DRIVER( sspaceat )	/* 155-162 (c) */
/*TODO*/ //	DRIVER( sspacatc )	/* 139-146 (c) */
/*TODO*/ //	DRIVER( headon )	/* 163-167/192-193 (c) Gremlin */
/*TODO*/ //	DRIVER( headonb )	/* 163-167/192-193 (c) Gremlin */
/*TODO*/ //	DRIVER( headon2 )	/* ???-??? (c) 1979 Sega */
	/* ???-??? Fortress */
	/* ???-??? Gee Bee */
	/* 255-270  Head On 2 / Deep Scan */
/*TODO*/ //	DRIVER( invho2 )	/* 271-286 (c) 1979 Sega */
/*TODO*/ //	DRIVER( samurai )	/* 289-302 + upgrades (c) 1980 Sega */
/*TODO*/ //	DRIVER( invinco )	/* 310-318 (c) 1979 Sega */
/*TODO*/ //	DRIVER( invds )		/* 367-382 (c) 1979 Sega */
/*TODO*/ //	DRIVER( tranqgun )	/* 413-428 (c) 1980 Sega */
	/* 450-465  Tranquilizer Gun (different version?) */
	/* ???-??? Car Hunt / Deep Scan */
/*TODO*/ //	DRIVER( spacetrk )	/* 630-645 (c) 1980 Sega */
/*TODO*/ //	DRIVER( sptrekct )	/* (c) 1980 Sega */
/*TODO*/ //	DRIVER( carnival )	/* 651-666 (c) 1980 Sega */
/*TODO*/ //	DRIVER( carnvckt )	/* 501-516 (c) 1980 Sega */
/*TODO*/ //	DRIVER( digger )	/* 684-691 no copyright notice */
/*TODO*/ //	DRIVER( pulsar )	/* 790-805 (c) 1981 Sega */
/*TODO*/ //	DRIVER( heiankyo )	/* (c) [1979?] Denki Onkyo */

	/* Sega G-80 vector games */
/*TODO*/ //	DRIVER( spacfury )	/* (c) 1981 */
/*TODO*/ //	DRIVER( spacfura )	/* no copyright notice */
/*TODO*/ //	DRIVER( zektor )	/* (c) 1982 */
/*TODO*/ //	DRIVER( tacscan )	/* (c) */
/*TODO*/ //	DRIVER( elim2 )		/* (c) 1981 Gremlin */
/*TODO*/ //	DRIVER( elim2a )	/* (c) 1981 Gremlin */
/*TODO*/ //	DRIVER( elim4 )		/* (c) 1981 Gremlin */
/*TODO*/ //	DRIVER( startrek )	/* (c) 1982 */

	/* Sega G-80 raster games */
/*TODO*/ //	DRIVER( astrob )	/* (c) 1981 */
/*TODO*/ //	DRIVER( astrob1 )	/* (c) 1981 */
/*TODO*/ //	DRIVER( 005 )		/* (c) 1981 */
/*TODO*/ //	DRIVER( monsterb )	/* (c) 1982 */
/*TODO*/ //	DRIVER( spaceod )	/* (c) 1981 */
/*TODO*/ //	DRIVER( pignewt )	/* (c) 1983 */
/*TODO*/ //	DRIVER( pignewta )	/* (c) 1983 */
/*TODO*/ //	DRIVER( sindbadm )	/* 834-5244 (c) 1983 Sega */

        /* Sega "Zaxxon hardware" games */
        driver_zaxxon,	/* (c) 1982 */
        driver_zaxxon2,	/* (c) 1982 */
        driver_zaxxonb,	/* bootleg */
        driver_szaxxon,	/* (c) 1982 */
        driver_futspy,	/* (c) 1984 */
        driver_razmataz,	/* modified 834-0213, 834-0214 (c) 1983 */
        driver_congo,	/* 605-5167 (c) 1983 */
        driver_tiptop,	/* 605-5167 (c) 1983 */

	/* Sega System 1 / System 2 games */
        driver_starjack,	/* 834-5191 (c) 1983 (S1) */
        driver_starjacs,	/* (c) 1983 Stern (S1) */
        driver_regulus,	/* 834-5328(c) 1983 (S1) */
	driver_regulusu,	/* 834-5328(c) 1983 (S1) */
	driver_upndown,	/* (c) 1983 (S1) */
	driver_mrviking,	/* 834-5383 (c) 1984 (S1) */
	driver_mrvikinj,	/* 834-5383 (c) 1984 (S1) */
	driver_swat,		/* 834-5388 (c) 1984 Coreland / Sega (S1) */
	driver_flicky,	/* (c) 1984 (S1) */
	driver_flicky2,	/* (c) 1984 (S1) */
	/* Water Match (S1) */
	driver_bullfgtj,	/* 834-5478 (c) 1984 Sega / Coreland (S1) */
	driver_pitfall2,	/* 834-5627 [1985?] reprogrammed, (c) 1984 Activision (S1) */
	driver_pitfallu,	/* 834-5627 [1985?] reprogrammed, (c) 1984 Activision (S1) */
	driver_seganinj,	/* 834-5677 (c) 1985 (S1) */
	driver_seganinu,	/* 834-5677 (c) 1985 (S1) */
	driver_nprinces,	/* 834-5677 (c) 1985 (S1) */
	driver_nprincsu,	/* 834-5677 (c) 1985 (S1) */
	driver_nprincsb,	/* bootleg? (S1) */
	driver_imsorry,	/* 834-5707 (c) 1985 Coreland / Sega (S1) */
	driver_imsorryj,	/* 834-5707 (c) 1985 Coreland / Sega (S1) */
	driver_teddybb,	/* 834-5712 (c) 1985 (S1) */
	driver_hvymetal,	/* 834-5745 (c) 1985 (S2?) */
	driver_myhero,	/* 834-5755 (c) 1985 (S1) */
	driver_myheroj,	/* 834-5755 (c) 1985 Coreland / Sega (S1) */
	driver_myherok,	/* 834-5755 (c) 1985 Coreland / Sega (S1) */
	driver_shtngmst,	/* 834-5719/5720 (c) 1985 (S2) */
        driver_chplft,	/* 834-5795 (c) 1985, (c) 1982 Dan Gorlin (S2) */
        driver_chplftb,	/* 834-5795 (c) 1985, (c) 1982 Dan Gorlin (S2) */
	driver_chplftbl,	/* bootleg (S2) */
	driver_4dwarrio,	/* 834-5918 (c) 1985 Coreland / Sega (S1) */
	driver_brain,		/* (c) 1986 Coreland / Sega (S2?) */
        driver_wboy,		/* 834-5984 (c) 1986 + Escape license (S1) */
	driver_wboy2,		/* 834-5984 (c) 1986 + Escape license (S1) */
	driver_wboy3,
	driver_wboy4,		/* 834-5984 (c) 1986 + Escape license (S1) */
	driver_wboyu,		/* 834-5753 (? maybe a conversion) (c) 1986 + Escape license (S1) */
	driver_wboy4u,	/* 834-5984 (c) 1986 + Escape license (S1) */
	driver_wbdeluxe,	/* (c) 1986 + Escape license (S1) */
	driver_gardia,	/* 834-6119 (S2?) */
	driver_gardiab,	/* bootleg */
	driver_blockgal,	/* 834-6303 (S1) */
	driver_blckgalb,	/* bootleg */
	driver_tokisens,	/* (c) 1987 (from a bootleg board) (S2) */
        driver_wbml,		/* bootleg (S2) */
        driver_wbmlj,		/* (c) 1987 Sega/Westone (S2) */
	driver_wbmlj2,	/* (c) 1987 Sega/Westone (S2) */
	driver_wbmlju,	/* bootleg? (S2) */
	driver_dakkochn,	/* 836-6483? (S2) */
	driver_ufosensi,	/* 834-6659 (S2) */

	/* other Sega 8-bit games */
/*TODO*/ //	DRIVER( turbo )		/* (c) 1981 Sega */
/*TODO*/ //	DRIVER( turboa )	/* (c) 1981 Sega */
/*TODO*/ //	DRIVER( turbob )	/* (c) 1981 Sega */
/*TODO*/ //TESTDRIVER( kopunch )	/* 834-0103 (c) 1981 Sega */
/*TODO*/ //	DRIVER( suprloco )	/* (c) 1982 Sega */
/*TODO*/ //	DRIVER( champbas )	/* (c) 1983 Sega */
/*TODO*/ //	DRIVER( champbb2 )
/*TODO*/ //	DRIVER( appoooh )	/* (c) 1984 Sega */
        driver_bankp,		/* (c) 1984 Sega */
/*TODO*/ //	DRIVER( dotrikun )	/* cabinet test board */
/*TODO*/ //	DRIVER( dotriku2 )	/* cabinet test board */

	/* Sega System 16 games */
	// Not working
/*TODO*/ //	DRIVER( alexkidd )	/* (c) 1986 (protected) */
/*TODO*/ //	DRIVER( aliensya )	/* (c) 1987 (protected) */
/*TODO*/ //	DRIVER( aliensyb )	/* (c) 1987 (protected) */
/*TODO*/ //	DRIVER( aliensyj )	/* (c) 1987 (protected. Japan) */
/*TODO*/ //	DRIVER( astorm )	/* (c) 1990 (protected) */
/*TODO*/ //	DRIVER( astorm2p )	/* (c) 1990 (protected 2 Players) */
/*TODO*/ //	DRIVER( auraila )	/* (c) 1990 Sega / Westone (protected) */
/*TODO*/ //	DRIVER( bayrouta )	/* (c) 1989 (protected) */
/*TODO*/ //	DRIVER( bayrtbl1 )	/* (c) 1989 (protected) (bootleg) */
/*TODO*/ //	DRIVER( bayrtbl2 )	/* (c) 1989 (protected) (bootleg) */
/*TODO*/ //	DRIVER( enduror )	/* (c) 1985 (protected) */
/*TODO*/ //	DRIVER( eswat )		/* (c) 1989 (protected) */
/*TODO*/ //	DRIVER( fpoint )	/* (c) 1989 (protected) */
/*TODO*/ //	DRIVER( goldnaxb )	/* (c) 1989 (protected) */
/*TODO*/ //	DRIVER( goldnaxc )	/* (c) 1989 (protected) */
/*TODO*/ //	DRIVER( goldnaxj )	/* (c) 1989 (protected. Japan) */
/*TODO*/ //	DRIVER( jyuohki )	/* (c) 1988 (protected. Altered Beast Japan) */
/*TODO*/ //	DRIVER( moonwalk )	/* (c) 1990 (protected) */
/*TODO*/ //	DRIVER( moonwlka )	/* (c) 1990 (protected) */
/*TODO*/ //	DRIVER( passsht )	/* (protected) */
/*TODO*/ //	DRIVER( sdioj )		/* (c) 1987 (protected. Japan) */
/*TODO*/ //	DRIVER( shangon )	/* (c) 1992 (protected) */
/*TODO*/ //	DRIVER( shinobia )	/* (c) 1987 (protected) */
/*TODO*/ //	DRIVER( shinobib )	/* (c) 1987 (protected) */
/*TODO*/ //	DRIVER( tetris )	/* (c) 1988 (protected) */
/*TODO*/ //	DRIVER( tetrisa )	/* (c) 1988 (protected) */
/*TODO*/ //	DRIVER( wb3a )		/* (c) 1988 Sega / Westone (protected) */

/*TODO*/ //TESTDRIVER( aceattac )	/* (protected) */
/*TODO*/ //TESTDRIVER( aburner )	/* */
/*TODO*/ //TESTDRIVER( aburner2 )  /* */
/*TODO*/ //TESTDRIVER( afighter )	/* (protected) */
/*TODO*/ //TESTDRIVER( bloxeed )	/* (protected) */
/*TODO*/ //TESTDRIVER( cltchitr )	/* (protected) */
/*TODO*/ //TESTDRIVER( cotton )	/* (protected) */
/*TODO*/ //TESTDRIVER( cottona )	/* (protected) */
/*TODO*/ //TESTDRIVER( ddcrew )	/* (protected) */
/*TODO*/ //TESTDRIVER( dunkshot )	/* (protected) */
/*TODO*/ //TESTDRIVER( exctleag )  /* (protected) */
/*TODO*/ //TESTDRIVER( lghost )	/* (protected) */
/*TODO*/ //TESTDRIVER( loffire )	/* (protected) */
/*TODO*/ //TESTDRIVER( mvp )		/* (protected) */
/*TODO*/ //TESTDRIVER( ryukyu )	/* (protected) */
/*TODO*/ //TESTDRIVER( suprleag )  /* (protected) */
/*TODO*/ //TESTDRIVER( thndrbld )	/* (protected) */
/*TODO*/ //TESTDRIVER( thndrbdj )  /* (protected?) */
/*TODO*/ //TESTDRIVER( toutrun )	/* (protected) */
/*TODO*/ //TESTDRIVER( toutruna )	/* (protected) */

	// Working
/*TODO*/ //	DRIVER( alexkida )	/* (c) 1986 */
/*TODO*/ //	DRIVER( aliensyn )	/* (c) 1987 */
/*TODO*/ //	DRIVER( altbeas2 )	/* (c) 1988 */
/*TODO*/ //	DRIVER( altbeast )	/* (c) 1988 */
/*TODO*/ //	DRIVER( astormbl )	/* bootleg */
/*TODO*/ //	DRIVER( atomicp )	/* (c) 1990 Philko */
/*TODO*/ //	DRIVER( aurail )	/* (c) 1990 Sega / Westone */
/*TODO*/ //	DRIVER( bayroute )	/* (c) 1989 */
/*TODO*/ //	DRIVER( bodyslam )	/* (c) 1986 */
/*TODO*/ //	DRIVER( dduxbl )	/* (c) 1989 (Datsu bootleg) */
/*TODO*/ //	DRIVER( dumpmtmt )	/* (c) 1986 (Japan) */
/*TODO*/ //	DRIVER( endurob2 )	/* (c) 1985 (Beta bootleg) */
/*TODO*/ //	DRIVER( endurobl )	/* (c) 1985 (Herb bootleg) */
/*TODO*/ //	DRIVER( eswatbl )	/* (c) 1989 (but bootleg) */
/*TODO*/ //	DRIVER( fantzone )	/* (c) 1986 */
/*TODO*/ //	DRIVER( fantzono )	/* (c) 1986 */
/*TODO*/ //	DRIVER( fpointbl )	/* (c) 1989 (Datsu bootleg) */
/*TODO*/ //	DRIVER( goldnabl )	/* (c) 1989 (bootleg) */
/*TODO*/ //	DRIVER( goldnaxa )	/* (c) 1989 */
/*TODO*/ //	DRIVER( goldnaxe )	/* (c) 1989 */
/*TODO*/ //	DRIVER( hangon )	/* (c) 1985 */
/*TODO*/ //	DRIVER( hwchamp )	/* (c) 1987 */
/*TODO*/ //	DRIVER( mjleague )	/* (c) 1985 */
/*TODO*/ //	DRIVER( moonwlkb )	/* bootleg */
/*TODO*/ //	DRIVER( outrun )	/* (c) 1986 (bootleg)*/
/*TODO*/ //	DRIVER( outruna )	/* (c) 1986 (bootleg) */
/*TODO*/ //	DRIVER( outrunb )	/* (c) 1986 (protected beta bootleg) */
/*TODO*/ //	DRIVER( passht4b )	/* bootleg */
/*TODO*/ //	DRIVER( passshtb )	/* bootleg */
/*TODO*/ //	DRIVER( quartet )	/* (c) 1986 */
/*TODO*/ //	DRIVER( quartet2 )	/* (c) 1986 */
/*TODO*/ //	DRIVER( quartetj )	/* (c) 1986 */
/*TODO*/ //	DRIVER( riotcity )	/* (c) 1991 Sega / Westone */
/*TODO*/ //	DRIVER( sdi )		/* (c) 1987 */
/*TODO*/ //	DRIVER( shangonb )	/* (c) 1992 (but bootleg) */
/*TODO*/ //	DRIVER( sharrier )	/* (c) 1985 */
/*TODO*/ //	DRIVER( shdancbl )	/* (c) 1989 (but bootleg) */
/*TODO*/ //	DRIVER( shdancer )	/* (c) 1989 */
/*TODO*/ //	DRIVER( shdancrj )	/* (c) 1989 */
/*TODO*/ //	DRIVER( shinobi )	/* (c) 1987 */
/*TODO*/ //	DRIVER( shinobl )	/* (c) 1987 (but bootleg) */
/*TODO*/ //	DRIVER( tetrisbl )	/* (c) 1988 (but bootleg) */
/*TODO*/ //	DRIVER( timscanr )	/* (c) 1987 */
/*TODO*/ //	DRIVER( toryumon )	/* (c) 1995 */
/*TODO*/ //	DRIVER( tturf )		/* (c) 1989 Sega / Sunsoft */
/*TODO*/ //	DRIVER( tturfbl )	/* (c) 1989 (Datsu bootleg) */
/*TODO*/ //	DRIVER( tturfu )	/* (c) 1989 Sega / Sunsoft */
/*TODO*/ //	DRIVER( wb3 )		/* (c) 1988 Sega / Westone */
/*TODO*/ //	DRIVER( wb3bl )		/* (c) 1988 Sega / Westone (but bootleg) */
/*TODO*/ //	DRIVER( wrestwar )	/* (c) 1989 */



	/* other Data East games */
/*TODO*/ //	DRIVER( astrof )	/* (c) [1980?] */
/*TODO*/ //	DRIVER( astrof2 )	/* (c) [1980?] */
/*TODO*/ //	DRIVER( astrof3 )	/* (c) [1980?] */
/*TODO*/ //	DRIVER( tomahawk )	/* (c) [1980?] */
/*TODO*/ //	DRIVER( tomahaw5 )	/* (c) [1980?] */
/*TODO*/ //	DRIVER( kchamp )	/* (c) 1984 Data East USA (US) */
/*TODO*/ //	DRIVER( karatedo )	/* (c) 1984 Data East Corporation (Japan) */
/*TODO*/ //	DRIVER( kchampvs )	/* (c) 1984 Data East USA (US) */
/*TODO*/ //	DRIVER( karatevs )	/* (c) 1984 Data East Corporation (Japan) */
/*TODO*/ //	DRIVER( firetrap )	/* (c) 1986 */
/*TODO*/ //	DRIVER( firetpbl )	/* bootleg */

/*TODO*/ //	DRIVER( shootout )	/* (c) 1985 Data East USA (US) */
/*TODO*/ //	DRIVER( shootouj )	/* (c) 1985 Data East USA (Japan) */
/*TODO*/ //	DRIVER( shootoub )	/* bootleg */
        driver_sidepckt,	/* (c) 1986 Data East Corporation */
        driver_sidepctj,	/* (c) 1986 Data East Corporation */
        driver_sidepctb,	/* bootleg */



/*TODO*/ //	DRIVER( actfancr )	/* (c) 1989 Data East Corporation (World) */
/*TODO*/ //	DRIVER( actfanc1 )	/* (c) 1989 Data East Corporation (World) */
/*TODO*/ //	DRIVER( actfancj )	/* (c) 1989 Data East Corporation (Japan) */
/*TODO*/ //	DRIVER( triothep )	/* (c) 1989 Data East Corporation (Japan) */

	/* Data East 8-bit games */
        driver_lastmiss,	/* (c) 1986 Data East USA (US) */
        driver_lastmss2,	/* (c) 1986 Data East USA (US) */
        driver_shackled,	/* (c) 1986 Data East USA (US) */
        driver_breywood,	/* (c) 1986 Data East Corporation (Japan) */
        driver_csilver,	/* (c) 1987 Data East Corporation (Japan) */
        driver_ghostb,	/* (c) 1987 Data East USA (US) */
        driver_ghostb3,	/* (c) 1987 Data East USA (US) */
        driver_meikyuh,	/* (c) 1987 Data East Corporation (Japan) */
        driver_srdarwin,	/* (c) 1987 Data East Corporation (Japan) */
        driver_gondo,		/* (c) 1987 Data East USA (US) */
        driver_makyosen,	/* (c) 1987 Data East Corporation (Japan) */
        driver_garyoret,	/* (c) 1987 Data East Corporation (Japan) */
        driver_cobracom,	/* (c) 1988 Data East Corporation (World) */
        driver_cobracmj,	/* (c) 1988 Data East Corporation (Japan) */
        driver_oscar,	/* (c) 1988 Data East USA (US) */
        driver_oscarj,	/* (c) 1987 Data East Corporation (Japan) */

	/* Data East 16-bit games */
/*TODO*/ //	DRIVER( karnov )	/* (c) 1987 Data East USA (US) */
/*TODO*/ //	DRIVER( karnovj )	/* (c) 1987 Data East Corporation (Japan) */
/*TODO*/ //TESTDRIVER( wndrplnt )	/* (c) 1987 Data East Corporation (Japan) */
/*TODO*/ //	DRIVER( chelnov )	/* (c) 1988 Data East USA (US) */
/*TODO*/ //	DRIVER( chelnovj )	/* (c) 1988 Data East Corporation (Japan) */
/* the following ones all run on similar hardware */
/*TODO*/ //	DRIVER( hbarrel )	/* (c) 1987 Data East USA (US) */
/*TODO*/ //	DRIVER( hbarrelw )	/* (c) 1987 Data East Corporation (World) */
/*TODO*/ //	DRIVER( baddudes )	/* (c) 1988 Data East USA (US) */
/*TODO*/ //	DRIVER( drgninja )	/* (c) 1988 Data East Corporation (Japan) */
/*TODO*/ //TESTDRIVER( birdtry )	/* (c) 1988 Data East Corporation (Japan) */
/*TODO*/ //	DRIVER( robocop )	/* (c) 1988 Data East Corporation (World) */
/*TODO*/ //	DRIVER( robocopu )	/* (c) 1988 Data East USA (US) */
/*TODO*/ //	DRIVER( robocpu0 )	/* (c) 1988 Data East USA (US) */
/*TODO*/ //	DRIVER( robocopb )	/* bootleg */
/*TODO*/ //	DRIVER( hippodrm )	/* (c) 1989 Data East USA (US) */
/*TODO*/ //	DRIVER( ffantasy )	/* (c) 1989 Data East Corporation (Japan) */
/*TODO*/ //	DRIVER( slyspy )	/* (c) 1989 Data East USA (US) */
/*TODO*/ //	DRIVER( slyspy2 )	/* (c) 1989 Data East USA (US) */
/*TODO*/ //	DRIVER( secretag )	/* (c) 1989 Data East Corporation (World) */
/*TODO*/ //TESTDRIVER( secretab )	/* bootleg */
/*TODO*/ //	DRIVER( midres )	/* (c) 1989 Data East Corporation (World) */
/*TODO*/ //	DRIVER( midresu )	/* (c) 1989 Data East USA (US) */
/*TODO*/ //	DRIVER( midresj )	/* (c) 1989 Data East Corporation (Japan) */
/*TODO*/ //	DRIVER( bouldash )	/* (c) 1990 Data East Corporation */
/* end of similar hardware */
/*TODO*/ //	DRIVER( stadhero )	/* (c) 1988 Data East Corporation (Japan) */
/*TODO*/ //	DRIVER( madmotor )	/* (c) [1989] Mitchell */
	/* All these games have a unique code stamped on the mask roms */
/*TODO*/ //	DRIVER( vaportra )	/* MAA (c) 1989 Data East Corporation (US) */
/*TODO*/ //	DRIVER( kuhga )		/* MAA (c) 1989 Data East Corporation (Japan) */
/*TODO*/ //	DRIVER( cbuster )	/* MAB (c) 1990 Data East Corporation (World) */
/*TODO*/ //	DRIVER( cbusterw )	/* MAB (c) 1990 Data East Corporation (World) */
/*TODO*/ //	DRIVER( cbusterj )	/* MAB (c) 1990 Data East Corporation (Japan) */
/*TODO*/ //	DRIVER( twocrude )	/* MAB (c) 1990 Data East USA (US) */
/*TODO*/ //	DRIVER( darkseal )	/* MAC (c) 1990 Data East Corporation (World) */
/*TODO*/ //	DRIVER( darksea1 )	/* MAC (c) 1990 Data East Corporation (World) */
/*TODO*/ //	DRIVER( darkseaj )	/* MAC (c) 1990 Data East Corporation (Japan) */
/*TODO*/ //	DRIVER( gatedoom )	/* MAC (c) 1990 Data East Corporation (US) */
/*TODO*/ //	DRIVER( gatedom1 )	/* MAC (c) 1990 Data East Corporation (US) */
/*TODO*/ //TESTDRIVER( edrandy )	/* MAD (c) 1990 Data East Corporation (World) */
/*TODO*/ //TESTDRIVER( edrandyj )	/* MAD (c) 1990 Data East Corporation (Japan) */
/*TODO*/ //	DRIVER( supbtime )	/* MAE (c) 1990 Data East Corporation (Japan) */
	/* Mutant Fighter/Death Brade MAF (c) 1991 */
/*TODO*/ //	DRIVER( cninja )	/* MAG (c) 1991 Data East Corporation (World) */
/*TODO*/ //	DRIVER( cninja0 )	/* MAG (c) 1991 Data East Corporation (World) */
/*TODO*/ //	DRIVER( cninjau )	/* MAG (c) 1991 Data East Corporation (US) */
/*TODO*/ //	DRIVER( joemac )	/* MAG (c) 1991 Data East Corporation (Japan) */
/*TODO*/ //	DRIVER( stoneage )	/* bootleg */
	/* Robocop 2           MAH (c) 1991 */
	/* Desert Assault/Thunderzone MAJ (c) 1991 */
	/* Rohga Armour Attack/Wolf Fang MAM (c) 1991 */
	/* Captain America     MAN (c) 1991 */
/*TODO*/ //	DRIVER( tumblep )	/* MAP (c) 1991 Data East Corporation (World) */
/*TODO*/ //	DRIVER( tumblepj )	/* MAP (c) 1991 Data East Corporation (Japan) */
/*TODO*/ //	DRIVER( tumblepb )	/* bootleg */
/*TODO*/ //	DRIVER( tumblep2 )	/* bootleg */
/*TODO*/ //TESTDRIVER( funkyjet )	/* MAT (c) 1992 Mitchell */


	/* Tehkan / Tecmo games (Tehkan became Tecmo in 1986) */
/*TODO*/ //	DRIVER( senjyo )	/* (c) 1983 Tehkan */
/*TODO*/ //	DRIVER( starforc )	/* (c) 1984 Tehkan */
/*TODO*/ //	DRIVER( starfore )	/* (c) 1984 Tehkan */
/*TODO*/ //	DRIVER( megaforc )	/* (c) 1985 Tehkan + Video Ware license */
/*TODO*/ //	DRIVER( baluba )	/* (c) 1986 Able Corp. */
        driver_bombjack,	/* (c) 1984 Tehkan */
        driver_bombjac2,	/* (c) 1984 Tehkan */
        driver_pbaction,	/* (c) 1985 Tehkan */
        driver_pbactio2,	/* (c) 1985 Tehkan */
	/* 6011 Pontoon (c) 1985 Tehkan is a gambling game - removed */
/*TODO*/ //	DRIVER( tehkanwc )	/* (c) 1985 Tehkan */
/*TODO*/ //	DRIVER( gridiron )	/* (c) 1985 Tehkan */
/*TODO*/ //	DRIVER( teedoff )	/* 6102 - (c) 1986 Tecmo */
/*TODO*/ //	DRIVER( solomon )	/* (c) 1986 Tecmo */
        driver_rygar,	/* 6002 - (c) 1986 Tecmo */
        driver_rygar2,	/* 6002 - (c) 1986 Tecmo */
        driver_rygarj,	/* 6002 - (c) 1986 Tecmo */
        driver_gemini,	/* (c) 1987 Tecmo */
        driver_silkworm,/* 6217 - (c) 1988 Tecmo */
        driver_silkwrm2,/* 6217 - (c) 1988 Tecmo */
/*TODO*/ //	DRIVER( gaiden )	/* 6215 - (c) 1988 Tecmo */
/*TODO*/ //	DRIVER( shadoww )	/* 6215 - (c) 1988 Tecmo */
/*TODO*/ //	DRIVER( tknight )	/* (c) 1989 Tecmo */
/*TODO*/ //	DRIVER( wildfang )	/* (c) 1989 Tecmo */
/*TODO*/ //	DRIVER( wc90b )		/* bootleg */

	/* Konami bitmap games */
/*TODO*/ //	DRIVER( tutankhm )	/* GX350 (c) 1982 Konami */
/*TODO*/ //	DRIVER( tutankst )	/* GX350 (c) 1982 Stern */
/*TODO*/ //	DRIVER( junofrst )	/* GX310 (c) 1983 Konami */

	/* Konami games */
        driver_pooyan,	/* GX320 (c) 1982 */
        driver_pooyans,	/* GX320 (c) 1982 Stern */
        driver_pootan,	/* bootleg */
        driver_timeplt,	/* GX393 (c) 1982 */
        driver_timepltc,	/* GX393 (c) 1982 + Centuri license*/
        driver_spaceplt,	/* bootleg */
        driver_psurge,	/* (c) 1988 unknown (NOT Konami) */
/*TODO*/ //	DRIVER( megazone )	/* GX319 (c) 1983 */
/*TODO*/ //	DRIVER( megaznik )	/* GX319 (c) 1983 + Interlogic / Kosuka */
/*TODO*/ //	DRIVER( pandoras )	/* GX328 (c) 1984 + Interlogic */
/*TODO*/ //	DRIVER( gyruss )	/* GX347 (c) 1983 */
/*TODO*/ //	DRIVER( gyrussce )	/* GX347 (c) 1983 + Centuri license */
/*TODO*/ //	DRIVER( venus )		/* bootleg */

        driver_rocnrope,	/* GX364 (c) 1983 */
        driver_rocnropk,	/* GX364 (c) 1983 + Kosuka */
        driver_circusc,	/* GX380 (c) 1984 */
        driver_circusc2,	/* GX380 (c) 1984 */
        driver_circuscc,	/* GX380 (c) 1984 + Centuri license */
        driver_circusce,	/* GX380 (c) 1984 + Centuri license */
        driver_tp84,		/* GX388 (c) 1984 */
        driver_tp84a,		/* GX388 (c) 1984 */
     
/*TODO*/ //	DRIVER( sbasketb )	/* GX405 (c) 1984 */
        driver_mikie,	/* GX469 (c) 1984 */
        driver_mikiej,	/* GX469 (c) 1984 */
        driver_mikiehs,	/* GX469 (c) 1984 */

/*TODO*/ //	DRIVER( yiear )		/* GX407 (c) 1985 */
/*TODO*/ //	DRIVER( yiear2 )	/* GX407 (c) 1985 */

        driver_pingpong,	/* GX555 (c) 1985 */

/*TODO*/ //	DRIVER( finalizr )	/* GX523 (c) 1985 */
/*TODO*/ //	DRIVER( finalizb )	/* bootleg */
        driver_ironhors,	/* GX560 (c) 1986 */
        driver_dairesya,	/* GX560 (c) 1986 (Japan) */
        driver_farwest,
        driver_contra,	/* GX633 (c) 1987 */
        driver_contrab,	/* bootleg */
        driver_contraj,	/* GX633 (c) 1987 (Japan) */
        driver_contrajb,	/* bootleg */
        driver_gryzor,	/* GX633 (c) 1987 */
        driver_combasc,	/* GX611 (c) 1988 */
        driver_combasct,	/* GX611 (c) 1987 */
        driver_combascj,	/* GX611 (c) 1987 (Japan) */
        driver_bootcamp,	/* GX611 (c) 1987 */
        driver_combascb,	/* bootleg */
/*TODO*/ //	DRIVER( rockrage )	/* GX620 (c) 1986 (World?) */
/*TODO*/ //	DRIVER( rockragj )	/* GX620 (c) 1986 (Japan) */

/*TODO*/ //	DRIVER( fastlane )	/* GX752 (c) 1987 */
/*TODO*/ //	DRIVER( labyrunr )	/* GX771 (c) 1987 (Japan) */
/*TODO*/ //	DRIVER( thehustl )	/* GX765 (c) 1987 (Japan) */
/*TODO*/ //	DRIVER( thehustj )	/* GX765 (c) 1987 (Japan) */
/*TODO*/ //	DRIVER( battlnts )	/* GX777 (c) 1987 */
/*TODO*/ //	DRIVER( battlntj )	/* GX777 (c) 1987 (Japan) */
/*TODO*/ //	DRIVER( bladestl )	/* GX797 (c) 1987 */
/*TODO*/ //	DRIVER( bladstle )	/* GX797 (c) 1987 */
/*TODO*/ //	DRIVER( hcastle )	/* GX768 (c) 1988 */
/*TODO*/ //	DRIVER( hcastlea )	/* GX768 (c) 1988 */
/*TODO*/ //	DRIVER( hcastlej )	/* GX768 (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( ajax )		/* GX770 (c) 1987 */
/*TODO*/ //	DRIVER( ajaxj )		/* GX770 (c) 1987 (Japan) */
/*TODO*/ //	DRIVER( scontra )	/* GX775 (c) 1988 */
/*TODO*/ //	DRIVER( scontraj )	/* GX775 (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( thunderx )	/* GX873 (c) 1988 */
/*TODO*/ //	DRIVER( thnderxj )	/* GX873 (c) 1988 (Japan) */

/*TODO*/ //	DRIVER( 88games )	/* GX861 (c) 1988 */
/*TODO*/ //	DRIVER( konami88 )	/* GX861 (c) 1988 */
/*TODO*/ //	DRIVER( hypsptsp )	/* GX861 (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( gbusters )	/* GX878 (c) 1988 */
/*TODO*/ //	DRIVER( crazycop )	/* GX878 (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( crimfght )	/* GX821 (c) 1989 (US) */
/*TODO*/ //	DRIVER( crimfgt2 )	/* GX821 (c) 1989 (World) */
/*TODO*/ //	DRIVER( crimfgtj )	/* GX821 (c) 1989 (Japan) */
/*TODO*/ //	DRIVER( spy )		/* GX857 (c) 1989 (US) */
/*TODO*/ //	DRIVER( bottom9 )	/* GX891 (c) 1989 */
/*TODO*/ //	DRIVER( bottom9n )	/* GX891 (c) 1989 */
/*TODO*/ //	DRIVER( blockhl )	/* GX973 (c) 1989 */
/*TODO*/ //	DRIVER( quarth )	/* GX973 (c) 1989 (Japan) */
/*TODO*/ //	DRIVER( aliens )	/* GX875 (c) 1990 (World) */
/*TODO*/ //	DRIVER( aliens2 )	/* GX875 (c) 1990 (World) */
/*TODO*/ //	DRIVER( aliensu )	/* GX875 (c) 1990 (US) */
/*TODO*/ //	DRIVER( aliensj )	/* GX875 (c) 1990 (Japan) */
/*TODO*/ //	DRIVER( surpratk )	/* GX911 (c) 1990 (Japan) */
/*TODO*/ //	DRIVER( parodius )	/* GX955 (c) 1990 (Japan) */
/*TODO*/ //	DRIVER( rollerg )	/* GX999 (c) 1991 (US) */
/*TODO*/ //	DRIVER( rollergj )	/* GX999 (c) 1991 (Japan) */
/*TODO*/ //TESTDRIVER( xexex )		/* GX067 (c) 1991 */
/*TODO*/ //	DRIVER( simpsons )	/* GX072 (c) 1991 */
/*TODO*/ //	DRIVER( simpsn2p )	/* GX072 (c) 1991 */
/*TODO*/ //	DRIVER( simps2pj )	/* GX072 (c) 1991 (Japan) */
/*TODO*/ //	DRIVER( vendetta )	/* GX081 (c) 1991 (Asia) */
/*TODO*/ //	DRIVER( vendett2 )	/* GX081 (c) 1991 (Asia) */
/*TODO*/ //	DRIVER( vendettj )	/* GX081 (c) 1991 (Japan) */
/*TODO*/ //	DRIVER( wecleman )	/* GX602 (c) 1986 */
/*TODO*/ //	DRIVER( hotchase )	/* GX763 (c) 1988 */
/*TODO*/ //	DRIVER( ultraman )	/* GX910 (c) 1991 Banpresto/Bandai */

	/* Konami "Nemesis hardware" games */
/*TODO*/ //	DRIVER( nemesis )	/* GX456 (c) 1985 */
/*TODO*/ //	DRIVER( nemesuk )	/* GX456 (c) 1985 */
/*TODO*/ //	DRIVER( konamigt )	/* GX561 (c) 1985 */
/*TODO*/ //	DRIVER( salamand )	/* GX587 (c) 1986 */
/*TODO*/ //	DRIVER( lifefrce )	/* GX587 (c) 1986 */
/*TODO*/ //	DRIVER( lifefrcj )	/* GX587 (c) 1986 */
	/* GX400 BIOS based games */
/*TODO*/ //	DRIVER( rf2 )		/* GX561 (c) 1985 */
/*TODO*/ //	DRIVER( twinbee )	/* GX412 (c) 1985 */
/*TODO*/ //	DRIVER( gradius )	/* GX456 (c) 1985 */
/*TODO*/ //	DRIVER( gwarrior )	/* GX578 (c) 1985 */

	/* Konami "Twin 16" games */
/*TODO*/ //	DRIVER( devilw )	/* GX687 (c) 1987 */
/*TODO*/ //	DRIVER( darkadv )	/* GX687 (c) 1987 */
/*TODO*/ //	DRIVER( majuu )		/* GX687 (c) 1987 (Japan) */
/*TODO*/ //	DRIVER( vulcan )	/* GX785 (c) 1988 */
/*TODO*/ //	DRIVER( gradius2 )	/* GX785 (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( grdius2a )	/* GX785 (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( grdius2b )	/* GX785 (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( cuebrick )	/* GX903 (c) 1989 */
/*TODO*/ //	DRIVER( fround )	/* GX870 (c) 1988 */
/*TODO*/ //	DRIVER( hpuncher )	/* GX870 (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( miaj )		/* GX808 (c) 1989 (Japan) */

	/* Konami Gradius III board */
/*TODO*/ //	DRIVER( gradius3 )	/* GX945 (c) 1989 (Japan) */
/*TODO*/ //	DRIVER( grdius3a )	/* GX945 (c) 1989 (Asia) */

	/* (some) Konami 68000 games */
/*TODO*/ //	DRIVER( mia )		/* GX808 (c) 1989 */
/*TODO*/ //	DRIVER( mia2 )		/* GX808 (c) 1989 */
/*TODO*/ //	DRIVER( tmnt )		/* GX963 (c) 1989 (US) */
/*TODO*/ //	DRIVER( tmht )		/* GX963 (c) 1989 (UK) */
/*TODO*/ //	DRIVER( tmntj )		/* GX963 (c) 1989 (Japan) */
/*TODO*/ //	DRIVER( tmht2p )	/* GX963 (c) 1989 (UK) */
/*TODO*/ //	DRIVER( tmnt2pj )	/* GX963 (c) 1990 (Japan) */
/*TODO*/ //	DRIVER( punkshot )	/* GX907 (c) 1990 (US) */
/*TODO*/ //	DRIVER( punksht2 )	/* GX907 (c) 1990 (US) */
/*TODO*/ //	DRIVER( lgtnfght )	/* GX939 (c) 1990 (US) */
/*TODO*/ //	DRIVER( trigon )	/* GX939 (c) 1990 (Japan) */
/*TODO*/ //	DRIVER( blswhstl )	/* GX060 (c) 1991 */
/*TODO*/ //	DRIVER( detatwin )	/* GX060 (c) 1991 (Japan) */
/*TODO*/ //TESTDRIVER( glfgreat )	/* GX061 (c) 1991 */
/*TODO*/ //	DRIVER( tmnt2 )		/* GX063 (c) 1991 (US) */
/*TODO*/ //	DRIVER( tmnt22p )	/* GX063 (c) 1991 (US) */
/*TODO*/ //	DRIVER( tmnt2a )	/* GX063 (c) 1991 (Asia) */
/*TODO*/ //	DRIVER( ssriders )	/* GX064 (c) 1991 (World) */
/*TODO*/ //	DRIVER( ssrdrebd )	/* GX064 (c) 1991 (World) */
/*TODO*/ //	DRIVER( ssrdrebc )	/* GX064 (c) 1991 (World) */
/*TODO*/ //	DRIVER( ssrdruda )	/* GX064 (c) 1991 (US) */
/*TODO*/ //	DRIVER( ssrdruac )	/* GX064 (c) 1991 (US) */
/*TODO*/ //	DRIVER( ssrdrubc )	/* GX064 (c) 1991 (US) */
/*TODO*/ //	DRIVER( ssrdrabd )	/* GX064 (c) 1991 (Asia) */
/*TODO*/ //	DRIVER( ssrdrjbd )	/* GX064 (c) 1991 (Japan) */
/*TODO*/ //	DRIVER( xmen )		/* GX065 (c) 1992 (US) */
/*TODO*/ //	DRIVER( xmen6p )	/* GX065 (c) 1992 */
/*TODO*/ //	DRIVER( xmen2pj )	/* GX065 (c) 1992 (Japan) */
/*TODO*/ //	DRIVER( thndrx2 )	/* GX073 (c) 1991 (Japan) */

	/* Exidy games */
/*TODO*/ //	DRIVER( sidetrac )	/* (c) 1979 */
/*TODO*/ //	DRIVER( targ )		/* (c) 1980 */
/*TODO*/ //	DRIVER( spectar )	/* (c) 1980 */
/*TODO*/ //	DRIVER( spectar1 )	/* (c) 1980 */
/*TODO*/ //	DRIVER( venture )	/* (c) 1981 */
/*TODO*/ //	DRIVER( venture2 )	/* (c) 1981 */
/*TODO*/ //	DRIVER( venture4 )	/* (c) 1981 */
/*TODO*/ //	DRIVER( mtrap )		/* (c) 1981 */
/*TODO*/ //	DRIVER( mtrap3 )	/* (c) 1981 */
/*TODO*/ //	DRIVER( mtrap4 )	/* (c) 1981 */
/*TODO*/ //	DRIVER( pepper2 )	/* (c) 1982 */
/*TODO*/ //	DRIVER( hardhat )	/* (c) 1982 */
/*TODO*/ //	DRIVER( fax )		/* (c) 1983 */
/*TODO*/ //	DRIVER( circus )	/* no copyright notice [1977?] */
/*TODO*/ //	DRIVER( robotbwl )	/* no copyright notice */
/*TODO*/ //	DRIVER( crash )		/* Exidy [1979?] */
/*TODO*/ //	DRIVER( ripcord )	/* Exidy [1977?] */
/*TODO*/ //	DRIVER( starfire )	/* Exidy [1979?] */
/*TODO*/ //	DRIVER( fireone )	/* (c) 1979 Exidy */

	/* Exidy 440 games */
/*TODO*/ //	DRIVER( crossbow )	/* (c) 1983 */
/*TODO*/ //	DRIVER( cheyenne )	/* (c) 1984 */
/*TODO*/ //	DRIVER( combat )	/* (c) 1985 */
/*TODO*/ //	DRIVER( cracksht )	/* (c) 1985 */
/*TODO*/ //	DRIVER( claypign )	/* (c) 1986 */
/*TODO*/ //	DRIVER( chiller )	/* (c) 1986 */
/*TODO*/ //	DRIVER( topsecex )	/* (c) 1986 */
/*TODO*/ //	DRIVER( hitnmiss )	/* (c) 1987 */
/*TODO*/ //	DRIVER( hitnmis2 )	/* (c) 1987 */
/*TODO*/ //	DRIVER( whodunit )	/* (c) 1988 */
/*TODO*/ //	DRIVER( showdown )	/* (c) 1988 */

	/* Atari vector games */
/*TODO*/ //	DRIVER( asteroid )	/* (c) 1979 */
/*TODO*/ //	DRIVER( asteroi1 )	/* no copyright notice */
/*TODO*/ //	DRIVER( asteroib )	/* bootleg */
/*TODO*/ //	DRIVER( astdelux )	/* (c) 1980 */
/*TODO*/ //	DRIVER( astdelu1 )	/* (c) 1980 */
/*TODO*/ //	DRIVER( bwidow )	/* (c) 1982 */
/*TODO*/ //	DRIVER( bzone )		/* (c) 1980 */
/*TODO*/ //	DRIVER( bzone2 )	/* (c) 1980 */
/*TODO*/ //	DRIVER( gravitar )	/* (c) 1982 */
/*TODO*/ //	DRIVER( gravitr2 )	/* (c) 1982 */
/*TODO*/ //	DRIVER( llander )	/* no copyright notice */
/*TODO*/ //	DRIVER( llander1 )	/* no copyright notice */
/*TODO*/ //	DRIVER( redbaron )	/* (c) 1980 */
/*TODO*/ //	DRIVER( spacduel )	/* (c) 1980 */
/*TODO*/ //	DRIVER( tempest )	/* (c) 1980 */
/*TODO*/ //	DRIVER( tempest1 )	/* (c) 1980 */
/*TODO*/ //	DRIVER( tempest2 )	/* (c) 1980 */
/*TODO*/ //	DRIVER( temptube )	/* hack */
/*TODO*/ //	DRIVER( starwars )	/* (c) 1983 */
/*TODO*/ //	DRIVER( starwar1 )	/* (c) 1983 */
/*TODO*/ //	DRIVER( esb )		/* (c) 1985 */
/*TODO*/ //	DRIVER( mhavoc )	/* (c) 1983 */
/*TODO*/ //	DRIVER( mhavoc2 )	/* (c) 1983 */
/*TODO*/ //	DRIVER( mhavocp )	/* (c) 1983 */
/*TODO*/ //	DRIVER( mhavocrv )	/* hack */
/*TODO*/ //	DRIVER( quantum )	/* (c) 1982 */	/* made by Gencomp */
/*TODO*/ //	DRIVER( quantum1 )	/* (c) 1982 */	/* made by Gencomp */
/*TODO*/ //	DRIVER( quantump )	/* (c) 1982 */	/* made by Gencomp */

	/* Atari b/w games */
/*TODO*/ //	DRIVER( sprint1 )	/* no copyright notice */
/*TODO*/ //	DRIVER( sprint2 )	/* no copyright notice */
/*TODO*/ //	DRIVER( sbrkout )	/* no copyright notice */
/*TODO*/ //	DRIVER( dominos )	/* no copyright notice */
/*TODO*/ //	DRIVER( nitedrvr )	/* no copyright notice [1976] */
/*TODO*/ //	DRIVER( bsktball )	/* no copyright notice */
/*TODO*/ //	DRIVER( avalnche )	/* no copyright notice [1978] */
/*TODO*/ //	DRIVER( subs )		/* no copyright notice [1976] */
/*TODO*/ //	DRIVER( atarifb )	/* no copyright notice [1978] */
/*TODO*/ //	DRIVER( atarifb1 )	/* no copyright notice [1978] */
/*TODO*/ //	DRIVER( atarifb4 )	/* no copyright notice [1979] */
/*TODO*/ //	DRIVER( abaseb )	/* no copyright notice [1979] */
/*TODO*/ //	DRIVER( abaseb2 )	/* no copyright notice [1979] */
/*TODO*/ //	DRIVER( soccer )	/* no copyright notice */
/*TODO*/ //	DRIVER( canyon )	/* no copyright notice [1977] */
/*TODO*/ //	DRIVER( canbprot )	/* no copyright notice [1977] */
/*TODO*/ //	DRIVER( skydiver )	/* no copyright notice [1977] */

	/* Atari "Centipede hardware" games */
/*TODO*/ //	DRIVER( warlord )	/* (c) 1980 */
/*TODO*/ //	DRIVER( centiped )	/* (c) 1980 */
/*TODO*/ //	DRIVER( centipd2 )	/* (c) 1980 */
/*TODO*/ //	DRIVER( centipdb )	/* bootleg */
/*TODO*/ //	DRIVER( centipb2 )	/* bootleg */
/*TODO*/ //	DRIVER( milliped )	/* (c) 1982 */
/*TODO*/ //	DRIVER( qwakprot )	/* (c) 1982 */

	/* "Kangaroo hardware" games */
/*TODO*/ //TESTDRIVER( fnkyfish )	/* (c) 1981 Sun Electronics */
    driver_kangaroo,	/* (c) 1982 Sun Electronics */
    driver_kangaroa,	/* (c) 1982 Atari */
    driver_kangarob,	/* bootleg */
/*TODO*/ //	DRIVER( arabian )	/* (c) 1983 Sun Electronics */
/*TODO*/ //	DRIVER( arabiana )	/* (c) 1983 Atari */

	/* Atari "Missile Command hardware" games */
/*TODO*/ //	DRIVER( missile )	/* (c) 1980 */
/*TODO*/ //	DRIVER( missile2 )	/* (c) 1980 */
/*TODO*/ //	DRIVER( suprmatk )	/* (c) 1980 + (c) 1981 Gencomp */

	/* misc Atari games */
/*TODO*/ //	DRIVER( foodf )		/* (c) 1982 */	/* made by Gencomp */
/*TODO*/ //	DRIVER( liberatr )	/* (c) 1982 */
/*TODO*/ //TESTDRIVER( liberat2 )
/*TODO*/ //	DRIVER( ccastles )	/* (c) 1983 */
/*TODO*/ //	DRIVER( ccastle2 )	/* (c) 1983 */
/*TODO*/ //	DRIVER( cloak )		/* (c) 1983 */
/*TODO*/ //	DRIVER( cloud9 )	/* (c) 1983 */
/*TODO*/ //	DRIVER( jedi )		/* (c) 1984 */

	/* Atari System 1 games */
/*TODO*/ //	DRIVER( marble )	/* (c) 1984 */
/*TODO*/ //	DRIVER( marble2 )	/* (c) 1984 */
/*TODO*/ //	DRIVER( marblea )	/* (c) 1984 */
/*TODO*/ //	DRIVER( peterpak )	/* (c) 1984 */
/*TODO*/ //	DRIVER( indytemp )	/* (c) 1985 */
/*TODO*/ //	DRIVER( indytem2 )	/* (c) 1985 */
/*TODO*/ //	DRIVER( indytem3 )	/* (c) 1985 */
/*TODO*/ //	DRIVER( indytem4 )	/* (c) 1985 */
/*TODO*/ //	DRIVER( roadrunn )	/* (c) 1985 */
/*TODO*/ //	DRIVER( roadblst )	/* (c) 1986, 1987 */

	/* Atari System 2 games */
/*TODO*/ //	DRIVER( paperboy )	/* (c) 1984 */
/*TODO*/ //	DRIVER( ssprint )	/* (c) 1986 */
/*TODO*/ //	DRIVER( csprint )	/* (c) 1986 */
/*TODO*/ //	DRIVER( 720 )		/* (c) 1986 */
/*TODO*/ //	DRIVER( 720b )		/* (c) 1986 */
/*TODO*/ //	DRIVER( apb )		/* (c) 1987 */
/*TODO*/ //	DRIVER( apb2 )		/* (c) 1987 */

	/* later Atari games */
/*TODO*/ //	DRIVER( gauntlet )	/* (c) 1985 */
/*TODO*/ //	DRIVER( gauntir1 )	/* (c) 1985 */
/*TODO*/ //	DRIVER( gauntir2 )	/* (c) 1985 */
/*TODO*/ //	DRIVER( gaunt2p )	/* (c) 1985 */
/*TODO*/ //	DRIVER( gaunt2 )	/* (c) 1986 */
/*TODO*/ //	DRIVER( vindctr2 )	/* (c) 1988 */
/*TODO*/ //	DRIVER( toobin )	/* (c) 1988 */
/*TODO*/ //	DRIVER( toobin2 )	/* (c) 1988 */
/*TODO*/ //	DRIVER( toobinp )	/* (c) 1988 */
/*TODO*/ //	DRIVER( vindictr )	/* (c) 1988 */
/*TODO*/ //	DRIVER( klax )		/* (c) 1989 */
/*TODO*/ //	DRIVER( klax2 )		/* (c) 1989 */
/*TODO*/ //	DRIVER( klax3 )		/* (c) 1989 */
/*TODO*/ //	DRIVER( klaxj )		/* (c) 1989 (Japan) */
/*TODO*/ //	DRIVER( blstroid )	/* (c) 1987 */
/*TODO*/ //	DRIVER( blstroi2 )	/* (c) 1987 */
/*TODO*/ //	DRIVER( xybots )	/* (c) 1987 */
/*TODO*/ //	DRIVER( eprom )		/* (c) 1989 */
/*TODO*/ //	DRIVER( eprom2 )	/* (c) 1989 */
/*TODO*/ //	DRIVER( skullxbo )	/* (c) 1989 */
/*TODO*/ //	DRIVER( skullxb2 )	/* (c) 1989 */
/*TODO*/ //	DRIVER( badlands )	/* (c) 1989 */
/*TODO*/ //	DRIVER( cyberbal )	/* (c) 1989 */
/*TODO*/ //	DRIVER( cyberbt )	/* (c) 1989 */
/*TODO*/ //	DRIVER( cyberb2p )	/* (c) 1989 */
/*TODO*/ //	DRIVER( rampart )	/* (c) 1990 */
/*TODO*/ //	DRIVER( ramprt2p )	/* (c) 1990 */
/*TODO*/ //	DRIVER( rampartj )	/* (c) 1990 (Japan) */
/*TODO*/ //	DRIVER( shuuz )		/* (c) 1990 */
/*TODO*/ //	DRIVER( shuuz2 )	/* (c) 1990 */
/*TODO*/ //	DRIVER( hydra )		/* (c) 1990 */
/*TODO*/ //	DRIVER( hydrap )	/* (c) 1990 */
/*TODO*/ //	DRIVER( pitfight )	/* (c) 1990 */
/*TODO*/ //	DRIVER( pitfigh3 )	/* (c) 1990 */
/*TODO*/ //	DRIVER( thunderj )	/* (c) 1990 */
/*TODO*/ //	DRIVER( batman )	/* (c) 1991 */
/*TODO*/ //	DRIVER( relief )	/* (c) 1992 */
/*TODO*/ //	DRIVER( relief2 )	/* (c) 1992 */
/*TODO*/ //	DRIVER( offtwall )	/* (c) 1991 */
/*TODO*/ //	DRIVER( offtwalc )	/* (c) 1991 */
/*TODO*/ //	DRIVER( arcadecl )	/* (c) 1992 */
/*TODO*/ //	DRIVER( sparkz )	/* (c) 1992 */

	/* SNK / Rock-ola games */
/*TODO*/ //	DRIVER( sasuke )	/* [1980] Shin Nihon Kikaku (SNK) */
/*TODO*/ //	DRIVER( satansat )	/* (c) 1981 SNK */
/*TODO*/ //	DRIVER( zarzon )	/* (c) 1981 Taito, gameplay says SNK */
/*TODO*/ //	DRIVER( vanguard )	/* (c) 1981 SNK */
/*TODO*/ //	DRIVER( vangrdce )	/* (c) 1981 SNK + Centuri */
/*TODO*/ //	DRIVER( fantasy )	/* (c) 1981 Rock-ola */
/*TODO*/ //	DRIVER( fantasyj )	/* (c) 1981 SNK */
/*TODO*/ //	DRIVER( pballoon )	/* (c) 1982 SNK */
/*TODO*/ //	DRIVER( nibbler )	/* (c) 1982 Rock-ola */
/*TODO*/ //	DRIVER( nibblera )	/* (c) 1982 Rock-ola */

	/* later SNK games, each game can be identified by PCB code and ROM
	code, the ROM code is the same between versions, and usually based
	upon the Japanese title. */
/*TODO*/ //	DRIVER( lasso )		/*       'WM' (c) 1982 */
/*TODO*/ //	DRIVER( joyfulr )	/* A2001      (c) 1983 */
/*TODO*/ //	DRIVER( mnchmobl )	/* A2001      (c) 1983 + Centuri license */
/*TODO*/ //	DRIVER( marvins )	/* A2003      (c) 1983 */
/*TODO*/ //	DRIVER( madcrash )	/* A2005      (c) 1984 */
/*TODO*/ //	DRIVER( vangrd2 )	/*            (c) 1984 */
/*TODO*/ //	DRIVER( hal21 )		/*            (c) 1985 */
/*TODO*/ //	DRIVER( hal21j )	/*            (c) 1985 (Japan) */
/*TODO*/ //	DRIVER( aso )		/*            (c) 1985 */
        driver_tnk3,		/* A5001      (c) 1985 */
        driver_tnk3j,		/* A5001      (c) 1985 */
        driver_athena,	        /*       'UP' (c) 1986 */
        driver_fitegolf,	/*       'GU' (c) 1988 */
        driver_ikari,		/* A5004 'IW' (c) 1986 */
        driver_ikarijp,         /* A5004 'IW' (c) 1986 (Japan) */
        driver_ikarijpb,	/* bootleg */
        driver_victroad,	/*            (c) 1986 */
        driver_dogosoke,	/*            (c) 1986 */
        driver_gwar,		/* A7003 'GV' (c) 1987 */
        driver_gwarj,		/* A7003 'GV' (c) 1987 (Japan) */
        driver_gwara,		/* A7003 'GV' (c) 1987 */
        driver_gwarb,		/* bootleg */
        driver_bermudat,	/* A6003 'WW' (c) 1987 */
        driver_bermudaj,	/* A6003 'WW' (c) 1987 */
        driver_bermudaa,	/* A6003 'WW' (c) 1987 */
        driver_worldwar,	/* A6003 'WW' (c) 1987 */
        driver_psychos,         /*       'PS' (c) 1987 */
        driver_psychosj,	/*       'PS' (c) 1987 (Japan) */
        driver_chopper,         /* A7003 'KK' (c) 1988 */
        driver_legofair,	/* A7003 'KK' (c) 1988 */
        driver_ftsoccer,	/*            (c) 1988 */
        driver_tdfever,         /* A6006 'TD' (c) 1987 */
        driver_tdfeverj,	/* A6006 'TD' (c) 1987 */
/*TODO*/ //	DRIVER( ikari3 )	/* A7007 'IK3'(c) 1989 */
/*TODO*/ //	DRIVER( pow )		/* A7008 'DG' (c) 1988 */
/*TODO*/ //	DRIVER( powj )		/* A7008 'DG' (c) 1988 */
/*TODO*/ //	DRIVER( searchar )	/* A8007 'BH' (c) 1989 */
/*TODO*/ //	DRIVER( sercharu )	/* A8007 'BH' (c) 1989 */
/*TODO*/ //	DRIVER( streetsm )	/* A8007 'S2' (c) 1989 */
/*TODO*/ //	DRIVER( streets1 )	/* A7008 'S2' (c) 1989 */
/*TODO*/ //	DRIVER( streetsj )	/* A8007 'S2' (c) 1989 */
	/* Mechanized Attack   A8002 'MA' (c) 1989 */
/*TODO*/ //	DRIVER( prehisle )	/* A8003 'GT' (c) 1989 */
/*TODO*/ //	DRIVER( prehislu )	/* A8003 'GT' (c) 1989 */
/*TODO*/ //	DRIVER( gensitou )	/* A8003 'GT' (c) 1989 */
	/* Beast Busters       A9003 'BB' (c) 1989 */

	/* SNK / Alpha 68K games */
/*TODO*/ //TESTDRIVER( kyros )
/*TODO*/ //TESTDRIVER( sstingry )
/*TODO*/ //TESTDRIVER( paddlema )	/* Alpha-68K96I  'PM' (c) 1988 SNK */
/*TODO*/ //	DRIVER( timesold )	/* Alpha-68K96II 'BT' (c) 1987 SNK / Romstar */
/*TODO*/ //	DRIVER( timesol1 )  /* Alpha-68K96II 'BT' (c) 1987 */
/*TODO*/ //	DRIVER( btlfield )  /* Alpha-68K96II 'BT' (c) 1987 */
/*TODO*/ //	DRIVER( skysoldr )	/* Alpha-68K96II 'SS' (c) 1988 SNK (Romstar with dip switch) */
/*TODO*/ //	DRIVER( goldmedl )	/* Alpha-68K96II 'GM' (c) 1988 SNK */
/*TODO*/ //TESTDRIVER( goldmedb )	/* Alpha-68K96II bootleg */
/*TODO*/ //	DRIVER( skyadvnt )	/* Alpha-68K96V  'SA' (c) 1989 SNK of America licensed from Alpha */
/*TODO*/ //	DRIVER( gangwars )	/* Alpha-68K96V       (c) 1989 Alpha */
/*TODO*/ //	DRIVER( gangwarb )	/* Alpha-68K96V bootleg */
/*TODO*/ //	DRIVER( sbasebal )	/* Alpha-68K96V       (c) 1989 SNK of America licensed from Alpha */

	/* Technos games */
/*TODO*/ //	DRIVER( bigprowr )	/* TA-0007 (c) 1983 */
/*TODO*/ //	DRIVER( tagteam )	/* TA-0007 (c) 1983 + Data East license */
/*TODO*/ //	DRIVER( ssozumo )	/* TA-0008 (c) 1984 */
	/* TA-0011 Dog Fight (Data East) / Batten O'hara no Sucha-Raka Kuuchuu Sen 1985 */



/*TODO*/ //	DRIVER( battlane )	/* TA-???? (c) 1986 + Taito license */
/*TODO*/ //	DRIVER( battlan2 )	/* TA-???? (c) 1986 + Taito license */
/*TODO*/ //	DRIVER( battlan3 )	/* TA-???? (c) 1986 + Taito license */

/*TODO*/ //	DRIVER( ctribe )	/* TA-0028 (c) 1990 (US) */
/*TODO*/ //	DRIVER( ctribeb )	/* bootleg */
/*TODO*/ //	DRIVER( blockout )	/* TA-0029 (c) 1989 + California Dreams */
/*TODO*/ //	DRIVER( blckout2 )	/* TA-0029 (c) 1989 + California Dreams */
/*TODO*/ //	DRIVER( ddragon3 )	/* TA-0030 (c) 1990 */
/*TODO*/ //	DRIVER( ddrago3b )	/* bootleg */
	/* TA-0031 WWF Wrestlefest */

	/* Stern "Berzerk hardware" games */
/*TODO*/ //	DRIVER( berzerk )	/* (c) 1980 */
/*TODO*/ //	DRIVER( berzerk1 )	/* (c) 1980 */
/*TODO*/ //	DRIVER( frenzy )	/* (c) 1982 */

	/* GamePlan games */
/*TODO*/ //	DRIVER( megatack )	/* (c) 1980 Centuri */
/*TODO*/ //	DRIVER( killcom )	/* (c) 1980 Centuri */
/*TODO*/ //	DRIVER( challeng )	/* (c) 1981 Centuri */
/*TODO*/ //	DRIVER( kaos )		/* (c) 1981 */

	/* "stratovox hardware" games */
/*TODO*/ //	DRIVER( route16 )	/* (c) 1981 Tehkan/Sun + Centuri license */
/*TODO*/ //	DRIVER( route16b )	/* bootleg */
/*TODO*/ //	DRIVER( stratvox )	/* Taito */
/*TODO*/ //	DRIVER( stratvxb )	/* bootleg */
/*TODO*/ //	DRIVER( speakres )	/* no copyright notice */

	/* Zaccaria games */
/*TODO*/ //	DRIVER( monymony )	/* (c) 1983 */
/*TODO*/ //	DRIVER( jackrabt )	/* (c) 1984 */
/*TODO*/ //	DRIVER( jackrab2 )	/* (c) 1984 */
/*TODO*/ //	DRIVER( jackrabs )	/* (c) 1984 */

	/* UPL games */
/*TODO*/ //	DRIVER( nova2001 )	/* UPL-83005 (c) 1983 */
/*TODO*/ //	DRIVER( nov2001u )	/* UPL-83005 (c) [1983] + Universal license */
/*TODO*/ //	DRIVER( pkunwar )	/* [1985?] */
/*TODO*/ //	DRIVER( pkunwarj )	/* [1985?] */
/*TODO*/ //	DRIVER( ninjakd2 )	/* (c) 1987 */
/*TODO*/ //	DRIVER( ninjak2a )	/* (c) 1987 */
/*TODO*/ //	DRIVER( ninjak2b )	/* (c) 1987 */
/*TODO*/ //	DRIVER( rdaction )	/* (c) 1987 + World Games license */
/*TODO*/ //	DRIVER( mnight )	/* (c) 1987 distributed by Kawakus */
/*TODO*/ //	DRIVER( arkarea )	/* UPL-87007 (c) [1988?] */

	/* Williams/Midway TMS34010 games */
/*TODO*/ //	DRIVER( narc )		/* (c) 1988 Williams */
/*TODO*/ //TESTDRIVER( narc3 )		/* (c) 1988 Williams */
/*TODO*/ //	DRIVER( trog )		/* (c) 1990 Midway */
/*TODO*/ //	DRIVER( trog3 )		/* (c) 1990 Midway */
/*TODO*/ //	DRIVER( trogp )		/* (c) 1990 Midway */
/*TODO*/ //	DRIVER( smashtv )	/* (c) 1990 Williams */
/*TODO*/ //	DRIVER( smashtv6 )	/* (c) 1990 Williams */
/*TODO*/ //	DRIVER( smashtv5 )	/* (c) 1990 Williams */
/*TODO*/ //	DRIVER( smashtv4 )	/* (c) 1990 Williams */
/*TODO*/ //	DRIVER( hiimpact )	/* (c) 1990 Williams */
/*TODO*/ //	DRIVER( shimpact )	/* (c) 1991 Midway */
/*TODO*/ //	DRIVER( strkforc )	/* (c) 1991 Midway */
/*TODO*/ //	DRIVER( mk )		/* (c) 1992 Midway */
/*TODO*/ //	DRIVER( mkla1 )		/* (c) 1992 Midway */
/*TODO*/ //	DRIVER( mkla2 )		/* (c) 1992 Midway */
/*TODO*/ //	DRIVER( mkla3 )		/* (c) 1992 Midway */
/*TODO*/ //	DRIVER( mkla4 )		/* (c) 1992 Midway */
/*TODO*/ //	DRIVER( term2 )		/* (c) 1992 Midway */
/*TODO*/ //	DRIVER( totcarn )	/* (c) 1992 Midway */
/*TODO*/ //	DRIVER( totcarnp )	/* (c) 1992 Midway */
/*TODO*/ //	DRIVER( mk2 )		/* (c) 1993 Midway */
/*TODO*/ //	DRIVER( mk2r32 )	/* (c) 1993 Midway */
/*TODO*/ //	DRIVER( mk2r14 )	/* (c) 1993 Midway */
/*TODO*/ //	DRIVER( nbajam )	/* (c) 1993 Midway */
/*TODO*/ //	DRIVER( nbajamr2 )	/* (c) 1993 Midway */
/*TODO*/ //	DRIVER( nbajamte )	/* (c) 1994 Midway */

	/* Cinematronics vector games */
/*TODO*/ //	DRIVER( spacewar )
/*TODO*/ //	DRIVER( barrier )
/*TODO*/ //	DRIVER( starcas )	/* (c) 1980 */
/*TODO*/ //	DRIVER( starcas1 )	/* (c) 1980 */
/*TODO*/ //	DRIVER( tailg )
/*TODO*/ //	DRIVER( ripoff )
/*TODO*/ //	DRIVER( armora )
/*TODO*/ //	DRIVER( wotw )
/*TODO*/ //	DRIVER( warrior )
/*TODO*/ //	DRIVER( starhawk )
/*TODO*/ //	DRIVER( solarq )	/* (c) 1981 */
/*TODO*/ //	DRIVER( boxingb )	/* (c) 1981 */
/*TODO*/ //	DRIVER( speedfrk )
/*TODO*/ //	DRIVER( sundance )
/*TODO*/ //	DRIVER( demon )		/* (c) 1982 Rock-ola */
	/* this one uses 68000+Z80 instead of the Cinematronics CPU */
/*TODO*/ //	DRIVER( cchasm )
/*TODO*/ //	DRIVER( cchasm1 )	/* (c) 1983 Cinematronics / GCE */

	/* "The Pit hardware" games */
/*TODO*/ //	DRIVER( roundup )	/* (c) 1981 Amenip/Centuri */
/*TODO*/ //	DRIVER( fitter )	/* (c) 1981 Taito */
/*TODO*/ //	DRIVER( thepit )	/* (c) 1982 Centuri */
/*TODO*/ //	DRIVER( intrepid )	/* (c) 1983 Nova Games Ltd. */
/*TODO*/ //	DRIVER( intrepi2 )	/* (c) 1983 Nova Games Ltd. */
/*TODO*/ //	DRIVER( portman )	/* (c) 1982 Nova Games Ltd. */
/*TODO*/ //	DRIVER( suprmous )	/* (c) 1982 Taito */
/*TODO*/ //	DRIVER( suprmou2 )	/* (c) 1982 Chu Co. Ltd. */
/*TODO*/ //	DRIVER( machomou )	/* (c) 1982 Techstar */

	/* Valadon Automation games */
        driver_bagman,	/* (c) 1982 */
        driver_bagnard,	/* (c) 1982 */
        driver_bagmans,	/* (c) 1982 + Stern license */
        driver_bagmans2,	/* (c) 1982 + Stern license */
        driver_sbagman,	/* (c) 1984 */
        driver_sbagmans,	/* (c) 1984 + Stern license */
        driver_pickin,	/* (c) 1983 */

	/* Seibu Denshi / Seibu Kaihatsu games */

/*TODO*/ //	DRIVER( empcity )	/* (c) 1986 Seibu Kaihatsu (bootleg?) */
/*TODO*/ //	DRIVER( empcityj )	/* (c) 1986 Taito Corporation (Japan) */
/*TODO*/ //	DRIVER( stfight )	/* (c) 1986 Seibu Kaihatsu (Germany) (bootleg?) */
/*TODO*/ //	DRIVER( dynduke )	/* (c) 1989 Seibu Kaihatsu + Fabtek license */
/*TODO*/ //	DRIVER( dbldyn )	/* (c) 1989 Seibu Kaihatsu + Fabtek license */
/*TODO*/ //	DRIVER( raiden )	/* (c) 1990 Seibu Kaihatsu */
/*TODO*/ //	DRIVER( raidena )	/* (c) 1990 Seibu Kaihatsu */
/*TODO*/ //	DRIVER( raidenk )	/* (c) 1990 Seibu Kaihatsu + IBL Corporation license */
/*TODO*/ //	DRIVER( dcon )		/* (c) 1992 Success */

	/* Tad games (Tad games run on Seibu hardware) */
/*TODO*/ //	DRIVER( cabal )		/* (c) 1988 Tad + Fabtek license */
/*TODO*/ //	DRIVER( cabal2 )	/* (c) 1988 Tad + Fabtek license */
/*TODO*/ //	DRIVER( cabalbl )	/* bootleg */
/*TODO*/ //	DRIVER( toki )		/* (c) 1989 Tad */
/*TODO*/ //	DRIVER( toki2 )		/* (c) 1989 Tad */
/*TODO*/ //	DRIVER( toki3 )		/* (c) 1989 Tad */
/*TODO*/ //	DRIVER( tokiu )		/* (c) 1989 Tad + Fabtek license */
/*TODO*/ //	DRIVER( tokib )		/* bootleg */
/*TODO*/ //	DRIVER( bloodbro )	/* (c) 1990 Tad */
/*TODO*/ //	DRIVER( weststry )	/* bootleg */

	/* Jaleco games */
/*TODO*/ //	DRIVER( exerion )	/* (c) 1983 Jaleco */
/*TODO*/ //	DRIVER( exeriont )	/* (c) 1983 Jaleco + Taito America license */
/*TODO*/ //	DRIVER( exerionb )	/* bootleg */
/*TODO*/ //	DRIVER( formatz )	/* (c) 1984 Jaleco */
/*TODO*/ //	DRIVER( aeroboto )	/* (c) 1984 Williams */

/*TODO*/ //	DRIVER( pinbo )		/* (c) 1984 Jaleco */
/*TODO*/ //	DRIVER( pinbos )	/* (c) 1985 Strike */
/*TODO*/ //	DRIVER( psychic5 )	/* (c) 1987 Jaleco */
/*TODO*/ //	DRIVER( ginganin )	/* (c) 1987 Jaleco */
/*TODO*/ //	DRIVER( cischeat )	/* (c) 1990 Jaleco */
/*TODO*/ //	DRIVER( f1gpstar )	/* (c) 1991 Jaleco */

	/* Jaleco Mega System 1 games */
/*TODO*/ //	DRIVER( lomakai )	/* (c) 1988 (World) */
/*TODO*/ //	DRIVER( makaiden )	/* (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( p47 )		/* (c) 1988 */
/*TODO*/ //	DRIVER( p47j )		/* (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( kickoff )	/* (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( tshingen )	/* (c) 1988 (Japan) */
/*TODO*/ //	DRIVER( astyanax )	/* (c) 1989 */
/*TODO*/ //	DRIVER( lordofk )	/* (c) 1989 (Japan) */
/*TODO*/ //	DRIVER( hachoo )	/* (c) 1989 */
/*TODO*/ //	DRIVER( plusalph )	/* (c) 1989 */
/*TODO*/ //	DRIVER( stdragon )	/* (c) 1989 */
/*TODO*/ //	DRIVER( iganinju )	/* (c) 1989 (Japan) */
/*TODO*/ //	DRIVER( rodland )	/* (c) 1990 */
/*TODO*/ //	DRIVER( rodlandj )	/* (c) 1990 (Japan) */
/*TODO*/ //	DRIVER( 64street )	/* (c) 1991 */
/*TODO*/ //	DRIVER( 64streej )	/* (c) 1991 (Japan) */
/*TODO*/ //	DRIVER( edf )		/* (c) 1991 */
/*TODO*/ //	DRIVER( avspirit )	/* (c) 1991 */
/*TODO*/ //	DRIVER( phantasm )	/* (c) 1991 (Japan) */
/*TODO*/ //	DRIVER( bigstrik )	/* (c) 1992 */
/*TODO*/ //	DRIVER( chimerab )	/* (c) 1993 */
/*TODO*/ //	DRIVER( cybattlr )	/* (c) 1993 */
/*TODO*/ //	DRIVER( peekaboo )	/* (c) 1993 */
/*TODO*/ //	DRIVER( soldamj )	/* (c) 1992 (Japan) */

	/* Video System Co. games */
/*TODO*/ //	DRIVER( pspikes )	/* (c) 1991 */
/*TODO*/ //	DRIVER( svolly91 )	/* (c) 1991 */
/*TODO*/ //	DRIVER( turbofrc )	/* (c) 1991 */
/*TODO*/ //	DRIVER( aerofgt )	/* (c) 1992 */
/*TODO*/ //	DRIVER( aerofgtb )	/* (c) 1992 */
/*TODO*/ //	DRIVER( aerofgtc )	/* (c) 1992 */
/*TODO*/ //TESTDRIVER( unkvsys )

	/* Orca games */
/*TODO*/ //	DRIVER( marineb )	/* (c) 1982 Orca */
/*TODO*/ //	DRIVER( changes )	/* (c) 1982 Orca */
/*TODO*/ //	DRIVER( looper )	/* (c) 1982 Orca */
/*TODO*/ //	DRIVER( springer )	/* (c) 1982 Orca */
/*TODO*/ //	DRIVER( hoccer )	/* (c) 1983 Eastern Micro Electronics, Inc. */
/*TODO*/ //	DRIVER( hoccer2 )	/* (c) 1983 Eastern Micro Electronics, Inc. */
/*TODO*/ //	DRIVER( hopprobo )	/* (c) 1983 Sega */
/*TODO*/ //	DRIVER( wanted )	/* (c) 1984 Sigma Ent. Inc. */

/*TODO*/ //	DRIVER( zodiack )	/* (c) 1983 Orca + Esco Trading Co license */
/*TODO*/ //	DRIVER( dogfight )	/* (c) 1983 Thunderbolt */
/*TODO*/ //	DRIVER( moguchan )	/* (c) 1982 Orca + Eastern Commerce Inc. license (doesn't appear on screen) */
/*TODO*/ //	DRIVER( percuss )	/* (c) 1981 Orca */
        driver_espial,	/* (c) 1983 Thunderbolt, Orca logo is hidden in title screen */
        driver_espiale,	/* (c) 1983 Thunderbolt, Orca logo is hidden in title screen */
	/* Vastar was made by Orca, but when it was finished, Orca had already bankrupted. */
	/* So they sold this game as "Made by Sesame Japan" because they couldn't use */
	/* the name "Orca" */
/*TODO*/ //	DRIVER( vastar )	/* (c) 1983 Sesame Japan */
/*TODO*/ //	DRIVER( vastar2 )	/* (c) 1983 Sesame Japan */


	/* Gaelco games */
/*TODO*/ //	DRIVER( splash )	/* (c) 1992 Gaelco */

	/* Kaneko "AX System" games */
/*TODO*/ //	DRIVER( berlwall )	/* (c) 1991 Kaneko */
/*TODO*/ //	DRIVER( berlwalt )	/* (c) 1991 Kaneko */
/*TODO*/ //	DRIVER( gtmr )		/* (c) 1994 Kaneko */
/*TODO*/ //	DRIVER( gtmre )		/* (c) 1994 Kaneko */
/*TODO*/ //TESTDRIVER( gtmr2 )
/*TODO*/ //TESTDRIVER( shogwarr )

	/* other Kaneko games */
/*TODO*/ //	DRIVER( galpanic )	/* (c) 1990 Kaneko */


/*TODO*/ //	DRIVER( spacefb )	/* (c) [1980?] Nintendo */
/*TODO*/ //	DRIVER( spacefbg )	/* 834-0031 (c) 1980 Gremlin */
/*TODO*/ //	DRIVER( spacefbb )	/* bootleg */
/*TODO*/ //	DRIVER( spacebrd )	/* bootleg */
/*TODO*/ //	DRIVER( spacedem )	/* (c) 1980 Nintendo / Fortrek */
/*TODO*/ //	DRIVER( blueprnt )	/* (c) 1982 Bally Midway (Zilec in ROM 3U, and the programmer names) */
/*TODO*/ //	DRIVER( blueprnj )	/* (c) 1982 Jaleco (Zilec in ROM 3U, and the programmer names) */
/*TODO*/ //	DRIVER( saturn )	/* (c) 1983 Jaleco (Zilec in ROM R6, and the programmer names) */
/*TODO*/ //	DRIVER( omegrace )	/* (c) 1981 Midway */
/*TODO*/ //	DRIVER( dday )		/* (c) 1982 Olympia */
/*TODO*/ //	DRIVER( ddayc )		/* (c) 1982 Olympia + Centuri license */
            driver_gundealr,	/* (c) 1990 Dooyong */
            driver_gundeala,	/* (c) Dooyong */
            driver_yamyam,	/* (c) 1990 Dooyong */
            driver_wiseguy,	/* (c) 1990 Dooyong */
/*TODO*/ //	DRIVER( leprechn )	/* (c) 1982 Tong Electronic */
/*TODO*/ //	DRIVER( potogold )	/* (c) 1982 Tong Electronic */
            driver_hexa,   /* D. R. Korea */
/*TODO*/ //	DRIVER( redalert )	/* (c) 1981 Irem (GDI game) */
/*TODO*/ //	DRIVER( irobot )	/* (c) 1983 Atari */
/*TODO*/ //	DRIVER( spiders )	/* (c) 1981 Sigma Ent. Inc. */
/*TODO*/ //	DRIVER( spiders2 )	/* (c) 1981 Sigma Ent. Inc. */
/*TODO*/ //	DRIVER( stactics )	/* [1981 Sega] */
/*TODO*/ //	DRIVER( exterm )	/* (c) 1989 Premier Technology - a Gottlieb game */
/*TODO*/ //	DRIVER( sharkatt )	/* (c) Pacific Novelty */
/*TODO*/ //	DRIVER( zerozone )	/* (c) 1993 Comad */
/*TODO*/ //	DRIVER( exctsccr )	/* (c) 1983 Alpha Denshi Co. */
/*TODO*/ //	DRIVER( exctscca )	/* (c) 1983 Alpha Denshi Co. */
/*TODO*/ //	DRIVER( exctsccb )	/* bootleg */
/*TODO*/ //	DRIVER( exctscc2 )
/*TODO*/ //	DRIVER( speedbal )	/* (c) 1987 Tecfri */
/*TODO*/ //	DRIVER( sauro )		/* (c) 1987 Tecfri */
/*TODO*/ //	DRIVER( ambush )	/* (c) 1983 Nippon Amuse Co-Ltd */
/*TODO*/ //	DRIVER( starcrus )	/* [1977 Ramtek] */
/*TODO*/ //	DRIVER( shanghai )	/* (c) 1988 Sun Electronics */
/*TODO*/ //	DRIVER( goindol )	/* (c) 1987 Sun a Electronics */
/*TODO*/ //	DRIVER( homo )		/* bootleg */
/*TODO*/ //TESTDRIVER( dlair )
/*TODO*/ //	DRIVER( meteor )	/* (c) 1981 Venture Line */
/*TODO*/ //	DRIVER( bjtwin )	/* (c) 1993 NMK */
/*TODO*/ //	DRIVER( aztarac )	/* (c) 1983 Centuri (vector game) */
/*TODO*/ //	DRIVER( mole )		/* (c) 1982 Yachiyo Electronics, Ltd. */
/*TODO*/ //	DRIVER( gotya )		/* (c) 1981 Game-A-Tron */

	/* Neo Geo games */
	/* the four digits number is the game ID stored at address 0x0108 of the program ROM */
/*TODO*/ //	DRIVER( nam1975 )	/* 0001 (c) 1990 SNK */
/*TODO*/ //	DRIVER( bstars )	/* 0002 (c) 1990 SNK */
/*TODO*/ //	DRIVER( tpgolf )	/* 0003 (c) 1990 SNK */
/*TODO*/ //	DRIVER( mahretsu )	/* 0004 (c) 1990 SNK */
/*TODO*/ //	DRIVER( maglord )	/* 0005 (c) 1990 Alpha Denshi Co */
/*TODO*/ //	DRIVER( maglordh )	/* 0005 (c) 1990 Alpha Denshi Co */
/*TODO*/ //	DRIVER( ridhero )	/* 0006 (c) 1990 SNK */
/*TODO*/ //	DRIVER( alpham2 )	/* 0007 (c) 1991 SNK */
	/* 0008 */
/*TODO*/ //	DRIVER( ncombat )	/* 0009 (c) 1990 Alpha Denshi Co */
/*TODO*/ //	DRIVER( cyberlip )	/* 0010 (c) 1990 SNK */
/*TODO*/ //	DRIVER( superspy )	/* 0011 (c) 1990 SNK */
	/* 0012 */
	/* 0013 */
/*TODO*/ //	DRIVER( mutnat )	/* 0014 (c) 1992 SNK */
	/* 0015 */
/*TODO*/ //	DRIVER( kotm )		/* 0016 (c) 1991 SNK */
/*TODO*/ //	DRIVER( sengoku )	/* 0017 (c) 1991 SNK */
/*TODO*/ //	DRIVER( sengokh )	/* 0017 (c) 1991 SNK */
/*TODO*/ //	DRIVER( burningf )	/* 0018 (c) 1991 SNK */
/*TODO*/ //	DRIVER( burningh )	/* 0018 (c) 1991 SNK */
/*TODO*/ //	DRIVER( lbowling )	/* 0019 (c) 1990 SNK */
/*TODO*/ //	DRIVER( gpilots )	/* 0020 (c) 1991 SNK */
/*TODO*/ //	DRIVER( joyjoy )	/* 0021 (c) 1990 SNK */
/*TODO*/ //	DRIVER( bjourney )	/* 0022 (c) 1990 Alpha Denshi Co */
/*TODO*/ //	DRIVER( quizdais )	/* 0023 (c) 1991 SNK */
/*TODO*/ //	DRIVER( lresort )	/* 0024 (c) 1992 SNK */
/*TODO*/ //	DRIVER( eightman )	/* 0025 (c) 1991 SNK / Pallas */
	/* 0026 Fun Fun Brothers - prototype? */
/*TODO*/ //	DRIVER( minasan )	/* 0027 (c) 1990 Monolith Corp. */
	/* 0028 */
/*TODO*/ //	DRIVER( legendos )	/* 0029 (c) 1991 SNK */
/*TODO*/ //	DRIVER( 2020bb )	/* 0030 (c) 1991 SNK / Pallas */
/*TODO*/ //	DRIVER( 2020bbh )	/* 0030 (c) 1991 SNK / Pallas */
/*TODO*/ //	DRIVER( socbrawl )	/* 0031 (c) 1991 SNK */
/*TODO*/ //	DRIVER( roboarmy )	/* 0032 (c) 1991 SNK */
/*TODO*/ //	DRIVER( fatfury1 )	/* 0033 (c) 1991 SNK */
/*TODO*/ //	DRIVER( fbfrenzy )	/* 0034 (c) 1992 SNK */
	/* 0035 */
/*TODO*/ //	DRIVER( bakatono )	/* 0036 (c) 1991 Monolith Corp. */
/*TODO*/ //	DRIVER( crsword )	/* 0037 (c) 1991 Alpha Denshi Co */
/*TODO*/ //	DRIVER( trally )	/* 0038 (c) 1991 Alpha Denshi Co */
/*TODO*/ //	DRIVER( kotm2 )		/* 0039 (c) 1992 SNK */
/*TODO*/ //	DRIVER( sengoku2 )	/* 0040 (c) 1993 SNK */
/*TODO*/ //	DRIVER( bstars2 )	/* 0041 (c) 1992 SNK */
/*TODO*/ //	DRIVER( quizdai2 )	/* 0042 (c) 1992 SNK */
/*TODO*/ //	DRIVER( 3countb )	/* 0043 (c) 1993 SNK */
/*TODO*/ //	DRIVER( aof )		/* 0044 (c) 1992 SNK */
/*TODO*/ //	DRIVER( samsho )	/* 0045 (c) 1993 SNK */
/*TODO*/ //	DRIVER( tophuntr )	/* 0046 (c) 1994 SNK */
/*TODO*/ //	DRIVER( fatfury2 )	/* 0047 (c) 1992 SNK */
/*TODO*/ //	DRIVER( janshin )	/* 0048 (c) 1994 Aicom */
/*TODO*/ //	DRIVER( androdun )	/* 0049 (c) 1992 Visco */
/*TODO*/ //	DRIVER( ncommand )	/* 0050 (c) 1992 Alpha Denshi Co */
/*TODO*/ //	DRIVER( viewpoin )	/* 0051 (c) 1992 Sammy */
/*TODO*/ //	DRIVER( ssideki )	/* 0052 (c) 1992 SNK */
/*TODO*/ //	DRIVER( wh1 )		/* 0053 (c) 1992 Alpha Denshi Co */
	/* 0054 Crossed Swords 2 (CD only) */
/*TODO*/ //	DRIVER( kof94 )		/* 0055 (c) 1994 SNK */
/*TODO*/ //	DRIVER( aof2 )		/* 0056 (c) 1994 SNK */
/*TODO*/ //	DRIVER( wh2 )		/* 0057 (c) 1993 ADK */
/*TODO*/ //	DRIVER( fatfursp )	/* 0058 (c) 1993 SNK */
/*TODO*/ //	DRIVER( savagere )	/* 0059 (c) 1995 SNK */
/*TODO*/ //	DRIVER( fightfev )	/* 0060 (c) 1994 Viccom */
/*TODO*/ //	DRIVER( ssideki2 )	/* 0061 (c) 1994 SNK */
/*TODO*/ //	DRIVER( spinmast )	/* 0062 (c) 1993 Data East Corporation */
/*TODO*/ //	DRIVER( samsho2 )	/* 0063 (c) 1994 SNK */
/*TODO*/ //	DRIVER( wh2j )		/* 0064 (c) 1994 ADK / SNK */
/*TODO*/ //	DRIVER( wjammers )	/* 0065 (c) 1994 Data East Corporation */
/*TODO*/ //	DRIVER( karnovr )	/* 0066 (c) 1994 Data East Corporation */
/*TODO*/ //	DRIVER( gururin )	/* 0067 (c) 1994 Face */
/*TODO*/ //	DRIVER( pspikes2 )	/* 0068 (c) 1994 Video System Co. */
/*TODO*/ //	DRIVER( fatfury3 )	/* 0069 (c) 1995 SNK */
	/* 0070 */
	/* 0071 */
	/* 0072 */
/*TODO*/ //	DRIVER( panicbom )	/* 0073 (c) 1994 Eighting / Hudson */
/*TODO*/ //	DRIVER( aodk )		/* 0074 (c) 1994 ADK / SNK */
/*TODO*/ //	DRIVER( sonicwi2 )	/* 0075 (c) 1994 Video System Co. */
/*TODO*/ //	DRIVER( zedblade )	/* 0076 (c) 1994 NMK */
	/* 0077 */
/*TODO*/ //	DRIVER( galaxyfg )	/* 0078 (c) 1995 Sunsoft */
/*TODO*/ //	DRIVER( strhoop )	/* 0079 (c) 1994 Data East Corporation */
/*TODO*/ //	DRIVER( quizkof )	/* 0080 (c) 1995 Saurus */
/*TODO*/ //	DRIVER( ssideki3 )	/* 0081 (c) 1995 SNK */
/*TODO*/ //	DRIVER( doubledr )	/* 0082 (c) 1995 Technos */
/*TODO*/ //	DRIVER( pbobble )	/* 0083 (c) 1994 Taito */
/*TODO*/ //	DRIVER( kof95 )		/* 0084 (c) 1995 SNK */
	/* 0085 Shinsetsu Samurai Spirits Bushidoretsuden / Samurai Shodown RPG (CD only) */
/*TODO*/ //	DRIVER( tws96 )		/* 0086 (c) 1996 Tecmo */
/*TODO*/ //	DRIVER( samsho3 )	/* 0087 (c) 1995 SNK */
/*TODO*/ //	DRIVER( stakwin )	/* 0088 (c) 1995 Saurus */
/*TODO*/ //	DRIVER( pulstar )	/* 0089 (c) 1995 Aicom */
/*TODO*/ //	DRIVER( whp )		/* 0090 (c) 1995 ADK / SNK */
	/* 0091 */
/*TODO*/ //	DRIVER( kabukikl )	/* 0092 (c) 1995 Hudson */
/*TODO*/ //	DRIVER( neobombe )	/* 0093 (c) 1997 Hudson */
/*TODO*/ //	DRIVER( gowcaizr )	/* 0094 (c) 1995 Technos */
/*TODO*/ //	DRIVER( rbff1 )		/* 0095 (c) 1995 SNK */
/*TODO*/ //	DRIVER( aof3 )		/* 0096 (c) 1996 SNK */
/*TODO*/ //	DRIVER( sonicwi3 )	/* 0097 (c) 1995 Video System Co. */
	/* 0098 Idol Mahjong - final romance 2 (CD only? not confirmed, MVS might exist) */
	/* 0099 */
/*TODO*/ //	DRIVER( turfmast )	/* 0200 (c) 1996 Nazca */
/*TODO*/ //	DRIVER( mslug )		/* 0201 (c) 1996 Nazca */
/*TODO*/ //	DRIVER( puzzledp )	/* 0202 (c) 1995 Taito (Visco license) */
/*TODO*/ //	DRIVER( mosyougi )	/* 0203 (c) 1995 ADK / SNK */
	/* 0204 ADK World (CD only) */
	/* 0205 Neo-Geo CD Special (CD only) */
/*TODO*/ //	DRIVER( marukodq )	/* 0206 (c) 1995 Takara */
/*TODO*/ //	DRIVER( neomrdo )	/* 0207 (c) 1996 Visco */
/*TODO*/ //	DRIVER( sdodgeb )	/* 0208 (c) 1996 Technos */
/*TODO*/ //	DRIVER( goalx3 )	/* 0209 (c) 1995 Visco */
	/* 0210 */
	/* 0211 Oshidashi Zintrick (CD only? not confirmed, MVS might exist) */
/*TODO*/ //	DRIVER( overtop )	/* 0212 (c) 1996 ADK */
/*TODO*/ //	DRIVER( neodrift )	/* 0213 (c) 1996 Visco */
/*TODO*/ //	DRIVER( kof96 )		/* 0214 (c) 1996 SNK */
/*TODO*/ //	DRIVER( ssideki4 )	/* 0215 (c) 1996 SNK */
/*TODO*/ //	DRIVER( kizuna )	/* 0216 (c) 1996 SNK */
/*TODO*/ //	DRIVER( ninjamas )	/* 0217 (c) 1996 ADK / SNK */
/*TODO*/ //	DRIVER( ragnagrd )	/* 0218 (c) 1996 Saurus */
/*TODO*/ //	DRIVER( pgoal )		/* 0219 (c) 1996 Saurus */
	/* 0220 Choutetsu Brikin'ger - iron clad (CD only? not confirmed, MVS might exist) */
/*TODO*/ //	DRIVER( magdrop2 )	/* 0221 (c) 1996 Data East Corporation */
/*TODO*/ //	DRIVER( samsho4 )	/* 0222 (c) 1996 SNK */
/*TODO*/ //	DRIVER( rbffspec )	/* 0223 (c) 1996 SNK */
/*TODO*/ //	DRIVER( twinspri )	/* 0224 (c) 1996 ADK */
/*TODO*/ //	DRIVER( wakuwak7 )	/* 0225 (c) 1996 Sunsoft */
	/* 0226 */
/*TODO*/ //	DRIVER( stakwin2 )	/* 0227 (c) 1996 Saurus */
	/* 0228 */
	/* 0229 King of Fighters '96 CD Collection (CD only) */
/*TODO*/ //	DRIVER( breakers )	/* 0230 (c) 1996 Visco */
/*TODO*/ //	DRIVER( miexchng )	/* 0231 (c) 1997 Face */
/*TODO*/ //	DRIVER( kof97 )		/* 0232 (c) 1997 SNK */
/*TODO*/ //	DRIVER( magdrop3 )	/* 0233 (c) 1997 Data East Corporation */
/*TODO*/ //	DRIVER( lastblad )	/* 0234 (c) 1997 SNK */
/*TODO*/ //	DRIVER( puzzldpr )	/* 0235 (c) 1997 Taito (Visco license) */
/*TODO*/ //	DRIVER( irrmaze )	/* 0236 (c) 1997 SNK / Saurus */
/*TODO*/ //	DRIVER( popbounc )	/* 0237 (c) 1997 Video System Co. */
/*TODO*/ //	DRIVER( shocktro )	/* 0238 (c) 1997 Saurus */
/*TODO*/ //	DRIVER( blazstar )	/* 0239 (c) 1998 Yumekobo */
/*TODO*/ //	DRIVER( rbff2 )		/* 0240 (c) 1998 SNK */
/*TODO*/ //	DRIVER( mslug2 )	/* 0241 (c) 1998 SNK */
/*TODO*/ //	DRIVER( kof98 )		/* 0242 (c) 1998 SNK */
/*TODO*/ //	DRIVER( lastbld2 )	/* 0243 (c) 1998 SNK */
/*TODO*/ //	DRIVER( neocup98 )	/* 0244 (c) 1998 SNK */
/*TODO*/ //	DRIVER( breakrev )	/* 0245 (c) 1998 Visco */
/*TODO*/ //	DRIVER( shocktr2 )	/* 0246 (c) 1998 Saurus */
/*TODO*/ //	DRIVER( flipshot )	/* 0247 (c) 1998 Visco */
/*TODO*/ //TESTDRIVER( pbobbl2n )	/* 0248 (c) 1999 Taito (SNK license) */
/*TODO*/ //TESTDRIVER( ctomaday )	/* 0249 (c) 1999 Visco */
/*TODO*/ //TESTDRIVER( mslugx )	/* 0250 (c) 1999 SNK */
/*TODO*/ //TESTDRIVER( kof99 )		/* 0251 (c) 1999 SNK */
/*TODO*/ //TESTDRIVER( garou )		/* 0253 (c) 1999 SNK */
        null /* end of array */
    };
}
