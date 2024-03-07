import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Map;
import java.util.TreeMap;

abstract class BaseAI implements IOthelloAI {
    protected static int depthLimit = 6;
    protected int me;
    protected static int[][] heuristic = {
            { 4, -3, 2, 2, 2, 2, -3, 4 },
            { -3, -4, -1, -1, -1, -1, -4, -3 },
            { 2, -1, 1, 0, 0, 1, -1, 2 },
            { 2, -1, 0, 1, 1, 0, -1, 2 },
            { 2, -1, 0, 1, 1, 0, -1, 2 },
            { 2, -1, 1, 0, 0, 1, -1, 2 },
            { -3, -4, -1, -1, -1, -1, -4, -3 },
            { 4, -3, 2, 2, 2, 2, -3, 4 }
    };
    private Map<GameState, Pair> explored;

    protected class Pair {
        double utility;
        Position move;

        public Pair(double utility, Position move) {
            this.utility = utility;
            this.move = move;
        }
    }

    /**
     * Simple utility function which is amount of tokens minus amount of enemy
     * tokens
     */
    protected abstract double Eval(GameState s);

    protected boolean isCutOff(GameState s, int ply) {
        if (ply > depthLimit)
            return true;
        if (s.isFinished())
            return true;
        return false;
    }

    protected Pair MaxValue(GameState s, int ply, double alpha, double beta) {
        if (explored.containsKey(s))
            return explored.get(s);
        if (isCutOff(s, ply))
            return new Pair(Eval(s), null);
        Pair v = new Pair(Double.NEGATIVE_INFINITY, null);

        List<Position> l = s.legalMoves();
        Collections.shuffle(l);
        PriorityQueue<Position> moves = new PriorityQueue<Position>(new MaxMoveComparator());
        moves.addAll(l);
        while (!moves.isEmpty()) {
            Position a = moves.remove();
            GameState new_s = new GameState(s.getBoard(), s.getPlayerInTurn());
            new_s.insertToken(a);
            Pair p = MinValue(new_s, ply + 1, alpha, beta);
            if (p.utility > v.utility) {
                v = new Pair(p.utility, a);
                if (v.utility > alpha)
                    alpha = v.utility;
            }
            if (v.utility >= beta)
                return v;
        }

        v.utility = v.utility == Double.POSITIVE_INFINITY || v.utility == Double.NEGATIVE_INFINITY ? Eval(s)
                : v.utility;
        explored.put(s, v);
        return v;
    }

    protected Pair MinValue(GameState s, int ply, double alpha, double beta) {
        if (explored.containsKey(s))
            return explored.get(s);
        if (isCutOff(s, ply))
            return new Pair(Eval(s), null);
        Pair v = new Pair(Double.POSITIVE_INFINITY, null);

        List<Position> l = s.legalMoves();
        Collections.shuffle(l);
        PriorityQueue<Position> moves = new PriorityQueue<Position>(new MinMoveComparator());
        moves.addAll(l);
        while (!moves.isEmpty()) {
            Position a = moves.remove();
            GameState new_s = new GameState(s.getBoard(), s.getPlayerInTurn());
            new_s.insertToken(a);
            Pair p = MaxValue(new_s, ply + 1, alpha, beta);
            if (p.utility < v.utility) {
                v = new Pair(p.utility, a);
                if (v.utility < beta)
                    beta = v.utility;
            }
            if (v.utility >= alpha)
                return v;
        }

        v.utility = v.utility == Double.POSITIVE_INFINITY || v.utility == Double.NEGATIVE_INFINITY ? Eval(s)
                : v.utility;
        explored.put(s, v);
        return v;
    }

    @Override
    public Position decideMove(GameState s) {
        explored = new TreeMap<>(new GameStateComparator());
        Pair p = MaxValue(s, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        me = s.getPlayerInTurn();
        return p.move;
    }
}

class MaxMoveComparator implements Comparator<Position> {
    private static int[][] heuristic = {
            { 4, -3, 2, 2, 2, 2, -3, 4 },
            { -3, -4, -1, -1, -1, -1, -4, -3 },
            { 2, -1, 1, 0, 0, 1, -1, 2 },
            { 2, -1, 0, 1, 1, 0, -1, 2 },
            { 2, -1, 0, 1, 1, 0, -1, 2 },
            { 2, -1, 1, 0, 0, 1, -1, 2 },
            { -3, -4, -1, -1, -1, -1, -4, -3 },
            { 4, -3, 2, 2, 2, 2, -3, 4 }
    };

    @Override
    public int compare(Position move1, Position move2) {
        return heuristic[move1.col][move1.row] - heuristic[move2.col][move2.row];
    }
}

class MinMoveComparator implements Comparator<Position> {
    private static int[][] heuristic = {
            { 4, -3, 2, 2, 2, 2, -3, 4 },
            { -3, -4, -1, -1, -1, -1, -4, -3 },
            { 2, -1, 1, 0, 0, 1, -1, 2 },
            { 2, -1, 0, 1, 1, 0, -1, 2 },
            { 2, -1, 0, 1, 1, 0, -1, 2 },
            { 2, -1, 1, 0, 0, 1, -1, 2 },
            { -3, -4, -1, -1, -1, -1, -4, -3 },
            { 4, -3, 2, 2, 2, 2, -3, 4 }
    };

