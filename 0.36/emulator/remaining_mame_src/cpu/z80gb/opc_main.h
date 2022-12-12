#define	INC_8BIT(x) \
{ \
  register UINT8 r,f; \
  x++; \
  r=(x);  \
  f=(UINT8)(Regs.b.F&FLAG_C); \
  if( r==0 )       f|=FLAG_Z; \
  if( (r&0xF)==0 ) f|=FLAG_H; \
  Regs.b.F=f; \
}

#define	DEC_8BIT(x) \
{ \
  register UINT8 r,f; \
  x--; \
  r=(x);  \
  f=(UINT8)((Regs.b.F&FLAG_C)|FLAG_N); \
  if( r==0 )       f|=FLAG_Z; \
  if( (r&0xF)==0 ) f|=FLAG_H; \
  Regs.b.F=f; \
}

#define	ADD_HL_RR(x) \
{ \
  register UINT32 r1,r2; \
  register UINT8 f; \
  r1=Regs.w.HL+(x); \
  r2=(Regs.w.HL&0xFFF)+((x)&0xFFF); \
  f=(UINT8)(Regs.b.F&FLAG_Z); \
  if( r1>0xFFFF ) f|=FLAG_C; \
  if( r2>0x0FFF ) f|=FLAG_H; \
  Regs.w.HL=(UINT16)r1; \
  Regs.b.F=f; \
}

#define	ADD_A_X(x) \
{ \
  register UINT16 r1,r2; \
  register UINT8 f; \
  r1=(UINT16)((Regs.b.A&0xF)+((x)&0xF)); \
  r2=(UINT16)(Regs.b.A+(x)); \
  Regs.b.A=(UINT8)r2; \
  if( ((UINT8)r2)==0 ) f=FLAG_Z; \
    else f=0; \
  if( r2>0xFF ) f|=FLAG_C; \
  if( r1>0xF )  f|=FLAG_H; \
  Regs.b.F=f; \
}

#define	SUB_A_X(x) \
{ \
  register UINT16 r1,r2; \
  register UINT8 f; \
  r1=(UINT16)((Regs.b.A&0xF)-((x)&0xF)); \
  r2=(UINT16)(Regs.b.A-(x)); \
  Regs.b.A=(UINT8)r2; \
  if( ((UINT8)r2)==0 ) f=FLAG_N|FLAG_Z; \
    else f=FLAG_N; \
  if( r2>0xFF ) f|=FLAG_C; \
  if( r1>0xF )  f|=FLAG_H; \
  Regs.b.F=f; \
}

/*
   #define		CP_A_X(x) \
   { \
   register UINT16 r; \
   register UINT8 f; \
   r=(UINT16)(Regs.b.A-(x)); \
   if( ((UINT8)r)==0 ) \
   f=FLAG_N|FLAG_Z; \
   else \
   f=FLAG_N; \
   f|=(UINT8)((r>>8)&FLAG_C); \
   if( (r^Regs.b.A^(x))&0x10 ) \
   f|=FLAG_H; \
   Regs.b.F=f; \
   }
 */

#define	CP_A_X(x) \
{ \
  register UINT16 r1,r2; \
  register UINT8 f; \
  r1=(UINT16)((Regs.b.A&0xF)-((x)&0xF)); \
  r2=(UINT16)(Regs.b.A-(x)); \
  if( ((UINT8)r2)==0 ) f=FLAG_N|FLAG_Z; \
    else f=FLAG_N; \
  if( r2>0xFF ) f|=FLAG_C; \
  if( r1>0xF )  f|=FLAG_H; \
  Regs.b.F=f; \
}

#define	SBC_A_X(x) \
{ \
  register UINT16 r1,r2; \
  register UINT8 f; \
  r1=(UINT16)((Regs.b.A&0xF)-((x)&0xF)-((Regs.b.F&FLAG_C)?1:0)); \
  r2=(UINT16)(Regs.b.A-(x)-((Regs.b.F&FLAG_C)?1:0)); \
  Regs.b.A=(UINT8)r2; \
  if( ((UINT8)r2)==0 ) f=FLAG_N|FLAG_Z; \
    else f=FLAG_N; \
  if( r2>0xFF ) f|=FLAG_C; \
  if( r1>0xF )  f|=FLAG_H; \
  Regs.b.F=f; \
}

#define	ADC_A_X(x) \
{ \
  register UINT16 r1,r2;  \
  register UINT8 f; \
  r1=(UINT16)((Regs.b.A&0xF)+((x)&0xF)+((Regs.b.F&FLAG_C)?1:0));  \
  r2=(UINT16)(Regs.b.A+(x)+((Regs.b.F&FLAG_C)?1:0)); \
  if( (Regs.b.A=(UINT8)r2)==0 ) f=FLAG_Z; \
    else f=0; \
  if( r2>0xFF )	f|=FLAG_C; \
  if( r1>0xF )	f|=FLAG_H; \
  Regs.b.F=f; \
}

#define	AND_A_X(x) \
  if( (Regs.b.A&=(x))==0 ) \
    Regs.b.F=FLAG_H|FLAG_Z; \
  else \
    Regs.b.F=FLAG_H;
    
#define XOR_A_X(x) \
  if( (Regs.b.A^=(x))==0 ) \
    Regs.b.F=FLAG_Z; \
  else \
    Regs.b.F=0;

#define	OR_A_X(x) \
  if( (Regs.b.A|=(x))==0 ) \
    Regs.b.F=FLAG_Z; \
  else \
    Regs.b.F=0;


case 0x00: /*	   NOP */
  break;
