footer: © 2016 Sumanth Chinthagunta
slidenumbers: true

# [fit] Low Latency Change Data Capture with
# [fit] Maxwell
# [fit] and
# [fit] Kafka
![](https://raw.githubusercontent.com/spring-projects/spring-cloud/gh-pages/img/project-icon-large.png)

---

![left](./images/sumanth.png)
# Me

Sumanth Chinthagunta ([@xmlking](http://twitter.com/xmlking))
SaaS Architect
Unitedhealth Group
[xmlking@gmail.com](mailto:xmlking@gmail.com)

---

# low latency dataflow

A low latency *Change Data Capture* flow to continuously replicate data from OLTP(MySQL) to OLAP(NoSQL) systems with no impact to the source.

---

# Real-time dataflow ...
![](./images/cdc-architecture.png)

---

# Features

* Multi-tenant: can contain data from many different databases, support multiple consumers.
* Flexible CDC: Capture changes from many data sources and types.
    * Source consistency preservation. No impact to the source.
    * Both DML (INSERT/UPDATE/DELETE) and DDL (ALTER/CREATE/DROP) are captured non invasively.
    * Produce Logical Change Records (LCR) in JSON format.
    * Commits at the source are grouped by transaction.
* Flexible Consumer Dataflows: consumer dataflows can be implemented in Apache NiFi, Flink, Spark or Apex
    * Parallel processing data filtering, transformation and loading.
* Flexible Databus: store LCRs in **Kafka** streams for durability and pub-sub semantics.
    * Use *only* Kafka as input for all consumer dataflows.
    * Feed data to many client types (real-time, slow/catch-up, full bootstrap).
    * Consumption from an arbitrary time point in the change stream including full bootstrap capability of the entire data.
    * Guaranteed in-commit-order and at-least-once delivery.
    * Partitioned consumption (partitioned data to different Kafka topics based on database name, table or any field of LCR)
* Both batch and near real time delivery.

---

# [fit] Demo

---


# Thanks!

Sumanth Chinthagunta ([@xmlking](http://twitter.com/xmlking))

* Apache NiFi: https://nifi.apache.org/
* Maxwell: http://maxwells-daemon.io/
* This Presentation: https://github.com/xmlking/cdc-kafka-hadoop

---

# Image Credits

* http://i.imgur.com/atz81.jpg
* http://theroomermill.net/wp-content/uploads/2014/06/island-house.jpg
