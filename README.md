# Startsteps x Zalando Final Project: E-Commerce Shop RESTful API
## Initial setup
- Database name is `ecommercedb` with the following tables:
- Go to *Run* -> *Edit Configurations...*
- Under the Build and Run section: 
  - Click on the dropdown *Modify Options* and select *Environmental Variables* under *Operating System*
  - Set the `DB_USER`, `DB_PASSWORD` and `APP_SECRETKEY` in the *Environment Variables* field like so:
  - `DB_USER=sqlUsernameGoesHere;DB_PASSWORD=sqlPasswordGoesHere;APP_SECRETKEY=generatedKeyGoesHere`
## Swagger API
- While the application is running, [click here](http://localhost:8080/swagger-ui/index.html) to API docs and endpoints