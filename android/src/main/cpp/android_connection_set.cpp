void _openConnection(const char *, const unsigned short); 
const char *_getMessage();
bool _sendMessage(const char *);
bool _closeConnection();

namespace function_set {
	//socket connection
	//return false cause error or has connection 
	void (*openConnection) (const char*,const unsigned short) = _openConnection;
	//return message 
	const char*(*getMessage) () = _getMessage;
	//send message
	bool (*sendMessage)(const char *) = _sendMessage;
	//return false cause error or no connection 
	bool (*closeConnection) () = _closeConnection;
}
#include "console.h"
#include <cstdio>
#include <cerrno>
#include <iostream>
#include <cstring>
#include <unistd.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <netdb.h>
#include <netinet/in.h>
#include <arpa/inet.h>

bool hasConnection = false;
char _msgTemp[512];
static void *recvMessageLoop(void *);
int sock = -1;

void _openConnection(const char *server, const unsigned short port) {
  if (!server) throw "Server name is null!";
  if (hasConnection) _closeConnection();
  //IP convertion
  addrinfo *ip_address;
  {
	  addrinfo hints;
	  memset(&hints, 0, sizeof(hints));
	  hints.ai_family = AF_INET;
	  hints.ai_socktype = SOCK_STREAM;
	  char port_str[6];
		sprintf(port_str, "%u", port);
	  int status = getaddrinfo(server, port_str, &hints, &ip_address);
	  if (status != 0) {
	    sprintf(_msgTemp, "Address conv: %s", gai_strerror(status));
	    throw _msgTemp;
	  }
  }
  sock = socket(AF_INET, SOCK_STREAM, 0);
  if (sock < 0) {
		sprintf(_msgTemp, "Create socket: %s", strerror(errno));
    throw _msgTemp;
  }
  if (connect(sock, (sockaddr*)ip_address->ai_addr, sizeof(sockaddr)) < 0) {
    sprintf(_msgTemp, "Connect: %s", strerror(errno));
    close(sock);
    sock = -1;
    throw _msgTemp;
  }
  freeaddrinfo(ip_address);
  console::write(2, "Connected to server");
  hasConnection = true;
}

bool _sendMessage(const char *msg) {
  if (!hasConnection) throw "No connection already!";
  for (unsigned int sended = 0, total = strlen(msg), n; sended < total; sended += n) {
	  n = send(sock, msg + sended, total - sended, 0);
	  if(n < 0) {
	    sprintf(_msgTemp, "Send: %s", strerror(errno));
	    throw _msgTemp;
	  }
  }
  return true;
}
char _recvBuff[4096];
const char *_getMessage() {
	if(!hasConnection) throw "No connection already";
	_recv = recv(sock, _recvBuff, 4095, 0);
  if (_recv < 0) {
    sprintf(_msgTemp, "Receive: %s", strerror(errno));
    throw _msgTemp;
	} else {
	  _recvBuff[_recv] = '\0';
	}
	return _recvBuff;
}
bool _closeConnection() {
  if(!hasConnection) return false;
  close(sock);
  sock = -1;
  console::write(2, "Connection Closed");
  hasConnection = false;
  return true;
}

