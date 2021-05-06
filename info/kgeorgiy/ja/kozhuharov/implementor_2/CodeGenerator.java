package info.kgeorgiy.ja.kozhuharov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class that provides methods to generate classes and interfaces source code.
 */
public class CodeGenerator {
    /**
     * System-dependent line separator string, returned by {@link System#lineSeparator()}.
     */
    private static final String lineSep = System.lineSeparator();

    /**
     * Two line separators, returned by {@link System#lineSeparator()}.
     */
    private static final String lineSepDouble = System.lineSeparator().repeat(2);

    /**
     * Body opening bracket.
     */
    private static final String bodyOpenBracket = "{";

    /**
     * Body closing bracket.
     */
    private static final String bodyCloseBracket = "}";

    /**
     * Round opening bracket.
     */
    private static final String roundOpenBracket = "(";

    /**
     * Round closing bracket.
     */
    private static final String roundCloseBracket = ")";

    /**
     * Comma and whitespace.
     */
    private static final String comma = ", ";

    /**
     * Semicolon.
     */
    private static final String semiColon = ";";

    /**
     * Single space.
     */
    private static final String space = " ";

    /**
     * Single tab.
     */
    private static final String tab = "\t";

    /**
     * Double tab.
     */
    private static final String tabDouble = "\t".repeat(2);

    /**
     * Empty {@link String}.
     */
    private static final String emptyString = "";

    /**
     * Suffix of default-generated class.
     */
    private static final String implNameSuffix = "Impl";
    // Keywords

    /**
     * {@code package} keyword.
     */
    private static final String PACKAGE = "package";

    /**
     * {@code class} keyword.
     */
    private static final String CLASS = "class";

    /**
     * {@code implements} keyword.
     */
    private static final String IMPLEMENTS = "implements";

    /**
     * {@code extends} keyword.
     */
    private static final String EXTENDS = "extends";

    /**
     * {@code throws} keyword.
     */
    private static final String THROWS = "throws";

    /**
     * {@code super} keyword.
     */
    private static final String SUPER = "super";

    /**
     * {@code return} keyword.
     */
    private static final String RETURN = "return";

    /**
     * {@code public} access modifier.
     */
    private static final String PUBLIC = "public";

    /**
     * Returns the new class name for a given {@link Class} token.
     *
     * @param token the given {@link Class} token
     * @return the new class name
     */
    public static String getClassName(final Class<?> token) {
        return token.getSimpleName() + implNameSuffix;
    }

    /**
     * Returns the packages of a given {@link Class} token.
     *
     * @param token the given {@link Class} token
     * @return a string of the packages of a given {@link Class} token
     */
    private static String declarePackage(final Class<?> token) {
        return token.getPackageName().equals(emptyString) ?
                emptyString :
                String.join(space, PACKAGE, token.getPackage().getName()) + semiColon + lineSep;
    }
    /**
     * Returns the first line of class declaration of a given {@link Class} token with
     * body opening bracket({@value bodyOpenBracket}).
     * @param token the given {@link Class} token
     * @return a string of the packages of a given {@link Class} token
     */
    private static String generateClassHeading(final Class<?> token) {
        return String.join(
                space,
                PUBLIC,
                CLASS,
                getClassName(token),
                token.isInterface() ? IMPLEMENTS : EXTENDS,
                token.getCanonicalName(),
                bodyOpenBracket);
    }

    /*
     * Enumerations
     */
    /**
     * Returns the string of parameters joined by comma and space({@value comma}).
     *
     * @param parameters array of parameters to enumerate
     * @param isDeclaration true if declaring the arguments, false if calling a function
     * @return the string of arguments
     */
    private static String enumerateArguments(final Parameter[] parameters, final boolean isDeclaration) {
        return Arrays.stream(parameters)
                .map(param ->
                        ((isDeclaration ? param.getType().getCanonicalName() + space : emptyString) +
                                param.getName()))
                .collect(Collectors.joining(comma));
    }

    /**
     * Returns the string, starting with {@value THROWS} and enumerating exceptions.<p>
     * Exceptions are joined with comma and space({@value comma}).
     *
     * @param exceptions array of exception to enumerate
     * @return the string of arguments
     */
    private static String enumerateExceptions(final Class<?>[] exceptions) {
        return exceptions.length == 0 ?
                emptyString :
                String.join(
                        space,
                        THROWS,
                        Arrays.stream(exceptions)
                                .map(Class::getCanonicalName)
                                .collect(Collectors.joining(comma))
                );
    }

    /*
     * Methods and constructor generation
     */
    /**
     * Returns the default value for a given {@link Class} token.
     *
     * @param returnValue {@link Class} of return value
     * @return the default value for given {@link Class} token
     */
    private static String getDefaultValue(final Class<?> returnValue) {
        if (!returnValue.isPrimitive()) {
            return "null";
        } else if (returnValue.equals(boolean.class)) {
            return "false";
        } else if (returnValue.equals(void.class)) {
            return emptyString;
        }
        return "0";
    }

    /**
     * Generates the opening line for the given method or constructor.
     *
     * @param executable the given {@link Class} token
     * @param name the given {@link Class} token
     * @return the opening line of the executable
     */
    private static String generateExecutable(final Executable executable, final String name) {
        return String.join(space,
                PUBLIC,
                name,
                roundOpenBracket + enumerateArguments(executable.getParameters(), true) + roundCloseBracket,
                enumerateExceptions(executable.getExceptionTypes()),
                bodyOpenBracket);
    }

