create table discussions(
  id serial primary key,
  subject varchar(255) not null
);

create table posts(
  id serial primary key,
  nick varchar(40) not null,
  contents text not null,
  email varchar(100) not null,
  create_date timestamp not null,
  secret varchar(100) not null,
  discussion_id bigint not null,
  foreign key (discussion_id) references discussions(id)
);

create index post_discussions_fk on posts(discussion_id);