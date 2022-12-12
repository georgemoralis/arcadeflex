#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>


int atoh(char* buff);
char* modify_ea_string(char* old_ea_string, char* insert_string);
char* modify_imm_string(char* old_ea_string, char* insert_string);
char* replace_clk_string(char* old_clk_string, char* replace_string, int add_value);
int get_clk_add(int func_num, int ea_mode);

void add_op_header(FILE* filep, char low, char high);
void add_prototype_header(FILE* filep);
void add_prototype_footer(FILE* filep);
void add_table_header(FILE* filep);
void add_table_footer(FILE* filep);

int generate_funcs(FILE* file_ac, FILE* file_dm, FILE* file_nz);
int generate_table(FILE* table_file);
int generate_prototypes(FILE* prototype_file);

#define PRINT_TABLE_ENTRY(file, name, mask, match, clks) \
{ \
    unsigned bits = mask; \
    bits = ((bits & 0xaaaa) >> 1) + (bits & 0x5555); \
    bits = ((bits & 0xcccc) >> 2) + (bits & 0x3333); \
    bits = ((bits & 0xf0f0) >> 4) + (bits & 0x0f0f); \
    bits = ((bits & 0xff00) >> 8) + (bits & 0x00ff); \
    fprintf(file, "\t{%-24s, %2d, 0x%04x, 0x%04x},\n", name, bits, mask, match); \
}

#define PRINT_PROTOTYPE(file, name) fprintf(file, "void %s(void);\n", name)


typedef struct
{
	char* name; /* handler function */
	int op_mask;	/* mask on opcode */
	int op_match;	/* what to match after masking */
	int ea_mask;	/* what ea modes are allowed */
	int size;		/* operation size (0=8, 1=16, 2=32, 3=unsized) */
	int base_cycles; /* base cycles used on real 68000 (-1 = special) */
} opcode_handler_struct;


int g_errors = 0;
char g_m68k_in_c[255+1] = "m68k_in.c";


int g_ea_8_cycle_table[64]=
{
    0,  0,  0,  0,  0,  0,  0,  0, /* 000 xxx data register direct */
    0,  0,  0,  0,  0,  0,  0,  0, /* 001 xxx address register direct */
    4,  4,  4,  4,  4,  4,  4,  4, /* 010 xxx address register indirect */
    4,  4,  4,  4,  4,  4,  4,  4, /* 011 xxx address register indirect with postincrement */
    6,  6,  6,  6,  6,  6,  6,  6, /* 100 xxx address register indirect with predecrement */
    8,  8,  8,  8,  8,  8,  8,  8, /* 101 xxx address register indirect with displacement */
   10, 10, 10, 10, 10, 10, 10, 10, /* 110 xxx address register indirect with index */
    8,                             /* 111 000 absolute short */
   12,                             /* 111 001 absolute long */
    8,                             /* 111 010 program counter with displacement */
   10,                             /* 111 011 program counter with index */
    4,                             /* 111 100 immediate */
    0,                             /* 111 101 <invalid> */
    0,                             /* 111 110 <invalid> */
    0,                             /* 111 111 <invalid> */
};

int g_ea_32_cycle_table[64]=
{
    0,  0,  0,  0,  0,  0,  0,  0, /* 000 xxx data register direct */
    0,  0,  0,  0,  0,  0,  0,  0, /* 001 xxx address register direct */
    8,  8,  8,  8,  8,  8,  8,  8, /* 010 xxx address register indirect */
    8,  8,  8,  8,  8,  8,  8,  8, /* 011 xxx address register indirect with postincrement */
   10, 10, 10, 10, 10, 10, 10, 10, /* 100 xxx address register indirect with predecrement */
   12, 12, 12, 12, 12, 12, 12, 12, /* 101 xxx address register indirect with displacement */
   14, 14, 14, 14, 14, 14, 14, 14, /* 110 xxx address register indirect with index */
   12,                             /* 111 000 absolute short */
   16,                             /* 111 001 absolute long */
   12,                             /* 111 010 program counter with displacement */
   14,                             /* 111 011 program counter with index */
    8,                             /* 111 100 immediate */
    0,                             /* 111 101 <invalid> */
    0,                             /* 111 110 <invalid> */
    0,                             /* 111 111 <invalid> */
};

int* g_ea_cycle_table[3] = {g_ea_8_cycle_table, g_ea_8_cycle_table, g_ea_32_cycle_table};

int g_jmp_cycle_table[64]=
{
    0,  0,  0,  0,  0,  0,  0,  0, /* 000 xxx data register direct */
    0,  0,  0,  0,  0,  0,  0,  0, /* 001 xxx address register direct */
    8,  8,  8,  8,  8,  8,  8,  8, /* 010 xxx address register indirect */
    0,  0,  0,  0,  0,  0,  0,  0, /* 011 xxx address register indirect with postincrement */
    0,  0,  0,  0,  0,  0,  0,  0, /* 100 xxx address register indirect with predecrement */
   10, 10, 10, 10, 10, 10, 10, 10, /* 101 xxx address register indirect with displacement */
   14, 14, 14, 14, 14, 14, 14, 14, /* 110 xxx address register indirect with index */
   10,                             /* 111 000 absolute short */
   12,                             /* 111 001 absolute long */
   10,                             /* 111 010 program counter with displacement */
   14,                             /* 111 011 program counter with index */
    0,                             /* 111 100 immediate */
    0,                             /* 111 101 <invalid> */
    0,                             /* 111 110 <invalid> */
    0,                             /* 111 111 <invalid> */
};

int g_jsr_cycle_table[64]=
{
    0,  0,  0,  0,  0,  0,  0,  0, /* 000 xxx data register direct */
    0,  0,  0,  0,  0,  0,  0,  0, /* 001 xxx address register direct */
   16, 16, 16, 16, 16, 16, 16, 16, /* 010 xxx address register indirect */
    0,  0,  0,  0,  0,  0,  0,  0, /* 011 xxx address register indirect with postincrement */
    0,  0,  0,  0,  0,  0,  0,  0, /* 100 xxx address register indirect with predecrement */
   18, 18, 18, 18, 18, 18, 18, 18, /* 101 xxx address register indirect with displacement */
   22, 22, 22, 22, 22, 22, 22, 22, /* 110 xxx address register indirect with index */
   18,                             /* 111 000 absolute short */
   20,                             /* 111 001 absolute long */
   18,                             /* 111 010 program counter with displacement */
   22,                             /* 111 011 program counter with index */
    0,                             /* 111 100 immediate */
    0,                             /* 111 101 <invalid> */
    0,                             /* 111 110 <invalid> */
    0,                             /* 111 111 <invalid> */
};

int g_lea_cycle_table[64]=
{
    0,  0,  0,  0,  0,  0,  0,  0, /* 000 xxx data register direct */
    0,  0,  0,  0,  0,  0,  0,  0, /* 001 xxx address register direct */
    4,  4,  4,  4,  4,  4,  4,  4, /* 010 xxx address register indirect */
    0,  0,  0,  0,  0,  0,  0,  0, /* 011 xxx address register indirect with postincrement */
    0,  0,  0,  0,  0,  0,  0,  0, /* 100 xxx address register indirect with predecrement */
    8,  8,  8,  8,  8,  8,  8,  8, /* 101 xxx address register indirect with displacement */
   12, 12, 12, 12, 12, 12, 12, 12, /* 110 xxx address register indirect with index */
    8,                             /* 111 000 absolute short */
   12,                             /* 111 001 absolute long */
    8,                             /* 111 010 program counter with displacement */
   12,                             /* 111 011 program counter with index */
    0,                             /* 111 100 immediate */
    0,                             /* 111 101 <invalid> */
    0,                             /* 111 110 <invalid> */
    0,                             /* 111 111 <invalid> */
};

int g_pea_cycle_table[64]=
{
    0,  0,  0,  0,  0,  0,  0,  0, /* 000 xxx data register direct */
    0,  0,  0,  0,  0,  0,  0,  0, /* 001 xxx address register direct */
   12, 12, 12, 12, 12, 12, 12, 12, /* 010 xxx address register indirect */
    0,  0,  0,  0,  0,  0,  0,  0, /* 011 xxx address register indirect with postincrement */
    0,  0,  0,  0,  0,  0,  0,  0, /* 100 xxx address register indirect with predecrement */
   16, 16, 16, 16, 16, 16, 16, 16, /* 101 xxx address register indirect with displacement */
   20, 20, 20, 20, 20, 20, 20, 20, /* 110 xxx address register indirect with index */
   16,                             /* 111 000 absolute short */
   20,                             /* 111 001 absolute long */
   16,                             /* 111 010 program counter with displacement */
   20,                             /* 111 011 program counter with index */
    0,                             /* 111 100 immediate */
    0,                             /* 111 101 <invalid> */
    0,                             /* 111 110 <invalid> */
    0,                             /* 111 111 <invalid> */
};

int g_moves_bw_cycle_table[64]=
{
    0,  0,  0,  0,  0,  0,  0,  0, /* 000 xxx data register direct */
    0,  0,  0,  0,  0,  0,  0,  0, /* 001 xxx address register direct */
   18, 18, 18, 18, 18, 18, 18, 18, /* 010 xxx address register indirect */
   20, 20, 20, 20, 20, 20, 20, 20, /* 011 xxx address register indirect with postincrement */
   20, 20, 20, 20, 20, 20, 20, 20, /* 100 xxx address register indirect with predecrement */
   20, 20, 20, 20, 20, 20, 20, 20, /* 101 xxx address register indirect with displacement */
   24, 24, 24, 24, 24, 24, 24, 24, /* 110 xxx address register indirect with index */
   20,                             /* 111 000 absolute short */
   24,                             /* 111 001 absolute long */
    0,                             /* 111 010 program counter with displacement */
    0,                             /* 111 011 program counter with index */
    0,                             /* 111 100 immediate */
    0,                             /* 111 101 <invalid> */
    0,                             /* 111 110 <invalid> */
    0,                             /* 111 111 <invalid> */
};

int g_moves_l_cycle_table[64]=
{
    0,  0,  0,  0,  0,  0,  0,  0, /* 000 xxx data register direct */
    0,  0,  0,  0,  0,  0,  0,  0, /* 001 xxx address register direct */
   22, 22, 22, 22, 22, 22, 22, 22, /* 010 xxx address register indirect */
   24, 24, 24, 24, 24, 24, 24, 24, /* 011 xxx address register indirect with postincrement */
   24, 24, 24, 24, 24, 24, 24, 24, /* 100 xxx address register indirect with predecrement */
   24, 24, 24, 24, 24, 24, 24, 24, /* 101 xxx address register indirect with displacement */
   28, 28, 28, 28, 28, 28, 28, 28, /* 110 xxx address register indirect with index */
   24,                             /* 111 000 absolute short */
   28,                             /* 111 001 absolute long */
    0,                             /* 111 010 program counter with displacement */
    0,                             /* 111 011 program counter with index */
    0,                             /* 111 100 immediate */
    0,                             /* 111 101 <invalid> */
    0,                             /* 111 110 <invalid> */
    0,                             /* 111 111 <invalid> */
};

