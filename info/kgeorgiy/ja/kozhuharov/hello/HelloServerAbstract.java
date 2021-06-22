package info.kgeorgiy.ja.kozhuharov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class HelloServerAbstract implements HelloServer {
    protected static int SECONDS_BEFORE_TERMINATION = 5;
    protected ExecutorService requestListener;

    @Override
    public void start(int port, int threads) {
        if (!openConnection(port)) {
            return;
        }
        requestListener = Executors.newSingleThreadExecutor();
        registerListener(threads);
    }

    protected abstract boolean openConnection(int port);

    protected abstract void registerListener(int threads);

    protected byte[] formResponse(final String response) {
        return ("Hello, " + response).getBytes(Charset.forName("UTF-8"));
    }
}