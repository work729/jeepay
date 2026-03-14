package com.jeequan.jeepay.pay.channel.mayifu;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

@Slf4j
public class MayifupayKit {

    private static String encodingCharset = "UTF-8";

    public static String getSign(Map<String, Object> map, String key) {
        ArrayList<String> list = new ArrayList<String>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String k = entry.getKey();
            Object v = entry.getValue();
            if (k == null) continue;
            if ("sign".equals(k) || "signature".equals(k)) continue;
            if (v == null) continue;
            String vs = v.toString();
            if (vs.length() == 0) continue;
            list.add(k + "=" + vs);
        }
        int size = list.size();
        String[] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (i > 0) sb.append("&");
            sb.append(arrayToSort[i]);
        }
        String result = sb.toString() + "&key=" + key;
        log.info("mayifu signStr:{}", result);
        result = md5(result, encodingCharset).toUpperCase();
        return result;
    }

    public static String md5(String value, String charset) {
        MessageDigest md;
        try {
            byte[] data = value.getBytes(charset);
            md = MessageDigest.getInstance("MD5");
            byte[] digestData = md.digest(data);
            return toHex(digestData);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toHex(byte input[]) {
        if (input == null) {
            return null;
        }
        StringBuffer output = new StringBuffer(input.length * 2);
        for (int i = 0; i < input.length; i++) {
            int current = input[i] & 0xff;
            if (current < 16) {
                output.append("0");
            }
            output.append(Integer.toString(current, 16));
        }
        return output.toString();
    }

    public static String getPaymentUrl(String payUrl) {
        if (StringUtils.isEmpty(payUrl)) {
            return payUrl;
        }
        if (payUrl.contains("/api/")) {
            return payUrl;
        }
        if (!payUrl.endsWith("/")) {
            payUrl += "/";
        }
        return payUrl + "api/pay/create_order";
    }
}
