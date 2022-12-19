#ifndef __RP5H01_H__
#define __RP5H01_H__

/* max simultaneous chips supported. change if you need more */
#define MAX_RP5H01	1

struct RP5H01_interface {
	int num;					/* number of chips */
	int region[MAX_RP5H01];		/* memory region where data resides */
	int offset[MAX_RP5H01];		/* memory offset within the above region where data resides */
};

extern int RP5H01_init( struct RP5H01_interface *interface );
extern void RP5H01_enable_w( int which, int data );				/* /CE */
extern void RP5H01_reset_w( int which, int data );				/* RESET */
extern void RP5H01_clock_w( int which, int data );				/* DATA CLOCK (active low) */
extern void RP5H01_test_w( int which, int data );				/* TEST */
extern int RP5H01_counter_r( int which );						/* COUNTER OUT */
extern int RP5H01_data_r( int which );							/* DATA */

/* direct-access stubs */
extern extern extern extern extern extern 
#endif /* __RP5H01_H__ */
