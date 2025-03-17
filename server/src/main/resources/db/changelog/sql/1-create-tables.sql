create table users (
    id bigint generated always as identity primary key,
    username varchar(64) not null unique,
    password varchar(255) not null,
    created_at timestamp not null
);

create table records (
    id bigint generated always as identity primary key,
    user_id bigint references users(id) not null,
    file_name varchar(256) not null,
    note varchar(32),
    octave smallint,
    created_at timestamp not null
);
