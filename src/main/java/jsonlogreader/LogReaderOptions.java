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

import java.util.List;
import jsonlogreader.RecordFormatter.StandardFormat;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Parse the command line options.
 * @author Edward Hetherington
 */
public class LogReaderOptions {
	private final Options options = new Options();
	private CommandLine cmdLine = null;
	
	private boolean gui = false;
//	private StandardFormat format = StandardFormat.DEBUG_TALL_9;	// verbose!
	private StandardFormat format = StandardFormat.STANDARD;
	private boolean adjustDateTimeOnDeserialize = false;
	private boolean follow = false;
	private boolean verbose = false;
	private String inputFile = "-";		// default to "-" to signify stdin
	private String outputFile = "-";	// default to "-" to signify stdout
	
	/**
	 * Create a logReaderOptions ready to parse the command line options.
	 */
	public LogReaderOptions() {
		options.addOption(Option.builder(Opt.HELP.shortOpt)
			.longOpt(Opt.HELP.longOpt)
			.desc(Opt.HELP.description)
			.build());
		
		options.addOption(Option.builder(Opt.FORMAT.shortOpt)
			.longOpt(Opt.FORMAT.longOpt)
			.desc(Opt.FORMAT.description)
			.hasArg()
			.argName("FORMAT")
			.required(false)
			.build());
		
		options.addOption(Option.builder(Opt.ADJUST_TZ.shortOpt)
			.longOpt(Opt.ADJUST_TZ.longOpt)
			.desc(Opt.ADJUST_TZ.description)
			.required(false)
			.build());
		
		options.addOption(Option.builder(Opt.GUI.shortOpt)
			.longOpt(Opt.GUI.longOpt)
			.desc(Opt.GUI.description)
			.build());
		
		options.addOption(Option.builder(Opt.FOLLOW.shortOpt)
			.longOpt(Opt.FOLLOW.longOpt)
			.desc(Opt.FOLLOW.description)
			.build());
		
		options.addOption(Option.builder(Opt.VERBOSE.shortOpt)
			.longOpt(Opt.VERBOSE.longOpt)
			.desc(Opt.VERBOSE.description)
			.build());
		
		options.addOption(Option.builder(Opt.OUT_FILE.shortOpt)
			.longOpt(Opt.OUT_FILE.longOpt)
			.desc(Opt.OUT_FILE.description)
			.hasArg()
			.argName("FILE")
			.required(false)
			.build());
	}
	
	/**
	 * Parse the command line options;
	 * @param args the command line options array
	 */
	public void parseOptions(String[] args) {
		CommandLineParser parser = new DefaultParser();
		try {
			cmdLine = parser.parse(options, args);
		} catch (ParseException ex) {
			System.out.println(ex.getMessage());
			showHelp();
			System.exit(1);
		}
		
		setParams();
	}
	
	/**
	 * Get the requested output req.
	 * @return the requested req
	 */
	public StandardFormat getFormat() {
		return format;
	}
	
	/**
	 * Get adjustDateTimeOnDeserialize. Date/time objects may be adjusted to the
	 * context timezone.
	 * @return true if the adjustment to the context time zone is requested
	 */
	public boolean getAdjustDateTimezoneOnDeserialize() {
		return adjustDateTimeOnDeserialize;
	}
	
	/**
	 * Get the requested input file.
	 * @return the input file requested, <code>"-"</code> if none was requested.
	 */
	public String getInputFile() {
		return inputFile;
	}
	
	/**
	 * Get the requested output file. Defaults to the standard output.
	 * @return the output file requested, <code>"-"</code> if none was
	 * requested.
	 */
	public String getOutputFile() {
		return outputFile;
	}
	
	/**
	 * Ask if the user requested a GUI.
	 * @return true if a GUI was requested. Default to false;
	 */
	public boolean useGUI() {
		return gui;
	}
	
	/**
	 * Ask if the user requested "monitoring" a log file as it is being created.
	 * @return true if monitoring was requested. Default to false;
	 */
	public boolean follow() {
		return follow;
	}
	
	/**
	 * Ask if the user requested verbose information. Selected options are
	 * displayed, and a record count and elapsed time are printed.
	 * @return true if verbose output requested
	 */
	public boolean verbose() {
		return verbose;
	}
	
