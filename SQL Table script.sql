## User table, will store information about a user
## 
##
CREATE TABLE users (
    userid int auto_increment,
    username varchar(20) unique not null,
    passwordhash text not null,
    party blob, # list of instanceids
    encountered_humons blob, # list of humonids
    
    PRIMARY KEY (userid)
);

CREATE TABLE humons (
	humonid int auto_increment,
	image blob,

    PRIMARY KEY(humonid)
);

CREATE TABLE images (
	humonid int primary key auto_increment,
    image mediumblob#,
    #foreign key (humonid) REFERENCES humons(humonid)
);

CREATE TABLE playerhumons (
	instanceid int auto_increment,
    userid int not null, # Player who 'owns' this humon
    humonid int not null,
    
    primary key (instanceid), # unique instance id uid-hid-idk
    foreign key (humonid) REFERENCES humons(humonid)
);

Insert into users (username, passwordhash, party, encountered_humons) values ('Tom', 'tompasshash', '[1,2,3]', '[1,2,3,4,5,6]');