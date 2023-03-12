/*
 All
 */
SELECT mu.id AS mu_id,
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
    lbl.id AS lbl_id,
    lbl.name AS lbl_name,
    lbl.description AS lbl_description,
    media.id AS media_id,
    media.public_id AS media_public_id,
    media.created_at AS media_created_at,
    media.updated_at AS media_updated_at,
    media.name AS media_name,
    media.description AS media_desciption,
    media.TYPE AS media_type,
    media.URI AS media_uri,
    media.extra_data AS media_extra_data
FROM muser AS mu
    JOIN activity AS ac ON mu.id = ac.muser_id
    JOIN media ON ac.id = media.activity_id
    JOIN label_activity AS lbla ON lbla.activity_id = ac.id
    JOIN label AS lbl ON lbla.label_id = lbl.id;
/*
 activities by user
 */
SELECT mu.id AS mu_id,
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
    ac.tags AS ac_tags
FROM muser AS mu
    JOIN activity AS ac ON mu.id = ac.muser_id
WHERE mu.public_id = ?;
/*
 activity by id
 */
SELECT ac.id AS ac_id,
    ac.public_id AS ac_public_id,
    ac.created_at AS ac_created_at,
    ac.updated_at AS ac_updated_at,
    ac.name AS ac_name,
    ac.description AS ac_description,
    ac.type AS ac_type,
    ac.state AS ac_state,
    ac.status AS ac_status,
    ac.tags AS ac_tags
FROM activity AS ac
WHERE ac.public_id = ?;
/*
 media by activity
 */
SELECT media.id AS media_id,
    media.public_id AS media_public_id,
    media.created_at AS media_created_at,
    media.updated_at AS media_updated_at,
    media.name AS media_name,
    media.description AS media_desciption,
    media.TYPE AS media_type,
    media.URI AS media_uri,
    media.extra_data AS media_extra_data
FROM media
WHERE media.activity_id = ?;
/*
 labels by activity
 */
SELECT lbl.id AS lbl_id,
    lbl.name AS lbl_name,
    lbl.description AS lbl_description
FROM label_activity AS lbla
    JOIN label AS lbl ON lbla.label_id = lbl.id
WHERE lbla.activity_id = ?;

DO $$ BEGIN
SELECT ac.id
FROM activity AS ac
WHERE ac.public_id = 'f129017c-e576-4098-884d-398107a65018';

/*SELECT ac.id AS ac_id
 FROM activity AS ac
 WHERE ac.id = _act.id;
 
 */
END $$ ac_id,
ac_public_id,
ac_created_at,
ac_updated_at,
ac_name,
ac_description,
ac_type,
ac_state,
ac_status,
ac_tags,
lbl_id,
lbl_name,
lbl_description,
media_id,
media_public_id,
media_created_at,
media_updated_at,
media_name,
media_desciption,
media_type,
media_uri,
media_extra_data