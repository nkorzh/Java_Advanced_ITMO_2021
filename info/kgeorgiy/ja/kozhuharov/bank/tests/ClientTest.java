package info.kgeorgiy.ja.kozhuharov.bank.tests;

import info.kgeorgiy.ja.kozhuharov.bank.Account;
import info.kgeorgiy.ja.kozhuharov.bank.Bank;
import info.kgeorgiy.ja.kozhuharov.bank.Client;
import info.kgeorgiy.ja.kozhuharov.bank.Person;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientTest {
    private static final int defaultBankPort = 8888;
    private static Registry registry;
    private static Bank bank = null;

    @BeforeClass
    public static void init() {
        System.out.println("Running client tests");
        try {
            registry = Utils.startRmiRegistry(Registry.REGISTRY_PORT);
            bank = Utils.startBank(defaultBankPort);
        } catch (RemoteException e) {
            System.err.println("Can't start RMI Registry" + e.getMessage());
        }
    }

    @AfterClass
    public static void finish() {
        try {
            UnicastRemoteObject.unexportObject(bank, true);
        } catch (NoSuchObjectException e) {
            System.err.println("Could not unexport bank " + e.getMessage());
        }
        Utils.unexportRegistry(registry);
    }

    @Test
    public void test01_increments() throws RemoteException {
        Person p = Utils.createPerson(230);
        for (int i = 0; i < 10; i++) {
            Client.main(p.getFirstName(), p.getLastName(), p.getPassport(), "123", "1");
        }
        Account account = bank.getAccount(p, "123");
        assertEquals(10, account.getAmount());
    }

    @Test
    public void test02_twoAccounts() throws RemoteException {
        Person p1 = Utils.createPerson(228);
        for (int i = 0; i < 10; i++) {
            Client.main(p1.getFirstName(), p1.getLastName(), p1.getPassport(), "666", "10");
            Client.main(p1.getFirstName(), p1.getLastName(), p1.getPassport(), "667", "-3");
        }
        Account first = bank.getAccount(p1, "666");
        Account second = bank.getAccount(p1, "667");
        assertEquals(100, first.getAmount());
        assertEquals(-30, second.getAmount());
    }
}
