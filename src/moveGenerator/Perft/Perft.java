package moveGenerator.Perft;

import moveGenerator.LegalGenerator;
import state.Board;
import state.Move;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * A performance evaluator for the generator
 */
public class Perft {

    static int divideDepth = 6;

    private static long divide(Board b, LegalGenerator g, int depth) {
        List<Move> possibleMoves = b.getMoves();
        long nodes = 0; // the amount of nodes

        if (depth == 1) {
            nodes = possibleMoves.size();
            if (divideDepth == 2) {
                System.out.print(b.moves.peek() + " ");
                System.out.println(nodes);
            }
            return nodes; // base case
        }

        for (Move m : possibleMoves) {
            b.makeMove(m);
            nodes += divide(b, g, depth - 1);
            b.unMakeMove(m);
        }
        if (depth == divideDepth - 1) {
            System.out.print(b.moves.peek() + " ");
            System.out.println(nodes);
        }
        return nodes;
    }


    private static long perft(Board b, LegalGenerator g, int depth) {
        List<Move> possibleMoves = b.getMoves();
        long nodes = 0; // the amount of nodes

        if (depth == 1) {
            nodes = possibleMoves.size();
            return nodes; // base case
        }

        for (Move m : possibleMoves) {
            b.makeMove(m);
            nodes += perft(b, g, depth - 1);
            b.unMakeMove(m);
        }
        return nodes;
    }

    public static void readPerftSuite() {
        long totalStart = System.currentTimeMillis();
        Scanner s = null;
        try {
            s = new Scanner((new Perft()).getClass().getClassLoader().getResource("perft/perftsuite.txt").openStream());
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            System.exit(0); // stop if the file is not found
        }

        boolean allCorrect = true;

        LegalGenerator g = new LegalGenerator();

        while (s.hasNextLine()) {
            String nextLine = s.nextLine();
            if (nextLine.trim().equals("") || nextLine.substring(0, 2).trim().equals("//")) { // if it starts with //, skip this line
                continue;
            }
            String[] parts = nextLine.split(";");
            for (String p : parts) {
                p = p.trim(); // trim the spaces
            }

            Board b = new Board(parts[0]); // the first part is always the fen
            for (int i = 1; i < parts.length; i++) {
                String current = parts[i].trim();
                int depth = Integer.parseInt(current.split(" ")[0].substring(1, 2));

                long expectedNodeCount = Long.parseLong(current.split(" ")[1]);

                long start = System.currentTimeMillis();
                long actualNodeCount = 0;
                try {
                    actualNodeCount = perft(b, g, depth);
                } catch (Exception e) {
                    System.out.println(parts[0] + " D" + depth + " expected: " + expectedNodeCount + " failed");
                    Arrays.stream(e.getStackTrace()).forEach(System.out::println);
                    continue;
                }
                long duration = System.currentTimeMillis() - start;

                if (expectedNodeCount != actualNodeCount) { // if the value is not correct
                    allCorrect = false;
                    System.out.println(parts[0] + " D" + depth + " expected: " + expectedNodeCount + " actual: " + actualNodeCount);
                } else {
                    System.out.println(parts[0] + " D" + depth + " done in " + duration + " miliseconds");
                }
            }
        }
        if (allCorrect) {
            System.out.println("all correct");
        }
        long totalDuration = System.currentTimeMillis() - totalStart;
        System.out.println("Total time: " + totalDuration);
    }

    public static void main(String[] a) {
        readPerftSuite();
    }
}
