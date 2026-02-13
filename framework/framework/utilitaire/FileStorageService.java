package framework.utilitaire;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class FileStorageService {
    private static final String DEFAULT_UPLOAD_DIR = "uploads"; // relative to webapp root

    public static String store(UploadedFile file) {
        return storeIn(file, DEFAULT_UPLOAD_DIR);
    }

    public static String storeIn(UploadedFile file, String targetDir) {
        if (file == null || file.getSize() == 0) return null;
        if (targetDir == null || targetDir.trim().isEmpty()) targetDir = DEFAULT_UPLOAD_DIR;
        try {
            Path dir = Paths.get(targetDir).toAbsolutePath();
            Files.createDirectories(dir);
            String safeName = buildSafeFilename(file.getOriginalFilename());
            Path dest = dir.resolve(safeName);
            try (FileOutputStream fos = new FileOutputStream(dest.toFile())) {
                fos.write(file.getBytes());
            }
            return dest.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private static String buildSafeFilename(String original) {
        String base = (original == null || original.isEmpty()) ? "file" : original.replaceAll("[^a-zA-Z0-9._-]", "_");
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String rnd = UUID.randomUUID().toString().substring(0, 8);
        // If original has extension, keep it; otherwise just append timestamp and rnd
        int dot = base.lastIndexOf('.');
        String name = (dot > 0) ? base.substring(0, dot) : base;
        String ext = (dot > 0) ? base.substring(dot) : "";
        return name + "_" + ts + "_" + rnd + ext;
    }
}
