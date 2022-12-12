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
package gr.codebb.arcadeflex.v036.platform;

/**
 *
 * @author shadow
 */
public class libc {

    

    
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

 
}
