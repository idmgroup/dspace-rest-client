# dspace-rest-client
Java REST Client for DSpace

## Build hints

### Update WADL

{% highlight shell %}
$ ./scripts/get-wadl.sh
{% endhighlight %}


### Regenerate DSpaceRestClient.java

{% highlight shell %}
$ ./scripts/wadl2java.pl src/main/resources/org/dspace/demo/rest/application.wadl scripts/DSpaceRestClient.java.tt >| src/main/java/com/idmgroup/dspace/rest/DSpaceRestClient.java
{% endhighlight %}

