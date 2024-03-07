import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

public class TestAll {
    private static int count = 0;

    public static void main(String[] args) throws IOException {
        int testAmount = 10;
        int size = 8;

        FileWriter winsFile;
        FileWriter losesFile;
        FileWriter drawsFile;
        FileWriter averageMoveFile;
        FileWriter longestMoveFile;

        // Create files
        winsFile = new FileWriter("wins.csv");
        losesFile = new FileWriter("loses.csv");
        drawsFile = new FileWriter("draws.csv");
        averageMoveFile = new FileWriter("average.csv");
        longestMoveFile = new FileWriter("longest.csv");

        // Write header of files
        winsFile.write("White\\Black,");
        losesFile.write("White\\Black,");
        drawsFile.write("White\\Black,");
        averageMoveFile.write("White\\Black,");
        longestMoveFile.write("White\\Black,");
        for (int i = 0; i < args.length; i++) {
            winsFile.write(args[i]);
            losesFile.write(args[i]);
            drawsFile.write(args[i]);
            averageMoveFile.write(args[i]);
            longestMoveFile.write(args[i]);
            if (i < args.length - 1) {
                winsFile.write(",");
                losesFile.write(",");
                drawsFile.write(",");
                averageMoveFile.write(",");
                longestMoveFile.write(",");
            }
            if (i == args.length - 1) {
                winsFile.write("\n");
                losesFile.write("\n");
                drawsFile.write("\n");
                averageMoveFile.write("\n");
                longestMoveFile.write("\n");
            }
        }

        int threads = Runtime.getRuntime().availableProcessors();
        System.out.println(threads + " processors detected");

        for (int bot2 = 0; bot2 < args.length; bot2++) {
            // Write left column of tables
            winsFile.write(args[bot2] + ",");
            losesFile.write(args[bot2] + ",");
            drawsFile.write(args[bot2] + ",");
            averageMoveFile.write(args[bot2] + ",");
            longestMoveFile.write(args[bot2] + ",");

            for (int bot1 = 0; bot1 < args.length; bot1++) {
                if (bot1 == bot2) {
                    winsFile.write("N/A");
                    losesFile.write("N/A");
                    drawsFile.write("N/A");
                    averageMoveFile.write("N/A");
                    longestMoveFile.write("N/A");
                    if (bot1 < args.length - 1) {
                        winsFile.write(",");
                        losesFile.write(",");
                        drawsFile.write(",");
                        averageMoveFile.write(",");
                        longestMoveFile.write(",");
                    }
                    continue;
                }

                System.out.println("Testing " + args[bot1] + " against " + args[bot2]);

                int ai1Wins = 0;
                int ai2Wins = 0;
                long ai1TotalTime = 0;
                long ai2TotalTime = 0;
                long ai1LongestTime = 0;
                long ai2LongestTime = 0;
                int ai1Moves = 0;
                int ai2Moves = 0;
                int draws = 0;

                ExecutorService executor = Executors.newFixedThreadPool(threads);
                List<Future<Stat>> futures = new ArrayList<>();

                for (int i = 0; i < testAmount; i++) {
                    String botA = args[bot1];
                    String botB = args[bot2];
                    Callable<Stat> task = () -> test(size, botA, botB);
                    Future<Stat> future = executor.submit(task);
                    futures.add(future);
                }

                System.out.print("Tested 0 times");

                for (Future<Stat> future : futures) {
                    try {
                        Stat stat = future.get();
                        switch (stat.winner) {
                            case Result.AI1:
                                ai1Wins++;
                                break;
                            case Result.AI2:
                                ai2Wins++;
                                break;
                            case Result.DRAW:
                                draws++;
                                break;
                        }
                        ai1TotalTime += stat.ai1TotalTime;
                        ai2TotalTime += stat.ai2TotalTime;
                        ai1LongestTime = Math.max(ai1LongestTime, stat.ai1LongestTime);
                        ai2LongestTime = Math.max(ai2LongestTime, stat.ai2LongestTime);
                        ai1Moves += stat.ai1Moves;
                        ai2Moves += stat.ai2Moves;

                    } catch (InterruptedException | ExecutionException e) {
                        System.err.println(e);
                    }
                }

                System.out.println();
                count = 0;

                executor.shutdown();

                winsFile.write(Integer.toString(ai1Wins));
                losesFile.write(Integer.toString(ai2Wins));
                drawsFile.write(Integer.toString(draws));
                averageMoveFile.write(Long.toString(ai1TotalTime / 1_000_000 / ai1Moves));
                longestMoveFile.write(Long.toString(ai1LongestTime / 1_000_000));

                if (bot1 < args.length - 1) {
                    winsFile.write(",");
                    losesFile.write(",");
                    drawsFile.write(",");
                    averageMoveFile.write(",");
                    longestMoveFile.write(",");
                }
                if (bot1 == args.length - 1) {
                    winsFile.write("\n");
                    losesFile.write("\n");
                    drawsFile.write("\n");
                    averageMoveFile.write("\n");
                    longestMoveFile.write("\n");
                }
            }
        }

        winsFile.close();
        losesFile.close();
        drawsFile.close();
        averageMoveFile.close();
        longestMoveFile.close();
    }

