package com.csnt.ins.utils;

import org.apache.log4j.Layout;
import org.apache.log4j.Priority;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

/**
 * @author source
 */
public class CsntRollingFileAppender extends RollingFileAppender {
    private LocalDate localDate = null;
    private String parternFileName = null;
    private long nextRollover = 0;

    public CsntRollingFileAppender() {
        super();
    }

    public CsntRollingFileAppender(Layout layout, String filename, boolean append)
            throws IOException {
        super(layout, filename, append);
        super.setFile(DatePatternParser.pase(new StringBuilder(filename)));
    }


    public CsntRollingFileAppender(Layout layout, String filename)
            throws IOException {
        super(layout, filename);
    }


    @Override
    public boolean isAsSevereAsThreshold(Priority priority) {
        return this.getThreshold().equals(priority);
    }

    @Override
    public void setFile(String file) {
        if (parternFileName == null) {
            parternFileName = file;
        }

        file = DatePatternParser.pase(new StringBuilder(parternFileName));
        super.fileName = file.trim();
    }

    private String genFileName() {
        return DatePatternParser.pase(new StringBuilder(parternFileName)).trim();
    }

    @Override
    public void rollOver() {
        File target;
        File file;

        if (qw != null) {
            long size = ((CountingQuietWriter) qw).getCount();
            LogLog.debug("rolling over count=" + size);
            //   if operation fails, do not roll again until
            //      maxFileSize more bytes are written
            this.nextRollover = size + maxFileSize;
        }
        LogLog.debug("maxBackupIndex=" + maxBackupIndex);

        boolean renameSucceeded = true;
        // If maxBackups <= 0, then there is no file renaming to be done.
        if (maxBackupIndex > 0) {
            // Delete the oldest file, to keep Windows happy.
            String genFile = genFileName();
            file = new File(genFile + '.' + maxBackupIndex);
            if (file.exists()) {
                renameSucceeded = file.delete();
            }

            // Map {(maxBackupIndex - 1), ..., 2, 1} to {maxBackupIndex, ..., 3, 2}
            for (int i = maxBackupIndex - 1; i >= 1 && renameSucceeded; i--) {
                file = new File(genFile + "." + i);
                if (file.exists()) {
                    target = new File(genFile + '.' + (i + 1));
                    LogLog.debug("Renaming file " + file + " to " + target);
                    renameSucceeded = file.renameTo(target);
                }
            }

            if (renameSucceeded) {
                // Rename fileName to fileName.1
                target = new File(genFile + "." + 1);

                this.closeFile(); // keep windows happy.

                file = new File(genFile);
                LogLog.debug("Renaming file " + file + " to " + target);
                renameSucceeded = file.renameTo(target);
                //
                //   if file rename failed, reopen file with append = true
                //
                if (!renameSucceeded) {
                    try {
                        this.setFile(genFile, true, bufferedIO, bufferSize);
                    } catch (IOException e) {
                        if (e instanceof InterruptedIOException) {
                            Thread.currentThread().interrupt();
                        }
                        LogLog.error("setFile(" + genFile + ", true) call failed.", e);
                    }
                }
            }
        }

        //
        //   if all renames were successful, then
        //
        if (renameSucceeded) {
            try {
                // This will also close the file. This is OK since multiple
                // close operations are safe.
                this.setFile(fileName, false, bufferedIO, bufferSize);
                nextRollover = 0;
            } catch (IOException e) {
                if (e instanceof InterruptedIOException) {
                    Thread.currentThread().interrupt();
                }
                LogLog.error("setFile(" + fileName + ", false) call failed.", e);
            }
        }
    }

    static class DatePatternParser {

        public DatePatternParser() {
        }

        public static final String HEAD = "%d";

        public static String pase(StringBuilder str) {
            return pase(str, 0);
        }

        public static String pase(StringBuilder str, int fromIndex) {
            SimpleDateFormat sdf = null;
            int index = str.indexOf(HEAD, fromIndex);
            if (index < 0) {
                return str.toString();
            }
            int indexEnd = -1;
            boolean isPase = false;
            String sub = str.substring(str.indexOf(HEAD, index), str.indexOf("{", index));

            if (HEAD.equals(sub.trim())) {
                isPase = true;
            }
            if (isPase) {
                indexEnd = str.indexOf("}", index) + 1;
                int indexParn = str.indexOf("{", index);
                String p = str.substring(indexParn + 1, indexEnd - 1);
                sdf = new SimpleDateFormat(p);
                str.replace(index, indexEnd, sdf.format(new Date()));
            }
            return pase(str, index + 1);
        }
    }

    @Override
    protected void subAppend(LoggingEvent event) {
        super.subAppend(event);
        boolean rollOver = false;
        if (localDate == null) {
            localDate = LocalDate.now();
            rollOver = true;
        }

        if (fileName != null && qw != null) {
            long size = ((CountingQuietWriter) qw).getCount();
            boolean flag = (size >= maxFileSize && size >= nextRollover) || !localDate.equals(LocalDate.now());
            if (flag) {
                rollOver = true;
            }
        }

        if (rollOver) {
            rollOver();
        }

        localDate = LocalDate.now();
    }
}
