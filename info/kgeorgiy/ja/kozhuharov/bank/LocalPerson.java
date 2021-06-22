package info.kgeorgiy.ja.kozhuharov.bank;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class LocalPerson extends PersonAbstract implements Serializable {
    private final Map<String, LocalAccount> accounts;

    public LocalPerson(String firstName, String lastName, String pasportId, Map<String, LocalAccount> accounts) {
        super(firstName, lastName, pasportId);
        this.accounts = accounts;
    }

    public Set<String> getAccounts() {
        return accounts.keySet();
    }

    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }
}
