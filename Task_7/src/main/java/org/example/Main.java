package org.example;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("Usage: java PiCalculation <iterations> <threads>");
      System.exit(1);
    }

    int iterations = Integer.parseInt(args[0]);
    int threads = Integer.parseInt(args[1]);

    ExecutorService executor = Executors.newFixedThreadPool(threads);
    Double pi = 0.0;

    for (int i = 0; i < threads; i++) {
      int finalI = i;
      try {
        pi += executor.submit(() -> calculatePi(finalI, threads, iterations)).get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    }
    executor.shutdown();

    try {
      executor.awaitTermination(1, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    pi = pi * 4;
    System.out.println("Result: " + pi);
  }

  private static double calculatePi(int firstNumber, int divider, int maxIterationNumber) {
    double answer = 0;
    for (int i = firstNumber; i < maxIterationNumber; i += divider) {
      answer += getLeibnizSeriesElement(i);
    }
    return answer;
  }

  private static double getLeibnizSeriesElement(int n) {
    return Math.pow(-1, n) / (2 * n + 1);
  }
}