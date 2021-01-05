package gr.codebb.arcadeflex.common;

/** @author shadow */
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

  public static long charArrayToLong(char[] b) {
    int start = 0;
    int i = 0;
    int len = 4;
    int cnt = 0;
    char[] tmp = new char[len];
    for (i = start; i < (start + len); i++) {
      tmp[cnt] = b[i];
      cnt++;
    }
    long accum = 0;
    i = 0;
    for (int shiftBy = 0; shiftBy < 32; shiftBy += 8) {
      accum |= ((long) (tmp[i] & 0xff)) << shiftBy;
      i++;
    }
    return accum;
  }

  /**
   * Convert a char array to a unsigned short
   *
   * @param b
   * @return
   */
  public static int charArrayToInt(char[] b) {
    int start = 0;
    int low = b[start] & 0xff;
    int high = b[start + 1] & 0xff;
    return (int) (high << 8 | low);
  }
}
