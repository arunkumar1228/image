package com.example.image.service;

import com.example.image.entity.Image;
import com.example.image.message.ResponseMessage;
import com.example.image.repo.ImageRepo;
import com.example.image.response.ResponseFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImageService {

    @Autowired
    ImageRepo imageRepo;

    public ResponseEntity<ResponseMessage> store(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        Image image = new Image(fileName, file.getContentType(), file.getSize(), file.getBytes());
        String message = "";
        try {
            imageRepo.save(image);
            message = "Uploaded the file successfully " + file.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + "!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<byte[]> getFile(Long id) {
        Image image = imageRepo.findById(id).get();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getName() + "\"")
                .body(image.getData());
    }

    public ResponseEntity<List<ResponseFile>> getAllFiles() {
        List<ResponseFile> list = imageRepo.findAll().stream().map(image -> {
            String fileDownloadUri = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/files/")
                    .path(String.valueOf(image.getId()))
                    .toUriString();

            return new ResponseFile(
                    image.getName(),
                    fileDownloadUri,
                    image.getType(),
                    image.getData().length);
        }).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }
}
