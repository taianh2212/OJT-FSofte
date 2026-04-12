package com.tourbooking.booking.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface FileService {
    List<String> uploadImages(List<MultipartFile> files);
}
