/*
 * 
 * 
 */
package gr.codebb.arcadeflex.v036.machine;

import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.machine.eepromH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.platform.fileio.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;

public class eeprom 
{
    public static FILE eepromlog = null;//fopen("eeprom.log", "wa");  //for debug purposes
    public static final int SERIAL_BUFFER_LENGTH =30;

    static EEPROM_interface intf;

    static int serial_count;
    static char[] serial_buffer=new char[SERIAL_BUFFER_LENGTH];
    static /*unsigned*/ char[] eeprom_data=new char[256];
    static int eeprom_data_bits;
    static int latch,reset_line,clock_line,sending;
    static int locked;


    public static void EEPROM_init(EEPROM_interface intrface)
    {
            intf = intrface;
            for (int i = 0; i < (1 << intf.address_bits) * intf.data_bits / 8; i++)
                eeprom_data[i] = 0xff; //memset(eeprom_data,0xff,(1 << intf.address_bits) * intf.data_bits / 8);
            serial_count = 0;
            latch = 0;
            reset_line = ASSERT_LINE;
            clock_line = ASSERT_LINE;
            sending = 0;
            if (intf.cmd_unlock!=null) locked = 1;
            else locked = 0;
    }

    public static void EEPROM_write(int bit)
    {

    if (eepromlog!=null) fprintf(eepromlog,"EEPROM write bit %d\n",bit);


            if (serial_count >= SERIAL_BUFFER_LENGTH-1)
            {
                    if (eepromlog!=null) fprintf(eepromlog,"error: EEPROM serial buffer overflow\n");
                    return;
            }

            serial_buffer[serial_count++] = (bit!=0 ? '1' : '0');
            serial_buffer[serial_count] ='\0';	/* nul terminate so we can treat it as a string */

            if (eepromlog!=null) fprintf(eepromlog,"EEPROM write bit, buffer = " + new String(serial_buffer)+"\n");
            if (eepromlog!=null) fprintf(eepromlog,"EEPROM write bit, serial_count = %d\n",serial_count);
            if ((intf.cmd_read!=null) 
                    && (serial_count == (strlen(intf.cmd_read) + intf.address_bits))
                    && !strncmp(serial_buffer,intf.cmd_read,strlen(intf.cmd_read)))
            {
                    int i,address;
                    address = 0;
                    for (i = 0;i < intf.address_bits;i++)
                    {
                            address <<= 1;
                            if (serial_buffer[i + strlen(intf.cmd_read)] == '1') address |= 1;
                    }
                    if (intf.data_bits == 16)
                            eeprom_data_bits = (((eeprom_data[2*address+0] << 8)&0xFF) + ((eeprom_data[2*address+1])&0xFF));
                    else
                            eeprom_data_bits = eeprom_data[address] &0xFF;
                    sending = 1;
                    serial_count = 0;
                    if (eepromlog!=null) fprintf(eepromlog,"EEPROM read %04x from address %02x\n",eeprom_data_bits,address);
            }
            else if (intf.cmd_erase!=null && serial_count == (strlen(intf.cmd_erase) + intf.address_bits) &&
                            !strncmp(serial_buffer,intf.cmd_erase,strlen(intf.cmd_erase)))
            {
                    int i,address;

                    address = 0;
                    for (i = 0;i < intf.address_bits;i++)
                    {
                            address <<= 1;
                            if (serial_buffer[i + strlen(intf.cmd_erase)] == '1') address |= 1;
                    }
                    if (eepromlog!=null) fprintf(eepromlog,"EEPROM erase address %02x\n",address);
                    if (locked == 0)
                    {
                            if (intf.data_bits == 16)
                            {
                                    eeprom_data[2*address+0] = 0x00;
                                    eeprom_data[2*address+1] = 0x00;
                            }
                            else
                                    eeprom_data[address] = 0x00;
                    }
                    else
                    {
                        if (eepromlog!=null) fprintf(eepromlog,"Error: EEPROM is locked\n");
                        serial_count = 0;
                    }
            }
            else if (intf.cmd_write!=null && serial_count == (strlen(intf.cmd_write) + intf.address_bits + intf.data_bits) &&
                            !strncmp(serial_buffer,intf.cmd_write,strlen(intf.cmd_write)))
            {
                    int i,address,data;

                    address = 0;
                    for (i = 0;i < intf.address_bits;i++)
                    {
                            address <<= 1;
                            if (serial_buffer[i + strlen(intf.cmd_write)] == '1') address |= 1;
                    }
                    data = 0;
                    for (i = 0;i < intf.data_bits;i++)
                    {
                            data <<= 1;
                            if (serial_buffer[i + strlen(intf.cmd_write) + intf.address_bits] == '1') data |= 1;
                    }
                    if (eepromlog!=null) fprintf(eepromlog,"EEPROM write %04x to address %02x\n",data,address);
                    if (locked == 0)
                    {
                            if (intf.data_bits == 16)
                            {
                                    eeprom_data[2*address+0] = (char)((data >> 8)&0xff);
                                    eeprom_data[2*address+1] = (char)(data & 0xff);
                            }
                            else
                                    eeprom_data[address] = (char)(data&0xff);
                    }
                    else
                    {
                        if (eepromlog!=null) fprintf(eepromlog,"Error: EEPROM is locked\n");
                        serial_count = 0;
                    }
            }
            else if (intf.cmd_lock!=null && serial_count == strlen(intf.cmd_lock) &&
                            !strncmp(serial_buffer,intf.cmd_lock,strlen(intf.cmd_lock)))
            {
                    if (eepromlog!=null) fprintf(eepromlog,"EEPROM lock\n");
                    locked = 1;
                    serial_count = 0;
            }
            else if (intf.cmd_unlock!=null && serial_count == strlen(intf.cmd_unlock) &&
                            !strncmp(serial_buffer,intf.cmd_unlock,strlen(intf.cmd_unlock)))
            {
                    if (eepromlog!=null) fprintf(eepromlog,"EEPROM unlock\n");
                    locked = 0;
                    serial_count = 0;
            }
    }

