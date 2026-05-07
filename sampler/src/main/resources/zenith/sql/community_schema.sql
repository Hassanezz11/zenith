-- Run this script in SSMS after the main schema.sql
-- Adds community chat and friend request tables

USE db9;
GO

-- Community messages (visible to all users)
CREATE TABLE CommunityMessages (
    MessageId INT IDENTITY(1,1) PRIMARY KEY,
    UserId    INT          NOT NULL REFERENCES Users(UserId),
    Contenu   NVARCHAR(MAX) NOT NULL,
    DateEnvoi DATETIME     DEFAULT GETDATE()
);
GO

-- Friend requests and accepted friendships
-- UserId1 = sender of the request, UserId2 = receiver
-- Status: 'PENDING' | 'ACCEPTED' | 'REJECTED'
CREATE TABLE Amis (
    Id          INT IDENTITY(1,1) PRIMARY KEY,
    UserId1     INT          NOT NULL REFERENCES Users(UserId),
    UserId2     INT          NOT NULL REFERENCES Users(UserId),
    Status      NVARCHAR(20) NOT NULL DEFAULT 'PENDING',
    DateCreated DATETIME     DEFAULT GETDATE()
);
GO

-- Optional seed: make the two test users friends so messaging works immediately
-- INSERT INTO Amis (UserId1, UserId2, Status) VALUES (2, 3, 'ACCEPTED');
