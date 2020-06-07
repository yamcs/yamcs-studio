package org.yamcs.studio.core.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;

/**
 * Writes CSV according to RFC 4180.
 */
public class CsvWriter implements Closeable {

    private static final char[] CRLF = new char[] { '\r', '\n' };

    private boolean hasHeader;
    private boolean hasRecords;
    private int columnCount;

    private Writer writer;

    public CsvWriter(Writer writer) {
        this.writer = writer;
    }

    public void writeHeader(String[] header) throws IOException {
        if (hasHeader) {
            throw new IllegalArgumentException("Header can only be written once");
        }
        if (hasRecords) {
            throw new IllegalArgumentException("Header must be written before any other records");
        }

        hasHeader = true;
        columnCount = header.length;

        writer.write('"');
        writer.write(escape(header[0]));
        writer.write('"');
        for (int i = 1; i < header.length; i++) {
            writer.write(',');
            writer.write('"');
            writer.write(escape(header[i]));
            writer.write('"');
        }
        writer.write(CRLF);
    }

    public void writeRecord(String[] record) throws IOException {
        if (!hasHeader && !hasRecords) {
            columnCount = record.length;
        }

        if (columnCount != record.length) {
            throw new IllegalArgumentException("All records must be of equal length");
        }

        hasRecords = true;

        writer.write('"');
        writer.write(escape(record[0]));
        writer.write('"');
        for (int i = 1; i < record.length; i++) {
            writer.write(',');
            writer.write('"');
            writer.write(escape(record[i]));
            writer.write('"');
        }
        writer.write(CRLF);
    }

    private static String escape(String text) {
        return text.replace("\"", "\"\"");
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
