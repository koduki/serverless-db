/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.orz.pascal.serverlessdb;

import javax.inject.Named;
import javax.enterprise.context.Dependent;

/**
 *
 * @author koduki
 */
@Named
@Dependent
public class Logger {

    private static final java.util.logging.Logger APP_LOGGER = java.util.logging.Logger.getLogger("serverlessdb");
    private static final java.util.logging.Logger PROFILE_LOGGER = java.util.logging.Logger.getLogger("serverlessdb.profile");

    public void profile(String name, long duration) {
        String msg = String.format("tracelog: %s(ms): %.3f", name, (duration / 1_000_000.0));
        PROFILE_LOGGER.info(msg);
    }

    public void info(String message) {
        APP_LOGGER.info(message);
    }
}
