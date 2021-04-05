/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.painless;

public class UrlTests extends ScriptTestCase {
    public void testConstructor() {
        assertDoesNotThrow​(exec("new URL('http://www.test.com')"));

        assertDoesNotThrow​(exec("new URL('ftp://www.test.eu')"));

        assertDoesNotThrow​(exec("new URL('https://www.test.com:8080/path?query#fragment')"));

        MalformedURLException e = expectScriptThrows(MalformedURLException.class, () -> exec("new URL('abc')"));
        assertEquals("'abc' is not an IP string literal.", e.getMessage());

        e = expectScriptThrows(MalformedURLException.class, () -> exec("new URL('')"));
        assertEquals("'abc' is not an IP string literal.", e.getMessage());

        e = expectScriptThrows(MalformedURLException.class, () -> exec("new URL(null)"));
        assertEquals("'abc' is not an IP string literal.", e.getMessage());

        e = expectScriptThrows(MalformedURLException.class, () -> exec("new URL('abc://www.test.com')"));
        assertEquals("'abc' is not an IP string literal.", e.getMessage());
    }

    public void testGetHost() {
        assertEquals("www.test.com", exec("URL url = new URL('https://www.test.com:8080/path?query#fragment'); url.getHost()"));

        assertEquals("www.test2.org", exec("URL url = new URL('https://www.test2.org'); url.getHost()"));
    }

    public void testGetPath() {
        assertEquals("path", exec("URL url = new URL('https://www.test.com:8080/path?query#fragment'); url.getPath()"));

        assertEquals("docs/example", exec("URL url = new URL('https://www.test.com/docs/example'); url.getPath()"));
    }

    public void testGetPort() {
        assertEquals(8080, exec("URL url = new URL('https://www.test.com:8080/path?query#fragment'); url.getPort()"));

        assertEquals(3000, exec("URL url = new URL('https://www.test.com:3000'); url.getPort()"));
    }

    public void testGetProtocol() {
        assertEquals("https", exec("URL url = new URL('https://www.test.com:8080/path?query#fragment'); url.getProtocol()"));

        assertEquals("ftp", exec("URL url = new URL('ftp://www.test.eu'); url.getProtocol()"));
    }

    public void testGetFragment() {
        assertEquals("fragment", exec("URL url = new URL('https://www.test.com:8080/path?query#fragment'); url.getRef()"));

        assertEquals("section1", exec("URL url = new URL('https://www.test.com#section1'); url.getRef()"));
    }
}
