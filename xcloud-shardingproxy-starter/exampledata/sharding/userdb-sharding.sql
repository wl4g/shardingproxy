drop schema if exists userdb_g0db0;
drop schema if exists userdb_g0db1;
drop schema if exists userdb_g0db2;
drop schema if exists userdb_g1db0;
drop schema if exists userdb_g1db1;
drop schema if exists userdb_g1db2;

-- Sharding Group0(db0)
drop schema if exists userdb_g0db0;
create schema if not exists userdb_g0db0;
use userdb_g0db0;
drop table if exists t_user_0;
drop table if exists t_user_1;
drop table if exists t_user_2;
create table if not exists t_user_0(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db0，存储数据范围(ID取值)：[0 ~ 1000w)，分片算法：id%9=0,1,2';
create table if not exists t_user_1(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db0，存储数据范围(ID取值)：[1000w ~ 2000w)，分片算法：id%9=0,1,2';
create table if not exists t_user_2(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db0，存储数据范围(ID取值)：[2000w ~ 3000w)，分片算法：id%9=0,1,2';
insert into `userdb_g0db0`.`t_user_0` (`id`, `name`) values (109,'g0db0-t0-jack109');
insert into `userdb_g0db0`.`t_user_1` (`id`, `name`) values (10000004,'g0db0-t1-jack10000004');
insert into `userdb_g0db0`.`t_user_2` (`id`, `name`) values (27000027,'g0db0-t2-jack27000027');

-- Sharding Group0(db1)
drop schema if exists userdb_g0db1;
create schema if not exists userdb_g0db1;
use userdb_g0db1;
drop table if exists t_user_0;
drop table if exists t_user_1;
drop table if exists t_user_2;
create table if not exists t_user_0(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db1，存储数据范围(ID取值)：[0 ~ 1000w)，分片算法：id%9=3,4,5';
create table if not exists t_user_1(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db1，存储数据范围(ID取值)：[1000w ~ 2500w)，分片算法：id%9=3,4,5';
create table if not exists t_user_2(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db1，存储数据范围(ID取值)：[2500w ~ 4000w)，分片算法：id%9=3,4,5';
insert into `userdb_g0db1`.`t_user_0` (`id`, `name`) values (201,'g0db1-t0-jack201');
insert into `userdb_g0db1`.`t_user_1` (`id`, `name`) values (10000021,'g0db1-t1-jack10000021');
insert into `userdb_g0db1`.`t_user_2` (`id`, `name`) values (25000025,'g0db1-t2-jack25000025');

-- Sharding Group0(db2)
drop schema if exists userdb_g0db2;
create schema if not exists userdb_g0db2;
use userdb_g0db2;
drop table if exists t_user_0;
drop table if exists t_user_1;
drop table if exists t_user_2;
create table if not exists t_user_0(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db2，存储数据范围(ID取值)：[0 ~ 1000w)，分片算法：id%9=6,7,8';
create table if not exists t_user_1(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db2，存储数据范围(ID取值)：[1000w ~ 2500w)，分片算法：id%9=6,7,8';
create table if not exists t_user_2(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db2，存储数据范围(ID取值)：[2500w ~ 4000w)，分片算法：id%9=6,7,8';
insert into `userdb_g0db2`.`t_user_0` (`id`, `name`) values (250,'g0db2-t0-jack250');
insert into `userdb_g0db2`.`t_user_1` (`id`, `name`) values (10000034,'g0db2-t1-jack10000034');
insert into `userdb_g0db2`.`t_user_2` (`id`, `name`) values (25000037,'g0db2-t2-jack25000037');

-- Sharding Group1(db0)
drop schema if exists userdb_g1db0;
create schema if not exists userdb_g1db0;
use userdb_g1db0;
drop table if exists t_user_0;
drop table if exists t_user_1;
drop table if exists t_user_2;
drop table if exists t_user_3;
create table if not exists t_user_0(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db0，存储数据范围(ID取值)：[0 ~ 1000w)，分片算法：id%10=0,1,2,3';
create table if not exists t_user_1(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db0，存储数据范围(ID取值)：[1000w ~ 2000w)，分片算法：id%10=0,1,2,3';
create table if not exists t_user_2(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db0，存储数据范围(ID取值)：[2000w ~ 3000w)，分片算法：id%10=0,1,2,3';
create table if not exists t_user_3(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db0，存储数据范围(ID取值)：[3000w ~ 4000w)，分片算法：id%10=0,1,2,3';
insert into `userdb_g1db0`.`t_user_0` (`id`, `name`) values (100,'g1db0-t0-jack100');
insert into `userdb_g1db0`.`t_user_1` (`id`, `name`) values (10000001,'g1db0-t1-jack10000001');
insert into `userdb_g1db0`.`t_user_2` (`id`, `name`) values (20000001,'g1db0-t2-jack20000001');
insert into `userdb_g1db0`.`t_user_3` (`id`, `name`) values (30000001,'g1db0-t3-jack30000001');

-- Sharding Group1(db1)
drop schema if exists userdb_g1db1;
create schema if not exists userdb_g1db1;
use userdb_g1db1;
drop table if exists t_user_0;
drop table if exists t_user_1;
drop table if exists t_user_2;
create table if not exists t_user_0(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db1，存储数据范围(ID取值)：[0 ~ 1000w)，分片算法：id%10=4,5,6';
create table if not exists t_user_1(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db1，存储数据范围(ID取值)：[1000w ~ 2500w)，分片算法：id%10=4,5,6';
create table if not exists t_user_2(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db1，存储数据范围(ID取值)：[2500w ~ 4000w)，分片算法：id%10=4,5,6';
insert into `userdb_g1db1`.`t_user_0` (`id`, `name`) values (104,'g1db1-t0-jack104');
insert into `userdb_g1db1`.`t_user_1` (`id`, `name`) values (10000005,'g1db1-t1-jack10000005');
insert into `userdb_g1db1`.`t_user_2` (`id`, `name`) values (25000006,'g1db1-t2-jack25000006');

-- Sharding Group1(db2)
drop schema if exists userdb_g1db2;
create schema if not exists userdb_g1db2;
use userdb_g1db2;
drop table if exists t_user_0;
drop table if exists t_user_1;
drop table if exists t_user_2;
create table if not exists t_user_0(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db2，存储数据范围(ID取值)：[0 ~ 1000w)，分片算法：id%10=7,8,9';
create table if not exists t_user_1(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db2，存储数据范围(ID取值)：[1000w ~ 2500w)，分片算法：id%10=7,8,9';
create table if not exists t_user_2(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db2，存储数据范围(ID取值)：[2500w ~ 4000w)，分片算法：id%10=7,8,9';
insert into `userdb_g1db2`.`t_user_0` (`id`, `name`) values (107,'g1db2-t0-jack107');
insert into `userdb_g1db2`.`t_user_1` (`id`, `name`) values (10000008,'g1db2-t1-jack10000008');
insert into `userdb_g1db2`.`t_user_2` (`id`, `name`) values (25000009,'g1db2-t2-jack25000009');
