
bool _openConnection(const char *, const unsigned int); 
const char *_recvConnection();
bool _sendMessage(const char *);
bool _closeConnection();

namespace function_set {
	//socket connection
	//return false cause error or has connection 
	bool (*openConnection) (const char*,const unsigned int) = _openConnection;
	//return message 
	const char*(*recvConnection) () = _recvConnection;
	//send message
	bool (*sendMessage)(const char *) = _sendMessage;
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
char _tempMsg[256];
int socketFd = -1;
sockaddr_in server_addr;
bool _hasConnection = false;
bool _openConnection(const char *server, const unsigned int port) {
	if (_hasConnection) _closeConnection();
	if(socketFd < 0) {
		if ((socketFd = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
			strcpy(_tempMsg, "Error create socket: ");
			strcat(_tempMsg, strerror(errno));
			strcat(_tempMsg, ".\0");
			console::write(4, _tempMsg);
			return false;
		}
		console::write(0, "Created socket");
	}
  server_addr.sin_family = AF_INET;
  //server_addr.sin_addr.s_addr = inet_addr(server);
  server_addr.sin_port = htons(port);
  //made ip address from host name
  struct hostent *he = gethostbyname(server);
  struct in_addr **addr_list;
  if (he == NULL) {
  	strcpy(_tempMsg, "Hostname : ");
		strcat(_tempMsg, strerror(errno));
		strcat(_tempMsg, ".\0");
		console::write(4, _tempMsg);
		return false;
  }
  addr_list = (struct in_addr **) he->h_addr_list;
  char* ipAddress = nullptr;
  for (unsigned int i = 0; addr_list[i] != NULL; i++) {
    ipAddress = inet_ntoa(*addr_list[i]);
  }
  if (inet_pton(AF_INET, ipAddress, &server_addr.sin_addr.s_addr) <= 0) {
    strcpy(_tempMsg, "Servername : ");
		strcat(_tempMsg, strerror(errno));
		strcat(_tempMsg, ".\0");
		console::write(4, _tempMsg);
		return false;
  }
	if (connect(socketFd, (sockaddr*) &server_addr, sizeof(server_addr)) < 0) {
		strcpy(_tempMsg, "Error: ");
		strcat(_tempMsg, strerror(errno));
		strcat(_tempMsg, ".\0");
		console::write(4, _tempMsg);
		return false;
	}
	console::write(2, "Connected to server");
	_hasConnection = true;
	return true;
}
char recvBuffer[2048];
const char *_recvConnection() {
	if (!_hasConnection) return "No connection already!";
	for(unsigned int i = 0; i < 3; i++) {
		unsigned int length = recv(socketFd, recvBuffer, 2048, 0);
		if (length) {
			recvBuffer[length] = '\0';
			return recvBuffer;
		}
		strcpy(_tempMsg, "Recv: ");
		strcat(_tempMsg, strerror(errno));
		strcat(_tempMsg, ".\0");
		console::write(4, _tempMsg);
		sleep(3); //sleep for 3 seconds
	}
	return "No such message!";
}
bool _sendMessage(const char *msg) {
	if(!_hasConnection) return false;
	if (send(socketFd, msg, strlen(msg), 0) < 0) {
		strcpy(_tempMsg, "Send: ");
		strcat(_tempMsg, strerror(errno));
		strcat(_tempMsg, ".\0");
		console::write(4, _tempMsg);
    return false;
	}
  return true;
}
bool _closeConnection() {
	if(!_hasConnection) return false;
	
	close(socketFd);
	_hasConnection = false;
	console::write(2, "Connection Closed");
	return true;
}
