package gr.codebb.arcadeflex.common.libc;

import java.util.Random;

/** @author shadow */
public class cstdlib {

  private static Random rand = new Random();

  /*
   *   return next random number
   */
  public static int rand() {
    return rand.nextInt();
  }
}
