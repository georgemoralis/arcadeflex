package gr.codebb.arcadeflex.common;

/**
 *
 * @author shadow
 */
public class Util {

    /**
     * function to combine arrays in one array
     *
     * @param a first array
     * @param b second array
     * @return combined array
     */
    public static int[] combineIntArrays(int[] a, int[] b) {
        int length = a.length + b.length;
        int[] result = new int[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
