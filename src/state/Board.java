package state;

import moveGenerator.LegalGenerator;

import java.util.*;

/**
 * Bitboard representation. Uses a long as the bitboard, as it is 64 bits long, the first bit represents a1,
 * the last bit represents h8, from left to right from bottom to top
 */
public class Board {
    private long[] bitboards; // There are 12 different piece types
    private int[][] mailbox; // a redundant matrix to identify a piece by square (first coordinate being the x, second the y) (0 indexed)
    private boolean whiteToMove;
    private Stack<Boolean> whiteQueenSideCastle;
    private Stack<Boolean> whiteKingSideCastle;
    private Stack<Boolean> blackQueenSideCastle;
    private Stack<Boolean> blackKingSideCastle;
    private Stack<Integer> enPassentSquare; // Ranging from 1 to 64. 0 if no square. Denotes the square right behind a double pawn push.
                                 // So after 1.e4, this will be 21 (e3)
    public Stack<Integer> halfMoveClock; // A clock counting the half moves since the last pawn move or capture. Used to determine
                               // the fifty-move rule
    public Stack<Move> moves = new Stack<>();

    private Stack<String> fens = new Stack<>(); // A stack of fen representations of the past board positions to calculate threefold repetition

    private List<Move> currentlyPossibleMoves;

    private LegalGenerator generator = new LegalGenerator();

    public Board() {
        // Set first values for the stack to circumsize EmptyStackExceptions
        whiteQueenSideCastle = new Stack<>();
        whiteKingSideCastle = new Stack<>();
        blackQueenSideCastle = new Stack<>();
        blackKingSideCastle = new Stack<>();
        enPassentSquare = new Stack<>();
        halfMoveClock = new Stack<>();
        whiteQueenSideCastle.push(true);
        whiteKingSideCastle.push(true);
        blackQueenSideCastle.push(true);
        blackKingSideCastle.push(true);
        enPassentSquare.push(0);
        halfMoveClock.push(0);

        bitboards = new long[14]; //(0 and 1 are not used due to the setup of the integers. 0 would be an empty square of the white pieces
        // while 1 would be an empty square of the black pieces.
        bitboards[Constants.WHITE_ROOK] = Constants.INITIAL_WHITE_ROOKS;
        bitboards[Constants.WHITE_KNIGHT] = Constants.INITIAL_WHITE_KNIGHTS;
        bitboards[Constants.WHITE_BISHOP] = Constants.INITIAL_WHITE_BISHOPS;
        bitboards[Constants.WHITE_QUEEN] = Constants.INITIAL_WHITE_QUEEN;
        bitboards[Constants.WHITE_KING] = Constants.INITIAL_WHITE_KING;
        bitboards[Constants.WHITE_PAWN] = Constants.INITIAL_WHITE_PAWNS;
        bitboards[Constants.BLACK_ROOK] = Constants.INITIAL_BLACK_ROOKS;
        bitboards[Constants.BLACK_KNIGHT] = Constants.INITIAL_BLACK_KNIGHTS;
        bitboards[Constants.BLACK_BISHOP] = Constants.INITIAL_BLACK_BISHOPS;
        bitboards[Constants.BLACK_QUEEN] = Constants.INITIAL_BLACK_QUEEN;
        bitboards[Constants.BLACK_KING] = Constants.INITIAL_BLACK_KING;
        bitboards[Constants.BLACK_PAWN] = Constants.INITIAL_BLACK_PAWNS;
        whiteToMove = true;
        whiteQueenSideCastle.push(true);
        whiteKingSideCastle.push(true);
        blackKingSideCastle.push(true);
        blackQueenSideCastle.push(true);
        mailbox = new int[8][8];
        for (int i = 0; i < 8; i++) {
            System.arraycopy(Constants.INITIAL_MAILBOX[i], 0, mailbox[i], 0, Constants.INITIAL_MAILBOX[i].length);
        }
        currentlyPossibleMoves = generator.getMoves(this, Constants.WHITE);
    }

