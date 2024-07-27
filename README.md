<br>
<div align="center">

 <a href=".">![GitHub contributors](https://img.shields.io/github/contributors/radus7efan/store-management-tool)</a>
 <a href=".">![GitHub commit activity (branch)](https://img.shields.io/github/commit-activity/t/radus7efan/stock-price-predictor)</a>
</div>

<div>
<h2 align="center"> 📈 Stock Price Predictor [SPP] 📉 </h2>

  <p align="center">
    ___________________________________
    <br />
    <br />
    <a href="#installation">Installation</a>
    ·
    <a href="#how-to-play">How To Play</a>
    ·
    <a href="#contact">Contact</a>
    <br>
    ___________________________________
  </p>
</div>

### About The Project

- `Stock Price Predictor` is an application meant to be used for fetching stock prices and predicting future prices.

<br>

- The application supports one command line arguments that specifies the maximum number of files to be sampled for each exchange. If the value is higher that the available number of sample files, all the files will be processed for that exchange. The Default value that will be used if the argument is not provided is `2`.

<br>

- The output of the predict operation is returned by the API and also saved in a csv file.
    * The file structure will be the same as for the input files: `/output/EXCHANGE_NAME/STOCK_ID`. 
    * Each file name will have the date and time on which it was created concatenated, so the output won't be overwritten, but a new file is generated each time.

<br>

- For the persisting layer the application uses a `h2` in memory database to store stock prices.

<br>

- The prediction algorithm is the one suggested, with the only change that for the second and third prices, the operation (add or subtract of the difference) is randomly decided.

  * first predicted (`n+1`) data point is same as the second-highest value present in the 10 data points
  * `n+2` data point has half the difference between `n` and `n+1`
  * `n+3` data point has 1/4th the difference between `n+1` and `n+2`

<br>

- There are two available operations:
  - Get Prices
    - Get persisted stock prices. ( *getStockPrices* )
    - The operation accepts three query parameters representing the `Exchange`, `Stock-Id` and a `Timestamp` (in `dd-MM-yyy` format) and supports filtering based on the parameters.
    - If no parameter is passed, it will return 10 consecutive prices starting from a random `Timestamp` for each `Stock-Id` available for each `Exchange`.
    - Filtering can be applied using different combinations of the query parameters mentioned above:
      - `Exchange` is provided - will return 10 consecutive prices starting from a random `Timestamp` for each `Stock-Id` available for the provided `Exchange`.
      - `Stock-Id` is provided - will return 10 consecutive prices starting from a random `Timestamp` for the provided `Stock-Id`.
      - `Timestamp` is provided - will be used as a starting timestamp when querying the stock prices and will return prices only prices starting from that timestamp. If it is not provided, a random timestamp will be generated.
      - If all the parameters are provided, the application will return 10 consecutive stock prices for the provided `Exchange` & `Stock-Id` starting from the provided `Timestamp`.
  - Output example for the `Get Stock Prices` operation:
```json
{
  "LSE": [
    {
      "stockId": "FLTR",
      "timestamp": "2023-11-13",
      "price": 17613.24
    },
    {
      "stockId": "FLTR",
      "timestamp": "2023-11-14",
      "price": 17525.17
    },
    {
      "stockId": "FLTR",
      "timestamp": "2023-11-15",
      "price": 17542.7
    },
    ...
  ]
}
```

<br>

- Predict future prices
  - Predict future stock prices based on a list of stock prices sent as input. ( *getPredictedPrices* )
  - This operation is a POST and accepts as a request body, a list of 10 stock prices

- Input example for the `Predict Prices` operation:
```json
[
  {
    "stockId": "ASH",
    "timestamp": "2023-11-30",
    "price": 115.66
  },
  {
    "stockId": "ASH",
    "timestamp": "2023-12-01",
    "price": 117.39
  },
  ...
]
```
- Output example for the `Predict Prices` operation:
```json
[
  {
    "stockId": "ASH",
    "timestamp": "2023-11-30",
    "price": 115.66
  },
  {
    "stockId": "ASH",
    "timestamp": "2023-12-01",
    "price": 117.39
  },
  ...
]
```


### Future improvements

- Add unit & integration tests and improve test coverage to at least 80% if possible.
- Add multiple prediction algorithms, so that we can retrieve predicted prices using different algorithms.
- Train an ML model on the input dataset, so it can predict future prices. Even thought the data set is quite small and not sufficient for a good result, it would be nice to have for a demonstration.
- Endpoints to compare predicted prices, calculate averages, get price trend, generate plots with actual price and predicted values.
- Add new operations for persisting new prices, add new exchanges, add new stocks, update the current values, etc.
- Add pagination and multiple filtering options for the existing endpoints, optimize fetching operations when retrieved data is large.
- Set up a real database instead of an in memory database and keep persisted prices instead of populating the database each time the application starts.
- Add metrics to evaluate elapsed time for each operation and performance.
- Configure and enable swagger-ui for an easier interaction with the application.
- Define an Open API contract for the application that describes all the available operations and details about the input/output of the application.
- Add authentication & authorization, some users may not have access to the predictor or some exchanges.
 
### Built With

<div align="center">

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Apache Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)

</div>

### Getting Started

- To get a local copy up and running follow these simple steps.

### Prerequisites

What you need to run this application locally:
- [Java 17](https://www.oracle.com/java/technologies/downloads/) or later
- [Maven 3.5+](https://maven.apache.org/download.cgi)

### Installation

1. Clone the repository
   ```sh
   git clone git@github.com:radus7efan/stock-price-predictor.git
   ```
2. Build the application
   ```sh
    mvn clean package
   ```
   - this step will build the application and generate the necessary code.


3. Run the appplication
   ```sh
   java -jar target/stock-price-predictor-0.0.1-SNAPSHOT.jar 2
   ```
4. After the application starts, you will be able to call any of the exposed endpoints using the base path:
    ```rest
    http://localhost:8182/api/v1/stock-price/
    ```
    Examples:
    ```rest
    GET http://localhost:8182/api/v1/stock-price/prices
    GET http://localhost:8182/api/v1/stock-price/prices?exchange=LSE
    GET http://localhost:8182/api/v1/stock-price/prices?exchange=LSE&timestamp=01-01-2023
    GET http://localhost:8182/api/v1/stock-price/prices?exchange=LSE&stockName=FLTR&timestamp=01-01-2023
    PSOT http://localhost:8182/api/v1/stock-price/predict
    ```

### How to play

- Clone, Build & Run the application using the instructions above
- Use cURL, Postman or your preferred software to call the endpoints exposed by the application

<div align="center">

![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white)

[Postman Collection](Stock-Price-Predictor.postman_collection.json)

</div>


### Contact

<div align="center">
<h3>Radu Stefan
</h3>

[![LinkedIn][linkedin-shield]][linkedin-url]
</div>


[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://linkedin.com/in/radu-stefan-710
