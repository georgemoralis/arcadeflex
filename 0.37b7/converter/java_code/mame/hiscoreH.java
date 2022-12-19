#ifndef HISCORE_H
#define HISCORE_H

void hs_open( const char *name );

void computer_writemem_byte(int cpu, int addr, int value);
int computer_readmem_byte(int cpu, int addr);

#endif
