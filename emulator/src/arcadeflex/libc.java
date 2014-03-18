/*
This file is part of Arcadeflex.

Arcadeflex is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Arcadeflex is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Arcadeflex.  If not, see <http://www.gnu.org/licenses/>.
 */
package arcadeflex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shadow
 */
public class libc {

    
    /*************************************
     *
     *  Unsigned Byte Pointer Emulation
     *************************************/
    public static class UBytePtr {
        
        public UBytePtr() {
        }
        
        public UBytePtr(int size) {
            memory = new char[size];
            base=0;
        }
        public UBytePtr(char[] m) {
            set(m, 0);
        }
        public UBytePtr(char[] m ,int b)
        {
            set(m,b);
        }
        public UBytePtr(UByte[] m)
        {
            memory = new char[m.length];
            //System.arraycopy(m, 0, memory, 0, m.length);
            for(int i=0; i<m.length; i++)
            {
                memory[i] = m[i].read();
            }
            base=0;
        }
        public void set(char[] m, int b) {
            memory = m;
            base = b;
        }
        public void set(char[] m) {
            memory = m;
            base = 0;
        }
         public UBytePtr(UBytePtr cp, int b) {
            set(cp.memory, cp.base + b);
        }
         public UBytePtr(UBytePtr cp)
         {
             set(cp.memory,cp.base);
         }
        public char read() {
            return (char)(memory[base] & 0xFF);
        }
        public char read(int offset) {
            return (char)(memory[base + offset] & 0xFF); //return only the first 8bits
        }
        public void write(int offset, int value) {
            memory[base + offset] = (char) (value &0xFF);//store 8 bits only
        }
        public void inc() {
            this.base += 1;
        }
        public void writeinc(int value) {
            this.memory[(this.base++)] = (char) (value &0xFF);//store 8 bits only
        }
        public void inc(int count)
        {
            this.base+=count;
        }
        public char readinc() {
            return (char)((this.memory[(this.base++)])&0xFF);
        }
        public char readdec() {
            return (char)((this.memory[(this.base--)])&0xFF);
        }
        /*
        public UBytePtr(char[] m) {
            set(m, 0);
        }

        public UBytePtr(char[] m, int b) {
            set(m, b);
        }

        public UBytePtr(UBytePtr cp, int b) {
            set(cp.memory, cp.base + b);
        }

        public void set(char[] m, int b) {
            memory = m;
            base = b;
        }

        public char read(int offset) {
            return memory[base + offset];
        }

        public char read() {
            return memory[base];
        }

        public char readdec() {
            return this.memory[(this.base--)];
        }

        public char readinc() {
            return this.memory[(this.base++)];
        }

        public void write(int offset, int value) {
            memory[base + offset] = (char) value;
        }

        public void write(int value) {
            memory[base] = (char) value;
        }

        public void writeinc(int value) {
            this.memory[(this.base++)] = (char) value;
        }

        public void and(int value) {
            int tempbase = this.base;
            char[] tempmemory = this.memory;
            tempmemory[tempbase] = (char) (tempmemory[tempbase] & (char) value);
        }

        public void or(int value) {
            int tempbase = this.base;
            char[] tempmemory = this.memory;
            tempmemory[tempbase] = (char) (tempmemory[tempbase] | (char) value);
        }

        public void dec() {
            this.base -= 1;
        }

        
        
    
    */
        public char[] memory;
        public int base;
    }
    
     public static class UByte 
     {

        public UByte() {
        }


        public void set(char input) {
            if(input>255){
                throw new UnsupportedOperationException("tried to set unsigned byte with over 255 value");     
            }
            memory = input;
        }

        public char read() {
            return (char) (memory & 0xff);
        }
        char memory;
    }
      public static class UShort 
     {

        public UShort() {
        }


        public void set(char input) {
            if(input>65535){
                throw new UnsupportedOperationException("tried to set unsigned short with over 65535 value");     
            }
            memory = input;
        }

        public char read() {
            return memory;
        }
        char memory;
    }   
    public static class IntSubArray
    {
        public int[] buffer;
        public int offset;
        
        public IntSubArray(int size)
        {
            this.buffer = new int[size];
            this.offset = 0;
        }
        public IntSubArray(IntSubArray subarray, int offset)
        {
            this.buffer = subarray.buffer;
            this.offset = subarray.offset + offset;
        }
        public IntSubArray(int[] buffer, int offset)
        {
            this.buffer = buffer;
            this.offset = offset;
        }
        public int read(int index)
        {
            return buffer[index+offset];
        }
        public void write(int index,int value)
        {
            buffer[index+offset]=value;
        }
        public void writeinc(int value)
        {
            buffer[offset++]=value;
        }
        public void writedec(int value)
        {
            buffer[offset--]=value;
        }
        
    }
}
