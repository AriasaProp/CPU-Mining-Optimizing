#include "console_log.h"

#include <cstdint>
#include <cstdio>
#include <cstring>
#include <string>
#include <ctime>
//2^20 

char *htmlMsg;
char *endHtmlMsg;

void console_log::initialize() {
	htmlMsg = new char[console_log::MAX_MSG_SIZE+1];
	memset(htmlMsg, ' ', console_log::MAX_MSG_SIZE);
	endHtmlMsg = htmlMsg+console_log::MAX_MSG_SIZE;
	*endHtmlMsg = '\0';
	write(0,"Wellcome to CPU Mining Opt", 27)
}

char *console_log::write(unsigned int lv, const char *msg, unsigned long length) {
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
  size_t l_move = 41+length;
  char buff[l_move];
  sprintf(buff, "<font color='#%s'>%s| %s</font>\n", tx_clr, tmsg, msg);
  size_t l_cur = console_log::MAX_MSG_SIZE - l_move;
  memmove(htmlMsg+l_move, htmlMsg, l_cur);
  memcpy(htmlMsg, buff, l_move);
  char *findLastMsg = strstr(htmlMsg, "<f");
  char *footMsg = strstr(findLastMsg, "</font>\n");
  if (footMsg) {
  	size_t pivot = endHtmlMsg-footMsg-8;
  	if (pivot) {
  		findLastMsg = footMsg+8;
  	}
  }
	memset(findLastMsg, ' ', (endHtmlMsg-findLastMsg)-1);
	return htmlMsg;
}

void console_log::destroy() {
	delete htmlMsg;
	endHtmlMsg = nullptr;
}
