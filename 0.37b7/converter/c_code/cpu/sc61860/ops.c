INLINE UINT8 READ_OP(void) 
{
	return cpu_readop(sc61860.pc++);
}

INLINE UINT8 READ_OP_ARG(void) 
{
	return cpu_readop_arg(sc61860.pc++);
}

INLINE UINT16 READ_OP_ARG_WORD(void)
{
	UINT16 t=cpu_readop(sc61860.pc++)<<8;
	t|=cpu_readop(sc61860.pc++);
	return t;
}

INLINE UINT8 READ_BYTE(UINT16 adr) 
{
	return cpu_readmem16(adr);
}

INLINE void WRITE_BYTE(UINT16 a,UINT8 v) 
{
	cpu_writemem16(a,v);
}

#define PUSH(v) sc61860.ram[--sc61860.r]=v
#define POP() sc61860.ram[sc61860.r++]

INLINE void sc61860_load_imm(int r, UINT8 v)
{
	sc61860.ram[r]=v;
}

INLINE void sc61860_load(void)
{
	sc61860.ram[A]=sc61860.ram[sc61860.p];
}

INLINE void sc61860_load_imm_p(UINT8 v)
{
	sc61860.p=v;
}

INLINE void sc61860_load_imm_q(UINT8 v)
{
	sc61860.q=v;
}

INLINE void sc61860_load_r(void)
{
	sc61860.r=sc61860.ram[A];
}

INLINE void sc61860_load_ext(int r)
{
	sc61860.ram[r]=READ_BYTE(sc61860.dp);
}

INLINE void sc61860_load_dp(void)
{
	sc61860.dp=READ_OP_ARG_WORD();
}

INLINE void sc61860_load_dl(void)
{
	sc61860.dp=(sc61860.dp&~0xff)|READ_OP_ARG();
}

INLINE void sc61860_store_p(void)
{
	sc61860.ram[A]=sc61860.p;
}

INLINE void sc61860_store_q(void)
{
	sc61860.ram[A]=sc61860.q;
}

INLINE void sc61860_store_r(void)
{
	sc61860.ram[A]=sc61860.r;
}

INLINE void sc61860_store_ext(int r)
{
	WRITE_BYTE(sc61860.dp, sc61860.ram[r]);
}

INLINE void sc61860_exam(int a, int b)
{
	UINT8 t=sc61860.ram[a];
	sc61860.ram[a]=sc61860.ram[b];
	sc61860.ram[b]=t;
}

INLINE void sc61860_test(int reg, UINT8 value)
{
	sc61860.zero=(sc61860.ram[reg]&value)==0;
}

INLINE void sc61860_test_ext(void)
{
	sc61860.zero=(READ_BYTE(sc61860.dp)&READ_OP_ARG())==0;
}

INLINE void sc61860_and(int reg, UINT8 value)
{
	sc61860.zero=(sc61860.ram[reg]&=value)==0;
}

INLINE void sc61860_and_ext(void)
{
	UINT8 t=READ_BYTE(sc61860.dp)&READ_OP_ARG();
	sc61860.zero=t==0;
    WRITE_BYTE(sc61860.dp,t);
}

INLINE void sc61860_or(int reg, UINT8 value)
{
	sc61860.zero=(sc61860.ram[reg]|=value)==0;
}

INLINE void sc61860_or_ext(void)
{
	UINT8 t=READ_BYTE(sc61860.dp)|READ_OP_ARG();
	sc61860.zero=t==0;
    WRITE_BYTE(sc61860.dp,t);
}

INLINE void sc61860_rotate_right(void)
{
	int t=sc61860.ram[A];
	if (sc61860.carry) t|=0x100;
	sc61860.carry=t&1;
	sc61860.ram[A]=t>>1;
}

INLINE void sc61860_rotate_left(void)
{
	int t=sc61860.ram[A]<<1;
	if (sc61860.carry) t|=1;
	sc61860.carry=t&0x100;
	sc61860.ram[A]=t;
}

