package com.snaprix.location.utils;

/**
 * Created by vladimirryabchikov on 1/27/15.
 */
public class LocationKitLogger {
    private static boolean SHOW_LOGS = false;
    private static LocationKitCrashHandler sCrashHandler;

    // +getCrashHandler(): LocationKitCrashHandler
    public static LocationKitCrashHandler getCrashHandler() {
        return sCrashHandler;
    }

    // +setCrashHandler(crashHandler: LocationKitCrashHandler)
    public static void setCrashHandler(LocationKitCrashHandler crashHandler) {
        sCrashHandler = crashHandler;
    }

    // +isShowLogs(): boolean
    public static boolean isShowLogs() {
        return SHOW_LOGS;
    }

    // +setShowLogs(enabled: boolean)
    public static void setShowLogs(boolean enabled) {
        LocationKitLogger.SHOW_LOGS = enabled;
    }
}