package framework.utilitaire;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class UploadedFile {
    private final String fieldName;
    private final String originalFilename;
    private final String contentType;
    private final byte[] bytes;

    public UploadedFile(String fieldName, String originalFilename, String contentType, byte[] bytes) {
        this.fieldName = fieldName;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.bytes = bytes != null ? bytes : new byte[0];
    }

    public String getFieldName() { return fieldName; }
    public String getOriginalFilename() { return originalFilename; }
    public String getContentType() { return contentType; }
    public long getSize() { return bytes.length; }

    public byte[] getBytes() { return bytes; }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(bytes);
    }
}
