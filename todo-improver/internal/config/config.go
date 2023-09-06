package config

import (
	"encoding/json"
	"os"
)

type ImproverConfig struct {
	DBDSN    string `json:"dbdsn"`
	Timeout  int32  `json:"timeout"`
	PollFreq int32  `json:"pollfreq"`
}

func GetConfig() (*ImproverConfig, error) {
	if dsn, ok := os.LookupEnv("postgress_dsn"); ok {
		return &ImproverConfig{
			DBDSN: dsn,
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
