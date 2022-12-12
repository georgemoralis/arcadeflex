/***************************************************************************

   tiaintf.c

   Interface the routines in tiasound.c to MESS

***************************************************************************/

#include "driver.h"
#include "sound/tiasound.h"
#include "sound/tiaintf.h"

#define MIN_SLICE 10    /* minimum update step */

#define BUFFER_LEN 8192

static unsigned int sample_pos;

static int channel;

static const struct TIAinterface *intf;
static INT16 *buffer;

int tia_sh_start(const struct MachineSound *msound)
{
//	int i, res;

	intf = msound->sound_interface;

	if (Machine->sample_rate == 0) return 0;
	sample_pos = 0;

	channel = mixer_allocate_channel(intf->volume);

	if ((buffer = malloc(sizeof(INT16) * BUFFER_LEN)) == 0)
		return 1;
	memset(buffer,0,sizeof(INT16) * BUFFER_LEN);

	Tia_sound_init (intf->clock, Machine->sample_rate);
	return 0;
}


void tia_sh_stop (void)
{
	/* Nothing to do here */
}

void tia_sh_update (void)
{
	int buflen;

	if (Machine->sample_rate == 0) return;

	buflen = mixer_samples_this_frame();
	if (sample_pos < buflen)
		Tia_process (buffer + sample_pos, buflen - sample_pos);
	sample_pos = 0;

	mixer_play_streamed_sample_16(channel,buffer,2*buflen,Machine->sample_rate);
}
