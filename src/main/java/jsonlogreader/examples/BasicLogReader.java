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
package jsonlogreader.examples;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.TimeZone;
import jsonlogreader.LogReaderOptions;
import jsonlogreader.RecordFormatter;
import jsonlogreader.model.Log;
import jsonlogreader.model.Header;
import jsonlogreader.model.Record;
import jsonlogreader.model.TinyLoggerMapper;

/**
 * Basic example to read a log file with a single Log object.
 * 
 * @author Edward Hetherington
 */
public class BasicLogReader {
	/**
	 * Basic example. Reads the first Log object from a file or the input
	 * stream.
	 * 
	 * @param args file name and several options
	 * @throws IOException on any I/O errors
	 */
	static public void main(String[] args) throws IOException {
		
		/////////////////////////////////////////////////////////////
		//           get the command line options
		/////////////////////////////////////////////////////////////
		LogReaderOptions options = new LogReaderOptions();
		options.parseOptions(args);

		// get a source
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
			inputStream = new FileInputStream(inputFile);
		}

		// get a destination
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
		
		// We need to select the UTF-8 chaset in case the input isn't just
		// ASCII.
		final PrintWriter writer =
			new PrintWriter(
			new BufferedWriter(
			new OutputStreamWriter(outputStream,
				StandardCharsets.UTF_8)), true);
		
		// pick a record formatter
		RecordFormatter formatter = new RecordFormatter(options.getFormat());
		
		/////////////////////////////////////////////////////////////
		//       Get a jackson ObjectMapper to parse the input
		/////////////////////////////////////////////////////////////
		ObjectMapper mapper = new TinyLoggerMapper();

		/////////////////////////////////////////////////////////////
		//     Do we want to adjust the time zones to localtime
		//             or leave them as created?
		/////////////////////////////////////////////////////////////
		if (options.getAdjustDateTimezoneOnDeserialize()) {
			mapper.setTimeZone(TimeZone.getDefault());
		} else {
			// leave the timestamps as generated
			mapper.disable(
				DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
		}
		
		/////////////////////////////////////////////////////////////
		//                  Read the first Log object
		/////////////////////////////////////////////////////////////
		Log log = mapper.readValue(inputStream, Log.class);
		if (log == null) {
			System.err.println("Ooops! no log");
			System.exit(1);
		}
		
		/////////////////////////////////////////////////////////////
		//                  Print the Log
		/////////////////////////////////////////////////////////////
			
		// print the header, if present
		Header header = log.getHeader();
		if (header != null) {
			writer.println(header);
		}
		
		// print the records
		List<Record> records = log.getRecords();
		records.forEach(r->writer.println(formatter.format(r)));
		
		System.exit(0);
	}
	
}
