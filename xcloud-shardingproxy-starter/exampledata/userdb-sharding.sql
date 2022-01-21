-- MGR (MySQL group replication) cluster 0
drop schema if exists userdb_db0;
drop schema if exists userdb_db1;
-- MGR (MySQL group replication) cluster 1
drop schema if exists userdb_db2;
drop schema if exists userdb_db3;
-- Small database that do not participate in sharding.
drop schema if exists userdb_single;

-- --------------------------------- Sharding of MGR cluster 0, matrix: 4db x 4table) --------------------------------------

-- db0
drop schema if exists userdb_db0;
create schema if not exists userdb_db0;
use userdb_db0;
drop table if exists t_user_0;
drop table if exists t_user_1;
drop table if exists t_user_2;
drop table if exists t_user_3;
create table if not exists t_user_0(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, `pwd` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：userdb，所属物理库：db0，ID取值范围：[0~2000w)，库定位算法：id%4=0';
create table if not exists t_user_1(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, `pwd` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：userdb，所属物理库：db0，ID取值范围：[2000w~4000w)，库定位算法：id%4=0';
create table if not exists t_user_2(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, `pwd` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：userdb，所属物理库：db0，ID取值范围：[4000w~6000w)，库定位算法：id%4=0';
create table if not exists t_user_3(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, `pwd` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：userdb，所属物理库：db0，ID取值范围：[6000w~8000w)，库定位算法：id%4=0';
insert into `t_user_0` (`id`, `name`, `pwd`) values (100,'jack-100 from db0.t0',null);
insert into `t_user_1` (`id`, `name`, `pwd`) values (20000100,'jack-20000100 from db0.t1',null);
insert into `t_user_2` (`id`, `name`, `pwd`) values (40000100,'jack-40000100 from db0.t2',null);
insert into `t_user_3` (`id`, `name`, `pwd`) values (60000100,'jack-60000100 from db0.t3',null);

-- db1
drop schema if exists userdb_db1;
create schema if not exists userdb_db1;
use userdb_db1;
drop table if exists t_user_0;
drop table if exists t_user_1;
drop table if exists t_user_2;
drop table if exists t_user_3;
create table if not exists t_user_0(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, `pwd` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：userdb，所属物理库：db1，ID取值范围：[0~1000w)，库定位算法：id%4=1';
create table if not exists t_user_1(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, `pwd` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：userdb，所属物理库：db1，ID取值范围：[1000w~4000w)，库定位算法：id%4=1';
create table if not exists t_user_2(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, `pwd` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：userdb，所属物理库：db1，ID取值范围：[4000w~6000w)，库定位算法：id%4=1';
create table if not exists t_user_3(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, `pwd` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：userdb，所属物理库：db1，ID取值范围：[6000w~8000w)，库定位算法：id%4=1';
insert into `t_user_0` (`id`, `name`, `pwd`) values (101,'jack-101 from r0z0t0',null);
insert into `t_user_1` (`id`, `name`, `pwd`) values (20000101,'jack-20000101 from db1.t1',null);
insert into `t_user_2` (`id`, `name`, `pwd`) values (40000101,'jack-40000101 from db1.t2',null);
insert into `t_user_3` (`id`, `name`, `pwd`) values (60000101,'jack-60000101 from db1.t3',null);

-- db2
drop schema if exists userdb_db2;
create schema if not exists userdb_db2;
use userdb_db2;
drop table if exists t_user_0;
drop table if exists t_user_1;
drop table if exists t_user_2;
drop table if exists t_user_3;
create table if not exists t_user_0(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, `pwd` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：userdb，所属物理库：db2，ID取值范围：[0~1000w)，库定位算法：id%4=2';
create table if not exists t_user_1(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, `pwd` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：userdb，所属物理库：db2，ID取值范围：[1000w~4000w)，库定位算法：id%4=2';
create table if not exists t_user_2(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, `pwd` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：userdb，所属物理库：db2，ID取值范围：[4000w~6000w)，库定位算法：id%4=2';
create table if not exists t_user_3(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, `pwd` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：userdb，所属物理库：db2，ID取值范围：[6000w~8000w)，库定位算法：id%4=2';
insert into `t_user_0` (`id`, `name`, `pwd`) values (202,'jack-202 from r0z0t0',null);
insert into `t_user_1` (`id`, `name`, `pwd`) values (20000102,'jack-20000102 from db1.t1',null);
insert into `t_user_2` (`id`, `name`, `pwd`) values (40000102,'jack-40000102 from db1.t2',null);
insert into `t_user_3` (`id`, `name`, `pwd`) values (60000102,'jack-60000102 from db1.t3',null);

-- db3
drop schema if exists userdb_db3;
create schema if not exists userdb_db3;
use userdb_db3;
drop table if exists t_user_0;
drop table if exists t_user_1;
drop table if exists t_user_2;
drop table if exists t_user_3;
create table if not exists t_user_0(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, `pwd` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：userdb，所属物理库：db3，ID取值范围：[0~1000w)，库定位算法：id%4=3';
create table if not exists t_user_1(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, `pwd` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：userdb，所属物理库：db3，ID取值范围：[1000w~4000w)，库定位算法：id%4=3';
create table if not exists t_user_2(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, `pwd` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：userdb，所属物理库：db3，ID取值范围：[4000w~6000w)，库定位算法：id%4=3';
create table if not exists t_user_3(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, `pwd` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：userdb，所属物理库：db3，ID取值范围：[6000w~8000w)，库定位算法：id%4=3';
insert into `t_user_0` (`id`, `name`, `pwd`) values (205,'jack-205 from r0z0t0',null);
insert into `t_user_1` (`id`, `name`, `pwd`) values (20000103,'jack-20000103 from db1.t1',null);
insert into `t_user_2` (`id`, `name`, `pwd`) values (40000103,'jack-40000103 from db1.t2',null);
insert into `t_user_3` (`id`, `name`, `pwd`) values (60000103,'jack-60000103 from db1.t3',null);

-- --------------------------------- Non Sharding single database --------------------------------------

create schema if not exists userdb_single;
