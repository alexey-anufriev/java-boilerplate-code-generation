package com.xlab.ice.boilerplatecodegen.test;

import org.junit.Assert;
import org.junit.Test;

public class GetterTest {

    @Test
    public void test() {
        Entity entity = new Entity();
        Assert.assertEquals("some value", entity.getProperty());
    }
}