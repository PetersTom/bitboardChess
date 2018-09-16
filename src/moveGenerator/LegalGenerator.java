package moveGenerator;

import state.Bitboard;
import state.Board;
import state.Constants;
import state.Move;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates only legal moves. First generates pseudo legal moves, then checks them.
 */
public class LegalGenerator {


    /**
     * Gets the legal moves for color @param color
     * @param board, the board entity containing the current state
     * @param color, one of Constants.WHITE/BLACK, the color to get the moves for
     * @return a list of legal moves
     */
    public List<Move> getMoves(Board board, int color) {
        long[] bitboards = board.getBitboards();
        long ownPieces = board.isWhiteToMove() ? board.getWhitePieces() : board.getBlackPieces();
        long opponentPieces = board.isWhiteToMove() ? board.getBlackPieces() : board.getWhitePieces();
        long occupied = board.getOccupied();
        long kingPosition = bitboards[Constants.KING | color];

        List<Move> moves = new ArrayList<>();

        long king = bitboards[Constants.KING | color];
        long occupiedWithoutKing = occupied & ~king; // Remove the king to calculate king danger squares
        long kingDangerSquares = getAttackedSquaresIncludingBlocked(bitboards, occupiedWithoutKing, color^1);
        long kingMoves = Constants.KING_ATTACKS[Long.numberOfLeadingZeros(king)] & ~ownPieces; // numberOfLeadingZeros is the square - 1
        // but the index in the attack array is 0 based, so no +1 is needed.
        // Add the king moves.
        moves.addAll(convertBitboardToMoves(board, king, kingMoves, opponentPieces, ~kingDangerSquares, Constants.KING, color));



        //Find the checkers
        long checkers = 0L;
        // Find opposite color rooks that attack this king
        long rookCheckers = getAxisAlignedAttackSquaresIncludingBlocked(king, occupied) & bitboards[Constants.ROOK | color^1];
        // Opposite color bishops that attack this king
        long bishopCheckers = getDiagonalAttackSquaresIncludingBlocked(king, occupied) & bitboards[Constants.BISHOP | color^1];
        // Queens
        long queenCheckers = getQueenAttackSquaresIncludingBlocked(king, occupied) & bitboards[Constants.QUEEN | color^1];
        // Knights
        long knightCheckers = getKnightAttackSquaresIncludingBlocked(king) & bitboards[Constants.KNIGHT | color^1];
        // Pawns, suppose there was a pawn on this kings place. If its attacks land on a pawn of the opponent, this king is
        // in check by that pawn. Therefore we want to get the attack squares of the pawns of this color and not the opposite.
        long pawnCheckers = getPawnAttackSquares(king, color) & bitboards[Constants.PAWN | color^1];

        checkers |= rookCheckers | bishopCheckers | queenCheckers | knightCheckers | pawnCheckers;

        int numCheckers = Bitboard.getPopCount(checkers);
        if (numCheckers > 1) { // double check, only king moves valid
            return moves;
        }

        long captureMask = 0xFFFFFFFFFFFFFFFFL; // Where captures are possible, only the checkers if there is a check
        long pushMask = 0xFFFFFFFFFFFFFFFFL; // Where quiet moves are possible, only the attack rays of sliders if there is a check

        if (numCheckers == 1) {
            // if ony one checker, we can evade check by capturing it
            captureMask = checkers;

            // If the piece giving check is a slider, we can evade check by blocking it
            int checkerSquare = Long.numberOfLeadingZeros(Bitboard.leastSignificantBit(checkers)) + 1;
            if (board.isSlider(checkerSquare)) {
                pushMask = getCombinedSliderRaysToSquare(king, bitboards[Constants.BISHOP | color^1], bitboards[Constants.QUEEN | color^1],
                        bitboards[Constants.ROOK | color^1], occupied) & ~checkers; // This would also result in the attackers themselves.
                // There can only be one attacker at this point (only one checker), so to get the push mask it suffices
                // to delete the checker(s).
            } else {
                pushMask = 0L; // If the checker is not a slider, no push can evade check.
            }
        }

        //castling
        if (numCheckers == 0) { // can only castle if not in check
            if (color == Constants.WHITE) {
                if (board.isWhiteKingSideCastle()) {
                    if ((occupied & 0x0600000000000000L) == 0) { // no pieces in between
                        if ((0x0600000000000000L & getAttackedSquaresIncludingBlocked(bitboards, occupied, Constants.BLACK)) == 0) { // not in or over check
                            moves.add(new Move(5, 7, Constants.WHITE_KING, Constants.EMPTY, 2));
                        }
                    }
                }
                if (board.isWhiteQueenSideCastle()) {
                    if ((occupied & 0x7000000000000000L) == 0) { // no pieces in between
                        if ((0x3000000000000000L & getAttackedSquaresIncludingBlocked(bitboards, occupied, Constants.BLACK)) == 0) { // not in or over check
                            moves.add(new Move(5, 3, Constants.WHITE_KING, Constants.EMPTY, 3));
                        }
                    }
                }
            } else { // black
                if (board.isBlackKingSideCastle()) {
                    if ((occupied & 0x0000000000000006L) == 0) { // no pieces in between
                        if ((0x0000000000000006L & getAttackedSquaresIncludingBlocked(bitboards, occupied, Constants.WHITE)) == 0) { // not in or over check
                            moves.add(new Move(61, 63, Constants.BLACK_KING, Constants.EMPTY, 2));
                        }
                    }
                }
                if (board.isBlackQueenSideCastle()) {
                    if ((occupied & 0x0000000000000070L) == 0) { // no pieces in between
                        if ((0x0000000000000030L & getAttackedSquaresIncludingBlocked(bitboards, occupied, Constants.WHITE)) == 0) { // not in or over check
                            moves.add(new Move(61, 59, Constants.BLACK_KING, Constants.EMPTY, 3));
                        }
                    }
                }
            }
        }

        // get all the pawn moves
        long pawns = bitboards[Constants.PAWN | color];
        for (long p : getIndividualPieces(pawns)) {
            long possiblePawnAttacks = getPawnAttackSquares(p, color);
            long possibleCaptures = possiblePawnAttacks & opponentPieces; // can only capture opponent pieces
            long possibleEnPassentCapture = possiblePawnAttacks & board.getEnPassenSquareBitboard();
            long lastRank = color == Constants.WHITE ? Constants.EIGHTH_RANK : Constants.FIRST_RANK;
            long possiblePawnPushes;
            long possiblePawnDoublePushes = 0;
            if (color == Constants.WHITE) {
                possiblePawnPushes = Bitboard.nortOne(p) & ~occupied;
                if ((p & Constants.SECOND_RANK) != 0) { //double move possible
                    possiblePawnDoublePushes = Bitboard.nortOne(possiblePawnPushes) & ~occupied;
                }
            } else {
                possiblePawnPushes = Bitboard.soutOne(p) & ~occupied;
                if ((p & Constants.SEVENTH_RANK) != 0) { // double move possible
                    possiblePawnDoublePushes = Bitboard.soutOne(possiblePawnPushes) & ~occupied;
                }
            }
            possiblePawnPushes &= pushMask;
            possiblePawnDoublePushes &= pushMask;
            possibleCaptures &= captureMask;

            long possiblePromotions = possiblePawnPushes & lastRank;
            possiblePromotions |= possibleCaptures & lastRank; // capture and pushmask are already taken care of
            // remove the promotions from the regular moves
            possiblePawnPushes &= ~possiblePromotions;
            possibleCaptures &= ~possiblePromotions;

            // check en passent against pushmask and capturemask
            long enPassentCaptureSquare = color == Constants.WHITE ? Bitboard.soutOne(possibleEnPassentCapture) : Bitboard.nortOne(possibleEnPassentCapture);
            if ((enPassentCaptureSquare & captureMask) == 0 && (possibleEnPassentCapture & pushMask) == 0) {
                //if the en passent move does not capture the checking piece or moves in between the checker and the king,
                //the en passent is not valid and it will be set to 0
                possibleEnPassentCapture = 0;
            }
            List<Long> sliderRaysToKing = getSliderRaysToSquare(kingPosition, bitboards[Constants.BISHOP | color^1],
                    bitboards[Constants.QUEEN | color^1], bitboards[Constants.ROOK | color^1], occupied & ~p);
            long pinningRay = getIntersectingRay(sliderRaysToKing, p);
            moves.addAll(convertPawnBitboardToMoves(board, p, possiblePawnPushes | possibleCaptures, pinningRay,
                    opponentPieces, color, false, false, false));
            moves.addAll(convertPawnBitboardToMoves(board, p, possiblePawnDoublePushes, pinningRay, opponentPieces, color,
                    false, false, true));
            moves.addAll(convertPawnBitboardToMoves(board, p, possiblePromotions, pinningRay, opponentPieces, color,
                    false, true, false));

            // For en passent captures, also check if the capture does not put the king into check by removing the captured pawn
            sliderRaysToKing = getSliderRaysToSquare(kingPosition, bitboards[Constants.BISHOP | color^1],
                    bitboards[Constants.QUEEN | color^1], bitboards[Constants.ROOK | color^1], occupied & ~p & ~enPassentCaptureSquare);
            pinningRay = getIntersectingRay(sliderRaysToKing, p);
            moves.addAll(convertPawnBitboardToMoves(board, p, possibleEnPassentCapture, pinningRay, opponentPieces, color,
                    true, false, false));
        }

        // get all the other moves while keeping track of pins
        // get the rook moves
        long rooks = bitboards[Constants.ROOK | color];
        for (long r : getIndividualPieces(rooks)) {
            long possibleMoves = getAxisAlignedAttackSquaresIncludingBlocked(r, occupied) & ~ownPieces; // make sure to not attack own pieces
            possibleMoves &= captureMask | pushMask; // Apply the capture and push mask
            List<Long> sliderRaysToKing = getSliderRaysToSquare(kingPosition, bitboards[Constants.BISHOP | color^1],
                    bitboards[Constants.QUEEN | color^1], bitboards[Constants.ROOK | color^1], occupied & ~r); // remove r from occupied to see if it is pinned
            long pinningRay = getIntersectingRay(sliderRaysToKing, r);
            moves.addAll(convertBitboardToMoves(board, r, possibleMoves, opponentPieces, pinningRay, Constants.ROOK, color));
        }

        // get the bishop moves
        long bishops = bitboards[Constants.BISHOP | color];
        for (long b : getIndividualPieces(bishops)) {
            long possibleMoves = getDiagonalAttackSquaresIncludingBlocked(b, occupied) & ~ownPieces; // make sure to not attack own pieces
            possibleMoves &= captureMask | pushMask; // Apply the capture and push mask
            List<Long> sliderRaysToKing = getSliderRaysToSquare(kingPosition, bitboards[Constants.BISHOP | color^1],
                    bitboards[Constants.QUEEN | color^1], bitboards[Constants.ROOK | color^1], occupied & ~b);
            long pinningRay = getIntersectingRay(sliderRaysToKing, b);
            moves.addAll(convertBitboardToMoves(board, b, possibleMoves, opponentPieces, pinningRay, Constants.BISHOP, color));
        }

        // get the knight moves
        long knights = bitboards[Constants.KNIGHT | color];
        for (long k : getIndividualPieces(knights)) {
            long possibleMoves = getKnightAttackSquaresIncludingBlocked(k) & ~ownPieces; // make sure to not attack own pieces
            possibleMoves &= captureMask | pushMask; // Apply the capture and push mask
            // legalmoves is 0 because the knight cannot be pinned
            List<Long> sliderRaysToKing = getSliderRaysToSquare(kingPosition, bitboards[Constants.BISHOP | color^1],
                    bitboards[Constants.QUEEN | color^1], bitboards[Constants.ROOK | color^1], occupied & ~k); // to check if the knight is pinned
            long pinningRay = getIntersectingRay(sliderRaysToKing, k);
            moves.addAll(convertBitboardToMoves(board, k, possibleMoves, opponentPieces, pinningRay, Constants.KNIGHT, color));
        }

        // get the queen moves
        long queens = bitboards[Constants.QUEEN | color];
        for (long q : getIndividualPieces(queens)) {
            long possibleMoves = getQueenAttackSquaresIncludingBlocked(q, occupied) & ~ownPieces; // make sure to not attack own pieces
            possibleMoves &= captureMask | pushMask; // Apply the capture and push mask
            List<Long> sliderRaysToKing = getSliderRaysToSquare(kingPosition, bitboards[Constants.BISHOP | color^1],
                    bitboards[Constants.QUEEN | color^1], bitboards[Constants.ROOK | color^1], occupied & ~q);
            long pinningRay = getIntersectingRay(sliderRaysToKing, q);
            moves.addAll(convertBitboardToMoves(board, q, possibleMoves, opponentPieces, pinningRay, Constants.QUEEN, color));
        }

        return moves;
    }

