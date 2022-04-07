* To validate https://sarifweb.azurewebsites.net/Validation  
* To encode
    ```shell
    gzip -c complete-duke-basic.sarif.json | base64 -w0
    ```
* To send
    ```shell
    curl \
    -u {user} \
    -X POST \
    -H "Accept: application/vnd.github.v3+json" \
    https://api.github.com/repos/{owner}/{repo}/code-scanning/sarifs \
    -d '{"commit_sha":"33a06ea95f0633af4930e2c295fa4766dc895b01","ref":"refs/heads/start-tutorial","sarif":"<BASE64>"}'
    ```

