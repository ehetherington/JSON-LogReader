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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import jsonlogreader.model.TinyLoggerMapper;

/**
 * Determine whether a log file contains a <code>Log</code> object or a series
 * of <code>Record</code>s. This class takes a fairly crude approach. It looks
 * at the first 5 <code>JsonToken</code>s for a "signature".
 * <p>
 * This should probably be changed to look for a FIELD_NAME token with a name of
 * records before the first FIELD_NAME token with a name of isoDateTime.
 * <p>
 * Logs with just a series records has no enclosing records array, whereas logs
 * with a "root element" of Log does.
 * <p>
 * This would greatly simplify this class.
 * @author Edward Hetherington
 */
public class LogIdentifier {
	
	private final static Map<Signature, LogType> signatures = new HashMap<>();
	
	static {
		JsonToken[] tokens;

		// Log without header (the original version)
		tokens = new JsonToken[] {
			JsonToken.START_OBJECT,	// name = null
			JsonToken.FIELD_NAME,	// name = records
			JsonToken.START_ARRAY,	// name = records
			JsonToken.START_OBJECT,	// name = null
			JsonToken.FIELD_NAME	// name = isoDateTime
		};
		signatures.put(
			new Signature("LogWithoutHeader", tokens), LogType.LOG);

		// Log with header
		tokens = new JsonToken[] {
			JsonToken.START_OBJECT,	// name = null
			JsonToken.FIELD_NAME,	// name = logHeader
			JsonToken.START_OBJECT,	// name = logHeader
			JsonToken.FIELD_NAME,	// name = startDate
			JsonToken.VALUE_STRING	// name = startDate
		};
		signatures.put(
			new Signature("LogWithHeader", tokens), LogType.LOG_WITH_HEADER);

		// Series of records
		tokens = new JsonToken[] {
			JsonToken.START_OBJECT,	// name = null
			JsonToken.FIELD_NAME,	// name = isoDateTime
			JsonToken.VALUE_STRING,	// name = isoDateTime
			JsonToken.FIELD_NAME,	// name = timespec
			JsonToken.START_OBJECT	// name = timespec
		};
		signatures.put(
			new Signature("SeriesOfRecords", tokens), LogType.RECORDS);
	}
	
	/**
	 * This class just provides static methods.
	 */
	private LogIdentifier() {}
	
	/**
	 * Identify the log file type by filename
	 * @param filename the pathname of the file in question
	 * @return the LogType determined
	 */
	static public LogType identifyLog(String filename) {
		return identifyLog(new File(filename));
	}
	
	/**
	 * Identify the log file type by File object
	 * @param file the File object for the file in question
	 * @return the LogType determined
	 */
	static public LogType identifyLog(File file) {
		if (!file.exists()) return LogType.UNKNOWN;
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return LogType.UNKNOWN;
		}
		
		return identifyLog(fis);
	}
	
	/**
	 * Identify the log file type by inspecting the beginning of an input
	 * stream. This is not general purpose method, as it consumes the beginning
	 * of the log, and then closes it.
	 * <p>It is used by TinyLoggerReader with a special purpose subclass of
	 * InputStream that takes care of these problems.</p>
	 * @param inputStream the InputSream containing the log
	 * @return the LogType determined
	 */
	static LogType identifyLog(InputStream inputStream) {
		TinyLoggerMapper mapper = new TinyLoggerMapper();
		JsonFactory factory = mapper.getFactory();
		JsonToken[] tokens = new JsonToken[Signature.SIG_LENGTH];
		try (JsonParser parser = factory.createParser(inputStream)) {
			for (int n = 0; n < Signature.SIG_LENGTH; n++) {
				tokens[n] = parser.nextToken();
			}
		} catch (IOException ex) {
			return LogType.UNKNOWN;
		}
		LogType logType = signatures.get(new Signature("unknown", tokens));
		return logType != null ? logType : LogType.UNKNOWN;
	}

	/**
	 * libtinylogger produces logs in three flavors.
	 * <ul>
	 * <li>Log without header (the original)</li>
	 * <li>Log with header</li>
	 * <li>Series of Records</li>
	 * </ul>
	 */
	public enum LogType {
		/** Log without header */
		LOG,
		/** Log with header */
		LOG_WITH_HEADER,
		/** series of Records */
		RECORDS,
		/** not a recognized log */
		UNKNOWN;
	}

	private static class Signature {
		final static int SIG_LENGTH = 5;
		String description;
		JsonToken signature[];
	
		Signature(String logType, JsonToken[] tokens) {
			if ((tokens == null) || (tokens.length != SIG_LENGTH)) {
			throw new IllegalArgumentException(
				String.format("signatures must be %s tokens long", SIG_LENGTH));
			}
			this.description = logType;
			this.signature = tokens;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 89 * hash + Arrays.deepHashCode(this.signature);
			return hash;
		}
		
		@Override
		public boolean equals(Object anObject) {
			if (this == anObject) {
				return true;
			}
			if (! (anObject instanceof Signature)) {
				return false;
			} else {
				return Arrays.equals(signature,
					((Signature) anObject).signature);
			}
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (JsonToken token: signature) {
				sb.append(token).append(" ");
			}
			return sb.toString();
		}
	}
}
