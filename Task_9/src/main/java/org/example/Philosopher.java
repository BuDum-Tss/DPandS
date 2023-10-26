package org.example;

import java.util.concurrent.ThreadLocalRandom;
public abstract class Philosopher implements Runnable {

  protected final String name;
  protected final Fork leftFork;
  protected final Fork rightFork;
  protected Philosopher(String name,Fork leftFork, Fork rightFork) {
    this.name = name;
    this.leftFork=leftFork;
    this.rightFork=rightFork;
  }
  protected void think(){
    System.out.println("The philosopher " + name + " is thinking...");
    randomSleep();
    System.out.println("The philosopher " + name + " finished thinking!");
  }
  protected void eat(Fork leftFork, Fork rightFork){
    System.out.println("The philosopher " + name + " is eating using " + leftFork.toString() + " and " + rightFork.toString());
    randomSleep();
    System.out.println("The philosopher " + name + " finished eating!");
  }
  private void randomSleep() {
    long millis = ThreadLocalRandom.current().nextLong(1000, 6000);
    try {
      Thread.sleep(millis);
    } catch (InterruptedException exception) {
      exception.printStackTrace();
    }
  }
}
