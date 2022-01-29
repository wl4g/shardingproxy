-- MGR (MySQL group replication) cluster 0
drop schema if exists paymentdb_r0z0mgr0db0; -- region0/zone0/mgrCluster0/database0 like to: cn_south1_a1_mgr0_paymentdb0
drop schema if exists paymentdb_r0z0mgr0db1;
-- MGR (MySQL group replication) cluster 1
drop schema if exists paymentdb_r0z0mgr1db0;
drop schema if exists paymentdb_r0z0mgr1db1;
drop schema if exists paymentdb_r0z0mgr1db2;
drop schema if exists paymentdb_r0z0mgr1db3;
-- Small database that do not participate in sharding.
drop schema if exists paymentdb_r0z0mgr2_single;

-- --------------------------------- Sharding of MGR cluster 0 (matrix: 2db x 4table) --------------------------------------

-- mgr0.db0
drop schema if exists paymentdb_r0z0mgr0db0;
create schema if not exists paymentdb_r0z0mgr0db0;
use paymentdb_r0z0mgr0db0;
drop table if exists t_bill_0;
drop table if exists t_bill_1;
drop table if exists t_bill_2;
drop table if exists t_bill_3;
create table if not exists t_bill_0(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属分片组：group0，所属物理库：mgr0.db0，ID取值范围：[0~1000w)，库定位算法：id%8=0,1,2,3';
create table if not exists t_bill_1(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属分片组：group0，所属物理库：mgr0.db0，ID取值范围：[1000w~2000w)，库定位算法：id%8=0,1,2,3';
create table if not exists t_bill_2(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属分片组：group0，所属物理库：mgr0.db0，ID取值范围：[2000w~3000w)，库定位算法：id%8=0,1,2,3';
create table if not exists t_bill_3(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属分片组：group0，所属物理库：mgr0.db0，ID取值范围：[3000w~4000w)，库定位算法：id%8=0,1,2,3';
insert into `t_bill_0` (`id`, `name`) values (107,'skirt-107 from r0z0mgr0db0.t0');
insert into `t_bill_1` (`id`, `name`) values (10000009,'skirt-10000009 from r0z0mgr0db0.t1');
insert into `t_bill_2` (`id`, `name`) values (27000018,'skirt-27000018 from r0z0mgr0db0.t2');
insert into `t_bill_3` (`id`, `name`) values (37000019,'skirt-37000019 from r0z0mgr0db0.t3');

-- mgr0.db1
drop schema if exists paymentdb_r0z0mgr0db1;
create schema if not exists paymentdb_r0z0mgr0db1;
use paymentdb_r0z0mgr0db1;
drop table if exists t_bill_0;
drop table if exists t_bill_1;
drop table if exists t_bill_2;
drop table if exists t_bill_3;
create table if not exists t_bill_0(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属分片组：group0，所属物理库：mgr0.db1，ID取值范围：[0~1000w)，库定位算法：id%8=4,5,6,7';
create table if not exists t_bill_1(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属分片组：group0，所属物理库：mgr0.db1，ID取值范围：[1000w~2000w)，库定位算法：id%8=4,5,6,7';
create table if not exists t_bill_2(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属分片组：group0，所属物理库：mgr0.db1，ID取值范围：[2000w~3000w)，库定位算法：id%8=4,5,6,7';
create table if not exists t_bill_3(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属分片组：group0，所属物理库：mgr0.db1，ID取值范围：[3000w~4000w)，库定位算法：id%8=4,5,6,7';
insert into `t_bill_0` (`id`, `name`) values (205,'skirt-205 from r0z0mgr0.t0');
insert into `t_bill_1` (`id`, `name`) values (10000029,'skirt-10000029 from r0z0mgr0db1.t1');
insert into `t_bill_2` (`id`, `name`) values (25000029,'skirt-25000029 from r0z0mgr0db1.t2');
insert into `t_bill_3` (`id`, `name`) values (35000029,'skirt-35000029 from r0z0mgr0db1.t3');

-- --------------------------------- Non Sharding single database --------------------------------------

create schema if not exists paymentdb_r0z0mgr2_single;