    /**
     * Generates the constructor for the given {@link Constructor}.
     *
     * @param constructor the given {@link Constructor} token
     * @return the code of constructor
     */
    private static String generateConstructor(final Constructor<?> constructor) {
        return String.join(lineSep,
                tab + generateExecutable(constructor, getClassName(constructor.getDeclaringClass())),
                String.join(emptyString,
                        tab.repeat(2),
                        SUPER,
                        roundOpenBracket,
                        enumerateArguments(constructor.getParameters(), false),
                        roundCloseBracket) + semiColon,
                tab + bodyCloseBracket);
    }

    /**
     * Generates the constructor {@link List} of constructor implementations for the given {@link Class}.
     *
     * @param token the given {@link Class} token, which constructors should be implemented
     * @return the list of constructors' implementations
     * @throws ImplerException in case given {@link Class} is not abstract and it doesn't have
     * public constructors.
     */
    private static List<String> generateConstructors(final Class<?> token) throws ImplerException {
        if (token.isInterface()) {
            return Collections.emptyList();
        }
        final List<Constructor<?>> constructors = Arrays.stream(token.getDeclaredConstructors())
                .filter(constructor -> !Modifier.isPrivate(constructor.getModifiers()))
                .collect(Collectors.toList());

        if (constructors.isEmpty()) {
            throw new ImplerException("Missing public constructors for " + token.getName());
        }
        return constructors.stream()
                .map(CodeGenerator::generateConstructor)
                .collect(Collectors.toList());
    }

    /**
     * Generates the {@link Stream} of {@link Method}s of given {@link Class} token.
     *
     * @param token the given {@link Class} token, which declared methods should be returned
     * @return the {@link Stream} of declared {@link Method}s
     */
    private static Stream<Method> retrieveDeclaredMethods(final Class<?> token) {
        final Function<Method[], Set<MethodWrapper>> convertToMethodKeys = methods ->
                Arrays.stream(methods)
                        .map(MethodWrapper::new)
                        .collect(Collectors.toSet());

        Set<MethodWrapper> methods = new HashSet<>();
        Queue<Class<?>> tokensToCheck = new ArrayDeque<>();
        tokensToCheck.add(token);

        while (!tokensToCheck.isEmpty()) {
            final Class<?> curToken = tokensToCheck.poll();
            methods.addAll(convertToMethodKeys.apply(curToken.getDeclaredMethods()));
            tokensToCheck.addAll(Arrays.asList(curToken.getInterfaces()));
            final Class<?> superClass = curToken.getSuperclass();
            if (superClass != null) {
                tokensToCheck.add(superClass);
            }
        }
        return methods.stream()
                .filter(MethodWrapper::isAbstract)
                .map(MethodWrapper::getMethod);
    }

    /**
     * Returns the {@link List} of implemented methods of given {@link Class} token.
     *
     * @param token the given {@link Class} token, which methods should be implemented
     * @return the {@link List} of implemented {@link Method}s
     */
    private static List<String> generateMethods(final Class<?> token) {
        final Function<Method, String> generateMethod = method ->
                String.join(
                        lineSep,
                        tab +
                                CodeGenerator.generateExecutable(
                                        method,
                                        String.join(
                                                space,
                                                method.getReturnType().getCanonicalName(),
                                                method.getName())),
                        String.join(
                                space,
                                tabDouble + RETURN,
                                getDefaultValue(method.getReturnType())) + semiColon,
                        tab + bodyCloseBracket);

        return retrieveDeclaredMethods(token)
                .map(generateMethod)
                .collect(Collectors.toList());
    }

    /**
     * Returns default implementation of given {@link Class} token.
     *
     * @param token {@link Class} token, which implementation should be returned
     * @return {@link String} with code
     * @throws ImplerException in case non-abstract {@link Class} doesn't have public constructors
     */
    public static String generateClassBody(final Class<?> token) throws ImplerException {
        return String.join(
                lineSep,
                declarePackage(token),
                generateClassHeading(token),
                String.join(lineSep, generateConstructors(token)),
                String.join(lineSepDouble, generateMethods(token)),
                bodyCloseBracket
        );
    }

    /**
     * {@link Method} wrapper class with custom {@link MethodWrapper#hashCode()}
     */
    static class MethodWrapper {
        /**
         * Wrapped {@link Method}
         */
        final private Method method;

        /**
         * The constructor creating the new instance of {@link MethodWrapper}.
         * @param method {@link Method} to wrap
         */
        MethodWrapper(Method method) {
            this.method = method;
        }

        /**
         * Getter which returns wrapped {@link Method}
         * @return the method itself
         */
        Method getMethod() {
            return method;
        }

        /**
         * Returns of wrapped {@link Method} is abstract
         * @return the result of calling {@link Modifier#isAbstract(int)} on {@link Method}s modifiers.
         */
        boolean isAbstract() {
            return Modifier.isAbstract(method.getModifiers());
        }

        /**
         * Custom {@link Method#hashCode()}.
         *
         * @return the hash of the wrapped {@link Method}, considering method's name and {@link Parameter}s types
         */
        @Override
        public int hashCode() {
            final int POW = 43;
            return method.getName().hashCode() +
                    POW * Arrays.hashCode(method.getParameterTypes());
        }

        /**
         * Method checking whether the object that is given and the method are the same.
         *
         * @param o an object for this method to be compared to
         * @return true if equal, false otherwise
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MethodWrapper that = (MethodWrapper) o;
            return Objects.equals(method.getReturnType(), that.method.getReturnType()) &&
                    method.getName().equals(that.method.getName()) &&
                    Arrays.equals(method.getParameterTypes(), that.method.getParameterTypes());
        }
    }
}
