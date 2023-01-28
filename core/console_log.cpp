#include "console_log.h"

#include <cstdint>
#include <cstdio>
#include <ctime>
#include <cstring>
#include <string>
//2^20 

char *htmlMsg;
char *endHtmlMsg;

void console_log::initialize() {
	htmlMsg = new char[console_log::MAX_MSG_SIZE+1];
	memset(htmlMsg,' ', console_log::MAX_MSG_SIZE);
	endHtmlMsg = htmlMsg+console_log::MAX_MSG_SIZE;
	write(0,"Wellcome to CPU Mining Opt", 27);
}

const char *console_log::write(unsigned int lv, const char *msg, unsigned long length) {
	char tmsg[10];
  std::time_t t = std::time(0);
  if(!strftime(tmsg, 10, "%T", std::localtime(&t))){
  	memcpy(tmsg, "Error!!   ", 10);
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
  size_t l_move = 50+length;
  char buff[l_move];
  sprintf(buff, "<font color='#%s'>%s| %s</font><br>", tx_clr, tmsg, msg);
  memmove(htmlMsg+l_move, htmlMsg, console_log::MAX_MSG_SIZE-l_move);
  memcpy(htmlMsg, buff, l_move);
  
	return htmlMsg;
}

void console_log::destroy() {
	delete htmlMsg;
	endHtmlMsg = nullptr;
}