    public static Stat test(int size, String bot1, String bot2) {
        String errMsg = null;
        boolean err = false;

        IOthelloAI ai1 = null;
        IOthelloAI ai2 = null;

        try {
            ai1 = parseGameLogicParam(bot1);
            ai2 = parseGameLogicParam(bot2);
            if (ai1 == null || ai2 == null) {
                errMsg = "Invalid argument";
                err = true;
            }
        } catch (ClassNotFoundException cnf) {
            errMsg = cnf.toString();
            err = true;
        } catch (NoSuchMethodException nsme) {
            errMsg = "Your GameInstance had no constructor.";
            err = true;
        } catch (InstantiationException ie) {
            errMsg = "Your GameInstance could not be instantiated.";
            err = true;
        } catch (IllegalAccessException iae) {
            errMsg = "Your GameInstance caused an illegal access exception.";
            err = true;
        } catch (InvocationTargetException ite) {
            errMsg = "Your GameInstance constructor threw an exception: " + ite.toString();
            err = true;
        }

        if (err) {
            System.err.println(errMsg);
            return null;
        }

        GameState s = new GameState(size, 1);
        Stat stat = new Stat();
        while (!s.isFinished()) {
            if (s.legalMoves().size() == 0) {
                s.changePlayer();
                continue;
            }
            long startTime = System.nanoTime();
            Position move = getPlaceForNextToken(s, ai1, ai2);
            long duration = System.nanoTime() - startTime;
            if (s.getPlayerInTurn() == 1) {
                stat.ai1TotalTime += duration;
                stat.ai1LongestTime = Math.max(stat.ai1LongestTime, duration);
                stat.ai1Moves++;
            } else {
                stat.ai2TotalTime += duration;
                stat.ai2LongestTime = Math.max(stat.ai2LongestTime, duration);
                stat.ai2Moves++;
            }
            if (!s.insertToken(move)) {
                if (s.getPlayerInTurn() == 1)
                    System.out.println("AI1 made an illegal move");
                else
                    System.out.println("AI2 made an illegal move");
            }
        }
        int[] tokens = s.countTokens();
        if (tokens[0] > tokens[1])
            stat.winner = Result.AI1;
        else if (tokens[1] > tokens[0])
            stat.winner = Result.AI2;
        else
            stat.winner = Result.DRAW;

        count++;
        System.out.print("\r" + "Tested " + count + " times");

        return stat;
    }

    /**
     * Returns an instance of the specified class implementing IOthelloLogic
     * 
     * @param cmdParam String from the command line that should be a path to a java
     *                 class implementing IOthelloLogic
     * @throws TBD
     */
    public static IOthelloAI parseGameLogicParam(String cmdParam)
            throws ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException,
            InvocationTargetException {
        return (IOthelloAI) Class.forName(cmdParam).getConstructor().newInstance();
    }

    /**
     * Get a position to place the next token
     */
    private static Position getPlaceForNextToken(GameState state, IOthelloAI ai1, IOthelloAI ai2) {
        if (state.getPlayerInTurn() == 2)
            return ai2.decideMove(state);
        else {
            return ai1.decideMove(state);
        }
    }
}

enum Result {
    AI1,
    AI2,
    DRAW;
}

class Stat {
    public long ai1TotalTime = 0;
    public long ai2TotalTime = 0;
    public long ai1LongestTime = 0;
    public long ai2LongestTime = 0;
    public int ai1Moves = 0;
    public int ai2Moves = 0;
    public Result winner;
}
