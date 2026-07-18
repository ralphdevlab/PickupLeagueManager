# Pickup League Manager - Phase 4

A Java desktop app for managing soccer and basketball pickup league rosters.
Player data is stored in a MySQL database. You can add, view, update, and delete
players, run a custom action that calculates a team's average age, and all input
is validated so you can't enter bad data or crash the program.

## Before you run it

You'll need Java 17 or newer, and MySQL installed and running. MySQL Workbench
is helpful for viewing the database but not required. The MySQL driver is already
inside the JAR, so there's nothing extra to install for that.

## Step 1: Set up the database

Run the included pickup_league_setup.sql script. In MySQL Workbench, go to
File > Open SQL Script, pick the file, and hit the lightning bolt to run it.

That creates a database called pickup_league, a players table, and loads 22
players into it. The last line of the script counts the rows and should return 22.

If you'd rather use the command line:

    mysql -u root -p < pickup_league_setup.sql

## Step 2: Run the app

    java -jar PickupLeagueManager.jar

When it starts, a small window asks for your database connection info:

- Server: localhost:3306 (change the port if yours is different)
- Username: your MySQL username, usually root
- Password: your MySQL password

None of this is hardcoded - the app builds the connection from whatever you type,
so it works on any machine that has the database. If you get the details wrong,
it tells you and lets you try again instead of crashing.

Once it connects, the main window opens with all 22 players listed.

## What the buttons do

Refresh from database reloads the table from MySQL. Add player takes the info
in the form and adds a new player. To update or delete, click a player's row
first (that loads them into the form), then hit Update selected or Delete selected.
Average age of team is the custom action - type in a team ID and it tells you the
average age of the players on that team. Clear form empties the fields.

Every change goes straight to the database. If you refresh the players table in
Workbench after adding or editing someone, you'll see the change there too.

## A note on how it's built

The code follows the Model-View-Controller pattern. Player and PlayerValidator
hold the data and the rules, PlayerGUI is the interface, and PlayerList handles
talking to the database using JDBC with prepared statements. The table and column
names are hardcoded since everyone runs the same database, but the server,
username, and password always come from the user.
