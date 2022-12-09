/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.sndhrdw;
//generic imports

import static arcadeflex.v036.generic.funcPtr.*;
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static arcadeflex.v036.cpu.z80.z80H.Z80_NMI_INT;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.cpu_cause_interrupt;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.cpu_get_pc;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.cpu_set_reset_line;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.cpu_spin;
import static arcadeflex.v036.mame.cpuintrfH.ASSERT_LINE;
import static arcadeflex.v036.mame.cpuintrfH.CLEAR_LINE;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static arcadeflex.v036.sound.adpcm.ADPCM_play;
import static gr.codebb.arcadeflex.v036.platform.osdepend.*;

public class rastan {

    /**
     * ********************************************************************************************
     * Soundboard Status bitfield definition: bit meaning 0 Set if theres any
     * data pending that the main cpu sent to the slave 1 ??? ( Its not being
     * checked both on Rastan and SSI 2 Set if theres any data pending that the
     * slave sent to the main cpu
     *
     ***********************************************************************************************
     *
     * It seems like 1 nibble commands are only for control purposes. 2 nibble
     * commands are the real messages passed from one board to the other.
     *
     *********************************************************************************************
     */
    static int nmi_enabled = 0;/* interrupts off */

 /* status of soundboard ( reports any commands pending ) */
    static int u8_SlaveContrStat = 0;

    static int transmit = 0;/* number of bytes to transmit/receive */
    static int tr_mode;/* transmit mode (1 or 2 bytes) */
    static int lasthalf = 0;/* in 2 bytes mode this is first nibble (LSB bits 0,1,2,3) received */

    static int m_transmit = 0;/* as above but for motherboard*/
    static int m_tr_mode;/* as above */
    static int m_lasthalf = 0;/* as above */

    static int IRQ_req = 0;/*no request*/
    static int NMI_req = 0;/*no request*/

    static int u8_soundcommand;
    static int u8_soundboarddata;

