package gr.codebb.arcadeflex.v058.machine;

import arcadeflex.v036.generic.funcPtr.DaisyChainInterruptEntryPtr;
import arcadeflex.v036.generic.funcPtr.DaisyChainInterruptRetiPtr;
import arcadeflex.v036.generic.funcPtr.DaisyChainResetPtr;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.timer.*;
import static gr.codebb.arcadeflex.v058.machine.z80fmlyH.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;

public class z80fmly {
	public static class z80ctc
	{
		public int vector;                 /* interrupt vector */
		public int clock;                  /* system clock */
		public double invclock16;          /* 16/system clock */
		public double invclock256;         /* 256/system clock */
		public IntrPtr intr;    /* interrupt callback */
		public WriteHandlerPtr[] zc= new WriteHandlerPtr[4];    /* zero crossing callbacks */
		public int notimer;                /* no timer masks */
		public int[] mask=new int[4];                /* masked channel flags */
		public int[] mode=new int[4];                /* current mode */
		public int[] tconst=new int[4];              /* time constant */
		public int[] down=new int[4];                /* down counter (clock mode only) */
		public int[] extclk=new int[4];              /* current signal from the external clock */
		public timer_entry[] timer=new timer_entry[4];             /* array of active timers */
	
		public int[] int_state=new int[4];           /* interrupt status (for daisy chain) */
	};
	
	static z80ctc[] ctcs=new z80ctc[MAX_CTC];
	

	/* these are the bits of the incoming commands to the CTC */
        public static int INTERRUPT = 0x80;
        public static int INTERRUPT_ON = 0x80;
        public static int INTERRUPT_OFF = 0x00;

        public static int MODE = 0x40;
        public static int MODE_TIMER = 0x00;
        public static int MODE_COUNTER = 0x40;

        public static int PRESCALER = 0x20;
        public static int PRESCALER_256 = 0x20;
        public static int PRESCALER_16 = 0x00;

        public static int EDGE = 0x10;
        public static int EDGE_FALLING = 0x00;
        public static int EDGE_RISING = 0x10;

        public static int TRIGGER = 0x08;
        public static int TRIGGER_AUTO = 0x00;
        public static int TRIGGER_CLOCK = 0x08;

        public static int CONSTANT = 0x04;
        public static int CONSTANT_LOAD = 0x04;
        public static int CONSTANT_NONE = 0x00;;

        public static int RESET = 0x02;
        public static int RESET_CONTINUE = 0x00;
        public static int RESET_ACTIVE = 0x02;

        public static int CONTROL = 0x01;
        public static int CONTROL_VECTOR = 0x00;
        public static int CONTROL_WORD = 0x01;

        /* these extra bits help us keep things accurate */
        public static int WAITING_FOR_TRIG = 0x100;


/*TODO*///	static void z80ctc_timercallback (int param);
	
	
	public static void z80ctc_init (z80ctc_interface intf)
	{
		int i;
	
		/*TODO*///memset (ctcs, 0, sizeof (ctcs));
	
		for (i = 0; i < intf.num; i++)
		{
                        ctcs[i] = new z80ctc();
			ctcs[i].clock = intf.baseclock[i];
			ctcs[i].invclock16 = 16.0 / (double)intf.baseclock[i];
			ctcs[i].invclock256 = 256.0 / (double)intf.baseclock[i];
			ctcs[i].notimer = intf.notimer[i];
			ctcs[i].intr = intf.intr[i];
			ctcs[i].zc[0] = intf.zc0[i];
			ctcs[i].zc[1] = intf.zc1[i];
			ctcs[i].zc[2] = intf.zc2[i];
			ctcs[i].zc[3] = null;
			z80ctc_reset.handler(i);
		}
	}
	
	
	public static double z80ctc_getperiod (int which, int ch)
	{
		z80ctc ctc = ctcs[which];
		double clock;
		int mode;
	
		/* keep channel within range, and get the current mode */
		ch &= 3;
		mode = ctc.mode[ch];
	
		/* if reset active */
		if( (mode & RESET) == RESET_ACTIVE) return 0;
		/* if counter mode */
		if( (mode & MODE) == MODE_COUNTER)
		{
			logerror("CTC %d is CounterMode : Can't calcrate period\n", ch );
			return 0;
		}
	
		/* compute the period */
		clock = ((mode & PRESCALER) == PRESCALER_16) ? ctc.invclock16 : ctc.invclock256;
                ctcs[which] = ctc;
		return clock * (double)ctc.tconst[ch];
	}
	
