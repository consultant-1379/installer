IF NOT EXISTS (SELECT * FROM sysobjects WHERE NAME = 'Monitor_db')
CREATE TABLE DC.Monitor_db (
DATETIMEID DATE NOT NULL,
ROP CHAR(10),
MAIN_BUFFER_USAGE      INT,
TEMP_BUFFER_USAGE INT
)