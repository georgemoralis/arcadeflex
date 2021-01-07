package gr.codebb.arcadeflex.v058.machine;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.*;

public class z80fmlyH {
    /*  Z80 FMLY.H   Z80 FAMILY IC EMURATION */
    public static int MAX_CTC = 2;

    public static int NOTIMER_0 = (1<<0);
    public static int NOTIMER_1 = (1<<1);
    public static int NOTIMER_2 = (1<<2);
    public static int NOTIMER_3 = (1<<3);
    
    public static abstract interface IntrPtr {
        public abstract void handler(int which);
    }

    public static class z80ctc_interface
    {
            public int num;                                      /* number of CTCs to emulate */
            public int[] baseclock=new int[MAX_CTC];                           /* timer clock */
            public int[] notimer=new int[MAX_CTC];                         /* timer disablers */
            public IntrPtr[] intr=new IntrPtr[MAX_CTC];             /* callback when change interrupt status */
            public WriteHandlerPtr[] zc0=new WriteHandlerPtr[MAX_CTC];   /* ZC/TO0 callback */
            public WriteHandlerPtr[] zc1=new WriteHandlerPtr[MAX_CTC];   /* ZC/TO1 callback */
            public WriteHandlerPtr[] zc2=new WriteHandlerPtr[MAX_CTC];   /* ZC/TO2 callback */

        public z80ctc_interface(int i, int[] i0, int[] i1, IntrPtr[] interruptPtr, WriteHandlerPtr[] writeHandlerPtr, WriteHandlerPtr[] writeHandlerPtr0, WriteHandlerPtr[] writeHandlerPtr1) {
            num = i;
            baseclock = i0;
            notimer = i1;
            intr = interruptPtr;
            zc0 = writeHandlerPtr;
            zc1 = writeHandlerPtr0;
            zc2 = writeHandlerPtr1;
        }
    };

/*TODO*///void z80ctc_init (z80ctc_interface *intf);
/*TODO*///
/*TODO*///double z80ctc_getperiod (int which, int ch);
/*TODO*///
/*TODO*///void z80ctc_reset (int which);
/*TODO*///
/*TODO*///void z80ctc_w (int which, int offset, int data);
/*TODO*///
/*TODO*///int z80ctc_r (int which, int offset);
/*TODO*///
/*TODO*///void z80ctc_trg_w (int which, int trg, int offset, int data);
/*TODO*///
/*TODO*////* Z80 DaisyChain controll */
/*TODO*///int z80ctc_interrupt( int which );
/*TODO*///void z80ctc_reti( int which );
/*TODO*////*--------------------------------------------------------------------*/
    public static final int MAX_PIO = 1;

    public static class z80pio_interface
    {
            int num;                                      /* number of PIOs to emulate */
            Interrupt_retiPtr[] intr = new Interrupt_retiPtr[MAX_CTC];             /* callback when change interrupt status */
            Interrupt_retiPtr[] rdyA = new Interrupt_retiPtr[MAX_PIO];             /* portA ready active callback (do not support yet)*/
            Interrupt_retiPtr[] rdyB = new Interrupt_retiPtr[MAX_PIO];             /* portB ready active callback (do not support yet)*/
            
            public z80pio_interface(int num, Interrupt_retiPtr[] intr, Interrupt_retiPtr[] rdyA, Interrupt_retiPtr[] rdyB){
                this.num = num;
                this.intr = intr;
                this.rdyA = rdyA;
                this.rdyB = rdyB;
            }
    };


/*TODO*///void z80pio_init (z80pio_interface *intf);
/*TODO*///void z80pio_reset (int which);
/*TODO*///void z80pio_d_w( int which , int ch , int data );
/*TODO*///void z80pio_c_w( int which , int ch , int data );
/*TODO*///int z80pio_c_r( int which , int ch );
/*TODO*///int z80pio_d_r( int which , int ch );
/*TODO*///
/*TODO*////* set/clear /astb input */
/*TODO*///void	z80pio_astb_w(int which, int state);
/*TODO*////* set/clear /bstb input */
/*TODO*///void	z80pio_bstb_w(int which, int state);
/*TODO*///
/*TODO*///void z80pio_p_w( int which , int ch , int data );
/*TODO*///int z80pio_p_r( int which , int ch );
/*TODO*///
/*TODO*////* Z80 DaisyChain controll */
/*TODO*///int z80pio_interrupt( int which );
/*TODO*///void z80pio_reti( int which );
/*TODO*///
/*TODO*////* mame interface */
/*TODO*///
/*TODO*////* this functions can use when C/D = A0 , A/B = A1 */
/*TODO*///
/*TODO*///
/*TODO*///

}