static opcode_handler_struct g_func_table[] =
{
/*  name                       mask   match    ea   size  base clks */
	{"m68000_1010"           , 0xf000, 0xa000, 0x000, 1,   0},
	{"m68000_1111"           , 0xf000, 0xf000, 0x000, 1,   0},
	{"m68000_abcd_rr"        , 0xf1f8, 0xc100, 0x000, 0,   6},
	{"m68000_abcd_mm_ax7"    , 0xfff8, 0xcf08, 0x000, 0,  18},
	{"m68000_abcd_mm_ay7"    , 0xf1ff, 0xc10f, 0x000, 0,  18},
	{"m68000_abcd_mm_axy7"   , 0xffff, 0xcf0f, 0x000, 0,  18},
	{"m68000_abcd_mm"        , 0xf1f8, 0xc108, 0x000, 0,  18},
	{"m68000_add_er_d_8"     , 0xf1f8, 0xd000, 0x000, 0,   4},
	{"m68000_add_er_8"       , 0xf1c0, 0xd000, 0xbff, 0,   4},
	{"m68000_add_er_d_16"    , 0xf1f8, 0xd040, 0x000, 1,   4},
	{"m68000_add_er_a_16"    , 0xf1f8, 0xd048, 0x000, 1,   4},
	{"m68000_add_er_16"      , 0xf1c0, 0xd040, 0xfff, 1,   4},
	{"m68000_add_er_d_32"    , 0xf1f8, 0xd080, 0x000, 2,   8},
	{"m68000_add_er_a_32"    , 0xf1f8, 0xd088, 0x000, 2,   8},
	{"m68000_add_er_32"      , 0xf1c0, 0xd080, 0xfff, 2,   6}, /* 8 for imm */
	{"m68000_add_re_8"       , 0xf1c0, 0xd100, 0x3f8, 0,   8},
	{"m68000_add_re_16"      , 0xf1c0, 0xd140, 0x3f8, 1,   8},
	{"m68000_add_re_32"      , 0xf1c0, 0xd180, 0x3f8, 2,  12},
	{"m68000_adda_d_16"      , 0xf1f8, 0xd0c0, 0x000, 1,   8},
	{"m68000_adda_a_16"      , 0xf1f8, 0xd0c8, 0x000, 1,   8},
	{"m68000_adda_16"        , 0xf1c0, 0xd0c0, 0xfff, 1,   8},
	{"m68000_adda_d_32"      , 0xf1f8, 0xd1c0, 0x000, 2,   8},
	{"m68000_adda_a_32"      , 0xf1f8, 0xd1c8, 0x000, 2,   8},
	{"m68000_adda_32"        , 0xf1c0, 0xd1c0, 0xfff, 2,   6}, /* 8 for imm */
	{"m68000_addi_d_8"       , 0xfff8, 0x0600, 0x000, 0,   8},
	{"m68000_addi_8"         , 0xffc0, 0x0600, 0xbf8, 0,  12},
	{"m68000_addi_d_16"      , 0xfff8, 0x0640, 0x000, 1,   8},
	{"m68000_addi_16"        , 0xffc0, 0x0640, 0xbf8, 1,  12},
	{"m68000_addi_d_32"      , 0xfff8, 0x0680, 0x000, 2,  16},
	{"m68000_addi_32"        , 0xffc0, 0x0680, 0xbf8, 2,  20},
	{"m68000_addq_d_8"       , 0xf1f8, 0x5000, 0x000, 0,   4},
	{"m68000_addq_8"         , 0xf1c0, 0x5000, 0xbf8, 0,   8},
	{"m68000_addq_d_16"      , 0xf1f8, 0x5040, 0x000, 1,   4},
	{"m68000_addq_a_16"      , 0xf1f8, 0x5048, 0x000, 1,   4},
	{"m68000_addq_16"        , 0xf1c0, 0x5040, 0xff8, 1,   8},
	{"m68000_addq_d_32"      , 0xf1f8, 0x5080, 0x000, 2,   8},
	{"m68000_addq_a_32"      , 0xf1f8, 0x5088, 0x000, 2,   8},
	{"m68000_addq_32"        , 0xf1c0, 0x5080, 0xff8, 2,  12},
	{"m68000_addx_rr_8"      , 0xf1f8, 0xd100, 0x000, 0,   4},
	{"m68000_addx_rr_16"     , 0xf1f8, 0xd140, 0x000, 1,   4},
	{"m68000_addx_rr_32"     , 0xf1f8, 0xd180, 0x000, 2,   8},
	{"m68000_addx_mm_8_ax7"  , 0xfff8, 0xdf08, 0x000, 0,  18},
	{"m68000_addx_mm_8_ay7"  , 0xf1ff, 0xd10f, 0x000, 0,  18},
	{"m68000_addx_mm_8_axy7" , 0xffff, 0xdf0f, 0x000, 0,  18},
	{"m68000_addx_mm_8"      , 0xf1f8, 0xd108, 0x000, 0,  18},
	{"m68000_addx_mm_16"     , 0xf1f8, 0xd148, 0x000, 1,  18},
	{"m68000_addx_mm_32"     , 0xf1f8, 0xd188, 0x000, 2,  30},
	{"m68000_and_er_d_8"     , 0xf1f8, 0xc000, 0x000, 0,   4},
	{"m68000_and_er_8"       , 0xf1c0, 0xc000, 0xbff, 0,   4},
	{"m68000_and_er_d_16"    , 0xf1f8, 0xc040, 0x000, 1,   4},
	{"m68000_and_er_16"      , 0xf1c0, 0xc040, 0xbff, 1,   4},
	{"m68000_and_er_d_32"    , 0xf1f8, 0xc080, 0x000, 2,   8},
	{"m68000_and_er_32"      , 0xf1c0, 0xc080, 0xbff, 2,   6}, /* 8 for imm */
	{"m68000_and_re_8"       , 0xf1c0, 0xc100, 0x3f8, 0,   8},
	{"m68000_and_re_16"      , 0xf1c0, 0xc140, 0x3f8, 1,   8},
	{"m68000_and_re_32"      , 0xf1c0, 0xc180, 0x3f8, 2,  12},
	{"m68000_andi_to_ccr"    , 0xffff, 0x023c, 0x000, 0,  20},
	{"m68000_andi_to_sr"     , 0xffff, 0x027c, 0x000, 1,  20},
	{"m68000_andi_d_8"       , 0xfff8, 0x0200, 0x000, 0,   8},
	{"m68000_andi_8"         , 0xffc0, 0x0200, 0xbf8, 0,  12},
	{"m68000_andi_d_16"      , 0xfff8, 0x0240, 0x000, 1,   8},
	{"m68000_andi_16"        , 0xffc0, 0x0240, 0xbf8, 1,  12},
	{"m68000_andi_d_32"      , 0xfff8, 0x0280, 0x000, 2,  14},
	{"m68000_andi_32"        , 0xffc0, 0x0280, 0xbf8, 2,  20},
	{"m68000_asr_s_8"        , 0xf1f8, 0xe000, 0x000, 0,  14}, /* fix in code */
	{"m68000_asr_s_16"       , 0xf1f8, 0xe040, 0x000, 1,  14},
	{"m68000_asr_s_32"       , 0xf1f8, 0xe080, 0x000, 2,  16},
	{"m68000_asr_r_8"        , 0xf1f8, 0xe020, 0x000, 0,  14},
	{"m68000_asr_r_16"       , 0xf1f8, 0xe060, 0x000, 1,  22},
	{"m68000_asr_r_32"       , 0xf1f8, 0xe0a0, 0x000, 2,  38},
	{"m68000_asr_ea"         , 0xffc0, 0xe0c0, 0x3f8, 1,   8},
	{"m68000_asl_s_8"        , 0xf1f8, 0xe100, 0x000, 0,  14},
	{"m68000_asl_s_16"       , 0xf1f8, 0xe140, 0x000, 1,  14},
	{"m68000_asl_s_32"       , 0xf1f8, 0xe180, 0x000, 2,  16},
	{"m68000_asl_r_8"        , 0xf1f8, 0xe120, 0x000, 0,  14},
	{"m68000_asl_r_16"       , 0xf1f8, 0xe160, 0x000, 1,  22},
	{"m68000_asl_r_32"       , 0xf1f8, 0xe1a0, 0x000, 2,  38},
	{"m68000_asl_ea"         , 0xffc0, 0xe1c0, 0x3f8, 1,   8},
	{"m68000_bhi_16"         , 0xffff, 0x6200, 0x000, 1,  11}, /* fix in code */
	{"m68020_bhi_32"         , 0xffff, 0x62ff, 0x000, 2,  11},
	{"m68000_bhi_8"          , 0xff00, 0x6200, 0x000, 0,   9},
	{"m68000_bls_16"         , 0xffff, 0x6300, 0x000, 1,  11},
	{"m68020_bls_32"         , 0xffff, 0x63ff, 0x000, 2,  11},
	{"m68000_bls_8"          , 0xff00, 0x6300, 0x000, 0,   9},
	{"m68000_bcc_16"         , 0xffff, 0x6400, 0x000, 1,  11},
	{"m68020_bcc_32"         , 0xffff, 0x64ff, 0x000, 2,  11},
	{"m68000_bcc_8"          , 0xff00, 0x6400, 0x000, 0,   9},
	{"m68000_bcs_16"         , 0xffff, 0x6500, 0x000, 1,  11},
	{"m68020_bcs_32"         , 0xffff, 0x65ff, 0x000, 2,  11},
	{"m68000_bcs_8"          , 0xff00, 0x6500, 0x000, 0,   9},
	{"m68000_bne_16"         , 0xffff, 0x6600, 0x000, 1,  11},
	{"m68020_bne_32"         , 0xffff, 0x66ff, 0x000, 2,  11},
	{"m68000_bne_8"          , 0xff00, 0x6600, 0x000, 0,   9},
	{"m68000_beq_16"         , 0xffff, 0x6700, 0x000, 1,  11},
	{"m68020_beq_32"         , 0xffff, 0x67ff, 0x000, 2,  11},
	{"m68000_beq_8"          , 0xff00, 0x6700, 0x000, 0,   9},
	{"m68000_bvc_16"         , 0xffff, 0x6800, 0x000, 1,  11},
	{"m68020_bvc_32"         , 0xffff, 0x68ff, 0x000, 2,  11},
	{"m68000_bvc_8"          , 0xff00, 0x6800, 0x000, 0,   9},
	{"m68000_bvs_16"         , 0xffff, 0x6900, 0x000, 1,  11},
	{"m68020_bvs_32"         , 0xffff, 0x69ff, 0x000, 2,  11},
	{"m68000_bvs_8"          , 0xff00, 0x6900, 0x000, 0,   9},
	{"m68000_bpl_16"         , 0xffff, 0x6a00, 0x000, 1,  11},
	{"m68020_bpl_32"         , 0xffff, 0x6aff, 0x000, 2,  11},
	{"m68000_bpl_8"          , 0xff00, 0x6a00, 0x000, 0,   9},
	{"m68000_bmi_16"         , 0xffff, 0x6b00, 0x000, 1,  11},
	{"m68020_bmi_32"         , 0xffff, 0x6bff, 0x000, 2,  11},
	{"m68000_bmi_8"          , 0xff00, 0x6b00, 0x000, 0,   9},
	{"m68000_bge_16"         , 0xffff, 0x6c00, 0x000, 1,  11},
	{"m68020_bge_32"         , 0xffff, 0x6cff, 0x000, 2,  11},
	{"m68000_bge_8"          , 0xff00, 0x6c00, 0x000, 0,   9},
	{"m68000_blt_16"         , 0xffff, 0x6d00, 0x000, 1,  11},
	{"m68020_blt_32"         , 0xffff, 0x6dff, 0x000, 2,  11},
	{"m68000_blt_8"          , 0xff00, 0x6d00, 0x000, 0,   9},
	{"m68000_bgt_16"         , 0xffff, 0x6e00, 0x000, 1,  11},
	{"m68020_bgt_32"         , 0xffff, 0x6eff, 0x000, 2,  11},
	{"m68000_bgt_8"          , 0xff00, 0x6e00, 0x000, 0,   9},
	{"m68000_ble_16"         , 0xffff, 0x6f00, 0x000, 1,  11},
	{"m68020_ble_32"         , 0xffff, 0x6fff, 0x000, 2,  11},
	{"m68000_ble_8"          , 0xff00, 0x6f00, 0x000, 0,   9},
	{"m68000_bchg_r_d"       , 0xf1f8, 0x0140, 0x000, 2,   8},
	{"m68000_bchg_r"         , 0xf1c0, 0x0140, 0xbf8, 0,   8},
	{"m68000_bchg_s_d"       , 0xfff8, 0x0840, 0x000, 2,  12},
	{"m68000_bchg_s"         , 0xffc0, 0x0840, 0xbf8, 0,  12},
	{"m68000_bclr_r_d"       , 0xf1f8, 0x0180, 0x000, 2,  10},
	{"m68000_bclr_r"         , 0xf1c0, 0x0180, 0xbf8, 0,   8},
	{"m68000_bclr_s_d"       , 0xfff8, 0x0880, 0x000, 2,  14},
	{"m68000_bclr_s"         , 0xffc0, 0x0880, 0xbf8, 0,  12},
	{"m68020_bfchg_d"        , 0xfff8, 0xeac0, 0x000, 2,  12},
	{"m68020_bfchg"          , 0xffc0, 0xeac0, 0xa78, 2,  12},
	{"m68020_bfclr_d"        , 0xfff8, 0xecc0, 0x000, 2,  12},
	{"m68020_bfclr"          , 0xffc0, 0xecc0, 0xa78, 2,  12},
	{"m68020_bfexts_d"       , 0xfff8, 0xebc0, 0x000, 2,  12},
	{"m68020_bfexts"         , 0xffc0, 0xebc0, 0xa7b, 2,  12},
	{"m68020_bfextu_d"       , 0xfff8, 0xe9c0, 0x000, 2,  12},
	{"m68020_bfextu"         , 0xffc0, 0xe9c0, 0xa7b, 2,  12},
	{"m68020_bfffo_d"        , 0xfff8, 0xedc0, 0x000, 2,  12},
	{"m68020_bfffo"          , 0xffc0, 0xedc0, 0xa7b, 2,  12},
	{"m68020_bfins_d"        , 0xfff8, 0xefc0, 0x000, 2,  12},
	{"m68020_bfins"          , 0xffc0, 0xefc0, 0xa78, 2,  12},
	{"m68020_bfset_d"        , 0xfff8, 0xeec0, 0x000, 2,  12},
	{"m68020_bfset"          , 0xffc0, 0xeec0, 0xa78, 2,  12},
	{"m68020_bftst_d"        , 0xfff8, 0xe8c0, 0x000, 2,  12},
	{"m68020_bftst"          , 0xffc0, 0xe8c0, 0xa7b, 2,  12},
	{"m68010_bkpt"           , 0xfff8, 0x4848, 0x000, 0,  11},
	{"m68000_bra_16"         , 0xffff, 0x6000, 0x000, 1,  10},
	{"m68020_bra_32"         , 0xffff, 0x60ff, 0x000, 2,  10},
	{"m68000_bra_8"          , 0xff00, 0x6000, 0x000, 0,  10},
	{"m68000_bset_r_d"       , 0xf1f8, 0x01c0, 0x000, 2,   8},
	{"m68000_bset_r"         , 0xf1c0, 0x01c0, 0xbf8, 0,   8},
	{"m68000_bset_s_d"       , 0xfff8, 0x08c0, 0x000, 2,  12},
	{"m68000_bset_s"         , 0xffc0, 0x08c0, 0xbf8, 0,  12},
	{"m68000_bsr_16"         , 0xffff, 0x6100, 0x000, 1,  18},
	{"m68020_bsr_32"         , 0xffff, 0x61ff, 0x000, 2,  18},
	{"m68000_bsr_8"          , 0xff00, 0x6100, 0x000, 0,  18},
	{"m68000_btst_r_d"       , 0xf1f8, 0x0100, 0x000, 2,   6},
	{"m68000_btst_r"         , 0xf1c0, 0x0100, 0xbff, 0,   4},
	{"m68000_btst_s_d"       , 0xfff8, 0x0800, 0x000, 2,  10},
	{"m68000_btst_s"         , 0xffc0, 0x0800, 0xbfb, 0,   8},
	{"m68020_callm"          , 0xffc0, 0x06c0, 0x27b, 2,   8},
	{"m68020_cas_8"          , 0xffc0, 0x0ac0, 0x3f8, 0,  36},
	{"m68020_cas_16"         , 0xffc0, 0x0cc0, 0x3f8, 1,  36},
	{"m68020_cas_32"         , 0xffc0, 0x0ec0, 0x3f8, 2,  36},
	{"m68020_cas2_16"        , 0xffff, 0x0cfc, 0x000, 1,  36},
	{"m68020_cas2_32"        , 0xffff, 0x0efc, 0x000, 2,  36},
	{"m68000_chk_d_16"       , 0xf1f8, 0x4180, 0x000, 1,  10},
	{"m68000_chk_16"         , 0xf1c0, 0x4180, 0xbff, 1,  10},
	{"m68020_chk_d_32"       , 0xf1f8, 0x4100, 0x000, 1,  10},
	{"m68020_chk_32"         , 0xf1c0, 0x4100, 0xbff, 1,  10},
	{"m68020_chk2_cmp2_8"    , 0xffc0, 0x00c0, 0x27b, 0,  12},
	{"m68020_chk2_cmp2_16"   , 0xffc0, 0x02c0, 0x27b, 1,  12},
	{"m68020_chk2_cmp2_32"   , 0xffc0, 0x04c0, 0x27b, 2,  12},
	{"m68000_clr_d_8"        , 0xfff8, 0x4200, 0x000, 0,   4},
	{"m68000_clr_8"          , 0xffc0, 0x4200, 0xbf8, 0,   8},
	{"m68000_clr_d_16"       , 0xfff8, 0x4240, 0x000, 1,   4},
	{"m68000_clr_16"         , 0xffc0, 0x4240, 0xbf8, 1,   8},
	{"m68000_clr_d_32"       , 0xfff8, 0x4280, 0x000, 2,   6},
	{"m68000_clr_32"         , 0xffc0, 0x4280, 0xbf8, 2,  12},
	{"m68000_cmp_d_8"        , 0xf1f8, 0xb000, 0x000, 0,   4},
	{"m68000_cmp_8"          , 0xf1c0, 0xb000, 0xbff, 0,   4},
	{"m68000_cmp_d_16"       , 0xf1f8, 0xb040, 0x000, 1,   4},
	{"m68000_cmp_a_16"       , 0xf1f8, 0xb048, 0x000, 1,   4},
	{"m68000_cmp_16"         , 0xf1c0, 0xb040, 0xfff, 1,   4},
	{"m68000_cmp_d_32"       , 0xf1f8, 0xb080, 0x000, 2,   6},
	{"m68000_cmp_a_32"       , 0xf1f8, 0xb088, 0x000, 2,   6},
	{"m68000_cmp_32"         , 0xf1c0, 0xb080, 0xfff, 2,   6},
	{"m68000_cmpa_d_16"      , 0xf1f8, 0xb0c0, 0x000, 1,   6},
	{"m68000_cmpa_a_16"      , 0xf1f8, 0xb0c8, 0x000, 1,   6},
	{"m68000_cmpa_16"        , 0xf1c0, 0xb0c0, 0xfff, 1,   6},
	{"m68000_cmpa_d_32"      , 0xf1f8, 0xb1c0, 0x000, 2,   6},
	{"m68000_cmpa_a_32"      , 0xf1f8, 0xb1c8, 0x000, 2,   6},
	{"m68000_cmpa_32"        , 0xf1c0, 0xb1c0, 0xfff, 2,   6},
	{"m68000_cmpi_d_8"       , 0xfff8, 0x0c00, 0x000, 0,   8},
	{"m68000_cmpi_8"         , 0xffc0, 0x0c00, 0xbf8, 0,   8},
	{"m68020_cmpi_pcdi_8"    , 0xffff, 0x0c3a, 0x000, 0,   8},
	{"m68020_cmpi_pcix_8"    , 0xffff, 0x0c3b, 0x000, 0,   8},
	{"m68000_cmpi_d_16"      , 0xfff8, 0x0c40, 0x000, 1,   8},
	{"m68000_cmpi_16"        , 0xffc0, 0x0c40, 0xbf8, 1,   8},
	{"m68020_cmpi_pcdi_16"   , 0xffff, 0x0c7a, 0x000, 1,   8},
	{"m68020_cmpi_pcix_16"   , 0xffff, 0x0c7b, 0x000, 1,   8},
	{"m68000_cmpi_d_32"      , 0xfff8, 0x0c80, 0x000, 2,  14},
	{"m68000_cmpi_32"        , 0xffc0, 0x0c80, 0xbf8, 2,  12},
	{"m68020_cmpi_pcdi_32"   , 0xffff, 0x0cba, 0x000, 2,  12},
	{"m68020_cmpi_pcix_32"   , 0xffff, 0x0cbb, 0x000, 2,  12},
	{"m68000_cmpm_8_ax7"     , 0xfff8, 0xbf08, 0x000, 0,  12},
	{"m68000_cmpm_8_ay7"     , 0xf1ff, 0xb10f, 0x000, 0,  12},
	{"m68000_cmpm_8_axy7"    , 0xffff, 0xbf0f, 0x000, 0,  12},
	{"m68000_cmpm_8"         , 0xf1f8, 0xb108, 0x000, 0,  12},
	{"m68000_cmpm_16"        , 0xf1f8, 0xb148, 0x000, 1,  12},
	{"m68000_cmpm_32"        , 0xf1f8, 0xb188, 0x000, 2,  20},
	{"m68020_cpbcc"          , 0xf180, 0xf080, 0x000, 2,   0},
	{"m68020_cpdbcc"         , 0xf1f8, 0xf048, 0x000, 2,   0},
	{"m68020_cpgen"          , 0xf1c0, 0xf000, 0x000, 2,   0},
	{"m68020_cpscc"          , 0xf1c0, 0xf040, 0x000, 2,   0},
	{"m68020_cptrapcc"       , 0xf1f8, 0xf078, 0x000, 2,   0},
	{"m68000_dbt"            , 0xfff8, 0x50c8, 0x000, 1,  12}, /* fix in code */
	{"m68000_dbf"            , 0xfff8, 0x51c8, 0x000, 1,  10},
	{"m68000_dbhi"           , 0xfff8, 0x52c8, 0x000, 1,  11},
	{"m68000_dbls"           , 0xfff8, 0x53c8, 0x000, 1,  11},
	{"m68000_dbcc"           , 0xfff8, 0x54c8, 0x000, 1,  11},
	{"m68000_dbcs"           , 0xfff8, 0x55c8, 0x000, 1,  11},
	{"m68000_dbne"           , 0xfff8, 0x56c8, 0x000, 1,  11},
	{"m68000_dbeq"           , 0xfff8, 0x57c8, 0x000, 1,  11},
	{"m68000_dbvc"           , 0xfff8, 0x58c8, 0x000, 1,  11},
	{"m68000_dbvs"           , 0xfff8, 0x59c8, 0x000, 1,  11},
	{"m68000_dbpl"           , 0xfff8, 0x5ac8, 0x000, 1,  11},
	{"m68000_dbmi"           , 0xfff8, 0x5bc8, 0x000, 1,  11},
	{"m68000_dbge"           , 0xfff8, 0x5cc8, 0x000, 1,  11},
	{"m68000_dblt"           , 0xfff8, 0x5dc8, 0x000, 1,  11},
	{"m68000_dbgt"           , 0xfff8, 0x5ec8, 0x000, 1,  11},
	{"m68000_dble"           , 0xfff8, 0x5fc8, 0x000, 1,  11},
	{"m68000_divs_d_16"      , 0xf1f8, 0x81c0, 0x000, 1, 158},
	{"m68000_divs_16"        , 0xf1c0, 0x81c0, 0xbff, 1, 158},
	{"m68000_divu_d_16"      , 0xf1f8, 0x80c0, 0x000, 1, 140},
	{"m68000_divu_16"        , 0xf1c0, 0x80c0, 0xbff, 1, 140},
	{"m68020_divl_d_32"      , 0xfff8, 0x4c40, 0x000, 2, 150},
	{"m68020_divl_32"        , 0xffc0, 0x4c40, 0xbff, 2, 150},
	{"m68000_eor_d_8"        , 0xf1f8, 0xb100, 0x000, 0,   4},
	{"m68000_eor_8"          , 0xf1c0, 0xb100, 0xbf8, 0,   8},
	{"m68000_eor_d_16"       , 0xf1f8, 0xb140, 0x000, 1,   4},
	{"m68000_eor_16"         , 0xf1c0, 0xb140, 0xbf8, 1,   8},
	{"m68000_eor_d_32"       , 0xf1f8, 0xb180, 0x000, 2,   8},
	{"m68000_eor_32"         , 0xf1c0, 0xb180, 0xbf8, 2,  12},
	{"m68000_eori_to_ccr"    , 0xffff, 0x0a3c, 0x000, 0,  20},
	{"m68000_eori_to_sr"     , 0xffff, 0x0a7c, 0x000, 1,  20},
	{"m68000_eori_d_8"       , 0xfff8, 0x0a00, 0x000, 0,   8},
	{"m68000_eori_8"         , 0xffc0, 0x0a00, 0xbf8, 0,  12},
	{"m68000_eori_d_16"      , 0xfff8, 0x0a40, 0x000, 1,   8},
	{"m68000_eori_16"        , 0xffc0, 0x0a40, 0xbf8, 1,  12},
	{"m68000_eori_d_32"      , 0xfff8, 0x0a80, 0x000, 2,  16},
	{"m68000_eori_32"        , 0xffc0, 0x0a80, 0xbf8, 2,  20},
	{"m68000_exg_dd"         , 0xf1f8, 0xc140, 0x000, 2,   6},
	{"m68000_exg_aa"         , 0xf1f8, 0xc148, 0x000, 2,   6},
	{"m68000_exg_da"         , 0xf1f8, 0xc188, 0x000, 2,   6},
	{"m68000_ext_16"         , 0xfff8, 0x4880, 0x000, 1,   4},
	{"m68000_ext_32"         , 0xfff8, 0x48c0, 0x000, 2,   4},
	{"m68020_extb"           , 0xfff8, 0x49c0, 0x000, 2,   4},
	{"m68000_illegal"        , 0xffff, 0x4afc, 0x000, 1,   0},
	{"m68000_jmp"            , 0xffc0, 0x4ec0, 0x27b, 2,   0}, /* make table */
	{"m68000_jsr"            , 0xffc0, 0x4e80, 0x27b, 2,   0}, /* make table */
	{"m68000_lea"            , 0xf1c0, 0x41c0, 0x27b, 2,   0}, /* make table */
	{"m68000_link_16_a7"     , 0xffff, 0x4e57, 0x000, 1,  16},
	{"m68000_link_16"        , 0xfff8, 0x4e50, 0x000, 1,  16},
	{"m68020_link_32_a7"     , 0xffff, 0x480f, 0x000, 1,  16},
	{"m68020_link_32"        , 0xfff8, 0x4808, 0x000, 1,  16},
	{"m68000_lsr_s_8"        , 0xf1f8, 0xe008, 0x000, 0,  14}, /* fix in code */
	{"m68000_lsr_s_16"       , 0xf1f8, 0xe048, 0x000, 1,  14},
	{"m68000_lsr_s_32"       , 0xf1f8, 0xe088, 0x000, 2,  16},
	{"m68000_lsr_r_8"        , 0xf1f8, 0xe028, 0x000, 0,  14},
	{"m68000_lsr_r_16"       , 0xf1f8, 0xe068, 0x000, 1,  22},
	{"m68000_lsr_r_32"       , 0xf1f8, 0xe0a8, 0x000, 2,  38},
	{"m68000_lsr_ea"         , 0xffc0, 0xe2c0, 0x3f8, 1,   8},
	{"m68000_lsl_s_8"        , 0xf1f8, 0xe108, 0x000, 0,  14}, /* fix in code */
	{"m68000_lsl_s_16"       , 0xf1f8, 0xe148, 0x000, 1,  14},
	{"m68000_lsl_s_32"       , 0xf1f8, 0xe188, 0x000, 2,  16},
	{"m68000_lsl_r_8"        , 0xf1f8, 0xe128, 0x000, 0,  14},
	{"m68000_lsl_r_16"       , 0xf1f8, 0xe168, 0x000, 1,  22},
	{"m68000_lsl_r_32"       , 0xf1f8, 0xe1a8, 0x000, 2,  38},
	{"m68000_lsl_ea"         , 0xffc0, 0xe3c0, 0x3f8, 1,   8},
	{"m68000_move_dd_d_8"    , 0xf1f8, 0x1000, 0x000, 0,   4},
	{"m68000_move_dd_8"      , 0xf1c0, 0x1000, 0xbff, 0,   4},
	{"m68000_move_ai_d_8"    , 0xf1f8, 0x1080, 0x000, 0,   8},
	{"m68000_move_ai_8"      , 0xf1c0, 0x1080, 0xbff, 0,   8},
	{"m68000_move_pi_d_8"    , 0xf1f8, 0x10c0, 0x000, 0,   8},
	{"m68000_move_pi_8"      , 0xf1c0, 0x10c0, 0xbff, 0,   8},
	{"m68000_move_pi7_d_8"   , 0xfff8, 0x1ec0, 0x000, 0,   8},
	{"m68000_move_pi7_8"     , 0xffc0, 0x1ec0, 0xbff, 0,   8},
	{"m68000_move_pd_d_8"    , 0xf1f8, 0x1100, 0x000, 0,   8},
	{"m68000_move_pd_8"      , 0xf1c0, 0x1100, 0xbff, 0,   8},
	{"m68000_move_pd7_d_8"   , 0xfff8, 0x1f00, 0x000, 0,   8},
	{"m68000_move_pd7_8"     , 0xffc0, 0x1f00, 0xbff, 0,   8},
	{"m68000_move_di_d_8"    , 0xf1f8, 0x1140, 0x000, 0,  12},
	{"m68000_move_di_8"      , 0xf1c0, 0x1140, 0xbff, 0,  12},
	{"m68000_move_ix_d_8"    , 0xf1f8, 0x1180, 0x000, 0,  14},
	{"m68000_move_ix_8"      , 0xf1c0, 0x1180, 0xbff, 0,  14},
	{"m68000_move_aw_d_8"    , 0xfff8, 0x11c0, 0x000, 0,  12},
	{"m68000_move_aw_8"      , 0xffc0, 0x11c0, 0xbff, 0,  12},
	{"m68000_move_al_d_8"    , 0xfff8, 0x13c0, 0x000, 0,  16},
	{"m68000_move_al_8"      , 0xffc0, 0x13c0, 0xbff, 0,  16},
	{"m68000_move_dd_d_16"   , 0xf1f8, 0x3000, 0x000, 1,   4},
	{"m68000_move_dd_a_16"   , 0xf1f8, 0x3008, 0x000, 1,   4},
	{"m68000_move_dd_16"     , 0xf1c0, 0x3000, 0xfff, 1,   4},
	{"m68000_move_ai_d_16"   , 0xf1f8, 0x3080, 0x000, 1,   8},
	{"m68000_move_ai_a_16"   , 0xf1f8, 0x3088, 0x000, 1,   8},
	{"m68000_move_ai_16"     , 0xf1c0, 0x3080, 0xfff, 1,   8},
	{"m68000_move_pi_d_16"   , 0xf1f8, 0x30c0, 0x000, 1,   8},
	{"m68000_move_pi_a_16"   , 0xf1f8, 0x30c8, 0x000, 1,   8},
	{"m68000_move_pi_16"     , 0xf1c0, 0x30c0, 0xfff, 1,   8},
	{"m68000_move_pd_d_16"   , 0xf1f8, 0x3100, 0x000, 1,   8},
	{"m68000_move_pd_a_16"   , 0xf1f8, 0x3108, 0x000, 1,   8},
	{"m68000_move_pd_16"     , 0xf1c0, 0x3100, 0xfff, 1,   8},
	{"m68000_move_di_d_16"   , 0xf1f8, 0x3140, 0x000, 1,  12},
	{"m68000_move_di_a_16"   , 0xf1f8, 0x3148, 0x000, 1,  12},
	{"m68000_move_di_16"     , 0xf1c0, 0x3140, 0xfff, 1,  12},
	{"m68000_move_ix_d_16"   , 0xf1f8, 0x3180, 0x000, 1,  14},
	{"m68000_move_ix_a_16"   , 0xf1f8, 0x3188, 0x000, 1,  14},
	{"m68000_move_ix_16"     , 0xf1c0, 0x3180, 0xfff, 1,  14},
	{"m68000_move_aw_d_16"   , 0xfff8, 0x31c0, 0x000, 1,  12},
	{"m68000_move_aw_a_16"   , 0xfff8, 0x31c8, 0x000, 1,  12},
	{"m68000_move_aw_16"     , 0xffc0, 0x31c0, 0xfff, 1,  12},
	{"m68000_move_al_d_16"   , 0xfff8, 0x33c0, 0x000, 1,  16},
	{"m68000_move_al_a_16"   , 0xfff8, 0x33c8, 0x000, 1,  16},
	{"m68000_move_al_16"     , 0xffc0, 0x33c0, 0xfff, 1,  16},
	{"m68000_move_dd_d_32"   , 0xf1f8, 0x2000, 0x000, 2,   4},
	{"m68000_move_dd_a_32"   , 0xf1f8, 0x2008, 0x000, 2,   4},
	{"m68000_move_dd_32"     , 0xf1c0, 0x2000, 0xfff, 2,   4},
	{"m68000_move_ai_d_32"   , 0xf1f8, 0x2080, 0x000, 2,  12},
	{"m68000_move_ai_a_32"   , 0xf1f8, 0x2088, 0x000, 2,  12},
	{"m68000_move_ai_32"     , 0xf1c0, 0x2080, 0xfff, 2,  12},
	{"m68000_move_pi_d_32"   , 0xf1f8, 0x20c0, 0x000, 2,  12},
	{"m68000_move_pi_a_32"   , 0xf1f8, 0x20c8, 0x000, 2,  12},
	{"m68000_move_pi_32"     , 0xf1c0, 0x20c0, 0xfff, 2,  12},
	{"m68000_move_pd_d_32"   , 0xf1f8, 0x2100, 0x000, 2,  12},
	{"m68000_move_pd_a_32"   , 0xf1f8, 0x2108, 0x000, 2,  12},
	{"m68000_move_pd_32"     , 0xf1c0, 0x2100, 0xfff, 2,  12},
	{"m68000_move_di_d_32"   , 0xf1f8, 0x2140, 0x000, 2,  16},
	{"m68000_move_di_a_32"   , 0xf1f8, 0x2148, 0x000, 2,  16},
	{"m68000_move_di_32"     , 0xf1c0, 0x2140, 0xfff, 2,  16},
	{"m68000_move_ix_d_32"   , 0xf1f8, 0x2180, 0x000, 2,  18},
	{"m68000_move_ix_a_32"   , 0xf1f8, 0x2188, 0x000, 2,  18},
	{"m68000_move_ix_32"     , 0xf1c0, 0x2180, 0xfff, 2,  18},
	{"m68000_move_aw_d_32"   , 0xfff8, 0x21c0, 0x000, 2,  16},
	{"m68000_move_aw_a_32"   , 0xfff8, 0x21c8, 0x000, 2,  16},
	{"m68000_move_aw_32"     , 0xffc0, 0x21c0, 0xfff, 2,  16},
	{"m68000_move_al_d_32"   , 0xfff8, 0x23c0, 0x000, 2,  20},
	{"m68000_move_al_a_32"   , 0xfff8, 0x23c8, 0x000, 2,  20},
	{"m68000_move_al_32"     , 0xffc0, 0x23c0, 0xfff, 2,  20},
	{"m68000_movea_d_16"     , 0xf1f8, 0x3040, 0x000, 1,   4},
	{"m68000_movea_a_16"     , 0xf1f8, 0x3048, 0x000, 1,   4},
	{"m68000_movea_16"       , 0xf1c0, 0x3040, 0xfff, 1,   4},
	{"m68000_movea_d_32"     , 0xf1f8, 0x2040, 0x000, 2,   4},
	{"m68000_movea_a_32"     , 0xf1f8, 0x2048, 0x000, 2,   4},
	{"m68000_movea_32"       , 0xf1c0, 0x2040, 0xfff, 2,   4},
	{"m68010_move_fr_ccr_d"  , 0xfff8, 0x42c0, 0x000, 1,   4},
	{"m68010_move_fr_ccr"    , 0xffc0, 0x42c0, 0xbf8, 1,   8},
	{"m68000_move_to_ccr_d"  , 0xfff8, 0x44c0, 0x000, 1,  12},
	{"m68000_move_to_ccr"    , 0xffc0, 0x44c0, 0xbff, 1,  12},
	{"m68000_move_fr_sr_d"   , 0xfff8, 0x40c0, 0x000, 1,   6},
	{"m68000_move_fr_sr"     , 0xffc0, 0x40c0, 0xbf8, 1,   8},
	{"m68000_move_to_sr_d"   , 0xfff8, 0x46c0, 0x000, 1,  12},
	{"m68000_move_to_sr"     , 0xffc0, 0x46c0, 0xbff, 1,  12},
	{"m68000_move_fr_usp"    , 0xfff8, 0x4e68, 0x000, 2,   4},
	{"m68000_move_to_usp"    , 0xfff8, 0x4e60, 0x000, 2,   4},
	{"m68010_movec_cr"       , 0xffff, 0x4e7a, 0x000, 0,  12},
	{"m68010_movec_rc"       , 0xffff, 0x4e7b, 0x000, 0,  10},
	{"m68000_movem_pd_16"    , 0xfff8, 0x48a0, 0x000, 1,  40}, /* fix in code */
	{"m68000_movem_pd_32"    , 0xfff8, 0x48e0, 0x000, 1,  72},	/* ASG: size really 2, but cycles like 1 */
	{"m68000_movem_pi_16"    , 0xfff8, 0x4c98, 0x000, 1,  44},
	{"m68000_movem_pi_32"    , 0xfff8, 0x4cd8, 0x000, 1,  76},	/* ASG: size really 2, but cycles like 1 */
	{"m68000_movem_re_16"    , 0xffc0, 0x4880, 0x278, 1,  36}, /* HJB was 0x2f8 */
	{"m68000_movem_re_32"    , 0xffc0, 0x48c0, 0x278, 1,  68}, /* HJB was 0x2f8 */	/* ASG: size really 2, but cycles like 1 */
	{"m68000_movem_er_16"    , 0xffc0, 0x4c80, 0x27b, 1,  40}, /* JCB was 0x37b */
	{"m68000_movem_er_32"    , 0xffc0, 0x4cc0, 0x27b, 1,  72}, /* JCB was 0x37b */	/* ASG: size really 2, but cycles like 1 */
	{"m68000_movep_er_16"    , 0xf1f8, 0x0108, 0x000, 1,  16},
	{"m68000_movep_er_32"    , 0xf1f8, 0x0148, 0x000, 2,  24},
	{"m68000_movep_re_16"    , 0xf1f8, 0x0188, 0x000, 1,  16},
	{"m68000_movep_re_32"    , 0xf1f8, 0x01c8, 0x000, 2,  24},
	{"m68010_moves_8"        , 0xffc0, 0x0e00, 0x3f8, 0,   0}, /* make table */
	{"m68010_moves_16"       , 0xffc0, 0x0e40, 0x3f8, 1,   0}, /* make table */
	{"m68010_moves_32"       , 0xffc0, 0x0e80, 0x3f8, 2,   0}, /* make table */
	{"m68000_moveq"          , 0xf100, 0x7000, 0x000, 2,   4},
	{"m68000_muls_d_16"      , 0xf1f8, 0xc1c0, 0x000, 1,  54},
	{"m68000_muls_16"        , 0xf1c0, 0xc1c0, 0xbff, 1,  54},
	{"m68000_mulu_d_16"      , 0xf1f8, 0xc0c0, 0x000, 1,  54},
	{"m68000_mulu_16"        , 0xf1c0, 0xc0c0, 0xbff, 1,  54},
	{"m68020_mull_d_32"      , 0xfff8, 0x4c00, 0x000, 2,  54},
	{"m68020_mull_32"        , 0xffc0, 0x4c00, 0xbff, 2,  54},
	{"m68000_nbcd_d"         , 0xfff8, 0x4800, 0x000, 0,   6},
	{"m68000_nbcd"           , 0xffc0, 0x4800, 0xbf8, 0,   8},
	{"m68000_neg_d_8"        , 0xfff8, 0x4400, 0x000, 0,   4},
	{"m68000_neg_8"          , 0xffc0, 0x4400, 0xbf8, 0,   8},
	{"m68000_neg_d_16"       , 0xfff8, 0x4440, 0x000, 1,   4},
	{"m68000_neg_16"         , 0xffc0, 0x4440, 0xbf8, 1,   8},
	{"m68000_neg_d_32"       , 0xfff8, 0x4480, 0x000, 2,   6},
	{"m68000_neg_32"         , 0xffc0, 0x4480, 0xbf8, 2,  12},
	{"m68000_negx_d_8"       , 0xfff8, 0x4000, 0x000, 0,   4},
	{"m68000_negx_8"         , 0xffc0, 0x4000, 0xbf8, 0,   8},
	{"m68000_negx_d_16"      , 0xfff8, 0x4040, 0x000, 1,   4},
	{"m68000_negx_16"        , 0xffc0, 0x4040, 0xbf8, 1,   8},
	{"m68000_negx_d_32"      , 0xfff8, 0x4080, 0x000, 2,   6},
	{"m68000_negx_32"        , 0xffc0, 0x4080, 0xbf8, 2,  12},
	{"m68000_nop"            , 0xffff, 0x4e71, 0x000, 1,   4},
	{"m68000_not_d_8"        , 0xfff8, 0x4600, 0x000, 0,   4},
	{"m68000_not_8"          , 0xffc0, 0x4600, 0xbf8, 0,   8},
	{"m68000_not_d_16"       , 0xfff8, 0x4640, 0x000, 1,   4},
	{"m68000_not_16"         , 0xffc0, 0x4640, 0xbf8, 1,   8},
	{"m68000_not_d_32"       , 0xfff8, 0x4680, 0x000, 2,   6},
	{"m68000_not_32"         , 0xffc0, 0x4680, 0xbf8, 2,  12},
	{"m68000_or_er_d_8"      , 0xf1f8, 0x8000, 0x000, 0,   4},
	{"m68000_or_er_8"        , 0xf1c0, 0x8000, 0xbff, 0,   4},
	{"m68000_or_er_d_16"     , 0xf1f8, 0x8040, 0x000, 1,   4},
	{"m68000_or_er_16"       , 0xf1c0, 0x8040, 0xbff, 1,   4},
	{"m68000_or_er_d_32"     , 0xf1f8, 0x8080, 0x000, 2,   8},
	{"m68000_or_er_32"       , 0xf1c0, 0x8080, 0xbff, 2,   6}, /* 8 for imm */
	{"m68000_or_re_8"        , 0xf1c0, 0x8100, 0x3f8, 0,   8},
	{"m68000_or_re_16"       , 0xf1c0, 0x8140, 0x3f8, 1,   8},
	{"m68000_or_re_32"       , 0xf1c0, 0x8180, 0x3f8, 2,  12},
	{"m68000_ori_to_ccr"     , 0xffff, 0x003c, 0x000, 0,  20},
	{"m68000_ori_to_sr"      , 0xffff, 0x007c, 0x000, 1,  20},
	{"m68000_ori_d_8"        , 0xfff8, 0x0000, 0x000, 0,   8},
	{"m68000_ori_8"          , 0xffc0, 0x0000, 0xbf8, 0,  12},
	{"m68000_ori_d_16"       , 0xfff8, 0x0040, 0x000, 1,   8},
	{"m68000_ori_16"         , 0xffc0, 0x0040, 0xbf8, 1,  12},
	{"m68000_ori_d_32"       , 0xfff8, 0x0080, 0x000, 2,  16},
	{"m68000_ori_32"         , 0xffc0, 0x0080, 0xbf8, 2,  20},
	{"m68020_pack_rr"        , 0xf1f8, 0x8140, 0x000, 0,   8},
	{"m68020_pack_mm_ax7"    , 0xf1ff, 0x814f, 0x000, 0,   8},
	{"m68020_pack_mm_ay7"    , 0xfff8, 0x8f48, 0x000, 0,   8},
	{"m68020_pack_mm_axy7"   , 0xffff, 0x8f4f, 0x000, 0,   8},
	{"m68020_pack_mm"        , 0xf1f8, 0x8148, 0x000, 0,   8},
	{"m68000_pea"            , 0xffc0, 0x4840, 0x27b, 2,   0}, /* make table */
	{"m68000_rst"            , 0xffff, 0x4e70, 0x000, 1, 132},
	{"m68000_ror_s_8"        , 0xf1f8, 0xe018, 0x000, 0,  14}, /* fix in code */
	{"m68000_ror_s_16"       , 0xf1f8, 0xe058, 0x000, 1,  14},
	{"m68000_ror_s_32"       , 0xf1f8, 0xe098, 0x000, 2,  16},
	{"m68000_ror_r_8"        , 0xf1f8, 0xe038, 0x000, 0,  14},
	{"m68000_ror_r_16"       , 0xf1f8, 0xe078, 0x000, 1,  22},
	{"m68000_ror_r_32"       , 0xf1f8, 0xe0b8, 0x000, 2,  38},
	{"m68000_ror_ea"         , 0xffc0, 0xe6c0, 0x3f8, 1,   8},
	{"m68000_rol_s_8"        , 0xf1f8, 0xe118, 0x000, 0,  14}, /* fix in code */
	{"m68000_rol_s_16"       , 0xf1f8, 0xe158, 0x000, 1,  14},
	{"m68000_rol_s_32"       , 0xf1f8, 0xe198, 0x000, 2,  16},
	{"m68000_rol_r_8"        , 0xf1f8, 0xe138, 0x000, 0,  14},
	{"m68000_rol_r_16"       , 0xf1f8, 0xe178, 0x000, 1,  22},
	{"m68000_rol_r_32"       , 0xf1f8, 0xe1b8, 0x000, 2,  38},
	{"m68000_rol_ea"         , 0xffc0, 0xe7c0, 0x3f8, 1,   8},
	{"m68000_roxr_s_8"       , 0xf1f8, 0xe010, 0x000, 0,  14}, /* fix in code */
	{"m68000_roxr_s_16"      , 0xf1f8, 0xe050, 0x000, 1,  14},
	{"m68000_roxr_s_32"      , 0xf1f8, 0xe090, 0x000, 2,  16},
	{"m68000_roxr_r_8"       , 0xf1f8, 0xe030, 0x000, 0,  14},
	{"m68000_roxr_r_16"      , 0xf1f8, 0xe070, 0x000, 1,  22},
	{"m68000_roxr_r_32"      , 0xf1f8, 0xe0b0, 0x000, 2,  38},
	{"m68000_roxr_ea"        , 0xffc0, 0xe4c0, 0x3f8, 1,   8},
	{"m68000_roxl_s_8"       , 0xf1f8, 0xe110, 0x000, 0,  14}, /* fix in code */
	{"m68000_roxl_s_16"      , 0xf1f8, 0xe150, 0x000, 1,  14},
	{"m68000_roxl_s_32"      , 0xf1f8, 0xe190, 0x000, 2,  16},
	{"m68000_roxl_r_8"       , 0xf1f8, 0xe130, 0x000, 0,  14},
	{"m68000_roxl_r_16"      , 0xf1f8, 0xe170, 0x000, 1,  22},
	{"m68000_roxl_r_32"      , 0xf1f8, 0xe1b0, 0x000, 2,  38},
	{"m68000_roxl_ea"        , 0xffc0, 0xe5c0, 0x3f8, 1,   8},
	{"m68010_rtd"            , 0xffff, 0x4e74, 0x000, 2,  16},
	{"m68000_rte"            , 0xffff, 0x4e73, 0x000, 2,  20},
	{"m68020_rtm"            , 0xfff0, 0x06c0, 0x000, 2,  20},
	{"m68000_rtr"            , 0xffff, 0x4e77, 0x000, 2,  20},
	{"m68000_rts"            , 0xffff, 0x4e75, 0x000, 2,  16},
	{"m68000_sbcd_rr"        , 0xf1f8, 0x8100, 0x000, 0,   6},
	{"m68000_sbcd_mm_ax7"    , 0xfff8, 0x8f08, 0x000, 0,  18},
	{"m68000_sbcd_mm_ay7"    , 0xf1ff, 0x810f, 0x000, 0,  18},
	{"m68000_sbcd_mm_axy7"   , 0xffff, 0x8f0f, 0x000, 0,  18},
	{"m68000_sbcd_mm"        , 0xf1f8, 0x8108, 0x000, 0,  18},
	{"m68000_st_d"           , 0xfff8, 0x50c0, 0x000, 0,   6}, /* fix in code */
	{"m68000_st"             , 0xffc0, 0x50c0, 0xbf8, 0,   8},
	{"m68000_sf_d"           , 0xfff8, 0x51c0, 0x000, 0,   4},
	{"m68000_sf"             , 0xffc0, 0x51c0, 0xbf8, 0,   8},
	{"m68000_shi_d"          , 0xfff8, 0x52c0, 0x000, 0,   5},
	{"m68000_shi"            , 0xffc0, 0x52c0, 0xbf8, 0,   8},
	{"m68000_sls_d"          , 0xfff8, 0x53c0, 0x000, 0,   5},
	{"m68000_sls"            , 0xffc0, 0x53c0, 0xbf8, 0,   8},
	{"m68000_scc_d"          , 0xfff8, 0x54c0, 0x000, 0,   5},
	{"m68000_scc"            , 0xffc0, 0x54c0, 0xbf8, 0,   8},
	{"m68000_scs_d"          , 0xfff8, 0x55c0, 0x000, 0,   5},
	{"m68000_scs"            , 0xffc0, 0x55c0, 0xbf8, 0,   8},
	{"m68000_sne_d"          , 0xfff8, 0x56c0, 0x000, 0,   5},
	{"m68000_sne"            , 0xffc0, 0x56c0, 0xbf8, 0,   8},
	{"m68000_seq_d"          , 0xfff8, 0x57c0, 0x000, 0,   5},
	{"m68000_seq"            , 0xffc0, 0x57c0, 0xbf8, 0,   8},
	{"m68000_svc_d"          , 0xfff8, 0x58c0, 0x000, 0,   5},
	{"m68000_svc"            , 0xffc0, 0x58c0, 0xbf8, 0,   8},
	{"m68000_svs_d"          , 0xfff8, 0x59c0, 0x000, 0,   5},
	{"m68000_svs"            , 0xffc0, 0x59c0, 0xbf8, 0,   8},
	{"m68000_spl_d"          , 0xfff8, 0x5ac0, 0x000, 0,   5},
	{"m68000_spl"            , 0xffc0, 0x5ac0, 0xbf8, 0,   8},
	{"m68000_smi_d"          , 0xfff8, 0x5bc0, 0x000, 0,   5},
	{"m68000_smi"            , 0xffc0, 0x5bc0, 0xbf8, 0,   8},
	{"m68000_sge_d"          , 0xfff8, 0x5cc0, 0x000, 0,   5},
	{"m68000_sge"            , 0xffc0, 0x5cc0, 0xbf8, 0,   8},
	{"m68000_slt_d"          , 0xfff8, 0x5dc0, 0x000, 0,   5},
	{"m68000_slt"            , 0xffc0, 0x5dc0, 0xbf8, 0,   8},
	{"m68000_sgt_d"          , 0xfff8, 0x5ec0, 0x000, 0,   5},
	{"m68000_sgt"            , 0xffc0, 0x5ec0, 0xbf8, 0,   8},
	{"m68000_sle_d"          , 0xfff8, 0x5fc0, 0x000, 0,   5},
	{"m68000_sle"            , 0xffc0, 0x5fc0, 0xbf8, 0,   8},
	{"m68000_stop"           , 0xffff, 0x4e72, 0x000, 1,   4},
	{"m68000_sub_er_d_8"     , 0xf1f8, 0x9000, 0x000, 0,   4},
	{"m68000_sub_er_8"       , 0xf1c0, 0x9000, 0xbff, 0,   4},
	{"m68000_sub_er_d_16"    , 0xf1f8, 0x9040, 0x000, 1,   4},
	{"m68000_sub_er_a_16"    , 0xf1f8, 0x9048, 0x000, 1,   4},
	{"m68000_sub_er_16"      , 0xf1c0, 0x9040, 0xfff, 1,   4},
	{"m68000_sub_er_d_32"    , 0xf1f8, 0x9080, 0x000, 2,   8},
	{"m68000_sub_er_a_32"    , 0xf1f8, 0x9088, 0x000, 2,   8},
	{"m68000_sub_er_32"      , 0xf1c0, 0x9080, 0xfff, 2,   6}, /* 8 for imm */
	{"m68000_sub_re_8"       , 0xf1c0, 0x9100, 0x3f8, 0,   8},
	{"m68000_sub_re_16"      , 0xf1c0, 0x9140, 0x3f8, 1,   8},
	{"m68000_sub_re_32"      , 0xf1c0, 0x9180, 0x3f8, 2,  12},
	{"m68000_suba_d_16"      , 0xf1f8, 0x90c0, 0x000, 1,   8},
	{"m68000_suba_a_16"      , 0xf1f8, 0x90c8, 0x000, 1,   8},
	{"m68000_suba_16"        , 0xf1c0, 0x90c0, 0xfff, 1,   8},
	{"m68000_suba_d_32"      , 0xf1f8, 0x91c0, 0x000, 2,   8},
	{"m68000_suba_a_32"      , 0xf1f8, 0x91c8, 0x000, 2,   8},
	{"m68000_suba_32"        , 0xf1c0, 0x91c0, 0xfff, 2,   6}, /* 8 for imm */
	{"m68000_subi_d_8"       , 0xfff8, 0x0400, 0x000, 0,   8},
	{"m68000_subi_8"         , 0xffc0, 0x0400, 0xbf8, 0,  12},
	{"m68000_subi_d_16"      , 0xfff8, 0x0440, 0x000, 1,   8},
	{"m68000_subi_16"        , 0xffc0, 0x0440, 0xbf8, 1,  12},
	{"m68000_subi_d_32"      , 0xfff8, 0x0480, 0x000, 2,  16},
	{"m68000_subi_32"        , 0xffc0, 0x0480, 0xbf8, 2,  20},
	{"m68000_subq_d_8"       , 0xf1f8, 0x5100, 0x000, 0,   4},
	{"m68000_subq_8"         , 0xf1c0, 0x5100, 0xbf8, 0,   8},
	{"m68000_subq_d_16"      , 0xf1f8, 0x5140, 0x000, 1,   4},
	{"m68000_subq_a_16"      , 0xf1f8, 0x5148, 0x000, 1,   8},
	{"m68000_subq_16"        , 0xf1c0, 0x5140, 0xff8, 1,   8},
	{"m68000_subq_d_32"      , 0xf1f8, 0x5180, 0x000, 2,   8},
	{"m68000_subq_a_32"      , 0xf1f8, 0x5188, 0x000, 2,   8},
	{"m68000_subq_32"        , 0xf1c0, 0x5180, 0xff8, 2,  12},
	{"m68000_subx_rr_8"      , 0xf1f8, 0x9100, 0x000, 0,   4},
	{"m68000_subx_rr_16"     , 0xf1f8, 0x9140, 0x000, 1,   4},
	{"m68000_subx_rr_32"     , 0xf1f8, 0x9180, 0x000, 2,   8},
	{"m68000_subx_mm_8_ax7"  , 0xfff8, 0x9f08, 0x000, 0,  18},
	{"m68000_subx_mm_8_ay7"  , 0xf1ff, 0x910f, 0x000, 0,  18},
	{"m68000_subx_mm_8_axy7" , 0xffff, 0x9f0f, 0x000, 0,  18},
	{"m68000_subx_mm_8"      , 0xf1f8, 0x9108, 0x000, 0,  18},
	{"m68000_subx_mm_16"     , 0xf1f8, 0x9148, 0x000, 1,  18},
	{"m68000_subx_mm_32"     , 0xf1f8, 0x9188, 0x000, 2,  30},
	{"m68000_swap"           , 0xfff8, 0x4840, 0x000, 1,   4},
	{"m68000_tas_d"          , 0xfff8, 0x4ac0, 0x000, 0,   4},
	{"m68000_tas"            , 0xffc0, 0x4ac0, 0xbf8, 0,  14},
	{"m68000_trap"           , 0xfff0, 0x4e40, 0x000, 1,   4},
	{"m68020_trapt_0"        , 0xffff, 0x50fc, 0x000, 0,   4},
	{"m68020_trapt_16"       , 0xffff, 0x50fa, 0x000, 0,   4},
	{"m68020_trapt_32"       , 0xffff, 0x50fb, 0x000, 0,   4},
	{"m68020_trapf_0"        , 0xffff, 0x51fc, 0x000, 0,   4},
	{"m68020_trapf_16"       , 0xffff, 0x51fa, 0x000, 0,   4},
	{"m68020_trapf_32"       , 0xffff, 0x51fb, 0x000, 0,   4},
	{"m68020_traphi_0"       , 0xffff, 0x52fc, 0x000, 0,   4},
	{"m68020_traphi_16"      , 0xffff, 0x52fa, 0x000, 0,   4},
	{"m68020_traphi_32"      , 0xffff, 0x52fb, 0x000, 0,   4},
	{"m68020_trapls_0"       , 0xffff, 0x53fc, 0x000, 0,   4},
	{"m68020_trapls_16"      , 0xffff, 0x53fa, 0x000, 0,   4},
	{"m68020_trapls_32"      , 0xffff, 0x53fb, 0x000, 0,   4},
	{"m68020_trapcc_0"       , 0xffff, 0x54fc, 0x000, 0,   4},
	{"m68020_trapcc_16"      , 0xffff, 0x54fa, 0x000, 0,   4},
	{"m68020_trapcc_32"      , 0xffff, 0x54fb, 0x000, 0,   4},
	{"m68020_trapcs_0"       , 0xffff, 0x55fc, 0x000, 0,   4},
	{"m68020_trapcs_16"      , 0xffff, 0x55fa, 0x000, 0,   4},
	{"m68020_trapcs_32"      , 0xffff, 0x55fb, 0x000, 0,   4},
	{"m68020_trapne_0"       , 0xffff, 0x56fc, 0x000, 0,   4},
	{"m68020_trapne_16"      , 0xffff, 0x56fa, 0x000, 0,   4},
	{"m68020_trapne_32"      , 0xffff, 0x56fb, 0x000, 0,   4},
	{"m68020_trapeq_0"       , 0xffff, 0x57fc, 0x000, 0,   4},
	{"m68020_trapeq_16"      , 0xffff, 0x57fa, 0x000, 0,   4},
	{"m68020_trapeq_32"      , 0xffff, 0x57fb, 0x000, 0,   4},
	{"m68020_trapvc_0"       , 0xffff, 0x58fc, 0x000, 0,   4},
	{"m68020_trapvc_16"      , 0xffff, 0x58fa, 0x000, 0,   4},
	{"m68020_trapvc_32"      , 0xffff, 0x58fb, 0x000, 0,   4},
	{"m68020_trapvs_0"       , 0xffff, 0x59fc, 0x000, 0,   4},
	{"m68020_trapvs_16"      , 0xffff, 0x59fa, 0x000, 0,   4},
	{"m68020_trapvs_32"      , 0xffff, 0x59fb, 0x000, 0,   4},
	{"m68020_trappl_0"       , 0xffff, 0x5afc, 0x000, 0,   4},
	{"m68020_trappl_16"      , 0xffff, 0x5afa, 0x000, 0,   4},
	{"m68020_trappl_32"      , 0xffff, 0x5afb, 0x000, 0,   4},
	{"m68020_trapmi_0"       , 0xffff, 0x5bfc, 0x000, 0,   4},
	{"m68020_trapmi_16"      , 0xffff, 0x5bfa, 0x000, 0,   4},
	{"m68020_trapmi_32"      , 0xffff, 0x5bfb, 0x000, 0,   4},
	{"m68020_trapge_0"       , 0xffff, 0x5cfc, 0x000, 0,   4},
	{"m68020_trapge_16"      , 0xffff, 0x5cfa, 0x000, 0,   4},
	{"m68020_trapge_32"      , 0xffff, 0x5cfb, 0x000, 0,   4},
	{"m68020_traplt_0"       , 0xffff, 0x5dfc, 0x000, 0,   4},
	{"m68020_traplt_16"      , 0xffff, 0x5dfa, 0x000, 0,   4},
	{"m68020_traplt_32"      , 0xffff, 0x5dfb, 0x000, 0,   4},
	{"m68020_trapgt_0"       , 0xffff, 0x5efc, 0x000, 0,   4},
	{"m68020_trapgt_16"      , 0xffff, 0x5efa, 0x000, 0,   4},
	{"m68020_trapgt_32"      , 0xffff, 0x5efb, 0x000, 0,   4},
	{"m68020_traple_0"       , 0xffff, 0x5ffc, 0x000, 0,   4},
	{"m68020_traple_16"      , 0xffff, 0x5ffa, 0x000, 0,   4},
	{"m68020_traple_32"      , 0xffff, 0x5ffb, 0x000, 0,   4},
	{"m68000_trapv"          , 0xffff, 0x4e76, 0x000, 1,   4},
	{"m68000_tst_d_8"        , 0xfff8, 0x4a00, 0x000, 0,   4},
	{"m68000_tst_8"          , 0xffc0, 0x4a00, 0xbf8, 0,   4},
	{"m68020_tst_pcdi_8"     , 0xffff, 0x4a3a, 0x000, 0,   4},
	{"m68020_tst_pcix_8"     , 0xffff, 0x4a3b, 0x000, 0,   4},
	{"m68020_tst_imm_8"      , 0xffff, 0x4a3c, 0x000, 0,   4},
	{"m68000_tst_d_16"       , 0xfff8, 0x4a40, 0x000, 1,   4},
	{"m68020_tst_a_16"       , 0xfff8, 0x4a48, 0x000, 1,   4},
	{"m68000_tst_16"         , 0xffc0, 0x4a40, 0xbf8, 1,   4},
	{"m68020_tst_pcdi_16"    , 0xffff, 0x4a7a, 0x000, 1,   4},
	{"m68020_tst_pcix_16"    , 0xffff, 0x4a7b, 0x000, 1,   4},
	{"m68020_tst_imm_16"     , 0xffff, 0x4a7c, 0x000, 1,   4},
	{"m68000_tst_d_32"       , 0xfff8, 0x4a80, 0x000, 2,   4},
	{"m68020_tst_a_32"       , 0xfff8, 0x4a88, 0x000, 2,   4},
	{"m68000_tst_32"         , 0xffc0, 0x4a80, 0xbf8, 2,   4},
	{"m68020_tst_pcdi_32"    , 0xffff, 0x4aba, 0x000, 2,   4},
	{"m68020_tst_pcix_32"    , 0xffff, 0x4abb, 0x000, 2,   4},
	{"m68020_tst_imm_32"     , 0xffff, 0x4abc, 0x000, 2,   4},
	{"m68000_unlk_a7"        , 0xffff, 0x4e5f, 0x000, 1,  12},
	{"m68000_unlk"           , 0xfff8, 0x4e58, 0x000, 1,  12},
	{"m68020_unpk_rr"        , 0xf1f8, 0x8180, 0x000, 0,   8},
	{"m68020_unpk_mm_ax7"    , 0xf1ff, 0x818f, 0x000, 0,   8},
	{"m68020_unpk_mm_ay7"    , 0xfff8, 0x8f88, 0x000, 0,   8},
	{"m68020_unpk_mm_axy7"   , 0xffff, 0x8f8f, 0x000, 0,   8},
	{"m68020_unpk_mm"        , 0xf1f8, 0x8188, 0x000, 0,   8},
	{0, 0, 0, 0, 0}
};

