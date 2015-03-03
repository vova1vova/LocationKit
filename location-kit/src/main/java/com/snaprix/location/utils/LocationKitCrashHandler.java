package com.snaprix.location.utils;

/**
 * Created by vladimirryabchikov on 1/27/15.
 */
public interface LocationKitCrashHandler {
    // logFatalException(e: Throwable)
    void logFatalException(Throwable e);
    // logNotFatalException(e: Throwable)
    void logNotFatalException(Throwable e);
}