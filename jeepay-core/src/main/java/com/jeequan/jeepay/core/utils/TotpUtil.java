package com.jeequan.jeepay.core.utils;

import cn.hutool.core.codec.Base32;
import cn.hutool.core.util.RandomUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TotpUtil {

    public static String generateSecret(int byteLength) {
        byte[] bytes = RandomUtil.randomBytes(byteLength);
        return Base32.encode(bytes);
    }

    public static boolean verifyCode(String base32Secret, String code, int digits, int periodSeconds, int window) {
        if (code == null || code.length() == 0) {
            return false;
        }
        int codeInt;
        try {
            codeInt = Integer.parseInt(code);
        } catch (NumberFormatException e) {
            return false;
        }
        long time = System.currentTimeMillis() / 1000L;
        long counter = time / periodSeconds;
        for (int i = -window; i <= window; i++) {
            if (generateTotp(Base32.decode(base32Secret), counter + i, digits) == codeInt) {
                return true;
            }
        }
        return false;
    }

    public static String buildOtpauthUri(String issuer, String accountName, String base32Secret, int digits, int periodSeconds, String algorithm) {
        String label = issuer + ":" + accountName;
        return "otpauth://totp/" + urlEncode(label)
                + "?secret=" + base32Secret
                + "&issuer=" + urlEncode(issuer)
                + "&digits=" + digits
                + "&period=" + periodSeconds
                + "&algorithm=" + algorithm;
    }

    private static int generateTotp(byte[] key, long counter, int digits) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] msg = toBytes(counter);
            byte[] hash = mac.doFinal(msg);
            int offset = hash[hash.length - 1] & 0x0F;
            int binary =
                    ((hash[offset] & 0x7f) << 24) |
                    ((hash[offset + 1] & 0xff) << 16) |
                    ((hash[offset + 2] & 0xff) << 8) |
                    (hash[offset + 3] & 0xff);
            int mod = (int) Math.pow(10, digits);
            return binary % mod;
        } catch (Exception e) {
            return -1;
        }
    }

    private static byte[] toBytes(long val) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putLong(val);
        return buffer.array();
    }

    private static String urlEncode(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (isSafe(c)) {
                sb.append(c);
            } else {
                sb.append('%');
                sb.append(String.format("%02X", (int) c));
            }
        }
        return sb.toString();
    }

    private static boolean isSafe(char c) {
        return (c >= 'A' && c <= 'Z') ||
                (c >= 'a' && c <= 'z') ||
                (c >= '0' && c <= '9') ||
                c == '-' || c == '_' || c == '.' || c == '~' || c == ' ' || c == ':';
    }
}
