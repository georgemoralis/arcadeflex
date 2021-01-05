package gr.codebb.arcadeflex.common.libc;

/** @author shadow */
public class cstdio {

  /**
   * Write formatted data to string
   *
   * @param str
   * @param arguments
   * @return
   */
  public static String sprintf(String str, Object... arguments) {
    return String.format(str, arguments);
  }
}
