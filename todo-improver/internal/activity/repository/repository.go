package repository

import (
	"context"
	"fmt"
	"time"

	"github.com/andrescosta/todo-spring-react/todo-improver/internal/activity/model"
	"github.com/andrescosta/todo-spring-react/todo-improver/internal/config"
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
	var activities []model.Activity

	conn, err := a.pool.Acquire(ctx)
	defer conn.Release()
	if err != nil {
		return nil, err
	}
	rows, err := conn.Query(ctx,
		`
		SELECT a.id,a.name,a.title,a.summary, uris.URI 
		FROM ACTIVITY a 
		join (
		SELECT me.activity_id, me.URI,
		ROW_NUMBER() OVER(PARTITION BY me.activity_id ORDER BY me.activity_id) AS row_number
		 FROM MEDIA me WHERE me.URI is not null) uris ON a.id=uris.activity_id
		WHERE uris.row_number=1
		`)

	if err != nil {
		return nil, err
	}

	defer rows.Close()

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

	return activities, err
}
