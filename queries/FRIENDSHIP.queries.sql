-- FRIENDSHIP operations

-- 1. INSERT : send a friendship request (user 2 to user 3)
INSERT INTO FRIENDSHIP
(Date_Requested, Requester_ID, Receiver_ID, Status)
VALUES
(CURRENT_DATE, 2, 3, 'Pending');

-- 2 UPDATE: accept a friendship request
UPDATE FRIENDSHIP
SET Status = 'Accepted',
    ApprovedDate = CURRENT_DATE
WHERE Friendship_id = 1
  AND Status = 'Pending';

-- 3. JOIN: retrieve pending friendship requests with the requester username
SELECT F.Friendship_id,
       U.Username AS Requester,
       F.Receiver_ID,
       F.Date_Requested
FROM FRIENDSHIP F
JOIN `USER` U
    ON F.Requester_ID = U.UserID
WHERE F.Status = 'Pending';

