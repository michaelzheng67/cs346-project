# GOOGLE.ai

*"**G**oing **O**n **O**utstanding **G**lobal **L**eisure **E**xcursions! **A**mazing **I**magery"*

## Project & Goal

A social media app designed to connect people who love travelling and sharing their experiences. The goal is to allow users to store, bookmark and display their past trips and excursions alongside other users in a social media format reminiscent of Pinterest or Reddit for travelling.

## Team Members

Aryan Rastogi [a8rastogi@uwaterloo.ca](mailto:a8rastogi@uwaterloo.ca) \
Daman Gill [daman.gill@uwaterloo.ca](mailto:daman.gill@uwaterloo.ca) \
Michael Zheng [qx2zheng@uwaterloo.ca](mailto:qx2zheng@uwaterloo.ca) \
Max Hurlimann [melhurli@uwaterloo.ca](mailto:melhurli@uwaterloo.ca)

## Documentation

Aside from this readme, documentation and writing is in the [wiki.](https://git.uwaterloo.ca/melhurli/group215-project/-/wikis/home)

#### [Proposal](https://git.uwaterloo.ca/melhurli/group215-project/-/wikis/proposal)

* [Project Plan](https://git.uwaterloo.ca/melhurli/group215-project/-/wikis/project-plan)
* [Requirements](https://git.uwaterloo.ca/melhurli/group215-project/-/wikis/requirements)
* [Design](https://git.uwaterloo.ca/melhurli/group215-project/-/wikis/design)

#### [Release Notes](https://git.uwaterloo.ca/melhurli/group215-project/-/wikis/Release-Notes)

#### [Sprint Meeting Minutes](https://git.uwaterloo.ca/melhurli/group215-project/-/wikis/meeting-minutes)

#### [Instructions](https://git.uwaterloo.ca/melhurli/group215-project/-/wikis/instructions)

#### [Reflections on Practices](https://git.uwaterloo.ca/melhurli/group215-project/-/wikis/reflections-on-practices)

## Installation Details

Simply download and run the installer for your platform, either linux or windows. The server is already hosted in the cloud, so you can simply install and run the application (front-end) and it'll work.

# Development info

## Commands to run locally

The three projects in the gradle multiproject scheme are server, application, and models. The server is hosted on GCP. To build, run, or execute tests, you can do:

```
.\gradlew x:run

.\gradlew x:build

.\gradlew x:test

```
Where "x" is either application or server (models has nothing to run). 


## cURL requests to test out various endpoints (just examples, may have to change values depending on constraints)

curl http://localhost:8080/user/get_pins/2

curl -X POST \
-H "Content-Type: application/json" \
-d '{"email":"michael@email.com","password":"michaelPassword","name":"michael", "bio":"bio", "joined":"joined", "
image":"image"}' \
http://localhost:8080/user/insert

curl -X POST \
-H "Content-Type: application/json" \
-d '{"testing": false, "email":"michael@email.com","password":"michaelPassword","name":"michael", "bio":"bio", "
joined":"joined", "image":"image"}' \
https://journeymid.ue.r.appspot.com/user/insert

curl -X POST \
-H "Content-Type: application/json" \
-d '{"userId":2,"city":"city","image":"image"}' \
http://localhost:8080/pin/insert

curl -X POST \
-H "Content-Type: application/json" \
-d '{"userId":2,"name":"name","startTime":"startTime", "endTime":"endTIme"}' \
http://localhost:8080/trip/insert

curl -X POST \
-H "Content-Type: application/json" \
-d '{"userId":2,"name":"name", "description":"description", "startTime":"startTime", "endTime":"endTIme", "score":1, "
image":"image", "status":"status"}' \
https://journeymid.ue.r.appspot.com/trip/insert

## Starting Backend Cloud Service

From the GCP Console:

```
git clone {repo}
git pull (if cloned before)

./gradlew :server:build
gcloud app deploy app.yaml

```

## Troubleshooting (Common issues for devs)

### Getting any errors related to java versioning

e.g No matching toolchains found for requested specification: {languageVersion=17, vendor=AZUL,
implementation=vendor-specific} for MAC_OS on x86_64.
make sure that you have downloaded java 17 from
Azul: https://www.azul.com/downloads/?version=java-17-lts&package=jdk#zulu

## Credits

Credit to Sean Proctor (github.com/sproctor) for the base of the Compose Desktop auth0 implementation: https://levelup.gitconnected.com/oauth-in-compose-for-desktop-with-auth0-9990075606a1https://levelup.gitconnected.com/oauth-in-compose-for-desktop-with-auth0-9990075606a1

Credit to Mouaad Aallam (https://github.com/aallam) for his implementation of an OpenAI API client for Kotlin: https://github.com/aallam/openai-kotlin

Credit to StackOverFlow user Gabriele Mariotti for the following implementation example of a dropdown menu: https://stackoverflow.com/questions/67111020/exposed-drop-down-menu-for-jetpack-compose

Credit to the following resources for implementation and details on Image Uploads:
* https://www.reddit.com/r/Kotlin/comments/n16u8z/desktop_compose_file_picker/
* https://stackoverflow.com/questions/12558413/how-to-filter-file-type-in-filedialog
* https://stackoverflow.com/questions/37066216/java-encode-file-to-base64-string-to-match-with-other-encoded-string
