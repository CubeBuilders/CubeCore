package io.siggi.cubecore.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class LineReader implements Closeable {
    private final BufferedInputStream in;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    private long currentReadPosition = 0;
    private long beginningOfLine = 0;
    private long endOfLine = 0;

    public LineReader(InputStream in) {
        if (in instanceof BufferedInputStream) {
            this.in = (BufferedInputStream) in;
        } else {
            this.in = new BufferedInputStream(in);
        }
    }

    public String readLine() throws IOException {
        beginningOfLine = endOfLine = currentReadPosition;
        boolean read = false;
        out.reset();
        int c;
        while ((c = in.read()) != -1) {
            currentReadPosition += 1L;
            read = true;
            if (c == 0x0D) {
                in.mark(1);
                int nextByte = in.read();
                if (nextByte != 0x0A) {
                    in.reset();
                } else {
                    currentReadPosition += 1L;
                }
                break;
            } else if (c == 0x0A) {
                break;
            }
            out.write(c);
            endOfLine = currentReadPosition;
        }
        if (!read) return null;
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    public long getBeginningOfLine() {
        return beginningOfLine;
    }

    public long getEndOfLine() {
        return endOfLine;
    }

    @Override
    public void close() throws IOException {
        this.in.close();
    }
}
