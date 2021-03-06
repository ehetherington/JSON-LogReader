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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TimeZone;
import jsonlogreader.model.TinyLoggerMapper;

/**
 * This program reads JSON log files produced by libtinylogger. It is a guide to
 * reading those files using FasterXML/jackson. It will not fail gracefully with
 * a deliberately broken log file, but will read logs as produced by
 * libtinylogger.
 * 
 * <p>This has become a "front-end" for StreamReader. It just processes command
 * line arguments, then uses StreamReader to actually process the input.
 * 
 * <p>See the examples directory for more basic examples.</p>
 * 
 * Enhancements planned:
 * <ul>
 * <li>gui requested
 * <p> A simple gui to select a file to read and which tinylogger format to use
 * for output.
 * </p>
 * </ul>
 * 
 * <p>This project uses FasterXML/jackson for its object mapper and parser. The
 * model uses jackson annotations sparingly and only one custom serializer  /
 * deserializer, so should be relatively easy to port to different parsers.
 * </p>
 * 
 * @author Edward Hetherington
 * 
 * @see <a href="https://github.com/FasterXML/jackson-databind">jackson-databind</a>
 * @see <a href="https://github.com/FasterXML/jackson-core">jackson-core</a>
 */
public class JsonLogReader {
	
	/**
	 * This has been reduced to just the static main method that handles the
	 * command line options and then runs a StreamReader.
	 */
	private JsonLogReader() {};

	/**
	 * Read a log file generated by libtinylogger using the Json format.
	 * @param args the command line arguments
	 * @throws java.io.IOException on IO errors
	 */
	public static void main(String[] args) throws IOException {
		LogReaderOptions options = new LogReaderOptions();
		options.parseOptions(args);

		// set up the input
		InputStream inputStream;
		String inputFile = options.getInputFile();
		// "-" indicates stdin
		if ("-".equals(inputFile)) {
			inputStream = System.in;
		} else {
			File file = new File(inputFile);
			if (!file.exists()) {
				System.err.println(String.format("File %s does not exist",
					inputFile));
				System.exit(1);
			}
			
			// follow a file as it grows
			// Intended for use with a stream of Records. As each Record is
			// added, it is processed.
			// Also works with Logs, but a whole log is needed before it is
			// processed. If your app quickly produces logs in a single file,
			// this may be usefull.
			if (options.follow()) {
				TailDashF tailDashF = new TailDashF(file);
				tailDashF.start();
				inputStream = tailDashF.getInputStream();
			} else {
				inputStream = new FileInputStream(inputFile);
			}
		}

		// set up the output
		OutputStream outputStream;
		String outputFile = options.getOutputFile();
		// "-" indicates stdout
		if ("-".equals(outputFile)) {
			outputStream = System.out;
		} else {
			outputStream =
				new FileOutputStream(outputFile);
		}
		if (outputStream == null) {
			System.err.println("can't create printStrewm");
			System.exit(1);
		}

		// TinyLoggerMapper has the required serializers and deserializes
		// registered for SDLevel and ZonedDateTime classes.
		ObjectMapper mapper = new TinyLoggerMapper();

		// If we want to adjust the date/time on deserialization, use the
		// current (default) timezone.
		if (options.getAdjustDateTimezoneOnDeserialize()) {
			mapper.setTimeZone(TimeZone.getDefault());
		} else {
			// leave the timestamps as generated
			mapper.disable(
				DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
		}

		/*
		** The input and output have been set up, and the mapper has been
		** configured. Create amd set the desired record formatter, and process
		** the log.
		*/
		StreamReader reader = new StreamReader(mapper);
		RecordFormatter formatter =
				new RecordFormatter(options.getFormat());
		reader.setFormatter(formatter);
		reader.readLog(inputStream, outputStream);
	}
}