    /**
     * Returns an iterable to be able to iterate over all the individual pieces in the piece bitboard.
     *
     * @param p the piece bitboard to get the individual pieces from
     * @return an iterable
     */
    private Iterable<Long> getIndividualPieces(long p) {
        List<Long> pieces = new ArrayList<>();
        while (p != 0) {
            pieces.add(Bitboard.leastSignificantBit(p));
            p = Bitboard.resetLeastSignificantBit(p);
        }
        return pieces;
    }

    /**
     * Converts a bitboard of moves to a list of moves. Checks for pins and only converts the legal moves.
     *
     * @param piece            a bitboard containing the moving piece
     * @param possibleMoves    a bitboard containing the places this piece can move to
     * @param legalMoves       a bitboard containing the rays of sliders to the king without the piece contained
     *                          in piece, also contains the sliders themselves. This is used to calculate pins.
     *                         In the case the piece we are calculating is the king itself, this variable can be used as
     *                         ~kingDangerSquares and it will still be correct. If it is 0, it is set to all 1 so that it
     *                         is ignored.
     * @param opponentPieces   a bitboard containing the opponent pieces to determine captures
     * @param pieceType        an integer containing the piecetype (Constants.KING/KNIGHT/etc)
     * @param color            an integer determining the color of the moving piece (Constants.BLACK/WHITE)
     * @return a list of moves
     */
    private List<Move> convertBitboardToMoves(Board b, long piece, long possibleMoves, long opponentPieces, long legalMoves, int pieceType, int color) {
        List<Move> moves = new ArrayList<>();
        if (legalMoves == 0) legalMoves = 0xffffffffffffffffL; // If legalMoves is 0, ignore it.
        int fromSquare = Long.numberOfLeadingZeros(piece) + 1;
        long possibleCaptures = possibleMoves & opponentPieces & legalMoves;
        long possibleQuietMoves = possibleMoves & ~opponentPieces & legalMoves;
        while (possibleCaptures != 0) {
            int toSquare = Long.numberOfLeadingZeros(Bitboard.leastSignificantBit(possibleCaptures)) + 1;
            possibleCaptures = Bitboard.resetLeastSignificantBit(possibleCaptures);

            moves.add(new Move(fromSquare, toSquare, pieceType | color, b.getPieceType(toSquare), 4)); // 4 because it is a capture
        }
        while (possibleQuietMoves != 0) {
            int toSquare = Long.numberOfLeadingZeros(Bitboard.leastSignificantBit(possibleQuietMoves)) + 1;
            possibleQuietMoves = Bitboard.resetLeastSignificantBit(possibleQuietMoves);

            moves.add(new Move(fromSquare, toSquare, pieceType | color, Constants.EMPTY, 0)); // quiet moves
        }
        return moves;
    }

