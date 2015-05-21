package com.ailk.sftp;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ailk.sftp.task.Task;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * SFTP Adapter for NG
 * 
 * @author GongXingFa
 * 
 */
public class SimpleSFTP {
	private static Logger logger = LoggerFactory.getLogger(SimpleSFTP.class);
	private static Integer MAX_SAME_AUTH_SESSION = 10;
	private static Map<String, List<Integer>> SESSION_POOL = Maps.newHashMap();

	public static class SftpURL {
		// sftp://test_user:test_password@192.168.1.0:22/mail/recv/20151022.eml
		private String user;
		private String password;
		private String host;
		private Integer port;
		private String path;
		private String authority;

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public Integer getPort() {
			return port;
		}

		public void setPort(Integer port) {
			this.port = port;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getAuthority() {
			return authority;
		}

		private SftpURL() {

		}

		public static SftpURL parse(String url) throws Exception {
			if (!url.startsWith("sftp")) {
				throw new Exception(url + " is Not a valid URL");
			}
			URL u = null;
			try {
				u = new URL(url.substring(1));
			} catch (MalformedURLException e) {
				throw new Exception(
						"Not a valid SFTP URL.The right SFTP url format: sftp://${user}:${password}@${host}:${port}/${path}/${filename}");
			}
			SftpURL sftpURL = new SftpURL();
			sftpURL.user = u.getUserInfo().split(":")[0];
			sftpURL.password = u.getUserInfo().split(":")[1];
			sftpURL.host = u.getHost();
			sftpURL.port = u.getPort();
			sftpURL.path = u.getPath();
			sftpURL.authority = u.getAuthority();
			return sftpURL;
		}
	}

	/**
	 * Get the balance session
	 * 
	 * @param sessionSeqs
	 * @return
	 */
	private static Integer blanceSession(List<Integer> sessionSeqs) {
		Integer min = Integer.MAX_VALUE;
		Integer blanceSeq = -1;
		for (Integer seq : sessionSeqs) {
			Queue<Task> taskQueue = SFTPClient.getSessionTaskQueue(seq);
			Integer size = taskQueue.size();
			if (size < min) {
				min = size;
				blanceSeq = seq;
			}
		}
		return blanceSeq;
	}

	/**
	 * Get the SFTP Session
	 * 
	 * @param sftpurl
	 *            format
	 *            sftp://user:passwd@10.1.196.41:22/mail/recv/20151022.eml
	 * @return return sequence number or not return -1
	 */
	private static Integer getSession(SftpURL sftpurl) {
		Integer sessionSeqNum = 0;
		synchronized (SESSION_POOL) {
			List<Integer> sessionList = SESSION_POOL
					.get(sftpurl.getAuthority());
			if (sessionList == null) {
				sessionList = Lists.newArrayList();
				Integer seq = SFTPClient.newSession(sftpurl.getHost(),
						sftpurl.getUser(), sftpurl.getPassword(),
						sftpurl.getPort());
				sessionList.add(seq);
				SESSION_POOL.put(sftpurl.getAuthority(), sessionList);
				sessionSeqNum = seq;
			} else {
				if (sessionList.size() >= MAX_SAME_AUTH_SESSION) {
					sessionSeqNum = blanceSession(sessionList);
				} else {
					Integer seq = SFTPClient.newSession(sftpurl.getHost(),
							sftpurl.getUser(), sftpurl.getPassword(),
							sftpurl.getPort());
					sessionList.add(seq);
					sessionSeqNum = seq;
				}
			}
		}

		return sessionSeqNum;
	}

	public static void main(String[] args) throws Exception {
		String urlStr = "sftp://test_user:test_password@10.1.196.41:22/mail/recv/20151022.eml";
		SftpURL sftpurl = SftpURL.parse(urlStr);
		System.out.println("PATH:" + sftpurl.getPath());

	}

