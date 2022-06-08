# Account Manager
A simple webservice exposing a REST API for
fetching the balance of existing accounts & transferring funds

## Running
Run the application by running it with the provided `bootRun` gradle task

Use either the gradle wrapper
```bash
./gradlew bootRun
```
or if you feel confident, your own provided system gradle
```bash
gradle bootRun
```

## Tests
Run all tests with...
```
./gradlew test
``` 

## API

### **GET** `/accounts/<ACCOUNT_ID>`
Returns the current balance of the provided account. See example below
```
curl localhost:8080/accounts/12345678 | jq

{
  "balance": {
    "amount": 1000000,
    "currency": "HKD",
    "formatted": "HKD1,000,000.00"
  }
}
```
### **POST** `/accounts/<ACCOUNT_ID>/transfer`
Transfers funds from the provided account in the path to the destination account in the request. See example below
```
 curl -X POST  localhost:8080/accounts/12345678/transfer \
   -H 'Content-Type: application/json' \
   -d '{"destination_account_number":"88888888","amount":{ "currency": "HKD", "amount": 2000}}'
```
We just sent `88888888` 2000 HKD. Let's verify that the funds were transferred correctly!

First we can check the source account...
```
curl localhost:8080/accounts/12345678 | jq 
{
  "balance": {
    "amount": 998000,
    "currency": "HKD",
    "formatted": "HKD998,000.00"
  }
}
```
And the destination account...
```
curl localhost:8080/accounts/88888888 | jq 
{
  "balance": {
    "amount": 1002000,
    "currency": "HKD",
    "formatted": "HKD1,002,000.00"
  }
}
```
All good!

#### Error bodies

Not enough funds:
```
{
  "error":"NOT_ENOUGH_FUNDS",
  "message":"Not enough funds to execute transaction"
}
```

Destination account not found:
```
{
  "error":"DESTINATION_ACCOUNT_NOT_FOUND",
  "message":"Destination account not found for transfer"
}
```