package org.example;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class VeryGoodPhilosopher extends Philosopher{

  private final ReentrantLock forks;
  private Condition condition;
  public VeryGoodPhilosopher(String name, Fork leftFork, Fork rightFork, ReentrantLock forks, Condition condition) {
    super(name, leftFork, rightFork);
    this.forks = forks;
    this.condition = condition;
  }

  @Override
  public void run() {
    while (true) {
      think();
      forks.lock();
      try {
        System.out.println("The philosopher " + name + " tries take forks...");
        while(!leftFork.tryPickUp() || !rightFork.tryPickUp()) {
          if (leftFork.isHeldByCurrentThread()) {
            leftFork.putDown();
          }
          if (rightFork.isHeldByCurrentThread()) {
            rightFork.putDown();
          }
          System.out.println("The philosopher " + name + " waits notification...");
          condition.await();
          System.out.println("The philosopher " + name + " tries take forks...");
        }
      } catch (InterruptedException exception) {
        exception.printStackTrace();
      } finally {
        forks.unlock();
      }
      System.out.println("The philosopher " + name + " took forks");
      eat(leftFork, rightFork);
      forks.lock();
      System.out.println("The philosopher " + name + " puts forks");
      rightFork.putDown();
      leftFork.putDown();
      condition.signalAll();
      forks.unlock();
    }
  }
}
