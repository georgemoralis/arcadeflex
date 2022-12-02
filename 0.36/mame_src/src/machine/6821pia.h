/**********************************************************************

	Motorola 6821 PIA interface and emulation

	This function emulates all the functionality of up to 4 M6821
	peripheral interface adapters.

**********************************************************************/

#ifndef PIA_6821
#define PIA_6821


#define MAX_PIA 8


/* this is the standard ordering of the registers */
/* alternate ordering swaps registers 1 and 2 */
#define	PIA_DDRA	0
#define	PIA_CTLA	1
#define	PIA_DDRB	2
#define	PIA_CTLB	3

/* PIA addressing modes */
#define PIA_STANDARD_ORDERING		0
#define PIA_ALTERNATE_ORDERING		1

#define PIA_8BIT					0
#define PIA_16BIT					2

#define PIA_LOWER					0
#define PIA_UPPER					4
#define PIA_AUTOSENSE				8

#define PIA_16BIT_LOWER				(PIA_16BIT | PIA_LOWER)
#define PIA_16BIT_UPPER				(PIA_16BIT | PIA_UPPER)
#define PIA_16BIT_AUTO				(PIA_16BIT | PIA_AUTOSENSE)

struct pia6821_interface
{
	int (*in_a_func)(int offset);
	int (*in_b_func)(int offset);
	int (*in_ca1_func)(int offset);
	int (*in_cb1_func)(int offset);
	int (*in_ca2_func)(int offset);
	int (*in_cb2_func)(int offset);
	void (*out_a_func)(int offset, int val);
	void (*out_b_func)(int offset, int val);
	void (*out_ca2_func)(int offset, int val);
	void (*out_cb2_func)(int offset, int val);
	void (*irq_a_func)(int state);
	void (*irq_b_func)(int state);
};


void pia_unconfig(void);
void pia_config(int which, int addressing, const struct pia6821_interface *intf);
void pia_reset(void);
int pia_read(int which, int offset);
void pia_write(int which, int offset, int data);
void pia_set_input_a(int which, int data);
void pia_set_input_ca1(int which, int data);
void pia_set_input_ca2(int which, int data);
void pia_set_input_b(int which, int data);
void pia_set_input_cb1(int which, int data);
void pia_set_input_cb2(int which, int data);

/******************* Standard 8-bit CPU interfaces, D0-D7 *******************/

int pia_0_r(int offset);
int pia_1_r(int offset);
int pia_2_r(int offset);
int pia_3_r(int offset);
int pia_4_r(int offset);
int pia_5_r(int offset);
int pia_6_r(int offset);
int pia_7_r(int offset);

void pia_0_w(int offset, int data);
void pia_1_w(int offset, int data);
void pia_2_w(int offset, int data);
void pia_3_w(int offset, int data);
void pia_4_w(int offset, int data);
void pia_5_w(int offset, int data);
void pia_6_w(int offset, int data);
void pia_7_w(int offset, int data);

/******************* 8-bit A/B port interfaces *******************/

void pia_0_porta_w(int offset, int data);
void pia_1_porta_w(int offset, int data);
void pia_2_porta_w(int offset, int data);
void pia_3_porta_w(int offset, int data);
void pia_4_porta_w(int offset, int data);
void pia_5_porta_w(int offset, int data);
void pia_6_porta_w(int offset, int data);
void pia_7_porta_w(int offset, int data);

void pia_0_portb_w(int offset, int data);
void pia_1_portb_w(int offset, int data);
void pia_2_portb_w(int offset, int data);
void pia_3_portb_w(int offset, int data);
void pia_4_portb_w(int offset, int data);
void pia_5_portb_w(int offset, int data);
void pia_6_portb_w(int offset, int data);
void pia_7_portb_w(int offset, int data);

int pia_0_porta_r(int offset);
int pia_1_porta_r(int offset);
int pia_2_porta_r(int offset);
int pia_3_porta_r(int offset);
int pia_4_porta_r(int offset);
int pia_5_porta_r(int offset);
int pia_6_porta_r(int offset);
int pia_7_porta_r(int offset);

int pia_0_portb_r(int offset);
int pia_1_portb_r(int offset);
int pia_2_portb_r(int offset);
int pia_3_portb_r(int offset);
int pia_4_portb_r(int offset);
int pia_5_portb_r(int offset);
int pia_6_portb_r(int offset);
int pia_7_portb_r(int offset);

/******************* 1-bit CA1/CA2/CB1/CB2 port interfaces *******************/

void pia_0_ca1_w(int offset, int data);
void pia_1_ca1_w(int offset, int data);
void pia_2_ca1_w(int offset, int data);
void pia_3_ca1_w(int offset, int data);
void pia_4_ca1_w(int offset, int data);
void pia_5_ca1_w(int offset, int data);
void pia_6_ca1_w(int offset, int data);
void pia_7_ca1_w(int offset, int data);
void pia_0_ca2_w(int offset, int data);
void pia_1_ca2_w(int offset, int data);
void pia_2_ca2_w(int offset, int data);
void pia_3_ca2_w(int offset, int data);
void pia_4_ca2_w(int offset, int data);
void pia_5_ca2_w(int offset, int data);
void pia_6_ca2_w(int offset, int data);
void pia_7_ca2_w(int offset, int data);

void pia_0_cb1_w(int offset, int data);
void pia_1_cb1_w(int offset, int data);
void pia_2_cb1_w(int offset, int data);
void pia_3_cb1_w(int offset, int data);
void pia_4_cb1_w(int offset, int data);
void pia_5_cb1_w(int offset, int data);
void pia_6_cb1_w(int offset, int data);
void pia_7_cb1_w(int offset, int data);
void pia_0_cb2_w(int offset, int data);
void pia_1_cb2_w(int offset, int data);
void pia_2_cb2_w(int offset, int data);
void pia_3_cb2_w(int offset, int data);
void pia_4_cb2_w(int offset, int data);
void pia_5_cb2_w(int offset, int data);
void pia_6_cb2_w(int offset, int data);
void pia_7_cb2_w(int offset, int data);

int pia_0_ca1_r(int offset);
int pia_1_ca1_r(int offset);
int pia_2_ca1_r(int offset);
int pia_3_ca1_r(int offset);
int pia_4_ca1_r(int offset);
int pia_5_ca1_r(int offset);
int pia_6_ca1_r(int offset);
int pia_7_ca1_r(int offset);
int pia_0_ca2_r(int offset);
int pia_1_ca2_r(int offset);
int pia_2_ca2_r(int offset);
int pia_3_ca2_r(int offset);
int pia_4_ca2_r(int offset);
int pia_5_ca2_r(int offset);
int pia_6_ca2_r(int offset);
int pia_7_ca2_r(int offset);

int pia_0_cb1_r(int offset);
int pia_1_cb1_r(int offset);
int pia_2_cb1_r(int offset);
int pia_3_cb1_r(int offset);
int pia_4_cb1_r(int offset);
int pia_5_cb1_r(int offset);
int pia_6_cb1_r(int offset);
int pia_7_cb1_r(int offset);
int pia_0_cb2_r(int offset);
int pia_1_cb2_r(int offset);
int pia_2_cb2_r(int offset);
int pia_3_cb2_r(int offset);
int pia_4_cb2_r(int offset);
int pia_5_cb2_r(int offset);
int pia_6_cb2_r(int offset);
int pia_7_cb2_r(int offset);


#endif
