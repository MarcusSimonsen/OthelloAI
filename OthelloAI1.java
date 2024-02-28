public class OthelloAI1 implements IOthelloAI {
	private static int depthLimit = 5;

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
	public double Eval(GameState s) {
		int[] tokens = s.countTokens();
		return tokens[0] - tokens[1];
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

		for (Position a : s.legalMoves()) {
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

		for (Position a : s.legalMoves()) {
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
		return p.move;
	}
}
