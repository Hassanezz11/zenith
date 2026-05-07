-- Run this in SSMS after schema.sql
-- Adds the BannedUsers table for admin ban/unban functionality

USE db9;
GO

IF OBJECT_ID('BannedUsers', 'U') IS NULL
BEGIN
    CREATE TABLE BannedUsers (
        UserId         INT      NOT NULL PRIMARY KEY REFERENCES Users(UserId),
        BannedAt       DATETIME NOT NULL DEFAULT GETDATE(),
        BannedByAdminId INT     NOT NULL REFERENCES Users(UserId)
    );
END
GO
