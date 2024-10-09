# Deep Thought

	A Reinforcement Learning engine backed by a knowledge graph. Deepthought relies on Q-Learning
	and a specific data schema that consists of "atomic" chunks of data that are treated as symbols. 
	To make interaction with the internal schema simple the API provides multiple "learn" endpoints 
	that can consume either unstructured text blocks or JSON. 

# Getting Started

	If you want to deploy the system yourself or contribute to this project, you'll need to follow the
	following steps. If you want to give it a try you can skip forward to the **Using the API** section
	and point your requests to http://206.189.234.126:9080

  1. **What you'll need**

      - [Neo4j](https://neo4j.com)
      - [Java 8 SDK](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html)
      - [Postman](https://www.postman.com/)
    
  2. **Set up the database**
	
  3. **Set schema constraints in neo4j**
	
  4. **Configure the API**
	
  5. **Starting the API**


# Using the API

Deep Thought uses [OpenAPI](https://swagger.io/specification/) for managing the ReST API endpoints. You can access the [Deep Thought Swagger spec](http://206.189.234.126:9080/swagger-ui.html). If you have deployed this locally the address to access the swagger spec is [http://localhost:9080/swagger-ui.html](http://localhost:9080/swagger-ui.html)

  - **Learn**
  - **Predict**


# Fundamental Concepts and Assumptions

Deepthought treats all data as if structure is an inherent property of data and that all data is 
	just a sequence of symbols that can be used to make categorical predictions. 