	/* interrupt request callback with daisy-chain circuit */
	public static void z80ctc_interrupt_check( z80ctc ctc )
	{
		int state = 0;
		int ch;
	
		for( ch = 3 ; ch >= 0 ; ch-- )
		{
			/* if IEO disable , same and lower IRQ is masking */
	/* ASG: changed this line because this state could have an interrupt pending as well! */
	/*		if( ctc.int_state[ch] & Z80_INT_IEO ) state  = Z80_INT_IEO;*/
			if(( ctc.int_state[ch] & Z80_INT_IEO )!=0 ) state  = ctc.int_state[ch];
			else                                   state |= ctc.int_state[ch];
		}
		/* change interrupt status */
		if (ctc.intr != null) (ctc.intr).handler(state);
	}
	
	
	public static DaisyChainResetPtr z80ctc_reset = new DaisyChainResetPtr() {
            public void handler(int which) {
                z80ctc ctc = ctcs[which];
		int i;
	
		/* set up defaults */
		for (i = 0; i < 4; i++)
		{
			ctc.mode[i] = RESET_ACTIVE;
			ctc.tconst[i] = 0x100;
			if (ctc.timer[i] != null)
				timer_remove (ctc.timer[i]);
			ctc.timer[i] = null;
			ctc.int_state[i] = 0;
		}
		z80ctc_interrupt_check( ctc );
            }
        };
	
	
/*TODO*///	void z80ctc_0_reset (void) { z80ctc_reset (0); }
/*TODO*///	void z80ctc_1_reset (void) { z80ctc_reset (1); }
	
	
	public static void z80ctc_w (int which, int offset, int data)
	{
		//z80ctc *ctc = ctcs + which;
                z80ctc ctc = ctcs[which];
		int mode, ch;
	
		/* keep channel within range, and get the current mode */
		ch = offset & 3;
		mode = ctc.mode[ch];
	
		/* if we're waiting for a time constant, this is it */
		if ((mode & CONSTANT) == CONSTANT_LOAD)
		{
			/* set the time constant (0 . 0x100) */
			ctc.tconst[ch] = data!=0 ? data : 0x100;
	
			/* clear the internal mode -- we're no longer waiting */
			ctc.mode[ch] &= ~CONSTANT;
	
			/* also clear the reset, since the constant gets it going again */
			ctc.mode[ch] &= ~RESET;
	
			/* if we're in timer mode.... */
			if ((mode & MODE) == MODE_TIMER)
			{
				/* if we're triggering on the time constant, reset the down counter now */
				if ((mode & TRIGGER) == TRIGGER_AUTO)
				{
					double clock = ((mode & PRESCALER) == PRESCALER_16) ? ctc.invclock16 : ctc.invclock256;
					if (ctc.timer[ch] != null)
						timer_remove (ctc.timer[ch]);
					if ((ctc.notimer & (1<<ch)) == 0)
						ctc.timer[ch] = timer_pulse(clock * (double)ctc.tconst[ch], (which << 2) + ch, z80ctc_timercallback);
				}
	
				/* else set the bit indicating that we're waiting for the appropriate trigger */
				else
					ctc.mode[ch] |= WAITING_FOR_TRIG;
			}
	
			/* also set the down counter in case we're clocking externally */
			ctc.down[ch] = ctc.tconst[ch];
	
			/* all done here */
			return;
		}
	
/*TODO*///		/* if we're writing the interrupt vector, handle it specially */
/*TODO*///	#if 0	/* Tatsuyuki Satoh changes */
/*TODO*///		/* The 'Z80family handbook' wrote,                            */
/*TODO*///		/* interrupt vector is able to set for even channel (0 or 2)  */
/*TODO*///		if ((data & CONTROL) == CONTROL_VECTOR && (ch&1) == 0)
/*TODO*///	#else
		if ((data & CONTROL) == CONTROL_VECTOR && ch == 0)
/*TODO*///	#endif
		{
			ctc.vector = data & 0xf8;
			logerror("CTC Vector = %02x\n", ctc.vector);
			return;
		}
	
		/* this must be a control word */
		if ((data & CONTROL) == CONTROL_WORD)
		{
			/* set the new mode */
			ctc.mode[ch] = data;
			logerror("CTC ch.%d mode = %02x\n", ch, data);
	
			/* if we're being reset, clear out any pending timers for this channel */
			if ((data & RESET) == RESET_ACTIVE)
			{
				if (ctc.timer[ch] != null)
					timer_remove (ctc.timer[ch]);
				ctc.timer[ch] = null;
	
				if( ctc.int_state[ch] != 0 )
				{
					/* clear interrupt service , request */
					ctc.int_state[ch] = 0;
					z80ctc_interrupt_check( ctc );
				}
			}
	
			/* all done here */
			return;
		}
	}
	
