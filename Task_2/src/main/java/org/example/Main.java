package org.example;

public class Main {

  public static void main(String[] args) {
    Runnable task = () -> {
      for (int i = 0; i < 10; i++) {
        System.out.println("Task " + i + " in 2nd thread");

      }
    };
    Thread thread = new Thread(task);
    thread.start();
    try {
      thread.join();
    } catch (InterruptedException exception) {
      System.out.println("Created thread has been interrupted");
    }
    for (int i = 0; i < 10; i++) {
      System.out.println("Task " + i + " in main thread");
    }
  }
}