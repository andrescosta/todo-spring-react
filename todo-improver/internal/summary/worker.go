package summary

import (
	"io"
	"net/http"

	"golang.org/x/net/html"
)

func Fillsummary(url string) error {
	resp, err := http.Get(url)
	if resp.StatusCode != http.StatusOK {
		return nil
	}
	if err != nil {
		return err
	}
	tokenizer := html.NewTokenizer(resp.Body)
	title := false
	for {
		tokenType := tokenizer.Next()
		token := tokenizer.Token()
		if tokenType == html.ErrorToken {
			if tokenizer.Err() == io.EOF {
				return nil
			}
			continue
		}
		if title {
			title = false
			println(html.UnescapeString(token.String()))
			return nil
		}

		if tokenType == html.StartTagToken && token.Data == "title" {
			title = true
		}
	}
}
