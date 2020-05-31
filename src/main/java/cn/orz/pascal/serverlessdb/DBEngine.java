/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.orz.pascal.serverlessdb;

import cn.orz.pascal.serverlessdb.profiles.Trace;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Function;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 *
 * @author koduki
 */
@Trace
@Named
@ApplicationScoped
public class DBEngine {

    String jdbcPrefix = "jdbc:h2:file:";
    String dbdir = "./db/";

    @Inject
    @ConfigProperty(name = "serverlessdb.backupdir")
    String backupdir;

    @Inject
    @ConfigProperty(name = "serverlessdb.bucketname")
    String bcuketName;

    public DBEngine() {
    }

    synchronized public void connection(String dbname, Function<Connection, Optional<SQLException>> callback)
            throws SQLException, ClassNotFoundException, IOException {
        clearLocalDir(dbname);

        Bucket bucket = getBucket();

        readDbFiles(bucket, dbname);

        Class.forName("org.h2.Driver");
        try (Connection con = DriverManager.getConnection(jdbcPrefix + dbdir + dbname)) {
            Optional<SQLException> r = execute(callback, con);
            if (r.isPresent()) {
                throw r.get();
            }
        } finally {
            storeDbFiles(dbname, bucket);
        }
    }

    public void reset(String dbname) {
        Bucket bucket = getBucket();
        Blob blob = bucket.get(backupdir + dbname);
        blob.delete();
    }

    private Optional<SQLException> execute(Function<Connection, Optional<SQLException>> callback,
            final Connection con) {
        Optional<SQLException> r = callback.apply(con);
        return r;
    }

    private void storeDbFiles(String dbname, Bucket bucket) throws IOException {
        byte[] bytes = Files.readAllBytes(getDbFilePath(dbname));
        bucket.create(backupdir + dbname, bytes);
    }

    private void readDbFiles(Bucket bucket, String dbname) {
        Blob blob = bucket.get(backupdir + dbname);
        try {
            if (blob.exists()) {
                blob.downloadTo(getDbFilePath(dbname));
            }
        } catch (NullPointerException ex) {
            System.err.println("WARN: no db");
        }
    }

    private Bucket getBucket() {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        Bucket bucket = storage.get(bcuketName);
        return bucket;
    }

    private void clearLocalDir(String dbname) throws IOException {
        if (!Files.exists(Path.of(dbdir))) {
            Files.createDirectories(Path.of(dbdir));
        }
        Files.deleteIfExists(getDbFilePath(dbname));
    }

    private Path getDbFilePath(String dbname) {
        return Path.of(dbdir + dbname + ".mv.db");
    }
}
