package com.cop.zip4j.crypto.aesnew.pbkdf2;

/*
 * Source referred from Matthias Gartner's PKCS#5 implementation -
 * see http://rtner.de/software/PBKDF2.html
 */

class BinTools {

    public static final String hex = "0123456789ABCDEF";

    public static String bin2hex(final byte[] b) {
        if (b == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer(2 * b.length);
        for (int i = 0; i < b.length; i++) {
            int v = (256 + b[i]) % 256;
            sb.append(hex.charAt((v / 16) & 15));
            sb.append(hex.charAt((v % 16) & 15));
        }
        return sb.toString();
    }

    public static byte[] hex2bin(final String s) {
        String m = s;
        if (s == null) {
            // Allow empty input string.
            m = "";
        } else if (s.length() % 2 != 0) {
            // Assume leading zero for odd string length
            m = "0" + s;
        }
        byte r[] = new byte[m.length() / 2];
        for (int i = 0, n = 0; i < m.length(); n++) {
            char h = m.charAt(i++);
            char l = m.charAt(i++);
            r[n] = (byte)(hex2bin(h) * 16 + hex2bin(l));
        }
        return r;
    }

    public static int hex2bin(char c) {
        if (c >= '0' && c <= '9') {
            return (c - '0');
        }
        if (c >= 'A' && c <= 'F') {
            return (c - 'A' + 10);
        }
        if (c >= 'a' && c <= 'f') {
            return (c - 'a' + 10);
        }
        throw new IllegalArgumentException(
                "Input string may only contain hex digits, but found '" + c
                        + "'");
    }
}