case 0x01: /*	   LD BC,n16 */
  Regs.w.BC = mem_ReadWord (Regs.w.PC);
  Regs.w.PC += 2;
  break;
case 0x02: /*	   LD (BC),A */
  mem_WriteByte (Regs.w.BC, Regs.b.A);
  break;
case 0x03: /*	   INC BC */

#if 0				/* FIXME ?? do we want to support this? (bug emulation) */
  if (Regs.b.B == 0xFE)
  {
    trash_sprites (state);
  }
#endif
  Regs.w.BC += 1;
  break;
case 0x04: /*	   INC B */

  INC_8BIT (Regs.b.B)
  break;
case 0x05: /*	   DEC B */

  DEC_8BIT (Regs.b.B)
  break;
case 0x06: /*	   LD B,n8 */

  Regs.b.B = mem_ReadByte (Regs.w.PC++);
  break;
case 0x07: /*	   RLCA */

  Regs.b.A = (UINT8) ((Regs.b.A << 1) | (Regs.b.A >> 7));
  if (Regs.b.A & 1)
  {
    Regs.b.F = FLAG_C;
  }
  else
  {
    Regs.b.F = 0;
  }
  break;
case 0x08: /*	   LD (n16),SP */

  mem_WriteWord (mem_ReadWord (Regs.w.PC), Regs.w.SP);
  Regs.w.PC += 2;
  break;
case 0x09: /*	   ADD HL,BC */

  ADD_HL_RR (Regs.w.BC)
  break;
case 0x0A: /*	   LD A,(BC) */

  Regs.b.A = mem_ReadByte (Regs.w.BC);
  break;
case 0x0B: /*	   DEC BC */

#if 0				/* FIXME ?? do we want to support this? (bug emulation) */
  if (Regs.b.B == 0xFE)
  {
    trash_sprites (state);
  }
#endif

  Regs.w.BC -= 1;
  break;
case 0x0C: /*	   INC C */

  INC_8BIT (Regs.b.C)
  break;
case 0x0D: /*	   DEC C */

  DEC_8BIT (Regs.b.C)
  break;
case 0x0E: /*	   LD C,n8 */

  Regs.b.C = mem_ReadByte (Regs.w.PC++);
  break;
case 0x0F: /*	   RRCA */

  Regs.b.A = (UINT8) ((Regs.b.A >> 1) | (Regs.b.A << 7));
  if (Regs.b.A & 0x80)
  {
    Regs.b.F |= FLAG_C;
  }
  else
  {
    Regs.b.F = 0;
  }
  break;
case 0x10: /*	   STOP */
  break;
case 0x11: /*	   LD DE,n16 */

  Regs.w.DE = mem_ReadWord (Regs.w.PC);
  Regs.w.PC += 2;
  break;
case 0x12: /*	   LD (DE),A */
  mem_WriteByte (Regs.w.DE, Regs.b.A);
  break;
case 0x13: /*	   INC DE */

#if 0				/* FIXME ?? do we want to support this? (bug emulation) */
  if (Regs.b.D == 0xFE)
  {
    trash_sprites (state);
  }
#endif

  Regs.w.DE += 1;
  break;
case 0x14: /*	   INC D */

  INC_8BIT (Regs.b.D)
  break;
case 0x15: /*	   DEC D */

  DEC_8BIT (Regs.b.D)
  break;
case 0x16: /*	   LD D,n8 */

  Regs.b.D = mem_ReadByte (Regs.w.PC++);
  break;
case 0x17: /*	   RLA */
  
  x = (Regs.b.A & 0x80) ? FLAG_C : 0;

  Regs.b.A = (UINT8) ((Regs.b.A << 1) | ((Regs.b.F & FLAG_C) ? 1 : 0));
  Regs.b.F = x;
  break;
case 0x18: /*	   JR	   n8 */
  {
	INT8 offset;

    offset = mem_ReadByte (Regs.w.PC++);
    Regs.w.PC += offset;
  }
  break;
case 0x19: /*	   ADD HL,DE */

  ADD_HL_RR (Regs.w.DE)
  break;
case 0x1A: /*	   LD A,(DE) */

  Regs.b.A = mem_ReadByte (Regs.w.DE);
  break;
case 0x1B: /*	   DEC DE */

#if 0				/* FIXME ?? do we want to support this? (bug emulation) */
  if (Regs.b.D == 0xFE)
  {
    trash_sprites (state);
  }
#endif

  Regs.w.DE -= 1;
  break;
case 0x1C: /*	   INC E */

  INC_8BIT (Regs.b.E)
  break;
case 0x1D: /*	   DEC E */

  DEC_8BIT (Regs.b.E)
  break;
case 0x1E: /*	   LD E,n8 */

  Regs.b.E = mem_ReadByte (Regs.w.PC++);
  break;
case 0x1F: /*	   RRA */
  
  x = (Regs.b.A & 1) ? FLAG_C : 0;

  Regs.b.A = (UINT8) ((Regs.b.A >> 1) | ((Regs.b.F & FLAG_C) ? 0x80 : 0));
  Regs.b.F = x;
  break;
case 0x20: /*	   JR NZ,n8 */

  if (Regs.b.F & FLAG_Z)
  {
    Regs.w.PC++;
  }
  else
  {
	INT8 offset;

    offset = mem_ReadByte (Regs.w.PC++);
    Regs.w.PC += offset;
    ICycles += 4;
  }
  break;
case 0x21: /*	   LD HL,n16 */

  Regs.w.HL = mem_ReadWord (Regs.w.PC);
  Regs.w.PC += 2;
  break;
