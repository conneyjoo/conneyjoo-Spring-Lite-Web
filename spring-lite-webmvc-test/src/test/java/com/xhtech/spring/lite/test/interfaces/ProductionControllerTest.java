package com.xhtech.spring.lite.test.interfaces;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProductionControllerTest
{
    @Autowired
    protected TestRestTemplate restTemplate;

    @Test
    public void apiV1ProductionOrders()
    {
        String result = restTemplate.getForObject("/api/v1/production/orders", String.class);
        Assert.isTrue(result.startsWith("{\"code\":401,\"msg\":\"An Authentication object was not found in the SecurityContext\"}"), result);
    }
}
