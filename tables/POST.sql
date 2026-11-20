-- Table: POST
-- Create order(foreign keys): USER, MOMENT, POST, FRIENDSHIP, COMMENT, REALMOJIS, REACTS_WITH, TAGGED_ON

CREATE TABLE `POST` (
  `PostID` int NOT NULL AUTO_INCREMENT,
  `FrontCam` varchar(255) NOT NULL,
  `BackCam` varchar(255) NOT NULL,
  `Caption` varchar(255) DEFAULT NULL,
  `RetakeCount` int DEFAULT '0',
  `HoursLate` decimal(5,2) DEFAULT '0.00',
  `PostedAt` datetime NOT NULL,
  `UserID` int NOT NULL,
  `MomentID` int NOT NULL,
  PRIMARY KEY (`PostID`),
  KEY `fk_post_user` (`UserID`),
  KEY `fk_post_moment` (`MomentID`),
  CONSTRAINT `fk_post_moment` FOREIGN KEY (`MomentID`) REFERENCES `MOMENT` (`MomentID`),
  CONSTRAINT `fk_post_user` FOREIGN KEY (`UserID`) REFERENCES `USER` (`UserID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
