/*********************************************************************

  cheat.h

*********************************************************************/

#ifndef CHEAT_H
#define CHEAT_H

extern int he_did_cheat;


int cheat_menu(struct osd_bitmap *bitmap, int selection);
void DoCheat(struct osd_bitmap *bitmap);

#endif
