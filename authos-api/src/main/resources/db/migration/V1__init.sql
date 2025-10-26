create table public.users
(
    id                    serial
        primary key,
    password              varchar(255) not null,
    email                 varchar(255) not null
        unique,
    phone_number          varchar(15),
    avatar_url            text,
    given_name            varchar(255),
    family_name           varchar(255),
    created_at            timestamp    not null,
    last_login_at         timestamp,
    is_active             boolean   default true,
    email_verified        boolean   default false,
    mfa_enabled           boolean   default false,
    recovery_codes        text,
    failed_login_attempts integer   default 0,
    locked_until          timestamp,
    username              varchar(255),
    website               text,
    gender                varchar(50),
    birthdate             date,
    zone_info             varchar(255),
    locale                varchar(10),
    updated_at            timestamp default CURRENT_TIMESTAMP,
    address               jsonb,
    phone_number_verified boolean   default false,
    middle_name           varchar(128),
    name                  varchar(256)
);

alter table public.users
    owner to stevetosak;

create table public.role
(
    id   serial
        primary key,
    name varchar(32) not null
        unique
);

alter table public.role
    owner to stevetosak;

create table public.users_roles
(
    user_id integer not null
        references public.users
            on delete cascade,
    role_id integer not null
        references public.role
            on delete cascade,
    primary key (user_id, role_id)
);

alter table public.users_roles
    owner to stevetosak;

create table public.app_group
(
    id         serial
        primary key,
    name       varchar(255),
    created_at timestamp,
    user_id    integer
        references public.users,
    is_default boolean     default false,
    sso_policy varchar(32) default 'Partial'::character varying
        constraint app_group_sso_policy_check
            check ((sso_policy)::text = ANY
                   (ARRAY [('Full'::character varying)::text, ('Partial'::character varying)::text, ('Same Domain'::character varying)::text, ('Disabled'::character varying)::text])),
    mfa_policy varchar(32) default 'Disabled'::character varying
        constraint app_group_mfa_policy_check
            check ((mfa_policy)::text = ANY
                   (ARRAY [('Email'::character varying)::text, ('Phone'::character varying)::text, ('Disabled'::character varying)::text]))
);

alter table public.app_group
    owner to stevetosak;

create table public.app
(
    id                             serial
        primary key,
    name                           varchar(255) not null,
    user_id                        integer      not null
        references public.users
            on delete cascade,
    client_id                      varchar(255) not null
        unique,
    client_secret                  varchar(255) not null,
    client_secret_expires_at       timestamp,
    created_at                     timestamp    not null,
    group_id                       integer
        references public.app_group
            on delete cascade,
    grant_types                    varchar(256) not null,
    logo_uri                       text,
    scopes                         varchar(256) not null,
    client_uri                     text,
    response_types                 varchar(255) not null,
    short_description              varchar(256),
    token_endpoint_auth_method     varchar(64) default 'client_secret_post'::character varying
        constraint app_token_endpoint_auth_method_check
            check ((token_endpoint_auth_method)::text = ANY
                   ((ARRAY ['client_secret_basic'::character varying, 'client_secret_post'::character varying, 'private_key_jwt'::character varying, 'none'::character varying])::text[])),
    refresh_token_rotation_enabled boolean     default false,
    duster_callback_uri            text
);

alter table public.app
    owner to stevetosak;

create table public.app_credentials
(
    app_id integer
        references public.app,
    type   varchar(32) default 'client_secret'::character varying not null
);

alter table public.app_credentials
    owner to stevetosak;

create table public.redirect_uris
(
    app_id       integer not null
        references public.app
            on delete cascade,
    redirect_uri text    not null,
    primary key (app_id, redirect_uri)
);

alter table public.redirect_uris
    owner to stevetosak;

create table public.ppid
(
    group_id   integer     not null
        references public.app_group
            on delete cascade,
    user_id    integer     not null
        references public.users
            on delete cascade,
    salt       varchar(16) not null,
    created_at timestamp default CURRENT_TIMESTAMP,
    ppid_hash  varchar(64) not null,
    primary key (user_id, group_id)
);

alter table public.ppid
    owner to stevetosak;

create table public.mfa_token
(
    id          serial
        primary key,
    token_value varchar(128) not null,
    user_id     integer      not null
        references public.users,
    created_at  timestamp default CURRENT_TIMESTAMP,
    expires_at  timestamp,
    used        boolean   default false,
    type        varchar(32)
);

alter table public.mfa_token
    owner to stevetosak;

create table public.authorization_code
(
    code_hash    varchar(64) not null
        primary key,
    issued_at    timestamp default now(),
    client_id    varchar(255)
        references public.app (client_id),
    redirect_uri text,
    scope        varchar(128),
    expires_at   timestamp,
    used         boolean   default false,
    user_id      integer
        references public.users
);

alter table public.authorization_code
    owner to stevetosak;

create table public.access_token
(
    token_hash varchar(64) not null
        primary key,
    client_id  varchar(64) not null,
    expires_at timestamp   not null,
    created_at timestamp default now(),
    revoked    boolean   default false,
    user_id    integer
        references public.users,
    scope      varchar(128)
);

alter table public.access_token
    owner to stevetosak;

create table public.issued_id_tokens
(
    jti               varchar(36) not null
        primary key,
    access_token_hash varchar(64)
                                  references public.access_token
                                      on delete set null,
    audience          varchar(255),
    issue_time        timestamp,
    expiration_time   timestamp,
    sub               varchar(255),
    revoked           boolean default false,
    ua_hash           varchar(64),
    ip_hash           varchar(64)
);

alter table public.issued_id_tokens
    owner to stevetosak;

create table public.refresh_token
(
    client_id    varchar(64)  not null,
    token_val    varchar(128) not null,
    issued_at    timestamp    not null,
    revoked      boolean default false,
    expires_at   timestamp    not null,
    last_used_at timestamp,
    scope        varchar(128) not null,
    user_id      integer      not null
        references public.users,
    token_hash   varchar(64),
    primary key (client_id, user_id)
);

alter table public.refresh_token
    owner to stevetosak;

create index token_hash_idx
    on public.refresh_token using hash (token_hash);

create table public.duster_app
(
    id               serial
        primary key,
    user_id          integer      not null
        references public.users,
    client_id        varchar(255) not null,
    client_secret    varchar(255) not null,
    created_at       timestamp   default now(),
    name             varchar(32),
    token_fetch_mode varchar(12) default 'auto'::character varying
        constraint duster_app_token_fetch_mode_check
            check ((token_fetch_mode)::text = ANY
                   ((ARRAY ['auto'::character varying, 'fresh'::character varying])::text[]))
);

alter table public.duster_app
    owner to stevetosak;

