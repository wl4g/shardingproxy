-- MySQL group replication cluster group 0
drop schema if exists userdb_r0z0mgr0;
drop schema if exists userdb_r0z0mgr1;
drop schema if exists userdb_r0z0mgr2;
-- MySQL group replication cluster group 1
drop schema if exists userdb_r0z0mgr3;
drop schema if exists userdb_r0z0mgr4;
drop schema if exists userdb_r0z0mgr6;

-- --------------------------------- group 0 --------------------------------------

-- MGR0
drop schema if exists userdb_r0z0mgr0;
create schema if not exists userdb_r0z0mgr0;
use userdb_r0z0mgr0;
drop table if exists t_user_0;
drop table if exists t_user_1;
drop table if exists t_user_2;
create table if not exists t_user_0(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db0，存储数据范围(ID取值)：[0 ~ 1000w)，分片算法：id%9=0,1,2';
create table if not exists t_user_1(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db0，存储数据范围(ID取值)：[1000w ~ 2000w)，分片算法：id%9=0,1,2';
create table if not exists t_user_2(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db0，存储数据范围(ID取值)：[2000w ~ 3000w)，分片算法：id%9=0,1,2';
insert into `userdb_r0z0mgr0`.`t_user_0` (`id`, `name`) values (109,'r0z0mgr0-t0-jack109');
insert into `userdb_r0z0mgr0`.`t_user_1` (`id`, `name`) values (10000004,'r0z0mgr0-t1-jack10000004');
insert into `userdb_r0z0mgr0`.`t_user_2` (`id`, `name`) values (27000027,'r0z0mgr0-t2-jack27000027');

-- MGR1
drop schema if exists userdb_r0z0mgr1;
create schema if not exists userdb_r0z0mgr1;
use userdb_r0z0mgr1;
drop table if exists t_user_0;
drop table if exists t_user_1;
drop table if exists t_user_2;
create table if not exists t_user_0(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db1，存储数据范围(ID取值)：[0 ~ 1000w)，分片算法：id%9=3,4,5';
create table if not exists t_user_1(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db1，存储数据范围(ID取值)：[1000w ~ 2500w)，分片算法：id%9=3,4,5';
create table if not exists t_user_2(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db1，存储数据范围(ID取值)：[2500w ~ 4000w)，分片算法：id%9=3,4,5';
insert into `userdb_r0z0mgr1`.`t_user_0` (`id`, `name`) values (201,'r0z0mgr1-t0-jack201');
insert into `userdb_r0z0mgr1`.`t_user_1` (`id`, `name`) values (10000021,'r0z0mgr1-t1-jack10000021');
insert into `userdb_r0z0mgr1`.`t_user_2` (`id`, `name`) values (25000025,'r0z0mgr1-t2-jack25000025');

-- MGR3
drop schema if exists userdb_r0z0mgr2;
create schema if not exists userdb_r0z0mgr2;
use userdb_r0z0mgr2;
drop table if exists t_user_0;
drop table if exists t_user_1;
drop table if exists t_user_2;
create table if not exists t_user_0(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db2，存储数据范围(ID取值)：[0 ~ 1000w)，分片算法：id%9=6,7,8';
create table if not exists t_user_1(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db2，存储数据范围(ID取值)：[1000w ~ 2500w)，分片算法：id%9=6,7,8';
create table if not exists t_user_2(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db2，存储数据范围(ID取值)：[2500w ~ 4000w)，分片算法：id%9=6,7,8';
insert into `userdb_r0z0mgr2`.`t_user_0` (`id`, `name`) values (250,'r0z0mgr2-t0-jack250');
insert into `userdb_r0z0mgr2`.`t_user_1` (`id`, `name`) values (10000034,'r0z0mgr2-t1-jack10000034');
insert into `userdb_r0z0mgr2`.`t_user_2` (`id`, `name`) values (25000037,'r0z0mgr2-t2-jack25000037');

-- --------------------------------- group 1 --------------------------------------

-- MGR4
drop schema if exists userdb_r0z0mgr3;
create schema if not exists userdb_r0z0mgr3;
use userdb_r0z0mgr3;
drop table if exists t_user_0;
drop table if exists t_user_1;
drop table if exists t_user_2;
drop table if exists t_user_3;
create table if not exists t_user_0(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db0，存储数据范围(ID取值)：[0 ~ 1000w)，分片算法：id%10=0,1,2,3';
create table if not exists t_user_1(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db0，存储数据范围(ID取值)：[1000w ~ 2000w)，分片算法：id%10=0,1,2,3';
create table if not exists t_user_2(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db0，存储数据范围(ID取值)：[2000w ~ 3000w)，分片算法：id%10=0,1,2,3';
create table if not exists t_user_3(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db0，存储数据范围(ID取值)：[3000w ~ 4000w)，分片算法：id%10=0,1,2,3';
insert into `userdb_r0z0mgr3`.`t_user_0` (`id`, `name`) values (100,'r0z0mgr3-t0-jack100');
insert into `userdb_r0z0mgr3`.`t_user_1` (`id`, `name`) values (10000001,'r0z0mgr3-t1-jack10000001');
insert into `userdb_r0z0mgr3`.`t_user_2` (`id`, `name`) values (20000001,'r0z0mgr3-t2-jack20000001');
insert into `userdb_r0z0mgr3`.`t_user_3` (`id`, `name`) values (30000001,'r0z0mgr3-t3-jack30000001');

-- MGR5
drop schema if exists userdb_r0z0mgr4;
create schema if not exists userdb_r0z0mgr4;
use userdb_r0z0mgr4;
drop table if exists t_user_0;
drop table if exists t_user_1;
drop table if exists t_user_2;
create table if not exists t_user_0(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db1，存储数据范围(ID取值)：[0 ~ 1000w)，分片算法：id%10=4,5,6';
create table if not exists t_user_1(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db1，存储数据范围(ID取值)：[1000w ~ 2500w)，分片算法：id%10=4,5,6';
create table if not exists t_user_2(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db1，存储数据范围(ID取值)：[2500w ~ 4000w)，分片算法：id%10=4,5,6';
insert into `userdb_r0z0mgr4`.`t_user_0` (`id`, `name`) values (104,'r0z0mgr4-t0-jack104');
insert into `userdb_r0z0mgr4`.`t_user_1` (`id`, `name`) values (10000005,'r0z0mgr4-t1-jack10000005');
insert into `userdb_r0z0mgr4`.`t_user_2` (`id`, `name`) values (25000006,'r0z0mgr4-t2-jack25000006');

-- MGR6
drop schema if exists userdb_r0z0mgr6;
create schema if not exists userdb_r0z0mgr6;
use userdb_r0z0mgr6;
drop table if exists t_user_0;
drop table if exists t_user_1;
drop table if exists t_user_2;
create table if not exists t_user_0(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db2，存储数据范围(ID取值)：[0 ~ 1000w)，分片算法：id%10=7,8,9';
create table if not exists t_user_1(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db2，存储数据范围(ID取值)：[1000w ~ 2500w)，分片算法：id%10=7,8,9';
create table if not exists t_user_2(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db2，存储数据范围(ID取值)：[2500w ~ 4000w)，分片算法：id%10=7,8,9';
insert into `userdb_r0z0mgr6`.`t_user_0` (`id`, `name`) values (107,'r0z0mgr6-t0-jack107');
insert into `userdb_r0z0mgr6`.`t_user_1` (`id`, `name`) values (10000008,'r0z0mgr6-t1-jack10000008');
insert into `userdb_r0z0mgr6`.`t_user_2` (`id`, `name`) values (25000009,'r0z0mgr6-t2-jack25000009');

