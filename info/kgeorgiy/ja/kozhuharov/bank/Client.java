package info.kgeorgiy.ja.kozhuharov.bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import static info.kgeorgiy.ja.kozhuharov.bank.ConsoleUtils.*;

public class Client {
    public static void main(String... args) {
        try {
            final Bank bank;
            try {
                bank = (Bank) Naming.lookup("//localhost/bank");
            } catch (final NotBoundException e) {
                System.out.println("No bank with such name bound: " + e.getMessage());
                return;
            } catch (final MalformedURLException e) {
                System.out.println("Invalid url: " + e.getMessage());
                return;
            }
            if (!validateArgs(args)) {
                System.err.println("Invalid usage:");
                printUsage();
                return;
            }
            final int amountInc;
            final String firstName = args[0];
            final String lastName = args[1];
            final String passport = args[2];
            final String accountId = args[3];
            try {
                amountInc = Integer.parseInt(args[4]);
            } catch (final NumberFormatException nfe) {
                System.err.println("Error parsing integer (5-th arg).");
                printUsage();
                return;
            }
            Person person = bank.getRemotePerson(firstName, lastName, passport);
            if (person == null) {
                if (!bank.createPerson(firstName, lastName, passport)) {
                    System.out.println("Persons data is invalid. This passport belongs to someone else");
                    return;
                }
                person = bank.getRemotePerson(firstName, lastName, passport);
                System.out.println("New person has been created.");
            }
            Account account = bank.getAccount(person, accountId);
            if (account == null) {
                System.out.println("No such account, creating new one with subID: " + accountId);
                account = bank.createAccount(person, accountId);
                if (account != null) {
                    System.out.println("New account has been created.");
                } else {
                    System.err.println("Invalid person, can't create account.");
                    return;
                }
            }
            account.setAmount(account.getAmount() + amountInc);
            System.out.println("Current balance: " + account.getAmount() + " on account " + account.getId());
        } catch (final RemoteException e) {
            System.out.println("An error occurred while working with remote bank: " + e.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("Expected usage:");
        System.out.println("\t\t<first name> <last name> <passport> <account id> <account value increase amount>");
    }

    private static boolean validateArgs(final String[] args) {
        return !(args.length != 5 || anyNull((Object[]) args));
    }
}
