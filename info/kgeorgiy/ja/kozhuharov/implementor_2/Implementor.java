package info.kgeorgiy.ja.kozhuharov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * The following class generates implements interfaces and classes given by user.
 */
public class Implementor implements JarImpler {
    /**
     * Default constructor
     */
    public Implementor() {
    }

    /**
     * Main function which implements console interface for {@link Implementor}.
     * <ul>
     *     <li>Pass 2 args to create source code file.</li>
     *     <li>Pass 3 args to create jar file</li>
     * </ul>
     *
     * @param args console arguments
     */
    public static void main(final String[] args) {
        if (!approveArgs(args)) {
            return;
        }
        final JarImpler implementor = new Implementor();
        final boolean useJarImpl = args.length == 3;
        final String className = useJarImpl ? args[1] : args[0];
        final String pathName = useJarImpl ? args[2] : System.getProperty("user.dir");
        try {
            final Class<?> classToImplement = Class.forName(className);
            final Path path = Path.of(pathName);
            if (useJarImpl) {
                implementor.implementJar(classToImplement, path);
            } else {
                implementor.implement(classToImplement, path);
            }
        } catch (final ClassNotFoundException e) {
            System.err.println("Couldn't find class to implement (" + className + ")" + e.getMessage());
        } catch (final InvalidPathException e) {
            System.err.println("Error creating output file path: " + e.getMessage());
        } catch (final ImplerException e) {
            System.err.println("Error implementing " + className + ": " + e.getMessage());
        }
    }

    /**
     * Approves or declines received arguments.
     *
     * @param args arguments given by user.
     * @return {@code true}, if all arguments are valid, else {@code false}.
     */
    protected static boolean approveArgs(final String[] args) {
        if (args == null || (args.length != 1 && args.length != 3)) {
            System.err.println("Invalid arguments number, expected usage:");
            System.err.println("\t\t<class-name>");
            System.err.println("\tor");
            System.err.println("\t\t-jar <class-name> <target-path>");
            return false;
        }
        for (String arg : args) {
            if (arg == null) {
                System.err.println("Invalid argument, expected not null");
                return false;
            }
        }
        if (args.length == 3 && !args[0].equals("-jar") && !args[0].equals("--jar")) {
            System.err.println("Expected '-jar' or '--jar' as first argument, found '" + args[0] + "'");
            return false;
        }
        return true;
    }

    /**
     * Method to define given {@link Class} token location.
     * @param token {@link Class} to define location
     * @param rootDirectory current home directory
     * @return defined {@link Path}
     * @throws ImplerException in case we couldn't create parent directories
     * or {@link Class} token name is invalid.
     */
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

    /**
     * Checks if {@link Class} token default implementation can be created.
     *
     * @param token {@link Class} to be checked
     * @throws ImplerException in case if {@link Class} token is:
     * <ul>
     *     <li>primitive</li>
     *     <li>array</li>
     *     <li>enum</li>
     *     <li>final</li>
     *     <li>private</li>
     * </ul>
     */
    private void validateToken(final Class<?> token) throws ImplerException {
        final int modifiers = token.getModifiers();

        if (token.isPrimitive() || token.isArray() || token == Enum.class ||
                Modifier.isFinal(modifiers) || Modifier.isPrivate(modifiers)) {
            throw new ImplerException("Invalid token: " + token.getName());
        }
    }

    /**
     * Converts given {@link String} to unicode encoding.
     *
     * @param text {@link String} to convert.
     * @return string at Unicode.
     */
    private static String toUnicode(String text) {
        StringBuilder builder = new StringBuilder();
        for (char c : text.toCharArray()) {
            builder.append(c < 128 ? c : String.format("\\u%04X", (int) c));
        }
        return builder.toString();
    }

    /**
     * Method to create default implementation.
     *
     * @param token {@link Class} token to create implementation for.
     * @param root root directory.
     * @throws ImplerException in case {@link #validateToken(Class)} defines implementation can't be created.
     */
    @Override
    public void implement(final Class<?> token, final Path root) throws ImplerException {
        validateToken(token);
        final Path classPath = getClassPath(token, root);
        try (final BufferedWriter writer = Files.newBufferedWriter(classPath)) {
            writer.write(toUnicode(CodeGenerator.generateClassBody(token)));
        } catch (final IOException e) {
            throw new ImplerException("Error opening or creating file: " + classPath);
        }
    }