	public static WriteHandlerPtr z80ctc_0_w = new WriteHandlerPtr() {public void handler(int offset, int data) { z80ctc_w (0, offset, data); } };
	public static WriteHandlerPtr z80ctc_1_w = new WriteHandlerPtr() {public void handler(int offset, int data) { z80ctc_w (1, offset, data); } };
	
	
	public static int z80ctc_r (int which, int ch)
	{
		z80ctc ctc = ctcs[which];
		int mode;
	
		/* keep channel within range */
		ch &= 3;
		mode = ctc.mode[ch];
	
		/* if we're in counter mode, just return the count */
		if ((mode & MODE) == MODE_COUNTER)
			return ctc.down[ch];
	
		/* else compute the down counter value */
		else
		{
			double clock = ((mode & PRESCALER) == PRESCALER_16) ? ctc.invclock16 : ctc.invclock256;
	
                        logerror("CTC clock %f\n",1.0/clock);
	
	
			if (ctc.timer[ch] != null)
				return ((int)(timer_timeleft (ctc.timer[ch]) / clock) + 1) & 0xff;
			else
				return 0;
		}
	}
	
	public static ReadHandlerPtr z80ctc_0_r  = new ReadHandlerPtr() { public int handler(int offset) { return z80ctc_r (0, offset); } };
	public static ReadHandlerPtr z80ctc_1_r  = new ReadHandlerPtr() { public int handler(int offset) { return z80ctc_r (1, offset); } };
	
	
	public static DaisyChainInterruptEntryPtr z80ctc_interrupt = new DaisyChainInterruptEntryPtr() {
            public int handler(int which) {
                z80ctc ctc = ctcs[which];
		int ch;
	
		for( ch = 0 ; ch < 4 ; ch++ )
		{
			if( ctc.int_state[ch] != 0)
			{
				if( ctc.int_state[ch] == Z80_INT_REQ)
					ctc.int_state[ch] = Z80_INT_IEO;
				break;
			}
		}
		if( ch > 3 )
		{
			logerror("CTC entry INT : non IRQ\n");
			ch = 0;
		}
		z80ctc_interrupt_check( ctc );
		return ctc.vector + ch * 2;
            }
        };
	
	/* when operate RETI , soud be call this function for request pending interrupt */
	public static DaisyChainInterruptRetiPtr z80ctc_reti = new DaisyChainInterruptRetiPtr() {
            public void handler(int which) {
                z80ctc ctc = ctcs[which];
		int ch;
	
		for( ch = 0 ; ch < 4 ; ch++ )
		{
			if(( ctc.int_state[ch] & Z80_INT_IEO ) != 0)
			{
				/* highest served interrupt found */
				/* clear interrupt status */
				ctc.int_state[ch] &= ~Z80_INT_IEO;
				/* search next interrupt */
				break;
			}
		}
		/* set next interrupt stattus */
		z80ctc_interrupt_check( ctc );
            }
        };
	
	public static timer_callback z80ctc_timercallback = new timer_callback() {
            public void handler(int param) {

		int which = param >> 2;
		int ch = param & 3;
		z80ctc ctc = ctcs[which];
	
		/* down counter has reached zero - see if we should interrupt */
		if ((ctc.mode[ch] & INTERRUPT) == INTERRUPT_ON)
		{
			if( (ctc.int_state[ch] & Z80_INT_REQ) == 0 )
			{
				ctc.int_state[ch] |= Z80_INT_REQ;
				z80ctc_interrupt_check( ctc );
			}
		}
		/* generate the clock pulse */
		if (ctc.zc[ch] != null)
		{
			(ctc.zc[ch]).handler(0,1);
			(ctc.zc[ch]).handler(0,0);
		}
	
		/* reset the down counter */
		ctc.down[ch] = ctc.tconst[ch];
            }
        };


