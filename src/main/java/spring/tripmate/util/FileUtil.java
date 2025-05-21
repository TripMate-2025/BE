package spring.tripmate.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class FileUtil {

    private final String baseUploadPath = System.getProperty("user.dir") + File.separator + "uploads";
    // 예: C:/Users/yourname/project/uploads

    public String saveFile(MultipartFile file, String subDir) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        try {
            // uploads/post 또는 uploads/consumer 디렉토리 생성
            String uploadPath = baseUploadPath + File.separator + subDir;
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs(); //경로 없으면 자동 생성
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID() + extension;

            String fullPath = uploadPath + File.separator + newFilename;
            file.transferTo(new File(fullPath));

            // 프론트에 보낼 접근 가능 경로로 수정 (정적 리소스 서빙 필요시 별도 설정)
            return "/uploads/" + subDir + "/" + newFilename;

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    public void deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) return;

        // 실제 파일 시스템 경로로 변환
        String normalizedPath = relativePath.replace("/", File.separator);
        String fullPath = baseUploadPath + normalizedPath.substring("/uploads".length());

        File file = new File(fullPath);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                throw new RuntimeException("파일 삭제 실패: " + fullPath);
            }
        }
    }
}
