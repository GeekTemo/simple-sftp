package com.ailk.sftp.task;

import com.ailk.sftp.TaskHandler;

public class Tasks {
	public static UploadTask newUploadTask(String src, String dst,
			TaskHandler handler) {
		return new UploadTask(src, dst, handler);
	}

	public static DownloadTask newDownloadTask(String src, String dst,
			TaskHandler handler) {
		return new DownloadTask(src, dst, handler);
	}

	public static DeleteTask newDeleteTask(String file, TaskHandler handler) {
		return new DeleteTask(file, handler);
	}
}