    /**
     * Converts a bitboard of moves of a pawn to a list of moves. Checks for pins and only converts the legal moves.
     *
     * @param piece            a bitboard containing the moving piece
     * @param possibleMoves    a bitboard containing the places this piece can move to
     * @param legalMoves       a bitboard containing the rays of sliders to the king without the piece contained
     *                          in piece, also contains the sliders themselves. This is used to calculate pins.
     *                         In the case the piece we are calculating is the king itself, this variable can be used as
     *                         ~kingDangerSquares and it will still be correct. If it is 0, it is set to all 1 so that it
     *                         is ignored.
     * @param opponentPieces   a bitboard containing the opponent pieces to determine captures
     * @param color            an integer determining the color of the moving piece (Constants.BLACK/WHITE)
     * @param enPassent        whether this move is en en passent move
     * @param promotion        whether this move is a promotion
     * @param doublePush       whether this move is a double push
     * @return a list of moves
     */
    private List<Move> convertPawnBitboardToMoves(Board b, long piece, long possibleMoves, long legalMoves, long opponentPieces, int color, boolean enPassent, boolean promotion, boolean doublePush) {
        List<Move> moves = new ArrayList<>();
        int fromSquare = Long.numberOfLeadingZeros(piece) + 1; // 1 indexed
        if (legalMoves == 0) legalMoves = 0xffffffffffffffffL; // If legalMoves is 0, ignore it.
        long possibleCaptures = possibleMoves & opponentPieces & legalMoves;
        long possiblePushes = possibleMoves & ~opponentPieces & legalMoves;
        while (possibleCaptures != 0) {
            int toSquare = Long.numberOfLeadingZeros(Bitboard.leastSignificantBit(possibleCaptures)) + 1; // 1 indexed
            possibleCaptures = Bitboard.resetLeastSignificantBit(possibleCaptures);
            if (promotion) {
                moves.add(new Move(fromSquare, toSquare, Constants.PAWN | color, b.getPieceType(toSquare), 15)); //queen promo capture
                moves.add(new Move(fromSquare, toSquare, Constants.PAWN | color, b.getPieceType(toSquare), 14)); //rook promo capture
                moves.add(new Move(fromSquare, toSquare, Constants.PAWN | color, b.getPieceType(toSquare), 13)); //bishop promo capture
                moves.add(new Move(fromSquare, toSquare, Constants.PAWN | color, b.getPieceType(toSquare), 12)); //knight promo capture
            } else { //normal capture, no promotion
                moves.add(new Move(fromSquare, toSquare, Constants.PAWN | color, b.getPieceType(toSquare), 4));
            }
        }
        while (possiblePushes != 0) {
            int toSquare = Long.numberOfLeadingZeros(Bitboard.leastSignificantBit(possiblePushes)) + 1; // 1 indexed
            possiblePushes = Bitboard.resetLeastSignificantBit(possiblePushes);

            if (promotion) {
                moves.add(new Move(fromSquare, toSquare, Constants.PAWN | color, b.getPieceType(toSquare), 11)); //queen promo
                moves.add(new Move(fromSquare, toSquare, Constants.PAWN | color, b.getPieceType(toSquare), 10)); //rook promo
                moves.add(new Move(fromSquare, toSquare, Constants.PAWN | color, b.getPieceType(toSquare), 9)); //bishop promo
                moves.add(new Move(fromSquare, toSquare, Constants.PAWN | color, b.getPieceType(toSquare), 8)); //knight promo
            } else if (doublePush) {
                moves.add(new Move(fromSquare, toSquare, Constants.PAWN | color, b.getPieceType(toSquare), 1));
            } else if (enPassent) { // the move itself is a push, therefore it is not under captures but under possiblePushes
                int captureSquare = color == Constants.WHITE ? toSquare - 8 : toSquare + 8;
                moves.add(new Move(fromSquare, toSquare, Constants.PAWN | color, b.getPieceType(captureSquare), 5));
            } else {
                moves.add(new Move(fromSquare, toSquare, Constants.PAWN | color, b.getPieceType(toSquare), 0)); // normal pushes
            }
        }
        return moves;
    }

