package com.ariasaproject.cpuminingopt;

import static java.lang.Integer.rotateLeft;
import static java.lang.System.arraycopy;

import java.security.GeneralSecurityException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Hasher {
  private Mac mac;
  private byte[] H = new byte[32];
  private byte[] B = new byte[128 + 4];
  private int[] X = new int[32];
  private int[] V = new int[32 * 1024];
  private int[] salsa = new int[16];

  public Hasher() throws GeneralSecurityException {
    mac = Mac.getInstance("HmacSHA256");
  }

  public boolean hashCheck(byte[] header, byte[] target, int nonce) throws GeneralSecurityException {
    int i, j, k;

    arraycopy(header, 0, B, 0, 76);
    B[76] = (byte) (nonce >> 24);
    B[77] = (byte) (nonce >> 16);
    B[78] = (byte) (nonce >> 8);
    B[79] = (byte) (nonce >> 0);
    mac.init(new SecretKeySpec(B, 0, 80, "HmacSHA256"));
    B[80] = 0;
    B[81] = 0;
    B[82] = 0;
    for (i = 0; i < 4; i++) {
      B[83] = (byte) (i + 1);
      mac.update(B, 0, 84);
      mac.doFinal(H, 0);

      for (j = 0; j < 8; j++) {
        X[i * 8 + j] =
            (H[j * 4 + 0] & 0xff) << 0
                | (H[j * 4 + 1] & 0xff) << 8
                | (H[j * 4 + 2] & 0xff) << 16
                | (H[j * 4 + 3] & 0xff) << 24;
      }
    }

    for (i = 0; i < 1024; i++) {
      arraycopy(X, 0, V, i * 32, 32);
      xorSalsa8(0, 16);
      xorSalsa8(16, 0);
    }
    for (i = 0; i < 1024; i++) {
      k = (X[16] & 1023) * 32;
      for (j = 0; j < 32; j++) X[j] ^= V[k + j];
      xorSalsa8(0, 16);
      xorSalsa8(16, 0);
    }

    for (i = 0; i < 32; i++) {
      B[i * 4 + 0] = (byte) (X[i] >> 0);
      B[i * 4 + 1] = (byte) (X[i] >> 8);
      B[i * 4 + 2] = (byte) (X[i] >> 16);
      B[i * 4 + 3] = (byte) (X[i] >> 24);
    }

    B[128 + 3] = 1;
    mac.update(B, 0, 128 + 4);
    mac.doFinal(H, 0);
    // compare to Target
    byte c, t;
    for (i = 31; i >= 0; i--) {
      c = H[i];
      t = target[i];
      if (c != t) {
        return (c < t);
      }
    }
    return false;
  }

  private void xorSalsa8(int di, int xi) {
    salsa[0] = (X[di + 0] ^= X[xi + 0]);
    salsa[1] = (X[di + 1] ^= X[xi + 1]);
    salsa[2] = (X[di + 2] ^= X[xi + 2]);
    salsa[3] = (X[di + 3] ^= X[xi + 3]);
    salsa[4] = (X[di + 4] ^= X[xi + 4]);
    salsa[5] = (X[di + 5] ^= X[xi + 5]);
    salsa[6] = (X[di + 6] ^= X[xi + 6]);
    salsa[7] = (X[di + 7] ^= X[xi + 7]);
    salsa[8] = (X[di + 8] ^= X[xi + 8]);
    salsa[9] = (X[di + 9] ^= X[xi + 9]);
    salsa[10] = (X[di + 10] ^= X[xi + 10]);
    salsa[11] = (X[di + 11] ^= X[xi + 11]);
    salsa[12] = (X[di + 12] ^= X[xi + 12]);
    salsa[13] = (X[di + 13] ^= X[xi + 13]);
    salsa[14] = (X[di + 14] ^= X[xi + 14]);
    salsa[15] = (X[di + 15] ^= X[xi + 15]);
    for (int i = 0; i < 8; i += 2) {
      salsa[4] ^= rotateLeft(salsa[0] + salsa[12], 7);
      salsa[8] ^= rotateLeft(salsa[4] + salsa[0], 9);
      salsa[12] ^= rotateLeft(salsa[8] + salsa[4], 13);
      salsa[0] ^= rotateLeft(salsa[12] + salsa[8], 18);
      salsa[9] ^= rotateLeft(salsa[5] + salsa[1], 7);
      salsa[13] ^= rotateLeft(salsa[9] + salsa[5], 9);
      salsa[1] ^= rotateLeft(salsa[13] + salsa[9], 13);
      salsa[5] ^= rotateLeft(salsa[1] + salsa[13], 18);
      salsa[14] ^= rotateLeft(salsa[10] + salsa[6], 7);
      salsa[2] ^= rotateLeft(salsa[14] + salsa[10], 9);
      salsa[6] ^= rotateLeft(salsa[2] + salsa[14], 13);
      salsa[10] ^= rotateLeft(salsa[6] + salsa[2], 18);
      salsa[3] ^= rotateLeft(salsa[15] + salsa[11], 7);
      salsa[7] ^= rotateLeft(salsa[3] + salsa[15], 9);
      salsa[11] ^= rotateLeft(salsa[7] + salsa[3], 13);
      salsa[15] ^= rotateLeft(salsa[11] + salsa[7], 18);
      salsa[1] ^= rotateLeft(salsa[0] + salsa[3], 7);
      salsa[2] ^= rotateLeft(salsa[1] + salsa[0], 9);
      salsa[3] ^= rotateLeft(salsa[2] + salsa[1], 13);
      salsa[0] ^= rotateLeft(salsa[3] + salsa[2], 18);
      salsa[6] ^= rotateLeft(salsa[5] + salsa[4], 7);
      salsa[7] ^= rotateLeft(salsa[6] + salsa[5], 9);
      salsa[4] ^= rotateLeft(salsa[7] + salsa[6], 13);
      salsa[5] ^= rotateLeft(salsa[4] + salsa[7], 18);
      salsa[11] ^= rotateLeft(salsa[10] + salsa[9], 7);
      salsa[8] ^= rotateLeft(salsa[11] + salsa[10], 9);
      salsa[9] ^= rotateLeft(salsa[8] + salsa[11], 13);
      salsa[10] ^= rotateLeft(salsa[9] + salsa[8], 18);
      salsa[12] ^= rotateLeft(salsa[15] + salsa[14], 7);
      salsa[13] ^= rotateLeft(salsa[12] + salsa[15], 9);
      salsa[14] ^= rotateLeft(salsa[13] + salsa[12], 13);
      salsa[15] ^= rotateLeft(salsa[14] + salsa[13], 18);
    }
    X[di + 0] += salsa[0];
    X[di + 1] += salsa[1];
    X[di + 2] += salsa[2];
    X[di + 3] += salsa[3];
    X[di + 4] += salsa[4];
    X[di + 5] += salsa[5];
    X[di + 6] += salsa[6];
    X[di + 7] += salsa[7];
    X[di + 8] += salsa[8];
    X[di + 9] += salsa[9];
    X[di + 10] += salsa[10];
    X[di + 11] += salsa[11];
    X[di + 12] += salsa[12];
    X[di + 13] += salsa[13];
    X[di + 14] += salsa[14];
    X[di + 15] += salsa[15];
  }
}
