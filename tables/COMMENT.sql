-- Table: COMMENT
-- Create order (foreign keys): USER, MOMENT, POST, FRIENDSHIP, COMMENT, REALMOJIS ,REACTS_WITH, TAGGED_ON

CREATE TABLE `COMMENT` (
  `PostID` int NOT NULL,
  `CommentNumber` int NOT NULL,
  `Content` text NOT NULL,
  `TimePosted` datetime NOT NULL,
  `UserID` int NOT NULL,
  PRIMARY KEY (`PostID`,`CommentNumber`),
  KEY `UserID` (`UserID`),
  CONSTRAINT `comment_ibfk_1` FOREIGN KEY (`PostID`) REFERENCES `POST` (`PostID`) ON DELETE CASCADE,
  CONSTRAINT `comment_ibfk_2` FOREIGN KEY (`UserID`) REFERENCES `USER` (`UserID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
