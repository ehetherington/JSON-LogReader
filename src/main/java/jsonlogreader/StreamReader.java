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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;
import jsonlogreader.LogIdentifier.LogType;
import jsonlogreader.model.Log;
import jsonlogreader.model.Header;
import jsonlogreader.model.Record;
import jsonlogreader.model.TinyLoggerMapper;

/**
 * StreamReader can read both types of logs libtinylogger can produce.
 * One is a Log with an array of Records The other is a series of Records. The
 * Log form may optionally have a header if libtinylogger is configured to do
 * so.
 * 
 * <p>The goal is to provide an example of reading streams of
 * JSON Logs or Records produced by libtinylogger in either form by use of
 * the LogIdentifier class.</p>
 * 
 * <p>If you know beforehand which type of log you are reading, the "BasicXXX"
 * examples skip the log type determination.</p>
 * 
 * <p>For a minimum example of reading a <code>Log</code> file, see the
 * BasicLogReader.java example.</p>
 * 
 * <p>For a minimum example of reading a log that is a series of
 * <code>Record</code>s, see the BasiRecordReader.java example.</p>
 * 
 * @author Edward Hetherington
 */
public class StreamReader {
	final private Logger logger =
		Logger.getLogger(StreamReader.class.getName());
	private ObjectMapper mapper = null;
	private RecordFormatter formatter = null;
	private final String lineSeparator = System.lineSeparator();
	private final boolean useNativeLineSeparator = true;
	
	private final boolean DEFAULT_ADJUST_DATES_TO_CONTEXT = true;
		
	/**
	 * Create a StreamReader with the specified ObjectMapper. The ObjectMapper
	 * must have the SDLevel and JavaTime modules registered that
	 * TinyLoggerMapper does in order to de-serialize a tinylogger log.
	 * @param mapper the ObjectMapper to use
	 */
	public StreamReader(ObjectMapper mapper) {
		this.mapper = mapper;
		formatter = new RecordFormatter();
		formatter.setUseNativeLineSeparator(useNativeLineSeparator);
	}
		
