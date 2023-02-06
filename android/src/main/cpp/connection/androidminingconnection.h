#ifndef _AndroidMiningConnection_Included
#define _AndroidMiningConnection_Included

#include "connection/miningsocket.h"
#include <pthread.h>

struct AndroidSocket: public MiningSocket {
private:
	bool ready;
	bool hasConnection;
	pthread_mutex_t mutex;
	pthread_cond_t cond;
	pthread_t thread;
	sockaddr_in server_addr;
public:
	AndroidSocket();
	~AndroidSocket();
	bool openConnection(const char*, unsigned int&) override;
	bool write(const char*) override;
	char *read() override;
	bool closeConnection() override;
};

#endif //_AndroidMiningConnection_Included