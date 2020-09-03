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
package jsonlogreader.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.logging.Level;
import javax.swing.text.DateFormatter;

/**
 *
 * @author ehetherington
 */
public class Record {
	private String isoDateTime;
	private Timespec timespec;
	private int sequence;
	private String logger;
	private Level level;
	private String file;
	private String function;
	private int line;
	private long threadId;
	private String threadName;
	String message;
	
	private DateTimeFormatter dateTimeFormatter =
//		DateTimeFormatter.ISO_DATE_TIME;
		DateTimeFormatter.ISO_ZONED_DATE_TIME;
	
//	// to read dates as OffsetDateTime, when configuring mapper...
//	mapper.registerModule(new JavaTimeModule());
//	private OffsetDateTime offsetDateTime;
	
	public Record() {
	}
	
	@JsonIgnore
	public Date getDate() {
		return getDateFromTimespec();
	}
	
	@JsonIgnore
	public Date getDateFromTimespec() {
		long millis = timespec.getSec() * 1000L;
		millis += timespec.getNsec() / 1000_000L;
		return new Date(millis);
	}
	
	@JsonIgnore
	public OffsetDateTime getOffsetDateTime() {
		OffsetDateTime t = null;
		t = OffsetDateTime.parse(isoDateTime);
		return t;
	}
	
	@JsonIgnore
	public ZonedDateTime getZonedDateTime() {
		ZonedDateTime t = null;
		try {
			t = ZonedDateTime.parse(isoDateTime);
			System.out.println("getZonedDateTime(): " + ZonedDateTime.parse(isoDateTime));
		} catch (DateTimeParseException e) {
		}
		return t;
	}

	/**
	 * @return the timespec
	 */
	public Timespec getTimespec() {
		return timespec;
	}

	/**
	 * @param timespec the timespec to set
	 */
	public void setTimespec(Timespec timespec) {
		this.timespec = timespec;
	}

	/**
	 * @return the sequence
	 */
	public int getSequence() {
		return sequence;
	}

	/**
	 * @param sequence the sequence to set
	 */
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	/**
	 * @return the logger
	 */
	public String getLogger() {
		return logger;
	}

	/**
	 * @param logger the logger to set
	 */
	public void setLogger(String logger) {
		this.logger = logger;
	}

	/**
	 * @return the level
	 */
	public Level getLevel() {
		return level;
	}
	
	/**
	 * @param level the level to set
	 */
	public void setLevel(Level level) {
		this.level = level;
	}

	/**
	 * @return the file
	 */
	public String getFile() {
		return file;
	}

	/**
	 * @param clazz the file to set
	 */
	public void setFile(String clazz) {
		this.file = clazz;
	}

	/**
	 * @return the function
	 */
	public String getFunction() {
		return function;
	}

	/**
	 * @param function the function to set
	 */
	public void setFunction(String function) {
		this.function = function;
	}

	/**
	 * @return the line
	 */
	public int getLine() {
		return line;
	}

	/**
	 * @param line the line to set
	 */
	public void setLine(int line) {
		this.line = line;
	}

	/**
	 * @return the threadId
	 */
	public long getThreadId() {
		return threadId;
	}

	/**
	 * @param threadId the threadId to set
	 */
	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}

	/**
	 * @return the threadName
	 */
	public String getThreadName() {
		return threadName;
	}

	/**
	 * @param threadName the threadName to set
	 */
	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the isoDateTime
	 */
	public String getIsoDateTime() {
		return isoDateTime;
	}

	/**
	 * @param isoDateTime the isoDateTime to set
	 */
	public void setIsoDateTime(String isoDateTime) {
		this.isoDateTime = isoDateTime;
	}
	
	/**
	 * Returns a string with all fields displayed.
	 * @return a string with all fields displayed.
	 */
	@Override
	public String toString() {
		
		return
//			getDate() + " " +
			dateTimeFormatter.format(getOffsetDateTime()) + " " +
			getLevel() + " " +
			getThreadId() + ":" + getThreadName() + " " +
			getFile() + ":" + getFunction() + ":" + getLine() + " " +
			message;
	}
	
	DateFormatter df = new DateFormatter();
}