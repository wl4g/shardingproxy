-- MGR (MySQL group replication) cluster 0
drop schema if exists paymentdb_r0z0mgr0db0; -- region0/zone0/mgrCluster0/database0 like to: cn_south1_a1_mgr0_paymentdb0
drop schema if exists paymentdb_r0z0mgr0db1;
-- MGR (MySQL group replication) cluster 1
drop schema if exists paymentdb_r0z0mgr1db0;
drop schema if exists paymentdb_r0z0mgr1db1;
-- Small database that do not participate in sharding.
drop schema if exists paymentdb_r0z0mgr2_single;

-- --------------------------------- Sharding of MGR cluster 0 --------------------------------------

-- mgr0.db0
drop schema if exists paymentdb_r0z0mgr0db0;
create schema if not exists paymentdb_r0z0mgr0db0;
use paymentdb_r0z0mgr0db0;
drop table if exists t_bill_0;
drop table if exists t_bill_1;
drop table if exists t_bill_2;
drop table if exists t_bill_3;
create table if not exists t_bill_0(id bigint not null, order_id bigint(20) NOT NULL, `lables` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属物理库：mgr0.db0';
create table if not exists t_bill_1(id bigint not null, order_id bigint(20) NOT NULL, `lables` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属物理库：mgr0.db0';
create table if not exists t_bill_2(id bigint not null, order_id bigint(20) NOT NULL, `lables` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属物理库：mgr0.db0';
create table if not exists t_bill_3(id bigint not null, order_id bigint(20) NOT NULL, `lables` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属物理库：mgr0.db0';
insert into `t_bill_0` (`id`, `order_id`, `lables`) values (107, 10000000, 'label-107 from r0z0mgr0db0.t0');
insert into `t_bill_1` (`id`, `order_id`, `lables`) values (10000009, 10000001, 'label-10000009 from r0z0mgr0db0.t1');
insert into `t_bill_2` (`id`, `order_id`, `lables`) values (27000018, 10000002, 'label-27000018 from r0z0mgr0db0.t2');
insert into `t_bill_3` (`id`, `order_id`, `lables`) values (37000019, 10000003, 'label-37000019 from r0z0mgr0db0.t3');

-- mgr0.db1
drop schema if exists paymentdb_r0z0mgr0db1;
create schema if not exists paymentdb_r0z0mgr0db1;
use paymentdb_r0z0mgr0db1;
drop table if exists t_bill_0;
drop table if exists t_bill_1;
drop table if exists t_bill_2;
drop table if exists t_bill_3;
create table if not exists t_bill_0(id bigint not null, order_id bigint(20) NOT NULL, `lables` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属物理库：mgr0.db1，ID取值范围：[0~1000w)，库定位算法：id%8=4,5,6,7';
create table if not exists t_bill_1(id bigint not null, order_id bigint(20) NOT NULL, `lables` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属物理库：mgr0.db1，ID取值范围：[1000w~2000w)，库定位算法：id%8=4,5,6,7';
create table if not exists t_bill_2(id bigint not null, order_id bigint(20) NOT NULL, `lables` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属物理库：mgr0.db1，ID取值范围：[2000w~3000w)，库定位算法：id%8=4,5,6,7';
create table if not exists t_bill_3(id bigint not null, order_id bigint(20) NOT NULL, `lables` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属物理库：mgr0.db1，ID取值范围：[3000w~4000w)，库定位算法：id%8=4,5,6,7';
insert into `t_bill_0` (`id`, `order_id`, `lables`) values (205, 10000004, 'label-205 from r0z0mgr0.t0');
insert into `t_bill_1` (`id`, `order_id`, `lables`) values (10000029, 10000005, 'label-10000029 from r0z0mgr0db1.t1');
insert into `t_bill_2` (`id`, `order_id`, `lables`) values (25000029, 10000006, 'label-25000029 from r0z0mgr0db1.t2');
insert into `t_bill_3` (`id`, `order_id`, `lables`) values (35000029, 10000007, 'label-35000029 from r0z0mgr0db1.t3');

-- --------------------------------- Non Sharding single database --------------------------------------

create schema if not exists paymentdb_r0z0mgr2_single;
