package ru.pyatkinmv.pognaleey.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pyatkinmv.pognaleey.repository.ResourceRepository;

@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {

  private final ResourceRepository resourceRepository;

  @GetMapping("/{id}")
  public ResponseEntity<InputStreamResource> getResourceById(@PathVariable Long id) {
    try {
      ResourceRepository.ResourceData resource = resourceRepository.findById(id);

      return ResponseEntity.ok()
          .header(
              HttpHeaders.CONTENT_DISPOSITION,
              String.format("attachment; filename=\"%s\"", resource.name()))
          .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
          .body(new InputStreamResource(resource.data()));
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }
}
