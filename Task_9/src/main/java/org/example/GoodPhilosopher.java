package org.example;

public class GoodPhilosopher extends Philosopher{

  public GoodPhilosopher(String name, Fork leftFork, Fork rightFork) {
    super(name, leftFork, rightFork);
  }

  @Override
  public void run() {
    while (true) {
      think();
      System.out.println("The philosopher " + name + " tries take left fork...");
      if (leftFork.tryPickUp()) {
        System.out.println("The philosopher " + name + " tries take right fork...");
        if (rightFork.tryPickUp()) {
          eat(leftFork, rightFork);
          rightFork.putDown();
        }
        leftFork.putDown();
      }
    }
  }
}
