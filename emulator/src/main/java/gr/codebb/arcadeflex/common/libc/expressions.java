package gr.codebb.arcadeflex.common.libc;

/** @author shadow */
public class expressions {

  public static int sizeof(byte[] array) {
    return array.length;
  }

  public static int sizeof(short[] array) {
    return array.length;
  }

  public static int sizeof(int[] array) {
    return array.length;
  }

  public static int sizeof(char[] array) {
    return array.length;
  }

  /** function equals to c bool */
  public static int BOOL(int value) {
    return value != 0 ? 1 : 0;
  }

  public static int BOOL(boolean value) {
    return value ? 1 : 0;
  }

  /** function equals to c NOT */
  public static int NOT(int value) {
    return value == 0 ? 1 : 0;
  }

  public static int NOT(boolean value) {
    return !value ? 1 : 0;
  }
}
