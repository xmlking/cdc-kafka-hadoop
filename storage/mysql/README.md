MySQL
=====
Database setup and configuration for Maxwell CDC integration

### working directory
*you will be running all commends below from* **storage/mysql** *directory*

### Install MySQL (one time)
```bash
brew install mysql
mysql -V  # Verify the MySQL installation
```

### Initializing Database (one time)
```bash
unset TMPDIR
# mysqld --initialize-insecure --user=`whoami` --basedir="$(brew --prefix mysql)"  --datadir=./data
mysqld --defaults-file=./my.cnf --initialize-insecure --user=`whoami`
```

### Run MySQL
```bash
cd storage/mysql/
mysqld --defaults-file=./my.cnf # start
mysqladmin -u root -p shutdown  # stop
```

#### Setup Security (for Production) (one time)
```bash
mysql_secure_installation
```
```
Would you like to setup VALIDATE PASSWORD plugin? N
Please set the password for root here: # set and remember new password.
Remove anonymous users?  Y
Disallow root login remotely? Y
Remove test database and access to it? N
Reload privilege tables now? Y
```

#### Grant permissions for maxwell (one time)
```sql
mysql -u root -p

mysql> GRANT ALL on maxwell.* to 'maxwell'@'%' identified by 'XXXXXX';
mysql> GRANT SELECT, REPLICATION CLIENT, REPLICATION SLAVE on *.* to 'maxwell'@'%';

# or for running maxwell locally:

mysql> GRANT SELECT, REPLICATION CLIENT, REPLICATION SLAVE on *.* to 'maxwell'@'localhost' identified by 'XXXXXX';
mysql> GRANT ALL on maxwell.* to 'maxwell'@'localhost';
```

#### MySQL Command Line Tool
```sql
mysql -u root -p
SHOW DATABASES; # List all existing databases.
SELECT DISTINCT User FROM mysql.user;  #List all MySQL / MariaDB users.
SHOW VARIABLES WHERE Variable_Name LIKE "%dir"; #  see Env
DROP DATABASE maxwell;
DROP TABLE test.shop;
```
