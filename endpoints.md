# Ktor Endpoints

### Default

```
'/' -> boilerplate endpoint. You can hit it to check if the server is running
```

### Users (Entities that use the app)

```
/user/{id} -> GET : gets user info given id
```

input: user id in URL \
output: user info as JSON

```
/user/exchange_id/{auth_id} -> GET : gets user id by auth id
```

input: auth id in URL \
output: user id

```
/user/insert -> POST : inserts user into db
```

input: user info as json (user insert model) \
output: success message, inserts into db

```
/user/update/{id} -> PATCH : updates user in db given id
```

input: id in url, user info as json (user update model) \
output: user info object if successful, updates in db

```
/user/liked -> POST : inserts liked trip related to user into db
```

input: user and trip id as json (liked insert model) \
output: success message, inserts into db

```
/user/disliked -> DELETE : deletes previously liked trip by user
```
input: user and trip id as json (liked insert model) \
output: success message, deletes from db

```
/user/{id}/friends -> GET : get friends of user
```

input: user id in url \
output: json of friends

```
/user/{id}/pins -> GET : get pins related to user
```

input: user id in url \
output: json of pins

```
/user/{id}/trips -> GET : get trips created by user
```

input: user id in url \
output: json of trips

```
/user/{userid}/trips/{tripid}/pins -> GET : get pins specific to trip of user
```

input: user and trip id in url \
output: json of pins

```
/user/exists/{id} -> GET : check if user with specified authid id exists in db
```

input: user auth id in url \
output: boolean value of whether user exists

### Pins (Location objects)

```
/pin/insert -> POST : create pin
```

input: pin info as json (pin insert model) \
output: success message, inserts into db

```
/trip/{id}/pins -> GET : get all pins of a trip
```

output: json of pins

### Trips (Accumulation of Pins)

```
/trips -> GET : get all existing trips
```

output: json of all trips

```
/trip/insert -> POST : create trip
```

input: trip info as json (trip insert model) \
output: id of inserted trip

```
/trip/{id} -> DELETE : deletes trip
```
input: trip id in url \
output: success message


```
/trip/top/{n} -> GET : get top n trips by score
```

input: "n" variable in url \
output: json of trips

```
/trip/update/{id} -> PATCH : update trip in db given trip id
```

input: trip id in url \
output: trip info object if successful, updates in db

```
/trip/{id}/pins-delete -> DELETE : delete all pins with associated trip id
```

input: trip id in url \
output: success message, pins deleted from db

### Friends endpoints
```
/friends/insert -> POST : insert a friend relation
```
input: friendship info as json (friend insert model) \
output: success message, friends inserted

```
/friends/remove -> POST : remove a friend relation
```
input: friendship info as json (friend insert model) \
output: success message, friends removed



### AI endpoints

```
/ai/{id} -> GET : get an ai suggestion of where user should visit next
```

input: id in url \
output: string in json

```
/ai/profiler/{id} -> GET : get an ai suggestion for type of user profile
```

input: id in url \
output: string in json


