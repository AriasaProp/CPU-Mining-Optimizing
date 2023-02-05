#ifndef _AndroidMiningConnection_Included
#define _AndroidMiningConnection_Included

#include "connection/miningsocket.h"
#include <pthread.h>

struct AndroidSocket: public MiningSocket {
	pthread_mutex_t mutex;
	pthread_cond_t cond;
	pthread_t thread;
	AndroidSocket();
	~AndroidSocket();
	void *socketLoop(void*);
	bool openConnection(const char*, unsigned int&) override;
	bool write(const char*) override;
	char *read() override;
	bool closeConnection() override;
};

#endif //_AndroidMiningConnection_Included