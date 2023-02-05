#ifndef _AndroidMiningConnection_Included
#define _AndroidMiningConnection_Included

#include "connection/miningsocket.h"

struct AndroidSocket: public MiningSocket {
	AndroidSocket();
	~AndroidSocket();
	bool openConnection(const char*, unsigned int&) override;
	bool write(const char*) override;
	char *read(const char*) override;
	bool closeConnection() override;
};

#endif //_AndroidMiningConnection_Included