/*
 Helper test methods to add a couple of items to our list
 */

function addSingleJsonPost(){

    var item = {
        "uid":"First-blog-post-ever_aa826639-d829-48b1-b394-8f4ef0d41016",
        "state":"Nascent",
        "created":{
            "time":1343224588265
        },
        "title":{
            "content":"Finally, First *blog post* ever"
        },
        "headline":{
            "content":"First *blog post* ever"
        },
        "summary":{
            "content":"This journey took a long time. It almost never started and then it had many close calls. This post narrates this story."
        },
        "content":[{
                "text":{
                    "content":"It would have been so easy -- just pick a blogging platform and  voila one has a blog with a lot of functionality. In case you really want to own it just roll out one of the many blogging suites on your server. But **none** can boast of the pain and joy creating your own software  brings with it. The creativity, the decision making, the struggles to implement the vision at the micro and macro level,  and other nitty gritties spice up the adventure."
                }
            },{
                "headline":{
                    "content":"The need"
                },
                "text":{
                    "content":"<p class=\"bla\">[Twitter](http://twitter.com) is too terse;  most journals have more ads then real content;  a private diary isn't social enough.</p><p>A good blog is about content, content and content.  A great or unique writing style makes the difference when content is not a problem.  All other things equal a great design and technological schnickschnack give the blog that aura of invincibility.</p>"
                }

        }],
        "tags":[{
            "tag":"blog"
        },{
            "tag":"writing"
        }],
        "rating":{
            "likes":1,
            "dislikes":0
        },
        "comments":[{
            "uid": "123",
            "user":{
                "twitterId":"@agreeable"
            },
            "text":"totally agree",
            "created":{
                "time":1343224588265
            },
            "rating":{
                "likes":1,
                "dislikes":0
            },
            "replies":[{
                "uid":"7890",
                "user":{
                    "twitterId":"@disagreeable"
                },
                "text":"totally agree with this commenter",
                "created":{
                    "time":1343224588265
                },
                "rating":{
                    "likes":0,
                    "dislikes":1
                },
                "replies":[]
            }]
        },{
            "uid":"1234",
            "user":{
                "twitterId":"@disagreeable"
            },
            "text":"totally disagree",
            "created":{
                "time":1343224588265
            },
            "rating":{
                "likes":0,
                "dislikes":1
            },
            "replies":[{
                "uid":"5678",
                "user":{
                    "twitterId":"@agreeable"
                },
                "text":"totally disagree with this commenter",
                "created":{
                    "time":1343224588265
                },
                "rating":{
                    "likes":0,
                    "dislikes":1
                },
                "replies":[]
            }]
        }]
    }
    var post = HaBlog.CreatePostFromJSon(item);
    HaBlog.postListController.addPost(post);
}