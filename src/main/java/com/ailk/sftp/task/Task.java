package com.ailk.sftp.task;

import com.ailk.sftp.TaskHandler;
import com.jcraft.jsch.ChannelSftp;

/**
 * The FTP Task(upload, download, delete)
 * @author GongXingFa
 *
 */
public abstract class Task {
	
	public abstract void done(ChannelSftp sftpChannel);
	
	public abstract TaskHandler handler();
}