/* Convert a hex value written in ASCII */
int atoh(char* buff)
{
	int accum = 0;

	for(;;buff++)
	{
		if(*buff >= '0' && *buff <= '9')
		{
			accum <<= 4;
			accum += *buff - '0';
		}
		else if(*buff >= 'a' && *buff <= 'f')
		{
			accum <<= 4;
			accum += *buff - 'a' + 10;
		}
		else break;
	}
	return accum;
}


/* Safe version of fgets that works on Macs and PCs */
char *safe_fgets(char * s, int n, FILE * file)
{
	char *result = fgets(s, n, file);
	if (s[0] == '\r')
		memcpy(s, s + 1, n - 1);
	return result;
}


char* modify_ea_string(char* old_ea_string, char* insert_string)
{
	static char buff[300];
	static char* blank = "";
	char end_bit[200];
	char* ea_start;
	char* ea_end;

	if(strstr(old_ea_string, "uint ea") != NULL)
		return blank;
	strcpy(buff, old_ea_string);
	ea_start = strstr(buff, "m68ki_get_ea_");
	ea_end = *(ea_start+13) == '8' ? ea_start + 16 : ea_start + 17;
	strcpy(end_bit, ea_end);
	sprintf(ea_start, "%s%s", insert_string, end_bit);

	return buff;
}


