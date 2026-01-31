CREATE DATABASE danceplanner;
USE danceplanner;
CREATE TABLE club (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE group_level (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    club_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    FOREIGN KEY (club_id) REFERENCES club(id)
);

CREATE TABLE dancer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    club_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    level_id BIGINT,
    FOREIGN KEY (club_id) REFERENCES club(id),
    FOREIGN KEY (level_id) REFERENCES group_level(id)
);

USE danceplanner;

-- dacă tabela club e goală, adaugă un club de test
INSERT INTO club(name) VALUES ('Test Club');

SELECT * FROM club;
ALTER TABLE group_level
    ADD COLUMN description VARCHAR(255) NULL;

DESCRIBE group_level;
DESCRIBE dancer;
INSERT INTO club(name) VALUES ('Test Club');

USE danceplanner;

CREATE TABLE dance_hall (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    club_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    FOREIGN KEY (club_id) REFERENCES club(id)
);

SELECT * FROM dance_hall;

USE danceplanner;

CREATE TABLE coach (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    club_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    FOREIGN KEY (club_id) REFERENCES club(id)
);


USE danceplanner;

CREATE TABLE private_lesson (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    club_id BIGINT NOT NULL,
    dancer_id BIGINT NOT NULL,
    coach_id BIGINT NOT NULL,
    dance_hall_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time   DATETIME NOT NULL,

    FOREIGN KEY (club_id) REFERENCES club(id),
    FOREIGN KEY (dancer_id) REFERENCES dancer(id),
    FOREIGN KEY (coach_id) REFERENCES coach(id),
    FOREIGN KEY (dance_hall_id) REFERENCES dance_hall(id)
);

SHOW CREATE TABLE private_lesson;
DROP TABLE private_lesson;

CREATE TABLE private_lesson (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    club_id BIGINT NOT NULL,
    dancer_id BIGINT NOT NULL,
    coach_id BIGINT NOT NULL,
    dance_hall_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time   DATETIME NOT NULL,

    FOREIGN KEY (club_id) REFERENCES club(id),
    FOREIGN KEY (dancer_id) REFERENCES dancer(id),
    FOREIGN KEY (coach_id) REFERENCES coach(id),
    FOREIGN KEY (dance_hall_id) REFERENCES dance_hall(id)
);

SELECT *FROM danceplanner.private_lesson;
DESCRIBE private_lesson;

USE danceplanner;
CREATE TABLE admin_user (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  username     VARCHAR(50) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role         VARCHAR(20) NOT NULL
);

ALTER TABLE coach
    ADD COLUMN username VARCHAR(50) UNIQUE,
    ADD COLUMN password_hash VARCHAR(255);

ALTER TABLE dancer
    ADD COLUMN username VARCHAR(50) UNIQUE,
    ADD COLUMN password_hash VARCHAR(255);
    
    UPDATE admin_user
SET username      = 'admin',
    password_hash = '$2a$10$6pUPxOtMsNDAEMaKlcx9cuM.KyWITGQyuMNcFLjD.xtBu6IJZAFHG',
    role          = 'ADMIN'
WHERE id = 1;

UPDATE coach
SET username      = 'coach1',
    password_hash = '$2a$10$4T5sfeoXI1002IkQ174L1OlS3yfXMKRBuhXlziOgTXOirUX5XI/Aq'
WHERE id = 1;

UPDATE dancer
SET username      = 'dancer1',
    password_hash = '$2a$10$bLvQzOZGRh9qRlB07Ae/r.ubHMwyV0lqUGSYay74v838JO2l6QaKm'
WHERE id = 1;
dancer
SELECT *from danceplanner.coach;

select id, username, password_hash from dancer where username is not null;

insert into dancer_account(club_id, username, password_hash)
values (1, '', '$2a$10$DcQUfYVjojM/pvB2Cu.3mu5Di3mi5zc2s67sXWV0xRpcw0TE3u9IS');

update dancer
set username='deea', password_hash='$2a$10$Hz/rN04SJ2FhUjtpiHNg5uSe3kID4mJS/hjJllv2l3I0IzQGwkHnO'
where id=7 and club_id=1;

SELECT username, LENGTH(password_hash) AS len
FROM dancer
WHERE username = 'dancer1';

update coach
set username='mike', password_hash='$2a$10$Hz/rN04SJ2FhUjtpiHNg5uSe3kID4mJS/hjJllv2l3I0IzQGwkHnO'
where id=6;


CREATE TABLE IF NOT EXISTS timetable_entry (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  club_id BIGINT NOT NULL,

  day_of_week INT NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,

  hall_id BIGINT NOT NULL,
  coach_id BIGINT NOT NULL,
  group_level_id BIGINT NOT NULL
);
CREATE INDEX idx_tt_group_day
  ON timetable_entry (club_id, group_level_id, day_of_week);

