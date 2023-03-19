create table developer
(
    id                      bigserial not null,
    activation_token        varchar(255),
    email                   varchar(255),
    full_name               varchar(255),
    password                varchar(255),
    position                varchar(255),
    reset_token             varchar(255),
    reset_token_expire_time timestamp,
    role                    varchar(255),
    status                  varchar(255),
    team_id                 int8,
    primary key (id)
);

create table report
(
    id           bigserial not null,
    end_date     timestamp,
    name         varchar(255),
    start_date   timestamp,
    developer_id int8,
    primary key (id)
);

create table task
(
    id           bigserial not null,
    description  varchar(255),
    end_date     timestamp,
    start_date   timestamp not null,
    status       varchar(255),
    title        varchar(255),
    developer_id int8,
    primary key (id)
);

create table task_report
(
    task_id   int8 not null,
    report_id int8 not null
);

create table team
(
    id          bigserial not null,
    name        varchar(255),
    description varchar(255),
    primary key (id)
);

alter table developer
    add constraint UK_chmq1wdfnhjthxo65affwdnky unique (email);

alter table developer
    add constraint FKb0awdupi48yb8632o6jschhqw foreign key (team_id) references team on delete cascade;

alter table report
    add constraint FK9nn3u8pta0twpflbxvvx07luf foreign key (developer_id) references developer on delete cascade;

alter table task
    add constraint FKeacaw7uu1jgtof0posl41wqo4 foreign key (developer_id) references developer on delete cascade;

alter table task_report
    add constraint FKind2k1gyafmj311cf4muvr2hg foreign key (report_id) references report on delete cascade;

alter table task_report
    add constraint FKeujkcp6d3t6c5wb5hxtlbs6a3 foreign key (task_id) references task on delete cascade;
