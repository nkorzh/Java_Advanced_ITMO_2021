package info.kgeorgiy.ja.kozhuharov.bank.tests;

import info.kgeorgiy.ja.kozhuharov.bank.*;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class BankTest {
    private static Bank bank;
    private static Registry registry;
    private static Random random;
    private static ConcurrentMap<Integer, Person> persons;
    private static final int defaultBankPort = 8888;

    @BeforeClass
    public static void initBank() {
        persons = new ConcurrentHashMap<>();
        random = new Random(System.currentTimeMillis());
        System.out.println("Running remote bank tests");
        try {
            registry = Utils.startRmiRegistry(Registry.REGISTRY_PORT);
        } catch (RemoteException e) {
            System.err.println("Can't start RMI Registry" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Before
    public void createBank() {
        if (bank != null) {
            try {
                UnicastRemoteObject.unexportObject(bank, true);
            } catch (NoSuchObjectException e) {
                System.err.println("Unexporting object not found " + e.getMessage());
            }
        }
        bank = Utils.startBank(defaultBankPort);
    }

    @AfterClass
    public static void finish() {
        try {
            UnicastRemoteObject.unexportObject(bank, true);
        } catch (NoSuchObjectException e) {
            System.err.println("Could not unexport bank " + e.getMessage());
        }
        try {
            UnicastRemoteObject.unexportObject(registry, true);
        } catch (NoSuchObjectException e) {
            System.err.println("Could not unexport object " + e.getMessage());
        }
    }

    @Test
    public void test_null_to_nonexisting_person() throws RemoteException {
        assertNull(bank.getLocalPerson("abra", "cadabra", "123123123"));
        assertNull(bank.getLocalPerson("stolen", "passport", "007"));
    }

    @Test
    public void test_no_accounts_for_just_registered() throws RemoteException {
        Person person = personRandom();
        while (!register(person)) {
            person = personRandom();
        }
        assertTrue(bank.getAllAccountsSubIds(person).isEmpty());
    }

    @Test
    public void test_register_and_find_same() throws RemoteException {
        Person expectedP = personRandom();
        while (!register(expectedP)) {
            expectedP = personRandom();
        }
        final Person actualP = bank.getRemotePerson(expectedP.getFirstName(), expectedP.getLastName(), expectedP.getPassport());
        assertEqualPersons(expectedP, actualP);
    }

    @Test
    public void test_register_many() throws RemoteException {
        List<Person> people = Stream.generate(BankTest::personRandom)
                .limit(100)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        for (Person p : people) {
            if (!bank.createPerson(p.getFirstName(), p.getLastName(), p.getPassport())) {
                continue;
            }
            assertEqualPersons(p, bank.getRemotePerson(p.getFirstName(), p.getLastName(), p.getPassport()));
        }
    }

    @Test
    public void test_all_account_names_saved() throws RemoteException {
        Person person = personRandom();
        while (!register(person)) {
            person = personRandom();
        }
        Set<String> subIds = Stream.generate(() -> String.valueOf(random.nextInt(Integer.MAX_VALUE)))
                .limit(random.nextInt(200))
                .collect(Collectors.toSet());
        for (String subId : subIds) {
            bank.createAccount(person, subId);
        }
        Set<String> personAccounts = bank.getAllAccountsSubIds(person);
        assertEquals("Wrong accounts amount", subIds.size(), personAccounts.size());
        assertTrue("Some accounts are absent", personAccounts.containsAll(subIds));
    }

    @Test
    public void test_same_account_register() throws RemoteException {
        Person person = personRandom();
        while (!register(person)) {
            person = personRandom();
        }
        int initialAccounts = bank.getAllAccountsSubIds(person).size();
        final String accName = subIdRandom();
        ExecutorService workers = Executors.newFixedThreadPool(20);
        final Person finalRegistered = person;
        for (int i = 0; i < random.nextInt(123); i++) {
            workers.submit(() -> bank.createAccount(finalRegistered, accName));
        }
        try {
            workers.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
        assertEquals("Added same account twice",
                initialAccounts + 1,
                bank.getAllAccountsSubIds(person).size()
        );
    }

    @Test
    public void test_local_after_addition_has_new_account() throws RemoteException {
        Person person = personRandom();
        while (!register(person)) {
            person = personRandom();
        }
        Person localPerson = bank.getLocalPerson(person.getFirstName(), person.getLastName(), person.getPassport());
        String subId = subIdRandom();
        while (bank.getAccount(person, subId) != null) {
            subId = subIdRandom();
        }
        Account remoteAccount = bank.createAccount(person, subId);
        assertNotNull("Someone else has created account with subID: " + subId, remoteAccount);
        assertNull("Local account has updates", ((LocalPerson) localPerson).getAccount(subId));
        assertNull("Bank does not differ local from remote person", bank.getAccount(localPerson, subId));

        localPerson = bank.getLocalPerson(person.getFirstName(), person.getLastName(), person.getPassport());
        assertNotNull("Local after account creation did not get update", bank.getAccount(localPerson, subId));
        assertNotNull("Remote person not updated", bank.getAccount(person, subId));
        remoteAccount.incrementAmount(50);
        assertNotEquals(
                "Local has same amount of money",
                bank.getAccount(person, subId).getAmount(),
                bank.getAccount(localPerson, subId).getAmount()
        );
    }

    @Test
    public void test_cant_add_with_wrong_credits() throws RemoteException {
        Person person = personRandom();
        while (!register(person)) {
            person = personRandom();
        }
        assertNull(bank.getLocalPerson("abra", person.getLastName(), person.getPassport()));
        assertNull(bank.getLocalPerson(person.getFirstName(), "cadabra", person.getPassport()));
        assertNull(bank.getLocalPerson(person.getFirstName(), person.getFirstName(), "-0000000"));
        Person fake = new RemotePerson(person.getFirstName(), "faasd", person.getPassport(), 229);
        assertNull(bank.createAccount(fake, "newacc"));
    }

    private static String subIdRandom() {
        return String.valueOf(random.nextInt(Integer.MAX_VALUE));
    }

    private static void assertEqualPersons(Person expected, Person actual) throws RemoteException {
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getPassport(), actual.getPassport());
    }

    private static boolean register(Person person) throws RemoteException {
        return bank.createPerson(person.getFirstName(), person.getLastName(), person.getPassport());
    }

    public static Person personRandom() {
        return getPerson(random.nextInt(500) + 1);
    }

    private static Person getPerson(int seed) {
        return persons.computeIfAbsent(seed, Utils::createPerson);
    }
}
