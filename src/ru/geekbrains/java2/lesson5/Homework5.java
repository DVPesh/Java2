package ru.geekbrains.java2.lesson5;

import java.util.Arrays;

public class Homework5 {

    private static final int ARRAY_SIZE = 10_000_000;

    public static void main(String[] args) {
        float[] array1 = calculateInOneThread();
        float[] array2 = calculateInTwoThreads();

        System.out.println(Arrays.equals(array1, array2)); //проверка что массивы одинаковые
    }

    private static float[] createArrayWithOnes() {
        float[] array = new float[ARRAY_SIZE];
        for (int i = 0; i < ARRAY_SIZE; i++) {
            array[i] = 1f;
        }
        return array;
    }

    private static float[] calculateInOneThread() {
        float[] array = createArrayWithOnes();
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < array.length; i++) {
            array[i] = (float) (array[i] * calculateFormula(i));
        }
        System.out.printf("Время обработки одним потоком: %d мс%n", (System.currentTimeMillis() - startTime));
        return array;
    }

    private static float[] calculateInTwoThreads() {
        float[] array = createArrayWithOnes();
        long startTime = System.currentTimeMillis();

        float[] array1 = new float[array.length / 2];
        float[] array2 = new float[array.length - array1.length];

        System.arraycopy(array, 0, array1, 0, array1.length);
        System.arraycopy(array, array1.length, array2, 0, array2.length);

        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < array1.length; i++) {
                array1[i] = (float) (array1[i] * calculateFormula(i));
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < array2.length; i++) {
                array2[i] = (float) (array2[i] * calculateFormula(array1.length + i));
            }
        });

        thread1.start();
        thread2.start();
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            System.out.println(e);
            return null;
        }
        System.arraycopy(array1, 0, array, 0, array1.length);
        System.arraycopy(array2, 0, array, array1.length, array2.length);
        System.out.printf("Время обработки двумя потоками: %d мс%n", (System.currentTimeMillis() - startTime));
        return array;
    }

    private static double calculateFormula(int value) {
        return Math.sin(0.2f + value / 5) * Math.cos(0.2f + value / 5) * Math.cos(0.4f + value / 2);
    }

}
