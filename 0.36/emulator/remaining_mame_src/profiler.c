#include "driver.h"
#include "osinline.h"

static int use_profiler;


#define MEMORY 6

struct profile_data
{
	unsigned int count[MEMORY][PROFILER_TOTAL];
	unsigned int cpu_context_switches[MEMORY];
};

static struct profile_data profile;
static int memory;


static int FILO_type[10];
static unsigned int FILO_start[10];
static int FILO_length;

void profiler_start(void)
{
	use_profiler = 1;
	FILO_length = 0;
}

void profiler_stop(void)
{
	use_profiler = 0;
}

void profiler_mark(int type)
{
	unsigned int curr_cycles;


	if (!use_profiler)
	{
		FILO_length = 0;
		return;
	}

	if (type >= PROFILER_CPU1 && type <= PROFILER_CPU8)
		profile.cpu_context_switches[memory]++;

	curr_cycles = osd_cycles();

	if (type != PROFILER_END)
	{
		if (FILO_length >= 10)
		{
if (errorlog) fprintf(errorlog,"Profiler error: FILO buffer overflow\n");
			return;
		}

		if (FILO_length > 0)
		{
			/* handle nested calls */
			profile.count[memory][FILO_type[FILO_length-1]] += (unsigned int)(curr_cycles - FILO_start[FILO_length-1]);
		}
		FILO_type[FILO_length] = type;
		FILO_start[FILO_length] = curr_cycles;
		FILO_length++;
	}
	else
	{
		if (FILO_length <= 0)
		{
if (errorlog) fprintf(errorlog,"Profiler error: FILO buffer underflow\n");
			return;
		}

		profile.count[memory][FILO_type[FILO_length-1]] += (unsigned int)(curr_cycles - FILO_start[FILO_length-1]);
		FILO_length--;
		if (FILO_length > 0)
		{
			/* handle nested calls */
			FILO_start[FILO_length-1] = curr_cycles;
		}
	}
}

void profiler_show(void)
{
	int i,j;
	unsigned int total,normalize;
	unsigned int computed;
	int line;
	char buf[30];
	static char *names[PROFILER_TOTAL] =
	{
		"CPU 1",
		"CPU 2",
		"CPU 3",
		"CPU 4",
		"CPU 5",
		"CPU 6",
		"CPU 7",
		"CPU 8",
		"Video",
		"Blit ",
		"Sound",
		"Mixer",
		"Cllbk",
		"Hiscr",
		"Input",
		"Extra",
		"User1",
		"User2",
		"User3",
		"User4",
		"Prflr",
		"Idle ",
	};


	if (!use_profiler) return;

	profiler_mark(PROFILER_PROFILER);

	computed = 0;
	i = 0;
	while (i < PROFILER_PROFILER)
	{
		for (j = 0;j < MEMORY;j++)
			computed += profile.count[j][i];
		i++;
	}
	normalize = computed;
	while (i < PROFILER_TOTAL)
	{
		for (j = 0;j < MEMORY;j++)
			computed += profile.count[j][i];
		i++;
	}
	total = computed;

	if (total == 0 || normalize == 0) return;	/* we have been just reset */

	line = 0;
	for (i = 0;i < PROFILER_TOTAL;i++)
	{
		computed = 0;
		{
			for (j = 0;j < MEMORY;j++)
				computed += profile.count[j][i];
		}
		if (computed)
		{
			if (i < PROFILER_PROFILER)
				sprintf(buf,"%s%3d%%%3d%%",names[i],(computed + total/200) / (total/100),(computed + normalize/200) / (normalize/100));
			else
				sprintf(buf,"%s%3d%%",names[i],(computed + total/200) / (total/100));
			ui_text(buf,0,(line++)*Machine->uifontheight);
		}
	}

	computed = 0;
	{
		for (j = 0;j < MEMORY;j++)
			computed += profile.cpu_context_switches[j];
	}
	sprintf(buf,"CPU switches%4d",computed / MEMORY);
	ui_text(buf,0,(line++)*Machine->uifontheight);

	/* reset the counters */
	memory = (memory + 1) % MEMORY;
	profile.cpu_context_switches[memory] = 0;
	for (i = 0;i < PROFILER_TOTAL;i++)
		profile.count[memory][i] = 0;

	profiler_mark(PROFILER_END);
}
