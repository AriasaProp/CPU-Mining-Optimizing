package com.ariasaproject.cpumininglearn;

import static com.ariasaproject.cpumininglearn.app.MessageUtils.getMessage;
import static com.ariasaproject.cpumininglearn.utilities.StringUtils.join;
import static com.ariasaproject.cpumininglearn.utilities.StringUtils.split;

import com.ariasaproject.cpumininglearn.list.LinkedList;
import org.apache.commons.text.WordUtils;

public class App {
  public static void main(String[] args) {
    LinkedList tokens;
    tokens = split(getMessage());
    String result = join(tokens);
    System.out.println(WordUtils.capitalize(result));
  }
}
