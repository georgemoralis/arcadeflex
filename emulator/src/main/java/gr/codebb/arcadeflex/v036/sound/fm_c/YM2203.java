package gr.codebb.arcadeflex.v036.sound.fm_c;

public class YM2203 {

    public FM_OPN OPN;
    public FM_CH[] CH;

    public YM2203() {
        OPN = new FM_OPN();
        CH = new FM_CH[3];
        for (int i = 0; i < 3; i++) {
            CH[i] = new FM_CH();
        }
    }
}
