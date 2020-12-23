/**
 * ported to 0.58
 * ported to 0.37b5
 */
package gr.codebb.arcadeflex.v058.sound;

public class vlm5030H {

    public static class VLM5030interface {

        public VLM5030interface(int baseclock, int volume, int memory_region, int memory_size, String[] samplenames) {
            this.baseclock = baseclock;
            this.volume = volume;
            this.memory_region = memory_region;
            this.memory_size = memory_size;
            this.samplenames = samplenames;
        }

        public VLM5030interface(int baseclock, int volume, int memory_region, int memory_size) {
            this.baseclock = baseclock;
            this.volume = volume;
            this.memory_region = memory_region;
            this.memory_size = memory_size;
            this.samplenames = null;
        }
        int baseclock;/* master clock (normaly 3.58MHz) */
        int volume;/* volume                         */
        int memory_region;/* memory region of speech rom    */
        int memory_size;/* memory size of speech rom (0=memory region length) */
        String[] samplenames;/* optional samples to replace emulation */
    }
}
