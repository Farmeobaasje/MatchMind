2025-12-23 15:58:04.113  6193-6193  MatchDetailScreen       com.Lyno.matchmindai                 D  Screen loaded with fixtureId: 1379146
2025-12-23 15:58:05.289  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures?league=39&season=2025&status=FT&timezone=Europe%2FAmsterdam
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 15:58:05.367  6193-6306  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures?league=39&season=2025&status=FT&timezone=Europe%2FAmsterdam
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28b71709520b7f-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:06 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=ALC%2BKOtO%2FIU8z3%2BVotIFm2KIM5FHeF12VEbilyZ4NWpPt9StDJcZYt4Hb9opRj7x4cnfmTzt87f50ffLfvImgovxXg50p1APCZD4mOU7x4j52LyEXK5oQQ%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766501885322
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501885289
-> x-envoy-upstream-service-time: 11
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 299
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7173
-> x-ttl: 300
BODY Content-Type: application/json
BODY START
2025-12-23 15:58:05.367  6193-6306  Ktor-ApiSports          com.Lyno.matchmindai                 D  {"get":"fixtures","parameters":{"league":"39","season":"2025","status":"FT","timezone":"Europe\/Amsterdam"},"errors":[],"results":170,"paging":{"current":1,"total":1},"response":[{"fixture":{"id":1378969,"referee":"Anthony Taylor, England","timezone":"Europe\/Amsterdam","date":"2025-08-15T21:00:00+02:00","timestamp":1755284400,"periods":{"first":1755284400,"second":1755288000},"venue":{"id":550,"name":"Anfield","city":"Liverpool"},"status":{"long":"Match Finished","short":"FT","elapsed":90,"extra":7}},"league":{"id":39,"name":"Premier League","country":"England","logo":"https:\/\/media.api-sports.io\/football\/leagues\/39.png","flag":"https:\/\/media.api-sports.io\/flags\/gb-eng.svg","season":2025,"round":"Regular Season - 1","standings":true},"teams":{"home":{"id":40,"name":"Liverpool","logo":"https:\/\/media.api-sports.io\/football\/teams\/40.png","winner":true},"away":{"id":35,"name":"Bournemouth","logo":"https:\/\/media.api-sports.io\/football\/teams\/35.png","winner":false}},"goals":{"home":4,"away":2},"score":{"halftime":{"home":1,"away":0},"fulltime":{"home":4,"away":2},"extratime":{"home":null,"away":null},"penalty":{"home":null,"away":null}}},{"fixture":{"id":1378970,"referee":"Craig Pawson, England","timezone":"Europe\/Amsterdam","date":"2025-08-16T13:30:00+02:00","timestamp":1755343800,"periods":{"first":1755343800,"second":1755347400},"venue":{"id":495,"name":"Villa Park","city":"Birmingham"},"status":{"long":"Match Finished","short":"FT","elapsed":90,"extra":7}},"league":{"id":39,"name":"Premier League","country":"England","logo":"https:\/\/media.api-sports.io\/football\/leagues\/39.png","flag":"https:\/\/media.api-sports.io\/flags\/gb-eng.svg","season":2025,"round":"Regular Season - 1","standings":true},"teams":{"home":{"id":66,"name":"Aston Villa","logo":"https:\/\/media.api-sports.io\/football\/teams\/66.png","winner":null},"away":{"id":34,"name":"Newcastle","logo":"https:\/\/media.api-sports.io\/football\/teams\/34.png","winner":null}},"goals":{"home":0,"away":0},"score":{"halftime":{"home":0,"away":0},"fulltime":{"home":0,"away":0},"extratime":{"home":null,"away":null},"penalty":{"home":null,"away":null}}},{"fixture":{"id":1378974,"referee":"Michael Oliver, England","timezone":"Europe\/Amsterdam","date":"2025-08-16T16:00:00+02:00","timestamp":1755352800,"periods":{"first":1755352800,"second":1755356400},"venue":{"id":593,"name":"Tottenham Hotspur Stadium","city":"London"},"status":{"long":"Match Finished","short":"FT","elapsed":90,"extra":4}},"league":{"id":39,"name":"Premier League","country":"England","logo":"https:\/\/media.api-sports.io\/football\/leagues\/39.png","flag":"https:\/\/media.api-sports.io\/flags\/gb-eng.svg","season":2025,"round":"Regular Season - 1","standings":true},"teams":{"home":{"id":47,"name":"Tottenham","logo":"https:\/\/media.api-sports.io\/football\/teams\/47.png","winner":true},"away":{"id":44,"name":"Burnley","logo":"https:\/\/media.api-sports.io\/football\/teams\/44.png","winner":false}},"goals":{"home":3,"away":0},"score":{"halftime":{"home":1,"away":0},"fulltime":{"home":3,"away":0},"extratime":{"home":null,"away":null},"penalty":{"home":null,"away":null}}},{"fixture":{"id":1378971,"referee":"Samuel Barrott, England","timezone":"Europe\/Amsterdam","date":"2025-08-16T16:00:00+02:00","timestamp":1755352800,"periods":{"first":1755352800,"second":1755356400},"venue":{"id":508,"name":"Amex Stadium","city":"Brighton"},"status":{"long":"Match Finished","short":"FT","elapsed":90,"extra":9}},"league":{"id":39,"name":"Premier League","country":"England","logo":"https:\/\/media.api-sports.io\/football\/leagues\/39.png","flag":"https:\/\/media.api-sports.io\/flags\/gb-eng.svg","season":2025,"round":"Regular Season - 1","standings":true},"teams":{"home":{"id":51,"name":"Brighton","logo":"https:\/\/media.api-sports.io\/football\/teams\/51.png","winner":null},"away":{"id":36,"name":"Fulham","logo":"https:\/\/media.api-sports.io\/football\/teams\/36.png","winner":null}},"goals":{"home":1,"away":1},"score":{"half
2025-12-23 15:58:05.367  6193-6306  Ktor-ApiSports          com.Lyno.matchmindai                 D  BODY END
2025-12-23 15:58:05.387  6193-6193  MatchRepository         com.Lyno.matchmindai                 D  Retrieved 151 historical fixtures for league 39, season 2025
2025-12-23 15:58:05.387  6193-6193  MatchRepository         com.Lyno.matchmindai                 D  Using league data for prediction: 151 fixtures for league 39
2025-12-23 15:58:05.387  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Found 151 historical matches for LeagueID: 39 (Home: 65, Away: 50)
2025-12-23 15:58:05.387  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Team-specific matches: Home Team 65 = 11 matches, Away Team 50 = 12 matches
2025-12-23 15:58:05.387  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Fetching xG data for 20 recent fixtures
2025-12-23 15:58:05.388  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1378983 (Crystal Palace vs Nottingham Forest)
2025-12-23 15:58:05.388  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379012 (Burnley vs Nottingham Forest)
2025-12-23 15:58:05.388  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379027 (Nottingham Forest vs Sunderland)
2025-12-23 15:58:05.388  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379037 (Newcastle vs Nottingham Forest)
2025-12-23 15:58:05.389  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379051 (Bournemouth vs Nottingham Forest)
2025-12-23 15:58:05.389  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379065 (Nottingham Forest vs Manchester United)
2025-12-23 15:58:05.389  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379075 (Nottingham Forest vs Leeds)
2025-12-23 15:58:05.389  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379095 (Nottingham Forest vs Brighton)
2025-12-23 15:58:05.389  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379108 (Wolves vs Nottingham Forest)
2025-12-23 15:58:05.389  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379133 (Fulham vs Nottingham Forest)
2025-12-23 15:58:05.389  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1378990 (Brighton vs Manchester City)
2025-12-23 15:58:05.389  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379009 (Arsenal vs Manchester City)
2025-12-23 15:58:05.389  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379025 (Manchester City vs Burnley)
2025-12-23 15:58:05.389  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379032 (Brentford vs Manchester City)
2025-12-23 15:58:05.389  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379044 (Manchester City vs Everton)
2025-12-23 15:58:05.389  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379050 (Aston Villa vs Manchester City)
2025-12-23 15:58:05.390  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379064 (Manchester City vs Bournemouth)
2025-12-23 15:58:05.390  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379087 (Newcastle vs Manchester City)
2025-12-23 15:58:05.390  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379094 (Manchester City vs Leeds)
2025-12-23 15:58:05.390  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379103 (Fulham vs Manchester City)
2025-12-23 15:58:05.642  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1378983
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 15:58:05.646  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379012
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 15:58:05.652  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379027
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 15:58:05.655  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379037
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 15:58:05.661  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379051
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 15:58:05.667  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379065
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 15:58:05.675  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379075
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 15:58:05.688  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379095
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 15:58:05.695  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379108
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 15:58:05.701  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379133
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 15:58:05.708  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1378990
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 15:58:05.716  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379009
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 15:58:05.723  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379025
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 15:58:05.732  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379032
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 15:58:05.738  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379044
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 15:58:05.745  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379050
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 15:58:05.751  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379064
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 15:58:05.756  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379087
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 15:58:05.760  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379094
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 15:58:05.765  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379103
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 15:58:05.772  6193-6274  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1378983
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28b7193b5d0b7f-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:06 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=ajVI%2BMmoW7dqs6YuIwmPOpc8xkga6i4RynQjt1B6jVE%2BILLdGZXhaDt3llhXsiY3kDLC3rjFXUMHf%2FNXz5jOTPF0akmXwXPOKeTJoCmhxxa8%2FRbihtJ5%2FA%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766501885676
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501885644
-> x-envoy-upstream-service-time: 7
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 297
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7171
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1378983"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":52,"name":"Crystal Palace","logo":"https:\/\/media.api-sports.io\/football\/teams\/52.png"},"statistics":[{"type":"Shots on Goal","value":4},{"type":"Shots off Goal","value":4},{"type":"Total Shots","value":8},{"type":"Blocked Shots","value":0},{"type":"Shots insidebox","value":7},{"type":"Shots outsidebox","value":1},{"type":"Fouls","value":11},{"type":"Corner Kicks","value":1},{"type":"Offsides","value":2},{"type":"Ball Possession","value":"42%"},{"type":"Yellow Cards","value":3},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":0},{"type":"Total passes","value":377},{"type":"Passes accurate","value":287},{"type":"Passes %","value":"76%"},{"type":"expected_goals","value":"1.10"},{"type":"goals_prevented","value":0}]},{"team":{"id":65,"name":"Nottingham Forest","logo":"https:\/\/media.api-sports.io\/football\/teams\/65.png"},"statistics":[{"type":"Shots on Goal","value":1},{"type":"Shots off Goal","value":7},{"type":"Total Shots","value":9},{"type":"Blocked Shots","value":1},{"type":"Shots insidebox","value":8},{"type":"Shots outsidebox","value":1},{"type":"Fouls","value":11},{"type":"Corner Kicks","value":3},{"type":"Offsides","value":2},{"type":"Ball Possession","value":"58%"},{"type":"Yellow Cards","value":3},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":3},{"type":"Total passes","value":514},{"type":"Passes accurate","value":442},{"type":"Passes %","value":"86%"},{"type":"expected_goals","value":"0.93"},{"type":"goals_prevented","value":0}]}]}
BODY END
2025-12-23 15:58:05.774  6193-6309  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379012
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28b7194eca95d9-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:06 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=NQQRJN6KVQSlbonOy3Umg6I81mDUFuVLBeYombkyw9IjMMvcUod5TjihCoXSs5s8Lc0P5eBNK7hogMUzg42nIAxkVmvr2HarkBPpBfU6SjoqjkA5ouZ%2BLg%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766501885679
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501885648
-> x-envoy-upstream-service-time: 7
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 298
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7172
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379012"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":44,"name":"Burnley","logo":"https:\/\/media.api-sports.io\/football\/teams\/44.png"},"statistics":[{"type":"Shots on Goal","value":5},{"type":"Shots off Goal","value":4},{"type":"Total Shots","value":12},{"type":"Blocked Shots","value":3},{"type":"Shots insidebox","value":9},{"type":"Shots outsidebox","value":3},{"type":"Fouls","value":12},{"type":"Corner Kicks","value":4},{"type":"Offsides","value":1},{"type":"Ball Possession","value":"37%"},{"type":"Yellow Cards","value":1},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":7},{"type":"Total passes","value":348},{"type":"Passes accurate","value":269},{"type":"Passes %","value":"77%"},{"type":"expected_goals","value":"0.85"},{"type":"goals_prevented","value":0}]},{"team":{"id":65,"name":"Nottingham Forest","logo":"https:\/\/media.api-sports.io\/football\/teams\/65.png"},"statistics":[{"type":"Shots on Goal","value":8},{"type":"Shots off Goal","value":2},{"type":"Total Shots","value":17},{"type":"Blocked Shots","value":7},{"type":"Shots insidebox","value":9},{"type":"Shots outsidebox","value":8},{"type":"Fouls","value":11},{"type":"Corner Kicks","value":5},{"type":"Offsides","value":2},{"type":"Ball Possession","value":"63%"},{"type":"Yellow Cards","value":1},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":4},{"type":"Total passes","value":628},{"type":"Passes accurate","value":564},{"type":"Passes %","value":"90%"},{"type":"expected_goals","value":"1.10"},{"type":"goals_prevented","value":0}]}]}
BODY END
2025-12-23 15:58:05.775  6193-6299  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379027
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28b7198b6d0b34-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:06 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=FgYQfI3yiiUOhs%2Fjk%2B7%2FdU8jcnK5DTFfyLfAuGrlWjMiujPQv%2FFBLDy6ST4TAuDVOC8%2FZHRCkiX0K3RNkH1%2F%2FlujGGhxeywJyJTNUllosdyXNqbG6hjvqw%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766501885718
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501885690
-> x-envoy-upstream-service-time: 4
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 295
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7169
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379027"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":65,"name":"Nottingham Forest","logo":"https:\/\/media.api-sports.io\/football\/teams\/65.png"},"statistics":[{"type":"Shots on Goal","value":6},{"type":"Shots off Goal","value":10},{"type":"Total Shots","value":22},{"type":"Blocked Shots","value":6},{"type":"Shots insidebox","value":15},{"type":"Shots outsidebox","value":7},{"type":"Fouls","value":11},{"type":"Corner Kicks","value":7},{"type":"Offsides","value":0},{"type":"Ball Possession","value":"65%"},{"type":"Yellow Cards","value":4},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":2},{"type":"Total passes","value":578},{"type":"Passes accurate","value":521},{"type":"Passes %","value":"90%"},{"type":"expected_goals","value":"1.68"},{"type":"goals_prevented","value":1}]},{"team":{"id":746,"name":"Sunderland","logo":"https:\/\/media.api-sports.io\/football\/teams\/746.png"},"statistics":[{"type":"Shots on Goal","value":3},{"type":"Shots off Goal","value":5},{"type":"Total Shots","value":11},{"type":"Blocked Shots","value":3},{"type":"Shots insidebox","value":8},{"type":"Shots outsidebox","value":3},{"type":"Fouls","value":6},{"type":"Corner Kicks","value":4},{"type":"Offsides","value":1},{"type":"Ball Possession","value":"35%"},{"type":"Yellow Cards","value":2},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":6},{"type":"Total passes","value":318},{"type":"Passes accurate","value":258},{"type":"Passes %","value":"81%"},{"type":"expected_goals","value":"1.66"},{"type":"goals_prevented","value":1}]}]}
BODY END
2025-12-23 15:58:05.777  6193-6314  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379095
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28b7198ba90b7f-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:06 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=09QozZcn57f6gqN1%2BcKi5vV6VsS37Xc9kUTODtoYTK7Fssz7QAbmpWx%2BgNLnsj3FDOEKCt7Er6EX%2Fhjs5k%2F6kaB9oIGqWoXMEmDaJhKNzROi11JkVV3IfA%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766501885718
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501885691
-> x-envoy-upstream-service-time: 5
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 294
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7168
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379095"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":65,"name":"Nottingham Forest","logo":"https:\/\/media.api-sports.io\/football\/teams\/65.png"},"statistics":[{"type":"Shots on Goal","value":3},{"type":"Shots off Goal","value":6},{"type":"Total Shots","value":19},{"type":"Blocked Shots","value":10},{"type":"Shots insidebox","value":13},{"type":"Shots outsidebox","value":6},{"type":"Fouls","value":11},{"type":"Corner Kicks","value":9},{"type":"Offsides","value":1},{"type":"Ball Possession","value":"52%"},{"type":"Yellow Cards","value":0},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":3},{"type":"Total passes","value":492},{"type":"Passes accurate","value":409},{"type":"Passes %","value":"83%"},{"type":"expected_goals","value":"0.89"},{"type":"goals_prevented","value":0}]},{"team":{"id":51,"name":"Brighton","logo":"https:\/\/media.api-sports.io\/football\/teams\/51.png"},"statistics":[{"type":"Shots on Goal","value":6},{"type":"Shots off Goal","value":5},{"type":"Total Shots","value":17},{"type":"Blocked Shots","value":6},{"type":"Shots insidebox","value":11},{"type":"Shots outsidebox","value":6},{"type":"Fouls","value":10},{"type":"Corner Kicks","value":2},{"type":"Offsides","value":0},{"type":"Ball Possession","value":"48%"},{"type":"Yellow Cards","value":1},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":3},{"type":"Total passes","value":476},{"type":"Passes accurate","value":392},{"type":"Passes %","value":"82%"},{"type":"expected_goals","value":"2.00"},{"type":"goals_prevented","value":0}]}]}
BODY END
2025-12-23 15:58:05.778  6193-6312  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379037
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28b7198f6d66e5-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:06 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=4DoWL%2FcLHa5N1K8wCXhD%2BcDnT%2BUoDMZ%2BL6XpCi9TIxcip07eaOoE42SwpZSNnm1s1Q7boGReC%2BoGJYjrFJWZecvFIlZOki3byKwpOxqd3ajhektl1G6YgQ%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766501885713
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501885684
-> x-envoy-upstream-service-time: 4
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 296
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7170
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379037"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":34,"name":"Newcastle","logo":"https:\/\/media.api-sports.io\/football\/teams\/34.png"},"statistics":[{"type":"Shots on Goal","value":9},{"type":"Shots off Goal","value":4},{"type":"Total Shots","value":18},{"type":"Blocked Shots","value":5},{"type":"Shots insidebox","value":13},{"type":"Shots outsidebox","value":5},{"type":"Fouls","value":16},{"type":"Corner Kicks","value":8},{"type":"Offsides","value":0},{"type":"Ball Possession","value":"51%"},{"type":"Yellow Cards","value":0},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":4},{"type":"Total passes","value":464},{"type":"Passes accurate","value":387},{"type":"Passes %","value":"83%"},{"type":"expected_goals","value":"3.45"},{"type":"goals_prevented","value":1}]},{"team":{"id":65,"name":"Nottingham Forest","logo":"https:\/\/media.api-sports.io\/football\/teams\/65.png"},"statistics":[{"type":"Shots on Goal","value":4},{"type":"Shots off Goal","value":1},{"type":"Total Shots","value":5},{"type":"Blocked Shots","value":0},{"type":"Shots insidebox","value":4},{"type":"Shots outsidebox","value":1},{"type":"Fouls","value":15},{"type":"Corner Kicks","value":1},{"type":"Offsides","value":2},{"type":"Ball Possession","value":"49%"},{"type":"Yellow Cards","value":4},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":7},{"type":"Total passes","value":452},{"type":"Passes accurate","value":372},{"type":"Passes %","value":"82%"},{"type":"expected_goals","value":"0.30"},{"type":"goals_prevented","value":1}]}]}
BODY END
2025-12-23 15:58:05.781  6193-6308  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379075
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28b719a84628ad-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:07 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=Fjjhr%2F9rCqBo1Q5fgi8%2BjqcgjMx7jEd5Ysn%2FIhd7%2BPOIkjUT1C%2BUSCkTL7CtY1DMeqEmv6lzpY9sel8wN9jvxhV8cKbXTVURIldfMZirmQRGLn3Acoi4Fg%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766501885738
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501885708
-> x-envoy-upstream-service-time: 5
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 290
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7164
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379075"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":65,"name":"Nottingham Forest","logo":"https:\/\/media.api-sports.io\/football\/teams\/65.png"},"statistics":[{"type":"Shots on Goal","value":6},{"type":"Shots off Goal","value":2},{"type":"Total Shots","value":14},{"type":"Blocked Shots","value":6},{"type":"Shots insidebox","value":10},{"type":"Shots outsidebox","value":4},{"type":"Fouls","value":10},{"type":"Corner Kicks","value":6},{"type":"Offsides","value":2},{"type":"Ball Possession","value":"46%"},{"type":"Yellow Cards","value":2},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":2},{"type":"Total passes","value":348},{"type":"Passes accurate","value":277},{"type":"Passes %","value":"80%"},{"type":"expected_goals","value":"2.55"},{"type":"goals_prevented","value":0}]},{"team":{"id":63,"name":"Leeds","logo":"https:\/\/media.api-sports.io\/football\/teams\/63.png"},"statistics":[{"type":"Shots on Goal","value":3},{"type":"Shots off Goal","value":5},{"type":"Total Shots","value":10},{"type":"Blocked Shots","value":2},{"type":"Shots insidebox","value":5},{"type":"Shots outsidebox","value":5},{"type":"Fouls","value":11},{"type":"Corner Kicks","value":4},{"type":"Offsides","value":1},{"type":"Ball Possession","value":"54%"},{"type":"Yellow Cards","value":1},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":3},{"type":"Total passes","value":435},{"type":"Passes accurate","value":353},{"type":"Passes %","value":"81%"},{"type":"expected_goals","value":"0.69"},{"type":"goals_prevented","value":0}]}]}
BODY END
2025-12-23 15:58:05.786  6193-6312  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379108
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28b7199fa495d9-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:06 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=41Pifla21PS1lHyxG6Fl5gXEbK8%2FHz3yYtfkna5jHxjwFnqv1C9cSRXtQYpMdcYgVzDoC6V8%2BbPk5RrK25iyEoWLGgxs4IWBe4%2FOyBRiCQOmldqx71buzg%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766501885726
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501885698
-> x-envoy-upstream-service-time: 5
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 292
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7166
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379108"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":39,"name":"Wolves","logo":"https:\/\/media.api-sports.io\/football\/teams\/39.png"},"statistics":[{"type":"Shots on Goal","value":1},{"type":"Shots off Goal","value":2},{"type":"Total Shots","value":5},{"type":"Blocked Shots","value":2},{"type":"Shots insidebox","value":4},{"type":"Shots outsidebox","value":1},{"type":"Fouls","value":19},{"type":"Corner Kicks","value":3},{"type":"Offsides","value":2},{"type":"Ball Possession","value":"50%"},{"type":"Yellow Cards","value":2},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":2},{"type":"Total passes","value":415},{"type":"Passes accurate","value":329},{"type":"Passes %","value":"79%"},{"type":"expected_goals","value":"0.91"},{"type":"goals_prevented","value":0}]},{"team":{"id":65,"name":"Nottingham Forest","logo":"https:\/\/media.api-sports.io\/football\/teams\/65.png"},"statistics":[{"type":"Shots on Goal","value":3},{"type":"Shots off Goal","value":3},{"type":"Total Shots","value":10},{"type":"Blocked Shots","value":4},{"type":"Shots insidebox","value":7},{"type":"Shots outsidebox","value":3},{"type":"Fouls","value":11},{"type":"Corner Kicks","value":9},{"type":"Offsides","value":2},{"type":"Ball Possession","value":"50%"},{"type":"Yellow Cards","value":2},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":1},{"type":"Total passes","value":416},{"type":"Passes accurate","value":334},{"type":"Passes %","value":"80%"},{"type":"expected_goals","value":"0.74"},{"type":"goals_prevented","value":0}]}]}
BODY END
2025-12-23 15:58:05.788  6193-6274  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379051
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28b7199abeb926-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:06 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=45fdvCZRlcmRcvkKfY%2FsT81j4VE9%2FeSN45xKVhDD0%2Be6wtpPUnUMXeSD2GYlKxpHLcNj0GU40p4%2BzRpH22tE5kTj4obzwV%2FlC0XwGGlsQGMxhHmXMOhCjA%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766501885729
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501885694
-> x-envoy-upstream-service-time: 4
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 293
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7167
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379051"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":35,"name":"Bournemouth","logo":"https:\/\/media.api-sports.io\/football\/teams\/35.png"},"statistics":[{"type":"Shots on Goal","value":5},{"type":"Shots off Goal","value":4},{"type":"Total Shots","value":13},{"type":"Blocked Shots","value":4},{"type":"Shots insidebox","value":5},{"type":"Shots outsidebox","value":8},{"type":"Fouls","value":17},{"type":"Corner Kicks","value":6},{"type":"Offsides","value":3},{"type":"Ball Possession","value":"52%"},{"type":"Yellow Cards","value":3},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":4},{"type":"Total passes","value":447},{"type":"Passes accurate","value":359},{"type":"Passes %","value":"80%"},{"type":"expected_goals","value":"0.58"},{"type":"goals_prevented","value":-1}]},{"team":{"id":65,"name":"Nottingham Forest","logo":"https:\/\/media.api-sports.io\/football\/teams\/65.png"},"statistics":[{"type":"Shots on Goal","value":4},{"type":"Shots off Goal","value":2},{"type":"Total Shots","value":8},{"type":"Blocked Shots","value":2},{"type":"Shots insidebox","value":3},{"type":"Shots outsidebox","value":5},{"type":"Fouls","value":7},{"type":"Corner Kicks","value":4},{"type":"Offsides","value":0},{"type":"Ball Possession","value":"48%"},{"type":"Yellow Cards","value":1},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":3},{"type":"Total passes","value":409},{"type":"Passes accurate","value":318},{"type":"Passes %","value":"78%"},{"type":"expected_goals","value":"0.37"},{"type":"goals_prevented","value":-1}]}]}
BODY END
2025-12-23 15:58:05.792  6193-6273  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379009
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28b719bbcc0b7f-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:07 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=7qrxPzpk7xBSm9ef3fTVKVUgKyyXNoA4n5CbCgDn9uOarSIpBr1bjQt6uDCm9mIRw3jUJiI6ZHjxlNRL9LVmILgF1QG6jiZPJjSSTmAX8m1YyGLbvZs6Qw%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766501885747
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501885719
-> x-envoy-upstream-service-time: 5
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 289
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7163
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379009"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":42,"name":"Arsenal","logo":"https:\/\/media.api-sports.io\/football\/teams\/42.png"},"statistics":[{"type":"Shots on Goal","value":3},{"type":"Shots off Goal","value":6},{"type":"Total Shots","value":12},{"type":"Blocked Shots","value":3},{"type":"Shots insidebox","value":11},{"type":"Shots outsidebox","value":1},{"type":"Fouls","value":11},{"type":"Corner Kicks","value":11},{"type":"Offsides","value":4},{"type":"Ball Possession","value":"67%"},{"type":"Yellow Cards","value":1},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":2},{"type":"Total passes","value":582},{"type":"Passes accurate","value":517},{"type":"Passes %","value":"89%"},{"type":"expected_goals","value":"0.89"},{"type":"goals_prevented","value":0}]},{"team":{"id":50,"name":"Manchester City","logo":"https:\/\/media.api-sports.io\/football\/teams\/50.png"},"statistics":[{"type":"Shots on Goal","value":3},{"type":"Shots off Goal","value":2},{"type":"Total Shots","value":5},{"type":"Blocked Shots","value":0},{"type":"Shots insidebox","value":4},{"type":"Shots outsidebox","value":1},{"type":"Fouls","value":10},{"type":"Corner Kicks","value":1},{"type":"Offsides","value":2},{"type":"Ball Possession","value":"33%"},{"type":"Yellow Cards","value":2},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":2},{"type":"Total passes","value":300},{"type":"Passes accurate","value":229},{"type":"Passes %","value":"76%"},{"type":"expected_goals","value":"0.87"},{"type":"goals_prevented","value":0}]}]}
BODY END
2025-12-23 15:58:05.796  6193-6267  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379065
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28b719ab50796e-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:07 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=MA%2BTthMS%2FEVwI6ituwxqdd44%2Fppz%2BHgCEs72l4tzWPK12KmMimnaM1%2BqYbEcudoJRjhpACO4552S7SYvqytC%2FYKNZUVFOipzogzXuWsTZ4s2169asRHMGg%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766501885735
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501885703
-> x-envoy-upstream-service-time: 5
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 291
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7165
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379065"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":65,"name":"Nottingham Forest","logo":"https:\/\/media.api-sports.io\/football\/teams\/65.png"},"statistics":[{"type":"Shots on Goal","value":3},{"type":"Shots off Goal","value":8},{"type":"Total Shots","value":17},{"type":"Blocked Shots","value":6},{"type":"Shots insidebox","value":11},{"type":"Shots outsidebox","value":6},{"type":"Fouls","value":17},{"type":"Corner Kicks","value":8},{"type":"Offsides","value":2},{"type":"Ball Possession","value":"41%"},{"type":"Yellow Cards","value":1},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":4},{"type":"Total passes","value":350},{"type":"Passes accurate","value":263},{"type":"Passes %","value":"75%"},{"type":"expected_goals","value":"1.93"},{"type":"goals_prevented","value":0}]},{"team":{"id":33,"name":"Manchester United","logo":"https:\/\/media.api-sports.io\/football\/teams\/33.png"},"statistics":[{"type":"Shots on Goal","value":7},{"type":"Shots off Goal","value":6},{"type":"Total Shots","value":18},{"type":"Blocked Shots","value":5},{"type":"Shots insidebox","value":8},{"type":"Shots outsidebox","value":10},{"type":"Fouls","value":0},{"type":"Corner Kicks","value":5},{"type":"Offsides","value":1},{"type":"Ball Possession","value":"59%"},{"type":"Yellow Cards","value":1},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":1},{"type":"Total passes","value":525},{"type":"Passes accurate","value":442},{"type":"Passes %","value":"84%"},{"type":"expected_goals","value":"1.12"},{"type":"goals_prevented","value":0}]}]}
BODY END
2025-12-23 15:58:05.807  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Arsenal (42) in fixture 1379009
2025-12-23 15:58:05.807  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Manchester City (50) in fixture 1379009
2025-12-23 15:58:05.808  6193-6193  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379009 (Arsenal vs Manchester City)
2025-12-23 15:58:05.815  6193-6311  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379025
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28b719cb9d0b34-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:07 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=3WyuZbKZK223Fw2RDDYVCY%2FWl6Selij2rz2Mpp4dqZUCoNTTFjPsv5AK44Z3nwYKC%2Bs9ffwI56dZf6%2FyfuDTOVsvJ5VhNM2wxw31PcikCMOXbMnssfdMzw%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766501885753
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501885725
-> x-envoy-upstream-service-time: 4
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 288
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7162
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379025"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":50,"name":"Manchester City","logo":"https:\/\/media.api-sports.io\/football\/teams\/50.png"},"statistics":[{"type":"Shots on Goal","value":8},{"type":"Shots off Goal","value":7},{"type":"Total Shots","value":21},{"type":"Blocked Shots","value":6},{"type":"Shots insidebox","value":13},{"type":"Shots outsidebox","value":8},{"type":"Fouls","value":5},{"type":"Corner Kicks","value":10},{"type":"Offsides","value":1},{"type":"Ball Possession","value":"69%"},{"type":"Yellow Cards","value":1},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":1},{"type":"Total passes","value":638},{"type":"Passes accurate","value":566},{"type":"Passes %","value":"89%"},{"type":"expected_goals","value":"2.03"},{"type":"goals_prevented","value":-2}]},{"team":{"id":44,"name":"Burnley","logo":"https:\/\/media.api-sports.io\/football\/teams\/44.png"},"statistics":[{"type":"Shots on Goal","value":2},{"type":"Shots off Goal","value":2},{"type":"Total Shots","value":9},{"type":"Blocked Shots","value":5},{"type":"Shots insidebox","value":5},{"type":"Shots outsidebox","value":4},{"type":"Fouls","value":7},{"type":"Corner Kicks","value":2},{"type":"Offsides","value":1},{"type":"Ball Possession","value":"31%"},{"type":"Yellow Cards","value":3},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":5},{"type":"Total passes","value":295},{"type":"Passes accurate","value":242},{"type":"Passes %","value":"82%"},{"type":"expected_goals","value":"0.41"},{"type":"goals_prevented","value":-2}]}]}
BODY END
2025-12-23 15:58:05.819  6193-6300  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1378990
COMMON HEADERS
-> accept-ranges: bytes
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28b719d9906053-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:07 GMT
-> expires: 0
-> last-modified: Thu, 18 Sep 2025 06:58:28 GMT
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=tn81VIU8FKq5pRg0KSi8Bs2b8%2BdnDkVzmG%2FdwiLBwSeEOnAkb2g9UKvR1qRka6gynu9bjS57jruIgKpQIXQCXUC8WX%2FYc9wlCFdwQXbc4IseMmwBNo7Cfg%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766501885769
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501885744
-> x-envoy-upstream-service-time: 0
BODY Content-Type: application/json
BODY START
{"get": "","parameters": [],"errors": {"rateLimit": "Too many requests. You have exceeded the limit of requests per minute of your subscription."},"results": 0,"paging": {"current": 1,"total": 1},"response": []}

                                                                                                    BODY END
