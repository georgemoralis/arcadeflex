#ifndef tms5220_h
#define tms5220_h

void tms5220_set_irq(void (*func)(int));

void tms5220_data_write(int data);

void tms5220_process(INT16 *buffer, unsigned int size);

#endif

