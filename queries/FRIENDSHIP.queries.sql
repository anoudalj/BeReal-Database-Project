
-- FRIENDSHIP Operations

-- 1. INSERT: Send a friendship request

INSERT INTO FRIENDSHIP
(Date_Requested, Requester_ID, Receiver_ID, Status)
VALUES
(CURRENT_DATE, 101, 102, 'Pending');


-- 2. UPDATE: Accept a friendship request

UPDATE FRIENDSHIP
SET Status = 'Accepted',
    ApprovedDate = CURRENT_DATE
WHERE Friendship_id = 301
AND Status = 'Pending';


-- 3. JOIN: Retrieve pending friendship requests

SELECT F.Friendship_id,
       U.Username AS Requester,
       F.Receiver_ID,
       F.Date_Requested
FROM FRIENDSHIP F
JOIN `USER` U
    ON F.Requester_ID = U.UserID
WHERE F.Status = 'Pending';
