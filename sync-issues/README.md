# GitHub to Jira issues synchronization

Sample (and working) scripts triggered by the [issues.yml](../.github/workflows/issues.yml) GitHub workflow when an issue is opened, closed and reopended to, respectively, create, close and reopen a subtask in Jira.  
The naming convention for the GitHub issue's title is `[<JIRA_ISSUE_ID>] <TITLE_YOU_WANT>`.  
In this way, when the GitHub issue is:
- opened, a **subtask** will be added in Jira to the `<JIRA_ISSUE_ID>` issue with:
    - Jira subtask's *title* following the pattern `[<GITHUB_REPO_NAME>#<GITHUB_ISSUE_NUMBER>`]  <TITLE_YOU_WANT>` (`<TITLE_YOU_WANT>` coming from above)
    - Jira subtask's *description* will be the link to the GitHub issue
- closed, the subtask will be closed in Jira 
- reopened, the subtask will be reopened in Jira 

> :warning: None of the changes done in Jira will be reflected in GitHub
