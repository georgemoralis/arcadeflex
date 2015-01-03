package arcadeflex;

/**
 *
 * @author shadow
 */
public class ptrlib {

    /**
     * ***********************************
     *
     * Unsigned Byte Pointer Emulation ***********************************
     */
    public static class UBytePtr {

        public int bsize = 1;
        public char[] memory;
        public int offset;

        public UBytePtr() {
        }

        public UBytePtr(int size) {
            memory = new char[size];
            offset = 0;
        }

        public UBytePtr(char[] m) {
            set(m, 0);
        }

        public UBytePtr(char[] m, int b) {
            set(m, b);
        }

        public UBytePtr(UBytePtr cp, int b) {
            set(cp.memory, cp.offset + b);
        }

        public UBytePtr(UBytePtr cp) {
            set(cp.memory, cp.offset);
        }

        public void set(char[] m, int b) {
            memory = m;
            offset = b;
        }

        public void set(char[] m) {
            memory = m;
            offset = 0;
        }
        public void set(UBytePtr cp, int b) {
            set(cp.memory, cp.offset + b);
        }

        public void inc() {
            offset += bsize;
        }

        public void inc(int count) {
            offset += count * bsize;
        }

        public void dec(int count) {
            offset -= count * bsize;
        }

        public char read() {
            return (char) (memory[offset] & 0xFF);
        }
        public char READ_WORD(int index)
        {
            //return (char)(((memory[offset + 1 + index] << 8)& 0xFF) | (memory[offset + index]& 0xFF));
            return (char)((( memory[offset+index]&0xFF) << 0)
                    | (( memory[offset+index + 1]&0xFF) << 8));
            
        }
        public int READ_DWORD(int index)//unchecked!
        {
           /*return( ((memory[offset + 3 + index] << 24)& 0xFF)
            | ((memory[offset + 2 + index] << 16)& 0xFF)
            | ((memory[offset + 1 + index] << 8)& 0xFF) 
            | ((memory[offset + index]& 0xFF)));*/
            int myNumber = (( memory[offset+index]&0xFF) << 0)
                    | (( memory[offset+index + 1]&0xFF) << 8)
                    | (( memory[offset+index + 2]&0xFF) << 16)
                    | (( memory[offset+index + 3]&0xFF) << 24);
            return myNumber;
        }

        public char read(int index) {
            return (char) (memory[offset + index] & 0xFF); //return only the first 8bits
        }

        public char readinc() {
            return (char) ((memory[(this.offset++)]) & 0xFF);
        }

        public char readdec() {
            return (char) ((memory[(this.offset--)]) & 0xFF);
        }
        public void WRITE_WORD(int index , int value)
        {
            memory[offset + index] = (char)(value & 0xFF);
            memory[offset + index+ 1] = (char)((value >> 8)&0xFF);
        }

        public void write(int index, int value) {
            memory[offset + index] = (char) (value & 0xFF);//store 8 bits only
        }
        public void write(int value) {
            memory[offset] = (char) (value & 0xFF);//store 8 bits only
        }

        public void writeinc(int value) {
            this.memory[(this.offset++)] = (char) (value & 0xFF);//store 8 bits only
        }
    }
    /*
     *     Unsigned Short Ptr emulation
     *
     */

    public static class UShortPtr {

        public int bsize = 2;
        public char[] memory;
        public int offset;

        public UShortPtr() {
        }
        public UShortPtr(int size) {
            memory = new char[size];
            offset = 0;
        }
        public UShortPtr(UShortPtr cp, int b) {
            set(cp.memory, cp.offset + b);
        }

        public UShortPtr(UShortPtr cp) {
            set(cp.memory, cp.offset);
        }
        public void set(char[] m, int b) {
            memory = m;
            offset = b;
        }

        public void set(char[] m) {
            memory = m;
            offset = 0;
        }
        public char read(int index)
        {
            return (char)(memory[offset + 1 + index * 2] << 8 | memory[offset + index * 2]);
        }

        public void write(int index, char value)
        {
            memory[offset + index*2] = (char)(value & 0xFF);
            memory[offset + index * 2 + 1] = (char)((value >> 8)&0xFF);
        }

    }

}
