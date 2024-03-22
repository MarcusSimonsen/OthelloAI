# OthelloAI project

This project is completed by the group named 1 on LearnIT, by the students:

| Name                     | ITU email    |
|--------------------------|--------------|
| Christian Ivert Andersen | cian@itu.dk  |
| Magnus Brun Larsen       | brml@itu.dk  |
| Marcus Henrik Simonsen   | mhsi@itu.dk  |
| Mikkel Toft              | mikto@itu.dk |

## How to run

Our bot is compiled like any other java file.

```sh
javac OthelloAI1.java
```

The DumAI bot is compiled like described in the project handout:

```sh
javac DumAI.java
```

(This will also compile all the testing bots.)

The bots can now play against each other like described in the project handout by first compiling the main `Othello.java` program and then running against each other:

```sh
javac Othello.java
```

```sh
java Othello OthelloAI1 DumAI
```

## How testing was done

In order to ease the process of testing the performance of the bots against each other, two scripts was created.
These are called `Test.java` and `TestAll.java`.
In order to compile these, the following files must be compiled first:

- `Stat.java`
- `Result.java`

These are used in the two test scripts.

Both of the test scripts include a variable for setting the number of games to run called `testAmount`.

Also, both scripts are multithreaded in order to minimize running time.
Since none of our bots are multithreaded, this is not a problem.

### Test two bots against each other

The `Test.java` script can be used to test two bots against each other using the following command (after compiling):

```sh
java Test BotA BotB
```

where:

- `BotA` is the bot playing as black to be tested,
- `BotB` is the bot playing as white to be tested.

### Testing all/many bots against each other

The `TestAll.java` script can be used to test any number (at least two) against each other playing all pairs of given bots against each other both as white and black.

```sh
java TestAll BotA BotB ... BotZ
```

Running this script will create the following `.csv` files:

- `wins.csv` a csv "table" with the number of wins for the bot playing as black,
- `loses.csv` a csv "table" with the number of loses for the bot playing as black,
- `draws.csv` a csv "table" with the number of draws in during the games between the two bots,
- `average.csv` a csv "table" with the average time to calculate a move for the bot playing as black,
- `longest.csv` a csv "table" with the longest time to calculate a move for the bot playing as black,

The generated tables are all formatted with the bots playing as black in the first row and the bots playing as white in the first column.
These `.csv` should then be relatively easy to import into any Excel-like data processor program for further analysis.

