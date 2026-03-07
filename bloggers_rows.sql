-- Updated to match Blogger entity structure
-- Blogger entity fields: id, userName, firstName, lastName, email, password, plainPassword, about
-- Note: Passwords are BCrypt hashed (cost factor 12)
-- Plain passwords are included in comments for reference

INSERT INTO "public"."blogger" (
    "id",
    "user_name",
    "first_name",
    "last_name",
    "email",
    "password",
    "plain_password",
    "about",
    "created_at",
    "created_by",
    "updated_at",
    "updated_by"
) VALUES
    -- Plain password: P@ssword#123
    ('2', 'johndoe', 'John', 'Doe', 'john.doe@example.com', '$2a$12$6E9v.BvCkM9DZrSHh8GHeeCN2KbgF4OpaGWUea1RIsbWWXI.nq6ey', 'P@ssword#123', 'A software developer specializing in backend systems.', '2024-08-22 16:59:26.445629', 'TRENDY-THREAD_MS', null, null),
    -- Plain password: Secur3P@ss!
    ('3', 'janesmith', 'Jane', 'Smith', 'jane.smith@example.com', '$2a$12$mfitSBHfQyle7ez2OVhMSOOtXBvNJOi0De6AXmpw/R2zjz/n1EbpC', 'Secur3P@ss!', 'A front-end developer with a passion for user interfaces.', '2024-08-22 16:59:45.585923', 'TRENDY-THREAD_MS', null, null),
    -- Plain password: P@ssw0rd!
    ('4', 'alicejohnson', 'Alice', 'Johnson', 'alice.johnson@example.com', '$2a$12$LBHWGuSaRBUI6EqwaKbYguQaaO6Xm44nhhWGy02yD2cwiyMwJmhoC', 'P@ssw0rd!', 'A DevOps engineer focused on CI/CD pipelines.', '2024-08-22 16:59:54.479553', 'TRENDY-THREAD_MS', null, null),
    -- Plain password: MyS3cretP@ss
    ('5', 'bobbrown', 'Bob', 'Brown', 'bob.brown@example.com', '$2a$12$lm32sQIOtRchuBRDSw.W5O6NNAkB7bs0C/0QE6xk4ljPiyyGmqMd.', 'MyS3cretP@ss', 'A cloud architect specializing in AWS and Azure.', '2024-08-22 17:00:05.414025', 'TRENDY-THREAD_MS', null, null),
    -- Plain password: Ch@rlie123
    ('6', 'charliedavis', 'Charlie', 'Davis', 'charlie.davis@example.com', '$2a$12$1z3GMwOIcZ8REDrtWiLNqukBQ1mhANUSD6aByG.vmCB00Hqh9sHEa', 'Ch@rlie123', 'A database administrator with expertise in SQL and NoSQL databases.', '2024-08-22 17:00:12.991674', 'TRENDY-THREAD_MS', null, null),
    -- Plain password: Ev@nS#2024
    ('7', 'dianaevans', 'Diana', 'Evans', 'diana.evans@example.com', '$2a$12$4dfY2CCBxc74PiJZ5j3ct.yiXxSfxqzqxSyPknDqiGJ06dI/vg.TW', 'Ev@nS#2024', 'A security analyst with experience in threat detection and response.', '2024-08-22 17:00:19.852909', 'TRENDY-THREAD_MS', null, null),
    -- Plain password: Eth@nSecure123
    ('8', 'ethanharris', 'Ethan', 'Harris', 'ethan.harris@example.com', '$2a$12$7IGy7.DzrpmLYxvO6QmgLOJoVEQQmg2C5oGqL8ugbYuwbslLYVp9i', 'Eth@nSecure123', 'A mobile app developer with a focus on Android applications.', '2024-08-22 17:00:28.575854', 'TRENDY-THREAD_MS', null, null),
    -- Plain password: F!0n@Gr33n
    ('9', 'fionagreen', 'Fiona', 'Green', 'fiona.green@example.com', '$2a$12$mjbtyKT4b2.Crxv1uAbrJu94yoHlZyW9qb8SHVXo/tsx6enJw.F4S', 'F!0n@Gr33n', 'A project manager specializing in agile methodologies.', '2024-08-22 17:00:34.702538', 'TRENDY-THREAD_MS', null, null),
    -- Plain password: G3orgeKing@123
    ('10', 'georgeking', 'George', 'King', 'george.king@example.com', '$2a$12$zSNZTsUN7fvmHstQWgJcS.TaVFnH4NLGTvKKf5w3pXve2mWN/eR5i', 'G3orgeKing@123', 'A full-stack developer with experience in MERN and MEAN stacks.', '2024-08-22 17:00:42.586382', 'TRENDY-THREAD_MS', null, null),
    -- Plain password: HannahL@w1s
    ('11', 'hannahlewis', 'Hannah', 'Lewis', 'hannah.lewis@example.com', '$2a$12$3VF5LGk/od1ZM7ddguf7vO36mzviGek2p8ql1b3U0LPYPHREHKmsS', 'HannahL@w1s', 'A quality assurance engineer focused on automated testing.', '2024-08-22 17:00:50.881954', 'TRENDY-THREAD_MS', null, null),
    -- Plain password: OliviaTech#789
    ('12', 'oliviabennet', 'Olivia', 'Bennett', 'olivia.bennet@techworld.com', '$2a$12$giV.OedNUCpEHEVl9p3XcuUGcqNnFSj2Y201f6czYbGltZLTkonbW', 'OliviaTech#789', 'An AI specialist focused on developing cutting-edge machine learning models.', '2024-08-22 17:01:16.276802', 'TRENDY-THREAD_MS', null, null);
