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

import java.time.ZonedDateTime;

/**
 * A simple header. It is optionally generated depending on the tinylogger build
 * options.
 * <br>
 * Fields are:
 * <ul>
 * <li> <code>startDate</code>
 * <p>A timestamp created when the LOG_CHANNEL is opened</p>
 * <li>     <code>hostname</code>
 * <p>The hostname from /proc/sys/kernel/hostname</p>
 * 
 * <li>     <code>notes</code>
 * <p>Text set with <code>log_set_json_notes()</code> <i>before</i> the channel
 * was opened.</p>
 * </ul>
 * @author Edward Hetherington
 */
public class Header {
	private ZonedDateTime startDate;
	private String hostname = null;
	private String notes = null;

	/**
	 * read from /proc/sys/kernel/hostname
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * @param hostname the hostname to set
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * If notes were not set by <code>log_set_json_notes()</code>, null will be
	 * returned.
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * A timestamp created when the LOG_CHANNEL is opened
	 * @return the startDate
	 */
	public ZonedDateTime getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(ZonedDateTime startDate) {
		this.startDate = startDate;
	}
	
	/**
	 * Convenience/development method.
	 * A simple format:
	 * <br>label=value pairs separated by newline with no trailing newline.
	 * <pre>
	 * startDate=2020-10-26T23:00:05.107196674-04:00[America/New_York]
	 * hostname=sambashare
	 * comment=hello
	 * </pre>
	 * @return the appropriate String
	 */
	@Override
	public String toString() {
		return String.format("startDate=%s%nhostname=%s%ncomment=%s",
			getStartDate(), getHostname(), getNotes());
	}
	
}
