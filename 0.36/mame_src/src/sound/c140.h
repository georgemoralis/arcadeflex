/* C140.h */

#ifndef _NAMCO_C140_
#define _NAMCO_C140_

int C140_sh_start( const struct MachineSound *msound );
void C140_sh_stop( void );
void C140_sh_update( void );
int C140_r( int offset );
void C140_w( int offset, int data );

struct C140interface {
    int frequency;
    int region;
    int mixing_level;
};

#endif
