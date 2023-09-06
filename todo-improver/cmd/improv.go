package main

import (
	"context"
	"database/sql"
	"os/signal"
	"sync"
	"syscall"
	"time"

	"github.com/andrescosta/todo-spring-react/todo-improver/internal/activity"
	"github.com/andrescosta/todo-spring-react/todo-improver/internal/config"
	"github.com/andrescosta/todo-spring-react/todo-improver/internal/workers"
	"github.com/andrescosta/todo-spring-react/todo-improver/pkg/logging"
)

/*

                         +--> Worker 1..n: Get Title
                         |                     |            ------------------------------
                         |                     +--------->  |            Errors results  |
Producer: Get Activities |                                  | Consumer:                  |
                         |                     +--------->  |            Success results |
                         |                     |            ------------------------------
                         +--> Worker 1..n: Get Summary
*/

func main() {
	ctx, done := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	ctx = logging.WithLogger(ctx)
	logger := logging.FromContext(ctx)
	defer func() {
		done()
		if r := recover(); r != nil {
			logger.Fatalw("application panic", "panic", r)
		}
	}()
	config, err := config.GetConfig()
	if err != nil {
		logger.Error("Config not found.")
		return
	}
	logger.Info("Starting ...")
	doStart(ctx, config)
	done()
	logger.Info("Stopped")
}

func doStart(ctx context.Context, config *config.ImproverConfig) {
	logger := logging.FromContext(ctx)
	var wgProcess sync.WaitGroup
	wgProcess.Add(1)
	go start(ctx, &wgProcess, config)
	logger.Info("Started ...")
	wgProcess.Wait()
}

func start(ctx context.Context, wgProcess *sync.WaitGroup, config *config.ImproverConfig) {
	logger := logging.FromContext(ctx)
	defer wgProcess.Done()
	ticker := time.NewTicker(10000 * time.Millisecond)
	manager, err := activity.NewManager(ctx, config)
	if err != nil {
		logger.Error(err)
	}
	defer manager.Close()

	titleChan := make(chan activity.Activity)
	summaryChan := make(chan activity.Activity)
	titleResChan := make(chan workers.WorkerTitleResult)
	summaryResChan := make(chan workers.WorkerSummaryResult)
	errChan := make(chan workers.WorkerError)

	var wgTitleWorker, wgSummaryWorker, wgConsumer, wgProducer sync.WaitGroup
	const numWorkers = 3
	wgTitleWorker.Add(numWorkers)
	wgSummaryWorker.Add(numWorkers)

	for i := 0; i < numWorkers; i++ {
		go func(id int) {
			defer wgTitleWorker.Done()
			workers.GetTitleWorker(ctx, titleChan, errChan, titleResChan)
			logger.Debugf("Title Worker %d stopped.", id)
		}(i)
		go func(id int) {
			defer wgSummaryWorker.Done()
			workers.GetSummaryWorker(ctx, summaryChan, errChan, summaryResChan)
			logger.Debugf("Summary Worker %d stopped.", id)
		}(i)
	}

	wgConsumer.Add(1)
	go func() {
		defer wgConsumer.Done()
		for {
			select {
			case rt := <-titleResChan:
				logger.Debugf("Title for %d", rt.Activity.Id)
				rt.Activity.Title = sql.NullString{
					String: rt.Title,
					Valid:  true,
				}
				err = manager.UpdateActivity(ctx, rt.Activity)
				if err != nil {
					logger.Error(err.Error())
				}
				logger.Debugf("Title done for %d", rt.Activity.Id)
			case rs := <-summaryResChan:
				rs.Activity.Summary = sql.NullString{
					String: rs.Summary,
					Valid:  true,
				}
				err = manager.UpdateActivity(ctx, rs.Activity)
				if err != nil {
					logger.Error(err.Error())
				}
				logger.Debugf("Summary done for %d", rs.Activity.Id)
			case er := <-errChan:
				logger.Errorf("Error processing activity: %d %s", er.Activity.Id, er.Err.Error())
			case <-ctx.Done():
				logger.Debug("Done consumers")
				return
			}
		}
	}()

	wgProducer.Add(1)
	go func() {
		defer ticker.Stop()
		defer wgProducer.Done()
		for {
			select {
			case <-ticker.C:
				if a, err := manager.GetActivities(ctx, time.Now()); err != nil {
					logger.Error(err.Error())
				} else {
					for _, v := range a {
						logger.Debugf("Adding activity %d", v.Id)
						select {
						case titleChan <- v:
							logger.Debugf("Added title activity %d", v.Id)
						case summaryChan <- v:
							logger.Debugf("Added summary activity %d", v.Id)
						case <-ctx.Done():
							return
						}
						logger.Debugf("Done Adding activity %d", v.Id)
					}
				}
			case <-ctx.Done():
				return
			}
		}
	}()

	wgProducer.Wait()
	wgTitleWorker.Wait()
	wgSummaryWorker.Wait()
	wgConsumer.Wait()
	close(titleResChan)
	close(summaryResChan)
	close(errChan)
	close(summaryChan)
	close(titleChan)
	logger.Debugf("done process")
	return
}
