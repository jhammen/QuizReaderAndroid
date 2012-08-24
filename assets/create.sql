
create table word ( 
	_id			integer primary key autoincrement,
	language	char(2),
	token		varchar(128) unique
);

create index word_language_index on word(language);
create index word_token_index on word(token);

create table title (
	_id			integer primary key autoincrement,
	language	char(2),
	name		varchar(256) unique,
	filepath	varchar(512) unique,
	section				integer,
	total_sections		integer,
	paragraph			integer
);

create table quizword (
	_id			integer primary key autoincrement,
	word_id		integer,
 	title_id	integer,
 	section 	integer,
 	paragraph	integer,
 	foreign key(word_id)	references word(_id),	
 	foreign key(title_id)	references title(_id) ON DELETE CASCADE
);

create table definition (
	_id			integer primary key autoincrement,
 	quizword_id	integer,
 	text		varchar(128),
 	foreign key(quizword_id) REFERENCES quizword(_id) ON DELETE CASCADE
)