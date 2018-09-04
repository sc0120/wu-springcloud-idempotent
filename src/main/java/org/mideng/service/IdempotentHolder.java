package org.mideng.service;

import org.mideng.bean.Idempotent;

public class IdempotentHolder {

	private static ThreadLocal<Idempotent> idempotent = new ThreadLocal<>();
	
	public static Idempotent getIdempotentVo() {
		return idempotent.get();
	}

	public static void setIdempotentVo(Idempotent idempotentVoOri) {
		idempotent.set(idempotentVoOri);
	}

	public static void clear() {
		idempotent.remove();
	}
}
