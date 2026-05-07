USE db9;
GO

IF OBJECT_ID('Formations', 'U') IS NULL
BEGIN
    CREATE TABLE Formations (
        FormationId  INT IDENTITY(1,1) PRIMARY KEY,
        UserId       INT            NOT NULL REFERENCES Users(UserId),
        Titre        NVARCHAR(200)  NOT NULL,
        Etablissement NVARCHAR(200),
        Annee        NVARCHAR(20),
        Description  NVARCHAR(MAX),
        DateAjout    DATETIME       DEFAULT GETDATE()
    );
END
GO

INSERT INTO Formations (UserId, Titre, Etablissement, Annee, Description) VALUES
(1, 'Master en Ingenierie Logicielle', 'ENSIAS', '2024', 'Specialisation en architecture logicielle et IA.'),
(1, 'Certification Unity Pro', 'Unity Technologies', '2023', 'Developpement de jeux 3D temps reel.'),
(2, 'Licence en Informatique', 'FST Tanger', '2022', 'Programmation et bases de donnees.'),
(3, 'Bootcamp Game Design', 'Zenith Academy', '2025', 'Conception de jeux multi-joueurs.');
