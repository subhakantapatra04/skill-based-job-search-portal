
package com.jobsearch.skilljob.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.jobsearch.skilljob.entity.AppUser;
import com.jobsearch.skilljob.enums.RoleType;
import com.jobsearch.skilljob.exception.BadRequestException;
import com.jobsearch.skilljob.exception.ResourceNotFoundException;
import com.jobsearch.skilljob.repository.JobApplicationRepository;
import com.jobsearch.skilljob.repository.UserRepository;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/users/resume")
public class ResumeController {

    private static final String CLOUDINARY_PREFIX = "cloudinary:";

    private final UserRepository users;
    private final JobApplicationRepository applications;
    private final ObjectProvider<Cloudinary> cloudinaryProvider;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.resume.storage:local}")
    private String storageMode;

    public ResumeController(
            UserRepository users,
            JobApplicationRepository applications,
            ObjectProvider<Cloudinary> cloudinaryProvider) {

        this.users = users;
        this.applications = applications;
        this.cloudinaryProvider = cloudinaryProvider;
    }

    // UPLOAD RESUME
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> upload(
            @RequestParam("file") MultipartFile file,
            Authentication auth) throws Exception {

        if (file.isEmpty()) {
            throw new BadRequestException("Choose a resume file.");
        }

        String original = Objects.requireNonNullElse(
                file.getOriginalFilename(), "resume");

        String lower = original.toLowerCase(Locale.ROOT);

        if (!(lower.endsWith(".pdf")
                || lower.endsWith(".doc")
                || lower.endsWith(".docx"))) {

            throw new BadRequestException(
                    "Only PDF, DOC or DOCX files are allowed.");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException(
                    "Maximum resume size is 5 MB.");
        }

        AppUser user = users.findByEmail(auth.getName())
                .orElseThrow();

        String ext = lower.substring(lower.lastIndexOf('.'));

        String url = "/api/users/resume/" + user.getId();

        if ("cloudinary".equalsIgnoreCase(storageMode)) {

            Cloudinary cloudinary = cloudinaryProvider.getIfAvailable();

            if (cloudinary == null) {
                throw new IllegalStateException(
                        "Cloudinary is not configured.");
            }

            String publicId = filename(user.getId()) + ext;

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "type", "authenticated",
                            "public_id", publicId,
                            "asset_folder", "skill-job-search/resumes",
                            "overwrite", true
                    )
            );

            String savedPublicId = (String) uploadResult.get("public_id");

            if (savedPublicId == null || savedPublicId.isBlank()) {
                throw new IllegalStateException(
                        "Cloudinary did not return a public ID.");
            }

            // Store only the private asset identifier in MySQL.
            user.setResumeUrl(CLOUDINARY_PREFIX + savedPublicId);

        } else {

            // Existing local/Docker storage.
            Path dir = Paths.get(uploadDir, "resumes")
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(dir);

            Path target = dir.resolve(filename(user.getId()) + ext);

            file.transferTo(target);

            user.setResumeUrl(url);
        }

        users.save(user);

        return Map.of(
                "message", "Resume uploaded successfully",
                "resumeUrl", url
        );
    }

    // VIEW / DOWNLOAD RESUME
    @GetMapping("/{userId}")
    public ResponseEntity<?> download(
            @PathVariable Long userId,
            Authentication auth) throws Exception {

        AppUser requester = users.findByEmail(auth.getName())
                .orElseThrow();

        AppUser candidate = users.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Candidate not found"));

        // Preserve existing resume authorization rules.
        boolean allowed =
                requester.getId().equals(userId)
                || requester.getRole() == RoleType.ADMIN
                || (requester.getRole() == RoleType.RECRUITER
                    && applications.existsForCandidateAndRecruiter(
                            userId, requester.getId()));

        if (!allowed) {
            throw new BadRequestException(
                    "You are not allowed to view this resume.");
        }

        String storedResume = candidate.getResumeUrl();

        if (storedResume == null || storedResume.isBlank()) {
            throw new ResourceNotFoundException(
                    "Resume not uploaded");
        }

        // Cloudinary storage
        if (storedResume.startsWith(CLOUDINARY_PREFIX)) {

            return downloadFromCloudinary(storedResume);
        }

        // Existing local/Docker storage
        Path dir = Paths.get(uploadDir, "resumes")
                .toAbsolutePath()
                .normalize();

        Path file = null;

        for (String ext : List.of(".pdf", ".docx", ".doc")) {

            Path candidateFile = dir.resolve(filename(userId) + ext);

            if (Files.exists(candidateFile)) {
                file = candidateFile;
                break;
            }
        }

        if (file == null) {
            throw new ResourceNotFoundException(
                    "Resume not uploaded");
        }

        Resource resource = new FileSystemResource(file);

        String type = Files.probeContentType(file);

        if (type == null) {
            type = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(type))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getFileName() + "\""
                )
                .body(resource);
    }

    // SECURE CLOUDINARY DOWNLOAD
    private ResponseEntity<byte[]> downloadFromCloudinary(
            String storedResume) throws Exception {

        Cloudinary cloudinary = cloudinaryProvider.getIfAvailable();

        if (cloudinary == null) {
            throw new IllegalStateException(
                    "Cloudinary is not configured.");
        }

        String publicId = storedResume.substring(
                CLOUDINARY_PREFIX.length());

        int dot = publicId.lastIndexOf('.');

        if (dot < 0) {
            throw new ResourceNotFoundException(
                    "Invalid resume reference.");
        }

        String ext = publicId.substring(dot)
                .toLowerCase(Locale.ROOT);

        if (!(ext.equals(".pdf")
                || ext.equals(".doc")
                || ext.equals(".docx"))) {

            throw new ResourceNotFoundException(
                    "Unsupported resume format.");
        }

        String format = ext.substring(1);

        // Generate a signed URL valid for two minutes.
        String signedUrl = cloudinary.privateDownload(
                publicId,
                format,
                ObjectUtils.asMap(
                        "resource_type", "raw",
                        "type", "authenticated",
                        "expires_at",
                        Instant.now().plusSeconds(120).getEpochSecond()
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(signedUrl))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofByteArray()
        );

        if (response.statusCode() != 200) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to retrieve resume from cloud storage.");
        }

        String contentType = switch (ext) {
            case ".pdf" -> "application/pdf";
            case ".docx" ->
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case ".doc" -> "application/msword";
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };

        String fileName = filenameFromPublicId(publicId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(response.body().length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + fileName + "\""
                )
                .body(response.body());
    }

    private String filenameFromPublicId(String publicId) {
        int slash = publicId.lastIndexOf('/');
        return slash >= 0 ? publicId.substring(slash + 1) : publicId;
    }

    private String filename(Long id) {
        return "candidate-" + id;
    }
}
