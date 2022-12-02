/***************************************************************************

	Midway/Williams Audio Board

****************************************************************************/

extern struct MemoryReadAddress williams_cvsd_readmem[];
extern struct MemoryWriteAddress williams_cvsd_writemem[];
extern struct MemoryReadAddress williams_adpcm_readmem[];
extern struct MemoryWriteAddress williams_adpcm_writemem[];
extern struct MemoryReadAddress williams_narc_master_readmem[];
extern struct MemoryWriteAddress williams_narc_master_writemem[];
extern struct MemoryReadAddress williams_narc_slave_readmem[];
extern struct MemoryWriteAddress williams_narc_slave_writemem[];

extern struct CustomSound_interface williams_custom_interface;
extern struct YM2151interface williams_cvsd_ym2151_interface;
extern struct YM2151interface williams_adpcm_ym2151_interface;
extern struct DACinterface williams_cvsd_dac_interface;
extern struct DACinterface williams_adpcm_dac_interface;
extern struct DACinterface williams_narc_dac_interface;
extern struct hc55516_interface williams_cvsd_interface;
extern struct OKIM6295interface williams_adpcm_6295_interface_REGION_SOUND1;

void williams_cvsd_init(int cpunum, int pianum);
void williams_cvsd_data_w(int offset, int data);
void williams_cvsd_reset_w(int state);

void williams_adpcm_init(int cpunum);
void williams_adpcm_data_w(int offset, int data);
void williams_adpcm_reset_w(int state);

void williams_narc_init(int cpunum);
void williams_narc_data_w(int offset, int data);
void williams_narc_reset_w(int state);


#define SOUND_CPU_WILLIAMS_CVSD								\
	{														\
		CPU_M6809 | CPU_AUDIO_CPU,							\
		8000000/4,	/* 2 Mhz */								\
		williams_cvsd_readmem,williams_cvsd_writemem,0,0,	\
		ignore_interrupt,1									\
	}

#define SOUND_WILLIAMS_CVSD									\
	{														\
		SOUND_CUSTOM,										\
		&williams_custom_interface							\
	},														\
	{														\
		SOUND_YM2151,										\
		&williams_cvsd_ym2151_interface						\
	},														\
	{														\
		SOUND_DAC,											\
		&williams_cvsd_dac_interface						\
	},														\
	{														\
		SOUND_HC55516,										\
		&williams_cvsd_interface							\
	}


#define SOUND_CPU_WILLIAMS_ADPCM							\
	{														\
		CPU_M6809 | CPU_AUDIO_CPU,							\
		8000000/4,	/* 2 Mhz */								\
		williams_adpcm_readmem,williams_adpcm_writemem,0,0,	\
		ignore_interrupt,1									\
	}

#define SOUND_WILLIAMS_ADPCM(rgn)							\
	{														\
		SOUND_CUSTOM,										\
		&williams_custom_interface							\
	},														\
	{														\
		SOUND_YM2151,										\
		&williams_adpcm_ym2151_interface					\
	},														\
	{														\
		SOUND_DAC,											\
		&williams_adpcm_dac_interface						\
	},														\
	{														\
		SOUND_OKIM6295,										\
		&williams_adpcm_6295_interface_##rgn				\
	}


#define SOUND_CPU_WILLIAMS_NARC								\
	{														\
		CPU_M6809 | CPU_AUDIO_CPU,							\
		8000000/4,	/* 2 Mhz */								\
		williams_narc_master_readmem,williams_narc_master_writemem,0,0,\
		ignore_interrupt,1									\
	},														\
	{														\
		CPU_M6809 | CPU_AUDIO_CPU,							\
		8000000/4,	/* 2 Mhz */								\
		williams_narc_slave_readmem,williams_narc_slave_writemem,0,0,\
		ignore_interrupt,1									\
	}

#define SOUND_WILLIAMS_NARC									\
	{														\
		SOUND_CUSTOM,										\
		&williams_custom_interface							\
	},														\
	{														\
		SOUND_YM2151,										\
		&williams_adpcm_ym2151_interface					\
	},														\
	{														\
		SOUND_DAC,											\
		&williams_narc_dac_interface						\
	},														\
	{														\
		SOUND_HC55516,										\
		&williams_cvsd_interface							\
	}
