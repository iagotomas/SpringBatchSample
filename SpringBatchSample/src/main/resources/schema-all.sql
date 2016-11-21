DROP TABLE people IF EXISTS;

CREATE TABLE people  (
    person_id BIGINT IDENTITY NOT NULL PRIMARY KEY,
    first_name VARCHAR(20),
    last_name VARCHAR(20)
);


DROP TABLE processdata IF EXISTS;

CREATE TABLE processdata (
    processdata_id BIGINT IDENTITY NOT NULL PRIMARY KEY,
    parameter  VARCHAR(20) NOT NULL,
    state	   VARCHAR(20) NOT NULL
);

DROP TABLE processdataslice IF EXISTS;
CREATE TABLE processdataslice (
    processdataslice_id BIGINT IDENTITY NOT NULL PRIMARY KEY,
    process_id BIGINT IDENTITY NOT NULL,
    parameter  VARCHAR(20) NOT NULL,
    start	   DATE NOT NULL,
    end		   DATE 
);