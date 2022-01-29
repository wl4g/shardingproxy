drop schema if exists warehousedb;
create schema if not exists warehousedb;
use warehousedb;
drop table if exists t_goods;
create table if not exists t_goods(id bigint not null, `name` varchar(64) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='所属虚拟库：warehousedb，所属分片组：group0，所属物理库：mgr0.db0，ID取值范围：[0~1000w)，库定位算法：id%8=0,1,2,3';
insert into `t_goods` (`id`, `name`) values (107,'skirt-101 from readwrite t_goods');
