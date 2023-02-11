
void _openConnection(const char *, const unsigned int); 
const char *_recvConnection();
bool _sendMessage(const char *);
bool _closeConnection();

namespace function_set {
	//socket connection
	//return false cause error or has connection 
	void (*openConnection) (const char*,const unsigned int) = _openConnection;
	//return message 
	const char*(*recvConnection) () = _recvConnection;
	//send message
	bool (*sendMessage)(const char *) = _sendMessage;
	//return false cause error or no connection 
	bool (*closeConnection) () = _closeConnection;
}
#include "console.h"
#include <cstdio>
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
void _openConnection(const char *server, const unsigned int port) {
	if (!server) throw "Server name is null!";
	if (_hasConnection) _closeConnection();
  server_addr.sin_family = AF_INET;
  //server_addr.sin_addr.s_addr = inet_addr(server);
  server_addr.sin_port = htons(port);
  //made ip address from host name
  hostent *he = gethostbyname(server);
  if (he == NULL) {
  	sprintf(_tempMsg, "Hostname : %s", strerror(errno));
		throw _tempMsg;
  }
  in_addr **addr_list = (in_addr **) he->h_addr_list;
  for (unsigned int i = 0;;i++) {
  	if (addr_list[i] == NULL) throw "Failed to convert hostname to IP address";
	  if (inet_pton(AF_INET, inet_ntoa(*addr_list[i]), &server_addr.sin_addr.s_addr) > 0)
	  	break;
    sprintf(_tempMsg, "Servername[%d]: %s", i, strerror(errno));
		console::write(0, _tempMsg);
  }
  for (unsigned int i = 0;;i++) {
	  if(socketFd < 0) {
			if ((socketFd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
				sprintf(_tempMsg, "Socket: %s", strerror(errno));
				throw _tempMsg;
			}
			console::write(0, "Created socket");
		}
		if (connect(socketFd, (sockaddr*) &server_addr, sizeof(server_addr)) < 0) {
			strcpy(_tempMsg, "Connect: ");
			switch (errno) {
				case EBADF:
					close(socketFd);
					socketFd = -1;
					console::write(0, "Bad socket file descriptor");
					if (i < 3)
						continue;
					strcat(_tempMsg, "The socket parameter is not a valid socket descriptor");
					break;
				case ETIMEDOUT:
					close(socketFd);
					console::write(0, "Connection timeout");
					if (i < 3) {
						sleep(1);
						continue;
					}
					strcat(_tempMsg, "The connection establishment timed out before a connection was made");
					break;
				case EADDRNOTAVAIL:
					strcat(_tempMsg, "The specified address is not available from the local machine");
					break;
				case EAFNOSUPPORT:
					strcat(_tempMsg, "The address family is not supported");
					break;
				case EALREADY:
					strcat(_tempMsg, "The socket descriptor socket is marked nonblocking, and a previous connection attempt has not completed");
					break;
				case ECONNREFUSED:
					strcat(_tempMsg, "The connection request was rejected by the destination host");
					break;
				case EFAULT:
					strcat(_tempMsg, "Using address and address_len would result in an attempt to copy the address into a portion of the caller's address space to which data cannot be written");
					break;
				case EINTR:
					strcat(_tempMsg, "The attempt to establish a connection was interrupted by delivery of a signal that was caught. The connection will be established asynchronously");
					break;
				case EINVAL:
					strcat(_tempMsg, "The address_len parameter is not a valid length");
					break;
				case EIO:
					strcat(_tempMsg, "There has been a network or a transport failure");
					break;
				case ENETUNREACH:
					strcat(_tempMsg, "The network cannot be reached from this host");
					break;
				case EPROTOTYPE:
					strcat(_tempMsg, "The protocol is the wrong type for this socket");
					break;
				default:
					strcat(_tempMsg, strerror(errno));
					break;
			}
			strcat(_tempMsg, ".\0");
			throw _tempMsg;
		}
  }
	console::write(2, "Connected to server");
	_hasConnection = true;
}
char recvBuffer[2049];
const char *_recvConnection() {
	if (!_hasConnection) return "No connection already!";
	int length = recv(socketFd, recvBuffer, 2048, MSG_WAITALL);
	if (length > 0) {
		recvBuffer[length] = '\0';
		return recvBuffer;
	} else if(length < 0) {
		sprintf(_tempMsg, "Recv: %s", strerror(errno));
		console::write(4, _tempMsg);
	}
	return " ";
}
bool _sendMessage(const char *msg) {
	if (!_hasConnection) return false;
	for (int sent = 0, total = strlen(msg), n; sent < total; sent += n) {
    n = send(socketFd, msg + sent, total - sent, 0);
    if (n == -1) {
    	sprintf(_tempMsg, "Send: %s", strerror(errno));
			console::write(4, _tempMsg);
	    return false;
    }
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
