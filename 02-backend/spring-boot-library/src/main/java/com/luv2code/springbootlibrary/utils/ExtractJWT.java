package com.luv2code.springbootlibrary.utils;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ExtractJWT {

    private ExtractJWT() {
        /* This utility class should not be instantiated */
    }

    public static String payloadJWTExtraction(String token, String extraction) {

        token = token.replace("Bearer ", "").trim();

        String[] chunks = token.split("\\.");
        if (chunks.length < 2) {
            return null;
        }

        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payload = new String(decoder.decode(chunks[1]));

        payload = payload.replace("\"\"", "\"");

        String[] entries = payload.split(",");
        Map<String, String> map = new HashMap<>();

        for (String entry : entries) {
            if (entry.contains(extraction)) {
                String[] keyValue = entry.split(":");
                if (keyValue.length > 1) {
                    String value = keyValue[1].trim();
                    value = value.replace("\"", "")
                            .replace("}", "")
                            .replace("{", "")
                            .replace("[", "")
                            .replace("]", "")
                            .trim();
                    map.put(extraction, value);
                }
            }
        }

        return map.getOrDefault(extraction, null);
    }
}
