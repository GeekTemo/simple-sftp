package com.ailk.sftp;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.ailk.sftp.task.Task;
import com.google.common.collect.Maps;

public class SFTPClientContext {

	private static final Map<Integer, SFTPSession> SESSIONS = Maps
			.newConcurrentMap();
	private static final Map<Integer, Queue<Task>> QUEUES = Maps
			.newConcurrentMap();
	private static final AtomicInteger sequence = new AtomicInteger(0);

	public static int nextSequence() {
		return sequence.addAndGet(1);
	}

	protected static void addQueue(Integer seqNum, Queue<Task> queue) {
		QUEUES.put(seqNum, queue);
	}

	protected static Queue<Task> getQueue(Integer seqNum) {
		return QUEUES.get(seqNum);
	}

	protected static void removeQueue(Integer seqNum) {
		QUEUES.remove(seqNum);
	}

	protected static void addSession(Integer seqNum, SFTPSession session) {
		SESSIONS.put(seqNum, session);
	}

	protected static SFTPSession getSession(Integer seqNum) {
		return SESSIONS.get(seqNum);
	}

	protected static void removeSession(Integer seqNum) {
		SESSIONS.remove(seqNum);
	}

	/**
	 * the session sequence number list
	 * 
	 * @return
	 */
	protected static Set<Integer> sequences() {
		return SESSIONS.keySet();
	}
}
