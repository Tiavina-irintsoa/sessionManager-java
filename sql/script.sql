create sequence idsession;

create table session_value(
    idsession varchar primary key,
    valeur json, 
    invalidate timestamp
);


alter table session_value 
    add column date_insertion timestamp default now();

-- insert
insert into session_value values( ( SELECT left(md5(random()::text), 14) || nextval('idsession'))  , '{ }' );

-- set
UPDATE session_value
SET valeur = valeur::jsonb || '{"value": "new_value"}'::jsonb;;

-- unset
UPDATE session_value
SET valeur = valeur::jsonb - 'nom' where idsession = ....

-- get
SELECT valeur->>'nom' as nombre FROM session_value;

alter table 

-- vaovao
alter table session_value 
add column start timestamp default now();

update session_value
set start = '2023-10-08 08:58:10';

select '03:00:00'::interval - '00:12:56.216093'::interval;
