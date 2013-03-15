package com.example.provaq.rest;
import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

@Path("/hello")
public class HelloWorld {

    @GET
    @Path("/greet/{name}")
    @Produces("text/plain")
    public String greet(@PathParam("name") String name) {
        return "Hello, " + name + "\n";
    }

    @GET
    @Produces("text/provenance-notation")
    @Path("/provenance/greet/{name}")
    public String greetProvenance(@PathParam("name") String name,
    		@Context UriInfo ui) {
    	
    	// Get our absolute URI
    	// See http://cxf.apache.org/docs/jax-rs-basics.html#JAX-RSBasics-URIcalculationusingUriInfoandUriBuilder
    	UriBuilder appUri = ui.getBaseUriBuilder().path(getClass());
		UriBuilder greetUri = appUri.path(getClass(), "greet");
    	
    	// Absolute URIs for resources we are to give provenance about
    	URI greetURI = greetUri.build(name);
    	URI appURI = appUri.build("").resolve("../");
    	
    	// Simple PROV-N trace <http://www.w3.org/TR/prov-n/>
    	// 
    	// The resource .../hello/greet/fred is derived from an name entity with prov:value "fred"
    	// and was attributed to the .../hello service
    	
    	
    	// Note: PROV-N should probably better be built using the PROV Toolbox rather than 
    	// this naive approach!
    	StringBuilder prov = new StringBuilder();
    	prov.append("document\n");

    	URI greetPrefix = greetURI.resolve("./");
		prov.append(String.format(" prefix greet <%s>\n", greetPrefix));
		prov.append(String.format(" prefix app <%s>\n", appURI));
		prov.append("\n");
		
    	// Note, we could have used just entity(name:) and have
    	// the complete URI in a name: prefix, but this gives nicer
    	// rendering in provconvert
    	String greetEntity = "greet:" + greetPrefix.relativize(greetURI); 
    	
    	prov.append(String.format(" entity(%s)\n", greetEntity));
    	prov.append(String.format(" wasDerivedFrom(%s, name)\n", greetEntity));
    	prov.append("\n");
    	
    	prov.append(String.format(" entity(name, [ prov:value=\"%s\" ])\n", name));    	
    	prov.append(String.format(" wasAttributedTo(%s, app:hello)\n", greetEntity));
    	prov.append("\n");
    	
    	prov.append(" agent(app:hello, [ prov:type='prov:SoftwareAgent' ])\n");
    	
    	prov.append("endDocument\n");
    	return prov.toString();    	
    }
}

