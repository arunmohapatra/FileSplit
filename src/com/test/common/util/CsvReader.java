// **************************************************************
// Copyright (c) 2016 by Progress Software Corporation
// All rights reserved. No part of this program or document
// may be reproduced in any form or by any means without
// permission in writing from Progress Software Corporation.
// *************************************************************

package com.progress.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * utility class for loading .csv files.  This has support for line parsing and splitting  lines
 * that are separated by commas.  Delimiter can be changed (maybe use tabs or something).  
 * Line parsing also supports quoted lines.  This also supports line comments, and 
 * ignores blank lines.
 * 
 * @author mbaker
 *
 */
public class CsvReader<T> {
	
	private final CsvLineParser<T> parser;


	public CsvReader ( CsvLineParser<T> parser) {
		this.parser = parser;
	}
	
	public List<T> load(InputStream input) throws IOException {
		
		List<T> list = new ArrayList<T>();
		
		BufferedReader reader = null;
		try  {

			reader = new BufferedReader(new InputStreamReader(input));
			
			String line = null;

			int lineNumber = 0;
			while ((line = reader.readLine()) != null) {

				lineNumber++;
				
				T obj = parser.parseLine(line, lineNumber);
				
				if (obj != null) {
					list.add(obj);
				}
			}
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					//ignore it
				}
			}
		}
		
		return list;
	}
	

	public abstract static class CsvLineParser<T> {
		
		public final T parseLine(final String line, int lineNumber) throws IOException {
			
			String sline = line.trim();
			
			// handle blank line
			if (sline.length() == 0) {
				return null;
			}

			// handle commented line
			if (isCommentLine(sline)) {
				return null;
			}

			String parts[] = splitCsvWithQuotes(sline);
			
			return parseEntries(parts, lineNumber);
		};

		protected abstract T parseEntries(String[] lineParts, int lineNumber) throws IOException;
		
		/**
		 * split a comma delimited string that contains quotes around some items
		 * 
		 * @param s
		 */
		protected String[] splitCsvWithQuotes(String s) {
			List<String> tokensList = new ArrayList<String>();
			boolean inQuotes = false;
			StringBuilder b = new StringBuilder();
			char startQuote = 0;
			for (char c : s.toCharArray()) {

				if (',' == c) {
					if (inQuotes) {
						b.append(c);
					} else {
						tokensList.add(b.toString());
						b = new StringBuilder();
					}
					continue;

				}

				if (c == '\"' || c == '\'') {
					
					if (!inQuotes) {
						startQuote = c;
						inQuotes = true;
					} else if (c == startQuote) {
						inQuotes = false;
						startQuote = 0;
					}
				}

				b.append(c);
			}
			tokensList.add(b.toString());

			return tokensList.toArray(new String[tokensList.size()]);
		}
		
		/**
		 * strip quotes off the string.  string must start AND end with either double quotes, or single quotes.
		 * If string is null, returns null.  If string does not start AND end with quotes, returns the string.
		 * Nested quotes are ignored.
		 * 
		 * @param s
		 * @return
		 */
		protected String stripQuotes(String s) {
			if (s == null) {
				return null;
			}

			if (s.length() < 2) {
				return s;
			}
			if (s.startsWith("\"") && s.endsWith("\"")) {
				return s.substring(1, s.length() - 1);
			}
			
			if (s.startsWith("'") && s.endsWith("'")) {
				return s.substring(1, s.length() - 1);
			}
			
			return s;
		}
		

		/*
		 * trim whitespace off a string.  If string is null, return null
		 */
		protected String trim(String s) {
			if (s == null) {
				return null;
			}

			return s.trim();
		}	
		
		/*
		 * trim string, strip quotes, and trim the resulting string
		 */
		protected String trimAndStripQuotes(String s) {
			return trim(stripQuotes(trim(s)));
		}


		public boolean isCommentLine(String sline) {
			return sline.charAt(0) == '#' || sline.charAt(0) == ';';
		} 
		
	}

}