    /**
     * Makes a board with a specific position
     * @param s         the board position in Forsyth-Edwards notation
     */
    public Board(String s) {
        // First read the mailbox implementation
        mailbox = new int[8][8];
        String[] parts = s.split("\\s+");
        int x = 0;
        int y = 7;
        for (int i = 0; i < parts[0].length(); i++) { // only take the first bit, the piece placement
            char c = parts[0].charAt(i);
            switch (c) {
                case 'R':
                    mailbox[x][y] = Constants.WHITE_ROOK;
                    x++;
                    break;
                case 'N':
                    mailbox[x][y] = Constants.WHITE_KNIGHT;
                    x++;
                    break;
                case 'B':
                    mailbox[x][y] = Constants.WHITE_BISHOP;
                    x++;
                    break;
                case 'Q':
                    mailbox[x][y] = Constants.WHITE_QUEEN;
                    x++;
                    break;
                case 'K':
                    mailbox[x][y] = Constants.WHITE_KING;
                    x++;
                    break;
                case 'P':
                    mailbox[x][y] = Constants.WHITE_PAWN;
                    x++;
                    break;
                case 'r':
                    mailbox[x][y] = Constants.BLACK_ROOK;
                    x++;
                    break;
                case 'n':
                    mailbox[x][y] = Constants.BLACK_KNIGHT;
                    x++;
                    break;
                case 'b':
                    mailbox[x][y] = Constants.BLACK_BISHOP;
                    x++;
                    break;
                case 'q':
                    mailbox[x][y] = Constants.BLACK_QUEEN;
                    x++;
                    break;
                case 'k':
                    mailbox[x][y] = Constants.BLACK_KING;
                    x++;
                    break;
                case 'p':
                    mailbox[x][y] = Constants.BLACK_PAWN;
                    x++;
                    break;
                case '/':
                    y--;
                    x = 0;
                    break;
                default: // a number
                    x += Character.getNumericValue(c);
                    break;
            }
        }

        this.bitboards = convertMailboxToBitboards(mailbox);

        whiteQueenSideCastle = new Stack<>();
        whiteKingSideCastle = new Stack<>();
        blackQueenSideCastle = new Stack<>();
        blackKingSideCastle = new Stack<>();
        enPassentSquare = new Stack<>();
        halfMoveClock = new Stack<>();

        whiteToMove = parts[1].charAt(0) == 'w'; // true if it is a w, false if it is a b

        whiteQueenSideCastle.push(parts[2].contains("Q"));
        whiteKingSideCastle.push(parts[2].contains("K"));
        blackQueenSideCastle.push(parts[2].contains("q"));
        blackKingSideCastle.push(parts[2].contains("k"));

        char enPassentX = parts[3].charAt(0);
        if (enPassentX == '-') {
            enPassentSquare.push(0);
        } else {
            char enPassentY = parts[3].charAt(1);
            int enPassentToPush = (Character.getNumericValue(enPassentY) - 1) * 8 + (enPassentX - 96);
            enPassentSquare.push(enPassentToPush);
        }

        halfMoveClock.push(Integer.parseInt(parts[4]));
        for (int i = 1; i < Integer.parseInt(parts[5]) * 2; i++) {
            moves.add(new Move(0, 0, 0, 0, 0)); // add a dummy move
        }
        currentlyPossibleMoves = generator.getMoves(this, colorToMove());
    }

    /**
     * Gets the piecetype defined in Constants (WHITE_ROOK etc) by square.
     * @param x The x-coordinate (0 indexed)
     * @param y The y-coordinate (0 indexed)
     */
    public int getPieceType(int x, int y) {
        return mailbox[x][y];
    }

    /**
     * Gets the piecetype defined in Constants (WHITE_ROOK etc) by square.
     * @param square the square (1 indexed)
     * @return the piecetype
     */
    public int getPieceType(int square) {
        return getPieceType((square-1) % 8, (square - 1) / 8);
    }

