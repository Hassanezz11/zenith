-- Run this entire file in SQL Server Management Studio against localhost (sa login)
-- Matches the class diagram: Utilisateur / Joueur / Administrateur / Jeu / Review / Message / ChatBot

CREATE DATABASE db9;
GO
USE db9;
GO

-- -------------------------------------------------------------------------
-- Utilisateur (base) + Joueur + Administrateur  (single-table inheritance)
-- EstAdmin = 1  →  Administrateur    EstAdmin = 0  →  Joueur
-- -------------------------------------------------------------------------
CREATE TABLE Users (
    UserId       INT IDENTITY(1,1) PRIMARY KEY,
    NomComplet   NVARCHAR(100)  NOT NULL,              -- Utilisateur.nom
    Email        NVARCHAR(150)  NOT NULL UNIQUE,        -- Utilisateur.email
    MotDePasse   NVARCHAR(255)  NOT NULL,               -- Utilisateur.motdepasse
    Rang         NVARCHAR(50)   DEFAULT 'Bronze',        -- Joueur rank
    ZCoins       INT            DEFAULT 0,
    XP           INT            DEFAULT 0,
    EstAdmin     BIT            DEFAULT 0,               -- 1 = Administrateur
    DateCreation DATETIME       DEFAULT GETDATE()
);

-- -------------------------------------------------------------------------
-- Jeu  (-id, -titre, -category, -prix, -description)
-- -------------------------------------------------------------------------
CREATE TABLE Jeux (
    JeuId         INT IDENTITY(1,1) PRIMARY KEY,
    Titre         NVARCHAR(200)  NOT NULL,
    Categorie     NVARCHAR(100),
    Description   NVARCHAR(MAX),
    Prix          DECIMAL(10,2)  DEFAULT 0.0,
    PromoLabel    NVARCHAR(50),
    CouleurAccent NCHAR(7)       DEFAULT '#ccff00'
);

-- -------------------------------------------------------------------------
-- Joueur.ownedGames / Joueur.preferedGames  (EstWishlist = 0 → owned, 1 → wishlist)
-- -------------------------------------------------------------------------
CREATE TABLE UsersJeux (
    Id          INT IDENTITY(1,1) PRIMARY KEY,
    UserId      INT  NOT NULL REFERENCES Users(UserId),
    JeuId       INT  NOT NULL REFERENCES Jeux(JeuId),
    EstWishlist BIT  DEFAULT 0,
    DateAjout   DATETIME DEFAULT GETDATE()
);

-- -------------------------------------------------------------------------
-- Message  (+id, +contenu, +date)
-- ExpediteurId / DestinataireId come from the Joueur→Message relationship
-- -------------------------------------------------------------------------
CREATE TABLE Messages (
    MessageId      INT IDENTITY(1,1) PRIMARY KEY,
    ExpediteurId   INT           NOT NULL REFERENCES Users(UserId),
    DestinataireId INT           NOT NULL REFERENCES Users(UserId),
    Contenu        NVARCHAR(MAX) NOT NULL,
    DateEnvoi      DATETIME      DEFAULT GETDATE(),
    EstLu          BIT           DEFAULT 0
);

-- -------------------------------------------------------------------------
-- Review  (-id, -JoueurId, -JeuId, -rating, -comment)
-- -------------------------------------------------------------------------
CREATE TABLE Avis (
    AvisId      INT IDENTITY(1,1) PRIMARY KEY,
    JeuId       INT NOT NULL REFERENCES Jeux(JeuId),
    UserId      INT NOT NULL REFERENCES Users(UserId),   -- JoueurId in diagram
    Note        INT CHECK (Note BETWEEN 1 AND 5),         -- rating
    Commentaire NVARCHAR(MAX),                            -- comment
    DateAvis    DATETIME DEFAULT GETDATE()
);

-- -------------------------------------------------------------------------
-- ChatBot history  (persists Joueur↔ChatBot interactions)
-- -------------------------------------------------------------------------
CREATE TABLE ChatHistory (
    ChatId    INT IDENTITY(1,1) PRIMARY KEY,
    UserId    INT           NOT NULL REFERENCES Users(UserId),
    Message   NVARCHAR(MAX) NOT NULL,
    Response  NVARCHAR(MAX) NOT NULL,
    SentAt    DATETIME      DEFAULT GETDATE()
);

-- =========================================================================
-- SEED DATA  (mirrors the 6 hardcoded games in ZenithStore.java)
-- =========================================================================
INSERT INTO Jeux (Titre, Categorie, Description, Prix, PromoLabel, CouleurAccent) VALUES
('Cyber Siege',   'Shooter',  'Tactical multiplayer shooter in a cyberpunk city.',   29.99, NULL,   '#ccff00'),
('Void Protocol', 'RPG',      'Deep space RPG with branching narrative.',              0.00, 'FREE', '#6ce5ff'),
('Neon Horizon',  'Racing',   'Futuristic racing with neon-lit tracks.',              19.99, '-20%', '#ff6ad5'),
('Echo Forge',    'Strategy', 'Base-building strategy in a post-apocalyptic world.',  34.99, NULL,   '#ffb347'),
('Dusk Circuit',  'Racing',   'Underground illegal racing with full customization.',   0.00, 'FREE', '#9a7cff'),
('Astral Raid',   'Shooter',  'Cooperative alien-invasion shooter.',                  24.99, '-10%', '#72f1b8');

-- Admin user  (EstAdmin = 1 → Administrateur)
INSERT INTO Users (NomComplet, Email, MotDePasse, Rang, ZCoins, XP, EstAdmin)
VALUES ('Zenith X99', 'admin@zenith.com', 'admin1234', 'Diamond', 5000, 12000, 1);

-- Regular players  (EstAdmin = 0 → Joueur)
INSERT INTO Users (NomComplet, Email, MotDePasse, Rang, ZCoins, XP, EstAdmin)
VALUES ('Nova Strike', 'nova@zenith.com', 'pass1234', 'Gold', 800, 4500, 0);

INSERT INTO Users (NomComplet, Email, MotDePasse, Rang, ZCoins, XP, EstAdmin)
VALUES ('Pixel Ghost', 'ghost@zenith.com', 'pass1234', 'Silver', 300, 1800, 0);

-- Admin owns two games, has one wishlisted
INSERT INTO UsersJeux (UserId, JeuId, EstWishlist) VALUES (1, 1, 0); -- Cyber Siege  owned
INSERT INTO UsersJeux (UserId, JeuId, EstWishlist) VALUES (1, 2, 0); -- Void Protocol owned
INSERT INTO UsersJeux (UserId, JeuId, EstWishlist) VALUES (1, 4, 1); -- Echo Forge   wishlist
