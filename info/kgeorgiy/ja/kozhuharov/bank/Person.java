package info.kgeorgiy.ja.kozhuharov.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Person extends Remote {

    String getFirstName() throws RemoteException;

    String getLastName() throws RemoteException;

    String getPassport() throws RemoteException;
}
