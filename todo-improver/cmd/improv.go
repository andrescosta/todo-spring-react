package main

import (
	"context"
	"os/signal"
	"syscall"
	"time"

	"github.com/andrescosta/todo-spring-react/todo-improver/internal/activity"
	"github.com/andrescosta/todo-spring-react/todo-improver/internal/config"
	"github.com/andrescosta/todo-spring-react/todo-improver/internal/summary"
)

func main() {
	ctx, done := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	ticker := startProducer(ctx)
	println("Started")
	select {
	case <-ctx.Done():
		ticker.Stop()
		done()
	}
	println("Stopped")
}

func startProducer(ctx context.Context) time.Ticker {
	ticker := time.NewTicker(4000 * time.Millisecond)
	go producer(ctx, ticker)
	return *ticker
}

func producer(ctx context.Context, ticker *time.Ticker) error {
	config := config.ImproverConfig{
		DBURL: "postgres://todo_user:mysecretpassword@localhost:5432/todo",
	}
	manager, err := activity.NewManager(ctx, &config)
	if err != nil {
		return err
	}
	defer manager.Close()

	for {
		select {
		case <-ticker.C:
			if a, err := manager.GetActivities(ctx, time.Now()); err != nil {
				println(err.Error())
			} else {
				for _, v := range a {
					err := summary.Fillsummary(v.URI)
					if err != nil {
						println("error:", err)
					}
				}
			}
		case <-ctx.Done():
			return nil
		}
	}
}