case 0x22: /*	   LD (HL+),A */

#if 0				/* FIXME ?? do we want to support this? (bug emulation) */
  if (Regs.b.H == 0xFE)
  {
    trash_sprites (state);
  }
#endif

  mem_WriteByte (Regs.w.HL, Regs.b.A);
  Regs.w.HL += 1;
  break;
case 0x23: /*	   INC HL */

#if 0				/* FIXME ?? do we want to support this? (bug emulation) */
  if (Regs.b.H == 0xFE)
  {
    trash_sprites (state);
  }
#endif

  Regs.w.HL += 1;
  break;
case 0x24: /*	   INC H */

  INC_8BIT (Regs.b.H);
  break;
case 0x25: /*	   DEC H */

  DEC_8BIT (Regs.b.H);
  break;
case 0x26: /*	   LD H,n8 */

  Regs.b.H = mem_ReadByte (Regs.w.PC++);
  break;
case 0x27: /*	   DAA */

  Regs.w.AF = DAATable[(((UINT16) (Regs.b.F & (FLAG_N | FLAG_C | FLAG_H))) << 4) | Regs.b.A];
  break;
case 0x28: /*	   JR Z,n8 */

  if (Regs.b.F & FLAG_Z)
  {
	INT8 offset;

    offset = mem_ReadByte (Regs.w.PC++);
    Regs.w.PC += offset;

    ICycles += 4;
  }
  else
  {
    Regs.w.PC += 1;
  }
  break;
case 0x29: /*	   ADD HL,HL */

  ADD_HL_RR (Regs.w.HL)
  break;
case 0x2A: /*	   LD A,(HL+) */
#if 0				/* FIXME ?? do we want to support this? (bug emulation) */
  if (Regs.b.H == 0xFE)
  {
    trash_sprites (state);
  }
#endif

  Regs.b.A = mem_ReadByte (Regs.w.HL);
  Regs.w.HL += 1;
  break;
case 0x2B: /*	   DEC HL */

#if 0				/* FIXME ?? do we want to support this? (bug emulation) */
  if (Regs.b.H == 0xFE)
  {
    trash_sprites (state);
  }
#endif

  Regs.w.HL -= 1;
  break;
case 0x2C: /*	   INC L */

  INC_8BIT (Regs.b.L);
  break;
case 0x2D: /*	   DEC L */

  DEC_8BIT (Regs.b.L);
  break;
case 0x2E: /*	   LD L,n8 */

  Regs.b.L = mem_ReadByte (Regs.w.PC++);
  break;
case 0x2F: /*	   CPL */

  Regs.b.A = ~Regs.b.A;
  Regs.b.F |= FLAG_N | FLAG_H;
  return 4;
  break;
case 0x30: /*	   JR NC,n8 */

  if (Regs.b.F & FLAG_C)
  {
    Regs.w.PC += 1;
  }
  else
  {
	INT8 offset;

    offset = mem_ReadByte (Regs.w.PC++);
    Regs.w.PC += offset;
    ICycles += 4;
  }
  break;
case 0x31: /*	   LD SP,n16 */

  Regs.w.SP = mem_ReadWord (Regs.w.PC);
  Regs.w.PC += 2;
  break;
case 0x32: /*	   LD (HL-),A */

#if 0				/* FIXME ?? do we want to support this? (bug emulation) */
  if (Regs.b.H == 0xFE)
  {
    trash_sprites (state);
  }
#endif

  mem_WriteByte (Regs.w.HL, Regs.b.A);
  Regs.w.HL -= 1;
  break;
case 0x33: /*	   INC SP */

  Regs.w.SP += 1;
  break;
case 0x34: /*	   INC (HL) */
  
  {
	register UINT8 r, f;

	f = (UINT8) (Regs.b.F & FLAG_C);
	r = (UINT8) (mem_ReadByte (Regs.w.HL) + 1);
    mem_WriteByte (Regs.w.HL, r);

    if (r == 0)
      f |= FLAG_Z;

    if ((r & 0xF) == 0)
      f |= FLAG_H;

    Regs.b.F = f;
  }
  break;
case 0x35: /*	   DEC (HL) */
  
  {
	register UINT8 r, f;

	f = (UINT8) ((Regs.b.F & FLAG_C) | FLAG_N);
	r = (UINT8) (mem_ReadByte (Regs.w.HL) - 1);
    mem_WriteByte (Regs.w.HL, r);

    if (r == 0)
      f |= FLAG_Z;

    if ((r & 0xF) != 0xF)
      f |= FLAG_H;

    Regs.b.F = f;
  }
  break;
case 0x36: /*	   LD (HL),n8 */
  /* FIXED / broken ? */
  mem_WriteByte (Regs.w.HL, mem_ReadByte (Regs.w.PC++));
  break;
case 0x37: /*	   SCF */

  Regs.b.F = (UINT8) ((Regs.b.F & FLAG_Z) | FLAG_C);
  break;
case 0x38: /*	   JR C,n8 */

  if (Regs.b.F & FLAG_C)
  {
	INT8 offset;

    offset = mem_ReadByte (Regs.w.PC++);
    Regs.w.PC += offset;

    ICycles += 4;
  }
  else
  {
    Regs.w.PC += 1;
  }
  break;
case 0x39: /*	   ADD HL,SP */
  ADD_HL_RR (Regs.w.SP)
  break;
case 0x3A: /*	   LD A,(HL-) */
#if 0				/* FIXME ?? do we want to support this? (bug emulation) */
  if (Regs.b.H == 0xFE)
  {
    trash_sprites (state);
  }
