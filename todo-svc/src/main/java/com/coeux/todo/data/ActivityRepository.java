package com.coeux.todo.data;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.coeux.todo.entities.Activity;
import com.coeux.todo.entities.ActivityState;
import com.coeux.todo.entities.ActivityStatus;
import com.coeux.todo.entities.ActivityType;
import com.coeux.todo.entities.Label;
import com.coeux.todo.entities.MUser;
import com.coeux.todo.entities.Media;
import com.google.gson.Gson;

@Component
public class ActivityRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    JsonParser parser;

    static private final Logger logger = LoggerFactory.getLogger(ActivityRepository.class);

    public ActivityRepository() {
        this.parser = JsonParserFactory.getJsonParser();
    }

    @Transactional
    public int deleteActivity(UUID publicId) {
        return jdbcTemplate.update(DELETE_ACTIVITY, new Object[] { publicId });
    }

    public List<Activity> getActivitiesByUser(UUID publicId) {
        logger.info("query by user: " + publicId);
        var res = jdbcTemplate.query(QUERY_ACTIVITIES_BY_USER, new ActivityRowMapper(),
                new Object[] { publicId });
        return res;
    }

    public List<Activity> getActivitiesByType(UUID publicId, ActivityType type) {
        return jdbcTemplate.query(QUERY_ACTIVITIES_BY_USER_FILTER_BY_TYPE, new ActivityRowMapper(),
                new Object[] { publicId, type.toString() });
    }

    public Activity getActivityByPublicId(UUID muserPublicId, UUID publicId) {
        return jdbcTemplate.queryForObject(SELECT_ACTIVITY_BY_PUBLIC_ID, new ActivityRowMapper(),
                new Object[] { muserPublicId, publicId });
    }

    @Transactional
    public Activity saveActivity(MUser user, Activity activity) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            Array tags = null;
            try {
                tags = connection.createArrayOf("TEXT", activity.tags());
                PreparedStatement stmt = connection.prepareStatement(INSERT_ACTIVITY,
                        Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, activity.name());
                stmt.setString(2, activity.description());
                stmt.setString(3, activity.type() != null ? activity.type().toString() : ActivityType.OTHER.toString());
                stmt.setString(4,
                        activity.state() != null ? activity.state().toString() : ActivityState.active.toString());
                stmt.setString(5,
                        activity.status() != null ? activity.status().toString() : ActivityStatus.pending.toString());
                stmt.setArray(6, tags);
                stmt.setObject(7, new Gson().toJson(activity.extraData()), Types.OTHER);
                stmt.setLong(8, user.id());
                return stmt;

            } finally {
                if (tags != null) {
                    try {
                        tags.free();
                    } catch (SQLException e) {
                        throw new Error(e);
                    }
                }
            }
        }, keyHolder);
        var keys = keyHolder.getKeys();
        if (keys == null || keys.isEmpty()) {
            throw new Error();
        }
        var id = (long) keys.get("id");

        return activity.withIDs(id, (UUID) keys.get("public_id"));
    }

    public class ActivityRowMapper implements RowMapper<Activity> {
        @Override
        public Activity mapRow(ResultSet rs, int rowNum) throws SQLException {
            Activity activity = new Activity(
                    rs.getLong("ac_id"),
                    UUID.fromString(rs.getString("ac_public_id")),
                    rs.getString("ac_name"),
                    rs.getString("ac_description"),
                    null,
                    ActivityType.valueOf(rs.getString("ac_type")),
                    ActivityState.valueOf(rs.getString("ac_state")),
                    ActivityStatus.valueOf(rs.getString("ac_status")),
                    (String[]) rs.getArray("ac_tags").getArray(),
                    null,
                    null,
                    parse(rs.getString("ac_extra_data")));

            return activity;
        }

    }

    private Map<String, Object> parse(String data) {
        return data != null ? parser.parseMap(data) : null;
    }


    static final String INSERT_ACTIVITY = """
        INSERT INTO activity
        (
            name, description, type, state, status, tags,
            extra_data, muser_id
        )
        VALUES
        (?,?,?,?,?,?,?,?)
        """;


    /* Update */
    static final String UPDATE_ACTIVITY = """
            UPDATE activity
            SET
                name=?, description=?, type=?, state=?, status=?, tags=?,
                extra_data=?, media_idv, muser_id=?, label_activity_id=?
            WHERE
            public_id=?
            """;

    static final String DELETE_ACTIVITY = "DELETE FROM activity WHERE public_id=?";

    private static final String BASE_ACTIVITIES_QUERY = """
            SELECT
                mu.id AS mu_id,
                mu.public_id AS mu_public_id,
                mu.created_at AS mu_created_at,
                mu.updated_at AS mu_updated_at,
                mu.full_name AS mu_full_name,
                mu.email AS mu_email,
                ac.id AS ac_id,
                ac.public_id AS ac_public_id,
                ac.created_at AS ac_created_at,
                ac.updated_at AS ac_updated_at,
                ac.name AS ac_name,
                ac.description AS ac_description,
                ac.type AS ac_type,
                ac.state AS ac_state,
                ac.status AS ac_status,
                ac.tags AS ac_tags,
                ac.extra_data AS ac_extra_data
            FROM
            muser AS mu
            JOIN activity AS ac ON mu.id = ac.muser_id
            """;

    private static final String QUERY_ACTIVITIES_BY_USER = BASE_ACTIVITIES_QUERY + " WHERE mu.public_id = ? ";
    private static final String QUERY_ACTIVITIES_BY_USER_FILTER_BY_TYPE = QUERY_ACTIVITIES_BY_USER + " AND ac.type=?";

    /*private static final String QUERY_ACTIVITIES_BY_USER_FILTER_BY_STATE = QUERY_ACTIVITIES_BY_USER + " AND ac.state=?";
    private static final String QUERY_ACTIVITIES_BY_USER_FILTER_BY_STATUS = QUERY_ACTIVITIES_BY_USER
            + " AND ac.status=?";
    private static final String QUERY_ACTIVITIES_BY_LABEL = BASE_ACTIVITIES_QUERY + """
            JOIN label_activity AS lbla ON lbla.activity_id=ac.id
            JOIN label AS lbl ON lbla.label_id = lbl.id
            WHERE mu.public_id = ?
                """;*/
    private static final String SELECT_ACTIVITY_BY_PUBLIC_ID = """
            SELECT
                ac.id AS ac_id,
                ac.public_id AS ac_public_id,
                ac.created_at AS ac_created_at,
                ac.updated_at AS ac_updated_at,
                ac.name AS ac_name,
                ac.description AS ac_description,
                ac.type AS ac_type,
                ac.state AS ac_state,
                ac.status AS ac_status,
                ac.tags AS ac_tags,
                ac.extra_data AS ac_extra_data
            FROM
            muser AS mu
            JOIN activity AS ac ON mu.id = ac.muser_id
            WHERE mu.public_id = ? and ac.public_id = ?;
                    """;
}
