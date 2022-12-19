#ifndef TMS36XX_SOUND_H
#define TMS36XX_SOUND_H

#define MAX_TMS36XX 4

/* subtypes */
#define MM6221AA    21      /* Phoenix (fixed melodies) */
#define TMS3615 	15		/* Naughty Boy, Pleiads (13 notes, one output) */
#define TMS3617 	17		/* Monster Bash (13 notes, six outputs) */

/* The interface structure */
struct TMS36XXinterface {
	int num;
	int mixing_level[MAX_TMS36XX];
	int subtype[MAX_TMS36XX];
	int basefreq[MAX_TMS36XX];		/* base frequecnies of the chips */
	double decay[MAX_TMS36XX][6];	/* decay times for the six harmonic notes */
	double speed[MAX_TMS36XX];		/* tune speed (meaningful for the TMS3615 only) */
};

extern int tms36xx_sh_start(const struct MachineSound *msound);
extern void tms36xx_sh_stop(void);
extern void tms36xx_sh_update(void);

/* MM6221AA interface functions */
extern void mm6221aa_tune_w(int chip, int tune);

/* TMS3615/17 interface functions */
extern void tms36xx_note_w(int chip, int octave, int note);

/* TMS3617 interface functions */
extern void tms3617_enable_w(int chip, int enable);

#endif