INLINE void sc61860_swap(void)
{
	int t=sc61860.ram[A];
	sc61860.ram[A]=(t<<4)|((t>>4)&0xf);
}

INLINE void sc61860_inc(int reg)
{
	sc61860.q=reg;
	sc61860.ram[reg]++;
	sc61860.zero=sc61860.carry=sc61860.ram[reg]==0;
}

INLINE void sc61860_inc_p(void)
{
	sc61860.p++;
}

INLINE void sc61860_dec(int reg)
{
	sc61860.q=reg;
	sc61860.ram[reg]--;
	sc61860.zero=sc61860.ram[reg]==0;
	sc61860.carry=sc61860.ram[reg]==0xff;
}

INLINE void sc61860_dec_p(void)
{
	sc61860.p--;
}

INLINE void sc61860_add(int reg, UINT8 value)
{
	int t=sc61860.ram[reg]+value;
	sc61860.zero=(sc61860.ram[reg]=t)==0;
	sc61860.carry=t>=0x100;
}

INLINE void sc61860_add_carry(void)
{
	int t=sc61860.ram[sc61860.p]+sc61860.ram[A];
	if (sc61860.carry) t++;
	sc61860.zero=(sc61860.ram[sc61860.p]=t)==0;
	sc61860.carry=t>=0x100;
}

INLINE void sc61860_add_word(void)
{
	int t=sc61860.ram[sc61860.p]+sc61860.ram[A],t2;
	sc61860.ram[sc61860.p]=t;
	t2=sc61860.ram[sc61860.p+1]+sc61860.ram[B];
	if (t>=0x100) t2++;
	sc61860.ram[sc61860.p+1]=t2;
	sc61860.zero=t2==0;
	sc61860.carry=t2>=0x100;
}


INLINE void sc61860_sub(int reg, UINT8 value)
{
	int t=sc61860.ram[reg]-value;
	sc61860.zero=(sc61860.ram[reg]=t)==0;
	sc61860.carry=t<0;
}

INLINE void sc61860_sub_carry(void)
{
	int t=sc61860.ram[sc61860.p]-sc61860.ram[A];
	if (sc61860.carry) t--;
	sc61860.zero=(sc61860.ram[sc61860.p]=t)==0;
	sc61860.carry=t<0;
}


INLINE void sc61860_sub_word(void)
{
	int t=sc61860.ram[sc61860.p]-sc61860.ram[A],t2;
	sc61860.ram[sc61860.p]=t;
	t2=sc61860.ram[sc61860.p+1]-sc61860.ram[B];
	if (t<0) t2--;
	sc61860.ram[sc61860.p+1]=t2;
	sc61860.zero=t2==0;
	sc61860.carry=t2<0;
}

INLINE void sc61860_cmp(int reg, UINT8 value)
{
	int t=sc61860.ram[reg]-value;
	sc61860.zero=t==0;
	sc61860.carry=t<0;
}

INLINE void sc61860_pop(void)
{
	sc61860.ram[A]=POP();
}

INLINE void sc61860_push(void)
{
	PUSH(sc61860.ram[A]);
}

INLINE void sc61860_prepare_table_call(void)
{
	int adr;
	sc61860.ram[H]=READ_OP();
	adr=READ_OP_ARG_WORD();
	PUSH(adr>>8);
	PUSH(adr&0xff);
}

INLINE void sc61860_execute_table_call(void)
{
	int i, v, adr;
	for (i=0; i<sc61860.ram[H]; i++) {
		v=READ_OP();
		adr=READ_OP_ARG_WORD();
		sc61860.zero=v==sc61860.ram[A];
		if (sc61860.zero) {
			sc61860.pc=adr;
			change_pc(sc61860.pc);
			return;
		}
	}
	sc61860.pc=READ_OP_ARG_WORD();
	change_pc(sc61860.pc);
}


INLINE void sc61860_call(UINT16 adr)
{
	PUSH(sc61860.pc>>8);
	PUSH(sc61860.pc&0xff);
	sc61860.pc=adr;
	change_pc(sc61860.pc);
}

