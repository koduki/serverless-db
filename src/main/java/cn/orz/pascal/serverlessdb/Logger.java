/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.orz.pascal.serverlessdb;

import java.util.Optional;
import java.util.function.Supplier;

/**
 *
 * @author koduki
 */
public class Logger {

    public static <T> T trace(String message, Supplier<T> callback) {
        long s = System.nanoTime();
        T r = callback.get();
        long e = System.nanoTime();

        if (r instanceof Optional && ((Optional) r).isPresent()) {
            throw new RuntimeException((Exception) ((Optional) r).get());
        }

        String msg = String.format("tracelog:" + message + "%.3f", ((e - s) / 1_000_000.0));
        System.out.println(msg);
        return r;
    }

    public static void trace(String message, Runnable callback) {
        long s = System.nanoTime();
        callback.run();
        long e = System.nanoTime();

        String msg = String.format("tracelog:" + message + "%.3f", ((e - s) / 1_000_000.0));
        System.out.println(msg);
    }
}