    /**
     * Executes move m
     * @param m the move to execute
     */
    public void makeMove(Move m) {
        int enPassentSquareToPush = 0; // These will be pushed to the stack at the end. To prevent multiple pushes.
        int halfMoveClockToPush = halfMoveClock.peek() + 1; // to increment it
        boolean whiteKingSideCastleToPush = whiteKingSideCastle.peek();
        boolean whiteQueenSideCastleToPush = whiteQueenSideCastle.peek();
        boolean blackKingSideCastleToPush = blackKingSideCastle.peek();
        boolean blackQueenSideCastleToPush = blackQueenSideCastle.peek();
        moves.push(m);
        int movingPiece = m.getMovingPiece(); // directly the index in the bitmap array
        int fromField = m.getFrom();
        int toField = m.getTo();

        if (movingPiece == Constants.WHITE_KING) { //revoke castling rights
            whiteKingSideCastleToPush = false;
            whiteQueenSideCastleToPush = false;
        } else if (movingPiece == Constants.BLACK_KING) {
            blackKingSideCastleToPush = false;
            blackQueenSideCastleToPush =false;
        } else if (movingPiece == Constants.WHITE_ROOK) {
            if (fromField == 1) {
                whiteQueenSideCastleToPush =false;
            } else if (fromField == 8) {
                whiteKingSideCastleToPush =false;
            }
        } else if (movingPiece == Constants.BLACK_ROOK) {
            if (fromField == 57) {
                blackQueenSideCastleToPush =false;
            } else if (fromField == 64) {
                blackKingSideCastleToPush =false;
            }
        }
        // Set en-passant and half move clock if applicable
        if (movingPiece == Constants.WHITE_PAWN) { // If it is a white pawn
            halfMoveClockToPush = 0;
             if (toField - fromField == 16) { //If it is a white pawn push
                 enPassentSquareToPush = toField - 8;
             }
        } else if (movingPiece == Constants.BLACK_PAWN) { // If it is a black pawn
            halfMoveClockToPush = 0;
            if (fromField - toField == 16) { //If it is a black pawn push
                enPassentSquareToPush = toField + 8;
            }
        }

        // delete the piece from the old square
        bitboards[movingPiece] = bitboards[movingPiece] & ~(1L << (64 - fromField)); // A mask setting the "from" bit to 0
        // set the piece to the new square
        if (!m.isPromotion()) { // set the bit of the correct map
            bitboards[movingPiece] = bitboards[movingPiece] | (1L << (64 - toField)); // A mask setting the "to" bit to 1
        } else {
            int promotionPiece = m.getPromotionPiece() | Constants.getColor(movingPiece);
            bitboards[promotionPiece] = bitboards[promotionPiece] | (1L << (64 - toField));
        }
        if (m.isKingCastle()) {
            if (movingPiece == Constants.WHITE_KING) {
                bitboards[Constants.WHITE_ROOK] = bitboards[Constants.WHITE_ROOK] & ~(1L << (64 - 8)); // Set the right rook to 0
                bitboards[Constants.WHITE_ROOK] = bitboards[Constants.WHITE_ROOK] | (1L << (64 - 6)); // Set the rook on square 6
            } else if (movingPiece == Constants.BLACK_KING) { //Can only be BLACK_KING, but still check for safety
                bitboards[Constants.BLACK_ROOK] = bitboards[Constants.BLACK_ROOK] & ~(1L << (64 - 64));
                bitboards[Constants.BLACK_ROOK] = bitboards[Constants.BLACK_ROOK] | (1L << (64 - 62));
            }
        } else if (m.isQueenCastle()) {
            if (movingPiece == Constants.WHITE_KING) {
                bitboards[Constants.WHITE_ROOK] = bitboards[Constants.WHITE_ROOK] & ~(1L << (64 - 1)); // Set the right rook to 0
                bitboards[Constants.WHITE_ROOK] = bitboards[Constants.WHITE_ROOK] | (1L << (64 - 4)); // Set the rook on square 6
            } else if (movingPiece == Constants.BLACK_KING) { //Can only be BLACK_KING, but still check for safety
                bitboards[Constants.BLACK_ROOK] = bitboards[Constants.BLACK_ROOK] & ~(1L << (64 - 57));
                bitboards[Constants.BLACK_ROOK] = bitboards[Constants.BLACK_ROOK] | (1L << (64 - 60));
            }
        }
        int toX = (toField-1) % 8;
        int toY = (toField-1) / 8;
        int fromX = (fromField-1) % 8;
        int fromY = (fromField-1) / 8;
        // remove the captured piece
        if (m.isCapture()) { //if there is a capture:
            halfMoveClockToPush = 0;

            if (!m.isEnPassent()) {
                int capturedPiece = m.getCapturedPiece();
                bitboards[capturedPiece] = bitboards[capturedPiece] & ~(1L << (64 - toField)); // A mask setting the "to" bit to 0
                if (capturedPiece == Constants.WHITE_ROOK) { //revoke castling rights if rooks are captured
                    if (toField == 1) whiteQueenSideCastleToPush = false;
                    if (toField == 8) whiteKingSideCastleToPush = false;
                } else if (capturedPiece == Constants.BLACK_ROOK) {
                    if (toField == 57) blackQueenSideCastleToPush = false;
                    if (toField == 64) blackKingSideCastleToPush = false;
                }
            } else {
                if (movingPiece == Constants.WHITE_PAWN) {
                    int captureField = fromY * 8 + toX + 1;
                    bitboards[Constants.BLACK_PAWN] = bitboards[Constants.BLACK_PAWN] & ~(1L << (64 - captureField));
                } else if (movingPiece == Constants.BLACK_PAWN) {
                    int captureField = fromY * 8 + toX + 1;
                    bitboards[Constants.WHITE_PAWN] = bitboards[Constants.WHITE_PAWN] & ~(1L << (64 - captureField));
                }
            }
        }
        // Update the mailbox representation
        mailbox[fromX][fromY] = Constants.EMPTY;
        if (!m.isPromotion()) {
            mailbox[toX][toY] = movingPiece;
        } else {
            int promotionPiece = m.getPromotionPiece() | Constants.getColor(movingPiece);
            mailbox[toX][toY] = promotionPiece;
        }
        if (m.isKingCastle()) {
            if (movingPiece == Constants.WHITE_KING) {
                mailbox[7][0] = Constants.EMPTY;
                mailbox[5][0] = Constants.WHITE_ROOK;
            } else if (movingPiece == Constants.BLACK_KING) { //Can only be BLACK_KING, but still check for safety
                mailbox[7][7] = Constants.EMPTY;
                mailbox[5][7] = Constants.BLACK_ROOK;
            }
        } else if (m.isQueenCastle()) {
            if (movingPiece == Constants.WHITE_KING) {
                mailbox[0][0] = Constants.EMPTY;
                mailbox[3][0] = Constants.WHITE_ROOK;
            } else if (movingPiece == Constants.BLACK_KING) { //Can only be BLACK_KING, but still check for safety
                mailbox[0][7] = Constants.EMPTY;
                mailbox[3][7] = Constants.BLACK_ROOK;
            }
        } else if (m.isEnPassent()) { //remove the trailing pawn
            mailbox[toX][fromY] = Constants.EMPTY;
        }

        enPassentSquare.push(enPassentSquareToPush);
        halfMoveClock.push(halfMoveClockToPush);
        whiteKingSideCastle.push(whiteKingSideCastleToPush);
        whiteQueenSideCastle.push(whiteQueenSideCastleToPush);
        blackKingSideCastle.push(blackKingSideCastleToPush);
        blackQueenSideCastle.push(blackQueenSideCastleToPush);

        // Switch turns
        whiteToMove = !whiteToMove;
        // get the new moves
        this.currentlyPossibleMoves = generator.getMoves(this, colorToMove());
        fens.push(this.FENwithoutMoveCount());
    }

