/* C140.h */

#ifndef _NAMCO_C140_
#define _NAMCO_C140_

int C140_sh_start( const struct MachineSound *msound );

struct C140interface {
    int frequency;
    int region;
    int mixing_level;
};

#endif
