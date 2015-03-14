# PIPECore [![Build Status](https://travis-ci.org/sarahtattersall/PIPE.png?branch=master)](https://travis-ci.org/sarahtattersall/PIPECore)
This project contains the core set of classes for creating and animating Petri nets. 

This is an alpha release, supporting [hierarchical Petri nets and external interfaces](https://github.com/sjdayday/PIPECore/wiki), and is currently available through the hierarchical-nets branch.  Note that the maven information to use the hierarchical nets support is different from the PIPE 5.0 release in the master branch.

## Maven integration
To use this library in Maven projects add this GitHub project as an external repository:

```
<repositories>
    <repository>
        <id>PIPECore-mvn-repo</id>
        <url>https://raw.github.com/sjdayday/PIPECore/mvn-repo/</url>
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
    </repository>
</repositories>
```

Then include the SNAPSHOT version in your dependencies:
```
<dependencies>
    <dependency>
        <groupId>uk.ac.imperial</groupId>
        <artifactId>pipe-core</artifactId>
        <version>1.0.3-SNAPSHOT</version>
    </dependency>
</dependencies>
```

