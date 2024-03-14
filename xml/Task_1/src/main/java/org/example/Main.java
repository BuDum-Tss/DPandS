package org.example;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class Main {

  public static void main(String[] args) {
    try (InputStream stream = new FileInputStream("./people.xml")) {
      List<PersonInfo> data = new PeopleParser().parse(stream);
      for (PersonInfo person : data) {
        System.out.println(person);
      }
    } catch (IOException | XMLStreamException e) {
      e.printStackTrace();
    }
  }
}