package org.example;

public class Main {

  public static void main(String[] args) {
    Company company = new Company(4);
    Founder founder = new Founder(company);
    founder.start();
  }
}