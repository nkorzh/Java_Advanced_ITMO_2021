package info.kgeorgiy.ja.kozhuharov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class HelloUDPNonblockingServer extends HelloServerAbstract {
    private static final int BUFFER_SIZE = 1 << 6;
    private static final int SELECTOR_TIMEOUT = 500;
    private Selector selector;
    private InetSocketAddress address;

    @Override
    protected boolean openConnection(int port) {
        try {
            address = new InetSocketAddress(port);
            selector = Selector.open();
        } catch (final IllegalArgumentException e) {
            System.err.println("Invalid port: " + e.getMessage());
            return false;
        } catch (final IOException e) {
            System.err.println("Cannot start server");
            return false;
        }
        return true;
    }

    @Override
    protected void registerListener(final int threadsIgnored) {
        requestListener.submit(() -> {
            try (final DatagramChannel channel = DatagramChannel.open()) {
                channel.configureBlocking(false);
                channel.register(selector, SelectionKey.OP_READ);
                channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                channel.bind(address);
                SocketAddress senderAddress = null;

                while (!Thread.interrupted()) {
                    selector.select(SELECTOR_TIMEOUT);

                    for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                        final SelectionKey key = it.next();
                        try {
                            if (!key.isValid()) {
                                continue;
                            }
                            final DatagramChannel curChannel = (DatagramChannel) key.channel();
                            if (key.isReadable()) {
                                senderAddress = read(curChannel, key);
                            }
                            if (key.isWritable()) {
                                write(curChannel, key, senderAddress);
                            }
                        } catch (final IOException e) {
                            System.err.println("Error: " + e.getMessage());
                        }
                        selector.wakeup();
                        it.remove();
                    }
                }
            } catch (final IOException e) {
                System.err.println("Error receiving package: " + e.getMessage());
            }
        });
    }

    private SocketAddress read(final DatagramChannel channel, final SelectionKey key) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        final SocketAddress address = channel.receive(buffer);
        key.attach(buffer);
        key.interestOps(SelectionKey.OP_WRITE);
        return address;
    }

    private void write(final DatagramChannel channel, final SelectionKey key, final SocketAddress to) throws IOException {
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        buffer = ByteBuffer.wrap(formResponse(HelloUtils.bufferToText(buffer)));
        channel.send(buffer, to);
        key.interestOps(SelectionKey.OP_READ);
    }

    @Override
    public void close() {
        try {
            selector.close();
        } catch (IOException ignored) {
        }
        HelloUtils.shutdownAndAwait(requestListener, SECONDS_BEFORE_TERMINATION);
    }

    private static void printUsage() {
        System.out.println("Expected usage: <port> <threads>");
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
            final HelloServer server = new HelloUDPNonblockingServer();
            server.start(port, threads);
        } catch (final NumberFormatException e) {
            System.err.println("Error parsing integer arguments: " + e.getMessage());
        }
    }
}
