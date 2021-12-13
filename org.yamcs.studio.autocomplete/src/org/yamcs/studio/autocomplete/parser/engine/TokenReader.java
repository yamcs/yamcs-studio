/********************************************************************************
 * Copyright (c) 2009, 2021 Peter Smith and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.parser.engine;

import java.io.IOException;
import java.io.Reader;

public class TokenReader extends Reader {

    private Reader r;
    private int peek = -1;
    private int currentIndex = 0;

    public TokenReader(Reader r) {
        this.r = r;
    }

    @Override
    public void close() throws IOException {
        r.close();
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (peek != -1) {
            var read = 1;
            cbuf[off] = (char) peek;
            if (len > 1) {
                read = r.read(cbuf, off + 1, len - 1);
            }
            if (read != -1) {
                read += 1;
            }
            peek = -1;
            currentIndex += read;
            return read;
        }
        var read = r.read(cbuf, off, len);
        currentIndex += read;
        return read;
    }

    public char ignoreWhitespace() throws IOException {
        if (peek != -1 && !Character.isWhitespace(peek)) {
            var ret = (char) peek;
            peek = -1;
            currentIndex++;
            return ret;
        }
        while (true) {
            var c = (char) r.read();
            currentIndex++;
            if (!Character.isWhitespace(c)) {
                return c;
            }
        }
    }

    public int peek() throws IOException {
        if (peek != -1) {
            return peek;
        } else {
            return peek = read();
        }
    }

    public String readUntil(char token) throws IOException {
        var sb = new StringBuilder();
        char c = 0;
        while ((c = (char) r.read()) != token) {
            sb.append(c);
        }
        currentIndex += sb.length();
        return sb.toString();
    }

    public int getCurrentIndex() {
        return currentIndex;
    }
}