#endif

  Regs.b.A = mem_ReadByte (Regs.w.HL);
  Regs.w.HL -= 1;
  break;
case 0x3B: /*	   DEC SP */

  Regs.w.SP -= 1;
  break;
case 0x3C: /*	   INC	   A */

  INC_8BIT (Regs.b.A);
  break;
case 0x3D: /*	   DEC	   A */

  DEC_8BIT (Regs.b.A);
  break;
case 0x3E: /*	   LD A,n8 */

  Regs.b.A = mem_ReadByte (Regs.w.PC++);
  break;
case 0x3F: /*	   CCF */

  Regs.b.F = (UINT8) ((Regs.b.F & FLAG_Z) | ((Regs.b.F & FLAG_C) ? 0 : FLAG_C));
  break;
case 0x40: /*	   LD B,B */
  break;
case 0x41: /*	   LD B,C */

  Regs.b.B = Regs.b.C;
  break;
case 0x42: /*	   LD B,D */

  Regs.b.B = Regs.b.D;
  break;
case 0x43: /*	   LD B,E */

  Regs.b.B = Regs.b.E;
  break;
case 0x44: /*	   LD B,H */

  Regs.b.B = Regs.b.H;
  break;
case 0x45: /*	   LD B,L */

  Regs.b.B = Regs.b.L;
  break;
case 0x46: /*	   LD B,(HL) */

  Regs.b.B = mem_ReadByte (Regs.w.HL);
  break;
case 0x47: /*	   LD B,A */

  Regs.b.B = Regs.b.A;
  break;
case 0x48: /*	   LD C,B */

  Regs.b.C = Regs.b.B;
  break;
case 0x49: /*	   LD C,C */
  break;
case 0x4A: /*	   LD C,D */

  Regs.b.C = Regs.b.D;
  break;
case 0x4B: /*	   LD C,E */

  Regs.b.C = Regs.b.E;
  break;
case 0x4C: /*	   LD C,H */

  Regs.b.C = Regs.b.H;
  break;
case 0x4D: /*	   LD C,L */

  Regs.b.C = Regs.b.L;
  break;
case 0x4E: /*	   LD C,(HL) */

  Regs.b.C = mem_ReadByte (Regs.w.HL);
  break;
case 0x4F: /*	   LD C,A */

  Regs.b.C = Regs.b.A;
  break;
case 0x50: /*	   LD D,B */

  Regs.b.D = Regs.b.B;
  break;
case 0x51: /*	   LD D,C */

  Regs.b.D = Regs.b.C;
  break;
case 0x52: /*	   LD D,D */
  break;
case 0x53: /*	   LD D,E */

  Regs.b.D = Regs.b.E;
  break;
case 0x54: /*	   LD D,H */

  Regs.b.D = Regs.b.H;
  break;
case 0x55: /*	   LD D,L */

  Regs.b.D = Regs.b.L;
  break;
case 0x56: /*	   LD D,(HL) */

  Regs.b.D = mem_ReadByte (Regs.w.HL);
  break;
case 0x57: /*	   LD D,A */

  Regs.b.D = Regs.b.A;
  break;
case 0x58: /*	   LD E,B */

  Regs.b.E = Regs.b.B;
  break;
case 0x59: /*	   LD E,C */

  Regs.b.E = Regs.b.C;
  break;
case 0x5A: /*	   LD E,D */

  Regs.b.E = Regs.b.D;
  break;
case 0x5B: /*	   LD E,E */
  break;
case 0x5C: /*	   LD E,H */

  Regs.b.E = Regs.b.H;
  break;
case 0x5D: /*	   LD E,L */

  Regs.b.E = Regs.b.L;
  break;
case 0x5E: /*	   LD E,(HL) */

  Regs.b.E = mem_ReadByte (Regs.w.HL);
  break;
case 0x5F: /*	   LD E,A */

  Regs.b.E = Regs.b.A;
  break;
case 0x60: /*	   LD H,B */

  Regs.b.H = Regs.b.B;
  break;
case 0x61: /*	   LD H,C */

  Regs.b.H = Regs.b.C;
  break;
case 0x62: /*	   LD H,D */

  Regs.b.H = Regs.b.D;
  break;
case 0x63: /*	   LD H,E */

  Regs.b.H = Regs.b.E;
  break;
case 0x64: /*	   LD H,H */
  break;
case 0x65: /*	   LD H,L */

  Regs.b.H = Regs.b.L;
  break;
case 0x66: /*	   LD H,(HL) */

  Regs.b.H = mem_ReadByte (Regs.w.HL);
  break;
case 0x67: /*	   LD H,A */

  Regs.b.H = Regs.b.A;
  break;
case 0x68: /*	   LD L,B */

  Regs.b.L = Regs.b.B;
  break;
case 0x69: /*	   LD L,C */

  Regs.b.L = Regs.b.C;
  break;
case 0x6A: /*	   LD L,D */
  Regs.b.L = Regs.b.D;
  break;
case 0x6B: /*	   LD L,E */

  Regs.b.L = Regs.b.E;
  break;
case 0x6C: /*	   LD L,H */

  Regs.b.L = Regs.b.H;
  break;
case 0x6D: /*	   LD L,L */
  break;
case 0x6E: /*	   LD L,(HL) */

  Regs.b.L = mem_ReadByte (Regs.w.HL);
  break;
case 0x6F: /*	   LD L,A */

  Regs.b.L = Regs.b.A;
  break;
case 0x70: /*	   LD (HL),B */

  mem_WriteByte (Regs.w.HL, Regs.b.B);
  break;
