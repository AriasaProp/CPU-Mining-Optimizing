package com.ariasaproject.cpuminingopt;

import static java.lang.Integer.rotateLeft;
import static java.lang.System.arraycopy;

import java.security.GeneralSecurityException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Hasher {
  private static Mac mac;
  private static byte[] H = new byte[32];
  private static byte[] B = new byte[128 + 4];
  private static int[] X = new int[32];
  private static int[] V = new int[32 * 1024];
  private static native void nHash(byte[] B, int[] X);
  public static boolean hashCheck(byte[] header, byte[] target, int nonce) throws GeneralSecurityException {
  	if (mac == null) mac = Mac.getInstance("HmacSHA256");
    int i, j;

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
        X[i * 8 + j] = (H[j * 4] & 0xff) << 0 | (H[j * 4 + 1] & 0xff) << 8 | (H[j * 4 + 2] & 0xff) << 16 | (H[j * 4 + 3] & 0xff) << 24;
      }
    }
	nHash(B, X);
	B[128 + 3] = 1;
    mac.update(B, 0, 128 + 4);
    mac.doFinal(H, 0);
    //return H;
	
	//compare to Target
	byte c, t;
	for (i = 31; i >= 0; i--) {
		c = H[i];
		t = target[i];
		if (c!=t) {
		  return (c < t);
		}
	}
	return false;
  }
}