char* modify_imm_string(char* old_ea_string, char* insert_string)
{
	static char buff[300];
	static char* blank = "";
	char end_bit[200];
	char* real_start;
	char* ea_start;
	char* ea_end;

	if(strstr(old_ea_string, "uint ea") != NULL)
		return blank;
	strcpy(buff, old_ea_string);
	real_start = strstr(buff, "m68ki_read_");
	ea_start = strstr(buff, "m68ki_get_ea_");
	ea_end = *(ea_start+13) == '8' ? ea_start + 17 : ea_start + 18;
	strcpy(end_bit, ea_end);
	sprintf(real_start, "%s%s", insert_string, end_bit);

    return buff;
}


char* replace_clk_string(char* old_clk_string, char* replace_string, int add_value)
{
	static char buff[300];
	char spaces[200];
	char guts[30];
	char* guts_start;
	char* guts_end;
	char* ptr;
	int i;

	strcpy(spaces, old_clk_string);
	for( i = 0; i < 199; i++ )
		if( !isspace(spaces[i]) )
            break;
	spaces[i] = 0;

	guts_start = strstr(old_clk_string, "(") + 1;
	strcpy(guts, guts_start);
	guts_end = strstr(guts, ")");
	while((ptr = strstr(guts_end+1, ")")) != NULL)
		guts_end = ptr;
	*guts_end = 0;
	sprintf(buff, "%s%s(%s+%d);\n", spaces, replace_string, guts, add_value);
	return buff;
}


