package moveGenerator.Perft;

import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

public class PerftHelper {

    public void run() {
        Scanner scanner = new Scanner(System.in);
        HashMap<String, Integer> first = new HashMap<>();
        HashMap<String, Integer> second = new HashMap<>();
        boolean firstMap = true;

        int extra = 1; // extra number in case there is a promotion so the keys are different

        while (scanner.hasNext()) {
            String key = scanner.next();
            if (key.equals("/")) {
                if (firstMap) {
                    firstMap = false;
                    extra = 1; // to get the same keys
                    continue;
                } else {
                    break;
                }
            }
            HashMap<String, Integer> toPut;
            if (firstMap) {
                toPut = first;
            } else {
                toPut = second;
            }
            if (toPut.keySet().contains(key)) {
                key = key + extra;
                extra++;
            }
            int i = scanner.nextInt();
            toPut.put(key, i);
        }
        compare(first, second);
    }

    public void compare(HashMap<String, Integer> first, HashMap<String, Integer> second) {
        boolean nothing = true;
        if (first.keySet().size() != second.keySet().size()) { // not equal in size
            nothing = false;
            Set<String> keys = first.keySet();
            keys.removeAll(second.keySet());
            System.out.println("In first but not in second: " + keys);
            keys = second.keySet();
            keys.removeAll(first.keySet());
            System.out.println("In second but not in first: " + keys);
        }

        Set<String> equalKeys = first.keySet();
        equalKeys.retainAll(second.keySet());
        for (String s : equalKeys) {
            int firstInt = first.get(s);
            int secondInt = second.get(s);
            if (firstInt != secondInt) {
                nothing = false;
                System.out.println(s + " first: " + firstInt + " second: " + secondInt);
            }
        }
        if (nothing) {
            System.out.println("No differences");
        }
    }

    public static void main(String[] a) {
        new PerftHelper().run();
    }
}
