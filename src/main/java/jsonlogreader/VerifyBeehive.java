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

import jsonlogreader.model.Record;
import jsonlogreader.model.Log;
import jsonlogreader.model.Timespec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import jsonlogreader.model.SDLevelJsonDeserializer;
import jsonlogreader.model.SDLevelJsonSerializer;

/**
 * Verify several aspects of a JSON formatted log file produced by the
 * libtinylogger <code>beehive</code> program.
 * 
 * <code>beehive</code> creates 250 worker threads that each write 1001
 * messages. The main thread also writes 250 messages. The total is 250 + (250 *
 * 1001) = 250500 messages.
 * 
 * <p>Aspects verified:</p>
 * <ul>
 * <li>message order
 * <p>Each message is assigned a sequence number starting at 1. Verify that all
 * messages were logged in the proper order.</p>
 * </li>
 * <li>timestamp consistency
 * <p>Each message has two timestamps. One is parse from the isoDateTime string.
 * The other is the <code>timespec</code> structure. Verify that the parsed
 * isoDateTime and the timespec value are equal.</p>
 * </li>
 * <li>timestamp order
 * <p>Verify that the timestamps are in increasing order from message to
 * message.</p>
 * <li>1 main thread
 * <p>Veriry that the main thread produces 250 messages.</p>
 * </li>
 * <li>250 worker threads
 * <p>Veriry that 250 worker thread produce 1001 messages.</p>
 * </li>
 * <li>total number of threads
 * <p>Verify that there are 1 main + 250 worker threads.</p>
 * </ul>
 * @author ehetherington
 */
public class VerifyBeehive {
	private static final boolean DEBUG = false;
	
	private static final int N_WORKERS		= 250;     /**< number of worker threads to run */
	private static final int N_LOOPS		= 1000;    /**< number of loops for each thread to run */
	private static final int SLEEP_MICROS	= 500;     /**< maximum sleep duration for each loop */

	/**
	 * @param args the command line arguments
	 * @throws java.io.IOException when something bad happens
	 */
	public static void main(String[] args) throws IOException {
		if (DEBUG)  args = new String[] {"beehivexxx.json"};
		if (args.length != 1) {
			System.err.println("Must supply JSON log filename");
			System.exit(1);
		}
		File file = new File(args[0]);
		if (!file .exists()) {
			System.err.format("file %s does not exist%n", args[0]);
		}
		
		SimpleModule sdLevelModule = new SimpleModule("SDLevelModule");
		sdLevelModule.addSerializer(Level.class, new SDLevelJsonSerializer());
		sdLevelModule.addDeserializer(Level.class, new SDLevelJsonDeserializer());

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(sdLevelModule);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		// to disable writing java.util.Date, Calendar as number (timestamp):
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		
		// to serialize / deserialize OffsetDateTime (and other JSR-8601 stuff)
		mapper.registerModule(new JavaTimeModule());
		
		mapper.setTimeZone(TimeZone. getDefault());
		
		Log log = mapper.readValue(file, Log.class);
		
		Inspector inspector = new Inspector();
		inspector.inspect(log);
		Reporter reporter = new Reporter();
		reporter.report(log, inspector);
	}
	
	static class Histo {
		List<Integer> values = new ArrayList<>();
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		long sum = 0;
		boolean add(int value) {
			if (value < min) min = value;
			if (value > max) max = value;
			sum += value;
			values.add(value);
			return value >= 0;	// flag negative values
		}
		double getMean() {
			return (double) sum / (double) ((values.size()));
		}
		int[] getHisto(int nBins, int binSize) {
			if ((nBins < 1) || (binSize < 1)) return null;
			int[] bins = new int[nBins];
			int bin;
			for (int n = 0; n < nBins; n++) bins[n] = 0;
			for (Integer value: values) {
				// clip positive values
				bin = Integer.min(value / binSize, nBins - 1);
				// clip negative values
				bin = Integer.max(0, bin);
				bins[bin]++;
			}
			return bins;
		}
		@Override
		public String toString() {
			final int N_BINS = 50;
			final int BIN_SIZE = 1000;	// 1000 nanos = 1 microseconds
			int[] histo = getHisto(N_BINS, BIN_SIZE);
			StringBuilder sb = new StringBuilder();
			sb.append("n delta = ").append(values.size()).append("\n")
				.append("Mean: ").append(getMean()).append("\n");
			// deltaTimeHisto x in microsends
			for (int n = 0; n < N_BINS; n++) {
				sb.append((n * BIN_SIZE) / 1_000.000).append(", ")
					.append(histo[n]).append("\n");
			}
			return sb.toString();
		}
	}
	
