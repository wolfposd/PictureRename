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
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
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

        if (adjustHours != 0) {
            System.out.println("Adjusting Time by " + adjustHours + " hours");
        }

        File baseFolder = new File(path);

        for (File f : baseFolder.listFiles((FilenameFilter) (dir, name) -> name.toLowerCase().matches(".*(jpg)"))) {
            try {
                handleFile(rename, adjustHours, f);
            } catch (ImageProcessingException | IOException e) {
                System.err.println("Error with picture: " + f.getName() + ", skipping...");
            }

        }
        
        for (File f : baseFolder.listFiles((FilenameFilter) (dir, name) -> !name.toLowerCase().matches(".*(jpg)"))) {
            try {
                handleFileNotImage(rename, adjustHours, f);
            } catch (IOException e) {
                System.err.println("Error with file: " + f.getName() + ", skipping...");
            }
        }

    }

    private static void handleFileNotImage(boolean rename, int adjustHours, File f) throws IOException {
        
        
        BasicFileAttributes attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
        FileTime creationTime = attr.creationTime();
        
        System.err.println("Using file creation time for " + f.getName());
        Date date = new Date(creationTime.toMillis());
        if (adjustHours != 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.HOUR, adjustHours);
            date = cal.getTime();
        }
        
        rename(rename, f, date);
        
    }

    private static void handleFile(boolean rename, int adjustHours, File f) throws ImageProcessingException, IOException {
        Metadata md = ImageMetadataReader.readMetadata(f);

        ExifSubIFDDirectory directory = md.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

        if (directory == null) {
            System.out.println("No EXIF Infos: " + f.getName());
            return;
        }

        Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);

        if (date == null) {
            BasicFileAttributes attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
            FileTime creationTime = attr.creationTime();
            date = new Date(creationTime.toMillis());

            System.err.println("Using file creation time for " + f.getName());
        }

        if (adjustHours != 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.HOUR, adjustHours);
            date = cal.getTime();
        }

        rename(rename, f, date);
    }

    private static void rename(boolean rename, File f, Date date) {
        
        final String ending = f.getName().substring(f.getName().lastIndexOf(".")+1);

        String oldname = f.getName();
        String newName = format.format(date) + "_0." + ending;

        if (new File(f.getParentFile(), newName).exists()) {
            // already exists append number
            int i = checkForNumber(f.getParentFile(), newName);
            newName = format.format(date) + "_" + i + "." + ending;
        }

        if (rename) {
            System.out.println("Renaming: " + oldname + " -> " + newName);
            f.renameTo(new File(f.getParentFile(), newName));
        } else {
            System.out.println("Testing: " + oldname + " -> " + newName);
        }
    }

    private static int checkForNumber(File folder, String newName) {
        int i = 0;

        String checkname = newName.replace("_0.", "_" + i + ".");

        do {
            File check = new File(folder, checkname);
            if (check.exists()) {
                i++;
                checkname = newName.replace("_0.", "_" + i + ".");
            } else
                break;
        } while (true);

        return i;

    }

}
