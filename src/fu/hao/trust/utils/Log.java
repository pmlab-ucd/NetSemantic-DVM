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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.management.RuntimeErrorException;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

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
	static String fileName;
	
	public static void updateFileName() {
		File file = new File(Settings.getOutdir());
		if (!file.exists() || !file.isDirectory()) {
			file.mkdir();
		}
		fileName = Settings.getOutdir() + File.separator + Settings.getApkName() + "_" + Settings.getEntryClass() + "_" + Settings.getEntryMethod() + ".log";
		file = new File(fileName);
		if (file.exists()) {
			file.delete();
		}
	}

	private static boolean writeLog(String TAG, int theLevel, String title,
			String msg) throws IOException {
		if (fileName == null) {
			updateFileName();
		}
		
		if (theLevel >= Settings.logLevel) {
			Writer output;
			output = new BufferedWriter(new FileWriter(fileName, true));
			output.append(TAG + " - " + "[" + title + "]: " + msg + "\n");
			output.close();

			return true;
		}

		return false;
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

	protected static void log(String TAG, int theLevel, String title, String msg)
			throws IOException {
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

		if (writeLog(TAG, theLevel, title, msg)) {
			if (theLevel < MODE_MSG) {
				System.out.println("[" + TAG + "] - " + title + " - " + msg);
			} else if (theLevel >= MODE_ERROR) {
				// logger.error(title + " - " + msg);
				System.err.println("[" + TAG + "] - " + title + " - " + msg);
				throw new RuntimeErrorException(null, TAG + " - " + msg);
			} else {
				// Logger logger = LoggerFactory.getLogger(TAG);
				// logger.info(msg);
				System.err.println("[" + TAG + "] - " + title + " - " + msg);
				// writeLog(TAG, theLevel, title, msg, err);
			}
		}
	}

	public static void doAssert(String TAG, boolean b, String msg) {
		if (!b) {
			err(TAG, msg);
		}
	}

	public static void msg(String TAG, String format, Object... args) {
		msg(TAG, String.format(format, args));
	}

	public static void msg(String TAG, Object s) {
		try {
			log(TAG, MODE_MSG, "MSG", s.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void debug(String TAG, String format, Object... args) {
		debug(TAG, String.format(format, args));
	}

	public static void debug(String TAG, Object s) {
		try {
			log(TAG, MODE_DEBUG, "DEBUG", s.toString());
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

	public static void warn(String TAG, Object s) {
		try {
			log(TAG, MODE_WARNING, "WARN", s.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// forgive me for these cute names
	public static void warnwarn(String TAG, String format, Object... args) {
		warnwarn(TAG, String.format(format, args));
	}

	public static void warnwarn(String TAG, String s) {
		try {
			log(TAG, MODE_SEVERE_WARNING, "WARN*", s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	public static void err(String TAG, Object msg) {
		try {
			log(TAG, MODE_ERROR, "ERROR", msg.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void bb(String TAG, Object msg) {
		try {
			log(TAG, MODE_VERBOSE, "BB", msg.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
