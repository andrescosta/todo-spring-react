package config

type ImproverConfig struct {
	DBhost   string
	DBport   string
	DBuser   string
	DBDSN    string
	DBURL    string
	Timeout  int32
	PollFreq int32
}
