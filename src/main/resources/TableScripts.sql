DROP SCHEMA IF EXISTS linkedin_db;

CREATE SCHEMA linkedin_db;
USE linkedin_db;

-- Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(15),
    profile_photo_url VARCHAR(500),
    headline VARCHAR(220),
    location VARCHAR(100),
    summary TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Education table
CREATE TABLE education (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    school VARCHAR(200) NOT NULL,
    degree VARCHAR(100),
    field_of_study VARCHAR(100),
    start_year INT,
    end_year INT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Experience table
CREATE TABLE experience (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    company VARCHAR(100) NOT NULL,
    location VARCHAR(100),
    start_date DATE,
    end_date DATE,
    is_current BOOLEAN DEFAULT FALSE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Skills table
CREATE TABLE skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Posts table
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    post_type ENUM('POST', 'ARTICLE') DEFAULT 'POST',
    status ENUM('DRAFT', 'PUBLISHED', 'SCHEDULED') DEFAULT 'PUBLISHED',
    scheduled_at TIMESTAMP NULL,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Hashtags table
CREATE TABLE hashtags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tag VARCHAR(100) NOT NULL UNIQUE
);

-- Post hashtags junction
CREATE TABLE post_hashtags (
    post_id BIGINT NOT NULL,
    hashtag_id BIGINT NOT NULL,
    PRIMARY KEY (post_id, hashtag_id),
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (hashtag_id) REFERENCES hashtags(id) ON DELETE CASCADE
);

-- Likes table
CREATE TABLE likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_like (post_id, user_id),
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Comments table
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Connections table
CREATE TABLE connections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'BLOCKED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_connection (sender_id, receiver_id),
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Jobs table
CREATE TABLE jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    company VARCHAR(200) NOT NULL,
    location VARCHAR(100),
    job_type ENUM('FULL_TIME', 'PART_TIME', 'REMOTE', 'CONTRACT', 'INTERNSHIP') NOT NULL,
    experience_level ENUM('ENTRY', 'MID', 'SENIOR', 'EXECUTIVE') DEFAULT 'ENTRY',
    description TEXT NOT NULL,
    requirements TEXT,
    benefits TEXT,
    application_deadline DATE,
    posted_by BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (posted_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Job Applications table
CREATE TABLE job_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    resume_url VARCHAR(500),
    cover_letter TEXT,
    status ENUM('APPLIED', 'REVIEWING', 'INTERVIEW', 'OFFERED', 'REJECTED', 'WITHDRAWN') DEFAULT 'APPLIED',
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_application (job_id, user_id),
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Saved Jobs table
CREATE TABLE saved_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_saved_job (job_id, user_id),
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Notifications table
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type ENUM('CONNECTION_REQUEST', 'CONNECTION_ACCEPTED', 'POST_LIKE', 'POST_COMMENT', 'JOB_APPLICATION_UPDATE') NOT NULL,
    message VARCHAR(500) NOT NULL,
    reference_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =========================
-- USERS
-- =========================
INSERT INTO users (first_name, last_name, email, password, phone_number, profile_photo_url, headline, location, summary, is_active) VALUES
('Dhruv', 'Kashyap', 'dhruv@example.com', '$2a$10$abcdefghijklmnopqrstuv', '9876543210', 'https://randomuser.me/api/portraits/men/1.jpg', 'Full Stack Developer', 'Bengaluru, India', 'Passionate developer building scalable web apps with Spring Boot and React.', TRUE),
('Aarav', 'Sharma', 'aarav@example.com', '$2a$10$abcdefghijklmnopqrstuv', '9876543211', 'https://randomuser.me/api/portraits/men/2.jpg', 'Backend Developer', 'Hyderabad, India', 'Backend engineer focused on secure APIs and performance.', TRUE),
('Priya', 'Verma', 'priya@example.com', '$2a$10$abcdefghijklmnopqrstuv', '9876543212', 'https://randomuser.me/api/portraits/women/3.jpg', 'Frontend Engineer', 'Pune, India', 'Frontend developer who loves React, TypeScript, and polished UI.', TRUE),
('Sneha', 'Reddy', 'sneha@example.com', '$2a$10$abcdefghijklmnopqrstuv', '9876543213', 'https://randomuser.me/api/portraits/women/4.jpg', 'Data Analyst', 'Chennai, India', 'I work with SQL, Python, and dashboards to uncover insights.', TRUE),
('Rahul', 'Mehta', 'rahul@example.com', '$2a$10$abcdefghijklmnopqrstuv', '9876543214', 'https://randomuser.me/api/portraits/men/5.jpg', 'DevOps Engineer', 'Mumbai, India', 'Automating infrastructure and building fast delivery pipelines.', TRUE),
('Ananya', 'Gupta', 'ananya@example.com', '$2a$10$abcdefghijklmnopqrstuv', '9876543215', 'https://randomuser.me/api/portraits/women/6.jpg', 'Product Manager', 'Delhi, India', 'Turning product ideas into user outcomes.', TRUE),
('Rohan', 'Patel', 'rohan@example.com', '$2a$10$abcdefghijklmnopqrstuv', '9876543216', 'https://randomuser.me/api/portraits/men/7.jpg', 'Java Developer', 'Ahmedabad, India', 'Java and Spring Boot developer interested in enterprise apps.', TRUE),
('Isha', 'Kapoor', 'isha@example.com', '$2a$10$abcdefghijklmnopqrstuv', '9876543217', 'https://randomuser.me/api/portraits/women/8.jpg', 'UI/UX Designer', 'Noida, India', 'Designing user-friendly and visually consistent interfaces.', TRUE),
('Karan', 'Malhotra', 'karan@example.com', '$2a$10$abcdefghijklmnopqrstuv', '9876543218', 'https://randomuser.me/api/portraits/men/9.jpg', 'Cloud Engineer', 'Gurugram, India', 'AWS, containers, observability, and infrastructure as code.', TRUE),
('Meera', 'Joshi', 'meera@example.com', '$2a$10$abcdefghijklmnopqrstuv', '9876543219', 'https://randomuser.me/api/portraits/women/10.jpg', 'Business Analyst', 'Kolkata, India', 'Bridging business requirements and technical execution.', TRUE),
('Vikram', 'Singh', 'vikram@example.com', '$2a$10$abcdefghijklmnopqrstuv', '9876543220', 'https://randomuser.me/api/portraits/men/11.jpg', 'Software Engineer', 'Jaipur, India', 'Building backend systems and exploring distributed design.', TRUE),
('Neha', 'Agarwal', 'neha@example.com', '$2a$10$abcdefghijklmnopqrstuv', '9876543221', 'https://randomuser.me/api/portraits/women/12.jpg', 'QA Engineer', 'Lucknow, India', 'Focused on test automation and product quality.', TRUE);

-- =========================
-- EDUCATION
-- =========================
INSERT INTO education (user_id, school, degree, field_of_study, start_year, end_year, description) VALUES
(1, 'RNS Institute of Technology', 'B.E.', 'Computer Science', 2021, 2025, 'Focused on software development and full stack engineering.'),
(2, 'IIT Delhi', 'B.Tech', 'Computer Science', 2018, 2022, 'Worked on backend and system design projects.'),
(3, 'VIT Vellore', 'B.Tech', 'Information Technology', 2019, 2023, 'Built frontend-heavy projects and UI systems.'),
(4, 'Christ University', 'B.Sc', 'Data Science', 2018, 2021, 'Worked with analytics, ML basics, and visualization.'),
(5, 'NIT Trichy', 'B.Tech', 'Computer Engineering', 2017, 2021, 'Interested in systems, deployment, and automation.'),
(6, 'Delhi University', 'MBA', 'Product Management', 2020, 2022, 'Focused on product strategy and execution.'),
(7, 'Nirma University', 'B.Tech', 'Computer Science', 2018, 2022, 'Strong foundation in Java and DBMS.'),
(8, 'Amity University', 'B.Des', 'User Experience Design', 2019, 2023, 'Studied UX principles, design systems, and prototyping.'),
(9, 'Manipal University', 'B.Tech', 'Information Technology', 2017, 2021, 'Worked on cloud and infrastructure projects.'),
(10, 'St. Xavier''s College', 'BBA', 'Business Analytics', 2018, 2021, 'Learned business strategy and requirement analysis.'),
(11, 'JECRC University', 'B.Tech', 'Computer Science', 2018, 2022, 'Built software engineering projects and APIs.'),
(12, 'AKTU', 'B.Tech', 'Information Technology', 2018, 2022, 'Focused on testing, QA, and automation frameworks.');

-- =========================
-- EXPERIENCE
-- =========================
INSERT INTO experience (user_id, title, company, location, start_date, end_date, is_current, description) VALUES
(1, 'Software Developer Intern', 'CodeCraft', 'Bengaluru, India', '2024-01-10', '2024-06-10', FALSE, 'Worked on REST APIs and React integration.'),
(1, 'Freelance Web Developer', 'Self Employed', 'Remote', '2024-07-01', NULL, TRUE, 'Built websites and dashboards for clients.'),
(2, 'Backend Developer', 'TechNova', 'Hyderabad, India', '2022-07-01', NULL, TRUE, 'Built APIs and security modules using Spring Boot.'),
(3, 'Frontend Developer', 'PixelSoft', 'Pune, India', '2023-02-01', NULL, TRUE, 'Created reusable React components and pages.'),
(4, 'Data Analyst', 'InsightX', 'Chennai, India', '2021-08-01', NULL, TRUE, 'Built business dashboards and SQL reports.'),
(5, 'DevOps Engineer', 'CloudBridge', 'Mumbai, India', '2021-09-15', NULL, TRUE, 'Managed cloud infra and CI/CD pipelines.'),
(6, 'Associate Product Manager', 'InnoWorks', 'Delhi, India', '2022-06-01', NULL, TRUE, 'Defined features and aligned product strategy.'),
(7, 'Java Developer', 'FinAxis', 'Ahmedabad, India', '2022-08-01', NULL, TRUE, 'Worked on enterprise backend services.'),
(8, 'UI/UX Designer', 'DesignNest', 'Noida, India', '2023-01-01', NULL, TRUE, 'Designed user flows and prototypes.'),
(9, 'Cloud Engineer', 'SkyOps', 'Gurugram, India', '2021-07-01', NULL, TRUE, 'Built AWS infrastructure and monitoring.'),
(10, 'Business Analyst', 'BizMatrix', 'Kolkata, India', '2022-03-01', NULL, TRUE, 'Converted business needs into product requirements.'),
(11, 'Software Engineer', 'BlueStack', 'Jaipur, India', '2022-09-01', NULL, TRUE, 'Worked on Java APIs and performance tuning.'),
(12, 'QA Engineer', 'TestForge', 'Lucknow, India', '2022-05-15', NULL, TRUE, 'Created automated tests and bug reports.');

-- =========================
-- SKILLS
-- =========================
INSERT INTO skills (user_id, skill_name) VALUES
(1, 'Java'), (1, 'Spring Boot'), (1, 'React'), (1, 'MySQL'), (1, 'Git'),
(2, 'Java'), (2, 'Spring Security'), (2, 'MySQL'), (2, 'REST API'),
(3, 'React'), (3, 'TypeScript'), (3, 'JavaScript'), (3, 'CSS'),
(4, 'SQL'), (4, 'Python'), (4, 'Power BI'),
(5, 'Docker'), (5, 'Kubernetes'), (5, 'AWS'),
(6, 'Agile'), (6, 'Roadmapping'), (6, 'Product Strategy'),
(7, 'Java'), (7, 'Hibernate'), (7, 'Postman'),
(8, 'Figma'), (8, 'Wireframing'), (8, 'Design Systems'),
(9, 'AWS'), (9, 'Terraform'), (9, 'CI/CD'),
(10, 'Requirement Analysis'), (10, 'Documentation'), (10, 'SQL'),
(11, 'Spring Boot'), (11, 'Microservices'), (11, 'Git'),
(12, 'Selenium'), (12, 'JUnit'), (12, 'Automation Testing');

-- =========================
-- POSTS
-- =========================
INSERT INTO posts (user_id, content, post_type, status, scheduled_at, image_url) VALUES
(1, 'Excited to share that I am building a LinkedIn clone using Spring Boot, React, and MySQL.', 'POST', 'PUBLISHED', NULL, NULL),
(2, 'Implemented JWT authentication today. Took effort, but feels great now.', 'POST', 'PUBLISHED', NULL, NULL),
(3, 'Small UI improvements can dramatically change how polished a product feels.', 'POST', 'PUBLISHED', NULL, NULL),
(4, 'Data is useful only when it leads to better decisions.', 'POST', 'PUBLISHED', NULL, NULL),
(5, 'Automated deployment reduced our release time massively.', 'POST', 'PUBLISHED', NULL, NULL),
(6, 'User outcomes matter more than feature counts.', 'POST', 'PUBLISHED', NULL, NULL),
(7, 'Revisiting core Java concepts never goes out of style.', 'POST', 'PUBLISHED', NULL, NULL),
(8, 'A good design system saves everyone time.', 'POST', 'PUBLISHED', NULL, NULL),
(9, 'Cloud cost optimization is underrated engineering work.', 'POST', 'PUBLISHED', NULL, NULL),
(10, 'Requirements clarity can save weeks of rework.', 'POST', 'PUBLISHED', NULL, NULL),
(11, 'Today I optimized one API and the difference was immediately visible.', 'POST', 'PUBLISHED', NULL, NULL),
(12, 'Automated test coverage is not glamorous, but it is pure peace of mind.', 'POST', 'PUBLISHED', NULL, NULL),
(1, 'Learning full stack by building projects is honestly the best teacher.', 'ARTICLE', 'PUBLISHED', NULL, NULL),
(3, 'React component reuse makes frontend development way more maintainable.', 'ARTICLE', 'PUBLISHED', NULL, NULL),
(5, 'CI/CD done right feels like magic.', 'ARTICLE', 'PUBLISHED', NULL, NULL);

-- =========================
-- HASHTAGS
-- =========================
INSERT INTO hashtags (tag) VALUES
('java'),
('springboot'),
('react'),
('mysql'),
('frontend'),
('backend'),
('datascience'),
('devops'),
('cloud'),
('product'),
('testing'),
('uiux'),
('webdevelopment');

-- =========================
-- POST HASHTAGS
-- =========================
INSERT INTO post_hashtags (post_id, hashtag_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 13),
(2, 1), (2, 2), (2, 6),
(3, 5), (3, 12),
(4, 7),
(5, 8),
(6, 10),
(7, 1),
(8, 12), (8, 5),
(9, 9),
(10, 10),
(11, 6),
(12, 11),
(13, 13), (13, 1), (13, 3),
(14, 3), (14, 5),
(15, 8), (15, 9);

-- =========================
-- LIKES
-- =========================
INSERT INTO likes (post_id, user_id) VALUES
(1, 2), (1, 3), (1, 5), (1, 6), (1, 8),
(2, 1), (2, 5), (2, 7),
(3, 1), (3, 6), (3, 8), (3, 10),
(4, 2), (4, 5), (4, 10),
(5, 1), (5, 2), (5, 3), (5, 9),
(6, 1), (6, 3), (6, 10),
(7, 2), (7, 11),
(8, 3), (8, 6), (8, 12),
(9, 5), (9, 11),
(10, 4), (10, 6),
(11, 1), (11, 2), (11, 7), (11, 9),
(12, 3), (12, 5), (12, 11),
(13, 2), (13, 3), (13, 4), (13, 6),
(14, 1), (14, 8), (14, 10),
(15, 2), (15, 5), (15, 9);

-- =========================
-- COMMENTS
-- =========================
INSERT INTO comments (post_id, user_id, content) VALUES
(1, 2, 'Great project idea.'),
(1, 3, 'Looks really solid.'),
(1, 6, 'Nice one, keep building.'),
(2, 1, 'JWT setup can be rough at first 😄'),
(2, 5, 'Security work always feels rewarding.'),
(3, 8, 'Totally agree with this.'),
(3, 1, 'Clean UI is underrated.'),
(4, 10, 'Very true. Good analysis changes decisions.'),
(5, 9, 'Love deployment wins like this.'),
(5, 2, 'That is a huge improvement.'),
(6, 1, 'Strong product take.'),
(7, 11, 'Core concepts always matter.'),
(8, 3, 'Design systems save so much effort.'),
(9, 5, 'Cloud bills keep everyone humble.'),
(10, 6, 'Requirement clarity is half the battle.'),
(11, 2, 'Optimization wins are addictive.'),
(12, 12, 'Testing really does save teams.'),
(13, 3, 'Project-based learning works best.'),
(14, 8, 'Reusable components are gold.'),
(15, 9, 'CI/CD really does feel magical.');

-- =========================
-- CONNECTIONS
-- =========================
INSERT INTO connections (sender_id, receiver_id, status) VALUES
(1, 2, 'ACCEPTED'),
(1, 3, 'ACCEPTED'),
(1, 5, 'PENDING'),
(1, 6, 'ACCEPTED'),
(1, 8, 'PENDING'),
(2, 3, 'ACCEPTED'),
(2, 7, 'ACCEPTED'),
(2, 9, 'PENDING'),
(3, 4, 'PENDING'),
(3, 8, 'ACCEPTED'),
(4, 6, 'ACCEPTED'),
(4, 10, 'PENDING'),
(5, 9, 'ACCEPTED'),
(6, 10, 'ACCEPTED'),
(7, 11, 'ACCEPTED'),
(8, 12, 'PENDING'),
(9, 11, 'ACCEPTED'),
(10, 12, 'ACCEPTED');

-- =========================
-- JOBS
-- =========================
INSERT INTO jobs (title, company, location, job_type, experience_level, description, requirements, benefits, application_deadline, posted_by, is_active) VALUES
('Software Engineer Intern', 'TechNova', 'Bengaluru, India', 'INTERNSHIP', 'ENTRY', 'Work on full stack features using Java and React.', 'Java, SQL, REST APIs, React basics', 'Mentorship, PPO opportunity', '2026-05-30', 2, TRUE),
('Frontend Developer', 'PixelSoft', 'Pune, India', 'FULL_TIME', 'ENTRY', 'Build responsive frontend components.', 'React, TypeScript, CSS', 'Health insurance, hybrid work', '2026-06-15', 3, TRUE),
('Backend Developer', 'CodeBridge', 'Hyderabad, India', 'FULL_TIME', 'MID', 'Build scalable backend systems.', 'Java, Spring Boot, MySQL', 'Bonus, flexible timings', '2026-04-20', 2, TRUE),
('Data Analyst', 'InsightX', 'Chennai, India', 'FULL_TIME', 'ENTRY', 'Analyze business and product data.', 'SQL, Python, Excel', 'Medical benefits, growth opportunities', '2026-05-10', 4, TRUE),
('DevOps Engineer', 'CloudBridge', 'Mumbai, India', 'REMOTE', 'MID', 'Manage infra and pipelines.', 'AWS, Docker, Kubernetes', 'Remote work, certifications', '2026-05-25', 5, TRUE),
('Associate Product Manager', 'InnoWorks', 'Delhi, India', 'FULL_TIME', 'MID', 'Work with cross-functional teams on feature delivery.', 'Agile, product thinking, communication', 'Medical insurance, flexible work', '2026-06-01', 6, TRUE),
('Java Developer', 'FinAxis', 'Ahmedabad, India', 'FULL_TIME', 'ENTRY', 'Build backend modules and integrations.', 'Java, Spring, SQL', 'Learning budget, insurance', '2026-05-28', 7, TRUE),
('UI/UX Designer', 'DesignNest', 'Noida, India', 'FULL_TIME', 'ENTRY', 'Design user journeys and interface systems.', 'Figma, prototyping, UX research', 'Hybrid work, design mentorship', '2026-06-20', 8, TRUE),
('Cloud Engineer', 'SkyOps', 'Gurugram, India', 'FULL_TIME', 'MID', 'Build and monitor cloud environments.', 'AWS, Terraform, monitoring', 'Bonus, remote flexibility', '2026-05-18', 9, TRUE),
('QA Engineer', 'TestForge', 'Lucknow, India', 'FULL_TIME', 'ENTRY', 'Develop automated test suites and quality checks.', 'Selenium, JUnit, API testing', 'Health cover, training support', '2026-06-10', 12, TRUE);

-- =========================
-- JOB APPLICATIONS
-- =========================
INSERT INTO job_applications (job_id, user_id, resume_url, cover_letter, status) VALUES
(1, 1, 'https://example.com/resumes/dhruv.pdf', 'I am excited to apply for this internship and contribute to full stack projects.', 'APPLIED'),
(2, 1, 'https://example.com/resumes/dhruv.pdf', 'I enjoy building clean and responsive user interfaces.', 'REVIEWING'),
(3, 2, 'https://example.com/resumes/aarav.pdf', 'My backend experience aligns well with this role.', 'INTERVIEW'),
(4, 3, 'https://example.com/resumes/priya.pdf', 'I bring strong analytical and technical problem-solving skills.', 'APPLIED'),
(5, 5, 'https://example.com/resumes/rahul.pdf', 'This role is a strong fit for my DevOps background.', 'REVIEWING'),
(6, 10, 'https://example.com/resumes/meera.pdf', 'I would love to contribute to product planning and execution.', 'APPLIED'),
(7, 11, 'https://example.com/resumes/vikram.pdf', 'I have solid Java fundamentals and backend project experience.', 'APPLIED'),
(8, 8, 'https://example.com/resumes/isha.pdf', 'I am excited to work on meaningful design systems and flows.', 'INTERVIEW'),
(9, 9, 'https://example.com/resumes/karan.pdf', 'My cloud engineering experience directly matches this role.', 'APPLIED'),
(10, 12, 'https://example.com/resumes/neha.pdf', 'My background in automation testing makes me a strong candidate.', 'REVIEWING');

-- =========================
-- SAVED JOBS
-- =========================
INSERT INTO saved_jobs (job_id, user_id) VALUES
(1, 1), (2, 1), (3, 1),
(4, 4), (5, 2), (6, 6),
(7, 7), (8, 8), (9, 9), (10, 12),
(3, 11), (2, 3), (6, 10);

-- =========================
-- NOTIFICATIONS
-- =========================
INSERT INTO notifications (user_id, type, message, reference_id, is_read) VALUES
(1, 'CONNECTION_REQUEST', 'Rahul Mehta sent you a connection request.', 3, FALSE),
(1, 'CONNECTION_REQUEST', 'Isha Kapoor sent you a connection request.', 5, FALSE),
(1, 'CONNECTION_ACCEPTED', 'Ananya Gupta accepted your connection request.', 4, TRUE),
(1, 'POST_LIKE', 'Aarav Sharma liked your post.', 1, FALSE),
(1, 'POST_COMMENT', 'Priya Verma commented on your post.', 1, FALSE),
(2, 'POST_LIKE', 'Dhruv Kashyap liked your post.', 2, TRUE),
(3, 'JOB_APPLICATION_UPDATE', 'Your application for Frontend Developer is under review.', 2, FALSE),
(5, 'JOB_APPLICATION_UPDATE', 'Your application for DevOps Engineer is under review.', 5, FALSE),
(8, 'JOB_APPLICATION_UPDATE', 'You have been shortlisted for UI/UX Designer.', 8, FALSE),
(12, 'JOB_APPLICATION_UPDATE', 'Your application for QA Engineer is under review.', 10, TRUE),
(6, 'CONNECTION_ACCEPTED', 'Dhruv Kashyap accepted your connection request.', 6, TRUE),
(3, 'POST_COMMENT', 'Isha Kapoor commented on your post.', 14, FALSE);