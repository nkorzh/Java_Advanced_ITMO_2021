package info.kgeorgiy.ja.kozhuharov.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface Bank extends Remote {

    Account createAccount(Person person, String subId) throws RemoteException;

    Account getAccount(Person person, String subId) throws RemoteException;

    Set<String> getAllAccountsSubIds(Person person) throws RemoteException;

    boolean createPerson(String name, String surname, String passport) throws RemoteException;

    Person getLocalPerson(String firstname, String lastname, String passport) throws RemoteException;

    Person getRemotePerson(String firstname, String lastname, String passport) throws RemoteException;
}
