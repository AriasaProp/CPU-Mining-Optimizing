package com.ariasaproject.cpumininglearn.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MessageUtilsTest {
  @Test
  void testGetMessage() {
  	new Main().main(null);
    assertEquals("Hello      World!", MessageUtils.getMessage());
  }
}
