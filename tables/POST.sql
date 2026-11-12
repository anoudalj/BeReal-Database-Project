CREATE TABLE `post` (
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
  CONSTRAINT `fk_post_moment` FOREIGN KEY (`MomentID`) REFERENCES `moment` (`MomentID`),
  CONSTRAINT `fk_post_user` FOREIGN KEY (`UserID`) REFERENCES `user` (`UserID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
