#ifndef m_atari_vg_h
#define m_atari_vg_h

/***************************************************************************

  atari_vg.h

  Generic functions used by the Atari Vector games

***************************************************************************/

int atari_vg_earom_r(int offset);
void atari_vg_earom_w(int offset,int data);
void atari_vg_earom_ctrl(int offset, int data);
void atari_vg_earom_handler(void *file,int read_or_write);

#endif
