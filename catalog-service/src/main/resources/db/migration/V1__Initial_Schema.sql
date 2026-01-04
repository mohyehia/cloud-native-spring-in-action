create table book
(
    id                 bigserial primary key not null,
    author             varchar(200)          not null,
    isbn               varchar(200) unique   not null,
    price              float8                not null,
    title              varchar(200)          not null,
    created_date       timestamp             not null,
    last_modified_date timestamp             not null,
    version            integer               not null
)