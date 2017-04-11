/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2017 wolfposd
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.wolfposd.picturerename;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

/**
 * Renames pictures to their corresponding time in format
 * "yyyyMMdd_HHmmss.jpg"<br>
 * Requires EXIF information.
 * 
 * @author wolfposd
 *
 */
public class StartupPicture {

	public static final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");

	public static void main(String[] args) {

		if (args.length == 3) {
			walkFiles(args[0], Boolean.parseBoolean(args[1]), Integer.parseInt(args[2]));
		} else {
			System.out.println("Need 3 arguments:");
			System.out.println("     {image.dir} {rename=true/false} {adjust hours=int}");
			System.out.println("\nExample:");
			System.out.println("    java -jar PictureRename.jar /path/to/my/pictures false 12");
			System.out.println("    performs a dryrun and adds 12hours to every date");
		}

	}

	public static void walkFiles(String path, boolean rename, int adjustHours) {

		if (!rename) {
			System.out.println("Performing dry-run, no pictures will be changed");
		}

		File baseFolder = new File(path);

		for (File f : baseFolder.listFiles((FilenameFilter) (dir, name) -> name.toLowerCase().endsWith("jpg"))) {

			try {
				handleFile(rename, adjustHours, f);
			} catch (ImageProcessingException | IOException e) {
				System.err.println("Error with picture: " + f.getName() + ", skipping...");
			}

		}

	}

	private static void handleFile(boolean rename, int adjustHours, File f)
			throws ImageProcessingException, IOException {
		Metadata md = ImageMetadataReader.readMetadata(f);

		ExifSubIFDDirectory directory = md.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

		Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);

		if (adjustHours != 0) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.HOUR, adjustHours);
			date = cal.getTime();
		}

		System.out.println((rename ? "Renaming: " : "") + f.getName() + " -> " + format.format(date) + ".jpg");

		if (rename) {
			f.renameTo(new File(f.getParentFile(), format.format(date) + ".jpg"));
		}
	}

}
