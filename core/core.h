#ifndef _Included_Core_
#define _Included_Core_

#include <functional>


struct core {
public:
	std::function<void(bool)> afterCall;
	void startMining();
	void stopMining();
private:
	void miningThread();
};

#endif //_Included_Core_