-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               26.10.0 - MySQL Community Server - GPL
-- Server OS:                    macos15
-- HeidiSQL Version:             12.22.1.841
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for Bereal
CREATE DATABASE IF NOT EXISTS `Bereal` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `Bereal`;

-- Dumping structure for table Bereal.COMMENT
CREATE TABLE IF NOT EXISTS `COMMENT` (
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

-- Dumping data for table Bereal.COMMENT: ~0 rows (approximately)
INSERT INTO `COMMENT` (`PostID`, `CommentNumber`, `Content`, `TimePosted`, `UserID`) VALUES
	(2, 1, 'love this', '2026-11-18 11:00:53', 3);

-- Dumping structure for table Bereal.FRIENDSHIP
CREATE TABLE IF NOT EXISTS `FRIENDSHIP` (
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
  UNIQUE KEY `uq_friendship_pair` (`Requester_ID`,`Receiver_ID`),
  CONSTRAINT `friendship_ibfk_2` FOREIGN KEY (`Receiver_ID`) REFERENCES `USER` (`UserID`),
  CONSTRAINT `chk_friendship_status` CHECK (`Status` IN ('Pending','Accepted')),
  CONSTRAINT `chk_friendship_self` CHECK (`Requester_ID` <> `Receiver_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table Bereal.FRIENDSHIP: ~1 rows (approximately)
INSERT INTO `FRIENDSHIP` (`Friendship_id`, `Date_Requested`, `ApprovedDate`, `Requester_ID`, `Receiver_ID`, `Status`) VALUES
	(1, '2026-11-15', NULL, 3, 2, 'Pending');

-- Dumping structure for table Bereal.MOMENT
CREATE TABLE IF NOT EXISTS `MOMENT` (
  `MomentID` int NOT NULL AUTO_INCREMENT,
  `Time` time NOT NULL,
  `TimeZone` varchar(50) NOT NULL,
  `Date` date NOT NULL,
  PRIMARY KEY (`MomentID`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table Bereal.MOMENT: ~1 rows (approximately)
INSERT INTO `MOMENT` (`MomentID`, `Time`, `TimeZone`, `Date`) VALUES
	(2, '22:00:00', 'Asia/Riyadh', '2026-11-13');

-- Dumping structure for table Bereal.POST
CREATE TABLE IF NOT EXISTS `POST` (
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table Bereal.POST: ~1 rows (approximately)
INSERT INTO `POST` (`PostID`, `FrontCam`, `BackCam`, `Caption`, `RetakeCount`, `HoursLate`, `PostedAt`, `UserID`, `MomentID`) VALUES
	(2, 'images/front2.jpg', 'images/back2.jpg', 'Second BeReal', 0, 0.00, '2026-11-12 21:35:00', 3, 2);

-- Dumping structure for table Bereal.REACTS_WITH
CREATE TABLE IF NOT EXISTS `REACTS_WITH` (
  `UserID` int NOT NULL,
  `PostID` int NOT NULL,
  `RealMojiID` int NOT NULL,
  PRIMARY KEY (`UserID`,`PostID`,`RealMojiID`),
  KEY `FK_REACTS_WITH_POST` (`PostID`),
  KEY `FK_REACTS_WITH_REALMOJIS` (`RealMojiID`),
  CONSTRAINT `FK_REACTS_WITH_POST` FOREIGN KEY (`PostID`) REFERENCES `POST` (`PostID`) ON DELETE CASCADE,
  CONSTRAINT `FK_REACTS_WITH_REALMOJIS` FOREIGN KEY (`RealMojiID`) REFERENCES `REALMOJIS` (`RealMojiID`),
  CONSTRAINT `FK_REACTS_WITH_USER` FOREIGN KEY (`UserID`) REFERENCES `USER` (`UserID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table Bereal.REACTS_WITH: ~0 rows (approximately)

-- Dumping structure for table Bereal.REALMOJIS
CREATE TABLE IF NOT EXISTS `REALMOJIS` (
  `RealMojiID` int NOT NULL AUTO_INCREMENT,
  `Emoji` varchar(50) NOT NULL,
  `Image` varchar(255) NOT NULL,
  `UserID` int NOT NULL,
  PRIMARY KEY (`RealMojiID`),
  KEY `UserID` (`UserID`),
  CONSTRAINT `realmojis_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `USER` (`UserID`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table Bereal.REALMOJIS: ~2 rows (approximately)
INSERT INTO `REALMOJIS` (`RealMojiID`, `Emoji`, `Image`, `UserID`) VALUES
	(18, 'Happy', 'happy.jpg', 2),
	(19, 'Hearts', 'hearts.jpg', 2);

-- Dumping structure for table Bereal.TAGGED_ON
CREATE TABLE IF NOT EXISTS `TAGGED_ON` (
  `UserID` int NOT NULL,
  `PostID` int NOT NULL,
  PRIMARY KEY (`UserID`,`PostID`),
  KEY `PostID` (`PostID`),
  CONSTRAINT `tagged_on_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `USER` (`UserID`),
  CONSTRAINT `tagged_on_ibfk_2` FOREIGN KEY (`PostID`) REFERENCES `POST` (`PostID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table Bereal.TAGGED_ON: ~0 rows (approximately)

-- Dumping structure for table Bereal.USER
CREATE TABLE IF NOT EXISTS `USER` (
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table Bereal.USER: ~3 rows (approximately)
INSERT INTO `USER` (`UserID`, `Email`, `Username`, `Password`, `Bio`, `DOB`, `Account_Type`, `UserType`, `JoinDate`, `ProfilePicture`, `AccountStatus`) VALUES
	(2, 'testuser@example.com', 'testuser01', 'pbkdf2:120000:9H8JscW348gLapV0WpyH5Q==:uAh/w+ir4aQqx9Bg6NZR1bIT8zeTTyMafPC+ZxkrwJ0=', 'Testing user registration', '2004-05-15', 'Public', 'Regular', '2026-11-15', NULL, 'Active'),
	(3, 'seconduser@example.com', 'seconduser02', 'pbkdf2:120000:5jNI42Izwvyil0Z40oftaw==:RiW7FPfQAounlzq/o+MXUf5+MQv390ZuR2b1pMVui7U=', 'Enjoying BeReal', '2003-08-20', 'Public', 'Regular', '2026-11-15', NULL, 'Active'),
	(4, 'admin@example.com', 'admin01', 'pbkdf2:120000:ZiNYd2zy375emE01HRXoEg==:gNaSIo69mQD93RUTZHDPHIjxw+DqWgHiGuuDsVO4qWE=', 'Admin account', '2000-01-01', 'Public', 'Admin', '2026-11-15', NULL, 'Active');

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
