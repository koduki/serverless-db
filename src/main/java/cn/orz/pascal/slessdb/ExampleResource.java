package cn.orz.pascal.slessdb;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class ExampleResource {

    @Inject
    DBEngine db;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() throws SQLException, IOException, ClassNotFoundException {
        String url = "jdbc:h2:file:./db/testdb";

        db.connection(url, (con) -> {
            try {
                try (Statement st = con.createStatement()) {
//                st.execute("CREATE TABLE sample_tbl (name varchar(255))");
                    st.execute("INSERT INTO sample_tbl(name) values('Nanoha')");
                    st.execute("INSERT INTO sample_tbl(name) values('Fate')");
                    st.execute("INSERT INTO sample_tbl(name) values('Arisa')");
                }
                con.commit();

                try (Statement st = con.createStatement()) {
                    try (ResultSet rs = st.executeQuery("SELECT name FROM sample_tbl")) {
                        while (rs.next()) {
                            System.out.println(rs.getString(1));
                        }
                    }
                }
                return Optional.empty();
            } catch (SQLException ex) {
                return Optional.of(ex);
            }
        });

        return "hello";
    }

}
