# List of CPU core (and, for a debug build, disassembler) object files

CPU=$(strip $(findstring Z80@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/z80
CPUDEFS += -DHAS_Z80=1
CPUOBJS += $(OBJ)/cpu/z80/z80.o
DBGOBJS += $(OBJ)/cpu/z80/z80dasm.o
$(OBJ)/cpu/z80/z80.o: z80.c z80.h z80daa.h
else
CPUDEFS += -DHAS_Z80=0
endif

CPU=$(strip $(findstring Z80GB@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/z80gb
CPUDEFS += -DHAS_Z80GB=1
CPUOBJS += $(OBJ)/cpu/z80gb/z80gb.o
DBGOBJS += $(OBJ)/cpu/z80gb/z80gbd.o
$(OBJ)/cpu/z80gb/z80gb.o: z80gb.c z80gb.h daa_tab.h opc_cb.h opc_main.h
else
CPUDEFS += -DHAS_Z80GB=0
endif

CPU=$(strip $(findstring CDP1802@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/cdp1802
CPUDEFS += -DHAS_CDP1802=1
CPUOBJS += $(OBJ)/cpu/cdp1802/cdp1802.o
DBGOBJS += $(OBJ)/cpu/cdp1802/disasm.o
$(OBJ)/cpu/cdp1802/cdp1802.o: table.c
else
CPUDEFS += -DHAS_CDP1802=0
endif

CPU=$(strip $(findstring 8080@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/i8085
CPUDEFS += -DHAS_8080=1
CPUOBJS += $(OBJ)/cpu/i8085/i8085.o
DBGOBJS += $(OBJ)/cpu/i8085/8085dasm.o
$(OBJ)/cpu/i8085/i8085.o: i8085.c i8085.h i8085cpu.h i8085daa.h
else
CPUDEFS += -DHAS_8080=0
endif

CPU=$(strip $(findstring 8085A@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/i8085
CPUDEFS += -DHAS_8085A=1
CPUOBJS += $(OBJ)/cpu/i8085/i8085.o
DBGOBJS += $(OBJ)/cpu/i8085/8085dasm.o
$(OBJ)/cpu/i8085/i8085.o: i8085.c i8085.h i8085cpu.h i8085daa.h
else
CPUDEFS += -DHAS_8085A=0
endif

CPU=$(strip $(findstring M6502@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6502
CPUDEFS += -DHAS_M6502=1
CPUOBJS += $(OBJ)/cpu/m6502/m6502.o
DBGOBJS += $(OBJ)/cpu/m6502/6502dasm.o
$(OBJ)/cpu/m6502/m6502.o: m6502.c m6502.h ops02.h t6502.c t65c02.c t65sc02.c t6510.c
else
CPUDEFS += -DHAS_M6502=0
endif

CPU=$(strip $(findstring M65C02@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6502
CPUDEFS += -DHAS_M65C02=1
CPUOBJS += $(OBJ)/cpu/m6502/m6502.o
DBGOBJS += $(OBJ)/cpu/m6502/6502dasm.o
$(OBJ)/cpu/m6502/m6502.o: m6502.c m6502.h ops02.h t6502.c t65c02.c t65sc02.c t6510.c
else
CPUDEFS += -DHAS_M65C02=0
endif

CPU=$(strip $(findstring M65SC02@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6502
CPUDEFS += -DHAS_M65SC02=1
CPUOBJS += $(OBJ)/cpu/m6502/m6502.o
DBGOBJS += $(OBJ)/cpu/m6502/6502dasm.o
$(OBJ)/cpu/m6502/m6502.o: m6502.c m6502.h ops02.h t6502.c t65c02.c t65sc02.c t6510.c
else
CPUDEFS += -DHAS_M65SC02=0
endif

CPU=$(strip $(findstring M65CE02@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6502
CPUDEFS += -DHAS_M65CE02=1
CPUOBJS += $(OBJ)/cpu/m6502/m65ce02.o
DBGOBJS += $(OBJ)/cpu/m6502/6502dasm.o
$(OBJ)/cpu/m6502/m65ce02.o: m65ce02.c m65ce02.h opsce02.h t65ce02.c
else
CPUDEFS += -DHAS_M65CE02=0
endif

CPU=$(strip $(findstring M6509@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6502
CPUDEFS += -DHAS_M6509=1
CPUOBJS += $(OBJ)/cpu/m6502/m6509.o
DBGOBJS += $(OBJ)/cpu/m6502/6502dasm.o
$(OBJ)/cpu/m6502/m6509.o: m6509.c m6509.h ops09.h t6509.c
else
CPUDEFS += -DHAS_M6509=0
endif

CPU=$(strip $(findstring M6510@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6502
CPUDEFS += -DHAS_M6510=1
CPUOBJS += $(OBJ)/cpu/m6502/m6502.o
DBGOBJS += $(OBJ)/cpu/m6502/6502dasm.o
$(OBJ)/cpu/m6502/m6502.o: m6502.c m6502.h ops02.h t6502.c t65c02.c t65sc02.c t6510.c
else
CPUDEFS += -DHAS_M6510=0
endif

CPU=$(strip $(findstring M6510T@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6502
CPUDEFS += -DHAS_M6510=1 -DHAS_M6510T=1
CPUOBJS += $(OBJ)/cpu/m6502/m6502.o
DBGOBJS += $(OBJ)/cpu/m6502/6502dasm.o
$(OBJ)/cpu/m6502/m6502.o: m6502.c m6502.h ops02.h t6502.c t65c02.c t65sc02.c t6510.c
else
CPUDEFS += -DHAS_M6510T=0
endif

CPU=$(strip $(findstring M7501@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6502
CPUDEFS += -DHAS_M6510=1 -DHAS_M7501=1
CPUOBJS += $(OBJ)/cpu/m6502/m6502.o
DBGOBJS += $(OBJ)/cpu/m6502/6502dasm.o
$(OBJ)/cpu/m6502/m6502.o: m6502.c m6502.h ops02.h t6502.c t65c02.c t65sc02.c t6510.c
else
CPUDEFS += -DHAS_M7501=0
endif

CPU=$(strip $(findstring M8502@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6502
CPUDEFS += -DHAS_M6510=1 -DHAS_M8502=1
CPUOBJS += $(OBJ)/cpu/m6502/m6502.o
DBGOBJS += $(OBJ)/cpu/m6502/6502dasm.o
$(OBJ)/cpu/m6502/m6502.o: m6502.c m6502.h ops02.h t6502.c t65c02.c t65sc02.c t6510.c
else
CPUDEFS += -DHAS_M8502=0
endif

CPU=$(strip $(findstring M4510@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6502
CPUDEFS += -DHAS_M4510=1
CPUOBJS += $(OBJ)/cpu/m6502/m4510.o
DBGOBJS += $(OBJ)/cpu/m6502/6502dasm.o
else
CPUDEFS += -DHAS_M4510=0
endif

CPU=$(strip $(findstring N2A03@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6502
CPUDEFS += -DHAS_N2A03=1
CPUOBJS += $(OBJ)/cpu/m6502/m6502.o
DBGOBJS += $(OBJ)/cpu/m6502/6502dasm.o
$(OBJ)/cpu/m6502/m6502.o: m6502.c m6502.h ops02.h t6502.c t65c02.c t65sc02.c t6510.c
else
CPUDEFS += -DHAS_N2A03=0
endif

CPU=$(strip $(findstring H6280@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/h6280
CPUDEFS += -DHAS_H6280=1
CPUOBJS += $(OBJ)/cpu/h6280/h6280.o
DBGOBJS += $(OBJ)/cpu/h6280/6280dasm.o
$(OBJ)/cpu/h6280/h6280.o: h6280.c h6280.h h6280ops.h tblh6280.c
else
CPUDEFS += -DHAS_H6280=0
endif

CPU=$(strip $(findstring I86@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/i86
CPUDEFS += -DHAS_I86=1
CPUOBJS += $(OBJ)/cpu/i86/i86.o
DBGOBJS += $(OBJ)/cpu/i86/i86dasm.o
$(OBJ)/cpu/i86/i86.o: i86.c instr86.c i86.h i86intf.h ea.h host.h modrm.h
else
CPUDEFS += -DHAS_I86=0
endif

CPU=$(strip $(findstring I88@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/i86
CPUDEFS += -DHAS_I88=1
CPUOBJS += $(OBJ)/cpu/i86/i86.o
DBGOBJS += $(OBJ)/cpu/i86/i86dasm.o
$(OBJ)/cpu/i86/i86.o: i86.c instr86.c i86.h i86intf.h ea.h host.h modrm.h
else
CPUDEFS += -DHAS_I88=0
endif

CPU=$(strip $(findstring I186@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/i86
CPUDEFS += -DHAS_I186=1
CPUOBJS += $(OBJ)/cpu/i86/i86.o
DBGOBJS += $(OBJ)/cpu/i86/i86dasm.o
$(OBJ)/cpu/i86/i86.o: i86.c instr186.c i86.h i186intf.h ea.h host.h modrm.h
else
CPUDEFS += -DHAS_I186=0
endif

CPU=$(strip $(findstring I188@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/i86
CPUDEFS += -DHAS_I188=1
CPUOBJS += $(OBJ)/cpu/i86/i86.o
DBGOBJS += $(OBJ)/cpu/i86/i86dasm.o
$(OBJ)/cpu/i86/i86.o: i86.c instr186.c i86.h i186intf.h ea.h host.h modrm.h
else
CPUDEFS += -DHAS_I188=0
endif

CPU=$(strip $(findstring I286@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/i86
CPUDEFS += -DHAS_I286=1
CPUOBJS += $(OBJ)/cpu/i86/i286.o
DBGOBJS += $(OBJ)/cpu/i86/i86dasm.o
$(OBJ)/cpu/i86/i86.o: i86.c instr286.c i86.h i286intf.h ea.h host.h modrm.h
else
CPUDEFS += -DHAS_I286=0
endif

CPU=$(strip $(findstring V20@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/nec
CPUDEFS += -DHAS_V20=1
CPUOBJS += $(OBJ)/cpu/nec/nec.o
DBGOBJS += $(OBJ)/cpu/nec/necdasm.o
$(OBJ)/cpu/nec/nec.o: nec.c nec.h necintrf.h necea.h nechost.h necinstr.h necmodrm.h
else
CPUDEFS += -DHAS_V20=0
endif

CPU=$(strip $(findstring V30@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/nec
CPUDEFS += -DHAS_V30=1
CPUOBJS += $(OBJ)/cpu/nec/nec.o
DBGOBJS += $(OBJ)/cpu/nec/necdasm.o
$(OBJ)/cpu/nec/nec.o: nec.c nec.h necintrf.h necea.h nechost.h necinstr.h necmodrm.h
else
CPUDEFS += -DHAS_V30=0
endif

CPU=$(strip $(findstring V33@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/nec
CPUDEFS += -DHAS_V33=1
CPUOBJS += $(OBJ)/cpu/nec/nec.o
DBGOBJS += $(OBJ)/cpu/nec/necdasm.o
$(OBJ)/cpu/nec/nec.o: nec.c nec.h necintrf.h necea.h nechost.h necinstr.h necmodrm.h
else
CPUDEFS += -DHAS_V33=0
endif

CPU=$(strip $(findstring I8035@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/i8039
CPUDEFS += -DHAS_I8035=1
CPUOBJS += $(OBJ)/cpu/i8039/i8039.o
DBGOBJS += $(OBJ)/cpu/i8039/8039dasm.o
$(OBJ)/cpu/i8039/i8039.o: i8039.c i8039.h
else
CPUDEFS += -DHAS_I8035=0
endif

CPU=$(strip $(findstring I8039@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/i8039
CPUDEFS += -DHAS_I8039=1
CPUOBJS += $(OBJ)/cpu/i8039/i8039.o
DBGOBJS += $(OBJ)/cpu/i8039/8039dasm.o
$(OBJ)/cpu/i8039/i8039.o: i8039.c i8039.h
else
CPUDEFS += -DHAS_I8039=0
endif

CPU=$(strip $(findstring I8048@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/i8039
CPUDEFS += -DHAS_I8048=1
CPUOBJS += $(OBJ)/cpu/i8039/i8039.o
DBGOBJS += $(OBJ)/cpu/i8039/8039dasm.o
$(OBJ)/cpu/i8039/i8039.o: i8039.c i8039.h
else
CPUDEFS += -DHAS_I8048=0
endif

CPU=$(strip $(findstring N7751@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/i8039
CPUDEFS += -DHAS_N7751=1
CPUOBJS += $(OBJ)/cpu/i8039/i8039.o
DBGOBJS += $(OBJ)/cpu/i8039/8039dasm.o
$(OBJ)/cpu/i8039/i8039.o: i8039.c i8039.h
else
CPUDEFS += -DHAS_N7751=0
endif

CPU=$(strip $(findstring M6800@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6800
CPUDEFS += -DHAS_M6800=1
CPUOBJS += $(OBJ)/cpu/m6800/m6800.o
DBGOBJS += $(OBJ)/cpu/m6800/6800dasm.o
$(OBJ)/cpu/m6800/m6800.o: m6800.c m6800.h 6800ops.c 6800tbl.c
else
CPUDEFS += -DHAS_M6800=0
endif

CPU=$(strip $(findstring M6801@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6800
CPUDEFS += -DHAS_M6801=1
CPUOBJS += $(OBJ)/cpu/m6800/m6800.o
DBGOBJS += $(OBJ)/cpu/m6800/6800dasm.o
$(OBJ)/cpu/m6800/m6800.o: m6800.c m6800.h 6800ops.c 6800tbl.c
else
CPUDEFS += -DHAS_M6801=0
endif

CPU=$(strip $(findstring M6802@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6800
CPUDEFS += -DHAS_M6802=1
CPUOBJS += $(OBJ)/cpu/m6800/m6800.o
DBGOBJS += $(OBJ)/cpu/m6800/6800dasm.o
$(OBJ)/cpu/m6800/m6800.o: m6800.c m6800.h 6800ops.c 6800tbl.c
else
CPUDEFS += -DHAS_M6802=0
endif

CPU=$(strip $(findstring M6803@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6800
CPUDEFS += -DHAS_M6803=1
CPUOBJS += $(OBJ)/cpu/m6800/m6800.o
DBGOBJS += $(OBJ)/cpu/m6800/6800dasm.o
$(OBJ)/cpu/m6800/m6800.o: m6800.c m6800.h 6800ops.c 6800tbl.c
else
CPUDEFS += -DHAS_M6803=0
endif

CPU=$(strip $(findstring M6808@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6800
CPUDEFS += -DHAS_M6808=1
CPUOBJS += $(OBJ)/cpu/m6800/m6800.o
DBGOBJS += $(OBJ)/cpu/m6800/6800dasm.o
$(OBJ)/cpu/m6800/m6800.o: m6800.c m6800.h 6800ops.c 6800tbl.c
else
CPUDEFS += -DHAS_M6808=0
endif

CPU=$(strip $(findstring HD63701@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6800
CPUDEFS += -DHAS_HD63701=1
CPUOBJS += $(OBJ)/cpu/m6800/m6800.o
DBGOBJS += $(OBJ)/cpu/m6800/6800dasm.o
$(OBJ)/cpu/m6800/m6800.o: m6800.c m6800.h 6800ops.c 6800tbl.c
else
CPUDEFS += -DHAS_HD63701=0
endif

CPU=$(strip $(findstring NSC8105@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6800
CPUDEFS += -DHAS_NSC8105=1
CPUOBJS += $(OBJ)/cpu/m6800/m6800.o
DBGOBJS += $(OBJ)/cpu/m6800/6800dasm.o
$(OBJ)/cpu/m6800/m6800.o: m6800.c m6800.h 6800ops.c 6800tbl.c
else
CPUDEFS += -DHAS_NSC8105=0
endif

CPU=$(strip $(findstring M6805@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6805
CPUDEFS += -DHAS_M6805=1
CPUOBJS += $(OBJ)/cpu/m6805/m6805.o
DBGOBJS += $(OBJ)/cpu/m6805/6805dasm.o
$(OBJ)/cpu/m6805/m6805.o: m6805.c m6805.h 6805ops.c
else
CPUDEFS += -DHAS_M6805=0
endif

CPU=$(strip $(findstring M68705@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6805
CPUDEFS += -DHAS_M68705=1
CPUOBJS += $(OBJ)/cpu/m6805/m6805.o
DBGOBJS += $(OBJ)/cpu/m6805/6805dasm.o
$(OBJ)/cpu/m6805/m6805.o: m6805.c m6805.h 6805ops.c
else
CPUDEFS += -DHAS_M68705=0
endif

CPU=$(strip $(findstring HD63705@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6805
CPUDEFS += -DHAS_HD63705=1
CPUOBJS += $(OBJ)/cpu/m6805/m6805.o
DBGOBJS += $(OBJ)/cpu/m6805/6805dasm.o
$(OBJ)/cpu/m6805/m6805.o: m6805.c m6805.h 6805ops.c
else
CPUDEFS += -DHAS_HD63705=0
endif

CPU=$(strip $(findstring HD6309@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/hd6309
CPUDEFS += -DHAS_HD6309=1
CPUOBJS += $(OBJ)/cpu/hd6309/hd6309.o
DBGOBJS += $(OBJ)/cpu/hd6309/6309dasm.o
$(OBJ)/cpu/hd6309/hd6309.o: hd6309.c hd6309.h 6309ops.c 6309tbl.c
else
CPUDEFS += -DHAS_HD6309=0
endif

CPU=$(strip $(findstring M6809@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m6809
CPUDEFS += -DHAS_M6809=1
CPUOBJS += $(OBJ)/cpu/m6809/m6809.o
DBGOBJS += $(OBJ)/cpu/m6809/6809dasm.o
$(OBJ)/cpu/m6809/m6809.o: m6809.c m6809.h 6809ops.c 6809tbl.c
else
CPUDEFS += -DHAS_M6809=0
endif

CPU=$(strip $(findstring KONAMI@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/konami
CPUDEFS += -DHAS_KONAMI=1
CPUOBJS += $(OBJ)/cpu/konami/konami.o
DBGOBJS += $(OBJ)/cpu/konami/knmidasm.o
$(OBJ)/cpu/konami/konami.o: konami.c konami.h konamops.c konamtbl.c
else
CPUDEFS += -DHAS_KONAMI=0
endif

CPU=$(strip $(findstring M68000@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m68000
CPUDEFS += -DHAS_M68000=1
ifdef X86_ASM_68K
CPUOBJS += $(OBJ)/cpu/m68000/asmintf.o $(OBJ)/cpu/m68000/68kem.o
ASMDEFS += -DA68KEM
$(OBJ)/cpu/m68000/68kem.o: $(OBJ)/cpu/m68000/68kem.asm make68k.c
else
M68000_GENERATED_OBJS = \
$(OBJ)/cpu/m68000/m68kops.o $(OBJ)/cpu/m68000/m68kopac.o \
$(OBJ)/cpu/m68000/m68kopdm.o $(OBJ)/cpu/m68000/m68kopnz.o
CPUOBJS += $(M68000_GENERATED_OBJS) $(OBJ)/cpu/m68000/m68kcpu.o $(OBJ)/cpu/m68000/m68kmame.o
endif
DBGOBJS += $(OBJ)/cpu/m68000/m68kdasm.o
else
CPUDEFS += -DHAS_M68000=0
endif

CPU=$(strip $(findstring M68010@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m68000
CPUDEFS += -DHAS_M68010=1
ifdef X86_ASM_68K
CPUOBJS += $(OBJ)/cpu/m68000/asmintf.o $(OBJ)/cpu/m68000/68kem.o
ASMDEFS += -DA68KEM
$(OBJ)/cpu/m68000/68kem.o: $(OBJ)/cpu/m68000/68kem.asm make68k.c
else
M68000_GENERATED_OBJS = \
$(OBJ)/cpu/m68000/m68kops.o $(OBJ)/cpu/m68000/m68kopac.o \
$(OBJ)/cpu/m68000/m68kopdm.o $(OBJ)/cpu/m68000/m68kopnz.o
CPUOBJS += $(M68000_GENERATED_OBJS) $(OBJ)/cpu/m68000/m68kcpu.o $(OBJ)/cpu/m68000/m68kmame.o
endif
DBGOBJS += $(OBJ)/cpu/m68000/m68kdasm.o
else
CPUDEFS += -DHAS_M68010=0
endif

CPU=$(strip $(findstring M68EC020@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m68000
CPUDEFS += -DHAS_M68EC020=1
ifdef X86_ASM_68K
CPUOBJS += $(OBJ)/cpu/m68000/asmintf.o $(OBJ)/cpu/m68000/68kem.o
ASMDEFS += -DA68KEM
$(OBJ)/cpu/m68000/68kem.o: $(OBJ)/cpu/m68000/68kem.asm make68k.c
else
M68000_GENERATED_OBJS = \
$(OBJ)/cpu/m68000/m68kops.o $(OBJ)/cpu/m68000/m68kopac.o \
$(OBJ)/cpu/m68000/m68kopdm.o $(OBJ)/cpu/m68000/m68kopnz.o
CPUOBJS += $(M68000_GENERATED_OBJS) $(OBJ)/cpu/m68000/m68kcpu.o $(OBJ)/cpu/m68000/m68kmame.o
endif
DBGOBJS += $(OBJ)/cpu/m68000/m68kdasm.o
else
CPUDEFS += -DHAS_M68EC020=0
endif

CPU=$(strip $(findstring M68020@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/m68000
CPUDEFS += -DHAS_M68020=1
ifdef X86_ASM_68K
CPUOBJS += $(OBJ)/cpu/m68000/asmintf.o $(OBJ)/cpu/m68000/68kem.o
ASMDEFS += -DA68KEM
$(OBJ)/cpu/m68000/68kem.o: $(OBJ)/cpu/m68000/68kem.asm make68k.c
else
M68000_GENERATED_OBJS = \
$(OBJ)/cpu/m68000/m68kops.o $(OBJ)/cpu/m68000/m68kopac.o \
$(OBJ)/cpu/m68000/m68kopdm.o $(OBJ)/cpu/m68000/m68kopnz.o
CPUOBJS += $(M68000_GENERATED_OBJS) $(OBJ)/cpu/m68000/m68kcpu.o $(OBJ)/cpu/m68000/m68kmame.o
endif
DBGOBJS += $(OBJ)/cpu/m68000/m68kdasm.o
else
CPUDEFS += -DHAS_M68020=0
endif

CPU=$(strip $(findstring T11@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/t11
CPUDEFS += -DHAS_T11=1
CPUOBJS += $(OBJ)/cpu/t11/t11.o
DBGOBJS += $(OBJ)/cpu/t11/t11dasm.o
$(OBJ)/cpu/t11/t11.o: t11.c t11.h t11ops.c t11table.c
else
CPUDEFS += -DHAS_T11=0
endif

CPU=$(strip $(findstring S2650@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/s2650
CPUDEFS += -DHAS_S2650=1
CPUOBJS += $(OBJ)/cpu/s2650/s2650.o
DBGOBJS += $(OBJ)/cpu/s2650/2650dasm.o
$(OBJ)/cpu/s2650/s2650.o: s2650.c s2650.h s2650cpu.h
else
CPUDEFS += -DHAS_S2650=0
endif

CPU=$(strip $(findstring F8@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/f8
CPUDEFS += -DHAS_F8=1
CPUOBJS += $(OBJ)/cpu/f8/f8.o
DBGOBJS += $(OBJ)/cpu/f8/f8dasm.o
$(OBJ)/cpu/f8/f8.o: f8.c f8.h
else
CPUDEFS += -DHAS_F8=0
endif

CPU=$(strip $(findstring CP1600@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/cp1600
CPUDEFS += -DHAS_CP1600=1
CPUOBJS += $(OBJ)/cpu/cp1600/cp1600.o
DBGOBJS += $(OBJ)/cpu/cp1600/1600dasm.o
$(OBJ)/cpu/cp1600/cp1600.o: cp1600.c cp1600.h
else
CPUDEFS += -DHAS_CP1600=0
endif

CPU=$(strip $(findstring TMS34010@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/tms34010
CPUDEFS += -DHAS_TMS34010=1
CPUOBJS += $(OBJ)/cpu/tms34010/tms34010.o $(OBJ)/cpu/tms34010/34010fld.o
DBGOBJS += $(OBJ)/cpu/tms34010/34010dsm.o
$(OBJ)/cpu/tms34010/tms34010.o: tms34010.c tms34010.h 34010ops.c 34010tbl.c
else
CPUDEFS += -DHAS_TMS34010=0
endif

CPU=$(strip $(findstring TMS9900@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/tms9900
CPUDEFS += -DHAS_TMS9900=1
CPUOBJS += $(OBJ)/cpu/tms9900/tms9900.o
DBGOBJS += $(OBJ)/cpu/tms9900/9900dasm.o
$(OBJ)/cpu/tms9900/tms9900.o: tms9900.c tms9900.h 99xxcore.h 9900stat.h
else
CPUDEFS += -DHAS_TMS9900=0
endif

CPU=$(strip $(findstring TMS9940@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/tms9900
CPUDEFS += -DHAS_TMS9940=1
CPUOBJS += $(OBJ)/cpu/tms9900/tms9900.o
DBGOBJS += $(OBJ)/cpu/tms9900/9900dasm.o
$(OBJ)/cpu/tms9900/tms9900.o: tms9900.c tms9900.h 99xxcore.h 9900stat.h
else
CPUDEFS += -DHAS_TMS9940=0
endif

CPU=$(strip $(findstring TMS9980@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/tms9900
CPUDEFS += -DHAS_TMS9980=1
CPUOBJS += $(OBJ)/cpu/tms9900/tms9980a.o
DBGOBJS += $(OBJ)/cpu/tms9900/9900dasm.o
$(OBJ)/cpu/tms9900/tms9980a.o: tms9980a.c tms9900.h 99xxcore.h 99xxstat.h
else
CPUDEFS += -DHAS_TMS9980=0
endif

CPU=$(strip $(findstring TMS9985@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/tms9900
CPUDEFS += -DHAS_TMS9985=1
CPUOBJS += $(OBJ)/cpu/tms9900/tms9980a.o
DBGOBJS += $(OBJ)/cpu/tms9900/9900dasm.o
$(OBJ)/cpu/tms9900/tms9980a.o: tms9980a.c tms9900.h 99xxcore.h 99xxstat.h
else
CPUDEFS += -DHAS_TMS9985=0
endif

CPU=$(strip $(findstring TMS9989@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/tms9900
CPUDEFS += -DHAS_TMS9989=1
CPUOBJS += $(OBJ)/cpu/tms9900/tms9980a.o
DBGOBJS += $(OBJ)/cpu/tms9900/9900dasm.o
$(OBJ)/cpu/tms9900/tms9980a.o: tms9980a.c tms9900.h 99xxcore.h 99xxstat.h
else
CPUDEFS += -DHAS_TMS9989=0
endif

CPU=$(strip $(findstring TMS9995@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/tms9900
CPUDEFS += -DHAS_TMS9995=1
CPUOBJS += $(OBJ)/cpu/tms9900/tms9995.o
DBGOBJS += $(OBJ)/cpu/tms9900/9900dasm.o
$(OBJ)/cpu/tms9900/tms9995.o: tms9995.c tms9900.h 99xxcore.h 99xxstat.h
else
CPUDEFS += -DHAS_TMS9995=0
endif

CPU=$(strip $(findstring TMS99105A@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/tms9900
CPUDEFS += -DHAS_TMS99105A=1
CPUOBJS += $(OBJ)/cpu/tms9900/tms9995.o
DBGOBJS += $(OBJ)/cpu/tms9900/9900dasm.o
$(OBJ)/cpu/tms9900/tms9995.o: tms9995.c tms9900.h 99xxcore.h 99xxstat.h
else
CPUDEFS += -DHAS_TMS99105A=0
endif

CPU=$(strip $(findstring Z8000@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/z8000
CPUDEFS += -DHAS_Z8000=1
CPUOBJS += $(OBJ)/cpu/z8000/z8000.o
DBGOBJS += $(OBJ)/cpu/z8000/8000dasm.o
$(OBJ)/cpu/z8000/z8000.o: z8000.c z8000.h z8000cpu.h z8000dab.h z8000ops.c z8000tbl.c
else
CPUDEFS += -DHAS_Z8000=0
endif

CPU=$(strip $(findstring TMS320C10@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/tms32010
CPUDEFS += -DHAS_TMS320C10=1
CPUOBJS += $(OBJ)/cpu/tms32010/tms32010.o
DBGOBJS += $(OBJ)/cpu/tms32010/32010dsm.o
$(OBJ)/cpu/tms32010/tms32010.o: tms32010.c tms32010.h
else
CPUDEFS += -DHAS_TMS320C10=0
endif

CPU=$(strip $(findstring CCPU@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/ccpu
CPUDEFS += -DHAS_CCPU=1
CPUOBJS += $(OBJ)/cpu/ccpu/ccpu.o $(OBJ)/vidhrdw/cinemat.o
DBGOBJS += $(OBJ)/cpu/ccpu/ccpudasm.o
$(OBJ)/cpu/ccpu/ccpu.o: ccpu.c ccpu.h ccputabl.c
else
CPUDEFS += -DHAS_CCPU=0
endif

CPU=$(strip $(findstring ADSP2100@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/adsp2100
CPUDEFS += -DHAS_ADSP2100=1
CPUOBJS += $(OBJ)/cpu/adsp2100/adsp2100.o
DBGOBJS += $(OBJ)/cpu/adsp2100/2100dasm.o
$(OBJ)/cpu/adsp2100/adsp2100.o: adsp2100.c adsp2100.h 2100ops.c
else
CPUDEFS += -DHAS_ADSP2100=0
endif

CPU=$(strip $(findstring ADSP2105@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/adsp2100
CPUDEFS += -DHAS_ADSP2105=1
CPUOBJS += $(OBJ)/cpu/adsp2100/adsp2100.o
DBGOBJS += $(OBJ)/cpu/adsp2100/2100dasm.o
$(OBJ)/cpu/adsp2100/adsp2100.o: adsp2100.c adsp2100.h 2100ops.c
else
CPUDEFS += -DHAS_ADSP2105=0
endif

CPU=$(strip $(findstring PDP1@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/pdp1
CPUDEFS += -DHAS_PDP1=1
CPUOBJS += $(OBJ)/cpu/pdp1/pdp1.o
DBGOBJS += $(OBJ)/cpu/pdp1/pdp1dasm.o
$(OBJ)/cpu/pdp1/pdp1.o: pdp1.c pdp1.h
else
CPUDEFS += -DHAS_PDP1=0
endif

CPU=$(strip $(findstring PSXCPU@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/mips
CPUDEFS += -DHAS_PSXCPU=1
CPUOBJS += $(OBJ)/cpu/mips/mips.o
DBGOBJS += $(OBJ)/cpu/mips/mipsdasm.o
$(OBJ)/cpu/mips/mips.o: mips.c mips.h
else
CPUDEFS += -DHAS_PSXCPU=0
endif

CPU=$(strip $(findstring SC61860@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/sc61860
CPUDEFS += -DHAS_SC61860=1
CPUOBJS += $(OBJ)/cpu/sc61860/sc61860.o
DBGOBJS += $(OBJ)/cpu/sc61860/disasm.o
$(OBJ)/cpu/sc61860/sc61860.o: src/cpu/sc61860/sc61860.h \
	src/cpu/sc61860/sc.h src/cpu/sc61860/ops.c src/cpu/sc61860/table.c
else
CPUDEFS += -DHAS_SC61860=0
endif

CPU=$(strip $(findstring ARM@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/arm
CPUDEFS += -DHAS_ARM=1
CPUOBJS += $(OBJ)/cpu/arm/arm.o
DBGOBJS += $(OBJ)/cpu/arm/dasm.o
$(OBJ)/cpu/arm/arm.o: src/cpu/arm/arm.h
else
CPUDEFS += -DHAS_ARM=0
endif

CPU=$(strip $(findstring G65816@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/g65816
CPUDEFS += -DHAS_G65816=1
CPUOBJS += $(OBJ)/cpu/g65816/g65816.o
CPUOBJS += $(OBJ)/cpu/g65816/g65816o0.o
CPUOBJS += $(OBJ)/cpu/g65816/g65816o1.o
CPUOBJS += $(OBJ)/cpu/g65816/g65816o2.o
CPUOBJS += $(OBJ)/cpu/g65816/g65816o3.o
CPUOBJS += $(OBJ)/cpu/g65816/g65816o4.o
DBGOBJS += $(OBJ)/cpu/g65816/g65816ds.o
$(OBJ)/cpu/g65816/g65816.o: g65816.c g65816.h g65816cm.h g65816op.h
$(OBJ)/cpu/g65816/g65816o0.o: g65816o0.c g65816.h g65816cm.h g65816op.h
$(OBJ)/cpu/g65816/g65816o1.o: g65816o0.c g65816.h g65816cm.h g65816op.h
$(OBJ)/cpu/g65816/g65816o2.o: g65816o0.c g65816.h g65816cm.h g65816op.h
$(OBJ)/cpu/g65816/g65816o3.o: g65816o0.c g65816.h g65816cm.h g65816op.h
$(OBJ)/cpu/g65816/g65816o4.o: g65816o0.c g65816.h g65816cm.h g65816op.h
else
CPUDEFS += -DHAS_G65816=0
endif

CPU=$(strip $(findstring SPC700@,$(CPUS)))
ifneq ($(CPU),)
OBJDIRS += $(OBJ)/cpu/spc700
CPUDEFS += -DHAS_SPC700=1
CPUOBJS += $(OBJ)/cpu/spc700/spc700.o
DBGOBJS += $(OBJ)/cpu/spc700/spc700ds.o
$(OBJ)/cpu/spc700/spc700.o: spc700.c spc700.h
else
CPUDEFS += -DHAS_SPC700=0
endif


SOUND=$(strip $(findstring CUSTOM@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_CUSTOM=1
else
SOUNDDEFS += -DHAS_CUSTOM=0
endif

SOUND=$(strip $(findstring SAMPLES@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_SAMPLES=1
SOUNDOBJS += $(OBJ)/sound/samples.o
else
SOUNDDEFS += -DHAS_SAMPLES=0
endif

SOUND=$(strip $(findstring DAC@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_DAC=1
SOUNDOBJS += $(OBJ)/sound/dac.o
else
SOUNDDEFS += -DHAS_DAC=0
endif

SOUND=$(strip $(findstring AY8910@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_AY8910=1
SOUNDOBJS += $(OBJ)/sound/ay8910.o
else
SOUNDDEFS += -DHAS_AY8910=0
endif

SOUND=$(strip $(findstring YM2203@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM2203=1
SOUNDOBJS += $(OBJ)/sound/2203intf.o $(OBJ)/sound/ay8910.o $(OBJ)/sound/fm.o
else
SOUNDDEFS += -DHAS_YM2203=0
endif

SOUND=$(strip $(findstring YM2151@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM2151=1
SOUNDOBJS += $(OBJ)/sound/2151intf.o $(OBJ)/sound/ym2151.o $(OBJ)/sound/fm.o
else
SOUNDDEFS += -DHAS_YM2151=0
endif

SOUND=$(strip $(findstring YM2151_ALT@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM2151_ALT=1
SOUNDOBJS += $(OBJ)/sound/2151intf.o $(OBJ)/sound/ym2151.o $(OBJ)/sound/fm.o
else
SOUNDDEFS += -DHAS_YM2151_ALT=0
endif

SOUND=$(strip $(findstring YM2608@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM2608=1
SOUNDOBJS += $(OBJ)/sound/2608intf.o $(OBJ)/sound/ay8910.o $(OBJ)/sound/fm.o $(OBJ)/sound/ymdeltat.o
else
SOUNDDEFS += -DHAS_YM2608=0
endif

SOUND=$(strip $(findstring YM2610@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM2610=1
SOUNDOBJS += $(OBJ)/sound/2610intf.o $(OBJ)/sound/ay8910.o $(OBJ)/sound/fm.o $(OBJ)/sound/ymdeltat.o
else
SOUNDDEFS += -DHAS_YM2610=0
endif

SOUND=$(strip $(findstring YM2610B@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM2610B=1
SOUNDOBJS += $(OBJ)/sound/2610intf.o $(OBJ)/sound/ay8910.o $(OBJ)/sound/fm.o $(OBJ)/sound/ymdeltat.o
else
SOUNDDEFS += -DHAS_YM2610B=0
endif

SOUND=$(strip $(findstring YM2612@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM2612=1
SOUNDOBJS += $(OBJ)/sound/2612intf.o $(OBJ)/sound/ay8910.o $(OBJ)/sound/fm.o
else
SOUNDDEFS += -DHAS_YM2612=0
endif

SOUND=$(strip $(findstring YM3438@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM3438=1
SOUNDOBJS += $(OBJ)/sound/2612intf.o $(OBJ)/sound/ay8910.o $(OBJ)/sound/fm.o
else
SOUNDDEFS += -DHAS_YM3438=0
endif

SOUND=$(strip $(findstring YM2413@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM2413=1
SOUNDOBJS += $(OBJ)/sound/3812intf.o $(OBJ)/sound/ym2413.o $(OBJ)/sound/fmopl.o
else
SOUNDDEFS += -DHAS_YM2413=0
endif

SOUND=$(strip $(findstring YM3812@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM3812=1
SOUNDOBJS += $(OBJ)/sound/3812intf.o $(OBJ)/sound/fmopl.o
else
SOUNDDEFS += -DHAS_YM3812=0
endif

SOUND=$(strip $(findstring YM3526@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM3526=1
SOUNDOBJS += $(OBJ)/sound/3812intf.o $(OBJ)/sound/fmopl.o
else
SOUNDDEFS += -DHAS_YM3526=0
endif

SOUND=$(strip $(findstring YMZ280B@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YMZ280B=1
SOUNDOBJS += $(OBJ)/sound/ymz280b.o
else
SOUNDDEFS += -DHAS_YMZ280B=0
endif

SOUND=$(strip $(findstring Y8950@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_Y8950=1
SOUNDOBJS += $(OBJ)/sound/3812intf.o $(OBJ)/sound/fmopl.o $(OBJ)/sound/ymdeltat.o
else
SOUNDDEFS += -DHAS_Y8950=0
endif

SOUND=$(strip $(findstring SN76477@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_SN76477=1
SOUNDOBJS += $(OBJ)/sound/sn76477.o
else
SOUNDDEFS += -DHAS_SN76477=0
endif

SOUND=$(strip $(findstring SN76496@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_SN76496=1
SOUNDOBJS += $(OBJ)/sound/sn76496.o
else
SOUNDDEFS += -DHAS_SN76496=0
endif

SOUND=$(strip $(findstring POKEY@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_POKEY=1
SOUNDOBJS += $(OBJ)/sound/pokey.o
else
SOUNDDEFS += -DHAS_POKEY=0
endif

SOUND=$(strip $(findstring TIA@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_TIA=1
SOUNDOBJS += $(OBJ)/sound/tiasound.o $(OBJ)/sound/tiaintf.o
else
SOUNDDEFS += -DHAS_TIA=0
endif

SOUND=$(strip $(findstring NES@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_NES=1
SOUNDOBJS += $(OBJ)/sound/nes_apu.o
else
SOUNDDEFS += -DHAS_NES=0
endif

SOUND=$(strip $(findstring ASTROCADE@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_ASTROCADE=1
SOUNDOBJS += $(OBJ)/sound/astrocde.o
else
SOUNDDEFS += -DHAS_ASTROCADE=0
endif

SOUND=$(strip $(findstring NAMCO@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_NAMCO=1
SOUNDOBJS += $(OBJ)/sound/namco.o
else
SOUNDDEFS += -DHAS_NAMCO=0
endif

SOUND=$(strip $(findstring TMS36XX@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_TMS36XX=1
SOUNDOBJS += $(OBJ)/sound/tms36xx.o
else
SOUNDDEFS += -DHAS_TMS36XX=0
endif

SOUND=$(strip $(findstring TMS5110@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_TMS5110=1
SOUNDOBJS += $(OBJ)/sound/tms5110.o $(OBJ)/sound/5110intf.o
else
SOUNDDEFS += -DHAS_TMS5110=0
endif

SOUND=$(strip $(findstring TMS5220@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_TMS5220=1
SOUNDOBJS += $(OBJ)/sound/tms5220.o $(OBJ)/sound/5220intf.o
else
SOUNDDEFS += -DHAS_TMS5220=0
endif

SOUND=$(strip $(findstring VLM5030@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_VLM5030=1
SOUNDOBJS += $(OBJ)/sound/vlm5030.o
else
SOUNDDEFS += -DHAS_VLM5030=0
endif

SOUND=$(strip $(findstring ADPCM@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_ADPCM=1
SOUNDOBJS += $(OBJ)/sound/adpcm.o
else
SOUNDDEFS += -DHAS_ADPCM=0
endif

SOUND=$(strip $(findstring OKIM6295@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_OKIM6295=1
SOUNDOBJS += $(OBJ)/sound/adpcm.o
else
SOUNDDEFS += -DHAS_OKIM6295=0
endif

SOUND=$(strip $(findstring MSM5205@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_MSM5205=1
SOUNDOBJS += $(OBJ)/sound/msm5205.o
else
SOUNDDEFS += -DHAS_MSM5205=0
endif

SOUND=$(strip $(findstring UPD7759@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_UPD7759=1
SOUNDOBJS += $(OBJ)/sound/upd7759.o
else
SOUNDDEFS += -DHAS_UPD7759=0
endif

SOUND=$(strip $(findstring HC55516@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_HC55516=1
SOUNDOBJS += $(OBJ)/sound/hc55516.o
else
SOUNDDEFS += -DHAS_HC55516=0
endif

SOUND=$(strip $(findstring K005289@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_K005289=1
SOUNDOBJS += $(OBJ)/sound/k005289.o
else
SOUNDDEFS += -DHAS_K005289=0
endif

SOUND=$(strip $(findstring K007232@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_K007232=1
SOUNDOBJS += $(OBJ)/sound/k007232.o
else
SOUNDDEFS += -DHAS_K007232=0
endif

SOUND=$(strip $(findstring K051649@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_K051649=1
SOUNDOBJS += $(OBJ)/sound/k051649.o
else
SOUNDDEFS += -DHAS_K051649=0
endif

SOUND=$(strip $(findstring K053260@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_K053260=1
SOUNDOBJS += $(OBJ)/sound/k053260.o
else
SOUNDDEFS += -DHAS_K053260=0
endif

SOUND=$(strip $(findstring SEGAPCM@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_SEGAPCM=1
SOUNDOBJS += $(OBJ)/sound/segapcm.o
else
SOUNDDEFS += -DHAS_SEGAPCM=0
endif

SOUND=$(strip $(findstring RF5C68@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_RF5C68=1
SOUNDOBJS += $(OBJ)/sound/rf5c68.o
else
SOUNDDEFS += -DHAS_RF5C68=0
endif

SOUND=$(strip $(findstring CEM3394@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_CEM3394=1
SOUNDOBJS += $(OBJ)/sound/cem3394.o
else
SOUNDDEFS += -DHAS_CEM3394=0
endif

SOUND=$(strip $(findstring C140@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_C140=1
SOUNDOBJS += $(OBJ)/sound/c140.o
else
SOUNDDEFS += -DHAS_C140=0
endif

SOUND=$(strip $(findstring QSOUND@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_QSOUND=1
SOUNDOBJS += $(OBJ)/sound/qsound.o
else
SOUNDDEFS += -DHAS_QSOUND=0
endif

SOUND=$(strip $(findstring SPEAKER@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_SPEAKER=1
SOUNDOBJS += $(OBJ)/sound/speaker.o
else
SOUNDDEFS += -DHAS_SPEAKER=0
endif

SOUND=$(strip $(findstring WAVE@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_WAVE=1
SOUNDOBJS += $(OBJ)/sound/wave.o
else
SOUNDDEFS += -DHAS_WAVE=0
endif

SOUND=$(strip $(findstring BEEP@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_BEEP=1
SOUNDOBJS += $(OBJ)/sound/beep.o
else
SOUNDDEFS += -DHAS_BEEP=0
endif
