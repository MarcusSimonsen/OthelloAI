import java.io.IOException;
import java.lang.reflect.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

public class Test {
    public static void main(String[] args) {
        int testAmount = 100;
        int ai1Wins = 0;
        int ai2Wins = 0;
        long ai1TotalTime = 0;
        long ai2TotalTime = 0;
        long ai1LongestTime = 0;
        long ai2LongestTime = 0;
        int ai1Moves = 0;
        int ai2Moves = 0;
        int draws = 0;
        int size = 8;
        int threads = Runtime.getRuntime().availableProcessors();

        if (args.length != 2) {
            System.err.println("Two arguments are required");
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<Stat>> futures = new ArrayList<>();

        for (int i = 0; i < testAmount; i++) {
            Callable<Stat> task = () -> test(size, args);
            Future<Stat> future = executor.submit(task);
            futures.add(future);
        }

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

        executor.shutdown();

        System.out.println("AI1 got " + ai1Wins + " wins");
        System.out.println("AI2 got " + ai2Wins + " wins");
        System.out.println(draws + " draws");
        System.out
                .println("AI1 used an average of " + (ai1TotalTime / 1_000_000 / ai1Moves) + " milliseconds per move");
        System.out
                .println("AI2 used an average of " + (ai2TotalTime / 1_000_000 / ai2Moves) + " milliseconds per move");
        System.out.println("AI1s slowest move took " + (ai1LongestTime / 1_000_000) + " milliseconds");
        System.out.println("AI2s slowest move took " + (ai2LongestTime / 1_000_000) + " milliseconds");
    }

    public static Stat test(int size, String[] args) {
        String errMsg = null;
        boolean err = false;

        IOthelloAI ai1 = null;
        IOthelloAI ai2 = null;

        try {
            ai1 = parseGameLogicParam(args[0]);
            ai2 = parseGameLogicParam(args[1]);
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
