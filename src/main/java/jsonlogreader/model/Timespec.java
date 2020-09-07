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
import java.time.Instant;
import java.util.Date;

/**
 * Equivalent to Linux/C <code>struct timespec</code>.
 * <p> The <code>timespec</code> is provided for Linux/C situations because it
 * may be more convenient than parsing the isoDateTime field. There is more
 * information in the isoDateTime field as it contains the UTC offset. The UTC
 * offset may be added to the JSON log file to augment the <code>timespec</code>
 * in future updates.
 * </p>
 * <p>It is, however, useful for checking that the isoDateTime field is parsed
 * correctly.
 * <code>record.getInstant().compareTo(record.getTimespec().toInstant())</code>
 * should return 0.
 * <p>
 * A <code>struct timespec</code> is obtained in Linux/C by <code>
 * clock_gettime(log_config.clock_id, &amp;ts)</code>.
 * <p>
 * Structure members are:
 * <ul>
 * <li>     <code>time_t tv_sec</code>
 * <p>The number of whole seconds elapsed since the epoch (for a simple calendar
 *    time) or since some other starting point (for an elapsed time).</p>
 * <li>     <code>long int tv_nsec</code>
 * <p>The number of nanoseconds elapsed since the time given by the <code>tv_sec
 * </code> member.
 * <p>When <code>struct timespec</code> values are produced by GNU C
 * Library functions, the value in this field will always be greater than or
 * equal to zero, and less than 1,000,000,000. When <code>struct timespec</code>
 * values are supplied to GNU C Library functions, the value in this field must
 * be in the same range.</p>
 * </ul>
 * @author ehetherington
 */
public class Timespec implements Comparable<Timespec> {
	private long sec;	/* tv_sec */
	private long nsec;	/* tv_nsec */
	
	/**
	 * No-arg constructor needed to support de-serialization.
	 */
	public Timespec() {
	}
	
	/**
	 * Mirrors <code>struct timespec</code>.
	 * @param sec  The number of whole seconds elapsed since the epoch.
	 * @param nsec The number of nanoseconds elapsed since the time given by
	 *             the <code>sec</code> member. 
	 */
	public Timespec(long sec, long nsec) {
		this.sec = sec;
		this.nsec = nsec;
	}

	/**
	 * Get the <code>sec</code> field.
	 * @return the <code>sec</code>
	 */
	public long getSec() {
		return sec;
	}

	/**
	 * Set the <code>sec</code> field.
	 * @param sec the <code>sec</code> to set
	 */
	public void setSec(long sec) {
		this.sec = sec;
	}

	/**
	 * Get the <code>nsec</code> field.
	 * @return the <code>nsec</code>
	 */
	public long getNsec() {
		return nsec;
	}

	/**
	 * Set the <code>nsec</code> field.
	 * @param nsec the <code>nsec</code> to set
	 */
	public void setNsec(long nsec) {
		this.nsec = nsec;
	}
	
	/**
	 * Get an <code>Instant</code> equivalent to this <code>Timespec</code>. No
	 * information is lost, as <code>Instant</code> provides nanosecond
	 * resolution.
	 * @return that <code>Instant</code>
	 */
	@JsonIgnore
	public Instant toInstant() {
		return Instant.ofEpochSecond(sec, nsec);
	}

	/**
	 * Delegate the comparison to <code>Instant</code>. The two
	 * <code>Timespec</code>s are converted to <code>Instant</code>s and
	 * compared.
	 * @param other  the other <code>Timespec</code> to compare to
	 * @return the value returned by <code>Instant.compareTo()</code>
	 */
	@Override
	public int compareTo(Timespec other) {
		return toInstant().compareTo(other.toInstant());
	}
	
	/**
	 * Compute the difference between two <code>Timespec</code>s. This object
	 * will be the minuend, the other object will be the subtrahend.
	 * <p>
	 * Make sure each <code>Timespec</code> is less than <code>Long.MAX_VALUE /
	 * 1_000_000_000</code> seconds from the EPOCH. Otherwise the conversion of
	 * seconds to nanoseconds will overflow. That won't happend for about 292
	 * years.
	 * <p>
	 * @param other the other <code>Timespec</code> to subtract
	 * @return the difference in nanoseconds
	 */
	public long diffNanos(Timespec other) {
		long nanos = (sec * 1_000_000_000) + nsec;
		long otherNanos = (other.sec * 1_000_000_000) + other.nsec;
		return nanos - otherNanos;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
		if (!(object instanceof Timespec)) return false;
		Timespec other = (Timespec) object;
		return
			((this.sec == other.sec) &&
			(this.nsec == other.nsec));
	}
	
	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + (int) sec;
		result = 31 * result + (int) nsec;
		return result;
	}
	
	/**
	 * Get a String representation of this <code>Timespec</code>.
	 * @return that <code>String</code>
	 */
	@Override
	public String toString() {
		return "sec = " + sec + ", nsec = " + nsec;
	}
	
	/**
	 * Get a <code>Date</code>. A convenience method. Information is lost, as <code>Date</code> has
	 * only millisecond resolution.
	 * @return a <code>Date</code> with the nanos field converted to
	 * milliseconds by truncation (no rounding).
	 */
	@JsonIgnore
	public Date getDate() {
		return Date.from(toInstant());
	}
	
	
//	@JsonIgnore
//	private Date getDateXX() {
//		long millis = sec * 1000L;
//		millis += nsec / 1000_000L;
//		return new Date(millis);
//	}
}
