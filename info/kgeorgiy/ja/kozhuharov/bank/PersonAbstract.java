package info.kgeorgiy.ja.kozhuharov.bank;

public class PersonAbstract implements Person {
    private String firstName;
    private String lastName;
    private String passport;

    public PersonAbstract(final String firstName, final String lastName, final String passport) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passport = passport;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getPassport() {
        return passport;
    }
}