int get_clk_add(int func_num, int ea_mode)
{
	if(strcmp(g_func_table[func_num].name, "m68000_jmp") == 0)
		return g_jmp_cycle_table[ea_mode];
	if(strcmp(g_func_table[func_num].name, "m68000_jsr") == 0)
		return g_jsr_cycle_table[ea_mode];
	if(strcmp(g_func_table[func_num].name, "m68000_lea") == 0)
		return g_lea_cycle_table[ea_mode];
	if(strcmp(g_func_table[func_num].name, "m68000_pea") == 0)
		return g_pea_cycle_table[ea_mode];
	if(strcmp(g_func_table[func_num].name, "m68010_moves_8") == 0 || strcmp(g_func_table[func_num].name, "m68010_moves_16") == 0)
		return g_moves_bw_cycle_table[ea_mode];
	if(strcmp(g_func_table[func_num].name, "m68000_moves_32") == 0)
		return g_moves_l_cycle_table[ea_mode];

	/* ASG: added these cases -- immediate modes take 2 extra cycles here */
	if(ea_mode == 0x3c &&
	   (strcmp(g_func_table[func_num].name, "m68000_add_er_32") == 0 ||
		strcmp(g_func_table[func_num].name, "m68000_adda_32") == 0 ||
		strcmp(g_func_table[func_num].name, "m68000_and_er_32") == 0 ||
		strcmp(g_func_table[func_num].name, "m68000_or_er_32") == 0 ||
		strcmp(g_func_table[func_num].name, "m68000_sub_er_32") == 0 ||
		strcmp(g_func_table[func_num].name, "m68000_suba_32") == 0))
		return g_ea_cycle_table[g_func_table[func_num].size][ea_mode] + 2;

	return g_ea_cycle_table[g_func_table[func_num].size][ea_mode];
}

