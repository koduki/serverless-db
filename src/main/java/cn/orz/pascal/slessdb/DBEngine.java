/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.orz.pascal.slessdb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Function;
import javax.enterprise.context.ApplicationScoped;

/**
 *
 * @author koduki
 */
@ApplicationScoped
public class DBEngine {

    synchronized public void connection(String url, Function<Connection, Optional<SQLException>> callback) throws SQLException, ClassNotFoundException, IOException {
        String dbname = parseUrl(url);

        String backup = "/tmp/db.backup";
        Files.copy(Path.of(backup), Path.of(dbname + ".mv.db"), StandardCopyOption.REPLACE_EXISTING);

        Class.forName("org.h2.Driver");
        try (Connection con = DriverManager.getConnection(url)) {
            Optional<SQLException> r = callback.apply(con);
            if (r.isPresent()) {
                throw r.get();
            }
        } finally {
            System.out.println("backup:new");
            Files.copy(Path.of(dbname + ".mv.db"), Path.of(backup), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String parseUrl(String url) {
        String[] xs = url.split(":");
        String dbname = xs[xs.length - 1];
        return dbname;
    }
}
