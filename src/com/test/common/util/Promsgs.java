package com.progress.common.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.progress.common.message.IProMessage;

public class Promsgs implements IProMessage {

	private static final int PROMSGS_VERSION = 0x3E;

	private static final int CONTINUATION_SIZE = 6;
	private static final int MSG_SIZE = 81;
	private static final int HEADER_TRAILER = 0x0A;

	private String promsgsFile;
	private RandomAccessFile file;
	private FileChannel channel;
	private ByteBuffer buffer;
	private String codePage = "undefined";
	// high number is highest defined number, but there may be continuations in
	// the file
	// which exceed this.
	private int highNumber = 0;
	private int version = PROMSGS_VERSION;
	private String language = "ame";
	private Date creationDate = new Date();

	public static void main(String[] args) throws Exception {

		Promsgs promsgs = new Promsgs();
		promsgs.setPromsgsFile("C:\\Progress\\OpenEdge114\\promsgs");
		promsgs.open();

		System.out.println("PROMSGS Version: " + promsgs.getVersion());
		System.out.println("Code Page: " + promsgs.getCodePage());
		System.out.println("Language: " + promsgs.getLanguageAbbreviation());
		System.out.println("Creation Date: " + promsgs.getCreationDate());
		System.out.println("Highest Message Number: " + promsgs.getHighMessageNumber());

		for (int i = 1; i < promsgs.highNumber; i++) {
			System.out.println(promsgs.get(i));
		}

		promsgs.close();
	}
	
	public Promsgs() throws IOException, ProMessageException {
		String installDir = System.getProperty("Install.Dir");
		if (installDir != null) {
			setPromsgsFile(installDir + "/promsgs");
			open();
		}
	}
	
	public Promsgs(String promsgFile) throws IOException, ProMessageException {
		this.promsgsFile = promsgFile;
		open();
	}

	public void close() {
		try {
			cleanup();
		} catch (IOException e) {
			// ignore it
		}
	}

	private void open() throws IOException, ProMessageException {
		cleanup();

		File f = new File(promsgsFile);
		file = new RandomAccessFile(f, "r");
		channel = file.getChannel();
		buffer = channel.map(MapMode.READ_ONLY, 0, file.length());

		loadHeader();
	}

	private void cleanup() throws IOException {
		buffer = null;
		version = PROMSGS_VERSION;
		highNumber = 0;
		language = "ame";
		creationDate = new Date();
		codePage = "undefined";
		 

		if (channel != null) {
			channel.close();
		}

		if (file != null) {
			file.close();
		}

	}

	private void loadHeader() throws ProMessageException {

		// check version
		buffer.position(0);
		int version = buffer.getInt();

		byte[] versionB = new byte[4];
		buffer.get(versionB);

		buffer.get(new byte[1]);

		byte[] codePage = new byte[20];
		buffer.get(codePage);

		buffer.get(new byte[5]);

		byte[] createDate = new byte[8];
		buffer.get(createDate);

		byte[] languageAbbreviation = new byte[3];
		buffer.get(languageAbbreviation);

		buffer.get(new byte[2]);

		byte[] highNumber = new byte[7];
		buffer.get(highNumber);

		buffer.position(MSG_SIZE - 1);
		byte[] trailer = new byte[1];
		buffer.get(trailer);

		if (trailer[0] != HEADER_TRAILER) {
			throw new ProMessageException(
					"Invalid header in PROMSGS file.  Trailer was not found in header at expected location.");
		}

		try {
			String createDateS = new String(createDate);
			this.creationDate = new SimpleDateFormat("YYYYMMDD", Locale.ENGLISH).parse(createDateS);
		} catch (ParseException e) {
			throw new ProMessageException("Invalid creation date in PROMSGS file");
		}

		try {
			String versionS = new String(versionB);
			version = Integer.parseInt(versionS);
			this.version = version;
		} catch (NumberFormatException e) {
			throw new ProMessageException("Invalid version in PROMSGS file");
		}

		try {
			String highNumberS = new String(highNumber);
			this.highNumber = Integer.parseInt(highNumberS);
		} catch (NumberFormatException e) {
			throw new ProMessageException("Invalid high message number in PROMSGS file");
		}

		this.codePage = new String(codePage).trim();
		this.language = new String(languageAbbreviation).trim();

		if (version > PROMSGS_VERSION) {
			throw new ProMessageException("Invalid version in PROMSGS file.  Expected version: " + PROMSGS_VERSION
					+ ". Read " + version);
		}

	}

	private void setPromsgsFile(String promsgsFile) {
		this.promsgsFile = promsgsFile;
	}

	public String getMessage(long msgNumber) throws ProMessageException {

		int msg = (int) msgNumber;

		if (msg > highNumber) {
			throw new ProMessageException("Message number must be " + highNumber + " or less.");
		}

		try {
			return get(msg);
		} catch (UnsupportedEncodingException e) {
			throw new ProMessageException(e.getMessage());
		}

	}

	private String get(int msgNumber) throws ProMessageException, UnsupportedEncodingException {

		if (msgNumber < 1) {
			throw new ProMessageException(msgNumber + " must be 1 or greater.");
		}

		byte msg[] = new byte[MSG_SIZE];

		int offset = MSG_SIZE * msgNumber;

		if (offset + MSG_SIZE > buffer.capacity()) {
			throw new ProMessageException(msgNumber + " is too high.");
		}

		// duplicate the buffer so we can change its position without affecting the position of the reads
		// for other threads
		ByteBuffer buffer = this.buffer.duplicate();
		buffer.position(offset);
		buffer.get(msg);

		StringBuilder sb = new StringBuilder();
		// check for overflow to new message
		if (msg[msg.length - 1] != 0) {

			String part = abstractString(msg, CONTINUATION_SIZE, msg.length - CONTINUATION_SIZE);
			sb.append(part);

			int extNumber = abstractContinuation(msg);

			String continuation = get(extNumber);

			sb.append(continuation);

		} else {
			sb.append(abstractString(msg, 0, msg.length - 1));
		}

		return sb.toString();

	}

	private int abstractContinuation(byte[] msg) throws NumberFormatException, UnsupportedEncodingException {
		return Integer.parseInt(abstractString(msg, 0, 6));
	}

	private String abstractString(byte[] msg, int offset, int length) throws UnsupportedEncodingException {

		if ("undefined".equals(codePage)) {
			return new String(msg, offset, length);
		} else {
			return new String(msg, offset, length, codePage);
		}
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public int getHighMessageNumber() {
		return highNumber;
	}

	public String getLanguageAbbreviation() {
		return language;
	}

	public int getVersion() {
		return version;
	}

	public String getCodePage() {
		return codePage;
	}

	public void finalize() {
		try {
			cleanup();
		} catch (IOException e) {
			// ignore it
		}
	}
}
