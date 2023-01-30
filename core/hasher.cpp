#include "hasher.h"

#include <cstdint>
#include <cstring>

unsigned int *tSl;
unsigned int *tV;

void hasher::initialize() {
	//nothing todo for now 
	tSl = new unsigned int[15];
	tV = new unsigned int[32 * 1024];
}

void xorSalsa(unsigned int *X){
	size_t i;
	for (i = 0; i < 16;i++) {
		tSl[i] = (X[i] ^= X[16 + i]);
	}
    for (i = 0; i < 4; i++) {
      tSl[4] ^= (tSl[0] + tSl[12]) << 7;
      tSl[8] ^= (tSl[4] + tSl[0]) << 9;
      tSl[12] ^= (tSl[8] + tSl[4]) << 13;
      tSl[0] ^= (tSl[12] + tSl[8]) << 18;
      tSl[9] ^= (tSl[5] + tSl[1]) << 7;
      tSl[13] ^= (tSl[9] + tSl[5]) << 9;
      tSl[1] ^= (tSl[13] + tSl[9]) << 13;
      tSl[5] ^= (tSl[1] + tSl[13]) << 18;
      tSl[14] ^= (tSl[10] + tSl[6]) << 7;
      tSl[2] ^= (tSl[14] + tSl[10]) << 9;
      tSl[6] ^= (tSl[2] + tSl[14]) << 13;
      tSl[10] ^= (tSl[6] + tSl[2]) << 18;
      tSl[3] ^= (tSl[15] + tSl[11]) << 7;
      tSl[7] ^= (tSl[3] + tSl[15]) << 9;
      tSl[11] ^= (tSl[7] + tSl[3]) << 13;
      tSl[15] ^= (tSl[11] + tSl[7]) << 18;
      tSl[1] ^= (tSl[0] + tSl[3]) << 7;
      tSl[2] ^= (tSl[1] + tSl[0]) << 9;
      tSl[3] ^= (tSl[2] + tSl[1]) << 13;
      tSl[0] ^= (tSl[3] + tSl[2]) << 18;
      tSl[6] ^= (tSl[5] + tSl[4]) << 7;
      tSl[7] ^= (tSl[6] + tSl[5]) << 9;
      tSl[4] ^= (tSl[7] + tSl[6]) << 13;
      tSl[5] ^= (tSl[4] + tSl[7]) << 18;
      tSl[11] ^= (tSl[10] + tSl[9]) << 7;
      tSl[8] ^= (tSl[11] + tSl[10]) << 9;
      tSl[9] ^= (tSl[8] + tSl[11]) << 13;
      tSl[10] ^= (tSl[9] + tSl[8]) << 18;
      tSl[12] ^= (tSl[15] + tSl[14]) << 7;
      tSl[13] ^= (tSl[12] + tSl[15]) << 9;
      tSl[14] ^= (tSl[13] + tSl[12]) << 13;
      tSl[15] ^= (tSl[14] + tSl[13]) << 18;
    }
	for (i = 0; i < 16;i++) {
		X[i] += tSl[i];
		//then second threat
		tSl[i] = (X[i+16] ^= X[i]);
	}
    for (i = 0; i < 4; i++) {
      tSl[4] ^= (tSl[0] + tSl[12]) << 7;
      tSl[8] ^= (tSl[4] + tSl[0]) << 9;
      tSl[12] ^= (tSl[8] + tSl[4]) << 13;
      tSl[0] ^= (tSl[12] + tSl[8]) << 18;
      tSl[9] ^= (tSl[5] + tSl[1]) << 7;
      tSl[13] ^= (tSl[9] + tSl[5]) << 9;
      tSl[1] ^= (tSl[13] + tSl[9]) << 13;
      tSl[5] ^= (tSl[1] + tSl[13]) << 18;
      tSl[14] ^= (tSl[10] + tSl[6]) << 7;
      tSl[2] ^= (tSl[14] + tSl[10]) << 9;
      tSl[6] ^= (tSl[2] + tSl[14]) << 13;
      tSl[10] ^= (tSl[6] + tSl[2]) << 18;
      tSl[3] ^= (tSl[15] + tSl[11]) << 7;
      tSl[7] ^= (tSl[3] + tSl[15]) << 9;
      tSl[11] ^= (tSl[7] + tSl[3]) << 13;
      tSl[15] ^= (tSl[11] + tSl[7]) << 18;
      tSl[1] ^= (tSl[0] + tSl[3]) << 7;
      tSl[2] ^= (tSl[1] + tSl[0]) << 9;
      tSl[3] ^= (tSl[2] + tSl[1]) << 13;
      tSl[0] ^= (tSl[3] + tSl[2]) << 18;
      tSl[6] ^= (tSl[5] + tSl[4]) << 7;
      tSl[7] ^= (tSl[6] + tSl[5]) << 9;
      tSl[4] ^= (tSl[7] + tSl[6]) << 13;
      tSl[5] ^= (tSl[4] + tSl[7]) << 18;
      tSl[11] ^= (tSl[10] + tSl[9]) << 7;
      tSl[8] ^= (tSl[11] + tSl[10]) << 9;
      tSl[9] ^= (tSl[8] + tSl[11]) << 13;
      tSl[10] ^= (tSl[9] + tSl[8]) << 18;
      tSl[12] ^= (tSl[15] + tSl[14]) << 7;
      tSl[13] ^= (tSl[12] + tSl[15]) << 9;
      tSl[14] ^= (tSl[13] + tSl[12]) << 13;
      tSl[15] ^= (tSl[14] + tSl[13]) << 18;
    }
	for (i = 0; i < 16;i++) {
		X[i+16] += tSl[i];
	}
}

void hasher::hash(void *B, unsigned int *X) {
	size_t i, j, k;
	for (i = 0; i < 1024; i++) {
      memcpy(tV+(i*32), X, sizeof(unsigned int)*32);
	  xorSalsa(X);
    }
	for (i = 0; i < 1024; i++) {
      k = (X[16] & 1023) * 32;
      for (j = 0; j < 32; j++) X[j] ^= tV[k + j];
	  xorSalsa(X);
    }
	memcpy(B, X, sizeof(unsigned int)*32);
}

void hasher::destroy() {
	//nothing todo for now 
	delete tSl;
}
