package com.endevor_migration.endevor_migration.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.endevor_migration.endevor_migration.service.ImportMongo;
import com.endevor_migration.endevor_migration.service.MigrationGit;
import com.endevor_migration.endevor_migration.service.ValidateService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@CrossOrigin
public class EndevorMigController {

    @Autowired
    ImportMongo importMongo;

    @Autowired
    MigrationGit migGit;

    @Autowired
    ValidateService validateService;

    @Value("${custom.base-path}")
    private String basePath;

    @Value("${custom.report-path}")
    private String reportPath;

    @GetMapping("/")
    public String home() {
        return "zMigGIT";
    }

    @PostMapping("/extract")
    public String extactEnv(@RequestParam("file") MultipartFile file) {

        try {

            InputStream input = file.getInputStream();
            String msg = importMongo.insJsonMongo(input);
            return msg;
        }

        catch (Exception e) {
            e.printStackTrace();
            return "MongoDB Error.";
        }
    }

    @GetMapping("/mapping")
    public Map<String, String> mappingData() throws IOException {
        LinkedHashMap<String, String> mapping = migGit.mappingData();

        return mapping;
    }

    @PostMapping("/transform")
    public ResponseEntity<String> transformPlatform(@RequestBody Map<String, String> data) {
        String source = data.get("sourcePlatform");
        String platform = data.get("platform");
        String type = data.get("type");

        System.out.println("Received platform: " + platform + ", type: " + type);

        StringBuilder msg = new StringBuilder();

        if (source.equalsIgnoreCase("endevor")) {
            boolean transformRes = migGit.transform();
            if (transformRes) {
                msg.append("✅ Transformation completed and files were saved locally.\n");
            }

            else {
                msg.append("❌ Invalid source platform provided.\n");
            }
        } else {
            msg.append("⚠️ Please select a target platform");
        }

        if ("target".equalsIgnoreCase(type)) {
            boolean gitPushRes = migGit.gitPush(platform);

            if (gitPushRes) {
                msg.append("✅ Changes pushed to Git repository.");
            } else {
                msg.append("❌ Failed to push changes to Git.");
            }

        } else {
            msg.append("⚠️ Please select a source platform");
        }

        return ResponseEntity.ok(msg.toString());
    }

    @GetMapping("/refresh-structure")
    public String detectSubFolder() throws IOException {

        String folderPath = basePath + "\\TRAINING";

        File baseFolder = new File(folderPath);

        if (!baseFolder.exists()) {
            System.out.println("❌ Folder not found.");
            return "This Folder is Empty.";
        }

        Map<String, Object> structure = new LinkedHashMap<>();
        structure.put(baseFolder.getName(), validateService.buildFolderTree(baseFolder));

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(structure);

        return json;
    }

    @GetMapping("/refresh")
    public String delFolder() {
        boolean res = validateService.delSubFolder();
        if (res) {
            return "✅ Contents of the local folder deleted successfully.";
        } else {
            return "❌ Failed to delete some contents.";
        }

    }

    @GetMapping("/validate")
    public String validateReport() {

        System.out.println("Reports Create SuccessFully");
        return "✅ Reports Create SuccessFully.";
    }

    @GetMapping("/reports/filesize")
    public ResponseEntity<Resource> openReport() throws FileNotFoundException {

        boolean sizeRes = validateService.folderCreation();

        if (sizeRes) {
            validateService.writeReport();
        }

        String filePath = reportPath + "\\Report.html";

        File file = new File(filePath);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + file.getName())
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }

    @GetMapping("/reports/filecount")
    public ResponseEntity<Resource> openCount() throws FileNotFoundException {

        boolean countRes = validateService.folderCreation();
        if (countRes) {
            validateService.validateReport();
        }

        String filePath = reportPath + "\\ValidateReport.html";

        File file = new File(filePath);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + file.getName())
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }
}
