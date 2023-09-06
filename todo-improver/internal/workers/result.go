package workers

import (
	"github.com/andrescosta/todo-spring-react/todo-improver/internal/activity"
)

type WorkerTitleResult struct {
	Activity activity.Activity
	Title    string
}

type WorkerSummaryResult struct {
	Activity activity.Activity
	Summary  string
}

type WorkerError struct {
	Activity activity.Activity
	Err      error
}