INLINE void sc61860_return(void)
{
	UINT16 t=POP();
	t|=POP()<<8;
	sc61860.pc=t;
	change_pc(sc61860.pc);
}

INLINE void sc61860_jump(bool yes)
{
	UINT16 adr=READ_OP_ARG_WORD();
	if (yes) {
		sc61860.pc=adr;
		change_pc(sc61860.pc);
	}
}

INLINE void sc61860_jump_rel_plus(bool yes)
{
	UINT16 adr=sc61860.pc;
	adr+=READ_OP_ARG();
	if (yes) {
		sc61860.pc=adr;
		change_pc(sc61860.pc);
		sc61860_icount-=3;
	}
}

INLINE void sc61860_jump_rel_minus(bool yes)
{
	UINT16 adr=sc61860.pc;
	adr-=READ_OP_ARG();
	if (yes) {
		sc61860.pc=adr;
		change_pc(sc61860.pc);
		sc61860_icount-=3;
	}
}

INLINE void sc61860_loop(void)
{
	UINT16 adr=sc61860.pc;
	adr-=READ_OP_ARG();
	sc61860.ram[sc61860.r]--;
	sc61860.zero=sc61860.ram[sc61860.r]==0;
	sc61860.carry=sc61860.ram[sc61860.r]==0xff;
	if (!sc61860.carry) {
		sc61860.pc=adr;
		adr=POP();
		change_pc(sc61860.pc);
		sc61860_icount-=3;
	}
}

INLINE void sc61860_leave(void)
{
	sc61860.ram[sc61860.r]=0;
}

INLINE void sc61860_wait(void)
{
	int t=READ_OP();
	sc61860_icount-=t;
	sc61860_icount-=t;
	sc61860_icount-=3;
}

INLINE void sc61860_set_carry(void)
{
	sc61860.carry=1;
	sc61860.zero=1;
}

INLINE void sc61860_reset_carry(void)
{
	sc61860.carry=0;
	sc61860.zero=1;
}

INLINE void sc61860_out_a(void)
{
	sc61860.q=IA;
	if (sc61860.config&&sc61860.config->outa) sc61860.config->outa(sc61860.ram[IA]);
}

INLINE void sc61860_out_b(void)
{
	sc61860.q=IB;
	if (sc61860.config&&sc61860.config->outb) sc61860.config->outb(sc61860.ram[IB]);
}

INLINE void sc61860_out_f(void)
{
	sc61860.q=F0;
	/*sc61860.ram[F0]; */
}


/*   c0 display on
   c1 counter reset
   c2 cpu halt
   c3 computer off
   c4 beeper frequency (1 4khz, 0 2khz), or (c5=0) membran pos1/pos2
   c5 beeper on
   c6 beeper steuerung*/
INLINE void sc61860_out_c(void)
{
	sc61860.q=C;
	if (sc61860.config&&sc61860.config->outc) sc61860.config->outc(sc61860.ram[C]);
}

INLINE void sc61860_in_a(void)
{
	int data=0;
	if (sc61860.config&&sc61860.config->ina) data=sc61860.config->ina();
	sc61860.ram[A]=data;
	sc61860.zero=data==0;
}

INLINE void sc61860_in_b(void)
{
	int data=0;
	if (sc61860.config&&sc61860.config->inb) data=sc61860.config->inb();
	sc61860.ram[A]=data;
	sc61860.zero=data==0;
}

/* 0 systemclock 512ms
   1 systemclock 2ms
   2 ?
   3 brk/on key
   4 ?
   5 ?
   6 reset
   7 cassette input */
INLINE void sc61860_test_special(void)
{
	int t=0;
	if (sc61860.timer.t512ms) t|=1;
	if (sc61860.timer.t2ms) t|=2;
	if (sc61860.config&&sc61860.config->brk&&sc61860.config->brk()) t|=8;
	if (sc61860.config&&sc61860.config->reset&&sc61860.config->reset()) t|=0x40;

	sc61860.zero=(t&READ_OP())==0;
}

