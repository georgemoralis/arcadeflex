/***************************************************************************

	M72 audio interface

****************************************************************************/

void m72_init_sound(void);
void m72_ym2151_irq_handler(int irq);
void m72_sound_command_w(int offset,int data);
void m72_sound_irq_ack_w(int offset,int data);
int m72_sample_r(int offset);
void m72_sample_w(int offset,int data);

/* the port goes to different address bits depending on the game */
void m72_set_sample_start(int start);
void vigilant_sample_addr_w(int offset,int data);
void shisen_sample_addr_w(int offset,int data);
void rtype2_sample_addr_w(int offset,int data);
