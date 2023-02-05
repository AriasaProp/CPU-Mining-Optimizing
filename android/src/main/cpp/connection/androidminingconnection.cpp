#include "androidminingconnection.h"

#include <iostream>
#include <string>
#include <cstring>
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <netinet/in.h>
#include <unistd.h>

bool hasSocket;
bool hasConnection;

sockaddr_in server_addr;

static void *socketLoop(void*);

AndroidSocket::AndroidSocket() {
	hasSocket = false;
  pthread_mutex_init(&mutex, NULL);
  pthread_cond_init(&cond, NULL);
  pthread_attr_t attr; 
  pthread_attr_init(&attr);
  pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
  pthread_create(&thread, &attr, socketLoop, 0);
  pthread_attr_destroy(&attr);
}

AndroidSocket::~AndroidSocket() {
  pthread_mutex_lock(&mutex);
  while (hasSocket)
  	pthread_cond_wait(&cond, &mutex);
  pthread_mutex_unlock(&mutex);
  pthread_cond_destroy(&cond);
  pthread_mutex_destroy(&mutex);
}

bool AndroidSocket::openConnection(const char *server, unsigned int &port) {
	
	hostent *srv = gethostbyname(server);
  if (!srv) {
    std::cerr << "Error: failed to resolve hostname" << std::endl;
    return false;
  }
  memset(&server_addr, 0, sizeof(server_addr));
  server_addr.sin_family = AF_INET;
  memcpy(&server_addr.sin_addr.s_addr, srv->h_addr, srv->h_length);
  server_addr.sin_port = htons(port);
  pthread_mutex_lock(&mutex);
  while (!hasSocket) {
  	pthread_cond_wait(&cond, &mutex);
  }
  hasConnection = true;
  pthread_cond_broadcast(&cond);
  pthread_mutex_unlock(&mutex);
  
	return true;
}
bool AndroidSocket::write(const char*) {
	
	return 0;
}
char *AndroidSocket::read() {
	
	return 0;
}
bool AndroidSocket::closeConnection() {
	
	return 0;
}

static void *socketLoop(void*) {
	//try make socket
	int sockfd;
	while ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0)
    std::cerr << "Socket: failed to create socket" << std::endl;
  pthread_mutex_lock(&mutex);
  hasSocket = true;
  hasConnection = false;
	pthread_cond_broadcast(&cond);
  pthread_mutex_unlock(&mutex);
	//loop connection
	for(;;) {
		//wait till has broadcast somethime
  	pthread_cond_wait(&cond, &mutex);
		//loop if has connection request
		while (hasConnection) {
			while (connect(sockfd, (struct sockaddr*) &server_addr, sizeof(server_addr)) < 0)
		    std::cerr << "Socket: failed to connect server" << std::endl;
		  
		  
		  
		}
	  //close connection
	  close(sockfd);
	}
	//try destroy socket
  pthread_mutex_lock(&mutex);
  hasSocket = false;
	pthread_cond_broadcast(&cond);
  pthread_mutex_unlock(&mutex);
  return NULL;
}

