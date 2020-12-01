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
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import jsonlogreader.RecordFormatter;
import jsonlogreader.model.Log;
import jsonlogreader.model.Record;
import jsonlogreader.model.TinyLoggerMapper;

/**
 * Demonstrate the effects of
 * <code>DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE</code>.
 * <p>
 * When <code>com.fasterxml.jackson.databind.ObjectMapper</code> deserializes
 * input, it defaults to adjusting timezone information in Java 8 date/time
 * data types like <code>ZonedDateTime</code>, to the mapper's
 * <code>Context</code>'s timezone. That is UTC by default.</p>
 * <p>
 * If you want those objects to retain their timezone info as written, The
 * ADJUST_DATES_TO_CONTEXT_TIME_ZONE feature must be disabled. For example:</p>
 * <pre>
 *	mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
 *	Log log = mapper.readValue(sample, Log.class);
 * </pre>
 * <p>
 * If you want them to be adjusted to a timezone other than UTC, leave that
 * feature enabled, AND set the context timezone to the desired timezone, For
 * example, if you want them adjusted to the local timezone, you would use the
 * following:
 * </p>
 * <pre>
 *	mapper.setTimeZone(TimeZone.getDefault());
 *	Log log = mapper.readValue(sample, Log.class);
 * </pre>
 * @author Edward Hetherington
 */
public class TimeZoneDemo {
	
