package com.alexeyanufriev.boilerplatecodegen;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GetterTest {

    @Test
    public void test() {
        Entity entity = new Entity();
        Assertions.assertEquals("some value", entity.getProperty());
    }
}