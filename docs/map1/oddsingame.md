odds/live
This endpoint returns in-play odds for fixtures in progress.

Fixtures are added between 15 and 5 minutes before the start of the fixture. Once the fixture is over they are removed from the endpoint between 5 and 20 minutes. No history is stored. So fixtures that are about to start, fixtures in progress and fixtures that have just ended are available in this endpoint.

Update Frequency : This endpoint is updated every 5 seconds.*

* This value can change in the range of 5 to 60 seconds

INFORMATIONS ABOUT STATUS

"status": {
"stopped": false, // True if the fixture is stopped by the referee for X reason
"blocked": false, // True if bets on this fixture are temporarily blocked
"finished": false // True if the fixture has not started or if it is finished
},
INFORMATIONS ABOUT VALUES

When several identical values exist for the same bet the main field is set to True for the bet being considered, the others will have the value False.

The main field will be set to True only if several identical values exist for the same bet.

When a value is unique for a bet the main value will always be False or null.

Example below :

"id": 36,
"name": "Over/Under Line",
"values": [
{
"value": "Over",
"odd": "1.975",
"handicap": "2",
"main": true, // Bet to consider
"suspended": false // True if this bet is temporarily suspended
},
{
"value": "Over",
"odd": "3.45",
"handicap": "2",
"main": false, // Bet to no consider
"suspended": false
},
]
query Parameters
fixture
integer
The id of the fixture

league
integer (In this endpoint the "season" parameter is ...Show pattern
The id of the league

bet
integer
The id of the bet

header Parameters
x-apisports-key
required
string
Your Api-Key

Responses
200 OK
204 No Content
499 Time Out
500 Internal Server Error

get
/odds/live

Request samples
Use CasesPhpPythonNodeJavaScriptCurlRuby

Copy
// Get all available odds
get("https://v3.football.api-sports.io/odds/live");

// Get all available odds from one {fixture}
get("https://v3.football.api-sports.io/odds/live?fixture=164327");

// Get all available odds from one {league}
get("https://v3.football.api-sports.io/odds/live?league=39");

// It’s possible to make requests by mixing the available parameters
get("https://v3.football.api-sports.io/odds/live?bet=4&league=39");
get("https://v3.football.api-sports.io/odds/live?bet=4&fixture=164327");
Response samples
200204499500
Content type
application/json

Copy
Expand allCollapse all
{
"get": "odds/live",
"parameters": {
"fixture": "721238"
},
"errors": [ ],
"results": 1,
"paging": {
"current": 1,
"total": 1
},
"response": [
{}
]
}