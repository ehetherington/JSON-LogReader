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

import java.util.Comparator;
import java.util.logging.Level;

/**
 * SD_Level adds Linux systemd logging levels to Level.
 * 
 * 
 * @author Edward Hetherington
 */
public class SDLevel extends Level {

	/**
	 * Assign intLevel 1300 to SD_EMERG "&lt;0&gt;"
	 */
	public static final Level EMERG = new SDLevel("EMERG", 1300);

	/**
	 * Assign intLevel 1200 to SD_ALERT "&lt;1&gt;"
	 */
	public static final Level ALERT = new SDLevel("ALERT", 1200);

	/**
	 * Assign intLevel 1100 to SD_CRIT "&lt;2&gt;"
	 */
	public static final Level CRIT = new SDLevel("CRIT", 1100);

	/**
	 * Assign intLevel 950 to SD_ERR "&lt;3&gt;"
	 */
	public static final Level ERR = new SDLevel("ERR", 950);

	/**
	 * Use existing WARNING for SD_WARNING "&lt;4&gt;"
	 */
//	public static final Level WARNING = new SDLevel("WARNING", 900);

	/**
	 * Assign intLevel 850 to SD_NOTICE "&lt;5&gt;"
	 */
	public static final Level NOTICE = new SDLevel("NOTICE", 850);

	/**
	 * Use existing INFO for SD_INFO "&lt;6&gt;"
	 */
//	public static final Level WARNING = new SDLevel("WARNING", 900);

	/**
	 * Assign intLevel 600 to SD_DEBUG "&lt;7&gt;"
	 */
	public static final Level DEBUG = new SDLevel("DEBUG", 600);
	
	private SDLevel(String label, int level) {
		super(label, level);
	}
	
	/**
     * Parse a level name string into a Level.
     * <p>
     * The argument string may consist of either a level name
     * or an integer value.
     * <p>
     * For example:
     * <ul>
     * <li>     "SEVERE"
     * <li>     "1000"
     * </ul>
     *
     * @param  name   string to be parsed
     * @throws NullPointerException if the name is null
     * @throws IllegalArgumentException if the value is not valid.
     * Valid values are integers between <CODE>Integer.MIN_VALUE</CODE>
     * and <CODE>Integer.MAX_VALUE</CODE>, and all known level names.
     * Known names are the levels defined by this class (e.g., <CODE>FINE</CODE>,
     * <CODE>FINER</CODE>, <CODE>FINEST</CODE>), or created by this class with
     * appropriate package access, or new levels defined or created
     * by subclasses.
     *
     * @return The parsed value. Passing an integer that corresponds to a known name
     * (e.g., 700) will return the associated name (e.g., <CODE>CONFIG</CODE>).
     * Passing an integer that does not (e.g., 1) will return a new level name
     * initialized to that value.
	 */
	public static synchronized Level parse(String name) throws IllegalArgumentException {
		return Level.parse(name);
	}
	
	/**
	 * A <code>Comparator</code> that may be useful. It compares based on the
	 * intLevel() values of the <code>Level</code>s..
	 */
	static public class IntValueComparator implements Comparator<Level> {

		@Override
		public int compare(Level o1, Level o2) {
			return o1.intValue() - o2.intValue();
		}
	}
}
