package com.endevor_migration.endevor_migration.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;

@Service
public class MigrationGit {
    @Value("${spring.data.mongodb.uri}")
    private String uri;

    @Value("${spring.data.mongodb.database}")
    private String dbName;

    @Value("${custom.base-path}")
    private String basePath;

    public LinkedHashMap<String, String> mappingData() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("SYSTEM", "FOLDER");
        map.put("STAGE", "BRANCH");
        map.put("SUBSYSTEM", "SUB-FOLDER");
        map.put("TYPE", "SUB-FOLDER");
        map.put("ELEMENT\n \n\tElement 1\n\tElement 2", "REPO FILES\n \n\tFile 1\n\tFile 2");
        return map;
    }

    public boolean transform() {
        File folder = new File(basePath);
        boolean res = false;
        if (folder.exists()) {
            dataRead();
            res = true;
        } else {
            boolean success = folder.mkdirs();
            if (success) {
                dataRead();
                res = true;
            }
        }
        return res;
    }

    private void dataRead() {
        String env = "STUDTEST", env1 = "STUDPROD";

        MongoCursor<Document> cursor2 = null, cursor3 = null;
        MongoCursor<String> cursor = null, cursor1 = null;

        try {
            MongoClient client = MongoClients.create(uri);
            MongoDatabase database = client.getDatabase(dbName);

            HashMap<String, String> Typemap = new HashMap<>();
            Typemap.put("COBCOPY", ".cpy");
            Typemap.put("COBSRCE", ".cbl");
            Typemap.put("COBDCLG", ".cpy");
            Typemap.put("JCLLIB", ".jcl");
            Typemap.put("DCLGLIB", ".lib");
            Typemap.put("PROCESS", ".jcl");

            MongoCollection<Document> sysCollection = database.getCollection("SYSTEM");
            MongoCollection<Document> subSysCollection = database.getCollection("SUBSYSTEM");
            MongoCollection<Document> eleMasterCollection = database.getCollection("ELEMENTMASTER");
            MongoCollection<Document> eleContentCollection = database.getCollection("ELEMENTCONTENT");

            // Bson SystemFields = Projections.fields(Projections.include("system"), Projections.exclude());
            // Bson SubSysFields = Projections.fields(Projections.include("system", "subsystem"), Projections.exclude());
            Bson EleMasterFields = Projections.fields(
                    Projections.include("system", "subsys", "type", "element"),
                    Projections.excludeId());
            Bson EleContentFields = Projections.fields(
                    Projections.include("element", "content", "CCID"),
                    Projections.excludeId());

            Bson filterEnv = eq("env_name", env);
            Bson filterEnv1 = eq("env_name", env1);
            Bson filtercombo = Filters.or(filterEnv, filterEnv1);

            cursor = sysCollection.distinct("system", filtercombo, String.class).iterator();

            while (cursor.hasNext()) {
                String s = cursor.next();
                Bson filter = eq("system", s);
                String Syspath = basePath + "\\" + s;
                FolderStructureCreation(Syspath);

                cursor1 = subSysCollection.distinct("subsystem", Filters.and(filter, filtercombo), String.class)
                        .iterator();

                while (cursor1.hasNext()) {
                    String o = cursor1.next();
                    Bson filter1 = eq("subsys", o);
                    String Subsyspath = basePath + "\\" + s + "\\" + o;
                    FolderStructureCreation(Subsyspath);

                    cursor2 = eleMasterCollection.find(Filters.and(filter, filter1, filtercombo))
                            .projection(EleMasterFields).iterator();

                    while (cursor2.hasNext()) {
                        Document o1 = cursor2.next();
                        String element = o1.getString("element");
                        Object objType = o1.get("type");
                        String type = objType.toString().trim();
                        Bson filter2 = eq("element", element);
                        System.out.println("\n\n" + element + type);
                        String path = basePath + "/" + s + "/" + o + "/" + type;
                        FolderStructureCreation(path);

                        cursor3 = eleContentCollection.find(filter2).projection(EleContentFields).sort(ascending("_id"))
                                .iterator();

                        while (cursor3.hasNext()) {
                            Document o2 = cursor3.next();
                            String fileExtn;
                            if (Typemap.containsKey(type))
                                fileExtn = Typemap.get(type);
                            else
                                fileExtn = "";
                            ArrayList<String> ElementContent = new ArrayList<String>();
                            ElementContent = (ArrayList<String>) o2.get("content");
                            FileCreation(path, element, ElementContent, fileExtn);
                        }
                    }
                }
            }

            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean gitPush(String platform) {
        boolean res = true;
        try {
            String uri = "";
            String username = "";
            String token = "";

            if ("github".equalsIgnoreCase(platform)) {
                uri = "https://github.com/username/repo.git"; // GitHub Repo URL
                username = "username"; // GitHub Username
                token = "token"; // GitHub Token
            } else if ("gitlab".equalsIgnoreCase(platform)) {
                uri = "https://gitlab.com/vrn_zmig_repo/vrx_zmig_repo1.git"; // GitLab Repo URL
                username = "username"; // GitLab Username
                token = "token"; // GitLab Token
            }

            File file = new File(basePath);
            Git git;

            if (new File(file, ".git").exists()) {
                git = Git.open(file);
            } else {
                git = Git.init().setDirectory(file).setInitialBranch("main").call();
            }

            git.add().addFilepattern(".").call();
            git.commit().setMessage("zMigGIT Load").call();

            Set<String> remotes = git.getRepository().getRemoteNames();
            if (remotes.contains("origin")) {
                git.remoteSetUrl().setRemoteName("origin").setRemoteUri(new URIish(uri)).call();
            } else {
                git.remoteAdd().setName("origin").setUri(new URIish(uri)).call();
            }

            git.push()
                    .setRemote("origin")
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token))
                    .call();

            System.out.println("Pushed to remote repository successfully");

        } catch (Exception e) {
            e.printStackTrace();
            res = false;
        }

        return res;
    }

    private void FolderStructureCreation(String path) throws IOException {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
            System.out.println("Created folder structure " + path);
        } else {
            System.out.println("Directory " + path + " exists already");
        }
    }

    private void FileCreation(String path, String fileName, ArrayList<String> content, String fileextn)
            throws IOException {
        fileName = fileName.trim();
        File file = new File(path + "\\" + fileName + fileextn);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        for (int k = 0; k < content.size(); k++) {
            bw.write(content.get(k));
            bw.write("\n");
        }

        bw.close();
        System.out.println("Updated file with new version " + fileName);
    }
}