2025-12-23 15:58:05.819  6193-6274  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379044
COMMON HEADERS
-> accept-ranges: bytes
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28b719dfc166e5-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:07 GMT
-> expires: 0
-> last-modified: Thu, 18 Sep 2025 06:58:28 GMT
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=zG%2FMxrkNaz0SC%2BE%2BHeaz1VwZ4hf9gWHt1RP3oNqLehMrtCP2lDRLyQMYU6mcB3oA%2F7ejtGjpfP%2BxtXn%2FFN9LwQ1zAOeQlzxcHPUcccltqXicqnej%2FXTKfA%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766501885764
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501885742
-> x-envoy-upstream-service-time: 0
BODY Content-Type: application/json
BODY START
{"get": "","parameters": [],"errors": {"rateLimit": "Too many requests. You have exceeded the limit of requests per minute of your subscription."},"results": 0,"paging": {"current": 1,"total": 1},"response": []}

                                                                                                    BODY END
2025-12-23 15:58:05.822  6193-6306  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379050
COMMON HEADERS
-> accept-ranges: bytes
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28b719ebfa0b7f-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:07 GMT
-> expires: 0
-> last-modified: Thu, 18 Sep 2025 06:58:28 GMT
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=nl7L97umh2YrS073QAlwMe%2FHa7SxfDb0bogLRPP9mMddCnoiQhaiJuZ7Gbl%2B8tYlt1qeYyunguQ1aNbqhW8k67luT0Iv2ffaNLGjW%2F6P7zcitvQV6OlTAA%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766501885770
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501885748
-> x-envoy-upstream-service-time: 0
BODY Content-Type: application/json
BODY START
{"get": "","parameters": [],"errors": {"rateLimit": "Too many requests. You have exceeded the limit of requests per minute of your subscription."},"results": 0,"paging": {"current": 1,"total": 1},"response": []}

                                                                                                    BODY END
2025-12-23 15:58:05.822  6193-6269  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379032
COMMON HEADERS
-> accept-ranges: bytes
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28b719d85a95d9-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:07 GMT
-> expires: 0
-> last-modified: Thu, 18 Sep 2025 06:58:28 GMT
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=3sZ0XqzbtfMgg5w6rdhFWJuM5szZjbnI8l1PBAJDM7%2BynodO15QnR%2BS%2BQtmPcdk2XRIb8jkvik6Zr5RSc9cgjIkywyI78bwRD8l5UFew8JmhuH8SfvmHcA%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766501885759
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501885736
-> x-envoy-upstream-service-time: 0
BODY Content-Type: application/json
BODY START
{"get": "","parameters": [],"errors": {"rateLimit": "Too many requests. You have exceeded the limit of requests per minute of your subscription."},"results": 0,"paging": {"current": 1,"total": 1},"response": []}

                                                                                                    BODY END
2025-12-23 15:58:05.822  6193-6314  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379133
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28b719dc97fea4-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:07 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=5kFIlm%2BwMO943sHm39qj9nnHJdtv13IXQi6nJRNU9t4Nt7t1GVG6o4EseOu9i%2FJtntLN7wc1mcO029FrF3mmEgTT2KGBe%2F2S7UxmKvZq77qWvYfaq%2BX%2FHg%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766501885769
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501885736
-> x-envoy-upstream-service-time: 4
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 287
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7161
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379133"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":36,"name":"Fulham","logo":"https:\/\/media.api-sports.io\/football\/teams\/36.png"},"statistics":[{"type":"Shots on Goal","value":1},{"type":"Shots off Goal","value":4},{"type":"Total Shots","value":11},{"type":"Blocked Shots","value":6},{"type":"Shots insidebox","value":8},{"type":"Shots outsidebox","value":3},{"type":"Fouls","value":5},{"type":"Corner Kicks","value":1},{"type":"Offsides","value":2},{"type":"Ball Possession","value":"50%"},{"type":"Yellow Cards","value":5},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":2},{"type":"Total passes","value":495},{"type":"Passes accurate","value":396},{"type":"Passes %","value":"80%"},{"type":"expected_goals","value":"1.50"},{"type":"goals_prevented","value":0}]},{"team":{"id":65,"name":"Nottingham Forest","logo":"https:\/\/media.api-sports.io\/football\/teams\/65.png"},"statistics":[{"type":"Shots on Goal","value":2},{"type":"Shots off Goal","value":6},{"type":"Total Shots","value":12},{"type":"Blocked Shots","value":4},{"type":"Shots insidebox","value":6},{"type":"Shots outsidebox","value":6},{"type":"Fouls","value":9},{"type":"Corner Kicks","value":5},{"type":"Offsides","value":2},{"type":"Ball Possession","value":"50%"},{"type":"Yellow Cards","value":4},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":0},{"type":"Total passes","value":490},{"type":"Passes accurate","value":417},{"type":"Passes %","value":"85%"},{"type":"expected_goals","value":"0.67"},{"type":"goals_prevented","value":0}]}]}
BODY END
2025-12-23 15:58:05.823  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Crystal Palace (52) in fixture 1378983
2025-12-23 15:58:05.823  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Nottingham Forest (65) in fixture 1378983
2025-12-23 15:58:05.823  6193-6193  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1378983 (Crystal Palace vs Nottingham Forest)
2025-12-23 15:58:05.826  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Burnley (44) in fixture 1379012
2025-12-23 15:58:05.826  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Nottingham Forest (65) in fixture 1379012
2025-12-23 15:58:05.826  6193-6193  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379012 (Burnley vs Nottingham Forest)
2025-12-23 15:58:05.828  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Nottingham Forest (65) in fixture 1379095
2025-12-23 15:58:05.828  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Brighton (51) in fixture 1379095
2025-12-23 15:58:05.828  6193-6193  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379095 (Nottingham Forest vs Brighton)
2025-12-23 15:58:05.830  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Nottingham Forest (65) in fixture 1379027
2025-12-23 15:58:05.830  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Sunderland (746) in fixture 1379027
2025-12-23 15:58:05.830  6193-6193  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379027 (Nottingham Forest vs Sunderland)
2025-12-23 15:58:05.832  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Newcastle (34) in fixture 1379037
2025-12-23 15:58:05.832  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Nottingham Forest (65) in fixture 1379037
2025-12-23 15:58:05.832  6193-6193  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379037 (Newcastle vs Nottingham Forest)
2025-12-23 15:58:05.834  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Bournemouth (35) in fixture 1379051
2025-12-23 15:58:05.834  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Nottingham Forest (65) in fixture 1379051
2025-12-23 15:58:05.834  6193-6193  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379051 (Bournemouth vs Nottingham Forest)
2025-12-23 15:58:05.837  6193-6306  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379064
COMMON HEADERS
-> accept-ranges: bytes
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28b719eb04b926-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:07 GMT
-> expires: 0
-> last-modified: Thu, 18 Sep 2025 06:58:28 GMT
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=RcNUGpMGAdneR%2BWLt8bljG3zfkjf6quVBAgXwlbh98nhaqYoWVHOvxETk%2BIQhSvqVnI2za7pbfuHU2YgRF4EBmFM%2Bc5WyLGq6GDB%2FOK0ii9I1pmiseDkug%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766501885779
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501885753
-> x-envoy-upstream-service-time: 0
BODY Content-Type: application/json
BODY START
{"get": "","parameters": [],"errors": {"rateLimit": "Too many requests. You have exceeded the limit of requests per minute of your subscription."},"results": 0,"paging": {"current": 1,"total": 1},"response": []}

                                                                                                    BODY END
