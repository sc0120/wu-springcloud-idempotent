package org.mideng.service;

public class IdempotentKeyService {

	private static ThreadLocal<String> idempotent = new ThreadLocal<>();
	
	public static String get() {
		return idempotent.get();
	}

	public static void set(String key) {
		idempotent.set(key);
	}

	public static void clear() {
		idempotent.remove();
	}
}
