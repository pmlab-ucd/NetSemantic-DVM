/*
* Copyright 2014 Mingyuan Xia (http://mxia.me) and contributors
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* Contributors:
*   Mingyuan Xia
*   Lu Gong
*/

package fu.hao.trust.utils;

import patdroid.util.Report;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {
    public static final int MODE_VERBOSE = 0;
    public static final int MODE_DEBUG = 1;
    public static final int MODE_MSG = 2;
    public static final int MODE_WARNING = 3;
    public static final int MODE_SEVERE_WARNING = 4;
    public static final int MODE_ERROR = 5;
    public static final int MODE_REPORT = 6;
    public static final int MODE_CONCISE_REPORT = 7;
    public static final Writer stdout = new BufferedWriter(new OutputStreamWriter(System.out));
    public static final Writer stderr = new BufferedWriter(new OutputStreamWriter(System.err));
    public static Writer out = stdout;
    public static Writer err = stderr;
    

    private static ThreadLocal<String> indent = new ThreadLocal<String>() {
        @Override
        protected String initialValue() { return ""; }
    };

    private static void writeLog(String tag, int theLevel, String title, String msg, Writer w) {
        if (theLevel >= Settings.logLevel) {
            try {
                w.write(tag + " - " + indent.get() + "[" + title + "]: " + msg + "\n");
            } catch (IOException e) {
                // logging system should never die
                exit(1);
            }
        }
    }

    public static void exit(int r) {
        try {
            Log.out.close();
            Log.err.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(r);
    }

    protected static void log(String tag, int theLevel, String title, String msg) {	
    	 if (theLevel >= Settings.logLevel) {
    		 Logger logger = LoggerFactory
    	    			.getLogger(tag);
    		 logger.info(msg);
    		 writeLog(tag, theLevel, title, msg, out);
    	 }
    }

    protected static void badlog(String tag, int theLevel, String title, String msg) {
        switch (theLevel) {
            case MODE_WARNING: Report.nWarnings++; break;
            case MODE_SEVERE_WARNING: Report.nSevereWarnings++; break;
            case MODE_ERROR: Report.nErrors++; break;
            default: break;
        }
        Logger logger = LoggerFactory
    			.getLogger(tag);
        logger.error(title + " - " + msg);
        writeLog(tag, theLevel, title, msg, err);
    }

    public static void increaseIndent() { indent.set(indent.get() + "  "); }
    public static void decreaseIndent() { indent.set(indent.get().substring(2)); }
    public static void resetIndent() { indent.remove(); }
    public static void doAssert(String tag, boolean b, String msg) { if (!b) { err(tag, msg); } }
    public static void msg(String tag, String format, Object... args) { msg(tag, String.format(format, args)); }
    public static void msg(String tag, String s) { log(tag, MODE_MSG, "MSG", s);	}
    public static void debug(String tag, String format, Object... args) { debug(tag, String.format(format, args));	}
    public static void debug(String tag, String s) { log(tag, MODE_DEBUG, "DEBUG", s); }

    private static String exceptionToString(Exception e) {
        String s = e.toString() + "\n";
        StackTraceElement[] st = e.getStackTrace();
        for (StackTraceElement i : st) {
            s += i.toString() + "\n";
        }
        return s;
    }

    public static void warn(String tag, String format, Object... args) { warn(tag, String.format(format, args));	}
    public static void warn(String tag, Exception e) { warn(tag, exceptionToString(e)); }
    public static void warn(String tag, String s) { badlog(tag, MODE_WARNING, "WARN", s); }
    // forgive me for these cute names
    public static void warnwarn(String tag, String format, Object... args) { warnwarn(tag, String.format(format, args));	}
    public static void warnwarn(String tag, String s) { badlog(tag, MODE_SEVERE_WARNING, "WARN*", s); }
    public static void warnwarn(String tag, boolean b, String s) { if (!b) { warnwarn(tag, s); } }
    public static void err(String tag, Exception e) { err(tag, exceptionToString(e)); }
    public static void err(String tag, String format, Object... args) { err(tag, String.format(format, args)); }
    public static void err(String tag, String msg) { badlog(tag, MODE_ERROR, "ERROR", msg); }
}
