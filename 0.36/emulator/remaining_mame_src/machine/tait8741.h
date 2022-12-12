#ifndef __TAITO8741__
#define __TAITO8741__

#define MAX_TAITO8741 4

/* NEC 8741 program mode */
#define TAITO8741_MASTER 0
#define TAITO8741_SLAVE  1
#define TAITO8741_PORT   2

struct TAITO8741interface
{
	int num;
	int mode[MAX_TAITO8741];            /* program select */
	int serial_connect[MAX_TAITO8741];	/* serial port connection */
	int (*portHandler_r[MAX_TAITO8741])(int offset); /* parallel port handler */
};

int  TAITO8741_start(const struct TAITO8741interface *taito8741intf);
void TAITO8741_stop(void);

void TAITO8741_reset(int num);

/* write handler */
void TAITO8741_0_w(int offset, int data);
void TAITO8741_1_w(int offset, int data);
void TAITO8741_2_w(int offset, int data);
void TAITO8741_3_w(int offset, int data);
/* read handler */
int TAITO8741_0_r(int offset);
int TAITO8741_1_r(int offset);
int TAITO8741_2_r(int offset);
int TAITO8741_3_r(int offset);

#endif
