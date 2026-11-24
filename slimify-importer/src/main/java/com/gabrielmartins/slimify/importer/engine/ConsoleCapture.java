package com.gabrielmartins.slimify.importer.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ConsoleCapture {

    private final Object LOCK = new Object();
    private final byte[] CONFIRM = "Y\n".getBytes(StandardCharsets.UTF_8);

    public String run(Runnable action) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        synchronized (LOCK) {
            InputStream in = System.in;
            PrintStream out = System.out;
            PrintStream err = System.err;
            PrintStream capture = new PrintStream(buffer, true, StandardCharsets.UTF_8);
            try {
                System.setIn(new ByteArrayInputStream(CONFIRM));
                System.setOut(capture);
                System.setErr(capture);
                action.run();
            } finally {
                System.setIn(in);
                System.setOut(out);
                System.setErr(err);
            }
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