    /**
     * Undos move m.
     * @throws IllegalArgumentException if m is not the exact last move on the stack
     * @param m
     */
    public void unMakeMove(Move m) {
        if (moves.peek() != m) {
            throw new IllegalArgumentException("This move was not the last one played");
        }
        //Undo the castling rights
        whiteKingSideCastle.pop();
        whiteQueenSideCastle.pop();
        blackKingSideCastle.pop();
        blackQueenSideCastle.pop();
        enPassentSquare.pop();
        //undo halfmoveclock
        halfMoveClock.pop();
        //delete this move from the moves list
        moves.pop();
        fens.pop();
        //set the turn
        whiteToMove = !whiteToMove;

        //the bitboard representation
        int toField = m.getTo();
        int fromField = m.getFrom();
        int movingPiece = m.getMovingPiece();
        bitboards[movingPiece] = bitboards[movingPiece] & ~(1L << (64 - toField)); // Remove the piece from the to field
        bitboards[movingPiece] = bitboards[movingPiece] | (1L << (64 - fromField)); // Set the piece to the from field
        if (m.isPromotion()) { //if promotion, remove the promotionpiece
            int promotionPiece = m.getPromotionPiece() | Constants.getColor(movingPiece);
            bitboards[promotionPiece] = bitboards[promotionPiece] & ~(1L << (64 - toField));
        }
        int toX = (toField-1) % 8; //0 indexed
        int toY = (toField-1) / 8;
        int fromX = (fromField-1) % 8;
        int fromY = (fromField-1) / 8;
        if (m.isCapture()) {
            if (m.isEnPassent()) {
                if (movingPiece == Constants.WHITE_PAWN) {
                    int captureField = fromY * 8 + toX + 1;
                    bitboards[Constants.BLACK_PAWN] = bitboards[Constants.BLACK_PAWN] | (1L << (64 - captureField));
                } else if (movingPiece == Constants.BLACK_PAWN) {
                    int captureField = fromY * 8 + toX + 1;
                    bitboards[Constants.WHITE_PAWN] = bitboards[Constants.WHITE_PAWN] | (1L << (64 - captureField));
                }
            } else { // normal capture, so set the piece back
                int capturedPiece = m.getCapturedPiece();
                bitboards[capturedPiece] = bitboards[capturedPiece] | (1L << 64 - toField);
            }
        } else { // can never be a castling if it was a capture
            // if it was a castling, also set the rook back
            if (m.isKingCastle()) {
                if (movingPiece == Constants.WHITE_KING) {
                    bitboards[Constants.WHITE_ROOK] = bitboards[Constants.WHITE_ROOK] | (1L << (64 - 8)); // Set the right rook to its original place
                    bitboards[Constants.WHITE_ROOK] = bitboards[Constants.WHITE_ROOK] & ~(1L << (64 - 6)); // remove the rook from 6
                } else if (movingPiece == Constants.BLACK_KING) { //Can only be BLACK_KING, but still check for safety
                    bitboards[Constants.BLACK_ROOK] = bitboards[Constants.BLACK_ROOK] | (1L << (64 - 64));
                    bitboards[Constants.BLACK_ROOK] = bitboards[Constants.BLACK_ROOK] & ~(1L << (64 - 62));
                }
            } else if (m.isQueenCastle()) {
                if (movingPiece == Constants.WHITE_KING) {
                    bitboards[Constants.WHITE_ROOK] = bitboards[Constants.WHITE_ROOK] | (1L << (64 - 1)); // Set the right rook to its original square
                    bitboards[Constants.WHITE_ROOK] = bitboards[Constants.WHITE_ROOK] & ~(1L << (64 - 4)); // Remove from square 6
                } else if (movingPiece == Constants.BLACK_KING) { //Can only be BLACK_KING, but still check for safety
                    bitboards[Constants.BLACK_ROOK] = bitboards[Constants.BLACK_ROOK] | (1L << (64 - 57));
                    bitboards[Constants.BLACK_ROOK] = bitboards[Constants.BLACK_ROOK] & ~(1L << (64 - 60));
                }
            }
        }
        // normal moves and promotion undo
        mailbox[fromX][fromY] = movingPiece;
        mailbox[toX][toY] = Constants.EMPTY;
        if (m.isCapture()) {
            if (m.isEnPassent()) { // add the pawn
                mailbox[toX][fromY] = Constants.PAWN | Constants.getColor(movingPiece)^1; // opposite color pawn
            } else { // normal capture
                int capturedPiece = m.getCapturedPiece();
                mailbox[toX][toY] = capturedPiece;
            }
        } else if (m.isKingCastle()) { // undo the castlings
            if (movingPiece == Constants.WHITE_KING) {
                mailbox[7][0] = Constants.WHITE_ROOK;
                mailbox[5][0] = Constants.EMPTY;
            } else if (movingPiece == Constants.BLACK_KING) {
                mailbox[7][7] = Constants.BLACK_ROOK;
                mailbox[5][7] = Constants.EMPTY;
            }
        } else if (m.isQueenCastle()) {
            if (movingPiece == Constants.WHITE_KING) {
                mailbox[0][0] = Constants.WHITE_ROOK;
                mailbox[3][0] = Constants.EMPTY;
            } else if (movingPiece == Constants.BLACK_KING) {
                mailbox[0][7] = Constants.BLACK_ROOK;
                mailbox[3][7] = Constants.EMPTY;
            }
        }
        // get the new moves
        this.currentlyPossibleMoves = generator.getMoves(this, colorToMove());
    }

