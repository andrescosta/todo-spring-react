package com.coeux.todo.data;

import java.net.URI;
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
import com.coeux.todo.entities.ExtraInfo;
import com.coeux.todo.entities.Label;
import com.coeux.todo.entities.MUser;
import com.coeux.todo.entities.Media;
import com.coeux.todo.entities.MediaType;
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

    public void deleteActivity(UUID publicId) {
        int rows = jdbcTemplate.update(DELETE_ACTIVITY, new Object[] { publicId });
        if (rows == 0) {
            /// TODO: throw an error
        }
    }

    public MUser saveMUser(MUser user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {

            PreparedStatement stmt = connection.prepareStatement(INSERT_USER,
                    Statement.RETURN_GENERATED_KEYS);
            stmt.setObject(1, user.publicId());
            stmt.setString(2, user.name());
            stmt.setString(3, user.email());
            return stmt;
        }, keyHolder);

        var keys = keyHolder.getKeys();

        if (keys == null || keys.size() == 0) {
            throw new Error("keys map is empty");
        }
        MUser user2 = new MUser((long) keys.get("id"), (UUID) keys.get("public_id"), user.name(), user.email(), null,
                null);
        return user2;
    }

    @Transactional
    public Activity saveActivity(UUID publicId, Activity activity) {
        var user = getOrAddMUser(publicId);
        return saveActivity(user, activity);
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
        Media[] newMedia = null;
        if (activity.media() != null) {
            newMedia = saveMedia(id, activity.media());
        }
        if (activity.labels() != null) {
            associateLabels(id, activity.labels());
        }

        return new Activity(id, (UUID) keys.get("public_id"), activity.name(), activity.description(), null,
                activity.type(), activity.state(), activity.status(),
                activity.tags(), newMedia, activity.labels(), activity.extraData());
    }

    private void associateLabels(long id, Label[] labels) {
        for (Label label : labels) {
            associateLabel(id, label);
        }
    }

    @Transactional
    private void associateLabel(long activityId, Label label) {
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_LABEL_ACTIVITY);
            stmt.setLong(1, getLabelByPublicId(label.publicId()).id());
            stmt.setLong(2, activityId);
            return stmt;

        });
    }

    private Media[] saveMedia(long id, Media[] media) {
        var ret = new Media[media.length];
        var i = 0;
        for (Media media2 : media) {
            ret[i++] = saveMedia(id, media2);
        }
        return ret;
    }

    @Transactional
    private Media saveMedia(long activityId, Media media) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_MEDIA,
                    Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, media.name());
            stmt.setString(2, media.description());
            stmt.setString(3, media.type().toString());
            stmt.setString(4, media.uri().toString());
            stmt.setObject(5, media.extraData() != null ? new Gson().toJson(media.extraData()) : null, Types.OTHER);
            stmt.setLong(6, activityId);
            return stmt;

        }, keyHolder);
        var keys = keyHolder.getKeys();
        if (keys == null || keys.isEmpty()) {
            throw new Error();
        }
        return new Media((long) keys.get("id"), (UUID) keys.get("public_id"), media.name(), media.description(),
                media.type(), media.uri(), null, media.extraData());
    }

    public List<Activity> getActivitiesByUser(UUID publicId) {
        logger.info("query by user: "+ publicId );
        var res = jdbcTemplate.query(QUERY_ACTIVITIES_BY_USER, new ActivityRowMapper(),
                new Object[] { publicId });
        return res;
    }

    public List<Activity> getActivitiesByType(UUID publicId, ActivityType type) {
        return jdbcTemplate.query(QUERY_ACTIVITIES_BY_USER_FILTER_BY_TYPE, new ActivityRowMapper(),
                new Object[] { publicId, type.toString() });
    }

    public Activity getActivityByPublicId(UUID publicId) {
        return jdbcTemplate.queryForObject(SELECT_ACTIVITY_BY_PUBLIC_ID, new ActivityRowMapper(),
                new Object[] { publicId });
    }

    public List<Media> getMediaByActivity(long id) {
        return jdbcTemplate.query(SELECT_MEDIA_BY_ACTIVITY_ID, new MediaRowMapper(), new Object[] { id });
    }

    public List<Label> getLabelsByActivity(long id) {
        return jdbcTemplate.query(SELECT_LABELS_BY_ACTIVITY_ID, new LabelRowMapper(), new Object[] { id });
    }

    public Label getLabelByPublicId(UUID publicId) {
        return jdbcTemplate.queryForObject(SELECT_LABEL_BY_PUBLIC_ID, new LabelRowMapper(), new Object[] { publicId });
    }

    public List<Label> getLabelsByMUSerId(UUID publicId) {
        return jdbcTemplate.query(SELECT_LABEL_BY_MUSER_PUBLIC_ID, new LabelRowMapper(),
                new Object[] { publicId });
    }

    public MUser getOrAddMUser(UUID publicId) {
        var muser = getMUSer(publicId, null);
        if (muser == null) {
            muser = saveMUser(new MUser(publicId));
        }
        return muser;
    }

    public MUser getMUSer(UUID publicId, MUser muserDefault) {
        var list = jdbcTemplate.query(SELECT_MUSER_PUBLIC_ID, new MUserRowMapper(), new Object[] { publicId });
        if (list.isEmpty())
            return muserDefault;
        else
            return list.get(0);
    }

    public boolean existMUSer(UUID publicId) {
        var list = jdbcTemplate.query(SELECT_MUSER_PUBLIC_ID, new MUserRowMapper(), new Object[] { publicId });

        return list.size() == 1;
    }

    public Label saveLabel(Label label) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_LABEL,
                    Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, label.name());
            stmt.setString(2, label.description());
            stmt.setLong(3, label.muser().id());
            return stmt;

        }, keyHolder);
        var keys = keyHolder.getKeys();
        if (keys == null || keys.isEmpty()) {
            throw new Error();
        }
        return new Label((long) keys.get("id"), (UUID) keys.get("public_id"), label.name(), label.description(), null);
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
                    getMediaByActivity(rs.getLong("ac_id")).toArray(Media[]::new),
                    getLabelsByActivity(rs.getLong("ac_id")).toArray(Label[]::new),
                    parse(rs.getString("ac_extra_data")));

            return activity;
        }

    }

    private Map<String, Object> parse(String data) {
        return data != null ? parser.parseMap(data) : null;
    }

    public class LabelRowMapper implements RowMapper<Label> {
        @Override
        public Label mapRow(ResultSet rs, int rowNum) throws SQLException {
            Label label = new Label(rs.getLong("lbl_id"),
                    UUID.fromString(rs.getString("lbl_public_id")),
                    rs.getString("lbl_name"),
                    rs.getString("lbl_description"),
                    null);
            return label;
        }
    }

    public class MediaRowMapper implements RowMapper<Media> {
        @Override
        public Media mapRow(ResultSet rs, int rowNum) throws SQLException {
            Media media = new Media(
                    rs.getLong("media_id"),
                    UUID.fromString(rs.getString("media_public_id")),
                    rs.getString("media_name"),
                    rs.getString("media_desciption"),
                    MediaType.valueOf(rs.getString("media_type")),
                    URI.create(rs.getString("media_uri")),
                    new ExtraInfo(rs.getDate("media_created_at").toLocalDate(),
                            rs.getDate("media_updated_at").toLocalDate()),
                    parse(rs.getString("media_extra_data")));
            return media;

        }
    }

    public class MUserRowMapper implements RowMapper<MUser> {
        @Override
        public MUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            MUser muser = new MUser(rs.getLong("mu_id"),
                    UUID.fromString(rs.getString("mu_public_id")),
                    rs.getString("mu_full_name"),
                    rs.getString("mu_email"),
                    new ExtraInfo(rs.getDate("mu_created_at").toLocalDate(),
                            rs.getDate("mu_updated_at").toLocalDate()),
                    null);
            return muser;
        }
    }

    static final String INSERT_USER = "INSERT INTO muser (public_id,full_name,email) VALUES(?,?,?)";
    static final String INSERT_ACTIVITY = """
            INSERT INTO activity
            (
                name, description, type, state, status, tags,
                extra_data, muser_id
            )
            VALUES
            (?,?,?,?,?,?,?,?)
            """;
    static final String INSERT_LABEL_ACTIVITY = "INSERT INTO label_activity (label_id, activity_id) VALUES (?,?)";
    static final String INSERT_LABEL = "INSERT INTO label (name, description, muser_id) VALUES (?,?,?)";
    static final String INSERT_MEDIA = "INSERT INTO media (name, description, type, uri, extra_data, activity_id) VALUES (?,?,?,?,?,?)";

    /* Update */
    static final String UPDATE_USER = "UPDATE muser SET full_name=?,email=? WHERE public_id=?";
    static final String UPDATE_ACTIVITY = """
            UPDATE activity
            SET
                name=?, description=?, type=?, state=?, status=?, tags=?,
                extra_data=?, media_idv, muser_id=?, label_activity_id=?
            WHERE
            public_id=?
            """;
    static final String UPDATE_LABEL = "UPDATE label SET name=?, description=? WHERE public_id=?";
    static final String UPDATE_MEDIA = "UPDATE media SET name=?, description=?, type=?, uri=?, extra_data=? WHERE public_id=?";

    /* Delete */
    static final String DELETE_USER = "DELETE FROM muser WHERE public_id=?";
    static final String DELETE_ACTIVITY = "DELETE FROM activity WHERE public_id=?";
    static final String DELETE_LABEL = "DELETE FROM label WHERE public_id=?";
    static final String DELETE_MEDIA = "DELETE  FROM media WHERE public_id=?";

    /* Queries */
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
    private static final String QUERY_ACTIVITIES_BY_USER_FILTER_BY_STATE = QUERY_ACTIVITIES_BY_USER + " AND ac.state=?";
    private static final String QUERY_ACTIVITIES_BY_USER_FILTER_BY_STATUS = QUERY_ACTIVITIES_BY_USER
            + " AND ac.status=?";
    private static final String QUERY_ACTIVITIES_BY_LABEL = BASE_ACTIVITIES_QUERY + """
            JOIN label_activity AS lbla ON lbla.activity_id=ac.id
            JOIN label AS lbl ON lbla.label_id = lbl.id
            WHERE mu.public_id = ?
                """;
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
            FROM activity AS ac
            WHERE ac.public_id = ?;
                    """;

    private static final String SELECT_MEDIA_BY_ACTIVITY_ID = """
            SELECT
                media.id AS media_id,
                media.public_id AS media_public_id,
                media.created_at AS media_created_at,
                media.updated_at AS media_updated_at,
                media.name AS media_name,
                media.description AS media_desciption,
                media.TYPE AS media_type,
                media.URI AS media_uri,
                media.extra_data AS media_extra_data
            FROM media
            WHERE media.activity_id = ?

                    """;
    private static final String SELECT_LABELS_BY_ACTIVITY_ID = """
            SELECT
                lbl.id AS lbl_id,
                lbl.public_id AS lbl_public_id,
                lbl.name AS lbl_name,
                lbl.description AS lbl_description
            FROM
            label_activity AS lbla
            JOIN label AS lbl ON lbla.label_id = lbl.id
            WHERE lbla.activity_id = ?;

                                    """;

    private static final String SELECT_LABEL_BY_PUBLIC_ID = """
            SELECT
                lbl.id AS lbl_id,
                lbl.public_id AS lbl_public_id,
                lbl.name AS lbl_name,
                lbl.description AS lbl_description
            FROM
            label AS lbl
            WHERE lbl.public_id = ?;

                                    """;

    private static final String SELECT_LABEL_BY_MUSER_PUBLIC_ID = """
            SELECT
                lbl.id AS lbl_id,
                lbl.public_id AS lbl_public_id,
                lbl.name AS lbl_name,
                lbl.description AS lbl_description,
                lbl.muser_id AS lbl_muser_id
            FROM
            label AS lbl
            JOIN muser AS mu ON lbl.muser_id=mu.id
            WHERE mu.public_id = ?;

                                    """;

    private static final String SELECT_MUSER_PUBLIC_ID = """
            SELECT
                mu.id AS mu_id,
                mu.public_id AS mu_public_id,
                mu.created_at AS mu_created_at,
                mu.updated_at AS mu_updated_at,
                mu.full_name AS mu_full_name,
                mu.email AS mu_email
            FROM
                muser AS mu
                WHERE mu.public_id = ?;

                                        """;

}