	static class Inspector {
		// calculate average message to message time
		Histo deltaTimeHisto = new Histo();
		Timespec lastTimespec = null;
		
		// sequence numbers start at 1
		int expectedSequence = 1;
		// sort out log records by threadId
		// <threadId, List<Record>>
		Map<Long, List<Record>> threadMap = new LinkedHashMap<>();
		// keep track of the date/time mismatches
		List<Record> dateTimeMismatches = new ArrayList<>();
		// keep track of sequence erros
		// <expectedSequence, record>
		Map<Integer, Record> sequenceErrors = new LinkedHashMap<>();
		// keep track of timestamp deltas that are negative
		List<Record> timeSequenceErrors = new ArrayList<>();
		// make histo of records per thread
		// <nRecords, count>
		Map<Integer, Integer> countMap = new HashMap<>();
		
		void inspect(Log log) {
			for (Record r: log.getRecords()) {
				// sort out log records by threadId
				List<Record> msgs = threadMap.computeIfAbsent(r.getThreadId(),
						(unused)-> new ArrayList<>());
				msgs.add(r);
				
				// check the sequence numbers
				if (r.getSequence() != expectedSequence) {
					sequenceErrors.put(expectedSequence, r);
				}
				expectedSequence++;
			
				// do stats on time diffs
				Timespec current = r.getTimespec();
				if (lastTimespec != null) {
					long diffTime = current.diffNanos(lastTimespec);
					if (diffTime < 0) {
						timeSequenceErrors.add(r);
					}
					deltaTimeHisto.add((int) diffTime);
				}
				lastTimespec = current;
				
				// check that the parsed isoDateTime matches the timespec
				Instant timestampInstant = r.getInstant();
				Instant timespecInstant = r.getTimespec().toInstant();
				if (!timestampInstant.equals(timespecInstant)) {
					dateTimeMismatches.add(r);
				}
			}

			// make a histo of thread message counts
			Collection<List<Record>> threads = threadMap.values();
			for(List<Record> records: threads) {
				Integer count = countMap.computeIfAbsent(records.size(),
						(unused)-> 0);
				countMap.put(records.size(), count + 1);
			}
		}
	}
	
	static class Reporter {
		void report(Log log, Inspector inspector) {
//			System.out.format("deltaTimeMean = %.12f%n",
//				inspector.deltaTimeHisto.getMean());
			System.out.format("sequenceErrors: %d %s%n",
				inspector.sequenceErrors.size(),
				inspector.sequenceErrors.size() == 0 ? "PASSED" : "FAILED");
			
			System.out.format("timeSequenceErrors: %d %s%n",
				inspector.timeSequenceErrors.size(),
				inspector.timeSequenceErrors.size() == 0 ? "PASSED" : "FAILED");
			
			System.out.format("dateTimeMismatches = %d %s%n",
				inspector.dateTimeMismatches.size(),
				inspector.dateTimeMismatches.size() == 0 ? "PASSED" : "FAILED");
			
			int nMsgsExpected = N_WORKERS + (N_WORKERS * (N_LOOPS + 1));
			System.out.format("nRecords = %d %s%n",
				log.getRecords().size(),
				log.getRecords().size() == nMsgsExpected ? "PASSED" : "FAILED");

			// make sure the main thread issued N_WORKERS "waiting for..." messages
			Integer mainCount = inspector.countMap.getOrDefault(N_WORKERS, 0);
			boolean mainCountTest = mainCount == 1;
			System.out.format("expected 1 main thread with %d messages, got %d %s %n",
				N_WORKERS, mainCount,
				mainCountTest ? "PASSED" : "FAILED");

			// Maker sure we get N_WORKERS threads with N_LOOPS + 1 messages
			Integer workerCount = inspector.countMap.getOrDefault(N_LOOPS + 1, 0);
			boolean workerCountTest = workerCount == N_WORKERS;
			System.out.format("expected %d workers with %d messages, got %d %s %n",
				N_WORKERS, N_LOOPS + 1, workerCount,
				workerCountTest ? "PASSED" : "FAILED");
			
			// Total threads = 1 main thread + N_WORKERS
			boolean threadCountTest = N_WORKERS + 1 == inspector.threadMap.size();
			System.out.format("Expected %d threads, got %d %s%n",
					N_WORKERS + 1, inspector.threadMap.size(),
					threadCountTest ? "PASSED" : "FAILED");
		}
	}
	
}
