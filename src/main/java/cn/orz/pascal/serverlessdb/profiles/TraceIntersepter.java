/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.orz.pascal.serverlessdb.profiles;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import cn.orz.pascal.serverlessdb.Logger;

/**
 *
 * @author koduki
 */
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
@Trace
public class TraceIntersepter {

    @Inject
    Logger logger;

    @AroundInvoke
    public Object invoke(InvocationContext ic) throws Exception {
        long s = System.nanoTime();
        Object result = ic.proceed();
        long e = System.nanoTime();

        logger.profile(ic.getMethod().getName(), (e - s));
        return result;
    }
}