	/**
	 * Set parameters based on command line options.
	 */
	private void setParams() {
		// print the help message
		if (cmdLine.hasOption(Opt.HELP.shortOpt)) {
			showHelp();
			System.exit(0);
		}
		
		// adjust date/time on deserializaton to the current context
		if (cmdLine.hasOption(Opt.ADJUST_TZ.shortOpt)) {
			adjustDateTimeOnDeserialize = true;
		}
		
		// select the output req
		if (cmdLine.hasOption(Opt.FORMAT.shortOpt)) {
			String req =
				cmdLine.getOptionValue(Opt.FORMAT.shortOpt).toUpperCase();
			
			StandardFormat fmt = null;
			try {
				fmt = StandardFormat.valueOf(req);
			} catch (IllegalArgumentException e) {
			}
			
			if (fmt != null) {
				this.format = fmt;
			} else {
				System.err.println("available formats");
				StandardFormat[] formats =
					RecordFormatter.StandardFormat.values();
				for (StandardFormat f: formats) {
					System.err.format("\t%s%n", f.name());
				}
				System.exit(1);
			}
		}
		
		// Get the input file. Use the standard input as a default.
		// (Unless a gui is requested).
		List<String> arguments = cmdLine.getArgList();
		if (arguments.size() > 1) {
			System.err.println("only one input file is allowed");
			System.exit(1);
		}
		
		// if input file specified, override stdin
		if (arguments.size() == 1) {
			inputFile = arguments.get(0);
		}
		
		// Get the output file. Use the standard output as a default.
		// (Unless a gui is requested).
		if (cmdLine.hasOption(Opt.OUT_FILE.shortOpt)) {
			outputFile = cmdLine.getOptionValue(Opt.OUT_FILE.shortOpt);
		}
		
		// See if the user wants verbose output.
		if (cmdLine.hasOption(Opt.VERBOSE.shortOpt)) {
			verbose = true;
		}
		
		// See if the user wants a GUI. If no input file was specified, the
		// user needs to select one through the GUI.
		if (cmdLine.hasOption(Opt.GUI.shortOpt)) {
			gui = true;
		}
		
		// See if the user wants to "follow" a growing file.
		if (cmdLine.hasOption(Opt.FOLLOW.shortOpt)) {
			follow = true;
		}
		
		if (verbose) {
			System.out.format("gui requested = %b%n" , gui);
			System.out.format("format requested = %s%n", format);
			System.out.format("adjustDateTimeOnDeserialize = %b%n", adjustDateTimeOnDeserialize);
			System.out.format("follow requested = %b%n" , follow);
			System.out.format("Verbose messages = %s%n", verbose);
			System.out.format("input file = %s%n",
				"-".equals(inputFile) ? "<stdin>" : inputFile);
			System.out.format("output file = %s%n",
				"-".equals(outputFile) ? "<stdout>" : outputFile);
		}
	}
	
	/**
	 * Display the options available.
	 */
	public void showHelp() {
		HelpFormatter formatter = new HelpFormatter();
//		formatter.printHelp(usage, options);
		formatter.printHelp(usage, options, true);
	}
	
	/**
	 * Collect some String constants use to configure the parser.
	 */
	private enum Opt {
		HELP ("h", "help", "Display this help"),
		FORMAT("f", "format", "select output format"),
		ADJUST_TZ("a", "adjust_tz", "adjust timesone on deserialization"),
		GUI ("g", "gui", "Use an interactive GUI (<FILE> is then optional)"),
		FOLLOW ("F", "follow", "follow a growing file, as in 'tail -f'"),
		VERBOSE ("v", "verbose", "print debugging stuff"),
		OUT_FILE ("o", "output", "Write output to the specified file"),
		;
		final String shortOpt;
		final String longOpt;
		final String description;
		Opt(String shortOpt, String longOpt, String description) {
			this.shortOpt = shortOpt;
			this.longOpt = longOpt;
			this.description = description;
		}
	}
	
	private final String LS = System.getProperty("line.separator");
	private final String usage =
		"Usage: LogReader [OPTION]... [FILE]" + LS +
		"Read and convert JSON logger FILE to the standard output" + LS + LS +
		"With no FILE, or when FILE is -, read standard input." + LS + LS;
	
}
