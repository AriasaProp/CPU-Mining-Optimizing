#include "hex.h"
#include <cstring>

//constructors
hex::hex(const char* d) {
  size = strlen(d)/2;
  value = new unsigned char[size];
	for (int i = 0; i < size; ++i) {
    char h = *(d++);
    unsigned char &v = value[i];
    // convert hex char to value
    if (h >= '0' && h <= '9') {
        v = h - '0';
    } else if (h >= 'a' && h <= 'f') {
        v = h - 'a' + 10;
    }
    v <<= 4;
    // convert hex char to value 2
    h = *(d++);
    // combine two values into one unsigned char
    if (h >= '0' && h <= '9') {
        v |= h - '0';
    } else if (h >= 'a' && h <= 'f') {
        v |= h - 'a' + 10;
    }
	}
}

hex::hex(const void *d, const unsigned long l): size(l) {
	value = new unsigned char[size];
	memcpy(value, d, size);
}
//destructors
hex::~hex() {
	delete[] value;
	if (out) delete[] out;
}
//enviroment
unsigned long hex::size() const {
	return size;
}
//append value
hex hex::operator+(const hex &other) {
	unsigned long new_size = size+other.size;
	unsigned char new_value[new_size];
	memcpy(new_value, value, size);
	memcpy(new_value+size, other.value, other.size);
	return hex(new_value, new_size);
}
//append save value
hex hex::operator+=(const hex &other) {
	unsigned long new_size = size+other.size;
	unsigned char *new_value = new unsigned char[new_size];
	memcpy(new_value, value, size);
	memcpy(new_value+size, other.value, other.size);
	delete[] value;
	value = new_value;
	size = new_size;
	return *this;
}
//reassign
hex hex::operator=(const hex &other) {
	if (other.size != size) {
		delete[] value;
		value = new unsigned char[other.size];
		size = other.size;
	}
	memcpy(value, other.value, size);
	return *this;
}
//cast
hex::operator int() const {
	int res;
	memcpy(&res, value, sizeof(int));
	return res;
}
hex::operator long() const {
	long res;
	memcpy(&res, value, sizeof(long));
	return res;
}
hex::operator unsigned int() const {
	unsigned int res;
	memcpy(&res, value, sizeof(unsigned int));
	return res;
}
hex::operator unsigned long() const {
	unsigned long res;
	memcpy(&res, value, sizeof(unsigned long));
	return res;
}
hex::operator char() const {
	char res;
	memcpy(&res, value, sizeof(char));
	return res;
}
hex::operator unsigned char() const {
	return *value;
}
hex::operator float() const {
	float res;
	memcpy(&res, value, sizeof(float));
	return res;
}
hex::operator double() const {
	double res;
	memcpy(&res, value, sizeof(double));
	return res;
}
//to string array
hex::hex::operator unsigned char*() const {
	return value;
}
const char *hex_num = "0123456789abcdef";
hex::hex::operator char*() const {
	if (out_s != size*2) {
		if (out) delete[] out;
		out_s = size*2;
		out = new char[out_s];
	}
	char *c = out;
	unsigned char *v = value, *vend = value + size;
	while (v != vend) {
		*(c++) = hex_num[(*v >> 4) & 15];
		*(c++) = hex_num[*v & 15];
    v++;
  }
	return out;
}