2025-12-23 15:58:05.840  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Nottingham Forest (65) in fixture 1379075
2025-12-23 15:58:05.840  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Leeds (63) in fixture 1379075
2025-12-23 15:58:05.840  6193-6193  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379075 (Nottingham Forest vs Leeds)
2025-12-23 15:58:05.842  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Wolves (39) in fixture 1379108
2025-12-23 15:58:05.842  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Nottingham Forest (65) in fixture 1379108
2025-12-23 15:58:05.842  6193-6193  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379108 (Wolves vs Nottingham Forest)
2025-12-23 15:58:05.846  6193-6300  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379094
COMMON HEADERS
-> accept-ranges: bytes
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28b719f8cb95d9-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:07 GMT
-> expires: 0
-> last-modified: Thu, 18 Sep 2025 06:58:28 GMT
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=bSzSfm7TrQQ6wNsCMyH9acahhkWLP4ftJygUUNwL%2BEYmG29dd8phUXktIusxYw7vKNIwulcOPzIhKf1SLWgBq8rGT8gCQ%2FqivIyb3TpcGVlJOUr3r5HEig%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766501885784
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501885762
-> x-envoy-upstream-service-time: 0
BODY Content-Type: application/json
BODY START
{"get": "","parameters": [],"errors": {"rateLimit": "Too many requests. You have exceeded the limit of requests per minute of your subscription."},"results": 0,"paging": {"current": 1,"total": 1},"response": []}

                                                                                                    BODY END
2025-12-23 15:58:05.846  6193-6275  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379087
COMMON HEADERS
-> accept-ranges: bytes
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28b719fbcb0b34-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:07 GMT
-> expires: 0
-> last-modified: Thu, 18 Sep 2025 06:58:28 GMT
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=qCRbqPJDxKydBQVdU0Zqbf8ZG%2Fa0E3O2%2BV6h%2BxRQvCPYaEHnJedHLklG%2Bc2jkm2eDH3%2FoQh2ztv5EVYZyeeVVse1KfO0z99Bb3FTvskMCFxLuEZYu7VusQ%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766501885784
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501885758
-> x-envoy-upstream-service-time: 0
BODY Content-Type: application/json
BODY START
{"get": "","parameters": [],"errors": {"rateLimit": "Too many requests. You have exceeded the limit of requests per minute of your subscription."},"results": 0,"paging": {"current": 1,"total": 1},"response": []}

                                                                                                    BODY END
2025-12-23 15:58:05.846  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Nottingham Forest (65) in fixture 1379065
2025-12-23 15:58:05.846  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Manchester United (33) in fixture 1379065
2025-12-23 15:58:05.846  6193-6193  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379065 (Nottingham Forest vs Manchester United)
2025-12-23 15:58:05.847  6193-6269  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379103
COMMON HEADERS
-> accept-ranges: bytes
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28b71a0ff566e5-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:07 GMT
-> expires: 0
-> last-modified: Thu, 18 Sep 2025 06:58:28 GMT
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=WYfURSP2Y6e%2B9fi69%2F39jd4EjAUTmadh6q6HELas%2BTzMh2s0O872O8R8PItaCFYuasix8Jh3NLB1UqNZqSIgizhwmfHwAixkmqCpvdDJ4JIroGJHOb5rEw%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766501885794
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501885767
-> x-envoy-upstream-service-time: 0
BODY Content-Type: application/json
BODY START
{"get": "","parameters": [],"errors": {"rateLimit": "Too many requests. You have exceeded the limit of requests per minute of your subscription."},"results": 0,"paging": {"current": 1,"total": 1},"response": []}

                                                                                                    BODY END
