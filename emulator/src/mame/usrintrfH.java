
package mame;


public class usrintrfH {
  public static final int DT_COLOR_WHITE = 0;
  public static final int DT_COLOR_YELLOW = 1;
  public static final int DT_COLOR_RED = 2;

	public static class DisplayText
	{
		public DisplayText() {};
		public DisplayText(String t, int c, int _x, int _y) { text = t; color = c; x = _x; y = _y; };
		public DisplayText(int z) { this(null, 0, 0, 0); };
		public static DisplayText[] create(int n)
		{ DisplayText []a = new DisplayText[n];	for(int k = 0; k < n; k++) a[k] = new DisplayText(); return a; }
		public String text;	/* 0 marks the end of the array */
		public int color;
		public int x;
		public int y;
	};    
}
