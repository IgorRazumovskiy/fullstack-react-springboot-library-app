package com.luv2code.springbootlibrary.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class ExtractJWT {

    private ExtractJWT() {
        /* This utility class should not be instantiated */
    }

    public static String payloadJWTExtraction(String token, String extraction) {
        String rawToken = token.replace("Bearer ", "").trim();

        String[] chunks = rawToken.split("\\.");
        if (chunks.length < 2) {
            return null;
        }

        String payload = new String(Base64.getUrlDecoder().decode(chunks[1]), StandardCharsets.UTF_8)
                .replace("\"\"", "\"");

        String result = null;
        for (String entry : payload.split(",")) {
            if (entry.contains(extraction)) {
                String[] keyValue = entry.split(":");
                if (keyValue.length > 1) {
                    result = keyValue[1]
                            .replace("\"", "")
                            .replace("}", "")
                            .replace("{", "")
                            .replace("[", "")
                            .replace("]", "")
                            .trim();
                }
            }
        }
        return result;
    }
}
