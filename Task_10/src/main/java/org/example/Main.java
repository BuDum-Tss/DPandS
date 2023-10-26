package org.example;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

  public static void main(String[] args) {
    Lock lock1 = new ReentrantLock();
    Lock lock2 = new ReentrantLock();
    lock2.lock();
    Runnable task = () -> {

      for (int i = 0; i < 10; i++) {
        lock2.lock();
        lock1.lock();
        lock2.unlock();
        System.out.println("Task " + i + " in 2nd thread");
        lock1.unlock();
      }
    };
    Thread thread = new Thread(task);
    thread.start();
    for (int i = 0; i < 10; i++) {
      lock1.lock();
      lock2.unlock();
      System.out.println("Task " + i + " in main thread");
      lock1.unlock();
      lock2.lock();
    }
    lock2.unlock();
  }
}