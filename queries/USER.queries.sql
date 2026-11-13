-- USER Operations

-- 1. INSERT: Register a new user

INSERT INTO `USER`
(Email, Username, Password, Bio, DOB, Account_Type, UserType, JoinDate, ProfilePicture, AccountStatus)
VALUES
('testuser@example.com', 'testuser01', 'DemoPassword123', 'Testing user registration', '2004-05-15', 'Public', 'Regular', CURDATE(), NULL, 'Active');



-- 2. RETRIEVE: View a user's details

SELECT UserID, Email, Username, Bio, DOB, Account_Type, UserType, JoinDate, ProfilePicture, AccountStatus
FROM `USER`
WHERE Username = 'testuser01';



-- 3. UPDATE: Update a user's profile information

UPDATE `USER`
SET Bio = 'Computer Science student',
    Account_Type = 'Private'
WHERE Username = 'testuser01';



-- 4. INSERT: Register another user

INSERT INTO `USER`
(Email, Username, Password, Bio, DOB, Account_Type, UserType, JoinDate, ProfilePicture, AccountStatus)
VALUES
('seconduser@example.com', 'seconduser02', 'DemoPassword456', 'Enjoying BeReal', '2003-08-20', 'Public', 'Regular', CURDATE(), NULL, 'Active');



-- 5. UPDATE: Change a user's account status

UPDATE `USER`
SET AccountStatus = 'Inactive'
WHERE Username = 'seconduser02';



-- 6. DELETE: Remove a user account

DELETE FROM `USER`
WHERE Username = 'seconduser02';