create table shop
(
  id    int auto_increment primary key,
  name  varchar(255),
  owner varchar(255),
);

create table user
(
  id int auto_increment primary key,
  name varchar(255),
  sex  varchar(6),
  age  int
);

create table user_detail
(
  id int auto_increment primary key,
  name varchar(255),
  sex  varchar(6),
  age  int,
  address varchar(255),
  description varchar(255)
);

create table user_shop
(
  id int auto_increment primary key,
  dt char(10),
  shop_id int,
  user_id int,
  amount double
)

