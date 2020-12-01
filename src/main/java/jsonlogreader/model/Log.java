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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

/**
 * The tinylogger json log file is a <code>Log</code>, which is a list of <code>
 * Record</code>.
 * 
 * @author Edward Hetherington
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Log {
	private Header header = null;
	private List<Record> records = new ArrayList<>();

	/**
	 * Retrieve the header
	 * @return the header
	 */
	public Header getHeader() {
		return header;
	}

	/**
	 * Set the header
	 * @param header the header to set
	 */
	public void setHeader(Header header) {
		this.header = header;
	}

	/**
	 * Retrieve the list of records.
	 * @return the records
	 */
	public List<Record> getRecords() {
		return records;
	}

	/**
	 * Set the list of records.
	 * @param records the records to set
	 */
	public void setRecords(List<Record> records) {
		this.records = records;
	}

	/**
	 * Add a record.
	 * @param record the record to add
	 */
	public void add(Record record) {
		records.add(record);
	}
}
