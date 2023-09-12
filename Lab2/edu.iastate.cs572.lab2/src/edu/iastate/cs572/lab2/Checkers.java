package edu.iastate.cs572.lab2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;



public class Checkers extends JPanel {
    long currTime;
    int totalMoves = 0;
    long totalTime;
//    long avgTime;
    public static void main(String[] args) {
        JFrame window = new JFrame("Checkers with AI agent");
        Checkers content = new Checkers();
        window.setContentPane(content);
        window.pack();
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        window.setLocation( (screensize.width - window.getWidth())/2,
                (screensize.height - window.getHeight())/2 );
        window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        window.setResizable(false);
        window.setVisible(true);
    }

    private JButton newGameButton;
    private JButton stopButton;
    private JLabel message;
    public Checkers() {

        setLayout(null);
        setPreferredSize( new Dimension(600,300) );
        setBackground(new Color(182, 161, 94, 224));  // color of background.
        Board board = new Board();
        add(board);
        add(newGameButton);
        add(stopButton);
        add(message);
        board.setBounds(20,20,164,164);
        newGameButton.setBounds(310, 50, 200, 50);
        stopButton.setBounds(310, 120, 200, 50);
        message.setBounds(0, 200, 600, 30);

    }

    private class Board extends JPanel implements ActionListener, MouseListener {

        CheckersData board;
        boolean gameInProgress;
        int currPlayer;
        int selectedRow, selectedCol;
        CheckersMove[] legalMoves;
        CheckersAlphaBetaSearch player = new CheckersAlphaBetaSearch(); // AI agent

        Board() {
            setBackground(Color.BLACK);
            addMouseListener(this);
            stopButton = new JButton("End Game");
            stopButton.addActionListener(this);
            newGameButton = new JButton("New Game");
            newGameButton.addActionListener(this);
            message = new JLabel("",JLabel.CENTER);
            message.setFont(new  Font("Arial", Font.BOLD, 20));
            message.setForeground(Color.black);
            board = new CheckersData();
            doNewGame();
        }

        public void actionPerformed(ActionEvent evt) {
            Object src = evt.getSource();
            if (src == newGameButton)
                doNewGame();
            else if (src == stopButton)
                doStop();
        }

        void doNewGame() {
            if (gameInProgress) {
                message.setText("Finish the curr game first!");
                return;
            }
            board.setUpGame();
            currPlayer = CheckersData.RED;
            player.setCheckersData(board);
            legalMoves = board.getLegalMoves(CheckersData.RED);
            selectedRow = -1;
            message.setText("Red:  Make your move.");
            gameInProgress = true;
            newGameButton.setEnabled(false);
            stopButton.setEnabled(true);
            repaint();
            revalidate();
        }

        void doStop() {
            if (!gameInProgress) {
                message.setText("There is no game in progress!");
                return;
            }
            if (currPlayer == CheckersData.RED)
                gameOver("RED resigns.  BLACK wins.");
            else
                gameOver("BLACK resigns.  RED wins.");
        }

        void gameOver(String str) {
            message.setText(str);
            newGameButton.setEnabled(true);
            stopButton.setEnabled(false);
            gameInProgress = false;
            if(totalMoves > 0)
                System.out.println("Average time taken by AI agent: "+ (totalTime/totalMoves)/1000000 + "ms");
            else
                System.out.println("Average time taken by AI agent: 0 ns");
            System.out.println("Game Over");
        }

