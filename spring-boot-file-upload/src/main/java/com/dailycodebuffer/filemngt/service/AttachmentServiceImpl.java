package com.dailycodebuffer.filemngt.service;

import com.dailycodebuffer.filemngt.entity.Attachment;
import com.dailycodebuffer.filemngt.repository.AttachmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;

    public AttachmentServiceImpl(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    @Override
    public Attachment saveAttachment(MultipartFile file) throws Exception {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (fileName.startsWith("..") || fileName.endsWith("..")) {
                throw new Exception("Filename contains invalid path sequence: " + fileName);
            }

            Attachment attachment = new Attachment(fileName, file.getContentType(), file.getBytes());
            return attachmentRepository.save(attachment);

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Could not save File: " + fileName);
        }
    }

    @Override
    public List<Attachment> searchFiles(String query) {
        return attachmentRepository.findAll().stream()
                .filter(attachment -> attachment.getFileName().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    @Override
    public Attachment getAttachment(String fileId) throws Exception {
        return attachmentRepository
                .findById(Long.valueOf(fileId))
                .orElseThrow(() -> new Exception("File not found with Id: " + fileId));
    }

    // New Method: Retrieve all files
    @Override
    public List<Attachment> getAllFiles() {
        return attachmentRepository.findAll();
    }

    // New Method: Filter files by type
    public List<Attachment> filterFilesByType(String type) {
        return attachmentRepository.findAll().stream()
                .filter(attachment -> matchFileType(type, attachment.getFileType()))
                .collect(Collectors.toList());
    }

    // Utility method to match file types
    private boolean matchFileType(String type, String fileType) {
        switch (type.toLowerCase()) {
            case "pdf":
                return fileType.equalsIgnoreCase("application/pdf");
            case "doc":
                return fileType.equalsIgnoreCase("application/msword")
                        || fileType.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case "image":
                return fileType.startsWith("image/");
            case "other":
                return !(fileType.equalsIgnoreCase("application/pdf") ||
                        fileType.equalsIgnoreCase("application/msword") ||
                        fileType.startsWith("image/"));
            default:
                return false;
        }
    }
}
