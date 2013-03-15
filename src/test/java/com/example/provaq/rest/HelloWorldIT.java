package com.example.provaq.rest;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.BeforeClass;
import org.junit.Test;

public class HelloWorldIT {
	private static String endpointUrl;
	
	@BeforeClass
	public static void beforeClass() {
		endpointUrl = System.getProperty("service.url");
	}
	
	@Test
	public void testGreet() throws Exception {
		WebClient client = WebClient.create(endpointUrl + "/hello/greet/SierraTangoNevada");
		Response r = client.accept("text/plain").get();
		assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
		String value = IOUtils.toString((InputStream)r.getEntity());
		assertEquals("Hello, SierraTangoNevada\n", value);
	}
	
}