        void doClickSquare(int row, int col) {

            for (CheckersMove legalMove : legalMoves) {
                if (legalMove.fromRow == row && legalMove.fromCol == col) {
                    selectedRow = row;
                    selectedCol = col;
                    if (currPlayer == CheckersData.RED)
                        message.setText("RED:  Make your move.");
                    else
                        message.setText("BLACK:  Make your move.");
                    repaint();
                    revalidate();
                    return;
                }
            }
            if (selectedRow < 0) {
                message.setText("Click the piece you want to move.");
                return;
            }
            for (CheckersMove legalMove : legalMoves) {
                if (legalMove.fromRow == selectedRow && legalMove.fromCol == selectedCol
                        && legalMove.toRow == row && legalMove.toCol == col) {
                    doMakeMove(legalMove);
                    return;
                }
            }
            message.setText("Select the square you want to move to.");
        }
        void doMakeMove(CheckersMove move) {
            boolean isKingJump = board.makeMove(move);
            repaint();
            revalidate();
            if (!isKingJump && move.isJump()) {
                legalMoves = board.getLegalJumpsFrom(currPlayer,move.toRow,move.toCol);
                if (legalMoves != null) {
                    if (currPlayer == CheckersData.RED)
                        message.setText("RED:  You must continue jumping.");
                    else
                        message.setText("BLACK:  You must continue jumping.");
                    selectedRow = move.toRow;  // Since only one piece can be moved, select it.
                    selectedCol = move.toCol;
                    repaint();
                    revalidate();
                    return;
                }
            }
            if (currPlayer == CheckersData.RED) {
                currPlayer = CheckersData.BLACK;
                legalMoves = board.getLegalMoves(currPlayer);
                if (legalMoves == null) {
                    gameOver("BLACK has no moves.  RED wins.");
                    return;
                } else {
                    message.setText("BLACK:  Now AI's turn.");
                }
                //Start time here.
                long startTime = System.nanoTime();
                CheckersMove moveAI = player.makeMove(legalMoves);
                boolean isKingJumpAI = board.makeMove(moveAI);
                while (!isKingJumpAI && moveAI.isJump()) {
                    legalMoves = board.getLegalJumpsFrom(currPlayer,moveAI.toRow, moveAI.toCol);
                    if (legalMoves != null) {
                        message.setText("BLACK:  AI has another jump.");
                        selectedRow = move.toRow;
                        selectedCol = move.toCol;
                        repaint();
                        revalidate();
                        moveAI = player.makeMove(legalMoves);
                        board.makeMove(moveAI);
                    } else {
                        break;
                    }
                }
                //Stop time here
                long endTime   = System.nanoTime();
                long currTime = endTime - startTime;
                totalTime = totalTime + currTime;
                totalMoves++;
                System.out.println("Time taken by AI agent: "+ currTime/1000000 + "ms");
                repaint();
                revalidate();
            }
            currPlayer = CheckersData.RED;
            System.out.println("RED PLAYER IS UP");
            System.out.println();
            System.out.println();
            System.out.println();
            legalMoves = board.getLegalMoves(currPlayer);
            if (legalMoves == null)
                gameOver("RED has no moves.  BLACK wins.");
            else if (legalMoves[0].isJump())
                message.setText("RED:  Make your move.  You must jump.");
            else
                message.setText("RED:  Make your move.");

            selectedRow = -1;
            if (legalMoves != null) {
                boolean sameStartSquare = true;
                for (int i = 1; i < legalMoves.length; i++)
                    if (legalMoves[i].fromRow != legalMoves[0].fromRow
                            || legalMoves[i].fromCol != legalMoves[0].fromCol) {
                        sameStartSquare = false;
                        break;
                    }
                if (sameStartSquare) {
                    selectedRow = legalMoves[0].fromRow;
                    selectedCol = legalMoves[0].fromCol;
                }
            }
            repaint();
            revalidate();
        }

        @Override
        public void paintComponent(Graphics g) {
            g.setColor(Color.black);
            g.drawRect(0,0,getSize().width-1,getSize().height-1);
            g.drawRect(1,1,getSize().width-3,getSize().height-3);
            
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if ( row % 2 == col % 2 )
                        g.setColor(Color.WHITE);
                    else
                        g.setColor(Color.DARK_GRAY);
                    g.fillRect(2 + col*20, 2 + row*20, 20, 20);
                    switch (board.pieceAt(row,col)) {
                        case CheckersData.RED:
                            g.setColor(Color.RED);
                            g.fillOval(4 + col*20, 4 + row*20, 15, 15);
                            break;
                        case CheckersData.BLACK:
                            g.setColor(Color.BLACK);
                            g.fillOval(4 + col*20, 4 + row*20, 15, 15);
                            break;
                        case CheckersData.RED_KING:
                            g.setColor(Color.RED);
                            g.fillOval(4 + col*20, 4 + row*20, 15, 15);
                            g.setColor(Color.WHITE);
                            g.drawString("K", 7 + col*20, 16 + row*20);
                            break;
                        case CheckersData.BLACK_KING:
                            g.setColor(Color.BLACK);
                            g.fillOval(4 + col*20, 4 + row*20, 15, 15);
                            g.setColor(Color.WHITE);
                            g.drawString("K", 7 + col*20, 16 + row*20);
                            break;
                    }
                }
            }


            if (gameInProgress) {
                g.setColor(Color.cyan);
                for (CheckersMove legalMove : legalMoves) {
                    g.drawRect(2 + legalMove.fromCol * 20, 2 + legalMove.fromRow * 20, 19, 19);
                    g.drawRect(3 + legalMove.fromCol * 20, 3 + legalMove.fromRow * 20, 17, 17);
                }

                if (selectedRow >= 0) {
                    g.setColor(Color.white);
                    g.drawRect(2 + selectedCol*20, 2 + selectedRow*20, 19, 19);
                    g.drawRect(3 + selectedCol*20, 3 + selectedRow*20, 17, 17);
                    g.setColor(Color.green);
                    for (CheckersMove legalMove : legalMoves) {
                        if (legalMove.fromCol == selectedCol && legalMove.fromRow == selectedRow) {
                            g.drawRect(2 + legalMove.toCol * 20, 2 + legalMove.toRow * 20, 19, 19);
                            g.drawRect(3 + legalMove.toCol * 20, 3 + legalMove.toRow * 20, 17, 17);
                        }
                    }
                }
            }

        }

        @Override
        public void mousePressed(MouseEvent evt) {
            if (!gameInProgress)
                message.setText("Click \"New Game\" to start a new game.");
            else {
                int col = (evt.getX() - 2) / 20;
                int row = (evt.getY() - 2) / 20;
                if (col >= 0 && col < 8 && row >= 0 && row < 8)
                    doClickSquare(row,col);
            }
        }

        @Override
        public void mouseReleased(MouseEvent evt) { }
        @Override
        public void mouseClicked(MouseEvent evt) { }
        @Override
        public void mouseEntered(MouseEvent evt) { }
        @Override
        public void mouseExited(MouseEvent evt) { }

    }
}
