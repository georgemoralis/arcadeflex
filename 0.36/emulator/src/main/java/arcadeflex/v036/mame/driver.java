/**
 * ported to v0.36
 */
/**
 * Changelog
 * =========
 * 21/12/2022 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.mame;

//mame imports
import static arcadeflex.v036.mame.driverH.*;
//drivers imports
import static arcadeflex.v036.drivers.bankp.*;
import static arcadeflex.v036.drivers.docastle.*;
import static arcadeflex.v036.drivers.kangaroo.*;
import static arcadeflex.v036.drivers.ladybug.*;
import static arcadeflex.v036.drivers.blueprnt.*;
import static arcadeflex.v036.drivers.bagman.*;
import static arcadeflex.v036.drivers.bombjack.*;
import static arcadeflex.v036.drivers.cclimber.*;
import static arcadeflex.v036.drivers.cop01.*;
import static arcadeflex.v036.drivers._1942.*;
import static arcadeflex.v036.drivers.exedexes.*;
import static arcadeflex.v036.drivers.appoooh.*;
import static arcadeflex.v036.drivers.vulgus.*;
import static arcadeflex.v036.drivers.espial.*;
import static arcadeflex.v036.drivers.marineb.*;
import static arcadeflex.v036.drivers.funkybee.*;
import static arcadeflex.v036.drivers.nova2001.*;
import static arcadeflex.v036.drivers.pkunwar.*;
import static arcadeflex.v036.drivers.pooyan.*;
import static arcadeflex.v036.drivers.kyugo.*;
import static arcadeflex.v036.drivers.scramble.*;
import static arcadeflex.v036.drivers.amidar.*;
import static arcadeflex.v036.drivers.frogger.*;
import static arcadeflex.v036.drivers.scobra.*;
import static arcadeflex.v036.drivers.fastfred.*;
import static arcadeflex.v036.drivers.yamato.*;
import static arcadeflex.v036.drivers.galaxian.*;
import static arcadeflex.v036.drivers.hexa.*;
import static arcadeflex.v036.drivers.higemaru.*;
import static arcadeflex.v036.drivers.jack.*;
import static arcadeflex.v036.drivers.zodiack.*;
import static arcadeflex.v036.drivers.galaga.*;
import static arcadeflex.v036.drivers.digdug.*;
import static arcadeflex.v036.drivers.pengo.*;
import static arcadeflex.v036.drivers.pacman.*;
import static arcadeflex.v036.drivers.pbaction.*;
import static arcadeflex.v036.drivers.rallyx.*;
import static arcadeflex.v036.drivers.retofinv.*;
import static arcadeflex.v036.drivers.wiping.*;
import static arcadeflex.v036.drivers.wiz.*;
import static arcadeflex.v036.drivers.zaxxon.*;
import static arcadeflex.v036.drivers.superqix.*;
import static arcadeflex.v036.drivers.tehkanwc.*;
import static arcadeflex.v036.drivers.kchamp.*;
import static arcadeflex.v036.drivers.pingpong.*;
import static arcadeflex.v036.drivers.solomon.*;
import static arcadeflex.v036.drivers.minivadr.*;
import static arcadeflex.v036.drivers.bosco.*;
import static arcadeflex.v036.drivers.berzerk.*;
import static arcadeflex.v036.drivers.sauro.*;
import static arcadeflex.v036.drivers._1943.*;
import static arcadeflex.v036.drivers.gunsmoke.*;
import static arcadeflex.v036.drivers.champbas.*;
import static arcadeflex.v036.drivers.ambush.*;
import static arcadeflex.v036.drivers.exctsccr.*;
import static arcadeflex.v036.drivers.locomotn.*;
import static arcadeflex.v036.drivers.congo.*;
import static arcadeflex.v036.drivers.wc90.*;
import static arcadeflex.v036.drivers.wc90b.*;
import static arcadeflex.v036.drivers.speedbal.*;
import static arcadeflex.v036.drivers.commando.*;
import static arcadeflex.v036.drivers.galivan.*;
import static arcadeflex.v036.drivers.mrdo.*;
import static arcadeflex.v036.drivers.blktiger.*;
import static arcadeflex.v036.drivers.lwings.*;
import static arcadeflex.v036.drivers.stfight.*;
import static arcadeflex.v036.drivers.gundealr.*;
import static arcadeflex.v036.drivers.tecmo.*;
import static arcadeflex.v036.drivers.timeplt.*;
import static arcadeflex.v036.drivers.airbustr.*;
import static arcadeflex.v036.drivers.ninjakd2.*;

public class driver {

    public static GameDriver drivers[]
            = {
                /* "Pacman hardware" games */
                driver_pacman, /* (c) 1980 Namco */
                driver_pacmanjp, /* (c) 1980 Namco */
                driver_pacmanm, /* (c) 1980 Midway */
                driver_npacmod, /* (c) 1981 Namco */
                driver_pacmod, /* (c) 1981 Midway */
                driver_hangly, /* hack */
                driver_hangly2, /* hack */
                driver_puckman, /* hack */
                driver_pacheart, /* hack */
                driver_piranha, /* hack */
                driver_pacplus,
                driver_mspacman, /* (c) 1981 Midway (but it's a bootleg) */ /* made by Gencomp */
                driver_mspacatk, /* hack */
                driver_pacgal, /* hack */
                driver_maketrax, /* (c) 1981 Williams, high score table says KRL (fur Kural) */
                driver_crush, /* (c) 1981 Kural Samno Electric Ltd */
                driver_crush2, /* (c) 1981 Kural Esco Electric Ltd - bootleg? */
                driver_crush3, /* Kural Electric Ltd - bootleg? */
                driver_mbrush, /* 1981 bootleg */
                driver_paintrlr, /* 1981 bootleg */
                driver_eyes, /* (c) 1982 Digitrex Techstar + "Rockola presents" */
                driver_eyes2, /* (c) 1982 Techstar + "Rockola presents" */
                driver_mrtnt, /* (c) 1983 Telko */
                driver_ponpoko, /* (c) 1982 Sigma Ent. Inc. */
                driver_ponpokov, /* (c) 1982 Sigma Ent. Inc. + Venture Line license */
                driver_lizwiz, /* (c) 1985 Techstar + "Sunn presents" */
                driver_theglob, /* (c) 1983 Epos Corporation */
                driver_beastf, /* (c) 1984 Epos Corporation */
                driver_jumpshot,
                driver_dremshpr, /* (c) 1982 Sanritsu */
                driver_vanvan, /* (c) 1983 Karateco (bootleg?) */
                driver_vanvans, /* (c) 1983 Sanritsu */
                driver_alibaba, /* (c) 1982 Sega */
                driver_pengo, /* 834-0386 (c) 1982 Sega */
                driver_pengo2, /* 834-0386 (c) 1982 Sega */
                driver_pengo2u, /* 834-0386 (c) 1982 Sega */
                driver_penta, /* bootleg */
                /*TODO*///	driver_jrpacman,	/* (c) 1983 Midway */
                /* "Galaxian hardware" games */
                driver_galaxian, /* (c) Namco */
                driver_galmidw, /* (c) Midway */
                driver_superg, /* hack */
                driver_galaxb, /* bootleg */
                driver_galapx, /* hack */
                driver_galap1, /* hack */
                driver_galap4, /* hack */
                driver_galturbo, /* hack */
                driver_swarm, /* hack */
                driver_pisces, /* ? */
                driver_uniwars, /* (c) Irem */
                driver_gteikoku, /* (c) Irem */
                driver_spacbatt, /* bootleg */
                driver_warofbug, /* (c) 1981 Armenia */
                driver_redufo, /* ? */
                driver_pacmanbl, /* bootleg */
                driver_devilfsg, /* (c) 1984 Vision / Artic (bootleg?) */
                driver_zigzag, /* (c) 1982 LAX */
                driver_zigzag2, /* (c) 1982 LAX */
                driver_jumpbug, /* (c) 1981 Rock-ola */
                driver_jumpbugb, /* (c) 1981 Sega */
                driver_levers, /* (c) 1983 Rock-ola */
                driver_azurian, /* (c) 1982 Rait Electronics Ltd */
                driver_orbitron, /* Signatron USA */
                driver_mooncrgx, /* bootleg */
                driver_mooncrst, /* (c) 1980 Nichibutsu */
                driver_mooncrsg, /* (c) 1980 Gremlin */
                driver_smooncrs, /* Gremlin */
                driver_mooncrsb, /* bootleg */
                driver_mooncrs2, /* bootleg */
                driver_fantazia, /* bootleg */
                driver_eagle, /* (c) Centuri */
                driver_eagle2, /* (c) Centuri */
                driver_moonqsr, /* (c) 1980 Nichibutsu */
                driver_checkman, /* (c) 1982 Zilec-Zenitone */
                driver_checkmaj, /* (c) 1982 Jaleco (Zenitone/Zilec in ROM CM4, and the programmer names) */
                driver_streakng, /* [1980] Shoei */
                driver_blkhole, /* TDS (Tokyo Denshi Sekkei) */
                driver_moonal2, /* Nichibutsu */
                driver_moonal2b, /* Nichibutsu */
                driver_kingball, /* (c) 1980 Namco */
                driver_kingbalj, /* (c) 1980 Namco */
                /* "Scramble hardware" (and variations) games */
                driver_scramble, /* GX387 (c) 1981 Konami */
                driver_scrambls, /* GX387 (c) 1981 Stern */
                /*TODO*///	driver_scramblb,	/* bootleg */
                driver_atlantis, /* (c) 1981 Comsoft */
                driver_atlants2, /* (c) 1981 Comsoft */
                driver_theend, /* (c) 1980 Konami */
                driver_theends, /* (c) 1980 Stern */
                driver_ckongs, /* bootleg */
                driver_froggers, /* bootleg */
                driver_amidars, /* (c) 1982 Konami */
                driver_triplep, /* (c) 1982 KKI */
                driver_knockout, /* (c) 1982 KKK */
                driver_mariner, /* (c) 1981 Amenip */
                driver_mars, /* (c) 1981 Artic */
                driver_devilfsh, /* (c) 1982 Artic */
                driver_newsin7, /* (c) 1983 ATW USA, Inc. */
                driver_hotshock, /* (c) 1982 E.G. Felaco */
                /*TODO*///	driver_hunchbks,	/* (c) 1983 Century */
                driver_scobra, /* GX316 (c) 1981 Konami */
                driver_scobras, /* GX316 (c) 1981 Stern */
                driver_scobrab, /* GX316 (c) 1981 Karateco (bootleg?) */
                driver_stratgyx, /* GX306 (c) 1981 Konami */
                driver_stratgys, /* GX306 (c) 1981 Stern */
                driver_armorcar, /* (c) 1981 Stern */
                driver_armorca2, /* (c) 1981 Stern */
                driver_moonwar2, /* (c) 1981 Stern */
                driver_monwar2a, /* (c) 1981 Stern */
                driver_spdcoin, /* (c) 1984 Stern */
                driver_darkplnt, /* (c) 1982 Stern */
                driver_tazmania, /* (c) 1982 Stern */
                driver_tazmani2, /* (c) 1982 Stern */
                driver_calipso, /* (c) 1982 Tago */
                driver_anteater, /* (c) 1982 Tago */
                driver_rescue, /* (c) 1982 Stern */
                driver_minefld, /* (c) 1983 Stern */
                driver_losttomb, /* (c) 1982 Stern */
                driver_losttmbh, /* (c) 1982 Stern */
                driver_superbon, /* bootleg */
                driver_hustler, /* GX343 (c) 1981 Konami */
                driver_billiard, /* bootleg */
                driver_hustlerb, /* bootleg */
                driver_frogger, /* GX392 (c) 1981 Konami */
                driver_frogseg1, /* (c) 1981 Sega */
                driver_frogseg2, /* 834-0068 (c) 1981 Sega */
                driver_froggrmc, /* 800-3110 (c) 1981 Sega */
                driver_amidar, /* GX337 (c) 1981 Konami */
                driver_amidaru, /* GX337 (c) 1982 Konami + Stern license */
                driver_amidaro, /* GX337 (c) 1982 Konami + Olympia license */
                driver_amigo, /* bootleg */
                driver_turtles, /* (c) 1981 Stern */
                driver_turpin, /* (c) 1981 Sega */
                driver_600, /* GX353 (c) 1981 Konami */
                driver_flyboy, /* (c) 1982 Kaneko */
                driver_flyboyb, /* bootleg */
                driver_fastfred, /* (c) 1982 Atari */
                driver_jumpcoas, /* (c) 1983 Kaneko */
                /* "Crazy Climber hardware" games */
                driver_cclimber, /* (c) 1980 Nichibutsu */
                driver_cclimbrj, /* (c) 1980 Nichibutsu */
                driver_ccboot, /* bootleg */
                driver_ccboot2, /* bootleg */
                driver_ckong, /* (c) 1981 Falcon */
                driver_ckonga, /* (c) 1981 Falcon */
                driver_ckongjeu, /* bootleg */
                driver_ckongo, /* bootleg */
                driver_ckongalc, /* bootleg */
                driver_monkeyd, /* bootleg */
                driver_rpatrolb, /* bootleg */
                driver_silvland, /* Falcon */
                driver_yamato, /* (c) 1983 Sega */
                driver_yamato2, /* (c) 1983 Sega */
                driver_swimmer, /* (c) 1982 Tehkan */
                driver_swimmera, /* (c) 1982 Tehkan */
                driver_guzzler, /* (c) 1983 Tehkan */
                /*TODO*///
                /*TODO*///	/* Nichibutsu games */
                /*TODO*///	driver_friskyt,	/* (c) 1981 */
                /*TODO*///	driver_radrad,	/* (c) 1982 Nichibutsu USA */
                /*TODO*///	driver_seicross,	/* (c) 1984 + Alice */
                /*TODO*///	driver_sectrzon,	/* (c) 1984 + Alice */
                driver_wiping, /* (c) 1982 */
                driver_rugrats, /* (c) 1983 */
                driver_cop01, /* (c) 1985 */
                driver_cop01a, /* (c) 1985 */
                /*TODO*///	driver_terracre,	/* (c) 1985 */
                /*TODO*///	driver_terracrb,	/* (c) 1985 */
                /*TODO*///	driver_terracra,	/* (c) 1985 */
                driver_galivan, /* (c) 1985 */
                driver_galivan2, /* (c) 1985 */
                driver_dangar, /* (c) 1986 */
                driver_dangar2, /* (c) 1986 */
                driver_dangarb, /* bootleg */
                driver_ninjemak, /* (c) 1986 (US?) */
                driver_youma, /* (c) 1986 (Japan) */
                /*TODO*///	driver_terraf,	/* (c) 1987 */
                /*TODO*///	driver_terrafu,	/* (c) 1987 Nichibutsu USA */
                /*TODO*///	driver_kodure,	/* (c) 1987 (Japan) */
                /*TODO*///	driver_armedf,	/* (c) 1988 */
                /*TODO*///	driver_cclimbr2,	/* (c) 1988 (Japan) */
                /*TODO*///
                /*TODO*///	/* "Phoenix hardware" (and variations) games */
                /*TODO*///	driver_phoenix,	/* (c) 1980 Amstar */
                /*TODO*///	driver_phoenixa,	/* (c) 1980 Amstar + Centuri license */
                /*TODO*///	driver_phoenixt,	/* (c) 1980 Taito */
                /*TODO*///	driver_phoenix3,	/* bootleg */
                /*TODO*///	driver_phoenixc,	/* bootleg */
                /*TODO*///	driver_pleiads,	/* (c) 1981 Tehkan */
                /*TODO*///	driver_pleiadbl,	/* bootleg */
                /*TODO*///	driver_pleiadce,	/* (c) 1981 Centuri + Tehkan */
                /*TODO*///	driver_naughtyb,	/* (c) 1982 Jaleco */
                /*TODO*///	driver_naughtya,	/* bootleg */
                /*TODO*///	driver_naughtyc,	/* (c) 1982 Jaleco + Cinematronics */
                /*TODO*///	driver_popflame,	/* (c) 1982 Jaleco */
                /*TODO*///	driver_popflama,	/* (c) 1982 Jaleco */
                /*TODO*////*TESTDRIVER*/driver_popflamb,
                /*TODO*///
                /*TODO*///	/* Namco games (plus some intruders on similar hardware) */
                /*TODO*///	driver_geebee,	/* [1978] Namco */
                /*TODO*///	driver_geebeeg,	/* [1978] Gremlin */
                /*TODO*///	driver_bombbee,	/* [1979] Namco */
                /*TODO*///	driver_cutieq,	/* (c) 1979 Namco */
                /*TODO*///	driver_navalone,	/* (c) 1980 Namco */
                /*TODO*///	driver_kaitei,	/* [1980] K.K. Tokki */
                /*TODO*///	driver_kaitein,	/* [1980] Namco */
                /*TODO*///	driver_sos,		/* [1980] Namco */
                /*TODO*///	driver_tankbatt,	/* (c) 1980 Namco */
                /*TODO*///	driver_warpwarp,	/* (c) 1981 Namco */
                /*TODO*///	driver_warpwarr,	/* (c) 1981 Rock-ola - the high score table says "NAMCO" */
                /*TODO*///	driver_warpwar2,	/* (c) 1981 Rock-ola - the high score table says "NAMCO" */
                driver_rallyx, /* (c) 1980 Namco */
                driver_rallyxm, /* (c) 1980 Midway */
                driver_nrallyx, /* (c) 1981 Namco */
                driver_jungler, /* GX327 (c) 1981 Konami */
                driver_junglers, /* GX327 (c) 1981 Stern */
                driver_locomotn, /* GX359 (c) 1982 Konami + Centuri license */
                driver_gutangtn, /* GX359 (c) 1982 Konami + Sega license */
                driver_cottong, /* bootleg */
                driver_commsega, /* (c) 1983 Sega */
                /* the following ones all have a custom I/O chip */
                driver_bosco, /* (c) 1981 */
                driver_boscoo, /* (c) 1981 */
                driver_boscomd, /* (c) 1981 Midway */
                driver_boscomdo, /* (c) 1981 Midway */
                driver_galaga, /* (c) 1981 */
                driver_galagamw, /* (c) 1981 Midway */
                driver_galagads, /* hack */
                driver_gallag, /* bootleg */
                driver_galagab2, /* bootleg */
                driver_galaga84, /* hack */
                driver_nebulbee, /* hack */
                driver_digdug, /* (c) 1982 */
                driver_digdugb, /* (c) 1982 */
                driver_digdugat, /* (c) 1982 Atari */
                driver_dzigzag, /* bootleg */
                /*TODO*///	driver_xevious,	/* (c) 1982 */
                /*TODO*///	driver_xeviousa,	/* (c) 1982 + Atari license */
                /*TODO*///	driver_xevios,	/* bootleg */
                /*TODO*///	driver_sxevious,	/* (c) 1984 */
                /*TODO*///	driver_superpac,	/* (c) 1982 */
                /*TODO*///	driver_superpcm,	/* (c) 1982 Midway */
                /*TODO*///	driver_pacnpal,	/* (c) 1983 */
                /*TODO*///	driver_pacnchmp,	/* (c) 1983 */
                /*TODO*///	driver_phozon,	/* (c) 1983 */
                /*TODO*///	driver_mappy,		/* (c) 1983 */
                /*TODO*///	driver_mappyjp,	/* (c) 1983 */
                /*TODO*///	driver_digdug2,	/* (c) 1985 */
                /*TODO*///	driver_digdug2a,	/* (c) 1985 */
                /*TODO*///	driver_todruaga,	/* (c) 1984 */
                /*TODO*///	driver_todruagb,	/* (c) 1984 */
                /*TODO*///	driver_motos,		/* (c) 1985 */
                /*TODO*///	driver_grobda,	/* (c) 1984 */
                /*TODO*///	driver_grobda2,	/* (c) 1984 */
                /*TODO*///	driver_grobda3,	/* (c) 1984 */
                /*TODO*///	driver_gaplus,	/* (c) 1984 */
                /*TODO*///	driver_gaplusa,	/* (c) 1984 */
                /*TODO*///	driver_galaga3,	/* (c) 1984 */
                /*TODO*///	driver_galaga3a,	/* (c) 1984 */
                /*TODO*///	/* Z8000 games */
                /*TODO*///	driver_polepos,	/* (c) 1982  */
                /*TODO*///	driver_poleposa,	/* (c) 1982 + Atari license */
                /*TODO*///	driver_polepos1,	/* (c) 1982 Atari */
                /*TODO*///	driver_topracer,	/* bootleg */
                /*TODO*///	driver_polepos2,	/* (c) 1983 */
                /*TODO*///	driver_poleps2a,	/* (c) 1983 + Atari license */
                /*TODO*///	driver_poleps2b,	/* bootleg */
                /*TODO*///	driver_poleps2c,	/* bootleg */
                /*TODO*///	/* no custom I/O in the following, HD63701 (or compatible) microcontroller instead */
                /*TODO*///	driver_pacland,	/* (c) 1984 */
                /*TODO*///	driver_pacland2,	/* (c) 1984 */
                /*TODO*///	driver_pacland3,	/* (c) 1984 */
                /*TODO*///	driver_paclandm,	/* (c) 1984 Midway */
                /*TODO*///	driver_drgnbstr,	/* (c) 1984 */
                /*TODO*///	driver_skykid,	/* (c) 1985 */
                /*TODO*///	driver_baraduke,	/* (c) 1985 */
                /*TODO*///	driver_metrocrs,	/* (c) 1985 */
                /*TODO*///
                /*TODO*///	/* Namco System 86 games */
                /*TODO*///	driver_hopmappy,	/* (c) 1986 */
                /*TODO*///	driver_skykiddx,	/* (c) 1986 */
                /*TODO*///	driver_skykiddo,	/* (c) 1986 */
                /*TODO*///	driver_roishtar,	/* (c) 1986 */
                /*TODO*///	driver_genpeitd,	/* (c) 1986 */
                /*TODO*///	driver_rthunder,	/* (c) 1986 new version */
                /*TODO*///	driver_rthundro,	/* (c) 1986 old version */
                /*TODO*///	driver_wndrmomo,	/* (c) 1987 */
                /*TODO*///
                /*TODO*///	/* Namco System 1 games */
                /*TODO*///	driver_shadowld,	/* (c) 1987 */
                /*TODO*///	driver_youkaidk,	/* (c) 1987 (Japan new version) */
                /*TODO*///	driver_yokaidko,	/* (c) 1987 (Japan old version) */
                /*TODO*///	driver_dspirit,	/* (c) 1987 new version */
                /*TODO*///	driver_dspirito,	/* (c) 1987 old version */
                /*TODO*///	driver_blazer,	/* (c) 1987 (Japan) */
                /*TODO*///	driver_quester,	/* (c) 1987 (Japan) */
                /*TODO*///	driver_pacmania,	/* (c) 1987 */
                /*TODO*///	driver_pacmanij,	/* (c) 1987 (Japan) */
                /*TODO*///	driver_galaga88,	/* (c) 1987 */
                /*TODO*///	driver_galag88b,	/* (c) 1987 */
                /*TODO*///	driver_galag88j,	/* (c) 1987 (Japan) */
                /*TODO*///	driver_ws,		/* (c) 1988 (Japan) */
                /*TODO*///	driver_berabohm,	/* (c) 1988 (Japan) */
                /*TODO*///	/* 1988 Alice in Wonderland (English version of Marchen maze) */
                /*TODO*///	driver_mmaze,		/* (c) 1988 (Japan) */
                /*TODO*////*TESTDRIVER*/driver_bakutotu,	/* (c) 1988 */
                /*TODO*///	driver_wldcourt,	/* (c) 1988 (Japan) */
                /*TODO*///	driver_splatter,	/* (c) 1988 (Japan) */
                /*TODO*///	driver_faceoff,	/* (c) 1988 (Japan) */
                /*TODO*///	driver_rompers,	/* (c) 1989 (Japan) */
                /*TODO*///	driver_romperso,	/* (c) 1989 (Japan) */
                /*TODO*///	driver_blastoff,	/* (c) 1989 (Japan) */
                /*TODO*///	driver_ws89,		/* (c) 1989 (Japan) */
                /*TODO*///	driver_dangseed,	/* (c) 1989 (Japan) */
                /*TODO*///	driver_ws90,		/* (c) 1990 (Japan) */
                /*TODO*///	driver_pistoldm,	/* (c) 1990 (Japan) */
                /*TODO*///	driver_soukobdx,	/* (c) 1990 (Japan) */
                /*TODO*///	driver_puzlclub,	/* (c) 1990 (Japan) */
                /*TODO*///	driver_tankfrce,	/* (c) 1991 (US) */
                /*TODO*///	driver_tankfrcj,	/* (c) 1991 (Japan) */
                /*TODO*///
                /*TODO*///	/* Namco System 2 games */
                /*TODO*////*TESTDRIVER*/driver_finallap,	/* 87.12 Final Lap */
                /*TODO*////*TESTDRIVER*/driver_finalapd,	/* 87.12 Final Lap */
                /*TODO*////*TESTDRIVER*/driver_finalapc,	/* 87.12 Final Lap */
                /*TODO*////*TESTDRIVER*/driver_finlapjc,	/* 87.12 Final Lap */
                /*TODO*////*TESTDRIVER*/driver_finlapjb,	/* 87.12 Final Lap */
                /*TODO*///	driver_assault,	/* (c) 1988 */
                /*TODO*///	driver_assaultj,	/* (c) 1988 (Japan) */
                /*TODO*///	driver_assaultp,	/* (c) 1988 (Japan) */
                /*TODO*////*TESTDRIVER*/driver_metlhawk,	/* (c) 1988 */
                /*TODO*///	driver_mirninja,	/* (c) 1988 (Japan) */
                /*TODO*///	driver_ordyne,	/* (c) 1988 */
                /*TODO*///	driver_phelios,	/* (c) 1988 (Japan) */
                /*TODO*///	driver_burnforc,	/* (c) 1989 (Japan) */
                /*TODO*////*TESTDRIVER*/driver_dirtfoxj,	/* (c) 1989 (Japan) */
                /*TODO*///	driver_finehour,	/* (c) 1989 (Japan) */
                /*TODO*////*TESTDRIVER*/driver_fourtrax,	/* 89.11 */
                /*TODO*///	driver_marvland,	/* (c) 1989 (US) */
                /*TODO*///	driver_marvlanj,	/* (c) 1989 (Japan) */
                /*TODO*///	driver_valkyrie,	/* (c) 1989 (Japan) */
                /*TODO*///	DRIVER ( kyukaidk,	/* (c) 1990 (Japan) */
                /*TODO*///	DRIVER ( kyukaido,	/* (c) 1990 (Japan) */
                /*TODO*///	driver_dsaber,	/* (c) 1990 */
                /*TODO*///	driver_dsaberj,	/* (c) 1990 (Japan) */
                /*TODO*///	driver_rthun2,	/* (c) 1990 */
                /*TODO*///	driver_rthun2j,	/* (c) 1990 (Japan) */
                /*TODO*////*TESTDRIVER*/driver_finalap2,	/* 90.8  Final Lap 2 */
                /*TODO*////*TESTDRIVER*/driver_finalp2j,	/* 90.8  Final Lap 2 (Japan) */
                /*TODO*///	/* 91.3  Steel Gunner */
                /*TODO*///	/* 91.7  Golly Ghost */
                /*TODO*///	/* 91.9  Super World Stadium */
                /*TODO*////*TESTDRIVER*/driver_sgunner2,	/* (c) 1991 (Japan) */
                /*TODO*///	driver_cosmogng,	/* (c) 1991 (US) */
                /*TODO*///	driver_cosmognj,	/* (c) 1991 (Japan) */
                /*TODO*////*TESTDRIVER*/driver_finalap3,	/* 92.9  Final Lap 3 */
                /*TODO*////*TESTDRIVER*/driver_suzuka8h,
                /*TODO*///	/* 92.8  Bubble Trouble */
                /*TODO*///	driver_sws92,		/* (c) 1992 (Japan) */
                /*TODO*///	/* 93.4  Lucky & Wild */
                /*TODO*////*TESTDRIVER*/driver_suzuk8h2,
                /*TODO*///	driver_sws93,		/* (c) 1993 (Japan) */
                /*TODO*///	/* 93.6  Super World Stadium '93 */
                /*TODO*///
                /*TODO*////*
                /*TODO*///>other remaining Namco games
                /*TODO*///>-----------------------------------------------------
                /*TODO*///>Libble Rabble board
                /*TODO*///>83.12 Libble Rabble (The first Japanese game that uses M68K)
                /*TODO*///>86.4  Toy Pop
                /*TODO*///>
                /*TODO*///>-----------------------------------------------------
                /*TODO*///>NA-1 system
                /*TODO*///>92.10 F/A
                /*TODO*///
                /*TODO*///*/
                /*TODO*///
                /*TODO*///	/* Universal games */
                /*TODO*///	driver_cosmicg,	/* 7907 (c) 1979 */
                /*TODO*///	driver_cosmica,	/* 7910 (c) [1979] */
                /*TODO*///	driver_cosmica2,	/* 7910 (c) 1979 */
                /*TODO*///	driver_panic,		/* (c) 1980 */
                /*TODO*///	driver_panica,	/* (c) 1980 */
                /*TODO*///	driver_panicger,	/* (c) 1980 */
                /*TODO*///	driver_magspot2,	/* 8013 (c) [1980] */
                /*TODO*///	driver_devzone,	/* 8022 (c) [1980] */
                /*TODO*///	driver_nomnlnd,	/* (c) [1980?] */
                /*TODO*///	driver_nomnlndg,	/* (c) [1980?] + Gottlieb */
                /*TODO*///	driver_cheekyms,	/* (c) [1980?] */
                driver_ladybug, /* (c) 1981 */
                driver_ladybugb, /* bootleg */
                driver_snapjack, /* (c) */
                driver_cavenger, /* (c) 1981 */
                driver_mrdo, /* (c) 1982 */
                driver_mrdot, /* (c) 1982 + Taito license */
                driver_mrdofix, /* (c) 1982 + Taito license */
                driver_mrlo, /* bootleg */
                driver_mrdu, /* bootleg */
                driver_mrdoy, /* bootleg */
                driver_yankeedo, /* bootleg */
                driver_docastle,/* (c) 1983 */
                driver_docastl2,/* (c) 1983 */
                driver_douni,/* (c) 1983 */
                driver_dorunrun,/* (c) 1984 */
                driver_dorunru2,/* (c) 1984 */
                driver_dorunruc,/* (c) 1984 */
                driver_spiero,/* (c) 1987 */
                driver_dowild,/* (c) 1984 */
                driver_jjack,/* (c) 1984 */
                driver_kickridr,/* (c) 1984 */
                /*TODO*///	/* Nintendo games */
                /*TODO*///	driver_radarscp,	/* (c) 1980 Nintendo */
                /*TODO*///	driver_dkong,		/* (c) 1981 Nintendo of America */
                /*TODO*///	driver_dkongjp,	/* (c) 1981 Nintendo */
                /*TODO*///	driver_dkongjpo,	/* (c) 1981 Nintendo */
                /*TODO*///	driver_dkongjr,	/* (c) 1982 Nintendo of America */
                /*TODO*///	driver_dkngjrjp,	/* no copyright notice */
                /*TODO*///	driver_dkjrjp,	/* (c) 1982 Nintendo */
                /*TODO*///	driver_dkjrbl,	/* (c) 1982 Nintendo of America */
                /*TODO*///	driver_dkong3,	/* (c) 1983 Nintendo of America */
                /*TODO*///	driver_dkong3j,	/* (c) 1983 Nintendo */
                /*TODO*///	driver_mario,		/* (c) 1983 Nintendo of America */
                /*TODO*///	driver_mariojp,	/* (c) 1983 Nintendo */
                /*TODO*///	driver_masao,		/* bootleg */
                /*TODO*///	driver_hunchbkd,	/* (c) 1983 Century */
                /*TODO*///	driver_herbiedk,	/* (c) 1984 CVS */
                /*TODO*////*TESTDRIVER*/driver_herocast,
                /*TODO*///	driver_popeye,
                /*TODO*///	driver_popeye2,
                /*TODO*///	driver_popeyebl,	/* bootleg */
                /*TODO*///	driver_punchout,	/* (c) 1984 */
                /*TODO*///	driver_spnchout,	/* (c) 1984 */
                /*TODO*///	driver_spnchotj,	/* (c) 1984 (Japan) */
                /*TODO*///	driver_armwrest,	/* (c) 1985 */
                /*TODO*///
                /*TODO*///	/* Midway 8080 b/w games */
                /*TODO*///	driver_seawolf,	/* 596 [1976] */
                /*TODO*///	driver_gunfight,	/* 597 [1975] */
                /*TODO*///	/* 603 - Top Gun [1976] */
                /*TODO*///	driver_tornbase,	/* 605 [1976] */
                /*TODO*///	driver_280zzzap,	/* 610 [1976] */
                /*TODO*///	driver_maze,		/* 611 [1976] */
                /*TODO*///	driver_boothill,	/* 612 [1977] */
                /*TODO*///	driver_checkmat,	/* 615 [1977] */
                /*TODO*///	driver_desertgu,	/* 618 [1977] */
                /*TODO*///	driver_dplay,		/* 619 [1977] */
                /*TODO*///	driver_lagunar,	/* 622 [1977] */
                /*TODO*///	driver_gmissile,	/* 623 [1977] */
                /*TODO*///	driver_m4,		/* 626 [1977] */
                /*TODO*///	driver_clowns,	/* 630 [1978] */
                /*TODO*///	/* 640 - Space Walk [1978] */
                /*TODO*///	driver_einnings,	/* 642 [1978] Midway */
                /*TODO*///	driver_shuffle,	/* 643 [1978] */
                /*TODO*///	driver_dogpatch,	/* 644 [1977] */
                /*TODO*///	driver_spcenctr,	/* 645 (c) 1980 Midway */
                /*TODO*///	driver_phantom2,	/* 652 [1979] */
                /*TODO*///	driver_bowler,	/* 730 [1978] Midway */
                /*TODO*///	driver_invaders,	/* 739 [1979] */
                /*TODO*///	driver_blueshrk,	/* 742 [1978] */
                /*TODO*///	driver_invad2ct,	/* 851 (c) 1980 Midway */
                /*TODO*///	driver_invadpt2,	/* 852 [1980] Taito */
                /*TODO*///	driver_invdpt2m,	/* 852 [1980] Midway */
                /*TODO*///	/* 870 - Space Invaders Deluxe cocktail */
                /*TODO*///	driver_earthinv,
                /*TODO*///	driver_spaceatt,
                /*TODO*///	driver_sinvemag,
                /*TODO*///	driver_jspecter,
                /*TODO*///	driver_invrvnge,
                /*TODO*///	driver_invrvnga,
                /*TODO*///	driver_galxwars,
                /*TODO*///	driver_starw,
                /*TODO*///	driver_lrescue,	/* (c) 1979 Taito */
                /*TODO*///	driver_grescue,	/* bootleg? */
                /*TODO*///	driver_desterth,	/* bootleg */
                /*TODO*///	driver_cosmicmo,	/* Universal */
                /*TODO*///	driver_rollingc,	/* Nichibutsu */
                /*TODO*///	driver_bandido,	/* (c) Exidy */
                /*TODO*///	driver_ozmawars,	/* Shin Nihon Kikaku (SNK) */
                /*TODO*///	driver_solfight,	/* bootleg */
                /*TODO*///	driver_spaceph,	/* Zilec Games */
                /*TODO*///	driver_schaser,	/* Taito */
                /*TODO*///	driver_lupin3,	/* (c) 1980 Taito */
                /*TODO*///	driver_helifire,	/* (c) Nintendo */
                /*TODO*///	driver_helifira,	/* (c) Nintendo */
                /*TODO*///	driver_spacefev,
                /*TODO*///	driver_sfeverbw,
                /*TODO*///	driver_spclaser,
                /*TODO*///	driver_laser,
                /*TODO*///	driver_spcewarl,
                /*TODO*///	driver_polaris,	/* (c) 1980 Taito */
                /*TODO*///	driver_polarisa,	/* (c) 1980 Taito */
                /*TODO*///	driver_ballbomb,	/* (c) 1980 Taito */
                /*TODO*///	driver_m79amb,
                /*TODO*///	driver_alieninv,
                /*TODO*///	driver_sitv,
                /*TODO*///	driver_sicv,
                /*TODO*///	driver_sisv,
                /*TODO*///	driver_sisv2,
                /*TODO*///	driver_spacewr3,
                /*TODO*///	driver_invaderl,
                /*TODO*///	driver_yosakdon,
                /*TODO*///	driver_spceking,
                /*TODO*///	driver_spcewars,
                /*TODO*///
                /*TODO*///	/* "Midway" Z80 b/w games */
                /*TODO*///	driver_astinvad,	/* (c) 1980 Stern */
                /*TODO*///	driver_kamikaze,	/* Leijac Corporation */
                /*TODO*///	driver_spaceint,	/* [1980] Shoei */
                /*TODO*///
                /*TODO*///	/* Meadows S2650 games */
                /*TODO*///	driver_lazercmd,	/* [1976?] */
                /*TODO*///	driver_deadeye,	/* [1978?] */
                /*TODO*///	driver_gypsyjug,	/* [1978?] */
                /*TODO*///	driver_medlanes,	/* [1977?] */
                /*TODO*///
                /*TODO*///	/* Midway "Astrocade" games */
                /*TODO*///	driver_wow,		/* (c) 1980 */
                /*TODO*///	driver_robby,		/* (c) 1981 */
                /*TODO*///	driver_gorf,		/* (c) 1981 */
                /*TODO*///	driver_gorfpgm1,	/* (c) 1981 */
                /*TODO*///	driver_seawolf2,
                /*TODO*///	driver_spacezap,	/* (c) 1980 */
                /*TODO*///	driver_ebases,
                /*TODO*///
                /*TODO*///	/* Bally Midway MCR games */
                /*TODO*///	/* MCR1 */
                /*TODO*///	driver_solarfox,	/* (c) 1981 */
                /*TODO*///	driver_kick,		/* (c) 1981 */
                /*TODO*///	driver_kicka,		/* bootleg? */
                /*TODO*///	/* MCR2 */
                /*TODO*///	driver_shollow,	/* (c) 1981 */
                /*TODO*///	driver_shollow2,	/* (c) 1981 */
                /*TODO*///	driver_tron,		/* (c) 1982 */
                /*TODO*///	driver_tron2,		/* (c) 1982 */
                /*TODO*///	driver_kroozr,	/* (c) 1982 */
                /*TODO*///	driver_domino,	/* (c) 1982 */
                /*TODO*///	driver_wacko,		/* (c) 1982 */
                /*TODO*///	driver_twotiger,	/* (c) 1984 */
                /*TODO*///	/* MCR2 + MCR3 sprites */
                /*TODO*///	driver_journey,	/* (c) 1983 */
                /*TODO*///	/* MCR3 */
                /*TODO*///	driver_tapper,	/* (c) 1983 */
                /*TODO*///	driver_tappera,	/* (c) 1983 */
                /*TODO*///	driver_sutapper,	/* (c) 1983 */
                /*TODO*///	driver_rbtapper,	/* (c) 1984 */
                /*TODO*///	driver_timber,	/* (c) 1984 */
                /*TODO*///	driver_dotron,	/* (c) 1983 */
                /*TODO*///	driver_dotrone,	/* (c) 1983 */
                /*TODO*///	driver_destderb,	/* (c) 1984 */
                /*TODO*///	driver_destderm,	/* (c) 1984 */
                /*TODO*///	driver_sarge,		/* (c) 1985 */
                /*TODO*///	driver_rampage,	/* (c) 1986 */
                /*TODO*///	driver_rampage2,	/* (c) 1986 */
                /*TODO*///	driver_powerdrv,	/* (c) 1986 */
                /*TODO*///	driver_maxrpm,	/* (c) 1986 */
                /*TODO*///	driver_spyhunt,	/* (c) 1983 */
                /*TODO*///	driver_turbotag,	/* (c) 1985 */
                /*TODO*///	driver_crater,	/* (c) 1984 */
                /*TODO*///	/* MCR 68000 */
                /*TODO*///	driver_zwackery,	/* (c) 1984 */
                /*TODO*///	driver_xenophob,	/* (c) 1987 */
                /*TODO*///	driver_spyhunt2,	/* (c) 1987 */
                /*TODO*///	driver_blasted,	/* (c) 1988 */
                /*TODO*///	driver_archrivl,	/* (c) 1989 */
                /*TODO*///	driver_archriv2,	/* (c) 1989 */
                /*TODO*///	driver_trisport,	/* (c) 1989 */
                /*TODO*///	driver_pigskin,	/* (c) 1990 */
                /*TODO*////* other possible MCR games:
                /*TODO*///Black Belt
                /*TODO*///Shoot the Bull
                /*TODO*///Special Force
                /*TODO*///MotorDome
                /*TODO*///Six Flags (?)
                /*TODO*///*/
                /*TODO*///
                /*TODO*///	/* Bally / Sente games */
                /*TODO*///	driver_sentetst,
                /*TODO*///	driver_cshift,	/* (c) 1984 */
                /*TODO*///	driver_gghost,	/* (c) 1984 */
                /*TODO*///	driver_hattrick,	/* (c) 1984 */
                /*TODO*///	driver_otwalls,	/* (c) 1984 */
                /*TODO*///	driver_snakepit,	/* (c) 1984 */
                /*TODO*///	driver_snakjack,	/* (c) 1984 */
                /*TODO*///	driver_stocker,	/* (c) 1984 */
                /*TODO*///	driver_triviag1,	/* (c) 1984 */
                /*TODO*///	driver_triviag2,	/* (c) 1984 */
                /*TODO*///	driver_triviasp,	/* (c) 1984 */
                /*TODO*///	driver_triviayp,	/* (c) 1984 */
                /*TODO*///	driver_triviabb,	/* (c) 1984 */
                /*TODO*///	driver_gimeabrk,	/* (c) 1985 */
                /*TODO*///	driver_minigolf,	/* (c) 1985 */
                /*TODO*///	driver_minigol2,	/* (c) 1985 */
                /*TODO*///	driver_toggle,	/* (c) 1985 */
                /*TODO*///	driver_nametune,	/* (c) 1986 */
                /*TODO*///	driver_nstocker,	/* (c) 1986 */
                /*TODO*///	driver_sfootbal,	/* (c) 1986 */
                /*TODO*///	driver_spiker,	/* (c) 1986 */
                /*TODO*///	driver_rescraid,	/* (c) 1987 */
                /*TODO*///
                /*TODO*///	/* Irem games */
                /*TODO*///	/* trivia: IREM means "International Rental Electronics Machines" */
                /*TODO*///	driver_skychut,	/* (c) [1980] */
                /*TODO*///	driver_mpatrol,	/* (c) 1982 */
                /*TODO*///	driver_mpatrolw,	/* (c) 1982 + Williams license */
                /*TODO*///	driver_mranger,	/* bootleg */
                /*TODO*///	driver_troangel,	/* (c) 1983 */
                /*TODO*///	driver_yard,		/* (c) 1983 */
                /*TODO*///	driver_vsyard,	/* (c) 1983/1984 */
                /*TODO*///	driver_vsyard2,	/* (c) 1983/1984 */
                /*TODO*///	driver_travrusa,	/* (c) 1983 */
                /*TODO*///	driver_motorace,	/* (c) 1983 Williams license */
                /*TODO*///	/* M62 */
                /*TODO*///	driver_kungfum,	/* (c) 1984 */
                /*TODO*///	driver_kungfud,	/* (c) 1984 + Data East license */
                /*TODO*///	driver_spartanx,	/* (c) 1984 */
                /*TODO*///	driver_kungfub,	/* bootleg */
                /*TODO*///	driver_kungfub2,	/* bootleg */
                /*TODO*///	driver_battroad,	/* (c) 1984 */
                /*TODO*///	driver_ldrun,		/* (c) 1984 licensed from Broderbund */
                /*TODO*///	driver_ldruna,	/* (c) 1984 licensed from Broderbund */
                /*TODO*///	driver_ldrun2,	/* (c) 1984 licensed from Broderbund */
                /*TODO*///	driver_ldrun3,	/* (c) 1985 licensed from Broderbund */
                /*TODO*///	driver_ldrun4,	/* (c) 1986 licensed from Broderbund */
                /*TODO*///	driver_lotlot,	/* (c) 1985 licensed from Tokuma Shoten */
                /*TODO*///	driver_kidniki,	/* (c) 1986 + Data East USA license */
                /*TODO*///	driver_yanchamr,	/* (c) 1986 (Japan) */
                /*TODO*///	driver_spelunkr,	/* (c) 1985 licensed from Broderbund */
                /*TODO*///	driver_spelunk2,	/* (c) 1986 licensed from Broderbund */
                /*TODO*///
                /*TODO*///	driver_vigilant,	/* (c) 1988 (World) */
                /*TODO*///	driver_vigilntu,	/* (c) 1988 (US) */
                /*TODO*///	driver_vigilntj,	/* (c) 1988 (Japan) */
                /*TODO*///	driver_kikcubic,	/* (c) 1988 (Japan) */
                /*TODO*///	/* M72 (and derivatives) */
                /*TODO*///	driver_rtype,		/* (c) 1987 (Japan) */
                /*TODO*///	driver_rtypeu,	/* (c) 1987 + Nintendo USA license (US) */
                /*TODO*///	driver_rtypeb,	/* bootleg */
                /*TODO*///	driver_bchopper,	/* (c) 1987 */
                /*TODO*///	driver_mrheli,	/* (c) 1987 (Japan) */
                /*TODO*///	driver_nspirit,	/* (c) 1988 */
                /*TODO*///	driver_nspiritj,	/* (c) 1988 (Japan) */
                /*TODO*///	driver_imgfight,	/* (c) 1988 (Japan) */
                /*TODO*///	driver_loht,		/* (c) 1989 */
                /*TODO*///	driver_xmultipl,	/* (c) 1989 (Japan) */
                /*TODO*///	driver_dbreed,	/* (c) 1989 */
                /*TODO*///	driver_rtype2,	/* (c) 1989 */
                /*TODO*///	driver_rtype2j,	/* (c) 1989 (Japan) */
                /*TODO*///	driver_majtitle,	/* (c) 1990 (Japan) */
                /*TODO*///	driver_hharry,	/* (c) 1990 (World) */
                /*TODO*///	driver_hharryu,	/* (c) 1990 Irem America (US) */
                /*TODO*///	driver_dkgensan,	/* (c) 1990 (Japan) */
                /*TODO*////*TESTDRIVER*/driver_kengo,
                /*TODO*///	driver_gallop,	/* (c) 1991 (Japan) */
                /*TODO*////*TESTDRIVER*/driver_poundfor,
                /*TODO*///	/* not M72, but same sound hardware */
                /*TODO*///	driver_sichuan2,	/* (c) 1989 Tamtex */
                /*TODO*///	driver_sichuana,	/* (c) 1989 Tamtex */
                /*TODO*///	driver_shisen,	/* (c) 1989 Tamtex */
                /*TODO*///	/* M92 */
                /*TODO*///	driver_bmaster,	/* (c) 1991 Irem */
                /*TODO*///	driver_gunforce,	/* (c) 1991 Irem (World) */
                /*TODO*///	driver_gunforcu,	/* (c) 1991 Irem America (US) */
                /*TODO*///	driver_hook,		/* (c) 1992 Irem (World) */
                /*TODO*///	driver_hooku,		/* (c) 1992 Irem America (US) */
                /*TODO*///	driver_mysticri,	/* (c) 1992 Irem (World) */
                /*TODO*///	driver_gunhohki,	/* (c) 1992 Irem (Japan) */
                /*TODO*///	driver_uccops,	/* (c) 1992 Irem (World) */
                /*TODO*///	driver_uccopsj,	/* (c) 1992 Irem (Japan) */
                /*TODO*///	driver_rtypeleo,	/* (c) 1992 Irem (Japan) */
                /*TODO*///	driver_majtitl2,	/* (c) 1992 Irem (World) */
                /*TODO*///	driver_skingame,	/* (c) 1992 Irem America (US) */
                /*TODO*///	driver_skingam2,	/* (c) 1992 Irem America (US) */
                /*TODO*///	driver_inthunt,	/* (c) 1993 Irem (World) */
                /*TODO*///	driver_kaiteids,	/* (c) 1993 Irem (Japan) */
                /*TODO*////*TESTDRIVER*/driver_nbbatman,	/* (c) 1993 Irem America (US) */
                /*TODO*////*TESTDRIVER*/driver_leaguemn,	/* (c) 1993 Irem (Japan) */
                /*TODO*///	driver_lethalth,	/* (c) 1991 Irem (World) */
                /*TODO*///	driver_thndblst,	/* (c) 1991 Irem (Japan) */
                /*TODO*///	driver_psoldier,	/* (c) 1993 Irem (Japan) */
                /*TODO*///	/* M97 */
                /*TODO*////*TESTDRIVER*/driver_riskchal,
                /*TODO*////*TESTDRIVER*/driver_gussun,
                /*TODO*////*TESTDRIVER*/driver_shisen2,
                /*TODO*////*TESTDRIVER*/driver_quizf1,
                /*TODO*////*TESTDRIVER*/driver_atompunk,
                /*TODO*////*TESTDRIVER*/driver_bbmanw,
                /*TODO*///	/* M107 */
                /*TODO*////*TESTDRIVER*/driver_firebarr,	/* (c) 1993 Irem (Japan) */
                /*TODO*///	driver_dsoccr94,	/* (c) 1994 Irem (Data East Corporation license) */
                /*TODO*///
                /*TODO*///	/* Gottlieb/Mylstar games (Gottlieb became Mylstar in 1983) */
                /*TODO*///	driver_reactor,	/* GV-100 (c) 1982 Gottlieb */
                /*TODO*///	driver_mplanets,	/* GV-102 (c) 1983 Gottlieb */
                /*TODO*///	driver_qbert,		/* GV-103 (c) 1982 Gottlieb */
                /*TODO*///	driver_qbertjp,	/* GV-103 (c) 1982 Gottlieb + Konami license */
                /*TODO*///	driver_sqbert,	/* (c) 1983 Mylstar - never released */
                /*TODO*///	driver_krull,		/* GV-105 (c) 1983 Gottlieb */
                /*TODO*///	driver_mach3,		/* GV-109 (c) 1983 Mylstar */
                /*TODO*///	driver_usvsthem,	/* GV-??? (c) 198? Mylstar */
                /*TODO*///	driver_3stooges,	/* GV-113 (c) 1984 Mylstar */
                /*TODO*///	driver_qbertqub,	/* GV-119 (c) 1983 Mylstar */
                /*TODO*///	driver_curvebal,	/* GV-134 (c) 1984 Mylstar */
                /*TODO*///
                /*TODO*///	/* older Taito games */
                /*TODO*///	driver_crbaloon,	/* (c) 1980 Taito Corporation */
                /*TODO*///	driver_crbalon2,	/* (c) 1980 Taito Corporation */
                /*TODO*///
                /*TODO*///	/* Taito "Qix hardware" games */
                /*TODO*///	driver_qix,		/* (c) 1981 Taito America Corporation */
                /*TODO*///	driver_qixa,		/* (c) 1981 Taito America Corporation */
                /*TODO*///	driver_qixb,		/* (c) 1981 Taito America Corporation */
                /*TODO*///	driver_qix2,		/* (c) 1981 Taito America Corporation */
                /*TODO*///	driver_sdungeon,	/* (c) 1981 Taito America Corporation */
                /*TODO*///	driver_elecyoyo,	/* (c) 1982 Taito America Corporation */
                /*TODO*///	driver_elecyoy2,	/* (c) 1982 Taito America Corporation */
                /*TODO*///	driver_kram,		/* (c) 1982 Taito America Corporation */
                /*TODO*///	driver_kram2,		/* (c) 1982 Taito America Corporation */
                /*TODO*///	driver_zookeep,	/* (c) 1982 Taito America Corporation */
                /*TODO*///	driver_zookeep2,	/* (c) 1982 Taito America Corporation */
                /*TODO*///	driver_zookeep3,	/* (c) 1982 Taito America Corporation */
                /*TODO*///
                /*TODO*///	/* Taito SJ System games */
                /*TODO*///	driver_spaceskr,	/* (c) 1981 Taito Corporation */
                /*TODO*///	driver_junglek,	/* (c) 1982 Taito Corporation */
                /*TODO*///	driver_junglkj2,	/* (c) 1982 Taito Corporation */
                /*TODO*///	driver_jungleh,	/* (c) 1982 Taito America Corporation */
                /*TODO*///	driver_alpine,	/* (c) 1982 Taito Corporation */
                /*TODO*///	driver_alpinea,	/* (c) 1982 Taito Corporation */
                /*TODO*///	driver_timetunl,	/* (c) 1982 Taito Corporation */
                /*TODO*///	driver_wwestern,	/* (c) 1982 Taito Corporation */
                /*TODO*///	driver_wwester1,	/* (c) 1982 Taito Corporation */
                /*TODO*///	driver_frontlin,	/* (c) 1982 Taito Corporation */
                /*TODO*///	driver_elevator,	/* (c) 1983 Taito Corporation */
                /*TODO*///	driver_elevatob,	/* bootleg */
                /*TODO*///	driver_tinstar,	/* (c) 1983 Taito Corporation */
                /*TODO*///	driver_waterski,	/* (c) 1983 Taito Corporation */
                /*TODO*///	driver_bioatack,	/* (c) 1983 Taito Corporation + Fox Video Games license */
                /*TODO*///	driver_hwrace,	/* (c) 1983 Taito Corporation */
                /*TODO*///	driver_sfposeid,	/* 1984 */
                /*TODO*///	driver_kikstart,
                /*TODO*///
                /*TODO*///	/* other Taito games */
                /*TODO*///	driver_bking2,	/* (c) 1983 Taito Corporation */
                /*TODO*///	driver_gsword,	/* (c) 1984 Taito Corporation */
                /*TODO*///	driver_lkage,		/* (c) 1984 Taito Corporation */
                /*TODO*///	driver_lkageb,	/* bootleg */
                driver_retofinv, /* (c) 1985 Taito Corporation */
                driver_retofin1, /* bootleg */
                driver_retofin2, /* bootleg */
                /*TODO*///	driver_tsamurai,	/* (c) 1985 Taito */
                /*TODO*///	driver_tsamura2,	/* (c) 1985 Taito */
                /*TODO*///	driver_nunchaku,	/* (c) 1985 Taito */
                /*TODO*///	driver_yamagchi,	/* (c) 1985 Taito */
                /*TODO*////*TESTDRIVER*/driver_flstory,	/* (c) 1985 Taito Corporation */
                /*TODO*////*TESTDRIVER*/driver_flstoryj,	/* (c) 1985 Taito Corporation (Japan) */
                /*TODO*///	driver_gladiatr,	/* (c) 1986 Taito America Corporation (US) */
                /*TODO*///	driver_ogonsiro,	/* (c) 1986 Taito Corporation (Japan) */
                /*TODO*///	driver_bublbobl,	/* (c) 1986 Taito Corporation */
                /*TODO*///	driver_bublbobr,	/* (c) 1986 Taito America Corporation + Romstar license */
                /*TODO*///	driver_bubbobr1,	/* (c) 1986 Taito America Corporation + Romstar license */
                /*TODO*///	driver_boblbobl,	/* bootleg */
                /*TODO*///	driver_sboblbob,	/* bootleg */
                /*TODO*///	driver_tokio,		/* 1986 */
                /*TODO*///	driver_tokiob,	/* bootleg */
                /*TODO*///	driver_kicknrun,	/* (c) 1986 Taito Corporation */
                /*TODO*///	driver_mexico86,	/* bootleg (Micro Research) */
                /*TODO*///	driver_kikikai,	/* (c) 1986 Taito Corporation */
                /*TODO*///	driver_rastan,	/* (c) 1987 Taito Corporation Japan (World) */
                /*TODO*///	driver_rastanu,	/* (c) 1987 Taito America Corporation (US) */
                /*TODO*///	driver_rastanu2,	/* (c) 1987 Taito America Corporation (US) */
                /*TODO*///	driver_rastsaga,	/* (c) 1987 Taito Corporation (Japan)*/
                /*TODO*///	driver_rainbow,	/* (c) 1987 Taito Corporation */
                /*TODO*///	driver_rainbowe,	/* (c) 1988 Taito Corporation */
                /*TODO*///	driver_jumping,	/* bootleg */
                /*TODO*///	driver_arkanoid,	/* (c) 1986 Taito Corporation Japan (World) */
                /*TODO*///	driver_arknoidu,	/* (c) 1986 Taito America Corporation + Romstar license (US) */
                /*TODO*///	driver_arknoidj,	/* (c) 1986 Taito Corporation (Japan) */
                /*TODO*///	driver_arkbl2,	/* bootleg */
                /*TODO*////*TESTDRIVER*/driver_arkbl3,	/* bootleg */
                /*TODO*///	driver_arkatayt,	/* bootleg */
                /*TODO*////*TESTDRIVER*/driver_arkblock,	/* bootleg */
                /*TODO*///	driver_arkbloc2,	/* bootleg */
                /*TODO*///	driver_arkangc,	/* bootleg */
                driver_superqix, /* 1987 */
                driver_sqixbl, /* bootleg? but (c) 1987 */
                /*TODO*///	driver_superman,	/* (c) 1988 Taito Corporation */
                /*TODO*////*TESTDRIVER*/driver_footchmp,	/* (c) 1990 Taito Corporation Japan (World) */
                driver_minivadr, /* cabinet test board */
                /*TODO*///
                /*TODO*///	/* Taito "tnzs" hardware */
                /*TODO*///	driver_extrmatn,	/* (c) 1987 World Games */
                /*TODO*///	driver_arkanoi2,	/* (c) 1987 Taito Corporation Japan (World) */
                /*TODO*///	driver_ark2us,	/* (c) 1987 Taito America Corporation + Romstar license (US) */
                /*TODO*///	driver_ark2jp,	/* (c) 1987 Taito Corporation (Japan) */
                /*TODO*///	driver_plumppop,	/* (c) 1987 Taito Corporation (Japan) */
                /*TODO*///	driver_drtoppel,	/* (c) 1987 Taito Corporation (Japan) */
                /*TODO*///	driver_chukatai,	/* (c) 1988 Taito Corporation (Japan) */
                /*TODO*///	driver_tnzs,		/* (c) 1988 Taito Corporation (Japan) (new logo) */
                /*TODO*///	driver_tnzsb,		/* bootleg but Taito Corporation Japan (World) (new logo) */
                /*TODO*///	driver_tnzs2,		/* (c) 1988 Taito Corporation Japan (World) (old logo) */
                /*TODO*///	driver_insectx,	/* (c) 1989 Taito Corporation Japan (World) */
                /*TODO*///	driver_kageki,	/* (c) 1988 Taito America Corporation + Romstar license (US) */
                /*TODO*///	driver_kagekij,	/* (c) 1988 Taito Corporation (Japan) */
                /*TODO*///
                /*TODO*///	/* Taito L-System games */
                /*TODO*///	driver_fhawk,		/* (c) 1988 Taito Corporation (Japan) */
                /*TODO*///	driver_raimais,	/* (c) 1988 Taito Corporation (Japan) */
                /*TODO*///	driver_champwr,	/* (c) 1989 Taito Corporation Japan (World) */
                /*TODO*///	driver_champwru,	/* (c) 1989 Taito America Corporation (US) */
                /*TODO*///	driver_champwrj,	/* (c) 1989 Taito Corporation (Japan) */
                /*TODO*///	driver_puzznic,	/* (c) 1989 Taito Corporation (Japan) */
                /*TODO*///	driver_plotting,	/* (c) 1989 Taito Corporation Japan (World) */
                /*TODO*///	driver_palamed,	/* (c) 1990 Taito Corporation (Japan) */
                /*TODO*///	driver_horshoes,	/* (c) 1990 Taito America Corporation (US) */
                /*TODO*///	driver_cachat,	/* (c) 1993 Taito Corporation (Japan) */
                /*TODO*///
                /*TODO*///	/* Taito F2 games */
                /*TODO*///	driver_ssi,		/* (c) 1990 Taito Corporation Japan (World) */
                /*TODO*///	/* Majestic 12 (c) 1990 Taito America Corporation (US) */
                /*TODO*///	driver_majest12,	/* (c) 1990 Taito Corporation (Japan) */
                /*TODO*////*TESTDRIVER*/driver_finalb,
                /*TODO*////*TESTDRIVER*/driver_megab,
                /*TODO*///	driver_liquidk,	/* (c) 1990 Taito Corporation Japan (World) */
                /*TODO*///	driver_liquidku,	/* (c) 1990 Taito America Corporation (US) */
                /*TODO*///	driver_mizubaku,	/* (c) 1990 Taito Corporation (Japan) */
                /*TODO*///	driver_growl,		/* (c) 1990 Taito Corporation Japan (World) */
                /*TODO*///	driver_growlu,	/* (c) 1990 Taito America Corporation (US) */
                /*TODO*///	driver_runark,	/* (c) 1990 Taito Corporation (Japan) */
                /*TODO*///
                /*TODO*///	/* Toaplan games */
                /*TODO*///	driver_tigerh,	/* GX-551 [not a Konami board!] */
                /*TODO*///	driver_tigerh2,	/* GX-551 [not a Konami board!] */
                /*TODO*///	driver_tigerhj,	/* GX-551 [not a Konami board!] */
                /*TODO*///	driver_tigerhb1,	/* bootleg but (c) 1985 Taito Corporation */
                /*TODO*///	driver_tigerhb2,	/* bootleg but (c) 1985 Taito Corporation */
                /*TODO*///	driver_slapfigh,	/* TP-??? */
                /*TODO*///	driver_slapbtjp,	/* bootleg but (c) 1986 Taito Corporation */
                /*TODO*///	driver_slapbtuk,	/* bootleg but (c) 1986 Taito Corporation */
                /*TODO*///	driver_alcon,		/* TP-??? */
                /*TODO*///	driver_getstar,
                /*TODO*///	driver_getstarj,
                /*TODO*///	driver_getstarb,	/* GX-006 bootleg but (c) 1986 Taito Corporation */
                /*TODO*///
                /*TODO*///	driver_fshark,	/* TP-007 (c) 1987 Taito Corporation (World) */
                /*TODO*///	driver_skyshark,	/* TP-007 (c) 1987 Taito America Corporation + Romstar license (US) */
                /*TODO*///	driver_hishouza,	/* TP-007 (c) 1987 Taito Corporation (Japan) */
                /*TODO*///	driver_fsharkbt,	/* bootleg */
                /*TODO*///	driver_wardner,	/* TP-009 (c) 1987 Taito Corporation Japan (World) */
                /*TODO*///	driver_pyros,		/* TP-009 (c) 1987 Taito America Corporation (US) */
                /*TODO*///	driver_wardnerj,	/* TP-009 (c) 1987 Taito Corporation (Japan) */
                /*TODO*///	driver_twincobr,	/* TP-011 (c) 1987 Taito Corporation (World) */
                /*TODO*///	driver_twincobu,	/* TP-011 (c) 1987 Taito America Corporation + Romstar license (US) */
                /*TODO*///	driver_ktiger,	/* TP-011 (c) 1987 Taito Corporation (Japan) */
                /*TODO*///
                /*TODO*///	driver_rallybik,	/* TP-012 (c) 1988 Taito */
                /*TODO*///	driver_truxton,	/* TP-013B (c) 1988 Taito */
                /*TODO*///	driver_hellfire,	/* TP-??? (c) 1989 Toaplan + Taito license */
                /*TODO*///	driver_zerowing,	/* TP-015 (c) 1989 Toaplan */
                /*TODO*///	driver_demonwld,	/* TP-016 (c) 1989 Toaplan + Taito license */
                /*TODO*///	driver_fireshrk,	/* TP-017 (c) 1990 Toaplan */
                /*TODO*///	driver_samesame,	/* TP-017 (c) 1989 Toaplan */
                /*TODO*///	driver_outzone,	/* TP-018 (c) 1990 Toaplan */
                /*TODO*///	driver_outzonep,	/* bootleg */
                /*TODO*///	driver_vimana,	/* TP-019 (c) 1991 Toaplan (+ Tecmo license when set to Japan) */
                /*TODO*///	driver_vimana2,	/* TP-019 (c) 1991 Toaplan (+ Tecmo license when set to Japan)  */
                /*TODO*///	driver_vimanan,	/* TP-019 (c) 1991 Toaplan (+ Nova Apparate GMBH & Co license) */
                /*TODO*///	driver_snowbros,	/* MIN16-02 (c) 1990 Toaplan + Romstar license */
                /*TODO*///	driver_snowbroa,	/* MIN16-02 (c) 1990 Toaplan + Romstar license */
                /*TODO*///	driver_snowbrob,	/* MIN16-02 (c) 1990 Toaplan + Romstar license */
                /*TODO*///	driver_snowbroj,	/* MIN16-02 (c) 1990 Toaplan */
                /*TODO*///
                /*TODO*///	driver_tekipaki,	/* TP-020 (c) 1991 Toaplan */
                /*TODO*///	driver_ghox,		/* TP-021 (c) 1991 Toaplan */
                /*TODO*///	driver_dogyuun,	/* TP-022 (c) 1992 Toaplan */
                /*TODO*///	driver_kbash,		/* TP-023 (c) 1993 Toaplan */
                /*TODO*////*TESTDRIVER*/driver_tatsujn2,	/* TP-024 */
                /*TODO*///	driver_pipibibs,	/* TP-025 */
                /*TODO*////*TESTDRIVER*/driver_pipibibi,	/* bootleg */
                /*TODO*///	driver_whoopee,	/* TP-025 */
                /*TODO*////*TESTDRIVER*/driver_fixeight,	/* TP-026 (c) 1992 + Taito license */
                /*TODO*///	driver_vfive,		/* TP-027 (c) 1993 Toaplan */
                /*TODO*///	driver_batsugun,	/* TP-030 (c) 1993 Toaplan */
                /*TODO*///	driver_snowbro2,	/* TP-??? (c) 1994 Hanafram */
                /*TODO*///
                /*TODO*////*
                /*TODO*///Toa Plan's board list
                /*TODO*///(translated from http://www.aianet.ne.jp/~eisetu/rom/rom_toha.html)
                /*TODO*///
                /*TODO*///Title              ROMno.   Remark(1)   Remark(2)
                /*TODO*///--------------------------------------------------
                /*TODO*///Tiger Heli           A47      GX-551
                /*TODO*///Hishouzame           B02      TP-007
                /*TODO*///Kyukyoku Tiger       B30      TP-011
                /*TODO*///Dash Yarou           B45      TP-012
                /*TODO*///Tatsujin             B65      TP-013B   M6100649A
                /*TODO*///Zero Wing            O15      TP-015
                /*TODO*///Horror Story         O16      TP-016
                /*TODO*///Same!Same!Same!      O17      TP-017
                /*TODO*///Out Zone                      TP-018
                /*TODO*///Vimana                        TP-019
                /*TODO*///Teki Paki            O20      TP-020
                /*TODO*///Ghox               TP-21      TP-021
                /*TODO*///Dogyuun                       TP-022
                /*TODO*///Tatsujin Oh                   TP-024    *1
                /*TODO*///Fixeight                      TP-026
                /*TODO*///V-V                           TP-027
                /*TODO*///
                /*TODO*///*1 There is a doubt this game uses TP-024 board and TP-025 romsets.
                /*TODO*///
                /*TODO*///   86 Mahjong Sisters                                 Kit 2P 8W+2B     HC    Mahjong TP-
                /*TODO*///   88 Dash                                            Kit 2P 8W+2B                   TP-
                /*TODO*///   89 Fire Shark                                      Kit 2P 8W+2B     VC    Shooter TP-017
                /*TODO*///   89 Twin Hawk                                       Kit 2P 8W+2B     VC    Shooter TP-
                /*TODO*///   91 Whoopie                                         Kit 2P 8W+2B     HC    Action
                /*TODO*///   92 Teki Paki                                       Kit 2P                         TP-020
                /*TODO*///   92 Ghox                                            Kit 2P Paddle+1B VC    Action  TP-021
                /*TODO*///10/92 Dogyuun                                         Kit 2P 8W+2B     VC    Shooter TP-022
                /*TODO*///92/93 Knuckle Bash                 Atari Games        Kit 2P 8W+2B     HC    Action  TP-023
                /*TODO*///10/92 Tatsujin II/Truxton II       Taito              Kit 2P 8W+2B     VC    Shooter TP-024
                /*TODO*///10/92 Truxton II/Tatsujin II       Taito              Kit 2P 8W+2B     VC    Shooter TP-024
                /*TODO*///      Pipi & Bipi                                                                    TP-025
                /*TODO*///   92 Fix Eight                                       Kit 2P 8W+2B     VC    Action  TP-026
                /*TODO*///12/92 V  -  V (5)/Grind Stormer                       Kit 2P 8W+2B     VC    Shooter TP-027
                /*TODO*/// 1/93 Grind Stormer/V - V (Five)                      Kit 2P 8W+2B     VC    Shooter TP-027
                /*TODO*/// 2/94 Batsugun                                        Kit 2P 8W+2B     VC            TP-
                /*TODO*/// 4/94 Snow Bros. 2                                    Kit 2P 8W+2B     HC    Action  TP-
                /*TODO*///*/
                /*TODO*///
                /* Kyugo games */
                /* Kyugo only made four games: Repulse, Flash Gal, SRD Mission and Air Wolf. */
                /* Gyrodine was made by Crux. Crux was antecedent of Toa Plan, and spin-off from Orca. */
                driver_gyrodine, /* (c) 1984 Taito Corporation */
                driver_sonofphx, /* (c) 1985 Associated Overseas MFR */
                driver_repulse, /* (c) 1985 Sega */
                driver_99lstwar, /* (c) 1985 Proma */
                driver_99lstwra, /* (c) 1985 Proma */
                driver_flashgal, /* (c) 1985 Sega */
                driver_srdmissn, /* (c) 1986 Taito Corporation */
                driver_airwolf, /* (c) 1987 Kyugo */
                driver_skywolf, /* bootleg */
                driver_skywolf2, /* bootleg */
                /*TODO*///	/* Williams games */
                /*TODO*///	driver_defender,	/* (c) 1980 */
                /*TODO*///	driver_defendg,	/* (c) 1980 */
                /*TODO*///	driver_defendw,	/* (c) 1980 */
                /*TODO*////*TESTDRIVER*/driver_defndjeu,	/* bootleg */
                /*TODO*///	driver_defcmnd,	/* bootleg */
                /*TODO*////*TESTDRIVER*/driver_defcomnd,	/* bootleg */
                /*TODO*///	driver_defence,	/* bootleg */
                /*TODO*///	driver_mayday,
                /*TODO*///	driver_maydaya,
                /*TODO*///	driver_colony7,	/* (c) 1981 Taito */
                /*TODO*///	driver_colony7a,	/* (c) 1981 Taito */
                /*TODO*///	driver_stargate,	/* (c) 1981 */
                /*TODO*///	driver_robotron,	/* (c) 1982 */
                /*TODO*///	driver_robotryo,	/* (c) 1982 */
                /*TODO*///	driver_joust,		/* (c) 1982 */
                /*TODO*///	driver_joustr,	/* (c) 1982 */
                /*TODO*///	driver_joustwr,	/* (c) 1982 */
                /*TODO*///	driver_bubbles,	/* (c) 1982 */
                /*TODO*///	driver_bubblesr,	/* (c) 1982 */
                /*TODO*///	driver_splat,		/* (c) 1982 */
                /*TODO*///	driver_sinistar,	/* (c) 1982 */
                /*TODO*///	driver_sinista1,	/* (c) 1982 */
                /*TODO*///	driver_sinista2,	/* (c) 1982 */
                /*TODO*///	driver_blaster,	/* (c) 1983 */
                /*TODO*///	driver_mysticm,	/* (c) 1983 */
                /*TODO*///	driver_tshoot,	/* (c) 1984 */
                /*TODO*///	driver_inferno,	/* (c) 1984 */
                /*TODO*///	driver_joust2,	/* (c) 1986 */
                /*TODO*///
                /* Capcom games */
                /* The following is a COMPLETE list of the Capcom games up to 1997, as shown on */
                /* their web site. The list is sorted by production date. */
                driver_vulgus, /*  5/1984 (c) 1984 */
                driver_vulgus2, /*  5/1984 (c) 1984 */
                driver_vulgusj, /*  5/1984 (c) 1984 */
                /*TODO*///	driver_sonson,	/*  7/1984 (c) 1984 */
                driver_higemaru, /*  9/1984 (c) 1984 */
                driver_1942, /* 12/1984 (c) 1984 */
                driver_1942a, /* 12/1984 (c) 1984 */
                driver_1942b, /* 12/1984 (c) 1984 */
                driver_exedexes, /*  2/1985 (c) 1985 */
                driver_savgbees, /*  2/1985 (c) 1985 + Memetron license */
                driver_commando, /*  5/1985 (c) 1985 (World) */
                driver_commandu, /*  5/1985 (c) 1985 + Data East license (US) */
                driver_commandj, /*  5/1985 (c) 1985 (Japan) */
                driver_spaceinv, /* bootleg */
                /*TODO*///	driver_gng,		/*  9/1985 (c) 1985 */
                /*TODO*///	driver_gnga,		/*  9/1985 (c) 1985 */
                /*TODO*///	driver_gngt,		/*  9/1985 (c) 1985 */
                /*TODO*///	driver_makaimur,	/*  9/1985 (c) 1985 */
                /*TODO*///	driver_makaimuc,	/*  9/1985 (c) 1985 */
                /*TODO*///	driver_makaimug,	/*  9/1985 (c) 1985 */
                /*TODO*///	driver_diamond,	/* (c) 1989 KH Video (NOT A CAPCOM GAME but runs on GnG hardware) */
                driver_gunsmoke, /* 11/1985 (c) 1985 (World) */
                driver_gunsmrom, /* 11/1985 (c) 1985 + Romstar (US) */
                driver_gunsmoka, /* 11/1985 (c) 1985 (US) */
                driver_gunsmokj, /* 11/1985 (c) 1985 (Japan) */
                driver_sectionz, /* 12/1985 (c) 1985 */
                driver_sctionza, /* 12/1985 (c) 1985 */
                driver_trojan, /*  4/1986 (c) 1986 (US) */
                driver_trojanr, /*  4/1986 (c) 1986 + Romstar */
                driver_trojanj, /*  4/1986 (c) 1986 (Japan) */
                /*TODO*///	driver_srumbler,	/*  9/1986 (c) 1986 */
                /*TODO*///	driver_srumblr2,	/*  9/1986 (c) 1986 */
                /*TODO*///	driver_rushcrsh,	/*  9/1986 (c) 1986 */
                driver_lwings, /* 11/1986 (c) 1986 */
                driver_lwings2, /* 11/1986 (c) 1986 */
                driver_lwingsjp, /* 11/1986 (c) 1986 */
                /*TODO*///	driver_sidearms,	/* 12/1986 (c) 1986 (World) */
                /*TODO*///	driver_sidearmr,	/* 12/1986 (c) 1986 + Romstar license (US) */
                /*TODO*///	driver_sidearjp,	/* 12/1986 (c) 1986 (Japan) */
                /*TODO*///	driver_turtship,	/* (c) 1988 Philco (NOT A CAPCOM GAME but runs on modified Sidearms hardware) */
                /*TODO*///	driver_dyger,		/* (c) 1989 Philco (NOT A CAPCOM GAME but runs on modified Sidearms hardware) */
                driver_avengers, /*  2/1987 (c) 1987 (US) */
                driver_avenger2, /*  2/1987 (c) 1987 (US) */
                /*TODO*///	driver_bionicc,	/*  3/1987 (c) 1987 (US) */
                /*TODO*///	driver_bionicc2,	/*  3/1987 (c) 1987 (US) */
                /*TODO*///	driver_topsecrt,	/*  3/1987 (c) 1987 (Japan) */
                driver_1943, /*  6/1987 (c) 1987 (US) */
                driver_1943j, /*  6/1987 (c) 1987 (Japan) */
                driver_blktiger, /*  8/1987 (c) 1987 (US) */
                driver_bktigerb, /* bootleg */
                driver_blkdrgon, /*  8/1987 (c) 1987 (Japan) */
                driver_blkdrgnb, /* bootleg, hacked to say Black Tiger */
                /*TODO*///	driver_sf1,		/*  8/1987 (c) 1987 (World) */
                /*TODO*///	driver_sf1us,		/*  8/1987 (c) 1987 (US) */
                /*TODO*///	driver_sf1jp,		/*  8/1987 (c) 1987 (Japan) */
                /*TODO*///	driver_tigeroad,	/* 11/1987 (c) 1987 + Romstar (US) */
                /*TODO*///	driver_toramich,	/* 11/1987 (c) 1987 (Japan) */
                /*TODO*///	driver_f1dream,	/*  4/1988 (c) 1988 + Romstar */
                /*TODO*///	driver_f1dreamb,	/* bootleg */
                driver_1943kai, /*  6/1988 (c) 1987 (Japan) */
                /*TODO*///	driver_lastduel,	/*  7/1988 (c) 1988 (US) */
                /*TODO*///	driver_lstduela,	/*  7/1988 (c) 1988 (US) */
                /*TODO*///	driver_lstduelb,	/* bootleg */
                /*TODO*///	driver_madgear,	/*  2/1989 (c) 1989 (US) */
                /*TODO*///	driver_madgearj,	/*  2/1989 (c) 1989 (Japan) */
                /*TODO*///	driver_ledstorm,	/*  2/1989 (c) 1989 (US) */
                /*TODO*///	/*  3/1989 Dokaben (baseball) - see below among "Mitchell" games */
                /*TODO*///	/*  8/1989 Dokaben 2 (baseball) - see below among "Mitchell" games */
                /*TODO*///	/* 10/1989 Capcom Baseball - see below among "Mitchell" games */
                /*TODO*///	/* 11/1989 Capcom World - see below among "Mitchell" games */
                /*TODO*///	/*  3/1990 Adventure Quiz 2 Hatena no Dai-Bouken - see below among "Mitchell" games */
                /*TODO*///	/*  1/1991 Quiz Tonosama no Yabou - see below among "Mitchell" games */
                /*TODO*///	/*  4/1991 Ashita Tenki ni Naare (golf) - see below among "Mitchell" games */
                /*TODO*///	/*  5/1991 Ataxx - see below among "Leland" games */
                /*TODO*///	/*  6/1991 Quiz Sangokushi - see below among "Mitchell" games */
                /*TODO*///	/* 10/1991 Block Block - see below among "Mitchell" games */
                /*TODO*///	/*  6/1995 Street Fighter - the Movie - see below among "Incredible Technologies" games */
                /*TODO*///
                /*TODO*///	/* Capcom CPS1 games */
                /*TODO*///	driver_forgottn,	/*  7/1988 (c) 1988 (US) */
                /*TODO*///	driver_lostwrld,	/*  7/1988 (c) 1988 (Japan) */
                /*TODO*///	driver_ghouls,	/* 12/1988 (c) 1988 (World) */
                /*TODO*///	driver_ghoulsu,	/* 12/1988 (c) 1988 (US) */
                /*TODO*///	driver_ghoulsj,	/* 12/1988 (c) 1988 (Japan) */
                /*TODO*///	driver_strider,	/*  3/1989 (c) 1989 */
                /*TODO*///	driver_striderj,	/*  3/1989 (c) 1989 */
                /*TODO*///	driver_stridrja,	/*  3/1989 (c) 1989 */
                /*TODO*///	driver_dwj,		/*  4/1989 (c) 1989 (Japan) */
                /*TODO*///	driver_willow,	/*  6/1989 (c) 1989 (Japan) */
                /*TODO*///	driver_willowj,	/*  6/1989 (c) 1989 (Japan) */
                /*TODO*///	driver_unsquad,	/*  8/1989 (c) 1989 */
                /*TODO*///	driver_area88,	/*  8/1989 (c) 1989 */
                /*TODO*///	driver_ffight,	/* 12/1989 (c) (World) */
                /*TODO*///	driver_ffightu,	/* 12/1989 (c) (US)    */
                /*TODO*///	driver_ffightj,	/* 12/1989 (c) (Japan) */
                /*TODO*///	driver_1941,		/*  2/1990 (c) 1990 (World) */
                /*TODO*///	driver_1941j,		/*  2/1990 (c) 1990 (Japan) */
                /*TODO*///	driver_mercs,		/*  3/ 2/1990 (c) 1990 (World) */
                /*TODO*///	driver_mercsu,	/*  3/ 2/1990 (c) 1990 (US)    */
                /*TODO*///	driver_mercsj,	/*  3/ 2/1990 (c) 1990 (Japan) */
                /*TODO*///	driver_mtwins,	/*  6/19/1990 (c) 1990 (World) */
                /*TODO*///	driver_chikij,	/*  6/19/1990 (c) 1990 (Japan) */
                /*TODO*///	driver_msword,	/*  7/25/1990 (c) 1990 (World) */
                /*TODO*///	driver_mswordu,	/*  7/25/1990 (c) 1990 (US)    */
                /*TODO*///	driver_mswordj,	/*  6/23/1990 (c) 1990 (Japan) */
                /*TODO*///	driver_cawing,	/* 10/12/1990 (c) 1990 (World) */
                /*TODO*///	driver_cawingj,	/* 10/12/1990 (c) 1990 (Japan) */
                /*TODO*///	driver_nemo,		/* 11/30/1990 (c) 1990 (World) */
                /*TODO*///	driver_nemoj,		/* 11/20/1990 (c) 1990 (Japan) */
                /*TODO*///	driver_sf2,		/*  2/14/1991 (c) 1991 (World) */
                /*TODO*///	driver_sf2a,		/*  2/ 6/1991 (c) 1991 (US)    */
                /*TODO*///	driver_sf2b,		/*  2/14/1991 (c) 1991 (US)    */
                /*TODO*///	driver_sf2e,		/*  2/28/1991 (c) 1991 (US)    */
                /*TODO*///	driver_sf2j,		/* 12/10/1991 (c) 1991 (Japan) */
                /*TODO*///	driver_sf2jb,		/*  2/14/1991 (c) 1991 (Japan) */
                /*TODO*///	driver_3wonders,	/*  5/20/1991 (c) 1991 (US) */
                /*TODO*///	driver_wonder3,	/*  5/20/1991 (c) 1991 (Japan) */
                /*TODO*///	driver_kod,		/*  7/11/1991 (c) 1991 (World) */
                /*TODO*///	driver_kodj,		/*  8/ 5/1991 (c) 1991 (Japan) */
                /*TODO*///	driver_kodb,		/* bootleg */
                /*TODO*///	driver_captcomm,	/* 10/14/1991 (c) 1991 (World) */
                /*TODO*///	driver_captcomu,	/*  9/28/1991 (c) 1991 (US)    */
                /*TODO*///	driver_captcomj,	/* 12/ 2/1991 (c) 1991 (Japan) */
                /*TODO*///	driver_knights,	/* 11/27/1991 (c) 1991 (World) */
                /*TODO*///	driver_knightsj,	/* 11/27/1991 (c) 1991 (Japan) */
                /*TODO*///	driver_sf2ce,		/*  3/13/1992 (c) 1992 (World) */
                /*TODO*///	driver_sf2cea,	/*  3/13/1992 (c) 1992 (US)    */
                /*TODO*///	driver_sf2ceb,	/*  5/13/1992 (c) 1992 (US)    */
                /*TODO*///	driver_sf2cej,	/*  5/13/1992 (c) 1992 (Japan) */
                /*TODO*///	driver_sf2rb,		/* hack */
                /*TODO*///	driver_sf2red,	/* hack */
                /*TODO*///	driver_sf2accp2,	/* hack */
                /*TODO*///	driver_varth,		/*  6/12/1992 (c) 1992 (World) */
                /*TODO*///	driver_varthu,	/*  6/12/1992 (c) 1992 (US) */
                /*TODO*///	driver_varthj,	/*  7/14/1992 (c) 1992 (Japan) */
                /*TODO*///	driver_cworld2j,	/*  6/11/1992 (QUIZ 5) (c) 1992 (Japan) */
                /*TODO*///	driver_wof,		/* 10/ 2/1992 (c) 1992 (World) (CPS1 + QSound) */
                /*TODO*///	driver_wofj,		/* 10/31/1992 (c) 1992 (Japan) (CPS1 + QSound) */
                /*TODO*///	driver_sf2t,		/* 12/ 9/1992 (c) 1992 (US)    */
                /*TODO*///	driver_sf2tj,		/* 12/ 9/1992 (c) 1992 (Japan) */
                /*TODO*///	driver_dino,		/*  2/ 1/1993 (c) 1993 (World) (CPS1 + QSound) */
                /*TODO*///	driver_dinoj,		/*  2/ 1/1993 (c) 1993 (Japan) (CPS1 + QSound) */
                /*TODO*///	driver_punisher,	/*  4/22/1993 (c) 1993 (World) (CPS1 + QSound) */
                /*TODO*///	driver_punishru,	/*  4/22/1993 (c) 1993 (US)    (CPS1 + QSound) */
                /*TODO*///	driver_punishrj,	/*  4/22/1993 (c) 1993 (Japan) (CPS1 + QSound) */
                /*TODO*///	driver_slammast,	/*  7/13/1993 (c) 1993 (World) (CPS1 + QSound) */
                /*TODO*///	driver_mbomberj,	/*  7/13/1993 (c) 1993 (Japan) (CPS1 + QSound) */
                /*TODO*///	driver_mbombrd,	/* 12/ 6/1993 (c) 1993 (World) (CPS1 + QSound) */
                /*TODO*///	driver_mbombrdj,	/* 12/ 6/1993 (c) 1993 (Japan) (CPS1 + QSound) */
                /*TODO*///	driver_pnickj,	/*  6/ 8/1994 (c) 1994 + Compile license (Japan) not listed on Capcom's site */
                /*TODO*///	driver_qad,		/*  7/ 1/1992 (c) 1992 (US)    */
                /*TODO*///	driver_qadj,		/*  9/21/1994 (c) 1994 (Japan) */
                /*TODO*///	driver_qtono2,	/*  1/23/1995 (c) 1995 (Japan) */
                /*TODO*///	driver_pang3,		/*  5/11/1995 (c) 1995 Mitchell (Japan) not listed on Capcom's site */
                /*TODO*///	driver_megaman,	/* 10/ 6/1995 (c) 1995 (Asia)  */
                /*TODO*///	driver_rockmanj,	/*  9/22/1995 (c) 1995 (Japan) */
                /*TODO*/////	driver_sfzch,		/* 10/20/1995 (c) 1995 (Japan) (CPS Changer) */
                /*TODO*///
                /*TODO*///	/* Capcom CPS2 games */
                /*TODO*///	/* list completed by CPS2Shock */
                /*TODO*///	/* http://cps2shock.retrogames.com */
                /*TODO*////*TESTDRIVER*/driver_ssf2,		/* Super Street Fighter 2: The New Challengers (USA 930911) */
                /*TODO*////*TESTDRIVER*/driver_ssf2a,		/* Super Street Fighter 2: The New Challengers (Asia 930911) */
                /*TODO*////*TESTDRIVER*/driver_ssf2j,		/* Super Street Fighter 2: The New Challengers (Japan 930910) */
                /*TODO*////*TESTDRIVER*/driver_ecofe,		/* Eco Fighters (Etc 931203) */
                /*TODO*////*TESTDRIVER*/driver_ddtod,		/* Dungeons & Dragons: Tower of Doom (USA 940113) */
                /*TODO*////*TESTDRIVER*/driver_ddtoda,	/* Dungeons & Dragons: Tower of Doom (Asia 940113) */
                /*TODO*////*TESTDRIVER*/driver_ddtodr1,	/* Dungeons & Dragons: Tower of Doom (USA 940125) */
                /*TODO*////*TESTDRIVER*/driver_ssf2t,		/* Super Street Fighter 2 Turbo (USA 940223) */
                /*TODO*////*TESTDRIVER*/driver_ssf2xj,	/* Super Street Fighter 2 X: Grand Master Challenge (Japan 940223) */
                /*TODO*////*TESTDRIVER*/driver_avsp,		/* Aliens Vs. Predator (USA 940520) */
                /*TODO*////*TESTDRIVER*/driver_vampj,		/* Vampire: The Night Warriors (Japan 940705) */
                /*TODO*////*TESTDRIVER*/driver_vampa,		/* Vampire: The Night Warriors (Asia 940705) */
                /*TODO*////*TESTDRIVER*/driver_dstlk,		/* DarkStalkers: The Night Warriors (USA 940818) */
                /*TODO*////*TESTDRIVER*/driver_slam2e,	/* Saturday Night Slammasters II: Ring of Destruction (Euro 940902) */
                /*TODO*////*TESTDRIVER*/driver_armwara,	/* Armoured Warriors (Asia 940920) */
                /*TODO*////*TESTDRIVER*/driver_xmcotaj,	/* X-Men: Children of the Atom (Japan 941219) */
                /*TODO*////*TESTDRIVER*/driver_xmcota,	/* X-Men: Children of the Atom (USA 950105) */
                /*TODO*////*TESTDRIVER*/driver_vhuntj,	/* Vampire Hunter: Darkstalkers 2 (Japan 950302) */
                /*TODO*////*TESTDRIVER*/driver_nwarr,		/* Night Warriors: DarkStalkers Revenge (USA 950406) */
                /*TODO*////*TESTDRIVER*/driver_cybotsj,	/* Cyberbots: Full Metal Madness (Japan 950420) */
                /*TODO*////*TESTDRIVER*/driver_sfa,		/* Street Fighter Alpha: The Warriors Dream (USA 950627) */
                /*TODO*////*TESTDRIVER*/driver_sfar1,		/* Street Fighter Alpha: The Warriors Dream (USA 950727) */
                /*TODO*////*TESTDRIVER*/driver_sfzj,		/* Street Fighter Zero (Japan 950627) */
                /*TODO*////*TESTDRIVER*/driver_sfzjr1,	/* Street Fighter Zero (Japan 950727) */
                /*TODO*////*TESTDRIVER*/driver_msh,		/* Marvel Super Heroes (USA 951024) */
                /*TODO*////*TESTDRIVER*/driver_19xx,		/* 19XX: The Battle Against Destiny (USA 951207) */
                /*TODO*////*TESTDRIVER*/driver_ddsom,		/* Dungeons & Dragons 2: Shadow over Mystara (USA 960209) */
                /*TODO*////*TESTDRIVER*/driver_sfz2j,		/* Street Fighter Zero 2 (Japan 960227) */
                /*TODO*////*TESTDRIVER*/driver_spf2xj,	/* Super Puzzle Fighter 2 X (Japan 960531) */
                /*TODO*////*TESTDRIVER*/driver_spf2t,		/* Super Puzzle Fighter 2 Turbo (USA 960620) */
                /*TODO*////*TESTDRIVER*/driver_rckman2j,	/* Rockman 2: The Power Fighters (Japan 960708) */
                /*TODO*////*TESTDRIVER*/driver_sfz2a,		/* Street Fighter Zero 2 Alpha (Japan 960805) */
                /*TODO*///						/*  9/1996 Quiz Naneiro Dreams */
                /*TODO*////*TESTDRIVER*/driver_xmvsf,		/* X-Men Vs. Street Fighter (USA 961004) */
                /*TODO*////*TESTDRIVER*/driver_batcirj,	/* Battle Circuit (Japan 970319) */
                /*TODO*////*TESTDRIVER*/driver_batcira,	/* Battle Circuit (Asia 970319) */
                /*TODO*////*TESTDRIVER*/driver_vsav,		/* Vampire Savior: The Lord of Vampire (USA 970519) */
                /*TODO*////*TESTDRIVER*/driver_vsavj,		/* Vampire Savior: The Lord of Vampire (Japan 970519) */
                /*TODO*////*TESTDRIVER*/driver_mshvsf,	/* Marvel Super Heroes Vs. Street Fighter (USA 970625) */
                /*TODO*////*TESTDRIVER*/driver_mshvsfj,	/* Marvel Super Heroes Vs. Street Fighter (Japan 970707) */
                /*TODO*////*TESTDRIVER*/driver_vhunt2,	/* Vampire Hunter 2: Darkstalkers Revenge (Japan 970828) */
                /*TODO*////*TESTDRIVER*/driver_sgemf,		/* Super Gem Fighter Mini Mix (USA 970904) */
                /*TODO*////*TESTDRIVER*/driver_pfghtj,	/* Pocket Fighter (Japan 970904) */
                /*TODO*////*TESTDRIVER*/driver_vsav2,		/* Vampire Savior 2: The Lord of Vampire (Japan 970913) */
                /*TODO*////*TESTDRIVER*/driver_mvsc,		/* Marvel Super Heroes vs. Capcom: Clash of Super Heroes (USA 980123) */
                /*TODO*////*TESTDRIVER*/driver_sfa3,		/* Street Fighter Alpha 3 (USA 980629) */
                /*TODO*///						/* 1999 Giga Wing */
                /*TODO*///						/* Gulum Pa! */
                /*TODO*///
                /*TODO*///	/* Capcom CPS3 games */
                /*TODO*///	/* 10/1996 Warzard */
                /*TODO*///	/*  2/1997 Street Fighter III - New Generation */
                /*TODO*///	/* ???? Jojo's Bizarre Adventure */
                /*TODO*///	/* ???? Street Fighter 3: Second Impact ~giant attack~ */
                /*TODO*///	/* ???? Street Fighter 3: Third Strike ~fight to the finish~ */
                /*TODO*///
                /*TODO*///	/* Capcom ZN1/ZN2 games */
                /*TODO*////*TESTDRIVER*/driver_ts2j,		/*  Battle Arena Toshinden 2 (JAPAN 951124) */
                /*TODO*///						/*  7/1996 Star Gladiator */
                /*TODO*////*TESTDRIVER*/driver_sfex,		/*  Street Fighter EX (ASIA 961219) */
                /*TODO*////*TESTDRIVER*/driver_sfexj,		/*  Street Fighter EX (JAPAN 961130) */
                /*TODO*////*TESTDRIVER*/driver_sfexp,		/*  Street Fighter EX Plus (USA 970311) */
                /*TODO*////*TESTDRIVER*/driver_sfexpj,	/*  Street Fighter EX Plus (JAPAN 970311) */
                /*TODO*////*TESTDRIVER*/driver_rvschool,	/*  Rival Schools (ASIA 971117) */
                /*TODO*////*TESTDRIVER*/driver_jgakuen,	/*  Justice Gakuen (JAPAN 971117) */
                /*TODO*////*TESTDRIVER*/driver_sfex2,		/*  Street Fighter EX 2 (JAPAN 980312) */
                /*TODO*////*TESTDRIVER*/driver_tgmj,		/*  Tetris The Grand Master (JAPAN 980710) */
                /*TODO*////*TESTDRIVER*/driver_sfex2p,	/*  Street Fighter EX 2 Plus (JAPAN 990611) */
                /*TODO*///						/*  Star Gladiator 2 */
                /*TODO*///						/*  Rival Schools 2 */
                /*TODO*///
                /*TODO*///	/* Mitchell games */
                /*TODO*///	driver_mgakuen,	/* (c) 1988 Yuga */
                /*TODO*///	driver_mgakuen2,	/* (c) 1989 Face */
                /*TODO*///	driver_pkladies,	/* (c) 1989 Mitchell */
                /*TODO*///	driver_dokaben,	/*  3/1989 (c) 1989 Capcom (Japan) */
                /*TODO*///	/*  8/1989 Dokaben 2 (baseball) */
                /*TODO*///	driver_pang,		/* (c) 1989 Mitchell (World) */
                /*TODO*///	driver_pangb,		/* bootleg */
                /*TODO*///	driver_bbros,		/* (c) 1989 Capcom (US) not listed on Capcom's site */
                /*TODO*///	driver_pompingw,	/* (c) 1989 Mitchell (Japan) */
                /*TODO*///	driver_cbasebal,	/* 10/1989 (c) 1989 Capcom (Japan) (different hardware) */
                /*TODO*///	driver_cworld,	/* 11/1989 (QUIZ 1) (c) 1989 Capcom */
                /*TODO*///	driver_hatena,	/*  2/28/1990 (QUIZ 2) (c) 1990 Capcom (Japan) */
                /*TODO*///	driver_spang,		/*  9/14/1990 (c) 1990 Mitchell (World) */
                /*TODO*///	driver_sbbros,	/* 10/ 1/1990 (c) 1990 Mitchell + Capcom (US) not listed on Capcom's site */
                /*TODO*///	driver_marukin,	/* 10/17/1990 (c) 1990 Yuga (Japan) */
                /*TODO*///	driver_qtono1,	/* 12/25/1990 (QUIZ 3) (c) 1991 Capcom (Japan) */
                /*TODO*///	/*  4/1991 Ashita Tenki ni Naare (golf) */
                /*TODO*///	driver_qsangoku,	/*  6/ 7/1991 (QUIZ 4) (c) 1991 Capcom (Japan) */
                /*TODO*///	driver_block,		/*  9/10/1991 (c) 1991 Capcom (World) */
                /*TODO*///	driver_blockj,	/*  9/10/1991 (c) 1991 Capcom (Japan) */
                /*TODO*///	driver_blockbl,	/* bootleg */
                /*TODO*///
                /*TODO*///	/* Incredible Technologies games */
                /*TODO*///	driver_capbowl,	/* (c) 1988 Incredible Technologies */
                /*TODO*///	driver_capbowl2,	/* (c) 1988 Incredible Technologies */
                /*TODO*///	driver_clbowl,	/* (c) 1989 Incredible Technologies */
                /*TODO*///	driver_bowlrama,	/* (c) 1991 P & P Marketing */
                /*TODO*////*
                /*TODO*///The Incredible Technologies game list
                /*TODO*///http://www.itsgames.com/it/CorporateProfile/corporateprofile_main.htm
                /*TODO*///
                /*TODO*///ShuffleShot - (Incredible Technologies, Inc.)
                /*TODO*///Peter Jacobsen's Golden Tee '97 - (Incredible Technologies, Inc.)
                /*TODO*///World Class Bowling - (Incredible Technologies, Inc.)
                /*TODO*///Peter Jacobsen's Golden Tee 3D Golf - (Incredible Technologies, Inc.)
                /*TODO*///Street Fighter - "The Movie" (Capcom)
                /*TODO*///PAIRS - (Strata)
                /*TODO*///BloodStorm - (Strata)
                /*TODO*///Driver's Edge - (Strata)
                /*TODO*///NFL Hard Yardage - (Strata)
                /*TODO*///Time Killers - (Strata)
                /*TODO*///Neck 'n' Neck - (Bundra Games)
                /*TODO*///Ninja Clowns - (Strata)
                /*TODO*///Rim Rockin' Basketball - (Strata)
                /*TODO*///Arlington Horse Racing - (Strata)
                /*TODO*///Dyno Bop - (Grand Products)
                /*TODO*///Poker Dice - (Strata)
                /*TODO*///Peggle - (Strata)
                /*TODO*///Slick Shot - (Grand Products)
                /*TODO*///Golden Tee Golf II - (Strata)
                /*TODO*///Hot Shots Tennis - (Strata)
                /*TODO*///Strata Bowling - (Strata)
                /*TODO*///Golden Tee Golf I - (Strata)
                /*TODO*///Capcom Bowling - (Strata)
                /*TODO*///*/
                /*TODO*///
                /*TODO*///	/* Leland games */
                /*TODO*////*TESTDRIVER*/driver_mayhem,	/* (c) 1985 Cinematronics */
                /*TODO*////*TESTDRIVER*/driver_wseries,	/* (c) 1985 Cinematronics Inc. */
                /*TODO*////*TESTDRIVER*/driver_dangerz,	/* (c) 1986 Cinematronics USA Inc. */
                /*TODO*////*TESTDRIVER*/driver_basebal2,	/* (c) 1987 Cinematronics Inc. */
                /*TODO*////*TESTDRIVER*/driver_dblplay,	/* (c) 1987 Tradewest / The Leland Corp. */
                /*TODO*////*TESTDRIVER*/driver_teamqb,	/* (c) 1988 Leland Corp. */
                /*TODO*////*TESTDRIVER*/driver_strkzone,	/* (c) 1988 The Leland Corporation */
                /*TODO*////*TESTDRIVER*/driver_offroad,	/* (c) 1989 Leland Corp. */
                /*TODO*////*TESTDRIVER*/driver_offroadt,
                /*TODO*////*TESTDRIVER*/driver_pigout,	/* (c) 1990 The Leland Corporation */
                /*TODO*////*TESTDRIVER*/driver_pigoutj,	/* (c) 1990 The Leland Corporation */
                /*TODO*////*TESTDRIVER*/driver_redlin2p,
                /*TODO*////*TESTDRIVER*/driver_viper,
                /*TODO*////*TESTDRIVER*/driver_aafb,
                /*TODO*////*TESTDRIVER*/driver_aafb2p,
                /*TODO*////*TESTDRIVER*/driver_aafbu,
                /*TODO*////*TESTDRIVER*/driver_alleymas,
                /*TODO*////*TESTDRIVER*/driver_cerberus,
                /*TODO*////*TESTDRIVER*/driver_ataxx,
                /*TODO*////*TESTDRIVER*/driver_ataxxa,
                /*TODO*////*TESTDRIVER*/driver_indyheat,
                /*TODO*////*TESTDRIVER*/driver_wsf,
                /*TODO*///
                /*TODO*///	/* Gremlin 8080 games */
                /*TODO*///	/* the numbers listed are the range of ROM part numbers */
                /*TODO*///	driver_blockade,	/* 1-4 [1977 Gremlin] */
                /*TODO*///	driver_comotion,	/* 5-7 [1977 Gremlin] */
                /*TODO*///	driver_hustle,	/* 16-21 [1977 Gremlin] */
                /*TODO*///	driver_blasto,	/* [1978 Gremlin] */
                /*TODO*///
                /*TODO*///	/* Gremlin/Sega "VIC dual game board" games */
                /*TODO*///	/* the numbers listed are the range of ROM part numbers */
                /*TODO*///	driver_depthch,	/* 50-55 [1977 Gremlin?] */
                /*TODO*///	driver_safari,	/* 57-66 [1977 Gremlin?] */
                /*TODO*///	driver_frogs,		/* 112-119 [1978 Gremlin?] */
                /*TODO*///	driver_sspaceat,	/* 155-162 (c) */
                /*TODO*///	driver_sspacatc,	/* 139-146 (c) */
                /*TODO*///	driver_headon,	/* 163-167/192-193 (c) Gremlin */
                /*TODO*///	driver_headonb,	/* 163-167/192-193 (c) Gremlin */
                /*TODO*///	driver_headon2,	/* ???-??? (c) 1979 Sega */
                /*TODO*///	/* ???-??? Fortress */
                /*TODO*///	/* ???-??? Gee Bee */
                /*TODO*///	/* 255-270  Head On 2 / Deep Scan */
                /*TODO*///	driver_invho2,	/* 271-286 (c) 1979 Sega */
                /*TODO*///	driver_samurai,	/* 289-302 + upgrades (c) 1980 Sega */
                /*TODO*///	driver_invinco,	/* 310-318 (c) 1979 Sega */
                /*TODO*///	driver_invds,		/* 367-382 (c) 1979 Sega */
                /*TODO*///	driver_tranqgun,	/* 413-428 (c) 1980 Sega */
                /*TODO*///	/* 450-465  Tranquilizer Gun (different version?) */
                /*TODO*///	/* ???-??? Car Hunt / Deep Scan */
                /*TODO*///	driver_spacetrk,	/* 630-645 (c) 1980 Sega */
                /*TODO*///	driver_sptrekct,	/* (c) 1980 Sega */
                /*TODO*///	driver_carnival,	/* 651-666 (c) 1980 Sega */
                /*TODO*///	driver_carnvckt,	/* 501-516 (c) 1980 Sega */
                /*TODO*///	driver_digger,	/* 684-691 no copyright notice */
                /*TODO*///	driver_pulsar,	/* 790-805 (c) 1981 Sega */
                /*TODO*///	driver_heiankyo,	/* (c) [1979?] Denki Onkyo */
                /*TODO*///
                /*TODO*///	/* Sega G-80 vector games */
                /*TODO*///	driver_spacfury,	/* (c) 1981 */
                /*TODO*///	driver_spacfura,	/* no copyright notice */
                /*TODO*///	driver_zektor,	/* (c) 1982 */
                /*TODO*///	driver_tacscan,	/* (c) */
                /*TODO*///	driver_elim2,		/* (c) 1981 Gremlin */
                /*TODO*///	driver_elim2a,	/* (c) 1981 Gremlin */
                /*TODO*///	driver_elim4,		/* (c) 1981 Gremlin */
                /*TODO*///	driver_startrek,	/* (c) 1982 */
                /*TODO*///
                /*TODO*///	/* Sega G-80 raster games */
                /*TODO*///	driver_astrob,	/* (c) 1981 */
                /*TODO*///	driver_astrob1,	/* (c) 1981 */
                /*TODO*///	driver_005,		/* (c) 1981 */
                /*TODO*///	driver_monsterb,	/* (c) 1982 */
                /*TODO*///	driver_spaceod,	/* (c) 1981 */
                /*TODO*///	driver_pignewt,	/* (c) 1983 */
                /*TODO*///	driver_pignewta,	/* (c) 1983 */
                /*TODO*///	driver_sindbadm,	/* 834-5244 (c) 1983 Sega */
                /*TODO*///
                /* Sega "Zaxxon hardware" games */
                driver_zaxxon, /* (c) 1982 */
                driver_zaxxon2, /* (c) 1982 */
                driver_zaxxonb, /* bootleg */
                driver_szaxxon, /* (c) 1982 */
                driver_futspy, /* (c) 1984 */
                driver_razmataz, /* modified 834-0213, 834-0214 (c) 1983 */
                driver_congo, /* 605-5167 (c) 1983 */
                driver_tiptop, /* 605-5167 (c) 1983 */
                /*TODO*///	/* Sega System 1 / System 2 games */
                /*TODO*///	driver_starjack,	/* 834-5191 (c) 1983 (S1) */
                /*TODO*///	driver_starjacs,	/* (c) 1983 Stern (S1) */
                /*TODO*///	driver_regulus,	/* 834-5328 (c) 1983 (S1) */
                /*TODO*///	driver_regulusu,	/* 834-5328 (c) 1983 (S1) */
                /*TODO*///	driver_upndown,	/* (c) 1983 (S1) */
                /*TODO*///	driver_mrviking,	/* 834-5383 (c) 1984 (S1) */
                /*TODO*///	driver_mrvikinj,	/* 834-5383 (c) 1984 (S1) */
                /*TODO*///	driver_swat,		/* 834-5388 (c) 1984 Coreland / Sega (S1) */
                /*TODO*///	driver_flicky,	/* (c) 1984 (S1) */
                /*TODO*///	driver_flicky2,	/* (c) 1984 (S1) */
                /*TODO*///	/* Water Match (S1) */
                /*TODO*///	driver_bullfgtj,	/* 834-5478 (c) 1984 Sega / Coreland (S1) */
                /*TODO*///	driver_pitfall2,	/* 834-5627 [1985?] reprogrammed, (c) 1984 Activision (S1) */
                /*TODO*///	driver_pitfallu,	/* 834-5627 [1985?] reprogrammed, (c) 1984 Activision (S1) */
                /*TODO*///	driver_seganinj,	/* 834-5677 (c) 1985 (S1) */
                /*TODO*///	driver_seganinu,	/* 834-5677 (c) 1985 (S1) */
                /*TODO*///	driver_nprinces,	/* 834-5677 (c) 1985 (S1) */
                /*TODO*///	driver_nprincsu,	/* 834-5677 (c) 1985 (S1) */
                /*TODO*///	driver_nprincsb,	/* bootleg? (S1) */
                /*TODO*///	driver_imsorry,	/* 834-5707 (c) 1985 Coreland / Sega (S1) */
                /*TODO*///	driver_imsorryj,	/* 834-5707 (c) 1985 Coreland / Sega (S1) */
                /*TODO*///	driver_teddybb,	/* 834-5712 (c) 1985 (S1) */
                /*TODO*///	driver_hvymetal,	/* 834-5745 (c) 1985 (S2?) */
                /*TODO*///	driver_myhero,	/* 834-5755 (c) 1985 (S1) */
                /*TODO*///	driver_myheroj,	/* 834-5755 (c) 1985 Coreland / Sega (S1) */
                /*TODO*///	driver_myherok,	/* 834-5755 (c) 1985 Coreland / Sega (S1) */
                /*TODO*///	driver_shtngmst,	/* 834-5719/5720 (c) 1985 (S2) */
                /*TODO*///	driver_chplft,	/* 834-5795 (c) 1985, (c) 1982 Dan Gorlin (S2) */
                /*TODO*///	driver_chplftb,	/* 834-5795 (c) 1985, (c) 1982 Dan Gorlin (S2) */
                /*TODO*///	driver_chplftbl,	/* bootleg (S2) */
                /*TODO*///	driver_4dwarrio,	/* 834-5918 (c) 1985 Coreland / Sega (S1) */
                /*TODO*///	driver_brain,		/* (c) 1986 Coreland / Sega (S2?) */
                /*TODO*///	driver_wboy,		/* 834-5984 (c) 1986 + Escape license (S1) */
                /*TODO*///	driver_wboy2,		/* 834-5984 (c) 1986 + Escape license (S1) */
                /*TODO*///	driver_wboy3,
                /*TODO*///	driver_wboy4,		/* 834-5984 (c) 1986 + Escape license (S1) */
                /*TODO*///	driver_wboyu,		/* 834-5753 (? maybe a conversion) (c) 1986 + Escape license (S1) */
                /*TODO*///	driver_wboy4u,	/* 834-5984 (c) 1986 + Escape license (S1) */
                /*TODO*///	driver_wbdeluxe,	/* (c) 1986 + Escape license (S1) */
                /*TODO*///	driver_gardia,	/* 834-6119 (S2?) */
                /*TODO*///	driver_gardiab,	/* bootleg */
                /*TODO*///	driver_blockgal,	/* 834-6303 (S1) */
                /*TODO*///	driver_blckgalb,	/* bootleg */
                /*TODO*///	driver_tokisens,	/* (c) 1987 (from a bootleg board) (S2) */
                /*TODO*///	driver_wbml,		/* bootleg (S2) */
                /*TODO*///	driver_wbmlj,		/* (c) 1987 Sega/Westone (S2) */
                /*TODO*///	driver_wbmlj2,	/* (c) 1987 Sega/Westone (S2) */
                /*TODO*///	driver_wbmlju,	/* bootleg? (S2) */
                /*TODO*///	driver_dakkochn,	/* 836-6483? (S2) */
                /*TODO*///	driver_ufosensi,	/* 834-6659 (S2) */
                /*TODO*////*
                /*TODO*///other System 1 / System 2 games:
                /*TODO*///
                /*TODO*///WarBall
                /*TODO*///Rafflesia
                /*TODO*///Sanrin Sanchan
                /*TODO*///DokiDoki Penguin Land *not confirmed
                /*TODO*///*/
                /*TODO*///
                /*TODO*///	/* Sega System E games (Master System hardware) */
                /*TODO*////*
                /*TODO*///???          834-5492 (??? not sure it's System E)
                /*TODO*///Transformer  834-5803 (c) 1986
                /*TODO*///Opa Opa
                /*TODO*///Fantasy Zone 2
                /*TODO*///Hang-On Jr.
                /*TODO*///(more?)
                /*TODO*///*/
                /*TODO*///
                /*TODO*///	/* other Sega 8-bit games */
                /*TODO*///	driver_turbo,		/* (c) 1981 Sega */
                /*TODO*///	driver_turboa,	/* (c) 1981 Sega */
                /*TODO*///	driver_turbob,	/* (c) 1981 Sega */
                /*TODO*////*TESTDRIVER*/driver_kopunch,	/* 834-0103 (c) 1981 Sega */
                /*TODO*///	driver_suprloco,	/* (c) 1982 Sega */
                driver_champbas, /* (c) 1983 Sega */
                driver_champbb2,
                driver_appoooh, /* (c) 1984 Sega */
                driver_bankp,/* (c) 1984 Sega */
                /*TODO*///	driver_dotrikun,	/* cabinet test board */
                /*TODO*///	driver_dotriku2,	/* cabinet test board */
                /*TODO*///
                /*TODO*///	/* Sega System 16 games */
                /*TODO*///	// Not working
                /*TODO*///	driver_alexkidd,	/* (c) 1986 (protected) */
                /*TODO*///	driver_aliensya,	/* (c) 1987 (protected) */
                /*TODO*///	driver_aliensyb,	/* (c) 1987 (protected) */
                /*TODO*///	driver_aliensyj,	/* (c) 1987 (protected. Japan) */
                /*TODO*///	driver_astorm,	/* (c) 1990 (protected) */
                /*TODO*///	driver_astorm2p,	/* (c) 1990 (protected 2 Players) */
                /*TODO*///	driver_auraila,	/* (c) 1990 Sega / Westone (protected) */
                /*TODO*///	driver_bayrouta,	/* (c) 1989 (protected) */
                /*TODO*///	driver_bayrtbl1,	/* (c) 1989 (protected) (bootleg) */
                /*TODO*///	driver_bayrtbl2,	/* (c) 1989 (protected) (bootleg) */
                /*TODO*///	driver_enduror,	/* (c) 1985 (protected) */
                /*TODO*///	driver_eswat,		/* (c) 1989 (protected) */
                /*TODO*///	driver_fpoint,	/* (c) 1989 (protected) */
                /*TODO*///	driver_goldnaxb,	/* (c) 1989 (protected) */
                /*TODO*///	driver_goldnaxc,	/* (c) 1989 (protected) */
                /*TODO*///	driver_goldnaxj,	/* (c) 1989 (protected. Japan) */
                /*TODO*///	driver_jyuohki,	/* (c) 1988 (protected. Altered Beast Japan) */
                /*TODO*///	driver_moonwalk,	/* (c) 1990 (protected) */
                /*TODO*///	driver_moonwlka,	/* (c) 1990 (protected) */
                /*TODO*///	driver_passsht,	/* (protected) */
                /*TODO*///	driver_sdioj,		/* (c) 1987 (protected. Japan) */
                /*TODO*///	driver_shangon,	/* (c) 1992 (protected) */
                /*TODO*///	driver_shinobia,	/* (c) 1987 (protected) */
                /*TODO*///	driver_shinobib,	/* (c) 1987 (protected) */
                /*TODO*///	driver_tetris,	/* (c) 1988 (protected) */
                /*TODO*///	driver_tetrisa,	/* (c) 1988 (protected) */
                /*TODO*///	driver_wb3a,		/* (c) 1988 Sega / Westone (protected) */
                /*TODO*///
                /*TODO*////*TESTDRIVER*/driver_aceattac,	/* (protected) */
                /*TODO*////*TESTDRIVER*/driver_aburner,	/* */
                /*TODO*////*TESTDRIVER*/driver_aburner2,  /* */
                /*TODO*////*TESTDRIVER*/driver_afighter,	/* (protected) */
                /*TODO*////*TESTDRIVER*/driver_bloxeed,	/* (protected) */
                /*TODO*////*TESTDRIVER*/driver_cltchitr,	/* (protected) */
                /*TODO*////*TESTDRIVER*/driver_cotton,	/* (protected) */
                /*TODO*////*TESTDRIVER*/driver_cottona,	/* (protected) */
                /*TODO*////*TESTDRIVER*/driver_ddcrew,	/* (protected) */
                /*TODO*////*TESTDRIVER*/driver_dunkshot,	/* (protected) */
                /*TODO*////*TESTDRIVER*/driver_exctleag,  /* (protected) */
                /*TODO*////*TESTDRIVER*/driver_lghost,	/* (protected) */
                /*TODO*////*TESTDRIVER*/driver_loffire,	/* (protected) */
                /*TODO*////*TESTDRIVER*/driver_mvp,		/* (protected) */
                /*TODO*////*TESTDRIVER*/driver_ryukyu,	/* (protected) */
                /*TODO*////*TESTDRIVER*/driver_suprleag,  /* (protected) */
                /*TODO*////*TESTDRIVER*/driver_thndrbld,	/* (protected) */
                /*TODO*////*TESTDRIVER*/driver_thndrbdj,  /* (protected?) */
                /*TODO*////*TESTDRIVER*/driver_toutrun,	/* (protected) */
                /*TODO*////*TESTDRIVER*/driver_toutruna,	/* (protected) */
                /*TODO*///
                /*TODO*///	// Working
                /*TODO*///	driver_alexkida,	/* (c) 1986 */
                /*TODO*///	driver_aliensyn,	/* (c) 1987 */
                /*TODO*///	driver_altbeas2,	/* (c) 1988 */
                /*TODO*///	driver_altbeast,	/* (c) 1988 */
                /*TODO*///	driver_astormbl,	/* bootleg */
                /*TODO*///	driver_atomicp,	/* (c) 1990 Philko */
                /*TODO*///	driver_aurail,	/* (c) 1990 Sega / Westone */
                /*TODO*///	driver_bayroute,	/* (c) 1989 */
                /*TODO*///	driver_bodyslam,	/* (c) 1986 */
                /*TODO*///	driver_dduxbl,	/* (c) 1989 (Datsu bootleg) */
                /*TODO*///	driver_dumpmtmt,	/* (c) 1986 (Japan) */
                /*TODO*///	driver_endurob2,	/* (c) 1985 (Beta bootleg) */
                /*TODO*///	driver_endurobl,	/* (c) 1985 (Herb bootleg) */
                /*TODO*///	driver_eswatbl,	/* (c) 1989 (but bootleg) */
                /*TODO*///	driver_fantzone,	/* (c) 1986 */
                /*TODO*///	driver_fantzono,	/* (c) 1986 */
                /*TODO*///	driver_fpointbl,	/* (c) 1989 (Datsu bootleg) */
                /*TODO*///	driver_goldnabl,	/* (c) 1989 (bootleg) */
                /*TODO*///	driver_goldnaxa,	/* (c) 1989 */
                /*TODO*///	driver_goldnaxe,	/* (c) 1989 */
                /*TODO*///	driver_hangon,	/* (c) 1985 */
                /*TODO*///	driver_hwchamp,	/* (c) 1987 */
                /*TODO*///	driver_mjleague,	/* (c) 1985 */
                /*TODO*///	driver_moonwlkb,	/* bootleg */
                /*TODO*///	driver_outrun,	/* (c) 1986 (bootleg)*/
                /*TODO*///	driver_outruna,	/* (c) 1986 (bootleg) */
                /*TODO*///	driver_outrunb,	/* (c) 1986 (protected beta bootleg) */
                /*TODO*///	driver_passht4b,	/* bootleg */
                /*TODO*///	driver_passshtb,	/* bootleg */
                /*TODO*///	driver_quartet,	/* (c) 1986 */
                /*TODO*///	driver_quartet2,	/* (c) 1986 */
                /*TODO*///	driver_quartetj,	/* (c) 1986 */
                /*TODO*///	driver_riotcity,	/* (c) 1991 Sega / Westone */
                /*TODO*///	driver_sdi,		/* (c) 1987 */
                /*TODO*///	driver_shangonb,	/* (c) 1992 (but bootleg) */
                /*TODO*///	driver_sharrier,	/* (c) 1985 */
                /*TODO*///	driver_shdancbl,	/* (c) 1989 (but bootleg) */
                /*TODO*///	driver_shdancer,	/* (c) 1989 */
                /*TODO*///	driver_shdancrj,	/* (c) 1989 */
                /*TODO*///	driver_shinobi,	/* (c) 1987 */
                /*TODO*///	driver_shinobl,	/* (c) 1987 (but bootleg) */
                /*TODO*///	driver_tetrisbl,	/* (c) 1988 (but bootleg) */
                /*TODO*///	driver_timscanr,	/* (c) 1987 */
                /*TODO*///	driver_toryumon,	/* (c) 1995 */
                /*TODO*///	driver_tturf,		/* (c) 1989 Sega / Sunsoft */
                /*TODO*///	driver_tturfbl,	/* (c) 1989 (Datsu bootleg) */
                /*TODO*///	driver_tturfu,	/* (c) 1989 Sega / Sunsoft */
                /*TODO*///	driver_wb3,		/* (c) 1988 Sega / Westone */
                /*TODO*///	driver_wb3bl,		/* (c) 1988 Sega / Westone (but bootleg) */
                /*TODO*///	driver_wrestwar,	/* (c) 1989 */
                /*TODO*///
                /*TODO*///	/* Data East "Burger Time hardware" games */
                /*TODO*///	driver_lnc,		/* (c) 1981 */
                /*TODO*///	driver_zoar,		/* (c) 1982 */
                /*TODO*///	driver_btime,		/* (c) 1982 */
                /*TODO*///	driver_btime2,	/* (c) 1982 */
                /*TODO*///	driver_btimem,	/* (c) 1982 + Midway */
                /*TODO*///	driver_wtennis,	/* bootleg 1982 */
                /*TODO*///	driver_brubber,	/* (c) 1982 */
                /*TODO*///	driver_bnj,		/* (c) 1982 + Midway */
                /*TODO*///	driver_caractn,	/* bootleg */
                /*TODO*///	driver_disco,		/* (c) 1982 */
                /*TODO*///	driver_mmonkey,	/* (c) 1982 Technos Japan + Roller Tron */
                /*TODO*///	/* cassette system */
                /*TODO*////*TESTDRIVER*/driver_decocass,
                /*TODO*///	driver_cookrace,	/* bootleg */
                /*TODO*///
                /*TODO*///	/* other Data East games */
                /*TODO*///	driver_astrof,	/* (c) [1980?] */
                /*TODO*///	driver_astrof2,	/* (c) [1980?] */
                /*TODO*///	driver_astrof3,	/* (c) [1980?] */
                /*TODO*///	driver_tomahawk,	/* (c) [1980?] */
                /*TODO*///	driver_tomahaw5,	/* (c) [1980?] */
                driver_kchamp, /* (c) 1984 Data East USA (US) */
                driver_karatedo, /* (c) 1984 Data East Corporation (Japan) */
                driver_kchampvs, /* (c) 1984 Data East USA (US) */
                driver_karatevs, /* (c) 1984 Data East Corporation (Japan) */
                /*TODO*///	driver_firetrap,	/* (c) 1986 */
                /*TODO*///	driver_firetpbl,	/* bootleg */
                /*TODO*///	driver_brkthru,	/* (c) 1986 Data East USA (US) */
                /*TODO*///	driver_brkthruj,	/* (c) 1986 Data East Corporation (Japan) */
                /*TODO*///	driver_darwin,	/* (c) 1986 Data East Corporation (Japan) */
                /*TODO*///	driver_shootout,	/* (c) 1985 Data East USA (US) */
                /*TODO*///	driver_shootouj,	/* (c) 1985 Data East USA (Japan) */
                /*TODO*///	driver_shootoub,	/* bootleg */
                /*TODO*///	driver_sidepckt,	/* (c) 1986 Data East Corporation */
                /*TODO*///	driver_sidepctj,	/* (c) 1986 Data East Corporation */
                /*TODO*///	driver_sidepctb,	/* bootleg */
                /*TODO*///	driver_exprraid,	/* (c) 1986 Data East USA (US) */
                /*TODO*///	driver_wexpress,	/* (c) 1986 Data East Corporation (World?) */
                /*TODO*///	driver_wexpresb,	/* bootleg */
                /*TODO*///	driver_pcktgal,	/* (c) 1987 Data East Corporation (Japan) */
                /*TODO*///	driver_pcktgalb,	/* bootleg */
                /*TODO*///	driver_pcktgal2,	/* (c) 1989 Data East Corporation (World?) */
                /*TODO*///	driver_spool3,	/* (c) 1989 Data East Corporation (World?) */
                /*TODO*///	driver_spool3i,	/* (c) 1990 Data East Corporation + I-Vics license */
                /*TODO*///	driver_actfancr,	/* (c) 1989 Data East Corporation (World) */
                /*TODO*///	driver_actfanc1,	/* (c) 1989 Data East Corporation (World) */
                /*TODO*///	driver_actfancj,	/* (c) 1989 Data East Corporation (Japan) */
                /*TODO*///	driver_triothep,	/* (c) 1989 Data East Corporation (Japan) */
                /*TODO*///
                /*TODO*///	/* Data East 8-bit games */
                /*TODO*///	driver_lastmiss,	/* (c) 1986 Data East USA (US) */
                /*TODO*///	driver_lastmss2,	/* (c) 1986 Data East USA (US) */
                /*TODO*///	driver_shackled,	/* (c) 1986 Data East USA (US) */
                /*TODO*///	driver_breywood,	/* (c) 1986 Data East Corporation (Japan) */
                /*TODO*///	driver_csilver,	/* (c) 1987 Data East Corporation (Japan) */
                /*TODO*///	driver_ghostb,	/* (c) 1987 Data East USA (US) */
                /*TODO*///	driver_ghostb3,	/* (c) 1987 Data East USA (US) */
                /*TODO*///	driver_meikyuh,	/* (c) 1987 Data East Corporation (Japan) */
                /*TODO*///	driver_srdarwin,	/* (c) 1987 Data East Corporation (Japan) */
                /*TODO*///	driver_gondo,		/* (c) 1987 Data East USA (US) */
                /*TODO*///	driver_makyosen,	/* (c) 1987 Data East Corporation (Japan) */
                /*TODO*///	driver_garyoret,	/* (c) 1987 Data East Corporation (Japan) */
                /*TODO*///	driver_cobracom,	/* (c) 1988 Data East Corporation (World) */
                /*TODO*///	driver_cobracmj,	/* (c) 1988 Data East Corporation (Japan) */
                /*TODO*///	driver_oscar,		/* (c) 1988 Data East USA (US) */
                /*TODO*///	driver_oscarj,	/* (c) 1987 Data East Corporation (Japan) */
                /*TODO*///
                /*TODO*///	/* Data East 16-bit games */
                /*TODO*///	driver_karnov,	/* (c) 1987 Data East USA (US) */
                /*TODO*///	driver_karnovj,	/* (c) 1987 Data East Corporation (Japan) */
                /*TODO*////*TESTDRIVER*/driver_wndrplnt,	/* (c) 1987 Data East Corporation (Japan) */
                /*TODO*///	driver_chelnov,	/* (c) 1988 Data East USA (US) */
                /*TODO*///	driver_chelnovj,	/* (c) 1988 Data East Corporation (Japan) */
                /*TODO*////* the following ones all run on similar hardware */
                /*TODO*///	driver_hbarrel,	/* (c) 1987 Data East USA (US) */
                /*TODO*///	driver_hbarrelw,	/* (c) 1987 Data East Corporation (World) */
                /*TODO*///	driver_baddudes,	/* (c) 1988 Data East USA (US) */
                /*TODO*///	driver_drgninja,	/* (c) 1988 Data East Corporation (Japan) */
                /*TODO*////*TESTDRIVER*/driver_birdtry,	/* (c) 1988 Data East Corporation (Japan) */
                /*TODO*///	driver_robocop,	/* (c) 1988 Data East Corporation (World) */
                /*TODO*///	driver_robocopu,	/* (c) 1988 Data East USA (US) */
                /*TODO*///	driver_robocpu0,	/* (c) 1988 Data East USA (US) */
                /*TODO*///	driver_robocopb,	/* bootleg */
                /*TODO*///	driver_hippodrm,	/* (c) 1989 Data East USA (US) */
                /*TODO*///	driver_ffantasy,	/* (c) 1989 Data East Corporation (Japan) */
                /*TODO*///	driver_slyspy,	/* (c) 1989 Data East USA (US) */
                /*TODO*///	driver_slyspy2,	/* (c) 1989 Data East USA (US) */
                /*TODO*///	driver_secretag,	/* (c) 1989 Data East Corporation (World) */
                /*TODO*////*TESTDRIVER*/driver_secretab,	/* bootleg */
                /*TODO*///	driver_midres,	/* (c) 1989 Data East Corporation (World) */
                /*TODO*///	driver_midresu,	/* (c) 1989 Data East USA (US) */
                /*TODO*///	driver_midresj,	/* (c) 1989 Data East Corporation (Japan) */
                /*TODO*///	driver_bouldash,	/* (c) 1990 Data East Corporation */
                /*TODO*////* end of similar hardware */
                /*TODO*///	driver_stadhero,	/* (c) 1988 Data East Corporation (Japan) */
                /*TODO*///	driver_madmotor,	/* (c) [1989] Mitchell */
                /*TODO*///	/* All these games have a unique code stamped on the mask roms */
                /*TODO*///	driver_vaportra,	/* MAA (c) 1989 Data East Corporation (US) */
                /*TODO*///	driver_kuhga,		/* MAA (c) 1989 Data East Corporation (Japan) */
                /*TODO*///	driver_cbuster,	/* MAB (c) 1990 Data East Corporation (World) */
                /*TODO*///	driver_cbusterw,	/* MAB (c) 1990 Data East Corporation (World) */
                /*TODO*///	driver_cbusterj,	/* MAB (c) 1990 Data East Corporation (Japan) */
                /*TODO*///	driver_twocrude,	/* MAB (c) 1990 Data East USA (US) */
                /*TODO*///	driver_darkseal,	/* MAC (c) 1990 Data East Corporation (World) */
                /*TODO*///	driver_darksea1,	/* MAC (c) 1990 Data East Corporation (World) */
                /*TODO*///	driver_darkseaj,	/* MAC (c) 1990 Data East Corporation (Japan) */
                /*TODO*///	driver_gatedoom,	/* MAC (c) 1990 Data East Corporation (US) */
                /*TODO*///	driver_gatedom1,	/* MAC (c) 1990 Data East Corporation (US) */
                /*TODO*////*TESTDRIVER*/driver_edrandy,	/* MAD (c) 1990 Data East Corporation (World) */
                /*TODO*////*TESTDRIVER*/driver_edrandyj,	/* MAD (c) 1990 Data East Corporation (Japan) */
                /*TODO*///	driver_supbtime,	/* MAE (c) 1990 Data East Corporation (Japan) */
                /*TODO*///	/* Mutant Fighter/Death Brade MAF (c) 1991 */
                /*TODO*///	driver_cninja,	/* MAG (c) 1991 Data East Corporation (World) */
                /*TODO*///	driver_cninja0,	/* MAG (c) 1991 Data East Corporation (World) */
                /*TODO*///	driver_cninjau,	/* MAG (c) 1991 Data East Corporation (US) */
                /*TODO*///	driver_joemac,	/* MAG (c) 1991 Data East Corporation (Japan) */
                /*TODO*///	driver_stoneage,	/* bootleg */
                /*TODO*///	/* Robocop 2           MAH (c) 1991 */
                /*TODO*///	/* Desert Assault/Thunderzone MAJ (c) 1991 */
                /*TODO*///	/* Rohga Armour Attack/Wolf Fang MAM (c) 1991 */
                /*TODO*///	/* Captain America     MAN (c) 1991 */
                /*TODO*///	driver_tumblep,	/* MAP (c) 1991 Data East Corporation (World) */
                /*TODO*///	driver_tumblepj,	/* MAP (c) 1991 Data East Corporation (Japan) */
                /*TODO*///	driver_tumblepb,	/* bootleg */
                /*TODO*///	driver_tumblep2,	/* bootleg */
                /*TODO*///	/* Dragon Gun/Dragoon  MAR (c) 1992 */
                /*TODO*///	/* Wizard's Fire       MAS (c) 1992 */
                /*TODO*////*TESTDRIVER*/driver_funkyjet,	/* MAT (c) 1992 Mitchell */
                /*TODO*///	/* Diet GoGo      	   MAY (c) 1993 */
                /*TODO*/// 	/* Fighter's History   MBF (c) 1993 */
                /*TODO*///	/* Joe & Mac Return    MBN (c) 1994 */
                /*TODO*///	/* Chain Reaction      MCC (c) 1994 */
                /*TODO*///
                /*TODO*///	/* Tehkan / Tecmo games (Tehkan became Tecmo in 1986) */
                /*TODO*///	driver_senjyo,	/* (c) 1983 Tehkan */
                /*TODO*///	driver_starforc,	/* (c) 1984 Tehkan */
                /*TODO*///	driver_starfore,	/* (c) 1984 Tehkan */
                /*TODO*///	driver_megaforc,	/* (c) 1985 Tehkan + Video Ware license */
                /*TODO*///	driver_baluba,	/* (c) 1986 Able Corp. */
                driver_bombjack, /* (c) 1984 Tehkan */
                driver_bombjac2, /* (c) 1984 Tehkan */
                driver_pbaction, /* (c) 1985 Tehkan */
                driver_pbactio2, /* (c) 1985 Tehkan */
                /* 6011 Pontoon (c) 1985 Tehkan is a gambling game - removed */
                driver_tehkanwc, /* (c) 1985 Tehkan */
                driver_gridiron, /* (c) 1985 Tehkan */
                driver_teedoff, /* 6102 - (c) 1986 Tecmo */
                driver_solomon, /* (c) 1986 Tecmo */
                driver_rygar, /* 6002 - (c) 1986 Tecmo */
                driver_rygar2, /* 6002 - (c) 1986 Tecmo */
                driver_rygarj, /* 6002 - (c) 1986 Tecmo */
                driver_gemini, /* (c) 1987 Tecmo */
                driver_silkworm, /* 6217 - (c) 1988 Tecmo */
                driver_silkwrm2, /* 6217 - (c) 1988 Tecmo */
                /*TODO*///	driver_gaiden,	/* 6215 - (c) 1988 Tecmo */
                /*TODO*///	driver_shadoww,	/* 6215 - (c) 1988 Tecmo */
                /*TODO*///	driver_tknight,	/* (c) 1989 Tecmo */
                /*TODO*///	driver_wildfang,	/* (c) 1989 Tecmo */
                driver_wc90, /* (c) 1989 Tecmo */
                driver_wc90b, /* bootleg */
                /*TODO*///
                /*TODO*////* Other Tehkan games:
                /*TODO*///6009 Tank Busters
                /*TODO*///*/
                /*TODO*///
                /*TODO*///	/* Konami bitmap games */
                /*TODO*///	driver_tutankhm,	/* GX350 (c) 1982 Konami */
                /*TODO*///	driver_tutankst,	/* GX350 (c) 1982 Stern */
                /*TODO*///	driver_junofrst,	/* GX310 (c) 1983 Konami */
                /*TODO*///
                /* Konami games */
                driver_pooyan, /* GX320 (c) 1982 */
                driver_pooyans, /* GX320 (c) 1982 Stern */
                driver_pootan, /* bootleg */
                driver_timeplt, /* GX393 (c) 1982 */
                driver_timepltc, /* GX393 (c) 1982 + Centuri license*/
                driver_spaceplt, /* bootleg */
                driver_psurge, /* (c) 1988 unknown (NOT Konami) */
                /*TODO*///	driver_megazone,	/* GX319 (c) 1983 */
                /*TODO*///	driver_megaznik,	/* GX319 (c) 1983 + Interlogic / Kosuka */
                /*TODO*///	driver_pandoras,	/* GX328 (c) 1984 + Interlogic */
                /*TODO*///	driver_gyruss,	/* GX347 (c) 1983 */
                /*TODO*///	driver_gyrussce,	/* GX347 (c) 1983 + Centuri license */
                /*TODO*///	driver_venus,		/* bootleg */
                /*TODO*///	driver_trackfld,	/* GX361 (c) 1983 */
                /*TODO*///	driver_trackflc,	/* GX361 (c) 1983 + Centuri license */
                /*TODO*///	driver_hyprolym,	/* GX361 (c) 1983 */
                /*TODO*///	driver_hyprolyb,	/* bootleg */
                /*TODO*///	driver_rocnrope,	/* GX364 (c) 1983 */
                /*TODO*///	driver_rocnropk,	/* GX364 (c) 1983 + Kosuka */
                /*TODO*///	driver_circusc,	/* GX380 (c) 1984 */
                /*TODO*///	driver_circusc2,	/* GX380 (c) 1984 */
                /*TODO*///	driver_circuscc,	/* GX380 (c) 1984 + Centuri license */
                /*TODO*///	driver_circusce,	/* GX380 (c) 1984 + Centuri license */
                /*TODO*///	driver_tp84,		/* GX388 (c) 1984 */
                /*TODO*///	driver_tp84a,		/* GX388 (c) 1984 */
                /*TODO*///	driver_hyperspt,	/* GX330 (c) 1984 + Centuri */
                /*TODO*///	driver_hpolym84,	/* GX330 (c) 1984 */
                /*TODO*///	driver_sbasketb,	/* GX405 (c) 1984 */
                /*TODO*///	driver_mikie,		/* GX469 (c) 1984 */
                /*TODO*///	driver_mikiej,	/* GX469 (c) 1984 */
                /*TODO*///	driver_mikiehs,	/* GX469 (c) 1984 */
                /*TODO*///	driver_roadf,		/* GX461 (c) 1984 */
                /*TODO*///	driver_roadf2,	/* GX461 (c) 1984 */
                /*TODO*///	driver_yiear,		/* GX407 (c) 1985 */
                /*TODO*///	driver_yiear2,	/* GX407 (c) 1985 */
                /*TODO*///	driver_kicker,	/* GX477 (c) 1985 */
                /*TODO*///	driver_shaolins,	/* GX477 (c) 1985 */
                driver_pingpong, /* GX555 (c) 1985 */
                /*TODO*///	driver_gberet,	/* GX577 (c) 1985 */
                /*TODO*///	driver_rushatck,	/* GX577 (c) 1985 */
                /*TODO*///	driver_gberetb,	/* bootleg on different hardware */
                /*TODO*///	driver_mrgoemon,	/* GX621 (c) 1986 (Japan) */
                /*TODO*///	driver_jailbrek,	/* GX507 (c) 1986 */
                /*TODO*///	driver_finalizr,	/* GX523 (c) 1985 */
                /*TODO*///	driver_finalizb,	/* bootleg */
                /*TODO*///	driver_ironhors,	/* GX560 (c) 1986 */
                /*TODO*///	driver_dairesya,	/* GX560 (c) 1986 (Japan) */
                /*TODO*///	driver_farwest,
                /*TODO*///	driver_jackal,	/* GX631 (c) 1986 (World) */
                /*TODO*///	driver_topgunr,	/* GX631 (c) 1986 (US) */
                /*TODO*///	driver_jackalj,	/* GX631 (c) 1986 (Japan) */
                /*TODO*///	driver_topgunbl,	/* bootleg */
                /*TODO*///	driver_ddribble,	/* GX690 (c) 1986 */
                /*TODO*///	driver_contra,	/* GX633 (c) 1987 */
                /*TODO*///	driver_contrab,	/* bootleg */
                /*TODO*///	driver_contraj,	/* GX633 (c) 1987 (Japan) */
                /*TODO*///	driver_contrajb,	/* bootleg */
                /*TODO*///	driver_gryzor,	/* GX633 (c) 1987 */
                /*TODO*///	driver_combasc,	/* GX611 (c) 1988 */
                /*TODO*///	driver_combasct,	/* GX611 (c) 1987 */
                /*TODO*///	driver_combascj,	/* GX611 (c) 1987 (Japan) */
                /*TODO*///	driver_bootcamp,	/* GX611 (c) 1987 */
                /*TODO*///	driver_combascb,	/* bootleg */
                /*TODO*///	driver_rockrage,	/* GX620 (c) 1986 (World?) */
                /*TODO*///	driver_rockragj,	/* GX620 (c) 1986 (Japan) */
                /*TODO*///	driver_mx5000,	/* GX669 (c) 1987 */
                /*TODO*///	driver_flkatck,	/* GX669 (c) 1987 (Japan) */
                /*TODO*///	driver_fastlane,	/* GX752 (c) 1987 */
                /*TODO*///	driver_labyrunr,	/* GX771 (c) 1987 (Japan) */
                /*TODO*///	driver_thehustl,	/* GX765 (c) 1987 (Japan) */
                /*TODO*///	driver_thehustj,	/* GX765 (c) 1987 (Japan) */
                /*TODO*///	driver_battlnts,	/* GX777 (c) 1987 */
                /*TODO*///	driver_battlntj,	/* GX777 (c) 1987 (Japan) */
                /*TODO*///	driver_bladestl,	/* GX797 (c) 1987 */
                /*TODO*///	driver_bladstle,	/* GX797 (c) 1987 */
                /*TODO*///	driver_hcastle,	/* GX768 (c) 1988 */
                /*TODO*///	driver_hcastlea,	/* GX768 (c) 1988 */
                /*TODO*///	driver_hcastlej,	/* GX768 (c) 1988 (Japan) */
                /*TODO*///	driver_ajax,		/* GX770 (c) 1987 */
                /*TODO*///	driver_ajaxj,		/* GX770 (c) 1987 (Japan) */
                /*TODO*///	driver_scontra,	/* GX775 (c) 1988 */
                /*TODO*///	driver_scontraj,	/* GX775 (c) 1988 (Japan) */
                /*TODO*///	driver_thunderx,	/* GX873 (c) 1988 */
                /*TODO*///	driver_thnderxj,	/* GX873 (c) 1988 (Japan) */
                /*TODO*///	driver_mainevt,	/* GX799 (c) 1988 */
                /*TODO*///	driver_mainevt2,	/* GX799 (c) 1988 */
                /*TODO*///	driver_ringohja,	/* GX799 (c) 1988 (Japan) */
                /*TODO*///	driver_devstors,	/* GX890 (c) 1988 */
                /*TODO*///	driver_devstor2,	/* GX890 (c) 1988 */
                /*TODO*///	driver_devstor3,	/* GX890 (c) 1988 */
                /*TODO*///	driver_garuka,	/* GX890 (c) 1988 (Japan) */
                /*TODO*///	driver_88games,	/* GX861 (c) 1988 */
                /*TODO*///	driver_konami88,	/* GX861 (c) 1988 */
                /*TODO*///	driver_hypsptsp,	/* GX861 (c) 1988 (Japan) */
                /*TODO*///	driver_gbusters,	/* GX878 (c) 1988 */
                /*TODO*///	driver_crazycop,	/* GX878 (c) 1988 (Japan) */
                /*TODO*///	driver_crimfght,	/* GX821 (c) 1989 (US) */
                /*TODO*///	driver_crimfgt2,	/* GX821 (c) 1989 (World) */
                /*TODO*///	driver_crimfgtj,	/* GX821 (c) 1989 (Japan) */
                /*TODO*///	driver_spy,		/* GX857 (c) 1989 (US) */
                /*TODO*///	driver_bottom9,	/* GX891 (c) 1989 */
                /*TODO*///	driver_bottom9n,	/* GX891 (c) 1989 */
                /*TODO*///	driver_blockhl,	/* GX973 (c) 1989 */
                /*TODO*///	driver_quarth,	/* GX973 (c) 1989 (Japan) */
                /*TODO*///	driver_aliens,	/* GX875 (c) 1990 (World) */
                /*TODO*///	driver_aliens2,	/* GX875 (c) 1990 (World) */
                /*TODO*///	driver_aliensu,	/* GX875 (c) 1990 (US) */
                /*TODO*///	driver_aliensj,	/* GX875 (c) 1990 (Japan) */
                /*TODO*///	driver_surpratk,	/* GX911 (c) 1990 (Japan) */
                /*TODO*///	driver_parodius,	/* GX955 (c) 1990 (Japan) */
                /*TODO*///	driver_rollerg,	/* GX999 (c) 1991 (US) */
                /*TODO*///	driver_rollergj,	/* GX999 (c) 1991 (Japan) */
                /*TODO*////*TESTDRIVER*/driver_xexex,		/* GX067 (c) 1991 */
                /*TODO*///	driver_simpsons,	/* GX072 (c) 1991 */
                /*TODO*///	driver_simpsn2p,	/* GX072 (c) 1991 */
                /*TODO*///	driver_simps2pj,	/* GX072 (c) 1991 (Japan) */
                /*TODO*///	driver_vendetta,	/* GX081 (c) 1991 (Asia) */
                /*TODO*///	driver_vendett2,	/* GX081 (c) 1991 (Asia) */
                /*TODO*///	driver_vendettj,	/* GX081 (c) 1991 (Japan) */
                /*TODO*///	driver_wecleman,	/* GX602 (c) 1986 */
                /*TODO*///	driver_hotchase,	/* GX763 (c) 1988 */
                /*TODO*///	driver_ultraman,	/* GX910 (c) 1991 Banpresto/Bandai */
                /*TODO*///
                /*TODO*///	/* Konami "Nemesis hardware" games */
                /*TODO*///	driver_nemesis,	/* GX456 (c) 1985 */
                /*TODO*///	driver_nemesuk,	/* GX456 (c) 1985 */
                /*TODO*///	driver_konamigt,	/* GX561 (c) 1985 */
                /*TODO*///	driver_salamand,	/* GX587 (c) 1986 */
                /*TODO*///	driver_lifefrce,	/* GX587 (c) 1986 */
                /*TODO*///	driver_lifefrcj,	/* GX587 (c) 1986 */
                /*TODO*///	/* GX400 BIOS based games */
                /*TODO*///	driver_rf2,		/* GX561 (c) 1985 */
                /*TODO*///	driver_twinbee,	/* GX412 (c) 1985 */
                /*TODO*///	driver_gradius,	/* GX456 (c) 1985 */
                /*TODO*///	driver_gwarrior,	/* GX578 (c) 1985 */
                /*TODO*///
                /*TODO*///	/* Konami "Twin 16" games */
                /*TODO*///	driver_devilw,	/* GX687 (c) 1987 */
                /*TODO*///	driver_darkadv,	/* GX687 (c) 1987 */
                /*TODO*///	driver_majuu,		/* GX687 (c) 1987 (Japan) */
                /*TODO*///	driver_vulcan,	/* GX785 (c) 1988 */
                /*TODO*///	driver_gradius2,	/* GX785 (c) 1988 (Japan) */
                /*TODO*///	driver_grdius2a,	/* GX785 (c) 1988 (Japan) */
                /*TODO*///	driver_grdius2b,	/* GX785 (c) 1988 (Japan) */
                /*TODO*///	driver_cuebrick,	/* GX903 (c) 1989 */
                /*TODO*///	driver_fround,	/* GX870 (c) 1988 */
                /*TODO*///	driver_hpuncher,	/* GX870 (c) 1988 (Japan) */
                /*TODO*///	driver_miaj,		/* GX808 (c) 1989 (Japan) */
                /*TODO*///
                /*TODO*///	/* Konami Gradius III board */
                /*TODO*///	driver_gradius3,	/* GX945 (c) 1989 (Japan) */
                /*TODO*///	driver_grdius3a,	/* GX945 (c) 1989 (Asia) */
                /*TODO*///
                /*TODO*///	/* (some) Konami 68000 games */
                /*TODO*///	driver_mia,		/* GX808 (c) 1989 */
                /*TODO*///	driver_mia2,		/* GX808 (c) 1989 */
                /*TODO*///	driver_tmnt,		/* GX963 (c) 1989 (US) */
                /*TODO*///	driver_tmht,		/* GX963 (c) 1989 (UK) */
                /*TODO*///	driver_tmntj,		/* GX963 (c) 1989 (Japan) */
                /*TODO*///	driver_tmht2p,	/* GX963 (c) 1989 (UK) */
                /*TODO*///	driver_tmnt2pj,	/* GX963 (c) 1990 (Japan) */
                /*TODO*///	driver_punkshot,	/* GX907 (c) 1990 (US) */
                /*TODO*///	driver_punksht2,	/* GX907 (c) 1990 (US) */
                /*TODO*///	driver_lgtnfght,	/* GX939 (c) 1990 (US) */
                /*TODO*///	driver_trigon,	/* GX939 (c) 1990 (Japan) */
                /*TODO*///	driver_blswhstl,	/* GX060 (c) 1991 */
                /*TODO*///	driver_detatwin,	/* GX060 (c) 1991 (Japan) */
                /*TODO*////*TESTDRIVER*/driver_glfgreat,	/* GX061 (c) 1991 */
                /*TODO*///	driver_tmnt2,		/* GX063 (c) 1991 (US) */
                /*TODO*///	driver_tmnt22p,	/* GX063 (c) 1991 (US) */
                /*TODO*///	driver_tmnt2a,	/* GX063 (c) 1991 (Asia) */
                /*TODO*///	driver_ssriders,	/* GX064 (c) 1991 (World) */
                /*TODO*///	driver_ssrdrebd,	/* GX064 (c) 1991 (World) */
                /*TODO*///	driver_ssrdrebc,	/* GX064 (c) 1991 (World) */
                /*TODO*///	driver_ssrdruda,	/* GX064 (c) 1991 (US) */
                /*TODO*///	driver_ssrdruac,	/* GX064 (c) 1991 (US) */
                /*TODO*///	driver_ssrdrubc,	/* GX064 (c) 1991 (US) */
                /*TODO*///	driver_ssrdrabd,	/* GX064 (c) 1991 (Asia) */
                /*TODO*///	driver_ssrdrjbd,	/* GX064 (c) 1991 (Japan) */
                /*TODO*///	driver_xmen,		/* GX065 (c) 1992 (US) */
                /*TODO*///	driver_xmen6p,	/* GX065 (c) 1992 */
                /*TODO*///	driver_xmen2pj,	/* GX065 (c) 1992 (Japan) */
                /*TODO*///	driver_thndrx2,	/* GX073 (c) 1991 (Japan) */
                /*TODO*///
                /*TODO*////*
                /*TODO*///Konami System GX game list
                /*TODO*///1994.03 Racing Force (GX250)
                /*TODO*///1994.03 Golfing Greats 2 (GX218)
                /*TODO*///1994.04 Gokujou Parodius (GX321)
                /*TODO*///1994.07 Taisen Puzzle-dama (GX315)
                /*TODO*///1994.12 Soccer Super Stars (GX427)
                /*TODO*///1995.04 TwinBee Yahhoo! (GX424)
                /*TODO*///1995.08 Dragoon Might (GX417)
                /*TODO*///1995.12 Tokimeki Memorial Taisen Puzzle-dama (GX515)
                /*TODO*///1996.01 Salamander 2 (GX521)
                /*TODO*///1996.02 Sexy Parodius (GX533)
                /*TODO*///1996.03 Daisu-Kiss (GX535)
                /*TODO*///1996.03 Slam Dunk 2 / Run & Gun 2 (GX505)
                /*TODO*///1996.10 Taisen Tokkae-dama (GX615)
                /*TODO*///1996.12 Versus Net Soccer (GX627)
                /*TODO*///1997.07 Winning Spike (GX705)
                /*TODO*///1997.11 Rushing Heroes (GX?. Not released in Japan)
                /*TODO*///*/
                /*TODO*///
                /*TODO*///	/* Exidy games */
                /*TODO*///	driver_sidetrac,	/* (c) 1979 */
                /*TODO*///	driver_targ,		/* (c) 1980 */
                /*TODO*///	driver_spectar,	/* (c) 1980 */
                /*TODO*///	driver_spectar1,	/* (c) 1980 */
                /*TODO*///	driver_venture,	/* (c) 1981 */
                /*TODO*///	driver_venture2,	/* (c) 1981 */
                /*TODO*///	driver_venture4,	/* (c) 1981 */
                /*TODO*///	driver_mtrap,		/* (c) 1981 */
                /*TODO*///	driver_mtrap3,	/* (c) 1981 */
                /*TODO*///	driver_mtrap4,	/* (c) 1981 */
                /*TODO*///	driver_pepper2,	/* (c) 1982 */
                /*TODO*///	driver_hardhat,	/* (c) 1982 */
                /*TODO*///	driver_fax,		/* (c) 1983 */
                /*TODO*///	driver_circus,	/* no copyright notice [1977?] */
                /*TODO*///	driver_robotbwl,	/* no copyright notice */
                /*TODO*///	driver_crash,		/* Exidy [1979?] */
                /*TODO*///	driver_ripcord,	/* Exidy [1977?] */
                /*TODO*///	driver_starfire,	/* Exidy [1979?] */
                /*TODO*///	driver_fireone,	/* (c) 1979 Exidy */
                /*TODO*///
                /*TODO*///	/* Exidy 440 games */
                /*TODO*///	driver_crossbow,	/* (c) 1983 */
                /*TODO*///	driver_cheyenne,	/* (c) 1984 */
                /*TODO*///	driver_combat,	/* (c) 1985 */
                /*TODO*///	driver_cracksht,	/* (c) 1985 */
                /*TODO*///	driver_claypign,	/* (c) 1986 */
                /*TODO*///	driver_chiller,	/* (c) 1986 */
                /*TODO*///	driver_topsecex,	/* (c) 1986 */
                /*TODO*///	driver_hitnmiss,	/* (c) 1987 */
                /*TODO*///	driver_hitnmis2,	/* (c) 1987 */
                /*TODO*///	driver_whodunit,	/* (c) 1988 */
                /*TODO*///	driver_showdown,	/* (c) 1988 */
                /*TODO*///
                /*TODO*///	/* Atari vector games */
                /*TODO*///	driver_asteroid,	/* (c) 1979 */
                /*TODO*///	driver_asteroi1,	/* no copyright notice */
                /*TODO*///	driver_asteroib,	/* bootleg */
                /*TODO*///	driver_astdelux,	/* (c) 1980 */
                /*TODO*///	driver_astdelu1,	/* (c) 1980 */
                /*TODO*///	driver_bwidow,	/* (c) 1982 */
                /*TODO*///	driver_bzone,		/* (c) 1980 */
                /*TODO*///	driver_bzone2,	/* (c) 1980 */
                /*TODO*///	driver_gravitar,	/* (c) 1982 */
                /*TODO*///	driver_gravitr2,	/* (c) 1982 */
                /*TODO*///	driver_llander,	/* no copyright notice */
                /*TODO*///	driver_llander1,	/* no copyright notice */
                /*TODO*///	driver_redbaron,	/* (c) 1980 */
                /*TODO*///	driver_spacduel,	/* (c) 1980 */
                /*TODO*///	driver_tempest,	/* (c) 1980 */
                /*TODO*///	driver_tempest1,	/* (c) 1980 */
                /*TODO*///	driver_tempest2,	/* (c) 1980 */
                /*TODO*///	driver_temptube,	/* hack */
                /*TODO*///	driver_starwars,	/* (c) 1983 */
                /*TODO*///	driver_starwar1,	/* (c) 1983 */
                /*TODO*///	driver_esb,		/* (c) 1985 */
                /*TODO*///	driver_mhavoc,	/* (c) 1983 */
                /*TODO*///	driver_mhavoc2,	/* (c) 1983 */
                /*TODO*///	driver_mhavocp,	/* (c) 1983 */
                /*TODO*///	driver_mhavocrv,	/* hack */
                /*TODO*///	driver_quantum,	/* (c) 1982 */	/* made by Gencomp */
                /*TODO*///	driver_quantum1,	/* (c) 1982 */	/* made by Gencomp */
                /*TODO*///	driver_quantump,	/* (c) 1982 */	/* made by Gencomp */
                /*TODO*///
                /*TODO*///	/* Atari b/w games */
                /*TODO*///	driver_sprint1,	/* no copyright notice */
                /*TODO*///	driver_sprint2,	/* no copyright notice */
                /*TODO*///	driver_sbrkout,	/* no copyright notice */
                /*TODO*///	driver_dominos,	/* no copyright notice */
                /*TODO*///	driver_nitedrvr,	/* no copyright notice [1976] */
                /*TODO*///	driver_bsktball,	/* no copyright notice */
                /*TODO*///	driver_copsnrob,	/* [1976] */
                /*TODO*///	driver_avalnche,	/* no copyright notice [1978] */
                /*TODO*///	driver_subs,		/* no copyright notice [1976] */
                /*TODO*///	driver_atarifb,	/* no copyright notice [1978] */
                /*TODO*///	driver_atarifb1,	/* no copyright notice [1978] */
                /*TODO*///	driver_atarifb4,	/* no copyright notice [1979] */
                /*TODO*///	driver_abaseb,	/* no copyright notice [1979] */
                /*TODO*///	driver_abaseb2,	/* no copyright notice [1979] */
                /*TODO*///	driver_soccer,	/* no copyright notice */
                /*TODO*///	driver_canyon,	/* no copyright notice [1977] */
                /*TODO*///	driver_canbprot,	/* no copyright notice [1977] */
                /*TODO*///	driver_skydiver,	/* no copyright notice [1977] */
                /*TODO*///
                /*TODO*///	/* Atari "Centipede hardware" games */
                /*TODO*///	driver_warlord,	/* (c) 1980 */
                /*TODO*///	driver_centiped,	/* (c) 1980 */
                /*TODO*///	driver_centipd2,	/* (c) 1980 */
                /*TODO*///	driver_centipdb,	/* bootleg */
                /*TODO*///	driver_centipb2,	/* bootleg */
                /*TODO*///	driver_milliped,	/* (c) 1982 */
                /*TODO*///	driver_qwakprot,	/* (c) 1982 */
                /*TODO*///
                /* "Kangaroo hardware" games */
                /*TESTDRIVER*///driver_fnkyfish, /* (c) 1981 Sun Electronics */
                driver_kangaroo, /* (c) 1982 Sun Electronics */
                driver_kangaroa, /* (c) 1982 Atari */
                driver_kangarob, /* bootleg */
                /*TODO*///	driver_arabian,	/* (c) 1983 Sun Electronics */
                /*TODO*///	driver_arabiana,	/* (c) 1983 Atari */
                /*TODO*///
                /*TODO*///	/* Atari "Missile Command hardware" games */
                /*TODO*///	driver_missile,	/* (c) 1980 */
                /*TODO*///	driver_missile2,	/* (c) 1980 */
                /*TODO*///	driver_suprmatk,	/* (c) 1980 + (c) 1981 Gencomp */
                /*TODO*///
                /*TODO*///	/* misc Atari games */
                /*TODO*///	driver_foodf,		/* (c) 1982 */	/* made by Gencomp */
                /*TODO*///	driver_liberatr,	/* (c) 1982 */
                /*TODO*////*TESTDRIVER*/driver_liberat2,
                /*TODO*///	driver_ccastles,	/* (c) 1983 */
                /*TODO*///	driver_ccastle2,	/* (c) 1983 */
                /*TODO*///	driver_cloak,		/* (c) 1983 */
                /*TODO*///	driver_cloud9,	/* (c) 1983 */
                /*TODO*///	driver_jedi,		/* (c) 1984 */
                /*TODO*///
                /*TODO*///	/* Atari System 1 games */
                /*TODO*///	driver_marble,	/* (c) 1984 */
                /*TODO*///	driver_marble2,	/* (c) 1984 */
                /*TODO*///	driver_marblea,	/* (c) 1984 */
                /*TODO*///	driver_peterpak,	/* (c) 1984 */
                /*TODO*///	driver_indytemp,	/* (c) 1985 */
                /*TODO*///	driver_indytem2,	/* (c) 1985 */
                /*TODO*///	driver_indytem3,	/* (c) 1985 */
                /*TODO*///	driver_indytem4,	/* (c) 1985 */
                /*TODO*///	driver_roadrunn,	/* (c) 1985 */
                /*TODO*///	driver_roadblst,	/* (c) 1986, 1987 */
                /*TODO*///
                /*TODO*///	/* Atari System 2 games */
                /*TODO*///	driver_paperboy,	/* (c) 1984 */
                /*TODO*///	driver_ssprint,	/* (c) 1986 */
                /*TODO*///	driver_csprint,	/* (c) 1986 */
                /*TODO*///	driver_720,		/* (c) 1986 */
                /*TODO*///	driver_720b,		/* (c) 1986 */
                /*TODO*///	driver_apb,		/* (c) 1987 */
                /*TODO*///	driver_apb2,		/* (c) 1987 */
                /*TODO*///
                /*TODO*///	/* later Atari games */
                /*TODO*///	driver_gauntlet,	/* (c) 1985 */
                /*TODO*///	driver_gauntir1,	/* (c) 1985 */
                /*TODO*///	driver_gauntir2,	/* (c) 1985 */
                /*TODO*///	driver_gaunt2p,	/* (c) 1985 */
                /*TODO*///	driver_gaunt2,	/* (c) 1986 */
                /*TODO*///	driver_vindctr2,	/* (c) 1988 */
                /*TODO*///	driver_atetris,	/* (c) 1988 */
                /*TODO*///	driver_atetrisa,	/* (c) 1988 */
                /*TODO*///	driver_atetrisb,	/* bootleg */
                /*TODO*///	driver_atetcktl,	/* (c) 1989 */
                /*TODO*///	driver_atetckt2,	/* (c) 1989 */
                /*TODO*///	driver_toobin,	/* (c) 1988 */
                /*TODO*///	driver_toobin2,	/* (c) 1988 */
                /*TODO*///	driver_toobinp,	/* (c) 1988 */
                /*TODO*///	driver_vindictr,	/* (c) 1988 */
                /*TODO*///	driver_klax,		/* (c) 1989 */
                /*TODO*///	driver_klax2,		/* (c) 1989 */
                /*TODO*///	driver_klax3,		/* (c) 1989 */
                /*TODO*///	driver_klaxj,		/* (c) 1989 (Japan) */
                /*TODO*///	driver_blstroid,	/* (c) 1987 */
                /*TODO*///	driver_blstroi2,	/* (c) 1987 */
                /*TODO*///	driver_xybots,	/* (c) 1987 */
                /*TODO*///	driver_eprom,		/* (c) 1989 */
                /*TODO*///	driver_eprom2,	/* (c) 1989 */
                /*TODO*///	driver_skullxbo,	/* (c) 1989 */
                /*TODO*///	driver_skullxb2,	/* (c) 1989 */
                /*TODO*///	driver_badlands,	/* (c) 1989 */
                /*TODO*///	driver_cyberbal,	/* (c) 1989 */
                /*TODO*///	driver_cyberbt,	/* (c) 1989 */
                /*TODO*///	driver_cyberb2p,	/* (c) 1989 */
                /*TODO*///	driver_rampart,	/* (c) 1990 */
                /*TODO*///	driver_ramprt2p,	/* (c) 1990 */
                /*TODO*///	driver_rampartj,	/* (c) 1990 (Japan) */
                /*TODO*///	driver_shuuz,		/* (c) 1990 */
                /*TODO*///	driver_shuuz2,	/* (c) 1990 */
                /*TODO*///	driver_hydra,		/* (c) 1990 */
                /*TODO*///	driver_hydrap,	/* (c) 1990 */
                /*TODO*///	driver_pitfight,	/* (c) 1990 */
                /*TODO*///	driver_pitfigh3,	/* (c) 1990 */
                /*TODO*///	driver_thunderj,	/* (c) 1990 */
                /*TODO*///	driver_batman,	/* (c) 1991 */
                /*TODO*///	driver_relief,	/* (c) 1992 */
                /*TODO*///	driver_relief2,	/* (c) 1992 */
                /*TODO*///	driver_offtwall,	/* (c) 1991 */
                /*TODO*///	driver_offtwalc,	/* (c) 1991 */
                /*TODO*///	driver_arcadecl,	/* (c) 1992 */
                /*TODO*///	driver_sparkz,	/* (c) 1992 */
                /*TODO*///
                /*TODO*///	/* SNK / Rock-ola games */
                /*TODO*///	driver_sasuke,	/* [1980] Shin Nihon Kikaku (SNK) */
                /*TODO*///	driver_satansat,	/* (c) 1981 SNK */
                /*TODO*///	driver_zarzon,	/* (c) 1981 Taito, gameplay says SNK */
                /*TODO*///	driver_vanguard,	/* (c) 1981 SNK */
                /*TODO*///	driver_vangrdce,	/* (c) 1981 SNK + Centuri */
                /*TODO*///	driver_fantasy,	/* (c) 1981 Rock-ola */
                /*TODO*///	driver_fantasyj,	/* (c) 1981 SNK */
                /*TODO*///	driver_pballoon,	/* (c) 1982 SNK */
                /*TODO*///	driver_nibbler,	/* (c) 1982 Rock-ola */
                /*TODO*///	driver_nibblera,	/* (c) 1982 Rock-ola */
                /*TODO*///
                /*TODO*///	/* later SNK games, each game can be identified by PCB code and ROM
                /*TODO*///	code, the ROM code is the same between versions, and usually based
                /*TODO*///	upon the Japanese title. */
                /*TODO*///	driver_lasso,		/*       'WM' (c) 1982 */
                /*TODO*///	driver_joyfulr,	/* A2001      (c) 1983 */
                /*TODO*///	driver_mnchmobl,	/* A2001      (c) 1983 + Centuri license */
                /*TODO*///	driver_marvins,	/* A2003      (c) 1983 */
                /*TODO*///	driver_madcrash,	/* A2005      (c) 1984 */
                /*TODO*///	driver_vangrd2,	/*            (c) 1984 */
                /*TODO*///	driver_hal21,		/*            (c) 1985 */
                /*TODO*///	driver_hal21j,	/*            (c) 1985 (Japan) */
                /*TODO*///	driver_aso,		/*            (c) 1985 */
                /*TODO*///	driver_tnk3,		/* A5001      (c) 1985 */
                /*TODO*///	driver_tnk3j,		/* A5001      (c) 1985 */
                /*TODO*///	driver_athena,	/*       'UP' (c) 1986 */
                /*TODO*///	driver_fitegolf,	/*       'GU' (c) 1988 */
                /*TODO*///	driver_ikari,		/* A5004 'IW' (c) 1986 */
                /*TODO*///	driver_ikarijp,	/* A5004 'IW' (c) 1986 (Japan) */
                /*TODO*///	driver_ikarijpb,	/* bootleg */
                /*TODO*///	driver_victroad,	/*            (c) 1986 */
                /*TODO*///	driver_dogosoke,	/*            (c) 1986 */
                /*TODO*///	driver_gwar,		/* A7003 'GV' (c) 1987 */
                /*TODO*///	driver_gwarj,		/* A7003 'GV' (c) 1987 (Japan) */
                /*TODO*///	driver_gwara,		/* A7003 'GV' (c) 1987 */
                /*TODO*///	driver_gwarb,		/* bootleg */
                /*TODO*///	driver_bermudat,	/* A6003 'WW' (c) 1987 */
                /*TODO*///	driver_bermudaj,	/* A6003 'WW' (c) 1987 */
                /*TODO*///	driver_bermudaa,	/* A6003 'WW' (c) 1987 */
                /*TODO*///	driver_worldwar,	/* A6003 'WW' (c) 1987 */
                /*TODO*///	driver_psychos,	/*       'PS' (c) 1987 */
                /*TODO*///	driver_psychosj,	/*       'PS' (c) 1987 (Japan) */
                /*TODO*///	driver_chopper,	/* A7003 'KK' (c) 1988 */
                /*TODO*///	driver_legofair,	/* A7003 'KK' (c) 1988 */
                /*TODO*///	driver_ftsoccer,	/*            (c) 1988 */
                /*TODO*///	driver_tdfever,	/* A6006 'TD' (c) 1987 */
                /*TODO*///	driver_tdfeverj,	/* A6006 'TD' (c) 1987 */
                /*TODO*///	driver_ikari3,	/* A7007 'IK3'(c) 1989 */
                /*TODO*///	driver_pow,		/* A7008 'DG' (c) 1988 */
                /*TODO*///	driver_powj,		/* A7008 'DG' (c) 1988 */
                /*TODO*///	driver_searchar,	/* A8007 'BH' (c) 1989 */
                /*TODO*///	driver_sercharu,	/* A8007 'BH' (c) 1989 */
                /*TODO*///	driver_streetsm,	/* A8007 'S2' (c) 1989 */
                /*TODO*///	driver_streets1,	/* A7008 'S2' (c) 1989 */
                /*TODO*///	driver_streetsj,	/* A8007 'S2' (c) 1989 */
                /*TODO*///	/* Mechanized Attack   A8002 'MA' (c) 1989 */
                /*TODO*///	driver_prehisle,	/* A8003 'GT' (c) 1989 */
                /*TODO*///	driver_prehislu,	/* A8003 'GT' (c) 1989 */
                /*TODO*///	driver_gensitou,	/* A8003 'GT' (c) 1989 */
                /*TODO*///	/* Beast Busters       A9003 'BB' (c) 1989 */
                /*TODO*///
                /*TODO*///	/* SNK / Alpha 68K games */
                /*TODO*////*TESTDRIVER*/driver_kyros,
                /*TODO*////*TESTDRIVER*/driver_sstingry,
                /*TODO*////*TESTDRIVER*/driver_paddlema,	/* Alpha-68K96I  'PM' (c) 1988 SNK */
                /*TODO*///	driver_timesold,	/* Alpha-68K96II 'BT' (c) 1987 SNK / Romstar */
                /*TODO*///	driver_timesol1,  /* Alpha-68K96II 'BT' (c) 1987 */
                /*TODO*///	driver_btlfield,  /* Alpha-68K96II 'BT' (c) 1987 */
                /*TODO*///	driver_skysoldr,	/* Alpha-68K96II 'SS' (c) 1988 SNK (Romstar with dip switch) */
                /*TODO*///	driver_goldmedl,	/* Alpha-68K96II 'GM' (c) 1988 SNK */
                /*TODO*////*TESTDRIVER*/driver_goldmedb,	/* Alpha-68K96II bootleg */
                /*TODO*///	driver_skyadvnt,	/* Alpha-68K96V  'SA' (c) 1989 SNK of America licensed from Alpha */
                /*TODO*///	driver_gangwars,	/* Alpha-68K96V       (c) 1989 Alpha */
                /*TODO*///	driver_gangwarb,	/* Alpha-68K96V bootleg */
                /*TODO*///	driver_sbasebal,	/* Alpha-68K96V       (c) 1989 SNK of America licensed from Alpha */
                /*TODO*///
                /*TODO*///	/* Technos games */
                /*TODO*///	driver_scregg,	/* TA-0001 (c) 1983 */
                /*TODO*///	driver_eggs,		/* TA-0002 (c) 1983 Universal USA */
                /*TODO*///	driver_bigprowr,	/* TA-0007 (c) 1983 */
                /*TODO*///	driver_tagteam,	/* TA-0007 (c) 1983 + Data East license */
                /*TODO*///	driver_ssozumo,	/* TA-0008 (c) 1984 */
                /*TODO*///	driver_mystston,	/* TA-0010 (c) 1984 */
                /*TODO*///	/* TA-0011 Dog Fight (Data East) / Batten O'hara no Sucha-Raka Kuuchuu Sen 1985 */
                /*TODO*///	driver_bogeyman,	/* X-0204-0 (Data East part number) (c) [1985?] */
                /*TODO*///	driver_matmania,	/* TA-0015 (c) 1985 + Taito America license */
                /*TODO*///	driver_excthour,	/* TA-0015 (c) 1985 + Taito license */
                /*TODO*///	driver_maniach,	/* TA-0017 (c) 1986 + Taito America license */
                /*TODO*///	driver_maniach2,	/* TA-0017 (c) 1986 + Taito America license */
                /*TODO*///	driver_renegade,	/* TA-0018 (c) 1986 + Taito America license */
                /*TODO*///	driver_kuniokun,	/* TA-0018 (c) 1986 */
                /*TODO*///	driver_kuniokub,	/* bootleg */
                /*TODO*///	driver_xsleena,	/* TA-0019 (c) 1986 */
                /*TODO*///	driver_xsleenab,	/* bootleg */
                /*TODO*///	driver_solarwar,	/* TA-0019 (c) 1986 Taito + Memetron license */
                /*TODO*///	driver_battlane,	/* TA-???? (c) 1986 + Taito license */
                /*TODO*///	driver_battlan2,	/* TA-???? (c) 1986 + Taito license */
                /*TODO*///	driver_battlan3,	/* TA-???? (c) 1986 + Taito license */
                /*TODO*///	driver_ddragon,
                /*TODO*///	driver_ddragonb,	/* TA-0021 bootleg */
                /*TODO*///	/* TA-0022 Super Dodge Ball */
                /*TODO*///	/* TA-0023 China Gate */
                /*TODO*///	/* TA-0024 WWF Superstars */
                /*TODO*///	/* TA-0025 Champ V'Ball */
                /*TODO*///	driver_ddragon2,	/* TA-0026 (c) 1988 */
                /*TODO*///	/* TA-0027 ???? */
                /*TODO*///	driver_ctribe,	/* TA-0028 (c) 1990 (US) */
                /*TODO*///	driver_ctribeb,	/* bootleg */
                /*TODO*///	driver_blockout,	/* TA-0029 (c) 1989 + California Dreams */
                /*TODO*///	driver_blckout2,	/* TA-0029 (c) 1989 + California Dreams */
                /*TODO*///	driver_ddragon3,	/* TA-0030 (c) 1990 */
                /*TODO*///	driver_ddrago3b,	/* bootleg */
                /*TODO*///	/* TA-0031 WWF Wrestlefest */

                /* Stern "Berzerk hardware" games */
                driver_berzerk, /* (c) 1980 */
                driver_berzerk1, /* (c) 1980 */
                driver_frenzy, /* (c) 1982 */
                /*TODO*///
                /*TODO*///	/* GamePlan games */
                /*TODO*///	driver_megatack,	/* (c) 1980 Centuri */
                /*TODO*///	driver_killcom,	/* (c) 1980 Centuri */
                /*TODO*///	driver_challeng,	/* (c) 1981 Centuri */
                /*TODO*///	driver_kaos,		/* (c) 1981 */
                /*TODO*///
                /*TODO*///	/* "stratovox hardware" games */
                /*TODO*///	driver_route16,	/* (c) 1981 Tehkan/Sun + Centuri license */
                /*TODO*///	driver_route16b,	/* bootleg */
                /*TODO*///	driver_stratvox,	/* Taito */
                /*TODO*///	driver_stratvxb,	/* bootleg */
                /*TODO*///	driver_speakres,	/* no copyright notice */
                /*TODO*///
                /*TODO*///	/* Zaccaria games */
                /*TODO*///	driver_monymony,	/* (c) 1983 */
                /*TODO*///	driver_jackrabt,	/* (c) 1984 */
                /*TODO*///	driver_jackrab2,	/* (c) 1984 */
                /*TODO*///	driver_jackrabs,	/* (c) 1984 */
                /*TODO*///
                /* UPL games */
                driver_nova2001, /* UPL-83005 (c) 1983 */
                driver_nov2001u, /* UPL-83005 (c) [1983] + Universal license */
                driver_pkunwar, /* [1985?] */
                driver_pkunwarj, /* [1985?] */
                driver_ninjakd2, /* (c) 1987 */
                driver_ninjak2a, /* (c) 1987 */
                driver_ninjak2b, /* (c) 1987 */
                driver_rdaction, /* (c) 1987 + World Games license */
                /*TODO*///	driver_mnight,	/* (c) 1987 distributed by Kawakus */
                /*TODO*///	driver_arkarea,	/* UPL-87007 (c) [1988?] */
                /*TODO*////*
                /*TODO*///Urashima Mahjong    UPL-89052
                /*TODO*///
                /*TODO*///UPL Game List
                /*TODO*///V1.2   May 27,1999
                /*TODO*///
                /*TODO*///   83 Mouser                              Kit 2P              Action   83001
                /*TODO*/// 3/84 Nova 2001                 Universal Kit 2P  8W+2B   HC  Shooter  85005
                /*TODO*///   84 Penguin Wars (Kun)                      2P              Action
                /*TODO*///   84 Ninja Kun                 Taito                                  85003
                /*TODO*///   85 Raiders 5                 Taito                                  85004
                /*TODO*/// 8/87 Mission XX                          Kit 2P  8W+2B   VC  Shooter  86001
                /*TODO*///   87 Mutant Night                        Kit 2P  8W+2B   HC  Action
                /*TODO*/// 7/87 Rad Action/Ninja Taro   World Games Kit 2P  8W+2B   HC  Action   87003
                /*TODO*/// 7/87 Ninja Taro/Rad Action   World Games Kit 2P  8W+2B   HC  Action
                /*TODO*///   87 Ninja Taro II                       Kit 2P  8W+2B   HC  Action
                /*TODO*///   88 Aquaria                             Kit 2P  8W+2B
                /*TODO*///   89 Ochichi Mahjong                     Kit 2P  8W+2B   HC  Mahjong
                /*TODO*/// 9/89 Omega Fighter        American Sammy Kit 2P  8W+2B   HC  Shooter  89016
                /*TODO*///12/89 Task Force Harrier   American Sammy Kit 2P  8W+2B   VC  Shooter  89053
                /*TODO*///   90 Atomic Robo-Kid      American Sammy Kit 2P  8W+2B   HC  Shooter  88013
                /*TODO*///   90 Mustang - U.S.A.A.F./Fire Mustang   Kit 2P  8W+2B   HC  Shooter  90058
                /*TODO*///   91 Acrobat Mission               Taito Kit 2P  8W+2B   VC  Shooter
                /*TODO*///   91 Bio Ship Paladin/Spaceship Gomera   Kit 2P  8W+2B   HC  Shooter  90062
                /*TODO*///   91 Black Heart                         Kit 2P  8W+2B   HC  Shooter
                /*TODO*///   91 Van Dyke Fantasy                    Kit 2P  8W+2B
                /*TODO*/// 2/92 Strahl                              Kit 2P  8W+3B                91074
                /*TODO*///      Thunder Dragon 2                                                 93091
                /*TODO*///
                /*TODO*///*/
                /*TODO*///
                /*TODO*///	/* Williams/Midway TMS34010 games */
                /*TODO*///	driver_narc,		/* (c) 1988 Williams */
                /*TODO*////*TESTDRIVER*/driver_narc3,		/* (c) 1988 Williams */
                /*TODO*///	driver_trog,		/* (c) 1990 Midway */
                /*TODO*///	driver_trog3,		/* (c) 1990 Midway */
                /*TODO*///	driver_trogp,		/* (c) 1990 Midway */
                /*TODO*///	driver_smashtv,	/* (c) 1990 Williams */
                /*TODO*///	driver_smashtv6,	/* (c) 1990 Williams */
                /*TODO*///	driver_smashtv5,	/* (c) 1990 Williams */
                /*TODO*///	driver_smashtv4,	/* (c) 1990 Williams */
                /*TODO*///	driver_hiimpact,	/* (c) 1990 Williams */
                /*TODO*///	driver_shimpact,	/* (c) 1991 Midway */
                /*TODO*///	driver_strkforc,	/* (c) 1991 Midway */
                /*TODO*///	driver_mk,		/* (c) 1992 Midway */
                /*TODO*///	driver_mkla1,		/* (c) 1992 Midway */
                /*TODO*///	driver_mkla2,		/* (c) 1992 Midway */
                /*TODO*///	driver_mkla3,		/* (c) 1992 Midway */
                /*TODO*///	driver_mkla4,		/* (c) 1992 Midway */
                /*TODO*///	driver_term2,		/* (c) 1992 Midway */
                /*TODO*///	driver_totcarn,	/* (c) 1992 Midway */
                /*TODO*///	driver_totcarnp,	/* (c) 1992 Midway */
                /*TODO*///	driver_mk2,		/* (c) 1993 Midway */
                /*TODO*///	driver_mk2r32,	/* (c) 1993 Midway */
                /*TODO*///	driver_mk2r14,	/* (c) 1993 Midway */
                /*TODO*///	driver_nbajam,	/* (c) 1993 Midway */
                /*TODO*///	driver_nbajamr2,	/* (c) 1993 Midway */
                /*TODO*///	driver_nbajamte,	/* (c) 1994 Midway */
                /* Cinematronics raster games */
                driver_jack, /* (c) 1982 Cinematronics */
                driver_jack2, /* (c) 1982 Cinematronics */
                driver_jack3, /* (c) 1982 Cinematronics */
                driver_treahunt, /* (c) 1982 Hara Ind. */
                driver_zzyzzyxx, /* (c) 1982 Cinematronics + Advanced Microcomputer Systems */
                driver_zzyzzyx2, /* (c) 1982 Cinematronics + Advanced Microcomputer Systems */
                driver_brix, /* (c) 1982 Cinematronics + Advanced Microcomputer Systems */
                driver_freeze, /* Cinematronics */
                driver_sucasino, /* (c) 1982 Data Amusement */
                /*TODO*///	/* Cinematronics vector games */
                /*TODO*///	driver_spacewar,
                /*TODO*///	driver_barrier,
                /*TODO*///	driver_starcas,	/* (c) 1980 */
                /*TODO*///	driver_starcas1,	/* (c) 1980 */
                /*TODO*///	driver_tailg,
                /*TODO*///	driver_ripoff,
                /*TODO*///	driver_armora,
                /*TODO*///	driver_wotw,
                /*TODO*///	driver_warrior,
                /*TODO*///	driver_starhawk,
                /*TODO*///	driver_solarq,	/* (c) 1981 */
                /*TODO*///	driver_boxingb,	/* (c) 1981 */
                /*TODO*///	driver_speedfrk,
                /*TODO*///	driver_sundance,
                /*TODO*///	driver_demon,		/* (c) 1982 Rock-ola */
                /*TODO*///	/* this one uses 68000+Z80 instead of the Cinematronics CPU */
                /*TODO*///	driver_cchasm,
                /*TODO*///	driver_cchasm1,	/* (c) 1983 Cinematronics / GCE */
                /*TODO*///
                /*TODO*///	/* "The Pit hardware" games */
                /*TODO*///	driver_roundup,	/* (c) 1981 Amenip/Centuri */
                /*TODO*///	driver_fitter,	/* (c) 1981 Taito */
                /*TODO*///	driver_thepit,	/* (c) 1982 Centuri */
                /*TODO*///	driver_intrepid,	/* (c) 1983 Nova Games Ltd. */
                /*TODO*///	driver_intrepi2,	/* (c) 1983 Nova Games Ltd. */
                /*TODO*///	driver_portman,	/* (c) 1982 Nova Games Ltd. */
                /*TODO*///	driver_suprmous,	/* (c) 1982 Taito */
                /*TODO*///	driver_suprmou2,	/* (c) 1982 Chu Co. Ltd. */
                /*TODO*///	driver_machomou,	/* (c) 1982 Techstar */
                /*TODO*///
                /* Valadon Automation games */
                driver_bagman, /* (c) 1982 */
                driver_bagnard, /* (c) 1982 */
                driver_bagmans, /* (c) 1982 + Stern license */
                driver_bagmans2, /* (c) 1982 + Stern license */
                driver_sbagman, /* (c) 1984 */
                driver_sbagmans, /* (c) 1984 + Stern license */
                driver_pickin, /* (c) 1983 */
                /* Seibu Denshi / Seibu Kaihatsu games */
                driver_stinger, /* (c) 1983 Seibu Denshi */
                driver_scion, /* (c) 1984 Seibu Denshi */
                driver_scionc, /* (c) 1984 Seibu Denshi + Cinematronics license */
                driver_wiz, /* (c) 1985 Seibu Kaihatsu */
                driver_wizt, /* (c) 1985 Taito Corporation */
                driver_empcity, /* (c) 1986 Seibu Kaihatsu (bootleg?) */
                driver_empcityj, /* (c) 1986 Taito Corporation (Japan) */
                driver_stfight, /* (c) 1986 Seibu Kaihatsu (Germany) (bootleg?) */
                /*TODO*///	driver_dynduke,	/* (c) 1989 Seibu Kaihatsu + Fabtek license */
                /*TODO*///	driver_dbldyn,	/* (c) 1989 Seibu Kaihatsu + Fabtek license */
                /*TODO*///	driver_raiden,	/* (c) 1990 Seibu Kaihatsu */
                /*TODO*///	driver_raidena,	/* (c) 1990 Seibu Kaihatsu */
                /*TODO*///	driver_raidenk,	/* (c) 1990 Seibu Kaihatsu + IBL Corporation license */
                /*TODO*///	driver_dcon,		/* (c) 1992 Success */
                /*TODO*///
                /*TODO*////* Seibu STI System games:
                /*TODO*///
                /*TODO*///	Viper: Phase 1 					(c) 1995
                /*TODO*///	Viper: Phase 1 (New version)	(c) 1996
                /*TODO*///	Battle Balls					(c) 1996
                /*TODO*///	Raiden Fighters					(c) 1996
                /*TODO*///	Raiden Fighters 2 				(c) 1997
                /*TODO*///	Senku							(c) 1997
                /*TODO*///
                /*TODO*///*/
                /*TODO*///
                /*TODO*///	/* Tad games (Tad games run on Seibu hardware) */
                /*TODO*///	driver_cabal,		/* (c) 1988 Tad + Fabtek license */
                /*TODO*///	driver_cabal2,	/* (c) 1988 Tad + Fabtek license */
                /*TODO*///	driver_cabalbl,	/* bootleg */
                /*TODO*///	driver_toki,		/* (c) 1989 Tad */
                /*TODO*///	driver_toki2,		/* (c) 1989 Tad */
                /*TODO*///	driver_toki3,		/* (c) 1989 Tad */
                /*TODO*///	driver_tokiu,		/* (c) 1989 Tad + Fabtek license */
                /*TODO*///	driver_tokib,		/* bootleg */
                /*TODO*///	driver_bloodbro,	/* (c) 1990 Tad */
                /*TODO*///	driver_weststry,	/* bootleg */
                /*TODO*///
                /*TODO*///	/* Jaleco games */
                /*TODO*///	driver_exerion,	/* (c) 1983 Jaleco */
                /*TODO*///	driver_exeriont,	/* (c) 1983 Jaleco + Taito America license */
                /*TODO*///	driver_exerionb,	/* bootleg */
                /*TODO*///	driver_formatz,	/* (c) 1984 Jaleco */
                /*TODO*///	driver_aeroboto,	/* (c) 1984 Williams */
                /*TODO*///	driver_citycon,	/* (c) 1985 Jaleco */
                /*TODO*///	driver_citycona,	/* (c) 1985 Jaleco */
                /*TODO*///	driver_cruisin,	/* (c) 1985 Jaleco/Kitkorp */
                /*TODO*///	driver_pinbo,		/* (c) 1984 Jaleco */
                /*TODO*///	driver_pinbos,	/* (c) 1985 Strike */
                /*TODO*///	driver_psychic5,	/* (c) 1987 Jaleco */
                /*TODO*///	driver_ginganin,	/* (c) 1987 Jaleco */
                /*TODO*///	driver_cischeat,	/* (c) 1990 Jaleco */
                /*TODO*///	driver_f1gpstar,	/* (c) 1991 Jaleco */
                /*TODO*///
                /*TODO*///	/* Jaleco Mega System 1 games */
                /*TODO*///	driver_lomakai,	/* (c) 1988 (World) */
                /*TODO*///	driver_makaiden,	/* (c) 1988 (Japan) */
                /*TODO*///	driver_p47,		/* (c) 1988 */
                /*TODO*///	driver_p47j,		/* (c) 1988 (Japan) */
                /*TODO*///	driver_kickoff,	/* (c) 1988 (Japan) */
                /*TODO*///	driver_tshingen,	/* (c) 1988 (Japan) */
                /*TODO*///	driver_astyanax,	/* (c) 1989 */
                /*TODO*///	driver_lordofk,	/* (c) 1989 (Japan) */
                /*TODO*///	driver_hachoo,	/* (c) 1989 */
                /*TODO*///	driver_plusalph,	/* (c) 1989 */
                /*TODO*///	driver_stdragon,	/* (c) 1989 */
                /*TODO*///	driver_iganinju,	/* (c) 1989 (Japan) */
                /*TODO*///	driver_rodland,	/* (c) 1990 */
                /*TODO*///	driver_rodlandj,	/* (c) 1990 (Japan) */
                /*TODO*///	driver_64street,	/* (c) 1991 */
                /*TODO*///	driver_64streej,	/* (c) 1991 (Japan) */
                /*TODO*///	driver_edf,		/* (c) 1991 */
                /*TODO*///	driver_avspirit,	/* (c) 1991 */
                /*TODO*///	driver_phantasm,	/* (c) 1991 (Japan) */
                /*TODO*///	driver_bigstrik,	/* (c) 1992 */
                /*TODO*///	driver_chimerab,	/* (c) 1993 */
                /*TODO*///	driver_cybattlr,	/* (c) 1993 */
                /*TODO*///	driver_peekaboo,	/* (c) 1993 */
                /*TODO*///	driver_soldamj,	/* (c) 1992 (Japan) */
                /*TODO*///
                /*TODO*///	/* Video System Co. games */
                /*TODO*///	driver_pspikes,	/* (c) 1991 */
                /*TODO*///	driver_svolly91,	/* (c) 1991 */
                /*TODO*///	driver_turbofrc,	/* (c) 1991 */
                /*TODO*///	driver_aerofgt,	/* (c) 1992 */
                /*TODO*///	driver_aerofgtb,	/* (c) 1992 */
                /*TODO*///	driver_aerofgtc,	/* (c) 1992 */
                /*TODO*////*TESTDRIVER*/driver_unkvsys,
                /*TODO*///
                /* Orca games */
                driver_marineb, /* (c) 1982 Orca */
                driver_changes, /* (c) 1982 Orca */
                driver_looper, /* (c) 1982 Orca */
                driver_springer, /* (c) 1982 Orca */
                driver_hoccer, /* (c) 1983 Eastern Micro Electronics, Inc. */
                driver_hoccer2, /* (c) 1983 Eastern Micro Electronics, Inc. */
                driver_hopprobo, /* (c) 1983 Sega */
                driver_wanted, /* (c) 1984 Sigma Ent. Inc. */
                driver_funkybee, /* (c) 1982 Orca */
                driver_skylancr, /* (c) 1983 Orca + Esco Trading Co license */
                driver_zodiack, /* (c) 1983 Orca + Esco Trading Co license */
                driver_dogfight, /* (c) 1983 Thunderbolt */
                driver_moguchan, /* (c) 1982 Orca + Eastern Commerce Inc. license (doesn't appear on screen) */
                driver_percuss, /* (c) 1981 Orca */
                driver_espial, /* (c) 1983 Thunderbolt, Orca logo is hidden in title screen */
                driver_espiale, /* (c) 1983 Thunderbolt, Orca logo is hidden in title screen */
                /*TODO*///	/* Vastar was made by Orca, but when it was finished, Orca had already bankrupted. */
                /*TODO*///	/* So they sold this game as "Made by Sesame Japan" because they couldn't use */
                /*TODO*///	/* the name "Orca" */
                /*TODO*///	driver_vastar,	/* (c) 1983 Sesame Japan */
                /*TODO*///	driver_vastar2,	/* (c) 1983 Sesame Japan */
                /*TODO*////*
                /*TODO*///   other Orca games:
                /*TODO*///   82 Battle Cross                         Kit 2P
                /*TODO*///   82 River Patrol Empire Mfg/Kerstens Ind Ded 2P        HC Action
                /*TODO*///   82 Slalom                               Kit 2P        HC Action
                /*TODO*///   82 The Bounty                           Ded 2P
                /*TODO*///   83 Net Wars                                 2P
                /*TODO*///   83 Super Crush                          Kit 2P           Action
                /*TODO*///*/
                /*TODO*///
                /*TODO*///	/* Gaelco games */
                /*TODO*///	driver_splash,	/* (c) 1992 Gaelco */
                /*TODO*////*
                /*TODO*///Gaelco Game list:
                /*TODO*///=================
                /*TODO*///
                /*TODO*///1987:	Master Boy
                /*TODO*///1991:	Big Karnak, Master Boy 2
                /*TODO*///1992:	Splash, Thunder Hoop, Squash
                /*TODO*///1993:	World Rally, Glass
                /*TODO*///1994:	Strike Back, Target Hits, Thunder Hoop 2
                /*TODO*///1995:	Alligator Hunt, Toy, World Rally 2, Salter, Touch & Go
                /*TODO*///1996:	Maniac Square, Snow Board, Speed Up
                /*TODO*///1997:	Surf Planet
                /*TODO*///1998:	Radikal Bikers
                /*TODO*///1999:	Rolling Extreme
                /*TODO*///
                /*TODO*///All games newer than Splash are heavily protected.
                /*TODO*///*/
                /*TODO*///
                /*TODO*///	/* Kaneko "AX System" games */
                /*TODO*///	driver_berlwall,	/* (c) 1991 Kaneko */
                /*TODO*///	driver_berlwalt,	/* (c) 1991 Kaneko */
                /*TODO*///	driver_gtmr,		/* (c) 1994 Kaneko */
                /*TODO*///	driver_gtmre,		/* (c) 1994 Kaneko */
                /*TODO*////*TESTDRIVER*/driver_gtmr2,
                /*TODO*////*TESTDRIVER*/driver_shogwarr,
                /*TODO*///
                /*TODO*///	/* other Kaneko games */
                /*TODO*///	driver_galpanic,	/* (c) 1990 Kaneko */
                driver_airbustr, /* (c) 1990 Kaneko */
                /*TODO*///
                /*TODO*///	driver_spacefb,	/* (c) [1980?] Nintendo */
                /*TODO*///	driver_spacefbg,	/* 834-0031 (c) 1980 Gremlin */
                /*TODO*///	driver_spacefbb,	/* bootleg */
                /*TODO*///	driver_spacebrd,	/* bootleg */
                /*TODO*///	driver_spacedem,	/* (c) 1980 Nintendo / Fortrek */
                driver_blueprnt, /* (c) 1982 Bally Midway (Zilec in ROM 3U, and the programmer names) */
                driver_blueprnj, /* (c) 1982 Jaleco (Zilec in ROM 3U, and the programmer names) */
                driver_saturn, /* (c) 1983 Jaleco (Zilec in ROM R6, and the programmer names) */
                /*TODO*///	driver_omegrace,	/* (c) 1981 Midway */
                /*TODO*///	driver_dday,		/* (c) 1982 Olympia */
                /*TODO*///	driver_ddayc,		/* (c) 1982 Olympia + Centuri license */
                driver_gundealr, /* (c) 1990 Dooyong */
                driver_gundeala, /* (c) Dooyong */
                driver_yamyam, /* (c) 1990 Dooyong */
                driver_wiseguy, /* (c) 1990 Dooyong */
                /*TODO*///	driver_leprechn,	/* (c) 1982 Tong Electronic */
                /*TODO*///	driver_potogold,	/* (c) 1982 Tong Electronic */
                driver_hexa, /* D. R. Korea */
                /*TODO*///	driver_redalert,	/* (c) 1981 Irem (GDI game) */
                /*TODO*///	driver_irobot,	/* (c) 1983 Atari */
                /*TODO*///	driver_spiders,	/* (c) 1981 Sigma Ent. Inc. */
                /*TODO*///	driver_spiders2,	/* (c) 1981 Sigma Ent. Inc. */
                /*TODO*///	driver_stactics,	/* [1981 Sega] */
                /*TODO*///	driver_exterm,	/* (c) 1989 Premier Technology - a Gottlieb game */
                /*TODO*///	driver_sharkatt,	/* (c) Pacific Novelty */
                /*TODO*///	driver_kingofb,	/* (c) 1985 Woodplace Inc. */
                /*TODO*///	driver_ringking,	/* (c) 1985 Data East USA */
                /*TODO*///	driver_ringkin2,
                /*TODO*///	driver_ringkin3,	/* (c) 1985 Data East USA */
                /*TODO*///	driver_zerozone,	/* (c) 1993 Comad */
                driver_exctsccr, /* (c) 1983 Alpha Denshi Co. */
                driver_exctscca, /* (c) 1983 Alpha Denshi Co. */
                driver_exctsccb, /* bootleg */
                driver_exctscc2,
                driver_speedbal, /* (c) 1987 Tecfri */
                driver_sauro, /* (c) 1987 Tecfri */
                driver_ambush, /* (c) 1983 Nippon Amuse Co-Ltd */
                /*TODO*///	driver_starcrus,	/* [1977 Ramtek] */
                /*TODO*///	driver_shanghai,	/* (c) 1988 Sun Electronics */
                /*TODO*///	driver_goindol,	/* (c) 1987 Sun a Electronics */
                /*TODO*///	driver_homo,		/* bootleg */
                /*TODO*////*TESTDRIVER*/driver_dlair,
                /*TODO*///	driver_meteor,	/* (c) 1981 Venture Line */
                /*TODO*///	driver_bjtwin,	/* (c) 1993 NMK */
                /*TODO*///	driver_aztarac,	/* (c) 1983 Centuri (vector game) */
                /*TODO*///	driver_mole,		/* (c) 1982 Yachiyo Electronics, Ltd. */
                /*TODO*///	driver_gotya,		/* (c) 1981 Game-A-Tron */
                /*TODO*///
                /*TODO*///
                /*TODO*///#endif /* NEOMAME */
                /*TODO*///
                /*TODO*///#ifndef NEOFREE
                /*TODO*///
                /*TODO*///	/* Neo Geo games */
                /*TODO*///	/* the four digits number is the game ID stored at address 0x0108 of the program ROM */
                /*TODO*///	driver_nam1975,	/* 0001 (c) 1990 SNK */
                /*TODO*///	driver_bstars,	/* 0002 (c) 1990 SNK */
                /*TODO*///	driver_tpgolf,	/* 0003 (c) 1990 SNK */
                /*TODO*///	driver_mahretsu,	/* 0004 (c) 1990 SNK */
                /*TODO*///	driver_maglord,	/* 0005 (c) 1990 Alpha Denshi Co */
                /*TODO*///	driver_maglordh,	/* 0005 (c) 1990 Alpha Denshi Co */
                /*TODO*///	driver_ridhero,	/* 0006 (c) 1990 SNK */
                /*TODO*///	driver_alpham2,	/* 0007 (c) 1991 SNK */
                /*TODO*///	/* 0008 */
                /*TODO*///	driver_ncombat,	/* 0009 (c) 1990 Alpha Denshi Co */
                /*TODO*///	driver_cyberlip,	/* 0010 (c) 1990 SNK */
                /*TODO*///	driver_superspy,	/* 0011 (c) 1990 SNK */
                /*TODO*///	/* 0012 */
                /*TODO*///	/* 0013 */
                /*TODO*///	driver_mutnat,	/* 0014 (c) 1992 SNK */
                /*TODO*///	/* 0015 */
                /*TODO*///	driver_kotm,		/* 0016 (c) 1991 SNK */
                /*TODO*///	driver_sengoku,	/* 0017 (c) 1991 SNK */
                /*TODO*///	driver_sengokh,	/* 0017 (c) 1991 SNK */
                /*TODO*///	driver_burningf,	/* 0018 (c) 1991 SNK */
                /*TODO*///	driver_burningh,	/* 0018 (c) 1991 SNK */
                /*TODO*///	driver_lbowling,	/* 0019 (c) 1990 SNK */
                /*TODO*///	driver_gpilots,	/* 0020 (c) 1991 SNK */
                /*TODO*///	driver_joyjoy,	/* 0021 (c) 1990 SNK */
                /*TODO*///	driver_bjourney,	/* 0022 (c) 1990 Alpha Denshi Co */
                /*TODO*///	driver_quizdais,	/* 0023 (c) 1991 SNK */
                /*TODO*///	driver_lresort,	/* 0024 (c) 1992 SNK */
                /*TODO*///	driver_eightman,	/* 0025 (c) 1991 SNK / Pallas */
                /*TODO*///	/* 0026 Fun Fun Brothers - prototype? */
                /*TODO*///	driver_minasan,	/* 0027 (c) 1990 Monolith Corp. */
                /*TODO*///	/* 0028 */
                /*TODO*///	driver_legendos,	/* 0029 (c) 1991 SNK */
                /*TODO*///	driver_2020bb,	/* 0030 (c) 1991 SNK / Pallas */
                /*TODO*///	driver_2020bbh,	/* 0030 (c) 1991 SNK / Pallas */
                /*TODO*///	driver_socbrawl,	/* 0031 (c) 1991 SNK */
                /*TODO*///	driver_roboarmy,	/* 0032 (c) 1991 SNK */
                /*TODO*///	driver_fatfury1,	/* 0033 (c) 1991 SNK */
                /*TODO*///	driver_fbfrenzy,	/* 0034 (c) 1992 SNK */
                /*TODO*///	/* 0035 */
                /*TODO*///	driver_bakatono,	/* 0036 (c) 1991 Monolith Corp. */
                /*TODO*///	driver_crsword,	/* 0037 (c) 1991 Alpha Denshi Co */
                /*TODO*///	driver_trally,	/* 0038 (c) 1991 Alpha Denshi Co */
                /*TODO*///	driver_kotm2,		/* 0039 (c) 1992 SNK */
                /*TODO*///	driver_sengoku2,	/* 0040 (c) 1993 SNK */
                /*TODO*///	driver_bstars2,	/* 0041 (c) 1992 SNK */
                /*TODO*///	driver_quizdai2,	/* 0042 (c) 1992 SNK */
                /*TODO*///	driver_3countb,	/* 0043 (c) 1993 SNK */
                /*TODO*///	driver_aof,		/* 0044 (c) 1992 SNK */
                /*TODO*///	driver_samsho,	/* 0045 (c) 1993 SNK */
                /*TODO*///	driver_tophuntr,	/* 0046 (c) 1994 SNK */
                /*TODO*///	driver_fatfury2,	/* 0047 (c) 1992 SNK */
                /*TODO*///	driver_janshin,	/* 0048 (c) 1994 Aicom */
                /*TODO*///	driver_androdun,	/* 0049 (c) 1992 Visco */
                /*TODO*///	driver_ncommand,	/* 0050 (c) 1992 Alpha Denshi Co */
                /*TODO*///	driver_viewpoin,	/* 0051 (c) 1992 Sammy */
                /*TODO*///	driver_ssideki,	/* 0052 (c) 1992 SNK */
                /*TODO*///	driver_wh1,		/* 0053 (c) 1992 Alpha Denshi Co */
                /*TODO*///	/* 0054 Crossed Swords 2 (CD only) */
                /*TODO*///	driver_kof94,		/* 0055 (c) 1994 SNK */
                /*TODO*///	driver_aof2,		/* 0056 (c) 1994 SNK */
                /*TODO*///	driver_wh2,		/* 0057 (c) 1993 ADK */
                /*TODO*///	driver_fatfursp,	/* 0058 (c) 1993 SNK */
                /*TODO*///	driver_savagere,	/* 0059 (c) 1995 SNK */
                /*TODO*///	driver_fightfev,	/* 0060 (c) 1994 Viccom */
                /*TODO*///	driver_ssideki2,	/* 0061 (c) 1994 SNK */
                /*TODO*///	driver_spinmast,	/* 0062 (c) 1993 Data East Corporation */
                /*TODO*///	driver_samsho2,	/* 0063 (c) 1994 SNK */
                /*TODO*///	driver_wh2j,		/* 0064 (c) 1994 ADK / SNK */
                /*TODO*///	driver_wjammers,	/* 0065 (c) 1994 Data East Corporation */
                /*TODO*///	driver_karnovr,	/* 0066 (c) 1994 Data East Corporation */
                /*TODO*///	driver_gururin,	/* 0067 (c) 1994 Face */
                /*TODO*///	driver_pspikes2,	/* 0068 (c) 1994 Video System Co. */
                /*TODO*///	driver_fatfury3,	/* 0069 (c) 1995 SNK */
                /*TODO*///	/* 0070 */
                /*TODO*///	/* 0071 */
                /*TODO*///	/* 0072 */
                /*TODO*///	driver_panicbom,	/* 0073 (c) 1994 Eighting / Hudson */
                /*TODO*///	driver_aodk,		/* 0074 (c) 1994 ADK / SNK */
                /*TODO*///	driver_sonicwi2,	/* 0075 (c) 1994 Video System Co. */
                /*TODO*///	driver_zedblade,	/* 0076 (c) 1994 NMK */
                /*TODO*///	/* 0077 */
                /*TODO*///	driver_galaxyfg,	/* 0078 (c) 1995 Sunsoft */
                /*TODO*///	driver_strhoop,	/* 0079 (c) 1994 Data East Corporation */
                /*TODO*///	driver_quizkof,	/* 0080 (c) 1995 Saurus */
                /*TODO*///	driver_ssideki3,	/* 0081 (c) 1995 SNK */
                /*TODO*///	driver_doubledr,	/* 0082 (c) 1995 Technos */
                /*TODO*///	driver_pbobble,	/* 0083 (c) 1994 Taito */
                /*TODO*///	driver_kof95,		/* 0084 (c) 1995 SNK */
                /*TODO*///	/* 0085 Shinsetsu Samurai Spirits Bushidoretsuden / Samurai Shodown RPG (CD only) */
                /*TODO*///	driver_tws96,		/* 0086 (c) 1996 Tecmo */
                /*TODO*///	driver_samsho3,	/* 0087 (c) 1995 SNK */
                /*TODO*///	driver_stakwin,	/* 0088 (c) 1995 Saurus */
                /*TODO*///	driver_pulstar,	/* 0089 (c) 1995 Aicom */
                /*TODO*///	driver_whp,		/* 0090 (c) 1995 ADK / SNK */
                /*TODO*///	/* 0091 */
                /*TODO*///	driver_kabukikl,	/* 0092 (c) 1995 Hudson */
                /*TODO*///	driver_neobombe,	/* 0093 (c) 1997 Hudson */
                /*TODO*///	driver_gowcaizr,	/* 0094 (c) 1995 Technos */
                /*TODO*///	driver_rbff1,		/* 0095 (c) 1995 SNK */
                /*TODO*///	driver_aof3,		/* 0096 (c) 1996 SNK */
                /*TODO*///	driver_sonicwi3,	/* 0097 (c) 1995 Video System Co. */
                /*TODO*///	/* 0098 Idol Mahjong - final romance 2 (CD only? not confirmed, MVS might exist) */
                /*TODO*///	/* 0099 */
                /*TODO*///	driver_turfmast,	/* 0200 (c) 1996 Nazca */
                /*TODO*///	driver_mslug,		/* 0201 (c) 1996 Nazca */
                /*TODO*///	driver_puzzledp,	/* 0202 (c) 1995 Taito (Visco license) */
                /*TODO*///	driver_mosyougi,	/* 0203 (c) 1995 ADK / SNK */
                /*TODO*///	/* 0204 ADK World (CD only) */
                /*TODO*///	/* 0205 Neo-Geo CD Special (CD only) */
                /*TODO*///	driver_marukodq,	/* 0206 (c) 1995 Takara */
                /*TODO*///	driver_neomrdo,	/* 0207 (c) 1996 Visco */
                /*TODO*///	driver_sdodgeb,	/* 0208 (c) 1996 Technos */
                /*TODO*///	driver_goalx3,	/* 0209 (c) 1995 Visco */
                /*TODO*///	/* 0210 */
                /*TODO*///	/* 0211 Oshidashi Zintrick (CD only? not confirmed, MVS might exist) */
                /*TODO*///	driver_overtop,	/* 0212 (c) 1996 ADK */
                /*TODO*///	driver_neodrift,	/* 0213 (c) 1996 Visco */
                /*TODO*///	driver_kof96,		/* 0214 (c) 1996 SNK */
                /*TODO*///	driver_ssideki4,	/* 0215 (c) 1996 SNK */
                /*TODO*///	driver_kizuna,	/* 0216 (c) 1996 SNK */
                /*TODO*///	driver_ninjamas,	/* 0217 (c) 1996 ADK / SNK */
                /*TODO*///	driver_ragnagrd,	/* 0218 (c) 1996 Saurus */
                /*TODO*///	driver_pgoal,		/* 0219 (c) 1996 Saurus */
                /*TODO*///	/* 0220 Choutetsu Brikin'ger - iron clad (CD only? not confirmed, MVS might exist) */
                /*TODO*///	driver_magdrop2,	/* 0221 (c) 1996 Data East Corporation */
                /*TODO*///	driver_samsho4,	/* 0222 (c) 1996 SNK */
                /*TODO*///	driver_rbffspec,	/* 0223 (c) 1996 SNK */
                /*TODO*///	driver_twinspri,	/* 0224 (c) 1996 ADK */
                /*TODO*///	driver_wakuwak7,	/* 0225 (c) 1996 Sunsoft */
                /*TODO*///	/* 0226 */
                /*TODO*///	driver_stakwin2,	/* 0227 (c) 1996 Saurus */
                /*TODO*///	/* 0228 */
                /*TODO*///	/* 0229 King of Fighters '96 CD Collection (CD only) */
                /*TODO*///	driver_breakers,	/* 0230 (c) 1996 Visco */
                /*TODO*///	driver_miexchng,	/* 0231 (c) 1997 Face */
                /*TODO*///	driver_kof97,		/* 0232 (c) 1997 SNK */
                /*TODO*///	driver_magdrop3,	/* 0233 (c) 1997 Data East Corporation */
                /*TODO*///	driver_lastblad,	/* 0234 (c) 1997 SNK */
                /*TODO*///	driver_puzzldpr,	/* 0235 (c) 1997 Taito (Visco license) */
                /*TODO*///	driver_irrmaze,	/* 0236 (c) 1997 SNK / Saurus */
                /*TODO*///	driver_popbounc,	/* 0237 (c) 1997 Video System Co. */
                /*TODO*///	driver_shocktro,	/* 0238 (c) 1997 Saurus */
                /*TODO*///	driver_blazstar,	/* 0239 (c) 1998 Yumekobo */
                /*TODO*///	driver_rbff2,		/* 0240 (c) 1998 SNK */
                /*TODO*///	driver_mslug2,	/* 0241 (c) 1998 SNK */
                /*TODO*///	driver_kof98,		/* 0242 (c) 1998 SNK */
                /*TODO*///	driver_lastbld2,	/* 0243 (c) 1998 SNK */
                /*TODO*///	driver_neocup98,	/* 0244 (c) 1998 SNK */
                /*TODO*///	driver_breakrev,	/* 0245 (c) 1998 Visco */
                /*TODO*///	driver_shocktr2,	/* 0246 (c) 1998 Saurus */
                /*TODO*///	driver_flipshot,	/* 0247 (c) 1998 Visco */
                /*TODO*////*TESTDRIVER*/driver_pbobbl2n,	/* 0248 (c) 1999 Taito (SNK license) */
                /*TODO*////*TESTDRIVER*/driver_ctomaday,	/* 0249 (c) 1999 Visco */
                /*TODO*////*TESTDRIVER*/driver_mslugx,	/* 0250 (c) 1999 SNK */
                /*TODO*////*TESTDRIVER*/driver_kof99,		/* 0251 (c) 1999 SNK */
                /*TODO*////*TESTDRIVER*/driver_garou,		/* 0253 (c) 1999 SNK */
                null
            };
}
