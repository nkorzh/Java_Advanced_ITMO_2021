## TextStatistics

**Task**: 

1. Implement a class `TextStatistics`, Create a TextStatistics application that analyzes texts in different languages.
    1. Command line arguments:
        - text
        - locale,
        - output locale,
        - text
        - file,
        - report file.
    2. Supported text locales: all locales available in the system.
    3. Supported output locales: Russian and English.
    4. The files are UTF-8 encoded.
    5. Statistics should be calculated in the following categories:
        - sentences,
        - words,
        - numbers,
        - money,
        - dates.
    6. The following statistics should be collected for each category:
        - number of occurrences,
        - number of different values,
        - minimum value,
        - maximum value,
        - minimum length,
        - maximum length,
        - average value/length.
   
2. You can reckon that the entire text can be stored in memory.
3. When completing the task, you should pay attention to:
    - Decomposition of messages for localization
    - Matching messages by gender and number
4. Write tests that test the above behavior of the application.
    - To implement the tests, we recommend using JUnit ([Tutorial](https://www.petrikainulainen.net/programming/testing/junit-5-tutorial-writing-our-first-test-class/)). Many usage examples can be found in the tests to previous tasks.
    - If you are familiar with another test framework (for example, [TestNG](https://testng.org/)), you can use it.
    - You can't use self-written frameworks and tests that run through main.

### Note

- `.lib` files are required to launch scripts, they are supposed to be located at `./../../java-advanced-2021/lib`, where `./` is the root of this repository (JUnit 4.11 is necessary).
- Scripts should be launched from the directory they are located.
- This code does not fulfil all requirements, such as `String` comparing with `Collator`, which is necessary to make `TextStatistics` fully internalized.
