
package arcadeflex;

import static mame.driverH.*;
public class input {
    public static WriteHandlerPtr osd_led_w = new WriteHandlerPtr() { public void handler(int led,int on)
    {
        throw new UnsupportedOperationException("Unsupported osd_led_w");
    }};
}
