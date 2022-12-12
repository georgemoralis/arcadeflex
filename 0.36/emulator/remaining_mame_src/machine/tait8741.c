/*
Taito 8741 emulation

1.comminucation main and sub cpu
2.dipswitch and key handling

program types

*/

#include "driver.h"
#include "tait8741.h"

//#define __log__ 1

#define CMD_IDLE 0
#define CMD_08 1
#define CMD_4a 2

typedef struct TAITO8741_status{
	unsigned char toData;    /* to host data      */
	unsigned char fromData;  /* from host data    */
	unsigned char fromCmd;   /* from host command */
	unsigned char status;    /* b0 = rd ready,b1 = wd full,b2 = cmd ?? */
	unsigned char mode;
	unsigned char phase;
	unsigned char txd[8];
	unsigned char rxd[8];
	unsigned char parallelselect;
	unsigned char txpoint;
	int connect;
	unsigned char pending4a;
	int serial_out;
	int coins;
	int (*portHandler)(int offset);
}I8741;

static const struct TAITO8741interface *intf;
//static I8741 *taito8741;
static I8741 taito8741[MAX_TAITO8741];

/* for host data , write */
static void taito8741_hostdata_w(I8741 *st,int data)
{
	st->toData = data;
	st->status |= 0x01;
}

/* from host data , read */
static int taito8741_hostdata_r(I8741 *st)
{
	if( !(st->status & 0x02) ) return -1;
	st->status &= 0xfd;
	return st->fromData;
}

/* from host command , read */
static int taito8741_hostcmd_r(I8741 *st)
{
	if(!(st->status & 0x04)) return -1;
	st->status &= 0xfb;
	return st->fromCmd;
}


/* TAITO8741 I8741 emulation */

void taito8741_serial_rx(I8741 *st,unsigned char *data)
{
	memcpy(st->rxd,data,8);
}

/* timer callback of serial tx finish */
void taito8741_serial_tx(int num)
{
	I8741 *st = &taito8741[num];
	I8741 *sst;

	if( st->mode==TAITO8741_MASTER)
		st->serial_out = 1;

	st->txpoint = 1;
	if(st->connect >= 0 )
	{
		sst = &taito8741[st->connect];
		/* transfer data */
		taito8741_serial_rx(sst,st->txd);
#if __log__
		if(errorlog) fprintf(errorlog,"8741-%d Serial data TX to %d\n",num,st->connect);
#endif
		if( sst->mode==TAITO8741_SLAVE)
			sst->serial_out = 1;
	}
}

void TAITO8741_reset(int num)
{
	I8741 *st = &taito8741[num];
	st->status = 0x00;
	st->phase = 0;
	st->parallelselect = 0;
	st->txpoint = 1;
	st->pending4a = 0;
	st->serial_out = 0;
	st->coins = 0;
	memset(st->rxd,0,8);
	memset(st->txd,0,8);
}

/* 8741 update */
static void taito8741_update(int num)
{
	I8741 *st,*sst;
	int next = num;
	int data;

	do{
		num = next;
		st = &taito8741[num];
		if( st->connect != -1 )
			 sst = &taito8741[st->connect];
		else sst = 0;
		next = -1;
		/* check pending command */
		switch(st->phase)
		{
		case CMD_08: /* serial data latch */
			if( st->serial_out)
			{
				st->status &= 0xfb; /* patch for gsword */
				st->phase = CMD_IDLE;
				next = num; /* continue this chip */
			}
			break;
		case CMD_4a: /* wait for syncronus ? */
			if(!st->pending4a)
			{
				taito8741_hostdata_w(st,0);
				st->phase = CMD_IDLE;
				next = num; /* continue this chip */
			}
			break;
		case CMD_IDLE:
			/* ----- data in port check ----- */
			data = taito8741_hostdata_r(st);
			if( data != -1 )
			{
				switch(st->mode)
				{
				case TAITO8741_MASTER:
				case TAITO8741_SLAVE:
					/* buffering transmit data */
					if( st->txpoint < 8 )
					{
//if(errorlog && st->txpoint == 0 && num==1 && data&0x80) fprintf(errorlog,"Coin Put\n");
						st->txd[st->txpoint++] = data;
					}
					break;
				case TAITO8741_PORT:
					if( data & 0xf8)
					{ /* ?? */
					}
					else
					{ /* port select */
						st->parallelselect = data & 0x07;
						taito8741_hostdata_w(st,st->portHandler ? st->portHandler(st->parallelselect) : 0);
					}
				}
			}
			/* ----- new command fetch ----- */
			data = taito8741_hostcmd_r(st);
			switch( data )
			{
			case -1: /* no command data */
				break;
			case 0x00: /* read from parallel port */
				taito8741_hostdata_w(st,st->portHandler ? st->portHandler(0) : 0 );
				break;
			case 0x01: /* read receive buffer 0 */
			case 0x02: /* read receive buffer 1 */
			case 0x03: /* read receive buffer 2 */
			case 0x04: /* read receive buffer 3 */
			case 0x05: /* read receive buffer 4 */
			case 0x06: /* read receive buffer 5 */
			case 0x07: /* read receive buffer 6 */
//if(errorlog && data == 2 && num==0 && st->rxd[data-1]&0x80) fprintf(errorlog,"Coin Get\n");
				taito8741_hostdata_w(st,st->rxd[data-1]);
				break;
			case 0x08:	/* latch received serial data */
				st->txd[0] = st->portHandler ? st->portHandler(0) : 0;
				if( sst )
				{
					timer_set (TIME_NOW,num,taito8741_serial_tx);
					st->serial_out = 0;
					st->status |= 0x04;
					st->phase = CMD_08;
				}
				break;
			case 0x0a:	/* 8741-0 : set serial comminucation mode 'MASTER' */
				//st->mode = TAITO8741_MASTER;
				break;
			case 0x0b:	/* 8741-1 : set serial comminucation mode 'SLAVE'  */
				//st->mode = TAITO8741_SLAVE;
				break;
			case 0x1f:  /* 8741-2,3 : ?? set parallelport mode ?? */
			case 0x3f:  /* 8741-2,3 : ?? set parallelport mode ?? */
			case 0xe1:  /* 8741-2,3 : ?? set parallelport mode ?? */
				st->mode = TAITO8741_PORT;
				st->parallelselect = 1; /* preset read number */
				break;
			case 0x62:  /* 8741-3   : ? */
				break;
			case 0x4a:	/* ?? syncronus with other cpu and return 00H */
				if( sst )
				{
					if(sst->pending4a)
					{
						sst->pending4a = 0; /* syncronus */
						taito8741_hostdata_w(st,0); /* return for host */
						next = st->connect;
					}
					else st->phase = CMD_4a;
				}
				break;
			case 0x80:	/* 8741-3 : return check code */
				taito8741_hostdata_w(st,0x66);
				break;
			case 0x81:	/* 8741-2 : return check code */
				taito8741_hostdata_w(st,0x48);
				break;
			case 0xf0:  /* GSWORD 8741-1 : initialize ?? */
				break;
			case 0x82:  /* GSWORD 8741-2 unknown */
				break;
			}
			break;
		}
	}while(next>=0);
}

