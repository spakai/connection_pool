#Motivation

To learn the basics of Maven, Gradle, jUnit, Mockito and Java 8 by implementing active911's C++ connection pool in Java.

#Setup
Create development environment using Maven by running the following
```
mvn archetype:generate -DgroupId=com.spakai -DartifactId=connection-pool -Dpackage=com.spakai -Dversion=1.0-SNAPSHOT
```
This will create the following directories
```
connection-pool
 main/java/com/spakai
 test/java/com/spakai
```

Next , run the following to generate a build.gradle file. This uses the maven plugin inside gradle.

```
gradle init
```

#Dependencies

- JUnit
- Hamcrest since I am using assertThat instead of classic assertions
- Mockito to mock JdbConnectionFactory, JdbConnection and Connection classes

#Code coverage
Code coverage is measured using Jacoco for gradle and Cobertura for maven.
Current code has 100% coverage.

```
gradle tasks jacocoTestReport
```


```
maven cobertura:cobertura
```

#Code style
Code style check is done by using checkStyle plugin

#Code design

There are 3 interfaces/classes. I will add the implementation classes for the interfaces soon.


- JDBConnectionFactory 
Requires an implementation class that creates JDBConnections objects

public class MySQLJDBConnectionFactory implements JDBConnectionFactory {
  public JDBConnection create() {
    return new MySQLJDBConnection(......
  }
}

- JDBConnection
Contains the connection object 

public class MySQLJDBConnection implements JDBConnection {

  private java.sql.Connection connection;
  
  public MySQLJDBConnection() {
    connection = DriverManager.getConnection(username,password,host,port,"MySQL");
  }
  
  Connection getConnection() {
    return connection;
  }
  
 }

- ConnectionPool
Uses JDBConnectionFactory implementations to create the required number of JDBConnection implementation objects and stores them in a deque.
A client will call borrow() in order to get a connection, and forfeit when returning.
