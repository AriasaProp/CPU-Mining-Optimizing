#include "hash-library/sha256.h"
#include <iostream>
#include <string>

uint32_t findNonce(const string &header, const string &coinbase, const string &merkle_branch, const string &block_version, const string &nbits, const string &timestamp) {
  uint32_t nonce = 0;
  std::string hashStr;
  SHA256 hash;
  while (true) {
  	hash.reset();
    // Combine all the values to form the block header
    string blockHeader = header + coinbase + merkle_branch + block_version + nbits + timestamp + to_string(nonce);
    // Compute the double-SHA256 hash of the block header
    hash.add(block.data(), block.size());
    hashStr = hash.getHash();
    // Check if the hash meets the difficulty target
    if (hashStr.substr(0, 6) == "000000") {
      break;
    }
    nonce++;
  }
  return nonce;
}
