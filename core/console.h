#ifndef Console_Included
#define Console_Included

#include <functional>
#include <mutex>

namespace console {
	std::mutex console_mtx;
	std::function<void(const char *, const unsigned int)> receiveMsg;

	void initialize();
	void write(const unsigned int&, const char *, const unsigned int);
	void destroy();
}

#endif //Console_Included