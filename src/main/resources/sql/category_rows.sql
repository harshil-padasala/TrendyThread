INSERT INTO category (
    id,
    created_at,
    created_by,
    updated_at,
    updated_by,
    description,
    name,
    featured,
    display_order,
    post_count,
    auto_suggested
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

ALTER TABLE category ALTER COLUMN id RESTART WITH 12;