package info.kgeorgiy.ja.kozhuharov.bank.tests;

import info.kgeorgiy.ja.kozhuharov.bank.Bank;
import info.kgeorgiy.ja.kozhuharov.bank.Person;
import info.kgeorgiy.ja.kozhuharov.bank.RemoteBank;
import info.kgeorgiy.ja.kozhuharov.bank.RemotePerson;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Utils {
    public static Bank startBank(final int bankPort) {
        final Bank bank = new RemoteBank(bankPort);
        try {
            UnicastRemoteObject.exportObject(bank, bankPort);
            Naming.rebind("//localhost/bank", bank);
        } catch (RemoteException e) {
            System.err.println("Couldn't export object: " + e.getMessage());
            e.printStackTrace();
        } catch (MalformedURLException e) {
            System.err.println("Malformed URL");
        }
        return bank;
    }

    public static Registry startRmiRegistry(final int registryPort) throws RemoteException {
        return LocateRegistry.createRegistry(registryPort);
    }

    public static void unexportRegistry(final Registry registry) {
        try {
            UnicastRemoteObject.unexportObject(registry, true);
        } catch (NoSuchObjectException e) {
            System.err.println("Could not unexport object " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Person createPerson(int seed) {
        Person p = null;
        try {
            p = new RemotePerson(
                    "firstName" + seed,
                    "lastName" + seed,
                    "passport" + seed,
                    228 + seed
            );
        } catch (final RemoteException ignored) {
            System.err.println("Cant create person with seed " + seed);
        }
        return p;
    }
}