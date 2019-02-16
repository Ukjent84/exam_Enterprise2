 create sequence hibernate_sequence start with 1 increment by 1;

 create table flight_entity (
  id bigint not null, airline varchar(256),
  flight_number varchar(256),
  from_location varchar(256),
  price integer not null check (price<=20000),
  seats integer not null check (seats<=1000),
  to_location varchar(256),
   primary key (id)
    );
/*
If insecure about how to setup script:
go to yml file in testfolder and set
 hibernate:
      ddl-auto: validate
        to create-drop instead of validate
        and comment out flyway dependency +
          <scope>test</scope> under H2 dependency
         in pom folder then run application and you will see how hibernate sets it up*/