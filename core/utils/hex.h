#ifndef _HEX_Included_
#define _HEX_Included_

struct hex {
private:
  unsigned char *value;
  unsigned long size = 0;
  char *out;
  unsigned long out_s = 0;
public:
	//constructors
  hex(const char*);
  hex(const void*, const unsigned long);
	//destructors
  ~hex();
  //enviroment
  unsigned long size() const;
  //append save value
  hex operator+=(const hex&);
  //reassign
  hex operator=(const hex&);
  //cast
  operator int() const;
  operator long() const;
  operator unsigned int() const;
  operator unsigned long() const;
  operator char() const;
  operator unsigned char() const;
  operator float() const;
  operator double() const;
  //to string array
  operator unsigned char*() const;
  operator char*() const;
};

#endif //_HEX_Included_