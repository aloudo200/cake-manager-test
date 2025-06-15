Cake Manager Micro Service (fictitious)
=======================================

A summer intern started on this project but never managed to get it finished.
The developer assured us that some of the above is complete, but at the moment accessing the /cakes endpoint
returns a 404, so getting this working should be the first priority.

Requirements:
* By accessing /cakes, it should be possible to list the cakes currently in the system. JSON would be an acceptable response format.

* It must be possible to add a new cake.

* It must be possible to update an existing cake.

* It must be possible to delete an existing cake.

Comments:
* We feel like the software stack used by the original developer is quite outdated, it would be good to migrate the entire application to something more modern. If you wish to update the repo in this manner, feel free! An explanation of the benefits of doing so (and any downsides) can be discussed at interview.

* Any other changes to improve the repo are appreciated (implementation of design patterns, seperation of concerns, ensuring the API follows REST principles etc)

Bonus points:
* Add some suitable tests (unit/integration...)
* Add some Authentication / Authorisation to the API
* Continuous Integration via any cloud CI system
* Containerisation

Scope
* Only the API and related code is in scope. No GUI of any kind is required

Updated Project Info
=====================

JDK Version: 21 (eclipse-temurin:21-jdk-jammy)

To run the tomcat server locally, see the following steps:

1. Start the CakeManagerApplication main class
2. Open a web browser and access the following URL: `http://localhost:8080`
3. You will be brought to Swagger's built in /login page
4. Use the following credentials to log in as a "USER" with lower privileges:
   - Username: `tester`
   - Password: `waracle`
5. Use the following credentials to log in as a "CHEF" with the highest privileges:
   - Username: `headchef`
   - Password: `waracle`
6. Note that the password for both users is effectively ignored, since the purpose of this is to demonstrate the API
    functionality and authorisation using Spring Roles rather than implement a full authentication system.
7. Once logged in, you can access the API documentation at `http://localhost:8080/swagger-ui.html`
8. As a USER, you can only access "/getAllCakes", "/getCakeById", and "/addNewCake" endpoints.
9. As a CHEF, you can access all endpoints including "/updateCake" and "/deleteCake".
10. I implemented a simple /logout function also, which leverages Spring Security to invalidate the session.
    You can test this by accessing the "localhost:8080/logout" endpoint after logging in, and trying to access any of the endpoints again;
    It will simply redirect you to the login page as the session cookies have been deleted. The endpoints also cannot be accessed without a valid session.

Finally, for Docker setup, I created a simple Dockerfile that will build v1.0 of the application and run it on port 8080.
If, for example, using Docker Desktop, you can build the image with the following command from within the IDE terminal:

docker build --build-arg VERSION=1.0 -t cake-manager:1.0 .

To run the Docker container, simply update the configuration of the image as shown in the 'Docker-Container-Setup.png' screenshot,
and run the container from Docker Desktop. Otherwise, I have provided another screenshot demonstrating the running container.

Submission
==========

Please provide your version of this project as a git repository (e.g. Github, BitBucket, etc).

A fork of this repo, or a Pull Request would be suitable.

Good luck!
