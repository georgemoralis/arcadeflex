package arcadeflex;

abstract public class MediaFactory {
    static public MediaFactory INSTANCE;

    abstract public IUrlDownloadProgress createUrlDownloadProgress();

    static interface IUrlDownloadProgress {
        void setVersion(String s);
        void setVisible(boolean b);
        void setRomName(String s);
        void setFileName(String s);
    }
}
