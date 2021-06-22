## Bank

**Task**: 
1. Extend bank application
    1. Add the ability to work with individuals to the banking application.
    2. You can request a first name, last name, and passport number from an individual.
    3. Local individuals (`LocalPerson`) must be transmitted using the serialization mechanism.
    4. Remote individuals (`RemotePerson`) must be transmitted using remote objects.
    5. It should be possible to search for an individual by the passport number, with the choice of the return type.
    6. It should be possible to create a record about an individual based on their data.
    7. An individual may have multiple accounts to which access must be granted.
    8. An individual's account with a subId must match a bank account with an id of the type passport: subId.
    9. Changes made to the bank account (creating and changing the balance) should be visible to all the relevant `RemotePerson`, and only to those `LocalPerson` that were created after this change.
    10. Changes to accounts made via `RemotePerson` should be applied globally immediately, and changes made via `LocalPerson` should only be applied locally for that particular `LocalPerson`.
2. Implement an application that demonstrates working with individuals.
    1. Command line arguments: first name, last name, passport number of an individual, account number, change of the account amount.
    2. If there is no information about the specified individual, then it must be added. Otherwise, its data must be checked.
    3. If an individual does not have an account with the specified number, it is created with a zero balance.
    4. After updating the account amount, the new balance should be displayed on the console.
3. Write tests that test the above behavior of both the bank and the application.
    1. To implement the tests, we recommend using JUnit ([Tutorial](https://www.petrikainulainen.net/programming/testing/junit-5-tutorial-writing-our-first-test-class/)). Many usage examples can be found in the tests.
    2. If you are familiar with another test framework (for example, TestNG), you can use it.
    3. The jar files of the used libraries should be placed in the lib directory of your repository.
    4. You can't use self-written frameworks and tests that run through main.
4. **Difficult modification**
    1. Tests should not count on the presence of a running RMI Registry.
    2. Create a `BankTests` class that runs the tests.
    3. Create a script that runs `BankTests` and returns a code (status) of 0 if successful and 1 if unsuccessful.
    4. Create a script that runs tests using the standard approach for your test framework. The return code must be the same as in the previous paragraph.

### Note

- `.lib` files are required to launch scripts, they are supposed to be located at `./../../java-advanced-2021/lib`, where `./` is the root of this repository.
- Scripts should be launched from the directory they are located.