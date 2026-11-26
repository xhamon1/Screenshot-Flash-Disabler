package com.xhamon.noflashscreenshot;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateChecker {

    public interface Callback {
        void onResult(boolean updateAvailable, String latestVersion, String releaseUrl);
        void onError(Exception e);
    }

    private static final String API_URL =
            "https://api.github.com/repos/xhamon1/Screenshot-Flash-Disabler/releases/latest";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public static void check(String currentVersion, Callback callback) {
        EXECUTOR.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(API_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Accept", "application/vnd.github+json");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                }

                JSONObject json = new JSONObject(sb.toString());
                String tag = json.optString("tag_name", "");
                String releaseUrl = json.optString("html_url", "");
                String latest = tag.startsWith("v") ? tag.substring(1) : tag;

                callback.onResult(isNewer(latest, currentVersion), latest, releaseUrl);
            } catch (Exception e) {
                callback.onError(e);
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private static boolean isNewer(String latest, String current) {
        String[] a = latest.split("\\.");
        String[] b = current.split("\\.");
        int len = Math.max(a.length, b.length);
        for (int i = 0; i < len; i++) {
            int ai = i < a.length ? parse(a[i]) : 0;
            int bi = i < b.length ? parse(b[i]) : 0;
            if (ai != bi) return ai > bi;
        }
        return false;
    }

    private static int parse(String s) {
        try {
            return Integer.parseInt(s.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}
