package arcadeflex;

import static arcadeflex.libc_old.*;

public class ticker {
  long ticks_per_sec; 
  
  public static long ticker()
  {
      return uclock();
  }
  
}