	/**
	 * upload the file to the ftp asssynchronous
	 * 
	 * @param src
	 *            the local files
	 * @param url
	 *            the sftp url format:sftp://user:password@host:port/pathss
	 * @param handler
	 *            around the upload task, before or after the task to do
	 *            somethings
	 */
	public static void uploadNoWait(String src, String url, TaskHandler handler) {
		try {
			SftpURL sftpURL = SftpURL.parse(url);
			Integer sessionSeq = getSession(sftpURL);
			System.out.println("Get SFTP Session:" + sessionSeq);
			SFTPClient.upload(sessionSeq, src, sftpURL.getPath().substring(1),
					handler);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void downloadNoWait(String url, String dst,
			TaskHandler handler) {
		try {
			SftpURL sftpURL = SftpURL.parse(url);
			Integer sessionSeq = getSession(sftpURL);
			logger.warn("Get SFTP Session:" + sessionSeq);
			SFTPClient.download(sessionSeq, sftpURL.getPath().substring(1),
					dst, handler);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deleteNoWait(String url, TaskHandler handler) {
		try {
			SftpURL sftpURL = SftpURL.parse(url);
			Integer sessionSeq = getSession(sftpURL);
			SFTPClient.delete(sessionSeq, sftpURL.getPath().substring(1),
					handler);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Map<String, List<Session>> JSCH_SESSION_POOL = Maps
			.newHashMap();
	private static final Integer MAX_SAME_JSC_SESSION_COUNT = 10;

	private static AtomicInteger jsch_session_sequence = new AtomicInteger(0);

	private static Session blanceJschSession(List<Session> sessions) {
		if (jsch_session_sequence.get() >= 10000000) {
			jsch_session_sequence.set(0);
		}
		Session session = sessions.get(jsch_session_sequence.getAndIncrement()
				% MAX_SAME_JSC_SESSION_COUNT);
		return session;
	}

	static final JSch J_SCH = new JSch();

	private static Session getJschSession(SftpURL sftpURL) {
		Session session = null;
		synchronized (JSCH_SESSION_POOL) {
			List<Session> sessions = JSCH_SESSION_POOL.get(sftpURL
					.getAuthority());
			try {
				if (sessions == null) {
					sessions = Lists.newArrayList();
					session = J_SCH.getSession(sftpURL.getUser(),
							sftpURL.getHost(), sftpURL.getPort());
					session.setConfig("StrictHostKeyChecking", "no");
					session.setPassword(sftpURL.getPassword());
					session.connect();
					sessions.add(session);
					JSCH_SESSION_POOL.put(sftpURL.getAuthority(), sessions);
				} else {
					if (sessions.size() >= MAX_SAME_JSC_SESSION_COUNT) {
						session = blanceJschSession(sessions);
						if (!session.isConnected()) {
							session.connect();
						}
					} else {
						session = J_SCH.getSession(sftpURL.getUser(),
								sftpURL.getHost(), sftpURL.getPort());
						session.setConfig("StrictHostKeyChecking", "no");
						session.setPassword(sftpURL.getPassword());
						session.connect();
						sessions.add(session);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return session;
	}

	/**
	 * get jsch sftp channel
	 * 
	 * @param sftpURL
	 * @return
	 * @throws JSchException
	 */
	private static ChannelSftp newSFTPChannel(SftpURL sftpURL)
			throws JSchException {
		Session session = getJschSession(sftpURL);
		Channel channel = session.openChannel("sftp");
		ChannelSftp sftpChannel = (ChannelSftp) channel;
		return sftpChannel;
	}

	/**
	 * upload the file synchronous
	 * 
	 * @param src
	 * @param url
	 *            the sftp url
	 *            format:sftp://user:password@host:port/pathss/tx.txt
	 * @throws Exception
	 */
	public static void upload(String src, String url) throws Exception {
		SftpURL sftpURL = SftpURL.parse(url);
		ChannelSftp sftpChannel = newSFTPChannel(sftpURL);
		sftpChannel.connect();
		sftpChannel.put(src, sftpURL.getPath().substring(1));
		sftpChannel.disconnect();
		logger.info("Upload:"+url+" Success!");
	}

	public static void upload(InputStream src, String url) throws Exception {
		SftpURL sftpURL = SftpURL.parse(url);
		ChannelSftp sftpChannel = newSFTPChannel(sftpURL);
		String dst = sftpURL.getPath().substring(1);
		sftpChannel.connect();
		sftpChannel.put(src, dst);
		sftpChannel.disconnect();
		logger.info("Upload:"+url+" Success!");
	}

	/**
	 * mkdirs if dir not exit
	 * 
	 * @param url
	 */
	public static void mkdirs(String url) {
		SftpURL sftpURL = null;
		ChannelSftp sftp = null;
		try {
			sftpURL = SftpURL.parse(url);
			sftp = newSFTPChannel(sftpURL);
			sftp.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String filePath = sftpURL.getPath().substring(1);// sftp/testdir/testsubdir
		String dirPath = filePath.substring(0, filePath.lastIndexOf("/"));

		String tmpDir = dirPath += "/" + UUID.randomUUID().toString();
		try {
			sftp.mkdir(tmpDir);
			logger.info("Mkdirs:" + tmpDir + " Success");
		} catch (SftpException e) {
			List<String> flatDirs = Lists.newArrayList(dirPath.split("/"));
			String tmpFlatDir = flatDirs.remove(0);
			try {
				while (flatDirs.size() > 0) {
					sftp.mkdir(tmpFlatDir);
					String dir = flatDirs.remove(0);
					tmpFlatDir += "/" + dir;
				}
				sftp.mkdir(tmpDir);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		} finally {
			try {
				sftp.rmdir(tmpDir);
			} catch (SftpException e) {
				e.printStackTrace();
			}
		}
		sftp.disconnect();
	}

	public static void closeClient() {
		SFTPClient.closeClient();
	}
}
