export access_token=$(\
    curl -X POST http://$1/auth/realms/quarkus/protocol/openid-connect/token \
    --user backend-service:secret \
    -H 'content-type: application/x-www-form-urlencoded' \
    -d 'username=alice&password=alice&grant_type=password' | jq --raw-output '.access_token' \
 );
STAKEHOLDER_ID=$(curl -X POST "http://$1/controls/stakeholder" -H  "accept: application/json" -H  "Content-Type: application/json" -H "Authorization: Bearer "$access_token -d "{\"email\":\"jbfletcher@murdershewrote.com\",\"displayName\":\"Jessica Fletcher\",\"jobFunction\":\"CEO\"}"|jq -r .id);
curl -X POST "http://$1/controls/business-service" -H  "accept: application/json" -H  "Content-Type: application/json" -H "Authorization: Bearer "$access_token -d "{  \"name\": \"Home Banking BU $STAKEHOLDER_ID\",  \"description\": \"Important service to let private customer use their home banking accounts\",  \"owner\": {    \"id\": $STAKEHOLDER_ID  }}"|jq .;
curl -X POST "http://$1/controls/business-service" -H  "accept: application/json" -H  "Content-Type: application/json" -H "Authorization: Bearer "$access_token -d "{  \"name\": \"Online Investments service $STAKEHOLDER_ID\",  \"description\": \"Corporate customers investments management\",  \"owner\": {    \"id\": $STAKEHOLDER_ID  }}"|jq .;
curl -X POST "http://$1/controls/business-service" -H  "accept: application/json" -H  "Content-Type: application/json" -H "Authorization: Bearer "$access_token -d "{  \"name\": \"Credit Cards BS $STAKEHOLDER_ID\",  \"description\": \"Internal credit card creation and management service\"}"|jq .;
