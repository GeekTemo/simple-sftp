package com.ailk.sftp;

import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ailk.sftp.task.Task;
import com.ailk.sftp.task.Tasks;
import com.google.common.collect.Queues;

/**
 * A very sample and fast SFTP Client
 * 
 * @author GongXingFa
 * 
 */
public class SFTPClient {
	private static Logger logger = LoggerFactory.getLogger(SFTPClient.class);

	/**
	 * New a SFTP Session and return the session sequence number
	 * 
	 * @param host
	 * @param user
	 * @param password
	 * @param port
	 * @return
	 */
	public static Integer newSession(String host, String user, String password,
			Integer port) {
		Integer seqNum = SFTPClientContext.nextSequence();
		SFTPSession session = new SFTPSession(host, user, password, port,
				seqNum);
		Queue<Task> queue = Queues.newLinkedBlockingQueue();
		SFTPClientContext.addQueue(seqNum, queue);
		SFTPClientContext.addSession(seqNum, session);
		session.start();
		return seqNum;
	}

	/**
	 * upload the file
	 * 
	 * @param sessionSeqNum
	 *            the session sequence number
	 * @param src
	 * @param dst
	 */
	public static void upload(Integer sessionSeqNum, String src, String dst,
			TaskHandler taskHandler) {
		Queue<Task> queue = SFTPClientContext.getQueue(sessionSeqNum);
		Task task = Tasks.newUploadTask(src, dst, taskHandler);
		queue.add(task);
		// logger.warn("Add the Upload task to Queue:" + sessionSeqNum);
	}

	/**
	 * upload the file
	 * 
	 * @param sessionSeqNum
	 *            the session sequence number
	 * @param src
	 * @param dst
	 */
	public static void download(Integer sessionSeqNum, String src, String dst,
			TaskHandler taskHandler) {
		Queue<Task> queue = SFTPClientContext.getQueue(sessionSeqNum);
		Task task = Tasks.newDownloadTask(src, dst, taskHandler);
		queue.add(task);
		logger.warn("Add the Download task to Queue:" + sessionSeqNum);
	}

	/**
	 * delete the file
	 * 
	 * @param sessionSeqNum
	 *            the session sequence number
	 * @param file
	 * @param taskHandler
	 */
	public static void delete(Integer sessionSeqNum, String file,
			TaskHandler taskHandler) {
		Queue<Task> queue = SFTPClientContext.getQueue(sessionSeqNum);
		Task task = Tasks.newDeleteTask(file, taskHandler);
		queue.add(task);
		logger.warn("Add the Delete task to Queue:" + sessionSeqNum);
	}

	public static void closeSession(Integer seqNum) {
		SFTPSession session = SFTPClientContext.getSession(seqNum);
		session.closeSession();
		SFTPClientContext.removeSession(seqNum);
	}

	public static void closeClient() {
		for (Integer i : SFTPClientContext.sequences()) {
			closeSession(i);
		}
		logger.warn("About to close SFTPClient.....");
	}

	/**
	 * Get the session Queue
	 * 
	 * @param sessionSeq
	 * @return
	 */
	public static Queue<Task> getSessionTaskQueue(Integer sessionSeq) {
		return SFTPClientContext.getQueue(sessionSeq);
	}

}
