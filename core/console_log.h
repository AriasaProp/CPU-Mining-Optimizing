#ifndef Included_Console_Log
#define Included_Console_Log

namespace console_log {
	void initialize();
	const char *write(unsigned int, const char*, unsigned long);
	void destroy();
}

#endif //Included_Console_Log