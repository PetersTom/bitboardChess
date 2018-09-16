package GUI;

import state.Move;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.List;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;


public class Gui extends JPanel {
    private Color darkBrown = new Color(139,69,19);
    private Color lightBrown = new Color(245,222,179);
    public static int cellWidth = 100;

    private String boardState; // The boardstate in Forsyth-Edwards Notation. It is changed slightly. An empty square
    // is represented by E instead of a number. However, it also accepts the original interpretation with numbers.
    // It is still from white perspective last row to first.

    private boolean boardRepaintNeeded = true; //sets whether or not the board should be repainted to avoid flickering
    private BufferedImage backupCanvas;

    private int selectedPieceSquare = 0; // The square that is selected. 0 if no piece is selected
    private List<Move> selectedPieceMoves; // null if no piece is selected.

    private Image whitePawn;
    private Image blackPawn;
    private Image whiteRook;
    private Image blackRook;
    private Image whiteKnight;
    private Image blackKnight;
    private Image whiteBishop;
    private Image blackBishop;
    private Image whiteQueen;
    private Image blackQueen;
    private Image whiteKing;
    private Image blackKing;

    public Gui() {
        this.setPreferredSize(new Dimension(8 * cellWidth, 8 * cellWidth));

        try {
            URL u = getClass().getClassLoader().getResource("images/wPawn.png");
            assert u != null;   //Assume the resource is there, if it is not, exceptions will occur, but that is alright,
            //because when that happens, the application cannot start anyways.
            whitePawn = ImageIO.read(u);
            u = getClass().getClassLoader().getResource("images/bPawn.png");
            assert u != null;
            blackPawn = ImageIO.read(u);
            u = getClass().getClassLoader().getResource("images/wRook.png");
            assert u != null;
            whiteRook = ImageIO.read(u);
            u = getClass().getClassLoader().getResource("images/bRook.png");
            assert u != null;
            blackRook = ImageIO.read(u);
            u = getClass().getClassLoader().getResource("images/wKnight.png");
            assert u != null;
            whiteKnight = ImageIO.read(u);
            u = getClass().getClassLoader().getResource("images/bKnight.png");
            assert u != null;
            blackKnight = ImageIO.read(u);
            u = getClass().getClassLoader().getResource("images/wBishop.png");
            assert u != null;
            whiteBishop = ImageIO.read(u);
            u = getClass().getClassLoader().getResource("images/bBishop.png");
            assert u != null;
            blackBishop = ImageIO.read(u);
            u = getClass().getClassLoader().getResource("images/wQueen.png");
            assert u != null;
            whiteQueen = ImageIO.read(u);
            u = getClass().getClassLoader().getResource("images/bQueen.png");
            assert u != null;
            blackQueen = ImageIO.read(u);
            u = getClass().getClassLoader().getResource("images/wKing.png");
            assert u != null;
            whiteKing = ImageIO.read(u);
            u = getClass().getClassLoader().getResource("images/bKing.png");
            assert u != null;
            blackKing = ImageIO.read(u);
        } catch (IOException | IllegalArgumentException ex) {
            System.err.println("Can't read images");
            ex.printStackTrace();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        //Implement double buffering
        BufferedImage offscreen;
        offscreen = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics offg = offscreen.getGraphics();
        offg.setColor(Color.BLACK);
        offg.fillRect(0,0, getWidth(), getHeight());

        if (boardRepaintNeeded) {

            redrawBoard(offg);  //draw the board on the original offscreen canvas
            backupCanvas = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics backupGraphics = backupCanvas.getGraphics();
            backupGraphics.drawImage(offscreen, 0, 0, this);    //and draw the board on the backup
            boardRepaintNeeded = false;

        } else {
            offg.drawImage(backupCanvas, 0, 0, this);
        }

        //drawPointer(offg);
        drawSelectedPiece(offg);
        //copy to real screen
        g.drawImage(offscreen, 0, 0, this);



    }

    private void redrawBoard(Graphics g) {
        //redraw
        drawField(g);
        //draw king red if checked
        //drawKingCheck(g);
        //draw last move
        //paintLastMove(g);
        //draw the pieces
        drawPieces(g);
    }

    private void drawSelectedPiece(Graphics g) {
        if (selectedPieceSquare == 0) return; // To prevent a weird green line at the bottom left
        Point graphicalSelectedPiece = getGraphicalPosition(selectedPieceSquare);
        g.setColor(Color.GREEN);
        g.drawRect(graphicalSelectedPiece.x, graphicalSelectedPiece.y, cellWidth, cellWidth);
        for (Move m : this.selectedPieceMoves) { //draw the possible moves
            g.setColor(new Color(0, 185, 255));
            Point graphicalPoint = getGraphicalPosition(m.getTo());
            g.fillOval(graphicalPoint.x + (cellWidth / 3), graphicalPoint.y + (cellWidth / 3), cellWidth / 3, cellWidth / 3);
        }
    }

    /**
     * Paints the field
     * @param g
     */
    private void drawField(Graphics g) {
        for (int x = 0; x < 8; x += 1) {
            for (int y = 0; y < 8; y += 1) {
                if ((x+y) % 2 != 0) {       //because they start at 0. It would have been equal if it starts at 1.
                    g.setColor(darkBrown);
                } else {
                    g.setColor(lightBrown);
                }
                //This is a square
                //noinspection SuspiciousNameCombination
                g.fillRect(x * cellWidth, y * cellWidth, cellWidth, cellWidth);
                g.setColor(Color.black);
                //noinspection SuspiciousNameCombination
                g.drawRect(x * cellWidth, y * cellWidth, cellWidth, cellWidth);
            }
        }
    }

    /**
     * Draws the pieces. Assumes that boardState is up to date
     * @param g
     */
    private void drawPieces(Graphics g) {
        int x = 1; //the column to paint to
        int y = 8; //the row to paint to
        String pieces = boardState.split(" ")[0]; // Get the first part of the FEN, the pieces
        for (int i = 0; i < pieces.length() ; i++) {
            char c = pieces.charAt(i);
            Image picture = null;
            switch (c) {
                case 'R':
                    picture = whiteRook;
                    break;
                case 'N':
                    picture = whiteKnight;
                    break;
                case 'B':
                    picture = whiteBishop;
                    break;
                case 'Q':
                    picture = whiteQueen;
                    break;
                case 'K':
                    picture = whiteKing;
                    break;
                case 'P':
                    picture = whitePawn;
                    break;
                case 'r':
                    picture = blackRook;
                    break;
                case 'n':
                    picture = blackKnight;
                    break;
                case 'b':
                    picture = blackBishop;
                    break;
                case 'q':
                    picture = blackQueen;
                    break;
                case 'k':
                    picture = blackKing;
                    break;
                case 'p':
                    picture = blackPawn;
                    break;
                case '/':
                    y--; //go to next row
                    x = 0; // the +1 is done at the end
                    break;
                default:
                    // In this case it should be a number
                    x += Character.getNumericValue(c) - 1;  // minus 1 because it was already moved one
                                                            // step when painting the previous piece
                    break;
            }
            if (picture != null) {
                g.drawImage(picture, (x-1)*cellWidth, (8-y)*cellWidth, cellWidth, cellWidth, null);
            }
            x++; //move the drawing pointer one place to the right
        }
    }

    public void requestBoardRepaint(String boardState) {
        this.boardState = boardState;
        this.boardRepaintNeeded = true;
    }

    public void setSelectedPieceSquare(int square, List<Move> possibleMoves) {
        this.selectedPieceSquare = square;
        this.selectedPieceMoves = possibleMoves;
    }

    /**
     * Converts a graphical point to a squarenumber (1 indexed)
     * @param p the graphical point with the origin in the upper left corner and positive y axis down
     * @return the square number, 1 indexed
     */
    public static int getSquare(Point p) {
        return (p.x / cellWidth) + ((7 - (p.y / cellWidth)) * 8) + 1;
    }

    /**
     * Converts a graphical point to a squarenumber (1 indexed)
     * @param graphicalX the graphical x to convert, positive to the right, 0 lies left
     * @param graphicalY the graphical y to convert, positive down, 0 lies up top.
     * @return the square number, 1 indexed
     */
    public static int getSquare(int graphicalX, int graphicalY) {
        return getSquare(new Point(graphicalX, graphicalY));
    }

    /**
     * Converts a square number (1 indexed) to a graphical position
     * @param square the square to get the position of
     * @return a graphical position of the given square
     */
    public static Point getGraphicalPosition(int square) {
        int x = (square - 1) % 8;
        int y = (square - 1) / 8;
        int graphicalX = x * cellWidth;
        int graphicalY = (7 - y) * cellWidth;
        return new Point(graphicalX, graphicalY);
    }
}