    /**
     * Finds whether or not the squares in targetSquares are attacked by a certain color pieces
     *
     * @param b             the board state
     * @param targetSquares the squares to check
     * @param color         the attacking color
     * @return if this square is attacked by pieces of the color @param color
     */
    public boolean isAttacked(Board b, long targetSquares, int color) {
        long remainingTargetSquares = targetSquares;
        long[] bitboards = b.getBitboards();
        long occupied = b.getOccupied();
        while (remainingTargetSquares != 0) {
            long targetSquare = Bitboard.leastSignificantBit(remainingTargetSquares);
            remainingTargetSquares = Bitboard.resetLeastSignificantBit(remainingTargetSquares);

            // if a white pawn is on a black pawn attack square, this square is attacked
            if ((bitboards[Constants.PAWN | color] & getPawnAttackSquares(targetSquare, color^1)) != 0) return true;
            if ((bitboards[Constants.KNIGHT | color] & getKnightAttackSquaresIncludingBlocked(targetSquare)) != 0)
                return true;
            if ((bitboards[Constants.KING | color] & getKingAttackSquaresIncludingBlocked(targetSquare)) != 0)
                return true;

            // Is this square attacked by a queen or rook along a file or rank?
            long slidingAttackers = bitboards[Constants.ROOK | color] | bitboards[Constants.QUEEN | color];
            if (slidingAttackers != 0) {
                if ((getAxisAlignedAttackSquaresIncludingBlocked(slidingAttackers, occupied) & targetSquare) != 0)
                    return true;
            }
            slidingAttackers = bitboards[Constants.BISHOP | color] | bitboards[Constants.QUEEN | color];
            if (slidingAttackers != 0) {
                if ((getDiagonalAttackSquaresIncludingBlocked(slidingAttackers, occupied) & targetSquare) != 0)
                    return true;
            }
        }
        return false;
    }