    public boolean isWhiteToMove() {
        return this.whiteToMove;
    }

    public int colorToMove() {
        return this.isWhiteToMove() ? Constants.WHITE : Constants.BLACK;
    }

    /**
     * Returns a copy of the bitboards
     * @return a copy of the bitboards
     */
    public long[] getBitboards() {
        return this.bitboards.clone(); // a copy of the bitboards array
    }

    /**
     * A FEN representation of the board
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(FENwithoutMoveCount()); // add the move counts to the fen

        s.append(' ');

        //append half-movecount
        s.append(halfMoveClock.peek());

        s.append(' ');

        //append total move count
        s.append((moves.size() / 2) + 1);

        return s.toString(); // remove the trailing /
    }

    /**
     * A Fen representation of the board without the move counts. Used in threefold repetition checks.
     * @return
     */
    public String FENwithoutMoveCount() {
        StringBuilder s = new StringBuilder();
        String prefix = ""; // The delimiter
        for (int y = 7; y >= 0; y--) {
            int emptyCount = 0;
            s.append(prefix);
            for (int x = 0; x < 8; x++) {
                int pieceType = getPieceType(x, y);
                switch (pieceType) {
                    case Constants.WHITE_ROOK:
                        if (emptyCount != 0) {
                            s.append(emptyCount);
                            emptyCount = 0;
                        }
                        s.append('R');
                        break;
                    case Constants.WHITE_BISHOP:
                        if (emptyCount != 0) {
                            s.append(emptyCount);
                            emptyCount = 0;
                        }
                        s.append('B');
                        break;
                    case Constants.WHITE_KNIGHT:
                        if (emptyCount != 0) {
                            s.append(emptyCount);
                            emptyCount = 0;
                        }
                        s.append('N');
                        break;
                    case Constants.WHITE_QUEEN:
                        if (emptyCount != 0) {
                            s.append(emptyCount);
                            emptyCount = 0;
                        }
                        s.append('Q');
                        break;
                    case Constants.WHITE_KING:
                        if (emptyCount != 0) {
                            s.append(emptyCount);
                            emptyCount = 0;
                        }
                        s.append('K');
                        break;
                    case Constants.WHITE_PAWN:
                        if (emptyCount != 0) {
                            s.append(emptyCount);
                            emptyCount = 0;
                        }
                        s.append('P');
                        break;
                    case Constants.BLACK_ROOK:
                        if (emptyCount != 0) {
                            s.append(emptyCount);
                            emptyCount = 0;
                        }
                        s.append('r');
                        break;
                    case Constants.BLACK_BISHOP:
                        if (emptyCount != 0) {
                            s.append(emptyCount);
                            emptyCount = 0;
                        }
                        s.append('b');
                        break;
                    case Constants.BLACK_KNIGHT:
                        if (emptyCount != 0) {
                            s.append(emptyCount);
                            emptyCount = 0;
                        }
                        s.append('n');
                        break;
                    case Constants.BLACK_QUEEN:
                        if (emptyCount != 0) {
                            s.append(emptyCount);
                            emptyCount = 0;
                        }
                        s.append('q');
                        break;
                    case Constants.BLACK_KING:
                        if (emptyCount != 0) {
                            s.append(emptyCount);
                            emptyCount = 0;
                        }
                        s.append('k');
                        break;
                    case Constants.BLACK_PAWN:
                        if (emptyCount != 0) {
                            s.append(emptyCount);
                            emptyCount = 0;
                        }
                        s.append('p');
                        break;
                    case Constants.EMPTY:
                        emptyCount++;
                }
            } // at the end of each row
            if (emptyCount != 0) {
                s.append(emptyCount);
            }
            prefix = "/";
        }

        s.append(' ');

        //Active colour
        char activeColour = whiteToMove ? 'w' : 'b';
        s.append(activeColour);

        s.append(' ');

        //Castling rights
        if (!(blackKingSideCastle.peek() || blackQueenSideCastle.peek() || whiteKingSideCastle.peek() || whiteQueenSideCastle.peek())) {
            s.append('-');
        } else {
            if (whiteKingSideCastle.peek()) s.append('K');
            if (whiteQueenSideCastle.peek()) s.append('Q');
            if (blackKingSideCastle.peek()) s.append('k');
            if (blackQueenSideCastle.peek()) s.append('q');
        }

        s.append(' ');

        // en passant
        if (enPassentSquare.peek() == 0) s.append('-');
        else {
            int x = enPassentSquare.peek() % 8;
            x = x == 0 ? 8 : x; //8 % 8 will be 0, but we want 8
            int y = (enPassentSquare.peek() / 8) + 1;
            char xChar = (char) (x + 96); // 97 is a, 104 is h
            s.append(xChar);
            s.append(y);
        }
        return s.toString();
    }

