package com.grupo14IngSis.snippetSearcherRunner;

import org.example.Runner;

import java.util.Arrays;

public class RunnerWrapper {

    private static final Runner runner = new Runner();

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: RunnerWrapper <path> <version>");
            return;
        }

        try {
            runner.executionCommand(Arrays.asList(args));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
