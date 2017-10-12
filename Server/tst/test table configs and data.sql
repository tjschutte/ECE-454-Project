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
    
    PRIMARY KEY(humonid)
);

CREATE TABLE playerhumons (
	instanceid int auto_increment,
    userid int not null, # Player who 'owns' this humon
    humonid int not null,
    
    primary key (instanceid), # unique instance id uid-hid-idk
    foreign key (humonid) REFERENCES humons(humonid)
);

Insert into users (username, passwordhash, party, encountered_humons) values ('Tom', 'tompasshash', '[{"name":"HumonName1","image":null,"hID":"hID1","uID":"uID1","iID":"iID1","hp":"hp1","moves":[{"name":"Punch","dmg":"10"},{"name":"Kick","dmg":"15"},{"name":"Slap","dmg":"2"},{"name":"Whimper","dmg":"1"}]},{"name":"HumonName2","image":null,"hID":"hID2","uID":"uID2","iID":"iID2","hp":"hp2","moves":[{"name":"Punch","dmg":"10"},{"name":"Kick","dmg":"15"},{"name":"Slap","dmg":"2"},{"name":"Whimper","dmg":"1"}]}]', null);
Insert into users (username, passwordhash, party, encountered_humons) values ("Joe", "Joepasshash", null, null);
Insert into users (username, passwordhash, party, encountered_humons) values ("Michael", "Michaelpasshash", null, null);