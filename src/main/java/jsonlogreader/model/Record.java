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
package jsonlogreader.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.Level;

/**
 * The model for the log messages.
 * <br><br>
 * <ul>
 * <li>isoDateTime
 * <p>The timestamp of the record. This is de-serialized as a
 * <code>ZonedDateTime</code>. Libtinylogger currently generates
 * <code>OffsetDateTime</code>, but may add the zone info (such as
 * <code>[America/New_York]</code> in the future.<br>
 * Example:<br>
 * <code>"isoDateTime" : "2020-09-05T18:46:16.367593137-04:00"</code>
 * </p>
 * </li>
 * <li>timespec
 *   <ul>
 * 		<li><code>time_t tv_sec</code>
 *		<li><code>long int tv_nsec</code>
 *	 </ul>
 *   <p>This is described in the <code>Timespec</code> class.</p>
 * </li>
 * <li>sequence
 *   <p> The sequence number of the record in the <code>Log</code>, starting at
 *        1.
 *   </p>
 * </li>
 * <li><code>logger</code>
 * <p> The string <code>libtinylogger</code>. The library version will probably
 *     be appended in the future.
 * </p>
 * </li>
 * <li><code>level</code>
 * <p> De-serialized as <code>Level</code>. Extra levels are added in
 *     <code>SDLevel</code> to represent systemd logging levels.
 * </p>
 * </li>
 * <li><code>file</code>
 * <p> The source code file captured by the <code>__FILE__</code> macro.
 * </p>
 * </li>
 * <li><code>function</code>
 * <p> The source code file captured by the <code>__function__</code> macro.
 * </p>
 * </li>
 * <li><code>line</code>
 * <p> The source code file captured by the <code>__LINE__</code> macro.
 * </p>
 * </li>
 * 
 * <li><code>threadId</code>
 * <p>The thread id of the calling thread. This is the Linux thread id, not the
 * Pozix one.
 * </p>
 * </li>
 * 
 * <li><code>threadName</code>
 * <p>The thread name of the calling thread.
 * </p>
 * 
 * <li><code>message</code>
 * <p>Last, but not least, the user message.
 * </p>
 * </li>
 * 
 * </ul>
 * 
 * @author Edward Hetherington
 */
@JsonPropertyOrder({ "isoDateTime" })	// apparently @JsonProperty() goes last
public class Record {
	private ZonedDateTime zonedDateTime;
	private Timespec timespec;
	private int sequence;
	private String logger;
	private Level level;
	private String file;
	private String function;
	private int line;
	private long threadId;
	private String threadName;
	private String message;
	
	/**
	 * Get the <code>ZonedDateTime</code> representing the isoDateTime from the
	 * log message.
	 * @return the <code>OffsetDateTime</code>
	 */
	@JsonProperty("isoDateTime")
	public ZonedDateTime getZonedDateTime() {
		return zonedDateTime;
	}
	
	/**
	 * Set the <code>OffsetDateTime</code> representing the isoDateTime from the
	 * log message.
	 * @param zonedDateTime the <code>ZonedDateTime</code> to set
	 */
	@JsonProperty("isoDateTime")
	public void setZonedDateTime(ZonedDateTime zonedDateTime) {
		this.zonedDateTime = zonedDateTime;
	}

	/**
	 * Get the <code>Timespec</code> from the record.
	 * @return that <code>Timespec</code>
	 */
	public Timespec getTimespec() {
		return timespec;
	}

	/**
	 * Set the <code>Timespec</code> for the record.
	 * @param timespec the <code>Timespec</code> to set
	 */
	public void setTimespec(Timespec timespec) {
		this.timespec = timespec;
	}

	/**
	 * Get the sequence number of the record.
	 * @return the sequence
	 */
	public int getSequence() {
		return sequence;
	}

	/**
	 * Set the sequence number of the record.
	 * @param sequence the sequence to set
	 */
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	/**
	 * Get the logger string from the record.
	 * @return the logger string
	 */
	public String getLogger() {
		return logger;
	}

	/**
	 * Set the logger string in the record.
	 * @param logger the logger string to set
	 */
	public void setLogger(String logger) {
		this.logger = logger;
	}

	/**
	 * Get the logging <code>Level</code> of the record.
	 * @return the <code>Level</code> of the record.
	 */
	public Level getLevel() {
		return level;
	}
	
	/**
	 * Set the logging <code>Level</code> of the record.
	 * @param level the <code>Level</code> to set
	 */
	public void setLevel(Level level) {
		this.level = level;
	}

	/**
	 * Get the filename of the calling code.
	 * @return the filename of the calling code.
	 */
	public String getFile() {
		return file;
	}

	/**
	 * Get the filename of the calling code.
	 * @param file the file to set
	 */
	public void setFile(String file) {
		this.file = file;
	}

	/**
	 * Get the function of the calling code.
	 * @return the function of the calling code
	 */
	public String getFunction() {
		return function;
	}

	/**
	 * Set the function name of the calling code.
	 * @param function the function name to set
	 */
	public void setFunction(String function) {
		this.function = function;
	}

	/**
	 * Get the line number of the calling code.
	 * @return the line number of the calling code
	 */
	public int getLine() {
		return line;
	}

	/**
	 * Set the line number of the calling code.
	 * @param line the line number to set
	 */
	public void setLine(int line) {
		this.line = line;
	}

	/**
	 * Get the thread id of the calling code.
	 * @return the threadId of the calling code
	 */
	public long getThreadId() {
		return threadId;
	}

	/**
	 * Set the thread id of the calling code.
	 * @param threadId the threadId to set
	 */
	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}

	/**
	 * Get the thread name of the calling code.
	 * @return the threadName of the calling code
	 */
	public String getThreadName() {
		return threadName;
	}

	/**
	 * Set the thread name of the calling code.
	 * @param threadName the threadName to set
	 */
	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	/**
	 * Get the actual message from the record.
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Set the actual message in the record.
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * Returns a string with all fields displayed.
	 * @return a string with all fields displayed.
	 */
	@Override
	public String toString() {
		
		return
			DateTimeFormatter.ISO_ZONED_DATE_TIME.format(zonedDateTime) + " " +
			getLevel() + " " +
			getThreadId() + ":" + getThreadName() + " " +
			getFile() + ":" + getFunction() + ":" + getLine() + " " +
			message;
	}
	
	/* ===== convenience methods ===== */
	
	/**
	 * Get the <code>Date</code> derived from the <code>ZonedDataTIme</code>.
	 * This is just a convenience method.
	 * @return that <code>Date</code>
	 */
	@JsonIgnore
	public Date getDate() {
		return new Date(zonedDateTime.toInstant().toEpochMilli());
	}
	
	/**
	 * Get the <code>Instant</code> derived from the <code>ZonedDataTIme</code>.
	 * This is just a convenience method.
	 * @return that <code>Instant</code>
	 */
	@JsonIgnore
	public Instant getInstant() {
		return zonedDateTime.toInstant();
	}
}