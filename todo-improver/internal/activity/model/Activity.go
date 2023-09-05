package model

import "database/sql"

type NullString struct {
	sql.NullString
}

type Activity struct {
	Id       int
	Name     string
	Act_type string
	Title    NullString
	Summary  NullString
	URI      string
}
