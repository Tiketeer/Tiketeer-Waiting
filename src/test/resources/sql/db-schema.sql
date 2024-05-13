create table if not exists ticketings (
    running_minutes integer,
    created_at timestamp(6),
    event_time timestamp(6),
    price bigint,
    sale_end timestamp,
    sale_start timestamp,
    owner_id binary(16),
    ticketing_id binary(16),
    category varchar(255),
    description TEXT,
    location varchar(255),
    title varchar(255),
    primary key (ticketing_id)
);