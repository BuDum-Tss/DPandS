package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

  public static void main(String args[]) {
    ReentrantLock forks = new ReentrantLock();
    Condition condition = forks.newCondition();
    Fork[] forksArray = new Fork[5];
    for (int i = 0; i < 5; ++i) {
      forksArray[i] = new Fork(i);
    }
    String[] names = new String[]{"Socrates", "Plato", "Aristotle", "Thales", "Pythagoras"};
    List<Philosopher> philosophers = new ArrayList<>();
    for (int i = 0; i < 5; ++i) {
      philosophers.add(new VeryGoodPhilosopher(names[i], forksArray[i], i == 4 ? forksArray[0] : forksArray[i + 1], forks, condition));
    }
    philosophers.forEach(philosopher -> new Thread(philosopher).start());
  }
}