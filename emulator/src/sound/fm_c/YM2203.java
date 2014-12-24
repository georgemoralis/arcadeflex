package sound.fm_c;

public class YM2203 {

    public FM_OPN OPN = new FM_OPN();
    public FM_CH[] CH = new FM_CH[3];

    public YM2203() {
        for (int i = 0; i < 3; i++) {
            CH[i] = new FM_CH();
        }
    }
}
