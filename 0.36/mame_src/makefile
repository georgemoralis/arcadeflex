AR = @ar
CC = @gcc
LD = @gcc
#ASM = @nasm
ASM = @nasmw
ASMFLAGS = -f coff
VPATH=src $(wildcard src/cpu/*)

ifdef K6
EMULATOR_EXE = mamek6.exe
ARCH = -march=k6
else
ifdef I686
EMULATOR_EXE = mameppro.exe
ARCH = -march=pentiumpro
else
EMULATOR_EXE = mame.exe
ARCH = -march=pentium
endif
endif

# uncomment next line to include the debugger
# DEBUG = 1

# uncomment next line to do a smaller compile including only one driver
# TINY_COMPILE = 1
TINY_NAME = driver_labyrunr
TINY_OBJS = obj/drivers/labyrunr.o obj/vidhrdw/labyrunr.o obj/vidhrdw/konamiic.o

# uncomment one of the two next lines to not compile the NeoGeo games or to
# compile only the NeoGeo games
# NEOFREE = 1
# NEOMAME = 1

# uncomment next line to include the symbols for symify
# SYMBOLS = 1

# uncomment next line to use Assembler 68k engine
X86_ASM_68K = 1

ifdef NEOMAME
CPUS+=Z80@
CPUS+=M68000@
SOUNDS+=YM2610@
else
# uncomment the following lines to include a CPU core
CPUS+=Z80@
#CPUS+=Z80GB@
CPUS+=8080@
CPUS+=8085A@
CPUS+=M6502@
CPUS+=M65C02@
#CPUS+=M65SC02@
#CPUS+=M65CE02@
#CPUS+=M6509@
CPUS+=M6510@
CPUS+=N2A03@
CPUS+=H6280@
CPUS+=I86@
CPUS+=V20@
CPUS+=V30@
CPUS+=V33@
CPUS+=I8035@
CPUS+=I8039@
CPUS+=I8048@
CPUS+=N7751@
CPUS+=M6800@
CPUS+=M6801@
CPUS+=M6802@
CPUS+=M6803@
CPUS+=M6808@
CPUS+=HD63701@
CPUS+=NSC8105@
CPUS+=M6805@
CPUS+=M68705@
CPUS+=HD63705@
CPUS+=HD6309@
CPUS+=M6809@
CPUS+=KONAMI@
CPUS+=M68000@
CPUS+=M68010@
CPUS+=M68EC020@
CPUS+=M68020@
CPUS+=T11@
CPUS+=S2650@
CPUS+=TMS34010@
#CPUS+=TMS9900@
#CPUS+=TMS9940@
CPUS+=TMS9980@
#CPUS+=TMS9985@
#CPUS+=TMS9989@
#CPUS+=TMS9995@
#CPUS+=TMS99105A@
#CPUS+=TMS99110A@
CPUS+=Z8000@
CPUS+=TMS320C10@
CPUS+=CCPU@
CPUS+=ADSP2100@
#CPUS+=PDP1@

# uncomment the following lines to include a sound core
SOUNDS+=CUSTOM@
SOUNDS+=SAMPLES@
SOUNDS+=DAC@
SOUNDS+=AY8910@
SOUNDS+=YM2203@
# enable only one of the following two
#SOUNDS+=YM2151@
SOUNDS+=YM2151_ALT@
SOUNDS+=YM2608@
SOUNDS+=YM2610@
SOUNDS+=YM2610B@
SOUNDS+=YM2612@
SOUNDS+=YM3438@
SOUNDS+=YM2413@
SOUNDS+=YM3812@
SOUNDS+=YM3526@
SOUNDS+=Y8950@
SOUNDS+=SN76477@
SOUNDS+=SN76496@
SOUNDS+=POKEY@
#SOUNDS+=TIA@
SOUNDS+=NES@
SOUNDS+=ASTROCADE@
SOUNDS+=NAMCO@
SOUNDS+=TMS36XX@
SOUNDS+=TMS5220@
SOUNDS+=VLM5030@
SOUNDS+=ADPCM@
SOUNDS+=OKIM6295@
SOUNDS+=MSM5205@
SOUNDS+=UPD7759@
SOUNDS+=HC55516@
SOUNDS+=K005289@
SOUNDS+=K007232@
SOUNDS+=K051649@
SOUNDS+=K053260@
SOUNDS+=SEGAPCM@
SOUNDS+=RF5C68@
SOUNDS+=CEM3394@
SOUNDS+=C140@
SOUNDS+=QSOUND@
#SOUNDS+=SPEAKER@
#SOUNDS+=WAVE@
endif


# check that the required libraries are available
ifeq ($(wildcard $(DJDIR)/lib/liballeg.a),)
noallegro:
	@echo Missing Allegro library! Get it from http://www.talula.demon.co.uk/allegro/
endif
ifeq ($(wildcard $(DJDIR)/lib/libaudio.a),)
noseal:
	@echo Missing SEAL library! Get it from http://www.egerter.com/
endif
ifeq ($(wildcard $(DJDIR)/lib/libz.a),)
nozlib:
	@echo Missing zlib library! Get it from http://www.cdrom.com/pub/infozip/zlib/
endif

#if obj subdirectory doesn't exist, create the tree before proceeding
ifeq ($(wildcard ./obj),)
noobj: maketree all
endif



# List of CPU core (and, for a debug build, disassembler) object files
CPUDEFS =
CPUOBJS =
DBGOBJS =
ASMDEFS =

CPU=$(strip $(findstring Z80@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_Z80=1
CPUOBJS += obj/cpu/z80/z80.o
DBGOBJS += obj/cpu/z80/z80dasm.o
endif

CPU=$(strip $(findstring Z80GB@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_Z80GB=1
CPUOBJS += obj/cpu/z80gb/z80gb.o
DBGOBJS += obj/cpu/z80gb/z80gbd.o
endif

CPU=$(strip $(findstring 8080@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_8080=1
CPUOBJS += obj/cpu/i8085/i8085.o
DBGOBJS += obj/cpu/i8085/8085dasm.o
endif

CPU=$(strip $(findstring 8085A@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_8085A=1
CPUOBJS += obj/cpu/i8085/i8085.o
DBGOBJS += obj/cpu/i8085/8085dasm.o
endif

CPU=$(strip $(findstring M6502@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_M6502=1
CPUOBJS += obj/cpu/m6502/m6502.o
DBGOBJS += obj/cpu/m6502/6502dasm.o
endif

CPU=$(strip $(findstring M65C02@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_M65C02=1
CPUOBJS += obj/cpu/m6502/m6502.o
DBGOBJS += obj/cpu/m6502/6502dasm.o
endif

CPU=$(strip $(findstring M65SC02@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_M65SC02=1
CPUOBJS += obj/cpu/m6502/m6502.o
DBGOBJS += obj/cpu/m6502/6502dasm.o
endif

CPU=$(strip $(findstring M65CE02@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_M65CE02=1
CPUOBJS += obj/cpu/m6502/m65ce02.o
DBGOBJS += obj/cpu/m6502/6502dasm.o
endif

CPU=$(strip $(findstring M6509@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_M6509=1
CPUOBJS += obj/cpu/m6502/m6509.o
DBGOBJS += obj/cpu/m6502/6502dasm.o
endif

CPU=$(strip $(findstring M6510@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_M6510=1
CPUOBJS += obj/cpu/m6502/m6502.o
DBGOBJS += obj/cpu/m6502/6502dasm.o
endif

CPU=$(strip $(findstring N2A03@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_N2A03=1
CPUOBJS += obj/cpu/m6502/m6502.o
DBGOBJS += obj/cpu/m6502/6502dasm.o
endif

CPU=$(strip $(findstring H6280@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_H6280=1
CPUOBJS += obj/cpu/h6280/h6280.o
DBGOBJS += obj/cpu/h6280/6280dasm.o
endif

CPU=$(strip $(findstring I86@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_I86=1
CPUOBJS += obj/cpu/i86/i86.o
DBGOBJS += obj/cpu/i86/i86dasm.o
endif

CPU=$(strip $(findstring V20@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_V20=1
CPUOBJS += obj/cpu/nec/nec.o
DBGOBJS += obj/cpu/nec/necdasm.o
endif

CPU=$(strip $(findstring V30@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_V30=1
CPUOBJS += obj/cpu/nec/nec.o
DBGOBJS += obj/cpu/nec/necdasm.o
endif

CPU=$(strip $(findstring V33@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_V33=1
CPUOBJS += obj/cpu/nec/nec.o
DBGOBJS += obj/cpu/nec/necdasm.o
endif

CPU=$(strip $(findstring I8035@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_I8035=1
CPUOBJS += obj/cpu/i8039/i8039.o
DBGOBJS += obj/cpu/i8039/8039dasm.o
endif

CPU=$(strip $(findstring I8039@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_I8039=1
CPUOBJS += obj/cpu/i8039/i8039.o
DBGOBJS += obj/cpu/i8039/8039dasm.o
endif

CPU=$(strip $(findstring I8048@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_I8048=1
CPUOBJS += obj/cpu/i8039/i8039.o
DBGOBJS += obj/cpu/i8039/8039dasm.o
endif

CPU=$(strip $(findstring N7751@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_N7751=1
CPUOBJS += obj/cpu/i8039/i8039.o
DBGOBJS += obj/cpu/i8039/8039dasm.o
endif

CPU=$(strip $(findstring M6800@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_M6800=1
CPUOBJS += obj/cpu/m6800/m6800.o
DBGOBJS += obj/cpu/m6800/6800dasm.o
endif

CPU=$(strip $(findstring M6801@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_M6801=1
CPUOBJS += obj/cpu/m6800/m6800.o
DBGOBJS += obj/cpu/m6800/6800dasm.o
endif

CPU=$(strip $(findstring M6802@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_M6802=1
CPUOBJS += obj/cpu/m6800/m6800.o
DBGOBJS += obj/cpu/m6800/6800dasm.o
endif

CPU=$(strip $(findstring M6803@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_M6803=1
CPUOBJS += obj/cpu/m6800/m6800.o
DBGOBJS += obj/cpu/m6800/6800dasm.o
endif

CPU=$(strip $(findstring M6808@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_M6808=1
CPUOBJS += obj/cpu/m6800/m6800.o
DBGOBJS += obj/cpu/m6800/6800dasm.o
endif

CPU=$(strip $(findstring HD63701@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_HD63701=1
CPUOBJS += obj/cpu/m6800/m6800.o
DBGOBJS += obj/cpu/m6800/6800dasm.o
endif

CPU=$(strip $(findstring NSC8105@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_NSC8105=1
CPUOBJS += obj/cpu/m6800/m6800.o
DBGOBJS += obj/cpu/m6800/6800dasm.o
endif

CPU=$(strip $(findstring M6805@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_M6805=1
CPUOBJS += obj/cpu/m6805/m6805.o
DBGOBJS += obj/cpu/m6805/6805dasm.o
endif

CPU=$(strip $(findstring M68705@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_M68705=1
CPUOBJS += obj/cpu/m6805/m6805.o
DBGOBJS += obj/cpu/m6805/6805dasm.o
endif

CPU=$(strip $(findstring HD63705@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_HD63705=1
CPUOBJS += obj/cpu/m6805/m6805.o
DBGOBJS += obj/cpu/m6805/6805dasm.o
endif

CPU=$(strip $(findstring HD6309@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_HD6309=1
CPUOBJS += obj/cpu/m6809/m6809.o
DBGOBJS += obj/cpu/m6809/6809dasm.o
endif

CPU=$(strip $(findstring M6809@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_M6809=1
CPUOBJS += obj/cpu/m6809/m6809.o
DBGOBJS += obj/cpu/m6809/6809dasm.o
endif

CPU=$(strip $(findstring KONAMI@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_KONAMI=1
CPUOBJS += obj/cpu/konami/konami.o
DBGOBJS += obj/cpu/konami/knmidasm.o
endif

CPU=$(strip $(findstring M68000@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_M68000=1
ifdef X86_ASM_68K
CPUOBJS += obj/cpu/m68000/asmintf.o obj/cpu/m68000/68kem.oa
ASMDEFS += -DA68KEM
else
CPUOBJS += obj/cpu/m68000/m68kops.og obj/cpu/m68000/m68kopac.og \
	obj/cpu/m68000/m68kopdm.og obj/cpu/m68000/m68kopnz.og \
	obj/cpu/m68000/m68kcpu.o obj/cpu/m68000/m68kmame.o
endif
DBGOBJS += obj/cpu/m68000/d68k.o
endif

CPU=$(strip $(findstring M68010@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_M68010=1
ifdef X86_ASM_68K
CPUOBJS += obj/cpu/m68000/asmintf.o obj/cpu/m68000/68kem.oa
ASMDEFS += -DA68KEM
else
CPUOBJS += obj/cpu/m68000/m68kops.og obj/cpu/m68000/m68kopac.og \
	obj/cpu/m68000/m68kopdm.og obj/cpu/m68000/m68kopnz.og \
	obj/cpu/m68000/m68kcpu.o obj/cpu/m68000/m68kmame.o
endif
DBGOBJS += obj/cpu/m68000/d68k.o
endif

CPU=$(strip $(findstring M68EC020@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_M68EC020=1
ifdef X86_ASM_68K
CPUOBJS += obj/cpu/m68000/asmintf.o obj/cpu/m68000/68kem.oa
ASMDEFS += -DA68KEM
else
CPUOBJS += obj/cpu/m68000/m68kops.og obj/cpu/m68000/m68kopac.og \
	obj/cpu/m68000/m68kopdm.og obj/cpu/m68000/m68kopnz.og \
	obj/cpu/m68000/m68kcpu.o obj/cpu/m68000/m68kmame.o
endif
DBGOBJS += obj/cpu/m68000/d68k.o
endif

CPU=$(strip $(findstring M68020@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_M68020=1
ifdef X86_ASM_68K
CPUOBJS += obj/cpu/m68000/asmintf.o obj/cpu/m68000/68kem.oa
ASMDEFS += -DA68KEM
else
CPUOBJS += obj/cpu/m68000/m68kops.og obj/cpu/m68000/m68kopac.og \
	obj/cpu/m68000/m68kopdm.og obj/cpu/m68000/m68kopnz.og \
	obj/cpu/m68000/m68kcpu.o obj/cpu/m68000/m68kmame.o
endif
DBGOBJS += obj/cpu/m68000/d68k.o
endif

CPU=$(strip $(findstring T11@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_T11=1
CPUOBJS += obj/cpu/t11/t11.o
DBGOBJS += obj/cpu/t11/t11dasm.o
endif

CPU=$(strip $(findstring S2650@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_S2650=1
CPUOBJS += obj/cpu/s2650/s2650.o
DBGOBJS += obj/cpu/s2650/2650dasm.o
endif

CPU=$(strip $(findstring TMS34010@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_TMS34010=1
CPUOBJS += obj/cpu/tms34010/tms34010.o obj/cpu/tms34010/34010fld.o
DBGOBJS += obj/cpu/tms34010/34010dsm.o
endif

CPU=$(strip $(findstring TMS9900@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_TMS9900=1
CPUOBJS += obj/cpu/tms9900/tms9900.o
DBGOBJS += obj/cpu/tms9900/9900dasm.o
endif

CPU=$(strip $(findstring TMS9940@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_TMS9940=1
CPUOBJS += obj/cpu/tms9900/tms9900.o
DBGOBJS += obj/cpu/tms9900/9900dasm.o
endif

CPU=$(strip $(findstring TMS9980@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_TMS9980=1
CPUOBJS += obj/cpu/tms9900/tms9980a.o
DBGOBJS += obj/cpu/tms9900/9900dasm.o
endif

CPU=$(strip $(findstring TMS9985@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_TMS9985=1
CPUOBJS += obj/cpu/tms9900/tms9980a.o
DBGOBJS += obj/cpu/tms9900/9900dasm.o
endif

CPU=$(strip $(findstring TMS9989@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_TMS9989=1
CPUOBJS += obj/cpu/tms9900/tms9980a.o
DBGOBJS += obj/cpu/tms9900/9900dasm.o
endif

CPU=$(strip $(findstring TMS9995@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_TMS9995=1
CPUOBJS += obj/cpu/tms9900/tms9995.o
DBGOBJS += obj/cpu/tms9900/9900dasm.o
endif

CPU=$(strip $(findstring TMS99105A@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_TMS99105A=1
CPUOBJS += obj/cpu/tms9900/tms9995.o
DBGOBJS += obj/cpu/tms9900/9900dasm.o
endif

CPU=$(strip $(findstring TMS99105A@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_TMS99105A=1
CPUOBJS += obj/cpu/tms9900/tms9995.o
DBGOBJS += obj/cpu/tms9900/9900dasm.o
endif

CPU=$(strip $(findstring Z8000@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_Z8000=1
CPUOBJS += obj/cpu/z8000/z8000.o
DBGOBJS += obj/cpu/z8000/8000dasm.o
endif

CPU=$(strip $(findstring TMS320C10@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_TMS320C10=1
CPUOBJS += obj/cpu/tms32010/tms32010.o
DBGOBJS += obj/cpu/tms32010/32010dsm.o
endif

CPU=$(strip $(findstring CCPU@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_CCPU=1
CPUOBJS += obj/cpu/ccpu/ccpu.o obj/vidhrdw/cinemat.o
DBGOBJS += obj/cpu/ccpu/ccpudasm.o
endif

CPU=$(strip $(findstring ADSP2100@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_ADSP2100=1
CPUOBJS += obj/cpu/adsp2100/adsp2100.o
DBGOBJS += obj/cpu/adsp2100/2100dasm.o
endif

CPU=$(strip $(findstring PDP1@,$(CPUS)))
ifneq ($(CPU),)
CPUDEFS += -DHAS_PDP1=1
CPUOBJS += obj/cpu/pdp1/pdp1.o
DBGOBJS += obj/cpu/pdp1/pdp1dasm.o
endif



SOUND=$(strip $(findstring CUSTOM@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_CUSTOM=1
endif

SOUND=$(strip $(findstring SAMPLES@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_SAMPLES=1
SOUNDOBJS += obj/sound/samples.o
endif

SOUND=$(strip $(findstring DAC@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_DAC=1
SOUNDOBJS += obj/sound/dac.o
endif

SOUND=$(strip $(findstring AY8910@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_AY8910=1
SOUNDOBJS += obj/sound/ay8910.o
endif

SOUND=$(strip $(findstring YM2203@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM2203=1
SOUNDOBJS += obj/sound/2203intf.o obj/sound/ay8910.o obj/sound/fm.o
endif

SOUND=$(strip $(findstring YM2151@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM2151=1
SOUNDOBJS += obj/sound/2151intf.o obj/sound/ym2151.o obj/sound/fm.o
endif

SOUND=$(strip $(findstring YM2151_ALT@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM2151_ALT=1
SOUNDOBJS += obj/sound/2151intf.o obj/sound/ym2151.o obj/sound/fm.o
endif

SOUND=$(strip $(findstring YM2608@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM2608=1
SOUNDOBJS += obj/sound/2608intf.o obj/sound/ay8910.o obj/sound/fm.o obj/sound/ymdeltat.o
endif

SOUND=$(strip $(findstring YM2610@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM2610=1
SOUNDOBJS += obj/sound/2610intf.o obj/sound/ay8910.o obj/sound/fm.o obj/sound/ymdeltat.o
endif

SOUND=$(strip $(findstring YM2610B@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM2610B=1
SOUNDOBJS += obj/sound/2610intf.o obj/sound/ay8910.o obj/sound/fm.o obj/sound/ymdeltat.o
endif

SOUND=$(strip $(findstring YM2612@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM2612=1
SOUNDOBJS += obj/sound/2612intf.o obj/sound/ay8910.o obj/sound/fm.o
endif

SOUND=$(strip $(findstring YM3438@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM3438=1
SOUNDOBJS += obj/sound/2612intf.o obj/sound/ay8910.o obj/sound/fm.o
endif

SOUND=$(strip $(findstring YM2413@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM2413=1
SOUNDOBJS += obj/sound/3812intf.o obj/sound/ym2413.o obj/sound/fmopl.o
endif

SOUND=$(strip $(findstring YM3812@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM3812=1
SOUNDOBJS += obj/sound/3812intf.o obj/sound/fmopl.o
endif

SOUND=$(strip $(findstring YM3526@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_YM3526=1
SOUNDOBJS += obj/sound/3812intf.o obj/sound/fmopl.o
endif

SOUND=$(strip $(findstring Y8950@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_Y8950=1
SOUNDOBJS += obj/sound/3812intf.o obj/sound/fmopl.o obj/sound/ymdeltat.o
endif

SOUND=$(strip $(findstring SN76477@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_SN76477=1
SOUNDOBJS += obj/sound/sn76477.o
endif

SOUND=$(strip $(findstring SN76496@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_SN76496=1
SOUNDOBJS += obj/sound/sn76496.o
endif

SOUND=$(strip $(findstring POKEY@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_POKEY=1
SOUNDOBJS += obj/sound/pokey.o
endif

SOUND=$(strip $(findstring TIA@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_TIA=1
SOUNDOBJS += obj/sound/tiasound.o obj/sound/tiaintf.o
endif

SOUND=$(strip $(findstring NES@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_NES=1
SOUNDOBJS += obj/sound/nes_apu.o
endif

SOUND=$(strip $(findstring ASTROCADE@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_ASTROCADE=1
SOUNDOBJS += obj/sound/astrocde.o
endif

SOUND=$(strip $(findstring NAMCO@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_NAMCO=1
SOUNDOBJS += obj/sound/namco.o
endif

SOUND=$(strip $(findstring TMS36XX@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_TMS36XX=1
SOUNDOBJS += obj/sound/tms36xx.o
endif

SOUND=$(strip $(findstring TMS5220@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_TMS5220=1
SOUNDOBJS += obj/sound/tms5220.o obj/sound/5220intf.o
endif

SOUND=$(strip $(findstring VLM5030@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_VLM5030=1
SOUNDOBJS += obj/sound/vlm5030.o
endif

SOUND=$(strip $(findstring ADPCM@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_ADPCM=1
SOUNDOBJS += obj/sound/adpcm.o
endif

SOUND=$(strip $(findstring OKIM6295@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_OKIM6295=1
SOUNDOBJS += obj/sound/adpcm.o
endif

SOUND=$(strip $(findstring MSM5205@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_MSM5205=1
SOUNDOBJS += obj/sound/msm5205.o
endif

SOUND=$(strip $(findstring UPD7759@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_UPD7759=1
SOUNDOBJS += obj/sound/upd7759.o
endif

SOUND=$(strip $(findstring HC55516@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_HC55516=1
SOUNDOBJS += obj/sound/hc55516.o
endif

SOUND=$(strip $(findstring K005289@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_K005289=1
SOUNDOBJS += obj/sound/k005289.o
endif

SOUND=$(strip $(findstring K007232@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_K007232=1
SOUNDOBJS += obj/sound/k007232.o
endif

SOUND=$(strip $(findstring K051649@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_K051649=1
SOUNDOBJS += obj/sound/k051649.o
endif

SOUND=$(strip $(findstring K053260@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_K053260=1
SOUNDOBJS += obj/sound/k053260.o
endif

SOUND=$(strip $(findstring SEGAPCM@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_SEGAPCM=1
SOUNDOBJS += obj/sound/segapcm.o
endif

SOUND=$(strip $(findstring RF5C68@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_RF5C68=1
SOUNDOBJS += obj/sound/rf5c68.o
endif

SOUND=$(strip $(findstring CEM3394@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_CEM3394=1
SOUNDOBJS += obj/sound/cem3394.o
endif

SOUND=$(strip $(findstring C140@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_C140=1
SOUNDOBJS += obj/sound/c140.o
endif

SOUND=$(strip $(findstring QSOUND@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_QSOUND=1
SOUNDOBJS += obj/sound/qsound.o
endif

SOUND=$(strip $(findstring SPEAKER@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_SPEAKER=1
SOUNDOBJS += obj/sound/speaker.o
endif

SOUND=$(strip $(findstring WAVE@,$(SOUNDS)))
ifneq ($(SOUND),)
SOUNDDEFS += -DHAS_WAVE=1
SOUNDOBJS += obj/sound/wave.o
endif



ifdef DEBUG
DEBUGDEF = -DMAME_DEBUG
else
DEBUGDEF =
DBGOBJS =
endif

DEFS = -DX86_ASM -DLSB_FIRST -DINLINE="static __inline__" -Dasm=__asm__
CDEFS = $(DEFS) $(CPUDEFS) $(SOUNDDEFS) $(ASMDEFS) $(DEBUGDEF)

ifdef SYMBOLS
# Sorry for the -Wno-unused, but I was tired ;)
CFLAGS = -Isrc -Isrc/msdos -Iobj/cpu/m68000 -Isrc/cpu/m68000 \
	-O0 -pedantic -Wall -Werror -Wno-unused -g
else
CFLAGS = -Isrc -Isrc/msdos -Iobj/cpu/m68000 -Isrc/cpu/m68000 \
	$(ARCH) -O3 -fomit-frame-pointer -fstrict-aliasing \
	-Werror -Wall -Wno-sign-compare -Wunused \
	-Wpointer-arith -Wbad-function-cast -Wcast-align -Waggregate-return \
	-pedantic \
	-Wshadow \
	-Wstrict-prototypes
#	-W had to remove because of the "missing initializer" warning
#	-Wredundant-decls \
#	-Wlarger-than-27648 \
#	-Wcast-qual \
#	-Wwrite-strings \
#	-Wconversion \
#	-Wmissing-prototypes \
#	-Wmissing-declarations
endif

ifdef SYMBOLS
LDFLAGS =
else
#LDFLAGS = -s -Wl,--warn-common
LDFLAGS = -s
endif

LIBS = -lalleg -laudio -lz \

COREOBJS = obj/version.o obj/driver.o obj/mame.o \
	obj/drawgfx.o obj/common.o obj/usrintrf.o \
	obj/cpuintrf.o obj/memory.o obj/timer.o obj/palette.o \
	obj/input.o obj/inptport.o obj/cheat.o obj/unzip.o \
	obj/audit.o obj/info.o obj/png.o obj/artwork.o \
	obj/tilemap.o obj/sprite.o obj/gfxobj.o \
	obj/state.o obj/datafile.o obj/hiscore.o \
	$(sort $(CPUOBJS)) \
	obj/sndintrf.o \
	obj/sound/streams.o obj/sound/mixer.o \
	$(sort $(SOUNDOBJS)) \
	obj/sound/votrax.o \
	obj/machine/z80fmly.o obj/machine/6821pia.o \
	obj/machine/8255ppi.o \
	obj/vidhrdw/generic.o obj/vidhrdw/vector.o \
	obj/vidhrdw/avgdvg.o obj/machine/mathbox.o \
	obj/machine/ticket.o obj/machine/eeprom.o \
	obj/mamedbg.o obj/window.o \
	obj/profiler.o \
	$(sort $(DBGOBJS)) \

DRVLIBS = obj/pacman.a \
	obj/nichibut.a \
	obj/phoenix.a obj/namco.a obj/univers.a obj/nintendo.a \
	obj/midw8080.a obj/meadows.a obj/midway.a \
	obj/irem.a obj/gottlieb.a obj/taito.a obj/toaplan.a \
	obj/kyugo.a obj/williams.a obj/gremlin.a obj/vicdual.a \
	obj/capcom.a obj/capbowl.a obj/leland.a \
	obj/sega.a obj/dataeast.a obj/tehkan.a obj/konami.a \
	obj/exidy.a obj/atari.a obj/snk.a obj/technos.a \
	obj/berzerk.a obj/gameplan.a obj/stratvox.a obj/zaccaria.a \
	obj/upl.a obj/tms.a obj/cinemar.a obj/cinemav.a obj/thepit.a \
	obj/valadon.a obj/seibu.a obj/tad.a obj/jaleco.a obj/visco.a \
	obj/orca.a obj/gaelco.a obj/kaneko.a obj/other.a \

NEOLIBS = obj/neogeo.a \

MSDOSOBJS = obj/msdos/msdos.o obj/msdos/video.o obj/msdos/blit.o obj/msdos/asmblit.o \
	obj/msdos/vector.o obj/msdos/gen15khz.o obj/msdos/ati15khz.o \
	obj/msdos/sound.o obj/msdos/input.o obj/msdos/fileio.o \
	obj/msdos/ticker.o obj/msdos/config.o obj/msdos/fronthlp.o \

ifdef TINY_COMPILE
	OBJS = $(TINY_OBJS)
	TINYFLAGS = -DTINY_COMPILE -DTINY_NAME=$(TINY_NAME)
else
	ifdef NEOFREE
		OBJS = $(DRVLIBS)
		TINYFLAGS = -DNEOFREE
	else
		ifdef NEOMAME
			OBJS = $(NEOLIBS)
			TINYFLAGS = -DNEOMAME
		else
			OBJS = $(DRVLIBS) $(NEOLIBS)
			TINYFLAGS =
		endif
	endif
endif

all: $(EMULATOR_EXE) romcmp.exe

$(EMULATOR_EXE):  $(COREOBJS) $(MSDOSOBJS) $(OBJS) $(LIBS)
# always recompile the version string
	$(CC) $(CDEFS) $(CFLAGS) $(TINYFLAGS) -c src/version.c -o obj/version.o
	@echo Linking $@...
	$(LD) $(LDFLAGS) $(COREOBJS) $(MSDOSOBJS) $(OBJS) $(LIBS) -o $@
ifndef DEBUG
	upx $(EMULATOR_EXE)
	$(EMULATOR_EXE) -gamelistheader -noclones > gamelist.txt
	$(EMULATOR_EXE) -gamelist -noclones | sort >> gamelist.txt
	$(EMULATOR_EXE) -gamelistfooter >> gamelist.txt
endif

romcmp.exe: obj/romcmp.o obj/unzip.o
	@echo Linking $@...
	$(LD) $(LDFLAGS) $^ -lz -o $@


obj/%.o: src/%.c
	@echo Compiling $<...
	$(CC) $(CDEFS) $(CFLAGS) $(TINYFLAGS) -c $< -o $@

# generate C source files for the 68000 emulator
obj/cpu/m68000/%.c obj/cpu/m68000/%.h: src/cpu/m68000/m68kmake.c src/cpu/m68000/m68k_in.c
	@echo M68K make $<...
	$(CC) $(CDEFS) $(CFLAGS) -DDOS -o obj/cpu/m68000/m68kmake.exe $<
	obj/cpu/m68000/m68kmake obj/cpu/m68000 src/cpu/m68000/m68k_in.c

# generated C files for the 68000 emulator
obj/%.og: obj/%.c
	@echo Compiling $<...
	$(CC) $(CDEFS) $(CFLAGS) $(TINYFLAGS) -c $< -o $@

# generate asm source files for the 68000 emulator
obj/cpu/m68000/68kem.asm:  src/cpu/m68000/make68k.c
	@echo Compiling $<...
	$(CC) $(CDEFS) $(CFLAGS) -O0 -DDOS -o obj/cpu/m68000/make68k.exe $<
	@echo Generating $@...
	obj/cpu/m68000/make68k $@ obj/cpu/m68000/comptab.asm

# generated asm files for the 68000 emulator
obj/cpu/m68000/68kem.oa:  obj/cpu/m68000/68kem.asm
	@echo Assembling $<...
	$(ASM) -o $@ $(ASMFLAGS) $(subst -D,-d,$(ASMDEFS)) $<

# video blitting functions
obj/msdos/asmblit.o: src/msdos/asmblit.asm
	@echo Assembling $<...
	$(ASM) -o $@ $(ASMFLAGS) $(subst -D,-d,$(ASMDEFS)) $<

obj/cpu/z80/z80.asm:  src/cpu/z80/makez80.c
	@echo Compiling $<...
	@$(CC) $(CDEFS) $(CFLAGS) -DDOS -o obj/cpu/z80/makez80.exe $<
	@echo Generating $@...
	obj/cpu/z80/makez80 $(Z80DEF) $(CDEFS) $(CFLAGS) $@

obj/%.a:
	@echo Archiving $@...
	$(AR) cr $@ $^

obj/pacman.a: \
	obj/machine/pacman.o obj/drivers/pacman.o \
	obj/machine/pacplus.o \
	obj/machine/theglob.o \
	obj/machine/jrpacman.o obj/drivers/jrpacman.o obj/vidhrdw/jrpacman.o \
	obj/vidhrdw/pengo.o obj/drivers/pengo.o \

obj/nichibut.a: \
	obj/vidhrdw/cclimber.o obj/sndhrdw/cclimber.o obj/drivers/cclimber.o \
	obj/drivers/yamato.o \
	obj/vidhrdw/seicross.o obj/sndhrdw/wiping.o obj/drivers/seicross.o \
	obj/vidhrdw/wiping.o obj/drivers/wiping.o \
	obj/vidhrdw/cop01.o obj/drivers/cop01.o \
	obj/vidhrdw/terracre.o obj/drivers/terracre.o \
	obj/vidhrdw/galivan.o obj/drivers/galivan.o \
	obj/vidhrdw/armedf.o obj/drivers/armedf.o \

obj/phoenix.a: \
	obj/vidhrdw/phoenix.o obj/sndhrdw/phoenix.o obj/drivers/phoenix.o \
	obj/sndhrdw/pleiads.o \
	obj/vidhrdw/naughtyb.o obj/drivers/naughtyb.o \

obj/namco.a: \
	obj/machine/geebee.o obj/vidhrdw/geebee.o obj/sndhrdw/geebee.o obj/drivers/geebee.o \
	obj/vidhrdw/warpwarp.o obj/sndhrdw/warpwarp.o obj/drivers/warpwarp.o \
	obj/vidhrdw/tankbatt.o obj/drivers/tankbatt.o \
	obj/vidhrdw/galaxian.o obj/sndhrdw/galaxian.o obj/drivers/galaxian.o \
	obj/vidhrdw/rallyx.o obj/drivers/rallyx.o \
	obj/drivers/locomotn.o \
	obj/machine/bosco.o obj/sndhrdw/bosco.o obj/vidhrdw/bosco.o obj/drivers/bosco.o \
	obj/machine/galaga.o obj/vidhrdw/galaga.o obj/drivers/galaga.o \
	obj/machine/digdug.o obj/vidhrdw/digdug.o obj/drivers/digdug.o \
	obj/vidhrdw/xevious.o obj/machine/xevious.o obj/drivers/xevious.o \
	obj/machine/superpac.o obj/vidhrdw/superpac.o obj/drivers/superpac.o \
	obj/machine/phozon.o obj/vidhrdw/phozon.o obj/drivers/phozon.o \
	obj/machine/mappy.o obj/vidhrdw/mappy.o obj/drivers/mappy.o \
	obj/machine/grobda.o obj/vidhrdw/grobda.o obj/drivers/grobda.o \
	obj/machine/gaplus.o obj/vidhrdw/gaplus.o obj/drivers/gaplus.o \
	obj/machine/polepos.o obj/vidhrdw/polepos.o obj/sndhrdw/polepos.o obj/drivers/polepos.o \
	obj/vidhrdw/pacland.o obj/drivers/pacland.o \
	obj/vidhrdw/skykid.o obj/drivers/skykid.o \
	obj/vidhrdw/baraduke.o obj/drivers/baraduke.o \
	obj/vidhrdw/namcos86.o obj/drivers/namcos86.o \
	obj/machine/namcos1.o obj/vidhrdw/namcos1.o obj/drivers/namcos1.o \
	obj/machine/namcos2.o obj/vidhrdw/namcos2.o obj/drivers/namcos2.o \

obj/univers.a: \
	obj/vidhrdw/cosmic.o obj/drivers/cosmic.o \
	obj/vidhrdw/cheekyms.o obj/drivers/cheekyms.o \
	obj/vidhrdw/ladybug.o obj/drivers/ladybug.o \
	obj/vidhrdw/mrdo.o obj/drivers/mrdo.o \
	obj/machine/docastle.o obj/vidhrdw/docastle.o obj/drivers/docastle.o \

obj/nintendo.a: \
	obj/vidhrdw/dkong.o obj/sndhrdw/dkong.o obj/drivers/dkong.o \
	obj/vidhrdw/mario.o obj/sndhrdw/mario.o obj/drivers/mario.o \
	obj/vidhrdw/popeye.o obj/drivers/popeye.o \
	obj/vidhrdw/punchout.o obj/sndhrdw/punchout.o obj/drivers/punchout.o \

obj/midw8080.a: \
	obj/machine/8080bw.o obj/machine/74123.o \
	obj/vidhrdw/8080bw.o obj/sndhrdw/8080bw.o obj/drivers/8080bw.o \
	obj/vidhrdw/m79amb.o obj/drivers/m79amb.o \
	obj/sndhrdw/z80bw.o obj/drivers/z80bw.o \

obj/meadows.a: \
	obj/drivers/lazercmd.o obj/vidhrdw/lazercmd.o \
	obj/drivers/meadows.o obj/sndhrdw/meadows.o obj/vidhrdw/meadows.o \

obj/midway.a: \
	obj/machine/wow.o obj/vidhrdw/wow.o obj/sndhrdw/wow.o obj/drivers/wow.o \
	obj/sndhrdw/gorf.o \
	obj/machine/mcr.o obj/sndhrdw/mcr.o \
	obj/vidhrdw/mcr1.o obj/vidhrdw/mcr2.o obj/vidhrdw/mcr3.o \
	obj/drivers/mcr1.o obj/drivers/mcr2.o obj/drivers/mcr3.o \
	obj/vidhrdw/mcr68.o obj/drivers/mcr68.o \
	obj/vidhrdw/balsente.o obj/drivers/balsente.o \

obj/irem.a: \
	obj/vidhrdw/skychut.o obj/drivers/skychut.o \
	obj/sndhrdw/irem.o \
	obj/vidhrdw/mpatrol.o obj/drivers/mpatrol.o \
	obj/vidhrdw/troangel.o obj/drivers/troangel.o \
	obj/vidhrdw/yard.o obj/drivers/yard.o \
	obj/vidhrdw/travrusa.o obj/drivers/travrusa.o \
	obj/vidhrdw/m62.o obj/drivers/m62.o \
	obj/vidhrdw/vigilant.o obj/drivers/vigilant.o \
	obj/vidhrdw/m72.o obj/sndhrdw/m72.o obj/drivers/m72.o \
	obj/vidhrdw/shisen.o obj/drivers/shisen.o \
	obj/vidhrdw/m92.o obj/drivers/m92.o \
	obj/drivers/m97.o \
	obj/vidhrdw/m107.o obj/drivers/m107.o \

obj/gottlieb.a: \
	obj/vidhrdw/gottlieb.o obj/sndhrdw/gottlieb.o obj/drivers/gottlieb.o \

obj/taito.a: \
	obj/vidhrdw/crbaloon.o obj/drivers/crbaloon.o \
	obj/machine/qix.o obj/vidhrdw/qix.o obj/drivers/qix.o \
	obj/machine/taitosj.o obj/vidhrdw/taitosj.o obj/drivers/taitosj.o \
	obj/vidhrdw/bking2.o obj/drivers/bking2.o \
	obj/vidhrdw/gsword.o obj/drivers/gsword.o obj/machine/tait8741.o \
	obj/vidhrdw/retofinv.o obj/drivers/retofinv.o \
	obj/vidhrdw/tsamurai.o obj/drivers/tsamurai.o \
	obj/machine/flstory.o obj/vidhrdw/flstory.o obj/drivers/flstory.o \
	obj/vidhrdw/gladiatr.o obj/drivers/gladiatr.o \
	obj/machine/bublbobl.o obj/vidhrdw/bublbobl.o obj/drivers/bublbobl.o \
	obj/machine/mexico86.o obj/vidhrdw/mexico86.o obj/drivers/mexico86.o \
	obj/vidhrdw/rastan.o obj/sndhrdw/rastan.o obj/drivers/rastan.o \
	obj/machine/rainbow.o obj/drivers/rainbow.o \
	obj/machine/arkanoid.o obj/vidhrdw/arkanoid.o obj/drivers/arkanoid.o \
	obj/vidhrdw/superqix.o obj/drivers/superqix.o \
	obj/vidhrdw/superman.o obj/drivers/superman.o obj/machine/cchip.o \
	obj/vidhrdw/footchmp.o obj/drivers/footchmp.o \
	obj/vidhrdw/minivadr.o obj/drivers/minivadr.o \
	obj/machine/tnzs.o obj/vidhrdw/tnzs.o obj/drivers/tnzs.o \
	obj/drivers/lkage.o obj/vidhrdw/lkage.o \
	obj/vidhrdw/taitol.o obj/drivers/taitol.o \
	obj/vidhrdw/taitof2.o obj/drivers/taitof2.o \
	obj/vidhrdw/ssi.o obj/drivers/ssi.o \

obj/toaplan.a: \
	obj/machine/slapfght.o obj/vidhrdw/slapfght.o obj/drivers/slapfght.o \
	obj/machine/twincobr.o obj/vidhrdw/twincobr.o \
	obj/drivers/twincobr.o obj/drivers/wardner.o \
	obj/machine/toaplan1.o obj/vidhrdw/toaplan1.o obj/drivers/toaplan1.o \
	obj/vidhrdw/snowbros.o obj/drivers/snowbros.o \
	obj/vidhrdw/toaplan2.o obj/drivers/toaplan2.o \

obj/kyugo.a: \
	obj/drivers/kyugo.o obj/vidhrdw/kyugo.o \

obj/williams.a: \
	obj/machine/williams.o obj/vidhrdw/williams.o obj/sndhrdw/williams.o obj/drivers/williams.o \

obj/capcom.a: \
	obj/vidhrdw/vulgus.o obj/drivers/vulgus.o \
	obj/vidhrdw/sonson.o obj/drivers/sonson.o \
	obj/vidhrdw/higemaru.o obj/drivers/higemaru.o \
	obj/vidhrdw/1942.o obj/drivers/1942.o \
	obj/vidhrdw/exedexes.o obj/drivers/exedexes.o \
	obj/vidhrdw/commando.o obj/drivers/commando.o \
	obj/vidhrdw/gng.o obj/drivers/gng.o \
	obj/vidhrdw/gunsmoke.o obj/drivers/gunsmoke.o \
	obj/vidhrdw/srumbler.o obj/drivers/srumbler.o \
	obj/machine/lwings.o obj/vidhrdw/lwings.o obj/drivers/lwings.o \
	obj/vidhrdw/sidearms.o obj/drivers/sidearms.o \
	obj/vidhrdw/bionicc.o obj/drivers/bionicc.o \
	obj/vidhrdw/1943.o obj/drivers/1943.o \
	obj/vidhrdw/blktiger.o obj/drivers/blktiger.o \
	obj/vidhrdw/tigeroad.o obj/drivers/tigeroad.o \
	obj/vidhrdw/lastduel.o obj/drivers/lastduel.o \
	obj/vidhrdw/sf1.o obj/drivers/sf1.o \
	obj/machine/kabuki.o \
	obj/vidhrdw/mitchell.o obj/drivers/mitchell.o \
	obj/vidhrdw/cbasebal.o obj/drivers/cbasebal.o \
	obj/vidhrdw/cps1.o obj/drivers/cps1.o \
	obj/drivers/zn.o \

obj/capbowl.a: \
	obj/machine/capbowl.o obj/vidhrdw/capbowl.o obj/vidhrdw/tms34061.o obj/drivers/capbowl.o \

obj/gremlin.a: \
	obj/vidhrdw/blockade.o obj/drivers/blockade.o \

obj/vicdual.a: \
	obj/vidhrdw/vicdual.o obj/drivers/vicdual.o \
	obj/sndhrdw/carnival.o obj/sndhrdw/depthch.o obj/sndhrdw/invinco.o obj/sndhrdw/pulsar.o \

obj/sega.a: \
	obj/machine/segacrpt.o \
	obj/vidhrdw/sega.o obj/sndhrdw/sega.o obj/machine/sega.o obj/drivers/sega.o \
	obj/vidhrdw/segar.o obj/sndhrdw/segar.o obj/machine/segar.o obj/drivers/segar.o \
	obj/vidhrdw/zaxxon.o obj/sndhrdw/zaxxon.o obj/drivers/zaxxon.o \
	obj/sndhrdw/congo.o obj/drivers/congo.o \
	obj/machine/turbo.o obj/vidhrdw/turbo.o obj/drivers/turbo.o \
	obj/drivers/kopunch.o \
	obj/vidhrdw/suprloco.o obj/drivers/suprloco.o \
	obj/vidhrdw/champbas.o obj/drivers/champbas.o \
	obj/vidhrdw/appoooh.o obj/drivers/appoooh.o \
	obj/vidhrdw/bankp.o obj/drivers/bankp.o \
	obj/vidhrdw/dotrikun.o obj/drivers/dotrikun.o \
	obj/vidhrdw/system1.o obj/drivers/system1.o \
	obj/machine/system16.o obj/vidhrdw/system16.o obj/sndhrdw/system16.o obj/drivers/system16.o \

obj/dataeast.a: \
	obj/machine/btime.o obj/vidhrdw/btime.o obj/drivers/btime.o \
	obj/vidhrdw/astrof.o obj/sndhrdw/astrof.o obj/drivers/astrof.o \
	obj/vidhrdw/kchamp.o obj/drivers/kchamp.o \
	obj/vidhrdw/firetrap.o obj/drivers/firetrap.o \
	obj/vidhrdw/brkthru.o obj/drivers/brkthru.o \
	obj/vidhrdw/shootout.o obj/drivers/shootout.o \
	obj/vidhrdw/sidepckt.o obj/drivers/sidepckt.o \
	obj/vidhrdw/exprraid.o obj/drivers/exprraid.o \
	obj/vidhrdw/pcktgal.o obj/drivers/pcktgal.o \
	obj/vidhrdw/actfancr.o obj/drivers/actfancr.o \
	obj/vidhrdw/dec8.o obj/drivers/dec8.o \
	obj/vidhrdw/karnov.o obj/drivers/karnov.o \
	obj/machine/dec0.o obj/vidhrdw/dec0.o obj/drivers/dec0.o \
	obj/vidhrdw/stadhero.o obj/drivers/stadhero.o \
	obj/vidhrdw/madmotor.o obj/drivers/madmotor.o \
	obj/vidhrdw/vaportra.o obj/drivers/vaportra.o \
	obj/vidhrdw/cbuster.o obj/drivers/cbuster.o \
	obj/vidhrdw/darkseal.o obj/drivers/darkseal.o \
	obj/vidhrdw/supbtime.o obj/drivers/supbtime.o \
	obj/vidhrdw/cninja.o obj/drivers/cninja.o \
	obj/vidhrdw/tumblep.o obj/drivers/tumblep.o \
	obj/vidhrdw/funkyjet.o obj/drivers/funkyjet.o \

obj/tehkan.a: \
	obj/sndhrdw/senjyo.o obj/vidhrdw/senjyo.o obj/drivers/senjyo.o \
	obj/vidhrdw/bombjack.o obj/drivers/bombjack.o \
	obj/vidhrdw/pbaction.o obj/drivers/pbaction.o \
	obj/vidhrdw/tehkanwc.o obj/drivers/tehkanwc.o \
	obj/vidhrdw/solomon.o obj/drivers/solomon.o \
	obj/vidhrdw/tecmo.o obj/drivers/tecmo.o \
	obj/vidhrdw/gaiden.o obj/drivers/gaiden.o \
	obj/vidhrdw/wc90.o obj/drivers/wc90.o \
	obj/vidhrdw/wc90b.o obj/drivers/wc90b.o \

obj/konami.a: \
	obj/machine/scramble.o obj/sndhrdw/scramble.o obj/drivers/scramble.o \
	obj/vidhrdw/frogger.o obj/sndhrdw/frogger.o obj/drivers/frogger.o \
	obj/drivers/scobra.o \
	obj/vidhrdw/amidar.o obj/drivers/amidar.o \
	obj/vidhrdw/fastfred.o obj/drivers/fastfred.o \
	obj/sndhrdw/timeplt.o \
	obj/vidhrdw/tutankhm.o obj/drivers/tutankhm.o \
	obj/drivers/junofrst.o \
	obj/vidhrdw/pooyan.o obj/drivers/pooyan.o \
	obj/vidhrdw/timeplt.o obj/drivers/timeplt.o \
	obj/vidhrdw/megazone.o obj/drivers/megazone.o \
	obj/vidhrdw/pandoras.o obj/drivers/pandoras.o \
	obj/sndhrdw/gyruss.o obj/vidhrdw/gyruss.o obj/drivers/gyruss.o \
	obj/machine/konami.o obj/vidhrdw/trackfld.o obj/sndhrdw/trackfld.o obj/drivers/trackfld.o \
	obj/vidhrdw/rocnrope.o obj/drivers/rocnrope.o \
	obj/vidhrdw/circusc.o obj/drivers/circusc.o \
	obj/machine/tp84.o obj/vidhrdw/tp84.o obj/drivers/tp84.o \
	obj/vidhrdw/hyperspt.o obj/drivers/hyperspt.o \
	obj/vidhrdw/sbasketb.o obj/drivers/sbasketb.o \
	obj/vidhrdw/mikie.o obj/drivers/mikie.o \
	obj/vidhrdw/yiear.o obj/drivers/yiear.o \
	obj/vidhrdw/shaolins.o obj/drivers/shaolins.o \
	obj/vidhrdw/pingpong.o obj/drivers/pingpong.o \
	obj/vidhrdw/gberet.o obj/drivers/gberet.o \
	obj/vidhrdw/jailbrek.o obj/drivers/jailbrek.o \
	obj/vidhrdw/finalizr.o obj/drivers/finalizr.o \
	obj/vidhrdw/ironhors.o obj/drivers/ironhors.o \
	obj/machine/jackal.o obj/vidhrdw/jackal.o obj/drivers/jackal.o \
	obj/machine/ddrible.o obj/vidhrdw/ddrible.o obj/drivers/ddrible.o \
	obj/vidhrdw/contra.o obj/drivers/contra.o \
	obj/vidhrdw/combatsc.o obj/drivers/combatsc.o \
	obj/vidhrdw/hcastle.o obj/drivers/hcastle.o \
	obj/vidhrdw/nemesis.o obj/drivers/nemesis.o \
	obj/vidhrdw/konamiic.o \
	obj/vidhrdw/rockrage.o obj/drivers/rockrage.o \
	obj/vidhrdw/flkatck.o obj/drivers/flkatck.o \
	obj/vidhrdw/fastlane.o obj/drivers/fastlane.o \
	obj/vidhrdw/labyrunr.o obj/drivers/labyrunr.o \
	obj/vidhrdw/battlnts.o obj/drivers/battlnts.o \
	obj/vidhrdw/bladestl.o obj/drivers/bladestl.o \
	obj/machine/ajax.o obj/vidhrdw/ajax.o obj/drivers/ajax.o \
	obj/vidhrdw/thunderx.o obj/drivers/thunderx.o \
	obj/vidhrdw/mainevt.o obj/drivers/mainevt.o \
	obj/vidhrdw/88games.o obj/drivers/88games.o \
	obj/vidhrdw/gbusters.o obj/drivers/gbusters.o \
	obj/vidhrdw/crimfght.o obj/drivers/crimfght.o \
	obj/vidhrdw/spy.o obj/drivers/spy.o \
	obj/vidhrdw/bottom9.o obj/drivers/bottom9.o \
	obj/vidhrdw/blockhl.o obj/drivers/blockhl.o \
	obj/vidhrdw/aliens.o obj/drivers/aliens.o \
	obj/vidhrdw/surpratk.o obj/drivers/surpratk.o \
	obj/vidhrdw/parodius.o obj/drivers/parodius.o \
	obj/vidhrdw/rollerg.o obj/drivers/rollerg.o \
	obj/vidhrdw/xexex.o obj/drivers/xexex.o \
	obj/machine/simpsons.o obj/vidhrdw/simpsons.o obj/drivers/simpsons.o \
	obj/vidhrdw/vendetta.o obj/drivers/vendetta.o \
	obj/vidhrdw/twin16.o obj/drivers/twin16.o \
	obj/vidhrdw/gradius3.o obj/drivers/gradius3.o \
	obj/vidhrdw/tmnt.o obj/drivers/tmnt.o \
	obj/vidhrdw/xmen.o obj/drivers/xmen.o \
	obj/vidhrdw/wecleman.o obj/drivers/wecleman.o \
	obj/vidhrdw/ultraman.o obj/drivers/ultraman.o \

obj/exidy.a: \
	obj/machine/exidy.o obj/vidhrdw/exidy.o obj/sndhrdw/exidy.o obj/drivers/exidy.o \
	obj/sndhrdw/targ.o \
	obj/vidhrdw/circus.o obj/drivers/circus.o \
	obj/machine/starfire.o obj/vidhrdw/starfire.o obj/drivers/starfire.o \
	obj/sndhrdw/exidy440.o obj/vidhrdw/exidy440.o obj/drivers/exidy440.o \

obj/atari.a: \
	obj/machine/atari_vg.o \
	obj/machine/asteroid.o obj/sndhrdw/asteroid.o \
	obj/vidhrdw/llander.o obj/sndhrdw/llander.o obj/drivers/asteroid.o \
	obj/drivers/bwidow.o \
	obj/sndhrdw/bzone.o  obj/drivers/bzone.o \
	obj/sndhrdw/redbaron.o \
	obj/drivers/tempest.o \
	obj/machine/starwars.o obj/machine/swmathbx.o \
	obj/drivers/starwars.o obj/sndhrdw/starwars.o \
	obj/machine/mhavoc.o obj/drivers/mhavoc.o \
	obj/machine/quantum.o obj/drivers/quantum.o \
	obj/machine/atarifb.o obj/vidhrdw/atarifb.o obj/drivers/atarifb.o \
	obj/machine/sprint2.o obj/vidhrdw/sprint2.o obj/drivers/sprint2.o \
	obj/machine/sbrkout.o obj/vidhrdw/sbrkout.o obj/drivers/sbrkout.o \
	obj/machine/dominos.o obj/vidhrdw/dominos.o obj/drivers/dominos.o \
	obj/vidhrdw/nitedrvr.o obj/machine/nitedrvr.o obj/drivers/nitedrvr.o \
	obj/vidhrdw/bsktball.o obj/machine/bsktball.o obj/drivers/bsktball.o \
	obj/vidhrdw/copsnrob.o obj/machine/copsnrob.o obj/drivers/copsnrob.o \
	obj/machine/avalnche.o obj/vidhrdw/avalnche.o obj/drivers/avalnche.o \
	obj/machine/subs.o obj/vidhrdw/subs.o obj/drivers/subs.o \
	obj/machine/atarifb.o obj/vidhrdw/atarifb.o obj/drivers/atarifb.o \
	obj/vidhrdw/canyon.o obj/drivers/canyon.o \
	obj/vidhrdw/skydiver.o obj/drivers/skydiver.o \
	obj/vidhrdw/warlord.o obj/drivers/warlord.o \
	obj/machine/centiped.o obj/vidhrdw/centiped.o obj/drivers/centiped.o \
	obj/machine/milliped.o obj/vidhrdw/milliped.o obj/drivers/milliped.o \
	obj/vidhrdw/qwakprot.o obj/drivers/qwakprot.o \
	obj/machine/kangaroo.o obj/vidhrdw/kangaroo.o obj/drivers/kangaroo.o \
	obj/machine/arabian.o obj/vidhrdw/arabian.o obj/drivers/arabian.o \
	obj/machine/missile.o obj/vidhrdw/missile.o obj/drivers/missile.o \
	obj/machine/foodf.o obj/vidhrdw/foodf.o obj/drivers/foodf.o \
	obj/vidhrdw/liberatr.o obj/machine/liberatr.o obj/drivers/liberatr.o \
	obj/vidhrdw/ccastles.o obj/drivers/ccastles.o \
	obj/machine/cloak.o obj/vidhrdw/cloak.o obj/drivers/cloak.o \
	obj/vidhrdw/cloud9.o obj/drivers/cloud9.o \
	obj/machine/jedi.o obj/vidhrdw/jedi.o obj/sndhrdw/jedi.o obj/drivers/jedi.o \
	obj/machine/atarigen.o obj/sndhrdw/atarijsa.o \
	obj/machine/slapstic.o \
	obj/vidhrdw/atarisy1.o obj/drivers/atarisy1.o \
	obj/vidhrdw/atarisy2.o obj/drivers/atarisy2.o \
	obj/vidhrdw/gauntlet.o obj/drivers/gauntlet.o \
	obj/vidhrdw/atetris.o obj/drivers/atetris.o \
	obj/vidhrdw/toobin.o obj/drivers/toobin.o \
	obj/vidhrdw/vindictr.o obj/drivers/vindictr.o \
	obj/vidhrdw/klax.o obj/drivers/klax.o \
	obj/vidhrdw/blstroid.o obj/drivers/blstroid.o \
	obj/vidhrdw/xybots.o obj/drivers/xybots.o \
	obj/vidhrdw/eprom.o obj/drivers/eprom.o \
	obj/vidhrdw/skullxbo.o obj/drivers/skullxbo.o \
	obj/vidhrdw/badlands.o obj/drivers/badlands.o \
	obj/vidhrdw/cyberbal.o obj/drivers/cyberbal.o \
	obj/vidhrdw/rampart.o obj/drivers/rampart.o \
	obj/vidhrdw/shuuz.o obj/drivers/shuuz.o \
	obj/vidhrdw/hydra.o obj/drivers/hydra.o \
	obj/vidhrdw/thunderj.o obj/drivers/thunderj.o \
	obj/vidhrdw/batman.o obj/drivers/batman.o \
	obj/vidhrdw/relief.o obj/drivers/relief.o \
	obj/vidhrdw/offtwall.o obj/drivers/offtwall.o \
	obj/vidhrdw/arcadecl.o obj/drivers/arcadecl.o \

obj/snk.a: \
	obj/vidhrdw/rockola.o obj/sndhrdw/rockola.o obj/drivers/rockola.o \
	obj/vidhrdw/lasso.o obj/drivers/lasso.o \
	obj/drivers/munchmo.o \
	obj/vidhrdw/marvins.o obj/drivers/marvins.o \
	obj/drivers/hal21.o \
	obj/vidhrdw/snk.o obj/drivers/snk.o \
	obj/vidhrdw/snk68.o obj/drivers/snk68.o \
	obj/vidhrdw/prehisle.o obj/drivers/prehisle.o \
	obj/vidhrdw/alpha68k.o obj/drivers/alpha68k.o \

obj/technos.a: \
	obj/drivers/scregg.o \
	obj/vidhrdw/tagteam.o obj/drivers/tagteam.o \
	obj/vidhrdw/ssozumo.o obj/drivers/ssozumo.o \
	obj/vidhrdw/mystston.o obj/drivers/mystston.o \
	obj/vidhrdw/bogeyman.o obj/drivers/bogeyman.o \
	obj/vidhrdw/matmania.o obj/drivers/matmania.o obj/machine/maniach.o \
	obj/vidhrdw/renegade.o obj/drivers/renegade.o \
	obj/vidhrdw/xain.o obj/drivers/xain.o \
	obj/vidhrdw/battlane.o obj/drivers/battlane.o \
	obj/vidhrdw/ddragon.o obj/drivers/ddragon.o \
	obj/vidhrdw/ddragon3.o obj/drivers/ddragon3.o \
	obj/vidhrdw/blockout.o obj/drivers/blockout.o \

obj/berzerk.a: \
	obj/machine/berzerk.o obj/vidhrdw/berzerk.o obj/sndhrdw/berzerk.o obj/drivers/berzerk.o \

obj/gameplan.a: \
	obj/vidhrdw/gameplan.o obj/drivers/gameplan.o \

obj/stratvox.a: \
	obj/vidhrdw/route16.o obj/drivers/route16.o \

obj/zaccaria.a: \
	obj/vidhrdw/zaccaria.o obj/drivers/zaccaria.o \

obj/upl.a: \
	obj/vidhrdw/nova2001.o obj/drivers/nova2001.o \
	obj/vidhrdw/pkunwar.o obj/drivers/pkunwar.o \
	obj/vidhrdw/ninjakd2.o obj/drivers/ninjakd2.o \
	obj/vidhrdw/mnight.o obj/drivers/mnight.o \

obj/tms.a: \
	obj/machine/exterm.o obj/vidhrdw/exterm.o obj/drivers/exterm.o \
	obj/machine/smashtv.o obj/vidhrdw/smashtv.o obj/drivers/smashtv.o \

obj/cinemar.a: \
	obj/vidhrdw/jack.o obj/drivers/jack.o \

obj/cinemav.a: \
	obj/sndhrdw/cinemat.o obj/drivers/cinemat.o \
	obj/machine/cchasm.o obj/vidhrdw/cchasm.o obj/sndhrdw/cchasm.o obj/drivers/cchasm.o \

obj/thepit.a: \
	obj/vidhrdw/thepit.o obj/drivers/thepit.o \

obj/valadon.a: \
	obj/machine/bagman.o obj/vidhrdw/bagman.o obj/drivers/bagman.o \

obj/seibu.a: \
	obj/vidhrdw/wiz.o obj/drivers/wiz.o \
	obj/machine/stfight.o obj/vidhrdw/stfight.o obj/drivers/stfight.o \
	obj/sndhrdw/seibu.o \
	obj/vidhrdw/dynduke.o obj/drivers/dynduke.o \
	obj/vidhrdw/raiden.o obj/drivers/raiden.o \
	obj/vidhrdw/dcon.o obj/drivers/dcon.o \

obj/tad.a: \
	obj/vidhrdw/cabal.o obj/drivers/cabal.o \
	obj/vidhrdw/toki.o obj/drivers/toki.o \
	obj/vidhrdw/bloodbro.o obj/drivers/bloodbro.o \

obj/jaleco.a: \
	obj/vidhrdw/exerion.o obj/drivers/exerion.o \
	obj/vidhrdw/aeroboto.o obj/drivers/aeroboto.o \
	obj/vidhrdw/citycon.o obj/drivers/citycon.o \
	obj/vidhrdw/pinbo.o obj/drivers/pinbo.o \
	obj/vidhrdw/psychic5.o obj/drivers/psychic5.o \
	obj/vidhrdw/ginganin.o obj/drivers/ginganin.o \
	obj/vidhrdw/megasys1.o obj/drivers/megasys1.o \
	obj/vidhrdw/cischeat.o obj/drivers/cischeat.o \

obj/visco.a: \
	obj/vidhrdw/aerofgt.o obj/drivers/aerofgt.o \

obj/leland.a: \
	obj/machine/8254pit.o obj/vidhrdw/leland.o obj/drivers/leland.o \
	obj/drivers/ataxx.o \

obj/orca.a: \
	obj/vidhrdw/marineb.o obj/drivers/marineb.o \
	obj/vidhrdw/funkybee.o obj/drivers/funkybee.o \
	obj/vidhrdw/zodiack.o obj/drivers/zodiack.o \
	obj/machine/espial.o obj/vidhrdw/espial.o obj/drivers/espial.o \
	obj/machine/vastar.o obj/vidhrdw/vastar.o obj/drivers/vastar.o \

obj/gaelco.a: \
	obj/vidhrdw/gaelco.o obj/drivers/gaelco.o \

obj/kaneko.a: \
	obj/vidhrdw/kaneko16.o obj/drivers/kaneko16.o \
	obj/vidhrdw/galpanic.o obj/drivers/galpanic.o \
	obj/vidhrdw/airbustr.o obj/drivers/airbustr.o \

obj/neogeo.a: \
	obj/machine/neogeo.o obj/machine/pd4990a.o obj/vidhrdw/neogeo.o obj/drivers/neogeo.o \

obj/other.a: \
	obj/vidhrdw/spacefb.o obj/sndhrdw/spacefb.o obj/drivers/spacefb.o \
	obj/vidhrdw/blueprnt.o obj/drivers/blueprnt.o \
	obj/drivers/omegrace.o \
	obj/vidhrdw/dday.o obj/drivers/dday.o \
	obj/vidhrdw/gundealr.o obj/drivers/gundealr.o \
	obj/machine/leprechn.o obj/vidhrdw/leprechn.o obj/drivers/leprechn.o \
	obj/vidhrdw/hexa.o obj/drivers/hexa.o \
	obj/vidhrdw/redalert.o obj/sndhrdw/redalert.o obj/drivers/redalert.o \
	obj/machine/irobot.o obj/vidhrdw/irobot.o obj/drivers/irobot.o \
	obj/machine/spiders.o obj/vidhrdw/crtc6845.o obj/vidhrdw/spiders.o obj/drivers/spiders.o \
	obj/machine/stactics.o obj/vidhrdw/stactics.o obj/drivers/stactics.o \
	obj/vidhrdw/sharkatt.o obj/drivers/sharkatt.o \
	obj/vidhrdw/kingobox.o obj/drivers/kingobox.o \
	obj/vidhrdw/zerozone.o obj/drivers/zerozone.o \
	obj/machine/exctsccr.o obj/vidhrdw/exctsccr.o obj/drivers/exctsccr.o \
	obj/vidhrdw/speedbal.o obj/drivers/speedbal.o \
	obj/vidhrdw/sauro.o obj/drivers/sauro.o \
	obj/vidhrdw/ambush.o obj/drivers/ambush.o \
	obj/vidhrdw/starcrus.o obj/drivers/starcrus.o \
	obj/drivers/shanghai.o \
	obj/vidhrdw/goindol.o obj/drivers/goindol.o \
	obj/drivers/dlair.o \
	obj/vidhrdw/meteor.o obj/drivers/meteor.o \
	obj/vidhrdw/bjtwin.o obj/drivers/bjtwin.o \
	obj/vidhrdw/aztarac.o obj/sndhrdw/aztarac.o obj/drivers/aztarac.o \
	obj/vidhrdw/mole.o obj/drivers/mole.o \
	obj/vidhrdw/gotya.o obj/drivers/gotya.o \

# dependencies
obj/cpu/z80/z80.o: z80.c z80.h z80daa.h
obj/cpu/z8000/z8000.o: z8000.c z8000.h z8000cpu.h z8000dab.h z8000ops.c z8000tbl.c
obj/cpu/s2650/s2650.o: s2650.c s2650.h s2650cpu.h
obj/cpu/h6280/h6280.o: h6280.c h6280.h h6280ops.h tblh6280.c
obj/cpu/i8039/i8039.o: i8039.c i8039.h
obj/cpu/i8085/i8085.o: i8085.c i8085.h i8085cpu.h i8085daa.h
obj/cpu/i86/i86.o: i86.c i86.h i86intrf.h ea.h host.h instr.h modrm.h
obj/cpu/nec/nec.o: nec.c nec.h necintrf.h necea.h nechost.h necinstr.h necmodrm.h
obj/cpu/m6502/m6502.o: m6502.c m6502.h ops02.h t6502.c t65c02.c t65sc02.c t6510.c
obj/cpu/m6502/m65ce02.o: m65ce02.c m65ce02.h opsce02.h t65ce02.c
obj/cpu/m6502/m6509.o: m6509.c m6509.h ops09.h t6509.c
obj/cpu/m6800/m6800.o: m6800.c m6800.h 6800ops.c 6800tbl.c
obj/cpu/m6805/m6805.o: m6805.c m6805.h 6805ops.c
obj/cpu/m6809/m6809.o: m6809.c m6809.h 6809ops.c 6809tbl.c
obj/cpu/tms32010/tms32010.o: tms32010.c tms32010.h
obj/cpu/tms34010/tms34010.o: tms34010.c tms34010.h 34010ops.c 34010tbl.c
obj/cpu/tms9900/tms9900.o: tms9900.c tms9900.h 9900stat.h
obj/cpu/t11/t11.o: t11.c t11.h t11ops.c t11table.c
obj/cpu/m68000/m68kcpu.o: obj/cpu/m68000/m68kops.c m68kmake.c m68k_in.c
obj/cpu/ccpu/ccpu.o: ccpu.c ccpu.h ccputabl.c
obj/cpu/konami/konami.o: konami.c konami.h konamops.c konamtbl.c

makedir:
	@echo make makedir is no longer necessary, just type make

maketree:
	@echo Making object tree...
	@md obj
	@md obj\cpu
	@md obj\cpu\z80
	@md obj\cpu\z80gb
	@md obj\cpu\m6502
	@md obj\cpu\h6280
	@md obj\cpu\i86
	@md obj\cpu\nec
	@md obj\cpu\i8039
	@md obj\cpu\i8085
	@md obj\cpu\m6800
	@md obj\cpu\m6805
	@md obj\cpu\m6809
	@md obj\cpu\konami
	@md obj\cpu\m68000
	@md obj\cpu\s2650
	@md obj\cpu\t11
	@md obj\cpu\tms34010
	@md obj\cpu\tms9900
	@md obj\cpu\z8000
	@md obj\cpu\tms32010
	@md obj\cpu\ccpu
	@md obj\cpu\adsp2100
	@md obj\cpu\pdp1
	@md obj\sound
	@md obj\drivers
	@md obj\machine
	@md obj\vidhrdw
	@md obj\sndhrdw
	@md obj\msdos

clean:
	@echo Deleting object tree...
	deltree /Y obj
	@echo Deleting $(EMULATOR_EXE)...
	@del $(EMULATOR_EXE)
	@echo Deleting romcmp.exe...
	@del romcmp.exe

cleandebug:
	@echo Deleting debug obj tree...
	@del obj\*.o
	@del obj\cpu\z80\*.o
	@del obj\cpu\z80\*.oa
	@del obj\cpu\z80\*.asm
	@del obj\cpu\z80\*.exe
	@del obj\cpu\z80gb\*.o
	@del obj\cpu\m6502\*.o
	@del obj\cpu\h6280\*.o
	@del obj\cpu\i86\*.o
	@del obj\cpu\nec\*.o
	@del obj\cpu\i8039\*.o
	@del obj\cpu\i8085\*.o
	@del obj\cpu\m6800\*.o
	@del obj\cpu\m6800\*.oa
	@del obj\cpu\m6800\*.exe
	@del obj\cpu\m6805\*.o
	@del obj\cpu\m6809\*.o
	@del obj\cpu\konami\*.o
	@del obj\cpu\m68000\*.o
	@del obj\cpu\m68000\*.c
	@del obj\cpu\m68000\*.h
	@del obj\cpu\m68000\*.oa
	@del obj\cpu\m68000\*.og
	@del obj\cpu\m68000\*.asm
	@del obj\cpu\m68000\*.exe
	@del obj\cpu\s2650\*.o
	@del obj\cpu\t11\*.o
	@del obj\cpu\tms34010\*.o
	@del obj\cpu\tms9900\*.o
	@del obj\cpu\z8000\*.o
	@del obj\cpu\tms32010\*.o
	@del obj\cpu\ccpu\*.o
	@del obj\cpu\adsp2100\*.o
	@del obj\cpu\pdp1\*.o
	@del $(EMULATOR_EXE)

cleantiny:
	@echo Deleting tiny obj tree...
	@del obj\driver.o
	@del obj\usrintrf.o
	@del obj\cheat.o
	@del obj\vidhrdw\konamiic.o
	@del obj\msdos\input.o

