package info.kgeorgiy.ja.kozhuharov.bank;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.net.*;

public final class Server {
    private final static int DEFAULT_PORT = 8080;

    public static void main(final String... args) {
        final int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        try {
            final Bank bank = new RemoteBank(port);
            UnicastRemoteObject.exportObject(bank, port);
            Registry registry = LocateRegistry.createRegistry(DEFAULT_PORT);
            registry.bind("//localhost/bank", bank);
            System.out.println("Server started");
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (final AlreadyBoundException e) {
            e.printStackTrace();
        }
    }
}
