package cn.orz.pascal.serverlessdb;

import cn.orz.pascal.serverlessdb.profiles.Trace;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Trace
@Path("/sql")
public class SQLResource {

    @Inject
    Logger logger;

    @Inject
    DBEngine db;

    @GET
    @Path("test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test() throws SQLException, IOException, ClassNotFoundException {
        db.connection("testdb", (con) -> {
            try {
                try (Statement st = con.createStatement()) {
                    st.execute("CREATE TABLE IF NOT EXISTS sample_tbl (name varchar(255))");
                    st.execute("INSERT INTO sample_tbl(name) values('Nanoha')");
                    st.execute("INSERT INTO sample_tbl(name) values('Fate')");
                    st.execute("INSERT INTO sample_tbl(name) values('Arisa')");
                }
                con.commit();

                try (Statement st = con.createStatement()) {
                    try (ResultSet rs = st.executeQuery("SELECT name FROM sample_tbl")) {
                        while (rs.next()) {
//                            System.out.println(rs.getString(1));
                        }
                    }
                }
                return Optional.empty();
            } catch (SQLException ex) {
                return Optional.of(ex);
            }
        });

        return "success";
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("execute_update")
    public int executeUpdate(@FormParam("dbname") String dbname, @FormParam("sql") String sql) throws SQLException, IOException, ClassNotFoundException {
        logger.info("execute_update:" + sql);

        List<Integer> result = new ArrayList<>();

        db.connection(dbname, (con) -> {
            try {
                try (Statement st = con.createStatement()) {
                    int r = st.executeUpdate(sql);
                    result.add(r);
                }
                con.commit();

                return Optional.empty();
            } catch (SQLException ex) {
                return Optional.of(ex);
            }
        });

        return result.get(0);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("execute_query")
    public List<Map<String, Object>> executeQuery(@FormParam("dbname") String dbname, @FormParam("sql") String sql) throws SQLException, IOException, ClassNotFoundException {
        logger.info("execute_query:" + sql);

        List<Map<String, Object>> result = new ArrayList<>();
        db.connection(dbname, (con) -> {
            try {
                try (Statement st = con.createStatement()) {
                    try (ResultSet rs = st.executeQuery(sql)) {
                        while (rs.next()) {
                            Map<String, Object> r = new HashMap<>();

                            int colmunCount = rs.getMetaData().getColumnCount();
                            for (int i = 0; i < colmunCount; i++) {
                                String name = rs.getMetaData().getColumnName(i + 1);
                                r.put(name, rs.getObject(name));
                            }
                            result.add(r);
                        }
                    }
                }

                return Optional.empty();
            } catch (SQLException ex) {
                return Optional.of(ex);
            }
        });

        return result;
    }
}
