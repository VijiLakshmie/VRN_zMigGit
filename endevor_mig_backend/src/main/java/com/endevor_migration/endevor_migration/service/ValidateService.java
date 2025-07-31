package com.endevor_migration.endevor_migration.service;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
//import java.awt.Desktop;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

@Service
public class ValidateService {

    @Value("${custom.base-path}")
    private String basePath;

    @Value("${custom.report-path}")
    private String reportPath;

    @Value("${spring.data.mongodb.uri}")
    private String uri;

    @Value("${spring.data.mongodb.database}")
    private String dbName;

    public boolean folderCreation(){

        File folder = new File(reportPath);

        return folder.exists() || folder.mkdirs();
    }

    public void writeReport() {
        String filePath = reportPath + "\\Report.html";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {

            // HTML Start
            bw.write("<!DOCTYPE html>");
            bw.newLine();
            bw.write("<html><head><meta charset='UTF-8'><title>File Size Report</title>");
            bw.newLine();
            bw.write("<style>");
            bw.write("body { font-family: Arial; padding: 20px; }");
            bw.write("table { border-collapse: collapse; font-size: 13px; }");
            bw.write("th, td { border: 1px solid #999; padding: 6px 10px; text-align: center; }");
            bw.write("th { background-color: #333; color: white; }");
            bw.write("tr:nth-child(even) { background-color: #f2f2f2; }");
            bw.write("h2 { margin-left:10%; font-size: 20px; }");
            bw.write("</style></head><body>");
            bw.newLine();
            bw.write("<h2>File Size Report</h2>");
            bw.newLine();
            bw.write("<table>");
            bw.write(
                    "<thead><tr><th>SYSTEM</th><th>SUBSYSTEM</th><th>TYPE</th><th>ELEMENT</th><th>File Size</th></tr></thead>");
            bw.write("<tbody>");
            bw.newLine();

            File Sysdir = new File(basePath);
            String[] Sys = Sysdir.list();

            for (String sy : Sys) {
                if (sy.equalsIgnoreCase(".git") || sy.equalsIgnoreCase(".gitignore")
                        || sy.equalsIgnoreCase("README.md"))
                    continue;

                File Subsysdir = new File(basePath + "\\" + sy);
                String[] Subsys = Subsysdir.list();

                for (String subsy : Subsys) {
                    if (subsy.equalsIgnoreCase(".gitkeep"))
                        continue;

                    File Typedir = new File(basePath + "\\" + sy + "\\" + subsy);
                    String[] Type = Typedir.list();

                    for (String s : Type) {
                        if (s.equalsIgnoreCase(".gitkeep"))
                            continue;

                        File ElemFiledir = new File(basePath + "\\" + sy + "\\" + subsy + "\\" + s);
                        String[] ElemFile = ElemFiledir.list();

                        for (String value : ElemFile) {
                            if (value.equalsIgnoreCase(".gitkeep"))
                                continue;

                            File f = new File(basePath + "\\" + sy + "\\" + subsy + "\\" + s + "\\" + value);
                            long fileSize = f.length();

                            String row = String.format(
                                    "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%d</td></tr>",
                                    sy, subsy, s, value, fileSize);
                            bw.write(row);
                            bw.newLine();
                        }
                    }
                }
            }

            bw.write("</tbody></table></body></html>");
            System.out.println("HTML report written successfully at: " + filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void validateReport() {
        String filePath = reportPath + "\\ValidateReport.html";
        String env = "STUDTEST";
        String env1 = "STUDPROD";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {

            MongoClient client = MongoClients.create(uri);
            MongoDatabase endevorDb = client.getDatabase(dbName);
            MongoCollection<Document> CollSystem = endevorDb.getCollection("SYSTEM");
            MongoCollection<Document> CollSubsystem = endevorDb.getCollection("SUBSYSTEM");
            MongoCollection<Document> CollElement = endevorDb.getCollection("ELEMENTMASTER");
            MongoCollection<Document> CollElementContent = endevorDb.getCollection("ELEMENTCONTENT");

            MongoCursor<String> cursor, cursor1, cursor2, cursor4;
            MongoCursor<Document> cursor3;

            int LocalElementcount, LocalTotal = 0, DBTotal = 0;

            Bson filterEnv = eq("env_name", env);
            Bson filterEnv1 = eq("env_name", env1);
            Bson filtercombo = Filters.or(filterEnv, filterEnv1);

            // HTML Start
            bw.write("<!DOCTYPE html>");
            bw.newLine();
            bw.write("<html><head><meta charset='UTF-8'><title>Validation Report</title>");
            bw.newLine();
            bw.write("<style>");
            bw.write("body { font-family: Arial; padding: 20px; }");
            bw.write("table { border-collapse: collapse; font-size: 13px; }");
            bw.write("th, td { border: 1px solid #999; padding: 6px 10px; text-align: center; }");
            bw.write("th { background-color: #333; color: white; }");
            bw.write("tr:nth-child(even) { background-color: #f2f2f2; }");
            bw.write("h2 { margin-left: 5%; font-size: 20px; }");
            bw.write("</style></head><body>");
            bw.newLine();
            bw.write("<h2>MongoDB vs Local File Count Report</h2>");
            bw.newLine();
            bw.write("<table>");
            bw.write(
                    "<thead><tr><th>System</th><th>Subsystem</th><th>Type</th><th>MongoDB Count</th><th>Local Count</th></tr></thead>");
            bw.write("<tbody>");
            bw.newLine();

            cursor = CollSystem.distinct("system", filtercombo, String.class).iterator();
            while (cursor.hasNext()) {
                String system = cursor.next();
                Bson filter = eq("system", system);

                cursor1 = CollSubsystem.distinct("subsystem", Filters.and(filter, filtercombo), String.class)
                        .iterator();
                while (cursor1.hasNext()) {
                    String subsystem = cursor1.next();
                    Bson filter1 = eq("subsys", subsystem);

                    cursor2 = CollElement.distinct("type", Filters.and(filter, filter1, filtercombo), String.class)
                            .iterator();
                    while (cursor2.hasNext()) {
                        String type = cursor2.next();
                        Bson filter2 = eq("type", type);

                        // === Local Count ===
                        File ElemFiledir = new File(basePath + "\\" + system + "\\" + subsystem + "\\" + type);
                        String[] ElemFile = ElemFiledir.list();
                        LocalElementcount = ElemFile != null ? ElemFile.length : 0;
                        LocalTotal += LocalElementcount;

                        // === MongoDB Count ===
                        int DBElementcount = 0;
                        cursor4 = CollElement
                                .distinct("element", Filters.and(filter, filter1, filtercombo, filter2), String.class)
                                .iterator();
                        while (cursor4.hasNext()) {
                            String element = cursor4.next();
                            Bson filter3 = eq("element", element);
                            cursor3 = CollElementContent.find(filter3)
                                    .projection(Projections.include("element", "content", "CCID"))
                                    .sort(ascending("_id")).iterator();
                            if (cursor3.hasNext()) {
                                DBElementcount++;
                                DBTotal++;
                            }
                        }

                        // Write row
                        bw.write(String.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%d</td><td>%d</td></tr>",
                                system, subsystem, type, DBElementcount, LocalElementcount));
                        bw.newLine();
                    }
                }
            }

            // TOTAL row
            bw.write(String.format(
                    "<tr><td><b>TOTAL</b></td><td></td><td></td><td><b>%d</b></td><td><b>%d</b></td></tr>",
                    DBTotal, LocalTotal));
            bw.newLine();

            bw.write("</tbody></table></body></html>");
            System.out.println("Validation HTML report written successfully at: " + filePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> buildFolderTree(File folder) {
        Map<String, Object> result = new LinkedHashMap<>();

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    result.put(file.getName(), buildFolderTree(file));
                }
            }
        }

        return result;
    }

    public boolean delSubFolder() {
        File folder = new File(basePath);
        boolean res = false;
        if (folder.exists() && folder.isDirectory()) {
            boolean result = deleteContents(folder);
            if (result) {
                res = true;
                System.out.println("Contents of the folder deleted successfully.");
            } else {
                res = false;
                System.out.println("Failed to delete some contents.");
            }
        } else {
            System.out.println("Folder does not exist.");
        }

        return res;
    }

    private static boolean deleteContents(File dir) {
        boolean allDeleted = true;

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                boolean success;
                if (file.isDirectory()) {
                    success = deleteDirectory(file);
                } else {
                    success = file.delete();
                }

                if (!success) {
                    allDeleted = false;
                    System.out.println("Failed to delete: " + file.getAbsolutePath());
                }
            }
        }
        return allDeleted;
    }

    private static boolean deleteDirectory(File dir) {
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (!deleteDirectory(child)) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}
