package gr.codebb.arcadeflex.v036.platform;

import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static arcadeflex.v036.mame.mame.*;

public class ticker {
  public static long TICKS_PER_SEC; 
  
  public static long ticker()
  {
      return uclock();
  }
  public static void init_ticker()
  {
    TICKS_PER_SEC = UCLOCKS_PER_SEC;

    if (errorlog!=null) fprintf(errorlog,"using uclock() for timing\n");
  }
  
}
