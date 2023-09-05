SELECT a.id,a.name,a.title,a.summary,a.type, m.URI 
		 FROM ACTIVITY a 
			  join (SELECT me.activity_id, me.URI FROM MEDIA me WHERE 
					me.URI is not null GROUP BY me.activity_id,me.URI LIMIT 1) m
			  ON a.id=m.activity_id
		 WHERE title is null;

ALTER TABLE ACTIVITY ADD COLUMN summary text;