CREATE INDEX idx_tt_coach_day
  ON timetable_entry (club_id, coach_id, day_of_week);

CREATE INDEX idx_tt_hall_day
  ON timetable_entry (club_id, hall_id, day_of_week);


create table if not exists dancer_availability (
  id bigint auto_increment primary key,
  club_id bigint not null,
  dancer_id bigint not null,
  day_of_week int not null,      -- 1..7
  start_time time not null,
  end_time time not null,

  foreign key (club_id) references club(id),
  foreign key (dancer_id) references dancer(id)
);

create table if not exists dancer_private_request (
  id bigint auto_increment primary key,
  club_id bigint not null,
  dancer_id bigint not null,
  total_hours int not null,
  updated_at timestamp default current_timestamp on update current_timestamp,

  foreign key (club_id) references club(id),
  foreign key (dancer_id) references dancer(id),
  unique key uq_req (club_id, dancer_id)
);

create table if not exists dancer_private_request (
  id bigint auto_increment primary key,
  club_id bigint not null,
  dancer_id bigint not null,
  total_hours int not null,
  updated_at timestamp default current_timestamp on update current_timestamp,

  foreign key (club_id) references club(id),
  foreign key (dancer_id) references dancer(id),
  unique key uq_req (club_id, dancer_id)
);

create table if not exists dancer_private_request_preference (
  id bigint auto_increment primary key,
  request_id bigint not null,
  coach_id bigint not null,
  hours int not null,

  foreign key (request_id) references dancer_private_request(id) on delete cascade,
  foreign key (coach_id) references coach(id)
);

CREATE TABLE IF NOT EXISTS coach_availability (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  club_id BIGINT NOT NULL,
  coach_id BIGINT NOT NULL,
  day_of_week INT NOT NULL,   -- 1..7
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,

  FOREIGN KEY (club_id) REFERENCES club(id),
  FOREIGN KEY (coach_id) REFERENCES coach(id)
);



CREATE TABLE IF NOT EXISTS calendar_event (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  club_id BIGINT NOT NULL,

  event_type VARCHAR(10) NOT NULL,     -- 'GROUP' sau 'PRIVATE'

  start_dt DATETIME NOT NULL,
  end_dt   DATETIME NOT NULL,

  hall_id  BIGINT NOT NULL,
  coach_id BIGINT NULL,

  group_level_id BIGINT NULL,          -- doar pt GROUP
  dancer_id      BIGINT NULL,          -- doar pt PRIVATE (dansatorul)

  source_timetable_entry_id BIGINT NULL, -- daca a venit din timetable_entry
  source_request_id BIGINT NULL,          -- daca a venit din request (optional)

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (club_id) REFERENCES club(id),
  FOREIGN KEY (hall_id) REFERENCES dance_hall(id),
  FOREIGN KEY (coach_id) REFERENCES coach(id),
  FOREIGN KEY (group_level_id) REFERENCES group_level(id),
  FOREIGN KEY (dancer_id) REFERENCES dancer(id)
);

-- index-uri utile pt cautari + overlap
CREATE INDEX idx_calendar_time ON calendar_event (club_id, start_dt, end_dt);
CREATE INDEX idx_calendar_hall ON calendar_event (club_id, hall_id, start_dt, end_dt);
CREATE INDEX idx_calendar_coach ON calendar_event (club_id, coach_id, start_dt, end_dt);
CREATE INDEX idx_calendar_dancer ON calendar_event (club_id, dancer_id, start_dt, end_dt);



DROP TABLE calendar_event;

CREATE TABLE IF NOT EXISTS calendar_event (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  club_id BIGINT NOT NULL,

  event_type VARCHAR(10) NOT NULL,  -- 'GROUP' / 'PRIVATE'

  day_of_week INT NOT NULL,         -- 1..7
  start_time TIME NOT NULL,
  end_time   TIME NOT NULL,

  hall_id BIGINT NOT NULL,
  coach_id BIGINT NOT NULL,

  group_level_id BIGINT NULL,       -- doar pt GROUP
  dancer_id      BIGINT NULL,       -- doar pt PRIVATE

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (club_id) REFERENCES club(id),
  FOREIGN KEY (hall_id) REFERENCES dance_hall(id),
  FOREIGN KEY (coach_id) REFERENCES coach(id),
  FOREIGN KEY (group_level_id) REFERENCES group_level(id),
  FOREIGN KEY (dancer_id) REFERENCES dancer(id)
);

