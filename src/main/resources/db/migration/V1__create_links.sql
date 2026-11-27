create table links (
    code varchar(16) primary key,
    url  varchar(2048) not null,
    created_at timestamptz not null default now(),
    expires_at timestamptz not null
);

create index idx_links_expires_at on links (expires_at);