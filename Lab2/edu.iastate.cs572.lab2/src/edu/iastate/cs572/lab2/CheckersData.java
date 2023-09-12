package edu.iastate.cs572.lab2;


import java.util.*;
import java.util.Arrays;


public class CheckersData {


    static final int
            EMPTY = 0,
            RED = 1,
            RED_KING = 2,
            BLACK = 3,
            BLACK_KING = 4;


    int[][] board;

    int redKings;
    int redMen;
    int blackKings;
    int blackMen;
    double redDist;
    double blackDist;
    boolean win;
    boolean lose;




    CheckersData(int[][] board, int redKings, int redMen, int blackKings, int blackMen, double redDist, double blackDist, boolean win, boolean lose) {
        this.board = board;
        this.redKings = redKings;
        this.redMen = redMen;
        this.blackKings = blackKings;
        this.blackMen = blackMen;
        this.redDist = redDist;
        this.blackDist = blackDist;
        this.win = win;
        this.lose = lose;
    }
    CheckersData() {
        board = new int[8][8];
        redKings = 0;
        redMen = 12;
        blackKings = 0;
        blackMen = 12;
        redDist = 72;
        blackDist = 72;
        win = false;
        lose = false;
        setUpGame();
    }

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < board.length; i++) {
            int[] row = board[i];
            sb.append(8 - i).append(" ");
            for (int n : row) {
                if (n == 0) {
                    sb.append(" ");
                } else if (n == 1) {
                    sb.append(ANSI_RED + "R" + ANSI_RESET);
                } else if (n == 2) {
                    sb.append(ANSI_RED + "K" + ANSI_RESET);
                } else if (n == 3) {
                    sb.append(ANSI_YELLOW + "B" + ANSI_RESET);
                } else if (n == 4) {
                    sb.append(ANSI_YELLOW + "K" + ANSI_RESET);
                }
                sb.append(" ");
            }
            sb.append(System.lineSeparator());
        }
        sb.append("  a b c d e f g h");

        return sb.toString();
    }

    void setUpGame() {
        for (int i=0; i<8; i++) {
            for (int j=0; j<8; j++) {
                board[i][j] = EMPTY;
            }
        }

        for (int i=0; i<3; i++) {
            for (int j=i%2; j<8; j+=2) {
                board[i][j] = BLACK;
            }
        }

        for (int i=5; i<8; i++) {
            for (int j=i%2; j<8; j+=2) {
                board[i][j] = RED;
            }
        }
        redKings = 0;
        redMen = 12;
        blackKings = 0;
        blackMen = 12;
        redDist = 72;
        blackDist = 72;
        win = false;
        lose = false;
    }

    int pieceAt(int row, int col) {
        return board[row][col];
    }

    boolean makeMove(CheckersMove move) {
        return makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol);
    }


    /**
     * @param fromRow row index of the from square
     * @param fromCol column index of the from square
     * @param toRow   row index of the to square
     * @param toCol   column index of the to square
     * @return        true if the piece becomes a king, otherwise false
     */
    boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {

        boolean isKing = (board[fromRow][fromCol] == BLACK_KING || board[fromRow][fromCol] == RED_KING);

        int type = this.board[fromRow][fromCol];
        CheckersMove move = new CheckersMove(fromRow, fromCol, toRow, toCol);
        this.board[fromRow][fromCol] = EMPTY;

        if (move.isJump()) {
            int jumpRow = fromRow + (toRow-fromRow)/2;
            int jumpCol = fromCol + (toCol-fromCol)/2;

            switch (this.board[jumpRow][jumpCol]) {
                case RED:
                    this.redMen--;
                    this.redDist -= jumpRow;
                    if (!isKing)
                        this.blackDist--;
                    break;
                case RED_KING:
                    this.redKings--;
                    if (!isKing)
                        this.blackDist--;
                    break;
                case BLACK:
                    this.blackMen--;
                    this.blackDist -= (7 - jumpRow);
                    this.redDist--;
                    break;
                case BLACK_KING:
                    this.blackKings--;
                    if (!isKing)
                        this.redDist--;
                    break;
            }
            this.board[jumpRow][jumpCol] = EMPTY;
        }
        if (blackMen + blackKings == 0)
            lose = true;

        if (redMen + redKings == 0)
            win = true;

        if (type == RED || type == RED_KING) {
            if (!isKing)
                this.redDist--;
            if (toRow == 0) {
                this.board[toRow][toCol] = RED_KING;
                if (!isKing) {
                    this.redKings++;
                    this.redMen--;
                    if (getLegalMoves(BLACK) == null) {
                        lose = true;
                    }
                    return true;
                }
            }
            else {
                this.board[toRow][toCol] = type;
            }
            if (getLegalMoves(BLACK) == null) {
                lose = true;
            }
        }
        else if (type == BLACK || type == BLACK_KING) {
            if (!isKing)
                this.blackDist--;
            if (toRow == 7) {
                this.board[toRow][toCol] = BLACK_KING;
                if (!isKing) {
                    this.blackKings++;
                    this.blackMen--;
                    if (getLegalMoves(RED) == null) {
                        win = true;
                    }
                    return true;
                }
            }
            else {
                this.board[toRow][toCol] = type;
            }
            if (getLegalMoves(RED) == null) {
                win = true;
            }
        }
        return false;
    }
    CheckersMove[] getLegalMoves(int player) {

        ArrayList<CheckersMove> moves = new ArrayList<>();
        boolean jump_found = false;

        if (player == BLACK) {

            for (int i=0; i<8; i++) {
                for (int j=0; j<8; j++) {
                    if (board[i][j] == BLACK || board[i][j] == BLACK_KING) {
                        if (jump_found) {
                            if (getLegalJumpsFrom(player, i, j) != null) {
                                moves.addAll(Arrays.asList(getLegalJumpsFrom(player, i, j)));
                            }
                        }
                        else {
                            if (i<6 && j>1 && (board[i + 1][j - 1] == RED || board[i + 1][j - 1] == RED_KING) && board[i + 2][j - 2] == EMPTY) {
                                jump_found = true;
                                if (getLegalJumpsFrom(player, i, j) != null) {
                                    moves = new ArrayList<>(Arrays.asList(getLegalJumpsFrom(player, i, j)));
                                }
                            }
                            if (i<6 && j<6 && (board[i + 1][j + 1] == RED || board[i + 1][j + 1] == RED_KING) && board[i + 2][j + 2] == EMPTY) {
                                if (!jump_found) {
                                    jump_found = true;
                                    moves = new ArrayList<>();
                                }
                                if (getLegalJumpsFrom(player, i, j) != null) {
                                    moves.addAll(Arrays.asList(getLegalJumpsFrom(player, i, j)));
                                }
                            }
                            if (i<7 && j>0 && board[i + 1][j - 1] == EMPTY && !jump_found) {
                                CheckersMove move = new CheckersMove(i, j, i + 1, j - 1);
                                moves.add(move);
                            }
                            if (i<7 && j<7 && board[i + 1][j + 1] == EMPTY && !jump_found) {
                                CheckersMove move = new CheckersMove(i, j, i + 1, j + 1);
                                moves.add(move);
                            }
                        }
                    }
                    if (board[i][j] == BLACK_KING) {
                        if (i>1 && j>1 && (board[i-1][j-1] == RED || board[i-1][j-1] == RED_KING) && board[i-2][j-2] == EMPTY) {
                            if (!jump_found) {
                                jump_found = true;
                                moves = new ArrayList<>();
                            }
                            if (getLegalJumpsFrom(player, i, j) != null) {
                                moves.addAll(Arrays.asList(getLegalJumpsFrom(player, i, j)));
                            }
                        }
                        if (i>1 && j<6 && (board[i-1][j+1] == RED || board[i-1][j+1] == RED_KING) && board[i-2][j+2] == EMPTY) {
                            if (!jump_found) {
                                jump_found = true;
                                moves = new ArrayList<>();
                            }
                            if (getLegalJumpsFrom(player, i, j) != null) {
                                moves.addAll(Arrays.asList(getLegalJumpsFrom(player, i, j)));
                            }
                        }
                        if (i>0 && j>0 && board[i-1][j-1] == EMPTY && !jump_found) {
                            CheckersMove move = new CheckersMove(i,j,i-1,j-1);
                            moves.add(move);
                        }
                        if (i>0 && j<7 && board[i-1][j+1] == EMPTY && !jump_found) {
                            CheckersMove move = new CheckersMove(i,j,i-1,j+1);
                            moves.add(move);
                        }
                    }
                }
            }

        }
        else if (player == RED) {
            for (int i=0; i<8; i++) {
                for (int j=0; j<8; j++) {
                    if (board[i][j] == RED || board[i][j] == RED_KING) {
                        if (jump_found) {
                            if (getLegalJumpsFrom(player, i, j) != null) {
                                moves.addAll(Arrays.asList(getLegalJumpsFrom(player, i, j)));
                            }
                        }
                        else {
                            if (i>1 && j>1 && (board[i - 1][j - 1] == BLACK || board[i - 1][j - 1] == BLACK_KING) && board[i - 2][j - 2] == EMPTY) {
                                jump_found = true;
                                moves = new ArrayList<>();
                                if (getLegalJumpsFrom(player, i, j) != null) {
                                    moves = new ArrayList<>(Arrays.asList(getLegalJumpsFrom(player, i, j)));
                                }
                            }
                            if (i>1 && j<6 && (board[i - 1][j + 1] == BLACK || board[i - 1][j + 1] == BLACK_KING) && board[i - 2][j + 2] == EMPTY) {
                                if (!jump_found) {
                                    jump_found = true;
                                    moves = new ArrayList<>();
                                }
                                if (getLegalJumpsFrom(player, i, j) != null) {
                                    moves.addAll(Arrays.asList(getLegalJumpsFrom(player, i, j)));
                                }
                            }
                            if (i>0 && j>0 && board[i - 1][j - 1] == EMPTY && !jump_found) {
                                CheckersMove move = new CheckersMove(i, j, i - 1, j - 1);
                                moves.add(move);
                            }
                            if (i>0 && j<7 && board[i - 1][j + 1] == EMPTY && !jump_found) {
                                CheckersMove move = new CheckersMove(i, j, i - 1, j + 1);
                                moves.add(move);
                            }
                        }
                    }
                    if (board[i][j] == RED_KING) {
                        if (i<6 && j>1 && (board[i+1][j-1] == BLACK || board[i+1][j-1] == BLACK_KING) && board[i+2][j-2] == EMPTY) {
                            if (!jump_found) {
                                jump_found = true;
                                moves = new ArrayList<>();
                            }
                            if (getLegalJumpsFrom(player, i, j) != null) {
                                moves.addAll(Arrays.asList(getLegalJumpsFrom(player, i, j)));
                            }
                        }
                        if (i<6 && j<6 && (board[i+1][j+1] == BLACK || board[i+1][j+1] == BLACK_KING) && board[i+2][j+2] == EMPTY) {
                            if (!jump_found) {
                                jump_found = true;
                                moves = new ArrayList<>();
                            }
                            if (getLegalJumpsFrom(player, i, j) != null) {
                                moves.addAll(Arrays.asList(getLegalJumpsFrom(player, i, j)));
                            }
                        }
                        if (i<7 && j>0 && board[i+1][j-1] == EMPTY && !jump_found) {
                            CheckersMove move = new CheckersMove(i,j,i+1,j-1);
                            moves.add(move);
                        }
                        if (i<7 && j<7 && board[i+1][j+1] == EMPTY && !jump_found) {
                            CheckersMove move = new CheckersMove(i,j,i+1,j+1);
                            moves.add(move);
                        }
                    }
                }
            }
        }

        if (moves.isEmpty()) {
            return null;
        }
        else {
            CheckersMove[] moves_arr = new CheckersMove[moves.size()];
            return moves.toArray(moves_arr);
        }


    }

    CheckersMove[] getLegalJumpsFrom(int player, int row, int col) {
        ArrayList<CheckersMove> moves = new ArrayList<>();
        boolean isKing = (board[row][col] == BLACK_KING || board[row][col] == RED_KING);
        if (player == BLACK) {
            if (row<6 && col>1 && (board[row + 1][col - 1] == RED || board[row + 1][col - 1] == RED_KING) && board[row + 2][col - 2] == EMPTY) {
                CheckersMove move = new CheckersMove(row, col, row + 2, col - 2);
                moves.add(move);
            }
            if (row<6 && col<6 && (board[row + 1][col + 1] == RED || board[row + 1][col + 1] == RED_KING) && board[row + 2][col + 2] == EMPTY) {
                CheckersMove move = new CheckersMove(row, col, row + 2, col + 2);
                moves.add(move);
            }
            if (isKing) {
                if (row>1 && col>1 && (board[row - 1][col - 1] == RED || board[row - 1][col - 1] == RED_KING) && board[row - 2][col - 2] == EMPTY) {
                    CheckersMove move = new CheckersMove(row, col, row - 2, col - 2);
                    moves.add(move);
                }
                if (row>1 && col<6 && (board[row - 1][col + 1] == RED || board[row - 1][col + 1] == RED_KING) && board[row - 2][col + 2] == EMPTY) {
                    CheckersMove move = new CheckersMove(row, col, row - 2, col + 2);
                    moves.add(move);
                }
            }
        }
        else if (player == RED) {
            if (row>1 && col>1 && (board[row - 1][col - 1] == BLACK || board[row - 1][col - 1] == BLACK_KING) && board[row - 2][col - 2] == EMPTY) {
                CheckersMove move = new CheckersMove(row, col, row - 2, col - 2);
                moves.add(move);
            }
            if (row>1 && col<6 && (board[row - 1][col + 1] == BLACK || board[row - 1][col + 1] == BLACK_KING) && board[row - 2][col + 2] == EMPTY) {
                CheckersMove move = new CheckersMove(row, col, row - 2, col + 2);
                moves.add(move);
            }
            if (isKing) {
                if (row<6 && col>1 && (board[row + 1][col - 1] == BLACK || board[row + 1][col - 1] == BLACK_KING) && board[row + 2][col - 2] == EMPTY) {
                    CheckersMove move = new CheckersMove(row, col, row + 2, col - 2);
                    moves.add(move);
                }
                if (row<6 && col<6 && (board[row + 1][col + 1] == BLACK || board[row + 1][col + 1] == BLACK_KING) && board[row + 2][col + 2] == EMPTY) {
                    CheckersMove move = new CheckersMove(row, col, row + 2, col + 2);
                    moves.add(move);
                }
            }
        }
        if (moves.isEmpty()) {
            return null;
        }
        else {
            CheckersMove[] moves_arr = new CheckersMove[moves.size()];
            return moves.toArray(moves_arr);
        }

    }
}