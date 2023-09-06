package workers

import (
	"context"

	"github.com/andrescosta/todo-spring-react/todo-improver/internal/activity"
)

func GetSummaryWorker(ctx context.Context, activityC <-chan activity.Activity, errors chan<- WorkerError, results chan<- WorkerSummaryResult) {
	for {
		select {
		case activity, ok := <-activityC:
			if ok {
				results <- WorkerSummaryResult{Activity: activity, Summary: ""}
			}
		case <-ctx.Done():
			return
		}
	}
}
