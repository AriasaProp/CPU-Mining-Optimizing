package com.ariasaproject.cpuminingopt;

public class Base64 {
  private static final char[] BASE64_ALPHABET =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

  public static final String encode(String str) {

    byte[] buf = str.getBytes();
    int size = buf.length;
    char[] ar = new char[((size + 2) / 3) * 4];
    int a = 0;
    int i = 0;
    byte b0, b1, b2;
    while (i < size) {
      b0 = buf[i++];
      b1 = (i < size) ? buf[i++] : 0;
      b2 = (i < size) ? buf[i++] : 0;
      ar[a++] = BASE64_ALPHABET[(b0 >> 2) & 0x3f];
      ar[a++] = BASE64_ALPHABET[((b0 << 4) | ((b1 & 0xFF) >> 4)) & 0x3f];
      ar[a++] = BASE64_ALPHABET[((b1 << 2) | ((b2 & 0xFF) >> 6)) & 0x3f];
      ar[a++] = BASE64_ALPHABET[b2 & 0x3f];
    }
    switch (size % 3) {
      case 1:
        ar[--a] = '=';
      case 2:
        ar[--a] = '=';
    }
    return new String(ar);
  }
}
