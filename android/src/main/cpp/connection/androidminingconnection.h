#ifndef _AndroidMiningConnection_Included
#define _AndroidMiningConnection_Included

#include "connection/miningsocket.h"
#include <pthread.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <netinet/in.h>
#include <unistd.h>

struct AndroidSocket {
public:
	bool ready;
	bool hasConnection;
	pthread_mutex_t mutex;
	pthread_cond_t cond;
	pthread_t thread;
	sockaddr_in server_addr;
	AndroidSocket();
	~AndroidSocket();
	bool openConnection(const char*, unsigned int&);
	bool write(const char*);
	char *read();
	bool closeConnection();
};

#endif //_AndroidMiningConnection_Included