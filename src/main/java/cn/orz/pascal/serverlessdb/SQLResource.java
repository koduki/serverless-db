package cn.orz.pascal.serverlessdb;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/sql")
public class SQLResource {

    @Inject
    DBEngine db;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("test")
    public String test() throws SQLException, IOException, ClassNotFoundException {
        long s = System.nanoTime();
        db.connection((con) -> {
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
                            System.out.println(rs.getString(1));
                        }
                    }
                }
                return Optional.empty();
            } catch (SQLException ex) {
                return Optional.of(ex);
            }
        });
        long e = System.nanoTime();

        String msg = String.format("tracelog:" + "web response(ms): " + "%.3f", ((e - s) / 1_000_000.0));
        System.out.println(msg);

        return "success";
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("execute")
    public boolean execute(@QueryParam("sql") String sql) throws SQLException, IOException, ClassNotFoundException {
        System.out.println("query:" + sql);

        long s = System.nanoTime();
        List<Boolean> result = new ArrayList<>();

        db.connection((con) -> {
            try {
                try (Statement st = con.createStatement()) {
                    boolean r = st.execute(sql);
                    result.add(r);
                }
                con.commit();

                return Optional.empty();
            } catch (SQLException ex) {
                return Optional.of(ex);
            }
        });
        long e = System.nanoTime();

        String msg = String.format("tracelog:" + "web response(ms): " + "%.3f", ((e - s) / 1_000_000.0));
        System.out.println(msg);

        return result.get(0);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("execute_query")
    public List<Map<String, Object>> executeQuery(@FormParam("sql") String sql) throws SQLException, IOException, ClassNotFoundException {
        System.out.println("query:" + sql);

        long s = System.nanoTime();
        List<Map<String, Object>> result = new ArrayList<>();

        db.connection((con) -> {
            try {
                try (Statement st = con.createStatement()) {
                    try (ResultSet rs = st.executeQuery(sql)) {
                        while (rs.next()) {
                            Map<String, Object> r = new HashMap<>();

                            int colmunCount = rs.getMetaData().getColumnCount();
                            for (int i = 0; i < colmunCount; i++) {
                                String name = rs.getMetaData().getColumnName(i+1);
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
        long e = System.nanoTime();

        String msg = String.format("tracelog:" + "web response(ms): " + "%.3f", ((e - s) / 1_000_000.0));
        System.out.println(msg);

        return result;
    }
}
