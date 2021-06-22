package info.kgeorgiy.ja.kozhuharov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

public abstract class HelloClientAbstract implements HelloClient {

    protected boolean isValidResponse(final String response, final String prefix, final int index, final int requestId) {
        final String expected = formRequest(prefix, index, requestId);
        return response.contains(expected);
    }

    protected String formRequest(String prefix, int thread, int requestId) {
        return prefix + thread + "_" + requestId;
    }

    protected static void printUsage() {
        System.err.println("Expected usage: " +
                "<name or IP of server> <port> <request prefix> <threads> <requests per thread>");
    }

    protected static boolean validateArgs(String[] args) {
        if (args == null || args.length != 5) {
            return false;
        }
        for (int i = 0; i < 5; i++) {
            if (args[i] == null) {
                System.err.println((i + 1) + "argument is null");
                return false;
            }
        }
        return true;
    }
}
