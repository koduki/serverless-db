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
import javax.ws.rs.QueryParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Trace
@Path("/management")
public class ManagementResource {

    @Inject
    Logger logger;

    @Inject
    DBEngine db;

    @GET
    @Path("clear_db")
    @Produces(MediaType.APPLICATION_JSON)
    public String clear_db(@QueryParam("dbname") String dbname) throws SQLException, IOException, ClassNotFoundException {
        logger.info("clear db:" + dbname);
        db.reset(dbname);

        return "success";
    }
}
