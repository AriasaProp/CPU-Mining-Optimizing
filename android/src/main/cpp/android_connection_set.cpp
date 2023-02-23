void _openConnection(const char *, const unsigned short); 
const char *_getMessage();
void _sendMessage(const char *);
bool _closeConnection();

namespace function_set {
	//socket connection
	//return false cause error or has connection 
	void (*openConnection) (const char*,const unsigned short) = _openConnection;
	//return message 
	const char*(*getMessage) () = _getMessage;
	//send message
	void (*sendMessage)(const char *) = _sendMessage;
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
  //IP convertion
	addrinfo *res, *p;
	{
		addrinfo hints;
	  memset(&hints, 0, sizeof(hints));
	  hints.ai_family = AF_UNSPEC; // IP not spesific (IPv4 or IPv6)
	  hints.ai_socktype = SOCK_STREAM; // TCP
		sprintf(_msgTemp, "%u", port);
	  int status = getaddrinfo(server, _msgTemp, &hints, &res);
	  if (status != 0) {
	    sprintf(_msgTemp, "Address conv: %s", gai_strerror(status));
	    throw _msgTemp;
	  }
	}
  for (p = res; p != NULL; p = p->ai_next) {
	  if ((sock = socket(p->ai_family, p->ai_socktype, p->ai_protocol)) == -1) {
			sprintf(_msgTemp, "Create socket: %s", strerror(errno));
	    console::write(4, _msgTemp);
	    continue;
	  }
	  if (connect(sock, p->ai_addr, p->ai_addrlen) != -1)
	  	break;
    sprintf(_msgTemp, "Connect: %s", strerror(errno));
    console::write(4, _msgTemp);
    close(sock);
  	sock = -1;
  }
	freeaddrinfo(res);
	if (p == NULL) throw "failed make connection";
  console::write(2, "Connected to server");
  hasConnection = true;
}
void _sendMessage(const char *msg) {
  if (!hasConnection) throw "No connection already!";
  const char *const end = msg + strlen(msg); 
  while (msg != end) {
    size_t n = send(sock, msg, size_t(end - msg), 0);
    if (n < 0) {
      sprintf(_msgTemp, "Send: %s", strerror(errno));
      throw _msgTemp;
    }
    memcpy(_msgTemp, msg, n);
    msg += n;
		console::write(0, _msgTemp);
  }
}
char _recvBuff[4096];
const char *_getMessage() {
	if(!hasConnection) throw "No connection already";
	int _recv = recv(sock, _recvBuff, 4095, 0);
  if (_recv < 0) {
    sprintf(_msgTemp, "Receive: %s", strerror(errno));
    throw _msgTemp;
	}
	_recvBuff[_recv] = '\0';
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