    /**
     * Gets the white pieces
     * @return a bitboard representation of the white pieces
     */
    public long getWhitePieces() {
        return bitboards[Constants.WHITE_ROOK] | bitboards[Constants.WHITE_KNIGHT] | bitboards[Constants.WHITE_BISHOP] |
                bitboards[Constants.WHITE_QUEEN] | bitboards[Constants.WHITE_KING] | bitboards[Constants.WHITE_PAWN];
    }

    /**
     * Gets the black pieces
     * @return a bitboard representation of the black pieces
     */
    public long getBlackPieces() {
        return bitboards[Constants.BLACK_ROOK] | bitboards[Constants.BLACK_KNIGHT] | bitboards[Constants.BLACK_BISHOP] |
                bitboards[Constants.BLACK_QUEEN] | bitboards[Constants.BLACK_KING] | bitboards[Constants.BLACK_PAWN];
    }

    /**
     *
     * @param color, one of Constants.WHITE/Constants.BLACK
     * @return the pieces of the color specified by color
     */
    public long getPieces(int color) {
        if (color == Constants.WHITE) {
            return getWhitePieces();
        } else if (color == Constants.BLACK) {
            return getBlackPieces();
        }
        throw new IllegalArgumentException(color + " is not one of Constants.White/Constants.Black");
    }

