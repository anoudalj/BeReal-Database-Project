-- USER operations
-- The database already has two users, so I use a new username
-- to test the insert, deactivate, and delete operations.

-- 1- INSERT: register a new user
INSERT INTO `USER`
(Email, Username, Password, Bio, DOB, Account_Type, UserType, JoinDate, ProfilePicture, AccountStatus)
VALUES
('tempuser@example.com', 'tempuser03', 'pbkdf2:120000:nBeGq4rDBfOCbWOQmEvc9A==:pLw8OeKIDp+fsbIQ4GJL72bvrUnQtupwrDvsY6Rn3LU=', 'Temporary demo user', '2005-01-10', 'Public', 'Regular', CURDATE(), NULL, 'Active');

-- 2- RETRIEVE: view a user's details
SELECT UserID, Email, Username, Bio, DOB, Account_Type, UserType, JoinDate, ProfilePicture, AccountStatus
FROM `USER`
WHERE Username = 'testuser01';

-- 3- RETRIEVE: all users with public account
SELECT Username, Bio, ProfilePicture
FROM `USER`
WHERE Account_Type = 'Public';

-- 4- UPDATE: update user's profile information
UPDATE `USER`
SET Bio = 'Computer Science student'
WHERE Username = 'testuser01';

-- 5- UPDATE: change a user's profile picture
UPDATE `USER`
SET ProfilePicture = 'new_profile.jpg'
WHERE Username = 'testuser01';

-- 6- UPDATE: deactivat an account (safer than deleting a user who has posts)
UPDATE `USER`
SET AccountStatus = 'Inactive'
WHERE Username = 'tempuser03';

-- 7- DELETE: remove an account that has no posts or comments or friendships
DELETE FROM `USER`
WHERE Username = 'tempuser03';
