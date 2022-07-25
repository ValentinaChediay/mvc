package org.example.web.dto;

import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

public class UploadFile {

  private MultipartFile file;

  public MultipartFile getFile() {
    return file;
  }

  public void setFile(MultipartFile file) {
    this.file = file;
  }

  public void validate(Errors errors){
    if (file.getSize() == 0) {
      errors.rejectValue("file", "file not selected", "Field value must be not null");
    }
  }
}
