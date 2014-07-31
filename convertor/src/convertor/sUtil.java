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
package convertor;

/**
 *
 * @author george
 */
public class sUtil {
    public static char getChar()
    {
        if(Convertor.inpos >= 0 && Convertor.inpos < Convertor.inbuf.length)
        {
            return (char)(Convertor.inbuf[Convertor.inpos] + 256 & 0xff);
        } else
        {
            return '\0';
        }
    } 
    public static char getNextChar()
    {
        if(Convertor.inpos + 1 >= 0 && Convertor.inpos + 1 < Convertor.inbuf.length)
        {
            return (char)(Convertor.inbuf[Convertor.inpos + 1] + 256 & 0xff);
        } else
        {
            return '\0';
        }
    }
    
    public static char parseChar()
    {
        if(Convertor.inpos >= 0 && Convertor.inpos < Convertor.inbuf.length)
        {
            return (char)(Convertor.inbuf[Convertor.inpos++] + 256 & 0xff);
        } else
        {
            return '\0';
        }
    }
    public static void skipLine()
    {
        char c;
        do
        {
            if(Convertor.inpos >= Convertor.inbuf.length)
            {
                break;
            }
            c = parseChar();
        } while(c != '\n');
    }
    public static void skipTo(char paramChar)
    {
        while (Convertor.inpos < Convertor.inbuf.length)
        {
          char c = parseChar();
          if (c == paramChar)
            break;
        }
      }
    public static boolean isSeparator()
    {
        char c = getChar();
        switch(c)
        {
        case 0: // '\0'
        case 9: // '\t'
        case 10: // '\n'
        case 13: // '\r'
        case 32: // ' '
        case 33: // '!'
        case 34: // '"'
        case 37: // '%'
        case 38: // '&'
        case 40: // '('
        case 41: // ')'
        case 42: // '*'
        case 43: // '+'
        case 44: // ','
        case 45: // '-'
        case 46: // '.'
        case 47: // '/'
        case 58: // ':'
        case 59: // ';'
        case 60: // '<'
        case 61: // '='
        case 62: // '>'
        case 63: // '?'
        case 91: // '['
        case 93: // ']'
        case 94: // '^'
        case 123: // '{'
        case 124: // '|'
        case 125: // '}'
        case 126: // '~'
            return true;

        case 1: // '\001'
        case 2: // '\002'
        case 3: // '\003'
        case 4: // '\004'
        case 5: // '\005'
        case 6: // '\006'
        case 7: // '\007'
        case 8: // '\b'
        case 11: // '\013'
        case 12: // '\f'
        case 14: // '\016'
        case 15: // '\017'
        case 16: // '\020'
        case 17: // '\021'
        case 18: // '\022'
        case 19: // '\023'
        case 20: // '\024'
        case 21: // '\025'
        case 22: // '\026'
        case 23: // '\027'
        case 24: // '\030'
        case 25: // '\031'
        case 26: // '\032'
        case 27: // '\033'
        case 28: // '\034'
        case 29: // '\035'
        case 30: // '\036'
        case 31: // '\037'
        case 35: // '#'
        case 36: // '$'
        case 39: // '\''
        case 48: // '0'
        case 49: // '1'
        case 50: // '2'
        case 51: // '3'
        case 52: // '4'
        case 53: // '5'
        case 54: // '6'
        case 55: // '7'
        case 56: // '8'
        case 57: // '9'
        case 64: // '@'
        case 65: // 'A'
        case 66: // 'B'
        case 67: // 'C'
        case 68: // 'D'
        case 69: // 'E'
        case 70: // 'F'
        case 71: // 'G'
        case 72: // 'H'
        case 73: // 'I'
        case 74: // 'J'
        case 75: // 'K'
        case 76: // 'L'
        case 77: // 'M'
        case 78: // 'N'
        case 79: // 'O'
        case 80: // 'P'
        case 81: // 'Q'
        case 82: // 'R'
        case 83: // 'S'
        case 84: // 'T'
        case 85: // 'U'
        case 86: // 'V'
        case 87: // 'W'
        case 88: // 'X'
        case 89: // 'Y'
        case 90: // 'Z'
        case 92: // '\\'
        case 95: // '_'
        case 96: // '`'
        case 97: // 'a'
        case 98: // 'b'
        case 99: // 'c'
        case 100: // 'd'
        case 101: // 'e'
        case 102: // 'f'
        case 103: // 'g'
        case 104: // 'h'
        case 105: // 'i'
        case 106: // 'j'
        case 107: // 'k'
        case 108: // 'l'
        case 109: // 'm'
        case 110: // 'n'
        case 111: // 'o'
        case 112: // 'p'
        case 113: // 'q'
        case 114: // 'r'
        case 115: // 's'
        case 116: // 't'
        case 117: // 'u'
        case 118: // 'v'
        case 119: // 'w'
        case 120: // 'x'
        case 121: // 'y'
        case 122: // 'z'
        default:
            return false;
        }
    }
    public static String parseToken()
    {
        String s;
        for(s = ""; !isSeparator(); s = (new StringBuilder()).append(s).append(parseChar()).toString()) { }
        return s;
    } 
    public static void skipSpace(int i)
    {
        int j = 0;
        do
        {
            if(Convertor.inpos >= Convertor.inbuf.length || j >= i)
            {
                break;
            }
            char c = getChar();
            if(c != '\t')
            {
                break;
            }
            Convertor.inpos++;
            j++;
        } while(true);
    }
     public static void skipSpace()
    {
        do
        {
            if(Convertor.inpos >= Convertor.inbuf.length)
            {
                break;
            }
            char c = getChar();
            if(c != ' ' && c != '\t')
            {
                break;
            }
            Convertor.inpos++;
        } while(true);
    }
    public static boolean getToken(String s)
    {
        int j = Convertor.inpos;
        Convertor.inpos--;
        if(!isSeparator())
        {
            Convertor.inpos = j;
            return false;
        }
        Convertor.inpos++;
        for(int i = 0; i < s.length(); i++)
        {
            char c = parseChar();
            if(c != s.charAt(i))
            {
                Convertor.inpos = j;
                return false;
            }
        }

        if(!isSeparator())
        {
            Convertor.inpos = j;
            return false;
        } else
        {
            return true;
        }
    }
    public static void putString(String s)
    {
        for(int i = 0; i < s.length(); i++)
        {
            Convertor.outbuf[Convertor.outpos++] = (byte)s.charAt(i);
        }

    }
}
