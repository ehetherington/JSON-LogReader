/*
 * The MIT License
 *
 * Copyright 2020 Edward Hetherington.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jsonlogreader;

import java.time.format.DateTimeFormatter;
import jsonlogreader.model.Record;

/**
 * Replicate all libtinylogger message formats except log_fmt_systemd.
 * @author Edward Hetherington
 */
public class RecordFormatter {
	private DateTimeFormatter dateTimeFormatter;
	private boolean showLevel = true;
	private boolean showThreadId = false;
	private boolean showThreadName = false;
	private boolean showSourceFile = false;
	private boolean showSourceFunction = false;
	private boolean showSourceLine = false;
	private boolean useNativeLineSeparator = true;
	final private String lineSeparator = System.lineSeparator();
	
	/**
	 * Create a record formatter that replicates <code>log_fmt_standard</code>.
	 */
	public RecordFormatter() {
		this.dateTimeFormatter =
			DateTimeFormatter.ofPattern(
				DateTimeFormat.TIME_FMT_FRACT_0.toString());
	}
	
	/**
	 * Create a record formatter that replicates the specified libtinylogger
	 * format.
	 * @param format the format to replicate
	 */
	public RecordFormatter(StandardFormat format) {
		this.dateTimeFormatter = format.dateTimeFormat == null ? null :
			DateTimeFormatter.ofPattern(format.dateTimeFormat.toString());
		this.showLevel = format.showLevel;
		this.showThreadId = format.showThreadId;
		this.showThreadName = format.showThreadName;
		this.showSourceFile = format.showSourceFile;
		this.showSourceFunction = format.showSourceFunction;
		this.showSourceLine = format.showSourceLine;
	}
	
	/**
	 * Use the native line separator. Defaults to true;
	 * @param yesNo false leaves the message as written. true converts newlines
	 * to the native value.
	 */
	public void setUseNativeLineSeparator(boolean yesNo) {
		useNativeLineSeparator = yesNo;
	}
	