2025-12-23 15:58:05.852  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Manchester City (50) in fixture 1379025
2025-12-23 15:58:05.852  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Burnley (44) in fixture 1379025
2025-12-23 15:58:05.852  6193-6193  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379025 (Manchester City vs Burnley)
2025-12-23 15:58:05.855  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE https://v3.football.api-sports.io/fixtures/statistics?fixture=1378990 failed with exception: io.ktor.serialization.JsonConvertException: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 15:58:05.855  6193-6193  MatchRepository         com.Lyno.matchmindai                 W  ⚠️ Unexpected error for fixture 1378990: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 15:58:05.856  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Unexpected error for fixture 1378990: JsonConvertException
2025-12-23 15:58:05.858  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE https://v3.football.api-sports.io/fixtures/statistics?fixture=1379044 failed with exception: io.ktor.serialization.JsonConvertException: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 15:58:05.858  6193-6193  MatchRepository         com.Lyno.matchmindai                 W  ⚠️ Unexpected error for fixture 1379044: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 15:58:05.858  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Unexpected error for fixture 1379044: JsonConvertException
2025-12-23 15:58:05.859  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE https://v3.football.api-sports.io/fixtures/statistics?fixture=1379050 failed with exception: io.ktor.serialization.JsonConvertException: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 15:58:05.860  6193-6193  MatchRepository         com.Lyno.matchmindai                 W  ⚠️ Unexpected error for fixture 1379050: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 15:58:05.860  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Unexpected error for fixture 1379050: JsonConvertException
2025-12-23 15:58:05.862  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Fulham (36) in fixture 1379133
2025-12-23 15:58:05.862  6193-6193  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Nottingham Forest (65) in fixture 1379133
2025-12-23 15:58:05.862  6193-6193  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379133 (Fulham vs Nottingham Forest)
2025-12-23 15:58:05.864  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE https://v3.football.api-sports.io/fixtures/statistics?fixture=1379032 failed with exception: io.ktor.serialization.JsonConvertException: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 15:58:05.864  6193-6193  MatchRepository         com.Lyno.matchmindai                 W  ⚠️ Unexpected error for fixture 1379032: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 15:58:05.864  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Unexpected error for fixture 1379032: JsonConvertException
2025-12-23 15:58:05.866  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE https://v3.football.api-sports.io/fixtures/statistics?fixture=1379064 failed with exception: io.ktor.serialization.JsonConvertException: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 15:58:05.866  6193-6193  MatchRepository         com.Lyno.matchmindai                 W  ⚠️ Unexpected error for fixture 1379064: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 15:58:05.866  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Unexpected error for fixture 1379064: JsonConvertException
2025-12-23 15:58:05.867  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE https://v3.football.api-sports.io/fixtures/statistics?fixture=1379094 failed with exception: io.ktor.serialization.JsonConvertException: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 15:58:05.867  6193-6193  MatchRepository         com.Lyno.matchmindai                 W  ⚠️ Unexpected error for fixture 1379094: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 15:58:05.868  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Unexpected error for fixture 1379094: JsonConvertException
2025-12-23 15:58:05.869  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE https://v3.football.api-sports.io/fixtures/statistics?fixture=1379087 failed with exception: io.ktor.serialization.JsonConvertException: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 15:58:05.869  6193-6193  MatchRepository         com.Lyno.matchmindai                 W  ⚠️ Unexpected error for fixture 1379087: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 15:58:05.869  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Unexpected error for fixture 1379087: JsonConvertException
2025-12-23 15:58:05.870  6193-6193  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE https://v3.football.api-sports.io/fixtures/statistics?fixture=1379103 failed with exception: io.ktor.serialization.JsonConvertException: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 15:58:05.870  6193-6193  MatchRepository         com.Lyno.matchmindai                 W  ⚠️ Unexpected error for fixture 1379103: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 15:58:05.870  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Unexpected error for fixture 1379103: JsonConvertException
2025-12-23 15:58:05.870  6193-6193  DataTrace               com.Lyno.matchmindai                 D  ✅ Successfully enriched 0 matches with xG data (out of 20 total)
2025-12-23 15:58:05.870  6193-6193  DataTrace               com.Lyno.matchmindai                 W  ⚠️ WARNING: No xG data found for any of the 20 fixtures
2025-12-23 15:58:05.871  6193-6193  DataTrace               com.Lyno.matchmindai                 D  Possible causes: API rate limit, missing statistics, or league doesn't track xG
2025-12-23 15:58:05.871  6193-6193  DataTrace               com.Lyno.matchmindai                 D  xG data available for 0 fixtures
2025-12-23 15:58:05.871  6193-6193  MatchDetailViewModel    com.Lyno.matchmindai                 D  Historical data loaded: total=151, home=11, away=12
2025-12-23 15:58:05.873  6193-6276  EnhancedScorePredictor  com.Lyno.matchmindai                 D  🔎 ANALYZING: Nottingham Forest vs Manchester City
2025-12-23 15:58:05.874  6193-6276  EnhancedScorePredictor  com.Lyno.matchmindai                 D  📊 History Size: Home=11 matches, Away=12 matches
2025-12-23 15:58:05.874  6193-6276  System.out              com.Lyno.matchmindai                 I  🔒 PREDICTOR ANALYZING: Nottingham Forest vs Manchester City (from historical fixtures)
2025-12-23 15:58:05.874  6193-6276  EnhancedScorePredictor  com.Lyno.matchmindai                 D  STATE-OF-THE-ART PREDICTION: homeFixtures=11, awayFixtures=12, leagueFixtures=151, xgData=0 fixtures
2025-12-23 15:58:05.875  6193-6276  ExpectedGoalsService    com.Lyno.matchmindai                 D  League Averages Data Sources: totalFixtures=151, homeAvg=1.57, awayAvg=1.24, sources=[GOALS_FALLBACK: 151 (100.0%)]
2025-12-23 15:58:05.875  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1378972: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.877  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.877  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.038 (timeWeight=0.192, qualityWeight=0.500)
2025-12-23 15:58:05.877  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1378972: dataSource=GOALS_FALLBACK, inputScore=3.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.879  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1378983: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.879  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.879  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.042 (timeWeight=0.210, qualityWeight=0.500)
2025-12-23 15:58:05.879  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1378983: dataSource=GOALS_FALLBACK, inputScore=1.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.880  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379012: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.881  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.881  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.059 (timeWeight=0.297, qualityWeight=0.500)
2025-12-23 15:58:05.881  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379012: dataSource=GOALS_FALLBACK, inputScore=1.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.882  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379027: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.882  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.883  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.065 (timeWeight=0.324, qualityWeight=0.500)
2025-12-23 15:58:05.883  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379027: dataSource=GOALS_FALLBACK, inputScore=0.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.883  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379037: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.885  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.885  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.072 (timeWeight=0.359, qualityWeight=0.500)
2025-12-23 15:58:05.886  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379037: dataSource=GOALS_FALLBACK, inputScore=2.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.886  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379051: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.886  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.887  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.094 (timeWeight=0.471, qualityWeight=0.500)
2025-12-23 15:58:05.887  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379051: dataSource=GOALS_FALLBACK, inputScore=2.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.887  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379065: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.887  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.888  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.102 (timeWeight=0.509, qualityWeight=0.500)
2025-12-23 15:58:05.888  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379065: dataSource=GOALS_FALLBACK, inputScore=2.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.888  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379075: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.889  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.890  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.113 (timeWeight=0.564, qualityWeight=0.500)
2025-12-23 15:58:05.890  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379075: dataSource=GOALS_FALLBACK, inputScore=3.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.890  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379095: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.891  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.891  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.148 (timeWeight=0.738, qualityWeight=0.500)
2025-12-23 15:58:05.891  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379095: dataSource=GOALS_FALLBACK, inputScore=0.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.891  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379108: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.891  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.892  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.153 (timeWeight=0.767, qualityWeight=0.500)
2025-12-23 15:58:05.892  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379108: dataSource=GOALS_FALLBACK, inputScore=0.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.892  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379133: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.893  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.893  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.196 (timeWeight=0.979, qualityWeight=0.500)
2025-12-23 15:58:05.894  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379133: dataSource=GOALS_FALLBACK, inputScore=1.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.895  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1378975: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.895  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.896  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.038 (timeWeight=0.189, qualityWeight=0.500)
2025-12-23 15:58:05.896  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1378975: dataSource=GOALS_FALLBACK, inputScore=4.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.896  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1378986: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.897  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.898  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.041 (timeWeight=0.207, qualityWeight=0.500)
2025-12-23 15:58:05.898  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1378986: dataSource=GOALS_FALLBACK, inputScore=2.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.898  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1378990: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.899  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.899  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.046 (timeWeight=0.229, qualityWeight=0.500)
2025-12-23 15:58:05.900  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1378990: dataSource=GOALS_FALLBACK, inputScore=1.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.901  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379009: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.901  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.902  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.060 (timeWeight=0.300, qualityWeight=0.500)
2025-12-23 15:58:05.902  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379009: dataSource=GOALS_FALLBACK, inputScore=1.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.903  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379025: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.903  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.904  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.065 (timeWeight=0.324, qualityWeight=0.500)
2025-12-23 15:58:05.904  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379025: dataSource=GOALS_FALLBACK, inputScore=1.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.904  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379032: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.906  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.906  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.072 (timeWeight=0.359, qualityWeight=0.500)
2025-12-23 15:58:05.906  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379032: dataSource=GOALS_FALLBACK, inputScore=1.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.906  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379044: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.906  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.906  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.085 (timeWeight=0.425, qualityWeight=0.500)
2025-12-23 15:58:05.906  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379044: dataSource=GOALS_FALLBACK, inputScore=0.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.907  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379050: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.909  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.909  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.094 (timeWeight=0.471, qualityWeight=0.500)
2025-12-23 15:58:05.909  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379050: dataSource=GOALS_FALLBACK, inputScore=0.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.909  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379064: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.910  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.910  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.103 (timeWeight=0.515, qualityWeight=0.500)
2025-12-23 15:58:05.910  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379064: dataSource=GOALS_FALLBACK, inputScore=1.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.911  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379087: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.911  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.911  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.133 (timeWeight=0.666, qualityWeight=0.500)
2025-12-23 15:58:05.912  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379087: dataSource=GOALS_FALLBACK, inputScore=1.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.912  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379094: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.912  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.912  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.146 (timeWeight=0.729, qualityWeight=0.500)
2025-12-23 15:58:05.913  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379094: dataSource=GOALS_FALLBACK, inputScore=2.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.913  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379103: No xG data available. Score variance may be inflated.
2025-12-23 15:58:05.914  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 15:58:05.914  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.151 (timeWeight=0.757, qualityWeight=0.500)
2025-12-23 15:58:05.914  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379103: dataSource=GOALS_FALLBACK, inputScore=5.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 15:58:05.914  6193-6275  System.out              com.Lyno.matchmindai                 I  [DEBUG] Bayesian Smoothing (C=2.0):
2025-12-23 15:58:05.914  6193-6275  System.out              com.Lyno.matchmindai                 I    isAttack=true, totalWeightedXg=1.286
2025-12-23 15:58:05.914  6193-6275  System.out              com.Lyno.matchmindai                 I    totalMatches=1.082, smoothedStrength=1.436
2025-12-23 15:58:05.914  6193-6275  System.out              com.Lyno.matchmindai                 I    normalizedStrength=0.915
2025-12-23 15:58:05.915  6193-6275  System.out              com.Lyno.matchmindai                 I  [DEBUG] Bayesian Smoothing (C=2.0):
2025-12-23 15:58:05.915  6193-6275  System.out              com.Lyno.matchmindai                 I    isAttack=false, totalWeightedXg=1.127
2025-12-23 15:58:05.915  6193-6275  System.out              com.Lyno.matchmindai                 I    totalMatches=1.082, smoothedStrength=1.170
2025-12-23 15:58:05.916  6193-6275  System.out              com.Lyno.matchmindai                 I    normalizedStrength=0.944
2025-12-23 15:58:05.916  6193-6275  System.out              com.Lyno.matchmindai                 I  [DEBUG] Bayesian Smoothing (C=2.0):
2025-12-23 15:58:05.916  6193-6275  System.out              com.Lyno.matchmindai                 I    isAttack=true, totalWeightedXg=1.762
2025-12-23 15:58:05.916  6193-6275  System.out              com.Lyno.matchmindai                 I    totalMatches=1.034, smoothedStrength=1.397
2025-12-23 15:58:05.917  6193-6275  System.out              com.Lyno.matchmindai                 I    normalizedStrength=1.128
2025-12-23 15:58:05.918  6193-6275  System.out              com.Lyno.matchmindai                 I  [DEBUG] Bayesian Smoothing (C=2.0):
2025-12-23 15:58:05.918  6193-6275  System.out              com.Lyno.matchmindai                 I    isAttack=false, totalWeightedXg=2.060
2025-12-23 15:58:05.918  6193-6275  System.out              com.Lyno.matchmindai                 I    totalMatches=1.034, smoothedStrength=1.713
2025-12-23 15:58:05.919  6193-6275  System.out              com.Lyno.matchmindai                 I    normalizedStrength=1.092
2025-12-23 15:58:05.919  6193-6275  EnhancedScorePredictor  com.Lyno.matchmindai                 D  ⚔️ Home Attack Strength: Raw=-0.278 -> Bayesian=-0.089
2025-12-23 15:58:05.919  6193-6275  EnhancedScorePredictor  com.Lyno.matchmindai                 D  🛡️ Home Defense Strength: Raw=-0.172 -> Bayesian=-0.057
2025-12-23 15:58:05.920  6193-6275  EnhancedScorePredictor  com.Lyno.matchmindai                 D  ⚔️ Away Attack Strength: Raw=0.319 -> Bayesian=0.120
2025-12-23 15:58:05.920  6193-6275  EnhancedScorePredictor  com.Lyno.matchmindai                 D  🛡️ Away Defense Strength: Raw=0.238 -> Bayesian=0.088
2025-12-23 15:58:05.921  6193-6275  DataTrace               com.Lyno.matchmindai                 D  Home Attack Delta: -0.278 (If close to 0.0, team is considered average)
2025-12-23 15:58:05.921  6193-6275  DataTrace               com.Lyno.matchmindai                 D  Away Attack Delta: 0.319 (If close to 0.0, team is considered average)
2025-12-23 15:58:05.921  6193-6275  DataTrace               com.Lyno.matchmindai                 D  Smoothing: Raw=0.757 -> Smoothed=0.915 (Impact: 120%)
2025-12-23 15:58:05.921  6193-6275  DataTrace               com.Lyno.matchmindai                 D  Smoothing (Away): Raw=1.375 -> Smoothed=1.128 (Impact: 82%)
2025-12-23 15:58:05.921  6193-6275  System.out              com.Lyno.matchmindai                 I  [DEBUG] Raw Team Strengths (log scale):
2025-12-23 15:58:05.921  6193-6275  System.out              com.Lyno.matchmindai                 I    Home Attack: -0.089
2025-12-23 15:58:05.921  6193-6275  System.out              com.Lyno.matchmindai                 I    Home Defense: -0.057
2025-12-23 15:58:05.922  6193-6275  System.out              com.Lyno.matchmindai                 I    Away Attack: 0.120
2025-12-23 15:58:05.922  6193-6275  System.out              com.Lyno.matchmindai                 I    Away Defense: 0.088
2025-12-23 15:58:05.922  6193-6275  System.out              com.Lyno.matchmindai                 I  [DEBUG] Raw Strength vs Bayesian Strength (C=2.0):
2025-12-23 15:58:05.922  6193-6275  System.out              com.Lyno.matchmindai                 I    Home Attack: Raw=-0.278 vs Bayesian=-0.089
2025-12-23 15:58:05.922  6193-6275  System.out              com.Lyno.matchmindai                 I    Home Defense: Raw=-0.172 vs Bayesian=-0.057
2025-12-23 15:58:05.922  6193-6275  System.out              com.Lyno.matchmindai                 I    Away Attack: Raw=0.319 vs Bayesian=0.120
2025-12-23 15:58:05.923  6193-6275  System.out              com.Lyno.matchmindai                 I    Away Defense: Raw=0.238 vs Bayesian=0.088
2025-12-23 15:58:05.923  6193-6275  EnhancedScorePredictor  com.Lyno.matchmindai                 D  Home advantage calculation: leagueHomeAvg=1.57, leagueAwayAvg=1.24, ratio=1.27, adjustedRatio=1.27
2025-12-23 15:58:05.926  6193-6275  DataTrace               com.Lyno.matchmindai                 D  Stats for Team 65: xG_Coverage=0.00, Raw_Goals=1.19, Data_Quality=0.05
2025-12-23 15:58:05.926  6193-6275  DataTrace               com.Lyno.matchmindai                 W  ⚠️ LOW DATA QUALITY WARNING: Team 65 has qualityScore=0.05 (< 0.2). Model may ignore this data.
2025-12-23 15:58:05.926  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Quality Analysis: fixtures=11, xgCoverage=0.0%, avgQuality=0.12, combinedQuality=0.05
2025-12-23 15:58:05.926  6193-6275  DataTrace               com.Lyno.matchmindai                 D  Stats for Team 39: xG_Coverage=0.00, Raw_Goals=1.70, Data_Quality=0.05
2025-12-23 15:58:05.926  6193-6275  DataTrace               com.Lyno.matchmindai                 W  ⚠️ LOW DATA QUALITY WARNING: Team 39 has qualityScore=0.05 (< 0.2). Model may ignore this data.
2025-12-23 15:58:05.927  6193-6275  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Quality Analysis: fixtures=12, xgCoverage=0.0%, avgQuality=0.12, combinedQuality=0.05
2025-12-23 15:58:05.927  6193-6275  System.out              com.Lyno.matchmindai                 I  [DEBUG] Adjusted Team Strengths (exp scale):
2025-12-23 15:58:05.927  6193-6275  System.out              com.Lyno.matchmindai                 I    Home Attack: 0.915
2025-12-23 15:58:05.927  6193-6275  System.out              com.Lyno.matchmindai                 I    Home Defense: 0.944
2025-12-23 15:58:05.927  6193-6275  System.out              com.Lyno.matchmindai                 I    Away Attack: 1.128
2025-12-23 15:58:05.927  6193-6275  System.out              com.Lyno.matchmindai                 I    Away Defense: 1.092
2025-12-23 15:58:05.927  6193-6275  System.out              com.Lyno.matchmindai                 I    Home Advantage: 1.267
2025-12-23 15:58:05.929  6193-6275  EnhancedScorePredictor  com.Lyno.matchmindai                 D  🧮 Lambda Calculation: Attack(0.915) * Defense(1.092) * LeagueAvg(1.570) * HomeAdv(1.267) = 1.987
2025-12-23 15:58:05.930  6193-6275  EnhancedScorePredictor  com.Lyno.matchmindai                 D  🧮 Lambda Calculation (Away): Attack(1.128) * Defense(0.944) * LeagueAvg(1.238) = 1.319
2025-12-23 15:58:05.930  6193-6275  System.out              com.Lyno.matchmindai                 I  [DEBUG] Raw Expected Goals (before soft-cap):
2025-12-23 15:58:05.930  6193-6275  System.out              com.Lyno.matchmindai                 I    Home: 1.987
2025-12-23 15:58:05.930  6193-6275  System.out              com.Lyno.matchmindai                 I    Away: 1.319
2025-12-23 15:58:05.930  6193-6275  DataTrace               com.Lyno.matchmindai                 D  📊 RANK CORRECTION: No rank data available, skipping correction
2025-12-23 15:58:05.931  6193-6275  EnhancedScorePredictor  com.Lyno.matchmindai                 D  🧢 Soft-Cap Check: Raw Lambda Home=1.987 -> Capped Lambda Home=1.987 (NO_CAP)
2025-12-23 15:58:05.931  6193-6275  EnhancedScorePredictor  com.Lyno.matchmindai                 D  🧢 Soft-Cap Check: Raw Lambda Away=1.319 -> Capped Lambda Away=1.319 (NO_CAP)
2025-12-23 15:58:05.931  6193-6275  System.out              com.Lyno.matchmindai                 I  [DEBUG] Capped Expected Goals (after soft-cap):
2025-12-23 15:58:05.931  6193-6275  System.out              com.Lyno.matchmindai                 I    Home: 1.987 (capped: NO)
2025-12-23 15:58:05.931  6193-6275  System.out              com.Lyno.matchmindai                 I    Away: 1.319 (capped: NO)
2025-12-23 15:58:05.932  6193-6275  EnhancedScorePredictor  com.Lyno.matchmindai                 D  STATE-OF-THE-ART Probability Capping: Raw=0.50-0.26-0.25, Capped=0.50-0.26-0.25
2025-12-23 15:58:05.934  6193-6275  EnhancedScorePredictor  com.Lyno.matchmindai                 D  STATE-OF-THE-ART RESULTS: expectedGoals=1.99-1.32, probabilities=0.50-0.26-0.25, confidence=0.27, hasModifiers=false, lambdaSoftCap=NOT_NEEDED
2025-12-23 15:58:05.938  6193-6275  NewsImpactAnalyzer      com.Lyno.matchmindai                 D  Future match detected. Using mock news instead of Tavily search.
2025-12-23 15:58:05.939  6193-6275  NewsImpactAnalyzer      com.Lyno.matchmindai                 D  Using mock news for match: Nottingham Forest vs Manchester City
2025-12-23 15:58:05.962  6193-6299  Ktor-Streaming          com.Lyno.matchmindai                 D  REQUEST: https://api.deepseek.com/chat/completions
METHOD: HttpMethod(value=POST)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Authorization: Bearer sk-fcf6a8d664d043d29a9a39cb914adaa9
CONTENT HEADERS
-> Content-Length: 7360
-> Content-Type: application/json
BODY Content-Type: application/json
BODY START
{
"model": "deepseek-chat",
"messages": [
{
"role": "user",
2025-12-23 15:58:05.962  6193-6299  Ktor-Streaming          com.Lyno.matchmindai                 D              "content": "            \nJIJ BENT: Een Senior Data Scientist gespecialiseerd in voetbalmodellen.\nDOEL: Vertaal kwalitatief nieuws naar kwantitatieve model-modifiers.\n\nINPUT DATA:\n1. Teamnamen & Competitie\n2. Basis Voorspelling (Win/Gelijk/Verlies %)\n3. Nieuws Snippets (Blessures, Ruzies, Transfers)\n\nOUTPUT REGELS (JSON):\n- Modifiers moeten tussen 0.5 (catastrofaal) en 1.5 (perfect) liggen.\n- 1.0 is neutraal (geen nieuws = 1.0).\n- 'confidence': Hoe zeker ben je van het nieuws? (0.0 - 1.0).\n- 'chaos_factor': 0.0 (Saai) tot 1.0 (Totale chaos/Derby).\n\nRANGE GUIDE:\n- 0.85 - 0.95: Belangrijke speler twijfelachtig/lichte blessure.\n- 0.70 - 0.80: Sterspeler definitief afwezig.\n- < 0.70: Meerdere sleutelspelers weg / Team in crisis.\n- > 1.10: Team in topvorm / Manager 'bounce' effect.\n\nBELANGRIJK:\n- Wees CONSERVATIEF. Bij twijfel, houd modifiers dicht bij 1.0.\n- Hallucineer GEEN blessures die niet in de snippets staan.\n\n            \n            SPECIFIEKE MATCH CONTEXT:\n            MATCH: Nottingham Forest vs Manchester City (Premier League)\n            \n            STATISTISCHE BASELINE (Dixon-Coles Model):\n            - Thuis Winst: 49%\n            - Gelijk: 25%\n            - Uit Winst: 24%\n            - Verwachte Goals: 2.0 - 1.3\n            - xG Dominantie: Nottingham Forest domineert offensief\n            \n            NIEUWS CONTEXT (Tavily Search):\n            • Nottingham Forest en Manchester City bereiden zich voor op belangrijke Premier League confrontatie\n• Beide teams rapporteren volledige fitheid voor aanstaande wedstrijd\n• Coaches benadrukken tactisch belang van deze wedstrijd\n• Geen nieuwe blessures gemeld in aanloop naar Nottingham Forest vs Manchester City\n• Weersvoorspelling: Ideale omstandigheden voor voetbal\n            \n            ADVANCED FEATURE ENGINEERING TAAK:\n            Jij bent een Senior Data Scientist die kwalitatieve nieuwsdata vertaalt naar kwantitatieve Dixon-Coles parameters.\n            \n            ANALYSE HIERARCHIE (volgorde van belangrijkheid):\n            1. SPELER BESCHIKBAARHEID (hoogste impact):\n               - Sleutelspelers geblesseerd/schorsing? (aanval/verdediging/middenveld)\n               - % basisopstelling beschikbaar (0-100%)\n               - Vervangers kwaliteit t.o.v. basisspeler\n               \n            2. TACTISCHE CONFIGURATIE:\n               - Formatiewijziging (bijv. 4-3-3 → 5-3-2 defensief)\n               - Coach tactiek (hoog druk vs laag blok)\n               - Spelstijl match-up (counter vs possession)\n               \n            3. PSYCHOLOGISCHE FACTOREN:\n               - Wedstrijd belang (kampioenschap/degradatie/derby)\n               - Recente vorm (winst/verlies reeks)\n               - Thuispubliek impact (fortress vs zwak thuis)\n               \n            4. EXTERNE VARIABELEN:\n               - Weersomstandigheden (regen/wind/temperatuur)\n               - Veldomstandigheden (kunstgras/nat)\n               - Reisvermoeidheid (lange reis/kort rust)\n               \n            QUANTITATIVE MODIFIER LOGICA:\n            Voor ELKE modifier (home_attack_mod, home_defense_mod, away_attack_mod, away_defense_mod):\n            \n            IMPACT SCALE (0.5 - 1.5):\n            • 0.50-0.65: Catastrofaal (3+ sleutelspelers weg, team in crisis)\n            • 0.66-0.79: Ernstig (2 sleutelspelers weg, belangrijke tactische mismatch)\n            • 0.80-0.89: Matig (1 sleutelspeler twijfelachtig, kleine tactische nadeel)\n            • 0.90-0.95: Licht (kleine blessure, vermoeidheid, kleine nadeel)\n            • 0.96-1.04: Neutraal (geen impact, standaard verwachting)\n            • 1.05-1.10: Licht positief (motivatie, kleine thuispubliek voordeel)\n            • 1.11-1.20: Matig positief (sterke vorm, tactisch voordeel)\n            • 1.21-1.35: Sterk positief (perfecte omstandigheden, alles mee)\n            • 1.36-1.50: Extreem positief (historisch voordeel, alles perfect)\n        
2025-12-23 15:58:05.962  6193-6299  Ktor-Streaming          com.Lyno.matchmindai                 D              "tool_calls": null,
"tool_call_id": null
}
],
"temperature": 0.3,
"response_format": {
"type": "json_object"
},
"stream": false,
"tools": null,
"tool_choice": null
}
BODY END
2025-12-23 15:58:32.174  6193-6275  Ktor-Streaming          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=POST)
FROM: https://api.deepseek.com/chat/completions
COMMON HEADERS
-> access-control-allow-credentials: true
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 14:58:07 GMT
-> server: elb
-> strict-transport-security: max-age=31536000; includeSubDomains; preload
-> transfer-encoding: chunked
-> vary: origin, access-control-request-method, access-control-request-headers
-> via: 1.1 428178e04c8c5f5460c3935b1e7203be.cloudfront.net (CloudFront)
-> x-amz-cf-id: 6NXejoX5qKLc5i-J8Calt91-7JQC9VT0CZTlHVfkZy8thaDJ4cMN5g==
-> x-amz-cf-pop: AMS58-P6
-> x-android-received-millis: 1766501886309
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766501886010
-> x-cache: Miss from cloudfront
-> x-content-type-options: nosniff
-> x-ds-trace-id: 9aae8ebedbbe2e9c2624afc9772f8a7d
BODY Content-Type: application/json
BODY START
{"id":"88491b1f-be17-40cf-a6ae-605a8a453682","object":"chat.completion","created":1766501887,"model":"deepseek-chat","choices":[{"index":0,"message":{"role":"assistant","content":"{\n    \"home_attack_mod\": 1.0,\n    \"home_defense_mod\": 1.0,\n    \"away_attack_mod\": 1.0,\n    \"away_defense_mod\": 1.0,\n    \"chaos_factor\": 0.2,\n    \"news_relevance\": 1.0,\n    \"confidence\": 0.9,\n    \"reasoning\": \"Analyse volgens de gegeven hiërarchie: 1) SPELER BESCHIKBAARHEID: Het nieuws specificeert 'volledige fitheid' en 'geen nieuwe blessures gemeld'. Dit betekent 100% basisopstelling beschikbaar voor beide teams. Geen vermelding van sleutelspelers die afwezig zijn. Modifiers blijven op 1.0 (neutraal) omdat er geen kwantificeerbaar nadeel of voordeel is in beschikbaarheid. 2) TACTISCHE CONFIGURATIE: Nieuws vermeldt dat coaches 'tactisch belang benadrukken', maar specificeert geen formatiewijzigingen of specifieke tactische shifts. Bij gebrek aan concrete informatie, blijft de conservatieve benadering van toepassing: modifiers op 1.0. 3) PSYCHOLOGISCHE FACTOREN: Wedstrijd wordt 'belangrijk' genoemd, maar is geen degradatie- of titelbeslissende wedstrijd op basis van de gegeven context. Geen informatie over recente vormreeksen of specifieke thuispubliek impact. Modifiers op 1.0. 4) EXTERNE VARIABELEN: Weersomstandigheden zijn 'ideaal'. Dit zou een kleine positieve impact kunnen hebben op de aanval voor beide teams (betere balcontrole, passing), maar aangezien het voor beide teams gelijk is en het een klein effect is, en we conservatief moeten zijn, houden we de modifiers op 1.0. Een afzonderlijke 'veldmodifier' is niet gevraagd. CHAOS FACTOR: Zeer laag (0.2). Reden: Geen blessures, geen tactische onzekerheden gemeld, stabiele omstandigheden. Het nieuws wijst op voorspelbaarheid. NEWS RELEVANCE: 1.0. Reden: Alle nieuwssnippets zijn direct over deze specifieke wedstrijd en de betrokken teams. CONFIDENCE: 0.9. Reden: Het nieuws is zeer specifiek, eenduidig ('geen nieuwe blessures', 'volledige fitheid', 'ideale omstandigheden') en direct relevant. Er is weinig ruimte voor interpretatiefouten. CONCLUSIE: Bij afwezigheid van negatief of positief differentieel nieuws tussen de teams, en door strikte toepassing van het conservatisme principe, blijven alle modifiers op het neutrale punt 1.0. De baseline statistieken (bv. xG dominantie voor Nottingham Forest) worden niet gebruikt om modifiers te zetten, omdat dit reeds in het basismodel verwerkt zit en het nieuws hier geen nieuwe informatie over geeft.\"\n}"},"logprobs":null,"finish_reason":"stop"}],"usage":{"prompt_tokens":2128,"completion_tokens":709,"total_tokens":2837,"prompt_tokens_details":{"cached_tokens":2112},"prompt_cache_hit_tokens":2112,"prompt_cache_miss_tokens":16},"system_fingerprint":"fp_eaab8d114b_prod0820_fp8_kvcache"}
BODY END
2025-12-23 15:58:32.180  6193-6311  MatchRepository         com.Lyno.matchmindai                 D  DeepSeek analysis generated successfully
2025-12-23 16:02:01.815  6193-6193  MatchDetailScreen       com.Lyno.matchmindai                 D  Screen loaded with fixtureId: 1379146
2025-12-23 16:02:01.843  6193-6300  MatchRepository         com.Lyno.matchmindai                 W  No match details found for fixture 1379146
2025-12-23 16:02:01.844  6193-6304  System.out              com.Lyno.matchmindai                 I  🔒 INTEGRITY CHECK: ID=1379146 belongs to Nottingham Forest vs Manchester City
2025-12-23 16:02:01.851  6193-6304  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures?league=39&season=2025&status=FT&timezone=Europe%2FAmsterdam
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 16:02:01.926  6193-6304  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures?league=39&season=2025&status=FT&timezone=Europe%2FAmsterdam
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28bcdd898095d9-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:03 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=0YbbDT6WKf9frRa9aPEHd7ACnjHkVzblMl%2BKuEg1puZl0nEfoWzUORQ0RPZQ2Dxn6IdcRHF9mWxTEW93gGBV6zCN%2BSEbALfKYzx11fvMAwzfKdSgPk7WWg%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766502121886
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502121853
-> x-envoy-upstream-service-time: 9
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 299
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7149
BODY Content-Type: application/json
BODY START
2025-12-23 16:02:01.926  6193-6304  Ktor-ApiSports          com.Lyno.matchmindai                 D  {"get":"fixtures","parameters":{"league":"39","season":"2025","status":"FT","timezone":"Europe\/Amsterdam"},"errors":[],"results":170,"paging":{"current":1,"total":1},"response":[{"fixture":{"id":1378969,"referee":"Anthony Taylor, England","timezone":"Europe\/Amsterdam","date":"2025-08-15T21:00:00+02:00","timestamp":1755284400,"periods":{"first":1755284400,"second":1755288000},"venue":{"id":550,"name":"Anfield","city":"Liverpool"},"status":{"long":"Match Finished","short":"FT","elapsed":90,"extra":7}},"league":{"id":39,"name":"Premier League","country":"England","logo":"https:\/\/media.api-sports.io\/football\/leagues\/39.png","flag":"https:\/\/media.api-sports.io\/flags\/gb-eng.svg","season":2025,"round":"Regular Season - 1","standings":true},"teams":{"home":{"id":40,"name":"Liverpool","logo":"https:\/\/media.api-sports.io\/football\/teams\/40.png","winner":true},"away":{"id":35,"name":"Bournemouth","logo":"https:\/\/media.api-sports.io\/football\/teams\/35.png","winner":false}},"goals":{"home":4,"away":2},"score":{"halftime":{"home":1,"away":0},"fulltime":{"home":4,"away":2},"extratime":{"home":null,"away":null},"penalty":{"home":null,"away":null}}},{"fixture":{"id":1378970,"referee":"Craig Pawson, England","timezone":"Europe\/Amsterdam","date":"2025-08-16T13:30:00+02:00","timestamp":1755343800,"periods":{"first":1755343800,"second":1755347400},"venue":{"id":495,"name":"Villa Park","city":"Birmingham"},"status":{"long":"Match Finished","short":"FT","elapsed":90,"extra":7}},"league":{"id":39,"name":"Premier League","country":"England","logo":"https:\/\/media.api-sports.io\/football\/leagues\/39.png","flag":"https:\/\/media.api-sports.io\/flags\/gb-eng.svg","season":2025,"round":"Regular Season - 1","standings":true},"teams":{"home":{"id":66,"name":"Aston Villa","logo":"https:\/\/media.api-sports.io\/football\/teams\/66.png","winner":null},"away":{"id":34,"name":"Newcastle","logo":"https:\/\/media.api-sports.io\/football\/teams\/34.png","winner":null}},"goals":{"home":0,"away":0},"score":{"halftime":{"home":0,"away":0},"fulltime":{"home":0,"away":0},"extratime":{"home":null,"away":null},"penalty":{"home":null,"away":null}}},{"fixture":{"id":1378974,"referee":"Michael Oliver, England","timezone":"Europe\/Amsterdam","date":"2025-08-16T16:00:00+02:00","timestamp":1755352800,"periods":{"first":1755352800,"second":1755356400},"venue":{"id":593,"name":"Tottenham Hotspur Stadium","city":"London"},"status":{"long":"Match Finished","short":"FT","elapsed":90,"extra":4}},"league":{"id":39,"name":"Premier League","country":"England","logo":"https:\/\/media.api-sports.io\/football\/leagues\/39.png","flag":"https:\/\/media.api-sports.io\/flags\/gb-eng.svg","season":2025,"round":"Regular Season - 1","standings":true},"teams":{"home":{"id":47,"name":"Tottenham","logo":"https:\/\/media.api-sports.io\/football\/teams\/47.png","winner":true},"away":{"id":44,"name":"Burnley","logo":"https:\/\/media.api-sports.io\/football\/teams\/44.png","winner":false}},"goals":{"home":3,"away":0},"score":{"halftime":{"home":1,"away":0},"fulltime":{"home":3,"away":0},"extratime":{"home":null,"away":null},"penalty":{"home":null,"away":null}}},{"fixture":{"id":1378971,"referee":"Samuel Barrott, England","timezone":"Europe\/Amsterdam","date":"2025-08-16T16:00:00+02:00","timestamp":1755352800,"periods":{"first":1755352800,"second":1755356400},"venue":{"id":508,"name":"Amex Stadium","city":"Brighton"},"status":{"long":"Match Finished","short":"FT","elapsed":90,"extra":9}},"league":{"id":39,"name":"Premier League","country":"England","logo":"https:\/\/media.api-sports.io\/football\/leagues\/39.png","flag":"https:\/\/media.api-sports.io\/flags\/gb-eng.svg","season":2025,"round":"Regular Season - 1","standings":true},"teams":{"home":{"id":51,"name":"Brighton","logo":"https:\/\/media.api-sports.io\/football\/teams\/51.png","winner":null},"away":{"id":36,"name":"Fulham","logo":"https:\/\/media.api-sports.io\/football\/teams\/36.png","winner":null}},"goals":{"home":1,"away":1},"score":{"half
2025-12-23 16:02:01.926  6193-6304  Ktor-ApiSports          com.Lyno.matchmindai                 D  BODY END
2025-12-23 16:02:01.943  6193-6345  MatchRepository         com.Lyno.matchmindai                 D  Retrieved 151 historical fixtures for league 39, season 2025
2025-12-23 16:02:01.943  6193-6345  MatchRepository         com.Lyno.matchmindai                 D  Using league data for prediction: 151 fixtures for league 39
2025-12-23 16:02:01.943  6193-6345  DataTrace               com.Lyno.matchmindai                 D  Found 151 historical matches for LeagueID: 39 (Home: 65, Away: 50)
2025-12-23 16:02:01.943  6193-6345  DataTrace               com.Lyno.matchmindai                 D  Team-specific matches: Home Team 65 = 11 matches, Away Team 50 = 12 matches
2025-12-23 16:02:01.944  6193-6345  DataTrace               com.Lyno.matchmindai                 D  Fetching xG data for 20 recent fixtures
2025-12-23 16:02:01.945  6193-6304  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1378983 (Crystal Palace vs Nottingham Forest)
2025-12-23 16:02:01.946  6193-6304  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379027 (Nottingham Forest vs Sunderland)
2025-12-23 16:02:01.946  6193-6304  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379012 (Burnley vs Nottingham Forest)
2025-12-23 16:02:01.946  6193-6304  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379037 (Newcastle vs Nottingham Forest)
2025-12-23 16:02:01.946  6193-6304  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379051 (Bournemouth vs Nottingham Forest)
2025-12-23 16:02:01.946  6193-6347  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379065 (Nottingham Forest vs Manchester United)
2025-12-23 16:02:01.947  6193-6300  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379075 (Nottingham Forest vs Leeds)
2025-12-23 16:02:01.948  6193-6304  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379095 (Nottingham Forest vs Brighton)
2025-12-23 16:02:01.949  6193-6347  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379108 (Wolves vs Nottingham Forest)
2025-12-23 16:02:01.949  6193-6347  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379133 (Fulham vs Nottingham Forest)
2025-12-23 16:02:01.950  6193-6344  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1378990 (Brighton vs Manchester City)
2025-12-23 16:02:01.951  6193-6344  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379025 (Manchester City vs Burnley)
2025-12-23 16:02:01.951  6193-6304  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379032 (Brentford vs Manchester City)
2025-12-23 16:02:01.952  6193-6304  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379044 (Manchester City vs Everton)
2025-12-23 16:02:01.952  6193-6304  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379050 (Aston Villa vs Manchester City)
2025-12-23 16:02:01.952  6193-6345  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379103 (Fulham vs Manchester City)
2025-12-23 16:02:01.952  6193-6344  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379087 (Newcastle vs Manchester City)
2025-12-23 16:02:01.952  6193-6345  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379094 (Manchester City vs Leeds)
2025-12-23 16:02:01.952  6193-6304  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379064 (Manchester City vs Bournemouth)
2025-12-23 16:02:01.952  6193-6347  DataTrace               com.Lyno.matchmindai                 D  Fetching xG for fixture 1379009 (Arsenal vs Manchester City)
2025-12-23 16:02:02.203  6193-6311  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379027
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 16:02:02.205  6193-6346  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1378983
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 16:02:02.206  6193-6347  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379037
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 16:02:02.210  6193-6349  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379012
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 16:02:02.218  6193-6346  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379051
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 16:02:02.219  6193-6347  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379075
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 16:02:02.225  6193-6304  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379108
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 16:02:02.226  6193-6350  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379065
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 16:02:02.228  6193-6345  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379095
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 16:02:02.233  6193-6351  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379133
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 16:02:02.242  6193-6353  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379025
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 16:02:02.246  6193-6348  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1378990
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 16:02:02.261  6193-6345  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379032
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 16:02:02.275  6193-6300  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379044
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 16:02:02.277  6193-6353  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379050
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 16:02:02.288  6193-6345  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379087
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 16:02:02.290  6193-6300  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379094
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 16:02:02.297  6193-6345  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379009
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 16:02:02.309  6193-6311  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1378983
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28bcdfce6e0b34-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:03 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=w0wTKWNj7G4lDmS1A4WxP7BRWT81vhso4X62FUswtxZBMNb04Re%2BgV69tltRnvWhijEYN0YCsroh2brqbxNts24GK%2BAFesdWRG4CEoe6vqH1%2Bdl6BO4uKg%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766502122247
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502122211
-> x-envoy-upstream-service-time: 5
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 298
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7148
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1378983"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":52,"name":"Crystal Palace","logo":"https:\/\/media.api-sports.io\/football\/teams\/52.png"},"statistics":[{"type":"Shots on Goal","value":4},{"type":"Shots off Goal","value":4},{"type":"Total Shots","value":8},{"type":"Blocked Shots","value":0},{"type":"Shots insidebox","value":7},{"type":"Shots outsidebox","value":1},{"type":"Fouls","value":11},{"type":"Corner Kicks","value":1},{"type":"Offsides","value":2},{"type":"Ball Possession","value":"42%"},{"type":"Yellow Cards","value":3},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":0},{"type":"Total passes","value":377},{"type":"Passes accurate","value":287},{"type":"Passes %","value":"76%"},{"type":"expected_goals","value":"1.10"},{"type":"goals_prevented","value":0}]},{"team":{"id":65,"name":"Nottingham Forest","logo":"https:\/\/media.api-sports.io\/football\/teams\/65.png"},"statistics":[{"type":"Shots on Goal","value":1},{"type":"Shots off Goal","value":7},{"type":"Total Shots","value":9},{"type":"Blocked Shots","value":1},{"type":"Shots insidebox","value":8},{"type":"Shots outsidebox","value":1},{"type":"Fouls","value":11},{"type":"Corner Kicks","value":3},{"type":"Offsides","value":2},{"type":"Ball Possession","value":"58%"},{"type":"Yellow Cards","value":3},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":3},{"type":"Total passes","value":514},{"type":"Passes accurate","value":442},{"type":"Passes %","value":"86%"},{"type":"expected_goals","value":"0.93"},{"type":"goals_prevented","value":0}]}]}
BODY END
2025-12-23 16:02:02.326  6193-6366  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Nottingham Forest (65) in fixture 1379027
2025-12-23 16:02:02.326  6193-6366  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Sunderland (746) in fixture 1379027
2025-12-23 16:02:02.326  6193-6366  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379027 (Nottingham Forest vs Sunderland)
2025-12-23 16:02:02.327  6193-6346  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379027
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28bcdfcb8966e5-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:03 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=oP7bPlE2H98%2Bh%2BW08xMuiUnRZw95BS1AI8X%2FBNXFsmwYf4WTykXoKTFZGtvOC8bIjS08sAPn1%2BcTLIrTaKZ4fWZmynDB9YgCkxt%2F4oROwZ23Ii6wOQN7HQ%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766502122262
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502122212
-> x-envoy-upstream-service-time: 15
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 296
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7146
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379027"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":65,"name":"Nottingham Forest","logo":"https:\/\/media.api-sports.io\/football\/teams\/65.png"},"statistics":[{"type":"Shots on Goal","value":6},{"type":"Shots off Goal","value":10},{"type":"Total Shots","value":22},{"type":"Blocked Shots","value":6},{"type":"Shots insidebox","value":15},{"type":"Shots outsidebox","value":7},{"type":"Fouls","value":11},{"type":"Corner Kicks","value":7},{"type":"Offsides","value":0},{"type":"Ball Possession","value":"65%"},{"type":"Yellow Cards","value":4},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":2},{"type":"Total passes","value":578},{"type":"Passes accurate","value":521},{"type":"Passes %","value":"90%"},{"type":"expected_goals","value":"1.68"},{"type":"goals_prevented","value":1}]},{"team":{"id":746,"name":"Sunderland","logo":"https:\/\/media.api-sports.io\/football\/teams\/746.png"},"statistics":[{"type":"Shots on Goal","value":3},{"type":"Shots off Goal","value":5},{"type":"Total Shots","value":11},{"type":"Blocked Shots","value":3},{"type":"Shots insidebox","value":8},{"type":"Shots outsidebox","value":3},{"type":"Fouls","value":6},{"type":"Corner Kicks","value":4},{"type":"Offsides","value":1},{"type":"Ball Possession","value":"35%"},{"type":"Yellow Cards","value":2},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":6},{"type":"Total passes","value":318},{"type":"Passes accurate","value":258},{"type":"Passes %","value":"81%"},{"type":"expected_goals","value":"1.66"},{"type":"goals_prevented","value":1}]}]}
BODY END
2025-12-23 16:02:02.327  6193-6363  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379012
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28bcdfca9eb926-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:03 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=QAx9HSbi0IpTeoS1RTcbBk56EfENcjXaw1BR4Q26Z6gSQTQLY1xvdvcnM5f%2BVWG4KrDRccMWe0UCrbxuzYNPR8cOL0nXJD9akz9h9nz3FCfx6iQxqxCP8g%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766502122275
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502122215
-> x-envoy-upstream-service-time: 10
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 295
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7145
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379012"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":44,"name":"Burnley","logo":"https:\/\/media.api-sports.io\/football\/teams\/44.png"},"statistics":[{"type":"Shots on Goal","value":5},{"type":"Shots off Goal","value":4},{"type":"Total Shots","value":12},{"type":"Blocked Shots","value":3},{"type":"Shots insidebox","value":9},{"type":"Shots outsidebox","value":3},{"type":"Fouls","value":12},{"type":"Corner Kicks","value":4},{"type":"Offsides","value":1},{"type":"Ball Possession","value":"37%"},{"type":"Yellow Cards","value":1},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":7},{"type":"Total passes","value":348},{"type":"Passes accurate","value":269},{"type":"Passes %","value":"77%"},{"type":"expected_goals","value":"0.85"},{"type":"goals_prevented","value":0}]},{"team":{"id":65,"name":"Nottingham Forest","logo":"https:\/\/media.api-sports.io\/football\/teams\/65.png"},"statistics":[{"type":"Shots on Goal","value":8},{"type":"Shots off Goal","value":2},{"type":"Total Shots","value":17},{"type":"Blocked Shots","value":7},{"type":"Shots insidebox","value":9},{"type":"Shots outsidebox","value":8},{"type":"Fouls","value":11},{"type":"Corner Kicks","value":5},{"type":"Offsides","value":2},{"type":"Ball Possession","value":"63%"},{"type":"Yellow Cards","value":1},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":4},{"type":"Total passes","value":628},{"type":"Passes accurate","value":564},{"type":"Passes %","value":"90%"},{"type":"expected_goals","value":"1.10"},{"type":"goals_prevented","value":0}]}]}
BODY END
2025-12-23 16:02:02.329  6193-6368  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379037
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28bcdfcf8795d9-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:03 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=e1nkn26WzTleBTqHNWKp%2FDNlC%2BcAhj7xMjwkPx4a2hrsGaKAD8iaDyILTPOPryP8QfLWnb8mXY1pFzXJrgfN0WV2tWTyrqprAMnozwAIt2UUHXQuiSDRIg%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766502122257
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502122209
-> x-envoy-upstream-service-time: 19
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 297
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7147
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379037"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":34,"name":"Newcastle","logo":"https:\/\/media.api-sports.io\/football\/teams\/34.png"},"statistics":[{"type":"Shots on Goal","value":9},{"type":"Shots off Goal","value":4},{"type":"Total Shots","value":18},{"type":"Blocked Shots","value":5},{"type":"Shots insidebox","value":13},{"type":"Shots outsidebox","value":5},{"type":"Fouls","value":16},{"type":"Corner Kicks","value":8},{"type":"Offsides","value":0},{"type":"Ball Possession","value":"51%"},{"type":"Yellow Cards","value":0},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":4},{"type":"Total passes","value":464},{"type":"Passes accurate","value":387},{"type":"Passes %","value":"83%"},{"type":"expected_goals","value":"3.45"},{"type":"goals_prevented","value":1}]},{"team":{"id":65,"name":"Nottingham Forest","logo":"https:\/\/media.api-sports.io\/football\/teams\/65.png"},"statistics":[{"type":"Shots on Goal","value":4},{"type":"Shots off Goal","value":1},{"type":"Total Shots","value":5},{"type":"Blocked Shots","value":0},{"type":"Shots insidebox","value":4},{"type":"Shots outsidebox","value":1},{"type":"Fouls","value":15},{"type":"Corner Kicks","value":1},{"type":"Offsides","value":2},{"type":"Ball Possession","value":"49%"},{"type":"Yellow Cards","value":4},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":7},{"type":"Total passes","value":452},{"type":"Passes accurate","value":372},{"type":"Passes %","value":"82%"},{"type":"expected_goals","value":"0.30"},{"type":"goals_prevented","value":1}]}]}
BODY END
2025-12-23 16:02:02.334  6193-6368  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379032
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28bce0188395d9-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:03 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=oUvRQoAINAqnBEkUr1n7g%2BODrVzS%2B3LHwwiSsjCyKNlVuS%2BYco3t8kRD3vxYR253vM%2BaFpiyIqNXvdg%2BlLTHQ5RjI%2FHXw8CCApv2GHRy0nCd%2F6Xh09VmTA%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766502122311
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502122263
-> x-envoy-upstream-service-time: 8
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 293
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7143
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379032"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":55,"name":"Brentford","logo":"https:\/\/media.api-sports.io\/football\/teams\/55.png"},"statistics":[{"type":"Shots on Goal","value":1},{"type":"Shots off Goal","value":3},{"type":"Total Shots","value":6},{"type":"Blocked Shots","value":2},{"type":"Shots insidebox","value":4},{"type":"Shots outsidebox","value":2},{"type":"Fouls","value":10},{"type":"Corner Kicks","value":3},{"type":"Offsides","value":1},{"type":"Ball Possession","value":"30%"},{"type":"Yellow Cards","value":3},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":3},{"type":"Total passes","value":311},{"type":"Passes accurate","value":249},{"type":"Passes %","value":"80%"},{"type":"expected_goals","value":"0.70"},{"type":"goals_prevented","value":0}]},{"team":{"id":50,"name":"Manchester City","logo":"https:\/\/media.api-sports.io\/football\/teams\/50.png"},"statistics":[{"type":"Shots on Goal","value":4},{"type":"Shots off Goal","value":2},{"type":"Total Shots","value":10},{"type":"Blocked Shots","value":4},{"type":"Shots insidebox","value":6},{"type":"Shots outsidebox","value":4},{"type":"Fouls","value":6},{"type":"Corner Kicks","value":2},{"type":"Offsides","value":1},{"type":"Ball Possession","value":"70%"},{"type":"Yellow Cards","value":2},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":1},{"type":"Total passes","value":764},{"type":"Passes accurate","value":698},{"type":"Passes %","value":"91%"},{"type":"expected_goals","value":"0.85"},{"type":"goals_prevented","value":0}]}]}
BODY END
2025-12-23 16:02:02.340  6193-6353  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379064
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 16:02:02.341  6193-6366  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Brentford (55) in fixture 1379032
2025-12-23 16:02:02.342  6193-6366  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Manchester City (50) in fixture 1379032
2025-12-23 16:02:02.342  6193-6366  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379032 (Brentford vs Manchester City)
2025-12-23 16:02:02.343  6193-6358  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Burnley (44) in fixture 1379012
2025-12-23 16:02:02.344  6193-6358  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Nottingham Forest (65) in fixture 1379012
2025-12-23 16:02:02.344  6193-6358  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379012 (Burnley vs Nottingham Forest)
2025-12-23 16:02:02.345  6193-6360  Ktor-ApiSports          com.Lyno.matchmindai                 D  REQUEST: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379103
METHOD: HttpMethod(value=GET)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
-> Content-Type: application/json; application/json
-> x-apisports-key: 91fb3c5d6674adddbb4e23b15ac01c80
CONTENT HEADERS
-> Content-Length: 0
BODY Content-Type: null
BODY START

                                                                                                    BODY END
