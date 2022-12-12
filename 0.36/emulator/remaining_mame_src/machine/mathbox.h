/*
 * mathbox.h: math box simulation (Battlezone/Red Baron/Tempest)
 *
 * Copyright 1991, 1992, 1993, 1996 Eric Smith
 *
 * $Header: /usr2/eric/vg/atari/vecsim/RCS/mathbox.h,v 1.1 1996/08/29 07:23:59 eric Exp eric $
 */

typedef short s16;
typedef int s32;

void mb_go (int addr, int data);
int mb_status_r (int offset);
int mb_lo_r (int offset);
int mb_hi_r (int offset);

extern s16 mb_result;
