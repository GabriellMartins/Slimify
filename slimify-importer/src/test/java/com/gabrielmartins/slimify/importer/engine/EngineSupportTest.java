package com.gabrielmartins.slimify.importer.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

class EngineSupportTest {

    @Test
    void parserKeepsTheLastMeaningfulLine() {
        String output = "Loading world...\n\u001B[31mProvided world seems to be corrupted.\u001B[0m\n\n   \n";

        assertEquals("Provided world seems to be corrupted.", OutputParser.reason(output));
    }

    @Test
    void parserIgnoresStackTraceLines() {
        String output = "Failed to load world level file:\njava.io.IOException: boom\n\tat a.b.C.d(C.java:1)\n\tat e.f.G.h(G.java:2)";

        assertEquals("java.io.IOException: boom", OutputParser.reason(output));
    }

    @Test
    void parserHasAFallbackForEmptyOutput() {
        assertEquals("The converter produced no file", OutputParser.reason(""));
    }

    @Test
    void captureGathersOutputAndFeedsTheConfirmation() {
        String output = ConsoleCapture.run(() -> {
            System.out.println("hello");
            System.err.println("oops");
            System.out.println("answer=" + new Scanner(System.in).next());
        });

        assertTrue(output.contains("hello"));
        assertTrue(output.contains("oops"));
        assertTrue(output.contains("answer=Y"));
    }

    @Test
    void captureRestoresTheStreamsEvenOnFailure() {
        InputStream in = System.in;
        PrintStream out = System.out;
        PrintStream err = System.err;

        assertThrows(IllegalStateException.class, () -> ConsoleCapture.run(() -> {
            throw new IllegalStateException("boom");
        }));

        assertSame(in, System.in);
        assertSame(out, System.out);
        assertSame(err, System.err);
    }
}
