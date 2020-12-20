
package platform;

import java.io.*;

public class SeekableByteArrayInputStream extends ByteArrayInputStream {

    public SeekableByteArrayInputStream(byte buf[]) {
        super(buf);
    }
    public SeekableByteArrayInputStream(byte buf[], int offset, int length) {
        super(buf,offset,length);
    }

    public void seek(int p) {
        this.pos=p;
    }

    public long tell()  {
        return pos;
    }
}

