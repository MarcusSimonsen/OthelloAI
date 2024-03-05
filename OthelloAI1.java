import java.util.List;
import java.util.Collections;

public class OthelloAI1 implements IOthelloAI {
    private static int depthLimit = 6;
    private int me;
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

    private class Pair {
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
    public int Eval(GameState s) {
        int[][] board = s.getBoard();
        int value = 0;
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

    public boolean isCutOff(GameState s, int ply) {
        if (ply > depthLimit)
            return true;
        if (s.isFinished())
            return true;
        return false;
    }

    public Pair MaxValue(GameState s, int ply, double alpha, double beta) {
        if (isCutOff(s, ply))
            return new Pair(Eval(s), null);
        Pair v = new Pair(Double.NEGATIVE_INFINITY, null);

        List<Position> l = s.legalMoves();
        Collections.shuffle(l);
        for (Position a : l) {
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

        return new Pair(v.utility == Double.NEGATIVE_INFINITY ? Eval(s) : v.utility, v.move);
    }

    public Pair MinValue(GameState s, int ply, double alpha, double beta) {
        if (isCutOff(s, ply))
            return new Pair(Eval(s), null);
        Pair v = new Pair(Double.POSITIVE_INFINITY, null);

        List<Position> l = s.legalMoves();
        Collections.shuffle(l);
        for (Position a : l) {
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
        return new Pair(v.utility == Double.POSITIVE_INFINITY ? Eval(s) : v.utility, v.move);
    }

    public Position decideMove(GameState s) {
        Pair p = MaxValue(s, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        me = s.getPlayerInTurn();
        return p.move;
    }
}
