package utils;

import api.ApiServer;

import java.io.IOException;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.URL;

public final class ApiRuntime {
    private static int port = 8080;
    private static boolean started = false;

    private ApiRuntime() {
    }

    public static synchronized void ensureStarted() {
        if (started) {
            return;
        }

        for (int candidate = 8080; candidate <= 8090; candidate++) {
            try {
                ApiServer server = new ApiServer();
                server.start(candidate);
                port = candidate;
                started = true;
                return;
            } catch (BindException e) {
                // Port used: maybe another app instance, try next one.
            } catch (IOException e) {
                // Try next candidate
            }
        }

        // As fallback, if something is already listening on 8080 and responds, use it.
        if (isApiReachable(8080)) {
            port = 8080;
            started = true;
        }
    }

    public static String getBaseUrl() {
        return "http://localhost:" + port;
    }

    public static int getPort() {
        return port;
    }

    private static boolean isApiReachable(int portToCheck) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:" + portToCheck + "/api/health").openConnection();
            connection.setConnectTimeout(1200);
            connection.setReadTimeout(1200);
            connection.setRequestMethod("GET");
            return connection.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
