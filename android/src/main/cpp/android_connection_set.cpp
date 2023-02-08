bool _openConnection(const char *, const unsigned int); 
bool _closeConnection();

namespace function_set {
	//socket connection
	//return false cause error or has connection 
	bool (*openConnection) (const char*,const unsigned int) = _openConnection;
	//return false cause error or no connection 
	bool (*closeConnection) () = _closeConnection;
}
#include "console.h"
#include <cstring>
#include <cerrno>
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>

//define socket
int socketFd = -1;
sockaddr_in server_addr;
bool _hasConnection = false;
bool _openConnection(const char *server, const unsigned int port) {
	if (_hasConnection) _closeConnection();
	if(socketFd < 0) {
		if ((socketFd = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
			console::write(4, "Failed to create socket");
			console::write(4, strerror(errno));
			return false;
		}
		console::write(2, "Created socket");
	}
  server_addr.sin_family = AF_INET;
  server_addr.sin_addr.s_addr = inet_addr(server);
  server_addr.sin_port = htons(port);
	if (connect(socketFd, (sockaddr*) &server_addr, sizeof(server_addr)) == -1) {
		char _tempMsg[512];
		strcpy(_tempMsg, "Error connect server cause: ");
		strcat(_tempMsg, strerror(errno));
		strcat(_tempMsg, " then ");
		strcat(_tempMsg, strerror(errno));
		strcat(_tempMsg, '\0');
		console::write(4, _tempMsg);
		return false;
	}
	console::write(2, "Connected to server");
	_hasConnection = true;
	return true;
}
bool _closeConnection() {
	if(socketFd < 0 || !_hasConnection) return false;
	
	close(socketFd);
	_hasConnection = false;
	console::write(2, "Connection Closed");
	return true;
}