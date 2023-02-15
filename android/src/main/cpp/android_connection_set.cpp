void _openConnection(const char *, const unsigned short); 
const char *_getMessage(const char*);
bool _sendMessage(const char *);
bool _closeConnection();

namespace function_set {
	//socket connection
	//return false cause error or has connection 
	void (*openConnection) (const char*,const unsigned short) = _openConnection;
	//return message 
	const char*(*getMessage) (const char*) = _getMessage;
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
#include <pthread.h>
#include <vector>

char _msgTemp[512];
static void *recvMessageLoop(void *);

struct sock_data {
	bool running;
	int sock = -1;
	pthread_mutex_t mtx;
	pthread_cond_t cond;
	pthread_t thread;
	
	std::vector<const char*> recv_message;
} *curr;

void _openConnection(const char *server, const unsigned short port) {
  if (!server) throw "Server name is null!";
  if (curr) _closeConnection();
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
  curr = new sock_data;
  curr->sock = socket(AF_INET, SOCK_STREAM, 0);
  if (curr->sock < 0) {
  	delete curr;
  	curr = nullptr;
		sprintf(_msgTemp, "Create socket: %s", strerror(errno));
    throw _msgTemp;
  }
  if (connect(curr->sock, (sockaddr*)ip_address->ai_addr, sizeof(sockaddr)) < 0) {
    sprintf(_msgTemp, "Connect: %s", strerror(errno));
    close(curr->sock);
  	delete curr;
  	curr = nullptr;
    throw _msgTemp;
  }
  freeaddrinfo(ip_address);
  console::write(2, "Connected to server");
  curr->running = true;
  pthread_mutex_init(&curr->mtx, NULL);
  pthread_cond_init(&curr->cond, NULL);
  pthread_attr_t attr; 
  pthread_attr_init(&attr);
  pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
  pthread_create(&curr->thread, &attr, recvMessageLoop, curr);
  pthread_attr_destroy(&attr);
  
}

bool _sendMessage(const char *msg) {
  if (!curr) throw "No connection already!";
  for (unsigned int sended = 0, total = strlen(msg), n; sended < total; sended += n) {
	  n = send(curr->sock, msg + sended, total - sended, 0);
	  if(n < 0) {
	    sprintf(_msgTemp, "Send: %s", strerror(errno));
	    throw _msgTemp;
	  }
  }
  return true;
}
const char *_getMessage(const char *msg) {
	const char *result = nullptr;
  pthread_mutex_lock(&curr->mtx);
  for(std::vector<const char*>::iterator it = curr->recv_message.begin(); it != curr->recv_message.end(); it++) {
  	if (strstr(*it, msg)) {
  		result = *it;
  		curr->recv_message.erase(it);
  		break;
  	}
  }
  pthread_mutex_unlock(&curr->mtx);
	return result;
}
static void *recvMessageLoop(void *args) {
	sock_data *curr_data = (sock_data*)args;
	char _recvBuff[4096];
	bool run_recv = true;
	while (run_recv) {
		int _recv = recv(curr_data->sock, _recvBuff, 4095, 0);
	  if (_recv < 0) {
	    sprintf(_msgTemp, "Receive: %s", strerror(errno));
	    throw _msgTemp;
	  }
	  _recvBuff[_recv] = '\0';
	  pthread_mutex_lock(&curr_data->mtx);
	  char *sv = new char[_recv];
	  memcpy(sv, _recvBuff, _recv);
	  curr_data->recv_message.push_back(sv);
	  run_recv = curr_data->running;
	  pthread_mutex_unlock(&curr_data->mtx);
	} while (run_recv);
	
  pthread_mutex_lock(&curr_data->mtx);
  curr_data->running = true;
  pthread_cond_broadcast(&curr_data->cond);
  pthread_mutex_unlock(&curr_data->mtx);
	return NULL;
}

bool _closeConnection() {
  if(!curr) return false;
  
  pthread_mutex_lock(&curr->mtx);
  curr->running = false;
  while(!curr->running)
  	pthread_cond_wait(&curr->cond,&curr->mtx);
  for(const char *rm : curr->recv_message) {
  	delete rm;
  }
  curr->recv_message.clear();
  pthread_mutex_unlock(&curr->mtx);
  pthread_cond_destroy(&curr->cond);
  pthread_mutex_destroy(&curr->mtx);
  
  close(curr->sock);
  delete curr;
  curr = nullptr;
  console::write(2, "Connection Closed");
  return true;
}

