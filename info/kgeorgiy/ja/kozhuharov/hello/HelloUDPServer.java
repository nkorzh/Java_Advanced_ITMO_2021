package info.kgeorgiy.ja.kozhuharov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPServer extends HelloServerAbstract {
    private DatagramSocket udpSocket;
    private ExecutorService executors;

    @Override
    protected void registerListener(final int threads) {
        executors = Executors.newFixedThreadPool(threads);
        requestListener.submit(() -> {
            try {
                while (!Thread.interrupted()) {
                    final DatagramPacket packet = HelloUtils.createPacket(udpSocket);
                    udpSocket.receive(packet);
                    executors.submit(() -> {
                        try {
                            packet.setData(formResponse(HelloUtils.getBody(packet)));
                            udpSocket.send(packet);
                        } catch (final IOException e) {
                            if (!udpSocket.isClosed()) {
                                System.err.println("Error sending response: " + e.getMessage());
                            }
                        }
                    });
                }
            } catch (final IOException e) {
                if (!udpSocket.isClosed()) {
                    System.err.println("Error receiving packet: " + e.getMessage());
                }
            }
        });
    }

    @Override
    protected boolean openConnection(int port) {
        try {
            udpSocket = new DatagramSocket(port);
        } catch (final SocketException e) {
            System.err.println("Socket could not be opened or bind to port " + port);
            return false;
        }
        return true;
    }

    @Override
    public void close() {
        udpSocket.close();
        HelloUtils.shutdownAndAwait(requestListener, SECONDS_BEFORE_TERMINATION);
        HelloUtils.shutdownAndAwait(executors, SECONDS_BEFORE_TERMINATION);
    }

    private static void printUsage() {
        System.out.println("Expected usage: <port> <threads to process requests>");
    }

    private static boolean validateArgs(String[] args) {
        if (args == null || args.length != 2) {
            return false;
        }
        for (int i = 0; i < 2; i++) {
            if (args[i] == null) {
                System.err.println((i + 1) + " argument is null");
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        if (!validateArgs(args)) {
            printUsage();
            return;
        }
        try {
            final int port = Integer.parseInt(args[0]);
            final int threads = Integer.parseInt(args[1]);

            final HelloServer server = new HelloUDPServer();
            server.start(port, threads);
        } catch (final NumberFormatException e) {
            System.err.println("Error parsing integer arguments: " + e.getMessage());
        }
    }
}
