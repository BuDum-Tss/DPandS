package org.example;

import java.util.concurrent.Semaphore;

public class Main {

  public static void main(String[] args) {
    Semaphore semaphore1 = new Semaphore(1);
    Semaphore semaphore2 = new Semaphore(1);
    Runnable task = () -> {
      for (int i = 0; i < 10; i++) {
        try {
          semaphore2.acquire();
          semaphore1.acquire();
          semaphore2.release();
          System.out.println("Task " + i + " in 2nd thread");
          semaphore1.release();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

      }
    };
    Thread thread = new Thread(task);
    thread.start();
    for (int i = 0; i < 10; i++) {
      try {
        semaphore2.acquire();
        semaphore1.acquire();
        semaphore2.release();
        System.out.println("Task " + i + " in main thread");
        semaphore1.release();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}