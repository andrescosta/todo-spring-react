package activity

import "database/sql"

type Activity struct {
	Id       int
	Name     string
	Act_type string
	Title    sql.NullString
	Summary  sql.NullString
	URI      string
}
