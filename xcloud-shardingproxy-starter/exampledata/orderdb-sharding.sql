-- MGR (MySQL group replication) cluster 0
drop schema if exists orderdb_db0;
drop schema if exists orderdb_db1;
-- MGR (MySQL group replication) cluster 1
drop schema if exists orderdb_db2;
drop schema if exists orderdb_db3;
-- Small database that do not participate in sharding.
drop schema if exists orderdb_single;

-- --------------------------------- Sharding of MGR cluster 0, matrix: 4db x 4table) --------------------------------------

-- db0
drop schema if exists orderdb_db0;
create schema if not exists orderdb_db0;
use orderdb_db0;
drop table if exists t_order_0;
drop table if exists t_order_1;
drop table if exists t_order_2;
drop table if exists t_order_3;
create table if not exists t_order_0(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属物理库：db0，ID取值范围：[0~2000w)，库定位算法：id%4=0';
create table if not exists t_order_1(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属物理库：db0，ID取值范围：[2000w~4000w)，库定位算法：id%4=0';
create table if not exists t_order_2(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属物理库：db0，ID取值范围：[4000w~6000w)，库定位算法：id%4=0';
create table if not exists t_order_3(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属物理库：db0，ID取值范围：[6000w~8000w)，库定位算法：id%4=0';
insert into `t_order_0` (`id`, `name`) values (100,'skirt-100 from db0.t0');
insert into `t_order_1` (`id`, `name`) values (20000100,'skirt-20000100 from db0.t1');
insert into `t_order_2` (`id`, `name`) values (40000100,'skirt-40000100 from db0.t2');
insert into `t_order_3` (`id`, `name`) values (60000100,'skirt-60000100 from db0.t3');

-- db1
drop schema if exists orderdb_db1;
create schema if not exists orderdb_db1;
use orderdb_db1;
drop table if exists t_order_0;
drop table if exists t_order_1;
drop table if exists t_order_2;
drop table if exists t_order_3;
create table if not exists t_order_0(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属物理库：db1，ID取值范围：[0~1000w)，库定位算法：id%4=1';
create table if not exists t_order_1(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属物理库：db1，ID取值范围：[1000w~4000w)，库定位算法：id%4=1';
create table if not exists t_order_2(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属物理库：db1，ID取值范围：[4000w~6000w)，库定位算法：id%4=1';
create table if not exists t_order_3(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属物理库：db1，ID取值范围：[6000w~8000w)，库定位算法：id%4=1';
insert into `t_order_0` (`id`, `name`) values (101,'skirt-101 from r0z0t0');
insert into `t_order_1` (`id`, `name`) values (20000101,'skirt-20000101 from db1.t1');
insert into `t_order_2` (`id`, `name`) values (40000101,'skirt-40000101 from db1.t2');
insert into `t_order_3` (`id`, `name`) values (60000101,'skirt-60000101 from db1.t3');

-- db2
drop schema if exists orderdb_db2;
create schema if not exists orderdb_db2;
use orderdb_db2;
drop table if exists t_order_0;
drop table if exists t_order_1;
drop table if exists t_order_2;
drop table if exists t_order_3;
create table if not exists t_order_0(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属物理库：db2，ID取值范围：[0~1000w)，库定位算法：id%4=2';
create table if not exists t_order_1(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属物理库：db2，ID取值范围：[1000w~4000w)，库定位算法：id%4=2';
create table if not exists t_order_2(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属物理库：db2，ID取值范围：[4000w~6000w)，库定位算法：id%4=2';
create table if not exists t_order_3(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属物理库：db2，ID取值范围：[6000w~8000w)，库定位算法：id%4=2';
insert into `t_order_0` (`id`, `name`) values (202,'skirt-202 from r0z0t0');
insert into `t_order_1` (`id`, `name`) values (20000102,'skirt-20000102 from db1.t1');
insert into `t_order_2` (`id`, `name`) values (40000102,'skirt-40000102 from db1.t2');
insert into `t_order_3` (`id`, `name`) values (60000102,'skirt-60000102 from db1.t3');

-- db3
drop schema if exists orderdb_db3;
create schema if not exists orderdb_db3;
use orderdb_db3;
drop table if exists t_order_0;
drop table if exists t_order_1;
drop table if exists t_order_2;
drop table if exists t_order_3;
create table if not exists t_order_0(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属物理库：db3，ID取值范围：[0~1000w)，库定位算法：id%4=3';
create table if not exists t_order_1(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属物理库：db3，ID取值范围：[1000w~4000w)，库定位算法：id%4=3';
create table if not exists t_order_2(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属物理库：db3，ID取值范围：[4000w~6000w)，库定位算法：id%4=3';
create table if not exists t_order_3(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：orderdb，所属物理库：db3，ID取值范围：[6000w~8000w)，库定位算法：id%4=3';
insert into `t_order_0` (`id`, `name`) values (205,'skirt-205 from r0z0t0');
insert into `t_order_1` (`id`, `name`) values (20000103,'skirt-20000103 from db1.t1');
insert into `t_order_2` (`id`, `name`) values (40000103,'skirt-40000103 from db1.t2');
insert into `t_order_3` (`id`, `name`) values (60000103,'skirt-60000103 from db1.t3');

-- --------------------------------- Non Sharding single database --------------------------------------

create schema if not exists orderdb_single;