    /**
     * Produces <var>.jar</var> file implementing class or interface specified by provided <var>token</var>.
     * <p>
     * Generated class classes name should be same as classes name of the type token with <var>Impl</var> suffix
     * added.
     *
     * @param token type token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException when implementation cannot be generated.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        final Path path;
        try {
            Files.createDirectories(jarFile.getParent());
            path = Files.createTempDirectory(jarFile.toAbsolutePath().getParent(), "tmp");
        } catch (final IOException e) {
            throw new ImplerException(e.getMessage());
        }
        try {
            implement(token, path);
            compile(token, path);
            generateArtifact(token, jarFile, path);
        } finally {
            try {
                Files.walkFileTree(path, recursiveDirCleaner);
            } catch (final IOException e) {
                System.err.println("Failed to delete " + path + ": " + e.getMessage());
            }
        }
    }

    /**
     * Compiles class, which implements given {@code token} class.
     *
     * @param token type token to create implementation for
     * @param path  directory to store compiled file
     * @throws ImplerException if compilation fails for some reason
     */
    private void compile(final Class<?> token, final Path path) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Could not find java compiler");
        }
        final String file = Path.of(
                path.toString(),
                token.getPackageName().replace('.', File.separatorChar),
                CodeGenerator.getClassName(token) + ".java")
                .toString();
        final String[] args = new String[] {
                file,
                "-cp",
                path + File.pathSeparator + getClassPath(token)
        };
        final int exitCode = compiler.run(null, null, null, args);
        if (exitCode != 0) {
            throw new ImplerException("Error compiling implementation of " + token.getCanonicalName());
        }
    }

    /**
     * Returns {@link Class} file path
     * @param token class to get path
     * @return {@link Path}
     * @throws ImplerException in case correct {@link Path} couldn't be constructed
     */
    private static Path getClassPath(final Class<?> token) throws ImplerException {
        try {
            return Path.of(token.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
        } catch (final URISyntaxException e) {
            throw new ImplerException("URL cannot be converted to URI", e);
        }
    }

    /**
     * Creates {@code .jar} file containing compiled by {@link #compile(Class, Path)}
     * implementation of {@code token}
     *
     * @param token class that was implemented
     * @param jarPath directory to store {@code .jar} file
     * @param compiledClassPath directory containing compiled implementation of {@code token}
     * @throws ImplerException if error occurs during {@link JarOutputStream} work
     */
    private void generateArtifact(final Class<?> token, final Path jarPath, final Path compiledClassPath)
            throws ImplerException {
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        try (final JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarPath), manifest)) {
            final String classFileName = token.getPackageName().replace('.', '/') +
                    "/" + CodeGenerator.getClassName(token) + ".class";
            jarOutputStream.putNextEntry(new ZipEntry(classFileName));
            Files.copy(Paths.get(compiledClassPath.toString(), classFileName), jarOutputStream);
        } catch (final IOException e) {
            throw new ImplerException("Error writing " + e.getMessage());
        }
    }

    /**
     * Recursive directory cleaner.
     */
    private static final SimpleFileVisitor<Path> recursiveDirCleaner = new SimpleFileVisitor<>() {
        /**
         * File deleting visitor
         *
         * @param file {@link Path} file to visit
         * @param attrs {@link BasicFileAttributes} {@code file} attributes
         * @return {@link FileVisitResult#CONTINUE}
         * @throws IOException if deletion fails for some reason
         */
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        /**
         * Directory visitor, which deletes directory after deleting all files inside it
         *
         * @param dir {@link Path} directory to visit
         * @param exc {@link IOException} instance if error occured during directory visiting
         * @return {@link FileVisitResult#CONTINUE}
         * @throws IOException if deletion fails for some reason
         */
        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc)
                throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    };
}
