-- MGR (MySQL group replication) cluster 0
drop schema if exists orderdb_r0z0mgr0db0; -- region0/zone0/mgrCluster0/database0 like to: cn_south1_a1_mgr0_orderdb0
drop schema if exists orderdb_r0z0mgr0db1;
-- MGR (MySQL group replication) cluster 1
drop schema if exists orderdb_r0z0mgr1db0;
drop schema if exists orderdb_r0z0mgr1db1;
drop schema if exists orderdb_r0z0mgr1db2;
drop schema if exists orderdb_r0z0mgr1db3;
-- Small database that do not participate in sharding.
drop schema if exists orderdb_r0z0mgr2_single;

-- --------------------------------- Sharding Group 0 (of MGR cluster 0, matrix: 2db x 4table) --------------------------------------

-- group0.db0
drop schema if exists orderdb_r0z0mgr0db0;
create schema if not exists orderdb_r0z0mgr0db0;
use orderdb_r0z0mgr0db0;
drop table if exists t_order_0;
drop table if exists t_order_1;
drop table if exists t_order_2;
drop table if exists t_order_3;
create table if not exists t_order_0(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group0，所属物理库：mgr0.db0，ID取值范围：[0~1000w)，库定位算法：id%8=0,1,2,3';
create table if not exists t_order_1(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group0，所属物理库：mgr0.db0，ID取值范围：[1000w~2000w)，库定位算法：id%8=0,1,2,3';
create table if not exists t_order_2(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group0，所属物理库：mgr0.db0，ID取值范围：[2000w~3000w)，库定位算法：id%8=0,1,2,3';
create table if not exists t_order_3(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group0，所属物理库：mgr0.db0，ID取值范围：[3000w~4000w)，库定位算法：id%8=0,1,2,3';
insert into `t_order_0` (`id`, `name`) values (107,'skirt-107 from r0z0mgr0db0.t0');
insert into `t_order_1` (`id`, `name`) values (10000009,'skirt-10000009 from r0z0mgr0db0.t1');
insert into `t_order_2` (`id`, `name`) values (27000018,'skirt-27000018 from r0z0mgr0db0.t2');
insert into `t_order_3` (`id`, `name`) values (37000019,'skirt-37000019 from r0z0mgr0db0.t3');

-- group0.db1
drop schema if exists orderdb_r0z0mgr0db1;
create schema if not exists orderdb_r0z0mgr0db1;
use orderdb_r0z0mgr0db1;
drop table if exists t_order_0;
drop table if exists t_order_1;
drop table if exists t_order_2;
drop table if exists t_order_3;
create table if not exists t_order_0(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group0，所属物理库：mgr0.db1，ID取值范围：[0~1000w)，库定位算法：id%8=4,5,6,7';
create table if not exists t_order_1(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group0，所属物理库：mgr0.db1，ID取值范围：[1000w~2000w)，库定位算法：id%8=4,5,6,7';
create table if not exists t_order_2(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group0，所属物理库：mgr0.db1，ID取值范围：[2000w~3000w)，库定位算法：id%8=4,5,6,7';
create table if not exists t_order_3(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group0，所属物理库：mgr0.db1，ID取值范围：[3000w~4000w)，库定位算法：id%8=4,5,6,7';
insert into `t_order_0` (`id`, `name`) values (205,'skirt-205 from r0z0mgr0.t0');
insert into `t_order_1` (`id`, `name`) values (10000029,'skirt-10000029 from r0z0mgr0db1.t1');
insert into `t_order_2` (`id`, `name`) values (25000029,'skirt-25000029 from r0z0mgr0db1.t2');
insert into `t_order_3` (`id`, `name`) values (35000029,'skirt-35000029 from r0z0mgr0db1.t3');

-- --------------------------------- Sharding Group 1 (of MGR cluster 1, matrix: 4db x 8table) --------------------------------------

-- group1.db0
drop schema if exists orderdb_r0z0mgr1db0;
create schema if not exists orderdb_r0z0mgr1db0;
use orderdb_r0z0mgr1db0;
drop table if exists t_order_0;
drop table if exists t_order_1;
drop table if exists t_order_2;
drop table if exists t_order_3;
drop table if exists t_order_4;
drop table if exists t_order_5;
drop table if exists t_order_6;
drop table if exists t_order_7;
create table if not exists t_order_0(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db0，ID取值范围：[4000w~6000w)，库定位算法：id%32=0,1,2,3,4,5,6,7';
create table if not exists t_order_1(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db0，ID取值范围：[6000w~8000w)，库定位算法：id%32=0,1,2,3,4,5,6,7';
create table if not exists t_order_2(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db0，ID取值范围：[8000w~10000w)，库定位算法：id%32=0,1,2,3,4,5,6,7';
create table if not exists t_order_3(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db0，ID取值范围：[10000w~12000w)，库定位算法：id%32=0,1,2,3,4,5,6,7';
create table if not exists t_order_4(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db0，ID取值范围：[12000w~14000w)，库定位算法：id%32=0,1,2,3,4,5,6,7';
create table if not exists t_order_5(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db0，ID取值范围：[14000w~16000w)，库定位算法：id%32=0,1,2,3,4,5,6,7';
create table if not exists t_order_6(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db0，ID取值范围：[16000w~18000w)，库定位算法：id%32=0,1,2,3,4,5,6,7';
create table if not exists t_order_7(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db0，ID取值范围：[18000w~20000w)，库定位算法：id%32=0,1,2,3,4,5,6,7';
insert into `t_order_0` (`id`, `name`) values (40000000,'skirt-40000000 from r0z0mgr1db0.t0');
insert into `t_order_1` (`id`, `name`) values (60000002,'skirt-60000002 from r0z0mgr1db0.t1');
insert into `t_order_2` (`id`, `name`) values (80000003,'skirt-80000003 from r0z0mgr1db0.t2');
insert into `t_order_3` (`id`, `name`) values (100000003,'skirt-100000003 from r0z0mgr1db0.t3');
insert into `t_order_4` (`id`, `name`) values (120000003,'skirt-120000003 from r0z0mgr1db0.t3');
insert into `t_order_5` (`id`, `name`) values (140000003,'skirt-140000003 from r0z0mgr1db0.t3');
insert into `t_order_6` (`id`, `name`) values (160000003,'skirt-160000003 from r0z0mgr1db0.t3');
insert into `t_order_7` (`id`, `name`) values (180000003,'skirt-180000003 from r0z0mgr1db0.t3');

-- group1.db1
drop schema if exists orderdb_r0z0mgr1db1;
create schema if not exists orderdb_r0z0mgr1db1;
use orderdb_r0z0mgr1db1;
drop table if exists t_order_0;
drop table if exists t_order_1;
drop table if exists t_order_2;
drop table if exists t_order_3;
drop table if exists t_order_4;
drop table if exists t_order_5;
drop table if exists t_order_6;
drop table if exists t_order_7;
create table if not exists t_order_0(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db1，ID取值范围：[4000w~6000w)，库定位算法：iid%32=8,9,10,11,12,13,14,15';
create table if not exists t_order_1(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db1，ID取值范围：[6000w~8000w)，库定位算法：iid%32=8,9,10,11,12,13,14,15';
create table if not exists t_order_2(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db1，ID取值范围：[8000w~10000w)，库定位算法：id%32=8,9,10,11,12,13,14,15';
create table if not exists t_order_3(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db1，ID取值范围：[10000w~12000w)，库定位算法：id%32=8,9,10,11,12,13,14,15';
create table if not exists t_order_4(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db1，ID取值范围：[12000w~14000w)，库定位算法：id%32=8,9,10,11,12,13,14,15';
create table if not exists t_order_5(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db1，ID取值范围：[14000w~16000w)，库定位算法：id%32=8,9,10,11,12,13,14,15';
create table if not exists t_order_6(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db1，ID取值范围：[16000w~18000w)，库定位算法：id%32=8,9,10,11,12,13,14,15';
create table if not exists t_order_7(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db1，ID取值范围：[18000w~20000w)，库定位算法：id%32=8,9,10,11,12,13,14,15';
insert into `t_order_0` (`id`, `name`) values (40000011,'skirt-40000011 from r0z0mgr1db1.t0');
insert into `t_order_1` (`id`, `name`) values (60000009,'skirt-60000009 from r0z0mgr1db1.t1');
insert into `t_order_2` (`id`, `name`) values (80000009,'skirt-80000009 from r0z0mgr1db1.t2');
insert into `t_order_3` (`id`, `name`) values (100000009,'skirt-100000009 from r0z0mgr1db1.t2');
insert into `t_order_4` (`id`, `name`) values (120000009,'skirt-120000009 from r0z0mgr1db1.t2');
insert into `t_order_5` (`id`, `name`) values (140000009,'skirt-140000009 from r0z0mgr1db1.t2');
insert into `t_order_6` (`id`, `name`) values (160000009,'skirt-160000009 from r0z0mgr1db1.t2');
insert into `t_order_7` (`id`, `name`) values (180000009,'skirt-180000009 from r0z0mgr1db1.t2');

-- group1.db2
drop schema if exists orderdb_r0z0mgr1db2;
create schema if not exists orderdb_r0z0mgr1db2;
use orderdb_r0z0mgr1db2;
drop table if exists t_order_0;
drop table if exists t_order_1;
drop table if exists t_order_2;
drop table if exists t_order_3;
drop table if exists t_order_4;
drop table if exists t_order_5;
drop table if exists t_order_6;
drop table if exists t_order_7;
create table if not exists t_order_0(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db2，ID取值范围：[4000w~6000w)，库定位算法：id%32=16,17,18,19,20,21,22,23';
create table if not exists t_order_1(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db2，ID取值范围：[6000w~8000w)，库定位算法：id%32=16,17,18,19,20,21,22,23';
create table if not exists t_order_2(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db2，ID取值范围：[8000w~10000w)，库定位算法：id%32=16,17,18,19,20,21,22,23';
create table if not exists t_order_3(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db2，ID取值范围：[10000w~12000w)，库定位算法：id%32=16,17,18,19,20,21,22,23';
create table if not exists t_order_4(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db2，ID取值范围：[12000w~14000w)，库定位算法：id%32=16,17,18,19,20,21,22,23';
create table if not exists t_order_5(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db2，ID取值范围：[14000w~16000w)，库定位算法：id%32=16,17,18,19,20,21,22,23';
create table if not exists t_order_6(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db2，ID取值范围：[16000w~18000w)，库定位算法：id%32=16,17,18,19,20,21,22,23';
create table if not exists t_order_7(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db2，ID取值范围：[18000w~20000w)，库定位算法：id%32=16,17,18,19,20,21,22,23';
insert into `t_order_0` (`id`, `name`) values (40000016,'skirt-40000016 from r0z0mgr1db2.t0');
insert into `t_order_1` (`id`, `name`) values (60000016,'skirt-60000016 from r0z0mgr1db2.t1');
insert into `t_order_2` (`id`, `name`) values (80000016,'skirt-80000016 from r0z0mgr1db2.t2');
insert into `t_order_3` (`id`, `name`) values (100000016,'skirt-100000016 from r0z0mgr1db2.t2');
insert into `t_order_4` (`id`, `name`) values (120000016,'skirt-120000016 from r0z0mgr1db2.t2');
insert into `t_order_5` (`id`, `name`) values (140000016,'skirt-140000016 from r0z0mgr1db2.t2');
insert into `t_order_6` (`id`, `name`) values (160000016,'skirt-160000016 from r0z0mgr1db2.t2');
insert into `t_order_7` (`id`, `name`) values (180000016,'skirt-180000016 from r0z0mgr1db2.t2');

-- group1.db3
drop schema if exists orderdb_r0z0mgr1db3;
create schema if not exists orderdb_r0z0mgr1db3;
use orderdb_r0z0mgr1db3;
drop table if exists t_order_0;
drop table if exists t_order_1;
drop table if exists t_order_2;
drop table if exists t_order_3;
drop table if exists t_order_4;
drop table if exists t_order_5;
drop table if exists t_order_6;
drop table if exists t_order_7;
create table if not exists t_order_0(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db2，ID取值范围：[4000w~6000w)，库定位算法：id%32=24,25,26,27,28,29,20,31';
create table if not exists t_order_1(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db2，ID取值范围：[6000w~8000w)，库定位算法：id%32=24,25,26,27,28,29,20,31';
create table if not exists t_order_2(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db2，ID取值范围：[8000w~10000w)，库定位算法：id%32=24,25,26,27,28,29,20,31';
create table if not exists t_order_3(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db2，ID取值范围：[10000w~12000w)，库定位算法：id%32=24,25,26,27,28,29,20,31';
create table if not exists t_order_4(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db2，ID取值范围：[12000w~14000w)，库定位算法：id%32=24,25,26,27,28,29,20,31';
create table if not exists t_order_5(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db2，ID取值范围：[14000w~16000w)，库定位算法：id%32=24,25,26,27,28,29,20,31';
create table if not exists t_order_6(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db2，ID取值范围：[16000w~18000w)，库定位算法：id%32=24,25,26,27,28,29,20,31';
create table if not exists t_order_7(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属分片组：group1，所属物理库：mgr1.db2，ID取值范围：[18000w~20000w)，库定位算法：id%32=24,25,26,27,28,29,20,31';
insert into `t_order_0` (`id`, `name`) values (40000025,'skirt-40000007 from r0z0mgr1db3.t0');
insert into `t_order_1` (`id`, `name`) values (60000025,'skirt-60000025 from r0z0mgr1db3.t1');
insert into `t_order_2` (`id`, `name`) values (80000025,'skirt-80000025 from r0z0mgr1db3.t2');
insert into `t_order_3` (`id`, `name`) values (100000025,'skirt-100000025 from r0z0mgr1db3.t2');
insert into `t_order_4` (`id`, `name`) values (120000025,'skirt-120000025 from r0z0mgr1db3.t2');
insert into `t_order_5` (`id`, `name`) values (140000025,'skirt-140000025 from r0z0mgr1db3.t2');
insert into `t_order_6` (`id`, `name`) values (160000025,'skirt-160000025 from r0z0mgr1db3.t2');
insert into `t_order_7` (`id`, `name`) values (180000025,'skirt-180000025 from r0z0mgr1db3.t2');

-- --------------------------------- Non Sharding single database --------------------------------------

create schema if not exists orderdb_r0z0mgr2_single;
