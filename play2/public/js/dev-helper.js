/*
 Helper test methods to add a couple of items to our list
 */

function addSinglePost(){
    testPost = HaBlog.Post.create({
        uid:"1",
        title:"Test Title",
        headline:"headline",
        tags:[
            {
                tag:'blog'
            },
            {
                tag:'write'
            }
        ],
        author:'pprang',
        sections:[
            {
                text:'It would have been so easy -- just pick a blogging platform and\n voila one has a blog with a lot of functionality. In case you really want to own it just roll out one of the many blogging suites on your server. But **none** can boast of the pain and joy creating your own software\n        brings with it. The creativity, the decision making, the struggles to implement the vision at the micro and macro level,\n        and other nitty gritties spice up the adventure.'
            },
            {
                headline:'The need',
                text:"<p class =\"bla\">[Twitter](http://twitter.com) is too terse;\n most journals have more ads then real content;\n a private diary isn't social enough.</p><p>A good blog is about content, content and content.\n A great or unique writing style makes the difference when content is not a problem.\n        All other things equal a great design and technological schnickschnack give the blog that aura of invincibility.</p>"
            }
        ],
        created:moment()
    });

    HaBlog.postsController.addPost(testPost);
}