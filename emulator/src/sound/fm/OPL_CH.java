package sound.fm;


public class OPL_CH {
    public OPL_CH() 
    { 
        SLOT = new OPL_SLOT[2];
        SLOT[0] = new OPL_SLOT(); 
        SLOT[1] = new OPL_SLOT(); 
        op1_out= new int[2];
    }
   public OPL_SLOT[] SLOT;
    public int /*UINT8*/ CON;			/* connection type                     */
    public int /*UINT8*/ FB;			/* feed back       :(shift down bit)   */
    public int[] connect1;	/* slot1 output pointer                */
    public int[] connect2;	/* slot2 output pointer                */
    public int[] op1_out;	/* slot1 output for selfeedback        */
   	/* phase generator state */
    public long /*UINT32*/  block_fnum;	/* block+fnum      :                   */
    public int /*UINT8*/ kcode;		/* key code        : KeyScaleCode      */
    public long /*UINT32*/  fc;			/* Freq. Increment base                */
    public long /*UINT32*/  ksl_base;	/* KeyScaleLevel Base step             */
    public int /*UINT8*/ keyon;		/* key on/off flag                     */    
}
