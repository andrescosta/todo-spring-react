package com.coeux.todo.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.coeux.todo.entities.Label;

@Component
public class LabelRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void associateLabels(long id, Label[] labels) {
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

    @Transactional
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
        return label.withIDs((long) keys.get("id"), (UUID) keys.get("public_id"));
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

    static final String INSERT_LABEL = "INSERT INTO label (name, description, muser_id) VALUES (?,?,?)";
    static final String INSERT_LABEL_ACTIVITY = "INSERT INTO label_activity (label_id, activity_id) VALUES (?,?)";
    static final String UPDATE_LABEL = "UPDATE label SET name=?, description=? WHERE public_id=?";
    static final String DELETE_LABEL = "DELETE FROM label WHERE public_id=?";
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

}
