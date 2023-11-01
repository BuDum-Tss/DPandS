package org.example;

public class BadPhilosopher extends Philosopher {

  public BadPhilosopher(String name, Fork leftFork, Fork rightFork) {
    super(name, leftFork, rightFork);
  }
  @Override
  public void run() {
    while (true) {
      think();
      System.out.println("The philosopher " + name + " waits left fork...");
      leftFork.pickUp();
      System.out.println("The philosopher " + name + " waits right fork...");
      rightFork.pickUp();
      eat(leftFork, rightFork);
      rightFork.putDown();
      leftFork.putDown();
    }
  }
}