	TimeZoneDemo(String sample) {
		List<TimeZone> timeZones = new ArrayList<>();
		timeZones.add(TimeZone.getTimeZone("Europe/Paris"));
		timeZones.add(TimeZone.getTimeZone("UTC"));	// ObjectMapper's default
		timeZones.add(TimeZone.getTimeZone("America/New_York"));
		boolean[] disableAdjustTimezone = {false, true};
		
		for (Boolean disableAdjust: disableAdjustTimezone) {
			System.out.format("%n========== DisableAdjustDatesToContext = %b%n",
				disableAdjust);
			System.out.format("========== record timestamps will%s be adjusted " +
					"to mapper context timezone%n",
				disableAdjust ? " not" : "");
			for(TimeZone tz: timeZones) {
				TinyLoggerMapper mapper = new TinyLoggerMapper();
				if (disableAdjust) {
					mapper.disable(
						DeserializationFeature.
							ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
				}
				mapper.setTimeZone(tz);
				SampleReader reader = new SampleReader(sample, mapper);
				System.out.format("%n===== Mapper Context Timezone = %s, " +
						"adjust to context = %b%n",
					tz.getID(), ! disableAdjust);
				try {
					reader.readSample();
				} catch (IOException ex) {
					Logger.getLogger(TimeZoneDemo.class.getName())
						.log(Level.SEVERE, null, ex);
				}
			}
		}
	}
	
	/**
	 * Read the sample text with the pre-configured mapper. Display the
	 * processed records to System.out.
	 */
	class SampleReader {
		String sample;
		ObjectMapper mapper;
		
		SampleReader(String path,
			ObjectMapper mapper) {
			this.sample = path;
			this.mapper = mapper;
		}
		
		boolean readSample() throws IOException {
			Log log = mapper.readValue(sample, Log.class);
			
			if (log == null) return false;
			
			RecordFormatter formatter =
				new RecordFormatter(RecordFormatter.StandardFormat.TIMESTAMP_DEMO);
			
			for(Record record: log.getRecords()) {
				System.out.println("Record Timezone: " +
					record.getZonedDateTime().getZone());
				System.out.println(formatter.format(record));
			}
			
			return true;
		}
	}
	
	private static String readFile (String pathname) throws IOException {
		StringBuilder sample = new StringBuilder();
		try (Stream<String> stream = Files.lines(Paths.get(pathname),
			StandardCharsets.UTF_8)) {
			stream.forEach(s -> sample.append(s).append("\n"));
		}
		return sample.toString();
	}

	/**
	 * Read a JSON format log file generated by libtinylogger.
	 * The sample text is then "played back" through differently configured
	 * <code>com.fasterxml.jackson.databind.ObjectMapper</code> mappers to show
	 * the effect of disabling the <code>ADJUST_DATES_TO_CONTEXT_TIME_ZONE</code>
	 * feature and setting different mapper context timezones.
	 * <p>
	 * If an argument is given, it must be the pathname of one such file.
	 * If no argument is given, the compiled in sample is read.
	 * </p>
	 * @param args the command line arguments
	 * @throws java.io.IOException on IO errors
	 */
	public static void main(String[] args) throws IOException {
		String sample = null;
		if (args.length == 0) {
			sample = Example.sample;
		} else {
			try {
				sample = readFile(args[0]);
			} catch (IOException e) {
				System.err.format("could not read %s: %s%n",
					args[0], e.getCause());
			}
		}
		
		// run the demo
		if (sample != null) new TimeZoneDemo(sample);
		
		// if you want to generate the sample file here instead of using
		// libtinylogger's json-timezones example program...
		if (false) {
			File file = new File("timestamp-demo-file.json");
			PrintStream ps = new PrintStream(file);
			ps.print(Example.sample);
			ps.close();
		}
	}

	/**
	 * libtinylogger has an example program called json-timezones.c. It produces
	 * a test file for this program, and its output was included here.
	 * <p>
	 * It uses a modified version of json_formatter.c which gets the timezone
	 * for every log message. json-timezone.c sets the environment TZ variable
	 * to Europe/Paris, UTC, and America/New_York. After setting each timezone,
	 * it then logs a message. So, each message has essentially the same time,
	 * but in a different timezone.
	 * <p>
	 * Note: the 200 microsecond time differences from message to message are
	 * due to the several file system accesses, including a file read for each
	 * timezone, and not an indication of message rate performance.
	 * Determination of zone info is normally done only once at library
	 * "initialization".
	 * <p>
	 * Note: The SIMULATED timestamps <i>in</i> the message are not supposed to
	 * exactly match the ACTUAL timestamps <i>of</i> the message. The message
	 * content is constructed first with one clock reading, then it is sent
	 * later and gets another clock reading.
	 */
	static public class Example {
		/**
		 * Example is public just to get the JavaDoc. It is not intended to be
		 * instantiated.
		 */
		private Example() {}
		
		static String sample =
"{" + "\n" +
"  \"records\" : [  {" + "\n" +
"    \"isoDateTime\" : \"2020-10-16T05:47:21.822070496+02:00[Europe/Paris]\"," + "\n" +
"    \"timespec\" : {" + "\n" +
"      \"sec\" : 1602820041," + "\n" +
"      \"nsec\" : 822070496" + "\n" +
"    }," + "\n" +
"    \"sequence\" : 1," + "\n" +
"    \"logger\" : \"tinylogger\"," + "\n" +
"    \"level\" : \"INFO\"," + "\n" +
"    \"file\" : \"json-timezones.c\"," + "\n" +
"    \"function\" : \"main\"," + "\n" +
"    \"line\" : 42," + "\n" +
"    \"threadId\" : 867959," + "\n" +
"    \"threadName\" : \"json-timezones\"," + "\n" +
"    \"message\" : \"Logged at 2020-10-16 05:47:21.822053831+02:00 in Europe/Paris\"" + "\n" +
"  },  {" + "\n" +
"    \"isoDateTime\" : \"2020-10-16T03:47:21.822282682+00:00[UTC]\"," + "\n" +
"    \"timespec\" : {" + "\n" +
"      \"sec\" : 1602820041," + "\n" +
"      \"nsec\" : 822282682" + "\n" +
"    }," + "\n" +
"    \"sequence\" : 2," + "\n" +
"    \"logger\" : \"tinylogger\"," + "\n" +
"    \"level\" : \"INFO\"," + "\n" +
"    \"file\" : \"json-timezones.c\"," + "\n" +
"    \"function\" : \"main\"," + "\n" +
"    \"line\" : 47," + "\n" +
"    \"threadId\" : 867959," + "\n" +
"    \"threadName\" : \"json-timezones\"," + "\n" +
"    \"message\" : \"Logged at 2020-10-16 03:47:21.822268612+00:00 in UTC (or thereabouts)\"" + "\n" +
"  },  {" + "\n" +
"    \"isoDateTime\" : \"2020-10-15T23:47:21.822478983-04:00[America/New_York]\"," + "\n" +
"    \"timespec\" : {" + "\n" +
"      \"sec\" : 1602820041," + "\n" +
"      \"nsec\" : 822478983" + "\n" +
"    }," + "\n" +
"    \"sequence\" : 3," + "\n" +
"    \"logger\" : \"tinylogger\"," + "\n" +
"    \"level\" : \"INFO\"," + "\n" +
"    \"file\" : \"json-timezones.c\"," + "\n" +
"    \"function\" : \"main\"," + "\n" +
"    \"line\" : 52," + "\n" +
"    \"threadId\" : 867959," + "\n" +
"    \"threadName\" : \"json-timezones\"," + "\n" +
"    \"message\" : \"Logged at 2020-10-15 23:47:21.822475920-04:00 in America/New_York\"" + "\n" +
"  } ]" + "\n" +
"}" + "\n"
;
	}
	
}
