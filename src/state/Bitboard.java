package state;

public class Bitboard {

    /**
     * Converts a long to its binary representation and prints it in 8 rows of 8 starting at the bottom.
     * Used to see a bitboard.
     * @param x the long to convert.
     * @return A string in the aforementioned format.
     */
    public static String bitboardString(long x) {
        String b = String.format("%64s", Long.toBinaryString(x)).replace(' ', '0');
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            s.append(b.substring((7 - i) * 8, (7 - i) * 8 + 8)); //(7-)move from back to beginning to show it as whites perspective
            s.append('\n');
        }
        return s.toString();
    }

    /**
     * Moves the bitboard one to the south
     * @param l the bitboard to move
     * @return the shifted bitboard
     */
    public static long soutOne(long l) {
        return l << 8;
    }

    /**
     * Moves teh bitboard one to the north
     * @param l the bitboard to move
     * @return the shifted bitboard
     */
    public static long nortOne(long l) {
        return l >>> 8;
    }

    /**
     * Moves the bitboard one to the east.
     * @param l the bitboard to move
     * @return the shifted bitboard
     */
    public static long eastOne(long l) {
        return (l >>> 1) & Constants.NOT_A_FILE; // Done to avoid showing up on the next row
    }

    /**
     * Moves the bitboard one to the north east
     * @param l the bitboard to move
     * @return the shifted bitboard
     */
    public static long noEaOne(long l) {
        return (l >>> 9) & Constants.NOT_A_FILE;
    }

    /**
     * Moves the bitboard one to the south east
     * @param l the bitboard to move
     * @return the shifted bitboard
     */
    public static long soEaOne(long l) {
        return (l << 7) & Constants.NOT_A_FILE;
    }

    /**
     * Moves the bitboard one to the west
     * @param l the bitboard to move
     * @return the shifted bitboard
     */
    public static long westOne(long l) {
        return (l << 1) & Constants.NOT_H_FILE;
    }

    /**
     * Moves the bitboard one to the south west
     * @param l the bitboard to move
     * @return the shifted bitboard
     */
    public static long soWeOne(long l) {
        return (l << 9) & Constants.NOT_H_FILE;
    }

    /**
     * Moves the bitboard one to the north west
     * @param l the bitboard to move
     * @return the shifted bitboard
     */
    public static long noWeOne(long l) {
        return (l >>> 7) & Constants.NOT_H_FILE;
    }

    /**
     * Finds the least significant bit in a bitboard (upper rightmost one)
     * @param l the bitboard to check
     * @return a bitboard representing the least significant bit
     */
    public static long leastSignificantBit(long l) {
        return l & -l;
    }

    /**
     * Splits a ray into two different rays. The split should be lying on the ray and will not be included in either one
     * of the output rays.
     * @param ray the ray to split
     * @param split the splitting point. Should lie on the ray
     * @return an array of length 2 that contains the splitted rays
     */
    public static long[] splitRay(long ray, long split) {
        if (ray == 0) {
            throw new IllegalArgumentException("there is no ray");
        }
        if ((ray & split) == 0) {
            throw new IllegalArgumentException("split does not lie on ray");
        }
        long first = 0L;
        long second = 0L;
        boolean splitReached = false;
        while (ray != 0) {
            long square = leastSignificantBit(ray);
            ray = resetLeastSignificantBit(ray);
            if (square == split) {
                splitReached = true;
                continue; // do not add this to either ray
            }

            if (!splitReached) {
                first |= square;
            } else {
                second |= square;
            }
        }
        return new long[]{first, second};
    }

    /**
     * Sets the least significant bit in a bitboard (upper rightmost one) to 0
     * @param l the bitboard to change
     * @return a bitboard representation with the least significant bit set to 0
     */
    public static long resetLeastSignificantBit(long l) {
        return l & (l-1);
    }

    /**
     * Finds the amount of one's in the bitboard
     * @param l, the bitboard to check
     * @return the amount of one's in the bitboard
     */
    public static int getPopCount(long l) {
        int amount = 0;
        while (l != 0) {
            l = resetLeastSignificantBit(l);
            amount++;
        }
        return amount;
    }
}
