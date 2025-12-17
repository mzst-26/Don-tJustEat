package com.example;
import static org.junit.Assert.assertNotNull;

import com.example.dontjusteat.customer_login;

import org.junit.Test;
public class customerLoginUnitTest {

    @Test
    public void testActivityClassExists() {
        customer_login login = new customer_login();
        assertNotNull(login);
    }
}
