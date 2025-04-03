DROP TABLE IF EXISTS revoked_jwt CASCADE;
DROP TABLE IF EXISTS mfa_token CASCADE;
DROP TABLE IF EXISTS access_token CASCADE;
DROP TABLE IF EXISTS authorization_code CASCADE;
DROP TABLE IF EXISTS sessions CASCADE;
DROP TABLE IF EXISTS ppid CASCADE;
DROP TABLE IF EXISTS user_app_group CASCADE;
DROP TABLE IF EXISTS app_group_app CASCADE;
DROP TABLE IF EXISTS app_group CASCADE;
DROP TABLE IF EXISTS app CASCADE;
DROP TABLE IF EXISTS users_roles CASCADE;
DROP TABLE IF EXISTS role CASCADE;
DROP TABLE IF EXISTS users CASCADE;


create table users
(
    id                    serial
        primary key,
    password              varchar(255) not null,
    email                 varchar(255) not null
        unique,
    phone                 varchar(15),
    avatar_url            text,
    given_name            varchar(255),
    family_name           varchar(255),
    created_at            timestamp    not null,
    last_login_at         timestamp,
    is_active             boolean default true,
    email_verified        boolean default false,
    mfa_enabled           boolean default false,
    recovery_codes        text,
    failed_login_attempts integer default 0,
    locked_until          timestamp
);

alter table users
    owner to stevetosak;

create table role
(
    id   serial
        primary key,
    name varchar(32) not null
        unique
);

alter table role
    owner to stevetosak;

create table users_roles
(
    user_id integer not null
        references users
            on delete cascade,
    role_id integer not null
        references role
            on delete cascade,
    primary key (user_id, role_id)
);

alter table users_roles
    owner to stevetosak;


create table app_group
(
    id         serial
        primary key,
    name       varchar(255),
    created_at timestamp
);

alter table app_group
    owner to stevetosak;

create table app
(
    id            serial
        primary key,
    name          varchar(255) not null,
    user_id       integer      not null
        references users
            on delete cascade,
    redirect_uri  text,
    client_id     varchar(255) not null
        unique,
    client_secret varchar(255) not null,
    created_at    timestamp    not null,
    group_id int references app_group(id)
);

alter table app
    owner to stevetosak;



create table user_app_group
(
    user_id  integer not null
        references users
            on delete cascade,
    group_id integer not null
        references app_group
            on delete cascade,
    primary key (user_id, group_id)
);

alter table user_app_group
    owner to stevetosak;

create table sessions
(
    id               serial
        primary key,
    ipv4_addr        varchar(15),
    created_at       timestamp default CURRENT_TIMESTAMP,
    expires_at       timestamp,
    user_agent       text,
    last_accessed_at timestamp,
    user_id          integer
        references users
            on delete cascade,
    app_id           integer
        references app
            on delete cascade
);

alter table sessions
    owner to stevetosak;

create table authorization_code
(
    id           serial
        primary key,
    code_hash    varchar(64),
    issued_at    timestamp default now(),
    client_id    varchar(255)
        references app (client_id),
    redirect_uri text,
    expires_at   timestamp,
    scope varchar(128),
    used         boolean   default false
);

alter table authorization_code
    owner to stevetosak;

create table ppid
(
    group_id   integer     not null
        references app_group,
    user_id    integer     not null
        references users,
    salt       varchar(16) not null,
    created_at timestamp default CURRENT_TIMESTAMP,
    ppid_hash  varchar(16) not null,
    primary key (user_id, group_id)
);

alter table ppid
    owner to stevetosak;

create table access_token
(
    id         serial
        primary key,
    token_hash varchar(64) not null
        unique,
    client_id  varchar(64) not null,
    auth_code_id int references authorization_code(id) not null,
    expires_at timestamp   not null,
    created_at timestamp default now(),
    revoked    boolean   default false
);

alter table access_token
    owner to stevetosak;

create table mfa_token
(
    id          serial
        primary key,
    token_value varchar(128) not null,
    user_id     integer      not null
        references users,
    created_at  timestamp default CURRENT_TIMESTAMP,
    expires_at  timestamp,
    used        boolean   default false,
    type        varchar(32)
);

alter table mfa_token
    owner to stevetosak;

create table revoked_jwt
(
    jti        varchar(32) not null
        primary key,
    user_id    integer
        references users,
    issued_at  timestamp   not null,
    expired_at timestamp   not null,
    ua_hash    varchar(64),
    ip_hash    varchar(64)
);

alter table revoked_jwt
    owner to stevetosak;

