package edu.iastate.cs572.lab2;


public class CheckersAlphaBetaSearch {
    private CheckersData board;

    public void setCheckersData(CheckersData board) {
        this.board = board;
    }

    private static final int MAX_DEPTH = 10;

    private double maxValue(CheckersData state, double alpha, double beta, int depth) {

        if (isTerminal(state)) {
            return eval(state, depth);
        }
        double maxEval = Double.NEGATIVE_INFINITY;


        for (CheckersMove move : state.getLegalMoves(CheckersData.BLACK)) {

            CheckersData temporary = copy(state);
            temporary.makeMove(move);

            if (depth < MAX_DEPTH) {

                double value;

                if (move.isJump() && temporary.getLegalJumpsFrom(CheckersData.BLACK, move.toRow, move.toCol) != null) {
                    value = maxValue(temporary, alpha, beta, depth + 1);
                }
                else
                {
                    value = minValue(temporary, alpha, beta, depth + 1);
                }
                maxEval = Math.max(value, maxEval);
            }
            else
            {
                maxEval = Math.max(eval(temporary, depth), maxEval);
            }
            alpha = Math.max(alpha, maxEval);

            if (beta <= alpha) {
                break;
            }
        }

        return maxEval;
    }

    private double minValue(CheckersData state, double alpha, double beta, int depth) {
        if (isTerminal(state)) {
            return eval(state, depth);
        }
        double maxEval = Double.POSITIVE_INFINITY;

        for (CheckersMove move : state.getLegalMoves(CheckersData.RED)) {

            CheckersData temporary = copy(state);

            temporary.makeMove(move);
            if (depth < MAX_DEPTH) {

                double value;
                if (move.isJump() && temporary.getLegalJumpsFrom(CheckersData.RED, move.toRow, move.toCol) != null) {
                    value = minValue(temporary, alpha, beta, depth+1);
                } else {
                    value = maxValue(temporary, alpha, beta, depth + 1);
                }
                maxEval = Math.min(value, maxEval);
            } else {
                maxEval = Math.min(eval(temporary, depth), maxEval);
            }
            beta = Math.min(beta, maxEval);

            if (beta >= alpha) {
                break;
            }
        }
        return maxEval;
    }

    private double eval(CheckersData state, int depth) {

        double scoreDiff = 10*state.blackKings + 5*state.blackMen - 10*state.redKings - 5*state.redMen;
        double totalScore = 10*state.blackKings + 5*state.blackMen + 10*state.redKings + 5*state.redMen;
        double scoreProportion = scoreDiff/totalScore;
        double scoreRatio = ((double)(10*state.blackKings + 5*state.blackMen)) / ((double)(10*state.redKings + 5*state.redMen + 1));
        scoreRatio = scoreRatio / totalScore;

        double kingAvgDistDiff = (state.redDist / (state.redMen + 1)) - (state.blackDist / (state.blackMen + 1));
        double kingDistRatio = kingAvgDistDiff / 7.0;


        return 1000000*scoreProportion + 100000*scoreRatio + 10000*kingDistRatio;

    }



    public CheckersMove makeMove(CheckersMove[] legalMoves) {

        System.out.println(board);
        System.out.println();

        CheckersMove result = null;
        double resultValue = Double.NEGATIVE_INFINITY;

        for (CheckersMove move : legalMoves) {
            CheckersData temporary = copy(this.board);
            temporary.makeMove(move);
            double value = minValue(temporary, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0);
            if (value > resultValue) {
                result = move;
                resultValue = value;
            }
        }

        CheckersData temporary = copy(this.board);
        temporary.makeMove(result);

        return result;
    }

    private static boolean isTerminal(CheckersData state) {
        return state.win || state.lose;
    }

    private CheckersData copy(CheckersData state) {

        int[][] tempBoard = new int[8][8];

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                tempBoard[i][j] = state.board[i][j];
            }
        }

        return new CheckersData(tempBoard, state.redKings, state.redMen,
                state.blackKings, state.blackMen,
                state.redDist, state.blackDist,
                state.win, state.lose);
    }
}