	public static void z80ctc_trg_w (int which, int trg, int offset, int data)
	{
		z80ctc ctc = ctcs[which];
		int ch = trg & 3;
		int mode;
	
		data = data!=0 ? 1 : 0;
		mode = ctc.mode[ch];
	
		/* see if the trigger value has changed */
		if (data != ctc.extclk[ch])
		{
			ctc.extclk[ch] = data;
	
			/* see if this is the active edge of the trigger */
			if (((mode & EDGE) == EDGE_RISING && data!=0) || ((mode & EDGE) == EDGE_FALLING && (data==0)))
			{
				/* if we're waiting for a trigger, start the timer */
				if (((mode & WAITING_FOR_TRIG)!=0) && (mode & MODE) == MODE_TIMER)
				{
					double clock = ((mode & PRESCALER) == PRESCALER_16) ? ctc.invclock16 : ctc.invclock256;
	
                                        logerror("CTC clock %f\n",1.0/clock);
	
	
					if (ctc.timer[ch] != null)
						timer_remove (ctc.timer[ch]);
					if ((ctc.notimer & (1<<ch)) == 0)
						ctc.timer[ch] = timer_pulse (clock * (double)ctc.tconst[ch], (which << 2) + ch, z80ctc_timercallback);
				}
	
				/* we're no longer waiting */
				ctc.mode[ch] &= ~WAITING_FOR_TRIG;
	
				/* if we're clocking externally, decrement the count */
				if ((mode & MODE) == MODE_COUNTER)
				{
					ctc.down[ch]--;
	
					/* if we hit zero, do the same thing as for a timer interrupt */
					if (ctc.down[ch] == 0)
						z80ctc_timercallback.handler((which << 2) + ch);
				}
			}
		}
	}
	
/*TODO*///	public static WriteHandlerPtr z80ctc_0_trg0_w = new WriteHandlerPtr() {public void handler(int offset, int data) { z80ctc_trg_w (0, 0, offset, data); } };
	public static WriteHandlerPtr z80ctc_0_trg1_w = new WriteHandlerPtr() {public void handler(int offset, int data) { z80ctc_trg_w (0, 1, offset, data); } };
	public static WriteHandlerPtr z80ctc_0_trg2_w = new WriteHandlerPtr() {public void handler(int offset, int data) { z80ctc_trg_w (0, 2, offset, data); } };
	public static WriteHandlerPtr z80ctc_0_trg3_w = new WriteHandlerPtr() {public void handler(int offset, int data) { z80ctc_trg_w (0, 3, offset, data); } };
/*TODO*///	public static WriteHandlerPtr z80ctc_1_trg0_w = new WriteHandlerPtr() {public void handler(int offset, int data) { z80ctc_trg_w (1, 0, offset, data); } };
/*TODO*///	public static WriteHandlerPtr z80ctc_1_trg1_w = new WriteHandlerPtr() {public void handler(int offset, int data) { z80ctc_trg_w (1, 1, offset, data); } };
/*TODO*///	public static WriteHandlerPtr z80ctc_1_trg2_w = new WriteHandlerPtr() {public void handler(int offset, int data) { z80ctc_trg_w (1, 2, offset, data); } };
	public static WriteHandlerPtr z80ctc_1_trg3_w = new WriteHandlerPtr() {public void handler(int offset, int data) { z80ctc_trg_w (1, 3, offset, data); } };
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------- Z80 PIO ---------------------------------*/
/*TODO*///	
/*TODO*///	/* starforce emurate Z80PIO subset */
/*TODO*///	/* ch.A mode 1 input handshake mode from sound command */
/*TODO*///	/* ch.b not use */
/*TODO*///	
/*TODO*///	
        public static final int PIO_MODE0 = 0x00;		/* output mode */
        public static final int PIO_MODE1 = 0x01;		/* input  mode */
        public static final int PIO_MODE2 = 0x02;		/* i/o    mode */
        public static final int PIO_MODE3 = 0x03;		/* bit    mode */
/*TODO*///	/* pio controll port operation (bit 0-3) */
        public static final int PIO_OP_MODE = 0x0f;	/* mode select        */
        public static final int PIO_OP_INTC = 0x07;	/* interrupt controll */
        public static final int PIO_OP_INTE = 0x03;	/* interrupt enable   */
/*TODO*///	#define PIO_OP_INTE 0x03	/* interrupt enable   */
/*TODO*///	/* pio interrupt controll nit */
        public static final int PIO_INT_ENABLE = 0x80;  /* ENABLE : 0=disable , 1=enable */
        public static final int PIO_INT_AND    = 0x40;  /* LOGIC  : 0=OR      , 1=AND    */
        public static final int PIO_INT_HIGH   = 0x20;  /* LEVEL  : 0=low     , 1=high   */
        public static final int PIO_INT_MASK   = 0x10;  /* MASK   : 0=off     , 1=on     */
	
