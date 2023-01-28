#include "console_log.h"

#include <cstdint>
#include <cstdio>
#include <ctime>
#include <cstring>
#include <string>
//2^20 

std::string *htmlMsg;
std::string out;

void console_log::initialize() {
	htmlMsg = new std::string[console_log::MAX_MSG_SIZE];
	write(0,"Wellcome to CPU Mining Opt", 27);
}

const char *console_log::write(unsigned int lv, const char *msg, unsigned long length) {
	char tmsg[8];
  std::time_t t = std::time(0);
  if(!strftime(tmsg, 8, "%T", std::localtime(&t))){
  	memcpy(tmsg, "Error!! ", 8);
  }
	char tx_clr[6];
	switch(lv) {
		default:
		case 0:
			memcpy(tx_clr,"808080",6);//debug
			break;
		case 1:
			memcpy(tx_clr,"0000ff",6);//info
			break;
		case 2:
			memcpy(tx_clr,"00ff00",6);//success
			break;
		case 3:
			memcpy(tx_clr,"ffff00",6);//warning
			break;
		case 4:
			memcpy(tx_clr,"ff0000",6);//Error
			break;
  }
  size_t l_move = 45+length;
  char buff[l_move];
  sprintf(buff, "<font color='#%s'>%s| %s</font>\n", tx_clr, tmsg, msg);
  for (size_t i = console_log::MAX_MSG_SIZE - 1; i > 0; i--) {
  	htmlMsg[i] = htmlMsg[i-1];
  }
  htmlMsg[0] = std::string(buff);
  out = "";
  for (size_t i = 0; i < console_log::MAX_MSG_SIZE; i++) {
  	out += htmlMsg[i];
  }
	return out.c_str();
}

void console_log::destroy() {
	delete htmlMsg;
}
