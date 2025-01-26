package ru.pyatkinmv.pognaleey.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.pyatkinmv.pognaleey.security.AuthenticatedUserProvider;
import ru.pyatkinmv.pognaleey.service.AdminService;
import ru.pyatkinmv.pognaleey.service.AdminService.UploadImageDto;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/uploadTitleImage")
    public ResponseEntity<String> uploadResource(@RequestParam(required = false, defaultValue = "true") boolean aiGenerated,
                                                 @RequestParam(required = false, defaultValue = "false") boolean keepOriginal,
                                                 @RequestParam(required = false) @Nullable String authorName,
                                                 @RequestParam(required = false) @Nullable String authorUrl,
                                                 @RequestParam Long guideId,
                                                 @RequestBody MultipartFile file) {
        try {
            validateUser();
            adminService.uploadTitleImage(new UploadImageDto(file, guideId, aiGenerated, keepOriginal, authorName, authorUrl));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // TODO: Add authorization
    private void validateUser() {
        var user = AuthenticatedUserProvider.getCurrentUserOrThrow();
        if (!user.getUsername().equals("pyatkinmv")) {
            throw new AccessDeniedException("You have no rights to do it");
        }
    }
}
