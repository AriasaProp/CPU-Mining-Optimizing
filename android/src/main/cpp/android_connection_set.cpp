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
#include <cstring>
#include <cerrno>
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <boost/asio.hpp>
#include <boost/array.hpp>


bool _hasConnection = false;
boost::asio::io_context io_context;
boost::system::error_code error;
boost::array<char, 1024> response_buf;
tcp::socket socket = 0;
void _openConnection(const char *server, const unsigned short port) {
  if (!server) throw "Server name is null!";
  if (_hasConnection) _closeConnection();
  tcp::resolver resolver(io_context);
  tcp::resolver::results_type endpoints = resolver.resolve(server, std::to_string(port));
  socket = tcp::socket(io_context);
  boost::asio::connect(socket, endpoints, error);
  if (error) {
    socket = 0;
    throw error.message().c_str();
  }
  console::write(2, "Connected to server");
  _hasConnection = true;
}

const char *_recvConnection() {
  if (!_hasConnection) throw "No connection already!";
  size_t response_length = socket.read_some(boost::asio::buffer(response_buf), error);
  if (error)
    throw error.message().c_str();
  return response_buf.data();
}

bool _sendMessage(const char *msg) {
  if (!_hasConnection) throw "No connection already!";
  boost::asio::write(socket, boost::asio::buffer(msg), error);
  if (error)
    throw error.message().c_str();
  return true;
}

bool _closeConnection() {
  if(!_hasConnection) return false;
  socket.close(error);
  if (error) {
    console::write(4, error.message().c_str());
    return false;
  }
  socket = 0;
  _hasConnection = false;
  console::write(2, "Connection Closed");
  return true;
}
