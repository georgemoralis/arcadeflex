/*  Z80 FMLY.H   Z80 FAMILY IC EMURATION */


#define MAX_CTC 2

#define NOTIMER_0 (1<<0)
#define NOTIMER_1 (1<<1)
#define NOTIMER_2 (1<<2)
#define NOTIMER_3 (1<<3)

typedef struct
{
	int num;                                      /* number of CTCs to emulate */
	int baseclock[MAX_CTC];                           /* timer clock */
	int notimer[MAX_CTC];                         /* timer disablers */
	void (*intr[MAX_CTC])(int which);             /* callback when change interrupt status */
	void (*zc0[MAX_CTC])(int offset, int data);   /* ZC/TO0 callback */
	void (*zc1[MAX_CTC])(int offset, int data);   /* ZC/TO1 callback */
	void (*zc2[MAX_CTC])(int offset, int data);   /* ZC/TO2 callback */
} z80ctc_interface;

void z80ctc_init (z80ctc_interface *intf);

double z80ctc_getperiod (int which, int ch);

void z80ctc_reset (int which);
void z80ctc_0_reset (void);
void z80ctc_1_reset (void);

void z80ctc_w (int which, int offset, int data);
void z80ctc_0_w (int offset, int data);
void z80ctc_1_w (int offset, int data);

int z80ctc_r (int which, int offset);
int z80ctc_0_r (int offset);
int z80ctc_1_r (int offset);

void z80ctc_trg_w (int which, int trg, int offset, int data);
void z80ctc_0_trg0_w (int offset, int data);
void z80ctc_0_trg1_w (int offset, int data);
void z80ctc_0_trg2_w (int offset, int data);
void z80ctc_0_trg3_w (int offset, int data);
void z80ctc_1_trg0_w (int offset, int data);
void z80ctc_1_trg1_w (int offset, int data);
void z80ctc_1_trg2_w (int offset, int data);
void z80ctc_1_trg3_w (int offset, int data);

/* Z80 DaisyChain controll */
int z80ctc_interrupt( int which );
void z80ctc_reti( int which );
/*--------------------------------------------------------------------*/
#define MAX_PIO 1

typedef struct
{
	int num;                                      /* number of PIOs to emulate */
	void (*intr[MAX_CTC])(int which);             /* callback when change interrupt status */
	void (*rdyA[MAX_PIO])(int data );             /* portA ready active callback (do not support yet)*/
	void (*rdyB[MAX_PIO])(int data );             /* portB ready active callback (do not support yet)*/
} z80pio_interface;

void z80pio_init (z80pio_interface *intf);
void z80pio_reset (int which);
void z80pio_d_w( int which , int ch , int data );
void z80pio_c_w( int which , int ch , int data );
int z80pio_c_r( int which , int ch );
int z80pio_d_r( int which , int ch );

void z80pio_p_w( int which , int ch , int data );
int z80pio_p_r( int which , int ch );

/* Z80 DaisyChain controll */
int z80pio_interrupt( int which );
void z80pio_reti( int which );

/* mame interface */
void z80pio_0_reset (void);

/* this functions can use when C/D = A0 , A/B = A1 */
void z80pio_0_w(int offset , int data);
int  z80pio_0_r(int offset );

void z80pioA_0_p_w(int offset , int data);
void z80pioB_0_p_w(int offset , int data);
int z80pioA_0_p_r( int offset );
int z80pioB_0_p_r( int offset );