	/**
	 * Use the specified <code>DateTimeFormatter</code>.
	 * @param dateTimeFormatter the formatter to use
	 */
	public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
		this.dateTimeFormatter = dateTimeFormatter;
	}
	
	/**
	 * Select the visibility of the message level.
	 * @param yesNo true to print the message level
	 */
	public void setShowLevel(boolean yesNo) {
		this.showLevel = yesNo;
	}
	
	/**
	 * Select the visibility of the calling thread id.
	 * @param yesNo true to print the calling thread id
	 */
	public void setShowThreadId(boolean yesNo) {
		this.showLevel = yesNo;
	}
	
	/**
	 * Select the visibility of the calling thread name.
	 * @param yesNo true to print the calling thread name
	 */
	public void setShowThreadName(boolean yesNo) {
		this.showLevel = yesNo;
	}

	/**
	 * Select the visibility of the source file name.
	 * @param yesNo true to print the source file name
	 */
	public void setShowSourceFile(boolean yesNo) {
		this.showSourceFile = yesNo;
	}

	/**
	 * Select the visibility of the source function name.
	 * @param yesNo true to print the source function name
	 */
	public void setShowSourceFunction(boolean yesNo) {
		this.showSourceFunction = yesNo;
	}

	/**
	 * Select the visibility of the source file name.
	 * @param yesNo true to print the souirce file name
	 */
	public void setShowSourceLine(boolean yesNo) {
		this.showSourceLine = yesNo;
	}
	
	/**
	 * Records are generated on a Linux machine and always have
	 * <code>'\n'</code> line separators. Replace them with "\r\n" on windows.
	 */
	private String toNative(String text) {
		if (("\n".equals(lineSeparator)) ||
			!useNativeLineSeparator) return text;
		return text == null ? null :
			text.replaceAll("\n", lineSeparator);
	}
	
	/**
	 * Format the record in the configured format.
	 * @param record the record to format
	 * @return the formatted record
	 */
	public String format(Record record) {
		StringBuilder sb = new StringBuilder();
		String separator = "";
		
		// Date/Time
		if (dateTimeFormatter != null) {
			dateTimeFormatter.formatTo(record.getZonedDateTime(), sb);
			separator = " ";
		}
		
		// Level
		if (showLevel) {
			sb.append(separator).append(record.getLevel());
			separator = " ";
		}
		
		// Thread ID (numeric)
		if (showThreadId) {
			sb.append(separator).append(record.getThreadId());
			separator = showThreadName ? ":" : " ";
		}
		
		// Thread Name
		if (showThreadName) {
			sb.append(separator).append(record.getThreadName());
			separator = " ";
		}
		
		// Caller file
		if (showSourceFile) {
			sb.append(separator).append(record.getFile());
			separator = (showSourceFunction || showSourceLine) ? ":" : " ";
		}
		
		// Caller function
		if (showSourceFunction) {
			sb.append(separator).append(record.getFunction());
			separator = showSourceLine ? ":" : " ";
		}
		
		// Caller line
		if (showSourceFile) {
			sb.append(separator).append(record.getLine());
			separator = " ";
		}
		
		// message is generated on a Linux machine and always has '\n'
		// replace with "\r\n" on windows
		sb.append(separator).append(toNative(record.getMessage()));

		return sb.toString();
	}

	/**
	 * A selection of pre-defined formats for the RecordFomatter to use.
	 * All pre-defined formats include <code>"uuuu-MM-dd HH:mm:ss"</code>. They
	 * include varying amounts of the fraction of the second, from none to
	 * nanosecond resolution.
	 * <p>
	 * The <code>TIME_FMT_XXX</code> ones separate the date and time with a space,
	 * while the <code>ISOT_FMT_XXX</code> ones use the ISO-8601 recommended
	 * <code>"T"</code>.
	 * </p>
	 */
	public enum DateTimeFormat {

		/** <code>2020-09-31 07:01:15</code> */
		TIME_FMT_FRACT_0("uuuu-MM-dd HH:mm:ss"),
		/** <code>2020-09-31 07:01:15.123</code>  */
		TIME_FMT_FRACT_3("uuuu-MM-dd HH:mm:ss.SSS"),
		/** <code>2020-09-31 07:01:15.123456</code>  */
		TIME_FMT_FRACT_6("uuuu-MM-dd HH:mm:ss.SSSSSS"),

		/** <code>2020-09-31 07:01:15.123456789</code>  */
		TIME_FMT_FRACT_9("uuuu-MM-dd HH:mm:ss.SSSSSSSSS"),

		/** add UTC offset <code>2020-09-31 07:01:15.123456789-04:00</code>  */
		TIME_FMT_FRACT_9_OFFSET("uuuu-MM-dd HH:mm:ss.SSSSSSSSSxxxxx"),

		/** <code>2020-09-31T07:01:15</code>  */
		ISOT_FMT_FRACT_0("uuuu-MM-ddTHH:mm:ss"),	// ISO-8601 "T"
		/** <code>2020-09-31T07:01:15.123</code>  */
		ISOT_FMT_FRACT_3("uuuu-MM-ddTHH:mm:ss.SSS"),
		/** <code>2020-09-31T07:01:15.123456</code>  */
		ISOT_FMT_FRACT_6("uuuu-MM-ddTHH:mm:ss.SSSSSS"),
		/** <code>2020-09-31T07:01:15.123456789</code>  */
		ISOT_FMT_FRACT_9("uuuu-MM-ddTHH:mm:ss.SSSSSSSSS"),
		/** add UTC offset <code>2020-09-31 07:01:15.123456789-04:00</code>  */
		TIME_FMT_FRACT_NANO("uuuu-MM-dd HH:mm:ss.nnnnnnnnn"),
		;

        final String formatString;

        private DateTimeFormat(final String formatString) {
            this.formatString = formatString;
        }

		/**
		 * Returns the format string.
		 * @return  the format string
		 */
		@Override
		public String toString() {
			return formatString;
		}
	}

	/**
	 * The pre-defined formats in libtinylogger except <code>log_fmt_systemd
	 * </code> and the structured formats <code>log_fmt_json</code> and <code>
	 * log_fmt_xml</code>.
	 */
	public enum StandardFormat {

		/** Replicate <code>log_fmt_basic</code>. */
		BASIC(null,					// no timestamp
			false, false, false,	// level, threadId, threadName
			false, false, false),	// file, function, line),
		/** Replicate <code>log_fmt_standard</code>. */
		STANDARD(DateTimeFormat.TIME_FMT_FRACT_0,
			true, false, false,		// level, threadId, threadName
			false, false, false),	// file, function, line),
		/** Replicate <code>log_fmt_debug</code>. */
		DEBUG(DateTimeFormat.TIME_FMT_FRACT_3,
			true, false, false,		// level, threadId, threadName
			true, true, true),		// file, function, line),
		/** Replicate <code>log_fmt_debug_tid</code>. */
		DEBUG_TID(DateTimeFormat.TIME_FMT_FRACT_3,
			true, true, false,		// level, threadId, threadName
			true, true, true),		// file, function, line),
		/** Replicate <code>log_fmt_log_fmt_tname</code>. */
		DEBUG_TNAME(DateTimeFormat.TIME_FMT_FRACT_3,
			true, false, true,		// level, threadId, threadName
			true, true, true),		// file, function, line),
		/** Replicate <code>log_fmt_log_fmt_tall</code>. */
		DEBUG_TALL(DateTimeFormat.TIME_FMT_FRACT_3,
			true, true, true,		// level, threadId, threadName
			true, true, true),		// file, function, line),
		/** Replicate <code>log_fmt_debug_tall</code> with nanoseconds. */
		DEBUG_TALL_9(DateTimeFormat.TIME_FMT_FRACT_9,
			true, true, true,		// level, threadId, threadName
			true, true, true),		// file, function, line),
		/** Format for TimestampDemo. */
		TIMESTAMP_DEMO(DateTimeFormat.TIME_FMT_FRACT_9_OFFSET,
			false, false, false,	// level, threadId, threadName
			false, false, false),	// file, function, line),
		;

		final DateTimeFormat dateTimeFormat;
		final boolean showLevel;
		final boolean showThreadId;
		final boolean showThreadName;
		final boolean showSourceFile;
		final boolean showSourceFunction;
		final boolean showSourceLine;
		
        private StandardFormat(
			final DateTimeFormat formatString,
			final boolean showLevel,
			final boolean showThreadId,
			final boolean showThreadName,
			final boolean showSourceFile,
			final boolean showSourceFunction,
			final boolean showSourceLine
			) {
			this.dateTimeFormat = formatString;
			this.showLevel = showLevel;
			this.showThreadId = showThreadId;
			this.showThreadName = showThreadName;
			this.showSourceFile = showSourceFile;
			this.showSourceFunction = showSourceFunction;
			this.showSourceLine = showSourceLine;
        }
	}
}
