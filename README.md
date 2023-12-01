# Currency Converter


### Task:
- Write a currency converter.
- It must convert the currency taking into account the current exchange rate (for the day) and the agent commission.



### **Project requirements:**
- As a source of currency data, following sources are used:
    - https://cbu.uz/ru/arkhiv-kursov-valyut/
    - https://cbu.uz/ru/arkhiv-kursov-valyut/json/all/2023-12-01/
- Use **Postgresql** for storage
- Use **Java Spring** for backend
- When the service starts, the **currency** in the database should be updated to the **current day**
- **GET** methods return JSON
- **POST** methods accept JSON
- The **secret key** must be stored in a file
- The currency is displayed in the format up to the **6th decimal place** (inclusive)
- The database and application must run in **docker**. (OPTIONAL (increased complexity): docker-compose)


**Initial data:**
- **Secret key** is saved to database from file.
- **Commissions** are created and inserted into database for each currency pair (taken from Central Bank's API).
- **Accounts** are created and inserted into database for each currency with some initial balance.


**Commands:**
- GET | **http://localhost:8080/api/convert?from=USD&to=UZS&amount=100**
    - Returns the amount of currency that the user will receive after conversion (but does not perform the conversion).
    - Currency is transferred from '**from**' to '**to**', taking into account the current exchange rate and the deduction of an agent commission for each conversion.
      
    - The commission for each conversion is taken from the database and is 0% by default.
    - At the same time, commissions for **UZS -> USD** and **USD -> UZS** may be different.
    - Conversion not from (to) UZS to (from) any other currency occurs through double conversion of UZS and the commission must be deducted twice taking into account different commissions
    - For the base rate, use the **Central Bank** rate for the current day

- GET | **http://localhost:8080/api/officialrates?date=2023-11-30&pair=USD/RUB**
    - Returns the **official Central Bank** rate on the desired day for the desired currency pair.
      
- POST | **http://localhost:8080/api/convert** | Header: {"Secret-Key": "a@h62HekGws2451mngGsi97f2sg4"}, Body: {"from":"USD","to":"AED", "amount": "3333"} 
    - Performs currency conversion. Debits money from accounts in the currency we issue, charges the currency we accept and additional commissions.
    - If there is not enough money to issue, the conversion is not performed and an error message is returned in **JSON** format and code **403**.
    - If such currency does not exist, returns error message in **JSON** format with the status code **404**.

- POST | **http://localhost:8080/api/setcommission**
    - New commission amount is set to currency pair.
    - If the pair does not exist, returns error message in **JSON** format with the status code **404**.
    - The method must check for the presence of a secret key in the header. If the key does not match the secret key, returns error message in **JSON** format with the status code **403**.

**Instruction to run the project:**
- Clone the repository with the following git command:
```
git clone https://github.com/laziz511/Uzum-Final-Task
```

- In the project folder, run the project with the following docker command:
```
docker-compose up --build
```