	public static class z80pio
	{
		int[] vector=new int[2];                        /* interrupt vector               */
		DaisyChainInterruptRetiPtr intr;              /* interrupt callbacks            */
		DaisyChainInterruptRetiPtr[] rdyr = new DaisyChainInterruptRetiPtr[2];            /* RDY active callback            */
		int[] mode=new int[2];                          /* mode 00=in,01=out,02=i/o,03=bit*/
		int[] enable=new int[2];                        /* interrupt enable               */
		int[] mask=new int[2];                          /* mask folowers                  */
		int[] dir=new int[2];                           /* direction (bit mode)           */
		int[] rdy=new int[2];                           /* ready pin level                */
		int[] in=new int[2];                            /* input port data                */
		int[] out=new int[2];                           /* output port                    */
		int[] strobe=new int[2];							/* strobe inputs */
		int[] int_state=new int[2];                     /* interrupt status (daisy chain) */
	};
	
        static z80pio[] pios = new z80pio[MAX_PIO];

	static z80pio z80pio_set_rdy(z80pio pio, int ch, int state)
	{
		/* set state */
		pio.rdy[ch] = state;
	
		/* call callback with state */
		if (pio.rdyr[ch]!=null)
			pio.rdyr[ch].handler(pio.rdy[ch]);
                        
                return pio;
	}
	
	/* initialize pio emurator */
	public static void z80pio_init (z80pio_interface intf)
	{
		int i;
	
		//memset (pios, 0, sizeof (pios));
                pios = new z80pio[MAX_PIO];
	
		for (i = 0; i < intf.num; i++)
		{
                        pios[i] = new z80pio();
                        
			pios[i].intr = intf.intr[i];
			pios[i].rdyr[0] = intf.rdyA[i];
			pios[i].rdyr[1] = intf.rdyB[i];
			z80pio_reset.handler(i);
		}
	}
	
	static z80pio z80pio_interrupt_check( z80pio pio )
	{
		int state;
	
		if(( pio.int_state[1] & Z80_INT_IEO ) != 0) state  = Z80_INT_IEO;
		else                                  state  = pio.int_state[1];
		if(( pio.int_state[0] & Z80_INT_IEO ) != 0) state  = Z80_INT_IEO;
		else                                  state |= pio.int_state[0];
		/* change daisy chain status */
		if (pio.intr != null) pio.intr.handler(state);
                
                return pio;
	}
	
	static z80pio z80pio_check_irq( z80pio pio , int ch )
	{
		int irq = 0;
		int data;
		int old_state;
	
		if(( pio.enable[ch] & PIO_INT_ENABLE ) != 0)
		{
			if( pio.mode[ch] == PIO_MODE3 )
			{
				data  =  pio.in[ch] & pio.dir[ch]; /* input data only */
				data &= ~pio.mask[ch];              /* mask follow     */
				if( (pio.enable[ch]&PIO_INT_HIGH) == 0 )/* active level    */
					data ^= pio.mask[ch];             /* active low  */
				if(( pio.enable[ch]&PIO_INT_AND ) != 0)    /* logic      */
				     { if( data == pio.mask[ch] ) irq = 1; }
				else { if( data == 0             ) irq = 1; }
				/* if portB , portA mode 2 check */
				if( ch!=0 && (pio.mode[0]==PIO_MODE2) )
				{
					if( pio.rdy[ch] == 0 ) irq = 1;
				}
			}
			else if( pio.rdy[ch] == 0 ) irq = 1;
		}
		old_state = pio.int_state[ch];
		if( irq!=0 ) pio.int_state[ch] |=  Z80_INT_REQ;
		else      pio.int_state[ch] &= ~Z80_INT_REQ;
	
		if( old_state != pio.int_state[ch] )
			pio = z80pio_interrupt_check( pio );
                
                return pio;
	}
	