/************************************************************************************
 "string" operations
***********************************************************************************/
INLINE void sc61860_add_bcd_a(void)
{	
	int i,t, v=sc61860.ram[A];
	for (i=0; i<=sc61860.ram[I]; i++) {
		t=(sc61860.ram[sc61860.p]&0xf)+(v&0xf);
		if ((t&0xf)>9) t=t+0x10-10;
		t+=(sc61860.ram[sc61860.p]&0xf0)+(v&0xf0);
		if ((t&0xf0)>=0xa0) { t=t+0x100-0xa0; }
		sc61860.ram[sc61860.p--]=t;
		sc61860.zero=(t&0xff)==0;
		sc61860.carry=t>=0x100;
		v=(sc61860.carry)?1:0;
		sc61860_icount-=3;
	}
}

INLINE void sc61860_add_bcd(void)
{	
	int i,t,v=0;
	for (i=0; i<=sc61860.ram[I]; i++) {
		t=(sc61860.ram[sc61860.p]&0xf)+(sc61860.ram[sc61860.q]&0xf)+v;
		if (t>=10) t=t+0x10-10;
		t+=(sc61860.ram[sc61860.p]&0xf0)+(sc61860.ram[sc61860.q--]&0xf0);
		if ((t&0xf0)>=0xa0) { t=t+0x100-0xa0; }
		sc61860.ram[sc61860.p--]=t;
		sc61860.zero=(t&0xff)==0;
		sc61860.carry=t>=0x100;
		v=(sc61860.carry)?1:0;
		sc61860_icount-=3;
	}
}

INLINE void sc61860_sub_bcd_a(void)
{	
	int i,t, v=sc61860.ram[A];
	for (i=0; i<=sc61860.ram[I]; i++) {
		t=(sc61860.ram[sc61860.p]&0xf)-(v&0xf);
		if (t<0) t=t-0x10+10;
		t+=(sc61860.ram[sc61860.p]&0xf0)-(v&0xf0);
		if (t<0) { t=t-0x100+0xa0; }
		sc61860.ram[sc61860.p--]=t;
		sc61860.zero=(t&0xff)==0;
		sc61860.carry=t<0;
		v=(sc61860.carry)?1:0;
		sc61860_icount-=3;
	}
}

INLINE void sc61860_sub_bcd(void)
{	
	int i,t,v=0;
	for (i=0; i<=sc61860.ram[I]; i++) {
		t=(sc61860.ram[sc61860.p]&0xf)-(sc61860.ram[sc61860.q]&0xf)-v;
		if (t<0) t=t-0x10+10;
		t+=(sc61860.ram[sc61860.p]&0xf0)-(sc61860.ram[sc61860.q--]&0xf0);
		if (t<0) { t=t-0x100+0xa0; }
		sc61860.ram[sc61860.p--]=t;
		sc61860.zero=(t&0xff)==0;
		sc61860.carry=t<0;
		v=(sc61860.carry)?1:0;
		sc61860_icount-=3;
	}
}

/* side effect p-i-1 -> p correct! */
INLINE void sc61860_shift_left_nibble(void)
{	
	int i,t=0;
	for (i=0; i<=sc61860.ram[I]; i++) {
		t|=sc61860.ram[sc61860.p]<<4;
		sc61860.ram[sc61860.p--]=t;
		t>>=8;
		sc61860_icount--;
	}
}

/* side effect p+i+1 -> p correct! */
INLINE void sc61860_shift_right_nibble(void)
{	
	int i,t=0;
	for (i=0; i<=sc61860.ram[I]; i++) {
		t|=sc61860.ram[sc61860.p];
		sc61860.ram[sc61860.p++]=t>>4;
		t=(t<<8)&0xf00;
		sc61860_icount--;
	}
}

INLINE void sc61860_inc_load_dp(int reg)
{
	if (++sc61860.ram[reg]==0) sc61860.ram[reg+1]++;
	sc61860.dp=sc61860.ram[reg]|(sc61860.ram[reg+1]<<8);
}

