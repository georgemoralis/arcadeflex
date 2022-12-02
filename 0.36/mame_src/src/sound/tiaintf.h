#ifndef tiaintf_h
#define tiaintf_h

#include "sound/tiasound.h"

#define TIA_DEFAULT_GAIN 16

struct TIAinterface
{
    unsigned int clock;
	int volume;
	int gain;
   int baseclock;
};

int tia_sh_start (const struct MachineSound *msound);
void tia_sh_stop (void);
void tia_sh_update (void);
void tia_w (UINT16 addr, UINT8 val);


#endif