    /**
     * Gets the occupied squares
     * @return a bitboard representation of the occupied squares
     */
    public long getOccupied() {
        return getBlackPieces() | getWhitePieces();
    }

    /**
     * Gets the empty squares
     * @return a bitboard representation of the empty squares
     */
    public long getEmptySquares() {
        return ~(getBlackPieces() | getWhitePieces());
    }

    /**
     * Gets the en passent square
     * @return the en passent square 1 indexed.
     */
    public int getEnPassentSquare() {
        return this.enPassentSquare.peek();
    }

    /**
     * Gets the en passent square
     * @return the en passent square in bitboard form or 0 if there is no en passent square
     */
    public long getEnPassenSquareBitboard() {
        return (this.enPassentSquare.peek() != 0) ? 1L << (64 - this.enPassentSquare.peek()) : 0;
    }

    public boolean isWhiteQueenSideCastle() {
        return this.whiteQueenSideCastle.peek();
    }

    public boolean isWhiteKingSideCastle() {
        return this.whiteKingSideCastle.peek();
    }

    public boolean isBlackQueenSideCastle() {
        return this.blackQueenSideCastle.peek();
    }

    public boolean isBlackKingSideCastle() {
        return this.blackKingSideCastle.peek();
    }

    /**
     * Finds whether or not the piece on square square is a slider (queen, rook or bishop)
     * @param square, the int of the square, 1 indexed
     * @return true if it is a slider (color doesn't matter) false otherwise
     */
    public boolean isSlider(int square) {
        int piece = getPieceType((square - 1) % 8, (square-1) / 8); // 0 indexed
        return piece == Constants.WHITE_BISHOP || piece == Constants.WHITE_ROOK ||
                piece == Constants.WHITE_QUEEN || piece == Constants.BLACK_BISHOP ||
                piece == Constants.BLACK_ROOK || piece == Constants.BLACK_QUEEN;
    }

    /**
     * Gets all the legal moves for color @param color
     * @return a list of all the legal moves for color @param color
     */
    public List<Move> getMoves() {
        return this.currentlyPossibleMoves;
    }

    public boolean isCheckMate() {
        return currentlyPossibleMoves.size() == 0 && colorToMoveInCheck();
    }

    public boolean isStaleMate() {
        return currentlyPossibleMoves.size() == 0 && !colorToMoveInCheck();
    }

    public boolean fiftyMoveRule() {
        return this.halfMoveClock.peek() >= 50;
    }

    public boolean threefoldRepetition() {
        return Collections.frequency(fens, this.FENwithoutMoveCount()) >= 3;
    }

    public boolean isPotentialDraw() {
        return threefoldRepetition() || fiftyMoveRule();
    }

    /**
     * Checks if the king of the color that currently has to move is in check
     * @return true if it is, false if not
     */
    public boolean colorToMoveInCheck() {
        if (whiteToMove) {
            return whiteInCheck();
        } else {
            return blackInCheck();
        }
    }

    /**
     * Checks if the white king is in check
     * @return true if the white king is attacked, false if not
     */
    public boolean whiteInCheck() {
        return (
                bitboards[Constants.WHITE_KING] &
                        generator.getAttackedSquaresIncludingBlocked(bitboards, getOccupied(), Constants.BLACK)
                ) != 0;
    }

    /**
     * Checks if the black king is in check
     * @return true if the black king is attacked, false if not
     */
    public boolean blackInCheck() {
        return (
                bitboards[Constants.BLACK_KING] &
                        generator.getAttackedSquaresIncludingBlocked(bitboards, getOccupied(), Constants.WHITE)
                ) != 0;
    }

