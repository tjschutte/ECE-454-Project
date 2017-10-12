# ECE-454-Project: Hu-mon
The Hú-mon app will include use of several Android sensors in order to provide a rich and diverse user experience. An integral part of the Hú-mon app will be the use of the camera.  The camera will be used by users to create new Hú-mons to battle and join their team.  Users will take a picture of themselves or of their friends.  This will then generate a Hú-mon with random stats based on the player's current  Hú-mon’s, or if they have none a low-level version of the Hú-mon will be generated.


Another feature of the Hú-mon app will be the use of location services.  As part of encouraging users to get up and be active, users will have to get up and move to be able to encounter new wild Hú-mon’s.  Via the map users can see locations that are marked as ‘wild’ where there will have a chance to encounter wild versions of Hú-mon’s they have already encountered.


In-order to protect user privacy, as well as limit the potential storage Hú-mon will use on device each player will have a local catalog of Hú-mons they have encountered.  This catalog will have locations to capture them, other Hú-mons that have not been encountered (Through taking a picture and creating the Hú-mon, or through battling friend) will not be included in the catalog.  This will restrict the spread of Hú-mons to be slower and help to contain them within social groups.


The app will utilize a step counter in order to heal Hú-mons. A background thread will run that will keep track of the amount of steps taken by the user. Every time the user takes a certain interval of steps (e.g. 100 steps), a percentage of the Hú-mons health will be restored, up to 100%. In addition, there will be a secondary method of healing Hú-mons, using a timer. This timer will take  a significant interval to heal Hú-mons (e.g.. every hour you heal the equivalent of a few steps). The timer method will heal significantly less than the steps to encourage activity from the user, and will also run in the background invisible to user.
Each user will create a unique username that will be stored in the server. This username can then be shared with friends to add to their friends list. Friends can then send each other a request to battle. When a battle commences, each user chooses a Hú-mon to battle with. They will enter a battle UI where both users choose which attack they would like their Hú-mon to use for a particular turn. The Hú-mons then use their attacks and both users are notified of status effects and damage done. The battle ends when all Hú-mons of a user have no health remaining.


Every time a user adds a new Hú-mon using pictures, they will be added to a local data storage. In addition, all Hú-mons you encounter when battling friends are added to the local data storage. These Hú-mons have unique ids for them to be stored on the server.  A user can then encounter any Hú-mon they have previously encountered, and they can view all encountered Hú-mons so far in a list format.
When battling against a wild Hú-mon or against a friend, a battle UI will commence. A user will choose which Hú-mons to use in a battle before the fight, and will automatically use their first chosen Hú-mon. For each turn, both users (or the wild Hú-mon) will choose a move. An algorithm will determine who goes first each turn using a combination of stats, status effects and random number generator. Both attacks will then take place in their order and both users will have their statuses updated. Attacks can do damage, heal, change stats etc. When a Hú-mon reaches zero health, they are removed from the battle. The battle continues until a user has no eligible Hú-mons. When facing a wild Hú-mon, the user will have the option to capture it and add it to their list of Hú-mons rather than defeating it.


When someone creates a new Hú-mon by taking a picture, they will have a list of stats to choose from (attack, defense, speed etc).  They will then rank their stats as to which are most and least favored by the Hú-mon. When a new instance of this Hú-mon is created, they will have their stats randomly generated, with the more favored stats having higher values. In addition, whenever a Hú-mon levels up, all of their stats will increase by a random number affected by ranking.
As a user walks a set interval of steps, they will have a random chance to battle any Hú-mon that they have already encountered (You cannot find Hú-mons of people  you do not know) when the user is located in an area that is marked ‘wild’ on the map. The user then has an option to battle the Hú-mon, and if they do they will have a chance to capture them. Stretch goal: Radiuses will appear on the map where a wild Hú-mon can be encountered. A user can only trigger a battle with a wild Hú-mon when within this area.

## App Development:
To-Do: How to work on the app and be productive

## Server Development:
Requirements:
* Eclipse IDE for java developers. (Other IDES may work, but have not been tested).
* mySQL (version 5.7).  Note: Nothing but a connection is established presently.  SQL scripts for DB creation will be added soon.
* Internet connection if testing server / client relationships non locally.


Steps to import:
1. Check out the ECE-454-Project git repository to your machine.
2. Open Eclipse IDE and import a Maven project.
</br> `File -> Import -> Maven -> Existing Maven project`
3. Import only the `Server` folder from the git repository.
4. Maven should build automatically and report any errors.  Assuming no errors, the server should be ready to start.

## TestClient
The test client provides a simple way of communication with the Server.  Simply import the project into Eclipse and run it.  You will be greeted with a Jpanel that will allow you to make a connection to the server and issue same commands along the socket.  The test client is mostly meant as a way to communicate with the server and debug messages to and from it, not as a fully featured application on its own.