2025-12-23 16:02:02.345  6193-6367  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379051
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28bce01b2a9705-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:03 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=4jqnISH9ZBYJF0vt8UVFMcdJwNsxUjkXzJDJz48kgDXdX1530s817G1myPqr9rcy132poDlxNedcWv4Thik68W2yeOOey5Ci6C1v8KdHoCW%2FB8tExoi1zQ%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766502122296
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502122258
-> x-envoy-upstream-service-time: 7
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 294
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7144
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379051"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":35,"name":"Bournemouth","logo":"https:\/\/media.api-sports.io\/football\/teams\/35.png"},"statistics":[{"type":"Shots on Goal","value":5},{"type":"Shots off Goal","value":4},{"type":"Total Shots","value":13},{"type":"Blocked Shots","value":4},{"type":"Shots insidebox","value":5},{"type":"Shots outsidebox","value":8},{"type":"Fouls","value":17},{"type":"Corner Kicks","value":6},{"type":"Offsides","value":3},{"type":"Ball Possession","value":"52%"},{"type":"Yellow Cards","value":3},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":4},{"type":"Total passes","value":447},{"type":"Passes accurate","value":359},{"type":"Passes %","value":"80%"},{"type":"expected_goals","value":"0.58"},{"type":"goals_prevented","value":-1}]},{"team":{"id":65,"name":"Nottingham Forest","logo":"https:\/\/media.api-sports.io\/football\/teams\/65.png"},"statistics":[{"type":"Shots on Goal","value":4},{"type":"Shots off Goal","value":2},{"type":"Total Shots","value":8},{"type":"Blocked Shots","value":2},{"type":"Shots insidebox","value":3},{"type":"Shots outsidebox","value":5},{"type":"Fouls","value":7},{"type":"Corner Kicks","value":4},{"type":"Offsides","value":0},{"type":"Ball Possession","value":"48%"},{"type":"Yellow Cards","value":1},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":3},{"type":"Total passes","value":409},{"type":"Passes accurate","value":318},{"type":"Passes %","value":"78%"},{"type":"expected_goals","value":"0.37"},{"type":"goals_prevented","value":-1}]}]}
BODY END
2025-12-23 16:02:02.351  6193-6369  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379075
COMMON HEADERS
-> accept-ranges: bytes
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28bce03e0f0bde-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:03 GMT
-> expires: 0
-> last-modified: Thu, 18 Sep 2025 06:58:28 GMT
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=hr2icg924ytE0o1uwZdoi%2F3EnJvPEBFh96nYjJ2KAD0%2B7sSglSzrIYX6TWRTDO4C305vItVrrw3s3OC4ELP5CQx%2FLEltpb%2BiGPg7keP1R0lAvyE480CXWw%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766502122325
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502122283
-> x-envoy-upstream-service-time: 2
BODY Content-Type: application/json
BODY START
{"get": "","parameters": [],"errors": {"rateLimit": "Too many requests. You have exceeded the limit of requests per minute of your subscription."},"results": 0,"paging": {"current": 1,"total": 1},"response": []}

                                                                                                    BODY END
