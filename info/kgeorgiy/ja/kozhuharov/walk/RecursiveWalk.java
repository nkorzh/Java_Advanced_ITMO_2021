package info.kgeorgiy.ja.kozhuharov.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Kozhukharov Nikita
 */
public class RecursiveWalk {
    public static void main(final String[] args) {
        try {
            if (args != null && args.length >= 2 && args[0] != null && args[1] != null) {
                doWalk(args[0], args[1]);
            } else {
                throw new IOException("Invalid arguments: \"java Walk <input file> <output file>\" expected.");
            }
        } catch (final IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void doWalk(final String inputFilename, final String outputFilename) throws IOException {
        final Path inputFile;
        final Path outputFile;
        try {
            inputFile = Paths.get(inputFilename);
        } catch (final InvalidPathException e) {
            throw new IOException("Wrong input file name: " + e.getMessage());
        }                                             
        try {
            outputFile = Paths.get(outputFilename);
        } catch (final InvalidPathException e) {
            throw new IOException("Wrong output file name: " + e.getMessage());
        }
        if (outputFile.getParent() != null) {
            try {
                Files.createDirectories(outputFile.getParent());
            } catch (final IOException e) {
                throw new IOException("Cannot create parent directories for output file: " + e.getMessage());
            }
        }
        try (final BufferedReader reader = Files.newBufferedReader(inputFile)) {
            try (final BufferedWriter writer = Files.newBufferedWriter(outputFile)) {

                final PJWFileHasher hasher = new PJWFileHasher(writer);
                String fileName;
                while ((fileName = reader.readLine()) != null) {
                    hasher.startFileWalk(fileName);
                }
            } catch (IOException e) {
                throw new IOException("Error in output file: " + e.getMessage());
            }
        } catch (IOException e) {
            throw new IOException("Error in input file: " + e.getMessage());
        }
    }
}