    /**
     * Gets all attack squares of one color. Includes pieces that are attacked/defended.
     *
     * @param bitboards, the bitboards array of all the pieces
     * @param occupied,  a bitboard containing all the occupied squares.
     * @param color,     one of Constants.WHITE/Constants.BLACK
     * @return a bitboard of all squares that are attacked by white attacked/defended pieces included.
     */
    public long getAttackedSquaresIncludingBlocked(long[] bitboards, long occupied, int color) {
        long attacks = getKingAttackSquaresIncludingBlocked(bitboards[Constants.KING | color]);
        attacks |= getPawnAttackSquares(bitboards[Constants.PAWN | color], color);
        attacks |= getAxisAlignedAttackSquaresIncludingBlocked(bitboards[Constants.ROOK | color], occupied);
        attacks |= getDiagonalAttackSquaresIncludingBlocked(bitboards[Constants.BISHOP | color], occupied);
        attacks |= getQueenAttackSquaresIncludingBlocked(bitboards[Constants.QUEEN | color], occupied);
        attacks |= getKnightAttackSquaresIncludingBlocked(bitboards[Constants.KNIGHT | color]);
        return attacks;
    }

    /**
     * Gets the squares that are diagonally attacked. It includes the blocker. See also @method getAxisAlignedAttackSquaresIncludingBlocked
     *
     * @param p,        the bitboard containing the pieces that are attacking
     * @param occupied, the bitboard containing the occupied squares
     * @return the squares that are diagonally attacked, including the blocker. Does both diagonals
     */
    private long getDiagonalAttackSquaresIncludingBlocked(long p, long occupied) {
        return getNormalDiagonalAttackSquaresIncludingBlocked(p, occupied) | getAntiDiagonalAttackSquaresIncludingBlocked(p, occupied);
    }

    private long getNormalDiagonalAttackSquaresIncludingBlocked(long p, long occupied) {
        long attacks = 0;
        while (p != 0) {
            long slider = Bitboard.leastSignificantBit(p); // Get the next piece (next least significant bit)
            p = Bitboard.resetLeastSignificantBit(p); // Set that next piece to 0 so we process it only once.
            int square = Long.numberOfLeadingZeros(slider); // Get the square this piece is on (minus 1,
            // but the index in the DIAGONAL_MASK array is 0 based anyway, so the arithmatic fits perfectly

            long lineMask = Constants.DIAGONAL_MASK[square]; //first do it in diagonal
            long forward = occupied & lineMask; // all potential blockers
            long reverse = Long.reverse(forward); // for the negative rays
            forward -= 2 * slider; // -2s
            reverse -= 2 * Long.reverse(slider); // for the negative rays the same
            // Another xor with o is omitted, because when done in both the positive and negative rays, they cancel each
            // other out (o^o = 0)
            forward ^= Long.reverse(reverse); // negative and positive rays xor'ed.
            attacks |= (forward & lineMask & ~slider); // mask again and add to the attack bitboard, also remove the piece itself
        }
        return attacks;
    }

    private long getAntiDiagonalAttackSquaresIncludingBlocked(long p, long occupied) {
        long attacks = 0;
        while (p != 0) {
            long slider = Bitboard.leastSignificantBit(p); // Get the next piece (next least significant bit)
            p = Bitboard.resetLeastSignificantBit(p); // Set that next piece to 0 so we process it only once.
            int square = Long.numberOfLeadingZeros(slider); // Get the square this piece is on (minus 1,
            // but the index in the DIAGONAL_MASK array is 0 based anyway, so the arithmetic fits perfectly

            long lineMask = Constants.ANTIDIAGONAL_MASK[square];
            long forward = occupied & lineMask;
            long reverse = Long.reverse(forward);
            forward -= 2 * slider;
            reverse -= 2 * Long.reverse(slider);
            forward ^= Long.reverse(reverse);
            attacks |= (forward & lineMask & ~slider);
        }
        return attacks;
    }

    /**
     * Calculates a bitboard of all attacks that are axis aligned. It includes the blocker of both colors.
     * Example of one row:
     * p = 001000, occupied = 011010, result will be 010110
     * This is calculated using o^(o-2s), where o is occupied and s is the slider
     *
     * @param p,        a bitboard containing the attacking pieces
     * @param occupied, a bitboard containing the occupied squares
     * @return a bitboard with all attack squares, including the potential blocker.
     */
    private long getAxisAlignedAttackSquaresIncludingBlocked(long p, long occupied) {
        return getVerticalAttackSquaresIncludingBlocked(p, occupied) | getHorizontalAttackSquaresIncludingBlocked(p, occupied);
    }

