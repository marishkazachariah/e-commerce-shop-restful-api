# Startsteps x Zalando Final Project: E-Commerce Shop RESTful API
## Initial setup
- database name is `ecommercedb` with the following tables:
- go to *Run* -> *Edit Configurations...*
- Under the Build and Run section: 
  - Click on the dropdown *Modify Options* and select *Environmental Variables* under *Operating System*
  - set the `DB_USER`, `DB_PASSWORD` and `APP_SECRETKEY` in the *Environment Variables* field like so:
  - `DB_USER=sqlUsernameGoesHere;DB_PASSWORD=sqlPasswordGoesHere;APP_SECRETKEY=generatedKeyGoesHere`