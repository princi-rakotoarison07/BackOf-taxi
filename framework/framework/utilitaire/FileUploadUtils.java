package framework.utilitaire;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FileUploadUtils {

    public static Map<String, UploadedFile> getUploadedFiles(HttpServletRequest request) {
        Map<String, UploadedFile> map = new HashMap<>();
        try {
            String ct = request.getContentType();
            System.out.println("[Upload] Content-Type: " + ct);
            Collection<Part> parts = request.getParts();
            System.out.println("[Upload] parts count: " + (parts != null ? parts.size() : 0));
            if (parts == null) return map;
            for (Part p : parts) {
                String submitted = getSubmittedFileName(p);
                if (submitted != null && !submitted.isEmpty() && p.getSize() > 0) {
                    System.out.println("[Upload] file field='" + p.getName() + "' filename='" + submitted + "' size=" + p.getSize());
                    byte[] bytes = toBytes(p.getInputStream());
                    UploadedFile uf = new UploadedFile(p.getName(), submitted, p.getContentType(), bytes);
                    map.put(p.getName(), uf);
                } else {
                    System.out.println("[Upload] skip part name='" + p.getName() + "' submitted='" + submitted + "' size=" + p.getSize());
                }
            }
        } catch (Exception ignore) {
            System.out.println("[Upload] Exception while parsing parts: " + ignore);
        }
        return map;
    }

    private static byte[] toBytes(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int r;
        while ((r = in.read(buf)) != -1) {
            bos.write(buf, 0, r);
        }
        return bos.toByteArray();
    }

    // Extract filename from content-disposition
    private static String getSubmittedFileName(Part part) {
        String cd = part.getHeader("content-disposition");
        if (cd == null) return null;
        for (String piece : cd.split(";")) {
            String s = piece.trim();
            if (s.startsWith("filename")) {
                String fn = s.substring(s.indexOf('=') + 1).trim().replace("\"", "");
                // Some browsers send full path, strip it
                int slash = Math.max(fn.lastIndexOf('/'), fn.lastIndexOf('\\'));
                return (slash >= 0) ? fn.substring(slash + 1) : fn;
            }
        }
        return null;
    }
}
