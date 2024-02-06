# cb-explainer
simple application to get explain plan of queries to couchbase

## Why?

Some queries were running faster on DEV environment compared to PROD environment in my work and I wanted to compare the explain plan in both environments.

I ended up comparing the explain plans as JSON but I thought it might be nice if I have visualizations as well.

## Images

<img src="./cb-explainer.PNG">

<img src="./cb-explainer.gif">

## usage
create file cb.properties (that contains connection properties) beside the jar or inside root directory

file has to be similar to this:

connection=couchbase://localhost

user=username

pass=password

bucket=bucketName

scope=_default


then run the application

```
java -jar cb-explainer-0.0.1-SNAPSHOT.jar
```

and then click 'Connect' and if successful add your queries separated by new lines and click 'Explain'


## Attributions
<a href="https://www.freepik.com/free-vector/branding-identity-corporate-vector-logo-design_22116270.htm#query=logo&position=5&from_view=keyword&track=sph&uuid=115a6eec-82f2-4fe7-bc12-3c8bb4b7e6e9">Image by Rochak Shukla</a> on Freepik
