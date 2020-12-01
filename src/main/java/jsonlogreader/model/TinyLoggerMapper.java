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

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.logging.Level;

/**
 * Convenience class to register the modules necessary to serialize and
 * de-serialize <code>SDLevel</code> and JavaTime (JSR-310) classes
 * (ZonedDateTime in particular).
 * @author Edward Hetherington
 */
public class TinyLoggerMapper extends ObjectMapper {

	/**
	 * Create a <code>ObjectMapper</code> with modules to serialize and
	 * de-serialize <code>SDLevel</code> and JDK8 java.time (JSR-310) Objects
	 * pre-registered.
	 */
	public TinyLoggerMapper() {
		SimpleModule sdLevelModule = new SimpleModule("SDLevelModule");
		sdLevelModule.addSerializer(
			Level.class, new SDLevelJsonSerializer());
		sdLevelModule.addDeserializer(
			Level.class, new SDLevelJsonDeserializer());
		
		// to serialize and deserialize Level and SDLevel
		registerModule(sdLevelModule);
		
		// to serialize and deserialize ZonedDataTime (and others)
		registerModule(new JavaTimeModule());
	}
	
	@Override
	final public ObjectMapper registerModule(Module module) {
		return super.registerModule(module);
	}
	
}