case 0x71: /*	   LD (HL),C */

  mem_WriteByte (Regs.w.HL, Regs.b.C);
  break;
case 0x72: /*	   LD (HL),D */

  mem_WriteByte (Regs.w.HL, Regs.b.D);
  break;
case 0x73: /*	   LD (HL),E */

  mem_WriteByte (Regs.w.HL, Regs.b.E);
  break;
case 0x74: /*	   LD (HL),H */

  mem_WriteByte (Regs.w.HL, Regs.b.H);
  break;
case 0x75: /*	   LD (HL),L */

  mem_WriteByte (Regs.w.HL, Regs.b.L);
  break;
case 0x76: /*	   HALT */
  {
	UINT32 skip_cycles;
	Regs.w.enable |= HALTED;
    CheckInterrupts = 1;
    Regs.w.PC--;
    
    /* Calculate nr of cycles which can be skipped */
	skip_cycles = (0x100 << gb_timer_shift) - gb_timer_count;
	if (skip_cycles > z80gb_ICount) skip_cycles = z80gb_ICount;
    
    /* round cycles to multiple of 4 always round upwards */
	skip_cycles = (skip_cycles+3) & ~3;
	if (skip_cycles > ICycles) ICycles += skip_cycles - ICycles;
  }
  break;
case 0x77: /*	   LD (HL),A */

  mem_WriteByte (Regs.w.HL, Regs.b.A);
  break;
case 0x78: /*	   LD A,B */

  Regs.b.A = Regs.b.B;
  break;
case 0x79: /*	   LD A,C */

  Regs.b.A = Regs.b.C;
  break;
case 0x7A: /*	   LD A,D */

  Regs.b.A = Regs.b.D;
  break;
case 0x7B: /*	   LD A,E */

  Regs.b.A = Regs.b.E;
  break;
case 0x7C: /*	   LD A,H */

  Regs.b.A = Regs.b.H;
  break;
case 0x7D: /*	   LD A,L */

  Regs.b.A = Regs.b.L;
  break;
case 0x7E: /*	   LD A,(HL) */

  Regs.b.A = mem_ReadByte (Regs.w.HL);
  break;
case 0x7F: /*	   LD A,A */
  break;
case 0x80: /*	   ADD A,B */

  ADD_A_X (Regs.b.B)
  break;
case 0x81: /*	   ADD A,C */

  ADD_A_X (Regs.b.C)
  break;
case 0x82: /*	   ADD A,D */

  ADD_A_X (Regs.b.D)
  break;
case 0x83: /*	   ADD A,E */

  ADD_A_X (Regs.b.E)
  break;
case 0x84: /*	   ADD A,H */

  ADD_A_X (Regs.b.H)
  break;
case 0x85: /*	   ADD A,L */

  ADD_A_X (Regs.b.L)
  break;
case 0x86: /*	   ADD A,(HL) */

  x = mem_ReadByte (Regs.w.HL);

  ADD_A_X (x)
  break;
case 0x87: /*	   ADD A,A */

  ADD_A_X (Regs.b.A)
  break;
case 0x88: /*	   ADC A,B */

  ADC_A_X (Regs.b.B)
  break;
case 0x89: /*	   ADC A,C */

  ADC_A_X (Regs.b.C)
  break;
case 0x8A: /*	   ADC A,D */

  ADC_A_X (Regs.b.D)
  break;
case 0x8B: /*	   ADC A,E */

  ADC_A_X (Regs.b.E)
  break;
case 0x8C: /*	   ADC A,H */

  ADC_A_X (Regs.b.H)
  break;
case 0x8D: /*	   ADC A,L */

  ADC_A_X (Regs.b.L)
  break;
case 0x8E: /*	   ADC A,(HL) */

  x = mem_ReadByte (Regs.w.HL);

  ADC_A_X (x)
  break;
case 0x8F: /*	   ADC A,A */

  ADC_A_X (Regs.b.A)
  break;
case 0x90: /*	   SUB A,B */

  SUB_A_X (Regs.b.B)
  break;
case 0x91: /*	   SUB A,C */

  SUB_A_X (Regs.b.C)
  break;
case 0x92: /*	   SUB A,D */

  SUB_A_X (Regs.b.D)
  break;
case 0x93: /*	   SUB A,E */

  SUB_A_X (Regs.b.E)
  break;
case 0x94: /*	   SUB A,H */

  SUB_A_X (Regs.b.H)
  break;
case 0x95: /*	   SUB A,L */

  SUB_A_X (Regs.b.L)
  break;
case 0x96: /*	   SUB A,(HL) */


  x = mem_ReadByte (Regs.w.HL);

  SUB_A_X (x)
  break;
case 0x97: /*	   SUB A,A */

  SUB_A_X (Regs.b.A)
  break;
case 0x98: /*	   SBC A,B */

  SBC_A_X (Regs.b.B)
  break;
case 0x99: /*	   SBC A,C */

  SBC_A_X (Regs.b.C)
  break;
case 0x9A: /*	   SBC A,D */

  SBC_A_X (Regs.b.D)
  break;
case 0x9B: /*	   SBC A,E */

  SBC_A_X (Regs.b.E)
  break;
case 0x9C: /*	   SBC A,H */

  SBC_A_X (Regs.b.H)
  break;
case 0x9D: /*	   SBC A,L */

  SBC_A_X (Regs.b.L)
  break;
case 0x9E: /*	   SBC A,(HL) */

  x = mem_ReadByte (Regs.w.HL);

  SBC_A_X (x)
  break;
case 0x9F: /*	   SBC A,A */

  SBC_A_X (Regs.b.A)
  break;
