/*****************************************************************
machine\swmathbx.h

This file is Copyright 1997, Steve Baines.

Release 2.0 (5 August 1997)

See drivers\starwars.c for notes

******************************************************************/


void init_starwars(void);

void run_mbox(void);
void init_swmathbox (void);

/* Read handlers */
int reh(int);
int rel(int);
int prng(int);

/* Write handlers */
void prngclr(int, int);
void mw0(int, int);
void mw1(int, int);
void mw2(int, int);
void swmathbx(int, int);

