package repository

import (
	"context"
	"errors"
	"fmt"
	"time"

	"github.com/andrescosta/todo-spring-react/todo-improver/internal/activity/model"
	"github.com/andrescosta/todo-spring-react/todo-improver/internal/config"
	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type ActivityRepository struct {
	pool *pgxpool.Pool
}

func NewActivityRepositoy(ctx context.Context, conf *config.ImproverConfig) (*ActivityRepository, error) {
	pgxConfig, err := pgxpool.ParseConfig(conf.DBURL)
	if err != nil {
		return nil, fmt.Errorf("failed to parse connection string: %w", err)
	}

	pool, err := pgxpool.NewWithConfig(ctx, pgxConfig)
	if err != nil {
		return nil, fmt.Errorf("failed to create connection pool: %w", err)
	}

	repo := ActivityRepository{
		pool: pool,
	}
	return &repo, nil
}

func (a *ActivityRepository) Close() {
	a.pool.Close()
}

func (a *ActivityRepository) GetActivities(ctx context.Context, when time.Time) ([]model.Activity, error) {
	conn, err := a.pool.Acquire(ctx)
	if err != nil {
		return nil, err
	}
	defer conn.Release()
	/*
	   An activity can have many MEDIA type URIs associated to it.
	   We are taking only ONE for the sake of this demo.
	*/
	/*
		It uses LATERAL JOIN instead of a regular join for creating for-loop like query.
		More info:
			https://sqlfordevs.com/for-each-loop-lateral-join
		Another query considered using windows-functions:
				SELECT a.id,a.name,a.title,a.summary, uris.URI
				FROM ACTIVITY a
				join (
				SELECT me.activity_id, me.URI,
				ROW_NUMBER() OVER(PARTITION BY me.activity_id ORDER BY me.activity_id) AS row_number
				 FROM MEDIA me WHERE me.URI is not null) uris ON a.id=uris.activity_id
				WHERE uris.row_number=1
		More info: https://mode.com/sql-tutorial/sql-window-functions/

	*/
	rows, err := conn.Query(ctx,
		`SELECT a.id,a.name,a.title,a.summary, uris.URI 
		FROM 
		ACTIVITY a 
		LEFT JOIN LATERAL (
		SELECT me.activity_id, me.URI
		FROM MEDIA me WHERE me.URI is not null
		and me.activity_id=a.id
		order by me.created_at
		limit 1) uris ON true`)

	if err != nil {
		return nil, err
	}

	defer rows.Close()

	var activities []model.Activity
	for rows.Next() {
		if err := rows.Err(); err != nil {
			return nil, err
		}
		var activity model.Activity
		if err := rows.Scan(&activity.Id, &activity.Name, &activity.Title,
			&activity.Summary, &activity.URI); err != nil {
			return nil, err
		}
		activities = append(activities, activity)
	}

	return activities, nil
}

func (a *ActivityRepository) UpdateActivity(ctx context.Context, activity model.Activity) error {
	conn, err := a.pool.Acquire(ctx)
	if err != nil {
		return err
	}
	defer conn.Release()

	tx, err := conn.BeginTx(ctx, pgx.TxOptions{IsoLevel: pgx.ReadCommitted})
	if err != nil {
		return err
	}

	const stmtName = "Update activity title and summary"

	_, err = tx.Prepare(ctx, stmtName,
		"UPDATE Activity SET title=$1, summary=$2 WHERE id=$3")

	if err != nil {
		return err
	}

	rows, err := tx.Exec(ctx, stmtName, activity.Title, activity.Summary, activity.Id)

	if err != nil {
		return a.rollBack(ctx, tx, err)
	}

	if rows.RowsAffected() != 1 {
		return a.rollBack(ctx, tx, errors.New("activity_id wrong"))
	}

	return a.commit(ctx, tx)
}

func (a *ActivityRepository) rollBack(ctx context.Context, tx pgx.Tx, err error) error {
	if errr := tx.Rollback(ctx); errr != nil {
		return errors.Join(err, errr)
	}
	return err
}

func (a *ActivityRepository) commit(ctx context.Context, tx pgx.Tx) error {
	if err := tx.Commit(ctx); err != nil {
		return err
	}
	return nil
}
