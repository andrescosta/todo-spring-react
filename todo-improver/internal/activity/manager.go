package activity

import (
	"context"
	"time"

	"github.com/andrescosta/todo-spring-react/todo-improver/internal/activity/model"
	"github.com/andrescosta/todo-spring-react/todo-improver/internal/activity/repository"
	"github.com/andrescosta/todo-spring-react/todo-improver/internal/config"
)

type Manager interface {
	GetActivities(ctx context.Context, when time.Time) ([]model.Activity, error)
	UpdateActivity(ctx context.Context, activity model.Activity) error
	Close()
}

type MyManager struct {
	config *config.ImproverConfig
	repo   *repository.ActivityRepository
}

func NewManager(ctx context.Context, config *config.ImproverConfig) (Manager, error) {

	r, err := repository.NewActivityRepositoy(ctx, config)
	if err != nil {
		return nil, err
	}
	m := MyManager{
		config: config,
		repo:   r,
	}
	return m, nil
}

func (m MyManager) Close() {
	m.repo.Close()
}

func (m MyManager) GetActivities(ctx context.Context, when time.Time) ([]model.Activity, error) {
	if act, err := m.repo.GetActivities(ctx, when); err != nil {
		return nil, err
	} else {
		return act, err
	}
}

func (m MyManager) UpdateActivity(ctx context.Context, activity model.Activity) error {
	err := m.repo.UpdateActivity(ctx, activity)
	return err
}
