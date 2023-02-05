#ifndef Console_Included
#define Console_Included

#include <functional>

namespace console {
	void initialize(std::functional<void(const char *, const unsigned int)>);
	void write(const unsigned int&, const char *, const unsigned int);
	void destroy();
}

#endif //Console_Included