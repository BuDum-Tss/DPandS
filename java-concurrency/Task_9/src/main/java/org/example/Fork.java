package org.example;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Fork {
  private int id;
  private Lock lock;

  public Fork(int id) {
    this.id = id;
    this.lock = new ReentrantLock();
  }
  public boolean tryPickUp() {
    return lock.tryLock();
  }
  public void pickUp() {
    lock.lock();
  }

  public void putDown() {
    lock.unlock();
  }
  @Override
  public String toString() {
    return "Fork " + id;
  }
}