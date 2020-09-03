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

/**
 * Equivalent to Linux/C struct other.
 * <p>
 * A <code>struct other</code> is obtained by <code>
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
public class Timespec {
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
	 * @return the sec
	 */
	public long getSec() {
		return sec;
	}

	/**
	 * @param sec the sec to set
	 */
	public void setSec(long sec) {
		this.sec = sec;
	}

	/**
	 * @return the nsec
	 */
	public long getNsec() {
		return nsec;
	}

	/**
	 * @param nsec the nsec to set
	 */
	public void setNsec(long nsec) {
		this.nsec = nsec;
	}
	
	/**
	 * Compute the difference between two <code>Timespec</code>.
	 * @param previous the <code>Timespec</code>
	 * @return the difference in nanoseconds
	 */
	public long diffNanos(Timespec previous) {
		long nanos = (sec * 1_000_000_000) + nsec;
		long previousNanos = (previous.sec * 1_000_000_000) + previous.nsec;
		return nanos - previousNanos;
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
	
	@Override
	public String toString() {
		return "sec = " + sec + ", nsec = " + nsec;
	}
}
