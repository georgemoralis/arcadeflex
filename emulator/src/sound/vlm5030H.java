package sound;

/**
 * ported to 0.37b11
 */

public class vlm5030H {
    public static class VLM5030interface {

        public VLM5030interface(int baseclock, int volume, int memory_region, int memory_size, int vcu, String[] samplenames) {
            this.baseclock = baseclock;
            this.volume = volume;
            this.memory_region = memory_region;
            this.memory_size = memory_size;
            this.vcu = vcu;
            this.samplenames = samplenames;
        }

        public VLM5030interface(int baseclock, int volume, int memory_region, int memory_size, int vcu) {
            this.baseclock = baseclock;
            this.volume = volume;
            this.memory_region = memory_region;
            this.memory_size = memory_size;
            this.vcu = vcu;
            this.samplenames = null;
        }

        int baseclock;      /* master clock (normaly 3.58MHz) */

        int volume;         /* volume                         */

        int memory_region;  /* memory region of speech rom    */

        int memory_size;    /* memory size of speech rom (0=memory region length) */

        int vcu;            /* vcu pin level                  */

        String[] samplenames;	/* optional samples to replace emulation */

    }
}
