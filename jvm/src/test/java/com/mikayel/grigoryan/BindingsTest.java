package com.mikayel.grigoryan;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class BindingsTest {
    @Test
    public void testMultiplyDouble() {
        System.out.println("java.library.path = " + System.getProperty("java.library.path"));
        double x = Bindings.multiplyDouble(new ArrayList<>(), new ArrayList<>());
        System.out.println(x);
    }
}