    public static void EEPROM_reset()
    {
    if (eepromlog!=null && serial_count!=0)
    {
            fprintf(eepromlog,"EEPROM reset, buffer = %s\n",serial_buffer);
    }

            serial_count = 0;
            sending = 0;
    }


     public static void EEPROM_write_bit(int bit)
    {
    //#if VERBOSE
    if (eepromlog!=null) fprintf(eepromlog,"write bit %d\n",bit);
    //#endif
            latch = bit;
    }

    public static int EEPROM_read_bit()
    {
            int res;

            if (sending!=0)
                    res = (eeprom_data_bits >> intf.data_bits) & 1;
            else res = 1;

    //#if VERBOSE
    if (eepromlog!=null) fprintf(eepromlog,"read bit %d\n",res);
    //#endif

            return res;
    }

    public static void EEPROM_set_cs_line(int state)
    {
    //#if VERBOSE
    if (eepromlog!=null) fprintf(eepromlog,"set reset line %d\n",state);
    //#endif
            reset_line = state;

            if (reset_line != CLEAR_LINE)
                    EEPROM_reset();
    }

    public static void EEPROM_set_clock_line(int state)
    {
    //#if VERBOSE
    if (eepromlog!=null) fprintf(eepromlog,"set clock line %d\n",state);
    //#endif
            if (state == PULSE_LINE || (clock_line == CLEAR_LINE && state != CLEAR_LINE))
            {
                    if (reset_line == CLEAR_LINE)
                    {
                            if (sending!=0)
                                    eeprom_data_bits = (eeprom_data_bits << 1) | 1;
                            else
                                    EEPROM_write(latch);
                    }
            }

            clock_line = state;
    }


    public static void EEPROM_load(Object f)
    {
            osd_fread(f,eeprom_data,0,(1 << intf.address_bits) * intf.data_bits / 8);
    }

    public static void EEPROM_save(Object f)
    {
            osd_fwrite(f,eeprom_data,0,(1 << intf.address_bits) * intf.data_bits / 8);
    }    
}