	public static DaisyChainResetPtr z80pio_reset = new DaisyChainResetPtr() {
            public void handler(int which) {
                z80pio pio = pios[which];
		int i;
	
		for( i = 0 ; i <= 1 ; i++){
			pio.mask[i]   = 0xff;	/* mask all on */
			pio.enable[i] = 0x00;	/* disable     */
			pio.mode[i]   = 0x01;	/* mode input  */
			pio.dir[i]    = 0x01;	/* dir  input  */
			pio = z80pio_set_rdy(pio,i,0);	/* RDY = low   */
			pio.out[i]    = 0x00;	/* outdata = 0 */
			pio.int_state[i] = 0;
			pio.strobe[i] = 0;
		}
		pio = z80pio_interrupt_check( pio );
                
                pios[which] = pio;
            }
        };
            
	/* pio data register write */
	public static void z80pio_d_w( int which , int ch , int data )
	{
		z80pio pio = pios[which];
		if( ch != 0 ) ch = 1;
	
		pio.out[ch] = data;	/* latch out data */
		switch( pio.mode[ch] ){
		case PIO_MODE0:			/* mode 0 output */
		case PIO_MODE2:			/* mode 2 i/o */
			pio = z80pio_set_rdy(pio, ch,1); /* ready = H */
			pio = z80pio_check_irq( pio , ch );
			return;
		case PIO_MODE1:			/* mode 1 intput */
		case PIO_MODE3:			/* mode 0 bit */
			return;
		default:
			logerror("PIO-%c data write,bad mode\n",'A'+ch );
		}
                
                pios[which] = pio;
	}
	
	/* pio controll register write */
	public static void z80pio_c_w( int which , int ch , int data )
	{
		z80pio pio = pios[which];
		if( ch!=0 ) ch = 1;
	
		/* load direction phase ? */
		if( pio.mode[ch] == 0x13 ){
			pio.dir[ch] = data;
			pio.mode[ch] = 0x03;
			return;
		}
		/* load mask folows phase ? */
		if(( pio.enable[ch] & PIO_INT_MASK ) != 0){	/* load mask folows */
			pio.mask[ch] = data;
			pio.enable[ch] &= ~PIO_INT_MASK;
			logerror("PIO-%c interrupt mask %02x\n",'A'+ch,data );
			return;
		}
		switch( data & 0x0f ){
		case PIO_OP_MODE:	/* mode select 0=out,1=in,2=i/o,3=bit */
			pio.mode[ch] = (data >> 6 );
			if( pio.mode[ch] == 0x03 ) pio.mode[ch] = 0x13;
			logerror("PIO-%c Mode %x\n",'A'+ch,pio.mode[ch] );
			break;
		case PIO_OP_INTC:		/* interrupt control */
			pio.enable[ch] = data & 0xf0;
			pio.mask[ch]   = 0x00;
			/* when interrupt enable , set vector request flag */
			logerror("PIO-%c Controll %02x\n",'A'+ch,data );
			break;
		case PIO_OP_INTE:		/* interrupt enable controll */
			pio.enable[ch] &= ~PIO_INT_ENABLE;
			pio.enable[ch] |= (data & PIO_INT_ENABLE);
			logerror("PIO-%c enable %02x\n",'A'+ch,data&0x80 );
			break;
		default:
				if( (data&1) == 0 )
				{
					pio.vector[ch] = data;
					logerror("PIO-%c vector %02x\n",'A'+ch,data);
				}
				else logerror("PIO-%c illegal command %02x\n",'A'+ch,data );
		}
		/* interrupt check */
		pio = z80pio_check_irq( pio , ch );
                
                pios[which] = pio;
	}
	
