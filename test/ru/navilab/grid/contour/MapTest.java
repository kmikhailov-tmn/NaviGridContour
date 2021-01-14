package ru.navilab.grid.contour;

import java.util.Hashtable;

/**
 * Created by: Mikhailov_KG
 * Date: 24.07.2020
 */
public class MapTest {
    static class A {
        int z;
        int k;
    }

    private static long test(int a, int b) {
        return ((long) a << 32) + b;
    }

    private static long test2(int a, int b) {
        return ((long) a << 32) | b;
    }

    public static void main(String[] args) {
        Hashtable<Long, A> hashtable = new Hashtable<>();
        int maxValue = 2000;
        for (int i = 1500; i < maxValue; i++) {
            for (int j = 1500; j < maxValue; j++) {
                hashtable.put(test(i, j), new A());
//                long l1 = test(i, j);
//                long l2 = test2(i, j);
//                if (l1 != l2) {
//                    System.out.println(i + " " + j);
//                }
            }
        }
        System.err.println(hashtable.values().size());
    }

}
