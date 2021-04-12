# Herkimer

<img align="right" src="https://static.wikia.nocookie.net/muppet/images/4/4e/Herkimer.jpg/revision/latest/scale-to-width-down/300?cb=20081201045924">

Herkimer is a component that stores connection info for provisioned resources. Example: DBH database instances,
Storagegrid object areas etc. It also is a register of all ApplicationDeployment witha corresponding ID. The API is
documented through runnable http requests in the http package.

The component is named after Herkimer from the TV-show Fraggle
Rock (https://muppet.fandom.com/wiki/Episode_502:_The_Riddle_of_Rhyming_Rock).

## Setup

In order to use this project you must set repositories in your `~/.gradle/init.gradle` file

     allprojects {
         ext.repos= {
             mavenCentral()
             jcenter()
         }
         repositories repos
         buildscript {
          repositories repos
         }
     }

We use a local repository for distributionUrl in our gradle-wrapper.properties, you need to change it to a public repo
in order to use the gradlew command. `../gradle/wrapper/gradle-wrapper.properties`

    <...>
    distributionUrl=https\://services.gradle.org/distributions/gradle-<version>-bin.zip
    <...>

Herkimer uses a postgres database, so for local development you have to run `docker-compose up`

## Terminology

- ApplicationDeployment: Aurora abstraction for a deployed application
- Kind: A type of resource. Example MinioPolcy, PostgresDatabaseInstance
- Resource: A concrete resource of a specific kind that one can claim access to. Example database instance
- ResourceClaim: A claim to a specific resource. The claim contains the credentials needed to connect to the resource

## How it works

In order to claim a resource one has to have an ApplicationDeployment(Ad). The ID of the Ad is used as OwnerId when
creating a Resource. One can then use the resourceId and AdID(ownerId claim) to register a claim.

