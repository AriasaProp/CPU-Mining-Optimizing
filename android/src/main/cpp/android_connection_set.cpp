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
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <netinet/in.h>
#include <unistd.h>

//define socket
int socketFd;
sockaddr_in server_addr;
bool _hasConnection = false;
bool _openConnection(const char *server, const unsigned int port) {
	if (_hasConnection) _closeConnection();
	if(socketFd < 0) {
		if ((socketFd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
			console::write(4, "Failed to create socket");
			return false;
		}
		console::write(2, "Created socket");
	}
	hostent *srv = gethostbyname(server);
  if (!srv) {
		console::write(4, "Failed to resolve hostname");
    return false;
  }
  memset(&server_addr, 0, sizeof(server_addr));
  server_addr.sin_family = AF_INET;
  memcpy(&server_addr.sin_addr.s_addr, srv->h_addr, srv->h_length);
  server_addr.sin_port = htons(port);
	if (connect(socketFd, (sockaddr*) &server_addr, sizeof(server_addr)) < 0) {
		console::write(4, "Failed to connect server");
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