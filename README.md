# PIPECore [![Build Status](https://travis-ci.org/sarahtattersall/PIPE.png?branch=master)](https://travis-ci.org/sarahtattersall/PIPECore)
This project contains the core set of classes for creating and animating Petri nets. 

This is a beta version, primarily supporting [hierarchical Petri nets and external interfaces](https://github.com/sjdayday/PIPECore/wiki).  

Thanks to Malte Schilling (https://github.com/malteschilling), it also incorporates some preliminary support for timed transitions; a delay can be specified for a transition, and it will only fire 'delay' ms after it is enabled.  See TimedPetriNetRunner and RealTimePetriNetRunner.

This version of the code is currently available through the hierarchical-nets branch, or as the 2.0.0 release through Maven, below.  The corresponding branch of the user interface is PIPE 5.1.x, also available through the hierarchical-nets branch of PIPE.  The GUI is still under active development so is only available as a snapshot. 

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

Then include the artifact in your dependencies:
```
<dependencies>
    <dependency>
        <groupId>uk.ac.imperial</groupId>
        <artifactId>pipe-core</artifactId>
        <version>2.0.0</version>
    </dependency>
</dependencies>
```

