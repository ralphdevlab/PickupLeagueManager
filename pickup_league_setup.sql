-- ============================================================
-- Pickup League Manager - Database Setup Script
-- Run this in MySQL Workbench to create the database, table,
-- and sample data used by the Java application.
-- Sample roster uses well-known NBA and soccer players.
-- ============================================================

CREATE DATABASE IF NOT EXISTS pickup_league;
USE pickup_league;

DROP TABLE IF EXISTS players;

CREATE TABLE players (
    player_id          INT PRIMARY KEY,
    first_name         VARCHAR(40)  NOT NULL,
    last_name          VARCHAR(40)  NOT NULL,
    date_of_birth      DATE         NOT NULL,
    sport              VARCHAR(20)  NOT NULL,
    jersey_number      INT          NOT NULL,
    team_id            INT          NOT NULL,
    eligibility_status VARCHAR(30)  NOT NULL
);

-- team_id 10 = Basketball squad A, 11 = Basketball squad B
-- team_id 20 = Soccer squad A,      21 = Soccer squad B
INSERT INTO players
    (player_id, first_name, last_name, date_of_birth, sport, jersey_number, team_id, eligibility_status)
VALUES
    -- NBA players
    (101, 'Luka',     'Doncic',      '1999-02-28', 'Basketball', 77, 10, 'Eligible'),
    (102, 'Jayson',   'Tatum',       '1998-03-03', 'Basketball',  0, 10, 'Eligible'),
    (103, 'Ja',       'Morant',      '1999-08-10', 'Basketball', 12, 10, 'Eligible'),
    (104, 'Anthony',  'Edwards',     '2001-08-05', 'Basketball',  5, 10, 'Eligible'),
    (105, 'Tyrese',   'Haliburton',  '2000-02-29', 'Basketball',  0, 10, 'Eligible'),
    (106, 'Victor',   'Wembanyama',  '2004-01-04', 'Basketball',  1, 11, 'Eligible'),
    (107, 'Devin',    'Booker',      '1996-10-30', 'Basketball',  1, 11, 'Eligible'),
    (108, 'Donovan',  'Mitchell',    '1996-09-07', 'Basketball', 45, 11, 'Eligible'),
    (109, 'LeBron',   'James',       '1984-12-30', 'Basketball', 23, 11, 'Ineligible - Overage'),
    (110, 'Stephen',  'Curry',       '1988-03-14', 'Basketball', 30, 11, 'Eligible'),
    (111, 'Bronny',   'James',        '2004-10-06', 'Basketball',  9, 11, 'Eligible'),
    -- Soccer players
    (201, 'Kylian',   'Mbappe',      '1998-12-20', 'Soccer',      9, 20, 'Eligible'),
    (202, 'Erling',   'Haaland',     '2000-07-21', 'Soccer',      9, 21, 'Eligible'),
    (203, 'Jude',     'Bellingham',  '2003-06-29', 'Soccer',      5, 20, 'Eligible'),
    (204, 'Vinicius', 'Junior',      '2000-07-12', 'Soccer',      7, 20, 'Eligible'),
    (205, 'Bukayo',   'Saka',        '2001-09-05', 'Soccer',      7, 21, 'Eligible'),
    (206, 'Pedri',    'Gonzalez',    '2002-11-25', 'Soccer',      8, 20, 'Eligible'),
    (207, 'Phil',     'Foden',       '2000-05-28', 'Soccer',     47, 21, 'Eligible'),
    (208, 'Lamine',   'Yamal',       '2007-07-13', 'Soccer',     19, 20, 'Ineligible - Underage'),
    (209, 'Lionel',   'Messi',       '1987-06-24', 'Soccer',     10, 21, 'Eligible'),
    (210, 'Cristiano','Ronaldo',     '1985-02-05', 'Soccer',      7, 21, 'Ineligible - Overage'),
    (211, 'Rodri',    'Hernandez',   '1996-06-22', 'Soccer',     16, 20, 'Eligible');

SELECT COUNT(*) AS total_players FROM players;
