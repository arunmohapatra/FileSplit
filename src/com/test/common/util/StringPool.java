//**************************************************************
//  Copyright (c) 2012-2013 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************

package com.progress.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a generic string pool class. This class is <b>NOT thread safe</b> by
 * default.
 * 
 * The default configuration is faster, but not thread safe. Use the default
 * constructor if you only need the string pool class in a local context.
 * 
 * You can use getDefault() or getThreadLocal() if you need thread safety.
 * 
 */
public class StringPool {

	private final static int THREAD_POOL_SIZE = 1024;

	private final Map<String, String> pool;
	
	private long saved;
	
	private int count;

	/**
	 * Simple holder class used to lazy initialize the global string pool.
	 * 
	 * @author mbaker
	 * 
	 */
	private static class StringPoolHolder {

		private static final StringPool instance = new StringPool(new ConcurrentHashMap<String, String>(
				THREAD_POOL_SIZE));
	}


	private static final ThreadLocal<StringPool> threadLocalStringPool = new ThreadLocal<StringPool>() {
		protected StringPool initialValue() {
			return new StringPool();
		}
	};	
	
	/**
	 * create a new thread pool with the default non-thread safe configuration
	 */
	public StringPool() {
		pool = new HashMap<String, String>(THREAD_POOL_SIZE);
	}

	/**
	 * Create a new thread pool using your own map in which to store strings.
	 * Use this constructor if you want a weak hashmap or some other variety.
	 * 
	 * @param pool
	 *            The map used to store strings
	 */
	public StringPool(Map<String, String> pool) {
		this.pool = pool;
	}


	/**
	 * Get the normalized version of the given string.
	 * 
	 * @param s
	 * @return
	 */
	public String get(String s) {
		if (s == null) {
			return null;
		}

		final String norm = pool.get(s);

		if (norm == null) {
			pool.put(s, s);
			return s;
		} else if (norm != s){
			// http://www.javamex.com/tutorials/memory/string_memory_usage.shtml
			saved += 8 * (int) ((((s.length()) * 2) + 45) / 8);
			count++;
		}

		return norm;
	}
	
	/**
	 * Gets the normalized version of the given string if it is
	 * already in the pool.  Otherwise it returns the original value
	 * of the string.
	 * 
	 * @param s
	 * @return
	 */
	public String getIfPresent(String s) {
		if (s == null) {
			return null;
		}
		
		final String norm = pool.get(s);
		
		if (norm != null) {
			return norm;
		}
		
		return s;
	}

	/**
	 * Creates and returns a NEW string array containing all the elements of the
	 * given string array, where each string has been normalized. Null entries
	 * in the array are preserved.
	 * 
	 * @param strings
	 * @return
	 */
	public String[] get(final String[] strings) {
		if (strings == null) {
			return null;
		}

		final String[] norm = new String[strings.length];
		for (int i = 0; i < norm.length; i++) {
			norm[i] = get(strings[i]);
		}

		return norm;
	}

	/**
	 * Normalizes the strings in the given string array. The original string
	 * array is returned.
	 * 
	 * Null entries in the array are preserved.
	 * 
	 * @param strings
	 * @return
	 */
	public String[] normalize(final String[] strings) {
		if (strings == null) {
			return null;
		}

		for (int i = 0; i < strings.length; i++) {
			strings[i] = get(strings[i]);
		}

		return strings;
	}

	/**
	 * Get the default global StringPool object. The returned ThreadPool object
	 * is thread safe.
	 * 
	 * @return The global thread pool instance
	 */
	public static StringPool getDefault() {
		return StringPoolHolder.instance;
	}

	/**
	 * Get the local local thread instance of the string pool. The returned
	 * ThreadPool object is not thread safe, so don't use it across threads.
	 * 
	 * @return The thread local instance of a thread pool.
	 */
	public static StringPool getThreadLocal() {
		return threadLocalStringPool.get();
	}
	
	/**
	 * returns an *estimate* of the number of bytes saved. Does not include the original.
	 * @return
	 */
	public long getSavings() {
		return saved;
	}
	
	/**
	 * returns the number of duplicates removed.  Does not include the original.
	 * @return
	 */
	public int getCount() {
		return count;
	}

}