void add_op_header(FILE* filep, char low, char high)
{
	fprintf(filep, "#include \"m68kcpu.h\"\n\n");
	fprintf(filep, "#include \"m68kops.h\"\n\n");
	fprintf(filep, "/* ======================================================================== */\n");
	fprintf(filep, "/* ======================= INSTRUCTION HANDLERS %c-%c ======================= */\n", low, high);
	fprintf(filep, "/* ======================================================================== */\n");
	fprintf(filep, "/* Instruction handler function names follow this convention:\n");
	fprintf(filep, " *\n");
	fprintf(filep, " * m68000_NAME_EXTENSIONS(void)\n");
	fprintf(filep, " * where NAME is the name of the opcode it handles and EXTENSIONS are any\n");
	fprintf(filep, " * extensions for special instances of that opcode.\n");
	fprintf(filep, " *\n");
	fprintf(filep, " * Examples:\n");
	fprintf(filep, " *   m68000_add_er_ai_8(): add opcode, from effective address to register,\n");
	fprintf(filep, " *                         using address register indirect, size = byte\n");
	fprintf(filep, " *\n");
	fprintf(filep, " *   m68000_asr_s_8(): arithmetic shift right, static count, size = byte\n");
	fprintf(filep, " *\n");
	fprintf(filep, " *\n");
	fprintf(filep, " * Note: move uses the form m68000_move_DST_SRC_SIZE\n");
	fprintf(filep, " *\n");
	fprintf(filep, " * Common extensions:\n");
	fprintf(filep, " * 8   : size = byte\n");
	fprintf(filep, " * 16  : size = word\n");
	fprintf(filep, " * 32  : size = long\n");
	fprintf(filep, " * rr  : register to register\n");
	fprintf(filep, " * mm  : memory to memory\n");
	fprintf(filep, " * a7  : using a7 register\n");
	fprintf(filep, " * ax7 : using a7 in X part of instruction (....XXX......YYY)\n");
	fprintf(filep, " * ay7 : using a7 in Y part of instruction (....XXX......YYY)\n");
	fprintf(filep, " * axy7: using a7 in both parts of instruction (....XXX......YYY)\n");
	fprintf(filep, " * r   : register\n");
	fprintf(filep, " * s   : static\n");
	fprintf(filep, " * er  : effective address -> register\n");
	fprintf(filep, " * re  : register -> effective address\n");
	fprintf(filep, " * ea  : using effective address mode of operation\n");
	fprintf(filep, " * d   : data register direct\n");
	fprintf(filep, " * a   : address register direct\n");
	fprintf(filep, " * ai  : address register indirect\n");
	fprintf(filep, " * pi  : address register indirect with postincrement\n");
	fprintf(filep, " * pi7 : address register 7 indirect with postincrement\n");
	fprintf(filep, " * pd  : address register indirect with predecrement\n");
	fprintf(filep, " * pd7 : address register 7 indirect with predecrement\n");
	fprintf(filep, " * di  : address register indirect with displacement\n");
	fprintf(filep, " * ix  : address register indirect with index\n");
	fprintf(filep, " * aw  : absolute word\n");
	fprintf(filep, " * al  : absolute long\n");
	fprintf(filep, " * pcdi: program counter with displacement\n");
	fprintf(filep, " * pcix: program counter with index\n");
	fprintf(filep, " */\n\n\n");
}

void add_prototype_header(FILE* filep)
{
	fprintf(filep, "#ifndef M68KOPS__HEADER\n");
	fprintf(filep, "#define M68KOPS__HEADER\n\n");
}

void add_prototype_footer(FILE* filep)
{
	fprintf(filep, "\n#endif /* M68KOPS__HEADER */\n");
}


void add_table_header(FILE* filep)
{
	fprintf(filep, "/* ======================================================================== */\n");
	fprintf(filep, "/* ========================= OPCODE TABLE BUILDER ========================= */\n");
	fprintf(filep, "/* ======================================================================== */\n\n");

	fprintf(filep, "#include \"m68kops.h\"\n");
	fprintf(filep, "#include \"m68kcpu.h\"\n");
	fprintf(filep, "#include <stdlib.h>\n\n");
	fprintf(filep, "#include <string.h>\n\n");

	fprintf(filep, "extern void  (*m68k_instruction_jump_table[])(void); /* opcode handler jump table */\n\n");

	fprintf(filep, "/* This is used to generate the opcode handler jump table */\n");
	fprintf(filep, "typedef struct\n");
	fprintf(filep, "{\n");
	fprintf(filep, "\tvoid (*opcode_handler)(void); /* handler function */\n");
	fprintf(filep, "\tuint bits;\t\t\t/* number of bits set in mask */\n");
    fprintf(filep, "\tuint mask;\t\t\t/* mask on opcode */\n");
	fprintf(filep, "\tuint match;\t\t\t/* what to match after masking */\n");
	fprintf(filep, "} opcode_handler_struct;\n\n\n");

	fprintf(filep, "/* Opcode handler table */\n");
	fprintf(filep, "static opcode_handler_struct m68k_opcode_handler_table[] =\n");
	fprintf(filep, "{\n");
	fprintf(filep, "/*  opcode handler              mask   match */\n");
}

void add_table_footer(FILE* filep)
{
	fprintf(filep, "\t{0, 0, 0, 0}\n");
	fprintf(filep, "};\n\n\n");

	fprintf(filep, "/*\n");
	fprintf(filep, " * Comparison function for qsort()\n");
	fprintf(filep, " * For entries with an equal number of set bits in\n");
	fprintf(filep, " * the mask compare the match values\n");
	fprintf(filep, " */\n");
	fprintf(filep, "static int DECL_SPEC compare_nof_true_bits(const void* aptr, const void* bptr)\n");
	fprintf(filep, "{\n");
	fprintf(filep, "\tconst opcode_handler_struct *a = aptr, *b = bptr;\n");
	fprintf(filep, "\tif( a->bits != b->bits )\n");
	fprintf(filep, "\t\treturn a->bits - b->bits;\n");
	fprintf(filep, "\tif( a->mask != b->mask )\n");
	fprintf(filep, "\t\treturn a->mask - b->mask;\n");
	fprintf(filep, "\treturn a->match - b->match;\n");
	fprintf(filep, "}\n\n");

	fprintf(filep, "/* Build the opcode handler jump table */\n");
	fprintf(filep, "void m68ki_build_opcode_table(void)\n");
	fprintf(filep, "{\n");
	fprintf(filep, "\topcode_handler_struct *ostruct;\n");
	fprintf(filep, "\tuint table_length = 0;\n");
	fprintf(filep, "\tint i,j;\n");
	fprintf(filep, "\n");
	fprintf(filep, "\tfor(ostruct = m68k_opcode_handler_table;ostruct->opcode_handler != 0;ostruct++)\n");
	fprintf(filep, "\t\ttable_length++;\n");
	fprintf(filep, "\n");
	fprintf(filep, "\tqsort((void *)m68k_opcode_handler_table, table_length, sizeof(m68k_opcode_handler_table[0]), compare_nof_true_bits);\n");
	fprintf(filep, "\n");
	fprintf(filep, "\tfor( i = 0; i < 0x10000; i++ )\n");
	fprintf(filep, "\t{\n");
	fprintf(filep, "\t\t/* default to illegal */\n");
	fprintf(filep, "\t\tm68k_instruction_jump_table[i] = m68000_illegal;\n");
	fprintf(filep, "\t}\n");
	fprintf(filep, "\n");
	fprintf(filep, "\tostruct = m68k_opcode_handler_table;\n");
	fprintf(filep, "\twhile (ostruct->mask != 0xff00)\n");
	fprintf(filep, "\t{\n");
	fprintf(filep, "\t\tfor (i = 0;i < 0x10000;i++)\n");
	fprintf(filep, "\t\t{\n");
	fprintf(filep, "\t\t\tif ((i & ostruct->mask) == ostruct->match)\n");
	fprintf(filep, "\t\t\t{\n");
	fprintf(filep, "\t\t\t\tm68k_instruction_jump_table[i] = ostruct->opcode_handler;\n");
	fprintf(filep, "\t\t\t}\n");
	fprintf(filep, "\t\t}\n");
	fprintf(filep, "\t\tostruct++;\n");
	fprintf(filep, "\t}\n");
	fprintf(filep, "\twhile (ostruct->mask == 0xff00)\n");
	fprintf(filep, "\t{\n");
	fprintf(filep, "\t\tfor (i = 0;i <= 0xff;i++)\n");
	fprintf(filep, "\t\t\tm68k_instruction_jump_table[ostruct->match | i] = ostruct->opcode_handler;\n");
	fprintf(filep, "\t\tostruct++;\n");
	fprintf(filep, "\t}\n");
	fprintf(filep, "\twhile (ostruct->mask == 0xf1f8)\n");
	fprintf(filep, "\t{\n");
	fprintf(filep, "\t\tfor (i = 0;i < 8;i++)\n");
	fprintf(filep, "\t\t{\n");
	fprintf(filep, "\t\t\tfor (j = 0;j < 8;j++)\n");
	fprintf(filep, "\t\t\t{\n");
	fprintf(filep, "\t\t\t\tm68k_instruction_jump_table[ostruct->match | (i << 9) | j] = ostruct->opcode_handler;\n");
	fprintf(filep, "\t\t\t}\n");
	fprintf(filep, "\t\t}\n");
	fprintf(filep, "\t\tostruct++;\n");
	fprintf(filep, "\t}\n");
	fprintf(filep, "\twhile (ostruct->mask == 0xfff0)\n");
	fprintf(filep, "\t{\n");
	fprintf(filep, "\t\tfor (i = 0;i <= 0x0f;i++)\n");
	fprintf(filep, "\t\t\tm68k_instruction_jump_table[ostruct->match | i] = ostruct->opcode_handler;\n");
	fprintf(filep, "\t\tostruct++;\n");
	fprintf(filep, "\t}\n");
	fprintf(filep, "\twhile (ostruct->mask == 0xf1ff)\n");
	fprintf(filep, "\t{\n");
	fprintf(filep, "\t\tfor (i = 0;i <= 0x07;i++)\n");
	fprintf(filep, "\t\t\tm68k_instruction_jump_table[ostruct->match | (i << 9)] = ostruct->opcode_handler;\n");
	fprintf(filep, "\t\tostruct++;\n");
	fprintf(filep, "\t}\n");
	fprintf(filep, "\twhile (ostruct->mask == 0xfff8)\n");
	fprintf(filep, "\t{\n");
	fprintf(filep, "\t\tfor (i = 0;i <= 0x07;i++)\n");
	fprintf(filep, "\t\t\tm68k_instruction_jump_table[ostruct->match | i] = ostruct->opcode_handler;\n");
	fprintf(filep, "\t\tostruct++;\n");
	fprintf(filep, "\t}\n");
	fprintf(filep, "\twhile (ostruct->mask == 0xffff)\n");
	fprintf(filep, "\t{\n");
	fprintf(filep, "\t\tm68k_instruction_jump_table[ostruct->match] = ostruct->opcode_handler;\n");
	fprintf(filep, "\t\tostruct++;\n");
	fprintf(filep, "\t}\n");
	fprintf(filep, "}\n");
}


