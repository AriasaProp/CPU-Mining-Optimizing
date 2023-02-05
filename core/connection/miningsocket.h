#ifndef _MiningSocket_Included
#define _MiningSocket_Included

struct MiningSocket {
	virtual bool openConnection(const char*, unsigned int&) = 0;
	virtual bool write(const char*) = 0;
	virtual char *read() = 0;
	virtual bool closeConnection() = 0;
};


#endif //_MiningSocket_Included