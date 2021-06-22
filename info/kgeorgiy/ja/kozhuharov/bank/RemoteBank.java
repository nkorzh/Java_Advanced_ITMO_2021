package info.kgeorgiy.ja.kozhuharov.bank;

import java.rmi.RemoteException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import static info.kgeorgiy.ja.kozhuharov.bank.ConsoleUtils.*;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Person> persons = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<String>> passportToAccSubIds = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Account createAccount(final Person person, final String subId) throws RemoteException {
        if (!validatePerson(person) || subId == null) {
            return null;
        }
        final String accountId = accountId(person.getPassport(), subId);
        if (accounts.containsKey(accountId)) {
            return accounts.get(accountId);
        }
        accounts.putIfAbsent(accountId, new RemoteAccount(accountId, port));
        passportToAccSubIds.computeIfAbsent(person.getPassport(), passport -> new ConcurrentSkipListSet<>())
                .add(subId);
        return accounts.get(accountId);
    }

    @Override
    public Account getAccount(final Person person, final String subId) throws RemoteException {
        if (!validatePerson(person) || subId == null) {
            return null;
        }
        if (person instanceof LocalPerson) {
            return ((LocalPerson) person).getAccount(subId);
        }
        return accounts.get(accountId(person.getPassport(), subId));
    }

    @Override
    public Set<String> getAllAccountsSubIds(final Person person) throws RemoteException {
        return validatePerson(person) ? passportToAccSubIds.get(person.getPassport()) : null;
    }

    @Override
    public boolean createPerson(final String firstName, final String lastName, final String passport)
            throws RemoteException {
        if (anyNull(firstName, lastName, passport) || persons.containsKey(passport)) {
            return false;
        }
        synchronized (this) {
            persons.put(passport, new RemotePerson(firstName, lastName, passport, port));
            passportToAccSubIds.put(passport, ConcurrentHashMap.newKeySet());
        }
        return true;
    }

    @Override
    public Person getLocalPerson(final String firstname, final String lastname, final String passport)
            throws RemoteException {
        final Person remotePerson = getRemotePerson(firstname, lastname, passport);
        if (remotePerson == null) {
            return null;
        }
        final ConcurrentMap<String, LocalAccount> personAccounts = new ConcurrentHashMap<>();
        for (String subId : getAllAccountsSubIds(remotePerson)) {
            personAccounts.put(subId, new LocalAccount(getAccount(remotePerson, subId)));
        }
        return new LocalPerson(
                remotePerson.getFirstName(),
                remotePerson.getLastName(),
                remotePerson.getPassport(),
                personAccounts
        );
    }

    @Override
    public Person getRemotePerson(String firstname, String lastname, String passport) throws RemoteException {
        return validatePerson(firstname, lastname, passport) ? persons.get(passport) : null;
    }

    public boolean validatePerson(final Person person) throws RemoteException {
        return person != null && validatePerson(person.getFirstName(), person.getLastName(), person.getPassport());
    }

    public boolean validatePerson(final String firstname, final String lastname, final String passport)
            throws RemoteException {
        if (anyNull(firstname, lastname, passport)) {
            return false;
        }
        Person person = persons.get(passport);
        return Objects.nonNull(person) &&
                Objects.equals(person.getFirstName(), firstname) &&
                Objects.equals(person.getLastName(), lastname);
    }

    private String accountId(final String passport, final String subId) {
        return passport + ":" + subId;
    }
}
