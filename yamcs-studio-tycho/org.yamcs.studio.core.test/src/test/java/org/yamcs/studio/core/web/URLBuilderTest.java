package org.yamcs.studio.core.web;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

public class URLBuilderTest {

    @Test
    public void testURLs() {
        URLBuilder urlb = new URLBuilder("resource");
        assertEquals("resource", urlb.toString());
        
        urlb.setParam("a", false);
        assertEquals("resource?a=false", urlb.toString());
        
        urlb.setParam("b", Arrays.asList("x", "y", "z"));
        assertEquals("resource?a=false&b[]=x&b[]=y&b[]=z", urlb.toString());
    }
}
