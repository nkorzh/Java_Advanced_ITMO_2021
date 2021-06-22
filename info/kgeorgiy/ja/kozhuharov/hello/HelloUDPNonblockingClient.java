package info.kgeorgiy.ja.kozhuharov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HelloUDPNonblockingClient extends HelloClientAbstract {
    private static final int BUFF_SIZE = 1 << 15;
    private static final int DEFAULT_TIMEOUT_MILLISECONDS = 500;

    @Override
    public void run(final String host, final int port, final String prefix, final int threads, final int requests) {
        final InetSocketAddress server;
        try {
            server = new InetSocketAddress(host, port);
        } catch (final IllegalArgumentException e) {
            System.err.println("Null host or invalid port: " + e.getMessage());
            return;
        }
        final Map<DatagramChannel, Integer> channels = new HashMap<>();
        int[] requestsPerChannel = new int[threads];

        try (final Selector selector = Selector.open()) {

            for (int i = 0; i < threads; i++) {
                final DatagramChannel dc = DatagramChannel.open();
                dc.configureBlocking(false);
                dc.connect(server);
                dc.register(selector, SelectionKey.OP_WRITE);
                channels.put(dc, i);
            }

            int threadsDone = 0;
            while (threadsDone < threads && !Thread.interrupted()) {
                selector.select(DEFAULT_TIMEOUT_MILLISECONDS);
                wakeupKeys(selector);
                for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                    try {
                        final SelectionKey key = it.next();
                        if (!key.isValid()) {
                            continue;
                        }
                        final DatagramChannel channel = (DatagramChannel) key.channel();
                        final int channelId = channels.get(channel);

                        if (key.isReadable()) {
                            String response = read(channel);
                            if (!isValidResponse(response, prefix, channelId, requestsPerChannel[channelId])) {
                                continue;
                            }
                            if (++requestsPerChannel[channelId] >= requests) {
                                threadsDone++;
                            }
                            key.interestOps(SelectionKey.OP_WRITE);
                        }
                        if (key.isWritable()) {
                            int currentRequests = requestsPerChannel[channelId];
                            if (currentRequests >= requests) {
                                key.interestOps(SelectionKey.OP_READ);
                                break;
                            }
                            final String message = formRequest(prefix, channelId, currentRequests);
                            channel.write(HelloUtils.textToBuffer(message));
                            key.interestOps(SelectionKey.OP_READ);
                        }
                    } catch (final IOException e) {
                        System.err.println("Error sending or receiving the packet: " + e.getMessage());
                    } finally {
                        it.remove();
                    }
                }
            }
            for (final DatagramChannel channel : channels.keySet()) {
                channel.close();
            }
        } catch (final IOException e) {
            System.err.println("Error occurred while sending requests: " + e.getMessage());
        }
    }

    private String read(final DatagramChannel channel) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(BUFF_SIZE);
        channel.receive(buf);
        return HelloUtils.bufferToText(buf);
    }

    private void wakeupKeys(final Selector selector) {
        if (selector.selectedKeys().isEmpty()) {
            for (final SelectionKey key : selector.keys()) {
                key.interestOps(SelectionKey.OP_WRITE);
            }
        }
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
        final HelloClient client = new HelloUDPNonblockingClient();
        client.run(host, port, prefix, threads, requestsPerThread);
    }
}