2025-12-23 16:02:02.354  6193-6364  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379108
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28bce038556600-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:03 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=cxdKcmLAXfZL%2BDf4dP%2BDb3xmw1Lha0Fn3HqHAAkfLk6F00JLqN3KAlSMooV5CX7RfcXDQchMplMWF2lOZmK%2Bli6WsopIdcud2tYUXG1TH58mb5QuWBH80g%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766502122321
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502122283
-> x-envoy-upstream-service-time: 7
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 291
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7141
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379108"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":39,"name":"Wolves","logo":"https:\/\/media.api-sports.io\/football\/teams\/39.png"},"statistics":[{"type":"Shots on Goal","value":1},{"type":"Shots off Goal","value":2},{"type":"Total Shots","value":5},{"type":"Blocked Shots","value":2},{"type":"Shots insidebox","value":4},{"type":"Shots outsidebox","value":1},{"type":"Fouls","value":19},{"type":"Corner Kicks","value":3},{"type":"Offsides","value":2},{"type":"Ball Possession","value":"50%"},{"type":"Yellow Cards","value":2},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":2},{"type":"Total passes","value":415},{"type":"Passes accurate","value":329},{"type":"Passes %","value":"79%"},{"type":"expected_goals","value":"0.91"},{"type":"goals_prevented","value":0}]},{"team":{"id":65,"name":"Nottingham Forest","logo":"https:\/\/media.api-sports.io\/football\/teams\/65.png"},"statistics":[{"type":"Shots on Goal","value":3},{"type":"Shots off Goal","value":3},{"type":"Total Shots","value":10},{"type":"Blocked Shots","value":4},{"type":"Shots insidebox","value":7},{"type":"Shots outsidebox","value":3},{"type":"Fouls","value":11},{"type":"Corner Kicks","value":9},{"type":"Offsides","value":2},{"type":"Ball Possession","value":"50%"},{"type":"Yellow Cards","value":2},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":1},{"type":"Total passes","value":416},{"type":"Passes accurate","value":334},{"type":"Passes %","value":"80%"},{"type":"expected_goals","value":"0.74"},{"type":"goals_prevented","value":0}]}]}
BODY END
2025-12-23 16:02:02.358  6193-6366  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Bournemouth (35) in fixture 1379051
2025-12-23 16:02:02.358  6193-6366  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Nottingham Forest (65) in fixture 1379051
2025-12-23 16:02:02.358  6193-6366  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379051 (Bournemouth vs Nottingham Forest)
2025-12-23 16:02:02.363  6193-6275  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1378990
COMMON HEADERS
-> accept-ranges: bytes
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28bce07d45a462-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:03 GMT
-> expires: 0
-> last-modified: Thu, 18 Sep 2025 06:58:28 GMT
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=xJV4cokITYxz4pqBHftiuXBJq%2BpYurYht6oiLAJ8fMHE5ZIPE1TKAG%2BM3TywzL1vlMZ1tkbVOwQSFZ%2B6YWLIrxpDqw5j2KO%2BVeGELcXlkmm6wkwPBnXvaw%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766502122353
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502122320
-> x-envoy-upstream-service-time: 0
BODY Content-Type: application/json
BODY START
{"get": "","parameters": [],"errors": {"rateLimit": "Too many requests. You have exceeded the limit of requests per minute of your subscription."},"results": 0,"paging": {"current": 1,"total": 1},"response": []}

                                                                                                    BODY END
2025-12-23 16:02:02.370  6193-6358  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Crystal Palace (52) in fixture 1378983
2025-12-23 16:02:02.370  6193-6358  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Nottingham Forest (65) in fixture 1378983
2025-12-23 16:02:02.370  6193-6358  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1378983 (Crystal Palace vs Nottingham Forest)
2025-12-23 16:02:02.374  6193-6357  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379050
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28bce03bf266e5-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:03 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=rosUqOatZRY5KAZy1%2FXvSGgI6%2BhZ5dcyaQs28N7mKANx7T%2F6L9BLoxBOP%2FIRpmRTmlatQEQryyGyw9%2FFkS1%2BTswrG3gec25wUvfL9%2BpRz0lO%2Fmt%2Bdc0S8Q%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766502122324
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502122287
-> x-envoy-upstream-service-time: 6
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 290
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7140
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379050"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":66,"name":"Aston Villa","logo":"https:\/\/media.api-sports.io\/football\/teams\/66.png"},"statistics":[{"type":"Shots on Goal","value":3},{"type":"Shots off Goal","value":1},{"type":"Total Shots","value":9},{"type":"Blocked Shots","value":5},{"type":"Shots insidebox","value":8},{"type":"Shots outsidebox","value":1},{"type":"Fouls","value":8},{"type":"Corner Kicks","value":5},{"type":"Offsides","value":1},{"type":"Ball Possession","value":"47%"},{"type":"Yellow Cards","value":1},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":3},{"type":"Total passes","value":449},{"type":"Passes accurate","value":388},{"type":"Passes %","value":"86%"},{"type":"expected_goals","value":"0.81"},{"type":"goals_prevented","value":0}]},{"team":{"id":50,"name":"Manchester City","logo":"https:\/\/media.api-sports.io\/football\/teams\/50.png"},"statistics":[{"type":"Shots on Goal","value":4},{"type":"Shots off Goal","value":5},{"type":"Total Shots","value":18},{"type":"Blocked Shots","value":9},{"type":"Shots insidebox","value":14},{"type":"Shots outsidebox","value":4},{"type":"Fouls","value":16},{"type":"Corner Kicks","value":6},{"type":"Offsides","value":3},{"type":"Ball Possession","value":"53%"},{"type":"Yellow Cards","value":4},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":2},{"type":"Total passes","value":504},{"type":"Passes accurate","value":455},{"type":"Passes %","value":"90%"},{"type":"expected_goals","value":"1.18"},{"type":"goals_prevented","value":0}]}]}
BODY END
2025-12-23 16:02:02.374  6193-6365  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379065
COMMON HEADERS
-> accept-ranges: bytes
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28bce038145c48-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:03 GMT
-> expires: 0
-> last-modified: Thu, 18 Sep 2025 06:58:28 GMT
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=eeQbATFe7bESHhG3aOPXUJbFePSDKxpi4eqX2%2F6jJsEWBxxMuk%2BsVxW7r6Nl5fpdRCv00ToKuemMYeI6h6z6YLcM%2BhX%2Bxfr5K3zLX%2FfhW%2FfCdxqI5jWFbA%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766502122357
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502122285
-> x-envoy-upstream-service-time: 1
BODY Content-Type: application/json
BODY START
{"get": "","parameters": [],"errors": {"rateLimit": "Too many requests. You have exceeded the limit of requests per minute of your subscription."},"results": 0,"paging": {"current": 1,"total": 1},"response": []}

                                                                                                    BODY END
2025-12-23 16:02:02.375  6193-6363  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379133
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28bce06bacdb35-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:03 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=2aGlVj3rPD0HEJ%2FpBe32aRs5CuMb3wrRM2DPX%2FsYICo1sIG3dpSnRNycMI9uwc9%2BZxbOeR7a8cnIXyIoDjj36c5T7AZlZFEGBC4LTA90LwNSefFk%2BzNMwg%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766502122361
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502122315
-> x-envoy-upstream-service-time: 7
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 287
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7137
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379133"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":36,"name":"Fulham","logo":"https:\/\/media.api-sports.io\/football\/teams\/36.png"},"statistics":[{"type":"Shots on Goal","value":1},{"type":"Shots off Goal","value":4},{"type":"Total Shots","value":11},{"type":"Blocked Shots","value":6},{"type":"Shots insidebox","value":8},{"type":"Shots outsidebox","value":3},{"type":"Fouls","value":5},{"type":"Corner Kicks","value":1},{"type":"Offsides","value":2},{"type":"Ball Possession","value":"50%"},{"type":"Yellow Cards","value":5},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":2},{"type":"Total passes","value":495},{"type":"Passes accurate","value":396},{"type":"Passes %","value":"80%"},{"type":"expected_goals","value":"1.50"},{"type":"goals_prevented","value":0}]},{"team":{"id":65,"name":"Nottingham Forest","logo":"https:\/\/media.api-sports.io\/football\/teams\/65.png"},"statistics":[{"type":"Shots on Goal","value":2},{"type":"Shots off Goal","value":6},{"type":"Total Shots","value":12},{"type":"Blocked Shots","value":4},{"type":"Shots insidebox","value":6},{"type":"Shots outsidebox","value":6},{"type":"Fouls","value":9},{"type":"Corner Kicks","value":5},{"type":"Offsides","value":2},{"type":"Ball Possession","value":"50%"},{"type":"Yellow Cards","value":4},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":0},{"type":"Total passes","value":490},{"type":"Passes accurate","value":417},{"type":"Passes %","value":"85%"},{"type":"expected_goals","value":"0.67"},{"type":"goals_prevented","value":0}]}]}
BODY END
2025-12-23 16:02:02.377  6193-6311  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379044
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28bce02eca0b34-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:03 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=LcSjiiuR6jGjFKCKvb8Jd144Uzdq%2FVyDNVTQcoliTrqTTNtFUbaolZr2doyKau8hk9LOx%2F6RzLv3kCHKW5D6RYUB5l49d9n675sscNBGIpmWgxzPDKewQw%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766502122325
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502122277
-> x-envoy-upstream-service-time: 10
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 292
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7142
-> x-sub-ttl: 43200
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379044"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":50,"name":"Manchester City","logo":"https:\/\/media.api-sports.io\/football\/teams\/50.png"},"statistics":[{"type":"Shots on Goal","value":7},{"type":"Shots off Goal","value":5},{"type":"Total Shots","value":19},{"type":"Blocked Shots","value":7},{"type":"Shots insidebox","value":16},{"type":"Shots outsidebox","value":3},{"type":"Fouls","value":8},{"type":"Corner Kicks","value":11},{"type":"Offsides","value":0},{"type":"Ball Possession","value":"71%"},{"type":"Yellow Cards","value":0},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":1},{"type":"Total passes","value":749},{"type":"Passes accurate","value":685},{"type":"Passes %","value":"91%"},{"type":"expected_goals","value":"2.38"},{"type":"goals_prevented","value":0}]},{"team":{"id":45,"name":"Everton","logo":"https:\/\/media.api-sports.io\/football\/teams\/45.png"},"statistics":[{"type":"Shots on Goal","value":1},{"type":"Shots off Goal","value":2},{"type":"Total Shots","value":5},{"type":"Blocked Shots","value":2},{"type":"Shots insidebox","value":4},{"type":"Shots outsidebox","value":1},{"type":"Fouls","value":14},{"type":"Corner Kicks","value":3},{"type":"Offsides","value":1},{"type":"Ball Possession","value":"29%"},{"type":"Yellow Cards","value":2},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":5},{"type":"Total passes","value":300},{"type":"Passes accurate","value":237},{"type":"Passes %","value":"79%"},{"type":"expected_goals","value":"0.81"},{"type":"goals_prevented","value":0}]}]}
BODY END
2025-12-23 16:02:02.387  6193-6367  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE https://v3.football.api-sports.io/fixtures/statistics?fixture=1378990 failed with exception: io.ktor.serialization.JsonConvertException: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 16:02:02.395  6193-6366  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Wolves (39) in fixture 1379108
2025-12-23 16:02:02.397  6193-6366  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Nottingham Forest (65) in fixture 1379108
2025-12-23 16:02:02.397  6193-6366  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379108 (Wolves vs Nottingham Forest)
2025-12-23 16:02:02.398  6193-6367  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Aston Villa (66) in fixture 1379050
2025-12-23 16:02:02.398  6193-6367  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Manchester City (50) in fixture 1379050
2025-12-23 16:02:02.398  6193-6367  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379050 (Aston Villa vs Manchester City)
2025-12-23 16:02:02.399  6193-6367  MatchRepository         com.Lyno.matchmindai                 W  ⚠️ Unexpected error for fixture 1378990: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 16:02:02.399  6193-6367  DataTrace               com.Lyno.matchmindai                 D  Unexpected error for fixture 1378990: JsonConvertException
2025-12-23 16:02:02.408  6193-6350  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379087
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28bce05b0ab926-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:03 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=EbDrpOEfzPuh6zqVzMhqYR%2FjiTvK%2Bjn1zHxBtt7BJGgLFJylNnTXMYHhqAj0mSeECTqnsY5ZmIT2Wv%2Fh3I9XX1tkPr8gw81Vhr1Kj2Xz%2Bz0OPpBpP8lD7g%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766502122341
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502122302
-> x-envoy-upstream-service-time: 5
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 289
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7139
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379087"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":34,"name":"Newcastle","logo":"https:\/\/media.api-sports.io\/football\/teams\/34.png"},"statistics":[{"type":"Shots on Goal","value":5},{"type":"Shots off Goal","value":3},{"type":"Total Shots","value":9},{"type":"Blocked Shots","value":1},{"type":"Shots insidebox","value":7},{"type":"Shots outsidebox","value":2},{"type":"Fouls","value":13},{"type":"Corner Kicks","value":5},{"type":"Offsides","value":3},{"type":"Ball Possession","value":"33%"},{"type":"Yellow Cards","value":1},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":3},{"type":"Total passes","value":285},{"type":"Passes accurate","value":220},{"type":"Passes %","value":"77%"},{"type":"expected_goals","value":"2.24"},{"type":"goals_prevented","value":0}]},{"team":{"id":50,"name":"Manchester City","logo":"https:\/\/media.api-sports.io\/football\/teams\/50.png"},"statistics":[{"type":"Shots on Goal","value":4},{"type":"Shots off Goal","value":9},{"type":"Total Shots","value":17},{"type":"Blocked Shots","value":4},{"type":"Shots insidebox","value":14},{"type":"Shots outsidebox","value":3},{"type":"Fouls","value":8},{"type":"Corner Kicks","value":9},{"type":"Offsides","value":2},{"type":"Ball Possession","value":"67%"},{"type":"Yellow Cards","value":3},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":3},{"type":"Total passes","value":584},{"type":"Passes accurate","value":525},{"type":"Passes %","value":"90%"},{"type":"expected_goals","value":"2.49"},{"type":"goals_prevented","value":0}]}]}
BODY END
2025-12-23 16:02:02.410  6193-6353  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Manchester City (50) in fixture 1379044
2025-12-23 16:02:02.411  6193-6353  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Everton (45) in fixture 1379044
2025-12-23 16:02:02.413  6193-6353  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379044 (Manchester City vs Everton)
2025-12-23 16:02:02.414  6193-6369  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379025
COMMON HEADERS
-> accept-ranges: bytes
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28bce07b5eb954-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:03 GMT
-> expires: 0
-> last-modified: Thu, 18 Sep 2025 06:58:28 GMT
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=%2FawXyaqdkLs2TmFh%2FdOqIf3R%2FtB9Tqdm3gA7apm8tSJN5F5r1ohBJbNLbXzwNgUVP0S90w14KfGrAeflUrz%2BaMe0wfNueJj109L0HyVhmZH8T5qRwG4DPA%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766502122373
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502122324
-> x-envoy-upstream-service-time: 1
BODY Content-Type: application/json
BODY START
{"get": "","parameters": [],"errors": {"rateLimit": "Too many requests. You have exceeded the limit of requests per minute of your subscription."},"results": 0,"paging": {"current": 1,"total": 1},"response": []}

                                                                                                    BODY END
2025-12-23 16:02:02.414  6193-6358  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Newcastle (34) in fixture 1379037
2025-12-23 16:02:02.415  6193-6358  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Nottingham Forest (65) in fixture 1379037
2025-12-23 16:02:02.421  6193-6358  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379037 (Newcastle vs Nottingham Forest)
2025-12-23 16:02:02.425  6193-6364  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379094
COMMON HEADERS
-> accept-ranges: bytes
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28bce08b665b28-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:03 GMT
-> expires: 0
-> last-modified: Thu, 18 Sep 2025 06:58:28 GMT
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=5bXdj3%2BxdxgaEt8ufgdFewX%2BN5RJNsKnhCj1IytloG%2BcHs4SdYEncM0L81jIHute6TJ6FnT00%2FvxJKkhhEf93JzdPsIjrCTZmjTL%2FZGJXTSkBeAMCPnuWA%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766502122386
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502122336
-> x-envoy-upstream-service-time: 0
BODY Content-Type: application/json
BODY START
{"get": "","parameters": [],"errors": {"rateLimit": "Too many requests. You have exceeded the limit of requests per minute of your subscription."},"results": 0,"paging": {"current": 1,"total": 1},"response": []}

                                                                                                    BODY END
