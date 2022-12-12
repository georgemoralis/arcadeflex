/*
 *	Header file for the PD4990A Serial I/O calendar & clock.
 */
void addretrace (void);
int read_4990_testbit(void);
int read_4990_databit(void);
void write_4990_control(int offset, int data);
void increment_day(void);
void increment_month(void);