case 0xA0: /*	   AND A,B */

  AND_A_X (Regs.b.B)
  break;
case 0xA1: /*	   AND A,C */

  AND_A_X (Regs.b.C)
  break;
case 0xA2: /*	   AND A,D */

  AND_A_X (Regs.b.D)
  break;
case 0xA3: /*	   AND A,E */

  AND_A_X (Regs.b.E)
  break;
case 0xA4: /*	   AND A,H */

  AND_A_X (Regs.b.H)
  break;
case 0xA5: /*	   AND A,L */

  AND_A_X (Regs.b.L)
  break;
case 0xA6: /*	   AND A,(HL) */

  x = mem_ReadByte (Regs.w.HL);

  AND_A_X (x)
  break;
case 0xA7: /*	   AND A,A */

  Regs.b.F = (Regs.b.A == 0) ? (FLAG_H | FLAG_Z) : FLAG_H;
  break;
case 0xA8: /*	   XOR A,B */

  XOR_A_X (Regs.b.B)
  break;
case 0xA9: /*	   XOR A,C */

  XOR_A_X (Regs.b.C)
  break;
case 0xAA: /*	   XOR A,D */

  XOR_A_X (Regs.b.D)
  break;
case 0xAB: /*	   XOR A,E */

  XOR_A_X (Regs.b.E)
  break;
case 0xAC: /*	   XOR A,H */

  XOR_A_X (Regs.b.H)
  break;
case 0xAD: /*	   XOR A,L */

  XOR_A_X (Regs.b.L)
  break;
case 0xAE: /*	   XOR A,(HL) */

  x = mem_ReadByte (Regs.w.HL);

  XOR_A_X (x)
  break;
case 0xAF: /*	   XOR A,A */

  XOR_A_X (Regs.b.A)
  break;
case 0xB0: /*	   OR A,B */

  OR_A_X (Regs.b.B)
  break;
case 0xB1: /*	   OR A,C */

  OR_A_X (Regs.b.C)
  break;
case 0xB2: /*	   OR A,D */

  OR_A_X (Regs.b.D)
  break;
case 0xB3: /*	   OR A,E */

  OR_A_X (Regs.b.E)
  break;
case 0xB4: /*	   OR A,H */

  OR_A_X (Regs.b.H)
  break;
case 0xB5: /*	   OR A,L */

  OR_A_X (Regs.b.L)
  break;
case 0xB6: /*	   OR A,(HL) */

  x = mem_ReadByte (Regs.w.HL);

  OR_A_X (x)
  break;
case 0xB7: /*	   OR A,A */

  OR_A_X (Regs.b.A)
  break;
case 0xB8: /*	   CP A,B */

  CP_A_X (Regs.b.B)
  break;
case 0xB9: /*	   CP A,C */

  CP_A_X (Regs.b.C)
  break;
case 0xBA: /*	   CP A,D */

  CP_A_X (Regs.b.D)
  break;
case 0xBB: /*	   CP A,E */

  CP_A_X (Regs.b.E)
  break;
case 0xBC: /*	   CP A,H */

  CP_A_X (Regs.b.H)
  break;
case 0xBD: /*	   CP A,L */

  CP_A_X (Regs.b.L)
  break;
case 0xBE: /*	   CP A,(HL) */

  x = mem_ReadByte (Regs.w.HL);

  CP_A_X (x)
  break;
case 0xBF: /*	   CP A,A */

  CP_A_X (Regs.b.A)
  break;
case 0xC0: /*	   RET NZ */

  if (!(Regs.b.F & FLAG_Z))
  {
    Regs.w.PC = mem_ReadWord (Regs.w.SP);
    Regs.w.SP += 2;
    ICycles += 12;
  }
  break;
case 0xC1: /*	   POP BC */

  Regs.w.BC = mem_ReadWord (Regs.w.SP);
  Regs.w.SP += 2;
  break;
case 0xC2: /*	   JP NZ,n16 */

  if (Regs.b.F & FLAG_Z)
  {
    Regs.w.PC += 2;
  }
  else
  {
    Regs.w.PC = mem_ReadWord (Regs.w.PC);
    ICycles += 4;
  }
  break;
case 0xC3: /*	   JP n16 */

  Regs.w.PC = mem_ReadWord (Regs.w.PC);
  break;
case 0xC4: /*	   CALL NZ,n16 */

  if (Regs.b.F & FLAG_Z)
  {
    Regs.w.PC += 2;
  }
  else
  {
	register UINT16 PC;
    PC = mem_ReadWord (Regs.w.PC);
    Regs.w.PC += 2;

    Regs.w.SP -= 2;
    mem_WriteWord (Regs.w.SP, Regs.w.PC);
    Regs.w.PC = PC;
    ICycles += 12;
  }
  break;
case 0xC5: /*	   PUSH BC */

  Regs.w.SP -= 2;
  mem_WriteWord (Regs.w.SP, Regs.w.BC);
  break;
case 0xC6: /*	   ADD A,n8 */

  x = mem_ReadByte (Regs.w.PC++);
  ADD_A_X (x)
  break;
case 0xC7: /*	   RST 0 */
  
  {
	register UINT16 PC;
    PC = Regs.w.PC;
    Regs.w.PC = 0;

    Regs.w.SP -= 2;
    mem_WriteWord (Regs.w.SP, PC);
  }
  break;
case 0xC8: /*	   RET Z */

  if (Regs.b.F & FLAG_Z)
  {
    Regs.w.PC = mem_ReadWord (Regs.w.SP);
    Regs.w.SP += 2;
    ICycles += 12;
  }
  break;