2025-12-23 16:02:02.429  6193-6358  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE https://v3.football.api-sports.io/fixtures/statistics?fixture=1379094 failed with exception: io.ktor.serialization.JsonConvertException: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 16:02:02.429  6193-6366  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Fulham (36) in fixture 1379133
2025-12-23 16:02:02.429  6193-6366  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Nottingham Forest (65) in fixture 1379133
2025-12-23 16:02:02.429  6193-6366  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379133 (Fulham vs Nottingham Forest)
2025-12-23 16:02:02.433  6193-6358  MatchRepository         com.Lyno.matchmindai                 W  ⚠️ Unexpected error for fixture 1379094: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 16:02:02.433  6193-6358  DataTrace               com.Lyno.matchmindai                 D  Unexpected error for fixture 1379094: JsonConvertException
2025-12-23 16:02:02.436  6193-6300  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379095
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28bce069365ec9-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:03 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=2Pyqm0t7sCsIXAKV6cwXREpvLcV4tKONjZyEgtnL%2FwskJ9wNyTDy1LNDAHKP6tT3ep345FKBUCvXXuzFZW0UBzu8F%2Botp6CIkaWK5ncQxCHW7cGgqCErww%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766502122370
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502122316
-> x-envoy-upstream-service-time: 8
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 288
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7138
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379095"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":65,"name":"Nottingham Forest","logo":"https:\/\/media.api-sports.io\/football\/teams\/65.png"},"statistics":[{"type":"Shots on Goal","value":3},{"type":"Shots off Goal","value":6},{"type":"Total Shots","value":19},{"type":"Blocked Shots","value":10},{"type":"Shots insidebox","value":13},{"type":"Shots outsidebox","value":6},{"type":"Fouls","value":11},{"type":"Corner Kicks","value":9},{"type":"Offsides","value":1},{"type":"Ball Possession","value":"52%"},{"type":"Yellow Cards","value":0},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":3},{"type":"Total passes","value":492},{"type":"Passes accurate","value":409},{"type":"Passes %","value":"83%"},{"type":"expected_goals","value":"0.89"},{"type":"goals_prevented","value":0}]},{"team":{"id":51,"name":"Brighton","logo":"https:\/\/media.api-sports.io\/football\/teams\/51.png"},"statistics":[{"type":"Shots on Goal","value":6},{"type":"Shots off Goal","value":5},{"type":"Total Shots","value":17},{"type":"Blocked Shots","value":6},{"type":"Shots insidebox","value":11},{"type":"Shots outsidebox","value":6},{"type":"Fouls","value":10},{"type":"Corner Kicks","value":2},{"type":"Offsides","value":0},{"type":"Ball Possession","value":"48%"},{"type":"Yellow Cards","value":1},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":3},{"type":"Total passes","value":476},{"type":"Passes accurate","value":392},{"type":"Passes %","value":"82%"},{"type":"expected_goals","value":"2.00"},{"type":"goals_prevented","value":0}]}]}
BODY END
2025-12-23 16:02:02.437  6193-6367  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE https://v3.football.api-sports.io/fixtures/statistics?fixture=1379025 failed with exception: io.ktor.serialization.JsonConvertException: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 16:02:02.437  6193-6358  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Newcastle (34) in fixture 1379087
2025-12-23 16:02:02.438  6193-6358  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Manchester City (50) in fixture 1379087
2025-12-23 16:02:02.438  6193-6358  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379087 (Newcastle vs Manchester City)
2025-12-23 16:02:02.438  6193-6367  MatchRepository         com.Lyno.matchmindai                 W  ⚠️ Unexpected error for fixture 1379025: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 16:02:02.439  6193-6367  DataTrace               com.Lyno.matchmindai                 D  Unexpected error for fixture 1379025: JsonConvertException
2025-12-23 16:02:02.439  6193-6367  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE https://v3.football.api-sports.io/fixtures/statistics?fixture=1379065 failed with exception: io.ktor.serialization.JsonConvertException: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 16:02:02.440  6193-6275  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379103
COMMON HEADERS
-> accept-ranges: bytes
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28bce0bf6d0b34-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:03 GMT
-> expires: 0
-> last-modified: Thu, 18 Sep 2025 06:58:28 GMT
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=QOTO9xYwCi4I6DtLV%2B1SoZzBFLNzMm%2FhDhHfvjCK%2FiN2cCrOdnnWrYd7r56SlbyH4f1x4T%2F%2BYFqDd5wXbUWhd9gyc4Wl%2BG7rOZKwC%2BFc4kXEW0i%2FOkiRcQ%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766502122395
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502122348
-> x-envoy-upstream-service-time: 1
BODY Content-Type: application/json
BODY START
{"get": "","parameters": [],"errors": {"rateLimit": "Too many requests. You have exceeded the limit of requests per minute of your subscription."},"results": 0,"paging": {"current": 1,"total": 1},"response": []}

                                                                                                    BODY END