    private long[] convertMailboxToBitboards(int[][] mailbox) {
        long[] bitboardsToReturn = new long[14];
        bitboardsToReturn[Constants.WHITE_ROOK] = convertMailboxToBitboard(mailbox, Constants.WHITE_ROOK);
        bitboardsToReturn[Constants.WHITE_KNIGHT] = convertMailboxToBitboard(mailbox, Constants.WHITE_KNIGHT);
        bitboardsToReturn[Constants.WHITE_BISHOP] = convertMailboxToBitboard(mailbox, Constants.WHITE_BISHOP);
        bitboardsToReturn[Constants.WHITE_QUEEN] = convertMailboxToBitboard(mailbox, Constants.WHITE_QUEEN);
        bitboardsToReturn[Constants.WHITE_KING] = convertMailboxToBitboard(mailbox, Constants.WHITE_KING);
        bitboardsToReturn[Constants.WHITE_PAWN] = convertMailboxToBitboard(mailbox, Constants.WHITE_PAWN);
        bitboardsToReturn[Constants.BLACK_ROOK] = convertMailboxToBitboard(mailbox, Constants.BLACK_ROOK);
        bitboardsToReturn[Constants.BLACK_KNIGHT] = convertMailboxToBitboard(mailbox, Constants.BLACK_KNIGHT);
        bitboardsToReturn[Constants.BLACK_BISHOP] = convertMailboxToBitboard(mailbox, Constants.BLACK_BISHOP);
        bitboardsToReturn[Constants.BLACK_QUEEN] = convertMailboxToBitboard(mailbox, Constants.BLACK_QUEEN);
        bitboardsToReturn[Constants.BLACK_KING] = convertMailboxToBitboard(mailbox, Constants.BLACK_KING);
        bitboardsToReturn[Constants.BLACK_PAWN] = convertMailboxToBitboard(mailbox, Constants.BLACK_PAWN);
        return bitboardsToReturn;
    }

    /**
     * Convert the mailbox to a bitboard of one piece
     * @param mailbox the mailbox to scan
     * @param piece the piece to look for, should include the color
     * @return a bitboard of the piece position
     */
    private long convertMailboxToBitboard(int[][] mailbox, int piece) {
        long bitboard = 0L;
        for (int x = 7; x >= 0; x--) { // from right to left
            for (int y = 7; y >= 0; y--) { // from top to bottom
                if (mailbox[x][y] == piece) {
                    int shift = Constants.ROW_SIZE * (7 - y) + (7 - x);
                    bitboard |= 1L << shift;
                }
            }
        }
        return bitboard;
    }

    /**
     * Find the value of a piece at a specific place. x and y are zero indexed. This is used in board evaluation.
     * @param x x coordinate, zero indexed
     * @param y y coordinate, zero indexed
     * @return the value of the piece on that spot
     */
    public int getPieceValue(int x, int y) {
        int pieceType = mailbox[x][y];
        switch (pieceType) {
            case Constants.WHITE_ROOK:
                return 5;
            case Constants.WHITE_KNIGHT:
                return 3;
            case Constants.WHITE_BISHOP:
                return 3;
            case Constants.WHITE_QUEEN:
                return 9;
            case Constants.WHITE_PAWN:
                return 1;
            case Constants.BLACK_ROOK:
                return -5;
            case Constants.BLACK_KNIGHT:
                return -3;
            case Constants.BLACK_BISHOP:
                return -3;
            case Constants.BLACK_QUEEN:
                return -9;
            case Constants.BLACK_PAWN:
                return -1;
            default: // a number
                return 0;
        }
    }

    /**
     * Returns the last move played
     * @return the last move played
     */
    public Move getLastMove() {
        return moves.peek();
    }

    public Board copy() {
        Board clone = new Board();
        clone.bitboards = Arrays.copyOf(this.bitboards, this.bitboards.length);
        for (int i = 0; i < this.mailbox.length; i++) {
            clone.mailbox[i] = Arrays.copyOf(this.mailbox[i], this.mailbox[i].length);
        }
        clone.whiteToMove = this.whiteToMove;
        clone.whiteQueenSideCastle = (Stack<Boolean>)this.whiteQueenSideCastle.clone();
        clone.whiteKingSideCastle = (Stack<Boolean>)this.whiteKingSideCastle.clone();
        clone.blackQueenSideCastle = (Stack<Boolean>)this.blackQueenSideCastle.clone();
        clone.blackKingSideCastle = (Stack<Boolean>)this.blackKingSideCastle.clone();
        clone.enPassentSquare = (Stack<Integer>)this.enPassentSquare.clone();
        clone.halfMoveClock = (Stack<Integer>)this.halfMoveClock.clone();
        clone.moves = (Stack<Move>)this.moves.clone();
        clone.fens = (Stack<String>)this.fens.clone();

        clone.generator = this.generator;
        clone.currentlyPossibleMoves = generator.getMoves(clone, clone.colorToMove());
        return clone;
    }
}
