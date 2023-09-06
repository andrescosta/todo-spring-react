package main

import (
	"context"
	"database/sql"
	"fmt"
	"os/signal"
	"sync"
	"syscall"
	"time"

	"github.com/andrescosta/todo-spring-react/todo-improver/internal/activity"
	"github.com/andrescosta/todo-spring-react/todo-improver/internal/config"
	"github.com/andrescosta/todo-spring-react/todo-improver/internal/workers"
)

/*

                         +--> Worker 1: Get Title
                         |                    |             Merger Errors results
                         |                    +--------->
Producer: Get Activities |
                         |                     +-------->   Merger Success results
                         |                     |
                         +--> Worker 2: Get Summary
*/

func main() {
	ctx, done := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	println("Starting ...")
	var wgProcess sync.WaitGroup
	wgProcess.Add(1)
	go start(ctx, &wgProcess)
	select {
	case <-ctx.Done():
		done()
	}
	wgProcess.Wait()
	println("Stopped")
}

func start(ctx context.Context, wgProcess *sync.WaitGroup) error {
	ticker := time.NewTicker(10000 * time.Millisecond)
	config := config.ImproverConfig{
		DBURL: "postgres://todo_user:mysecretpassword@localhost:5432/todo",
	}
	manager, err := activity.NewManager(ctx, &config)
	if err != nil {
		return err
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
		go func() {
			workers.GetTitleWorker(ctx, titleChan, errChan, titleResChan)
			wgTitleWorker.Done()
			println("Done title")
		}()
		go func() {
			workers.GetSummaryWorker(ctx, summaryChan, errChan, summaryResChan)
			wgSummaryWorker.Done()
			println("Done summary")
		}()
	}

	wgConsumer.Add(1)
	go func() {
		for {
			select {
			case rt := <-titleResChan:
				fmt.Printf("Title for %d\n", rt.Activity.Id)
				rt.Activity.Title = sql.NullString{
					String: rt.Title,
					Valid:  true,
				}
				err = manager.UpdateActivity(ctx, rt.Activity)
				if err != nil {
					println(err.Error())
				}
				fmt.Printf("Title done for %d\n", rt.Activity.Id)
			case rs := <-summaryResChan:
				rs.Activity.Summary = sql.NullString{
					String: rs.Summary,
					Valid:  true,
				}
				err = manager.UpdateActivity(ctx, rs.Activity)
				if err != nil {
					println(err.Error())
				}
				fmt.Printf("Summary done for %d\n", rs.Activity.Id)
			case er := <-errChan:
				println(er.Activity.Id, er.Err.Error())
			case <-ctx.Done():
				wgConsumer.Done()
				println("Done consumers")
				return
			}
		}
	}()

	wgProducer.Add(1)
	go func() {
	loop:
		for {
			select {
			case <-ticker.C:
				if a, err := manager.GetActivities(ctx, time.Now()); err != nil {
					println(err.Error())
				} else {
					for _, v := range a {
						fmt.Printf("Adding activity %d\n", v.Id)
						select {
						case titleChan <- v:
							fmt.Printf("Added title activity %d\n", v.Id)
						case summaryChan <- v:
							fmt.Printf("Added summary activity %d\n", v.Id)
						case <-ctx.Done():
							break loop
						}
						fmt.Printf("Done Adding activity %d\n", v.Id)
					}
				}
			case <-ctx.Done():
				break loop
			}
		}
		ticker.Stop()
		println("Done ticker")
		wgProducer.Done()
		return

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
	wgProcess.Done()
	println("done process")
	return nil
}
