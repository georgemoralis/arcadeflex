# a tiny compile is without Neogeo games
COREDEFS += -DTINY_COMPILE=1 -DTINY_NAME=driver_buggychl

# uses these CPUs
CPUS+=Z80@
CPUS+=M68705@

# uses these SOUNDs
SOUNDS+=AY8910@

OBJS = $(OBJ)/machine/buggychl.o $(OBJ)/vidhrdw/buggychl.o $(OBJ)/drivers/buggychl.o

# MAME specific core objs
COREOBJS += $(OBJ)/driver.o