    @Override
    public int compare(Position move1, Position move2) {
        return (-heuristic[move1.col][move1.row]) - (-heuristic[move2.col][move2.row]);
    }
}

class GameStateComparator implements Comparator<GameState> {
    @Override
    public int compare(GameState s1, GameState s2) {
        int size1 = s1.getBoard().length;
        int size2 = s2.getBoard().length;
        if (size1 != size2) {
            if (size1 < size2)
                return -1;
            else if (size2 < size1)
                return 1;
            return 0;
        } else {
            int[][] b1 = s1.getBoard();
            int[][] b2 = s2.getBoard();

            for (int i = 0; i < b1.length; i++) {
                for (int j = 0; j < b1[i].length; j++) {
                    if (b1[i][j] != b2[i][j]) {
                        return b1[i][j] < b2[i][j] ? -1 : 1;
                    }
                }
            }
        }

        return 0;
    }
}

class OthelloAI1 extends BaseAI {
    public OthelloAI1() {
    }

    @Override
    protected double Eval(GameState s) {
        int[][] board = s.getBoard();
        double value = 0;
        if (s.isFinished()) {
            int[] tokens = s.countTokens();
            if (tokens[me - 1] > tokens[me % 2])
                value = Double.MAX_VALUE;
            else if (tokens[me - 1] < tokens[me % 2])
                value = Double.MIN_VALUE;
            else
                value = 0;
        } else if (board.length != 8) {
            int[] tokens = s.countTokens();
            value = (me == 1 ? tokens[0] : tokens[1]) - (me == 1 ? tokens[1] : tokens[0]);
        } else {
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    if (board[i][j] == 0)
                        continue;
                    if (me == 1)
                        value += board[i][j] == 1 ? heuristic[i][j] : -heuristic[i][j];
                    else
                        value += board[i][j] == 2 ? heuristic[i][j] : -heuristic[i][j];
                }
            }
        }
        return value;
    }
}

class Maximize extends BaseAI {
    public Maximize() {
    }

    @Override
    protected double Eval(GameState s) {
        double value = 0;
        int[] tokens = s.countTokens();
        value = me == 1 ? tokens[0] : tokens[1];
        return value;
    }
}

class MaximizeWinning extends BaseAI {
    public MaximizeWinning() {
    }

    @Override
    protected double Eval(GameState s) {
        double value = 0;
        if (s.isFinished()) {
            int[] tokens = s.countTokens();
            if (tokens[me - 1] > tokens[me % 2])
                value = Double.MAX_VALUE;
            else if (tokens[me - 1] < tokens[me % 2])
                value = Double.MIN_VALUE;
            else
                value = 0;
        } else {
            int[] tokens = s.countTokens();
            value = me == 1 ? tokens[0] : tokens[1];
        }
        return value;
    }
}

class Balance extends BaseAI {
    public Balance() {
    }

    @Override
    protected double Eval(GameState s) {
        double value = 0;
        int[] tokens = s.countTokens();
        value = (me == 1 ? tokens[0] : tokens[1]) - (me == 1 ? tokens[1] : tokens[0]);
        return value;
    }
}

class BalanceWinning extends BaseAI {
    public BalanceWinning() {
    }

    @Override
    protected double Eval(GameState s) {
        double value = 0;
        if (s.isFinished()) {
            int[] tokens = s.countTokens();
            if (tokens[me - 1] > tokens[me % 2])
                value = Double.MAX_VALUE;
            else if (tokens[me - 1] < tokens[me % 2])
                value = Double.MIN_VALUE;
            else
                value = 0;
        } else {
            int[] tokens = s.countTokens();
            value = (me == 1 ? tokens[0] : tokens[1]) - (me == 1 ? tokens[1] : tokens[0]);
        }
        return value;
    }
}

class MaximizeWeighted extends BaseAI {
    public MaximizeWeighted() {
    }

    @Override
    protected double Eval(GameState s) {
        int[][] board = s.getBoard();
        double value = 0;
        if (board.length != 8) {
            int[] tokens = s.countTokens();
            value = (me == 1 ? tokens[0] : tokens[1]) - (me == 1 ? tokens[1] : tokens[0]);
        } else {
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    if (board[i][j] == 0)
                        continue;
                    if (me == 1)
                        value += board[i][j] == 1 ? heuristic[i][j] : 0;
                    else
                        value += board[i][j] == 2 ? heuristic[i][j] : 0;
                }
            }
        }
        return value;
    }
}

class BalanceWeighted extends BaseAI {
    public BalanceWeighted() {
    }

    @Override
    protected double Eval(GameState s) {
        int[][] board = s.getBoard();
        double value = 0;
        if (board.length != 8) {
            int[] tokens = s.countTokens();
            value = (me == 1 ? tokens[0] : tokens[1]) - (me == 1 ? tokens[1] : tokens[0]);
        } else {
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    if (board[i][j] == 0)
                        continue;
                    if (me == 1)
                        value += board[i][j] == 1 ? heuristic[i][j] : -heuristic[i][j];
                    else
                        value += board[i][j] == 2 ? heuristic[i][j] : -heuristic[i][j];
                }
            }
        }
        return value;
    }
}

class BalanceWeightedWinning extends BaseAI {
    public BalanceWeightedWinning() {
    }

    @Override
    protected double Eval(GameState s) {
        int[][] board = s.getBoard();
        double value = 0;
        if (s.isFinished()) {
            int[] tokens = s.countTokens();
            if (tokens[me - 1] > tokens[me % 2])
                value = Double.MAX_VALUE;
            else if (tokens[me - 1] < tokens[me % 2])
                value = Double.MIN_VALUE;
            else
                value = 0;
        } else if (board.length != 8) {
            int[] tokens = s.countTokens();
            value = (me == 1 ? tokens[0] : tokens[1]) - (me == 1 ? tokens[1] : tokens[0]);
        } else {
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    if (board[i][j] == 0)
                        continue;
                    if (me == 1)
                        value += board[i][j] == 1 ? heuristic[i][j] : -heuristic[i][j];
                    else
                        value += board[i][j] == 2 ? heuristic[i][j] : -heuristic[i][j];
                }
            }
        }
        return value;

    }
}
