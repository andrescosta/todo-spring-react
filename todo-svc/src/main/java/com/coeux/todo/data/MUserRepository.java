package com.coeux.todo.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.coeux.todo.entities.ExtraInfo;
import com.coeux.todo.entities.MUser;

@Component
public class MUserRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Transactional
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

        if (keys == null || keys.isEmpty()) {
            throw new Error("keys map is empty");
        }
        return user.withIDs((long) keys.get("id"), (UUID) keys.get("public_id"));
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
    static final String UPDATE_USER = "UPDATE muser SET full_name=?,email=? WHERE public_id=?";

    static final String DELETE_USER = "DELETE FROM muser WHERE public_id=?";
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
