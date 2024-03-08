import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Map;
import java.util.TreeMap;

abstract class MiniMax implements IOthelloAI {
    protected static int depthLimit = 6;
    protected int me;
    protected static float[][] heuristic = {
            { 4, -3, 2, 2, 2, 2, -3, 4 },
            { -3, -4, -1, -1, -1, -1, -4, -3 },
            { 2, -1, 1, 0, 0, 1, -1, 2 },
            { 2, -1, 0, 1, 1, 0, -1, 2 },
            { 2, -1, 0, 1, 1, 0, -1, 2 },
            { 2, -1, 1, 0, 0, 1, -1, 2 },
            { -3, -4, -1, -1, -1, -1, -4, -3 },
            { 4, -3, 2, 2, 2, 2, -3, 4 }
    };
    private Map<GS, Tuple> explored;

    protected record Tuple(Float utility, Position move) {
    }

    /**
     * Simple utility function which is amount of tokens minus amount of enemy
     * tokens
     */
    protected abstract float Eval(GS s);

    protected boolean isCutOff(GS s, int ply) {
        if (ply > depthLimit)
            return true;
        if (s.isFinished())
            return true;
        return false;
    }

    protected Tuple MaxValue(GS s, int ply, float alpha, float beta) {
        if (explored.containsKey(s))
            return explored.get(s);
        if (isCutOff(s, ply))
            return new Tuple(Eval(s), null);
        Tuple v = new Tuple(Float.NEGATIVE_INFINITY, null);

        List<Position> l = s.legalMoves();
        PriorityQueue<Position> moves = new PriorityQueue<Position>(new MaxMoveComparator());
        moves.addAll(l);
        while (!moves.isEmpty()) {
            Position a = moves.remove();
            GS new_s = new GS(s);
            new_s.insertToken(a);
            Tuple p = MinValue(new_s, ply + 1, alpha, beta);
            if (p.utility() > v.utility()) {
                v = new Tuple(p.utility(), a);
                if (v.utility() > alpha)
                    alpha = v.utility();
            }
            if (v.utility() >= beta)
                return v;
        }

        v = new Tuple(v.utility() == Float.POSITIVE_INFINITY || v.utility() == Float.NEGATIVE_INFINITY ? Eval(s)
                : v.utility(), v.move());
        explored.put(s, v);
        return v;
    }

    protected Tuple MinValue(GS s, int ply, float alpha, float beta) {
        if (explored.containsKey(s))
            return explored.get(s);
        if (isCutOff(s, ply))
            return new Tuple(Eval(s), null);
        Tuple v = new Tuple(Float.POSITIVE_INFINITY, null);

        List<Position> l = s.legalMoves();
        PriorityQueue<Position> moves = new PriorityQueue<Position>(new MinMoveComparator());
        moves.addAll(l);
        while (!moves.isEmpty()) {
            Position a = moves.remove();
            GS new_s = new GS(s);
            new_s.insertToken(a);
            Tuple p = MaxValue(new_s, ply + 1, alpha, beta);
            if (p.utility() < v.utility()) {
                v = new Tuple(p.utility(), a);
                if (v.utility() < beta)
                    beta = v.utility();
            }
            if (v.utility() >= alpha)
                return v;
        }

        v = new Tuple(v.utility() == Float.POSITIVE_INFINITY || v.utility() == Float.NEGATIVE_INFINITY ? Eval(s)
                : v.utility(), v.move());
        explored.put(s, v);
        return v;
    }

