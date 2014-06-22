# PIPECore [![Build Status](https://travis-ci.org/sarahtattersall/PIPE.png?branch=master)](https://travis-ci.org/sarahtattersall/PIPECore)
This project contains the core set of classes for creating and animating Petri nets. 

## Maven integration
To use this library in Maven projects add this GitHub project as an external repository:

```
<repositories>
    <repository>
        <id>PIPECore-mvn-repo</id>
        <url>https://raw.github.com/sarahtattersall/PIPECore/mvn-repo/</url>
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
    </repository>
</repositories>
```

Then either include the SNAPSHOT or latest release version in your dependencies:
```
<dependencies>
    <dependency>
        <groupId>uk.ac.imperial</groupId>
        <artifactId>pipe-core</artifactId>
        <version>1.0.1</version>
    </dependency>
</dependencies>
```

