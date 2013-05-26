/*
This file is part of Arcadeflex.

Arcadeflex is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Arcadeflex is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Arcadeflex.  If not, see <http://www.gnu.org/licenses/>.
 */


package arcadeflex;

import static arcadeflex.libc.*;
/**
 *
 * @author shadow
 */
public class Main
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
          ConvertArguments("arcadeflex", args);
          args = null;
          System.exit(osdepend.main(argc, argv));
        
    }

}