    /**
     * ********************************************************************
     */
    /*  looking from sound board point of view ...                         */
    /**
     * ********************************************************************
     */
    public static WriteHandlerPtr rastan_c000_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) /* Meaning of this address is unknown !!! */ {
        }
    };

    public static WriteHandlerPtr rastan_d000_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) /* ADPCM chip used in Rastan does not stop playing the sample by itself
	** it must be said to stop instead. This is the address that does it.
         */ {
            if (Machine.samples == null) {
                return;
            }
            /*#if 0
		if (data==0)
			mixer_stop_sample(channel);
	#endif*/
        }
    };

    public static void Interrupt_Controller() {
        if (IRQ_req != 0) {
            cpu_cause_interrupt(1, 0);
            /* IRQ_req = 0; */
        }

        if (NMI_req != 0 && nmi_enabled != 0) {
            cpu_cause_interrupt(1, Z80_NMI_INT);
            NMI_req = 0;
        }
    }
    static int u8_pom = 0;
    public static ReadHandlerPtr rastan_a001_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (transmit == 0) {
                logerror("Slave unexpected receiving! (PC = %04x)\n", cpu_get_pc());
            } else {
                if (tr_mode == 1) {
                    u8_pom = u8_SlaveContrStat & 0xFF;
                } else {
                    /*2-bytes transmision*/
                    if (transmit == 2) {
                        u8_pom = u8_soundcommand & 0x0f;
                    } else {
                        u8_pom = (u8_soundcommand & 0xf0) >> 4;
                        u8_SlaveContrStat &= 0xfe;/* Ready to receive new commands */
                    }
                }
                transmit--;
            }

            Interrupt_Controller();

            return u8_pom & 0xFF;
        }
    };

    public static WriteHandlerPtr rastan_a000_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int pom;

            if (transmit != 0) {
                logerror("Slave mode changed while expecting to transmit! (PC = %04x) \n", cpu_get_pc());
            }

            pom = (data >> 2) & 0x01;
            transmit = 1 + (1 - pom);
            /* one or two bytes long transmission */
            lasthalf = 0;
            tr_mode = transmit;

            pom = data & 0x03;
            if (pom == 0x01) {
                nmi_enabled = 0;/* off */
            }

            if (pom == 0x02) {
                nmi_enabled = 1;/* on */
            }

            if (pom == 0x03) {
                logerror("Int mode = 3! (PC = %04x)\n", cpu_get_pc());
            }
        }
    };

    public static WriteHandlerPtr rastan_a001_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            data &= 0x0f;

            if (transmit == 0) {
                logerror("Slave unexpected transmission! (PC = %04x)\n", cpu_get_pc());
            } else {
                if (transmit == 2) {
                    lasthalf = data;
                }
                transmit--;
                if (transmit == 0) {
                    if (tr_mode == 2) {
                        u8_soundboarddata = (lasthalf + (data << 4)) & 0xFF;
                        u8_SlaveContrStat |= 4;/* report data pending on main */
                        cpu_spin();/* writing should take longer than emulated, so spin */
                    } else {

                    }
                }
            }

            Interrupt_Controller();
        }
    };

    public static WriteHandlerPtr rastan_adpcm_trigger_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr rom = memory_region(REGION_SOUND1);
            int len = memory_region_length(REGION_SOUND1);
            int start = data << 8;
            int end;

            /* look for end of sample */
            end = (start + 3) & ~3;
            while (end < len && rom.READ_DWORD(end) != 0x08080808) {
                end += 4;
            }

            ADPCM_play(0, start, (end - start) * 2);
        }
    };

    public static WriteYmHandlerPtr rastan_irq_handler = new WriteYmHandlerPtr() {
        public void handler(int irq) {
            IRQ_req = irq;
        }
    };

    /**
     * ********************************************************************
     */
    /*  now looking from main board point of view                          */
    /**
     * ********************************************************************
     */
    public static WriteHandlerPtr rastan_sound_port_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int pom;

            if ((data & 0xff) != 0x01) {
                pom = (data >> 2) & 0x01;
                m_transmit = 1 + (1 - pom);/* one or two bytes long transmission */
                m_lasthalf = 0;
                m_tr_mode = m_transmit;
            } else {
                if (m_transmit == 1) {
                    /*logerror("single-doubled (first was=%02x)\n",m_lasthalf);*/
                } else {
                    logerror("rastan_sound_port_w() - unknown innerworking\n");
                }
            }
        }
    };

    public static WriteHandlerPtr rastan_sound_comm_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            data &= 0x0f;

            if (m_transmit == 0) {
                logerror("Main unexpected transmission! (PC = %08x)\n", cpu_get_pc());
            } else {
                if (m_transmit == 2) {
                    m_lasthalf = data;
                }

                m_transmit--;

                if (m_transmit == 0) {
                    if (m_tr_mode == 2) {
                        u8_soundcommand = (m_lasthalf + (data << 4)) & 0xFF;
                        u8_SlaveContrStat |= 1;/* report data pending for slave */
                        NMI_req = 1;
                    } else {
                        /* this does a hi-lo transition to reset the sound cpu */
                        if (data != 0) {
                            cpu_set_reset_line(1, ASSERT_LINE);
                        } else {
                            cpu_set_reset_line(1, CLEAR_LINE);
                        }

                        m_transmit++;
                    }
                }
            }
        }
    };

    public static ReadHandlerPtr rastan_sound_comm_r = new ReadHandlerPtr() {
        public int handler(int offset) {

            m_transmit--;
            if (m_tr_mode == 2) {
                u8_SlaveContrStat &= 0xfb;/* clear pending data for main bit */

                if (m_transmit == 1) {
                    return u8_soundboarddata & 0x0f;
                }

                return (u8_soundboarddata >> 4) & 0x0f;

            } else {
                m_transmit++;
                return u8_SlaveContrStat & 0xFF;
            }
        }
    };

    public static WriteHandlerPtr rastan_sound_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset == 0) {
                rastan_sound_port_w.handler(0, data & 0xff);
            } else if (offset == 2) {
                rastan_sound_comm_w.handler(0, data & 0xff);
            }
        }
    };

    public static ReadHandlerPtr rastan_sound_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (offset == 2) {
                return rastan_sound_comm_r.handler(0);
            } else {
                return 0;
            }
        }
    };
}
