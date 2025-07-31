package com.endevor_migration.endevor_migration.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

@Service
public class ImportMongo {
    @Value("${spring.data.mongodb.uri}")
    private String uri;

    @Value("${spring.data.mongodb.database}")
    private String dbName;

    public String insJsonMongo(InputStream input) {
        
        MongoClient client = MongoClients.create(uri);
        MongoIterable<String> dbNames = client.listDatabaseNames();
        boolean exist = false;
        for (String names : dbNames) {
            if (names.equalsIgnoreCase(dbName)) {
                exist = true;
                break;
            }
        }

        if (exist) {
            for (int i = 1; i <= 3; i++) {
                try {
                    Thread.sleep(900);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Database '" + dbName + "' already exists.");
            return "Database " + dbName + " already exists.";
        } else {
            MongoDatabase database = client.getDatabase(dbName);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {

                ObjectMapper mapper = new ObjectMapper();

                String line;
                String currentCollection = null;

                while ((line = reader.readLine()) != null) {
                    line = line.replaceAll("[^\\x20-\\x7E\\r\\n{}\\[\\]\":,]", "").trim();

                    if (line.startsWith("##")) {
                        currentCollection = line.substring(2).trim();
                        System.out.println("Detected Collection :: " + currentCollection);
                    }

                    else if (!line.isEmpty() && currentCollection != null && line.startsWith("{")) {
                        StringBuilder builder = new StringBuilder();
                        builder.append(line);
                        while (!line.endsWith("}")) {
                            line = reader.readLine();
                            if (line == null)
                                break;
                            line = line.trim();
                            builder.append(line);
                        }

                        String str = builder.toString();
                        str = str.replaceAll(":(\\s*)0+(\\d+)", ":$1$2");
                        JsonNode jNode = mapper.readTree(str);
                        Document doc = Document.parse(jNode.toString());

                        MongoCollection<Document> collection = database.getCollection(currentCollection);
                        collection.insertOne(doc);
                    }

                }

                System.out.println("\nAll Collections Inserted Successfully");

            } catch (Exception e) {
                e.printStackTrace();
            }

            return "Database " + dbName + " created successfully.";
        }

    }

}
