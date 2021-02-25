echo $GH_ISSUE_TITLE
echo $NUMBER
echo $DESCRIPTION
JIRA_ISSUE_ID=$(echo $GH_ISSUE_TITLE|cut -d] -f1|sed 's/[[]//g')
TITLE=$(echo $GH_ISSUE_TITLE|cut -d] -f2)
echo $JIRA_ISSUE_ID
echo $TITLE
PAYLOAD="{ \"fields\": { \"project\": { \"key\": \"TACKLE\" }, \"parent\": { \"key\": \"$JIRA_ISSUE_ID\" }, \"summary\": \"[tackle-controls#"$NUMBER"]$TITLE\", \"description\": \"$DESCRIPTION\", \"issuetype\": { \"id\": 5 } } }"
echo $PAYLOAD
curl -X POST -H "Content-Type: application/json" -H "Authorization: Bearer "$BEARER -d "$PAYLOAD" https://issues.redhat.com/rest/api/2/issue/
