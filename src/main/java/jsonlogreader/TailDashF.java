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
package jsonlogreader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

/**
 * Monitor a growing file and ...
 * @author Edward Hetherington
 */
public class TailDashF {
	private static final Logger log =
		Logger.getLogger("jsonlogreader.TailDashF");
	// this works without specifying UTF_8, but it can't hurt...
	private static final Charset charset = StandardCharsets.UTF_8;
	private static final int POLL_RATE = 50; // milliseconds
	private static int threadCount = 0;
	
	private TailerListener tailerListener = null;
	private Tailer tailer = null;
	private Thread thread = null;
	
	public TailDashF(File file) {
		tailerListener = new TailerListener();
		tailer = new Tailer(file, charset,
			tailerListener, POLL_RATE, false, false, 4096);
		thread = new Thread(tailer);
		thread.setDaemon(true); // jvm exits if only daemon threads are alive
		thread.setName("Tailer" + threadCount++);
	}
	
	public void start() {
		thread.start();
	}
	
	/**
	 * Get the InputSream.
	 * @return the InputSream
	 */
	public  InputStream getInputStream() {
		return tailerListener.getInputStream();
	}
	
	@SuppressWarnings("deprecation")
	public void stop() {
		if (thread == null) {
			throw new IllegalStateException();
		}
		
		tailerListener.close();	// close the input and output streams
		tailer.stop();
		thread.stop();	// todo: use thread.interrupt, etc (see javadoc)
	}
	
	/**
	 * TailerListener REPLACES the newlines that Tailer removes.
	 * This is useful because the parser can give errors that point to the
	 * actual input line, rather that a large character position in line 1.
	 */
	private class TailerListener extends TailerListenerAdapter {
		private PipedInputStream is = null;
		private PipedOutputStream os = null;
		
		TailerListener() {
			is = new PipedInputStream();
			os = new PipedOutputStream();
			try {
				os.connect(is);
			} catch (IOException ex) {
				log.log(Level.SEVERE, "MyTailListener<init>", ex);
			}
		}

		@Override
		public void handle(String line) {
			try {
				os.write(String.format("%s%n", line).getBytes(charset));
				os.flush();
			} catch (IOException ex) {
				log.log(Level.SEVERE, "TailDashF.handle()", ex);
			}
		}
		
		public void close() {
			try {
				os.flush();
				os.close();
				is.close();
			} catch (IOException ex) {
				log.log(Level.SEVERE,
					"TailDashF.stop() closing outputStream: ", ex);
			}
		}
		
		/**
		 * TailerListener creates an InputStream that waits for more input
		 * @return the reader
		 */
		private InputStream getInputStream() {
			return is;
		}
	}
}