2025-12-23 16:02:02.440  6193-6350  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379064
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28bce099d495d9-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:03 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=Tj3CXsOoaxmMU5sja3gL6ANHYM1eqpRabgIjxyXDTKFbQvoujvvYCZ4%2BCq1%2BGi4ZZVRrhQiqcevvVoAXoJqFk58WN%2FunuKqYWkqTAmc65IPjVpEjgwUMmw%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766502122385
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502122344
-> x-envoy-upstream-service-time: 5
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 286
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7136
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379064"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":50,"name":"Manchester City","logo":"https:\/\/media.api-sports.io\/football\/teams\/50.png"},"statistics":[{"type":"Shots on Goal","value":8},{"type":"Shots off Goal","value":4},{"type":"Total Shots","value":15},{"type":"Blocked Shots","value":3},{"type":"Shots insidebox","value":13},{"type":"Shots outsidebox","value":2},{"type":"Fouls","value":8},{"type":"Corner Kicks","value":9},{"type":"Offsides","value":0},{"type":"Ball Possession","value":"48%"},{"type":"Yellow Cards","value":2},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":4},{"type":"Total passes","value":493},{"type":"Passes accurate","value":425},{"type":"Passes %","value":"86%"},{"type":"expected_goals","value":"2.22"},{"type":"goals_prevented","value":-1}]},{"team":{"id":35,"name":"Bournemouth","logo":"https:\/\/media.api-sports.io\/football\/teams\/35.png"},"statistics":[{"type":"Shots on Goal","value":5},{"type":"Shots off Goal","value":2},{"type":"Total Shots","value":8},{"type":"Blocked Shots","value":1},{"type":"Shots insidebox","value":4},{"type":"Shots outsidebox","value":4},{"type":"Fouls","value":11},{"type":"Corner Kicks","value":4},{"type":"Offsides","value":3},{"type":"Ball Possession","value":"52%"},{"type":"Yellow Cards","value":2},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":4},{"type":"Total passes","value":521},{"type":"Passes accurate","value":461},{"type":"Passes %","value":"88%"},{"type":"expected_goals","value":"0.72"},{"type":"goals_prevented","value":-1}]}]}
BODY END
2025-12-23 16:02:02.440  6193-6358  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE https://v3.football.api-sports.io/fixtures/statistics?fixture=1379075 failed with exception: io.ktor.serialization.JsonConvertException: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 16:02:02.441  6193-6358  MatchRepository         com.Lyno.matchmindai                 W  ⚠️ Unexpected error for fixture 1379075: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 16:02:02.441  6193-6358  DataTrace               com.Lyno.matchmindai                 D  Unexpected error for fixture 1379075: JsonConvertException
2025-12-23 16:02:02.441  6193-6367  MatchRepository         com.Lyno.matchmindai                 W  ⚠️ Unexpected error for fixture 1379065: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 16:02:02.441  6193-6367  DataTrace               com.Lyno.matchmindai                 D  Unexpected error for fixture 1379065: JsonConvertException
2025-12-23 16:02:02.443  6193-6275  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=GET)
FROM: https://v3.football.api-sports.io/fixtures/statistics?fixture=1379009
COMMON HEADERS
-> access-control-allow-credentials: True
-> access-control-allow-headers: x-rapidapi-key, x-apisports-key, x-rapidapi-host
-> access-control-allow-methods: GET, OPTIONS
-> access-control-allow-origin: *
-> cache-control: no-store, no-cache, must-revalidate
-> cf-cache-status: DYNAMIC
-> cf-ray: 9b28bce0d99121d7-AMS
-> connection: keep-alive
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:03 GMT
-> expires: 0
-> nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
-> pragma: no-cache
-> report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=Zqb1emVTJz8PHzWEPo5ZBvDb5yHJa90P%2BIj1jLkfHLWUgwHtRAQfIQc2u7kKuXr1TPZbbyTCDbK5ClFVodWs9rEguK91wJ564qAuVDxAU6yo36%2FkAz6%2F0w%3D%3D"}]}
-> server: cloudflare
-> strict-transport-security: max-age=31536000; includeSubDomains
-> transfer-encoding: chunked
-> vary: Accept-Encoding
-> x-android-received-millis: 1766502122418
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502122382
-> x-envoy-upstream-service-time: 9
-> x-ratelimit-limit: 300
-> x-ratelimit-remaining: 285
-> x-ratelimit-requests-limit: 7500
-> x-ratelimit-requests-remaining: 7135
BODY Content-Type: application/json
BODY START
{"get":"fixtures\/statistics","parameters":{"fixture":"1379009"},"errors":[],"results":2,"paging":{"current":1,"total":1},"response":[{"team":{"id":42,"name":"Arsenal","logo":"https:\/\/media.api-sports.io\/football\/teams\/42.png"},"statistics":[{"type":"Shots on Goal","value":3},{"type":"Shots off Goal","value":6},{"type":"Total Shots","value":12},{"type":"Blocked Shots","value":3},{"type":"Shots insidebox","value":11},{"type":"Shots outsidebox","value":1},{"type":"Fouls","value":11},{"type":"Corner Kicks","value":11},{"type":"Offsides","value":4},{"type":"Ball Possession","value":"67%"},{"type":"Yellow Cards","value":1},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":2},{"type":"Total passes","value":582},{"type":"Passes accurate","value":517},{"type":"Passes %","value":"89%"},{"type":"expected_goals","value":"0.89"},{"type":"goals_prevented","value":0}]},{"team":{"id":50,"name":"Manchester City","logo":"https:\/\/media.api-sports.io\/football\/teams\/50.png"},"statistics":[{"type":"Shots on Goal","value":3},{"type":"Shots off Goal","value":2},{"type":"Total Shots","value":5},{"type":"Blocked Shots","value":0},{"type":"Shots insidebox","value":4},{"type":"Shots outsidebox","value":1},{"type":"Fouls","value":10},{"type":"Corner Kicks","value":1},{"type":"Offsides","value":2},{"type":"Ball Possession","value":"33%"},{"type":"Yellow Cards","value":2},{"type":"Red Cards","value":null},{"type":"Goalkeeper Saves","value":2},{"type":"Total passes","value":300},{"type":"Passes accurate","value":229},{"type":"Passes %","value":"76%"},{"type":"expected_goals","value":"0.87"},{"type":"goals_prevented","value":0}]}]}
BODY END
2025-12-23 16:02:02.444  6193-6358  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Nottingham Forest (65) in fixture 1379095
2025-12-23 16:02:02.444  6193-6358  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Brighton (51) in fixture 1379095
2025-12-23 16:02:02.444  6193-6358  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379095 (Nottingham Forest vs Brighton)
2025-12-23 16:02:02.444  6193-6358  Ktor-ApiSports          com.Lyno.matchmindai                 D  RESPONSE https://v3.football.api-sports.io/fixtures/statistics?fixture=1379103 failed with exception: io.ktor.serialization.JsonConvertException: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 16:02:02.445  6193-6358  MatchRepository         com.Lyno.matchmindai                 W  ⚠️ Unexpected error for fixture 1379103: Illegal input: Unexpected JSON token at offset 25: Expected start of the object '{', but had '[' instead at path: $.parameters
JSON input: {"get": "","parameters": [],"errors": {"rateLimit": "To.....
2025-12-23 16:02:02.447  6193-6367  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Manchester City (50) in fixture 1379064
2025-12-23 16:02:02.447  6193-6367  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Bournemouth (35) in fixture 1379064
2025-12-23 16:02:02.447  6193-6367  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379064 (Manchester City vs Bournemouth)
2025-12-23 16:02:02.447  6193-6358  DataTrace               com.Lyno.matchmindai                 D  Unexpected error for fixture 1379103: JsonConvertException
2025-12-23 16:02:02.447  6193-6353  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Arsenal (42) in fixture 1379009
2025-12-23 16:02:02.447  6193-6353  DataTrace               com.Lyno.matchmindai                 D  No xG data for team Manchester City (50) in fixture 1379009
2025-12-23 16:02:02.447  6193-6353  DataTrace               com.Lyno.matchmindai                 D  ❌ No xG data found for fixture 1379009 (Arsenal vs Manchester City)
2025-12-23 16:02:02.447  6193-6353  DataTrace               com.Lyno.matchmindai                 D  ✅ Successfully enriched 0 matches with xG data (out of 20 total)
2025-12-23 16:02:02.447  6193-6353  DataTrace               com.Lyno.matchmindai                 W  ⚠️ WARNING: No xG data found for any of the 20 fixtures
2025-12-23 16:02:02.447  6193-6353  DataTrace               com.Lyno.matchmindai                 D  Possible causes: API rate limit, missing statistics, or league doesn't track xG
2025-12-23 16:02:02.447  6193-6353  DataTrace               com.Lyno.matchmindai                 D  xG data available for 0 fixtures
2025-12-23 16:02:02.447  6193-6353  System.out              com.Lyno.matchmindai                 I  🚀 FINAL CHECK: Sending Nottingham Forest vs Manchester City to Predictor
2025-12-23 16:02:02.447  6193-6353  System.out              com.Lyno.matchmindai                 I  🔒 PREDICTOR LOCKED ON: Nottingham Forest vs Manchester City (ID: 1379146)
2025-12-23 16:02:02.448  6193-6353  EnhancedScorePredictor  com.Lyno.matchmindai                 D  🔒 ID-BASED INTEGRITY: Analyzing Nottingham Forest vs Manchester City (ID: 1379146)
2025-12-23 16:02:02.448  6193-6353  EnhancedScorePredictor  com.Lyno.matchmindai                 D  🔎 ANALYZING: Nottingham Forest vs Manchester City
2025-12-23 16:02:02.448  6193-6353  EnhancedScorePredictor  com.Lyno.matchmindai                 D  📊 History Size: Home=11 matches, Away=12 matches
2025-12-23 16:02:02.448  6193-6353  System.out              com.Lyno.matchmindai                 I  🔒 PREDICTOR ANALYZING: Nottingham Forest vs Manchester City (from historical fixtures)
2025-12-23 16:02:02.448  6193-6353  EnhancedScorePredictor  com.Lyno.matchmindai                 D  STATE-OF-THE-ART PREDICTION: homeFixtures=11, awayFixtures=12, leagueFixtures=151, xgData=0 fixtures
2025-12-23 16:02:02.450  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  League Averages Data Sources: totalFixtures=151, homeAvg=1.57, awayAvg=1.24, sources=[GOALS_FALLBACK: 151 (100.0%)]
2025-12-23 16:02:02.450  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1378972: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.451  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.452  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.038 (timeWeight=0.192, qualityWeight=0.500)
2025-12-23 16:02:02.452  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1378972: dataSource=GOALS_FALLBACK, inputScore=3.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.452  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1378983: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.453  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.453  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.042 (timeWeight=0.210, qualityWeight=0.500)
2025-12-23 16:02:02.453  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1378983: dataSource=GOALS_FALLBACK, inputScore=1.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.453  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379012: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.455  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.455  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.059 (timeWeight=0.297, qualityWeight=0.500)
2025-12-23 16:02:02.455  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379012: dataSource=GOALS_FALLBACK, inputScore=1.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.455  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379027: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.456  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.457  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.065 (timeWeight=0.324, qualityWeight=0.500)
2025-12-23 16:02:02.457  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379027: dataSource=GOALS_FALLBACK, inputScore=0.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.457  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379037: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.457  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.458  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.072 (timeWeight=0.359, qualityWeight=0.500)
2025-12-23 16:02:02.458  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379037: dataSource=GOALS_FALLBACK, inputScore=2.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.458  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379051: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.459  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.459  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.094 (timeWeight=0.471, qualityWeight=0.500)
2025-12-23 16:02:02.459  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379051: dataSource=GOALS_FALLBACK, inputScore=2.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.459  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379065: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.459  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.459  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.102 (timeWeight=0.509, qualityWeight=0.500)
2025-12-23 16:02:02.459  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379065: dataSource=GOALS_FALLBACK, inputScore=2.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.459  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379075: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.459  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.459  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.113 (timeWeight=0.564, qualityWeight=0.500)
2025-12-23 16:02:02.460  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379075: dataSource=GOALS_FALLBACK, inputScore=3.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.460  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379095: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.460  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.461  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.148 (timeWeight=0.738, qualityWeight=0.500)
2025-12-23 16:02:02.461  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379095: dataSource=GOALS_FALLBACK, inputScore=0.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.461  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379108: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.461  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.462  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.153 (timeWeight=0.767, qualityWeight=0.500)
2025-12-23 16:02:02.462  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379108: dataSource=GOALS_FALLBACK, inputScore=0.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.462  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379133: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.462  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=11, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.462  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.196 (timeWeight=0.979, qualityWeight=0.500)
2025-12-23 16:02:02.462  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379133: dataSource=GOALS_FALLBACK, inputScore=1.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.463  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1378975: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.464  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.464  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.038 (timeWeight=0.189, qualityWeight=0.500)
2025-12-23 16:02:02.464  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1378975: dataSource=GOALS_FALLBACK, inputScore=4.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.464  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1378986: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.465  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.465  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.041 (timeWeight=0.207, qualityWeight=0.500)
2025-12-23 16:02:02.465  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1378986: dataSource=GOALS_FALLBACK, inputScore=2.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.465  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1378990: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.465  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.466  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.046 (timeWeight=0.229, qualityWeight=0.500)
2025-12-23 16:02:02.466  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1378990: dataSource=GOALS_FALLBACK, inputScore=1.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.466  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379009: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.466  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.466  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.060 (timeWeight=0.300, qualityWeight=0.500)
2025-12-23 16:02:02.466  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379009: dataSource=GOALS_FALLBACK, inputScore=1.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.466  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379025: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.467  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.468  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.065 (timeWeight=0.324, qualityWeight=0.500)
2025-12-23 16:02:02.468  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379025: dataSource=GOALS_FALLBACK, inputScore=1.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.468  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379032: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.468  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.468  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.072 (timeWeight=0.359, qualityWeight=0.500)
2025-12-23 16:02:02.469  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379032: dataSource=GOALS_FALLBACK, inputScore=1.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.469  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379044: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.469  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.469  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.085 (timeWeight=0.425, qualityWeight=0.500)
2025-12-23 16:02:02.469  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379044: dataSource=GOALS_FALLBACK, inputScore=0.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.469  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379050: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.469  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.470  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.094 (timeWeight=0.471, qualityWeight=0.500)
2025-12-23 16:02:02.470  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379050: dataSource=GOALS_FALLBACK, inputScore=0.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.470  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379064: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.470  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.470  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.103 (timeWeight=0.515, qualityWeight=0.500)
2025-12-23 16:02:02.470  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379064: dataSource=GOALS_FALLBACK, inputScore=1.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.471  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379087: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.471  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.471  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.133 (timeWeight=0.666, qualityWeight=0.500)
2025-12-23 16:02:02.471  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379087: dataSource=GOALS_FALLBACK, inputScore=1.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.471  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379094: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.472  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.472  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.146 (timeWeight=0.729, qualityWeight=0.500)
2025-12-23 16:02:02.472  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379094: dataSource=GOALS_FALLBACK, inputScore=2.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.472  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 W  Using actual goals fallback for fixture 1379103: No xG data available. Score variance may be inflated.
2025-12-23 16:02:02.472  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Time Decay Half-Life: fixtureCount=12, dataQuality=0.50, finalHalfLife=54.0 days, 365-day-weight=0.009
2025-12-23 16:02:02.472  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Source Penalty Applied: source=GOALS_FALLBACK, penalty=0.4x, finalWeight=0.151 (timeWeight=0.757, qualityWeight=0.500)
2025-12-23 16:02:02.472  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Fixture 1379103: dataSource=GOALS_FALLBACK, inputScore=5.00, baseQuality=0.30, sourceMultiplier=0.40, finalQuality=0.30
2025-12-23 16:02:02.472  6193-6353  System.out              com.Lyno.matchmindai                 I  [DEBUG] Bayesian Smoothing (C=2.0):
2025-12-23 16:02:02.473  6193-6353  System.out              com.Lyno.matchmindai                 I    isAttack=true, totalWeightedXg=1.286
2025-12-23 16:02:02.473  6193-6353  System.out              com.Lyno.matchmindai                 I    totalMatches=1.082, smoothedStrength=1.436
2025-12-23 16:02:02.473  6193-6353  System.out              com.Lyno.matchmindai                 I    normalizedStrength=0.915
2025-12-23 16:02:02.474  6193-6353  System.out              com.Lyno.matchmindai                 I  [DEBUG] Bayesian Smoothing (C=2.0):
2025-12-23 16:02:02.474  6193-6353  System.out              com.Lyno.matchmindai                 I    isAttack=false, totalWeightedXg=1.127
2025-12-23 16:02:02.474  6193-6353  System.out              com.Lyno.matchmindai                 I    totalMatches=1.082, smoothedStrength=1.170
2025-12-23 16:02:02.474  6193-6353  System.out              com.Lyno.matchmindai                 I    normalizedStrength=0.944
2025-12-23 16:02:02.474  6193-6353  System.out              com.Lyno.matchmindai                 I  [DEBUG] Bayesian Smoothing (C=2.0):
2025-12-23 16:02:02.474  6193-6353  System.out              com.Lyno.matchmindai                 I    isAttack=true, totalWeightedXg=1.762
2025-12-23 16:02:02.475  6193-6353  System.out              com.Lyno.matchmindai                 I    totalMatches=1.034, smoothedStrength=1.397
2025-12-23 16:02:02.475  6193-6353  System.out              com.Lyno.matchmindai                 I    normalizedStrength=1.128
2025-12-23 16:02:02.475  6193-6353  System.out              com.Lyno.matchmindai                 I  [DEBUG] Bayesian Smoothing (C=2.0):
2025-12-23 16:02:02.475  6193-6353  System.out              com.Lyno.matchmindai                 I    isAttack=false, totalWeightedXg=2.060
2025-12-23 16:02:02.475  6193-6353  System.out              com.Lyno.matchmindai                 I    totalMatches=1.034, smoothedStrength=1.713
2025-12-23 16:02:02.475  6193-6353  System.out              com.Lyno.matchmindai                 I    normalizedStrength=1.092
2025-12-23 16:02:02.475  6193-6353  EnhancedScorePredictor  com.Lyno.matchmindai                 D  ⚔️ Home Attack Strength: Raw=-0.278 -> Bayesian=-0.089
2025-12-23 16:02:02.475  6193-6353  EnhancedScorePredictor  com.Lyno.matchmindai                 D  🛡️ Home Defense Strength: Raw=-0.172 -> Bayesian=-0.057
2025-12-23 16:02:02.475  6193-6353  EnhancedScorePredictor  com.Lyno.matchmindai                 D  ⚔️ Away Attack Strength: Raw=0.319 -> Bayesian=0.120
2025-12-23 16:02:02.475  6193-6353  EnhancedScorePredictor  com.Lyno.matchmindai                 D  🛡️ Away Defense Strength: Raw=0.238 -> Bayesian=0.088
2025-12-23 16:02:02.475  6193-6353  DataTrace               com.Lyno.matchmindai                 D  Home Attack Delta: -0.278 (If close to 0.0, team is considered average)
2025-12-23 16:02:02.475  6193-6353  DataTrace               com.Lyno.matchmindai                 D  Away Attack Delta: 0.319 (If close to 0.0, team is considered average)
2025-12-23 16:02:02.476  6193-6353  DataTrace               com.Lyno.matchmindai                 D  Smoothing: Raw=0.757 -> Smoothed=0.915 (Impact: 120%)
2025-12-23 16:02:02.476  6193-6353  DataTrace               com.Lyno.matchmindai                 D  Smoothing (Away): Raw=1.375 -> Smoothed=1.128 (Impact: 82%)
2025-12-23 16:02:02.476  6193-6353  System.out              com.Lyno.matchmindai                 I  [DEBUG] Raw Team Strengths (log scale):
2025-12-23 16:02:02.476  6193-6353  System.out              com.Lyno.matchmindai                 I    Home Attack: -0.089
2025-12-23 16:02:02.476  6193-6353  System.out              com.Lyno.matchmindai                 I    Home Defense: -0.057
2025-12-23 16:02:02.476  6193-6353  System.out              com.Lyno.matchmindai                 I    Away Attack: 0.120
2025-12-23 16:02:02.476  6193-6353  System.out              com.Lyno.matchmindai                 I    Away Defense: 0.088
2025-12-23 16:02:02.476  6193-6353  System.out              com.Lyno.matchmindai                 I  [DEBUG] Raw Strength vs Bayesian Strength (C=2.0):
2025-12-23 16:02:02.476  6193-6353  System.out              com.Lyno.matchmindai                 I    Home Attack: Raw=-0.278 vs Bayesian=-0.089
2025-12-23 16:02:02.477  6193-6353  System.out              com.Lyno.matchmindai                 I    Home Defense: Raw=-0.172 vs Bayesian=-0.057
2025-12-23 16:02:02.477  6193-6353  System.out              com.Lyno.matchmindai                 I    Away Attack: Raw=0.319 vs Bayesian=0.120
2025-12-23 16:02:02.477  6193-6353  System.out              com.Lyno.matchmindai                 I    Away Defense: Raw=0.238 vs Bayesian=0.088
2025-12-23 16:02:02.477  6193-6353  EnhancedScorePredictor  com.Lyno.matchmindai                 D  Home advantage calculation: leagueHomeAvg=1.57, leagueAwayAvg=1.24, ratio=1.27, adjustedRatio=1.27
2025-12-23 16:02:02.478  6193-6353  DataTrace               com.Lyno.matchmindai                 D  Stats for Team 65: xG_Coverage=0.00, Raw_Goals=1.19, Data_Quality=0.05
2025-12-23 16:02:02.478  6193-6353  DataTrace               com.Lyno.matchmindai                 W  ⚠️ LOW DATA QUALITY WARNING: Team 65 has qualityScore=0.05 (< 0.2). Model may ignore this data.
2025-12-23 16:02:02.478  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Quality Analysis: fixtures=11, xgCoverage=0.0%, avgQuality=0.12, combinedQuality=0.05
2025-12-23 16:02:02.478  6193-6353  DataTrace               com.Lyno.matchmindai                 D  Stats for Team 39: xG_Coverage=0.00, Raw_Goals=1.70, Data_Quality=0.05
2025-12-23 16:02:02.478  6193-6353  DataTrace               com.Lyno.matchmindai                 W  ⚠️ LOW DATA QUALITY WARNING: Team 39 has qualityScore=0.05 (< 0.2). Model may ignore this data.
2025-12-23 16:02:02.478  6193-6353  ExpectedGoalsService    com.Lyno.matchmindai                 D  Data Quality Analysis: fixtures=12, xgCoverage=0.0%, avgQuality=0.12, combinedQuality=0.05
2025-12-23 16:02:02.478  6193-6353  System.out              com.Lyno.matchmindai                 I  [DEBUG] Adjusted Team Strengths (exp scale):
2025-12-23 16:02:02.478  6193-6353  System.out              com.Lyno.matchmindai                 I    Home Attack: 0.915
2025-12-23 16:02:02.478  6193-6353  System.out              com.Lyno.matchmindai                 I    Home Defense: 0.944
2025-12-23 16:02:02.479  6193-6353  System.out              com.Lyno.matchmindai                 I    Away Attack: 1.128
2025-12-23 16:02:02.479  6193-6353  System.out              com.Lyno.matchmindai                 I    Away Defense: 1.092
2025-12-23 16:02:02.479  6193-6353  System.out              com.Lyno.matchmindai                 I    Home Advantage: 1.267
2025-12-23 16:02:02.479  6193-6353  EnhancedScorePredictor  com.Lyno.matchmindai                 D  🧮 Lambda Calculation: Attack(0.915) * Defense(1.092) * LeagueAvg(1.570) * HomeAdv(1.267) = 1.987
2025-12-23 16:02:02.479  6193-6353  EnhancedScorePredictor  com.Lyno.matchmindai                 D  🧮 Lambda Calculation (Away): Attack(1.128) * Defense(0.944) * LeagueAvg(1.238) = 1.319
2025-12-23 16:02:02.479  6193-6353  System.out              com.Lyno.matchmindai                 I  [DEBUG] Raw Expected Goals (before soft-cap):
2025-12-23 16:02:02.479  6193-6353  System.out              com.Lyno.matchmindai                 I    Home: 1.987
2025-12-23 16:02:02.479  6193-6353  System.out              com.Lyno.matchmindai                 I    Away: 1.319
2025-12-23 16:02:02.479  6193-6353  DataTrace               com.Lyno.matchmindai                 D  📊 RANK CORRECTION: No rank data available, skipping correction
2025-12-23 16:02:02.480  6193-6353  EnhancedScorePredictor  com.Lyno.matchmindai                 D  🧢 Soft-Cap Check: Raw Lambda Home=1.987 -> Capped Lambda Home=1.987 (NO_CAP)
2025-12-23 16:02:02.480  6193-6353  EnhancedScorePredictor  com.Lyno.matchmindai                 D  🧢 Soft-Cap Check: Raw Lambda Away=1.319 -> Capped Lambda Away=1.319 (NO_CAP)
2025-12-23 16:02:02.480  6193-6353  System.out              com.Lyno.matchmindai                 I  [DEBUG] Capped Expected Goals (after soft-cap):
2025-12-23 16:02:02.480  6193-6353  System.out              com.Lyno.matchmindai                 I    Home: 1.987 (capped: NO)
2025-12-23 16:02:02.480  6193-6353  System.out              com.Lyno.matchmindai                 I    Away: 1.319 (capped: NO)
2025-12-23 16:02:02.481  6193-6353  EnhancedScorePredictor  com.Lyno.matchmindai                 D  STATE-OF-THE-ART Probability Capping: Raw=0.50-0.26-0.25, Capped=0.50-0.26-0.25
2025-12-23 16:02:02.481  6193-6353  EnhancedScorePredictor  com.Lyno.matchmindai                 D  STATE-OF-THE-ART RESULTS: expectedGoals=1.99-1.32, probabilities=0.50-0.26-0.25, confidence=0.27, hasModifiers=false, lambdaSoftCap=NOT_NEEDED
2025-12-23 16:02:02.486  6193-6345  MatchRepository         com.Lyno.matchmindai                 D  Searching Tavily for query: Nottingham Forest vs Manchester City Premier League news injuries
2025-12-23 16:02:02.490  6193-6345  SearchService           com.Lyno.matchmindai                 D  Searching with custom query (raw): 'Nottingham Forest vs Manchester City Premier League news injuries' (focus: news)
2025-12-23 16:02:02.491  6193-6345  TavilyApi               com.Lyno.matchmindai                 D  Searching Tavily with query: 'Nottingham Forest vs Manchester City Premier League news injuries', focus: 'news'
2025-12-23 16:02:02.495  6193-6345  Ktor                    com.Lyno.matchmindai                 D  REQUEST: https://api.tavily.com/search
METHOD: HttpMethod(value=POST)
COMMON HEADERS
-> Accept: application/json
-> Accept-Charset: UTF-8
CONTENT HEADERS
-> Content-Length: 248
-> Content-Type: application/json
BODY Content-Type: application/json
BODY START
{
"api_key": "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",
"query": "Nottingham Forest vs Manchester City Premier League news injuries",
"search_depth": "advanced",
"topic": "news",
"max_results": 5,
"include_answer": false
}
BODY END
2025-12-23 16:02:02.835  6193-6300  Ktor                    com.Lyno.matchmindai                 D  RESPONSE: 200 OK
METHOD: HttpMethod(value=POST)
FROM: https://api.tavily.com/search
COMMON HEADERS
-> connection: keep-alive
-> content-length: 7929
-> content-security-policy: default-src 'none'; script-src 'self'; connect-src 'self'; img-src 'self'; style-src 'self';base-uri 'self';form-action 'self'; require-trusted-types-for 'script'; upgrade-insecure-requests;
-> content-type: application/json
-> date: Tue, 23 Dec 2025 15:02:04 GMT
-> server: uvicorn
-> x-android-received-millis: 1766502122827
-> x-android-response-source: NETWORK 200
-> x-android-selected-protocol: http/1.1
-> x-android-sent-millis: 1766502122720
BODY Content-Type: application/json
BODY START
2025-12-23 16:02:02.835  6193-6300  Ktor                    com.Lyno.matchmindai                 D  {"query":"Nottingham Forest vs Manchester City Premier League news injuries","follow_up_questions":null,"answer":null,"images":[],"results":[{"url":"https://www.sportingnews.com/uk/football/news/fulham-vs-nottingham-forest-prediction-lineups-odds-bet-builder-premier-league-mnf/0b102eec003d4e42d2034035","title":"Fulham vs. Nottingham Forest prediction, lineups, odds and bet builder for Premier League MNF - sportingnews.com","score":0.9124362,"published_date":"Mon, 22 Dec 2025 09:20:01 GMT","content":"## Fulham vs. Nottingham Forest Team News\n\n### Fulham Team News\n\n This is Fulham's first league game without Calvin Bassey, Alex Iwobi and Samuel Chukwueze, who are all out at the Africa Cup of Nations with Nigeria.\n Jorge Cuenca, Sasa Lukic and club-record signing Kevin will likely start in their places.\n Ryan Sessegnon and Rodrigo Muniz continue to be out with injury.\n\n### Nottingham Forest Team News [...] Like their opponents, Forest are without two AFCON stars with Ibrahim Sangare and Wily Boly called up by Ivory Coast.\n Taiwo Awoyini and Ola Ainamissed out on selection for Nigeria due to injury and are joined by Chris Wood and Ryan Yates on the sidelines.\n In better news for Dyche, first-choice keeper Matz Sels is likely to be fit after missing the win over Spurs with a groin issue.\n\n## Fulham vs. Nottingham Forest Lineups: Predicted Starting XI’s [...] ## Fulham vs. Nottingham Forest: Form Guide\n\n### Fulham – Last 5 matches (All competitions)\n\n Newcastle 2-1 Fulham (EFL Cup)\n Burnley 2-3 Fulham (Premier League)\n Fulham 1-2 Crystal Palace (Premier League)\n Fulham 4-5 Manchester City (Premier League)\n Tottenham 1-2 Fulham (Premier League)\n\n### Nottingham Forest – Last 5 matches (All competitions)","raw_content":null},{"url":"https://www.skysports.com/share/13483780","title":"Fulham 1-0 Nottingham Forest: Raul Jimenez penalty earns Cottagers fortunate victory over toothless Forest - Sky Sports","score":0.09268778,"published_date":"Mon, 22 Dec 2025 22:20:59 GMT","content":"#### Nottingham Forest 0\n\nLatest Premier League Odds\n\n Get Sky Sports\n Get a NOW Sports Membership\n\n Watch Live\n\n# Fulham 1-0 Nottingham Forest: Raul Jimenez penalty earns Cottagers fortunate victory over toothless Forest\n\nReport and free match highlights as Raul Jimenez's first-half penalty earns Fulham narrow 1-0 win over Nottingham Forest in game of few chances at Craven Cottage; visitors fail to test Bernd Leno before Jimenez misses late chance to double lead\n\nRon Walker [...] The Cottagers struggled to create in their first Premier League match without the attacking talent of Alex Iwobi and Samuel Chukwueze, but in a moment of rare quality earned their match-winning penalty when Kevin's quick feet proved too fast for Douglas Luiz, who clumsily brought him down inside his own box.\n\nThat aside, Forest goalkeeper John Victor was a relative spectator in west London with Forest controlling much of the match, without the cutting edge required to force an equaliser. [...] Skip to content\n\nSky Sports Homepage\n\n Menu\n\n Home\n Scores\n Watch\n Sky Bet\n Shop\n + Podcasts\n  + Upcoming on Sky\n  + Get Sky Sports\n  + Sky Sports App\n  + Sky Sports with no contract\n  + Kick It Out\n  + British South Asians in Football\n\nWatch Sky Sports\n\n# Fulham vs Nottingham Forest; Premier League\n\nFTFull Time AETAfter Extra Time LIVEThis is a live match. ETExtra Time HTHalf Time\n\nFulham vs Nottingham Forest. Premier League.\n\nCraven Cottage.\n\n#### Fulham 1\n\n R Jiménez (45+5'50th minute pen)","raw_content":null},{"url":"https://www.premierleague.com/en/match/2562059/fulham-vs-nottingham-forest","title":"Fulham v Nottingham Forest | 2025/2026 | Premier League | Overview - Premier League","score":0.045183755,"published_date":"Tue, 23 Dec 2025 06:23:27 GMT","content":"Fulham\n\n1-0\n\nHT 1 - 0\n\nNott'm Forest\n\n Jiménez 45'+5' (Pen)\n\n Jiménez 45'+7'\n Berge 56'\n Wilson 77'\n Cuenca 82'\n Andersen 90'+4'\n\n Victor 45'+4'\n Murillo 70'\n Milenkov
2025-12-23 16:02:02.835  6193-6300  Ktor                    com.Lyno.matchmindai                 D  BODY END
2025-12-23 16:02:02.840  6193-6345  TavilyApi               com.Lyno.matchmindai                 D  Tavily search successful: 5 results
2025-12-23 16:02:02.840  6193-6345  MatchRepository         com.Lyno.matchmindai                 D  Tavily search found 5 results for query: Nottingham Forest vs Manchester City Premier League news injuries
2025-12-23 16:02:02.841  6193-6345  System.out              com.Lyno.matchmindai                 I  Breaking news found: [Fulham vs. Nottingham Forest prediction, lineups, odds and bet builder for Premier League MNF - sportingnews.com, Fulham 1-0 Nottingham Forest: Raul Jimenez penalty earns Cottagers fortunate victory over toothless Forest - Sky Sports, Fulham v Nottingham Forest | 2025/2026 | Premier League | Overview - Premier League]
2025-12-23 16:02:15.331  6193-6193  MatchDetailScreen       com.Lyno.matchmindai                 D  Screen loaded with fixtureId: 1379146
2025-12-23 16:02:17.122  6193-6193  WindowOnBackDispatcher  com.Lyno.matchmindai                 W  sendCancelIfRunning: isInProgress=false callback=androidx.activity.OnBackPressedDispatcher$Api34Impl$createOnBackAnimationCallback$1@9f455f1
2025-12-23 16:02:17.142  6193-6193  HWUI                    com.Lyno.matchmindai                 W  Image decoding logging dropped!
2025-12-23 16:02:17.143  6193-6193  HWUI                    com.Lyno.matchmindai                 W  Image decoding logging dropped!
2025-12-23 16:02:18.734  6193-6193  MatchDetailScreen       com.Lyno.matchmindai                 D  Screen loaded with fixtureId: 1379146
2025-12-23 16:02:18.734  6193-6193  MatchDetailScreen       com.Lyno.matchmindai                 D  Creating ViewModel with fixtureId: 1379146
2025-12-23 16:02:18.735  6193-6367  MatchRepository         com.Lyno.matchmindai                 W  No match details found for fixture 1379146
2025-12-23 16:02:19.996  6193-6193  MatchDetailScreen       com.Lyno.matchmindai                 D  Screen loaded with fixtureId: 1379146
