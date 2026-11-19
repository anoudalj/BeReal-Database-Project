-- Table: REALMOJIS
-- Create order (foreign keys): USER, MOMENT, POST, FRIENDSHIP, COMMENT, REALMOJIS, REACTS_WITH, TAGGED_ON

CREATE TABLE `REALMOJIS` (
  `RealMojiID` int NOT NULL AUTO_INCREMENT,
  `Emoji` varchar(50) NOT NULL,
  `Image` varchar(255) NOT NULL,
  `UserID` int NOT NULL,
  PRIMARY KEY (`RealMojiID`),
  KEY `UserID` (`UserID`),
  CONSTRAINT `realmojis_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `USER` (`UserID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
