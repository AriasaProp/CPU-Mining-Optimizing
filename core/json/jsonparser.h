#ifndef JSON_Parser_Included
#define JSON_Parser_Included

#include <map>
#include <string>

struct json_object {
	bool valid;
	std:map<std::string, std::string> value;
	
	json_object();
	~json_object();
	
	std::string operator[](std::string);
};

json_object json_parser(const char*, const unsigned int length);

#endif //JSON_Parser_Included