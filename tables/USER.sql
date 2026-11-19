-- Table: USER
-- Create order (foreign keys): USER, MOMENT, POST, FRIENDSHIP, COMMENT, REALMOJIS, REACTS_WITH, TAGGED_ON

CREATE TABLE `USER` (
  `UserID` int NOT NULL AUTO_INCREMENT,
  `Email` varchar(255) NOT NULL,
  `Username` varchar(50) NOT NULL,
  `Password` varchar(255) NOT NULL,
  `Bio` text,
  `DOB` date DEFAULT NULL,
  `Account_Type` varchar(30) DEFAULT NULL,
  `UserType` varchar(30) DEFAULT NULL,
  `JoinDate` date DEFAULT NULL,
  `ProfilePicture` varchar(255) DEFAULT NULL,
  `AccountStatus` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`UserID`),
  UNIQUE KEY `Email` (`Email`),
  UNIQUE KEY `Username` (`Username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
