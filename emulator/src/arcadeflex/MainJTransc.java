package arcadeflex;

import static arcadeflex.libc_old.ConvertArguments;
import static arcadeflex.libc_old.argc;
import static arcadeflex.libc_old.argv;

public class MainJTransc {
    static public void main(String[] args) {

        ConvertArguments("arcadeflex", new String[] { "1928" });
        args = null;
        System.exit(osdepend.main(argc, argv));
    }
}
