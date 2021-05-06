package info.kgeorgiy.ja.kozhuharov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodeGenerator {
    private static final String lineSep = System.lineSeparator();
    private static final String lineSepDouble = System.lineSeparator().repeat(2);
    private static final String bodyOpenBracket = "{";
    private static final String bodyCloseBracket = "}";
    private static final String roundOpenBracket = "(";
    private static final String roundCloseBracket = ")";
    private static final String comma = ", ";
    private static final String semiColon = ";";
    private static final String space = " ";
    private static final String tab = "\t";
    private static final String tabDouble = "\t".repeat(2);
    private static final String emptyString = "";
    private static final String implNameSuffix = "Impl";
    // Keywords
    private static final String PACKAGE = "package";
    private static final String CLASS = "class";
    private static final String IMPLEMENTS = "implements";
    private static final String EXTENDS = "extends";
    private static final String THROWS = "throws";
    private static final String SUPER = "super";
    private static final String RETURN = "return";
    // Access modifiers
    private static final String PUBLIC = "public";

    private static String getClassName(final Class<?> token) {
        return token.getSimpleName() + implNameSuffix;
    }

    private static String declarePackage(final Class<?> token) {
        return token.getPackageName().equals(emptyString) ?
                emptyString :
                String.join(space, PACKAGE, token.getPackage().getName()) + semiColon + lineSep;
    }

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

    /**
     * Enumerations
     */
    private static String enumerateArguments(final Parameter[] parameters, final boolean isDeclaration) {
        return Arrays.stream(parameters)
                .map(param ->
                        ((isDeclaration ? param.getType().getCanonicalName() + space : emptyString) +
                                param.getName()))
                .collect(Collectors.joining(comma));
    }

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

    /**
     * Methods and constructor generation
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

    private static String generateExecutable(final Executable executable, final String name) {
        return String.join(space,
                PUBLIC,
                name,
                roundOpenBracket + enumerateArguments(executable.getParameters(), true) + roundCloseBracket,
                enumerateExceptions(executable.getExceptionTypes()),
                bodyOpenBracket);
    }

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

    static class MethodWrapper {
        final private Method method;

        MethodWrapper(Method method) {
            this.method = method;
        }

        Method getMethod() {
            return method;
        }

        boolean isAbstract() {
            return Modifier.isAbstract(method.getModifiers());
        }

        @Override
        public int hashCode() {
            final int POW = 43;
            return method.getName().hashCode() +
                    POW * Arrays.hashCode(method.getParameterTypes());
        }

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
