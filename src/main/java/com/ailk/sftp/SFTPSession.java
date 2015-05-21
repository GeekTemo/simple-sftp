package com.ailk.sftp;

import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ailk.sftp.task.Task;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SFTPSession extends Thread {
	private static Logger logger = LoggerFactory.getLogger(SFTPSession.class);
	private JSch jsch = new JSch();
	private Session session;
	private Integer seqNum;
	private String host;
	private volatile Boolean keepAlive = true;

	protected SFTPSession(String host, String user, String password,
			Integer SeqNum) {
		this(host, user, password, 22, SeqNum);
	}

	protected SFTPSession(String host, String user, String password,
			Integer port, Integer seqNum) {
		this.host = host;
		this.seqNum = seqNum;
		try {
			session = jsch.getSession(user, host, port);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(password);
		} catch (JSchException e) {
			e.printStackTrace();
		}
	}

	protected Integer seqNum() {
		return seqNum;
	}

	protected void closeSession() {
		keepAlive = false;
	}

	public void run() {
		try {
			session.connect();
			logger.warn("SFTP Session:" + seqNum + " Connected to:" + host);
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;
			Queue<Task> taskQueue = SFTPClientContext.getQueue(seqNum);
			while (keepAlive) {
				Task task = taskQueue.poll();
				if (task != null) {
					if (task.handler() != null) {
						task.handler().doBefore();
						task.done(sftpChannel);
						task.handler().doAfter();
					} else {
						task.done(sftpChannel);
					}
				} else {
					Thread.sleep(300);
				}
			}
			sftpChannel.disconnect();
			sftpChannel.exit();
			session.disconnect();
			logger.warn("SFTP Session:" + seqNum + " Disconnect to " + host);
		} catch (JSchException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
