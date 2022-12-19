#ifndef TIA_H
#define TIA_H

#define TIA_DEFAULT_GAIN 16

struct TIAinterface {
    unsigned int clock;
	int volume;
	int gain;
	int baseclock;
};


#endif


