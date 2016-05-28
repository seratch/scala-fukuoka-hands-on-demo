-- For H2 Database
create table tasks (
  id bigserial not null primary key,
  title varchar(512) not null,
  description varchar(512),
  deadline date not null,
  is_done boolean not null,
  created_at timestamp not null,
  updated_at timestamp not null
)
