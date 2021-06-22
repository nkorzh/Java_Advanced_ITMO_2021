package info.kgeorgiy.ja.kozhuharov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPClient extends HelloClientAbstract {
    private static final int socketTimeoutMiliseconds = 500;

    @Override
    public void run(final String host,
                    final int port,
                    final String prefix,
                    final int threads,
                    final int requestsPerThread) {
        final SocketAddress serverSockAddr;
        try {
            serverSockAddr = new InetSocketAddress(InetAddress.getByName(host), port);
        } catch (final UnknownHostException e) {
            System.err.println("Unknown host: " + host);
            return;
        }
        final ExecutorService threadPool = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            final int thread = i;
            threadPool.submit(() -> {
                try (final DatagramSocket udpSocket = new DatagramSocket()) {
                    udpSocket.setSoTimeout(socketTimeoutMiliseconds);
                    final DatagramPacket packetToSend = HelloUtils.createPacket(udpSocket, serverSockAddr);

                    for (int request = 0; request < requestsPerThread; request++) {
                        final String requestBody = formRequest(prefix, thread, request);
                        while (true) {
                            try {
                                packetToSend.setData(requestBody.getBytes(Charset.forName("UTF-8")));
                                udpSocket.send(packetToSend);

                                final DatagramPacket servResponse =
                                        HelloUtils.createPacket(udpSocket, serverSockAddr);

                                servResponse.setData(new byte[udpSocket.getReceiveBufferSize()]);
                                udpSocket.receive(servResponse);

                                final String responseBody = HelloUtils.getBody(servResponse);
                                if (isValidResponse(responseBody, prefix, thread, request)) {
                                    break;
                                }
                            } catch (final IOException e) {
                                System.err.println("Error sending or receiving packet: " + e.getMessage());
                            }
                        }
                    }
                } catch (final SocketException e) {
                    System.err.println("Unable to create socket: " + e.getMessage());
                }
            });
        }
        HelloUtils.shutdownAndAwait(threadPool, socketTimeoutMiliseconds * 10 * threads * requestsPerThread);
    }

    public static void main(String[] args) {
        if (!validateArgs(args)) {
            printUsage();
            return;
        }
        final String host = args[0];
        final String prefix = args[2];
        final int port, threads, requestsPerThread;
        try {
            port = Integer.parseInt(args[1]);
            threads = Integer.parseInt(args[3]);
            requestsPerThread = Integer.parseInt(args[4]);
        } catch (final NumberFormatException nfe) {
            System.err.println("Error parsing integer argument: " + nfe.getMessage());
            printUsage();
            return;
        }
        final HelloClient client = new HelloUDPClient();
        client.run(host, port, prefix, threads, requestsPerThread);
    }
}
