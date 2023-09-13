package org.example;

import java.util.ArrayList;
import java.util.List;

public final class Founder {

  private final List<Runnable> workers;

  public Founder(final Company company) {
    this.workers = new ArrayList<>(company.getDepartmentsCount());
  }

  public void start() {
    for (final Runnable worker : workers) {
      new Thread(worker).start();
    }
  }
}