package com.coeux.todo.data;

import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.coeux.todo.entities.ExtraInfo;
import com.coeux.todo.entities.Media;
import com.coeux.todo.entities.MediaType;
import com.google.gson.Gson;

@Component
public class MediaRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    JsonParser parser;

    public MediaRepository(){
        this.parser = JsonParserFactory.getJsonParser();
    }

    @Transactional
    public Media[] saveMedia(long id, Media[] media) {
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
        return media.withIDs((long) keys.get("id"), (UUID) keys.get("public_id"));
    }

    public List<Media> getMediaByActivity(long id) {
        return jdbcTemplate.query(SELECT_MEDIA_BY_ACTIVITY_ID, new MediaRowMapper(), new Object[] { id });
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

    private Map<String, Object> parse(String data) {
        return data != null ? parser.parseMap(data) : null;
    }


    static final String INSERT_MEDIA = "INSERT INTO media (name, description, type, uri, extra_data, activity_id) VALUES (?,?,?,?,?,?)";
    static final String UPDATE_MEDIA = "UPDATE media SET name=?, description=?, type=?, uri=?, extra_data=? WHERE public_id=?";
    static final String DELETE_MEDIA = "DELETE  FROM media WHERE public_id=?";

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


}