CREATE INDEX idx_cal_time   ON calendar_event (club_id, day_of_week, start_time, end_time);
CREATE INDEX idx_cal_hall   ON calendar_event (club_id, hall_id, day_of_week, start_time, end_time);
CREATE INDEX idx_cal_coach  ON calendar_event (club_id, coach_id, day_of_week, start_time, end_time);
CREATE INDEX idx_cal_dancer ON calendar_event (club_id, dancer_id, day_of_week, start_time, end_time);



CREATE TABLE IF NOT EXISTS timetable_private_entry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    club_id BIGINT NOT NULL,

    day_of_week INT NOT NULL,      -- 1..7
    start_time TIME NOT NULL,
    end_time   TIME NOT NULL,

    hall_id BIGINT NOT NULL,
    coach_id BIGINT NOT NULL,
    dancer_id BIGINT NOT NULL,

    FOREIGN KEY (club_id) REFERENCES club(id),
    FOREIGN KEY (hall_id) REFERENCES dance_hall(id),
    FOREIGN KEY (coach_id) REFERENCES coach(id),
    FOREIGN KEY (dancer_id) REFERENCES dancer(id),

    CHECK (day_of_week BETWEEN 1 AND 7),
    CHECK (start_time < end_time)
);
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'danceplanner'
  AND table_name IN (
    'club',
    'group_level',
    'dancer',
    'coach',
    'dance_hall',
    'timetable_entry'
  );
  
  SHOW TABLE STATUS FROM danceplanner;

DESCRIBE dancer;
DESCRIBE timetable_entry;

SELECT table_name, constraint_name, constraint_type
FROM information_schema.table_constraints
WHERE table_schema = 'danceplanner'
  AND table_name IN (
    'club',
    'group_level',
    'dancer',
    'coach',
    'dance_hall',
    'timetable_entry'
  )
ORDER BY table_name;

SHOW INDEX FROM timetable_entry;

CREATE OR REPLACE VIEW vw_timetable_overview AS
SELECT t.id,
       t.day_of_week,
       t.start_time,
       t.end_time,
       h.name AS hall_name,
       c.name AS coach_name,
       g.name AS group_name,
       d.name AS dancer_name
FROM timetable_entry t
JOIN dance_hall h ON t.hall_id = h.id
JOIN coach c ON t.coach_id = c.id
LEFT JOIN group_level g ON t.group_level_id = g.id
LEFT JOIN dancer d ON t.dancer_id = d.id;

CREATE INDEX idx_tt_group_day
ON calendar_event (club_id, group_level_id, day_of_week);

CREATE INDEX idx_tt_coach_day
ON calendar_event (club_id, coach_id, day_of_week);

CREATE INDEX idx_tt_hall_day
ON calendar_event (club_id, hall_id, day_of_week);

SHOW INDEX FROM calendar_event;

SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'danceplanner'
  AND table_name IN (
    'club',
    'group_level',
    'dancer',
    'coach',
    'dance_hall',
    'calendar_event'
  );
  
  describe calendar_event;
  
  SELECT table_name, constraint_name, constraint_type
FROM information_schema.table_constraints
WHERE table_schema = 'danceplanner'
  AND table_name IN (
    'club',
    'group_level',
    'dancer',
    'coach',
    'dance_hall',
    'calendar_event'
  )
ORDER BY table_name;

DROP INDEX idx_tt_group_day ON timetable_entry;
DROP INDEX idx_tt_coach_day ON timetable_entry;
DROP INDEX idx_tt_hall_day  ON timetable_entry;

CREATE INDEX idx_ce_group_day
ON calendar_event (club_id, group_level_id, day_of_week);

CREATE INDEX idx_ce_coach_day
ON calendar_event (club_id, coach_id, day_of_week);

CREATE INDEX idx_ce_hall_day
ON calendar_event (club_id, hall_id, day_of_week);

SHOW INDEX FROM calendar_event;

CREATE OR REPLACE VIEW vw_calendar_overview AS
SELECT ce.id,
       ce.day_of_week,
       ce.start_time,
       ce.end_time,
       ce.event_type,
       h.name AS hall_name,
       c.name AS coach_name,
       g.name AS group_name,
       d.name AS dancer_name
FROM calendar_event ce
JOIN dance_hall h ON ce.hall_id = h.id
JOIN coach c ON ce.coach_id = c.id
LEFT JOIN group_level g ON ce.group_level_id = g.id
LEFT JOIN dancer d ON ce.dancer_id = d.id;

SELECT * FROM vw_calendar_overview;

DROP TABLE IF EXISTS timetable_private_entry;
DROP TABLE IF EXISTS private_lesson;