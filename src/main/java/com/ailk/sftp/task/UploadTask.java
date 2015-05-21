package com.ailk.sftp.task;

import com.ailk.sftp.TaskHandler;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

public class UploadTask extends Task {

	private String src;
	private String dst;
	private TaskHandler handler;
	
	public UploadTask(String src, String dst, TaskHandler handler){
		this.src=src;
		this.dst=dst;
		this.handler = handler;
	}
	@Override
	public void done(ChannelSftp sftpChannel) {
		try {
			sftpChannel.put(src, dst);
		} catch (SftpException e) {
			e.printStackTrace();
		}
	}
	@Override
	public TaskHandler handler() {
		return handler;
	}

}
