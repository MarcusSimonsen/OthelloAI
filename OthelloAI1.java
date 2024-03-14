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
        return s.isFinished();
    }

    protected Tuple MaxValue(GS s, int ply, float alpha, float beta) {
        if (explored.containsKey(s))
            return explored.get(s);
        if (isCutOff(s, ply))
            return new Tuple(Eval(s), null);
        if (!s.hasLegalMove()) {
            s.changePlayer();
            return MinValue(s, ply+1, alpha, beta);
        }
        Tuple v = new Tuple(Float.NEGATIVE_INFINITY, null);

        List<Position> l = s.legalMoves();
        if (ply == 0)
            Collections.shuffle(l);
        PriorityQueue<Position> moves = new PriorityQueue<>(new MaxMoveComparator());
        moves.addAll(l);
        for (Position a : moves) {
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

        explored.put(s, v);
        return v;
    }

    protected Tuple MinValue(GS s, int ply, float alpha, float beta) {
        if (explored.containsKey(s))
            return explored.get(s);
        if (isCutOff(s, ply))
            return new Tuple(Eval(s), null);
        if (!s.hasLegalMove()) {
            s.changePlayer();
            return MaxValue(s, ply+1, alpha, beta);
        }
        Tuple v = new Tuple(Float.POSITIVE_INFINITY, null);

        List<Position> l = s.legalMoves();
        if (ply == 0)
            Collections.shuffle(l);
        PriorityQueue<Position> moves = new PriorityQueue<Position>(new MinMoveComparator());
        moves.addAll(l);
        for (Position a : moves) {
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

        explored.put(s, v);
        return v;
    }

    @Override
    public Position decideMove(GameState s) {
        explored = new TreeMap<>(new GSComparator());
        Tuple p = MaxValue(new GS(s), 0, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
        me = s.getPlayerInTurn();
        return p.move();
    }
}

class MaxMoveComparator implements Comparator<Position> {
    private static final int[][] heuristic = {
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
    private static final int[][] heuristic = {
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

class GSComparator implements Comparator<GS> {
    @Override
    public int compare(GS s1, GS s2) {
        int size1 = s1.getSize();
        int size2 = s2.getSize();
        if (size1 != size2) {
            return size1 - size2;
        } else {
            int half = size1 / 2;
            // Upper left quadrant
            for (int i = half-1; i >= 0; i--) {
                for (int j = half-1; j >= 0; j--) {
                    if (s1.getPlace(i, j) != s2.getPlace(i, j)) {
                        return s1.getPlace(i, j) < s2.getPlace(i, j) ? -1 : 1;
                    }
                }
            }
            // Upper right quadrant
            for (int i = half-1; i >= 0; i--) {
                for (int j = half; j < size1; j++) {
                    if (s1.getPlace(i, j) != s2.getPlace(i, j)) {
                        return s1.getPlace(i, j) < s2.getPlace(i, j) ? -1 : 1;
                    }
                }
            }
            // Lower left quadrant
            for (int i = half; i < size1; i++) {
                for (int j = half-1; j >= 0; j--) {
                    if (s1.getPlace(i, j) != s2.getPlace(i, j)) {
                        return s1.getPlace(i, j) < s2.getPlace(i, j) ? -1 : 1;
                    }
                }
            }
            // Lower right quadrant
            for (int i = half; i < size1; i++) {
                for (int j = half; j < size1; j++) {
                    if (s1.getPlace(i, j) != s2.getPlace(i, j)) {
                        return s1.getPlace(i, j) < s2.getPlace(i, j) ? -1 : 1;
                    }
                }
            }
        }

        return 0;
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
        float value = 0;
        int size = s.getSize();
        if (size != 8) {
            int[] tokens = s.countTokens();
            value = (me == 1 ? tokens[0] : tokens[1]) - (me == 1 ? tokens[1] : tokens[0]);
        } else {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (s.getPlace(i, j) == 0)
                        continue;
                    if (me == 1)
                        value += s.getPlace(i, j) == 1 ? heuristic[i][j] : 0;
                    else
                        value += s.getPlace(i, j) == 2 ? heuristic[i][j] : 0;
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
        float value = 0;
        int size = s.getSize();
        if (size != 8) {
            int[] tokens = s.countTokens();
            value = (me == 1 ? tokens[0] : tokens[1]) - (me == 1 ? tokens[1] : tokens[0]);
        } else {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (s.getPlace(i, j) == 0)
                        continue;
                    if (me == 1)
                        value += s.getPlace(i, j) == 1 ? heuristic[i][j] : -heuristic[i][j];
                    else
                        value += s.getPlace(i, j) == 2 ? heuristic[i][j] : -heuristic[i][j];
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
        float value = 0;
        int size = s.getSize();
        if (s.isFinished()) {
            int[] tokens = s.countTokens();
            if (tokens[me - 1] > tokens[me % 2])
                value = Float.MAX_VALUE;
            else if (tokens[me - 1] < tokens[me % 2])
                value = Float.MIN_VALUE;
            else
                value = 0;
        } else if (size != 8) {
            int[] tokens = s.countTokens();
            value = (me == 1 ? tokens[0] : tokens[1]) - (me == 1 ? tokens[1] : tokens[0]);
        } else {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (s.getPlace(i, j) == 0)
                        continue;
                    if (me == 1)
                        value += s.getPlace(i, j) == 1 ? heuristic[i][j] : -heuristic[i][j];
                    else
                        value += s.getPlace(i, j) == 2 ? heuristic[i][j] : -heuristic[i][j];
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
                setPlace(i, j, board[i][j]);
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
        this.size = s.getSize();
        this.board = new int[this.size][this.size];
        for (int i = 0; i < s.getSize(); i++)
            System.arraycopy(board[i], 0, this.board[i], 0, this.board[i].length);
        this.currentPlayer = s.getPlayerInTurn();
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
     * Returns 0 if place is blank, 1 if blacn and 2 if white
     */
    public int getPlace(int i, int j) {
        return board[i][j];
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
     * Sets the tile at the specified location to the given value:
     * 0: blank
     * 1: black
     * 2: white
     */
    public void setPlace(int i, int j, int value) {
        board[i][j] = value;
    }

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
        if (hasLegalMove())
            return false;
        else { // current player has no legal moves
            changePlayer();
            if (!hasLegalMove()) // next player also has no legal moves
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
        if (getPlace(place.col, place.row) != 0) // The position is not empty
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
                        setPlace(place.col + deltaX * i, place.row + deltaY * i, currentPlayer);
                }
            }
        }

        if (capturesFound) {
            // Place the token at the given place
            setPlace(place.col, place.row, currentPlayer);
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
        List<Position> posPlaces = new ArrayList<Position>();
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                if (getPlace(i, j) == 0) {
                    posPlaces.add(new Position(i, j));
                }
            }
        }
        List<Position> legalPlaces = new ArrayList<Position>();
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

    public boolean hasLegalMove() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (getPlace(i, j) != 0)
                    continue;
                Position p = new Position(i, j);
                for (int deltaX = -1; deltaX <= 1; deltaX++) {
                    for (int deltaY = -1; deltaY <= 1; deltaY++) {
                        if (captureInDirection(p, deltaX, deltaY) > 0) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
        if (deltaX == 0) {
            while (0 <= rr + deltaY && rr + deltaY < size && getPlace(cc, rr + deltaY) == opponent) {
                rr += deltaY;
                captured++;
            }
        }
        else if (deltaY == 0) {
            while (0 <= cc + deltaX && cc + deltaX < size && getPlace(cc + deltaX, rr) == opponent) {
                cc += deltaX;
                captured++;
            }
        }
        else {
            while (0 <= rr + deltaY && rr + deltaY < size
                    && 0 <= cc + deltaX && cc + deltaX < size
                    && getPlace(cc + deltaX, rr + deltaY) == opponent) {
                cc += deltaX;
                rr += deltaY;
                captured++;
            }
        }
        if (0 <= cc + deltaX && cc + deltaX < size && 0 <= rr + deltaY && rr + deltaY < size
                && getPlace(cc + deltaX, rr + deltaY) == currentPlayer && captured > 0) {
            return captured;
        } else
            return 0;
    }

}