case 0xC9: /*	   RET */

  Regs.w.PC = mem_ReadWord (Regs.w.SP);
  Regs.w.SP += 2;
  break;
case 0xCA: /*	   JP Z,n16 */

  if (Regs.b.F & FLAG_Z)
  {
    Regs.w.PC = mem_ReadWord (Regs.w.PC);
    ICycles += 4;
  }
  else
  {
    Regs.w.PC += 2;
  }
  break;
case 0xCB: /*	   PREFIX! */
  x = mem_ReadByte (Regs.w.PC++);
  ICycles += CyclesCB[x];
  switch (x)
  {
    #include "opc_cb.h"
  }  
  break;
case 0xCC: /*	   CALL Z,n16 */

  if (Regs.b.F & FLAG_Z)
  {
	register UINT16 PC;
    PC = mem_ReadWord (Regs.w.PC);
    Regs.w.PC += 2;

    Regs.w.SP -= 2;
    mem_WriteWord (Regs.w.SP, Regs.w.PC);
    Regs.w.PC = PC;
    ICycles += 12;
  }
  else
  {
    Regs.w.PC += 2;
  }
  break;
case 0xCD: /*	   CALL n16 */
  {
	register UINT16 PC;
    PC = mem_ReadWord (Regs.w.PC);
    Regs.w.PC += 2;

    Regs.w.SP -= 2;
    mem_WriteWord (Regs.w.SP, Regs.w.PC);
    Regs.w.PC = PC;
  }
  break;
case 0xCE: /*	   ADC A,n8 */

  x = mem_ReadByte (Regs.w.PC++);
  ADC_A_X (x)
  break;
case 0xCF: /*	   RST 8 */

  Regs.w.SP -= 2;
  mem_WriteWord (Regs.w.SP, Regs.w.PC);
  Regs.w.PC = 8;
  break;
case 0xD0: /*	   RET NC */

  if (!(Regs.b.F & FLAG_C))
  {
    Regs.w.PC = mem_ReadWord (Regs.w.SP);
    Regs.w.SP += 2;
    ICycles += 12;
  }
  break;
case 0xD1: /*	   POP DE */

  Regs.w.DE = mem_ReadWord (Regs.w.SP);
  Regs.w.SP += 2;
  break;
case 0xD2: /*	   JP NC,n16 */

  if (Regs.b.F & FLAG_C)
  {
    Regs.w.PC += 2;
  }
  else
  {
    Regs.w.PC = mem_ReadWord (Regs.w.PC);
    ICycles += 4;
  }
  break;
case 0xD3: /*	   EH? */
  break;
case 0xD4: /*	   CALL NC,n16 */

  if (Regs.b.F & FLAG_C)
  {
    Regs.w.PC += 2;
  }
  else
  {
	register UINT16 PC;
    PC = mem_ReadWord (Regs.w.PC);
    Regs.w.PC += 2;

    Regs.w.SP -= 2;
    mem_WriteWord (Regs.w.SP, Regs.w.PC);
    Regs.w.PC = PC;
    ICycles += 12;
  }
  break;
case 0xD5: /*	   PUSH DE */

  Regs.w.SP -= 2;
  mem_WriteWord (Regs.w.SP, Regs.w.DE);
  break;
case 0xD6: /*	   SUB A,n8 */

  x = mem_ReadByte (Regs.w.PC++);
  SUB_A_X (x)
  break;
case 0xD7: /*	   RST	   $10 */

  Regs.w.SP -= 2;
  mem_WriteWord (Regs.w.SP, Regs.w.PC);
  Regs.w.PC = 0x10;
  break;
case 0xD8: /*	   RET C */

  if (Regs.b.F & FLAG_C)
  {
    Regs.w.PC = mem_ReadWord (Regs.w.SP);
    Regs.w.SP += 2;
    ICycles += 12;
  }
  break;
case 0xD9: /*	   RETI */

  Regs.w.PC = mem_ReadWord (Regs.w.SP);
  Regs.w.SP += 2;
  Regs.w.enable |= IME;
  CheckInterrupts = 1;
  break;
case 0xDA: /*	   JP C,n16 */

  if (Regs.b.F & FLAG_C)
  {
    Regs.w.PC = mem_ReadWord (Regs.w.PC);
    ICycles += 4;
  }
  else
  {
    Regs.w.PC += 2;
  }
  break;
case 0xDB: /*	   EH? */
  break;
case 0xDC: /*	   CALL C,n16 */

  if (Regs.b.F & FLAG_C)
  {
	register UINT16 PC;
    PC = mem_ReadWord (Regs.w.PC);
    Regs.w.PC += 2;

    Regs.w.SP -= 2;
    mem_WriteWord (Regs.w.SP, Regs.w.PC);
    Regs.w.PC = PC;
    ICycles += 12;
  }
  else
  {
    Regs.w.PC += 2;
  }
  break;
case 0xDD: /*	   EH? */
  break;
case 0xDE: /*	   SBC A,n8 */

  x = mem_ReadByte (Regs.w.PC++);
  SBC_A_X (x)
  break;
case 0xDF: /*	   RST	   $18 */

  Regs.w.SP -= 2;
  mem_WriteWord (Regs.w.SP, Regs.w.PC);
  Regs.w.PC = 0x18;
  break;
case 0xE0: /*	   LD	   ($FF00+n8),A */
  mem_WriteByte (mem_ReadByte (Regs.w.PC++) + 0xFF00, Regs.b.A);
  break;
case 0xE1: /*	   POP HL */

  Regs.w.HL = mem_ReadWord (Regs.w.SP);
  Regs.w.SP += 2;
  break;
