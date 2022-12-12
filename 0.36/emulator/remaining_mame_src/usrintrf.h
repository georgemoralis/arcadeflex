/*********************************************************************

  usrintrf.h

  Functions used to handle MAME's crude user interface.

*********************************************************************/

#ifndef USRINTRF_H
#define USRINTRF_H

struct DisplayText
{
	const char *text;	/* 0 marks the end of the array */
	int color;	/* see #defines below */
	int x;
	int y;
};

#define DT_COLOR_WHITE 0
#define DT_COLOR_YELLOW 1
#define DT_COLOR_RED 2


struct GfxElement *builduifont(void);
void pick_uifont_colors(void);
void displaytext(const struct DisplayText *dt,int erase,int update_screen);
void ui_text(const char *buf,int x,int y);
void ui_drawbox(int leftx,int topy,int width,int height);
void ui_displaymessagewindow(const char *text);
void ui_displaymenu(const char **items,const char **subitems,char *flag,int selected,int arrowize_subitem);
int showcopyright(void);
int showgamewarnings(void);
void set_ui_visarea (int xmin, int ymin, int xmax, int ymax);

void init_user_interface(void);
int handle_user_interface(void);

int onscrd_active(void);
int setup_active(void);

void CLIB_DECL usrintf_showmessage(const char *text,...);

#endif
