# driver-pete-server
Server for driver pete


Security properties can be set adding security.properties file to the src/main/resources:

facebook.appKey=<YOUR FACEBOOK APP ID>
facebook.appSecret=<YOUR FACEBOOK APP SECRET>
token.secret=<BIG RANDOM STRING (e.g. 96 characters)>


Example of token secret:
wergwegoDsTmXfogIieDI0cD/8FpnojdfghdfJT5U9I/FGVmBzwertR8cbXTvoPjX+Pq/T/b1PqpHX0lYm0oCBjXWICA==


On travis and amazon security properties has to be set using environmental variables.
Notice that you can not set env var with '.', use '_' instead (e.g. export token_secret=<...>)
