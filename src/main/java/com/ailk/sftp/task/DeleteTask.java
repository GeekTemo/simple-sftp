package com.ailk.sftp.task;

import com.ailk.sftp.TaskHandler;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

public class DeleteTask extends Task {
	private String file;
	private TaskHandler handler;
	public  DeleteTask(String file, TaskHandler handler){
		this.file=file;
		this.handler = handler;
	}
	
	@Override
	public void done(ChannelSftp sftpChannel) {
		try {
			sftpChannel.rm(file);
		} catch (SftpException e) {
			e.printStackTrace();
		}
	}

	@Override
	public TaskHandler handler() {
		return handler;
	}


}