case 0xE2: /*	   LD ($FF00+C),A */

  mem_WriteByte ((UINT16) (0xFF00 + Regs.b.C), Regs.b.A);
  break;
case 0xE3: /*	   EH? */
  break;
case 0xE4: /*	   EH? */
  break;
case 0xE5: /*	   PUSH HL */

  Regs.w.SP -= 2;
  mem_WriteWord (Regs.w.SP, Regs.w.HL);
  break;
case 0xE6: /*	   AND A,n8 */

  x = mem_ReadByte (Regs.w.PC++);
  AND_A_X (x)
  break;
case 0xE7: /*	   RST $20 */

  Regs.w.SP -= 2;
  mem_WriteWord (Regs.w.SP, Regs.w.PC);
  Regs.w.PC = 0x20;
  break;
case 0xE8: /*	   ADD SP,n8 */
/*
 *	 Z - Reset.
 *	 N - Reset.
 *	 H - Set or reset according to operation.
 *	 C - Set or reset according to operation.
 */

  {
	register INT32 n;
	register UINT32 r1, r2;
	register UINT8 f;

    /* printf( "Hmmm.. ADD SP,n8\n" ); */

	n = (INT32) ((INT8) mem_ReadByte (Regs.w.PC++));
    r1 = Regs.w.SP + n;
    r2 = (Regs.w.SP & 0xFFF) + (n & 0xFFF);

    if (r1 > 0xFFFF)
    {
      f = FLAG_C;
    }
    else
    {
      f = 0;
    }

    if (r2 > 0xFFF)
    {
      f |= FLAG_H;
    }

	Regs.w.SP = (UINT16) r1;
    Regs.b.F = f;
  }
  break;
case 0xE9: /*	   JP (HL) */

  Regs.w.PC = Regs.w.HL;
  break;
case 0xEA: /*	   LD (n16),A */

  mem_WriteByte (mem_ReadWord (Regs.w.PC), Regs.b.A);
  Regs.w.PC += 2;
  break;
case 0xEB: /*	   EH? */
  break;
case 0xEC: /*	   EH? */
  break;
case 0xED: /*	   EH? */
  break;
case 0xEE: /*	   XOR A,n8 */

  x = mem_ReadByte (Regs.w.PC++);
  XOR_A_X (x)
  break;
case 0xEF: /*	   RST $28 */

  Regs.w.SP -= 2;
  mem_WriteWord (Regs.w.SP, Regs.w.PC);
  Regs.w.PC = 0x28;
  break;
case 0xF0: /*	   LD A,($FF00+n8) */

  Regs.b.A = mem_ReadByte (0xFF00 + mem_ReadByte (Regs.w.PC++));
  break;
case 0xF1: /*	   POP AF */

  Regs.w.AF = (UINT16) (mem_ReadWord (Regs.w.SP) & 0xFFF0);
  Regs.w.SP += 2;
  break;
case 0xF2: /*	   LD A,($FF00+C) */

  Regs.b.A = mem_ReadByte ((UINT16) (0xFF00 + Regs.b.C));
  break;
case 0xF3: /*	   DI */

  Regs.w.enable &= ~IME;
  break;
case 0xF4: /*	   EH? */
  break;
case 0xF5: /*	   PUSH AF */

  Regs.w.SP -= 2;
  mem_WriteWord (Regs.w.SP, (UINT16) (Regs.w.AF & 0xFFF0));
  break;
case 0xF6: /*	   OR A,n8 */

  x = mem_ReadByte (Regs.w.PC++);
  OR_A_X (x)
  break;
case 0xF7: /*	   RST $30 */

  Regs.w.SP -= 2;
  mem_WriteWord (Regs.w.SP, Regs.w.PC);
  Regs.w.PC = 0x30;
  break;
case 0xF8: /*	   LD HL,SP+n8 */
/*
 *	 n = one UINT8 signed immediate value.
 * Flags affected:
 *	 Z - Reset.
 *	 N - Reset.
 *	 H - Set or reset according to operation.
 *	 C - Set or reset according to operation.
 *
 */

  {
	register INT32 n;
	register UINT32 r1, r2;
	register UINT8 f;

	n = (INT32) ((INT8) mem_ReadByte (Regs.w.PC++));
    r1 = Regs.w.SP + n;
    r2 = (Regs.w.SP & 0xFFF) + (n & 0xFFF);

    if (r1 > 0xFFFF)
    {
      f = FLAG_C;
    }
    else
    {
      f = 0;
    }

    if (r2 > 0xFFF)
    {
      f |= FLAG_H;
    }

	Regs.w.HL = (UINT16) r1;
    Regs.b.F = f;
  }
  break;
case 0xF9: /*	   LD SP,HL */

  Regs.w.SP = Regs.w.HL;
  break;
case 0xFA: /*	   LD A,(n16) */

  Regs.b.A = mem_ReadByte (mem_ReadWord (Regs.w.PC));
  Regs.w.PC += 2;
  break;
case 0xFB: /*	   EI */

  Regs.w.enable |= IME;
  CheckInterrupts = 1;
  break;
case 0xFC: /*	   EH? */
  break;
case 0xFD: /*	   EH? */
  break;
case 0xFE: /*	   CP A,n8 */

  x = mem_ReadByte (Regs.w.PC++);
  CP_A_X (x)
  break;
case 0xFF: /*	   RST $38 */

  Regs.w.SP -= 2;
  mem_WriteWord (Regs.w.SP, Regs.w.PC);
  Regs.w.PC = 0x38;
  break;
