-- ========================================
-- TrendyThread Application - Development Data
-- Profile: dev (H2/PostgreSQL Compatible)
-- Date: March 7, 2026
-- Description: Comprehensive initialization data for all entities
-- ========================================

-- Execution Order: MUST insert in this order due to foreign key constraints:
-- 1. Blogger (no dependencies)
-- 2. Category (no dependencies)
-- 3. Post (depends on Blogger and Category)
-- 4. Comment (depends on Post and Blogger)

-- ========================================
-- 1. BLOGGER TABLE DATA (11 records)
-- ========================================
-- Blogger entity: userName, firstName, lastName, email, password (BCrypt), plainPassword, about
-- Note: Passwords are BCrypt hashed with cost factor 12

INSERT INTO blogger (
    id,
    user_name,
    first_name,
    last_name,
    email,
    password,
    plain_password,
    about,
    role,               -- ← ADD THIS
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
      ('2', 'johndoe', 'John', 'Doe', 'john.doe@example.com', '$2a$12$6E9v.BvCkM9DZrSHh8GHeeCN2KbgF4OpaGWUea1RIsbWWXI.nq6ey', 'P@ssword#123', 'A software developer specializing in backend systems.', 'ROLE_USER', '2024-08-22 16:59:26.445629', 'TRENDY-THREAD_MS', null, null),
      ('3', 'janesmith', 'Jane', 'Smith', 'jane.smith@example.com', '$2a$12$mfitSBHfQyle7ez2OVhMSOOtXBvNJOi0De6AXmpw/R2zjz/n1EbpC', 'Secur3P@ss!', 'A front-end developer with a passion for user interfaces.', 'ROLE_USER', '2024-08-22 16:59:45.585923', 'TRENDY-THREAD_MS', null, null),
      ('4', 'alicejohnson', 'Alice', 'Johnson', 'alice.johnson@example.com', '$2a$12$LBHWGuSaRBUI6EqwaKbYguQaaO6Xm44nhhWGy02yD2cwiyMwJmhoC', 'P@ssw0rd!', 'A DevOps engineer focused on CI/CD pipelines.', 'ROLE_USER', '2024-08-22 16:59:54.479553', 'TRENDY-THREAD_MS', null, null),
      ('5', 'bobbrown', 'Bob', 'Brown', 'bob.brown@example.com', '$2a$12$lm32sQIOtRchuBRDSw.W5O6NNAkB7bs0C/0QE6xk4ljPiyyGmqMd.', 'MyS3cretP@ss', 'A cloud architect specializing in AWS and Azure.', 'ROLE_USER', '2024-08-22 17:00:05.414025', 'TRENDY-THREAD_MS', null, null),
      ('6', 'charliedavis', 'Charlie', 'Davis', 'charlie.davis@example.com', '$2a$12$1z3GMwOIcZ8REDrtWiLNqukBQ1mhANUSD6aByG.vmCB00Hqh9sHEa', 'Ch@rlie123', 'A database administrator with expertise in SQL and NoSQL databases.', 'ROLE_USER', '2024-08-22 17:00:12.991674', 'TRENDY-THREAD_MS', null, null),
      ('7', 'dianaevans', 'Diana', 'Evans', 'diana.evans@example.com', '$2a$12$4dfY2CCBxc74PiJZ5j3ct.yiXxSfxqzqxSyPknDqiGJ06dI/vg.TW', 'Ev@nS#2024', 'A security analyst with experience in threat detection and response.', 'ROLE_USER', '2024-08-22 17:00:19.852909', 'TRENDY-THREAD_MS', null, null),
      ('8', 'ethanharris', 'Ethan', 'Harris', 'ethan.harris@example.com', '$2a$12$7IGy7.DzrpmLYxvO6QmgLOJoVEQQmg2C5oGqL8ugbYuwbslLYVp9i', 'Eth@nSecure123', 'A mobile app developer with a focus on Android applications.', 'ROLE_USER', '2024-08-22 17:00:28.575854', 'TRENDY-THREAD_MS', null, null),
      ('9', 'fionagreen', 'Fiona', 'Green', 'fiona.green@example.com', '$2a$12$mjbtyKT4b2.Crxv1uAbrJu94yoHlZyW9qb8SHVXo/tsx6enJw.F4S', 'F!0n@Gr33n', 'A project manager specializing in agile methodologies.', 'ROLE_USER', '2024-08-22 17:00:34.702538', 'TRENDY-THREAD_MS', null, null),
      ('10', 'georgeking', 'George', 'King', 'george.king@example.com', '$2a$12$zSNZTsUN7fvmHstQWgJcS.TaVFnH4NLGTvKKf5w3pXve2mWN/eR5i', 'G3orgeKing@123', 'A full-stack developer with experience in MERN and MEAN stacks.', 'ROLE_USER', '2024-08-22 17:00:42.586382', 'TRENDY-THREAD_MS', null, null),
      ('11', 'hannahlewis', 'Hannah', 'Lewis', 'hannah.lewis@example.com', '$2a$12$3VF5LGk/od1ZM7ddguf7vO36mzviGek2p8ql1b3U0LPYPHREHKmsS', 'HannahL@w1s', 'A quality assurance engineer focused on automated testing.', 'ROLE_USER', '2024-08-22 17:00:50.881954', 'TRENDY-THREAD_MS', null, null),
      ('12', 'oliviabennet', 'Olivia', 'Bennett', 'olivia.bennet@techworld.com', '$2a$12$giV.OedNUCpEHEVl9p3XcuUGcqNnFSj2Y201f6czYbGltZLTkonbW', 'OliviaTech#789', 'An AI specialist focused on developing cutting-edge machine learning models.', 'ROLE_USER', '2024-08-22 17:01:16.276802', 'TRENDY-THREAD_MS', null, null);
-- ========================================
-- 2. CATEGORY TABLE DATA (11 records)
-- ========================================

INSERT INTO category (
    id,
    created_at,
    created_by,
    updated_at,
    updated_by,
    description,
    name,
    featured,        -- ← ADD
    display_order,   -- ← ADD
    post_count,      -- ← ADD
    auto_suggested   -- ← ADD
) VALUES
      ('1',  '2024-08-22 17:01:48.334631', 'TRENDY-THREAD_MS', null, null, 'A category exploring the principles and technologies behind quantum computing and its potential applications.', 'Quantum Computing',        false, 1,  4, false),
      ('2',  '2024-08-22 17:01:52.424802', 'TRENDY-THREAD_MS', null, null, 'A category that encompasses various cloud computing services and technologies.', 'Cloud Computing',                  false, 2,  4, false),
      ('3',  '2024-08-22 17:01:58.825926', 'TRENDY-THREAD_MS', null, null, 'A category focused on AI technologies, including machine learning, neural networks, and deep learning.', 'Artificial Intelligence',  false, 3,  3, false),
      ('4',  '2024-08-22 17:02:04.170166', 'TRENDY-THREAD_MS', null, null, 'A category dedicated to the development of websites and web applications using various frameworks and technologies.', 'Web Development',    false, 4,  4, false),
      ('5',  '2024-08-22 17:02:08.50146',  'TRENDY-THREAD_MS', null, null, 'A category that covers topics related to securing information systems from threats and vulnerabilities.', 'Cybersecurity',            false, 5,  4, false),
      ('6',  '2024-08-22 17:02:13.678003', 'TRENDY-THREAD_MS', null, null, 'A category that includes topics on developing mobile applications for Android, iOS, and other platforms.', 'Mobile Development',       false, 6,  4, false),
      ('7',  '2024-08-22 17:02:18.538183', 'TRENDY-THREAD_MS', null, null, 'A category focused on data analysis, visualization, and the use of statistical methods to extract insights.', 'Data Science',            false, 7,  4, false),
      ('8',  '2024-08-22 17:02:22.73661',  'TRENDY-THREAD_MS', null, null, 'A category that covers the concepts and technologies behind decentralized digital ledgers and cryptocurrencies.', 'Blockchain',            false, 8,  4, false),
      ('9',  '2024-08-22 17:02:27.44715',  'TRENDY-THREAD_MS', null, null, 'A category dedicated to the network of physical devices that are connected and communicate over the internet.', 'Internet of Things',    false, 9,  4, false),
      ('10', '2024-08-22 17:02:31.952916', 'TRENDY-THREAD_MS', null, null, 'A category that combines software development and IT operations, focusing on continuous integration and delivery.', 'DevOps',               false, 10, 2, false),
      ('11', '2024-08-22 17:02:36.662944', 'TRENDY-THREAD_MS', null, null, 'A category that includes topics related to building and deploying models that enable computers to learn from data.', 'Machine Learning',    false, 11, 4, false);

-- ========================================
-- 3. POST TABLE DATA (42 records)
-- ========================================

INSERT INTO post (
    id,
    created_at,
    created_by,
    updated_at,
    updated_by,
    content,
    description,
    title,
    category_id,
    blogger_id
)
VALUES
    ('1',  '2024-08-22 17:05:22.56145',    'TRENDY-THREAD_MS', '2024-08-22 17:05:22.968952', 'TRENDY-THREAD_MS', 'Quantum computing leverages the principles of quantum mechanics to process information in fundamentally new ways, offering potential breakthroughs in solving complex problems.',               'An introduction to the principles and applications of quantum computing.',                          'Exploring Quantum Computing Fundamentals',      '1',  '2'),
    ('2',  '2024-08-22 17:05:49.384631',   'TRENDY-THREAD_MS', '2024-08-22 17:05:49.793234', 'TRENDY-THREAD_MS', 'Cloud computing continues to revolutionize the IT industry with its scalable resources and flexibility. The future promises even more advancements in cloud technologies and services.',       'Insights into the evolving landscape of cloud computing and its future prospects.',                  'The Future of Cloud Computing',                 '2',  '3'),
    ('3',  '2024-08-22 17:06:28.137981',   'TRENDY-THREAD_MS', null,                          null,               'Artificial Intelligence is making significant strides in various fields, including healthcare, finance, and autonomous systems. New algorithms and models are pushing the boundaries of what AI can achieve.', 'A look into the latest advancements and applications of AI technology.',                    'Advancements in Artificial Intelligence',       '3',  '4'),
    ('4',  '2024-08-22 17:06:53.386701',   'TRENDY-THREAD_MS', null,                          null,               'Modern web development involves using frameworks and libraries that enhance productivity and maintainability. This post explores popular technologies and techniques for building robust web applications.', 'An overview of current best practices and technologies for web development.',              'Building Modern Web Applications',              '4',  '5'),
    ('5',  '2024-08-22 17:07:18.063248',   'TRENDY-THREAD_MS', '2024-08-22 17:07:18.475523', 'TRENDY-THREAD_MS', 'Cybersecurity threats are evolving rapidly. This post covers various types of threats, including malware, phishing, and ransomware, and provides strategies for mitigating these risks.',     'An in-depth look at common cybersecurity threats and how to protect against them.',                 'Understanding Cybersecurity Threats',           '5',  '6'),
    ('6',  '2024-08-22 17:07:42.82577',    'TRENDY-THREAD_MS', '2024-08-22 17:07:43.562426', 'TRENDY-THREAD_MS', 'Mobile app development requires understanding platform-specific guidelines and utilizing tools for efficient development. This post discusses techniques for creating high-quality mobile apps for iOS and Android.', 'Guidelines and best practices for developing mobile applications for various platforms.', 'Developing Apps for Mobile Platforms',          '6',  '7'),
    ('7',  '2024-08-22 17:07:56.26001',    'TRENDY-THREAD_MS', '2024-08-22 17:07:56.970342', 'TRENDY-THREAD_MS', 'Data science involves a range of techniques for analyzing and interpreting complex data. This post covers essential methods and tools used by data scientists to derive insights and make data-driven decisions.', 'An introduction to key techniques and tools used in data science.',                       'Exploring Data Science Techniques',             '7',  '8'),
    ('8',  '2024-08-22 17:08:11.313793',   'TRENDY-THREAD_MS', null,                          null,               'Blockchain technology underpins many modern cryptocurrencies and decentralized applications. This post explains the fundamental principles of blockchain and its potential uses in various industries.', 'A comprehensive guide to understanding blockchain technology and its applications.',       'Blockchain Technology Explained',               '8',  '9'),
    ('9',  '2024-08-22 17:08:26.263145',   'TRENDY-THREAD_MS', '2024-08-22 17:08:27.082158', 'TRENDY-THREAD_MS', 'The Internet of Things (IoT) connects physical devices to the internet, enabling them to share data and interact. This post introduces IoT concepts and discusses various applications of connected devices.', 'An overview of Internet of Things (IoT) and how connected devices communicate.',           'Connecting Devices: IoT Basics',                '9',  '10'),
    ('10', '2024-08-22 17:08:43.364918',   'TRENDY-THREAD_MS', '2024-08-22 17:08:44.183875', 'TRENDY-THREAD_MS', 'DevOps integrates development and operations to improve efficiency and productivity. This post covers key practices and tools that facilitate continuous integration and delivery in DevOps.',  'A guide to understanding DevOps principles and practices for continuous integration and delivery.',  'Introduction to DevOps Practices',              '10', '11'),
    ('11', '2024-08-22 17:09:27.243654',   'TRENDY-THREAD_MS', '2024-08-22 17:09:27.846446', 'TRENDY-THREAD_MS', 'Machine learning algorithms enable computers to learn from data and make predictions. This post provides a detailed explanation of popular algorithms and their use cases in different domains.',  'An in-depth look at various machine learning algorithms and their applications.',                  'Machine Learning Algorithms Explained',         '11', '12'),
    ('12', '2024-08-22 17:10:03.13523',    'TRENDY-THREAD_MS', null,                          null,               'Quantum computing offers different advantages over classical computing by leveraging quantum bits and superposition. This post compares these two paradigms and explores their respective strengths and weaknesses.', 'A comparison between quantum computing and classical computing paradigms.',              'Quantum Computing vs. Classical Computing',     '1',  '2'),
    ('13', '2024-08-22 17:10:19.929469',   'TRENDY-THREAD_MS', '2024-08-22 17:10:20.74885',  'TRENDY-THREAD_MS', 'Securing cloud computing environments involves implementing best practices such as encryption, access controls, and regular security audits. This post offers practical advice for safeguarding cloud resources.', 'Tips and best practices for securing cloud computing environments.',                     'Cloud Computing Security Best Practices',       '2',  '3'),
    ('14', '2024-08-22 17:10:36.285417',   'TRENDY-THREAD_MS', null,                          null,               'AI technologies are making significant impacts in healthcare by improving diagnostics, personalizing treatments, and enhancing patient care. This post explores current trends and future directions in AI-driven healthcare.', 'An exploration of how artificial intelligence is transforming the healthcare industry.', 'AI in Healthcare: Current Trends',              '3',  '4'),
    ('15', '2024-08-22 17:10:58.944485',   'TRENDY-THREAD_MS', null,                          null,               'Web development tools and frameworks are constantly evolving. This post reviews some of the most popular and effective tools used by developers today, including front-end and back-end technologies.', 'A review of popular tools and frameworks for web development.',                           'Modern Web Development Tools',                  '4',  '5'),
    ('16', '2024-08-22 17:11:10.003639',   'TRENDY-THREAD_MS', '2024-08-22 17:11:11.335423', 'TRENDY-THREAD_MS', 'Cybersecurity has evolved significantly as new threats have emerged. This post provides an overview of the historical evolution of cybersecurity and discusses current challenges and solutions.',  'An overview of how cybersecurity has evolved over the years and current challenges.',               'The Evolution of Cybersecurity',                '5',  '6'),
    ('17', '2024-08-22 17:11:27.718028',   'TRENDY-THREAD_MS', '2024-08-22 17:11:28.436449', 'TRENDY-THREAD_MS', 'Developing high-quality mobile applications requires adherence to best practices. This post covers essential practices for mobile app development, including design principles, performance optimization, and blogger experience.', 'Best practices and tips for developing mobile applications effectively.',               'Mobile Development Best Practices',             '6',  '7'),
    ('18', '2024-08-22 17:11:38.982348',   'TRENDY-THREAD_MS', '2024-08-22 17:11:39.80236',  'TRENDY-THREAD_MS', 'Data science provides powerful tools for making data-driven business decisions. This post explores how data analysis and visualization can be applied to improve business strategies and outcomes.', 'How data science can be used to make better business decisions.',                        'Data Science for Business Decisions',           '7',  '8'),
    ('19', '2024-08-22 17:11:56.39103',    'TRENDY-THREAD_MS', null,                          null,               'Blockchain technology is the foundation of cryptocurrencies and decentralized applications. This post introduces the basic concepts of blockchain and discusses its benefits and applications.',    'A beginner''s guide to understanding blockchain technology and its benefits.',                     'Introduction to Blockchain Technology',         '8',  '9'),
    ('20', '2024-08-22 17:12:42.166014',   'TRENDY-THREAD_MS', null,                          null,               'The Internet of Things (IoT) is evolving rapidly with new technologies and applications. This post discusses current trends in IoT and explores potential future developments in the field.',      'Current trends in Internet of Things (IoT) technology and its future prospects.',                  'IoT Trends and Future Directions',              '9',  '10'),
    ('21', '2024-08-22 17:13:05.307924',   'TRENDY-THREAD_MS', null,                          null,               'DevOps practices aim to improve collaboration between development and operations teams. This post provides an introduction to DevOps concepts, tools, and techniques for enhancing software delivery.', 'An introduction to DevOps practices and tools for improving software delivery.',          'Getting Started with DevOps',                   '10', '11'),
    ('22', '2024-08-22 17:13:37.342016',   'TRENDY-THREAD_MS', '2024-08-22 17:13:37.946087', 'TRENDY-THREAD_MS', 'Machine learning models are increasingly used for predictive analytics to forecast trends and make data-driven predictions. This post explores different machine learning techniques used in predictive analytics and their applications.', 'How machine learning techniques can be used for predictive analytics.',                'Machine Learning for Predictive Analytics',     '11', '12'),
    ('23', '2024-08-22 17:15:21.502448',   'TRENDY-THREAD_MS', '2024-08-22 17:15:22.211152', 'TRENDY-THREAD_MS', 'Quantum computing holds promise for transforming financial services by solving complex problems related to risk analysis, portfolio optimization, and fraud detection. This post explores potential applications of quantum computing in finance.', 'Potential applications of quantum computing in the financial sector.',                 'Quantum Computing Applications in Finance',     '1',  '2'),
    ('26', '2024-08-22 17:59:22.650077',   'TRENDY-THREAD_MS', null,                          null,               'Cloud computing offers numerous benefits for small businesses, including cost savings, scalability, and flexibility. This post discusses how small businesses can effectively use cloud services to enhance their operations and drive growth.', 'How small businesses can leverage cloud computing for growth and efficiency.',          'Cloud Computing for Small Businesses',          '2',  '3'),
    ('27', '2024-08-22 17:59:51.296484',   'TRENDY-THREAD_MS', '2024-08-22 18:00:04.28615',  'TRENDY-THREAD_MS', 'Web development frameworks evolve rapidly with new innovations. This post highlights some of the latest and most innovative frameworks that are changing the landscape of web development.',    'A look at new and innovative frameworks in web development.',                                       'Innovative Web Development Frameworks',         '4',  '5'),
    ('28', '2024-08-22 18:00:24.782755',   'TRENDY-THREAD_MS', null,                          null,               'Defending against cybersecurity threats requires a multi-layered approach. This post discusses best practices for enhancing cybersecurity defenses, including threat detection, response strategies, and risk management.', 'Effective strategies for defending against cybersecurity threats.',                     'Best Practices for Cybersecurity Defense',      '5',  '6'),
    ('29', '2024-08-22 18:00:41.179938',   'TRENDY-THREAD_MS', '2024-08-22 18:00:41.488643', 'TRENDY-THREAD_MS', 'Mobile development is rapidly evolving with new technologies and platforms. This post examines the trends and innovations in mobile development and their impact on the tech industry.',        'How mobile development is evolving and its impact on technology.',                                  'The Growing Role of Mobile Development',         '6',  '7'),
    ('30', '2024-08-22 18:01:04.117196',   'TRENDY-THREAD_MS', null,                          null,               'Data science can provide valuable insights for business decision-making. This post explores how data analysis, machine learning, and visualization can be used to drive business strategy and performance.', 'Using data science techniques to gain valuable business insights.',                     'Leveraging Data Science for Business Insights', '7',  '8'),
    ('31', '2024-08-22 18:01:24.187729',   'TRENDY-THREAD_MS', null,                          null,               'Blockchain technology is not just for cryptocurrencies. This post explores various applications of blockchain, including supply chain management, voting systems, and more.',                     'Understanding the various applications of blockchain technology.',                                  'Blockchain and its Applications',               '8',  '9'),
    ('32', '2024-08-22 18:01:44.029889',   'TRENDY-THREAD_MS', null,                          null,               'The Internet of Things (IoT) connects devices and enables them to communicate and interact. This post provides an introduction to IoT concepts and explores various applications and benefits of connected devices.', 'Basic concepts and applications of the Internet of Things (IoT).',                    'Introduction to Internet of Things (IoT)',       '9',  '10'),
    ('33', '2024-08-22 18:02:07.709098',   'TRENDY-THREAD_MS', null,                          null,               'Machine learning is a subset of artificial intelligence that focuses on building systems that can learn from data. This post introduces the fundamentals of machine learning and explores its various applications.', 'An introduction to the basics of machine learning and its applications.',               'Getting Started with Machine Learning',          '11', '11'),
    ('35', '2024-08-22 18:03:46.126639',   'TRENDY-THREAD_MS', null,                          null,               'Quantum computing has the potential to revolutionize various industries by solving problems that are currently intractable.',                                                                       'Exploring the potential implications of quantum computing on various industries.',                  'Quantum Computing and its Implications',        '1',  '2'),
    ('36', '2024-08-22 18:04:12.531957',   'TRENDY-THREAD_MS', null,                          null,               'Cloud computing offers several benefits for businesses, including cost reduction, scalability, and flexibility. This post explains how businesses can leverage cloud computing to enhance their operations and drive growth.', 'Understanding the key benefits of cloud computing for businesses.',                     'Cloud Computing and Its Benefits',              '2',  '3'),
    ('37', '2024-08-22 18:04:33.010662',   'TRENDY-THREAD_MS', null,                          null,               'AI technologies are driving numerous innovations in healthcare, from improving diagnostics to personalizing treatments. This post explores some of the most exciting AI-powered innovations in the healthcare sector.', 'Innovations in healthcare driven by artificial intelligence.',                           'AI-Powered Healthcare Innovations',             '3',  '4'),
    ('38', '2024-08-22 18:04:51.953709',   'TRENDY-THREAD_MS', null,                          null,               'Web development technologies are constantly evolving. This post highlights recent advancements and trends that are shaping the future of web development, including new frameworks and tools.',    'Recent advancements and trends in web development technologies.',                                   'Advancements in Web Development Technologies',  '4',  '5'),
    ('39', '2024-08-22 18:05:09.36705',    'TRENDY-THREAD_MS', null,                          null,               'Improving cybersecurity practices is essential for protecting against modern threats. This post covers best practices for enhancing cybersecurity, including risk assessment, threat detection, and response strategies.', 'Effective strategies for improving cybersecurity practices.',                           'Best Practices in Cybersecurity',               '5',  '6'),
    ('40', '2024-08-22 18:05:30.866018',   'TRENDY-THREAD_MS', null,                          null,               'Mobile application development is rapidly evolving with new trends and technologies. This post explores current trends in mobile app development, including emerging technologies and best practices.', 'Current trends and innovations in mobile application development.',                     'Trends in Mobile Application Development',      '6',  '7'),
    ('41', '2024-08-22 18:05:49.501751',   'TRENDY-THREAD_MS', null,                          null,               'Data science offers various techniques that can drive business success, including predictive analytics, machine learning, and data visualization. This post explores these techniques and how they can be applied to achieve business goals.', 'Techniques in data science that can drive business success.',                          'Data Science Techniques for Business Success',  '7',  '8'),
    ('42', '2024-08-22 18:06:01.994276',   'TRENDY-THREAD_MS', null,                          null,               'Blockchain technology is transforming financial services by offering new ways to secure transactions, manage assets, and streamline operations. This post discusses innovative uses of blockchain in the financial sector.', 'Innovative uses of blockchain technology in financial services.',                       'Blockchain Innovations in Financial Services',  '8',  '9'),
    ('43', '2024-08-22 18:06:23.702494',   'TRENDY-THREAD_MS', null,                          null,               'The Internet of Things (IoT) is playing a crucial role in the development of smart cities by improving infrastructure, services, and quality of life. This post explores various IoT applications in smart cities and their benefits.', 'How IoT technology is being used in the development of smart cities.',                 'IoT Applications in Smart Cities',              '9',  '10'),
    ('44', '2024-08-22 18:06:43.46689',    'TRENDY-THREAD_MS', null,                          null,               'Machine learning is increasingly being used in healthcare to improve diagnostics, personalize treatments, and streamline operations. This post explores various applications of machine learning in the healthcare sector.', 'Exploring the use of machine learning in the healthcare industry.',                     'Machine Learning Applications in Healthcare',   '11', '11');

-- ========================================
-- 4. COMMENT TABLE DATA (50 sample records)
-- ========================================
-- Note: Including first 50 comments for brevity. Full dataset has 124 comments.
-- Comment entity: content, post_id, blogger_id

INSERT INTO comment (
    id,
    created_at,
    created_by,
    updated_at,
    updated_by,
    content,
    post_id,
    blogger_id
)
VALUES
    ('1',   '2024-08-22 18:10:36.959377', 'TRENDY-THREAD_MS', null, null, 'Incredible insights! This post really broadened my understanding of the topic.',                            '1', '2'),
    ('2',   '2024-08-22 18:10:51.782921', 'TRENDY-THREAD_MS', null, null, 'A very engaging and informative post. Thanks for sharing your expertise!',                                  '1', '3'),
    ('3',   '2024-08-22 18:10:59.052444', 'TRENDY-THREAD_MS', null, null, 'I appreciate the clarity and detail in this article. Well done!',                                           '1', '4'),
    ('4',   '2024-08-22 18:11:10.623259', 'TRENDY-THREAD_MS', null, null, 'This post provided some valuable information. I found it very useful!',                                     '2', '5'),
    ('5',   '2024-08-22 18:11:15.743879', 'TRENDY-THREAD_MS', null, null, 'Excellent post! The explanations were thorough and easy to understand.',                                     '2', '6'),
    ('6',   '2024-08-22 18:11:23.116525', 'TRENDY-THREAD_MS', null, null, 'Great job on this article. It''s both informative and engaging.',                                            '2', '7'),
    ('7',   '2024-08-22 18:11:33.560868', 'TRENDY-THREAD_MS', null, null, 'I really enjoyed reading this post. It''s insightful and well-written.',                                     '3', '8'),
    ('8',   '2024-08-22 18:11:38.390689', 'TRENDY-THREAD_MS', null, null, 'This post was very enlightening. Thank you for the valuable information!',                                   '3', '9'),
    ('9',   '2024-08-22 18:11:43.25542',  'TRENDY-THREAD_MS', null, null, 'Thank you for this well-written post. It provided great insights into the topic.',                          '3', '10'),
    ('10',  '2024-08-22 18:14:27.333502', 'TRENDY-THREAD_MS', null, null, 'An outstanding post with clear explanations. I learned a lot from it.',                                      '4', '11'),
    ('11',  '2024-08-22 18:15:39.933751', 'TRENDY-THREAD_MS', null, null, 'This article is fantastic! I appreciate the depth of knowledge shared.',                                    '4', '12'),
    ('12',  '2024-08-22 18:15:44.84765',  'TRENDY-THREAD_MS', null, null, 'Great read! The post was informative and well-presented.',                                                  '4', '2'),
    ('13',  '2024-08-22 18:15:53.244365', 'TRENDY-THREAD_MS', null, null, 'Really engaging post. It covered the topic in a comprehensive way.',                                        '5', '3'),
    ('14',  '2024-08-22 18:16:00.618058', 'TRENDY-THREAD_MS', null, null, 'A very interesting article. I enjoyed reading it and found it very useful.',                                 '5', '4'),
    ('15',  '2024-08-22 18:16:05.839166', 'TRENDY-THREAD_MS', null, null, 'This post is both insightful and thought-provoking. Well done!',                                            '5', '5'),
    ('16',  '2024-08-22 18:16:15.264549', 'TRENDY-THREAD_MS', null, null, 'Thank you for this informative article. It was both helpful and engaging.',                                  '6', '6'),
    ('17',  '2024-08-22 18:16:19.462763', 'TRENDY-THREAD_MS', null, null, 'An excellent post with a lot of valuable information. I really appreciate it.',                              '6', '7'),
    ('18',  '2024-08-22 18:16:24.06747',  'TRENDY-THREAD_MS', null, null, 'This post was very enlightening and well-written. Thank you for sharing!',                                  '6', '8'),
    ('19',  '2024-08-22 18:16:34.257872', 'TRENDY-THREAD_MS', null, null, 'Great article! The content was both informative and interesting.',                                           '7', '9'),
    ('20',  '2024-08-22 18:16:39.324236', 'TRENDY-THREAD_MS', null, null, 'Thanks for the comprehensive post. It covered everything I needed to know.',                                 '7', '10'),
    ('21',  '2024-08-22 18:16:43.215553', 'TRENDY-THREAD_MS', null, null, 'This post was very insightful. I learned a lot from the detailed explanation.',                              '7', '11'),
    ('22',  '2024-08-22 18:16:47.721281', 'TRENDY-THREAD_MS', null, null, 'I found this article to be extremely helpful. Thanks for the great content!',                               '7', '12'),
    ('23',  '2024-08-22 18:17:04.769308', 'TRENDY-THREAD_MS', null, null, 'This post provides a fantastic overview of quantum computing. Very enlightening!',                          '8', '2'),
    ('24',  '2024-08-22 18:17:09.020544', 'TRENDY-THREAD_MS', null, null, 'Great insights on quantum technology. I appreciate the detailed explanation.',                              '8', '3'),
    ('25',  '2024-08-22 18:17:13.640826', 'TRENDY-THREAD_MS', null, null, 'Thanks for the clear breakdown of quantum computing concepts. Very useful!',                                '8', '4'),
    ('26',  '2024-08-22 18:17:23.765404', 'TRENDY-THREAD_MS', null, null, 'Informative post! I learned a lot about quantum computing from this article.',                              '9', '5'),
    ('27',  '2024-08-22 18:17:27.144529', 'TRENDY-THREAD_MS', null, null, 'The explanations provided here are excellent. This post makes complex topics more understandable.',         '9', '6'),
    ('28',  '2024-08-22 18:17:30.887795', 'TRENDY-THREAD_MS', null, null, 'I appreciate the detailed approach to quantum computing. It''s a great resource for beginners.',             '9', '7'),
    ('29',  '2024-08-22 18:17:40.76331',  'TRENDY-THREAD_MS', null, null, 'This article is a great introduction to quantum computing. Thank you for sharing!',                        '10', '8'),
    ('30',  '2024-08-22 18:17:48.341489', 'TRENDY-THREAD_MS', null, null, 'Well-written and informative post. It sheds light on the future of computing technology.',                  '10', '9'),
    ('31',  '2024-08-22 18:17:52.540808', 'TRENDY-THREAD_MS', null, null, 'This post has given me a solid understanding of quantum computing fundamentals. Thanks!',                   '10', '10'),
    ('32',  '2024-08-22 18:18:03.393953', 'TRENDY-THREAD_MS', null, null, 'The explanations are very clear and the topic is well-covered. Excellent post!',                            '11', '11'),
    ('33',  '2024-08-22 18:18:07.869379', 'TRENDY-THREAD_MS', null, null, 'This article is a great resource for anyone interested in learning about quantum computing.',               '11', '12'),
    ('34',  '2024-08-22 18:18:12.303563', 'TRENDY-THREAD_MS', null, null, 'Thanks for the comprehensive guide on quantum computing. Very useful and insightful.',                      '11', '2'),
    ('35',  '2024-08-22 18:18:29.09944',  'TRENDY-THREAD_MS', null, null, 'Great post! It simplifies complex quantum computing concepts effectively.',                                  '12', '3'),
    ('36',  '2024-08-22 18:18:34.011265', 'TRENDY-THREAD_MS', null, null, 'The information provided here is very helpful for understanding quantum computing.',                        '12', '4'),
    ('37',  '2024-08-22 18:18:39.336518', 'TRENDY-THREAD_MS', null, null, 'This post provides a clear explanation of quantum computing principles. Thank you!',                        '12', '5'),
    ('38',  '2024-08-22 18:22:45.606955', 'TRENDY-THREAD_MS', null, null, 'Great read! I found the insights very enlightening and the writing style engaging.',                        '13', '6'),
    ('39',  '2024-08-22 18:22:53.18368',  'TRENDY-THREAD_MS', null, null, 'This was a really informative article. I appreciate the effort put into it.',                              '13', '7'),
    ('40',  '2024-08-22 18:22:56.732636', 'TRENDY-THREAD_MS', null, null, 'Interesting perspective! I enjoyed the clarity and depth of the discussion.',                              '13', '8'),
    ('41',  '2024-08-22 18:23:07.213967', 'TRENDY-THREAD_MS', null, null, 'Thanks for sharing this! I found it very useful and well-written.',                                        '14', '9'),
    ('42',  '2024-08-22 18:23:10.694592', 'TRENDY-THREAD_MS', null, null, 'Nice job on this post! It was very helpful and easy to understand.',                                        '14', '10'),
    ('43',  '2024-08-22 18:23:15.481175', 'TRENDY-THREAD_MS', null, null, 'Well done! The article was clear and provided great information.',                                          '14', '11'),
    ('44',  '2024-08-22 18:23:23.8527',   'TRENDY-THREAD_MS', null, null, 'I enjoyed reading this. The content was well-organized and insightful.',                                   '15', '12'),
    ('45',  '2024-08-22 18:23:28.512781', 'TRENDY-THREAD_MS', null, null, 'Excellent article! I appreciate the thorough explanation and engaging content.',                            '15', '2'),
    ('46',  '2024-08-22 18:23:32.09567',  'TRENDY-THREAD_MS', null, null, 'This was a great read! I found the information very valuable and well-presented.',                         '15', '3'),
    ('47',  '2024-08-22 18:23:47.14943',  'TRENDY-THREAD_MS', null, null, 'Fantastic post! The content was very clear and informative.',                                              '16', '4'),
    ('48',  '2024-08-22 18:23:50.823764', 'TRENDY-THREAD_MS', null, null, 'Great job on this article! It was both interesting and easy to follow.',                                   '16', '5'),
    ('49',  '2024-08-22 18:23:55.54529',  'TRENDY-THREAD_MS', null, null, 'Very informative! I found the content well-researched and engaging.',                                      '16', '6'),
    ('50',  '2024-08-22 18:24:08.400211', 'TRENDY-THREAD_MS', null, null, 'This article was a pleasure to read. The insights were well-articulated and helpful.',                     '17', '7'),
    ('51',  '2024-08-22 18:24:14.66531',  'TRENDY-THREAD_MS', null, null, 'Nice article! I appreciated the clear and concise information provided.',                                  '17', '8'),
    ('52',  '2024-08-22 18:24:18.584976', 'TRENDY-THREAD_MS', null, null, 'Well-written post! It provided useful information in an easy-to-understand manner.',                       '17', '9'),
    ('53',  '2024-08-22 18:24:30.156281', 'TRENDY-THREAD_MS', null, null, 'I enjoyed this post a lot. The writing was clear and the information was valuable.',                       '18', '10'),
    ('54',  '2024-08-22 18:24:34.260007', 'TRENDY-THREAD_MS', null, null, 'This was a very engaging article. I found the content both informative and enjoyable.',                    '18', '11'),
    ('55',  '2024-08-22 18:24:38.04058',  'TRENDY-THREAD_MS', null, null, 'Great post! The content was comprehensive and well-explained.',                                            '18', '12'),
    ('56',  '2024-08-22 18:24:46.437347', 'TRENDY-THREAD_MS', null, null, 'Thank you for this article! I found the insights very helpful and the writing style great.',               '19', '2'),
    ('57',  '2024-08-22 18:24:50.744166', 'TRENDY-THREAD_MS', null, null, 'Fantastic read! The article was very well-structured and informative.',                                    '19', '3'),
    ('58',  '2024-08-22 18:24:55.448738', 'TRENDY-THREAD_MS', null, null, 'I appreciated the depth of this article. It was both informative and enjoyable.',                          '19', '4'),
    ('59',  '2024-08-22 18:25:01.643946', 'TRENDY-THREAD_MS', null, null, 'This was a great post! The content was well-presented and engaging.',                                      '19', '5'),
    ('60',  '2024-08-22 18:25:15.744471', 'TRENDY-THREAD_MS', null, null, 'I found this post to be very insightful. The explanation was clear and concise.',                          '20', '6'),
    ('61',  '2024-08-22 18:25:22.776138', 'TRENDY-THREAD_MS', null, null, 'Great post! The information was presented in a very readable format.',                                     '20', '7'),
    ('62',  '2024-08-22 18:25:26.681699', 'TRENDY-THREAD_MS', null, null, 'This article was very helpful. I enjoyed the straightforward approach and clarity.',                       '20', '8'),
    ('63',  '2024-08-22 18:25:35.589241', 'TRENDY-THREAD_MS', null, null, 'Well done on this post! It was very engaging and easy to understand.',                                     '21', '9'),
    ('64',  '2024-08-22 18:25:39.992125', 'TRENDY-THREAD_MS', null, null, 'I appreciated the detailed explanation in this post. It was very informative.',                            '21', '10'),
    ('65',  '2024-08-22 18:25:44.293546', 'TRENDY-THREAD_MS', null, null, 'Nice job on the post! The content was clear and useful.',                                                  '21', '11'),
    ('66',  '2024-08-22 18:25:54.533107', 'TRENDY-THREAD_MS', null, null, 'Excellent article! The writing was clear and the content was very helpful.',                               '22', '12'),
    ('67',  '2024-08-22 18:25:58.526272', 'TRENDY-THREAD_MS', null, null, 'This post was very informative. I liked the way the information was organized.',                           '22', '2'),
    ('68',  '2024-08-22 18:26:02.111099', 'TRENDY-THREAD_MS', null, null, 'Great read! The article was engaging and provided valuable insights.',                                     '22', '3'),
    ('69',  '2024-08-22 18:26:17.060956', 'TRENDY-THREAD_MS', null, null, 'Great read! The article was engaging and provided valuable insights.',                                     '23', '4'),
    ('70',  '2024-08-22 18:26:22.692887', 'TRENDY-THREAD_MS', null, null, 'I enjoyed this post. It was well-written and easy to follow.',                                             '23', '5'),
    ('71',  '2024-08-22 18:26:26.174806', 'TRENDY-THREAD_MS', null, null, 'Fantastic article! The content was both comprehensive and accessible.',                                    '23', '6'),
    ('72',  '2024-08-22 18:26:34.518526', 'TRENDY-THREAD_MS', null, null, 'This was a very interesting post. I liked the depth of information provided.',                            '26', '7'),
    ('73',  '2024-08-22 18:26:38.618107', 'TRENDY-THREAD_MS', null, null, 'Well-written post! The clarity and depth were much appreciated.',                                         '26', '8'),
    ('74',  '2024-08-22 18:26:42.456228', 'TRENDY-THREAD_MS', null, null, 'Great content! The article was engaging and very informative.',                                            '26', '9'),
    ('75',  '2024-08-22 18:26:51.656069', 'TRENDY-THREAD_MS', null, null, 'I found this post to be very useful. The information was presented clearly.',                              '27', '10'),
    ('76',  '2024-08-22 18:26:55.563343', 'TRENDY-THREAD_MS', null, null, 'Excellent job on this article! The content was well-organized and insightful.',                            '27', '11'),
    ('77',  '2024-08-22 18:26:59.45441',  'TRENDY-THREAD_MS', null, null, 'I appreciated the straightforward presentation of the material. Well done!',                              '27', '12'),
    ('78',  '2024-08-22 18:27:08.568747', 'TRENDY-THREAD_MS', null, null, 'This post was very engaging. The writing was clear and informative.',                                      '28', '2'),
    ('79',  '2024-08-22 18:27:12.561399', 'TRENDY-THREAD_MS', null, null, 'Great read! The article provided useful information in an accessible way.',                                '28', '3'),
    ('80',  '2024-08-22 18:27:16.254041', 'TRENDY-THREAD_MS', null, null, 'Very helpful article. The content was presented in a clear and concise manner.',                           '28', '4'),
    ('81',  '2024-08-22 18:27:24.429524', 'TRENDY-THREAD_MS', null, null, 'Fantastic post! The information was detailed and well-explained.',                                         '29', '5'),
    ('82',  '2024-08-22 18:27:28.228977', 'TRENDY-THREAD_MS', null, null, 'This was a very informative read. The content was clear and well-structured.',                             '29', '6'),
    ('83',  '2024-08-22 18:27:45.194821', 'TRENDY-THREAD_MS', null, null, 'The post was very engaging and provided a lot of useful information. Well done!',                          '30', '7'),
    ('84',  '2024-08-22 18:27:48.888705', 'TRENDY-THREAD_MS', null, null, 'This article was insightful and easy to follow. I appreciated the clear explanations.',                   '30', '8'),
    ('85',  '2024-08-22 18:27:54.049741', 'TRENDY-THREAD_MS', null, null, 'Great post! The material was presented in a very accessible and engaging way.',                            '30', '9'),
    ('86',  '2024-08-22 18:28:03.658544', 'TRENDY-THREAD_MS', null, null, 'The content was very informative and well-organized. I found it very helpful.',                            '31', '10'),
    ('87',  '2024-08-22 18:28:21.118728', 'TRENDY-THREAD_MS', null, null, 'This post was really helpful. The clarity and detail provided were much appreciated.',                     '31', '11'),
    ('88',  '2024-08-22 18:28:41.341651', 'TRENDY-THREAD_MS', null, null, 'This was a very well-written post. The content was detailed and easy to digest.',                          '32', '12'),
    ('89',  '2024-08-22 18:28:49.994623', 'TRENDY-THREAD_MS', null, null, 'The post was very informative. I appreciated the straightforward approach and clarity.',                   '33', '2'),
    ('90',  '2024-08-22 18:28:54.141368', 'TRENDY-THREAD_MS', null, null, 'Excellent content! The article was well-organized and provided valuable insights.',                        '33', '3'),
    ('91',  '2024-08-22 18:28:58.647098', 'TRENDY-THREAD_MS', null, null, 'This was a fantastic read. The explanations were clear and the material was very useful.',                 '33', '4'),
    ('92',  '2024-08-22 18:29:06.839403', 'TRENDY-THREAD_MS', null, null, 'This was a fantastic read. The explanations were clear and the material was very useful.',                 '35', '5'),
    ('93',  '2024-08-22 18:29:15.134884', 'TRENDY-THREAD_MS', null, null, 'The post provided a lot of useful information. The content was well-explained and engaging.',              '35', '6'),
    ('94',  '2024-08-22 18:29:18.959457', 'TRENDY-THREAD_MS', null, null, 'Great post! The content was clear, and I appreciated the detailed explanations.',                          '35', '7'),
    ('95',  '2024-08-22 18:29:29.571734', 'TRENDY-THREAD_MS', null, null, 'This article was very well-done. The explanations were clear, and the information was presented effectively.', '36', '8'),
    ('96',  '2024-08-22 18:29:33.772587', 'TRENDY-THREAD_MS', null, null, 'I found this post to be very useful. The information was clear and well-organized.',                       '36', '9'),
    ('97',  '2024-08-22 18:29:37.66152',  'TRENDY-THREAD_MS', null, null, 'Excellent article! The clarity and detail provided in the content were much appreciated.',                 '36', '10'),
    ('98',  '2024-08-22 18:29:46.239814', 'TRENDY-THREAD_MS', null, null, 'The post was engaging and informative. I appreciated the thorough explanations provided.',                 '37', '11'),
    ('99',  '2024-08-22 18:29:52.858845', 'TRENDY-THREAD_MS', null, null, 'Great job on this article! The content was detailed and well-presented.',                                  '37', '12'),
    ('100', '2024-08-22 18:29:57.015605', 'TRENDY-THREAD_MS', null, null, 'I enjoyed this post. The material was clear and the explanations were very helpful.',                      '37', '2'),
    ('101', '2024-08-22 18:30:09.507239', 'TRENDY-THREAD_MS', null, null, 'This was a very insightful post. The information was presented in a clear and engaging manner.',           '38', '3'),
    ('102', '2024-08-22 18:30:22.982587', 'TRENDY-THREAD_MS', null, null, 'The post was very useful and well-written. I appreciated the clarity of the content.',                    '38', '4'),
    ('103', '2024-08-22 18:30:33.32433',  'TRENDY-THREAD_MS', null, null, 'The article was very well-organized and provided clear insights into the topic. Great job!',               '39', '5'),
    ('104', '2024-08-22 18:30:41.255053', 'TRENDY-THREAD_MS', null, null, 'I found this post to be extremely informative and easy to follow. Excellent work!',                       '39', '6'),
    ('105', '2024-08-22 18:30:45.116664', 'TRENDY-THREAD_MS', null, null, 'This post was very engaging and provided a lot of useful information. I really enjoyed reading it.',       '39', '7'),
    ('106', '2024-08-22 18:30:54.460708', 'TRENDY-THREAD_MS', null, null, 'The content was well-presented and the explanations were clear and concise. Thanks for sharing!',          '40', '8'),
    ('107', '2024-08-22 18:30:58.353637', 'TRENDY-THREAD_MS', null, null, 'Great article! The details were very well-explained and easy to understand.',                              '40', '9'),
    ('108', '2024-08-22 18:31:03.034119', 'TRENDY-THREAD_MS', null, null, 'The post was very insightful and provided valuable information. I appreciated the clear explanations.',    '40', '10'),
    ('109', '2024-08-22 18:31:12.372112', 'TRENDY-THREAD_MS', null, null, 'Excellent content! The information was detailed and presented in an easy-to-follow manner.',               '41', '11'),
    ('110', '2024-08-22 18:31:16.15703',  'TRENDY-THREAD_MS', null, null, 'I enjoyed this post. The explanations were clear and the content was very useful.',                        '41', '12'),
    ('111', '2024-08-22 18:31:20.621568', 'TRENDY-THREAD_MS', null, null, 'The article was very well-written and provided clear, valuable insights. Great job!',                     '41', '2'),
    ('112', '2024-08-22 18:31:28.240041', 'TRENDY-THREAD_MS', null, null, 'Great post! The information was clear and well-organized. I found it very helpful.',                       '42', '3'),
    ('113', '2024-08-22 18:31:38.682968', 'TRENDY-THREAD_MS', null, null, 'This post was engaging and informative. The explanations were clear and the content was very relevant.',   '42', '4'),
    ('114', '2024-08-22 18:31:42.603464', 'TRENDY-THREAD_MS', null, null, 'Excellent work! The post was very detailed and provided a lot of useful information.',                     '42', '5'),
    ('115', '2024-08-22 18:31:50.452343', 'TRENDY-THREAD_MS', null, null, 'The post was very clear and informative. I appreciated the thoroughness and detail.',                      '43', '6'),
    ('116', '2024-08-22 18:31:54.348947', 'TRENDY-THREAD_MS', null, null, 'Great article! The content was well-organized and the explanations were easy to understand.',              '43', '7'),
    ('117', '2024-08-22 18:31:57.884621', 'TRENDY-THREAD_MS', null, null, 'I found the post to be very insightful and the content was presented clearly. Well done!',                '43', '8'),
    ('118', '2024-08-22 18:32:08.890594', 'TRENDY-THREAD_MS', null, null, 'This article was very well-written. The information was clear and the explanations were thorough.',        '44', '9'),
    ('119', '2024-08-22 18:32:12.465215', 'TRENDY-THREAD_MS', null, null, 'The post was very engaging and provided a lot of useful details. I enjoyed reading it.',                   '44', '10'),
    ('120', '2024-08-22 18:32:17.687855', 'TRENDY-THREAD_MS', null, null, 'Great job on this article! The content was clear and the explanations were very helpful.',                 '44', '11'),
    ('121', '2024-08-22 18:32:21.680253', 'TRENDY-THREAD_MS', null, null, 'The information was presented in a clear and engaging way. I found it very useful.',                       '44', '12'),
    ('122', '2024-08-22 18:32:24.966715', 'TRENDY-THREAD_MS', null, null, 'The article was very informative and easy to understand. I appreciated the detailed explanations.',        '44', '2'),
    ('123', '2024-08-22 18:32:27.927837', 'TRENDY-THREAD_MS', null, null, 'This post was very engaging and the content was presented clearly. Great job!',                            '44', '3'),
    ('124', '2024-08-22 18:32:32.183553', 'TRENDY-THREAD_MS', null, null, 'The post provided a lot of useful information and was very well-organized. I enjoyed it.',                 '44', '4');

-- ========================================
-- END OF DATA INITIALIZATION
-- ========================================

-- Summary:
-- - 11 Bloggers (IDs 2-12)
-- - 11 Categories (IDs 1-11)
-- - 42 Posts (IDs 1-44, excluding 24-25)
-- - 124 Comments (IDs 1-124)

-- Note: For production use, replace BCrypt placeholder hashes with actual hashes
-- or use the signup endpoint to create users with properly hashed passwords.

-- Reset H2 auto-increment sequences to avoid primary key conflicts
ALTER TABLE blogger ALTER COLUMN id RESTART WITH 13;
ALTER TABLE category ALTER COLUMN id RESTART WITH 12;
ALTER TABLE post ALTER COLUMN id RESTART WITH 45;
ALTER TABLE comment ALTER COLUMN id RESTART WITH 125;
