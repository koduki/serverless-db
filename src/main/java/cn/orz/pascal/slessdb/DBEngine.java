/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.orz.pascal.slessdb;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 *
 * @author koduki
 */
@ApplicationScoped
public class DBEngine {

    @Inject
    @ConfigProperty(name = "serverlessdb.url")
    String url;

    @Inject
    @ConfigProperty(name = "serverlessdb.backup")
    String backup;

    String dbname;

    String bcuketName = "serverlsss-db26y345872t7852";

    public DBEngine() {
    }

    @PostConstruct
    public void init() {
        this.dbname = parseUrl(url);
    }

    synchronized public void connection(Function<Connection, Optional<SQLException>> callback) throws SQLException, ClassNotFoundException, IOException {
        if (!Files.exists(Path.of(dbname))) {
            Files.createDirectories(Path.of("./db"));
        }

        Bucket bucket = Logger.trace("gcs auth(ms): ", () -> {
            Storage storage = StorageOptions.getDefaultInstance().getService();
            return storage.get(bcuketName);
        });

        Logger.trace("gcs read(ms): ", () -> {
            Blob blob = bucket.get(backup);
            try {
                if (blob.exists()) {
                    blob.downloadTo(Path.of(dbname + ".mv.db"));
                }
            } catch (NullPointerException ex) {
                System.err.println("WARN: no db");
            }
        });

        Class.forName("org.h2.Driver");
        try (Connection con = DriverManager.getConnection(url)) {
            Optional<SQLException> r = callback.apply(con);
            if (r.isPresent()) {
                throw r.get();
            }
        } finally {
            Logger.trace("gcs write(ms): ", () -> {
                try {
                    byte[] bytes = Files.readAllBytes(Path.of(dbname + ".mv.db"));
                    bucket.create(backup, bytes);
                    return Optional.empty();
                } catch (IOException ex) {
                    return Optional.of(ex);
                }
            });
        }
    }

    private String parseUrl(String url) {
        String[] xs = url.split(":");
        return xs[xs.length - 1];
    }

}
