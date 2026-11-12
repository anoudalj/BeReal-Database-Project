CREATE TABLE `moment` (
  `MomentID` int NOT NULL,
  `Time` time NOT NULL,
  `TimeZone` varchar(50) NOT NULL,
  `Date` date NOT NULL,
  PRIMARY KEY (`MomentID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