INLINE void sc61860_dec_load_dp(int reg)
{
	if (--sc61860.ram[reg]==0xff) sc61860.ram[reg+1]--;
	sc61860.dp=sc61860.ram[reg]|(sc61860.ram[reg+1]<<8);
}

INLINE void sc61860_inc_load_dp_load(void)
{
	if (++sc61860.ram[XL]==0) sc61860.ram[XH]++;
	sc61860.dp=sc61860.ram[XL]|(sc61860.ram[XH]<<8);
	sc61860.ram[A]=READ_BYTE(sc61860.dp);
}

INLINE void sc61860_dec_load_dp_load(void)
{
	if (--sc61860.ram[XL]==0xff) sc61860.ram[XH]--;
	sc61860.dp=sc61860.ram[XL]|(sc61860.ram[XH]<<8);
	sc61860.ram[A]=READ_BYTE(sc61860.dp);
}

INLINE void sc61860_inc_load_dp_store(void)
{
	if (++sc61860.ram[YL]==0) sc61860.ram[YH]++;
	sc61860.dp=sc61860.ram[YL]|(sc61860.ram[YH]<<8);
	WRITE_BYTE(sc61860.dp,sc61860.ram[A]);
}

INLINE void sc61860_dec_load_dp_store(void)
{
	if (--sc61860.ram[YL]==0xff) sc61860.ram[YH]--;
	sc61860.dp=sc61860.ram[YL]|(sc61860.ram[YH]<<8);
	WRITE_BYTE(sc61860.dp,sc61860.ram[A]);
}

INLINE void sc61860_fill(void)
{
	int i;
	for (i=0;i<=sc61860.ram[I];i++) {
		sc61860.ram[sc61860.p++]=sc61860.ram[A]; /* could be overwritten? */
		sc61860_icount--;
	}
}

INLINE void sc61860_fill_ext(void)
{
	int i;
	for (i=0;i<=sc61860.ram[I];i++) {
		WRITE_BYTE(sc61860.dp, sc61860.ram[A]);
		if (i!=sc61860.ram[I]) sc61860.dp++;
		sc61860_icount-=3;
	}
}

INLINE void sc61860_copy(int count)
{
	int i;
	for (i=0; i<=count; i++) {
		sc61860.ram[sc61860.p++]=sc61860.ram[sc61860.q++];
		sc61860_icount-=2;
	}

}

INLINE void sc61860_copy_ext(int count)
{
	int i;
	for (i=0; i<=count; i++) {
		sc61860.ram[sc61860.p++]=READ_BYTE(sc61860.dp);
		if (i!=count) sc61860.dp++;
		sc61860_icount-=4;
	}
}

INLINE void sc61860_copy_int(int count)
{
	int i;
	for (i=0; i<=count; i++) {
		sc61860.ram[sc61860.p++]=
			READ_BYTE((sc61860.ram[A]|(sc61860.ram[B]<<8)) ); /* internal rom! */
		if (i!=count) {
			if (++sc61860.ram[A]==0) sc61860.ram[B]++;
		}
		sc61860_icount-=4;
	}
}

INLINE void sc61860_exchange(int count)
{
	int i;
	UINT8 t;
	for (i=0; i<=count; i++) {
		t=sc61860.ram[sc61860.p];
		sc61860.ram[sc61860.p++]=sc61860.ram[sc61860.q];
		sc61860.ram[sc61860.q++]=t;	
		sc61860_icount-=3;
	}
}

INLINE void sc61860_exchange_ext(int count)
{
	int i;
	UINT8 t;
	for (i=0; i<=count; i++) {
		t=sc61860.ram[sc61860.p];
		sc61860.ram[sc61860.p++]=READ_BYTE(sc61860.dp);
		WRITE_BYTE(sc61860.dp, t);
		if (i!=count) sc61860.dp++;
		sc61860_icount-=6;
	}
}
