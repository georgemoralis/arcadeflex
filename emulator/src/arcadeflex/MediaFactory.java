package arcadeflex;

abstract public class MediaFactory {
    static public MediaFactory INSTANCE;

    abstract public IUrlDownloadProgress createUrlDownloadProgress();

    public IInput getInput() {
        return new IInput() {
            @Override
            public boolean isPressing(int button) {
                return false;
            }
        };
    }

    public IAudio getAudio() {
        return new IAudio() {
            @Override
            public void emitSamples(short[] samples) {

            }
        };
    }

    public IScreen getScreen() {
        return new IScreen() {
            @Override
            public void blit(int[] pixels, int width, int height) {
            }
        };
    }

    static interface IUrlDownloadProgress {
        void setVersion(String s);
        void setVisible(boolean b);
        void setRomName(String s);
        void setFileName(String s);
    }

    static interface IInput {
        boolean isPressing(int button);
    }

    static interface IAudio {
        void emitSamples(short[] samples);
    }

    static interface IScreen {
        void blit(int[] pixels, int width, int height);
    }
}
