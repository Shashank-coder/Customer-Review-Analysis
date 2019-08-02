# Customer-Review-Analysis
This is a Big Data Project which handles customer reviews as data is streamed and real-time analysis is done and visualized using a scalable distributed system.

## Introduction

Online shopping is growing on a daily basis and we can see that more and more consumers are preferring shopping online rather than to go out and shop. We can see that there the increase in preference for online shopping is due to the increased feasibility in access to the internet and the number of facilities provided by this. It is much easier for a customer to just sit in front of their computer and visit various stores and select what they want. Because of this it is easy for them to get reviews about the products from people who have already used it and it can give a better insight for the customers to make decisions about the products they want to buy and helps them making better decisions.
When there is such a demand for online shopping we can make it better by doing some research about what people are buying, how they are liking it, what review they give about a product and how the reviews affect future sales of the product. It can also help sellers to better market and sell their products.
Since there is a lot of data available about the sales and reviews, it becomes tedious to process all the data and we have to resort to big data technologies to handle the data in a smooth fashion and to get efficient results.
For our analysis we have taken review data from Amazon and then processed it to rank the products based on the average star rating for every year and by doing so we can see the trends in product sales every year.


## Methodology

Architecture

We created a data streamer using Java which streamed data from 6 different data files of 2 GB each and passes to Kafka producer. Kafka consumer consumes these data from kafka at a 1 second wait time. Now, in the consumer we manipulate the consumer record and store in elastic search on the cloud. On the other hand the consumer record is also stored in cassandra locally.
On the cloud the elastic search is connected to kibana which is also on the cloud in which we created our own dashboard of visualizations. Here we can observe the real time visualizations of the streamed data.
While locally we use pyspark to import data from cassandra into dataframe and perform various data analysis to achieve yearly product trend analysis.

![](/bdarch.jpg)



Data Scraping

For this project we have used Amazon review data which we have accessed from AWS.
We get a lot of datasets in Amazon and each file is about 2gb in size. The data is stored in the form of JSONs where each record represents a review. The reviews have the following columns.




We have used the columns “product_id”, “star_rating” and “date” for our analysis.



Data Storing

For storing data we have used Elastic Search and Cassandra. We have used both these because of the different facilities it provides. We have listed the reasons for using both.

Elasatic Search: We have used Elastic Search as we wanted to use Kibana for Visualizing real time data.

Cassandra: We have used Cassandra as it is a distributed NoSQL Database. We used this because it is more Scalable. The main reason we are going for this database is because we wanted the whole historic data and analyse the top products sold every year.

Data Processing

Once we have handled the data flow, we now have to do processing on this to get some analysis reports.
    For our analysis we have done the processing using PySpark. Here we have taken the data from Cassandra in the form of a dataframe and now we have dropped the columns which we do not require for our analysis. We have only kept “product_id”, “star_rating” and “date”; Now the “date” is the complete date and we require only the year for our analysis so we update this column.
    Once we get the data in the desired form we now filter the data based on the year. After that we create a new dataframe where we take all the unique product_ids and find the average star_rating, then we order it based on average star_rating, finally we take the top three rows to get the top three products and create a pi chart for visualization.









## Visualization

Real Time Visualization of streamed data on Kibana


![](/bigdata.png)











Data Analysis Visualization on complete Data at an instance

Here is the pie chart distribution of top 3 products each year on the basis of star rating.
![](/bdpie1.jpg)
![](/bdpie2.jpg)
![](/bdpie3.jpg)
![](/bdpie4.jpg)



## Obstacles

Since, we didn’t had a UI from where our data was originating, so we had a lot of difficulty to stream our data into kafka producer. So, we created our own streaming code to stream our data into a queue and provide it to the producer. 

Moreover, we had limited resources so it was a little difficult to store huge data in our local machine. Therefore, we used elastic search and kibana on the cloud.

## Takeaways

The primary and most important learning from this project was to make a data set scalable and run it in a distributed environment. This enables the model to be run in various different systems in a distributed way and still arrive at the same result. This project also required us to be able to link several different tools to be able to transfer the data to scale it, stream it and manipulate to the needs. We also learnt how to visualize the data which was passed through multiple tools for the same. A key takeaway from this project was, we learned how to create a scalable architecture for handling big data by streaming it, visualizing it and finally performing the desired analysis. 

## Future Work 

In the future, there are several ways in which we can build on our project. Firstly, in this project we only used the data from past 5 years due to lack of hardware resources. However, we would like to use a larger dataset to make the analysis more useful and realistic. We would like to make this project work in real time updating itself with each buy from the customer in amazon or any other e-commerce website. We have shown this using an already existing database and streaming it but this can be done in real time too keeping the visualization and the analysis of the data always updated. We have also discussed the idea of including Machine Learning techniques to analyse the user shopping trends and suggest the best product that would be customised according to the user needs. This would be a big step up as we would be analyzing each click of the user to suggest a product based on already existing data. Lastly, since the data will be stored within the environment, we would like to implement Elastic Search to search based on different categories or even query using keywords.


## Conclusion

As per our objective, we used the data from AWS which consisted of user ratings of the products on Amazon. We used Kafka to extract AWS Database, and convert it into live stream. Kibana and ElasticSearch were used to visualize the data real time with a refresh rate of 5 sec. On the other hand, we use Cassandra for storing the historic data. Finally, we used Pyspark to extract the data from Cassandra and on which we did feature engineering and presented the data as per our the requirement of our analysis, in this case was top 3 products with the best average star rating for each year.

## References

https://kafka.apache.org/documentation/

https://www.elastic.co/guide/index.html

http://cassandra.apache.org/doc/latest/

https://spark.apache.org/docs/latest/


