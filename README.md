## Motivation

To learn the basics of Maven or Gradle, jUnit, Mockito and Java 8 by implementing active911's C++ connection pool in Java.
To understand an interesting implementation of the Strategy and Factory Design Pattern.

## Setup
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

## Dependencies

- JUnit
- Hamcrest since I am using assertThat instead of classic assertions
- Mockito to mock JdbConnectionFactory, JdbConnection and Connection classes

## Code coverage
Code coverage is measured using Jacoco for gradle and Cobertura for maven.
Current code has 100% coverage.

```
gradle tasks jacocoTestReport
```


```
maven cobertura:cobertura
```

## Code style
Code style check is done by using checkStyle plugin

## Code design

This project uses Strategy and Factory Design Pattern. This allows ConnectionPool class to be generic. The type of database supported is determined by the type of Factory object that is injected during runtime. The specific factory creates required specific connection class that contains the actual connection to the database. So for example , if you need a MySQL connection pool, pass in a MySQLJDBConnectionFactory object.

The design does satisfy the 3 common rules of design

1. Program to an interface
2. Favour composition over inheritance
3. Identify the aspects of your application that vary and separate them from what stays the same.

There are 3 interfaces/classes.

- JDBConnectionFactory
Requires an implementation class that creates JDBConnections objects
```
public class MySQLJDBConnectionFactory implements JDBConnectionFactory {
  public JDBConnection create() {
    return new MySQLJDBConnection(......
  }
}
```

- JDBConnection
Contains the connection object
```
public class MySQLJDBConnection implements JDBConnection {

  private java.sql.Connection connection;

  public MySQLJDBConnection() {
    connection = DriverManager.getConnection(username,password,host,port,"MySQL");
  }

  Connection getConnection() {
    return connection;
  }

 }
```

- ConnectionPool
Uses JDBConnectionFactory implementations to create the required number of JDBConnection implementation objects and stores them in a deque.

```
public class ConnectionPool {

  private Deque<JdbConnection> pooled = new ArrayDeque<>();

  private JdbConnectionFactory factory;

  public ConnectionPool(JdbConnectionFactory factory, int poolSize) {
    for (int i = 0; i < poolSize; i++) {
      pooled.addLast(factory.create());
    }

  }
```
A client will call borrow() in order to get a connection, and forfeit when returning.