int generate_funcs(FILE* file_ac, FILE* file_dm, FILE* file_nz)
{
	FILE* input_file;
	FILE* output_file;
	static char func_lines[200][300];
	char name[200];
	char full_name[200];
	char* name_start;
	char* name_end;
	char insert[30];
	int insert_pt;
	int num_lines = 0;
	int ea_line = 0;
	int i;
	int func_num;
	int ea;
	int size;
	char ea_spaces[200];

	/* Our input file is m68k_in.c */
	if((input_file=fopen(g_m68k_in_c, "rt")) == NULL)
	{
		sprintf(name, "can't open %s", g_m68k_in_c);
		perror(name);
		exit(-1);
	}

	/* Add the header to the first output file */
	add_op_header(output_file = file_ac, 'A', 'C');

	for(;;)
	{
		/* Find the first line of the function */
		func_lines[0][0] = '\n';
		while(func_lines[0][0] == '\n')
			if(safe_fgets(func_lines[0], 200, input_file) == NULL)
				exit(0);

		/* Extract the name of the function */
		name_start = strstr(func_lines[0], "m68");
		strcpy(name, name_start);
		name_end = strstr(name, "(");
		*name_end = '\0';
		while( name_end > name && name_end[-1] == ' ' )
			*--name_end = '\0';
		strcpy(full_name, name);

        /* Change output files if we pass 'c' or 'n' */
		if(output_file == file_ac && full_name[7] > 'c')
			add_op_header(output_file = file_dm, 'D', 'M');
		if(output_file == file_dm && full_name[7] > 'm')
			add_op_header(output_file = file_nz, 'N', 'Z');

		/* Find the point in the name to insert ea mode bits */
		switch(*(name_end-1))
		{
		case '8':
			insert_pt = - 2;
			break;
		case '2': case '6':
			insert_pt = - 3;
			break;
		default:
			insert_pt = 0;
		}

		strcpy(insert, name_end+insert_pt);
		*(name_end+insert_pt) = 0;

		/* Get the rest of the function and find the "get_ea" bit */
		num_lines = 0;
		ea_line = 0;
		for(i=1;i<200;i++)
		{
			if(safe_fgets(func_lines[i], 200, input_file) == NULL)
				exit(0);
			if(func_lines[i][0] == '}')
			{
				num_lines = i+1; /* don't cut off the } */
				break;
			}
			if(strstr(func_lines[i], "m68ki_get_ea_8") != NULL ||
			   strstr(func_lines[i], "m68ki_get_ea_16") != NULL ||
			   strstr(func_lines[i], "m68ki_get_ea_32") != NULL)
				ea_line = i;
		}

		/* Look for the function in the function table */
		func_num = -1;
		/* find what function it is */
		for(i=0;g_func_table[i].name != NULL;i++)
			if(strcmp(full_name, g_func_table[i].name) == 0)
				func_num = i;

		if(func_num == -1)
		{
			printf("Unable to find function %s\n", full_name);
			g_errors++;
			continue;
		}

		/* Get the ea mask and size of function */
		ea = g_func_table[func_num].ea_mask;
		size = g_func_table[func_num].size;

		/* If there's no get_ea part to this function */
		if(ea_line == 0)
		{
			/* Error: there's no get_ea, but the function table says ther should be) */
			if(ea != 0)
			{
				printf("Can't find EA line for function %s\n", full_name);
				g_errors++;
				continue;
			}
			/* This function doesn't need any ea modes added */
			/* print function as-is */
			for(i=0;i<num_lines;i++)
				fprintf(output_file, "%s", func_lines[i]);
			/* print 2 newlines */
			fprintf(output_file, "\n\n");
			continue;
		}

		/* Preserve indentation */
		for(i=0;func_lines[ea_line][i] == '\t' || func_lines[ea_line][i] == ' ';i++)
			ea_spaces[i] = func_lines[ea_line][i];
		ea_spaces[i] = 0;

		/* Sanity check */
		if(ea == 0)
		{
			printf("ERROR!! %s should not have EA 0 at this point\n", func_lines[0]);
			g_errors++;
		}


		/* Now we check the ea mode value to see what versions of this function we have to generate. */

		if(ea & 0x200) /* AI */
		{
			/* print new function name */
			fprintf(output_file, "void %s_ai%s(void)\n", name, insert);
			/* print up to ea line */
			for(i=1;i<ea_line;i++)
				fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x10)));
			/* print new ea mode */
			if(strstr(func_lines[ea_line], "uint ea") != NULL)
				fprintf(output_file, "%suint ea = EA_AI;\n", ea_spaces);
			else
				fprintf(output_file, "%s", modify_ea_string(func_lines[ea_line], "EA_AI"));

			/* print rest of function */
			for(i=ea_line+1;i<num_lines;i++)
				fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x10)));
			/* print 2 newlines */
			fprintf(output_file, "\n\n");
		}
		if(ea & 0x100) /* PI */
		{
			if(size == 0)
			{
				/* print new function name */
				fprintf(output_file, "void %s_pi%s(void)\n", name, insert);
				/* print up to ea line */
				for(i=1;i<ea_line;i++)
					fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x18)));
				/* print new ea mode */
				if(strstr(func_lines[ea_line], "uint ea") != NULL)
					fprintf(output_file, "%suint ea = EA_PI_8;\n", ea_spaces);
				else
					fprintf(output_file, "%s", modify_ea_string(func_lines[ea_line], "EA_PI_8"));

				/* print rest of function */
				for(i=ea_line+1;i<num_lines;i++)
				fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x18)));
				/* print 2 newlines */
				fprintf(output_file, "\n\n");

				/* print new function name */
				fprintf(output_file, "void %s_pi7%s(void)\n", name, insert);
				/* print up to ea line */
				for(i=1;i<ea_line;i++)
					fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x1f)));
				/* print new ea mode */
				if(strstr(func_lines[ea_line], "uint ea") != NULL)
					fprintf(output_file, "%suint ea = EA_PI7_8;\n", ea_spaces);
				else
					fprintf(output_file, "%s", modify_ea_string(func_lines[ea_line], "EA_PI7_8"));

				/* print rest of function */
				for(i=ea_line+1;i<num_lines;i++)
					fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x1f)));
				/* print 2 newlines */
				fprintf(output_file, "\n\n");
			}
			else if(size == 1)
			{
				/* print new function name */
				fprintf(output_file, "void %s_pi%s(void)\n", name, insert);
				/* print up to ea line */
				for(i=1;i<ea_line;i++)
					fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x18)));
				/* print new ea mode */
				if(strstr(func_lines[ea_line], "uint ea") != NULL)
					fprintf(output_file, "%suint ea = EA_PI_16;\n", ea_spaces);
				else
					fprintf(output_file, "%s", modify_ea_string(func_lines[ea_line], "EA_PI_16"));

				/* print rest of function */
				for(i=ea_line+1;i<num_lines;i++)
					fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x18)));
				/* print 2 newlines */
				fprintf(output_file, "\n\n");
			}
			else if(size == 2)
			{
				/* print new function name */
				fprintf(output_file, "void %s_pi%s(void)\n", name, insert);
				/* print up to ea line */
				for(i=1;i<ea_line;i++)
					fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x18)));
				/* print new ea mode */
				if(strstr(func_lines[ea_line], "uint ea") != NULL)
					fprintf(output_file, "%suint ea = EA_PI_32;\n", ea_spaces);
				else
					fprintf(output_file, "%s", modify_ea_string(func_lines[ea_line], "EA_PI_32"));

				/* print rest of function */
				for(i=ea_line+1;i<num_lines;i++)
					fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x18)));
				/* print 2 newlines */
				fprintf(output_file, "\n\n");
			}
		}
		if(ea & 0x80) /* PD */
		{
		   if(size == 0)
		   {
			   /* print new function name */
			   fprintf(output_file, "void %s_pd%s(void)\n", name, insert);
			   /* print up to ea line */
			   for(i=1;i<ea_line;i++)
					fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x20)));
			   /* print new ea mode */
			   if(strstr(func_lines[ea_line], "uint ea") != NULL)
					fprintf(output_file, "%suint ea = EA_PD_8;\n", ea_spaces);
			   else
					fprintf(output_file, "%s", modify_ea_string(func_lines[ea_line], "EA_PD_8"));

			   /* print rest of function */
			   for(i=ea_line+1;i<num_lines;i++)
					fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x20)));
			   /* print 2 newlines */
			   fprintf(output_file, "\n\n");


			   /* print new function name */
			   fprintf(output_file, "void %s_pd7%s(void)\n", name, insert);
			   /* print up to ea line */
			   for(i=1;i<ea_line;i++)
					fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x27)));
			   /* print new ea mode */
			   if(strstr(func_lines[ea_line], "uint ea") != NULL)
					fprintf(output_file, "%suint ea = EA_PD7_8;\n", ea_spaces);
			   else
					fprintf(output_file, "%s", modify_ea_string(func_lines[ea_line], "EA_PD7_8"));

			   /* print rest of function */
			   for(i=ea_line+1;i<num_lines;i++)
					fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x27)));
			   /* print 2 newlines */
			   fprintf(output_file, "\n\n");
		   }
		   else if(size == 1)
		   {
			   /* print new function name */
			   fprintf(output_file, "void %s_pd%s(void)\n", name, insert);
			   /* print up to ea line */
			   for(i=1;i<ea_line;i++)
					fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x20)));
			   /* print new ea mode */
			   if(strstr(func_lines[ea_line], "uint ea") != NULL)
					fprintf(output_file, "%suint ea = EA_PD_16;\n", ea_spaces);
			   else
					fprintf(output_file, "%s", modify_ea_string(func_lines[ea_line], "EA_PD_16"));

			   /* print rest of function */
			   for(i=ea_line+1;i<num_lines;i++)
					fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x20)));
			   /* print 2 newlines */
			   fprintf(output_file, "\n\n");
		   }
		   else if(size == 2)
		   {
			   /* print new function name */
			   fprintf(output_file, "void %s_pd%s(void)\n", name, insert);
			   /* print up to ea line */
			   for(i=1;i<ea_line;i++)
					fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x20)));
			   /* print new ea mode */
			   if(strstr(func_lines[ea_line], "uint ea") != NULL)
					fprintf(output_file, "%suint ea = EA_PD_32;\n", ea_spaces);
			   else
					fprintf(output_file, "%s", modify_ea_string(func_lines[ea_line], "EA_PD_32"));

			   /* print rest of function */
			   for(i=ea_line+1;i<num_lines;i++)
					fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x20)));
			   /* print 2 newlines */
			   fprintf(output_file, "\n\n");
		   }
		}
		if(ea & 0x40) /* DI */
		{
			/* print new function name */
			fprintf(output_file, "void %s_di%s(void)\n", name, insert);
			/* print up to ea line */
			for(i=1;i<ea_line;i++)
				fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x28)));
			/* print new ea mode */
			if(strstr(func_lines[ea_line], "uint ea") != NULL)
				fprintf(output_file, "%suint ea = EA_DI;\n", ea_spaces);
			else
				fprintf(output_file, "%s", modify_ea_string(func_lines[ea_line], "EA_DI"));

			/* print rest of function */
			for(i=ea_line+1;i<num_lines;i++)
				fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x28)));
			/* print 2 newlines */
			fprintf(output_file, "\n\n");
		}
		if(ea & 0x20) /* IX */
		{
			/* print new function name */
			fprintf(output_file, "void %s_ix%s(void)\n", name, insert);
			/* print up to ea line */
			for(i=1;i<ea_line;i++)
				fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x30)));
			/* print new ea mode */
			if(strstr(func_lines[ea_line], "uint ea") != NULL)
				fprintf(output_file, "%suint ea = EA_IX;\n", ea_spaces);
			else
				fprintf(output_file, "%s", modify_ea_string(func_lines[ea_line], "EA_IX"));

			/* print rest of function */
			for(i=ea_line+1;i<num_lines;i++)
				fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x30)));
			/* print 2 newlines */
			fprintf(output_file, "\n\n");
		}
		if(ea & 0x10) /* AW */
		{
			/* print new function name */
			fprintf(output_file, "void %s_aw%s(void)\n", name, insert);
			/* print up to ea line */
			for(i=1;i<ea_line;i++)
				fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x38)));
			/* print new ea mode */
			if(strstr(func_lines[ea_line], "uint ea") != NULL)
				fprintf(output_file, "%suint ea = EA_AW;\n", ea_spaces);
			else
				fprintf(output_file, "%s", modify_ea_string(func_lines[ea_line], "EA_AW"));

			/* print rest of function */
			for(i=ea_line+1;i<num_lines;i++)
				fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x38)));
			/* print 2 newlines */
			fprintf(output_file, "\n\n");
		}
		if(ea & 0x8) /* AL */
		{
			/* print new function name */
			fprintf(output_file, "void %s_al%s(void)\n", name, insert);
			/* print up to ea line */
			for(i=1;i<ea_line;i++)
				fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x39)));
			/* print new ea mode */
			if(strstr(func_lines[ea_line], "uint ea") != NULL)
				fprintf(output_file, "%suint ea = EA_AL;\n", ea_spaces);
			else
				fprintf(output_file, "%s", modify_ea_string(func_lines[ea_line], "EA_AL"));

			/* print rest of function */
			for(i=ea_line+1;i<num_lines;i++)
				fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x39)));
			/* print 2 newlines */
			fprintf(output_file, "\n\n");
		}
		if(ea & 0x2) /* PCDI */
		{
			/* print new function name */
			fprintf(output_file, "void %s_pcdi%s(void)\n", name, insert);
			/* print up to ea line */
			for(i=1;i<ea_line;i++)
				fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x3a)));
			/* print new ea mode */
			fprintf(output_file, "%suint old_pc = (CPU_PC+=2) - 2;\n", ea_spaces);
			fprintf(output_file, "%suint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));\n", ea_spaces);
			fprintf(output_file, "%s", modify_ea_string(func_lines[ea_line], "ea"));

			/* print rest of function */
			for(i=ea_line+1;i<num_lines;i++)
				fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x3a)));
			/* print 2 newlines */
			fprintf(output_file, "\n\n");
		}
		if(ea & 0x1) /* PCIX */
		{
			/* print new function name */
			fprintf(output_file, "void %s_pcix%s(void)\n", name, insert);
			/* print up to ea line */
			for(i=1;i<ea_line;i++)
				fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x3b)));
			/* print new ea mode */
			if(strstr(func_lines[ea_line], "uint ea") != NULL)
				fprintf(output_file, "%suint ea = EA_PCIX;\n", ea_spaces);
			else
				fprintf(output_file, "%s", modify_ea_string(func_lines[ea_line], "EA_PCIX"));

			/* print rest of function */
			for(i=ea_line+1;i<num_lines;i++)
				fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x3b)));
			/* print 2 newlines */
			fprintf(output_file, "\n\n");
		}
		if(ea & 0x4) /* IMM */
		{
			char* imm_ptr = "m68ki_read_imm_8()";
			if(size == 1)
				imm_ptr = "m68ki_read_imm_16()";
			else if(size == 2)
				imm_ptr = "m68ki_read_imm_32()";

			/* print new function name */
			fprintf(output_file, "void %s_i%s(void)\n", name, insert);
			/* print up to ea line */
			for(i=1;i<ea_line;i++)
				fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x3c)));
			/* print new ea mode */
			if(strstr(func_lines[ea_line], "uint ea") != NULL)
				fprintf(output_file, "%suint ea = %s;\n", imm_ptr, ea_spaces);
			else
				fprintf(output_file, "%s", modify_imm_string(func_lines[ea_line], imm_ptr));

			/* print rest of function */
			for(i=ea_line+1;i<num_lines;i++)
				fprintf(output_file, "%s", strstr(func_lines[i], "USE_CLKS") == NULL ? func_lines[i] : replace_clk_string(func_lines[i], "USE_CLKS", get_clk_add(func_num, 0x3c)));
			/* print 2 newlines */
			fprintf(output_file, "\n\n");
		 }
	 }
	 fclose(input_file);
	 return 0;
}


