#Motivation

To learn the basics of maven/gradle, junit  and Java 8 by implementing active911's C++ connection pool in Java.

#Setup
Create dev environment using Maven by running the following
```
mvn archetype:generate -DgroupId=com.spakai -DartifactId=connection-pool -Dpackage=com.spakai -Dversion=1.0-SNAPSHOT
```
This will create the following directories
```
main/java/com/spakai
test/java/com/spakai
```

#Dependencies

- JUnit
- Hamcrest since I am using assertThat instead of classic assertions
- Mockito to mock JDBConnectionFactory, JDBConnection and Connection classes

#Code coverage
Code coverage is measured using Jacoco for gradle and Cobertura for maven.
