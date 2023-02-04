#ifndef _Included_Core_
#define _Included_Core_

#include <functional>


namespace core {
	void startMining(std::function<void()>);
	void stopMining(std::function<void()>);
}

#endif //_Included_Core_