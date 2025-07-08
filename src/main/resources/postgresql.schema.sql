-- PostgreSQL schema for Post entity
-- This will be automatically executed if ddl-auto is set to 'none' or for manual setup

-- auto-generated definition
create table flyway_schema_history
(
    installed_rank integer                 not null
        constraint flyway_schema_history_pk
            primary key,
    version        varchar(50),
    description    varchar(200)            not null,
    type           varchar(20)             not null,
    script         varchar(1000)           not null,
    checksum       integer,
    installed_by   varchar(100)            not null,
    installed_on   timestamp default now() not null,
    execution_time integer                 not null,
    success        boolean                 not null
);

alter table flyway_schema_history
    owner to test;

create index flyway_schema_history_s_idx
    on flyway_schema_history (success);

-- auto-generated definition
create table post
(
    id    bigint not null
        primary key,
    title varchar(255)
);

alter table post
    owner to test;



-- auto-generated definition
create table post_comment
(
    id      bigint not null
        primary key,
    review  varchar(255),
    post_id bigint
        constraint post_comment_post_id_fk
            references post
);

alter table post_comment
    owner to test;



-- auto-generated definition
create table post_tag
(
    post_id bigint not null
        constraint post_tag_post_id_fk
            references post,
    tag_id  bigint not null
        constraint post_tag_tag_id_fk
            references tag,
    primary key (post_id, tag_id)
);

alter table post_tag
    owner to test;




-- Sample data (optional)
-- INSERT INTO post (title, content) VALUES 
--     ('Welcome to Spring Test Bed', 'This is the first post in our PostgreSQL database'),
--     ('JPA Configuration Test', 'Testing JPA configuration with PostgreSQL');
