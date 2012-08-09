Getting Started
=====

Use the sbt script


	> ./sbt

	> test            #run tests

	> gen-idea        #generate idea files

	> publish-local   #publish to the local ivy repo


Afterwards you can use play2 to launch a web-container with some web-services. Install play 2.0.3 http://download.playframework.org/releases/play-2.0.3.zip by unzipping it and make the folder a part of the PATH. Remember to publish the artifacts locally as shown above (./sbt publish-local) and then move to the folder play2 and invoke

	> play run


URIs:

To create a new blog entry move into the domain folder and execute:

	>curl -XPOST -H "content-type: application/json" http://localhost:9000/blog --data @nascent.json

Note the uid created

	< HTTP/1.1 201 Created
	< Content-Type: text/plain; charset=utf-8
	< Location: First-blog-post-ever_cc059a23-a556-4828-ba60-510bf1664974
	< Content-Length: 57
	<
	* Connection #0 to host localhost left intact
	* Closing connection #0
	First-blog-post-ever_cc059a23-a556-4828-ba60-510bf1664974%


GET http://localhost:9000/blog/First-blog-post-ever_cc059a23-a556-4828-ba60-510bf1664974

To get all blog entries

GET http://localhost:9000/blog


Code
======

Checkout the tests.





Acknowledgements
========

https://github.com/paulp/sbt-extras for the sbt script







