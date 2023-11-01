package org.example;

import java.util.concurrent.TimeUnit;

public class Main {

  public static void main(String[] args) {
    Thread thread = new Thread(()->{
      while (!Thread.interrupted()){
        System.out.println("This is text.");
      }
    });
    thread.start();
    try {
      Thread.sleep(TimeUnit.SECONDS.toMillis(2));
    } catch (InterruptedException e) {
      System.out.println("Thread has been interrupted");
    }
    thread.interrupt();
    System.out.println("Thread is interrupted");
  }
}