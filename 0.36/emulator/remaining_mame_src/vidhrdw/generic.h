#include "driver.h"


extern unsigned char *videoram;
extern int videoram_size;
extern unsigned char *colorram;
extern unsigned char *spriteram;
extern int spriteram_size;
extern unsigned char *spriteram_2;
extern int spriteram_2_size;
extern unsigned char *spriteram_3;
extern int spriteram_3_size;
extern unsigned char *buffered_spriteram;
extern unsigned char *buffered_spriteram_2;
extern unsigned char *flip_screen;
extern unsigned char *flip_screen_x;
extern unsigned char *flip_screen_y;
extern unsigned char *dirtybuffer;
extern struct osd_bitmap *tmpbitmap;

int generic_vh_start(void);
int generic_bitmapped_vh_start(void);
void generic_vh_stop(void);
void generic_bitmapped_vh_stop(void);
void generic_bitmapped_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh);
int videoram_r(int offset);
int colorram_r(int offset);
void videoram_w(int offset,int data);
void colorram_w(int offset,int data);
int spriteram_r(int offset);
void spriteram_w(int offset,int data);
int spriteram_2_r(int offset);
void spriteram_2_w(int offset,int data);
void buffer_spriteram_w(int offset,int data);
void buffer_spriteram_2_w(int offset,int data);
void buffer_spriteram(unsigned char *ptr,int length);
void buffer_spriteram_2(unsigned char *ptr,int length);

