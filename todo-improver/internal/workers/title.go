package workers

import (
	"context"
	"errors"
	"fmt"
	"io"
	"net/http"
	"strings"

	"github.com/andrescosta/todo-spring-react/todo-improver/internal/activity"
	"github.com/andrescosta/todo-spring-react/todo-improver/pkg/logging"
	"golang.org/x/net/html"
)

func GetTitleWorker(ctx context.Context, activityc <-chan activity.Activity, errors chan<- WorkerError, results chan<- WorkerTitleResult) {
	logger := logging.FromContext(ctx)
	for {
		select {
		case activity, ok := <-activityc:
			logger.Debugf("Title for %d", activity.Id)
			if ok {
				title, err := getTitle(ctx, activity.URI)
				if err != nil {
					select {
					case errors <- WorkerError{Err: err, Activity: activity}:
					case <-ctx.Done():
						return
					}
				} else {
					if !activity.Title.Valid || (*title != activity.Title.String) {
						select {
						case results <- WorkerTitleResult{Activity: activity, Title: *title}:
						case <-ctx.Done():
							return
						}
						logger.Debugf("Title for %d pushed", activity.Id)
					}
				}
			}
		case <-ctx.Done():
			return
		}
	}

}

func getTitle(ctx context.Context, url string) (*string, error) {
	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return nil, err
	}

	req = req.WithContext(ctx)
	client := http.DefaultClient
	resp, err := client.Do(req)
	if err != nil {
		return nil, err
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("error, status code:%d", resp.StatusCode)
	}
	if err != nil {
		return nil, err
	}
	tokenizer := html.NewTokenizer(resp.Body)
	nexttitle := false
	var title string
	for {
		tokenType := tokenizer.Next()
		token := tokenizer.Token()
		if tokenType == html.ErrorToken {
			if tokenizer.Err() == io.EOF {
				if nexttitle {
					return nil, errors.New("Error parsing. Title text not found.")
				} else {
					return nil, errors.New("Title tag not found.")
				}
			}
			continue
		}
		if nexttitle {
			title = strings.TrimSpace(html.UnescapeString(token.String()))
			return &title, nil
		}

		if tokenType == html.StartTagToken && token.Data == "title" {
			nexttitle = true
		}
	}
}
