void _openConnection(const char *, const unsigned short); 
const char *_recvConnection();
bool _sendMessage(const char *);
bool _closeConnection();

namespace function_set {
	//socket connection
	//return false cause error or has connection 
	void (*openConnection) (const char*,const unsigned short) = _openConnection;
	//return message 
	const char*(*recvConnection) () = _recvConnection;
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

bool _hasConnection = false;
int sock = -1;
char _msgTemp[512];
void _openConnection(const char *server, const unsigned short port) {
  if (!server) throw "Server name is null!";
  if (_hasConnection) _closeConnection();
  //IP convertion
  addrinfo hints, *ip_address;
  memset(&hints, 0, sizeof(hints));
  hints.ai_family = AF_INET;
  hints.ai_socktype = SOCK_STREAM;
  int status = getaddrinfo(server, port, &hints, &ip_address);
  if (status != 0) {
    sprintf(_msgTemp, "Address conv: %s", strerror(status));
    throw _msgTemp;
  }
  sock = socket(AF_INET, SOCK_STREAM, 0);
  if (sock < 0) {
		sprintf(_msgTemp, "Create socket: %s", strerror(errno));
    throw _msgTemp;
  }
  if (connect(sock, (sockaddr*)ip_address->ai_addr, sizeof(sockaddr)) < 0) {
    sprintf(_msgTemp, "Connect: %s", strerror(errno));
    close(sock);
    throw _msgTemp;
  }
  freeaddrinfo(ip_address);
  console::write(2, "Connected to server");
  _hasConnection = true;
}
char _recvBuff[1025];
const char *_recvConnection() {
  if (!_hasConnection) throw "No connection already!";
  int received_bytes = recv(sock, _recvBuff, 1024, MSG_WAITALL);
  if (received_bytes < 0) {
    sprintf(_msgTemp, "Receive: %s", strerror(errno));
    close(sock);
    throw _msgTemp;
  }
  _recvBuff[received_bytes] = '\0';
  return _recvBuff;
}
bool _sendMessage(const char *msg) {
  if (!_hasConnection) throw "No connection already!";
  for (unsigned int sended = 0, total = strlen(msg), n; sended < total; sended += n) {
	  n = send(sock, msg + sended, total - sended, 0);
	  if(n < 0) {
	    sprintf(_msgTemp, "Send: %s", strerror(errno));
	    close(sock);
	    throw _msgTemp;
	  }
  }
  return true;
}
bool _closeConnection() {
  if(!_hasConnection) return false;
  close(sock);
  sock = -1;
  _hasConnection = false;
  console::write(2, "Connection Closed");
  return true;
}