	/* pio controll register read */
	public static int z80pio_c_r( int which , int ch )
	{
		if( ch != 0 ) ch = 1;
	
		logerror("PIO-%c controll read\n",'A'+ch );
		return 0;
	}
	
	/* pio data register read */
	public static int z80pio_d_r( int which , int ch )
	{
		z80pio pio = pios[which];
		if( ch != 0 ) ch = 1;
	
		switch( pio.mode[ch] ){
		case PIO_MODE0:			/* mode 0 output */
			return pio.out[ch];
		case PIO_MODE1:			/* mode 1 intput */
			pio = z80pio_set_rdy(pio, ch, 1);	/* ready = H */
			pio = z80pio_check_irq( pio , ch );
			return pio.in[ch];
		case PIO_MODE2:			/* mode 2 i/o */
			if( ch!=0 ) logerror("PIO-B mode 2 \n");
			pio = z80pio_set_rdy(pio, 1, 1); /* brdy = H */
			pio = z80pio_check_irq( pio , ch );
			return pio.in[ch];
		case PIO_MODE3:			/* mode 3 bit */
			return (pio.in[ch]&pio.dir[ch])|(pio.out[ch]&~pio.dir[ch]);
		}
		logerror("PIO-%c data read,bad mode\n",'A'+ch );
                
                pios[which] = pio;
                
		return 0;
	}
	
	public static DaisyChainInterruptEntryPtr z80pio_interrupt = new DaisyChainInterruptEntryPtr() {
            public int handler( int which ) {
                z80pio pio = pios[which];
		int ch = 0;
	
		/* port A */
		if( pio.int_state[0] == Z80_INT_REQ )
		{
			pio.int_state[0] |= Z80_INT_IEO;
		} if( pio.int_state[0] == 0 )
		{
			/* port B */
			ch = 1;
			if( pio.int_state[1] == Z80_INT_REQ )
			{
				pio.int_state[1] |= Z80_INT_IEO;
			}
			else
			{
				logerror("PIO entry INT : non IRQ\n");
				ch = 0;
			}
		}
		pio = z80pio_interrupt_check( pio );
                pios[which] = pio;
                
		return pio.vector[ch];
            }
        };
	
	public static DaisyChainInterruptRetiPtr z80pio_reti = new DaisyChainInterruptRetiPtr() {
            public void handler(int which) {
                z80pio pio = pios[which];
	
		if(( pio.int_state[0] & Z80_INT_IEO ) != 0)
		{
			pio.int_state[0] &= ~Z80_INT_IEO;
		} else if(( pio.int_state[1] & Z80_INT_IEO ) != 0)
		{
			pio.int_state[1] &= ~Z80_INT_IEO;
		}
		/* set next interrupt stattus */
		pio = z80pio_interrupt_check( pio );
                
                pios[which] = pio;
            }
        };
	
	/* z80pio port write */
	public static void z80pio_p_w( int which , int ch , int data )
	{
		z80pio pio = pios[which];
	
		if( ch!=0 ) ch = 1;
	
		pio.in[ch]  = data;
		switch( pio.mode[ch] ){
		case PIO_MODE0:
			logerror("PIO-%c OUTPUT mode and data write\n",'A'+ch );
			break;
		case PIO_MODE2:	/* only port A */
			ch = 1;		/* handshake and IRQ is use portB */
		case PIO_MODE1:
			pio = z80pio_set_rdy(pio, ch, 0);
			pio = z80pio_check_irq( pio , ch );
			break;
		case PIO_MODE3:
			/* irq check */
			pio = z80pio_check_irq( pio , ch );
			break;
		}
                
                pios[which] = pio;
	}
	
/*TODO*///	/* z80pio port read */
/*TODO*///	int z80pio_p_r( int which , int ch )
/*TODO*///	{
/*TODO*///		z80pio *pio = pios + which;
/*TODO*///	
/*TODO*///		if( ch ) ch = 1;
/*TODO*///	
/*TODO*///		switch( pio.mode[ch] ){
/*TODO*///		case PIO_MODE2:		/* port A only */
/*TODO*///		case PIO_MODE0:
/*TODO*///			z80pio_set_rdy(pio, ch, 0);
/*TODO*///			z80pio_check_irq( pio , ch );
/*TODO*///			break;
/*TODO*///		case PIO_MODE1:
/*TODO*///			logerror("PIO-%c INPUT mode and data read\n",'A'+ch );
/*TODO*///			break;
/*TODO*///		case PIO_MODE3:
/*TODO*///			/*     input bits                , output bits                */
/*TODO*///			return (pio.in[ch]&pio.dir[ch])|(pio.out[ch]&~pio.dir[ch]);
/*TODO*///		}
/*TODO*///		return pio.out[ch];
/*TODO*///	}
	
