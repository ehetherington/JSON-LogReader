# JSON-LogReader
This project is a companion to
[libtinylogger](https://github.com/ehetherington/libtinylogger), and not useful
for anything else.

libtinylogger is a small, yet flexible logger in C for small Linux projects.
One of the output formats it supports is JSON, and this project reads those log
files.

This project provides a Java data model matching that JSON output.

It includes a message formatter to format the log records in most of the
formats provided by libtinylogger.

Formats supported are:
- BASIC
- STANDARD
- DEBUG
- DEBUG_TID
- DEBUG_TNAME
- DEBUG_TALL
- DEBUG_TALL_9

A program to read those JSON log files and print them in some other formats provided by
libtinylogger is provided.

## Dependencies

Package                           | version used
----------------------------------|-------------
FasterXML jackson-core            | 2.9.4
FasterXML jackson-databind        | 2.9.4
FasterXML jackson-annotations     | 2.9.4
FasterXML jackson-datatype-jsr310 | 2.10.0
Apache    commons-io              | 2.6
Apache    commons-cli             | 1.4

[FasterXML/jackson](https://github.com/FasterXML/jackson)

[Apache commons](https://commons.apache.org/)

## Building
Using maven, the pom.xml takes care of the dependancies, so:
```
$ mvn package
....
$ ls ls -l target/JSON-LogReader-0.5.0-SNAPSHOT*
-rw-rw-r--. 1 ehetherington ehetherington   17541 Sep  3 09:04 target/JSON-LogReader-0.5.0-SNAPSHOT.jar
-rw-rw-r--. 1 ehetherington ehetherington 2128141 Sep  3 09:04 target/JSON-LogReader-0.5.0-SNAPSHOT-jar-with-dependencies.jar
```
## Running
The `*with-dependencies.jar` contains all dependencies, so assuming there is a
sample-log.json file, the following command will read a log file and print it
with the default format.
```
$ java -cp target/JSON-LogReader-0.5.0-SNAPSHOT-jar-with-dependencies.jar jsonlogreader.JsonLogReader sample-log.json
```

The program has a help option, so:
```
$ java -cp target/JSON-LogReader-0.5.0-SNAPSHOT-jar-with-dependencies.jar jsonlogreader.JsonLogReader --help
usage: Usage: LogReader [OPTION]... [FILE]
Read and convert JSON logger FILE to the standard output

With no FILE, or when FILE is -, read standard input.

 [-f <FORMAT>] [-F] [-g] [-h] [-o <FILE>] [-v]
 -f,--format <FORMAT>   select output format
 -F,--follow            follow a growing file, as in 'tail -f'
 -g,--gui               Use an interactive GUI (<FILE> is then optional)
 -h,--help              Display this help
 -o,--output <FILE>     Write output to the specified file
 -v,--verbose           print debugging stuff
```
The `-gui` and `-follow` options are not yet implemented.
