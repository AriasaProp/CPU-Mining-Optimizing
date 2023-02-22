void _openConnection(const char *, const unsigned short); 
char *_getMessage();
bool _sendMessage(const char *);
bool _closeConnection();

namespace function_set {
	//socket connection
	//return false cause error or has connection 
	void (*openConnection) (const char*,const unsigned short) = _openConnection;
	//return message 
	char*(*getMessage) () = _getMessage;
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
char _msgTemp[1024];
int sock = -1;
void _openConnection(const char *server, const unsigned short port) {
  if (!server) throw "Server name is null!";
  if (hasConnection) _closeConnection();
  // IP conversion
  addrinfo hints, *res, *p;
  memset(&hints, 0, sizeof(hints));
  hints.ai_family = AF_INET; // IP version
  hints.ai_socktype = SOCK_STREAM; // TCP
  char port_str[6]; // as long as for store 65536
  sprintf(port_str, "%u", port);
  int status = getaddrinfo(server, port_str, &hints, &res);
  if (status != 0) {
    sprintf(_msgTemp, "Address conv: %s", gai_strerror(status));
    throw _msgTemp;
  }
  for (p = res; p; p = p->ai_next) {
    sock = socket(p->ai_family, p->ai_socktype, p->ai_protocol);
    if (sock < 0)
      continue;
    if (connect(sock, p->ai_addr, p->ai_addrlen) < 0) {
      close(sock);
      sock = -1;
      continue;
    }
    break;
  }
  freeaddrinfo(res);
  if (!p) {
  	sprintf(_msgTemp, "Can't connect cause %s", strerror(errno))
    throw _msgTemp;
  }
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
char *_getMessage() {
	if(!hasConnection) throw "No connection already";
	int bytesReceived = 0;
  while (true) {
    int bytes = recv(sock, _recvBuff + bytesReceived, sizeof(_recvBuff) - bytesReceived - 1, 0);
    if (bytes < 0) {
      sprintf(_msgTemp, "Receive: %s", strerror(errno));
      throw _msgTemp;
    } else if (bytes == 0) {
      break;
    }
    bytesReceived += bytes;
    if (_recvBuff[bytesReceived - 1] == '\n') {
      break;
    }
  }
  _recvBuff[bytesReceived] = '\0';
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

