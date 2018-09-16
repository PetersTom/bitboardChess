package tests;

import moveGenerator.LegalGenerator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorTest {

    @Test
    void axisAlignedFloodFillAttack() {
        LegalGenerator g = new LegalGenerator();
        Method toTest = null;
        try {
            toTest = g.getClass().getDeclaredMethod("getAxisAlignedAttackSquares", long.class, long.class);
            toTest.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        assert toTest != null;
        long pieces = 0x0000002000000000L; // c4
        try {
            assertEquals(0x202020ff20202020L & ~pieces, toTest.invoke(g, pieces, pieces));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        long occupied = 0x0000302200000000L; // c4, c3, d3, g4
        try {
            assertEquals(0x000000fc20202020L & ~pieces, toTest.invoke(g, pieces, occupied));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        pieces |= 0x0000000008000000L; // add rook on e5 to test two rooks
        try {
            assertEquals(0x080808fcff282828L & ~pieces, toTest.invoke(g, pieces, occupied));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Test
    void diagonalFloodFillAttack() {
        LegalGenerator g = new LegalGenerator();
        Method toTest = null;
        try {
            toTest = g.getClass().getDeclaredMethod("getDiagonalAttackSquares", long.class, long.class);
            toTest.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        assert toTest != null;
        long pieces = 0x0000002000000000L; // c4
        try {
            assertEquals(0x0488500050880402L, toTest.invoke(g, pieces, pieces));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        long occupied = 0x0000302200000000L; // c4, c3, d3, g4
        try {
            assertEquals(0x0080400050880402L & ~pieces, toTest.invoke(g, pieces, occupied));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Test
    void diagonalAttacksIncludingBlocker() {
        LegalGenerator g = new LegalGenerator();
        Method toTest = null;
        try {
            toTest = g.getClass().getDeclaredMethod("getDiagonalAttackSquaresIncludingBlocked",
                    long.class, long.class);
            toTest.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        assert toTest != null;
        long pieces = 0x0000002000000000L; // c4
        try {
            assertEquals(0x0488500050880402L, toTest.invoke(g, pieces, pieces));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        long occupied = 0x0000302200000000L; // c4, c3, d3, g4
        try {
            assertEquals(0x0080500050880402L, toTest.invoke(g, pieces, occupied));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Test
    void axisAlignedAttacksIncludingBlocker() {
        LegalGenerator g = new LegalGenerator();
        Method toTest = null;
        try {
            toTest = g.getClass().getDeclaredMethod("getAxisAlignedAttackSquaresIncludingBlocked", long.class, long.class);
            toTest.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        assert toTest != null;

        long pieces = 0x0000002000000000L; // c4
        try {
            assertEquals(0x202020ff20202020L & ~pieces, toTest.invoke(g, pieces, pieces));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        long occupied = 0x0000302200000000L; // c4, c3, d3, g4
        try {
            assertEquals(0x000020de20202020L & ~pieces, toTest.invoke(g, pieces, occupied));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        pieces |= 0x0000000008000000L; // add rook on e5 to test two rooks
        try {
            assertEquals(0x080828def7282828L & ~pieces, toTest.invoke(g, pieces, occupied));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Test
    void raysToSquare() {
        LegalGenerator g = new LegalGenerator();
        Method toTest = null;
        try {
            toTest = g.getClass().getDeclaredMethod("getSliderRaysToSquare", long.class, long.class, long.class, long.class, long.class);
            toTest.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        assert toTest != null;

        long square = 0x0000100000000000L; //d3
        long bishops = 0x0000000020020000L; //c5 and g6
        long queens = 0x0000000008004000L; //b7 and e5
        long rooks = 0x0200400000000000L; //b3 and g1
        long occupied = 0x020050002c024000L; // also a pawn on f5
        try {
            List<Long> actual = (List<Long>)toTest.invoke(g, square, bishops, queens, rooks, occupied);
            assertArrayEquals(new Long[]{0L, 0x0000600000000000L}, actual.toArray(new Long[actual.size()]));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}