package info.kgeorgiy.ja.kozhuharov.hello;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

class HelloUtils {
    static DatagramPacket createPacket(DatagramSocket socket) throws SocketException {
        final int bufferSize = socket.getReceiveBufferSize();
        return new DatagramPacket(new byte[bufferSize], bufferSize);
    }

    static DatagramPacket createPacket(final DatagramSocket socket, final SocketAddress destAddress) throws SocketException {
        final int bufferSize = socket.getReceiveBufferSize();
        return new DatagramPacket(new byte[bufferSize], bufferSize, destAddress);
    }

    static String getBody(final DatagramPacket packet) {
        return new String(
                packet.getData(),
                packet.getOffset(),
                packet.getLength(),
                Charset.forName("UTF-8")
        );
    }

    static String bufferToText(final ByteBuffer buffer) {
        return new String(buffer.array(), Charset.forName("UTF-8")).trim();
    }

    static ByteBuffer textToBuffer(final String text) {
        return ByteBuffer.wrap(text.getBytes(Charset.forName("UTF-8")));
    }

    static void shutdownAndAwait(final ExecutorService pool, final int timeoutSeconds) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                    System.err.println("Pool did not terminate");
                }
            }
        } catch (final InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
