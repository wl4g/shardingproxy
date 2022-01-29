-- MGR (MySQL group replication) cluster 0
drop schema if exists paymentdb_r0z0mgr0db0; -- region0/zone0/mgrCluster0/database0 like to: cn_south1_a1_mgr0_paymentdb0
drop schema if exists paymentdb_r0z0mgr0db1;
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
create table if not exists t_bill_0(id bigint not null, order_id bigint(20) NOT NULL, `lables` varchar(64) character set utf8 collate utf8_bin, create_time datetime NOT NULL, update_time datetime NOT NULL, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属物理库：mgr0.db0';
create table if not exists t_bill_1(id bigint not null, order_id bigint(20) NOT NULL, `lables` varchar(64) character set utf8 collate utf8_bin, create_time datetime NOT NULL, update_time datetime NOT NULL, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属物理库：mgr0.db0';
create table if not exists t_bill_2(id bigint not null, order_id bigint(20) NOT NULL, `lables` varchar(64) character set utf8 collate utf8_bin, create_time datetime NOT NULL, update_time datetime NOT NULL, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属物理库：mgr0.db0';
create table if not exists t_bill_3(id bigint not null, order_id bigint(20) NOT NULL, `lables` varchar(64) character set utf8 collate utf8_bin, create_time datetime NOT NULL, update_time datetime NOT NULL, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属物理库：mgr0.db0';
insert into `t_bill_0` (`id`, `order_id`, `lables`, `create_time`, `update_time`) values (107, 10000000, 'label-107 from r0z0mgr0db0.t0', '2020-02-05 10:00:00', '2020-02-05 10:00:00');
insert into `t_bill_1` (`id`, `order_id`, `lables`, `create_time`, `update_time`) values (10000009, 10000001, 'label-10000009 from r0z0mgr0db0.t1', '2020-03-05 10:00:00', '2020-03-05 10:00:00');
insert into `t_bill_2` (`id`, `order_id`, `lables`, `create_time`, `update_time`) values (27000018, 10000002, 'label-27000018 from r0z0mgr0db0.t2', '2020-04-05 10:00:00', '2020-04-05 10:00:00');
insert into `t_bill_3` (`id`, `order_id`, `lables`, `create_time`, `update_time`) values (37000019, 10000003, 'label-37000019 from r0z0mgr0db0.t3', '2021-09-05 10:00:00', '2021-09-05 10:00:00');
-- ......
-- ......
insert into `t_bill_2192` (`id`, `order_id`, `lables`, `create_time`, `update_time`) values (37000019, 10000003, 'label-37000019 from r0z0mgr0db0.t2192', '2021-09-05 10:00:00', '2021-09-05 10:00:00');

-- mgr0.db1
drop schema if exists paymentdb_r0z0mgr0db1;
create schema if not exists paymentdb_r0z0mgr0db1;
use paymentdb_r0z0mgr0db1;
drop table if exists t_bill_0;
drop table if exists t_bill_1;
drop table if exists t_bill_2;
drop table if exists t_bill_3;
create table if not exists t_bill_0(id bigint not null, order_id bigint(20) NOT NULL, `lables` varchar(64) character set utf8 collate utf8_bin, create_time datetime NOT NULL, update_time datetime NOT NULL, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属物理库：mgr0.db1';
create table if not exists t_bill_1(id bigint not null, order_id bigint(20) NOT NULL, `lables` varchar(64) character set utf8 collate utf8_bin, create_time datetime NOT NULL, update_time datetime NOT NULL, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属物理库：mgr0.db1';
create table if not exists t_bill_2(id bigint not null, order_id bigint(20) NOT NULL, `lables` varchar(64) character set utf8 collate utf8_bin, create_time datetime NOT NULL, update_time datetime NOT NULL, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属物理库：mgr0.db1';
create table if not exists t_bill_3(id bigint not null, order_id bigint(20) NOT NULL, `lables` varchar(64) character set utf8 collate utf8_bin, create_time datetime NOT NULL, update_time datetime NOT NULL, primary key(id))engine=innodb comment='所属虚拟库：paymentdb，所属物理库：mgr0.db1';
insert into `t_bill_0` (`id`, `order_id`, `lables`, `create_time`, `update_time`) values (205, 10000004, 'label-205 from r0z0mgr0.t0', '2021-10-05 10:00:00', '2021-10-05 10:00:00');
insert into `t_bill_1` (`id`, `order_id`, `lables`, `create_time`, `update_time`) values (10000029, 10000005, 'label-10000029 from r0z0mgr0db1.t1', '2021-02-05 10:00:00', '2021-02-05 10:00:00');
insert into `t_bill_2` (`id`, `order_id`, `lables`, `create_time`, `update_time`) values (25000029, 10000006, 'label-25000029 from r0z0mgr0db1.t2', '2022-02-05 09:00:00', '2022-02-05 09:00:00');
insert into `t_bill_3` (`id`, `order_id`, `lables`, `create_time`, `update_time`) values (35000029, 10000007, 'label-35000029 from r0z0mgr0db1.t3', '2022-02-05 10:00:00', '2022-02-05 10:00:00');
-- ......
-- ......
insert into `t_bill_2192` (`id`, `order_id`, `lables`, `create_time`, `update_time`) values (37000019, 10000003, 'label-37000019 from r0z0mgr0db0.t2192', '2022-07-05 10:00:00', '2021-09-05 10:00:00');

-- --------------------------------- Non Sharding single database --------------------------------------

create schema if not exists paymentdb_r0z0mgr2_single;
