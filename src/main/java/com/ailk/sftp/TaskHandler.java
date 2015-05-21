package com.ailk.sftp;

import java.util.HashMap;
import java.util.Map;


/**
 * The handler around the task
 * @author GongXingFa
 *
 */
public abstract class TaskHandler {
	private Map<Object, Object> data = new HashMap<Object, Object>();

	public abstract void doBefore();

	public Object get(Object key) {
		return data.get(key);
	}

	public void set(Object key, Object value) {
		data.put(key, value);
	}

	public abstract void doAfter();
}