    private long getVerticalAttackSquaresIncludingBlocked(long p, long occupied) {
        long attacks = 0;
        while (p != 0) {
            long slider = Bitboard.leastSignificantBit(p); // Get the next piece (next least significant bit)
            p = Bitboard.resetLeastSignificantBit(p); // Set that next piece to 0 so we process it only once.
            int square = Long.numberOfLeadingZeros(slider); // Get the square this piece is on (minus 1,
            // but the index in the DIAGONAL_MASK array is 0 based anyway, so the arithmatic fits perfectly
            // file
            int x = square % 8;
            long lineMask = Constants.FILE_MASK[x]; //first do it vertical
            long forward = occupied & lineMask; // all potential blockers
            long reverse = Long.reverse(forward); // for the negative rays
            forward -= 2 * slider; // -2s
            reverse -= 2 * Long.reverse(slider); // for the negative rays the same
            // Another xor with o is omitted, because when done in both the positive and negative rays, they cancel each
            // other out (o^o = 0)
            forward ^= Long.reverse(reverse); // negative and positive rays xor'ed.
            attacks |= (forward & lineMask & ~slider); // mask again and add to the attack bitboard, also remove the piece itself
        }
        return attacks;
    }

    private long getHorizontalAttackSquaresIncludingBlocked(long p, long occupied) {
        long attacks = 0;
        while (p != 0) {
            long slider = Bitboard.leastSignificantBit(p); // Get the next piece
            p = Bitboard.resetLeastSignificantBit(p); // Set that next piece to 0 so we process it only once.
            int square = Long.numberOfLeadingZeros(slider); // Get the square this piece is on (minus 1,
            // but the index in the lineMask array is 0 based anyway, so the arithmatic fits perfectly
            // rank
            // same for horizontal
            int y = square / 8;
            long lineMask = Constants.RANK_MASK[y];
            long forward = occupied & lineMask;
            long reverse = Long.reverse(forward);
            forward -= 2 * slider;
            reverse -= 2 * Long.reverse(slider);
            forward ^= Long.reverse(reverse);
            attacks |= (forward & lineMask & ~slider);
        }
        return attacks;
    }

    /**
     * Calculates a bitboard of all queen attacks for all queens in bitboard p. Does not check for its own color pieces.
     *
     * @param p,        the queens to find the attacks for
     * @param occupied, a bitboard representing all the squares they can attack.
     * @return a bitboard with all the queen attacks, includes the blocker.
     */
    private long getQueenAttackSquaresIncludingBlocked(long p, long occupied) {
        return getAxisAlignedAttackSquaresIncludingBlocked(p, occupied) | getDiagonalAttackSquaresIncludingBlocked(p, occupied);
    }

    /**
     * Calculates a bitboard of all knight attacks for all knights in bitboard p. Does not check for its own color pieces
     *
     * @param p, the knights to find the attacks for
     * @return a bitboard representing all the squares they can jump to.
     */
    private long getKnightAttackSquaresIncludingBlocked(long p) {
        long attacks = 0;
        while (p != 0) {
            long knight = Bitboard.leastSignificantBit(p); // Get the next piece (next least significant bit)
            p = Bitboard.resetLeastSignificantBit(p); // Set that next piece to 0 so we process it only once.
            int square = Long.numberOfLeadingZeros(knight); // Get the square this piece is on (minus 1,
            // but the index in the DIAGONAL_MASK array is 0 based anyway, so the arithmatic fits perfectly

            attacks |= Constants.KNIGHT_ATTACKS[square];
        }
        return attacks;
    }

    /**
     * Calculates a bitboard of all king attacks for all kings in bitboard p. Does not check for its own color pieces
     *
     * @param p, the kings to find the attacks for
     * @return a bitboard representing all the squares they can attack
     */
    private long getKingAttackSquaresIncludingBlocked(long p) {
        long attacks = 0;
        while (p != 0) {
            long king = Bitboard.leastSignificantBit(p); // Get the next piece (next least significant bit)
            p = Bitboard.resetLeastSignificantBit(p); // Set that next piece to 0 so we process it only once.
            int square = Long.numberOfLeadingZeros(king); // Get the square this piece is on (minus 1,
            // but the index in the DIAGONAL_MASK array is 0 based anyway, so the arithmatic fits perfectly

            attacks |= Constants.KING_ATTACKS[square];
        }
        return attacks;
    }

    /**
     * This method finds all squares that are attacked by a pawn. It includes the attacked piece (even if it is of the same color)
     * ASSERTS that no pawns appear in the last row for that color
     *
     * @param p      the pawns to get the attack from
     * @param color, one of Constants.WHITE/Constants.BLACK
     * @return a bitboard representing the attacked squares
     */
    private long getPawnAttackSquares(long p, int color) {
        long attacked = 0;
        if (color == Constants.WHITE) {
            attacked |= Bitboard.noEaOne(p);
            attacked |= Bitboard.noWeOne(p);
        } else if (color == Constants.BLACK) {
            attacked |= Bitboard.soEaOne(p);
            attacked |= Bitboard.soWeOne(p);
        }
        return attacked;
    }

