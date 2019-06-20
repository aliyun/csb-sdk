package com.alibaba.csb.sdk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * Created by tingbin.ctb
 * 2019/6/20-12:10.
 */
public class GZipUtils {

    static public byte[] gzipBytes(byte[] bytes) throws IOException {
        GZIPOutputStream gzip = null;
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            gzip = new GZIPOutputStream(out);
            gzip.write(bytes);
            gzip.close();
            return out.toByteArray();
        } finally {
            if (gzip != null) {
                gzip.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }
}
