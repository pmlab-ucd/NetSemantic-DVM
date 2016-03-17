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
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.management.RuntimeErrorException;

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
	public static FileWriter fileWriter = null;
	public static Writer stdout = new BufferedWriter(new OutputStreamWriter(
			System.out));
	public static Writer stderr = new BufferedWriter(new OutputStreamWriter(
			System.err));
	public static Writer out = stdout;
	public static Writer err = stderr;

	private static ThreadLocal<String> indent = new ThreadLocal<String>() {
		@Override
		protected String initialValue() {
			return "";
		}
	};

	private static void writeLog(String TAG, int theLevel, String title,
			String msg, Writer w) throws IOException {
		if (fileWriter == null) {
			fileWriter = new FileWriter("output/" + Settings.apkName + ".log");
			out = new BufferedWriter(fileWriter);
			err = new BufferedWriter(fileWriter);
		}
		if (theLevel >= Settings.logLevel) {
			try {
				w.write(TAG + " - " + indent.get() + "[" + title + "]: " + msg
						+ "\n");
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
	
	public static void bb(String TAG, String msg) {
		System.out.println("[" + TAG + "] - " + msg);
		try {
			writeLog(TAG, 0, TAG, msg, out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected static void log(String TAG, int theLevel, String title, String msg)
			throws IOException {
		if (theLevel >= Settings.logLevel) {
			Logger logger = LoggerFactory.getLogger(TAG);
			logger.info(msg);
			//System.out.println(TAG + "--" + msg);
			writeLog(TAG, theLevel, title, msg, out);
		}
	}

	protected static void badlog(String TAG, int theLevel, String title,
			String msg) {
		switch (theLevel) {
		case MODE_WARNING:
			Report.nWarnings++;
			break;
		case MODE_SEVERE_WARNING:
			Report.nSevereWarnings++;
			break;
		case MODE_ERROR:
			Report.nErrors++;
			break;
		default:
			break;
		}
		Logger logger = LoggerFactory.getLogger(TAG);
		try {
			writeLog(TAG, theLevel, title, msg, err);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (theLevel >= MODE_ERROR) {
			logger.error(title + " - " + msg);
			throw new RuntimeErrorException(null, TAG + " - " + msg);
		} else {
			logger.warn(title + " - " + msg);
		}
	}

	public static void increaseIndent() {
		indent.set(indent.get() + "  ");
	}

	public static void decreaseIndent() {
		indent.set(indent.get().substring(2));
	}

	public static void resetIndent() {
		indent.remove();
	}

	public static void doAssert(String TAG, boolean b, String msg) {
		if (!b) {
			err(TAG, msg);
		}
	}

	public static void msg(String TAG, String format, Object... args) {
		msg(TAG, String.format(format, args));
	}

	public static void msg(String TAG, String s) {
		try {
			log(TAG, MODE_MSG, "MSG", s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void debug(String TAG, String format, Object... args) {
		debug(TAG, String.format(format, args));
	}

	public static void debug(String TAG, String s) {
		try {
			log(TAG, MODE_DEBUG, "DEBUG", s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String exceptionToString(Exception e) {
		String s = e.toString() + "\n";
		StackTraceElement[] st = e.getStackTrace();
		for (StackTraceElement i : st) {
			s += i.toString() + "\n";
		}
		return s;
	}

	public static void warn(String TAG, String format, Object... args) {
		warn(TAG, String.format(format, args));
	}

	public static void warn(String TAG, Exception e) {
		warn(TAG, exceptionToString(e));
	}

	public static void warn(String TAG, String s) {
		badlog(TAG, MODE_WARNING, "WARN", s);
	}

	// forgive me for these cute names
	public static void warnwarn(String TAG, String format, Object... args) {
		warnwarn(TAG, String.format(format, args));
	}

	public static void warnwarn(String TAG, String s) {
		badlog(TAG, MODE_SEVERE_WARNING, "WARN*", s);
	}

	public static void warnwarn(String TAG, boolean b, String s) {
		if (!b) {
			warnwarn(TAG, s);
		}
	}

	public static void err(String TAG, Exception e) {
		err(TAG, exceptionToString(e));
	}

	public static void err(String TAG, String format, Object... args) {
		err(TAG, String.format(format, args));
	}

	public static void err(String TAG, String msg) {
		badlog(TAG, MODE_ERROR, "ERROR", msg);
	}
}
