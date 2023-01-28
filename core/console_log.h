#ifndef Included_Console_Log
#define Included_Console_Log

#include <cstring>
#include <string>

namespace console_log {
	const unsigned int MAX_MSG_SIZE = 1048576;
	void initialize();
	std::string write(unsigned int, const char*, unsigned long);
	void destroy();
}

#endif //Included_Console_Log