	/**
	 * Create a StreamReader with a TinyLoggerMapper that adjusts the time
	 * stamps in the log file to the current default time zone.
	 */
	public StreamReader() {
		this(new TinyLoggerMapper());
		if (DEFAULT_ADJUST_DATES_TO_CONTEXT) {
			mapper.setTimeZone(TimeZone.getDefault());
		} else {
			mapper.disable(
				DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
		}
	}
	
	/**
	 * Get the current ObjectMapper. Useful if you want to change settings /
	 * features.
	 * @return the current ObjectMapper
	 */
	public ObjectMapper getMapper() {
		return mapper;
	}
	
	/**
	 * Set the ObjectMapper. In case you have an ObjectMapper you prefer.
	 * @param mapper the new ObjectMapper
	 */
	public void setMapper(ObjectMapper mapper) {
		if (mapper == null)
			throw new IllegalArgumentException("mapper must not be null");
		
		this.mapper = mapper;
	}
	
	/**
	 * Get the current RecordFormatter. You may want to change some parameters.
	 * @return the current RecordFormatter
	 */
	public RecordFormatter getFormatter() {
		return formatter;
	}
	
	/**
	 * Set the current RecordFormatter. You can create your own RecordFormatter
	 * by extending it.
	 * @param formatter the new RecordFormatter
	 */
	public void setFormatter(RecordFormatter formatter) {
		if (mapper == null)
			throw new IllegalArgumentException("formatter must not be null");
		
		this.formatter = formatter;
	}
	
	/**
	 * Logs are generated on a Linux machine and always have
	 * <code>'\n'</code> line separators. Replace them with "\r\n" on windows.
	 */
	private String toNative(String text) {
		if (("\n".equals(lineSeparator)) ||
			!useNativeLineSeparator) return text;
		return text == null ? null :
			text.replaceAll("\n", lineSeparator);
	}
	
	/**
	 * Read a log from the an InputStream, process it, and write the output to
	 * an OutputStream.
	 * 
	 * There are two "processors" implemented. One reads one or more Logs, and
	 * the other reads a stream of Records.
	 * 
	 * @param inputStream where to read the log from
	 * @param outputStream where to write the results
	 */
	public void readLog(InputStream inputStream, OutputStream outputStream) {
		
		InputStreamPeeker peeker = new InputStreamPeeker(inputStream);
		
		LogType logType = LogIdentifier.identifyLog(peeker);
		
		ReaderBase reader = null;
		
		switch(logType) {
			case LOG: case LOG_WITH_HEADER: {
				reader = new LogReader(peeker, outputStream);
			} break;
			case RECORDS: {
				reader = new RecordReader(peeker, outputStream);
			} break;
		}
		
		if (reader == null) {
			logger.severe("input does not appear to be a tinylogger log%n");
			System.exit(1);
		}
		
		try {
			reader.readObjects();
		} catch (IOException e) {
			logger.severe(String.format("readLog(): error reading stream: %s%n",
				e.getMessage()));
			System.exit(1);
		}
	}
	
	/**
	 * Special purpose InputStream.
	 * Created to allow LogIdentifier.identifyLog(InputStream) to "peek" into
	 * the input stream to identify which type of log it is.
	 * <p>
	 * On creation, it marks the start of the stream. It ignores the first close
	 * and instead resets the input stream. Subsequent closes are then passed on
	 * to the underlying input stream.
	 * </p>
	 */
	private final class InputStreamPeeker extends BufferedInputStream {
		boolean firstCloseIgnored = false;
		
		/**
		 * Wrap an InputStream so that it can be used by
		 * LogIdentifier.identifyLog(InputStream) and remain usable for future
		 * purposes.
		 * @param inputStream the InputStream to wrap
		 */
		private InputStreamPeeker(InputStream inputStream) {
			super(inputStream);
			mark(4096);
		}
		
		@Override
		public void close() throws IOException {
			if (firstCloseIgnored) {
				super.close();
			} else {
				reset();
				firstCloseIgnored = true;
			}
		}
	}
		
	private abstract class ReaderBase<T> {
		private final InputStream inputStream;
		private final OutputStream outputStream;

		ReaderBase(InputStream inputStream, OutputStream outputStream) {
			this.inputStream = inputStream;
			this.outputStream = outputStream;
		}

		JsonParser getParser(ObjectMapper mapper) {
			JsonParser parser = null;
			try {
				parser = mapper.getFactory().createParser(inputStream);
			} catch (IOException e) {
				logger.severe(String.format("can't get parser: %s",
					e.getMessage()));
			}
			return parser;
		}
		
		PrintStream getPrintStream() {
			PrintStream printStream = System.out;
			try {
				printStream = new PrintStream(outputStream, true,
					StandardCharsets.UTF_8.toString());
			} catch (UnsupportedEncodingException e) {
				logger.warning(String.format("can't create UTF-8 PrintStream",
					e.getMessage()));
			}
			
			return printStream;
		}
		
		PrintWriter getPrintWriter() {
			PrintWriter writer = null;
			Writer out =
				new java.io.BufferedWriter(
					new OutputStreamWriter(outputStream,
						StandardCharsets.UTF_8));
			writer = new PrintWriter(out, true);
			
			return writer;
		}
		
		abstract void readObjects() throws IOException;
	}
		
	private class LogReader extends ReaderBase {
		LogReader(InputStream inputStream, OutputStream outputStream) {
			super(inputStream, outputStream);
		}
		
		@Override
		void readObjects() throws IOException {
			JsonParser parser;
			if ((parser = getParser(mapper)) == null) return;
			PrintWriter writer = getPrintWriter();

			int serial = 0;
			while (parser.nextToken() != null) {
				Log log = mapper.readValue(parser, Log.class);
				if (log != null) {
					writer.format("%s=== log number %d%n",
						serial == 0 ? "" : "\n", ++serial);
					Header header = log.getHeader();
					// use simple formatting provided by Header.toString()
					// use native line.separator
					if (header != null)
						writer.println(toNative(header.toString()));
					List<Record> records = log.getRecords();
					records.forEach((record) -> {
						writer.println(formatter.format(record));
					});
				} else {
					logger.warning("expecting log, but it wasn't");
				}
			}
			
			parser.close();
		}
	}
		
	private class RecordReader extends ReaderBase {
		int sequenceCount = 0;
		RecordReader(InputStream inputStream, OutputStream outputStream) {
			super(inputStream, outputStream);
		}
		
		@Override
		void readObjects() throws IOException {
			JsonParser parser;
			if ((parser = getParser(mapper)) == null) return;
			
			PrintWriter writer = getPrintWriter();
			
			while (parser.nextToken() != null) {
				Record record = mapper.readValue(parser, Record.class);
				if (record != null) {
					if (record.getSequence() == 1) {
						writer.format(
							"found start of record stream #%d " +
								"(record sequence was 1)%n",
							sequenceCount++);
					}
					writer.println(formatter.format(record));
				} else {
					logger.warning("expecting record, but it wasn't");
				}
			}
			
			parser.close();
		}
	}
}
