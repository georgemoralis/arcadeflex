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
	mem_read_handler in_a_func;
	mem_read_handler in_b_func;
	mem_read_handler in_ca1_func;
	mem_read_handler in_cb1_func;
	mem_read_handler in_ca2_func;
	mem_read_handler in_cb2_func;
	mem_write_handler out_a_func;
	mem_write_handler out_b_func;
	mem_write_handler out_ca2_func;
	mem_write_handler out_cb2_func;
	void (*irq_a_func)(int state);
	void (*irq_b_func)(int state);
};

#ifdef __cplusplus
extern "C" {
#endif

void pia_config(int which, int addressing, const struct pia6821_interface *intf);
int pia_read(int which, int offset);
void pia_write(int which, int offset, int data);
void pia_set_input_a(int which, int data);
void pia_set_input_ca1(int which, int data);
void pia_set_input_ca2(int which, int data);
void pia_set_input_b(int which, int data);
void pia_set_input_cb1(int which, int data);
void pia_set_input_cb2(int which, int data);

/******************* Standard 8-bit CPU interfaces, D0-D7 *******************/



/******************* 8-bit A/B port interfaces *******************/





/******************* 1-bit CA1/CA2/CB1/CB2 port interfaces *******************/





#ifdef __cplusplus
}
#endif

#endif
