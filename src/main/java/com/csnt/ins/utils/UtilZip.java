package com.csnt.ins.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author hx
 * @date 2017/9/12
 */
public class UtilZip {
    private static final Logger logger = LoggerFactory.getLogger(UtilZip.class);

    /**
     * 压缩, 基于文件压缩，传入文件名和内容，压缩后生成压缩文件
     * @param zipFileName
     * @param srcFileName
     * @param data
     * @throws Exception
     */
    public static void zip(String zipFileName, String srcFileName, String data)
            throws Exception {
        FileOutputStream f = new FileOutputStream(zipFileName);
        ZipOutputStream out = new ZipOutputStream(f);
        out.putNextEntry(new ZipEntry(srcFileName));
        out.write(data.getBytes(Charset.forName("utf-8")));
        out.close();

        logger.info("zip success!! {}", srcFileName);
    }


    /**
     * 压缩，基于流压缩，传入文件名和数据，传出压缩后的数据流
     * @param srcFileName
     * @param data
     * @return
     * @throws IOException
     */
    public static byte[] zip(String srcFileName, String data)
            throws IOException {
        ByteArrayOutputStream byteArrayOutPut = new ByteArrayOutputStream();
        ZipOutputStream zipOutPut = new ZipOutputStream(new BufferedOutputStream(byteArrayOutPut));
        try {
            zipOutPut.putNextEntry(new ZipEntry(srcFileName));
            zipOutPut.write(data.getBytes(Charset.forName("utf-8")));
            zipOutPut.finish();
        } finally {
            zipOutPut.closeEntry();
            zipOutPut.close();
        }

        return byteArrayOutPut.toByteArray();
    }

    public static boolean isZeroZipFile(Path filePath) throws IOException {
        ZipFile zipFile = new ZipFile(filePath.toFile());
        int size = zipFile.size();
        zipFile.close();
        return 0 == size;
    }
}
