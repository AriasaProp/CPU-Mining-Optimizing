#ifndef Included_Console_Log
#define Included_Console_Log

namespace console_log {
	const unsigned int MAX_MSG_SIZE = 1048576
	void initialize();
	char *write(unsigned int, const char*, unsigned long) const;
	void destroy();
}

#endif //Included_Console_Log