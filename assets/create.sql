
create table title (
	_id			integer primary key autoincrement,
	language	char(2),
	name		varchar(256) unique,
	author		varchar(128) unique,
	filepath	varchar(512) unique,
	section				integer,
	total_sections		integer,
	paragraph			integer
);

create table word ( 
	_id			integer primary key autoincrement,
	language	char(2),
	token		varchar(128) unique,
	quiz_level	integer
);

create index word_language_index on word(language);
create index word_token_index on word(token);


create table definition (
	_id			integer primary key autoincrement,
 	word_id		integer,
 	root_id		integer,
 	title_id	integer,
 	text		varchar(128),
 	foreign key(word_id) REFERENCES word(_id),
 	foreign key(root_id) REFERENCES word(_id),
 	foreign key(title_id) REFERENCES title(_id) ON DELETE CASCADE
)

