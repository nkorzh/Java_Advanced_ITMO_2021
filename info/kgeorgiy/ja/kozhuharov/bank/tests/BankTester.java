package info.kgeorgiy.ja.kozhuharov.bank.tests;

import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class BankTester {
    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        Result result = junit.run(BankTest.class, ClientTest.class);
        System.exit(result.wasSuccessful() ? 0 : 1);
    }
}