    /**
     * When having a lot of rays, return the ray that this square is on.
     * @param rays the rays to check
     * @param square the square to get the corresponding ray from
     * @return the ray in rays that contains square. 0 if none contains it
     */
    private long getIntersectingRay(List<Long> rays, long square) {
        for (long r : rays) {
            if ((r & square) != 0) {
                return r;
            }
        }
        return 0;
    }

    /**
     * Combines all the rays that will reach this square into one bitboard. The attackers themselves are included
     */
    private long getCombinedSliderRaysToSquare(long square, long bishops, long queens, long rooks, long occupied) {
        List<Long> rays = getSliderRaysToSquare(square, bishops, queens, rooks, occupied);
        long total = 0L;
        for (long r : rays) {
            total |= r;
        }
        return total;
    }

    /**
     * Gives a list of all the rays that will reach this square. Attackers themselves included.
     */
    private List<Long> getSliderRaysToSquare(long square, long bishops, long queens, long rooks, long occupied) {
        List<Long> combine = new ArrayList<>();
        combine.addAll(getDiagonalRayToSquare(square, queens, bishops, occupied));
        combine.addAll(getAxisAlignedRayToSquare(square, queens, rooks, occupied));
        return combine;
    }

    /**
     * @param square  The square to get the rays to
     * @param queens  All the opposite queens that could potentially have a ray to this square
     * @param bishops All opposite bishops that could potentially have a ray to this square
     * @return an list of bitboards containing all the rays of the queens in @param queens and the bishops in @param bishops
     * that would attack @param square if no other piece was on the board.
     */
    private List<Long> getDiagonalRayToSquare(long square, long queens, long bishops, long occupied) {
        List<Long> totalRays = new ArrayList<>();
        long diagRays = getNormalDiagonalAttackSquaresIncludingBlocked(square, occupied);
        long diagonalAttackers = (getNormalDiagonalAttackSquares(bishops, occupied) | bishops) |
                (getNormalDiagonalAttackSquares(queens, occupied) | queens); // includes the pieces themselves
        long diagNormalRayToSquare = diagRays & diagonalAttackers;
        if (diagNormalRayToSquare != 0) {
            long[] splittedNormal = Bitboard.splitRay(diagNormalRayToSquare | square, square);
            totalRays.add(splittedNormal[0]);
            totalRays.add(splittedNormal[1]);
        }
        long antiDiagRays = getAntiDiagonalAttackSquaresIncludingBlocked(square, occupied);
        long antiDiagonalAttackers = (getAntiDiagonalAttackSquares(bishops, occupied) | bishops) |
                (getAntiDiagonalAttackSquares(queens, occupied) | queens);
        long diagAntiRayToSquare = antiDiagRays & antiDiagonalAttackers;
        if (diagAntiRayToSquare != 0) {
            long[] splittedAnti = Bitboard.splitRay(diagAntiRayToSquare | square, square);
            totalRays.add(splittedAnti[0]);
            totalRays.add(splittedAnti[1]);
        }
        return totalRays;
    }

    /**
     * @param square The square to get the rays to
     * @param queens All the opposite queens that could potentially have a ray to this square
     * @param rooks  All opposite bishops that could potentially have a ray to this square
     * @return a list of bitboards containing all the rays of the queens in @param queens and the rooks in @param rooks
     * that would attack @param square if no other piece was on the board.
     */
    private List<Long> getAxisAlignedRayToSquare(long square, long queens, long rooks, long occupied) {
        List<Long> totalRays = new ArrayList<>();
        long horizontalRays = getHorizontalAttackSquaresIncludingBlocked(square, occupied);
        long horizontalAttackers = (getHorizontalAttackSquares(rooks, occupied) | rooks) |
                (getHorizontalAttackSquares(queens, occupied) | queens);
        long horizontalRaysToSquare = horizontalRays & horizontalAttackers;
        if (horizontalRaysToSquare != 0) {
            long[] splittedHorizontal = Bitboard.splitRay(horizontalRaysToSquare | square, square);
            totalRays.add(splittedHorizontal[0]);
            totalRays.add(splittedHorizontal[1]);
        }
        long verticalRays = getVerticalAttackSquaresIncludingBlocked(square, occupied);
        long verticalAttackers = (getVerticalAttackSquares(rooks, occupied) | rooks) |
                (getVerticalAttackSquares(queens, occupied) | queens);
        long verticalRaysToSquare = verticalRays & verticalAttackers;
        if (verticalRaysToSquare != 0){
            long[] splittedVertical = Bitboard.splitRay(verticalRaysToSquare | square, square);
            totalRays.add(splittedVertical[0]);
            totalRays.add(splittedVertical[1]);
        }
        return totalRays;
    }

    /**
     * This method finds all squares that are attacked in an axis alligned way by the pieces in bitboard p
     *
     * @param p,        the pieces to find the attack for
     * @param occupied, a bitboard with the occupied squares
     * @return a bitboard containing all the squares that are attacked in an axis aligned way, does not include the blocker.
     */
    private long getAxisAlignedAttackSquares(long p, long occupied) {
        return getHorizontalAttackSquares(p, occupied) | getVerticalAttackSquares(p, occupied);
    }

