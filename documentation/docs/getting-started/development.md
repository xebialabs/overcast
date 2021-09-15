---
sidebar_position: 6
---

# Development

## How to build the project

`./gradlew clean build`

## Where documentation resides

You can find the documentation to edit in documentation/docs folder. The `docs` folder contains built documentation 
which is served on GitHub Pages.

## How to run documentation site locally

`./gradlew yarnRunStart`

The site will be opened automatically in your default browser on page: [http://localhost:3000/integration-server-gradle-plugin/](http://localhost:3000/integration-server-gradle-plugin/) 

## How to generate the documentation for GitHub

`./gradlew docBuild` and commit all modified files in docs folder.
