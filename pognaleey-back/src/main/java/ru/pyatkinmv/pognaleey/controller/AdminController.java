package ru.pyatkinmv.pognaleey.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.pyatkinmv.pognaleey.dto.AdminGuidesCreateDtoList;
import ru.pyatkinmv.pognaleey.dto.AdminUploadImageDto;
import ru.pyatkinmv.pognaleey.dto.TravelGuideInfoDto;
import ru.pyatkinmv.pognaleey.service.AdminService;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

  private final AdminService adminService;

  private static ResponseEntity<String> wrapWithResponse(Runnable runnable) {
    try {
      runnable.run();
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(e.getMessage());
    }

    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping("/uploadTitleImage")
  public ResponseEntity<String> uploadResource(
      @RequestParam(required = false, defaultValue = "true") boolean aiGenerated,
      @RequestParam(required = false, defaultValue = "false") boolean keepOriginal,
      @RequestParam(required = false) @Nullable String authorName,
      @RequestParam(required = false) @Nullable String authorUrl,
      @RequestParam Long guideId,
      @RequestBody MultipartFile file) {
    return wrapWithResponse(
        () ->
            adminService.uploadTitleImage(
                new AdminUploadImageDto(
                    file, guideId, aiGenerated, keepOriginal, authorName, authorUrl)));
  }

  @PutMapping("/generateImageResourcesAsync/forNotFound")
  public ResponseEntity<String> uploadResource() {
    return wrapWithResponse(adminService::generateImageResourcesForNotFoundAsync);
  }

  @PostMapping("/createGuides")
  public List<TravelGuideInfoDto> createGuides(@RequestBody AdminGuidesCreateDtoList dto) {
    return adminService.createGuide(dto);
  }
}