    private long getHorizontalAttackSquares(long p, long occupied) {
        long eastAttacks = p;
        long empty = ~occupied & Constants.NOT_A_FILE; // make A-File all occupied, to consider H-A-wraps after shift
        eastAttacks |= empty & (eastAttacks >>> 1); // 1. fill
        eastAttacks |= empty & (eastAttacks >>> 1); // 2. fill
        eastAttacks |= empty & (eastAttacks >>> 1); // 3. fill
        eastAttacks |= empty & (eastAttacks >>> 1); // 4. fill
        eastAttacks |= empty & (eastAttacks >>> 1); // 5. fill
        eastAttacks |= empty & (eastAttacks >>> 1); // 6. fill
        eastAttacks |= empty & (eastAttacks >>> 1); // 7. fill
        long westAttacks = p;
        empty = ~occupied & Constants.NOT_H_FILE;
        westAttacks |= empty & (westAttacks << 1);
        westAttacks |= empty & (westAttacks << 1);
        westAttacks |= empty & (westAttacks << 1);
        westAttacks |= empty & (westAttacks << 1);
        westAttacks |= empty & (westAttacks << 1);
        westAttacks |= empty & (westAttacks << 1);
        westAttacks |= empty & (westAttacks << 1);
        // the pieces are removed at the end
        return (eastAttacks | westAttacks) & ~p;
    }

    private long getVerticalAttackSquares(long p, long occupied) {
        long northAttacks = p;
        long empty = ~occupied;
        northAttacks |= empty & (northAttacks >>> 8); // should be unsigned shift, because otherwise the first row could become all 1
        northAttacks |= empty & (northAttacks >>> 8);
        northAttacks |= empty & (northAttacks >>> 8);
        northAttacks |= empty & (northAttacks >>> 8);
        northAttacks |= empty & (northAttacks >>> 8);
        northAttacks |= empty & (northAttacks >>> 8);
        northAttacks |= empty & (northAttacks >>> 8);
        long southAttacks = p;
        southAttacks |= empty & (southAttacks << 8);
        southAttacks |= empty & (southAttacks << 8);
        southAttacks |= empty & (southAttacks << 8);
        southAttacks |= empty & (southAttacks << 8);
        southAttacks |= empty & (southAttacks << 8);
        southAttacks |= empty & (southAttacks << 8);
        southAttacks |= empty & (southAttacks << 8);
        // the pieces are removed at the end
        return (northAttacks | southAttacks) & ~p;
    }

    private long getDiagonalAttackSquares(long p, long occupied) {
        return getNormalDiagonalAttackSquares(p, occupied) | getAntiDiagonalAttackSquares(p, occupied);
    }

    private long getNormalDiagonalAttackSquares(long p, long occupied) {
        long empty = ~occupied & Constants.NOT_H_FILE; // make A-File all occupied, to consider H-A-wraps after shift
        long soEaAttacks = p;
        soEaAttacks |= empty & (soEaAttacks << 9);
        soEaAttacks |= empty & (soEaAttacks << 9);
        soEaAttacks |= empty & (soEaAttacks << 9);
        soEaAttacks |= empty & (soEaAttacks << 9);
        soEaAttacks |= empty & (soEaAttacks << 9);
        soEaAttacks |= empty & (soEaAttacks << 9);
        soEaAttacks |= empty & (soEaAttacks << 9);
        long noWeAttacks = p;
        empty = ~occupied & Constants.NOT_A_FILE;
        noWeAttacks |= empty & (noWeAttacks >>> 9);
        noWeAttacks |= empty & (noWeAttacks >>> 9);
        noWeAttacks |= empty & (noWeAttacks >>> 9);
        noWeAttacks |= empty & (noWeAttacks >>> 9);
        noWeAttacks |= empty & (noWeAttacks >>> 9);
        noWeAttacks |= empty & (noWeAttacks >>> 9);
        noWeAttacks |= empty & (noWeAttacks >>> 9);

        return (noWeAttacks | soEaAttacks) & ~p;
    }

    private long getAntiDiagonalAttackSquares(long p, long occupied) {
        long noEaAttacks = p;
        long empty = ~occupied & Constants.NOT_H_FILE; // make A-File all occupied, to consider H-A-wraps after shift
        noEaAttacks |= empty & (noEaAttacks >>> 7);
        noEaAttacks |= empty & (noEaAttacks >>> 7);
        noEaAttacks |= empty & (noEaAttacks >>> 7);
        noEaAttacks |= empty & (noEaAttacks >>> 7);
        noEaAttacks |= empty & (noEaAttacks >>> 7);
        noEaAttacks |= empty & (noEaAttacks >>> 7);
        noEaAttacks |= empty & (noEaAttacks >>> 7);
        long soWeAttacks = p;
        empty = ~occupied & Constants.NOT_A_FILE;
        soWeAttacks |= empty & (soWeAttacks << 7);
        soWeAttacks |= empty & (soWeAttacks << 7);
        soWeAttacks |= empty & (soWeAttacks << 7);
        soWeAttacks |= empty & (soWeAttacks << 7);
        soWeAttacks |= empty & (soWeAttacks << 7);
        soWeAttacks |= empty & (soWeAttacks << 7);
        soWeAttacks |= empty & (soWeAttacks << 7);
        return (soWeAttacks | noEaAttacks) & ~p;
    }

}
