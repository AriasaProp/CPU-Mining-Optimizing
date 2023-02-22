#include "cryptopp/sha.h"
#include <iostream>
#include <string>

using namespace std;
using namespace CryptoPP;

uint32_t findNonce(const string &header, const string &coinbase,
                   const string &merkle_branch, const string &block_version,
                   const string &nbits, const string &timestamp) {
  uint32_t nonce = 0;
  string hashStr;

  while (true) {
    // Combine all the values to form the block header
    string blockHeader = header + coinbase + merkle_branch + block_version +
                         nbits + timestamp + to_string(nonce);

    // Compute the double-SHA256 hash of the block header
    SHA256 hash1;
    byte hash[SHA256::DIGESTSIZE];
    hash1.Update((const byte *)blockHeader.data(), blockHeader.size())
        .Final(hash);
    SHA256 hash2;
    hash2.Update(hash, SHA256::DIGESTSIZE).Final(hash);

    // Convert the hash to a hex string
    StringSource ss(hash, sizeof(hash), true,
                    new HexEncoder(new StringSink(hashStr)));

    // Check if the hash meets the difficulty target
    if (hashStr.substr(0, 6) == "000000") {
      break;
    }
    nonce++;
  }
  return nonce;
}
