package common;

/**
 *
 * @author shadow
 */
public class util {

    /*
     *   copy array
     */
    public static void CopyArray(Object[] dst, Object[] src) {
        if (src == null) {
            return;
        }
        int k;
        for (k = 0; k < src.length; k++) {
            dst[k] = src[k];

        }
    }
}
