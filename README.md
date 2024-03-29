# Startsteps x Zalando Final Project: E-Commerce Shop RESTful API

## Description
The following is a Capstone Project for the Startsteps' Java Backend Development Course in collaboration with Zalando. It is a simplified backend repo of an e-commerce shop, handling everything from product management to order processing. The APIs return client-side responses to interact with front-end applications, allowing users to browse products, adding them to a cart, and placing orders.

## Initial setup
- Database name is `ecommercedb` with the following tables:
- Go to *Run* -> *Edit Configurations...*
- Under the Build and Run section: 
  - Click on the dropdown *Modify Options* and select *Environmental Variables* under *Operating System*
  - Set the `DB_USER`, `DB_PASSWORD`, `APP_SECRETKEY`, `APP_JWTCOOKIE`, and `APP_EXPIRATIONMS` in the *Environment Variables* field like so:
  - ```DB_USER=sqlUsernameGoesHere;DB_PASSWORD=sqlPasswordGoesHere;APP_SECRETKEY=generatedKeyGoesHere;APP_JWTCOOKIE=jwtCookieGoesHere;APP_EXPIRATIONMS=appExpirationMSGoesHere```
  - Contact the owner of this repo for the `.env` variables
- Once the configurations are set up, Click Run. To access the endpoints, check the Swagger documentation below

## Swagger API
- While the application is running, [click here](http://localhost:8080/swagger-ui/index.html) to API docs and endpoints

## Docker Setup/Deployment
- This is still a work in progress, but from [this branch](https://github.com/marishkazachariah/e-commerce-shop-restful-api/tree/docker-build), I managed to run the containers locally 
