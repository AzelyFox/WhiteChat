-- phpMyAdmin SQL Dump
-- version 4.8.5
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- 생성 시간: 19-09-21 17:21
-- 서버 버전: 5.5.60-MariaDB
-- PHP 버전: 7.3.5

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 데이터베이스: `AppWhiteChat`
--

-- --------------------------------------------------------

--
-- 테이블 구조 `File`
--

CREATE TABLE `File` (
  `file_index` int(11) NOT NULL COMMENT '(Unique) File Index',
  `file_name` varchar(45) DEFAULT NULL COMMENT 'File Name',
  `file_location` varchar(45) DEFAULT NULL COMMENT 'File Location',
  `file_url` varchar(45) DEFAULT NULL COMMENT 'File Url',
  `file_available` int(11) DEFAULT NULL COMMENT 'File Is Available',
  `file_uploader` int(11) DEFAULT NULL COMMENT 'File Uploader Index',
  `file_expire_date` timestamp NULL DEFAULT NULL COMMENT 'File Expiration Date'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 테이블 구조 `Message`
--

CREATE TABLE `Message` (
  `message_index` int(11) NOT NULL COMMENT '(Unique) Message Index',
  `message_room` int(11) NOT NULL COMMENT 'Room Index',
  `message_owner` int(11) NOT NULL COMMENT 'Message Sender',
  `message_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Message Created TIMESTAMP',
  `message_content` varchar(200) NOT NULL COMMENT 'Message Content',
  `message_file` int(11) DEFAULT '0' COMMENT 'File index if attached'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 테이블 구조 `Participant`
--

CREATE TABLE `Participant` (
  `participant_index` int(11) NOT NULL,
  `room_index` int(11) NOT NULL COMMENT 'Room Key',
  `user_index` int(11) NOT NULL COMMENT 'Participating User Index'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 테이블 구조 `Post`
--

CREATE TABLE `Post` (
  `post_index` int(11) NOT NULL COMMENT '(Unique) Post Index',
  `post_owner` int(11) DEFAULT NULL COMMENT 'Post Owner',
  `post_subject` varchar(45) DEFAULT NULL COMMENT 'Post Subject',
  `post_content` varchar(45) DEFAULT NULL COMMENT 'Post Content',
  `post_is_notice` int(11) DEFAULT NULL COMMENT 'Post Is Global Notice',
  `post_created` timestamp NULL DEFAULT NULL COMMENT 'Post Created TIMESTAMP'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 테이블 구조 `Room`
--

CREATE TABLE `Room` (
  `room_index` int(11) NOT NULL COMMENT '(Unique) Room Index',
  `room_name` varchar(45) DEFAULT '' COMMENT 'Room Name',
  `room_password` varchar(45) DEFAULT NULL COMMENT 'Room Password',
  `room_notice` int(11) DEFAULT '0' COMMENT 'Room Notice Message Index'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 테이블 구조 `Social`
--

CREATE TABLE `Social` (
  `social_index` int(11) NOT NULL,
  `social_from` int(11) NOT NULL COMMENT 'User Index',
  `social_to` int(11) NOT NULL COMMENT 'Target User Index',
  `social_type` int(11) NOT NULL COMMENT 'Social Type'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 테이블 구조 `SocialType`
--

CREATE TABLE `SocialType` (
  `social_index` int(11) NOT NULL COMMENT '(Unique) Social Index',
  `social_name` varchar(45) NOT NULL COMMENT 'Social Name',
  `social_priority` int(11) NOT NULL COMMENT 'Social List Showing Priority'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 테이블 구조 `User`
--

CREATE TABLE `User` (
  `user_index` int(11) NOT NULL COMMENT '(Unique) User Index',
  `user_id` varchar(45) NOT NULL COMMENT '(Unique) User ID',
  `user_password` varchar(45) NOT NULL COMMENT '(Unique) User Password',
  `user_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'User Created TIMESTAMP',
  `user_rank` int(11) NOT NULL DEFAULT '1' COMMENT 'User Rank',
  `user_nickname` varchar(60) DEFAULT NULL COMMENT '(Unique) User NickName',
  `user_thumbnail` varchar(70) DEFAULT '' COMMENT 'User Thumbnail File Index',
  `user_cash` int(11) DEFAULT '0' COMMENT 'User Cash Amount',
  `user_banned` int(11) DEFAULT '0' COMMENT 'User Banned Date',
  `user_client` varchar(50) NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 테이블 구조 `UserRank`
--

CREATE TABLE `UserRank` (
  `rank_index` int(11) NOT NULL COMMENT '(Unique) Rank Index',
  `rank_name` varchar(45) DEFAULT NULL COMMENT 'Rank Name',
  `rank_can_message` int(11) DEFAULT NULL COMMENT 'This Rank Can Message',
  `rank_can_post` int(11) DEFAULT NULL COMMENT 'This Rank Can Post',
  `rank_can_post_notice` int(11) DEFAULT NULL COMMENT 'This Rank Can Global Notice Post'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- 덤프된 테이블의 인덱스
--

--
-- 테이블의 인덱스 `File`
--
ALTER TABLE `File`
  ADD PRIMARY KEY (`file_index`);

--
-- 테이블의 인덱스 `Message`
--
ALTER TABLE `Message`
  ADD PRIMARY KEY (`message_index`);

--
-- 테이블의 인덱스 `Participant`
--
ALTER TABLE `Participant`
  ADD PRIMARY KEY (`participant_index`);

--
-- 테이블의 인덱스 `Post`
--
ALTER TABLE `Post`
  ADD PRIMARY KEY (`post_index`);

--
-- 테이블의 인덱스 `Room`
--
ALTER TABLE `Room`
  ADD PRIMARY KEY (`room_index`);

--
-- 테이블의 인덱스 `Social`
--
ALTER TABLE `Social`
  ADD PRIMARY KEY (`social_index`);

--
-- 테이블의 인덱스 `SocialType`
--
ALTER TABLE `SocialType`
  ADD PRIMARY KEY (`social_index`);

--
-- 테이블의 인덱스 `User`
--
ALTER TABLE `User`
  ADD PRIMARY KEY (`user_index`),
  ADD UNIQUE KEY `user_id` (`user_id`),
  ADD KEY `user_index` (`user_index`);

--
-- 테이블의 인덱스 `UserRank`
--
ALTER TABLE `UserRank`
  ADD PRIMARY KEY (`rank_index`);

--
-- 덤프된 테이블의 AUTO_INCREMENT
--

--
-- 테이블의 AUTO_INCREMENT `File`
--
ALTER TABLE `File`
  MODIFY `file_index` int(11) NOT NULL AUTO_INCREMENT COMMENT '(Unique) File Index';

--
-- 테이블의 AUTO_INCREMENT `Message`
--
ALTER TABLE `Message`
  MODIFY `message_index` int(11) NOT NULL AUTO_INCREMENT COMMENT '(Unique) Message Index';

--
-- 테이블의 AUTO_INCREMENT `Participant`
--
ALTER TABLE `Participant`
  MODIFY `participant_index` int(11) NOT NULL AUTO_INCREMENT;

--
-- 테이블의 AUTO_INCREMENT `Post`
--
ALTER TABLE `Post`
  MODIFY `post_index` int(11) NOT NULL AUTO_INCREMENT COMMENT '(Unique) Post Index';

--
-- 테이블의 AUTO_INCREMENT `Room`
--
ALTER TABLE `Room`
  MODIFY `room_index` int(11) NOT NULL AUTO_INCREMENT COMMENT '(Unique) Room Index';

--
-- 테이블의 AUTO_INCREMENT `Social`
--
ALTER TABLE `Social`
  MODIFY `social_index` int(11) NOT NULL AUTO_INCREMENT;

--
-- 테이블의 AUTO_INCREMENT `SocialType`
--
ALTER TABLE `SocialType`
  MODIFY `social_index` int(11) NOT NULL AUTO_INCREMENT COMMENT '(Unique) Social Index';

--
-- 테이블의 AUTO_INCREMENT `User`
--
ALTER TABLE `User`
  MODIFY `user_index` int(11) NOT NULL AUTO_INCREMENT COMMENT '(Unique) User Index';

--
-- 테이블의 AUTO_INCREMENT `UserRank`
--
ALTER TABLE `UserRank`
  MODIFY `rank_index` int(11) NOT NULL AUTO_INCREMENT COMMENT '(Unique) Rank Index';
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