/* Generate the opcode handler table */

int generate_table(FILE* table_file)
{
	char name[100];
	char new_name[100];
	char* name_start;
	char* name_end;
	char* insert;
	int name_length;
	int mask;
	int match;
	int ea;
	int size;
	int clks;
	int i;


	/* Go through the entire function table */
	for( i = 0; g_func_table[i].name; i++ )
	{
		/* isolate the name parts */
		name_start = g_func_table[i].name;
		name_end = name_start + strlen(name_start);

        /* Find the point to insert the ea bits */
		switch(*(name_end-1))
		{
		case '8':
			insert = name_end - 2;
			break;
		case '2': case '6':
			insert = name_end - 3;
			break;
		default:
			insert = name_end;
		}

		name_length = insert - name_start;

		strcpy(name, name_start);
		mask = g_func_table[i].op_mask;
		match = g_func_table[i].op_match;
		ea = g_func_table[i].ea_mask;
		size = g_func_table[i].size;
		clks = g_func_table[i].base_cycles;

        /* This function has no ea modes, so just blast it through */
		if(ea == 0)
		{
			PRINT_TABLE_ENTRY(table_file, name, mask, match, clks);
			continue;
		}

		*(name+name_length) = 0;
		mask |= 0x38;

		/* Print out all the versions of this function that are necessary */
		if(ea & 0x200) /* AI */
		{
			sprintf(new_name, "%s_ai%s", name, insert);
			PRINT_TABLE_ENTRY(table_file, new_name, mask, match | 0x10, clks+g_ea_cycle_table[size][0x10]);
		}
		if(ea & 0x100) /* PI */
		{
			sprintf(new_name, "%s_pi%s", name, insert);
			PRINT_TABLE_ENTRY(table_file, new_name, mask, match | 0x18, clks+g_ea_cycle_table[size][0x18]);
			if(size == 0)
			{
				sprintf(new_name, "%s_pi7%s", name, insert);
				PRINT_TABLE_ENTRY(table_file, new_name, mask | 7, match | 0x1f, clks+g_ea_cycle_table[size][0x1f]);
			}
		}
		if(ea & 0x80) /* PD */
		{
			sprintf(new_name, "%s_pd%s", name, insert);
			PRINT_TABLE_ENTRY(table_file, new_name, mask, match | 0x20, clks+g_ea_cycle_table[size][0x20]);
			if(size == 0)
			{
				sprintf(new_name, "%s_pd7%s", name, insert);
				PRINT_TABLE_ENTRY(table_file, new_name, mask | 7, match | 0x27, clks+g_ea_cycle_table[size][0x27]);
			}
		}
		if(ea & 0x40) /* DI */
		{
			sprintf(new_name, "%s_di%s", name, insert);
			PRINT_TABLE_ENTRY(table_file, new_name, mask, match | 0x28, clks+g_ea_cycle_table[size][0x28]);
		}
		if(ea & 0x20) /* IX */
		{
			sprintf(new_name, "%s_ix%s", name, insert);
			PRINT_TABLE_ENTRY(table_file, new_name, mask, match | 0x30, clks+g_ea_cycle_table[size][0x30]);
		}

		mask |= 7;

		if(ea & 0x10) /* AW */
		{
			sprintf(new_name, "%s_aw%s", name, insert);
			PRINT_TABLE_ENTRY(table_file, new_name, mask, match | 0x38, clks+g_ea_cycle_table[size][0x38]);
		}
		if(ea & 0x8) /* AL */
		{
			sprintf(new_name, "%s_al%s", name, insert);
			PRINT_TABLE_ENTRY(table_file, new_name, mask, match | 0x39, clks+g_ea_cycle_table[size][0x39]);
		}
		if(ea & 0x2) /* PCDI */
		{
			sprintf(new_name, "%s_pcdi%s", name, insert);
			PRINT_TABLE_ENTRY(table_file, new_name, mask, match | 0x3a, clks+g_ea_cycle_table[size][0x3a]);
		}
		if(ea & 0x1) /* PCIX */
		{
			sprintf(new_name, "%s_pcix%s", name, insert);
			PRINT_TABLE_ENTRY(table_file, new_name, mask, match | 0x3b, clks+g_ea_cycle_table[size][0x3b]);
		}
		if(ea & 0x4) /* IMM */
		{
			sprintf(new_name, "%s_i%s", name, insert);
			PRINT_TABLE_ENTRY(table_file, new_name, mask, match | 0x3c, clks+g_ea_cycle_table[size][0x3c]);
		}
	}
	return 0;
}


/* Generate all the function prototypes */

int generate_prototypes(FILE* prototype_file)
{
	char name[100];
	char new_name[100];
	char* name_start;
	char* name_end;
	char* insert;
	int name_length;
	int mask;
	int match;
	int ea;
	int size;
	int clks;
	int i;

	fprintf(prototype_file, "/* ======================================================================== */\n");
	fprintf(prototype_file, "/* ============================ OPCODE HANDLERS =========================== */\n");
	fprintf(prototype_file, "/* ======================================================================== */\n\n");

    /* Go through the entire function table */
	for(i=0;g_func_table[i].name != 0;i++)
	{
		/* Get the name parts */
		name_start = g_func_table[i].name;
		name_end = name_start + strlen(name_start);

		/* Find the point to insert the ea bits */
		switch(*(name_end-1))
		{
		case '8':
			insert = name_end - 2;
			break;
		case '2': case '6':
			insert = name_end - 3;
			break;
		default:
			insert = name_end;
		}

		name_length = insert - name_start;

		strcpy(name, name_start);
		mask = g_func_table[i].op_mask;
		match = g_func_table[i].op_match;
		ea = g_func_table[i].ea_mask;
		size = g_func_table[i].size;
		clks = g_func_table[i].base_cycles;

		/* This function has no ea modes, so just blast it through */
		if(ea == 0)
		{
			PRINT_PROTOTYPE(prototype_file, name);
			continue;
		}

		*(name+name_length) = 0;
		mask |= 0x38;

		/* Print out all the versions of htis functions as necessary */
		if(ea & 0x200) /* AI */
		{
			sprintf(new_name, "%s_ai%s", name, insert);
			PRINT_PROTOTYPE(prototype_file, new_name);
		}
		if(ea & 0x100) /* PI */
		{
			sprintf(new_name, "%s_pi%s", name, insert);
			PRINT_PROTOTYPE(prototype_file, new_name);
			if(size == 0)
			{
				sprintf(new_name, "%s_pi7%s", name, insert);
				PRINT_PROTOTYPE(prototype_file, new_name);
			}
		}
		if(ea & 0x80) /* PD */
		{
			sprintf(new_name, "%s_pd%s", name, insert);
			PRINT_PROTOTYPE(prototype_file, new_name);
			if(size == 0)
			{
				sprintf(new_name, "%s_pd7%s", name, insert);
				PRINT_PROTOTYPE(prototype_file, new_name);
			}
		}
		if(ea & 0x40) /* DI */
		{
			sprintf(new_name, "%s_di%s", name, insert);
			PRINT_PROTOTYPE(prototype_file, new_name);
		}
		if(ea & 0x20) /* IX */
		{
			sprintf(new_name, "%s_ix%s", name, insert);
			PRINT_PROTOTYPE(prototype_file, new_name);
		}

		mask |= 7;

		if(ea & 0x10) /* AW */
		{
			sprintf(new_name, "%s_aw%s", name, insert);
			PRINT_PROTOTYPE(prototype_file, new_name);
		}
		if(ea & 0x8) /* AL */
		{
			sprintf(new_name, "%s_al%s", name, insert);
			PRINT_PROTOTYPE(prototype_file, new_name);
		}
		if(ea & 0x2) /* PCDI */
		{
			sprintf(new_name, "%s_pcdi%s", name, insert);
			PRINT_PROTOTYPE(prototype_file, new_name);
		}
		if(ea & 0x1) /* PCIX */
		{
			sprintf(new_name, "%s_pcix%s", name, insert);
			PRINT_PROTOTYPE(prototype_file, new_name);
		}
		if(ea & 0x4) /* IMM */
		{
			sprintf(new_name, "%s_i%s", name, insert);
			PRINT_PROTOTYPE(prototype_file, new_name);
		}
	}
	return 0;
}


int main(int ac, char **av)
{
	char output_path[255+1] = "";
	char filename[255+1];
    char* prototype_filename = "m68kops.h";
	char* table_filename	 = "m68kops.c";
	char* ops_ac_filename	 = "m68kopac.c";
	char* ops_dm_filename	 = "m68kopdm.c";
	char* ops_nz_filename	 = "m68kopnz.c";
	FILE* prototype_file;
	FILE* table_file;
	FILE* ops_ac_file;
	FILE* ops_dm_file;
	FILE* ops_nz_file;

	/* Check if output path and source for m68k_in.c are given */
    if( ac > 1 )
	{
		char *p;
		strcpy( output_path, av[1] );

		for( p = strchr(output_path, '\\'); p; p = strchr(p, '\\') )
			*p = '/';
        if( output_path[strlen(output_path)-1] != '/' )
			strcat( output_path, "/" );
		if( ac > 2 )
			strcpy(g_m68k_in_c, av[2]);
	}

	sprintf(filename, "%s%s", output_path, prototype_filename);
	if((prototype_file = fopen(filename, "wt")) == NULL)
	{
		fprintf(stderr, "Unable to create prototype file (%s)\n", filename);
		perror("");
		exit(-1);
	}

	sprintf(filename, "%s%s", output_path, table_filename);
	if((table_file = fopen(filename, "wt")) == NULL)
	{
		fprintf(stderr, "Unable to create table file (%s)\n", filename);
		perror("");
		exit(-1);
	}

	sprintf(filename, "%s%s", output_path, ops_ac_filename);
	if((ops_ac_file = fopen(filename, "wt")) == NULL)
	{
		fprintf(stderr, "Unable to create ops a-c file (%s)\n", filename);
		perror("");
		exit(-1);
	}
	sprintf(filename, "%s%s", output_path, ops_dm_filename);
	if((ops_dm_file = fopen(filename, "wt")) == NULL)
	{
		fprintf(stderr, "Unable to create ops d-m file (%s)\n", filename);
		exit(-1);
	}

	sprintf(filename, "%s%s", output_path, ops_nz_filename);
	if((ops_nz_file = fopen(filename, "wt")) == NULL)
	{
		fprintf(stderr, "Unable to create ops n-z file (%s)\n", filename);
		perror("");
		exit(-1);
	}

    add_prototype_header(prototype_file);
	generate_prototypes(prototype_file);
	add_prototype_footer(prototype_file);

	add_table_header(table_file);
	generate_table(table_file);
	add_table_footer(table_file);

	generate_funcs(ops_ac_file, ops_dm_file, ops_nz_file);

	fclose(prototype_file);
	fclose(table_file);
	fclose(ops_ac_file);
	fclose(ops_dm_file);
	fclose(ops_nz_file);

	printf("Process completed with %d errors\n", g_errors);

	return g_errors ? -1 : 0;
}
