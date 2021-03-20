package info.kgeorgiy.ja.kozhuharov.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class PJWFileHasher extends SimpleFileVisitor<Path> {
    private final BufferedWriter writer;
    private final int bufferMaxSize;

    public PJWFileHasher(final BufferedWriter writer, int bufferMaxSize) {
        this.writer = writer;
        this.bufferMaxSize = bufferMaxSize;
    }

    public PJWFileHasher(BufferedWriter writer) {
        this(writer, 1024);
    }

    private String formatHashResult(long hashResult, final String fileName) {
        return String.format("%016x %s%s", hashResult, fileName, System.lineSeparator());
    }

    private FileVisitResult writeHashAndContinue(long hashValue, final String fileName) {
        try {
            writer.write(formatHashResult(hashValue, fileName));
        } catch (final IOException e) {
            System.err.println("Error writing hash: " + e.getMessage());
        }
        return FileVisitResult.CONTINUE;
    }

    private long updateHash(long currentHash, byte[] bytes, int bytesRead) {
        for (int i = 0; i < bytesRead; i++) {
            currentHash = (currentHash << 8) + (bytes[i] & 0xff);
            final long topEightBits = currentHash & 0xff00_0000_0000_0000L;
            if (topEightBits != 0) {
                currentHash ^= topEightBits >> 48;
                currentHash &= ~topEightBits;
            }
        }
        return currentHash;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
        long hash = 0;
        final byte[] buffer = new byte[bufferMaxSize];
        try (InputStream reader = Files.newInputStream(file)) {
            int bytesRead;
            while ((bytesRead = reader.read(buffer)) >= 0) {
                hash = updateHash(hash, buffer, bytesRead);
            }
        } catch (final IOException e) {
            hash = 0;
        }
        return writeHashAndContinue(hash, file.toString());
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return writeHashAndContinue(0, file.toString());
    }

    public void startFileWalk(String fileName) throws IOException {
        final Path file;
        try {
             file = Paths.get(fileName);
        } catch (final InvalidPathException e) {
            writeHashAndContinue(0, fileName);
            return;
        }
        Files.walkFileTree(file, this);
    }
}