	/* for mame interface */
	
	public static void z80pio_0_reset () { z80pio_reset.handler(0); }
	
	public static WriteHandlerPtr z80pio_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if((offset&1)!=0) z80pio_c_w(0,(offset/2)&1,data);
		else         z80pio_d_w(0,(offset/2)&1,data);
	} };
	
	public static ReadHandlerPtr z80pio_0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return (offset&1)!=0 ? z80pio_c_r(0,(offset/2)&1) : z80pio_d_r(0,(offset/2)&1);
	} };
	
    public static WriteHandlerPtr z80pioA_0_p_w = new WriteHandlerPtr() {public void handler(int offset, int data) { z80pio_p_w(0,0,data);   } };
/*TODO*///	public static WriteHandlerPtr z80pioB_0_p_w = new WriteHandlerPtr() {public void handler(int offset, int data) { z80pio_p_w(0,1,data);   } };
/*TODO*///	public static ReadHandlerPtr z80pioA_0_p_r  = new ReadHandlerPtr() { public int handler(int offset)           { return z80pio_p_r(0,0); } };
/*TODO*///	public static ReadHandlerPtr z80pioB_0_p_r  = new ReadHandlerPtr() { public int handler(int offset)           { return z80pio_p_r(0,1); } };
/*TODO*///	
/*TODO*///	static void z80pio_update_strobe(int which, int ch, int state)
/*TODO*///	{
/*TODO*///		z80pio *pio = pios + which;
/*TODO*///	
/*TODO*///		if (ch) ch=1;
/*TODO*///	
/*TODO*///		switch (pio.mode[ch])
/*TODO*///		{
/*TODO*///			/* output mode */
/*TODO*///			case PIO_MODE0:
/*TODO*///			{
/*TODO*///				/* ensure valid */
/*TODO*///				state = state & 0x01;
/*TODO*///	
/*TODO*///				/* strobe changed state? */
/*TODO*///				if ((pio.strobe[ch]^state)!=0)
/*TODO*///				{
/*TODO*///					/* yes */
/*TODO*///					if (state!=0)
/*TODO*///					{
/*TODO*///						/* positive edge */
/*TODO*///						logerror("PIO-%c positive strobe\n",'A'+ch );
/*TODO*///						/* ready is now inactive */
/*TODO*///						z80pio_set_rdy(pio, ch, 0);
/*TODO*///	
/*TODO*///						/* int enabled? */
/*TODO*///						if (pio.enable[ch] & PIO_INT_ENABLE)
/*TODO*///						{
/*TODO*///							/* trigger an int request */
/*TODO*///							pio.int_state[ch] |= Z80_INT_REQ;
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///	
/*TODO*///				/* store strobe state */
/*TODO*///				pio.strobe[ch] = state;
/*TODO*///	
/*TODO*///				/* check interrupt */
/*TODO*///				z80pio_interrupt_check( pio );
/*TODO*///			}
/*TODO*///			break;
/*TODO*///	
/*TODO*///			default:
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* set /astb input */
/*TODO*///	/* /astb is active low */
/*TODO*///	/* output mode: a positive edge is used by peripheral to acknowledge
/*TODO*///	the receipt of data */
/*TODO*///	/* input mode: strobe is used by peripheral to load data from the peripheral
/*TODO*///	into port a input register, data loaded into pio when signal is active */
/*TODO*///	void	z80pio_astb_w(int which, int state)
/*TODO*///	{
/*TODO*///		z80pio_update_strobe(which, 0, state);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* set bstb input */
/*TODO*///	void	z80pio_bstb_w(int which, int state)
/*TODO*///	{
/*TODO*///		z80pio_update_strobe(which, 1, state);
/*TODO*///	}
}

