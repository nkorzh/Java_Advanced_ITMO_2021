package info.kgeorgiy.ja.kozhuharov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class Implementor implements Impler {

    public Implementor() {
    }

    private Path getClassPath(final Class<?> token, final Path rootDirectory) throws ImplerException {
        final Path path;
        try {
            path = rootDirectory
                .resolve(token.getPackageName().replace('.', File.separatorChar))
                .resolve(token.getSimpleName() + "Impl.java");
        } catch (final InvalidPathException e) {
            throw new ImplerException("Given root directory is invalid: " + rootDirectory);
        }
        try {
            Files.createDirectories(path.getParent());
        } catch (final IOException e) {
            throw new ImplerException("Can't create root directories: " + path.getParent());
        }
        return path;
    }

    private void validateToken(final Class<?> token) throws ImplerException {
        final int modifiers = token.getModifiers();

        if (token.isPrimitive() || token.isArray() || token == Enum.class ||
                Modifier.isFinal(modifiers) || Modifier.isPrivate(modifiers)) {
            throw new ImplerException("Invalid token: " + token.getName());
        }
    }

    @Override
    public void implement(final Class<?> token, final Path root) throws ImplerException {
        validateToken(token);
        final Path classPath = getClassPath(token, root);
        try (final BufferedWriter writer = Files.newBufferedWriter(classPath)) {
            writer.write(CodeGenerator.generateClassBody(token));
        } catch (final IOException e) {
            throw new ImplerException("Error opening or creating file: " + classPath);
        }
    }
}
