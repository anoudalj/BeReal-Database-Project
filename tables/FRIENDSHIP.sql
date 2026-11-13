CREATE TABLE `FRIENDSHIP` (
  `Friendship_id` int NOT NULL AUTO_INCREMENT,
  `Date_Requested` date NOT NULL,
  `ApprovedDate` date DEFAULT NULL,
  `Requester_ID` int NOT NULL,
  `Receiver_ID` int NOT NULL,
  `Status` varchar(10) NOT NULL DEFAULT 'Pending',
  PRIMARY KEY (`Friendship_id`),
  KEY `Requester_ID` (`Requester_ID`),
  KEY `Receiver_ID` (`Receiver_ID`),
  CONSTRAINT `friendship_ibfk_1` FOREIGN KEY (`Requester_ID`) REFERENCES `USER` (`UserID`),
  CONSTRAINT `friendship_ibfk_2` FOREIGN KEY (`Receiver_ID`) REFERENCES `USER` (`UserID`)
)  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
