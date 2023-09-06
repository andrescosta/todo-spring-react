package config

import (
	"encoding/json"
	"fmt"
	"os"
)

type ImproverConfig struct {
	DBDSN    string `json:"dbdsn"`
	Timeout  int32  `json:"timeout"`
	PollFreq int32  `json:"pollfreq"`
}

func GetConfig() (*ImproverConfig, error) {
	if host, ok := os.LookupEnv("APP_DB_HOST"); ok {
		port := os.Getenv("APP_DB_PORT")
		user := os.Getenv("APP_DB_USER")
		pwd := os.Getenv("APP_DB_PWD")
		db := os.Getenv("APP_DB_DB")
		url := fmt.Sprintf("host=%s port=%s user=%s password=%s dbname=%s sslmode=disable", host, port, user, pwd, db)
		return &ImproverConfig{
			DBDSN: url,
		}, nil
	} else {
		return getFromFile()
	}
}

func getFromFile() (*ImproverConfig, error) {
	file, err := os.Open("../config.json")
	if err != nil {
		return nil, err
	}
	defer func() {
		if err == nil {
			file.Close()
		}
	}()

	config, err := decode(file)
	if err != nil {
		return nil, err
	}
	return config, nil

}

func decode(file *os.File) (*ImproverConfig, error) {
	decoder := json.NewDecoder(file)
	var config ImproverConfig
	err := decoder.Decode(&config)
	return &config, err
}
