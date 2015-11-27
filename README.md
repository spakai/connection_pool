#Motivation

To Learn the basics of maven, junit  and Java 8 by implementing active911's C++ connection pool in Java.

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
- Hamcrest since i am using assertThat instead of classic assertions
- Mockito to mock the db connection

