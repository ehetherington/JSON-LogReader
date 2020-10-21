/*
 * The MIT License
 *
 * Copyright 2020 ehetherington.
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
import jsonlogreader.model.SDLevelJsonDeserializer;
import jsonlogreader.model.Record;
import jsonlogreader.model.Log;
import jsonlogreader.model.SDLevelJsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * A program to read JSON log file produced by libtinylogger. It certainly is
 * not "production ready", but is a guide to reading those files with jackson.
 * It will not fail gracefully with a deliberately broken log file, but will
 * read logs as produced by libtinylogger.
 * 
 * <p>A starting point for jackson is:
 * <a href="https://github.com/FasterXML/jackson-databind">jackson-databind</a>
 * </p>
 * 
 * <p> The options currently imply more than is currently supported. But they
 * are planned to be implemented.</p>
 * 
 * Enhancements planned:
 * <ul>
 * <li>follow requested
 * <p>Monitor an input file and print the messages as they are appended to the
 * input file.</p>
 * </li>
 * <li>gui requested
 * <p> A simple gui to select a file to read and which tinylogger format to use
 * for output.
 * </p>
 * <li>output file
 * <p>Specify an output file</p>
 * </li>
 * </ul>
 * 
 * 
 * @author ehetherington
 * @see <a href="https://github.com/FasterXML/jackson-databind">jackson-databind</a>
 * @see <a href="https://github.com/FasterXML/jackson-core">jackson-core</a>
 */
public class JsonLogReader {

	/**
	 * Read a log file generated by libtinylogger using the Json format.
	 * @param args the command line arguments
	 * @throws java.io.IOException on IO errors
	 */
	public static void main(String[] args) throws IOException {
		LogReaderOptions options = new LogReaderOptions();
		options.parseOptions(args);
		
		// set up the input
		InputStream inputStream = null;
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
		
		// set up the output
		PrintStream printStream = null;
		String outputFile = options.getOutputFile();
		// "-" indicates stdout
		if ("-".equals(outputFile)) {
			printStream = System.out;
		} else {
			printStream =
				new PrintStream(new FileOutputStream(outputFile), true);
		}

		// The Records hava a level field - convert them to
		// java.util.logging.Level objects.
		// serializer and deserializer for the Level objects
		// deserializer for SDLevel
		SimpleModule sdLevelModule = new SimpleModule("SDLevelModule");
		sdLevelModule.addSerializer(Level.class, new SDLevelJsonSerializer());
		sdLevelModule.addDeserializer(Level.class, new SDLevelJsonDeserializer());

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(sdLevelModule);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		// to disable writing java.util.Date, Calendar as number (timestamp):
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		// to adjust input timestamps to the local timezone
		// the JsonLogReader option defaults to no adjustment (use -a option to
		// turn it on)
		if (options.getAdjustDateTimezoneOnDeserialize()) {
			mapper.setTimeZone(TimeZone.getDefault());
		} else {
			// leave the timestamps as generated
			mapper.disable(
				DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
		}

		// to serialize / deserialize OffsetDateTime (and other JSR-8601 stuff)
		mapper.registerModule(new JavaTimeModule());
		
		// read the file
		Log log = mapper.readValue(inputStream, Log.class);
		
		// print out the records in the command line selected message format
		// (log_fmg_debug_tall by default, or set by the -f option)
		if (log != null) {
			RecordFormatter formatter =
				new RecordFormatter(options.getFormat());
			for (Record r: log.getRecords()) {
				printStream.println(formatter.format(r));
			}
		}
	}
	
}
