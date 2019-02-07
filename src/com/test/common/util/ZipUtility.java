package com.progress.common.util;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.UUID;

public class ZipUtility 
{
    public static String extractZipContent(File tempZipFile, File targetDirectory) throws Exception
    {
			List<String> lstFileNames = unzipFiles(tempZipFile, targetDirectory);
			String compileFileName = lstFileNames.get(0);
			String deleteFileName = lstFileNames.get(1);
			String pfFileName = lstFileNames.get(2);
			deleteFiles(deleteFileName, targetDirectory);
			return pfFileName;
    }
    public static List<String> unzipFiles(File tmpFile, File targetDirectory) throws IOException {
		String compileFileName = null;
		String pfFileName = null;
		String deleteFileName = null;
		List<String> lstFileNames = new ArrayList<String>();
		byte[] b = new byte[1024];
		int count;
		// Unzip temporary file to target directory
		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(new FileInputStream(tmpFile));

			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.getName().endsWith("/") || entry.getName().endsWith("\\")) {
					(new File(targetDirectory + "/" + entry.getName())).mkdirs();
					continue;
				}
				File file = new File(entry.getName());
				if (file.getParent() != null) {
					new File(targetDirectory + "/" + file.getParent()).mkdirs();
				}

				OutputStream dest = null;

				try {
					FileOutputStream fos = null;
					if ("compile.lst".equals(entry.getName())) {
						compileFileName = "compile" + UUID.randomUUID().toString() + ".lst";
						fos = new FileOutputStream(targetDirectory + "/" + compileFileName);
					} else if ("delete.lst".equals(entry.getName())) {
						deleteFileName = "delete" + UUID.randomUUID().toString() + ".lst";
						fos = new FileOutputStream(targetDirectory + "/" + deleteFileName);
					} else if (entry != null && entry.getName().endsWith(".pf")) {
						pfFileName = entry.getName();
						fos = new FileOutputStream(targetDirectory + "/" + pfFileName);
						pfFileName = targetDirectory + "/" + pfFileName;
					} else {
						fos = new FileOutputStream(targetDirectory + "/" + entry.getName());
					}
					dest = new BufferedOutputStream(fos, 1024);

					while ((count = zis.read(b, 0, 1024)) != -1) {
						dest.write(b, 0, count);
					}
				} finally {
					if (dest != null) {
						try {
							dest.close();
						} catch (Exception e) {
							// ignore it
						}
					}

				}

			}
		} finally {
			if (zis != null) {
				try {
					zis.close();
				} catch (Exception e) {
					// ignore it
				}

			}
		}

		lstFileNames.add(compileFileName);
		lstFileNames.add(deleteFileName);
		lstFileNames.add(pfFileName);
		return lstFileNames;
	}
    
    /**
	 * Deletes all files in delete.lst from target dir.
	 * 
	 * @param deleteLstFileName
	 * @param targetDirectory
	 * @throws IOException
	 */
	public static void deleteFiles(String deleteLstFileName, File targetDirectory) throws IOException {
		File deleteLst = new File(targetDirectory + "/" + deleteLstFileName);
		java.util.List<String> deleteFilesList;
		if (deleteLst.isFile()) {
			LineNumberReader lr = null;
			try {
				lr = new LineNumberReader(new FileReader(deleteLst));
				String thisLine;
				deleteFilesList = new ArrayList<String>();
				while ((thisLine = lr.readLine()) != null) {
					deleteFilesList.add(thisLine.trim());
				}
			} finally {
				try {
					lr.close();
				} catch (Exception e) {
					// ignore it
				}
			}
			deleteFiles(deleteFilesList, targetDirectory);
			deleteLst.delete();
		}
	}

	/**
	 * Deletes list of files from target dir.
	 * 
	 * @param deleteLstFileName
	 * @param targetDirectory
	 */
	public static boolean deleteFiles(List<String> deleteFilesList, File targetDirectory) {
		for (String fileName : deleteFilesList) {
			File file = new File(targetDirectory + "/" + fileName);
			if (file.exists()) {
				if (file.isDirectory()) {
					deleteFiles(Arrays.asList(file.list()), file);
				}
				file.delete();
			}
		}
		return true;
	}
    
}