int TAITO8741_start(const struct TAITO8741interface *taito8741intf)
{
	int i;

	intf = taito8741intf;

	//taito8741 = (I8741 *)malloc(intf->num*sizeof(I8741));
	//if( taito8741 == 0 ) return 1;

	for(i=0;i<intf->num;i++)
	{
		taito8741[i].connect     = intf->serial_connect[i];
		taito8741[i].portHandler = intf->portHandler_r[i];
		taito8741[i].mode        = intf->mode[i];
		TAITO8741_reset(i);
	}
	return 0;
}

/* read status port */
static int I8741_status_r(int num)
{
	I8741 *st = &taito8741[num];
	taito8741_update(num);
#if __log__
	if(errorlog) fprintf(errorlog,"8741-%d ST Read %02x PC=%04x\n",num,st->status,cpu_get_pc());
#endif
	return st->status;
}

/* read data port */
static int I8741_data_r(int num)
{
	I8741 *st = &taito8741[num];
	int ret = st->toData;
	st->status &= 0xfe;
#if __log__
	if(errorlog) fprintf(errorlog,"8741-%d DATA Read %02x PC=%04x\n",num,ret,cpu_get_pc());
#endif
	/* update chip */
	taito8741_update(num);

	switch( st->mode )
	{
	case TAITO8741_PORT: /* parallel data */
		taito8741_hostdata_w(st,st->portHandler ? st->portHandler(st->parallelselect) : 0);
		break;
	}
	return ret;
}

/* Write data port */
static void I8741_data_w(int num, int data)
{
	I8741 *st = &taito8741[num];
#if __log__
	if(errorlog) fprintf(errorlog,"8741-%d DATA Write %02x PC=%04x\n",num,data,cpu_get_pc());
#endif
	st->fromData = data;
	st->status |= 0x02;
	/* update chip */
	taito8741_update(num);
}

/* Write command port */
static void I8741_command_w(int num, int data)
{
	I8741 *st = &taito8741[num];
#if __log__
	if(errorlog) fprintf(errorlog,"8741-%d CMD Write %02x PC=%04x\n",num,data,cpu_get_pc());
#endif
	st->fromCmd = data;
	st->status |= 0x04;
	/* update chip */
	taito8741_update(num);
}

/* Write port handler */
void TAITO8741_0_w(int offset, int data)
{
	if(offset&1) I8741_command_w(0,data);
	else         I8741_data_w(0,data);
}
void TAITO8741_1_w(int offset, int data)
{
	if(offset&1) I8741_command_w(1,data);
	else         I8741_data_w(1,data);
}
void TAITO8741_2_w(int offset, int data)
{
	if(offset&1) I8741_command_w(2,data);
	else         I8741_data_w(2,data);
}
void TAITO8741_3_w(int offset, int data)
{
	if(offset&1) I8741_command_w(3,data);
	else         I8741_data_w(3,data);
}

/* Read port handler */
int TAITO8741_0_r(int offset)
{
	if(offset&1) return I8741_status_r(0);
	return I8741_data_r(0);
}
int TAITO8741_1_r(int offset)
{
	if(offset&1) return I8741_status_r(1);
	return I8741_data_r(1);
}
int TAITO8741_2_r(int offset)
{
	if(offset&1) return I8741_status_r(2);
	return I8741_data_r(2);
}
int TAITO8741_3_r(int offset)
{
	if(offset&1) return I8741_status_r(3);
	return I8741_data_r(3);
}
