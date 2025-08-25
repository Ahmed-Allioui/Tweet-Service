# Tweet-Service
# Presentation
Tweet Service is a microservice to manage tweets and comments. It provides the following functions :
  - create a new tweet
  - add comment to a tweet
  - get a tweet by id
  - get all tweets belonging to a specific user
  - get all tweets of the follows of a specific user
  - get all tweet by a certain hashtag
  - get all tweet containing a certain word
  - like a tweet
  - like a comment
  - unlike a tweet
  - unlike a comment
  - delete a tweet
  - delete all tweets of a user
  - delete a comment

# Requirements
In order to use the application you need to have the following applications installed:
  - JDK/JRE
  - Docker
  - Docker Compose

# Building the project
To build the project:
> gradlew clean build

# Starting the App
To start the application with the database:
> docker-compose up -d

# Endpoints
Once the app starts, you can see all enpoints using the link belong:
http://localhost:8081/swagger-ui

![image](https://user-images.githubusercontent.com/92538862/150372637-a677ab78-7fa2-4d0e-b8bf-c3cc356a90f1.png)