    @Override
    public Position decideMove(GameState s) {
        int[] tokens = new GS(s).countTokens();
        // System.out.println("Black: " + tokens[0]);
        // System.out.println("White: " + tokens[1]);
        explored = new TreeMap<>(new GSComparator());
        Tuple p = MaxValue(new GS(s), 0, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
        me = s.getPlayerInTurn();
        return p.move();
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

class GSComparator implements Comparator<GS> {
    @Override
    public int compare(GS s1, GS s2) {
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

class RandomizedArrayList<T> extends ArrayList<T> {
    @Override
    public boolean add(T e) {
        boolean res = super.add(e);
        int place = (int)(Math.random() * size());
        T tmp = get(place);
        set(place, e);
        set(size() - 1, tmp);
        return res;
    }
}

class Maximize extends MiniMax {
    public Maximize() {
    }

    @Override
    protected float Eval(GS s) {
        float value = 0;
        int[] tokens = s.countTokens();
        value = me == 1 ? tokens[0] : tokens[1];
        return value;
    }
}

class MaximizeWinning extends MiniMax {
    public MaximizeWinning() {
    }

    @Override
    protected float Eval(GS s) {
        float value = 0;
        if (s.isFinished()) {
            int[] tokens = s.countTokens();
            if (tokens[me - 1] > tokens[me % 2])
                value = Float.MAX_VALUE;
            else if (tokens[me - 1] < tokens[me % 2])
                value = Float.MIN_VALUE;
            else
                value = 0;
        } else {
            int[] tokens = s.countTokens();
            value = me == 1 ? tokens[0] : tokens[1];
        }
        return value;
    }
}

class Balance extends MiniMax {
    public Balance() {
    }

    @Override
    protected float Eval(GS s) {
        float value = 0;
        int[] tokens = s.countTokens();
        value = (me == 1 ? tokens[0] : tokens[1]) - (me == 1 ? tokens[1] : tokens[0]);
        return value;
    }
}

class BalanceWinning extends MiniMax {
    public BalanceWinning() {
    }

    @Override
    protected float Eval(GS s) {
        float value = 0;
        if (s.isFinished()) {
            int[] tokens = s.countTokens();
            if (tokens[me - 1] > tokens[me % 2])
                value = Float.MAX_VALUE;
            else if (tokens[me - 1] < tokens[me % 2])
                value = Float.MIN_VALUE;
            else
                value = 0;
        } else {
            int[] tokens = s.countTokens();
            value = (me == 1 ? tokens[0] : tokens[1]) - (me == 1 ? tokens[1] : tokens[0]);
        }
        return value;
    }
}

class MaximizeWeighted extends MiniMax {
    public MaximizeWeighted() {
    }

    @Override
    protected float Eval(GS s) {
        int[][] board = s.getBoard();
        float value = 0;
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

class BalanceWeighted extends MiniMax {
    public BalanceWeighted() {
    }

    @Override
    protected float Eval(GS s) {
        int[][] board = s.getBoard();
        float value = 0;
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

class BalanceWeightedWinning extends MiniMax {
    public BalanceWeightedWinning() {
    }

    @Override
    protected float Eval(GS s) {
        int[][] board = s.getBoard();
        float value = 0;
        if (s.isFinished()) {
            int[] tokens = s.countTokens();
            if (tokens[me - 1] > tokens[me % 2])
                value = Float.MAX_VALUE;
            else if (tokens[me - 1] < tokens[me % 2])
                value = Float.MIN_VALUE;
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

class GS {
    private final int EMPTY = 0;
    private final int BLACK = 1;
    private final int WHITE = 2;
    private int[][] board; // Possible values: 0 (empty), 1 (black), 2 (white)
    private int currentPlayer; // The player who is next to put a token on the board. Value is 1 or 2.
    private int size; // The number of columns = the number of rows on the board
    private int blackTokens; // Number of black tokens on board
    private int whiteTokens; // Number of white tokens on board

    // ************ Constructors ****************//
    public GS(GameState s) {
        int[][] board = s.getBoard();
        this.size = board.length;
        this.board = new int[size][size];
        this.blackTokens = 0;
        this.whiteTokens = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                this.board[i][j] = board[i][j];
                switch (board[i][j]) {
                    case BLACK:
                        this.blackTokens++;
                        break;
                    case WHITE:
                        this.whiteTokens++;
                }
            }
        }
        this.currentPlayer = s.getPlayerInTurn();
    }

    public GS(GS s) {
        int[][] board = s.getBoard();
        this.board = new int[s.getSize()][s.getSize()];
        for (int i = 0; i < this.board.length; i++)
            for (int j = 0; j < this.board[i].length; j++)
                this.board[i][j] = board[i][j];
        this.currentPlayer = s.getPlayerInTurn();
        this.size = s.getSize();
        this.blackTokens = s.blackTokens;
        this.whiteTokens = s.whiteTokens;
    }

    // ************ Getter methods *******************//
    /**
     * Returns the array representing the board of this game state
     */
    public int[][] getBoard() {
        return board;
    }

    /**
     * Returns the player whose turn it is, i.e. 1 (black) or 2 (white).
     */
    public int getPlayerInTurn() {
        return currentPlayer;
    }

    /**
     * Returns the size of the board
     */
    public int getSize() {
        return size;
    }

    // ************* Methods ****************//
    /**
     * Skips the turn of the current player (without) changing the board.
     */
    public void changePlayer() {
        currentPlayer = currentPlayer == 1 ? 2 : 1;
    }

    /**
     * Returns true if the game is finished (i.e. none of the players can make any
     * legal moves)
     * and false otherwise.
     */
    public boolean isFinished() {
        if (!legalMoves().isEmpty())
            return false;
        else { // current player has no legal moves
            changePlayer();
            if (legalMoves().isEmpty()) // next player also has no legal moves
                return true;
            else {
                changePlayer();
                return false;
            }
        }
    }

    /**
     * Counts tokens of the player 1 (black) and player 2 (white), respectively, and
     * returns an array
     * with the numbers in that order.
     */
    public int[] countTokens() {
        return new int[] { blackTokens, whiteTokens };
    }

    /**
     * If it is legal for the current player to put a token at the given place, then
     * the token is inserted, the required
     * tokens from the opponent is turned, and true is returned. If the move is not
     * legal, false is returned.
     * False is also returned if the given place does not represent a place on the
     * board.
     */
    public boolean insertToken(Position place) {
        if (place.col < 0 || place.row < 0 || place.col >= size || place.row >= size) // not a position on the board
            return false;
        if (board[place.col][place.row] != 0) // The position is not empty
            return false;

        boolean capturesFound = false;
        int captures = 0;
        // Capturing all possible opponents of the current player
        for (int deltaX = -1; deltaX <= 1; deltaX++) {
            for (int deltaY = -1; deltaY <= 1; deltaY++) {
                int captives = captureInDirection(place, deltaX, deltaY);
                captures += captives;
                if (captives > 0) {
                    capturesFound = true;
                    for (int i = 1; i <= captives; i++)
                        board[place.col + deltaX * i][place.row + deltaY * i] = currentPlayer;
                }
            }
        }

        if (capturesFound) {
            // Place the token at the given place
            board[place.col][place.row] = currentPlayer;
            switch (currentPlayer) {
                case BLACK:
                    blackTokens += captures + 1;
                    whiteTokens -= captures;
                    break;
                case WHITE:
                    blackTokens -= captures;
                    whiteTokens += captures + 1;
                    break;
            }
            this.changePlayer();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns a list of all the positions on the board that constitutes a legal
     * move for the current player.
     */
    public List<Position> legalMoves() {
        List<Position> posPlaces = new RandomizedArrayList<Position>();
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                if (board[i][j] == 0) {
                    posPlaces.add(new Position(i, j));
                }
            }
        }
        List<Position> legalPlaces = new RandomizedArrayList<Position>();
        for (Position p : posPlaces) {
            for (int deltaX = -1; deltaX <= 1; deltaX++) {
                for (int deltaY = -1; deltaY <= 1; deltaY++) {
                    if (captureInDirection(p, deltaX, deltaY) > 0) {
                        legalPlaces.add(p);
                    }
                }
            }
        }
        return legalPlaces;
    }

    /**
     * Checks how many tokens of the opponent the player can capture in the
     * direction given by deltaX and deltaY
     * if the player puts a token at the given position.
     * 
     * @param p      A position on the board
     * @param deltaX The step to be taken in the x-direction. Should be -1 (left), 0
     *               (none), or 1 (right).
     * @param deltaY The step to be taken in the delta direction. Should be -1 (up),
     *               0 (none), or 1 (down).
     */
    private int captureInDirection(Position p, int deltaX, int deltaY) {
        int opponent = (currentPlayer == 1 ? 2 : 1);

        int captured = 0;
        int cc = p.col;
        int rr = p.row;
        while (0 <= cc + deltaX && cc + deltaX < size && 0 <= rr + deltaY && rr + deltaY < size
                && board[cc + deltaX][rr + deltaY] == opponent) {
            cc = cc + deltaX;
            rr = rr + deltaY;
            captured++;
        }
        if (0 <= cc + deltaX && cc + deltaX < size && 0 <= rr + deltaY && rr + deltaY < size
                && board[cc + deltaX][rr + deltaY] == currentPlayer && captured > 0) {
            return captured;
        } else
            return 0;
    }

}
